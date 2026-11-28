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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Velas encendidas (parte 8g): de una a cuatro en el mismo sitio (se anaden poniendo otra encima, como las del juego), con
 * su llama en el modelo (animada) y algun hilo de humo. Dan 3 de luz cada una.
 */
public class Vela extends Block {
    public static final IntegerProperty VELAS = IntegerProperty.create("velas", 1, 4);
    private static final VoxelShape[] FORMAS = {
            Block.box(7.0, 0.0, 7.0, 9.0, 7.0, 9.0),
            Block.box(5.0, 0.0, 6.0, 11.0, 7.0, 9.0),
            Block.box(4.0, 0.0, 4.0, 12.0, 7.0, 11.0),
            Block.box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0)};

    public Vela(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(VELAS, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(VELAS);
    }

    @Override
    public VoxelShape getShape(BlockState st, BlockGetter nivel, BlockPos pos, CollisionContext c) {
        return FORMAS[st.getValue(VELAS) - 1];
    }

    @Override
    public boolean canBeReplaced(BlockState st, BlockPlaceContext c) {
        return !c.isSecondaryUseActive() && c.getItemInHand().getItem() == asItem() && st.getValue(VELAS) < 4
                || super.canBeReplaced(st, c);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext c) {
        BlockState ya = c.getLevel().getBlockState(c.getClickedPos());
        if (ya.is(this)) return ya.setValue(VELAS, Math.min(4, ya.getValue(VELAS) + 1));
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

    @Override
    public void animateTick(BlockState st, Level nivel, BlockPos pos, RandomSource r) {
        if (r.nextInt(6) == 0)
            nivel.addParticle(Fuegos.HUMO.get(), pos.getX() + 0.3 + r.nextDouble() * 0.4, pos.getY() + 0.75,
                    pos.getZ() + 0.3 + r.nextDouble() * 0.4, 0.0, 0.015, 0.0);
    }

    /** La luz de las velas: 3 por vela. */
    public static int luz(BlockState st) {
        return 3 * st.getValue(VELAS);
    }
}
