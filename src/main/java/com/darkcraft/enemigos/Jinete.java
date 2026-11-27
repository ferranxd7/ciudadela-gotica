package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.camino.Jinetes;
import com.darkcraft.net.Net;
import com.darkcraft.rey.Voces;
import com.darkcraft.world.CaminoNubes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Ser Lucan, el Jinete Nocturno (parte 7x; respuesta 132 del plan: "jinete nocturno, solo de noche, en caminos"): el heraldo
 * del Rey de Valdrenn que salio una noche por el camino de nubes con la ultima carta del Rey para la Fortaleza de Hueso y no
 * llego. Cada noche sale de la Torre de Ascenso y cabalga hacia la fortaleza; al amanecer se deshace sin haber llegado. Lo saca
 * y lo recoge {@link Jinetes} (sabe por donde va cada noche aunque no le vea nadie).
 * <p>
 * Es un fantasma montado: no tiene fisica ({@code noPhysics}) y va siempre a la altura del camino (los cascos a ras de lo que se
 * pisa en el camino de nubes): galopa por el camino y, peleando, tambien por encima de la nube, donde nadie mas puede ir. Ve a
 * quien tiene delante a 40 bloques; si su presa se le va a mas de {@link #CORREA}, la deja.
 * <p>
 * Sus golpes: la <b>carga</b> (de lejos: baja la guja como una lanza y cruza al galope por donde esta su presa, llevandose a
 * todo lo que pilla por delante; se esquiva rodando o saltando a un lado), el <b>tajo</b> (de cerca, desde la silla), el
 * <b>barrido</b> (la guja da la vuelta entera mientras el caballo gira; a quien se le pone detras o si son varios), el
 * <b>pisoton</b> (el caballo se alza y cae con las manos: la onda delante) y el <b>cuerno</b> (al empezar y a media vida: hiela la
 * sangre de los de cerca, que van mas despacio). Lo que empuja en el camino empuja a lo largo del camino, nunca fuera (caer del
 * camino de nubes es caer al vacio). A media vida, el <b>fuego de almas</b>: por donde galopa quedan llamas azules que queman. Si
 * se le pega mucho en poco tiempo, el caballo tropieza y cae sobre las rodillas (y entonces les entra mas).
 * <p>
 * Habla (ElevenLabs, en ingles con subtitulos; {@link Voces#BASE_JINETE} + N): 1 al verte (no lo repite en 4 min), 2 a media
 * vida, 3 si le llega el amanecer, 4 el grito de la carga (de vez en cuando) y 5 al caer. Al caer deja la carta que llevaba (el
 * libro perdido 31) a cada uno de los que le plantaron cara; lo demas (el cartel con sus ecos, la hazana y el bestiario) lo
 * reparte {@code valdrenn/Guardianes} como el de los demas jefes. El de un camino, caido, no vuelve.
 */
public class Jinete extends Guardian {
    public static final String TIPO = "jinete";
    public static final int ECOS = 8000;
    public static final int VOZ_BASE = Voces.BASE_JINETE;
    /** Lo que corre (bloques por tick): al trote por su camino, al galope peleando y en la carga. */
    public static final double TROTE = 0.32, GALOPE = 0.55, CARGA = 0.95;
    /** Lo que gira (grados por tick) cabalgando por el camino y peleando. */
    public static final float GIRO = 5f, GIRO_PELEA = 9f;
    /** Lo lejos que deja ir a su presa antes de dejarla. */
    public static final double CORREA = 72;
    /** La carga: desde que tick galopa y hasta cual, y lo ancho que barre (a cada lado de su camino). */
    public static final int CARGA_DESDE = 15, CARGA_HASTA = 51;
    public static final double CARGA_LADO = 1.7;
    /** El pisoton: donde caen las manos (delante) y el radio de la onda. */
    public static final double PISOTON_DELANTE = 2.4, R_PISOTON = 3.4;
    /** El cuerno: a cuantos bloques hiela la sangre. */
    public static final double R_CUERNO = 10;
    /** El fuego de almas (a media vida): lo que dura cada llama (ticks), su radio y lo que quema cada medio segundo. */
    public static final int FUEGO_DURA = 110;
    public static final double R_FUEGO = 0.9;
    public static final float FUEGO_DANO = 3.0f;
    /** Lo que tiene que recibir en {@link #VENTANA} ticks (fraccion de su vida) para que el caballo tropiece. */
    public static final float AGUANTE = 0.08f;
    public static final int VENTANA = 100, ATURDIDO_ESPERA = 500;

    /** La ultima vez que dijo su frase de entrada (tiempo del mundo, por camino): no la repite en cada intento. */
    private static final java.util.Map<String, Long> ULTIMA_ENTRADA = new java.util.HashMap<>();

    /** El camino por el que va (o null: sacado a mano lejos de todo camino) y el reino de ese camino. */
    private CaminoNubes.Ruta ruta;
    private String reino;
    private double s;
    private int sentido = 1;
    /** La altura de los cascos (el camino de nubes: siempre la misma). */
    private double pies = Double.NaN;
    private int cargaEspera = 40, tajoEspera = 0, barridoEspera = 30, pisotonEspera = 20, relinchoEspera = 200, arnesEspera = 90,
            hablaHasta, esperaRugido, recibidoT, aturdidoEspera, alejarse, ultimaArenga = -100000;
    private float recibido;
    private float rumboCarga;
    private boolean amanece, andando;
    /** Los que ya ha pillado la carga que esta haciendo. */
    private final Set<Integer> tocadosCarga = new HashSet<>();
    /** Las llamas de almas que deja a media vida: {x, y, z, hasta que tick}. */
    private final List<double[]> fuegos = new ArrayList<>();
    /** Para las pruebas: cuantas veces ha hecho cada cosa y a cuantos ha dado. */
    private int cargas, golpesCarga, tajos, barridos, pisotones, ondasPisoton, cuernos, helados, fuegosPuestos, quemados, aturdidos,
            fueraDelCamino;
    /** Para las pruebas: no elige golpes por su cuenta (lo que se le manda hacer, si). */
    public boolean quieta;
    /** Para las pruebas: no cabalga por su camino (se queda donde esta si no tiene presa). */
    public boolean parado;
    /** En el cliente: lo que corre (bloques por tick, suavizado) y la fase del paso (lo andado), para el modelo. */
    public float velocidad, velocidadO, fase, faseO;
    /** En el cliente: el ultimo casco que sono y cuando empezo el ultimo galope que suena (lo usa JineteCliente). */
    public int galopeVisto = -1000;

    public Jinete(EntityType<? extends Jinete> t, Level l) {
        super(t, l, TIPO, BossEvent.BossBarColor.BLUE);
        noPhysics = true;
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1100.0).add(Attributes.ATTACK_DAMAGE, 12.0).add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 3.0).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    /** Su camino (y el reino de ese camino) y lo andado de el. */
    public void camino(CaminoNubes.Ruta r, String reino, double s) {
        this.ruta = r;
        this.reino = reino;
        this.s = s;
    }

    public CaminoNubes.Ruta ruta() {
        return ruta;
    }

    public String reino() {
        return reino;
    }

    public double s() {
        return s;
    }

    /** La altura de los cascos (la de donde sale; el camino de nubes es llano). */
    public void pies(double y) {
        pies = y;
    }

    @Override
    public String tipo() {
        return TIPO;
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable("entity.darkcraft." + TIPO);
    }

    @Override
    public int musica() {
        return 43;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return TIPO;
    }

    @Override
    protected int[] ticks() {
        return JineteAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return JineteAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return JineteAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return JineteAnims.MORIR;
    }

    @Override
    protected int accionRugido() {
        return JineteAnims.CUERNO;
    }

    @Override
    protected int accionLevantarse() {
        return 0;
    }

    @Override
    protected double distanciaDeseada() {
        return 3.2;
    }

    @Override
    protected double alturaMaxima() {
        return 6.0;
    }

    @Override
    protected double correa() {
        return CORREA;
    }

    // ------------------------------------------------------------------ cuerpo: ni fisica, ni empujones, ni se guarda

    @Override
    protected void registerGoals() {
        // ni el navegador del juego (va por encima de la nube) ni volver a un puesto: eso lo hace el mismo (mover)
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::percibe));
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void checkDespawn() {
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(3.0, 1.5, 3.0);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_JINETE_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_JINETE_MUERTE.get();
    }

    @Override
    protected float getSoundVolume() {
        return 2.2f;
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return null;
    }

    // ------------------------------------------------------------------ para las pruebas

    public int cargas() {
        return cargas;
    }

    /** A cuantos se ha llevado por delante en sus cargas. */
    public int golpesCarga() {
        return golpesCarga;
    }

    public int tajos() {
        return tajos;
    }

    public int barridos() {
        return barridos;
    }

    public int pisotones() {
        return pisotones;
    }

    public int ondasPisoton() {
        return ondasPisoton;
    }

    /** Las veces que ha tocado el cuerno y a cuantos les ha helado la sangre. */
    public int cuernos() {
        return cuernos;
    }

    public int helados() {
        return helados;
    }

    /** Las llamas de almas que ha dejado y las veces que han quemado a alguien. */
    public int fuegosPuestos() {
        return fuegosPuestos;
    }

    public int quemados() {
        return quemados;
    }

    public int fuegos() {
        return fuegos.size();
    }

    public int aturdidos() {
        return aturdidos;
    }

    /** Las veces que un empujon suyo habria echado a alguien del camino (y se cambio por uno a lo largo del camino). */
    public int fueraDelCamino() {
        return fueraDelCamino;
    }

    public boolean andando() {
        return andando;
    }

    /** Si ya se esta deshaciendo con el amanecer. */
    public boolean amanece() {
        return amanece;
    }

    /** Para el comando y las pruebas: que caiga ya. */
    public void vencer() {
        if (level().isClientSide || !isAlive()) return;
        kill();
    }

    /** Para las pruebas: pasa ya a media vida. */
    public void mediaVida() {
        if (level().isClientSide || !isAlive()) return;
        setHealth(Math.min(getHealth(), getMaxHealth() * 0.45f));
    }

    /** Para las pruebas y el comando: que el caballo tropiece ya. */
    public void derribar() {
        aturdidos++;
        aturdidoEspera = ATURDIDO_ESPERA;
        aturdir();
    }

    // ------------------------------------------------------------------ que hace

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (quieta || amanece) return 0;
        double dy = presa.getY() - getY();
        float ang = angulo(presa);
        // de cerca: el pisoton (si la tiene delante), el barrido (si se le pone detras o son varios) o el tajo
        if (d < 3.9 && ang < 50 && Math.abs(dy) < 2.5 && pisotonEspera <= 0) {
            pisotonEspera = (segunda ? 80 : 110) + random.nextInt(40);
            return JineteAnims.PISOTEA;
        }
        if (d < 5.4) {
            if (barridoEspera <= 0 && (ang > 100 || cuantosCerca(5.6) >= 2 || random.nextFloat() < 0.25f)) {
                barridoEspera = (segunda ? 70 : 100) + random.nextInt(40);
                alejarse = 30 + random.nextInt(30);
                return JineteAnims.BARRIDO;
            }
            if (tajoEspera <= 0 && ang < 80) {
                tajoEspera = (segunda ? 22 : 34) + random.nextInt(20);
                // a veces, despues de pegar, se aleja para cargar otra vez
                if (random.nextFloat() < 0.45f) alejarse = 40 + random.nextInt(30);
                return JineteAnims.TAJO;
            }
        }
        // de lejos: la carga (de cara a su presa)
        if (d >= 8.5 && d <= 34 && cargaEspera <= 0 && ang < 70) {
            cargaEspera = (segunda ? 70 : 110) + random.nextInt(50);
            return JineteAnims.CARGA;
        }
        return 0;
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (accion) {
            case JineteAnims.CARGA -> 8 + random.nextInt(10);
            case JineteAnims.CUERNO, JineteAnims.ATURDIDO, JineteAnims.APARECE -> 8;
            default -> 10 + random.nextInt(12);
        };
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case JineteAnims.TAJO -> new Golpe(5.4f, 150f, 1.0f, 0f);
            case JineteAnims.BARRIDO -> new Golpe(5.6f, 360f, 0.9f, 0f);
            default -> null;
        };
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (accion == JineteAnims.TAJO) {
            tajos++;
            empujarEnElCamino(le, 0.55, le.getX() - getX(), le.getZ() - getZ(), 0.1);
        } else if (accion == JineteAnims.BARRIDO) {
            barridos++;
            empujarEnElCamino(le, 0.8, le.getX() - getX(), le.getZ() - getZ(), 0.15);
        }
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = JineteAnims.GOLPES[accion];
        switch (accion) {
            case JineteAnims.TAJO -> {
                if (t == g[0] - 4) playSound(DarkCraftMod.SFX_JINETE_TAJO.get(), 1.8f, 0.9f + random.nextFloat() * 0.15f);
                if (t == g[0]) estela(150, 4.6, 2.2);
            }
            case JineteAnims.BARRIDO -> {
                if (t == 4) playSound(DarkCraftMod.SFX_JINETE_ARNES.get(), 1.4f, 0.9f);
                if (t == g[0] - 5) playSound(DarkCraftMod.SFX_JINETE_TAJO.get(), 2.0f, 0.8f);
                // el caballo gira en el sitio mientras la guja da la vuelta
                if (t >= 10 && t < 22) {
                    setYRot(getYRot() + 15f);
                    yBodyRot = yHeadRot = getYRot();
                }
                if (t == g[0]) estela(360, 4.8, 1.8);
            }
            case JineteAnims.CARGA -> {
                if (t == 2) playSound(DarkCraftMod.SFX_JINETE_RELINCHO.get(), 2.4f, 0.95f + random.nextFloat() * 0.1f);
                if (t == 4) arenga();
                if (t == CARGA_DESDE) {
                    cargas++;
                    tocadosCarga.clear();
                    playSound(DarkCraftMod.SFX_JINETE_CARGA.get(), 2.6f, 0.95f + random.nextFloat() * 0.1f);
                    if (segunda) playSound(DarkCraftMod.SFX_JINETE_FUEGO.get(), 2.0f, 0.9f);
                }
            }
            case JineteAnims.PISOTEA -> {
                if (t == 6) playSound(DarkCraftMod.SFX_JINETE_RELINCHO.get(), 2.6f, 0.9f + random.nextFloat() * 0.1f);
                if (t == g[0] - 2) playSound(DarkCraftMod.SFX_JINETE_PISOTEA.get(), 2.8f, 0.9f + random.nextFloat() * 0.1f);
                if (t == g[0]) pisoton();
            }
            case JineteAnims.CUERNO -> {
                if (t == 12) playSound(DarkCraftMod.SFX_JINETE_CUERNO.get(), 6.0f, 0.95f);
                if (t == g[0]) cuerno();
            }
            case JineteAnims.ATURDIDO -> {
                if (t == 2) playSound(DarkCraftMod.SFX_JINETE_RELINCHO.get(), 2.0f, 0.75f);
                if (t == 12 && level() instanceof ServerLevel sl) {
                    playSound(DarkCraftMod.SFX_JINETE_PISOTEA.get(), 2.0f, 0.75f);
                    sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 0.4, getZ(), 18, 1.0, 0.2, 1.0, 0.02);
                    sl.sendParticles(ParticleTypes.CRIT, getX(), getY() + 2.6, getZ(), 12, 0.6, 0.4, 0.6, 0.2);
                }
            }
            case JineteAnims.APARECE -> {
                if (t == 1) playSound(DarkCraftMod.SFX_JINETE_APARECE.get(), 3.0f, 1.0f);
                if (t == 10) playSound(DarkCraftMod.SFX_JINETE_RELINCHO.get(), 2.4f, 1.0f);
                if (t % 3 == 0 && level() instanceof ServerLevel sl)
                    sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.6, getZ(), 6, 0.9, 1.0, 0.9, 0.02);
            }
            case JineteAnims.DESAPARECE -> {
                if (t == 2) playSound(DarkCraftMod.SFX_JINETE_APARECE.get(), 2.6f, 0.8f);
                if (t % 3 == 0 && level() instanceof ServerLevel sl)
                    sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.4, getZ(), 8, 0.9, 1.0, 0.9, 0.03);
                if (t >= JineteAnims.TICKS[accion] - 1) {
                    if (level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.5, getZ(), 40, 1.0, 1.2, 1.0, 0.04);
                        sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1.0, getZ(), 20, 1.0, 0.8, 1.0, 0.02);
                    }
                    DarkCraftMod.LOGGER.info("[DarkCraft] el Jinete Nocturno se deshace al amanecer en {}", blockPosition().toShortString());
                    discard();
                }
            }
            default -> {
            }
        }
    }

    @Override
    protected void alSegundaMitad() {
        hablaSiCalla(2);
        cargaEspera = Math.min(cargaEspera, 30);
    }

    @Override
    protected void alEmpezarPelea() {
        cargaEspera = 30;
        tajoEspera = 0;
        barridoEspera = 30;
        pisotonEspera = 20;
        long ahora = level().getGameTime();
        String k = reino != null ? reino : "";
        Long antes = ULTIMA_ENTRADA.get(k);
        if (antes == null || ahora - antes > 20 * 60 * 4 || ahora < antes) {
            ULTIMA_ENTRADA.put(k, ahora);
            habla(1);
            // el cuerno al acabar de hablar (entretanto se le encara, pero no pega)
            int dura = Voces.ticks(VOZ_BASE + 1);
            esperaRugido = tickCount + dura - 6;
            pausa = Math.max(pausa, dura);
        } else esperaRugido = 0;
    }

    @Override
    protected boolean puedeRugir() {
        return !amanece && (segunda || tickCount >= esperaRugido);
    }

    /** Dice su frase n (1 al verte, 2 a media vida, 3 al amanecer, 4 el grito de la carga, 5 al caer). */
    public void habla(int n) {
        if (!(level() instanceof ServerLevel sl)) return;
        int linea = VOZ_BASE + n;
        for (ServerPlayer p : sl.players()) if (p.distanceToSqr(this) < 72 * 72) Net.to(p, new Net.ReyHabla(getId(), linea));
        hablaHasta = tickCount + Voces.ticks(linea);
    }

    private void hablaSiCalla(int n) {
        if (tickCount >= hablaHasta + Voces.PAUSA) habla(n);
    }

    /** El grito de la carga: de vez en cuando (no cada carga) y si no esta hablando. */
    private void arenga() {
        if (tickCount - ultimaArenga < 900 || tickCount < hablaHasta + Voces.PAUSA || random.nextFloat() > 0.6f) return;
        ultimaArenga = tickCount;
        habla(4);
    }

    /**
     * Le llega el amanecer (lo llama {@link Jinetes}): si hay alguien cerca dice su frase (o la acaba) y se deshace; si no, se
     * deshace sin mas. Una vez.
     */
    public void amanecer() {
        if (amanece || level().isClientSide || !isAlive()) return;
        amanece = true;
        boolean alguien = level().getNearestPlayer(this, 48) != null;
        if (alguien) habla(3);
        setTarget(null);
        int espera = alguien ? Math.max(0, Voces.ticks(VOZ_BASE + 3) - 30) : 0;
        amanecerEn = tickCount + espera;
    }

    private int amanecerEn = Integer.MAX_VALUE;

    // ------------------------------------------------------------------ cada tick

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            // lo que corre y la fase del paso (para el modelo: el galope va al ritmo de lo que avanza)
            double dx = getX() - xo, dz = getZ() - zo;
            float v = (float) Math.sqrt(dx * dx + dz * dz);
            velocidadO = velocidad;
            velocidad += (v - velocidad) * 0.35f;
            faseO = fase;
            fase += v;
        }
    }

    @Override
    public void aiStep() {
        // primero se mueve (lo que anda en este tick lo cuenta el paso de la animacion del juego, que va en travel)
        if (!level().isClientSide && isAlive()) mover();
        super.aiStep();
        if (level().isClientSide || !isAlive()) return;
        if (cargaEspera > 0) cargaEspera--;
        if (tajoEspera > 0) tajoEspera--;
        if (barridoEspera > 0) barridoEspera--;
        if (pisotonEspera > 0) pisotonEspera--;
        if (aturdidoEspera > 0) aturdidoEspera--;
        if (alejarse > 0) alejarse--;
        if (recibidoT > 0 && --recibidoT == 0) recibido = 0;
        arderFuegos();
        // al amanecer: cuando acaba de hablar (o en seguida, si no hay nadie), se deshace
        if (amanece && tickCount >= amanecerEn && accion() != JineteAnims.DESAPARECE && !muriendo()) {
            amanecerEn = Integer.MAX_VALUE;
            empezar(JineteAnims.DESAPARECE);
        }
        // cabalgando: relincha de vez en cuando y le cruje el arnes
        if (accion() == 0 && getTarget() == null) {
            if (--relinchoEspera <= 0) {
                relinchoEspera = 300 + random.nextInt(400);
                playSound(DarkCraftMod.SFX_JINETE_RELINCHO.get(), 2.0f, 0.9f + random.nextFloat() * 0.15f);
            }
            if (--arnesEspera <= 0) {
                arnesEspera = 120 + random.nextInt(120);
                playSound(DarkCraftMod.SFX_JINETE_ARNES.get(), 0.9f, 0.9f + random.nextFloat() * 0.2f);
            }
        }
        // a media vida, los cascos echan fuego de almas al galope (cada tres ticks, una llama donde pisa)
        if (segunda && enPelea() && tickCount % 3 == 0 && Math.hypot(getX() - xo, getZ() - zo) > 0.3) llama(getX(), getZ());
    }

    /** Por donde va: detras de su presa, de vuelta a su camino o por el. Los cascos, siempre a la altura del camino. */
    private void mover() {
        setDeltaMovement(Vec3.ZERO);
        andando = false;
        if (Double.isNaN(pies)) pies = getY();
        LivingEntity t = getTarget();
        if (t != null && (!t.isAlive() || !objetivoValido(t) || distanceToSqr(t) > CORREA * CORREA || amanece)) {
            setTarget(null);
            t = null;
        }
        int a = accion();
        if (a != 0) {
            moverEnAccion(a, t);
            altura();
            return;
        }
        if (muriendo() || !isAlive() || amanece) {
            altura();
            return;
        }
        if (t != null) pelear(t);
        else if (!parado) cabalgar();
        altura();
    }

    /** Los cascos a la altura del camino. */
    private void altura() {
        if (!Double.isNaN(pies) && Math.abs(getY() - pies) > 1e-4) setPos(getX(), pies, getZ());
    }

    /** Lo que se mueve haciendo cada cosa: la carga galopa derecha; lo demas, quieto (girando hacia su presa al prepararse). */
    private void moverEnAccion(int a, LivingEntity t) {
        if (a == JineteAnims.CARGA) {
            int k = accionT;
            if (k < CARGA_DESDE - 3) {
                if (t != null) girarHacia(t, k < 4 ? 20f : 10f);
                rumboCarga = getYRot();
            } else {
                setYRot(rumboCarga);
                yBodyRot = yHeadRot = rumboCarga;
            }
            double v = k < CARGA_DESDE ? 0.04 * k / CARGA_DESDE : k <= CARGA_HASTA ? CARGA : Math.max(0, CARGA * (1.0 - (k - CARGA_HASTA) / 8.0));
            if (v > 0.001) {
                Vec3 f = Vec3.directionFromRotation(0, getYRot());
                double x0 = getX(), z0 = getZ();
                setPos(getX() + f.x * v, getY(), getZ() + f.z * v);
                andando = true;
                if (k >= CARGA_DESDE && k <= CARGA_HASTA) {
                    barrerCarga(x0, z0, getX(), getZ(), f);
                    if (segunda && k % 2 == 0) llama(getX() - f.x * 0.8, getZ() - f.z * 0.8);
                }
            }
            return;
        }
        // mientras se prepara sigue a su presa con el cuerpo; poco antes del golpe ya no gira (apartarse es esquivar)
        if (t != null && a != JineteAnims.ATURDIDO && a != JineteAnims.MORIR && a != JineteAnims.CUERNO && a != JineteAnims.BARRIDO
                && a != JineteAnims.APARECE && a != JineteAnims.DESAPARECE) {
            int[] g = JineteAnims.GOLPES[a];
            int fija = g.length == 0 ? 0 : g[g.length - 1] - 4;
            if (accionT < fija) girarHacia(t, accionT < 6 ? 10f : 4f);
        }
    }

    /** Peleando y sin hacer nada: se encara y se acerca al galope, o se aleja para cargar otra vez. */
    private void pelear(LivingEntity t) {
        double dx = t.getX() - getX(), dz = t.getZ() - getZ(), d = Math.sqrt(dx * dx + dz * dz);
        if (pausa <= 0 && Math.abs(t.getY() - getY()) < alturaMaxima() && (d < 6 || getSensing().hasLineOfSight(t))) {
            int n = elegir(t, d);
            if (n != 0) {
                if (n != JineteAnims.CARGA) girarHacia(t, 40f);
                empezar(n);
                return;
            }
        }
        getLookControl().setLookAt(t, 30f, 30f);
        if (alejarse > 0 && d < 16) {
            // se va al galope en linea (un poco de lado) para dar la vuelta y cargar
            double l = Math.max(0.01, d);
            double ux = -dx / l, uz = -dz / l;
            andarHacia(getX() + (ux * 0.9 - uz * 0.45) * 12, getZ() + (uz * 0.9 + ux * 0.45) * 12, GALOPE, GIRO_PELEA);
        } else if (d > distanciaDeseada() + 0.6) {
            // se acerca: al galope de lejos, al trote cuando esta cerca; si va a cargar, se encara sin acercarse
            if (cargaEspera <= 0 && d >= 8.5 && d <= 34) girarHacia(t, GIRO_PELEA);
            else andarHacia(t.getX(), t.getZ(), d > 9 ? GALOPE : TROTE, GIRO_PELEA);
        } else girarHacia(t, GIRO_PELEA);
    }

    /** Por su camino, al trote, hacia la fortaleza (y de vuelta si llega al final: no llega nunca a la escalera). */
    private void cabalgar() {
        if (ruta == null) return;
        double largo = ruta.largo();
        double lejos = Jinetes.distancia(ruta, getX(), getZ());
        s = Jinetes.donde(ruta, getX(), getZ());
        if (s > largo - 40) sentido = -1;
        else if (s < 20) sentido = 1;
        double[] p = ruta.punto(Mth.clamp(s + sentido * (lejos > 12 ? 4 : 9), 0, largo));
        andarHacia(p[0], p[1], TROTE, lejos > 12 ? GIRO_PELEA : GIRO);
    }

    /** Un paso hacia (x, z): gira como mucho {@code giro} grados y, si tiene que girar mucho, va mas despacio. */
    private void andarHacia(double x, double z, double paso, float giro) {
        double dx = x - getX(), dz = z - getZ(), d = Math.sqrt(dx * dx + dz * dz);
        if (d < 0.05) return;
        float quiere = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f;
        float y = Mth.approachDegrees(getYRot(), quiere, giro);
        setYRot(y);
        yBodyRot = yHeadRot = y;
        float dif = Math.abs(Mth.wrapDegrees(quiere - y));
        double k = dif > 90 ? 0.45 : dif > 45 ? 0.7 : 1.0;
        double l = Math.min(d, paso * k);
        Vec3 f = Vec3.directionFromRotation(0, y);
        setPos(getX() + f.x * l, getY(), getZ() + f.z * l);
        andando = l > 0.01;
    }

    /** Lo que se desvia su presa de hacia donde mira (grados, 0 = de frente). */
    private float angulo(LivingEntity t) {
        float quiere = (float) (Mth.atan2(t.getZ() - getZ(), t.getX() - getX()) * Mth.RAD_TO_DEG) - 90f;
        return Math.abs(Mth.wrapDegrees(quiere - getYRot()));
    }

    private int cuantosCerca(double r) {
        return level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(r, 2, r), e -> e != this && e.isAlive() && objetivoValido(e)).size();
    }

    // ------------------------------------------------------------------ los golpes

    /** El punto a {@code delante} bloques por delante y {@code derecha} a su derecha. */
    private Vec3 delante(double delante, double derecha) {
        Vec3 f = Vec3.directionFromRotation(0, getYRot());
        return new Vec3(getX() + f.x * delante - f.z * derecha, getY(), getZ() + f.z * delante + f.x * derecha);
    }

    /** La carga: a todo lo que este en la franja que acaba de cruzar (una vez por carga). */
    private void barrerCarga(double x0, double z0, double x1, double z1, Vec3 f) {
        double r = CARGA_LADO + 1.2;
        AABB caja = new AABB(Math.min(x0, x1) - r, getY() - 1.5, Math.min(z0, z1) - r, Math.max(x0, x1) + r, getY() + 3.5, Math.max(z0, z1) + r);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5f;
        for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, caja, e -> e != this && e.isAlive() && objetivoValido(e))) {
            if (tocadosCarga.contains(le.getId())) continue;
            // lo que se aparta del segmento que ha cruzado este tick (en horizontal)
            double sx = x1 - x0, sz = z1 - z0, l2 = sx * sx + sz * sz;
            double u = l2 < 1e-9 ? 0 : Mth.clamp(((le.getX() - x0) * sx + (le.getZ() - z0) * sz) / l2, 0, 1);
            double px = x0 + sx * u - le.getX(), pz = z0 + sz * u - le.getZ();
            if (px * px + pz * pz > (CARGA_LADO + le.getBbWidth() * 0.5) * (CARGA_LADO + le.getBbWidth() * 0.5)) continue;
            if (le.getY() > getY() + getBbHeight() || le.getY() + le.getBbHeight() < getY() - 0.8) continue;
            tocadosCarga.add(le.getId());
            if (!le.hurt(damageSources().mobAttack(this), dano)) continue;
            golpesCarga++;
            playSound(DarkCraftMod.SFX_JINETE_PISOTEA.get(), 1.6f, 1.2f);
            empujarEnElCamino(le, 1.3, f.x, f.z, 0.42);
            setLastHurtMob(le);
            if (level() instanceof ServerLevel sl)
                sl.sendParticles(ParticleTypes.CRIT, le.getX(), le.getY() + 1.0, le.getZ(), 14, 0.4, 0.5, 0.4, 0.3);
        }
    }

    /** El pisoton: la onda delante de las manos del caballo (a los que esten de pie). */
    private void pisoton() {
        pisotones++;
        Vec3 p = delante(PISOTON_DELANTE, 0);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, getY() + 0.2, p.z, 40, R_PISOTON * 0.45, 0.05, R_PISOTON * 0.45, 0.04);
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, p.x, getY() + 0.3, p.z, 14, R_PISOTON * 0.4, 0.1, R_PISOTON * 0.4, 0.02);
            sl.sendParticles(ParticleTypes.EXPLOSION, p.x, getY() + 0.4, p.z, 1, 0, 0, 0, 0);
        }
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.3f;
        for (LivingEntity le : cerca(p, R_PISOTON)) {
            if (!le.onGround() && le.getY() > getY() + 0.6) continue;
            if (le.hurt(damageSources().mobAttack(this), dano)) {
                ondasPisoton++;
                empujarEnElCamino(le, 0.9, le.getX() - getX(), le.getZ() - getZ(), 0.4);
            }
        }
    }

    /** El cuerno: hiela la sangre de los de cerca (van mas despacio un rato); el aro de almas. */
    private void cuerno() {
        cuernos++;
        if (level() instanceof ServerLevel sl) {
            for (int k = 0; k < 36; k++) {
                double a = Math.PI * 2 * k / 36;
                sl.sendParticles(ParticleTypes.SOUL, getX() + Math.cos(a) * 3.0, getY() + 1.0, getZ() + Math.sin(a) * 3.0, 1, 0, 0.1, 0, 0.02);
                sl.sendParticles(ParticleTypes.SNOWFLAKE, getX() + Math.cos(a) * 5.0, getY() + 1.2, getZ() + Math.sin(a) * 5.0, 1, 0, 0.05, 0, 0.02);
            }
        }
        for (LivingEntity le : cerca(position(), R_CUERNO)) {
            helados++;
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 1), this);
            le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 70, 0), this);
        }
    }

    /** Una llama de almas en (x, z), a ras del camino (a media vida). */
    private void llama(double x, double z) {
        for (double[] q : fuegos) if ((q[0] - x) * (q[0] - x) + (q[2] - z) * (q[2] - z) < 0.8 * 0.8) return;
        fuegos.add(new double[]{x, getY(), z, tickCount + FUEGO_DURA});
        fuegosPuestos++;
    }

    /** Las llamas de almas: arden un rato y queman a quien las pisa (cada medio segundo). */
    private void arderFuegos() {
        if (fuegos.isEmpty()) return;
        fuegos.removeIf(q -> tickCount >= q[3]);
        ServerLevel sl = level() instanceof ServerLevel l ? l : null;
        for (double[] q : fuegos) {
            if (sl != null && (tickCount + (int) q[0]) % 4 == 0)
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, q[0], q[1] + 0.15, q[2], 2, 0.3, 0.08, 0.3, 0.01);
            if (tickCount % 10 != 0) continue;
            for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, new AABB(q[0] - R_FUEGO, q[1] - 0.5, q[2] - R_FUEGO, q[0] + R_FUEGO, q[1] + 1.8, q[2] + R_FUEGO),
                    e -> e != this && e.isAlive() && objetivoValido(e))) {
                if (le.hurt(damageSources().inFire(), FUEGO_DANO)) {
                    quemados++;
                    le.setSecondsOnFire(2);
                }
            }
        }
    }

    /**
     * Empuja a alguien: en el camino de nubes, a lo largo del camino (en el sentido del empujon), nunca fuera de el (seria caer
     * al vacio); en otro sitio, hacia donde va el empujon. {@code arriba}: lo que le levanta.
     */
    private void empujarEnElCamino(LivingEntity le, double fuerza, double dx, double dz, double arriba) {
        double l = Math.sqrt(dx * dx + dz * dz);
        if (l < 1e-6) {
            Vec3 f = Vec3.directionFromRotation(0, getYRot());
            dx = f.x;
            dz = f.z;
            l = 1;
        }
        dx /= l;
        dz /= l;
        if (ruta != null && Jinetes.distancia(ruta, le.getX(), le.getZ()) < 3.5) {
            double[] tg = Jinetes.tangente(ruta, Jinetes.donde(ruta, le.getX(), le.getZ()));
            double dot = dx * tg[0] + dz * tg[1];
            if (Math.abs(dot) < 0.999) fueraDelCamino++;
            double sg = dot >= 0 ? 1 : -1;
            dx = tg[0] * sg;
            dz = tg[1] * sg;
        }
        le.knockback(fuerza, -dx, -dz);
        le.setDeltaMovement(le.getDeltaMovement().add(0, arriba, 0));
        le.hurtMarked = true;
    }

    /** Los que estan a {@code r} de un punto (en horizontal) y se les puede pegar. */
    private List<LivingEntity> cerca(Vec3 p, double r) {
        return level().getEntitiesOfClass(LivingEntity.class, new AABB(p.x - r - 1, getY() - 2, p.z - r - 1, p.x + r + 1, getY() + 4, p.z + r + 1),
                e -> e != this && e.isAlive() && objetivoValido(e) && e.distanceToSqr(p.x, e.getY(), p.z) <= r * r);
    }

    /** La estela de la guja: el arco que barre, a la altura de la silla. */
    private void estela(double arco, double r, double alto) {
        if (!(level() instanceof ServerLevel sl)) return;
        int n = (int) Math.max(8, arco / 14);
        for (int j = 0; j <= n; j++) {
            double ang = Math.toRadians(getYRot() + 90 - arco / 2 + arco * j / n);
            double x = getX() + Math.cos(ang) * r, z = getZ() + Math.sin(ang) * r;
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, x, getY() + alto, z, 1, 0, 0, 0, 0);
            if (j % 2 == 0) sl.sendParticles(ParticleTypes.SOUL, x, getY() + alto, z, 1, 0.1, 0.1, 0.1, 0.01);
        }
    }

    // ------------------------------------------------------------------ recibir y caer

    /** No repara en quien esta muy por debajo o por encima del camino (los de las islas de abajo no le importan). */
    @Override
    protected boolean percibe(LivingEntity p) {
        if (amanece || Math.abs(p.getY() - getY()) > 12) return false;
        return super.percibe(p);
    }

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        // el caballo de rodillas: un 30 % mas
        if (aturdido()) cuanto *= 1.3f;
        boolean ok = super.hurt(s, cuanto);
        if (ok && !level().isClientSide && isAlive() && s.getEntity() instanceof Player) {
            if (recibidoT <= 0) recibidoT = VENTANA;
            recibido += cuanto;
            int a = accion();
            if (recibido >= getMaxHealth() * AGUANTE && aturdidoEspera <= 0 && !aturdido() && a != JineteAnims.CUERNO && a != JineteAnims.CARGA
                    && a != JineteAnims.APARECE && a != JineteAnims.DESAPARECE && !amanece) {
                recibido = 0;
                recibidoT = 0;
                aturdidoEspera = ATURDIDO_ESPERA;
                aturdidos++;
                aturdir();
            }
        }
        return ok;
    }

    @Override
    public void die(DamageSource s) {
        boolean ya = !isAlive() && deathTime > 0;
        super.die(s);
        if (level().isClientSide || ya) return;
        fuegos.clear();
        habla(5);
        if (level() instanceof ServerLevel sl) Jinetes.caido(sl, this);
    }

    @Override
    protected void tickDeath() {
        if (!level().isClientSide && level() instanceof ServerLevel sl) {
            if (deathTime == 4) playSound(DarkCraftMod.SFX_JINETE_RELINCHO.get(), 2.6f, 0.8f);
            if (deathTime == 52) {
                playSound(DarkCraftMod.SFX_JINETE_PISOTEA.get(), 2.4f, 0.7f);
                sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 0.5, getZ(), 24, 1.2, 0.3, 1.2, 0.02);
            }
            // al final se deshace en almas
            if (deathTime >= JineteAnims.TICKS[JineteAnims.MORIR] - 20 && deathTime % 2 == 0)
                sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 0.8, getZ(), 4, 1.2, 0.5, 1.2, 0.03);
        }
        super.tickDeath();
    }
}
