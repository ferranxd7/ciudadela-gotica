package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * La bola de fuego que escupe Vorthak desde el aire (parte 7z): cae en arco (pesa menos que las del bufon) dejando un rastro de
 * llamas y humo; al dar en algo revienta: quema a quien pilla alrededor, le aparta y deja el suelo ardiendo un rato. Lo que se ve
 * es su objeto ({@code bola_dragon}), grande y a plena luz. A los enemigos no les da.
 */
public class ProyectilDragon extends ThrowableItemProjectile {
    /** El radio de la explosion, el del fuego que deja en el suelo y lo que dura (ticks). */
    public static final float R_BOLA = 3.2f, R_FUEGO = 2.2f;
    public static final int FUEGO = 90;
    public static final double GRAVEDAD = 0.03;
    private float dano = 12f;
    private int edad;
    /** Para la prueba: las bolas que dan de lleno, las que revientan y a cuantos queman al reventar. */
    public static int DAN, REVIENTAN, QUEMAN;

    public ProyectilDragon(EntityType<? extends ProyectilDragon> t, Level l) {
        super(t, l);
    }

    public ProyectilDragon(Level l, LivingEntity dueno, float dano) {
        super(DarkCraftMod.PROYECTIL_DRAGON.get(), dueno, l);
        setItem(new ItemStack(DarkCraftMod.BOLA_DRAGON.get()));
        this.dano = dano;
    }

    @Override
    protected Item getDefaultItem() {
        return DarkCraftMod.BOLA_DRAGON.get();
    }

    @Override
    protected float getGravity() {
        return (float) GRAVEDAD;
    }

    /**
     * La velocidad para caer en (tx, ty, tz) saliendo de (ox, oy, oz): un arco que tarda mas cuanto mas lejos (con lo que frena el
     * aire, un 1 % por tick, en cuenta a ojo; como los cantaros, con su gravedad).
     */
    public static Vec3 tiro(double ox, double oy, double oz, double tx, double ty, double tz) {
        double dx = tx - ox, dz = tz - oz, dy = ty - oy, h = Math.sqrt(dx * dx + dz * dz);
        double t = Mth.clamp(12 + h * 0.7, 14, 40);
        double k = 1 + 0.0055 * t;
        return new Vec3(dx / t * k, (dy + 0.5 * GRAVEDAD * t * t) / t * k, dz / t * k);
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            // el rastro: llamas, humo y alguna gota de lava
            for (int k = 0; k < 3; k++)
                level().addParticle(ParticleTypes.FLAME, getX() + (random.nextDouble() - 0.5) * 0.6, getY() + 0.25 + (random.nextDouble() - 0.5) * 0.6,
                        getZ() + (random.nextDouble() - 0.5) * 0.6, 0, 0.01, 0);
            level().addParticle(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.3, getZ(), 0, 0.02, 0);
            if (random.nextInt(4) == 0) level().addParticle(ParticleTypes.LAVA, getX(), getY(), getZ(), 0, 0, 0);
            return;
        }
        if (++edad > 140) discard();
    }

    @Override
    protected boolean canHitEntity(Entity e) {
        return super.canHitEntity(e) && !(e instanceof Enemigo);
    }

    private DamageSource fuego() {
        return new DamageSource(level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.FIREBALL), this,
                getOwner());
    }

    @Override
    protected void onHitEntity(EntityHitResult r) {
        if (level().isClientSide) return;
        if (!(r.getEntity() instanceof LivingEntity le)) return;
        if (le.hurt(fuego(), dano)) {
            DAN++;
            Calcinado.quemar(le, 4);
        }
    }

    @Override
    protected void onHit(HitResult r) {
        super.onHit(r);
        if (level().isClientSide) return;
        Vec3 p = r.getLocation();
        if (r instanceof BlockHitResult b && b.getDirection().getAxis().isHorizontal())
            p = p.add(Vec3.atLowerCornerOf(b.getDirection().getNormal()).scale(0.4));
        reventar(p, r instanceof EntityHitResult e ? e.getEntity() : null);
        discard();
    }

    /** Revienta: llamas y humo, quema y aparta a los de alrededor y deja el suelo de debajo ardiendo. */
    private void reventar(Vec3 p, Entity yaDado) {
        if (!(level() instanceof ServerLevel sl)) return;
        REVIENTAN++;
        sl.sendParticles(ParticleTypes.EXPLOSION, p.x, p.y + 0.5, p.z, 3, 0.8, 0.4, 0.8, 0.0);
        sl.sendParticles(ParticleTypes.FLAME, p.x, p.y + 0.4, p.z, 60, 1.2, 0.5, 1.2, 0.1);
        sl.sendParticles(ParticleTypes.LAVA, p.x, p.y + 0.4, p.z, 10, 0.8, 0.3, 0.8, 0.0);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, p.x, p.y + 0.8, p.z, 16, 1.0, 0.6, 1.0, 0.03);
        sl.playSound(null, p.x, p.y, p.z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.6f, 0.8f + random.nextFloat() * 0.15f);
        sl.playSound(null, p.x, p.y, p.z, DarkCraftMod.SFX_DRAGON_ASCUAS.get(), SoundSource.HOSTILE, 1.4f, 1.1f);
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, new AABB(p.x - R_BOLA, p.y - 2, p.z - R_BOLA, p.x + R_BOLA, p.y + 3, p.z + R_BOLA),
                e -> e.isAlive() && e != yaDado && !(e instanceof Enemigo))) {
            double dx = le.getX() - p.x, dz = le.getZ() - p.z, d = Math.sqrt(dx * dx + dz * dz);
            if (d > R_BOLA) continue;
            if (le.hurt(fuego(), dano * 0.7f)) {
                QUEMAN++;
                Calcinado.quemar(le, 3);
                if (d > 0.05) le.knockback(0.7, -dx / d, -dz / d);
                le.hurtMarked = true;
            }
        }
        // el fuego que queda en el suelo de debajo (como mucho cuatro bloques)
        int bx = Mth.floor(p.x), bz = Mth.floor(p.z), by = Mth.floor(p.y + 0.5);
        for (int k = by; k >= by - 4; k--) {
            BlockPos q = new BlockPos(bx, k, bz);
            if (!sl.getBlockState(q).getCollisionShape(sl, q).isEmpty() && sl.getBlockState(q.above()).getCollisionShape(sl, q.above()).isEmpty()) {
                double y = k + sl.getBlockState(q).getCollisionShape(sl, q).max(net.minecraft.core.Direction.Axis.Y);
                if (!sl.getFluidState(BlockPos.containing(p.x, y, p.z)).isSource())
                    sl.addFreshEntity(new Cantaro.Llamas(sl, p.x, y, p.z, getOwner() instanceof LivingEntity le ? le : null, R_FUEGO, FUEGO, false));
                break;
            }
        }
    }
}
