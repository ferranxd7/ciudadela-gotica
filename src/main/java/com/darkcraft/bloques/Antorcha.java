package com.darkcraft.bloques;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Las antorchas propias (parte 8g), de pie y de pared: la llama va en el modelo (animada) y alrededor salen las
 * particulas del mod (nunca las de Minecraft; la que se pasa al juego no se usa).
 */
public class Antorcha extends TorchBlock {
    private final String color;

    public Antorcha(Properties p, String color) {
        super(p, ParticleTypes.FLAME);
        this.color = color;
    }

    @Override
    public void animateTick(BlockState st, Level nivel, BlockPos pos, RandomSource r) {
        Fuegos.avivar(nivel, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 0.15, color, r, r.nextInt(2), true, false);
    }

    /** La antorcha de pared. */
    public static class Pared extends WallTorchBlock {
        private final String color;

        public Pared(Properties p, String color) {
            super(p, ParticleTypes.FLAME);
            this.color = color;
        }

        @Override
        public void animateTick(BlockState st, Level nivel, BlockPos pos, RandomSource r) {
            Direction d = st.getValue(FACING).getOpposite();
            Fuegos.avivar(nivel, pos.getX() + 0.5 + 0.27 * d.getStepX(), pos.getY() + 0.92, pos.getZ() + 0.5 + 0.27 * d.getStepZ(),
                    0.15, color, r, r.nextInt(2), true, false);
        }
    }
}
