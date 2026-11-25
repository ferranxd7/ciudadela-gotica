package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * El arpon que lanza el marinero ahogado: vuela como una flecha pesada, hace el dano de quien lo lanzo (el suyo x 1,15),
 * no se queda clavado en el jugador ni se puede recoger, y clavado en el suelo se deshace a los pocos segundos.
 */
public class Arpon extends AbstractArrow {
    private int clavado;

    public Arpon(EntityType<? extends Arpon> t, Level l) {
        super(t, l);
        pickup = Pickup.DISALLOWED;
    }

    public Arpon(Level l, LivingEntity de) {
        super(DarkCraftMod.ARPON.get(), de, l);
        pickup = Pickup.DISALLOWED;
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    protected void onHitEntity(EntityHitResult r) {
        Entity e = r.getEntity();
        Entity de = getOwner();
        if (e == de || (e instanceof Enemigo && de instanceof Enemigo)) return;
        float dano = de instanceof LivingEntity le && le.getAttribute(Attributes.ATTACK_DAMAGE) != null
                ? (float) le.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.15f : 7f;
        if (e.hurt(damageSources().mobProjectile(this, de instanceof LivingEntity le ? le : null), dano)) {
            if (e instanceof LivingEntity le) {
                Vec3 v = getDeltaMovement().multiply(1, 0, 1).normalize().scale(0.5);
                if (v.lengthSqr() > 0) le.push(v.x, 0.1, v.z);
            }
            playSound(SoundEvents.TRIDENT_HIT, 1.0f, 0.9f + random.nextFloat() * 0.2f);
        }
        discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && inGround && ++clavado > 100) discard();
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
