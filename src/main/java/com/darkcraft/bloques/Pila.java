package com.darkcraft.bloques;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Una pila de una a cuatro cosas (los libros, parte 8h): se anade otra poniendola encima, como las velas. */
public class Pila extends Block {
    public static final IntegerProperty CUANTOS = IntegerProperty.create("cuantos", 1, 4);

    public Pila(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(CUANTOS, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(CUANTOS);
    }

    @Override
    public VoxelShape getShape(BlockState st, BlockGetter nivel, BlockPos pos, CollisionContext c) {
        return Block.box(3.0, 0.0, 3.0, 13.0, 2.0 * st.getValue(CUANTOS), 13.0);
    }

    @Override
    public boolean canBeReplaced(BlockState st, BlockPlaceContext c) {
        return !c.isSecondaryUseActive() && c.getItemInHand().getItem() == asItem() && st.getValue(CUANTOS) < 4
                || super.canBeReplaced(st, c);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext c) {
        BlockState ya = c.getLevel().getBlockState(c.getClickedPos());
        if (ya.is(this)) return ya.setValue(CUANTOS, Math.min(4, ya.getValue(CUANTOS) + 1));
        return defaultBlockState();
    }

    @Override
    public boolean canSurvive(BlockState st, LevelReader nivel, BlockPos pos) {
        return Block.canSupportCenter(nivel, pos.below(), Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState st, Direction d, BlockState vecino, LevelAccessor nivel, BlockPos pos, BlockPos vecinoPos) {
        return d == Direction.DOWN && !st.canSurvive(nivel, pos) ? Blocks.AIR.defaultBlockState() : st;
    }
}
