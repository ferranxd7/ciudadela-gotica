package com.darkcraft.mision;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * El farol de Brann (parte 7an, la mision de {@link Ylva}): el viejo farol de aceite del ballenero, de laton verdoso con costras de
 * sal y el cristal ahumado. La {@link #variante} 0 queda tumbado en la arena del Costillar, donde se deshizo Brann, apagado: se coge
 * con clic derecho o la E (cada jugador el suyo, {@link Ylva#tomar}). La 1 esta en el pretil del final del espigon del Puerto Varado,
 * encendido junto al farol de almas de Ylva ("los colgare juntos al final del muelle, el suyo y el mio"): solo lo ve quien se lo
 * devolvio. Lo que le toca ver a quien juega lo dice su cliente ({@link #sinFarol}, {@link #enMuelle}).
 */
public class FarolBrann extends Entity {
    private static final EntityDataAccessor<Integer> VARIANTE = SynchedEntityData.defineId(FarolBrann.class, EntityDataSerializers.INT);

    /**
     * En el cliente: si a quien juega ya no le toca ver el de la arena (lo lleva o acabo la mision) y si le toca ver el del muelle (se
     * lo devolvio a Ylva). Lo pone {@code client.mision.ClientMisiones}; en el servidor no se mira.
     */
    public static volatile boolean sinFarol, enMuelle;

    public FarolBrann(EntityType<? extends FarolBrann> t, Level l) {
        super(t, l);
        noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(VARIANTE, 0);
    }

    /** 0: en la arena del Costillar (se coge); 1: en el muelle, junto al de Ylva. */
    public int variante() {
        return entityData.get(VARIANTE);
    }

    public void variante(int n) {
        entityData.set(VARIANTE, n);
    }

    /** En el cliente: si quien juega lo ve. */
    public boolean visto() {
        return variante() == 1 ? enMuelle : !sinFarol;
    }

    @Override
    public InteractionResult interact(Player p, InteractionHand mano) {
        if (mano != InteractionHand.MAIN_HAND || variante() != 0) return InteractionResult.PASS;
        if (p instanceof ServerPlayer sp) Ylva.tomar(sp, this);
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    /** Golpearlo no hace nada. */
    @Override
    public boolean skipAttackInteraction(Entity quien) {
        return true;
    }

    @Override
    public boolean hurt(DamageSource src, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return variante() == 0 && !(level().isClientSide && sinFarol);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 64 * 64;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag t) {
        variante(t.getInt("variante"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag t) {
        t.putInt("variante", variante());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
