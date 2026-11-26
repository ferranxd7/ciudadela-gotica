package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * La esquirla de hielo que lanza el peregrino helado (parte 7k): vuela casi recta y rapida. Sin modelo: se ve por sus
 * particulas (escarcha y copos, las pone el cliente a lo largo de lo que avanza; asi se ve igual con shaders). Al dar en
 * alguien le hace dano de frio ({@link Helado#frio}), le hiela un rato y le frena; al dar en algo se rompe. A los enemigos
 * no les da.
 */
public class Esquirla extends ThrowableProjectile {
    /** Lo que cae por tick y lo rapido que va (bloques por tick). */
    public static final double GRAVEDAD = 0.012, RAPIDEZ = 1.05;
    public static final float DANO = 4.0f;
    private int edad;
    /** Para la prueba: las que han dado en alguien. */
    public static int DAN;

    public Esquirla(EntityType<? extends Esquirla> t, Level l) {
        super(t, l);
    }

    public Esquirla(Level l, LivingEntity dueno) {
        super(DarkCraftMod.ESQUIRLA.get(), dueno, l);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected float getGravity() {
        return (float) GRAVEDAD;
    }

    @Override
    public void tick() {
        Vec3 antes = position();
        super.tick();
        if (level().isClientSide) {
            Vec3 b = position();
            for (int i = 0; i < 4; i++) {
                Vec3 p = antes.lerp(b, i / 4.0);
                level().addParticle(Helado.ESCARCHA, p.x + (random.nextDouble() - 0.5) * 0.08, p.y + (random.nextDouble() - 0.5) * 0.08,
                        p.z + (random.nextDouble() - 0.5) * 0.08, 0, 0, 0);
            }
            if (random.nextInt(2) == 0) level().addParticle(ParticleTypes.SNOWFLAKE, b.x, b.y, b.z, 0, 0, 0);
            return;
        }
        if (++edad > 60) discard();
    }

    @Override
    protected boolean canHitEntity(Entity e) {
        return super.canHitEntity(e) && !(e instanceof Enemigo);
    }

    @Override
    protected void onHitEntity(EntityHitResult r) {
        if (level().isClientSide) return;
        if (r.getEntity() instanceof LivingEntity le && le.hurt(Helado.frio(level(), this, getOwner()), DANO)) {
            DAN++;
            Helado.helar(le, Helado.FRIO_ESQUIRLA);
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0), getOwner());
        }
        romper();
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult r) {
        super.onHitBlock(r);
        if (level().isClientSide) return;
        romper();
        discard();
    }

    private void romper() {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 p = position();
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()), p.x, p.y, p.z, 10, 0.15, 0.15, 0.15, 0.1);
        sl.sendParticles(ParticleTypes.SNOWFLAKE, p.x, p.y, p.z, 5, 0.2, 0.2, 0.2, 0.03);
        sl.playSound(null, p.x, p.y, p.z, SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 0.45f, 1.5f + random.nextFloat() * 0.3f);
    }
}
