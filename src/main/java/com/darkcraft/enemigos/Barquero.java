package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.net.Net;
import com.darkcraft.rey.Voces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * El Barquero de la Laguna Negra (parte 7j): el jefe del Pantano Podrido. El que la hermana Ilse oia remar bajo la capilla
 * ("si lees esto, no subas a su barca"). Espera de pie en su barca, amarrada junto al embarcadero de la Laguna Negra
 * ({@code morvain/LagunaNegra}), con la linterna de almas en alto.
 * <ul>
 * <li>al ver a alguien dice su frase y, al acabarla, ruge y <b>salta</b> de la barca al embarcadero (cae con el remo por
 * delante: lo de alrededor de donde cae y una ola por las tablas); despues salta tambien a quien se aleja;</li>
 * <li>el <b>remazo</b> (alza el remo y lo estrella de plano delante: una ola de agua negra; saltando o rodando se libra) y el
 * <b>barrido</b> a media altura (no se salta: se rueda o se aleja uno);</li>
 * <li>el <b>bichero</b>: clava el garfio del remo en quien esta a unos pasos (rodar o cubrirse libran) y tira de el hasta sus
 * pies, y enseguida el <b>golpe de linterna</b> (de cerca; quema), que es tambien lo que da a quien se le pega o se le pone
 * detras;</li>
 * <li>la <b>linterna</b>: la alza y el fuego de almas se abre en un <b>anillo</b> por las tablas (saltando se libra; a media
 * vida, dos anillos seguidos);</li>
 * <li>a <b>media vida</b> grita y <b>llama a sus pasajeros</b>: los podridos que siguen flotando en la laguna se alzan y
 * van a por los jugadores; si no hay bastantes (dos, uno mas por cada amigo, hasta cuatro), suben otros del agua al
 * embarcadero (esos son efimeros: se deshacen si la pelea se acaba).</li>
 * </ul>
 * Habla (ElevenLabs, en ingles con subtitulos; {@link Voces#BASE_BARQUERO}): 1 al empezar (no la repite en 4 min; hasta
 * acabarla no salta ni ataca), 2 al llamar a sus pasajeros, 3 si cae su presa, 4 la primera vez que engancha a alguien con
 * el bichero y 5 al morir. Esperando en su barca, rema de vez en cuando (se le oye). Al caer (lo reparte
 * {@code valdrenn/Guardianes}): el cartel con sus ecos, la hazana {@code barquero} y el bestiario; y la laguna queda en paz
 * ({@code morvain/LagunaNegra#liberar}).
 */
public class Barquero extends Guardian {
    public static final int ECOS = 9500;
    public static final int VOZ_BASE = Voces.BASE_BARQUERO;
    /** Los ticks del salto en que despega y en que cae (el golpe de la caida es el siguiente). */
    public static final int DESPEGA = 9, ATERRIZA = BarqueroAnims.GOLPES[BarqueroAnims.SALTO][0] - 1;
    /** Entre que distancias clava el bichero (lo que llega el remo), y lo lejos que salta como mucho. */
    public static final double BICHERO_MIN = 2.6, BICHERO_MAX = 6.8, SALTO_MAX = 12.0;
    /** El anillo de la linterna: lo que crece por tick y hasta donde llega. */
    public static final double ANILLO_VEL = 0.42, ANILLO_MAX = 9.0;

    /** La ultima vez que dijo su frase de entrada (tiempo del mundo): no la repite en cada intento. */
    private static long ultimaEntrada = Long.MIN_VALUE / 2;

    private int bicheroEspera = 40, saltoEspera, linternaEspera = 80, cercaEspera, hablaHasta, esperaRugido, repetido, ultimo, remo = 60;
    private boolean llamo, bicheroDicho;
    private LivingEntity ultimaPresa, enganchado;
    private final List<Podrido> suyos = new ArrayList<>();
    /** El salto: de donde a donde (null = no salta a ningun sitio: cae donde esta) y lo alto del arco. */
    private Vec3 saltoDe, saltoA;
    private double saltoAlto;
    private boolean volando;
    /** Acabada la pelea lejos de su barca: vuelve a ella de un salto (aqui cae). */
    private Vec3 volverA;

    /** Un anillo de fuego de almas que se abre por el suelo desde donde alzo la linterna. */
    private static final class Anillo {
        final double x, y, z;
        double r = 1.0;
        int espera;
        final Set<Integer> tocados = new HashSet<>();

        Anillo(double x, double y, double z, int espera) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.espera = espera;
        }
    }

    private final List<Anillo> anillos = new ArrayList<>();
    /** Para las pruebas: saltos (con sitio donde caer), enganches del bichero, golpes de linterna tras enganchar, ondas, anillos,
     * a quien ha quemado un anillo, los pasajeros que ha despertado y los que ha subido del agua. */
    private int saltos, enganches, rematados, ondas, anillosHechos, alcanzados, quemados, despertados, subidos;

    public Barquero(EntityType<? extends Barquero> t, Level l) {
        super(t, l, "barquero", BossEvent.BossBarColor.BLUE);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 900.0).add(Attributes.ATTACK_DAMAGE, 12.5).add(Attributes.ARMOR, 9.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0).add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public String tipo() {
        return "barquero";
    }

    @Override
    public int musica() {
        return 35;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return "barquero";
    }

    @Override
    protected int[] ticks() {
        return BarqueroAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return BarqueroAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return BarqueroAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return BarqueroAnims.MORIR;
    }

    @Override
    protected int accionRugido() {
        return BarqueroAnims.GRITO;
    }

    @Override
    protected int accionLevantarse() {
        return BarqueroAnims.LEVANTARSE;
    }

    // ------------------------------------------------------------------ para las pruebas

    public int saltos() {
        return saltos;
    }

    public int enganches() {
        return enganches;
    }

    public int rematados() {
        return rematados;
    }

    public int ondas() {
        return ondas;
    }

    public int anillos() {
        return anillosHechos;
    }

    public int quemados() {
        return quemados;
    }

    /** A cuantos ha llegado un anillo de pie (les entre o no el golpe). */
    public int alcanzados() {
        return alcanzados;
    }

    public int despertados() {
        return despertados;
    }

    public int subidos() {
        return subidos;
    }

    /** Cuantos de los que ha subido del agua siguen en pie. */
    public int suyos() {
        int n = 0;
        for (Podrido a : suyos) if (a.isAlive() && !a.isRemoved()) n++;
        return n;
    }

    /** Para las pruebas: pasa ya a media vida (ruge y llama a sus pasajeros), sin esperar a que acabe lo que hace. */
    public void mediaVida() {
        if (level().isClientSide || !isAlive()) return;
        setHealth(Math.min(getHealth(), getMaxHealth() * 0.45f));
        segunda = true;
        alSegundaMitad();
        empezar(BarqueroAnims.GRITO);
    }

    /** Si va por el aire (saltando). */
    public boolean volando() {
        return volando;
    }

    /** Si hay un anillo de fuego abriendose. */
    public boolean anilloActivo() {
        return !anillos.isEmpty();
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
        // mas abajo que su presa (en su barca, o en el agua): sube de un salto hasta ella
        if (dy > 0.6 && d > 1.2 && d < SALTO_MAX + 2 && saltoEspera <= 0) {
            saltoEspera = 40;
            return BarqueroAnims.SALTO;
        }
        if (Math.abs(dy) > 2.5) return 0;
        // quien se le pone detras o se le pega: la linterna de un reves (quema)
        if (cercaEspera <= 0 && (d < 3.0 && detras(presa) || d < 1.6)) {
            cercaEspera = 40 + random.nextInt(30);
            return BarqueroAnims.GOLPE_LINTERNA;
        }
        // el anillo de fuego: a quien esta a menos de siete (y siempre que haya dos o mas cerca)
        if (linternaEspera <= 0 && d < 7.0 && (random.nextFloat() < (segunda ? 0.4f : 0.28f) || cercanos(7.0) >= 2)) {
            linternaEspera = (segunda ? 150 : 220) + random.nextInt(60);
            return BarqueroAnims.LINTERNA;
        }
        // el bichero a quien esta a unos pasos (a media vida, mas a menudo)
        if (d >= BICHERO_MIN + 1.4 && d <= BICHERO_MAX && bicheroEspera <= 0 && random.nextFloat() < (segunda ? 0.65f : 0.45f)) {
            bicheroEspera = (segunda ? 110 : 160) + random.nextInt(60);
            return BarqueroAnims.BICHERO;
        }
        // un salto a quien se aleja
        if (d >= 7.0 && d <= SALTO_MAX && Math.abs(dy) < 1.0 && saltoEspera <= 0 && random.nextFloat() < 0.35f) {
            saltoEspera = 140 + random.nextInt(80);
            return BarqueroAnims.SALTO;
        }
        if (d > 4.8) return 0;
        // de cerca: el remazo, el barrido o la linterna (no el mismo tres veces seguidas)
        float r = random.nextFloat();
        int a = r < 0.4f ? BarqueroAnims.REMAZO : r < 0.76f ? BarqueroAnims.BARRIDO : BarqueroAnims.GOLPE_LINTERNA;
        if (a == ultimo) {
            if (++repetido >= 2) {
                a = a == BarqueroAnims.REMAZO ? BarqueroAnims.BARRIDO : BarqueroAnims.REMAZO;
                repetido = 0;
            }
        } else repetido = 0;
        ultimo = a;
        return a;
    }

    /** Cuantos jugadores (que se puedan atacar) tiene a menos de {@code r}. */
    private int cercanos(double r) {
        int n = 0;
        for (Player p : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(r, 2, r)))
            if (objetivoValido(p) && p.distanceToSqr(this) < r * r) n++;
        return n;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case BarqueroAnims.REMAZO -> new Golpe(4.6f, 50f, 1.75f, 0.7f);
            case BarqueroAnims.BARRIDO -> new Golpe(4.6f, 220f, 1.2f, 0.9f);
            case BarqueroAnims.GOLPE_LINTERNA -> new Golpe(3.4f, 170f, 1.0f, 0.8f);
            // la caida del salto: a lo de alrededor de donde cae
            case BarqueroAnims.SALTO -> new Golpe(2.8f, 360f, 1.5f, 1.0f);
            default -> null;
        };
    }

    /** El golpe de linterna quema. */
    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (accion == BarqueroAnims.GOLPE_LINTERNA) le.setSecondsOnFire(4);
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = BarqueroAnims.GOLPES[accion];
        switch (accion) {
            case BarqueroAnims.REMAZO -> {
                if (t == g[0] - 1) empujar(0.35);
                if (t == g[0]) {
                    playSound(DarkCraftMod.SFX_BARQUERO_REMAZO.get(), 1.8f, 0.92f + random.nextFloat() * 0.12f);
                    onda(3.9, 2.3, 0.6f, 0.4);
                }
            }
            case BarqueroAnims.BARRIDO -> {
                if (t == g[0] - 4) playSound(DarkCraftMod.SFX_BARQUERO_BARRIDO.get(), 1.7f, 0.9f + random.nextFloat() * 0.12f);
                if (t == g[0] - 1) empujar(0.3);
                if (t == g[0]) barrido(4.0, 110);
            }
            case BarqueroAnims.BICHERO -> bichero(t);
            case BarqueroAnims.GOLPE_LINTERNA -> {
                if (t == g[0] - 3) playSound(DarkCraftMod.SFX_BARQUERO_BARRIDO.get(), 1.2f, 1.25f);
                if (t == g[0] && level() instanceof ServerLevel sl) {
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX() + d.x * 2.2, getY() + 1.6, getZ() + d.z * 2.2, 18, 0.7, 0.4, 0.7, 0.04);
                    sl.playSound(null, getX(), getY() + 1.5, getZ(), net.minecraft.sounds.SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 0.9f, 0.7f);
                }
            }
            case BarqueroAnims.LINTERNA -> {
                if (t == 10) playSound(DarkCraftMod.SFX_BARQUERO_LINTERNA.get(), 2.2f, 1.0f);
                if (t == g[0]) {
                    anillos.add(new Anillo(getX(), getY(), getZ(), 0));
                    anillosHechos++;
                    // a media vida, otro detras
                    if (segunda) {
                        anillos.add(new Anillo(getX(), getY(), getZ(), 12));
                        anillosHechos++;
                    }
                    if (level() instanceof ServerLevel sl)
                        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + getBbHeight() + 0.6, getZ(), 30, 0.3, 0.3, 0.3, 0.08);
                }
            }
            case BarqueroAnims.SALTO -> saltar(t);
            case BarqueroAnims.GRITO -> {
                if (t == 8) {
                    playSound(DarkCraftMod.SFX_BARQUERO_GRITO.get(), 2.8f, 1.0f);
                    if (level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + getBbHeight() * 0.9, getZ(), 20, 0.6, 0.4, 0.6, 0.03);
                        sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + getBbHeight() * 0.6, getZ(), 24, 2.0, 1.0, 2.0, 0.02);
                    }
                }
                // la de media vida: "arriba, mis pasajeros" y se alzan los de la laguna (o suben del agua)
                if (t == 16 && segunda && !llamo) {
                    llamo = true;
                    habla(2);
                    llamar();
                }
            }
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ el salto

    /**
     * Se agacha, salta (tick {@link #DESPEGA}) y cae con el remo por delante (tick {@link #ATERRIZA}): un arco que lleva hasta
     * poco antes de su presa (como mucho {@link #SALTO_MAX} bloques). Por el aire no choca con nada (asi pasa por encima de la
     * borda y de la barandilla); donde cae tiene que caber.
     */
    private void saltar(int t) {
        if (!(level() instanceof ServerLevel sl)) return;
        if (t == DESPEGA) {
            saltoDe = position();
            saltoA = dondeCae(sl);
            if (saltoA == null) return;
            double h = Math.hypot(saltoA.x - saltoDe.x, saltoA.z - saltoDe.z);
            saltoAlto = 1.8 + 0.4 * Math.max(0, saltoA.y - saltoDe.y) + 0.06 * h;
            volando = true;
            noPhysics = true;
            setNoGravity(true);
            saltos++;
            float giro = (float) Math.toDegrees(Math.atan2(saltoA.z - saltoDe.z, saltoA.x - saltoDe.x)) - 90f;
            if (h > 0.3) {
                setYRot(giro);
                yBodyRot = yHeadRot = giro;
            }
            playSound(DarkCraftMod.SFX_BARQUERO_BARRIDO.get(), 1.4f, 0.7f);
            sl.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 0.2, getZ(), 30, 0.8, 0.1, 0.8, 0.1);
        } else if (t > DESPEGA && t <= ATERRIZA && volando) {
            double s = (t - DESPEGA) / (double) (ATERRIZA - DESPEGA);
            Vec3 p = saltoDe.lerp(saltoA, s).add(0, saltoAlto * 4 * s * (1 - s), 0);
            setPos(p.x, p.y, p.z);
            setDeltaMovement(Vec3.ZERO);
            if (t == ATERRIZA) aterrizar();
        } else if (t == ATERRIZA + 1) {
            // el golpe de la caida (el del remo lo da golpear): las tablas crujen, el agua salta y una ola por el suelo
            playSound(DarkCraftMod.SFX_BARQUERO_REMAZO.get(), 2.0f, 0.85f + random.nextFloat() * 0.12f);
            onda(0.0, 4.2, 0.5f, 0.45);
        }
    }

    /** Donde caer: poco antes de su presa (en su suelo), si cabe; si no, encima de ella; si tampoco, null. De vuelta a su barca, en ella. */
    private Vec3 dondeCae(ServerLevel sl) {
        if (volverA != null) {
            Vec3 v = volverA;
            volverA = null;
            if (Math.hypot(v.x - getX(), v.z - getZ()) > SALTO_MAX + 4) return null;
            AABB caja = getBoundingBox().move(v.x - getX(), v.y - getY(), v.z - getZ()).deflate(0.05);
            return sl.noCollision(this, caja) ? v : null;
        }
        LivingEntity p = getTarget();
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double px = p != null ? p.getX() : getX() + d.x * 4, pz = p != null ? p.getZ() : getZ() + d.z * 4;
        double py = p != null ? p.getY() : getY();
        double dx = px - getX(), dz = pz - getZ(), l = Math.sqrt(dx * dx + dz * dz);
        if (l > 0.01) {
            dx /= l;
            dz /= l;
        } else {
            dx = d.x;
            dz = d.z;
        }
        double[] cortos = {Mth.clamp(l - 1.8, 2.5, SALTO_MAX), Mth.clamp(l - 0.8, 2.5, SALTO_MAX), Mth.clamp(l - 2.8, 2.0, SALTO_MAX), Mth.clamp(l, 2.0, SALTO_MAX)};
        for (double c : cortos) {
            double x = getX() + dx * c, z = getZ() + dz * c;
            double y = Puestos.suelo(sl, Mth.floor(x), Mth.floor(py + 0.01), Mth.floor(z));
            AABB caja = getBoundingBox().move(x - getX(), y - getY(), z - getZ()).deflate(0.05);
            if (sl.noCollision(this, caja) && !sl.noCollision(this, caja.move(0, -0.6, 0)) && !sl.containsAnyLiquid(caja)) return new Vec3(x, y, z);
        }
        return null;
    }

    /** Vuelve a tocar el suelo (o se le corta el salto). */
    private void aterrizar() {
        if (saltoA != null && volando) setPos(saltoA.x, saltoA.y, saltoA.z);
        volando = false;
        noPhysics = false;
        setNoGravity(false);
        setDeltaMovement(Vec3.ZERO);
        fallDistance = 0;
    }

    // ------------------------------------------------------------------ el bichero

    /**
     * El bichero: recoge el remo, lo clava (el golpe) en quien tiene delante a lo que llega el remo y, si le entra (rodar o
     * cubrirse libran), el garfio le engancha y tira de el hasta sus pies; enseguida, el golpe de linterna.
     */
    private void bichero(int t) {
        LivingEntity p = getTarget();
        int g = BarqueroAnims.GOLPES[BarqueroAnims.BICHERO][0];
        if (t == g - 6) playSound(DarkCraftMod.SFX_BARQUERO_BICHERO.get(), 1.5f, 0.92f + random.nextFloat() * 0.14f);
        if (t == g) {
            enganchado = null;
            if (p != null && p.isAlive() && objetivoValido(p) && hasLineOfSight(p)) {
                double dx = p.getX() - getX(), dz = p.getZ() - getZ(), d = Math.sqrt(dx * dx + dz * dz);
                Vec3 mira = Vec3.directionFromRotation(0, getYRot());
                boolean delante = d < 1.0 || (dx * mira.x + dz * mira.z) / d > Math.cos(Math.toRadians(24));
                if (d <= BICHERO_MAX + 0.6 && delante && Math.abs(p.getY() - getY()) < 2.5
                        && p.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.55f)) {
                    if (level() instanceof ServerLevel sl)
                        sl.sendParticles(ParticleTypes.CRIT, p.getX(), p.getY() + p.getBbHeight() * 0.6, p.getZ(), 10, 0.25, 0.25, 0.25, 0.2);
                    if (d > BICHERO_MIN) {
                        enganchado = p;
                        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, true));
                        enganches++;
                        if (!bicheroDicho) {
                            bicheroDicho = true;
                            hablaSiCalla(4);
                        }
                    }
                }
            }
        } else if (t > g && enganchado != null) {
            LivingEntity e = enganchado;
            double dx = getX() - e.getX(), dz = getZ() - e.getZ(), d = Math.sqrt(dx * dx + dz * dz);
            if (!e.isAlive()) {
                enganchado = null;
                return;
            }
            // ya lo tiene a los pies (o se acaba el tiron): el golpe de linterna
            if (d < 2.3 || t >= g + 14) {
                enganchado = null;
                if (d < 3.6) {
                    girarHacia(e, 180f);
                    rematados++;
                    cercaEspera = 30;
                    empezar(BarqueroAnims.GOLPE_LINTERNA);
                }
                return;
            }
            // tira: hacia sus pies, deprisa
            double v = Math.min(0.62, 0.28 + d * 0.06);
            e.setDeltaMovement(dx / d * v, Math.max(e.getDeltaMovement().y, 0.05), dz / d * v);
            e.hurtMarked = true;
            e.hasImpulse = true;
            if (level() instanceof ServerLevel sl && t % 2 == 0)
                sl.sendParticles(ParticleTypes.SPLASH, e.getX(), e.getY() + 0.2, e.getZ(), 6, 0.3, 0.05, 0.3, 0.05);
        }
    }

    // ------------------------------------------------------------------ el barrido, las olas y los anillos

    /** Lo que levanta el remo al barrer: agua y astillas a media altura, en un arco delante. */
    private void barrido(double r, double medio) {
        if (!(level() instanceof ServerLevel sl)) return;
        for (int k = 0; k <= 12; k++) {
            double a = Math.toRadians(getYRot() + 90 - medio + 2 * medio * k / 12.0);
            double x = getX() + Math.cos(a) * r, z = getZ() + Math.sin(a) * r;
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, x, getY() + 1.3, z, 1, 0, 0, 0, 0);
            if (k % 2 == 0) sl.sendParticles(ParticleTypes.SPLASH, x, getY() + 1.0, z, 6, 0.3, 0.2, 0.3, 0.1);
        }
    }

    /**
     * El remo (o la caida) contra las tablas: agua negra que salta y una ola (a {@code delante} bloques, de radio {@code r})
     * que alcanza a quien este de pie (saltando o rodando se libra; a quien ya le ha dado el golpe, no).
     */
    private void onda(double delante, double r, float mult, double arriba) {
        if (!(level() instanceof ServerLevel sl)) return;
        ondas++;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * delante, z = getZ() + d.z * delante;
        BlockState suelo = level().getBlockState(BlockPos.containing(x, getY() - 0.5, z));
        int n = (int) Math.round(r * 14);
        if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), x, getY() + 0.1, z, n / 2, r * 0.4, 0.05, r * 0.4, 0.2);
        sl.sendParticles(ParticleTypes.SPLASH, x, getY() + 0.3, z, n * 2, r * 0.45, 0.1, r * 0.45, 0.3);
        sl.sendParticles(ParticleTypes.BUBBLE_POP, x, getY() + 0.4, z, n, r * 0.4, 0.2, r * 0.4, 0.05);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, x, getY() + 0.2, z, n / 3, r * 0.4, 0.1, r * 0.4, 0.02);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * mult;
        for (Player p : level().getEntitiesOfClass(Player.class, new AABB(x - r - 1, getY() - 1, z - r - 1, x + r + 1, getY() + 2.5, z + r + 1))) {
            if (!objetivoValido(p) || !p.onGround() || p.hurtTime > 0 || p.distanceToSqr(x, p.getY(), z) > r * r) continue;
            if (p.hurt(damageSources().mobAttack(this), dano)) {
                p.knockback(0.6, x - p.getX(), z - p.getZ());
                p.setDeltaMovement(p.getDeltaMovement().add(0, arriba, 0));
                p.hurtMarked = true;
            }
        }
    }

    /** Los anillos de fuego de almas: cada tick crecen; queman a quien pillan de pie en su borde (una vez por anillo). */
    private void anillos(ServerLevel sl) {
        Iterator<Anillo> it = anillos.iterator();
        while (it.hasNext()) {
            Anillo a = it.next();
            if (a.espera > 0) {
                a.espera--;
                continue;
            }
            a.r += ANILLO_VEL;
            if (a.r > ANILLO_MAX) {
                it.remove();
                continue;
            }
            int n = (int) Math.round(a.r * 5.5);
            for (int k = 0; k < n; k++) {
                double ang = (k + random.nextDouble() * 0.5) * Math.PI * 2 / n;
                double x = a.x + Math.cos(ang) * a.r, z = a.z + Math.sin(ang) * a.r;
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, a.y + 0.15, z, 1, 0.05, 0.05, 0.05, 0.01);
                if (k % 4 == 0) sl.sendParticles(ParticleTypes.SOUL, x, a.y + 0.4, z, 1, 0.05, 0.1, 0.05, 0.01);
            }
            float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.8f;
            for (LivingEntity p : sl.getEntitiesOfClass(LivingEntity.class, new AABB(a.x - a.r - 1.5, a.y - 1.5, a.z - a.r - 1.5, a.x + a.r + 1.5, a.y + 2.5, a.z + a.r + 1.5),
                    e -> e != this && e.isAlive() && objetivoValido(e))) {
                if (a.tocados.contains(p.getId())) continue;
                double d = Math.hypot(p.getX() - a.x, p.getZ() - a.z);
                // el borde del fuego (saltando por encima se libra; rodando, la voltereta le hace invulnerable)
                if (Math.abs(d - a.r) > 0.75 || Math.abs(p.getY() - a.y) > 1.8 || !p.onGround()) continue;
                a.tocados.add(p.getId());
                alcanzados++;
                if (p.hurt(damageSources().mobAttack(this), dano)) {
                    p.setSecondsOnFire(3);
                    p.knockback(0.4, a.x - p.getX(), a.z - p.getZ());
                    p.hurtMarked = true;
                    quemados++;
                }
            }
        }
    }

    // ------------------------------------------------------------------ los pasajeros: se alzan del agua

    private void llamar() {
        if (!(level() instanceof ServerLevel sl)) return;
        List<ServerPlayer> cerca = new ArrayList<>();
        for (ServerPlayer p : sl.players()) if (p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) < 40 * 40) cerca.add(p);
        int n = Math.max(2, Math.min(4, 1 + cerca.size()));
        // primero, los que siguen flotando en la laguna: se alzan y van a por el jugador que tengan mas cerca
        List<Podrido> flotan = sl.getEntitiesOfClass(Podrido.class, getBoundingBox().inflate(30, 10, 30), a -> a.isAlive() && a.postura() == TUMBADO && a.accion() == 0);
        flotan.sort(Comparator.comparingDouble(a -> a.distanceToSqr(this)));
        int alzados = 0;
        for (Podrido a : flotan) {
            if (alzados >= n) break;
            LivingEntity t = null;
            double mejor = Double.MAX_VALUE;
            for (ServerPlayer p : cerca)
                if (objetivoValido(p) && p.distanceToSqr(a) < mejor) {
                    mejor = p.distanceToSqr(a);
                    t = p;
                }
            if (t == null) t = getTarget();
            if (t != null && a.acudir(t)) {
                alzados++;
                sl.sendParticles(ParticleTypes.SOUL, a.getX(), a.getY() + 0.8, a.getZ(), 8, 0.3, 0.3, 0.3, 0.02);
            }
        }
        despertados += alzados;
        // los que falten: suben del agua al embarcadero alrededor de el (los que salen del agua, tumbados en las tablas)
        int salen = 0;
        double giro = random.nextDouble() * Math.PI * 2;
        for (int k = 0; k < 18 && alzados + salen < n; k++) {
            double a = giro + k * (Math.PI * 2 / 6) + (k >= 6 ? 0.35 * (k / 6) : 0);
            double r = 4.5 + (k % 3) * 1.3;
            int bx = Mth.floor(getX() + Math.cos(a) * r), bz = Mth.floor(getZ() + Math.sin(a) * r);
            double y = Puestos.suelo(sl, bx, Mth.floor(getY()) + 1, bz);
            if (Math.abs(y - getY()) > 1.6) continue;
            Podrido m = DarkCraftMod.PODRIDO.get().create(sl);
            if (m == null) return;
            float mira = (float) Math.toDegrees(Math.atan2(getZ() - (bz + 0.5), getX() - (bx + 0.5))) - 90f;
            m.moveTo(bx + 0.5, y, bz + 0.5, mira, 0f);
            if (!sl.noCollision(m) || sl.containsAnyLiquid(m.getBoundingBox())) continue;
            boolean cercaDeAlguien = false;
            for (ServerPlayer p : cerca) if (p.distanceToSqr(m) < 2.5 * 2.5) cercaDeAlguien = true;
            if (cercaDeAlguien) continue;
            m.variante(salen == 3 ? Podrido.HINCHADO : salen % 2 == 0 ? Podrido.PODRIDO : Podrido.FATUO);
            // tumbados en las tablas (como recien salidos del agua): se alzan al ver a su presa (sin puesto: no vuelven)
            m.asignar(null, -1, BlockPos.containing(bx + 0.5, y, bz + 0.5), mira, TUMBADO, 0);
            m.efimero(200);
            sl.addFreshEntity(m);
            LivingEntity t = getTarget() != null ? getTarget()
                    : sl.getNearestPlayer(m.getX(), m.getY(), m.getZ(), 30, e -> e instanceof Player q && objetivoValido(q));
            if (t != null) m.setTarget(t);
            sl.sendParticles(ParticleTypes.SPLASH, bx + 0.5, y + 0.3, bz + 0.5, 40, 0.5, 0.2, 0.5, 0.2);
            sl.sendParticles(ParticleTypes.BUBBLE_POP, bx + 0.5, y + 0.3, bz + 0.5, 16, 0.4, 0.2, 0.4, 0.05);
            sl.sendParticles(ParticleTypes.SOUL, bx + 0.5, y + 0.3, bz + 0.5, 10, 0.4, 0.3, 0.4, 0.02);
            sl.playSound(null, bx + 0.5, y, bz + 0.5, DarkCraftMod.SFX_PODRIDO_ALZARSE.get(), SoundSource.HOSTILE, 1.3f, 0.75f + random.nextFloat() * 0.2f);
            suyos.add(m);
            salen++;
        }
        subidos += salen;
        DarkCraftMod.LOGGER.info("[DarkCraft] El Barquero llama a sus pasajeros: {} se alzan de la laguna, {} suben del agua ({} jugadores)", alzados, salen, cerca.size());
    }

    private void despedir() {
        for (Podrido a : suyos) if (a.isAlive() && !a.isRemoved()) a.disolver();
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
        llamo = bicheroDicho = false;
        bicheroEspera = 40;
        linternaEspera = 80;
        saltoEspera = 0;
        repetido = ultimo = 0;
        long ahora = level().getGameTime();
        if (ahora - ultimaEntrada > 20 * 60 * 4 || ahora < ultimaEntrada) {
            ultimaEntrada = ahora;
            habla(1);
            // se yergue mientras lo dice y no ruge ni salta hasta acabarlo (se queda en su barca mirando a su presa)
            int dura = Voces.ticks(VOZ_BASE + 1);
            esperaRugido = tickCount + dura - 12;
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
        enganchado = null;
        anillos.clear();
        esperaRugido = 0;
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
            // el fuego de la linterna (a su izquierda, a media altura) y los ojos bajo la capucha
            if (isAlive() && tickCount % 4 == 0) {
                Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
                double ix = -d.z, iz = d.x;   // su izquierda
                double alto = postura() == REZA ? getBbHeight() * 0.62 : getBbHeight() * 0.34;
                double fx = getX() + ix * 0.55 + d.x * (postura() == REZA ? 0.9 : 0.25), fz = getZ() + iz * 0.55 + d.z * (postura() == REZA ? 0.9 : 0.25);
                level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, fx, getY() + alto, fz, 0, 0.012, 0);
            }
            return;
        }
        if (!isAlive()) return;
        if (bicheroEspera > 0) bicheroEspera--;
        if (saltoEspera > 0) saltoEspera--;
        if (linternaEspera > 0) linternaEspera--;
        if (cercaEspera > 0) cercaEspera--;
        if (!anillos.isEmpty() && level() instanceof ServerLevel sl) anillos(sl);
        // si se le corta el salto (no deberia: no se le aturde), aterriza; el garfio suelta si acaba el bichero
        if (volando && accion() != BarqueroAnims.SALTO) aterrizar();
        if (enganchado != null && accion() != BarqueroAnims.BICHERO) enganchado = null;
        // acabada la pelea lejos de su barca: vuelve a ella de un salto (si esta a tiro; si no, la busca andando o nadando)
        if (!enPelea() && getTarget() == null && accion() == 0 && postura() == DE_PIE && hogar() != null && saltoEspera <= 0
                && hogar().distToCenterSqr(getX(), getY(), getZ()) > 2.5 * 2.5 && hogar().distToCenterSqr(getX(), getY(), getZ()) < (SALTO_MAX + 3) * (SALTO_MAX + 3)) {
            saltoEspera = 60;
            BlockPos h = hogar();
            volverA = new Vec3(h.getX() + 0.5, h.getY(), h.getZ() + 0.5);
            float giro = (float) Math.toDegrees(Math.atan2(volverA.z - getZ(), volverA.x - getX())) - 90f;
            setYRot(giro);
            yBodyRot = yHeadRot = giro;
            empezar(BarqueroAnims.SALTO);
        }
        // esperando en su barca: rema de vez en cuando (se le oye desde el muelle)
        if (!enPelea() && postura() == REZA && --remo <= 0) {
            remo = 160 + random.nextInt(140);
            playSound(DarkCraftMod.SFX_BARQUERO_REMO.get(), 1.4f, 0.9f + random.nextFloat() * 0.15f);
        }
        // ha caido aquel con quien peleaba
        if (enPelea() && getTarget() == null && ultimaPresa != null && ultimaPresa.isDeadOrDying()) {
            ultimaPresa = null;
            hablaSiCalla(3);
        }
    }

    @Override
    protected void alSegundaMitad() {
        bicheroEspera = Math.min(bicheroEspera, 30);
        linternaEspera = Math.min(linternaEspera, 60);
    }

    @Override
    public void die(DamageSource s) {
        boolean ya = !isAlive() && deathTime > 0;
        if (volando) aterrizar();
        enganchado = null;
        anillos.clear();
        super.die(s);
        if (level().isClientSide || ya) return;
        habla(5);
        despedir();
        if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1.2, getZ(), 40, 0.6, 0.6, 0.6, 0.05);
    }

    @Override
    public void remove(RemovalReason r) {
        if (!level().isClientSide) despedir();
        super.remove(r);
    }

    /** Mientras dice su primera frase se queda donde esta (en su barca, mirando a su presa); despues, de cerca. */
    @Override
    protected double distanciaDeseada() {
        return enPelea() && tickCount < esperaRugido ? 99.0 : 3.6;
    }

    @Override
    protected double prisa() {
        return segunda ? 1.25 : 1.1;
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (accion) {
            case BarqueroAnims.GRITO, BarqueroAnims.BICHERO -> 6;
            case BarqueroAnims.SALTO -> 10 + random.nextInt(8);
            case BarqueroAnims.GOLPE_LINTERNA -> 8 + random.nextInt(8);
            case BarqueroAnims.LINTERNA -> 12 + random.nextInt(8);
            default -> (segunda ? 8 : 14) + random.nextInt(12);
        };
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case BarqueroAnims.REMAZO -> DarkCraftMod.SFX_GOLPE_PESADO.get();
            default -> null;
        };
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        SoundType t = s.getSoundType(level(), p, this);
        playSound(t.getStepSound(), t.getVolume() * 0.8f, t.getPitch() * 0.6f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_BARQUERO_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_BARQUERO_MUERTE.get();
    }

    @Override
    public boolean causeFallDamage(float d, float m, DamageSource s) {
        return false;
    }

    /** El agua de la laguna no le frena (ni le ahoga). */
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_2.get(), 2));
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_3.get()));
        spawnAtLocation(new ItemStack(DarkCraftMod.CENIZAS.get(com.darkcraft.combate.Habilidad.LLAMAS).get()));
    }

    /** Para las pruebas y el comando: como esta. */
    public String estado() {
        return String.format(java.util.Locale.ROOT, "vida %.0f/%.0f, %s, %s en %.1f,%.1f,%.1f; saltos %d, enganches %d, rematados %d, ondas %d, anillos %d, quemados %d, pasajeros alzados %d, subidos %d",
                getHealth(), getMaxHealth(), enPelea() ? "peleando" : "esperando", postura() == DE_PIE ? "de pie" : "en su barca con la linterna en alto",
                getX(), getY(), getZ(), saltos, enganches, rematados, ondas, anillosHechos, quemados, despertados, subidos);
    }
}
