package com.darkcraft.bloques;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/**
 * Los muebles (parte 8h): un bloque con su forma (la del modelo, en cajas) que, si {@code girado}, mira hacia quien lo pone
 * (el modelo y la forma se escriben mirando al norte y se giran).
 */
public class Mueble extends HorizontalDirectionalBlock {
    private final boolean girado;
    private final Map<Direction, VoxelShape> formas = new EnumMap<>(Direction.class);

    public Mueble(Properties p, VoxelShape norte, boolean girado) {
        super(p);
        this.girado = girado;
        for (Direction d : Direction.Plane.HORIZONTAL) formas.put(d, girado ? girar(norte, d) : norte);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState st, BlockGetter nivel, BlockPos pos, CollisionContext c) {
        return formas.get(st.getValue(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext c) {
        return defaultBlockState().setValue(FACING, girado ? c.getHorizontalDirection().getOpposite() : Direction.NORTH);
    }

    /** Una forma escrita mirando al norte, girada como el modelo (y 90 = este, 180 = sur, 270 = oeste). */
    public static VoxelShape girar(VoxelShape norte, Direction d) {
        if (d == Direction.NORTH) return norte;
        VoxelShape out = Shapes.empty();
        for (AABB a : norte.toAabbs()) {
            double[] p = rot(a.minX, a.minZ, d), q = rot(a.maxX, a.maxZ, d);
            out = Shapes.or(out, Shapes.box(Math.min(p[0], q[0]), a.minY, Math.min(p[1], q[1]), Math.max(p[0], q[0]), a.maxY,
                    Math.max(p[1], q[1])));
        }
        return out.optimize();
    }

    private static double[] rot(double x, double z, Direction d) {
        return switch (d) {
            case EAST -> new double[]{1 - z, x};
            case SOUTH -> new double[]{1 - x, 1 - z};
            case WEST -> new double[]{z, 1 - x};
            default -> new double[]{x, z};
        };
    }

    /** Las formas de los muebles (mirando al norte). */
    public static VoxelShape forma(String tipo) {
        return switch (tipo) {
            case "m" -> Shapes.or(Block.box(0, 13, 0, 16, 16, 16), Block.box(1, 0, 1, 3, 13, 3), Block.box(13, 0, 1, 15, 13, 3),
                    Block.box(1, 0, 13, 3, 13, 15), Block.box(13, 0, 13, 15, 13, 15));
            case "h" -> Shapes.or(Block.box(3, 0, 3, 13, 9, 13), Block.box(3, 9, 11, 13, 16, 13));
            case "b" -> Block.box(0, 0, 4, 16, 8, 12);
            case "k" -> Block.box(4, 0, 4, 12, 10, 12);
            case "j" -> Shapes.or(Block.box(0, 0, 1, 16, 4, 15), Block.box(1, 4, 10.5, 15, 6, 14.5));
            case "r" -> Block.box(3, 0, 3, 13, 15, 13);
            default -> Shapes.block();
        };
    }
}
