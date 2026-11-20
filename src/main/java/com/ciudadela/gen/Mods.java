package com.ciudadela.gen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Acceso opcional a bloques de otros mods (Macaw's, Handcrafted, Supplementaries, Another Furniture,
 * Biomes O' Plenty...). Si el mod no esta instalado devuelve null y se usa el bloque vanilla.
 */
public final class Mods {
    private Mods() {}

    private static final ConcurrentHashMap<String, Optional<Block>> CACHE = new ConcurrentHashMap<>();

    public static boolean loaded(String modid) {
        return ModList.get().isLoaded(modid);
    }

    public static Block block(String id) {
        return CACHE.computeIfAbsent(id, k -> {
            ResourceLocation rl = ResourceLocation.tryParse(k);
            if (rl == null || !ForgeRegistries.BLOCKS.containsKey(rl)) return Optional.empty();
            return Optional.ofNullable(ForgeRegistries.BLOCKS.getValue(rl));
        }).orElse(null);
    }

    /** Estado de un bloque de mod con propiedades "nombre=valor"; null si no existe. */
    public static BlockState state(String id, String... props) {
        Block b = block(id);
        if (b == null) return null;
        BlockState s = b.defaultBlockState();
        for (String p : props) {
            int i = p.indexOf('=');
            if (i < 0) continue;
            Property<?> prop = b.getStateDefinition().getProperty(p.substring(0, i));
            if (prop != null) s = with(s, prop, p.substring(i + 1));
        }
        return s;
    }

    /** Igual que state() pero con respaldo vanilla. */
    public static BlockState or(BlockState fallback, String id, String... props) {
        BlockState s = state(id, props);
        return s != null ? s : fallback;
    }

    /** Aplica una propiedad si el estado la tiene (para orientar bloques de mods). */
    public static BlockState prop(BlockState s, String name, String value) {
        if (s == null) return null;
        Property<?> prop = s.getBlock().getStateDefinition().getProperty(name);
        return prop == null ? s : with(s, prop, value);
    }

    private static <T extends Comparable<T>> BlockState with(BlockState s, Property<T> p, String v) {
        return p.getValue(v).map(x -> s.setValue(p, x)).orElse(s);
    }
}
