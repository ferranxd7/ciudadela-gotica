package com.darkcraft.santuario;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.Combos;
import com.darkcraft.fortaleza.FortalezaDatos;
import com.darkcraft.fortaleza.Fortalezas;
import com.darkcraft.gen.CastlePlan;
import com.darkcraft.gen.TorreCupula;
import com.darkcraft.gen.MapShapes;
import com.darkcraft.net.Net;
import com.darkcraft.rol.Frascos;
import com.darkcraft.rol.Rol;
import com.darkcraft.story.Logros;
import com.darkcraft.story.Story;
import com.darkcraft.world.CastleSites;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Lo que hacen los santuarios (parte del servidor):
 * <ul>
 * <li><b>Encender</b>: el primero que llega a uno apagado lo enciende (queda encendido para todos).</li>
 * <li><b>Descansar</b>: cura del todo, quita los males, rellena los frascos, el mana y la estamina, deja alli el punto
 * de reaparicion y abre el menu del santuario (subir de nivel, repartir los frascos). Con enemigos encima no se puede.</li>
 * <li><b>Donde estan</b>: en los sitios que ya estaban construidos (el Reino de Valdrenn y la Fortaleza de Hueso) se
 * ponen solos la primera vez que alguien pasa cerca, como los libros perdidos; cada uno con su nombre.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Santuarios {
    private Santuarios() {}

    /** Hasta donde se alcanza a usar uno, y hasta donde vale "sigo descansando en el" (para subir de nivel). */
    static final double ALCANCE = 5.5, CERCA = 8.0;
    public static final String SIN_NOMBRE = "darkcraft.santuario.sin_nombre";

    /** En que santuario esta descansando cada uno (hasta que se aleja). */
    private static final Map<UUID, GlobalPos> DESCANSO = new HashMap<>();

    /** Si el jugador sigue junto al santuario encendido en el que descanso (alli se sube de nivel y se reparten los frascos). */
    public static boolean junto(ServerPlayer sp) {
        GlobalPos g = DESCANSO.get(sp.getUUID());
        if (g == null || g.dimension() != sp.level().dimension()) return false;
        if (g.pos().distToCenterSqr(sp.position()) > CERCA * CERCA) return false;
        BlockState st = sp.level().getBlockState(g.pos());
        return st.getBlock() instanceof SantuarioBlock && st.getValue(SantuarioBlock.ENCENDIDO);
    }

    /**
     * Deja al jugador como si hubiera descansado en el santuario de {@code pos} para sus menus (subir de nivel,
     * memorizar), sin curarle ni lo demas: lo usa la guia ("quiero hacerme mas fuerte").
     */
    public static boolean permitir(ServerPlayer sp, BlockPos pos) {
        BlockState st = sp.level().getBlockState(pos);
        if (!(st.getBlock() instanceof SantuarioBlock) || !st.getValue(SantuarioBlock.ENCENDIDO)) return false;
        DESCANSO.put(sp.getUUID(), GlobalPos.of(sp.level().dimension(), pos.immutable()));
        return true;
    }

    /** El santuario encendido mas cercano a {@code centro} (a {@code radio} bloques como mucho), o null. */
    public static BlockPos encendidoCerca(ServerLevel lvl, BlockPos centro, int radio) {
        BlockPos mejor = null;
        double d = Double.MAX_VALUE;
        for (BlockPos q : BlockPos.betweenClosed(centro.offset(-radio, -3, -radio), centro.offset(radio, 3, radio))) {
            BlockState st = lvl.getBlockState(q);
            if (st.getBlock() instanceof SantuarioBlock && st.getValue(SantuarioBlock.ENCENDIDO) && q.distSqr(centro) < d) {
                d = q.distSqr(centro);
                mejor = q.immutable();
            }
        }
        return mejor;
    }

    // ------------------------------------------------------------------ usar uno

    /** El jugador usa el santuario de {@code pos} (clic derecho o la tecla del aviso). */
    public static void usar(ServerPlayer sp, BlockPos pos) {
        ServerLevel lvl = sp.serverLevel();
        if (!sp.isAlive() || sp.isSpectator() || !lvl.isLoaded(pos)) return;
        BlockState st = lvl.getBlockState(pos);
        if (!(st.getBlock() instanceof SantuarioBlock)) return;
        if (pos.distToCenterSqr(sp.position()) > ALCANCE * ALCANCE) return;
        if (!sp.isCreative() && Rol.enCombate(sp)) {
            Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.santuario.enemigos", 0));
            return;
        }
        if (!st.getValue(SantuarioBlock.ENCENDIDO)) encender(sp, lvl, pos, st);
        else descansar(sp, lvl, pos);
    }

    private static void encender(ServerPlayer sp, ServerLevel lvl, BlockPos pos, BlockState st) {
        lvl.setBlock(pos, st.setValue(SantuarioBlock.ENCENDIDO, true), 3);
        double x = pos.getX() + 0.5, y = pos.getY() + 0.9, z = pos.getZ() + 0.5;
        lvl.playSound(null, x, y, z, DarkCraftMod.SFX_SANTUARIO_ENCIENDE.get(), SoundSource.BLOCKS, 1.3f, 1.0f);
        lvl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y + 0.2, z, 36, 0.18, 0.35, 0.18, 0.045);
        lvl.sendParticles(ParticleTypes.SCULK_SOUL, x, y + 0.3, z, 8, 0.2, 0.2, 0.2, 0.04);
        lvl.sendParticles(ParticleTypes.END_ROD, x, y + 0.4, z, 14, 0.3, 0.5, 0.3, 0.03);
        Net.to(sp, new Net.Cartel(Net.Cartel.SANTUARIO, nombre(lvl, pos), 0));
        Logros.dar(sp, "santuario");
        DarkCraftMod.LOGGER.info("[DarkCraft] {} enciende el santuario de {}", sp.getGameProfile().getName(), pos.toShortString());
    }

    private static void descansar(ServerPlayer sp, ServerLevel lvl, BlockPos pos) {
        descanso(sp);
        BlockPos sitio = dondeReaparecer(lvl, pos, sp);
        if (sitio != null) {
            float mira = (float) (Mth.atan2(pos.getZ() - sitio.getZ(), pos.getX() - sitio.getX()) * Mth.RAD_TO_DEG) - 90f;
            sp.setRespawnPosition(lvl.dimension(), sitio, mira, true, false);
        }
        DESCANSO.put(sp.getUUID(), GlobalPos.of(lvl.dimension(), pos.immutable()));
        lvl.playSound(null, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, DarkCraftMod.SFX_SANTUARIO_DESCANSO.get(), SoundSource.BLOCKS, 0.9f, 1.0f);
        lvl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, sp.getX(), sp.getY() + 0.9, sp.getZ(), 12, 0.3, 0.5, 0.3, 0.01);
        Net.to(sp, new Net.Santuario(pos, nombre(lvl, pos)));
    }

    /** Lo que da descansar: la vida entera, fuera los males, y los frascos, el mana y la estamina llenos. */
    public static void descanso(ServerPlayer sp) {
        sp.setHealth(sp.getMaxHealth());
        List<MobEffect> males = new ArrayList<>();
        for (MobEffectInstance e : sp.getActiveEffects()) if (e.getEffect().getCategory() == MobEffectCategory.HARMFUL) males.add(e.getEffect());
        for (MobEffect m : males) sp.removeEffect(m);
        sp.clearFire();
        sp.setTicksFrozen(0);
        sp.setAirSupply(sp.getMaxAirSupply());
        Frascos.rellenar(sp);
        Combos.llenar(sp);
        Rol.recuperado(sp);
        // el corcel de Morvain, entero y sin espera (parte 7b)
        com.darkcraft.montura.Montura.descanso(sp);
        // y los enemigos caidos vuelven a sus puestos (como en Dark Souls)
        com.darkcraft.enemigos.Puestos.reaparecer(sp);
    }

    /**
     * Donde se reaparece: donde esta el jugador si es buen sitio (de pie en el suelo, con hueco) o, si no, el primer
     * hueco libre pegado al santuario. El juego exige dos bloques libres; si luego los tapan, se reaparece en el mundo.
     */
    private static BlockPos dondeReaparecer(ServerLevel lvl, BlockPos santuario, ServerPlayer sp) {
        BlockPos aqui = sp.blockPosition();
        if (sp.onGround() && libre(lvl, aqui) && aqui.distManhattan(santuario) <= 6 && !aqui.equals(santuario) && !aqui.equals(santuario.above())) return aqui;
        for (int r = 1; r <= 3; r++)
            for (int dy = 0; dy >= -1; dy--)
                for (int dx = -r; dx <= r; dx++)
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                        BlockPos p = santuario.offset(dx, dy, dz);
                        if (libre(lvl, p)) return p;
                    }
        return null;
    }

    private static boolean libre(ServerLevel lvl, BlockPos p) {
        BlockState a = lvl.getBlockState(p), b = lvl.getBlockState(p.above());
        if (!a.getBlock().isPossibleToRespawnInThis(a) || !b.getBlock().isPossibleToRespawnInThis(b)) return false;
        BlockPos pie = p.below();
        return lvl.getBlockState(pie).isFaceSturdy(lvl, pie, Direction.UP);
    }

    /** Despertar en una cama tras pasar la noche tambien es descansar (mientras siga habiendo mundo normal). */
    @SubscribeEvent
    public static void onDespertar(PlayerWakeUpEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp && !e.wakeImmediately() && !e.updateLevel() && sp.isAlive()) descanso(sp);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        DESCANSO.remove(e.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent e) {
        DESCANSO.clear();
        SITIOS.clear();
    }

    // ------------------------------------------------------------------ poner uno (y su nombre)

    /** Pone un santuario apagado en {@code pos} con su nombre (la clave del texto). */
    public static void poner(ServerLevel lvl, BlockPos pos, String clave) {
        lvl.setBlock(pos, DarkCraftMod.SANTUARIO.get().defaultBlockState(), 3);
        Datos d = Datos.get(lvl.getServer());
        d.nombres.put(clave(lvl, pos), clave);
        d.setDirty();
    }

    private static String clave(ServerLevel lvl, BlockPos pos) {
        return lvl.dimension().location() + "|" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    /** La clave del nombre del santuario de {@code pos} (uno puesto a mano no tiene: "Santuario"). */
    public static String nombre(ServerLevel lvl, BlockPos pos) {
        return Datos.get(lvl.getServer()).nombres.getOrDefault(clave(lvl, pos), SIN_NOMBRE);
    }

    // ------------------------------------------------------------------ los de los sitios que ya estaban construidos

    /** Los de un Reino de Valdrenn: {nombre, sitio del mapa (o null), dx, dz respecto al sitio}. */
    /**
     * Los de Valdrenn: {clave, lugar, dx, dz, radio de busqueda}. El de la Torre de Ascenso va arriba de la torre, en la
     * plataforma (el lugar "torre_ascenso" aqui es el centro de la torre, no donde se pone su nombre en el mapa), al otro
     * lado del cofre de la cronica; el de la fuente, a un lado de la Fuente Mayor.
     */
    private static final Object[][] VALDRENN = {
            {"torre_ascenso", "torre_ascenso", -4, 2, 3},
            {"fuente", "fuente", 4, 0, 10},
    };

    /** Cada dos segundos: si el jugador pasa cerca del sitio de un santuario que aun no esta, se pone. */
    public static void tick(ServerPlayer p) {
        ServerLevel lvl = p.serverLevel();
        if (lvl.dimension() != Level.OVERWORLD) return;
        Datos d = Datos.get(p.server);
        if (p.getY() >= CastlePlan.Y0 - 40) {
            int[] s = CastleSites.nearest(lvl.getSeed(), p.getBlockX(), p.getBlockZ(), 1);
            if (s != null && Math.hypot(s[0] - p.getX(), s[1] - p.getZ()) <= CastlePlan.FOOTPRINT) valdrenn(p, lvl, s, d);
        }
        Fortalezas.Sitio f = Fortalezas.cercana(lvl.getSeed(), p.getX(), p.getZ(), 110);
        if (f != null && p.getY() >= Fortalezas.Y - 30) fortaleza(p, lvl, f, d);
    }

    private static void valdrenn(ServerPlayer p, ServerLevel lvl, int[] s, Datos d) {
        String reino = s[0] + "," + s[1];
        Sitios donde = null;
        for (Object[] v : VALDRENN) {
            String k = reino + ":" + v[0];
            if (d.puestos.containsKey(k)) continue;
            if (donde == null) donde = sitios(lvl, s);
            int[] pos = donde.lugares.get((String) v[1]);
            if (pos == null) continue;
            int x = s[0] + pos[0] + (Integer) v[2], z = s[1] + pos[1] + (Integer) v[3], radio = (Integer) v[4];
            if (Math.hypot(x - p.getX(), z - p.getZ()) > 56) continue;
            // primero, a ras del suelo de la isla; si alli no hay hueco, un poco mas arriba o mas abajo
            BlockPos sitio = hueco(lvl, x, z, CastlePlan.Y0, CastlePlan.Y0, radio, 2);
            if (sitio == null) sitio = hueco(lvl, x, z, CastlePlan.Y0 - 2, CastlePlan.Y0 + 3, radio, 2);
            if (sitio == null) continue;
            hecho(lvl, d, k, sitio, "darkcraft.santuario." + v[0]);
        }
        // la guia, sentada junto al santuario de la fuente (una vez; si alguien la quita, no vuelve sola)
        String kg = reino + ":guia";
        if (!d.puestos.containsKey(kg)) {
            BlockPos f = deSitio(p.server, reino + ":fuente");
            if (f != null && f.distToCenterSqr(p.position()) < 40 * 40 && lvl.isLoaded(f)) {
                BlockPos sitio = junto(lvl, f);
                // de espaldas al fuego, mirando a quien llega
                BlockPos mira = sitio == null ? null : sitio.offset(sitio.getX() - f.getX(), 0, sitio.getZ() - f.getZ());
                if (sitio != null && com.darkcraft.personajes.Guia.poner(lvl, sitio, mira, f) != null) {
                    d.puestos.put(kg, sitio.getX() + "," + sitio.getY() + "," + sitio.getZ());
                    d.setDirty();
                    DarkCraftMod.LOGGER.info("[DarkCraft] La guia, junto al santuario de la fuente: {}", sitio.toShortString());
                }
            }
        }
        // Kerrick, el mercader: al otro lado del santuario de la fuente (lejos de la guia), con un par de barriles (una vez)
        String km = reino + ":mercader";
        if (!d.puestos.containsKey(km)) {
            BlockPos f = deSitio(p.server, reino + ":fuente");
            if (f != null && f.distToCenterSqr(p.position()) < 40 * 40 && lvl.isLoaded(f)) {
                BlockPos sitio = cerca(lvl, f, 3, 4, deSitio(p.server, kg), 3.0);
                if (sitio != null) {
                    BlockPos mira = sitio.offset(sitio.getX() - f.getX(), 0, sitio.getZ() - f.getZ());
                    if (com.darkcraft.personajes.Mercader.poner(lvl, sitio, mira) != null) {
                        puesto(lvl, sitio, mira);
                        d.puestos.put(km, sitio.getX() + "," + sitio.getY() + "," + sitio.getZ());
                        d.setDirty();
                        DarkCraftMod.LOGGER.info("[DarkCraft] El mercader, junto al santuario de la fuente: {}", sitio.toShortString());
                    }
                }
            }
        }
        // Dorvan, el herrero: en el patio de armas, con su yunque, su fragua y sus cosas (una vez)
        String kh = reino + ":herrero";
        if (!d.puestos.containsKey(kh)) {
            int x = s[0] + HERRERIA[0], z = s[1] + HERRERIA[1];
            if (Math.hypot(x - p.getX(), z - p.getZ()) <= 48 && lvl.isLoaded(new BlockPos(x, CastlePlan.Y0, z))) {
                BlockPos sitio = herreria(lvl, x, z);
                if (sitio != null) {
                    d.puestos.put(kh, sitio.getX() + "," + sitio.getY() + "," + sitio.getZ());
                    d.setDirty();
                    DarkCraftMod.LOGGER.info("[DarkCraft] El herrero, en el patio de armas: {}", sitio.toShortString());
                }
            }
        }
        // el de la recepcion del arbol: dentro del tronco, a un lado de la fuente (antes de la puerta de Ser Gavran)
        String k = reino + ":arbol";
        if (!d.puestos.containsKey(k)) {
            if (donde == null) donde = sitios(lvl, s);
            int[] t = donde.arbol;
            if (t != null && Math.hypot(t[0] - 9 - p.getX(), t[1] - 3 - p.getZ()) <= 40 && Math.abs(p.getY() - t[2]) < 24) {
                BlockPos sitio = hueco(lvl, t[0] - 9, t[1] - 3, t[2], t[2], 3, 2);
                if (sitio != null) hecho(lvl, d, k, sitio, "darkcraft.santuario.arbol");
            }
        }
    }

    /** Donde va la herreria de Dorvan respecto al centro del reino: en el patio de armas, a un lado (este) del campo. */
    private static final int[] HERRERIA = {112, 161};

    /**
     * Un sitio a entre {@code r0} y {@code r1} bloques de {@code s} (suelo firme y dos de aire, con sitio alrededor) y a
     * mas de {@code lejos} de {@code evitar} (si lo hay).
     */
    public static BlockPos cerca(ServerLevel lvl, BlockPos s, int r0, int r1, BlockPos evitar, double lejos) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int r = r0; r <= r1; r++)
            for (int k = 0; k < r * 8; k++) {
                double a = k * Math.PI * 2 / (r * 8) - Math.PI * 3 / 4;
                int x = s.getX() + (int) Math.round(Math.cos(a) * r), z = s.getZ() + (int) Math.round(Math.sin(a) * r);
                if (evitar != null && Math.hypot(x - evitar.getX(), z - evitar.getZ()) < lejos) continue;
                for (int y = s.getY() - 1; y <= s.getY() + 1; y++) {
                    if (!suelo(lvl, m, x, y, z)) continue;
                    int lados = 0;
                    for (Direction d : Direction.Plane.HORIZONTAL) if (suelo(lvl, m, x + d.getStepX(), y, z + d.getStepZ())) lados++;
                    if (lados >= 3) return new BlockPos(x, y, z);
                }
            }
        return null;
    }

    /** Los barriles del mercader, a su lado (donde haya sitio). */
    private static void puesto(ServerLevel lvl, BlockPos sitio, BlockPos mira) {
        Direction frente = Direction.getNearest(mira.getX() - sitio.getX(), 0, mira.getZ() - sitio.getZ());
        Direction lado = frente.getClockWise();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        BlockPos[] donde = {sitio.relative(lado, 2), sitio.relative(lado, 2).relative(frente.getOpposite()), sitio.relative(lado.getOpposite(), 2)};
        BlockState[] que = {net.minecraft.world.level.block.Blocks.BARREL.defaultBlockState().setValue(net.minecraft.world.level.block.BarrelBlock.FACING, Direction.UP),
                net.minecraft.world.level.block.Blocks.BARREL.defaultBlockState().setValue(net.minecraft.world.level.block.BarrelBlock.FACING, lado),
                net.minecraft.world.level.block.Blocks.LANTERN.defaultBlockState()};
        for (int i = 0; i < donde.length; i++)
            if (suelo(lvl, m, donde[i].getX(), donde[i].getY(), donde[i].getZ())) lvl.setBlock(donde[i], que[i], 3);
    }

    /**
     * La herreria de Dorvan cerca de (x, z): el yunque delante de el, la fragua encendida (alto horno y hoguera) a su
     * izquierda, un barril y una piedra de afilar a su derecha. Dorvan mira al oeste, al yunque. Devuelve donde esta el.
     */
    private static BlockPos herreria(ServerLevel lvl, int x, int z) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int r = 0; r <= 4; r++)
            for (int k = 0; k < Math.max(1, r * 8); k++) {
                double a = k * Math.PI * 2 / Math.max(1, r * 8);
                int sx = x + (int) Math.round(Math.cos(a) * r), sz = z + (int) Math.round(Math.sin(a) * r);
                for (int y = CastlePlan.Y0 - 1; y <= CastlePlan.Y0 + 2; y++) {
                    // el y el yunque, con aire y suelo; y que se pueda llegar por delante del yunque
                    if (!suelo(lvl, m, sx, y, sz) || !suelo(lvl, m, sx - 1, y, sz) || !suelo(lvl, m, sx - 2, y, sz)) continue;
                    BlockPos el = new BlockPos(sx, y, sz), yunque = el.west();
                    lvl.setBlock(yunque, net.minecraft.world.level.block.Blocks.ANVIL.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.AnvilBlock.FACING, Direction.NORTH), 3);
                    Object[][] cosas = {
                            {-1, 2, net.minecraft.world.level.block.Blocks.BLAST_FURNACE.defaultBlockState()
                                    .setValue(net.minecraft.world.level.block.AbstractFurnaceBlock.FACING, Direction.NORTH)
                                    .setValue(net.minecraft.world.level.block.AbstractFurnaceBlock.LIT, true)},
                            {0, 2, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState()},
                            {1, 2, net.minecraft.world.level.block.Blocks.BARREL.defaultBlockState()
                                    .setValue(net.minecraft.world.level.block.BarrelBlock.FACING, Direction.UP)},
                            {-1, -2, net.minecraft.world.level.block.Blocks.GRINDSTONE.defaultBlockState()},
                            {1, -2, net.minecraft.world.level.block.Blocks.LANTERN.defaultBlockState()},
                    };
                    for (Object[] c : cosas) {
                        BlockPos q = el.offset((Integer) c[0], 0, (Integer) c[1]);
                        if (suelo(lvl, m, q.getX(), q.getY(), q.getZ())) lvl.setBlock(q, (BlockState) c[2], 3);
                    }
                    return com.darkcraft.personajes.Herrero.poner(lvl, el, yunque) != null ? el : null;
                }
            }
        return null;
    }

    /** Un sitio a dos o tres bloques del santuario (suelo firme y dos de aire) para sentar a la guia. */
    public static BlockPos junto(ServerLevel lvl, BlockPos s) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int r = 2; r <= 3; r++)
            for (int k = 0; k < r * 8; k++) {
                double a = k * Math.PI * 2 / (r * 8) + Math.PI / 4;
                int x = s.getX() + (int) Math.round(Math.cos(a) * r), z = s.getZ() + (int) Math.round(Math.sin(a) * r);
                for (int y = s.getY() - 1; y <= s.getY() + 1; y++)
                    if (suelo(lvl, m, x, y, z)) return new BlockPos(x, y, z);
            }
        return null;
    }

    /** El de la Fortaleza de Hueso: en el rellano de fuera de la puerta, a un lado del paso (cuando la fortaleza esta levantada). */
    private static void fortaleza(ServerPlayer p, ServerLevel lvl, Fortalezas.Sitio f, Datos d) {
        String k = "f" + f.clave();
        if (d.puestos.containsKey(k) || !FortalezaDatos.get(p.server).de(f).completa) return;
        int x = f.x() + 5, z = f.z() + TorreCupula.PFUERA + 5;
        if (Math.hypot(x - p.getX(), z - p.getZ()) > 56) return;
        BlockPos sitio = hueco(lvl, x, z, Fortalezas.Y, Fortalezas.Y, 2, 1);
        if (sitio != null) hecho(lvl, d, k, sitio, "darkcraft.santuario.fortaleza");
    }

    /** Pone el santuario de un sitio (si aun no estaba puesto): las paradas del camino de nubes (parte 5d). */
    public static void ponerEn(ServerLevel lvl, String k, BlockPos sitio, String nombre) {
        Datos d = Datos.get(lvl.getServer());
        if (d.puestos.containsKey(k)) return;
        hecho(lvl, d, k, sitio, nombre);
    }

    private static void hecho(ServerLevel lvl, Datos d, String k, BlockPos sitio, String nombre) {
        poner(lvl, sitio, nombre);
        d.puestos.put(k, sitio.getX() + "," + sitio.getY() + "," + sitio.getZ());
        d.setDirty();
        DarkCraftMod.LOGGER.info("[DarkCraft] Santuario {} puesto en {}", nombre, sitio.toShortString());
    }

    /** Olvida el santuario de un sitio (su sitio se rehizo): se vuelve a poner donde toque al acercarse alguien. */
    public static void olvidar(MinecraftServer srv, String clave) {
        Datos d = Datos.get(srv);
        if (d.puestos.remove(clave) != null) d.setDirty();
    }

    /** Donde esta el santuario de un sitio construido ("x,z:torre_ascenso", "x,z:fuente", "x,z:arbol"...), o null si aun no se ha puesto. */
    public static BlockPos deSitio(MinecraftServer srv, String clave) {
        String v = Datos.get(srv).puestos.get(clave);
        if (v == null) return null;
        String[] q = v.split(",");
        try {
            return new BlockPos(Integer.parseInt(q[0]), Integer.parseInt(q[1]), Integer.parseInt(q[2]));
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Los santuarios puestos hasta ahora (para la prueba de la CI). */
    public static String resumen(MinecraftServer srv) {
        return Datos.get(srv).puestos.toString();
    }

    /** Lo que hace falta saber de un reino para poner sus santuarios (se calcula una vez por reino). */
    private record Sitios(Map<String, int[]> lugares, int[] arbol) {}

    private static final Map<String, Sitios> SITIOS = new HashMap<>();

    private static Sitios sitios(ServerLevel lvl, int[] s) {
        return SITIOS.computeIfAbsent(s[0] + "," + s[1], k -> {
            CastlePlan plan = Story.planAt(lvl, s[0], s[1]);
            MapShapes.Data m = MapShapes.build(plan);
            Map<String, int[]> l = new HashMap<>();
            for (int i = 0; i < m.labels().length; i++) l.put(m.labels()[i], new int[]{m.labelPos()[i * 2], m.labelPos()[i * 2 + 1]});
            // la torre: su centro (el nombre del mapa va desplazado, fuera de la torre, en el aire)
            CastlePlan.Cine ci = plan.cine();
            l.put("torre_ascenso", new int[]{ci.ascX() - s[0], ci.ascZ() - s[1]});
            return new Sitios(l, plan.treeHall());
        });
    }

    /**
     * Un sitio para el santuario cerca de (x0, z0): suelo firme entre las alturas y0 e y1 (de abajo arriba), dos bloques
     * de aire encima y otros {@code despejado} de aire alrededor (que no quede encajado ni tape un paso estrecho), sin
     * agua y sin puertas al lado.
     */
    static BlockPos hueco(ServerLevel lvl, int x0, int z0, int y0, int y1, int radio, int despejado) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int r = 0; r <= radio; r++)
            for (int k = 0; k < Math.max(1, r * 8); k++) {
                double a = k * Math.PI * 2 / Math.max(1, r * 8);
                int x = x0 + (int) Math.round(Math.cos(a) * r), z = z0 + (int) Math.round(Math.sin(a) * r);
                if (!lvl.hasChunkAt(m.set(x, y0, z))) continue;
                for (int y = y0; y <= y1; y++) {
                    if (!suelo(lvl, m, x, y, z)) continue;
                    // alrededor: al menos `despejado` de los cuatro lados con suelo y aire (se puede llegar y rodearlo)
                    int lados = 0;
                    boolean puerta = false;
                    for (Direction d : Direction.Plane.HORIZONTAL) {
                        if (lvl.getBlockState(m.set(x + d.getStepX(), y, z + d.getStepZ())).getBlock() instanceof net.minecraft.world.level.block.DoorBlock) puerta = true;
                        if (suelo(lvl, m, x + d.getStepX(), y, z + d.getStepZ())) lados++;
                    }
                    if (puerta || lados < Math.max(1, despejado)) continue;
                    return new BlockPos(x, y, z);
                }
            }
        return null;
    }

    private static boolean suelo(ServerLevel lvl, BlockPos.MutableBlockPos m, int x, int y, int z) {
        if (!lvl.getBlockState(m.set(x, y - 1, z)).isFaceSturdy(lvl, m, Direction.UP)) return false;
        if (!lvl.getBlockState(m.set(x, y, z)).isAir() || !lvl.getFluidState(m).isEmpty()) return false;
        return lvl.getBlockState(m.set(x, y + 1, z)).isAir();
    }

    // ------------------------------------------------------------------ lo que se guarda

    /** Los santuarios puestos en los sitios construidos y el nombre de cada santuario. */
    public static final class Datos extends SavedData {
        public static final String ID = "darkcraft_santuarios";
        /** Los de los sitios construidos: "reino:sitio" -> "x,y,z". */
        final Map<String, String> puestos = new HashMap<>();
        /** El nombre de cada uno: "dimension|x,y,z" -> la clave del texto. */
        final Map<String, String> nombres = new HashMap<>();

        public static Datos get(MinecraftServer srv) {
            return srv.overworld().getDataStorage().computeIfAbsent(Datos::load, Datos::new, ID);
        }

        static Datos load(CompoundTag t) {
            Datos d = new Datos();
            CompoundTag l = t.getCompound("puestos");
            for (String k : l.getAllKeys()) d.puestos.put(k, l.getString(k));
            CompoundTag n = t.getCompound("nombres");
            for (String k : n.getAllKeys()) d.nombres.put(k, n.getString(k));
            return d;
        }

        @Override
        public CompoundTag save(CompoundTag t) {
            CompoundTag l = new CompoundTag();
            puestos.forEach(l::putString);
            t.put("puestos", l);
            CompoundTag n = new CompoundTag();
            nombres.forEach(n::putString);
            t.put("nombres", n);
            return t;
        }
    }
}
