package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * El generador de Morvain (parte 7a): el terreno sale de {@link Terreno} (el mapa disenado, igual para todos y sin semilla)
 * y el suelo de cada region de {@link Suelo}; lo que va encima (arboles muertos, rocas, huesos...) lo pone {@link Adorno}.
 * Escribe directamente en las secciones del chunk (como el generador del juego): Distant Horizons genera con el miles de
 * chunks lejanos y tiene que ir rapido. Sin estado: lo pueden llamar varios hilos a la vez.
 */
public class MorvainGenerador extends ChunkGenerator {
    public static final Codec<MorvainGenerador> CODEC = RecordCodecBuilder.create(i -> i.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)).apply(i, i.stable(MorvainGenerador::new)));

    public MorvainGenerador(BiomeSource b) {
        super(b);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    /** Para escribir bloques en el chunk que se esta generando (lo que cae fuera del chunk no se escribe). */
    interface Escritor {
        void poner(int x, int y, int z, BlockState st);
    }

    /** Las alturas de un chunk y su borde (18 x 18, para las pendientes) y las columnas del chunk. */
    static final class Trozo {
        final double[][] h = new double[18][18];
        final Terreno.Columna[][] c = new Terreno.Columna[16][16];

        Trozo(int x0, int z0) {
            for (int dz = -1; dz <= 16; dz++)
                for (int dx = -1; dx <= 16; dx++) {
                    Terreno.Columna col = Terreno.columna(x0 + dx, z0 + dz);
                    h[dz + 1][dx + 1] = col.altura();
                    if (dx >= 0 && dx < 16 && dz >= 0 && dz < 16) c[dz][dx] = col;
                }
        }

        /** La pendiente (cuanto sube por bloque hacia el vecino mas alto o mas bajo). */
        double pendiente(int dx, int dz) {
            double a = h[dz + 1][dx + 1];
            double m = Math.max(Math.max(Math.abs(h[dz + 1][dx + 2] - a), Math.abs(h[dz + 1][dx] - a)),
                    Math.max(Math.abs(h[dz + 2][dx + 1] - a), Math.abs(h[dz][dx + 1] - a)));
            return m;
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor e, Blender b, RandomState rs, StructureManager sm, ChunkAccess c) {
        int x0 = c.getPos().getMinBlockX(), z0 = c.getPos().getMinBlockZ();
        Trozo t = new Trozo(x0, z0);
        int min = c.getMinBuildHeight(), max = c.getMaxBuildHeight() - 1;
        Heightmap fondo = c.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap cima = c.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        Set<LevelChunkSection> usadas = new HashSet<>();
        try {
            for (LevelChunkSection s : c.getSections()) {
                s.acquire();
                usadas.add(s);
            }
            BlockState agua = Blocks.WATER.defaultBlockState();
            int[] tops = new int[256];
            for (int dz = 0; dz < 16; dz++)
                for (int dx = 0; dx < 16; dx++) {
                    Terreno.Columna col = t.c[dz][dx];
                    int x = x0 + dx, z = z0 + dz;
                    int top = Math.max(min + 1, Math.min(max - 1, (int) Math.floor(col.altura())));
                    tops[dz * 16 + dx] = top;
                    double pend = t.pendiente(dx, dz);
                    Suelo.Capas capas = Suelo.capas(col, x, z, top, pend);
                    for (int y = min; y <= top; y++) {
                        BlockState st = y == min ? Blocks.BEDROCK.defaultBlockState() : capas.en(y);
                        if (st.isAir()) continue;
                        poner(c, dx, y, dz, st);
                        fondo.update(dx, y, dz, st);
                        cima.update(dx, y, dz, st);
                    }
                    if (top < Terreno.AGUA)
                        for (int y = top + 1; y <= Terreno.AGUA; y++) {
                            poner(c, dx, y, dz, agua);
                            cima.update(dx, y, dz, agua);
                        }
                }
            // lo construido: el Arco del Umbral (donde se llega) y la ciudadela encadenada con sus cadenas
            boolean llegada = Llegada.toca(x0, z0), ciudadela = Ciudadela.toca(x0, z0), santuario = SantuariosMorvain.toca(x0, z0);
            if (llegada || ciudadela || santuario) {
                Escritor es = (x, y, z, st) -> {
                    int dx = x - x0, dz = z - z0;
                    if (dx < 0 || dx > 15 || dz < 0 || dz > 15 || y <= min || y > max) return;
                    poner(c, dx, y, dz, st);
                    fondo.update(dx, y, dz, st);
                    cima.update(dx, y, dz, st);
                };
                if (llegada) Llegada.chunk(es, x0, z0, tops);
                if (santuario) SantuariosMorvain.chunk(es, x0, z0, tops);
                if (ciudadela) Ciudadela.chunk(es, x0, z0);
            }
        } finally {
            for (LevelChunkSection s : usadas) s.release();
        }
        return CompletableFuture.completedFuture(c);
    }

    private static void poner(ChunkAccess c, int dx, int y, int dz, BlockState st) {
        LevelChunkSection s = c.getSection(c.getSectionIndex(y));
        s.setBlockState(dx, y & 15, dz, st, false);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel nivel, ChunkAccess c, StructureManager sm) {
        super.applyBiomeDecoration(nivel, c, sm);
        try {
            Adorno.chunk(nivel, c);
        } catch (Throwable t) {
            DarkCraftMod.LOGGER.error("Morvain: no se pudo adornar el chunk {}", c.getPos(), t);
        }
        // Velmor, el pueblo marchito de las llanuras (parte 7e): despues de la hierba, encima
        try {
            if (Velmor.toca(c.getPos().x, c.getPos().z)) Velmor.chunk(nivel, c);
        } catch (Throwable t) {
            DarkCraftMod.LOGGER.error("Morvain: no se pudo levantar Velmor en el chunk {}", c.getPos(), t);
        }
        // el molino de la Molinera, al este de Velmor (parte 7f)
        try {
            if (Molino.toca(c.getPos().x, c.getPos().z)) Molino.chunk(nivel, c);
        } catch (Throwable t) {
            DarkCraftMod.LOGGER.error("Morvain: no se pudo levantar el molino en el chunk {}", c.getPos(), t);
        }
        // la Arboleda de los Ahorcados, en el Bosque Muerto (parte 7g)
        try {
            if (Arboleda.toca(c.getPos().x, c.getPos().z)) Arboleda.chunk(nivel, c);
        } catch (Throwable t) {
            DarkCraftMod.LOGGER.error("Morvain: no se pudo levantar la arboleda en el chunk {}", c.getPos(), t);
        }
        // la Aldea de los Pilotes, en el Pantano Podrido (parte 7i)
        try {
            if (Pilotes.toca(c.getPos().x, c.getPos().z)) Pilotes.chunk(nivel, c);
            // la Laguna Negra, donde espera el Barquero (parte 7j)
            if (LagunaNegra.toca(c.getPos().x, c.getPos().z)) LagunaNegra.chunk(nivel, c);
        } catch (Throwable t) {
            DarkCraftMod.LOGGER.error("Morvain: no se pudo levantar la aldea de los pilotes en el chunk {}", c.getPos(), t);
        }
        // el Monasterio de la Helada, en las Montanas Nevadas (parte 7k)
        try {
            if (Monasterio.toca(c.getPos().x, c.getPos().z)) Monasterio.chunk(nivel, c);
        } catch (Throwable t) {
            DarkCraftMod.LOGGER.error("Morvain: no se pudo levantar el monasterio en el chunk {}", c.getPos(), t);
        }
        // la escalera de los penitentes y la ermita de la cumbre, donde reza el Abad (parte 7l): despues del monasterio,
        // que la escalera sale por el postigo de su huerto
        try {
            if (Ermita.toca(c.getPos().x, c.getPos().z)) Ermita.chunk(nivel, c);
        } catch (Throwable t) {
            DarkCraftMod.LOGGER.error("Morvain: no se pudo levantar la ermita en el chunk {}", c.getPos(), t);
        }
        // la Puerta Oeste de la Capital y el barrio quemado de detras (parte 7m)
        try {
            if (Capital.toca(c.getPos().x, c.getPos().z)) Capital.chunk(nivel, c);
        } catch (Throwable t) {
            DarkCraftMod.LOGGER.error("Morvain: no se pudo levantar la puerta de la capital en el chunk {}", c.getPos(), t);
        }
        // el Patio del Juramento, al este de la plaza de la capital, donde espera el Condestable (parte 7n): despues de la
        // capital, que su suelo llega hasta la calle de ella
        try {
            if (Juramento.toca(c.getPos().x, c.getPos().z)) Juramento.chunk(nivel, c);
        } catch (Throwable t) {
            DarkCraftMod.LOGGER.error("Morvain: no se pudo levantar el patio del Juramento en el chunk {}", c.getPos(), t);
        }
        // la Academia Espectral, en la meseta del noreste, a la puerta de su santuario (parte 7o)
        try {
            if (Academia.toca(c.getPos().x, c.getPos().z)) Academia.chunk(nivel, c);
        } catch (Throwable t) {
            DarkCraftMod.LOGGER.error("Morvain: no se pudo levantar la academia en el chunk {}", c.getPos(), t);
        }
        // el Observatorio, al este de la academia, a la puerta de su santuario, donde espera la Rectora (parte 7p)
        try {
            if (Observatorio.toca(c.getPos().x, c.getPos().z)) Observatorio.chunk(nivel, c);
        } catch (Throwable t) {
            DarkCraftMod.LOGGER.error("Morvain: no se pudo levantar el observatorio en el chunk {}", c.getPos(), t);
        }
        // el Puerto Varado, en la Costa de los Huesos, a la puerta del santuario del Faro de los Huesos (parte 7q)
        try {
            if (Varadero.toca(c.getPos().x, c.getPos().z)) Varadero.chunk(nivel, c);
        } catch (Throwable t) {
            DarkCraftMod.LOGGER.error("Morvain: no se pudo levantar el puerto varado en el chunk {}", c.getPos(), t);
        }
        // el Costillar de la Ballena, al norte de la Costa de los Huesos, a la puerta de su santuario, donde espera Brann (parte 7r)
        try {
            if (Costillar.toca(c.getPos().x, c.getPos().z)) Costillar.chunk(nivel, c);
        } catch (Throwable t) {
            DarkCraftMod.LOGGER.error("Morvain: no se pudo levantar el costillar en el chunk {}", c.getPos(), t);
        }
        // el Puente de la Grieta, en la Tierra sin Fe, a la puerta del santuario de la Grieta (parte 7s)
        try {
            if (Grieta.toca(c.getPos().x, c.getPos().z)) Grieta.chunk(nivel, c);
        } catch (Throwable t) {
            DarkCraftMod.LOGGER.error("Morvain: no se pudo levantar la grieta en el chunk {}", c.getPos(), t);
        }
    }

    @Override
    public void applyCarvers(WorldGenRegion r, long seed, RandomState rs, BiomeManager bm, StructureManager sm, ChunkAccess c,
                             GenerationStep.Carving paso) {
    }

    @Override
    public void buildSurface(WorldGenRegion r, StructureManager sm, RandomState rs, ChunkAccess c) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion r) {
    }

    @Override
    public int getGenDepth() {
        return Morvain.ALTO;
    }

    @Override
    public int getSeaLevel() {
        return Terreno.AGUA;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types tipo, LevelHeightAccessor l, RandomState rs) {
        int top = (int) Math.floor(Terreno.columna(x, z).altura());
        if (tipo == Heightmap.Types.WORLD_SURFACE_WG || tipo == Heightmap.Types.WORLD_SURFACE || tipo == Heightmap.Types.MOTION_BLOCKING)
            top = Math.max(top, Terreno.AGUA);
        return top + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor l, RandomState rs) {
        int min = l.getMinBuildHeight(), h = l.getHeight();
        int top = (int) Math.floor(Terreno.columna(x, z).altura());
        BlockState[] col = new BlockState[h];
        BlockState piedra = Blocks.STONE.defaultBlockState(), agua = Blocks.WATER.defaultBlockState(), aire = Blocks.AIR.defaultBlockState();
        for (int i = 0; i < h; i++) {
            int y = min + i;
            col[i] = y <= top ? piedra : y <= Terreno.AGUA ? agua : aire;
        }
        return new NoiseColumn(min, col);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState rs, BlockPos p) {
        Terreno.Columna c = Terreno.columna(p.getX(), p.getZ());
        info.add(String.format("Morvain: %s, suelo %.1f%s", c.region().id, c.altura(), c.rio() < 40 ? ", junto al rio" : ""));
    }
}
