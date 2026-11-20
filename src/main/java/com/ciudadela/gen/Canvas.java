package com.ciudadela.gen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

    public final WorldGenLevel level;
    public final int minX, minZ, maxX, maxZ, minY, maxY;
    private final BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
    private final RandomSource random;

    public Canvas(WorldGenLevel level, int chunkX, int chunkZ, RandomSource random) {
        this.level = level;
        this.minX = chunkX << 4;
        this.minZ = chunkZ << 4;
        this.maxX = minX + 15;
        this.maxZ = minZ + 15;
        this.minY = level.getMinBuildHeight();
        this.maxY = level.getMaxBuildHeight() - 1;
        this.random = random;
    }

    public boolean in(int x, int y, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ && y >= minY && y <= maxY;
    }

    public boolean inXZ(int x, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public void set(int x, int y, int z, BlockState s) {
        if (!in(x, y, z)) return;
        level.setBlock(m.set(x, y, z), s, 2);
    }

    public BlockState get(int x, int y, int z) {
        if (!in(x, y, z)) return Pal.AIR;
        return level.getBlockState(m.set(x, y, z));
    }

    public boolean isAir(int x, int y, int z) {
        return in(x, y, z) && level.getBlockState(m.set(x, y, z)).isAir();
    }

    public void setIfAir(int x, int y, int z, BlockState s) {
        if (!in(x, y, z)) return;
        if (level.getBlockState(m.set(x, y, z)).isAir()) level.setBlock(m, s, 2);
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState s) {
        int ax = Math.max(Math.min(x0, x1), minX), bx = Math.min(Math.max(x0, x1), maxX);
        int az = Math.max(Math.min(z0, z1), minZ), bz = Math.min(Math.max(z0, z1), maxZ);
        int ay = Math.max(Math.min(y0, y1), minY), by = Math.min(Math.max(y0, y1), maxY);
        for (int x = ax; x <= bx; x++)
            for (int z = az; z <= bz; z++)
                for (int y = ay; y <= by; y++)
                    level.setBlock(m.set(x, y, z), s, 2);
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, StateFn f) {
        int ax = Math.max(Math.min(x0, x1), minX), bx = Math.min(Math.max(x0, x1), maxX);
        int az = Math.max(Math.min(z0, z1), minZ), bz = Math.min(Math.max(z0, z1), maxZ);
        int ay = Math.max(Math.min(y0, y1), minY), by = Math.min(Math.max(y0, y1), maxY);
        for (int x = ax; x <= bx; x++)
            for (int z = az; z <= bz; z++)
                for (int y = ay; y <= by; y++)
                    level.setBlock(m.set(x, y, z), f.at(x, y, z), 2);
    }

    /** Columna vertical [y0, y1]. */
    public void column(int x, int z, int y0, int y1, StateFn f) {
        if (!inXZ(x, z)) return;
        int a = Math.max(Math.min(y0, y1), minY), b = Math.min(Math.max(y0, y1), maxY);
        for (int y = a; y <= b; y++) level.setBlock(m.set(x, y, z), f.at(x, y, z), 2);
    }

    public void column(int x, int z, int y0, int y1, BlockState s) {
        if (!inXZ(x, z)) return;
        int a = Math.max(Math.min(y0, y1), minY), b = Math.min(Math.max(y0, y1), maxY);
        for (int y = a; y <= b; y++) level.setBlock(m.set(x, y, z), s, 2);
    }

    /** Agua que debe fluir (cascadas): se programa su actualizacion. */
    public void flowingWater(int x, int y, int z) {
        if (!in(x, y, z)) return;
        level.setBlock(m.set(x, y, z), Pal.WATER, 2);
        level.scheduleTick(m.immutable(), Fluids.WATER, 0);
    }

    public void chest(int x, int y, int z, Direction facing, ResourceLocation loot) {
        if (!in(x, y, z)) return;
        BlockPos p = new BlockPos(x, y, z);
        level.setBlock(p, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 2);
        RandomizableContainerBlockEntity.setLootTable(level, random, p, loot);
    }

    public void barrel(int x, int y, int z, Direction facing, ResourceLocation loot) {
        if (!in(x, y, z)) return;
        BlockPos p = new BlockPos(x, y, z);
        level.setBlock(p, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, facing), 2);
        if (loot != null) RandomizableContainerBlockEntity.setLootTable(level, random, p, loot);
    }

    public void addEntity(Entity e) {
        if (!inXZ((int) Math.floor(e.getX()), (int) Math.floor(e.getZ()))) return;
        level.addFreshEntity(e);
    }
}
