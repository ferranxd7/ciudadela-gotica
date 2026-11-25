package com.darkcraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Las nubes del mundo de nubes (parte 3a): translucidas, sin caras entre nube y nube (se ve el bulto, no los
 * cubos) y la luz del cielo pasa. {@code nube} no se pisa (se cae a traves, al vacio); {@code nube_firme} si (los
 * caminos).
 * <p>
 * La nube lleva un {@link #TONO} (0-3) segun lo honda que esta bajo lo alto de su columna: arriba casi blanca, abajo
 * gris lila. El modelo va sin sombreado por caras ("shade": false): el volumen de los bollos lo dan los tonos (las
 * paredes de un escalon se oscurecen hacia abajo y por debajo el mar de nubes es mas oscuro), no las caras de los cubos.
 */
public class NubeBlock extends Block {
    public static final IntegerProperty TONO = IntegerProperty.create("tono", 0, 3);

    private final boolean firme;

    public NubeBlock(Properties p, boolean firme) {
        super(p);
        this.firme = firme;
        registerDefaultState(stateDefinition.any().setValue(TONO, 0));
    }

    /** El tono de una nube que esta {@code hondo} bloques por debajo de lo alto de su columna. */
    public static int tono(int hondo) {
        return hondo <= 0 ? 0 : hondo <= 2 ? 1 : hondo <= 5 ? 2 : 3;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(TONO);
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
