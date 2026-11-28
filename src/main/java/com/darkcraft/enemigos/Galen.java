package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.Lazareto;
import com.darkcraft.net.Net;
import com.darkcraft.rey.Voces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Maese Galen, el fisico del Lazareto (parte 7av): el jefe de la Sala del Elixir. Cuando la Peste Gris llego al Pantano Podrido se
 * llevo a los enfermos a su torre de la laguna y les dio a beber lo que destilaba (libro perdido 38, "Notas de Maese Galen"): no
 * curaba, les cambiaba (los {@link Destilado}). La noche del cielo rojo hirvieron los alambiques y el se conecto a su alambique de
 * la espalda; sigue destilando junto al gran alambique, esperando la ultima gota.
 * <ul>
 * <li>espera <b>destilando</b> ({@code REZA}) al oeste del gran alambique, de cara a el, con la jeringa al trasluz; al ver a alguien
 * en la Sala del Elixir se vuelve mientras dice su frase y al acabarla <b>se prepara</b> (su rugido: alza la jeringa, le da dos
 * golpecitos con el bisturi y suelta un chorrito);</li>
 * <li>la <b>jeringa</b> (la echa atras y se lanza a fondo: envenena y le devuelve algo de vida —le chupa la sangre—), el
 * <b>bisturi</b> (dos tajos: cada uno suma sangrado, {@link Sangrado}), la <b>lluvia de frascos</b> (tres de fuego o de acido, que
 * caen por donde esta su presa: {@link Frasco}; bebido, cinco) y el <b>vapor</b> (gira la llave de su alambique: una marca en el
 * suelo bajo cada uno y alguna mas; al rato, una columna de vapor hirviendo que quema y lanza por el aire: sal de la marca);</li>
 * <li>a <b>media vida</b> se <b>bebe el elixir</b> ({@code BEBER}, {@code alSegundaMitad}): se arranca los tubos del cuello, se bebe
 * el frasco grande del cinto y el cristal le brota por todo el cuerpo (la variante {@link #BEBIDO}): desde entonces va mas deprisa,
 * echa cinco frascos, mas marcas de vapor, el <b>estallido</b> (se encoge y el cristal le revienta por fuera: a quien este a
 * {@link #R_ESTALLIDO}) y el gran alambique hierve y suelta vapor por su base (a quien se le arrime).</li>
 * </ul>
 * Habla (ElevenLabs, en ingles con subtitulos; {@link Voces#BASE_GALEN}): 1 al empezar (no la repite en 4 min), 2 al beberse el
 * elixir, 3 si cae su presa, 4 el primer vapor y 5 al caer. Al caer se queda de rodillas tendiendo la mano al gran alambique, que
 * deja de hervir, y se rompe en cristal verde; lo demas (el cartel con sus ecos, la hazana {@code galen} y el bestiario) lo reparte
 * {@code valdrenn/Guardianes}; los Destilados descansan y ya no vuelven ({@code Lazareto.paz}). Deja su jeringa
 * ({@link com.darkcraft.armas.JeringaFisico}).
 */
public class Galen extends Guardian {
    public static final int ECOS = 17000;
    public static final int VOZ_BASE = Voces.BASE_GALEN;
    /** La variante de despues de beberse el elixir (el cristal le brota; lo dibuja el cliente). */
    public static final int BEBIDO = 1;
    /** Si se ha arrancado los tubos (en cuanto se los arranca, antes del cristal). Lo sabe el cliente. */
    private static final EntityDataAccessor<Boolean> ARRANCADOS = SynchedEntityData.defineId(Galen.class, EntityDataSerializers.BOOLEAN);
    /** De la marca a la columna de vapor (ticks), el radio de la columna y lo alto que llega; el estallido; el vapor del gran alambique. */
    public static final int MARCA_T = 26;
    public static final double R_VAPOR = 1.5, VAPOR_ALTO = 4.0, R_ESTALLIDO = 4.2, R_VENTEO = 3.9;
    /** Los frascos de cada lluvia (bebido, mas) y cada cuanto suelta vapor el gran alambique (bebido). */
    public static final int FRASCOS = 3, FRASCOS_BEBIDO = 5, VENTEO_T = 100;
    /** Lo que le devuelve la jeringa al clavarla (de vida). */
    public static final float DRENA = 8f, DRENA_BEBIDO = 12f;

    public static final DustParticleOptions VAPOR = new DustParticleOptions(new Vector3f(0.92f, 0.95f, 0.93f), 1.8f),
            ELIXIR = new DustParticleOptions(new Vector3f(0.36f, 0.94f, 0.52f), 1.1f),
            MARCA = new DustParticleOptions(new Vector3f(0.78f, 0.88f, 0.82f), 1.4f);
    public static final BlockParticleOption CRISTAL = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.LIME_STAINED_GLASS.defaultBlockState());

    /** La ultima vez que dijo su frase de entrada (tiempo del mundo): no la repite en cada intento. */
    private static long ultimaEntrada = Long.MIN_VALUE / 2;

    private record Marca(double x, double y, double z, int cuando) {}

    private final List<Marca> marcas = new ArrayList<>();
    private int frascosEspera = 60, vaporEspera = 90, estallidoEspera = 40, hablaHasta, esperaRugido, alambiqueSuena, venteo = VENTEO_T, frascoTipo;
    private boolean cuatro;
    private LivingEntity ultimaPresa;
    /** Para las pruebas. */
    private int jeringazos, envenenados, bisturis, frascosLanzados, lluvias, vapores, columnas, columnasQueDan, estallidos, estallidosQueDan, venteos,
            venteosQueDan, bebidas;
    private float drenado;
    /** Para las pruebas: no elige golpes por su cuenta (lo que se le manda hacer, si; y lo ya lanzado sigue). */
    public boolean quieta;

    public Galen(EntityType<? extends Galen> t, Level l) {
        super(t, l, "galen", BossEvent.BossBarColor.GREEN);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1150.0).add(Attributes.ATTACK_DAMAGE, 11.0).add(Attributes.ARMOR, 4.0)
                .add(Attributes.ARMOR_TOUGHNESS, 1.0).add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ARRANCADOS, false);
    }

    /** Si se ha arrancado los tubos del cuello (el cliente esconde los tubos y ensena los cabos). */
    public boolean arrancados() {
        return entityData.get(ARRANCADOS);
    }

    /** Si se ha bebido el elixir (a media vida: el cristal le ha brotado). */
    public boolean bebido() {
        return variante() == BEBIDO;
    }

    @Override
    public String tipo() {
        return "galen";
    }

    @Override
    public int musica() {
        return 52;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return "galen";
    }

    @Override
    protected int[] ticks() {
        return GalenAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return GalenAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return GalenAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return GalenAnims.MORIR;
    }

    @Override
    protected int accionRugido() {
        return GalenAnims.PREPARAR;
    }

    @Override
    protected int accionLevantarse() {
        return GalenAnims.VOLVERSE;
    }

    // ------------------------------------------------------------------ para las pruebas

    public int jeringazos() {
        return jeringazos;
    }

    /** Lo que le ha devuelto la jeringa (vida). */
    public float drenado() {
        return drenado;
    }

    public int envenenados() {
        return envenenados;
    }

    /** Los tajos del bisturi que han dado. */
    public int bisturis() {
        return bisturis;
    }

    /** Las lluvias de frascos y los frascos que ha lanzado. */
    public int lluvias() {
        return lluvias;
    }

    public int frascosLanzados() {
        return frascosLanzados;
    }

    /** Las veces que ha soltado el vapor, las columnas que han salido y las que han dado. */
    public int vapores() {
        return vapores;
    }

    public int columnas() {
        return columnas;
    }

    public int columnasQueDan() {
        return columnasQueDan;
    }

    public int marcasPendientes() {
        return marcas.size();
    }

    /** Los estallidos (y a cuantos han dado) y lo que ha venteado el gran alambique (y a cuantos ha dado). */
    public int estallidos() {
        return estallidos;
    }

    public int estallidosQueDan() {
        return estallidosQueDan;
    }

    public int venteos() {
        return venteos;
    }

    public int venteosQueDan() {
        return venteosQueDan;
    }

    /** Las veces que se ha bebido el elixir. */
    public int bebidas() {
        return bebidas;
    }

    /** Para las pruebas: pasa ya a media vida (se bebe el elixir), sin esperar a que acabe lo que hace. */
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

    /** Para las pruebas: que el gran alambique suelte su vapor ya (si se ha bebido el elixir y esta en la sala). */
    public void ventearYa() {
        venteo = 0;
    }

    /** Si esta en la Sala del Elixir (ahi pelea; fuera, en las pruebas, no hay gran alambique). */
    public boolean enSala() {
        return Lazareto.enElixir(getX(), getY(), getZ());
    }

    // ------------------------------------------------------------------ que hace

    @Override
    protected int elegir(LivingEntity presa, double d) {
        boolean b = bebido();
        if (quieta || Math.abs(presa.getY() - getY()) > 5.0) return 0;
        // el vapor: de lejos o de cerca, cada rato
        if (vaporEspera <= 0 && d < 20 && random.nextFloat() < (b ? 0.3f : 0.22f)) {
            vaporEspera = (b ? 170 : 240) + random.nextInt(60);
            return GalenAnims.VAPOR;
        }
        // bebido: rodeado (dos o mas a su alcance) o de vez en cuando, el estallido
        if (b && estallidoEspera <= 0 && d < R_ESTALLIDO + 0.4 && (cercanos(R_ESTALLIDO) >= 2 || random.nextFloat() < 0.4f)) {
            estallidoEspera = 130 + random.nextInt(60);
            return GalenAnims.ESTALLIDO;
        }
        if (frascosEspera <= 0 && d > 4.5 && d < 18 && random.nextFloat() < 0.5f) {
            frascosEspera = (b ? 110 : 150) + random.nextInt(50);
            return GalenAnims.FRASCOS;
        }
        if (d < 2.8) return random.nextFloat() < 0.55f ? GalenAnims.BISTURI : GalenAnims.JERINGA;
        if (d < 4.4) return GalenAnims.JERINGA;
        return 0;
    }

    /** Cuantos de los que pueden ser su presa (jugadores o su presa de ahora) tiene a menos de r. */
    private int cercanos(double r) {
        int n = 0;
        for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(r, 3, r),
                e -> e != this && e.isAlive() && objetivoValido(e) && (e instanceof Player || e == getTarget())))
            if (Math.hypot(le.getX() - getX(), le.getZ() - getZ()) < r) n++;
        return n;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case GalenAnims.JERINGA -> new Golpe(4.4f, 36f, 1.15f, 0.5f);
            case GalenAnims.BISTURI -> new Golpe(3.0f, 110f, 0.7f, 0.4f);
            default -> null;
        };
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (accion == GalenAnims.JERINGA) {
            // le clava la aguja: veneno y le saca sangre (a Galen le devuelve vida)
            jeringazos++;
            if (!le.hasEffect(MobEffects.POISON)) envenenados++;
            le.addEffect(new MobEffectInstance(MobEffects.POISON, 100, bebido() ? 1 : 0), this);
            float antes = getHealth();
            heal(bebido() ? DRENA_BEBIDO : DRENA);
            drenado += getHealth() - antes;
            if (level() instanceof ServerLevel sl) {
                for (int k = 0; k < 8; k++) {
                    double s = k / 8.0;
                    sl.sendParticles(Sangrado.GOTA, Mth.lerp(s, le.getX(), getX()), Mth.lerp(s, le.getY() + le.getBbHeight() * 0.6, getY() + 1.6),
                            Mth.lerp(s, le.getZ(), getZ()), 1, 0.05, 0.05, 0.05, 0.0);
                }
                sl.sendParticles(ELIXIR, le.getX(), le.getY() + le.getBbHeight() * 0.6, le.getZ(), 6, 0.2, 0.3, 0.2, 0.0);
            }
        }
        // el bisturi abre la carne: suma sangrado (tres tajos, hemorragia)
        if (accion == GalenAnims.BISTURI) {
            bisturis++;
            Sangrado.sumar(le, 34f, this);
        }
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = GalenAnims.GOLPES[accion];
        switch (accion) {
            case GalenAnims.JERINGA -> {
                if (t == g[0] - 9) playSound(DarkCraftMod.SFX_GALEN_JERINGA.get(), 1.8f, 0.95f + random.nextFloat() * 0.1f);
                if (t == g[0] - 3) empujar(0.34);
                if (t == g[0] && level() instanceof ServerLevel sl) {
                    Vec3 p = punta(3.6);
                    sl.sendParticles(ELIXIR, p.x, p.y, p.z, 8, 0.15, 0.15, 0.15, 0.02);
                    sl.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 4, 0.1, 0.1, 0.1, 0.1);
                }
            }
            case GalenAnims.BISTURI -> {
                if (t == g[0] - 4) playSound(DarkCraftMod.SFX_GALEN_BISTURI.get(), 1.6f, 0.95f + random.nextFloat() * 0.1f);
                if ((t == g[0] || t == g[1]) && level() instanceof ServerLevel sl) {
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, getX() + d.x * 1.8, getY() + 1.4, getZ() + d.z * 1.8, 1, 0, 0, 0, 0);
                }
            }
            case GalenAnims.FRASCOS -> {
                if (t == 4) playSound(DarkCraftMod.SFX_GALEN_FRASCOS.get(), 1.6f, 1.0f);
                int n = bebido() ? FRASCOS_BEBIDO : FRASCOS;
                if (t >= g[0] && t < g[0] + 3 * n && (t - g[0]) % 3 == 0) lanzarFrasco((t - g[0]) / 3, n);
            }
            case GalenAnims.VAPOR -> {
                if (t == 4 && !cuatro) {
                    cuatro = true;
                    hablaSiCalla(4);
                }
                // la llave del alambique: un silbido de vapor a su espalda
                if (t == 10 && level() instanceof ServerLevel sl) {
                    playSound(SoundEvents.FIRE_EXTINGUISH, 1.0f, 0.6f);
                    Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
                    sl.sendParticles(ParticleTypes.CLOUD, getX() - d.x * 0.6, getY() + 2.8, getZ() - d.z * 0.6, 10, 0.2, 0.3, 0.2, 0.04);
                }
                if (t == g[0]) soltarVapor();
            }
            case GalenAnims.ESTALLIDO -> {
                if (t == g[0] - 8) playSound(SoundEvents.AMETHYST_CLUSTER_BREAK, 1.4f, 0.7f);
                if (t > 4 && t < g[0] && t % 3 == 0 && level() instanceof ServerLevel sl)
                    sl.sendParticles(ELIXIR, getX(), getY() + 1.6, getZ(), 4, 0.5, 0.7, 0.5, 0.0);
                if (t == g[0]) estallar();
            }
            case GalenAnims.PREPARAR -> {
                // los golpecitos y el chorrito de la aguja
                if ((t == 16 || t == 20) && level() instanceof ServerLevel sl) {
                    playSound(SoundEvents.GLASS_HIT, 0.9f, 1.6f);
                    Vec3 p = mano();
                    sl.sendParticles(ParticleTypes.CRIT, p.x, p.y + 0.6, p.z, 2, 0.05, 0.05, 0.05, 0.02);
                }
                if (t == g[0] && level() instanceof ServerLevel sl) {
                    Vec3 p = mano();
                    playSound(SoundEvents.BOTTLE_EMPTY, 1.0f, 1.4f);
                    for (int k = 0; k < 6; k++)
                        sl.sendParticles(ELIXIR, p.x, p.y + 1.2 + k * 0.15, p.z, 2, 0.03, 0.05, 0.03, 0.0);
                    sl.sendParticles(ParticleTypes.DRIPPING_WATER, p.x, p.y + 1.6, p.z, 3, 0.05, 0.05, 0.05, 0.0);
                }
            }
            case GalenAnims.BEBER -> beber(t);
            case GalenAnims.VOLVERSE -> {
                // mientras se vuelve, se gira entero hacia quien llega (como mucho 16 grados por tick)
                if (t >= 4 && getTarget() != null) girarHacia(getTarget(), 16f);
            }
            default -> {
            }
        }
    }

    /** La punta de la aguja (mas o menos): {@code lejos} delante, a la altura del pecho. */
    private Vec3 punta(double lejos) {
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        return new Vec3(getX() + d.x * lejos, getY() + 1.6, getZ() + d.z * lejos);
    }

    /** Donde lleva la jeringa alzada (delante, a su derecha, a la altura de las gafas). */
    private Vec3 mano() {
        Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
        return new Vec3(getX() + d.x * 0.7 - d.z * 0.35, getY() + 2.4, getZ() + d.z * 0.7 + d.x * 0.35);
    }

    // ------------------------------------------------------------------ la lluvia de frascos

    /** El frasco k de n: de fuego o de acido, a su presa (el primero) o alrededor de ella (los demas), en arco. */
    private void lanzarFrasco(int k, int n) {
        if (!(level() instanceof ServerLevel sl)) return;
        if (k == 0) {
            lluvias++;
            frascoTipo = random.nextInt(2);
        }
        LivingEntity t = getTarget();
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double ox = getX() + d.x * 0.6 + d.z * 0.4, oz = getZ() + d.z * 0.6 - d.x * 0.4, oy = getY() + getBbHeight() * 0.85;
        double tx, ty, tz;
        if (t == null) {
            tx = ox + d.x * 9;
            tz = oz + d.z * 9;
            ty = getY();
        } else {
            Vec3 v = t.getDeltaMovement();
            tx = t.getX() + v.x * 6;
            tz = t.getZ() + v.z * 6;
            ty = t.getY() + 0.1;
        }
        if (k > 0) {
            // alrededor de la presa, repartidos (que haya que salir de alli)
            double a = (k - 1) * Math.PI * 2 / Math.max(1, n - 1) + random.nextDouble() * 0.6, r = 1.8 + random.nextDouble() * 1.4;
            tx += Math.cos(a) * r;
            tz += Math.sin(a) * r;
        }
        Frasco f = new Frasco(level(), this, (frascoTipo + k) % 2 == 0 ? Frasco.FUEGO : Frasco.ACIDO);
        f.setPos(ox, oy, oz);
        f.setDeltaMovement(Cantaro.tiro(ox, oy, oz, tx, ty, tz));
        sl.addFreshEntity(f);
        frascosLanzados++;
        playSound(DarkCraftMod.SFX_FRASCO_LANZAR.get(), 0.9f, 0.85f + random.nextFloat() * 0.3f);
    }

    // ------------------------------------------------------------------ el vapor

    /** Gira la llave: una marca bajo los pies de cada uno (bebido, otra a su lado) y alguna mas; al rato, la columna de vapor. */
    private void soltarVapor() {
        if (!(level() instanceof ServerLevel sl)) return;
        vapores++;
        List<LivingEntity> blancos = new ArrayList<>(sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(24, 8, 24),
                e -> e != this && e.isAlive() && objetivoValido(e) && (e instanceof Player || e == getTarget())));
        for (LivingEntity le : blancos) {
            if (le.distanceToSqr(this) > 26 * 26) continue;
            marcar(sl, le.getX(), le.getZ(), le.getY());
            if (bebido()) {
                double a = random.nextDouble() * Math.PI * 2, r = 2.2 + random.nextDouble() * 1.4;
                marcar(sl, le.getX() + Math.cos(a) * r, le.getZ() + Math.sin(a) * r, le.getY());
            }
        }
        int extra = bebido() ? 4 : 2;
        for (int i = 0; i < extra; i++) {
            double a = random.nextDouble() * Math.PI * 2, r = 3.0 + random.nextDouble() * 5;
            marcar(sl, getX() + Math.cos(a) * r, getZ() + Math.sin(a) * r, getY());
        }
    }

    private void marcar(ServerLevel sl, double x, double z, double cerca) {
        double y = Puestos.suelo(sl, Mth.floor(x), Mth.floor(cerca) + 2, Mth.floor(z));
        if (Math.abs(y - cerca) > 4) y = cerca;
        marcas.add(new Marca(x, y, z, tickCount + MARCA_T));
    }

    /** Cada tick: las marcas silban (el suelo humea) y, a su hora, la columna de vapor hirviendo. */
    private void tickMarcas(ServerLevel sl) {
        for (int i = marcas.size() - 1; i >= 0; i--) {
            Marca m = marcas.get(i);
            if (tickCount < m.cuando()) {
                if (tickCount % 2 == 0)
                    for (int k = 0; k < 10; k++) {
                        double a = (k / 10.0 + tickCount * 0.02) * Math.PI * 2;
                        sl.sendParticles(MARCA, m.x() + Math.cos(a) * R_VAPOR, m.y() + 0.08, m.z() + Math.sin(a) * R_VAPOR, 1, 0, 0, 0, 0);
                    }
                if (tickCount % 3 == 0) sl.sendParticles(ParticleTypes.CLOUD, m.x(), m.y() + 0.1, m.z(), 2, 0.4, 0.02, 0.4, 0.01);
                if (m.cuando() - tickCount == 8)
                    sl.playSound(null, m.x(), m.y(), m.z(), SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 0.6f, 1.4f);
                continue;
            }
            marcas.remove(i);
            columna(sl, m);
        }
    }

    /** La columna de vapor: a quien pilla dentro, dano, le lanza por el aire y le escalda (lentitud). */
    private void columna(ServerLevel sl, Marca m) {
        columnas++;
        sl.playSound(null, m.x(), m.y(), m.z(), DarkCraftMod.SFX_GALEN_VAPOR.get(), SoundSource.HOSTILE, 1.4f, 0.9f + random.nextFloat() * 0.2f);
        for (int k = 0; k < 8; k++)
            sl.sendParticles(ParticleTypes.CLOUD, m.x(), m.y() + 0.3 + k * 0.5, m.z(), 6, 0.35 + k * 0.04, 0.2, 0.35 + k * 0.04, 0.06);
        sl.sendParticles(VAPOR, m.x(), m.y() + 1.6, m.z(), 30, 0.6, 1.4, 0.6, 0.02);
        sl.sendParticles(ParticleTypes.POOF, m.x(), m.y() + 0.4, m.z(), 12, 0.5, 0.1, 0.5, 0.06);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.8f;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, new AABB(m.x() - 2, m.y() - 0.5, m.z() - 2, m.x() + 2, m.y() + VAPOR_ALTO, m.z() + 2),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            if (le.distanceToSqr(m.x(), le.getY(), m.z()) > R_VAPOR * R_VAPOR) continue;
            if (le.hurt(damageSources().mobAttack(this), dano)) {
                columnasQueDan++;
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1), this);
                le.setDeltaMovement(le.getDeltaMovement().add(0, 0.62, 0));
                le.hurtMarked = true;
            }
        }
    }

    // ------------------------------------------------------------------ el estallido (bebido)

    /** El cristal que le ha brotado revienta hacia fuera: a quien este a {@link #R_ESTALLIDO}, dano y le aparta. */
    private void estallar() {
        if (!(level() instanceof ServerLevel sl)) return;
        estallidos++;
        playSound(DarkCraftMod.SFX_GALEN_ESTALLIDO.get(), 2.2f, 1.0f);
        sl.sendParticles(CRISTAL, getX(), getY() + 1.5, getZ(), 90, 1.2, 0.9, 1.2, 0.35);
        sl.sendParticles(ELIXIR, getX(), getY() + 1.5, getZ(), 30, 1.4, 0.8, 1.4, 0.05);
        for (int k = 0; k < 24; k++) {
            double a = k * Math.PI * 2 / 24;
            sl.sendParticles(ParticleTypes.CRIT, getX() + Math.cos(a) * 2.5, getY() + 1.3, getZ() + Math.sin(a) * 2.5, 1, 0, 0, 0, 0.2);
        }
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.0f;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(R_ESTALLIDO, 2, R_ESTALLIDO),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            double dx = le.getX() - getX(), dz = le.getZ() - getZ(), dd = Math.hypot(dx, dz);
            if (dd > R_ESTALLIDO) continue;
            if (le.hurt(damageSources().mobAttack(this), dano)) {
                estallidosQueDan++;
                Sangrado.sumar(le, 20f, this);
                if (dd > 1e-3) le.setDeltaMovement(le.getDeltaMovement().add(dx / dd * 1.1, 0.35, dz / dd * 1.1));
                le.hurtMarked = true;
            }
        }
    }

    // ------------------------------------------------------------------ beberse el elixir (a media vida)

    private void beber(int t) {
        if (!(level() instanceof ServerLevel sl)) return;
        if (t == 2) {
            hablaHasta = 0;
            habla(2);
        }
        // se arranca los tubos del cuello: el elixir salta y chisporrotea en el cobre
        if (t == 14) {
            entityData.set(ARRANCADOS, true);
            playSound(DarkCraftMod.SFX_GALEN_TUBOS.get(), 2.0f, 1.0f);
            sl.sendParticles(ELIXIR, getX(), getY() + 2.6, getZ(), 30, 0.4, 0.3, 0.4, 0.06);
            sl.sendParticles(Sangrado.GOTA, getX(), getY() + 2.5, getZ(), 10, 0.2, 0.2, 0.2, 0.02);
            sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 2.9, getZ(), 8, 0.3, 0.2, 0.3, 0.03);
        }
        if (t == 30) playSound(DarkCraftMod.SFX_GALEN_BEBER.get(), 2.0f, 1.0f);
        // bebiendo: lo que se le cae por la barbilla
        if (t >= 42 && t <= 68 && t % 3 == 0) sl.sendParticles(ELIXIR, getX(), getY() + 2.5, getZ(), 3, 0.15, 0.1, 0.15, 0.0);
        // tira el frasco (se rompe) y el cristal le brota por todo el cuerpo
        if (t == 74) {
            Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
            double fx = getX() - d.z * 2.2 + d.x * 0.8, fz = getZ() + d.x * 2.2 + d.z * 0.8;
            sl.playSound(null, fx, getY(), fz, SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.2f, 0.9f);
            sl.sendParticles(Frasco.CRISTAL, fx, getY() + 0.2, fz, 16, 0.2, 0.1, 0.2, 0.1);
            bebidas++;
            variante(BEBIDO);
            playSound(SoundEvents.AMETHYST_CLUSTER_PLACE, 2.0f, 0.6f);
            sl.sendParticles(CRISTAL, getX(), getY() + 1.6, getZ(), 50, 0.6, 1.0, 0.6, 0.12);
            sl.sendParticles(ELIXIR, getX(), getY() + 1.6, getZ(), 40, 0.8, 1.1, 0.8, 0.03);
            vaporEspera = Math.min(vaporEspera, 70);
            frascosEspera = Math.min(frascosEspera, 50);
            estallidoEspera = 30;
            venteo = 60;
            DarkCraftMod.LOGGER.info("[DarkCraft] Maese Galen se bebe el elixir");
        }
    }

    // ------------------------------------------------------------------ el gran alambique

    /**
     * Mientras pelea en la Sala del Elixir el gran alambique hierve (su ruido, cada 9 s, y el vapor que le sale por el cuello); bebido,
     * cada {@link #VENTEO_T} ticks suelta vapor por la base: a quien este arrimado ({@link #R_VENTEO} del centro), dano y le aparta.
     */
    private void tickAlambique(ServerLevel sl) {
        if (!enPelea() || !enSala()) return;
        double[] a = Lazareto.alambique();
        if (--alambiqueSuena <= 0) {
            alambiqueSuena = 180;
            sl.playSound(null, a[0], a[1] + 2, a[2], DarkCraftMod.SFX_GALEN_ALAMBIQUE.get(), SoundSource.HOSTILE, bebido() ? 2.0f : 1.3f, 1.0f);
        }
        if (tickCount % 6 == 0) {
            sl.sendParticles(ParticleTypes.BUBBLE_POP, a[0], a[1] + 2.5, a[2], 3, 1.0, 1.2, 1.0, 0.01);
            sl.sendParticles(ParticleTypes.CLOUD, a[0], a[1] + 6.4, a[2], bebido() ? 3 : 1, 0.2, 0.2, 0.2, 0.03);
        }
        if (!bebido()) return;
        // el aviso: silba y humea por la base un segundo antes
        if (venteo == 20) sl.playSound(null, a[0], a[1] + 1, a[2], SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 1.2f, 0.5f);
        if (venteo > 0 && venteo <= 20 && venteo % 4 == 0)
            for (int k = 0; k < 12; k++) {
                double ang = k * Math.PI * 2 / 12;
                sl.sendParticles(ParticleTypes.CLOUD, a[0] + Math.cos(ang) * 2.8, a[1] + 0.2, a[2] + Math.sin(ang) * 2.8, 1, 0.05, 0.05, 0.05, 0.01);
            }
        if (--venteo > 0) return;
        venteo = VENTEO_T;
        venteos++;
        sl.playSound(null, a[0], a[1] + 1, a[2], DarkCraftMod.SFX_GALEN_VAPOR.get(), SoundSource.HOSTILE, 1.8f, 0.8f);
        for (int k = 0; k < 24; k++) {
            double ang = k * Math.PI * 2 / 24;
            double c = Math.cos(ang), s = Math.sin(ang);
            sl.sendParticles(ParticleTypes.CLOUD, a[0] + c * 2.9, a[1] + 0.5, a[2] + s * 2.9, 0, c, 0.15, s, 0.25);
            sl.sendParticles(VAPOR, a[0] + c * 3.2, a[1] + 1.0, a[2] + s * 3.2, 1, 0.2, 0.4, 0.2, 0.0);
        }
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, new AABB(a[0] - R_VENTEO, a[1] - 1, a[2] - R_VENTEO, a[0] + R_VENTEO, a[1] + 3, a[2] + R_VENTEO),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            double dx = le.getX() - a[0], dz = le.getZ() - a[2], dd = Math.hypot(dx, dz);
            if (dd > R_VENTEO) continue;
            if (le.hurt(damageSources().mobAttack(this), 4.5f)) {
                venteosQueDan++;
                if (dd > 1e-3) le.setDeltaMovement(le.getDeltaMovement().add(dx / dd * 0.9, 0.3, dz / dd * 0.9));
                le.hurtMarked = true;
            }
        }
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
        cuatro = false;
        frascosEspera = 60;
        vaporEspera = 90;
        estallidoEspera = 40;
        alambiqueSuena = 0;
        venteo = VENTEO_T;
        long ahora = level().getGameTime();
        if (ahora - ultimaEntrada > 20 * 60 * 4 || ahora < ultimaEntrada) {
            ultimaEntrada = ahora;
            habla(1);
            // se vuelve mientras lo dice y no se prepara ni ataca hasta acabarlo (si le pegan antes, si)
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
        variante(0);
        entityData.set(ARRANCADOS, false);
        esperaRugido = 0;
    }

    /** A media vida no se prepara: se bebe el elixir. */
    @Override
    protected void alSegundaMitad() {
        empezar(GalenAnims.BEBER);
        pausa = Math.max(pausa, 6);
    }

    @Override
    public void setTarget(LivingEntity t) {
        super.setTarget(t);
        if (t != null && !level().isClientSide) ultimaPresa = t;
    }

    /** Destilando junto al gran alambique solo repara en quien entra en la Sala del Elixir y se le acerca (13 bloques, si le ve). */
    @Override
    protected boolean percibe(LivingEntity p) {
        if (enSala() && !Lazareto.enElixir(p.getX(), p.getY(), p.getZ())) return false;
        if (postura() != DE_PIE) return distanceToSqr(p) < 13 * 13 && hasLineOfSight(p);
        return super.percibe(p);
    }

    /** Los suyos no son presa; peleando en la Sala del Elixir, los de fuera tampoco (no se le saca de alli). */
    @Override
    protected boolean objetivoValido(LivingEntity e) {
        if (e instanceof Destilado || e instanceof Galen) return false;
        if (e instanceof Player && enPelea() && enSala() && !Lazareto.enElixir(e.getX(), e.getY(), e.getZ())) return false;
        return super.objetivoValido(e);
    }

    /** Si le pegan mientras dice su frase, deja de esperar. */
    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        if (!level().isClientSide && esperaRugido > tickCount && s.getEntity() instanceof Player) {
            esperaRugido = tickCount;
            pausa = Math.min(pausa, 8);
        }
        return super.hurt(s, cuanto);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            cliente();
            return;
        }
        if (!isAlive()) return;
        if (frascosEspera > 0) frascosEspera--;
        if (vaporEspera > 0) vaporEspera--;
        if (estallidoEspera > 0) estallidoEspera--;
        ServerLevel sl = (ServerLevel) level();
        tickMarcas(sl);
        tickAlambique(sl);
        // ha caido aquel con quien peleaba
        if (enPelea() && getTarget() == null && ultimaPresa != null && ultimaPresa.isDeadOrDying()) {
            ultimaPresa = null;
            hablaSiCalla(3);
        }
    }

    /** En el cliente: destilando, la gota que cae de la aguja al gran alambique; la aguja gotea; bebido, el cristal le chispea. */
    private void cliente() {
        if (!isAlive()) return;
        Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
        if (postura() == REZA && accion() == 0 && tickCount % 80 == 32) {
            double x = getX() + d.x * 1.0, z = getZ() + d.z * 1.0;
            level().addParticle(ELIXIR, x, getY() + 2.3, z, 0, -0.05, 0);
            level().playLocalSound(getX(), getY() + 2, getZ(), SoundEvents.BREWING_STAND_BREW, SoundSource.HOSTILE, 0.5f, 1.3f, false);
        }
        if (postura() == DE_PIE && tickCount % 9 == 0) {
            double x = getX() + d.x * 1.1 - d.z * 0.6, z = getZ() + d.z * 1.1 + d.x * 0.6;
            level().addParticle(ParticleTypes.DRIPPING_WATER, x, getY() + 0.9, z, 0, 0, 0);
        }
        if (bebido() && tickCount % 4 == 0)
            level().addParticle(ELIXIR, getX() + (random.nextDouble() - 0.5) * 1.0, getY() + 0.6 + random.nextDouble() * 2.2,
                    getZ() + (random.nextDouble() - 0.5) * 1.0, 0, 0.02, 0);
    }

    @Override
    public void die(DamageSource s) {
        boolean ya = !isAlive() && deathTime > 0;
        marcas.clear();
        super.die(s);
        if (level().isClientSide || ya) return;
        hablaHasta = 0;
        habla(5);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ELIXIR, getX(), getY() + 1.6, getZ(), 30, 0.6, 1.0, 0.6, 0.03);
            sl.sendParticles(CRISTAL, getX(), getY() + 1.4, getZ(), 30, 0.6, 0.8, 0.6, 0.1);
        }
    }

    /**
     * Al caer no se va en humo: cae de rodillas tendiendo la mano al gran alambique, que deja de hervir (el vapor se acaba), dice su
     * ultima frase y al final se rompe en cristal verde.
     */
    @Override
    protected void tickDeath() {
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        int fin = GalenAnims.TICKS[GalenAnims.MORIR] + 70;
        if (level() instanceof ServerLevel sl) {
            if (enSala() && deathTime < 120 && deathTime % 8 == 0) {
                // el gran alambique se va apagando: cada vez menos burbujas y menos vapor
                double[] a = Lazareto.alambique();
                int n = Math.max(0, 4 - deathTime / 30);
                if (n > 0) sl.sendParticles(ParticleTypes.BUBBLE_POP, a[0], a[1] + 2.5, a[2], n, 0.9, 1.0, 0.9, 0.01);
                if (deathTime < 60) sl.sendParticles(ParticleTypes.CLOUD, a[0], a[1] + 6.4, a[2], 1, 0.2, 0.2, 0.2, 0.02);
            }
            if (deathTime % 5 == 0 && deathTime < fin - 6) sl.sendParticles(ELIXIR, getX(), getY() + 1.2, getZ(), 2, 0.5, 0.6, 0.5, 0.0);
            if (deathTime == fin - 12) sl.playSound(null, getX(), getY() + 1, getZ(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.HOSTILE, 1.6f, 0.6f);
            if (deathTime == fin - 2) {
                // se rompe en cristal verde
                sl.sendParticles(CRISTAL, getX(), getY() + 1.2, getZ(), 140, 0.7, 1.0, 0.7, 0.12);
                sl.sendParticles(Frasco.CRISTAL, getX(), getY() + 1.2, getZ(), 40, 0.6, 0.8, 0.6, 0.15);
                sl.sendParticles(ELIXIR, getX(), getY() + 1.2, getZ(), 50, 0.8, 1.0, 0.8, 0.03);
                sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.8, getZ(), 14, 0.5, 0.8, 0.5, 0.03);
                sl.playSound(null, getX(), getY() + 1, getZ(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.8f, 0.6f);
            }
        }
        if (deathTime >= fin) remove(RemovalReason.KILLED);
    }

    /** Mientras dice su primera frase se queda donde esta (volviendose); despues, a media distancia. */
    @Override
    protected double distanciaDeseada() {
        return enPelea() && tickCount < esperaRugido ? 99.0 : bebido() ? 2.8 : 3.3;
    }

    @Override
    protected double prisa() {
        return bebido() ? 1.25 : 1.0;
    }

    @Override
    protected int pausaTras(int accion) {
        boolean b = bebido();
        return switch (accion) {
            case GalenAnims.PREPARAR, GalenAnims.BEBER -> 6;
            case GalenAnims.VAPOR, GalenAnims.FRASCOS -> (b ? 7 : 11) + random.nextInt(8);
            default -> (b ? 6 : 10) + random.nextInt(12);
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

    /** El paso, y los frascos del cinto que tintinean. */
    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        playSound(tickCount % 2 == 0 ? SoundEvents.GLASS_STEP : s.getSoundType().getStepSound(), 0.4f, tickCount % 2 == 0 ? 1.5f : 0.8f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_GALEN_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_GALEN_MUERTE.get();
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        spawnAtLocation(new ItemStack(DarkCraftMod.JERINGA_FISICO.get()));
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_3.get(), 3));
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_2.get(), 2));
        spawnAtLocation(new ItemStack(DarkCraftMod.CENIZAS.get(com.darkcraft.combate.Habilidad.SANGRE).get()));
    }

    /** Para las pruebas y el comando: como esta. */
    public String estado() {
        return String.format(java.util.Locale.ROOT, "vida %.0f/%.0f, %s, %s en %.1f,%.1f,%.1f; jeringazos %d (envenenan %d, le devuelven %.0f), bisturi %d, "
                        + "lluvias %d (frascos %d), vapores %d (columnas %d, dan %d), estallidos %d (dan %d), el alambique ventea %d (da %d)%s%s",
                getHealth(), getMaxHealth(), enPelea() ? "peleando" : "esperando", postura() == DE_PIE ? "de pie" : "destilando", getX(), getY(), getZ(),
                jeringazos, envenenados, drenado, bisturis, lluvias, frascosLanzados, vapores, columnas, columnasQueDan, estallidos, estallidosQueDan,
                venteos, venteosQueDan, arrancados() ? " (sin tubos)" : "", bebido() ? " (bebido)" : "");
    }
}
