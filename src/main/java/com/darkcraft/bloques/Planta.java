package com.darkcraft.bloques;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Las plantas de los bloques nuevos (parte 8d): hierba alta, helechos, matorrales, flores, hongos y juncos. Una cruz con
 * transparencia, sin choque, que se mueve un poco de sitio en cada bloque (como la hierba del juego) y que se pone sobre
 * cualquier cara de arriba firme, tierra o arena (las del mod llevan las etiquetas {@code minecraft:dirt} y {@code sand}).
 */
public class Planta extends BushBlock {
    private static final VoxelShape FORMA = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

    public Planta(Properties p) {
        super(p);
    }

    @Override
    public VoxelShape getShape(BlockState st, BlockGetter nivel, BlockPos pos, CollisionContext c) {
        Vec3 o = st.getOffset(nivel, pos);
        return FORMA.move(o.x, o.y, o.z);
    }

    @Override
    protected boolean mayPlaceOn(BlockState st, BlockGetter nivel, BlockPos pos) {
        return st.is(BlockTags.DIRT) || st.is(BlockTags.SAND) || st.isFaceSturdy(nivel, pos, Direction.UP);
    }
}
