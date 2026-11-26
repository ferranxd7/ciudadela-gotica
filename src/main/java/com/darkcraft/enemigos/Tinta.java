package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * El tintero que lanza el aprendiz de la Academia (parte 7o): vuela en arco y revienta en una mancha de tinta que ciega y
 * frena un momento a quien este al lado (y le duele un poco). A los enemigos no les da.
 */
public class Tinta extends ThrowableItemProjectile {
    public static final float DANO = 2.0f;
    public static final double R = 1.8;
    private int edad;
    /** Para la prueba: los que han reventado y a cuantos han cegado. */
    public static int REVIENTAN, CIEGAN;

    public Tinta(EntityType<? extends Tinta> t, Level l) {
        super(t, l);
    }

    public Tinta(Level l, LivingEntity dueno) {
        super(DarkCraftMod.TINTA.get(), dueno, l);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.INK_SAC;
    }

    @Override
    protected float getGravity() {
        return (float) Cantaro.GRAVEDAD;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (random.nextInt(2) == 0) level().addParticle(ParticleTypes.SQUID_INK, getX(), getY() + 0.1, getZ(), 0, 0, 0);
            return;
        }
        if (++edad > 120) discard();
    }

    @Override
    protected boolean canHitEntity(Entity e) {
        return super.canHitEntity(e) && !(e instanceof Enemigo);
    }

    @Override
    protected void onHit(HitResult r) {
        super.onHit(r);
        if (level().isClientSide) return;
        reventar(r.getLocation());
        discard();
    }

    private void reventar(Vec3 p) {
        if (!(level() instanceof ServerLevel sl)) return;
        REVIENTAN++;
        sl.sendParticles(ParticleTypes.SQUID_INK, p.x, p.y + 0.3, p.z, 40, 0.8, 0.4, 0.8, 0.05);
        sl.sendParticles(Erudito.TINTA, p.x, p.y + 0.4, p.z, 30, 0.9, 0.5, 0.9, 0.0);
        sl.playSound(null, p.x, p.y, p.z, DarkCraftMod.SFX_ERUDITO_TINTA.get(), SoundSource.HOSTILE, 1.2f, 0.95f + random.nextFloat() * 0.1f);
        for (Player pl : sl.getEntitiesOfClass(Player.class, new AABB(p.x - R, p.y - 1.5, p.z - R, p.x + R, p.y + 2.5, p.z + R),
                q -> !q.isCreative() && !q.isSpectator() && q.isAlive())) {
            if (pl.distanceToSqr(p.x, pl.getY(), p.z) > R * R) continue;
            pl.hurt(damageSources().thrown(this, getOwner()), DANO);
            pl.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0), getOwner());
            pl.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1), getOwner());
            CIEGAN++;
        }
    }
}
