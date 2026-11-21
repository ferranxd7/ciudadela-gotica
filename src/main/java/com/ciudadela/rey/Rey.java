package com.ciudadela.rey;

import com.ciudadela.CiudadelaMod;
import com.ciudadela.net.Net;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;

/**
 * Corvin, el Rey que Espera. Espera sentado en su trono; cuando alguien entra en el salon se cierran
 * los rastrillos, habla, se levanta y pelea en tres fases (normal, fuego y lava). Toda la pelea la lleva
 * el propio jefe en el servidor; el cliente solo pone las animaciones, el brillo y las particulas.
 */
public class Rey extends Monster {
    // ------------------------------------------------------------------ datos que ve el cliente
    private static final EntityDataAccessor<Integer> FASE = SynchedEntityData.defineId(Rey.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> POSE = SynchedEntityData.defineId(Rey.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ANIM = SynchedEntityData.defineId(Rey.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ANIM_N = SynchedEntityData.defineId(Rey.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FLAGS = SynchedEntityData.defineId(Rey.class, EntityDataSerializers.INT);

    public static final int POSE_PIE = 0, POSE_SENTADO = 1, POSE_GUARDIA = 2, POSE_CARGA = 3;
    public static final int F_LUCHA = 1, F_SIN_CORONA = 2, F_CENIZA = 4, F_HABLA = 8;

    public static final ResourceKey<DamageType> GOLPE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(CiudadelaMod.MODID, "rey"));
    public static final ResourceKey<DamageType> AREA = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(CiudadelaMod.MODID, "rey_area"));

    /** Vida "real" del jefe (el maximo de Minecraft es 1024): el dano que recibe se escala. */
    public static final float VIDA = 1000f;
    /** Vida efectiva de cada fase para un jugador solo (con espada de netherite afilada V: unos 5 minutos). */
    public static final float VIDA_FASE = 2400f;

    // ------------------------------------------------------------------ cliente
    public final AnimationState sitAnim = new AnimationState(), idleAnim = new AnimationState(),
            guardAnim = new AnimationState(), chargeAnim = new AnimationState(), talkAnim = new AnimationState();
    public final AnimationState[] actions = new AnimationState[ReyAnims.TICKS.length];
    private int actionId = 0, actionStart = -1000;
    public int lastFx = -1, ashStart = -1;

    // ------------------------------------------------------------------ servidor
    public enum Estado { DORMIDO, INTRO, LEVANTA, LUCHA, CAMBIO, ATURDIDO, MUERTE }

    private Estado estado = Estado.DORMIDO;
    private int t;                       // ticks en el estado actual
    private Vec3 home;                   // asiento del trono
    private Sala sala;
    private final Set<UUID> jugadores = new LinkedHashSet<>();
    private final ServerBossEvent barra = new ServerBossEvent(Component.translatable("entity.ciudadela.rey"),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_6);
    private Atq atq;
    private int at;                      // ticks dentro del ataque
    private int recup;
    private final EnumMap<Atq, Integer> cds = new EnumMap<>(Atq.class);
    private LivingEntity objetivo;
    private int cambiaObjetivo;
    private Vec3 destino, dir;
    private double alturaInicio;
    private final Map<UUID, Integer> golpesGiro = new HashMap<>();
    private final Set<UUID> tocados = new HashSet<>();
    private final List<double[]> zonas = new ArrayList<>(); // {x, y, z, r0, r1, ticksQueQuedan, tipo}
    private final List<Vec3> puntos = new ArrayList<>();
    private int puerta = -1;              // paso de los rastrillos (-1 nada)
    private boolean cerrando;
    private int burla;
    private boolean contra;

    public Rey(EntityType<? extends Rey> type, Level level) {
        super(type, level);
        for (int i = 0; i < actions.length; i++) actions[i] = new AnimationState();
        this.xpReward = 0;
        this.setPersistenceRequired();
        this.barra.setVisible(false);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, VIDA)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 10)
                .add(Attributes.FOLLOW_RANGE, 64)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, 0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FASE, 1);
        this.entityData.define(POSE, POSE_SENTADO);
        this.entityData.define(ANIM, 0);
        this.entityData.define(ANIM_N, 0);
        this.entityData.define(FLAGS, 0);
    }

    // ------------------------------------------------------------------ acceso

    public int fase() {
        return this.entityData.get(FASE);
    }

    public int pose() {
        return this.entityData.get(POSE);
    }

    public boolean flag(int f) {
        return (this.entityData.get(FLAGS) & f) != 0;
    }

    private void setFlag(int f, boolean on) {
        int v = this.entityData.get(FLAGS);
        this.entityData.set(FLAGS, on ? v | f : v & ~f);
    }

    public boolean luchando() {
        return flag(F_LUCHA);
    }

    public Estado estado() {
        return estado;
    }

    public Set<UUID> jugadores() {
        return jugadores;
    }

    public Sala sala() {
        if (sala == null && home != null && level() instanceof ServerLevel sl)
            sala = Sala.near(sl, (int) home.x, (int) home.z);
        return sala;
    }

    public void setHome(Vec3 seat, Sala s) {
        this.home = seat;
        this.sala = s;
    }

    private void anim(int id) {
        this.entityData.set(ANIM, id);
        this.entityData.set(ANIM_N, this.entityData.get(ANIM_N) + 1);
    }

    private void setPose(int p) {
        this.entityData.set(POSE, p);
    }

    // ------------------------------------------------------------------ cliente: animaciones

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (level().isClientSide && ANIM_N.equals(key)) {
            int id = this.entityData.get(ANIM);
            for (AnimationState a : actions) a.stop();
            actionId = 0;
            if (id > 0 && id < actions.length) {
                actions[id].start(this.tickCount);
                actionId = id;
                actionStart = this.tickCount;
            }
        }
    }

    /** Si hay una animacion de accion en marcha (en el cliente). */
    public boolean acting() {
        return actionId > 0 && (actionId == ReyAnims.MUERTE || this.tickCount - actionStart <= ReyAnims.TICKS[actionId]);
    }

    public int actionId() {
        return acting() ? actionId : 0;
    }

    private void clientTick() {
        int p = pose();
        sitAnim.animateWhen(p == POSE_SENTADO, this.tickCount);
        guardAnim.animateWhen(p == POSE_GUARDIA, this.tickCount);
        chargeAnim.animateWhen(p == POSE_CARGA, this.tickCount);
        talkAnim.animateWhen(flag(F_HABLA), this.tickCount);
        idleAnim.startIfStopped(this.tickCount);
        if (actionId > 0 && actionId != ReyAnims.MUERTE && this.tickCount - actionStart > ReyAnims.TICKS[actionId] + 2) {
            actions[actionId].stop();
            actionId = 0;
        }
        if (flag(F_CENIZA) && ashStart < 0) ashStart = this.tickCount;
    }

    /** Transparencia al convertirse en ceniza (1 = normal). */
    public float alpha(float pt) {
        if (ashStart < 0) return 1f;
        float k = (this.tickCount - ashStart + pt) / 40f;
        return Mth.clamp(1f - k, 0f, 1f);
    }

    // ------------------------------------------------------------------ cosas de Minecraft

    @Override
    protected void registerGoals() {
        // la pelea la lleva el propio jefe
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public void checkDespawn() {
    }

    @Override
    public boolean isPushable() {
        return estado == Estado.LUCHA;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float dist, float mul, DamageSource src) {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource src) {
        return SoundEvents.WITHER_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.4f;
    }

    @Override
    public float getVoicePitch() {
        return 0.55f;
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (home != null) {
            tag.putDouble("CasaX", home.x);
            tag.putDouble("CasaY", home.y);
            tag.putDouble("CasaZ", home.z);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("CasaX")) home = new Vec3(tag.getDouble("CasaX"), tag.getDouble("CasaY"), tag.getDouble("CasaZ"));
        // si el mundo se cerro en mitad de la pelea, el rey vuelve a su trono
        estado = Estado.DORMIDO;
        t = 0;
    }

    // ------------------------------------------------------------------ tick

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) clientTick();
    }

    @Override
    protected void customServerAiStep() {
        if (!(level() instanceof ServerLevel sl)) return;
        t++;
        if (home == null) home = position();
        tickPuertas(sl);
        tickZonas(sl);
        switch (estado) {
            case DORMIDO -> dormido(sl);
            case INTRO -> intro(sl);
            case LEVANTA -> levanta(sl);
            case LUCHA -> lucha(sl);
            case CAMBIO -> cambio(sl);
            case ATURDIDO -> aturdido(sl);
            case MUERTE -> muerte(sl);
        }
        barra.setProgress(Mth.clamp(getHealth() / getMaxHealth(), 0f, 1f));
    }

    private void cambiar(Estado e) {
        estado = e;
        t = 0;
    }

    // ------------------------------------------------------------------ dormido en el trono

    private void dormido(ServerLevel sl) {
        setPose(POSE_SENTADO);
        setFlag(F_LUCHA, false);
        setFlag(F_HABLA, false);
        this.entityData.set(FASE, 1);
        setNoGravity(true);
        this.noPhysics = true;
        getNavigation().stop();
        setDeltaMovement(Vec3.ZERO);
        if (home != null) {
            moveTo(home.x, home.y, home.z, 0f, 0f);
            setYBodyRot(0f);
            setYHeadRot(0f);
        }
        if (getHealth() < getMaxHealth()) setHealth(getMaxHealth());
        if (t % 10 != 0) return;
        Sala s = sala();
        if (s == null) return;
        List<ServerPlayer> dentro = new ArrayList<>();
        for (ServerPlayer p : sl.players())
            if (valido(p) && s.inside(p.position(), 1.0)) dentro.add(p);
        if (dentro.isEmpty()) return;
        // empieza: se cierran las puertas y habla
        jugadores.clear();
        for (ServerPlayer p : dentro) {
            jugadores.add(p.getUUID());
            Vec3 in = s.pushInside(p.position());
            if (in != null) p.teleportTo(sl, in.x, in.y, in.z, p.getYRot(), p.getXRot());
        }
        ReyEstado.get(sl.getServer()).set(s.key(), ReyEstado.LUCHA);
        cerrando = true;
        puerta = 0;
        cambiar(Estado.INTRO);
        for (ServerPlayer p : jugadoresVivos(sl)) Net.to(p, new Net.ReyFoco(getId(), INTRO_TICKS));
        CiudadelaMod.LOGGER.info("[Ciudadela] El Rey despierta ({} jugadores)", jugadores.size());
    }

    public static boolean valido(Player p) {
        return p.isAlive() && !p.isSpectator() && !p.isCreative();
    }

    private List<ServerPlayer> jugadoresVivos(ServerLevel sl) {
        List<ServerPlayer> l = new ArrayList<>();
        for (UUID id : jugadores) {
            ServerPlayer p = sl.getServer().getPlayerList().getPlayer(id);
            if (p != null && p.level() == sl && valido(p)) l.add(p);
        }
        return l;
    }

    // ------------------------------------------------------------------ intro: habla sentado

    public static final int INTRO_TICKS = 20 * 17;
    private static final int[][] INTRO_LINES = {{30, 1}, {30 + 95, 2}, {30 + 95 + 115, 3}};

    private void intro(ServerLevel sl) {
        setPose(POSE_SENTADO);
        for (int[] l : INTRO_LINES)
            if (t == l[0]) {
                setFlag(F_HABLA, true);
                habla(sl, l[1]);
            }
        if (t == INTRO_LINES[2][0] + 70) setFlag(F_HABLA, false);
        if (t == 30 + 85 || t == 30 + 95 + 105) setFlag(F_HABLA, false);
        if (t >= INTRO_TICKS - 60) {
            setFlag(F_HABLA, false);
            levantarse(sl);
        }
    }

    private void levantarse(ServerLevel sl) {
        anim(ReyAnims.LEVANTARSE);
        setPose(POSE_PIE);
        sonido(sl, SoundEvents.ARMOR_EQUIP_NETHERITE, 1.2f, 0.6f);
        cambiar(Estado.LEVANTA);
    }

    private void levanta(ServerLevel sl) {
        Sala s = sala();
        if (t <= 20 && s != null) {
            // del asiento al pie de la tarima
            Vec3 a = home, b = s.front();
            double k = t / 20.0;
            k = k * k * (3 - 2 * k);
            double y = Mth.lerp(k, a.y, b.y) + Math.sin(k * Math.PI) * 0.6;
            setPos(Mth.lerp(k, a.x, b.x), y, Mth.lerp(k, a.z, b.z));
        }
        if (t == 30) {
            sonido(sl, SoundEvents.ENDER_DRAGON_GROWL, 0.5f, 0.6f);
            sonido(sl, SoundEvents.TRIDENT_RIPTIDE_3, 1.0f, 0.5f);
        }
        if (t >= ReyAnims.TICKS[ReyAnims.LEVANTARSE]) {
            setNoGravity(false);
            this.noPhysics = false;
            setFlag(F_LUCHA, true);
            barra.setVisible(true);
            for (ServerPlayer p : jugadoresVivos(sl)) barra.addPlayer(p);
            recup = 10;
            cambiar(Estado.LUCHA);
        }
    }

    private void habla(ServerLevel sl, int linea) {
        for (ServerPlayer p : sl.players())
            if (p.distanceToSqr(this) < 80 * 80) Net.to(p, new Net.ReyHabla(getId(), linea));
    }

    // ------------------------------------------------------------------ pelea

    private void lucha(ServerLevel sl) {
        List<ServerPlayer> vivos = jugadoresVivos(sl);
        actualizarJugadores(sl, vivos);
        if (vivos.isEmpty()) {
            if (t > 40) reiniciar(sl, "todos han caido");
            return;
        }
        if (atq == null) setPose(POSE_PIE);
        else if (atq == Atq.GUARDIA) setPose(contra ? POSE_PIE : POSE_GUARDIA);
        // objetivo: el mas cercano, y de vez en cuando otro
        if (objetivo == null || !objetivo.isAlive() || !(objetivo instanceof ServerPlayer sp && vivos.contains(sp)) || --cambiaObjetivo <= 0) {
            objetivo = vivos.get(0);
            double best = 1e9;
            for (ServerPlayer p : vivos) {
                double d = p.distanceToSqr(this) + (vivos.size() > 1 ? random.nextDouble() * 60 : 0);
                if (d < best) {
                    best = d;
                    objetivo = p;
                }
            }
            cambiaObjetivo = 200 + random.nextInt(200);
            setTarget(objetivo);
        }
        LivingEntity o = objetivo;
        // no salir de la sala (si esta en la sala)
        Sala s = sala();
        if (s != null && level().dimension() == Level.OVERWORLD && !s.inside(position(), 0.5)) {
            Vec3 c = s.center();
            moveTo(c.x, s.t.y(), c.z, getYRot(), 0);
        }
        if (atq != null) {
            tickAtaque(sl, o, vivos);
            return;
        }
        // mirar al objetivo
        mirar(o, 25f);
        if (recup > 0) {
            recup--;
            // se acerca despacio
            if (distanceTo(o) > 4.0 && t % 10 == 0) getNavigation().moveTo(o, 0.8 * velocidad());
            return;
        }
        double d = distanceTo(o);
        Atq next = elegir(d);
        if (next != null) {
            empezar(sl, next, o);
            return;
        }
        if (t % 8 == 0) getNavigation().moveTo(o, velocidad());
        if (--burla < -600 && random.nextInt(400) == 0) {
            burla = 0;
            habla(sl, fase() == 3 ? 9 : 8);
        }
    }

    private double velocidad() {
        return switch (fase()) {
            case 1 -> 1.0;
            case 2 -> 1.25;
            default -> 1.45;
        };
    }

    private void actualizarJugadores(ServerLevel sl, List<ServerPlayer> vivos) {
        Set<ServerPlayer> enBarra = new HashSet<>(barra.getPlayers());
        for (ServerPlayer p : vivos) if (!enBarra.contains(p)) barra.addPlayer(p);
        for (ServerPlayer p : enBarra) if (!vivos.contains(p)) barra.removePlayer(p);
    }

    private void mirar(Entity o, float max) {
        double dx = o.getX() - getX(), dz = o.getZ() - getZ();
        float want = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f;
        float cur = getYRot();
        float nyaw = cur + Mth.clamp(Mth.wrapDegrees(want - cur), -max, max);
        setYRot(nyaw);
        setYBodyRot(nyaw);
        setYHeadRot(nyaw);
        getLookControl().setLookAt(o, 30f, 30f);
    }

    private Vec3 adelante() {
        float r = getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(r), 0, Mth.cos(r));
    }

    // ------------------------------------------------------------------ ataques

    enum Atq {
        //            anim                    t   cd   min  max   fases  peso
        COMBO3(ReyAnims.COMBO3, 44, 50, 0, 5.5, 0b011, 4),
        SALTO(ReyAnims.SALTO, 40, 130, 5, 18, 0b111, 3),
        GIRO(ReyAnims.GIRO, 36, 110, 0, 4.5, 0b111, 2),
        GUARDIA(0, 60, 320, 0, 6, 0b011, 1.5),
        TAJO(ReyAnims.TAJO_LEJOS, 28, 90, 5, 26, 0b111, 2.5),
        COMBO5(ReyAnims.COMBO5, 50, 70, 0, 5.5, 0b110, 4),
        ABANICO(ReyAnims.ABANICO, 30, 150, 4, 18, 0b110, 3),
        EMBESTIDA(0, 46, 190, 6, 22, 0b110, 3),
        PILARES(ReyAnims.PILARES, 48, 210, 0, 30, 0b110, 2.5),
        BOLAS(ReyAnims.BOLAS, 26, 130, 6, 30, 0b110, 2.5),
        COMBO8(ReyAnims.COMBO8, 58, 60, 0, 5.5, 0b100, 5),
        GEISER(ReyAnims.GEISER, 40, 190, 0, 30, 0b100, 3),
        GOLPE_FINAL(ReyAnims.GOLPE_FINAL, 92, 900, 0, 40, 0b100, 100);

        final int anim, dur, cd;
        final double min, max, peso;
        final int fases;

        Atq(int anim, int dur, int cd, double min, double max, int fases, double peso) {
            this.anim = anim;
            this.dur = dur;
            this.cd = cd;
            this.min = min;
            this.max = max;
            this.fases = fases;
            this.peso = peso;
        }
    }

    private Atq elegir(double d) {
        int f = fase();
        List<Atq> c = new ArrayList<>();
        double total = 0;
        for (Atq a : Atq.values()) {
            if ((a.fases & (1 << (f - 1))) == 0) continue;
            if (d < a.min || d > a.max) continue;
            if (cds.getOrDefault(a, 0) > tGlobal) continue;
            if (a == Atq.GOLPE_FINAL && getHealth() > VIDA / 3f * 0.85f && tGlobal < 600) continue;
            c.add(a);
            total += a.peso;
        }
        if (c.isEmpty()) return null;
        if (c.contains(Atq.GOLPE_FINAL)) return Atq.GOLPE_FINAL;
        double r = random.nextDouble() * total;
        for (Atq a : c) {
            r -= a.peso;
            if (r <= 0) return a;
        }
        return c.get(c.size() - 1);
    }

    private int tGlobal;

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide) tGlobal++;
    }

    private void empezar(ServerLevel sl, Atq a, LivingEntity o) {
        atq = a;
        at = 0;
        tocados.clear();
        golpesGiro.clear();
        puntos.clear();
        contra = false;
        cds.put(a, tGlobal + a.cd);
        getNavigation().stop();
        if (a.anim > 0) anim(a.anim);
        destino = o.position();
        dir = adelante();
        alturaInicio = getY();
    }

    private void acabar() {
        if (atq == Atq.GOLPE_FINAL) {
            setNoGravity(false);
            atq = null;
            anim(ReyAnims.ATURDIDO);
            cambiar(Estado.ATURDIDO);
            return;
        }
        atq = null;
        setNoGravity(false);
        recup = switch (fase()) {
            case 1 -> 14 + random.nextInt(12);
            case 2 -> 8 + random.nextInt(9);
            default -> 3 + random.nextInt(6);
        };
    }

    private void tickAtaque(ServerLevel sl, LivingEntity o, List<ServerPlayer> vivos) {
        Atq a = atq;
        int k = at++;
        boolean fuego = fase() >= 2;
        switch (a) {
            case COMBO3 -> {
                if (k < 8 || (k > 13 && k < 18) || (k > 23 && k < 28)) mirar(o, 18f);
                if (k == 6 || k == 16) empujar(0.35);
                if (k == 27) empujar(0.45);
                if (k == 10 || k == 20) tajo(sl, vivos, 4.8, 150, 30, fuego, 0.6, 0);
                if (k == 31) {
                    tajo(sl, vivos, 5.3, 95, 42, fuego, 0.9, 0.25);
                    golpeSuelo(sl, position().add(adelante().scale(2.6)), 1.6f);
                }
            }
            case COMBO5 -> {
                if (k % 6 < 3) mirar(o, 20f);
                int[] hits = {7, 13, 20, 26, 36};
                for (int i = 0; i < hits.length; i++) {
                    if (k == hits[i] - 3) empujar(i == 4 ? 0.5 : 0.32);
                    if (k == hits[i]) {
                        tajo(sl, vivos, 4.8, i == 4 ? 100 : 140, i == 4 ? 40 : 26, true, 0.6, i == 4 ? 0.3 : 0);
                        if (i == 4) golpeSuelo(sl, position().add(adelante().scale(2.6)), 1.8f);
                    }
                }
            }
            case COMBO8 -> {
                mirar(o, 30f);
                int[] hits = {5, 10, 15, 21, 26, 32, 37, 42};
                for (int i = 0; i < hits.length; i++) {
                    if (k == hits[i] - 2) empujar(0.28);
                    if (k == hits[i]) tajo(sl, vivos, 4.8, 150, i == 7 ? 32 : 20, true, i == 7 ? 1.0 : 0.3, i == 7 ? 0.35 : 0);
                }
                if (k == 42) golpeSuelo(sl, position().add(adelante().scale(2.5)), 2f);
            }
            case SALTO -> salto(sl, o, vivos, k, fuego);
            case GIRO -> {
                if (k == 4) sonido(sl, SoundEvents.TRIDENT_RIPTIDE_2, 1.2f, 0.7f);
                if (k >= 6 && k <= 26 && k % 5 == 1) {
                    for (ServerPlayer p : vivos) {
                        if (p.distanceTo(this) > 4.4) continue;
                        int n = golpesGiro.getOrDefault(p.getUUID(), 0);
                        if (n >= 3) continue;
                        if (golpear(sl, p, 18, fuego, 0.5, 0.1, false)) golpesGiro.put(p.getUUID(), n + 1);
                    }
                    anillo(sl, fuego ? ParticleTypes.FLAME : ParticleTypes.SWEEP_ATTACK, 3.5, getY() + 1.4, fuego ? 18 : 6);
                    sonido(sl, SoundEvents.PLAYER_ATTACK_SWEEP, 1.0f, 0.6f);
                }
                if (k == 26 && fase() >= 2) {
                    // deja un anillo ardiendo
                    zonas.add(new double[]{getX(), getY(), getZ(), 2.5, 5.0, 90, 1});
                    aviso(sl, Net.Aviso.ANILLO, getX(), getY(), getZ(), 5.0f, 2.5f, 0, 90, Net.Aviso.NARANJA);
                }
            }
            case GUARDIA -> {
                mirar(o, 12f);
                if (contra) {
                    // contraataque (la animacion ya esta puesta)
                    if (atqContraT == 6) tajo(sl, vivos, 5.6, 75, 48, fuego, 1.6, 0.3);
                    atqContraT++;
                    if (atqContraT >= ReyAnims.TICKS[ReyAnims.CONTRAATAQUE]) acabar();
                    return;
                }
                if (k >= a.dur) acabar();
                return;
            }
            case TAJO -> {
                if (k < 10) mirar(o, 20f);
                if (k == 12) {
                    int n = fase() == 3 ? 3 : 1;
                    for (int i = 0; i < n; i++) {
                        float off = (i - (n - 1) / 2f) * 16f;
                        lanzar(sl, ReyProyectil.TAJO, o, off, 1.15, fase() == 1 ? 34 : 30);
                    }
                    sonido(sl, SoundEvents.PLAYER_ATTACK_SWEEP, 1.5f, 0.45f);
                }
            }
            case ABANICO -> {
                if (k == 0) aviso(sl, Net.Aviso.SECTOR, getX(), suelo(), getZ(), 18f, 80f, getYRot(), 13, Net.Aviso.ROJO);
                if (k < 10) mirar(o, 10f);
                if (k == 13) {
                    for (int i = -2; i <= 2; i++) lanzar(sl, ReyProyectil.OLA, o, i * 18f, 0.85, 30);
                    sonido(sl, SoundEvents.BLAZE_SHOOT, 1.4f, 0.6f);
                    sonido(sl, SoundEvents.FIRECHARGE_USE, 1.2f, 0.5f);
                }
            }
            case BOLAS -> {
                mirar(o, 25f);
                if (k == 7 || k == 15) {
                    for (int i = -1; i <= 1; i++) lanzar(sl, ReyProyectil.BOLA, o, i * 8f, 0.75, 22);
                    sonido(sl, SoundEvents.BLAZE_SHOOT, 1.2f, 0.7f);
                }
            }
            case EMBESTIDA -> embestida(sl, o, vivos, k);
            case PILARES, GEISER -> pilares(sl, o, vivos, k, a == Atq.GEISER);
            case GOLPE_FINAL -> golpeFinal(sl, o, vivos, k);
        }
        if (atq != null && at >= atq.dur) acabar();
    }

    private int atqContraT;

    private void empujar(double v) {
        Vec3 f = adelante().scale(v);
        setDeltaMovement(getDeltaMovement().add(f.x, 0.02, f.z));
        hasImpulse = true;
    }

    private double suelo() {
        Sala s = sala();
        if (s != null && level().dimension() == Level.OVERWORLD) return s.floorAt(getX(), getZ());
        return Math.floor(getY());
    }

    // --- salto con golpe al suelo
    private void salto(ServerLevel sl, LivingEntity o, List<ServerPlayer> vivos, int k, boolean fuego) {
        if (k < 10) mirar(o, 25f);
        if (k == 0 || k == 10) {
            // donde caera: donde esta el objetivo (un poco adelantado)
            Vec3 v = o.getDeltaMovement().multiply(6, 0, 6);
            destino = o.position().add(v);
            Sala s = sala();
            if (s != null && level().dimension() == Level.OVERWORLD) {
                double x = Mth.clamp(destino.x, s.t.x0() + 2, s.t.x1() - 1), z = Mth.clamp(destino.z, s.t.z0() + 6, s.t.z1() - 1);
                destino = new Vec3(x, s.floorAt(x, z), z);
            }
            if (k == 10) aviso(sl, Net.Aviso.CIRCULO, destino.x, destino.y, destino.z, 4.6f, 0, 0, 14, Net.Aviso.ROJO);
        }
        if (k == 12) {
            alturaInicio = getY();
            dir = position();
            setNoGravity(true);
            this.noPhysics = true;
            sonido(sl, SoundEvents.RAVAGER_ROAR, 0.6f, 1.4f);
        }
        if (k > 12 && k <= 24) {
            double q = (k - 12) / 12.0;
            double x = Mth.lerp(q, dir.x, destino.x), z = Mth.lerp(q, dir.z, destino.z);
            double y = Mth.lerp(q, alturaInicio, destino.y) + Math.sin(q * Math.PI) * 4.5;
            setPos(x, y, z);
            setDeltaMovement(Vec3.ZERO);
        }
        if (k == 24) {
            setNoGravity(false);
            this.noPhysics = false;
            setPos(destino.x, destino.y, destino.z);
            for (ServerPlayer p : vivos) {
                double d = p.position().distanceTo(destino);
                if (d < 4.6) golpear(sl, p, d < 2.0 ? 52 : 40, fuego, 1.3, 0.6, true);
            }
            golpeSuelo(sl, destino, 4.6f);
        }
    }

    // --- embestida (fase 2 y 3)
    private void embestida(ServerLevel sl, LivingEntity o, List<ServerPlayer> vivos, int k) {
        if (k < 14) {
            mirar(o, 30f);
            setPose(POSE_CARGA);
            if (k == 0) {
                sonido(sl, SoundEvents.RAVAGER_ROAR, 1.0f, 0.8f);
            }
            if (k == 13) {
                dir = adelante();
                aviso(sl, Net.Aviso.LINEA, getX(), suelo(), getZ(), 18f, 3.2f, getYRot(), 6, Net.Aviso.ROJO);
            }
            return;
        }
        if (k == 14) {
            dir = adelante();
            aviso(sl, Net.Aviso.LINEA, getX(), suelo(), getZ(), 18f, 3.2f, getYRot(), 4, Net.Aviso.ROJO);
        }
        if (k < 20) return;
        if (k < 38) {
            setPose(POSE_CARGA);
            setDeltaMovement(dir.x * 1.05, getDeltaMovement().y, dir.z * 1.05);
            hasImpulse = true;
            puntos.add(position());
            sl.sendParticles(fase() == 3 ? ParticleTypes.LAVA : ParticleTypes.FLAME, getX(), getY() + 0.2, getZ(), 6, 0.5, 0.1, 0.5, 0.02);
            for (ServerPlayer p : vivos)
                if (p.distanceTo(this) < 2.4 && tocados.add(p.getUUID())) golpear(sl, p, 40, true, 1.7, 0.4, false);
            if (k % 4 == 0) sonido(sl, SoundEvents.IRON_GOLEM_STEP, 1.2f, 0.6f);
            if (horizontalCollision && k > 22) {
                // choca con la pared: se aturde un momento
                golpeSuelo(sl, position().add(dir.scale(1.2)), 2f);
                sonido(sl, SoundEvents.ANVIL_LAND, 1.0f, 0.5f);
                at = 38;
            }
            return;
        }
        if (k == 38) {
            setPose(POSE_PIE);
            setDeltaMovement(Vec3.ZERO);
            // estela de fuego que quema un rato
            for (int i = 0; i < puntos.size(); i += 2) {
                Vec3 p = puntos.get(i);
                zonas.add(new double[]{p.x, p.y, p.z, 0, 1.3, 70, 1});
            }
        }
    }

    // --- pilares de fuego (fase 2) y geiseres de lava (fase 3)
    private void pilares(ServerLevel sl, LivingEntity o, List<ServerPlayer> vivos, int k, boolean lava) {
        int impacto = lava ? 36 : 42;
        float r = lava ? 2.2f : 1.8f;
        if (k == 0) {
            puntos.clear();
            for (ServerPlayer p : vivos) puntos.add(new Vec3(p.getX(), suelo(p), p.getZ()));
            int extra = lava ? 6 : 3;
            for (int i = 0; i < extra; i++) {
                double a = random.nextDouble() * Math.PI * 2, d = 3 + random.nextDouble() * 9;
                double x = getX() + Math.cos(a) * d, z = getZ() + Math.sin(a) * d;
                puntos.add(new Vec3(x, suelo(x, z), z));
            }
            for (Vec3 p : puntos) aviso(sl, Net.Aviso.CIRCULO, p.x, p.y, p.z, r, 0, 0, impacto, lava ? Net.Aviso.NARANJA : Net.Aviso.ROJO);
        }
        if (k < 12) mirar(o, 20f);
        if (k == 15) {
            golpeSuelo(sl, position().add(adelante().scale(1.5)), 1.5f);
            sonido(sl, SoundEvents.BLAZE_SHOOT, 1.0f, 0.5f);
        }
        if (k == impacto) {
            for (Vec3 c : puntos) {
                for (int y = 0; y < 7; y++)
                    sl.sendParticles(lava ? ParticleTypes.LAVA : ParticleTypes.FLAME, c.x, c.y + y * 0.8, c.z, lava ? 4 : 10, 0.4, 0.3, 0.4, 0.04);
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, c.x, c.y + 1, c.z, 12, 0.5, 1.5, 0.5, 0.05);
                sl.playSound(null, c.x, c.y, c.z, lava ? SoundEvents.LAVA_POP : SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.5f, 0.6f);
                for (ServerPlayer p : vivos)
                    if (Math.hypot(p.getX() - c.x, p.getZ() - c.z) < r && p.getY() < c.y + 3 && tocados.add(p.getUUID()))
                        golpear(sl, p, lava ? 40 : 34, true, 0, lava ? 1.1 : 0.9, true);
            }
            sonido(sl, lava ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.GENERIC_EXPLODE, 1.0f, 0.7f);
        }
    }

    private double suelo(Entity e) {
        return suelo(e.getX(), e.getZ());
    }

    private double suelo(double x, double z) {
        Sala s = sala();
        if (s != null && level().dimension() == Level.OVERWORLD) return s.floorAt(x, z);
        return Math.floor(getY());
    }

    // --- golpe final (fase 3): solo se salva quien este en la zona dorada
    private void golpeFinal(ServerLevel sl, LivingEntity o, List<ServerPlayer> vivos, int k) {
        double R = 21;
        if (k == 0) {
            habla(sl, 9);
            alturaInicio = getY();
            dir = position();
        }
        if (k == 16) {
            setNoGravity(true);
            this.noPhysics = true;
            Vec3 c = centroArena();
            aviso(sl, Net.Aviso.CIRCULO, c.x, suelo(c.x, c.z), c.z, (float) R, 0, 0, 58, Net.Aviso.ROJO);
            puntos.clear();
            int n = vivos.size() > 2 ? 2 : 1;
            for (int i = 0; i < n; i++) {
                double a = random.nextDouble() * Math.PI * 2, d = 7 + random.nextDouble() * 8;
                Vec3 p = new Vec3(c.x + Math.cos(a) * d, 0, c.z + Math.sin(a) * d);
                p = new Vec3(p.x, suelo(p.x, p.z), p.z);
                puntos.add(p);
                aviso(sl, Net.Aviso.CIRCULO, p.x, p.y, p.z, 3.2f, 0, 0, 58, Net.Aviso.ORO);
            }
            sonido(sl, SoundEvents.WITHER_SPAWN, 0.6f, 0.6f);
        }
        if (k > 16 && k <= 28) setPos(dir.x, alturaInicio + (k - 16) / 12.0 * 6.5, dir.z);
        if (k > 28 && k < 70) {
            setPos(dir.x, alturaInicio + 6.5 + Math.sin(k * 0.3) * 0.15, dir.z);
            if (k % 3 == 0) sl.sendParticles(ParticleTypes.LAVA, getX(), getY() + 2.5, getZ(), 3, 0.6, 0.4, 0.6, 0.02);
            if (k % 10 == 0) sonido(sl, SoundEvents.FIRE_AMBIENT, 2f, 0.5f);
        }
        if (k >= 70 && k < 74) setPos(dir.x, Mth.lerp((k - 69) / 4.0, alturaInicio + 6.5, alturaInicio), dir.z);
        if (k == 74) {
            setPos(dir.x, alturaInicio, dir.z);
            setNoGravity(false);
            this.noPhysics = false;
            for (ServerPlayer p : vivos) {
                boolean safe = false;
                for (Vec3 s : puntos) if (Math.hypot(p.getX() - s.x, p.getZ() - s.z) < 3.4) safe = true;
                if (!safe) golpear(sl, p, 80, true, 1.0, 1.0, true);
            }
            golpeSuelo(sl, position(), 8f);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 0.5, getZ(), 2, 1, 0.2, 1, 0);
            sonido(sl, SoundEvents.GENERIC_EXPLODE, 2.0f, 0.5f);
            sonido(sl, SoundEvents.DRAGON_FIREBALL_EXPLODE, 2.0f, 0.6f);
        }
    }

    /** Centro del sitio donde se pelea (sala o plataforma). */
    private Vec3 centroArena() {
        Sala s = sala();
        if (s != null && level().dimension() == Level.OVERWORLD) {
            Vec3 c = s.center();
            return new Vec3(c.x, c.y, c.z + 3);
        }
        return Volcan.CENTRO;
    }

    // ------------------------------------------------------------------ golpes

    private DamageSource fuente(boolean area) {
        var reg = level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        return new DamageSource(reg.getHolderOrThrow(area ? AREA : GOLPE), this);
    }

    private float mulFase() {
        return switch (fase()) {
            case 1 -> 1f;
            case 2 -> 1.12f;
            default -> 1.25f;
        };
    }

    private float mulJugadores() {
        return 1f + 0.15f * Math.max(0, jugadores.size() - 1);
    }

    /** Dano bruto (antes de armadura: 38 = unos 3,5 corazones con netherite y proteccion IV). */
    boolean golpear(ServerLevel sl, ServerPlayer p, float bruto, boolean fuego, double kb, double up, boolean area) {
        if (!valido(p)) return false;
        p.invulnerableTime = 0;
        boolean ok = p.hurt(fuente(area), bruto * mulFase() * mulJugadores());
        if (ok) {
            if (fuego) p.setSecondsOnFire(fase() >= 3 ? 6 : 4);
            if (!area && p.isBlocking() && random.nextFloat() < 0.35f) p.disableShield(true);
        }
        if (kb > 0) p.knockback(kb, getX() - p.getX(), getZ() - p.getZ());
        if (up > 0) p.setDeltaMovement(p.getDeltaMovement().add(0, up, 0));
        p.hurtMarked = true;
        return ok;
    }

    private void tajo(ServerLevel sl, List<ServerPlayer> vivos, double reach, double arco, float bruto, boolean fuego, double kb, double up) {
        Vec3 f = adelante();
        for (ServerPlayer p : vivos) {
            Vec3 d = p.position().subtract(position());
            double dist = Math.sqrt(d.x * d.x + d.z * d.z);
            if (dist > reach + p.getBbWidth() / 2 || d.y < -2 || d.y > 4) continue;
            double ang = Math.toDegrees(Math.acos(Mth.clamp((d.x * f.x + d.z * f.z) / Math.max(1e-4, dist), -1, 1)));
            if (dist > 1.2 && ang > arco / 2) continue;
            golpear(sl, p, bruto, fuego, kb, up, false);
        }
        // particulas del tajo
        int n = 9;
        for (int i = 0; i < n; i++) {
            double a = Math.toRadians(getYRot() + 90 + (i - (n - 1) / 2.0) * arco / (n - 1));
            double x = getX() + Math.cos(a) * reach * 0.7, z = getZ() + Math.sin(a) * reach * 0.7;
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, x, getY() + 1.5, z, 1, 0, 0, 0, 0);
            if (fuego) sl.sendParticles(fase() == 3 ? ParticleTypes.LAVA : ParticleTypes.FLAME, x, getY() + 1.5, z, 3, 0.2, 0.2, 0.2, 0.01);
        }
        sonido(sl, SoundEvents.PLAYER_ATTACK_SWEEP, 1.4f, 0.55f + random.nextFloat() * 0.1f);
        if (fuego) sonido(sl, SoundEvents.FIRECHARGE_USE, 0.6f, 1.2f);
    }

    private void golpeSuelo(ServerLevel sl, Vec3 c, float r) {
        BlockState st = sl.getBlockState(BlockPos.containing(c.x, c.y - 0.5, c.z));
        if (st.isAir()) st = net.minecraft.world.level.block.Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        ParticleOptions crack = new BlockParticleOption(ParticleTypes.BLOCK, st);
        int n = (int) (r * 10);
        for (int i = 0; i < n; i++) {
            double a = random.nextDouble() * Math.PI * 2, d = random.nextDouble() * r;
            sl.sendParticles(crack, c.x + Math.cos(a) * d, c.y + 0.1, c.z + Math.sin(a) * d, 2, 0.1, 0.1, 0.1, 0.15);
        }
        sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, c.x, c.y + 0.2, c.z, (int) (r * 2), r * 0.4, 0.1, r * 0.4, 0.02);
        if (fase() >= 2) sl.sendParticles(fase() == 3 ? ParticleTypes.LAVA : ParticleTypes.FLAME, c.x, c.y + 0.2, c.z, (int) (r * 4), r * 0.4, 0.1, r * 0.4, 0.05);
        sl.playSound(null, c.x, c.y, c.z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.8f, 1.3f);
        sl.playSound(null, c.x, c.y, c.z, SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.6f, 0.5f);
    }

    private void anillo(ServerLevel sl, ParticleOptions p, double r, double y, int n) {
        for (int i = 0; i < n; i++) {
            double a = i * Math.PI * 2 / n;
            sl.sendParticles(p, getX() + Math.cos(a) * r, y, getZ() + Math.sin(a) * r, 1, 0, 0, 0, 0);
        }
    }

    private void lanzar(ServerLevel sl, int tipo, LivingEntity o, float offDeg, double vel, float bruto) {
        ReyProyectil pr = new ReyProyectil(CiudadelaMod.REY_PROYECTIL.get(), sl);
        pr.setOwner(this);
        double y = tipo == ReyProyectil.OLA ? getY() + 0.3 : getY() + 1.8;
        pr.setPos(getX(), y, getZ());
        Vec3 aim = o.position().add(0, tipo == ReyProyectil.OLA ? 0.3 : o.getBbHeight() * 0.5, 0).subtract(pr.position());
        double yaw = Math.atan2(aim.z, aim.x) + Math.toRadians(offDeg);
        double h = Math.sqrt(aim.x * aim.x + aim.z * aim.z);
        double py = tipo == ReyProyectil.OLA ? 0 : Mth.clamp(aim.y / Math.max(1, h), -0.4, 0.4);
        Vec3 v = new Vec3(Math.cos(yaw), py, Math.sin(yaw)).normalize().scale(vel);
        pr.preparar(tipo, v, bruto * mulFase() * mulJugadores(), fase());
        sl.addFreshEntity(pr);
    }

    private void aviso(ServerLevel sl, int forma, double x, double y, double z, float a, float b, float ang, int dur, int color) {
        Net.Aviso m = new Net.Aviso(forma, x, y, z, a, b, ang, dur, color);
        for (ServerPlayer p : sl.players()) if (p.distanceToSqr(x, y, z) < 90 * 90) Net.to(p, m);
    }

    /** Zonas que queman (anillo del giro, estela de la embestida). */
    private void tickZonas(ServerLevel sl) {
        if (zonas.isEmpty()) return;
        Iterator<double[]> it = zonas.iterator();
        while (it.hasNext()) {
            double[] z = it.next();
            z[5]--;
            if (z[5] <= 0) {
                it.remove();
                continue;
            }
            if (((int) z[5]) % 4 == 0) {
                double a = random.nextDouble() * Math.PI * 2, d = z[3] + random.nextDouble() * (z[4] - z[3]);
                sl.sendParticles(fase() == 3 ? ParticleTypes.LAVA : ParticleTypes.FLAME, z[0] + Math.cos(a) * d, z[1] + 0.1, z[2] + Math.sin(a) * d, 2, 0.1, 0.05, 0.1, 0.01);
            }
            if (((int) z[5]) % 10 == 0)
                for (ServerPlayer p : jugadoresVivos(sl)) {
                    double d = Math.hypot(p.getX() - z[0], p.getZ() - z[2]);
                    if (d >= z[3] && d <= z[4] && Math.abs(p.getY() - z[1]) < 2) {
                        p.invulnerableTime = 0;
                        p.hurt(fuente(true), 6 * mulFase());
                        p.setSecondsOnFire(3);
                    }
                }
        }
    }

    private void sonido(ServerLevel sl, SoundEvent s, float vol, float pitch) {
        sl.playSound(null, getX(), getY() + 1.5, getZ(), s, SoundSource.HOSTILE, vol, pitch);
    }

    // ------------------------------------------------------------------ recibir dano

    @Override
    public boolean hurt(DamageSource src, float amount) {
        if (level().isClientSide) return false;
        if (src.is(DamageTypes.GENERIC_KILL)) {
            // /kill: vuelve al trono como nuevo
            if (level() instanceof ServerLevel sl) reiniciar(sl, "/kill");
            return false;
        }
        if (src.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            if (level() instanceof ServerLevel sl) reiniciar(sl, "vacio");
            return false;
        }
        if (src.is(DamageTypeTags.IS_FIRE) || src.is(DamageTypes.LAVA) || src.is(DamageTypes.IN_WALL) || src.is(DamageTypes.CRAMMING)
                || src.is(DamageTypes.DROWN) || src.is(DamageTypes.FALL)) return false;
        Entity att = src.getEntity();
        if (estado != Estado.LUCHA && estado != Estado.ATURDIDO) {
            if (att instanceof Player && level() instanceof ServerLevel sl) {
                sonido(sl, SoundEvents.ANVIL_PLACE, 0.4f, 1.8f);
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, getX(), getY() + 1.6, getZ(), 6, 0.4, 0.5, 0.4, 0.2);
            }
            return false;
        }
        float mul = (VIDA / 3f) / (VIDA_FASE * (1f + 0.6f * Math.max(0, jugadores.size() - 1)));
        if (src.is(DamageTypeTags.IS_PROJECTILE)) mul *= 0.25f;
        if (src.is(DamageTypeTags.IS_EXPLOSION)) mul *= 0.3f;
        if (att == null) mul *= 0.5f;
        if (estado == Estado.ATURDIDO) mul *= 1.5f;
        // en guardia para casi todo y contraataca
        if (atq == Atq.GUARDIA && !contra && att instanceof Player && !src.is(DamageTypeTags.IS_PROJECTILE) && level() instanceof ServerLevel sl) {
            mul *= 0.15f;
            contra = true;
            atqContraT = 0;
            anim(ReyAnims.CONTRAATAQUE);
            setPose(POSE_PIE);
            sonido(sl, SoundEvents.ANVIL_PLACE, 0.7f, 1.6f);
            sonido(sl, SoundEvents.SHIELD_BLOCK, 1.0f, 0.6f);
            sl.sendParticles(ParticleTypes.CRIT, getX(), getY() + 1.8, getZ(), 20, 0.5, 0.4, 0.5, 0.3);
        }
        float dmg = amount * mul;
        float hp = getHealth();
        int f = fase();
        float suelo = f == 1 ? VIDA * 2f / 3f : (f == 2 ? VIDA / 3f : 0f);
        if (hp - dmg <= suelo + 0.01f) {
            boolean ok = super.hurt(src, Math.max(0f, hp - suelo - 0.5f));
            if (level() instanceof ServerLevel sl) {
                if (f < 3) {
                    setHealth(suelo);
                    empezarCambio(sl);
                } else {
                    setHealth(1f);
                    empezarMuerte(sl);
                }
            }
            return ok;
        }
        return super.hurt(src, dmg);
    }

    @Override
    public void die(DamageSource src) {
        // nunca muere "a lo vanilla": lo controla empezarMuerte()
        if (level() instanceof ServerLevel sl && estado != Estado.MUERTE) {
            setHealth(1f);
            empezarMuerte(sl);
        }
    }

    // ------------------------------------------------------------------ cambios de fase

    private void empezarCambio(ServerLevel sl) {
        atq = null;
        setNoGravity(false);
        this.noPhysics = false;
        setPose(POSE_PIE);
        getNavigation().stop();
        anim(ReyAnims.GRITO);
        cambiar(Estado.CAMBIO);
        habla(sl, fase() == 1 ? 4 : 5);
    }

    private void cambio(ServerLevel sl) {
        getNavigation().stop();
        setDeltaMovement(0, getDeltaMovement().y, 0);
        if (t == 18) {
            int nf = fase() + 1;
            this.entityData.set(FASE, nf);
            sonido(sl, SoundEvents.RAVAGER_ROAR, 2.0f, 0.5f);
            sonido(sl, SoundEvents.ENDER_DRAGON_GROWL, 1.0f, 0.7f);
            sonido(sl, nf == 2 ? SoundEvents.BLAZE_SHOOT : SoundEvents.BUCKET_EMPTY_LAVA, 1.5f, 0.5f);
            // onda que empuja a todos
            for (ServerPlayer p : jugadoresVivos(sl)) {
                double d = p.distanceTo(this);
                if (d < 14) {
                    p.invulnerableTime = 0;
                    p.hurt(fuente(true), 10f);
                    p.knockback(2.2 * (1 - d / 16), getX() - p.getX(), getZ() - p.getZ());
                    p.setDeltaMovement(p.getDeltaMovement().add(0, 0.5, 0));
                    p.hurtMarked = true;
                }
            }
            for (int r = 1; r <= 6; r++) anillo(sl, nf == 2 ? ParticleTypes.FLAME : ParticleTypes.LAVA, r * 1.6, getY() + 0.3, 12 + r * 6);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1, getZ(), 1, 0, 0, 0, 0);
        }
        if (t >= ReyAnims.TICKS[ReyAnims.GRITO]) {
            if (fase() == 3 && level().dimension() == Level.OVERWORLD && Volcan.disponible(sl)) {
                // a los volcanes
                Volcan.llevar(sl, this, jugadoresVivos(sl));
                return;
            }
            recup = 6;
            cambiar(Estado.LUCHA);
        }
    }

    private void aturdido(ServerLevel sl) {
        getNavigation().stop();
        if (t % 10 == 0) sl.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 2.4, getZ(), 6, 0.4, 0.2, 0.4, 0.01);
        if (t >= ReyAnims.TICKS[ReyAnims.ATURDIDO]) {
            recup = 4;
            cambiar(Estado.LUCHA);
        }
    }

    /** Llamado por Volcan cuando el rey ya esta en la plataforma. */
    public void enVolcan(ServerLevel sl, Collection<UUID> quienes) {
        jugadores.clear();
        jugadores.addAll(quienes);
        this.entityData.set(FASE, 3);
        setFlag(F_LUCHA, true);
        barra.setVisible(true);
        for (ServerPlayer p : jugadoresVivos(sl)) barra.addPlayer(p);
        recup = 40;
        cambiar(Estado.LUCHA);
    }

    public void setVida(float hp) {
        setHealth(hp);
    }

    // ------------------------------------------------------------------ muerte

    private void empezarMuerte(ServerLevel sl) {
        atq = null;
        setNoGravity(false);
        this.noPhysics = false;
        getNavigation().stop();
        setPose(POSE_PIE);
        anim(ReyAnims.MUERTE);
        cambiar(Estado.MUERTE);
        habla(sl, 6);
        sonido(sl, SoundEvents.WITHER_DEATH, 0.6f, 0.5f);
    }

    private void muerte(ServerLevel sl) {
        getNavigation().stop();
        setDeltaMovement(0, getDeltaMovement().y, 0);
        if (t == 68) {
            setFlag(F_SIN_CORONA, true);
            sonido(sl, SoundEvents.ARMOR_EQUIP_GOLD, 1.5f, 0.6f);
            sonido(sl, SoundEvents.CHAIN_BREAK, 1.0f, 0.5f);
        }
        if (t == 80) setFlag(F_CENIZA, true);
        if (t >= 80 && t <= 120 && t % 2 == 0) {
            sl.sendParticles(ParticleTypes.ASH, getX(), getY() + 1.2, getZ(), 30, 0.6, 0.9, 0.6, 0.02);
            sl.sendParticles(ParticleTypes.WHITE_ASH, getX(), getY() + 1.2, getZ(), 20, 0.6, 0.9, 0.6, 0.02);
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.0, getZ(), 3, 0.4, 0.6, 0.4, 0.01);
            if (t % 10 == 0) sonido(sl, SoundEvents.SAND_BREAK, 1.0f, 0.5f);
        }
        if (t == 125) {
            barra.removeAllPlayers();
            barra.setVisible(false);
            Victoria.ganar(sl, this, jugadoresVivos(sl), new ArrayList<>(jugadores));
            discard();
        }
    }

    // ------------------------------------------------------------------ volver a empezar

    /** Todos han caido (o /kill): el rey vuelve a su trono con toda la vida y se abren las puertas. */
    public void reiniciar(ServerLevel sl, String why) {
        CiudadelaMod.LOGGER.info("[Ciudadela] El Rey vuelve a su trono ({})", why);
        barra.removeAllPlayers();
        barra.setVisible(false);
        atq = null;
        zonas.clear();
        jugadores.clear();
        Sala s = sala();
        if (s != null) {
            ServerLevel ow = sl.getServer().overworld();
            ReyEstado.get(sl.getServer()).set(s.key(), ReyEstado.DORMIDO);
            cerrando = false;
            puerta = 0;
            if (level().dimension() != Level.OVERWORLD) {
                // en los volcanes: este rey desaparece y aparece otro en el trono
                s.openAll(ow);
                Volcan.devolverTodos(sl);
                discard();
                ReyEventos.asegurarRey(ow, s);
                return;
            }
        }
        this.entityData.set(FASE, 1);
        setFlag(F_LUCHA, false);
        setFlag(F_HABLA, false);
        setFlag(F_SIN_CORONA, false);
        setFlag(F_CENIZA, false);
        anim(0);
        setHealth(getMaxHealth());
        if (home != null) moveTo(home.x, home.y, home.z, 0f, 0f);
        cambiar(Estado.DORMIDO);
    }

    private void tickPuertas(ServerLevel sl) {
        if (puerta < 0) return;
        Sala s = sala();
        if (s == null) {
            puerta = -1;
            return;
        }
        if (t % 4 != 0) return;
        ServerLevel ow = sl.getServer().overworld();
        if (s.gateStep(ow, puerta, cerrando)) puerta = -1;
        else puerta++;
    }

    /** Abre las puertas poco a poco (al ganar). */
    public static void abrirPuertas(ServerLevel ow, Sala s) {
        s.openAll(ow);
        for (int[] d : s.t.doors())
            ow.playSound(null, d[0] + 0.5, s.t.y() + 2, d[1] + 0.5, SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 1.2f, 0.6f);
    }

    @Override
    public void remove(RemovalReason reason) {
        barra.removeAllPlayers();
        super.remove(reason);
    }

    /** Para la prueba de la CI: que esta haciendo. */
    public String debug() {
        return estado + " fase " + fase() + " vida " + Math.round(getHealth()) + (atq != null ? " ataque " + atq + "@" + at : "")
                + (recup > 0 ? " recup " + recup : "") + " jugadores " + jugadores.size();
    }

    // para las pruebas y los comandos
    public void forzarFase(int f) {
        if (level() instanceof ServerLevel sl) {
            if (estado == Estado.DORMIDO || estado == Estado.INTRO) return;
            float target = f <= 1 ? VIDA : (f == 2 ? VIDA * 2f / 3f + 1 : VIDA / 3f + 1);
            setHealth(target);
            if (f > fase()) {
                this.entityData.set(FASE, f - 1);
                setHealth(f == 2 ? VIDA * 2f / 3f : VIDA / 3f);
                empezarCambio(sl);
            }
        }
    }

    public void forzarMuerte() {
        if (level() instanceof ServerLevel sl && estado != Estado.MUERTE) {
            setHealth(1f);
            empezarMuerte(sl);
        }
    }

    public static Vector3f colorFase(int f) {
        return switch (f) {
            case 2 -> new Vector3f(1f, 0.45f, 0.05f);
            case 3 -> new Vector3f(1f, 0.75f, 0.2f);
            default -> new Vector3f(0.8f, 0.9f, 1f);
        };
    }

    public static DustParticleOptions polvoOro() {
        return new DustParticleOptions(new Vector3f(1f, 0.82f, 0.25f), 1.2f);
    }
}
