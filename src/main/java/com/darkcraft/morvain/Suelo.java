package com.darkcraft.morvain;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * El suelo de cada region de Morvain: lo de arriba, lo de debajo y el relleno de cada columna (y las capas de roca de los
 * acantilados), segun la region, la altura, la pendiente y si le cubre el agua (o la sangre).
 */
final class Suelo {
    private Suelo() {}

    static final Ruido P = new Ruido(0x5375656CL);

    private static BlockState b(net.minecraft.world.level.block.Block x) {
        return x.defaultBlockState();
    }

    /** Lo que va en cada altura de una columna. */
    static final class Capas {
        final int top, hondo;
        final BlockState arriba, bajo, relleno;
        /** Acantilado: capas de roca por altura en vez de arriba/bajo. */
        final BlockState[] franjas;

        Capas(int top, BlockState arriba, BlockState bajo, int hondo, BlockState relleno, BlockState[] franjas) {
            this.top = top;
            this.arriba = arriba;
            this.bajo = bajo;
            this.hondo = hondo;
            this.relleno = relleno;
            this.franjas = franjas;
        }

        BlockState en(int y) {
            if (franjas != null && y > top - 9) return franjas[Math.floorMod(y / 3 + (y * 7 / 11), franjas.length)];
            if (y == top) return arriba;
            if (y > top - hondo) return bajo;
            if (y < 24) return b(Blocks.DEEPSLATE);
            return relleno;
        }
    }

    static Capas capas(Terreno.Columna c, int x, int z, int top, double pend) {
        double n = P.fbm(x, z, 40, 2), n2 = P.fbm(x + 300, z - 300, 11, 1);
        boolean bajoAgua = top < Terreno.AGUA;
        BlockState piedra = b(Blocks.STONE);
        switch (c.region()) {
            case LLANURAS: {
                if (bajoAgua) return new Capas(top, n > 0.1 ? b(Blocks.GRAVEL) : n > -0.3 ? b(Blocks.SAND) : b(Blocks.CLAY), b(Blocks.SAND), 3, piedra, null);
                if (c.rio() < 4) return new Capas(top, b(Blocks.GRAVEL), b(Blocks.DIRT), 3, piedra, null);
                if (pend > 1.6) return new Capas(top, b(Blocks.STONE), b(Blocks.STONE), 2, piedra, null);
                BlockState a = n > 0.42 ? b(Blocks.COARSE_DIRT) : n2 > 0.55 ? b(Blocks.GRAVEL) : b(Blocks.GRASS_BLOCK);
                return new Capas(top, a, b(Blocks.DIRT), 4, piedra, null);
            }
            case BOSQUE: {
                if (bajoAgua) return new Capas(top, b(Blocks.MUD), b(Blocks.DIRT), 3, piedra, null);
                if (pend > 1.7) return new Capas(top, b(Blocks.STONE), b(Blocks.TUFF), 2, piedra, null);
                BlockState a = n > 0.25 ? b(Blocks.PODZOL) : n > -0.2 ? b(Blocks.COARSE_DIRT) : n2 > 0.3 ? b(Blocks.MOSS_BLOCK) : b(Blocks.GRASS_BLOCK);
                return new Capas(top, a, b(Blocks.DIRT), 4, piedra, null);
            }
            case PANTANO: {
                if (bajoAgua) return new Capas(top, n > 0.3 ? b(Blocks.CLAY) : b(Blocks.MUD), b(Blocks.MUD), 3, piedra, null);
                BlockState a = n > 0.15 ? b(Blocks.MUD) : n2 > 0.4 ? b(Blocks.MUDDY_MANGROVE_ROOTS) : b(Blocks.GRASS_BLOCK);
                return new Capas(top, a, b(Blocks.MUD), 3, b(Blocks.PACKED_MUD), null);
            }
            case MONTANAS: {
                if (bajoAgua) return new Capas(top, b(Blocks.GRAVEL), b(Blocks.STONE), 2, piedra, null);
                double nieve = 205 + 28 * n;
                if (pend > 1.25) {
                    BlockState[] f = top > nieve + 60
                            ? new BlockState[]{b(Blocks.STONE), b(Blocks.PACKED_ICE), b(Blocks.STONE), b(Blocks.ANDESITE), b(Blocks.SNOW_BLOCK)}
                            : new BlockState[]{b(Blocks.STONE), b(Blocks.ANDESITE), b(Blocks.STONE), b(Blocks.TUFF), b(Blocks.STONE), b(Blocks.DEEPSLATE)};
                    return new Capas(top, f[0], f[0], 2, piedra, f);
                }
                if (top > nieve) return new Capas(top, b(Blocks.SNOW_BLOCK), b(Blocks.SNOW_BLOCK), 2 + (int) (3 * (n2 + 1)), piedra, null);
                if (top > 150) return new Capas(top, n > 0.1 ? b(Blocks.STONE) : b(Blocks.GRAVEL), b(Blocks.STONE), 2, piedra, null);
                return new Capas(top, n > 0.3 ? b(Blocks.COARSE_DIRT) : b(Blocks.GRASS_BLOCK), b(Blocks.DIRT), 3, piedra, null);
            }
            case CAPITAL: {
                if (bajoAgua) return new Capas(top, b(Blocks.GRAVEL), b(Blocks.DIRT), 3, piedra, null);
                if (c.rio() < 4) return new Capas(top, b(Blocks.GRAVEL), b(Blocks.DIRT), 3, piedra, null);
                BlockState a = n > 0.35 ? b(Blocks.COARSE_DIRT) : n2 > 0.6 ? b(Blocks.COBBLESTONE) : b(Blocks.GRASS_BLOCK);
                return new Capas(top, a, b(Blocks.DIRT), 4, piedra, null);
            }
            case ACADEMIA: {
                if (bajoAgua) return new Capas(top, b(Blocks.GRAVEL), b(Blocks.CALCITE), 3, piedra, null);
                if (pend > 1.1)
                    return new Capas(top, b(Blocks.CALCITE), b(Blocks.CALCITE), 2, b(Blocks.CALCITE),
                            new BlockState[]{b(Blocks.CALCITE), b(Blocks.SMOOTH_BASALT), b(Blocks.CALCITE), b(Blocks.TUFF), b(Blocks.CALCITE)});
                BlockState a = n > 0.3 ? b(Blocks.MOSS_BLOCK) : b(Blocks.GRASS_BLOCK);
                return new Capas(top, a, b(Blocks.DIRT), 3, b(Blocks.CALCITE), null);
            }
            case COSTA: {
                if (bajoAgua) return new Capas(top, n > 0.2 ? b(Blocks.GRAVEL) : b(Blocks.SAND), b(Blocks.SAND), 3, piedra, null);
                if (pend > 1.6) return new Capas(top, b(Blocks.SANDSTONE), b(Blocks.SANDSTONE), 3, piedra, null);
                BlockState bajo = n2 > 0.62 ? b(Blocks.BONE_BLOCK) : b(Blocks.SANDSTONE);
                BlockState a = top > 76 && n > 0.2 ? b(Blocks.COARSE_DIRT) : b(Blocks.SAND);
                return new Capas(top, a, b(Blocks.SAND), 3, bajo, null);
            }
            case SIN_FE: {
                if (bajoAgua) return new Capas(top, b(Blocks.NETHERRACK), b(Blocks.RED_TERRACOTTA), 3, b(Blocks.BLACKSTONE), null);
                if (pend > 1.3)
                    return new Capas(top, b(Blocks.RED_TERRACOTTA), b(Blocks.TERRACOTTA), 3, b(Blocks.BLACKSTONE),
                            new BlockState[]{b(Blocks.RED_TERRACOTTA), b(Blocks.TERRACOTTA), b(Blocks.BLACKSTONE), b(Blocks.BROWN_TERRACOTTA),
                                    b(Blocks.RED_TERRACOTTA), b(Blocks.BASALT)});
                BlockState a = n > 0.3 ? b(Blocks.RED_SAND) : n > -0.15 ? b(Blocks.COARSE_DIRT) : n2 > 0.2 ? b(Blocks.SOUL_SOIL) : b(Blocks.BROWN_TERRACOTTA);
                return new Capas(top, a, b(Blocks.TERRACOTTA), 4, b(Blocks.BLACKSTONE), null);
            }
            case CENTRO: {
                if (bajoAgua) return new Capas(top, b(Blocks.NETHERRACK), b(Blocks.BLACKSTONE), 3, b(Blocks.BLACKSTONE), null);
                double rho = Math.hypot(x, z);
                if (rho < 130)
                    return new Capas(top, n2 > 0.3 ? b(Blocks.CRYING_OBSIDIAN) : b(Blocks.OBSIDIAN), b(Blocks.OBSIDIAN), 4, b(Blocks.BLACKSTONE), null);
                if (pend > 1.2)
                    return new Capas(top, b(Blocks.BASALT), b(Blocks.BLACKSTONE), 3, b(Blocks.BLACKSTONE),
                            new BlockState[]{b(Blocks.BASALT), b(Blocks.BLACKSTONE), b(Blocks.POLISHED_BLACKSTONE), b(Blocks.BLACKSTONE)});
                BlockState a = n > 0.25 ? b(Blocks.SOUL_SOIL) : n > -0.2 ? b(Blocks.BLACKSTONE) : b(Blocks.BASALT);
                return new Capas(top, a, b(Blocks.BLACKSTONE), 3, b(Blocks.BLACKSTONE), null);
            }
            default: {
                // el fondo del mar
                return new Capas(top, n > 0.2 ? b(Blocks.GRAVEL) : n > -0.4 ? b(Blocks.SAND) : b(Blocks.CLAY), b(Blocks.SAND), 3, piedra, null);
            }
        }
    }
}
