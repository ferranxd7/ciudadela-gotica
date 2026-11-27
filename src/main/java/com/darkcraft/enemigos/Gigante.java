package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.Gigantes;
import com.darkcraft.morvain.Morvain;
import com.darkcraft.morvain.RutasGigantes;
import com.darkcraft.morvain.Terreno;
import com.darkcraft.net.Net;
import com.darkcraft.rey.Voces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Los gigantes errantes de Morvain (parte 7w; respuesta 132 del plan): los ultimos de los que levantaron el reino. Cada uno
 * anda sin descanso su vuelta de siempre ({@link RutasGigantes}; los saca y los recoge {@link Gigantes}, que sabe por donde va
 * cada uno aunque no le vea nadie). Unos ocho bloques de alto (el modelo, de {@code tools/enemigos/gigante.py}, a 1,8):
 * <ul>
 * <li><b>Ogmund, el de la Campana</b> ({@link #OGMUND}, las Llanuras Palidas): pega con la campana de su aldea atada al cabo de
 * una viga; dobla al andar.</li>
 * <li><b>Ulfgar, el de las Costillas</b> ({@link #ULFGAR}, la Costa de los Huesos): una costilla de los suyos.</li>
 * <li><b>Haldor, el de la Columna</b> ({@link #HALDOR}, la Capital en ruinas): una columna de marmol con su capitel.</li>
 * </ul>
 * No tienen fisica ({@code noPhysics}): andan derechos por el terreno de Morvain ({@link Terreno}: los pies a ras de suelo; en
 * otro mundo, el del mapa de alturas) y no se les empuja. Ven a quien tienen delante a 30 bloques; si su presa se aleja de su
 * camino mas de {@link #CORREA}, la dejan y vuelven a el.
 * <p>
 * Sus golpes: el <b>mazazo</b> (lo alza a dos manos y lo estrella delante: pega donde cae y la onda alrededor; despues se queda
 * clavado un momento), el <b>barrido</b> (de su derecha a su izquierda, bajo y ancho), el <b>pisoton</b> (al que esta a sus pies:
 * la onda alrededor del pie), la <b>patada</b> (al que se le mete entre los pies: lo lanza lejos), la <b>roca</b> (al que esta
 * lejos: la arranca del suelo y se la tira, {@link RocaGigante}) y el <b>rugido</b> (al empezar y a media vida: empuja a los de
 * cerca). Si se le pega mucho en poco tiempo <b>cae sobre una rodilla</b> (aturdido): su cabeza queda a tiro y los golpes le
 * hacen un 50 % mas.
 * <p>
 * Hablan (ElevenLabs, en ingles con subtitulos; {@link Voces#BASE_GIGANTE} + 3 x gigante + N): al empezar (no lo repiten en
 * 4 min), a media vida y al caer. Al caer: de rodillas, dicen su ultima frase y caen de bruces; donde caen queda su arma
 * ({@link Gigantes#monumento}). Lo demas (el cartel con sus ecos, la hazana y el bestiario) lo reparte
 * {@code valdrenn/Guardianes} como el de los demas jefes.
 */
public class Gigante extends Guardian {
    public static final int OGMUND = 0, ULFGAR = 1, HALDOR = 2;
    /** Su clave (bestiario, nombre, logro) por variante. */
    public static final String[] TIPOS = {"gigante_campana", "gigante_costilla", "gigante_columna"};
    public static final int ECOS = 12000;
    public static final int VOZ_BASE = Voces.BASE_GIGANTE;
    /** Lo que anda por su camino y persiguiendo (bloques por tick) y lo que gira (grados por tick). */
    public static final double PASO = 0.1, PASO_PELEA = 0.17;
    public static final float GIRO = 2.0f, GIRO_PELEA = 3.5f;
    /** Lo lejos de su camino que deja ir a su presa (y a si mismo) antes de volver. */
    public static final double CORREA = 48;
    /** El mazazo: a cuanto cae delante (y a su derecha), su radio de golpe y el de la onda. */
    public static final double MAZAZO_DELANTE = 6.0, MAZAZO_LADO = 0.8, R_MAZAZO = 2.6, R_ONDA_MAZAZO = 4.8;
    /** El pisoton: donde cae el pie (delante y a su derecha) y el radio de la onda. */
    public static final double PISOTON_DELANTE = 1.6, PISOTON_LADO = 1.0, R_PISOTON = 4.6;
    /** El rugido empuja a los que esten a este radio. */
    public static final double R_RUGIDO = 7.0;
    /** Lo que tiene que recibir en {@link #VENTANA} ticks (fraccion de su vida maxima) para caer sobre una rodilla. */
    public static final float AGUANTE = 0.07f;
    public static final int VENTANA = 120, ATURDIDO_ESPERA = 600;

    /** La ultima vez que dijo su frase de entrada cada uno (tiempo del mundo): no la repiten en cada intento. */
    private static final long[] ULTIMA_ENTRADA = {Long.MIN_VALUE / 2, Long.MIN_VALUE / 2, Long.MIN_VALUE / 2};

    private double s;
    private int rocaEspera = 60, pisotonEspera = 30, mazazoEspera = 20, barridoEspera = 10, patadaEspera = 0, campanaEspera = 40,
            cadenaEspera = 60, hablaHasta, esperaRugido, recibidoT, aturdidoEspera;
    private float recibido;
    private boolean andando;
    /** Para las pruebas: cuantas veces ha hecho cada cosa y a cuantos ha dado. */
    private int mazazos, golpesMazazo, ondasMazazo, barridos, pisotones, ondasPisoton, patadas, rocas, aturdidos, empujados;
    /** Para las pruebas: no elige golpes por su cuenta (lo que se le manda hacer, si). */
    public boolean quieta;
    /** Para las pruebas: no anda por su camino (se queda donde esta si no tiene presa). */
    public boolean parado;
    /** En el cliente: el ultimo pie que planto y el ultimo tick de accion visto (los pasos y los temblores de GiganteCliente). */
    public int pasoVisto = Integer.MIN_VALUE, golpeVisto = -1;

    public Gigante(EntityType<? extends Gigante> t, Level l) {
        super(t, l, TIPOS[0], BossEvent.BossBarColor.WHITE);
        noPhysics = true;
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 2200.0).add(Attributes.ATTACK_DAMAGE, 16.0).add(Attributes.ARMOR, 6.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FOLLOW_RANGE, 30.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    /** Pone quien es (y por donde anda): su nombre en la barra. */
    @Override
    public void variante(int v) {
        super.variante(Mth.clamp(v, 0, 2));
        barra.setName(Component.translatable("entity.darkcraft." + tipo()));
    }

    /** Lo que lleva andado de su vuelta. */
    public double s() {
        return s;
    }

    public void s(double v) {
        s = v;
    }

    @Override
    public String tipo() {
        return TIPOS[Mth.clamp(variante(), 0, 2)];
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable("entity.darkcraft." + tipo());
    }

    @Override
    public int musica() {
        return 42;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return tipo();
    }

    @Override
    protected int[] ticks() {
        return GiganteAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return GiganteAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return GiganteAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return GiganteAnims.MORIR;
    }

    @Override
    protected int accionRugido() {
        return GiganteAnims.RUGIDO;
    }

    @Override
    protected int accionLevantarse() {
        return 0;
    }

    @Override
    protected double distanciaDeseada() {
        return 3.0;
    }

    @Override
    protected double alturaMaxima() {
        return 9.0;
    }

    @Override
    protected double correa() {
        return CORREA;
    }

    // ------------------------------------------------------------------ cuerpo: ni fisica, ni empujones, ni se guarda

    @Override
    protected void registerGoals() {
        // ni perseguir con el navegador del juego (un cuerpo de ocho bloques no cabe en sus caminos) ni volver a un puesto:
        // eso lo hace el mismo (mover)
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

    /** Lo que se dibuja (el arma llega lejos): que no se deje de dibujar al salirse su caja de la pantalla. */
    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(8.0, 3.0, 8.0);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_GIGANTE_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DarkCraftMod.SFX_GIGANTE_MUERTE.get();
    }

    @Override
    protected float getSoundVolume() {
        return 3.0f;
    }

    @Override
    public float getVoicePitch() {
        return 0.85f + random.nextFloat() * 0.1f;
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return null;
    }

    // ------------------------------------------------------------------ para las pruebas

    public int mazazos() {
        return mazazos;
    }

    /** A cuantos ha dado el mazazo donde cae y a cuantos su onda. */
    public int golpesMazazo() {
        return golpesMazazo;
    }

    public int ondasMazazo() {
        return ondasMazazo;
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

    public int patadas() {
        return patadas;
    }

    /** Las rocas que ha tirado. */
    public int rocas() {
        return rocas;
    }

    /** Las veces que ha caido sobre una rodilla y a cuantos ha empujado su rugido. */
    public int aturdidos() {
        return aturdidos;
    }

    public int empujados() {
        return empujados;
    }

    /** Si esta andando (por su camino o detras de alguien). */
    public boolean andando() {
        return andando;
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

    // ------------------------------------------------------------------ que hace

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (quieta) return 0;
        double dy = presa.getY() - getY();
        // a sus pies: le pisa o le da una patada
        if (d < 4.4 && dy < 3.0) {
            if (pisotonEspera <= 0 && (patadaEspera > 0 || random.nextFloat() < 0.6f)) {
                pisotonEspera = (segunda ? 70 : 100) + random.nextInt(40);
                return GiganteAnims.PISOTON;
            }
            if (patadaEspera <= 0) {
                patadaEspera = 60 + random.nextInt(40);
                return GiganteAnims.PATADA;
            }
        }
        // a la distancia de su arma: el mazazo o el barrido
        if (d >= 3.2 && d <= 8.6 && Math.abs(dy) < 5.0) {
            if (mazazoEspera <= 0 && (barridoEspera > 0 || random.nextFloat() < 0.55f)) {
                mazazoEspera = (segunda ? 70 : 100) + random.nextInt(40);
                return GiganteAnims.MAZAZO;
            }
            if (barridoEspera <= 0) {
                barridoEspera = (segunda ? 50 : 70) + random.nextInt(30);
                return GiganteAnims.BARRIDO;
            }
        }
        // lejos: la roca
        if (d > 11 && d < 40 && rocaEspera <= 0 && hasLineOfSight(presa)) {
            rocaEspera = (segunda ? 130 : 180) + random.nextInt(60);
            return GiganteAnims.ROCA;
        }
        return 0;
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (accion) {
            case GiganteAnims.MAZAZO -> 18 + random.nextInt(14);
            case GiganteAnims.ROCA -> 24 + random.nextInt(16);
            case GiganteAnims.RUGIDO, GiganteAnims.ATURDIDO -> 12;
            default -> 10 + random.nextInt(14);
        };
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case GiganteAnims.BARRIDO -> new Golpe(9.0f, 200f, 1.15f, 1.8f);
            case GiganteAnims.PATADA -> new Golpe(5.6f, 110f, 0.9f, 3.2f);
            default -> null;
        };
    }

    /** El barrido pasa bajo y por fuera de sus pies: al que esta pegado a sus piernas no le llega. */
    @Override
    protected boolean pilla(int accion, int n, LivingEntity le) {
        if (accion == GiganteAnims.BARRIDO) {
            double dx = le.getX() - getX(), dz = le.getZ() - getZ();
            return dx * dx + dz * dz > 2.2 * 2.2;
        }
        return true;
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (accion == GiganteAnims.PATADA) {
            le.setDeltaMovement(le.getDeltaMovement().add(0, 0.55, 0));
            le.hurtMarked = true;
        }
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        int[] g = GiganteAnims.GOLPES[accion];
        switch (accion) {
            case GiganteAnims.MAZAZO -> {
                if (t == 8) playSound(DarkCraftMod.SFX_GIGANTE_CADENA.get(), 1.6f, 0.8f);
                if (t == g[0] - 6) playSound(DarkCraftMod.SFX_GIGANTE_BARRIDO.get(), 2.2f, 0.7f);
                if (t == g[0]) mazazo();
                // clavado en el suelo: el polvo que sigue cayendo
                if (t > g[0] && t < g[0] + 18 && t % 4 == 0 && level() instanceof ServerLevel sl) {
                    Vec3 p = delante(MAZAZO_DELANTE, MAZAZO_LADO);
                    sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, p.x, getY() + 0.3, p.z, 1, 0.8, 0.1, 0.8, 0.01);
                }
            }
            case GiganteAnims.BARRIDO -> {
                if (t == g[0] - 8) playSound(DarkCraftMod.SFX_GIGANTE_BARRIDO.get(), 2.6f, 0.85f + random.nextFloat() * 0.1f);
                if (t == g[0]) {
                    barridos++;
                    estela(200, 7.5, 1.0);
                }
            }
            case GiganteAnims.PISOTON -> {
                if (t == 6) playSound(DarkCraftMod.SFX_GIGANTE_CADENA.get(), 1.2f, 0.7f);
                if (t == g[0]) pisoton();
            }
            case GiganteAnims.PATADA -> {
                if (t == g[0] - 4) playSound(DarkCraftMod.SFX_GIGANTE_BARRIDO.get(), 1.8f, 1.2f);
                if (t == g[0]) {
                    patadas++;
                    playSound(DarkCraftMod.SFX_GIGANTE_PATADA.get(), 2.6f, 0.9f + random.nextFloat() * 0.1f);
                    polvo(delante(3.0, 1.0), 1.4, 10);
                }
            }
            case GiganteAnims.ROCA -> {
                if (t == g[0]) arrancar();
                if (t == g[0] + 10) playSound(DarkCraftMod.SFX_GIGANTE_CADENA.get(), 1.2f, 0.6f);
                if (t == g[1]) tirar();
            }
            case GiganteAnims.RUGIDO -> {
                if (t == 6) playSound(DarkCraftMod.SFX_GIGANTE_RUGIDO.get(), 4.0f, 0.95f);
                if (t == g[0]) rugir();
            }
            case GiganteAnims.ATURDIDO -> {
                if (t == 18 && level() instanceof ServerLevel sl) {
                    // la rodilla en el suelo
                    playSound(DarkCraftMod.SFX_GIGANTE_PISOTON.get(), 2.4f, 0.7f);
                    polvo(delante(0.6, -0.8), 2.0, 26);
                    sl.sendParticles(ParticleTypes.CRIT, getX(), getY() + 4.2, getZ(), 12, 0.6, 0.4, 0.6, 0.2);
                }
            }
            default -> {
            }
        }
    }

    @Override
    protected void alSegundaMitad() {
        hablaSiCalla(2);
        rocaEspera = Math.min(rocaEspera, 40);
    }

    @Override
    protected void alEmpezarPelea() {
        rocaEspera = 40;
        pisotonEspera = 20;
        mazazoEspera = 10;
        barridoEspera = 0;
        long ahora = level().getGameTime();
        int v = Mth.clamp(variante(), 0, 2);
        if (ahora - ULTIMA_ENTRADA[v] > 20 * 60 * 4 || ahora < ULTIMA_ENTRADA[v]) {
            ULTIMA_ENTRADA[v] = ahora;
            habla(1);
            // ruge al acabar de hablar (entretanto se le acerca, pero no pega)
            int dura = Voces.ticks(VOZ_BASE + v * 3 + 1);
            esperaRugido = tickCount + dura - 6;
            pausa = Math.max(pausa, dura);
        } else esperaRugido = 0;
    }

    @Override
    protected boolean puedeRugir() {
        return segunda || tickCount >= esperaRugido;
    }

    /** Dice su frase n (1 al empezar, 2 a media vida, 3 al caer) a quien este cerca (voz y subtitulo). */
    public void habla(int n) {
        if (!(level() instanceof ServerLevel sl)) return;
        int linea = VOZ_BASE + Mth.clamp(variante(), 0, 2) * 3 + n;
        for (ServerPlayer p : sl.players()) if (p.distanceToSqr(this) < 72 * 72) Net.to(p, new Net.ReyHabla(getId(), linea));
        hablaHasta = tickCount + Voces.ticks(linea);
    }

    private void hablaSiCalla(int n) {
        if (tickCount >= hablaHasta + Voces.PAUSA) habla(n);
    }

    // ------------------------------------------------------------------ cada tick

    @Override
    public void aiStep() {
        // primero se mueve (lo que anda en este tick lo cuenta el paso de la animacion del juego, que va en travel)
        if (!level().isClientSide && isAlive()) mover();
        super.aiStep();
        if (level().isClientSide || !isAlive()) return;
        if (rocaEspera > 0) rocaEspera--;
        if (pisotonEspera > 0) pisotonEspera--;
        if (mazazoEspera > 0) mazazoEspera--;
        if (barridoEspera > 0) barridoEspera--;
        if (patadaEspera > 0) patadaEspera--;
        if (aturdidoEspera > 0) aturdidoEspera--;
        if (recibidoT > 0 && --recibidoT == 0) recibido = 0;
        // andando: la campana de Ogmund dobla y lo que lleva cruje
        if (andando && accion() == 0) {
            if (variante() == OGMUND && --campanaEspera <= 0) {
                campanaEspera = 150 + random.nextInt(90);
                playSound(DarkCraftMod.SFX_GIGANTE_CAMPANA.get(), 4.0f, 0.92f + random.nextFloat() * 0.08f);
            }
            if (--cadenaEspera <= 0) {
                cadenaEspera = 80 + random.nextInt(80);
                playSound(DarkCraftMod.SFX_GIGANTE_CADENA.get(), 1.0f, 0.6f + random.nextFloat() * 0.2f);
            }
        }
    }

    /** Por donde va: detras de su presa, de vuelta a su camino o por el. Los pies, siempre a ras de suelo. */
    private void mover() {
        setDeltaMovement(Vec3.ZERO);
        andando = false;
        LivingEntity t = getTarget();
        if (t != null && (!t.isAlive() || !objetivoValido(t) || suelta(t))) {
            setTarget(null);
            t = null;
        }
        int a = accion();
        if (a != 0) {
            // mientras se prepara sigue a su presa con el cuerpo; poco antes del golpe ya no gira (apartarse es esquivar)
            if (t != null && a != GiganteAnims.ATURDIDO && a != GiganteAnims.MORIR && a != GiganteAnims.RUGIDO) {
                int[] g = GiganteAnims.GOLPES[a];
                int fija = g.length == 0 ? 0 : g[g.length - 1] - 4;
                if (accionT < fija) girarHacia(t, accionT < 6 ? 8f : 3f);
            }
            pies();
            return;
        }
        if (muriendo() || !isAlive()) return;
        if (t != null) {
            double dx = t.getX() - getX(), dz = t.getZ() - getZ(), d = Math.sqrt(dx * dx + dz * dz);
            if (pausa <= 0 && Math.abs(t.getY() - getY()) < alturaMaxima() && getSensing().hasLineOfSight(t)) {
                int n = elegir(t, d);
                if (n != 0) {
                    girarHacia(t, 40f);
                    empezar(n);
                    pies();
                    return;
                }
            }
            getLookControl().setLookAt(t, 20f, 20f);
            // a quien esta lejos de su vuelta no le sigue (le tira rocas si llega): vuelve a ella y le mira
            int r = Mth.clamp(variante(), 0, 2);
            boolean sigue = !Morvain.es(level()) || RutasGigantes.distancia(r, t.getX(), t.getZ()) <= CORREA;
            if (sigue && d > distanciaDeseada()) andarHacia(t.getX(), t.getZ(), PASO_PELEA, GIRO_PELEA);
            else if (!sigue && RutasGigantes.distancia(r, getX(), getZ()) > 6) {
                double[] p = RutasGigantes.punto(r, RutasGigantes.donde(r, getX(), getZ()));
                andarHacia(p[0], p[1], PASO_PELEA, GIRO_PELEA);
            } else girarHacia(t, GIRO_PELEA);
        } else if (!parado) {
            int r = Mth.clamp(variante(), 0, 2);
            // por su camino: hacia un punto un poco mas adelante (asi dobla las esquinas en curva)
            double lejos = RutasGigantes.distancia(r, getX(), getZ());
            s = RutasGigantes.donde(r, getX(), getZ());
            double[] p = RutasGigantes.punto(r, s + (lejos > 20 ? 4 : 14));
            andarHacia(p[0], p[1], PASO, lejos > 20 ? GIRO_PELEA : GIRO);
        }
        pies();
    }

    /** Si deja a su presa: muy lejos de el (a la que se aleja de su vuelta no la sigue, pero la tiene: ver mover). */
    private boolean suelta(LivingEntity t) {
        return distanceToSqr(t) > 64 * 64;
    }

    /** No repara en quien anda lejos de su vuelta (mas de la correa y 16): asi no la deja y la vuelve a ver a cada momento. */
    @Override
    protected boolean percibe(LivingEntity p) {
        if (Morvain.es(level()) && RutasGigantes.distancia(Mth.clamp(variante(), 0, 2), p.getX(), p.getZ()) > CORREA + 16) return false;
        return super.percibe(p);
    }

    /** Un paso hacia (x, z): gira como mucho {@code giro} grados y, si tiene que girar mucho, casi en el sitio. */
    private void andarHacia(double x, double z, double paso, float giro) {
        double dx = x - getX(), dz = z - getZ(), d = Math.sqrt(dx * dx + dz * dz);
        if (d < 0.05) return;
        float quiere = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f;
        float y = Mth.approachDegrees(getYRot(), quiere, giro);
        setYRot(y);
        yBodyRot = yHeadRot = y;
        float dif = Math.abs(Mth.wrapDegrees(quiere - y));
        double k = dif > 70 ? 0.2 : dif > 35 ? 0.55 : 1.0;
        double l = Math.min(d, paso * k);
        Vec3 f = Vec3.directionFromRotation(0, y);
        setPos(getX() + f.x * l, getY(), getZ() + f.z * l);
        andando = l > 0.01;
    }

    /** Los pies a ras de suelo: lo mas alto de lo que pisa (las cuatro esquinas de sus pies), sin saltos de golpe. */
    private void pies() {
        double h = suelo(level(), getX(), getZ(), getY());
        double y = getY() + Mth.clamp(h - getY(), -0.3, 0.3);
        if (Math.abs(y - getY()) > 1e-4) setPos(getX(), y, getZ());
    }

    /** El suelo bajo un gigante en (x, z): en Morvain el del terreno (no el de los arboles ni las ruinas); si no, el del mapa. */
    public static double suelo(Level l, double x, double z, double siNo) {
        double h = -1e9;
        for (int k = 0; k < 4; k++) {
            double px = x + ((k & 1) == 0 ? -1.1 : 1.1), pz = z + ((k & 2) == 0 ? -1.1 : 1.1);
            double y;
            if (Morvain.es(l)) y = Math.floor(Terreno.columna(px, pz).altura()) + 1;
            else {
                BlockPos b = BlockPos.containing(px, 0, pz);
                if (!l.hasChunkAt(b)) continue;
                y = l.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, b.getX(), b.getZ());
            }
            h = Math.max(h, y);
        }
        return h < -1e8 ? siNo : h;
    }

    // ------------------------------------------------------------------ los golpes

    /** El punto a {@code delante} bloques por delante y {@code derecha} a su derecha. */
    private Vec3 delante(double delante, double derecha) {
        Vec3 f = Vec3.directionFromRotation(0, getYRot());
        // su derecha: girar la mirada 90 grados hacia el sentido de las agujas del reloj visto desde arriba
        return new Vec3(getX() + f.x * delante - f.z * derecha, getY(), getZ() + f.z * delante + f.x * derecha);
    }

    /** El mazazo: donde cae el arma pega fuerte; alrededor, la onda (a los de pie); el polvo, las piedras y el temblor. */
    private void mazazo() {
        mazazos++;
        Vec3 p = delante(MAZAZO_DELANTE, MAZAZO_LADO);
        playSound(DarkCraftMod.SFX_GIGANTE_MAZAZO.get(), 4.0f, 0.9f + random.nextFloat() * 0.1f);
        if (variante() == OGMUND) playSound(DarkCraftMod.SFX_GIGANTE_CAMPANA.get(), 3.0f, 0.8f);
        polvo(p, R_ONDA_MAZAZO, 60);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (LivingEntity le : cerca(p, R_ONDA_MAZAZO)) {
            double d = Math.sqrt(le.distanceToSqr(p.x, le.getY(), p.z));
            if (d <= R_MAZAZO) {
                if (le.hurt(damageSources().mobAttack(this), dano * 1.6f)) {
                    golpesMazazo++;
                    le.knockback(1.2, p.x - le.getX(), p.z - le.getZ());
                    le.setDeltaMovement(le.getDeltaMovement().add(0, 0.4, 0));
                    le.hurtMarked = true;
                }
            } else if (le.onGround() && le.hurt(damageSources().mobAttack(this), dano * 0.7f)) {
                ondasMazazo++;
                le.knockback(0.7, p.x - le.getX(), p.z - le.getZ());
                le.setDeltaMovement(le.getDeltaMovement().add(0, 0.35, 0));
                le.hurtMarked = true;
            }
        }
    }

    /** El pisoton: la onda alrededor del pie (a los de pie). */
    private void pisoton() {
        pisotones++;
        Vec3 p = delante(PISOTON_DELANTE, PISOTON_LADO);
        playSound(DarkCraftMod.SFX_GIGANTE_PISOTON.get(), 4.0f, 0.9f + random.nextFloat() * 0.1f);
        polvo(p, R_PISOTON, 50);
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (LivingEntity le : cerca(p, R_PISOTON)) {
            if (!le.onGround()) continue;
            if (le.hurt(damageSources().mobAttack(this), dano * 1.25f)) {
                ondasPisoton++;
                le.knockback(1.0, p.x - le.getX(), p.z - le.getZ());
                le.setDeltaMovement(le.getDeltaMovement().add(0, 0.5, 0));
                le.hurtMarked = true;
            }
        }
    }

    /** El rugido: empuja a los que esten cerca (sin dano) y los deja un momento sin fuerzas. */
    private void rugir() {
        if (!(level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 6.0, getZ(), 30, 1.2, 0.6, 1.2, 0.08);
        polvo(position(), R_RUGIDO, 40);
        for (LivingEntity le : cerca(position(), R_RUGIDO)) {
            empujados++;
            le.knockback(1.4, getX() - le.getX(), getZ() - le.getZ());
            le.hurtMarked = true;
            le.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 60, 0), this);
        }
    }

    /** Arranca la roca del suelo (a su izquierda, delante): tierra, piedras y el crujido. */
    private void arrancar() {
        Vec3 p = delante(2.4, -1.4);
        playSound(DarkCraftMod.SFX_GIGANTE_ARRANCA.get(), 3.0f, 0.9f + random.nextFloat() * 0.1f);
        polvo(p, 1.8, 30);
    }

    /** Tira la roca a su presa (o delante, si ya no la tiene): sale de su mano, por encima de la cabeza. */
    private void tirar() {
        if (!(level() instanceof ServerLevel sl)) return;
        rocas++;
        Vec3 f = Vec3.directionFromRotation(0, getYRot());
        Vec3 o = new Vec3(getX() + f.x * 2.6 + f.z * 1.0, getY() + 6.6, getZ() + f.z * 2.6 - f.x * 1.0);
        LivingEntity p = getTarget();
        Vec3 blanco = p != null ? new Vec3(p.getX(), p.getY(), p.getZ()) : o.add(f.scale(18)).add(0, -6.6, 0);
        RocaGigante r = new RocaGigante(sl, this);
        r.setPos(o.x, o.y, o.z);
        r.setDeltaMovement(RocaGigante.tiro(o, blanco));
        sl.addFreshEntity(r);
        playSound(DarkCraftMod.SFX_GIGANTE_BARRIDO.get(), 2.4f, 1.1f);
    }

    /** Los que estan de pie (o no) a {@code r} de un punto del suelo y se les puede pegar. */
    private java.util.List<LivingEntity> cerca(Vec3 p, double r) {
        return level().getEntitiesOfClass(LivingEntity.class, new AABB(p.x - r - 1, getY() - 2, p.z - r - 1, p.x + r + 1, getY() + 4, p.z + r + 1),
                e -> e != this && e.isAlive() && objetivoValido(e) && e.distanceToSqr(p.x, e.getY(), p.z) <= r * r);
    }

    /** El golpe en el suelo: el polvo y las piedras de lo que hay debajo, el humo y el estallido. */
    private void polvo(Vec3 p, double r, int n) {
        if (!(level() instanceof ServerLevel sl)) return;
        double y = suelo(level(), p.x, p.z, getY());
        BlockState b = level().getBlockState(BlockPos.containing(p.x, y - 0.5, p.z));
        if (b.isAir()) b = Blocks.DIRT.defaultBlockState();
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, b), p.x, y + 0.2, p.z, n, r * 0.35, 0.1, r * 0.35, 0.3);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, p.x, y + 0.3, p.z, n / 3, r * 0.4, 0.15, r * 0.4, 0.02);
        sl.sendParticles(ParticleTypes.EXPLOSION, p.x, y + 0.4, p.z, r > 4 ? 3 : 1, r * 0.3, 0.1, r * 0.3, 0);
    }

    /** La estela del barrido: el arco que barre el arma, bajo. */
    private void estela(double arco, double r, double alto) {
        if (!(level() instanceof ServerLevel sl)) return;
        int n = (int) Math.max(8, arco / 12);
        for (int j = 0; j <= n; j++) {
            double ang = Math.toRadians(getYRot() + 90 - arco / 2 + arco * j / n);
            double x = getX() + Math.cos(ang) * r, z = getZ() + Math.sin(ang) * r;
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, x, getY() + alto, z, 1, 0, 0, 0, 0);
            if (j % 2 == 0) sl.sendParticles(ParticleTypes.CLOUD, x, getY() + 0.3, z, 1, 0.2, 0.05, 0.2, 0.02);
        }
    }

    // ------------------------------------------------------------------ recibir y caer

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        // sobre una rodilla, con la cabeza a tiro: un 50 % mas
        if (aturdido()) cuanto *= 1.5f;
        boolean ok = super.hurt(s, cuanto);
        if (ok && !level().isClientSide && isAlive() && s.getEntity() instanceof Player) {
            if (recibidoT <= 0) recibidoT = VENTANA;
            recibido += cuanto;
            if (recibido >= getMaxHealth() * AGUANTE && aturdidoEspera <= 0 && accion() != GiganteAnims.RUGIDO && !aturdido()) {
                recibido = 0;
                recibidoT = 0;
                aturdidoEspera = ATURDIDO_ESPERA;
                aturdidos++;
                aturdir();
            }
        }
        return ok;
    }

    /** Para las pruebas y el comando: que caiga ahora sobre una rodilla. */
    public void derribar() {
        aturdidos++;
        aturdidoEspera = ATURDIDO_ESPERA;
        aturdir();
    }

    @Override
    public void die(DamageSource s) {
        boolean ya = !isAlive() && deathTime > 0;
        super.die(s);
        if (level().isClientSide || ya) return;
        habla(3);
        if (level() instanceof ServerLevel sl) Gigantes.caido(sl, this);
    }

    @Override
    protected void tickDeath() {
        int m = GiganteAnims.MORIR;
        // de rodillas (1,6 s) y de bruces (7,4 s): el golpe contra el suelo
        if (!level().isClientSide && level() instanceof ServerLevel sl) {
            if (deathTime == 30) {
                playSound(DarkCraftMod.SFX_GIGANTE_PISOTON.get(), 3.0f, 0.75f);
                polvo(position(), 3.0, 30);
            }
            if (deathTime == 146) {
                playSound(DarkCraftMod.SFX_GIGANTE_MAZAZO.get(), 4.0f, 0.6f);
                polvo(delante(4.0, 0), 5.0, 70);
            }
            // donde cae, se queda su arma (antes de deshacerse)
            if (deathTime == GiganteAnims.TICKS[m] + 6) Gigantes.monumento(sl, Mth.clamp(variante(), 0, 2), getX(), getY(), getZ(), getYRot());
            if (deathTime >= GiganteAnims.TICKS[m] + 6 && deathTime % 2 == 0)
                sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY() + 1.0, getZ(), 2, 2.0, 0.5, 2.0, 0.01);
        }
        super.tickDeath();
    }
}
