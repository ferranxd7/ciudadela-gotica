package com.darkcraft.mision;

import com.darkcraft.DarkCraftMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * La campana del peregrino (parte 7ag): cuelga de su soga en la rama del Arbol de la Horca que se inclina sobre el camino, en la
 * Arboleda de los Ahorcados ({@code morvain.Arboleda#campana}). Cuando pasa el viento suena ({@code sfx.campana_viento}, cada 8-18 s
 * con alguien a 40 bloques: asi se la encuentra, como dice Oswin) y se mece; golpearla la hace sonar. Con clic derecho (o la E,
 * mirandola) se coge ({@link Oswin#tomar}): cada jugador coge la suya, y a quien ya la cogio (o ya acabo la mision) su cliente no se
 * la dibuja ni la deja mirar ({@link #oculta}).
 */
public class Campana extends Entity {
    /** El evento de entidad de sonar (el cliente la mece). */
    public static final byte EV_SUENA = 60;
    /** Lo que cuelga por debajo del cabo de la soga (lo alto de la entidad). */
    public static final float ALTO = 0.62f;

    /**
     * En el cliente: si a quien juega ya no le toca verla (la cogio o acabo la mision). Lo pone {@code client.mision.ClientMisiones};
     * en el servidor no se mira (en un mundo de un jugador comparten la JVM).
     */
    public static volatile boolean oculta;

    private int siguiente = 60;
    /** En el cliente: cuando sono por ultima vez (para mecerla) y lo fuerte que fue. */
    public int sonoEn = -1000;
    public float fuerza = 1f;

    public Campana(EntityType<? extends Campana> t, Level l) {
        super(t, l);
        noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (--siguiente > 0) return;
        siguiente = 160 + random.nextInt(200);
        if (level().getNearestPlayer(this, 40) != null) sonar(false);
    }

    /** Suena (el viento, o un golpe): el sonido para todos los de cerca y el vaiven en sus clientes. */
    public void sonar(boolean golpe) {
        level().playSound(null, getX(), getY() + ALTO * 0.5, getZ(), golpe ? DarkCraftMod.SFX_CAMPANA_TOQUE.get() : DarkCraftMod.SFX_CAMPANA_VIENTO.get(),
                SoundSource.AMBIENT, golpe ? 1.2f : 1.0f, 0.92f + random.nextFloat() * 0.16f);
        level().broadcastEntityEvent(this, golpe ? (byte) (EV_SUENA + 1) : EV_SUENA);
    }

    @Override
    public void handleEntityEvent(byte b) {
        if (b == EV_SUENA || b == EV_SUENA + 1) {
            sonoEn = tickCount;
            fuerza = b == EV_SUENA ? 1f : 2.2f;
        } else super.handleEntityEvent(b);
    }

    /** Lo que se mece ahora (grados, alrededor de su soga): un vaiven suave siempre y el que deja cada vez que suena. */
    public float vaiven(float pt) {
        float t = tickCount + pt;
        float base = Mth.sin(t * 0.045f + getId()) * 4f;
        float d = t - sonoEn;
        float golpe = d < 0 || d > 70 ? 0f : Mth.sin(d * 0.42f) * 16f * fuerza * (float) Math.exp(-d / 22f);
        return base + golpe;
    }

    @Override
    public InteractionResult interact(Player p, InteractionHand mano) {
        if (mano != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (p instanceof ServerPlayer sp) Oswin.tomar(sp, this);
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    /** Golpearla la hace sonar (no se rompe). */
    @Override
    public boolean skipAttackInteraction(Entity quien) {
        if (!level().isClientSide && quien instanceof Player) sonar(true);
        return true;
    }

    @Override
    public boolean hurt(DamageSource src, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return !(level().isClientSide && oculta);
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
