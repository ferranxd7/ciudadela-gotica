package com.ciudadela.world;

import com.ciudadela.gen.CastlePlan;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Se ejecuta una vez por chunk del Overworld y construye la parte de la ciudadela que cae en el.
 */
public class CastleFeature extends Feature<NoneFeatureConfiguration> {
    public CastleFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos o = ctx.origin();
        int chunkX = o.getX() >> 4, chunkZ = o.getZ() >> 4;
        int bx = chunkX << 4, bz = chunkZ << 4;
        long seed = level.getSeed();
        int reach = Math.max(CastlePlan.FOOTPRINT, Storm.BIOME_R) + 16;
        int rx0 = Math.floorDiv(bx - reach, CastleSites.REGION), rx1 = Math.floorDiv(bx + 15 + reach, CastleSites.REGION);
        int rz0 = Math.floorDiv(bz - reach, CastleSites.REGION), rz1 = Math.floorDiv(bz + 15 + reach, CastleSites.REGION);
        boolean placed = false;
        for (int rx = rx0; rx <= rx1; rx++)
            for (int rz = rz0; rz <= rz1; rz++) {
                int[] s = CastleSites.site(seed, rx, rz);
                if (s == null) continue;
                if (Math.abs(s[0] - (bx + 8)) > reach + 8 || Math.abs(s[1] - (bz + 8)) > reach + 8) continue;
                // microclima: bioma de tormenta en la isla y alrededores
                try {
                    Storm.paintBiome(level, chunkX, chunkZ, s[0], s[1]);
                } catch (Throwable t) {
                    com.ciudadela.CiudadelaMod.LOGGER.warn("No se pudo poner el bioma de tormenta en {},{}: {}", chunkX, chunkZ, t.toString());
                }
                if (Math.abs(s[0] - (bx + 8)) > CastlePlan.FOOTPRINT + 24 || Math.abs(s[1] - (bz + 8)) > CastlePlan.FOOTPRINT + 24) continue;
                ChunkGenerator gen = ctx.chunkGenerator();
                ServerLevel sl = level.getLevel();
                RandomState rs = sl.getChunkSource().randomState();
                CastlePlan plan = PlanCache.get(seed, s[0], s[1],
                        (x, z) -> gen.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, rs));
                if (!plan.touches(chunkX, chunkZ)) continue;
                plan.buildChunk(level, chunkX, chunkZ, ctx.random());
                placed = true;
            }
        return placed;
    }
}
