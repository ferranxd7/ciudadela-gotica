package com.darkcraft.client;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.cine.BarcoVolador;
import com.darkcraft.cine.Desenlace;
import com.darkcraft.cine.Intro;
import com.darkcraft.cine.Reparacion;
import com.darkcraft.cine.Shot;
import com.darkcraft.cine.Viaje;
import com.darkcraft.pueblo.gen.Barco;
import com.darkcraft.client.cine.BarcoVoladorRenderer;
import com.darkcraft.client.ui.Pix;
import com.darkcraft.rey.Voces;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Lo propio de las cinematicas con guion (las de la parte 3d, del numero 38 en adelante): lo que suena y cuando (la voz
 * de la Sombra con sus subtitulos, los efectos), cuando esta la pantalla en negro y lo que se pinta encima de la imagen
 * (las rayas de la caida, la marca que arde, los parpados). {@link ClientCine} lleva lo de siempre (la camara por los
 * planos, el marco, saltarla con ESPACIO) y pregunta aqui lo demas.
 * <p>
 * La de inicio ({@link Intro}): negro con la voz, la caida, la Sombra te atrapa, la marca, y despiertas en el muelle.
 * La reparacion del barco ({@link Reparacion}): sus golpes y sus velas. Los viajes ({@link Viaje}): el barco que vuela
 * (una entidad que solo existe aqui, movida cada tick con {@link Viaje#barco}), la nube que levanta al despegar y al
 * posarse, la campana, el quejido del casco, las velas, el viento, la musica y, la primera vez, la Sombra.
 */
public final class Guion {
    private Guion() {}

    private static final ResourceLocation MARCA = new ResourceLocation(DarkCraftMod.MODID, "textures/gui/marca_sombra.png");

    /** Cuanto antes de atraparte empieza su sonido: crece poco mas de un segundo y da el golpe. */
    private static final float ATRAPA_ANTES = 1.15f;

    /** Un sonido sin sitio (se oye igual en los dos oidos) que se puede apagar poco a poco. */
    private static final class Sonido extends AbstractTickableSoundInstance {
        private float baja, hacia = -1, paso;

        Sonido(SoundEvent ev, float vol) {
            super(ev, SoundSource.MASTER, SoundInstance.createUnseededRandom());
            this.volume = vol;
            this.relative = true;
            this.attenuation = Attenuation.NONE;
            this.delay = 0;
        }

        void apagar(float segundos) {
            if (baja <= 0) baja = Math.max(0.004f, volume / Math.max(1f, segundos * 20f));
        }

        /** A ese volumen, poco a poco (sin apagarse). */
        void nivel(float vol, float segundos) {
            hacia = vol;
            paso = Math.max(0.002f, Math.abs(vol - volume) / Math.max(1f, segundos * 20f));
        }

        @Override
        public void tick() {
            if (baja <= 0 && hacia >= 0) {
                volume = volume < hacia ? Math.min(hacia, volume + paso) : Math.max(hacia, volume - paso);
                if (volume == hacia) hacia = -1;
            }
            if (baja <= 0) return;
            volume -= baja;
            if (volume <= 0.003f) stop();
        }
    }

    /** Una cosa que pasa en un momento: {@code t} segundos desde el principio. */
    private record Paso(float t, Runnable que) {}

    private static final List<Paso> pasos = new ArrayList<>();
    private static final List<Sonido> sonando = new ArrayList<>();
    private static boolean[] hecho = new boolean[0];
    /** Las frases de la Sombra de esta cinematica: {numero, segundo en que empieza}. */
    private static float[][] frases = new float[0][];
    /** Las de Maelis (la del final, parte 7ad): {numero, segundo en que empieza}. */
    private static float[][] frasesGuia = new float[0][];
    /** El final que se esta viendo (0 si la cinematica no es la del final; 1 el alba, 2 la nueva Sombra) y su primer plano (la sala). */
    private static int finalCual, ultimaGuia;
    private static Shot primerPlano;
    private static Sonido aireFinal;
    private static Sonido abismo, viento;
    private static int ultimaFrase;

    /** El viaje que se esta viendo (null si la cinematica no es un viaje): sus tiempos, de que amarre a cual y el barco que vuela. */
    private static Viaje.Tiempos viaje;
    private static int[] viajeDe = {0, 0}, viajeA = {0, 0};
    private static boolean aValdrenn;
    private static BarcoVolador barco;
    private static final List<Sonido> vuelo = new ArrayList<>();
    /** El quejido del casco mientras baja en la llamada (se apaga al posarse). */
    private static final List<Sonido> quejidos = new ArrayList<>();
    /** El numero de la entidad del barco en el cliente: tan alto que el servidor nunca dara ese a otra. */
    private static final int BARCO_ID = 2_147_000_039;
    /** Los cortes del viaje: el ultimo pasado, el ultimo del que se ha avisado al servidor, y los ticks esperando (en este y en total). */
    private static int cortePasado, corteAvisado, corteTicks, esperado, esperadoSiempre;
    private static Shot ultimoPlano;
    /** Lo mas que se espera en cada corte, en ticks (si el servidor no llega a tiempo, se sigue). */
    private static final int[] CORTE_LIMITE = {0, 100, 240, 160};

    // ------------------------------------------------------------------ empezar, cada tick, acabar

    public static void empezar(int capitulo, double[] datos, List<Shot> planos) {
        acabar();
        ultimoPlano = planos.isEmpty() ? null : planos.get(planos.size() - 1);
        pasos.clear();
        frases = new float[0][];
        frasesGuia = new float[0][];
        ultimaFrase = ultimaGuia = 0;
        finalCual = 0;
        primerPlano = planos.isEmpty() ? null : planos.get(0);
        if (capitulo == Intro.ID) intro();
        else if (capitulo == Reparacion.ID) reparacion();
        else if (Viaje.es(capitulo)) viaje(capitulo, datos);
        else if (Desenlace.es(capitulo)) desenlace(capitulo);
        hecho = new boolean[pasos.size()];
    }

    /**
     * El final (parte 7ad, {@link Desenlace}): con la nueva Sombra dos latidos en la sala (el corazon que se ha tomado); fuera, el aire
     * de lo alto; al salir la isla entra la musica de los creditos ({@link ClientMusic#final_}, que sigue en ellos) y habla Maelis (la
     * 17 o la 21, en off: la musica baja mientras).
     */
    private static void desenlace(int capitulo) {
        finalCual = Desenlace.cual(capitulo);
        int voz = Desenlace.voz(capitulo);
        float dura = duraGuia(voz);
        frasesGuia = new float[][]{{voz, Desenlace.VOZ}};
        if (finalCual == 2) {
            pasos.add(new Paso(0.7f, () -> suena(DarkCraftMod.SFX_INTRO_LATIDO.get(), 0.9f)));
            pasos.add(new Paso(3.9f, () -> suena(DarkCraftMod.SFX_INTRO_LATIDO.get(), 1f)));
        }
        pasos.add(new Paso(Desenlace.P[1] - 0.3f, () -> {
            aireFinal = suena(DarkCraftMod.AMBIENTE_CADENA.get(), 0.15f);
            aireFinal.nivel(0.6f, 2.5f);
        }));
        pasos.add(new Paso(Desenlace.MUSICA, () -> ClientMusic.final_(finalCual, 4.5f)));
        pasos.add(new Paso(Desenlace.VOZ - 0.5f, () -> {
            ClientMusic.finalNivel(0.42f, 0.6f);
            if (aireFinal != null) aireFinal.nivel(0.3f, 0.6f);
        }));
        pasos.add(new Paso(Desenlace.VOZ, () -> fraseGuia(voz)));
        pasos.add(new Paso(Desenlace.VOZ + dura + 0.3f, () -> {
            ClientMusic.finalNivel(ClientMusic.FINAL, 1.8f);
            if (aireFinal != null) aireFinal.nivel(0.6f, 1.8f);
        }));
        pasos.add(new Paso(Desenlace.NEGRO - 0.5f, () -> {
            if (aireFinal != null) aireFinal.apagar(1.8f);
        }));
    }

    /** Lo que dura una frase de Maelis (de {@code voces.json}; si no se sabe, lo de {@link Desenlace}). */
    private static float duraGuia(int voz) {
        float s = com.darkcraft.dialogo.Dialogos.habla("guia", voz);
        return s > 0.3f ? s : voz == Desenlace.VOZ_SOMBRA ? Desenlace.DURA_SOMBRA : Desenlace.DURA_ALBA;
    }

    private static void fraseGuia(int n) {
        ultimaGuia = n;
        suena(SoundEvent.createVariableRangeEvent(new ResourceLocation(DarkCraftMod.MODID, "voz.guia." + n)), 1f);
    }

    /** La ultima frase de Maelis que ha empezado a sonar en una cinematica (para las pruebas). */
    public static int ultimaGuia() {
        return ultimaGuia;
    }

    /** La reparacion: el martilleo mientras se cierra el casco y crece el palo, y las velas al caer (la fanfarria la pone el servidor). */
    private static void reparacion() {
        pasos.add(new Paso(Reparacion.CASCO0 - 0.3f, () -> suena(DarkCraftMod.SFX_BARCO_REPARA.get(), 1f)));
        // (los dos golpes secos de la lona caen a los 2 y a los 3,5 s del sonido)
        pasos.add(new Paso(Reparacion.VELAS0 - 1.7f, () -> suena(DarkCraftMod.SFX_BARCO_VELAS.get(), 0.9f)));
    }

    /** El viento del vuelo, a ese volumen (el que ya suena y el que empiece despues). */
    private static void viento(float nivel, float segundos) {
        vientoNivel = nivel;
        for (Sonido v : vuelo) v.nivel(nivel, segundos);
    }

    private static float vientoNivel = 0.9f;

    private static int dato(double[] d, int i) {
        return d != null && i < d.length ? (int) Math.round(d[i]) : 0;
    }

    /** Un viaje del barco: {tipo, de dx, de dz, a dx, a dz, si se llega a Valdrenn} (ver {@link Viaje#datos}). */
    private static void viaje(int capitulo, double[] datos) {
        Viaje.Tiempos T = Viaje.tiempos(capitulo);
        viaje = T;
        cortePasado = corteAvisado = corteTicks = esperado = 0;
        viajeDe = new int[]{dato(datos, 1), dato(datos, 2)};
        viajeA = new int[]{dato(datos, 3), dato(datos, 4)};
        aValdrenn = dato(datos, 5) != 0;
        frases = T.voz;
        for (float[] f : T.voz) {
            int linea = (int) f[0];
            pasos.add(new Paso(f[1], () -> frase(linea)));
        }
        // mientras habla la Sombra, la musica y el viento bajan (si no, no se la entiende); entre dos frases seguidas no suben
        vientoNivel = 0.9f;
        for (int i = 0; i < T.voz.length; i++) {
            float desde = T.voz[i][1], hasta = desde + Voces.sombra((int) T.voz[i][0]);
            while (i + 1 < T.voz.length && T.voz[i + 1][1] - hasta < 3.5f) {
                i++;
                hasta = T.voz[i][1] + Voces.sombra((int) T.voz[i][0]);
            }
            pasos.add(new Paso(desde - 0.45f, () -> {
                ClientMusic.viajeNivel(0.36f, 0.5f);
                viento(0.4f, 0.5f);
            }));
            pasos.add(new Paso(hasta + 0.15f, () -> {
                ClientMusic.viajeNivel(ClientMusic.VIAJE, 1.1f);
                viento(0.9f, 1.1f);
            }));
        }
        float frenado = T.llega + T.crucero + T.frena;
        if (T.cambioA >= 0) {
            pasos.add(new Paso(0.35f, () -> suena(DarkCraftMod.SFX_BARCO_CAMPANA.get(), 0.9f)));
            // la musica empieza suave en el corte (sus ocho primeros segundos) y el tema entra cuando el barco ya avanza
            pasos.add(new Paso(T.cambioA + 0.45f, () -> ClientMusic.viaje(true, 2.5f)));
            pasos.add(new Paso(T.sube - 0.3f, () -> suena(DarkCraftMod.SFX_BARCO_ELEVA.get(), 1f)));
            pasos.add(new Paso(T.avanza - 0.6f, () -> suena(DarkCraftMod.SFX_BARCO_VELAS.get(), 0.9f)));
            pasos.add(new Paso(T.avanza, () -> vuelo.add(suena(DarkCraftMod.SFX_BARCO_VUELO.get(), vientoNivel))));
            // el viento dura 25 s: si el vuelo es mas largo, otra vez antes de que se acabe
            if (frenado - T.avanza > 21.5f) pasos.add(new Paso(T.avanza + 20.5f, () -> vuelo.add(suena(DarkCraftMod.SFX_BARCO_VUELO.get(), vientoNivel))));
            pasos.add(new Paso(T.ultimo, () -> ClientMusic.viaje(false, T.fin - T.ultimo + 1.5f)));
            pasos.add(new Paso(frenado - 0.6f, () -> {
                for (Sonido v : vuelo) v.apagar(2.6f);
                vuelo.clear();
            }));
        } else {
            // la llamada no lleva musica: la campana, el viento hasta que se posa, las velas al detenerse y el casco al bajar
            pasos.add(new Paso(0.05f, () -> suena(DarkCraftMod.SFX_BARCO_CAMPANA.get(), 1f)));
            pasos.add(new Paso(0.3f, () -> vuelo.add(suena(DarkCraftMod.SFX_BARCO_VUELO.get(), 1f))));
            pasos.add(new Paso(frenado - 0.5f, () -> suena(DarkCraftMod.SFX_BARCO_VELAS.get(), 0.85f)));
            pasos.add(new Paso(frenado + T.gira - 0.4f, () -> {
                quejidos.add(suena(DarkCraftMod.SFX_BARCO_ELEVA.get(), 0.6f));
            }));
            pasos.add(new Paso(T.toca() - 0.3f, () -> {
                for (Sonido v : vuelo) v.apagar(2.0f);
                vuelo.clear();
                for (Sonido v : quejidos) v.apagar(1.2f);
                quejidos.clear();
            }));
        }
        // (el golpe gordo del casco contra el muelle cae al segundo de empezar el sonido)
        pasos.add(new Paso(T.toca() - 0.95f, () -> suena(DarkCraftMod.SFX_BARCO_ATRACA.get(), 1f)));
    }

    private static void intro() {
        frases = new float[6][];
        for (int n = 1; n <= 6; n++) {
            frases[n - 1] = new float[]{n, Intro.VOZ[n]};
            int linea = n;
            pasos.add(new Paso(Intro.VOZ[n], () -> frase(linea)));
        }
        pasos.add(new Paso(0.2f, () -> abismo = suena(DarkCraftMod.SFX_INTRO_ABISMO.get(), 1f)));
        pasos.add(new Paso(Intro.CAIDA - 0.1f, () -> {
            if (abismo != null) abismo.apagar(1.2f);
            viento = suena(DarkCraftMod.SFX_INTRO_CAIDA.get(), 1f);
        }));
        pasos.add(new Paso(Intro.ATRAPA - ATRAPA_ANTES, () -> suena(DarkCraftMod.SFX_INTRO_ATRAPA.get(), 1f)));
        pasos.add(new Paso(Intro.ATRAPA, () -> {
            if (viento != null) viento.apagar(0.15f);
        }));
        pasos.add(new Paso(Intro.ATRAPA + 0.35f, () -> abismo = suena(DarkCraftMod.SFX_INTRO_ABISMO.get(), 0.9f)));
        pasos.add(new Paso(Intro.MARCA - 0.1f, () -> suena(DarkCraftMod.SFX_INTRO_MARCA.get(), 1f)));
        pasos.add(new Paso(Intro.LATIDO, () -> suena(DarkCraftMod.SFX_INTRO_LATIDO.get(), 1f)));
        pasos.add(new Paso(Intro.OJOS - 0.8f, () -> {
            if (abismo != null) abismo.apagar(1.6f);
        }));
    }

    public static void tick(Minecraft mc, int capitulo, float t) {
        for (int i = 0; i < pasos.size(); i++) {
            if (hecho[i] || t < pasos.get(i).t()) continue;
            hecho[i] = true;
            // lo que tocaba hace mas de un segundo (un tiron del juego) ya no viene a cuento
            if (t - pasos.get(i).t() < 1.0f) pasos.get(i).que().run();
        }
        sonando.removeIf(s -> s.isStopped());
        // el final, en la sala: con el alba sube luz de donde estaba la Sombra; con la nueva Sombra la oscuridad entra en quien la tomo
        if (finalCual > 0 && t < Desenlace.Q[0] && mc.level != null && primerPlano != null && primerPlano.look.length >= 3) {
            RandomSource r = mc.level.random;
            double sx = primerPlano.look[0], sy = primerPlano.look[1] - 3.0, sz = primerPlano.look[2];
            if (finalCual == 1) {
                for (int i = 0; i < 3; i++)
                    mc.level.addParticle(ParticleTypes.END_ROD, sx + (r.nextDouble() - 0.5) * 2.6, sy + 0.8 + r.nextDouble() * 5.5,
                            sz + (r.nextDouble() - 0.5) * 2.6, (r.nextDouble() - 0.5) * 0.01, 0.035 + r.nextDouble() * 0.05, (r.nextDouble() - 0.5) * 0.01);
            } else if (mc.player != null) {
                for (int i = 0; i < 4; i++) {
                    double a = r.nextDouble() * Math.PI * 2, d = 0.7 + r.nextDouble() * 1.2;
                    mc.level.addParticle(ParticleTypes.SQUID_INK, mc.player.getX() + Math.cos(a) * d, mc.player.getY() + 0.2 + r.nextDouble() * 1.7,
                            mc.player.getZ() + Math.sin(a) * d, -Math.cos(a) * 0.06, 0.01, -Math.sin(a) * 0.06);
                }
            }
        }
        if (viaje != null) {
            moverBarco(mc, t);
            // en Valdrenn siempre hay tormenta: al llegar, la lluvia entra en el ultimo plano (despues la manda el servidor)
            if (aValdrenn && viaje.tipo != Viaje.LLAMADA && t >= viaje.ultimo && mc.level != null) {
                float k = suave((t - viaje.ultimo) / (viaje.fin - viaje.ultimo - 0.5f));
                if (mc.level.getRainLevel(1f) < k) {
                    mc.level.setRainLevel(k);
                    mc.level.setThunderLevel(0.6f * k);
                }
            }
        }
    }

    /** El barco que vuela: aparece cuando el de bloques se quita (en negro), se mueve cada tick y se va cuando vuelve a ser de bloques. */
    /**
     * Los cortes en negro de un viaje ({@link Viaje#corte}): al llegar a cada uno se avisa al servidor (que entonces cambia
     * el mundo: quita el barco de bloques, se lleva al jugador al otro amarre, vuelve a poner el barco) y se espera, con la
     * pantalla en negro y el reloj parado, hasta verlo hecho. Devuelve true mientras hay que esperar.
     */
    public static boolean espera(Minecraft mc, int capitulo, float t) {
        if (viaje == null || mc.level == null || mc.player == null) return false;
        for (int k = cortePasado + 1; k <= 3; k++) {
            float tk = Viaje.corte(viaje, k);
            if (tk >= 0) {
                if (t < tk) return false;
                if (corteAvisado < k) {
                    corteAvisado = k;
                    corteTicks = 0;
                    com.darkcraft.net.Net.CH.sendToServer(new com.darkcraft.net.Net.CineFase(k));
                }
                if (!cambiado(mc, k) && ++corteTicks <= CORTE_LIMITE[k]) {
                    esperado++;
                    esperadoSiempre++;
                    return true;
                }
            }
            cortePasado = k;
            // el jugador ya esta en cubierta: que al acabar mire a donde acaba mirando la camara (el ultimo plano termina en sus ojos)
            if (k == 3 && viaje.tipo != Viaje.LLAMADA && ultimoPlano != null) {
                float[] a = Shot.angles(ultimoPlano.at(ultimoPlano.t1));
                ClientCine.mirar(a[0], a[1]);
            }
        }
        return false;
    }

    /** Si el mundo ya esta como tiene que estar tras ese corte. */
    private static boolean cambiado(Minecraft mc, int k) {
        net.minecraft.core.BlockPos.MutableBlockPos p = new net.minecraft.core.BlockPos.MutableBlockPos();
        // el palo del barco de bloques: ya no esta en el muelle del que se sale, o ya esta en el que se llega
        if (k == 1) return mc.level.getBlockState(p.set(Barco.BX + viajeDe[0], Barco.CUBIERTA + 3, Barco.PALO + viajeDe[1])).isAir();
        if (k == 3) return !mc.level.getBlockState(p.set(Barco.BX + viajeA[0], Barco.CUBIERTA + 3, Barco.PALO + viajeA[1])).isAir();
        // el salto: el jugador ya esta junto al otro amarre y han llegado los chunks del muelle y de por donde viene el barco
        double[] f = Viaje.flota(viajeA, true);
        double dx = mc.player.getX() - f[0], dz = mc.player.getZ() - f[2];
        if (dx * dx + dz * dz > 48 * 48) return false;
        int px = Barco.BX + viajeA[0], pz = Barco.PALO + viajeA[1];
        var chunks = mc.level.getChunkSource();
        return chunks.hasChunk(px >> 4, pz >> 4) && chunks.hasChunk(px >> 4, (pz + 24) >> 4) && chunks.hasChunk((px + 10) >> 4, (pz + 8) >> 4)
                && chunks.hasChunk(mc.player.chunkPosition().x, mc.player.chunkPosition().z);
    }

    /** Los ticks que han esperado en sus cortes todos los viajes desde que se abrio el juego (para el reloj de las pruebas). */
    public static int esperadoSiempre() {
        return esperadoSiempre;
    }

    /** Cuantos ticks ha esperado esta cinematica en sus cortes, y por cual va (para las pruebas). */
    public static String cortes() {
        return viaje == null ? "-" : "corte " + cortePasado + " (avisado " + corteAvisado + "), esperado " + esperado + " ticks";
    }

    private static void moverBarco(Minecraft mc, float t) {
        double[] s = mc.level == null ? null : Viaje.barco(viaje, t, viajeDe, viajeA);
        if (s == null) {
            quitarBarco(mc);
            return;
        }
        boolean nuevo = barco == null || barco.isRemoved() || barco.level() != mc.level;
        if (nuevo) {
            quitarBarco(mc);
            BarcoVoladorRenderer.olvidar();
            barco = new BarcoVolador(DarkCraftMod.BARCO_VOLADOR.get(), mc.level);
            barco.setId(BARCO_ID);
            barco.setPos(s[0], s[1], s[2]);
            mc.level.putNonPlayerEntity(BARCO_ID, barco);
        }
        // de golpe al aparecer y al saltar de un amarre al otro (en negro); si no, entre un tick y el siguiente
        boolean golpe = nuevo || barco.distanceToSqr(s[0], s[1], s[2]) > 40 * 40;
        barco.mover(s[0], s[1], s[2], (float) s[3], (float) s[4], (float) s[5], golpe);
        // la nube que levanta el casco al despegar y al posarse
        double alto = s[1] - Viaje.PY;
        if (alto > 0.15 && alto < 7.5) {
            RandomSource r = mc.level.random;
            for (int i = 0; i < 5; i++) {
                double ang = r.nextDouble() * Math.PI * 2, ca = Math.cos(ang), sa = Math.sin(ang);
                double x = s[0] + ca * (3 + r.nextDouble() * 4), z = s[2] + 1 + sa * (6 + r.nextDouble() * 10);
                mc.level.addParticle(ParticleTypes.CLOUD, x, Viaje.PY - 3.6 + r.nextDouble() * 1.2, z, ca * 0.12, 0.02 + r.nextDouble() * 0.05, sa * 0.12);
            }
        }
    }

    private static void quitarBarco(Minecraft mc) {
        if (barco == null) return;
        if (mc.level != null && barco.level() == mc.level) mc.level.removeEntity(BARCO_ID, Entity.RemovalReason.DISCARDED);
        barco = null;
    }

    /** El barco que vuela ahora mismo, si lo hay (para las pruebas). */
    public static BarcoVolador barco() {
        return barco;
    }

    /** Se acaba (sola, saltada o porque lo dice el servidor): lo que sonaba se apaga enseguida. */
    public static void acabar() {
        for (Sonido s : sonando) s.apagar(0.25f);
        sonando.clear();
        abismo = viento = aireFinal = null;
        finalCual = 0;
        vuelo.clear();
        quejidos.clear();
        if (viaje != null) {
            // (si se salta a medias, la musica se va enseguida; si acaba sola, ya se estaba yendo)
            ClientMusic.viaje(false, 1.5f);
            quitarBarco(Minecraft.getInstance());
            viaje = null;
        }
    }

    private static Sonido suena(SoundEvent ev, float vol) {
        Sonido s = new Sonido(ev, vol);
        Minecraft.getInstance().getSoundManager().play(s);
        sonando.add(s);
        return s;
    }

    private static void frase(int n) {
        ultimaFrase = n;
        if (n > 0 && n < DarkCraftMod.SOMBRA_VOZ.length && DarkCraftMod.SOMBRA_VOZ[n] != null) suena(DarkCraftMod.SOMBRA_VOZ[n].get(), 1f);
    }

    /** La ultima frase de la Sombra que ha empezado a sonar (para las pruebas). */
    public static int ultimaFrase() {
        return ultimaFrase;
    }

    /** Cuantos sonidos del guion siguen sonando (para las pruebas). */
    public static int sonando() {
        return sonando.size();
    }

    // ------------------------------------------------------------------ lo que cambia de la camara y del negro

    /** Lo negra que esta la pantalla (0-1) en ese momento. */
    public static float negro(int capitulo, float t, float total) {
        if (capitulo == Intro.ID) {
            if (t < Intro.CAIDA) return 1f;                                   // la voz en lo oscuro
            if (t < Intro.CAIDA + 0.25f) return 1f - (t - Intro.CAIDA) / 0.25f; // te arroja: de golpe
            if (t < Intro.ATRAPA - 0.3f) return 0f;
            if (t < Intro.ATRAPA) {                                           // la Sombra se cierra sobre ti
                float k = (t - (Intro.ATRAPA - 0.3f)) / 0.3f;
                return k * k;
            }
            if (t < Intro.OJOS) return 1f;
            return 0f;                                                        // (los parpados van aparte)
        }
        if (viaje != null) return Viaje.negro(viaje, t);
        if (Desenlace.es(capitulo)) {
            // sale de negro, un corte en negro entre plano y plano y, al final, a negro (en la del alba, despues de la luz)
            float a = t < 0.7f ? 1f - suave(t / 0.7f) : 0f;
            for (int i = 0; i + 1 < Desenlace.P.length; i++) {
                float q = Desenlace.Q[i], p = Desenlace.P[i + 1];
                if (t >= q - 0.35f && t < q) a = Math.max(a, suave(1f - (q - t) / 0.35f));
                else if (t >= q && t < p) a = 1f;
                else if (t >= p && t < p + 0.5f) a = Math.max(a, 1f - suave((t - p) / 0.5f));
            }
            if (t >= Desenlace.NEGRO) a = Math.max(a, suave((t - Desenlace.NEGRO) / (Desenlace.Q[3] - 0.1f - Desenlace.NEGRO)));
            return a;
        }
        if (capitulo == Reparacion.ID) {
            // la entrada, un corte corto en cada cambio de plano y el fundido de vuelta a los ojos del jugador
            float a = t < 0.6f ? 1f - suave(t / 0.6f) : 0f;
            for (int i = 1; i < Reparacion.PLANO.length - 1; i++) {
                float tc = Reparacion.PLANO[i];
                if (t >= tc - 0.3f && t < tc) a = Math.max(a, suave(1f - (tc - t) / 0.3f));
                else if (t >= tc && t < tc + 0.5f) a = Math.max(a, 1f - suave((t - tc) / 0.5f));
            }
            float vuelve = total - 0.45f;
            if (t >= vuelve - 0.4f && t < vuelve) a = Math.max(a, suave(1f - (vuelve - t) / 0.4f));
            else if (t >= vuelve) a = Math.max(a, 1f - suave((t - vuelve) / 0.45f));
            return a;
        }
        return 0f;
    }

    /**
     * Cuanto se sube el brillo (0-1) mientras manda la camara de cine: en los viajes y la reparacion, del todo (en Valdrenn
     * es siempre de noche), y al acabar un viaje baja poco a poco hasta el del jugador (acaba en sus ojos: sin salto).
     */
    public static float brillo(int capitulo, float t) {
        if (capitulo == Intro.ID) return 0f;
        if (viaje != null && viaje.tipo != Viaje.LLAMADA) return 1f - suave((t - (viaje.fin - 1.8f)) / 1.6f);
        return 1f;
    }

    /** Si en ese momento se dibuja a los jugadores: reparando el barco y esperandolo en el muelle, si; a bordo, hasta que despega. */
    public static boolean jugadores(int capitulo, float t) {
        if (capitulo == Reparacion.ID || capitulo == Viaje.LLAMADA) return true;
        // el final: en la sala se ve a los que estaban
        if (Desenlace.es(capitulo)) return t < Desenlace.Q[0] + 0.2f;
        return viaje != null && viaje.cambioA >= 0 && t < viaje.cambioA - 0.1f;
    }

    /** Grados que se ladea la camara de mas. */
    public static float ladeo(int capitulo, float t) {
        if (capitulo == Intro.ID) {
            if (t >= Intro.CAIDA && t < Intro.ATRAPA + 0.5f) return 7f * Mth.sin((t - Intro.CAIDA) * 0.9f) + 2.5f * Mth.sin(t * 3.7f);
            // al despertar, la cabeza ladeada en el suelo; se endereza al incorporarse
            if (t >= Intro.OJOS) return 16f * (1f - suave((t - Intro.OJOS - 1.6f) / 2.6f));
        }
        return 0f;
    }

    /** El campo de vision: el del plano, salvo al final de la de inicio, que acaba en el del jugador. */
    public static double fov(int capitulo, float t, double delPlano, double delJugador) {
        if (capitulo == Intro.ID && t >= Intro.FIN - 1.6f) return Mth.lerp(suave((t - (Intro.FIN - 1.6f)) / 1.6f), delPlano, delJugador);
        if (capitulo == Intro.ID && t >= Intro.CAIDA && t < Intro.OJOS) return delPlano + 10 * suave((t - Intro.CAIDA) / 6f);
        // los viajes acaban en los ojos del jugador, de pie en el alcazar
        if (viaje != null && viaje.tipo != Viaje.LLAMADA && t >= viaje.fin - 1.6f) return Mth.lerp(suave((t - (viaje.fin - 1.6f)) / 1.6f), delPlano, delJugador);
        return delPlano;
    }

    /** Si en esta cinematica salen las brasas que suben (las de las de los jefes). */
    public static boolean brasas(int capitulo, float t) {
        // (y en la sala, en el final de la nueva Sombra)
        if (capitulo == Desenlace.SOMBRA) return t < Desenlace.Q[0];
        return capitulo == Intro.ID && (t < Intro.CAIDA + 0.3f || (t >= Intro.ATRAPA && t < Intro.OJOS + 0.4f));
    }

    // ------------------------------------------------------------------ lo que se pinta

    /** Debajo del negro (encima de la imagen del mundo): las rayas de la caida y los parpados. */
    public static void bajoNegro(GuiGraphics g, int w, int h, int capitulo, float t) {
        if (capitulo == Desenlace.ALBA && t >= Desenlace.LUZ) {
            // el alba acaba en una luz calida que lo llena todo (y despues, negro)
            float k = suave((t - Desenlace.LUZ) / (Desenlace.NEGRO + 0.6f - Desenlace.LUZ));
            if (k * 255 >= 4) g.fill(0, 0, w, h, argb(0.62f * k, 255, 216, 168));
            return;
        }
        if (capitulo != Intro.ID) return;
        if (t >= Intro.CAIDA && t < Intro.ATRAPA) rayas(g, w, h, t);
        if (t >= Intro.OJOS) parpados(g, w, h, t - Intro.OJOS);
    }

    /** Encima del negro: la marca. */
    public static void sobreNegro(GuiGraphics g, int w, int h, int capitulo, float t) {
        if (capitulo == Intro.ID && t >= Intro.MARCA && t < Intro.OJOS) marca(g, w, h, t);
    }

    private static float tope(float v) {
        return v < 0 ? 0 : Math.min(1, v);
    }

    private static float suave(float v) {
        v = tope(v);
        return v * v * (3 - 2 * v);
    }

    private static int argb(float a, int r, int gg, int b) {
        return (Math.round(tope(a) * 255) << 24) | (r << 16) | (gg << 8) | b;
    }

    private static float azar(int i, int k) {
        float v = Mth.sin(i * 12.9898f + k * 78.233f) * 43758.547f;
        return v - Mth.floor(v);
    }

    /** El aire que pasa: rayas que salen del centro hacia los bordes, cada vez mas deprisa; al final, rojas. */
    private static void rayas(GuiGraphics g, int w, int h, float t) {
        float d = t - Intro.CAIDA, queda = Intro.ATRAPA - t;
        float vis = Math.min(tope(d / 1.2f), 1f) * (0.55f + 0.45f * tope(d / 6f));
        float rojo = tope(1f - queda / 1.6f);
        float R = (float) Math.sqrt(w * w + h * h) * 0.5f;
        PoseStack pose = g.pose();
        for (int i = 0; i < 54; i++) {
            float vel = 0.55f + 0.9f * azar(i, 1) + 0.35f * tope(d / 7f);
            float p = (d * vel + azar(i, 2)) % 1f;
            float a = (float) Math.sin(Math.PI * p) * vis * (0.16f + 0.3f * azar(i, 3));
            if (a * 255 < 4) continue;
            float r0 = (0.18f + 0.82f * p * p) * R, largo = (0.03f + 0.25f * p) * R;
            boolean sombra = azar(i, 5) < 0.2f + 0.6f * rojo;
            pose.pushPose();
            pose.translate(w / 2f, h / 2f, 0);
            pose.mulPose(Axis.ZP.rotationDegrees(azar(i, 4) * 360f + d * 9f));
            int ancho = azar(i, 6) < 0.3f ? 2 : 1;
            g.fill(Math.round(r0), -ancho, Math.round(r0 + largo), ancho, sombra ? argb(a * 1.2f, 150, 14, 20) : argb(a, 232, 238, 255));
            pose.popPose();
        }
        // lo oscuro que se cierra por los bordes al final
        if (rojo > 0.02f) {
            int n = 6;
            for (int k = 0; k < n; k++) {
                float a = rojo * 0.5f * (1f - k / (float) n);
                int m = Math.round((k + 1) * Math.min(w, h) * 0.035f * rojo);
                int c = argb(a * 0.5f, 30, 0, 4);
                g.fill(0, 0, w, m, c);
                g.fill(0, h - m, w, h, c);
                g.fill(0, m, m, h - m, c);
                g.fill(w - m, m, w, h - m, c);
            }
        }
    }

    /** Lo abiertos que estan los ojos (0-1) a {@code d} segundos de empezar a abrirlos: un parpadeo y luego del todo. */
    public static float ojos(float d) {
        if (d <= 0) return 0;
        if (d < 0.7f) return 0.30f * suave(d / 0.7f);
        if (d < 1.15f) return Mth.lerp(suave((d - 0.7f) / 0.45f), 0.30f, 0.03f);
        if (d < 1.5f) return 0.03f;
        if (d < 2.5f) {
            float k = (d - 1.5f) / 1.0f;
            return Mth.lerp(1 - (1 - k) * (1 - k) * (1 - k), 0.03f, 1f);
        }
        return 1f;
    }

    private static void parpados(GuiGraphics g, int w, int h, float d) {
        float o = ojos(d);
        // la luz que deslumbra al abrir y se va
        float luz = 0.34f * tope(o * 3f) * (1f - suave((d - 1.6f) / 2.2f));
        if (luz * 255 >= 4) g.fill(0, 0, w, h, argb(luz, 226, 232, 240));
        if (o >= 0.999f) return;
        int medio = h / 2, cerrado = Math.round(medio * (1f - o)) + 1;
        g.fill(0, 0, w, cerrado, 0xFF000000);
        g.fill(0, h - cerrado, w, h, 0xFF000000);
        // el borde del parpado, difuminado en cuatro franjas
        int franja = Math.max(2, h / 60);
        for (int k = 0; k < 4; k++) {
            int c = argb(0.8f - 0.2f * k, 0, 0, 0);
            g.fill(0, cerrado + k * franja, w, cerrado + (k + 1) * franja, c);
            g.fill(0, h - cerrado - (k + 1) * franja, w, h - cerrado - k * franja, c);
        }
    }

    /** La marca de la Sombra: entra al rojo blanco, late mientras habla y se apaga antes de abrir los ojos. */
    private static void marca(GuiGraphics g, int w, int h, float t) {
        float d = t - Intro.MARCA;
        float sale = 1f - suave((t - Intro.MARCA_FUERA) / 1.0f);
        float entra = suave(d / 0.9f);
        float vis = entra * sale;
        if (vis * 255 < 4) return;
        int s = Math.max(2, Math.round(h / 150f));
        float escala = s * (1f + 0.28f * (1f - suave(d / 0.7f)));
        float late = 0.5f + 0.5f * Mth.sin(d * 2.4f);
        float caliente = (float) Math.exp(-d * 1.5f);
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(w / 2f, h * 0.47f, 0);
        pose.scale(escala, escala, 1);
        // el resplandor (suma luz), dos veces mas grande
        Pix.suma();
        Pix.color(1f, 0.55f + 0.3f * caliente, 0.45f + 0.4f * caliente, vis * (0.45f + 0.35f * late + 0.3f * caliente));
        pose.pushPose();
        pose.scale(1.6f, 1.6f, 1);
        g.blit(MARCA, -32, -32, 64, 0, 64, 64, 128, 64);
        pose.popPose();
        g.blit(MARCA, -32, -32, 64, 0, 64, 64, 128, 64);
        // la marca
        Pix.mezcla();
        Pix.color(1f, 0.78f + 0.22f * caliente + 0.08f * late, 0.7f + 0.3f * caliente, vis);
        g.blit(MARCA, -32, -32, 0, 0, 64, 64, 128, 64);
        if (caliente > 0.05f) {
            Pix.suma();
            Pix.color(1f, 0.95f, 0.85f, vis * caliente * 0.8f);
            g.blit(MARCA, -32, -32, 0, 0, 64, 64, 128, 64);
        }
        Pix.blanco();
        Pix.mezcla();
        pose.popPose();
    }

    /** Los subtitulos de la Sombra (y de Maelis, en el final), en la banda de abajo. */
    public static void subtitulos(GuiGraphics g, Font font, int w, int h, float t, int bh) {
        for (float[] f : frasesGuia) {
            int n = (int) f[0];
            float desde = f[1] - 0.1f, hasta = f[1] + duraGuia(n) + 0.9f;
            if (t < desde || t > hasta) continue;
            float a = Math.min(tope((t - desde) / 0.3f), tope((hasta - t) / 0.4f));
            if (a * 255 < 4) continue;
            List<FormattedCharSequence> partes = font.split(Component.translatable("darkcraft.dialogo.guia." + n), (int) (w * 0.8f));
            int lh = 10, y0 = h - Math.max(bh, 24) / 2 - partes.size() * lh / 2 + 1;
            for (int k = 0; k < partes.size(); k++) {
                FormattedCharSequence p = partes.get(k);
                int x = (w - font.width(p)) / 2;
                g.drawString(font, p, x + 1, y0 + k * lh + 1, argb(a * 0.85f, 30, 18, 6), false);
                g.drawString(font, p, x, y0 + k * lh, argb(a, 240, 224, 196), false);
            }
        }
        for (float[] f : frases) {
            int n = (int) f[0];
            float desde = f[1] - 0.1f, hasta = f[1] + Voces.sombra(n) + 0.9f;
            if (t < desde || t > hasta) continue;
            float a = Math.min(tope((t - desde) / 0.3f), tope((hasta - t) / 0.4f));
            if (a * 255 < 4) continue;
            Component linea = Component.translatable("darkcraft.sombra.voz." + n);
            List<FormattedCharSequence> partes = font.split(linea, (int) (w * 0.8f));
            int lh = 10, y0 = h - Math.max(bh, 24) / 2 - partes.size() * lh / 2 + 1;
            for (int k = 0; k < partes.size(); k++) {
                FormattedCharSequence p = partes.get(k);
                int x = (w - font.width(p)) / 2;
                g.drawString(font, p, x + 1, y0 + k * lh + 1, argb(a * 0.85f, 24, 0, 30), false);
                g.drawString(font, p, x, y0 + k * lh, argb(a, 216, 200, 236), false);
            }
        }
    }
}
