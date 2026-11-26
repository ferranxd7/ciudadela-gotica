package com.darkcraft.fortaleza;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * El remolino del portal de la Sombra (parte 5e, respuesta 53 del plan: "rojo y negro, que gira hacia el centro; un solo
 * dibujo grande, no por bloque"). Una entidad sin cuerpo en el pie del hueco del arco (en su centro en x y z): el cliente
 * dibuja la superficie entera del hueco con el dibujo de la espiral girando ({@code client/fortaleza/RemolinoRenderer}).
 * No se guarda: la pone {@link FortalezaEventos} mientras el portal esta abierto. Al abrirse crece desde el centro
 * ({@link #ABIERTO}, de 0 a 1 en dos segundos).
 */
public class Remolino extends Entity {
    private static final EntityDataAccessor<Float> ABIERTO = SynchedEntityData.defineId(Remolino.class, EntityDataSerializers.FLOAT);
    /** Lo abierto en el tick anterior (en el cliente, para suavizar). */
    public float abiertoO;

    public Remolino(EntityType<? extends Remolino> t, Level l) {
        super(t, l);
        noPhysics = true;
        abiertoO = 1f;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(ABIERTO, 1f);
    }

    public float abierto() {
        return entityData.get(ABIERTO);
    }

    public void abierto(float a) {
        entityData.set(ABIERTO, Mth.clamp(a, 0f, 1f));
    }

    /** Lo abierto entre dos ticks (para dibujarlo). */
    public float abiertoEn(float pt) {
        return Mth.lerp(pt, abiertoO, abierto());
    }

    @Override
    public void tick() {
        super.tick();
        abiertoO = abierto();
        if (!level().isClientSide && abierto() < 1f) abierto(abierto() + 0.025f);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag t) {
        abierto(t.contains("abierto") ? t.getFloat("abierto") : 1f);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag t) {
        t.putFloat("abierto", abierto());
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 320 * 320;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
