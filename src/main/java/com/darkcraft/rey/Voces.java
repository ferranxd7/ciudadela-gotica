package com.darkcraft.rey;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Lo que dura cada frase de los jefes (la del audio: la apunta la CI en rey_voces.json al montar las voces). Lo usan
 * el servidor, para que una frase no empiece hasta que acabe la anterior y la pelea no empiece hasta que acabe de
 * hablar, y el cliente, para los subtitulos y la cola de frases.
 */
public final class Voces {
    private Voces() {}

    /** Frases de Gavran en el paquete de voz: 100 + N (las del Rey van de 1 a 13). */
    public static final int BASE_GAVRAN = 100;
    /** Pausa entre dos frases seguidas (ticks). */
    public static final int PAUSA = 24;
    /** Lo que se queda el subtitulo despues de acabar la frase (ticks). */
    public static final int SUBTITULO_EXTRA = 55;

    // las de la ultima vez que se montaron (por si falta el fichero)
    private static final float[] REY = {0, 6.9f, 6.2f, 4.7f, 4.3f, 6.2f, 3.63f, 2.83f, 4.3f, 3.65f, 7.0f, 3.74f, 4.9f, 4.2f};
    private static final float[] GAVRAN = {0, 7.1f, 7.8f, 6.3f, 5.2f, 1.94f, 1.38f, 4.6f, 3.3f, 3.54f, 3.22f, 5.4f, 2.69f,
            3.54f, 5.4f, 5.4f, 4.0f, 4.9f, 6.9f};

    static {
        try (InputStream in = Voces.class.getResourceAsStream("/assets/darkcraft/rey_voces.json")) {
            if (in != null) {
                JsonObject o = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                leer(o, "duraciones", REY);
                leer(o, "gavran", GAVRAN);
            }
        } catch (Exception ignored) {
        }
    }

    private static void leer(JsonObject o, String clave, float[] d) {
        if (!o.has(clave)) return;
        JsonObject t = o.getAsJsonObject(clave);
        for (String k : t.keySet()) {
            int i = Integer.parseInt(k);
            if (i > 0 && i < d.length) d[i] = t.get(k).getAsFloat();
        }
    }

    public static boolean esGavran(int linea) {
        return linea > BASE_GAVRAN;
    }

    /** Si la linea existe (1..13 el Rey, 101..118 Gavran). */
    public static boolean valida(int linea) {
        return esGavran(linea) ? linea - BASE_GAVRAN < GAVRAN.length : linea > 0 && linea < REY.length;
    }

    /** Segundos que dura la frase. */
    public static float seg(int linea) {
        if (esGavran(linea)) {
            int n = linea - BASE_GAVRAN;
            return n > 0 && n < GAVRAN.length ? GAVRAN[n] : 4f;
        }
        return linea > 0 && linea < REY.length ? REY[linea] : 4f;
    }

    public static int ticks(int linea) {
        return Math.round(seg(linea) * 20);
    }

    /**
     * Frases de pelea (burlas y gritos de un golpe): si al acabar la que suena ya ha pasado su momento, no se dicen.
     * Las demas (las de las cinematicas) siempre se dicen enteras, aunque tengan que esperar un poco.
     */
    public static boolean deRelleno(int linea) {
        if (esGavran(linea)) {
            int n = linea - BASE_GAVRAN;
            return n == 5 || n == 6 || n == 8 || n == 9 || n == 10 || n == 13 || n == 14 || n == 15;
        }
        return linea == 7 || linea == 8 || linea == 9 || linea == 13;
    }
}
