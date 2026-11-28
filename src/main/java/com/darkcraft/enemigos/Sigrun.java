package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.mision.Duelo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Sigrun, la Guardiana del Paso (parte 7ap; respuestas 95 y 117 del plan): la ultima de los diez que el capitan de la Capital mando a
 * guardar el Paso del Colmillo para que la gente pudiera huir al monasterio. Lo guardaron nueve dias; sus nueve hermanos cayeron uno a
 * uno (sus espadas siguen clavadas en la nieve detras de ella) y ella juro que nadie pasaria mientras siguiera en pie. Sigue en pie.
 * No es un enemigo como los demas: <b>se habla con ella</b> ({@link Duelo}) y pelea en <b>duelo</b>, a quien lo acepta.
 * <ul>
 * <li>{@link #PAZ}: guarda el paso con la espada clavada en la nieve delante (no ataca a nadie; quien le pega, acepta el duelo).</li>
 * <li>{@link #DUELO}: arranca la espada de la nieve y la alza ({@code DESENVAINAR}); pelea tras su escudo de lagrima (de frente, sin
 * atacar, para casi todo; las flechas, todo; si le pegan mucho, contesta con un escudazo): <b>tajo</b> de arriba abajo, la <b>tanda</b>
 * de tres (fuera, dentro y a fondo), <b>escudazo</b> y <b>estocada</b> de lejos; a media vida golpea dos veces el escudo ("Bien...
 * ¡otra vez!") y suma el <b>giro</b> (la vuelta entera con la espada tendida) y el <b>salto</b> que cae con la espada en la nieve.
 * Solo pega y persigue a quien pelea con ella (los demas miran).</li>
 * <li>{@link #RENDIDA}: no muere: cuando le queda poca vida se rinde de rodillas ("Basta... Me has vencido") y espera a que se le
 * hable: acabar con ella o dejarla marchar.</li>
 * <li>{@link #CAIDA}: la rematan: se le escurre la espada, queda tendida en la nieve con ella en el pecho y se deshace en nieve.</li>
 * <li>{@link #MARCHA}: la dejan marchar: se levanta, saluda con la espada y se va paso arriba sin su escudo, hasta perderse en la
 * nieve.</li>
 * </ul>
 * Despues vuelve a su puesto para los demas (cada uno su duelo y su final): a quien ya acabo el suyo no le toca verla
 * ({@link #OCULTA}, lo pone el cliente), no la oye ni ve su barra.
 */
public class Sigrun extends Guardian {
    public static final int ECOS = 9000;
    /** Lo que hace (sincronizado: el modelo pone la postura de cada uno). */
    public static final int PAZ = 0, DUELO = 1, RENDIDA = 2, CAIDA = 3, MARCHA = 4;
    private static final EntityDataAccessor<Byte> MODO = SynchedEntityData.defineId(Sigrun.class, EntityDataSerializers.BYTE);
    /** Si tiene la espada clavada en la nieve (guardando el paso) y si se esta deshaciendo en nieve (al acabar su final). */
    private static final EntityDataAccessor<Boolean> CLAVADA = SynchedEntityData.defineId(Sigrun.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FUNDE = SynchedEntityData.defineId(Sigrun.class, EntityDataSerializers.BOOLEAN);

    /** En el cliente: si a quien juega ya no le toca verla (acabo su duelo). Lo pone {@code client.mision.ClientMisiones}. */
    public static volatile java.util.function.Predicate<Sigrun> OCULTA = s -> false;

    /** Lo que deja pasar el escudo de lo que le llega de frente sin estar atacando (un martillo, la mitad; las flechas, nada). */
    public static final float ESCUDO = 0.25f, ESCUDO_MARTILLO = 0.5f;
    /** Lo que para con el escudo de lo que le llega de frente sin estar atacando (lo demas le entra entero). */
    public static final float CUBRE = 0.7f;
    /** Por debajo de esta parte de su vida se rinde (no muere). */
    public static final float RINDE = 0.08f;
    /** Lo que esta tendida tras caer, lo que tarda en deshacerse en nieve y lo que se aleja paso arriba (bloques). */
    public static final int TENDIDA = 100, FUNDIDO = 30, LEJOS = 16;
    /** Lo que espera de rodillas sin nadie que la venciera cerca antes de volver a su puesto (ticks). */
    public static final int ESPERA_RENDIDA = 1200;

    // ---- en el servidor
    private int modoT;
    private UUID retador;
    /** Los que la vencieron (los que peleaban cuando se rindio): pueden elegir su final. */
    private final Set<UUID> vencedores = new HashSet<>();
    /** A quien ya le dijo "has caido" en este duelo. */
    private final Set<UUID> caidos = new HashSet<>();
    private boolean rendirPendiente, contestar;
    private int estocadaEspera = 50, saltoEspera, giroEspera, bloqueados, ultimoBloqueo, sinVencedores, fundeT = -1;
    private Vec3 saltoDir = Vec3.ZERO;
    /** Hacia donde se queda mirando de rodillas o tendida (el puesto no la hace girar). */
    private float giroFijo;
    /** Adonde va al marcharse y a quien saluda antes. */
    private BlockPos marchaA;
    private UUID saluda;
    /** Para las pruebas: los golpes que ha parado con el escudo y las veces que se ha rendido desde que arranco el juego. */
    public static int BLOQUEOS, RENDICIONES;

    // ---- en el cliente
    /** Desde cuando se deshace (o vuelve a aparecer) en el cliente (-1 si no). */
    public int fundeDesde = -1, apareceDesde = -1;
    private boolean fundeVisto;

    public Sigrun(EntityType<? extends Sigrun> t, Level l) {
        super(t, l, "sigrun", BossEvent.BossBarColor.WHITE);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 900.0).add(Attributes.ATTACK_DAMAGE, 12.5).add(Attributes.ARMOR, 12.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0).add(Attributes.MOVEMENT_SPEED, 0.27).add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(MODO, (byte) PAZ);
        entityData.define(CLAVADA, true);
        entityData.define(FUNDE, false);
    }

    public int modo() {
        return entityData.get(MODO);
    }

    private void modo(int m) {
        entityData.set(MODO, (byte) m);
        modoT = 0;
    }

    /** Si tiene la espada clavada en la nieve delante (guardando el paso). */
    public boolean clavada() {
        return entityData.get(CLAVADA);
    }

    /** Si se esta deshaciendo en nieve (al final de su caida o de su marcha). */
    public boolean funde() {
        return entityData.get(FUNDE);
    }

    public UUID retador() {
        return retador;
    }

    public Set<UUID> vencedores() {
        return vencedores;
    }

    /** Para las pruebas y el comando: como esta. */
    public String resumen() {
        String[] n = {"en paz", "en duelo", "rendida", "caida", "marchandose"};
        return n[Math.max(0, Math.min(4, modo()))] + (clavada() ? " (espada clavada)" : "") + (funde() ? " (deshaciendose)" : "")
                + String.format(java.util.Locale.ROOT, ", vida %.0f/%.0f", getHealth(), getMaxHealth()) + (segunda() ? ", segunda mitad" : "")
                + (accion() != 0 ? ", " + SigrunAnims.NOMBRES[accion()] : "") + ", vencedores " + vencedores.size()
                + (hogar() != null ? ", a " + String.format(java.util.Locale.ROOT, "%.1f", Math.sqrt(hogar().distToCenterSqr(getX(), getY(), getZ()))) + " de su puesto" : "");
    }

    // ------------------------------------------------------------------ lo de los guardianes

    @Override
    public String tipo() {
        return "sigrun";
    }

    @Override
    public int musica() {
        return 49;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return "sigrun";
    }

    @Override
    protected int[] ticks() {
        return SigrunAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return SigrunAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return SigrunAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        // solo muere de verdad con /kill (o el vacio): cae como al rematarla
        return SigrunAnims.CAER;
    }

    @Override
    protected int accionRugido() {
        // al empezar el duelo desenvaina ella misma (retar); a media vida, el grito
        return segunda ? SigrunAnims.GRITO : SigrunAnims.DESENVAINAR;
    }

    @Override
    protected boolean puedeRugir() {
        return false;
    }

    @Override
    protected int accionLevantarse() {
        return SigrunAnims.LEVANTARSE;
    }

    @Override
    protected boolean empiezaPeleas() {
        return modo() == DUELO;
    }

    @Override
    protected double correa() {
        return 32.0;
    }

    @Override
    protected boolean veBarra(ServerPlayer p) {
        return !Duelo.acabado(p);
    }

    /** Con amigos: el duelo es con quien lo acepto (si se meten otros, su vida no cambia). */
    @Override
    public void paraGrupo(int jugadores) {
        super.paraGrupo(1);
    }

    @Override
    protected boolean anda() {
        return modo() == DUELO || modo() == PAZ;
    }

    /** La cabeza sigue a quien tenga cerca en paz, en duelo y de rodillas (tendida o yendose, no). */
    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.removeAllGoals(g -> g instanceof net.minecraft.world.entity.ai.goal.LookAtPlayerGoal);
        goalSelector.addGoal(8, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, Player.class, 10f, 0.04f) {
            @Override
            public boolean canUse() {
                return (modo() == PAZ || modo() == DUELO || modo() == RENDIDA) && accion() == 0 && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return (modo() == PAZ || modo() == DUELO || modo() == RENDIDA) && super.canContinueToUse();
            }
        });
    }

    // ------------------------------------------------------------------ a quien ve y a quien pega

    /** Si pelea con ella: quien acepto el duelo y quien se ha metido (le ha pegado). */
    public boolean esRival(LivingEntity e) {
        return e instanceof ServerPlayer p && (p.getUUID().equals(retador) || rivales().contains(p.getUUID())) && !Duelo.acabado(p);
    }

    @Override
    protected boolean percibe(LivingEntity p) {
        return modo() == DUELO && esRival(p) && distanceToSqr(p) < 32 * 32;
    }

    @Override
    protected boolean objetivoValido(LivingEntity e) {
        if (modo() != DUELO) return false;
        if (e instanceof Player p) return !p.isCreative() && !p.isSpectator() && esRival(p);
        return e == getTarget();
    }

    @Override
    public void setTarget(LivingEntity t) {
        // fuera del duelo no va a por nadie
        if (t != null && modo() != DUELO) return;
        super.setTarget(t);
    }

    /** El clic derecho: hablarle (en paz o rendida; en duelo no, que se alza el escudo). */
    @Override
    protected net.minecraft.world.InteractionResult mobInteract(Player p, net.minecraft.world.InteractionHand mano) {
        if (modo() != PAZ && modo() != RENDIDA) return net.minecraft.world.InteractionResult.PASS;
        if (!level().isClientSide && p instanceof ServerPlayer sp && mano == net.minecraft.world.InteractionHand.MAIN_HAND) Duelo.hablar(sp, this);
        return net.minecraft.world.InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public boolean isPickable() {
        return !(level().isClientSide && OCULTA.test(this)) && super.isPickable();
    }

    // ------------------------------------------------------------------ empezar el duelo, rendirse, caer, marcharse

    /** Acepta el duelo de ese jugador (o le pego estando en paz): arranca la espada de la nieve y la alza. */
    public void retar(ServerPlayer sp) {
        if (level().isClientSide || modo() != PAZ || !isAlive()) return;
        retador = sp.getUUID();
        caidos.clear();
        vencedores.clear();
        modo(DUELO);
        entityData.set(CLAVADA, false);
        cortarAccion();
        girarHacia(sp, 180f);
        setTarget(sp);
        empezar(SigrunAnims.DESENVAINAR);
        pausa = Math.max(pausa, SigrunAnims.TICKS[SigrunAnims.DESENVAINAR] + 6);
        estocadaEspera = 40;
        level().playSound(null, getX(), getY() + 1, getZ(), DarkCraftMod.SFX_DUELO_EMPIEZA.get(), SoundSource.HOSTILE, 2.0f, 1.0f);
        DarkCraftMod.LOGGER.info("[DarkCraft] Sigrun acepta el duelo de {}", sp.getGameProfile().getName());
    }

    /** Se rinde de rodillas (no muere): quien peleaba con ella la ha vencido. */
    private void rendirse(ServerPlayer quien) {
        rendirPendiente = false;
        if (modo() != DUELO || !(level() instanceof ServerLevel sl)) return;
        RENDICIONES++;
        vencedores.clear();
        java.util.List<ServerPlayer> quienes = new java.util.ArrayList<>();
        for (ServerPlayer p : sl.players())
            if ((esRival(p) || p == quien) && p.distanceToSqr(this) < 64 * 64 && !Duelo.acabado(p)) quienes.add(p);
        if (quien != null && !Duelo.acabado(quien) && !quienes.contains(quien)) quienes.add(quien);
        for (ServerPlayer p : quienes) vencedores.add(p.getUUID());
        modo(RENDIDA);
        cortarAccion();
        super.setTarget(null);
        getNavigation().stop();
        rendirPelea();
        if (quien != null) girarHacia(quien, 180f);
        giroFijo = getYRot();
        empezar(SigrunAnims.RENDIRSE);
        sinVencedores = 0;
        sl.playSound(null, getX(), getY() + 1, getZ(), DarkCraftMod.SFX_SIGRUN_RINDE.get(), SoundSource.HOSTILE, 1.6f, 1.0f);
        Duelo.vencida(this, sl, quienes);
        DarkCraftMod.LOGGER.info("[DarkCraft] Sigrun se rinde ({} la vencieron)", vencedores.size());
    }

    /** De rodillas otra vez para quien ya la vencio y vuelve a hablarle (estaba en paz en su puesto). */
    public void arrodillar(ServerPlayer sp) {
        if (level().isClientSide || modo() != PAZ) return;
        vencedores.add(sp.getUUID());
        modo(RENDIDA);
        cortarAccion();
        getNavigation().stop();
        girarHacia(sp, 180f);
        giroFijo = getYRot();
        empezar(SigrunAnims.RENDIRSE);
        sinVencedores = 0;
    }

    /** La rematan: se le escurre la espada, queda tendida en la nieve y se deshace. */
    public void caer() {
        if (level().isClientSide || !isAlive()) return;
        if (modo() != RENDIDA) giroFijo = getYRot();
        modo(CAIDA);
        cortarAccion();
        getNavigation().stop();
        empezar(SigrunAnims.CAER);
        level().playSound(null, getX(), getY() + 1, getZ(), DarkCraftMod.SFX_SIGRUN_MUERE.get(), SoundSource.HOSTILE, 1.8f, 1.0f);
        DarkCraftMod.LOGGER.info("[DarkCraft] Sigrun cae: el paso resistio");
    }

    /** La dejan marchar: se levanta, saluda a quien se lo dijo y se va paso arriba (sin su escudo) hasta perderse en la nieve. */
    public void marchar(ServerPlayer quien) {
        if (level().isClientSide || !isAlive()) return;
        modo(MARCHA);
        saluda = quien == null ? null : quien.getUUID();
        cortarAccion();
        getNavigation().stop();
        empezar(SigrunAnims.LEVANTARSE);
        // paso arriba: hacia donde da la espalda en su puesto
        BlockPos h = hogar() != null ? hogar() : blockPosition();
        Vec3 atras = Vec3.directionFromRotation(0, Duelo.GIRO + 180f);
        marchaA = BlockPos.containing(h.getX() + 0.5 + atras.x * (LEJOS + 8), h.getY(), h.getZ() + 0.5 + atras.z * (LEJOS + 8));
        DarkCraftMod.LOGGER.info("[DarkCraft] Sigrun se va al monasterio a buscar los nombres de sus hermanos");
    }

    /** Vuelve a su puesto (para los demas: cada uno su duelo y su final): de pie, la espada clavada y la vida entera. */
    public void volverAlPuesto() {
        if (level().isClientSide) return;
        BlockPos h = hogar();
        if (h != null) moveTo(h.getX() + 0.5, h.getY(), h.getZ() + 0.5, Duelo.GIRO, 0f);
        setYRot(Duelo.GIRO);
        yBodyRot = yHeadRot = yRotO = yBodyRotO = yHeadRotO = Duelo.GIRO;
        if (modo() == DUELO) rendirPelea();
        modo(PAZ);
        cortarAccion();
        super.setTarget(null);
        getNavigation().stop();
        entityData.set(CLAVADA, true);
        entityData.set(FUNDE, false);
        fundeT = -1;
        retador = null;
        saluda = null;
        vencedores.clear();
        caidos.clear();
        rivales().clear();
        segunda = false;
        setHealth(getMaxHealth());
        if (level() instanceof ServerLevel sl)
            sl.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 1.1, getZ(), 30, 0.5, 0.9, 0.5, 0.02);
    }

    /** Lo que hace cuando su duelo se acaba sin que se rinda (quien la reto cayo o se fue): vuelve a su puesto a clavar la espada. */
    @Override
    protected void alAcabarPelea() {
        if (modo() == DUELO) {
            modo(PAZ);
            retador = null;
            caidos.clear();
        }
    }

    @Override
    protected void alSegundaMitad() {
        saltoEspera = 30;
        giroEspera = 50;
        if (level() instanceof ServerLevel sl) Duelo.decir(this, sl, 11, 48);
    }

    // ------------------------------------------------------------------ como pelea

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (modo() != DUELO || Math.abs(presa.getY() - getY()) > 3.0) return 0;
        // le han pegado en el escudo dos veces seguidas: contesta
        if (contestar && d < 3.4) {
            contestar = false;
            return SigrunAnims.ESCUDAZO;
        }
        if (segunda && saltoEspera <= 0 && d > 4.0 && d < 10.0 && random.nextFloat() < 0.5f) {
            saltoEspera = 160 + random.nextInt(80);
            return SigrunAnims.SALTO;
        }
        if (segunda && giroEspera <= 0 && d < 3.6 && random.nextFloat() < 0.35f) {
            giroEspera = 120 + random.nextInt(60);
            return SigrunAnims.GIRO;
        }
        if (d > 4.6 && d < 8.6 && estocadaEspera <= 0 && random.nextFloat() < 0.6f) {
            estocadaEspera = 100 + random.nextInt(60);
            return SigrunAnims.ESTOCADA;
        }
        if (d > 3.8) return 0;
        if (d < 2.4 && random.nextFloat() < 0.25f) return SigrunAnims.ESCUDAZO;
        return random.nextFloat() < 0.45f ? SigrunAnims.COMBO : SigrunAnims.TAJO;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case SigrunAnims.TAJO -> new Golpe(3.6f, 70f, 1.5f, 0.8f);
            case SigrunAnims.COMBO -> n < 2 ? new Golpe(3.4f, 140f, 0.8f, 0.5f) : new Golpe(4.0f, 40f, 1.2f, 1.0f);
            case SigrunAnims.ESCUDAZO -> new Golpe(2.8f, 100f, 0.6f, 2.0f);
            case SigrunAnims.ESTOCADA -> new Golpe(4.2f, 35f, 1.4f, 1.2f);
            case SigrunAnims.GIRO -> new Golpe(3.8f, 360f, 1.1f, 1.0f);
            case SigrunAnims.SALTO -> new Golpe(3.2f, 360f, 1.6f, 1.2f);
            default -> null;
        };
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (accion == SigrunAnims.ESCUDAZO) le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2));
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = SigrunAnims.GOLPES[accion];
        switch (accion) {
            case SigrunAnims.DESENVAINAR -> {
                // arranca la espada de la nieve (y suena el acero) y la alza al cielo
                if (t == 12) {
                    nieve(1.2, 10);
                    playSound(SoundEvents.SNOW_BREAK, 1.2f, 0.7f);
                }
                if (t == 17) playSound(DarkCraftMod.SFX_GOLPE_LIGERO.get(), 0.9f, 1.3f);
            }
            case SigrunAnims.TAJO -> {
                if (t == g[0] - 6) esfuerzo();
                if (t == g[0] - 2 || t == g[0] - 1) empujar(0.3);
            }
            case SigrunAnims.COMBO -> {
                if (t == g[0] - 3) esfuerzo();
                if (t == g[0] - 1 || t == g[1] - 1) empujar(0.28);
                if (t == g[2] - 2 || t == g[2] - 1) empujar(0.5);
                if (t == g[2] - 4) esfuerzo();
            }
            case SigrunAnims.ESCUDAZO -> {
                if (t == g[0] - 1) empujar(0.85);
                if (t == g[0]) playSound(DarkCraftMod.SFX_ESCUDO_GOLPE.get(), 1.3f, 0.8f);
            }
            case SigrunAnims.ESTOCADA -> {
                if (t == g[0] - 8) esfuerzo();
                if (t >= g[0] - 3 && t <= g[0]) empujar(0.78);
            }
            case SigrunAnims.GIRO -> {
                if (t == g[0] - 5) esfuerzo();
                if (t >= g[0] - 3 && t <= g[0] + 1) empujar(0.18);
            }
            case SigrunAnims.SALTO -> {
                if (t == 8) esfuerzo();
                if (t == 13) {
                    // salta hacia donde esta su presa (hasta nueve bloques) y cae con la espada en la nieve en el tick del golpe
                    LivingEntity p = getTarget();
                    saltoDir = Vec3.directionFromRotation(0, getYRot());
                    double lejos = p == null ? 4.0 : Math.min(9.0, Math.max(2.0, Math.sqrt(distanceToSqr(p)) - 1.5));
                    double v = lejos / 7.6;
                    setDeltaMovement(saltoDir.x * v, 0.46, saltoDir.z * v);
                    hasImpulse = true;
                } else if (t > 13 && t < g[0] && !onGround()) {
                    // el paso de cada tick de la accion frena al que ataca: en el aire no (y no se tuerce: va adonde salto)
                    double h = Math.sqrt(getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().z * getDeltaMovement().z) / 0.6;
                    setDeltaMovement(saltoDir.x * h, getDeltaMovement().y, saltoDir.z * h);
                } else if (t == g[0]) {
                    playSound(DarkCraftMod.SFX_SIGRUN_SALTO.get(), 1.6f, 1.0f);
                    nieve(1.6, 40);
                }
            }
            case SigrunAnims.GRITO -> {
                // golpea dos veces el escudo con la espada
                if (t == 10 || t == 18) playSound(DarkCraftMod.SFX_ESCUDO_GOLPE.get(), 1.6f, t == 10 ? 0.85f : 0.75f);
            }
            case SigrunAnims.RENDIRSE -> {
                if (t == 20) nieve(0.6, 12);
            }
            case SigrunAnims.CAER -> {
                if (t == 36) {
                    nieve(1.0, 24);
                    playSound(SoundEvents.SNOW_FALL, 1.2f, 0.7f);
                }
            }
            case SigrunAnims.PLANTAR -> {
                if (t == 12) {
                    nieve(0.9, 12);
                    playSound(SoundEvents.SNOW_BREAK, 1.3f, 0.6f);
                    playSound(DarkCraftMod.SFX_GOLPE_LIGERO.get(), 0.5f, 0.6f);
                }
                if (t == 13) entityData.set(CLAVADA, true);
            }
            default -> {
            }
        }
    }

    /** Nieve que salta delante de ella (a {@code delante} bloques). */
    private void nieve(double delante, int n) {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        double x = getX() + d.x * delante, z = getZ() + d.z * delante;
        BlockState suelo = level().getBlockState(BlockPos.containing(x, getY() - 0.5, z));
        if (suelo.isAir()) suelo = Blocks.SNOW_BLOCK.defaultBlockState();
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, suelo), x, getY() + 0.1, z, n, 0.4, 0.05, 0.4, 0.15);
        sl.sendParticles(ParticleTypes.SNOWFLAKE, x, getY() + 0.3, z, n / 2, 0.5, 0.2, 0.5, 0.03);
    }

    private void esfuerzo() {
        if (random.nextFloat() < 0.7f) playSound(DarkCraftMod.SFX_SIGRUN_ESFUERZO.get(), 1.0f, 0.95f + random.nextFloat() * 0.1f);
    }

    // ------------------------------------------------------------------ el escudo y el dano

    /** Si el escudo le tapa ese golpe: de frente (70 grados a cada lado), en duelo, sin estar atacando (o aguantando otro). */
    private boolean cubre(DamageSource s) {
        if (modo() != DUELO || postura() != DE_PIE || aturdido()) return false;
        int a = accion();
        if (a != 0 && a != SigrunAnims.BLOQUEA) return false;
        if (s.is(DamageTypeTags.BYPASSES_ARMOR) || s.is(DamageTypeTags.IS_FIRE) || s.is(DamageTypeTags.IS_EXPLOSION)) return false;
        Vec3 de = s.getSourcePosition();
        if (de == null) return false;
        Vec3 mira = Vec3.directionFromRotation(0, yBodyRot);
        double dx = de.x - getX(), dz = de.z - getZ(), l = Math.sqrt(dx * dx + dz * dz);
        return l > 0.01 && (dx * mira.x + dz * mira.z) / l > 0.34;
    }

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        if (level().isClientSide) return super.hurt(s, cuanto);
        // lo que mata de verdad (/kill, el vacio)
        if (s.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return super.hurt(s, cuanto);
        ServerPlayer sp = s.getEntity() instanceof ServerPlayer p ? p : null;
        // a quien ya acabo su duelo no le toca: ni la ve ni le pega
        if (sp != null && Duelo.acabado(sp)) return false;
        switch (modo()) {
            case PAZ -> {
                // pegarle estando en paz es aceptar el duelo (quien ya la vencio, no: tiene que hablarle)
                if (sp != null && !sp.isSpectator() && Duelo.puedeRetar(sp) && accion() == 0) Duelo.aceptar(sp, this);
                return false;
            }
            case DUELO -> {
                if (cubre(s) && random.nextFloat() < CUBRE) {
                    boolean flecha = s.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile;
                    level().playSound(null, getX(), getY() + 1.4, getZ(), DarkCraftMod.SFX_ESCUDO_GOLPE.get(), SoundSource.HOSTILE, 1.1f,
                            0.95f + random.nextFloat() * 0.15f);
                    BLOQUEOS++;
                    if (accion() == 0) empezar(SigrunAnims.BLOQUEA);
                    bloqueados = tickCount - ultimoBloqueo < 50 ? bloqueados + 1 : 1;
                    ultimoBloqueo = tickCount;
                    if (bloqueados >= 2) {
                        bloqueados = 0;
                        contestar = true;
                        pausa = Math.min(pausa, 4);
                    }
                    if (flecha) return false;
                    cuanto *= com.darkcraft.armas.Martillo.rompe(s) ? ESCUDO_MARTILLO : ESCUDO;
                }
                boolean ok = super.hurt(s, cuanto);
                if (rendirPendiente) rendirse(sp);
                return ok;
            }
            default -> {
                // rendida, caida o marchandose: ya no se le hace nada
                return false;
            }
        }
    }

    /** En duelo no muere: lo que la dejaria por debajo de {@link #RINDE} la deja ahi y se rinde. */
    @Override
    public void setHealth(float v) {
        if (!level().isClientSide && modo() == DUELO && isAlive() && v < getHealth() && v < getMaxHealth() * RINDE) {
            super.setHealth(Math.max(1f, getMaxHealth() * RINDE * 0.6f));
            rendirPendiente = true;
            return;
        }
        super.setHealth(v);
    }

    // ------------------------------------------------------------------ cada tick

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            cliente();
            return;
        }
        if (!isAlive() || !(level() instanceof ServerLevel sl)) return;
        modoT++;
        if (estocadaEspera > 0) estocadaEspera--;
        if (saltoEspera > 0) saltoEspera--;
        if (giroEspera > 0) giroEspera--;
        if (rendirPendiente) rendirse(null);
        switch (modo()) {
            case PAZ -> paz();
            case DUELO -> duelo(sl);
            case RENDIDA -> rendida(sl);
            case CAIDA -> caida(sl);
            case MARCHA -> marcha(sl);
            default -> {
            }
        }
        // de rodillas o tendida no gira (en su puesto, el de los enemigos la volveria hacia donde mira)
        if ((modo() == RENDIDA || modo() == CAIDA) && isAlive()) {
            setYRot(giroFijo);
            yBodyRot = giroFijo;
            if (modo() == CAIDA) yHeadRot = giroFijo;
            setDeltaMovement(0, getDeltaMovement().y, 0);
        }
    }

    /** En paz: si volvio a su puesto con la espada colgando, la clava en la nieve. */
    private void paz() {
        if (clavada() || accion() != 0 || hogar() == null) return;
        if (hogar().distToCenterSqr(getX(), getY(), getZ()) < 1.6 * 1.6 && getNavigation().isDone()) {
            setYRot(Duelo.GIRO);
            yBodyRot = yHeadRot = Duelo.GIRO;
            empezar(SigrunAnims.PLANTAR);
        }
    }

    /** En duelo: a quien cae le dice que no hay verguenza en eso (una vez cada uno). */
    private void duelo(ServerLevel sl) {
        for (ServerPlayer p : sl.players()) {
            if (!p.isDeadOrDying() || caidos.contains(p.getUUID()) || !esRival(p) || p.distanceToSqr(this) > 48 * 48) continue;
            caidos.add(p.getUUID());
            Duelo.caido(p);
            Duelo.decir(this, sl, 10, 48);
        }
    }

    /** Rendida, de rodillas: espera a que le hable quien la vencio; sin ninguno cerca un rato, se levanta y vuelve a su puesto. */
    private void rendida(ServerLevel sl) {
        boolean alguno = false;
        for (ServerPlayer p : sl.players())
            if (vencedores.contains(p.getUUID()) && !Duelo.acabado(p) && p.distanceToSqr(this) < 48 * 48) alguno = true;
        sinVencedores = alguno ? 0 : sinVencedores + 1;
        if (sinVencedores > ESPERA_RENDIDA && accion() == 0) {
            empezar(SigrunAnims.LEVANTARSE);
            modo(PAZ);
            entityData.set(CLAVADA, false);
            vencedores.clear();
            setHealth(getMaxHealth());
        }
    }

    /** Caida: tendida en la nieve un rato y se deshace en nieve; despues vuelve a su puesto (para los demas). */
    private void caida(ServerLevel sl) {
        if (modoT == SigrunAnims.TICKS[SigrunAnims.CAER] + TENDIDA) {
            entityData.set(FUNDE, true);
            fundeT = 0;
        }
        if (fundeT >= 0) {
            fundeT++;
            if (fundeT % 3 == 0) sl.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 0.3, getZ(), 6, 0.6, 0.2, 0.9, 0.02);
            if (fundeT == FUNDIDO / 2) sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.3, getZ(), 12, 0.5, 0.15, 0.8, 0.01);
            if (fundeT >= FUNDIDO) volverAlPuesto();
        }
    }

    /** Marchandose: se levanta, saluda y se va paso arriba; lejos de su puesto (o pasado un rato), se pierde en la nieve. */
    private void marcha(ServerLevel sl) {
        if (fundeT >= 0) {
            fundeT++;
            getNavigation().stop();
            if (fundeT % 2 == 0) sl.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 1.0, getZ(), 8, 0.5, 0.9, 0.5, 0.04);
            if (fundeT >= FUNDIDO) volverAlPuesto();
            return;
        }
        int levanta = SigrunAnims.TICKS[SigrunAnims.LEVANTARSE], saludo = SigrunAnims.TICKS[SigrunAnims.SALUDO];
        if (modoT == levanta + 1 && accion() == 0) {
            ServerPlayer p = saluda == null ? null : sl.getServer().getPlayerList().getPlayer(saluda);
            if (p != null) girarHacia(p, 180f);
            empezar(SigrunAnims.SALUDO);
        }
        if (modoT == levanta + saludo + 4) sl.playSound(null, getX(), getY() + 1, getZ(), DarkCraftMod.SFX_SIGRUN_SE_VA.get(), SoundSource.NEUTRAL, 1.4f, 1.0f);
        if (modoT > levanta + saludo + 2 && accion() == 0 && marchaA != null) {
            if (modoT % 20 == 0 || getNavigation().isDone())
                getNavigation().moveTo(marchaA.getX() + 0.5, marchaA.getY(), marchaA.getZ() + 0.5, 0.55);
            BlockPos h = hogar() != null ? hogar() : marchaA;
            double lejos = Math.sqrt(h.distToCenterSqr(getX(), getY(), getZ()));
            if (lejos > LEJOS || modoT > levanta + saludo + 260) {
                entityData.set(FUNDE, true);
                fundeT = 0;
            }
        }
    }

    // ------------------------------------------------------------------ en el cliente

    private void cliente() {
        boolean f = funde();
        if (f && !fundeVisto) fundeDesde = tickCount;
        if (!f && fundeVisto) apareceDesde = tickCount;
        fundeVisto = f;
        // nieve que cae a su alrededor mientras se va
        if (f && random.nextInt(2) == 0)
            level().addParticle(ParticleTypes.SNOWFLAKE, getX() + (random.nextDouble() - 0.5), getY() + random.nextDouble() * 2.0,
                    getZ() + (random.nextDouble() - 0.5), 0, 0.03, 0);
    }

    /** Lo opaca que se ve ahora (se deshace en nieve al final de su caida o de su marcha, y aparece en su puesto). */
    public float alfa(float pt) {
        if (funde() && fundeDesde >= 0) return net.minecraft.util.Mth.clamp(1f - (tickCount + pt - fundeDesde) / FUNDIDO, 0f, 1f);
        if (apareceDesde >= 0) return net.minecraft.util.Mth.clamp((tickCount + pt - apareceDesde) / 20f, 0f, 1f);
        return 1f;
    }

    // ------------------------------------------------------------------ lo demas

    @Override
    protected int pausaTras(int accion) {
        return switch (accion) {
            case SigrunAnims.DESENVAINAR -> 6;
            case SigrunAnims.GRITO -> 4;
            case SigrunAnims.BLOQUEA -> 2;
            case SigrunAnims.SALTO -> 24 + random.nextInt(12);
            case SigrunAnims.ATURDIDO -> 4;
            default -> (segunda ? 8 : 14) + random.nextInt(12);
        };
    }

    @Override
    protected double distanciaDeseada() {
        return 2.6;
    }

    @Override
    protected double alturaMaxima() {
        return 3.0;
    }

    @Override
    protected double prisa() {
        return 1.15;
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return switch (accion) {
            case SigrunAnims.TAJO, SigrunAnims.SALTO -> DarkCraftMod.SFX_GOLPE_PESADO.get();
            case SigrunAnims.COMBO, SigrunAnims.ESTOCADA, SigrunAnims.GIRO -> DarkCraftMod.SFX_GOLPE_LIGERO.get();
            default -> null;
        };
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        super.playStepSound(p, s);
        playSound(DarkCraftMod.SFX_ARMADURA_PASO.get(), 0.35f, 0.95f + random.nextFloat() * 0.1f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return modo() == DUELO ? DarkCraftMod.SFX_SIGRUN_DOLOR.get() : null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_SIGRUN_MUERE.get();
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return modo() == DUELO;
    }

    /** No se guarda nada de su duelo: al volver a cargarse esta en su puesto, en paz y con la espada clavada. */
    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        BlockPos s = Duelo.sitio();
        // (asignar la gira hacia donde mira en su puesto: se le devuelve el giro con que se guardo)
        float giro = getYRot();
        asignar("sigrun", -1, s, Duelo.GIRO, DE_PIE, 0);
        setYRot(giro);
        yBodyRot = yHeadRot = giro;
        entityData.set(MODO, (byte) PAZ);
        // en su puesto, con la espada clavada; si se guardo lejos (en su duelo), vuelve andando y la clava
        entityData.set(CLAVADA, s.distToCenterSqr(getX(), getY(), getZ()) < 2.0 * 2.0);
        entityData.set(FUNDE, false);
    }
}
