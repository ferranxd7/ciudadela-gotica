package com.darkcraft.gavran;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Raiz de Gavran (darkcraft:raiz_gavran): corteza casi negra con venas de savia verde. Se ve en las estacas de sus
 * ataques (entidades block_display) y, puesta de verdad, son los muros de corteza del Juicio de las espinas: se
 * deshace sola un rato despues de crecer (aunque se cierre el mundo a mitad: el aviso queda guardado en el chunk).
 */
public class RaizBlock extends Block {
    /** Lo que dura un muro de corteza (ticks desde que crece cada bloque). */
    public static final int VIDA = 190;

    public RaizBlock(Properties p) {
        super(p);
    }

    @Override
    public void onPlace(BlockState st, Level lvl, BlockPos pos, BlockState old, boolean moving) {
        super.onPlace(st, lvl, pos, old, moving);
        if (!lvl.isClientSide && !old.is(this)) lvl.scheduleTick(pos, this, VIDA + lvl.random.nextInt(12));
    }

    @Override
    public void tick(BlockState st, ServerLevel sl, BlockPos pos, RandomSource r) {
        sl.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, DarkCraftMod.RAIZ_GAVRAN.get().defaultBlockState()),
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.1);
        if (r.nextInt(6) == 0)
            sl.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.6f, 0.6f + r.nextFloat() * 0.2f);
    }
}
