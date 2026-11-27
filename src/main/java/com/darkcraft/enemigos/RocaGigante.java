package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * La roca que arranca un gigante errante del suelo y le tira a quien esta lejos (parte 7w): un pedrusco de casi dos bloques que
 * vuela en arco dando vueltas (la dibuja {@code client/enemigos/RocaGiganteRenderer} con bloques de piedra) y al caer revienta:
 * a quien le da de lleno, el golpe entero; alrededor ({@link #RADIO}), menos. A los enemigos no les da. Se ve venir: sale de lo
 * alto de su mano y tarda en llegar lo que tarda en caer.
 */
public class RocaGigante extends ThrowableItemProjectile {
    public static final double GRAVEDAD = 0.05, RADIO = 3.2;
    /** Lo que pega de lleno y alrededor (veces el golpe del gigante). */
    public static final float LLENO = 1.4f, ALREDEDOR = 0.75f;
    private int edad;
    /** Para las pruebas: las que han caido y a cuantos han dado. */
    public static int CAEN, DAN;
    /** En el cliente: lo que pasa cuando revienta una (el temblor de la camara; lo pone {@code client/enemigos/GiganteCliente}). */
    public static java.util.function.Consumer<Vec3> AL_CAER;

    public RocaGigante(EntityType<? extends RocaGigante> t, Level l) {
        super(t, l);
    }

    public RocaGigante(Level l, LivingEntity dueno) {
        super(DarkCraftMod.ROCA_GIGANTE.get(), dueno, l);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.COBBLESTONE;
    }

    @Override
    protected float getGravity() {
        return (float) GRAVEDAD;
    }

    /**
     * La velocidad para caer en {@code b} saliendo de {@code o}: un arco alto que tarda mas cuanto mas lejos (de 18 a 44 ticks;
     * con lo que se come el aire, un 1 % por tick, en cuenta a ojo).
     */
    public static Vec3 tiro(Vec3 o, Vec3 b) {
        double dx = b.x - o.x, dz = b.z - o.z, dy = b.y - o.y, h = Math.sqrt(dx * dx + dz * dz);
        double t = Mth.clamp(14 + h * 0.85, 18, 44);
        double k = 1 + 0.0055 * t;
        return new Vec3(dx / t * k, (dy + 0.5 * GRAVEDAD * t * t) / t * k, dz / t * k);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (random.nextInt(2) == 0)
                level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()), getX(), getY() + 0.4, getZ(), 0, -0.05, 0);
            return;
        }
        if (++edad > 140) discard();
    }

    @Override
    public void onClientRemoval() {
        if (AL_CAER != null) AL_CAER.accept(position());
    }

    @Override
    protected boolean canHitEntity(Entity e) {
        return super.canHitEntity(e) && !(e instanceof Enemigo);
    }

    @Override
    protected void onHitEntity(EntityHitResult r) {
    }

    @Override
    protected void onHit(HitResult r) {
        super.onHit(r);
        if (level().isClientSide) return;
        reventar(r.getLocation());
        discard();
    }

    /** Revienta: la piedra en pedazos, el polvo, el golpe (de lleno y alrededor) y el temblor. */
    private void reventar(Vec3 p) {
        if (!(level() instanceof ServerLevel sl)) return;
        CAEN++;
        BlockState piedra = Blocks.COBBLESTONE.defaultBlockState();
        BlockState suelo = sl.getBlockState(BlockPos.containing(p.x, p.y - 0.4, p.z));
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, piedra), p.x, p.y + 0.4, p.z, 60, 0.8, 0.4, 0.8, 0.35);
        if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), p.x, p.y + 0.2, p.z, 40, 1.2, 0.2, 1.2, 0.25);
        sl.sendParticles(ParticleTypes.EXPLOSION, p.x, p.y + 0.5, p.z, 2, 0.6, 0.2, 0.6, 0);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, p.x, p.y + 0.4, p.z, 16, 1.0, 0.3, 1.0, 0.02);
        sl.playSound(null, p.x, p.y, p.z, DarkCraftMod.SFX_GIGANTE_ROCA.get(), SoundSource.HOSTILE, 3.5f, 0.85f + random.nextFloat() * 0.15f);
        Entity o = getOwner();
        float golpe = o instanceof LivingEntity le ? (float) le.getAttributeValue(Attributes.ATTACK_DAMAGE) : 16f;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, new AABB(p.x - RADIO - 1, p.y - 3, p.z - RADIO - 1, p.x + RADIO + 1, p.y + 3, p.z + RADIO + 1),
                e -> e.isAlive() && !(e instanceof Enemigo) && !(e instanceof net.minecraft.world.entity.player.Player pl && (pl.isCreative() || pl.isSpectator())))) {
            double d = Math.sqrt(le.distanceToSqr(p.x, le.getY() + le.getBbHeight() * 0.5, p.z));
            if (d > RADIO + le.getBbWidth() * 0.5) continue;
            boolean lleno = d < 1.3 + le.getBbWidth() * 0.5;
            if (le.hurt(damageSources().thrown(this, o), golpe * (lleno ? LLENO : ALREDEDOR))) {
                DAN++;
                le.knockback(lleno ? 1.2 : 0.6, p.x - le.getX(), p.z - le.getZ());
                le.setDeltaMovement(le.getDeltaMovement().add(0, lleno ? 0.45 : 0.3, 0));
                le.hurtMarked = true;
            }
        }
    }
}
