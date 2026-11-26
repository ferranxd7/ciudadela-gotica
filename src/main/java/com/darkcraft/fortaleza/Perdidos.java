package com.darkcraft.fortaleza;

import com.darkcraft.espejo.Espejo;
import com.darkcraft.gen.CastlePlan;
import com.darkcraft.gen.MapShapes;
import com.darkcraft.story.Logros;
import com.darkcraft.story.Story;
import com.darkcraft.world.CastleSites;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Los libros perdidos: "cosas que decia la gente" de Valdrenn (una lista de la compra, una carta, un sermon, el diario
 * de un nino, el encargo de un herrero) y de la Fortaleza de Hueso (juramentos, cartas, notas sobre el portal). Los de la
 * fortaleza estan en atriles desde que se levanta; los de Valdrenn se ponen en atriles la primera vez que alguien pasa
 * cerca de su sitio (el reino ya estaba construido en los mundos de antes). Se apuntan en el Libro de la Sombra.
 */
public final class Perdidos {
    private Perdidos() {}

    public static final int TOTAL = 23;
    /** Paginas de cada libro (las mismas en todos los idiomas; ver ci/historia/perdidos.py). */
    private static final int[] PAGINAS = {0, 2, 2, 3, 2, 3, 2, 3, 2, 3, 3, 1, 3, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3};
    /** Donde va cada libro de Valdrenn: el sitio del mapa (MapShapes). */
    private static final String[] SITIO = {null, "pueblo", "puerta_interior", "catedral", "jardin", "patio_armas"};
    public static final String TAG = "darkcraft_perdido";

    public static ItemStack libro(int n) {
        ItemStack b = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag t = b.getOrCreateTag();
        t.putString("title", "Libro perdido " + n);
        t.putString("author", "?");
        t.putInt("generation", 2);
        ListTag pages = new ListTag();
        for (int p = 1; p <= PAGINAS[n]; p++) {
            MutableComponent page = Component.empty();
            if (p == 1) {
                page.append(Component.translatable("darkcraft.perdido." + n + ".titulo").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
                page.append(Component.literal("\n"));
                page.append(Component.translatable("darkcraft.perdido." + n + ".autor")
                        .withStyle(s -> s.withItalic(true).withColor(TextColor.fromRgb(0x5A2A10))));
                page.append(Component.literal("\n"));
            }
            page.append(Component.translatable("darkcraft.perdido." + n + ".p" + p));
            pages.add(StringTag.valueOf(Component.Serializer.toJson(page)));
        }
        t.put("pages", pages);
        t.putBoolean("resolved", true);
        t.putInt(TAG, n);
        b.setHoverName(Component.translatable("darkcraft.perdido." + n + ".titulo").withStyle(s -> s.withItalic(false).withColor(ChatFormatting.GOLD)));
        CompoundTag display = t.getCompound("display");
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.translatable("darkcraft.perdido." + n + ".autor")
                .withStyle(s -> s.withItalic(true).withColor(ChatFormatting.GRAY)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.translatable("darkcraft.perdido.lore")
                .withStyle(s -> s.withItalic(true).withColor(ChatFormatting.DARK_GRAY)))));
        display.put("Lore", lore);
        t.put("display", display);
        return b;
    }

    /** Numero de libro perdido de un objeto (0 si no lo es). */
    public static int de(ItemStack st) {
        if (st == null || !st.is(Items.WRITTEN_BOOK) || !st.hasTag()) return 0;
        int n = st.getTag().getInt(TAG);
        return n >= 1 && n <= TOTAL ? n : 0;
    }

    /** El jugador ha leido (o tiene) el libro n: se apunta en su Libro de la Sombra. */
    public static void encontrado(ServerPlayer p, int n) {
        if (n < 1 || n > TOTAL) return;
        int antes = Espejo.perdidos(p);
        if ((antes & (1 << n)) != 0) return;
        int ahora = antes | (1 << n);
        Espejo.ponerPerdidos(p, ahora);
        Logros.dar(p, "libro_perdido");
        if (Integer.bitCount(ahora >> 1) >= TOTAL) Logros.dar(p, "libros_perdidos");
        p.displayClientMessage(Component.translatable("darkcraft.perdido.anotado",
                Component.translatable("darkcraft.perdido." + n + ".titulo")).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
    }

    // ------------------------------------------------------------------ los de Valdrenn

    /** Cada dos segundos: si el jugador pasa cerca del sitio de un libro de Valdrenn que aun no esta, se pone. */
    public static void tick(ServerPlayer p) {
        ServerLevel lvl = p.serverLevel();
        if (lvl.dimension() != Level.OVERWORLD || p.getY() < CastlePlan.Y0 - 40) return;
        int[] s = CastleSites.nearest(lvl.getSeed(), p.getBlockX(), p.getBlockZ(), 1);
        if (s == null || Math.hypot(s[0] - p.getX(), s[1] - p.getZ()) > CastlePlan.FOOTPRINT) return;
        FortalezaDatos d = FortalezaDatos.get(p.server);
        int[][] sitios = null;
        for (int n = 1; n < SITIO.length; n++) {
            String k = s[0] + "," + s[1] + ":" + n;
            if (d.libros.contains(k)) continue;
            if (sitios == null) sitios = sitios(lvl, s);
            int[] pos = sitios[n];
            if (pos == null) continue;
            int x = s[0] + pos[0], z = s[1] + pos[1];
            if (Math.hypot(x - p.getX(), z - p.getZ()) > 56) continue;
            if (!lvl.hasChunkAt(new BlockPos(x, CastlePlan.Y0, z))) continue;
            BlockPos sitio = hueco(lvl, x, z, n);
            if (sitio == null) continue;
            Direction mira = Direction.from2DDataValue(Math.floorMod(n * 3 + x, 4));
            lvl.setBlock(sitio, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, mira).setValue(LecternBlock.HAS_BOOK, true), 3);
            if (lvl.getBlockEntity(sitio) instanceof LecternBlockEntity le) le.setBook(libro(n));
            d.libros.add(k);
            d.setDirty();
            com.darkcraft.DarkCraftMod.LOGGER.info("Libro perdido {} puesto en un atril en {}", n, sitio.toShortString());
        }
    }

    /** Donde va cada libro de un reino (relativo a su centro), calculado una vez por reino. */
    private static final java.util.Map<String, int[][]> SITIOS = new java.util.HashMap<>();

    private static int[][] sitios(ServerLevel lvl, int[] s) {
        return SITIOS.computeIfAbsent(s[0] + "," + s[1], k -> {
            MapShapes.Data m = MapShapes.build(Story.planAt(lvl, s[0], s[1]));
            int[][] r = new int[SITIO.length][];
            for (int n = 1; n < SITIO.length; n++) r[n] = posicion(m, SITIO[n]);
            return r;
        });
    }

    private static int[] posicion(MapShapes.Data m, String sitio) {
        for (int i = 0; i < m.labels().length; i++)
            if (m.labels()[i].equals(sitio)) return new int[]{m.labelPos()[i * 2], m.labelPos()[i * 2 + 1]};
        return null;
    }

    /**
     * Un sitio para el atril cerca de (x, z): suelo firme a la altura del reino (dentro de un edificio vale: se busca de
     * abajo arriba), dos bloques de aire encima, sin agua, y que no tape una puerta.
     */
    private static BlockPos hueco(ServerLevel lvl, int x0, int z0, int n) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int r = 0; r <= 14; r++)
            for (int k = 0; k < Math.max(1, r * 8); k++) {
                double a = (k + n * 0.37) * Math.PI * 2 / Math.max(1, r * 8);
                int x = x0 + (int) Math.round(Math.cos(a) * r), z = z0 + (int) Math.round(Math.sin(a) * r);
                if (!lvl.hasChunkAt(m.set(x, CastlePlan.Y0, z))) continue;
                for (int y = CastlePlan.Y0 - 3; y <= CastlePlan.Y0 + 10; y++) {
                    BlockState pie = lvl.getBlockState(m.set(x, y - 1, z));
                    if (!pie.isFaceSturdy(lvl, m, Direction.UP)) continue;
                    BlockState a0 = lvl.getBlockState(m.set(x, y, z)), a1 = lvl.getBlockState(m.set(x, y + 1, z));
                    if (!a0.isAir() || !a1.isAir() || !lvl.getFluidState(m.set(x, y, z)).isEmpty()) continue;
                    // que no este pegado a una puerta
                    boolean puerta = false;
                    for (Direction d : Direction.Plane.HORIZONTAL)
                        if (lvl.getBlockState(m.set(x, y, z).move(d)).getBlock() instanceof net.minecraft.world.level.block.DoorBlock) puerta = true;
                    if (puerta) continue;
                    return new BlockPos(x, y, z);
                }
            }
        return null;
    }
}
