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
    /** Las del Capitan de Brumavela: 40 + N (1..5). */
    public static final int BASE_CAPITAN = 40;
    /** Las de la Molinera de Velmor (parte 7f): 60 + N (1..5). */
    public static final int BASE_MOLINERA = 60;
    /** Las de Maese Odran, el Verdugo de la Arboleda (parte 7h): 70 + N (1..5). */
    public static final int BASE_VERDUGO = 70;
    /** Las del Barquero de la Laguna Negra (parte 7j): 80 + N (1..5). */
    public static final int BASE_BARQUERO = 80;
    /** Las de Hrodric, el Abad de la Cumbre (parte 7l): 90 + N (1..5). */
    public static final int BASE_ABAD = 90;
    /** Las del Condestable del Juramento (parte 7n): 20 + N (1..5; entre las del Rey, 1..13, y las del Capitan). */
    public static final int BASE_CONDESTABLE = 20;
    /** Las de Ysolde, la Rectora de la Academia (parte 7p): 30 + N (1..5; entre las del Condestable y las del Capitan). */
    public static final int BASE_RECTORA = 30;
    /** Las de Brann, el Ballenero (parte 7r): 50 + N (1..5; entre las del Capitan y las de la Molinera). */
    public static final int BASE_BALLENERO = 50;
    /** Pausa entre dos frases seguidas (ticks): 0,3 s. */
    public static final int PAUSA = 6;
    /** Lo que se queda el subtitulo despues de acabar de hablar (ticks): 0,8 s. */
    public static final int SUBTITULO_EXTRA = 16;

    // el segundo en que acaba de hablar en cada frase: los de la ultima vez que se montaron (por si falta el fichero)
    private static final float[] REY = {0, 6.37f, 5.73f, 4.31f, 3.92f, 5.76f, 3.07f, 2.52f, 3.93f, 3.13f, 6.48f, 3.21f, 4.36f, 3.6f};
    private static final float[] GAVRAN = {0, 6.63f, 7.26f, 5.9f, 4.67f, 1.34f, 1.02f, 4.46f, 2.98f, 3.29f, 2.66f, 4.85f, 2.21f,
            3.04f, 4.7f, 4.92f, 3.27f, 4.36f, 6.44f};
    private static final float[] CAPITAN = {0, 7.15f, 3.74f, 2.7f, 2.95f, 4.06f};
    private static final float[] MOLINERA = {0, 9.0f, 6.43f, 3.86f, 4.39f, 4.67f};
    private static final float[] VERDUGO = {0, 5.82f, 4.09f, 2.36f, 3.1f, 5.46f};
    private static final float[] BARQUERO = {0, 5.2f, 3.9f, 2.4f, 1.4f, 4.1f};
    private static final float[] ABAD = {0, 8.25f, 5.2f, 3.5f, 2.7f, 5.3f};
    private static final float[] CONDESTABLE = {0, 7.21f, 2.68f, 1.39f, 3.2f, 4.15f};
    private static final float[] RECTORA = {0, 5.63f, 5.08f, 1.93f, 3.3f, 3.98f};
    private static final float[] BALLENERO = {0, 6.03f, 4.92f, 4.27f, 5.11f, 5.09f};
    // la Sombra en las cinematicas (1..6 la de inicio, 7..9 el viaje a Valdrenn)
    private static final float[] SOMBRA = {0, 2.26f, 4.23f, 3.56f, 6.57f, 5.44f, 3.17f, 6.06f, 5.98f, 4.63f};

    static {
        try (InputStream in = Voces.class.getResourceAsStream("/assets/darkcraft/rey_voces.json")) {
            if (in != null) {
                JsonObject o = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                leer(o, "habla_capitan", CAPITAN);
                leer(o, "habla_molinera", MOLINERA);
                leer(o, "habla_verdugo", VERDUGO);
                leer(o, "habla_barquero", BARQUERO);
                leer(o, "habla_abad", ABAD);
                leer(o, "habla_condestable", CONDESTABLE);
                leer(o, "habla_rectora", RECTORA);
                leer(o, "habla_ballenero", BALLENERO);
                leer(o, "habla_sombra", SOMBRA);
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

    public static boolean esCapitan(int linea) {
        return linea > BASE_CAPITAN && linea < BASE_BALLENERO;
    }

    public static boolean esMolinera(int linea) {
        return linea > BASE_MOLINERA && linea < BASE_VERDUGO;
    }

    public static boolean esVerdugo(int linea) {
        return linea > BASE_VERDUGO && linea < BASE_BARQUERO;
    }

    public static boolean esBarquero(int linea) {
        return linea > BASE_BARQUERO && linea < BASE_ABAD;
    }

    public static boolean esAbad(int linea) {
        return linea > BASE_ABAD && linea < BASE_GAVRAN;
    }

    public static boolean esCondestable(int linea) {
        return linea > BASE_CONDESTABLE && linea < BASE_RECTORA;
    }

    public static boolean esRectora(int linea) {
        return linea > BASE_RECTORA && linea < BASE_CAPITAN;
    }

    public static boolean esBallenero(int linea) {
        return linea > BASE_BALLENERO && linea < BASE_MOLINERA;
    }

    /**
     * Si la linea existe (1..13 el Rey, 21..25 el Condestable, 31..35 la Rectora, 41..45 el Capitan, 51..55 el Ballenero, 61..65 la Molinera, 71..75 el Verdugo, 81..85
     * el Barquero, 91..95 el Abad, 101..118 Gavran).
     */
    public static boolean valida(int linea) {
        if (esCondestable(linea)) return linea - BASE_CONDESTABLE < CONDESTABLE.length;
        if (esRectora(linea)) return linea - BASE_RECTORA < RECTORA.length;
        if (esBallenero(linea)) return linea - BASE_BALLENERO < BALLENERO.length;
        if (esCapitan(linea)) return linea - BASE_CAPITAN < CAPITAN.length;
        if (esMolinera(linea)) return linea - BASE_MOLINERA < MOLINERA.length;
        if (esVerdugo(linea)) return linea - BASE_VERDUGO < VERDUGO.length;
        if (esBarquero(linea)) return linea - BASE_BARQUERO < BARQUERO.length;
        if (esAbad(linea)) return linea - BASE_ABAD < ABAD.length;
        return esGavran(linea) ? linea - BASE_GAVRAN < GAVRAN.length : linea > 0 && linea < REY.length;
    }

    /** Segundos que tarda en decir la frase (hasta que acaba de hablar). */
    public static float seg(int linea) {
        if (esCondestable(linea)) {
            int n = linea - BASE_CONDESTABLE;
            return n > 0 && n < CONDESTABLE.length ? CONDESTABLE[n] : 4f;
        }
        if (esRectora(linea)) {
            int n = linea - BASE_RECTORA;
            return n > 0 && n < RECTORA.length ? RECTORA[n] : 4f;
        }
        if (esBallenero(linea)) {
            int n = linea - BASE_BALLENERO;
            return n > 0 && n < BALLENERO.length ? BALLENERO[n] : 4f;
        }
        if (esCapitan(linea)) {
            int n = linea - BASE_CAPITAN;
            return n > 0 && n < CAPITAN.length ? CAPITAN[n] : 4f;
        }
        if (esMolinera(linea)) {
            int n = linea - BASE_MOLINERA;
            return n > 0 && n < MOLINERA.length ? MOLINERA[n] : 4f;
        }
        if (esVerdugo(linea)) {
            int n = linea - BASE_VERDUGO;
            return n > 0 && n < VERDUGO.length ? VERDUGO[n] : 4f;
        }
        if (esBarquero(linea)) {
            int n = linea - BASE_BARQUERO;
            return n > 0 && n < BARQUERO.length ? BARQUERO[n] : 4f;
        }
        if (esAbad(linea)) {
            int n = linea - BASE_ABAD;
            return n > 0 && n < ABAD.length ? ABAD[n] : 4f;
        }
        if (esGavran(linea)) {
            int n = linea - BASE_GAVRAN;
            return n > 0 && n < GAVRAN.length ? GAVRAN[n] : 4f;
        }
        return linea > 0 && linea < REY.length ? REY[linea] : 4f;
    }

    /** Segundos que tarda la Sombra en decir su frase n (1..9) de las cinematicas. */
    public static float sombra(int n) {
        return n > 0 && n < SOMBRA.length ? SOMBRA[n] : 4f;
    }

    public static int ticks(int linea) {
        return Math.round(seg(linea) * 20);
    }

    /**
     * Frases de pelea (burlas y gritos de un golpe): si al acabar la que suena ya ha pasado su momento, no se dicen.
     * Las demas (las de las cinematicas) siempre se dicen enteras, aunque tengan que esperar un poco.
     */
    public static boolean deRelleno(int linea) {
        if (esCondestable(linea)) {
            int n = linea - BASE_CONDESTABLE;
            return n == 3;
        }
        if (esRectora(linea)) {
            int n = linea - BASE_RECTORA;
            return n == 3 || n == 4;
        }
        if (esBallenero(linea)) {
            int n = linea - BASE_BALLENERO;
            return n == 3 || n == 4;
        }
        if (esCapitan(linea)) {
            int n = linea - BASE_CAPITAN;
            return n == 3 || n == 4;
        }
        if (esMolinera(linea)) {
            int n = linea - BASE_MOLINERA;
            return n == 3 || n == 4;
        }
        if (esVerdugo(linea)) {
            int n = linea - BASE_VERDUGO;
            return n == 3 || n == 4;
        }
        if (esBarquero(linea)) {
            int n = linea - BASE_BARQUERO;
            return n == 3 || n == 4;
        }
        if (esAbad(linea)) {
            int n = linea - BASE_ABAD;
            return n == 3 || n == 4;
        }
        if (esGavran(linea)) {
            int n = linea - BASE_GAVRAN;
            return n == 5 || n == 6 || n == 8 || n == 9 || n == 10 || n == 13 || n == 14 || n == 15;
        }
        return linea == 7 || linea == 8 || linea == 9 || linea == 13;
    }
}
