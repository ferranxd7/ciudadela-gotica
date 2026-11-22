package com.darkcraft.story;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.CastlePlan;
import com.darkcraft.world.CastleSites;
import com.darkcraft.world.PlanCache;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * La cronica de Valdrenn: cinco capitulos escritos por Aldous, el cronista del rey. Los textos estan
 * en los ficheros de idioma (cada jugador los lee en el idioma de su juego).
 */
public final class Story {
    private Story() {}

    public static final int CHAPTERS = 5;
    /** Paginas de cada libro (capitulos 1..5). */
    private static final int[] PAGES = {0, 8, 7, 7, 7, 7};
    private static final String KEY = "darkcraft";

    // ------------------------------------------------------------------ libros

    public static ItemStack book(int ch) {
        ItemStack b = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag t = b.getOrCreateTag();
        t.putString("title", "Cronica de Valdrenn " + ch);
        t.putString("author", "Aldous");
        t.putInt("generation", 0);
        ListTag pages = new ListTag();
        for (int p = 1; p <= PAGES[ch]; p++) {
            MutableComponent page = Component.empty();
            if (p == 1) {
                page.append(Component.translatable("darkcraft.capitulo", ch).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
                page.append(Component.literal("\n"));
                page.append(Component.translatable("darkcraft.cap." + ch + ".titulo")
                        .withStyle(s -> s.withItalic(true).withColor(TextColor.fromRgb(0x5A2A10))));
                page.append(Component.literal("\n\n"));
            }
            page.append(Component.translatable("darkcraft.cap." + ch + ".p" + p));
            pages.add(StringTag.valueOf(Component.Serializer.toJson(page)));
        }
        t.put("pages", pages);
        t.putBoolean("resolved", true);
        b.setHoverName(Component.translatable("darkcraft.libro", ch).withStyle(s -> s.withItalic(false).withColor(ChatFormatting.DARK_RED)));
        CompoundTag display = t.getCompound("display");
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.translatable("darkcraft.cap." + ch + ".titulo")
                .withStyle(s -> s.withItalic(true).withColor(ChatFormatting.GOLD)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.translatable("darkcraft.libro.lore")
                .withStyle(s -> s.withItalic(true).withColor(ChatFormatting.DARK_GRAY)))));
        display.put("Lore", lore);
        t.put("display", display);
        t.putInt(KEY + "_libro", ch);
        return b;
    }

    /** El mapa de pergamino de la ciudadela que esta en (cx, cz). */
    public static ItemStack map(int cx, int cz) {
        ItemStack m = new ItemStack(DarkCraftMod.MAPA.get());
        CompoundTag t = m.getOrCreateTag();
        t.putInt("castilloX", cx);
        t.putInt("castilloZ", cz);
        return m;
    }

    // ------------------------------------------------------------------ progreso de cada jugador

    private static CompoundTag data(Player p) {
        CompoundTag root = p.getPersistentData();
        CompoundTag pers = root.getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag mine = pers.getCompound(KEY);
        pers.put(KEY, mine);
        root.put(Player.PERSISTED_NBT_TAG, pers);
        return mine;
    }

    /** Capitulos leidos (bit 1 = capitulo 1...). */
    public static int read(Player p) {
        return data(p).getInt("leidos");
    }

    public static void markRead(Player p, int ch) {
        CompoundTag d = data(p);
        d.putInt("leidos", d.getInt("leidos") | (1 << ch));
    }

    public static boolean seenCine(Player p, int ch) {
        return (data(p).getInt("cine") & (1 << ch)) != 0;
    }

    public static void markSeen(Player p, int ch) {
        CompoundTag d = data(p);
        d.putInt("cine", d.getInt("cine") | (1 << ch));
    }

    public static void reset(Player p) {
        CompoundTag d = data(p);
        d.putInt("leidos", 0);
        d.putInt("cine", 0);
    }

    /** Siguiente capitulo por leer (1..5) o 6 si ya se han leido todos. */
    public static int next(int readMask) {
        for (int c = 1; c <= CHAPTERS; c++) if ((readMask & (1 << c)) == 0) return c;
        return CHAPTERS + 1;
    }

    // ------------------------------------------------------------------ planos en tiempo de juego

    /** Plano de la ciudadela mas cercana a (x, z), o null. */
    public static CastlePlan planNear(ServerLevel lvl, int x, int z) {
        long seed = lvl.getSeed();
        int[] s = CastleSites.nearest(seed, x, z, 2);
        if (s == null) return null;
        return planAt(lvl, s[0], s[1]);
    }

    public static CastlePlan planAt(ServerLevel lvl, int sx, int sz) {
        ServerLevel ow = lvl.getServer().overworld();
        var gen = ow.getChunkSource().getGenerator();
        var rs = ow.getChunkSource().randomState();
        return PlanCache.get(ow.getSeed(), sx, sz, (x, z) -> gen.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, ow, rs));
    }
}
