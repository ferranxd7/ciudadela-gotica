package com.darkcraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Las nubes del mundo de nubes (parte 3a): translucidas, sin caras entre nube y nube (se ve el bulto, no los
 * cubos) y la luz del cielo pasa. {@code nube} no se pisa (se cae a traves, al vacio); {@code nube_firme} si (los
 * caminos).
 */
public class NubeBlock extends Block {
    private final boolean firme;

    public NubeBlock(Properties p, boolean firme) {
        super(p);
        this.firme = firme;
    }

    @Override
    public boolean skipRendering(BlockState st, BlockState otro, Direction d) {
        return otro.getBlock() instanceof NubeBlock || super.skipRendering(st, otro, d);
    }

    /** La nube de verdad no se puede senalar (ni sale su recuadro al mirarla, ni tapa lo que hay detras al apuntar). */
    @Override
    public VoxelShape getShape(BlockState st, BlockGetter l, BlockPos p, CollisionContext c) {
        return firme ? Shapes.block() : Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState st, BlockGetter l, BlockPos p, CollisionContext c) {
        return Shapes.empty();
    }

    @Override
    public float getShadeBrightness(BlockState st, BlockGetter l, BlockPos p) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState st, BlockGetter l, BlockPos p) {
        return !firme;
    }
}
