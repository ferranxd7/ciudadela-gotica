package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Lo que lanzan los Destilados del Lazareto (parte 7au): los <b>frascos</b> de los mancebos, que vuelan en arco (se ve un frasco de
 * su color girando) y revientan al dar en algo, y el <b>escupitajo</b> de los enfermos, que va mas derecho.
 * <ul>
 * <li>{@link #FUEGO}: quema a quien pille de lleno y el suelo arde un rato;</li>
 * <li>{@link #ACIDO}: a quien pille, lo frena; deja un charco que corroe (dano magico, frena y se come la armadura);</li>
 * <li>{@link #CURA}: el elixir verde que el mancebo le lanza a uno de los suyos herido: cura a los destilados de alrededor (y un
 * momento mas, el charco);</li>
 * <li>{@link #ESCUPITAJO}: envenena a quien pille.</li>
 * </ul>
 * A los enemigos no les da (los atraviesa) y los charcos no les hacen nada.
 */
public class Frasco extends ThrowableItemProjectile {
    public static final int FUEGO = 0, ACIDO = 1, CURA = 2, ESCUPITAJO = 3;
    /** Lo que cae por tick (como el cantaro de los calcinados: los mismos arcos). */
    public static final double GRAVEDAD = 0.05;
    /** Los charcos que dejan: radio y ticks. */
    public static final float R_FUEGO = 1.7f, R_ACIDO = 1.8f, R_CURA = 2.2f, R_CURA_ELIXIR = 3.2f;
    public static final int T_FUEGO = 70, T_ACIDO = 100, T_CURA = 50;
    private static final EntityDataAccessor<Byte> TIPO = SynchedEntityData.defineId(Frasco.class, EntityDataSerializers.BYTE);
    static final DustParticleOptions VERDE_ACIDO = new DustParticleOptions(new Vector3f(0.62f, 0.85f, 0.18f), 1.1f),
            VERDE_CURA = new DustParticleOptions(new Vector3f(0.32f, 1.0f, 0.48f), 1.2f),
            BABA = new DustParticleOptions(new Vector3f(0.46f, 0.6f, 0.28f), 0.9f);
    static final BlockParticleOption CRISTAL = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GLASS.defaultBlockState());
    private int edad;

    /** Para la prueba: los lanzados, los que revientan, los que dan de lleno, lo que queman y corroen los charcos y lo que curan. */
    public static int LANZADOS, REVIENTAN, DAN, QUEMAN, CORROEN, CURAN, ESCUPEN, ARMADURA;

    public Frasco(EntityType<? extends Frasco> t, Level l) {
        super(t, l);
    }

    public Frasco(Level l, LivingEntity dueno, int tipo) {
        super(DarkCraftMod.FRASCO_DESTILADO.get(), dueno, l);
        tipo(tipo);
        LANZADOS++;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TIPO, (byte) 0);
    }

    public int tipo() {
        return entityData.get(TIPO);
    }

    public void tipo(int t) {
        entityData.set(TIPO, (byte) t);
        setItem(objeto(t));
    }

    /** Lo que se ve volar: un frasco arrojadizo del color de lo que lleva (el escupitajo, una bola verdosa). */
    public static ItemStack objeto(int t) {
        if (t == ESCUPITAJO) return new ItemStack(Items.SLIME_BALL);
        ItemStack s = new ItemStack(Items.SPLASH_POTION);
        s.getOrCreateTag().putInt("CustomPotionColor", switch (t) {
            case FUEGO -> 0xE8641C;
            case ACIDO -> 0xB8D838;
            default -> 0x3CF078;
        });
        return s;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SPLASH_POTION;
    }

    @Override
    protected float getGravity() {
        return (float) GRAVEDAD;
    }

    /** La velocidad para que el escupitajo caiga en (tx, ty, tz): mas derecho y mas deprisa que un frasco. */
    public static Vec3 directo(double ox, double oy, double oz, double tx, double ty, double tz) {
        double dx = tx - ox, dz = tz - oz, dy = ty - oy, h = Math.sqrt(dx * dx + dz * dz);
        double t = Mth.clamp(4 + h * 0.55, 5, 14);
        double k = 1 + 0.0055 * t;
        return new Vec3(dx / t * k, (dy + 0.5 * GRAVEDAD * t * t) / t * k, dz / t * k);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            switch (tipo()) {
                case FUEGO -> level().addParticle(ParticleTypes.SMALL_FLAME, getX(), getY() + 0.15, getZ(), 0, 0.01, 0);
                case ACIDO -> {
                    if (random.nextInt(2) == 0) level().addParticle(VERDE_ACIDO, getX(), getY() + 0.1, getZ(), 0, 0, 0);
                }
                case CURA -> {
                    if (random.nextInt(3) == 0) level().addParticle(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 0.1, getZ(), 0, 0, 0);
                }
                default -> level().addParticle(BABA, getX(), getY(), getZ(), 0, 0, 0);
            }
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
        if (level().isClientSide || !(r.getEntity() instanceof LivingEntity le)) return;
        int t = tipo();
        if (t == CURA) return;
        if (!le.hurt(damageSources().thrown(this, getOwner()), t == ESCUPITAJO ? 2.5f : 2.0f)) return;
        DAN++;
        switch (t) {
            case FUEGO -> {
                if (!le.fireImmune()) le.setSecondsOnFire(4);
            }
            case ACIDO -> le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 0), getOwner());
            default -> {
                le.addEffect(new MobEffectInstance(MobEffects.POISON, 70, 0), getOwner());
                ESCUPEN++;
            }
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

    /** Revienta: cristal y lo que llevaba (llamas, acido, el elixir verde) y, en el suelo de debajo, su charco. */
    private void reventar(Vec3 p) {
        if (!(level() instanceof ServerLevel sl)) return;
        REVIENTAN++;
        int t = tipo();
        LivingEntity dueno = getOwner() instanceof LivingEntity le ? le : null;
        if (t == ESCUPITAJO) {
            sl.sendParticles(BABA, p.x, p.y + 0.1, p.z, 14, 0.25, 0.1, 0.25, 0.02);
            sl.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SLIME_BALL)), p.x, p.y + 0.1, p.z, 6, 0.2, 0.1, 0.2, 0.08);
            sl.playSound(null, p.x, p.y, p.z, DarkCraftMod.SFX_FRASCO_ACIDO.get(), SoundSource.HOSTILE, 0.5f, 1.45f);
            return;
        }
        sl.sendParticles(CRISTAL, p.x, p.y + 0.2, p.z, 14, 0.2, 0.15, 0.2, 0.12);
        switch (t) {
            case FUEGO -> {
                sl.sendParticles(ParticleTypes.FLAME, p.x, p.y + 0.3, p.z, 26, 0.5, 0.3, 0.5, 0.05);
                sl.sendParticles(ParticleTypes.LAVA, p.x, p.y + 0.3, p.z, 4, 0.3, 0.2, 0.3, 0.0);
                sl.playSound(null, p.x, p.y, p.z, DarkCraftMod.SFX_FRASCO_FUEGO.get(), SoundSource.HOSTILE, 1.2f, 0.9f + random.nextFloat() * 0.2f);
            }
            case ACIDO -> {
                sl.sendParticles(VERDE_ACIDO, p.x, p.y + 0.2, p.z, 24, 0.5, 0.2, 0.5, 0.02);
                sl.sendParticles(ParticleTypes.SMOKE, p.x, p.y + 0.4, p.z, 10, 0.4, 0.2, 0.4, 0.02);
                sl.playSound(null, p.x, p.y, p.z, DarkCraftMod.SFX_FRASCO_ACIDO.get(), SoundSource.HOSTILE, 1.1f, 0.9f + random.nextFloat() * 0.2f);
            }
            default -> {
                sl.sendParticles(VERDE_CURA, p.x, p.y + 0.3, p.z, 30, 0.8, 0.4, 0.8, 0.02);
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, p.x, p.y + 0.4, p.z, 14, 0.9, 0.4, 0.9, 0.02);
                sl.playSound(null, p.x, p.y, p.z, DarkCraftMod.SFX_FRASCO_CURA.get(), SoundSource.HOSTILE, 1.0f, 0.95f + random.nextFloat() * 0.1f);
                curar(sl, p, R_CURA_ELIXIR);
            }
        }
        double y = suelo(sl, p);
        if (Double.isNaN(y)) return;
        sl.addFreshEntity(switch (t) {
            case FUEGO -> new Charco(sl, p.x, y, p.z, dueno, FUEGO, R_FUEGO, T_FUEGO);
            case ACIDO -> new Charco(sl, p.x, y, p.z, dueno, ACIDO, R_ACIDO, T_ACIDO);
            default -> new Charco(sl, p.x, y, p.z, dueno, CURA, R_CURA, T_CURA);
        });
    }

    /** El suelo de debajo de p (como mucho cuatro bloques), o NaN (sin suelo, o en el agua). */
    public static double suelo(ServerLevel sl, Vec3 p) {
        int bx = Mth.floor(p.x), bz = Mth.floor(p.z), by = Mth.floor(p.y + 0.5);
        for (int k = by; k >= by - 4; k--) {
            BlockPos q = new BlockPos(bx, k, bz);
            if (!sl.getBlockState(q).getCollisionShape(sl, q).isEmpty() && sl.getBlockState(q.above()).getCollisionShape(sl, q.above()).isEmpty()) {
                double y = k + sl.getBlockState(q).getCollisionShape(sl, q).max(net.minecraft.core.Direction.Axis.Y);
                return sl.getFluidState(BlockPos.containing(p.x, y, p.z)).isSource() ? Double.NaN : y;
            }
        }
        return Double.NaN;
    }

    /** El elixir verde: cura a los destilados de alrededor (6 y un cuarto de su vida). Devuelve a cuantos. */
    public static int curar(ServerLevel sl, Vec3 p, double r) {
        int n = 0;
        for (Destilado d : sl.getEntitiesOfClass(Destilado.class, new AABB(p.x - r, p.y - 2, p.z - r, p.x + r, p.y + 3, p.z + r), e -> e.isAlive() && !e.muriendo())) {
            if (d.distanceToSqr(p.x, d.getY(), p.z) > r * r) continue;
            float antes = d.getHealth();
            d.heal(6f + d.getMaxHealth() * 0.25f);
            if (d.getHealth() > antes) {
                n++;
                CURAN++;
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, d.getX(), d.getY() + d.getBbHeight() * 0.6, d.getZ(), 8, 0.3, 0.5, 0.3, 0.02);
            }
        }
        return n;
    }

    /**
     * El charco que deja un frasco (o la cuba al reventar): la nube de Minecraft (sin efectos) con sus particulas, que cada medio
     * segundo quema ({@link #FUEGO}), corroe ({@link #ACIDO}: dano magico, frena y gasta la armadura) o cura a los destilados
     * ({@link #CURA}), y se va encogiendo. No se guarda como tal.
     */
    public static class Charco extends AreaEffectCloud {
        private final int tipo;

        public Charco(Level l, double x, double y, double z, LivingEntity dueno, int tipo, float radio, int ticks) {
            super(l, x, y, z);
            this.tipo = tipo;
            if (dueno != null) setOwner(dueno);
            setRadius(radio);
            setRadiusOnUse(0f);
            setWaitTime(0);
            setDuration(ticks);
            setRadiusPerTick(-radio * 0.3f / ticks);
            setParticle(tipo == FUEGO ? ParticleTypes.FLAME : tipo == ACIDO ? VERDE_ACIDO : VERDE_CURA);
        }

        public int tipo() {
            return tipo;
        }

        @Override
        public void tick() {
            super.tick();
            if (!(level() instanceof ServerLevel sl) || isRemoved()) return;
            float r = getRadius();
            if (tickCount % 5 == 0) {
                switch (tipo) {
                    case FUEGO -> sl.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.3, getZ(), 3, r * 0.5, 0.1, r * 0.5, 0.01);
                    case ACIDO -> sl.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.2, getZ(), 2, r * 0.5, 0.05, r * 0.5, 0.005);
                    default -> sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 0.3, getZ(), 2, r * 0.5, 0.1, r * 0.5, 0.01);
                }
            }
            if (tickCount % 10 != 0) return;
            AABB caja = new AABB(getX() - r, getY() - 0.5, getZ() - r, getX() + r, getY() + 1.5, getZ() + r);
            if (tipo == CURA) {
                for (Destilado d : sl.getEntitiesOfClass(Destilado.class, caja, e -> e.isAlive() && !e.muriendo())) {
                    if (d.distanceToSqr(getX(), d.getY(), getZ()) > r * r || d.getHealth() >= d.getMaxHealth()) continue;
                    d.heal(2.0f);
                    CURAN++;
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, d.getX(), d.getY() + 1.0, d.getZ(), 3, 0.3, 0.4, 0.3, 0.01);
                }
                return;
            }
            for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, caja,
                    o -> o.isAlive() && !(o instanceof Enemigo) && !(o instanceof Player p && (p.isCreative() || p.isSpectator())))) {
                if (le.distanceToSqr(getX(), le.getY(), getZ()) > r * r) continue;
                if (tipo == FUEGO) {
                    if (le.fireImmune()) continue;
                    if (le.hurt(damageSources().inFire(), 2.0f)) QUEMAN++;
                    le.setSecondsOnFire(3);
                    continue;
                }
                if (le.hurt(damageSources().magic(), 1.5f)) CORROEN++;
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0));
                // el acido se come la armadura (un punto cada medio segundo en cada pieza)
                for (EquipmentSlot es : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                    ItemStack a = le.getItemBySlot(es);
                    if (a.isEmpty() || !a.isDamageableItem()) continue;
                    a.hurtAndBreak(1, le, x -> x.broadcastBreakEvent(es));
                    ARMADURA++;
                }
            }
        }
    }
}
