package com.darkcraft.santuario;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * El santuario: una hoguera de almas (un brasero de piedra y garras de hueso con fuego azul palido; modelo propio, en
 * tools/santuario). Apagado, quien llega lo enciende; encendido, se descansa en el: cura, rellena los frascos, deja alli
 * el punto de reaparicion y abre su menu (subir de nivel, repartir los frascos). No se puede romper. Lo que hace esta en
 * {@link Santuarios}; la llama, las chispas y su rumor, en el cliente (client/ui/ClientSantuario).
 */
public class SantuarioBlock extends Block {
    public static final BooleanProperty ENCENDIDO = BooleanProperty.create("encendido");
    /** El pie, el fuste y el cuenco (las garras no chocan: se quedan dentro de la caja del cuenco). */
    private static final VoxelShape FORMA = Shapes.or(Block.box(1, 0, 1, 15, 4, 15), Block.box(4, 4, 4, 12, 9, 12), Block.box(2, 9, 2, 14, 13, 14));

    public SantuarioBlock(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(ENCENDIDO, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(ENCENDIDO);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext c) {
        return defaultBlockState();
    }

    @Override
    public VoxelShape getShape(BlockState st, BlockGetter l, BlockPos p, CollisionContext c) {
        return FORMA;
    }

    @Override
    public InteractionResult use(BlockState st, Level lvl, BlockPos pos, Player p, InteractionHand hand, BlockHitResult hit) {
        if (lvl.isClientSide) return InteractionResult.SUCCESS;
        if (p instanceof ServerPlayer sp) Santuarios.usar(sp, pos);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isPathfindable(BlockState st, BlockGetter l, BlockPos p, PathComputationType t) {
        return false;
    }
}
