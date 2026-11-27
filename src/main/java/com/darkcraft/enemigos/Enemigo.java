package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Lo comun de los enemigos del mod con forma de persona (parte 3c). No son zombis de Minecraft con otra piel:
 * <ul>
 * <li><b>Sus golpes se ven venir</b>: cada ataque es una <i>accion</i> con su animacion (tools/enemigos), que dura lo
 * que dura y pega en sus ticks de golpe a quien pille delante, en su alcance y su arco. Mientras se prepara sigue a su
 * presa con el cuerpo; poco antes del golpe ya no gira: quien se aparta, lo esquiva. Rodar, cubrirse y la parada
 * funcionan como con cualquier golpe (es dano de criatura).</li>
 * <li><b>Tienen su puesto</b> ({@link Puestos}): un sitio, hacia donde miran y como esperan (de pie, de rodillas o
 * tirados en el suelo como un muerto mas). No ven a quien les llega por la espalda agachado; si su presa se aleja
 * demasiado de su puesto la dejan y vuelven. Los de un puesto no se guardan con el mundo: los lleva {@link Puestos}.</li>
 * <li>Si se les rompe la guardia ({@code Combate.desequilibrar}) o se les para un golpe, quedan <b>aturdidos</b>.</li>
 * </ul>
 */
public abstract class Enemigo extends Monster {
    private static final EntityDataAccessor<Byte> VARIANTE = SynchedEntityData.defineId(Enemigo.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> ACCION = SynchedEntityData.defineId(Enemigo.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> SERIE = SynchedEntityData.defineId(Enemigo.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> POSTURA = SynchedEntityData.defineId(Enemigo.class, EntityDataSerializers.BYTE);

    /** Como espera en su puesto (COLGADO: de la soga de una rama, los ahorcados de la parte 7g). */
    public static final int DE_PIE = 0, REZA = 1, TUMBADO = 2, COLGADO = 3;

    private static final UUID MOD_GRUPO = UUID.fromString("7b1e2c90-5a44-4d7e-9c1b-3f0a6d2e8b11");
    /** El animo (parte 4c): un capitan de elite les arenga (pegan mas) o ha caido el suyo (pegan menos y van mas despacio). */
    private static final UUID MOD_ANIMO = UUID.fromString("c1d7a3e2-6b5f-4e8a-9d21-7f3b0a4c5e66");
    private static final UUID MOD_ANIMO_PASO = UUID.fromString("a4e2b7c9-1d3f-4b6a-8e5c-2f9d0b7a6c13");

    /** Un golpe: hasta donde llega (bloques), el arco que barre (grados), por cuanto multiplica el ataque y lo que empuja de mas. */
    public record Golpe(float alcance, float arco, float dano, float empuje) {}

    // ---- en el cliente: el reloj de los bucles y el de cada accion
    public final AnimationState reloj = new AnimationState();
    public final AnimationState[] acciones;
    /** De 0 (tranquilo) a 1 (en guardia), suavizado, para mezclar las dos posturas. */
    public float guardia, guardiaO;
    private int serieVista = -1;

    // ---- en el servidor
    protected int accionT;
    /** Ticks hasta que pueda volver a atacar. */
    protected int pausa;
    private final Set<Integer> tocados = new HashSet<>();
    private boolean despertando;
    private float rumbo;
    private BlockPos hogar;
    private float hogarGiro;
    private int hogarPostura, ronda;
    private String sitio = "";
    private int puesto = -1;
    private int enCasa, atascado;
    /** Los que llama un jefe: no se guardan y, pasado su tiempo sin presa, se deshacen. */
    private int efimero;
    private int animoT;
    private double animo;

    protected Enemigo(EntityType<? extends Enemigo> t, Level l) {
        super(t, l);
        acciones = new AnimationState[ticks().length];
        for (int i = 0; i < acciones.length; i++) acciones[i] = new AnimationState();
        setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(VARIANTE, (byte) 0);
        entityData.define(ACCION, (byte) 0);
        entityData.define(SERIE, (byte) 0);
        entityData.define(POSTURA, (byte) 0);
    }

    // ------------------------------------------------------------------ lo que dice cada enemigo

    /** Clave del bestiario. */
    public abstract String tipo();

    /** Lo que dura cada accion y sus ticks de golpe (las tablas que escribe tools/enemigos). */
    protected abstract int[] ticks();

    protected abstract int[][] golpes();

    /** La accion con la que ataca a su presa, que esta a {@code d} bloques (0 = aun no). */
    protected abstract int elegir(LivingEntity presa, double d);

    /** Como pega el golpe numero {@code n} de la accion (null = no pega). */
    protected abstract Golpe golpe(int accion, int n);

    protected int accionAturdido() {
        return 0;
    }

    protected int accionMorir() {
        return 0;
    }

    /** La accion de levantarse desde esa postura (0 = se pone de pie sin mas). */
    protected int accionDespertar(int postura) {
        return 0;
    }

    /** Lo que espera tras una accion antes de volver a atacar. */
    protected int pausaTras(int accion) {
        return 16 + random.nextInt(16);
    }

    /** A que distancia de su presa quiere quedarse. */
    protected double distanciaDeseada() {
        return 1.7;
    }

    /** Lo que puede haber de desnivel con su presa para atacarla (los que lanzan algo, mas). */
    protected double alturaMaxima() {
        return 2.6;
    }

    /**
     * Hacia donde soltar una flecha (unitario) para que, saliendo a esa velocidad, caiga en un blanco que esta a h bloques
     * en horizontal (hacia dir, unitario y sin y) y dy por encima: el tiro bajo, con la caida y el frenado de las flechas
     * del juego (cada tick: avanza, se frena un 1% y cae 0,05). Si no llega, el tiro mas largo que hay.
     */
    public static Vec3 tiro(double h, double dy, Vec3 dir, double v) {
        double lo = -1.45, hi = 0.72;
        for (int it = 0; it < 28; it++) {
            double mid = (lo + hi) * 0.5;
            if (alturaA(mid, h, v) < dy) lo = mid;
            else hi = mid;
        }
        double a = (lo + hi) * 0.5;
        return new Vec3(dir.x * Math.cos(a), Math.sin(a), dir.z * Math.cos(a));
    }

    /** A que altura pasa la flecha lanzada con ese angulo al recorrer h en horizontal (muy abajo si no llega). */
    private static double alturaA(double a, double h, double v) {
        double vx = Math.cos(a) * v, vy = Math.sin(a) * v, x = 0, y = 0;
        for (int t = 0; t < 160 && vx > 1e-3; t++) {
            if (x + vx >= h) return y + vy * (h - x) / vx;
            x += vx;
            y += vy;
            vx *= 0.99;
            vy = vy * 0.99 - 0.05;
        }
        return -1e9;
    }

    /** Lo deprisa que persigue (por lo que anda). */
    protected double prisa() {
        return 1.0;
    }

    /** Cada tick de una accion (para el paso de la estocada, lo que suena, lo que lanza...). */
    protected void alAvanzar(int accion, int t) {
    }

    /** Si ese golpe le alcanza (ademas del alcance y el arco): la siega de la Molinera va a ras de suelo y se salta. */
    protected boolean pilla(int accion, int n, LivingEntity le) {
        return true;
    }

    protected void alGolpear(LivingEntity le, int accion) {
    }

    /** El sonido del arma al descargar el golpe (null = ninguno). */
    protected SoundEvent silbido(int accion) {
        return DarkCraftMod.SFX_GOLPE_LIGERO.get();
    }

    /** Lo que suelta al reparar en su presa (null = nada). */
    protected SoundEvent sonidoAlerta() {
        return null;
    }

    /** Hasta donde le siguen los ojos y lo lejos que persigue a su presa desde su puesto. */
    protected double correa() {
        return 30;
    }

    // ------------------------------------------------------------------ estado

    public int variante() {
        return entityData.get(VARIANTE);
    }

    public void variante(int v) {
        entityData.set(VARIANTE, (byte) v);
    }

    public int accion() {
        return entityData.get(ACCION);
    }

    public int postura() {
        return entityData.get(POSTURA);
    }

    public void postura(int p) {
        entityData.set(POSTURA, (byte) p);
    }

    public int accionTicks() {
        return accionT;
    }

    /** El puesto que guarda (el sitio y su numero), o -1 si va por libre (un comando, un huevo). */
    public int puesto() {
        return puesto;
    }

    public String sitio() {
        return sitio;
    }

    public BlockPos hogar() {
        return hogar;
    }

    /** Lo pone {@link Puestos} al sacarlo: su puesto, hacia donde mira, como espera y cuanto ronda. */
    public void asignar(String sitio, int puesto, BlockPos hogar, float giro, int postura, int ronda) {
        this.sitio = sitio;
        this.puesto = puesto;
        this.hogar = hogar.immutable();
        this.hogarGiro = giro;
        this.hogarPostura = postura;
        this.ronda = ronda;
        postura(postura);
        setYRot(giro);
        yBodyRot = yHeadRot = yRotO = yBodyRotO = yHeadRotO = giro;
    }

    /** Con amigos, los mismos enemigos pero con mas vida: la mitad mas por cada jugador de mas (hasta el triple y medio). */
    /** El maximo de vida que se le deja a cualquiera (antes de {@link #sinTopeDeVida}, 1024). */
    public static final double TOPE_VIDA = 1.0e6;

    /**
     * Quita el tope de 1024 de la vida maxima del juego: con el, los jefes de mas de 1024 (el Abad, el Pontifice, Aldric, los
     * gigantes, el Jinete...) se quedaban en 1024 y con amigos casi ninguno ganaba la vida de mas ({@link #paraGrupo}). El maximo
     * es un double final de {@code RangedAttribute}: se busca por su valor (los nombres cambian fuera del entorno de desarrollo).
     * Se hace al construir el mod, en el servidor y en el cliente (asi los dos calculan igual la vida maxima).
     */
    public static void sinTopeDeVida() {
        try {
            if (!(Attributes.MAX_HEALTH instanceof net.minecraft.world.entity.ai.attributes.RangedAttribute r)) return;
            for (java.lang.reflect.Field f : net.minecraft.world.entity.ai.attributes.RangedAttribute.class.getDeclaredFields()) {
                if (f.getType() != double.class || java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                f.setAccessible(true);
                if (f.getDouble(r) == 1024.0) {
                    f.setDouble(r, TOPE_VIDA);
                    com.darkcraft.DarkCraftMod.LOGGER.info("[DarkCraft] la vida maxima ya no se queda en 1024 (ahora {})", r.sanitizeValue(5000.0));
                }
            }
        } catch (Throwable t) {
            com.darkcraft.DarkCraftMod.LOGGER.warn("[DarkCraft] no se ha podido quitar el tope de la vida maxima: {}", t.toString());
        }
    }

    public void paraGrupo(int jugadores) {
        var vida = getAttribute(Attributes.MAX_HEALTH);
        if (vida == null) return;
        vida.removeModifier(MOD_GRUPO);
        int mas = Math.max(0, Math.min(5, jugadores - 1));
        if (mas > 0) vida.addTransientModifier(new AttributeModifier(MOD_GRUPO, "darkcraft_grupo", 0.5 * mas, AttributeModifier.Operation.MULTIPLY_BASE));
        setHealth(getMaxHealth());
    }

    /** Lo llama un jefe: no se guarda con el mundo y se deshace solo cuando lleve {@code ticks} sin nadie a quien seguir. */
    public void efimero(int ticks) {
        efimero = Math.max(1, ticks);
    }

    public boolean esEfimero() {
        return efimero > 0;
    }

    /**
     * El animo (parte 4c): {@code mas} &gt; 0, un capitan les ha arengado (pegan un tanto mas); {@code mas} &lt; 0, ha caido
     * su capitan (pegan menos y van mas despacio). Dura {@code ticks}; 0 lo quita.
     */
    public void animo(double mas, int ticks) {
        var ataque = getAttribute(Attributes.ATTACK_DAMAGE);
        var paso = getAttribute(Attributes.MOVEMENT_SPEED);
        if (ataque != null) {
            ataque.removeModifier(MOD_ANIMO);
            if (mas != 0 && ticks > 0) ataque.addTransientModifier(new AttributeModifier(MOD_ANIMO, "darkcraft_animo", mas, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        if (paso != null) {
            paso.removeModifier(MOD_ANIMO_PASO);
            if (mas < 0 && ticks > 0) paso.addTransientModifier(new AttributeModifier(MOD_ANIMO_PASO, "darkcraft_animo_paso", mas * 0.6, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        animo = mas;
        animoT = mas == 0 ? 0 : Math.max(0, ticks);
    }

    /** El animo que tiene ahora (0 si ninguno). */
    public double animo() {
        return animoT > 0 ? animo : 0;
    }

    /** Le avisan de alguien (un capitan, una emboscada): va a por el si es valido y no se aleja demasiado de su puesto. */
    public boolean acudir(LivingEntity quien) {
        if (level().isClientSide || !isAlive() || quien == null || !quien.isAlive() || !objetivoValido(quien) || lejosDeCasa(quien)) return false;
        setTarget(quien);
        return true;
    }

    /** Se deshace en bruma (sin botin ni ecos: no ha muerto). */
    public void disolver() {
        if (level() instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, getX(), getY() + getBbHeight() * 0.5, getZ(), 18, 0.3, 0.6, 0.3, 0.02);
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL, getX(), getY() + getBbHeight() * 0.5, getZ(), 6, 0.3, 0.5, 0.3, 0.02);
        }
        discard();
    }

    /** Empieza una accion (corta la que hubiera). */
    public void empezar(int accion) {
        if (level().isClientSide || accion <= 0 || accion >= ticks().length) return;
        entityData.set(ACCION, (byte) accion);
        entityData.set(SERIE, (byte) (entityData.get(SERIE) + 1));
        accionT = 0;
        tocados.clear();
        rumbo = getYRot();
        getNavigation().stop();
    }

    private void terminar() {
        int a = accion();
        entityData.set(ACCION, (byte) 0);
        accionT = 0;
        if (despertando) {
            despertando = false;
            postura(DE_PIE);
            pausa = Math.max(pausa, 8);
        } else pausa = Math.max(pausa, pausaTras(a));
    }

    /** Se queda aturdido (le han roto la guardia o le han parado un golpe): corta lo que estuviera haciendo. */
    public void aturdir() {
        int a = accionAturdido();
        if (level().isClientSide || !isAlive() || a == 0 || despertando) return;
        empezar(a);
        pausa = ticks()[a] + 8;
    }

    public boolean aturdido() {
        return accion() != 0 && accion() == accionAturdido();
    }

    // ------------------------------------------------------------------ cabeza: a quien ve, a quien sigue

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new Atacar());
        goalSelector.addGoal(5, new Volver());
        goalSelector.addGoal(6, new Rondar());
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10f, 0.04f) {
            @Override
            public boolean canUse() {
                return postura() == DE_PIE && accion() == 0 && super.canUse();
            }
        });
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 8, true, false, this::percibe));
    }

    /** Si repara en ese jugador: de pie, lo que tiene delante (agachado, la mitad de lejos y nunca por la espalda). */
    protected boolean percibe(LivingEntity p) {
        double d2 = distanceToSqr(p);
        if (postura() == TUMBADO) return d2 < 3.6 * 3.6;
        if (postura() == REZA) return d2 < (p.isCrouching() ? 3.0 * 3.0 : 6.0 * 6.0);
        double alc = getAttributeValue(Attributes.FOLLOW_RANGE) * (p.isCrouching() ? 0.5 : 1.0);
        if (d2 > alc * alc) return false;
        if (d2 < (p.isCrouching() ? 2.2 * 2.2 : 4.5 * 4.5)) return true;
        Vec3 mira = Vec3.directionFromRotation(0, yHeadRot);
        double dx = p.getX() - getX(), dz = p.getZ() - getZ(), l = Math.sqrt(dx * dx + dz * dz);
        return l < 0.01 || (dx * mira.x + dz * mira.z) / l > (p.isCrouching() ? 0.35 : -0.2);
    }

    /** A quien le puede caer un golpe: jugadores (no en creativo ni espectador) y su presa. */
    protected boolean objetivoValido(LivingEntity e) {
        if (e instanceof Player p) return !p.isCreative() && !p.isSpectator();
        return e == getTarget();
    }

    private boolean lejosDeCasa(LivingEntity presa) {
        if (hogar == null) return false;
        double c = correa();
        return hogar.distToCenterSqr(getX(), getY(), getZ()) > c * c || hogar.distToCenterSqr(presa.getX(), presa.getY(), presa.getZ()) > (c + 8) * (c + 8);
    }

    private final class Atacar extends Goal {
        private int camino;

        Atacar() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity t = getTarget();
            if (t == null || !t.isAlive()) return false;
            if (lejosDeCasa(t)) {
                setTarget(null);
                return false;
            }
            return true;
        }

        @Override
        public void start() {
            setAggressive(true);
            camino = 0;
        }

        @Override
        public void stop() {
            setAggressive(false);
            getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity t = getTarget();
            if (t == null) return;
            if (postura() != DE_PIE || despertando) {
                getNavigation().stop();
                return;
            }
            int a = accion();
            if (a != 0) {
                getNavigation().stop();
                if (a == accionAturdido()) return;
                // sigue a su presa con el cuerpo mientras se prepara; poco antes del golpe ya no gira
                int[] g = golpes()[a];
                int fija = g.length == 0 ? 0 : g[0] - 3;
                for (int k = 1; k < g.length; k++) if (accionT > g[k - 1] + 1 && accionT < g[k] - 3) fija = Integer.MAX_VALUE;
                if (accionT < fija) rumbo = girarHacia(t, accionT < 4 ? 30f : 9f);
                else fijar();
                return;
            }
            getLookControl().setLookAt(t, 30f, 30f);
            double dx = t.getX() - getX(), dz = t.getZ() - getZ(), d = Math.sqrt(dx * dx + dz * dz);
            if (pausa <= 0 && Math.abs(t.getY() - getY()) < alturaMaxima() && getSensing().hasLineOfSight(t)) {
                int n = elegir(t, d);
                if (n != 0) {
                    girarHacia(t, 40f);
                    empezar(n);
                    return;
                }
            }
            if (d > distanciaDeseada()) {
                if (--camino <= 0) {
                    camino = 5 + random.nextInt(6);
                    getNavigation().moveTo(t, prisa());
                }
            } else {
                getNavigation().stop();
                girarHacia(t, 20f);
            }
        }
    }

    /** Sin presa, vuelve a su puesto. */
    private final class Volver extends Goal {
        Volver() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        private boolean lejos() {
            double r = Math.max(1.4, ronda + 1.0);
            return hogar != null && hogar.distToCenterSqr(getX(), getY(), getZ()) > r * r;
        }

        @Override
        public boolean canUse() {
            return getTarget() == null && accion() == 0 && postura() == DE_PIE && lejos();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void start() {
            atascado = 0;
            getNavigation().moveTo(hogar.getX() + 0.5, hogar.getY(), hogar.getZ() + 0.5, 0.9);
        }

        @Override
        public void tick() {
            if (getNavigation().isDone()) getNavigation().moveTo(hogar.getX() + 0.5, hogar.getY(), hogar.getZ() + 0.5, 0.9);
            // si no encuentra el camino (se cayo de un sitio alto, una puerta), acaba apareciendo en su puesto
            if (++atascado > 400 && level().getNearestPlayer(Enemigo.this, 20) == null) {
                moveTo(hogar.getX() + 0.5, hogar.getY(), hogar.getZ() + 0.5, hogarGiro, 0);
                atascado = 0;
            }
        }

        @Override
        public void stop() {
            getNavigation().stop();
        }
    }

    /** Los que rondan: de vez en cuando, unos pasos alrededor de su puesto. */
    private final class Rondar extends Goal {
        private int espera;

        Rondar() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (ronda <= 0 || hogar == null || getTarget() != null || accion() != 0 || postura() != DE_PIE) return false;
            if (espera > 0) {
                espera--;
                return false;
            }
            return true;
        }

        @Override
        public void start() {
            espera = 100 + random.nextInt(160);
            for (int k = 0; k < 6; k++) {
                BlockPos p = hogar.offset(random.nextInt(2 * ronda + 1) - ronda, 0, random.nextInt(2 * ronda + 1) - ronda);
                if (getNavigation().moveTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, 0.55)) return;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return getTarget() == null && !getNavigation().isDone();
        }
    }

    @Override
    public void setTarget(LivingEntity t) {
        LivingEntity antes = getTarget();
        super.setTarget(t);
        // el grito de cuando ve a alguien (no cada vez que cambia de presa)
        if (t != null && antes == null && !level().isClientSide && isAlive() && sonidoAlerta() != null && tickCount - ultimaAlerta > 100) {
            ultimaAlerta = tickCount;
            playSound(sonidoAlerta(), 1.0f, 0.9f + random.nextFloat() * 0.2f);
        }
    }

    private int ultimaAlerta = -1000;
    private int sinPresaEfimero;

    /** Gira el cuerpo entero hacia alguien, como mucho {@code max} grados; devuelve hacia donde queda mirando. */
    protected float girarHacia(LivingEntity t, float max) {
        float quiere = (float) (Mth.atan2(t.getZ() - getZ(), t.getX() - getX()) * Mth.RAD_TO_DEG) - 90f;
        float y = Mth.approachDegrees(getYRot(), quiere, max);
        setYRot(y);
        yBodyRot = yHeadRot = y;
        return y;
    }

    private void fijar() {
        setYRot(rumbo);
        yBodyRot = yHeadRot = rumbo;
    }

    /** Un paso hacia delante (la estocada, el tajo que avanza). */
    protected void empujar(double fuerza) {
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        setDeltaMovement(d.x * fuerza, getDeltaMovement().y, d.z * fuerza);
        hasImpulse = true;
    }

    // ------------------------------------------------------------------ cada tick

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            cliente();
            return;
        }
        if (!isAlive()) return;
        if (pausa > 0) pausa--;
        if (animoT > 0) {
            if (--animoT == 0) animo(0, 0);
            else if (tickCount % 9 == 0 && level() instanceof net.minecraft.server.level.ServerLevel sl) {
                if (animo > 0) sl.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.78f, 0.06f, 0.06f), 1.1f),
                        getX(), getY() + getBbHeight() * 0.55, getZ(), 2, getBbWidth() * 0.4, getBbHeight() * 0.25, getBbWidth() * 0.4, 0.0);
                else sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE, getX(), getY() + getBbHeight() + 0.2, getZ(), 1, 0.15, 0.05, 0.15, 0.005);
            }
        }
        if (efimero > 0 && getTarget() == null && accion() == 0 && ++sinPresaEfimero > efimero) {
            disolver();
            return;
        } else if (getTarget() != null) sinPresaEfimero = 0;
        int a = accion();
        if (a != 0) {
            accionT++;
            // quieto mientras dura (los pasos de cada golpe los da alAvanzar)
            setDeltaMovement(getDeltaMovement().multiply(0.6, 1, 0.6));
            int[] g = golpes()[a];
            for (int k = 0; k < g.length; k++) {
                if (g[k] - 2 == accionT && silbido(a) != null) playSound(silbido(a), 0.8f, 0.75f + random.nextFloat() * 0.2f);
                // el golpe barre tres ticks: el suyo y los dos siguientes
                if (accionT >= g[k] && accionT <= g[k] + 2) golpear(a, k, accionT == g[k]);
            }
            alAvanzar(a, accionT);
            if (a == accion() && accionT >= ticks()[a]) terminar();
            return;
        }
        // lo despierta su presa, o un golpe
        if (postura() != DE_PIE) {
            setYRot(hogarGiro);
            yBodyRot = yHeadRot = hogarGiro;
            setDeltaMovement(0, getDeltaMovement().y, 0);
            if (getTarget() != null || hurtTime > 0) {
                int d = accionDespertar(postura());
                if (d != 0) {
                    despertando = true;
                    empezar(d);
                } else postura(DE_PIE);
            }
            return;
        }
        // en su puesto y sin presa: vuelve a mirar a donde miraba, se cura y, pasado un rato, vuelve a esperar como esperaba
        if (hogar != null && getTarget() == null && hogar.distToCenterSqr(getX(), getY(), getZ()) < 2.2 * 2.2 && getNavigation().isDone()) {
            enCasa++;
            if (ronda <= 0) {
                float y = Mth.approachDegrees(getYRot(), hogarGiro, 6f);
                setYRot(y);
                yBodyRot = y;
            }
            if (enCasa % 10 == 0 && getHealth() < getMaxHealth()) heal(2f);
            if (enCasa > 160 && hogarPostura == REZA && level().getNearestPlayer(this, 12) == null) postura(REZA);
        } else enCasa = 0;
    }

    private void golpear(int a, int n, boolean primero) {
        Golpe g = golpe(a, n);
        if (g == null) return;
        if (primero) tocados.clear();
        Vec3 dir = Vec3.directionFromRotation(0, getYRot());
        double cosMin = Math.cos(Math.toRadians(g.arco() / 2.0));
        AABB caja = getBoundingBox().inflate(g.alcance() + 1.0, 1.2, g.alcance() + 1.0);
        for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, caja, e -> e != this && e.isAlive() && objetivoValido(e))) {
            if (tocados.contains(le.getId())) continue;
            double dx = le.getX() - getX(), dz = le.getZ() - getZ(), d = Math.sqrt(dx * dx + dz * dz);
            if (d > g.alcance() + le.getBbWidth() * 0.5) continue;
            if (le.getY() > getY() + getBbHeight() + 0.6 || le.getY() + le.getBbHeight() < getY() - 0.6) continue;
            if (d > 0.8 && (dx * dir.x + dz * dir.z) / d < cosMin) continue;
            if (!hasLineOfSight(le) || !pilla(a, n, le)) continue;
            tocados.add(le.getId());
            float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * g.dano();
            if (!le.hurt(damageSources().mobAttack(this), dano)) continue;
            if (g.empuje() > 0) le.knockback(g.empuje(), -dir.x, -dir.z);
            setLastHurtMob(le);
            alGolpear(le, a);
        }
    }

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        // las flechas de los arqueros y ballesteros no hacen nada a los suyos (en la puerta disparan por encima de la pelea)
        if (s.getEntity() instanceof Enemigo && s.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile) return false;
        boolean ok = super.hurt(s, cuanto);
        // el que recibe un golpe avisa a los que tiene al lado (no a toda la calle)
        if (ok && !level().isClientSide && s.getEntity() instanceof LivingEntity quien && isAlive() && objetivoValido(quien)) {
            if (getTarget() == null && !lejosDeCasa(quien)) setTarget(quien);
            for (Enemigo e : level().getEntitiesOfClass(Enemigo.class, getBoundingBox().inflate(7, 3, 7), o -> o != this && o.isAlive() && o.getTarget() == null))
                if (e.hasLineOfSight(this) && !e.lejosDeCasa(quien)) e.setTarget(quien);
        }
        return ok;
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity e) {
        return false;   // no pegan por tocar: solo con sus golpes
    }

    // ------------------------------------------------------------------ morir: su animacion, no la voltereta de Minecraft

    @Override
    public void die(DamageSource s) {
        super.die(s);
        if (!level().isClientSide) {
            int m = accionMorir();
            if (m != 0) {
                entityData.set(ACCION, (byte) m);
                entityData.set(SERIE, (byte) (entityData.get(SERIE) + 1));
            } else entityData.set(ACCION, (byte) 0);
        }
    }

    @Override
    protected void tickDeath() {
        int m = accionMorir();
        if (m == 0) {
            super.tickDeath();
            return;
        }
        ++deathTime;
        if (deathTime >= ticks()[m] + 14 && !level().isClientSide() && !isRemoved()) {
            level().broadcastEntityEvent(this, (byte) 60);
            remove(RemovalReason.KILLED);
        }
    }

    /** Si esta haciendo su animacion de morir (el modelo no pone nada mas debajo). */
    public boolean muriendo() {
        return deathTime > 0 && accionMorir() != 0;
    }

    private void cliente() {
        reloj.startIfStopped(tickCount);
        guardiaO = guardia;
        guardia += ((isAggressive() ? 1f : 0f) - guardia) * 0.22f;
        int s = entityData.get(SERIE);
        if (s != serieVista) {
            serieVista = s;
            int a = accion();
            for (AnimationState st : acciones) st.stop();
            if (a > 0 && a < acciones.length) acciones[a].start(tickCount);
            accionDesde = tickCount;
        } else if (accion() == 0) {
            for (AnimationState st : acciones) st.stop();
        }
    }

    /** En el cliente: el tick en que empezo la accion que se esta viendo (-1 si ninguna). */
    public int accionDesde = -1;

    /** La accion que se esta viendo en el cliente (0 = ninguna). */
    public int accionVista() {
        int a = accion();
        return a > 0 && a < acciones.length && acciones[a].isStarted() ? a : 0;
    }

    // ------------------------------------------------------------------ no se van, no se guardan los de un puesto

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return puesto < 0 && efimero == 0 && super.shouldBeSaved();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        t.putByte("variante", (byte) variante());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        if (t.contains("variante")) variante(t.getByte("variante"));
    }

    @Override
    public int getMaxHeadYRot() {
        return 50;
    }

    @Override
    protected boolean shouldDropLoot() {
        return true;
    }

    @Override
    protected float getSoundVolume() {
        return 0.9f;
    }
}
