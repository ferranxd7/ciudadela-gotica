package com.ciudadela.cine;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Tiempos de la cinematica, iguales en el servidor y en el cliente. Los de la narracion del
 * capitulo 1 salen del fichero que genera la CI junto al audio (cinematica/cap1.json).
 */
public final class Timeline {
    private Timeline() {}

    /** Duracion de la animacion de titulo de los capitulos 2 a 5. */
    public static final float TITLE = 8.0f;
    /** En el capitulo 1 la voz empieza al acabar el titulo. */
    public static final float NARR_START = 7.0f;
    /** Rayo, campanadas y aparicion de las letras. */
    public static final float BOLT = 0.9f, LETTERS = 1.3f, LINE = 3.0f, SUBTITLE = 3.6f, OUT = 6.6f;
    public static final float[] BELLS = {1.2f, 3.4f, 5.6f};
    /** Grupos de frases de la narracion que van en cada plano de camara. */
    public static final int[][] GROUPS = {{0, 1}, {2, 4}, {5, 5}, {6, 7}, {8, 9}};

    private static float duration = 60f;
    private static float[][] lines;
    private static boolean audio;
    private static boolean loaded;

    private static synchronized void load() {
        if (loaded) return;
        loaded = true;
        try (InputStream in = Timeline.class.getResourceAsStream("/assets/ciudadela/cinematica/cap1.json")) {
            if (in == null) throw new IllegalStateException("sin tiempos");
            JsonObject o = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            duration = o.get("duracion").getAsFloat();
            audio = o.has("audio") && o.get("audio").getAsBoolean();
            JsonArray a = o.getAsJsonArray("lineas");
            lines = new float[a.size()][2];
            for (int i = 0; i < a.size(); i++) {
                lines[i][0] = a.get(i).getAsJsonArray().get(0).getAsFloat();
                lines[i][1] = a.get(i).getAsJsonArray().get(1).getAsFloat();
            }
        } catch (Exception e) {
            lines = new float[10][2];
            for (int i = 0; i < 10; i++) {
                lines[i][0] = 0.3f + i * 6f;
                lines[i][1] = lines[i][0] + 5f;
            }
            duration = 61f;
        }
    }

    public static float[][] lines() {
        load();
        return lines;
    }

    public static boolean hasAudio() {
        load();
        return audio;
    }

    public static float narration() {
        load();
        return duration;
    }

    /** Duracion total de la cinematica del capitulo ch. */
    public static float total(int ch) {
        return ch == 1 ? NARR_START + narration() + 2.6f : TITLE;
    }

    /** Instante del corte entre el plano g y el g+1 (segundos desde el principio). */
    public static float cut(int g) {
        float[][] l = lines();
        int last = Math.min(GROUPS[g][1], l.length - 1), first = Math.min(GROUPS[g + 1][0], l.length - 1);
        return NARR_START + (l[last][1] + l[first][0]) / 2f;
    }

    /** Momento en que el capitulo 1 vuelve al jugador (en negro). */
    public static float back(int ch) {
        return total(ch) - 1.8f;
    }
}
