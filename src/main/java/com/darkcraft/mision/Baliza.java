package com.darkcraft.mision;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Una baliza de la niebla (parte 7ao, la mision de {@link Corwen}): un poste de roble negro clavado en una base de piedras, con un brazo
 * arriba que sale hacia la laguna y, colgado de su punta, un farol grande de hierro. Hay tres en la Aldea de los Pilotes
 * ({@link #variante}: 0 al final del muelle, 1 en el islote del oeste, 2 en el de la capilla ahogada). Se encienden con el yesquero de
 * Corwen (clic derecho o la E: {@link Corwen#encender}) y <b>cada jugador ve las suyas</b>: encendidas las que encendio el (o todas,
 * acabada la mision), apagadas las demas ({@link #encendida}: lo dice su cliente con {@link #ENCENDIDAS}).
 */
public class Baliza extends Entity {
    private static final EntityDataAccessor<Integer> VARIANTE = SynchedEntityData.defineId(Baliza.class, EntityDataSerializers.INT);
    /** Lo alto del farol (bloques sobre la base): ahi se mira para encenderla y salen las llamitas. */
    public static final double ALTO_FAROL = 2.05;
    /** Lo que sale el farol del poste, hacia donde mira la baliza (bloques). */
    public static final double BRAZO = 0.56;

    /**
     * En el cliente: las balizas que ve encendidas quien juega (bit 0 la del muelle, 1 la del islote del oeste, 2 la de la capilla); lo
     * pone {@code client.mision.ClientMisiones}. En el servidor no se mira.
     */
    public static volatile int ENCENDIDAS;

    public Baliza(EntityType<? extends Baliza> t, Level l) {
        super(t, l);
        noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(VARIANTE, 0);
    }

    /** Cual es: 0 el muelle, 1 el islote del oeste, 2 el islote de la capilla. */
    public int variante() {
        return entityData.get(VARIANTE);
    }

    public void variante(int n) {
        entityData.set(VARIANTE, Mth.clamp(n, 0, 2));
    }

    /** En el cliente: si quien juega la ve encendida. */
    public boolean encendida() {
        return (ENCENDIDAS & (1 << variante())) != 0;
    }

    /** Donde cuelga el farol (x, z): {@link #BRAZO} por delante del poste, hacia donde mira. */
    public double farolX() {
        return getX() - Mth.sin(getYRot() * Mth.DEG_TO_RAD) * BRAZO;
    }

    public double farolZ() {
        return getZ() + Mth.cos(getYRot() * Mth.DEG_TO_RAD) * BRAZO;
    }

    @Override
    public InteractionResult interact(Player p, InteractionHand mano) {
        if (mano != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (p instanceof ServerPlayer sp) Corwen.encender(sp, this);
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    /** Golpearla no hace nada. */
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
        return true;
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
        return d < 160 * 160;
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
