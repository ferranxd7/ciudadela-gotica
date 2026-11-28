package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * El cartucho de polvora que lanzan los barreneros enterrados (parte 7aq): vuela en arco con la mecha chisporroteando, cae y se
 * queda en el suelo; la mecha sigue ({@link #MECHA} ticks: se oye y se ve) y revienta. La explosion no rompe nada (es la mina de
 * otro): hace dano y empuja a todos los que pille, <b>tambien a los enterrados</b> (estan ciegos). En el agua se apaga. Si da en
 * alguien, cae a sus pies. El del morral (al morir un barrenero) ya esta en el suelo y revienta antes, mas flojo.
 */
public class Cartucho extends ThrowableItemProjectile {
    public static final double GRAVEDAD = 0.05;
    /** Lo que tarda en reventar desde que cae (y como mucho desde que se lanza). */
    public static final int MECHA = 32, MAXIMO = 90, MECHA_MORRAL = 26;
    /** La explosion: radio y dano en el centro (el del morral, menos). */
    public static final float RADIO = 2.8f, DANO = 10f, RADIO_MORRAL = 2.4f, DANO_MORRAL = 7f;
    /** En el suelo (lo sabe tambien el cliente: asi no lo sigue haciendo caer). */
    private static final EntityDataAccessor<Boolean> POSADO = SynchedEntityData.defineId(Cartucho.class, EntityDataSerializers.BOOLEAN);
    private boolean morral;
    private int edad, mecha = -1;
    /** Para la prueba: los que han caido, los que han reventado, a quien han dado (y cuantos de ellos eran enterrados), los apagados. */
    public static int CAEN, REVIENTAN, DAN, DAN_ENTERRADOS, APAGADOS;

    public Cartucho(EntityType<? extends Cartucho> t, Level l) {
        super(t, l);
    }

    public Cartucho(Level l, LivingEntity dueno) {
        super(DarkCraftMod.CARTUCHO.get(), dueno, l);
    }

    /** El del morral: ya en el suelo, con la mecha mas corta. */
    public void morral(double x, double y, double z) {
        setPos(x, y, z);
        setDeltaMovement(0, -0.1, 0);
        morral = true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(POSADO, false);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.RED_CANDLE;
    }

    @Override
    protected float getGravity() {
        return posado() ? 0f : (float) GRAVEDAD;
    }

    public boolean posado() {
        return entityData.get(POSADO);
    }

    @Override
    public void tick() {
        boolean posado = posado();
        if (posado) {
            // en el suelo: quieto (no vuela ni choca), la mecha
            setDeltaMovement(Vec3.ZERO);
            baseTick();
        } else super.tick();
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.SMALL_FLAME, getX(), getY() + 0.25, getZ(), 0, 0.01, 0);
            if (random.nextInt(2) == 0) level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.3, getZ(), 0, 0.02, 0);
            if (posado && random.nextInt(2) == 0)
                level().addParticle(ParticleTypes.LAVA, getX(), getY() + 0.2, getZ(), 0, 0, 0);
            return;
        }
        edad++;
        if (isInWater()) {
            apagar();
            return;
        }
        if (morral && !posado() && (onGround() || edad > 4)) posar();
        if (mecha < 0 && edad > MAXIMO) mecha = 0;
        if (mecha >= 0 && --mecha <= 0) estallar();
    }

    private void posar() {
        if (posado()) return;
        entityData.set(POSADO, true);
        CAEN++;
        mecha = morral ? MECHA_MORRAL : MECHA;
        setDeltaMovement(Vec3.ZERO);
        level().playSound(null, getX(), getY(), getZ(), DarkCraftMod.SFX_ENTERRADO_MECHA.get(), SoundSource.HOSTILE, morral ? 1.2f : 1.0f, 1.0f);
    }

    @Override
    protected boolean canHitEntity(Entity e) {
        return !posado() && super.canHitEntity(e) && !(e instanceof Enemigo);
    }

    /** Si da en alguien, le cae a los pies (y sigue la mecha). */
    @Override
    protected void onHitEntity(EntityHitResult r) {
        if (level().isClientSide) return;
        Entity e = r.getEntity();
        setPos(e.getX(), e.getY() + 0.2, e.getZ());
        setDeltaMovement(0, -0.15, 0);
    }

    /** En el suelo se queda; en una pared rebota un poco y sigue cayendo. */
    @Override
    protected void onHitBlock(BlockHitResult r) {
        super.onHitBlock(r);
        if (level().isClientSide) return;
        Direction cara = r.getDirection();
        Vec3 p = r.getLocation();
        if (cara == Direction.UP) {
            setPos(p.x, p.y + 0.02, p.z);
            posar();
        } else if (cara == Direction.DOWN) {
            setDeltaMovement(getDeltaMovement().multiply(0.3, -0.2, 0.3));
        } else {
            Vec3 n = Vec3.atLowerCornerOf(cara.getNormal());
            setPos(p.x + n.x * 0.15, p.y, p.z + n.z * 0.15);
            Vec3 v = getDeltaMovement();
            setDeltaMovement(n.x != 0 ? -v.x * 0.25 : v.x * 0.3, Math.min(v.y, 0), n.z != 0 ? -v.z * 0.25 : v.z * 0.3);
        }
    }

    private void apagar() {
        APAGADOS++;
        if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.3, getZ(), 8, 0.2, 0.2, 0.2, 0.02);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 0.8f, 1.2f);
        discard();
    }

    /** Revienta: fogonazo, humo y piedras; dano y empujon a todos los que pille (los enterrados tambien); y la mina tiembla. */
    private void estallar() {
        if (!(level() instanceof ServerLevel sl)) return;
        REVIENTAN++;
        float radio = morral ? RADIO_MORRAL : RADIO, dano = morral ? DANO_MORRAL : DANO;
        double x = getX(), y = getY() + 0.3, z = getZ();
        sl.sendParticles(ParticleTypes.EXPLOSION, x, y, z, morral ? 2 : 3, 0.4, 0.3, 0.4, 0);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.3, z, 16, 0.7, 0.5, 0.7, 0.03);
        sl.sendParticles(ParticleTypes.FLAME, x, y, z, 20, 0.6, 0.3, 0.6, 0.08);
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBBLED_DEEPSLATE.defaultBlockState()), x, y, z, 30, 0.6, 0.3, 0.6, 0.25);
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GRAVEL.defaultBlockState()), x, y, z, 24, 0.7, 0.3, 0.7, 0.25);
        sl.playSound(null, x, y, z, DarkCraftMod.SFX_ENTERRADO_ESTALLIDO.get(), SoundSource.HOSTILE, morral ? 1.6f : 2.0f, 0.9f + random.nextFloat() * 0.15f);
        Entity dueno = getOwner();
        for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, new AABB(x - radio, y - radio, z - radio, x + radio, y + radio, z + radio),
                o -> o.isAlive() && !(o instanceof Player p && (p.isCreative() || p.isSpectator())))) {
            Vec3 c = le.position().add(0, le.getBbHeight() * 0.5, 0);
            double d = c.distanceTo(new Vec3(x, y, z));
            if (d > radio + le.getBbWidth() * 0.5) continue;
            float k = (float) Math.max(0.3, 1.0 - d / (radio + 0.5));
            le.invulnerableTime = 0;
            // sin el cartucho como lo que pega (un proyectil de uno de ellos no le haria nada a los suyos: Enemigo.hurt), y si a los suyos
            if (!le.hurt(damageSources().explosion(null, dueno instanceof LivingEntity l ? l : null), dano * k)) continue;
            DAN++;
            if (le instanceof Enterrado) DAN_ENTERRADOS++;
            Vec3 fuera = c.subtract(x, y - 0.6, z);
            if (fuera.lengthSqr() < 1e-4) fuera = new Vec3(0, 1, 0);
            fuera = fuera.normalize().scale(0.55 * k);
            le.setDeltaMovement(le.getDeltaMovement().add(fuera.x, 0.25 + fuera.y * 0.5, fuera.z));
            le.hurtMarked = true;
        }
        com.darkcraft.morvain.Mina.temblor(sl, x, y, z);
        discard();
    }
}
