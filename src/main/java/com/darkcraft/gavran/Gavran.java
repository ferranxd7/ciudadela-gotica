package com.darkcraft.gavran;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.cine.Cinematics;
import com.darkcraft.cine.Shot;
import com.darkcraft.net.Net;
import com.darkcraft.rey.ReyEstado;
import com.darkcraft.story.Logros;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * Ser Gavran, el campeon del Rey. Espera de rodillas en una rama del Arbol de la Corona, sobre la puerta de la
 * sala. Cuando alguien entra, la puerta se cierra, habla, salta a la tarima del trono y pelea. Parte B1: la
 * primera fase (el duelo de caballeros); al bajarle a media vida se retira a su rama y vuelve a empezar (la
 * segunda fase llega en la parte B2). La pelea la lleva el propio jefe en el servidor; el cliente pone las
 * animaciones, el brillo y las particulas.
 */
public class Gavran extends Monster {
    // ------------------------------------------------------------------ datos que ve el cliente
    private static final EntityDataAccessor<Integer> FASE = SynchedEntityData.defineId(Gavran.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> POSE = SynchedEntityData.defineId(Gavran.class, EntityDataSerializers.INT);
    /** Animacion de accion: (contador << 5) | id, en un solo valor (ver Rey). */
    private static final EntityDataAccessor<Integer> ANIM_N = SynchedEntityData.defineId(Gavran.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FLAGS = SynchedEntityData.defineId(Gavran.class, EntityDataSerializers.INT);
    /** Postura (0..100): al llenarse se le rompe la guardia. */
    private static final EntityDataAccessor<Integer> POSTURA = SynchedEntityData.defineId(Gavran.class, EntityDataSerializers.INT);

    public static final int POSE_PIE = 0, POSE_POSADO = 1, POSE_GUARDIA = 2, POSE_CARGA = 3;
    public static final int F_LUCHA = 1, F_INTRO = 2, F_HABLA = 4, F_HOJAS = 8, F_ALERTA = 16, F_CAMBIO = 32;
    /** En la fase 2 es mas grande (el arbol le ha hecho crecer). */
    public static final float ESCALA_F2 = 1.15f;

    public static final ResourceKey<DamageType> GOLPE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(DarkCraftMod.MODID, "gavran"));
    public static final ResourceKey<DamageType> AREA = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(DarkCraftMod.MODID, "gavran_area"));

    /** Vida "real" (el dano que recibe se escala): media para cada fase. */
    public static final float VIDA = 1000f;
    /** Vida efectiva de cada fase para un jugador solo (netherite afilada V: unos 2 minutos). */
    public static final float VIDA_FASE = 520f;
    private static final float DANO = 0.75f;
    /** Lo que dura cada frase (segundos, con el eco): para no pisarlas. */
    /** Linea de voz de Gavran en el paquete de voz (las del rey van de 1 a 13). */
    public static final int VOZ_BASE = com.darkcraft.rey.Voces.BASE_GAVRAN;

    // ------------------------------------------------------------------ cliente
    public final AnimationState posadoAnim = new AnimationState(), idleAnim = new AnimationState(),
            guardAnim = new AnimationState(), chargeAnim = new AnimationState(), talkAnim = new AnimationState();
    public final AnimationState[] actions = new AnimationState[GavranAnims.TICKS.length];
    private int actionId = 0, actionStart = -1000;
    public int hojasIni = -1, ultimoFx = -1;

    // ------------------------------------------------------------------ servidor
    public enum Estado { DORMIDO, INTRO, LUCHA, ATURDIDO, CAMBIO, FIN1, MUERTE }

    private Estado estado = Estado.DORMIDO;
    private int t;
    private int tGlobal;
    private Vec3 rama;
    private SalaArbol sala;
    private final Set<UUID> jugadores = new LinkedHashSet<>();
    private final ServerBossEvent barra = new ServerBossEvent(Component.translatable("entity.darkcraft.gavran"),
            BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
    private final Raices raices = new Raices(this);
    private Atq atq;
    private int at, recup;
    private final EnumMap<Atq, Integer> cds = new EnumMap<>(Atq.class);
    private LivingEntity objetivo;
    private int cambiaObjetivo;
    private Vec3 dir;
    private final Set<UUID> tocados = new HashSet<>();
    private final List<UUID> escuderos = new ArrayList<>();
    private boolean contra;
    private int contraT;
    private float postura;
    private int ultimoGolpeRecibido = -1000, ventanaHasta = -1000, hablaHasta = -1000, frase5 = -1000, burla;
    // salto: de donde, a donde, cuando empieza y cuanto dura
    private Vec3 saltoDe, saltoA;
    private int saltoIni = -1, saltoDur;
    private double saltoAlto;
    private Vec3 ultimaPos;
    private int atascado;
    private boolean muestra;
    /** Tras volver a su rama no despierta hasta que la sala se quede vacia (hay que salir y volver a entrar). */
    private boolean armado = true;
    // fase 2: juicios hechos (por vida), si toca uno ya, a quien tienen atrapado las raices, desde cuando esta en un
    // balcon, a donde salta, si ya dijo lo del corazon, y si el final (B2) es el de la fase 2
    private int juicios;
    private boolean juicioPendiente, corazonDicho, finFase2;
    private UUID atrapado;
    private int atrapadoHasta = -1, enBalcon = -1;
    private Vec3 destino;
    private final List<Vec3> trampas = new ArrayList<>();
    /** Muros del ultimo Juicio: {centro, direccion desde Gavran} (para la prueba de la CI). */
    private final List<Vec3[]> murosJuicio = new ArrayList<>();

    public Gavran(EntityType<? extends Gavran> type, Level level) {
        super(type, level);
        for (int i = 0; i < actions.length; i++) actions[i] = new AnimationState();
        this.xpReward = 0;
        this.setPersistenceRequired();
        this.barra.setVisible(false);
        this.setMaxUpStep(1.1f);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, VIDA)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.ATTACK_DAMAGE, 10)
                .add(Attributes.FOLLOW_RANGE, 64)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, 0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FASE, 1);
        this.entityData.define(POSE, POSE_POSADO);
        this.entityData.define(ANIM_N, 0);
        this.entityData.define(FLAGS, 0);
        this.entityData.define(POSTURA, 0);
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

    public int postura() {
        return this.entityData.get(POSTURA);
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

    public SalaArbol sala() {
        if (sala == null && level() instanceof ServerLevel sl) sala = SalaArbol.cerca(sl, getX(), getZ());
        return sala;
    }

    void setSala(SalaArbol s, Vec3 rama) {
        this.sala = s;
        this.rama = rama;
    }

    private void anim(int id) {
        int n = ((this.entityData.get(ANIM_N) >>> 5) + 1) & 0x3FFFFFF;
        this.entityData.set(ANIM_N, (n << 5) | (id & 31));
    }

    private void setPose(int p) {
        this.entityData.set(POSE, p);
    }

    // ------------------------------------------------------------------ cliente: animaciones

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (FASE.equals(key)) refreshDimensions();
        if (level().isClientSide && ANIM_N.equals(key)) {
            int id = this.entityData.get(ANIM_N) & 31;
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
        return actionId > 0 && (actionId == GavranAnims.MUERTE || actionId == GavranAnims.SUBIR
                || this.tickCount - actionStart <= GavranAnims.TICKS[actionId]);
    }

    public int actionId() {
        return acting() ? actionId : 0;
    }

    private void clientTick() {
        int p = pose();
        posadoAnim.animateWhen(p == POSE_POSADO, this.tickCount);
        guardAnim.animateWhen(p == POSE_GUARDIA, this.tickCount);
        chargeAnim.animateWhen(p == POSE_CARGA, this.tickCount);
        talkAnim.animateWhen(flag(F_HABLA), this.tickCount);
        idleAnim.startIfStopped(this.tickCount);
        if (actionId > 0 && actionId != GavranAnims.MUERTE && actionId != GavranAnims.SUBIR
                && this.tickCount - actionStart > GavranAnims.TICKS[actionId] + 2) {
            actions[actionId].stop();
            actionId = 0;
        }
        // al subir a la rama se queda en el aire con la pose del salto hasta que vuelve a estar de rodillas
        if (actionId == GavranAnims.SUBIR && p == POSE_POSADO) {
            actions[actionId].stop();
            actionId = 0;
        }
        if (flag(F_HOJAS) && hojasIni < 0) hojasIni = this.tickCount;
        if (!flag(F_HOJAS)) hojasIni = -1;
    }

    /** Transparencia al deshacerse en hojas (1 = normal). */
    public float alpha(float pt) {
        if (hojasIni < 0) return 1f;
        return Mth.clamp(1f - (this.tickCount - hojasIni + pt) / 50f, 0f, 1f);
    }

    // ------------------------------------------------------------------ cosas de Minecraft

    @Override
    protected void registerGoals() {
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
        return false;
    }

    @Override
    protected void pushEntities() {
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
        return SoundEvents.NETHERITE_BLOCK_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState st) {
        this.playSound(DarkCraftMod.SFX_GAVRAN_PASO.get(), 0.55f, 0.9f + this.random.nextFloat() * 0.2f);
    }

    @Override
    protected float getSoundVolume() {
        return 1.3f;
    }

    @Override
    public float getVoicePitch() {
        return 0.6f;
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(4.5, 2.5, 4.5);
    }

    @Override
    public EntityDimensions getDimensions(net.minecraft.world.entity.Pose p) {
        EntityDimensions d = super.getDimensions(p);
        return fase() >= 2 ? d.scale(ESCALA_F2) : d;
    }

    /** Cambia de fase (el tamano y la textura van con ella). */
    private void ponerFase(int f) {
        this.entityData.set(FASE, f);
        refreshDimensions();
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(f >= 2 ? 0.31 : 0.27);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (rama != null) {
            tag.putDouble("RamaX", rama.x);
            tag.putDouble("RamaY", rama.y);
            tag.putDouble("RamaZ", rama.z);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("RamaX")) rama = new Vec3(tag.getDouble("RamaX"), tag.getDouble("RamaY"), tag.getDouble("RamaZ"));
        // si el mundo se cerro en mitad de la pelea, vuelve a su rama (y a la fase 1)
        estado = Estado.DORMIDO;
        t = 0;
        ponerFase(1);
    }

    // ------------------------------------------------------------------ tick

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) clientTick();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide) tGlobal++;
    }

    /** Para la prueba de la CI: la fase con la que se ve en el escaparate. */
    public void faseMuestra(int f) {
        if (fase() != f) ponerFase(f);
    }

    /** Para la prueba de la CI: quieto en una pose/animacion concreta, sin pelear. */
    public void muestra(int pose, int anim, boolean habla) {
        muestra = true;
        setNoGravity(true);
        setPose(pose);
        setFlag(F_HABLA, habla);
        setFlag(F_LUCHA, false);
        if (anim >= 0) anim(anim);
    }

    @Override
    protected void customServerAiStep() {
        if (!(level() instanceof ServerLevel sl)) return;
        t++;
        if (muestra) {
            getNavigation().stop();
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        if (rama == null) rama = position();
        raices.tick(sl);
        tickSalto(sl);
        switch (estado) {
            case DORMIDO -> dormido(sl);
            case INTRO -> intro(sl);
            case LUCHA -> lucha(sl);
            case ATURDIDO -> aturdido(sl);
            case CAMBIO -> cambio(sl);
            case FIN1 -> fin1(sl);
            case MUERTE -> muerte(sl);
        }
        if (hablaHasta < tGlobal && flag(F_HABLA)) setFlag(F_HABLA, false);
        barra.setProgress(Mth.clamp(getHealth() / getMaxHealth(), 0f, 1f));
        if (postura > 0 && tGlobal - ultimoGolpeRecibido > 50) postura = Math.max(0, postura - 0.45f);
        int pv = Math.round(postura);
        if (pv != postura()) this.entityData.set(POSTURA, pv);
    }

    private void cambiar(Estado e) {
        estado = e;
        t = 0;
    }

    // ------------------------------------------------------------------ esperando en la rama

    private void posarEnRama() {
        setNoGravity(true);
        this.noPhysics = true;
        getNavigation().stop();
        setDeltaMovement(Vec3.ZERO);
        if (rama != null) {
            // de cara al trono (norte); la cabeza la mueve el control de mirada (en la entrada mira a quien entra)
            setPos(rama.x, rama.y, rama.z);
            setYRot(180f);
            setYBodyRot(180f);
            if (estado != Estado.INTRO) {
                setYHeadRot(180f);
                setXRot(0f);
            }
        }
    }

    private void dormido(ServerLevel sl) {
        setPose(POSE_POSADO);
        setFlag(F_LUCHA, false);
        setFlag(F_INTRO, false);
        setFlag(F_ALERTA, false);
        posarEnRama();
        if (getHealth() < getMaxHealth()) setHealth(getMaxHealth());
        if (t % 10 != 0) return;
        SalaArbol s = sala();
        if (s == null) return;
        // si se quedo marcada una pelea (el mundo se cerro o se descargo a mitad), se arregla: puerta abierta
        ReyEstado est = ReyEstado.get(sl.getServer());
        // ya cayo (se guardo a mitad de su muerte): no vuelve
        if (est.get(s.key()) == ReyEstado.MUERTO) {
            GavranEventos.abrirSala(sl, s);
            discard();
            return;
        }
        if (t % 40 == 0 && est.get(s.key()) == ReyEstado.LUCHA) {
            est.set(s.key(), ReyEstado.DORMIDO);
            GavranEventos.abrirSala(sl, s);
        }
        List<ServerPlayer> dentro = new ArrayList<>();
        for (ServerPlayer p : sl.players())
            if (valido(p) && s.dentro(p.position())) dentro.add(p);
        if (!armado) {
            if (dentro.isEmpty()) armado = true;
            return;
        }
        if (dentro.isEmpty()) return;
        empezarIntro(sl, s, dentro);
    }

    public static boolean valido(Player p) {
        return p.isAlive() && !p.isSpectator() && !p.isCreative();
    }

    List<ServerPlayer> vivos(ServerLevel sl) {
        List<ServerPlayer> l = new ArrayList<>();
        for (UUID id : jugadores) {
            ServerPlayer p = sl.getServer().getPlayerList().getPlayer(id);
            if (p != null && p.level() == sl && valido(p)) l.add(p);
        }
        return l;
    }

    // ------------------------------------------------------------------ la entrada: la puerta se cierra, habla y salta

    /**
     * Cuando dice cada frase de la entrada, salta, saluda y empieza la pelea (ticks): salen de lo que dura cada frase
     * (Voces), con una pausa entre una y otra, y la pelea no empieza hasta que acaba de hablar.
     */
    private static final int P = com.darkcraft.rey.Voces.PAUSA;
    private static final int T_FRASE1 = 66;
    private static final int T_FRASE2 = T_FRASE1 + voz(1) + P;
    private static final int T_SALTA = T_FRASE2 + voz(2) + 16;
    private static final int T_FRASE3 = T_SALTA + 70;
    private static final int T_SALUDO = T_FRASE3 + 110;
    private static final int T_FRASE4 = Math.max(T_FRASE3 + voz(3) + P, T_SALUDO + 20);
    public static final int T_LUCHA = T_FRASE4 + voz(4) + 30;
    /** Duracion de la entrada (ticks). */
    public static final int INTRO_TICKS = T_LUCHA + 10;
    /** Lo que sube de mas al saltar de la rama (el plano del salto lo sigue con la misma cuenta). */
    private static final double SALTO_ALTO = 2.5;

    private static int voz(int n) {
        return com.darkcraft.rey.Voces.ticks(com.darkcraft.rey.Voces.BASE_GAVRAN + n);
    }

    /** Segundos de la entrada en que cambia de plano (para la prueba de la CI): rama, de cerca, salto, contrapicado, saludo. */
    public static float[] cortesEntrada() {
        return new float[]{3.2f, T_FRASE2 / 20f, T_SALTA / 20f, T_FRASE3 / 20f, T_SALUDO / 20f, INTRO_TICKS / 20f - 0.6f};
    }

    private void empezarIntro(ServerLevel sl, SalaArbol s, List<ServerPlayer> dentro) {
        jugadores.clear();
        for (ServerPlayer p : sl.players()) {
            if (!valido(p)) continue;
            if (s.enUmbral(p.position())) {
                // en el umbral: hacia dentro, que ahi va la barrera
                p.teleportTo(sl, s.ccx(), s.F(), s.cz() - 17.5, 180f, p.getXRot());
            }
            if (s.dentro(p.position())) jugadores.add(p.getUUID());
        }
        ReyEstado.get(sl.getServer()).set(s.key(), ReyEstado.LUCHA);
        GavranEventos.cerrarSala(sl, s);
        Raices.limpiarSueltas(sl, s);
        // los monstruos que se hubieran colado en la sala se van (no los escuderos ni los que tengan nombre)
        for (Monster m : sl.getEntitiesOfClass(Monster.class, s.caja(), m -> !(m instanceof Gavran) && !m.isPersistenceRequired()
                && !m.getTags().contains(GavranEventos.TAG_ESCUDERO))) {
            sl.sendParticles(ParticleTypes.POOF, m.getX(), m.getY() + 0.8, m.getZ(), 8, 0.3, 0.5, 0.3, 0.02);
            m.discard();
        }
        List<Shot> planos = despejar(sl, s, rama);
        // el plano del salto mira a donde esta Gavran de verdad (aunque el servidor vaya con retraso)
        planos.get(3).siguiendo(getId(), 3.0f, -4.5f, s.F() + 6.5, s.F() + 29);
        setFlag(F_INTRO, true);
        setFlag(F_ALERTA, true);
        cambiar(Estado.INTRO);
        for (ServerPlayer p : vivos(sl)) Cinematics.playShots(p, Cinematics.GAVRAN_INTRO, planos, INTRO_TICKS / 20f);
        DarkCraftMod.LOGGER.info("[DarkCraft] Ser Gavran despierta ({} jugadores)", jugadores.size());
    }

    private void intro(ServerLevel sl) {
        SalaArbol s = sala();
        if (t < T_SALTA) {
            setPose(POSE_POSADO);
            posarEnRama();
            // mira a los que han entrado
            List<ServerPlayer> v = vivos(sl);
            if (!v.isEmpty()) getLookControl().setLookAt(v.get(0), 20f, 20f);
        }
        if (t == T_FRASE1) habla(sl, 1);
        if (t == T_FRASE2) habla(sl, 2);
        if (t == T_SALTA) {
            anim(GavranAnims.DESCENDER);
            setPose(POSE_PIE);
            setFlag(F_ALERTA, false);
        }
        int salta = T_SALTA + GavranAnims.GOLPES_DESCENDER[0], cae = T_SALTA + GavranAnims.GOLPES_DESCENDER[1];
        if (t == salta - 6) sonido(sl, DarkCraftMod.SFX_MADERA_CRUJE.get(), 1.6f, 0.9f);
        if (t == salta && s != null) {
            sonido(sl, DarkCraftMod.SFX_GAVRAN_SALTO.get(), 2.0f, 1.0f);
            saltar(rama, s.aterrizaje(), cae - salta, SALTO_ALTO);
            hojasCayendo(sl, rama, 40);
        }
        if (t == cae && s != null) {
            Vec3 a = s.aterrizaje();
            golpeSuelo(sl, a.add(0, 0, 1.2), 4.2f);
            sonido(sl, DarkCraftMod.SFX_GAVRAN_CLAVAR.get(), 2.2f, 0.9f);
            // la onda tira hacia atras a quien este cerca, sin hacer dano
            for (ServerPlayer p : vivos(sl)) {
                double d = p.position().distanceTo(a);
                if (d < 7) {
                    p.knockback(1.2 * (1 - d / 8), a.x - p.getX(), a.z - p.getZ());
                    p.hurtMarked = true;
                }
            }
        }
        // se da la vuelta hacia los jugadores despues de caer
        if (t > cae + 6 && t < T_LUCHA) {
            List<ServerPlayer> v = vivos(sl);
            if (!v.isEmpty()) mirar(v.get(0), 6f);
        }
        if (t == T_FRASE3) habla(sl, 3);
        if (t == T_SALUDO) anim(GavranAnims.SALUDO);
        if (t == T_FRASE4) habla(sl, 4);
        if (t >= T_LUCHA) {
            setNoGravity(false);
            this.noPhysics = false;
            setFlag(F_LUCHA, true);
            setFlag(F_INTRO, false);
            barra.setVisible(true);
            for (ServerPlayer p : vivos(sl)) barra.addPlayer(p);
            recup = 10;
            burla = 0;
            cambiar(Estado.LUCHA);
            cds.put(Atq.INVOCAR, tGlobal + 400);
        }
    }

    private static double[] p(double... v) {
        return v;
    }

    /**
     * Despeja la rama y las hojas o lianas que taparian a Gavran en los planos de la entrada (en la rama, de cerca y
     * el salto). Devuelve los planos.
     */
    static List<Shot> despejar(ServerLevel sl, SalaArbol s, Vec3 rama) {
        s.despejarRama(sl, rama);
        // luz invisible en la rama (bloques de luz: ni se ven ni se tocan) para que se le vea desde la sala
        for (int[] o : new int[][]{{0, 2, -2}, {2, 1, 0}, {-2, 1, 0}, {0, 4, 1}}) {
            BlockPos lp = BlockPos.containing(rama.x + o[0], rama.y + o[1], rama.z + o[2]);
            if (sl.getBlockState(lp).isAir())
                sl.setBlock(lp, Blocks.LIGHT.defaultBlockState().setValue(net.minecraft.world.level.block.LightBlock.LEVEL, 11), 2);
        }
        List<Shot> planos = introPlanos(s, rama);
        for (int i = 1; i <= 3; i++) s.despejarVista(sl, planos.get(i).pos, planos.get(i).look);
        return planos;
    }

    /** Donde va Gavran en un salto de 'de' a 'a' (q de 0 a 1, alto: lo que sube de mas en el arco). */
    static Vec3 enSalto(Vec3 de, Vec3 a, double alto, double q) {
        q = Mth.clamp(q, 0, 1);
        double x = Mth.lerp(q, de.x, a.x), z = Mth.lerp(q, de.z, a.z);
        // sube un poco y cae acelerando (o sube frenando si el destino esta mas alto)
        double dy = a.y - de.y;
        double y = dy < 0 ? de.y + dy * q * q + alto * 4 * q * (1 - q) * 0.5
                : de.y + dy * (1 - (1 - q) * (1 - q)) + alto * 4 * q * (1 - q) * 0.5;
        return new Vec3(x, y, z);
    }

    /** Planos de la entrada: la puerta que se cierra, Gavran en la rama, el salto, su cara y el saludo. */
    public static List<Shot> introPlanos(SalaArbol s, Vec3 rama) {
        double cx = s.ccx(), F = s.F(), cz = s.ccz();
        double hx = rama.x, hy = rama.y + 3.5, hz = rama.z;
        Vec3 c = s.aterrizaje();
        List<Shot> l = new ArrayList<>();
        // la puerta se cierra detras de ellos
        l.add(new Shot(0f, 3.2f, 64, 0, 1,
                p(cx + 7.5, F + 3.4, cz - 27.5, cx + 5.8, F + 3.1, cz - 25.5),
                p(cx, F + 5.2, cz - 15.4, cx, F + 5.0, cz - 15.4)));
        // arriba, en la rama, de rodillas: "el Rey ha muerto" (desde un lado y casi a su altura: desde abajo le tapa la rama)
        l.add(new Shot(3.2f, T_FRASE2 / 20f, 58, 0, 1,
                p(cx + 10, F + 27, cz - 28.5, cx + 7, F + 29, cz - 26),
                p(hx, hy, hz, hx, hy + 0.2, hz)));
        // de cerca, a su altura
        l.add(new Shot(T_FRASE2 / 20f, T_SALTA / 20f, 52, 0, 1,
                p(hx + 3.4, hy - 0.3, hz - 6.8, hx + 2.3, hy - 0.1, hz - 5.6),
                p(hx, hy + 0.1, hz, hx, hy + 0.2, hz)));
        // el salto: la camara le sigue desde un lado (quieta mientras se levanta en la rama, cae con el y se para
        // cuando aterriza); los puntos salen del mismo salto que hace el servidor, cada cuarto de segundo
        int salta = T_SALTA + GavranAnims.GOLPES_DESCENDER[0], cae = T_SALTA + GavranAnims.GOLPES_DESCENDER[1];
        float s0 = T_SALTA / 20f, s1 = T_FRASE3 / 20f;
        int n = 15;
        double[] pos = new double[n * 3], look = new double[n * 3];
        for (int i = 0; i < n; i++) {
            double tick = (s0 + (s1 - s0) * i / (n - 1.0)) * 20;
            Vec3 g = enSalto(rama, c, SALTO_ALTO, (tick - salta) / (cae - salta));
            double ly = g.y + 3.0;
            look[i * 3] = g.x;
            look[i * 3 + 1] = ly;
            look[i * 3 + 2] = g.z;
            pos[i * 3] = cx - 14;
            pos[i * 3 + 1] = Mth.clamp(ly - 4.5, F + 6.5, F + 29);
            pos[i * 3 + 2] = cz - 26;
        }
        l.add(new Shot(s0, s1, 70, 0, 0, pos, look));
        // contrapicado mientras se levanta y les mira: "pero hice un juramento"
        l.add(new Shot(T_FRASE3 / 20f, T_SALUDO / 20f, 60, 0, 1,
                p(c.x + 2.8, F + 2.6, c.z + 7.5, c.x + 1.6, F + 2.3, c.z + 6.0),
                p(c.x, c.y + 4.6, c.z, c.x, c.y + 4.9, c.z)));
        // el saludo de caballero y el titulo
        l.add(new Shot(T_SALUDO / 20f, INTRO_TICKS / 20f - 0.6f, 66, 0, 2,
                p(c.x - 3.6, F + 4.2, c.z + 10.5, c.x - 2.4, F + 3.8, c.z + 8.8),
                p(c.x, c.y + 3.8, c.z, c.x, c.y + 3.6, c.z)));
        return l;
    }

    // ------------------------------------------------------------------ voz

    /** Dice la frase n (1..18) a quien este cerca (voz y subtitulo). */
    void habla(ServerLevel sl, int n) {
        for (ServerPlayer p : sl.players())
            if (p.distanceToSqr(this) < 96 * 96) Net.to(p, new Net.ReyHabla(getId(), VOZ_BASE + n));
        hablaHasta = tGlobal + com.darkcraft.rey.Voces.ticks(VOZ_BASE + n) + 6;
        setFlag(F_HABLA, true);
    }

    /** Frase que solo dice si no esta hablando ya (y con una pausa despues de la anterior). */
    private void hablaSiCalla(ServerLevel sl, int n) {
        if (tGlobal >= hablaHasta + com.darkcraft.rey.Voces.PAUSA) habla(sl, n);
    }

    // ------------------------------------------------------------------ pelea

    private void lucha(ServerLevel sl) {
        List<ServerPlayer> vivos = vivos(sl);
        actualizarJugadores(vivos);
        if (vivos.isEmpty()) {
            if (t > 40) reiniciar(sl, "todos han caido");
            return;
        }
        if (atq == null) setPose(POSE_PIE);
        if (objetivo == null || !objetivo.isAlive() || !(objetivo instanceof ServerPlayer sp && vivos.contains(sp)) || --cambiaObjetivo <= 0) {
            objetivo = vivos.get(0);
            double best = 1e9;
            for (ServerPlayer p : vivos) {
                double d = p.distanceToSqr(this) + (vivos.size() > 1 ? random.nextDouble() * 80 : 0);
                if (d < best) {
                    best = d;
                    objetivo = p;
                }
            }
            cambiaObjetivo = 200 + random.nextInt(200);
            setTarget(objetivo);
        }
        LivingEntity o = objetivo;
        SalaArbol s = sala();
        // que no salga del anillo (ni se meta en el tronco)
        if (s != null && saltoIni < 0) {
            double d = s.dist(getX(), getZ());
            if (d < SalaArbol.R_TRONCO - 1.5 || d > SalaArbol.R_MURO + 2.5 || getY() < s.F() - 3) {
                Vec3 v = s.alAnillo(position(), 0.5);
                moveTo(v.x, s.suelo(sl, v.x, v.z), v.z, getYRot(), 0);
                setDeltaMovement(Vec3.ZERO);
            }
        }
        if (fase() >= 2) ambienteFase2(sl, vivos);
        if (atq != null) {
            tickAtaque(sl, o, vivos);
            return;
        }
        mirar(o, 22f);
        if (recup > 0) {
            recup--;
            if (distanceTo(o) > 4.5) andarHacia(o.position(), 0.75);
            else getMoveControl().setWantedPosition(getX(), getY(), getZ(), 0);
            return;
        }
        double d = distanceTo(o);
        Atq next = elegir(sl, d, o);
        if (next != null) {
            empezar(sl, next, o);
            return;
        }
        andarHacia(o.position(), 1.0);
        vigilarAtasco(sl, o);
        if (++burla > 900 && random.nextInt(300) == 0) {
            burla = 0;
            hablaSiCalla(sl, random.nextBoolean() ? 8 : 13);
        }
    }

    private void actualizarJugadores(List<ServerPlayer> vivos) {
        Set<ServerPlayer> enBarra = new HashSet<>(barra.getPlayers());
        for (ServerPlayer p : vivos) if (!enBarra.contains(p)) barra.addPlayer(p);
        for (ServerPlayer p : enBarra) if (!vivos.contains(p)) barra.removePlayer(p);
    }

    void mirar(Entity o, float max) {
        double dx = o.getX() - getX(), dz = o.getZ() - getZ();
        float want = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f;
        float cur = getYRot();
        float nyaw = cur + Mth.clamp(Mth.wrapDegrees(want - cur), -max, max);
        setYRot(nyaw);
        setYBodyRot(nyaw);
        setYHeadRot(nyaw);
        getLookControl().setLookAt(o, 30f, 30f);
    }

    Vec3 adelante() {
        float r = getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(r), 0, Mth.cos(r));
    }

    public double adelanteX() {
        return adelante().x;
    }

    public double adelanteZ() {
        return adelante().z;
    }

    /** Andar hacia un punto rodeando el tronco por el lado mas corto y sin salirse del anillo. */
    private void andarHacia(Vec3 obj, double vel) {
        SalaArbol s = sala();
        Vec3 dest = obj;
        if (s != null) {
            // el segmento hasta el objetivo pasa demasiado cerca del centro: ir por el anillo
            double px = getX() - s.ccx(), pz = getZ() - s.ccz(), qx = obj.x - s.ccx(), qz = obj.z - s.ccz();
            double vx = qx - px, vz = qz - pz, l2 = vx * vx + vz * vz;
            double k = l2 < 1e-6 ? 0 : Mth.clamp(-(px * vx + pz * vz) / l2, 0, 1);
            double cxk = px + vx * k, czk = pz + vz * k;
            if (Math.sqrt(cxk * cxk + czk * czk) < SalaArbol.R_TRONCO + 1.8) {
                double a0 = Math.atan2(pz, px), a1 = Math.atan2(qz, qx);
                double da = Mth.wrapDegrees((a1 - a0) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
                double paso = Math.copySign(Math.min(Math.abs(da), 0.42), da);
                double r = Math.max(Math.hypot(px, pz), SalaArbol.R_TRONCO + 3.0);
                dest = s.enAnillo(a0 + paso, r, getY());
            }
            dest = s.alAnillo(dest, 0.4);
        }
        getMoveControl().setWantedPosition(dest.x, obj.y, dest.z, vel);
    }

    /** Si lleva rato queriendo andar y no se mueve (un brasero, una esquina), da un salto hacia el objetivo. */
    private void vigilarAtasco(ServerLevel sl, LivingEntity o) {
        if (t % 20 != 0) return;
        Vec3 p = position();
        if (ultimaPos != null && p.distanceTo(ultimaPos) < 0.6 && distanceTo(o) > 6) atascado++;
        else atascado = 0;
        ultimaPos = p;
        if (atascado >= 2 && saltoIni < 0) {
            atascado = 0;
            SalaArbol s = sala();
            Vec3 d = o.position().subtract(p).multiply(1, 0, 1).normalize().scale(Math.min(7, distanceTo(o) - 3));
            Vec3 a = p.add(d);
            if (s != null) {
                a = s.alAnillo(a, 0.6);
                a = new Vec3(a.x, s.suelo(sl, a.x, a.z), a.z);
            }
            saltar(p, a, 14, 2.8);
            sonido(sl, DarkCraftMod.SFX_GAVRAN_SALTO.get(), 1.0f, 1.2f);
        }
    }

    // ------------------------------------------------------------------ saltos (por el aire, sin chocar)

    void saltar(Vec3 de, Vec3 a, int ticks, double alto) {
        saltoDe = de;
        saltoA = a;
        saltoIni = t;
        saltoDur = Math.max(1, ticks);
        saltoAlto = alto;
        setNoGravity(true);
        this.noPhysics = true;
    }

    private void tickSalto(ServerLevel sl) {
        if (saltoIni < 0) return;
        double q = Mth.clamp((t - saltoIni) / (double) saltoDur, 0, 1);
        Vec3 v = enSalto(saltoDe, saltoA, saltoAlto, q);
        setPos(v.x, v.y, v.z);
        setDeltaMovement(Vec3.ZERO);
        if (q >= 1) {
            saltoIni = -1;
            setPos(saltoA.x, saltoA.y, saltoA.z);
            if (estado == Estado.LUCHA || estado == Estado.ATURDIDO) {
                setNoGravity(false);
                this.noPhysics = false;
            }
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DARK_OAK_PLANKS.defaultBlockState()),
                    getX(), getY() + 0.1, getZ(), 20, 1.0, 0.1, 1.0, 0.15);
        }
    }

    // ------------------------------------------------------------------ ataques

    enum Atq {
        //            anim                       dur  cd   min  max   peso  fase (0 = las dos)
        COMBO3(GavranAnims.COMBO3, 58, 70, 0, 6.6, 4, 0),
        COMBO5(GavranAnims.COMBO5, 80, 150, 0, 6.6, 2.5, 0),
        GUARDIA(0, 64, 280, 0, 7.5, 1.6, 0),
        EMBESTIDA(0, 50, 210, 7.5, 26, 3, 0),
        RAICES(GavranAnims.RAICES, 46, 170, 5, 30, 3, 0),
        CLAVAR(GavranAnims.CLAVAR, 62, 240, 0, 11, 2.5, 0),
        INVOCAR(GavranAnims.INVOCAR, 54, 640, 0, 40, 2, 1),
        // fase 2 (parte B2); los de peso 0 no salen al azar: los lanza la pelea cuando tocan
        ATRAPAR(GavranAnims.RAICES, 46, 300, 3, 34, 2.6, 2),
        HOJAS(GavranAnims.INVOCAR, 76, 280, 0, 40, 2.4, 2),
        SUELO(GavranAnims.CLAVAR, 222, 1100, 0, 40, 1.3, 2),
        JUICIO(GavranAnims.JUICIO, 150, 600, 0, 80, 0, 2),
        BALCON(GavranAnims.SUBIR_BALCON, 44, 420, 0, 80, 0, 2),
        BAJAR(GavranAnims.CAER, 48, 0, 0, 80, 0, 2),
        SALTO(GavranAnims.CAER, 48, 0, 0, 80, 0, 2);

        final int anim, dur, cd, fase;
        final double min, max, peso;

        Atq(int anim, int dur, int cd, double min, double max, double peso, int fase) {
            this.anim = anim;
            this.dur = dur;
            this.cd = cd;
            this.min = min;
            this.max = max;
            this.peso = peso;
            this.fase = fase;
        }
    }

    private Atq elegir(ServerLevel sl, double d, LivingEntity o) {
        SalaArbol s = sala();
        int f = fase();
        // en un balcon (el, o el objetivo): solo cuenta la altura de uno respecto al otro
        boolean yoArriba = s != null && getY() > s.F() + 8;
        boolean arriba = o.getY() > getY() + 6;
        if (f >= 2) {
            if (juicioPendiente && !yoArriba) {
                juicioPendiente = false;
                return Atq.JUICIO;
            }
            if (atrapado != null && tGlobal < atrapadoHasta - 20 && !yoArriba) {
                ServerPlayer p = sl.getServer().getPlayerList().getPlayer(atrapado);
                if (p != null && valido(p) && p.distanceTo(this) < 22) {
                    objetivo = p;
                    return Atq.SALTO;
                }
            }
            if (yoArriba && (o.getY() < getY() - 4 || tGlobal - enBalcon > 300)) return Atq.BAJAR;
            if (!yoArriba && arriba && cds.getOrDefault(Atq.BALCON, 0) <= tGlobal && sitioBalcon(sl, o) != null) return Atq.BALCON;
        }
        List<Atq> c = new ArrayList<>();
        double total = 0;
        for (Atq a : Atq.values()) {
            if (a.peso <= 0 || (a.fase != 0 && a.fase != f)) continue;
            if (cds.getOrDefault(a, 0) > tGlobal) continue;
            if (arriba && a != Atq.RAICES && a != Atq.INVOCAR && a != Atq.ATRAPAR && a != Atq.HOJAS) continue;
            if (!arriba && (d < a.min || d > a.max)) continue;
            // en el balcon no: ni embestidas, ni raices por el suelo, ni clavar, ni la marea
            if (yoArriba && (a == Atq.EMBESTIDA || a == Atq.RAICES || a == Atq.CLAVAR || a == Atq.SUELO || a == Atq.INVOCAR)) continue;
            if (a == Atq.INVOCAR && escuderosVivos(sl) >= 2) continue;
            if (a == Atq.EMBESTIDA && !caminoLibre(o)) continue;
            c.add(a);
            total += a.peso * (a == Atq.COMBO5 && getHealth() < VIDA * 0.75f ? 1.6 : 1.0);
        }
        if (c.isEmpty()) return null;
        double r = random.nextDouble() * total;
        for (Atq a : c) {
            r -= a.peso * (a == Atq.COMBO5 && getHealth() < VIDA * 0.75f ? 1.6 : 1.0);
            if (r <= 0) return a;
        }
        return c.get(c.size() - 1);
    }

    /** Para embestir: que el tronco no este en medio. */
    private boolean caminoLibre(LivingEntity o) {
        SalaArbol s = sala();
        if (s == null) return true;
        double px = getX() - s.ccx(), pz = getZ() - s.ccz(), qx = o.getX() - s.ccx(), qz = o.getZ() - s.ccz();
        double vx = qx - px, vz = qz - pz, l2 = vx * vx + vz * vz;
        double k = l2 < 1e-6 ? 0 : Mth.clamp(-(px * vx + pz * vz) / l2, 0, 1);
        return Math.hypot(px + vx * k, pz + vz * k) > SalaArbol.R_TRONCO + 1.0;
    }

    private void empezar(ServerLevel sl, Atq a, LivingEntity o) {
        atq = a;
        at = 0;
        tocados.clear();
        contra = false;
        cds.put(a, tGlobal + a.cd);
        getNavigation().stop();
        getMoveControl().setWantedPosition(getX(), getY(), getZ(), 0);
        if (a.anim > 0) anim(a.anim);
        dir = adelante();
    }

    private void acabar() {
        atq = null;
        setPose(POSE_PIE);
        recup = 12 + random.nextInt(12);
        // justo despues de un ataque es el momento de contraatacar: esos golpes rompen mas la guardia
        ventanaHasta = tGlobal + 26;
    }

    private void tickAtaque(ServerLevel sl, LivingEntity o, List<ServerPlayer> vivos) {
        Atq a = atq;
        int k = at++;
        switch (a) {
            case COMBO3 -> {
                int[] h = GavranAnims.GOLPES_COMBO3;
                if (k < h[0] - 3 || (k > h[0] + 2 && k < h[1] - 3) || (k > h[1] + 2 && k < h[2] - 4)) mirar(o, 15f);
                for (int i = 0; i < h.length; i++) {
                    if (k == h[i] - 3) empujar(i == 2 ? 0.42 : 0.3);
                    if (k == h[i]) {
                        if (i == 0) tajo(sl, vivos, 6.4, 150, 34, 0.6, 0.05);
                        if (i == 1) tajo(sl, vivos, 6.6, 175, 34, 0.7, 0.05);
                        if (i == 2) {
                            tajo(sl, vivos, 6.8, 70, 46, 1.0, 0.35);
                            golpeSuelo(sl, position().add(adelante().scale(5.2)), 2.4f);
                        }
                    }
                }
                if (k == h[2] - 12) aviso(sl, Net.Aviso.SECTOR, getX(), suelo(sl), getZ(), 7.2f, 70f, getYRot(), 12, Net.Aviso.ROJO);
                if (k >= a.dur) acabar();
            }
            case COMBO5 -> {
                int[] h = GavranAnims.GOLPES_COMBO5;
                if (k % 7 < 4 && k < h[4] - 4) mirar(o, 16f);
                for (int i = 0; i < h.length; i++) {
                    if (k == h[i] - 3) empujar(i == 4 ? 0.45 : 0.28);
                    if (k == h[i]) {
                        switch (i) {
                            case 0 -> tajo(sl, vivos, 6.4, 150, 32, 0.6, 0.05);
                            case 1 -> tajo(sl, vivos, 6.6, 175, 32, 0.7, 0.05);
                            case 2 -> tajo(sl, vivos, 6.2, 120, 34, 0.4, 0.55);
                            case 3 -> tajo(sl, vivos, 6.4, 360, 36, 1.1, 0.15);
                            default -> {
                                tajo(sl, vivos, 6.8, 70, 48, 1.1, 0.4);
                                golpeSuelo(sl, position().add(adelante().scale(5.2)), 2.6f);
                            }
                        }
                    }
                }
                if (k == h[3] - 10) aviso(sl, Net.Aviso.CIRCULO, getX(), suelo(sl), getZ(), 6.4f, 0, 0, 10, Net.Aviso.ROJO);
                if (k == h[4] - 12) aviso(sl, Net.Aviso.SECTOR, getX(), suelo(sl), getZ(), 7.2f, 70f, getYRot(), 12, Net.Aviso.ROJO);
                if (k >= a.dur) acabar();
            }
            case GUARDIA -> {
                if (contra) {
                    if (contraT < 4) mirar(o, 30f);
                    if (contraT == GavranAnims.GOLPES_CONTRAATAQUE[0] - 3) empujar(0.6);
                    if (contraT == GavranAnims.GOLPES_CONTRAATAQUE[0]) tajo(sl, vivos, 7.6, 40, 50, 1.4, 0.2);
                    contraT++;
                    if (contraT >= GavranAnims.TICKS[GavranAnims.CONTRAATAQUE]) acabar();
                    return;
                }
                setPose(POSE_GUARDIA);
                mirar(o, 10f);
                if (k >= a.dur) acabar();
            }
            case EMBESTIDA -> embestida(sl, o, vivos, k);
            case RAICES -> {
                SalaArbol s = sala();
                boolean arriba = s != null && o.getY() > s.F() + 6;
                int h = GavranAnims.GOLPES_RAICES[0];
                if (k < h - 2) mirar(o, 25f);
                if (k == 2) {
                    Vec3 hacia = o.position().subtract(position()).multiply(1, 0, 1);
                    dir = hacia.lengthSqr() < 1e-4 ? adelante() : hacia.normalize();
                    if (arriba) aviso(sl, Net.Aviso.CIRCULO, o.getX(), Math.floor(o.getY() + 0.01), o.getZ(), 1.9f, 0, 0, h + 4, Net.Aviso.VERDE);
                    else aviso(sl, Net.Aviso.LINEA, getX(), suelo(sl), getZ(), 24f, 2.9f, (float) (Mth.atan2(dir.z, dir.x) * Mth.RAD_TO_DEG) - 90f,
                            h, Net.Aviso.VERDE);
                    if (random.nextInt(3) == 0) hablaSiCalla(sl, 13);
                }
                if (k == h) {
                    golpeSuelo(sl, position().add(adelante().scale(1.8)), 1.8f);
                    if (arriba) raices.bajo(sl, new Vec3(o.getX(), o.getY(), o.getZ()), 4, 36);
                    else raices.linea(sl, position(), dir, 24, 1, 36);
                }
                if (k >= a.dur) acabar();
            }
            case CLAVAR -> {
                int h = GavranAnims.GOLPES_CLAVAR[0];
                if (k < h - 4) mirar(o, 18f);
                if (k == 3 && random.nextBoolean()) hablaSiCalla(sl, 6);
                Vec3 punta = position().add(adelante().scale(1.4));
                if (k == h - 12) aviso(sl, Net.Aviso.CIRCULO, punta.x, suelo(sl), punta.z, 3.2f, 0, 0, 12, Net.Aviso.ROJO);
                if (k == h) {
                    double y = suelo(sl);
                    golpeSuelo(sl, punta, 3.2f);
                    sonido(sl, DarkCraftMod.SFX_GAVRAN_CLAVAR.get(), 2.2f, 1.0f);
                    for (ServerPlayer p : vivos)
                        if (Math.hypot(p.getX() - punta.x, p.getZ() - punta.z) < 3.2 && tocados.add(p.getUUID()))
                            golpear(sl, p, 38, 1.2, 0.5, true);
                    raices.onda(new Vec3(punta.x, y, punta.z), 0.75, 25, 34);
                    aviso(sl, Net.Aviso.ONDA, punta.x, y, punta.z, 25f, 0.75f, 0, 34, Net.Aviso.VERDE);
                }
                if (k >= a.dur) acabar();
            }
            case INVOCAR -> {
                if (k < 10) mirar(o, 20f);
                if (k == 6) hablaSiCalla(sl, 7);
                if (k == GavranAnims.GOLPES_INVOCAR[0]) invocar(sl, vivos);
                if (k >= a.dur) acabar();
            }
            // ---------------------------------------------------------------- fase 2
            case ATRAPAR -> {
                int h = GavranAnims.GOLPES_RAICES[0];
                if (k < h - 2) mirar(o, 25f);
                if (k == 1) {
                    // un circulo verde a los pies de cada uno: quien no salga, queda atrapado
                    trampas.clear();
                    for (ServerPlayer p : vivos) {
                        if (trampas.size() >= 4 || p.distanceTo(this) > 36) continue;
                        Vec3 c = new Vec3(p.getX(), Math.floor(p.getY() + 0.01), p.getZ());
                        trampas.add(c);
                        aviso(sl, Net.Aviso.CIRCULO, c.x, c.y, c.z, 1.8f, 0, 0, h + 3, Net.Aviso.VERDE);
                    }
                    if (random.nextInt(3) == 0) hablaSiCalla(sl, 13);
                }
                if (k == h + 2) {
                    golpeSuelo(sl, position().add(adelante().scale(1.8)), 1.6f);
                    atrapado = null;
                    for (Vec3 c : trampas) {
                        raices.jaula(sl, c, 40);
                        for (ServerPlayer p : vivos)
                            if (Math.hypot(p.getX() - c.x, p.getZ() - c.z) < 1.8 && Math.abs(p.getY() - c.y) < 2.2) {
                                atrapar(p, 38);
                                if (atrapado == null) {
                                    atrapado = p.getUUID();
                                    atrapadoHasta = tGlobal + 38;
                                }
                            }
                    }
                }
                if (k >= h + 8) {
                    acabar();
                    // si ha atrapado a alguien, salta sobre el ya
                    if (atrapado != null) recup = 0;
                }
            }
            case HOJAS -> {
                if (k < 12) mirar(o, 20f);
                if (k == 12 || k == 40) {
                    SalaArbol s = sala();
                    for (ServerPlayer p : vivos) {
                        if (p.distanceTo(this) > 44) continue;
                        Vec3 pp = new Vec3(p.getX(), Math.floor(p.getY() + 0.01), p.getZ());
                        lluvia(sl, pp, 18);
                        // y alrededor (solo si esta en el suelo de la sala)
                        if (s != null && pp.y < s.F() + 1.5)
                            for (int i = 0; i < (vivos.size() > 1 ? 1 : 2); i++) {
                                double an = random.nextDouble() * Math.PI * 2, r = 3.8 + random.nextDouble() * 3.4;
                                Vec3 q = s.alAnillo(pp.add(Math.cos(an) * r, 0, Math.sin(an) * r), 1.0);
                                lluvia(sl, new Vec3(q.x, s.suelo(sl, q.x, q.z), q.z), 18);
                            }
                    }
                }
                if (k >= a.dur) acabar();
            }
            case SUELO -> {
                int h = GavranAnims.GOLPES_CLAVAR[0];
                if (k < h - 4) mirar(o, 18f);
                if (k == 2) hablaSiCalla(sl, 15);
                if (k == h) {
                    golpeSuelo(sl, position().add(adelante().scale(1.4)), 3.0f);
                    sonido(sl, DarkCraftMod.SFX_GAVRAN_CLAVAR.get(), 2.2f, 0.85f);
                    sonido(sl, DarkCraftMod.SFX_RAICES_ATRAPAN.get(), 2.0f, 0.75f);
                }
                if (k == h + 12) {
                    // de rodilla con la espada clavada mientras las raices cubren todo el suelo
                    setPose(POSE_POSADO);
                    anim(0);
                    Vec3 c = position();
                    double y = suelo(sl);
                    raices.marea(new Vec3(c.x, y, c.z), 0.5, 85, 170, 16);
                    aviso(sl, Net.Aviso.ONDA, c.x, y, c.z, 85f, 0.5f, 0, 170, Net.Aviso.VERDE);
                }
                // el suelo cubierto se ve: un disco verde que crece con la marea
                if (k >= h + 12 && k < h + 12 + 170 && (k - h - 12) % 8 == 0) {
                    double r = Math.min(85, 2 + 0.5 * (k - h - 12) + 4);
                    aviso(sl, Net.Aviso.CIRCULO, getX(), suelo(sl), getZ(), (float) r, 0, 0, 10, Net.Aviso.VERDE);
                }
                if (k == h + 12 + 172) {
                    setPose(POSE_PIE);
                    anim(GavranAnims.LEVANTARSE);
                }
                if (k >= a.dur) acabar();
            }
            case JUICIO -> juicio(sl, o, vivos, k);
            case BALCON -> {
                int[] h = GavranAnims.GOLPES_SUBIR_BALCON;
                if (k == 0) {
                    destino = sitioBalcon(sl, o);
                    if (destino == null) {
                        acabar();
                        return;
                    }
                    hablaSiCalla(sl, 14);
                    aviso(sl, Net.Aviso.CIRCULO, destino.x, destino.y, destino.z, 2.8f, 0, 0, h[1] + 2, Net.Aviso.ROJO);
                }
                if (destino == null) {
                    acabar();
                    return;
                }
                if (k < h[0]) mirarPunto(destino, 30f);
                if (k == h[0]) {
                    sonido(sl, DarkCraftMod.SFX_GAVRAN_SALTO.get(), 2.0f, 0.95f);
                    saltar(position(), destino, h[1] - h[0], 4.5);
                    hojasCayendo(sl, position(), 20);
                }
                if (k == h[1]) {
                    golpeSuelo(sl, destino, 2.6f);
                    for (ServerPlayer p : vivos)
                        if (Math.hypot(p.getX() - destino.x, p.getZ() - destino.z) < 2.8 && Math.abs(p.getY() - destino.y) < 2.5
                                && tocados.add(p.getUUID())) golpear(sl, p, 36, 1.6, 0.5, false);
                    enBalcon = tGlobal;
                }
                if (k >= a.dur) acabar();
            }
            case BAJAR, SALTO -> saltoGolpe(sl, o, vivos, k, a == Atq.SALTO);
        }
    }

    private void mirarPunto(Vec3 p, float max) {
        double dx = p.x - getX(), dz = p.z - getZ();
        float want = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f;
        float cur = getYRot();
        float nyaw = cur + Mth.clamp(Mth.wrapDegrees(want - cur), -max, max);
        setYRot(nyaw);
        setYBodyRot(nyaw);
        setYHeadRot(nyaw);
    }

    /** Las raices atrapan a alguien: no puede andar ni saltar un rato (sin iconos raros en pantalla). */
    private void atrapar(ServerPlayer p, int ticks) {
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 6, false, false, false));
        // salto negativo (el cliente lo recibe como -56): no puede despegar del suelo
        p.addEffect(new MobEffectInstance(MobEffects.JUMP, ticks, 200, false, false, false));
        p.setDeltaMovement(0, Math.min(0, p.getDeltaMovement().y), 0);
        p.hurtMarked = true;
    }

    /** Un circulo rojo y, al poco, la lluvia de hojas cortantes en el. */
    private void lluvia(ServerLevel sl, Vec3 c, int retraso) {
        aviso(sl, Net.Aviso.CIRCULO, c.x, c.y, c.z, 2.6f, 0, 0, retraso, Net.Aviso.ROJO);
        raices.hojas(c, 2.6, retraso, 32);
    }

    /** Un sitio en el balcon cerca de o donde quepa Gavran (o null si no lo hay o o no esta en un balcon). */
    private Vec3 sitioBalcon(ServerLevel sl, LivingEntity o) {
        SalaArbol s = sala();
        if (s == null || o.getY() < s.F() + 8) return null;
        double a0 = s.ang(o.getX(), o.getZ());
        double lado = Math.signum(Mth.wrapDegrees((s.ang(getX(), getZ()) - a0) * Mth.RAD_TO_DEG));
        if (lado == 0) lado = 1;
        double R = 35.6;
        int y = (int) Math.floor(o.getY() + 0.01);
        for (int k : new int[]{1, -1, 2, -2, 3, -3}) {
            double an = a0 + lado * k * (3.2 / R);
            Vec3 p = s.enAnillo(an, R, y);
            BlockPos base = BlockPos.containing(p.x, y - 1, p.z);
            if (!sl.getBlockState(base).isFaceSturdy(sl, base, Direction.UP)) continue;
            boolean libre = true;
            for (int dy = 0; dy < 7 && libre; dy++)
                for (int dx = -1; dx <= 1 && libre; dx++)
                    for (int dz = -1; dz <= 1 && libre; dz++) {
                        BlockPos q = BlockPos.containing(p.x + dx, y + dy, p.z + dz);
                        if (!sl.getBlockState(q).getCollisionShape(sl, q).isEmpty()) libre = false;
                    }
            if (libre) return new Vec3(p.x, y, p.z);
        }
        return null;
    }

    /** Salto con golpe: del balcon al suelo (bajar) o sobre alguien atrapado por las raices. */
    private void saltoGolpe(ServerLevel sl, LivingEntity o, List<ServerPlayer> vivos, int k, boolean aAlguien) {
        int[] h = GavranAnims.GOLPES_CAER;
        SalaArbol s = sala();
        if (k == 0) {
            Vec3 desde = position();
            Vec3 hacia = o.position().subtract(desde).multiply(1, 0, 1);
            double dist = hacia.length();
            Vec3 d = dist > 2.4 ? desde.add(hacia.scale((dist - 1.8) / dist)) : desde;
            if (s != null) {
                // bajando del balcon con el objetivo aun arriba: al suelo, bajo el borde
                if (!aAlguien && o.getY() > s.F() + 6) d = s.enAnillo(s.ang(getX(), getZ()), 28, s.F());
                d = s.alAnillo(d, 0.8);
                d = new Vec3(d.x, s.suelo(sl, d.x, d.z), d.z);
            }
            destino = d;
            aviso(sl, Net.Aviso.CIRCULO, d.x, d.y, d.z, 3.0f, 0, 0, h[1] + 2, Net.Aviso.ROJO);
        }
        if (destino == null) {
            acabar();
            return;
        }
        if (k < h[0]) mirarPunto(destino, 30f);
        if (k == h[0]) {
            sonido(sl, DarkCraftMod.SFX_GAVRAN_SALTO.get(), 2.0f, 1.05f);
            saltar(position(), destino, h[1] - h[0], aAlguien ? 3.5 : 2.5);
        }
        if (k == h[1]) {
            golpeSuelo(sl, destino, 3.2f);
            sonido(sl, DarkCraftMod.SFX_GAVRAN_CLAVAR.get(), 2.0f, 1.0f);
            for (ServerPlayer p : vivos)
                if (Math.hypot(p.getX() - destino.x, p.getZ() - destino.z) < 3.0 && Math.abs(p.getY() - destino.y) < 2.5
                        && tocados.add(p.getUUID())) golpear(sl, p, aAlguien ? 46 : 40, 1.4, 0.45, false);
            if (aAlguien) atrapado = null;
            enBalcon = -1;
        }
        if (k >= Atq.SALTO.dur) acabar();
    }

    /** El Juicio de las espinas: muros de corteza a su alrededor, carga con la espada en alto y la clava. */
    private void juicio(ServerLevel sl, LivingEntity o, List<ServerPlayer> vivos, int k) {
        int h = GavranAnims.GOLPES_JUICIO[0];
        SalaArbol s = sala();
        if (k < 8) mirar(o, 20f);
        if (k == 2) habla(sl, 12);
        if (k == 6) {
            // cuatro muros en diagonal: detras de ellos (o del tronco) no llegan las espinas
            float yaw0 = getYRot();
            murosJuicio.clear();
            for (int i = 0; i < 4; i++) {
                double an = Math.toRadians(yaw0 + 45 + 90 * i);
                Vec3 dw = new Vec3(-Math.sin(an), 0, Math.cos(an));
                Vec3 c = position().add(dw.scale(10.5));
                if (s != null) {
                    c = s.alAnillo(c, 2.6);
                    c = new Vec3(c.x, s.suelo(sl, c.x, c.z), c.z);
                }
                Vec3 n = c.subtract(position()).multiply(1, 0, 1);
                if (n.lengthSqr() < 4) continue;
                aviso(sl, Net.Aviso.CIRCULO, c.x, c.y, c.z, 3.2f, 0, 0, 22, Net.Aviso.VERDE);
                raices.muro(sl, c, n, 7, 5, 20);
                murosJuicio.add(new Vec3[]{c, n.normalize()});
            }
        }
        if (k >= 8 && k < h && k % 10 == 0) sl.sendParticles(Raices.VERDE, getX(), getY() + 6.5, getZ(), 10, 0.4, 0.8, 0.4, 0);
        if (k == h - 30) aviso(sl, Net.Aviso.CIRCULO, getX(), suelo(sl), getZ(), 44f, 0, 0, 30, Net.Aviso.ROJO);
        if (k == h) {
            golpeSuelo(sl, position().add(adelante().scale(1.4)), 4.0f);
            aviso(sl, Net.Aviso.ONDA, getX(), suelo(sl), getZ(), 44f, 2.2f, 0, 20, Net.Aviso.ROJO);
            for (ServerPlayer p : raices.espinas(sl, 70)) Logros.dar(p, Logros.GAVRAN_JUICIO);
        }
        if (k >= Atq.JUICIO.dur) acabar();
    }

    /** Ambiente de la fase 2: el corazon brilla y late, caen hojas sobre la pelea y cruje la madera. */
    private void ambienteFase2(ServerLevel sl, List<ServerPlayer> vivos) {
        if (t % 8 == 0) {
            Vec3 h = corazonPos();
            sl.sendParticles(Raices.VERDE, h.x, h.y, h.z, 3, 0.12, 0.12, 0.12, 0);
            sl.sendParticles(ParticleTypes.GLOW, h.x, h.y, h.z, 1, 0.08, 0.08, 0.08, 0.01);
        }
        if (t % 60 == 0) {
            Vec3 h = corazonPos();
            sl.playSound(null, h.x, h.y, h.z, DarkCraftMod.SFX_CORAZON_LATIDO.get(), SoundSource.HOSTILE, 0.5f, 1.0f);
        }
        if (t % 6 == 0 && !vivos.isEmpty()) {
            ServerPlayer p = vivos.get(random.nextInt(vivos.size()));
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AZALEA_LEAVES.defaultBlockState()),
                    p.getX(), p.getY() + 12, p.getZ(), 3, 6, 1.5, 6, 0.01);
        }
        if (t % 170 == 0) sonido(sl, DarkCraftMod.SFX_MADERA_CRUJE.get(), 1.0f, 0.7f + random.nextFloat() * 0.3f);
    }

    /** Donde esta el corazon del arbol (en el pecho, a su izquierda). */
    public Vec3 corazonPos() {
        float k = fase() >= 2 ? ESCALA_F2 : 1f;
        Vec3 f = adelante();
        Vec3 izq = new Vec3(f.z, 0, -f.x);
        return position().add(0, 3.79 * k, 0).add(f.scale(0.57 * k)).add(izq.scale(0.29 * k));
    }

    /** Si el golpe (o la flecha) da en el corazon. */
    private boolean alCorazon(DamageSource src, ServerPlayer p) {
        Vec3 h = corazonPos();
        Entity d = src.getDirectEntity();
        if (d != null && d != p) return d.position().distanceTo(h) < 1.15 || d.getBoundingBox().inflate(0.35).contains(h);
        Vec3 o = p.getEyePosition(), v = p.getViewVector(1f);
        Vec3 w = h.subtract(o);
        double tt = w.dot(v);
        if (tt < 0 || tt > 7) return false;
        return w.subtract(v.scale(tt)).length() < 0.8;
    }

    private void corazon(ServerLevel sl, ServerPlayer p) {
        Vec3 h = corazonPos();
        sl.sendParticles(Raices.VERDE, h.x, h.y, h.z, 24, 0.3, 0.3, 0.3, 0);
        sl.sendParticles(ParticleTypes.DAMAGE_INDICATOR, h.x, h.y, h.z, 4, 0.2, 0.2, 0.2, 0.1);
        sl.playSound(null, h.x, h.y, h.z, DarkCraftMod.SFX_CORAZON_LATIDO.get(), SoundSource.HOSTILE, 1.6f, 1.25f);
        romper(sl, 5);
        Logros.dar(p, Logros.GAVRAN_CORAZON);
        if (!corazonDicho) {
            corazonDicho = true;
            habla(sl, 16);
        }
    }

    private void empujar(double v) {
        Vec3 f = adelante().scale(v);
        setDeltaMovement(getDeltaMovement().add(f.x, 0.02, f.z));
        hasImpulse = true;
    }

    private double suelo(ServerLevel sl) {
        SalaArbol s = sala();
        return s != null ? s.suelo(sl, getX(), getZ()) : Math.floor(getY());
    }

    // --- embestida con el hombro
    private void embestida(ServerLevel sl, LivingEntity o, List<ServerPlayer> vivos, int k) {
        if (k < 16) {
            setPose(POSE_CARGA);
            mirar(o, 25f);
            if (k == 2) sonido(sl, DarkCraftMod.SFX_GAVRAN_EMBESTIDA.get(), 1.8f, 1.0f);
            if (k == 10) {
                dir = adelante();
                aviso(sl, Net.Aviso.LINEA, getX(), suelo(sl), getZ(), 22f, 3.6f, getYRot(), 10, Net.Aviso.ROJO);
            }
            return;
        }
        if (k == 16) dir = adelante();
        if (k < 40) {
            setPose(POSE_CARGA);
            SalaArbol s = sala();
            Vec3 next = position().add(dir.scale(1.6));
            boolean pared = horizontalCollision && k > 18;
            if (s != null) {
                double d = s.dist(next.x, next.z);
                if (d < SalaArbol.R_TRONCO - 0.2 || d > SalaArbol.R_MURO + 0.8) pared = true;
            }
            if (pared) {
                // choca: golpe contra el tronco o el muro y se queda un momento aturdido
                golpeSuelo(sl, position().add(dir.scale(1.6)), 2.2f);
                sonido(sl, DarkCraftMod.SFX_GAVRAN_CHOQUE.get(), 1.8f, 0.8f);
                setDeltaMovement(Vec3.ZERO);
                at = 40;
                recup = 30;
                return;
            }
            setDeltaMovement(dir.x * 1.05, getDeltaMovement().y, dir.z * 1.05);
            hasImpulse = true;
            if (k % 2 == 0)
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ROOTED_DIRT.defaultBlockState()),
                        getX(), getY() + 0.2, getZ(), 8, 0.7, 0.1, 0.7, 0.1);
            for (ServerPlayer p : vivos)
                if (p.distanceTo(this) < 2.7 && tocados.add(p.getUUID())) golpear(sl, p, 42, 1.8, 0.45, false);
            return;
        }
        if (k == 40) {
            setPose(POSE_PIE);
            setDeltaMovement(Vec3.ZERO);
        }
        if (k >= Atq.EMBESTIDA.dur) acabar();
    }

    // --- escuderos esqueleto
    private int escuderosVivos(ServerLevel sl) {
        escuderos.removeIf(u -> {
            Entity e = sl.getEntity(u);
            return e == null || !e.isAlive();
        });
        return escuderos.size();
    }

    private void invocar(ServerLevel sl, List<ServerPlayer> vivos) {
        SalaArbol s = sala();
        int n = (vivos.size() > 1 || getHealth() < VIDA * 0.7f) ? 3 : 2;
        n = Math.min(n, 3 - escuderosVivos(sl));
        sonido(sl, DarkCraftMod.SFX_GAVRAN_INVOCAR.get(), 2.0f, 1.0f);
        for (int i = 0; i < n; i++) {
            double a = Math.toRadians(getYRot() + 90 + (i - (n - 1) / 2.0) * 50);
            Vec3 p = position().add(Math.cos(a) * 3.6, 0, Math.sin(a) * 3.6);
            if (s != null) {
                p = s.alAnillo(p, 0.8);
                p = new Vec3(p.x, s.suelo(sl, p.x, p.z), p.z);
            }
            Skeleton e = EntityType.SKELETON.create(sl);
            if (e == null) continue;
            e.moveTo(p.x, p.y, p.z, getYRot(), 0);
            e.finalizeSpawn(sl, sl.getCurrentDifficultyAt(BlockPos.containing(p)), MobSpawnType.MOB_SUMMONED, null, null);
            e.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            e.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            ItemStack casco = new ItemStack(Items.LEATHER_HELMET);
            ((DyeableLeatherItem) Items.LEATHER_HELMET).setColor(casco, 0x2E4A2C);
            e.setItemSlot(EquipmentSlot.HEAD, casco);
            for (EquipmentSlot sl2 : EquipmentSlot.values()) e.setDropChance(sl2, 0f);
            e.setCanPickUpLoot(false);
            e.setCustomName(Component.translatable("darkcraft.gavran.escudero"));
            e.setPersistenceRequired();
            e.addTag(GavranEventos.TAG_ESCUDERO);
            e.reassessWeaponGoal();
            if (!vivos.isEmpty()) e.setTarget(vivos.get(random.nextInt(vivos.size())));
            sl.addFreshEntity(e);
            escuderos.add(e.getUUID());
            raices.adorno(sl, p);
            sl.sendParticles(ParticleTypes.SOUL, p.x, p.y + 0.5, p.z, 16, 0.4, 0.6, 0.4, 0.03);
            sl.sendParticles(Raices.VERDE, p.x, p.y + 1, p.z, 12, 0.4, 0.8, 0.4, 0);
        }
    }

    void quitarEscuderos(ServerLevel sl) {
        for (UUID u : escuderos) {
            Entity e = sl.getEntity(u);
            if (e != null) {
                sl.sendParticles(ParticleTypes.SOUL, e.getX(), e.getY() + 1, e.getZ(), 10, 0.3, 0.6, 0.3, 0.02);
                e.discard();
            }
        }
        escuderos.clear();
        SalaArbol s = sala();
        if (s != null)
            for (Skeleton e : sl.getEntitiesOfClass(Skeleton.class, s.caja(), x -> x.getTags().contains(GavranEventos.TAG_ESCUDERO)))
                e.discard();
    }

    // ------------------------------------------------------------------ golpes

    private DamageSource fuente(boolean area) {
        var reg = level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        return new DamageSource(reg.getHolderOrThrow(area ? AREA : GOLPE), this);
    }

    private float mulJugadores() {
        return 1f + 0.15f * Math.max(0, jugadores.size() - 1);
    }

    private float mulDificultad() {
        return switch (level().getDifficulty()) {
            case PEACEFUL, EASY -> 0.7f;
            case HARD -> 1.35f;
            default -> 1f;
        };
    }

    /**
     * Dano bruto (antes de armadura). Con netherite y proteccion IV en normal: 34 = unos 1,9 corazones, 46 = unos 3
     * y 50 = unos 3,4 (como los golpes del Rey: 1,9 / 2,6 / 3,2).
     */
    boolean golpear(ServerLevel sl, ServerPlayer p, float bruto, double kb, double up, boolean area) {
        if (!valido(p)) return false;
        p.invulnerableTime = 0;
        float d = bruto * mulJugadores() * DANO;
        boolean ok = p.hurt(fuente(area), d);
        if (ok && !area && p.isBlocking() && random.nextFloat() < 0.4f) p.disableShield(true);
        if (kb > 0) p.knockback(kb, getX() - p.getX(), getZ() - p.getZ());
        if (up > 0) p.setDeltaMovement(p.getDeltaMovement().add(0, up, 0));
        p.hurtMarked = true;
        return ok;
    }

    private void tajo(ServerLevel sl, List<ServerPlayer> vivos, double reach, double arco, float bruto, double kb, double up) {
        Vec3 f = adelante();
        for (ServerPlayer p : vivos) {
            Vec3 d = p.position().subtract(position());
            double dist = Math.sqrt(d.x * d.x + d.z * d.z);
            if (dist > reach + p.getBbWidth() / 2 || d.y < -2.5 || d.y > 5.5) continue;
            double ang = Math.toDegrees(Math.acos(Mth.clamp((d.x * f.x + d.z * f.z) / Math.max(1e-4, dist), -1, 1)));
            if (dist > 1.6 && ang > arco / 2) continue;
            golpear(sl, p, bruto, kb, up, false);
        }
        int n = arco >= 300 ? 16 : 9;
        for (int i = 0; i < n; i++) {
            double a = Math.toRadians(getYRot() + 90 + (i - (n - 1) / 2.0) * Math.min(arco, 345) / (n - 1));
            double x = getX() + Math.cos(a) * reach * 0.7, z = getZ() + Math.sin(a) * reach * 0.7;
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, x, getY() + 2.2, z, 1, 0, 0, 0, 0);
            sl.sendParticles(Raices.VERDE, x, getY() + 2.2, z, 2, 0.3, 0.3, 0.3, 0);
        }
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AZALEA_LEAVES.defaultBlockState()),
                getX(), getY() + 3, getZ(), 8, 1.2, 1.0, 1.2, 0.05);
        sonido(sl, DarkCraftMod.SFX_GAVRAN_TAJO.get(), 1.6f, 0.9f + random.nextFloat() * 0.2f);
    }

    private void golpeSuelo(ServerLevel sl, Vec3 c, float r) {
        BlockState st = sl.getBlockState(BlockPos.containing(c.x, c.y - 0.5, c.z));
        if (st.isAir()) st = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        ParticleOptions crack = new BlockParticleOption(ParticleTypes.BLOCK, st);
        int n = (int) (r * 10);
        for (int i = 0; i < n; i++) {
            double a = random.nextDouble() * Math.PI * 2, d = random.nextDouble() * r;
            sl.sendParticles(crack, c.x + Math.cos(a) * d, c.y + 0.1, c.z + Math.sin(a) * d, 2, 0.1, 0.1, 0.1, 0.15);
        }
        sl.sendParticles(com.darkcraft.arbol.PuertaArbol.POLVO, c.x, c.y + 0.4, c.z, (int) (r * 6), r * 0.45, 0.25, r * 0.45, 0.02);
        sl.sendParticles(Raices.VERDE, c.x, c.y + 0.3, c.z, (int) (r * 3), r * 0.4, 0.2, r * 0.4, 0);
        sl.playSound(null, c.x, c.y, c.z, DarkCraftMod.SFX_GOLPE_SUELO.get(), SoundSource.HOSTILE,
                Math.min(2.5f, 0.8f + r * 0.12f), 0.85f + random.nextFloat() * 0.16f);
    }

    private void hojasCayendo(ServerLevel sl, Vec3 c, int n) {
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AZALEA_LEAVES.defaultBlockState()),
                c.x, c.y + 2, c.z, n, 2.0, 1.5, 2.0, 0.08);
    }

    void aviso(ServerLevel sl, int forma, double x, double y, double z, float a, float b, float ang, int dur, int color) {
        Net.Aviso m = new Net.Aviso(forma, x, y, z, a, b, ang, dur, color);
        for (ServerPlayer p : sl.players()) if (p.distanceToSqr(x, y, z) < 96 * 96) Net.to(p, m);
    }

    private void sonido(ServerLevel sl, SoundEvent s, float vol, float pitch) {
        sl.playSound(null, getX(), getY() + 2.5, getZ(), s, SoundSource.HOSTILE, vol, pitch);
    }

    // ------------------------------------------------------------------ recibir dano

    @Override
    public boolean hurt(DamageSource src, float amount) {
        if (level().isClientSide) return false;
        if (src.is(DamageTypes.GENERIC_KILL) || src.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            if (level() instanceof ServerLevel sl) reiniciar(sl, src.is(DamageTypes.GENERIC_KILL) ? "/kill" : "vacio");
            return false;
        }
        if (src.is(DamageTypeTags.IS_FIRE) || src.is(DamageTypes.LAVA) || src.is(DamageTypes.IN_WALL) || src.is(DamageTypes.CRAMMING)
                || src.is(DamageTypes.DROWN) || src.is(DamageTypes.FALL) || src.is(DamageTypes.CACTUS)
                || src.is(DamageTypes.SWEET_BERRY_BUSH) || src.is(DamageTypes.FREEZE)) return false;
        Entity att = src.getEntity();
        if (!(level() instanceof ServerLevel sl)) return false;
        if (estado != Estado.LUCHA && estado != Estado.ATURDIDO) {
            if (att instanceof Player) {
                sonido(sl, SoundEvents.ANVIL_PLACE, 0.4f, 1.6f);
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, getX(), getY() + 2.6, getZ(), 6, 0.6, 0.8, 0.6, 0.2);
            }
            return false;
        }
        boolean cuerpo = att instanceof Player && !src.is(DamageTypeTags.IS_PROJECTILE) && src.getDirectEntity() == att;
        float mul = (VIDA / 2f) / (VIDA_FASE * mulDificultad() * (1f + 0.6f * Math.max(0, jugadores.size() - 1)));
        if (src.is(DamageTypeTags.IS_PROJECTILE)) mul *= 0.25f;
        if (src.is(DamageTypeTags.IS_EXPLOSION)) mul *= 0.3f;
        if (att == null) mul *= 0.5f;
        if (estado == Estado.ATURDIDO) mul *= 1.6f;
        // en guardia para casi todo (tambien las flechas) y, si le pegan de cerca, contraataca
        if (atq == Atq.GUARDIA && !contra) {
            mul *= 0.08f;
            sonido(sl, DarkCraftMod.SFX_GAVRAN_CHOQUE.get(), 1.4f, 1.1f);
            sl.sendParticles(ParticleTypes.CRIT, getX(), getY() + 2.8, getZ(), 20, 0.6, 0.5, 0.6, 0.3);
            if (cuerpo) {
                contra = true;
                contraT = 0;
                anim(GavranAnims.CONTRAATAQUE);
                setPose(POSE_PIE);
                romper(sl, 7);
                if (tGlobal - frase5 > 300 && random.nextBoolean()) {
                    frase5 = tGlobal;
                    hablaSiCalla(sl, 5);
                }
            }
        } else if (cuerpo) {
            // postura: cada golpe la llena un poco, y mucho mas si es justo despues de que el acabe un ataque
            romper(sl, tGlobal <= ventanaHasta ? 8f : 2.5f);
        }
        // fase 2: el corazon del arbol que brilla en su pecho es su punto debil (de cerca o con flechas)
        if (fase() >= 2 && att instanceof ServerPlayer sp && (cuerpo || src.is(DamageTypeTags.IS_PROJECTILE)) && alCorazon(src, sp)) {
            mul *= 2.2f;
            corazon(sl, sp);
        }
        ultimoGolpeRecibido = tGlobal;
        float dmg = amount * mul;
        float hp = getHealth();
        float suelo = fase() == 1 ? VIDA / 2f : 0f;
        if (hp - dmg <= suelo + 0.01f) {
            boolean ok = super.hurt(src, Math.max(0f, hp - suelo - 0.5f));
            setHealth(Math.max(1f, suelo));
            // fase 1: el arbol despierta; fase 2: muerte con honor
            if (fase() == 1) empezarCambio(sl);
            else empezarMuerte(sl);
            return ok;
        }
        boolean ok = super.hurt(src, dmg);
        // fase 2: el Juicio de las espinas al bajar del 70% y del 35% de la vida de la fase
        if (fase() >= 2 && ok) {
            float q = getHealth() / (VIDA / 2f);
            if ((juicios == 0 && q < 0.7f) || (juicios == 1 && q < 0.35f)) {
                juicios++;
                juicioPendiente = true;
            }
        }
        return ok;
    }

    /** Llena la postura; si llega a 100 se le rompe la guardia. */
    private void romper(ServerLevel sl, float v) {
        if (estado != Estado.LUCHA) return;
        postura = Math.min(100f, postura + v);
        if (postura >= 100f) guardiaRota(sl);
    }

    private void guardiaRota(ServerLevel sl) {
        postura = 0;
        atq = null;
        contra = false;
        setPose(POSE_PIE);
        getNavigation().stop();
        anim(GavranAnims.ATURDIDO);
        cambiar(Estado.ATURDIDO);
        sonido(sl, DarkCraftMod.SFX_GAVRAN_GUARDIA_ROTA.get(), 2.2f, 1.0f);
        sl.sendParticles(ParticleTypes.FLASH, getX(), getY() + 3, getZ(), 1, 0, 0, 0, 0);
        sl.sendParticles(ParticleTypes.CRIT, getX(), getY() + 3, getZ(), 40, 1.0, 1.0, 1.0, 0.5);
        for (ServerPlayer p : vivos(sl)) if (p.distanceTo(this) < 24) Logros.dar(p, Logros.GAVRAN_GUARDIA);
    }

    private void aturdido(ServerLevel sl) {
        getMoveControl().setWantedPosition(getX(), getY(), getZ(), 0);
        if (t % 12 == 0) sl.sendParticles(Raices.VERDE, getX(), getY() + 2.5, getZ(), 6, 0.6, 0.4, 0.6, 0);
        if (t == 30) sonido(sl, DarkCraftMod.SFX_CORAZON_LATIDO.get(), 1.4f, 1.0f);
        if (t >= GavranAnims.TICKS[GavranAnims.ATURDIDO]) {
            recup = 6;
            cambiar(Estado.LUCHA);
        }
    }

    @Override
    public void die(DamageSource src) {
        // nunca muere "a lo vanilla": lo lleva empezarMuerte()
        if (level() instanceof ServerLevel sl && estado != Estado.MUERTE && estado != Estado.FIN1 && estado != Estado.CAMBIO) {
            setHealth(1f);
            if (fase() == 1) empezarCambio(sl);
            else empezarMuerte(sl);
        }
    }

    // ------------------------------------------------------------------ cambio de fase (parte B2): el arbol despierta

    /** Lo que dura la cinematica del cambio de fase (ticks) y cuando pasa cada cosa. */
    public static final int CAMBIO_TICKS = 400;
    private static final int C_CAPULLO = 30, C_DESPIERTA = 120, C_ESTALLA = 250;

    private void empezarCambio(ServerLevel sl) {
        if (estado == Estado.CAMBIO) return;
        SalaArbol s = sala();
        atq = null;
        contra = false;
        saltoIni = -1;
        postura = 0;
        atrapado = null;
        enBalcon = -1;
        raices.limpiar();
        quitarEscuderos(sl);
        getNavigation().stop();
        setNoGravity(false);
        this.noPhysics = false;
        // delante del trono, mirando a la sala (la cinematica se rueda alrededor de ese sitio)
        Vec3 c = s != null ? s.aterrizaje() : position();
        moveTo(c.x, c.y, c.z, 0f, 0f);
        setYRot(0f);
        setYBodyRot(0f);
        setYHeadRot(0f);
        setDeltaMovement(Vec3.ZERO);
        setPose(POSE_POSADO);
        anim(0);
        setFlag(F_LUCHA, false);
        setFlag(F_CAMBIO, true);
        cambiar(Estado.CAMBIO);
        List<Shot> planos = s != null ? cambioPlanos(s) : List.of();
        if (s != null) for (Shot sh : planos) s.despejarVista(sl, sh.pos, sh.look);
        for (ServerPlayer p : vivos(sl)) {
            Logros.dar(p, Logros.GAVRAN_FASE1);
            // que nadie se quede dentro de el
            if (p.position().distanceTo(c) < 6 && s != null) {
                Vec3 q = c.add(0, 0, 9);
                p.teleportTo(sl, q.x, s.suelo(sl, q.x, q.z), q.z, 180f, 0f);
            }
            if (!planos.isEmpty()) Cinematics.playShots(p, Cinematics.GAVRAN_CAMBIO, planos, CAMBIO_TICKS / 20f);
        }
        DarkCraftMod.LOGGER.info("[DarkCraft] Ser Gavran: el arbol despierta (fase 2)");
    }

    private void cambio(ServerLevel sl) {
        SalaArbol s = sala();
        getNavigation().stop();
        getMoveControl().setWantedPosition(getX(), getY(), getZ(), 0);
        setDeltaMovement(0, getDeltaMovement().y, 0);
        Vec3 c = position();
        if (t == 10) habla(sl, 11);
        if (t == C_CAPULLO) {
            raices.capullo(sl, c, 90);
            sonido(sl, DarkCraftMod.SFX_RAICES_ATRAPAN.get(), 2.0f, 0.75f);
        }
        if (t == C_CAPULLO + 45) sonido(sl, DarkCraftMod.SFX_MADERA_CRUJE.get(), 2.0f, 0.8f);
        if (t == C_DESPIERTA) {
            sonido(sl, DarkCraftMod.SFX_ARBOL_DESPIERTA.get(), 3.0f, 1.0f);
            if (s != null) raices.despertar(sl, s);
        }
        // hojas que caen de la copa por toda la sala mientras el arbol despierta
        if (s != null && t >= C_DESPIERTA && t < C_ESTALLA + 40 && t % 2 == 0) {
            for (int i = 0; i < 6; i++) {
                double r = SalaArbol.R_TRONCO + random.nextDouble() * (SalaArbol.R_MURO - SalaArbol.R_TRONCO);
                Vec3 p = s.enAnillo(random.nextDouble() * Math.PI * 2, r, s.F() + 17 + random.nextDouble() * 6);
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, (random.nextInt(4) == 0 ? Blocks.FLOWERING_AZALEA_LEAVES
                        : Blocks.AZALEA_LEAVES).defaultBlockState()), p.x, p.y, p.z, 4, 1.5, 1.0, 1.5, 0.02);
            }
            if (t % 40 == 0) sonido(sl, DarkCraftMod.SFX_MADERA_CRUJE.get(), 1.6f, 0.7f + random.nextFloat() * 0.3f);
        }
        if (t == C_ESTALLA) {
            // revienta el capullo: sale mas grande, con corteza y espinas, y el corazon brillando
            ponerFase(2);
            raices.estallar(sl, c);
            sonido(sl, DarkCraftMod.SFX_ESPINAS.get(), 2.6f, 0.8f);
            sonido(sl, DarkCraftMod.SFX_CORTEZA_MURO.get(), 2.4f, 0.7f);
            setPose(POSE_PIE);
            anim(GavranAnims.ALZARSE);
        }
        if (t == C_ESTALLA + GavranAnims.GOLPES_ALZARSE[0]) {
            // ruge: una onda que empuja a todos (sin dano)
            sonido(sl, DarkCraftMod.SFX_RUGIDO.get(), 2.5f, 0.6f);
            sonido(sl, DarkCraftMod.SFX_GAVRAN_GUARDIA_ROTA.get(), 2.0f, 0.6f);
            aviso(sl, Net.Aviso.ONDA, c.x, suelo(sl), c.z, 16f, 0.9f, 0, 18, Net.Aviso.VERDE);
            for (ServerPlayer p : vivos(sl)) {
                double d = p.distanceTo(this);
                if (d < 14) {
                    p.knockback(1.6 * (1 - d / 16), getX() - p.getX(), getZ() - p.getZ());
                    p.setDeltaMovement(p.getDeltaMovement().add(0, 0.35, 0));
                    p.hurtMarked = true;
                }
            }
            sl.sendParticles(Raices.VERDE, c.x, c.y + 4, c.z, 60, 2.0, 1.5, 2.0, 0);
        }
        if (t >= CAMBIO_TICKS) {
            setFlag(F_CAMBIO, false);
            setFlag(F_LUCHA, true);
            recup = 16;
            cds.put(Atq.SUELO, tGlobal + 500);
            cds.put(Atq.HOJAS, tGlobal + 100);
            cds.put(Atq.ATRAPAR, tGlobal + 60);
            cds.put(Atq.BALCON, tGlobal + 200);
            cambiar(Estado.LUCHA);
        }
    }

    /** Planos del cambio de fase, alrededor del sitio delante del trono (donde se queda de rodillas). */
    public static List<Shot> cambioPlanos(SalaArbol s) {
        Vec3 g = s.aterrizaje();
        double x = g.x, y = g.y, z = g.z, F = s.F();
        List<Shot> l = new ArrayList<>();
        // de rodillas, la espada clavada: "¡Basta! Arbol de la Corona..."
        l.add(new Shot(0f, 4.5f, 55, 0, 1, p(x + 5.5, y + 1.8, z + 7.5, x + 4.0, y + 1.6, z + 5.8), p(x, y + 2.2, z, x, y + 2.0, z)));
        // las raices le envuelven: la camara le rodea por delante
        double r = 6.5, a0 = Math.toRadians(70), a1 = Math.toRadians(15), a2 = Math.toRadians(-40);
        l.add(new Shot(4.5f, 9.0f, 58, 0, 0,
                p(x + r * Math.sin(a0), y + 3.0, z + r * Math.cos(a0), x + r * Math.sin(a1), y + 3.4, z + r * Math.cos(a1),
                        x + r * Math.sin(a2), y + 3.8, z + r * Math.cos(a2)),
                p(x, y + 3.0, z, x, y + 3.4, z)));
        // el arbol despierta: desde arriba, raices rompiendo el suelo de la sala
        l.add(new Shot(9.0f, 12.5f, 70, 0, 1, p(x + 12, y + 3, z + 12, x + 9, y + 14, z + 9), p(x, y + 2, z + 2, x + 1, y + 13, z - 1)));
        // revienta el capullo y se alza, mas grande
        l.add(new Shot(12.5f, 16.0f, 62, 0, 1, p(x + 0.8, y + 3.2, z + 9.5, x + 0.5, y + 3.6, z + 8.0), p(x, y + 3.6, z, x, y + 4.2, z)));
        // contrapicado y el titulo
        l.add(new Shot(16.0f, CAMBIO_TICKS / 20f - 0.6f, 64, 0, 2, p(x - 4, y + 1.2, z + 9.5, x - 3, y + 1.0, z + 8.5),
                p(x, y + 4.6, z, x, y + 4.4, z)));
        return l;
    }

    // ------------------------------------------------------------------ fin de cada fase en las partes B1 y B2: se retira a su rama

    private void empezarFin1(ServerLevel sl) {
        if (estado == Estado.FIN1) return;
        atq = null;
        contra = false;
        atrapado = null;
        raices.limpiar();
        quitarEscuderos(sl);
        setNoGravity(false);
        this.noPhysics = false;
        saltoIni = -1;
        setPose(POSE_PIE);
        getNavigation().stop();
        anim(GavranAnims.ATURDIDO);
        cambiar(Estado.FIN1);
        barra.setVisible(false);
        quitarBarra();
    }

    private void fin1(ServerLevel sl) {
        getMoveControl().setWantedPosition(getX(), getY(), getZ(), 0);
        if (t == 24) {
            if (finFase2) sonido(sl, DarkCraftMod.SFX_CORAZON_LATIDO.get(), 1.8f, 0.8f);
            else habla(sl, 9);
        }
        int sube = 84, despega = sube + GavranAnims.GOLPES_SUBIR[0];
        if (t == sube) anim(GavranAnims.SUBIR);
        if (t == despega && rama != null) {
            sonido(sl, DarkCraftMod.SFX_GAVRAN_SALTO.get(), 2.0f, 1.1f);
            saltar(position(), rama, 24, 3.0);
        }
        if (t == despega + 25) {
            posarEnRama();
            setPose(POSE_POSADO);
            hojasCayendo(sl, rama, 40);
            sonido(sl, DarkCraftMod.SFX_MADERA_CRUJE.get(), 1.6f, 1.0f);
            String sub = finFase2 ? "darkcraft.gavran.continuara2" : "darkcraft.gavran.continuara";
            for (ServerPlayer p : vivos(sl)) {
                Logros.dar(p, finFase2 ? Logros.GAVRAN_FASE2 : Logros.GAVRAN_FASE1);
                p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 90, 20));
                p.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("darkcraft.gavran.continuara.titulo")));
                p.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(sub)));
                p.sendSystemMessage(Component.translatable(sub + ".aviso").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
        if (t >= despega + 140) {
            finFase2 = false;
            reiniciar(sl, "fin de la fase (la muerte con honor llega en la parte B3)");
        }
    }

    // ------------------------------------------------------------------ muerte con honor (parte B3)

    /** Lo que dura la cinematica de su muerte (ticks) y cuando pasa cada cosa. */
    public static final int MUERTE_TICKS = 560;
    private static final int M_FRASE17 = 24, M_FRASE18 = 140, M_HOJAS = 278, M_FLORECE = 300, M_COFRE = 410, M_FIN = 560;

    private void empezarMuerte(ServerLevel sl) {
        if (estado == Estado.MUERTE) return;
        SalaArbol s = sala();
        atq = null;
        contra = false;
        saltoIni = -1;
        atrapado = null;
        raices.limpiar();
        quitarEscuderos(sl);
        setNoGravity(false);
        this.noPhysics = false;
        getNavigation().stop();
        // delante del trono, de cara a la sala (donde se arrodilla y donde aparecera el cofre)
        Vec3 c = s != null ? s.aterrizaje() : position();
        moveTo(c.x, c.y, c.z, 0f, 0f);
        setYRot(0f);
        setYBodyRot(0f);
        setYHeadRot(0f);
        setDeltaMovement(Vec3.ZERO);
        setPose(POSE_PIE);
        anim(GavranAnims.MUERTE);
        setFlag(F_LUCHA, false);
        setFlag(F_CAMBIO, false);
        cambiar(Estado.MUERTE);
        barra.setVisible(false);
        quitarBarra();
        List<ServerPlayer> v = vivos(sl);
        List<UUID> todos = new ArrayList<>(jugadores);
        // ha aguantado la segunda fase (el logro de la pelea entera llega cuando sube el cofre)
        for (ServerPlayer p : v) Logros.dar(p, Logros.GAVRAN_FASE2);
        if (s != null) {
            ReyEstado.get(sl.getServer()).ganadores(s.key(), todos);
            List<Shot> planos = muertePlanos(s);
            for (Shot sh : planos) s.despejarVista(sl, sh.pos, sh.look);
            for (ServerPlayer p : v) {
                if (p.position().distanceTo(c) < 6) {
                    Vec3 q = c.add(0, 0, 9);
                    p.teleportTo(sl, q.x, s.suelo(sl, q.x, q.z), q.z, 180f, 0f);
                }
                Cinematics.playShots(p, Cinematics.GAVRAN_MUERTE, planos, MUERTE_TICKS / 20f);
            }
        }
        DarkCraftMod.LOGGER.info("[DarkCraft] Ser Gavran cae con honor ({} jugadores)", todos.size());
    }

    private void muerte(ServerLevel sl) {
        SalaArbol s = sala();
        getNavigation().stop();
        getMoveControl().setWantedPosition(getX(), getY(), getZ(), 0);
        setDeltaMovement(0, getDeltaMovement().y, 0);
        if (t == M_FRASE17) habla(sl, 17);
        if (t == M_FRASE18) habla(sl, 18);
        // se deshace en hojas que suben en remolino
        if (t == M_HOJAS) {
            setFlag(F_HOJAS, true);
            sonido(sl, DarkCraftMod.SFX_HOJAS_DISOLVER.get(), 2.2f, 1.0f);
        }
        if (t >= M_HOJAS && t <= M_HOJAS + 56 && t % 2 == 0) {
            double k = (t - M_HOJAS) / 56.0;
            for (int i = 0; i < 3; i++) {
                double a = t * 0.35 + i * 2.1, r = 1.2 + k * 2.0;
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, (i == 0 ? Blocks.FLOWERING_AZALEA_LEAVES : Blocks.AZALEA_LEAVES).defaultBlockState()),
                        getX() + Math.cos(a) * r, getY() + 0.8 + k * 6, getZ() + Math.sin(a) * r, 8, 0.4, 0.6, 0.4, 0.04);
            }
            sl.sendParticles(ParticleTypes.CHERRY_LEAVES, getX(), getY() + 3, getZ(), 6, 1.2, 1.8, 1.2, 0.02);
            sl.sendParticles(ParticleTypes.GLOW, getX(), getY() + 3, getZ(), 4, 1.0, 1.8, 1.0, 0.02);
        }
        // el arbol florece y se ilumina
        if (t == M_FLORECE && s != null) {
            sonido(sl, DarkCraftMod.SFX_FLORECER.get(), 3.0f, 1.0f);
            s.florecer(sl);
        }
        if (s != null && t >= M_FLORECE && t < M_FIN && t % 3 == 0)
            for (int i = 0; i < 5; i++) {
                double r = SalaArbol.R_TRONCO + random.nextDouble() * (SalaArbol.R_MURO - SalaArbol.R_TRONCO);
                Vec3 p = s.enAnillo(random.nextDouble() * Math.PI * 2, r, s.F() + 16 + random.nextDouble() * 8);
                sl.sendParticles(ParticleTypes.CHERRY_LEAVES, p.x, p.y, p.z, 3, 2.0, 1.0, 2.0, 0.01);
                if (i == 0) sl.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 1.5, 1.0, 1.5, 0.01);
            }
        // a sus pies, el cofre del capitulo 5 sube en un haz de luz
        if (t == M_COFRE && s != null) {
            VictoriaGavran.cofre(sl, s, BlockPos.containing(s.aterrizaje()));
            // desde aqui ya no hay vuelta atras: si el mundo se cierra ahora, sigue muerto (y la puerta se abre)
            ReyEstado.get(sl.getServer()).set(s.key(), ReyEstado.MUERTO);
        }
        if (t == M_COFRE + 30)
            for (UUID u : jugadores) {
                ServerPlayer p = sl.getServer().getPlayerList().getPlayer(u);
                if (p != null) Logros.dar(p, Logros.GAVRAN);
            }
        if (t >= M_FIN) {
            if (s != null) {
                ReyEstado.get(sl.getServer()).set(s.key(), ReyEstado.MUERTO);
                GavranEventos.abrirSala(sl, s);
            }
            DarkCraftMod.LOGGER.info("[DarkCraft] Ser Gavran ya descansa: el arbol ha florecido");
            discard();
        }
    }

    /** Planos de su muerte, alrededor del sitio delante del trono (donde se arrodilla). */
    public static List<Shot> muertePlanos(SalaArbol s) {
        Vec3 g = s.aterrizaje();
        double x = g.x, y = g.y, z = g.z;
        List<Shot> l = new ArrayList<>();
        // cae de rodillas y clava el mandoble: "Bien luchado... el juramento se ha roto... por fin"
        l.add(new Shot(0f, 6.4f, 56, 0, 1, p(x + 5.0, y + 2.2, z + 7.5, x + 3.6, y + 1.8, z + 6.0), p(x, y + 2.4, z, x, y + 2.0, z)));
        // de cerca, la cabeza gacha: "Corvin, amigo mio..."
        l.add(new Shot(6.4f, 13.9f, 46, 0, 1, p(x - 2.6, y + 3.2, z + 5.2, x - 2.0, y + 3.0, z + 4.4), p(x, y + 3.0, z, x, y + 2.9, z)));
        // se deshace en hojas
        l.add(new Shot(13.9f, 16.6f, 60, 0, 1, p(x + 1.0, y + 2.6, z + 8.8, x + 0.6, y + 3.6, z + 8.0), p(x, y + 2.8, z, x, y + 4.8, z)));
        // el arbol florece y se ilumina: la camara sube por el lado este mirando a la rama del norte, que se llena de flores
        l.add(new Shot(16.6f, 21.6f, 70, 0, 1, p(x + 19.5, y + 7, z + 10.5, x + 17.5, y + 11, z + 7.5, x + 15.5, y + 14, z + 5.5),
                p(x, y + 22, z + 7.5, x, y + 24, z + 6.5, x, y + 26, z + 5)));
        // a sus pies, el cofre sube en un haz de luz
        l.add(new Shot(21.6f, MUERTE_TICKS / 20f - 0.6f, 58, 0, 2, p(x + 3.2, y + 2.4, z + 6.4, x + 2.2, y + 1.9, z + 4.8), p(x, y + 0.8, z, x, y + 0.6, z)));
        return l;
    }

    // ------------------------------------------------------------------ volver a empezar

    /** Todos han caido (o /kill, o fin de la fase en B1): vuelve a su rama con toda la vida y se abre la puerta. */
    public void reiniciar(ServerLevel sl, String why) {
        DarkCraftMod.LOGGER.info("[DarkCraft] Ser Gavran vuelve a su rama ({})", why);
        quitarBarra();
        barra.setVisible(false);
        atq = null;
        contra = false;
        saltoIni = -1;
        raices.limpiar();
        quitarEscuderos(sl);
        jugadores.clear();
        postura = 0;
        SalaArbol s = sala();
        if (s != null) {
            ReyEstado.get(sl.getServer()).set(s.key(), ReyEstado.DORMIDO);
            GavranEventos.abrirSala(sl, s);
            Raices.limpiarSueltas(sl, s);
        }
        ponerFase(1);
        setFlag(F_LUCHA, false);
        setFlag(F_INTRO, false);
        setFlag(F_HABLA, false);
        setFlag(F_HOJAS, false);
        setFlag(F_CAMBIO, false);
        juicios = 0;
        juicioPendiente = false;
        corazonDicho = false;
        atrapado = null;
        enBalcon = -1;
        trampas.clear();
        setNoGravity(false);
        this.noPhysics = false;
        anim(0);
        setHealth(getMaxHealth());
        posarEnRama();
        setPose(POSE_POSADO);
        cambiar(Estado.DORMIDO);
        armado = false;
    }

    /** No despierta hasta que la sala se quede vacia (lo usa GavranEventos al ponerle con alguien ya dentro). */
    void desarmar() {
        armado = false;
    }

    @Override
    public void remove(RemovalReason reason) {
        quitarBarra();
        raices.limpiar();
        super.remove(reason);
    }

    private void quitarBarra() {
        barra.removeAllPlayers();
        if (level() instanceof ServerLevel sl)
            for (ServerPlayer p : sl.getServer().getPlayerList().getPlayers())
                p.connection.send(net.minecraft.network.protocol.game.ClientboundBossEventPacket.createRemovePacket(barra.getId()));
    }

    // ------------------------------------------------------------------ pruebas y comandos

    public String debug() {
        return estado + " fase " + fase() + " vida " + Math.round(getHealth()) + (atq != null ? " ataque " + atq + "@" + at : "")
                + (recup > 0 ? " recup " + recup : "") + " postura " + Math.round(postura) + " jugadores " + jugadores.size()
                + " escuderos " + escuderos.size()
                + (fase() >= 2 ? " juicios " + juicios + (juicioPendiente ? "+" : "") + (atrapado != null ? " atrapado" : "")
                + (enBalcon >= 0 ? " en el balcon" : "") + (raices.hayMarea() ? " marea" : "")
                + String.format(Locale.ROOT, " alto %.1f", getBbHeight()) : "");
    }

    /** Para las pruebas y /darkcraft gavran fase 2: en la fase 1 el arbol despierta ya; en la 2 acaba la pelea (B2). */
    public boolean forzarFinFase() {
        if (!(level() instanceof ServerLevel sl) || (estado != Estado.LUCHA && estado != Estado.ATURDIDO)) return false;
        if (fase() == 1) {
            setHealth(VIDA / 2f);
            empezarCambio(sl);
        } else {
            setHealth(1f);
            empezarMuerte(sl);
        }
        return true;
    }

    /** Para la prueba de la CI: pone la vida (el Juicio salta al bajar del 70% de la fase 2). */
    public void ponerVida(float v) {
        setHealth(Mth.clamp(v, 1f, VIDA));
    }

    public int juicios() {
        return juicios;
    }

    public List<Vec3[]> murosJuicio() {
        return murosJuicio;
    }

    public void forzarMuerte() {
        if (level() instanceof ServerLevel sl && estado != Estado.MUERTE) {
            setHealth(1f);
            empezarMuerte(sl);
        }
    }

    /** Para la prueba de la CI: un ataque concreto ya (si esta luchando). */
    public boolean forzarAtaque(String nombre) {
        if (!(level() instanceof ServerLevel sl) || estado != Estado.LUCHA) return false;
        Atq a;
        try {
            a = Atq.valueOf(nombre.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return false;
        }
        List<ServerPlayer> v = vivos(sl);
        if (v.isEmpty()) return false;
        if (atq != null) acabar();
        postura = 0;
        objetivo = v.get(0);
        empezar(sl, a, objetivo);
        return true;
    }

    /** Para la prueba de la CI: le rompe la guardia ya. */
    public boolean forzarGuardiaRota() {
        if (!(level() instanceof ServerLevel sl) || estado != Estado.LUCHA) return false;
        guardiaRota(sl);
        return true;
    }

    /** Para la prueba de la CI: salta la entrada (la pelea empieza ya). */
    public void saltarIntro() {
        if (estado == Estado.INTRO) t = T_LUCHA - 1;
    }

    /** Para la prueba de la CI: si esta en el cambio de fase. */
    public boolean enCambio() {
        return estado == Estado.CAMBIO;
    }
}
