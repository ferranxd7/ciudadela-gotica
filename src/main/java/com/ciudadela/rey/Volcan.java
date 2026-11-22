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
    private static final String HECHO = "volcan_v1";

    /** Volcanes: {x, z, radio, alto sobre la lava}. Los mismos en el cliente (humo de los crateres). */
    public static final int[][] VOLCANES = volcanes();

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
        for (int i = 0; i < VOLCANES.length; i++) {
            int[] vv = VOLCANES[i];
            for (int x = -vv[2]; x <= vv[2]; x++)
                for (int z = -vv[2]; z <= vv[2]; z++)
                    if (x * x + z * z <= vv[2] * vv[2]) COLUMNAS.add(new int[]{1, vv[0] + x, vv[1] + z, i});
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
        for (int k = 0; k < 900 && !COLUMNAS.isEmpty(); k++) columna(v, COLUMNAS.poll());
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
            case 1 -> volcan(v, x, z, VOLCANES[c[3]]);
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

    private static void volcan(ServerLevel v, int x, int z, int[] vv) {
        double dx = x - vv[0], dz = z - vv[1];
        double d = Math.sqrt(dx * dx + dz * dz) / vv[2];
        if (d > 1) return;
        double n = suave(x * 0.15, z * 0.15, vv[0]) - 0.5;
        double crater = 0.2;
        double h;
        if (d < crater) h = vv[3] - 7 + n * 2;
        else h = vv[3] * Math.pow(1 - (d - crater) / (1 - crater), 1.25) * (1 - 0.25 * (d - crater)) + n * 4 + (d < crater + 0.06 ? 2 : 0);
        int top = MAR + (int) Math.round(Math.max(0, h));
        int from = Math.max(MAR - 2, top - 7);
        for (int y = from; y <= top; y++) {
            BlockState st;
            if (y == top) {
                double q = ruido(x, z, 11);
                if (d < crater + 0.12 && q < 0.6) st = Blocks.MAGMA_BLOCK.defaultBlockState();
                else if (q < 0.18) st = Blocks.MAGMA_BLOCK.defaultBlockState();
                else if (q < 0.55) st = Blocks.BASALT.defaultBlockState();
                else st = Blocks.BLACKSTONE.defaultBlockState();
            } else st = Blocks.BLACKSTONE.defaultBlockState();
            set(v, x, y, z, st);
        }
        if (d < crater) {
            // lago de lava en el crater
            int lava = MAR + vv[3] - 5;
            for (int y = top + 1; y <= lava; y++) set(v, x, y, z, Blocks.LAVA.defaultBlockState());
        } else if (d < crater + 0.08 && ruido(x, z, 17) < 0.08) {
            // fuentes en el borde: rios de lava que bajan por la ladera
            set(v, x, top + 1, z, Blocks.LAVA.defaultBlockState());
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
    public static final float CINE = 13.5f;

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
        // 2) desde el otro lado de la plataforma, mirando arriba: el rey en llamas sobre el volcan mas alto, y cae
        int[] vb = VOLCANES[0];
        for (int[] v : VOLCANES) if (v[3] > vb[3]) vb = v;
        double dl = Math.max(1, Math.hypot(vb[0], vb[1])), ux = vb[0] / dl, uz = vb[1] / dl;
        l.add(new Shot(4.2f, 7.6f, 74, 1.5f, 0,
                p(-ux * 30 + 0.5, y + 3.5, -uz * 30 + 0.5, -ux * 25 + 0.5, y + 3.0, -uz * 25 + 0.5),
                p(0.5, y + 28, 0.5, 0.5, y + 26.5, 0.5, 0.5, y + 6, 0.5)));
        // 3) el golpe contra la plataforma
        l.add(new Shot(7.6f, 10.2f, 72, 0, 2,
                p(9, y + 4, 10, 7, y + 3.4, 8),
                p(0.5, y + 2, 0.5, 0.5, y + 2.2, 0.5)));
        // 4) cerca de la cara: los ojos de lava
        l.add(new Shot(10.2f, CINE - 0.6f, 58, 0, 1,
                p(0.5, y + 3.6, -4.2, 0.5, y + 3.5, -3.4),
                p(0.5, y + 3.4, 0.5, 0.5, y + 3.3, 0.5)));
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
