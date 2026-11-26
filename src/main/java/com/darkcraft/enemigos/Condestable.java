package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.Juramento;
import com.darkcraft.net.Net;
import com.darkcraft.rey.Voces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * El Condestable del Juramento (parte 7n): el jefe de la Capital en ruinas. Ser Halvard, el que mando cerrar todas las
 * puertas de la ciudad la noche del cielo rojo (libro perdido 22) para que el fuego no saliera de ella: la gente ardio en las
 * calles y la guardia en las murallas, y el se quedo de rodillas en el Patio del Juramento ({@code morvain/Juramento}) con el
 * montante clavado delante, ardiendo dentro de su armadura. "Juro guardar la puerta. Juro no dormir. Juro no morir."
 * <ul>
 * <li>al ver a alguien (en el patio) se levanta mientras dice su frase, el rastrillo cae detras de quien ha entrado y al
 * acabar de hablar ruge;</li>
 * <li>el <b>tajo</b>: alza el montante con las dos manos y lo descarga contra el suelo; del golpe <b>corre el fuego</b> por el
 * suelo en linea hasta lejos (a media vida, tres lineas en abanico); de lado se libra;</li>
 * <li>el <b>barrido</b>, ancho, a media altura: quema y empuja;</li>
 * <li>la <b>embestida</b>: agacha el hombro y cruza el patio; si se le esquiva y da contra un muro, se queda aturdido;</li>
 * <li>la <b>llamarada</b>, a quien se le pega o se le pone detras: clava el montante y revienta un anillo de fuego que se
 * abre alrededor (saltandolo no quema);</li>
 * <li><b>alzar</b>: el montante al cielo, y desde lo alto de los muros cae la brea ardiendo donde esta cada uno (y en algun
 * sitio mas: se ve la marca antes);</li>
 * <li>a <b>media vida</b> toca el cuerno: "¡Guardia! ¡A mi! ¡Sostened la puerta!": se alza la guardia de los montones de
 * ceniza del patio (dos, una mas por amigo, hasta cuatro);</li>
 * <li><b>el juramento</b>: la primera vez que le vencen no cae: hinca la rodilla ("Jure... ¡no morir!"), y se alza otra vez
 * con un tercio de la vida y el fuego reventando a su alrededor; desde entonces pelea con mas furia (el fuego de sus golpes
 * llega mas lejos y descansa menos entre uno y otro).</li>
 * </ul>
 * El fuego no le hace nada; el frio, un 60 % mas. Habla (ElevenLabs, en ingles con subtitulos; {@link Voces#BASE_CONDESTABLE}):
 * 1 al empezar (no la repite en 4 min), 2 al tocar el cuerno, 3 si cae su presa, 4 al jurar y 5 al caer de verdad: "Abrid...
 * abrid las puertas. Que vuelvan a casa": entonces se alza el rastrillo, la reja de su torre y, por fin, el de la Puerta Oeste
 * (lo hace {@code Juramento}); lo demas (el cartel con sus ecos, la hazana {@code condestable} y el bestiario) lo reparte
 * {@code valdrenn/Guardianes}.
 */
public class Condestable extends Guardian {
    public static final int ECOS = 12500;
    public static final int VOZ_BASE = Voces.BASE_CONDESTABLE;
    /** La linea de fuego del tajo: hasta donde llega (con furia, mas) y lo que hay entre una llama y otra. */
    public static final double FUEGO_MAX = 13.0, FUEGO_FURIA = 16.0, FUEGO_PASO = 1.5;
    /** Lo que duran las llamas de la linea y su radio. */
    public static final int FUEGO_TICKS = 46;
    public static final float FUEGO_R = 1.1f;
    /** El anillo de la llamarada: hasta donde llega (el del juramento, mas) y lo que tarda en abrirse. */
    public static final double ANILLO = 8.5, ANILLO_JURAMENTO = 11.0;
    public static final int ANILLO_TICKS = 12;
    /** La embestida: de donde a donde la usa, lo que dura la carrera y lo que corre por tick. */
    public static final double EMBESTIDA_MIN = 6.5, EMBESTIDA_MAX = 17.0, EMBESTIDA_PASO = 0.85;
    public static final int EMBESTIDA_TICKS = 16;
    /** Lo que recupera al jurar (de la vida entera). */
    public static final float JURAMENTO_VIDA = 0.3f;

    static final DustParticleOptions BRASA = new DustParticleOptions(new Vector3f(1.0f, 0.42f, 0.1f), 1.2f),
            MARCA = new DustParticleOptions(new Vector3f(1.0f, 0.32f, 0.05f), 1.1f),
            CENIZA = new DustParticleOptions(new Vector3f(0.55f, 0.52f, 0.5f), 1.6f);
    static final BlockParticleOption PIEDRA = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE_BRICKS.defaultBlockState());

    /** La ultima vez que dijo su frase de entrada (tiempo del mundo): no la repite en cada intento. */
    private static long ultimaEntrada = Long.MIN_VALUE / 2;

    private int alzarEspera = 80, cercaEspera, embestidaEspera = 60, tajoLejosEspera = 60, hablaHasta, esperaRugido, repetido, ultimo, espera = 60;
    private boolean llamo, juro, furia, vencido;
    private LivingEntity ultimaPresa;
    private final List<Calcinado> suyos = new ArrayList<>();

    /** Una llama de la linea del tajo (sale en su tick) o la marca de donde cae la brea (hasta su tick). */
    private record Llama(double x, double y, double z, int cuando) {}

    private final List<Llama> llamas = new ArrayList<>(), marcas = new ArrayList<>();
    private final Set<Integer> fuegoTocados = new HashSet<>(), anilloTocados = new HashSet<>(), embestidaTocados = new HashSet<>();
    /** El anillo de fuego que se abre: desde que tick (-1 ninguno) y hasta donde llega. */
    private int anilloDesde = -1;
    private double anilloMax;
    private Vec3 embestidaDir;
    /** Para las pruebas. */
    private int tajos, llamasHechas, llamasQueDan, barridos, embestidas, embestidasQueDan, choques, llamaradas, anillosQueDan, alzados, brea,
            guardiasAlzados, juramentos;

    public Condestable(EntityType<? extends Condestable> t, Level l) {
        super(t, l, "condestable", BossEvent.BossBarColor.RED);
    }

    public static AttributeSupplier.Builder atributos() {
        // la vida: la que da Minecraft como mucho (1024); el juramento le devuelve un tercio
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1024.0).add(Attributes.ATTACK_DAMAGE, 15.0).add(Attributes.ARMOR, 14.0)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0).add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.FOLLOW_RANGE, 34.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public String tipo() {
        return "condestable";
    }

    @Override
    public int musica() {
        return 37;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return "condestable";
    }

    @Override
    protected int[] ticks() {
        return CondestableAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return CondestableAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return CondestableAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return CondestableAnims.MORIR;
    }

    @Override
    protected int accionRugido() {
        return CondestableAnims.GRITO;
    }

    @Override
    protected int accionLevantarse() {
        return CondestableAnims.LEVANTARSE;
    }

    // ------------------------------------------------------------------ para las pruebas

    public int tajos() {
        return tajos;
    }

    /** Cuantas llamas ha echado por el suelo (las lineas del tajo) y a cuantos han dado. */
    public int llamasHechas() {
        return llamasHechas;
    }

    public int llamasQueDan() {
        return llamasQueDan;
    }

    public int barridos() {
        return barridos;
    }

    public int embestidas() {
        return embestidas;
    }

    public int embestidasQueDan() {
        return embestidasQueDan;
    }

    /** Las veces que se ha estrellado contra un muro embistiendo (y se ha quedado aturdido). */
    public int choques() {
        return choques;
    }

    public int llamaradas() {
        return llamaradas;
    }

    public int anillosQueDan() {
        return anillosQueDan;
    }

    /** Las veces que ha alzado el montante al cielo y los cantaros de brea que han caido de los muros. */
    public int alzados() {
        return alzados;
    }

    public int brea() {
        return brea;
    }

    public int guardiasAlzados() {
        return guardiasAlzados;
    }

    public int juramentos() {
        return juramentos;
    }

    public boolean furia() {
        return furia;
    }

    /** Cuantos de los de la guardia que ha llamado siguen en pie. */
    public int suyos() {
        int n = 0;
        for (Calcinado c : suyos) if (c.isAlive() && !c.isRemoved()) n++;
        return n;
    }

    /** Cuantas llamas y marcas estan por salir. */
    public int pendientes() {
        return llamas.size() + marcas.size();
    }

    /** Para las pruebas: pasa ya a media vida (toca el cuerno y se alza la guardia), sin esperar a que acabe lo que hace. */
    public void mediaVida() {
        if (level().isClientSide || !isAlive()) return;
        setHealth(Math.min(getHealth(), getMaxHealth() * 0.45f));
        segunda = true;
        alSegundaMitad();
        empezar(CondestableAnims.GRITO);
    }

    /** Para el comando y las pruebas: que caiga de verdad (sin jurar). */
    public void vencer() {
        if (level().isClientSide || !isAlive()) return;
        vencido = true;
        kill();
    }

    // ------------------------------------------------------------------ que hace

    /** Si {@code p} esta a su espalda (mas de 110 grados de donde mira). */
    private boolean detras(LivingEntity p) {
        Vec3 mira = Vec3.directionFromRotation(0, yBodyRot);
        double dx = p.getX() - getX(), dz = p.getZ() - getZ(), l = Math.sqrt(dx * dx + dz * dz);
        return l > 0.01 && (dx * mira.x + dz * mira.z) / l < -0.34;
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        double dy = presa.getY() - getY();
        if (Math.abs(dy) > 4.0) return 0;
        // quien se le pega o se le pone detras: la llamarada (el anillo de fuego)
        if (cercaEspera <= 0 && (d < 3.8 && detras(presa) || d < 2.6)) {
            cercaEspera = (furia ? 55 : 75) + random.nextInt(30);
            return CondestableAnims.LLAMARADA;
        }
        // la brea de los muros: a quien se aleja, y de vez en cuando a cualquiera
        if (alzarEspera <= 0 && (d > 8.0 || random.nextFloat() < 0.2f)) {
            alzarEspera = (segunda ? 170 : 250) + random.nextInt(60);
            return CondestableAnims.ALZAR;
        }
        // de lejos: la embestida, que cruza el patio
        if (embestidaEspera <= 0 && d > EMBESTIDA_MIN && d < EMBESTIDA_MAX && random.nextFloat() < 0.5f) {
            embestidaEspera = (furia ? 90 : 130) + random.nextInt(50);
            return CondestableAnims.EMBESTIDA;
        }
        // o el tajo, que le manda el fuego por el suelo
        if (d > 6.0 && d < FUEGO_MAX && tajoLejosEspera <= 0 && random.nextFloat() < 0.4f) {
            tajoLejosEspera = 80 + random.nextInt(50);
            return CondestableAnims.TAJO;
        }
        if (d > 5.8) return 0;
        // de cerca: el tajo o el barrido (no el mismo tres veces seguidas)
        int a = random.nextFloat() < 0.45f ? CondestableAnims.TAJO : CondestableAnims.BARRIDO;
        if (a == ultimo) {
            if (++repetido >= 2) {
                a = a == CondestableAnims.TAJO ? CondestableAnims.BARRIDO : CondestableAnims.TAJO;
                repetido = 0;
            }
        } else repetido = 0;
        ultimo = a;
        return a;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case CondestableAnims.TAJO -> new Golpe(5.6f, 50f, 1.8f, 1.0f);
            case CondestableAnims.BARRIDO -> new Golpe(5.8f, 240f, 1.3f, 1.3f);
            case CondestableAnims.LLAMARADA -> new Golpe(3.0f, 90f, 0.8f, 0.6f);
            default -> null;
        };
    }

    /** Lo que da el montante, quema; el barrido, ademas, frena. */
    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        Calcinado.quemar(le, accion == CondestableAnims.BARRIDO ? 4 : 5);
        if (accion == CondestableAnims.BARRIDO) le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1), this);
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = CondestableAnims.GOLPES[accion];
        switch (accion) {
            case CondestableAnims.TAJO -> {
                if (t == g[0] - 1) empujar(0.3);
                if (t == g[0]) {
                    playSound(DarkCraftMod.SFX_CONDESTABLE_TAJO.get(), 2.4f, 0.92f + random.nextFloat() * 0.12f);
                    tajos++;
                    fuegoTocados.clear();
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    if (level() instanceof ServerLevel sl) {
                        double x = getX() + d.x * 4.4, z = getZ() + d.z * 4.4;
                        sl.sendParticles(PIEDRA, x, getY() + 0.3, z, 40, 0.7, 0.2, 0.7, 0.2);
                        sl.sendParticles(ParticleTypes.FLAME, x, getY() + 0.4, z, 40, 0.8, 0.3, 0.8, 0.08);
                        sl.sendParticles(ParticleTypes.LAVA, x, getY() + 0.4, z, 8, 0.5, 0.2, 0.5, 0.0);
                    }
                    lineaDeFuego(0);
                    if (segunda || furia) {
                        lineaDeFuego(-22);
                        lineaDeFuego(22);
                    }
                }
            }
            case CondestableAnims.BARRIDO -> {
                if (t == g[0] - 5) playSound(DarkCraftMod.SFX_CONDESTABLE_BARRIDO.get(), 2.2f, 0.9f + random.nextFloat() * 0.12f);
                if (t == g[0] - 1) empujar(0.25);
                if (t == g[0]) {
                    barridos++;
                    if (level() instanceof ServerLevel sl)
                        for (int k = 0; k <= 14; k++) {
                            double a = Math.toRadians(getYRot() + 90 - 120 + 240 * k / 14.0);
                            double x = getX() + Math.cos(a) * 4.8, z = getZ() + Math.sin(a) * 4.8;
                            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, x, getY() + 1.8, z, 1, 0, 0, 0, 0);
                            sl.sendParticles(ParticleTypes.FLAME, x, getY() + 1.6, z, 5, 0.3, 0.2, 0.3, 0.03);
                            // con furia, el fuego se queda en el suelo por donde pasa el montante
                            if (furia && k % 4 == 2) {
                                double y = suelo(sl, x, z);
                                if (!Double.isNaN(y)) sl.addFreshEntity(new Cantaro.Llamas(sl, x, y, z, this, FUEGO_R, FUEGO_TICKS, k == 2));
                            }
                        }
                }
            }
            case CondestableAnims.EMBESTIDA -> {
                if (t == 6) playSound(DarkCraftMod.SFX_CONDESTABLE_EMBESTIDA.get(), 2.4f, 0.95f + random.nextFloat() * 0.1f);
                if (t == g[0]) {
                    embestidas++;
                    embestidaTocados.clear();
                    embestidaDir = Vec3.directionFromRotation(0, getYRot());
                }
                if (embestidaDir != null && t >= g[0] && t < g[0] + EMBESTIDA_TICKS) embestir(t - g[0]);
                if (t == g[0] + EMBESTIDA_TICKS) embestidaDir = null;
            }
            case CondestableAnims.LLAMARADA -> {
                if (t == g[0]) {
                    playSound(DarkCraftMod.SFX_CONDESTABLE_LLAMARADA.get(), 2.4f, 0.95f + random.nextFloat() * 0.1f);
                    llamaradas++;
                    anillo(ANILLO);
                }
            }
            case CondestableAnims.ALZAR -> {
                if (t == 8) playSound(DarkCraftMod.SFX_CONDESTABLE_ALZAR.get(), 2.4f, 1.0f);
                if (t >= 12 && t <= g[0] + 8 && t % 2 == 0 && level() instanceof ServerLevel sl) {
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    double y = getY() + getBbHeight() + 1.8;
                    sl.sendParticles(ParticleTypes.FLAME, getX() + d.x * 0.4, y, getZ() + d.z * 0.4, 6, 0.25, 0.6, 0.25, 0.04);
                    sl.sendParticles(ParticleTypes.LAVA, getX() + d.x * 0.4, y, getZ() + d.z * 0.4, 1, 0.1, 0.2, 0.1, 0.0);
                }
                if (t == g[0]) {
                    alzados++;
                    breaDeLosMuros(true);
                }
                if (t == g[0] + 16 && (segunda || furia)) breaDeLosMuros(false);
            }
            case CondestableAnims.GRITO -> {
                if (t == 8) {
                    playSound(DarkCraftMod.SFX_CONDESTABLE_GRITO.get(), 3.0f, 1.0f);
                    if (level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.FLAME, getX(), getY() + getBbHeight() * 0.7, getZ(), 70, 1.6, 1.4, 1.6, 0.12);
                        sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + getBbHeight() * 0.9, getZ(), 20, 1.0, 0.6, 1.0, 0.04);
                    }
                }
                // la de media vida: el cuerno y "¡guardia, a mi!": se alza la guardia de la ceniza
                if (t == 16 && segunda && !llamo) {
                    llamo = true;
                    habla(2);
                    cuerno();
                    llamar();
                }
            }
            case CondestableAnims.JURAMENTO -> {
                // de rodillas, tiembla y echa chispas; se va curando; al alzarse, el fuego revienta
                if (level() instanceof ServerLevel sl && t % 3 == 0)
                    sl.sendParticles(ParticleTypes.FLAME, getX(), getY() + 1.2, getZ(), 8, 0.7, 0.9, 0.7, 0.03);
                if (t >= 12 && t <= g[0]) {
                    float f = (t - 12) / (float) (g[0] - 12);
                    super.setHealth(Math.max(getHealth(), Mth.lerp(f, 1f, getMaxHealth() * JURAMENTO_VIDA)));
                }
                if (t == g[0] - 10) playSound(DarkCraftMod.SFX_CONDESTABLE_ALZAR.get(), 2.6f, 0.9f);
                if (t == g[0]) {
                    playSound(DarkCraftMod.SFX_CONDESTABLE_LLAMARADA.get(), 3.0f, 0.85f);
                    anillo(ANILLO_JURAMENTO);
                    furia = true;
                    if (level() instanceof ServerLevel sl)
                        sl.sendParticles(ParticleTypes.FLAME, getX(), getY() + getBbHeight() * 0.6, getZ(), 120, 1.4, 1.8, 1.4, 0.15);
                }
            }
            case CondestableAnims.LEVANTARSE -> {
                if (t == 3 && level() instanceof ServerLevel sl) {
                    sl.sendParticles(CENIZA, getX(), getY() + 1.2, getZ(), 40, 0.9, 1.0, 0.9, 0.0);
                    sl.sendParticles(ParticleTypes.FLAME, getX(), getY() + 1.6, getZ(), 20, 0.8, 1.0, 0.8, 0.03);
                    playSound(SoundEvents.NETHERITE_BLOCK_PLACE, 1.6f, 0.5f);
                }
            }
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ el fuego que corre por el suelo, el anillo, la embestida

    /** Lo alto del suelo bajo (x, z) cerca de su altura, o NaN si no hay (un hueco, un muro). */
    private double suelo(ServerLevel sl, double x, double z) {
        int bx = Mth.floor(x), bz = Mth.floor(z), by = Mth.floor(getY());
        for (int y = by + 2; y >= by - 4; y--) {
            BlockPos p = new BlockPos(bx, y, bz);
            BlockState s = sl.getBlockState(p);
            if (!s.getCollisionShape(sl, p).isEmpty() && sl.getBlockState(p.above()).getCollisionShape(sl, p.above()).isEmpty()) return y + 1;
        }
        return Double.NaN;
    }

    /** Del golpe del montante, el fuego corre por el suelo hacia delante (desviado {@code grados}): una llama tras otra. */
    private void lineaDeFuego(float grados) {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot() + grados);
        double max = furia ? FUEGO_FURIA : FUEGO_MAX;
        int k = 0;
        for (double s = 4.4; s <= max; s += FUEGO_PASO, k++) {
            double x = getX() + d.x * s, z = getZ() + d.z * s;
            double y = suelo(sl, x, z);
            if (Double.isNaN(y)) break;
            llamas.add(new Llama(x, y, z, tickCount + k + 1));
        }
    }

    /** Las llamas de las lineas en su momento: prenden el suelo y queman a quien pillan al salir. */
    private void llamas(ServerLevel sl) {
        float base = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        Iterator<Llama> it = llamas.iterator();
        boolean primera = true;
        while (it.hasNext()) {
            Llama l = it.next();
            if (tickCount < l.cuando()) continue;
            it.remove();
            llamasHechas++;
            sl.addFreshEntity(new Cantaro.Llamas(sl, l.x(), l.y(), l.z(), this, FUEGO_R, FUEGO_TICKS, primera && llamasHechas % 6 == 1));
            primera = false;
            sl.sendParticles(ParticleTypes.FLAME, l.x(), l.y() + 0.3, l.z(), 12, 0.35, 0.4, 0.35, 0.04);
            sl.sendParticles(ParticleTypes.LAVA, l.x(), l.y() + 0.3, l.z(), 1, 0.2, 0.1, 0.2, 0.0);
            for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, new AABB(l.x() - 1.6, l.y() - 0.5, l.z() - 1.6, l.x() + 1.6, l.y() + 2.2,
                    l.z() + 1.6), e -> e != this && e.isAlive() && objetivoValido(e))) {
                if (le.distanceToSqr(l.x(), le.getY(), l.z()) > 1.3 * 1.3 || !fuegoTocados.add(le.getId())) continue;
                if (le.hurt(damageSources().mobAttack(this), base * 0.8f)) {
                    Calcinado.quemar(le, 4);
                    le.setDeltaMovement(le.getDeltaMovement().add(0, 0.35, 0));
                    le.hurtMarked = true;
                    llamasQueDan++;
                }
            }
        }
    }

    /** Un anillo de fuego que se abre desde el hasta {@code r} (la llamarada; el del juramento, mas grande). */
    private void anillo(double r) {
        anilloDesde = tickCount;
        anilloMax = r;
        anilloTocados.clear();
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(PIEDRA, getX(), getY() + 0.2, getZ(), 50, 1.4, 0.1, 1.4, 0.2);
            sl.sendParticles(ParticleTypes.LAVA, getX(), getY() + 0.4, getZ(), 12, 1.0, 0.2, 1.0, 0.0);
        }
    }

    /** Cada tick mientras se abre el anillo: las llamas en su borde y quien este en el (en el suelo) se quema. */
    private void tickAnillo(ServerLevel sl) {
        int t = tickCount - anilloDesde;
        if (anilloDesde < 0 || t > ANILLO_TICKS) {
            anilloDesde = -1;
            return;
        }
        double r = 1.5 + (anilloMax - 1.5) * t / ANILLO_TICKS, r0 = 1.5 + (anilloMax - 1.5) * Math.max(0, t - 1) / ANILLO_TICKS;
        int n = (int) Math.max(10, r * 5);
        for (int k = 0; k < n; k++) {
            double a = k * Math.PI * 2 / n + t * 0.13;
            sl.sendParticles(ParticleTypes.FLAME, getX() + Math.cos(a) * r, getY() + 0.25, getZ() + Math.sin(a) * r, 2, 0.1, 0.15, 0.1, 0.02);
            if (k % 3 == 0) sl.sendParticles(BRASA, getX() + Math.cos(a) * r, getY() + 0.5, getZ() + Math.sin(a) * r, 1, 0.1, 0.2, 0.1, 0.0);
        }
        float base = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(anilloMax + 1, 2.0, anilloMax + 1),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            double dx = le.getX() - getX(), dz = le.getZ() - getZ(), d = Math.sqrt(dx * dx + dz * dz);
            // lo alcanza el borde del anillo (entre donde estaba el tick de antes y donde esta ahora, con medio bloque de holgura)
            if (d < r0 - 0.9 || d > r + 0.5 || anilloTocados.contains(le.getId())) continue;
            // saltandolo no quema (los pies por encima del fuego)
            if (le.getY() > getY() + 0.75) continue;
            anilloTocados.add(le.getId());
            if (le.hurt(damageSources().mobAttack(this), base * 1.1f)) {
                Calcinado.quemar(le, 5);
                double l = Math.max(0.1, d);
                le.knockback(0.9, -dx / l, -dz / l);
                anillosQueDan++;
            }
        }
    }

    /** Un tick de la carrera: corre hacia delante y arrolla a quien pille (una vez); si da contra un muro, se queda aturdido. */
    private void embestir(int k) {
        Vec3 d = embestidaDir;
        if (k > 2 && horizontalCollision) {
            choques++;
            embestidaDir = null;
            playSound(SoundEvents.ANVIL_LAND, 1.8f, 0.5f);
            if (level() instanceof ServerLevel sl)
                sl.sendParticles(PIEDRA, getX() + d.x * 1.2, getY() + 1.6, getZ() + d.z * 1.2, 50, 0.6, 1.0, 0.6, 0.2);
            empezar(CondestableAnims.ATURDIDO);
            pausa = CondestableAnims.TICKS[CondestableAnims.ATURDIDO] + 12;
            return;
        }
        setDeltaMovement(d.x * EMBESTIDA_PASO, getDeltaMovement().y, d.z * EMBESTIDA_PASO);
        hasImpulse = true;
        if (level() instanceof ServerLevel sl && k % 2 == 0)
            sl.sendParticles(ParticleTypes.FLAME, getX() - d.x * 0.8, getY() + 0.3, getZ() - d.z * 0.8, 6, 0.4, 0.2, 0.4, 0.02);
        float base = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(1.4, 0.5, 1.4), e -> e != this && e.isAlive()
                && objetivoValido(e))) {
            double dx = le.getX() - getX(), dz = le.getZ() - getZ(), l = Math.sqrt(dx * dx + dz * dz);
            if (l > 2.6 || (l > 0.5 && (dx * d.x + dz * d.z) / l < -0.2) || !embestidaTocados.add(le.getId())) continue;
            if (le.hurt(damageSources().mobAttack(this), base * 1.5f)) {
                le.knockback(1.8, -d.x, -d.z);
                le.setDeltaMovement(le.getDeltaMovement().add(0, 0.4, 0));
                le.hurtMarked = true;
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1), this);
                Calcinado.quemar(le, 3);
                embestidasQueDan++;
                playSound(DarkCraftMod.SFX_GOLPE_PESADO.get(), 1.6f, 0.7f);
            }
        }
    }

    // ------------------------------------------------------------------ la brea de los muros

    /**
     * La brea: desde lo alto de los muros del patio (lo mas cerca de cada blanco) sale un cantaro de pez ardiendo hacia
     * cada uno de los que pelean (a 30 bloques) y, la primera tanda, hacia dos o tres sitios mas del patio; donde va a caer
     * se ve la marca en el suelo mientras vuela.
     */
    private void breaDeLosMuros(boolean otros) {
        if (!(level() instanceof ServerLevel sl)) return;
        List<double[]> donde = new ArrayList<>();
        for (Player p : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(30, 8, 30)))
            if (objetivoValido(p) && p.isAlive()) donde.add(new double[]{p.getX(), p.getZ()});
        LivingEntity t = getTarget();
        if (t != null && !(t instanceof Player) && t.isAlive()) donde.add(new double[]{t.getX(), t.getZ()});
        if (otros) {
            int n = 2 + random.nextInt(2);
            for (int k = 0; k < n; k++) {
                double a = random.nextDouble() * Math.PI * 2, r = 5 + random.nextDouble() * 10;
                double x = getX() + Math.cos(a) * r, z = getZ() + Math.sin(a) * r;
                if (Juramento.enPatio(x, z)) donde.add(new double[]{x, z});
            }
        }
        List<int[]> almenas = Juramento.almenasDeDentro();
        for (double[] q : donde) {
            double y = suelo(sl, q[0], q[1]);
            if (Double.isNaN(y)) continue;
            // la almena mas cercana (y no encima del blanco)
            int[] mejor = null;
            double md = Double.MAX_VALUE;
            for (int[] a : almenas) {
                double dd = Math.hypot(a[0] + 0.5 - q[0], a[2] + 0.5 - q[1]);
                if (dd > 4 && dd < md) {
                    md = dd;
                    mejor = a;
                }
            }
            if (mejor == null) continue;
            double ox = mejor[0] + 0.5, oy = mejor[1] + 0.6, oz = mejor[2] + 0.5;
            Cantaro c = new Cantaro(sl, this);
            c.setPos(ox, oy, oz);
            c.setDeltaMovement(Cantaro.tiro(ox, oy, oz, q[0], y, q[1]));
            sl.addFreshEntity(c);
            sl.sendParticles(ParticleTypes.FLAME, ox, oy + 0.3, oz, 10, 0.2, 0.2, 0.2, 0.03);
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, ox, oy + 0.5, oz, 4, 0.2, 0.2, 0.2, 0.01);
            int vuela = (int) Mth.clamp(10 + Math.hypot(q[0] - ox, q[1] - oz) * 1.1, 10, 34);
            marcas.add(new Llama(q[0], y, q[1], tickCount + vuela));
            brea++;
        }
    }

    /** Las marcas de donde va a caer la brea: un corro de brasas en el suelo hasta que llega. */
    private void tickMarcas(ServerLevel sl) {
        Iterator<Llama> it = marcas.iterator();
        while (it.hasNext()) {
            Llama m = it.next();
            if (tickCount >= m.cuando() + 2) {
                it.remove();
                continue;
            }
            if ((m.cuando() - tickCount) % 3 != 0) continue;
            for (int k = 0; k < 10; k++) {
                double a = k * Math.PI * 2 / 10 + tickCount * 0.15;
                sl.sendParticles(MARCA, m.x() + Math.cos(a) * Cantaro.R_LLAMAS * 0.8, m.y() + 0.1, m.z() + Math.sin(a) * Cantaro.R_LLAMAS * 0.8,
                        1, 0.02, 0.0, 0.02, 0.0);
            }
            sl.sendParticles(ParticleTypes.SMOKE, m.x(), m.y() + 0.2, m.z(), 2, 0.4, 0.05, 0.4, 0.01);
        }
    }

    // ------------------------------------------------------------------ el cuerno y la guardia

    private void cuerno() {
        if (!(level() instanceof ServerLevel sl)) return;
        for (ServerPlayer p : sl.players())
            if (p.distanceToSqr(this) < 90 * 90)
                p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                        net.minecraft.core.Holder.direct(DarkCraftMod.SFX_CONDESTABLE_CUERNO.get()), SoundSource.HOSTILE, getX(), getY() + 4, getZ(), 2.4f,
                        1.0f, sl.random.nextLong()));
    }

    /** "¡Guardia! ¡A mi!": de los montones de ceniza del patio se alza la guardia (efimera: se deshace al acabar la pelea). */
    private void llamar() {
        if (!(level() instanceof ServerLevel sl)) return;
        List<ServerPlayer> cerca = new ArrayList<>();
        for (ServerPlayer p : sl.players()) if (p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) < 40 * 40) cerca.add(p);
        int n = Math.max(2, Math.min(4, 1 + cerca.size()));
        List<int[]> sitios = Juramento.enPatio(getX(), getZ()) ? new ArrayList<>(Juramento.montones()) : new ArrayList<>();
        if (sitios.isEmpty()) {
            // fuera del patio (las pruebas): alrededor de el
            for (int k = 0; k < 6; k++) {
                double a = k * Math.PI / 3;
                sitios.add(new int[]{Mth.floor(getX() + Math.cos(a) * 7), Mth.floor(getZ() + Math.sin(a) * 7)});
            }
        }
        // los montones mas lejos de los jugadores primero (que no salgan encima de nadie)
        sitios.sort((a, b) -> Double.compare(lejania(b, cerca), lejania(a, cerca)));
        int salen = 0;
        for (int[] q : sitios) {
            if (salen >= n) break;
            int bx = q[0], bz = q[1];
            double y = Puestos.suelo(sl, bx, Mth.floor(getY()) + 2, bz);
            if (Math.abs(y - getY()) > 2.6 || lejania(q, cerca) < 3.0) continue;
            Calcinado c = DarkCraftMod.CALCINADO.get().create(sl);
            if (c == null) return;
            float mira = (float) Math.toDegrees(Math.atan2(getZ() - (bz + 0.5), getX() - (bx + 0.5))) - 90f;
            c.moveTo(bx + 0.5, y, bz + 0.5, mira, 0f);
            if (!sl.noCollision(c)) continue;
            c.variante(Calcinado.GUARDIA);
            // enterrados en la ceniza: se alzan de golpe (sin puesto: no vuelven)
            c.asignar(null, -1, BlockPos.containing(bx + 0.5, y, bz + 0.5), mira, TUMBADO, 0);
            c.efimero(240);
            sl.addFreshEntity(c);
            LivingEntity t = getTarget() != null ? getTarget()
                    : sl.getNearestPlayer(c.getX(), c.getY(), c.getZ(), 30, e -> e instanceof Player pl && objetivoValido(pl));
            if (t != null) c.setTarget(t);
            sl.sendParticles(CENIZA, bx + 0.5, y + 0.4, bz + 0.5, 40, 0.5, 0.4, 0.5, 0.0);
            sl.sendParticles(ParticleTypes.FLAME, bx + 0.5, y + 0.6, bz + 0.5, 14, 0.4, 0.5, 0.4, 0.03);
            sl.playSound(null, bx + 0.5, y, bz + 0.5, DarkCraftMod.SFX_CALCINADO_ALZARSE.get(), SoundSource.HOSTILE, 1.4f, 0.8f + random.nextFloat() * 0.2f);
            suyos.add(c);
            salen++;
        }
        guardiasAlzados += salen;
        DarkCraftMod.LOGGER.info("[DarkCraft] El Condestable toca el cuerno: se alzan {} de la guardia ({} jugadores)", salen, cerca.size());
    }

    private static double lejania(int[] q, List<ServerPlayer> jugadores) {
        double m = Double.MAX_VALUE;
        for (ServerPlayer p : jugadores) m = Math.min(m, Math.hypot(p.getX() - (q[0] + 0.5), p.getZ() - (q[1] + 0.5)));
        return m;
    }

    private void despedir() {
        for (Calcinado c : suyos) if (c.isAlive() && !c.isRemoved()) c.disolver();
        suyos.clear();
    }

    // ------------------------------------------------------------------ el juramento: no cae la primera vez

    /** Si al recibir un golpe de muerte jura en vez de caer: la primera vez de cada pelea, peleando (no si se le vence a mano). */
    private boolean puedeJurar() {
        return !juro && !vencido && enPelea() && isAlive() && !level().isClientSide;
    }

    @Override
    public void setHealth(float h) {
        if (h <= 0f && level() != null && puedeJurar()) {
            super.setHealth(1f);
            jurar();
            return;
        }
        super.setHealth(h);
    }

    /** "Jure... ¡no morir!": hinca la rodilla (no le entra nada mientras) y se alza con un tercio de la vida y furia. */
    private void jurar() {
        juro = true;
        juramentos++;
        llamas.clear();
        marcas.clear();
        embestidaDir = null;
        empezar(CondestableAnims.JURAMENTO);
        pausa = Math.max(pausa, CondestableAnims.TICKS[CondestableAnims.JURAMENTO] + 6);
        hablaHasta = 0;
        habla(4);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.5, getZ(), 30, 0.8, 1.0, 0.8, 0.03);
            sl.sendParticles(CENIZA, getX(), getY() + 1.0, getZ(), 40, 1.0, 0.8, 1.0, 0.0);
        }
        DarkCraftMod.LOGGER.info("[DarkCraft] El Condestable jura no morir: se alza otra vez");
    }

    // ------------------------------------------------------------------ lo que dice

    /** Dice la frase n (1..5) a quien este cerca (voz y subtitulo). */
    public void habla(int n) {
        if (!(level() instanceof ServerLevel sl)) return;
        for (ServerPlayer p : sl.players()) if (p.distanceToSqr(this) < 64 * 64) Net.to(p, new Net.ReyHabla(getId(), VOZ_BASE + n));
        hablaHasta = tickCount + Voces.ticks(VOZ_BASE + n);
    }

    private void hablaSiCalla(int n) {
        if (tickCount >= hablaHasta + Voces.PAUSA) habla(n);
    }

    @Override
    protected void alEmpezarPelea() {
        llamo = juro = furia = vencido = false;
        alzarEspera = 80;
        cercaEspera = 0;
        embestidaEspera = 60;
        tajoLejosEspera = 60;
        repetido = ultimo = 0;
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
        return segunda || tickCount >= esperaRugido;
    }

    @Override
    protected void alAcabarPelea() {
        despedir();
        llamas.clear();
        marcas.clear();
        anilloDesde = -1;
        embestidaDir = null;
        esperaRugido = 0;
        juro = furia = false;
    }

    @Override
    protected void alSegundaMitad() {
        alzarEspera = Math.min(alzarEspera, 60);
        embestidaEspera = Math.min(embestidaEspera, 40);
    }

    @Override
    public void setTarget(LivingEntity t) {
        super.setTarget(t);
        if (t != null && !level().isClientSide) ultimaPresa = t;
    }

    /** Solo repara en quien esta dentro del patio (los de fuera, tras el rastrillo, no le importan). */
    @Override
    protected boolean percibe(LivingEntity p) {
        return Juramento.enPatio(p.getX(), p.getZ()) && super.percibe(p);
    }

    @Override
    protected boolean objetivoValido(LivingEntity e) {
        if (e instanceof Player && enPelea() && Juramento.enPatio(getX(), getZ()) && !Juramento.enPatio(e.getX(), e.getZ())) return false;
        return super.objetivoValido(e);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            if (!isAlive() && deathTime > CondestableAnims.TICKS[CondestableAnims.MORIR]) return;
            Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
            // las brasas de la armadura, el humo del yelmo y las llamas del montante
            if (tickCount % 3 == 0)
                level().addParticle(ParticleTypes.SMALL_FLAME, getX() + (random.nextDouble() - 0.5) * 1.4, getY() + 0.6 + random.nextDouble() * 3.4,
                        getZ() + (random.nextDouble() - 0.5) * 1.4, 0, 0.02, 0);
            if (tickCount % 9 == 0)
                level().addParticle(ParticleTypes.SMOKE, getX() + d.x * 0.3, getY() + getBbHeight() + 0.1, getZ() + d.z * 0.3, 0, 0.03, 0);
            if (postura() != DE_PIE && tickCount % 4 == 0)
                level().addParticle(ParticleTypes.FLAME, getX() + d.x * 1.1 + (random.nextDouble() - 0.5) * 0.3, getY() + 0.3 + random.nextDouble() * 2.2,
                        getZ() + d.z * 1.1 + (random.nextDouble() - 0.5) * 0.3, 0, 0.02, 0);
            if (tickCount % 7 == 0)
                level().addParticle(BRASA, getX() + (random.nextDouble() - 0.5) * 1.6, getY() + 1.0 + random.nextDouble() * 3.0,
                        getZ() + (random.nextDouble() - 0.5) * 1.6, 0, 0.04, 0);
            return;
        }
        if (!isAlive()) return;
        if (alzarEspera > 0) alzarEspera--;
        if (cercaEspera > 0) cercaEspera--;
        if (embestidaEspera > 0) embestidaEspera--;
        if (tajoLejosEspera > 0) tajoLejosEspera--;
        if (level() instanceof ServerLevel sl) {
            if (!llamas.isEmpty()) llamas(sl);
            if (!marcas.isEmpty()) tickMarcas(sl);
            if (anilloDesde >= 0) tickAnillo(sl);
        }
        // esperando de rodillas: el fuego quieto de su armadura y su respiracion (se le oye desde la puerta)
        if (!enPelea() && postura() == REZA && --espera <= 0) {
            espera = 150 + random.nextInt(120);
            playSound(DarkCraftMod.SFX_CONDESTABLE_ESPERA.get(), 1.8f, 0.95f + random.nextFloat() * 0.1f);
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
        llamas.clear();
        marcas.clear();
        anilloDesde = -1;
        embestidaDir = null;
        super.die(s);
        if (level().isClientSide || ya) return;
        hablaHasta = 0;
        habla(5);
        despedir();
        if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.FLAME, getX(), getY() + 2.4, getZ(), 50, 1.0, 1.4, 1.0, 0.05);
    }

    /** Al caer no se va en humo: de rodilla, apoyado en el montante, dice su ultima frase; luego se apaga y se deshace en ceniza. */
    @Override
    protected void tickDeath() {
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        int fin = CondestableAnims.TICKS[CondestableAnims.MORIR] + 50;
        if (level() instanceof ServerLevel sl) {
            if (deathTime % 6 == 0 && deathTime < fin - 10)
                sl.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 2.6, getZ(), 4, 0.5, 0.8, 0.5, 0.01);
            if (deathTime == fin - 2) {
                sl.sendParticles(CENIZA, getX(), getY() + 1.8, getZ(), 160, 1.0, 1.6, 1.0, 0.0);
                sl.sendParticles(ParticleTypes.ASH, getX(), getY() + 2.0, getZ(), 120, 1.2, 1.8, 1.2, 0.02);
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 2.2, getZ(), 30, 0.8, 1.2, 0.8, 0.02);
                sl.sendParticles(ParticleTypes.SMALL_FLAME, getX(), getY() + 2.0, getZ(), 40, 0.8, 1.4, 0.8, 0.02);
                sl.playSound(null, getX(), getY() + 1, getZ(), SoundEvents.NETHERITE_BLOCK_BREAK, SoundSource.HOSTILE, 2.0f, 0.5f);
            }
        }
        if (deathTime >= fin) remove(RemovalReason.KILLED);
    }

    @Override
    public void remove(RemovalReason r) {
        if (!level().isClientSide) despedir();
        super.remove(r);
    }

    // ------------------------------------------------------------------ el fuego no le hace nada; el frio, mas

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        // jurando no le entra nada (salvo que se le venza a mano)
        if (!vencido && accion() == CondestableAnims.JURAMENTO && isAlive()) return false;
        if (s.is(DamageTypeTags.IS_FREEZING)) cuanto *= 1.6f;
        return super.hurt(s, cuanto);
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    /** Mientras dice su primera frase se queda donde esta (levantandose); despues, de cerca. */
    @Override
    protected double distanciaDeseada() {
        return enPelea() && tickCount < esperaRugido ? 99.0 : 4.4;
    }

    @Override
    protected double prisa() {
        return furia ? 1.3 : segunda ? 1.15 : 1.0;
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (accion) {
            case CondestableAnims.GRITO -> 6;
            case CondestableAnims.ALZAR, CondestableAnims.LLAMARADA -> (furia ? 6 : 12) + random.nextInt(10);
            case CondestableAnims.EMBESTIDA -> (furia ? 8 : 14) + random.nextInt(10);
            case CondestableAnims.JURAMENTO -> 4;
            default -> (furia ? 6 : segunda ? 10 : 16) + random.nextInt(12);
        };
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return accion == CondestableAnims.TAJO ? DarkCraftMod.SFX_GOLPE_PESADO.get() : null;
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        SoundType t = s.getSoundType(level(), p, this);
        playSound(t.getStepSound(), t.getVolume() * 1.0f, t.getPitch() * 0.5f);
        playSound(SoundEvents.NETHERITE_BLOCK_STEP, 0.7f, 0.55f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_CONDESTABLE_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_CONDESTABLE_MUERTE.get();
    }

    @Override
    public boolean causeFallDamage(float d, float m, DamageSource s) {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_3.get(), 3));
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_2.get(), 2));
        spawnAtLocation(new ItemStack(DarkCraftMod.CENIZAS.get(com.darkcraft.combate.Habilidad.GRITO).get()));
    }

    /** Para las pruebas y el comando: como esta. */
    public String estado() {
        return String.format(java.util.Locale.ROOT, "vida %.0f/%.0f, %s, %s en %.1f,%.1f,%.1f; tajos %d, llamas %d (dan %d), barridos %d, embestidas %d "
                        + "(dan %d, contra el muro %d), llamaradas %d (dan %d), alzar %d (brea %d), guardia alzada %d, juramentos %d%s",
                getHealth(), getMaxHealth(), enPelea() ? "peleando" : "esperando", postura() == DE_PIE ? "de pie" : "de rodilla",
                getX(), getY(), getZ(), tajos, llamasHechas, llamasQueDan, barridos, embestidas, embestidasQueDan, choques, llamaradas, anillosQueDan,
                alzados, brea, guardiasAlzados, juramentos, furia ? " (con furia)" : "");
    }
}
