package com.ciudadela.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Microclima de la ciudadela: siempre llueve (con tormenta) en la isla y sus alrededores.
 * 1) La zona usa un bioma propio ("tormenta") con cielo gris, niebla oscura y lluvia asegurada.
 * 2) A cada jugador que esta dentro se le envia lluvia y truenos aunque en el resto del mundo haga
 *    sol, y caen relampagos solo visuales (no queman nada). Al salir recupera el tiempo real.
 * 3) Ahi arriba siempre es de noche: cada tick se le manda medianoche (despues de la hora real que
 *    el servidor reparte cada segundo, asi nunca llega a verse el dia).
 */
public final class Storm {
    private Storm() {}

    public static final ResourceKey<Biome> BIOME = ResourceKey.create(Registries.BIOME, new ResourceLocation("ciudadela", "tormenta"));
    /** Radio (en bloques) de la zona de tormenta alrededor del centro de la ciudadela. */
    public static final int BIOME_R = 470;
    private static final int ENTER = 500, EXIT = 580;

    private static final Set<UUID> INSIDE = ConcurrentHashMap.newKeySet();

    // ------------------------------------------------------------------ bioma

    /** Pinta el bioma de tormenta en las partes del chunk que caen dentro del radio. */
    public static void paintBiome(WorldGenLevel level, int chunkX, int chunkZ, int sx, int sz) {
        long r2 = (long) BIOME_R * BIOME_R;
        boolean any = false;
        for (int qx = 0; qx < 4 && !any; qx++)
            for (int qz = 0; qz < 4 && !any; qz++) {
                long dx = (chunkX << 4) + qx * 4 + 2 - sx, dz = (chunkZ << 4) + qz * 4 + 2 - sz;
                if (dx * dx + dz * dz <= r2) any = true;
            }
        if (!any) return;
        Holder<Biome> storm = level.registryAccess().registryOrThrow(Registries.BIOME).getHolder(BIOME).map(h -> (Holder<Biome>) h).orElse(null);
        if (storm == null) return;
        ChunkAccess chunk = level.getChunk(chunkX, chunkZ);
        chunk.fillBiomesFromNoise((x, y, z, sampler) -> {
            long dx = QuartPos.toBlock(x) + 2 - sx, dz = QuartPos.toBlock(z) + 2 - sz;
            if (dx * dx + dz * dz <= r2) return storm;
            return chunk.getNoiseBiome(x, y, z);
        }, null);
        chunk.setUnsaved(true);
    }

    // ------------------------------------------------------------------ tiempo por jugador

    /** Se llama una vez por segundo para cada jugador. */
    public static void tick(ServerPlayer p) {
        ServerLevel lvl = p.serverLevel();
        UUID id = p.getUUID();
        boolean was = INSIDE.contains(id);
        boolean inside = false;
        int[] site = null;
        if (lvl.dimension() == Level.OVERWORLD) {
            site = CastleSites.nearest(lvl.getSeed(), (int) p.getX(), (int) p.getZ(), 1);
            if (site != null) {
                double d = Math.hypot(site[0] - p.getX(), site[1] - p.getZ());
                inside = was ? d < EXIT : d < ENTER;
            }
        }
        if (inside) {
            if (!was) INSIDE.add(id);
            if (!was || p.tickCount % 40 == 0) p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0));
            p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 1.0F));
            float thunder = 0.6F + 0.4F * (float) Math.sin(lvl.getGameTime() / 1200.0);
            p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, thunder));
            if (lvl.random.nextInt(14) == 0) lightning(lvl, p);
        } else if (was) {
            INSIDE.remove(id);
            restore(p, lvl);
        }
    }

    /** Medianoche. */
    private static final long NIGHT = 18000L;

    /** Cada tick del servidor (al final): los que estan en la isla ven siempre la noche. */
    public static void night(MinecraftServer server) {
        if (INSIDE.isEmpty()) return;
        for (UUID id : INSIDE) {
            ServerPlayer p = server.getPlayerList().getPlayer(id);
            if (p == null || p.serverLevel().dimension() != Level.OVERWORLD) continue;
            p.connection.send(new ClientboundSetTimePacket(p.serverLevel().getGameTime(), NIGHT, false));
        }
    }

    public static boolean inside(ServerPlayer p) {
        return INSIDE.contains(p.getUUID());
    }

    public static void forget(UUID id) {
        INSIDE.remove(id);
    }

    private static void restore(ServerPlayer p, ServerLevel lvl) {
        p.connection.send(new ClientboundSetTimePacket(lvl.getGameTime(), lvl.getDayTime(), lvl.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)));
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

    /** Relampago solo visual; suele caer en lo mas alto (torres, agujas) cerca del jugador. */
    private static void lightning(ServerLevel lvl, ServerPlayer p) {
        BlockPos best = null;
        for (int i = 0; i < 6; i++) {
            double a = lvl.random.nextDouble() * Math.PI * 2;
            double dist = 30 + lvl.random.nextDouble() * 110;
            int x = (int) Math.floor(p.getX() + Math.cos(a) * dist), z = (int) Math.floor(p.getZ() + Math.sin(a) * dist);
            if (!lvl.hasChunk(x >> 4, z >> 4)) continue;
            int y = lvl.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            if (best == null || y > best.getY()) best = new BlockPos(x, y, z);
        }
        if (best == null) return;
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(lvl);
        if (bolt == null) return;
        bolt.moveTo(Vec3.atBottomCenterOf(best));
        bolt.setVisualOnly(true);
        lvl.addFreshEntity(bolt);
    }
}
