package com.darkcraft.arbol;

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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * El ascensor magico de la torre del arbol: un disco de piedra negra con runas de oro que sube por el centro
 * de la torre (por dentro de un tubo de cristal) hasta la recepcion del arbol, y baja. Quien se queda encima
 * dos segundos viaja montado en el (no se puede caer ni bajarse a medio camino); desde el otro extremo se le
 * llama igual, esperando dos segundos encima de la runa.
 * <p>
 * El de la torre de la Fortaleza de Hueso (parte 5a) es el mismo, de otro tipo: una placa cuadrada de hueso con runas
 * rojas y una calavera, que sube por el tubo de barrotes del centro de la torre hasta la terraza del portal.
 */
public class Ascensor extends Entity {
    private static final EntityDataAccessor<Integer> ESTADO = SynchedEntityData.defineId(Ascensor.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> INICIO = SynchedEntityData.defineId(Ascensor.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> Y0 = SynchedEntityData.defineId(Ascensor.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> Y1 = SynchedEntityData.defineId(Ascensor.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CARGA = SynchedEntityData.defineId(Ascensor.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TIPO = SynchedEntityData.defineId(Ascensor.class, EntityDataSerializers.INT);

    /** Los tipos: el disco de oro de la torre del arbol y la placa de hueso de la fortaleza. */
    public static final int ARBOL = 0, FORTALEZA = 1;

    public static final int ABAJO = 0, ARRIBA = 1, SUBE = 2, BAJA = 3;
    /** Duracion del viaje (ticks) y lo que hay que esperar encima para que arranque. */
    public static final int VIAJE = 300, ESPERA = 40;
    public static final double RADIO = 2.5;

    /** Quien acaba de bajarse: no vuelve a arrancar hasta que se aparte del disco. */
    private final Set<UUID> recien = new HashSet<>();
    private int llamada;
    /** Ticks que le quedan sin responder (hay pelea arriba). */
    private int bloqueado;
    /** Solo el propio ascensor puede soltar a sus pasajeros (al llegar). */
    private boolean soltando;

    public Ascensor(EntityType<? extends Ascensor> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.blocksBuilding = false;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ESTADO, ABAJO);
        this.entityData.define(INICIO, 0L);
        this.entityData.define(Y0, 0);
        this.entityData.define(Y1, 0);
        this.entityData.define(CARGA, 0);
        this.entityData.define(TIPO, ARBOL);
    }

    public void setTipo(int t) {
        this.entityData.set(TIPO, t);
    }

    public int tipo() {
        return this.entityData.get(TIPO);
    }

    /** La placa de hueso de la fortaleza (y no el disco de la torre del arbol). */
    public boolean deHueso() {
        return tipo() == FORTALEZA;
    }

    /** Recorrido: de y0 (suelo de abajo) a y1 (suelo de la recepcion), parado en un extremo. */
    public void preparar(int y0, int y1, boolean arriba) {
        this.entityData.set(Y0, y0);
        this.entityData.set(Y1, y1);
        this.entityData.set(ESTADO, arriba ? ARRIBA : ABAJO);
        setPos(getX(), arriba ? y1 : y0, getZ());
    }

    public int estado() {
        return this.entityData.get(ESTADO);
    }

    public boolean enMarcha() {
        int e = estado();
        return e == SUBE || e == BAJA;
    }

    public int y0() {
        return this.entityData.get(Y0);
    }

    public int y1() {
        return this.entityData.get(Y1);
    }

    /** 0..1: cuanto lleva esperando alguien encima (las runas brillan mas). */
    public float carga() {
        return this.entityData.get(CARGA) / (float) ESPERA;
    }

    /** Ticks de viaje (con fraccion), o -1 si esta parado. */
    public double viaje(float pt) {
        if (!enMarcha()) return -1;
        return level().getGameTime() + pt - this.entityData.get(INICIO);
    }

    /** Altura del disco (se pisa en ella) en el tick de juego t. */
    public double alturaEn(double t) {
        int e = estado();
        double a = y0(), b = y1();
        if (e == ABAJO) return a;
        if (e == ARRIBA) return b;
        double u = Mth.clamp((t - this.entityData.get(INICIO)) / VIAJE, 0, 1);
        double s = u * u * u * (u * (u * 6 - 15) + 10);
        return e == SUBE ? a + (b - a) * s : b + (a - b) * s;
    }

    @Override
    public void tick() {
        super.tick();
        long now = level().getGameTime();
        setPos(getX(), alturaEn(now), getZ());
        if (level().isClientSide) {
            particulas(now);
            return;
        }
        ServerLevel sl = (ServerLevel) level();
        if (enMarcha()) {
            long t = now - this.entityData.get(INICIO);
            if (t % 30 == 5) sl.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.7f, 1.3f);
            if (t % 40 == 20) sl.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2f, 0.7f + random.nextFloat() * 0.5f);
            if (t >= VIAJE) llegar(sl);
            return;
        }
        boolean abajo = estado() == ABAJO;
        double aqui = abajo ? y0() : y1(), alli = abajo ? y1() : y0();
        List<ServerPlayer> encima = encima(sl, aqui);
        // quien acaba de llegar no vuelve a viajar hasta que se baje del disco (solo por la posicion: justo al
        // bajarse no esta "en el suelo" un tick y no hay que olvidarle por eso)
        recien.removeIf(u -> {
            ServerPlayer p = sl.getServer().getPlayerList().getPlayer(u);
            return p == null || p.level() != sl || !sobreDisco(p, aqui, 0.6);
        });
        encima.removeIf(p -> recien.contains(p.getUUID()));
        // mientras Ser Gavran pelea en la sala, el disco no responde (ni sube ni baja)
        if ((!encima.isEmpty() || !encima(sl, alli).isEmpty()) && now % 10 == 0
                && com.darkcraft.gavran.GavranEventos.salaCerrada(sl, getX(), getZ())) {
            for (ServerPlayer p : encima) p.displayClientMessage(Component.translatable("darkcraft.ascensor.bloqueado"), true);
            for (ServerPlayer p : encima(sl, alli)) p.displayClientMessage(Component.translatable("darkcraft.ascensor.bloqueado"), true);
            this.entityData.set(CARGA, 0);
            llamada = 0;
            bloqueado = 40;
        }
        if (bloqueado > 0) {
            bloqueado--;
            return;
        }
        if (!encima.isEmpty()) {
            int c = this.entityData.get(CARGA) + 1;
            this.entityData.set(CARGA, c);
            for (ServerPlayer p : encima) p.displayClientMessage(Component.translatable(abajo ? "darkcraft.ascensor.sube" : "darkcraft.ascensor.baja", barra(c)), true);
            if (c % 10 == 1) sl.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.5f, 0.6f + c / (float) ESPERA);
            if (c >= ESPERA) {
                for (ServerPlayer p : encima) if (!p.isPassenger()) p.startRiding(this, true);
                arrancar(sl, abajo ? SUBE : BAJA);
            }
            llamada = 0;
            return;
        }
        if (this.entityData.get(CARGA) > 0) this.entityData.set(CARGA, Math.max(0, this.entityData.get(CARGA) - 2));
        // alguien lo llama desde el otro extremo
        List<ServerPlayer> lejos = encima(sl, alli);
        if (lejos.isEmpty()) {
            llamada = 0;
            return;
        }
        llamada++;
        for (ServerPlayer p : lejos) p.displayClientMessage(Component.translatable("darkcraft.ascensor.llama", barra(llamada)), true);
        if (llamada % 10 == 1) {
            sl.playSound(null, getX(), alli, getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.5f, 0.6f + llamada / (float) ESPERA);
        }
        if (llamada >= ESPERA) {
            llamada = 0;
            arrancar(sl, abajo ? SUBE : BAJA);
        }
    }

    private static String barra(int c) {
        int n = Mth.clamp(Math.round(c * 8f / ESPERA), 0, 8);
        return "◆".repeat(n) + "◇".repeat(8 - n);
    }

    /** ¿Esta este jugador encima del disco (o de la runa del otro extremo) a la altura y? (solo la posicion) */
    private boolean sobreDisco(ServerPlayer p, double y, double margen) {
        return Math.abs(p.getX() - getX()) < RADIO + margen && Math.abs(p.getZ() - getZ()) < RADIO + margen && Math.abs(p.getY() - y) < 1.5;
    }

    /** Jugadores de pie sobre el disco (o sobre la runa del otro extremo) a la altura y. */
    private List<ServerPlayer> encima(ServerLevel sl, double y) {
        List<ServerPlayer> out = new ArrayList<>();
        for (ServerPlayer p : sl.players()) {
            if (p.isSpectator() || p.isPassenger() || !p.isAlive()) continue;
            if (Math.abs(p.getX() - getX()) > RADIO - 0.05 || Math.abs(p.getZ() - getZ()) > RADIO - 0.05) continue;
            if (Math.abs(p.getY() - y) > 0.75) continue;
            if (!p.onGround() && !p.getAbilities().flying) continue;
            out.add(p);
        }
        return out;
    }

    private void arrancar(ServerLevel sl, int dir) {
        this.entityData.set(CARGA, 0);
        this.entityData.set(INICIO, sl.getGameTime());
        this.entityData.set(ESTADO, dir);
        sl.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2f, 1.2f);
        // el ruido del disco va pegado a el y sube (o baja) con los que van encima
        sl.playSound(null, this, com.darkcraft.DarkCraftMod.SFX_ASCENSOR_MUEVE.get(), SoundSource.BLOCKS, 1.8f, deHueso() ? 0.8f : 1.0f);
        sl.sendParticles(new DustParticleOptions(color(), 1.4f), getX(), getY() + 0.2, getZ(), 50, 1.6, 0.1, 1.6, 0.02);
        if (deHueso()) sl.sendParticles(ParticleTypes.WHITE_ASH, getX(), getY() + 0.3, getZ(), 40, 1.6, 0.2, 1.6, 0.02);
    }

    private void llegar(ServerLevel sl) {
        boolean arriba = estado() == SUBE;
        this.entityData.set(ESTADO, arriba ? ARRIBA : ABAJO);
        setPos(getX(), arriba ? y1() : y0(), getZ());
        sl.playSound(null, getX(), getY(), getZ(), com.darkcraft.DarkCraftMod.SFX_ASCENSOR_LLEGA.get(), SoundSource.BLOCKS, 2.0f, 1.0f);
        if (deHueso()) {
            sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 0.3, getZ(), 18, 1.8, 0.2, 1.8, 0.03);
            sl.sendParticles(ParticleTypes.WHITE_ASH, getX(), getY() + 0.4, getZ(), 50, 1.8, 0.3, 1.8, 0.03);
        } else sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.3, getZ(), 30, 1.8, 0.2, 1.8, 0.04);
        soltarTodos();
    }

    /** Baja a todos los pasajeros donde estan (encima del disco). */
    public void soltarTodos() {
        soltando = true;
        try {
            for (Entity p : new ArrayList<>(getPassengers())) {
                recien.add(p.getUUID());
                p.stopRiding();
            }
        } finally {
            soltando = false;
        }
    }

    /** ¿Puede bajarse este pasajero ahora? (a medio viaje no). */
    public boolean dejaBajar() {
        return soltando || !enMarcha();
    }

    /** Si el jugador se va (o se cierra el servidor) a medio viaje: lo deja en el destino, de pie. */
    public void rescatar(ServerPlayer p) {
        double y = estado() == SUBE ? y1() : estado() == BAJA ? y0() : getY();
        soltando = true;
        try {
            p.stopRiding();
        } finally {
            soltando = false;
        }
        p.teleportTo(p.serverLevel(), getX(), y + 0.01, getZ(), p.getYRot(), p.getXRot());
        p.fallDistance = 0;
    }

    // ------------------------------------------------------------------ pasajeros

    @Override
    protected boolean canAddPassenger(Entity e) {
        return getPassengers().size() < 8;
    }

    @Override
    protected void positionRider(Entity p, MoveFunction f) {
        int i = getPassengers().indexOf(p);
        double ox = 0, oz = 0;
        if (i > 0) {
            double a = i * 2.39996;
            ox = Math.cos(a) * 1.35;
            oz = Math.sin(a) * 1.35;
        }
        f.accept(p, getX() + ox, getY() + 0.02, getZ() + oz);
        p.fallDistance = 0;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity p) {
        return new Vec3(p.getX(), getY() + 0.02, p.getZ());
    }

    @Override
    public void remove(RemovalReason why) {
        // si desaparece a medio viaje, nadie se queda colgando en el hueco
        if (!level().isClientSide && enMarcha())
            for (Entity p : new ArrayList<>(getPassengers())) if (p instanceof ServerPlayer sp) rescatar(sp);
        super.remove(why);
    }

    // ------------------------------------------------------------------ el cliente calcula la altura el mismo (sin tirones)

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport) {
        setPos(x, alturaEn(level().getGameTime()), z);
    }

    @Override
    public void lerpMotion(double x, double y, double z) {
    }

    /** El color del polvo de las runas: oro en el disco del arbol, rojo sangre en la placa de hueso. */
    private Vector3f color() {
        return deHueso() ? new Vector3f(0.78f, 0.1f, 0.07f) : new Vector3f(1f, 0.78f, 0.28f);
    }

    private void particulas(long now) {
        if (enMarcha()) {
            for (int i = 0; i < 3; i++) {
                double a = random.nextDouble() * Math.PI * 2;
                double r = RADIO - 0.1;
                // por el borde: en la placa cuadrada, por su contorno
                double ox = Math.cos(a) * r, oz = Math.sin(a) * r;
                if (deHueso()) {
                    double m = Math.max(Math.abs(Math.cos(a)), Math.abs(Math.sin(a)));
                    ox /= m;
                    oz /= m;
                }
                level().addParticle(new DustParticleOptions(color(), 1.0f), getX() + ox, getY() - 0.1, getZ() + oz, 0, 0, 0);
            }
            if (now % 3 == 0) {
                double a = random.nextDouble() * Math.PI * 2;
                level().addParticle(deHueso() ? ParticleTypes.WHITE_ASH : ParticleTypes.END_ROD, getX() + Math.cos(a) * 1.8, getY() + 0.1,
                        getZ() + Math.sin(a) * 1.8, 0, estado() == SUBE ? -0.08 : 0.08, 0);
            }
        } else if (this.entityData.get(CARGA) > 0) {
            for (int i = 0; i < 2; i++) {
                double a = random.nextDouble() * Math.PI * 2, r = random.nextDouble() * RADIO;
                level().addParticle(ParticleTypes.ENCHANT, getX() + Math.cos(a) * r, getY() + 1.4, getZ() + Math.sin(a) * r, 0, -0.6, 0);
            }
        }
    }

    // ------------------------------------------------------------------ lo demas

    @Override
    public boolean isPickable() {
        return false;
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
        return new AABB(getX() - 3, Math.min(y0(), getY()) - 1, getZ() - 3, getX() + 3, Math.max(y1(), getY()) + 1, getZ() + 3);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 192 * 192;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag t) {
        this.entityData.set(Y0, t.getInt("Y0"));
        this.entityData.set(Y1, t.getInt("Y1"));
        this.entityData.set(ESTADO, t.getBoolean("Arriba") ? ARRIBA : ABAJO);
        this.entityData.set(TIPO, t.getInt("Tipo"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag t) {
        t.putInt("Y0", y0());
        t.putInt("Y1", y1());
        t.putBoolean("Arriba", estado() == ARRIBA || estado() == SUBE);
        t.putInt("Tipo", tipo());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
