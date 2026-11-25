package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * El espectro de la corte de Valdrenn (parte 4b): los nobles que no se fueron del castillo. Flotan (no caen ni se hacen
 * dano al caer; no hacen ruido al moverse), se dibujan medio transparentes y **se desvanecen** para aparecer al lado (o a
 * la espalda) de quien persiguen. El cortesano ({@link #ESTOQUE}) tira estocadas que llegan lejos y un tajo doble; la dama
 * ({@link #GARRAS}) da zarpazos que dejan frio (lentitud) y **se lamenta**: un grito alrededor (se ve venir) que frena y
 * quita fuerzas. Si le dan, a veces se va de un salto a otro sitio.
 */
public class Espectro extends Enemigo {
    public static final int ESTOQUE = 0, GARRAS = 1;
    private int lamentoEspera = 40, desvaneceEspera = 60;

    public Espectro(EntityType<? extends Espectro> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.ARMOR, 0.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.FOLLOW_RANGE, 24.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    public String tipo() {
        return variante() == GARRAS ? "espectro_dama" : "espectro_cortesano";
    }

    @Override
    protected int[] ticks() {
        return EspectroAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return EspectroAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return EspectroAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return EspectroAnims.MORIR;
    }

    /** Acurrucado (emboscada): se levanta; velando: se alza. */
    @Override
    protected int accionDespertar(int postura) {
        return postura == TUMBADO ? EspectroAnims.LEVANTARSE : EspectroAnims.ALZARSE;
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (Math.abs(presa.getY() - getY()) > 2.4) return 0;
        // lejos (o tras un rato sin llegar): se desvanece y aparece al lado
        if (d > 6.5 && desvaneceEspera <= 0 && random.nextFloat() < 0.18f) return EspectroAnims.DESVANECERSE;
        if (variante() == GARRAS) {
            if (d <= 4.6 && lamentoEspera <= 0 && random.nextFloat() < 0.3f) return EspectroAnims.LAMENTO;
            if (d <= 2.5) return EspectroAnims.ZARPAZO;
            return 0;
        }
        if (d <= 2.4) return random.nextFloat() < 0.55f ? EspectroAnims.TAJO_DOBLE : EspectroAnims.ESTOCADA;
        if (d <= 4.4 && random.nextFloat() < 0.5f) return EspectroAnims.ESTOCADA;
        return 0;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case EspectroAnims.ESTOCADA -> new Golpe(4.4f, 30f, 1.25f, 0.4f);
            case EspectroAnims.TAJO_DOBLE -> new Golpe(2.8f, 130f, 0.85f, 0.3f);
            case EspectroAnims.ZARPAZO -> new Golpe(2.6f, 100f, 0.9f, 0.35f);
            default -> null;
        };
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        super.alGolpear(le, accion);
        // el toque de un muerto deja frio
        if (accion == EspectroAnims.ZARPAZO) le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1), this);
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        if (accion == EspectroAnims.ESTOCADA && t == EspectroAnims.GOLPES[accion][0] - 1) empujar(0.55);
        else if (accion == EspectroAnims.LAMENTO) {
            if (t == 6) playSound(DarkCraftMod.SFX_ESPECTRO_LAMENTO.get(), 2.0f, 0.95f + random.nextFloat() * 0.1f);
            if (t == EspectroAnims.GOLPES[accion][0]) lamento();
        } else if (accion == EspectroAnims.DESVANECERSE) {
            if (t == 3) playSound(DarkCraftMod.SFX_ESPECTRO_DESVANECE.get(), 1.0f, 1.0f);
            if (t == 12) aparecerJunto();
        }
    }

    /** El grito: a todos los que esten a menos de 5 bloques, un poco de dano, lentitud y debilidad (los empuja atras). */
    private void lamento() {
        lamentoEspera = 100 + random.nextInt(80);
        if (level() instanceof ServerLevel sl)
            sl.sendParticles(ParticleTypes.SCULK_SOUL, getX(), getY() + 1.4, getZ(), 26, 2.0, 0.6, 2.0, 0.02);
        for (Player p : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(5.0, 2.0, 5.0), p -> objetivoValido(p) && p.distanceTo(this) <= 5.0)) {
            if (!hasLineOfSight(p)) continue;
            float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6f;
            if (p.hurt(damageSources().indirectMagic(this, this), dano)) {
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1), this);
                p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0), this);
                Vec3 d = p.position().subtract(position()).normalize();
                p.knockback(0.6, -d.x, -d.z);
            }
        }
    }

    /** A 2-3,5 bloques de su presa, a un lado o detras (donde quepa y haya suelo); si no hay sitio, se queda. */
    private void aparecerJunto() {
        LivingEntity p = getTarget();
        desvaneceEspera = 140 + random.nextInt(100);
        if (p == null || !(level() instanceof ServerLevel sl)) return;
        Vec3 mira = Vec3.directionFromRotation(0, p.getYRot());
        for (int k = 0; k < 10; k++) {
            // detras o de lado (nunca justo delante de quien mira)
            double ang = Math.toRadians(90 + random.nextDouble() * 180) + Math.atan2(mira.z, mira.x);
            double r = 2.0 + random.nextDouble() * 1.5;
            double x = p.getX() + Math.cos(ang) * r, z = p.getZ() + Math.sin(ang) * r;
            BlockPos b = BlockPos.containing(x, p.getY() + 0.5, z);
            double y = Puestos.suelo(sl, b.getX(), b.getY(), b.getZ());
            if (Math.abs(y - p.getY()) > 2.0) continue;
            Vec3 antes = position();
            setPos(x, y, z);
            if (!sl.noCollision(this) || sl.noCollision(this, getBoundingBox().move(0, -1.2, 0))) {
                setPos(antes.x, antes.y, antes.z);
                continue;
            }
            sl.sendParticles(ParticleTypes.SOUL, antes.x, antes.y + 1.0, antes.z, 14, 0.3, 0.6, 0.3, 0.02);
            teleportTo(x, y, z);
            girarHacia(p, 180f);
            sl.sendParticles(ParticleTypes.SOUL, x, y + 1.0, z, 14, 0.3, 0.6, 0.3, 0.02);
            playSound(DarkCraftMod.SFX_ESPECTRO_DESVANECE.get(), 1.0f, 1.25f);
            return;
        }
    }

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        boolean ok = super.hurt(s, cuanto);
        // a veces, tras un golpe, se va (si no esta haciendo otra cosa)
        if (ok && !level().isClientSide && isAlive() && accion() == 0 && desvaneceEspera <= 0 && getTarget() != null && random.nextFloat() < 0.2f) {
            empezar(EspectroAnims.DESVANECERSE);
        }
        return ok;
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case EspectroAnims.ESTOCADA -> DarkCraftMod.SFX_ESTOCADA.get();
            case EspectroAnims.TAJO_DOBLE -> DarkCraftMod.SFX_GOLPE_LIGERO.get();
            case EspectroAnims.ZARPAZO -> DarkCraftMod.SFX_ESPECTRO_ZARPAZO.get();
            default -> null;
        };
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_ESPECTRO_ALERTA.get();
    }

    @Override
    protected int pausaTras(int accion) {
        return accion == EspectroAnims.LAMENTO ? 16 + random.nextInt(12) : 8 + random.nextInt(14);
    }

    @Override
    protected double distanciaDeseada() {
        return variante() == GARRAS ? 1.8 : 2.4;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            // un rastro frio, de vez en cuando
            if (random.nextInt(14) == 0 && !muriendo())
                level().addParticle(ParticleTypes.SOUL, getRandomX(0.5), getY() + 0.2 + random.nextDouble() * 1.4, getRandomZ(0.5), 0, 0.01, 0);
            return;
        }
        if (lamentoEspera > 0) lamentoEspera--;
        if (desvaneceEspera > 0) desvaneceEspera--;
    }

    /** Flotan: ni caen con fuerza ni se hacen dano al caer, ni hacen ruido al moverse. */
    @Override
    public boolean causeFallDamage(float distancia, float multiplicador, DamageSource s) {
        return false;
    }

    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return postura() == TUMBADO ? null : DarkCraftMod.SFX_ESPECTRO_SUSURRO.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_ESPECTRO_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_ESPECTRO_MUERTE.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 260;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (jugador && random.nextFloat() < 0.06f + 0.02f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
    }
}
