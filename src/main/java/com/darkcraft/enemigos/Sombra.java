package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.EfectosRaiz;
import com.darkcraft.morvain.Aguja;
import com.darkcraft.morvain.Homenaje;
import com.darkcraft.net.Net;
import com.darkcraft.rey.Voces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
 * La Sombra sin Nombre (parte 7ac): el jefe final, encadenada en medio de la sala de la aguja de la ciudadela (el Corazon de la Aguja).
 * Hereda de {@link Guardian} (su barra, su musica —dos temas: uno por mitad—, la pelea). Mide unos siete bloques (el modelo va a 2,4).
 * <ul>
 * <li><b>Espera colgada de sus cadenas</b> ({@code REZA}: los brazos en cruz atados a los grilletes de los pilares). Quien entra en la
 * sala la despierta: la primera vez (a quien aun no la ha oido) dice su frase 26 mientras tira de las cadenas tres veces y al acabar las
 * rompe (a los 16,6 s); despues, la 33 ("¿Otra vez aqui? Bien.") y las rompe enseguida. Ruge y empieza la pelea; mientras pelea, la
 * puerta de la aguja queda sellada ({@link Aguja}).</li>
 * <li>Los <b>zarpazos</b> (dos), el <b>giro</b> (todo alrededor, empuja), el <b>latigazo de la cadena</b> (el eslabon de la muneca vuela
 * en linea hasta su presa: a quien pilla lo arrastra hacia ella), las <b>manos</b> (golpea el suelo: bajo los pies de cada uno sale una
 * mano de sombra un instante despues, que agarra y frena), las <b>lanzas</b> de sombra (tres en abanico; cinco con la furia) y la
 * <b>niebla</b> (se deshace y aparece a la espalda de su presa con los zarpazos).</li>
 * <li>A <b>media vida rompe la ultima cadena</b> (frase 27: "¡Basta! Eres MIO...") y se le abren mas ojos (variante 1); desde entonces
 * tambien la <b>mirada</b> (sube, carga y un rayo rojo barre la sala de un lado a otro: cerca de ella pasa por encima), el
 * <b>grito</b> (empuja a todos y lo oscurece todo) y <b>los que le fallaron</b> (frase 29 la primera vez: se alzan del suelo marcados
 * de sombra, que se deshacen al acabar).</li>
 * <li>Si cae su presa, la frase 28 ("Levantate, marcado. La muerte es mia...").</li>
 * <li><b>No muere de un golpe</b>: con la vida a cero <b>se rinde</b> ({@link #rendirse}): cae de rodillas en el aire, ofrece su corazon
 * (frase 30) y el corazon sale de su pecho y flota delante ({@code morvain.Corazon}). Lo demas (golpearlo o tomarlo) lo lleva
 * {@link Aguja}; al elegir, {@link #acabar} dice su ultima frase (31 o 32) y se deshace; entonces muere de verdad (el cartel, los ecos,
 * la hazana y el bestiario los reparte {@code valdrenn/Guardianes}).</li>
 * </ul>
 */
public class Sombra extends Guardian {
    private static final EntityDataAccessor<Boolean> RENDIDA = SynchedEntityData.defineId(Sombra.class, EntityDataSerializers.BOOLEAN);

    public static final int ECOS = 60000;
    /** La marca de la cadena (en la ficha): las frases que ya ha oido cada uno (la 26, el discurso largo al despertar). */
    public static final int BIT_DISCURSO = 32;
    /** Lo que espera entre un ataque especial y el siguiente del mismo (ticks). */
    public static final int ESPERA_CADENA = 20 * 6, ESPERA_MANOS = 20 * 10, ESPERA_LANZAS = 20 * 8, ESPERA_NIEBLA = 20 * 9,
            ESPERA_MIRADA = 20 * 15, ESPERA_GRITO = 20 * 13, ESPERA_CAIDOS = 20 * 34, ESPERA_GIRO = 20 * 4;
    /** El latigazo de la cadena: desde donde, hasta donde y lo ancho que pilla. */
    public static final double CADENA_MIN = 4.0, CADENA_MAX = 15.0, CADENA_ANCHO = 1.1;
    public static final int CADENA_APUNTA = 12;
    /** Las manos: cuando salen (desde el golpe al suelo) y su radio. */
    public static final int MANOS_SALEN = 26;
    public static final double R_MANO = 1.6;
    /** Las lanzas: cuantas, lo abiertas que van (grados) y su velocidad. */
    public static final float LANZA_ABRE = 11f, LANZA_VEL = 1.55f;
    /** La niebla: cuando se apaga y cuando aparece, y lo que se pone detras. */
    public static final int APAGA = 6, APARECE = 16;
    public static final double DETRAS = 2.8;
    /** La mirada: de que tick a que tick barre, lo que gira en total (grados), hasta donde llega en el suelo y lo que pilla. */
    public static final int MIRADA_DESDE = 31, MIRADA_HASTA = 61;
    public static final float MIRADA_GIRO = 140f;
    public static final double MIRADA_SUELO = 12.0, MIRADA_ANCHO = 1.0, OJOS = 6.5;
    /** El grito: su radio. */
    public static final double R_GRITO = 10.0;
    /** Cuando sale el corazon de su pecho (ticks desde que se rinde) y cuando lo ofrece (su frase 30). */
    public static final int OFRECE = 18, SALE_CORAZON = 62;
    /** Lo que tarda en irse al acabar (ticks): lo que dura su ultima frase y un poco mas. */
    public static final int VETE = 150;
    static final DustParticleOptions NEGRO = new DustParticleOptions(new Vector3f(0.04f, 0.02f, 0.05f), 1.8f),
            ROJO = new DustParticleOptions(new Vector3f(0.85f, 0.06f, 0.06f), 1.4f),
            ROJO_CHICO = new DustParticleOptions(new Vector3f(0.9f, 0.1f, 0.08f), 0.8f);

    private int esperaCadena, esperaManos, esperaLanzas, esperaNiebla, esperaMirada, esperaGrito, esperaCaidos, esperaGiro, hablaHasta;
    private boolean dijo28, dijo29, larga;
    private LivingEntity ultimaPresa;
    private Vec3 cadenaHasta;
    private float miradaRumbo;
    private int encadena;
    private final List<Vec3> marcas = new ArrayList<>();
    private final Set<Integer> tocadosEspecial = new HashSet<>();
    private final List<Marcado> caidos = new ArrayList<>();
    /** Desde cuando esta rendida (tick) y como acaba: 0 aun no, 1 golpeado el corazon, 2 tomado; y cuando empezo a irse. */
    private int rendidaDesde = -1, final_, acabaDesde = -1;
    private UUID quienEligio;
    /** Para la prueba: no elige ataques sola (solo hace lo que se le manda). */
    public boolean quieta;

    /** Para las pruebas: cuantas veces ha hecho cada cosa y a cuantos ha pillado. */
    public static int DESPIERTA, CORTA, ROMPE, ZARPAZOS, GIROS, CADENAS, ARRASTRA, MANOS, MANOS_PILLAN, LANZAS, NIEBLAS, DETRAS_SALE, MIRADAS,
            MIRADA_PILLA, GRITOS, GRITO_PILLA, CAIDOS, LEVANTADOS, FURIAS, RENDICIONES, FINALES;

    public Sombra(EntityType<? extends Sombra> t, Level l) {
        super(t, l, "sombra", BossEvent.BossBarColor.RED);
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 2800.0).add(Attributes.ATTACK_DAMAGE, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27).add(Attributes.FOLLOW_RANGE, 48.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, 8.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(RENDIDA, false);
    }

    @Override
    public String tipo() {
        return "sombra";
    }

    /** Su musica: un tema por mitad (47 hasta romper la ultima cadena, 48 despues). */
    @Override
    public int musica() {
        return furia() ? 48 : 47;
    }

    @Override
    public int ecos() {
        return ECOS;
    }

    @Override
    public String logro() {
        return "sombra";
    }

    @Override
    protected int[] ticks() {
        return SombraAnims.TICKS;
    }

    @Override
    protected int[][] golpes() {
        return SombraAnims.GOLPES;
    }

    @Override
    protected int accionAturdido() {
        return SombraAnims.ATURDIDO;
    }

    @Override
    protected int accionMorir() {
        return 0;
    }

    @Override
    protected int accionRugido() {
        return SombraAnims.RUGIDO;
    }

    @Override
    protected int accionLevantarse() {
        return SombraAnims.DESPIERTA_CORTA;
    }

    /** Si ya rompio la ultima cadena (media vida: mas ojos, la mirada, el grito y los caidos). */
    public boolean furia() {
        return variante() == 1;
    }

    /** Si se ha rendido (de rodillas, ofreciendo su corazon). */
    public boolean rendida() {
        return entityData.get(RENDIDA);
    }

    /** Desde cuando esta rendida (ticks de esta entidad; -1 si no). */
    public int rendidaDesde() {
        return rendidaDesde;
    }

    /**
     * Si sigue atada a sus cadenas (esperando, o mientras despierta, hasta que las rompe). Lo usa el dibujante (las cadenas del
     * cliente) con {@code tAccion}, los ticks de la accion que se ve.
     */
    public boolean encadenada(float tAccion, int accionVista) {
        if (rendida()) return false;
        if (accionVista == SombraAnims.DESPIERTA) return tAccion < SombraAnims.GOLPES[SombraAnims.DESPIERTA][0];
        if (accionVista == SombraAnims.DESPIERTA_CORTA) return tAccion < SombraAnims.GOLPES[SombraAnims.DESPIERTA_CORTA][0];
        return postura() == REZA && accionVista == 0;
    }

    /** Lo mismo en el servidor. */
    public boolean encadenada() {
        return encadenada(accionTicks(), accion());
    }

    /**
     * Los anclajes de sus cadenas (en el mundo): los grilletes de los pilares del este y del oeste (las munecas) y de los dos de detras
     * (al norte: la espalda). En el orden: muneca derecha, muneca izquierda, espalda derecha, espalda izquierda.
     */
    public static Vec3[] anclas() {
        int[] k = {4, 1, 3, 2};
        Vec3[] out = new Vec3[4];
        int y0 = Homenaje.pisa(Homenaje.AZOTEA);
        for (int i = 0; i < 4; i++) {
            double[] c = Homenaje.pilarAguja(k[i]);
            double ux = -c[0] / Homenaje.R_PILAR, uz = -c[1] / Homenaje.R_PILAR;
            int gx = (int) Math.floor(c[0] + ux * 1.6), gz = (int) Math.floor(c[1] + uz * 1.6);
            out[i] = new Vec3(gx + 0.5, y0 + Homenaje.GRILLETE + 0.5, gz + 0.5);
        }
        return out;
    }

    // ------------------------------------------------------------------ lo que dice

    /** Dice la frase n (sin sitio, a los que esten cerca); {@code ya}: corta lo que estuviera diciendo. */
    public void decir(int n, boolean ya) {
        if (!(level() instanceof ServerLevel sl)) return;
        for (ServerPlayer p : sl.players()) if (p.distanceToSqr(this) < 64 * 64) Net.to(p, new Net.SombraDice(ya ? n + Net.SombraDice.YA : n));
        hablaHasta = tickCount + Math.round(Voces.sombra(n) * 20);
        DarkCraftMod.LOGGER.info("[DarkCraft] la Sombra dice su frase {}", n);
    }

    private void decirSiCalla(int n) {
        if (tickCount >= hablaHasta + Voces.PAUSA) decir(n, false);
    }

    public boolean hablando() {
        return tickCount < hablaHasta;
    }

    // ------------------------------------------------------------------ la pelea

    @Override
    protected void alEmpezarPelea() {
        esperaCadena = 70;
        esperaManos = 120;
        esperaLanzas = 90;
        esperaNiebla = 160;
        esperaMirada = 60;
        esperaGrito = 120;
        esperaCaidos = 40;
        esperaGiro = 0;
        dijo28 = dijo29 = false;
        encadena = 0;
        marcas.clear();
    }

    @Override
    protected void alAcabarPelea() {
        deshacerCaidos();
        marcas.clear();
        // vuelve a ser la de antes (con sus dos ojos); al rato vuelve a colgar de sus cadenas
        variante(0);
    }

    /** A media vida: rompe la ultima cadena (frase 27) y se le abren los ojos. */
    @Override
    protected void alSegundaMitad() {
        hablaHasta = 0;
        decir(27, true);
        empezar(SombraAnims.FURIA);
        FURIAS++;
        esperaMirada = 40;
        esperaGrito = 90;
        esperaCaidos = 30;
        pausa = Math.max(pausa, 4);
    }

    @Override
    public void setTarget(LivingEntity t) {
        if (rendida() && t != null) return;
        super.setTarget(t);
        if (t != null && !level().isClientSide) ultimaPresa = t;
    }

    /** Solo repara en quien entra en su sala (a la vista). */
    @Override
    protected boolean percibe(LivingEntity p) {
        if (rendida() || !Homenaje.enAguja(p.getX(), p.getY() + 0.1, p.getZ())) return false;
        return distanceToSqr(p) < 40 * 40;
    }

    @Override
    protected double correa() {
        return 30.0;
    }

    /** Mientras despierta (colgada) no se mueve; despues, pegada a su presa. */
    @Override
    protected double distanciaDeseada() {
        return 2.8;
    }

    @Override
    protected double alturaMaxima() {
        return 6.0;
    }

    @Override
    protected double prisa() {
        return furia() ? 1.35 : 1.2;
    }

    @Override
    protected int pausaTras(int accion) {
        return switch (accion) {
            case SombraAnims.RUGIDO, SombraAnims.DESPIERTA, SombraAnims.DESPIERTA_CORTA -> 4;
            case SombraAnims.NIEBLA -> 0;
            case SombraAnims.FURIA -> 6;
            case SombraAnims.MIRADA, SombraAnims.CAIDOS, SombraAnims.GRITO -> 8 + random.nextInt(6);
            default -> (furia() ? 5 : 9) + random.nextInt(8);
        };
    }

    /** Colgada de sus cadenas: despierta despacio (y lo dice) la primera vez; si ya la habian oido, enseguida. */
    @Override
    protected int accionDespertar(int postura) {
        larga = false;
        if (level() instanceof ServerLevel sl)
            for (ServerPlayer p : sl.players())
                if (p.isAlive() && !p.isSpectator() && Homenaje.enAguja(p.getX(), p.getY() + 0.1, p.getZ())
                        && (com.darkcraft.rol.Ficha.dialogo(p, Aguja.MARCA) & BIT_DISCURSO) == 0) larga = true;
        return larga ? SombraAnims.DESPIERTA : SombraAnims.DESPIERTA_CORTA;
    }

    @Override
    protected int elegir(LivingEntity presa, double d) {
        if (quieta) return 0;
        if (furia()) {
            if (esperaCaidos <= 0 && random.nextFloat() < 0.4f) return SombraAnims.CAIDOS;
            if (esperaMirada <= 0 && d > 4.5 && d < 20 && random.nextFloat() < 0.5f) return SombraAnims.MIRADA;
            if (esperaGrito <= 0 && (cuantosCerca(6.0) >= 2 || d < 4.0) && random.nextFloat() < 0.45f) return SombraAnims.GRITO;
        }
        if (esperaManos <= 0 && d < 22 && random.nextFloat() < 0.3f) return SombraAnims.MANOS;
        if (esperaCadena <= 0 && d > CADENA_MIN && d < CADENA_MAX - 1 && random.nextFloat() < 0.55f) return SombraAnims.CADENA;
        if (esperaLanzas <= 0 && d > 5.5 && d < 24 && random.nextFloat() < 0.5f) return SombraAnims.LANZAS;
        if (esperaNiebla <= 0 && (d > 10 || random.nextFloat() < 0.12f)) return SombraAnims.NIEBLA;
        if (d < 4.6) {
            if (esperaGiro <= 0 && (cuantosCerca(5.0) >= 2 || random.nextFloat() < 0.3f)) return SombraAnims.GIRO;
            return SombraAnims.ZARPAZOS;
        }
        if (d < 5.2 && esperaGiro <= 0) return SombraAnims.GIRO;
        return 0;
    }

    private int cuantosCerca(double r) {
        int n = 0;
        if (level() instanceof ServerLevel sl)
            for (ServerPlayer p : sl.players()) if (objetivoValido(p) && p.distanceToSqr(this) < r * r) n++;
        return n;
    }

    @Override
    protected Golpe golpe(int accion, int n) {
        return switch (accion) {
            case SombraAnims.ZARPAZOS -> new Golpe(4.8f, 150f, 1.0f, 0.7f);
            case SombraAnims.GIRO -> new Golpe(5.4f, 360f, 1.15f, 1.8f);
            default -> null;
        };
    }

    @Override
    protected SoundEvent silbido(int accion) {
        return null;
    }

    @Override
    protected void alGolpear(LivingEntity le, int accion) {
        if (accion == SombraAnims.ZARPAZOS) ZARPAZOS++;
        if (accion == SombraAnims.GIRO) GIROS++;
    }

    @Override
    protected void alAvanzar(int accion, int t) {
        switch (accion) {
            case SombraAnims.DESPIERTA -> despierta(t, true);
            case SombraAnims.DESPIERTA_CORTA -> despierta(t, false);
            case SombraAnims.RUGIDO -> rugido(t);
            case SombraAnims.ZARPAZOS -> {
                if (t == 9 || t == 18) {
                    empujar(0.35);
                    playSound(DarkCraftMod.SFX_SOMBRA_ZARPAZO.get(), 1.4f, 0.85f + random.nextFloat() * 0.2f);
                }
            }
            case SombraAnims.GIRO -> {
                if (t == 2) esperaGiro = ESPERA_GIRO;
                if (t == 9) playSound(DarkCraftMod.SFX_SOMBRA_GIRO.get(), 1.6f, 0.9f);
                if (t >= 9 && t <= 17 && level() instanceof ServerLevel sl) {
                    double a = (t - 9) * Math.PI / 4;
                    for (int k = 0; k < 4; k++) {
                        double b = a + k * Math.PI / 2;
                        sl.sendParticles(NEGRO, getX() + Math.cos(b) * 3.6, getY() + 1.6, getZ() + Math.sin(b) * 3.6, 3, 0.3, 0.4, 0.3, 0.01);
                    }
                }
            }
            case SombraAnims.CADENA -> cadena(t);
            case SombraAnims.MANOS -> manos(t);
            case SombraAnims.LANZAS -> lanzas(t);
            case SombraAnims.NIEBLA -> niebla(t);
            case SombraAnims.MIRADA -> mirada(t);
            case SombraAnims.GRITO -> grito(t);
            case SombraAnims.CAIDOS -> caidos(t);
            case SombraAnims.FURIA -> furiaT(t);
            case SombraAnims.CAE -> cae(t);
            default -> {
            }
        }
    }

    /** Despierta: tira de sus cadenas (tres tirones la larga, uno la corta) y las rompe en su golpe. */
    private void despierta(int t, boolean largaT) {
        int rompe = SombraAnims.GOLPES[largaT ? SombraAnims.DESPIERTA : SombraAnims.DESPIERTA_CORTA][0];
        if (t == 1) {
            if (largaT) {
                DESPIERTA++;
                // el discurso, a todos los de la sala (y apuntado: la proxima vez, la corta)
                if (level() instanceof ServerLevel sl)
                    for (ServerPlayer p : sl.players())
                        if (p.distanceToSqr(this) < 48 * 48) {
                            int m = com.darkcraft.rol.Ficha.dialogo(p, Aguja.MARCA);
                            com.darkcraft.rol.Ficha.dialogo(p, Aguja.MARCA, m | BIT_DISCURSO);
                        }
                hablaHasta = 0;
                decir(26, true);
            } else {
                CORTA++;
                hablaHasta = 0;
                decir(33, true);
            }
        }
        // los tirones: crujen las cadenas (cada vez mas fuerte)
        int[] tirones = largaT ? new int[]{148, 232, 316} : new int[]{34};
        for (int k = 0; k < tirones.length; k++)
            if (t == tirones[k]) {
                playSound(DarkCraftMod.SFX_SOMBRA_CADENAS.get(), 1.4f + 0.4f * k, 0.95f - 0.05f * k);
                if (level() instanceof ServerLevel sl) for (Vec3 a : anclas()) sl.sendParticles(ParticleTypes.CRIT, a.x, a.y, a.z, 6 + 4 * k, 0.3, 0.3, 0.3, 0.1);
            }
        if (t == rompe) {
            ROMPE++;
            playSound(DarkCraftMod.SFX_SOMBRA_ROMPE.get(), 2.4f, 1.0f);
            if (level() instanceof ServerLevel sl) {
                Vec3 pecho = position().add(0, 5.0, 0);
                ItemParticleOption eslabon = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.CHAIN));
                for (Vec3 a : anclas()) {
                    // los eslabones que saltan a lo largo de cada cadena
                    for (int k = 0; k <= 10; k++) {
                        Vec3 p = pecho.lerp(a, k / 10.0);
                        sl.sendParticles(eslabon, p.x, p.y, p.z, 3, 0.2, 0.2, 0.2, 0.15);
                    }
                    sl.sendParticles(ParticleTypes.EXPLOSION, a.x, a.y, a.z, 1, 0, 0, 0, 0);
                }
                sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 3.0, getZ(), 40, 1.0, 2.0, 1.0, 0.05);
                sl.sendParticles(NEGRO, getX(), getY() + 3.0, getZ(), 60, 1.4, 2.4, 1.4, 0.03);
            }
        }
        // mientras dice su frase no se mueve de donde cuelga
        setDeltaMovement(0, getDeltaMovement().y, 0);
    }

    /** El rugido (al empezar la pelea): la onda empuja a quien este cerca. */
    private void rugido(int t) {
        if (t == 8) playSound(DarkCraftMod.SFX_SOMBRA_RUGIDO.get(), 2.6f, 0.95f + random.nextFloat() * 0.1f);
        if (t == 13 && level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SONIC_BOOM, getX(), getY() + 4.0, getZ(), 1, 0, 0, 0, 0);
            for (ServerPlayer p : sl.players()) {
                if (!objetivoValido(p) || p.distanceToSqr(this) > 6 * 6) continue;
                Vec3 d = p.position().subtract(position());
                double l = Math.max(0.1, Math.sqrt(d.x * d.x + d.z * d.z));
                p.knockback(1.1, -d.x / l, -d.z / l);
                p.hurtMarked = true;
            }
        }
    }

    /** El latigazo de la cadena: apunta (a donde esta su presa) y el eslabon vuela en linea; a quien pilla lo arrastra hacia ella. */
    private void cadena(int t) {
        if (!(level() instanceof ServerLevel sl)) return;
        LivingEntity p = getTarget();
        if (t == 2) {
            esperaCadena = ESPERA_CADENA;
            CADENAS++;
            tocadosEspecial.clear();
        }
        if (t == CADENA_APUNTA) {
            Vec3 o = position().add(0, 2.2, 0);
            Vec3 hacia = p != null ? p.position().add(0, 1.0, 0) : o.add(Vec3.directionFromRotation(0, getYRot()).scale(CADENA_MAX));
            Vec3 d = hacia.subtract(o);
            double l = d.length();
            cadenaHasta = l < 0.1 ? o : o.add(d.scale(Math.max(l + 2.0, CADENA_MAX * 0.8) / l));
            playSound(DarkCraftMod.SFX_SOMBRA_CADENA.get(), 1.6f, 1.0f);
        }
        if (t >= SombraAnims.GOLPES[SombraAnims.CADENA][0] && t <= SombraAnims.GOLPES[SombraAnims.CADENA][0] + 3 && cadenaHasta != null) {
            Vec3 o = position().add(0, 2.2, 0);
            int paso = t - SombraAnims.GOLPES[SombraAnims.CADENA][0];
            Vec3 hasta = o.lerp(cadenaHasta, Math.min(1.0, (paso + 1) / 3.0));
            ItemParticleOption eslabon = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.CHAIN));
            int n = (int) Math.ceil(o.distanceTo(hasta) * 2);
            for (int k = 0; k <= n; k++) {
                Vec3 q = o.lerp(hasta, k / (double) Math.max(1, n));
                sl.sendParticles(eslabon, q.x, q.y, q.z, 1, 0.03, 0.03, 0.03, 0.0);
                if (k % 3 == 0) sl.sendParticles(NEGRO, q.x, q.y, q.z, 1, 0.05, 0.05, 0.05, 0.0);
            }
            for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, new AABB(o, hasta).inflate(CADENA_ANCHO + 1), e -> e != this && e.isAlive() && objetivoValido(e))) {
                if (tocadosEspecial.contains(le.getId())) continue;
                if (distanciaASegmento(le.position().add(0, le.getBbHeight() * 0.5, 0), o, hasta) > CADENA_ANCHO) continue;
                tocadosEspecial.add(le.getId());
                if (!le.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.8f)) continue;
                ARRASTRA++;
                // lo arrastra hacia ella (hasta quedar delante)
                Vec3 v = position().subtract(le.position());
                double lv = Math.sqrt(v.x * v.x + v.z * v.z);
                if (lv > 2.5) le.setDeltaMovement(v.x / lv * Math.min(1.6, lv * 0.16), 0.32, v.z / lv * Math.min(1.6, lv * 0.16));
                le.hurtMarked = true;
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 2, false, false));
                sl.sendParticles(ParticleTypes.CRIT, le.getX(), le.getY() + 1.0, le.getZ(), 10, 0.3, 0.4, 0.3, 0.2);
            }
        }
    }

    /** La distancia de un punto a un segmento. */
    public static double distanciaASegmento(Vec3 p, Vec3 a, Vec3 b) {
        Vec3 ab = b.subtract(a);
        double l2 = ab.lengthSqr();
        if (l2 < 1e-6) return p.distanceTo(a);
        double t = Mth.clamp(p.subtract(a).dot(ab) / l2, 0.0, 1.0);
        return p.distanceTo(a.add(ab.scale(t)));
    }

    /** Las manos: golpea el suelo; bajo los pies de cada uno (y por donde va, con la furia) sale una mano de sombra que agarra. */
    private void manos(int t) {
        if (!(level() instanceof ServerLevel sl)) return;
        int golpe = SombraAnims.GOLPES[SombraAnims.MANOS][0];
        if (t == 2) {
            esperaManos = ESPERA_MANOS;
            MANOS++;
        }
        if (t == golpe) {
            playSound(DarkCraftMod.SFX_SOMBRA_MANOS.get(), 1.8f, 0.9f);
            sl.sendParticles(ParticleTypes.SQUID_INK, getX(), getY() + 0.3, getZ(), 30, 1.2, 0.1, 1.2, 0.08);
            marcas.clear();
            for (ServerPlayer p : sl.players()) {
                if (!objetivoValido(p) || p.distanceToSqr(this) > 26 * 26 || !Homenaje.enAguja(p.getX(), p.getY() + 0.1, p.getZ())) continue;
                marcas.add(new Vec3(p.getX(), Math.floor(p.getY() + 0.01), p.getZ()));
                Vec3 v = p.getDeltaMovement();
                if (furia() && v.x * v.x + v.z * v.z > 0.004)
                    marcas.add(new Vec3(p.getX() + v.x * 10, Math.floor(p.getY() + 0.01), p.getZ() + v.z * 10));
            }
            LivingEntity q = getTarget();
            if (q != null && !(q instanceof Player)) marcas.add(new Vec3(q.getX(), Math.floor(q.getY() + 0.01), q.getZ()));
        }
        if (t > golpe && t < golpe + MANOS_SALEN) {
            double r = R_MANO * (1.0 - (t - golpe) / (double) MANOS_SALEN * 0.5);
            if (t % 2 == 0)
                for (Vec3 m : marcas)
                    for (int k = 0; k < 8; k++) {
                        double a = k * Math.PI / 4 + t * 0.2;
                        sl.sendParticles(NEGRO, m.x + Math.cos(a) * r, m.y + 0.1, m.z + Math.sin(a) * r, 1, 0, 0, 0, 0);
                        if (k % 2 == 0) sl.sendParticles(ROJO_CHICO, m.x + Math.cos(a) * r * 0.5, m.y + 0.1, m.z + Math.sin(a) * r * 0.5, 1, 0, 0, 0, 0);
                    }
        }
        if (t == golpe + MANOS_SALEN) {
            float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.85f;
            BlockState sombra = negro();
            for (Vec3 m : marcas) {
                // los dedos: cinco estacas de sombra que salen del suelo en corro y se cierran
                for (int k = 0; k < 5; k++) {
                    double a = k * Math.PI * 2 / 5 + random.nextDouble() * 0.3;
                    double x = m.x + Math.cos(a) * 1.0, z = m.z + Math.sin(a) * 1.0;
                    float yaw = EfectosRaiz.yaw(m.x - x, m.z - z);
                    EfectosRaiz.estaca(sl, sombra, x, m.y, z, yaw, 28f, 2.4f, 0.32f, k, 22);
                }
                sl.sendParticles(ParticleTypes.SOUL, m.x, m.y + 0.5, m.z, 8, 0.5, 0.4, 0.5, 0.03);
                sl.playSound(null, m.x, m.y, m.z, DarkCraftMod.SFX_SOMBRA_MANOS.get(), SoundSource.HOSTILE, 1.0f, 1.2f);
                for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, new AABB(m.x - 3, m.y - 1, m.z - 3, m.x + 3, m.y + 3, m.z + 3),
                        e -> e != this && e.isAlive() && objetivoValido(e))) {
                    if (le.distanceToSqr(m.x, le.getY(), m.z) > R_MANO * R_MANO || le.getY() > m.y + 1.6) continue;
                    if (le.hurt(damageSources().mobAttack(this), dano)) {
                        MANOS_PILLAN++;
                        le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 36, 5, false, false));
                        le.setDeltaMovement(0, Math.min(0, le.getDeltaMovement().y), 0);
                        le.hurtMarked = true;
                    }
                }
            }
            marcas.clear();
        }
    }

    /** El bloque de las manos de sombra (la piedra de sombra del mod si esta; si no, hormigon negro). */
    private static BlockState negro() {
        try {
            return DarkCraftMod.PIEDRA_SOMBRA.get().defaultBlockState();
        } catch (Throwable t) {
            return Blocks.BLACK_CONCRETE.defaultBlockState();
        }
    }

    /** Las lanzas: forma las lanzas de sombra sobre la garra y las abre en abanico hacia su presa. */
    private void lanzas(int t) {
        if (!(level() instanceof ServerLevel sl)) return;
        if (t == 2) {
            esperaLanzas = ESPERA_LANZAS;
            LANZAS++;
        }
        if (t == 9) playSound(DarkCraftMod.SFX_SOMBRA_LANZAS.get(), 1.5f, 1.1f);
        if (t >= 6 && t < SombraAnims.GOLPES[SombraAnims.LANZAS][0])
            sl.sendParticles(NEGRO, getX(), getY() + 6.2, getZ(), 4, 0.6, 0.3, 0.6, 0.02);
        if (t == SombraAnims.GOLPES[SombraAnims.LANZAS][0]) {
            LivingEntity p = getTarget();
            Vec3 o = position().add(0, 5.4, 0);
            Vec3 hacia = p != null ? p.position().add(0, p.getBbHeight() * 0.5, 0) : o.add(Vec3.directionFromRotation(0, getYRot()).scale(10));
            Vec3 d = hacia.subtract(o).normalize();
            int n = furia() ? 5 : 3;
            float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6f;
            for (int k = 0; k < n; k++) {
                float giro = (k - (n - 1) / 2f) * LANZA_ABRE;
                Vec3 v = d.yRot((float) Math.toRadians(giro));
                LanzaSombra l = new LanzaSombra(level(), this, dano);
                l.setPos(o.x + v.x * 1.2, o.y + v.y * 1.2, o.z + v.z * 1.2);
                l.shoot(v.x, v.y, v.z, LANZA_VEL, 0.6f);
                sl.addFreshEntity(l);
            }
            playSound(DarkCraftMod.SFX_SOMBRA_LANZAS.get(), 1.8f, 0.85f);
        }
    }

    /** La niebla: se apaga (no se le puede pegar), aparece a la espalda de su presa y le echa las garras. */
    private void niebla(int t) {
        LivingEntity p = getTarget();
        if (t == 1) {
            esperaNiebla = ESPERA_NIEBLA;
            NIEBLAS++;
        }
        if (t == APAGA) {
            playSound(DarkCraftMod.SFX_SOMBRA_NIEBLA.get(), 1.6f, 1.0f);
            if (level() instanceof ServerLevel sl) sl.sendParticles(NEGRO, getX(), getY() + 3.0, getZ(), 60, 1.0, 2.2, 1.0, 0.03);
        }
        if (t == APARECE && p != null) {
            Vec3 m = p.getViewVector(1f);
            Vec3 h = new Vec3(m.x, 0, m.z);
            if (h.lengthSqr() < 1e-4) h = new Vec3(0, 0, 1);
            h = h.normalize();
            Vec3 detras = p.position().subtract(h.scale(DETRAS));
            Vec3 donde = libre(detras) ? detras : libre(p.position().add(h.scale(DETRAS))) ? p.position().add(h.scale(DETRAS)) : null;
            if (donde != null) {
                moveTo(donde.x, donde.y, donde.z, getYRot(), 0);
                fijarRumbo(girarHacia(p, 360f));
                DETRAS_SALE++;
            }
            if (level() instanceof ServerLevel sl) sl.sendParticles(NEGRO, getX(), getY() + 3.0, getZ(), 60, 1.0, 2.2, 1.0, 0.03);
            encadena = SombraAnims.ZARPAZOS;
        }
    }

    /** Si ahi cabe (sin bloques que choquen) y hay suelo debajo, dentro de la sala. */
    private boolean libre(Vec3 v) {
        if (!Homenaje.enAguja(v.x, v.y + 0.2, v.z) || Math.hypot(v.x, v.z) > Homenaje.R_AGUJA - 1.6) return false;
        AABB c = getType().getDimensions().makeBoundingBox(v.x, v.y, v.z);
        BlockPos bajo = BlockPos.containing(v.x, v.y - 0.5, v.z);
        return level().noCollision(this, c) && !level().getBlockState(bajo).getCollisionShape(level(), bajo).isEmpty();
    }

    /** Hacia donde mira el rayo de su mirada en el tick t de la accion (grados, como getYRot). */
    public static float rumboMirada(float rumbo0, float t) {
        float f = Mth.clamp((t - MIRADA_DESDE) / (float) (MIRADA_HASTA - MIRADA_DESDE), 0f, 1f);
        return rumbo0 - MIRADA_GIRO / 2f + MIRADA_GIRO * f;
    }

    /** Donde da el rayo en el suelo (desde los pies, con ese rumbo). */
    public static Vec3 rayoSuelo(Vec3 pies, float rumbo) {
        Vec3 d = Vec3.directionFromRotation(0, rumbo);
        return pies.add(d.x * MIRADA_SUELO, 0.05, d.z * MIRADA_SUELO);
    }

    /** La mirada: sube y carga (arden los ojos) y el rayo rojo barre la sala de un lado a otro; cerca de ella pasa por encima. */
    private void mirada(int t) {
        if (!(level() instanceof ServerLevel sl)) return;
        if (t == 1) {
            esperaMirada = ESPERA_MIRADA;
            MIRADAS++;
            tocadosEspecial.clear();
            playSound(DarkCraftMod.SFX_SOMBRA_MIRADA_CARGA.get(), 2.0f, 1.0f);
        }
        if (t < MIRADA_DESDE && t % 3 == 0) sl.sendParticles(ROJO, getX(), getY() + OJOS, getZ(), 3, 0.3, 0.2, 0.3, 0.02);
        if (t == MIRADA_DESDE) {
            miradaRumbo = getYRot();
            playSound(DarkCraftMod.SFX_SOMBRA_MIRADA.get(), 2.4f, 1.0f);
        }
        if (t >= MIRADA_DESDE && t <= MIRADA_HASTA) {
            float r = rumboMirada(miradaRumbo, t);
            Vec3 ojos = position().add(0, OJOS, 0), suelo = rayoSuelo(position(), r);
            sl.sendParticles(ParticleTypes.FLAME, suelo.x, suelo.y + 0.1, suelo.z, 4, 0.3, 0.05, 0.3, 0.02);
            sl.sendParticles(ROJO, suelo.x, suelo.y + 0.2, suelo.z, 3, 0.2, 0.1, 0.2, 0.0);
            float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.25f;
            for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(MIRADA_SUELO + 2, 4, MIRADA_SUELO + 2),
                    e -> e != this && e.isAlive() && objetivoValido(e))) {
                if (tocadosEspecial.contains(le.getId())) continue;
                // el cuerpo entero (los pies, el pecho y la cabeza) contra el rayo
                double dmin = Double.MAX_VALUE;
                for (double h : new double[]{0.2, le.getBbHeight() * 0.5, le.getBbHeight() * 0.9})
                    dmin = Math.min(dmin, distanciaASegmento(le.position().add(0, h, 0), ojos, suelo));
                if (dmin > MIRADA_ANCHO) continue;
                tocadosEspecial.add(le.getId());
                if (le.hurt(damageSources().indirectMagic(this, this), dano)) {
                    MIRADA_PILLA++;
                    le.setSecondsOnFire(3);
                }
            }
        }
    }

    /** El grito: abre los brazos y grita: empuja a todos los de alrededor y lo oscurece todo un momento. */
    private void grito(int t) {
        if (!(level() instanceof ServerLevel sl)) return;
        if (t == 2) {
            esperaGrito = ESPERA_GRITO;
            GRITOS++;
        }
        if (t == 12) playSound(DarkCraftMod.SFX_SOMBRA_GRITO.get(), 2.6f, 1.0f);
        if (t == SombraAnims.GOLPES[SombraAnims.GRITO][0]) {
            sl.sendParticles(ParticleTypes.SONIC_BOOM, getX(), getY() + 4.0, getZ(), 1, 0, 0, 0, 0);
            sl.sendParticles(NEGRO, getX(), getY() + 3.0, getZ(), 80, 3.0, 2.0, 3.0, 0.06);
            float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.55f;
            for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(R_GRITO, 4, R_GRITO), e -> e != this && e.isAlive() && objetivoValido(e))) {
                Vec3 d = le.position().subtract(position());
                double l = Math.sqrt(d.x * d.x + d.z * d.z);
                if (l > R_GRITO) continue;
                if (!le.hurt(damageSources().indirectMagic(this, this), dano)) continue;
                GRITO_PILLA++;
                l = Math.max(0.1, l);
                le.knockback(2.0 * (1.0 - l / (R_GRITO + 2)), -d.x / l, -d.z / l);
                le.hurtMarked = true;
                le.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 70, 0, false, false));
            }
        }
    }

    /** Los que le fallaron: alza las garras y se levantan del suelo los marcados de sombra (se deshacen al acabar la pelea). */
    private void caidos(int t) {
        if (!(level() instanceof ServerLevel sl)) return;
        if (t == 2) {
            esperaCaidos = ESPERA_CAIDOS;
            CAIDOS++;
            if (!dijo29) {
                dijo29 = true;
                hablaHasta = 0;
                decir(29, false);
            }
        }
        if (t == 16) playSound(DarkCraftMod.SFX_SOMBRA_CAIDOS.get(), 2.0f, 1.0f);
        if (t == SombraAnims.GOLPES[SombraAnims.CAIDOS][0]) {
            caidos.removeIf(e -> !e.isAlive() || e.isRemoved());
            int cerca = cuantosCerca(40);
            int n = Math.min(4, 2 + Math.max(0, cerca - 1)) - caidos.size();
            LivingEntity presa = getTarget();
            int[] clases = {Marcado.CABALLERO, Marcado.ROMPEDOR, Marcado.CAZADOR, Marcado.CABALLERO};
            for (int k = 0; k < n; k++) {
                Marcado e = DarkCraftMod.MARCADO.get().create(sl);
                if (e == null) continue;
                Vec3 donde = null;
                for (int intento = 0; intento < 16 && donde == null; intento++) {
                    double a = random.nextDouble() * Math.PI * 2, r = 4.0 + random.nextDouble() * 5.0;
                    Vec3 v = new Vec3(getX() + Math.cos(a) * r, getY(), getZ() + Math.sin(a) * r);
                    if (libreChico(v)) donde = v;
                }
                if (donde == null) continue;
                e.moveTo(donde.x, donde.y, donde.z, random.nextFloat() * 360f, 0);
                e.variante(clases[(k + caidos.size()) % clases.length]);
                e.efimero(20 * 10);
                e.postura(TUMBADO);
                sl.addFreshEntity(e);
                if (presa != null) e.setTarget(presa);
                sl.sendParticles(ParticleTypes.SOUL, donde.x, donde.y + 0.4, donde.z, 16, 0.4, 0.3, 0.4, 0.03);
                sl.sendParticles(NEGRO, donde.x, donde.y + 0.4, donde.z, 20, 0.5, 0.3, 0.5, 0.02);
                caidos.add(e);
                LEVANTADOS++;
            }
        }
    }

    /** Si ahi cabe uno de los caidos (una persona) dentro de la sala. */
    private boolean libreChico(Vec3 v) {
        if (!Homenaje.enAguja(v.x, v.y + 0.2, v.z) || Math.hypot(v.x, v.z) > Homenaje.R_AGUJA - 1.5) return false;
        AABB c = new AABB(v.x - 0.35, v.y, v.z - 0.35, v.x + 0.35, v.y + 1.9, v.z + 0.35);
        BlockPos bajo = BlockPos.containing(v.x, v.y - 0.5, v.z);
        return level().noCollision(c) && !level().getBlockState(bajo).getCollisionShape(level(), bajo).isEmpty();
    }

    private void deshacerCaidos() {
        for (Marcado e : caidos) if (e.isAlive() && !e.isRemoved()) e.disolver();
        caidos.clear();
    }

    /** La furia (a media vida): se encoge, se agarra el pecho y se abre de golpe: se le abren los ojos (desde su golpe). */
    private void furiaT(int t) {
        if (t == 6) playSound(DarkCraftMod.SFX_SOMBRA_DOLOR.get(), 2.0f, 0.7f);
        if (t == SombraAnims.GOLPES[SombraAnims.FURIA][0]) {
            variante(1);
            playSound(DarkCraftMod.SFX_SOMBRA_GRITO.get(), 2.6f, 0.85f);
            playSound(DarkCraftMod.SFX_SOMBRA_ROMPE.get(), 2.0f, 0.8f);
            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SONIC_BOOM, getX(), getY() + 4.0, getZ(), 1, 0, 0, 0, 0);
                sl.sendParticles(ROJO, getX(), getY() + 4.0, getZ(), 60, 1.4, 2.0, 1.4, 0.05);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1.0, getZ(), 40, 1.2, 0.4, 1.2, 0.04);
            }
        }
    }

    // ------------------------------------------------------------------ rendirse y acabar

    /**
     * Con la vida a cero (no muere): cae de rodillas en el aire, deja de pelear, ofrece su corazon (frase 30) y el corazon sale de su
     * pecho ({@link Aguja} lo pone delante en {@link #SALE_CORAZON}).
     */
    public void rendirse() {
        if (level().isClientSide || rendida() || !isAlive()) return;
        entityData.set(RENDIDA, true);
        rendidaDesde = tickCount;
        RENDICIONES++;
        deshacerCaidos();
        marcas.clear();
        super.setTarget(null);
        getNavigation().stop();
        rendirPelea();
        empezar(SombraAnims.CAE);
        playSound(DarkCraftMod.SFX_SOMBRA_CAE.get(), 2.4f, 1.0f);
        Aguja.alRendirse(this);
        DarkCraftMod.LOGGER.info("[DarkCraft] La Sombra se rinde: de rodillas, ofrece su corazon");
    }

    /** Vuelve a salir ya rendida (el servidor se cerro mientras esperaba la respuesta): de rodillas, sin la caida. */
    public void yaRendida() {
        if (level().isClientSide || rendida()) return;
        entityData.set(RENDIDA, true);
        rendidaDesde = tickCount - SALE_CORAZON - 1;
        variante(1);
        postura(DE_PIE);
        rendirPelea();
    }

    /** Lo que hace mientras cae de rodillas: dice su frase 30 y le sale el corazon del pecho. */
    private void cae(int t) {
        if (t == OFRECE) {
            hablaHasta = 0;
            decir(30, true);
        }
        if (t == SALE_CORAZON - 4) playSound(DarkCraftMod.SFX_SOMBRA_LATIDO.get(), 2.0f, 0.9f);
        if (level() instanceof ServerLevel sl && t > 20 && t % 3 == 0)
            sl.sendParticles(NEGRO, getX(), getY() + 2.0, getZ(), 3, 0.8, 1.2, 0.8, 0.01);
    }

    /** Donde flota el corazon (delante de su pecho, a la altura de la cara de un jugador). */
    public Vec3 sitioCorazon() {
        Vec3 d = Vec3.directionFromRotation(0, getYRot());
        return position().add(d.x * 2.3, 1.35, d.z * 2.3);
    }

    /**
     * Ya se ha elegido: {@code golpeado} (el corazon roto: frase 31, "No... yo esperaba...") o tomado (frase 32: "Si... Ahora esperaras
     * tu..."). Dice su ultima frase y se deshace; al acabar, muere de verdad.
     */
    public void acabar(boolean golpeado, ServerPlayer quien) {
        if (level().isClientSide || final_ != 0) return;
        final_ = golpeado ? 1 : 2;
        quienEligio = quien == null ? null : quien.getUUID();
        acabaDesde = tickCount;
        FINALES++;
        hablaHasta = 0;
        decir(golpeado ? 31 : 32, true);
    }

    /** Como acaba (0 aun no, 1 golpeado, 2 tomado). */
    public int finalElegido() {
        return final_;
    }

    /** En el cliente y en el servidor: lo que lleva yendose (0-1; 0 si aun no se va). */
    public float yendose(float pt) {
        if (deathTime <= 0) return 0f;
        return Mth.clamp((deathTime + pt) / 40f, 0f, 1f);
    }

    // ------------------------------------------------------------------ cada tick

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || rendida();
    }

    @Override
    public void postura(int p) {
        // rendida no vuelve a colgar de sus cadenas
        if (p == REZA && rendida()) return;
        super.postura(p);
    }

    @Override
    public void heal(float cuanto) {
        if (rendida()) return;
        super.heal(cuanto);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            // la bruma negra a sus pies; con la furia, las ascuas del bajo de la tunica
            if (isAlive() && tickCount % 3 == 0) {
                level().addParticle(ParticleTypes.LARGE_SMOKE, getX() + (random.nextDouble() - 0.5) * 1.6, getY() + 0.2, getZ() + (random.nextDouble() - 0.5) * 1.6,
                        0, 0.01, 0);
                if (furia() && random.nextInt(2) == 0)
                    level().addParticle(ParticleTypes.SMALL_FLAME, getX() + (random.nextDouble() - 0.5) * 1.8, getY() + 0.3, getZ() + (random.nextDouble() - 0.5) * 1.8,
                            0, 0.02, 0);
            }
            return;
        }
        if (!isAlive()) return;
        if (Aguja.rendida(level().getServer()) && !rendida()) yaRendida();
        if (rendida()) {
            setDeltaMovement(0, getDeltaMovement().y, 0);
            // ya se ha elegido: al acabar su ultima frase se deshace (muere de verdad)
            if (final_ != 0 && tickCount - acabaDesde >= Math.max(VETE, Math.round(Voces.sombra(final_ == 1 ? 31 : 32) * 20) + 30)) morir();
            return;
        }
        if (esperaCadena > 0) esperaCadena--;
        if (esperaManos > 0) esperaManos--;
        if (esperaLanzas > 0) esperaLanzas--;
        if (esperaNiebla > 0) esperaNiebla--;
        if (esperaMirada > 0) esperaMirada--;
        if (esperaGrito > 0) esperaGrito--;
        if (esperaCaidos > 0) esperaCaidos--;
        if (esperaGiro > 0) esperaGiro--;
        // tras la niebla, los zarpazos
        if (accion() == 0 && encadena != 0) {
            int a = encadena;
            encadena = 0;
            if (getTarget() != null) girarHacia(getTarget(), 360f);
            empezar(a);
        }
        // ha caido aquel con quien peleaba
        if (enPelea() && ultimaPresa != null && ultimaPresa.isDeadOrDying()) {
            ultimaPresa = null;
            if (!dijo28) {
                dijo28 = true;
                decirSiCalla(28);
            }
        }
        // nunca sale de su sala (si la empujan, vuelve a su sitio)
        if (tickCount % 20 == 0 && !Homenaje.enAguja(getX(), getY() + 0.5, getZ()) && hogar() != null) {
            BlockPos h = hogar();
            moveTo(h.getX() + 0.5, h.getY(), h.getZ() + 0.5, getYRot(), 0);
        }
    }

    /** Muere de verdad (ya se ha elegido): el reparto lo hace {@code Guardianes} al morir. */
    private void morir() {
        if (!isAlive()) return;
        ServerPlayer quien = quienEligio != null && level().getServer() != null ? level().getServer().getPlayerList().getPlayer(quienEligio) : null;
        DamageSource s = quien != null ? damageSources().playerAttack(quien) : damageSources().genericKill();
        setInvulnerable(false);
        super.hurt(s.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY) ? s : damageSources().genericKill(), Float.MAX_VALUE);
        if (isAlive()) kill();
    }

    @Override
    public boolean hurt(DamageSource s, float cuanto) {
        boolean salta = s.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY);
        if (rendida()) return salta && final_ != 0 && super.hurt(s, cuanto);
        // colgada de sus cadenas (esperando o mientras despierta) no se le puede pegar
        if (encadenada() && !salta) return false;
        // apagada (en la niebla) tampoco
        int a = accion();
        if (a == SombraAnims.NIEBLA && accionTicks() >= APAGA && accionTicks() < APARECE + 2 && !salta) return false;
        // (no muere de un golpe: con la vida a cero se rinde, lo hace setHealth)
        return super.hurt(s, cuanto);
    }

    /** No muere sin que se le responda: si la vida fuera a quedar a cero, se queda con un poco y se rinde. */
    @Override
    public void setHealth(float h) {
        if (h <= 0f && final_ == 0 && !level().isClientSide && getHealth() > 0f) {
            super.setHealth(1f);
            rendirse();
            return;
        }
        super.setHealth(h);
    }

    @Override
    public void die(DamageSource s) {
        boolean ya = !isAlive() && deathTime > 0;
        if (!level().isClientSide) deshacerCaidos();
        super.die(s);
        if (level().isClientSide || ya) return;
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 3.0, getZ(), 60, 1.0, 2.0, 1.0, 0.05);
            sl.sendParticles(NEGRO, getX(), getY() + 3.0, getZ(), 80, 1.2, 2.4, 1.2, 0.03);
        }
        playSound(DarkCraftMod.SFX_SOMBRA_CAE.get(), 2.0f, 0.8f);
        Aguja.alMorir(this, final_);
    }

    /** Al morir (tras elegir) se deshace subiendo (40 ticks) y se va. */
    @Override
    protected void tickDeath() {
        ++deathTime;
        if (level().isClientSide() || isRemoved()) return;
        if (level() instanceof ServerLevel sl && deathTime % 2 == 0) {
            sl.sendParticles(NEGRO, getX(), getY() + 2.0 + deathTime * 0.06, getZ(), 8, 0.8, 1.6, 0.8, 0.03);
            sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 3.0, getZ(), 2, 0.6, 1.0, 0.6, 0.04);
        }
        if (deathTime >= 44) remove(RemovalReason.KILLED);
    }

    @Override
    protected SoundEvent sonidoAlerta() {
        return null;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource s) {
        return DarkCraftMod.SFX_SOMBRA_DOLOR.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos p, BlockState s) {
        // flota: no hace ruido al moverse
    }

    @Override
    public boolean isPushable() {
        return !rendida() && super.isPushable();
    }

    @Override
    public void knockback(double fuerza, double x, double z) {
        // ni colgada, ni rendida, ni tan grande se la empuja
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource s, int saqueo, boolean jugador) {
        super.dropCustomDeathLoot(s, saqueo, jugador);
        spawnAtLocation(new ItemStack(DarkCraftMod.PIEDRA_FORJA_3.get(), 4));
    }

    // ------------------------------------------------------------------ para las pruebas y los comandos

    /** Pasa ya a media vida (rompe la ultima cadena). */
    public void mediaVida() {
        if (level().isClientSide || !isAlive() || rendida()) return;
        setHealth(Math.min(getHealth(), getMaxHealth() * 0.45f));
        segunda = true;
        alSegundaMitad();
    }

    /** Que se rinda ya (como si le hubieran quitado toda la vida). */
    public void vencer() {
        if (level().isClientSide || !isAlive()) return;
        super.setHealth(1.0f);
        rendirse();
    }

    /** Hace esa accion ya (mirando a su presa), sin esperar a nada. */
    public void probar(int accion) {
        if (level().isClientSide || !isAlive() || rendida()) return;
        postura(DE_PIE);
        if (getTarget() != null) girarHacia(getTarget(), 360f);
        empezar(accion);
    }

    public List<Marcado> caidos() {
        caidos.removeIf(e -> !e.isAlive() || e.isRemoved());
        return caidos;
    }

    /** Como esta. */
    public String estado() {
        return String.format(java.util.Locale.ROOT, "vida %.0f/%.0f, %s%s en %.1f,%.1f,%.1f; despierta %d (corta %d, rompe %d), zarpazos %d, giros %d,"
                        + " cadenas %d (arrastra %d), manos %d (pillan %d), lanzas %d, niebla %d (detras %d), mirada %d (pilla %d), grito %d (pilla %d),"
                        + " caidos %d (%d), furia %d, se rinde %d, finales %d",
                getHealth(), getMaxHealth(), rendida() ? "rendida" : enPelea() ? "peleando" : encadenada() ? "encadenada" : "de pie",
                furia() ? " con la furia" : "", getX(), getY(), getZ(), DESPIERTA, CORTA, ROMPE, ZARPAZOS, GIROS, CADENAS, ARRASTRA, MANOS,
                MANOS_PILLAN, LANZAS, NIEBLAS, DETRAS_SALE, MIRADAS, MIRADA_PILLA, GRITOS, GRITO_PILLA, CAIDOS, LEVANTADOS, FURIAS, RENDICIONES, FINALES);
    }
}
