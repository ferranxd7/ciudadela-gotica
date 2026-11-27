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
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * El farol de aceite de ballena que lanza Brann (parte 7r): vuela en arco (se ve el farol girando, con su llama) y al dar en
 * algo revienta: los cristales, el aceite que salpica ardiendo y, en el suelo de debajo, un charco de aceite que arde un buen
 * rato ({@link Cantaro.Llamas}, mas grande y mas largo que el del cantaro de los calcinados). A los enemigos no les da.
 */
public class FarolAceite extends ThrowableItemProjectile {
    public static final float DANO = 3.5f;
    /** El charco de aceite que arde: radio y ticks. */
    public static final float R_CHARCO = 2.6f;
    public static final int CHARCO = 110;
    private int edad;
    /** Para la prueba: los que han reventado y los que han dado de lleno. */
    public static int REVIENTAN, DAN;

    public FarolAceite(EntityType<? extends FarolAceite> t, Level l) {
        super(t, l);
    }

    public FarolAceite(Level l, LivingEntity dueno) {
        super(DarkCraftMod.FAROL_ACEITE.get(), dueno, l);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.LANTERN;
    }

    @Override
    protected float getGravity() {
        return (float) Cantaro.GRAVEDAD;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.FLAME, getX(), getY() + 0.2, getZ(), 0, 0.01, 0);
            if (random.nextInt(3) == 0) level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.3, getZ(), 0, 0.02, 0);
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
        if (level().isClientSide) return;
        if (r.getEntity() instanceof LivingEntity le && le.hurt(damageSources().thrown(this, getOwner()), DANO)) {
            DAN++;
            Calcinado.quemar(le, 5);
        }
    }

    @Override
    protected void onHit(HitResult r) {
        super.onHit(r);
        if (level().isClientSide) return;
        Vec3 p = r.getLocation();
        if (r instanceof BlockHitResult b && b.getDirection().getAxis().isHorizontal()) p = p.add(Vec3.atLowerCornerOf(b.getDirection().getNormal()).scale(0.3));
        reventar(p);
        discard();
    }

    /** Revienta: los cristales, el aceite ardiendo que salpica y, en el suelo de debajo, el charco que arde. */
    private void reventar(Vec3 p) {
        if (!(level() instanceof ServerLevel sl)) return;
        REVIENTAN++;
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GLASS.defaultBlockState()), p.x, p.y + 0.2, p.z, 16, 0.2, 0.2, 0.2, 0.15);
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GOLD_BLOCK.defaultBlockState()), p.x, p.y + 0.2, p.z, 6, 0.2, 0.2, 0.2, 0.1);
        sl.sendParticles(ParticleTypes.FLAME, p.x, p.y + 0.3, p.z, 40, 0.9, 0.3, 0.9, 0.07);
        sl.sendParticles(ParticleTypes.LAVA, p.x, p.y + 0.3, p.z, 8, 0.6, 0.2, 0.6, 0.0);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, p.x, p.y + 0.5, p.z, 10, 0.6, 0.3, 0.6, 0.02);
        sl.playSound(null, p.x, p.y, p.z, DarkCraftMod.SFX_BALLENERO_ACEITE.get(), SoundSource.HOSTILE, 1.6f, 0.9f + random.nextFloat() * 0.2f);
        // el suelo de debajo (como mucho cuatro bloques)
        int bx = Mth.floor(p.x), bz = Mth.floor(p.z), by = Mth.floor(p.y + 0.5);
        double y = Double.NaN;
        for (int k = by; k >= by - 4; k--) {
            BlockPos q = new BlockPos(bx, k, bz);
            if (!sl.getBlockState(q).getCollisionShape(sl, q).isEmpty() && sl.getBlockState(q.above()).getCollisionShape(sl, q.above()).isEmpty()) {
                y = k + 1 + sl.getBlockState(q).getCollisionShape(sl, q).max(net.minecraft.core.Direction.Axis.Y) - 1;
                break;
            }
        }
        if (Double.isNaN(y) || sl.getFluidState(BlockPos.containing(p.x, y, p.z)).isSource()) return;
        sl.addFreshEntity(new Cantaro.Llamas(sl, p.x, y, p.z, getOwner() instanceof LivingEntity le ? le : null, R_CHARCO, CHARCO, false));
    }
}
