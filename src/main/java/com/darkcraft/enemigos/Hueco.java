package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * El aldeano hueco de Brumavela: lo que queda de la gente del pueblo, con la herramienta que le quedo en la mano. La
 * horca llega lejos (estocada larga, que se esquiva de lado, y barrido); la hoz es rapida y corta (un tajo, o dos
 * seguidos); la antorcha prende a quien toca. Poca vida y poco aguante: uno solo es facil; tres a la vez, no.
 */
public class Hueco extends Enemigo {
    public static final int HORCA = 0, HOZ = 1, ANTORCHA = 2;

    public Hueco(EntityType<? extends Hueco> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 24.0).add(Attributes.ATTACK_DAMAGE, 4.5)
                .add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.FOLLOW_RANGE, 14.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.1);
    }

    @Override
    public String tipo() {
        return switch (variante()) {
            case HOZ -> "hueco_hoz";
            case ANTORCHA -> "hueco_antorcha";
            default -> "hueco_horca";
        };
    }

    @Override
    protected int[] ticks() {
        return HuecoAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return HuecoAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return HuecoAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return HuecoAnims.MORIR;
    }

    @Override
    protected int accionDespertar(int postura) {
        return postura == TUMBADO ? HuecoAnims.ALZARSE : HuecoAnims.LEVANTARSE;
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        return switch (variante()) {
            case HOZ -> d < 2.1 ? (random.nextFloat() < 0.55f ? HuecoAnims.HOZ_TAJO : HuecoAnims.HOZ_DOBLE) : 0;
            case ANTORCHA -> d < 2.4 ? (random.nextFloat() < 0.55f ? HuecoAnims.ANTORCHA_GOLPE : HuecoAnims.ANTORCHA_BARRIDO) : 0;
            default -> d < 3.5 ? (d > 2.3 || random.nextFloat() < 0.6f ? HuecoAnims.HORCA_ESTOCADA : HuecoAnims.HORCA_BARRIDO) : 0;
        };
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case HuecoAnims.HORCA_ESTOCADA -> new Golpe(3.5f, 36f, 1.3f, 0.5f);
            case HuecoAnims.HORCA_BARRIDO -> new Golpe(2.9f, 150f, 1.0f, 0.7f);
            case HuecoAnims.HOZ_TAJO -> new Golpe(2.1f, 100f, 1.0f, 0f);
            case HuecoAnims.HOZ_DOBLE -> new Golpe(2.1f, 120f, 0.8f, 0f);
            case HuecoAnims.ANTORCHA_GOLPE -> new Golpe(2.5f, 60f, 1.25f, 0.3f);
            case HuecoAnims.ANTORCHA_BARRIDO -> new Golpe(2.5f, 150f, 0.85f, 0.2f);
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        // el paso con el que descarga cada golpe
        switch (accion) {
            case HuecoAnims.HORCA_ESTOCADA -> {
                if (t == 15) empujar(0.9);
            }
            case HuecoAnims.HORCA_BARRIDO -> {
                if (t == 17) empujar(0.35);
            }
            case HuecoAnims.HOZ_TAJO -> {
                if (t == 9) empujar(0.6);
            }
            case HuecoAnims.HOZ_DOBLE -> {
                if (t == 9 || t == 17) empujar(0.45);
            }
            case HuecoAnims.ANTORCHA_GOLPE -> {
                if (t == 14) empujar(0.55);
            }
            case HuecoAnims.ANTORCHA_BARRIDO -> {
                if (t == 13) empujar(0.35);
            }
            default -> {
            }
        }
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (accion == HuecoAnims.ANTORCHA_GOLPE) le.setSecondsOnFire(4);
        else if (accion == HuecoAnims.ANTORCHA_BARRIDO) le.setSecondsOnFire(3);
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return accion == HuecoAnims.HORCA_ESTOCADA ? DarkCraftMod.SFX_ESTOCADA.get()
                : accion == HuecoAnims.HORCA_BARRIDO || accion == HuecoAnims.ANTORCHA_GOLPE ? DarkCraftMod.SFX_GOLPE_PESADO.get() : DarkCraftMod.SFX_GOLPE_LIGERO.get();
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_HUECO_ALERTA.get();
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (variante()) {
            case HOZ -> 8 + random.nextInt(12);
            case ANTORCHA -> 14 + random.nextInt(16);
            default -> 18 + random.nextInt(18);
        };
    }

    @Override
    protected double distanciaDeseada() {
        return variante() == HORCA ? 2.6 : 1.6;
    }

    @Override
    protected double prisa() {
        return variante() == HOZ ? 1.25 : 1.0;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // la antorcha: su llama y su humo (donde cae mas o menos la mano)
        if (level().isClientSide && variante() == ANTORCHA && isAlive() && tickCount % 3 == 0) {
            Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
            if (accion() != 0 || postura() != DE_PIE) return;
            // (a su derecha: con el frente (-sen, cos), la derecha es (-cos, -sen))
            double x = getX() + d.x * 0.6 - d.z * 0.3, z = getZ() + d.z * 0.6 + d.x * 0.3;
            double y = getY() + 1.95 + guardia * 0.1;
            level().addParticle(random.nextInt(3) == 0 ? ParticleTypes.SMOKE : ParticleTypes.FLAME, x, y, z, 0, 0.01, 0);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return postura() == TUMBADO ? null : DarkCraftMod.SFX_HUECO_GEMIDO.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_HUECO_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_HUECO_MUERTE.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 220;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (jugador && random.nextFloat() < 0.04f + 0.01f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
    }
}
