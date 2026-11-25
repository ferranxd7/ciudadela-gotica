package com.darkcraft.world;

import com.darkcraft.DarkCraftMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * El mundo de nubes (parte 3a): no hay suelo. Un mar de nubes ({@code darkcraft:nube}: translucida y sin choque, quien
 * cae la atraviesa y se pierde en el vacio) de bollos suaves alrededor de Y {@link #MEDIA}; encima flotan las islas
 * (Valdrenn a 380, la Fortaleza de Hueso a 320 y lo que se ira poniendo). Las nubes son iguales en todos los mundos (el
 * ruido no depende de la semilla). Lo que va sobre ellas lo ponen las estructuras y los "features"; el camino de nubes
 * firme de cada reino a su fortaleza lo pone {@link CaminoNubes} al hacer la superficie de cada chunk.
 */
public class NubesGenerador extends ChunkGenerator {
    public static final Codec<NubesGenerador> CODEC = RecordCodecBuilder.create(i -> i.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)).apply(i, i.stable(NubesGenerador::new)));

    /** La altura media de lo alto del mar de nubes (sube y baja unos 15 bloques). */
    public static final int MEDIA = 248;
    /**
     * La altura de los caminos de nube firme (el bloque; se pisa en {@link #SUELO}): de ahi sale la Torre de Ascenso de cada
     * reino y ahi empieza la escalera de hueso de cada fortaleza. Es tambien lo que da {@link #getBaseHeight} ("el suelo").
     */
    public static final int CAMINO = 250, SUELO = CAMINO + 1;
    private static final SimplexNoise GRANDE = new SimplexNoise(new LegacyRandomSource(0x6E756265L));
    private static final SimplexNoise MEDIO = new SimplexNoise(new LegacyRandomSource(0x6E756266L));
    private static final SimplexNoise FINO = new SimplexNoise(new LegacyRandomSource(0x6E756267L));

    public NubesGenerador(BiomeSource b) {
        super(b);
    }

    /** Lo mas alto de la nube en esa columna (233 .. 263). */
    public static int cima(int x, int z) {
        double v = 9.0 * GRANDE.getValue(x / 170.0, z / 170.0) + 4.0 * MEDIO.getValue(x / 48.0, z / 48.0)
                + 1.5 * FINO.getValue(x / 14.0, z / 14.0);
        return MEDIA + (int) Math.round(v);
    }

    /** Lo mas bajo de la nube en esa columna (por debajo, el vacio): de 14 a 21 bajo la cima. */
    public static int fondo(int x, int z) {
        double v = 5.0 * MEDIO.getValue(x / 60.0 + 31.7, z / 60.0 - 12.3) + 2.0 * FINO.getValue(x / 18.0 - 7.1, z / 18.0 + 3.3);
        return cima(x, z) - 14 - Math.min(7, (int) Math.round(Math.abs(v)));
    }

    /** Si el nivel es un mundo de nubes. */
    public static boolean es(net.minecraft.world.level.Level l) {
        return l instanceof net.minecraft.server.level.ServerLevel sl && sl.getChunkSource().getGenerator() instanceof NubesGenerador;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion r, long seed, RandomState rs, BiomeManager bm, StructureManager sm, ChunkAccess c,
                             GenerationStep.Carving paso) {
    }

    /** El camino de nube firme (y lo que se despeja a su alrededor), si pasa por este chunk. */
    @Override
    public void buildSurface(WorldGenRegion r, StructureManager sm, RandomState rs, ChunkAccess c) {
        try {
            CaminoNubes.construir(r.getSeed(), c);
        } catch (Throwable t) {
            DarkCraftMod.LOGGER.error("No se pudo poner el camino de nubes en el chunk {}", c.getPos(), t);
        }
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion r) {
    }

    @Override
    public int getGenDepth() {
        return 768;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor e, Blender b, RandomState rs, StructureManager sm, ChunkAccess c) {
        BlockState nube = DarkCraftMod.NUBE.get().defaultBlockState();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int x0 = c.getPos().getMinBlockX(), z0 = c.getPos().getMinBlockZ();
        for (int dx = 0; dx < 16; dx++)
            for (int dz = 0; dz < 16; dz++) {
                int x = x0 + dx, z = z0 + dz;
                int arriba = cima(x, z), abajo = fondo(x, z);
                // (el ProtoChunk apunta solo las alturas de los mapas de la generacion)
                for (int y = abajo; y <= arriba; y++) c.setBlockState(m.set(x, y, z), nube, false);
            }
        return CompletableFuture.completedFuture(c);
    }

    @Override
    public int getSeaLevel() {
        return -64;
    }

    @Override
    public int getMinY() {
        return -64;
    }

    /** "El suelo" para lo que lo busca (el pie de la Torre de Ascenso, por ejemplo): el de los caminos de nube firme. */
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types t, LevelHeightAccessor l, RandomState rs) {
        return SUELO;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor l, RandomState rs) {
        int min = l.getMinBuildHeight(), h = l.getHeight();
        BlockState[] col = new BlockState[h];
        BlockState aire = Blocks.AIR.defaultBlockState(), nube = DarkCraftMod.NUBE.get().defaultBlockState();
        int arriba = cima(x, z), abajo = fondo(x, z);
        for (int i = 0; i < h; i++) {
            int y = min + i;
            col[i] = y >= abajo && y <= arriba ? nube : aire;
        }
        return new NoiseColumn(min, col);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState rs, BlockPos p) {
        info.add("Dark Craft: mar de nubes " + fondo(p.getX(), p.getZ()) + ".." + cima(p.getX(), p.getZ()));
    }
}
