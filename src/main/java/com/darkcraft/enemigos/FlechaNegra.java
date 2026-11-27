package com.darkcraft.enemigos;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * La flecha negra de los cazadores marcados (parte 7u): una flecha de las suyas (pasa entre los enemigos) untada de
 * sombra: deja un rastro oscuro (el de las flechas con efectos del juego, del color de lo que lleva) y a quien da le
 * nubla la vista ({@code DARKNESS}) y le debilita un rato.
 */
public class FlechaNegra extends Flecha {
    public FlechaNegra(Level nivel, LivingEntity tirador) {
        super(nivel, tirador);
        addEffect(new MobEffectInstance(MobEffects.DARKNESS, 130, 0));
        addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 90, 0));
    }

    @Override
    protected void doPostHurtEffects(LivingEntity le) {
        super.doPostHurtEffects(le);
        Marcado.NUBLADOS++;
        if (level() instanceof ServerLevel sl)
            sl.sendParticles(Marcado.SOMBRA, le.getX(), le.getY() + le.getBbHeight() * 0.6, le.getZ(), 24, 0.3, 0.4, 0.3, 0.02);
    }

    @Override
    public void tick() {
        super.tick();
        // el rastro de sombra mientras vuela (el servidor lo manda: el cliente solo ve una flecha con efectos)
        if (!inGround && tickCount % 2 == 0 && level() instanceof ServerLevel sl)
            sl.sendParticles(Marcado.SOMBRA, getX(), getY(), getZ(), 2, 0.05, 0.05, 0.05, 0.0);
    }
}
