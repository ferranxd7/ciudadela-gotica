package com.ciudadela.rey;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Lo que lanza el Rey: el tajo a distancia (media luna de luz), las bolas de fuego y la ola de fuego
 * que va por el suelo. No rompe ni quema bloques.
 */
public class ReyProyectil extends Projectile {
    public static final int TAJO = 0, BOLA = 1, OLA = 2;

    private static final EntityDataAccessor<Integer> TIPO = SynchedEntityData.defineId(ReyProyectil.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FASE = SynchedEntityData.defineId(ReyProyectil.class, EntityDataSerializers.INT);

    private float dano = 20;
    private int vida;
    private final Set<UUID> tocados = new HashSet<>();

    public ReyProyectil(EntityType<? extends ReyProyectil> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TIPO, TAJO);
        this.entityData.define(FASE, 1);
    }

    public int tipo() {
        return this.entityData.get(TIPO);
    }

    public int fase() {
        return this.entityData.get(FASE);
    }

    public void preparar(int tipo, Vec3 v, float dano, int fase) {
        this.entityData.set(TIPO, tipo);
        this.entityData.set(FASE, fase);
        this.dano = dano;
        setDeltaMovement(v);
        float yaw = (float) (Mth.atan2(v.z, v.x) * Mth.RAD_TO_DEG) - 90f;
        setYRot(yaw);
        this.yRotO = yaw;
    }

    private int maxVida() {
        return switch (tipo()) {
            case TAJO -> 30;
            case OLA -> 28;
            default -> 60;
        };
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 v = getDeltaMovement();
        Vec3 next = position().add(v);
        if (level().isClientSide) {
            particulas();
            setPos(next);
            return;
        }
        ServerLevel sl = (ServerLevel) level();
        if (++vida > maxVida()) {
            discard();
            return;
        }
        // choca con paredes
        BlockPos bp = BlockPos.containing(next.x, next.y + (tipo() == OLA ? 0.5 : 0), next.z);
        BlockState st = sl.getBlockState(bp);
        if (!st.getCollisionShape(sl, bp).isEmpty()) {
            impacto(sl);
            return;
        }
        if (tipo() == OLA) {
            // la ola va pegada al suelo
            BlockPos below = BlockPos.containing(next.x, next.y - 0.6, next.z);
            if (sl.getBlockState(below).getCollisionShape(sl, below).isEmpty()) next = next.add(0, -0.5, 0);
        }
        setPos(next);
        double r = tipo() == TAJO ? 1.6 : (tipo() == OLA ? 1.1 : 0.8);
        AABB box = new AABB(getX() - r, getY() - (tipo() == TAJO ? 1.2 : 0.6), getZ() - r, getX() + r, getY() + (tipo() == OLA ? 1.6 : 1.2), getZ() + r);
        for (ServerPlayer p : sl.getEntitiesOfClass(ServerPlayer.class, box)) {
            if (!Rey.valido(p) || !tocados.add(p.getUUID())) continue;
            Entity owner = getOwner();
            var reg = sl.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
            DamageSource src = new DamageSource(reg.getHolderOrThrow(tipo() == TAJO ? Rey.GOLPE : Rey.AREA), this, owner);
            p.invulnerableTime = 0;
            if (p.hurt(src, dano) && tipo() != TAJO) p.setSecondsOnFire(fase() >= 3 ? 6 : 4);
            if (fase() >= 2 && tipo() == TAJO) p.setSecondsOnFire(3);
            p.knockback(0.6, -v.x, -v.z);
            p.hurtMarked = true;
            if (tipo() == BOLA) {
                impacto(sl);
                return;
            }
        }
    }

    private void impacto(ServerLevel sl) {
        if (tipo() == BOLA) {
            sl.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(), 20, 0.4, 0.4, 0.4, 0.06);
            sl.playSound(null, getX(), getY(), getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.0f, 0.8f);
        } else {
            sl.sendParticles(tipo() == TAJO ? ParticleTypes.SWEEP_ATTACK : ParticleTypes.FLAME, getX(), getY(), getZ(), 6, 0.4, 0.4, 0.4, 0.05);
        }
        discard();
    }

    private void particulas() {
        Level l = level();
        int f = fase();
        switch (tipo()) {
            case TAJO -> {
                if (f >= 2)
                    for (int i = 0; i < 3; i++)
                        l.addParticle(f == 3 ? ParticleTypes.LAVA : ParticleTypes.FLAME, getX() + (random.nextDouble() - 0.5) * 2, getY() + (random.nextDouble() - 0.5),
                                getZ() + (random.nextDouble() - 0.5) * 2, 0, 0.02, 0);
                else l.addParticle(ParticleTypes.ENCHANTED_HIT, getX(), getY(), getZ(), 0, 0, 0);
            }
            case BOLA -> {
                l.addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0, 0, 0);
                l.addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0, 0.02, 0);
                if (random.nextInt(3) == 0) l.addParticle(ParticleTypes.LAVA, getX(), getY(), getZ(), 0, 0, 0);
            }
            default -> {
                for (int i = 0; i < 5; i++)
                    l.addParticle(f == 3 ? ParticleTypes.LAVA : ParticleTypes.FLAME, getX() + (random.nextDouble() - 0.5) * 1.6, getY() + random.nextDouble() * 1.4,
                            getZ() + (random.nextDouble() - 0.5) * 1.6, 0, 0.06, 0);
                l.addParticle(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1, getZ(), 0, 0.05, 0);
            }
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource src, float amount) {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 128 * 128;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
