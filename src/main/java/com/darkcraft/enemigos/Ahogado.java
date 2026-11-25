package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * El marinero ahogado de Brumavela: la tripulacion de La Garza Gris. Lento y pesado, aguanta y pega mas que un hueco.
 * El del sable descarga tajos anchos que se ven venir de lejos (y una tanda de tres avanzando); el del arpon lo lanza
 * desde lejos —apunta un momento antes de soltarlo: quien se mueve de lado lo esquiva— y pincha a quien se le acerca.
 */
public class Ahogado extends Enemigo {
    public static final int SABLE = 0, ARPON = 1;

    private int sinLanzar;

    public Ahogado(EntityType<? extends Ahogado> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 34.0).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.ARMOR, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.21).add(Attributes.FOLLOW_RANGE, 16.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
    }

    @Override
    public String tipo() {
        return variante() == ARPON ? "ahogado_arpon" : "ahogado_sable";
    }

    @Override
    protected int[] ticks() {
        return AhogadoAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return AhogadoAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return AhogadoAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return AhogadoAnims.MORIR;
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        boolean aSuAltura = Math.abs(presa.getY() - getY()) < 2.6;
        if (variante() == ARPON) {
            if (d < 3.2 && aSuAltura) return AhogadoAnims.ARPON_PINCHAR;
            return d < 16 && sinLanzar <= 0 ? AhogadoAnims.ARPON_LANZAR : 0;
        }
        return d < 2.7 && aSuAltura ? (random.nextFloat() < 0.6f ? AhogadoAnims.SABLE_TAJO : AhogadoAnims.SABLE_COMBO) : 0;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case AhogadoAnims.SABLE_TAJO -> new Golpe(2.8f, 70f, 1.35f, 0.5f);
            case AhogadoAnims.SABLE_COMBO -> n < 2 ? new Golpe(2.7f, 140f, 0.85f, 0.2f) : new Golpe(2.8f, 70f, 1.2f, 0.6f);
            case AhogadoAnims.ARPON_PINCHAR -> new Golpe(3.5f, 36f, 1.1f, 0.4f);
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        switch (accion) {
            case AhogadoAnims.SABLE_TAJO -> {
                if (t == 15) empujar(0.7);
            }
            case AhogadoAnims.SABLE_COMBO -> {
                if (t == 13 || t == 24) empujar(0.5);
                else if (t == 37) empujar(0.75);
            }
            case AhogadoAnims.ARPON_PINCHAR -> {
                if (t == 17) empujar(0.85);
            }
            case AhogadoAnims.ARPON_LANZAR -> {
                if (t == AhogadoAnims.GOLPES[AhogadoAnims.ARPON_LANZAR][0]) lanzar();
            }
            default -> {
            }
        }
    }

    /** Suelta el arpon hacia donde apuntaba (el cuerpo ya no gira desde tres ticks antes: se esquiva de lado). */
    private void lanzar() {
        LivingEntity t = getTarget();
        Arpon a = new Arpon(level(), this);
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double lejos = t == null ? 8 : Math.max(2, Math.sqrt(distanceToSqr(t)));
        double sube = t == null ? 0 : (t.getY(0.5) - a.getY()) / lejos;
        a.shoot(d.x, Math.max(-0.6, Math.min(0.6, sube)) + lejos * 0.011, d.z, 1.7f, 1.5f);
        level().addFreshEntity(a);
        playSound(SoundEvents.TRIDENT_THROW, 1.0f, 0.8f + random.nextFloat() * 0.15f);
        sinLanzar = 50 + random.nextInt(40);
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return accion == AhogadoAnims.ARPON_PINCHAR ? DarkCraftMod.SFX_ESTOCADA.get()
                : accion == AhogadoAnims.ARPON_LANZAR ? null : DarkCraftMod.SFX_GOLPE_PESADO.get();
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_AHOGADO_ALERTA.get();
    }

    @Override
    protected int pausaTras(int accion) {
        return variante() == ARPON ? 22 + random.nextInt(20) : 20 + random.nextInt(22);
    }

    /** El arponero lanza cuesta abajo y cuesta arriba (desde lo alto de la Cuesta, a quien sube). */
    @Override
    protected double alturaMaxima() {
        return variante() == ARPON ? 9.0 : 2.6;
    }

    @Override
    protected double distanciaDeseada() {
        // el del arpon se queda lejos mientras tenga que lanzar; si no, se acerca a pinchar
        return variante() == ARPON ? (sinLanzar <= 0 ? 9.0 : 2.6) : 1.9;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide) {
            if (sinLanzar > 0) sinLanzar--;
        } else if (isAlive() && tickCount % 7 == 0) {
            // chorrea
            level().addParticle(ParticleTypes.FALLING_WATER, getRandomX(0.4), getY() + 0.6 + random.nextDouble() * 1.1, getRandomZ(0.4), 0, 0, 0);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return DarkCraftMod.SFX_AHOGADO_GORGOTEO.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_AHOGADO_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_AHOGADO_MUERTE.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (jugador && random.nextFloat() < 0.07f + 0.02f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
    }
}
