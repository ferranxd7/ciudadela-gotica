package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * El virote de los ballesteros Insomnes del Fuerte de la Marca (parte 7aw): una saeta pesada de ballesta de torno. Pasa entre los
 * suyos (es una {@link Flecha}) y su dano es de un tipo propio ({@code darkcraft:virote}, en {@code #minecraft:bypasses_shield}):
 * <b>el escudo no lo para</b>. Pega fuerte ({@link #dano}), empuja y deja al que da un instante mas lento; no se recoge. Se ve como
 * una flecha del juego (con el rastro de chispas de un tiro critico).
 */
public class Virote extends Flecha {
    public static final ResourceKey<DamageType> TIPO = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(DarkCraftMod.MODID, "virote"));
    /** Para las pruebas: los que han dado, los que han dado a alguien que se cubria con un escudo y el ultimo dano. */
    public static int DAN, A_ESCUDOS;
    public static float ULTIMO;

    private final float dano;

    public Virote(Level nivel, LivingEntity tirador, float dano) {
        super(nivel, tirador);
        this.dano = dano;
        pickup = AbstractArrow.Pickup.DISALLOWED;
        setCritArrow(true);
        setBaseDamage(0.1);
    }

    /** Lo que quita (antes de la armadura). */
    public float dano() {
        return dano;
    }

    @Override
    protected void onHitEntity(EntityHitResult r) {
        Entity e = r.getEntity();
        if (level().isClientSide) return;
        Entity dueno = getOwner();
        DamageSource src = new DamageSource(level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TIPO), this,
                dueno == null ? this : dueno);
        boolean cubria = e instanceof LivingEntity le && le.isBlocking();
        if (e.hurt(src, dano)) {
            DAN++;
            ULTIMO = dano;
            if (cubria) A_ESCUDOS++;
            if (e instanceof LivingEntity le) {
                Vec3 v = getDeltaMovement().multiply(1, 0, 1);
                if (v.lengthSqr() > 1.0e-6) {
                    v = v.normalize();
                    le.knockback(0.9, -v.x, -v.z);
                }
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1), dueno instanceof LivingEntity d ? d : null);
            }
            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CRIT, e.getX(), e.getY() + e.getBbHeight() * 0.6, e.getZ(), 14, 0.25, 0.3, 0.25, 0.3);
                sl.playSound(null, e.getX(), e.getY() + 1, e.getZ(), DarkCraftMod.SFX_VIROTE_IMPACTO.get(), SoundSource.HOSTILE, 1.2f,
                        0.9f + random.nextFloat() * 0.2f);
            }
            discard();
        } else {
            // no ha entrado (alguien invulnerable): rebota y cae
            setDeltaMovement(getDeltaMovement().scale(-0.1));
            setYRot(getYRot() + 180f);
            yRotO += 180f;
        }
    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult r) {
        super.onHitBlock(r);
        // se clava con su golpe seco (y no se queda: no se recoge y se va al rato)
        if (!level().isClientSide && level() instanceof ServerLevel sl)
            sl.playSound(null, getX(), getY(), getZ(), DarkCraftMod.SFX_VIROTE_IMPACTO.get(), SoundSource.HOSTILE, 0.7f, 1.2f + random.nextFloat() * 0.15f);
    }

    @Override
    public void tick() {
        super.tick();
        // los clavados se van antes que las flechas del juego (un minuto)
        if (!level().isClientSide && inGround && inGroundTime > 200) discard();
    }
}
