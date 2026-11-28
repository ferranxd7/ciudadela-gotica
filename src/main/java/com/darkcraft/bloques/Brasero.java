package com.darkcraft.bloques;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Un brasero de hierro negro con el fuego de su color en el cuenco (parte 8g). */
public class Brasero extends Block {
    private static final VoxelShape FORMA = Block.box(1.0, 0.0, 1.0, 15.0, 13.0, 15.0);
    private final String color;

    public Brasero(Properties p, String color) {
        super(p);
        this.color = color;
    }

    @Override
    public VoxelShape getShape(BlockState st, BlockGetter nivel, BlockPos pos, CollisionContext c) {
        return FORMA;
    }

    @Override
    public void animateTick(BlockState st, Level nivel, BlockPos pos, RandomSource r) {
        Fuegos.avivar(nivel, pos.getX() + 0.5, pos.getY() + 0.85, pos.getZ() + 0.5, 0.6, color, r, 2, true, true);
    }
}
