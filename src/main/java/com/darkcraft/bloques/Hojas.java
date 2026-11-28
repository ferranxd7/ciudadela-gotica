package com.darkcraft.bloques;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Las hojas de los arboles nuevos (parte 8e): como las del juego (con huecos, se ven las de detras, no tapan la vista ni
 * ahogan), pero no se caen nunca: no hacen la cuenta de la distancia al tronco (los arboles grandes tienen hojas muy lejos
 * de sus troncos) ni tienen ticks al azar.
 */
public class Hojas extends LeavesBlock {
    public Hojas(Properties p) {
        super(p);
    }

    @Override
    public boolean isRandomlyTicking(BlockState st) {
        return false;
    }

    @Override
    public void randomTick(BlockState st, ServerLevel nivel, BlockPos pos, RandomSource azar) {
    }

    @Override
    public void tick(BlockState st, ServerLevel nivel, BlockPos pos, RandomSource azar) {
    }

    @Override
    public BlockState updateShape(BlockState st, Direction d, BlockState vecino, LevelAccessor nivel, BlockPos pos, BlockPos vecinoPos) {
        if (st.getValue(WATERLOGGED)) nivel.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(nivel));
        return st;
    }
}
