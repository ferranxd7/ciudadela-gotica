package com.darkcraft.bloques;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Una arana (lampara de techo) de ocho velas que cuelga de sus cadenas (parte 8g), o la jaula colgante (8i): necesita algo
 *  encima (techo o cadena). */
public class Arana extends Block {
    private static final VoxelShape FORMA = Block.box(2.0, 3.0, 2.0, 14.0, 16.0, 14.0);
    private final VoxelShape forma;

    public Arana(Properties p) {
        this(p, FORMA);
    }

    /** Lo que cuelga con otra forma (la jaula colgante de la parte 8i). */
    public Arana(Properties p, VoxelShape forma) {
        super(p);
        this.forma = forma;
    }

    @Override
    public VoxelShape getShape(BlockState st, BlockGetter nivel, BlockPos pos, CollisionContext c) {
        return forma;
    }

    @Override
    public boolean canSurvive(BlockState st, LevelReader nivel, BlockPos pos) {
        BlockState arriba = nivel.getBlockState(pos.above());
        return arriba.getBlock() instanceof ChainBlock || Block.canSupportCenter(nivel, pos.above(), Direction.DOWN);
    }

    @Override
    public BlockState updateShape(BlockState st, Direction d, BlockState vecino, LevelAccessor nivel, BlockPos pos, BlockPos vecinoPos) {
        return d == Direction.UP && !st.canSurvive(nivel, pos) ? Blocks.AIR.defaultBlockState() : st;
    }
}
