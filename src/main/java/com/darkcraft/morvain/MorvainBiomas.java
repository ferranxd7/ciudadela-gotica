package com.darkcraft.morvain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.stream.Stream;

/**
 * Los biomas de Morvain: uno por region ({@code darkcraft:morvain_<region>}), sacados del mapa ({@link Terreno#region}):
 * el cielo, la niebla, el color del agua (roja en la Tierra sin Fe y el crater), la hierba y lo que flota en el aire.
 */
public class MorvainBiomas extends BiomeSource {
    public static ResourceKey<Biome> clave(Region r) {
        return ResourceKey.create(Registries.BIOME, new ResourceLocation("darkcraft", "morvain_" + r.id));
    }

    public static final Codec<MorvainBiomas> CODEC = RecordCodecBuilder.create(i -> i.group(
            RegistryOps.retrieveElement(clave(Region.MAR)), RegistryOps.retrieveElement(clave(Region.LLANURAS)),
            RegistryOps.retrieveElement(clave(Region.BOSQUE)), RegistryOps.retrieveElement(clave(Region.PANTANO)),
            RegistryOps.retrieveElement(clave(Region.MONTANAS)), RegistryOps.retrieveElement(clave(Region.CAPITAL)),
            RegistryOps.retrieveElement(clave(Region.ACADEMIA)), RegistryOps.retrieveElement(clave(Region.COSTA)),
            RegistryOps.retrieveElement(clave(Region.SIN_FE)), RegistryOps.retrieveElement(clave(Region.CENTRO))
    ).apply(i, i.stable(MorvainBiomas::new)));

    private final Holder<Biome>[] biomas;

    @SuppressWarnings("unchecked")
    public MorvainBiomas(Holder<Biome> mar, Holder<Biome> llanuras, Holder<Biome> bosque, Holder<Biome> pantano, Holder<Biome> montanas,
                         Holder<Biome> capital, Holder<Biome> academia, Holder<Biome> costa, Holder<Biome> sinFe, Holder<Biome> centro) {
        biomas = new Holder[]{mar, llanuras, bosque, pantano, montanas, capital, academia, costa, sinFe, centro};
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(biomas);
    }

    /**
     * La region no cambia con la altura y el juego pide el bioma de cada cuarto de chunk 192 veces (una por cada cuatro
     * de alto): cada hilo se guarda las ultimas columnas (64, por su sitio).
     */
    private static final ThreadLocal<long[]> CLAVES = ThreadLocal.withInitial(() -> {
        long[] k = new long[64];
        java.util.Arrays.fill(k, Long.MIN_VALUE);
        return k;
    });
    private static final ThreadLocal<byte[]> REGIONES = ThreadLocal.withInitial(() -> new byte[64]);

    @Override
    public Holder<Biome> getNoiseBiome(int qx, int qy, int qz, Climate.Sampler s) {
        long clave = ((long) qx << 32) ^ (qz & 0xffffffffL);
        int i = ((qx & 7) << 3) | (qz & 7);
        long[] k = CLAVES.get();
        byte[] r = REGIONES.get();
        if (k[i] != clave) {
            k[i] = clave;
            r[i] = (byte) Terreno.region(qx * 4 + 2, qz * 4 + 2).ordinal();
        }
        return biomas[r[i]];
    }
}
