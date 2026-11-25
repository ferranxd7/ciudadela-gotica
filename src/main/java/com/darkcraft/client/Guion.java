package com.darkcraft.client;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.cine.Intro;
import com.darkcraft.cine.Shot;
import com.darkcraft.client.ui.Pix;
import com.darkcraft.rey.Voces;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * Lo propio de las cinematicas con guion (las de la parte 3d, del numero 38 en adelante): lo que suena y cuando (la voz
 * de la Sombra con sus subtitulos, los efectos), cuando esta la pantalla en negro y lo que se pinta encima de la imagen
 * (las rayas de la caida, la marca que arde, los parpados). {@link ClientCine} lleva lo de siempre (la camara por los
 * planos, el marco, saltarla con ESPACIO) y pregunta aqui lo demas.
 * <p>
 * La de inicio ({@link Intro}): negro con la voz, la caida, la Sombra te atrapa, la marca, y despiertas en el muelle.
 */
public final class Guion {
    private Guion() {}

    private static final ResourceLocation MARCA = new ResourceLocation(DarkCraftMod.MODID, "textures/gui/marca_sombra.png");

    /** Cuanto antes de atraparte empieza su sonido: crece poco mas de un segundo y da el golpe. */
    private static final float ATRAPA_ANTES = 1.15f;

    /** Un sonido sin sitio (se oye igual en los dos oidos) que se puede apagar poco a poco. */
    private static final class Sonido extends AbstractTickableSoundInstance {
        private float baja;

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

        @Override
        public void tick() {
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
    private static Sonido abismo, viento;
    private static int ultimaFrase;

    // ------------------------------------------------------------------ empezar, cada tick, acabar

    public static void empezar(int capitulo, double[] datos, List<Shot> planos) {
        acabar();
        pasos.clear();
        frases = new float[0][];
        ultimaFrase = 0;
        if (capitulo == Intro.ID) intro();
        hecho = new boolean[pasos.size()];
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
    }

    /** Se acaba (sola, saltada o porque lo dice el servidor): lo que sonaba se apaga enseguida. */
    public static void acabar() {
        for (Sonido s : sonando) s.apagar(0.25f);
        sonando.clear();
        abismo = viento = null;
    }

    private static Sonido suena(SoundEvent ev, float vol) {
        Sonido s = new Sonido(ev, vol);
        Minecraft.getInstance().getSoundManager().play(s);
        sonando.add(s);
        return s;
    }

    private static void frase(int n) {
        ultimaFrase = n;
        if (n > 0 && n < DarkCraftMod.SOMBRA_VOZ.length) suena(DarkCraftMod.SOMBRA_VOZ[n].get(), 1f);
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
        return 0f;
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
        return delPlano;
    }

    /** Si en esta cinematica salen las brasas que suben (las de las de los jefes). */
    public static boolean brasas(int capitulo, float t) {
        return capitulo == Intro.ID && (t < Intro.CAIDA + 0.3f || (t >= Intro.ATRAPA && t < Intro.OJOS + 0.4f));
    }

    // ------------------------------------------------------------------ lo que se pinta

    /** Debajo del negro (encima de la imagen del mundo): las rayas de la caida y los parpados. */
    public static void bajoNegro(GuiGraphics g, int w, int h, int capitulo, float t) {
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

    /** Los subtitulos de la Sombra, en la banda de abajo. */
    public static void subtitulos(GuiGraphics g, Font font, int w, int h, float t, int bh) {
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
