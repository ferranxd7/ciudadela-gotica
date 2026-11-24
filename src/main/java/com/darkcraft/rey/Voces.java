package com.darkcraft.rey;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Lo que tarda en decirse cada frase de los jefes: el segundo en que acaba de hablar de verdad (sin el silencio del
 * final del audio ni la cola del eco; lo apunta la CI en rey_voces.json al montar las voces). Lo usan el servidor, para
 * que una frase no empiece hasta que acabe la anterior y la pelea no empiece hasta que acabe de hablar, y el cliente,
 * para los subtitulos y la cola de frases. El usuario pidio que no haya pausas largas: en cuanto acaba de hablar, lo
 * siguiente llega enseguida (0,3 s) y el subtitulo se va a los 0,8 s.
 */
public final class Voces {
    private Voces() {}

    /** Frases de Gavran en el paquete de voz: 100 + N (las del Rey van de 1 a 13). */
    public static final int BASE_GAVRAN = 100;
    /** Pausa entre dos frases seguidas (ticks): 0,3 s. */
    public static final int PAUSA = 6;
    /** Lo que se queda el subtitulo despues de acabar de hablar (ticks): 0,8 s. */
    public static final int SUBTITULO_EXTRA = 16;

    // el segundo en que acaba de hablar en cada frase: los de la ultima vez que se montaron (por si falta el fichero)
    private static final float[] REY = {0, 6.37f, 5.73f, 4.31f, 3.92f, 5.76f, 3.07f, 2.52f, 3.93f, 3.13f, 6.48f, 3.21f, 4.36f, 3.6f};
    private static final float[] GAVRAN = {0, 6.63f, 7.26f, 5.9f, 4.67f, 1.34f, 1.02f, 4.46f, 2.98f, 3.29f, 2.66f, 4.85f, 2.21f,
            3.04f, 4.7f, 4.92f, 3.27f, 4.36f, 6.44f};

    static {
        try (InputStream in = Voces.class.getResourceAsStream("/assets/darkcraft/rey_voces.json")) {
            if (in != null) {
                JsonObject o = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                if (o.has("habla")) {
                    leer(o, "habla", REY);
                    leer(o, "habla_gavran", GAVRAN);
                } else {
                    // un fichero de antes: solo trae lo que dura el audio (con medio segundo de cola)
                    leer(o, "duraciones", REY);
                    leer(o, "gavran", GAVRAN);
                    for (int i = 1; i < REY.length; i++) REY[i] = Math.max(0.6f, REY[i] - 0.45f);
                    for (int i = 1; i < GAVRAN.length; i++) GAVRAN[i] = Math.max(0.6f, GAVRAN[i] - 0.45f);
                }
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

    /** Segundos que tarda en decir la frase (hasta que acaba de hablar). */
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
