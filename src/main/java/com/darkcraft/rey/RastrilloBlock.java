package com.darkcraft.rey;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Rastrillo de hierro negro que cierra las puertas del salon del trono durante la pelea. Irrompible. */
public class RastrilloBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    private static final VoxelShape X = Block.box(0, 0, 6.5, 16, 16, 9.5);
    private static final VoxelShape Z = Block.box(6.5, 0, 0, 9.5, 16, 16);

    public RastrilloBlock(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction d = ctx.getHorizontalDirection();
        return defaultBlockState().setValue(AXIS, d.getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X);
    }

    @Override
    public VoxelShape getShape(BlockState st, BlockGetter lvl, BlockPos pos, CollisionContext ctx) {
        return st.getValue(AXIS) == Direction.Axis.X ? X : Z;
    }

    @Override
    public boolean skipRendering(BlockState st, BlockState other, Direction d) {
        return other.is(this) || super.skipRendering(st, other, d);
    }
}
