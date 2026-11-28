package com.darkcraft.bloques;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Un racimo de hojas (parte 8e): tres o cuatro bultos de hojas de distinto tamano en vez de un cubo, para las puntas de
 * las ramas y el borde de las copas (asi la copa no se ve cuadrada). Se choca con su caja (casi un bloque) y deja pasar la
 * luz y la vista como las hojas.
 */
public class Racimo extends Block {
    private static final VoxelShape FORMA = Block.box(1.0, 1.0, 1.0, 15.0, 15.0, 15.0);

    public Racimo(Properties p) {
        super(p);
    }

    @Override
    public VoxelShape getShape(BlockState st, BlockGetter nivel, BlockPos pos, CollisionContext c) {
        return FORMA;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState st, BlockGetter nivel, BlockPos pos) {
        return true;
    }
}
