package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * El arpon de Brann (parte 7r): lo lanza atado a su cabo (el cliente dibuja la cuerda de la mano de Brann a la cola del arpon:
 * {@code client/enemigos/ArponBalleneroRenderer}). Vuela como una flecha pesada; a quien pilla le hace dano y se le queda
 * <b>clavado</b> (va con el): entonces Brann tira del cabo ({@link Ballenero#tirar}) y lo arrastra hacia el. Si da en el suelo o
 * no da en nada, Brann lo recoge. No se recoge ni se guarda.
 */
public class ArponBallenero extends AbstractArrow {
    /** A quien se ha clavado (id; -1 = a nadie). */
    private static final EntityDataAccessor<Integer> CLAVADO = SynchedEntityData.defineId(ArponBallenero.class, EntityDataSerializers.INT);
    /** Volviendo a la mano de Brann (lo recoge). */
    private static final EntityDataAccessor<Boolean> VUELVE = SynchedEntityData.defineId(ArponBallenero.class, EntityDataSerializers.BOOLEAN);
    public static final float DANO = 0.9f;
    /** Lo que vuela sin dar en nada antes de que Brann lo recoja (ticks). */
    public static final int VUELO = 26;
    private int vuelo, vuelta;
    /** Si ya ha dado en algo (o se ha acabado su vuelo): Brann ya puede tirar del cabo. */
    private boolean listo;
    /** Para las pruebas: los que han pillado y los que han dado en el suelo. */
    public static int CLAVAN, AL_SUELO;

    public ArponBallenero(EntityType<? extends ArponBallenero> t, Level l) {
        super(t, l);
        pickup = Pickup.DISALLOWED;
    }

    public ArponBallenero(Level l, LivingEntity de) {
        super(DarkCraftMod.ARPON_BALLENERO.get(), de, l);
        pickup = Pickup.DISALLOWED;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(CLAVADO, -1);
        entityData.define(VUELVE, false);
    }

    /** A quien se ha clavado (en el cliente tambien), o null. */
    public LivingEntity clavado() {
        int id = entityData.get(CLAVADO);
        return id >= 0 && level().getEntity(id) instanceof LivingEntity le && le.isAlive() ? le : null;
    }

    public boolean listo() {
        return listo;
    }

    public boolean vuelve() {
        return entityData.get(VUELVE);
    }

    /** Brann lo recoge: vuelve volando a su mano (y alli se deshace). */
    public void recoger() {
        if (vuelve()) return;
        entityData.set(VUELVE, true);
        entityData.set(CLAVADO, -1);
        setNoPhysics(true);
        setNoGravity(true);
        vuelta = 0;
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void tick() {
        if (level().isClientSide) {
            super.tick();
            return;
        }
        Entity dueno = getOwner();
        if (!(dueno instanceof Ballenero b) || !b.isAlive()) {
            discard();
            return;
        }
        // volviendo a la mano de Brann
        if (vuelve()) {
            Vec3 mano = b.mano();
            Vec3 d = mano.subtract(position());
            double l = d.length();
            if (l < 1.6 || ++vuelta > 30) {
                discard();
                return;
            }
            Vec3 v = d.scale(Math.min(1.0, 1.6 / l));
            setDeltaMovement(v);
            setPos(getX() + v.x, getY() + v.y, getZ() + v.z);
            setYRot((float) (Math.toDegrees(Math.atan2(-v.x, -v.z)) + 180.0));
            return;
        }
        // clavado en alguien: va con el
        LivingEntity c = clavado();
        if (entityData.get(CLAVADO) >= 0) {
            if (c == null) {
                listo = true;
                entityData.set(CLAVADO, -1);
            } else {
                setPos(c.getX(), c.getY() + c.getBbHeight() * 0.6, c.getZ());
                setDeltaMovement(Vec3.ZERO);
                return;
            }
        }
        super.tick();
        if (inGround) listo = true;
        else if (++vuelo > VUELO) listo = true;
    }

    @Override
    protected boolean canHitEntity(Entity e) {
        return super.canHitEntity(e) && !(e instanceof Enemigo) && !vuelve();
    }

    @Override
    protected void onHitEntity(EntityHitResult r) {
        Entity e = r.getEntity();
        Entity de = getOwner();
        if (e == de || !(e instanceof LivingEntity le)) return;
        float dano = de instanceof LivingEntity dl && dl.getAttribute(Attributes.ATTACK_DAMAGE) != null
                ? (float) dl.getAttributeValue(Attributes.ATTACK_DAMAGE) * DANO : 10f;
        boolean ok = le.hurt(damageSources().mobProjectile(this, de instanceof LivingEntity dl ? dl : null), dano);
        playSound(SoundEvents.TRIDENT_HIT, 1.2f, 0.8f + random.nextFloat() * 0.2f);
        listo = true;
        // se le queda clavado (si no rodaba: un golpe que no entra no engancha)
        if (ok && !(le instanceof Player p && (p.isCreative() || p.isSpectator()))) {
            entityData.set(CLAVADO, le.getId());
            setDeltaMovement(Vec3.ZERO);
            CLAVAN++;
            if (level() instanceof ServerLevel sl)
                sl.sendParticles(ParticleTypes.CRIT, le.getX(), le.getY() + le.getBbHeight() * 0.6, le.getZ(), 12, 0.2, 0.3, 0.2, 0.2);
            if (de instanceof Ballenero b) b.enganchado(le);
        } else recoger();
    }

    @Override
    protected void onHitBlock(BlockHitResult r) {
        super.onHitBlock(r);
        if (!level().isClientSide) {
            listo = true;
            AL_SUELO++;
        }
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
    }
}
