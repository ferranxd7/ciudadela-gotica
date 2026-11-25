package com.darkcraft.world;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.pueblo.gen.PuebloPlan;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * La isla de Brumavela (parte 3b): se ejecuta una vez por chunk del mundo de nubes y levanta la parte del pueblo que cae
 * en el (el plano es el mismo en todos los mundos: {@link PuebloPlan}), y pinta su bioma (la hierba apagada y la bruma).
 */
public class PuebloFeature extends Feature<NoneFeatureConfiguration> {
    public static final ResourceKey<Biome> BIOMA = ResourceKey.create(Registries.BIOME, new ResourceLocation(DarkCraftMod.MODID, "brumavela"));

    public PuebloFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    /** Si (x, z) cae en Brumavela: la isla, la cala y el espigon. */
    public static boolean dentro(double x, double z) {
        return x * x + z * z <= 150.0 * 150.0 || x * x + (z - 128) * (z - 128) <= 66.0 * 66.0;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        if (!(ctx.chunkGenerator() instanceof NubesGenerador)) return false;
        WorldGenLevel level = ctx.level();
        BlockPos o = ctx.origin();
        int cx = o.getX() >> 4, cz = o.getZ() >> 4;
        if (Math.abs(cx) > 14 || Math.abs(cz) > 14) return false;
        try {
            bioma(level, cx, cz);
        } catch (Throwable t) {
            DarkCraftMod.LOGGER.warn("No se pudo poner el bioma de Brumavela en {},{}: {}", cx, cz, t.toString());
        }
        PuebloPlan plan = PuebloPlan.get();
        if (!plan.toca(cx, cz)) return false;
        plan.construir(level, cx, cz, ctx.random());
        return true;
    }

    private static void bioma(WorldGenLevel level, int cx, int cz) {
        boolean alguno = false;
        for (int qx = 0; qx < 4 && !alguno; qx++)
            for (int qz = 0; qz < 4 && !alguno; qz++) alguno = dentro((cx << 4) + qx * 4 + 2, (cz << 4) + qz * 4 + 2);
        if (!alguno) return;
        Holder<Biome> b = level.registryAccess().registryOrThrow(Registries.BIOME).getHolder(BIOMA).map(h -> (Holder<Biome>) h).orElse(null);
        if (b == null) return;
        ChunkAccess chunk = level.getChunk(cx, cz);
        chunk.fillBiomesFromNoise((x, y, z, sampler) -> dentro(QuartPos.toBlock(x) + 2, QuartPos.toBlock(z) + 2) ? b : chunk.getNoiseBiome(x, y, z), null);
        chunk.setUnsaved(true);
    }
}
