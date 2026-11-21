package com.ciudadela.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;

/** Camara invisible que solo existe en el cliente: la cinematica la mueve por los planos. */
public final class CineCamera extends Entity {
    public static final float EYE = 1.62f;

    public CineCamera(Level level) {
        super(EntityType.MARKER, level);
        this.noPhysics = true;
    }

    @Override
    protected float getEyeHeight(Pose pose, EntityDimensions dim) {
        return EYE;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
