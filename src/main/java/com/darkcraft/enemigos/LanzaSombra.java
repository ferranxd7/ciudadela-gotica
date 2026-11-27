package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Las lanzas de sombra de la Sombra sin Nombre (parte 7ac): van rectas y deprisa, casi sin caer, dejando un rastro negro y rojo; a
 * quien dan le hacen dano de sombra y le quitan fuerzas un momento. Al dar en algo se deshacen. Lo que se ve es su objeto
 * ({@code lanza_sombra}). A los enemigos no les dan.
 */
public class LanzaSombra extends ThrowableItemProjectile {
    static final DustParticleOptions NEGRO = new DustParticleOptions(new Vector3f(0.05f, 0.02f, 0.06f), 1.2f),
            ROJO = new DustParticleOptions(new Vector3f(0.8f, 0.05f, 0.05f), 0.7f);
    private float dano = 6f;
    private int edad;
    /** Para la prueba: las que han dado a alguien. */
    public static int DAN;

    public LanzaSombra(EntityType<? extends LanzaSombra> t, Level l) {
        super(t, l);
    }

    public LanzaSombra(Level l, LivingEntity dueno, float dano) {
        super(DarkCraftMod.LANZA_SOMBRA.get(), dueno, l);
        setItem(new ItemStack(DarkCraftMod.LANZA_SOMBRA_OBJETO.get()));
        this.dano = dano;
    }

    @Override
    protected Item getDefaultItem() {
        return DarkCraftMod.LANZA_SOMBRA_OBJETO.get();
    }

    @Override
    protected float getGravity() {
        return 0.006f;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            Vec3 v = getDeltaMovement();
            level().addParticle(NEGRO, getX() - v.x * 0.5, getY() - v.y * 0.5, getZ() - v.z * 0.5, 0, 0, 0);
            if (random.nextInt(2) == 0) level().addParticle(ROJO, getX(), getY(), getZ(), 0, 0, 0);
            return;
        }
        if (++edad > 70) discard();
    }

    @Override
    protected boolean canHitEntity(Entity e) {
        return super.canHitEntity(e) && !(e instanceof Enemigo);
    }

    @Override
    protected void onHitEntity(EntityHitResult r) {
        if (level().isClientSide) return;
        if (!(r.getEntity() instanceof LivingEntity le)) return;
        if (le.hurt(damageSources().indirectMagic(this, getOwner()), dano)) {
            DAN++;
            le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, true));
        }
    }

    @Override
    protected void onHit(HitResult r) {
        super.onHit(r);
        if (level().isClientSide) return;
        Vec3 p = r.getLocation();
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(NEGRO, p.x, p.y, p.z, 14, 0.25, 0.25, 0.25, 0.05);
            sl.sendParticles(ParticleTypes.SMOKE, p.x, p.y, p.z, 4, 0.1, 0.1, 0.1, 0.02);
            sl.playSound(null, p.x, p.y, p.z, DarkCraftMod.SFX_SOMBRA_NIEBLA.get(), SoundSource.HOSTILE, 0.5f, 1.6f + random.nextFloat() * 0.2f);
        }
        discard();
    }
}
