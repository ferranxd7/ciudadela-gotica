package com.ciudadela.gen;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Juegos de tronco/hojas para los arboles (vanilla y, si esta instalado, Biomes O' Plenty). */
public final class Woods {
    private Woods() {}

    public record Wood(BlockState log, BlockState wood, BlockState leaves, BlockState leaves2) {}

    private static BlockState lv(BlockState s) {
        if (s != null && s.hasProperty(LeavesBlock.PERSISTENT)) return s.setValue(LeavesBlock.PERSISTENT, true);
        return s;
    }

    private static Wood vanilla(String name) {
        return switch (name) {
            case "dark_oak" -> new Wood(Pal.b(Blocks.DARK_OAK_LOG), Pal.b(Blocks.DARK_OAK_WOOD), Pal.leaves(Blocks.DARK_OAK_LEAVES), Pal.leaves(Blocks.AZALEA_LEAVES));
            case "spruce" -> new Wood(Pal.b(Blocks.SPRUCE_LOG), Pal.b(Blocks.SPRUCE_WOOD), Pal.leaves(Blocks.SPRUCE_LEAVES), Pal.leaves(Blocks.SPRUCE_LEAVES));
            case "mangrove" -> new Wood(Pal.b(Blocks.MANGROVE_LOG), Pal.b(Blocks.MANGROVE_WOOD), Pal.leaves(Blocks.MANGROVE_LEAVES), Pal.leaves(Blocks.DARK_OAK_LEAVES));
            case "cherry" -> new Wood(Pal.b(Blocks.CHERRY_LOG), Pal.b(Blocks.CHERRY_WOOD), Pal.leaves(Blocks.CHERRY_LEAVES), Pal.leaves(Blocks.CHERRY_LEAVES));
            case "crimson" -> new Wood(Pal.b(Blocks.CRIMSON_STEM), Pal.b(Blocks.CRIMSON_HYPHAE), Pal.b(Blocks.NETHER_WART_BLOCK), Pal.b(Blocks.SHROOMLIGHT));
            case "warped" -> new Wood(Pal.b(Blocks.WARPED_STEM), Pal.b(Blocks.WARPED_HYPHAE), Pal.b(Blocks.WARPED_WART_BLOCK), Pal.b(Blocks.SHROOMLIGHT));
            case "azalea" -> new Wood(Pal.b(Blocks.OAK_LOG), Pal.b(Blocks.OAK_WOOD), Pal.leaves(Blocks.AZALEA_LEAVES), Pal.leaves(Blocks.FLOWERING_AZALEA_LEAVES));
            default -> new Wood(Pal.b(Blocks.OAK_LOG), Pal.b(Blocks.OAK_WOOD), Pal.leaves(Blocks.OAK_LEAVES), Pal.leaves(Blocks.DARK_OAK_LEAVES));
        };
    }

    /** Madera de Biomes O' Plenty si existe, si no la vanilla indicada. */
    private static Wood bop(String name, String fallback) {
        BlockState log = Mods.state("biomesoplenty:" + name + "_log");
        BlockState wood = Mods.state("biomesoplenty:" + name + "_wood");
        BlockState leaves = lv(Mods.state("biomesoplenty:" + name + "_leaves"));
        if (log == null || leaves == null) return vanilla(fallback);
        return new Wood(log, wood != null ? wood : log, leaves, leaves);
    }

    public static Wood forTree(Tree.Kind kind, Rng r) {
        return switch (kind) {
            case BIG_DARK -> r.chance(0.25) ? bop("umbran", "dark_oak") : vanilla("dark_oak");
            case SPRUCE -> r.chance(0.35) ? bop("fir", "spruce") : (r.chance(0.3) ? bop("redwood", "spruce") : vanilla("spruce"));
            case WILLOW -> bop("willow", "azalea");
            case MANGROVE -> vanilla("mangrove");
            case CHERRY -> r.chance(0.5) ? bop("jacaranda", "cherry") : vanilla("cherry");
            case MAGIC -> {
                double p = r.nextDouble();
                if (p < 0.3) yield bop("magic", "warped");
                if (p < 0.6) yield bop("hellbark", "crimson");
                yield r.chance(0.5) ? vanilla("crimson") : vanilla("warped");
            }
            case BUSH -> vanilla(r.chance(0.5) ? "dark_oak" : "spruce");
            case AZALEA -> vanilla("azalea");
            default -> {
                double p = r.nextDouble();
                if (p < 0.4) yield vanilla("dark_oak");
                if (p < 0.55) yield bop("dead", "dark_oak");
                if (p < 0.7) yield bop("umbran", "dark_oak");
                yield vanilla("oak");
            }
        };
    }

    /** Lo que cuelga de las ramas. */
    public static BlockState hanging(Tree.Kind kind, boolean tip) {
        if (kind == Tree.Kind.MAGIC) return tip ? Pal.b(Blocks.WEEPING_VINES) : Pal.b(Blocks.WEEPING_VINES_PLANT);
        if (tip) return Blocks.CAVE_VINES.defaultBlockState().setValue(CaveVines.BERRIES, true);
        return Blocks.CAVE_VINES_PLANT.defaultBlockState().setValue(CaveVines.BERRIES, false);
    }
}
