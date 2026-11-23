package com.darkcraft.fortaleza;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * El portal de la Sombra, en el arco de lo alto de la Fortaleza de Hueso: un remolino rojo y negro que no se puede
 * cruzar todavia (quien lo toca sale despedido hacia atras: "el umbral aun no te conoce").
 */
public class PortalSombraBlock extends HalfTransparentBlock {
    private static final VoxelShape FORMA = net.minecraft.world.level.block.Block.box(0, 0, 5, 16, 16, 11);

    public PortalSombraBlock(Properties p) {
        super(p);
    }

    @Override
    public VoxelShape getShape(BlockState st, BlockGetter lvl, BlockPos pos, CollisionContext ctx) {
        return FORMA;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState st, BlockGetter lvl, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }

    @Override
    public void entityInside(BlockState st, Level lvl, BlockPos pos, Entity e) {
        if (!lvl.isClientSide) FortalezaEventos.tocarPortal(lvl, pos, e);
    }

    @Override
    public void animateTick(BlockState st, Level lvl, BlockPos pos, RandomSource r) {
        double x = pos.getX() + r.nextDouble(), y = pos.getY() + r.nextDouble(), z = pos.getZ() + 0.5 + (r.nextDouble() - 0.5) * 0.5;
        if (r.nextInt(3) == 0) lvl.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0, 0.015, (r.nextDouble() - 0.5) * 0.04);
        if (r.nextInt(2) == 0) lvl.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, 0, 0.02, 0);
        if (r.nextInt(5) == 0) lvl.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0.03, 0);
        if (r.nextInt(160) == 0)
            lvl.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.35f,
                    0.45f + r.nextFloat() * 0.1f, false);
    }
}
