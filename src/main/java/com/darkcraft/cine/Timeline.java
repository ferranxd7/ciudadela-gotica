package com.darkcraft.cine;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Tiempos de la cinematica, iguales en el servidor y en el cliente. Los de la narracion de los capitulos narrados
 * (el 1 y el 5) salen del fichero que genera la CI junto al audio (cinematica/capN.json).
 */
public final class Timeline {
    private Timeline() {}

    /** Duracion de la animacion de titulo de los capitulos 2 a 4. */
    public static final float TITLE = 8.0f;
    /** En los capitulos narrados la voz empieza al acabar el titulo. */
    public static final float NARR_START = 7.0f;
    /** Rayo, campanadas y aparicion de las letras. */
    public static final float BOLT = 0.9f, LETTERS = 1.3f, LINE = 3.0f, SUBTITLE = 3.6f, OUT = 6.6f;
    public static final float[] BELLS = {1.2f, 3.4f, 5.6f};
    /** Grupos de frases de la narracion que van en cada plano de camara (capitulo 1). */
    public static final int[][] GROUPS = {{0, 1}, {2, 4}, {5, 5}, {6, 7}, {8, 9}};
    /** Los del capitulo 5: la sala, el arbol en flor, la tormenta, la llave, el cielo y la despedida. */
    public static final int[][] GROUPS5 = {{0, 0}, {1, 2}, {3, 4}, {5, 5}, {6, 7}, {8, 10}};

    private record Datos(float duracion, float[][] lineas, boolean audio) {}

    private static final Map<Integer, Datos> DATOS = new HashMap<>();

    /** Si el capitulo tiene narrador (voz, subtitulos y planos de camara). */
    public static boolean narrated(int ch) {
        return ch == 1 || ch == 5;
    }

    public static int[][] groups(int ch) {
        return ch == 5 ? GROUPS5 : GROUPS;
    }

    private static synchronized Datos load(int ch) {
        Datos d = DATOS.get(ch);
        if (d != null) return d;
        try (InputStream in = Timeline.class.getResourceAsStream("/assets/darkcraft/cinematica/cap" + ch + ".json")) {
            if (in == null) throw new IllegalStateException("sin tiempos");
            JsonObject o = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray a = o.getAsJsonArray("lineas");
            float[][] l = new float[a.size()][2];
            for (int i = 0; i < a.size(); i++) {
                l[i][0] = a.get(i).getAsJsonArray().get(0).getAsFloat();
                l[i][1] = a.get(i).getAsJsonArray().get(1).getAsFloat();
            }
            d = new Datos(o.get("duracion").getAsFloat(), l, o.has("audio") && o.get("audio").getAsBoolean());
        } catch (Exception e) {
            int n = ch == 5 ? 11 : 10;
            float[][] l = new float[n][2];
            for (int i = 0; i < n; i++) {
                l[i][0] = 0.3f + i * 6f;
                l[i][1] = l[i][0] + 5f;
            }
            d = new Datos(n * 6f + 1f, l, false);
        }
        DATOS.put(ch, d);
        return d;
    }

    /** Inicio y fin (segundos desde que empieza la voz) de cada frase del capitulo 1. */
    public static float[][] lines() {
        return lines(1);
    }

    public static float[][] lines(int ch) {
        return load(ch).lineas();
    }

    public static boolean hasAudio() {
        return hasAudio(1);
    }

    public static boolean hasAudio(int ch) {
        return load(ch).audio();
    }

    public static float narration(int ch) {
        return load(ch).duracion();
    }

    /** Duracion total de la cinematica del capitulo ch. */
    public static float total(int ch) {
        return narrated(ch) ? NARR_START + narration(ch) + 2.6f : TITLE;
    }

    /** Instante del corte entre el plano g y el g+1 del capitulo 1 (segundos desde el principio). */
    public static float cut(int g) {
        return cut(1, g);
    }

    /** Instante del corte entre el plano g y el g+1 del capitulo ch. */
    public static float cut(int ch, int g) {
        float[][] l = lines(ch);
        int[][] gr = groups(ch);
        int last = Math.min(gr[g][1], l.length - 1), first = Math.min(gr[g + 1][0], l.length - 1);
        return NARR_START + (l[last][1] + l[first][0]) / 2f;
    }

    /** Momento en que un capitulo narrado vuelve al jugador (en negro). */
    public static float back(int ch) {
        return total(ch) - 1.8f;
    }
}
