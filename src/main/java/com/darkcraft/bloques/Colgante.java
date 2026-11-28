package com.darkcraft.bloques;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Lo que cuelga de los arboles nuevos (parte 8e): musgo y barba de viejo. Una cruz sin choque que se sostiene de lo que
 * tiene encima (una cara de abajo firme, hojas, ramas, troncos u otro igual) y se cae si se lo quitan; {@code punta} es el
 * de abajo del todo (el que acaba), lo pone solo.
 */
public class Colgante extends Block {
    public static final BooleanProperty PUNTA = BooleanProperty.create("punta");
    private static final VoxelShape FORMA = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public Colgante(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(PUNTA, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(PUNTA);
    }

    @Override
    public VoxelShape getShape(BlockState st, BlockGetter nivel, BlockPos pos, CollisionContext c) {
        return FORMA;
    }

    @Override
    public boolean canSurvive(BlockState st, LevelReader nivel, BlockPos pos) {
        BlockPos arriba = pos.above();
        BlockState a = nivel.getBlockState(arriba);
        return a.is(this) || a.is(BlockTags.LEAVES) || a.is(BlockTags.LOGS) || a.isFaceSturdy(nivel, arriba, Direction.DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext c) {
        BlockState st = defaultBlockState().setValue(PUNTA, !c.getLevel().getBlockState(c.getClickedPos().below()).is(this));
        return st.canSurvive(c.getLevel(), c.getClickedPos()) ? st : null;
    }

    @Override
    public BlockState updateShape(BlockState st, Direction d, BlockState vecino, LevelAccessor nivel, BlockPos pos, BlockPos vecinoPos) {
        if (d == Direction.UP && !st.canSurvive(nivel, pos)) return Blocks.AIR.defaultBlockState();
        if (d == Direction.DOWN) return st.setValue(PUNTA, !vecino.is(this));
        return st;
    }
}
