package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/**
 * La Vieja (parte 7r): la ballena que Brann lleva cuarenta anos arponeando, que hace mucho que no esta. A media vida de Brann sale
 * del suelo como si la arena fuera el mar y embiste en linea recta; desde entonces vuelve cada tanto. Un cachalote fantasma de
 * unos diecisiete bloques (lo dibuja {@code client/enemigos/ViejaRenderer}, medio transparente y luciendo). No es un enemigo: no
 * se le pega ni tiene vida; pasa a traves de todo.
 * <ul>
 * <li><b>Aviso</b> ({@link #AVISO} ticks, no se ve): donde va a salir, su chorro de agua ("¡alli resopla!"), y por donde va a
 * pasar, una linea de espuma en la arena;</li>
 * <li><b>sale</b> ({@link #SALIR}): asoma del suelo con la cabeza alta;</li>
 * <li><b>embiste</b> a {@link #VELOCIDAD} bloques por tick hasta el otro lado: a quien pille (a lo largo de su cuerpo, a menos de
 * {@link #RADIO}) le da fuerte y lo lanza de lado y hacia arriba (una vez por embestida);</li>
 * <li><b>se hunde</b> ({@link #HUNDIR}) de cabeza en la arena y se va.</li>
 * </ul>
 * Al caer Brann <b>sube</b> ({@link #SUBIR}): sale de la arena de pie y se va al cielo despacio, apagandose. No se guarda.
 */
public class Vieja extends Entity {
    public static final int AVISO = 0, SALE = 1, EMBISTE = 2, HUNDE = 3, SUBE = 4;
    private static final EntityDataAccessor<Byte> ESTADO = SynchedEntityData.defineId(Vieja.class, EntityDataSerializers.BYTE);
    /** Lo que dura cada fase (ticks). */
    public static final int AVISO_TICKS = 30, SALIR = 22, HUNDIR = 18, SUBIR = 110;
    /** Lo que avanza por tick embistiendo (bloques). */
    public static final double VELOCIDAD = 1.05;
    /** Lo largo de su cuerpo y lo cerca de el que pilla. */
    public static final double LARGO = 17.0, RADIO = 3.1;
    /** Lo que pega (por el golpe de Brann) y lo que lanza. */
    public static final float DANO = 1.35f;

    // ---- en el servidor
    private Vec3 desde = Vec3.ZERO, hasta = Vec3.ZERO, dir = new Vec3(1, 0, 0);
    /** Lo que se pisa por donde pasa (la tripa le va a ras de arena). */
    private double suelo;
    private int t;
    private Ballenero dueno;
    private final Set<Integer> tocados = new HashSet<>();
    /** Para las pruebas: las que han salido, las que han embestido y a cuantos han dado. */
    public static int SALEN, EMBISTEN, DAN;

    // ---- en el cliente: desde cuando se ve el estado que se ve
    public int estadoVisto = -1, desdeVisto;

    public Vieja(EntityType<? extends Vieja> t, Level l) {
        super(t, l);
        noPhysics = true;
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(ESTADO, (byte) AVISO);
    }

    public int estado() {
        return entityData.get(ESTADO);
    }

    private void cambiar(int e) {
        entityData.set(ESTADO, (byte) e);
        t = 0;
    }

    /**
     * La embestida: de {@code desde} a {@code hasta} (a ras de {@code suelo}); avisa donde, sale de la arena antes de
     * {@code desde} y se hunde pasado {@code hasta}.
     */
    public static Vieja embestir(ServerLevel sl, Ballenero dueno, Vec3 desde, Vec3 hasta, double suelo) {
        Vieja v = DarkCraftMod.VIEJA.get().create(sl);
        if (v == null) return null;
        v.dueno = dueno;
        v.suelo = suelo;
        v.desde = new Vec3(desde.x, suelo, desde.z);
        v.hasta = new Vec3(hasta.x, suelo, hasta.z);
        Vec3 d = v.hasta.subtract(v.desde);
        v.dir = d.lengthSqr() < 1e-4 ? new Vec3(1, 0, 0) : d.normalize();
        float giro = (float) (Math.toDegrees(Math.atan2(v.dir.z, v.dir.x)) - 90.0);
        // bajo la arena, antes de donde sale
        v.moveTo(v.desde.x - v.dir.x * 6, suelo - 7, v.desde.z - v.dir.z * 6, giro, -24f);
        v.cambiar(AVISO);
        sl.addFreshEntity(v);
        SALEN++;
        sl.playSound(null, desde.x, suelo, desde.z, DarkCraftMod.SFX_BALLENERO_VIEJA.get(), SoundSource.HOSTILE, 4.0f, 1.0f);
        return v;
    }

    /** Al caer Brann: sale de la arena de pie en (x, z) y se va al cielo despacio. */
    public static Vieja subir(ServerLevel sl, double x, double suelo, double z, float giro) {
        Vieja v = DarkCraftMod.VIEJA.get().create(sl);
        if (v == null) return null;
        v.suelo = suelo;
        v.desde = v.hasta = new Vec3(x, suelo, z);
        v.moveTo(x, suelo - 10, z, giro, -72f);
        v.cambiar(SUBE);
        sl.addFreshEntity(v);
        sl.playSound(null, x, suelo, z, DarkCraftMod.SFX_BALLENERO_VIEJA.get(), SoundSource.HOSTILE, 4.0f, 0.85f);
        return v;
    }

    /** El camino de su embestida (pruebas): {desde, hasta}. */
    public Vec3[] camino() {
        return new Vec3[]{desde, hasta};
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            cliente();
            return;
        }
        if (!(level() instanceof ServerLevel sl)) return;
        t++;
        double x = getX(), y = getY(), z = getZ();
        float pitch = getXRot();
        switch (estado()) {
            case AVISO -> {
                aviso(sl);
                if (t >= AVISO_TICKS) cambiar(SALE);
            }
            case SALE -> {
                // de debajo de la arena a ras de ella, avanzando hasta donde empieza la embestida, con la cabeza alta
                float f = Mth.clamp(t / (float) SALIR, 0f, 1f);
                double s = f * f * (3 - 2 * f);
                x = desde.x - dir.x * 6 * (1 - f);
                z = desde.z - dir.z * 6 * (1 - f);
                y = suelo - 7 + (7 + 1.1) * s;
                pitch = -24f * (1 - f);
                estela(sl, x, z);
                if (t > SALIR / 2) golpe(sl, x, y, z);
                if (t >= SALIR) {
                    cambiar(EMBISTE);
                    EMBISTEN++;
                    sl.playSound(null, x, y, z, DarkCraftMod.SFX_BALLENERO_EMBESTIDA.get(), SoundSource.HOSTILE, 3.5f, 1.0f);
                }
            }
            case EMBISTE -> {
                x += dir.x * VELOCIDAD;
                z += dir.z * VELOCIDAD;
                y = suelo + 1.1 + Math.sin(t * 0.35) * 0.25;
                pitch = (float) Math.sin(t * 0.35) * 3f;
                estela(sl, x, z);
                golpe(sl, x, y, z);
                // pasado el final, se hunde
                if ((x - hasta.x) * dir.x + (z - hasta.z) * dir.z >= 0 || t > 160) cambiar(HUNDE);
            }
            case HUNDE -> {
                float f = Mth.clamp(t / (float) HUNDIR, 0f, 1f);
                x += dir.x * 0.6;
                z += dir.z * 0.6;
                y = suelo + 1.1 - 10 * f * f;
                pitch = 30f * f;
                if (t < 6) estela(sl, x, z);
                if (t >= HUNDIR) {
                    discard();
                    return;
                }
            }
            case SUBE -> {
                // sale de pie y sube al cielo, cada vez mas despacio
                y += t < 20 ? 0.55 : 0.32;
                pitch = -72f;
                if (t < 24 && t % 2 == 0) {
                    sl.sendParticles(ParticleTypes.SPLASH, x, suelo + 0.2, z, 40, 2.4, 0.2, 2.4, 0.3);
                    sl.sendParticles(ParticleTypes.CLOUD, x, suelo + 0.5, z, 10, 2.0, 0.4, 2.0, 0.05);
                }
                if (t % 3 == 0) sl.sendParticles(ParticleTypes.FALLING_WATER, x, y - 4, z, 8, 1.8, 3.0, 1.8, 0.0);
                if (t >= SUBIR) {
                    discard();
                    return;
                }
            }
            default -> {
            }
        }
        setPos(x, y, z);
        setXRot(pitch);
    }

    /** El aviso: su chorro de agua donde va a salir y la espuma por donde va a pasar. */
    private void aviso(ServerLevel sl) {
        // el chorro ("¡alli resopla!"): una columna de agua y vaho que sube
        sl.sendParticles(ParticleTypes.CLOUD, desde.x, suelo + 1.0, desde.z, 4, 0.3, 0.2, 0.3, 0.0);
        for (int k = 0; k < 4; k++)
            sl.sendParticles(ParticleTypes.SPLASH, desde.x, suelo + 1.0 + k * 1.6, desde.z, 6, 0.3, 0.4, 0.3, 0.2);
        sl.sendParticles(ParticleTypes.FALLING_WATER, desde.x, suelo + 6.0, desde.z, 6, 1.0, 0.6, 1.0, 0.0);
        if (t % 2 != 0) return;
        // la linea de espuma: de un lado a otro de por donde va a pasar
        double largo = hasta.subtract(desde).horizontalDistance();
        Vec3 lado = new Vec3(-dir.z, 0, dir.x);
        for (double s = 0; s <= largo; s += 1.5) {
            double px = desde.x + dir.x * s, pz = desde.z + dir.z * s;
            sl.sendParticles(ParticleTypes.CLOUD, px, suelo + 0.15, pz, 1, 0.3, 0.0, 0.3, 0.0);
            if ((int) s % 3 == 0) {
                sl.sendParticles(ParticleTypes.BUBBLE_POP, px + lado.x * RADIO, suelo + 0.2, pz + lado.z * RADIO, 1, 0.1, 0.0, 0.1, 0.0);
                sl.sendParticles(ParticleTypes.BUBBLE_POP, px - lado.x * RADIO, suelo + 0.2, pz - lado.z * RADIO, 1, 0.1, 0.0, 0.1, 0.0);
            }
        }
    }

    /** La arena que se abre como agua por donde pasa. */
    private void estela(ServerLevel sl, double x, double z) {
        sl.sendParticles(ParticleTypes.SPLASH, x, suelo + 0.3, z, 30, 2.2, 0.2, 2.2, 0.3);
        sl.sendParticles(ParticleTypes.CLOUD, x - dir.x * 6, suelo + 0.4, z - dir.z * 6, 6, 1.6, 0.3, 1.6, 0.03);
        sl.sendParticles(Salobre.POLVO, x, suelo + 0.4, z, 10, 1.8, 0.3, 1.8, 0.1);
    }

    /** A quien este a lo largo de su cuerpo: un golpe fuerte que lanza de lado y hacia arriba (una vez por embestida). */
    private void golpe(ServerLevel sl, double x, double y, double z) {
        Vec3 cabeza = new Vec3(x + dir.x * LARGO * 0.45, y, z + dir.z * LARGO * 0.45);
        Vec3 cola = new Vec3(x - dir.x * LARGO * 0.55, y, z - dir.z * LARGO * 0.55);
        AABB caja = new AABB(cabeza, cola).inflate(RADIO + 1, 4, RADIO + 1);
        float dano = dueno != null ? (float) dueno.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) * DANO : 16f;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, caja, e -> e.isAlive() && !(e instanceof Enemigo) && valido(e))) {
            if (tocados.contains(le.getId())) continue;
            // lo lejos de su cuerpo (la linea de la cabeza a la cola, en el plano)
            double ax = cola.x, az = cola.z, bx = cabeza.x - ax, bz = cabeza.z - az, L = bx * bx + bz * bz;
            double k = L < 1e-6 ? 0 : Mth.clamp(((le.getX() - ax) * bx + (le.getZ() - az) * bz) / L, 0, 1);
            double px = ax + k * bx, pz = az + k * bz;
            double d = Math.hypot(le.getX() - px, le.getZ() - pz);
            if (d > RADIO + le.getBbWidth() * 0.5) continue;
            if (le.getY() > y + 3.0 || le.getY() + le.getBbHeight() < y - 2.5) continue;
            tocados.add(le.getId());
            boolean ok = dueno != null && dueno.isAlive() ? le.hurt(damageSources().mobAttack(dueno), dano) : le.hurt(damageSources().generic(), dano);
            if (!ok) continue;
            DAN++;
            // de lado (hacia fuera de su camino) y hacia arriba
            double sx = le.getX() - px, sz = le.getZ() - pz, sl2 = Math.hypot(sx, sz);
            if (sl2 < 0.1) {
                sx = -dir.z;
                sz = dir.x;
                sl2 = 1;
            }
            double f = 1.25 * (1.0 - le.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE) * 0.5);
            le.setDeltaMovement(sx / sl2 * f + dir.x * 0.5, 0.75, sz / sl2 * f + dir.z * 0.5);
            le.hurtMarked = true;
            sl.sendParticles(ParticleTypes.SPLASH, le.getX(), le.getY() + 1, le.getZ(), 30, 0.5, 0.5, 0.5, 0.3);
        }
    }

    /** A quien puede dar: a los jugadores (no en creativo ni espectador) y a la presa de Brann. */
    private boolean valido(LivingEntity e) {
        if (e instanceof Player p) return !p.isCreative() && !p.isSpectator();
        return dueno != null && dueno.getTarget() == e;
    }

    private void cliente() {
        int e = estado();
        if (e != estadoVisto) {
            estadoVisto = e;
            desdeVisto = tickCount;
        }
    }

    /** Lo que se ve de ella (0..1) en el cliente: aparece al salir y se apaga al hundirse o al subir. */
    public float alfa(float pt) {
        float s = tickCount - desdeVisto + pt;
        return switch (estado()) {
            case AVISO -> 0f;
            case SALE -> Mth.clamp(s / SALIR, 0f, 1f);
            case EMBISTE -> 1f;
            case HUNDE -> Mth.clamp(1f - s / HUNDIR, 0f, 1f);
            case SUBE -> Mth.clamp(Math.min(s / 16f, 1f - (s - SUBIR + 50) / 50f), 0f, 1f);
            default -> 1f;
        };
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag t) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag t) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
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
        return d < 160 * 160;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(LARGO * 0.6, 6, LARGO * 0.6);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
