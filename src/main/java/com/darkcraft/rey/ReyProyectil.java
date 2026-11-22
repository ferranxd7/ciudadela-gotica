package com.darkcraft.rey;

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
 * que va por el suelo. Tambien los tajos de sus espadas en manos de un jugador (fase 0: dorados).
 * No rompe ni quema bloques. El tajo, al acabar o chocar, se deshace en chispas en unos pocos ticks.
 */
public class ReyProyectil extends Projectile {
    public static final int TAJO = 0, BOLA = 1, OLA = 2;

    private static final EntityDataAccessor<Integer> TIPO = SynchedEntityData.defineId(ReyProyectil.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FASE = SynchedEntityData.defineId(ReyProyectil.class, EntityDataSerializers.INT);
    /** Inclinacion de la media luna (grados). */
    private static final EntityDataAccessor<Float> GIRO = SynchedEntityData.defineId(ReyProyectil.class, EntityDataSerializers.FLOAT);
    /** Se esta deshaciendo (fin del vuelo o choque). */
    private static final EntityDataAccessor<Boolean> DISUELVE = SynchedEntityData.defineId(ReyProyectil.class, EntityDataSerializers.BOOLEAN);
    public static final int DISUELVE_TICKS = 7;
    public static final float RADIO = 1.7f, ARCO = 75f;
    private int disIni = -1;

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
        this.entityData.define(GIRO, 20f);
        this.entityData.define(DISUELVE, false);
    }

    public float giro() {
        return this.entityData.get(GIRO);
    }

    /** 0 mientras vuela; de 0 a 1 mientras se deshace. */
    public float disolviendo(float pt) {
        return disIni < 0 ? 0f : Mth.clamp((tickCount + pt - disIni) / DISUELVE_TICKS, 0f, 1f);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DISUELVE.equals(key) && this.entityData.get(DISUELVE) && disIni < 0) {
            disIni = tickCount;
            if (level().isClientSide) for (int i = 0; i < 26; i++) chispa(i / 25f * 2 - 1, 0.12);
        }
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
        // tajo en diagonal, a un lado o al otro
        float g = 18f + random.nextFloat() * (fase == 0 ? 40f : 32f);
        this.entityData.set(GIRO, random.nextBoolean() ? g : -g);
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
            if (disIni < 0) particulas();
            setPos(next);
            return;
        }
        ServerLevel sl = (ServerLevel) level();
        if (this.entityData.get(DISUELVE)) {
            setDeltaMovement(Vec3.ZERO);
            if (++vida > DISUELVE_TICKS) discard();
            return;
        }
        if (++vida > maxVida()) {
            if (tipo() == TAJO) disolver();
            else discard();
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
        if (getOwner() instanceof net.minecraft.world.entity.player.Player pl) {
            // tajos de las espadas del Rey en manos de un jugador: dan a los monstruos
            for (net.minecraft.world.entity.LivingEntity le : sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, box,
                    x -> com.darkcraft.armas.Combos.valido(pl, x))) {
                if (!tocados.add(le.getUUID())) continue;
                le.invulnerableTime = 0;
                if (le.hurt(sl.damageSources().playerAttack(pl), dano)) {
                    if (!le.fireImmune()) le.setSecondsOnFire(4);
                    le.knockback(0.5, -v.x, -v.z);
                }
            }
            return;
        }
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
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(), 8, 0.3, 0.3, 0.3, 0.03);
        } else if (tipo() == TAJO) {
            disolver();
            return;
        } else {
            sl.sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(), 6, 0.4, 0.4, 0.4, 0.05);
        }
        discard();
    }

    /** El tajo se para y se deshace en chispas (lo dibuja el cliente). */
    private void disolver() {
        this.entityData.set(DISUELVE, true);
        setDeltaMovement(Vec3.ZERO);
        vida = 0;
    }

    /**
     * Punto del arco en coordenadas de la media luna (x a un lado, y arriba, z hacia donde va), antes de girarla:
     * el arco esta de cara al viaje (curvado hacia arriba) y su centro va un poco por delante de las puntas.
     */
    public static double[] arcoLocal(double u, double rad) {
        double a = Math.toRadians(u * ARCO);
        double ly = rad * Math.cos(a) - 0.55 * RADIO;
        return new double[]{rad * Math.sin(a), ly - 0.15, ly * 0.4};
    }

    /** Punto del arco de la media luna en el mundo: u de -1 a 1 (de punta a punta), r de 0 a 1 hacia dentro. */
    public Vec3 puntoArco(double u, double r) {
        double[] q = arcoLocal(u, RADIO * (1 - 0.25 * r));
        double roll = Math.toRadians(giro());
        double x = q[0] * Math.cos(roll) - q[1] * Math.sin(roll), y = q[0] * Math.sin(roll) + q[1] * Math.cos(roll), z = q[2];
        double th = Math.toRadians(-getYRot());
        double wx = x * Math.cos(th) + z * Math.sin(th), wz = -x * Math.sin(th) + z * Math.cos(th);
        return new Vec3(getX() + wx, getY() + y, getZ() + wz);
    }

    private void chispa(double u, double vel) {
        Vec3 p = puntoArco(u, random.nextDouble() * 0.6);
        double vx = (random.nextDouble() - 0.5) * vel, vy = (random.nextDouble() - 0.3) * vel, vz = (random.nextDouble() - 0.5) * vel;
        Level l = level();
        switch (fase()) {
            case 0 -> l.addParticle(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(1f, 0.78f, 0.25f), 1.1f), p.x, p.y, p.z, vx, vy, vz);
            case 1 -> l.addParticle(ParticleTypes.END_ROD, p.x, p.y, p.z, vx * 0.4, vy * 0.4, vz * 0.4);
            case 2 -> l.addParticle(ParticleTypes.FLAME, p.x, p.y, p.z, vx * 0.5, vy * 0.5, vz * 0.5);
            default -> l.addParticle(random.nextInt(3) == 0 ? ParticleTypes.SMALL_FLAME : ParticleTypes.FLAME, p.x, p.y, p.z, vx * 0.5, vy * 0.5, vz * 0.5);
        }
    }

    private void particulas() {
        Level l = level();
        int f = fase();
        switch (tipo()) {
            case TAJO -> {
                // estela de chispas a lo largo del arco
                for (int i = 0; i < 3; i++) chispa(random.nextDouble() * 2 - 1, 0.03);
                if (f == 3 && random.nextInt(3) == 0) {
                    Vec3 p = puntoArco(random.nextDouble() * 2 - 1, 0.3);
                    l.addParticle(ParticleTypes.DRIPPING_LAVA, p.x, p.y, p.z, 0, 0, 0);
                }
            }
            case BOLA -> {
                // llamas que se quedan atras y una estela de humo
                Vec3 v = getDeltaMovement();
                for (int i = 0; i < 2; i++)
                    l.addParticle(ParticleTypes.FLAME, getX() + (random.nextDouble() - 0.5) * 0.5, getY() + (random.nextDouble() - 0.5) * 0.5,
                            getZ() + (random.nextDouble() - 0.5) * 0.5, -v.x * 0.2, 0.01, -v.z * 0.2);
                l.addParticle(ParticleTypes.SMOKE, getX() - v.x * 0.5, getY(), getZ() - v.z * 0.5, 0, 0.02, 0);
                if (tickCount % 2 == 0) l.addParticle(ParticleTypes.LARGE_SMOKE, getX() - v.x, getY() + 0.1, getZ() - v.z, 0, 0.03, 0);
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
