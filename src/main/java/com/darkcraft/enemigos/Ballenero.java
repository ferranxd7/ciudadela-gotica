package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.Costillar;
import com.darkcraft.net.Net;
import com.darkcraft.rey.Voces;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Brann, el Ballenero (parte 7r): el jefe de la Costa de los Huesos. El viejo arponero del Puerto Varado: la noche del cielo rojo
 * el mar se retiro de golpe y el se quedo en tierra, en el Costillar de la Ballena, arponeando a la Vieja, una ballena que hace
 * mucho que no esta (libro 26: "el viejo Brann sigue arponeando una ballena que hace mucho que no esta"; libro 27, su
 * bitacora). Cuarenta anos detras de ella. Un gigante viejo comido por la sal, con su pata de palo de hueso de ballena, el arpon
 * en la derecha y el farol de aceite en la izquierda.
 * <ul>
 * <li>espera <b>de rodilla</b> en medio del costillar mirando al mar que no esta; al ver a alguien dentro de las costillas se
 * levanta mientras dice su frase y al acabarla grita ("¡Alli resopla!");</li>
 * <li>el <b>arponazo</b>: lo echa atras y lo clava a fondo (llega lejos, se tira hacia delante);</li>
 * <li>el <b>tajo doble</b>: barre con la punta de un lado a otro y de vuelta;</li>
 * <li>el <b>arpon</b>: lo lanza atado a su cabo ({@link ArponBallenero}); a quien pilla se le clava y Brann <b>tira</b>: lo
 * arrastra hacia el. Si falla, lo recoge;</li>
 * <li>el <b>farol</b>: lo lanza en arco; revienta y el aceite arde en el suelo ({@link FarolAceite});</li>
 * <li>a <b>media vida</b> apunta al horizonte y alza el farol ("¡Alli resopla!"): sale <b>la Vieja</b> ({@link Vieja}) y
 * embiste de un lado a otro del costillar por donde esta su presa (antes se ve su chorro y la espuma por donde va a pasar);
 * desde entonces vuelve a llamarla cada tanto, y <b>salta</b>: cae clavando el arpon y la onda tumba a quien este de pie
 * cerca.</li>
 * </ul>
 * Habla (ElevenLabs, en ingles con subtitulos; {@link Voces#BASE_BALLENERO}): 1 al empezar (no la repite en 4 min), 2 al llamar
 * a la Vieja la primera vez, 3 si cae su presa, 4 la primera vez que se le clava el arpon a alguien ("cuarenta anos detras de
 * ella... ahora no se me escapa") y 5 al caer ("la marea... oigo la marea que vuelve"). Al caer se queda de rodillas agarrado
 * al arpon, la Vieja sube al cielo y el se deshace en sal; lo demas (el cartel con sus ecos, la hazana {@code ballenero} y el
 * bestiario) lo reparte {@code valdrenn/Guardianes}; los salobres que seguian faenando descansan ({@code morvain/Costillar}).
 */
public class Ballenero extends Guardian {
    public static final int ECOS = 15000;
    public static final int VOZ_BASE = Voces.BASE_BALLENERO;
    /** Si el arpon esta fuera (lanzado): el modelo no lo dibuja en la mano. */
    private static final EntityDataAccessor<Boolean> ARPON_FUERA = SynchedEntityData.defineId(Ballenero.class, EntityDataSerializers.BOOLEAN);
    /** Lo que arrastra el tiron (a mas lejos, mas). */
    public static final double TIRON = 1.05;
    /** La onda del salto: hasta donde llega (a quien este de pie). */
    public static final double R_ONDA = 5.2;
    /** Cada cuanto vuelve a llamar a la Vieja desde media vida (ticks). */
    public static final int VIEJA_CADA = 300;

    public static final DustParticleOptions SAL = new DustParticleOptions(new Vector3f(0.94f, 0.96f, 0.95f), 1.3f),
            ACEITE = new DustParticleOptions(new Vector3f(0.95f, 0.78f, 0.32f), 1.0f);

    /** La ultima vez que dijo su frase de entrada (tiempo del mundo): no la repite en cada intento. */
    private static long ultimaEntrada = Long.MIN_VALUE / 2;

    private int arponEspera = 60, farolEspera = 110, saltoEspera = 60, viejaEspera = -1, hablaHasta, esperaRugido, espera = 60, repetido;
    private boolean llamo, cuatro;
    private LivingEntity ultimaPresa;
    private ArponBallenero arpon;
    private LivingEntity enganche;
    /** Para las pruebas. */
    private int arponazos, tajos, lanzados, tirones, arrastrados, faroles, saltos, ondasQueDan, llamadas;
    private double arrastre;
    /** Para las pruebas: no elige golpes ni grita por su cuenta (lo que se le manda hacer, si; y lo ya lanzado sigue). */
    public boolean quieta;

    public Ballenero(EntityType<? extends Ballenero> t, Level l) {
        super(t, l, "ballenero", BossEvent.BossBarColor.BLUE);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1000.0).add(Attributes.ATTACK_DAMAGE, 12.5).add(Attributes.ARMOR, 6.0)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0).add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ARPON_FUERA, false);
    }

    /** Si el arpon esta fuera (lo sabe tambien el cliente). */
    public boolean arponFuera() {
        return entityData.get(ARPON_FUERA);
    }

    @Override
    public String tipo() {
        return "ballenero";
    }

    @Override
    public int musica() {
        return 39;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return "ballenero";
    }

    @Override
    protected int[] ticks() {
        return BalleneroAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return BalleneroAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return BalleneroAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return BalleneroAnims.MORIR;
    }

    @Override
    protected int accionRugido() {
        return BalleneroAnims.GRITO;
    }

    @Override
    protected int accionLevantarse() {
        return BalleneroAnims.LEVANTARSE;
    }

    // ------------------------------------------------------------------ para las pruebas

    public int arponazos() {
        return arponazos;
    }

    public int tajos() {
        return tajos;
    }

    /** Los arpones lanzados, los tirones y a cuantos ha arrastrado (y lo mas que ha arrastrado). */
    public int lanzados() {
        return lanzados;
    }

    public int tirones() {
        return tirones;
    }

    public int arrastrados() {
        return arrastrados;
    }

    public double arrastre() {
        return arrastre;
    }

    public int faroles() {
        return faroles;
    }

    public int saltos() {
        return saltos;
    }

    public int ondasQueDan() {
        return ondasQueDan;
    }

    public int llamadas() {
        return llamadas;
    }

    /** El arpon que esta fuera (pruebas), o null. */
    public ArponBallenero arpon() {
        return arpon != null && !arpon.isRemoved() ? arpon : null;
    }

    /** Para las pruebas: pasa ya a media vida (llama a la Vieja), sin esperar a que acabe lo que hace. */
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

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (quieta || Math.abs(presa.getY() - getY()) > 5.0) return 0;
        // sin el arpon en la mano (lo esta recogiendo) solo espera
        if (arponFuera()) return 0;
        // a media vida, a media distancia, salta y cae encima
        if (segunda && saltoEspera <= 0 && d >= 3.5 && d <= 10 && random.nextFloat() < 0.35f) {
            saltoEspera = 130 + random.nextInt(60);
            return BalleneroAnims.SALTO;
        }
        // de cerca: el tajo doble o el arponazo
        if (d < 4.3) {
            if (++repetido > 3 && farolEspera <= 0) {
                repetido = 0;
                farolEspera = 90 + random.nextInt(40);
                return BalleneroAnims.FAROL;
            }
            return random.nextFloat() < 0.55f ? BalleneroAnims.TAJO : BalleneroAnims.ARPONAZO;
        }
        repetido = 0;
        // a media distancia: el arponazo a fondo (se tira hacia delante)
        if (d < 6.0 && random.nextFloat() < 0.5f) return BalleneroAnims.ARPONAZO;
        // de lejos: el arpon con su cabo (a quien ve) o el farol
        if (arponEspera <= 0 && d >= 5.0 && d < 22 && hasLineOfSight(presa) && random.nextFloat() < 0.6f) {
            arponEspera = (segunda ? 90 : 130) + random.nextInt(40);
            return BalleneroAnims.ARPON;
        }
        if (farolEspera <= 0 && d >= 4.5 && d < 16 && random.nextFloat() < 0.5f) {
            farolEspera = (segunda ? 120 : 170) + random.nextInt(40);
            return BalleneroAnims.FAROL;
        }
        return 0;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case BalleneroAnims.ARPONAZO -> new Golpe(4.9f, 36f, 1.15f, 1.0f);
            case BalleneroAnims.TAJO -> new Golpe(4.2f, 150f, n == 0 ? 0.85f : 0.95f, n == 0 ? 0.6f : 0.9f);
            case BalleneroAnims.SALTO -> new Golpe(2.6f, 200f, 1.3f, 1.2f);
            default -> null;
        };
    }

    /** El arponazo deja sangrando (frena un poco). */
    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (accion == BalleneroAnims.ARPONAZO) le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0), this);
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = BalleneroAnims.GOLPES[accion];
        switch (accion) {
            case BalleneroAnims.ARPONAZO -> {
                if (t == g[0] - 5) playSound(DarkCraftMod.SFX_BALLENERO_ARPONAZO.get(), 2.0f, 0.95f + random.nextFloat() * 0.1f);
                if (t == g[0] - 1) empujar(0.55);
                if (t == g[0]) {
                    arponazos++;
                    if (level() instanceof ServerLevel sl) {
                        Vec3 d = Vec3.directionFromRotation(0, getYRot());
                        for (int k = 2; k <= 5; k++) sl.sendParticles(ParticleTypes.CRIT, getX() + d.x * k, getY() + 1.9, getZ() + d.z * k, 3, 0.1, 0.1, 0.1, 0.05);
                    }
                }
            }
            case BalleneroAnims.TAJO -> {
                for (int k = 0; k < g.length; k++) {
                    if (t == g[k] - 4) playSound(DarkCraftMod.SFX_BALLENERO_CUCHILLO.get(), 1.8f, 0.9f + random.nextFloat() * 0.2f);
                    if (t == g[k] - 1) empujar(0.28);
                    if (t == g[k]) {
                        tajos++;
                        if (level() instanceof ServerLevel sl) {
                            for (int j = 0; j <= 10; j++) {
                                double a = Math.toRadians(getYRot() + 90 - 75 + 150 * j / 10.0);
                                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, getX() + Math.cos(a) * 3.2, getY() + 1.7, getZ() + Math.sin(a) * 3.2, 1, 0, 0, 0, 0);
                            }
                        }
                    }
                }
            }
            case BalleneroAnims.ARPON -> {
                if (t == 8) playSound(SoundEvents.TRIDENT_RIPTIDE_1, 1.2f, 0.8f);
                // (un tick antes de soltarlo ya no se ve en la mano: el giro de la muneca al soltar)
                if (t == g[0] - 1) entityData.set(ARPON_FUERA, true);
                if (t == g[0]) lanzar();
            }
            case BalleneroAnims.TIRON -> {
                if (t == 2) playSound(DarkCraftMod.SFX_BALLENERO_TIRON.get(), 2.0f, 1.0f);
                if (t == g[0]) tironazo();
                if (t >= BalleneroAnims.TICKS[BalleneroAnims.TIRON] - 1) devolver();
            }
            case BalleneroAnims.FAROL -> {
                if (t == g[0] - 6) playSound(SoundEvents.LANTERN_HIT, 1.2f, 0.8f);
                if (t == g[0]) lanzarFarol();
            }
            case BalleneroAnims.GRITO -> {
                if (t == 6) {
                    playSound(DarkCraftMod.SFX_BALLENERO_GRITO.get(), 3.0f, 1.0f);
                    if (level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 2.6, getZ(), 60, 1.2, 1.0, 1.2, 0.4);
                        sl.sendParticles(SAL, getX(), getY() + 2.4, getZ(), 40, 1.2, 1.2, 1.2, 0.0);
                    }
                }
            }
            case BalleneroAnims.LLAMAR -> {
                if (t == 4) {
                    if (!llamo) {
                        llamo = true;
                        hablaHasta = 0;
                        habla(2);
                    }
                    playSound(DarkCraftMod.SFX_BALLENERO_GRITO.get(), 2.4f, 0.9f);
                }
                if (t > 10 && t < g[0] && t % 3 == 0 && level() instanceof ServerLevel sl)
                    sl.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 0.3, getZ(), 20, 1.4, 0.1, 1.4, 0.3);
                if (t == g[0]) llamarVieja();
            }
            case BalleneroAnims.SALTO -> {
                if (t == 9) {
                    LivingEntity p = getTarget();
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    double lejos = p == null ? 4.0 : Mth.clamp(Math.sqrt(distanceToSqr(p)) - 1.6, 2.0, 8.5);
                    double v = 0.12 + lejos * 0.1;
                    setDeltaMovement(d.x * v, 0.62, d.z * v);
                    hasImpulse = true;
                    playSound(DarkCraftMod.SFX_BALLENERO_ARPON.get(), 1.6f, 0.75f);
                    saltos++;
                } else if (t > 9 && t < g[0] && !onGround()) {
                    // el paso de cada tick de la accion frena al que ataca: en el aire no
                    Vec3 d = Vec3.directionFromRotation(0, getYRot());
                    double h = Math.sqrt(getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().z * getDeltaMovement().z) / 0.6;
                    setDeltaMovement(d.x * h, getDeltaMovement().y, d.z * h);
                }
                if (t == g[0]) onda();
            }
            case BalleneroAnims.LEVANTARSE -> {
                if (t == 3 && level() instanceof ServerLevel sl) {
                    sl.sendParticles(SAL, getX(), getY() + 1.2, getZ(), 30, 0.8, 0.8, 0.8, 0.0);
                    sl.sendParticles(Salobre.POLVO, getX(), getY() + 0.4, getZ(), 20, 0.8, 0.2, 0.8, 0.1);
                }
            }
            default -> {
            }
        }
    }

    /** Donde esta su mano derecha (la del arpon y el cabo): delante, a su derecha, a la altura del pecho. */
    public Vec3 mano() {
        Vec3 d = Vec3.directionFromRotation(0, yBodyRot);
        return new Vec3(getX() + d.x * 0.9 - d.z * 0.75, getY() + 2.1, getZ() + d.z * 0.9 + d.x * 0.75);
    }

    /** Donde esta su mano izquierda (la del farol). */
    private Vec3 manoIzquierda() {
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        return new Vec3(getX() + d.x * 0.8 + d.z * 0.8, getY() + 2.4, getZ() + d.z * 0.8 - d.x * 0.8);
    }

    // ------------------------------------------------------------------ el arpon y el tiron

    /** Lanza el arpon atado a su cabo hacia su presa (tiro de flecha pesada: con la caida en cuenta). */
    private void lanzar() {
        LivingEntity p = getTarget();
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 o = mano();
        ArponBallenero a = new ArponBallenero(sl, this);
        a.setPos(o.x, o.y, o.z);
        Vec3 dir;
        if (p != null) {
            double dx = p.getX() - o.x, dz = p.getZ() - o.z, h = Math.sqrt(dx * dx + dz * dz);
            Vec3 plano = h < 0.01 ? Vec3.directionFromRotation(0, getYRot()) : new Vec3(dx / h, 0, dz / h);
            dir = Enemigo.tiro(h, p.getY() + p.getBbHeight() * 0.55 - o.y, plano, 2.4);
        } else dir = Vec3.directionFromRotation(-4f, getYRot());
        a.shoot(dir.x, dir.y, dir.z, 2.4f, 0.6f);
        sl.addFreshEntity(a);
        arpon = a;
        enganche = null;
        lanzados++;
        playSound(DarkCraftMod.SFX_BALLENERO_ARPON.get(), 2.2f, 1.0f);
    }

    /** El arpon se le ha clavado a alguien: tira del cabo en cuanto pueda (y la primera vez lo dice). */
    void enganchado(LivingEntity le) {
        enganche = le;
        if (!cuatro) {
            cuatro = true;
            hablaSiCalla(4);
        }
    }

    /** Tira del cabo: arrastra hacia el a quien tenga el arpon clavado; si no hay nadie, lo recoge. */
    private void tironazo() {
        tirones++;
        ArponBallenero a = arpon();
        LivingEntity le = a != null ? a.clavado() : null;
        if (le == null) le = enganche;
        if (le != null && le.isAlive() && le.distanceToSqr(this) < 40 * 40) {
            double dx = getX() - le.getX(), dz = getZ() - le.getZ(), l = Math.sqrt(dx * dx + dz * dz);
            if (l > 2.2) {
                double f = TIRON * Mth.clamp((l - 1.6) / 6.0, 0.35, 1.4) * (1.0 - le.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) * 0.6);
                le.setDeltaMovement(dx / l * f, 0.42, dz / l * f);
                le.hurtMarked = true;
                arrastrados++;
                arrastre = Math.max(arrastre, f);
                if (level() instanceof ServerLevel sl) {
                    // el cabo que se tensa: gotas de agua a lo largo
                    for (int k = 1; k < 8; k++) {
                        double s = k / 8.0;
                        sl.sendParticles(ParticleTypes.SPLASH, Mth.lerp(s, le.getX(), getX()), Mth.lerp(s, le.getY() + 1.0, getY() + 2.0),
                                Mth.lerp(s, le.getZ(), getZ()), 3, 0.05, 0.05, 0.05, 0.05);
                    }
                }
            }
        }
        if (a != null) a.recoger();
        enganche = null;
    }

    /** El arpon vuelve a la mano (o se deshace si sigue fuera). */
    private void devolver() {
        ArponBallenero a = arpon();
        if (a != null) a.discard();
        arpon = null;
        enganche = null;
        entityData.set(ARPON_FUERA, false);
    }

    // ------------------------------------------------------------------ el farol

    /** El farol de aceite, en arco hacia su presa (cae donde estaba al soltarlo). */
    private void lanzarFarol() {
        LivingEntity p = getTarget();
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 o = manoIzquierda();
        FarolAceite f = new FarolAceite(sl, this);
        f.setPos(o.x, o.y, o.z);
        if (p != null) f.setDeltaMovement(Cantaro.tiro(o.x, o.y, o.z, p.getX(), p.getY() + 0.3, p.getZ()));
        else f.setDeltaMovement(Vec3.directionFromRotation(-30f, getYRot()).scale(0.8));
        sl.addFreshEntity(f);
        faroles++;
    }

    // ------------------------------------------------------------------ el salto

    /** Cae clavando el arpon: la onda tumba a quien este de pie cerca (saltando o rodando se libra). */
    private void onda() {
        if (!(level() instanceof ServerLevel sl)) return;
        playSound(DarkCraftMod.SFX_BALLENERO_ARPONAZO.get(), 2.6f, 0.7f);
        playSound(SoundEvents.GENERIC_EXPLODE, 0.8f, 1.4f);
        for (int k = 0; k < 28; k++) {
            double a = k * Math.PI * 2 / 28;
            for (double r = 1.5; r <= R_ONDA; r += 1.2)
                sl.sendParticles(Salobre.POLVO, getX() + Math.cos(a) * r, getY() + 0.2, getZ() + Math.sin(a) * r, 1, 0.1, 0.05, 0.1, 0.05);
        }
        sl.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 0.3, getZ(), 60, 2.0, 0.2, 2.0, 0.4);
        sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.4, getZ(), 20, 1.6, 0.2, 1.6, 0.05);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.7f;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(R_ONDA, 2, R_ONDA),
                e -> e != this && e.isAlive() && objetivoValido(e))) {
            if (le.distanceToSqr(getX(), le.getY(), getZ()) > R_ONDA * R_ONDA) continue;
            if (!le.onGround() || Math.abs(le.getY() - getY()) > 1.5) continue;
            if (le.hurt(damageSources().mobAttack(this), dano)) {
                ondasQueDan++;
                double dx = le.getX() - getX(), dz = le.getZ() - getZ(), l = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                le.setDeltaMovement(dx / l * 0.6, 0.5, dz / l * 0.6);
                le.hurtMarked = true;
            }
        }
    }

    // ------------------------------------------------------------------ la Vieja

    /**
     * Llama a la Vieja: embiste de un lado a otro por donde esta su presa. En el costillar, a lo largo de la sala (de oeste a
     * este o al reves, a la v de su presa); fuera, en la direccion de Brann a su presa.
     */
    private void llamarVieja() {
        LivingEntity p = getTarget();
        if (!(level() instanceof ServerLevel sl)) return;
        llamadas++;
        double px = p != null ? p.getX() : getX(), pz = p != null ? p.getZ() : getZ();
        Vec3 desde, hasta;
        double suelo;
        if (Costillar.enSala(getX(), getZ())) {
            double v = Mth.clamp(pz - Costillar.CZ - 0.5, -9, 9);
            boolean alEste = llamadas % 2 == 1;
            double u0 = Costillar.SALA_U0 - 6, u1 = Costillar.SALA_U1 + 2;
            desde = new Vec3(Costillar.CX + (alEste ? u0 : u1), 0, Costillar.CZ + 0.5 + v);
            hasta = new Vec3(Costillar.CX + (alEste ? u1 : u0), 0, Costillar.CZ + 0.5 + v);
            suelo = Costillar.PISO;
        } else {
            double dx = px - getX(), dz = pz - getZ(), l = Math.sqrt(dx * dx + dz * dz);
            Vec3 d = l < 0.5 ? Vec3.directionFromRotation(0, getYRot()) : new Vec3(dx / l, 0, dz / l);
            desde = new Vec3(px - d.x * 20, 0, pz - d.z * 20);
            hasta = new Vec3(px + d.x * 20, 0, pz + d.z * 20);
            suelo = Puestos.suelo(sl, Mth.floor(px), Mth.floor(p != null ? p.getY() : getY()) + 2, Mth.floor(pz));
        }
        Vieja.embestir(sl, this, desde, hasta, suelo);
        DarkCraftMod.LOGGER.info("[DarkCraft] Brann llama a la Vieja: de {} a {}", desde, hasta);
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
        llamo = cuatro = false;
        arponEspera = 60;
        farolEspera = 110;
        saltoEspera = 60;
        viejaEspera = -1;
        repetido = 0;
        long ahora = level().getGameTime();
        if (ahora - ultimaEntrada > 20 * 60 * 4 || ahora < ultimaEntrada) {
            ultimaEntrada = ahora;
            habla(1);
            // se levanta mientras lo dice y no grita ni ataca hasta acabarlo
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
        devolver();
        esperaRugido = 0;
        viejaEspera = -1;
    }

    /** A media vida no grita: llama a la Vieja (y desde entonces la vuelve a llamar cada tanto, y salta). */
    @Override
    protected void alSegundaMitad() {
        devolver();
        empezar(BalleneroAnims.LLAMAR);
        pausa = Math.max(pausa, 6);
        viejaEspera = VIEJA_CADA;
        saltoEspera = Math.min(saltoEspera, 40);
    }

    @Override
    public void setTarget(LivingEntity t) {
        super.setTarget(t);
        if (t != null && !level().isClientSide) ultimaPresa = t;
    }

    /** Solo repara en quien esta dentro de las costillas (los de fuera no le importan), si espera en su costillar. */
    @Override
    protected boolean percibe(LivingEntity p) {
        if (Costillar.enSala(getX(), getZ()) && !Costillar.enSala(p.getX(), p.getZ())) return false;
        return super.percibe(p);
    }

    @Override
    protected boolean objetivoValido(LivingEntity e) {
        if (e instanceof Player && enPelea() && Costillar.enSala(getX(), getZ()) && !Costillar.enSala(e.getX(), e.getZ())) return false;
        return super.objetivoValido(e);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            if (!isAlive() && deathTime > BalleneroAnims.TICKS[BalleneroAnims.MORIR]) return;
            // la sal que se le cae y la llama del farol
            if (tickCount % 5 == 0)
                level().addParticle(SAL, getX() + (random.nextDouble() - 0.5) * 1.4, getY() + 0.6 + random.nextDouble() * 2.6,
                        getZ() + (random.nextDouble() - 0.5) * 1.4, 0, -0.02, 0);
            return;
        }
        if (!isAlive()) return;
        if (arponEspera > 0) arponEspera--;
        if (farolEspera > 0) farolEspera--;
        if (saltoEspera > 0) saltoEspera--;
        // el arpon esta fuera y ya ha dado en algo (o ha acabado su vuelo): tira del cabo
        ArponBallenero a = arpon();
        if (arponFuera()) {
            if (a == null) {
                if (accion() != BalleneroAnims.ARPON && accion() != BalleneroAnims.TIRON) devolver();
            } else if (a.listo() && !a.vuelve() && (accion() == 0 || accion() == BalleneroAnims.ARPON && accionT > BalleneroAnims.GOLPES[BalleneroAnims.ARPON][0] + 2)) {
                LivingEntity c = a.clavado();
                if (c != null) girarHacia(c, 360f);
                empezar(BalleneroAnims.TIRON);
            }
        }
        // a media vida, la Vieja vuelve cada tanto
        if (segunda && enPelea() && viejaEspera > 0 && --viejaEspera <= 0) {
            if (accion() == 0 && getTarget() != null && !quieta && !arponFuera()) {
                empezar(BalleneroAnims.LLAMAR);
                viejaEspera = VIEJA_CADA + random.nextInt(80);
            } else viejaEspera = 10;
        }
        // de vigia: el mar que no esta, y su espera (el viento en los huesos y las olas que no llegan)
        if (!enPelea() && postura() == REZA && --espera <= 0) {
            espera = 200 + random.nextInt(80);
            playSound(DarkCraftMod.SFX_BALLENERO_ESPERA.get(), 1.6f, 1.0f);
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
        devolver();
        super.die(s);
        if (level().isClientSide || ya) return;
        hablaHasta = 0;
        habla(5);
        if (level() instanceof ServerLevel sl) sl.sendParticles(SAL, getX(), getY() + 2.4, getZ(), 50, 1.0, 1.4, 1.0, 0.0);
    }

    /** Al caer no se va en humo: se queda de rodillas agarrado al arpon, dice su ultima frase, la Vieja sube y se deshace en sal. */
    @Override
    protected void tickDeath() {
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        int fin = BalleneroAnims.TICKS[BalleneroAnims.MORIR] + 50;
        if (level() instanceof ServerLevel sl) {
            if (deathTime == 40) {
                // la Vieja sale de la arena por ultima vez y se va al cielo
                double x = Costillar.enSala(getX(), getZ()) ? Costillar.CX + 0.5 : getX() + Vec3.directionFromRotation(0, getYRot()).x * 10;
                double z = Costillar.enSala(getX(), getZ()) ? Costillar.CZ + 0.5 : getZ() + Vec3.directionFromRotation(0, getYRot()).z * 10;
                Vieja.subir(sl, x, getY(), z, getYRot());
            }
            if (deathTime % 4 == 0 && deathTime < fin - 6)
                sl.sendParticles(SAL, getX(), getY() + 1.6, getZ(), 4, 0.5, 0.8, 0.5, 0.0);
            if (deathTime > fin - 30 && deathTime % 2 == 0)
                sl.sendParticles(Salobre.POLVO, getX(), getY() + 1.0 + (deathTime - (fin - 30)) * 0.08, getZ(), 8, 0.5, 0.6, 0.5, 0.05);
            if (deathTime == fin - 2) {
                // se deshace en sal
                sl.sendParticles(Salobre.POLVO, getX(), getY() + 1.6, getZ(), 120, 0.7, 1.4, 0.7, 0.15);
                sl.sendParticles(SAL, getX(), getY() + 1.6, getZ(), 80, 0.8, 1.6, 0.8, 0.0);
                sl.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 0.5, getZ(), 60, 1.0, 0.3, 1.0, 0.3);
                sl.playSound(null, getX(), getY() + 1, getZ(), DarkCraftMod.SFX_SALOBRE_MUERTE.get(), SoundSource.HOSTILE, 2.0f, 0.7f);
            }
        }
        if (deathTime >= fin) remove(RemovalReason.KILLED);
    }

    @Override
    public void remove(RemovalReason r) {
        if (!level().isClientSide) {
            ArponBallenero a = arpon();
            if (a != null) a.discard();
        }
        super.remove(r);
    }

    /** Mientras dice su primera frase se queda donde esta (levantandose); despues, cerca: pelea de cerca. */
    @Override
    protected double distanciaDeseada() {
        return enPelea() && tickCount < esperaRugido ? 99.0 : 3.2;
    }

    @Override
    protected double prisa() {
        return segunda ? 1.12 : 1.0;
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (accion) {
            case BalleneroAnims.GRITO, BalleneroAnims.LLAMAR -> 6;
            case BalleneroAnims.ARPON -> 2;
            case BalleneroAnims.TIRON -> 4 + random.nextInt(6);
            case BalleneroAnims.SALTO -> (segunda ? 8 : 12) + random.nextInt(8);
            case BalleneroAnims.FAROL -> (segunda ? 10 : 16) + random.nextInt(10);
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

    /** La pata de palo en la arena. */
    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        playSound(tickCount % 2 == 0 ? SoundEvents.WOOD_STEP : SoundEvents.SAND_STEP, 0.5f, 0.7f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_BALLENERO_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_BALLENERO_MUERTE.get();
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_3.get(), 3));
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_2.get(), 2));
        spawnAtLocation(new ItemStack(DarkCraftMod.CENIZAS.get(com.darkcraft.combate.Habilidad.ESTOCADA).get()));
    }

    /** Para las pruebas y el comando: como esta. */
    public String estado() {
        return String.format(java.util.Locale.ROOT, "vida %.0f/%.0f, %s, %s en %.1f,%.1f,%.1f; arponazos %d, tajos %d, arpones %d (tirones %d, arrastra a %d, hasta %.2f), "
                        + "faroles %d, saltos %d (la onda da a %d), la Vieja %d%s",
                getHealth(), getMaxHealth(), enPelea() ? "peleando" : "esperando", postura() == DE_PIE ? "de pie" : "de vigia",
                getX(), getY(), getZ(), arponazos, tajos, lanzados, tirones, arrastrados, arrastre, faroles, saltos, ondasQueDan, llamadas,
                segunda ? " (a media vida)" : "");
    }

    /** Que todo el mundo pueda ver el aviso de la Vieja: lo de alrededor de la sala, para las pruebas. */
    public static AABB caja() {
        return new AABB(Costillar.CX - 40, Costillar.PISO - 4, Costillar.CZ - 24, Costillar.CX + 40, Costillar.PISO + 30, Costillar.CZ + 24);
    }
}
