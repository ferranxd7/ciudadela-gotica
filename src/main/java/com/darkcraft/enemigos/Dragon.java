package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.Nido;
import com.darkcraft.net.Net;
import com.darkcraft.rey.Voces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Vorthak, el Dragon de Ceniza (parte 7z; respuesta 132 del plan, "especiales: dragon"): el dragon viejo que guardaba los pasos
 * de las Montanas Nevadas para los reyes de Morvain. Cuando el cielo se volvio rojo se volvio loco: quemo la Atalaya del Cuervo
 * con su guarnicion, se hizo un nido en una repisa de la montana con el oro de los que subian a matarlo y desde entonces no deja
 * subir a nadie. Duerme sobre su tesoro ({@link Nido}).
 * <p>
 * No tiene fisica ({@code noPhysics}, como el Jinete): anda por la repisa (los pies, siempre en lo que pisa) y vuela por encima
 * de ella; todo lo mueve el ({@link #mover}).
 * <ul>
 * <li><b>Dormido</b> (de rodillas, postura {@code REZA}): se despierta si alguien se le acerca a {@link #R_DESPIERTA} bloques y
 * le ve (agachado, a {@link #R_DESPIERTA_AGACHADO}), si le pegan o si alguien abre un cofre a su lado (el oro suena). Al despertar
 * dice su frase, ruge y empieza la pelea.</li>
 * <li><b>En tierra</b>: el <b>mordisco</b> (la cabeza, a 4-10 bloques delante), el <b>zarpazo</b> (la mano derecha barre por
 * delante), el <b>coletazo</b> (a quien se le pone detras), el <b>aletazo</b> (las dos alas de golpe: el viento tira a todos los de
 * delante), el <b>pisoton</b> (se alza y cae con las manos: la onda delante; quien salta se libra) y el <b>aliento</b> (el fuego en
 * cono de su derecha a su izquierda; detras de un penasco no llega). Si se le pega mucho en poco tiempo cae de lado aturdido (y
 * entonces le entra mas). Pasado un rato en tierra alza el vuelo.</li>
 * <li><b>En el aire</b>: da vueltas alto sobre la repisa y ataca: el <b>vuelo rasante</b> (baja y pasa en linea echando fuego por
 * el suelo, que queda ardiendo), las <b>bolas de fuego</b> (se queda en el aire y escupe tres, que revientan en llamas), el
 * <b>picado</b> (sube, marca en el suelo donde va a caer y se lanza: la onda al caer) y aterriza (otra onda) para volver a tierra.
 * Si en el aire se le pega mucho (flechas, hechizos), cae y queda aturdido.</li>
 * <li>A <b>media vida</b> (su frase 2): mas rapido, menos rato en tierra, su aliento deja el suelo ardiendo y la <b>lluvia de
 * ascuas</b>: se queda sobre la repisa, ruge al cielo y caen ascuas por todas partes (una marca avisa donde; bajo techo no
 * llegan).</li>
 * </ul>
 * Habla (ElevenLabs, en ingles con subtitulos; {@link Voces#BASE_DRAGON} + N): 1 al despertar (no la repite en 4 min), 2 a media
 * vida, 3 si cae su presa, 4 "Burn!" de vez en cuando al echar fuego y 5 al caer. Si todos caen o se van, vuelve volando a su
 * tesoro con la vida entera y se vuelve a dormir. Al caer (si vuela, cae del cielo) se desploma y las brasas de sus grietas se
 * apagan; lo demas (el cartel con sus ecos, la hazana {@code dragon} y el bestiario) lo reparte {@code valdrenn/Guardianes}.
 */
public class Dragon extends Guardian {
    public static final String TIPO = "dragon";
    public static final int ECOS = 22000;
    public static final int VOZ_BASE = Voces.BASE_DRAGON;
    private static final EntityDataAccessor<Boolean> VUELA = SynchedEntityData.defineId(Dragon.class, EntityDataSerializers.BOOLEAN);

    /** Lo que anda en tierra (bloques por tick) y lo que gira andando y quieto (grados por tick). */
    public static final double ANDA = 0.17;
    public static final float GIRO_ANDA = 4f, GIRO_QUIETO = 6f;
    /** En el aire: dando vueltas, despacio (al ir a quedarse en el aire), en el rasante y en el picado (bloques por tick). */
    public static final double VUELO = 0.62, VUELO_LENTO = 0.34, V_RASANTE = 0.82, V_PICADO = 1.3;
    public static final float GIRO_VUELO = 4.5f;
    /** Las alturas (sobre el suelo de la repisa) a las que da vueltas, pasa echando fuego, marca el picado, escupe y llama a las ascuas. */
    public static final double ALTO_RONDA = 20, ALTO_RASANTE = 5, ALTO_PICADO = 28, ALTO_BOLAS = 15, ALTO_ASCUAS = 24;
    /** El radio de las vueltas. */
    public static final double R_RONDA = 26;
    /** Lo lejos (del centro de la repisa) que deja ir a su presa antes de dejarla. */
    public static final double CORREA = 62;
    /** A cuanto se despierta (si ve a quien llega); agachado, a cuanto. */
    public static final double R_DESPIERTA = 24, R_DESPIERTA_AGACHADO = 6.5;
    /** Lo que esta en tierra (ticks) antes de alzar el vuelo, antes y despues de media vida. */
    public static final int EN_TIERRA = 400, EN_TIERRA_2 = 300;
    /** Los golpes: el rugido (radio), el pisoton (donde caen las manos y la onda), el aterrizaje, la onda del picado, el aletazo y la cola. */
    public static final double R_RUGIDO = 16, PISOTON_DELANTE = 4.5, R_PISOTON = 6, R_ATERRIZA = 5.5, R_PICADO = 7, R_ALETAZO = 11, COLA = 13.5;
    /** El aliento: lo largo del fuego, lo que se abre (grados a cada lado) y cada cuanto quema a cada uno (ticks). */
    public static final double ALIENTO_LARGO = 17, ALIENTO_ABRE = 13;
    public static final int QUEMA_CADA = 4;
    /** El rasante: el radio de lo que quema en el suelo. */
    public static final double R_RASANTE = 3.2;
    /** La lluvia de ascuas: lo que dura (ticks), cada cuanto cae una, lo que avisa su marca y el radio de cada una. */
    public static final int LLUVIA = 130, ASCUA_CADA = 3, ASCUA_AVISO = 26;
    public static final double R_ASCUA = 2.4;
    /** Lo que tiene que recibir en {@link #VENTANA} ticks (fraccion de su vida) para caer aturdido, en tierra y en el aire. */
    public static final float AGUANTE_TIERRA = 0.07f, AGUANTE_VUELO = 0.035f;
    public static final int VENTANA = 100, ATURDIDO_ESPERA = 500;

    // lo que hace en el aire (maniobra)
    static final int M_NADA = 0, M_SUBIR = 1, M_RONDA = 2, M_RASANTE_ALINEA = 3, M_RASANTE = 4, M_BOLAS = 5, M_PICADO_SUBE = 6,
            M_PICADO_MARCA = 7, M_PICADO = 8, M_ASCUAS_VA = 9, M_ASCUAS = 10, M_BAJAR = 11, M_CAIDA = 12, M_VOLVER = 13;
    private static final String[] MANIOBRAS = {"nada", "subir", "ronda", "rasante (alinea)", "rasante", "bolas", "picado (sube)",
            "picado (marca)", "picado", "ascuas (va)", "ascuas", "bajar", "caida", "volver"};

    static final DustParticleOptions CENIZA = new DustParticleOptions(new Vector3f(0.42f, 0.4f, 0.38f), 1.6f),
            BRASA = new DustParticleOptions(new Vector3f(1.0f, 0.45f, 0.08f), 1.2f);

    /** La ultima vez que dijo su frase de entrada (tiempo del mundo): no la repite en cada intento. */
    private static long ultimaEntrada = Long.MIN_VALUE / 2;

    // ---- el sitio: el centro de la repisa, su suelo y su radio
    private double cx = Double.NaN, cy, cz, radio = 24;
    // ---- el aire
    private int maniobra, maniobraT, ataquesVuelo, ataquesQuiere = 2, rondaT, bolasQuedan, sentido = 1;
    private double velocidad, vy;
    /** El punto al que va (la maniobra), la direccion del rasante y el sitio donde va a caer (el picado, el aterrizaje). */
    private double mx, my, mz, rdx, rdz, sx, sy, sz;
    /** De donde salio el aterrizaje (lo baja hasta el sitio en los ticks de antes del golpe). */
    private double ax, ay, az;
    private boolean ascuasPendiente, trasPicado, vuelveACasa;
    // ---- la tierra
    private int tierraT, altoT, lejosT;
    private double pasoAcum;
    private float rumbo;
    // ---- esperas
    private int mordiscoEspera, zarpazoEspera = 10, coletazoEspera, aletazoEspera = 60, pisotonEspera = 30, alientoEspera = 40, bolasEspera,
            hablaHasta, esperaRugido, recibidoT, aturdidoEspera, caidaEspera, buscarEspera, cofreEspera, ultimoGrito = -100000;
    private float recibido;
    private LivingEntity ultimaPresa;
    /** Hasta que tick no le vuelve a quemar el fuego a cada uno (por su id). */
    private final Map<Integer, Integer> quemaHasta = new HashMap<>();
    /** Las ascuas que van a caer: {x, y (donde cae), z, tick en que cae}. */
    private final List<double[]> ascuas = new ArrayList<>();
    private int lluvia;
    /** Para las pruebas: cuantas veces ha hecho cada cosa y a cuantos ha dado. */
    private int mordiscos, zarpazos, coletazos, golpesCola, aletazos, tirados, pisotones, ondas, alientos, quemados, despegues, aterrizajes,
            rasantes, quemadosRasante, bolas, picados, golpesPicado, lluvias, ascuasCaidas, quemadosAscuas, aturdidos, caidas, rugidos, fuegos,
            tapados;
    /** Para las pruebas: no elige golpes ni maniobras por su cuenta (lo que se le manda hacer, si). */
    public boolean quieta;
    /** En el cliente: lo que corre (bloques por tick, suavizado), lo que anda (para el paso), el cabeceo y el alabeo del vuelo. */
    public float velocidadC, velocidadCO, fase, faseO, cabeceo, cabeceoO, alabeo, alabeoO, subida;

    public Dragon(EntityType<? extends Dragon> t, Level l) {
        super(t, l, TIPO, BossEvent.BossBarColor.RED);
        noPhysics = true;
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 2400.0).add(Attributes.ATTACK_DAMAGE, 15.0).add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 3.0).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(VUELA, false);
    }

    /** Si va por el aire (lo sabe el cliente: el modelo, el aleteo). */
    public boolean vuela() {
        return entityData.get(VUELA);
    }

    private void vuela(boolean v) {
        entityData.set(VUELA, v);
    }

    @Override
    public String tipo() {
        return TIPO;
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable("entity.darkcraft." + TIPO);
    }

    @Override
    public int musica() {
        return 45;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return TIPO;
    }

    @Override
    protected int[] ticks() {
        return DragonAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return DragonAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return DragonAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return DragonAnims.MORIR;
    }

    /** Ruge al empezar y a media vida: en tierra, de pie; si le pilla volando, al cielo (y llama a las ascuas). */
    @Override
    protected int accionRugido() {
        return vuela() ? DragonAnims.ASCUAS : DragonAnims.RUGIDO;
    }

    @Override
    protected int accionLevantarse() {
        return DragonAnims.DESPIERTA;
    }

    @Override
    protected double correa() {
        return CORREA;
    }

    @Override
    protected double alturaMaxima() {
        return 6.0;
    }

    // ------------------------------------------------------------------ cuerpo: ni fisica, ni empujones

    @Override
    protected void registerGoals() {
        // nada: a quien ve y por donde va lo lleva el mismo (buscarPresa, mover)
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public void checkDespawn() {
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        // las alas abiertas y la cola llegan lejos de la caja
        return getBoundingBox().inflate(13.0, 7.0, 13.0);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_DRAGON_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected float getSoundVolume() {
        return 2.6f;
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return null;
    }

    @Override
    public int getMaxHeadYRot() {
        return 10;
    }

    // ------------------------------------------------------------------ para las pruebas y los comandos

    /** Para el comando y las pruebas: que caiga ya. */
    public void vencer() {
        if (level().isClientSide || !isAlive()) return;
        kill();
    }

    /** Para las pruebas: pasa ya a media vida. */
    public void mediaVida() {
        if (level().isClientSide || !isAlive()) return;
        setHealth(Math.min(getHealth(), getMaxHealth() * 0.45f));
    }

    /** Para las pruebas y el comando: que caiga aturdido ya (en el aire, cae primero). */
    public void derribar() {
        if (level().isClientSide || !isAlive()) return;
        aturdidoEspera = ATURDIDO_ESPERA;
        if (vuela()) caer();
        else {
            aturdidos++;
            aturdir();
        }
    }

    /** Para las pruebas y el comando: que alce el vuelo ya (si esta en tierra y despierto). */
    public void despegar() {
        if (level().isClientSide || !isAlive() || vuela() || postura() != DE_PIE) return;
        empezar(DragonAnims.DESPEGA);
    }

    /**
     * Para las pruebas y el comando: que haga ya esa maniobra en el aire ("rasante", "bolas", "picado", "ascuas", "bajar"). Si
     * esta en tierra, primero despega (y la hace al acabar de subir).
     */
    public boolean maniobra(String que) {
        if (level().isClientSide || !isAlive()) return false;
        int m = switch (que) {
            case "rasante" -> M_RASANTE_ALINEA;
            case "bolas" -> M_BOLAS;
            case "picado" -> M_PICADO_SUBE;
            case "ascuas" -> M_ASCUAS_VA;
            case "bajar" -> M_BAJAR;
            default -> -1;
        };
        if (m < 0) return false;
        forzada = m;
        if (!vuela()) despegar();
        else if (accion() == 0 || accion() == DragonAnims.ESCUPE) {
            forzada = -1;
            return empezarManiobra(m, getTarget());
        }
        return true;
    }

    /** La maniobra que se le ha mandado hacer (pruebas), o -1. */
    private int forzada = -1;

    public String maniobraActual() {
        return maniobra >= 0 && maniobra < MANIOBRAS.length ? MANIOBRAS[maniobra] : "?";
    }

    public boolean enTierra() {
        return !vuela();
    }

    public int mordiscos() {
        return mordiscos;
    }

    public int zarpazos() {
        return zarpazos;
    }

    public int coletazos() {
        return coletazos;
    }

    public int golpesCola() {
        return golpesCola;
    }

    public int aletazos() {
        return aletazos;
    }

    public int tirados() {
        return tirados;
    }

    public int pisotones() {
        return pisotones;
    }

    public int ondas() {
        return ondas;
    }

    public int alientos() {
        return alientos;
    }

    public int quemados() {
        return quemados;
    }

    /** Las veces que el fuego del aliento no le llego a alguien que tenia delante (le tapaba algo). */
    public int tapados() {
        return tapados;
    }

    public int despegues() {
        return despegues;
    }

    public int aterrizajes() {
        return aterrizajes;
    }

    public int rasantes() {
        return rasantes;
    }

    public int quemadosRasante() {
        return quemadosRasante;
    }

    public int bolas() {
        return bolas;
    }

    public int picados() {
        return picados;
    }

    public int golpesPicado() {
        return golpesPicado;
    }

    public int lluvias() {
        return lluvias;
    }

    public int ascuasCaidas() {
        return ascuasCaidas;
    }

    public int quemadosAscuas() {
        return quemadosAscuas;
    }

    public int aturdidos() {
        return aturdidos;
    }

    public int caidas() {
        return caidas;
    }

    public int rugidos() {
        return rugidos;
    }

    /** Los fuegos que ha dejado en el suelo. */
    public int fuegos() {
        return fuegos;
    }

    public int ascuasEnCamino() {
        return ascuas.size();
    }

    /** El centro de la repisa y su suelo (pruebas): {x, y, z}. */
    public double[] centroRepisa() {
        centro();
        return new double[]{cx, cy, cz};
    }

    // ------------------------------------------------------------------ el sitio

    /** El centro de la repisa (la del nido, si esta en ella; si no, donde duerme) y lo que se pisa en ella. */
    private void centro() {
        if (!Double.isNaN(cx)) return;
        BlockPos h = hogar();
        if (Nido.distancia(getX(), getZ()) < 90) {
            cx = Nido.CX + 0.5;
            cz = Nido.CZ + 0.5;
            cy = Nido.PISO;
            radio = Nido.R_ARENA;
        } else {
            cx = h != null ? h.getX() + 0.5 : getX();
            cz = h != null ? h.getZ() + 0.5 : getZ();
            cy = h != null ? h.getY() : getY();
            radio = 24;
        }
    }

    private double alCentro(double x, double z) {
        return Math.hypot(x - cx, z - cz);
    }

    /** Lo alto de lo que se pisa (encima de un bloque entero) bajo (x, z), buscando de {@code y} + 4 hacia abajo; NaN si nada. */
    private double pisable(double x, double z, double y) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int bx = Mth.floor(x), bz = Mth.floor(z), y0 = Mth.floor(y) + 4;
        if (!level().hasChunkAt(new BlockPos(bx, y0, bz))) return Double.NaN;
        for (int by = y0; by >= y0 - 24; by--) {
            m.set(bx, by, bz);
            BlockState s = level().getBlockState(m);
            if (s.isAir()) continue;
            if (s.isFaceSturdy(level(), m, Direction.UP)) return by + 1;
        }
        return Double.NaN;
    }

    /** Lo alto del terreno (el primer aire encima de lo mas alto) en (x, z); si ese chunk no esta, el suelo de la repisa. */
    private double terreno(double x, double z) {
        int bx = Mth.floor(x), bz = Mth.floor(z);
        if (!level().hasChunkAt(new BlockPos(bx, (int) cy, bz))) return cy;
        return level().getHeight(Heightmap.Types.MOTION_BLOCKING, bx, bz);
    }

    /** Lo alto que tiene que ir volando para no dar en la montana: lo que quiere o 6 por encima del terreno (aqui y delante). */
    private double sobreTerreno(double y, double x, double z, float yaw) {
        return sobreTerreno(y, x, z, yaw, 6);
    }

    private double sobreTerreno(double y, double x, double z, float yaw, double margen) {
        Vec3 f = Vec3.directionFromRotation(0, yaw);
        double h = Math.max(terreno(x, z), Math.max(terreno(x + f.x * 8, z + f.z * 8), terreno(x + f.x * 16, z + f.z * 16)));
        return Math.max(y, h + margen);
    }

    /** Los pies en lo que pisa (suave: no salta los bultos del oro). */
    private void pegarAlSuelo() {
        double s = pisable(getX(), getZ(), getY());
        if (Double.isNaN(s)) return;
        double y = getY() + Mth.clamp(s - getY(), -0.45, 0.4);
        if (Math.abs(y - getY()) > 1e-4) setPos(getX(), y, getZ());
    }

    // ------------------------------------------------------------------ a quien ve

    /** Dormido: quien se le acerca y le ve (agachado, mucho mas cerca); despierto: cualquiera en su repisa o cerca de ella. */
    private boolean ve(Player p) {
        if (!p.isAlive() || p.isCreative() || p.isSpectator()) return false;
        if (postura() != DE_PIE) {
            double r = p.isCrouching() ? R_DESPIERTA_AGACHADO : R_DESPIERTA;
            return distanceToSqr(p) < r * r && hasLineOfSight(p);
        }
        return alCentro(p.getX(), p.getZ()) < CORREA && Math.abs(p.getY() - cy) < 40;
    }

    /** Si sigue con esa presa: viva, a la que se puede pegar y no demasiado lejos de su repisa. */
    private boolean sigue(LivingEntity t) {
        return t.isAlive() && objetivoValido(t) && alCentro(t.getX(), t.getZ()) < CORREA && Math.abs(t.getY() - cy) < 45;
    }

    /** Busca presa (cada cuarto de segundo): la mas cercana que vea; peleando, se cambia a quien tenga mucho mas cerca. */
    private void buscarPresa() {
        if (--buscarEspera > 0 || !(level() instanceof ServerLevel sl)) return;
        buscarEspera = 5;
        LivingEntity t = getTarget();
        Player mejor = null;
        double md = Double.MAX_VALUE;
        for (ServerPlayer p : sl.players()) {
            if (!ve(p)) continue;
            double d = p.distanceToSqr(this);
            if (d < md) {
                md = d;
                mejor = p;
            }
        }
        if (mejor == null) return;
        if (t == null) setTarget(mejor);
        else if (mejor != t && tickCount % 100 < 20 && md < t.distanceToSqr(this) * 0.36) setTarget(mejor);
    }

    @Override
    public void setTarget(LivingEntity t) {
        super.setTarget(t);
        if (t != null && !level().isClientSide) ultimaPresa = t;
    }

    // ------------------------------------------------------------------ lo que dice

    /** Dice su frase n (1 al despertar, 2 a media vida, 3 si cae su presa, 4 "Burn!", 5 al caer) a quien este cerca. */
    public void habla(int n) {
        if (!(level() instanceof ServerLevel sl)) return;
        int linea = VOZ_BASE + n;
        for (ServerPlayer p : sl.players()) if (p.distanceToSqr(this) < 96 * 96) Net.to(p, new Net.ReyHabla(getId(), linea));
        hablaHasta = tickCount + Voces.ticks(linea);
    }

    private boolean hablando() {
        return tickCount < hablaHasta + Voces.PAUSA;
    }

    private void hablaSiCalla(int n) {
        if (!hablando()) habla(n);
    }

    /** "Burn!": de vez en cuando (no cada vez) al echar fuego, si no esta hablando. */
    private void grito() {
        if (tickCount - ultimoGrito < 900 || hablando() || random.nextFloat() > 0.6f) return;
        ultimoGrito = tickCount;
        habla(4);
    }

    @Override
    protected void alEmpezarPelea() {
        mordiscoEspera = 0;
        zarpazoEspera = 10;
        coletazoEspera = 0;
        aletazoEspera = 60;
        pisotonEspera = 30;
        alientoEspera = 40;
        tierraT = 0;
        ascuasPendiente = false;
        vuelveACasa = false;
        long ahora = level().getGameTime();
        if (ahora - ultimaEntrada > 20 * 60 * 4 || ahora < ultimaEntrada) {
            ultimaEntrada = ahora;
            habla(1);
            // se despierta y le mira mientras lo dice; al acabar ruge (y entonces pelea)
            int dura = Voces.ticks(VOZ_BASE + 1);
            esperaRugido = tickCount + dura - 4;
            pausa = Math.max(pausa, dura);
        } else esperaRugido = 0;
    }

    /** El primer rugido: al acabar su frase, y en tierra (si la pelea empieza con el en el aire, al posarse). */
    @Override
    protected boolean puedeRugir() {
        return !vuela() && tickCount >= esperaRugido;
    }

    @Override
    protected void alAcabarPelea() {
        ascuas.clear();
        lluvia = 0;
        ascuasPendiente = false;
        vuelveACasa = true;
        esperaRugido = 0;
        if (vuela() && accion() != DragonAnims.ATERRIZA) empezarManiobra(M_VOLVER, null);
    }

    /** A media vida (su frase 2): ruge (en tierra) o llama a las ascuas (si vuela); desde entonces, menos rato en tierra. */
    @Override
    protected void alSegundaMitad() {
        hablaHasta = 0;
        habla(2);
        if (vuela()) {
            // le pillo en el aire: ruge al cielo donde esta (la accion ya empezada es la de las ascuas)
            maniobra = M_ASCUAS;
            maniobraT = 0;
        } else {
            ascuasPendiente = true;
            tierraT = Math.max(tierraT, EN_TIERRA_2 - 60);
        }
    }

    // ------------------------------------------------------------------ cada tick

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) return;
        // lo que corre, lo que anda, lo que sube y lo que se ladea al girar (para el modelo)
        double dx = getX() - xo, dz = getZ() - zo, dy = getY() - yo;
        float v = (float) Math.sqrt(dx * dx + dz * dz);
        velocidadCO = velocidadC;
        velocidadC += (v - velocidadC) * 0.3f;
        faseO = fase;
        fase += v;
        subida += ((float) dy - subida) * 0.25f;
        cabeceoO = cabeceo;
        alabeoO = alabeo;
        float quiereCabeceo = 0f, quiereAlabeo = 0f;
        if (vuela() && !muriendo()) {
            int a = accionVista();
            boolean recto = a == 0 || a == DragonAnims.RASANTE || a == DragonAnims.ESCUPE || a == DragonAnims.PICADO;
            if (recto && velocidadC > 0.08f) {
                // el morro arriba al subir, abajo al bajar (en el picado, mucho)
                quiereCabeceo = (float) Math.toDegrees(Math.atan2(subida, Math.max(0.2f, velocidadC)));
                quiereCabeceo = Mth.clamp(quiereCabeceo, -40f, 28f);
                float giro = Mth.wrapDegrees(getYRot() - yRotO);
                quiereAlabeo = Mth.clamp(-giro * 5.5f, -32f, 32f);
            }
        }
        cabeceo += (quiereCabeceo - cabeceo) * 0.15f;
        alabeo += (quiereAlabeo - alabeo) * 0.12f;
    }

    @Override
    public void aiStep() {
        // primero se mueve (como el jinete: sin fisica; lo que hace cada accion lo pone Enemigo despues)
        if (!level().isClientSide && isAlive()) mover();
        super.aiStep();
        if (level().isClientSide || !isAlive() || !(level() instanceof ServerLevel sl)) return;
        if (mordiscoEspera > 0) mordiscoEspera--;
        if (zarpazoEspera > 0) zarpazoEspera--;
        if (coletazoEspera > 0) coletazoEspera--;
        if (aletazoEspera > 0) aletazoEspera--;
        if (pisotonEspera > 0) pisotonEspera--;
        if (alientoEspera > 0) alientoEspera--;
        if (bolasEspera > 0) bolasEspera--;
        if (aturdidoEspera > 0) aturdidoEspera--;
        if (caidaEspera > 0) caidaEspera--;
        if (recibidoT > 0 && --recibidoT == 0) recibido = 0;
        // en guardia mientras pelea (el modelo pasa de quieto a en guardia)
        boolean guardia = enPelea() && getTarget() != null;
        if (isAggressive() != guardia) setAggressive(guardia);
        if (tickCount % 40 == 0) quemaHasta.values().removeIf(h -> h < tickCount);
        lluvia(sl);
        // dormido: si alguien abre un cofre a su lado, el oro suena y se despierta
        if (postura() != DE_PIE && getTarget() == null && --cofreEspera <= 0) {
            cofreEspera = 10;
            for (ServerPlayer p : sl.players())
                if (!p.isCreative() && !p.isSpectator() && p.distanceToSqr(this) < 14 * 14 && p.containerMenu instanceof ChestMenu) {
                    DarkCraftMod.LOGGER.info("[DarkCraft] Vorthak se despierta: {} le ha abierto el oro", p.getName().getString());
                    setTarget(p);
                    break;
                }
        }
        // ha caido aquel con quien peleaba
        if (enPelea() && ultimaPresa != null && ultimaPresa.isDeadOrDying()) {
            ultimaPresa = null;
            hablaSiCalla(3);
        }
    }

    /** Todo lo que se mueve: a quien sigue, la accion que hace, o en tierra o en el aire. */
    private void mover() {
        setDeltaMovement(Vec3.ZERO);
        centro();
        LivingEntity t = getTarget();
        if (t != null && !sigue(t)) {
            setTarget(null);
            t = null;
        }
        if (t == null) {
            buscarPresa();
            t = getTarget();
        } else if (tickCount % 20 == 0) {
            buscarEspera = 0;
            buscarPresa();
        }
        int a = accion();
        if (a != 0) {
            moverEnAccion(a, t);
            return;
        }
        if (postura() != DE_PIE) return;
        if (vuela()) volar(t);
        else tierra(t);
    }

    // ------------------------------------------------------------------ en tierra

    private void tierra(LivingEntity t) {
        pegarAlSuelo();
        if (t == null) {
            // sin presa: si esta lejos de su tesoro, vuelve volando; si no, nada (Enemigo le echa a dormir pasado un rato)
            BlockPos h = hogar();
            if (h != null && !enPelea() && pausa <= 0 && Math.hypot(getX() - h.getX() - 0.5, getZ() - h.getZ() - 0.5) > 3.5) {
                vuelveACasa = true;
                empezar(DragonAnims.DESPEGA);
            }
            return;
        }
        tierraT++;
        double dx = t.getX() - getX(), dz = t.getZ() - getZ(), d = Math.sqrt(dx * dx + dz * dz);
        // mientras dice su frase le mira (no pega ni anda)
        if (tickCount < esperaRugido) {
            girarHacia(t, GIRO_QUIETO * 0.5f);
            return;
        }
        if (!quieta && pausa <= 0 && quiereVolar(t, d)) {
            empezar(DragonAnims.DESPEGA);
            return;
        }
        if (forzada >= 0 && pausa <= 0) {
            empezar(DragonAnims.DESPEGA);
            return;
        }
        if (pausa <= 0 && Math.abs(t.getY() - getY()) < alturaMaxima()) {
            int n = elegir(t, d);
            if (n != 0) {
                if (n != DragonAnims.COLETAZO) girarHacia(t, 12f);
                empezar(n);
                return;
            }
        }
        float ang = angulo(t.getX(), t.getZ());
        if (d > 7.5) andarHacia(t.getX(), t.getZ(), ANDA * (segunda ? 1.15 : 1.0), GIRO_ANDA);
        else if (ang > 12) girarHacia(t, GIRO_QUIETO);
    }

    /** Si ya toca alzar el vuelo: lleva mucho en tierra, su presa esta en alto o lejos, o llama a las ascuas. */
    private boolean quiereVolar(LivingEntity t, double d) {
        if (ascuasPendiente && tierraT > 30) return true;
        if (tierraT > (segunda ? EN_TIERRA_2 : EN_TIERRA)) return true;
        altoT = t.getY() - getY() > 6 ? altoT + 1 : 0;
        lejosT = d > 22 ? lejosT + 1 : 0;
        return altoT > 40 || lejosT > 60;
    }

    @Override
    protected int elegir(LivingEntity t, double d) {
        if (quieta) return 0;
        float ang = angulo(t.getX(), t.getZ());
        boolean detras = ang > 115;
        if (detras && d < COLA && coletazoEspera <= 0) {
            coletazoEspera = (segunda ? 60 : 80) + random.nextInt(30);
            return DragonAnims.COLETAZO;
        }
        if (d < 4.2 && pisotonEspera <= 0) {
            pisotonEspera = (segunda ? 80 : 110) + random.nextInt(40);
            return DragonAnims.PISOTON;
        }
        if (aletazoEspera <= 0 && cuantosCerca(10) >= 2 && random.nextFloat() < 0.5f) {
            aletazoEspera = 160 + random.nextInt(80);
            return DragonAnims.ALETAZO;
        }
        if (d < 7 && ang < 75 && zarpazoEspera <= 0) {
            zarpazoEspera = (segunda ? 34 : 46) + random.nextInt(20);
            return DragonAnims.ZARPAZO;
        }
        if (d >= 4 && d <= 10 && ang < 30 && mordiscoEspera <= 0) {
            mordiscoEspera = (segunda ? 22 : 32) + random.nextInt(20);
            return DragonAnims.MORDISCO;
        }
        if (d >= 5 && d <= 16 && ang < 40 && alientoEspera <= 0) {
            alientoEspera = (segunda ? 150 : 200) + random.nextInt(60);
            return DragonAnims.ALIENTO;
        }
        if (d < 6 && aletazoEspera <= 0 && random.nextFloat() < 0.3f) {
            aletazoEspera = 160 + random.nextInt(80);
            return DragonAnims.ALETAZO;
        }
        return 0;
    }

    @Override
    protected int pausaTras(int accion) {
        int p = switch (accion) {
            case DragonAnims.MORDISCO -> 10 + random.nextInt(8);
            case DragonAnims.ZARPAZO -> 12 + random.nextInt(8);
            case DragonAnims.COLETAZO -> 14;
            case DragonAnims.ALETAZO -> 16;
            case DragonAnims.PISOTON -> 20;
            case DragonAnims.ALIENTO -> 24;
            case DragonAnims.ATERRIZA -> 10;
            case DragonAnims.ATURDIDO -> 6;
            case DragonAnims.RUGIDO, DragonAnims.DESPIERTA, DragonAnims.DESPEGA, DragonAnims.ESCUPE, DragonAnims.ASCUAS, DragonAnims.RASANTE,
                 DragonAnims.PICADO -> 2;
            default -> 12;
        };
        return segunda ? Math.max(1, p * 7 / 10) : p;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case DragonAnims.MORDISCO -> new Golpe(10.5f, 56f, 1.4f, 0.6f);
            case DragonAnims.ZARPAZO -> new Golpe(7.0f, 150f, 1.0f, 0.9f);
            default -> null;
        };
    }

    /** El mordisco no llega a lo que tiene bajo el pecho (la cabeza esta delante); el zarpazo, a todo lo de delante. */
    @Override
    protected boolean pilla(int accion, int n, LivingEntity le) {
        if (accion == DragonAnims.MORDISCO) return Math.hypot(le.getX() - getX(), le.getZ() - getZ()) >= 3.8;
        return true;
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (accion == DragonAnims.MORDISCO) mordiscos++;
        else if (accion == DragonAnims.ZARPAZO) {
            zarpazos++;
            // la mano barre de su derecha a su izquierda: le echa a su izquierda
            Vec3 f = Vec3.directionFromRotation(0, getYRot());
            empujar(le, 0.9, f.z, -f.x, 0.2);
        }
    }

    /** Anda hacia (x, z): gira como mucho {@code giro} grados; si tiene que girar mucho, primero gira (es enorme). */
    private void andarHacia(double x, double z, double paso, float giro) {
        double dx = x - getX(), dz = z - getZ(), d = Math.sqrt(dx * dx + dz * dz);
        if (d < 0.05) return;
        float quiere = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f;
        float y = Mth.approachDegrees(getYRot(), quiere, giro);
        setYRot(y);
        yBodyRot = yHeadRot = y;
        float dif = Math.abs(Mth.wrapDegrees(quiere - y));
        double k = dif > 60 ? 0.15 : dif > 30 ? 0.55 : 1.0;
        double l = Math.min(d, paso * k);
        Vec3 f = Vec3.directionFromRotation(0, y);
        double nx = getX() + f.x * l, nz = getZ() + f.z * l;
        if (Nido.distancia(nx, nz) < Nido.R_MEZCLA + 10) {
            double[] q = Nido.apartar(nx, nz, 2.6);
            nx = q[0];
            nz = q[1];
        }
        double hecho = Math.hypot(nx - getX(), nz - getZ());
        setPos(nx, getY(), nz);
        pegarAlSuelo();
        // sus pasos: cada dos bloques y medio, un golpe sordo
        pasoAcum += hecho;
        if (pasoAcum > 2.6) {
            pasoAcum = 0;
            playSound(DarkCraftMod.SFX_DRAGON_PISOTON.get(), 0.9f, 1.35f + random.nextFloat() * 0.15f);
        }
    }

    /** Lo que se desvia (x, z) de hacia donde mira (grados, 0 = de frente). */
    private float angulo(double x, double z) {
        float quiere = (float) (Mth.atan2(z - getZ(), x - getX()) * Mth.RAD_TO_DEG) - 90f;
        return Math.abs(Mth.wrapDegrees(quiere - getYRot()));
    }

    /** Gira (sin moverse) hacia (x, z). */
    private void encarar(double x, double z, float max) {
        float quiere = (float) (Mth.atan2(z - getZ(), x - getX()) * Mth.RAD_TO_DEG) - 90f;
        float y = Mth.approachDegrees(getYRot(), quiere, max);
        setYRot(y);
        yBodyRot = yHeadRot = y;
    }

    private int cuantosCerca(double r) {
        return level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(r, 3, r), e -> e != this && e.isAlive() && objetivoValido(e)).size();
    }

    /** El punto a {@code delante} bloques por delante, {@code derecha} a su derecha y {@code arriba} por encima de sus pies. */
    private Vec3 delante(double delante, double derecha, double arriba) {
        Vec3 f = Vec3.directionFromRotation(0, getYRot());
        return new Vec3(getX() + f.x * delante - f.z * derecha, getY() + arriba, getZ() + f.z * delante + f.x * derecha);
    }

    // ------------------------------------------------------------------ cada accion

    @Override
    public void empezar(int accion) {
        super.empezar(accion);
        if (accion == DragonAnims.ATERRIZA) {
            ax = getX();
            ay = getY();
            az = getZ();
        }
        rumbo = getYRot();
    }

    /** Lo que se mueve haciendo cada cosa. */
    private void moverEnAccion(int a, LivingEntity t) {
        int k = accionT;
        switch (a) {
            case DragonAnims.DESPEGA -> {
                if (k < DragonAnims.DESPEGA_SALTO) {
                    pegarAlSuelo();
                    if (t != null) encarar(t.getX(), t.getZ(), 2f);
                } else {
                    // se eleva: el empujon de las alas y luego sube, cada vez mas adelante
                    double sube = k < DragonAnims.DESPEGA_SALTO + 6 ? 0.5 : 0.32;
                    velocidad = Math.min(0.42, velocidad + 0.02);
                    vy = sube;
                    Vec3 f = Vec3.directionFromRotation(0, getYRot());
                    setPos(getX() + f.x * velocidad, getY() + sube, getZ() + f.z * velocidad);
                }
            }
            case DragonAnims.ATERRIZA -> {
                int g = DragonAnims.GOLPES[a][0];
                if (k <= g) {
                    // baja hasta su sitio en los ticks de antes del golpe
                    double u = Math.min(1.0, k / (double) g);
                    u = u * u;
                    setPos(ax + (sx - ax) * u, ay + (sy - ay) * u, az + (sz - az) * u);
                } else pegarAlSuelo();
            }
            case DragonAnims.RASANTE -> {
                // recto por su linea, a ras de suelo
                float yaw = (float) (Mth.atan2(rdz, rdx) * Mth.RAD_TO_DEG) - 90f;
                setYRot(Mth.approachDegrees(getYRot(), yaw, 2f));
                yBodyRot = yHeadRot = getYRot();
                velocidad += Mth.clamp(V_RASANTE - velocidad, -0.04, 0.04);
                Vec3 f = Vec3.directionFromRotation(0, getYRot());
                double yq = sobreTerreno(my, getX(), getZ(), getYRot(), 4.5);
                vy += Mth.clamp(Mth.clamp((yq - getY()) * 0.15, -0.35, 0.35) - vy, -0.05, 0.05);
                setPos(getX() + f.x * velocidad, getY() + vy, getZ() + f.z * velocidad);
            }
            case DragonAnims.ESCUPE -> {
                // se queda en el aire de cara a su presa
                if (t != null) encarar(t.getX(), t.getZ(), 5f);
                quedarseEnElAire(my);
            }
            case DragonAnims.PICADO -> {
                if (maniobra == M_CAIDA) caida();
                else picar();
            }
            case DragonAnims.ASCUAS -> quedarseEnElAire(Double.NaN);
            case DragonAnims.ATURDIDO, DragonAnims.MORIR -> pegarAlSuelo();
            case DragonAnims.COLETAZO -> pegarAlSuelo();
            case DragonAnims.ALIENTO -> {
                pegarAlSuelo();
                if (t != null && k < 14) encarar(t.getX(), t.getZ(), 3f);
            }
            case DragonAnims.DESPIERTA, DragonAnims.RUGIDO -> {
                pegarAlSuelo();
                if (t != null && k > 24) encarar(t.getX(), t.getZ(), 2.5f);
            }
            default -> {
                pegarAlSuelo();
                // mientras se prepara sigue a su presa con el cuerpo; poco antes del golpe ya no gira (apartarse es esquivar)
                int[] g = DragonAnims.GOLPES[a];
                int fija = g.length == 0 ? 0 : g[0] - 4;
                if (t != null && k < fija) encarar(t.getX(), t.getZ(), k < 4 ? 8f : 3f);
            }
        }
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        if (!(level() instanceof ServerLevel sl)) return;
        int[] g = DragonAnims.GOLPES[accion];
        switch (accion) {
            case DragonAnims.DESPIERTA -> {
                if (t == 2) playSound(DarkCraftMod.SFX_DRAGON_RESPIRA.get(), 2.4f, 0.8f);
                if (t == 30) sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 3, getZ(), 16, 2.0, 1.0, 2.0, 0.02);
                // (el rugido de verdad es el de la pelea, al acabar su frase)
                if (t == 36 && !hablando()) playSound(DarkCraftMod.SFX_DRAGON_RUGIDO.get(), 3.0f, 0.9f);
            }
            case DragonAnims.RUGIDO -> {
                if (t == 8 && !hablando()) playSound(DarkCraftMod.SFX_DRAGON_RUGIDO.get(), 5.0f, 0.95f);
                if (t == 17) rugir(sl);
            }
            case DragonAnims.MORDISCO -> {
                if (t == 4) playSound(DarkCraftMod.SFX_DRAGON_RESPIRA.get(), 1.4f, 1.3f);
                if (t == g[0] - 2) playSound(DarkCraftMod.SFX_DRAGON_MORDISCO.get(), 2.6f, 0.9f + random.nextFloat() * 0.15f);
                if (t == g[0]) {
                    Vec3 b = delante(8.4, 0, 0.8);
                    sl.sendParticles(ParticleTypes.CRIT, b.x, b.y, b.z, 12, 0.6, 0.4, 0.6, 0.2);
                }
            }
            case DragonAnims.ZARPAZO -> {
                if (t == g[0] - 4) playSound(DarkCraftMod.SFX_DRAGON_COLA.get(), 2.0f, 1.3f);
                if (t == g[0]) estela(150, 5.2, 1.2);
            }
            case DragonAnims.COLETAZO -> {
                if (t == g[0] - 6) playSound(DarkCraftMod.SFX_DRAGON_COLA.get(), 3.0f, 0.95f);
                if (t == g[0]) coletazo(sl);
            }
            case DragonAnims.ALETAZO -> {
                if (t == 6) playSound(DarkCraftMod.SFX_DRAGON_DESPEGA.get(), 3.0f, 1.15f);
                if (t == g[0]) aletazo(sl);
            }
            case DragonAnims.PISOTON -> {
                if (t == 8) playSound(DarkCraftMod.SFX_DRAGON_RUGIDO.get(), 2.6f, 1.2f);
                if (t == g[0] - 2) playSound(DarkCraftMod.SFX_DRAGON_PISOTON.get(), 4.0f, 0.9f);
                if (t == g[0]) {
                    pisotones++;
                    Vec3 p = delante(PISOTON_DELANTE, 0, 0);
                    ondas += onda(sl, p, R_PISOTON, 1.3f, 1.2, 0.45, true);
                    polvo(sl, p, R_PISOTON);
                }
            }
            case DragonAnims.ALIENTO -> {
                if (t == 3) playSound(DarkCraftMod.SFX_DRAGON_RESPIRA.get(), 2.2f, 1.15f);
                if (t == 6) grito();
                if (t == DragonAnims.ALIENTO_DESDE - 2) {
                    alientos++;
                    playSound(DarkCraftMod.SFX_DRAGON_FUEGO.get(), 4.0f, 0.95f);
                }
                if (t >= DragonAnims.ALIENTO_DESDE && t <= DragonAnims.ALIENTO_HASTA) aliento(sl, t);
            }
            case DragonAnims.DESPEGA -> {
                if (t == 3) playSound(DarkCraftMod.SFX_DRAGON_DESPEGA.get(), 4.0f, 1.0f);
                if (t == DragonAnims.DESPEGA_SALTO) {
                    despegues++;
                    vuela(true);
                    maniobra = M_NADA;
                    velocidad = 0.05;
                    // el golpe de las alas: levanta ceniza y aparta a los de alrededor
                    polvo(sl, position(), 8);
                    for (LivingEntity le : cerca(position(), 9))
                        empujar(le, 1.0, le.getX() - getX(), le.getZ() - getZ(), 0.35);
                }
            }
            case DragonAnims.ATERRIZA -> {
                if (t == 0) playSound(DarkCraftMod.SFX_DRAGON_ATERRIZA.get(), 4.0f, 1.0f);
                if (t == g[0]) aterrizar(sl);
            }
            case DragonAnims.RASANTE -> {
                if (t == 2) grito();
                if (t == DragonAnims.RASANTE_DESDE - 3) {
                    rasantes++;
                    playSound(DarkCraftMod.SFX_DRAGON_FUEGO.get(), 5.0f, 0.9f);
                }
                if (t >= DragonAnims.RASANTE_DESDE && t <= DragonAnims.RASANTE_HASTA) rasante(sl, t);
            }
            case DragonAnims.ESCUPE -> {
                if (t == g[0] - 3) playSound(DarkCraftMod.SFX_DRAGON_FUEGO.get(), 2.6f, 1.5f);
                if (t == g[0]) escupir(sl);
            }
            case DragonAnims.PICADO -> {
                if (t == 1 && maniobra != M_CAIDA) playSound(DarkCraftMod.SFX_DRAGON_PICADO.get(), 5.0f, 1.0f);
            }
            case DragonAnims.ASCUAS -> {
                if (t == 4) playSound(DarkCraftMod.SFX_DRAGON_DESPEGA.get(), 3.0f, 0.9f);
                if (t == DragonAnims.ASCUAS_RUGIDO) {
                    if (!hablando()) playSound(DarkCraftMod.SFX_DRAGON_RUGIDO.get(), 6.0f, 0.85f);
                    empezarLluvia(sl);
                }
            }
            case DragonAnims.ATURDIDO -> {
                if (t == 2) playSound(DarkCraftMod.SFX_DRAGON_DOLOR.get(), 3.0f, 0.8f);
                if (t == 12) {
                    playSound(DarkCraftMod.SFX_DRAGON_PISOTON.get(), 3.0f, 0.7f);
                    polvo(sl, position(), 6);
                    sl.sendParticles(ParticleTypes.CRIT, getX(), getY() + 4, getZ(), 18, 1.0, 0.6, 1.0, 0.2);
                }
            }
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ los golpes de tierra

    /** El rugido: a los de cerca se les encoge el corazon (van mas despacio y pegan menos un rato). */
    private void rugir(ServerLevel sl) {
        rugidos++;
        Vec3 b = delante(7.0, 0, 5.0);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, b.x, b.y, b.z, 24, 1.2, 0.8, 1.2, 0.06);
        sl.sendParticles(BRASA, b.x, b.y, b.z, 30, 1.6, 1.0, 1.6, 0.0);
        for (LivingEntity le : cerca(position(), R_RUGIDO)) {
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), this);
            le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0), this);
        }
    }

    /** El coletazo: la cola barre lo que tiene detras (a quien se le ha puesto a la espalda) y lo echa a un lado. */
    private void coletazo(ServerLevel sl) {
        coletazos++;
        Vec3 f = Vec3.directionFromRotation(0, getYRot());
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.15f;
        for (LivingEntity le : cerca(position(), COLA)) {
            double dx = le.getX() - getX(), dz = le.getZ() - getZ(), d = Math.sqrt(dx * dx + dz * dz);
            if (d < 2.5 || (dx * f.x + dz * f.z) / Math.max(1e-3, d) > -0.4) continue;
            if (!le.hurt(damageSources().mobAttack(this), dano)) continue;
            golpesCola++;
            empujar(le, 1.5, dx - f.z * d * 0.6, dz + f.x * d * 0.6, 0.3);
        }
        for (int j = 0; j <= 12; j++) {
            double ang = Math.toRadians(getYRot() + 90 + 180 - 70 + 140 * j / 12.0);
            double x = getX() + Math.cos(ang) * 9.0, z = getZ() + Math.sin(ang) * 9.0;
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, x, getY() + 1.2, z, 1, 0, 0, 0, 0);
            sl.sendParticles(CENIZA, x, getY() + 0.6, z, 2, 0.4, 0.2, 0.4, 0.0);
        }
    }

    /** El aletazo: el viento de las alas tira a todos los de delante (casi sin dano) y los echa lejos. */
    private void aletazo(ServerLevel sl) {
        aletazos++;
        Vec3 f = Vec3.directionFromRotation(0, getYRot());
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.4f;
        for (LivingEntity le : cerca(position(), R_ALETAZO)) {
            double dx = le.getX() - getX(), dz = le.getZ() - getZ(), d = Math.sqrt(dx * dx + dz * dz);
            if (d > 2 && (dx * f.x + dz * f.z) / d < -0.35) continue;
            le.hurt(damageSources().mobAttack(this), dano);
            tirados++;
            empujar(le, 2.1, dx, dz, 0.45);
        }
        for (int k = 0; k < 24; k++) {
            double ang = Math.toRadians(getYRot() + 90 - 100 + 200 * k / 23.0);
            for (double r = 3; r <= R_ALETAZO; r += 2.5)
                sl.sendParticles(ParticleTypes.CLOUD, getX() + Math.cos(ang) * r, getY() + 0.5, getZ() + Math.sin(ang) * r, 0,
                        Math.cos(ang) * 0.5, 0.05, Math.sin(ang) * 0.5, 1.0);
        }
    }

    /** El fuego del aliento en el tick t: de su boca, en cono, barriendo de su derecha a su izquierda; lo que lo tapa, lo para. */
    private void aliento(ServerLevel sl, int t) {
        float giro = giroAliento(t);
        Vec3 boca = bocaAliento(this, giro);
        float yaw = getYRot() + giro;
        Vec3 h = Vec3.directionFromRotation(0, yaw);
        double suelo = getY();
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.32f;
        double tg = Math.tan(Math.toRadians(ALIENTO_ABRE));
        for (LivingEntity le : cerca(boca, ALIENTO_LARGO + 1)) {
            double dx = le.getX() - boca.x, dz = le.getZ() - boca.z;
            double a = dx * h.x + dz * h.z;
            if (a < 0.3 || a > ALIENTO_LARGO) continue;
            double lado = Math.abs(dx * h.z - dz * h.x);
            if (lado > 1.0 + a * tg + le.getBbWidth() * 0.5) continue;
            // la altura del fuego a esa distancia (baja desde la boca y se extiende por el suelo)
            double yf = Math.max(suelo + 0.6, boca.y - a * 0.47);
            if (le.getY() > yf + 1.6 || le.getY() + le.getBbHeight() < yf - 1.2) continue;
            if (quemaHasta.getOrDefault(le.getId(), -1) >= tickCount) continue;
            Vec3 c = new Vec3(le.getX(), le.getY() + le.getBbHeight() * 0.5, le.getZ());
            if (level().clip(new ClipContext(boca, c, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() != HitResult.Type.MISS) {
                quemaHasta.put(le.getId(), tickCount + QUEMA_CADA);
                tapados++;
                continue;
            }
            quemaHasta.put(le.getId(), tickCount + QUEMA_CADA - 1);
            // (cada pocos ticks: el instante invulnerable de despues de un golpe no lo para)
            le.invulnerableTime = 0;
            if (le.hurt(fuego(), dano)) {
                quemados++;
                Calcinado.quemar(le, 4);
            }
        }
        // a media vida el suelo queda ardiendo por donde pasa
        if (segunda && (t - DragonAnims.ALIENTO_DESDE) % 6 == 0) {
            Vec3 p = boca.add(h.x * (9 + random.nextDouble() * 5), 0, h.z * (9 + random.nextDouble() * 5));
            llamas(sl, p.x, p.z, getY() + 2, 1.6f, 70);
        }
        // donde da el fuego en el suelo, el humo
        if (t % 3 == 0) {
            Vec3 p = boca.add(h.x * 12, 0, h.z * 12);
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, p.x, getY() + 0.6, p.z, 3, 1.4, 0.3, 1.4, 0.02);
        }
    }

    /** Hacia donde mira la cabeza en el tick t del aliento (grados a su derecha): de +{@code GIRO} a -{@code GIRO}. */
    public static float giroAliento(float t) {
        float u = Mth.clamp((t - DragonAnims.ALIENTO_DESDE) / (float) (DragonAnims.ALIENTO_HASTA - DragonAnims.ALIENTO_DESDE), 0f, 1f);
        return DragonAnims.ALIENTO_GIRO * 0.9f * (1f - 2f * u);
    }

    /**
     * Donde tiene la boca echando el aliento con la cabeza girada {@code giro} grados a su derecha (de la pose del modelo, con su
     * escala: unos ocho bloques delante y tres arriba).
     */
    public static Vec3 bocaAliento(LivingEntity e, float giro) {
        float k = giro / 30.7f;
        double delante = 8.4 - 0.7 * k * k, derecha = 2.82 * k;
        Vec3 f = Vec3.directionFromRotation(0, e.getYRot());
        return new Vec3(e.getX() + f.x * delante - f.z * derecha, e.getY() + 3.3, e.getZ() + f.z * delante + f.x * derecha);
    }

    /** Donde tiene la boca volando (el rasante, al escupir): delante y arriba. */
    public static Vec3 bocaVuelo(LivingEntity e) {
        Vec3 f = Vec3.directionFromRotation(0, e.getYRot());
        return new Vec3(e.getX() + f.x * 8.3, e.getY() + 3.9, e.getZ() + f.z * 8.3);
    }

    /** Lo que quema el fuego: es fuego (los frutos y talismanes de la brasa lo paran) y lo echa el. */
    private DamageSource fuego() {
        return new DamageSource(level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.IN_FIRE), this);
    }

    /** Una onda en el suelo: a los que esten (de pie: quien salta se libra, si {@code saltando}) a {@code r} de p. */
    private int onda(ServerLevel sl, Vec3 p, double r, float mult, double empuje, double arriba, boolean saltando) {
        int n = 0;
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * mult;
        for (LivingEntity le : cerca(p, r)) {
            if (saltando && !le.onGround() && le.getY() > p.y + 0.7) continue;
            if (!le.hurt(damageSources().mobAttack(this), dano)) continue;
            n++;
            empujar(le, empuje, le.getX() - p.x, le.getZ() - p.z, arriba);
        }
        return n;
    }

    /** La ceniza y el polvo que levanta un golpe en el suelo. */
    private void polvo(ServerLevel sl, Vec3 p, double r) {
        BlockState s = level().getBlockState(BlockPos.containing(p.x, getY() - 0.5, p.z));
        if (!s.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, s), p.x, getY() + 0.2, p.z, 60, r * 0.4, 0.2, r * 0.4, 0.15);
        sl.sendParticles(ParticleTypes.EXPLOSION, p.x, getY() + 0.5, p.z, 3, r * 0.3, 0.2, r * 0.3, 0.0);
        sl.sendParticles(CENIZA, p.x, getY() + 0.6, p.z, 50, r * 0.45, 0.4, r * 0.45, 0.0);
        sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, p.x, getY() + 0.4, p.z, 10, r * 0.4, 0.2, r * 0.4, 0.01);
    }

    /** La estela de la garra: el arco que barre por delante. */
    private void estela(double arco, double r, double alto) {
        if (!(level() instanceof ServerLevel sl)) return;
        int n = (int) Math.max(8, arco / 14);
        for (int j = 0; j <= n; j++) {
            double ang = Math.toRadians(getYRot() + 90 - arco / 2 + arco * j / n);
            double x = getX() + Math.cos(ang) * r, z = getZ() + Math.sin(ang) * r;
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, x, getY() + alto, z, 1, 0, 0, 0, 0);
        }
    }

    // ------------------------------------------------------------------ en el aire

    private void volar(LivingEntity t) {
        maniobraT++;
        if (forzada >= 0) {
            int m = forzada;
            forzada = -1;
            if (empezarManiobra(m, t)) return;
        }
        switch (maniobra) {
            case M_NADA, M_SUBIR -> {
                if (maniobra == M_NADA) {
                    maniobra = M_SUBIR;
                    maniobraT = 0;
                    ataquesVuelo = 0;
                    ataquesQuiere = 2 + (segunda ? 1 : 0);
                    sentido = random.nextBoolean() ? 1 : -1;
                }
                double alto = cy + ALTO_RONDA;
                rondar(R_RONDA, alto, VUELO);
                if (Math.abs(getY() - sobreTerreno(alto, getX(), getZ(), getYRot())) < 2.5 || maniobraT > 90) {
                    if (t == null || vuelveACasa) empezarManiobra(M_VOLVER, null);
                    else {
                        maniobra = M_RONDA;
                        maniobraT = 0;
                        rondaT = 30 + random.nextInt(40);
                    }
                }
            }
            case M_RONDA -> {
                rondar(R_RONDA, cy + ALTO_RONDA, VUELO);
                if (t == null) {
                    if (maniobraT > 60) empezarManiobra(M_VOLVER, null);
                    return;
                }
                if (--rondaT <= 0 && !quieta) siguiente(t);
            }
            case M_RASANTE_ALINEA -> {
                volarHacia(mx, my + 5, mz, VUELO, GIRO_VUELO);
                if (Math.hypot(mx - getX(), mz - getZ()) < 6 || maniobraT > 200) {
                    maniobra = M_RASANTE;
                    maniobraT = 0;
                }
            }
            case M_RASANTE -> {
                // de cara a su linea, bajando hasta ras de suelo; el fuego empieza con la accion (en su tick, el fuego da en el
                // suelo 16 bloques antes de su presa: lo que tiene por delante el fuego depende de lo alto que va)
                double hacia = (getX() - sx) * rdx + (getZ() - sz) * rdz;
                volarHacia(getX() + rdx * 30, my, getZ() + rdz * 30, V_RASANTE, 3f, 4.5);
                Vec3 g = sueloRasante(this);
                double llega = g == null ? 24 : Math.hypot(g.x - getX(), g.z - getZ());
                if (hacia >= -(16 + llega + DragonAnims.RASANTE_DESDE * V_RASANTE) || maniobraT > 160) {
                    empezar(DragonAnims.RASANTE);
                    // al acabar de echar fuego, vuelve a subir
                    ataquesVuelo++;
                    maniobra = M_SUBIR;
                    maniobraT = 0;
                }
            }
            case M_BOLAS -> bolas(t);
            case M_PICADO_SUBE -> {
                volarHacia(mx, my, mz, VUELO, GIRO_VUELO);
                if ((Math.hypot(mx - getX(), mz - getZ()) < 4 && Math.abs(getY() - sobreTerreno(my, getX(), getZ(), getYRot())) < 3) || maniobraT > 160) {
                    maniobra = M_PICADO_MARCA;
                    maniobraT = 0;
                    LivingEntity p = t != null ? t : level().getNearestPlayer(this, 64);
                    double px = p != null ? p.getX() : cx, pz = p != null ? p.getZ() : cz;
                    double s = pisable(px, pz, p != null ? p.getY() + 1 : cy + 2);
                    sx = px;
                    sz = pz;
                    sy = Double.isNaN(s) ? (p != null ? p.getY() : cy) : s;
                    playSound(DarkCraftMod.SFX_DRAGON_RUGIDO.get(), 4.0f, 1.25f);
                }
            }
            case M_PICADO_MARCA -> {
                encarar(sx, sz, 6f);
                quedarseEnElAire(Double.NaN);
                marcaPicado();
                if (maniobraT >= 26) {
                    maniobra = M_PICADO;
                    maniobraT = 0;
                    velocidad = 0.4;
                    trasPicado = true;
                    picados++;
                    empezar(DragonAnims.PICADO);
                }
            }
            case M_PICADO -> picar();
            case M_ASCUAS_VA -> {
                volarHacia(cx, cy + ALTO_ASCUAS, cz, VUELO, GIRO_VUELO);
                if (Math.hypot(cx - getX(), cz - getZ()) < 4 || maniobraT > 200) {
                    maniobra = M_ASCUAS;
                    maniobraT = 0;
                    empezar(DragonAnims.ASCUAS);
                }
            }
            case M_ASCUAS -> {
                quedarseEnElAire(Double.NaN);
                if (t != null) encarar(t.getX(), t.getZ(), 2f);
                if (lluvia <= 0 && ascuas.isEmpty() && maniobraT > 20) {
                    ataquesVuelo++;
                    empezarManiobra(t == null ? M_VOLVER : M_BAJAR, t);
                }
            }
            case M_BAJAR, M_VOLVER -> bajar();
            case M_CAIDA -> caida();
            default -> maniobra = M_RONDA;
        }
    }

    /** Lo siguiente en el aire: las ascuas (si tocan), otro ataque o bajar a tierra. */
    private void siguiente(LivingEntity t) {
        if (ascuasPendiente) {
            ascuasPendiente = false;
            empezarManiobra(M_ASCUAS_VA, t);
            return;
        }
        if (ataquesVuelo >= ataquesQuiere) {
            empezarManiobra(random.nextFloat() < 0.5f ? M_PICADO_SUBE : M_BAJAR, t);
            return;
        }
        float r = random.nextFloat();
        int m = r < 0.45f ? M_RASANTE_ALINEA : r < 0.8f ? M_BOLAS : M_PICADO_SUBE;
        if (!empezarManiobra(m, t)) empezarManiobra(M_BOLAS, t);
    }

    /** Empieza una maniobra en el aire (con lo que necesite: la linea del rasante, el sitio del picado...). */
    private boolean empezarManiobra(int m, LivingEntity t) {
        centro();
        LivingEntity p = t != null ? t : getTarget();
        switch (m) {
            case M_RASANTE_ALINEA -> {
                if (p == null || !planRasante(p)) return false;
            }
            case M_BOLAS -> {
                if (p == null) return false;
                // a quedarse en el aire a unos 16 de su presa, del lado en que esta
                double dx = getX() - p.getX(), dz = getZ() - p.getZ(), l = Math.max(0.1, Math.hypot(dx, dz));
                mx = p.getX() + dx / l * 16;
                mz = p.getZ() + dz / l * 16;
                my = cy + ALTO_BOLAS;
                bolasQuedan = 3;
                bolasEspera = 0;
            }
            case M_PICADO_SUBE -> {
                // (no se lanza sobre quien esta bajo techo: en la atalaya, bajo un saliente)
                if (p == null || terreno(p.getX(), p.getZ()) > p.getY() + 2.5) return false;
                double dx = getX() - p.getX(), dz = getZ() - p.getZ(), l = Math.max(0.1, Math.hypot(dx, dz));
                mx = p.getX() + dx / l * 18;
                mz = p.getZ() + dz / l * 18;
                my = cy + ALTO_PICADO;
            }
            case M_ASCUAS_VA -> {
            }
            case M_BAJAR -> {
                // a unos 8 de su presa (del lado de el), dentro de la repisa y lejos de penascos y troncos
                double tx = p != null ? p.getX() : cx, tz = p != null ? p.getZ() : cz;
                double dx = getX() - tx, dz = getZ() - tz, l = Math.max(0.1, Math.hypot(dx, dz));
                double lx = tx + dx / l * 8, lz = tz + dz / l * 8;
                double r = alCentro(lx, lz), max = Math.max(4, radio - 7);
                if (r > max) {
                    lx = cx + (lx - cx) / r * max;
                    lz = cz + (lz - cz) / r * max;
                }
                if (Nido.distancia(lx, lz) < Nido.R_MEZCLA + 10) {
                    double[] q = Nido.apartar(lx, lz, 4.0);
                    lx = q[0];
                    lz = q[1];
                }
                sx = lx;
                sz = lz;
                double s = pisable(lx, lz, cy + 3);
                sy = Double.isNaN(s) ? cy : s;
            }
            case M_VOLVER -> {
                BlockPos h = hogar();
                sx = h != null ? h.getX() + 0.5 : cx;
                sz = h != null ? h.getZ() + 0.5 : cz;
                double s = pisable(sx, sz, (h != null ? h.getY() : cy) + 2);
                sy = Double.isNaN(s) ? (h != null ? h.getY() : cy) : s;
                vuelveACasa = true;
            }
            default -> {
                return false;
            }
        }
        maniobra = m;
        maniobraT = 0;
        trasPicado = false;
        return true;
    }

    /**
     * La linea del rasante: pasa por donde esta su presa, viniendo de donde esta el (o de un lado) pero nunca desde la montana (el
     * punto de entrada y el de bajar tienen que caer sobre lo bajo). Apunta el punto de entrada (m), la direccion (rd) y la presa (s).
     */
    private boolean planRasante(LivingEntity p) {
        double tx = p.getX(), tz = p.getZ();
        double gy = terreno(tx, tz);
        double base = Math.atan2(tz - getZ(), tx - getX());
        int[] orden = {0, 1, -1, 2, -2, 3, -3, 4};
        for (int k : orden) {
            double a = base + k * Math.PI / 4;
            double dx = Math.cos(a), dz = Math.sin(a);
            double p0x = tx - dx * 62, p0z = tz - dz * 62, b0x = tx - dx * 40, b0z = tz - dz * 40;
            if (terreno(p0x, p0z) > gy + 6 || terreno(b0x, b0z) > gy + 3 || terreno(tx + dx * 10, tz + dz * 10) > gy + 4) continue;
            mx = p0x;
            mz = p0z;
            my = gy + ALTO_RASANTE;
            rdx = dx;
            rdz = dz;
            sx = tx;
            sz = tz;
            sy = gy;
            return true;
        }
        return false;
    }

    /** Da vueltas alrededor del centro de la repisa (va hacia un punto del circulo un poco por delante de donde esta). */
    private void rondar(double r, double alto, double v) {
        double ang = Math.atan2(getZ() - cz, getX() - cx) + 0.42 * sentido;
        volarHacia(cx + Math.cos(ang) * r, alto, cz + Math.sin(ang) * r, v, GIRO_VUELO);
    }

    /** Vuela hacia (x, y, z) a {@code v}, girando como mucho {@code giro} grados por tick; nunca por debajo de 6 sobre el terreno. */
    private void volarHacia(double x, double y, double z, double v, float giro) {
        volarHacia(x, y, z, v, giro, 6);
    }

    /** Lo mismo, a {@code margen} del terreno como poco. */
    private void volarHacia(double x, double y, double z, double v, float giro, double margen) {
        double dx = x - getX(), dz = z - getZ(), dh = Math.sqrt(dx * dx + dz * dz);
        float yaw = getYRot();
        if (dh > 0.3) yaw = Mth.approachDegrees(yaw, (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f, giro);
        setYRot(yaw);
        yBodyRot = yHeadRot = yaw;
        velocidad += Mth.clamp(v - velocidad, -0.05, 0.035);
        double paso = Math.min(velocidad, dh);
        Vec3 f = Vec3.directionFromRotation(0, yaw);
        double yq = sobreTerreno(y, getX(), getZ(), yaw, margen);
        double vq = Mth.clamp((yq - getY()) * 0.1, -0.45, 0.4);
        vy += Mth.clamp(vq - vy, -0.04, 0.04);
        setPos(getX() + f.x * paso, getY() + vy, getZ() + f.z * paso);
    }

    /** Se queda en el aire batiendo las alas donde esta (a la altura {@code y}, si no es NaN): frena y se mece. */
    private void quedarseEnElAire(double y) {
        velocidad *= 0.85;
        Vec3 f = Vec3.directionFromRotation(0, getYRot());
        double yq = Double.isNaN(y) ? getY() : sobreTerreno(y, getX(), getZ(), getYRot());
        double vq = Mth.clamp((yq - getY()) * 0.08, -0.25, 0.25) + Math.sin(tickCount * 0.22) * 0.03;
        vy += Mth.clamp(vq - vy, -0.04, 0.04);
        setPos(getX() + f.x * velocidad, getY() + vy, getZ() + f.z * velocidad);
    }

    /** Las bolas de fuego: va a quedarse en el aire a un lado de su presa y le escupe tres. */
    private void bolas(LivingEntity t) {
        if (t == null) {
            empezarManiobra(M_VOLVER, null);
            return;
        }
        double dh = Math.hypot(mx - getX(), mz - getZ());
        if (dh > 3 && maniobraT < 160) {
            volarHacia(mx, my, mz, dh > 14 ? VUELO : VUELO_LENTO, GIRO_VUELO);
            return;
        }
        encarar(t.getX(), t.getZ(), 5f);
        quedarseEnElAire(my);
        if (bolasQuedan > 0 && bolasEspera <= 0 && angulo(t.getX(), t.getZ()) < 25) {
            bolasQuedan--;
            bolasEspera = 30;
            empezar(DragonAnims.ESCUPE);
        } else if (bolasQuedan <= 0 && bolasEspera <= 6) {
            ataquesVuelo++;
            maniobra = M_RONDA;
            maniobraT = 0;
            rondaT = 30 + random.nextInt(30);
        }
    }

    /** Escupe una bola de fuego a su presa (a donde va a estar), en arco. */
    private void escupir(ServerLevel sl) {
        LivingEntity t = getTarget();
        if (t == null) return;
        Vec3 boca = bocaVuelo(this);
        double tx = t.getX(), ty = t.getY() + 0.2, tz = t.getZ();
        double h = Math.hypot(tx - boca.x, tz - boca.z);
        double vuelo = Mth.clamp(10 + h * 0.9, 14, 40);
        Vec3 mov = t.getDeltaMovement();
        tx += mov.x * vuelo * 0.6;
        tz += mov.z * vuelo * 0.6;
        ProyectilDragon b = new ProyectilDragon(sl, this, (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.9f);
        b.setPos(boca.x, boca.y, boca.z);
        b.setDeltaMovement(ProyectilDragon.tiro(boca.x, boca.y, boca.z, tx, ty, tz));
        sl.addFreshEntity(b);
        bolas++;
        sl.sendParticles(ParticleTypes.FLAME, boca.x, boca.y, boca.z, 20, 0.4, 0.4, 0.4, 0.05);
    }

    /** El picado: la marca en el suelo donde va a caer (un corro de fuego que se cierra). */
    private void marcaPicado() {
        if (!(level() instanceof ServerLevel sl)) return;
        double falta = 1.0 - maniobraT / 26.0;
        for (int k = 0; k < 10; k++) {
            double a = tickCount * 0.3 + k * Math.PI / 5;
            double r = R_PICADO * (0.45 + 0.55 * Math.max(0, falta));
            sl.sendParticles(ParticleTypes.FLAME, sx + Math.cos(a) * r, sy + 0.1, sz + Math.sin(a) * r, 1, 0, 0.02, 0, 0);
        }
        for (int k = 0; k < 18; k++) {
            double a = k * Math.PI / 9;
            sl.sendParticles(BRASA, sx + Math.cos(a) * R_PICADO, sy + 0.15, sz + Math.sin(a) * R_PICADO, 1, 0, 0, 0, 0);
        }
        if (maniobraT % 4 == 0) sl.sendParticles(ParticleTypes.LARGE_SMOKE, sx, sy + 0.3, sz, 2, 0.6, 0.1, 0.6, 0.01);
    }

    /** Se lanza en picado al sitio marcado; poco antes de llegar, el aterrizaje (que da el golpe al tocar el suelo). */
    private void picar() {
        if (accion() == DragonAnims.ATERRIZA) return;
        velocidad = Math.min(V_PICADO, velocidad + 0.07);
        double dx = sx - getX(), dy = sy - getY(), dz = sz - getZ(), d = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (d <= velocidad * (DragonAnims.GOLPES[DragonAnims.ATERRIZA][0] + 0.5) || maniobraT > 120) {
            empezar(DragonAnims.ATERRIZA);
            return;
        }
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f;
        setYRot(Mth.approachDegrees(getYRot(), yaw, 8f));
        yBodyRot = yHeadRot = getYRot();
        vy = dy / d * velocidad;
        setPos(getX() + dx / d * velocidad, getY() + vy, getZ() + dz / d * velocidad);
    }

    /** Baja a tierra en su sitio (el de aterrizar o, sin presa, su tesoro): llega frenando y, cerca del suelo, se posa. */
    private void bajar() {
        double dx = sx - getX(), dz = sz - getZ(), dh = Math.sqrt(dx * dx + dz * dz);
        double sobre = getY() - sy;
        if (dh < 2.5 && sobre < 4.5 || maniobraT > 260) {
            empezar(DragonAnims.ATERRIZA);
            return;
        }
        double v = Mth.clamp(dh * 0.06, 0.18, VUELO);
        // de lejos, alto; encima del sitio, baja derecho (sin el margen sobre el terreno: va a posarse)
        double alto = dh < 3 ? sy + 2.5 : sy + Math.max(3.5, Math.min(ALTO_RONDA, dh * 0.4));
        volarHacia(sx, alto, sz, v, GIRO_VUELO + 1f, dh < 12 ? 0 : 6);
    }

    /** Cae del cielo (le han pegado mucho en el aire): aturdido al tocar el suelo. */
    private void caer() {
        caidas++;
        caidaEspera = 600;
        maniobra = M_CAIDA;
        maniobraT = 0;
        playSound(DarkCraftMod.SFX_DRAGON_DOLOR.get(), 4.0f, 0.75f);
        empezar(DragonAnims.PICADO);
    }

    private void caida() {
        velocidad *= 0.92;
        vy = Math.max(-1.0, vy - 0.07);
        Vec3 f = Vec3.directionFromRotation(0, getYRot());
        double nx = getX() + f.x * velocidad, nz = getZ() + f.z * velocidad;
        double s = pisable(nx, nz, getY());
        if (Double.isNaN(s)) s = terreno(nx, nz);
        double ny = getY() + vy;
        if (ny <= s + 0.05 || maniobraT > 140) {
            setPos(nx, maniobraT > 140 ? ny : s, nz);
            vuela(false);
            maniobra = M_NADA;
            vy = velocidad = 0;
            tierraT = 0;
            aturdidos++;
            aturdidoEspera = ATURDIDO_ESPERA;
            if (level() instanceof ServerLevel sl) {
                ondas += onda(sl, position(), 4.5, 0.5f, 0.8, 0.3, true);
                polvo(sl, position(), 7);
            }
            empezar(DragonAnims.ATURDIDO);
            pausa = DragonAnims.TICKS[DragonAnims.ATURDIDO] + 8;
            return;
        }
        setPos(nx, ny, nz);
    }

    /** Toca el suelo (al acabar de bajar o el picado): la onda (mayor la del picado) y ya en tierra. */
    private void aterrizar(ServerLevel sl) {
        aterrizajes++;
        setPos(sx, sy, sz);
        vuela(false);
        vy = velocidad = 0;
        tierraT = 0;
        altoT = lejosT = 0;
        boolean picado = trasPicado;
        trasPicado = false;
        maniobra = M_NADA;
        playSound(DarkCraftMod.SFX_DRAGON_PISOTON.get(), picado ? 5.0f : 3.5f, 0.8f);
        double r = picado ? R_PICADO : R_ATERRIZA;
        int n = onda(sl, position(), r, picado ? 1.6f : 0.9f, picado ? 1.8 : 1.2, picado ? 0.5 : 0.35, true);
        ondas += n;
        if (picado) golpesPicado += n;
        polvo(sl, position(), r);
        if (vuelveACasa && getTarget() == null) {
            // de vuelta en su tesoro: mira a donde miraba (y se dormira)
            vuelveACasa = false;
            BlockPos h = hogar();
            if (h != null && Math.hypot(getX() - h.getX() - 0.5, getZ() - h.getZ() - 0.5) < 3.5) {
                DarkCraftMod.LOGGER.info("[DarkCraft] Vorthak vuelve a su tesoro");
            }
        }
    }

    // ------------------------------------------------------------------ el rasante y la lluvia de ascuas

    /** El rasante en el tick t: el fuego da en el suelo por delante de el (un disco que avanza); queda ardiendo. */
    private void rasante(ServerLevel sl, int t) {
        Vec3 g = sueloRasante(this);
        if (g == null) return;
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5f;
        for (LivingEntity le : cerca(g, R_RASANTE)) {
            if (Math.abs(le.getY() - g.y) > 3) continue;
            if (quemaHasta.getOrDefault(le.getId(), -1) >= tickCount) continue;
            quemaHasta.put(le.getId(), tickCount + 6);
            le.invulnerableTime = 0;
            if (le.hurt(fuego(), dano)) {
                quemadosRasante++;
                Calcinado.quemar(le, 5);
            }
        }
        if ((t - DragonAnims.RASANTE_DESDE) % 3 == 0) llamas(sl, g.x, g.z, g.y + 1, 1.8f, 80);
        sl.sendParticles(ParticleTypes.FLAME, g.x, g.y + 0.4, g.z, 10, 1.2, 0.3, 1.2, 0.04);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, g.x, g.y + 0.8, g.z, 3, 1.0, 0.4, 1.0, 0.02);
    }

    /**
     * Donde da en el suelo el fuego del rasante: la boca echa hacia abajo y adelante (30 grados), asi que delante de el, tanto mas
     * lejos cuanto mas alto vaya (null si no hay suelo).
     */
    public static Vec3 sueloRasante(LivingEntity e) {
        Vec3 boca = bocaVuelo(e);
        Vec3 f = Vec3.directionFromRotation(0, e.getYRot());
        Level l = e.level();
        // de la altura de la boca sobre el suelo de debajo, hasta donde llega (dos pasadas: el suelo de alli manda)
        double ground = alto(l, boca.x, boca.z, boca.y);
        double lejos = Math.max(2, (boca.y - ground) / Math.tan(Math.toRadians(30)));
        double gx = boca.x + f.x * lejos, gz = boca.z + f.z * lejos;
        double g2 = alto(l, gx, gz, boca.y);
        lejos = Math.max(2, (boca.y - g2) / Math.tan(Math.toRadians(30)));
        gx = boca.x + f.x * lejos;
        gz = boca.z + f.z * lejos;
        double g3 = alto(l, gx, gz, boca.y);
        if (g3 < l.getMinBuildHeight() + 1) return null;
        return new Vec3(gx, g3, gz);
    }

    /** Lo alto del terreno bajo (x, z) por debajo de {@code y} (el mapa de alturas, que cuesta poco). */
    private static double alto(Level l, double x, double z, double y) {
        int bx = Mth.floor(x), bz = Mth.floor(z);
        if (!l.hasChunkAt(new BlockPos(bx, Mth.floor(y), bz))) return y - 8;
        return Math.min(y - 1, l.getHeight(Heightmap.Types.MOTION_BLOCKING, bx, bz));
    }

    /** Empieza la lluvia de ascuas (el rugido al cielo). */
    private void empezarLluvia(ServerLevel sl) {
        lluvias++;
        lluvia = LLUVIA;
        sl.playSound(null, cx, cy + 6, cz, DarkCraftMod.SFX_DRAGON_ASCUAS.get(), SoundSource.HOSTILE, 4.0f, 0.9f);
    }

    /** La lluvia: cada pocos ticks una ascua (unas donde estan los jugadores, otras por toda la repisa); su marca avisa y cae. */
    private void lluvia(ServerLevel sl) {
        if (lluvia > 0) {
            lluvia--;
            if (lluvia % 40 == 39) sl.playSound(null, cx, cy + 6, cz, DarkCraftMod.SFX_DRAGON_ASCUAS.get(), SoundSource.HOSTILE, 3.0f, 0.95f);
            if (lluvia % ASCUA_CADA == 0) {
                double x, z;
                List<ServerPlayer> js = new ArrayList<>();
                for (ServerPlayer p : sl.players()) if (ve(p)) js.add(p);
                if (!js.isEmpty() && random.nextFloat() < 0.4f) {
                    ServerPlayer p = js.get(random.nextInt(js.size()));
                    x = p.getX() + (random.nextDouble() - 0.5) * 4;
                    z = p.getZ() + (random.nextDouble() - 0.5) * 4;
                } else {
                    double a = random.nextDouble() * Math.PI * 2, r = Math.sqrt(random.nextDouble()) * (radio - 1);
                    x = cx + Math.cos(a) * r;
                    z = cz + Math.sin(a) * r;
                }
                double y = terreno(x, z);
                ascuas.add(new double[]{x, y, z, tickCount + ASCUA_AVISO});
            }
        }
        if (ascuas.isEmpty()) return;
        Iterator<double[]> it = ascuas.iterator();
        while (it.hasNext()) {
            double[] q = it.next();
            int falta = (int) (q[3] - tickCount);
            if (falta > 0) {
                // la marca: un corro de brasas que se cierra; al final, el ascua que cae
                if (tickCount % 2 == 0) {
                    double r = R_ASCUA * (0.4 + 0.6 * falta / ASCUA_AVISO);
                    for (int k = 0; k < 4; k++) {
                        double a = tickCount * 0.4 + k * Math.PI / 2;
                        sl.sendParticles(ParticleTypes.SMALL_FLAME, q[0] + Math.cos(a) * r, q[1] + 0.1, q[2] + Math.sin(a) * r, 1, 0, 0, 0, 0);
                    }
                }
                if (falta <= 8) sl.sendParticles(ParticleTypes.FLAME, q[0], q[1] + falta * 2.2, q[2], 4, 0.15, 0.4, 0.15, 0.0);
                if (falta == 8) sl.sendParticles(ParticleTypes.LAVA, q[0], q[1] + 16, q[2], 2, 0.1, 0.1, 0.1, 0.0);
                continue;
            }
            it.remove();
            ascuasCaidas++;
            sl.sendParticles(ParticleTypes.LAVA, q[0], q[1] + 0.2, q[2], 6, 0.5, 0.2, 0.5, 0.0);
            sl.sendParticles(ParticleTypes.FLAME, q[0], q[1] + 0.3, q[2], 24, 0.8, 0.3, 0.8, 0.06);
            sl.sendParticles(ParticleTypes.EXPLOSION, q[0], q[1] + 0.5, q[2], 1, 0, 0, 0, 0);
            sl.playSound(null, q[0], q[1], q[2], SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.7f, 1.5f + random.nextFloat() * 0.2f);
            float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6f;
            for (LivingEntity le : cerca(new Vec3(q[0], q[1], q[2]), R_ASCUA)) {
                if (Math.abs(le.getY() - q[1]) > 3) continue;
                if (le.hurt(fuego(), dano)) {
                    quemadosAscuas++;
                    Calcinado.quemar(le, 3);
                }
            }
            if (random.nextFloat() < 0.6f) llamas(sl, q[0], q[2], q[1] + 1, 1.3f, 50);
        }
    }

    /** Unas llamas en el suelo (las de los calcinados: queman a quien las pisa y se van apagando). */
    private void llamas(ServerLevel sl, double x, double z, double desde, float r, int ticks) {
        double y = pisable(x, z, desde);
        if (Double.isNaN(y)) return;
        BlockPos b = BlockPos.containing(x, y, z);
        if (!sl.getFluidState(b).isEmpty()) return;
        sl.addFreshEntity(new Cantaro.Llamas(sl, x, y, z, this, r, ticks, false));
        fuegos++;
    }

    // ------------------------------------------------------------------ empujar y los de alrededor

    /** Empuja a alguien hacia (dx, dz) con {@code fuerza} y le levanta {@code arriba}. */
    private void empujar(LivingEntity le, double fuerza, double dx, double dz, double arriba) {
        double l = Math.sqrt(dx * dx + dz * dz);
        if (l < 1e-6) {
            Vec3 f = Vec3.directionFromRotation(0, getYRot());
            dx = f.x;
            dz = f.z;
            l = 1;
        }
        le.knockback(fuerza, -dx / l, -dz / l);
        le.setDeltaMovement(le.getDeltaMovement().add(0, arriba, 0));
        le.hurtMarked = true;
    }

    /** Los que estan a {@code r} de un punto (en horizontal, y no muy por encima ni por debajo) y se les puede pegar. */
    private List<LivingEntity> cerca(Vec3 p, double r) {
        return level().getEntitiesOfClass(LivingEntity.class, new AABB(p.x - r - 1, p.y - 4, p.z - r - 1, p.x + r + 1, p.y + 6, p.z + r + 1),
                e -> e != this && e.isAlive() && objetivoValido(e) && e.distanceToSqr(p.x, e.getY(), p.z) <= (r + e.getBbWidth() * 0.5) * (r + e.getBbWidth() * 0.5));
    }

    // ------------------------------------------------------------------ recibir y caer

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        // caido de lado: un 30 % mas
        if (aturdido()) cuanto *= 1.3f;
        boolean ok = super.hurt(s, cuanto);
        if (ok && !level().isClientSide && isAlive() && s.getEntity() instanceof Player) {
            if (recibidoT <= 0) recibidoT = VENTANA;
            recibido += cuanto;
            int a = accion();
            boolean libre = a != DragonAnims.DESPIERTA && a != DragonAnims.RUGIDO && a != DragonAnims.ATERRIZA && a != DragonAnims.DESPEGA
                    && a != DragonAnims.ASCUAS && !aturdido() && postura() == DE_PIE;
            if (vuela()) {
                if (recibido >= getMaxHealth() * AGUANTE_VUELO && caidaEspera <= 0 && libre && maniobra != M_CAIDA) {
                    recibido = 0;
                    recibidoT = 0;
                    caer();
                }
            } else if (recibido >= getMaxHealth() * AGUANTE_TIERRA && aturdidoEspera <= 0 && libre) {
                recibido = 0;
                recibidoT = 0;
                aturdidoEspera = ATURDIDO_ESPERA;
                aturdidos++;
                aturdir();
            }
        }
        return ok;
    }

    @Override
    public void die(DamageSource s) {
        boolean ya = !isAlive() && deathTime > 0;
        super.die(s);
        if (level().isClientSide || ya) return;
        ascuas.clear();
        lluvia = 0;
        hablaHasta = 0;
        habla(5);
        if (vuela()) vy = Math.min(vy, 0.0);
    }

    /** Al caer: si vuela, se desploma del cielo; en el suelo se tumba, dice su ultima frase y las brasas de sus grietas se apagan. */
    @Override
    protected void tickDeath() {
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        ServerLevel sl = (ServerLevel) level();
        if (vuela()) {
            // cae del cielo (sin dejar de hacer su animacion de morir)
            velocidad *= 0.93;
            vy = Math.max(-1.1, vy - 0.08);
            Vec3 f = Vec3.directionFromRotation(0, getYRot());
            double nx = getX() + f.x * velocidad, nz = getZ() + f.z * velocidad, ny = getY() + vy;
            double s = pisable(nx, nz, getY());
            if (Double.isNaN(s)) s = terreno(nx, nz);
            if (ny <= s || deathTime > 120) {
                setPos(nx, deathTime > 120 ? ny : s, nz);
                vuela(false);
                playSound(DarkCraftMod.SFX_DRAGON_PISOTON.get(), 5.0f, 0.6f);
                polvo(sl, position(), 8);
            } else setPos(nx, ny, nz);
        } else pegarAlSuelo();
        int fin = Math.max(DragonAnims.TICKS[DragonAnims.MORIR] + 40, Voces.ticks(VOZ_BASE + 5) + 110);
        if (deathTime == 48 && !vuela()) {
            playSound(DarkCraftMod.SFX_DRAGON_PISOTON.get(), 4.5f, 0.65f);
            polvo(sl, position(), 7);
        }
        if (deathTime == Voces.ticks(VOZ_BASE + 5) + 8) playSound(DarkCraftMod.SFX_DRAGON_MUERTE.get(), 4.0f, 1.0f);
        if (deathTime > 60 && deathTime % 4 == 0) sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 2.0, getZ(), 2, 2.5, 0.8, 2.5, 0.01);
        if (deathTime > fin - 40 && deathTime % 2 == 0) sl.sendParticles(CENIZA, getX(), getY() + 1.5, getZ(), 10, 3.0, 1.0, 3.0, 0.0);
        if (deathTime == fin - 2) {
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.5, getZ(), 60, 3.0, 1.2, 3.0, 0.03);
            sl.sendParticles(CENIZA, getX(), getY() + 1.5, getZ(), 120, 3.5, 1.5, 3.5, 0.0);
            sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 2.0, getZ(), 30, 2.0, 1.0, 2.0, 0.04);
        }
        if (deathTime >= fin) {
            level().broadcastEntityEvent(this, (byte) 60);
            remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_3.get(), 3));
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_2.get(), 3));
        spawnAtLocation(new ItemStack(DarkCraftMod.CENIZAS.get(com.darkcraft.combate.Habilidad.LLAMAS).get()));
    }

    /** Para las pruebas y el comando: como esta. */
    public String estado() {
        return String.format(Locale.ROOT, "vida %.0f/%.0f, %s, %s (%s) en %.1f,%.1f,%.1f; mordiscos %d, zarpazos %d, coletazos %d (dan %d),"
                        + " aletazos %d (tira %d), pisotones %d, ondas %d, alientos %d (quema %d, tapados %d), despegues %d, aterrizajes %d,"
                        + " rasantes %d (quema %d), bolas %d, picados %d (dan %d), lluvias %d (ascuas %d, quema %d), aturdido %d, caidas %d,"
                        + " rugidos %d, fuegos %d",
                getHealth(), getMaxHealth(), enPelea() ? "peleando" : postura() != DE_PIE ? "dormido" : "despierto", vuela() ? "volando" : "en tierra",
                maniobraActual(), getX(), getY(), getZ(), mordiscos, zarpazos, coletazos, golpesCola, aletazos, tirados, pisotones, ondas, alientos,
                quemados, tapados, despegues, aterrizajes, rasantes, quemadosRasante, bolas, picados, golpesPicado, lluvias, ascuasCaidas,
                quemadosAscuas, aturdidos, caidas, rugidos, fuegos);
    }
}
