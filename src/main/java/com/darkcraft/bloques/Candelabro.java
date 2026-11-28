package com.darkcraft.bloques;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Los candelabros (parte 8g): `PIE` de pie en el suelo (tres velas, los brazos de lado a lado de quien lo pone) o `PARED`
 * (el aplique: una vela en un brazo que sale de la pared; mira hacia donde sale). Las llamas van en el modelo.
 */
public class Candelabro extends HorizontalDirectionalBlock {
    public static final int PIE = 0, PARED = 1;
    private static final VoxelShape PIE_FORMA = Block.box(1.0, 0.0, 6.0, 15.0, 16.0, 10.0);
    private static final VoxelShape PIE_FORMA_X = Block.box(6.0, 0.0, 1.0, 10.0, 16.0, 15.0);
    private final int tipo;

    public Candelabro(Properties p, int tipo) {
        super(p);
        this.tipo = tipo;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState st, BlockGetter nivel, BlockPos pos, CollisionContext c) {
        Direction d = st.getValue(FACING);
        if (tipo == PIE) return d.getAxis() == Direction.Axis.Z ? PIE_FORMA : PIE_FORMA_X;
        // el aplique: pegado a la pared de detras (la contraria a donde mira)
        return switch (d) {
            case NORTH -> Block.box(5.0, 3.0, 8.0, 11.0, 16.0, 16.0);
            case SOUTH -> Block.box(5.0, 3.0, 0.0, 11.0, 16.0, 8.0);
            case WEST -> Block.box(8.0, 3.0, 5.0, 16.0, 16.0, 11.0);
            default -> Block.box(0.0, 3.0, 5.0, 8.0, 16.0, 11.0);
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext c) {
        if (tipo == PIE) return defaultBlockState().setValue(FACING, c.getHorizontalDirection().getOpposite());
        Direction cara = c.getClickedFace();
        BlockState st = defaultBlockState().setValue(FACING, cara.getAxis().isHorizontal() ? cara : c.getHorizontalDirection().getOpposite());
        return st.canSurvive(c.getLevel(), c.getClickedPos()) ? st : null;
    }

    @Override
    public boolean canSurvive(BlockState st, LevelReader nivel, BlockPos pos) {
        if (tipo == PIE) return Block.canSupportCenter(nivel, pos.below(), Direction.UP);
        Direction d = st.getValue(FACING);
        BlockPos detras = pos.relative(d.getOpposite());
        return nivel.getBlockState(detras).isFaceSturdy(nivel, detras, d);
    }

    @Override
    public BlockState updateShape(BlockState st, Direction d, BlockState vecino, LevelAccessor nivel, BlockPos pos, BlockPos vecinoPos) {
        return !st.canSurvive(nivel, pos) ? Blocks.AIR.defaultBlockState() : st;
    }

    @Override
    public void animateTick(BlockState st, Level nivel, BlockPos pos, RandomSource r) {
        if (r.nextInt(8) == 0)
            nivel.addParticle(Fuegos.HUMO.get(), pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 0.0, 0.015, 0.0);
    }
}
