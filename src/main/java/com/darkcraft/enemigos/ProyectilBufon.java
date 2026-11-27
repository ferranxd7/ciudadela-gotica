package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
 * Lo que tira Pell, el Bufon del Rey (parte 7y): sus <b>cuchillos</b> (van rectos y deprisa, casi sin caer) y las <b>bolas de
 * fuego</b> de sus malabares (en arco; al dar en algo revientan en llamas: queman a quien pillan y dejan un poco de fuego en
 * el suelo). Lo que se ve es su objeto ({@code cuchillo_bufon} / {@code bola_bufon}). A los enemigos no les da.
 */
public class ProyectilBufon extends ThrowableItemProjectile {
    /** La bola de fuego: el radio de las llamas al reventar, y el charco que deja (radio y ticks). */
    public static final float R_BOLA = 1.7f, R_CHARCO = 1.3f;
    public static final int CHARCO = 50;
    public static final double GRAVEDAD_BOLA = Cantaro.GRAVEDAD, GRAVEDAD_CUCHILLO = 0.012;
    private float dano = 4f;
    private int edad;
    /** Para la prueba: los cuchillos y las bolas que dan de lleno, y las bolas que revientan. */
    public static int CUCHILLOS_DAN, BOLAS_DAN, BOLAS_REVIENTAN;

    public ProyectilBufon(EntityType<? extends ProyectilBufon> t, Level l) {
        super(t, l);
    }

    public ProyectilBufon(Level l, LivingEntity dueno, boolean bola, float dano) {
        super(DarkCraftMod.PROYECTIL_BUFON.get(), dueno, l);
        setItem(new ItemStack(bola ? DarkCraftMod.BOLA_BUFON.get() : DarkCraftMod.CUCHILLO_BUFON.get()));
        this.dano = dano;
    }

    @Override
    protected Item getDefaultItem() {
        return DarkCraftMod.CUCHILLO_BUFON.get();
    }

    /** Si es una bola de fuego (si no, un cuchillo). */
    public boolean bola() {
        return getItem().is(DarkCraftMod.BOLA_BUFON.get());
    }

    @Override
    protected float getGravity() {
        return (float) (bola() ? GRAVEDAD_BOLA : GRAVEDAD_CUCHILLO);
    }

    /** La velocidad de una bola para caer en (tx, ty, tz) saliendo de (ox, oy, oz): el arco de los cantaros. */
    public static Vec3 arco(double ox, double oy, double oz, double tx, double ty, double tz) {
        return Cantaro.tiro(ox, oy, oz, tx, ty, tz);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (bola()) {
                level().addParticle(ParticleTypes.FLAME, getX(), getY() + 0.1, getZ(), 0, 0.01, 0);
                if (random.nextInt(3) == 0) level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.2, getZ(), 0, 0.02, 0);
            } else if (random.nextInt(3) == 0) level().addParticle(ParticleTypes.CRIT, getX(), getY(), getZ(), 0, 0, 0);
            return;
        }
        if (++edad > 90) discard();
    }

    @Override
    protected boolean canHitEntity(Entity e) {
        return super.canHitEntity(e) && !(e instanceof Enemigo);
    }

    @Override
    protected void onHitEntity(EntityHitResult r) {
        if (level().isClientSide) return;
        if (!(r.getEntity() instanceof LivingEntity le)) return;
        if (le.hurt(damageSources().thrown(this, getOwner()), dano)) {
            if (bola()) {
                BOLAS_DAN++;
                le.setRemainingFireTicks(Math.max(le.getRemainingFireTicks(), 60));
            } else CUCHILLOS_DAN++;
        }
    }

    @Override
    protected void onHit(HitResult r) {
        super.onHit(r);
        if (level().isClientSide) return;
        Vec3 p = r.getLocation();
        if (bola()) {
            if (r instanceof BlockHitResult b && b.getDirection().getAxis().isHorizontal()) p = p.add(Vec3.atLowerCornerOf(b.getDirection().getNormal()).scale(0.3));
            reventar(p, r instanceof EntityHitResult e ? e.getEntity() : null);
        } else if (level() instanceof ServerLevel sl) {
            sl.playSound(null, p.x, p.y, p.z, SoundEvents.TRIDENT_HIT_GROUND, SoundSource.HOSTILE, 0.8f, 1.6f + random.nextFloat() * 0.2f);
            sl.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 6, 0.1, 0.1, 0.1, 0.1);
        }
        discard();
    }

    /** La bola revienta: llamas alrededor (quema a quien pilla) y un poco de fuego en el suelo. */
    private void reventar(Vec3 p, Entity yaDado) {
        if (!(level() instanceof ServerLevel sl)) return;
        BOLAS_REVIENTAN++;
        sl.sendParticles(ParticleTypes.FLAME, p.x, p.y + 0.3, p.z, 36, 0.7, 0.3, 0.7, 0.06);
        sl.sendParticles(ParticleTypes.LAVA, p.x, p.y + 0.3, p.z, 5, 0.4, 0.2, 0.4, 0.0);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, p.x, p.y + 0.5, p.z, 8, 0.5, 0.3, 0.5, 0.02);
        sl.playSound(null, p.x, p.y, p.z, DarkCraftMod.SFX_BUFON_BOLA.get(), SoundSource.HOSTILE, 1.5f, 0.95f + random.nextFloat() * 0.1f);
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, new AABB(p.x - R_BOLA, p.y - 1, p.z - R_BOLA, p.x + R_BOLA, p.y + 2, p.z + R_BOLA),
                e -> e.isAlive() && e != yaDado && !(e instanceof Enemigo))) {
            if (le.distanceToSqr(p.x, le.getY(), p.z) > R_BOLA * R_BOLA) continue;
            if (le.hurt(damageSources().thrown(this, getOwner()), dano * 0.6f)) le.setRemainingFireTicks(Math.max(le.getRemainingFireTicks(), 40));
        }
        // el fuego que queda en el suelo de debajo (como mucho tres bloques)
        int bx = Mth.floor(p.x), bz = Mth.floor(p.z), by = Mth.floor(p.y + 0.5);
        for (int k = by; k >= by - 3; k--) {
            BlockPos q = new BlockPos(bx, k, bz);
            if (!sl.getBlockState(q).getCollisionShape(sl, q).isEmpty() && sl.getBlockState(q.above()).getCollisionShape(sl, q.above()).isEmpty()) {
                double y = k + sl.getBlockState(q).getCollisionShape(sl, q).max(net.minecraft.core.Direction.Axis.Y);
                if (!sl.getFluidState(BlockPos.containing(p.x, y, p.z)).isSource())
                    sl.addFreshEntity(new Cantaro.Llamas(sl, p.x, y, p.z, getOwner() instanceof LivingEntity le ? le : null, R_CHARCO, CHARCO, false));
                break;
            }
        }
    }
}
