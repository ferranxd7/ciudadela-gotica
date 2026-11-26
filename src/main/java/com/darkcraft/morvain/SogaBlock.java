package com.darkcraft.morvain;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * La soga de la Arboleda de los Ahorcados (parte 7g): canamo viejo que cuelga de las ramas (modelos de
 * tools/morvain/soga.py). {@code parte}: la cuerda que sigue, el cabo deshilachado donde se acaba o el lazo vacio del
 * final. No se choca con ella ni se rompe; de los ahorcados que aun cuelgan, la soga de bloques llega hasta la de su
 * modelo ({@code enemigos/Ahorcado}).
 */
public class SogaBlock extends Block {
    public enum Parte implements StringRepresentable {
        CUERDA("cuerda"), CABO("cabo"), LAZO("lazo");

        private final String nombre;

        Parte(String n) {
            nombre = n;
        }

        @Override
        public String getSerializedName() {
            return nombre;
        }
    }

    public static final EnumProperty<Parte> PARTE = EnumProperty.create("parte", Parte.class);
    private static final VoxelShape CUERDA = Block.box(6.5, 0, 6.5, 9.5, 16, 9.5);
    private static final VoxelShape CABO = Block.box(6.5, 2, 6.5, 9.5, 16, 9.5);
    private static final VoxelShape LAZO = Shapes.or(Block.box(6.5, 6, 6.5, 9.5, 16, 9.5), Block.box(5, 0.5, 7, 11, 6.5, 9));

    public SogaBlock(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(PARTE, Parte.CUERDA));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(PARTE);
    }

    @Override
    public VoxelShape getShape(BlockState st, BlockGetter l, BlockPos p, CollisionContext c) {
        return switch (st.getValue(PARTE)) {
            case CABO -> CABO;
            case LAZO -> LAZO;
            default -> CUERDA;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState st, BlockGetter l, BlockPos p, CollisionContext c) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState st, BlockGetter l, BlockPos p) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState st, BlockGetter l, BlockPos p, PathComputationType t) {
        return true;
    }
}
