package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.EfectosRaiz;
import com.darkcraft.morvain.Cerco;
import com.darkcraft.net.Net;
import com.darkcraft.rey.Voces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Aldric, el Primer Marcado (parte 7v): el jefe del Crater de Sangre. El primero al que la Sombra saco de la muerte y le puso
 * su marca: "rompe mis cadenas y descansaras". Golpeo la cadena del sur mil inviernos; un dia subio por ella hasta ver la
 * ciudadela de cerca, vio lo que espera alli arriba y bajo (libro 30, su diario). Desde entonces reza de rodillas en lo alto
 * del ancla, donde empieza la cadena, y no deja subir a nadie que lleve la marca. Un caballero viejo y enorme con la coraza
 * quemada (por el agujero se le ve la marca encendida), la capa roja rota y la cadena rota enrollada al brazo derecho, con el
 * eslabon partido en la punta: su arma.
 * <ul>
 * <li>espera <b>de rodillas</b> en lo alto del ancla, de cara a la puerta; quien entra en el cerco y se le acerca (20 bloques,
 * a la vista) le despierta: se levanta mientras dice su frase y al acabarla <b>ruge</b>: los marcados del cerco se arrodillan
 * (<b>rendidos</b>: no pelean mientras dure, salvo que se les pegue) y la <b>cadena cierra la puerta</b> del cerco
 * ({@link Cerco#tick}); luego <b>salta</b> sobre su presa desde lo alto;</li>
 * <li>el <b>cadenazo</b> (la cadena de lado a lado por delante: ancho), el <b>latigazo</b> (recto, de lejos: a quien alcanza
 * lo arrastra hacia el), la <b>estrella</b> (la alza al cielo y la estrella delante: la onda tumba a quien este de pie), el
 * <b>torbellino</b> (dos vueltas con la cadena tendida: pega dos veces todo alrededor) y el <b>salto</b> (cae sobre su presa:
 * la onda al caer);</li>
 * <li>a <b>media vida</b> le arde la <b>marca</b> (su frase 2): desde entonces su <b>reflejo</b> repite cada golpe de cadena un
 * instante despues desde donde lo dio (como el de los marcados, mas grande) y <b>encadena</b>: golpea el suelo con el puno y
 * bajo los pies de cada uno sale una marca; al momento brotan cadenas del suelo que pegan y <b>sujetan</b> (casi no se
 * puede andar ni saltar un rato).</li>
 * </ul>
 * Habla (ElevenLabs, en ingles con subtitulos; {@link Voces#BASE_ALDRIC}): 1 al empezar (no la repite en 4 min), 2 al arderle la
 * marca, 3 si cae su presa, 4 la primera vez que encadena y 5 al caer. Al caer se queda de rodillas, mira a la ciudadela, dice
 * su ultima frase y se deshace en sombra; lo demas (el cartel con sus ecos, la hazana {@code aldric} y el bestiario) lo reparte
 * {@code valdrenn/Guardianes}; los marcados que seguian de rodillas en el cerco descansan y la cadena del sur queda libre
 * ({@link Cerco#enPaz}).
 */
public class Aldric extends Guardian {
    public static final int ECOS = 20000;
    public static final int VOZ_BASE = Voces.BASE_ALDRIC;
    /** Si es el reflejo de otro (una sombra que repite su golpe y se deshace) y si le arde la marca (media vida). */
    private static final EntityDataAccessor<Boolean> REFLEJO = SynchedEntityData.defineId(Aldric.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ARDE = SynchedEntityData.defineId(Aldric.class, EntityDataSerializers.BOOLEAN);
    /** Lo que tarda el reflejo en repetir el golpe (ticks desde que empieza el de verdad). */
    public static final int RETRASO = 12;
    /** La onda de la estrella (delante, bloques) y su radio; la del salto al caer; la del estallido de la marca. */
    public static final double ESTRELLA_DELANTE = 3.9, R_ESTRELLA = 3.1, R_SALTO = 4.2, R_MARCA = 4.4;
    /** Lo que tarda en brotar la cadena desde su marca (ticks), su radio y lo que sujeta (ticks). */
    public static final int CADENA_T = 22, SUJETA = 50;
    public static final double R_CADENA = 1.5;
    /** Lo que dura el vuelo del salto (ticks: del despegue a la caida, como su animacion). */
    static final int DESPEGA = 12, CAE = 28;
    /** Hasta donde mira de rodillas desde lo alto del ancla (bloques). */
    public static final double VE = 20;

    /** La ultima vez que dijo su frase de entrada (tiempo del mundo): no la repite en cada intento. */
    private static long ultimaEntrada = Long.MIN_VALUE / 2;

    private record Marca(double x, double y, double z, int cuando) {}

    private final List<Marca> marcas = new ArrayList<>();
    private int saltoEspera, latigoEspera = 40, estrellaEspera = 40, torbellinoEspera = 100, encadenarEspera = 60, hablaHasta, esperaRugido,
            espera = 60, arrastre;
    private boolean cuatro, rindio, enVuelo;
    private double vueloX, vueloZ;
    private LivingEntity ultimaPresa;
    /** El reflejo que esta por salir: la accion, desde donde y hacia donde, y en que tick. */
    private int reflejoAccion, reflejoCuando;
    private double reflejoX, reflejoY, reflejoZ;
    private float reflejoGiro;
    /** El de verdad (en un reflejo): a quien pega su reflejo. */
    private LivingEntity presaDelReflejo;
    /** Para las pruebas. */
    private int cadenazos, latigazos, arrastrados, estrellas, ondas, torbellinos, saltos, caidas, ondasSalto, encadenados, marcadas, brotes,
            sujetos, reflejos, rendidos, estallidos;
    /** Los golpes que han dado los reflejos de todos (pruebas). */
    public static int REFLEJOS_DAN;
    /** Para las pruebas: no elige golpes por su cuenta (lo que se le manda hacer, si; y lo ya lanzado sigue). */
    public boolean quieta;

    public Aldric(EntityType<? extends Aldric> t, Level l) {
        super(t, l, "aldric", BossEvent.BossBarColor.RED);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1300.0).add(Attributes.ATTACK_DAMAGE, 13.0).add(Attributes.ARMOR, 8.0)
                .add(Attributes.ARMOR_TOUGHNESS, 3.0).add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(REFLEJO, false);
        entityData.define(ARDE, false);
    }

    /** Si es el reflejo de otro (una sombra que repite su golpe y se deshace). */
    public boolean reflejo() {
        return entityData.get(REFLEJO);
    }

    /** Si le arde la marca (media vida: sus golpes los repite su reflejo). */
    public boolean arde() {
        return entityData.get(ARDE);
    }

    @Override
    public String tipo() {
        return "aldric";
    }

    @Override
    public int musica() {
        return 41;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return "aldric";
    }

    @Override
    protected int[] ticks() {
        return AldricAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return AldricAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return AldricAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return AldricAnims.MORIR;
    }

    @Override
    protected int accionRugido() {
        return AldricAnims.RUGIDO;
    }

    @Override
    protected int accionLevantarse() {
        return AldricAnims.LEVANTARSE;
    }

    // ------------------------------------------------------------------ para las pruebas

    public int cadenazos() {
        return cadenazos;
    }

    /** Los latigazos y a cuantos ha arrastrado. */
    public int latigazos() {
        return latigazos;
    }

    public int arrastrados() {
        return arrastrados;
    }

    /** Las estrellas y las ondas (de la estrella) que dan. */
    public int estrellas() {
        return estrellas;
    }

    public int ondas() {
        return ondas;
    }

    public int torbellinos() {
        return torbellinos;
    }

    /** Los saltos, las veces que ha caido de ellos y las ondas de la caida que dan. */
    public int saltos() {
        return saltos;
    }

    public int caidas() {
        return caidas;
    }

    public int ondasSalto() {
        return ondasSalto;
    }

    /** Las veces que encadena, las marcas que pone, las cadenas que brotan y a cuantos sujetan. */
    public int encadenados() {
        return encadenados;
    }

    public int marcadas() {
        return marcadas;
    }

    public int brotes() {
        return brotes;
    }

    public int sujetos() {
        return sujetos;
    }

    /** Los reflejos que salen y los marcados que se rinden a su rugido. */
    public int reflejos() {
        return reflejos;
    }

    public int rendidos() {
        return rendidos;
    }

    /** Las veces que le ha estallado la marca (media vida). */
    public int estallidos() {
        return estallidos;
    }

    /** Las marcas que esperan su cadena (pruebas). */
    public int marcasPendientes() {
        return marcas.size();
    }

    /** Si va por el aire en un salto (pruebas). */
    public boolean enVuelo() {
        return enVuelo;
    }

    /** Para las pruebas: pasa ya a media vida (le arde la marca), sin esperar a que acabe lo que hace. */
    public void mediaVida() {
        if (level().isClientSide || !isAlive()) return;
        setHealth(Math.min(getHealth(), getMaxHealth() * 0.45f));
        segunda = true;
        alSegundaMitad();
    }

    /** Para el comando y las pruebas: que caiga ya. */
    public void vencer() {
        if (level().isClientSide || !isAlive()) return;
        kill();
    }

    /** Para las pruebas: echar ya el reflejo de un golpe (aunque no le arda la marca). */
    public void reflejoDe(int accion) {
        reflejoAccion = accion;
        reflejoCuando = tickCount;
        reflejoX = getX();
        reflejoY = getY();
        reflejoZ = getZ();
        reflejoGiro = getYRot();
        echarReflejo();
    }

    // ------------------------------------------------------------------ que hace

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (quieta || reflejo()) return 0;
        double dy = presa.getY() - getY();
        // desde lo alto del ancla (o desde donde sea mas alto que su presa): salta sobre ella
        if (dy < -3.0 && d < 26 && onGround()) {
            saltoEspera = 120 + random.nextInt(60);
            return AldricAnims.SALTO;
        }
        if (Math.abs(dy) > 4.0) return 0;
        // a media vida: encadena (de cerca o de lejos)
        if (segunda && encadenarEspera <= 0 && d < 22 && random.nextFloat() < 0.4f) {
            encadenarEspera = 200 + random.nextInt(80);
            return AldricAnims.ENCADENAR;
        }
        // de lejos: salta sobre ella o la alcanza con el latigazo
        if (d > 7.0 && d < 18 && saltoEspera <= 0 && random.nextFloat() < 0.3f) {
            saltoEspera = (segunda ? 130 : 170) + random.nextInt(60);
            return AldricAnims.SALTO;
        }
        if (d >= 4.0 && d <= 8.2 && latigoEspera <= 0 && hasLineOfSight(presa) && random.nextFloat() < 0.5f) {
            latigoEspera = (segunda ? 50 : 70) + random.nextInt(30);
            return AldricAnims.LATIGAZO;
        }
        if (d < 4.8) {
            if (torbellinoEspera <= 0 && random.nextFloat() < (segunda ? 0.35f : 0.25f)) {
                torbellinoEspera = (segunda ? 110 : 150) + random.nextInt(50);
                return AldricAnims.TORBELLINO;
            }
            if (estrellaEspera <= 0 && random.nextFloat() < 0.35f) {
                estrellaEspera = 80 + random.nextInt(40);
                return AldricAnims.ESTRELLA;
            }
            return AldricAnims.CADENAZO;
        }
        return 0;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case AldricAnims.CADENAZO -> new Golpe(5.2f, 150f, 1.1f, 1.1f);
            case AldricAnims.LATIGAZO -> new Golpe(8.4f, 22f, 0.9f, 0f);
            case AldricAnims.ESTRELLA -> new Golpe(5.0f, 34f, 1.4f, 0.9f);
            case AldricAnims.TORBELLINO -> new Golpe(5.0f, 360f, 0.85f, 1.3f);
            default -> null;
        };
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (reflejo()) REFLEJOS_DAN++;
        switch (accion) {
            // el latigazo: la cadena se le enrosca y lo arrastra hacia el
            case AldricAnims.LATIGAZO -> {
                if (!reflejo()) arrastrados++;
                Vec3 a = new Vec3(getX() - le.getX(), 0, getZ() - le.getZ());
                double l = a.length();
                if (l > 2.4) {
                    a = a.scale(Math.min(1.25, 0.28 + (l - 2.4) * 0.16) / l);
                    le.setDeltaMovement(a.x, 0.32, a.z);
                    le.hurtMarked = true;
                }
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2), this);
            }
            case AldricAnims.CADENAZO, AldricAnims.TORBELLINO -> le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 16, 0), this);
            default -> {
            }
        }
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = AldricAnims.GOLPES[accion];
        // la marca arde: el reflejo repetira este golpe desde aqui
        if (t == 1 && !reflejo() && arde() && repite(accion)) {
            reflejoAccion = accion;
            reflejoCuando = tickCount + RETRASO;
            reflejoX = getX();
            reflejoY = getY();
            reflejoZ = getZ();
            reflejoGiro = getYRot();
        }
        switch (accion) {
            case AldricAnims.CADENAZO -> {
                if (t == g[0] - 8) playSound(DarkCraftMod.SFX_ALDRIC_CADENAZO.get(), reflejo() ? 1.2f : 2.0f, (reflejo() ? 0.8f : 0.95f) + random.nextFloat() * 0.1f);
                if (t == g[0] - 2) empujar(0.3);
                if (t == g[0]) {
                    if (!reflejo()) cadenazos++;
                    barrido(150, 4.6, 0.8);
                }
            }
            case AldricAnims.LATIGAZO -> {
                if (t == g[0] - 6) playSound(DarkCraftMod.SFX_ALDRIC_LATIGAZO.get(), reflejo() ? 1.2f : 2.0f, (reflejo() ? 0.8f : 0.95f) + random.nextFloat() * 0.1f);
                if (t == g[0]) {
                    if (!reflejo()) latigazos++;
                    linea(8.0);
                }
            }
            case AldricAnims.ESTRELLA -> {
                if (t == g[0] - 10) playSound(DarkCraftMod.SFX_ALDRIC_ESTRELLA.get(), reflejo() ? 1.4f : 2.4f, (reflejo() ? 0.8f : 0.95f) + random.nextFloat() * 0.1f);
                if (t == g[0] - 1) empujar(0.25);
                if (t == g[0]) {
                    if (!reflejo()) estrellas++;
                    ondas += onda(ESTRELLA_DELANTE, R_ESTRELLA, 0.75f, 0.45);
                }
            }
            case AldricAnims.TORBELLINO -> {
                if (t == 6) playSound(DarkCraftMod.SFX_ALDRIC_TORBELLINO.get(), reflejo() ? 1.3f : 2.2f, (reflejo() ? 0.8f : 1.0f));
                if (t == g[0] || t == g[1]) {
                    if (!reflejo()) torbellinos++;
                    barrido(360, 4.6, 0.6);
                }
                if (t == g[1] + 2 && !reflejo()) playSound(DarkCraftMod.SFX_ALDRIC_CADENAZO.get(), 1.4f, 1.15f);
            }
            case AldricAnims.SALTO -> salto(t);
            case AldricAnims.ENCADENAR -> {
                if (t == 6) playSound(DarkCraftMod.SFX_ALDRIC_RUGIDO.get(), 1.6f, 1.15f);
                if (t == g[0]) encadenar();
            }
            case AldricAnims.RUGIDO -> {
                if (t == 4) playSound(DarkCraftMod.SFX_ALDRIC_RUGIDO.get(), 2.8f, 0.95f);
                if (t == g[0] && level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 3.0, getZ(), 24, 0.6, 0.5, 0.6, 0.03);
                    sl.sendParticles(Marcado.MARCA, getX(), getY() + 2.0, getZ(), 30, 0.5, 0.5, 0.5, 0.1);
                    // al primer rugido de cada pelea, los marcados del cerco se arrodillan
                    if (!rindio) {
                        rindio = true;
                        rendir(sl);
                    }
                }
            }
            case AldricAnims.MARCA -> {
                if (t == 6) playSound(DarkCraftMod.SFX_ALDRIC_MARCA.get(), 2.6f, 1.0f);
                if (t > 6 && t < g[0] && t % 3 == 0 && level() instanceof ServerLevel sl)
                    sl.sendParticles(Marcado.BRASA, getX(), getY() + 2.0, getZ(), 6, 0.3, 0.3, 0.3, 0.04);
                if (t == g[0]) estallido();
            }
            case AldricAnims.LEVANTARSE -> {
                if (t == 3 && level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.6, getZ(), 16, 0.6, 0.3, 0.6, 0.02);
                    sl.sendParticles(Marcado.SOMBRA, getX(), getY() + 1.4, getZ(), 20, 0.6, 0.8, 0.6, 0.02);
                    playSound(SoundEvents.CHAIN_FALL, 1.4f, 0.6f);
                }
            }
            default -> {
            }
        }
    }

    /** Lo que repite el reflejo: los golpes de cadena (no el salto, ni encadenar, ni el rugido). */
    private static boolean repite(int accion) {
        return switch (accion) {
            case AldricAnims.CADENAZO, AldricAnims.LATIGAZO, AldricAnims.ESTRELLA, AldricAnims.TORBELLINO -> true;
            default -> false;
        };
    }

    /** La estela de un golpe de lado o en redondo: polvo y chispas en el arco que barre la cadena. */
    private void barrido(double arco, double r, double alto) {
        if (!(level() instanceof ServerLevel sl)) return;
        int n = (int) Math.max(6, arco / 15);
        for (int j = 0; j <= n; j++) {
            double a = Math.toRadians(getYRot() + 90 - arco / 2 + arco * j / n);
            double x = getX() + Math.cos(a) * r, z = getZ() + Math.sin(a) * r;
            sl.sendParticles(reflejo() ? Marcado.SOMBRA : ParticleTypes.CRIT, x, getY() + alto, z, 1, 0.1, 0.1, 0.1, 0.05);
            if (j % 2 == 0) sl.sendParticles(ParticleTypes.SWEEP_ATTACK, x, getY() + alto + 0.4, z, 1, 0, 0, 0, 0);
        }
    }

    /** El latigazo: la cadena recta delante, hasta {@code largo}; chispas a lo largo y un chasquido en la punta. */
    private void linea(double largo) {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        for (double s = 1.5; s <= largo; s += 0.7)
            sl.sendParticles(reflejo() ? Marcado.SOMBRA : ParticleTypes.CRIT, getX() + d.x * s, getY() + 1.4, getZ() + d.z * s, 2, 0.08, 0.08, 0.08, 0.02);
        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX() + d.x * largo, getY() + 1.4, getZ() + d.z * largo, 8, 0.2, 0.2, 0.2, 0.2);
    }

    /**
     * El golpe en el suelo: polvo, piedras y una onda (a {@code delante} bloques, de radio {@code r}) que alcanza a quien este
     * de pie (saltando o rodando se libra). Devuelve a cuantos ha dado.
     */
    private int onda(double delante, double r, float mult, double arriba) {
        if (!(level() instanceof ServerLevel sl)) return 0;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * delante, z = getZ() + d.z * delante;
        BlockState suelo = level().getBlockState(BlockPos.containing(x, getY() - 0.5, z));
        int n = (int) Math.round(r * 14);
        if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), x, getY() + 0.1, z, n, r * 0.4, 0.05, r * 0.4, 0.25);
        sl.sendParticles(ParticleTypes.EXPLOSION, x, getY() + 0.3, z, r > 3.5 ? 3 : 1, r * 0.3, 0, r * 0.3, 0);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, x, getY() + 0.2, z, n / 2, r * 0.4, 0.1, r * 0.4, 0.02);
        sl.sendParticles(reflejo() ? Marcado.SOMBRA : Marcado.BRASA, x, getY() + 0.3, z, 20, r * 0.35, 0.1, r * 0.35, 0.05);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * mult;
        int dan = 0;
        for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, new AABB(x - r - 1, getY() - 1, z - r - 1, x + r + 1, getY() + 2.5, z + r + 1),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            if (!le.onGround() || le.distanceToSqr(x, le.getY(), z) > r * r) continue;
            if (le.hurt(damageSources().mobAttack(this), dano)) {
                dan++;
                if (reflejo()) REFLEJOS_DAN++;
                le.knockback(0.6, x - le.getX(), z - le.getZ());
                le.setDeltaMovement(le.getDeltaMovement().add(0, arriba, 0));
                le.hurtMarked = true;
            }
        }
        return dan;
    }

    // ------------------------------------------------------------------ el salto

    /**
     * El salto: se agacha (0-11), en {@link #DESPEGA} sale despedido hacia donde esta su presa (con la velocidad justa para caer
     * encima en {@link #CAE}, aunque este mas abajo: desde lo alto del ancla al patio) y al caer, la onda.
     */
    private void salto(int t) {
        if (t == 4) playSound(DarkCraftMod.SFX_ALDRIC_RUGIDO.get(), 1.3f, 1.2f);
        if (t == DESPEGA) {
            LivingEntity p = getTarget();
            Vec3 d = Vec3.directionFromRotation(0, getYRot());
            double tx = p == null ? getX() + d.x * 5 : p.getX(), tz = p == null ? getZ() + d.z * 5 : p.getZ();
            double ty = p == null ? getY() : p.getY();
            double dx = tx - getX(), dz = tz - getZ(), h = Math.sqrt(dx * dx + dz * dz);
            // se queda a un paso de su presa (no encima); hacia abajo sale igual hacia arriba (subida no baja de 0,35): el vuelo
            // dura entonces mas que CAE - DESPEGA, y lo de lado se reparte en lo que dura de verdad
            double lleva = Math.max(0, h - 1.6);
            double vy = subida(ty - getY(), CAE - DESPEGA);
            double v = Math.min(1.15, lleva / vuelo(vy, ty - getY(), CAE - DESPEGA));
            vueloX = h < 0.01 ? 0 : dx / h * v;
            vueloZ = h < 0.01 ? 0 : dz / h * v;
            setDeltaMovement(vueloX, vy, vueloZ);
            hasImpulse = true;
            enVuelo = true;
            if (!reflejo()) saltos++;
            if (level() instanceof ServerLevel sl) {
                BlockState suelo = level().getBlockState(BlockPos.containing(getX(), getY() - 0.5, getZ()));
                if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), getX(), getY() + 0.1, getZ(), 24, 0.6, 0.05, 0.6, 0.2);
            }
        } else if (enVuelo && t > DESPEGA) {
            if (onGround() && t > DESPEGA + 2) caer();
            // el paso de cada tick de la accion (y el aire) frena: en el aire no
            else setDeltaMovement(vueloX, getDeltaMovement().y, vueloZ);
        }
    }

    /** La velocidad hacia arriba con la que, saliendo ahora, se esta a {@code dy} de altura al cabo de {@code n} ticks. */
    static double subida(double dy, int n) {
        double lo = -1.0, hi = 2.0;
        for (int it = 0; it < 40; it++) {
            double mid = (lo + hi) * 0.5;
            double y = 0, vy = mid;
            for (int k = 0; k < n; k++) {
                y += vy;
                vy = (vy - 0.08) * 0.98;
            }
            if (y < dy) lo = mid;
            else hi = mid;
        }
        return Math.max(0.35, Math.min(1.3, (lo + hi) * 0.5));
    }

    /**
     * Los ticks que tarda en volver a {@code dy} (ya bajando) saliendo hacia arriba con {@code vy}; si no llega a esa altura,
     * {@code siNo}.
     */
    static int vuelo(double vy, double dy, int siNo) {
        double y = 0;
        for (int k = 1; k <= 80; k++) {
            y += vy;
            vy = (vy - 0.08) * 0.98;
            if (vy < 0 && y <= dy + 1e-6) return k;
        }
        return siNo;
    }

    /** Cae del salto: la cadena contra el suelo y la onda alrededor. */
    private void caer() {
        enVuelo = false;
        setDeltaMovement(0, getDeltaMovement().y, 0);
        if (!(level() instanceof ServerLevel)) return;
        caidas++;
        playSound(DarkCraftMod.SFX_ALDRIC_SALTO.get(), 2.6f, 0.9f + random.nextFloat() * 0.1f);
        ondasSalto += onda(0.8, R_SALTO, 1.3f, 0.6);
    }

    // ------------------------------------------------------------------ encadenar

    /** Golpea el suelo con el puno: una marca bajo los pies de cada uno (y de su presa); al momento, la cadena brota. */
    private void encadenar() {
        if (!(level() instanceof ServerLevel sl)) return;
        encadenados++;
        playSound(DarkCraftMod.SFX_ALDRIC_ENCADENAR.get(), 2.2f, 0.85f);
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        BlockState suelo = level().getBlockState(BlockPos.containing(getX() + d.x * 1.4, getY() - 0.5, getZ() + d.z * 1.4));
        if (!suelo.isAir())
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), getX() + d.x * 1.4, getY() + 0.1, getZ() + d.z * 1.4, 30, 0.6, 0.05, 0.6, 0.2);
        sl.sendParticles(Marcado.MARCA, getX() + d.x * 1.4, getY() + 0.2, getZ() + d.z * 1.4, 30, 0.6, 0.1, 0.6, 0.05);
        List<LivingEntity> blancos = new ArrayList<>(sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(24, 6, 24),
                e -> e != this && e.isAlive() && objetivoValido(e) && (e instanceof Player || e == getTarget())));
        for (LivingEntity le : blancos) {
            if (le.distanceToSqr(this) > 24 * 24) continue;
            marcar(sl, le.getX(), le.getZ(), le.getY());
            // y otra delante de por donde va
            Vec3 v = le.getDeltaMovement();
            if (v.horizontalDistanceSqr() > 0.004) marcar(sl, le.getX() + v.x * 14, le.getZ() + v.z * 14, le.getY());
        }
        if (!cuatro) {
            cuatro = true;
            hablaSiCalla(4);
        }
    }

    private void marcar(ServerLevel sl, double x, double z, double cerca) {
        double y = Puestos.suelo(sl, Mth.floor(x), Mth.floor(cerca) + 2, Mth.floor(z));
        if (Math.abs(y - cerca) > 4) y = cerca;
        marcas.add(new Marca(x, y, z, tickCount + CADENA_T));
        marcadas++;
        sl.playSound(null, x, y, z, SoundEvents.CHAIN_PLACE, SoundSource.HOSTILE, 1.4f, 0.6f);
    }

    /** Cada tick: las marcas echan su aviso (un aro de brasas que se cierra y el suelo que tiembla) y, a su hora, la cadena. */
    private void tickMarcas(ServerLevel sl) {
        for (int i = marcas.size() - 1; i >= 0; i--) {
            Marca m = marcas.get(i);
            if (tickCount < m.cuando()) {
                if (tickCount % 2 == 0) {
                    double r = R_CADENA * (0.6 + 0.4 * (m.cuando() - tickCount) / (double) CADENA_T);
                    for (int k = 0; k < 10; k++) {
                        double a = (k / 10.0 + tickCount * 0.03) * Math.PI * 2;
                        sl.sendParticles(Marcado.MARCA, m.x() + Math.cos(a) * r, m.y() + 0.08, m.z() + Math.sin(a) * r, 1, 0, 0, 0, 0);
                    }
                }
                if (tickCount % 5 == 0) {
                    BlockState suelo = sl.getBlockState(BlockPos.containing(m.x(), m.y() - 0.5, m.z()));
                    if (!suelo.isAir())
                        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), m.x(), m.y() + 0.05, m.z(), 4, 0.5, 0.0, 0.5, 0.05);
                }
                continue;
            }
            marcas.remove(i);
            brotar(sl, m);
        }
    }

    /** Brota la cadena: tres cadenas que salen del suelo y se enroscan; a quien este encima, dano y lo sujeta un rato. */
    private void brotar(ServerLevel sl, Marca m) {
        brotes++;
        sl.playSound(null, m.x(), m.y(), m.z(), DarkCraftMod.SFX_ALDRIC_ENCADENAR.get(), SoundSource.HOSTILE, 1.6f, 1.0f + random.nextFloat() * 0.15f);
        BlockState cadena = Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS, Direction.Axis.Y);
        for (int k = 0; k < 3; k++) {
            float yaw = (float) (k * Math.PI * 2 / 3 + random.nextFloat() * 0.6);
            double r = 0.45 + random.nextDouble() * 0.3;
            EfectosRaiz.estaca(sl, cadena, m.x() + Math.cos(yaw) * r, m.y() - 0.2, m.z() + Math.sin(yaw) * r, yaw, 0.25f + random.nextFloat() * 0.2f,
                    2.4f + random.nextFloat() * 0.8f, 1.6f, k, SUJETA - 10);
        }
        BlockState suelo = sl.getBlockState(BlockPos.containing(m.x(), m.y() - 0.5, m.z()));
        if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), m.x(), m.y() + 0.1, m.z(), 26, 0.6, 0.1, 0.6, 0.25);
        sl.sendParticles(Marcado.SOMBRA, m.x(), m.y() + 1.0, m.z(), 16, 0.4, 0.6, 0.4, 0.02);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.7f;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, new AABB(m.x() - 2, m.y() - 1, m.z() - 2, m.x() + 2, m.y() + 3, m.z() + 2),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            if (le.distanceToSqr(m.x(), le.getY(), m.z()) > R_CADENA * R_CADENA) continue;
            le.invulnerableTime = 0;
            if (le.hurt(damageSources().mobAttack(this), dano)) {
                sujetos++;
                // la cadena sujeta todo lo que toca: casi no se anda ni se salta
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SUJETA, 5), this);
                le.setDeltaMovement(0, Math.min(0, le.getDeltaMovement().y), 0);
                le.hurtMarked = true;
            }
        }
    }

    // ------------------------------------------------------------------ el rugido: los marcados del cerco se rinden

    /** Los marcados del cerco se arrodillan donde estan (no pelean mientras dure la pelea, salvo que se les pegue). */
    private void rendir(ServerLevel sl) {
        int n = 0;
        AABB caja = new AABB(Cerco.CX - 45, Cerco.PISO - 12, Cerco.CZ - 45, Cerco.CX + 46, Cerco.PISO + 30, Cerco.CZ + 60);
        for (Marcado mk : sl.getEntitiesOfClass(Marcado.class, caja, q -> q.isAlive() && !q.reflejo())) {
            if (Cerco.distancia(mk.getX(), mk.getZ()) > Cerco.R_RENDIR) continue;
            if (mk.rendir(true, this)) n++;
        }
        rendidos += n;
        if (n > 0) sl.playSound(null, getX(), getY(), getZ(), DarkCraftMod.SFX_CERCO_ARRODILLAN.get(), SoundSource.HOSTILE, 2.4f, 1.0f);
        DarkCraftMod.LOGGER.info("[DarkCraft] Aldric ruge: se arrodillan {} marcados del cerco", n);
    }

    /** Se acaba la pelea (sin caer): los rendidos vuelven a lo suyo. */
    private void soltar(ServerLevel sl) {
        AABB caja = new AABB(Cerco.CX - 45, Cerco.PISO - 12, Cerco.CZ - 45, Cerco.CX + 46, Cerco.PISO + 30, Cerco.CZ + 60);
        for (Marcado mk : sl.getEntitiesOfClass(Marcado.class, caja, q -> q.isAlive() && q.rendido())) mk.rendir(false, this);
    }

    // ------------------------------------------------------------------ la marca que arde (media vida)

    /** Le estalla la marca: el fogonazo y la onda alrededor (a quien este de pie, cerca); desde ahora, su reflejo. */
    private void estallido() {
        if (!(level() instanceof ServerLevel sl)) return;
        estallidos++;
        sl.sendParticles(Marcado.MARCA, getX(), getY() + 2.0, getZ(), 60, 0.4, 0.4, 0.4, 0.25);
        sl.sendParticles(Marcado.SOMBRA, getX(), getY() + 1.6, getZ(), 40, 0.8, 0.8, 0.8, 0.04);
        sl.sendParticles(ParticleTypes.FLAME, getX(), getY() + 2.0, getZ(), 30, 0.4, 0.5, 0.4, 0.08);
        for (int k = 0; k < 24; k++) {
            double a = k * Math.PI * 2 / 24;
            sl.sendParticles(Marcado.BRASA, getX() + Math.cos(a) * R_MARCA, getY() + 0.3, getZ() + Math.sin(a) * R_MARCA, 2, 0.1, 0.05, 0.1, 0.02);
        }
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.55f;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(R_MARCA + 1, 2, R_MARCA + 1),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            if (le.distanceToSqr(getX(), le.getY(), getZ()) > R_MARCA * R_MARCA) continue;
            if (le.hurt(damageSources().indirectMagic(this, this), dano)) {
                le.knockback(0.9, getX() - le.getX(), getZ() - le.getZ());
                le.setRemainingFireTicks(Math.max(le.getRemainingFireTicks(), 40));
                le.hurtMarked = true;
            }
        }
    }

    // ------------------------------------------------------------------ el reflejo

    /** Sale el reflejo: una sombra suya en el sitio donde empezo el golpe, que lo repite (y se deshace al acabar). */
    private void echarReflejo() {
        if (!(level() instanceof ServerLevel sl) || reflejoAccion == 0) return;
        int accion = reflejoAccion;
        reflejoAccion = 0;
        Aldric r = DarkCraftMod.ALDRIC.get().create(sl);
        if (r == null) return;
        var dano = r.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dano != null) dano.setBaseValue(getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.8);
        r.moveTo(reflejoX, reflejoY, reflejoZ, reflejoGiro, 0f);
        r.yBodyRot = r.yHeadRot = reflejoGiro;
        r.entityData.set(REFLEJO, true);
        r.presaDelReflejo = getTarget();
        r.setNoGravity(true);
        sl.addFreshEntity(r);
        r.empezar(accion);
        reflejos++;
        sl.sendParticles(Marcado.SOMBRA, reflejoX, reflejoY + 1.5, reflejoZ, 24, 0.4, 0.9, 0.4, 0.02);
        sl.playSound(null, reflejoX, reflejoY, reflejoZ, DarkCraftMod.SFX_MARCADO_REFLEJO.get(), SoundSource.HOSTILE, 1.5f, 0.75f + random.nextFloat() * 0.1f);
    }

    @Override
    public boolean isPickable() {
        return !reflejo() && super.isPickable();
    }

    @Override
    public boolean isPushable() {
        return !reflejo() && super.isPushable();
    }

    @Override
    public boolean canBeCollidedWith() {
        return !reflejo() && super.canBeCollidedWith();
    }

    /** El reflejo no se gira hacia nadie: repite el golpe hacia donde se dio. */
    @Override
    protected float girarHacia(LivingEntity t, float max) {
        return reflejo() ? getYRot() : super.girarHacia(t, max);
    }

    @Override
    public boolean shouldBeSaved() {
        return !reflejo() && super.shouldBeSaved();
    }

    // ------------------------------------------------------------------ lo que dice

    /** Dice la frase n (1..5) a quien este cerca (voz y subtitulo). */
    public void habla(int n) {
        if (!(level() instanceof ServerLevel sl) || reflejo()) return;
        for (ServerPlayer p : sl.players()) if (p.distanceToSqr(this) < 64 * 64) Net.to(p, new Net.ReyHabla(getId(), VOZ_BASE + n));
        hablaHasta = tickCount + Voces.ticks(VOZ_BASE + n);
    }

    private void hablaSiCalla(int n) {
        if (tickCount >= hablaHasta + Voces.PAUSA) habla(n);
    }

    @Override
    protected void alEmpezarPelea() {
        cuatro = rindio = false;
        saltoEspera = 0;
        latigoEspera = 40;
        estrellaEspera = 40;
        torbellinoEspera = 100;
        encadenarEspera = 60;
        long ahora = level().getGameTime();
        if (ahora - ultimaEntrada > 20 * 60 * 4 || ahora < ultimaEntrada) {
            ultimaEntrada = ahora;
            habla(1);
            // se levanta mientras lo dice y no ruge ni ataca hasta acabarlo
            int dura = Voces.ticks(VOZ_BASE + 1);
            esperaRugido = tickCount + dura - 10;
            pausa = Math.max(pausa, dura);
        } else esperaRugido = 0;
    }

    @Override
    protected boolean puedeRugir() {
        return !quieta && (segunda || tickCount >= esperaRugido);
    }

    @Override
    protected void alAcabarPelea() {
        marcas.clear();
        entityData.set(ARDE, false);
        esperaRugido = 0;
        enVuelo = false;
        if (level() instanceof ServerLevel sl) soltar(sl);
    }

    /** A media vida le arde la marca (su frase 2): el estallido y desde ahora su reflejo; y encadena. */
    @Override
    protected void alSegundaMitad() {
        entityData.set(ARDE, true);
        empezar(AldricAnims.MARCA);
        pausa = Math.max(pausa, 6);
        encadenarEspera = Math.min(encadenarEspera, 40);
        hablaHasta = 0;
        habla(2);
    }

    @Override
    public void setTarget(LivingEntity t) {
        // al reflejo no se le pone presa (no empieza peleas: es una sombra)
        if (reflejo()) return;
        super.setTarget(t);
        if (t != null && !level().isClientSide) ultimaPresa = t;
    }

    /** Solo repara en quien entra en el cerco (lo de fuera de la muralla no le importa); de rodillas en lo alto, a 20 bloques. */
    @Override
    protected boolean percibe(LivingEntity p) {
        if (reflejo() || !Cerco.enCerco(p.getX(), p.getZ()) || Math.abs(p.getY() - Cerco.PISO) > 14) return false;
        if (postura() != DE_PIE) return distanceToSqr(p) < VE * VE && hasLineOfSight(p);
        return super.percibe(p);
    }

    /** A quien puede pegar: los de dentro del cerco (y su presa); un reflejo, los jugadores y la presa del que lo echo. */
    @Override
    protected boolean objetivoValido(LivingEntity e) {
        if (e instanceof Marcado) return false;
        if (reflejo()) {
            if (e instanceof Player p) return !p.isCreative() && !p.isSpectator();
            return e == presaDelReflejo;
        }
        if (e instanceof Player && enPelea() && !Cerco.enCerco(e.getX(), e.getZ())) return false;
        return super.objetivoValido(e);
    }

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        // al reflejo no se le puede pegar: es una sombra
        if (reflejo()) return false;
        return super.hurt(s, cuanto);
    }

    @Override
    public void aiStep() {
        if (reflejo()) {
            // la sombra: hace su golpe sin moverse de donde salio y se deshace al acabar
            setDeltaMovement(Vec3.ZERO);
            super.aiStep();
            if (level().isClientSide) {
                if (tickCount % 2 == 0)
                    level().addParticle(Marcado.SOMBRA, getX() + (random.nextDouble() - 0.5) * 1.0, getY() + random.nextDouble() * 3.0,
                            getZ() + (random.nextDouble() - 0.5) * 1.0, 0, 0.02, 0);
                return;
            }
            if (tickCount > 3 && accion() == 0 || tickCount > 90) {
                if (level() instanceof ServerLevel sl) sl.sendParticles(Marcado.SOMBRA, getX(), getY() + 1.5, getZ(), 24, 0.4, 0.9, 0.4, 0.02);
                discard();
            }
            return;
        }
        super.aiStep();
        if (level().isClientSide) {
            if (!isAlive() && deathTime > AldricAnims.TICKS[AldricAnims.MORIR]) return;
            // la marca le arde: brasas que le salen del pecho (por el agujero de la coraza)
            if (isAlive() && (arde() ? tickCount % 2 == 0 : tickCount % 9 == 0)) {
                Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
                level().addParticle(arde() ? Marcado.BRASA : Marcado.MARCA, getX() + d.x * 0.5, getY() + 2.1, getZ() + d.z * 0.5, d.x * 0.03,
                        0.04, d.z * 0.03);
            }
            return;
        }
        if (!isAlive()) return;
        if (saltoEspera > 0) saltoEspera--;
        if (latigoEspera > 0) latigoEspera--;
        if (estrellaEspera > 0) estrellaEspera--;
        if (torbellinoEspera > 0) torbellinoEspera--;
        if (encadenarEspera > 0) encadenarEspera--;
        ServerLevel sl = (ServerLevel) level();
        tickMarcas(sl);
        if (reflejoAccion != 0 && tickCount >= reflejoCuando) echarReflejo();
        // cayo de un salto sin acabar la accion (la corto un aturdido): la caida igual
        if (enVuelo && accion() != AldricAnims.SALTO && onGround()) caer();
        // de rodillas en lo alto del ancla: su rezo
        if (!enPelea() && postura() == REZA && --espera <= 0) {
            espera = 200 + random.nextInt(100);
            playSound(DarkCraftMod.SFX_ALDRIC_REZO.get(), 1.4f, 1.0f);
        }
        // andando, la cadena se arrastra por el suelo
        if (enPelea() && accion() == 0 && onGround() && getDeltaMovement().horizontalDistanceSqr() > 0.002 && --arrastre <= 0) {
            arrastre = 40;
            playSound(DarkCraftMod.SFX_ALDRIC_ARRASTRE.get(), 0.9f, 0.9f + random.nextFloat() * 0.15f);
        }
        // ha caido aquel con quien peleaba
        if (enPelea() && getTarget() == null && ultimaPresa != null && ultimaPresa.isDeadOrDying()) {
            ultimaPresa = null;
            hablaSiCalla(3);
        }
    }

    @Override
    public void die(DamageSource s) {
        boolean ya = !isAlive() && deathTime > 0;
        marcas.clear();
        super.die(s);
        if (level().isClientSide || ya) return;
        hablaHasta = 0;
        habla(5);
        // (los marcados que se rindieron siguen de rodillas: descansan, Cerco.paz)
        if (level() instanceof ServerLevel sl) sl.sendParticles(Marcado.MARCA, getX(), getY() + 2.0, getZ(), 40, 0.5, 0.6, 0.5, 0.1);
    }

    /** Al caer no se va en humo: cae de rodillas, mira a la ciudadela mientras dice su ultima frase y se deshace en sombra. */
    @Override
    protected void tickDeath() {
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        int fin = AldricAnims.TICKS[AldricAnims.MORIR] + 40;
        if (level() instanceof ServerLevel sl) {
            if (deathTime == 26) {
                playSound(DarkCraftMod.SFX_ALDRIC_MUERTE.get(), 2.4f, 1.0f);
                BlockState suelo = level().getBlockState(BlockPos.containing(getX(), getY() - 0.5, getZ()));
                if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), getX(), getY() + 0.1, getZ(), 30, 0.8, 0.05, 0.8, 0.2);
            }
            if (deathTime == 40) sl.playSound(null, getX(), getY(), getZ(), SoundEvents.CHAIN_FALL, SoundSource.HOSTILE, 1.6f, 0.55f);
            // la marca se le apaga despacio
            if (deathTime % 4 == 0 && deathTime < fin - 10)
                sl.sendParticles(Marcado.MARCA, getX(), getY() + 1.8, getZ(), 2, 0.3, 0.3, 0.3, 0.01);
            if (deathTime > fin - 36 && deathTime % 2 == 0)
                sl.sendParticles(Marcado.SOMBRA, getX(), getY() + 0.6 + (deathTime - (fin - 36)) * 0.06, getZ(), 6, 0.6, 0.6, 0.6, 0.02);
            if (deathTime == fin - 2) {
                // se deshace en sombra
                sl.sendParticles(Marcado.SOMBRA, getX(), getY() + 1.6, getZ(), 160, 0.8, 1.4, 0.8, 0.04);
                sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 2.0, getZ(), 40, 0.6, 1.2, 0.6, 0.05);
                sl.sendParticles(Marcado.MARCA, getX(), getY() + 1.8, getZ(), 30, 0.4, 0.4, 0.4, 0.2);
                sl.playSound(null, getX(), getY() + 1, getZ(), DarkCraftMod.SFX_MARCADO_MUERTE.get(), SoundSource.HOSTILE, 2.0f, 0.6f);
            }
        }
        if (deathTime >= fin) remove(RemovalReason.KILLED);
    }

    /** Mientras dice su primera frase se queda donde esta (levantandose); despues, cerca: pelea de cerca. */
    @Override
    protected double distanciaDeseada() {
        return enPelea() && tickCount < esperaRugido ? 99.0 : 3.6;
    }

    /** Lo que puede haber de desnivel con su presa para atacarla: desde lo alto del ancla salta sobre ella. */
    @Override
    protected double alturaMaxima() {
        return 12.0;
    }

    @Override
    protected double prisa() {
        return arde() ? 1.15 : 1.0;
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (accion) {
            case AldricAnims.RUGIDO, AldricAnims.MARCA -> 6;
            case AldricAnims.SALTO -> (arde() ? 8 : 14) + random.nextInt(8);
            case AldricAnims.ENCADENAR -> 8 + random.nextInt(6);
            case AldricAnims.TORBELLINO -> (arde() ? 12 : 18) + random.nextInt(10);
            default -> (arde() ? 8 : 12) + random.nextInt(12);
        };
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return null;
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return null;
    }

    /** El paso pesado de las grebas, y la cadena. */
    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        playSound(tickCount % 2 == 0 ? SoundEvents.NETHERITE_BLOCK_STEP : SoundEvents.CHAIN_STEP, 0.7f, 0.7f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return reflejo() ? null : DarkCraftMod.SFX_ALDRIC_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected boolean shouldDropLoot() {
        return !reflejo() && super.shouldDropLoot();
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_3.get(), 3));
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_2.get(), 2));
        spawnAtLocation(new ItemStack(DarkCraftMod.CENIZAS.get(com.darkcraft.combate.Habilidad.TORBELLINO).get()));
    }

    /** Para las pruebas y el comando: como esta. */
    public String estado() {
        return String.format(java.util.Locale.ROOT, "vida %.0f/%.0f, %s, %s en %.1f,%.1f,%.1f; cadenazos %d, latigazos %d (arrastra a %d), estrellas %d"
                        + " (ondas %d), torbellinos %d, saltos %d (cae %d, ondas %d), encadena %d (marcas %d, brotan %d, sujetan %d), reflejos %d,"
                        + " rendidos %d, estallidos %d%s",
                getHealth(), getMaxHealth(), enPelea() ? "peleando" : "esperando", postura() == DE_PIE ? "de pie" : "de rodillas",
                getX(), getY(), getZ(), cadenazos, latigazos, arrastrados, estrellas, ondas, torbellinos, saltos, caidas, ondasSalto, encadenados,
                marcadas, brotes, sujetos, reflejos, rendidos, estallidos, arde() ? " (le arde la marca)" : "");
    }
}
