package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * El fuego fatuo que lanza el podrido del fuego (parte 7i): una llama verde de pantano que vuela en arco. Sin modelo: se
 * ve por sus particulas (las pone el cliente a lo largo de lo que avanza; asi se ve igual con shaders). Al dar en alguien
 * le quema con magia y le envenena; donde cae deja una nube verde que envenena. A los enemigos no les da.
 */
public class FuegoFatuo extends ThrowableProjectile {
    /** Lo que cae por tick y lo rapido que va de lado (bloques por tick). */
    public static final double GRAVEDAD = 0.035, RAPIDEZ = 0.62;
    private int edad;

    public FuegoFatuo(EntityType<? extends FuegoFatuo> t, Level l) {
        super(t, l);
    }

    public FuegoFatuo(Level l, LivingEntity dueno) {
        super(DarkCraftMod.FUEGO_FATUO.get(), dueno, l);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected float getGravity() {
        return (float) GRAVEDAD;
    }

    /**
     * La velocidad para caer en (dx, dy, dz) desde donde sale: de lado a {@link #RAPIDEZ} (algo mas, por lo que frena el aire),
     * y hacia arriba lo que haga falta para llegar con la gravedad.
     */
    public static Vec3 tiro(double dx, double dy, double dz) {
        double h = Math.sqrt(dx * dx + dz * dz);
        double t = Math.max(4, h / RAPIDEZ);
        double frena = Math.max(0.6, 1 - 0.005 * t);
        double vh = h / t / frena, vy = dy / t + 0.5 * GRAVEDAD * t;
        return h < 0.01 ? new Vec3(0, vy, 0) : new Vec3(dx / h * vh, vy, dz / h * vh);
    }

    @Override
    public void tick() {
        Vec3 antes = position();
        super.tick();
        if (level().isClientSide) {
            Vec3 b = position();
            for (int i = 0; i < 4; i++) {
                Vec3 p = antes.lerp(b, i / 4.0);
                level().addParticle(Podrido.VERDE, p.x + (random.nextDouble() - 0.5) * 0.12, p.y + (random.nextDouble() - 0.5) * 0.12,
                        p.z + (random.nextDouble() - 0.5) * 0.12, 0, 0, 0);
            }
            if (random.nextInt(2) == 0) level().addParticle(ParticleTypes.GLOW, b.x, b.y, b.z, 0, 0.01, 0);
            if (random.nextInt(4) == 0) level().addParticle(ParticleTypes.SMOKE, b.x, b.y, b.z, 0, 0.02, 0);
            return;
        }
        if (++edad > 80) discard();
    }

    @Override
    protected boolean canHitEntity(Entity e) {
        return super.canHitEntity(e) && !(e instanceof Enemigo);
    }

    @Override
    protected void onHitEntity(EntityHitResult r) {
        if (level().isClientSide) return;
        if (r.getEntity() instanceof LivingEntity le) {
            if (le.hurt(damageSources().indirectMagic(this, getOwner()), 4f)) le.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0), getOwner());
        }
        estallar();
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult r) {
        super.onHitBlock(r);
        if (level().isClientSide) return;
        estallar();
        discard();
    }

    /** Se deshace en una nube verde que envenena (la de las nubes de los podridos). */
    private void estallar() {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 p = position();
        sl.sendParticles(Podrido.VERDE, p.x, p.y + 0.2, p.z, 22, 0.4, 0.3, 0.4, 0.03);
        sl.sendParticles(ParticleTypes.GLOW, p.x, p.y + 0.2, p.z, 8, 0.4, 0.3, 0.4, 0.05);
        sl.playSound(null, p.x, p.y, p.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 0.8f, 1.3f + random.nextFloat() * 0.2f);
        LivingEntity dueno = getOwner() instanceof LivingEntity le ? le : null;
        Podrido.nube(level(), dueno, p.x, Math.floor(p.y + 0.05), p.z, 1.7f, 70, Podrido.VERDE, new MobEffectInstance(MobEffects.POISON, 50, 0));
    }
}
