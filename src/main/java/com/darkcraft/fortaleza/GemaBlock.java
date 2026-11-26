package com.darkcraft.fortaleza;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Las gemas rojas del marco del portal de la Sombra (parte 5e, respuesta 52 del plan): apagadas hasta que el amuleto abre
 * el portal; entonces se encienden (con luz propia y alumbran).
 */
public class GemaBlock extends Block {
    public static final BooleanProperty LUZ = BooleanProperty.create("luz");

    public GemaBlock(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(LUZ, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(LUZ);
    }
}
