package com.ciudadela.rey;

import com.ciudadela.CiudadelaMod;
import com.ciudadela.cine.Cinematics;
import com.ciudadela.cine.Shot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

/**
 * La dimension de los volcanes (fase 3 del Rey): un mar de lava con volcanes alrededor, una plataforma
 * de piedra negra en medio con techo invisible, y meteoritos que caen (solo de adorno, en el cliente).
 * La plataforma y los volcanes se construyen poco a poco la primera vez que empieza una pelea.
 */
public final class Volcan {
    private Volcan() {}

    public static final ResourceKey<Level> NIVEL = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(CiudadelaMod.MODID, "volcanes"));
    public static final int Y = 64;            // bloque de arriba de la plataforma
    public static final int R = 22;            // radio de la plataforma
    public static final int MAR = 61;          // nivel que se pisa sobre la lava
    public static final Vec3 CENTRO = new Vec3(0.5, Y + 1, 0.5);
    private static final TicketType<ChunkPos> TICKET = TicketType.create("ciudadela_volcan", Comparator.comparingLong(ChunkPos::toLong), 20 * 60);
    private static final String HECHO = "volcan_v2";

    /**
     * Los volcanes de bloques que habia antes cerca de la plataforma: {x, z, radio, alto}. Ya no se construyen (ahora
     * los volcanes son enormes y estan a lo lejos, en el cielo): sus sitios se limpian al reconstruir la arena.
     */
    private static final int[][] ANTIGUOS = volcanes();

    private static int[][] volcanes() {
        Random r = new Random(1717);
        int n = 7;
        int[][] v = new int[n][];
        for (int i = 0; i < n; i++) {
            double a = Math.PI * 2 * i / n + (r.nextDouble() - 0.5) * 0.5;
            double d = 62 + r.nextInt(46);
            int rad = 17 + r.nextInt(13);
            int h = 26 + r.nextInt(30);
            v[i] = new int[]{(int) Math.round(Math.cos(a) * d), (int) Math.round(Math.sin(a) * d), rad, h};
        }
        return v;
    }

    public static boolean es(Level l) {
        return l.dimension() == NIVEL;
    }

    public static ServerLevel nivel(MinecraftServer s) {
        return s.getLevel(NIVEL);
    }

    /** Si la dimension existe y la arena esta construida. */
    public static boolean disponible(ServerLevel sl) {
        ServerLevel v = nivel(sl.getServer());
        return v != null && ReyEstado.get(sl.getServer()).selloAbierto(HECHO);
    }

    // ------------------------------------------------------------------ construir la arena

    private static final ArrayDeque<int[]> COLUMNAS = new ArrayDeque<>();
    private static boolean construyendo;

    /** Empieza a construir la arena si no lo esta (se llama al empezar una pelea). */
    public static void preparar(MinecraftServer server) {
        ServerLevel v = nivel(server);
        if (v == null || construyendo || ReyEstado.get(server).selloAbierto(HECHO)) return;
        construyendo = true;
        COLUMNAS.clear();
        // {tipo, x, z}: 0 plataforma, 1 volcan i, 2 aguja
        for (int x = -R - 1; x <= R + 1; x++)
            for (int z = -R - 1; z <= R + 1; z++) COLUMNAS.add(new int[]{0, x, z, 0});
        // donde estaban los volcanes de antes: vuelve el mar de lava
        for (int[] vv : ANTIGUOS) {
            int rr = vv[2] + 2;
            for (int x = -rr; x <= rr; x++)
                for (int z = -rr; z <= rr; z++)
                    if (x * x + z * z <= rr * rr) COLUMNAS.add(new int[]{3, vv[0] + x, vv[1] + z, 0});
        }
        // rocas negras y costras de lava enfriada por el mar, entre la plataforma y el horizonte
        Random rk = new Random(4242);
        for (int i = 0; i < 34; i++) {
            double a = rk.nextDouble() * Math.PI * 2, d = 30 + rk.nextDouble() * 80;
            int ax = (int) (Math.cos(a) * d), az = (int) (Math.sin(a) * d), rad = 3 + rk.nextInt(7), h = 1 + rk.nextInt(5);
            for (int x = -rad; x <= rad; x++)
                for (int z = -rad; z <= rad; z++)
                    if (x * x + z * z <= rad * rad) COLUMNAS.add(new int[]{4, ax + x, az + z, rad * 16 + h});
        }
        Random r = new Random(99);
        for (int i = 0; i < 12; i++) {
            double a = r.nextDouble() * Math.PI * 2, d = 30 + r.nextInt(26);
            int ax = (int) (Math.cos(a) * d), az = (int) (Math.sin(a) * d), rad = 2 + r.nextInt(3), h = 3 + r.nextInt(11);
            for (int x = -rad; x <= rad; x++)
                for (int z = -rad; z <= rad; z++)
                    if (x * x + z * z <= rad * rad) COLUMNAS.add(new int[]{2, ax + x, az + z, h - (int) Math.round(Math.sqrt(x * x + z * z) * 1.5)});
        }
        COLUMNAS.add(new int[]{9, 0, 0, 0}); // luces y techo invisible
        CiudadelaMod.LOGGER.info("[Ciudadela] Construyendo la arena de los volcanes ({} columnas)", COLUMNAS.size());
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !construyendo) return;
        ServerLevel v = nivel(e.getServer());
        if (v == null) {
            construyendo = false;
            return;
        }
        // por bloques puestos, para no frenar el servidor (limpiar un volcan viejo es mucho trabajo)
        int presupuesto = 24000;
        while (presupuesto > 0 && !COLUMNAS.isEmpty()) {
            int[] c = COLUMNAS.poll();
            presupuesto -= switch (c[0]) {
                case 3 -> 70;
                case 0 -> 22;
                default -> 12;
            };
            columna(v, c);
        }
        if (COLUMNAS.isEmpty()) {
            construyendo = false;
            ReyEstado.get(e.getServer()).abrirSello(HECHO);
            CiudadelaMod.LOGGER.info("[Ciudadela] Arena de los volcanes lista");
        }
    }

    private static double ruido(int x, int z, int s) {
        long h = (x * 73856093L) ^ (z * 19349663L) ^ (s * 83492791L);
        h ^= (h >>> 13);
        h *= 0x5bd1e995L;
        h ^= (h >>> 15);
        return ((h & 0xFFFF) / 65535.0);
    }

    private static double suave(double x, double z, int s) {
        int x0 = (int) Math.floor(x), z0 = (int) Math.floor(z);
        double fx = x - x0, fz = z - z0;
        fx = fx * fx * (3 - 2 * fx);
        fz = fz * fz * (3 - 2 * fz);
        double a = ruido(x0, z0, s), b = ruido(x0 + 1, z0, s), c = ruido(x0, z0 + 1, s), d = ruido(x0 + 1, z0 + 1, s);
        return Mth.lerp(fz, Mth.lerp(fx, a, b), Mth.lerp(fx, c, d));
    }

    private static void set(ServerLevel v, int x, int y, int z, BlockState st) {
        v.setBlock(new BlockPos(x, y, z), st, 2);
    }

    private static void columna(ServerLevel v, int[] c) {
        int x = c[1], z = c[2];
        v.getChunk(x >> 4, z >> 4);
        switch (c[0]) {
            case 0 -> plataforma(v, x, z);
            case 3 -> limpiar(v, x, z);
            case 4 -> roca(v, x, z, c[3] / 16, c[3] % 16);
            case 2 -> {
                int top = MAR + c[3];
                for (int y = MAR - 3; y <= top; y++)
                    set(v, x, y, z, ruido(x, y * 7 + z, 5) < 0.3 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState());
            }
            default -> luces(v);
        }
    }

    private static void plataforma(ServerLevel v, int x, int z) {
        double d = Math.sqrt(x * x + z * z);
        if (d > R + 0.5) return;
        // soporte: cono invertido de basalto hasta la lava
        int bottom = (int) Math.round(MAR - 4 + Math.max(0, d - 12) * 0.6);
        for (int y = bottom; y < Y; y++) {
            BlockState st = ruido(x * 3 + y, z * 5 - y, 2) < 0.35 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState();
            if (d > R - 1.5 && y == Y - 1) st = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            set(v, x, y, z, st);
        }
        // el suelo: anillos de piedra negra pulida, con grietas y oro
        BlockState top;
        if (d < 2.6) top = d < 1.2 ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
        else if (d > R - 1.3) {
            double ang = Math.toDegrees(Math.atan2(z, x));
            top = (Math.floorMod((int) Math.round(ang), 30) < 4) ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
        } else if (Math.abs(d - 9) < 0.55 || Math.abs(d - 15) < 0.55)
            top = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        else
            top = ruido(x, z, 3) < 0.22 ? Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        set(v, x, Y, z, top);
        // aire encima por si acaso
        for (int y = Y + 1; y <= Y + 14; y++) set(v, x, y, z, Blocks.AIR.defaultBlockState());
    }

    /** Quita lo que hubiera encima del mar (los volcanes de antes) y deja la lava. */
    private static void limpiar(ServerLevel v, int x, int z) {
        BlockState lava = Blocks.LAVA.defaultBlockState();
        for (int y = MAR - 2; y < MAR; y++) set(v, x, y, z, lava);
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int y = MAR; y <= MAR + 62; y++) {
            m.set(x, y, z);
            if (!v.getBlockState(m).isAir()) v.setBlock(m, Blocks.AIR.defaultBlockState(), 2);
        }
    }

    /** Una roca negra que asoma del mar de lava, con vetas de magma. */
    private static void roca(ServerLevel v, int x, int z, int rad, int h) {
        // centro de la roca: el de su circulo (se recalcula desde el radio no hace falta: la altura cae con el ruido)
        double n = suave(x * 0.35, z * 0.35, 31);
        double borde = suave(x * 0.2 + 7, z * 0.2 - 3, 41);
        int top = MAR - 1 + (int) Math.round(h * (0.35 + 0.9 * n) - (borde < 0.3 ? 1 : 0));
        if (top < MAR - 1) return;
        for (int y = MAR - 3; y <= top; y++) {
            BlockState st = ruido(x * 5 + y, z * 3 - y, 13) < 0.4 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState();
            if (y == top && ruido(x, z, 19) < 0.16) st = Blocks.MAGMA_BLOCK.defaultBlockState();
            set(v, x, y, z, st);
        }
    }

    private static void luces(ServerLevel v) {
        BlockState light = Blocks.LIGHT.defaultBlockState();
        for (int x = -R; x <= R; x += 6)
            for (int z = -R; z <= R; z += 6)
                if (x * x + z * z <= (R - 1) * (R - 1)) {
                    set(v, x, Y + 3, z, light);
                    set(v, x, Y + 9, z, light);
                }
        // techo invisible: nada de subir mas alto ni de volar
        int rc = R + 5;
        for (int x = -rc; x <= rc; x++)
            for (int z = -rc; z <= rc; z++)
                if (x * x + z * z <= rc * rc) set(v, x, Y + 14, z, Blocks.BARRIER.defaultBlockState());
    }

    // ------------------------------------------------------------------ llevar y traer

    /** Lleva al Rey y a los jugadores a la plataforma, con la cinematica de los volcanes. */
    public static boolean llevar(ServerLevel from, Rey rey, List<ServerPlayer> who) {
        MinecraftServer server = from.getServer();
        ServerLevel v = nivel(server);
        if (v == null) return false;
        for (int cx = -5; cx <= 5; cx++)
            for (int cz = -5; cz <= 5; cz++) v.getChunk(cx, cz);
        v.getChunkSource().addRegionTicket(TICKET, new ChunkPos(0, 0), 7, new ChunkPos(0, 0));
        Rey nuevo = CiudadelaMod.REY.get().create(v);
        if (nuevo == null) return false;
        nuevo.copiarDe(rey);
        nuevo.moveTo(CENTRO.x, CENTRO.y + 26, CENTRO.z, 180f, 0f);
        v.addFreshEntity(nuevo);
        List<UUID> ids = new ArrayList<>();
        int n = Math.max(1, who.size());
        for (int i = 0; i < who.size(); i++) {
            ServerPlayer p = who.get(i);
            double a = Math.PI * 0.5 + (i - (n - 1) / 2.0) * 0.5;
            double px = CENTRO.x + Math.cos(a) * 13, pz = CENTRO.z + Math.sin(a) * 13;
            float yaw = (float) (Math.toDegrees(Math.atan2(CENTRO.z - pz, CENTRO.x - px)) - 90);
            p.teleportTo(v, px, CENTRO.y, pz, yaw, 0f);
            p.clearFire();
            ids.add(p.getUUID());
        }
        nuevo.enVolcanCine(v, ids);
        List<Shot> shots = planos();
        for (ServerPlayer p : who) Cinematics.playShots(p, Cinematics.REY_VOLCAN, shots, CINE);
        rey.discardSinReinicio();
        v.playSound(null, CENTRO.x, CENTRO.y, CENTRO.z, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 3f, 0.5f);
        return true;
    }

    /** Que no se descarguen los chunks de la arena mientras se pelea. */
    public static void mantener(ServerLevel v) {
        v.getChunkSource().addRegionTicket(TICKET, new ChunkPos(0, 0), 7, new ChunkPos(0, 0));
    }

    /** Duracion de la cinematica de los volcanes. */
    public static final float CINE = 15.0f;

    /**
     * Volcanes lejanos (decorado del cielo, los dibuja el cliente): {angulo en grados, distancia, altura, radio de la base}.
     * Estan "en el infinito": no se acercan aunque te muevas.
     */
    public static final float[][] LEJANOS = {
            {12, 330, 250, 150}, {62, 360, 190, 125}, {108, 310, 225, 140}, {161, 370, 300, 175},
            {213, 320, 205, 130}, {262, 350, 260, 160}, {316, 300, 175, 115}};

    /** Direccion (x, z) del volcan lejano mas alto. */
    public static double[] dirGrande() {
        float[] b = LEJANOS[0];
        for (float[] v : LEJANOS) if (v[2] > b[2]) b = v;
        double a = Math.toRadians(b[0]);
        return new double[]{Math.cos(a), Math.sin(a)};
    }

    private static double[] p(double... v) {
        return v;
    }

    /** Planos: el paisaje de volcanes, el rey cayendo del cielo, el golpe en la plataforma y su cara. */
    public static List<Shot> planos() {
        List<Shot> l = new ArrayList<>();
        double y = Y;
        // 1) desde lejos, sobre la lava, la plataforma entre volcanes
        l.add(new Shot(0f, 4.2f, 70, -2, 1,
                p(-70, y + 26, 58, -52, y + 22, 46, -38, y + 18, 34),
                p(0, y + 4, 0, 0, y + 5, 0, 0, y + 6, 0)));
        // 2) desde el otro lado de la plataforma, mirando arriba: el rey en llamas delante del volcan mas alto, y cae
        double[] dg = dirGrande();
        double ux = dg[0], uz = dg[1];
        l.add(new Shot(4.2f, 7.6f, 74, 1.5f, 0,
                p(-ux * 30 + 0.5, y + 3.5, -uz * 30 + 0.5, -ux * 25 + 0.5, y + 3.0, -uz * 25 + 0.5),
                p(0.5, y + 28, 0.5, 0.5, y + 26.5, 0.5, 0.5, y + 6, 0.5)));
        // 3) el golpe contra la plataforma
        l.add(new Shot(7.6f, 9.0f, 72, 0, 2,
                p(9, y + 4, 10, 7, y + 3.4, 8),
                p(0.5, y + 2, 0.5, 0.5, y + 2.2, 0.5)));
        // 4) de cerca: se arranca la chaqueta y empieza a crecer
        l.add(new Shot(9.0f, 11.6f, 64, 0, 1,
                p(4.2, y + 3.0, -5.6, 3.6, y + 2.9, -4.9),
                p(0.5, y + 2.5, 0.5, 0.5, y + 3.4, 0.5)));
        // 5) desde abajo, alejandose: el gigante ruge
        l.add(new Shot(11.6f, CINE - 0.4f, 72, 0, 1,
                p(0.5, y + 1.3, -10.5, 0.5, y + 1.0, -14.5),
                p(0.5, y + 5.0, 0.5, 0.5, y + 6.8, 0.5)));
        return l;
    }

    /** Si alguien se cae de la plataforma: vuelve arriba con algo de dano. */
    public static void caer(ServerPlayer p) {
        ServerLevel v = p.serverLevel();
        double a = Math.atan2(p.getZ() - CENTRO.z, p.getX() - CENTRO.x);
        double px = CENTRO.x + Math.cos(a) * (R - 4), pz = CENTRO.z + Math.sin(a) * (R - 4);
        p.teleportTo(v, px, CENTRO.y + 0.5, pz, p.getYRot(), p.getXRot());
        p.fallDistance = 0;
        p.setDeltaMovement(Vec3.ZERO);
        var reg = v.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        p.invulnerableTime = 0;
        p.hurt(new net.minecraft.world.damagesource.DamageSource(reg.getHolderOrThrow(net.minecraft.world.damagesource.DamageTypes.LAVA)), 6f);
        v.playSound(null, px, CENTRO.y, pz, SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 1f, 0.8f);
    }

    /** Devuelve a un jugador al salon del trono (de la ciudadela mas cercana a su punto de aparicion). */
    public static void devolver(ServerPlayer p) {
        ServerLevel ow = p.getServer().overworld();
        BlockPos sp = ow.getSharedSpawnPos();
        Sala s = null;
        Rey r = ReyEventos.luchaDe(p);
        if (r != null) s = r.sala();
        if (s == null) s = Sala.near(ow, sp.getX(), sp.getZ());
        p.clearFire();
        if (s != null) {
            Vec3 c = s.center();
            p.teleportTo(ow, c.x, s.t.y(), c.z + 4, 180f, 0f);
        } else p.teleportTo(ow, sp.getX() + 0.5, sp.getY(), sp.getZ() + 0.5, 0f, 0f);
    }

    /** Devuelve a todos al salon del trono de esa sala. */
    public static void devolverTodos(ServerLevel v, Sala s) {
        ServerLevel ow = v.getServer().overworld();
        List<ServerPlayer> l = new ArrayList<>(v.players());
        for (int i = 0; i < l.size(); i++) {
            ServerPlayer p = l.get(i);
            p.clearFire();
            if (s != null) {
                Vec3 c = s.center();
                double off = (i - (l.size() - 1) / 2.0) * 1.5;
                p.teleportTo(ow, c.x + off, s.t.y(), c.z + 6, 180f, 0f);
            } else devolver(p);
        }
    }
}
