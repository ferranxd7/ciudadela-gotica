package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.UUID;

/**
 * Los calcinados de la Capital en ruinas (parte 7m): la gente de la capital, que ardio la noche que el cielo se puso rojo y
 * no acabo de apagarse. Negros de carbon, cuarteados, con las brasas que siguen dentro luciendo por las grietas; van
 * soltando chispas y humo. Tres:
 * <ul>
 * <li>la <b>guardia</b> del Juramento: armadura ennegrecida, sobreveste roja con la mano alzada, escudo de torre y una
 * espada al rojo. <b>Se cubre</b>: de frente y sin estar atacando, el escudo para casi todo (lo que llega en flecha, todo);
 * por un lado, por la espalda o mientras ataca, entra. El <b>tajo</b> quema; el <b>escudazo</b> empuja y frena;</li>
 * <li>el <b>incendiario</b>: un vecino con un cantaro de pez encendido; de lejos lo <b>lanza</b> en arco ({@link Cantaro}:
 * donde revienta el suelo arde un rato) y de cerca golpea con el (quema);</li>
 * <li>el <b>ceniciento</b>: consumido, en harapos, cubierto de ceniza; espera <b>enterrado en la ceniza</b> de los derrumbes
 * ({@code TUMBADO}: se alza de golpe); <b>zarpazo</b> y <b>agarron</b> (se te echa encima: te sujeta y te quema). Al morir
 * <b>revienta en ceniza</b>: ciega y frena un momento a quien este al lado.</li>
 * </ul>
 * El fuego no les hace nada (ni la lava); <b>el frio, un 60 % mas</b>, y el agua los apaga (les duele). La guardia y el
 * incendiario esperan agazapados junto a los rescoldos ({@code REZA}: se levantan al acercarse alguien) o de pie.
 */
public class Calcinado extends Enemigo {
    public static final int GUARDIA = 0, INCENDIARIO = 1, CENICIENTO = 2;
    /** Lo que deja pasar el escudo de frente (de lo que no es flecha). */
    public static final float ESCUDO = 0.2f;
    /** Lo que esperan entre un cantaro y el siguiente, un escudazo, un agarron (ticks). */
    public static final int ESPERA_CANTARO = 20 * 3 + 10, ESPERA_ESCUDAZO = 20 * 4, ESPERA_AGARRON = 20 * 3;
    /** La nube de ceniza del ceniciento al morir: radio y lo que dura. */
    public static final float R_CENIZA = 2.6f;
    private static final UUID MOD_GUARDIA = UUID.fromString("6b1e2f3a-4c5d-4e6f-8a9b-0c1d2e3f4a5b");
    static final DustParticleOptions HUMO = new DustParticleOptions(new Vector3f(0.18f, 0.16f, 0.15f), 1.6f),
            BRASA = new DustParticleOptions(new Vector3f(1.0f, 0.42f, 0.1f), 0.9f),
            CENIZA = new DustParticleOptions(new Vector3f(0.62f, 0.6f, 0.57f), 1.8f);
    static final BlockParticleOption CARBON = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COAL_BLOCK.defaultBlockState()),
            CENIZA_BLOQUE = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GRAY_CONCRETE_POWDER.defaultBlockState());

    private int ultimoCantaro = -100000, ultimoEscudazo = -100000, ultimoAgarron = -100000;

    /** Para la prueba: golpes parados con el escudo, cantaros lanzados, quemados, agarrones que pillan, nubes de ceniza. */
    public static int BLOQUEOS, CANTAROS, QUEMADOS, AGARRONES, CENIZAS, ALZADOS;

    public Calcinado(EntityType<? extends Calcinado> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 5.5)
                .add(Attributes.MOVEMENT_SPEED, 0.21).add(Attributes.FOLLOW_RANGE, 20.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.3)
                .add(Attributes.ARMOR, 3.0);
    }

    /** La guardia aguanta mas (la armadura) y pega mas fuerte. */
    @Override
    public void variante(int v) {
        super.variante(v);
        if (level().isClientSide) return;
        var vida = getAttribute(Attributes.MAX_HEALTH);
        var armadura = getAttribute(Attributes.ARMOR);
        if (vida == null || armadura == null) return;
        vida.removeModifier(MOD_GUARDIA);
        armadura.removeModifier(MOD_GUARDIA);
        if (v == GUARDIA) {
            vida.addPermanentModifier(new AttributeModifier(MOD_GUARDIA, "darkcraft_guardia_calcinada", 0.4, AttributeModifier.Operation.MULTIPLY_BASE));
            armadura.addPermanentModifier(new AttributeModifier(MOD_GUARDIA, "darkcraft_guardia_calcinada", 5.0, AttributeModifier.Operation.ADDITION));
        }
        setHealth(getMaxHealth());
    }

    @Override
    public String tipo() {
        return switch (variante()) {
            case INCENDIARIO -> "calcinado_incendiario";
            case CENICIENTO -> "calcinado_ceniciento";
            default -> "calcinado_guardia";
        };
    }

    @Override
    protected int[] ticks() {
        return CalcinadoAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return CalcinadoAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return CalcinadoAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return CalcinadoAnims.MORIR;
    }

    @Override
    protected int accionDespertar(int postura) {
        return postura == TUMBADO ? CalcinadoAnims.ALZARSE : CalcinadoAnims.LEVANTARSE;
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        return switch (variante()) {
            case INCENDIARIO -> {
                if (d > 4.5 && d < 15 && tickCount - ultimoCantaro > ESPERA_CANTARO) yield CalcinadoAnims.LANZAR;
                yield d < 2.9 ? CalcinadoAnims.TEA : 0;
            }
            case CENICIENTO -> {
                if (d > 2.0 && d < 4.4 && tickCount - ultimoAgarron > ESPERA_AGARRON && random.nextFloat() < 0.5f) yield CalcinadoAnims.AGARRON;
                yield d < 2.5 ? CalcinadoAnims.ZARPAZO : 0;
            }
            default -> {
                if (d < 2.6 && tickCount - ultimoEscudazo > ESPERA_ESCUDAZO && random.nextFloat() < 0.35f) yield CalcinadoAnims.ESCUDAZO;
                if (d < 3.0) yield CalcinadoAnims.TAJO;
                yield d < 3.5 && random.nextFloat() < 0.3f ? CalcinadoAnims.TAJO : 0;
            }
        };
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case CalcinadoAnims.TAJO -> new Golpe(3.3f, 90f, 1.3f, 0.5f);
            case CalcinadoAnims.ESCUDAZO -> new Golpe(2.7f, 80f, 0.7f, 1.4f);
            case CalcinadoAnims.TEA -> new Golpe(2.9f, 110f, 0.9f, 0.4f);
            case CalcinadoAnims.ZARPAZO -> new Golpe(2.6f, 90f, 1.0f, 0.35f);
            case CalcinadoAnims.AGARRON -> new Golpe(2.5f, 70f, 0.8f, 0.0f);
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        switch (accion) {
            case CalcinadoAnims.TAJO -> {
                if (t == 12) empujar(0.25);
                if (t == 16) playSound(DarkCraftMod.SFX_CALCINADO_TAJO.get(), 1.0f, 0.9f + random.nextFloat() * 0.2f);
                if (t >= 16 && t <= 21) estela(t);
            }
            case CalcinadoAnims.ESCUDAZO -> {
                if (t == 9) {
                    ultimoEscudazo = tickCount;
                    empujar(0.55);
                }
                if (t == 12) playSound(DarkCraftMod.SFX_CALCINADO_ESCUDO.get(), 1.2f, 0.85f + random.nextFloat() * 0.15f);
            }
            case CalcinadoAnims.LANZAR -> {
                if (t == 6) playSound(DarkCraftMod.SFX_CALCINADO_GEMIDO.get(), 0.8f, 1.2f);
                if (t >= 6 && t <= 16 && t % 2 == 0 && level() instanceof ServerLevel sl) {
                    // la mecha, alzada por encima del hombro
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    sl.sendParticles(ParticleTypes.FLAME, getX() - d.x * 0.3 - d.z * 0.4, getY() + getBbHeight() + 0.3, getZ() - d.z * 0.3 + d.x * 0.4,
                            2, 0.05, 0.05, 0.05, 0.01);
                }
                if (t == 17) lanzarCantaro();
            }
            case CalcinadoAnims.TEA -> {
                if (t == 6) empujar(0.3);
            }
            case CalcinadoAnims.AGARRON -> {
                if (t == 9) {
                    ultimoAgarron = tickCount;
                    // se echa encima: un salto corto hacia su presa
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    setDeltaMovement(d.x * 0.62, 0.22, d.z * 0.62);
                    hasImpulse = true;
                    playSound(DarkCraftMod.SFX_CALCINADO_ALERTA.get(), 1.0f, 1.15f);
                }
            }
            case CalcinadoAnims.ZARPAZO -> {
                if (t == 4) empujar(0.3);
            }
            case CalcinadoAnims.LEVANTARSE, CalcinadoAnims.ALZARSE -> {
                if (t == 2) despertar(accion == CalcinadoAnims.ALZARSE);
            }
            default -> {
            }
        }
    }

    /** Lo que pega, quema; el escudazo frena; el agarron sujeta (casi no deja moverse) y quema mas. */
    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        switch (accion) {
            case CalcinadoAnims.TAJO -> quemar(le, 4);
            case CalcinadoAnims.TEA -> quemar(le, 3);
            case CalcinadoAnims.ZARPAZO -> quemar(le, 2);
            case CalcinadoAnims.ESCUDAZO -> le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1), this);
            case CalcinadoAnims.AGARRON -> {
                AGARRONES++;
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 3), this);
                le.setDeltaMovement(Vec3.ZERO);
                le.hurtMarked = true;
                quemar(le, 5);
                if (level() instanceof ServerLevel sl)
                    sl.sendParticles(ParticleTypes.FLAME, le.getX(), le.getY() + 1.0, le.getZ(), 16, 0.3, 0.5, 0.3, 0.02);
            }
            default -> {
            }
        }
    }

    /** Le prende fuego (lo apaga el agua; lo frenan el fruto y el talisman de la brasa). */
    public static void quemar(LivingEntity le, int segundos) {
        if (le instanceof Calcinado || le.fireImmune()) return;
        le.setSecondsOnFire(Math.max(segundos, le.getRemainingFireTicks() / 20));
        QUEMADOS++;
    }

    /** El filo al rojo deja una estela de chispas por donde pasa. */
    private void estela(int t) {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot() + (t - 18) * 22);
        double s = 1.6;
        sl.sendParticles(ParticleTypes.FLAME, getX() + d.x * s, getY() + 1.6 - (t - 16) * 0.22, getZ() + d.z * s, 3, 0.15, 0.1, 0.15, 0.01);
        sl.sendParticles(BRASA, getX() + d.x * s, getY() + 1.5 - (t - 16) * 0.22, getZ() + d.z * s, 2, 0.2, 0.1, 0.2, 0.0);
    }

    /** Lanza el cantaro encendido en arco a donde esta su presa (con lo que cae en cuenta). */
    private void lanzarCantaro() {
        if (level().isClientSide) return;
        ultimoCantaro = tickCount;
        LivingEntity t = getTarget();
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double ox = getX() + d.x * 0.6, oz = getZ() + d.z * 0.6, oy = getY() + getBbHeight() + 0.2;
        double tx = t == null ? ox + d.x * 8 : t.getX(), tz = t == null ? oz + d.z * 8 : t.getZ(), ty = t == null ? getY() : t.getY();
        Cantaro c = new Cantaro(level(), this);
        c.setPos(ox, oy, oz);
        c.setDeltaMovement(Cantaro.tiro(ox, oy, oz, tx, ty + 0.2, tz));
        level().addFreshEntity(c);
        CANTAROS++;
        playSound(DarkCraftMod.SFX_GOLPE_LIGERO.get(), 0.8f, 0.8f);
    }

    /** Se levanta (estaba agazapado junto a los rescoldos) o se alza de la ceniza: chispas y ceniza. */
    private void despertar(boolean ceniza) {
        if (!(level() instanceof ServerLevel sl)) return;
        ALZADOS++;
        sl.sendParticles(ceniza ? CENIZA_BLOQUE : CARBON, getX(), getY() + 0.5, getZ(), 26, 0.5, 0.4, 0.5, 0.1);
        sl.sendParticles(ceniza ? CENIZA : HUMO, getX(), getY() + 0.8, getZ(), 20, 0.6, 0.5, 0.6, 0.02);
        sl.sendParticles(ParticleTypes.LAVA, getX(), getY() + 1.0, getZ(), 5, 0.3, 0.3, 0.3, 0.0);
        playSound(DarkCraftMod.SFX_CALCINADO_ALZARSE.get(), 1.0f, 0.9f + random.nextFloat() * 0.2f);
    }

    // ------------------------------------------------------------------ el escudo de la guardia

    /** Si el escudo le tapa ese golpe: de frente (70 grados a cada lado), sin estar atacando, ni aturdido, ni cayendo. */
    private boolean cubre(DamageSource s) {
        if (variante() != GUARDIA || postura() != DE_PIE || muriendo() || aturdido()) return false;
        int a = accion();
        if (a != 0 && a != CalcinadoAnims.BLOQUEO) return false;
        if (s.is(DamageTypeTags.BYPASSES_ARMOR) || s.is(DamageTypeTags.IS_FIRE) || s.is(DamageTypeTags.IS_EXPLOSION) || s.is(DamageTypeTags.IS_FREEZING))
            return false;
        Vec3 de = s.getSourcePosition();
        if (de == null) return false;
        Vec3 mira = Vec3.directionFromRotation(0, yBodyRot);
        double dx = de.x - getX(), dz = de.z - getZ(), l = Math.sqrt(dx * dx + dz * dz);
        return l > 0.01 && (dx * mira.x + dz * mira.z) / l > 0.34;
    }

    // ------------------------------------------------------------------ el fuego no les hace nada; el frio, mas; el agua, duele

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        if (s.getEntity() instanceof Enemigo && s.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile) return false;
        if (s.is(DamageTypeTags.IS_FIRE)) return false;
        if (s.is(DamageTypeTags.IS_FREEZING)) cuanto *= 1.6f;
        if (!level().isClientSide && cubre(s)) {
            boolean flecha = s.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile;
            level().playSound(null, getX(), getY() + 1, getZ(), DarkCraftMod.SFX_CALCINADO_ESCUDO.get(), SoundSource.HOSTILE, 0.9f, 1.1f + random.nextFloat() * 0.2f);
            if (level() instanceof ServerLevel sl) {
                Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
                sl.sendParticles(ParticleTypes.LAVA, getX() + d.x * 0.6, getY() + 1.2, getZ() + d.z * 0.6, 3, 0.2, 0.2, 0.2, 0.0);
            }
            BLOQUEOS++;
            if (accion() == 0) empezar(CalcinadoAnims.BLOQUEO);
            if (flecha) return false;
            cuanto *= ESCUDO;
        }
        return super.hurt(s, cuanto);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case CalcinadoAnims.ZARPAZO -> DarkCraftMod.SFX_GOLPE_LIGERO.get();
            case CalcinadoAnims.TEA -> DarkCraftMod.SFX_GOLPE_PESADO.get();
            default -> null;
        };
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_CALCINADO_ALERTA.get();
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (variante()) {
            case GUARDIA -> 18 + random.nextInt(16);
            case INCENDIARIO -> 16 + random.nextInt(14);
            default -> 10 + random.nextInt(12);
        };
    }

    @Override
    protected double distanciaDeseada() {
        return variante() == INCENDIARIO ? 7.0 : variante() == GUARDIA ? 2.2 : 1.8;
    }

    @Override
    protected double prisa() {
        return variante() == CENICIENTO ? 1.15 : variante() == GUARDIA ? 0.9 : 1.0;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide || !isAlive()) return;
        // las brasas que suben, el humo y alguna gota de fuego; el cantaro del incendiario humea
        if (tickCount % 4 == 0)
            level().addParticle(postura() == TUMBADO ? ParticleTypes.SMOKE : ParticleTypes.SMALL_FLAME, getX() + (random.nextDouble() - 0.5) * 0.6,
                    getY() + 0.3 + random.nextDouble() * (postura() == TUMBADO ? 0.4 : 1.5), getZ() + (random.nextDouble() - 0.5) * 0.6, 0, 0.015, 0);
        if (tickCount % 11 == 0)
            level().addParticle(ParticleTypes.SMOKE, getX() + (random.nextDouble() - 0.5) * 0.5, getY() + getBbHeight() * 0.9, getZ() + (random.nextDouble() - 0.5) * 0.5,
                    0, 0.03, 0);
        if (random.nextInt(70) == 0) level().addParticle(ParticleTypes.LAVA, getX(), getY() + 1.0, getZ(), 0, 0, 0);
        if (variante() == INCENDIARIO && postura() == DE_PIE && accion() == 0 && tickCount % 3 == 0) {
            Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
            double x = getX() + d.x * 0.55 - d.z * 0.42, z = getZ() + d.z * 0.55 + d.x * 0.42;
            level().addParticle(ParticleTypes.FLAME, x, getY() + 1.25, z, 0, 0.01, 0);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return postura() == TUMBADO ? null : DarkCraftMod.SFX_CALCINADO_GEMIDO.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_CALCINADO_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_CALCINADO_DOLOR.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    public void die(DamageSource s) {
        super.die(s);
        if (level().isClientSide) return;
        net.minecraft.server.level.ServerPlayer quien = s.getEntity() instanceof net.minecraft.server.level.ServerPlayer p ? p
                : getKillCredit() instanceof net.minecraft.server.level.ServerPlayer p2 ? p2 : null;
        if (quien != null && !esEfimero()) {
            com.darkcraft.espejo.Espejo.bestia(quien, tipo());
            com.darkcraft.story.Logros.dar(quien, "calcinado");
        }
    }

    /** Al acabar de morir (tirado de lado) no se va en humo: se deshace en ceniza y brasas (el ceniciento, en una nube). */
    @Override
    protected void tickDeath() {
        int m = CalcinadoAnims.MORIR;
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        if (deathTime == CalcinadoAnims.TICKS[m] - 4) deshacerse();
        if (deathTime >= CalcinadoAnims.TICKS[m]) remove(RemovalReason.KILLED);
    }

    private void deshacerse() {
        if (!(level() instanceof ServerLevel sl)) return;
        sl.sendParticles(CARBON, getX(), getY() + 0.4, getZ(), 40, 0.5, 0.3, 0.5, 0.18);
        sl.sendParticles(CENIZA, getX(), getY() + 0.5, getZ(), 30, 0.6, 0.4, 0.6, 0.02);
        sl.sendParticles(ParticleTypes.LAVA, getX(), getY() + 0.5, getZ(), 8, 0.4, 0.2, 0.4, 0.0);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.6, getZ(), 10, 0.4, 0.3, 0.4, 0.02);
        sl.playSound(null, getX(), getY(), getZ(), DarkCraftMod.SFX_CALCINADO_MUERTE.get(), SoundSource.HOSTILE, 1.1f, 0.9f + random.nextFloat() * 0.2f);
        if (variante() == CENICIENTO) ceniza(sl);
    }

    /** El ceniciento revienta en ceniza: ciega y frena un momento a quien este al lado (la nube dura un poco). */
    private void ceniza(ServerLevel sl) {
        CENIZAS++;
        sl.sendParticles(CENIZA_BLOQUE, getX(), getY() + 0.8, getZ(), 60, 1.0, 0.6, 1.0, 0.15);
        sl.sendParticles(ParticleTypes.WHITE_ASH, getX(), getY() + 1.0, getZ(), 80, 1.4, 0.8, 1.4, 0.05);
        sl.playSound(null, getX(), getY(), getZ(), DarkCraftMod.SFX_CALCINADO_CENIZA.get(), SoundSource.HOSTILE, 1.4f, 0.9f);
        for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, new AABB(blockPosition()).inflate(R_CENIZA, 1.5, R_CENIZA),
                o -> o instanceof Player p && !p.isCreative() && !p.isSpectator() && o.isAlive())) {
            if (le.distanceToSqr(this) > R_CENIZA * R_CENIZA) continue;
            le.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0), this);
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1), this);
        }
        AreaEffectCloud n = new AreaEffectCloud(level(), getX(), getY(), getZ());
        n.setOwner(this);
        n.setRadius(R_CENIZA);
        n.setRadiusOnUse(0f);
        n.setWaitTime(0);
        n.setDuration(60);
        n.setRadiusPerTick(-R_CENIZA * 0.5f / 60);
        n.setParticle(ParticleTypes.WHITE_ASH);
        n.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0));
        level().addFreshEntity(n);
    }

    /** Su ceniza y su humo no les hacen nada. */
    @Override
    public boolean canBeAffected(MobEffectInstance e) {
        if (e.getEffect() == MobEffects.BLINDNESS) return false;
        return super.canBeAffected(e);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (jugador && random.nextFloat() < 0.06f + 0.01f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
        if (jugador && variante() == GUARDIA && random.nextFloat() < 0.05f) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_2.get());
    }
}
