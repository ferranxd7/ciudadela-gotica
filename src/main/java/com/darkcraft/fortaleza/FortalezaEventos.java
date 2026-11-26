package com.darkcraft.fortaleza;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.caballeros.Caballeros;
import com.darkcraft.cine.CineShots;
import com.darkcraft.cine.Cinematics;
import com.darkcraft.cine.Shot;
import com.darkcraft.cine.Timeline;
import com.darkcraft.gen.TorreCupula;
import com.darkcraft.gen.FortalezaPlan;
import com.darkcraft.net.Net;
import com.darkcraft.story.Logros;
import com.darkcraft.story.Story;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Lo que pasa en las Fortalezas de Hueso: se levantan chunk a chunk cuando alguien se acerca (tambien en mundos de
 * antes), su zona (la isla y la montana del sur: lluvia sin tormenta, su musica; los enemigos van en sus puestos,
 * {@link FortalezaEnemigos}), el puente largo
 * que tiende la llave desde la montana, la puerta que abre la llave (cinematica), el amuleto en el altar (el capitulo 6
 * y el portal) y la forja del amuleto.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class FortalezaEventos {
    private FortalezaEventos() {}

    public static final ResourceKey<Biome> BIOMA = ResourceKey.create(Registries.BIOME, new ResourceLocation(DarkCraftMod.MODID, "fortaleza"));

    // ------------------------------------------------------------------ tareas programadas

    private record Tarea(int tick, Runnable r) {}

    private static final List<Tarea> TAREAS = new ArrayList<>();

    public static void despues(MinecraftServer srv, int ticks, Runnable r) {
        TAREAS.add(new Tarea(srv.getTickCount() + Math.max(0, ticks), r));
    }

    // ------------------------------------------------------------------ estado

    /** La fortaleza en cuya zona esta cada jugador. */
    private static final Map<UUID, Fortalezas.Sitio> ZONA = new HashMap<>();
    private static final Map<UUID, Integer> AVISOS = new HashMap<>();
    private static final ArrayDeque<long[]> COLA = new ArrayDeque<>();
    private static final Set<String> EN_COLA = new HashSet<>();

    public static Fortalezas.Sitio zona(Player p) {
        return ZONA.get(p.getUUID());
    }

    /** En la isla de la fortaleza (para los caballeros): a menos de 62 bloques del centro y a su altura. */
    static boolean enIsla(ServerPlayer p) {
        Fortalezas.Sitio s = ZONA.get(p.getUUID());
        if (s == null) return false;
        return Math.hypot(s.x() - p.getX(), s.z() - p.getZ()) < 80 && p.getY() > Fortalezas.Y - 14 && p.getY() < Fortalezas.Y + 80;
    }

    // ------------------------------------------------------------------ cada tick

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        MinecraftServer srv = e.getServer();
        int now = srv.getTickCount();
        if (!TAREAS.isEmpty()) {
            List<Tarea> ya = new ArrayList<>();
            TAREAS.removeIf(t -> {
                if (t.tick() > now) return false;
                ya.add(t);
                return true;
            });
            for (Tarea t : ya) {
                try {
                    t.r().run();
                } catch (Throwable x) {
                    DarkCraftMod.LOGGER.error("Fortaleza: fallo una tarea", x);
                }
            }
        }
        construirUno(srv);
        Puente.tick(srv);
        girarAmuletos(now);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.side != LogicalSide.SERVER || !(e.player instanceof ServerPlayer p)) return;
        if (p.tickCount % 20 != 13) return;
        if (p.level().dimension() != Level.OVERWORLD) {
            salir(p);
            return;
        }
        ServerLevel lvl = p.serverLevel();
        Fortalezas.Sitio s = Fortalezas.cercana(lvl.getSeed(), p.getX(), p.getZ(), 420);
        if (s != null) {
            encolar(lvl, s);
            double d = Math.hypot(s.x() - p.getX(), s.z() - p.getZ());
            // la montana del sur (y su puente) tambien es de la fortaleza
            double dm = Fortalezas.plan(lvl.getSeed(), s).montana.distancia(p.getX(), p.getZ());
            if (d < 150) devolverLlave(p, s);
            if (d < Fortalezas.ZONA || dm < 24) {
                entrar(p, s);
                asegurarAscensor(lvl, s, p);
                asegurarPortal(lvl, s);
            }
            else if (d > Fortalezas.ZONA + 25 && dm > 48) salir(p);
            if (d < 150) Logros.dar(p, "fortaleza");
        } else salir(p);
        if (p.tickCount % 40 == 13) Perdidos.tick(p);
        com.darkcraft.armas.LlaveHueso.alSostener(p);
    }

    /**
     * En la isla de la fortaleza no salen monstruos solos (ni zombis ni esqueletos ni fantasmas): la guardan los
     * caballeros zombi, que salen en grupos desde Caballeros. Abajo, en el suelo, todo sigue igual.
     */
    @SubscribeEvent
    public static void onSpawn(net.minecraftforge.event.entity.living.MobSpawnEvent.FinalizeSpawn e) {
        if (e.getY() < Fortalezas.Y - 40 || e.getEntity() instanceof com.darkcraft.caballeros.CaballeroZombi) return;
        if (!(e.getEntity() instanceof net.minecraft.world.entity.monster.Enemy)) return;
        var tipo = e.getSpawnType();
        if (tipo != net.minecraft.world.entity.MobSpawnType.NATURAL && tipo != net.minecraft.world.entity.MobSpawnType.CHUNK_GENERATION
                && tipo != net.minecraft.world.entity.MobSpawnType.PATROL) return;
        if (!(e.getLevel() instanceof ServerLevel sl) || sl.dimension() != Level.OVERWORLD) return;
        if (Fortalezas.cercana(sl.getSeed(), e.getX(), e.getZ(), 150) != null) e.setSpawnCancelled(true);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        ZONA.remove(e.getEntity().getUUID());
        AVISOS.remove(e.getEntity().getUUID());
    }

    // ------------------------------------------------------------------ levantar la fortaleza

    private static void encolar(ServerLevel lvl, Fortalezas.Sitio s) {
        FortalezaDatos d = FortalezaDatos.get(lvl.getServer());
        FortalezaDatos.Estado est = d.de(s);
        // el santuario de la de antes estaba en otro sitio: se vuelve a poner en el rellano de la nueva
        FortalezaPlan plan = Fortalezas.plan(lvl.getSeed(), s);
        if (rehacer(s, est, d, plan)) com.darkcraft.santuario.Santuarios.olvidar(lvl.getServer(), "f" + s.clave());
        if (est.puenteViejo != null && quitarPuenteViejo(lvl, est.puenteViejo, plan)) {
            est.puenteViejo = null;
            d.setDirty();
        }
        // (en pie con la isla y la fortaleza; las nubes de alrededor se siguen haciendo segun se cargan sus chunks)
        if (est.completa && est.hechos.size() >= plan.chunks().size() && est.porLimpiar.isEmpty()) return;
        java.util.Set<Long> todos = new java.util.LinkedHashSet<>(plan.chunks());
        todos.addAll(est.porLimpiar);
        for (long k : todos) {
            if (est.hechos.contains(k)) continue;
            String clave = s.clave() + ":" + k;
            if (EN_COLA.contains(clave)) continue;
            int cx = (int) (k >> 32), cz = (int) k;
            if (!lvl.getChunkSource().hasChunk(cx, cz)) continue;
            COLA.add(new long[]{s.x(), s.z(), cx, cz});
            EN_COLA.add(clave);
        }
    }

    /** Ticks que se deja descansar al servidor despues de un chunk que tardo mucho (para que no se quede atras). */
    private static int descanso = 0;

    /** Un chunk por tick: se construye su trozo de la fortaleza (y la puerta y el portal como esten). */
    private static void construirUno(MinecraftServer srv) {
        if (descanso > 0) {
            descanso--;
            return;
        }
        long[] t = COLA.poll();
        if (t == null) return;
        ServerLevel lvl = srv.overworld();
        int fx = (int) t[0], fz = (int) t[1], cx = (int) t[2], cz = (int) t[3];
        String clave = fx + "," + fz;
        long k = FortalezaPlan.clave(cx, cz);
        EN_COLA.remove(clave + ":" + k);
        if (!lvl.getChunkSource().hasChunk(cx, cz)) return;
        FortalezaDatos d = FortalezaDatos.get(srv);
        FortalezaDatos.Estado est = d.de(clave);
        if (est.hechos.contains(k)) return;
        FortalezaPlan plan = Fortalezas.plan(lvl.getSeed(), fx, fz);
        long t0 = System.nanoTime();
        if (est.porLimpiar.remove(k)) limpiar(lvl, cx, cz);
        if (!plan.toca(cx, cz)) {
            d.setDirty();
            return;
        }
        plan.construir(lvl, cx, cz, RandomSource.create(lvl.getSeed() ^ k));
        aplicarEstado(lvl, plan, est, cx, cz);
        pintarBioma(lvl, plan, cx, cz);
        est.hechos.add(k);
        if (!est.completa && est.hechos.containsAll(plan.nucleo())) {
            est.completa = true;
            DarkCraftMod.LOGGER.info("Fortaleza de Hueso en {} levantada ({} chunks de {})", clave, est.hechos.size(), plan.chunks().size());
        }
        d.setDirty();
        long ms = (System.nanoTime() - t0) / 1_000_000;
        if (ms > 120) DarkCraftMod.LOGGER.info("Fortaleza: chunk {},{} en {} ms", cx, cz, ms);
        // en un ordenador lento: como mucho la mitad del tiempo del servidor (un chunk de 150 ms, 3 ticks de descanso)
        descanso = (int) Math.min(40, ms / 50);
    }

    /**
     * Respuestas 3 y 4 del plan: una fortaleza de antes (la de las murallas cuadradas) se rehace de cero al acercarse
     * alguien: se vacian sus chunks y se levanta la nueva, con la puerta cerrada y sin portal (los logros se quedan). A
     * quien ya habia vencido a Ser Gavran se le devuelve la llave de hueso ({@link #devolverLlave}).
     */
    private static boolean rehacer(Fortalezas.Sitio s, FortalezaDatos.Estado est, FortalezaDatos d, FortalezaPlan plan) {
        if (est.version >= FortalezaDatos.VERSION) return false;
        if (est.version == 2) {
            // la de la parte 5a se queda; su escalera de caracol (si se levanto) se quita: ahora se sube por la montana
            if (est.puente && est.baseEscalera != Integer.MIN_VALUE) {
                int[] c = EscaleraVieja.centro(plan);
                est.puenteViejo = new int[]{c[0], c[1], est.baseEscalera, 1};
            }
            est.puente = false;
            est.baseEscalera = Integer.MIN_VALUE;
            est.version = 3;
            d.setDirty();
            DarkCraftMod.LOGGER.info("Fortaleza de Hueso en {}: pasa a la version 3 (se sube por la montana)", s.clave());
        }
        if (est.version == 3 || est.version == 4) {
            // parte 5c: la torre lleva trampas, la cripta bajo la fuente y la sala secreta: se vuelven a levantar los chunks
            // de la isla y la torre (la montana y las nubes no cambian); parte 5e: el portal nuevo de lo alto (de la 4 basta
            // con los de la terraza). La puerta, el puente, el muro falso y el portal se quedan como estaban (aplicarEstado
            // los vuelve a poner)
            boolean de3 = est.version == 3;
            int n = 0;
            for (long k : de3 ? plan.nucleo() : plan.terraza())
                if (est.hechos.remove(k)) {
                    est.porLimpiar.add(k);
                    n++;
                }
            est.completa = false;
            if (de3) est.muro = false;
            est.version = FortalezaDatos.VERSION;
            d.setDirty();
            DarkCraftMod.LOGGER.info("Fortaleza de Hueso en {}: pasa a la version {} ({}: {} chunks se vuelven a levantar)",
                    s.clave(), est.version, de3 ? "trampas, secretos y el portal nuevo" : "el portal nuevo", n);
            // (el santuario del rellano no se toca al rehacer solo la terraza)
            return de3 && n > 0;
        }
        // la escalera de hueso de la de antes, si se levanto: lo de debajo de la isla se quita aparte (quitarPuenteViejo)
        if (est.puente && est.baseEscalera != Integer.MIN_VALUE) {
            int[] c = plan.centroPuenteAntiguo();
            est.puenteViejo = new int[]{c[0], c[1], est.baseEscalera};
        }
        est.porLimpiar.addAll(est.hechos);
        est.hechos.clear();
        est.completa = false;
        est.rehecha = est.rehecha || !est.porLimpiar.isEmpty();
        est.puerta = est.puente = est.portal = false;
        est.baseEscalera = Integer.MIN_VALUE;
        est.parche = 0;
        est.version = FortalezaDatos.VERSION;
        d.setDirty();
        DarkCraftMod.LOGGER.info("Fortaleza de Hueso en {}: se rehace (la de antes ocupaba {} chunks)", s.clave(), est.porLimpiar.size());
        return true;
    }

    /**
     * El ascensor del tubo del centro de la torre (la placa de hueso): uno, cuando la fortaleza ya esta levantada y su
     * chunk procesa entidades. Sale abajo o arriba segun donde este quien llega.
     */
    private static void asegurarAscensor(ServerLevel lvl, Fortalezas.Sitio s, ServerPlayer p) {
        if (!FortalezaDatos.get(lvl.getServer()).de(s).completa) return;
        FortalezaPlan f = Fortalezas.plan(lvl.getSeed(), s);
        int[] ab = f.f.ascensorAbajo(), ar = f.f.ascensorArriba();
        if (!lvl.isPositionEntityTicking(new BlockPos(ab[0], ab[1], ab[2]))) return;
        net.minecraft.world.phys.AABB col = new net.minecraft.world.phys.AABB(ab[0] - 3, ab[1] - 3, ab[2] - 3, ab[0] + 4, ar[1] + 4, ab[2] + 4);
        List<com.darkcraft.arbol.Ascensor> as = lvl.getEntitiesOfClass(com.darkcraft.arbol.Ascensor.class, col);
        for (int i = 1; i < as.size(); i++) if (as.get(i).getPassengers().isEmpty()) as.get(i).discard();
        if (!as.isEmpty()) return;
        com.darkcraft.arbol.Ascensor a = DarkCraftMod.ASCENSOR.get().create(lvl);
        if (a == null) return;
        boolean arriba = p.getY() > (ab[1] + ar[1]) / 2.0;
        a.moveTo(ab[0] + 0.5, arriba ? ar[1] : ab[1], ab[2] + 0.5, 0f, 0f);
        a.setTipo(com.darkcraft.arbol.Ascensor.FORTALEZA);
        a.preparar(ab[1], ar[1], arriba);
        lvl.addFreshEntity(a);
    }

    /** Para las pruebas: el ascensor de la torre de esta fortaleza (o null). */
    public static com.darkcraft.arbol.Ascensor ascensor(ServerLevel lvl, FortalezaPlan f) {
        int[] ab = f.f.ascensorAbajo(), ar = f.f.ascensorArriba();
        net.minecraft.world.phys.AABB col = new net.minecraft.world.phys.AABB(ab[0] - 3, ab[1] - 3, ab[2] - 3, ab[0] + 4, ar[1] + 4, ab[2] + 4);
        List<com.darkcraft.arbol.Ascensor> as = lvl.getEntitiesOfClass(com.darkcraft.arbol.Ascensor.class, col);
        return as.isEmpty() ? null : as.get(0);
    }

    /** En una fortaleza rehecha, a quien ya tenia la llave (vencio a Ser Gavran) y no la lleva, se le da otra (una vez). */
    private static void devolverLlave(ServerPlayer p, Fortalezas.Sitio s) {
        FortalezaDatos d = FortalezaDatos.get(p.server);
        FortalezaDatos.Estado est = d.de(s);
        if (!est.rehecha || est.puerta || est.llaves.contains(p.getStringUUID()) || !Logros.tiene(p, "gavran")) return;
        for (net.minecraft.world.item.ItemStack it : p.getInventory().items) if (it.is(DarkCraftMod.LLAVE_HUESO.get())) return;
        net.minecraft.world.item.ItemStack llave = new net.minecraft.world.item.ItemStack(DarkCraftMod.LLAVE_HUESO.get());
        com.darkcraft.armas.LlaveHueso.apuntar(llave, s);
        com.darkcraft.rol.Equipo.dar(p, llave);
        est.llaves.add(p.getStringUUID());
        d.setDirty();
        p.sendSystemMessage(net.minecraft.network.chat.Component.translatable("darkcraft.fortaleza.llave_devuelta"));
    }

    /** Lo que es de la escalera de hueso (hueso, piedra negra, losas, nubes de lana, farolillos y calaveras). */
    private static boolean dePuente(BlockState b) {
        return b.is(Blocks.BONE_BLOCK) || b.is(Blocks.POLISHED_BLACKSTONE_BRICKS) || b.is(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS)
                || b.is(Blocks.CHISELED_POLISHED_BLACKSTONE) || b.is(Blocks.DEEPSLATE_TILES) || b.is(Blocks.BLACKSTONE)
                || b.is(Blocks.WHITE_WOOL) || b.is(Blocks.LIGHT_GRAY_WOOL) || b.is(Blocks.WHITE_STAINED_GLASS)
                || b.is(Blocks.SOUL_LANTERN) || b.is(Blocks.SKELETON_SKULL);
    }

    /**
     * Quita lo que quedaba de la escalera de hueso de la fortaleza de antes: lo que hay por debajo de lo que se vacia con
     * los chunks (de su base, con las patas, hasta Y - 56), solo sus bloques. Devuelve false si aun no estan cargados.
     */
    private static boolean quitarPuenteViejo(ServerLevel lvl, int[] pv, FortalezaPlan plan) {
        int Y = plan.Y, r = 12;
        for (int x = (pv[0] - r) >> 4; x <= (pv[0] + r) >> 4; x++)
            for (int z = (pv[1] - r) >> 4; z <= (pv[1] + r) >> 4; z++)
                if (!lvl.getChunkSource().hasChunk(x, z)) return false;
        if (pv.length > 3 && pv[3] == 1) {
            // la escalera de caracol de la parte 5a: pieza a pieza (alli esta ya la isla nueva)
            int n = EscaleraVieja.quitar(lvl, plan, pv[2]);
            DarkCraftMod.LOGGER.info("Fortaleza de Hueso: quitada la escalera de la parte 5a en {},{} ({} bloques)", pv[0], pv[1], n);
            return true;
        }
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        var aire = Blocks.AIR.defaultBlockState();
        int n = 0;
        for (int x = pv[0] - r; x <= pv[0] + r; x++)
            for (int z = pv[1] - r; z <= pv[1] + r; z++)
                for (int y = Math.max(lvl.getMinBuildHeight(), pv[2] - 15); y <= Y - 56; y++)
                    if (dePuente(lvl.getBlockState(m.set(x, y, z)))) {
                        lvl.setBlock(m, aire, 2 | 16);
                        n++;
                    }
        DarkCraftMod.LOGGER.info("Fortaleza de Hueso: quitada la escalera de la de antes en {},{} ({} bloques)", pv[0], pv[1], n);
        return true;
    }

    /** Vacia lo que quedaba de la fortaleza de antes en un chunk (de lo alto de las nubes para arriba). */
    private static void limpiar(ServerLevel lvl, int cx, int cz) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        var aire = Blocks.AIR.defaultBlockState();
        for (int x = cx << 4; x < (cx << 4) + 16; x++)
            for (int z = cz << 4; z < (cz << 4) + 16; z++)
                for (int y = Fortalezas.Y - 55; y <= Fortalezas.Y + 110; y++) {
                    m.set(x, y, z);
                    if (!lvl.getBlockState(m).isAir()) lvl.setBlock(m, aire, 2 | 16);
                }
    }

    /** Levanta ya todo lo que este cargado de la fortaleza (para los comandos y la prueba). */
    public static int construirYa(ServerLevel lvl, Fortalezas.Sitio s) {
        encolar(lvl, s);
        int n = 0;
        while (!COLA.isEmpty() && n < 400) {
            descanso = 0;
            construirUno(lvl.getServer());
            n++;
        }
        descanso = 0;
        return n;
    }

    /** La puerta, el puente, el muro falso y el portal como esten guardados, en un chunk recien construido. */
    private static void aplicarEstado(ServerLevel lvl, FortalezaPlan f, FortalezaDatos.Estado est, int cx, int cz) {
        if (est.puente) Puente.enChunk(lvl, f, cx, cz);
        if (est.puerta) {
            int[] r = f.f.reja();
            if ((r[2] >> 4) == cz)
                for (int x = r[0]; x <= r[3]; x++) {
                    if ((x >> 4) != cx) continue;
                    for (int y = r[1]; y <= r[4]; y++) lvl.setBlock(new BlockPos(x, y, r[2]), Blocks.AIR.defaultBlockState(), 2);
                    for (int y = r[4] + 1; y <= r[4] + TorreCupula.PALTO; y++) lvl.setBlock(new BlockPos(x, y, r[2]), reja(), 2);
                }
        }
        if (est.muro)
            for (int[] q : f.f.muroFalso())
                if ((q[0] >> 4) == cx && (q[2] >> 4) == cz) lvl.setBlock(new BlockPos(q[0], q[1], q[2]), Blocks.AIR.defaultBlockState(), 2);
        if (est.portal) {
            int[] q = f.f.portal();
            if ((q[2] >> 4) == cz)
                for (int x = q[0]; x <= q[3]; x++)
                    for (int y = q[1]; y <= q[4]; y++)
                        if ((x >> 4) == cx && f.f.enPortal(x, y)) lvl.setBlock(new BlockPos(x, y, q[2]), DarkCraftMod.PORTAL_SOMBRA.get().defaultBlockState(), 2);
            // las gemas encendidas, las cadenas rotas, los pilares sin su capitel y los cascotes (el remolino y el amuleto
            // los pone asegurarPortal)
            for (TorreCupula.Cambio c : f.f.alAbrir())
                if ((c.x() >> 4) == cx && (c.z() >> 4) == cz) lvl.setBlock(new BlockPos(c.x(), c.y(), c.z()), c.despues(), 2);
        }
    }

    static BlockState reja() {
        return DarkCraftMod.REJA_HUESO.get().defaultBlockState();
    }

    /** El bioma de la fortaleza (lluvia, cielo gris) alrededor de la isla, como el comando /fillbiome. */
    private static void pintarBioma(ServerLevel lvl, FortalezaPlan f, int cx, int cz) {
        Holder<Biome> b = lvl.registryAccess().registryOrThrow(Registries.BIOME).getHolder(BIOMA).map(h -> (Holder<Biome>) h).orElse(null);
        if (b == null) return;
        LevelChunk ch = lvl.getChunk(cx, cz);
        long r2 = 128L * 128L;
        boolean alguno = false;
        for (int qx = 0; qx < 4 && !alguno; qx++)
            for (int qz = 0; qz < 4 && !alguno; qz++) {
                long dx = (cx << 4) + qx * 4 + 2 - f.cx, dz = (cz << 4) + qz * 4 + 2 - f.cz;
                if (dx * dx + dz * dz <= r2) alguno = true;
            }
        if (!alguno) return;
        try {
            ch.fillBiomesFromNoise((x, y, z, sampler) -> {
                long dx = QuartPos.toBlock(x) + 2 - f.cx, dz = QuartPos.toBlock(z) + 2 - f.cz;
                if (dx * dx + dz * dz <= r2) return b;
                return ch.getNoiseBiome(x, y, z);
            }, null);
            ch.setUnsaved(true);
            lvl.getChunkSource().chunkMap.resendBiomesForChunks(List.of(ch));
        } catch (Throwable t) {
            DarkCraftMod.LOGGER.warn("Fortaleza: no se pudo poner su bioma en {},{}: {}", cx, cz, t.toString());
        }
    }

    // ------------------------------------------------------------------ la zona: lluvia y musica

    private static void entrar(ServerPlayer p, Fortalezas.Sitio s) {
        boolean nuevo = !s.equals(ZONA.put(p.getUUID(), s));
        if (nuevo) Net.to(p, new Net.Fortaleza(true));
        if (Cinematics.active(p)) return;
        // solo lluvia (nada de tormenta) y el dia y la noche de siempre
        if (nuevo || p.tickCount % 40 == 13) p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0));
        p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 1.0F));
        p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, 0.0F));
    }

    private static void salir(ServerPlayer p) {
        if (ZONA.remove(p.getUUID()) == null) return;
        Net.to(p, new Net.Fortaleza(false));
        ServerLevel lvl = p.serverLevel();
        if (lvl.isRaining()) {
            p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0));
            p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, lvl.getRainLevel(1.0F)));
            p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, lvl.getThunderLevel(1.0F)));
        } else {
            p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0));
            p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 0.0F));
            p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, 0.0F));
        }
    }

    /** El nombre del sitio de la fortaleza donde esta el jugador (para el Espejo), o null si no esta en ninguna. */
    public static String lugar(ServerPlayer p) {
        Fortalezas.Sitio s = ZONA.get(p.getUUID());
        if (s == null) return null;
        FortalezaPlan f = Fortalezas.plan(p.serverLevel().getSeed(), s);
        double dx = p.getX() - f.cx, dz = p.getZ() - f.cz, y = p.getY();
        if (Puente.enPuente(f, p.getX(), y, p.getZ())) return "darkcraft.mapa.lugar.fortaleza_puente";
        com.darkcraft.gen.Montana m = f.montana;
        if (m.distancia(p.getX(), p.getZ()) < 6 && y > com.darkcraft.gen.Montana.FONDO) {
            if (Math.hypot(p.getX() - m.mx - 0.5, p.getZ() - m.mz - 0.5) <= com.darkcraft.gen.Montana.RP + 2 && y > m.cima - 4)
                return "darkcraft.mapa.lugar.fortaleza_mirador";
            return "darkcraft.mapa.lugar.fortaleza_montana";
        }
        if (y >= f.Y + TorreCupula.TERRAZA - 1 && Math.max(Math.abs(dx), Math.abs(dz)) <= TorreCupula.TL + 1)
            return "darkcraft.mapa.lugar.fortaleza_portal";
        if ((TorreCupula.octo(dx, dz) <= TorreCupula.AT + 1 || Math.hypot(dx - TorreCupula.TX, dz) <= TorreCupula.RTU + 1) && y > f.Y + TorreCupula.SOTANO - 2)
            return "darkcraft.mapa.lugar.fortaleza_homenaje";
        if (Math.abs(dx) <= 16 && dz > TorreCupula.RM - 5 && dz < TorreCupula.PFUERA + 9 && y > f.Y - 2 && y < f.Y + 2 * TorreCupula.PALTO + 6)
            return "darkcraft.mapa.lugar.fortaleza_puerta";
        if (Math.hypot(dx, dz) < TorreCupula.RD && y > f.Y - 2 && y < f.Y + TorreCupula.RD)
            return "darkcraft.mapa.lugar.fortaleza_cupula";
        return "darkcraft.mapa.lugar.fortaleza";
    }

    // ------------------------------------------------------------------ el puente (desde la llave)

    /** Tiende el puente largo desde el mirador de la montana de la fortaleza s (si no estaba). Devuelve false si ya estaba. */
    public static boolean levantarPuente(ServerPlayer p, Fortalezas.Sitio s) {
        ServerLevel lvl = p.serverLevel();
        FortalezaDatos d = FortalezaDatos.get(p.server);
        FortalezaDatos.Estado est = d.de(s);
        if (est.puente) return false;
        FortalezaPlan f = Fortalezas.plan(lvl.getSeed(), s);
        est.puente = true;
        d.setDirty();
        Puente.levantar(lvl, f);
        Logros.dar(p, "puente");
        int[] c = Puente.arco(f);
        for (ServerPlayer q : lvl.players())
            if (q.distanceToSqr(c[0], q.getY(), c[2]) < 80 * 80) Logros.dar(q, "puente");
        lvl.playSound(null, c[0] + 0.5, c[1] + 2, c[2] + 0.5, DarkCraftMod.SFX_PUENTE_HUESO.get(), SoundSource.BLOCKS, 3.0f, 0.8f);
        lvl.sendParticles(ParticleTypes.SOUL, c[0] + 0.5, c[1] + 6, c[2] + 0.5, 40, 2.5, 4, 0.6, 0.03);
        return true;
    }

    // ------------------------------------------------------------------ la puerta (desde la llave)

    /** Abre la puerta de la fortaleza s con su cinematica. Devuelve false si ya estaba abierta. */
    public static boolean abrirPuerta(ServerPlayer p, Fortalezas.Sitio s) {
        ServerLevel lvl = p.serverLevel();
        MinecraftServer srv = p.server;
        FortalezaDatos d = FortalezaDatos.get(srv);
        FortalezaDatos.Estado est = d.de(s);
        if (est.puerta) return false;
        est.puerta = true;
        d.setDirty();
        FortalezaPlan f = Fortalezas.plan(lvl.getSeed(), s);
        List<Shot> planos = CineShots.puertaHueso(f);
        float total = planos.get(planos.size() - 1).t1;
        for (ServerPlayer q : lvl.players()) {
            if (q != p && (q.distanceToSqr(p) > 48 * 48 || Cinematics.active(q))) continue;
            Cinematics.playShots(q, Cinematics.PUERTA_HUESO, planos, total);
            Logros.dar(q, "puerta_hueso");
        }
        for (int k = 0; k < 18; k++) despues(srv, 16 + k * 4, () -> ojos(lvl, f));
        int[] r = f.f.reja();
        despues(srv, 86, () -> lvl.playSound(null, f.cx + 0.5, r[1] + 3, r[2] + 0.5, DarkCraftMod.SFX_PUERTA_HUESO.get(), SoundSource.BLOCKS, 3.0f, 1.0f));
        for (int k = 1; k <= TorreCupula.PALTO; k++) {
            final int paso = k;
            despues(srv, 92 + (k - 1) * 10, () -> subirReja(lvl, f, paso));
        }
        return true;
    }

    private static void ojos(ServerLevel lvl, FortalezaPlan f) {
        for (int[] o : f.f.ojos()) {
            lvl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, o[0] + 0.5, o[1] + 0.5, o[2] + 1.05, 3, 0.15, 0.15, 0.05, 0.01);
            lvl.sendParticles(ParticleTypes.SMOKE, o[0] + 0.5, o[1] + 0.6, o[2] + 1.1, 1, 0.1, 0.1, 0.05, 0.01);
        }
    }

    /** Sube la reja un bloque (paso 1..PALTO): fila nueva arriba, fuera la de abajo. */
    public static void subirReja(ServerLevel lvl, FortalezaPlan f, int paso) {
        int[] r = f.f.reja();
        for (int x = r[0]; x <= r[3]; x++) {
            lvl.setBlock(new BlockPos(x, r[4] + paso, r[2]), reja(), 3);
            lvl.setBlock(new BlockPos(x, r[1] + paso - 1, r[2]), Blocks.AIR.defaultBlockState(), 3);
        }
        lvl.sendParticles(ParticleTypes.WHITE_ASH, f.cx + 0.5, r[1] + paso, r[2] + 0.5, 20, 3.0, 0.4, 0.3, 0.02);
        lvl.playSound(null, f.cx + 0.5, r[1] + paso, r[2] + 0.5, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 1.4f, 0.55f);
        lvl.playSound(null, f.cx + 0.5, r[1] + paso, r[2] + 0.5, SoundEvents.BONE_BLOCK_HIT, SoundSource.BLOCKS, 1.2f, 0.6f);
    }

    /** Cierra la puerta otra vez (comando reiniciar). */
    public static void cerrarPuerta(ServerLevel lvl, FortalezaPlan f) {
        int[] r = f.f.reja();
        for (int x = r[0]; x <= r[3]; x++) {
            for (int y = r[1]; y <= r[4]; y++) lvl.setBlock(new BlockPos(x, y, r[2]), reja(), 3);
            for (int y = r[4] + 1; y <= r[4] + TorreCupula.PALTO; y++) lvl.setBlock(new BlockPos(x, y, r[2]), Blocks.AIR.defaultBlockState(), 3);
        }
    }

    // ------------------------------------------------------------------ el portal y el capitulo 6

    private static final Map<UUID, Integer> EMPUJES = new HashMap<>();

    /** Alguien toca el portal: todavia no se puede cruzar (sale despedido hacia atras). */
    public static void tocarPortal(Level lvl, BlockPos pos, Entity e) {
        double dir = e.getZ() >= pos.getZ() + 0.5 ? 1 : -1;
        e.setDeltaMovement(0, 0.42, dir * 0.95);
        e.hurtMarked = true;
        if (e instanceof ServerPlayer p) {
            Integer t = EMPUJES.get(p.getUUID());
            if (t != null && p.tickCount - t < 30 && p.tickCount >= t) return;
            EMPUJES.put(p.getUUID(), p.tickCount);
            p.displayClientMessage(Component.translatable("darkcraft.portal.aun_no").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
            lvl.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_DEPLETE.get(), SoundSource.BLOCKS, 0.8f, 0.6f);
            ((ServerLevel) lvl).sendParticles(ParticleTypes.SOUL, e.getX(), e.getY() + 1, e.getZ(), 10, 0.3, 0.5, 0.3, 0.05);
        }
    }

    /** El amuleto en el altar. Devuelve true si se ha gastado. */
    public static boolean usarAmuleto(ServerPlayer p, ItemStack st) {
        Fortalezas.Sitio s = ZONA.get(p.getUUID());
        if (s == null) {
            p.displayClientMessage(Component.translatable("darkcraft.amuleto.lejos").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            return false;
        }
        FortalezaPlan f = Fortalezas.plan(p.serverLevel().getSeed(), s);
        int[] al = f.f.altar();
        if (p.distanceToSqr(al[0] + 0.5, al[1] + 0.5, al[2] + 0.5) > 7 * 7) {
            p.displayClientMessage(Component.translatable("darkcraft.amuleto.altar").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            return false;
        }
        FortalezaDatos d = FortalezaDatos.get(p.server);
        FortalezaDatos.Estado est = d.de(s);
        if (est.portal) {
            if (!Story.seenCine(p, 6)) capitulo6(p, f, false);
            else p.displayClientMessage(Component.translatable("darkcraft.portal.ya").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            return false;
        }
        est.portal = true;
        d.setDirty();
        st.shrink(1);
        Logros.dar(p, "portal");
        capitulo6(p, f, true);
        return true;
    }

    /** El capitulo 6: la cinematica narrada en lo alto de la fortaleza; si abre, el portal se abre a mitad. */
    public static void capitulo6(ServerPlayer p, FortalezaPlan f, boolean abre) {
        ServerLevel lvl = p.serverLevel();
        despejarAzotea(lvl, f);
        Cinematics.playCapitulo6(p, f);
        if (!abre) return;
        int t = Math.round((Timeline.cut(6, 2) + 1.2f) * 20);
        despues(p.server, t, () -> abrirPortal(lvl, f));
        int[] al = f.f.altar();
        // el amuleto se deshace en el altar
        lvl.sendParticles(ParticleTypes.END_ROD, al[0] + 0.5, al[1] + 1.6, al[2] + 0.5, 30, 0.3, 0.4, 0.3, 0.08);
        lvl.playSound(null, al[0] + 0.5, al[1] + 1, al[2] + 0.5, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.5f, 0.5f);
    }

    /** Fuera los monstruos (y los caballeros) de lo alto de la torre del homenaje: que no salgan en la cinematica. */
    static void despejarAzotea(ServerLevel lvl, FortalezaPlan f) {
        int top = f.Y + TorreCupula.TERRAZA;
        net.minecraft.world.phys.AABB caja = new net.minecraft.world.phys.AABB(f.cx - 30, top - 3, f.cz - 50, f.cx + 30, top + 30, f.cz + 12);
        for (net.minecraft.world.entity.Mob m : lvl.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, caja,
                m -> m instanceof net.minecraft.world.entity.monster.Enemy && !m.hasCustomName()
                        && (m instanceof com.darkcraft.caballeros.CaballeroZombi || !m.isPersistenceRequired()))) {
            lvl.sendParticles(ParticleTypes.SOUL, m.getX(), m.getY() + 1, m.getZ(), 8, 0.3, 0.5, 0.3, 0.02);
            m.discard();
        }
    }

    /**
     * El portal se abre (respuestas 52, 53 y 55): el remolino crece desde el centro, se encienden las gemas del marco de
     * abajo arriba y los ojos de las estatuas, saltan las cadenas y revientan lo alto de los pilares.
     */
    public static void abrirPortal(ServerLevel lvl, FortalezaPlan f) {
        int[] q = f.f.portal();
        double mx = (q[0] + q[3]) / 2.0, my = (q[1] + q[4]) / 2.0;
        List<BlockPos> celdas = new ArrayList<>();
        for (int x = q[0]; x <= q[3]; x++) for (int y = q[1]; y <= q[4]; y++) if (f.f.enPortal(x, y)) celdas.add(new BlockPos(x, y, q[2]));
        celdas.sort((a, b) -> Double.compare(Math.hypot(a.getX() - mx, (a.getY() - my) * 0.6), Math.hypot(b.getX() - mx, (b.getY() - my) * 0.6)));
        lvl.playSound(null, mx + 0.5, my, q[2] + 0.5, DarkCraftMod.SFX_PORTAL_ABRE.get(), SoundSource.BLOCKS, 4.0f, 1.0f);
        quitarRemolinos(lvl, f);
        ponerRemolino(lvl, f, 0f);
        // (el bloque del portal no se ve: pone el roce y las chispas; el remolino es el dibujo)
        for (int i = 0; i < celdas.size(); i++) {
            BlockPos b = celdas.get(i);
            despues(lvl.getServer(), i / 3, () -> {
                lvl.setBlock(b, DarkCraftMod.PORTAL_SOMBRA.get().defaultBlockState(), 3);
                lvl.sendParticles(ParticleTypes.REVERSE_PORTAL, b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.1);
            });
        }
        int k = 0;
        for (TorreCupula.Cambio c : f.f.alAbrir()) {
            BlockPos b = new BlockPos(c.x(), c.y(), c.z());
            switch (c.fase()) {
                case TorreCupula.GEMAS_MARCO -> despues(lvl.getServer(), 6 + (c.y() - q[1]) * 3, () -> {
                    lvl.setBlock(b, c.despues(), 3);
                    lvl.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(1f, 0.1f, 0.05f), 1.6f),
                            b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5, 10, 0.4, 0.4, 0.4, 0.02);
                    lvl.playSound(null, b, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.6f, 0.6f + (c.y() - q[1]) * 0.04f);
                });
                case TorreCupula.OJOS -> despues(lvl.getServer(), 52, () -> {
                    lvl.setBlock(b, c.despues(), 3);
                    lvl.sendParticles(ParticleTypes.FLAME, b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.01);
                    lvl.playSound(null, b, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2f, 0.5f);
                });
                case TorreCupula.CADENA -> {
                    int t = 62 + (k++ % 6) * 2;
                    despues(lvl.getServer(), t, () -> {
                        BlockState era = lvl.getBlockState(b);
                        lvl.setBlock(b, c.despues(), 3);
                        lvl.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, era.isAir() ? c.antes() : era),
                                b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5, 14, 0.3, 0.2, 0.3, 0.1);
                        lvl.playSound(null, b, SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 2.0f, 0.6f);
                    });
                }
                case TorreCupula.PILARES -> despues(lvl.getServer(), 76, () -> {
                    BlockState era = lvl.getBlockState(b);
                    lvl.setBlock(b, c.despues(), 3);
                    lvl.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, era.isAir() ? c.antes() : era),
                            b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5, 20, 0.5, 0.5, 0.5, 0.15);
                    if (c.antes().getBlock() == DarkCraftMod.GEMA_ROJA.get()) {
                        lvl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5, 1, 0, 0, 0, 0);
                        lvl.playSound(null, b, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 2.5f, 0.7f);
                        lvl.playSound(null, b, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 2.0f, 0.5f);
                    }
                });
                case TorreCupula.CASCOTES -> despues(lvl.getServer(), 84, () -> {
                    lvl.setBlock(b, c.despues(), 3);
                    lvl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, b.getX() + 0.5, b.getY() + 0.6, b.getZ() + 0.5, 3, 0.3, 0.1, 0.3, 0.01);
                    lvl.playSound(null, b, SoundEvents.DEEPSLATE_BRICKS_FALL, SoundSource.BLOCKS, 1.5f, 0.7f);
                });
                default -> {
                }
            }
        }
        // el amuleto se queda en su pedestal, flotando
        despues(lvl.getServer(), 10, () -> ponerAmuleto(lvl, f));
    }

    /** Para el comando de reiniciar: el portal cerrado y todo lo suyo como antes de abrirse. */
    public static void cerrarPortal(ServerLevel lvl, FortalezaPlan f) {
        int[] q = f.f.portal();
        for (int x = q[0]; x <= q[3]; x++)
            for (int y = q[1]; y <= q[4]; y++)
                if (f.f.enPortal(x, y)) lvl.setBlock(new BlockPos(x, y, q[2]), Blocks.AIR.defaultBlockState(), 3);
        for (TorreCupula.Cambio c : f.f.alAbrir()) lvl.setBlock(new BlockPos(c.x(), c.y(), c.z()), c.antes(), 3);
        quitarRemolinos(lvl, f);
        int[] al = f.f.altar();
        for (Display.ItemDisplay d : lvl.getEntitiesOfClass(Display.ItemDisplay.class, cajaAltar(al), d -> d.getTags().contains(AMULETO_ALTAR))) d.discard();
    }

    // ------------------------------------------------------------------ el remolino y el amuleto del portal abierto

    /** La etiqueta del amuleto que flota en su pedestal (respuesta 56). */
    public static final String AMULETO_ALTAR = "darkcraft_amuleto_altar";
    /** Los amuletos que hay en sus pedestales (se giran desde onServerTick). */
    private static final List<Display.ItemDisplay> AMULETOS = new ArrayList<>();

    private static net.minecraft.world.phys.AABB cajaRemolino(FortalezaPlan f) {
        int[] q = f.f.portal();
        return new net.minecraft.world.phys.AABB(f.cx - 2, q[1] - 2, q[2] - 2, f.cx + 3, q[1] + 3, q[2] + 3);
    }

    private static net.minecraft.world.phys.AABB cajaAltar(int[] al) {
        return new net.minecraft.world.phys.AABB(al[0] - 1, al[1], al[2] - 1, al[0] + 2, al[1] + 5, al[2] + 2);
    }

    private static void quitarRemolinos(ServerLevel lvl, FortalezaPlan f) {
        for (Remolino r : lvl.getEntitiesOfClass(Remolino.class, cajaRemolino(f))) r.discard();
    }

    /** El remolino del portal (abierto de 0 a 1: con 0 crece desde el centro). */
    static Remolino ponerRemolino(ServerLevel lvl, FortalezaPlan f, float abierto) {
        Remolino r = DarkCraftMod.REMOLINO.get().create(lvl);
        if (r == null) return null;
        int[] q = f.f.portal();
        r.moveTo(f.cx + 0.5, q[1], q[2] + 0.5, 0f, 0f);
        r.abierto(abierto);
        r.abiertoO = abierto;
        lvl.addFreshEntity(r);
        return r;
    }

    /** El amuleto en el hueco de su pedestal: flota, gira y brilla. */
    static void ponerAmuleto(ServerLevel lvl, FortalezaPlan f) {
        int[] al = f.f.altar();
        for (Display.ItemDisplay d : lvl.getEntitiesOfClass(Display.ItemDisplay.class, cajaAltar(al), d -> d.getTags().contains(AMULETO_ALTAR))) d.discard();
        Display.ItemDisplay d = EntityType.ITEM_DISPLAY.create(lvl);
        if (d == null) return;
        CompoundTag tag = new CompoundTag();
        tag.put("item", new ItemStack(DarkCraftMod.AMULETO_HUESO.get()).save(new CompoundTag()));
        tag.putString("item_display", "fixed");
        tag.put("transformation", matriz(new Matrix4f().scale(0.8f)));
        CompoundTag luz = new CompoundTag();
        luz.putInt("sky", 15);
        luz.putInt("block", 15);
        tag.put("brightness", luz);
        d.load(tag);
        d.moveTo(al[0] + 0.5, al[1] + 2.6, al[2] + 0.5, 0f, 0f);
        d.addTag(AMULETO_ALTAR);
        com.darkcraft.armas.EfectosRaiz.efimera(d);
        lvl.addFreshEntity(d);
        AMULETOS.add(d);
        lvl.sendParticles(ParticleTypes.END_ROD, al[0] + 0.5, al[1] + 2.6, al[2] + 0.5, 16, 0.2, 0.2, 0.2, 0.05);
    }

    /** Con el portal abierto y su chunk en marcha: un remolino en el arco y el amuleto en su pedestal. */
    private static void asegurarPortal(ServerLevel lvl, Fortalezas.Sitio s) {
        FortalezaDatos.Estado est = FortalezaDatos.get(lvl.getServer()).de(s);
        if (!est.portal || !est.completa) return;
        FortalezaPlan f = Fortalezas.plan(lvl.getSeed(), s);
        int[] q = f.f.portal(), al = f.f.altar();
        if (!lvl.isPositionEntityTicking(new BlockPos(f.cx, q[1], q[2])) || !lvl.isPositionEntityTicking(new BlockPos(al[0], al[1], al[2]))) return;
        List<Remolino> rs = lvl.getEntitiesOfClass(Remolino.class, cajaRemolino(f));
        for (int i = 1; i < rs.size(); i++) rs.get(i).discard();
        if (rs.isEmpty()) ponerRemolino(lvl, f, 1f);
        List<Display.ItemDisplay> ds = lvl.getEntitiesOfClass(Display.ItemDisplay.class, cajaAltar(al), d -> d.getTags().contains(AMULETO_ALTAR));
        for (int i = 1; i < ds.size(); i++) ds.get(i).discard();
        if (ds.isEmpty()) ponerAmuleto(lvl, f);
        else if (!AMULETOS.contains(ds.get(0))) AMULETOS.add(ds.get(0));
        if (lvl.random.nextInt(2) == 0)
            lvl.sendParticles(ParticleTypes.ENCHANT, al[0] + 0.5, al[1] + 2.9, al[2] + 0.5, 6, 0.4, 0.4, 0.4, 0.4);
    }

    /** Para las pruebas y el comando: como esta el portal (lo que cambia al abrirse, el remolino y el amuleto). */
    public static String resumenPortal(ServerLevel lvl, FortalezaPlan f) {
        int[] n = new int[5], hechos = new int[5];
        for (TorreCupula.Cambio c : f.f.alAbrir()) {
            n[c.fase()]++;
            if (lvl.getBlockState(new BlockPos(c.x(), c.y(), c.z())).equals(c.despues())) hechos[c.fase()]++;
        }
        int[] q = f.f.portal();
        int bloques = 0, celdas = 0;
        for (int x = q[0]; x <= q[3]; x++)
            for (int y = q[1]; y <= q[4]; y++)
                if (f.f.enPortal(x, y)) {
                    celdas++;
                    if (lvl.getBlockState(new BlockPos(x, y, q[2])).is(DarkCraftMod.PORTAL_SOMBRA.get())) bloques++;
                }
        List<Remolino> rs = lvl.getEntitiesOfClass(Remolino.class, cajaRemolino(f));
        int[] al = f.f.altar();
        int am = lvl.getEntitiesOfClass(Display.ItemDisplay.class, cajaAltar(al), d -> d.getTags().contains(AMULETO_ALTAR)).size();
        return String.format(java.util.Locale.ROOT, "hueco %d/%d; gemas del marco %d/%d, ojos %d/%d, cadenas rotas %d/%d, pilares %d/%d, cascotes %d/%d;"
                        + " remolino %d%s; amuleto en el pedestal %d", bloques, celdas, hechos[0], n[0], hechos[1], n[1], hechos[2], n[2], hechos[3], n[3],
                hechos[4], n[4], rs.size(), rs.isEmpty() ? "" : String.format(java.util.Locale.ROOT, " (abierto %.2f)", rs.get(0).abierto()), am);
    }

    /** Cada medio segundo: un octavo de vuelta (y un poco arriba o abajo) a los amuletos de los pedestales. */
    private static void girarAmuletos(int tick) {
        if (AMULETOS.isEmpty() || tick % 10 != 0) return;
        AMULETOS.removeIf(d -> d.isRemoved());
        int paso = tick / 10;
        for (Display.ItemDisplay d : AMULETOS) {
            CompoundTag t = d.saveWithoutId(new CompoundTag());
            t.put("transformation", matriz(new Matrix4f().translate(0, 0.09f * (float) Math.sin(paso * Math.PI / 4), 0)
                    .rotateY((float) (paso * Math.PI / 4)).scale(0.8f)));
            t.putInt("interpolation_duration", 10);
            t.putInt("start_interpolation", 0);
            d.load(t);
        }
    }

    /** Al acabar el capitulo 6: el libro del capitulo para quien no lo tenga. */
    public static void finCapitulo6(ServerPlayer p) {
        Logros.dar(p, Logros.capitulo(6));
        boolean leido = (Story.read(p) & (1 << 6)) != 0;
        Story.markRead(p, 6);
        com.darkcraft.espejo.Espejo.leido(p, 6);
        if (leido) return;
        for (ItemStack st : p.getInventory().items)
            if (st.hasTag() && st.getTag().getInt("darkcraft_libro") == 6) return;
        ItemStack libro = Story.book(6);
        if (!p.getInventory().add(libro)) p.drop(libro, false);
        p.displayClientMessage(Component.translatable("darkcraft.cap6.libro").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC), true);
    }

    /** Clic derecho en el altar sin amuleto (portal ya abierto y sin ver el capitulo 6): se ve la cinematica. */
    @SubscribeEvent
    public static void onClicAltar(PlayerInteractEvent.RightClickBlock e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || e.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;
        if (p.getMainHandItem().is(DarkCraftMod.AMULETO_HUESO.get())) return;
        Fortalezas.Sitio s = ZONA.get(p.getUUID());
        if (s == null) return;
        FortalezaPlan f = Fortalezas.plan(p.serverLevel().getSeed(), s);
        int[] al = f.f.altar();
        BlockPos b = e.getPos();
        if (b.getX() != al[0] || b.getZ() != al[2] || Math.abs(b.getY() - al[1]) > 1) return;
        if (!FortalezaDatos.get(p.server).de(s).portal || Story.seenCine(p, 6) || Cinematics.active(p)) return;
        capitulo6(p, f, false);
        e.setCanceled(true);
    }

    // ------------------------------------------------------------------ libros perdidos en atriles

    @SubscribeEvent
    public static void onAbrir(PlayerContainerEvent.Open e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || !(e.getContainer() instanceof LecternMenu lm)) return;
        int n = Perdidos.de(lm.getBook());
        if (n > 0) Perdidos.encontrado(p, n);
    }

    // ------------------------------------------------------------------ la forja del amuleto

    @SubscribeEvent
    public static void onForja(PlayerEvent.ItemCraftedEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || !e.getCrafting().is(DarkCraftMod.AMULETO_HUESO.get())) return;
        Logros.dar(p, "amuleto");
        ServerLevel lvl = p.serverLevel();
        BlockPos mesa = mesa(p);
        Vec3 c = mesa != null ? Vec3.atCenterOf(mesa) : p.getEyePosition().add(p.getLookAngle().multiply(1, 0, 1).normalize().scale(1.6)).add(0, -1.2, 0);
        List<Shot> planos = CineShots.amuleto(c, p.getYRot());
        float total = planos.get(planos.size() - 1).t1;
        Cinematics.playShots(p, Cinematics.AMULETO, planos, total);
        // el amuleto flota sobre la mesa, gira y brilla (el de verdad ya esta en el inventario)
        Display.ItemDisplay d = EntityType.ITEM_DISPLAY.create(lvl);
        if (d != null) {
            CompoundTag tag = new CompoundTag();
            tag.put("item", new ItemStack(DarkCraftMod.AMULETO_HUESO.get()).save(new CompoundTag()));
            tag.putString("item_display", "fixed");
            tag.put("transformation", matriz(new Matrix4f().scale(0.9f)));
            CompoundTag luz = new CompoundTag();
            luz.putInt("sky", 15);
            luz.putInt("block", 15);
            tag.put("brightness", luz);
            d.load(tag);
            d.moveTo(c.x, c.y + 0.75, c.z, 0f, 0f);
            com.darkcraft.armas.EfectosRaiz.efimera(d);
            lvl.addFreshEntity(d);
            int pasos = Math.round(total * 20 / 5f);
            for (int k = 1; k <= pasos; k++) {
                final int i = k;
                despues(p.server, k * 5, () -> girar(lvl, d, c, i, pasos));
            }
        }
        lvl.playSound(null, c.x, c.y, c.z, DarkCraftMod.SFX_AMULETO_FORJA.get(), SoundSource.PLAYERS, 2.0f, 1.0f);
    }

    private static void girar(ServerLevel lvl, Display.ItemDisplay d, Vec3 c, int i, int pasos) {
        if (!d.isAlive()) return;
        if (i >= pasos) {
            lvl.sendParticles(ParticleTypes.END_ROD, d.getX(), d.getY(), d.getZ(), 24, 0.2, 0.2, 0.2, 0.08);
            lvl.sendParticles(ParticleTypes.FLASH, d.getX(), d.getY(), d.getZ(), 1, 0, 0, 0, 0);
            d.discard();
            return;
        }
        float u = i / (float) pasos;
        CompoundTag t = d.saveWithoutId(new CompoundTag());
        t.put("transformation", matriz(new Matrix4f().translate(0, 0.55f * (float) Math.sin(u * Math.PI * 0.5), 0)
                .rotateY((float) (i * Math.PI / 4)).scale(0.9f + 0.25f * u)));
        t.putInt("interpolation_duration", 5);
        t.putInt("start_interpolation", 0);
        d.load(t);
        double a = i * 0.7;
        for (int k = 0; k < 3; k++) {
            double b = a + k * Math.PI * 2 / 3;
            lvl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, c.x + Math.cos(b) * 0.9, c.y + 0.6 + u * 0.8, c.z + Math.sin(b) * 0.9, 1, 0, 0.02, 0, 0.01);
        }
        // chispas pequenas (las de la vara del End, tan cerca de la camara, tapaban el amuleto)
        if (i % 2 == 0) lvl.sendParticles(ParticleTypes.WAX_OFF, d.getX(), d.getY() + 0.5, d.getZ(), 2, 0.25, 0.2, 0.25, 0.4);
        if (i % 6 == 0) lvl.sendParticles(ParticleTypes.ENCHANT, c.x, c.y + 1.2, c.z, 12, 0.5, 0.5, 0.5, 0.6);
    }

    private static BlockPos mesa(ServerPlayer p) {
        BlockPos o = p.blockPosition();
        BlockPos mejor = null;
        double md = Double.MAX_VALUE;
        for (BlockPos b : BlockPos.betweenClosed(o.offset(-5, -2, -5), o.offset(5, 3, 5))) {
            if (!p.serverLevel().getBlockState(b).is(Blocks.CRAFTING_TABLE)) continue;
            double d = b.distSqr(o);
            if (d < md) {
                md = d;
                mejor = b.immutable();
            }
        }
        return mejor;
    }

    private static ListTag matriz(Matrix4f m) {
        ListTag l = new ListTag();
        for (int i = 0; i < 16; i++) l.add(FloatTag.valueOf(m.getRowColumn(i >> 2, i & 3)));
        return l;
    }

    // ------------------------------------------------------------------ el libro del acertijo

    /** Quien lee el libro del acertijo: logro y la receta del amuleto en su libro de recetas. */
    public static void leerAcertijo(ServerPlayer p) {
        Logros.dar(p, "acertijo");
        var r = p.server.getRecipeManager().byKey(new ResourceLocation(DarkCraftMod.MODID, "amuleto_hueso"));
        r.ifPresent(x -> p.awardRecipes(List.of(x)));
    }
}
