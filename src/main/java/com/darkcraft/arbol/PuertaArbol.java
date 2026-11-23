package com.darkcraft.arbol;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

/**
 * La gran puerta de la Sala del Arbol: dos hojas de madera negra con herrajes y un arbol de oro tallado.
 * Con la Llave del Arbol: la llave entra y gira, se encienden las runas, se descorren tres cerrojos y las
 * hojas se abren hacia la sala. La entidad esta en el centro del umbral (cara de la recepcion); el hueco
 * cerrado lo tapan bloques de barrera.
 */
public class PuertaArbol extends Entity {
    private static final EntityDataAccessor<Integer> ESTADO = SynchedEntityData.defineId(PuertaArbol.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> INICIO = SynchedEntityData.defineId(PuertaArbol.class, EntityDataSerializers.LONG);

    public static final int CERRADA = 0, ABRIENDO = 1, ABIERTA = 2;
    /** Durante la pelea con Ser Gavran: se cierra sola, se queda sellada y se vuelve a abrir al acabar. */
    public static final int CERRANDO = 3, SELLADA = 4, REABRIENDO = 5;
    /** Ticks que tardan las hojas en cerrarse (y la barrera) y en volver a abrirse. */
    public static final int T_CIERRE = 40, T_REAPERTURA = 80;
    /** Momentos de la secuencia (ticks desde que entra la llave). */
    public static final int T_RUNAS = 44, T_CERROJO = 52, T_HOJAS = 84, T_FIN_HOJAS = 160, T_BARRERA = 150, TOTAL = 170;
    /** Medidas: hueco de 7 x 10; cada hoja mide 3,5 x 10 x 0,375. */
    public static final float ANCHO = 7f, ALTO = 10f, HOJA = 3.5f, GROSOR = 0.375f;

    public PuertaArbol(EntityType<? extends PuertaArbol> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.blocksBuilding = false;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ESTADO, CERRADA);
        this.entityData.define(INICIO, 0L);
    }

    public int estado() {
        return this.entityData.get(ESTADO);
    }

    public void ponerAbierta() {
        this.entityData.set(ESTADO, ABIERTA);
    }

    /** Ticks desde que entro la llave (con fraccion); grande si ya estaba abierta, 0 si esta cerrada. */
    public float tiempo(float pt) {
        return switch (estado()) {
            case ABRIENDO, CERRANDO, REABRIENDO -> level().getGameTime() + pt - this.entityData.get(INICIO);
            case ABIERTA -> 100000f;
            default -> 0f;
        };
    }

    /** 0..1: cuanto estan abiertas las hojas (90 grados al final). */
    public float abertura(float pt) {
        int e = estado();
        if (e == SELLADA) return 0f;
        if (e == CERRANDO) {
            float u = Mth.clamp(tiempo(pt) / (T_CIERRE - 4f), 0f, 1f);
            // se cierra de golpe al final, como si alguien la empujara
            return 1f - u * u;
        }
        if (e == REABRIENDO) {
            float u = Mth.clamp((tiempo(pt) - 8f) / (T_REAPERTURA - 16f), 0f, 1f);
            return u * u * (3 - 2 * u);
        }
        float u = Mth.clamp((tiempo(pt) - T_HOJAS) / (T_FIN_HOJAS - T_HOJAS), 0f, 1f);
        // arranca pesada, coge impulso y frena al final
        return u * u * (3 - 2 * u);
    }

    /** 0..1: brillo de las runas y del arbol tallado. */
    public float brillo(float pt) {
        float t = tiempo(pt);
        int e = estado();
        if (e == CERRANDO || e == SELLADA) return 0.3f + 0.15f * Mth.sin((level().getGameTime() + pt) * 0.12f);
        if (e == ABIERTA || e == REABRIENDO) return 0.55f;
        float on = Mth.clamp((t - T_RUNAS) / 26f, 0f, 1f);
        float pulso = t > T_RUNAS + 26 ? 0.85f + 0.15f * Mth.sin(t * 0.25f) : 1f;
        float baja = 1f - 0.45f * Mth.clamp((t - T_FIN_HOJAS) / 20f, 0f, 1f);
        return on * pulso * baja;
    }

    /** 0..1: lo descorrido que esta el cerrojo i (0 arriba, 1 medio, 2 abajo). */
    public float cerrojo(int i, float pt) {
        int e = estado();
        if (e == CERRANDO || e == SELLADA) return 1f;
        if (e == REABRIENDO) return 1f;
        float t0 = T_CERROJO + i * 10;
        float u = Mth.clamp((tiempo(pt) - t0) / 7f, 0f, 1f);
        return u * u * (3 - 2 * u);
    }

    // ------------------------------------------------------------------ abrir con la llave

    @Override
    public InteractionResult interact(Player p, InteractionHand hand) {
        if (estado() != CERRADA) return InteractionResult.PASS;
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (!(p instanceof ServerPlayer sp)) return InteractionResult.PASS;
        ItemStack st = p.getItemInHand(hand);
        if (!st.is(DarkCraftMod.LLAVE_ARBOL.get())) {
            ItemStack otra = p.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (otra.is(DarkCraftMod.LLAVE_ARBOL.get())) st = otra;
        }
        if (!st.is(DarkCraftMod.LLAVE_ARBOL.get())) {
            sp.displayClientMessage(Component.translatable("darkcraft.puerta.cerrada"), true);
            level().playSound(null, getX(), getY() + 4, getZ(), SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 0.8f, 0.5f);
            return InteractionResult.CONSUME;
        }
        TorreArbol.abrir(this, sp, st);
        return InteractionResult.CONSUME;
    }

    /** Quien metio la llave (se lleva el logro aunque se haya ido). */
    private java.util.UUID abridor;

    /** Empieza la secuencia (la llama TorreArbol). */
    void empezar(ServerLevel sl, ServerPlayer quien) {
        this.abridor = quien.getUUID();
        this.entityData.set(INICIO, sl.getGameTime());
        this.entityData.set(ESTADO, ABRIENDO);
    }

    /** Ojo de la cerradura (centro de la puerta, en la cara de la recepcion). */
    public double[] cerradura() {
        return new double[]{getX(), getY() + 4.5, getZ() + GROSOR / 2 + 0.02};
    }

    // ------------------------------------------------------------------ la pelea con Ser Gavran

    /** La pelea empieza: las hojas se cierran solas y el hueco queda tapado. */
    public void cerrarLucha(ServerLevel sl) {
        int e = estado();
        if (e == CERRADA || e == SELLADA || e == CERRANDO) return;
        this.entityData.set(INICIO, sl.getGameTime());
        this.entityData.set(ESTADO, CERRANDO);
        sl.playSound(null, getX(), getY() + 5, getZ(), SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 1.6f, 0.5f);
    }

    /** La pelea acaba: se vuelve a abrir. */
    public void abrirLucha(ServerLevel sl) {
        int e = estado();
        if (e != SELLADA && e != CERRANDO) return;
        this.entityData.set(INICIO, sl.getGameTime());
        this.entityData.set(ESTADO, REABRIENDO);
        sl.playSound(null, getX(), getY() + 5, getZ(), com.darkcraft.DarkCraftMod.SFX_PUERTA_ABRE.get(), SoundSource.BLOCKS, 3.0f, 1.05f);
    }

    /** Tapa el hueco con barrera (sin tocar lo que no sea aire). */
    public void ponerBarrera(ServerLevel sl) {
        BlockPos b = blockPosition();
        for (int dx = -3; dx <= 3; dx++)
            for (int dy = 0; dy < 10; dy++) {
                BlockPos p = b.offset(dx, dy, 0);
                if (sl.getBlockState(p).isAir()) sl.setBlock(p, Blocks.BARRIER.defaultBlockState(), 3);
            }
    }

    private void tickLucha(ServerLevel sl, int e, int t) {
        if (e == CERRANDO) {
            if (t == T_CIERRE - 4) {
                sl.playSound(null, getX(), getY() + 5, getZ(), com.darkcraft.DarkCraftMod.SFX_PUERTA_CIERRA.get(), SoundSource.BLOCKS, 3.5f, 0.9f);
                sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY() + 1, getZ() - 0.6, 14, 2.6, 0.3, 0.3, 0.02);
                ponerBarrera(sl);
            }
            for (int i = 0; i < 3; i++)
                if (t == T_CIERRE + 2 + i * 6) {
                    double by = getY() + 7.5 - i * 2.5;
                    sl.playSound(null, getX(), by, getZ(), SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 1.4f, 0.55f);
                    sl.playSound(null, getX(), by, getZ(), SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.2f, 0.5f);
                }
            if (t >= T_CIERRE + 16) this.entityData.set(ESTADO, SELLADA);
        } else if (e == REABRIENDO) {
            if (t == T_REAPERTURA - 30) quitarBarrera(sl);
            if (t >= T_REAPERTURA) this.entityData.set(ESTADO, ABIERTA);
        }
    }

    @Override
    public void tick() {
        super.tick();
        int e0 = estado();
        if (e0 == CERRANDO || e0 == REABRIENDO) {
            if (!level().isClientSide) tickLucha((ServerLevel) level(), e0, (int) (level().getGameTime() - this.entityData.get(INICIO)));
            return;
        }
        if (estado() != ABRIENDO) return;
        int t = (int) (level().getGameTime() - this.entityData.get(INICIO));
        if (level().isClientSide) {
            particulasCliente(t);
            return;
        }
        ServerLevel sl = (ServerLevel) level();
        double[] k = cerradura();
        double x = getX(), y = getY(), z = getZ();
        switch (t) {
            case 22 -> sl.playSound(null, k[0], k[1], k[2], SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.2f, 0.55f);
            case 32 -> sl.playSound(null, k[0], k[1], k[2], SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.2f, 0.5f);
            case 40 -> sl.playSound(null, k[0], k[1], k[2], SoundEvents.LODESTONE_PLACE, SoundSource.BLOCKS, 1.5f, 0.6f);
            case T_RUNAS -> {
                sl.playSound(null, x, y + 5, z, com.darkcraft.DarkCraftMod.SFX_PUERTA_RUNAS.get(), SoundSource.BLOCKS, 3.0f, 1.0f);
                sl.playSound(null, x, y + 5, z, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, 0.7f);
            }
            case T_HOJAS -> {
                sl.playSound(null, x, y + 5, z, com.darkcraft.DarkCraftMod.SFX_PUERTA_ABRE.get(), SoundSource.BLOCKS, 3.5f, 1.0f);
            }
            case T_FIN_HOJAS -> sl.playSound(null, x, y + 5, z, com.darkcraft.DarkCraftMod.SFX_PUERTA_CIERRA.get(), SoundSource.BLOCKS, 2.0f, 1.15f);
            default -> {
            }
        }
        for (int i = 0; i < 3; i++)
            if (t == T_CERROJO + i * 10) {
                double by = y + 7.5 - i * 2.5;
                sl.playSound(null, x, by, z, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 1.5f, 0.6f);
                sl.playSound(null, x, by, z, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.2f, 0.5f);
                sl.sendParticles(ParticleTypes.SMOKE, x, by, z + 0.4, 8, 0.25, 0.1, 0.05, 0.01);
            }
        if (t > T_RUNAS && t < T_HOJAS && t % 2 == 0)
            sl.sendParticles(ParticleTypes.ENCHANT, x, y + 5, z + 2.5, 6, 2.2, 3.5, 0.6, 0.6);
        if (t == T_BARRERA) quitarBarrera(sl);
        if (t == TOTAL - 4) {
            // al acabar la cinematica: logro (con su fanfarria) para quien la abrio y para todos los que estan delante
            StringBuilder b = new StringBuilder();
            for (ServerPlayer p : sl.getServer().getPlayerList().getPlayers()) {
                double d = p.level() == sl ? Math.sqrt(p.distanceToSqr(this)) : -1;
                boolean es = p.getUUID().equals(abridor);
                b.append(' ').append(p.getGameProfile().getName()).append(es ? "(la abrio)" : "").append(" a ")
                        .append(d < 0 ? "otra dimension" : String.format("%.1f", d));
                if (es || (d >= 0 && d < 48)) com.darkcraft.story.Logros.dar(p, com.darkcraft.story.Logros.ARBOL);
            }
            DarkCraftMod.LOGGER.info("[DarkCraft] La puerta de la Sala del Arbol se ha abierto:{}", b);
        }
        if (t >= TOTAL) this.entityData.set(ESTADO, ABIERTA);
    }

    private void particulasCliente(int t) {
        if (t > T_HOJAS && t < T_FIN_HOJAS) {
            // por la rendija sale luz dorada
            float ab = abertura(0);
            for (int i = 0; i < 3; i++) {
                double yy = getY() + random.nextDouble() * ALTO;
                level().addParticle(new DustParticleOptions(new Vector3f(1f, 0.82f, 0.35f), 1.2f),
                        getX() + (random.nextDouble() - 0.5) * (0.4 + ab * 6), yy, getZ() + 0.3, 0, 0, 0.05);
            }
            if (t % 4 == 0)
                level().addParticle(ParticleTypes.END_ROD, getX() + (random.nextDouble() - 0.5) * 2, getY() + 1 + random.nextDouble() * 8,
                        getZ() - 0.5, 0, 0.01, 0.12);
        }
    }

    /** Quita las barreras del umbral (ya se puede pasar). */
    public void quitarBarrera(ServerLevel sl) {
        BlockPos b = blockPosition();
        for (int dx = -3; dx <= 3; dx++)
            for (int dy = 0; dy < 10; dy++) {
                BlockPos p = b.offset(dx, dy, 0);
                if (sl.getBlockState(p).is(Blocks.BARRIER)) sl.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            }
    }

    // ------------------------------------------------------------------ lo demas

    @Override
    public boolean isPickable() {
        return estado() == CERRADA;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource src, float amount) {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return new AABB(getX() - 4.5, getY() - 0.5, getZ() - 4.5, getX() + 4.5, getY() + 11, getZ() + 4.5);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 160 * 160;
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport) {
        setPos(x, y, z);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag t) {
        if (t.getBoolean("Abierta")) this.entityData.set(ESTADO, ABIERTA);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag t) {
        t.putBoolean("Abierta", estado() != CERRADA);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
