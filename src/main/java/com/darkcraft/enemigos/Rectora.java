package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.Observatorio;
import com.darkcraft.net.Net;
import com.darkcraft.rey.Voces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
 * Ysolde, la Rectora de la Academia (parte 7p): el jefe de la Academia Espectral. La noche del cielo rojo subio al
 * Observatorio con sus maestros a leer el fuego que caia ("no es fuego: es escritura") y alli se quedo, flotando ante el gran
 * telescopio con la mano alzada hacia las estrellas (libro perdido 25: "algo me lee a mi, linea a linea, desde arriba").
 * <ul>
 * <li>al ver a alguien (dentro de la rotonda) baja la vista del telescopio mientras dice su frase, el sello de luz cierra la
 * puerta detras de quien ha entrado ({@code morvain/Observatorio}) y al acabar de hablar grita;</li>
 * <li>el <b>baculo</b>: a quien se le acerca, lo barre de su derecha a su izquierda (ancho, empuja);</li>
 * <li>los <b>orbes</b>: carga la mano y suelta tres orbes de luz en abanico que siguen despacio a su presa ({@link Orbe});</li>
 * <li>la <b>lluvia de estrellas</b>: alza el baculo y la mano al cielo y las estrellas caen por la rendija de la cupula: donde
 * va a caer cada una se ve su circulo de luz en el suelo (bajo cada uno de los que pelean y en otros sitios de la rotonda);
 * hay que salir;</li>
 * <li>la <b>constelacion</b>: dibuja en el aire unas estrellas y las clava en el suelo alrededor de su presa; se unen con
 * lineas de luz y al poco se encienden: quien este sobre una linea (de pie), se quema;</li>
 * <li>el <b>parpadeo</b>: si se le pegan o se le ponen detras (o a veces tras un golpe), se apaga y aparece en otro sitio de
 * la rotonda;</li>
 * <li>a <b>media vida</b> se alza con los brazos abiertos: "¡Maestros! ¡A vuestros puestos!": de las hornacinas salen dos
 * maestros (uno mas por amigo, hasta cuatro); desde entonces lee mas deprisa (mas estrellas, constelaciones mas grandes, dos
 * lluvias seguidas).</li>
 * </ul>
 * La magia le hace la mitad; el fuego, un 40 % mas (los libros y la tinta arden). Habla (ElevenLabs, en ingles con
 * subtitulos; {@link Voces#BASE_RECTORA}): 1 al empezar (no la repite en 4 min), 2 al alzarse, 3 si cae su presa, 4 la primera
 * vez que hace caer las estrellas o dibuja la constelacion y 5 al caer: "Por fin... el cielo ha dejado de leerme". Al caer se
 * hunde, mira arriba una vez mas y se deshace en estrellas que suben por la rendija; se rompe el sello, se alza la reja de su
 * celda y los que leian en la Academia descansan; lo demas (el cartel con sus ecos, la hazana {@code rectora} y el bestiario)
 * lo reparte {@code valdrenn/Guardianes}.
 */
public class Rectora extends Guardian {
    public static final int ECOS = 14000;
    public static final int VOZ_BASE = Voces.BASE_RECTORA;
    /** El circulo de cada estrella que cae (radio) y lo que tarda en caer desde que se ve su marca (de mas a menos). */
    public static final double R_ESTRELLA = 2.1;
    public static final int ESTRELLA_TICKS = 24;
    /** Lo que tardan en encenderse las lineas de la constelacion, lo que estan encendidas y lo cerca de una que quema. */
    public static final int LINEAS_TICKS = 30, ENCENDIDA_TICKS = 10;
    public static final double R_LINEA = 0.75;
    /** Lo lejos que aparece al parpadear (dentro de la rotonda). */
    public static final double PARPADEO_MIN = 6.0, PARPADEO_MAX = 10.0;

    public static final DustParticleOptions LUZ = new DustParticleOptions(new Vector3f(0.72f, 0.82f, 1.0f), 1.2f),
            MARCA = new DustParticleOptions(new Vector3f(0.62f, 0.72f, 1.0f), 1.0f),
            LINEA = new DustParticleOptions(new Vector3f(0.55f, 0.6f, 1.0f), 0.8f),
            ARDE = new DustParticleOptions(new Vector3f(0.92f, 0.95f, 1.0f), 1.3f);

    /** La ultima vez que dijo su frase de entrada (tiempo del mundo): no la repite en cada intento. */
    private static long ultimaEntrada = Long.MIN_VALUE / 2;

    private int orbesEspera = 30, estrellasEspera = 90, constelacionEspera = 140, parpadeoEspera = 40, hablaHasta, esperaRugido, repetido,
            espera = 40, segundaLluvia = -1;
    private boolean llamo, cuatro;
    private LivingEntity ultimaPresa;
    private final List<Erudito> suyos = new ArrayList<>();

    /** Una estrella que cae: de donde sale, adonde va, cuando sale y cuando llega. */
    private record Estrella(double x0, double y0, double z0, double x1, double y1, double z1, int sale, int llega) {}

    /** Una constelacion en el suelo: sus estrellas (x, y, z) unidas en orden (y alguna mas), cuando se enciende y cuando acaba. */
    private record Constelacion(List<double[]> nodos, List<int[]> lineas, int enciende, int acaba, Set<Integer> tocados) {}

    private final List<Estrella> estrellas = new ArrayList<>();
    private final List<Constelacion> constelaciones = new ArrayList<>();
    /** Para las pruebas. */
    private int baculos, orbes, lluvias, estrellasCaidas, estrellasQueDan, constelacionesHechas, lineasQueDan, parpadeos, maestrosAlzados;
    /** Para las pruebas: no elige golpes ni grita por su cuenta (lo que se le manda hacer, si; y lo ya lanzado sigue cayendo). */
    public boolean quieta;

    public Rectora(EntityType<? extends Rectora> t, Level l) {
        super(t, l, "rectora", BossEvent.BossBarColor.PURPLE);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 960.0).add(Attributes.ATTACK_DAMAGE, 12.0).add(Attributes.ARMOR, 6.0)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0).add(Attributes.MOVEMENT_SPEED, 0.24).add(Attributes.FOLLOW_RANGE, 36.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85);
    }

    @Override
    public String tipo() {
        return "rectora";
    }

    @Override
    public int musica() {
        return 38;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return "rectora";
    }

    @Override
    protected int[] ticks() {
        return RectoraAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return RectoraAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return RectoraAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return RectoraAnims.MORIR;
    }

    @Override
    protected int accionRugido() {
        return RectoraAnims.GRITO;
    }

    @Override
    protected int accionLevantarse() {
        return RectoraAnims.LEVANTARSE;
    }

    // ------------------------------------------------------------------ para las pruebas

    public int baculos() {
        return baculos;
    }

    public int orbes() {
        return orbes;
    }

    /** Las veces que ha hecho caer las estrellas, las que han caido y a cuantos han dado. */
    public int lluvias() {
        return lluvias;
    }

    public int estrellasCaidas() {
        return estrellasCaidas;
    }

    public int estrellasQueDan() {
        return estrellasQueDan;
    }

    public int constelaciones() {
        return constelacionesHechas;
    }

    public int lineasQueDan() {
        return lineasQueDan;
    }

    public int parpadeos() {
        return parpadeos;
    }

    public int maestrosAlzados() {
        return maestrosAlzados;
    }

    /** Cuantos de los maestros que ha llamado siguen en pie. */
    public int suyos() {
        int n = 0;
        for (Erudito e : suyos) if (e.isAlive() && !e.isRemoved()) n++;
        return n;
    }

    /** Cuantas estrellas y constelaciones estan por caer o por encenderse. */
    public int pendientes() {
        return estrellas.size() + constelaciones.size();
    }

    /** Las estrellas que aun estan por llegar: {x, y, z} de su marca (pruebas). */
    public List<double[]> marcas() {
        List<double[]> l = new ArrayList<>();
        for (Estrella e : estrellas) l.add(new double[]{e.x1(), e.y1(), e.z1()});
        return l;
    }

    /** Los nodos de las constelaciones que aun no se han apagado (pruebas). */
    public List<double[]> nodos() {
        List<double[]> l = new ArrayList<>();
        for (Constelacion c : constelaciones) l.addAll(c.nodos());
        return l;
    }

    /** Para las pruebas: pasa ya a media vida (se alza y llama a sus maestros), sin esperar a que acabe lo que hace. */
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

    // ------------------------------------------------------------------ que hace

    /** Si {@code p} esta a su espalda (mas de 110 grados de donde mira). */
    private boolean detras(LivingEntity p) {
        Vec3 mira = Vec3.directionFromRotation(0, yBodyRot);
        double dx = p.getX() - getX(), dz = p.getZ() - getZ(), l = Math.sqrt(dx * dx + dz * dz);
        return l > 0.01 && (dx * mira.x + dz * mira.z) / l < -0.34;
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (quieta || Math.abs(presa.getY() - getY()) > 5.0) return 0;
        // quien se le pega o se le pone detras: se aparta (o lo barre con el baculo)
        if (parpadeoEspera <= 0 && (d < 3.4 && detras(presa) || d < 2.2 && random.nextFloat() < 0.35f)) return RectoraAnims.PARPADEO;
        if (d < 4.2) {
            if (++repetido > 2 && parpadeoEspera <= 0) {
                repetido = 0;
                return RectoraAnims.PARPADEO;
            }
            return RectoraAnims.BACULO;
        }
        repetido = 0;
        // la lluvia de estrellas, cada tanto, a cualquiera
        if (estrellasEspera <= 0 && random.nextFloat() < 0.55f) {
            estrellasEspera = (segunda ? 170 : 240) + random.nextInt(60);
            return RectoraAnims.ESTRELLAS;
        }
        // la constelacion, a quien esta a media distancia
        if (constelacionEspera <= 0 && d < 18 && random.nextFloat() < 0.5f) {
            constelacionEspera = (segunda ? 150 : 210) + random.nextInt(60);
            return RectoraAnims.CONSTELACION;
        }
        // y los orbes, a quien ve
        if (orbesEspera <= 0 && d < 22 && hasLineOfSight(presa)) {
            orbesEspera = (segunda ? 55 : 75) + random.nextInt(30);
            return RectoraAnims.ORBES;
        }
        return 0;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return accion == RectoraAnims.BACULO ? new Golpe(4.4f, 210f, 1.1f, 1.2f) : null;
    }

    /** Lo que da el baculo empuja lejos y frena un poco. */
    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (accion == RectoraAnims.BACULO) le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1), this);
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = RectoraAnims.GOLPES[accion];
        switch (accion) {
            case RectoraAnims.BACULO -> {
                if (t == g[0] - 6) playSound(DarkCraftMod.SFX_RECTORA_BACULO.get(), 2.0f, 0.95f + random.nextFloat() * 0.1f);
                if (t == g[0] - 1) empujar(0.25);
                if (t == g[0]) {
                    baculos++;
                    if (level() instanceof ServerLevel sl)
                        for (int k = 0; k <= 12; k++) {
                            double a = Math.toRadians(getYRot() + 90 - 105 + 210 * k / 12.0);
                            double x = getX() + Math.cos(a) * 3.6, z = getZ() + Math.sin(a) * 3.6;
                            sl.sendParticles(LUZ, x, getY() + 1.6, z, 2, 0.15, 0.15, 0.15, 0.0);
                            sl.sendParticles(ParticleTypes.END_ROD, x, getY() + 1.6, z, 1, 0.1, 0.1, 0.1, 0.01);
                        }
                }
            }
            case RectoraAnims.ORBES -> {
                if (t == 4) playSound(DarkCraftMod.SFX_RECTORA_ORBES.get(), 2.0f, 1.0f);
                if (t > 6 && t < g[0] && t % 2 == 0 && level() instanceof ServerLevel sl) {
                    Vec3 p = palma();
                    sl.sendParticles(LUZ, p.x, p.y, p.z, 3, 0.15, 0.15, 0.15, 0.0);
                    sl.sendParticles(ParticleTypes.ENCHANT, p.x, p.y, p.z, 4, 0.3, 0.3, 0.3, 0.4);
                }
                if (t == g[0]) lanzarOrbes();
            }
            case RectoraAnims.ESTRELLAS -> {
                if (t == 6) playSound(DarkCraftMod.SFX_RECTORA_ESTRELLAS.get(), 2.6f, 1.0f);
                if (t > 8 && t < g[0] && t % 2 == 0 && level() instanceof ServerLevel sl)
                    sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + getBbHeight() + 1.0, getZ(), 3, 0.3, 0.6, 0.3, 0.05);
                if (t == g[0]) {
                    lluvia(true);
                    if (segunda) segundaLluvia = tickCount + 34;
                    hablaPrimera();
                }
            }
            case RectoraAnims.CONSTELACION -> {
                if (t == g[0] - 12 || t == g[0] - 7 || t == g[0] - 2) {
                    if (level() instanceof ServerLevel sl) {
                        Vec3 p = punta();
                        sl.sendParticles(ARDE, p.x, p.y, p.z, 6, 0.1, 0.1, 0.1, 0.0);
                        sl.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 4, 0.1, 0.1, 0.1, 0.02);
                    }
                }
                if (t == g[0]) {
                    constelacion();
                    hablaPrimera();
                }
            }
            case RectoraAnims.PARPADEO -> {
                if (t == 2) playSound(DarkCraftMod.SFX_RECTORA_PARPADEO.get(), 1.6f, 1.0f);
                if (t == 10) apartarse();
            }
            case RectoraAnims.GRITO -> {
                if (t == 8) {
                    playSound(DarkCraftMod.SFX_RECTORA_GRITO.get(), 3.0f, 1.0f);
                    if (level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + getBbHeight() * 0.7, getZ(), 50, 1.4, 1.2, 1.4, 0.1);
                        sl.sendParticles(LUZ, getX(), getY() + getBbHeight() * 0.7, getZ(), 40, 1.2, 1.2, 1.2, 0.0);
                    }
                }
            }
            case RectoraAnims.ALZARSE -> {
                if (t == 4) playSound(DarkCraftMod.SFX_RECTORA_ALZARSE.get(), 2.6f, 1.0f);
                if (t > 6 && t < g[0] + 10 && t % 3 == 0 && level() instanceof ServerLevel sl)
                    sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.5, getZ(), 6, 0.8, 0.2, 0.8, 0.12);
                // "¡Maestros! ¡A vuestros puestos!": de las hornacinas salen sus maestros
                if (t == g[0] && !llamo) {
                    llamo = true;
                    hablaHasta = 0;
                    habla(2);
                    llamar();
                }
            }
            case RectoraAnims.LEVANTARSE -> {
                if (t == 3 && level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 2.0, getZ(), 40, 0.8, 1.0, 0.8, 0.6);
                    sl.sendParticles(LUZ, getX(), getY() + 2.4, getZ(), 20, 0.6, 0.8, 0.6, 0.0);
                }
            }
            default -> {
            }
        }
    }

    /** Donde esta su mano izquierda (la que carga los orbes): delante, a su izquierda, a media altura. */
    private Vec3 palma() {
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        return new Vec3(getX() + d.x * 1.2 + d.z * 0.5, getY() + 2.0, getZ() + d.z * 1.2 - d.x * 0.5);
    }

    /** Donde esta la estrella del baculo (delante y arriba). */
    private Vec3 punta() {
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        return new Vec3(getX() + d.x * 1.6 - d.z * 0.5, getY() + 3.0, getZ() + d.z * 1.6 + d.x * 0.5);
    }

    /** Los tres orbes, en abanico hacia su presa (cada uno la sigue despacio). */
    private void lanzarOrbes() {
        LivingEntity p = getTarget();
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 o = palma();
        Vec3 dir = p != null ? p.getEyePosition().subtract(o).normalize() : Vec3.directionFromRotation(getXRot(), getYRot());
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.55f;
        for (int k = -1; k <= 1; k++) {
            Vec3 v = dir.yRot((float) Math.toRadians(26.0 * k));
            Orbe b = new Orbe(sl, this, p, dano);
            b.setPos(o.x + v.x * 0.4, o.y + v.y * 0.4, o.z + v.z * 0.4);
            b.setDeltaMovement(v.scale(Orbe.VELOCIDAD));
            sl.addFreshEntity(b);
            orbes++;
        }
        sl.sendParticles(LUZ, o.x, o.y, o.z, 16, 0.25, 0.25, 0.25, 0.0);
    }

    // ------------------------------------------------------------------ la lluvia de estrellas

    /** Lo que se pisa cerca de (x, z) a su altura (o NaN si no hay suelo: un hueco). */
    private double suelo(ServerLevel sl, double x, double z) {
        int bx = Mth.floor(x), bz = Mth.floor(z), by = Mth.floor(getY());
        for (int y = by + 3; y >= by - 4; y--) {
            BlockPos p = new BlockPos(bx, y, bz);
            BlockState s = sl.getBlockState(p);
            if (!s.getCollisionShape(sl, p).isEmpty() && sl.getBlockState(p.above()).getCollisionShape(sl, p.above()).isEmpty()
                    && sl.getBlockState(p.above(2)).getCollisionShape(sl, p.above(2)).isEmpty()) return y + 1 + s.getCollisionShape(sl, p).max(net.minecraft.core.Direction.Axis.Y) - 1;
        }
        return Double.NaN;
    }

    /**
     * Las estrellas caen por la rendija de la cupula: una sobre cada uno de los que pelean (donde esta) y otras por la rotonda.
     * Cada una tiene su circulo de luz en el suelo desde ahora hasta que llega (unos {@link #ESTRELLA_TICKS}); la primera
     * tanda, con mas estrellas sueltas.
     */
    private void lluvia(boolean primera) {
        if (!(level() instanceof ServerLevel sl)) return;
        lluvias++;
        List<double[]> donde = new ArrayList<>();
        for (Player p : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(30, 8, 30)))
            if (objetivoValido(p) && p.isAlive()) donde.add(new double[]{p.getX(), p.getZ()});
        LivingEntity t = getTarget();
        if (t != null && !(t instanceof Player) && t.isAlive()) donde.add(new double[]{t.getX(), t.getZ()});
        int sueltas = (primera ? 4 : 2) + (segunda ? 2 : 0) + random.nextInt(2);
        boolean dentro = Observatorio.enRotonda(getX(), getZ());
        for (int k = 0; k < sueltas * 3 && sueltas > 0; k++) {
            double a = random.nextDouble() * Math.PI * 2, r = 2 + random.nextDouble() * 11;
            double x = dentro ? Observatorio.CX + 0.5 + Math.cos(a) * r : getX() + Math.cos(a) * r;
            double z = dentro ? Observatorio.CZ + 0.5 + Math.sin(a) * r : getZ() + Math.sin(a) * r;
            if (dentro && !Observatorio.enRotonda(x, z)) continue;
            boolean lejos = true;
            for (double[] q : donde) if (Math.hypot(q[0] - x, q[1] - z) < 3.0) lejos = false;
            if (!lejos) continue;
            donde.add(new double[]{x, z});
            sueltas--;
        }
        int k = 0;
        for (double[] q : donde) {
            double y = suelo(sl, q[0], q[1]);
            if (Double.isNaN(y)) continue;
            // salen de lo alto de la rendija (en la rotonda) o de muy arriba (fuera), cada una un poco despues
            double ox = dentro ? Observatorio.CX + 0.5 + (random.nextDouble() - 0.5) * 2 : q[0] + (random.nextDouble() - 0.5) * 6;
            double oz = dentro ? Observatorio.CZ - 7.5 + (random.nextDouble() - 0.5) * 12 : q[1] + (random.nextDouble() - 0.5) * 6;
            double oy = dentro ? Observatorio.MURO_ALTO + 13 : y + 22;
            int llega = tickCount + ESTRELLA_TICKS + k * 3 + (primera ? 0 : 6);
            estrellas.add(new Estrella(ox, oy, oz, q[0], y, q[1], llega - 12, llega));
            k++;
        }
        DarkCraftMod.LOGGER.info("[DarkCraft] La Rectora hace caer {} estrellas", k);
    }

    /** Cada tick: las marcas de las estrellas en el suelo, las que van cayendo (su estela) y las que llegan (revientan). */
    private void tickEstrellas(ServerLevel sl) {
        Iterator<Estrella> it = estrellas.iterator();
        boolean sonado = false;
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.25f;
        while (it.hasNext()) {
            Estrella e = it.next();
            int queda = e.llega() - tickCount;
            // la marca: un circulo de luz en el suelo que se cierra
            if (queda > 0 && queda % 2 == 0) {
                double r = R_ESTRELLA * (0.55 + 0.45 * Math.min(1.0, queda / (double) ESTRELLA_TICKS));
                for (int j = 0; j < 12; j++) {
                    double a = j * Math.PI * 2 / 12 + tickCount * 0.1;
                    sl.sendParticles(MARCA, e.x1() + Math.cos(a) * r, e.y1() + 0.1, e.z1() + Math.sin(a) * r, 1, 0.02, 0.0, 0.02, 0.0);
                }
                if (queda % 6 == 0) sl.sendParticles(ParticleTypes.END_ROD, e.x1(), e.y1() + 0.2, e.z1(), 2, 0.3, 0.05, 0.3, 0.01);
            }
            // cayendo: la estela desde la rendija
            if (tickCount >= e.sale() && queda > 0) {
                double f = (tickCount - e.sale()) / (double) (e.llega() - e.sale());
                double x = Mth.lerp(f, e.x0(), e.x1()), y = Mth.lerp(f, e.y0(), e.y1()), z = Mth.lerp(f, e.z0(), e.z1());
                sl.sendParticles(ARDE, x, y, z, 3, 0.08, 0.08, 0.08, 0.0);
                sl.sendParticles(ParticleTypes.END_ROD, x, y, z, 2, 0.05, 0.05, 0.05, 0.01);
                sl.sendParticles(ParticleTypes.FIREWORK, x, y, z, 1, 0.05, 0.05, 0.05, 0.01);
            }
            if (queda > 0) continue;
            it.remove();
            estrellasCaidas++;
            sl.sendParticles(ParticleTypes.END_ROD, e.x1(), e.y1() + 0.3, e.z1(), 40, R_ESTRELLA * 0.4, 0.3, R_ESTRELLA * 0.4, 0.15);
            sl.sendParticles(ParticleTypes.FIREWORK, e.x1(), e.y1() + 0.5, e.z1(), 20, 0.4, 0.4, 0.4, 0.12);
            sl.sendParticles(ParticleTypes.FLASH, e.x1(), e.y1() + 0.4, e.z1(), 1, 0, 0, 0, 0);
            if (!sonado) {
                sl.playSound(null, e.x1(), e.y1(), e.z1(), DarkCraftMod.SFX_RECTORA_ESTRELLA.get(), SoundSource.HOSTILE, 2.0f, 0.9f + random.nextFloat() * 0.2f);
                sonado = true;
            }
            for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, new AABB(e.x1() - R_ESTRELLA, e.y1() - 0.6, e.z1() - R_ESTRELLA,
                    e.x1() + R_ESTRELLA, e.y1() + 2.6, e.z1() + R_ESTRELLA), q -> q != this && q.isAlive() && objetivoValido(q))) {
                if (Math.hypot(le.getX() - e.x1(), le.getZ() - e.z1()) > R_ESTRELLA + le.getBbWidth() * 0.4) continue;
                if (le.hurt(damageSources().indirectMagic(this, this), dano)) {
                    le.setDeltaMovement(le.getDeltaMovement().add(0, 0.4, 0));
                    le.hurtMarked = true;
                    estrellasQueDan++;
                }
            }
        }
        if (segundaLluvia > 0 && tickCount >= segundaLluvia) {
            segundaLluvia = -1;
            lluvia(false);
        }
    }

    // ------------------------------------------------------------------ la constelacion

    /**
     * La constelacion: unas estrellas clavadas en el suelo alrededor de su presa (una bajo ella), unidas en cadena y con una
     * linea mas que cruza; las lineas se ven y a los {@link #LINEAS_TICKS} se encienden: quema a quien este sobre una (de pie).
     */
    private void constelacion() {
        LivingEntity p = getTarget();
        if (p == null || !(level() instanceof ServerLevel sl)) return;
        constelacionesHechas++;
        int n = segunda ? 7 : 5;
        List<double[]> nodos = new ArrayList<>();
        double y0 = suelo(sl, p.getX(), p.getZ());
        if (Double.isNaN(y0)) y0 = p.getY();
        nodos.add(new double[]{p.getX(), y0, p.getZ()});
        boolean dentro = Observatorio.enRotonda(getX(), getZ());
        double ang = random.nextDouble() * Math.PI * 2;
        for (int k = 1; k < n * 4 && nodos.size() < n; k++) {
            double[] ultimo = nodos.get(nodos.size() - 1);
            ang += (random.nextDouble() - 0.5) * 2.2;
            double r = 3.5 + random.nextDouble() * 3.0;
            double x = ultimo[0] + Math.cos(ang) * r, z = ultimo[2] + Math.sin(ang) * r;
            if (dentro && !Observatorio.enRotonda(x, z)) {
                ang += Math.PI * 0.6;
                continue;
            }
            double y = suelo(sl, x, z);
            if (Double.isNaN(y) || Math.abs(y - y0) > 2.5) continue;
            nodos.add(new double[]{x, y, z});
        }
        List<int[]> lineas = new ArrayList<>();
        for (int k = 0; k + 1 < nodos.size(); k++) lineas.add(new int[]{k, k + 1});
        if (nodos.size() >= 4) lineas.add(new int[]{0, nodos.size() / 2 + 1});
        constelaciones.add(new Constelacion(nodos, lineas, tickCount + LINEAS_TICKS, tickCount + LINEAS_TICKS + ENCENDIDA_TICKS, new HashSet<>()));
        sl.playSound(null, p.getX(), y0, p.getZ(), DarkCraftMod.SFX_RECTORA_CONSTELACION.get(), SoundSource.HOSTILE, 1.4f, 0.85f);
        DarkCraftMod.LOGGER.info("[DarkCraft] La Rectora dibuja una constelacion de {} estrellas", nodos.size());
    }

    /** Donde cae (x, z) sobre el segmento a-b en el plano: de 0 (en a) a 1 (en b). */
    private static double tLinea(double x, double z, double[] a, double[] b) {
        double dx = b[0] - a[0], dz = b[2] - a[2], L = dx * dx + dz * dz;
        return L < 1e-6 ? 0 : Mth.clamp(((x - a[0]) * dx + (z - a[2]) * dz) / L, 0, 1);
    }

    /** Lo lejos de (x, z) al segmento a-b en el plano. */
    private static double aLinea(double x, double z, double[] a, double[] b) {
        double t = tLinea(x, z, a, b);
        return Math.hypot(x - (a[0] + t * (b[0] - a[0])), z - (a[2] + t * (b[2] - a[2])));
    }

    /** Cada tick: las estrellas de la constelacion lucen, las lineas se dibujan (tenues; al encenderse, fuertes) y queman. */
    private void tickConstelaciones(ServerLevel sl) {
        Iterator<Constelacion> it = constelaciones.iterator();
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.1f;
        while (it.hasNext()) {
            Constelacion c = it.next();
            if (tickCount > c.acaba()) {
                it.remove();
                continue;
            }
            boolean encendida = tickCount >= c.enciende();
            if (tickCount == c.enciende()) {
                double[] q = c.nodos().get(0);
                sl.playSound(null, q[0], q[1], q[2], DarkCraftMod.SFX_RECTORA_CONSTELACION.get(), SoundSource.HOSTILE, 2.0f, 1.15f);
            }
            if (encendida || tickCount % 3 == 0) {
                for (double[] q : c.nodos()) {
                    sl.sendParticles(encendida ? ARDE : LUZ, q[0], q[1] + 0.15, q[2], encendida ? 3 : 2, 0.08, 0.02, 0.08, 0.0);
                    if (tickCount % 6 == 0) sl.sendParticles(ParticleTypes.END_ROD, q[0], q[1] + 0.3, q[2], 1, 0.05, 0.1, 0.05, 0.02);
                }
                for (int[] l : c.lineas()) {
                    double[] a = c.nodos().get(l[0]), b = c.nodos().get(l[1]);
                    double largo = Math.hypot(b[0] - a[0], b[2] - a[2]);
                    int pasos = Math.max(2, (int) (largo / (encendida ? 0.4 : 0.7)));
                    for (int k = 0; k <= pasos; k++) {
                        double f = k / (double) pasos;
                        sl.sendParticles(encendida ? ARDE : LINEA, Mth.lerp(f, a[0], b[0]), Mth.lerp(f, a[1], b[1]) + 0.12, Mth.lerp(f, a[2], b[2]), 1,
                                0.02, 0.0, 0.02, 0.0);
                    }
                    if (encendida && tickCount % 3 == 0)
                        sl.sendParticles(ParticleTypes.END_ROD, (a[0] + b[0]) / 2, (a[1] + b[1]) / 2 + 0.3, (a[2] + b[2]) / 2, 3, largo * 0.2, 0.1, largo * 0.2, 0.02);
                }
            }
            if (!encendida) continue;
            for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(30, 6, 30), q -> q != this && q.isAlive()
                    && objetivoValido(q))) {
                if (c.tocados().contains(le.getId())) continue;
                for (int[] l : c.lineas()) {
                    double[] a = c.nodos().get(l[0]), b = c.nodos().get(l[1]);
                    // de pie sobre la linea (saltando por encima, no): lo alto de la linea donde esta (sube con el escalon del estrado)
                    double yl = Mth.lerp(tLinea(le.getX(), le.getZ(), a, b), a[1], b[1]);
                    if (le.getY() > yl + 0.9 || le.getY() < yl - 1.0) continue;
                    if (aLinea(le.getX(), le.getZ(), a, b) > R_LINEA + le.getBbWidth() * 0.4) continue;
                    c.tocados().add(le.getId());
                    if (le.hurt(damageSources().indirectMagic(this, this), dano)) {
                        le.setDeltaMovement(le.getDeltaMovement().add(0, 0.3, 0));
                        le.hurtMarked = true;
                        le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0), this);
                        lineasQueDan++;
                    }
                    break;
                }
            }
        }
    }

    // ------------------------------------------------------------------ el parpadeo

    /** El parpadeo: a 6-10 bloques, dentro de la rotonda (si pelea en ella), lejos de su presa, donde quepa y haya suelo. */
    private void apartarse() {
        parpadeoEspera = (segunda ? 60 : 90) + random.nextInt(40);
        LivingEntity p = getTarget();
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 desde = p != null ? position().subtract(p.position()) : Vec3.directionFromRotation(0, getYRot()).scale(-1);
        double base = Math.atan2(desde.z, desde.x);
        boolean dentro = Observatorio.enRotonda(getX(), getZ());
        for (int k = 0; k < 16; k++) {
            double ang = base + (random.nextDouble() - 0.5) * Math.PI * (k < 8 ? 1.0 : 2.0);
            double r = PARPADEO_MIN + random.nextDouble() * (PARPADEO_MAX - PARPADEO_MIN);
            double x = getX() + Math.cos(ang) * r, z = getZ() + Math.sin(ang) * r;
            if (dentro && Observatorio.distancia(x, z) > Observatorio.R_DENTRO - 1.5) continue;
            BlockPos b = BlockPos.containing(x, getY() + 0.5, z);
            double y = Puestos.suelo(sl, b.getX(), b.getY() + 2, b.getZ());
            if (Math.abs(y - getY()) > 2.5) continue;
            Vec3 antes = position();
            setPos(x, y, z);
            if (!sl.noCollision(this) || sl.noCollision(this, getBoundingBox().move(0, -1.2, 0))) {
                setPos(antes.x, antes.y, antes.z);
                continue;
            }
            sl.sendParticles(LUZ, antes.x, antes.y + 1.6, antes.z, 30, 0.4, 1.0, 0.4, 0.0);
            sl.sendParticles(ParticleTypes.END_ROD, antes.x, antes.y + 1.6, antes.z, 12, 0.3, 0.8, 0.3, 0.05);
            teleportTo(x, y, z);
            if (p != null) girarHacia(p, 180f);
            sl.sendParticles(LUZ, x, y + 1.6, z, 30, 0.4, 1.0, 0.4, 0.0);
            playSound(DarkCraftMod.SFX_RECTORA_PARPADEO.get(), 1.4f, 1.25f);
            parpadeos++;
            return;
        }
    }

    // ------------------------------------------------------------------ los maestros

    /** "¡Maestros! ¡A vuestros puestos!": de las hornacinas de la rotonda salen sus maestros (efimeros: se van al acabar). */
    private void llamar() {
        if (!(level() instanceof ServerLevel sl)) return;
        List<ServerPlayer> cerca = new ArrayList<>();
        for (ServerPlayer p : sl.players()) if (p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) < 40 * 40) cerca.add(p);
        int n = Math.max(2, Math.min(4, 1 + cerca.size()));
        List<int[]> sitios = Observatorio.enRotonda(getX(), getZ()) ? new ArrayList<>(Observatorio.hornacinas()) : new ArrayList<>();
        if (sitios.isEmpty()) {
            // fuera de la rotonda (las pruebas): alrededor de ella
            for (int k = 0; k < 4; k++) {
                double a = k * Math.PI / 2 + Math.PI / 4;
                sitios.add(new int[]{Mth.floor(getX() + Math.cos(a) * 7), Mth.floor(getY()), Mth.floor(getZ() + Math.sin(a) * 7)});
            }
        }
        // las hornacinas mas lejos de los jugadores primero (que no salgan encima de nadie)
        sitios.sort((a, b) -> Double.compare(lejania(b, cerca), lejania(a, cerca)));
        int salen = 0;
        for (int[] q : sitios) {
            if (salen >= n) break;
            double y = Puestos.suelo(sl, q[0], q[1] + 2, q[2]);
            if (Math.abs(y - q[1]) > 2.6 || lejania(q, cerca) < 2.5) continue;
            Erudito e = DarkCraftMod.ERUDITO.get().create(sl);
            if (e == null) return;
            float mira = (float) Math.toDegrees(Math.atan2(getZ() - (q[2] + 0.5), getX() - (q[0] + 0.5))) - 90f;
            e.moveTo(q[0] + 0.5, y, q[2] + 0.5, mira, 0f);
            if (!sl.noCollision(e)) continue;
            e.variante(Erudito.MAESTRO);
            // (sin puesto: no vuelven) leyendo en su atril, se alzan de golpe
            e.asignar(null, -1, BlockPos.containing(q[0] + 0.5, y, q[2] + 0.5), mira, REZA, 0);
            e.efimero(240);
            sl.addFreshEntity(e);
            LivingEntity t = getTarget() != null ? getTarget()
                    : sl.getNearestPlayer(e.getX(), e.getY(), e.getZ(), 30, x -> x instanceof Player pl && objetivoValido(pl));
            if (t != null) e.setTarget(t);
            sl.sendParticles(LUZ, q[0] + 0.5, y + 1.0, q[2] + 0.5, 30, 0.4, 0.8, 0.4, 0.0);
            sl.sendParticles(ParticleTypes.ENCHANT, q[0] + 0.5, y + 1.2, q[2] + 0.5, 30, 0.4, 0.8, 0.4, 0.6);
            sl.playSound(null, q[0] + 0.5, y, q[2] + 0.5, DarkCraftMod.SFX_ERUDITO_ALZARSE.get(), SoundSource.HOSTILE, 1.4f, 0.9f);
            suyos.add(e);
            salen++;
        }
        maestrosAlzados += salen;
        DarkCraftMod.LOGGER.info("[DarkCraft] La Rectora llama a sus maestros: salen {} ({} jugadores)", salen, cerca.size());
    }

    private static double lejania(int[] q, List<ServerPlayer> jugadores) {
        double m = Double.MAX_VALUE;
        for (ServerPlayer p : jugadores) m = Math.min(m, Math.hypot(p.getX() - (q[0] + 0.5), p.getZ() - (q[2] + 0.5)));
        return m;
    }

    private void despedir() {
        for (Erudito e : suyos) if (e.isAlive() && !e.isRemoved()) e.disolver();
        suyos.clear();
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

    /** "Lee las estrellas... si puedes": la primera vez de cada pelea que hace caer las estrellas o dibuja la constelacion. */
    private void hablaPrimera() {
        if (cuatro) return;
        cuatro = true;
        hablaSiCalla(4);
    }

    @Override
    protected void alEmpezarPelea() {
        llamo = cuatro = false;
        orbesEspera = 30;
        estrellasEspera = 90;
        constelacionEspera = 140;
        parpadeoEspera = 40;
        repetido = 0;
        segundaLluvia = -1;
        long ahora = level().getGameTime();
        if (ahora - ultimaEntrada > 20 * 60 * 4 || ahora < ultimaEntrada) {
            ultimaEntrada = ahora;
            habla(1);
            // baja la vista del telescopio mientras lo dice y no grita ni ataca hasta acabarlo
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
        despedir();
        estrellas.clear();
        constelaciones.clear();
        esperaRugido = 0;
        segundaLluvia = -1;
    }

    /** A media vida no grita: se alza y llama a sus maestros (y desde entonces lee mas deprisa). */
    @Override
    protected void alSegundaMitad() {
        empezar(RectoraAnims.ALZARSE);
        pausa = Math.max(pausa, 6);
        estrellasEspera = Math.min(estrellasEspera, 70);
        constelacionEspera = Math.min(constelacionEspera, 110);
    }

    @Override
    public void setTarget(LivingEntity t) {
        super.setTarget(t);
        if (t != null && !level().isClientSide) ultimaPresa = t;
    }

    /** Solo repara en quien esta dentro de la rotonda (los del vestibulo, tras la puerta, no le importan). */
    @Override
    protected boolean percibe(LivingEntity p) {
        return Observatorio.enRotonda(p.getX(), p.getZ()) && super.percibe(p);
    }

    @Override
    protected boolean objetivoValido(LivingEntity e) {
        if (e instanceof Player && enPelea() && Observatorio.enRotonda(getX(), getZ()) && !Observatorio.enRotonda(e.getX(), e.getZ())) return false;
        return super.objetivoValido(e);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            if (!isAlive() && deathTime > RectoraAnims.TICKS[RectoraAnims.MORIR]) return;
            // las estrellas que la rodean, la luz de la aureola y, leyendo el cielo, las letras que suben hacia el telescopio
            if (tickCount % 4 == 0)
                level().addParticle(ParticleTypes.END_ROD, getX() + (random.nextDouble() - 0.5) * 1.6, getY() + 0.4 + random.nextDouble() * 3.0,
                        getZ() + (random.nextDouble() - 0.5) * 1.6, 0, 0.01, 0);
            if (tickCount % 7 == 0)
                level().addParticle(LUZ, getX() + (random.nextDouble() - 0.5) * 1.2, getY() + 2.6 + random.nextDouble() * 0.8,
                        getZ() + (random.nextDouble() - 0.5) * 1.2, 0, 0.02, 0);
            if (postura() != DE_PIE && tickCount % 3 == 0)
                level().addParticle(ParticleTypes.ENCHANT, getX() + (random.nextDouble() - 0.5) * 0.8, getY() + 2.2 + random.nextDouble() * 0.6,
                        getZ() + (random.nextDouble() - 0.5) * 0.8, 0, 0.6, 0);
            return;
        }
        if (!isAlive()) return;
        if (orbesEspera > 0) orbesEspera--;
        if (estrellasEspera > 0) estrellasEspera--;
        if (constelacionEspera > 0) constelacionEspera--;
        if (parpadeoEspera > 0) parpadeoEspera--;
        if (level() instanceof ServerLevel sl) {
            if (!estrellas.isEmpty() || segundaLluvia > 0) tickEstrellas(sl);
            if (!constelaciones.isEmpty()) tickConstelaciones(sl);
        }
        // leyendo el cielo: el zumbido de la cupula y sus campanillas de vez en cuando (se le oye desde la puerta)
        if (!enPelea() && postura() == REZA && --espera <= 0) {
            espera = 220 + random.nextInt(80);
            playSound(DarkCraftMod.SFX_RECTORA_ESPERA.get(), 1.6f, 1.0f);
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
        estrellas.clear();
        constelaciones.clear();
        segundaLluvia = -1;
        super.die(s);
        if (level().isClientSide || ya) return;
        hablaHasta = 0;
        habla(5);
        despedir();
        if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2.4, getZ(), 50, 1.0, 1.4, 1.0, 0.05);
    }

    /** Al caer no se va en humo: se hunde, dice su ultima frase, mira arriba una vez mas y se deshace en estrellas que suben. */
    @Override
    protected void tickDeath() {
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        int fin = RectoraAnims.TICKS[RectoraAnims.MORIR] + 40;
        if (level() instanceof ServerLevel sl) {
            if (deathTime % 4 == 0 && deathTime < fin - 6)
                sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.6, getZ(), 3, 0.5, 0.8, 0.5, 0.03);
            if (deathTime > fin - 30 && deathTime % 2 == 0)
                sl.sendParticles(LUZ, getX(), getY() + 1.0 + (deathTime - (fin - 30)) * 0.25, getZ(), 6, 0.4, 0.4, 0.4, 0.0);
            if (deathTime == fin - 2) {
                // las estrellas suben por la rendija
                for (int k = 0; k < 6; k++)
                    sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2.0 + k * 2.5, getZ(), 30, 0.6 - k * 0.08, 1.0, 0.6 - k * 0.08, 0.08);
                sl.sendParticles(ParticleTypes.FIREWORK, getX(), getY() + 2.0, getZ(), 60, 0.8, 1.6, 0.8, 0.1);
                sl.sendParticles(LUZ, getX(), getY() + 2.0, getZ(), 80, 0.8, 1.6, 0.8, 0.0);
                sl.playSound(null, getX(), getY() + 1, getZ(), DarkCraftMod.SFX_RECTORA_PARPADEO.get(), SoundSource.HOSTILE, 2.0f, 0.6f);
            }
        }
        if (deathTime >= fin) remove(RemovalReason.KILLED);
    }

    @Override
    public void remove(RemovalReason r) {
        if (!level().isClientSide) despedir();
        super.remove(r);
    }

    // ------------------------------------------------------------------ la magia le hace la mitad; el fuego, mas

    @Override
    public boolean fireImmune() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        if (s.is(DamageTypes.MAGIC) || s.is(DamageTypes.INDIRECT_MAGIC)) cuanto *= 0.5f;
        if (s.is(DamageTypeTags.IS_FIRE)) cuanto *= 1.4f;
        boolean ok = super.hurt(s, cuanto);
        // tras un golpe, a veces se aparta
        if (ok && !quieta && !level().isClientSide && isAlive() && enPelea() && accion() == 0 && parpadeoEspera <= 0 && getTarget() != null
                && random.nextFloat() < 0.25f) empezar(RectoraAnims.PARPADEO);
        return ok;
    }

    /** Mientras dice su primera frase se queda donde esta (bajando la vista); despues, a media distancia: lee de lejos. */
    @Override
    protected double distanciaDeseada() {
        return enPelea() && tickCount < esperaRugido ? 99.0 : 6.5;
    }

    @Override
    protected double prisa() {
        return segunda ? 1.15 : 1.0;
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (accion) {
            case RectoraAnims.GRITO, RectoraAnims.ALZARSE -> 6;
            case RectoraAnims.PARPADEO -> 4;
            case RectoraAnims.ESTRELLAS, RectoraAnims.CONSTELACION -> (segunda ? 8 : 14) + random.nextInt(10);
            case RectoraAnims.ORBES -> (segunda ? 10 : 16) + random.nextInt(10);
            default -> (segunda ? 8 : 12) + random.nextInt(12);
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

    /** Flota: ni pasos ni dano al caer. */
    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
    }

    @Override
    public boolean causeFallDamage(float d, float m, DamageSource s) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_RECTORA_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_RECTORA_MUERTE.get();
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_3.get(), 3));
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_2.get(), 2));
        spawnAtLocation(new ItemStack(DarkCraftMod.CENIZAS.get(com.darkcraft.combate.Habilidad.RELAMPAGO).get()));
    }

    /** Para las pruebas y el comando: como esta. */
    public String estado() {
        return String.format(java.util.Locale.ROOT, "vida %.0f/%.0f, %s, %s en %.1f,%.1f,%.1f; baculo %d, orbes %d, lluvias %d (estrellas %d, dan %d), "
                        + "constelaciones %d (dan %d), parpadeos %d, maestros %d%s",
                getHealth(), getMaxHealth(), enPelea() ? "peleando" : "esperando", postura() == DE_PIE ? "de pie" : "leyendo el cielo",
                getX(), getY(), getZ(), baculos, orbes, lluvias, estrellasCaidas, estrellasQueDan, constelacionesHechas, lineasQueDan, parpadeos,
                maestrosAlzados, segunda ? " (a media vida)" : "");
    }
}
