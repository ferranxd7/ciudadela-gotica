package com.ciudadela.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

/**
 * Paleta de materiales de la ciudadela: madera oscura y piedras oscuras, con variacion por posicion.
 */
public final class Pal {
    private Pal() {}

    public static BlockState b(Block block) {
        return block.defaultBlockState();
    }

    public static final BlockState AIR = b(Blocks.AIR);

    // --- Piedra oscura ---
    public static final BlockState DS_BRICKS = b(Blocks.DEEPSLATE_BRICKS);
    public static final BlockState DS_BRICKS_CRACKED = b(Blocks.CRACKED_DEEPSLATE_BRICKS);
    public static final BlockState DS_TILES = b(Blocks.DEEPSLATE_TILES);
    public static final BlockState DS_TILES_CRACKED = b(Blocks.CRACKED_DEEPSLATE_TILES);
    public static final BlockState DS_CHISELED = b(Blocks.CHISELED_DEEPSLATE);
    public static final BlockState DS_POLISHED = b(Blocks.POLISHED_DEEPSLATE);
    public static final BlockState DS_COBBLED = b(Blocks.COBBLED_DEEPSLATE);
    public static final BlockState DEEPSLATE = b(Blocks.DEEPSLATE);
    public static final BlockState BLACKSTONE = b(Blocks.BLACKSTONE);
    public static final BlockState PB = b(Blocks.POLISHED_BLACKSTONE);
    public static final BlockState PB_BRICKS = b(Blocks.POLISHED_BLACKSTONE_BRICKS);
    public static final BlockState PB_BRICKS_CRACKED = b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
    public static final BlockState PB_CHISELED = b(Blocks.CHISELED_POLISHED_BLACKSTONE);
    public static final BlockState GILDED = b(Blocks.GILDED_BLACKSTONE);
    public static final BlockState BASALT = b(Blocks.POLISHED_BASALT);
    public static final BlockState SMOOTH_BASALT = b(Blocks.SMOOTH_BASALT);
    public static final BlockState TUFF = b(Blocks.TUFF);
    public static final BlockState STONE = b(Blocks.STONE);
    public static final BlockState ANDESITE = b(Blocks.ANDESITE);
    public static final BlockState STONE_BRICKS = b(Blocks.STONE_BRICKS);
    public static final BlockState MOSSY_STONE_BRICKS = b(Blocks.MOSSY_STONE_BRICKS);
    public static final BlockState NETHER_BRICKS = b(Blocks.NETHER_BRICKS);
    public static final BlockState RED_NETHER_BRICKS = b(Blocks.RED_NETHER_BRICKS);
    public static final BlockState OBSIDIAN = b(Blocks.OBSIDIAN);
    public static final BlockState CRYING_OBSIDIAN = b(Blocks.CRYING_OBSIDIAN);
    public static final BlockState CALCITE = b(Blocks.CALCITE);

    // --- Madera oscura ---
    public static final BlockState DO_PLANKS = b(Blocks.DARK_OAK_PLANKS);
    public static final BlockState SP_PLANKS = b(Blocks.SPRUCE_PLANKS);
    public static final BlockState CR_PLANKS = b(Blocks.CRIMSON_PLANKS);
    public static final BlockState MG_PLANKS = b(Blocks.MANGROVE_PLANKS);
    public static final BlockState DO_LOG = b(Blocks.DARK_OAK_LOG);
    public static final BlockState DO_STRIPPED = b(Blocks.STRIPPED_DARK_OAK_LOG);
    public static final BlockState SP_LOG = b(Blocks.SPRUCE_LOG);
    public static final BlockState SP_STRIPPED = b(Blocks.STRIPPED_SPRUCE_LOG);
    public static final BlockState DO_WOOD = b(Blocks.DARK_OAK_WOOD);
    public static final BlockState DO_FENCE = b(Blocks.DARK_OAK_FENCE);
    public static final BlockState SP_FENCE = b(Blocks.SPRUCE_FENCE);
    public static final BlockState BOOKSHELF = b(Blocks.BOOKSHELF);

    // --- Terreno ---
    public static final BlockState GRASS_BLOCK = b(Blocks.GRASS_BLOCK);
    public static final BlockState DIRT = b(Blocks.DIRT);
    public static final BlockState COARSE_DIRT = b(Blocks.COARSE_DIRT);
    public static final BlockState PODZOL = b(Blocks.PODZOL);
    public static final BlockState ROOTED_DIRT = b(Blocks.ROOTED_DIRT);
    public static final BlockState MOSS = b(Blocks.MOSS_BLOCK);
    public static final BlockState GRAVEL = b(Blocks.GRAVEL);
    public static final BlockState SOUL_SOIL = b(Blocks.SOUL_SOIL);
    public static final BlockState WATER = b(Blocks.WATER);

    // --- Luz y detalles ---
    public static final BlockState LANTERN = b(Blocks.LANTERN);
    public static final BlockState LANTERN_HANG = b(Blocks.LANTERN).setValue(LanternBlock.HANGING, true);
    public static final BlockState SOUL_LANTERN = b(Blocks.SOUL_LANTERN);
    public static final BlockState SOUL_LANTERN_HANG = b(Blocks.SOUL_LANTERN).setValue(LanternBlock.HANGING, true);
    public static final BlockState CHAIN = b(Blocks.CHAIN);
    public static final BlockState COBWEB = b(Blocks.COBWEB);
    public static final BlockState CAMPFIRE = b(Blocks.CAMPFIRE);
    public static final BlockState SOUL_CAMPFIRE = b(Blocks.SOUL_CAMPFIRE);
    public static final BlockState IRON_BARS = b(Blocks.IRON_BARS);

    // --- Vidrieras ---
    public static final BlockState GLASS_GRAY = b(Blocks.GRAY_STAINED_GLASS);
    public static final BlockState GLASS_BLACK = b(Blocks.BLACK_STAINED_GLASS);
    public static final BlockState GLASS_RED = b(Blocks.RED_STAINED_GLASS);
    public static final BlockState GLASS_PURPLE = b(Blocks.PURPLE_STAINED_GLASS);
    public static final BlockState GLASS_BLUE = b(Blocks.BLUE_STAINED_GLASS);
    public static final BlockState GLASS_YELLOW = b(Blocks.YELLOW_STAINED_GLASS);
    public static final BlockState GLASS_ORANGE = b(Blocks.ORANGE_STAINED_GLASS);
    public static final BlockState GLASS_TINTED = b(Blocks.TINTED_GLASS);

    // ---------- Variaciones por posicion ----------

    /** Muro principal: ladrillo de pizarra profunda con mezcla de piedra negra. */
    public static BlockState wall(int x, int y, int z) {
        int p = Hash.pct(x, y, z, 11);
        if (p < 46) return DS_BRICKS;
        if (p < 58) return DS_BRICKS_CRACKED;
        if (p < 72) return DS_TILES;
        if (p < 88) return PB_BRICKS;
        if (p < 94) return PB_BRICKS_CRACKED;
        if (p < 98) return DS_COBBLED;
        return DS_TILES_CRACKED;
    }

    /** Muro viejo y musgoso (ruinas, islotes). */
    public static BlockState oldWall(int x, int y, int z) {
        int p = Hash.pct(x, y, z, 12);
        if (p < 35) return DS_BRICKS_CRACKED;
        if (p < 55) return DS_COBBLED;
        if (p < 70) return MOSSY_STONE_BRICKS;
        if (p < 85) return DS_BRICKS;
        return b(Blocks.MOSSY_COBBLESTONE);
    }

    public static BlockState foundation(int x, int y, int z) {
        int p = Hash.pct(x, y, z, 13);
        if (p < 50) return DS_COBBLED;
        if (p < 75) return BLACKSTONE;
        if (p < 90) return DEEPSLATE;
        return DS_BRICKS_CRACKED;
    }

    public static BlockState trim(int x, int y, int z) {
        return Hash.pct(x, y, z, 14) < 70 ? PB : DS_POLISHED;
    }

    public static BlockState roof(int x, int y, int z) {
        int p = Hash.pct(x, y, z, 15);
        if (p < 75) return DS_TILES;
        if (p < 90) return DS_TILES_CRACKED;
        return PB_BRICKS;
    }

    public static BlockState woodFloor(int x, int y, int z) {
        int p = Hash.pct(x, y, z, 16);
        if (p < 70) return DO_PLANKS;
        if (p < 92) return SP_PLANKS;
        return MG_PLANKS;
    }

    /** Relleno de entramado de madera (pisos altos). */
    public static BlockState timberInfill(int x, int y, int z) {
        int p = Hash.pct(x, y, z, 17);
        if (p < 55) return SP_PLANKS;
        if (p < 85) return DO_PLANKS;
        return b(Blocks.STRIPPED_SPRUCE_WOOD);
    }

    public static BlockState islandStone(int x, int y, int z, int depthFromTop) {
        int p = Hash.pct(x, y, z, 18);
        if (depthFromTop > 25) {
            if (p < 55) return DEEPSLATE;
            if (p < 75) return TUFF;
            if (p < 90) return DS_COBBLED;
            return BLACKSTONE;
        }
        if (p < 45) return STONE;
        if (p < 65) return ANDESITE;
        if (p < 80) return TUFF;
        if (p < 92) return DEEPSLATE;
        return b(Blocks.COBBLESTONE);
    }

    public static BlockState glass(int x, int y, int z) {
        int p = Hash.pct(x, y, z, 19);
        if (p < 40) return GLASS_GRAY;
        if (p < 70) return GLASS_BLACK;
        if (p < 88) return GLASS_RED;
        return GLASS_PURPLE;
    }

    public static BlockState paving(int x, int y, int z) {
        int p = Hash.pct(x, y, z, 20);
        if (p < 40) return PB_BRICKS;
        if (p < 65) return DS_TILES;
        if (p < 80) return DS_BRICKS;
        if (p < 90) return PB_BRICKS_CRACKED;
        return DS_POLISHED;
    }

    // ---------- Bloques con orientacion ----------

    public static BlockState stairs(Block block, Direction facing, boolean top) {
        return block.defaultBlockState()
                .setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, top ? Half.TOP : Half.BOTTOM);
    }

    public static BlockState slab(Block block, boolean top) {
        return block.defaultBlockState().setValue(SlabBlock.TYPE, top ? SlabType.TOP : SlabType.BOTTOM);
    }

    public static BlockState log(Block block, Direction.Axis axis) {
        return block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    public static BlockState facing(Block block, Direction dir) {
        return block.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, dir);
    }

    public static BlockState candles(Block candle, int n) {
        return candle.defaultBlockState().setValue(CandleBlock.CANDLES, Math.max(1, Math.min(4, n))).setValue(CandleBlock.LIT, true);
    }

    public static BlockState leaves(Block block) {
        return block.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    }

    /** Valla/barrotes/panel conectados a lo largo de un eje. */
    public static BlockState connected(Block block, Direction.Axis axis) {
        BlockState s = block.defaultBlockState();
        if (axis == Direction.Axis.X) {
            s = s.setValue(BlockStateProperties.EAST, true).setValue(BlockStateProperties.WEST, true);
        } else {
            s = s.setValue(BlockStateProperties.NORTH, true).setValue(BlockStateProperties.SOUTH, true);
        }
        return s;
    }

    /** Muro (wall) conectado en un eje. */
    public static BlockState wallBlock(Block block, Direction.Axis axis) {
        BlockState s = block.defaultBlockState();
        if (axis == Direction.Axis.X) {
            s = s.setValue(BlockStateProperties.EAST_WALL, WallSide.LOW).setValue(BlockStateProperties.WEST_WALL, WallSide.LOW)
                    .setValue(BlockStateProperties.UP, false);
        } else {
            s = s.setValue(BlockStateProperties.NORTH_WALL, WallSide.LOW).setValue(BlockStateProperties.SOUTH_WALL, WallSide.LOW)
                    .setValue(BlockStateProperties.UP, false);
        }
        return s;
    }

    public static Direction dirOf(int dx, int dz) {
        if (Math.abs(dx) >= Math.abs(dz)) return dx >= 0 ? Direction.EAST : Direction.WEST;
        return dz >= 0 ? Direction.SOUTH : Direction.NORTH;
    }
}
