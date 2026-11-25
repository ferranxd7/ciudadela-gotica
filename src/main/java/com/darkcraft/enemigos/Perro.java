package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * El perro de hueso de Valdrenn (parte 4b): los lebreles de la guardia. Van en jauria (dos o tres por puesto, uno al lado
 * del otro), corren mas que nadie y muerden deprisa; desde 3-7 bloques **saltan** (se agachan antes: se ve venir; se
 * esquiva rodando de lado). El primero que ve a alguien **aulla** y los de su jauria que esten cerca acuden. Poca vida:
 * lo malo es que llegan todos a la vez.
 */
public class Perro extends Enemigo {
    private int saltoEspera;
    /** El ultimo tick (del servidor) en que aullo un perro: los demas no repiten el aullido en un rato. */
    private static int ultimoAullido = -1000;

    public Perro(EntityType<? extends Perro> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 16.0).add(Attributes.ATTACK_DAMAGE, 3.5).add(Attributes.ARMOR, 0.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34).add(Attributes.FOLLOW_RANGE, 24.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    @Override
    public String tipo() {
        return "perro";
    }

    @Override
    protected int[] ticks() {
        return PerroAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return PerroAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return PerroAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return PerroAnims.MORIR;
    }

    /** Tirado entre los huesos (emboscada): se levanta; echado (dormido): se alza. */
    @Override
    protected int accionDespertar(int postura) {
        return postura == TUMBADO ? PerroAnims.LEVANTARSE : PerroAnims.ALZARSE;
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (Math.abs(presa.getY() - getY()) > 1.6) return 0;
        if (d <= 2.1) return PerroAnims.MORDISCO;
        if (d >= 3.2 && d <= 7.0 && saltoEspera <= 0 && random.nextFloat() < 0.35f) return PerroAnims.SALTO;
        return 0;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case PerroAnims.MORDISCO -> new Golpe(2.2f, 70f, 1.0f, 0.25f);
            case PerroAnims.SALTO -> new Golpe(2.4f, 100f, 1.35f, 0.7f);
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        if (accion == PerroAnims.MORDISCO) {
            if (t == PerroAnims.GOLPES[accion][0] - 2) empujar(0.32);
            if (t == PerroAnims.GOLPES[accion][0]) playSound(DarkCraftMod.SFX_PERRO_MORDISCO.get(), 1.0f, 0.9f + random.nextFloat() * 0.2f);
        } else if (accion == PerroAnims.SALTO) {
            // se agacha (0-10), salta hacia donde esta su presa (11) y vuela hasta caer encima (16)
            if (t == 11) {
                LivingEntity p = getTarget();
                Vec3 d = Vec3.directionFromRotation(0, getYRot());
                double lejos = p == null ? 4.0 : Math.min(6.5, Math.max(2.5, Math.sqrt(distanceToSqr(p)) - 1.2));
                double v = 0.16 + lejos * 0.12;
                setDeltaMovement(d.x * v, 0.42, d.z * v);
                hasImpulse = true;
                playSound(DarkCraftMod.SFX_PERRO_LADRIDO.get(), 0.7f, 1.25f);
                saltoEspera = 60 + random.nextInt(60);
            } else if (t > 11 && t <= 16 && !onGround()) {
                // el paso de cada tick de la accion frena al que ataca: en el aire no
                Vec3 d = Vec3.directionFromRotation(0, getYRot());
                double h = Math.sqrt(getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().z * getDeltaMovement().z) / 0.6;
                setDeltaMovement(d.x * h, getDeltaMovement().y, d.z * h);
            } else if (t == PerroAnims.GOLPES[accion][0]) {
                playSound(DarkCraftMod.SFX_PERRO_MORDISCO.get(), 1.1f, 0.8f + random.nextFloat() * 0.15f);
            }
        } else if (accion == PerroAnims.AULLIDO && t == 6) {
            playSound(DarkCraftMod.SFX_PERRO_AULLIDO.get(), 2.2f, 0.9f + random.nextFloat() * 0.15f);
            // los de su jauria (y los perros que lo oigan) acuden
            LivingEntity p = getTarget();
            if (p != null)
                for (Perro o : level().getEntitiesOfClass(Perro.class, getBoundingBox().inflate(20, 6, 20), o -> o != this && o.isAlive() && o.getTarget() == null))
                    if (o.postura() == DE_PIE || o.distanceTo(this) < 12) o.setTarget(p);
        }
    }

    /** El primero que ve a alguien aulla (si ningun otro aullo hace poco), y luego ataca. */
    @Override
    public void setTarget(LivingEntity t) {
        boolean nuevo = t != null && getTarget() == null;
        super.setTarget(t);
        if (nuevo && !level().isClientSide && isAlive() && postura() == DE_PIE && accion() == 0 && getServer() != null
                && getServer().getTickCount() - ultimoAullido > 120) {
            ultimoAullido = getServer().getTickCount();
            empezar(PerroAnims.AULLIDO);
        }
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return null;
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_PERRO_LADRIDO.get();
    }

    @Override
    protected int pausaTras(int accion) {
        return accion == PerroAnims.SALTO ? 10 + random.nextInt(10) : 4 + random.nextInt(10);
    }

    @Override
    protected double distanciaDeseada() {
        return 1.5;
    }

    @Override
    protected double prisa() {
        return 1.25;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide && saltoEspera > 0) saltoEspera--;
    }

    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        playSound(net.minecraft.sounds.SoundEvents.SKELETON_STEP, 0.18f, 1.3f + random.nextFloat() * 0.2f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return postura() == TUMBADO ? null : DarkCraftMod.SFX_PERRO_GRUNIDO.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_PERRO_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_PERRO_MUERTE.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    /** Los perros no se hacen dano al caer de un salto. */
    @Override
    public boolean causeFallDamage(float distancia, float multiplicador, DamageSource s) {
        return distancia > 6 && super.causeFallDamage(distancia - 4, multiplicador, s);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (jugador && random.nextFloat() < 0.03f + 0.02f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
    }
}
