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
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * El arquero esqueleto de Valdrenn (parte 4a): en las murallas, en lo alto de las torres y en los balcones. No baja a
 * buscar a nadie: desde su sitio se gira, tensa el arco (se ve venir: medio segundo largo con la cuerda en la barbilla),
 * suelta y saca otra flecha. Se esquiva de lado o rodando. A quien se le sube encima le da una patada o un golpe con el
 * arco. Poca vida: lo dificil es llegar hasta el.
 */
public class Arquero extends Enemigo {
    private int recarga;

    public Arquero(EntityType<? extends Arquero> t, Level l) {
        super(t, l);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 22.0).add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.ARMOR, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.FOLLOW_RANGE, 64.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    @Override
    public String tipo() {
        return "arquero";
    }

    @Override
    protected int[] ticks() {
        return ArqueroAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return ArqueroAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return ArqueroAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return ArqueroAnims.MORIR;
    }

    @Override
    protected int accionDespertar(int postura) {
        return postura == TUMBADO ? ArqueroAnims.ALZARSE : ArqueroAnims.LEVANTARSE;
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        boolean aSuAltura = Math.abs(presa.getY() - getY()) < 2.2;
        if (d < 2.2 && aSuAltura) return random.nextFloat() < 0.55f ? ArqueroAnims.PATADA : ArqueroAnims.ARCAZO;
        return d < 52 && recarga <= 0 ? ArqueroAnims.DISPARO : 0;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case ArqueroAnims.PATADA -> new Golpe(2.3f, 70f, 0.7f, 1.4f);
            case ArqueroAnims.ARCAZO -> new Golpe(2.5f, 110f, 0.9f, 0.5f);
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        if (accion == ArqueroAnims.DISPARO && t == ArqueroAnims.GOLPES[accion][0]) disparar();
        else if (accion == ArqueroAnims.DISPARO && t == 6) playSound(DarkCraftMod.SFX_ARCO_TENSAR.get(), 0.9f, 0.9f + random.nextFloat() * 0.15f);
        else if (accion == ArqueroAnims.PATADA && t == ArqueroAnims.GOLPES[accion][0] - 1) empujar(0.35);
    }

    /** Suelta la flecha hacia donde apunta el cuerpo (fijo desde tres ticks antes) y a la altura de su presa. */
    private void disparar() {
        LivingEntity t = getTarget();
        Arrow a = new Flecha(level(), this);
        a.setPos(getX(), getEyeY() - 0.15, getZ());
        a.setBaseDamage(2.0);
        a.pickup = AbstractArrow.Pickup.DISALLOWED;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double h = t == null ? 12 : Math.max(1, Math.sqrt(Math.pow(t.getX() - getX(), 2) + Math.pow(t.getZ() - getZ(), 2)));
        double dy = t == null ? 0 : t.getY(0.5) - a.getY();
        // con la caida de la flecha en cuenta (desde lo alto de una muralla, a 40 bloques, cae mucho)
        Vec3 v = tiro(h, dy, d, 2.1);
        a.shoot(v.x, v.y, v.z, 2.1f, 2.0f);
        level().addFreshEntity(a);
        playSound(DarkCraftMod.SFX_ARCO_SUELTA.get(), 1.0f, 0.9f + random.nextFloat() * 0.2f);
        recarga = 18 + random.nextInt(26);
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return accion == ArqueroAnims.DISPARO ? null : DarkCraftMod.SFX_GOLPE_LIGERO.get();
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return DarkCraftMod.SFX_ARQUERO_ALERTA.get();
    }

    @Override
    protected int pausaTras(int accion) {
        return accion == ArqueroAnims.DISPARO ? 8 + random.nextInt(14) : 12 + random.nextInt(14);
    }

    /** No se mueve de su sitio para buscar a nadie: se gira y dispara (solo da unos pasos hacia quien se le acerca). */
    @Override
    protected double distanciaDeseada() {
        return 99.0;
    }

    /** Dispara de arriba abajo (de una muralla, de lo alto de una torre) y de abajo arriba. */
    @Override
    protected double alturaMaxima() {
        return 56.0;
    }

    /** Se le escapa poco: lo ve todo desde arriba, pero no deja su sitio. */
    @Override
    protected double correa() {
        return 64.0;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide && recarga > 0) recarga--;
    }

    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        playSound(net.minecraft.sounds.SoundEvents.SKELETON_STEP, 0.25f, 0.9f + random.nextFloat() * 0.2f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return postura() == TUMBADO ? null : DarkCraftMod.SFX_ARQUERO_HUESOS.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_ARQUERO_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_ARQUERO_MUERTE.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 300;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        if (jugador && random.nextFloat() < 0.05f + 0.02f * saqueo) spawnAtLocation(DarkCraftMod.PIEDRA_FORJA_1.get());
    }
}
