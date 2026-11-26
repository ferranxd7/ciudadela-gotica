package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * La red que lanza el redero salobre (parte 7q): vuela en arco, abierta, y a quien pilla lo <b>enreda</b>: se le queda encima
 * (se ve en sus pies, enganchada) y casi no puede moverse un rato ({@link #ENREDA} ticks); al acabar se suelta y cae. Si da en
 * el suelo o en un muro se queda alli un momento y se deshace. A los enemigos no les da.
 */
public class Red extends ThrowableItemProjectile {
    public static final float DANO = 1.5f;
    /** Lo que dura enredado (ticks) y lo que frena (la lentitud: 6 = casi quieto). */
    public static final int ENREDA = 45, FRENO = 5;
    private static final EntityDataAccessor<Integer> PEGADA = SynchedEntityData.defineId(Red.class, EntityDataSerializers.INT);
    static final BlockParticleOption HILOS = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBWEB.defaultBlockState());
    private int edad, hasta = -1;
    /** Para la prueba: las que han pillado a alguien y las que han dado en el suelo. */
    public static int PILLAN, AL_SUELO;

    public Red(EntityType<? extends Red> t, Level l) {
        super(t, l);
    }

    public Red(Level l, LivingEntity dueno) {
        super(DarkCraftMod.RED.get(), dueno, l);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(PEGADA, -1);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.COBWEB;
    }

    @Override
    protected float getGravity() {
        return (float) Cantaro.GRAVEDAD;
    }

    /** A quien esta enganchada (o null si vuela o esta en el suelo). */
    public Entity pegada() {
        int id = entityData.get(PEGADA);
        return id < 0 ? null : level().getEntity(id);
    }

    @Override
    public void tick() {
        Entity v = pegada();
        if (v != null || hasta >= 0) {
            // enganchada a alguien, o tirada en el suelo: no vuela; en los pies de quien la lleva
            edad++;
            if (v != null) {
                setPos(v.getX(), v.getY() + 0.25, v.getZ());
                setDeltaMovement(Vec3.ZERO);
                if (level().isClientSide) {
                    if (random.nextInt(4) == 0)
                        level().addParticle(ParticleTypes.WHITE_ASH, v.getX() + (random.nextDouble() - 0.5) * 0.8, v.getY() + random.nextDouble() * 1.4,
                                v.getZ() + (random.nextDouble() - 0.5) * 0.8, 0, 0, 0);
                    return;
                }
                if (!v.isAlive() || edad >= hasta) soltar();
                return;
            }
            if (level().isClientSide) return;
            if (edad >= hasta) {
                if (level() instanceof ServerLevel sl) sl.sendParticles(HILOS, getX(), getY() + 0.2, getZ(), 10, 0.4, 0.1, 0.4, 0.05);
                discard();
            }
            return;
        }
        super.tick();
        if (level().isClientSide) {
            if (random.nextInt(2) == 0) level().addParticle(ParticleTypes.WHITE_ASH, getX(), getY() + 0.1, getZ(), 0, 0, 0);
            return;
        }
        if (++edad > 120) discard();
    }

    @Override
    protected boolean canHitEntity(Entity e) {
        return super.canHitEntity(e) && !(e instanceof Enemigo);
    }

    @Override
    protected void onHitEntity(EntityHitResult r) {
        super.onHitEntity(r);
        if (level().isClientSide || !(r.getEntity() instanceof LivingEntity le)) return;
        if (le instanceof Player p && (p.isCreative() || p.isSpectator())) return;
        // aunque no le duela (acaba de recibir otro golpe), queda enredado
        le.hurt(damageSources().thrown(this, getOwner()), DANO);
        if (le.isAlive()) enredar(le);
    }

    @Override
    protected void onHit(HitResult r) {
        super.onHit(r);
        if (level().isClientSide || pegada() != null || hasta >= 0) return;
        if (level() instanceof ServerLevel sl) sl.sendParticles(HILOS, getX(), getY() + 0.2, getZ(), 12, 0.5, 0.1, 0.5, 0.05);
        // en alguien que no se deja enredar: se deshace alli mismo
        if (r.getType() == HitResult.Type.ENTITY) {
            discard();
            return;
        }
        // en el suelo o en un muro: queda tirada un momento
        AL_SUELO++;
        setDeltaMovement(Vec3.ZERO);
        setNoGravity(true);
        edad = 0;
        hasta = 30;
    }

    private void enredar(LivingEntity le) {
        PILLAN++;
        Salobre.ENREDADOS++;
        le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ENREDA, FRENO), getOwner());
        le.setDeltaMovement(le.getDeltaMovement().multiply(0.2, 1, 0.2));
        le.hurtMarked = true;
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(HILOS, le.getX(), le.getY() + 1.0, le.getZ(), 24, 0.45, 0.6, 0.45, 0.08);
            sl.playSound(null, le.getX(), le.getY(), le.getZ(), DarkCraftMod.SFX_SALOBRE_RED.get(), SoundSource.HOSTILE, 0.8f, 1.25f);
        }
        entityData.set(PEGADA, le.getId());
        setNoGravity(true);
        edad = 0;
        hasta = ENREDA;
    }

    /** Se suelta (se acaba el enredo o cae quien la llevaba): queda en el suelo y se deshace. */
    private void soltar() {
        entityData.set(PEGADA, -1);
        if (level() instanceof ServerLevel sl) sl.sendParticles(HILOS, getX(), getY() + 0.3, getZ(), 14, 0.4, 0.3, 0.4, 0.06);
        discard();
    }
}
