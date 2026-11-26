package com.darkcraft.montura;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.CorcelAnims;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * El corcel de hueso y niebla (parte 7b, respuesta 129 del plan): la montura de Morvain. Lo llama el silbato que da la
 * guia ({@link Montura#llamar}): aparece debajo de quien silba y le lleva. Va al trote o, con la tecla de correr, al
 * galope; salta y vuelve a saltar en el aire (con la tecla de saltar: lo pide el cliente del jinete, que es quien lo
 * mueve, como a un caballo); se pelea montado. No hace caso a nadie mas que a su dueno, no recibe golpes de el, no se
 * hace dano al caer y vadea el agua poco honda como si fuera tierra.
 * <p>
 * No se guarda con el mundo: se deshace en niebla ({@link CorcelAnims#DESHACERSE}) si se queda solo un rato, si su dueno
 * se aleja o se va, si se mete en agua honda o si lo hieren mucho (le quitan vida los golpes que van a su jinete y los
 * que le dan a el); en ese caso tarda un rato en poder volver ({@link Montura#VUELTA}). La vida que le queda se apunta en
 * la ficha de su dueno y se recupera poco a poco.
 */
public class Corcel extends PathfinderMob {
    private static final EntityDataAccessor<Byte> ACCION = SynchedEntityData.defineId(Corcel.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> SERIE = SynchedEntityData.defineId(Corcel.class, EntityDataSerializers.BYTE);

    public static final float VIDA = 40f;
    /** La velocidad al trote; al galope se multiplica por {@link #GALOPE}. */
    public static final float PASO = 0.24f, GALOPE = 1.75f;
    /** El primer salto (unos 2,6 bloques) y el segundo, en el aire (hasta unos 5,6 en total). */
    public static final double SALTO = 0.62, SALTO_DOBLE = 0.70;
    /** Lo que espera sin jinete antes de irse (ticks) y lo lejos que puede estar su dueno. */
    public static final int ESPERA = 30 * 20;
    public static final double LEJOS = 64;
    /** Donde va sentado el jinete: alto (para que el {@code -0,35} del jugador le deje sobre la silla) y adelante. */
    public static final double ASIENTO = 1.30, ASIENTO_ADELANTE = 0.14;

    /** En el cliente del jinete: si pide galope (la tecla de correr; la pone {@code ClientCorcel}). */
    public static volatile boolean galopeCliente;

    // ---- en el cliente: el reloj de los bucles y el de cada accion, y lo que corre (para mezclar paso y galope)
    public final AnimationState reloj = new AnimationState();
    public final AnimationState[] acciones = new AnimationState[CorcelAnims.TICKS.length];
    private int serieVista = -1;
    /** Bloques por tick (suavizado) y lo que avanza cada ciclo de patas (segundos de animacion). */
    public float velocidad, velocidadO, fasePaso, fasePasoO, faseGalope, faseGalopeO;
    /** Ticks seguidos en el aire (para no poner el salto en cada escalon). */
    public int enAire;
    /** Lo pide el cliente del jinete al pulsar saltar ({@code ClientCorcel}); lo hace el propio corcel en su tick. */
    public boolean saltoPedido;
    private int saltos;

    // ---- en el servidor
    private UUID dueno;
    private int solo, accionT, alarde = 200;
    private boolean deshaciendo, herido;

    public Corcel(EntityType<? extends Corcel> t, Level l) {
        super(t, l);
        for (int i = 0; i < acciones.length; i++) acciones[i] = new AnimationState();
        setMaxUpStep(1.1f);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, VIDA)
                .add(Attributes.MOVEMENT_SPEED, PASO)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ACCION, (byte) 0);
        entityData.define(SERIE, (byte) 0);
    }

    // ------------------------------------------------------------------ dueno

    public void dueno(UUID u) {
        dueno = u;
    }

    @Nullable
    public UUID dueno() {
        return dueno;
    }

    public boolean esDe(Entity e) {
        return e != null && dueno != null && dueno.equals(e.getUUID());
    }

    public boolean deshaciendo() {
        return deshaciendo;
    }

    // ------------------------------------------------------------------ montar

    @Override
    public InteractionResult mobInteract(Player p, InteractionHand mano) {
        if (deshaciendo || isVehicle() || p.isPassenger() || p.isSecondaryUseActive()) return super.mobInteract(p, mano);
        if (!esDe(p)) return InteractionResult.PASS;
        if (!level().isClientSide) p.startRiding(this);
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    protected boolean canAddPassenger(Entity e) {
        return !deshaciendo && getPassengers().isEmpty() && esDe(e);
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        return !deshaciendo && getFirstPassenger() instanceof Player p ? p : null;
    }

    @Override
    public double getPassengersRidingOffset() {
        return ASIENTO;
    }

    @Override
    protected void positionRider(Entity p, Entity.MoveFunction mover) {
        if (!hasPassenger(p)) return;
        float a = yBodyRot * Mth.DEG_TO_RAD;
        mover.accept(p, getX() - Mth.sin(a) * ASIENTO_ADELANTE, getY() + getPassengersRidingOffset() + p.getMyRidingOffset(),
                getZ() + Mth.cos(a) * ASIENTO_ADELANTE);
    }

    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    /** Se baja por un lado (el de la mano buena; si no cabe, por el otro), como de un caballo. */
    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity p) {
        for (int lado : new int[]{1, -1}) {
            float giro = getYRot() + (p.getMainArm() == HumanoidArm.RIGHT ? 90f : -90f) * lado;
            Vec3 v = bajada(getCollisionHorizontalEscapeVector(getBbWidth(), p.getBbWidth(), giro), p);
            if (v != null) return v;
        }
        return position();
    }

    @Nullable
    private Vec3 bajada(Vec3 dir, LivingEntity p) {
        double x = getX() + dir.x, z = getZ() + dir.z, tope = getBoundingBox().maxY + 0.75;
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (Pose pose : p.getDismountPoses()) {
            m.set(x, getBoundingBox().minY, z);
            while (m.getY() < tope) {
                double suelo = level().getBlockFloorHeight(m);
                if (m.getY() + suelo > tope) break;
                if (DismountHelper.isBlockFloorValid(suelo)) {
                    Vec3 v = new Vec3(x, m.getY() + suelo, z);
                    AABB caja = p.getLocalBoundsForPose(pose).move(v);
                    if (DismountHelper.canDismountTo(level(), p, caja)) {
                        p.setPose(pose);
                        return v;
                    }
                }
                m.move(0, 1, 0);
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ llevar al jinete (en el cliente del jinete)

    @Override
    protected void tickRidden(Player p, Vec3 mando) {
        super.tickRidden(p, mando);
        setRot(p.getYRot(), p.getXRot() * 0.5f);
        yRotO = yBodyRot = yHeadRot = getYRot();
        if (!isControlledByLocalInstance()) return;
        if (onGround()) saltos = 0;
        if (saltoPedido) {
            saltoPedido = false;
            if (accion() == 0 && !isInWater()) {
                if (onGround()) saltar(false, mando);
                else if (saltos < 2) saltar(true, mando);
            }
        }
    }

    /** El salto (y el segundo, en el aire): lo hace el cliente que lo mueve; el servidor pone el ruido y la niebla. */
    private void saltar(boolean doble, Vec3 mando) {
        saltos = doble ? 2 : 1;
        Vec3 v = getDeltaMovement();
        setDeltaMovement(v.x, doble ? SALTO_DOBLE : SALTO, v.z);
        hasImpulse = true;
        if (mando.z > 0) {
            float a = getYRot() * Mth.DEG_TO_RAD;
            double empuje = doble ? 0.18 : 0.3;
            setDeltaMovement(getDeltaMovement().add(-Mth.sin(a) * empuje, 0, Mth.cos(a) * empuje));
        }
        net.minecraftforge.common.ForgeHooks.onLivingJump(this);
        com.darkcraft.net.Net.CH.sendToServer(new com.darkcraft.net.Net.CorcelSalto(doble));
    }

    @Override
    protected Vec3 getRiddenInput(Player p, Vec3 mando) {
        if (accion() != 0) return Vec3.ZERO;
        float lado = p.xxa * 0.5f, frente = p.zza;
        if (frente <= 0f) frente *= 0.3f;
        return new Vec3(lado, 0, frente);
    }

    @Override
    protected float getRiddenSpeed(Player p) {
        float v = (float) getAttributeValue(Attributes.MOVEMENT_SPEED);
        return galopeCliente && p.zza > 0f ? v * GALOPE : v;
    }

    /** Vadea el agua poco honda como si fuera tierra (en la honda se deshace). */
    @Override
    public boolean isAffectedByFluids() {
        return getFluidHeight(FluidTags.WATER) > 1.1;
    }

    @Override
    public boolean causeFallDamage(float d, float m, DamageSource s) {
        return false;
    }

    @Override
    protected void checkFallDamage(double dy, boolean suelo, BlockState b, BlockPos pos) {
        resetFallDistance();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeLeashed(Player p) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    /** No se guarda con el mundo: al volver, se le llama otra vez. */
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    // ------------------------------------------------------------------ golpes

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        if (deshaciendo || isInvulnerableTo(s)) return false;
        Entity quien = s.getEntity();
        // ni su dueno ni su jinete le hacen dano (los golpes del combate ya no le tocan: Combos.valido)
        if (quien != null && (esDe(quien) || hasPassenger(quien))) return false;
        return super.hurt(s, cuanto);
    }

    /** Lo que le toca de un golpe que iba a su jinete (sin escudo ni armadura: se lo lleva el). */
    public void recibir(float cuanto) {
        if (deshaciendo || cuanto <= 0f || level().isClientSide) return;
        setHealth(getHealth() - cuanto);
        // el respingo rojo en los clientes (sin sonido: lo pone el servidor)
        level().broadcastDamageEvent(this, damageSources().generic());
        playSound(DarkCraftMod.SFX_CORCEL_DOLOR.get(), 0.9f, 0.95f + random.nextFloat() * 0.15f);
        if (getHealth() <= 0f) {
            setHealth(1f);
            deshacer(true);
        }
    }

    /** Herido de muerte no muere: se deshace en niebla y tarda en volver. */
    @Override
    public void die(DamageSource s) {
        if (level().isClientSide) return;
        setHealth(1f);
        deshacer(true);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_CORCEL_DOLOR.get();
    }

    /** Sin sonido de morir: no muere, se deshace (y ese ya suena en {@link #deshacer}). */
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected float getSoundVolume() {
        return 0.9f;
    }

    // ------------------------------------------------------------------ los cascos

    @Override
    protected float nextStep() {
        return moveDist + 1.6f;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState b) {
        if (b.liquid()) return;
        double v = Math.sqrt(getDeltaMovement().horizontalDistanceSqr());
        if (level().isClientSide) return;
        Vec3 d = new Vec3(getX() - xo, 0, getZ() - zo);
        v = Math.max(v, d.length());
        float vol = v > 0.4 ? 0.55f : 0.35f, tono = v > 0.4 ? 0.9f : 1.05f;
        playSound(DarkCraftMod.SFX_CORCEL_PASO.get(), vol, tono + random.nextFloat() * 0.12f);
    }

    // ------------------------------------------------------------------ acciones (encabritarse, deshacerse)

    public int accion() {
        return entityData.get(ACCION);
    }

    public void empezar(int a) {
        if (level().isClientSide || a <= 0 || a >= CorcelAnims.TICKS.length) return;
        entityData.set(ACCION, (byte) a);
        entityData.set(SERIE, (byte) (entityData.get(SERIE) + 1));
        accionT = 0;
    }

    /**
     * Se deshace en niebla (herido: con su animacion y tarda {@link Montura#VUELTA} en volver; si no, igual pero sin
     * espera). Su jinete se baja.
     */
    public void deshacer(boolean herido) {
        if (deshaciendo || level().isClientSide) return;
        deshaciendo = true;
        this.herido = herido;
        ejectPassengers();
        setDeltaMovement(getDeltaMovement().multiply(0.2, 1, 0.2));
        empezar(CorcelAnims.DESHACERSE);
        playSound(DarkCraftMod.SFX_CORCEL_DESHACE.get(), 1.0f, herido ? 0.9f : 1.05f);
        if (level() instanceof ServerLevel sl) niebla(sl, 14);
        Montura.alDeshacerse(this, herido);
    }

    /** Se va al momento (sin animacion): cuando su dueno le vuelve a llamar desde lejos o se va del mundo. */
    public void quitar() {
        if (level() instanceof ServerLevel sl && !isRemoved()) niebla(sl, 20);
        deshaciendo = true;
        ejectPassengers();
        discard();
    }

    void niebla(ServerLevel sl, int n) {
        sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.9, getZ(), n, 0.45, 0.5, 0.45, 0.02);
        sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 0.8, getZ(), n / 3, 0.4, 0.4, 0.4, 0.02);
    }

    // ------------------------------------------------------------------ cada tick

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) cliente();
        else servidor();
    }

    private void servidor() {
        if (deshaciendo) {
            // solo se ve: la niebla que se lo lleva
            if (level() instanceof ServerLevel sl && tickCount % 2 == 0) {
                sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.6 + random.nextDouble(), getZ(), 2, 0.4, 0.3, 0.4, 0.01);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 0.2, getZ(), 1, 0.4, 0.1, 0.4, 0.005);
            }
            if (++accionT >= CorcelAnims.TICKS[CorcelAnims.DESHACERSE]) {
                if (level() instanceof ServerLevel sl) niebla(sl, 10);
                discard();
            }
            return;
        }
        if (accion() != 0 && ++accionT >= CorcelAnims.TICKS[accion()]) {
            entityData.set(ACCION, (byte) 0);
            accionT = 0;
        }
        ServerPlayer d = dueno == null ? null : (ServerPlayer) level().getPlayerByUUID(dueno);
        if (d == null || !d.isAlive() || d.distanceToSqr(this) > LEJOS * LEJOS) {
            deshacer(false);
            return;
        }
        if (getFluidHeight(FluidTags.WATER) > 1.3) {
            if (getFirstPassenger() instanceof ServerPlayer p)
                com.darkcraft.net.Net.to(p, new com.darkcraft.net.Net.Cartel(com.darkcraft.net.Net.Cartel.AVISO, "darkcraft.corcel.agua", 0));
            deshacer(false);
            return;
        }
        if (isVehicle()) {
            solo = 0;
            // va recuperando vida poco a poco (una cada 5 s)
            if (tickCount % 100 == 0 && getHealth() < getMaxHealth()) heal(1f);
        } else {
            if (++solo >= ESPERA) {
                deshacer(false);
                return;
            }
            // de vez en cuando, mientras espera a su dueno, se encabrita y relincha
            if (accion() == 0 && --alarde <= 0) {
                alarde = 260 + random.nextInt(300);
                if (d.distanceToSqr(this) < 20 * 20) {
                    empezar(CorcelAnims.ENCABRITARSE);
                    playSound(DarkCraftMod.SFX_CORCEL_RELINCHO.get(), 1.0f, 0.95f + random.nextFloat() * 0.1f);
                }
            }
            // mira a su dueno
            getLookControl().setLookAt(d, 20f, 20f);
        }
    }

    private void cliente() {
        reloj.startIfStopped(tickCount);
        int s = entityData.get(SERIE);
        if (s != serieVista) {
            serieVista = s;
            for (AnimationState st : acciones) st.stop();
            int a = accion();
            if (a > 0 && a < acciones.length) acciones[a].start(tickCount);
        } else if (accion() == 0) {
            for (AnimationState st : acciones) st.stop();
        }
        // lo que corre (bloques por tick, solo en horizontal) y lo que avanza cada ciclo de patas
        float v = (float) Math.sqrt((getX() - xo) * (getX() - xo) + (getZ() - zo) * (getZ() - zo));
        velocidadO = velocidad;
        velocidad += (Math.min(v, 1.2f) - velocidad) * 0.35f;
        fasePasoO = fasePaso;
        faseGalopeO = faseGalope;
        fasePaso += 0.05f * Math.min(velocidad / 0.16f, 1.7f);
        faseGalope += 0.05f * Mth.clamp(velocidad / 0.45f, 0.7f, 1.5f);
        enAire = onGround() || isInWater() ? 0 : enAire + 1;
        // la crin y la cola de niebla sueltan alguna voluta; y al galope, chispas azules de los cascos
        if (accion() != CorcelAnims.DESHACERSE && random.nextInt(velocidad > 0.3f ? 2 : 6) == 0) {
            float a = yBodyRot * Mth.DEG_TO_RAD;
            double atras = random.nextBoolean() ? -0.95 : 0.75, alto = atras < 0 ? 1.15 : 1.75;
            level().addParticle(ParticleTypes.SOUL, getX() + Mth.sin(a) * atras + (random.nextDouble() - 0.5) * 0.3, getY() + alto,
                    getZ() - Mth.cos(a) * atras + (random.nextDouble() - 0.5) * 0.3, 0, 0.01, 0);
        }
        if (velocidad > 0.4f && onGround() && random.nextInt(3) == 0)
            level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, getX() + (random.nextDouble() - 0.5) * 0.9, getY() + 0.05,
                    getZ() + (random.nextDouble() - 0.5) * 0.9, 0, 0.02, 0);
    }

    /** La accion que se esta viendo en el cliente (0 = ninguna). */
    public int accionVista() {
        int a = accion();
        return a > 0 && a < acciones.length && acciones[a].isStarted() ? a : 0;
    }

    // ------------------------------------------------------------------ guardar (solo por si acaso: no se guarda)

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        if (dueno != null) t.putUUID("dueno", dueno);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        if (t.hasUUID("dueno")) dueno = t.getUUID("dueno");
    }

    @Override
    public void remove(RemovalReason r) {
        super.remove(r);
        if (!level().isClientSide) Montura.alIrse(this);
    }

    @Override
    protected void playHurtSound(DamageSource s) {
        playSound(DarkCraftMod.SFX_CORCEL_DOLOR.get(), getSoundVolume(), 0.95f + random.nextFloat() * 0.15f);
    }

    /** El relincho al llegar (lo pone {@link Montura}). */
    void relinchar() {
        level().playSound(null, getX(), getY(), getZ(), DarkCraftMod.SFX_CORCEL_RELINCHO.get(), SoundSource.NEUTRAL, 1.1f, 1.0f);
    }
}
