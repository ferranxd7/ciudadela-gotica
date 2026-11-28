package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.EfectosRaiz;
import com.darkcraft.morvain.Mina;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Hakon, el Capataz de las Minas de Hierro Negro (parte 7ar): el jefe de la mina. Era el capataz del turno de noche; abrio el nivel
 * tres, donde la veta roja sangra, y mando volar la pared tras la que se oian voces (libro 36, el libro de turnos). La noche del cielo
 * rojo la montana temblo, se hundio la Galeria del Rey y el turno se quedo abajo; el, en la Caverna del Filon, con la veta metida
 * dentro: le salen cristales rojos del hombro, del brazo y de la espalda. Sigue picando el corazon de la veta y llamando al turno con
 * el silbato. Grande y ancho, con su casco y su candil, la barba gris del polvo de hierro, el abrigo largo, el farol al costado y el
 * pico doble de hierro negro.
 * <ul>
 * <li>espera <b>picando</b> el corazon de la veta, de espaldas a la caverna (se le oye desde la galeria); al ver a alguien dentro de
 * la caverna se vuelve mientras dice su frase y al acabarla toca el <b>silbato</b>: de los escombros de los bordes de la caverna salen
 * enterrados del turno (efimeros: se deshacen al acabar la pelea);</li>
 * <li>el <b>picotazo</b> (a dos manos, de arriba abajo: si lo paras con el escudo, el pico lo engancha y te rompe la guardia), el
 * <b>barrido</b> (muy abierto) y la <b>embestida</b> con el hombro de cristal (cruza media caverna);</li>
 * <li>la <b>veta</b>: clava el pico y los cristales rojos brotan en linea hacia su presa (lanzan por el aire); los <b>barrenos</b>:
 * enciende tres cartuchos en su farol y los lanza en abanico; el <b>derrumbe</b>: dos golpes al suelo, la caverna tiembla y del techo
 * caen piedras sobre cada uno (la marca en el suelo avisa);</li>
 * <li>a <b>media vida</b> se clava la mano de cristal en el suelo y <b>se bebe la veta</b>: desde entonces luce, va mas deprisa, la veta
 * brota tambien a su alrededor, lanza cinco cartuchos, caen mas piedras y vuelve a llamar al turno.</li>
 * </ul>
 * Habla (ElevenLabs, en ingles con subtitulos; {@link Voces#BASE_CAPATAZ}): 1 al empezar (no la repite en 4 min), 2 al beberse la
 * veta, 3 si cae su presa, 4 los primeros barrenos y 5 al caer. Al caer se queda de rodillas apoyado en el pico, su farol se apaga y
 * suben las cuarenta lamparas del turno (las almas); lo demas (el cartel con sus ecos, la hazana {@code capataz} y el bestiario) lo
 * reparte {@code valdrenn/Guardianes}; los enterrados de la mina descansan y ya no vuelven ({@code Mina.paz}). Deja su pico
 * ({@link com.darkcraft.armas.PicoCapataz}).
 */
public class Capataz extends Guardian {
    public static final int ECOS = 15000;
    public static final int VOZ_BASE = Voces.BASE_CAPATAZ;
    /** Si se ha bebido la veta (a media vida: luce mas; lo sabe el cliente). */
    private static final EntityDataAccessor<Boolean> VETA = SynchedEntityData.defineId(Capataz.class, EntityDataSerializers.BOOLEAN);
    /** De la marca a la piedra (ticks) y su radio; la veta: hasta donde llega y el radio de cada cristal. */
    public static final int MARCA_T = 26;
    public static final double R_ROCA = 1.7, VETA_LARGO = 14.0, R_CRISTAL = 1.25;
    /** A cuantos llama el silbato (y el maximo en pie a la vez). */
    public static final int TURNO_MAX = 4;

    public static final DustParticleOptions ROJO = new DustParticleOptions(new Vector3f(0.95f, 0.18f, 0.08f), 1.3f),
            MARCA = new DustParticleOptions(new Vector3f(0.55f, 0.5f, 0.46f), 1.5f);

    /** La ultima vez que dijo su frase de entrada (tiempo del mundo): no la repite en cada intento. */
    private static long ultimaEntrada = Long.MIN_VALUE / 2;

    private record Marca(double x, double y, double z, int cuando) {}

    private record Cristal(double x, double y, double z, int cuando, double r, float mult) {}

    private final List<Marca> marcas = new ArrayList<>();
    private final List<Cristal> cristales = new ArrayList<>();
    private final Set<UUID> dados = new HashSet<>();
    private final List<Enterrado> suyos = new ArrayList<>();
    private int vetaEspera = 70, barrenosEspera = 90, derrumbeEspera = 140, embestidaEspera = 60, silbatoEspera, hablaHasta, esperaRugido;
    private boolean cuatro;
    private LivingEntity ultimaPresa;
    /** Para las pruebas. */
    private int picotazos, barridos, embestidas, vetas, cristalesQueDan, barrenos, cartuchos, derrumbes, rocas, rocasQueDan, llamados, guardias,
            absorbe;
    /** Para las pruebas: no elige golpes por su cuenta (lo que se le manda hacer, si; y lo ya lanzado sigue). */
    public boolean quieta;

    public Capataz(EntityType<? extends Capataz> t, Level l) {
        super(t, l, "capataz", BossEvent.BossBarColor.RED);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1000.0).add(Attributes.ATTACK_DAMAGE, 12.0).add(Attributes.ARMOR, 8.0)
                .add(Attributes.ARMOR_TOUGHNESS, 3.0).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(VETA, false);
    }

    /** Si se ha bebido la veta (desde media vida luce). */
    public boolean veta() {
        return entityData.get(VETA);
    }

    @Override
    public String tipo() {
        return "capataz";
    }

    @Override
    public int musica() {
        return 50;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return "capataz";
    }

    @Override
    protected int[] ticks() {
        return CapatazAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return CapatazAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return CapatazAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return CapatazAnims.MORIR;
    }

    @Override
    protected int accionRugido() {
        return CapatazAnims.SILBATO;
    }

    @Override
    protected int accionLevantarse() {
        return CapatazAnims.VOLVERSE;
    }

    // ------------------------------------------------------------------ para las pruebas

    public int picotazos() {
        return picotazos;
    }

    public int barridos() {
        return barridos;
    }

    public int embestidas() {
        return embestidas;
    }

    /** Las vetas (lineas de cristales) y los cristales que han dado a alguien. */
    public int vetas() {
        return vetas;
    }

    public int cristalesQueDan() {
        return cristalesQueDan;
    }

    /** Los barrenos y los cartuchos que ha lanzado. */
    public int barrenos() {
        return barrenos;
    }

    public int cartuchos() {
        return cartuchos;
    }

    /** Los derrumbes, las piedras que han caido y las que han dado. */
    public int derrumbes() {
        return derrumbes;
    }

    public int rocas() {
        return rocas;
    }

    public int rocasQueDan() {
        return rocasQueDan;
    }

    /** Los del turno que ha llamado y los que siguen en pie. */
    public int llamados() {
        return llamados;
    }

    public int suyos() {
        int n = 0;
        for (Enterrado e : suyos) if (e.isAlive() && !e.isRemoved()) n++;
        return n;
    }

    public List<Enterrado> turno() {
        return List.copyOf(suyos);
    }

    /** Las guardias que ha roto el pico. */
    public int guardias() {
        return guardias;
    }

    /** Las piedras que esperan su caida (pruebas). */
    public int marcasPendientes() {
        return marcas.size();
    }

    /** Para las pruebas: pasa ya a media vida (se bebe la veta), sin esperar a que acabe lo que hace. */
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

    /** Para las pruebas: lo que hace el pico con quien lo para con el escudo (le rompe la guardia; devuelve si el golpe entra). */
    public boolean engancha(Player p) {
        return pilla(CapatazAnims.PICOTAZO, 0, p);
    }

    // ------------------------------------------------------------------ que hace

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (quieta || Math.abs(presa.getY() - getY()) > 5.0) return 0;
        boolean v = veta();
        // el derrumbe: de lejos o de cerca, cada rato
        if (derrumbeEspera <= 0 && d < 22 && random.nextFloat() < (v ? 0.3f : 0.2f)) {
            derrumbeEspera = (v ? 190 : 280) + random.nextInt(60);
            return CapatazAnims.DERRUMBE;
        }
        // ya bebida la veta, si no le queda nadie del turno vuelve a silbar (de tarde en tarde)
        if (v && silbatoEspera <= 0 && suyos() == 0 && random.nextFloat() < 0.25f) {
            silbatoEspera = 600;
            return CapatazAnims.SILBATO;
        }
        if (d > 4.5 && d < 15 && barrenosEspera <= 0 && random.nextFloat() < 0.5f) {
            barrenosEspera = (v ? 130 : 190) + random.nextInt(40);
            return CapatazAnims.BARRENOS;
        }
        if (d > 3.5 && d < VETA_LARGO && vetaEspera <= 0 && random.nextFloat() < 0.45f) {
            vetaEspera = (v ? 100 : 150) + random.nextInt(40);
            return CapatazAnims.VETA;
        }
        if (d > 5.0 && d < 10 && embestidaEspera <= 0 && random.nextFloat() < 0.35f) {
            embestidaEspera = (v ? 120 : 170) + random.nextInt(40);
            return CapatazAnims.EMBESTIDA;
        }
        if (d < 4.3) {
            if (v && vetaEspera <= 40 && random.nextFloat() < 0.25f) {
                vetaEspera = 110 + random.nextInt(40);
                return CapatazAnims.VETA;
            }
            return random.nextFloat() < 0.55f ? CapatazAnims.PICOTAZO : CapatazAnims.BARRIDO;
        }
        return 0;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case CapatazAnims.PICOTAZO -> new Golpe(4.2f, 50f, 1.3f, 1.2f);
            case CapatazAnims.BARRIDO -> new Golpe(4.1f, 170f, 1.0f, 1.4f);
            case CapatazAnims.EMBESTIDA -> new Golpe(2.9f, 100f, 1.15f, 2.0f);
            default -> null;
        };
    }

    /**
     * El pico engancha el escudo: a quien para el picotazo con un escudo se le baja (5 s sin poder cubrirse, como con el hacha del
     * juego) y el golpe entra entero.
     */
    @Override
    protected boolean pilla(int accion, int n, LivingEntity le) {
        if (accion == CapatazAnims.PICOTAZO && le instanceof Player p && p.isBlocking()) {
            ItemStack escudo = p.getUseItem();
            if (!escudo.isEmpty()) p.getCooldowns().addCooldown(escudo.getItem(), 100);
            p.stopUsingItem();
            p.level().broadcastEntityEvent(p, (byte) 30);
            level().playSound(null, p.getX(), p.getY() + 1, p.getZ(), DarkCraftMod.SFX_GUARDIA_ROTA.get(), SoundSource.HOSTILE, 1.1f, 0.85f);
            if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.CRIT, p.getX(), p.getY() + 1.1, p.getZ(), 16, 0.3, 0.3, 0.3, 0.3);
            guardias++;
        }
        return true;
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (accion == CapatazAnims.EMBESTIDA) le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1), this);
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = CapatazAnims.GOLPES[accion];
        switch (accion) {
            case CapatazAnims.PICOTAZO -> {
                if (t == g[0] - 11) playSound(DarkCraftMod.SFX_CAPATAZ_PICO.get(), 1.9f, 0.9f + random.nextFloat() * 0.15f);
                if (t == g[0] - 6) empujar(0.25);
                if (t == g[0]) {
                    picotazos++;
                    impacto(3.2, 1.0f);
                    // con la veta bebida, del golpe brotan tres cristales
                    if (veta() && level() instanceof ServerLevel sl) {
                        Vec3 d = Vec3.directionFromRotation(0, getYRot());
                        for (int k = -1; k <= 1; k++) brotar(sl, getX() + d.x * 3.4 - d.z * k * 1.2, getZ() + d.z * 3.4 + d.x * k * 1.2, 3, 0.7f);
                    }
                }
            }
            case CapatazAnims.BARRIDO -> {
                if (t == g[0] - 6) playSound(DarkCraftMod.SFX_CAPATAZ_PICO.get(), 1.6f, 1.15f);
                if (t == g[0] - 3) empujar(0.3);
                if (t == g[0]) {
                    barridos++;
                    if (level() instanceof ServerLevel sl)
                        for (int j = 0; j <= 10; j++) {
                            double a = Math.toRadians(getYRot() + 90 - 85 + 170 * j / 10.0);
                            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, getX() + Math.cos(a) * 3.4, getY() + 1.4, getZ() + Math.sin(a) * 3.4, 1, 0, 0, 0, 0);
                        }
                }
            }
            case CapatazAnims.EMBESTIDA -> {
                if (t == 2) playSound(DarkCraftMod.SFX_CAPATAZ_EMBESTIDA.get(), 2.0f, 1.0f);
                if (t == 4) embestidas++;
                if (t >= 5 && t <= g[0] + 1) {
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    setDeltaMovement(d.x * 0.78, getDeltaMovement().y, d.z * 0.78);
                    hasImpulse = true;
                    if (level() instanceof ServerLevel sl && t % 2 == 0) {
                        sl.sendParticles(Enterrado.POLVO_CLARO, getX(), getY() + 0.2, getZ(), 6, 0.5, 0.1, 0.5, 0.01);
                        sl.sendParticles(ROJO, getX(), getY() + 2.2, getZ(), 4, 0.5, 0.5, 0.5, 0.0);
                    }
                }
                if (t == g[0] && level() instanceof ServerLevel sl) {
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState()), getX() + d.x * 1.6,
                            getY() + 1.8, getZ() + d.z * 1.6, 20, 0.5, 0.5, 0.5, 0.2);
                }
            }
            case CapatazAnims.VETA -> {
                if (t == g[0] - 4) playSound(DarkCraftMod.SFX_CAPATAZ_VETA.get(), 2.2f, 1.0f);
                if (t == g[0]) {
                    impacto(2.6, 1.3f);
                    lineaDeVeta();
                    if (veta()) anilloDeVeta(3.2, 10, 6);
                }
            }
            case CapatazAnims.BARRENOS -> {
                if (t == 6) {
                    playSound(DarkCraftMod.SFX_ENTERRADO_MECHA.get(), 1.4f, 0.85f);
                    if (!cuatro) {
                        cuatro = true;
                        hablaSiCalla(4);
                    }
                }
                // las mechas encendidas en el farol (a su izquierda, a la cintura)
                if (t >= 6 && t < g[0] && t % 2 == 0 && level() instanceof ServerLevel sl) {
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    sl.sendParticles(ParticleTypes.SMALL_FLAME, getX() + d.x * 0.6 + d.z * 0.9, getY() + 1.6, getZ() + d.z * 0.6 - d.x * 0.9, 3, 0.15, 0.1, 0.15, 0.01);
                }
                if (t == g[0]) lanzarBarrenos();
            }
            case CapatazAnims.DERRUMBE -> {
                if (t == g[0]) {
                    impacto(2.4, 1.3f);
                    playSound(DarkCraftMod.SFX_CAPATAZ_TEMBLOR.get(), 2.6f, 1.0f);
                }
                if (t == g[1]) {
                    impacto(2.4, 1.3f);
                    derrumbe();
                }
            }
            case CapatazAnims.SILBATO -> {
                if (t == 10) playSound(DarkCraftMod.SFX_CAPATAZ_SILBATO.get(), 3.0f, 1.0f);
                if (t == 16) llamarTurno(veta() ? 2 : 0);
                if (t >= 10 && t <= 34 && t % 3 == 0 && level() instanceof ServerLevel sl)
                    sl.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.GRAVEL.defaultBlockState()), getX(), getY() + 9, getZ(), 6, 6, 1, 6, 0);
            }
            case CapatazAnims.ABSORBER -> {
                if (t == 2) playSound(DarkCraftMod.SFX_CAPATAZ_ABSORBER.get(), 2.6f, 1.0f);
                // la veta le corre por el brazo: chispas rojas del suelo a la mano de cristal
                if (t >= 10 && t <= 40 && level() instanceof ServerLevel sl) {
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    double mx = getX() + d.x * 1.0 + d.z * 0.9, mz = getZ() + d.z * 1.0 - d.x * 0.9;
                    for (int k = 0; k < 3; k++) {
                        double a = random.nextDouble() * Math.PI * 2, r = 2 + random.nextDouble() * 5;
                        double s = ((t - 10) / 30.0 + k / 3.0) % 1.0;
                        sl.sendParticles(ROJO, Mth.lerp(s, mx + Math.cos(a) * r, mx), getY() + 0.2 + s * 0.6, Mth.lerp(s, mz + Math.sin(a) * r, mz), 1, 0, 0, 0, 0);
                    }
                    if (t % 4 == 0) sl.sendParticles(ParticleTypes.LAVA, mx, getY() + 0.3, mz, 2, 0.3, 0.1, 0.3, 0.0);
                }
                if (t == 44) beberVeta();
            }
            case CapatazAnims.VOLVERSE -> {
                if (t == 2 && level() instanceof ServerLevel sl) {
                    sl.sendParticles(Enterrado.POLVO_CLARO, getX(), getY() + 1.6, getZ(), 20, 0.8, 0.8, 0.8, 0.02);
                    sl.sendParticles(ROJO, getX(), getY() + 2.4, getZ(), 12, 0.6, 0.6, 0.6, 0.0);
                }
                // mientras deja de picar se vuelve entero hacia quien llega (como mucho 16 grados por tick: media vuelta en doce)
                if (t >= 6 && getTarget() != null) girarHacia(getTarget(), 16f);
            }
            default -> {
            }
        }
    }

    /** Donde da el pico contra el suelo: grava, chispas, polvo, el ruido; y lo que cae del techo. */
    private void impacto(double lejos, float fuerte) {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * lejos, z = getZ() + d.z * lejos;
        BlockState suelo = level().getBlockState(BlockPos.containing(x, getY() - 0.5, z));
        if (!suelo.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), x, getY() + 0.1, z, (int) (24 * fuerte), 0.5, 0.05, 0.5, 0.15);
        sl.sendParticles(ParticleTypes.CRIT, x, getY() + 0.2, z, 8, 0.2, 0.1, 0.2, 0.2);
        sl.sendParticles(Enterrado.POLVO_CLARO, x, getY() + 0.4, z, (int) (10 * fuerte), 0.6, 0.2, 0.6, 0.02);
        sl.sendParticles(ROJO, x, getY() + 0.3, z, 6, 0.4, 0.1, 0.4, 0.0);
        if (fuerte > 1f)
            for (int k = 0; k < 8; k++)
                sl.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.GRAVEL.defaultBlockState()), getX() + (random.nextDouble() - 0.5) * 10,
                        getY() + 6 + random.nextDouble() * 3, getZ() + (random.nextDouble() - 0.5) * 10, 2, 0.2, 0.1, 0.2, 0.0);
    }

    // ------------------------------------------------------------------ la veta: los cristales que brotan

    /** El cristal rojo de la veta (encendido) o, fuera del juego, lo que haga sus veces. */
    private static BlockState cristal() {
        return com.darkcraft.gen.TorreCupula.gema(true);
    }

    /** Los cristales corren por el suelo hacia delante (cada 1,3 bloques, dos ticks uno de otro), hasta {@link #VETA_LARGO}. */
    private void lineaDeVeta() {
        if (!(level() instanceof ServerLevel sl)) return;
        vetas++;
        dados.clear();
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        int k = 0;
        for (double a = 2.4; a <= VETA_LARGO; a += 1.3, k++) {
            if (!brotar(sl, getX() + d.x * a, getZ() + d.z * a, 2 + 2 * k, 0.9f)) break;
        }
    }

    /** Un anillo de cristales a su alrededor (con la veta bebida). */
    private void anilloDeVeta(double r, int n, int retraso) {
        if (!(level() instanceof ServerLevel sl)) return;
        for (int i = 0; i < n; i++) {
            double a = i * Math.PI * 2 / n + random.nextDouble() * 0.3;
            brotar(sl, getX() + Math.cos(a) * r, getZ() + Math.sin(a) * r, retraso, 0.8f);
        }
    }

    /** Un cristal que brota en (x, z) dentro de {@code retraso} ticks (antes, el aviso: polvo y chispas rojas). Falso si no hay suelo. */
    private boolean brotar(ServerLevel sl, double x, double z, int retraso, float mult) {
        double y = suelo(sl, x, z);
        if (Double.isNaN(y)) return false;
        sl.sendParticles(ROJO, x, y + 0.1, z, 6, 0.3, 0.02, 0.3, 0.0);
        sl.sendParticles(Enterrado.POLVO_CLARO, x, y + 0.1, z, 3, 0.3, 0.02, 0.3, 0.01);
        EfectosRaiz.estaca(sl, cristal(), x, y - 0.2, z, EfectosRaiz.ang(sl.random), 0.3f * (sl.random.nextFloat() - 0.5f), 1.7f + sl.random.nextFloat() * 0.9f,
                0.45f + sl.random.nextFloat() * 0.25f, retraso, 24);
        cristales.add(new Cristal(x, y, z, tickCount + retraso + 1, R_CRISTAL, mult));
        return true;
    }

    /** Lo alto del suelo bajo (x, z) cerca de su altura, o NaN si no hay (un hueco, un muro). */
    private double suelo(ServerLevel sl, double x, double z) {
        int bx = Mth.floor(x), bz = Mth.floor(z), by = Mth.floor(getY());
        for (int y = by + 1; y >= by - 3; y--) {
            BlockPos p = new BlockPos(bx, y, bz);
            BlockState s = sl.getBlockState(p);
            if (!s.getCollisionShape(sl, p).isEmpty() && sl.getBlockState(p.above()).getCollisionShape(sl, p.above()).isEmpty())
                return y + 1;
        }
        return Double.NaN;
    }

    /** Cada tick: los cristales que salen dan a quien pillan (lanzan por el aire); una vez por veta a cada uno. */
    private void tickCristales(ServerLevel sl) {
        if (cristales.isEmpty()) return;
        float base = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (int i = cristales.size() - 1; i >= 0; i--) {
            Cristal c = cristales.get(i);
            if (tickCount < c.cuando()) continue;
            cristales.remove(i);
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState()), c.x(), c.y() + 0.8, c.z(), 6, 0.3, 0.5, 0.3, 0.05);
            sl.sendParticles(ROJO, c.x(), c.y() + 1.0, c.z(), 4, 0.3, 0.6, 0.3, 0.0);
            for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, new AABB(c.x() - c.r() - 0.5, c.y() - 0.5, c.z() - c.r() - 0.5, c.x() + c.r() + 0.5,
                    c.y() + 2.6, c.z() + c.r() + 0.5), e -> e != this && e.isAlive() && objetivoValido(e))) {
                if (dados.contains(le.getUUID()) || le.distanceToSqr(c.x(), le.getY(), c.z()) > c.r() * c.r()) continue;
                if (le.hurt(damageSources().mobAttack(this), base * c.mult())) {
                    dados.add(le.getUUID());
                    cristalesQueDan++;
                    le.setDeltaMovement(le.getDeltaMovement().add(0, 0.55, 0));
                    le.hurtMarked = true;
                }
            }
        }
    }

    // ------------------------------------------------------------------ los barrenos

    /** Tres cartuchos en abanico hacia su presa (cinco con la veta bebida), cada uno a donde caiga. */
    private void lanzarBarrenos() {
        if (!(level() instanceof ServerLevel sl)) return;
        barrenos++;
        LivingEntity t = getTarget();
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double ox = getX() + d.x * 0.8, oz = getZ() + d.z * 0.8, oy = getY() + getBbHeight() * 0.85;
        double dist = t == null ? 9 : Mth.clamp(Math.hypot(t.getX() - getX(), t.getZ() - getZ()), 5, 14);
        float yaw = t == null ? getYRot() : (float) Math.toDegrees(Math.atan2(t.getZ() - getZ(), t.getX() - getX())) - 90f;
        int n = veta() ? 5 : 3;
        for (int i = 0; i < n; i++) {
            float a = yaw + (n == 1 ? 0 : -26f + 52f * i / (n - 1)) + (random.nextFloat() - 0.5f) * 6f;
            Vec3 dir = Vec3.directionFromRotation(0, a);
            double r = dist + (random.nextDouble() - 0.5) * 3;
            double tx = getX() + dir.x * r, tz = getZ() + dir.z * r;
            double ty = suelo(sl, tx, tz);
            if (Double.isNaN(ty)) ty = t == null ? getY() : t.getY();
            Cartucho c = new Cartucho(level(), this);
            c.setPos(ox, oy, oz);
            c.setDeltaMovement(Cantaro.tiro(ox, oy, oz, tx, ty + 0.1, tz));
            level().addFreshEntity(c);
            cartuchos++;
        }
        playSound(DarkCraftMod.SFX_GOLPE_LIGERO.get(), 1.0f, 0.7f);
    }

    // ------------------------------------------------------------------ el derrumbe

    /** La caverna tiembla: una marca bajo los pies de cada uno (y alguna mas) y, al rato, la piedra que cae del techo. */
    private void derrumbe() {
        if (!(level() instanceof ServerLevel sl)) return;
        derrumbes++;
        List<LivingEntity> blancos = new ArrayList<>(sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(24, 6, 24),
                e -> e != this && e.isAlive() && objetivoValido(e) && (e instanceof Player || e == getTarget())));
        for (LivingEntity le : blancos) {
            if (le.distanceToSqr(this) > 24 * 24) continue;
            marcar(sl, le.getX(), le.getZ(), le.getY());
            if (veta()) {
                double a = random.nextDouble() * Math.PI * 2, r = 2.4 + random.nextDouble() * 1.5;
                marcar(sl, le.getX() + Math.cos(a) * r, le.getZ() + Math.sin(a) * r, le.getY());
            }
        }
        // y algunas al azar a su alrededor
        int extra = veta() ? 5 : 3;
        for (int i = 0; i < extra; i++) {
            double a = random.nextDouble() * Math.PI * 2, r = 3.5 + random.nextDouble() * 6;
            marcar(sl, getX() + Math.cos(a) * r, getZ() + Math.sin(a) * r, getY());
        }
    }

    private void marcar(ServerLevel sl, double x, double z, double cerca) {
        double y = Puestos.suelo(sl, Mth.floor(x), Mth.floor(cerca) + 2, Mth.floor(z));
        if (Math.abs(y - cerca) > 4) y = cerca;
        marcas.add(new Marca(x, y, z, tickCount + MARCA_T));
        // la piedra, ya en el techo: tiembla arriba y cae a su hora
        int techo = techo(sl, x, y, z);
        BlockState piedra = switch (random.nextInt(4)) {
            case 0 -> Blocks.COBBLED_DEEPSLATE.defaultBlockState();
            case 1 -> Blocks.TUFF.defaultBlockState();
            case 2 -> Blocks.BLACKSTONE.defaultBlockState();
            default -> Blocks.DEEPSLATE.defaultBlockState();
        };
        EfectosRaiz.cae(sl, piedra, x, y, z, Math.max(3f, techo - 1.4f), 1.1f + random.nextFloat() * 0.5f, 0.9f + random.nextFloat() * 0.5f, MARCA_T - 7, 7);
        sl.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.GRAVEL.defaultBlockState()), x, y + Math.max(3, techo - 0.5), z, 8, 0.5, 0.1, 0.5, 0.0);
        sl.playSound(null, x, y, z, DarkCraftMod.SFX_MINA_CRUJE.get(), SoundSource.HOSTILE, 0.9f, 0.8f + random.nextFloat() * 0.3f);
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

    /** Cada tick: las marcas echan su aviso y, a su hora, la piedra da en el suelo. */
    private void tickMarcas(ServerLevel sl) {
        for (int i = marcas.size() - 1; i >= 0; i--) {
            Marca m = marcas.get(i);
            if (tickCount < m.cuando()) {
                if (tickCount % 2 == 0)
                    for (int k = 0; k < 10; k++) {
                        double a = (k / 10.0 + tickCount * 0.02) * Math.PI * 2;
                        sl.sendParticles(MARCA, m.x() + Math.cos(a) * R_ROCA, m.y() + 0.08, m.z() + Math.sin(a) * R_ROCA, 1, 0, 0, 0, 0);
                    }
                if (tickCount % 4 == 0) sl.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.GRAVEL.defaultBlockState()), m.x(), m.y() + 3,
                        m.z(), 2, 0.4, 0.5, 0.4, 0.0);
                continue;
            }
            marcas.remove(i);
            roca(sl, m);
        }
    }

    /** La piedra da en el suelo: a quien pilla debajo, dano y lentitud. */
    private void roca(ServerLevel sl, Marca m) {
        rocas++;
        sl.playSound(null, m.x(), m.y(), m.z(), DarkCraftMod.SFX_CAPATAZ_ROCA.get(), SoundSource.HOSTILE, 1.6f, 0.85f + random.nextFloat() * 0.3f);
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBBLED_DEEPSLATE.defaultBlockState()), m.x(), m.y() + 0.4, m.z(), 30, 0.6, 0.3, 0.6, 0.15);
        sl.sendParticles(Enterrado.POLVO_CLARO, m.x(), m.y() + 0.5, m.z(), 14, 0.8, 0.4, 0.8, 0.02);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.85f;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, new AABB(m.x() - 2, m.y() - 1, m.z() - 2, m.x() + 2, m.y() + 3, m.z() + 2),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            if (le.distanceToSqr(m.x(), le.getY(), m.z()) > R_ROCA * R_ROCA) continue;
            if (le.hurt(damageSources().mobAttack(this), dano)) {
                rocasQueDan++;
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1), this);
            }
        }
    }

    // ------------------------------------------------------------------ el turno

    /**
     * El silbato: de los escombros de los bordes de la caverna salen los del turno (dos, y uno mas por jugador de mas; como mucho
     * {@link #TURNO_MAX} en pie), efimeros: se deshacen al acabar la pelea. {@code mas}: los que se suman (al beberse la veta).
     */
    private void llamarTurno(int mas) {
        if (!(level() instanceof ServerLevel sl)) return;
        List<ServerPlayer> cerca = new ArrayList<>();
        for (ServerPlayer p : sl.players()) if (p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) < 40 * 40) cerca.add(p);
        int n = Math.min(TURNO_MAX - suyos(), Math.max(2, Math.min(TURNO_MAX, 1 + cerca.size())) + mas);
        if (n <= 0) return;
        List<int[]> sitios = Mina.enFilon(getX(), getY(), getZ()) ? new ArrayList<>(Mina.turno()) : new ArrayList<>();
        if (sitios.isEmpty())
            for (int k = 0; k < 6; k++) {
                double a = k * Math.PI / 3;
                sitios.add(new int[]{Mth.floor(getX() + Math.cos(a) * 8), Mth.floor(getZ() + Math.sin(a) * 8)});
            }
        // los sitios mas lejos de los jugadores primero (que no salgan encima de nadie)
        sitios.sort((a, b) -> Double.compare(lejania(b, cerca), lejania(a, cerca)));
        int[] variantes = veta() ? new int[]{Enterrado.CARGADOR, Enterrado.PICADOR, Enterrado.BARRENERO, Enterrado.PICADOR}
                : new int[]{Enterrado.PICADOR, Enterrado.BARRENERO, Enterrado.PICADOR, Enterrado.CARGADOR};
        int salen = 0;
        for (int[] q : sitios) {
            if (salen >= n) break;
            int bx = q[0], bz = q[1];
            double y = Puestos.suelo(sl, bx, Mth.floor(getY()) + 2, bz);
            if (Math.abs(y - getY()) > 2.6 || lejania(q, cerca) < 3.0) continue;
            Enterrado e = DarkCraftMod.ENTERRADO.get().create(sl);
            if (e == null) return;
            float mira = (float) Math.toDegrees(Math.atan2(getZ() - (bz + 0.5), getX() - (bx + 0.5))) - 90f;
            e.moveTo(bx + 0.5, y, bz + 0.5, mira, 0f);
            e.variante(variantes[(salen + llamados) % variantes.length]);
            if (!sl.noCollision(e)) continue;
            // en los escombros: salen de golpe (sin puesto: no vuelven)
            e.asignar(null, -1, BlockPos.containing(bx + 0.5, y, bz + 0.5), mira, TUMBADO, 0);
            e.efimero(240);
            sl.addFreshEntity(e);
            LivingEntity t = getTarget() != null ? getTarget()
                    : sl.getNearestPlayer(e.getX(), e.getY(), e.getZ(), 30, x -> x instanceof Player pl && objetivoValido(pl));
            if (t != null) e.setTarget(t);
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GRAVEL.defaultBlockState()), bx + 0.5, y + 0.3, bz + 0.5, 30, 0.5, 0.3, 0.5, 0.1);
            sl.playSound(null, bx + 0.5, y, bz + 0.5, DarkCraftMod.SFX_ENTERRADO_ALZARSE.get(), SoundSource.HOSTILE, 1.4f, 0.8f + random.nextFloat() * 0.2f);
            suyos.add(e);
            salen++;
        }
        llamados += salen;
        DarkCraftMod.LOGGER.info("[DarkCraft] El Capataz toca el silbato: salen {} del turno ({} jugadores)", salen, cerca.size());
    }

    private static double lejania(int[] q, List<ServerPlayer> jugadores) {
        double m = Double.MAX_VALUE;
        for (ServerPlayer p : jugadores) m = Math.min(m, Math.hypot(p.getX() - (q[0] + 0.5), p.getZ() - (q[1] + 0.5)));
        return m;
    }

    private void despedir() {
        for (Enterrado e : suyos) if (e.isAlive() && !e.isRemoved()) e.disolver();
        suyos.clear();
    }

    // ------------------------------------------------------------------ beberse la veta

    /** Se levanta con la veta dentro: desde ahora luce, va mas deprisa, los cristales le brotan alrededor y vuelve a llamar al turno. */
    private void beberVeta() {
        if (!(level() instanceof ServerLevel sl)) return;
        absorbe++;
        entityData.set(VETA, true);
        sl.sendParticles(ROJO, getX(), getY() + 2.0, getZ(), 60, 0.8, 1.2, 0.8, 0.0);
        sl.sendParticles(ParticleTypes.LAVA, getX(), getY() + 1.0, getZ(), 12, 0.6, 0.4, 0.6, 0.0);
        anilloDeVeta(2.6, 8, 1);
        anilloDeVeta(4.8, 12, 8);
        hablaHasta = 0;
        habla(2);
        llamarTurno(1);
        vetaEspera = Math.min(vetaEspera, 50);
        derrumbeEspera = Math.min(derrumbeEspera, 120);
        DarkCraftMod.LOGGER.info("[DarkCraft] El Capataz se bebe la veta");
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
        vetaEspera = 70;
        barrenosEspera = 90;
        derrumbeEspera = 140;
        embestidaEspera = 60;
        silbatoEspera = 400;
        long ahora = level().getGameTime();
        if (ahora - ultimaEntrada > 20 * 60 * 4 || ahora < ultimaEntrada) {
            ultimaEntrada = ahora;
            habla(1);
            // se vuelve mientras lo dice y no silba ni ataca hasta acabarlo
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
        marcas.clear();
        cristales.clear();
        entityData.set(VETA, false);
        esperaRugido = 0;
    }

    /** A media vida no silba: se clava la mano de cristal en el suelo y se bebe la veta (y despues vuelve a llamar al turno). */
    @Override
    protected void alSegundaMitad() {
        empezar(CapatazAnims.ABSORBER);
        pausa = Math.max(pausa, 6);
    }

    @Override
    public void setTarget(LivingEntity t) {
        super.setTarget(t);
        if (t != null && !level().isClientSide) ultimaPresa = t;
    }

    /** Picando el corazon de la veta solo repara en quien entra en la Caverna del Filon y se le acerca (12 bloques). */
    @Override
    protected boolean percibe(LivingEntity p) {
        if (Mina.enFilon(getX(), getY(), getZ()) && !Mina.enFilon(p.getX(), p.getY(), p.getZ())) return false;
        if (postura() != DE_PIE) return distanceToSqr(p) < 12 * 12 && hasLineOfSight(p);
        return super.percibe(p);
    }

    /** Los suyos no son presa; peleando en la caverna, los de fuera tampoco (no se le saca de alli). */
    @Override
    protected boolean objetivoValido(LivingEntity e) {
        if (e instanceof Enterrado) return false;
        if (e instanceof Player && enPelea() && Mina.enFilon(getX(), getY(), getZ()) && !Mina.enFilon(e.getX(), e.getY(), e.getZ())) return false;
        return super.objetivoValido(e);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            cliente();
            return;
        }
        if (!isAlive()) return;
        if (vetaEspera > 0) vetaEspera--;
        if (barrenosEspera > 0) barrenosEspera--;
        if (derrumbeEspera > 0) derrumbeEspera--;
        if (embestidaEspera > 0) embestidaEspera--;
        if (silbatoEspera > 0) silbatoEspera--;
        ServerLevel sl = (ServerLevel) level();
        tickMarcas(sl);
        tickCristales(sl);
        // ha caido aquel con quien peleaba
        if (enPelea() && getTarget() == null && ultimaPresa != null && ultimaPresa.isDeadOrDying()) {
            ultimaPresa = null;
            hablaSiCalla(3);
        }
    }

    /** En el cliente: picando, las chispas y el golpe en la veta; con la veta bebida, chispas rojas que le salen de los cristales. */
    private void cliente() {
        if (!isAlive()) return;
        Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
        // picando el corazon de la veta: en el segundo 1,2 de cada 1,8 da en la roca (van a la vez que el dibujo)
        if (postura() == REZA && accion() == 0 && tickCount % 36 == 24) {
            double x = getX() + d.x * 2.4, z = getZ() + d.z * 2.4;
            for (int k = 0; k < 4; k++)
                level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, k % 2 == 0 ? Blocks.REDSTONE_BLOCK.defaultBlockState()
                        : Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState()), x, getY() + 2.6, z, 0, 0.05, 0);
            level().addParticle(ParticleTypes.CRIT, x, getY() + 2.6, z, -d.x * 0.1, 0.05, -d.z * 0.1);
            level().addParticle(ROJO, x, getY() + 2.6, z, 0, 0.02, 0);
            level().playLocalSound(getX(), getY() + 2, getZ(), DarkCraftMod.SFX_ENTERRADO_PICA.get(), SoundSource.HOSTILE, 1.2f, 0.7f + random.nextFloat() * 0.1f, false);
        }
        if (veta() && tickCount % 2 == 0)
            level().addParticle(ROJO, getX() + d.z * 0.8 + (random.nextDouble() - 0.5) * 0.8, getY() + 2.2 + random.nextDouble() * 1.4,
                    getZ() - d.x * 0.8 + (random.nextDouble() - 0.5) * 0.8, 0, 0.03, 0);
    }

    @Override
    public void die(DamageSource s) {
        boolean ya = !isAlive() && deathTime > 0;
        despedir();
        marcas.clear();
        cristales.clear();
        super.die(s);
        if (level().isClientSide || ya) return;
        hablaHasta = 0;
        habla(5);
        if (level() instanceof ServerLevel sl) sl.sendParticles(ROJO, getX(), getY() + 2.4, getZ(), 40, 0.8, 1.2, 0.8, 0.0);
    }

    /**
     * Al caer no se va en humo: se queda de rodillas apoyado en el pico, su farol se apaga, dice su ultima frase y suben las
     * cuarenta lamparas del turno (luces que salen del suelo de la caverna y se van hacia arriba); al final se deshace en polvo y la
     * veta que llevaba dentro se rompe.
     */
    @Override
    protected void tickDeath() {
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        int fin = CapatazAnims.TICKS[CapatazAnims.MORIR] + 70;
        if (level() instanceof ServerLevel sl) {
            if (deathTime == 26) sl.playSound(null, getX(), getY(), getZ(), SoundEvents.CHAIN_FALL, SoundSource.HOSTILE, 1.4f, 0.6f);
            if (deathTime == 40) sl.playSound(null, getX(), getY() + 2, getZ(), DarkCraftMod.SFX_CAPATAZ_ALMAS.get(), SoundSource.HOSTILE, 2.4f, 1.0f);
            // las cuarenta lamparas: una cada dos ticks, de los bordes de la caverna, que suben
            if (deathTime >= 40 && deathTime < 120 && deathTime % 2 == 0) {
                double a = random.nextDouble() * Math.PI * 2, r = 3 + random.nextDouble() * 12;
                double x = getX() + Math.cos(a) * r, z = getZ() + Math.sin(a) * r;
                double y = Puestos.suelo(sl, Mth.floor(x), Mth.floor(getY()) + 2, Mth.floor(z));
                if (Math.abs(y - getY()) > 4) y = getY();
                sl.sendParticles(ParticleTypes.END_ROD, x, y + 0.6, z, 0, 0, 1, 0, 0.09);
                sl.sendParticles(ParticleTypes.SMALL_FLAME, x, y + 0.5, z, 2, 0.05, 0.05, 0.05, 0.01);
            }
            if (deathTime % 4 == 0 && deathTime < fin - 6)
                sl.sendParticles(Enterrado.POLVO_CLARO, getX(), getY() + 1.6, getZ(), 3, 0.5, 0.8, 0.5, 0.0);
            if (deathTime == fin - 2) {
                // se deshace en polvo y la veta se rompe
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GRAVEL.defaultBlockState()), getX(), getY() + 1.6, getZ(), 120, 0.7, 1.2, 0.7, 0.1);
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState()), getX(), getY() + 2.4, getZ(), 40, 0.6, 0.8, 0.6, 0.15);
                sl.sendParticles(Enterrado.POLVO_CLARO, getX(), getY() + 1.8, getZ(), 60, 0.8, 1.4, 0.8, 0.03);
                sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 2.0, getZ(), 20, 0.6, 1.0, 0.6, 0.04);
                sl.playSound(null, getX(), getY() + 1, getZ(), DarkCraftMod.SFX_MINA_DERRUMBE.get(), SoundSource.HOSTILE, 1.0f, 1.3f);
            }
        }
        if (deathTime >= fin) remove(RemovalReason.KILLED);
    }

    @Override
    public void remove(RemovalReason r) {
        if (!level().isClientSide) despedir();
        super.remove(r);
    }

    /** Mientras dice su primera frase se queda donde esta (volviendose); despues, cerca: pelea de cerca. */
    @Override
    protected double distanciaDeseada() {
        return enPelea() && tickCount < esperaRugido ? 99.0 : 3.6;
    }

    @Override
    protected double prisa() {
        return veta() ? 1.2 : 1.0;
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (accion) {
            case CapatazAnims.SILBATO, CapatazAnims.ABSORBER -> 6;
            case CapatazAnims.DERRUMBE -> (veta() ? 6 : 10) + random.nextInt(8);
            case CapatazAnims.BARRENOS, CapatazAnims.VETA -> (veta() ? 8 : 12) + random.nextInt(10);
            default -> (veta() ? 7 : 11) + random.nextInt(12);
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

    /** Las botas herradas en la roca, y el farol que golpea el costado. */
    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        playSound(tickCount % 2 == 0 ? SoundEvents.ANCIENT_DEBRIS_STEP : SoundEvents.CHAIN_STEP, 0.6f, 0.7f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_CAPATAZ_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_CAPATAZ_MUERTE.get();
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        spawnAtLocation(new ItemStack(DarkCraftMod.PICO_CAPATAZ.get()));
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_3.get(), 3));
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_2.get(), 2));
        spawnAtLocation(new ItemStack(DarkCraftMod.CENIZAS.get(com.darkcraft.combate.Habilidad.LLAMAS).get()));
    }

    /** Para las pruebas y el comando: como esta. */
    public String estado() {
        return String.format(java.util.Locale.ROOT, "vida %.0f/%.0f, %s, %s en %.1f,%.1f,%.1f; picotazos %d (guardias rotas %d), barridos %d, "
                        + "embestidas %d, vetas %d (cristales que dan %d), barrenos %d (cartuchos %d), derrumbes %d (piedras %d, dan %d), "
                        + "turno %d (en pie %d)%s",
                getHealth(), getMaxHealth(), enPelea() ? "peleando" : "esperando", postura() == DE_PIE ? "de pie" : "picando",
                getX(), getY(), getZ(), picotazos, guardias, barridos, embestidas, vetas, cristalesQueDan, barrenos, cartuchos, derrumbes, rocas,
                rocasQueDan, llamados, suyos(), veta() ? " (con la veta bebida)" : "");
    }
}
