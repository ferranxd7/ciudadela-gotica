package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * El orbe del maestro de la Academia (parte 7o): una bola de luz lila (solo particulas) que vuela despacio y va girando
 * hacia su presa (se esquiva apartandose a tiempo o poniendo algo en medio). Al dar, dano magico. A los enemigos no les da.
 */
public class Orbe extends ThrowableProjectile {
    public static final double VELOCIDAD = 0.55;
    /** Lo que gira cada tick hacia su presa (0-1) y lo que dura. */
    static final double GIRO = 0.1;
    static final int VIDA = 90;
    private LivingEntity presa;
    private float dano = 5f;
    private int edad;
    /** Para la prueba: los que han dado. */
    public static int DAN;

    public Orbe(EntityType<? extends Orbe> t, Level l) {
        super(t, l);
    }

    public Orbe(Level l, LivingEntity dueno, LivingEntity presa, float dano) {
        super(DarkCraftMod.ORBE.get(), dueno, l);
        this.presa = presa;
        this.dano = dano;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected float getGravity() {
        return 0f;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), 0, 0, 0);
            level().addParticle(Erudito.VIOLETA, getX() + (random.nextDouble() - 0.5) * 0.3, getY() + (random.nextDouble() - 0.5) * 0.3,
                    getZ() + (random.nextDouble() - 0.5) * 0.3, 0, 0, 0);
            if (random.nextInt(3) == 0) level().addParticle(ParticleTypes.WITCH, getX(), getY(), getZ(), 0, 0, 0);
            return;
        }
        if (++edad > VIDA) {
            apagarse();
            return;
        }
        // gira despacio hacia su presa (a la altura del pecho)
        if (presa != null && presa.isAlive() && !presa.isRemoved()) {
            Vec3 quiere = presa.position().add(0, presa.getBbHeight() * 0.6, 0).subtract(position()).normalize();
            Vec3 v = getDeltaMovement().normalize().scale(1 - GIRO).add(quiere.scale(GIRO));
            setDeltaMovement(v.normalize().scale(VELOCIDAD));
        }
    }

    private void apagarse() {
        if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.WITCH, getX(), getY(), getZ(), 8, 0.2, 0.2, 0.2, 0.02);
        discard();
    }

    @Override
    protected boolean canHitEntity(Entity e) {
        return super.canHitEntity(e) && !(e instanceof Enemigo);
    }

    @Override
    protected void onHitEntity(EntityHitResult r) {
        if (level().isClientSide) return;
        if (r.getEntity() instanceof LivingEntity le && le.hurt(damageSources().indirectMagic(this, getOwner()), dano)) DAN++;
    }

    @Override
    protected void onHit(HitResult r) {
        super.onHit(r);
        if (level().isClientSide) return;
        Vec3 p = r.getLocation();
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 14, 0.2, 0.2, 0.2, 0.08);
            sl.sendParticles(Erudito.VIOLETA, p.x, p.y, p.z, 12, 0.3, 0.3, 0.3, 0.0);
            sl.playSound(null, p.x, p.y, p.z, DarkCraftMod.SFX_HECHIZO_IMPACTO.get(), SoundSource.HOSTILE, 0.9f, 1.2f);
        }
        discard();
    }
}
