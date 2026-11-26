package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AreaEffectCloud;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * El cantaro de pez encendido que lanza el incendiario calcinado (parte 7m): vuela en arco (se ve el cantaro girando y su
 * mecha) y al dar en algo revienta: a quien pille de lleno le quema, y alli el suelo arde un rato ({@link Llamas}). A los
 * enemigos no les da.
 */
public class Cantaro extends ThrowableItemProjectile {
    /** Lo que cae por tick. */
    public static final double GRAVEDAD = 0.05;
    public static final float DANO = 3.0f;
    /** Lo que arde el suelo donde revienta: radio y ticks. */
    public static final float R_LLAMAS = 1.8f;
    public static final int LLAMAS = 70;
    private int edad;
    /** Para la prueba: los que han reventado, los que han dado de lleno y lo que han quemado las llamas del suelo. */
    public static int REVIENTAN, DAN, LLAMAS_QUEMAN;

    public Cantaro(EntityType<? extends Cantaro> t, Level l) {
        super(t, l);
    }

    public Cantaro(Level l, LivingEntity dueno) {
        super(DarkCraftMod.CANTARO.get(), dueno, l);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.DECORATED_POT;
    }

    @Override
    protected float getGravity() {
        return (float) GRAVEDAD;
    }

    /**
     * La velocidad para caer en (tx, ty, tz) saliendo de (ox, oy, oz): un arco que tarda mas cuanto mas lejos (con lo que se
     * come el aire, un 1 % por tick, en cuenta a ojo).
     */
    public static Vec3 tiro(double ox, double oy, double oz, double tx, double ty, double tz) {
        double dx = tx - ox, dz = tz - oz, dy = ty - oy, h = Math.sqrt(dx * dx + dz * dz);
        double t = Mth.clamp(10 + h * 1.1, 10, 34);
        double k = 1 + 0.0055 * t;
        return new Vec3(dx / t * k, (dy + 0.5 * GRAVEDAD * t * t) / t * k, dz / t * k);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            // la mecha encendida y el humo de la pez
            level().addParticle(ParticleTypes.FLAME, getX(), getY() + 0.25, getZ(), 0, 0.01, 0);
            if (random.nextInt(2) == 0) level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.3, getZ(), 0, 0.02, 0);
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
            Calcinado.quemar(le, 4);
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

    /** Revienta: trozos de barro, la pez que salpica ardiendo y, en el suelo de debajo, las llamas. */
    private void reventar(Vec3 p) {
        if (!(level() instanceof ServerLevel sl)) return;
        REVIENTAN++;
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.TERRACOTTA.defaultBlockState()), p.x, p.y + 0.2, p.z, 18, 0.2, 0.2, 0.2, 0.15);
        sl.sendParticles(ParticleTypes.FLAME, p.x, p.y + 0.3, p.z, 30, 0.6, 0.3, 0.6, 0.06);
        sl.sendParticles(ParticleTypes.LAVA, p.x, p.y + 0.3, p.z, 6, 0.4, 0.2, 0.4, 0.0);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, p.x, p.y + 0.5, p.z, 8, 0.4, 0.3, 0.4, 0.02);
        sl.playSound(null, p.x, p.y, p.z, DarkCraftMod.SFX_CALCINADO_CANTARO.get(), SoundSource.HOSTILE, 1.3f, 0.9f + random.nextFloat() * 0.2f);
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
        sl.addFreshEntity(new Llamas(sl, p.x, y, p.z, getOwner() instanceof LivingEntity le ? le : null));
    }

    /**
     * Las llamas de la pez en el suelo: la nube de Minecraft (sin efectos) con llamas por particula, que cada medio segundo
     * quema a quien este dentro (no a los calcinados) y se va encogiendo. No se guarda como tal (al volver a cargar seria una
     * nube vacia que se acaba enseguida).
     */
    public static class Llamas extends AreaEffectCloud {
        /** Si suena la brea ardiendo al prender (las filas de fuego del Condestable, parte 7n, solo la primera). */
        private final boolean suena;

        public Llamas(Level l, double x, double y, double z, LivingEntity dueno) {
            this(l, x, y, z, dueno, R_LLAMAS, LLAMAS, true);
        }

        /** Unas llamas de otro tamano y otra duracion (las del montante del Condestable: mas pequenas y mas cortas). */
        public Llamas(Level l, double x, double y, double z, LivingEntity dueno, float radio, int ticks, boolean suena) {
            super(l, x, y, z);
            this.suena = suena;
            if (dueno != null) setOwner(dueno);
            setRadius(radio);
            setRadiusOnUse(0f);
            setWaitTime(0);
            setDuration(ticks);
            setRadiusPerTick(-radio * 0.35f / ticks);
            setParticle(ParticleTypes.FLAME);
        }

        @Override
        public void tick() {
            super.tick();
            if (!(level() instanceof ServerLevel sl) || isRemoved()) return;
            float r = getRadius();
            if (tickCount % 4 == 0) sl.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.3, getZ(), 3, r * 0.5, 0.1, r * 0.5, 0.01);
            // la brea ardiendo suena una vez: dura (3 s, con su fundido) lo que duran las llamas
            if (tickCount == 1 && suena)
                sl.playSound(null, getX(), getY(), getZ(), DarkCraftMod.SFX_CALCINADO_FUEGO.get(), SoundSource.HOSTILE, 0.9f, 0.9f + random.nextFloat() * 0.2f);
            if (tickCount % 10 != 0) return;
            for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, new AABB(getX() - r, getY() - 0.5, getZ() - r, getX() + r, getY() + 1.5, getZ() + r),
                    o -> o.isAlive() && !(o instanceof Calcinado) && !o.fireImmune() && !(o instanceof Player p && (p.isCreative() || p.isSpectator())))) {
                if (le.distanceToSqr(getX(), le.getY(), getZ()) > r * r) continue;
                if (le.hurt(damageSources().inFire(), 2.0f)) LLAMAS_QUEMAN++;
                Calcinado.quemar(le, 3);
            }
        }
    }
}
