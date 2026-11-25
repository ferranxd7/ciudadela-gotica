package com.darkcraft.pueblo.gen;

import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Pal;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

/** Los materiales de Brumavela (piedra vieja con musgo, madera gris) y cuatro cosas que se repiten. */
final class Mat {
    private Mat() {}

    /** Muro de piedra del pueblo: canto rodado, sillar y musgo. */
    static BlockState muro(int x, int y, int z, long s) {
        int p = Hash.pct(x, y, z, s + 101);
        if (p < 34) return Pal.b(Blocks.COBBLESTONE);
        if (p < 56) return Pal.STONE_BRICKS;
        if (p < 70) return Pal.b(Blocks.MOSSY_COBBLESTONE);
        if (p < 82) return Pal.MOSSY_STONE_BRICKS;
        if (p < 92) return Pal.ANDESITE;
        return Pal.b(Blocks.CRACKED_STONE_BRICKS);
    }

    /** Sillar mas cuidado (la capilla, el faro, el muelle). */
    static BlockState sillar(int x, int y, int z, long s) {
        int p = Hash.pct(x, y, z, s + 102);
        if (p < 52) return Pal.STONE_BRICKS;
        if (p < 68) return Pal.b(Blocks.CRACKED_STONE_BRICKS);
        if (p < 84) return Pal.MOSSY_STONE_BRICKS;
        if (p < 94) return Pal.b(Blocks.COBBLESTONE);
        return Pal.ANDESITE;
    }

    /** Suelo de losas gastadas. */
    static BlockState losas(int x, int y, int z, long s) {
        int p = Hash.pct(x, y, z, s + 103);
        if (p < 40) return Pal.STONE_BRICKS;
        if (p < 60) return Pal.b(Blocks.POLISHED_ANDESITE);
        if (p < 76) return Pal.b(Blocks.CRACKED_STONE_BRICKS);
        if (p < 90) return Pal.ANDESITE;
        return Pal.b(Blocks.MOSSY_STONE_BRICKS);
    }

    /** Muro encalado (el faro): cal vieja con desconchones. */
    static BlockState cal(int x, int y, int z, long s) {
        int p = Hash.pct(x, y, z, s + 104);
        if (p < 46) return Pal.CALCITE;
        if (p < 70) return Pal.b(Blocks.WHITE_TERRACOTTA);
        if (p < 84) return Pal.b(Blocks.DIORITE);
        if (p < 93) return Pal.b(Blocks.LIGHT_GRAY_TERRACOTTA);
        return Pal.b(Blocks.COBBLESTONE);
    }

    /** Piedra oscura de la cripta. */
    static BlockState cripta(int x, int y, int z, long s) {
        int p = Hash.pct(x, y, z, s + 105);
        if (p < 44) return Pal.DS_BRICKS;
        if (p < 62) return Pal.DS_BRICKS_CRACKED;
        if (p < 80) return Pal.DS_COBBLED;
        if (p < 92) return Pal.DS_TILES;
        return Pal.b(Blocks.MOSSY_COBBLESTONE);
    }

    /** Tablas de cubierta y de muelle: abeto con alguna mas oscura. */
    static BlockState tabla(int x, int y, int z, long s) {
        int p = Hash.pct(x, y, z, s + 106);
        return p < 72 ? Pal.SP_PLANKS : p < 90 ? Pal.DO_PLANKS : Pal.b(Blocks.STRIPPED_SPRUCE_WOOD);
    }

    static BlockState barril(Direction mira) {
        return Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, mira);
    }

    static BlockState escalera(Direction mira) {
        return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, mira);
    }

    static BlockState trampilla(Direction mira, boolean arriba, boolean abierta) {
        return Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, mira).setValue(TrapDoorBlock.HALF, arriba ? Half.TOP : Half.BOTTOM)
                .setValue(TrapDoorBlock.OPEN, abierta);
    }

    static BlockState velas(int n) {
        return Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, Math.max(1, Math.min(4, n))).setValue(CandleBlock.LIT, false);
    }

    static BlockState calavera(int giro) {
        return Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, Math.floorMod(giro, 16));
    }

    /** Un farol de almas colgado de su cadena (de {@code y} hacia abajo, {@code largo} eslabones). */
    static void farolColgado(Canvas c, int x, int y, int z, int largo) {
        for (int k = 0; k < largo; k++) c.set(x, y - k, z, Pal.CHAIN);
        c.set(x, y - largo, z, Pal.SOUL_LANTERN_HANG);
    }

    /** Un poste con su farol de almas encima (o colgado de un brazo si {@code brazo} no es null). */
    static void farola(Canvas c, int x, int y, int z, int alto, Direction brazo) {
        c.set(x, y, z, Pal.b(Blocks.COBBLESTONE_WALL));
        for (int k = 1; k < alto; k++) c.set(x, y + k, z, Pal.SP_FENCE);
        if (brazo == null) {
            c.set(x, y + alto, z, Pal.SOUL_LANTERN);
            return;
        }
        int bx = x + brazo.getStepX(), bz = z + brazo.getStepZ();
        c.unir(x, y + alto - 1, z, Pal.SP_FENCE);
        c.unir(bx, y + alto - 1, bz, Pal.SP_FENCE);
        c.set(bx, y + alto - 2, bz, Pal.SOUL_LANTERN_HANG);
    }
}
