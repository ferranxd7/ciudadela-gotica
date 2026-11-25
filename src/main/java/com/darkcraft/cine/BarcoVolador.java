package com.darkcraft.cine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * La Garza Gris volando (parte 3d): en las cinematicas del viaje el barco de bloques se quita del muelle y vuela esta
 * entidad, que <b>solo existe en el cliente</b> de quien ve la cinematica (el servidor nunca la crea). La mueve
 * {@code client/Guion} cada tick con {@link Viaje#barco}; su sitio es el pie del palo, en la cubierta. La dibuja
 * {@code client/cine/BarcoVoladorRenderer} con los mismos bloques que el barco de verdad ({@code pueblo/Garza}).
 * <p>
 * Es una entidad (y no algo pintado a mano en un evento de dibujo) para que salga igual con shaders.
 */
public class BarcoVolador extends Entity {
    /** Cabeceo (positivo = proa arriba) y balanceo, en grados; los de este tick y los del anterior. */
    public float cabeceo, cabeceoO, balanceo, balanceoO;

    public BarcoVolador(EntityType<? extends BarcoVolador> tipo, Level nivel) {
        super(tipo, nivel);
        this.noPhysics = true;
        this.noCulling = true;
        this.blocksBuilding = false;
    }

    /** La pone donde toca este tick (lo de antes pasa a ser "el tick anterior", para dibujarla entre los dos). */
    public void mover(double x, double y, double z, float giro, float cabeceoAhora, float balanceoAhora, boolean deGolpe) {
        setOldPosAndRot();
        cabeceoO = cabeceo;
        balanceoO = balanceo;
        setPos(x, y, z);
        float g = deGolpe ? giro : getYRot() + net.minecraft.util.Mth.wrapDegrees(giro - getYRot());
        setYRot(g);
        cabeceo = cabeceoAhora;
        balanceo = balanceoAhora;
        if (deGolpe) {
            setOldPosAndRot();
            cabeceoO = cabeceo;
            balanceoO = balanceo;
        }
    }

    @Override
    public void tick() {
        // no hace nada sola: la mueve la cinematica
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return new AABB(getX() - 30, getY() - 10, getZ() - 30, getX() + 30, getY() + 34, getZ() + 30);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return true;
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
