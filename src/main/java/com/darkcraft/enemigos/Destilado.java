package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
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

import java.util.UUID;

/**
 * Los Destilados del Lazareto de Maese Galen (parte 7au): los enfermos de la Peste Gris que Galen trato con su elixir, sus mancebos
 * (que lo bebian para no enfermar) y los que uso de cuba viva para destilarlo. Tres:
 * <ul>
 * <li>el <b>mancebo</b>: la mascara de pico y el abrigo encerado. De lejos <b>lanza frascos</b> en arco ({@link Frasco}: de fuego o de
 * acido; y si alguno de los suyos esta herido, el <b>elixir verde</b> que le cura) y de cerca pega con la mano de mortero (de arriba
 * abajo y de lado). Espera moliendo en su mesa ({@code REZA});</li>
 * <li>el <b>enfermo</b>: la piel gris y las bubas de cristal verde. <b>Zarpazo</b> que envenena, <b>se abalanza</b> desde unos
 * bloques y <b>escupe</b> lo que lleva dentro (envenena). Rapido. Espera sentado en el suelo, encogido ({@code REZA}), o tendido
 * como un enfermo mas ({@code TUMBADO});</li>
 * <li>la <b>cuba</b> (mas grande, {@link #GRANDE}): la cuba de cristal llena de destilado a la espalda. <b>Punetazo</b>,
 * <b>embestida</b> y <b>derrame</b> (se inclina y la cuba le vierte el acido por delante: charcos que corroen). <b>Al morir le revienta
 * la cuba</b>: el charco verde que deja cura a los destilados que lo pisen (hay que sacar a los demas de alli). Espera jadeando.</li>
 * </ul>
 */
public class Destilado extends Enemigo {
    public static final int MANCEBO = 0, ENFERMO = 1, CUBA = 2;
    /** Lo que se agranda la cuba (el dibujante y la caja). */
    public static final float GRANDE = 1.5f;
    /** Lo que esperan entre una y otra (ticks). */
    public static final int ESPERA_FRASCO = 20 * 3, ESPERA_CURA = 20 * 6, ESPERA_ESCUPIR = 20 * 5, ESPERA_SALTO = 20 * 4,
            ESPERA_EMBESTIDA = 20 * 6, ESPERA_DERRAME = 20 * 8;
    /** Lo herido que tiene que estar uno de los suyos para que el mancebo le lance el elixir, y lo lejos. */
    public static final float HERIDO = 0.6f;
    public static final double CURA_LEJOS = 12;
    /** El derrame: hasta donde llega por delante y su abertura (grados). El charco de la cuba al reventar: radio y ticks. */
    public static final double DERRAME_LARGO = 5.0, DERRAME_ARCO = 70;
    public static final float R_CUBA = 2.8f;
    public static final int T_CUBA = 20 * 9;
    private static final UUID MOD_VARIANTE = UUID.fromString("5c3e8a1d-7f42-4b96-a0d8-1e6b9c2f4a77");
    static final DustParticleOptions VERDE = new DustParticleOptions(new Vector3f(0.36f, 0.95f, 0.45f), 1.4f),
            ACIDO = new DustParticleOptions(new Vector3f(0.62f, 0.85f, 0.18f), 1.5f);
    static final BlockParticleOption CRISTAL = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GREEN_STAINED_GLASS.defaultBlockState());

    private int ultimoFrasco = -100000, ultimaCura = -100000, ultimoEscupitajo = -100000, ultimoSalto = -100000, ultimaEmbestida = -100000,
            ultimoDerrame = -100000;
    /** Lo que lanza el frasco que tiene en la mano y a quien (el elixir, a uno de los suyos). */
    private int frascoTipo = Frasco.FUEGO;
    private LivingEntity frascoA;

    /** Para la prueba: frascos (de cada tipo), elixires, escupitajos, saltos, embestidas, derrames, cubas rotas y envenenados. */
    public static int FRASCOS, ELIXIRES, ESCUPITAJOS, SALTOS, EMBESTIDAS, DERRAMES, DERRAMES_DAN, CUBAS, ENVENENADOS, ALZADOS;

    public Destilado(EntityType<? extends Destilado> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 28.0).add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23).add(Attributes.FOLLOW_RANGE, 24.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                .add(Attributes.ARMOR, 2.0);
    }

    /** El mancebo lleva el abrigo (algo de armadura); el enfermo aguanta menos y es mas rapido; la cuba aguanta mucho y pesa. */
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
        switch (v) {
            case CUBA -> {
                vida.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_cuba", 2.4, AttributeModifier.Operation.MULTIPLY_BASE));
                armadura.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_cuba", 4.0, AttributeModifier.Operation.ADDITION));
                empuje.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_cuba", 0.6, AttributeModifier.Operation.ADDITION));
                paso.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_cuba", -0.15, AttributeModifier.Operation.MULTIPLY_BASE));
                golpe.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_cuba", 0.4, AttributeModifier.Operation.MULTIPLY_BASE));
            }
            case ENFERMO -> {
                vida.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_enfermo", -0.15, AttributeModifier.Operation.MULTIPLY_BASE));
                paso.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_enfermo", 0.12, AttributeModifier.Operation.MULTIPLY_BASE));
            }
            default -> armadura.addPermanentModifier(new AttributeModifier(MOD_VARIANTE, "darkcraft_mancebo", 2.0, AttributeModifier.Operation.ADDITION));
        }
        setHealth(getMaxHealth());
    }

    /** La cuba es mas grande (su caja tambien). */
    @Override
    public EntityDimensions getDimensions(Pose p) {
        EntityDimensions d = super.getDimensions(p);
        return variante() == CUBA ? d.scale(GRANDE) : d;
    }

    @Override
    public String tipo() {
        return switch (variante()) {
            case ENFERMO -> "destilado_enfermo";
            case CUBA -> "destilado_cuba";
            default -> "destilado_mancebo";
        };
    }

    @Override
    protected int[] ticks() {
        return DestiladoAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return DestiladoAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return DestiladoAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return DestiladoAnims.MORIR;
    }

    /** Tendido se alza (se sienta y se pone en pie); sentado en el suelo se levanta; moliendo o jadeando, se vuelve. */
    @Override
    protected int accionDespertar(int postura) {
        if (postura == TUMBADO) return DestiladoAnims.ALZARSE;
        return variante() == ENFERMO ? DestiladoAnims.LEVANTARSE : DestiladoAnims.VOLVERSE;
    }

    // ------------------------------------------------------------------ que hace

    @Override
    protected int elegir(LivingEntity presa, double d) {
        return switch (variante()) {
            case ENFERMO -> {
                if (d > 3.5 && d < 9.0 && tickCount - ultimoEscupitajo > ESPERA_ESCUPIR && random.nextFloat() < 0.35f) yield DestiladoAnims.ESCUPITAJO;
                if (d > 2.8 && d < 5.5 && tickCount - ultimoSalto > ESPERA_SALTO && random.nextFloat() < 0.4f) yield DestiladoAnims.ABALANZARSE;
                yield d < 2.7 ? DestiladoAnims.ZARPAZO : 0;
            }
            case CUBA -> {
                if (d > 4.5 && d < 10.0 && tickCount - ultimaEmbestida > ESPERA_EMBESTIDA && random.nextFloat() < 0.4f) yield DestiladoAnims.EMBESTIDA;
                if (d < 4.2 && tickCount - ultimoDerrame > ESPERA_DERRAME && random.nextFloat() < 0.3f) yield DestiladoAnims.DERRAME;
                yield d < 3.3 ? DestiladoAnims.PUNETAZO : 0;
            }
            default -> {
                // primero los suyos: si alguno esta muy herido y a la vista, el elixir verde
                if (tickCount - ultimaCura > ESPERA_CURA) {
                    LivingEntity herido = herido();
                    if (herido != null) {
                        frascoTipo = Frasco.CURA;
                        frascoA = herido;
                        yield DestiladoAnims.FRASCO;
                    }
                }
                if (d < 3.2) yield random.nextFloat() < 0.5f ? DestiladoAnims.MORTERO : DestiladoAnims.MORTERO_BARRIDO;
                if (d > 3.6 && d < 16.0 && tickCount - ultimoFrasco > ESPERA_FRASCO && random.nextFloat() < 0.6f) {
                    frascoTipo = random.nextFloat() < 0.5f ? Frasco.FUEGO : Frasco.ACIDO;
                    frascoA = presa;
                    yield DestiladoAnims.FRASCO;
                }
                yield 0;
            }
        };
    }

    /** El mas herido de los suyos a la vista (menos de {@link #HERIDO} de su vida), si hay alguno. */
    private LivingEntity herido() {
        Destilado peor = null;
        float menos = HERIDO;
        for (Destilado o : level().getEntitiesOfClass(Destilado.class, getBoundingBox().inflate(CURA_LEJOS, 4, CURA_LEJOS),
                e -> e != this && e.isAlive() && !e.muriendo())) {
            float k = o.getHealth() / o.getMaxHealth();
            if (k < menos && hasLineOfSight(o)) {
                menos = k;
                peor = o;
            }
        }
        return peor;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case DestiladoAnims.MORTERO -> new Golpe(3.3f, 60f, 1.3f, 0.6f);
            case DestiladoAnims.MORTERO_BARRIDO -> new Golpe(3.1f, 140f, 0.9f, 0.8f);
            case DestiladoAnims.ZARPAZO -> new Golpe(2.8f, 90f, 1.0f, 0.3f);
            case DestiladoAnims.ABALANZARSE -> new Golpe(2.6f, 100f, 1.4f, 0.6f);
            case DestiladoAnims.PUNETAZO -> new Golpe(3.4f, 70f, 1.5f, 1.4f);
            case DestiladoAnims.EMBESTIDA -> new Golpe(2.6f, 100f, 1.3f, 2.0f);
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int g = golpes()[accion].length > 0 ? golpes()[accion][0] : -1;
        switch (accion) {
            case DestiladoAnims.FRASCO -> {
                // si lo que iba a curar ya no esta (o ya no hace falta), lanza a su presa uno de fuego
                if (frascoTipo == Frasco.CURA && (frascoA == null || !frascoA.isAlive()) && t < g) {
                    frascoTipo = Frasco.FUEGO;
                    frascoA = getTarget();
                }
                if (frascoTipo == Frasco.CURA && frascoA != null && t < g - 3) girarHacia(frascoA, 30f);
                if (t == g) lanzarFrasco();
            }
            case DestiladoAnims.MORTERO -> {
                if (t == g - 5) empujar(0.2);
                if (t == g) impacto(2.4, 0.8f);
            }
            case DestiladoAnims.MORTERO_BARRIDO -> {
                if (t == g - 3) empujar(0.25);
            }
            case DestiladoAnims.ZARPAZO -> {
                if (t == g - 4) empujar(0.3);
            }
            case DestiladoAnims.ABALANZARSE -> {
                if (t == 2) {
                    ultimoSalto = tickCount;
                    playSound(DarkCraftMod.SFX_DESTILADO_ALERTA.get(), 0.9f, 1.25f);
                }
                if (t == 14) {
                    // el salto: hacia delante y un poco arriba
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    LivingEntity presa = getTarget();
                    double lejos = presa == null ? 4.0 : Math.min(5.0, Math.max(2.0, Math.sqrt(distanceToSqr(presa)) - 1.0));
                    setDeltaMovement(d.x * lejos * 0.2, 0.36, d.z * lejos * 0.2);
                    hasImpulse = true;
                    SALTOS++;
                }
                if (t > 14 && t < g) {
                    // en el aire no le frena la accion: se le devuelve lo que le quita aiStep
                    Vec3 v = getDeltaMovement();
                    setDeltaMovement(v.x / 0.6, v.y, v.z / 0.6);
                }
                if (t == g) impacto(1.2, 0.6f);
            }
            case DestiladoAnims.ESCUPITAJO -> {
                if (t == 6) playSound(DarkCraftMod.SFX_DESTILADO_GEMIDO.get(), 0.8f, 1.3f);
                if (t == g) escupir();
            }
            case DestiladoAnims.PUNETAZO -> {
                if (t == g - 4) empujar(0.3);
                if (t == g) playSound(DarkCraftMod.SFX_CUBA_GOLPE.get(), 1.0f, 0.95f + random.nextFloat() * 0.1f);
            }
            case DestiladoAnims.EMBESTIDA -> {
                if (t == 4) {
                    ultimaEmbestida = tickCount;
                    EMBESTIDAS++;
                    playSound(DarkCraftMod.SFX_DESTILADO_ALERTA.get(), 1.2f, 0.7f);
                }
                if (t >= 5 && t <= g + 1) {
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    setDeltaMovement(d.x * 0.6, getDeltaMovement().y, d.z * 0.6);
                    hasImpulse = true;
                    if (level() instanceof ServerLevel sl && t % 2 == 0) sl.sendParticles(VERDE, getX(), getY() + 2.2, getZ(), 2, 0.4, 0.3, 0.4, 0.01);
                }
                if (t == g) playSound(DarkCraftMod.SFX_CUBA_GOLPE.get(), 1.2f, 0.8f);
            }
            case DestiladoAnims.DERRAME -> {
                if (t == 10) playSound(DarkCraftMod.SFX_CUBA_DERRAME.get(), 1.3f, 0.95f + random.nextFloat() * 0.1f);
                if (t > 10 && t < g && level() instanceof ServerLevel sl) {
                    // el destilado que le cae de la cuba por encima de los hombros
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    sl.sendParticles(ACIDO, getX() + d.x * 1.2, getY() + 2.4, getZ() + d.z * 1.2, 6, 0.4, 0.3, 0.4, 0.0);
                    sl.sendParticles(ParticleTypes.FALLING_WATER, getX() + d.x * 1.4, getY() + 2.2, getZ() + d.z * 1.4, 4, 0.4, 0.2, 0.4, 0.0);
                }
                if (t == g) derramar();
            }
            case DestiladoAnims.LEVANTARSE, DestiladoAnims.VOLVERSE, DestiladoAnims.ALZARSE -> {
                if (t == 2) despertar();
            }
            default -> {
            }
        }
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        switch (accion) {
            case DestiladoAnims.ZARPAZO -> {
                le.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 0), this);
                ENVENENADOS++;
            }
            case DestiladoAnims.ABALANZARSE -> {
                le.addEffect(new MobEffectInstance(MobEffects.POISON, 70, 0), this);
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1), this);
                ENVENENADOS++;
            }
            case DestiladoAnims.PUNETAZO, DestiladoAnims.EMBESTIDA -> le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1), this);
            default -> {
            }
        }
    }

    /** Lanza el frasco que tiene en la mano en arco: a su presa (al suelo, a sus pies) o, el elixir, a los pies de uno de los suyos. */
    private void lanzarFrasco() {
        if (level().isClientSide) return;
        LivingEntity t = frascoTipo == Frasco.CURA ? frascoA : getTarget();
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double ox = getX() + d.x * 0.5, oz = getZ() + d.z * 0.5, oy = getY() + getBbHeight() * 0.92;
        double tx, ty, tz;
        if (t == null) {
            tx = ox + d.x * 8;
            tz = oz + d.z * 8;
            ty = getY();
        } else {
            // a donde va a estar (un poco: el frasco tarda en llegar)
            Vec3 v = frascoTipo == Frasco.CURA ? Vec3.ZERO : t.getDeltaMovement();
            tx = t.getX() + v.x * 6;
            tz = t.getZ() + v.z * 6;
            ty = t.getY() + 0.1;
        }
        Frasco f = new Frasco(level(), this, frascoTipo);
        f.setPos(ox, oy, oz);
        f.setDeltaMovement(Cantaro.tiro(ox, oy, oz, tx, ty, tz));
        level().addFreshEntity(f);
        FRASCOS++;
        if (frascoTipo == Frasco.CURA) {
            ELIXIRES++;
            ultimaCura = tickCount;
        } else ultimoFrasco = tickCount;
        playSound(DarkCraftMod.SFX_FRASCO_LANZAR.get(), 0.9f, 0.9f + random.nextFloat() * 0.2f);
        frascoTipo = Frasco.FUEGO;
        frascoA = null;
    }

    /** Escupe lo que lleva dentro (mas derecho que un frasco) a la cara de su presa. */
    private void escupir() {
        if (level().isClientSide) return;
        ultimoEscupitajo = tickCount;
        LivingEntity t = getTarget();
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double ox = getX() + d.x * 0.5, oz = getZ() + d.z * 0.5, oy = getEyeY() - 0.1;
        double tx = t == null ? ox + d.x * 6 : t.getX(), tz = t == null ? oz + d.z * 6 : t.getZ(), ty = t == null ? getEyeY() : t.getY() + t.getBbHeight() * 0.6;
        Frasco f = new Frasco(level(), this, Frasco.ESCUPITAJO);
        f.setPos(ox, oy, oz);
        f.setDeltaMovement(Frasco.directo(ox, oy, oz, tx, ty, tz));
        level().addFreshEntity(f);
        ESCUPITAJOS++;
        if (level() instanceof ServerLevel sl) sl.sendParticles(Frasco.BABA, ox, oy, oz, 6, 0.1, 0.1, 0.1, 0.02);
    }

    /**
     * El derrame de la cuba: el destilado cae por delante (el abanico de {@link #DERRAME_ARCO} grados hasta {@link #DERRAME_LARGO}) y
     * quema a quien pille; en el suelo quedan tres charcos de acido en linea.
     */
    private void derramar() {
        if (!(level() instanceof ServerLevel sl)) return;
        ultimoDerrame = tickCount;
        DERRAMES++;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        for (int k = 0; k < 3; k++) {
            double lejos = 1.4 + k * 1.3;
            Vec3 p = new Vec3(getX() + d.x * lejos, getY() + 0.6, getZ() + d.z * lejos);
            sl.sendParticles(ACIDO, p.x, p.y, p.z, 16, 0.5, 0.2, 0.5, 0.02);
            sl.sendParticles(ParticleTypes.SPLASH, p.x, p.y, p.z, 12, 0.5, 0.1, 0.5, 0.1);
            double y = Frasco.suelo(sl, p);
            if (!Double.isNaN(y)) sl.addFreshEntity(new Frasco.Charco(sl, p.x, y, p.z, this, Frasco.ACIDO, 1.5f, 90));
        }
        double cos = Math.cos(Math.toRadians(DERRAME_ARCO / 2));
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.7f;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(DERRAME_LARGO, 2, DERRAME_LARGO),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            double dx = le.getX() - getX(), dz = le.getZ() - getZ(), l = Math.sqrt(dx * dx + dz * dz);
            if (l > DERRAME_LARGO || (l > 0.6 && (dx * d.x + dz * d.z) / l < cos)) continue;
            if (le.hurt(damageSources().magic(), dano)) {
                DERRAMES_DAN++;
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), this);
            }
        }
    }

    /** Donde da el golpe contra el suelo: el suelo que salta y algo de destilado. */
    private void impacto(double lejos, float fuerte) {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * lejos, z = getZ() + d.z * lejos;
        BlockState suelo = level().getBlockState(BlockPos.containing(x, getY() - 0.5, z));
        if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), x, getY() + 0.1, z, (int) (14 * fuerte), 0.4, 0.05, 0.4, 0.15);
        sl.sendParticles(ACIDO, x, getY() + 0.3, z, (int) (6 * fuerte), 0.4, 0.2, 0.4, 0.02);
        playSound(DarkCraftMod.SFX_GOLPE_PESADO.get(), 0.7f, 1.0f + random.nextFloat() * 0.2f);
    }

    /** Se levanta (o se alza de su camastro): el gemido y lo que se le sale de las bubas. */
    private void despertar() {
        ALZADOS++;
        playSound(DarkCraftMod.SFX_DESTILADO_ALERTA.get(), 1.0f, variante() == CUBA ? 0.7f : 0.95f + random.nextFloat() * 0.15f);
        if (level() instanceof ServerLevel sl) sl.sendParticles(VERDE, getX(), getY() + 1.0, getZ(), 10, 0.4, 0.5, 0.4, 0.01);
    }

    // ------------------------------------------------------------------ morir: a la cuba le revienta la cuba

    @Override
    public void die(DamageSource s) {
        boolean ya = !isAlive() && deathTime > 0;
        super.die(s);
        if (level().isClientSide || ya) return;
        if (variante() == CUBA) romperCuba();
        ServerPlayer quien = s.getEntity() instanceof ServerPlayer p ? p : getKillCredit() instanceof ServerPlayer p2 ? p2 : null;
        if (quien != null && !esEfimero()) {
            com.darkcraft.espejo.Espejo.bestia(quien, tipo());
            com.darkcraft.story.Logros.dar(quien, "destilado");
        }
    }

    /** Si se le ha roto la cuba (al morir: el modelo esconde el destilado de dentro). */
    public boolean cubaRota() {
        return variante() == CUBA && (deathTime > 0 || !isAlive());
    }

    /** Revienta la cuba: cristal, el destilado por el suelo y el charco verde que cura a los destilados que lo pisen. */
    private void romperCuba() {
        if (!(level() instanceof ServerLevel sl)) return;
        CUBAS++;
        Vec3 atras = Vec3.directionFromRotation(0, getYRot()).scale(-0.6);
        double x = getX() + atras.x, z = getZ() + atras.z;
        sl.sendParticles(CRISTAL, x, getY() + 2.2, z, 40, 0.6, 0.6, 0.6, 0.2);
        sl.sendParticles(VERDE, x, getY() + 1.6, z, 40, 0.9, 0.8, 0.9, 0.04);
        sl.sendParticles(ParticleTypes.SPLASH, x, getY() + 1.0, z, 40, 1.0, 0.3, 1.0, 0.2);
        sl.playSound(null, x, getY() + 1.5, z, DarkCraftMod.SFX_CUBA_ROMPE.get(), SoundSource.HOSTILE, 1.5f, 0.95f + random.nextFloat() * 0.1f);
        double y = Frasco.suelo(sl, new Vec3(x, getY() + 0.6, z));
        if (!Double.isNaN(y)) sl.addFreshEntity(new Frasco.Charco(sl, x, y, z, this, Frasco.CURA, R_CUBA, T_CUBA));
    }

    /** Al acabar de caer, lo que lleva dentro se le sale (un charquito que humea). */
    @Override
    protected void tickDeath() {
        super.tickDeath();
        if (level() instanceof ServerLevel sl && deathTime == DestiladoAnims.TICKS[DestiladoAnims.MORIR] - 6) {
            sl.sendParticles(VERDE, getX(), getY() + 0.3, getZ(), 18, 0.6, 0.1, 0.6, 0.01);
            sl.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.3, getZ(), 10, 0.5, 0.1, 0.5, 0.01);
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (jugador && random.nextFloat() < 0.07f + 0.01f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
        if (jugador && variante() == CUBA && random.nextFloat() < 0.12f) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_2.get());
        if (jugador && variante() == MANCEBO && random.nextFloat() < 0.15f) spawnAtLocation(net.minecraft.world.item.Items.GLASS_BOTTLE);
    }

    // ------------------------------------------------------------------ en el cliente: lo que luce

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide || !isAlive()) return;
        // las bubas del enfermo y la cuba echan de vez en cuando una lucecita verde
        if ((variante() == ENFERMO && tickCount % 12 == 0) || (variante() == CUBA && tickCount % 6 == 0)) {
            Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
            double atras = variante() == CUBA ? -0.75 : 0, alto = variante() == CUBA ? 2.4 : 1.2 + random.nextDouble() * 0.6;
            level().addParticle(VERDE, getX() + d.x * atras + (random.nextDouble() - 0.5) * 0.5, getY() + alto, getZ() + d.z * atras + (random.nextDouble() - 0.5) * 0.5,
                    0, 0.02, 0);
        }
    }

    // ------------------------------------------------------------------ sonidos y ritmo

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case DestiladoAnims.MORTERO, DestiladoAnims.MORTERO_BARRIDO, DestiladoAnims.ZARPAZO -> DarkCraftMod.SFX_GOLPE_LIGERO.get();
            default -> null;
        };
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_DESTILADO_ALERTA.get();
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (variante()) {
            case ENFERMO -> 8 + random.nextInt(12);
            case CUBA -> 20 + random.nextInt(16);
            default -> 16 + random.nextInt(16);
        };
    }

    @Override
    protected double distanciaDeseada() {
        return variante() == MANCEBO ? 6.5 : variante() == CUBA ? 2.4 : 1.6;
    }

    @Override
    protected double alturaMaxima() {
        return variante() == MANCEBO ? 6.0 : super.alturaMaxima();
    }

    @Override
    protected double prisa() {
        return variante() == ENFERMO ? 1.15 : variante() == CUBA ? 0.9 : 1.0;
    }

    @Override
    public float getVoicePitch() {
        float base = variante() == CUBA ? 0.72f : variante() == MANCEBO ? 1.08f : 1.0f;
        return base + (random.nextFloat() - 0.5f) * 0.12f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return DarkCraftMod.SFX_DESTILADO_GEMIDO.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_DESTILADO_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_DESTILADO_MUERTE.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        if (variante() == CUBA) playSound(SoundEvents.ZOMBIE_STEP, 0.45f, 0.65f);
        else playSound(SoundEvents.ZOMBIE_STEP, 0.15f, variante() == ENFERMO ? 1.15f : 1.0f);
    }

    // ------------------------------------------------------------------ para las pruebas

    /** Para las pruebas: que el siguiente frasco sea de ese tipo y a ese (el elixir, a uno de los suyos; si no, a su presa). */
    public void proximoFrasco(int tipo, LivingEntity a) {
        frascoTipo = tipo;
        frascoA = a;
    }

    /** Para las pruebas: que pueda volver a hacerlo todo ya. */
    public void sinEsperas() {
        ultimoFrasco = ultimaCura = ultimoEscupitajo = ultimoSalto = ultimaEmbestida = ultimoDerrame = -100000;
    }

    public String estado() {
        return String.format(java.util.Locale.ROOT, "%s vida %.0f/%.0f, postura %d, accion %d", tipo(), getHealth(), getMaxHealth(), postura(), accion());
    }
}
