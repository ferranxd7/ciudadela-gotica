package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Los penitentes de la Tierra sin Fe (parte 7s): los peregrinos que iban al crater a pedir perdon y a los que la Sombra
 * contesto la noche del cielo rojo. Caminan en procesion por la tierra agrietada, rezan de rodillas en sus campamentos
 * ({@code REZA}: cada uno lo suyo; se levantan si te acercas) o yacen postrados en cruz ({@code TUMBADO}: se alzan de golpe),
 * con la tunica y el capirote de su cofradia y el antifaz con la luz roja de los ojos. Tres:
 * <ul>
 * <li>el <b>flagelante</b> (rojo sangre, la espalda en carne viva): el <b>azote</b> y los tres <b>azotes</b> con la
 * disciplina, que dejan sangrando ({@link Sangrado}: si se acumula, la hemorragia); y herido, la <b>penitencia</b>: se azota a
 * si mismo (pierde vida) y se enfervoriza (pega mas y corre mas un rato);</li>
 * <li>el <b>portador</b> (negro, mas grande y duro, la cruz de madera a rastras): el <b>cruzazo</b> (la estrella en el suelo:
 * una onda a quien este de pie cerca), el <b>barrido</b> y la <b>embestida</b> con la cruz por delante como un ariete;</li>
 * <li>el <b>turiferario</b> (morado y oro, el incensario de laton): lo voltea alrededor (el <b>incensario</b>, quema), echa
 * una nube de <b>humo</b> de incienso de sangre ({@link Incienso}: hace sangrar y frena; a los penitentes los ampara) y, con
 * los suyos heridos, el <b>cantico</b> (los penitentes de alrededor se curan y aguantan mas).</li>
 * </ul>
 * Al morir caen de rodillas, de bruces, y se desangran.
 */
public class Penitente extends Enemigo {
    public static final int FLAGELANTE = 0, PORTADOR = 1, TURIFERARIO = 2;
    /** La sangre que suma cada azote (el triple, cada uno de los tres). */
    public static final float SANGRE_AZOTE = 34f, SANGRE_AZOTES = 24f;
    /** La penitencia: lo que se quita cada azote (de su vida maxima) y lo que dura el fervor (ticks). */
    public static final float PENITENCIA = 0.06f;
    public static final int FERVOR = 200;
    /** El cantico: hasta donde llega, lo que cura (de la vida maxima de cada uno) y cada cuanto. */
    public static final double R_CANTICO = 10.0;
    public static final float CURA = 0.15f;
    /** Lo que esperan entre dos de lo mismo (ticks). */
    public static final int ESPERA_PENITENCIA = 20 * 18, ESPERA_EMBESTIDA = 20 * 6, ESPERA_HUMO = 20 * 8, ESPERA_CANTICO = 20 * 12;
    /** La onda del cruzazo: delante (bloques) y su radio. */
    public static final double ONDA_DELANTE = 2.8, R_ONDA = 2.6;

    public static final DustParticleOptions SANGRE = new DustParticleOptions(new Vector3f(0.5f, 0.02f, 0.04f), 1.3f),
            FERVOR_P = new DustParticleOptions(new Vector3f(0.9f, 0.15f, 0.08f), 1.0f),
            CANTO = new DustParticleOptions(new Vector3f(0.95f, 0.75f, 0.3f), 1.0f);
    static final BlockParticleOption CHARCO = new BlockParticleOption(ParticleTypes.BLOCK,
            net.minecraft.world.level.block.Blocks.RED_CONCRETE.defaultBlockState());

    private int ultimaPenitencia = -100000, ultimaEmbestida = -100000, ultimoHumo = -100000, ultimoCantico = -100000, fervor;

    /**
     * Para la prueba: los azotes que han dado, las penitencias (y los enfervorizados), las ondas del cruzazo que han dado, las
     * embestidas, las nubes de humo, los canticos y a cuantos han curado, los que se alzan de la postracion y los que se
     * desangran al morir.
     */
    public static int AZOTES, PENITENCIAS, ENFERVORIZADOS, ONDAS, EMBESTIDAS, HUMOS, CANTICOS, CURADOS, ALZADOS, DESANGRADOS;

    public Penitente(EntityType<? extends Penitente> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 32.0).add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.ARMOR, 0.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23).add(Attributes.FOLLOW_RANGE, 24.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.3);
    }

    @Override
    public String tipo() {
        return switch (variante()) {
            case PORTADOR -> "penitente_portador";
            case TURIFERARIO -> "penitente_turiferario";
            default -> "penitente_flagelante";
        };
    }

    /** Cada uno, su vida, su golpe y su paso: el portador, grande y duro; el flagelante, rapido. */
    @Override
    public void variante(int v) {
        super.variante(v);
        set(Attributes.MAX_HEALTH, v == PORTADOR ? 70 : v == TURIFERARIO ? 36 : 32);
        set(Attributes.ATTACK_DAMAGE, v == PORTADOR ? 8.5 : v == TURIFERARIO ? 4.5 : 5.0);
        set(Attributes.ARMOR, v == PORTADOR ? 4.0 : v == TURIFERARIO ? 1.0 : 0.0);
        set(Attributes.MOVEMENT_SPEED, v == PORTADOR ? 0.19 : v == TURIFERARIO ? 0.21 : 0.24);
        set(Attributes.KNOCKBACK_RESISTANCE, v == PORTADOR ? 0.85 : v == TURIFERARIO ? 0.4 : 0.3);
        setHealth(getMaxHealth());
    }

    private void set(net.minecraft.world.entity.ai.attributes.Attribute a, double valor) {
        var i = getAttribute(a);
        if (i != null) i.setBaseValue(valor);
    }

    @Override
    protected int[] ticks() {
        return PenitenteAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return PenitenteAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return PenitenteAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return PenitenteAnims.MORIR;
    }

    /** Postrado en cruz: se alza; de rodillas: se levanta. */
    @Override
    protected int accionDespertar(int postura) {
        return postura == TUMBADO ? PenitenteAnims.ALZARSE : PenitenteAnims.LEVANTARSE;
    }

    /** Si esta enfervorizado (para las pruebas y el dibujo: sale vapor rojo). */
    public boolean enfervorizado() {
        return fervor > 0;
    }

    // ------------------------------------------------------------------ que hace

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (Math.abs(presa.getY() - getY()) > 3.0) return 0;
        return switch (variante()) {
            case PORTADOR -> {
                if (d >= 4.5 && d <= 9.5 && tickCount - ultimaEmbestida > ESPERA_EMBESTIDA && hasLineOfSight(presa) && random.nextFloat() < 0.5f)
                    yield PenitenteAnims.EMBESTIDA;
                if (d < 3.1) yield random.nextFloat() < 0.45f ? PenitenteAnims.CRUZAZO : PenitenteAnims.BARRIDO;
                yield d < 3.7 && random.nextFloat() < 0.3f ? PenitenteAnims.CRUZAZO : 0;
            }
            case TURIFERARIO -> {
                if (tickCount - ultimoCantico > ESPERA_CANTICO && heridosCerca() > 0) yield PenitenteAnims.CANTICO;
                if (d >= 2.4 && d <= 8.5 && tickCount - ultimoHumo > ESPERA_HUMO && hasLineOfSight(presa) && random.nextFloat() < 0.45f)
                    yield PenitenteAnims.HUMO;
                if (d < 3.2) yield PenitenteAnims.INCENSARIO;
                yield 0;
            }
            default -> {
                if (fervor <= 0 && getHealth() < getMaxHealth() * 0.6f && tickCount - ultimaPenitencia > ESPERA_PENITENCIA && d > 2.0)
                    yield PenitenteAnims.PENITENCIA;
                if (d < 3.0) yield random.nextFloat() < 0.35f ? PenitenteAnims.AZOTES : PenitenteAnims.AZOTE;
                yield d < 3.7 && random.nextFloat() < 0.3f ? PenitenteAnims.AZOTE : 0;
            }
        };
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case PenitenteAnims.AZOTE -> new Golpe(3.4f, 80f, 0.9f, 0.3f);
            case PenitenteAnims.AZOTES -> new Golpe(3.2f, 90f, 0.6f, 0.2f);
            case PenitenteAnims.CRUZAZO -> new Golpe(3.6f, 60f, 1.5f, 0.8f);
            case PenitenteAnims.BARRIDO -> new Golpe(3.8f, 170f, 1.0f, 0.9f);
            case PenitenteAnims.EMBESTIDA -> new Golpe(2.5f, 80f, 1.1f, 1.3f);
            case PenitenteAnims.INCENSARIO -> new Golpe(3.4f, 300f, 0.9f, 0.6f);
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = PenitenteAnims.GOLPES[accion];
        switch (accion) {
            case PenitenteAnims.AZOTE -> {
                if (t == g[0] - 3) playSound(DarkCraftMod.SFX_PENITENTE_AZOTE.get(), 1.1f, 0.9f + random.nextFloat() * 0.2f);
                if (t == g[0] - 1) empujar(0.25);
            }
            case PenitenteAnims.AZOTES -> {
                for (int k = 0; k < g.length; k++) {
                    if (t == g[k] - 3) playSound(DarkCraftMod.SFX_PENITENTE_AZOTE.get(), 1.0f, 0.95f + k * 0.08f + random.nextFloat() * 0.1f);
                    if (t == g[k] - 1) empujar(0.2);
                }
            }
            case PenitenteAnims.PENITENCIA -> {
                if (t == 2) {
                    ultimaPenitencia = tickCount;
                    PENITENCIAS++;
                }
                for (int k = 0; k < g.length - 1; k++)
                    if (t == g[k]) azotarse();
                if (t == g[g.length - 1]) enfervorizarse();
            }
            case PenitenteAnims.CRUZAZO -> {
                if (t == 8) playSound(DarkCraftMod.SFX_PENITENTE_ARRASTRE.get(), 0.9f, 0.9f + random.nextFloat() * 0.15f);
                if (t == g[0] - 1) empujar(0.35);
                if (t == g[0] + 1) onda();
            }
            case PenitenteAnims.BARRIDO -> {
                if (t == g[0] - 2) empujar(0.3);
            }
            case PenitenteAnims.EMBESTIDA -> {
                // agacha la cruz por delante (se ve venir) y corre cuatro o cinco bloques
                if (t == 4) {
                    ultimaEmbestida = tickCount;
                    EMBESTIDAS++;
                    playSound(DarkCraftMod.SFX_PENITENTE_ARRASTRE.get(), 1.3f, 1.05f);
                }
                if (t >= 7 && t <= g[g.length - 1]) empujar(0.5);
            }
            case PenitenteAnims.INCENSARIO -> {
                if (t == g[0] - 5) playSound(DarkCraftMod.SFX_PENITENTE_INCENSARIO.get(), 1.1f, 0.9f + random.nextFloat() * 0.2f);
                if (t >= g[0] - 4 && t <= g[0] + 4 && level() instanceof ServerLevel sl) {
                    // la estela de brasas y humo del incensario al voltearlo
                    double a = Math.toRadians(getYRot() + 90 + (t - g[0]) * 34);
                    sl.sendParticles(Incienso.HUMO, getX() + Math.cos(a) * 1.6, getY() + 1.0, getZ() + Math.sin(a) * 1.6, 3, 0.15, 0.1, 0.15, 0.0);
                    sl.sendParticles(ParticleTypes.SMALL_FLAME, getX() + Math.cos(a) * 1.6, getY() + 1.0, getZ() + Math.sin(a) * 1.6, 1, 0.1, 0.1, 0.1, 0.01);
                }
            }
            case PenitenteAnims.HUMO -> {
                if (t == 8 || t == 12 || t == 16) playSound(DarkCraftMod.SFX_PENITENTE_INCENSARIO.get(), 0.7f, 1.2f);
                if (t == g[0]) humo();
            }
            case PenitenteAnims.CANTICO -> {
                if (t == 4) {
                    ultimoCantico = tickCount;
                    playSound(DarkCraftMod.SFX_PENITENTE_CANTICO.get(), 1.6f, 0.95f + random.nextFloat() * 0.1f);
                }
                if (t > 6 && t < g[0] && t % 3 == 0 && level() instanceof ServerLevel sl)
                    sl.sendParticles(CANTO, getX(), getY() + 2.4, getZ(), 3, 0.5, 0.3, 0.5, 0.02);
                if (t == g[0]) cantico();
            }
            case PenitenteAnims.LEVANTARSE, PenitenteAnims.ALZARSE -> {
                if (t == 2) sacudirse(accion == PenitenteAnims.ALZARSE);
            }
            default -> {
            }
        }
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        switch (accion) {
            case PenitenteAnims.AZOTE -> {
                AZOTES++;
                Sangrado.sumar(le, SANGRE_AZOTE, this);
            }
            case PenitenteAnims.AZOTES -> {
                AZOTES++;
                Sangrado.sumar(le, SANGRE_AZOTES, this);
            }
            case PenitenteAnims.INCENSARIO -> le.setSecondsOnFire(2);
            case PenitenteAnims.EMBESTIDA -> le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 1), this);
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ la penitencia, la onda, el humo y el cantico

    /** Se azota la espalda: pierde vida (sin morir de eso) y le salta la sangre. */
    private void azotarse() {
        playSound(DarkCraftMod.SFX_PENITENTE_PENITENCIA.get(), 1.0f, 0.9f + random.nextFloat() * 0.2f);
        float quita = Math.min(getHealth() - 1f, getMaxHealth() * PENITENCIA);
        if (quita > 0) setHealth(getHealth() - quita);
        if (level() instanceof ServerLevel sl) {
            Vec3 d = Vec3.directionFromRotation(0, getYRot());
            sl.sendParticles(SANGRE, getX() - d.x * 0.35, getY() + 1.5, getZ() - d.z * 0.35, 12, 0.25, 0.2, 0.25, 0.1);
        }
    }

    /** Se yergue con los brazos al cielo: pega mas y corre mas {@link #FERVOR} ticks. */
    private void enfervorizarse() {
        fervor = FERVOR;
        ENFERVORIZADOS++;
        animo(0.4, FERVOR);
        addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, FERVOR, 1, false, false));
        playSound(DarkCraftMod.SFX_PENITENTE_ALERTA.get(), 1.4f, 0.8f);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(FERVOR_P, getX(), getY() + 1.2, getZ(), 30, 0.4, 0.7, 0.4, 0.05);
            sl.sendParticles(SANGRE, getX(), getY() + 1.6, getZ(), 20, 0.3, 0.3, 0.3, 0.15);
        }
    }

    /** La cruz da en el suelo: polvo, astillas y una onda que alcanza a quien este de pie cerca. */
    private void onda() {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * ONDA_DELANTE, z = getZ() + d.z * ONDA_DELANTE;
        BlockState suelo = level().getBlockState(BlockPos.containing(x, getY() - 0.5, z));
        if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), x, getY() + 0.1, z, 30, 0.6, 0.05, 0.6, 0.2);
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.DARK_OAK_PLANKS.defaultBlockState()),
                x, getY() + 0.4, z, 12, 0.4, 0.2, 0.4, 0.15);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, x, getY() + 0.2, z, 8, 0.7, 0.1, 0.7, 0.02);
        level().playSound(null, x, getY(), z, DarkCraftMod.SFX_PENITENTE_CRUZ.get(), SoundSource.HOSTILE, 1.4f, 0.9f + random.nextFloat() * 0.15f);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6f;
        for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, new AABB(x - 3, getY() - 1, z - 3, x + 3, getY() + 2, z + 3),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            if (!le.onGround() || le.distanceToSqr(x, le.getY(), z) > R_ONDA * R_ONDA) continue;
            if (le.hurt(damageSources().mobAttack(this), dano)) {
                ONDAS++;
                le.knockback(0.45, x - le.getX(), z - le.getZ());
                le.setDeltaMovement(le.getDeltaMovement().add(0, 0.3, 0));
            }
        }
    }

    /** Sacude el incensario hacia su presa: alli (o delante, si no hay) queda una nube de incienso de sangre. */
    private void humo() {
        if (!(level() instanceof ServerLevel sl)) return;
        ultimoHumo = tickCount;
        HUMOS++;
        LivingEntity p = getTarget();
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = p != null ? p.getX() : getX() + d.x * 3, z = p != null ? p.getZ() : getZ() + d.z * 3;
        double y = p != null ? p.getY() : getY();
        // de el a donde cae: una estela de humo
        for (int k = 1; k <= 6; k++) {
            double s = k / 6.0;
            sl.sendParticles(Incienso.HUMO, getX() + (x - getX()) * s, getY() + 1.6 - s * 0.8, getZ() + (z - getZ()) * s, 4, 0.2, 0.2, 0.2, 0.0);
        }
        playSound(DarkCraftMod.SFX_PENITENTE_HUMO.get(), 1.3f, 0.9f + random.nextFloat() * 0.2f);
        sl.addFreshEntity(new Incienso(sl, x, y, z, this));
    }

    /** Los penitentes de alrededor (el tambien) que no tienen la vida entera. */
    private int heridosCerca() {
        int n = 0;
        for (Penitente p : level().getEntitiesOfClass(Penitente.class, getBoundingBox().inflate(R_CANTICO, 3, R_CANTICO), e -> e.isAlive()))
            if (p.getHealth() < p.getMaxHealth() * 0.75f) n++;
        return n;
    }

    /** El cantico: los penitentes de alrededor se curan un tanto y aguantan mas un rato. */
    private void cantico() {
        if (!(level() instanceof ServerLevel sl)) return;
        CANTICOS++;
        for (Penitente p : level().getEntitiesOfClass(Penitente.class, getBoundingBox().inflate(R_CANTICO, 3, R_CANTICO), e -> e.isAlive())) {
            if (p.distanceToSqr(this) > R_CANTICO * R_CANTICO) continue;
            p.heal(p.getMaxHealth() * CURA);
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, false));
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0, false, false));
            sl.sendParticles(CANTO, p.getX(), p.getY() + p.getBbHeight() + 0.3, p.getZ(), 10, 0.3, 0.3, 0.3, 0.02);
            if (p != this) CURADOS++;
        }
        for (int k = 0; k < 28; k++) {
            double a = k * Math.PI * 2 / 28;
            sl.sendParticles(CANTO, getX() + Math.cos(a) * 2.0, getY() + 0.3, getZ() + Math.sin(a) * 2.0, 1, Math.cos(a) * 0.5, 0.05, Math.sin(a) * 0.5, 0.3);
        }
    }

    /** Se levanta de rezar (el polvo) o se alza de la postracion (el polvo y la tierra roja). */
    private void sacudirse(boolean dePostrado) {
        if (!(level() instanceof ServerLevel sl)) return;
        if (dePostrado) {
            ALZADOS++;
            sl.sendParticles(CHARCO, getX(), getY() + 0.2, getZ(), 24, 0.6, 0.15, 0.6, 0.1);
            sl.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.3, getZ(), 10, 0.6, 0.2, 0.6, 0.02);
            playSound(DarkCraftMod.SFX_PENITENTE_ALZARSE.get(), 1.1f, 0.9f + random.nextFloat() * 0.2f);
        } else sl.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.4, getZ(), 8, 0.4, 0.2, 0.4, 0.01);
    }

    // ------------------------------------------------------------------ cada tick

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            if (!isAlive()) return;
            // el humo del incensario (el turiferario) y la sangre que gotea del flagelante
            if (variante() == TURIFERARIO && tickCount % 4 == 0) {
                Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
                double alto = postura() == DE_PIE ? 0.75 : 0.45;
                double x = getX() + d.x * 0.45 - d.z * 0.35, z = getZ() + d.z * 0.45 + d.x * 0.35;
                level().addParticle(ParticleTypes.SMOKE, x, getY() + alto, z, 0, 0.03, 0);
                if (tickCount % 8 == 0) level().addParticle(Incienso.HUMO, x, getY() + alto + 0.2, z, 0, 0.02, 0);
            }
            if (variante() == FLAGELANTE && tickCount % 17 == 0)
                level().addParticle(SANGRE, getX() + (random.nextDouble() - 0.5) * 0.4, getY() + 1.2, getZ() + (random.nextDouble() - 0.5) * 0.4, 0, -0.05, 0);
            return;
        }
        if (fervor > 0) {
            fervor--;
            if (fervor % 6 == 0 && level() instanceof ServerLevel sl)
                sl.sendParticles(FERVOR_P, getX(), getY() + 1.4, getZ(), 2, 0.3, 0.4, 0.3, 0.02);
        }
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case PenitenteAnims.BARRIDO -> DarkCraftMod.SFX_GOLPE_PESADO.get();
            default -> null;
        };
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_PENITENTE_ALERTA.get();
    }

    @Override
    protected int pausaTras(int accion) {
        int fervorMenos = fervor > 0 ? 6 : 0;
        return switch (variante()) {
            case PORTADOR -> 22 + random.nextInt(18);
            case TURIFERARIO -> 16 + random.nextInt(14);
            default -> Math.max(4, 12 + random.nextInt(12) - fervorMenos);
        };
    }

    @Override
    protected double distanciaDeseada() {
        return variante() == PORTADOR ? 2.4 : variante() == TURIFERARIO ? 2.6 : 2.0;
    }

    @Override
    protected double prisa() {
        return variante() == PORTADOR ? 0.85 : fervor > 0 ? 1.25 : 1.0;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return DarkCraftMod.SFX_PENITENTE_REZO.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_PENITENTE_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_PENITENTE_DOLOR.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 260;
    }

    @Override
    public void die(DamageSource s) {
        super.die(s);
        if (level().isClientSide) return;
        ServerPlayer quien = s.getEntity() instanceof ServerPlayer p ? p : getKillCredit() instanceof ServerPlayer p2 ? p2 : null;
        if (quien != null && !esEfimero()) {
            com.darkcraft.espejo.Espejo.bestia(quien, tipo());
            com.darkcraft.story.Logros.dar(quien, "penitente");
        }
    }

    /** Al acabar de morir (de bruces) no se va en humo: se desangra (un charco que se queda un momento). */
    @Override
    protected void tickDeath() {
        int m = PenitenteAnims.MORIR;
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        if (deathTime == PenitenteAnims.TICKS[m] - 2) desangrarse();
        if (deathTime >= PenitenteAnims.TICKS[m] + 2) remove(RemovalReason.KILLED);
    }

    private void desangrarse() {
        if (!(level() instanceof ServerLevel sl)) return;
        DESANGRADOS++;
        sl.sendParticles(CHARCO, getX(), getY() + 0.15, getZ(), 40, 0.8, 0.05, 0.8, 0.12);
        sl.sendParticles(SANGRE, getX(), getY() + 0.3, getZ(), 34, 0.8, 0.15, 0.8, 0.02);
        sl.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.4, getZ(), 12, 0.6, 0.2, 0.6, 0.02);
        sl.playSound(null, getX(), getY(), getZ(), DarkCraftMod.SFX_PENITENTE_MUERTE.get(), SoundSource.HOSTILE, 1.2f, 0.9f + random.nextFloat() * 0.2f);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (jugador && random.nextFloat() < 0.06f + 0.01f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
        if (jugador && variante() == PORTADOR && random.nextFloat() < 0.06f) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_2.get());
    }

    /** Para las pruebas: cuanta sangre lleva sumada alguien. */
    public static float sangre(LivingEntity le) {
        return Sangrado.nivel(le);
    }

}
