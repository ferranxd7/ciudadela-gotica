package com.darkcraft.bloques;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Lo que se rompe (parte 8k): jarrones, anforas y urnas de huesos. Se rompen de un golpe de arma (lo llama el combate: en
 * aventura no llega el clic de romper bloques), de un flechazo o de cualquier proyectil, y al romperse sueltan lo de su botin
 * (muchas veces nada; flechas, un fruto o, rara vez, una piedra de forja), nunca a si mismos.
 */
public class Rompible extends Block {
    private final VoxelShape forma;

    public Rompible(Properties p, VoxelShape forma) {
        super(p);
        this.forma = forma;
    }

    /** La forma de cada uno: el anfora (con sus asas) es mas alta y ancha. */
    public static VoxelShape forma(String id) {
        return id.equals("jarron_grande") ? Block.box(2, 0, 2, 14, 16, 14) : Block.box(3, 0, 3, 13, 11, 13);
    }

    @Override
    public VoxelShape getShape(BlockState st, BlockGetter l, BlockPos pos, CollisionContext c) {
        return forma;
    }

    @Override
    public void onProjectileHit(Level l, BlockState st, BlockHitResult hit, Projectile pr) {
        if (l instanceof ServerLevel sl && pr.mayInteract(l, hit.getBlockPos())) sl.destroyBlock(hit.getBlockPos(), true, pr.getOwner());
    }

    /** Un golpe con arma (lo llama el combate): si se mira de cerca a algo que se rompe, se rompe (con su botin). */
    public static void golpe(ServerPlayer p) {
        Vec3 ojo = p.getEyePosition(), fin = ojo.add(p.getLookAngle().scale(4.0));
        BlockHitResult hit = p.level().clip(new ClipContext(ojo, fin, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, p));
        if (hit.getType() != HitResult.Type.BLOCK) return;
        BlockPos pos = hit.getBlockPos();
        if (p.level().getBlockState(pos).getBlock() instanceof Rompible) p.serverLevel().destroyBlock(pos, true, p);
    }
}
