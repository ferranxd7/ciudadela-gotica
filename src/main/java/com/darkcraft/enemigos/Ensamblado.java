package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.net.Net;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Set;
import java.util.UUID;

/**
 * Los Ensamblados del Osario de San Ulfo (parte 7as): cuando la Peste Gris lleno el Camposanto de los Pobres, Fray Osmund, el
 * sacristan, desenterro a los muertos y ordeno sus huesos en la cripta. La noche del cielo rojo se levantaron, pero no cada uno con
 * los suyos: la calavera de uno, los brazos de otro. Tres:
 * <ul>
 * <li>el <b>portacirios</b>: los jirones del habito y el candelabro de huesos con las velas encendidas. <b>Candelazo</b> de arriba
 * abajo y <b>barrido</b> (los dos queman). Espera de rodillas rezando ({@code REZA});</li>
 * <li>el <b>sepulturero</b>: la capa de enterrador y la pala. <b>Palada</b> y, de un poco mas lejos, la <b>tierra</b> de tumba que
 * echa a la cara (ciega un momento y frena). Espera cavando ({@code REZA});</li>
 * <li>el <b>coloso</b>: muchos cuerpos en uno (mas grande, {@link #GRANDE}) con la maza de femures. <b>Mazazo</b> (la onda: salta
 * o rueda), <b>barrido</b> muy abierto y <b>pisoton</b> (tumba a los de alrededor). Espera de pie, guardando.</li>
 * </ul>
 * Cualquiera puede esperar hecho un monton de huesos ({@code TUMBADO}: se arma al acercarse alguien). <b>Al caer se deshacen</b>
 * ({@link #DESHACERSE}) y, si nadie pisotea el monton en {@link #DESHECHO} ticks (cualquier golpe de un jugador lo acaba), se
 * vuelven a armar con media vida, una vez ({@link #REHACERSE}). Lo que les acaba del todo: un martillo, el fuego, una explosion.
 */
public class Ensamblado extends Enemigo {
    public static final int PORTACIRIOS = 0, SEPULTURERO = 1, COLOSO = 2;
    /** Lo que se agranda el coloso (el dibujante y la caja). */
    public static final float GRANDE = 1.6f;
    /** Lo que dura el monton antes de rehacerse (ticks), y la vida con la que se rehace. */
    public static final int DESHECHO = 100;
    public static final float VIDA_REHECHO = 0.5f;
    /** La tierra del sepulturero: hasta donde llega, su abertura (grados), y lo que esperan entre una y otra (ticks). */
    public static final double TIERRA_LARGO = 5.5, TIERRA_ARCO = 70;
    public static final int ESPERA_TIERRA = 20 * 5, ESPERA_PISOTON = 20 * 6;
    /** La onda del mazazo y el pisoton: radios. */
    public static final double R_ONDA = 3.0, R_PISOTON = 3.4;
    private static final UUID MOD_VARIANTE = UUID.fromString("7b1e4c2a-3d5f-4e81-9a6b-2c8d0e4f6a10");
    private static final EntityDataAccessor<Boolean> MONTON = SynchedEntityData.defineId(Ensamblado.class, EntityDataSerializers.BOOLEAN);
    static final DustParticleOptions HUESO = new DustParticleOptions(new Vector3f(0.82f, 0.78f, 0.66f), 1.2f),
            POLVO_HUESO = new DustParticleOptions(new Vector3f(0.7f, 0.67f, 0.58f), 1.8f);
    static final BlockParticleOption HUESOS = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()),
            TIERRA = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COARSE_DIRT.defaultBlockState());

    /** Los ticks que le quedan hecho un monton (0: en pie), si ya se rehizo una vez y lo ultimo que le pego. */
    private int deshecho;
    private boolean rehecho, pisoteado;
    private DamageSource ultimo;
    private int ultimaTierra = -100000, ultimoPisoton = -100000;
    /** A quien ya se le dijo que pisotee los huesos (una vez por sesion). */
    private static final Set<UUID> AVISADOS = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /** Para la prueba: los que se deshacen, se rehacen, los que acaban en polvo (y como), las tierras que ciegan, ondas y pisotones. */
    public static int DESHECHOS, REHECHOS, POLVO, POLVO_PISOTEADO, POLVO_MARTILLO, POLVO_FUEGO, CIEGOS, ONDAS, PISOTONES, QUEMADOS;

    public Ensamblado(EntityType<? extends Ensamblado> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(MONTON, false);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 5.5)
                .add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.FOLLOW_RANGE, 24.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.2)
                .add(Attributes.ARMOR, 3.0);
    }

    /** El coloso aguanta mucho mas (y pesa: casi no se le empuja y anda despacio). */
    @Override
    public void variante(int v) {
        super.variante(v);
        refreshDimensions();
        if (level().isClientSide) return;
        var vida = getAttribute(Attributes.MAX_HEALTH);
        var armadura = getAttribute(Attributes.ARMOR);
        var empuje = getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        var paso = getAttribute(Attributes.MOVEMENT_SPEED);
        var golpe = getAttribute(Attributes.ATTACK_DAMAGE);
        if (vida == null || armadura == null || empuje == null || paso == null || golpe == null) return;
        vida.removeModifier(MOD_VARIANTE);
        armadura.removeModifier(MOD_VARIANTE);
        empuje.removeModifier(MOD_VARIANTE);
        paso.removeModifier(MOD_VARIANTE);
        golpe.removeModifier(MOD_VARIANTE);
        if (v == COLOSO) {
            vida.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_coloso", 2.2, AttributeModifier.Operation.MULTIPLY_BASE));
            armadura.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_coloso", 5.0, AttributeModifier.Operation.ADDITION));
            empuje.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_coloso", 0.6, AttributeModifier.Operation.ADDITION));
            paso.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_coloso", -0.2, AttributeModifier.Operation.MULTIPLY_BASE));
            golpe.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_coloso", 0.35, AttributeModifier.Operation.MULTIPLY_BASE));
        } else if (v == SEPULTURERO) {
            vida.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_sepulturero", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        setHealth(getMaxHealth());
    }

    /** El coloso es mas grande (su caja tambien). */
    @Override
    public EntityDimensions getDimensions(Pose p) {
        EntityDimensions d = super.getDimensions(p);
        return variante() == COLOSO ? d.scale(GRANDE) : d;
    }

    @Override
    public String tipo() {
        return switch (variante()) {
            case SEPULTURERO -> "ensamblado_sepulturero";
            case COLOSO -> "ensamblado_coloso";
            default -> "ensamblado_portacirios";
        };
    }

    @Override
    protected int[] ticks() {
        return EnsambladoAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return EnsambladoAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return EnsambladoAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return EnsambladoAnims.MORIR;
    }

    /** Hecho un monton no se levanta por su presa (-1: espera; se rehace a su hora). Tumbado se arma; rezando o cavando, se levanta. */
    @Override
    protected int accionDespertar(int postura) {
        if (deshecho > 0) return -1;
        return postura == TUMBADO ? EnsambladoAnims.REHACERSE : EnsambladoAnims.LEVANTARSE;
    }

    // ------------------------------------------------------------------ que hace

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (deshecho > 0) return 0;
        return switch (variante()) {
            case SEPULTURERO -> {
                if (d > 3.2 && d < TIERRA_LARGO && tickCount - ultimaTierra > ESPERA_TIERRA && random.nextFloat() < 0.5f) yield EnsambladoAnims.TIERRA;
                yield d < 3.0 ? EnsambladoAnims.PALADA : 0;
            }
            case COLOSO -> {
                if (d < 2.6 && tickCount - ultimoPisoton > ESPERA_PISOTON && random.nextFloat() < 0.35f) yield EnsambladoAnims.PISOTON;
                if (d < 3.6) yield random.nextFloat() < 0.5f ? EnsambladoAnims.MAZAZO : EnsambladoAnims.BARRIDO_MAZA;
                yield 0;
            }
            default -> {
                if (d < 3.3) yield random.nextFloat() < 0.5f ? EnsambladoAnims.CANDELAZO : EnsambladoAnims.BARRIDO_CIRIO;
                yield 0;
            }
        };
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case EnsambladoAnims.CANDELAZO -> new Golpe(3.6f, 60f, 1.4f, 0.6f);
            case EnsambladoAnims.BARRIDO_CIRIO -> new Golpe(3.4f, 150f, 1.0f, 0.8f);
            case EnsambladoAnims.PALADA -> new Golpe(3.1f, 80f, 1.3f, 0.7f);
            case EnsambladoAnims.MAZAZO -> new Golpe(3.9f, 60f, 1.6f, 1.2f);
            case EnsambladoAnims.BARRIDO_MAZA -> new Golpe(3.9f, 150f, 1.15f, 1.5f);
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int g = golpes()[accion].length > 0 ? golpes()[accion][0] : -1;
        switch (accion) {
            case EnsambladoAnims.CANDELAZO -> {
                if (t == g - 6) empujar(0.2);
                if (t == g) impacto(2.6, 0.8f, true);
            }
            case EnsambladoAnims.BARRIDO_CIRIO -> {
                if (t == g - 3) empujar(0.25);
                if (t == g && level() instanceof ServerLevel sl) {
                    // las velas dejan un arco de llamas en el aire
                    for (int j = 0; j <= 8; j++) {
                        double a = Math.toRadians(getYRot() + 90 - 75 + 150 * j / 8.0);
                        sl.sendParticles(ParticleTypes.FLAME, getX() + Math.cos(a) * 2.6, getY() + 1.5, getZ() + Math.sin(a) * 2.6, 2, 0.1, 0.1, 0.1, 0.01);
                    }
                    playSound(DarkCraftMod.SFX_PORTACIRIOS_BARRIDO.get(), 1.0f, 0.95f + random.nextFloat() * 0.1f);
                }
            }
            case EnsambladoAnims.PALADA -> {
                if (t == g - 4) empujar(0.25);
                if (t == g) impacto(2.3, 0.7f, false);
            }
            case EnsambladoAnims.TIERRA -> {
                if (t == 8 && level() instanceof ServerLevel sl) {
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    sl.sendParticles(TIERRA, getX() + d.x * 1.4, getY() + 0.2, getZ() + d.z * 1.4, 12, 0.3, 0.05, 0.3, 0.1);
                    playSound(DarkCraftMod.SFX_ENTERRADO_PICA.get(), 0.7f, 0.6f);
                }
                if (t == g) echarTierra();
            }
            case EnsambladoAnims.MAZAZO -> {
                if (t == 8) playSound(DarkCraftMod.SFX_ESFUERZO.get(), 1.0f, 0.6f);
                if (t == g) {
                    impacto(3.0, 1.4f, false);
                    onda(3.0, R_ONDA, 0.6f);
                }
            }
            case EnsambladoAnims.BARRIDO_MAZA -> {
                if (t == g - 4) empujar(0.3);
            }
            case EnsambladoAnims.PISOTON -> {
                if (t == g) {
                    ultimoPisoton = tickCount;
                    PISOTONES++;
                    impacto(0.0, 1.6f, false);
                    onda(0.0, R_PISOTON, 0.5f);
                }
            }
            case EnsambladoAnims.REHACERSE -> {
                if (t == 2) rehacerse();
                if (t == ticks()[accion] - 1 && postura() != DE_PIE) postura(DE_PIE);
            }
            case EnsambladoAnims.LEVANTARSE -> {
                if (t == 2) {
                    playSound(DarkCraftMod.SFX_ENSAMBLADO_ALERTA.get(), 1.0f, 0.9f + random.nextFloat() * 0.2f);
                    if (level() instanceof ServerLevel sl) sl.sendParticles(POLVO_HUESO, getX(), getY() + 0.8, getZ(), 10, 0.4, 0.4, 0.4, 0.01);
                }
            }
            default -> {
            }
        }
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        switch (accion) {
            case EnsambladoAnims.CANDELAZO, EnsambladoAnims.BARRIDO_CIRIO -> {
                if (!le.fireImmune()) {
                    le.setSecondsOnFire(Math.max(3, le.getRemainingFireTicks() / 20));
                    QUEMADOS++;
                }
            }
            case EnsambladoAnims.MAZAZO -> le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1), this);
            default -> {
            }
        }
    }

    /** Donde da el golpe contra el suelo: huesos, polvo y el ruido (con el candelabro, chispas de las velas). */
    private void impacto(double lejos, float fuerte, boolean velas) {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * lejos, z = getZ() + d.z * lejos;
        BlockState suelo = level().getBlockState(BlockPos.containing(x, getY() - 0.5, z));
        if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), x, getY() + 0.1, z, (int) (18 * fuerte), 0.4, 0.05, 0.4, 0.15);
        sl.sendParticles(POLVO_HUESO, x, getY() + 0.4, z, (int) (8 * fuerte), 0.5, 0.2, 0.5, 0.02);
        if (velas) {
            sl.sendParticles(ParticleTypes.FLAME, x, getY() + 0.3, z, 12, 0.4, 0.1, 0.4, 0.03);
            sl.sendParticles(ParticleTypes.LAVA, x, getY() + 0.2, z, 3, 0.3, 0.05, 0.3, 0.0);
        }
        if (fuerte > 1f) {
            sl.sendParticles(HUESOS, x, getY() + 0.3, z, 16, 0.5, 0.1, 0.5, 0.15);
            playSound(DarkCraftMod.SFX_MONTON_GOLPE.get(), 1.4f, 0.85f + random.nextFloat() * 0.2f);
        } else playSound(DarkCraftMod.SFX_GOLPE_PESADO.get(), 0.8f, 0.9f + random.nextFloat() * 0.2f);
    }

    /** La onda: a quien este de pie cerca del golpe (los jugadores y su presa; saltando o rodando se libra). */
    private void onda(double lejos, double r, float k) {
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * lejos, z = getZ() + d.z * lejos;
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * k;
        for (LivingEntity p : level().getEntitiesOfClass(LivingEntity.class, new AABB(x - r, getY() - 1, z - r, x + r, getY() + 2, z + r),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            if (!p.onGround() || p.distanceToSqr(x, p.getY(), z) > r * r) continue;
            if (p.hurt(damageSources().mobAttack(this), dano)) {
                ONDAS++;
                p.knockback(0.6, x - p.getX(), z - p.getZ());
                p.setDeltaMovement(p.getDeltaMovement().add(0, 0.36, 0));
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1), this);
            }
        }
    }

    /** La tierra de tumba a la cara: a quien pille en el abanico de delante, ciego un momento, frenado y un poco de dano. */
    private void echarTierra() {
        if (!(level() instanceof ServerLevel sl)) return;
        ultimaTierra = tickCount;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        for (int k = 0; k < 14; k++) {
            double a = Math.toRadians(getYRot() + (random.nextDouble() - 0.5) * TIERRA_ARCO), r = 1.2 + random.nextDouble() * (TIERRA_LARGO - 1.2);
            Vec3 v = Vec3.directionFromRotation(0, (float) Math.toDegrees(a));
            sl.sendParticles(TIERRA, getX() + v.x * r, getY() + 1.1 + random.nextDouble() * 0.6, getZ() + v.z * r, 3, 0.15, 0.15, 0.15, 0.05);
        }
        sl.sendParticles(ParticleTypes.ASH, getX() + d.x * 2.5, getY() + 1.3, getZ() + d.z * 2.5, 30, 1.0, 0.4, 1.0, 0.02);
        playSound(DarkCraftMod.SFX_GOLPE_LIGERO.get(), 0.9f, 0.6f);
        double cos = Math.cos(Math.toRadians(TIERRA_ARCO / 2));
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(TIERRA_LARGO, 2, TIERRA_LARGO),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            double dx = le.getX() - getX(), dz = le.getZ() - getZ(), l = Math.sqrt(dx * dx + dz * dz);
            if (l > TIERRA_LARGO || (l > 0.5 && (dx * d.x + dz * d.z) / l < cos) || !hasLineOfSight(le)) continue;
            if (le.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.35f)) {
                le.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0), this);
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 0), this);
                CIEGOS++;
            }
        }
    }

    // ------------------------------------------------------------------ deshacerse y rehacerse

    /** Si esta hecho un monton (esperando a rehacerse). */
    public boolean deshecho() {
        return entityData.get(MONTON);
    }

    /** Lo que le queda hecho un monton (servidor; pruebas). */
    public int ticksDeshecho() {
        return deshecho;
    }

    public boolean rehecho() {
        return rehecho;
    }

    /** Lo que acaba con ellos del todo (el monton no se rehace): un martillo, el fuego, una explosion, o lo que no se para. */
    static boolean definitivo(DamageSource s, LivingEntity quien) {
        if (s == null) return false;
        if (s.is(DamageTypeTags.IS_FIRE) || s.is(DamageTypeTags.IS_EXPLOSION) || s.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || s.is(DamageTypes.GENERIC_KILL) || s.is(DamageTypes.FELL_OUT_OF_WORLD))
            return true;
        if (quien != null && quien.isOnFire()) return true;
        return s.getEntity() instanceof Player p && s.getDirectEntity() == p && p.getMainHandItem().getItem() instanceof com.darkcraft.armas.Martillo;
    }

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        if (level().isClientSide) return false;
        // hecho un monton: cualquier golpe de alguien (o lo que acaba del todo) lo deshace en polvo; lo demas no le hace nada
        if (deshecho > 0 && isAlive()) {
            boolean pisa = s.getEntity() instanceof LivingEntity && !(s.getEntity() instanceof Enemigo);
            if (!pisa && !definitivo(s, this)) return false;
            if (pisa && !definitivo(s, this)) POLVO_PISOTEADO++;
            pisoteado = true;
            ultimo = s;
            invulnerableTime = 0;
            return super.hurt(s, Math.max(cuanto, getHealth() + 1000f));
        }
        ultimo = s;
        return super.hurt(s, cuanto);
    }

    /** Lo que le dejaria muerto, si aun puede rehacerse y no es un golpe que acaba del todo, le deja hecho un monton. */
    @Override
    public void setHealth(float v) {
        if (!level().isClientSide && isAlive() && v <= 0 && !rehecho && !pisoteado && deshecho == 0 && tickCount > 1 && getHealth() > 0
                && !definitivo(ultimo, this)) {
            super.setHealth(1f);
            deshacerse();
            return;
        }
        super.setHealth(v);
    }

    /** Se le sueltan los huesos y cae hecho un monton (si nadie lo pisotea, a los {@link #DESHECHO} ticks se rehace). */
    private void deshacerse() {
        deshecho = DESHECHO;
        entityData.set(MONTON, true);
        DESHECHOS++;
        postura(TUMBADO);
        empezar(EnsambladoAnims.DESHACERSE);
        getNavigation().stop();
        setDeltaMovement(0, getDeltaMovement().y, 0);
        clearFire();
        playSound(DarkCraftMod.SFX_ENSAMBLADO_DESHACE.get(), 1.1f, 0.9f + random.nextFloat() * 0.2f);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(HUESOS, getX(), getY() + 0.6, getZ(), 24, 0.4, 0.4, 0.4, 0.12);
            sl.sendParticles(POLVO_HUESO, getX(), getY() + 0.5, getZ(), 14, 0.5, 0.3, 0.5, 0.02);
            // a quien le pego (y a los de cerca), la primera vez: que pisotee los huesos
            for (ServerPlayer p : sl.players())
                if (p.distanceToSqr(this) < 16 * 16 && !p.isSpectator() && AVISADOS.add(p.getUUID()))
                    Net.to(p, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.osario.pisotea", 0));
        }
    }

    /** El monton se arma otra vez: media vida y ya no se rehace mas. */
    private void rehacerse() {
        if (!(level() instanceof ServerLevel sl)) return;
        boolean deMonton = rehecho;
        sl.sendParticles(HUESOS, getX(), getY() + 0.4, getZ(), 20, 0.5, 0.2, 0.5, 0.1);
        sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.0, getZ(), 8, 0.3, 0.5, 0.3, 0.02);
        playSound(DarkCraftMod.SFX_ENSAMBLADO_REHACE.get(), 1.1f, 0.95f + random.nextFloat() * 0.1f);
        if (deMonton) REHECHOS++;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            cliente();
            return;
        }
        if (!isAlive()) return;
        if (deshecho > 0) {
            setDeltaMovement(0, getDeltaMovement().y, 0);
            if (--deshecho == 0) {
                // se vuelve a armar: con media vida, una vez
                entityData.set(MONTON, false);
                rehecho = true;
                super.setHealth(Math.max(1f, getMaxHealth() * VIDA_REHECHO));
                empezar(EnsambladoAnims.REHACERSE);
            } else if (deshecho % 20 == 0 && level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 0.3, getZ(), 2, 0.3, 0.05, 0.3, 0.01);
                if (deshecho <= 40) playSound(DarkCraftMod.SFX_ENSAMBLADO_TRAQUETEO.get(), 0.7f, 1.3f);
            }
            // poco antes de rehacerse, los huesos se arrastran unos hacia otros
            if (deshecho == 34) playSound(DarkCraftMod.SFX_MONTON_ARRASTRE.get(), 0.9f, variante() == COLOSO ? 0.8f : 1.05f);
        }
    }

    /** En el cliente: el portacirios, las llamitas de sus velas; el monton que se va a rehacer tiembla con lucecitas de alma. */
    private void cliente() {
        if (!isAlive()) return;
        Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
        if (variante() == PORTACIRIOS && postura() != TUMBADO && accion() == 0 && tickCount % 4 == 0) {
            // las tres velas en lo alto del candelabro (de pie, a su derecha y alto; rezando, delante)
            double up = postura() == REZA ? 2.2 : 2.9, fw = postura() == REZA ? 0.55 : 0.25, lado = postura() == REZA ? 0.05 : 0.35;
            double x = getX() + d.x * fw - d.z * lado, z = getZ() + d.z * fw + d.x * lado;
            level().addParticle(ParticleTypes.SMALL_FLAME, x + (random.nextDouble() - 0.5) * 0.5, getY() + up, z + (random.nextDouble() - 0.5) * 0.5, 0, 0.01, 0);
        }
        if (deshecho() && tickCount % 3 == 0)
            level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, getX() + (random.nextDouble() - 0.5) * 0.9, getY() + 0.15, getZ() + (random.nextDouble() - 0.5) * 0.9,
                    0, 0.01, 0);
        if (variante() == SEPULTURERO && postura() == REZA && accion() == 0 && tickCount % 40 == 11) {
            double x = getX() + d.x * 1.2, z = getZ() + d.z * 1.2;
            for (int k = 0; k < 3; k++) level().addParticle(TIERRA, x, getY() + 0.2, z, (random.nextDouble() - 0.5) * 0.1, 0.12, (random.nextDouble() - 0.5) * 0.1);
        }
    }

    // ------------------------------------------------------------------ morir: polvo

    @Override
    public void die(DamageSource s) {
        boolean ya = !isAlive() && deathTime > 0;
        super.die(s);
        if (level().isClientSide || ya) return;
        entityData.set(MONTON, false);
        deshecho = 0;
        POLVO++;
        if (s.getEntity() instanceof Player p && s.getDirectEntity() == p && p.getMainHandItem().getItem() instanceof com.darkcraft.armas.Martillo) POLVO_MARTILLO++;
        else if (s.is(DamageTypeTags.IS_FIRE) || isOnFire()) POLVO_FUEGO++;
        ServerPlayer quien = s.getEntity() instanceof ServerPlayer p ? p : getKillCredit() instanceof ServerPlayer p2 ? p2 : null;
        if (quien != null && !esEfimero()) {
            com.darkcraft.espejo.Espejo.bestia(quien, tipo());
            com.darkcraft.story.Logros.dar(quien, "ensamblado");
        }
    }

    /** Al acabar de caer se deshace en polvo de hueso (ya no se rehace). */
    @Override
    protected void tickDeath() {
        int m = EnsambladoAnims.MORIR;
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        if (deathTime == EnsambladoAnims.TICKS[m] - 4) polvo();
        if (deathTime >= EnsambladoAnims.TICKS[m]) remove(RemovalReason.KILLED);
    }

    private void polvo() {
        if (!(level() instanceof ServerLevel sl)) return;
        float k = variante() == COLOSO ? 1.6f : 1f;
        sl.sendParticles(HUESOS, getX(), getY() + 0.3, getZ(), (int) (30 * k), 0.5 * k, 0.2, 0.5 * k, 0.12);
        sl.sendParticles(POLVO_HUESO, getX(), getY() + 0.5, getZ(), (int) (36 * k), 0.6 * k, 0.4, 0.6 * k, 0.02);
        sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 0.8, getZ(), (int) (6 * k), 0.3, 0.4, 0.3, 0.03);
        sl.playSound(null, getX(), getY(), getZ(), DarkCraftMod.SFX_ENSAMBLADO_POLVO.get(), SoundSource.HOSTILE, 1.1f, (variante() == COLOSO ? 0.75f : 0.95f)
                + random.nextFloat() * 0.15f);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (jugador && random.nextFloat() < 0.07f + 0.01f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
        if (jugador && variante() == COLOSO && random.nextFloat() < 0.1f) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_2.get());
        if (jugador && random.nextFloat() < 0.3f) spawnAtLocation(net.minecraft.world.item.Items.BONE);
    }

    // ------------------------------------------------------------------ sonidos y ritmo

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case EnsambladoAnims.BARRIDO_CIRIO, EnsambladoAnims.PALADA -> DarkCraftMod.SFX_GOLPE_LIGERO.get();
            case EnsambladoAnims.CANDELAZO, EnsambladoAnims.MAZAZO, EnsambladoAnims.BARRIDO_MAZA -> DarkCraftMod.SFX_GOLPE_PESADO.get();
            default -> null;
        };
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_ENSAMBLADO_ALERTA.get();
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (variante()) {
            case COLOSO -> 22 + random.nextInt(16);
            default -> 14 + random.nextInt(14);
        };
    }

    @Override
    protected double distanciaDeseada() {
        return variante() == COLOSO ? 2.8 : variante() == SEPULTURERO ? 2.4 : 2.2;
    }

    @Override
    protected double prisa() {
        return variante() == COLOSO ? 0.95 : 1.0;
    }

    @Override
    protected boolean anda() {
        return deshecho == 0 && super.anda();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return postura() == TUMBADO ? null : DarkCraftMod.SFX_ENSAMBLADO_TRAQUETEO.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_ENSAMBLADO_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_ENSAMBLADO_DOLOR.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 220;
    }

    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        playSound(variante() == COLOSO ? net.minecraft.sounds.SoundEvents.SKELETON_STEP : net.minecraft.sounds.SoundEvents.SKELETON_STEP,
                variante() == COLOSO ? 0.5f : 0.3f, variante() == COLOSO ? 0.6f : 1.0f);
    }

    // ------------------------------------------------------------------ para las pruebas

    /** Para las pruebas: le deja hecho un monton ya (como si le hubieran tumbado). */
    public void tumbarYa() {
        if (level().isClientSide || !isAlive() || deshecho > 0) return;
        ultimo = null;
        setHealth(0f);
    }

    /** Para las pruebas: lo que le falta para rehacerse, a 1 (se rehace en el siguiente tick). */
    public void rehacerYa() {
        if (deshecho > 1) deshecho = 1;
    }

    public String estado() {
        return String.format(java.util.Locale.ROOT, "%s vida %.0f/%.0f, postura %d, accion %d, %s%s", tipo(), getHealth(), getMaxHealth(), postura(), accion(),
                deshecho > 0 ? "hecho un monton (" + deshecho + ")" : "en pie", rehecho ? ", ya rehecho" : "");
    }
}
