package com.darkcraft.enemigos;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

/**
 * El humo del incienso de sangre del turiferario (parte 7s): una nube roja y espesa que dura {@link #DURA} ticks. A quien
 * este dentro (los jugadores y la presa del turiferario) le hace sangrar ({@link Sangrado}) y le frena; a los penitentes
 * que esten dentro les ampara (aguantan mas). Como las llamas del cantaro: una nube de efectos del juego que no lleva
 * ninguno, con su propio tick (en el cliente es una nube cualquiera que echa particulas rojas).
 */
public class Incienso extends AreaEffectCloud {
    public static final float RADIO = 3.0f;
    public static final int DURA = 120;
    /** Lo que suma de sangre cada medio segundo a quien este dentro. */
    public static final float SANGRE = 12f;
    public static final DustParticleOptions HUMO = new DustParticleOptions(new Vector3f(0.42f, 0.05f, 0.07f), 2.2f);

    /** Para las pruebas: las veces que ha hecho sangrar y las que ha amparado a un penitente. */
    public static int SANGRAN, AMPARA;

    private final Penitente dueno;

    public Incienso(Level l, double x, double y, double z, Penitente dueno) {
        super(l, x, y, z);
        this.dueno = dueno;
        if (dueno != null) setOwner(dueno);
        setRadius(RADIO);
        setRadiusOnUse(0f);
        setWaitTime(0);
        setDuration(DURA);
        setRadiusPerTick(-RADIO * 0.25f / DURA);
        setParticle(HUMO);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel sl) || isRemoved()) return;
        float r = getRadius();
        if (tickCount % 3 == 0) {
            sl.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.6, getZ(), 6, r * 0.5, 0.4, r * 0.5, 0.01);
            sl.sendParticles(HUMO, getX(), getY() + 1.2, getZ(), 5, r * 0.45, 0.6, r * 0.45, 0.0);
        }
        if (tickCount % 10 != 0) return;
        for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, new AABB(getX() - r, getY() - 0.5, getZ() - r, getX() + r, getY() + 2.5, getZ() + r),
                LivingEntity::isAlive)) {
            if (le.distanceToSqr(getX(), le.getY(), getZ()) > r * r) continue;
            if (le instanceof Penitente p) {
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 0, false, false));
                AMPARA++;
                continue;
            }
            if (le instanceof Enemigo) continue;
            if (le instanceof Player pl) {
                if (pl.isCreative() || pl.isSpectator()) continue;
            } else if (dueno == null || dueno.getTarget() != le) continue;
            Sangrado.sumar(le, SANGRE, dueno);
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0), dueno);
            SANGRAN++;
        }
    }
}
