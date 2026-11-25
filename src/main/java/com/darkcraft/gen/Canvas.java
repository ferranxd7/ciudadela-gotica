package com.darkcraft.gen;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Lienzo recortado a un solo chunk: los elementos "dibujan" el castillo entero y solo se escriben
 * los bloques que caen dentro del chunk que se esta generando. Asi el castillo puede medir lo que
 * queramos sin romper la generacion por chunks de Minecraft.
 */
public final class Canvas {
    public interface StateFn {
        BlockState at(int x, int y, int z);
    }

    /**
     * Donde va lo que se dibuja cuando el lienzo no escribe en ningun mundo ({@link #apunte}): asi se sabe como es una
     * pieza (bloque a bloque) sin levantarla. Lo usa el barco, que se quita de un muelle y se pone en otro.
     */
    public interface Apunte {
        void pon(int x, int y, int z, BlockState s);

        BlockState mira(int x, int y, int z);

        /** Una valla, un muro bajo o un cristal de los que el juego une a sus vecinos al acabar de generar ({@link Canvas#unir}). */
        default void une(int x, int y, int z, BlockState s) {
            pon(x, y, z, s);
        }
    }

    public final WorldGenLevel level;
    public final int minX, minZ, maxX, maxZ, minY, maxY;
    private final BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
    private final RandomSource random;
    private final Apunte apunte;

    public Canvas(WorldGenLevel level, int chunkX, int chunkZ, RandomSource random) {
        this.level = level;
        this.minX = chunkX << 4;
        this.minZ = chunkZ << 4;
        this.maxX = minX + 15;
        this.maxZ = minZ + 15;
        this.minY = level.getMinBuildHeight();
        this.maxY = level.getMaxBuildHeight() - 1;
        this.random = random;
        this.apunte = null;
    }

    private Canvas(Apunte apunte) {
        this.level = null;
        this.minX = this.minZ = -30_000_000;
        this.maxX = this.maxZ = 30_000_000;
        this.minY = -2048;
        this.maxY = 2048;
        this.random = RandomSource.create(7);
        this.apunte = apunte;
    }

    /** Un lienzo sin mundo y sin bordes: todo lo que se le pone va a {@code a} (los cofres y demas, solo su bloque). */
    public static Canvas apunte(Apunte a) {
        return new Canvas(a);
    }

    /** Pone el bloque (ya se sabe que cae dentro). */
    private void pon(int x, int y, int z, BlockState s) {
        if (apunte != null) apunte.pon(x, y, z, s);
        else level.setBlock(m.set(x, y, z), s, 2);
    }

    private BlockState mira(int x, int y, int z) {
        return apunte != null ? apunte.mira(x, y, z) : level.getBlockState(m.set(x, y, z));
    }

    public boolean in(int x, int y, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ && y >= minY && y <= maxY;
    }

    public boolean inXZ(int x, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public void set(int x, int y, int z, BlockState s) {
        if (!in(x, y, z)) return;
        pon(x, y, z, s);
    }

    /**
     * Vallas, muros bajos, barrotes y cristales: se ponen sueltos y se unen solos a sus vecinos cuando el chunk acaba de
     * generarse (el juego repasa las posiciones apuntadas con los chunks de alrededor ya hechos: tambien en los bordes).
     */
    public void unir(int x, int y, int z, BlockState s) {
        if (!in(x, y, z)) return;
        if (apunte != null) {
            apunte.une(x, y, z, s);
            return;
        }
        pon(x, y, z, s);
        if (level instanceof net.minecraft.server.level.WorldGenRegion) level.getChunk(m).markPosForPostprocessing(m);
    }

    public BlockState get(int x, int y, int z) {
        if (!in(x, y, z)) return Pal.AIR;
        return mira(x, y, z);
    }

    public boolean isAir(int x, int y, int z) {
        return in(x, y, z) && mira(x, y, z).isAir();
    }

    public void setIfAir(int x, int y, int z, BlockState s) {
        if (!in(x, y, z)) return;
        if (mira(x, y, z).isAir()) pon(x, y, z, s);
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState s) {
        int ax = Math.max(Math.min(x0, x1), minX), bx = Math.min(Math.max(x0, x1), maxX);
        int az = Math.max(Math.min(z0, z1), minZ), bz = Math.min(Math.max(z0, z1), maxZ);
        int ay = Math.max(Math.min(y0, y1), minY), by = Math.min(Math.max(y0, y1), maxY);
        for (int x = ax; x <= bx; x++)
            for (int z = az; z <= bz; z++)
                for (int y = ay; y <= by; y++)
                    pon(x, y, z, s);
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, StateFn f) {
        int ax = Math.max(Math.min(x0, x1), minX), bx = Math.min(Math.max(x0, x1), maxX);
        int az = Math.max(Math.min(z0, z1), minZ), bz = Math.min(Math.max(z0, z1), maxZ);
        int ay = Math.max(Math.min(y0, y1), minY), by = Math.min(Math.max(y0, y1), maxY);
        for (int x = ax; x <= bx; x++)
            for (int z = az; z <= bz; z++)
                for (int y = ay; y <= by; y++)
                    pon(x, y, z, f.at(x, y, z));
    }

    /** Columna vertical [y0, y1]. */
    public void column(int x, int z, int y0, int y1, StateFn f) {
        if (!inXZ(x, z)) return;
        int a = Math.max(Math.min(y0, y1), minY), b = Math.min(Math.max(y0, y1), maxY);
        for (int y = a; y <= b; y++) pon(x, y, z, f.at(x, y, z));
    }

    public void column(int x, int z, int y0, int y1, BlockState s) {
        if (!inXZ(x, z)) return;
        int a = Math.max(Math.min(y0, y1), minY), b = Math.min(Math.max(y0, y1), maxY);
        for (int y = a; y <= b; y++) pon(x, y, z, s);
    }

    /** Agua que debe fluir (cascadas): se programa su actualizacion. */
    public void flowingWater(int x, int y, int z) {
        if (!in(x, y, z)) return;
        if (apunte != null) {
            apunte.pon(x, y, z, Pal.WATER);
            return;
        }
        level.setBlock(m.set(x, y, z), Pal.WATER, 2);
        level.scheduleTick(m.immutable(), Fluids.WATER, 0);
    }

    public void chest(int x, int y, int z, Direction facing, ResourceLocation loot) {
        if (!in(x, y, z)) return;
        if (apunte != null) {
            apunte.pon(x, y, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing));
            return;
        }
        BlockPos p = new BlockPos(x, y, z);
        level.setBlock(p, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 2);
        RandomizableContainerBlockEntity.setLootTable(level, random, p, loot);
    }

    /** Atril con un libro (los libros perdidos de la Fortaleza de Hueso). */
    public void lectern(int x, int y, int z, Direction facing, net.minecraft.world.item.ItemStack book) {
        if (!in(x, y, z)) return;
        if (apunte != null) {
            apunte.pon(x, y, z, Blocks.LECTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LecternBlock.FACING, facing)
                    .setValue(net.minecraft.world.level.block.LecternBlock.HAS_BOOK, true));
            return;
        }
        BlockPos p = new BlockPos(x, y, z);
        level.setBlock(p, Blocks.LECTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LecternBlock.FACING, facing)
                .setValue(net.minecraft.world.level.block.LecternBlock.HAS_BOOK, true), 2);
        if (level.getBlockEntity(p) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le) le.setBook(book.copy());
    }

    public void barrel(int x, int y, int z, Direction facing, ResourceLocation loot) {
        if (!in(x, y, z)) return;
        if (apunte != null) {
            apunte.pon(x, y, z, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, facing));
            return;
        }
        BlockPos p = new BlockPos(x, y, z);
        level.setBlock(p, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, facing), 2);
        if (loot != null) RandomizableContainerBlockEntity.setLootTable(level, random, p, loot);
    }

    /** Marca de los cofres y carteles de la historia (en los datos del bloque). */
    public static final String STORY_TAG = "darkcraft_cap";

    /** Cofre de un capitulo de la cronica: cada jugador ve su propio contenido (ver StoryEvents). */
    public void storyChest(int x, int y, int z, Direction facing, int chapter) {
        if (!in(x, y, z)) return;
        if (apunte != null) {
            apunte.pon(x, y, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing));
            return;
        }
        BlockPos p = new BlockPos(x, y, z);
        level.setBlock(p, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 2);
        BlockEntity be = level.getBlockEntity(p);
        if (be != null) be.getPersistentData().putInt(STORY_TAG, chapter);
    }

    /** Cartel junto al cofre: CAPITULO N y el titulo, en el idioma de cada jugador. */
    public void storySign(int x, int y, int z, Direction front, int chapter) {
        if (!in(x, y, z)) return;
        if (apunte != null) {
            apunte.pon(x, y, z, Blocks.DARK_OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, RotationSegment.convertToSegment(front)));
            return;
        }
        BlockPos p = new BlockPos(x, y, z);
        level.setBlock(p, Blocks.DARK_OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, RotationSegment.convertToSegment(front)), 2);
        if (level.getBlockEntity(p) instanceof SignBlockEntity sign) writeSign(sign, chapter);
    }

    /**
     * Texto del cartel de un capitulo (traducido en el cliente de cada jugador). Se escribe cargando los
     * datos del cartel, porque durante la generacion el cartel todavia no tiene mundo y setText fallaria.
     */
    public static void writeSign(SignBlockEntity sign, int chapter) {
        SignText t = new SignText()
                .setMessage(0, Component.translatable("darkcraft.capitulo", chapter).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .setMessage(1, Component.translatable("darkcraft.cap." + chapter + ".cartel1").withStyle(ChatFormatting.GOLD))
                .setMessage(2, Component.translatable("darkcraft.cap." + chapter + ".cartel2").withStyle(ChatFormatting.GOLD))
                .setMessage(3, Component.literal("- + -").withStyle(ChatFormatting.DARK_GRAY))
                .setColor(DyeColor.RED).setHasGlowingText(true);
        net.minecraft.nbt.CompoundTag tag = sign.saveWithoutMetadata();
        SignText.DIRECT_CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, t).result().ifPresent(n -> {
            tag.put("front_text", n);
            tag.put("back_text", n.copy());
        });
        tag.putBoolean("is_waxed", true);
        sign.load(tag);
        sign.getPersistentData().putInt(STORY_TAG, chapter);
    }

    public void addEntity(Entity e) {
        if (apunte != null || !inXZ((int) Math.floor(e.getX()), (int) Math.floor(e.getZ()))) return;
        level.addFreshEntity(e);
    }
}
