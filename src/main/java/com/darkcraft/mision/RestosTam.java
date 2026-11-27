package com.darkcraft.mision;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Lo que queda de Tam (parte 7ah, la mision de {@link Hedda}): en la sala de la guardia de la Puerta Oeste de la Capital, sentado en
 * el suelo de espaldas al torno del rastrillo, la cabeza caida, la sobreveste de la guardia quemada, la bufanda roja al cuello y su
 * lanza atravesada en la rueda del torno (nadie iba a subir la reja). Con clic derecho (o la E, mirandole): la bufanda o, tras decirle
 * la verdad a Hedda, la lanza ({@link Hedda#tomar}). Lo que ya cogio quien juega, su cliente no lo dibuja ({@link #sinBufanda},
 * {@link #sinLanza}); si ya no queda nada que coger, no deja mirarle.
 */
public class RestosTam extends Entity {
    /**
     * En el cliente: si a quien juega ya no le toca ver la bufanda (la lleva o acabo la mision) o la lanza (ya la saco), y si ya no hay
     * nada que coger. Lo pone {@code client.mision.ClientMisiones}; en el servidor no se mira.
     */
    public static volatile boolean sinBufanda, sinLanza, nada;

    public RestosTam(EntityType<? extends RestosTam> t, Level l) {
        super(t, l);
        noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public InteractionResult interact(Player p, InteractionHand mano) {
        if (mano != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (p instanceof ServerPlayer sp) Hedda.tomar(sp, this);
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    /** Golpearle no hace nada. */
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
        return !(level().isClientSide && nada);
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
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag t) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
