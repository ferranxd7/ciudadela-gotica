package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.EfectosRaiz;
import com.darkcraft.morvain.Osario;
import com.darkcraft.net.Net;
import com.darkcraft.rey.Voces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Fray Osmund, el Sacristan del Osario de San Ulfo (parte 7at): el jefe del Relicario. Cuando la Peste Gris lleno el Camposanto de
 * los Pobres desenterro a los muertos y ordeno sus huesos en la cripta, cuarenta mil, contados uno a uno (libro 37, "El libro de los
 * huesos"); murio contando y le enterraron a los pies de San Ulfo. La noche del cielo rojo se levanto con los demas, pero no con lo
 * suyo: es un mosaico de huesos de muchos, y a la espalda lleva la aureola que se hizo con los que le sobraron (cinco calaveras y
 * cuatro velas). Sigue contando.
 * <ul>
 * <li>espera de <b>rodillas</b> ante el relicario de San Ulfo, contando huesos, de espaldas a la entrada; al ver a alguien dentro del
 * Relicario se levanta mientras dice su frase y al acabarla toca la <b>campanilla</b> (su rugido): de los montones de huesos del
 * borde se levantan muertos (Ensamblados efimeros: se deshacen al acabar la pelea);</li>
 * <li>el <b>incensazo</b> (alza el incensario por encima del hombro y lo estrella delante: el polvo de hueso que arde queda en el
 * aire, frena y debilita), el <b>giro</b> (lo voltea y barre todo a su alrededor: quema);</li>
 * <li>la <b>corona</b>: las cinco calaveras de su aureola se sueltan, le orbitan (rozan) y vuelan una a una hacia su presa
 * ({@link CalaveraOsmund}); el <b>muro de huesos</b>: hunde las manos hacia el suelo y los huesos brotan en abanico (tres lineas;
 * lanzan por el aire); las <b>guirnaldas</b>: alza los brazos a la cupula y caen guirnaldas de huesos sobre cada uno (la marca en
 * el suelo avisa);</li>
 * <li>a <b>media vida</b> se <b>alza</b>: los muertos le sostienen en el aire (un haz de brazos de hueso) y desde entonces flota, va
 * mas deprisa, la corona trae siete calaveras, el muro cinco lineas, caen mas guirnaldas, la lampara de la cupula cruje encendida
 * y vuelve a llamar a los muertos (con un coloso).</li>
 * </ul>
 * Los martillos le hacen un cuarto mas (sus huesos se rompen). Habla (ElevenLabs, en ingles con subtitulos; {@link Voces#BASE_OSMUND}):
 * 1 al empezar (no la repite en 4 min), 2 al alzarse, 3 si cae su presa, 4 las primeras guirnaldas y 5 al caer. Al caer se le
 * cae lo que le sostiene, se queda de rodillas con el incensario en el suelo y suben las almas de los cuarenta mil; lo demas (el
 * cartel con sus ecos, la hazana {@code osmund} y el bestiario) lo reparte {@code valdrenn/Guardianes}; los muertos del osario
 * descansan y ya no vuelven ({@code Osario.paz}). Deja su incensario ({@link com.darkcraft.armas.IncensarioSacristan}).
 */
public class Osmund extends Guardian {
    public static final int ECOS = 16000;
    public static final int VOZ_BASE = Voces.BASE_OSMUND;
    /** Las calaveras de la aureola estan fuera (orbitando o volando) y si se ha alzado (a media vida: flota). Lo sabe el cliente. */
    private static final EntityDataAccessor<Boolean> CORONA = SynchedEntityData.defineId(Osmund.class, EntityDataSerializers.BOOLEAN),
            ALZADO = SynchedEntityData.defineId(Osmund.class, EntityDataSerializers.BOOLEAN);
    /** De la marca a la guirnalda (ticks) y su radio; el muro: hasta donde llega y el radio de cada hueso; la nube del incensario. */
    public static final int MARCA_T = 28, NUBE_T = 60;
    public static final double R_GUIRNALDA = 1.6, MURO_LARGO = 13.0, R_HUESO = 1.2, R_NUBE = 2.3;
    /** Lo que flota alzado (bloques sobre el suelo: 16 pixeles de su modelo, lo que mide el haz de brazos que le sostiene). */
    public static final double ALTURA = 1.5;
    /** Las calaveras de la corona (alzado, siete) y los muertos que llama la campanilla (como mucho, en pie a la vez). */
    public static final int CALAVERAS = 5, CALAVERAS_ALZADO = 7, MUERTOS_MAX = 4;

    public static final DustParticleOptions POLVO = new DustParticleOptions(new Vector3f(0.74f, 0.7f, 0.6f), 1.5f),
            ALMA = new DustParticleOptions(new Vector3f(0.36f, 0.74f, 0.98f), 1.1f),
            MARCA = new DustParticleOptions(new Vector3f(0.62f, 0.58f, 0.5f), 1.5f);

    /** La ultima vez que dijo su frase de entrada (tiempo del mundo): no la repite en cada intento. */
    private static long ultimaEntrada = Long.MIN_VALUE / 2;

    private record Marca(double x, double y, double z, int cuando) {}

    private record Hueso(double x, double y, double z, int cuando, double r, float mult) {}

    private record Nube(double x, double y, double z, int hasta) {}

    private final List<Marca> marcas = new ArrayList<>();
    private final List<Hueso> huesos = new ArrayList<>();
    private final List<Nube> nubes = new ArrayList<>();
    private final Set<UUID> dados = new HashSet<>();
    private final List<Ensamblado> suyos = new ArrayList<>();
    private final List<CalaveraOsmund> calaveras = new ArrayList<>();
    private int coronaEspera = 80, muroEspera = 60, guirnaldasEspera = 160, giroEspera = 40, llamarEspera, hablaHasta, esperaRugido, lampara;
    private boolean cuatro;
    private LivingEntity ultimaPresa;
    /** Para las pruebas. */
    private int incensazos, giros, nubesQueDan, coronas, sueltas, muros, huesosQueDan, guirnaldas, caidas, caidasQueDan, llamados, alzadas;
    /** Para las pruebas: no elige golpes por su cuenta (lo que se le manda hacer, si; y lo ya lanzado sigue). */
    public boolean quieta;

    public Osmund(EntityType<? extends Osmund> t, Level l) {
        super(t, l, "osmund", BossEvent.BossBarColor.WHITE);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1100.0).add(Attributes.ATTACK_DAMAGE, 12.0).add(Attributes.ARMOR, 6.0)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(CORONA, false);
        entityData.define(ALZADO, false);
    }

    /** Si las calaveras de su aureola estan fuera (el cliente no las dibuja en el aro). */
    public boolean corona() {
        return entityData.get(CORONA);
    }

    /** Si se ha alzado (a media vida: flota sobre los brazos de los muertos). */
    public boolean alzado() {
        return entityData.get(ALZADO);
    }

    @Override
    public String tipo() {
        return "osmund";
    }

    @Override
    public int musica() {
        return 51;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return "osmund";
    }

    @Override
    protected int[] ticks() {
        return OsmundAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return OsmundAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return OsmundAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return OsmundAnims.MORIR;
    }

    @Override
    protected int accionRugido() {
        return OsmundAnims.LLAMAR;
    }

    @Override
    protected int accionLevantarse() {
        return OsmundAnims.LEVANTARSE;
    }

    // ------------------------------------------------------------------ para las pruebas

    public int incensazos() {
        return incensazos;
    }

    public int giros() {
        return giros;
    }

    /** A cuantos ha frenado la nube del incensario. */
    public int nubesQueDan() {
        return nubesQueDan;
    }

    /** Las coronas y las calaveras que ha soltado (las que siguen fuera: {@link #calaverasFuera}). */
    public int coronas() {
        return coronas;
    }

    public int sueltas() {
        return sueltas;
    }

    public int calaverasFuera() {
        int n = 0;
        for (CalaveraOsmund c : calaveras) if (!c.isRemoved()) n++;
        return n;
    }

    /** Los muros de huesos y los huesos que han dado a alguien. */
    public int muros() {
        return muros;
    }

    public int huesosQueDan() {
        return huesosQueDan;
    }

    /** Las veces que ha llamado a las guirnaldas, las que han caido y las que han dado. */
    public int guirnaldas() {
        return guirnaldas;
    }

    public int caidas() {
        return caidas;
    }

    public int caidasQueDan() {
        return caidasQueDan;
    }

    public int marcasPendientes() {
        return marcas.size();
    }

    /** Los muertos que ha llamado y los que siguen en pie. */
    public int llamados() {
        return llamados;
    }

    public int suyos() {
        int n = 0;
        for (Ensamblado e : suyos) if (e.isAlive() && !e.isRemoved()) n++;
        return n;
    }

    public List<Ensamblado> muertos() {
        return List.copyOf(suyos);
    }

    /** Las veces que se ha alzado. */
    public int alzadas() {
        return alzadas;
    }

    /** Para las pruebas: pasa ya a media vida (se alza), sin esperar a que acabe lo que hace. */
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

    /** Si las calaveras (y lo suyo) pueden darle a esa: lo mismo que sus golpes. */
    public boolean presa(LivingEntity e) {
        return e != null && e != this && e.isAlive() && objetivoValido(e);
    }

    /** Donde esta su aureola (detras de la cabeza): de ahi salen y ahi vuelven las calaveras. */
    public Vec3 aureola() {
        Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
        return new Vec3(getX() - d.x * 0.45, getY() + 2.85, getZ() - d.z * 0.45);
    }

    // ------------------------------------------------------------------ que hace

    @Override
    protected int elegir(LivingEntity presa, double d) {
        boolean a = alzado();
        if (quieta || Math.abs(presa.getY() - getY()) > (a ? 6.5 : 5.0)) return 0;
        // las guirnaldas: de lejos o de cerca, cada rato
        if (guirnaldasEspera <= 0 && d < 22 && random.nextFloat() < (a ? 0.3f : 0.2f)) {
            guirnaldasEspera = (a ? 200 : 280) + random.nextInt(60);
            return OsmundAnims.GUIRNALDAS;
        }
        // ya alzado, si no le queda ningun muerto vuelve a tocar la campanilla (de tarde en tarde)
        if (a && llamarEspera <= 0 && suyos() == 0 && random.nextFloat() < 0.25f) {
            llamarEspera = 600;
            return OsmundAnims.LLAMAR;
        }
        if (!corona() && coronaEspera <= 0 && d > 3.0 && d < 18 && random.nextFloat() < 0.45f) {
            coronaEspera = (a ? 170 : 230) + random.nextInt(60);
            return OsmundAnims.CORONA;
        }
        if (muroEspera <= 0 && d > 3.5 && d < MURO_LARGO && random.nextFloat() < 0.45f) {
            muroEspera = (a ? 110 : 160) + random.nextInt(40);
            return OsmundAnims.MURO;
        }
        if (d < 4.6) {
            // rodeado (dos o mas a su alcance) o de vez en cuando: el giro
            if (giroEspera <= 0 && (cercanos(4.8) >= 2 || random.nextFloat() < 0.3f)) {
                giroEspera = 70 + random.nextInt(30);
                return OsmundAnims.GIRO;
            }
            return OsmundAnims.INCENSAZO;
        }
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
            case OsmundAnims.INCENSAZO -> new Golpe(4.5f, 60f, 1.3f, 1.3f);
            case OsmundAnims.GIRO -> new Golpe(4.7f, 360f, 1.0f, 1.7f);
            default -> null;
        };
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (accion == OsmundAnims.INCENSAZO) le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1), this);
        // las brasas del incensario al voltearlo
        if (accion == OsmundAnims.GIRO) le.setSecondsOnFire(3);
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = OsmundAnims.GOLPES[accion];
        switch (accion) {
            case OsmundAnims.INCENSAZO -> {
                if (t == g[0] - 6) playSound(DarkCraftMod.SFX_OSMUND_INCENSARIO.get(), 1.9f, 0.9f + random.nextFloat() * 0.15f);
                if (t == g[0] - 5) empujar(0.22);
                if (t == g[0]) {
                    incensazos++;
                    impacto(3.4, 1.0f);
                    nube(3.4);
                }
            }
            case OsmundAnims.GIRO -> {
                if (t == 1) playSound(DarkCraftMod.SFX_OSMUND_GIRO.get(), 2.0f, 1.0f);
                if (t == g[0]) {
                    giros++;
                    if (level() instanceof ServerLevel sl)
                        for (int j = 0; j < 18; j++) {
                            double a = j * Math.PI * 2 / 18;
                            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, getX() + Math.cos(a) * 3.6, getY() + 1.4, getZ() + Math.sin(a) * 3.6, 1, 0, 0, 0, 0);
                            if (j % 2 == 0)
                                sl.sendParticles(ParticleTypes.FLAME, getX() + Math.cos(a) * 3.0, getY() + 1.6, getZ() + Math.sin(a) * 3.0, 2, 0.2, 0.2, 0.2, 0.04);
                        }
                }
            }
            case OsmundAnims.CORONA -> {
                if (t == 4) playSound(DarkCraftMod.SFX_OSMUND_CORONA.get(), 2.2f, 1.0f);
                // la aureola se sacude antes de soltarlas
                if (t > 4 && t < g[0] && t % 3 == 0 && level() instanceof ServerLevel sl) {
                    Vec3 a = aureola();
                    sl.sendParticles(ALMA, a.x, a.y, a.z, 4, 0.5, 0.5, 0.2, 0.0);
                    sl.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.BONE_BLOCK.defaultBlockState()), a.x, a.y, a.z, 2, 0.5, 0.4, 0.2, 0.0);
                }
                if (t == g[0]) soltarCorona();
            }
            case OsmundAnims.MURO -> {
                if (t == g[0] - 3) playSound(DarkCraftMod.SFX_OSMUND_MURO.get(), 2.4f, 1.0f);
                if (t == g[0]) {
                    impacto(2.2, 1.2f);
                    muro();
                }
            }
            case OsmundAnims.LLAMAR -> {
                if (t == 8) playSound(DarkCraftMod.SFX_OSMUND_LLAMAR.get(), 3.0f, 1.0f);
                if (t == 30) llamarMuertos(alzado() ? 1 : 0);
                if (t >= 10 && t <= 40 && t % 4 == 0 && level() instanceof ServerLevel sl)
                    sl.sendParticles(ALMA, getX(), getY() + 2.0, getZ(), 6, 3.0, 1.2, 3.0, 0.0);
            }
            case OsmundAnims.GUIRNALDAS -> {
                if (t == 6 && !cuatro) {
                    cuatro = true;
                    hablaSiCalla(4);
                }
                if (t == g[0]) llamarGuirnaldas();
            }
            case OsmundAnims.ALZARSE -> {
                if (t == 2) {
                    playSound(DarkCraftMod.SFX_OSMUND_ALZARSE.get(), 2.6f, 1.0f);
                    hablaHasta = 0;
                    habla(2);
                }
                // los huesos del suelo suben hacia el: polvo, almas y huesos que se arremolinan a sus pies
                if (t >= 8 && t <= 46 && level() instanceof ServerLevel sl) {
                    for (int k = 0; k < 3; k++) {
                        double a = random.nextDouble() * Math.PI * 2, r = 1.5 + random.nextDouble() * 5;
                        double s = ((t - 8) / 38.0 + k / 3.0) % 1.0;
                        sl.sendParticles(ALMA, Mth.lerp(s, getX() + Math.cos(a) * r, getX()), getY() + 0.2 + s * 1.4,
                                Mth.lerp(s, getZ() + Math.sin(a) * r, getZ()), 1, 0, 0, 0, 0);
                    }
                    if (t % 3 == 0)
                        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()), getX(), getY() + 0.4, getZ(), 8, 0.8, 0.3, 0.8, 0.1);
                }
                if (t == 26) alzarse();
                if (t == 46) llamarMuertos(1);
            }
            case OsmundAnims.LEVANTARSE -> {
                if (t == 2 && level() instanceof ServerLevel sl)
                    sl.sendParticles(POLVO, getX(), getY() + 1.2, getZ(), 20, 0.8, 0.8, 0.8, 0.0);
                // mientras se levanta se vuelve entero hacia quien llega (como mucho 16 grados por tick)
                if (t >= 6 && getTarget() != null) girarHacia(getTarget(), 16f);
            }
            default -> {
            }
        }
    }

    /** Donde da el incensario contra el suelo: polvo de hueso, brasas y el golpe. */
    private void impacto(double lejos, float fuerte) {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * lejos, z = getZ() + d.z * lejos;
        double y = suelo(sl, x, z);
        if (Double.isNaN(y)) y = getY();
        BlockState s = level().getBlockState(BlockPos.containing(x, y - 0.5, z));
        if (!s.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, s), x, y + 0.1, z, (int) (22 * fuerte), 0.5, 0.05, 0.5, 0.15);
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()), x, y + 0.2, z, (int) (14 * fuerte), 0.4, 0.1, 0.4, 0.12);
        sl.sendParticles(POLVO, x, y + 0.4, z, (int) (14 * fuerte), 0.7, 0.3, 0.7, 0.02);
        sl.sendParticles(ParticleTypes.LAVA, x, y + 0.3, z, 3, 0.3, 0.1, 0.3, 0.0);
        sl.sendParticles(ParticleTypes.CRIT, x, y + 0.2, z, 8, 0.2, 0.1, 0.2, 0.2);
    }

    // ------------------------------------------------------------------ la nube del incensario

    /** Donde da el incensario queda una nube de polvo de hueso encendido ({@link #NUBE_T} ticks): frena y debilita a quien entra. */
    private void nube(double lejos) {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * lejos, z = getZ() + d.z * lejos;
        double y = suelo(sl, x, z);
        nubes.add(new Nube(x, Double.isNaN(y) ? getY() : y, z, tickCount + NUBE_T));
    }

    private void tickNubes(ServerLevel sl) {
        for (int i = nubes.size() - 1; i >= 0; i--) {
            Nube n = nubes.get(i);
            if (tickCount > n.hasta()) {
                nubes.remove(i);
                continue;
            }
            if (tickCount % 3 == 0) {
                sl.sendParticles(POLVO, n.x(), n.y() + 0.8, n.z(), 5, R_NUBE * 0.6, 0.5, R_NUBE * 0.6, 0.01);
                sl.sendParticles(ParticleTypes.WHITE_ASH, n.x(), n.y() + 1.0, n.z(), 4, R_NUBE * 0.5, 0.6, R_NUBE * 0.5, 0.01);
                if (tickCount % 9 == 0) sl.sendParticles(ParticleTypes.SMALL_FLAME, n.x(), n.y() + 0.6, n.z(), 2, R_NUBE * 0.4, 0.3, R_NUBE * 0.4, 0.005);
            }
            if (tickCount % 10 != 0) continue;
            for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, new AABB(n.x() - R_NUBE, n.y() - 0.5, n.z() - R_NUBE, n.x() + R_NUBE, n.y() + 2.5,
                    n.z() + R_NUBE), e -> e != this && e.isAlive() && objetivoValido(e))) {
                if (le.distanceToSqr(n.x(), le.getY(), n.z()) > R_NUBE * R_NUBE) continue;
                if (!le.hasEffect(MobEffects.WEAKNESS)) nubesQueDan++;
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0), this);
                le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0), this);
            }
        }
    }

    // ------------------------------------------------------------------ la corona

    /** Las calaveras de la aureola se sueltan y le orbitan; luego vuelan una a una ({@link CalaveraOsmund}). */
    private void soltarCorona() {
        if (!(level() instanceof ServerLevel sl)) return;
        int n = alzado() ? CALAVERAS_ALZADO : CALAVERAS;
        coronas++;
        entityData.set(CORONA, true);
        Vec3 a = aureola();
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()), a.x, a.y, a.z, 24, 0.6, 0.6, 0.3, 0.1);
        sl.sendParticles(ParticleTypes.SOUL, a.x, a.y, a.z, 10, 0.6, 0.6, 0.3, 0.03);
        for (int k = 0; k < n; k++) {
            CalaveraOsmund c = new CalaveraOsmund(sl, this, k, n);
            sl.addFreshEntity(c);
            calaveras.add(c);
            sueltas++;
        }
    }

    /** Cada tick: cuando ya no le queda ninguna calavera fuera, la aureola se rehace (vuelven a crecerle). */
    private void tickCorona(ServerLevel sl) {
        calaveras.removeIf(c -> c.isRemoved());
        if (corona() && calaveras.isEmpty()) {
            entityData.set(CORONA, false);
            Vec3 a = aureola();
            sl.sendParticles(POLVO, a.x, a.y, a.z, 14, 0.6, 0.6, 0.2, 0.0);
            sl.sendParticles(ParticleTypes.SOUL, a.x, a.y, a.z, 6, 0.5, 0.5, 0.2, 0.02);
        }
    }

    private void soltarCalaveras() {
        for (CalaveraOsmund c : calaveras) if (!c.isRemoved()) c.romper();
        calaveras.clear();
        entityData.set(CORONA, false);
    }

    // ------------------------------------------------------------------ el muro de huesos

    /** Los huesos brotan en abanico (tres lineas; alzado, cinco), cada 1,3 bloques y dos ticks uno de otro, hasta {@link #MURO_LARGO}. */
    private void muro() {
        if (!(level() instanceof ServerLevel sl)) return;
        muros++;
        dados.clear();
        int lineas = alzado() ? 5 : 3;
        double abre = alzado() ? 24 : 30;
        for (int i = 0; i < lineas; i++) {
            float ang = (float) (getYRot() + (i - (lineas - 1) / 2.0) * abre);
            Vec3 d = Vec3.directionFromRotation(0, ang);
            int k = 0;
            for (double a = 2.0; a <= MURO_LARGO; a += 1.3, k++) {
                if (!brotar(sl, getX() + d.x * a, getZ() + d.z * a, 3 + 2 * k, 0.85f)) break;
            }
        }
    }

    /** Un hueso que brota en (x, z) dentro de {@code retraso} ticks (antes, el aviso: polvo). Falso si no hay suelo. */
    private boolean brotar(ServerLevel sl, double x, double z, int retraso, float mult) {
        double y = suelo(sl, x, z);
        if (Double.isNaN(y)) return false;
        sl.sendParticles(POLVO, x, y + 0.1, z, 5, 0.3, 0.02, 0.3, 0.0);
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()), x, y + 0.1, z, 3, 0.3, 0.02, 0.3, 0.05);
        EfectosRaiz.estaca(sl, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y), x, y - 0.2, z,
                EfectosRaiz.ang(sl.random), 0.35f * (sl.random.nextFloat() - 0.5f), 1.6f + sl.random.nextFloat() * 0.9f, 0.4f + sl.random.nextFloat() * 0.2f,
                retraso, 24);
        huesos.add(new Hueso(x, y, z, tickCount + retraso + 1, R_HUESO, mult));
        return true;
    }

    /** Lo alto del suelo bajo (x, z) cerca de su altura (o de la de sus pies, si flota), o NaN si no hay (un hueco, un muro). */
    private double suelo(ServerLevel sl, double x, double z) {
        int bx = Mth.floor(x), bz = Mth.floor(z), by = Mth.floor(getY());
        for (int y = by + 1; y >= by - 4; y--) {
            BlockPos p = new BlockPos(bx, y, bz);
            BlockState s = sl.getBlockState(p);
            if (!s.getCollisionShape(sl, p).isEmpty() && sl.getBlockState(p.above()).getCollisionShape(sl, p.above()).isEmpty())
                return y + s.getCollisionShape(sl, p).max(Direction.Axis.Y);
        }
        return Double.NaN;
    }

    /** Cada tick: los huesos que brotan dan a quien pillan (lanzan por el aire); una vez por muro a cada uno. */
    private void tickHuesos(ServerLevel sl) {
        if (huesos.isEmpty()) return;
        float base = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (int i = huesos.size() - 1; i >= 0; i--) {
            Hueso h = huesos.get(i);
            if (tickCount < h.cuando()) continue;
            huesos.remove(i);
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()), h.x(), h.y() + 0.8, h.z(), 6, 0.3, 0.5, 0.3, 0.05);
            for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, new AABB(h.x() - h.r() - 0.5, h.y() - 0.5, h.z() - h.r() - 0.5, h.x() + h.r() + 0.5,
                    h.y() + 2.6, h.z() + h.r() + 0.5), e -> e != this && e.isAlive() && objetivoValido(e))) {
                if (dados.contains(le.getUUID()) || le.distanceToSqr(h.x(), le.getY(), h.z()) > h.r() * h.r()) continue;
                if (le.hurt(damageSources().mobAttack(this), base * h.mult())) {
                    dados.add(le.getUUID());
                    huesosQueDan++;
                    le.setDeltaMovement(le.getDeltaMovement().add(0, 0.5, 0));
                    le.hurtMarked = true;
                }
            }
        }
    }

    // ------------------------------------------------------------------ las guirnaldas

    /** Alza los brazos a la cupula: una marca bajo los pies de cada uno (y alguna mas) y, al rato, la guirnalda de huesos que cae. */
    private void llamarGuirnaldas() {
        if (!(level() instanceof ServerLevel sl)) return;
        guirnaldas++;
        double base = pies();
        List<LivingEntity> blancos = new ArrayList<>(sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(24, 8, 24),
                e -> e != this && e.isAlive() && objetivoValido(e) && (e instanceof Player || e == getTarget())));
        for (LivingEntity le : blancos) {
            if (le.distanceToSqr(this) > 26 * 26) continue;
            marcar(sl, le.getX(), le.getZ(), le.getY());
            if (alzado()) {
                double a = random.nextDouble() * Math.PI * 2, r = 2.4 + random.nextDouble() * 1.5;
                marcar(sl, le.getX() + Math.cos(a) * r, le.getZ() + Math.sin(a) * r, le.getY());
            }
        }
        int extra = alzado() ? 5 : 3;
        for (int i = 0; i < extra; i++) {
            double a = random.nextDouble() * Math.PI * 2, r = 3.5 + random.nextDouble() * 6;
            marcar(sl, getX() + Math.cos(a) * r, getZ() + Math.sin(a) * r, base);
        }
    }

    /** Lo alto del suelo bajo el (si flota, el de debajo, no el de sus pies). */
    private double pies() {
        if (!alzado() || !(level() instanceof ServerLevel sl)) return getY();
        double p = sueloBajo(sl);
        return Double.isNaN(p) ? getY() : p;
    }

    private void marcar(ServerLevel sl, double x, double z, double cerca) {
        double y = Puestos.suelo(sl, Mth.floor(x), Mth.floor(cerca) + 2, Mth.floor(z));
        if (Math.abs(y - cerca) > 4) y = cerca;
        marcas.add(new Marca(x, y, z, tickCount + MARCA_T));
        // la guirnalda, ya en lo alto: cuelga y cae a su hora (un haz de huesos, y otro mas corto atravesado)
        int techo = techo(sl, x, y, z);
        float alto = Math.max(3f, techo - 1.6f);
        BlockState hueso = Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        EfectosRaiz.cae(sl, hueso, x, y, z, alto, 0.55f + random.nextFloat() * 0.2f, 1.9f + random.nextFloat() * 0.7f, MARCA_T - 7, 7);
        EfectosRaiz.cae(sl, Blocks.BONE_BLOCK.defaultBlockState(), x + (random.nextDouble() - 0.5) * 0.4, y, z + (random.nextDouble() - 0.5) * 0.4,
                alto + 0.6f, 0.8f, 0.5f, MARCA_T - 7, 7);
        sl.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.BONE_BLOCK.defaultBlockState()), x, y + Math.max(3, techo - 0.5), z, 8, 0.5, 0.1, 0.5, 0.0);
    }

    /** Lo alto del techo sobre (x, y, z) (bloques sobre el suelo), o 14 si no lo hay a 24. */
    private static int techo(ServerLevel sl, double x, double y, double z) {
        int bx = Mth.floor(x), by = Mth.floor(y), bz = Mth.floor(z);
        for (int h = 2; h < 24; h++) {
            BlockPos p = new BlockPos(bx, by + h, bz);
            if (!sl.getBlockState(p).getCollisionShape(sl, p).isEmpty()) return h;
        }
        return 14;
    }

    /** Cada tick: las marcas echan su aviso y, a su hora, la guirnalda da en el suelo. */
    private void tickMarcas(ServerLevel sl) {
        for (int i = marcas.size() - 1; i >= 0; i--) {
            Marca m = marcas.get(i);
            if (tickCount < m.cuando()) {
                if (tickCount % 2 == 0)
                    for (int k = 0; k < 10; k++) {
                        double a = (k / 10.0 + tickCount * 0.02) * Math.PI * 2;
                        sl.sendParticles(MARCA, m.x() + Math.cos(a) * R_GUIRNALDA, m.y() + 0.08, m.z() + Math.sin(a) * R_GUIRNALDA, 1, 0, 0, 0, 0);
                    }
                if (tickCount % 4 == 0)
                    sl.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.BONE_BLOCK.defaultBlockState()), m.x(), m.y() + 3, m.z(), 2, 0.4, 0.5,
                            0.4, 0.0);
                if (m.cuando() - tickCount == 7)
                    sl.playSound(null, m.x(), m.y() + 3, m.z(), DarkCraftMod.SFX_GUIRNALDA_CAE.get(), SoundSource.HOSTILE, 1.2f, 0.9f + random.nextFloat() * 0.25f);
                continue;
            }
            marcas.remove(i);
            caer(sl, m);
        }
    }

    /** La guirnalda da en el suelo: a quien pilla debajo, dano y lentitud. */
    private void caer(ServerLevel sl, Marca m) {
        caidas++;
        sl.playSound(null, m.x(), m.y(), m.z(), SoundEvents.BONE_BLOCK_BREAK, SoundSource.HOSTILE, 1.6f, 0.7f + random.nextFloat() * 0.2f);
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()), m.x(), m.y() + 0.4, m.z(), 30, 0.6, 0.3, 0.6, 0.15);
        sl.sendParticles(POLVO, m.x(), m.y() + 0.5, m.z(), 12, 0.8, 0.4, 0.8, 0.02);
        sl.sendParticles(ParticleTypes.SOUL, m.x(), m.y() + 0.6, m.z(), 3, 0.4, 0.3, 0.4, 0.02);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.85f;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, new AABB(m.x() - 2, m.y() - 1, m.z() - 2, m.x() + 2, m.y() + 3, m.z() + 2),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            if (le.distanceToSqr(m.x(), le.getY(), m.z()) > R_GUIRNALDA * R_GUIRNALDA) continue;
            if (le.hurt(damageSources().mobAttack(this), dano)) {
                caidasQueDan++;
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1), this);
            }
        }
    }

    // ------------------------------------------------------------------ los muertos

    /**
     * La campanilla: de los montones de huesos del borde del Relicario se levantan los muertos (dos, y uno mas por jugador de mas;
     * como mucho {@link #MUERTOS_MAX} en pie), efimeros: se deshacen al acabar la pelea. {@code mas}: los que se suman (alzado).
     */
    private void llamarMuertos(int mas) {
        if (!(level() instanceof ServerLevel sl)) return;
        List<ServerPlayer> cerca = new ArrayList<>();
        for (ServerPlayer p : sl.players()) if (p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) < 40 * 40) cerca.add(p);
        int n = Math.min(MUERTOS_MAX - suyos(), Math.max(2, Math.min(MUERTOS_MAX, 1 + cerca.size())) + mas);
        if (n <= 0) return;
        double base = pies();
        List<int[]> sitios = Osario.enRelicario(getX(), base, getZ()) ? new ArrayList<>(Osario.muertos()) : new ArrayList<>();
        if (sitios.isEmpty())
            for (int k = 0; k < 6; k++) {
                double a = k * Math.PI / 3;
                sitios.add(new int[]{Mth.floor(getX() + Math.cos(a) * 7), Mth.floor(getZ() + Math.sin(a) * 7)});
            }
        // los sitios mas lejos de los jugadores primero (que no salgan encima de nadie)
        sitios.sort((a, b) -> Double.compare(lejania(b, cerca), lejania(a, cerca)));
        boolean coloso = alzado();
        for (Ensamblado e : suyos) if (e.isAlive() && e.variante() == Ensamblado.COLOSO) coloso = false;
        int[] variantes = {Ensamblado.PORTACIRIOS, Ensamblado.SEPULTURERO, Ensamblado.PORTACIRIOS, Ensamblado.SEPULTURERO};
        int salen = 0;
        for (int[] q : sitios) {
            if (salen >= n) break;
            int bx = q[0], bz = q[1];
            double y = Puestos.suelo(sl, bx, Mth.floor(base) + 2, bz);
            if (Math.abs(y - base) > 2.6 || lejania(q, cerca) < 3.0) continue;
            Ensamblado e = DarkCraftMod.ENSAMBLADO.get().create(sl);
            if (e == null) return;
            float mira = (float) Math.toDegrees(Math.atan2(getZ() - (bz + 0.5), getX() - (bx + 0.5))) - 90f;
            e.moveTo(bx + 0.5, y, bz + 0.5, mira, 0f);
            e.variante(coloso ? Ensamblado.COLOSO : variantes[(salen + llamados) % variantes.length]);
            if (!sl.noCollision(e)) {
                if (!coloso) continue;
                e.variante(Ensamblado.PORTACIRIOS);
                if (!sl.noCollision(e)) continue;
            }
            coloso = false;
            // de su monton de huesos: se rehacen (sin puesto: no vuelven)
            e.asignar(null, -1, BlockPos.containing(bx + 0.5, y, bz + 0.5), mira, TUMBADO, 0);
            e.efimero(240);
            sl.addFreshEntity(e);
            LivingEntity t = getTarget() != null ? getTarget()
                    : sl.getNearestPlayer(e.getX(), e.getY(), e.getZ(), 30, x -> x instanceof Player pl && objetivoValido(pl));
            if (t != null) e.setTarget(t);
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()), bx + 0.5, y + 0.3, bz + 0.5, 30, 0.5, 0.3, 0.5, 0.1);
            sl.sendParticles(ParticleTypes.SOUL, bx + 0.5, y + 0.8, bz + 0.5, 8, 0.3, 0.4, 0.3, 0.03);
            suyos.add(e);
            salen++;
        }
        llamados += salen;
        DarkCraftMod.LOGGER.info("[DarkCraft] Fray Osmund toca la campanilla: se levantan {} muertos ({} jugadores)", salen, cerca.size());
    }

    private static double lejania(int[] q, List<ServerPlayer> jugadores) {
        double m = Double.MAX_VALUE;
        for (ServerPlayer p : jugadores) m = Math.min(m, Math.hypot(p.getX() - (q[0] + 0.5), p.getZ() - (q[1] + 0.5)));
        return m;
    }

    private void despedir() {
        for (Ensamblado e : suyos) if (e.isAlive() && !e.isRemoved()) e.disolver();
        suyos.clear();
    }

    // ------------------------------------------------------------------ alzarse

    /** Los muertos le levantan: desde ahora flota sobre ellos, va mas deprisa y todo lo suyo crece (ver la clase). */
    private void alzarse() {
        if (!(level() instanceof ServerLevel sl)) return;
        alzadas++;
        entityData.set(ALZADO, true);
        setNoGravity(true);
        sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 0.6, getZ(), 30, 0.8, 0.4, 0.8, 0.05);
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()), getX(), getY() + 0.3, getZ(), 40, 1.0, 0.3, 1.0, 0.15);
        lampara = 20;
        coronaEspera = Math.min(coronaEspera, 60);
        guirnaldasEspera = Math.min(guirnaldasEspera, 120);
        DarkCraftMod.LOGGER.info("[DarkCraft] Fray Osmund se alza");
    }

    /** Lo alto del suelo debajo de el (hasta 6 bloques por debajo de sus pies), o NaN. */
    private double sueloBajo(ServerLevel sl) {
        int bx = Mth.floor(getX()), bz = Mth.floor(getZ()), by = Mth.floor(getY() + 0.2);
        for (int y = by; y >= by - 6; y--) {
            BlockPos p = new BlockPos(bx, y, bz);
            var forma = sl.getBlockState(p).getCollisionShape(sl, p);
            if (!forma.isEmpty()) return y + forma.max(Direction.Axis.Y);
        }
        return Double.NaN;
    }

    /** Alzado: flota a {@link #ALTURA} del suelo (subiendo y bajando despacio) y se mueve el solo hacia su presa. */
    private void flotar(ServerLevel sl) {
        setNoGravity(true);
        fallDistance = 0;
        double piso = sueloBajo(sl);
        double meta = Double.isNaN(piso) ? getY() : piso + ALTURA + 0.12 * Math.sin(tickCount * 0.07);
        double vy = Mth.clamp((meta - getY()) * 0.2, -0.12, 0.12);
        Vec3 dm = getDeltaMovement();
        double vx = dm.x, vz = dm.z;
        LivingEntity t = getTarget();
        if (accion() == 0 && postura() == DE_PIE && t != null) {
            double dx = t.getX() - getX(), dz = t.getZ() - getZ(), d = Math.hypot(dx, dz);
            if (d > distanciaDeseada() && d > 1e-3) {
                double v = 0.095 * prisa();
                vx = dx / d * v;
                vz = dz / d * v;
            } else {
                vx *= 0.5;
                vz *= 0.5;
            }
        } else if (accion() == 0) {
            vx *= 0.6;
            vz *= 0.6;
        }
        setDeltaMovement(vx, vy, vz);
        hasImpulse = true;
        // la lampara de la cupula, encendida y crujiendo mientras flota
        if (enPelea()) {
            if (--lampara <= 0) {
                lampara = 360;
                double[] l = Osario.lampara();
                sl.playSound(null, l[0], l[1], l[2], DarkCraftMod.SFX_OSMUND_LAMPARA.get(), SoundSource.HOSTILE, 1.8f, 1.0f);
            }
            if (tickCount % 5 == 0 && Osario.enRelicario(getX(), pies(), getZ())) {
                double[] l = Osario.lampara();
                for (int k = 0; k < 4; k++) {
                    double a = k * Math.PI / 2 + tickCount * 0.01;
                    sl.sendParticles(ParticleTypes.SMALL_FLAME, l[0] + Math.cos(a) * 1.1, l[1] + 0.6, l[2] + Math.sin(a) * 1.1, 1, 0.05, 0.05, 0.05, 0.0);
                }
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, l[0], l[1] + 0.3, l[2], 1, 0.3, 0.1, 0.3, 0.005);
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
        coronaEspera = 80;
        muroEspera = 60;
        guirnaldasEspera = 160;
        giroEspera = 40;
        llamarEspera = 400;
        long ahora = level().getGameTime();
        if (ahora - ultimaEntrada > 20 * 60 * 4 || ahora < ultimaEntrada) {
            ultimaEntrada = ahora;
            habla(1);
            // se levanta mientras lo dice y no toca la campanilla ni ataca hasta acabarlo (si le pegan antes, si)
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
        soltarCalaveras();
        marcas.clear();
        huesos.clear();
        nubes.clear();
        entityData.set(ALZADO, false);
        setNoGravity(false);
        esperaRugido = 0;
        lampara = 0;
    }

    /** A media vida no toca la campanilla: se alza (y despues vuelve a llamar a los muertos). */
    @Override
    protected void alSegundaMitad() {
        empezar(OsmundAnims.ALZARSE);
        pausa = Math.max(pausa, 6);
    }

    @Override
    public void setTarget(LivingEntity t) {
        super.setTarget(t);
        if (t != null && !level().isClientSide) ultimaPresa = t;
    }

    /** De rodillas ante el relicario solo repara en quien entra en el Relicario y se le acerca (12 bloques). */
    @Override
    protected boolean percibe(LivingEntity p) {
        if (Osario.enRelicario(getX(), pies(), getZ()) && !Osario.enRelicario(p.getX(), p.getY(), p.getZ())) return false;
        if (postura() != DE_PIE) return distanceToSqr(p) < 12 * 12 && hasLineOfSight(p);
        return super.percibe(p);
    }

    /** Los suyos no son presa; peleando en el Relicario, los de fuera tampoco (no se le saca de alli). */
    @Override
    protected boolean objetivoValido(LivingEntity e) {
        if (e instanceof Ensamblado || e instanceof Osmund) return false;
        if (e instanceof Player && enPelea() && Osario.enRelicario(getX(), pies(), getZ()) && !Osario.enRelicario(e.getX(), e.getY(), e.getZ())) return false;
        return super.objetivoValido(e);
    }

    /** Los martillos le hacen un cuarto mas (sus huesos se rompen); si le pegan mientras dice su frase, deja de esperar. */
    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        if (!level().isClientSide && esperaRugido > tickCount && s.getEntity() instanceof Player) {
            esperaRugido = tickCount;
            pausa = Math.min(pausa, 8);
        }
        if (com.darkcraft.armas.Martillo.rompe(s)) cuanto *= 1.25f;
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
        if (coronaEspera > 0) coronaEspera--;
        if (muroEspera > 0) muroEspera--;
        if (guirnaldasEspera > 0) guirnaldasEspera--;
        if (giroEspera > 0) giroEspera--;
        if (llamarEspera > 0) llamarEspera--;
        ServerLevel sl = (ServerLevel) level();
        tickMarcas(sl);
        tickHuesos(sl);
        tickNubes(sl);
        tickCorona(sl);
        if (alzado()) flotar(sl);
        else if (isNoGravity()) setNoGravity(false);
        // ha caido aquel con quien peleaba
        if (enPelea() && getTarget() == null && ultimaPresa != null && ultimaPresa.isDeadOrDying()) {
            ultimaPresa = null;
            hablaSiCalla(3);
        }
    }

    /** En el cliente: contando, el hueso que coge y deja; el incensario humea; alzado, almas que le suben por el haz de huesos. */
    private void cliente() {
        if (!isAlive()) return;
        Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
        // el humo del incensario (a su derecha, delante)
        if (tickCount % 4 == 0 && postura() == DE_PIE) {
            double x = getX() + d.x * 0.7 - d.z * 0.75, z = getZ() + d.z * 0.7 + d.x * 0.75;
            level().addParticle(ParticleTypes.SMOKE, x, getY() + (alzado() ? 1.9 : 0.5), z, 0, 0.02, 0);
            if (random.nextInt(3) == 0) level().addParticle(ParticleTypes.SMALL_FLAME, x, getY() + (alzado() ? 1.8 : 0.4), z, 0, 0.01, 0);
        }
        // contando: cada 2,4 s deja un hueso en el suelo (el chasquido)
        if (postura() == REZA && accion() == 0 && tickCount % 48 == 0) {
            double x = getX() + d.x * 1.2 - d.z * 0.6, z = getZ() + d.z * 1.2 + d.x * 0.6;
            level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()), x, getY() + 0.1, z, 0, 0.05, 0);
            level().playLocalSound(getX(), getY() + 0.5, getZ(), SoundEvents.SKELETON_STEP, SoundSource.HOSTILE, 0.5f, 1.4f + random.nextFloat() * 0.2f, false);
        }
        if (alzado() && tickCount % 3 == 0)
            level().addParticle(ALMA, getX() + (random.nextDouble() - 0.5) * 0.8, getY() - ALTURA + random.nextDouble() * ALTURA, getZ() + (random.nextDouble() - 0.5) * 0.8,
                    0, 0.04, 0);
    }

    @Override
    public void die(DamageSource s) {
        boolean ya = !isAlive() && deathTime > 0;
        despedir();
        soltarCalaveras();
        marcas.clear();
        huesos.clear();
        nubes.clear();
        super.die(s);
        if (level().isClientSide || ya) return;
        // se le cae lo que le sostenia
        entityData.set(ALZADO, false);
        setNoGravity(false);
        hablaHasta = 0;
        habla(5);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 2.0, getZ(), 30, 0.8, 1.2, 0.8, 0.03);
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()), getX(), getY() + 0.3, getZ(), 40, 1.0, 0.3, 1.0, 0.15);
        }
    }

    /**
     * Al caer no se va en humo: se queda de rodillas con el incensario en el suelo, dice su ultima frase y suben las almas de los
     * cuarenta mil (luces que salen del suelo del Relicario y se van hacia la cupula); al final se deshace en huesos.
     */
    @Override
    protected void tickDeath() {
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        int fin = OsmundAnims.TICKS[OsmundAnims.MORIR] + 70;
        if (level() instanceof ServerLevel sl) {
            if (deathTime == 24) sl.playSound(null, getX(), getY(), getZ(), SoundEvents.CHAIN_FALL, SoundSource.HOSTILE, 1.2f, 0.7f);
            if (deathTime == 40) sl.playSound(null, getX(), getY() + 2, getZ(), DarkCraftMod.SFX_OSARIO_SUSURROS.get(), SoundSource.HOSTILE, 2.0f, 0.9f);
            // las almas: una cada tick, de todo el Relicario, que suben
            if (deathTime >= 40 && deathTime < 130) {
                double a = random.nextDouble() * Math.PI * 2, r = 2 + random.nextDouble() * 10;
                double x = getX() + Math.cos(a) * r, z = getZ() + Math.sin(a) * r;
                double y = Puestos.suelo(sl, Mth.floor(x), Mth.floor(getY()) + 2, Mth.floor(z));
                if (Math.abs(y - getY()) > 4) y = getY();
                sl.sendParticles(ParticleTypes.END_ROD, x, y + 0.6, z, 0, 0, 1, 0, 0.09);
                if (deathTime % 2 == 0) sl.sendParticles(ParticleTypes.SOUL, x, y + 0.5, z, 1, 0.05, 0.05, 0.05, 0.02);
            }
            if (deathTime % 4 == 0 && deathTime < fin - 6) sl.sendParticles(POLVO, getX(), getY() + 1.6, getZ(), 3, 0.5, 0.8, 0.5, 0.0);
            if (deathTime == fin - 2) {
                // se deshace en huesos; la aureola se rompe y sus velas se apagan
                Vec3 au = aureola();
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()), getX(), getY() + 1.4, getZ(), 120, 0.7, 1.2, 0.7, 0.1);
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()), au.x, au.y - 1.2, au.z, 40, 0.8, 0.6, 0.4, 0.15);
                sl.sendParticles(ParticleTypes.SMOKE, au.x, au.y - 1.0, au.z, 16, 0.7, 0.5, 0.3, 0.02);
                sl.sendParticles(POLVO, getX(), getY() + 1.6, getZ(), 60, 0.8, 1.4, 0.8, 0.03);
                sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 2.0, getZ(), 20, 0.6, 1.0, 0.6, 0.04);
                sl.playSound(null, getX(), getY() + 1, getZ(), SoundEvents.BONE_BLOCK_BREAK, SoundSource.HOSTILE, 1.6f, 0.6f);
            }
        }
        if (deathTime >= fin) remove(RemovalReason.KILLED);
    }

    @Override
    public void remove(RemovalReason r) {
        if (!level().isClientSide) {
            despedir();
            soltarCalaveras();
        }
        super.remove(r);
    }

    /** Mientras dice su primera frase se queda donde esta (volviendose); despues, cerca: pelea de cerca. */
    @Override
    protected double distanciaDeseada() {
        return enPelea() && tickCount < esperaRugido ? 99.0 : alzado() ? 3.6 : 3.4;
    }

    @Override
    protected double prisa() {
        return alzado() ? 1.2 : 1.0;
    }

    /** Alzado no anda: se mueve el ({@link #flotar}). */
    @Override
    protected boolean anda() {
        return !alzado();
    }

    /** Alzado, su presa le queda por debajo: la busca aunque este mas abajo. */
    @Override
    protected double alturaMaxima() {
        return alzado() ? 4.2 : 2.6;
    }

    @Override
    protected int pausaTras(int accion) {
        boolean a = alzado();
        return switch (accion) {
            case OsmundAnims.LLAMAR, OsmundAnims.ALZARSE -> 6;
            case OsmundAnims.GUIRNALDAS -> (a ? 6 : 10) + random.nextInt(8);
            case OsmundAnims.CORONA, OsmundAnims.MURO -> (a ? 8 : 12) + random.nextInt(10);
            default -> (a ? 7 : 11) + random.nextInt(12);
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

    /** Los huesos de los pies en la piedra, y la cadena del incensario. */
    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        playSound(tickCount % 2 == 0 ? SoundEvents.SKELETON_STEP : SoundEvents.CHAIN_STEP, 0.5f, 0.6f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_OSMUND_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_OSMUND_MUERTE.get();
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        spawnAtLocation(new ItemStack(DarkCraftMod.INCENSARIO_SACRISTAN.get()));
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_3.get(), 3));
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_2.get(), 2));
        spawnAtLocation(new ItemStack(DarkCraftMod.CENIZAS.get(com.darkcraft.combate.Habilidad.TORBELLINO).get()));
    }

    /** Para las pruebas y el comando: como esta. */
    public String estado() {
        return String.format(java.util.Locale.ROOT, "vida %.0f/%.0f, %s, %s en %.1f,%.1f,%.1f; incensazos %d (la nube frena a %d), giros %d, coronas %d "
                        + "(calaveras %d, fuera %d), muros %d (huesos que dan %d), guirnaldas %d (caen %d, dan %d), muertos %d (en pie %d)%s",
                getHealth(), getMaxHealth(), enPelea() ? "peleando" : "esperando", postura() == DE_PIE ? "de pie" : "contando de rodillas",
                getX(), getY(), getZ(), incensazos, nubesQueDan, giros, coronas, sueltas, calaverasFuera(), muros, huesosQueDan, guirnaldas, caidas,
                caidasQueDan, llamados, suyos(), alzado() ? " (alzado)" : "");
    }
}
