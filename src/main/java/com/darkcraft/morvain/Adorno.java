package com.darkcraft.morvain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Random;

/**
 * Lo que va encima del suelo de Morvain (parte 7a): arboles muertos (sin hojas: las Tierras Marchitas), troncos caidos,
 * rocas, matojos secos, hierba palida, setas, nenufares en el pantano, costillas de hueso en la costa. Todo dentro del
 * propio chunk (nada pasa al vecino) y siempre igual en el mismo sitio (la semilla es la del chunk).
 */
final class Adorno {
    private Adorno() {}

    private static final BlockState AIRE = Blocks.AIR.defaultBlockState();

    static void chunk(WorldGenLevel nivel, ChunkAccess c) {
        int x0 = c.getPos().getMinBlockX(), z0 = c.getPos().getMinBlockZ();
        // en la plaza del Arco del Umbral, nada
        if (Llegada.plaza(x0, z0) || SantuariosMorvain.libre(x0, z0)) return;
        Random r = new Random(c.getPos().toLong() * 0x9E3779B97F4A7C15L + 0x4D6F72L);
        Region reg = Terreno.region(x0 + 8, z0 + 8);
        // en Velmor (parte 7e) y en el molino (7f) solo la hierba: los arboles y lo demas los pone el pueblo donde no estorban
        if (Velmor.cerca(x0, z0) || Molino.cerca(x0, z0)) {
            hierba(nivel, c, r, 40, Blocks.GRASS.defaultBlockState(), Blocks.DEAD_BUSH.defaultBlockState());
            return;
        }
        // en la Aldea de los Pilotes (parte 7i), nada: la laguna pone sus juncos, sus nenufares y sus mangles
        if (Pilotes.cerca(x0, z0)) return;
        // en la Laguna Negra (parte 7j), tampoco
        if (LagunaNegra.cerca(x0, z0)) return;
        // ni en el Monasterio de la Helada (parte 7k): ni rocas ni abetos en la terraza ni en el camino
        if (Monasterio.cerca(x0, z0)) return;
        // ni en la escalera de los penitentes ni en el calvario de la ermita (parte 7l)
        if (Ermita.cerca(x0, z0)) return;
        // ni en la Puerta Oeste de la Capital (parte 7m): los arboles quemados y los escombros los pone la ciudad
        if (Capital.cerca(x0, z0)) return;
        // ni en el Patio del Juramento (parte 7n)
        if (Juramento.cerca(x0, z0)) return;
        // ni en la Academia Espectral (parte 7o): sus arboles secos, su hierba y sus escombros los pone ella
        if (Academia.cerca(x0, z0)) return;
        // ni en el Puerto Varado (parte 7q): sus matas, sus corales y sus huesos los pone el
        if (Varadero.cerca(x0, z0)) return;
        // ni en el Observatorio (parte 7p)
        if (Observatorio.cerca(x0, z0)) return;
        // ni en el Costillar de la Ballena (parte 7r): sus huesos, su sal y sus matas los pone el
        if (Costillar.cerca(x0, z0)) return;
        // ni en el Puente de la Grieta (parte 7s): sus matas, sus huesos y sus piedras los pone ella
        if (Grieta.cerca(x0, z0)) return;
        // ni en el Altar Profanado (parte 7t): sus matas, sus huesos y sus piedras los pone el
        if (Altar.cerca(x0, z0)) return;
        if (Cerco.cerca(x0, z0)) return;
        // ni en el Teatro Hundido (parte 7y)
        if (Teatro.cerca(x0, z0)) return;
        // ni en las Minas de Hierro Negro (parte 7aq)
        if (Mina.cerca(x0, z0)) return;
        if (Nido.cerca(x0, z0)) return;
        // ni en el Osario de San Ulfo (parte 7as): los arboles muertos de alrededor los pone el
        if (Osario.cerca(x0, z0)) return;
        // en las herrerias (parte 7af) solo la hierba (alrededor de su suelo allanado)
        if (Herrerias.cerca(x0, z0)) {
            hierba(nivel, c, r, 24, Blocks.GRASS.defaultBlockState(), Blocks.DEAD_BUSH.defaultBlockState());
            return;
        }
        // en la Arboleda de los Ahorcados (parte 7g) solo setas y helechos: los arboles son los suyos
        if (Arboleda.cerca(x0, z0)) {
            hierba(nivel, c, r, 20, Blocks.BROWN_MUSHROOM.defaultBlockState(), Blocks.FERN.defaultBlockState());
            return;
        }
        switch (reg) {
            case LLANURAS -> {
                if (r.nextInt(3) == 0) arbol(nivel, c, r, Blocks.OAK_LOG.defaultBlockState(), 4, 4, 1);
                for (int i = r.nextInt(3); i > 0; i--) roca(nivel, c, r, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState());
                hierba(nivel, c, r, 40, Blocks.GRASS.defaultBlockState(), Blocks.DEAD_BUSH.defaultBlockState());
            }
            case BOSQUE -> {
                for (int i = 2 + r.nextInt(4); i > 0; i--)
                    arbol(nivel, c, r, (r.nextBoolean() ? Blocks.DARK_OAK_LOG : Blocks.SPRUCE_LOG).defaultBlockState(), 6, 9, 3);
                if (r.nextInt(3) == 0) caido(nivel, c, r, Blocks.DARK_OAK_LOG.defaultBlockState());
                hierba(nivel, c, r, 26, Blocks.BROWN_MUSHROOM.defaultBlockState(), Blocks.FERN.defaultBlockState());
            }
            case PANTANO -> {
                for (int i = r.nextInt(3); i > 0; i--) arbol(nivel, c, r, Blocks.MANGROVE_LOG.defaultBlockState(), 4, 6, 2);
                nenufares(nivel, c, r, 18);
                hierba(nivel, c, r, 30, Blocks.GRASS.defaultBlockState(), Blocks.DEAD_BUSH.defaultBlockState());
            }
            case MONTANAS -> {
                if (r.nextInt(2) == 0) roca(nivel, c, r, Blocks.STONE.defaultBlockState(), Blocks.ANDESITE.defaultBlockState());
                if (r.nextInt(3) == 0) arbol(nivel, c, r, Blocks.SPRUCE_LOG.defaultBlockState(), 5, 6, 2);
            }
            case CAPITAL, ACADEMIA -> {
                if (r.nextInt(2) == 0) roca(nivel, c, r, Blocks.COBBLESTONE.defaultBlockState(), Blocks.STONE_BRICKS.defaultBlockState());
                hierba(nivel, c, r, 24, Blocks.GRASS.defaultBlockState(), Blocks.DEAD_BUSH.defaultBlockState());
            }
            case COSTA -> {
                if (r.nextInt(7) == 0) costillas(nivel, c, r);
                hierba(nivel, c, r, 14, Blocks.DEAD_BUSH.defaultBlockState(), Blocks.DEAD_BUSH.defaultBlockState());
            }
            case SIN_FE -> {
                if (r.nextInt(3) == 0) arbol(nivel, c, r, Blocks.BASALT.defaultBlockState(), 5, 7, 2);
                hierba(nivel, c, r, 20, Blocks.DEAD_BUSH.defaultBlockState(), Blocks.DEAD_BUSH.defaultBlockState());
                if (r.nextInt(4) == 0) roca(nivel, c, r, Blocks.BONE_BLOCK.defaultBlockState(), Blocks.BLACKSTONE.defaultBlockState());
            }
            default -> {
            }
        }
    }

    /** Lo alto (el primer hueco de aire) de una columna del chunk; -1 si esta bajo el agua. */
    private static int suelo(ChunkAccess c, int dx, int dz) {
        int y = c.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, dx, dz) + 1;
        return y <= Terreno.AGUA ? -1 : y;
    }

    private static boolean tierra(BlockState s) {
        return s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.PODZOL) || s.is(Blocks.MOSS_BLOCK)
                || s.is(Blocks.MUD) || s.is(Blocks.SAND) || s.is(Blocks.RED_SAND) || s.is(Blocks.SOUL_SOIL) || s.is(Blocks.SNOW_BLOCK)
                || s.is(Blocks.STONE) || s.is(Blocks.GRAVEL) || s.is(Blocks.BROWN_TERRACOTTA) || s.is(Blocks.MUDDY_MANGROVE_ROOTS);
    }

    /** Un arbol muerto: tronco de {@code minimo..minimo+extra} y {@code ramas} ramas torcidas, sin hojas. */
    private static void arbol(WorldGenLevel n, ChunkAccess c, Random r, BlockState tronco, int minimo, int extra, int ramas) {
        int dx = 3 + r.nextInt(10), dz = 3 + r.nextInt(10);
        int y = suelo(c, dx, dz);
        if (y < 0) return;
        int x = c.getPos().getMinBlockX() + dx, z = c.getPos().getMinBlockZ() + dz;
        if (!tierra(n.getBlockState(new BlockPos(x, y - 1, z)))) return;
        int alto = minimo + r.nextInt(extra + 1);
        for (int i = 0; i < alto; i++) n.setBlock(new BlockPos(x, y + i, z), tronco, 2);
        Direction[] lados = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (int k = 0; k < ramas; k++) {
            Direction d = lados[r.nextInt(4)];
            int by = y + alto / 2 + r.nextInt(Math.max(1, alto / 2));
            int largo = 1 + r.nextInt(3);
            BlockState rama = tronco.hasProperty(RotatedPillarBlock.AXIS) ? tronco.setValue(RotatedPillarBlock.AXIS, d.getAxis()) : tronco;
            for (int i = 1; i <= largo; i++) {
                BlockPos p = new BlockPos(x + d.getStepX() * i, by + (i == largo ? 1 : 0), z + d.getStepZ() * i);
                if (n.getBlockState(p).isAir()) n.setBlock(p, i == largo ? tronco : rama, 2);
            }
        }
    }

    /** Un tronco tumbado. */
    private static void caido(WorldGenLevel n, ChunkAccess c, Random r, BlockState tronco) {
        int dx = 3 + r.nextInt(6), dz = 3 + r.nextInt(10);
        int y = suelo(c, dx, dz);
        if (y < 0) return;
        int x = c.getPos().getMinBlockX() + dx, z = c.getPos().getMinBlockZ() + dz;
        BlockState t = tronco.hasProperty(RotatedPillarBlock.AXIS) ? tronco.setValue(RotatedPillarBlock.AXIS, Direction.Axis.X) : tronco;
        for (int i = 0; i < 3 + r.nextInt(3); i++) {
            BlockPos p = new BlockPos(x + i, y, z);
            if (n.getBlockState(p).isAir() && !n.getBlockState(p.below()).isAir()) n.setBlock(p, t, 2);
        }
    }

    /** Una roca de 2-3 bloques. */
    private static void roca(WorldGenLevel n, ChunkAccess c, Random r, BlockState a, BlockState b) {
        int dx = 2 + r.nextInt(12), dz = 2 + r.nextInt(12);
        int y = suelo(c, dx, dz);
        if (y < 0) return;
        int x = c.getPos().getMinBlockX() + dx, z = c.getPos().getMinBlockZ() + dz;
        int t = 1 + r.nextInt(2);
        for (int i = -t; i <= t; i++)
            for (int k = -t; k <= t; k++)
                for (int j = -1; j <= t; j++)
                    if (i * i + k * k + j * j * 1.6 <= t * t + 0.5 && r.nextInt(5) > 0) n.setBlock(new BlockPos(x + i, y + j, z + k), r.nextBoolean() ? a : b, 2);
    }

    /** Hierba, matojos o setas sueltos (uno de cada {@code cada} sitios). */
    private static void hierba(WorldGenLevel n, ChunkAccess c, Random r, int veces, BlockState a, BlockState b) {
        for (int i = 0; i < veces; i++) {
            int dx = r.nextInt(16), dz = r.nextInt(16);
            int y = suelo(c, dx, dz);
            if (y < 0) continue;
            BlockPos p = new BlockPos(c.getPos().getMinBlockX() + dx, y, c.getPos().getMinBlockZ() + dz);
            BlockState abajo = n.getBlockState(p.below());
            BlockState s = r.nextInt(3) == 0 ? b : a;
            if (!n.getBlockState(p).isAir()) continue;
            if (s.is(Blocks.DEAD_BUSH) ? (abajo.is(Blocks.SAND) || abajo.is(Blocks.RED_SAND) || abajo.is(Blocks.COARSE_DIRT) || abajo.is(Blocks.DIRT)
                    || abajo.is(Blocks.GRASS_BLOCK) || abajo.is(Blocks.PODZOL) || abajo.is(Blocks.MUD) || abajo.is(Blocks.BROWN_TERRACOTTA)
                    || abajo.is(Blocks.TERRACOTTA)) : s.canSurvive(n, p))
                n.setBlock(p, s, 2);
        }
    }

    /** Nenufares sobre el agua del pantano. */
    private static void nenufares(WorldGenLevel n, ChunkAccess c, Random r, int veces) {
        for (int i = 0; i < veces; i++) {
            int dx = r.nextInt(16), dz = r.nextInt(16);
            BlockPos p = new BlockPos(c.getPos().getMinBlockX() + dx, Terreno.AGUA + 1, c.getPos().getMinBlockZ() + dz);
            if (n.getBlockState(p).isAir() && n.getBlockState(p.below()).is(Blocks.WATER)) n.setBlock(p, Blocks.LILY_PAD.defaultBlockState(), 2);
        }
    }

    /** Las costillas de un gigante medio enterradas en la arena (arcos de hueso). */
    private static void costillas(WorldGenLevel n, ChunkAccess c, Random r) {
        int dx = 4 + r.nextInt(8), dz = 2;
        int y = suelo(c, dx, 8);
        if (y < 0) return;
        int x = c.getPos().getMinBlockX() + dx, z0 = c.getPos().getMinBlockZ() + dz;
        BlockState hueso = Blocks.BONE_BLOCK.defaultBlockState();
        int alto = 6 + r.nextInt(5);
        for (int k = 0; k < 4; k++) {
            int z = z0 + k * 3 + 1;
            for (int t = 0; t <= 16; t++) {
                double a = Math.PI * t / 16;
                int ax = (int) Math.round(Math.cos(a) * 4), ay = (int) Math.round(Math.sin(a) * alto);
                BlockPos p = new BlockPos(x + ax, y - 1 + ay, z);
                if (n.getBlockState(p).isAir() || n.getBlockState(p).is(Blocks.SAND)) n.setBlock(p, hueso, 2);
            }
        }
    }
}
