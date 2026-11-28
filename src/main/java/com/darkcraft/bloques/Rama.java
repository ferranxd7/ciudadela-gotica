package com.darkcraft.bloques;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;

/**
 * Las ramas de los arboles nuevos (parte 8e): un palo de 4, 8 o 12 pixeles de grueso (como la planta de coro) que se une
 * por sus seis caras a otras ramas, a troncos y cortezas ({@code minecraft:logs}), a hojas ({@code minecraft:leaves}) y a
 * cualquier cara firme de al lado. Los arboles las ponen con sus uniones ya puestas; al ponerlas a mano se unen solas.
 */
public class Rama extends PipeBlock {
    /** {@code medio}: la mitad del grueso en bloques (0,125 ramita de 4 pixeles, 0,25 rama de 8, 0,375 rama gruesa de 12). */
    public Rama(float medio, Properties p) {
        super(medio, p);
        registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false)
                .setValue(WEST, false).setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    /** Si una rama se une a lo que tiene en esa direccion. */
    public static boolean une(BlockGetter nivel, BlockPos pos, Direction d) {
        BlockPos al = pos.relative(d);
        BlockState v = nivel.getBlockState(al);
        return v.getBlock() instanceof Rama || v.is(BlockTags.LOGS) || v.is(BlockTags.LEAVES)
                || v.isFaceSturdy(nivel, al, d.getOpposite());
    }

    /** El estado de una rama en ese sitio, unida a lo que tenga alrededor. */
    public BlockState unida(BlockGetter nivel, BlockPos pos) {
        BlockState st = defaultBlockState();
        for (Direction d : Direction.values()) st = st.setValue(PROPERTY_BY_DIRECTION.get(d), une(nivel, pos, d));
        return st;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext c) {
        BlockState st = unida(c.getLevel(), c.getClickedPos());
        // siempre unida a aquello contra lo que se pone (aunque no sea firme)
        return st.setValue(PROPERTY_BY_DIRECTION.get(c.getClickedFace().getOpposite()), true);
    }

    @Override
    public BlockState updateShape(BlockState st, Direction d, BlockState vecino, LevelAccessor nivel, BlockPos pos, BlockPos vecinoPos) {
        boolean une = vecino.getBlock() instanceof Rama || vecino.is(BlockTags.LOGS) || vecino.is(BlockTags.LEAVES)
                || vecino.isFaceSturdy(nivel, vecinoPos, d.getOpposite());
        return st.setValue(PROPERTY_BY_DIRECTION.get(d), une);
    }

    @Override
    public boolean isPathfindable(BlockState st, BlockGetter nivel, BlockPos pos, PathComputationType tipo) {
        return false;
    }
}
