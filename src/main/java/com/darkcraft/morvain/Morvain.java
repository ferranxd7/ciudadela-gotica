package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Morvain, las Tierras Marchitas (parte 7 del plan, respuestas 85-96 y 125-132): la dimension {@code darkcraft:morvain}
 * (el mapa de {@link Terreno}, 16.000 x 16.000, de 0 a 767 de alto, siempre al caer la tarde). Lo que no es generar:
 * <ul>
 * <li>los registros del generador ({@code darkcraft:morvain}, {@link MorvainGenerador}) y de sus biomas
 * ({@link MorvainBiomas});</li>
 * <li><b>cruzar</b>: el portal de lo alto de la Fortaleza de Hueso lleva al Arco del Umbral ({@link Llegada}) y el del
 * arco devuelve a donde se cruzo (se apunta en el jugador, {@code darkcraft_vuelta});</li>
 * <li>el Arco del Umbral, la primera vez que llega alguien: el nombre de su santuario y la guia sentada al lado; y siempre
 * que haya alguien cerca, el remolino del portal (no se guarda);</li>
 * <li><b>morir en Morvain</b> sin haber descansado en un santuario de alli: se reaparece en el arco (el punto de
 * reaparicion de antes se guarda y se devuelve al volver por el portal);</li>
 * <li>el nombre del sitio para el Espejo (y los carteles de zona): la region, o el arco.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Morvain {
    private Morvain() {}

    public static final ResourceKey<Level> NIVEL = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(DarkCraftMod.MODID, "morvain"));
    /** Lo alto del mundo (de 0 a ALTO - 1). */
    public static final int ALTO = 768;

    public static final DeferredRegister<Codec<? extends ChunkGenerator>> GENERADORES =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, DarkCraftMod.MODID);
    public static final DeferredRegister<Codec<? extends BiomeSource>> FUENTES = DeferredRegister.create(Registries.BIOME_SOURCE, DarkCraftMod.MODID);
    public static final RegistryObject<Codec<? extends ChunkGenerator>> GENERADOR = GENERADORES.register("morvain", () -> MorvainGenerador.CODEC);
    public static final RegistryObject<Codec<? extends BiomeSource>> BIOMAS = FUENTES.register("morvain", () -> MorvainBiomas.CODEC);

    public static void registrar(IEventBus bus) {
        GENERADORES.register(bus);
        FUENTES.register(bus);
    }

    public static boolean es(Level l) {
        return l.dimension() == NIVEL;
    }

    public static ServerLevel nivel(MinecraftServer s) {
        return s.getLevel(NIVEL);
    }

    // ------------------------------------------------------------------ cruzar el portal

    /** Lo que se guarda en el jugador para volver: de donde vino y su punto de reaparicion de antes. */
    private static final String VUELTA = "darkcraft_vuelta";
    /** Hasta que tick del servidor no vuelve a cruzar (recien cruzado), por jugador. */
    private static final Map<UUID, Integer> ESPERA = new HashMap<>();
    /**
     * Los que acaban de entrar en un portal y el bloque que tocaron: se les lleva al acabar el tick del servidor (no en
     * medio de su movimiento, que el juego aun esta moviendo al jugador por donde estaba).
     */
    private static final Map<UUID, BlockPos> PENDIENTES = new HashMap<>();

    /**
     * Un jugador toca el portal (el de la fortaleza o el de vuelta del arco). Devuelve true si se le ha llevado al otro
     * lado (o acaba de cruzar y no hay que hacer nada); false si Morvain no existe (y el portal le empuja como antes).
     */
    public static boolean portal(ServerPlayer p, BlockPos pos) {
        ServerLevel m = nivel(p.server);
        if (m == null) return false;
        // montado en el corcel: se va en niebla y se cruza a pie
        if (p.getVehicle() instanceof com.darkcraft.montura.Corcel c) c.quitar();
        if (p.isPassenger()) return false;
        int ahora = p.server.getTickCount();
        Integer hasta = ESPERA.get(p.getUUID());
        if (hasta != null && ahora < hasta) return true;
        ESPERA.put(p.getUUID(), ahora + 60);
        PENDIENTES.put(p.getUUID(), pos.immutable());
        return true;
    }

    private static CompoundTag datos(Player p) {
        return p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static void guardar(Player p, CompoundTag d) {
        p.getPersistentData().put(Player.PERSISTED_NBT_TAG, d);
    }

    /** De la fortaleza (o de donde este el portal) al Arco del Umbral. */
    public static void cruzar(ServerPlayer p, BlockPos pos, ServerLevel m) {
        // la vuelta: delante del portal, de espaldas a el, del lado por el que ha entrado (si alli hay suelo; si no, del otro)
        double dir = p.getZ() >= pos.getZ() + 0.5 ? 1 : -1;
        double vz = pos.getZ() + 0.5 + dir * 3;
        double vy = suelo(p.serverLevel(), p.getX(), p.getY(), vz);
        if (Double.isNaN(vy)) {
            double otro = suelo(p.serverLevel(), p.getX(), p.getY(), pos.getZ() + 0.5 - dir * 3);
            if (!Double.isNaN(otro)) {
                dir = -dir;
                vz = pos.getZ() + 0.5 + dir * 3;
                vy = otro;
            } else vy = p.getY();
        }
        CompoundTag d = datos(p);
        CompoundTag v = new CompoundTag();
        v.putString("dim", p.level().dimension().location().toString());
        v.putDouble("x", p.getX());
        v.putDouble("y", vy);
        v.putDouble("z", vz);
        v.putFloat("giro", dir > 0 ? 0f : 180f);
        CompoundTag antes = d.getCompound(VUELTA);
        if (antes.contains("respawn")) v.put("respawn", antes.getCompound("respawn"));
        d.put(VUELTA, v);
        guardar(p, d);
        ServerLevel de = p.serverLevel();
        de.playSound(null, pos, DarkCraftMod.SFX_PORTAL_ABRE.get(), SoundSource.BLOCKS, 1.2f, 1.4f);
        de.sendParticles(ParticleTypes.REVERSE_PORTAL, p.getX(), p.getY() + 1, p.getZ(), 40, 0.4, 0.8, 0.4, 0.2);
        preparar(m);
        BlockPos pies = Llegada.pies();
        p.teleportTo(m, pies.getX() + 0.5, pies.getY(), pies.getZ() + 0.5, 180f, 0f);
        m.playSound(null, pies, DarkCraftMod.SFX_PORTAL_ABRE.get(), SoundSource.BLOCKS, 1.0f, 0.8f);
        m.sendParticles(ParticleTypes.REVERSE_PORTAL, pies.getX() + 0.5, pies.getY() + 1, pies.getZ() + 0.5, 60, 0.5, 1.0, 0.5, 0.25);
        m.sendParticles(ParticleTypes.SOUL, pies.getX() + 0.5, pies.getY() + 0.5, pies.getZ() + 0.5, 12, 0.6, 0.3, 0.6, 0.02);
        com.darkcraft.story.Logros.dar(p, "morvain");
        com.darkcraft.rol.Rol.zona(p, "darkcraft.zona.morvain");
        DarkCraftMod.LOGGER.info("[DarkCraft] {} cruza a Morvain (vuelta a {} {} {} {})", p.getGameProfile().getName(), v.getString("dim"),
                Math.round(v.getDouble("x")), Math.round(v.getDouble("y")), Math.round(v.getDouble("z")));
    }

    /** Del arco de Morvain a donde se cruzo (o al punto de aparicion del mundo si no se sabe). */
    public static void volver(ServerPlayer p) {
        CompoundTag d = datos(p);
        CompoundTag v = d.getCompound(VUELTA);
        ServerLevel dest = null;
        if (v.contains("dim")) {
            ResourceLocation id = ResourceLocation.tryParse(v.getString("dim"));
            if (id != null) dest = p.server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
        }
        double x, y, z;
        float giro;
        if (dest != null && dest.dimension() != NIVEL) {
            x = v.getDouble("x");
            y = v.getDouble("y");
            z = v.getDouble("z");
            giro = v.getFloat("giro");
        } else {
            dest = p.server.overworld();
            BlockPos s = dest.getSharedSpawnPos();
            x = s.getX() + 0.5;
            y = s.getY();
            z = s.getZ() + 0.5;
            giro = 0f;
        }
        // el punto de reaparicion de antes, si se le puso el del arco al morir aqui
        if (v.contains("respawn")) {
            BlockPos arco = Llegada.pies();
            if (p.getRespawnDimension() == NIVEL && arco.equals(p.getRespawnPosition())) {
                CompoundTag r = v.getCompound("respawn");
                ResourceLocation rd = ResourceLocation.tryParse(r.getString("dim"));
                if (r.contains("x") && rd != null)
                    p.setRespawnPosition(ResourceKey.create(Registries.DIMENSION, rd), new BlockPos(r.getInt("x"), r.getInt("y"), r.getInt("z")),
                            r.getFloat("ang"), r.getBoolean("forzado"), false);
                else p.setRespawnPosition(Level.OVERWORLD, null, 0f, false, false);
            }
            v.remove("respawn");
            d.put(VUELTA, v);
            guardar(p, d);
        }
        dest.getChunk(Mth.floor(x) >> 4, Mth.floor(z) >> 4);
        ServerLevel de = p.serverLevel();
        de.playSound(null, p.blockPosition(), DarkCraftMod.SFX_PORTAL_ABRE.get(), SoundSource.BLOCKS, 1.2f, 1.4f);
        p.teleportTo(dest, x, y, z, giro, 0f);
        dest.playSound(null, BlockPos.containing(x, y, z), DarkCraftMod.SFX_PORTAL_ABRE.get(), SoundSource.BLOCKS, 1.0f, 0.8f);
        dest.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y + 1, z, 40, 0.4, 0.8, 0.4, 0.2);
        DarkCraftMod.LOGGER.info("[DarkCraft] {} vuelve de Morvain a {} {} {} {}", p.getGameProfile().getName(), dest.dimension().location(),
                Math.round(x), Math.round(y), Math.round(z));
    }

    /**
     * Donde ponerse de pie en (x, z) cerca de la altura y: primero a esa altura o un poco por encima (si alli hay un cascote
     * se sube encima), despues hasta tres por debajo (nunca mas: bajo la terraza del portal estan las salas de la torre).
     * NaN si no hay.
     */
    private static double suelo(ServerLevel l, double x, double y, double z) {
        int bx = Mth.floor(x), bz = Mth.floor(z), y0 = Mth.floor(y);
        for (int yy = y0; yy <= y0 + 4; yy++) if (dePie(l, bx, yy, bz)) return yy;
        for (int yy = y0 - 1; yy >= y0 - 3; yy--) if (dePie(l, bx, yy, bz)) return yy;
        return Double.NaN;
    }

    /** Si en (x, y, z) se puede estar de pie: algo firme debajo y dos huecos libres. */
    private static boolean dePie(ServerLevel l, int x, int y, int z) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos(x, y - 1, z);
        if (l.getBlockState(m).getCollisionShape(l, m).isEmpty()) return false;
        m.set(x, y, z);
        if (!l.getBlockState(m).getCollisionShape(l, m).isEmpty()) return false;
        m.set(x, y + 1, z);
        return l.getBlockState(m).getCollisionShape(l, m).isEmpty();
    }

    // ------------------------------------------------------------------ el Arco del Umbral

    /** La primera vez que llega alguien: los chunks de la plaza, el nombre del santuario y la guia (una vez). */
    public static void preparar(ServerLevel m) {
        for (int cx = (Llegada.X - Llegada.MEZCLA) >> 4; cx <= (Llegada.X + Llegada.MEZCLA) >> 4; cx++)
            for (int cz = (Llegada.Z - Llegada.MEZCLA) >> 4; cz <= (Llegada.PZ + 4) >> 4; cz++) m.getChunk(cx, cz);
        BlockPos s = Llegada.santuario();
        com.darkcraft.santuario.Santuarios.ponerEn(m, "morvain:llegada", s, "darkcraft.santuario.morvain_llegada");
        Datos d = Datos.get(m.getServer());
        if (!d.hecho("guia")) {
            BlockPos g = Llegada.guia();
            // mirando hacia el arco, por donde llega la gente
            if (com.darkcraft.personajes.Guia.poner(m, g, g.offset(0, 0, 6), s) != null) {
                d.marcar("guia");
                DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: la guia, junto al santuario del Arco del Umbral: {}", g.toShortString());
            }
        }
        remolino(m);
    }

    /** El remolino del portal de vuelta (no se guarda: se pone si falta). */
    static void remolino(ServerLevel m) {
        BlockPos q = Llegada.portal();
        if (!m.isLoaded(q)) return;
        AABB caja = new AABB(q).inflate(3);
        if (!m.getEntitiesOfClass(com.darkcraft.fortaleza.Remolino.class, caja).isEmpty()) return;
        com.darkcraft.fortaleza.Remolino r = DarkCraftMod.REMOLINO.get().create(m);
        if (r == null) return;
        r.moveTo(q.getX() + 0.5, q.getY(), q.getZ() + 0.5, 0f, 0f);
        r.abierto(1f);
        r.abiertoO = 1f;
        m.addFreshEntity(r);
    }

    /** Si hay alguien cerca del Patio del Juramento o del Observatorio (se mira cada segundo). */
    private static boolean cercaJuramento, cercaObservatorio;

    /** Al acabar cada tick, los que entraron en un portal; y cada segundo, con alguien cerca del arco, su remolino. */
    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        MinecraftServer srv = e.getServer();
        ServerLevel m = nivel(srv);
        if (!PENDIENTES.isEmpty()) {
            Map<UUID, BlockPos> ahora = new HashMap<>(PENDIENTES);
            PENDIENTES.clear();
            for (Map.Entry<UUID, BlockPos> en : ahora.entrySet()) {
                ServerPlayer p = srv.getPlayerList().getPlayer(en.getKey());
                if (p == null || m == null || !p.isAlive()) continue;
                try {
                    if (p.level().dimension() == NIVEL) volver(p);
                    else cruzar(p, en.getValue(), m);
                } catch (RuntimeException ex) {
                    DarkCraftMod.LOGGER.error("[DarkCraft] Morvain: no se pudo cruzar el portal", ex);
                }
            }
        }
        // el molino de la Molinera (parte 7f): la reja de su puerta y su crujido con el viento
        if (m != null && srv.getTickCount() % 100 == 13) Molino.crujir(m);
        // la arboleda (parte 7h): con el Verdugo vencido, las sogas de los colgados se quedan con el lazo vacio
        if (m != null && srv.getTickCount() % 100 == 37) Arboleda.cuenta(m);
        // la laguna negra (parte 7j): con el Barquero vencido, sus faroles de almas se vuelven faroles de verdad
        if (m != null && srv.getTickCount() % 100 == 61) LagunaNegra.paz(m);
        // el monasterio de la Helada (parte 7k): de vez en cuando su campana rota tañe sola
        // (hasta que cae el Abad de la Cumbre, parte 7l: entonces da su ultimo toque y calla)
        if (m != null && srv.getTickCount() % 100 == 83 && !Ermita.enPaz(srv)) Monasterio.campana(m);
        // la ermita (parte 7l): con el Abad caido, los hermanos de la iglesia que quedaban de rodillas descansan
        if (m != null && srv.getTickCount() % 100 == 91) Ermita.paz(m);
        // el Patio del Juramento (parte 7n): su rastrillo (cae al empezar la pelea, se alza al acabar) y, con el Condestable caido,
        // la reja de su torre, la Puerta Oeste abierta y su guardia en paz; cada tick con alguien cerca
        if (m != null) {
            if (srv.getTickCount() % 20 == 11) {
                cercaJuramento = false;
                boolean cercaPuerta = false;
                for (ServerPlayer p : m.players()) {
                    if (Math.abs(p.getX() - (Juramento.CX + Juramento.CENTRO_U)) < 140 && Math.abs(p.getZ() - Juramento.CZ) < 140) cercaJuramento = true;
                    else if (Math.abs(p.getX() - Capital.CX) < 100 && Math.abs(p.getZ() - Capital.CZ) < 100) cercaPuerta = true;
                }
                // con el Condestable caido, la Puerta Oeste se abre aunque el patio quede lejos (su chunk puede no estar cargado)
                if (cercaPuerta && !cercaJuramento) Juramento.paz(m);
            }
            if (cercaJuramento) Juramento.tick(m);
            // el Observatorio (parte 7p): su sello (se cierra al empezar la pelea con la Rectora y se rompe al acabar) y, con
            // ella caida, la reja de su celda; cada tick con alguien cerca
            if (srv.getTickCount() % 20 == 13) {
                cercaObservatorio = false;
                for (ServerPlayer p : m.players())
                    if (Observatorio.distancia(p.getX(), p.getZ()) < 120) cercaObservatorio = true;
            }
            if (cercaObservatorio) Observatorio.tick(m);
        }
        if (m == null || srv.getTickCount() % 20 != 7) return;
        for (ServerPlayer p : m.players())
            if (Llegada.cerca(p.getX(), p.getZ(), 160)) {
                remolino(m);
                break;
            }
        // quien entra en Velmor (parte 7e): su logro
        for (ServerPlayer p : m.players())
            if (!p.isSpectator() && Velmor.dentro(p.getX(), p.getZ()) && p.getY() < 140) com.darkcraft.story.Logros.dar(p, "velmor");
        // y quien entra en la Arboleda de los Ahorcados (parte 7g)
        for (ServerPlayer p : m.players())
            if (!p.isSpectator() && Arboleda.dentro(p.getX(), p.getZ()) && p.getY() < 160) com.darkcraft.story.Logros.dar(p, "arboleda");
        // y quien entra en la Aldea de los Pilotes (parte 7i)
        for (ServerPlayer p : m.players())
            if (!p.isSpectator() && Pilotes.dentro(p.getX(), p.getZ()) && p.getY() < 120) com.darkcraft.story.Logros.dar(p, "pilotes");
        // y quien entra en el Monasterio de la Helada (parte 7k): la campana le recibe la primera vez
        for (ServerPlayer p : m.players())
            if (!p.isSpectator() && Monasterio.dentro(p.getX(), p.getZ()) && Math.abs(p.getY() - Monasterio.PISO) < 30
                    && !com.darkcraft.story.Logros.tiene(p, "monasterio")) {
                com.darkcraft.story.Logros.dar(p, "monasterio");
                p.playNotifySound(DarkCraftMod.SFX_MONASTERIO_CAMPANA.get(), net.minecraft.sounds.SoundSource.AMBIENT, 1.4f, 0.95f);
            }
        // y quien cruza la Puerta Oeste de la Capital (parte 7m): por esta puerta no pasa nadie... salvo el
        for (ServerPlayer p : m.players())
            if (!p.isSpectator() && Capital.intramuros(p.getX(), p.getZ()) && Math.abs(p.getY() - Capital.PISO) < 40)
                com.darkcraft.story.Logros.dar(p, "capital");
        // y quien entra en el Patio del Juramento (parte 7n)
        for (ServerPlayer p : m.players())
            if (!p.isSpectator() && Juramento.enPatio(p.getX(), p.getZ()) && Math.abs(p.getY() - Juramento.PISO) < 20)
                com.darkcraft.story.Logros.dar(p, "juramento");
        // y quien entra en la Academia Espectral (parte 7o)
        for (ServerPlayer p : m.players())
            if (!p.isSpectator() && Academia.dentro(p.getX(), p.getZ()) && Math.abs(p.getY() - Academia.PISO) < 40)
                com.darkcraft.story.Logros.dar(p, "academia");
        // y quien entra en la rotonda del Observatorio (parte 7p)
        for (ServerPlayer p : m.players())
            if (!p.isSpectator() && Observatorio.enRotonda(p.getX(), p.getZ()) && Math.abs(p.getY() - Observatorio.PISO) < 12)
                com.darkcraft.story.Logros.dar(p, "observatorio");
        // y quien entra en el Puerto Varado, y quien sube a lo alto del Faro de los Huesos (parte 7q)
        for (ServerPlayer p : m.players()) {
            if (p.isSpectator() || Math.abs(p.getX() - Varadero.CX) > 200 || Math.abs(p.getZ() - Varadero.CZ) > 200) continue;
            if (Varadero.dentro(p.getX(), p.getZ()) && Math.abs(p.getY() - Varadero.PISO) < 30) com.darkcraft.story.Logros.dar(p, "puerto");
            if (Varadero.arribaDelFaro(p.getX(), p.getY(), p.getZ())) com.darkcraft.story.Logros.dar(p, "faro");
        }
    }

    // ------------------------------------------------------------------ morir en Morvain

    /**
     * Quien muere en Morvain sin un santuario de alli reaparece en el Arco del Umbral (y no en Brumavela, al otro lado del
     * portal). Su punto de reaparicion de antes se guarda: al volver por el portal se le devuelve.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onMuerte(LivingDeathEvent e) {
        if (e.isCanceled() || !(e.getEntity() instanceof ServerPlayer p) || p.level().dimension() != NIVEL) return;
        if (p.getRespawnDimension() == NIVEL && p.getRespawnPosition() != null) return;
        CompoundTag d = datos(p);
        CompoundTag v = d.getCompound(VUELTA);
        if (!v.contains("respawn")) {
            CompoundTag r = new CompoundTag();
            r.putString("dim", p.getRespawnDimension().location().toString());
            BlockPos rp = p.getRespawnPosition();
            if (rp != null) {
                r.putInt("x", rp.getX());
                r.putInt("y", rp.getY());
                r.putInt("z", rp.getZ());
                r.putFloat("ang", p.getRespawnAngle());
                r.putBoolean("forzado", p.isRespawnForced());
            }
            v.put("respawn", r);
            d.put(VUELTA, v);
            guardar(p, d);
        }
        p.setRespawnPosition(NIVEL, Llegada.pies(), 180f, true, false);
    }

    @SubscribeEvent
    public static void onSalir(PlayerEvent.PlayerLoggedOutEvent e) {
        ESPERA.remove(e.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent e) {
        ESPERA.clear();
        PENDIENTES.clear();
    }

    // ------------------------------------------------------------------ el nombre del sitio

    /** La clave del nombre del sitio de Morvain donde esta el jugador (el arco o la region). */
    public static String lugar(ServerPlayer p) {
        if (Llegada.cerca(p.getX(), p.getZ(), 34) && Math.abs(p.getY() - Llegada.Y) < 30) return "darkcraft.morvain.lugar.umbral";
        // Velmor, el pueblo marchito (parte 7e)
        if (Velmor.dentro(p.getX(), p.getZ()) && p.getY() < 140) return "darkcraft.morvain.lugar.velmor";
        // el molino de la Molinera (parte 7f)
        if (Molino.dentro(p.getX(), p.getZ()) && p.getY() < 140) return "darkcraft.morvain.lugar.molino";
        // la Arboleda de los Ahorcados (parte 7g)
        if (Arboleda.dentro(p.getX(), p.getZ()) && p.getY() < 160) return "darkcraft.morvain.lugar.arboleda";
        // la Aldea de los Pilotes (parte 7i)
        if (Pilotes.dentro(p.getX(), p.getZ()) && p.getY() < 120) return "darkcraft.morvain.lugar.pilotes";
        // la Laguna Negra (parte 7j)
        if (LagunaNegra.dentro(p.getX(), p.getZ()) && p.getY() < 120) return "darkcraft.morvain.lugar.laguna";
        // el Monasterio de la Helada (parte 7k)
        if (Monasterio.dentro(p.getX(), p.getZ()) && Math.abs(p.getY() - Monasterio.PISO) < 30) return "darkcraft.morvain.lugar.monasterio";
        // la escalera de los penitentes y el calvario de la ermita, en la cumbre (parte 7l)
        if (Ermita.dentro(p.getX(), p.getZ()) && Math.abs(p.getY() - Ermita.PISO) < 30) return "darkcraft.morvain.lugar.ermita";
        if (Ermita.enEscalera(p.getX(), p.getZ()) && p.getY() > Monasterio.PISO - 10 && p.getY() < Ermita.PISO + 20)
            return "darkcraft.morvain.lugar.escalera_penitentes";
        // el Patio del Juramento, donde espera el Condestable (parte 7n)
        if (Juramento.dentro(p.getX(), p.getZ()) && Math.abs(p.getY() - Juramento.PISO) < 40) return "darkcraft.morvain.lugar.juramento";
        // la Puerta Oeste de la Capital (parte 7m)
        if (Capital.dentro(p.getX(), p.getZ()) && Math.abs(p.getY() - Capital.PISO) < 40) return "darkcraft.morvain.lugar.capital_puerta";
        // el Faro de los Huesos y el Puerto Varado (parte 7q)
        if (Math.abs(p.getX() - Varadero.CX) < 200 && Math.abs(p.getZ() - Varadero.CZ) < 200) {
            if (Varadero.enFaro(p.getX(), p.getZ()) && Math.abs(p.getY() - Varadero.PISO) < 60) return "darkcraft.morvain.lugar.faro";
            if (Varadero.dentro(p.getX(), p.getZ()) && Math.abs(p.getY() - Varadero.PISO) < 40) return "darkcraft.morvain.lugar.puerto";
        }
        // el Observatorio de la Rectora (parte 7p)
        if (Observatorio.dentro(p.getX(), p.getZ()) && Math.abs(p.getY() - Observatorio.PISO) < 40) return "darkcraft.morvain.lugar.observatorio";
        // la Academia Espectral y su Gran Biblioteca (parte 7o)
        if (Academia.dentro(p.getX(), p.getZ()) && Math.abs(p.getY() - Academia.PISO) < 50)
            return Academia.enBiblioteca(p.getX(), p.getZ()) && p.getY() < Academia.PISO + 30 ? "darkcraft.morvain.lugar.biblioteca"
                    : "darkcraft.morvain.lugar.academia";
        return "darkcraft.zona.morvain." + Terreno.region(p.getX(), p.getZ()).id;
    }

    /** Para la prueba: que hay en el arco. */
    public static String resumen(ServerLevel m) {
        BlockPos s = Llegada.santuario(), q = Llegada.portal();
        int portal = 0;
        for (int dx = -4; dx <= 4; dx++)
            for (int h = 0; h < 13; h++)
                if (m.getBlockState(q.offset(dx, h, 0)).is(DarkCraftMod.PORTAL_SOMBRA.get())) portal++;
        return "arco en " + s.toShortString() + ": santuario " + (m.getBlockState(s).is(DarkCraftMod.SANTUARIO.get()) ? "SI" : "NO")
                + " (" + com.darkcraft.santuario.Santuarios.nombre(m, s) + "), portal " + portal + " bloques, remolino "
                + m.getEntitiesOfClass(com.darkcraft.fortaleza.Remolino.class, new AABB(q).inflate(3)).size() + ", guia "
                + m.getEntitiesOfClass(com.darkcraft.personajes.Guia.class, new AABB(s).inflate(6)).size();
    }

    // ------------------------------------------------------------------ lo que se guarda

    /** Lo que ya se ha hecho una vez en Morvain (la guia del arco...). */
    public static final class Datos extends SavedData {
        public static final String ID = "darkcraft_morvain";
        final Set<String> hechos = new HashSet<>();

        public static Datos get(MinecraftServer srv) {
            return srv.overworld().getDataStorage().computeIfAbsent(Datos::load, Datos::new, ID);
        }

        boolean hecho(String k) {
            return hechos.contains(k);
        }

        void marcar(String k) {
            if (hechos.add(k)) setDirty();
        }

        static Datos load(CompoundTag t) {
            Datos d = new Datos();
            for (Tag x : t.getList("hechos", Tag.TAG_STRING)) d.hechos.add(x.getAsString());
            return d;
        }

        @Override
        public CompoundTag save(CompoundTag t) {
            ListTag l = new ListTag();
            for (String k : hechos) l.add(StringTag.valueOf(k));
            t.put("hechos", l);
            return t;
        }
    }

    /** Un aviso corto encima de la barra (para los comandos). */
    static void avisar(ServerPlayer p, String clave) {
        p.displayClientMessage(Component.translatable(clave).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
    }
}
