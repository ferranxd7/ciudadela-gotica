package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * Una calavera de la corona de Fray Osmund (parte 7at): sale de su aureola, le orbita ({@link #ORBITA} ticks; roza a quien se
 * le acerca) y vuela hacia su presa con un chillido (una detras de otra, {@link #ENTRE} ticks), torciendo un poco hacia ella; al
 * dar en alguien o en la piedra se rompe en esquirlas de hueso. Se rompe tambien de un golpe (una flecha, la espada) y si su dueno
 * cae. No se guarda con el mundo. La dibuja {@code client/enemigos/CalaveraOsmundRenderer} (el craneo del juego, a plena luz).
 */
public class CalaveraOsmund extends Projectile {
    /** Lo que orbita antes de que vuele la primera, entre una y otra, lo que vuela como mucho y lo que tarda en ir al aro. */
    public static final int ORBITA = 50, ENTRE = 8, VIDA = 70, SALIR = 10;
    /** El radio de la orbita y lo deprisa que vuela (bloques por tick). */
    public static final double R_ORBITA = 2.5, VELOCIDAD = 0.7;
    /** Si ya vuela (lo sabe el cliente: el rastro). */
    private static final EntityDataAccessor<Boolean> VUELA = SynchedEntityData.defineId(CalaveraOsmund.class, EntityDataSerializers.BOOLEAN);
    private int k, n = 1, t, volando;
    private Vec3 salida = Vec3.ZERO;
    private final Map<Integer, Integer> rozados = new HashMap<>();
    /** Para las pruebas: las que han volado, las que han dado (volando), los roces (orbitando), las que se han roto y las rotas a golpes. */
    public static int LANZADAS, DAN, ROZAN, ROMPEN, ROTAS;

    public CalaveraOsmund(EntityType<? extends CalaveraOsmund> t, Level l) {
        super(t, l);
        noPhysics = true;
        setNoGravity(true);
    }

    /** La k-esima de n de la corona de {@code dueno}: sale de su aureola. */
    public CalaveraOsmund(Level l, Osmund dueno, int k, int n) {
        this(DarkCraftMod.CALAVERA_OSMUND.get(), l);
        setOwner(dueno);
        this.k = k;
        this.n = Math.max(1, n);
        salida = dueno.aureola().add((k - (n - 1) / 2.0) * 0.35, 0, 0);
        setPos(salida.x, salida.y, salida.z);
        setYRot(dueno.getYRot());
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(VUELA, false);
    }

    public boolean vuela() {
        return entityData.get(VUELA);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            // el rastro: fuego de almas y humo (volando, mas)
            if (tickCount % (vuela() ? 1 : 3) == 0) {
                level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, getX() + (random.nextDouble() - 0.5) * 0.2, getY() + 0.2, getZ() + (random.nextDouble() - 0.5) * 0.2,
                        0, 0.01, 0);
                if (vuela()) level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.2, getZ(), 0, 0.01, 0);
            }
            return;
        }
        Entity o = getOwner();
        if (!(o instanceof Osmund os) || !os.isAlive() || os.isRemoved()) {
            romper();
            return;
        }
        t++;
        if (!vuela()) orbitar((ServerLevel) level(), os);
        else volar((ServerLevel) level(), os);
    }

    /** Le orbita (al principio sale del aro poco a poco); roza a quien toca; a su hora sale volando hacia su presa. */
    private void orbitar(ServerLevel sl, Osmund os) {
        double a = Math.PI * 2 * k / n + t * 0.16;
        double crece = Math.min(1.0, t / (double) SALIR);
        Vec3 en = new Vec3(os.getX() + Math.cos(a) * R_ORBITA, os.getY() + os.getBbHeight() * 0.62 + 0.25 * Math.sin(t * 0.3 + k), os.getZ() + Math.sin(a) * R_ORBITA);
        Vec3 aro = os.aureola();
        Vec3 p = crece < 1 ? aro.lerp(en, crece) : en;
        Vec3 antes = position();
        setPos(p.x, p.y, p.z);
        Vec3 d = p.subtract(antes);
        if (d.lengthSqr() > 1e-4) setYRot((float) Math.toDegrees(Math.atan2(-d.x, d.z)));
        // roza a quien este en su camino (cada uno, una vez cada 12 ticks)
        float dano = (float) os.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) * 0.45f;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.45), os::presa)) {
            Integer ultimo = rozados.get(le.getId());
            if (ultimo != null && t - ultimo < 12) continue;
            rozados.put(le.getId(), t);
            if (le.hurt(damageSources().mobProjectile(this, os), dano)) {
                ROZAN++;
                sl.sendParticles(ParticleTypes.SOUL, le.getX(), le.getY() + le.getBbHeight() * 0.6, le.getZ(), 4, 0.2, 0.2, 0.2, 0.02);
            }
        }
        if (t < ORBITA + k * ENTRE) return;
        LivingEntity presa = os.getTarget();
        if (presa == null || !os.presa(presa))
            presa = sl.getNearestPlayer(getX(), getY(), getZ(), 24, e -> e instanceof Player pl && os.presa(pl));
        if (presa == null) {
            // sin nadie a quien ir: sigue orbitando un rato y vuelve al aro
            if (t > ORBITA + n * ENTRE + 120) volver(sl);
            return;
        }
        Vec3 dir = pecho(presa).subtract(position()).normalize();
        setDeltaMovement(dir.scale(VELOCIDAD));
        entityData.set(VUELA, true);
        LANZADAS++;
        sl.playSound(null, getX(), getY(), getZ(), DarkCraftMod.SFX_OSMUND_CALAVERA.get(), SoundSource.HOSTILE, 1.6f, 0.9f + random.nextFloat() * 0.25f);
    }

    private static Vec3 pecho(LivingEntity e) {
        return new Vec3(e.getX(), e.getY() + e.getBbHeight() * 0.6, e.getZ());
    }

    /** Vuela hacia su presa (torciendo un poco); da en alguien o en la piedra y se rompe. */
    private void volar(ServerLevel sl, Osmund os) {
        volando++;
        Vec3 dm = getDeltaMovement();
        LivingEntity presa = os.getTarget();
        if (presa != null && os.presa(presa) && volando < VIDA - 20) {
            Vec3 quiere = pecho(presa).subtract(position()).normalize().scale(VELOCIDAD);
            dm = dm.lerp(quiere, 0.07).normalize().scale(VELOCIDAD);
        }
        Vec3 de = position(), a = de.add(dm);
        BlockHitResult bh = sl.clip(new ClipContext(de, a, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (bh.getType() != HitResult.Type.MISS) a = bh.getLocation();
        EntityHitResult eh = ProjectileUtil.getEntityHitResult(sl, this, de, a, getBoundingBox().expandTowards(dm).inflate(0.35),
                e -> e instanceof LivingEntity le && os.presa(le));
        if (eh != null && eh.getEntity() instanceof LivingEntity le) {
            float dano = (float) os.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) * 0.7f;
            if (le.hurt(damageSources().mobProjectile(this, os), dano)) {
                DAN++;
                le.knockback(0.4, -dm.x, -dm.z);
            }
            romper();
            return;
        }
        if (bh.getType() != HitResult.Type.MISS || volando > VIDA) {
            romper();
            return;
        }
        setDeltaMovement(dm);
        setPos(a.x, a.y, a.z);
        setYRot((float) Math.toDegrees(Math.atan2(-dm.x, dm.z)));
        setXRot((float) Math.toDegrees(-Math.atan2(dm.y, Math.hypot(dm.x, dm.z))));
    }

    /** Vuelve al aro de su dueno sin haber volado (no se rompe: se le vuelve a poner). */
    private void volver(ServerLevel sl) {
        sl.sendParticles(ParticleTypes.SOUL, getX(), getY(), getZ(), 4, 0.15, 0.15, 0.15, 0.02);
        discard();
    }

    /** Se rompe en esquirlas de hueso. */
    public void romper() {
        if (isRemoved()) return;
        if (level() instanceof ServerLevel sl) {
            ROMPEN++;
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()), getX(), getY() + 0.2, getZ(), 18, 0.25, 0.25, 0.25, 0.12);
            sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 0.2, getZ(), 5, 0.2, 0.2, 0.2, 0.03);
            sl.playSound(null, getX(), getY(), getZ(), SoundEvents.BONE_BLOCK_BREAK, SoundSource.HOSTILE, 1.2f, 1.2f + random.nextFloat() * 0.3f);
        }
        discard();
    }

    /**
     * Un golpe de un jugador (lo llama el combate al pedirlo: en aventura el clic no llega a las entidades que no son criaturas): rompe
     * las calaveras que tenga delante, a su alcance. Devuelve cuantas.
     */
    public static int golpe(net.minecraft.server.level.ServerPlayer p) {
        int n = 0;
        Vec3 ojos = p.getEyePosition(), mira = p.getLookAngle();
        double cos = Math.cos(Math.toRadians(42));
        for (CalaveraOsmund c : p.level().getEntitiesOfClass(CalaveraOsmund.class, p.getBoundingBox().inflate(3.8))) {
            Vec3 d = c.position().add(0, 0.25, 0).subtract(ojos);
            double l = d.length();
            if (l > 3.5 || l < 1e-3 || d.dot(mira) / l < cos) continue;
            c.romper();
            ROTAS++;
            n++;
        }
        return n;
    }

    /** Un golpe la rompe (una flecha, una espada). */
    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        if (isInvulnerableTo(s)) return false;
        if (!level().isClientSide && s.getEntity() instanceof LivingEntity && !(s.getEntity() instanceof Osmund)) romper();
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 0.3f;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag t) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag t) {
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 64 * 64;
    }

    /** Para el dibujante: lo que lleva girada (la cabeza hacia donde va). */
    public float giro(float pt) {
        return Mth.rotLerp(pt, yRotO, getYRot());
    }
}
