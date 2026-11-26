package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.UUID;

/**
 * El podrido del Pantano Podrido (parte 7i): los que se ahogaron en el pantano y la Sombra hincho de agua negra. Lentos,
 * hinchados, chorreando; esperan <b>flotando boca abajo</b> en el agua ({@code TUMBADO}: el dibujante los sube hasta que
 * asoma la espalda) o de rodillas en el barro, y se alzan al acercarse alguien. Tres:
 * <ul>
 * <li>el <b>podrido</b>: zarpazo, mordisco a la carrera y el <b>vomito</b> de agua negra (un chorro delante que envenena
 * y deja un charco que frena);</li>
 * <li>el <b>fatuo</b>: lleva un fuego verde de pantano en la mano izquierda; golpea con el y, de lejos, lo <b>lanza</b>
 * ({@link FuegoFatuo}): donde cae, una nube que envenena;</li>
 * <li>el <b>hinchado</b>: la panza a punto de reventar; se deja <b>caer encima</b> de quien tiene delante y <b>eructa</b> una
 * nube de gas alrededor; al morir <b>revienta</b> (una nube mas grande y un golpe a quien este pegado).</li>
 * </ul>
 * A ellos no les hace nada su propio veneno ({@link #canBeAffected}).
 */
public class Podrido extends Enemigo {
    public static final int PODRIDO = 0, FATUO = 1, HINCHADO = 2;
    /** Lo que esperan entre un vomito, un fuego o un eructo y el siguiente (ticks). */
    public static final int ESPERA_VOMITO = 20 * 7, ESPERA_FATUO = 20 * 4, ESPERA_ERUCTO = 20 * 9;
    /** Para las pruebas: las veces que uno ha salido del agua de un impulso (parte 7j). */
    public static int SALIDAS;
    private int salirEspera;
    /** La nube del eructo y la del reventon: radio y lo que dura. */
    public static final float R_ERUCTO = 3.0f, R_REVIENTA = 3.4f;
    private static final UUID MOD_HINCHADO = UUID.fromString("2f6b9d14-8c3e-4a75-b1d2-7e0c5a9f3b48");
    static final DustParticleOptions AGUA_NEGRA = new DustParticleOptions(new Vector3f(0.07f, 0.09f, 0.06f), 1.5f),
            BARRO = new DustParticleOptions(new Vector3f(0.22f, 0.18f, 0.11f), 1.3f),
            GAS = new DustParticleOptions(new Vector3f(0.46f, 0.62f, 0.18f), 1.8f),
            VERDE = new DustParticleOptions(new Vector3f(0.5f, 1f, 0.42f), 1.2f);

    private int ultimoVomito = -100000, ultimoFatuo = -100000, ultimoEructo = -100000;

    /** Para la prueba: vomitos, fuegos lanzados, nubes de gas y reventones desde que arranco el juego. */
    public static int VOMITOS, FATUOS, NUBES, REVENTONES;

    public Podrido(EntityType<? extends Podrido> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 34.0).add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.21).add(Attributes.FOLLOW_RANGE, 18.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.3)
                .add(Attributes.ARMOR, 1.0);
    }

    /** El hinchado aguanta mas (la panza). */
    @Override
    public void variante(int v) {
        super.variante(v);
        if (level().isClientSide) return;
        var vida = getAttribute(Attributes.MAX_HEALTH);
        if (vida == null) return;
        vida.removeModifier(MOD_HINCHADO);
        if (v == HINCHADO) vida.addPermanentModifier(new AttributeModifier(MOD_HINCHADO, "darkcraft_hinchado", 0.65, AttributeModifier.Operation.MULTIPLY_BASE));
        setHealth(getMaxHealth());
    }

    @Override
    public String tipo() {
        return switch (variante()) {
            case FATUO -> "podrido_fatuo";
            case HINCHADO -> "podrido_hinchado";
            default -> "podrido";
        };
    }

    @Override
    protected int[] ticks() {
        return PodridoAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return PodridoAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return PodridoAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return PodridoAnims.MORIR;
    }

    @Override
    protected int accionDespertar(int postura) {
        return postura == TUMBADO ? PodridoAnims.ALZARSE : PodridoAnims.LEVANTARSE;
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        return switch (variante()) {
            case FATUO -> {
                if (d < 2.6) yield random.nextFloat() < 0.65f ? PodridoAnims.GOLPE_FATUO : PodridoAnims.ZARPAZO;
                if (d > 4.2 && d < 18 && tickCount - ultimoFatuo > ESPERA_FATUO) yield PodridoAnims.LANZAR_FATUO;
                yield d < 3.3 && random.nextFloat() < 0.3f ? PodridoAnims.GOLPE_FATUO : 0;
            }
            case HINCHADO -> {
                if (d < 4.0 && tickCount - ultimoEructo > ESPERA_ERUCTO && random.nextFloat() < 0.45f) yield PodridoAnims.ERUCTO;
                yield d < 2.8 ? PodridoAnims.APLASTAR : 0;
            }
            default -> {
                if (d > 3.0 && d < 6.2 && tickCount - ultimoVomito > ESPERA_VOMITO && random.nextFloat() < 0.5f) yield PodridoAnims.VOMITO;
                if (d < 2.3) yield random.nextFloat() < 0.6f ? PodridoAnims.ZARPAZO : PodridoAnims.MORDISCO;
                yield d < 3.7 && random.nextFloat() < 0.4f ? PodridoAnims.MORDISCO : 0;
            }
        };
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case PodridoAnims.ZARPAZO -> new Golpe(2.5f, 100f, 1.0f, 0.35f);
            case PodridoAnims.MORDISCO -> new Golpe(2.4f, 60f, 1.15f, 0.2f);
            // el chorro de agua negra: largo y estrecho
            case PodridoAnims.VOMITO -> new Golpe(5.2f, 40f, 0.5f, 0.1f);
            case PodridoAnims.GOLPE_FATUO -> new Golpe(2.6f, 90f, 1.1f, 0.4f);
            case PodridoAnims.APLASTAR -> new Golpe(2.9f, 80f, 1.75f, 1.0f);
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        switch (accion) {
            case PodridoAnims.ZARPAZO -> {
                if (t == 10) empujar(0.35);
            }
            case PodridoAnims.MORDISCO -> {
                if (t == 11) empujar(0.85);
            }
            case PodridoAnims.VOMITO -> {
                if (t == 14) {
                    ultimoVomito = tickCount;
                    VOMITOS++;
                    playSound(DarkCraftMod.SFX_PODRIDO_VOMITO.get(), 1.1f, 0.9f + random.nextFloat() * 0.2f);
                }
                if (t >= 15 && t <= 24) chorro(t);
                if (t == 19) {
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    nube(getX() + d.x * 2.8, getY(), getZ() + d.z * 2.8, 1.6f, 100, AGUA_NEGRA, false);
                }
            }
            case PodridoAnims.GOLPE_FATUO -> {
                if (t == 10) empujar(0.35);
                if (t == 11) chispasFatuo();
            }
            case PodridoAnims.LANZAR_FATUO -> {
                if (t == 16) lanzarFatuo();
            }
            case PodridoAnims.APLASTAR -> {
                if (t == 17) empujar(0.7);
                if (t == 20) caer();
            }
            case PodridoAnims.ERUCTO -> {
                if (t == 19) eructo();
            }
            case PodridoAnims.ALZARSE -> {
                if (t == 2) chapoteo(true);
            }
            case PodridoAnims.LEVANTARSE -> {
                if (t == 2) chapoteo(false);
            }
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ el agua negra, el fuego y el gas

    /** El chorro del vomito: agua negra que sale de la boca hacia delante (un tick). */
    private void chorro(int t) {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double boca = getY() + getBbHeight() * 0.78;
        for (int k = 0; k < 6; k++) {
            double s = 0.6 + k * 0.75 + random.nextDouble() * 0.3;
            double caida = 0.06 * s * s;
            sl.sendParticles(AGUA_NEGRA, getX() + d.x * s, boca - caida, getZ() + d.z * s, 2, 0.12, 0.08, 0.12, 0.0);
        }
        if (t % 3 == 0) sl.sendParticles(ParticleTypes.FALLING_DRIPSTONE_WATER, getX() + d.x * 2.5, boca - 0.5, getZ() + d.z * 2.5, 4, 0.5, 0.2, 0.5, 0.0);
    }

    private void chispasFatuo() {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        sl.sendParticles(VERDE, getX() + d.x * 1.6, getY() + 1.2, getZ() + d.z * 1.6, 12, 0.4, 0.3, 0.4, 0.02);
        sl.sendParticles(ParticleTypes.GLOW, getX() + d.x * 1.6, getY() + 1.2, getZ() + d.z * 1.6, 4, 0.3, 0.2, 0.3, 0.02);
    }

    /** Lanza el fuego fatuo a su presa: en arco, a donde esta ahora (se esquiva moviendose). */
    private void lanzarFatuo() {
        if (level().isClientSide) return;
        ultimoFatuo = tickCount;
        FATUOS++;
        LivingEntity t = getTarget();
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        // la mano izquierda, alzada por encima del hombro
        double ox = getX() + d.x * 0.5 + d.z * 0.4, oz = getZ() + d.z * 0.5 - d.x * 0.4, oy = getY() + getBbHeight() * 0.95;
        FuegoFatuo f = new FuegoFatuo(level(), this);
        f.setPos(ox, oy, oz);
        double tx = t == null ? ox + d.x * 8 : t.getX(), tz = t == null ? oz + d.z * 8 : t.getZ(), ty = t == null ? getY() : t.getY() + t.getBbHeight() * 0.4;
        f.setDeltaMovement(FuegoFatuo.tiro(tx - ox, ty - oy, tz - oz));
        level().addFreshEntity(f);
        playSound(DarkCraftMod.SFX_PODRIDO_FATUO.get(), 1.2f, 0.9f + random.nextFloat() * 0.2f);
    }

    /** La panza da en el suelo: salpica barro y agua negra, deja un charco que frena. */
    private void caer() {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * 1.3, z = getZ() + d.z * 1.3;
        sl.sendParticles(BARRO, x, getY() + 0.2, z, 26, 0.9, 0.15, 0.9, 0.05);
        sl.sendParticles(ParticleTypes.SPLASH, x, getY() + 0.3, z, 30, 0.9, 0.2, 0.9, 0.2);
        playSound(DarkCraftMod.SFX_PODRIDO_APLASTAR.get(), 1.4f, 0.9f + random.nextFloat() * 0.15f);
        nube(x, getY(), z, 1.4f, 60, AGUA_NEGRA, false);
    }

    /** El eructo del hinchado: una nube de gas alrededor que envenena y debilita. */
    private void eructo() {
        if (!(level() instanceof ServerLevel sl)) return;
        ultimoEructo = tickCount;
        sl.sendParticles(GAS, getX(), getY() + 1.4, getZ(), 40, 1.4, 0.6, 1.4, 0.03);
        sl.sendParticles(ParticleTypes.SNEEZE, getX(), getY() + 1.6, getZ(), 18, 0.6, 0.4, 0.6, 0.05);
        playSound(DarkCraftMod.SFX_PODRIDO_GAS.get(), 1.3f, 1.0f + random.nextFloat() * 0.15f);
        nube(getX(), getY(), getZ(), R_ERUCTO, 70, GAS, true);
    }

    /** Al salir del agua (o del barro): salpica y chorrea. */
    private void chapoteo(boolean agua) {
        if (!(level() instanceof ServerLevel sl)) return;
        if (agua) {
            sl.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 0.6, getZ(), 40, 0.6, 0.3, 0.6, 0.25);
            sl.sendParticles(ParticleTypes.BUBBLE_POP, getX(), getY() + 0.5, getZ(), 12, 0.5, 0.2, 0.5, 0.05);
        } else sl.sendParticles(BARRO, getX(), getY() + 0.3, getZ(), 14, 0.4, 0.2, 0.4, 0.03);
        playSound(DarkCraftMod.SFX_PODRIDO_ALZARSE.get(), 1.0f, 0.9f + random.nextFloat() * 0.2f);
    }

    /**
     * Una nube que envenena (la de un charco de agua negra, la del gas o la del fuego fatuo): la nube de efectos de
     * Minecraft, que no dana a los podridos ({@link #canBeAffected}).
     */
    public static void nube(Level l, LivingEntity dueno, double x, double y, double z, float r, int ticks, ParticleOptions p, MobEffectInstance... efectos) {
        if (l.isClientSide) return;
        AreaEffectCloud n = new AreaEffectCloud(l, x, y, z);
        if (dueno != null) n.setOwner(dueno);
        n.setRadius(r);
        n.setRadiusOnUse(0f);
        n.setWaitTime(0);
        n.setDuration(ticks);
        n.setRadiusPerTick(-r * 0.4f / ticks);
        n.setParticle(p);
        for (MobEffectInstance e : efectos) n.addEffect(e);
        l.addFreshEntity(n);
        NUBES++;
    }

    private void nube(double x, double y, double z, float r, int ticks, ParticleOptions p, boolean gas) {
        if (gas) nube(level(), this, x, y, z, r, ticks, p, new MobEffectInstance(MobEffects.POISON, 70, 0), new MobEffectInstance(MobEffects.WEAKNESS, 120, 0));
        else nube(level(), this, x, y, z, r, ticks, p, new MobEffectInstance(MobEffects.POISON, 50, 0), new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
    }

    /** Su propio veneno no les hace nada (ni el de los demas podridos). */
    @Override
    public boolean canBeAffected(MobEffectInstance e) {
        MobEffect m = e.getEffect();
        if (m == MobEffects.POISON || m == MobEffects.WEAKNESS || m == MobEffects.MOVEMENT_SLOWDOWN || m == MobEffects.CONFUSION) return false;
        return super.canBeAffected(e);
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        switch (accion) {
            case PodridoAnims.MORDISCO -> le.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0), this);
            case PodridoAnims.VOMITO -> {
                le.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0), this);
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0), this);
            }
            case PodridoAnims.GOLPE_FATUO -> le.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 0), this);
            case PodridoAnims.APLASTAR -> le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1), this);
            default -> {
            }
        }
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case PodridoAnims.ZARPAZO, PodridoAnims.MORDISCO, PodridoAnims.GOLPE_FATUO -> DarkCraftMod.SFX_GOLPE_LIGERO.get();
            default -> null;
        };
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_PODRIDO_ALERTA.get();
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (variante()) {
            case FATUO -> 14 + random.nextInt(14);
            case HINCHADO -> 22 + random.nextInt(20);
            default -> 16 + random.nextInt(18);
        };
    }

    @Override
    protected double distanciaDeseada() {
        return variante() == FATUO ? 5.5 : variante() == HINCHADO ? 2.2 : 1.8;
    }

    @Override
    protected double prisa() {
        return variante() == HINCHADO ? 0.85 : variante() == FATUO ? 1.0 : 1.05;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide) {
            // en el agua contra un borde mas alto (las tablas de un embarcadero o de una pasarela, a un bloque del agua): sale
            // de un impulso hacia su presa (nadando no llega a subir un bloque entero)
            if (salirEspera > 0) salirEspera--;
            LivingEntity t = getTarget();
            if (isAlive() && postura() == DE_PIE && accion() == 0 && isInWater() && horizontalCollision && t != null && t.getY() > getY() + 0.4
                    && salirEspera <= 0) {
                double dx = t.getX() - getX(), dz = t.getZ() - getZ(), l = Math.sqrt(dx * dx + dz * dz);
                if (l > 0.01) {
                    setDeltaMovement(dx / l * 0.3, 0.62, dz / l * 0.3);
                    hasImpulse = true;
                    salirEspera = 12;
                    SALIDAS++;
                }
            }
            return;
        }
        if (!isAlive()) return;
        // chorrean; el del fuego lo lleva en la mano; al hinchado se le escapa el gas por la raja
        if (tickCount % 7 == 0 && postura() != TUMBADO)
            level().addParticle(ParticleTypes.DRIPPING_WATER, getX() + (random.nextDouble() - 0.5) * 0.6, getY() + 0.8 + random.nextDouble() * 1.0,
                    getZ() + (random.nextDouble() - 0.5) * 0.6, 0, 0, 0);
        if (variante() == FATUO && postura() == DE_PIE && accion() == 0 && tickCount % 3 == 0) {
            Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
            double x = getX() + d.x * 0.55 + d.z * 0.42, z = getZ() + d.z * 0.55 - d.x * 0.42;
            level().addParticle(VERDE, x, getY() + 1.25, z, 0, 0.02, 0);
            if (tickCount % 9 == 0) level().addParticle(ParticleTypes.GLOW, x, getY() + 1.3, z, 0, 0.01, 0);
        }
        if (variante() == HINCHADO && postura() == DE_PIE && tickCount % 11 == 0) {
            Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
            level().addParticle(GAS, getX() + d.x * 0.55, getY() + 1.0, getZ() + d.z * 0.55, d.x * 0.02, 0.015, d.z * 0.02);
        }
        // flotando: burbujas alrededor
        if (postura() == TUMBADO && isInWater() && tickCount % 13 == 0)
            level().addParticle(ParticleTypes.BUBBLE_POP, getX() + (random.nextDouble() - 0.5) * 1.2, getY() + 0.5, getZ() + (random.nextDouble() - 0.5) * 1.2, 0, 0.02, 0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return postura() == TUMBADO ? null : DarkCraftMod.SFX_PODRIDO_GEMIDO.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_PODRIDO_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_PODRIDO_MUERTE.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    public void die(DamageSource s) {
        super.die(s);
        if (!(level() instanceof ServerLevel sl)) return;
        if (variante() == HINCHADO) reventar(sl);
        else {
            sl.sendParticles(AGUA_NEGRA, getX(), getY() + 1.0, getZ(), 24, 0.4, 0.6, 0.4, 0.02);
            sl.sendParticles(ParticleTypes.BUBBLE_POP, getX(), getY() + 0.6, getZ(), 10, 0.4, 0.3, 0.4, 0.02);
        }
        net.minecraft.server.level.ServerPlayer quien = s.getEntity() instanceof net.minecraft.server.level.ServerPlayer p ? p
                : getKillCredit() instanceof net.minecraft.server.level.ServerPlayer p2 ? p2 : null;
        if (quien != null && !esEfimero()) {
            com.darkcraft.espejo.Espejo.bestia(quien, tipo());
            com.darkcraft.story.Logros.dar(quien, "podrido");
        }
    }

    /** El hinchado revienta al morir: un golpe a quien este pegado y una nube grande de gas. */
    private void reventar(ServerLevel sl) {
        REVENTONES++;
        sl.sendParticles(GAS, getX(), getY() + 1.2, getZ(), 70, 1.6, 0.8, 1.6, 0.05);
        sl.sendParticles(AGUA_NEGRA, getX(), getY() + 1.2, getZ(), 30, 0.8, 0.6, 0.8, 0.1);
        sl.sendParticles(ParticleTypes.SNEEZE, getX(), getY() + 1.4, getZ(), 24, 0.8, 0.5, 0.8, 0.08);
        sl.playSound(null, getX(), getY(), getZ(), DarkCraftMod.SFX_PODRIDO_GAS.get(), SoundSource.HOSTILE, 1.8f, 0.8f);
        for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, new AABB(blockPosition()).inflate(2.4, 1.5, 2.4),
                o -> o instanceof Player p && !p.isCreative() && !p.isSpectator() && o.isAlive())) {
            if (le.distanceToSqr(this) > 2.4 * 2.4) continue;
            le.hurt(damageSources().mobAttack(this), 4f);
            Vec3 v = le.position().subtract(position()).normalize();
            le.knockback(0.7, -v.x, -v.z);
        }
        nube(level(), this, getX(), getY(), getZ(), R_REVIENTA, 110, GAS, new MobEffectInstance(MobEffects.POISON, 90, 1),
                new MobEffectInstance(MobEffects.WEAKNESS, 140, 0));
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (jugador && random.nextFloat() < 0.06f + 0.01f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
    }
}
