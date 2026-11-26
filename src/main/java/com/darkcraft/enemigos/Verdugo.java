package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.net.Net;
import com.darkcraft.rey.Voces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
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
import java.util.List;

/**
 * Maese Odran, el Verdugo de la Arboleda (parte 7h): el jefe del Bosque Muerto. El verdugo del libro perdido 19 ("alguien
 * tiene que llevar la cuenta... y el arbol todavia tiene sitio"), muerto y cosido, con su capucha, su hacha y su lazo. Espera
 * de pie en el patibulo de la Arboleda de los Ahorcados ({@code morvain/Arboleda}), apoyado en el hacha y mirando al Arbol de
 * la Horca.
 * <ul>
 * <li>al ver a alguien dice su frase y, al acabarla, ruge y <b>salta</b> del patibulo sobre su presa (cae con el hacha por
 * delante: lo de alrededor de donde cae, y una onda por el suelo); despues salta tambien a quien se aleja;</li>
 * <li>el <b>hachazo</b> de arriba abajo (se clava y la tierra se abre delante: saltando o rodando se libra), el
 * <b>barrido</b> a media altura (no se salta: se rueda o se aleja uno) y el <b>doble</b> tajo en cruz avanzando;</li>
 * <li>el <b>lazo</b> a quien esta lejos: si le entra (rodar o cubrirse libran), tira de el hasta sus pies y enseguida la
 * <b>ejecucion</b>, el hachazo rapido;</li>
 * <li>el <b>pisoton</b> a quien se le pone detras o pegado;</li>
 * <li>a <b>media vida</b> grita y <b>despierta a los colgados</b>: los que siguen colgando cerca se descuelgan sobre los
 * jugadores y, si no hay bastantes, se alzan otros de la tierra (dos, uno mas por cada amigo, hasta cuatro; esos son
 * efimeros: se deshacen si la pelea se acaba).</li>
 * </ul>
 * Habla (ElevenLabs, en ingles con subtitulos; {@link Voces#BASE_VERDUGO}): 1 al empezar (no la repite en 4 min; hasta
 * acabarla no salta ni ataca), 2 al despertar a los colgados, 3 si cae su presa, 4 la primera vez que engancha a alguien y 5
 * al morir. Al caer (lo reparte {@code valdrenn/Guardianes}): el cartel con sus ecos, la hazana {@code verdugo} y el
 * bestiario; y la cuenta se cierra: los colgados de la arboleda descansan ({@code morvain/Arboleda#liberar}).
 */
public class Verdugo extends Guardian {
    public static final int ECOS = 8000;
    public static final int VOZ_BASE = Voces.BASE_VERDUGO;
    /** Los ticks del salto en que despega y en que cae (el golpe de la caida es el siguiente). */
    public static final int DESPEGA = 9, ATERRIZA = VerdugoAnims.GOLPES[VerdugoAnims.SALTO][0] - 1;
    /** Entre que distancias lanza el lazo, y lo lejos que salta como mucho. */
    public static final double LAZO_MIN = 5.0, LAZO_MAX = 12.0, SALTO_MAX = 11.0;

    /** A quien lleva el lazo (el id; -1 = a nadie): el cliente dibuja la soga hasta el. */
    private static final EntityDataAccessor<Integer> LAZO = SynchedEntityData.defineId(Verdugo.class, EntityDataSerializers.INT);

    /** La ultima vez que dijo su frase de entrada (tiempo del mundo): no la repite en cada intento. */
    private static long ultimaEntrada = Long.MIN_VALUE / 2;

    private int lazoEspera = 50, saltoEspera, pisotonEspera, hablaHasta, esperaRugido, repetido, ultimo;
    private boolean llamo, lazoDicho;
    private LivingEntity ultimaPresa, enganchado;
    private final List<Ahorcado> suyos = new ArrayList<>();
    /** El salto: de donde a donde (null = no salta a ningun sitio: cae donde esta) y lo alto del arco. */
    private Vec3 saltoDe, saltoA;
    private double saltoAlto;
    private boolean volando;
    /** Para las pruebas: saltos (con sitio donde caer), lazos que han enganchado, ejecuciones, ondas, y los colgados que ha
     * despertado y los que ha alzado de la tierra. */
    private int saltos, lazos, ejecuciones, ondas, descolgados, alzados;

    public Verdugo(EntityType<? extends Verdugo> t, Level l) {
        super(t, l, "verdugo", BossEvent.BossBarColor.RED);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 820.0).add(Attributes.ATTACK_DAMAGE, 12.0).add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0).add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(LAZO, -1);
    }

    @Override
    public String tipo() {
        return "verdugo";
    }

    @Override
    public int musica() {
        return 34;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return "verdugo";
    }

    @Override
    protected int[] ticks() {
        return VerdugoAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return VerdugoAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return VerdugoAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return VerdugoAnims.MORIR;
    }

    @Override
    protected int accionRugido() {
        return VerdugoAnims.GRITO;
    }

    @Override
    protected int accionLevantarse() {
        return VerdugoAnims.LEVANTARSE;
    }

    // ------------------------------------------------------------------ para las pruebas

    public int saltos() {
        return saltos;
    }

    public int lazos() {
        return lazos;
    }

    public int ejecuciones() {
        return ejecuciones;
    }

    public int ondas() {
        return ondas;
    }

    public int descolgados() {
        return descolgados;
    }

    public int alzados() {
        return alzados;
    }

    /** Cuantos de los que ha alzado de la tierra siguen en pie. */
    public int suyos() {
        int n = 0;
        for (Ahorcado a : suyos) if (a.isAlive() && !a.isRemoved()) n++;
        return n;
    }

    /** Para las pruebas: pasa ya a media vida (ruge y despierta a los colgados), sin esperar a que acabe lo que hace. */
    public void mediaVida() {
        if (level().isClientSide || !isAlive()) return;
        setHealth(Math.min(getHealth(), getMaxHealth() * 0.45f));
        segunda = true;
        alSegundaMitad();
        empezar(VerdugoAnims.GRITO);
    }

    /** Si va por el aire (saltando). */
    public boolean volando() {
        return volando;
    }

    /** En el cliente: a quien lleva el lazo (o null). */
    public Entity lazo() {
        int id = entityData.get(LAZO);
        return id < 0 ? null : level().getEntity(id);
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
        // en lo alto (el patibulo): baja de un salto sobre su presa
        if (dy < -1.6 && d > 1.2 && d < SALTO_MAX + 2 && saltoEspera <= 0) {
            saltoEspera = 50;
            return VerdugoAnims.SALTO;
        }
        if (Math.abs(dy) > 3.5) return 0;
        // quien se le pone detras (o se le mete entre las piernas): el pisoton
        if (pisotonEspera <= 0 && (d < 3.4 && detras(presa) || d < 1.7)) {
            pisotonEspera = 50 + random.nextInt(30);
            return VerdugoAnims.PISOTON;
        }
        // a quien se aleja: el lazo (a media vida, mas a menudo) o un salto
        if (d >= LAZO_MIN && d <= LAZO_MAX && lazoEspera <= 0 && random.nextFloat() < (segunda ? 0.65f : 0.45f)) {
            lazoEspera = (segunda ? 110 : 160) + random.nextInt(60);
            return VerdugoAnims.LAZO;
        }
        if (d >= 6.5 && d <= SALTO_MAX && dy < 1.0 && saltoEspera <= 0 && random.nextFloat() < 0.35f) {
            saltoEspera = 140 + random.nextInt(80);
            return VerdugoAnims.SALTO;
        }
        if (d > 4.6) return 0;
        // de cerca: el hachazo, el barrido o el doble (no el mismo tres veces seguidas)
        float r = random.nextFloat();
        int a = r < 0.36f ? VerdugoAnims.HACHAZO : r < 0.7f ? VerdugoAnims.BARRIDO : VerdugoAnims.DOBLE;
        if (a == ultimo) {
            if (++repetido >= 2) {
                a = a == VerdugoAnims.HACHAZO ? VerdugoAnims.BARRIDO : VerdugoAnims.HACHAZO;
                repetido = 0;
            }
        } else repetido = 0;
        ultimo = a;
        return a;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case VerdugoAnims.HACHAZO -> new Golpe(4.4f, 50f, 1.8f, 0.7f);
            case VerdugoAnims.BARRIDO -> new Golpe(4.2f, 220f, 1.25f, 0.9f);
            case VerdugoAnims.DOBLE -> new Golpe(3.9f, 110f, 1.0f, 0.45f);
            case VerdugoAnims.EJECUCION -> new Golpe(3.6f, 80f, 2.2f, 1.3f);
            case VerdugoAnims.PISOTON -> new Golpe(3.0f, 360f, 0.8f, 1.6f);
            // la caida del salto: a lo de alrededor de donde cae
            case VerdugoAnims.SALTO -> new Golpe(2.8f, 360f, 1.5f, 1.0f);
            default -> null;
        };
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = VerdugoAnims.GOLPES[accion];
        switch (accion) {
            case VerdugoAnims.HACHAZO -> {
                if (t == g[0] - 1) empujar(0.35);
                if (t == g[0]) {
                    playSound(DarkCraftMod.SFX_VERDUGO_HACHAZO.get(), 1.8f, 0.92f + random.nextFloat() * 0.12f);
                    onda(3.6, 2.0, 0.6f, 0.4);
                }
            }
            case VerdugoAnims.BARRIDO -> {
                if (t == g[0] - 4) playSound(DarkCraftMod.SFX_VERDUGO_BARRIDO.get(), 1.7f, 0.9f + random.nextFloat() * 0.12f);
                if (t == g[0] - 1) empujar(0.3);
                if (t == g[0]) barrido(3.8, 110);
            }
            case VerdugoAnims.DOBLE -> {
                if (t == g[0] - 4 || t == g[1] - 4) empujar(0.5);
            }
            case VerdugoAnims.LAZO -> lazo(t);
            case VerdugoAnims.EJECUCION -> {
                if (t == g[0]) {
                    playSound(DarkCraftMod.SFX_VERDUGO_HACHAZO.get(), 2.0f, 0.85f);
                    if (level() instanceof ServerLevel sl) {
                        Vec3 d = Vec3.directionFromRotation(0, getYRot());
                        BlockState suelo = level().getBlockState(BlockPos.containing(getX() + d.x * 2.4, getY() - 0.5, getZ() + d.z * 2.4));
                        if (!suelo.isAir())
                            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), getX() + d.x * 2.4, getY() + 0.1, getZ() + d.z * 2.4, 30, 0.5, 0.05, 0.5, 0.15);
                    }
                }
            }
            case VerdugoAnims.PISOTON -> {
                if (t == g[0]) {
                    playSound(DarkCraftMod.SFX_VERDUGO_SALTO.get(), 1.5f, 1.15f);
                    onda(0.0, 3.2, 0.6f, 0.55);
                }
            }
            case VerdugoAnims.SALTO -> saltar(t);
            case VerdugoAnims.GRITO -> {
                if (t == 8) {
                    playSound(DarkCraftMod.SFX_VERDUGO_GRITO.get(), 2.8f, 1.0f);
                    if (level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + getBbHeight() * 0.85, getZ(), 18, 0.5, 0.4, 0.5, 0.03);
                        sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + getBbHeight() * 0.6, getZ(), 24, 2.0, 1.0, 2.0, 0.02);
                    }
                }
                // la de media vida: "arriba, mis ahorcados" y se descuelgan (o se alzan de la tierra)
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
     * Se agacha, salta (tick {@link #DESPEGA}) y cae con el hacha por delante (tick {@link #ATERRIZA}): un arco que lleva
     * hasta poco antes de su presa (como mucho {@link #SALTO_MAX} bloques). Por el aire no choca con nada (asi pasa por
     * encima de la barandilla del patibulo); donde cae tiene que caber.
     */
    private void saltar(int t) {
        if (!(level() instanceof ServerLevel sl)) return;
        if (t == DESPEGA) {
            saltoDe = position();
            saltoA = dondeCae(sl);
            if (saltoA == null) return;
            double h = Math.hypot(saltoA.x - saltoDe.x, saltoA.z - saltoDe.z);
            saltoAlto = 1.5 + 0.4 * Math.max(0, saltoDe.y - saltoA.y) + 0.06 * h;
            volando = true;
            noPhysics = true;
            setNoGravity(true);
            saltos++;
            float giro = (float) Math.toDegrees(Math.atan2(saltoA.z - saltoDe.z, saltoA.x - saltoDe.x)) - 90f;
            if (h > 0.3) {
                setYRot(giro);
                yBodyRot = yHeadRot = giro;
            }
            playSound(DarkCraftMod.SFX_VERDUGO_BARRIDO.get(), 1.4f, 0.7f);
            BlockState bajo = level().getBlockState(blockPosition().below());
            if (!bajo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, bajo), getX(), getY() + 0.1, getZ(), 14, 0.5, 0.05, 0.5, 0.1);
        } else if (t > DESPEGA && t <= ATERRIZA && volando) {
            double s = (t - DESPEGA) / (double) (ATERRIZA - DESPEGA);
            Vec3 p = saltoDe.lerp(saltoA, s).add(0, saltoAlto * 4 * s * (1 - s), 0);
            setPos(p.x, p.y, p.z);
            setDeltaMovement(Vec3.ZERO);
            if (t == ATERRIZA) aterrizar();
        } else if (t == ATERRIZA + 1) {
            // el golpe de la caida (el del hacha lo da golpear): polvo, el ruido y la onda por el suelo
            playSound(DarkCraftMod.SFX_VERDUGO_SALTO.get(), 2.0f, 0.9f + random.nextFloat() * 0.12f);
            onda(0.0, 4.2, 0.5f, 0.45);
        }
    }

    /** Donde caer: poco antes de su presa (en su suelo), si cabe; si no, encima de ella; si tampoco, null. */
    private Vec3 dondeCae(ServerLevel sl) {
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
        double[] cortos = {Mth.clamp(l - 1.6, 2.5, SALTO_MAX), Mth.clamp(l - 0.6, 2.5, SALTO_MAX), Mth.clamp(l - 2.6, 2.0, SALTO_MAX)};
        for (double c : cortos) {
            double x = getX() + dx * c, z = getZ() + dz * c;
            double y = Puestos.suelo(sl, Mth.floor(x), Mth.floor(py + 0.01), Mth.floor(z));
            AABB caja = getBoundingBox().move(x - getX(), y - getY(), z - getZ()).deflate(0.05);
            if (sl.noCollision(this, caja) && !sl.noCollision(this, caja.move(0, -0.6, 0))) return new Vec3(x, y, z);
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

    // ------------------------------------------------------------------ el lazo y la ejecucion

    /** El lazo: lo lanza (tick 12), engancha o no (18) y, si engancha, tira de quien lleva hasta sus pies y lo ejecuta. */
    private void lazo(int t) {
        LivingEntity p = getTarget();
        int g = VerdugoAnims.GOLPES[VerdugoAnims.LAZO][0];
        if (t == 12) {
            playSound(DarkCraftMod.SFX_VERDUGO_LAZO.get(), 1.4f, 0.9f + random.nextFloat() * 0.15f);
            entityData.set(LAZO, p == null ? -1 : p.getId());
        } else if (t == g) {
            enganchado = null;
            if (p != null && p.isAlive() && objetivoValido(p) && hasLineOfSight(p)) {
                double dx = p.getX() - getX(), dz = p.getZ() - getZ(), d = Math.sqrt(dx * dx + dz * dz);
                Vec3 mira = Vec3.directionFromRotation(0, getYRot());
                boolean delante = d < 1.0 || (dx * mira.x + dz * mira.z) / d > Math.cos(Math.toRadians(26));
                // (rodar o cubrirse libran: el golpe no entra)
                if (d <= LAZO_MAX + 1.0 && delante && Math.abs(p.getY() - getY()) < 4.0
                        && p.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.3f)) {
                    enganchado = p;
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, true));
                    lazos++;
                    if (!lazoDicho) {
                        lazoDicho = true;
                        hablaSiCalla(4);
                    }
                }
            }
            if (enganchado == null) entityData.set(LAZO, -1);
        } else if (t > g && enganchado != null) {
            LivingEntity e = enganchado;
            double dx = getX() - e.getX(), dz = getZ() - e.getZ(), d = Math.sqrt(dx * dx + dz * dz);
            if (!e.isAlive()) {
                soltarLazo();
                return;
            }
            // ya lo tiene a los pies (o se acaba el tiron): la ejecucion
            if (d < 2.4 || t >= 34) {
                soltarLazo();
                if (d < 3.6) {
                    girarHacia(e, 180f);
                    ejecuciones++;
                    empezar(VerdugoAnims.EJECUCION);
                }
                return;
            }
            // tira: hacia sus pies, deprisa al principio
            double v = Math.min(0.6, 0.25 + d * 0.05);
            e.setDeltaMovement(dx / d * v, Math.max(e.getDeltaMovement().y, 0.06), dz / d * v);
            e.hurtMarked = true;
            e.hasImpulse = true;
        }
    }

    private void soltarLazo() {
        enganchado = null;
        if (!level().isClientSide) entityData.set(LAZO, -1);
    }

    // ------------------------------------------------------------------ el barrido y las ondas

    /** Lo que levanta el hacha al barrer: hojas secas y tierra a media altura, en un arco delante. */
    private void barrido(double r, double medio) {
        if (!(level() instanceof ServerLevel sl)) return;
        BlockState suelo = level().getBlockState(BlockPos.containing(getX(), getY() - 0.5, getZ()));
        for (int k = 0; k <= 12; k++) {
            double a = Math.toRadians(getYRot() + 90 - medio + 2 * medio * k / 12.0);
            double x = getX() + Math.cos(a) * r, z = getZ() + Math.sin(a) * r;
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, x, getY() + 1.2, z, 1, 0, 0, 0, 0);
            if (!suelo.isAir() && k % 2 == 0) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), x, getY() + 0.3, z, 4, 0.3, 0.2, 0.3, 0.1);
        }
    }

    /**
     * El hacha (o el pie, o la caida) contra el suelo: tierra, raices y una onda (a {@code delante} bloques, de radio
     * {@code r}) que alcanza a quien este de pie (saltando o rodando se libra; a quien ya le ha dado el golpe, no).
     */
    private void onda(double delante, double r, float mult, double arriba) {
        if (!(level() instanceof ServerLevel sl)) return;
        ondas++;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * delante, z = getZ() + d.z * delante;
        BlockState suelo = level().getBlockState(BlockPos.containing(x, getY() - 0.5, z));
        int n = (int) Math.round(r * 14);
        if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), x, getY() + 0.1, z, n, r * 0.4, 0.05, r * 0.4, 0.25);
        sl.sendParticles(ParticleTypes.EXPLOSION, x, getY() + 0.3, z, 1, r * 0.3, 0, r * 0.3, 0);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, x, getY() + 0.2, z, n / 2, r * 0.4, 0.1, r * 0.4, 0.02);
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

    // ------------------------------------------------------------------ los colgados: se descuelgan o se alzan de la tierra

    private void llamar() {
        if (!(level() instanceof ServerLevel sl)) return;
        List<ServerPlayer> cerca = new ArrayList<>();
        for (ServerPlayer p : sl.players()) if (p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) < 40 * 40) cerca.add(p);
        int n = Math.max(2, Math.min(4, 1 + cerca.size()));
        // primero, los que siguen colgando cerca: se descuelgan sobre el jugador que tengan mas cerca
        List<Ahorcado> colgados = sl.getEntitiesOfClass(Ahorcado.class, getBoundingBox().inflate(28, 14, 28), a -> a.isAlive() && a.cuelga());
        colgados.sort(Comparator.comparingDouble(a -> a.distanceToSqr(this)));
        int despiertan = 0;
        for (Ahorcado a : colgados) {
            if (despiertan >= n) break;
            LivingEntity t = null;
            double mejor = Double.MAX_VALUE;
            for (ServerPlayer p : cerca)
                if (objetivoValido(p) && p.distanceToSqr(a) < mejor) {
                    mejor = p.distanceToSqr(a);
                    t = p;
                }
            if (t == null) t = getTarget();
            if (t != null && a.acudir(t)) {
                despiertan++;
                sl.sendParticles(ParticleTypes.SOUL, a.getX(), a.getY() + 2.4, a.getZ(), 8, 0.2, 0.4, 0.2, 0.02);
            }
        }
        descolgados += despiertan;
        // los que falten: se alzan de la tierra de la arboleda alrededor de el
        int salen = 0;
        double giro = random.nextDouble() * Math.PI * 2;
        for (int k = 0; k < 16 && despiertan + salen < n; k++) {
            double a = giro + k * (Math.PI * 2 / 5) + (k >= 5 ? 0.4 * (k / 5) : 0);
            double r = 4.5 + (k % 3) * 1.2;
            int bx = Mth.floor(getX() + Math.cos(a) * r), bz = Mth.floor(getZ() + Math.sin(a) * r);
            double y = Puestos.suelo(sl, bx, Mth.floor(getY()) + 1, bz);
            if (Math.abs(y - getY()) > 2.2) continue;
            Ahorcado m = DarkCraftMod.AHORCADO.get().create(sl);
            if (m == null) return;
            float mira = (float) Math.toDegrees(Math.atan2(getZ() - (bz + 0.5), getX() - (bx + 0.5))) - 90f;
            m.moveTo(bx + 0.5, y, bz + 0.5, mira, 0f);
            if (!sl.noCollision(m)) continue;
            m.variante(salen % 2 == 0 ? Ahorcado.GARRAS : Ahorcado.SOGA);
            // tumbados en la tierra: se alzan al ver a su presa (sin puesto: no vuelven ni se guardan)
            m.asignar(null, -1, BlockPos.containing(bx + 0.5, y, bz + 0.5), mira, TUMBADO, 0);
            m.efimero(200);
            sl.addFreshEntity(m);
            LivingEntity t = getTarget() != null ? getTarget()
                    : sl.getNearestPlayer(m.getX(), m.getY(), m.getZ(), 30, e -> e instanceof Player q && objetivoValido(q));
            if (t != null) m.setTarget(t);
            BlockState suelo = sl.getBlockState(BlockPos.containing(bx + 0.5, y - 0.5, bz + 0.5));
            if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), bx + 0.5, y + 0.1, bz + 0.5, 24, 0.5, 0.05, 0.5, 0.15);
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, bx + 0.5, y + 0.4, bz + 0.5, 16, 0.45, 0.5, 0.45, 0.015);
            sl.sendParticles(ParticleTypes.SOUL, bx + 0.5, y + 0.3, bz + 0.5, 10, 0.4, 0.3, 0.4, 0.02);
            sl.playSound(null, bx + 0.5, y, bz + 0.5, DarkCraftMod.SFX_EMBOSCADA_ALZARSE.get(), SoundSource.HOSTILE, 1.3f, 0.75f + random.nextFloat() * 0.2f);
            suyos.add(m);
            salen++;
        }
        alzados += salen;
        DarkCraftMod.LOGGER.info("[DarkCraft] El Verdugo despierta a los colgados: {} se descuelgan, {} se alzan ({} jugadores)", despiertan, salen, cerca.size());
    }

    private void despedir() {
        for (Ahorcado a : suyos) if (a.isAlive() && !a.isRemoved()) a.disolver();
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
        llamo = lazoDicho = false;
        lazoEspera = 50;
        saltoEspera = 0;
        repetido = ultimo = 0;
        long ahora = level().getGameTime();
        if (ahora - ultimaEntrada > 20 * 60 * 4 || ahora < ultimaEntrada) {
            ultimaEntrada = ahora;
            habla(1);
            // se yergue mientras lo dice y no ruge ni salta hasta acabarlo (se queda en el patibulo mirando a su presa)
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
        soltarLazo();
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
            // el brillo de los ojos bajo la capucha (unas chispas que se apagan) mientras pelea
            if (isAlive() && enPelea() && tickCount % 9 == 0) {
                Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
                level().addParticle(ParticleTypes.SMALL_FLAME, getX() + d.x * 0.35, getY() + getBbHeight() * 0.86, getZ() + d.z * 0.35, 0, 0.01, 0);
            }
            return;
        }
        if (!isAlive()) return;
        if (lazoEspera > 0) lazoEspera--;
        if (saltoEspera > 0) saltoEspera--;
        if (pisotonEspera > 0) pisotonEspera--;
        // si se le corta el salto o el lazo (no deberia: no se le aturde), aterriza y suelta
        if (volando && accion() != VerdugoAnims.SALTO) aterrizar();
        if (enganchado != null && accion() != VerdugoAnims.LAZO) soltarLazo();
        else if (enganchado == null && accion() != VerdugoAnims.LAZO && entityData.get(LAZO) >= 0) entityData.set(LAZO, -1);
        // ha caido aquel con quien peleaba
        if (enPelea() && getTarget() == null && ultimaPresa != null && ultimaPresa.isDeadOrDying()) {
            ultimaPresa = null;
            hablaSiCalla(3);
        }
    }

    @Override
    protected void alSegundaMitad() {
        lazoEspera = Math.min(lazoEspera, 30);
    }

    @Override
    public void die(DamageSource s) {
        boolean ya = !isAlive() && deathTime > 0;
        if (volando) aterrizar();
        soltarLazo();
        super.die(s);
        if (level().isClientSide || ya) return;
        habla(5);
        despedir();
    }

    @Override
    public void remove(RemovalReason r) {
        if (!level().isClientSide) despedir();
        super.remove(r);
    }

    /** Mientras dice su primera frase se queda donde esta (en el patibulo, mirando a su presa); despues, de cerca. */
    @Override
    protected double distanciaDeseada() {
        return enPelea() && tickCount < esperaRugido ? 99.0 : 3.4;
    }

    @Override
    protected double prisa() {
        return segunda ? 1.25 : 1.1;
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (accion) {
            case VerdugoAnims.GRITO, VerdugoAnims.LAZO -> 6;
            case VerdugoAnims.EJECUCION -> 16 + random.nextInt(10);
            case VerdugoAnims.SALTO -> 10 + random.nextInt(8);
            case VerdugoAnims.PISOTON -> 6 + random.nextInt(6);
            default -> (segunda ? 8 : 14) + random.nextInt(12);
        };
    }

    /** Desde lo alto del patibulo (tres bloques) tambien elige: salta. */
    @Override
    protected double alturaMaxima() {
        return 4.5;
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case VerdugoAnims.HACHAZO, VerdugoAnims.DOBLE, VerdugoAnims.EJECUCION -> DarkCraftMod.SFX_GOLPE_PESADO.get();
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
        playSound(t.getStepSound(), t.getVolume() * 0.9f, t.getPitch() * 0.6f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_VERDUGO_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_VERDUGO_MUERTE.get();
    }

    @Override
    public boolean causeFallDamage(float d, float m, DamageSource s) {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_2.get(), 2));
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_3.get()));
        spawnAtLocation(new ItemStack(DarkCraftMod.CENIZAS.get(com.darkcraft.combate.Habilidad.SANGRE).get()));
    }

    /** Para las pruebas y el comando: como esta. */
    public String estado() {
        return String.format(java.util.Locale.ROOT, "vida %.0f/%.0f, %s, %s en %.1f,%.1f,%.1f; saltos %d, lazos %d, ejecuciones %d, ondas %d, descolgados %d, alzados %d",
                getHealth(), getMaxHealth(), enPelea() ? "peleando" : "esperando", postura() == DE_PIE ? "de pie" : "apoyado en el hacha",
                getX(), getY(), getZ(), saltos, lazos, ejecuciones, ondas, descolgados, alzados);
    }
}
