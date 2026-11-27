package com.darkcraft.enemigos;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Un charco de sangre (parte 7t): lo que va dejando Anselmo, el Pontifice Sangrante, desde que se unge a media vida (sangra
 * por donde pisa). Dura {@link #DURA} ticks y se va secando (encoge); a quien lo pisa (los jugadores y la presa del Pontifice,
 * no los penitentes) le hace sangrar ({@link Sangrado}) y le frena un poco. Como el humo del incienso: una nube de efectos del
 * juego que no lleva ninguno, con su propio tick (en el cliente, una nube cualquiera que echa gotas de sangre).
 */
public class CharcoSangre extends AreaEffectCloud {
    public static final float RADIO = 1.8f;
    public static final int DURA = 220;
    /** Lo que suma de sangre cada medio segundo a quien lo pisa. */
    public static final float SANGRE = 9f;

    /** Para las pruebas: las veces que ha hecho sangrar. */
    public static int SANGRAN;

    private final LivingEntity dueno;

    public CharcoSangre(Level l, double x, double y, double z, LivingEntity dueno) {
        super(l, x, y, z);
        this.dueno = dueno;
        if (dueno != null) setOwner(dueno);
        setRadius(RADIO);
        setRadiusOnUse(0f);
        setWaitTime(0);
        setDuration(DURA);
        setRadiusPerTick(-RADIO * 0.45f / DURA);
        setParticle(Sangrado.GOTA);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel sl) || isRemoved()) return;
        float r = getRadius();
        if (tickCount % 4 == 0) {
            sl.sendParticles(Sangrado.CHORRO, getX(), getY() + 0.06, getZ(), 4, r * 0.45, 0.0, r * 0.45, 0.0);
            sl.sendParticles(ParticleTypes.DRIPPING_DRIPSTONE_LAVA, getX(), getY() + 0.1, getZ(), 1, r * 0.4, 0.0, r * 0.4, 0.0);
        }
        if (tickCount % 10 != 0) return;
        for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, new AABB(getX() - r, getY() - 0.5, getZ() - r, getX() + r, getY() + 1.2, getZ() + r),
                LivingEntity::isAlive)) {
            if (le instanceof Enemigo || le == dueno) continue;
            if (le.distanceToSqr(getX(), le.getY(), getZ()) > r * r || !le.onGround()) continue;
            if (le instanceof Player p && (p.isCreative() || p.isSpectator())) continue;
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0, false, false), dueno);
            Sangrado.sumar(le, SANGRE, dueno);
            SANGRAN++;
        }
    }
}
