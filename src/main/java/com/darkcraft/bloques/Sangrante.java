package com.darkcraft.bloques;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * La piedra sangrante (parte 8k): por sus caras le corre sangre (el dibujo) y del borde de abajo de cada cara libre, y de
 * debajo si no hay nada, caen gotas ({@link Fuegos#GOTA_SANGRE}: cuelgan un momento, engordan y caen).
 */
public class Sangrante extends Block {
    public Sangrante(Properties p) {
        super(p);
    }

    @Override
    public void animateTick(BlockState st, Level l, BlockPos pos, RandomSource r) {
        if (r.nextInt(3) != 0) return;
        int cara = r.nextInt(5);
        double x, y = pos.getY() - 0.02, z;
        if (cara == 4) {
            if (!l.getBlockState(pos.below()).isAir()) return;
            x = pos.getX() + 0.15 + r.nextDouble() * 0.7;
            z = pos.getZ() + 0.15 + r.nextDouble() * 0.7;
        } else {
            Direction d = Direction.from2DDataValue(cara);
            if (l.getBlockState(pos.relative(d)).isSolidRender(l, pos.relative(d))) return;
            double a = 0.1 + r.nextDouble() * 0.8;
            x = pos.getX() + 0.5 + d.getStepX() * 0.53 + (d.getStepX() == 0 ? a - 0.5 : 0);
            z = pos.getZ() + 0.5 + d.getStepZ() * 0.53 + (d.getStepZ() == 0 ? a - 0.5 : 0);
            y = pos.getY() + 0.04;
        }
        l.addParticle(Fuegos.GOTA_SANGRE.get(), x, y, z, 0, 0, 0);
    }
}
