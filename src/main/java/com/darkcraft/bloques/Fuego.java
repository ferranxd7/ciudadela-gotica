package com.darkcraft.bloques;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * El fuego propio (parte 8g): arde en el suelo (encima de una cara firme), no se extiende ni se apaga, quema a quien lo pisa
 * (sin el fuego de Minecraft encima) y echa llamas, chispas y humo de su color.
 */
public class Fuego extends Block {
    private static final VoxelShape FORMA = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private final String color;

    public Fuego(Properties p, String color) {
        super(p);
        this.color = color;
    }

    public String color() {
        return color;
    }

    @Override
    public VoxelShape getShape(BlockState st, BlockGetter nivel, BlockPos pos, CollisionContext c) {
        return FORMA;
    }

    @Override
    public boolean canSurvive(BlockState st, LevelReader nivel, BlockPos pos) {
        return nivel.getBlockState(pos.below()).isFaceSturdy(nivel, pos.below(), Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState st, Direction d, BlockState vecino, LevelAccessor nivel, BlockPos pos, BlockPos vecinoPos) {
        return d == Direction.DOWN && !st.canSurvive(nivel, pos) ? Blocks.AIR.defaultBlockState() : st;
    }

    @Override
    public void entityInside(BlockState st, Level nivel, BlockPos pos, Entity e) {
        if (!nivel.isClientSide && e instanceof LivingEntity && !e.fireImmune())
            e.hurt(nivel.damageSources().inFire(), 1.5F);
    }

    @Override
    public void animateTick(BlockState st, Level nivel, BlockPos pos, RandomSource r) {
        Fuegos.avivar(nivel, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, 0.8, color, r, 2, true, true);
    }
}
