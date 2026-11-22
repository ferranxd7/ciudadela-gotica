package com.darkcraft.world;

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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

    public static final ResourceKey<Biome> BIOME = ResourceKey.create(Registries.BIOME, new ResourceLocation("darkcraft", "tormenta"));
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
            if (!was) {
                INSIDE.add(id);
                com.darkcraft.net.Net.to(p, new com.darkcraft.net.Net.Isla(true));
            }
            // logro: pisar la isla
            if (site != null && p.onGround() && p.getY() > com.darkcraft.gen.CastlePlan.Y0 - 45
                    && Math.hypot(site[0] - p.getX(), site[1] - p.getZ()) < com.darkcraft.gen.CastlePlan.FOOTPRINT + 10)
                com.darkcraft.story.Logros.dar(p, com.darkcraft.story.Logros.ENCONTRAR);
            if (com.darkcraft.cine.Cinematics.active(p)) return;
            if (!was || p.tickCount % 40 == 0) p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0));
            p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 1.0F));
            float thunder = 0.6F + 0.4F * (float) Math.sin(lvl.getGameTime() / 1200.0);
            p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, thunder));
            trueno(lvl, p);
        } else if (was) {
            INSIDE.remove(id);
            com.darkcraft.net.Net.to(p, new com.darkcraft.net.Net.Isla(false));
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
        PROXIMO.remove(id);
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

    /** Cuando le toca el siguiente trueno a cada jugador (en segundos de juego). */
    private static final java.util.Map<UUID, Long> PROXIMO = new ConcurrentHashMap<>();

    /**
     * Sin rayos ni destellos: solo se oye el trueno, lejano, cada 15-40 segundos. El sonido se manda solo a ese
     * jugador y el cliente lo pone grave y lejos (ClientIsla).
     */
    private static void trueno(ServerLevel lvl, ServerPlayer p) {
        long ahora = lvl.getGameTime() / 20;
        Long t = PROXIMO.get(p.getUUID());
        if (t == null) {
            PROXIMO.put(p.getUUID(), ahora + 6 + lvl.random.nextInt(20));
            return;
        }
        if (ahora < t) return;
        PROXIMO.put(p.getUUID(), ahora + 15 + lvl.random.nextInt(26));
        p.playNotifySound(SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 0.9f, 0.6f + lvl.random.nextFloat() * 0.25f);
    }
}
