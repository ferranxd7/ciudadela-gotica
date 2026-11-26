package com.darkcraft.morvain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * La estela del mapa de Morvain (parte 7d): un obelisco bajo de piedra negra con el mapa de su region tallado y un
 * pergamino clavado (modelo de tools/morvain/estela.py; `facing` = hacia donde mira el pergamino). Quien la usa (clic
 * derecho o la tecla del aviso) copia ese trozo del mapa: {@link Fragmentos#usar}. No se puede romper.
 */
public class EstelaBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** El zocalo y el fuste (llega a 1,4 bloques: como una valla, no se salta). */
    private static final VoxelShape FORMA = Shapes.or(Block.box(2, 0, 2, 14, 3, 14), Block.box(4, 3, 4, 12, 23, 12));

    public EstelaBlock(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext c) {
        return defaultBlockState().setValue(FACING, c.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState st, Rotation r) {
        return st.setValue(FACING, r.rotate(st.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState st, Mirror m) {
        return st.rotate(m.getRotation(st.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState st, BlockGetter l, BlockPos p, CollisionContext c) {
        return FORMA;
    }

    @Override
    public InteractionResult use(BlockState st, Level lvl, BlockPos pos, Player p, InteractionHand hand, BlockHitResult hit) {
        if (lvl.isClientSide) return InteractionResult.SUCCESS;
        if (p instanceof ServerPlayer sp) Fragmentos.usar(sp, pos);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isPathfindable(BlockState st, BlockGetter l, BlockPos p, PathComputationType t) {
        return false;
    }
}
