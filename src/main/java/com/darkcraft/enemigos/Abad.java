package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.EfectosRaiz;
import com.darkcraft.morvain.Ermita;
import com.darkcraft.net.Net;
import com.darkcraft.rey.Voces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
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
 * Hrodric, el Abad de la Cumbre (parte 7l): el jefe de las Montanas Nevadas. El abad del Monasterio de la Helada que subio
 * a la cumbre a pedirle al frio que perdonara a los suyos, y el frio le contesto (libro perdido 21). Espera <b>de rodillas</b>
 * en medio del calvario de la Ermita de la Cumbre ({@code morvain/Ermita}), de cara a la ermita, rezando (se le oye desde la
 * escalera): "y no te arrodilles nunca en esta casa".
 * <ul>
 * <li>al ver a alguien se levanta mientras dice su frase y al acabarla ruge;</li>
 * <li>el <b>baculazo</b>: alza el baculo con las dos manos y lo estrella delante; del golpe brota una fila de <b>puas de
 * hielo</b> por el suelo hasta lejos (a media vida, tres filas en abanico); de lado se libra;</li>
 * <li>el <b>barrido</b> a media altura (no se salta: se rueda o se aleja uno);</li>
 * <li>el <b>pisoton</b>, a quien se le pega o se le pone detras: dos anillos de puas a su alrededor;</li>
 * <li>el <b>aliento de la cumbre</b>: un soplo blanco de nueve bloques que barre delante de su derecha a su izquierda: hiela
 * y frena;</li>
 * <li>los <b>carambanos</b>: alza el baculo al cielo y donde esta cada uno (y en algun sitio mas del calvario) sale una marca
 * de escarcha en el suelo; un segundo despues cae alli un carambano enorme y se rompe (a media vida, dos tandas);</li>
 * <li>a <b>media vida</b> grita: "¡hermanos, alzaos de la nieve!": se alzan helados de la nieve del calvario (dos, uno mas
 * por cada amigo, hasta cuatro) y se levanta la <b>ventisca</b>: mientras dura, quien no este junto a un brasero se va
 * helando (y helado del todo, le duele); junto al fuego, se deshiela. Vuelve cada poco mientras dura la pelea.</li>
 * </ul>
 * El frio no le hace nada; el fuego, un 50 % mas. Habla (ElevenLabs, en ingles con subtitulos; {@link Voces#BASE_ABAD}):
 * 1 al empezar (no la repite en 4 min), 2 al llamar a sus hermanos, 3 si cae su presa, 4 la primera vez que alguien se
 * hiela del todo y 5 al morir. Al caer se queda de rodillas apoyado en el baculo, por fin en paz, y se rompe en hielo; lo
 * demas (el cartel con sus ecos, la hazana {@code abad} y el bestiario) lo reparte {@code valdrenn/Guardianes}; y los
 * hermanos de la iglesia del monasterio descansan ({@code morvain/Ermita#liberar}).
 */
public class Abad extends Guardian {
    public static final int ECOS = 11000;
    public static final int VOZ_BASE = Voces.BASE_ABAD;
    /** La ventisca: lo que dura y lo que tarda en volver; lo cerca que hay que estar de un brasero para no helarse. */
    public static final int VENTISCA = 20 * 18, VENTISCA_PAUSA = 20 * 16;
    public static final double CALOR = 3.6;
    /** Lo que hiela la ventisca cada tick (bajan 2 solos) y su dano helado del todo (cada 30 ticks, por el golpe). */
    public static final int FRIO_VENTISCA = 6;
    /** El aliento: hasta donde llega, su abertura (grados) y lo que hiela cada tick. */
    public static final double ALIENTO_ALCANCE = 9.0, ALIENTO_ARCO = 44;
    public static final int FRIO_ALIENTO = 16;
    /** Los carambanos: lo que tardan en caer desde que sale la marca y su radio. */
    public static final int CARAMBANO_AVISO = 22;
    public static final double CARAMBANO_RADIO = 1.7;
    /** Las puas: hasta donde llega la fila del baculazo y lo que hay entre una y otra. */
    public static final double PUAS_MAX = 12.0, PUAS_PASO = 1.5;

    static final DustParticleOptions ESCARCHA = new DustParticleOptions(new Vector3f(0.86f, 0.95f, 1f), 1.4f),
            VAHO = new DustParticleOptions(new Vector3f(0.62f, 0.86f, 0.98f), 2.0f),
            MARCA = new DustParticleOptions(new Vector3f(0.75f, 0.92f, 1f), 1.1f);
    static final BlockParticleOption TROZOS = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()),
            NIEVE = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SNOW_BLOCK.defaultBlockState());

    /** La ultima vez que dijo su frase de entrada (tiempo del mundo): no la repite en cada intento. */
    private static long ultimaEntrada = Long.MIN_VALUE / 2;

    private int alientoEspera = 60, carambanosEspera = 90, cercaEspera, baculazoLejosEspera = 60, hablaHasta, esperaRugido, repetido, ultimo,
            rezo = 40, ventiscaHasta, ventiscaSiguiente, frioEspera;
    private boolean llamo, heladoDicho;
    private LivingEntity ultimaPresa;
    private final List<Helado> suyos = new ArrayList<>();
    private final Set<Integer> alientoTocados = new HashSet<>();

    /** Una pua de hielo que brota (el dano cuando acaba de salir) o un carambano que cae (el dano al llegar). */
    private record Pua(double x, double y, double z, int cuando, boolean carambano) {}

    private final List<Pua> puas = new ArrayList<>();
    /** Para las pruebas. */
    private int baculazos, puasHechas, puasQueDan, pisotones, alientos, heladosAliento, carambanos, carambanosQueDan, ventiscas, heladosVentisca,
            deshelados, alzados;

    public Abad(EntityType<? extends Abad> t, Level l) {
        super(t, l, "abad", BossEvent.BossBarColor.WHITE);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1100.0).add(Attributes.ATTACK_DAMAGE, 14.0).add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 5.0).add(Attributes.MOVEMENT_SPEED, 0.24).add(Attributes.FOLLOW_RANGE, 34.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public String tipo() {
        return "abad";
    }

    @Override
    public int musica() {
        return 36;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return "abad";
    }

    @Override
    protected int[] ticks() {
        return AbadAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return AbadAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return AbadAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return AbadAnims.MORIR;
    }

    @Override
    protected int accionRugido() {
        return AbadAnims.GRITO;
    }

    @Override
    protected int accionLevantarse() {
        return AbadAnims.LEVANTARSE;
    }

    // ------------------------------------------------------------------ para las pruebas

    public int baculazos() {
        return baculazos;
    }

    public int puas() {
        return puasHechas;
    }

    /** A cuantos ha dado una pua (del baculazo o del pisoton). */
    public int puasQueDan() {
        return puasQueDan;
    }

    public int pisotones() {
        return pisotones;
    }

    public int alientos() {
        return alientos;
    }

    public int heladosAliento() {
        return heladosAliento;
    }

    public int carambanos() {
        return carambanos;
    }

    public int carambanosQueDan() {
        return carambanosQueDan;
    }

    public int ventiscas() {
        return ventiscas;
    }

    /** A cuantos ha helado del todo la ventisca (y les ha dolido). */
    public int heladosVentisca() {
        return heladosVentisca;
    }

    /** Cuantas veces se ha deshelado alguien junto a un brasero con la ventisca. */
    public int deshelados() {
        return deshelados;
    }

    public int alzados() {
        return alzados;
    }

    /** Cuantos de los que ha alzado de la nieve siguen en pie. */
    public int suyos() {
        int n = 0;
        for (Helado h : suyos) if (h.isAlive() && !h.isRemoved()) n++;
        return n;
    }

    public boolean ventisca() {
        return tickCount < ventiscaHasta;
    }

    /** Cuantos carambanos y puas estan por caer o brotar. */
    public int pendientes() {
        return puas.size();
    }

    /** Para las pruebas: pasa ya a media vida (grita, alza a sus hermanos y la ventisca), sin esperar a que acabe lo que hace. */
    public void mediaVida() {
        if (level().isClientSide || !isAlive()) return;
        setHealth(Math.min(getHealth(), getMaxHealth() * 0.45f));
        segunda = true;
        alSegundaMitad();
        empezar(AbadAnims.GRITO);
    }

    /** Para las pruebas: la ventisca ya (o la quita). */
    public void ventisca(boolean si) {
        if (si) empezarVentisca();
        else {
            ventiscaHasta = tickCount;
            ventiscaSiguiente = tickCount + VENTISCA_PAUSA;
        }
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
        // quien se le pega o se le pone detras: el pisoton (anillos de puas)
        if (cercaEspera <= 0 && (d < 3.6 && detras(presa) || d < 2.4)) {
            cercaEspera = 50 + random.nextInt(30);
            return AbadAnims.PISOTON;
        }
        // los carambanos: a quien se aleja, y de vez en cuando a cualquiera
        if (carambanosEspera <= 0 && (d > 7.0 || random.nextFloat() < 0.25f)) {
            carambanosEspera = (segunda ? 150 : 230) + random.nextInt(60);
            return AbadAnims.CARAMBANOS;
        }
        // el aliento: a quien esta a tiro de el
        if (alientoEspera <= 0 && d < ALIENTO_ALCANCE - 1 && random.nextFloat() < (segunda ? 0.45f : 0.32f)) {
            alientoEspera = (segunda ? 150 : 210) + random.nextInt(60);
            return AbadAnims.ALIENTO;
        }
        // de lejos (a tiro de las puas): el baculazo, que manda la fila hasta alli
        if (d > 6.0 && d < PUAS_MAX && baculazoLejosEspera <= 0 && random.nextFloat() < 0.4f) {
            baculazoLejosEspera = 90 + random.nextInt(60);
            return AbadAnims.BACULAZO;
        }
        if (d > 5.6) return 0;
        // de cerca: el baculazo o el barrido (no el mismo tres veces seguidas)
        int a = random.nextFloat() < 0.45f ? AbadAnims.BACULAZO : AbadAnims.BARRIDO;
        if (a == ultimo) {
            if (++repetido >= 2) {
                a = a == AbadAnims.BACULAZO ? AbadAnims.BARRIDO : AbadAnims.BACULAZO;
                repetido = 0;
            }
        } else repetido = 0;
        ultimo = a;
        return a;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case AbadAnims.BACULAZO -> new Golpe(5.4f, 50f, 1.8f, 1.0f);
            case AbadAnims.BARRIDO -> new Golpe(5.2f, 230f, 1.25f, 1.1f);
            case AbadAnims.PISOTON -> new Golpe(3.4f, 360f, 0.9f, 1.2f);
            default -> null;
        };
    }

    /** Lo que da el baculo hiela; el barrido, ademas, frena. */
    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        Helado.helar(le, accion == AbadAnims.BARRIDO ? 70 : 90);
        if (accion == AbadAnims.BARRIDO) le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1), this);
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = AbadAnims.GOLPES[accion];
        switch (accion) {
            case AbadAnims.BACULAZO -> {
                if (t == g[0] - 1) empujar(0.3);
                if (t == g[0]) {
                    playSound(DarkCraftMod.SFX_ABAD_BACULAZO.get(), 2.2f, 0.92f + random.nextFloat() * 0.12f);
                    baculazos++;
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    if (level() instanceof ServerLevel sl) {
                        double x = getX() + d.x * 4.4, z = getZ() + d.z * 4.4;
                        sl.sendParticles(TROZOS, x, getY() + 0.3, z, 40, 0.7, 0.2, 0.7, 0.2);
                        sl.sendParticles(NIEVE, x, getY() + 0.3, z, 30, 0.9, 0.2, 0.9, 0.15);
                        sl.sendParticles(ParticleTypes.SNOWFLAKE, x, getY() + 0.6, z, 26, 1.2, 0.4, 1.2, 0.08);
                    }
                    filaDePuas(0);
                    if (segunda) {
                        filaDePuas(-24);
                        filaDePuas(24);
                    }
                }
            }
            case AbadAnims.BARRIDO -> {
                if (t == g[0] - 5) playSound(DarkCraftMod.SFX_ABAD_BARRIDO.get(), 2.0f, 0.9f + random.nextFloat() * 0.12f);
                if (t == g[0] - 1) empujar(0.25);
                if (t == g[0] && level() instanceof ServerLevel sl)
                    for (int k = 0; k <= 12; k++) {
                        double a = Math.toRadians(getYRot() + 90 - 115 + 230 * k / 12.0);
                        double x = getX() + Math.cos(a) * 4.6, z = getZ() + Math.sin(a) * 4.6;
                        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, x, getY() + 1.6, z, 1, 0, 0, 0, 0);
                        if (k % 2 == 0) sl.sendParticles(ParticleTypes.SNOWFLAKE, x, getY() + 1.4, z, 6, 0.3, 0.2, 0.3, 0.05);
                    }
            }
            case AbadAnims.PISOTON -> {
                if (t == g[0]) {
                    playSound(DarkCraftMod.SFX_ABAD_PISOTON.get(), 2.2f, 0.9f + random.nextFloat() * 0.12f);
                    pisotones++;
                    anilloDePuas();
                }
            }
            case AbadAnims.ALIENTO -> {
                if (t == 10) {
                    playSound(DarkCraftMod.SFX_ABAD_ALIENTO.get(), 2.2f, 0.9f + random.nextFloat() * 0.1f);
                    alientoTocados.clear();
                    alientos++;
                }
                if (t >= g[0] && t <= 48) aliento(t);
            }
            case AbadAnims.CARAMBANOS -> {
                if (t == 6) playSound(DarkCraftMod.SFX_ABAD_INVOCAR.get(), 2.4f, 1.0f);
                if (t == g[0]) marcar(true);
                // a media vida, otra tanda donde esten ahora
                if (t == g[0] + 16 && segunda) marcar(false);
                if (t >= 10 && t <= g[0] && level() instanceof ServerLevel sl && t % 3 == 0) {
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    sl.sendParticles(ParticleTypes.SNOWFLAKE, getX() + d.x * 0.6, getY() + getBbHeight() + 2.0, getZ() + d.z * 0.6, 8, 0.5, 0.8, 0.5, 0.05);
                    sl.sendParticles(ParticleTypes.END_ROD, getX() + d.x * 0.6, getY() + getBbHeight() + 2.2, getZ() + d.z * 0.6, 2, 0.2, 0.3, 0.2, 0.02);
                }
            }
            case AbadAnims.GRITO -> {
                if (t == 8) {
                    playSound(DarkCraftMod.SFX_ABAD_GRITO.get(), 3.0f, 1.0f);
                    if (level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + getBbHeight() * 0.8, getZ(), 60, 3.0, 1.2, 3.0, 0.12);
                        sl.sendParticles(VAHO, getX(), getY() + getBbHeight() * 0.85, getZ(), 30, 1.2, 0.6, 1.2, 0.04);
                    }
                }
                // la de media vida: "hermanos, alzaos de la nieve" — se alzan los helados y se levanta la ventisca
                if (t == 16 && segunda && !llamo) {
                    llamo = true;
                    habla(2);
                    llamar();
                    empezarVentisca();
                }
            }
            case AbadAnims.LEVANTARSE -> {
                if (t == 3 && level() instanceof ServerLevel sl) {
                    sl.sendParticles(TROZOS, getX(), getY() + 1.4, getZ(), 50, 0.9, 1.0, 0.9, 0.15);
                    sl.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 2.0, getZ(), 30, 1.0, 1.2, 1.0, 0.05);
                    playSound(DarkCraftMod.SFX_HELADO_ROMPER.get(), 2.0f, 0.6f);
                }
            }
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ las puas y los carambanos

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

    /** Del golpe del baculo, una fila de puas de hielo por el suelo hacia delante (desviada {@code grados}), una tras otra. */
    private void filaDePuas(float grados) {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot() + grados);
        float yaw = (float) Math.toRadians(-(getYRot() + grados));
        int k = 0;
        for (double s = 4.4; s <= PUAS_MAX; s += PUAS_PASO, k++) {
            double x = getX() + d.x * s, z = getZ() + d.z * s;
            double y = suelo(sl, x, z);
            if (Double.isNaN(y)) break;
            BlockState st = k % 2 == 0 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.BLUE_ICE.defaultBlockState();
            EfectosRaiz.estaca(sl, st, x, y, z, yaw + (random.nextFloat() - 0.5f) * 0.8f, 0.3f * (random.nextFloat() - 0.5f),
                    2.0f + random.nextFloat() * 0.8f + k * 0.1f, 0.5f + random.nextFloat() * 0.15f, k * 2, 22);
            puas.add(new Pua(x, y, z, tickCount + k * 2 + 2, false));
            puasHechas++;
        }
    }

    /** El pisoton: dos anillos de puas a su alrededor (el de dentro primero). */
    private void anilloDePuas() {
        if (!(level() instanceof ServerLevel sl)) return;
        sl.sendParticles(TROZOS, getX(), getY() + 0.2, getZ(), 40, 1.5, 0.1, 1.5, 0.2);
        sl.sendParticles(NIEVE, getX(), getY() + 0.2, getZ(), 30, 1.8, 0.1, 1.8, 0.15);
        double giro = random.nextDouble() * Math.PI * 2;
        for (int anillo = 0; anillo < 2; anillo++) {
            double r = anillo == 0 ? 2.6 : 4.3;
            int n = anillo == 0 ? 8 : 13;
            for (int k = 0; k < n; k++) {
                double a = giro + k * Math.PI * 2 / n + anillo * 0.2;
                double x = getX() + Math.cos(a) * r, z = getZ() + Math.sin(a) * r;
                double y = suelo(sl, x, z);
                if (Double.isNaN(y)) continue;
                BlockState st = (k + anillo) % 2 == 0 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.BLUE_ICE.defaultBlockState();
                // inclinadas hacia fuera
                float yaw = (float) Math.atan2(Math.cos(a), Math.sin(a));
                EfectosRaiz.estaca(sl, st, x, y, z, yaw, 0.35f, 1.6f + random.nextFloat() * 0.6f, 0.45f + random.nextFloat() * 0.1f, anillo * 4, 20);
                puas.add(new Pua(x, y, z, tickCount + anillo * 4 + 2, false));
                puasHechas++;
            }
        }
    }

    /**
     * Los carambanos: una marca de escarcha bajo cada uno de los que pelean (a 26 bloques) y, la primera tanda, en dos o tres
     * sitios mas del calvario; {@link #CARAMBANO_AVISO} ticks despues cae alli un carambano enorme.
     */
    private void marcar(boolean otros) {
        if (!(level() instanceof ServerLevel sl)) return;
        List<double[]> donde = new ArrayList<>();
        for (Player p : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(26, 8, 26)))
            if (objetivoValido(p) && p.isAlive()) donde.add(new double[]{p.getX(), p.getZ()});
        LivingEntity t = getTarget();
        if (t != null && !(t instanceof Player) && t.isAlive()) donde.add(new double[]{t.getX(), t.getZ()});
        if (otros) {
            int n = 2 + random.nextInt(2);
            for (int k = 0; k < n; k++) {
                double a = random.nextDouble() * Math.PI * 2, r = 4 + random.nextDouble() * 9;
                donde.add(new double[]{getX() + Math.cos(a) * r, getZ() + Math.sin(a) * r});
            }
        }
        for (double[] q : donde) {
            double y = suelo(sl, q[0], q[1]);
            if (Double.isNaN(y)) continue;
            puas.add(new Pua(q[0], y, q[1], tickCount + CARAMBANO_AVISO, true));
            EfectosRaiz.carambano(sl, q[0], y, q[1], 0.9f + random.nextFloat() * 0.3f, 3.2f + random.nextFloat() * 1.2f, CARAMBANO_AVISO - 7, 7);
            carambanos++;
        }
    }

    /** Lo que brota y lo que cae: el dano en su momento; las marcas de los carambanos mientras esperan. */
    private void puasYCarambanos(ServerLevel sl) {
        float base = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        Iterator<Pua> it = puas.iterator();
        while (it.hasNext()) {
            Pua p = it.next();
            if (tickCount < p.cuando()) {
                // la marca del carambano: un anillo de escarcha en el suelo
                if (p.carambano() && (p.cuando() - tickCount) % 3 == 0) {
                    for (int k = 0; k < 10; k++) {
                        double a = k * Math.PI * 2 / 10 + tickCount * 0.15;
                        sl.sendParticles(MARCA, p.x() + Math.cos(a) * CARAMBANO_RADIO * 0.85, p.y() + 0.1, p.z() + Math.sin(a) * CARAMBANO_RADIO * 0.85,
                                1, 0.02, 0.0, 0.02, 0.0);
                    }
                    sl.sendParticles(ParticleTypes.SNOWFLAKE, p.x(), p.y() + 0.2, p.z(), 2, 0.4, 0.05, 0.4, 0.01);
                }
                continue;
            }
            it.remove();
            double r = p.carambano() ? CARAMBANO_RADIO : 0.95;
            if (p.carambano()) {
                sl.sendParticles(TROZOS, p.x(), p.y() + 0.6, p.z(), 40, 0.6, 0.4, 0.6, 0.2);
                sl.sendParticles(ParticleTypes.SNOWFLAKE, p.x(), p.y() + 0.8, p.z(), 18, 0.8, 0.5, 0.8, 0.06);
                sl.playSound(null, p.x(), p.y(), p.z(), DarkCraftMod.SFX_ABAD_CARAMBANO.get(), SoundSource.HOSTILE, 1.8f, 0.85f + random.nextFloat() * 0.3f);
            } else sl.sendParticles(TROZOS, p.x(), p.y() + 0.5, p.z(), 8, 0.25, 0.4, 0.25, 0.08);
            for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, new AABB(p.x() - r - 0.5, p.y() - 0.5, p.z() - r - 0.5, p.x() + r + 0.5,
                    p.y() + 2.4, p.z() + r + 0.5), e -> e != this && e.isAlive() && objetivoValido(e))) {
                if (le.distanceToSqr(p.x(), le.getY(), p.z()) > r * r) continue;
                if (le.hurt(damageSources().mobAttack(this), base * (p.carambano() ? 1.2f : 0.95f))) {
                    Helado.helar(le, p.carambano() ? 110 : 80);
                    if (p.carambano()) {
                        le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1), this);
                        carambanosQueDan++;
                    } else {
                        le.setDeltaMovement(le.getDeltaMovement().add(0, 0.5, 0));
                        le.hurtMarked = true;
                        puasQueDan++;
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------ el aliento

    /** El soplo de la cumbre: barre de su derecha a su izquierda (t de 16 a 48); hiela y, la primera vez, duele y frena. */
    private void aliento(int t) {
        if (!(level() instanceof ServerLevel sl)) return;
        int g = AbadAnims.GOLPES[AbadAnims.ALIENTO][0];
        float barrido = Mth.lerp(Mth.clamp((t - g) / (float) (48 - g), 0f, 1f), 30f, -30f);
        Vec3 d = Vec3.directionFromRotation(0, getYRot() + barrido);
        double boca = getY() + getBbHeight() * 0.78;
        for (int k = 0; k < 7; k++) {
            double s = 1.0 + k * 1.25 + random.nextDouble() * 0.5;
            double ab = s * 0.26;
            sl.sendParticles(ParticleTypes.SNOWFLAKE, getX() + d.x * s, boca - s * 0.28, getZ() + d.z * s, 3, ab, ab * 0.5, ab, 0.02);
            if (k % 2 == 0) sl.sendParticles(VAHO, getX() + d.x * s, boca - s * 0.28, getZ() + d.z * s, 2, ab, ab * 0.4, ab, 0.0);
            if (k % 3 == 0) sl.sendParticles(ParticleTypes.CLOUD, getX() + d.x * s, boca - s * 0.28, getZ() + d.z * s, 1, ab * 0.5, ab * 0.3, ab * 0.5, 0.01);
        }
        double cosMin = Math.cos(Math.toRadians(ALIENTO_ARCO / 2));
        float base = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(ALIENTO_ALCANCE + 1, 3.0, ALIENTO_ALCANCE + 1),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            double dx = le.getX() - getX(), dz = le.getZ() - getZ(), l = Math.sqrt(dx * dx + dz * dz);
            if (l > ALIENTO_ALCANCE + le.getBbWidth() * 0.5) continue;
            if (l > 1.0 && (dx * d.x + dz * d.z) / l < cosMin) continue;
            if (le.getY() > getY() + getBbHeight() || le.getY() + le.getBbHeight() < getY() - 1.5) continue;
            Helado.helar(le, FRIO_ALIENTO);
            if (alientoTocados.add(le.getId()) && le.hurt(Helado.frio(level(), this, this), base * 0.55f)) {
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1), this);
                heladosAliento++;
            }
        }
    }

    // ------------------------------------------------------------------ la ventisca y los braseros

    private void empezarVentisca() {
        ventiscaHasta = tickCount + VENTISCA;
        ventiscaSiguiente = ventiscaHasta + VENTISCA_PAUSA;
        ventiscas++;
        if (level() instanceof ServerLevel sl)
            for (ServerPlayer p : sl.players())
                if (p.distanceToSqr(this) < 60 * 60)
                    sl.playSound(null, p.getX(), p.getY() + 1, p.getZ(), DarkCraftMod.SFX_ABAD_VENTISCA.get(), SoundSource.HOSTILE, 1.6f, 0.95f + random.nextFloat() * 0.1f);
        DarkCraftMod.LOGGER.info("[DarkCraft] El Abad levanta la ventisca ({} s)", VENTISCA / 20);
    }

    /** Si (x, y, z) esta junto a un brasero de la ermita (al calor del fuego). */
    public static boolean alCalor(double x, double y, double z) {
        for (BlockPos b : Ermita.get().braseros()) {
            double dx = x - (b.getX() + 0.5), dz = z - (b.getZ() + 0.5);
            if (dx * dx + dz * dz < CALOR * CALOR && Math.abs(y - b.getY()) < 3.5) return true;
        }
        return false;
    }

    private void tickVentisca(ServerLevel sl) {
        if (!enPelea()) return;
        if (!ventisca()) {
            if (segunda && tickCount >= ventiscaSiguiente && accion() == 0) empezarVentisca();
            return;
        }
        // el fuego de los braseros: un corro de llamas en el suelo hasta donde llega el calor
        if (tickCount % 6 == 0)
            for (BlockPos b : Ermita.get().braseros()) {
                double a0 = random.nextDouble() * Math.PI * 2;
                for (int k = 0; k < 6; k++) {
                    double a = a0 + k * Math.PI / 3;
                    sl.sendParticles(ParticleTypes.SMALL_FLAME, b.getX() + 0.5 + Math.cos(a) * CALOR, b.getY() - 0.8, b.getZ() + 0.5 + Math.sin(a) * CALOR,
                            1, 0.05, 0.0, 0.05, 0.002);
                }
            }
        for (ServerPlayer p : sl.players()) {
            if (!objetivoValido(p) || !p.isAlive() || p.distanceToSqr(this) > 40 * 40) continue;
            // la nieve que le azota (solo la ve el)
            sl.sendParticles(p, ParticleTypes.SNOWFLAKE, true, p.getX() - 3, p.getY() + 1.6, p.getZ(), 14, 5.0, 2.0, 5.0, 0.12);
            if (tickCount % 2 == 0) sl.sendParticles(p, ESCARCHA, true, p.getX(), p.getY() + 1.2, p.getZ(), 8, 4.0, 1.6, 4.0, 0.0);
            frio(p);
        }
    }

    /** Un tick de ventisca para {@code le}: junto a un brasero se deshiela; si no, se va helando y, helado del todo, le duele. */
    private void frio(LivingEntity le) {
        if (alCalor(le.getX(), le.getY(), le.getZ())) {
            if (le.getTicksFrozen() > 0) {
                le.setTicksFrozen(Math.max(0, le.getTicksFrozen() - 6));
                if (tickCount % 20 == 0) deshelados++;
            }
            return;
        }
        Helado.helar(le, FRIO_VENTISCA);
        if (le.isFullyFrozen() && ++frioEspera % 30 == 0 && le.hurt(Helado.frio(level(), this, this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.3f)) {
            heladosVentisca++;
            if (!heladoDicho) {
                heladoDicho = true;
                hablaSiCalla(4);
            }
        }
    }

    /** Para las pruebas: un tick de la ventisca sobre {@code le} (a las pruebas del servidor no les llegan jugadores de verdad). */
    public void ventiscaSobre(LivingEntity le) {
        if (!level().isClientSide && ventisca()) frio(le);
    }

    // ------------------------------------------------------------------ sus hermanos: se alzan de la nieve

    private void llamar() {
        if (!(level() instanceof ServerLevel sl)) return;
        List<ServerPlayer> cerca = new ArrayList<>();
        for (ServerPlayer p : sl.players()) if (p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) < 40 * 40) cerca.add(p);
        int n = Math.max(2, Math.min(4, 1 + cerca.size()));
        int salen = 0;
        double giro = random.nextDouble() * Math.PI * 2;
        for (int k = 0; k < 24 && salen < n; k++) {
            double a = giro + k * (Math.PI * 2 / 7) + (k >= 7 ? 0.3 * (k / 7) : 0);
            double r = 7.0 + (k % 3) * 2.2;
            double cx = getX(), cz = getZ();
            // en el calvario, alrededor del centro si se esta en el
            if (Ermita.dentro(getX(), getZ())) {
                cx = Ermita.CX + 0.5;
                cz = Ermita.CZ + 0.5;
            }
            int bx = Mth.floor(cx + Math.cos(a) * r), bz = Mth.floor(cz + Math.sin(a) * r);
            double y = Puestos.suelo(sl, bx, Mth.floor(getY()) + 2, bz);
            if (Math.abs(y - getY()) > 2.6) continue;
            Helado h = DarkCraftMod.HELADO.get().create(sl);
            if (h == null) return;
            float mira = (float) Math.toDegrees(Math.atan2(getZ() - (bz + 0.5), getX() - (bx + 0.5))) - 90f;
            h.moveTo(bx + 0.5, y, bz + 0.5, mira, 0f);
            if (!sl.noCollision(h) || sl.containsAnyLiquid(h.getBoundingBox())) continue;
            boolean cercaDeAlguien = false;
            for (ServerPlayer p : cerca) if (p.distanceToSqr(h) < 3.0 * 3.0) cercaDeAlguien = true;
            if (cercaDeAlguien) continue;
            h.variante(salen % 3 == 0 ? Helado.PENITENTE : salen % 3 == 1 ? Helado.MONJE : Helado.PEREGRINO);
            // enterrados en la nieve: se alzan de golpe (sin puesto: no vuelven)
            h.asignar(null, -1, BlockPos.containing(bx + 0.5, y, bz + 0.5), mira, TUMBADO, 0);
            h.efimero(240);
            sl.addFreshEntity(h);
            LivingEntity t = getTarget() != null ? getTarget()
                    : sl.getNearestPlayer(h.getX(), h.getY(), h.getZ(), 30, e -> e instanceof Player q && objetivoValido(q));
            if (t != null) h.setTarget(t);
            sl.sendParticles(NIEVE, bx + 0.5, y + 0.3, bz + 0.5, 40, 0.5, 0.3, 0.5, 0.15);
            sl.sendParticles(ParticleTypes.SNOWFLAKE, bx + 0.5, y + 0.6, bz + 0.5, 20, 0.5, 0.6, 0.5, 0.05);
            sl.playSound(null, bx + 0.5, y, bz + 0.5, DarkCraftMod.SFX_HELADO_ROMPER.get(), SoundSource.HOSTILE, 1.4f, 0.75f + random.nextFloat() * 0.2f);
            suyos.add(h);
            salen++;
        }
        alzados += salen;
        DarkCraftMod.LOGGER.info("[DarkCraft] El Abad llama a sus hermanos: se alzan {} de la nieve ({} jugadores)", salen, cerca.size());
    }

    private void despedir() {
        for (Helado h : suyos) if (h.isAlive() && !h.isRemoved()) h.disolver();
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

    @Override
    protected void alEmpezarPelea() {
        llamo = heladoDicho = false;
        alientoEspera = 60;
        carambanosEspera = 90;
        baculazoLejosEspera = 60;
        cercaEspera = 0;
        repetido = ultimo = 0;
        ventiscaHasta = 0;
        ventiscaSiguiente = Integer.MAX_VALUE;
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
        puas.clear();
        esperaRugido = 0;
        ventiscaHasta = 0;
        ventiscaSiguiente = Integer.MAX_VALUE;
    }

    @Override
    protected void alSegundaMitad() {
        alientoEspera = Math.min(alientoEspera, 40);
        carambanosEspera = Math.min(carambanosEspera, 60);
    }

    @Override
    public void setTarget(LivingEntity t) {
        super.setTarget(t);
        if (t != null && !level().isClientSide) ultimaPresa = t;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            if (!isAlive()) return;
            Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
            // el vaho al respirar (de pie), la escarcha que se le cae y la luz del cristal del baculo
            if (postura() == DE_PIE && tickCount % 17 == 0)
                level().addParticle(VAHO, getX() + d.x * 0.6, getY() + getBbHeight() * 0.8, getZ() + d.z * 0.6, d.x * 0.03, 0.01, d.z * 0.03);
            if (tickCount % 5 == 0)
                level().addParticle(ESCARCHA, getX() + (random.nextDouble() - 0.5) * 1.4, getY() + 0.5 + random.nextDouble() * 3.6,
                        getZ() + (random.nextDouble() - 0.5) * 1.4, 0, -0.015, 0);
            if (postura() != DE_PIE && tickCount % 11 == 0)
                level().addParticle(ParticleTypes.SNOWFLAKE, getX() + (random.nextDouble() - 0.5) * 3, getY() + 4.5, getZ() + (random.nextDouble() - 0.5) * 3,
                        0, -0.04, 0);
            return;
        }
        if (!isAlive()) return;
        if (alientoEspera > 0) alientoEspera--;
        if (carambanosEspera > 0) carambanosEspera--;
        if (cercaEspera > 0) cercaEspera--;
        if (baculazoLejosEspera > 0) baculazoLejosEspera--;
        if (level() instanceof ServerLevel sl) {
            if (!puas.isEmpty()) puasYCarambanos(sl);
            tickVentisca(sl);
        }
        // esperando de rodillas: reza (se le oye desde la escalera)
        if (!enPelea() && postura() == REZA && --rezo <= 0) {
            rezo = 180 + random.nextInt(160);
            playSound(DarkCraftMod.SFX_ABAD_REZO.get(), 2.0f, 0.92f + random.nextFloat() * 0.12f);
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
        puas.clear();
        ventiscaHasta = 0;
        super.die(s);
        if (level().isClientSide || ya) return;
        habla(5);
        despedir();
        if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 2.4, getZ(), 50, 1.0, 1.4, 1.0, 0.05);
    }

    /** Al acabar de morir no se va en humo: de rodillas, apoyado en el baculo, se queda quieto y se rompe en hielo. */
    @Override
    protected void tickDeath() {
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        int fin = AbadAnims.TICKS[AbadAnims.MORIR] + 30;
        if (deathTime == fin - 2 && level() instanceof ServerLevel sl) {
            sl.sendParticles(TROZOS, getX(), getY() + 2.0, getZ(), 160, 1.0, 1.6, 1.0, 0.3);
            sl.sendParticles(NIEVE, getX(), getY() + 1.2, getZ(), 60, 1.2, 0.8, 1.2, 0.2);
            sl.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 2.6, getZ(), 60, 1.4, 1.8, 1.4, 0.08);
            sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2.8, getZ(), 20, 0.6, 1.2, 0.6, 0.04);
            sl.playSound(null, getX(), getY() + 2, getZ(), DarkCraftMod.SFX_HELADO_ROMPER.get(), SoundSource.HOSTILE, 2.6f, 0.55f);
        }
        if (deathTime >= fin) remove(RemovalReason.KILLED);
    }

    @Override
    public void remove(RemovalReason r) {
        if (!level().isClientSide) despedir();
        super.remove(r);
    }

    // ------------------------------------------------------------------ el frio no le hace nada; el fuego, mas

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        if (s.is(DamageTypeTags.IS_FREEZING)) return false;
        if (s.is(DamageTypeTags.IS_FIRE)) cuanto *= 1.5f;
        return super.hurt(s, cuanto);
    }

    @Override
    public boolean fireImmune() {
        return false;
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    /** Mientras dice su primera frase se queda donde esta (levantandose); despues, de cerca. */
    @Override
    protected double distanciaDeseada() {
        return enPelea() && tickCount < esperaRugido ? 99.0 : 4.2;
    }

    @Override
    protected double prisa() {
        return segunda ? 1.2 : 1.05;
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (accion) {
            case AbadAnims.GRITO -> 6;
            case AbadAnims.CARAMBANOS, AbadAnims.ALIENTO -> 12 + random.nextInt(10);
            case AbadAnims.PISOTON -> 8 + random.nextInt(8);
            default -> (segunda ? 10 : 16) + random.nextInt(12);
        };
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return accion == AbadAnims.BACULAZO ? DarkCraftMod.SFX_GOLPE_PESADO.get() : null;
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        SoundType t = s.getSoundType(level(), p, this);
        playSound(t.getStepSound(), t.getVolume() * 1.0f, t.getPitch() * 0.5f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_ABAD_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_ABAD_MUERTE.get();
    }

    @Override
    public boolean causeFallDamage(float d, float m, DamageSource s) {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_3.get(), 2));
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_2.get(), 2));
        spawnAtLocation(new ItemStack(DarkCraftMod.CENIZAS.get(com.darkcraft.combate.Habilidad.ESCARCHA).get()));
    }

    /** Para las pruebas y el comando: como esta. */
    public String estado() {
        return String.format(java.util.Locale.ROOT, "vida %.0f/%.0f, %s, %s en %.1f,%.1f,%.1f; baculazos %d, puas %d (dan %d), pisotones %d, alientos %d (hielan %d), "
                        + "carambanos %d (dan %d), ventiscas %d%s (helados %d, al calor %d), hermanos alzados %d",
                getHealth(), getMaxHealth(), enPelea() ? "peleando" : "esperando", postura() == DE_PIE ? "de pie" : "de rodillas rezando",
                getX(), getY(), getZ(), baculazos, puasHechas, puasQueDan, pisotones, alientos, heladosAliento, carambanos, carambanosQueDan, ventiscas,
                ventisca() ? " (ahora)" : "", heladosVentisca, deshelados, alzados);
    }
}
