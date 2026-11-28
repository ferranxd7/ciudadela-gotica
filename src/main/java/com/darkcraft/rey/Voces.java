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
    /** Las de Brann, el Ballenero (parte 7r): 50 + N (1..5; la 6 y la 7, la luz del muelle, parte 7an; entre las del Capitan y las de la Molinera). */
    public static final int BASE_BALLENERO = 50;
    /** Las de Anselmo, el Pontifice Sangrante (parte 7t): 14 + N (1..5; entre las del Rey, 1..13, y las del Condestable). */
    public static final int BASE_PONTIFICE = 14;
    /** Las de Aldric, el Primer Marcado (parte 7v): 120 + N (1..5; despues de las de Gavran, 101..118). */
    public static final int BASE_ALDRIC = 120;
    /** Las de los gigantes errantes de Morvain (parte 7w): 130 + 3 x gigante + N (N 1..3: Ogmund 131..133, Ulfgar 134..136,
     * Haldor 137..139). */
    public static final int BASE_GIGANTE = 130;
    /** Las de Ser Lucan, el Jinete Nocturno (parte 7x): 140 + N (1..5). */
    public static final int BASE_JINETE = 140;
    /** Las de Pell, el Bufon del Rey (parte 7y): 150 + N (1..5). */
    public static final int BASE_BUFON = 150;
    /** Las de Vorthak, el Dragon de Ceniza (parte 7z): 160 + N (1..5). */
    public static final int BASE_DRAGON = 160;
    /** Las de la reina Isolde, en el salon del trono de la ciudadela (parte 7ab): 170 + N (1..6). */
    public static final int BASE_ISOLDE = 170;
    /** Las de Hakon, el Capataz de las Minas de Hierro Negro (parte 7ar): 180 + N (1..5). */
    public static final int BASE_CAPATAZ = 180;
    /** Las de Fray Osmund, el Sacristan del Osario de San Ulfo (parte 7at): 190 + N (1..5). */
    public static final int BASE_OSMUND = 190;
    /** Las de Maese Galen, el fisico del Lazareto (parte 7av): 200 + N (1..5). */
    public static final int BASE_GALEN = 200;
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
    // (la 6 y la 7: Brann ve la luz del muelle y se va a casa; parte 7an)
    private static final float[] BALLENERO = {0, 6.03f, 4.92f, 4.27f, 5.11f, 5.09f, 5.95f, 7.2f};
    private static final float[] PONTIFICE = {0, 6.5f, 5.5f, 3.0f, 4.0f, 5.5f};
    private static final float[] ALDRIC = {0, 7.0f, 5.4f, 3.6f, 3.9f, 6.6f};
    private static final float[] GIGANTE = {0, 7.4f, 6.8f, 7.0f, 6.8f, 4.8f, 7.6f, 8.2f, 6.8f, 4.2f};
    private static final float[] JINETE = {0, 7.5f, 4.2f, 4.1f, 2.5f, 8.0f};
    private static final float[] BUFON = {0, 8.0f, 5.6f, 6.2f, 1.6f, 7.8f};
    private static final float[] DRAGON = {0, 9.18f, 5.88f, 2.82f, 1.66f, 4.59f};
    private static final float[] ISOLDE = {0, 8.98f, 2.33f, 4.6f, 3.42f, 3.74f, 4.74f};
    private static final float[] CAPATAZ = {0, 5.48f, 7.67f, 5.96f, 3.23f, 6.46f};
    private static final float[] OSMUND = {0, 10.72f, 6.64f, 8.16f, 3.12f, 6.16f};
    private static final float[] GALEN = {0, 9.81f, 6.75f, 4.89f, 3.82f, 5.57f};
    // la Sombra en las cinematicas (1..6 la de inicio, 7..9 el viaje a Valdrenn), en la cadena del sur (21..23, parte 7aa), al
    // entrar en la torre del homenaje (24, parte 7ab) y en la aguja (25..33, parte 7ac: ante la puerta, al despertar, a media vida, si
    // cae su presa, al llamar a los caidos, al rendirse, golpeado o tomado su corazon, y al despertar otra vez); las 10..20 son las del
    // capitulo 6 (van en su narracion)
    private static final float[] SOMBRA = {0, 2.26f, 4.23f, 3.56f, 6.57f, 5.44f, 3.17f, 6.06f, 5.98f, 4.63f,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5.2f, 6.6f, 5.4f, 7.19f, 7.6f, 16.4f, 6.4f, 5.4f, 2.9f, 14.2f, 5.2f, 5.7f, 2.1f};

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
                leer(o, "habla_pontifice", PONTIFICE);
                leer(o, "habla_aldric", ALDRIC);
                leer(o, "habla_gigante", GIGANTE);
                leer(o, "habla_jinete", JINETE);
                leer(o, "habla_bufon", BUFON);
                leer(o, "habla_dragon", DRAGON);
                leer(o, "habla_isolde", ISOLDE);
                leer(o, "habla_capataz", CAPATAZ);
                leer(o, "habla_osmund", OSMUND);
                leer(o, "habla_galen", GALEN);
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
        return linea > BASE_GAVRAN && linea < BASE_ALDRIC;
    }

    public static boolean esAldric(int linea) {
        return linea > BASE_ALDRIC && linea < BASE_ALDRIC + 10;
    }

    /** Las de los gigantes (131..139). */
    public static boolean esGigante(int linea) {
        return linea > BASE_GIGANTE && linea < BASE_GIGANTE + 10;
    }

    /** Las del Jinete Nocturno (141..145). */
    public static boolean esJinete(int linea) {
        return linea > BASE_JINETE && linea < BASE_JINETE + 10;
    }

    /** Las de Pell, el Bufon del Rey (151..155). */
    public static boolean esBufon(int linea) {
        return linea > BASE_BUFON && linea < BASE_BUFON + 10;
    }

    /** Las de Vorthak, el Dragon de Ceniza (161..165). */
    public static boolean esDragon(int linea) {
        return linea > BASE_DRAGON && linea < BASE_DRAGON + 10;
    }

    /** Las de la reina Isolde (171..176). */
    public static boolean esIsolde(int linea) {
        return linea > BASE_ISOLDE && linea < BASE_ISOLDE + 10;
    }

    /** Las de Hakon, el Capataz (181..185). */
    public static boolean esCapataz(int linea) {
        return linea > BASE_CAPATAZ && linea < BASE_CAPATAZ + 10;
    }

    /** Las de Fray Osmund, el Sacristan (191..195). */
    public static boolean esOsmund(int linea) {
        return linea > BASE_OSMUND && linea < BASE_OSMUND + 10;
    }

    /** Las de Maese Galen, el fisico del Lazareto (201..205). */
    public static boolean esGalen(int linea) {
        return linea > BASE_GALEN && linea < BASE_GALEN + 10;
    }

    /** De que gigante es una linea suya (0 Ogmund, 1 Ulfgar, 2 Haldor). */
    public static int gigante(int linea) {
        return Math.max(0, Math.min(2, (linea - BASE_GIGANTE - 1) / 3));
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

    public static boolean esPontifice(int linea) {
        return linea > BASE_PONTIFICE && linea < BASE_CONDESTABLE;
    }

    public static boolean esBallenero(int linea) {
        return linea > BASE_BALLENERO && linea < BASE_MOLINERA;
    }

    /**
     * Si la linea existe (1..13 el Rey, 181..185 el Capataz, 191..195 Fray Osmund, 201..205 Maese Galen, 121..125 Aldric, 21..25 el Condestable, 31..35 la Rectora, 15..19 el Pontifice, 41..45 el Capitan, 51..55 el Ballenero, 61..65 la Molinera, 71..75 el Verdugo, 81..85
     * el Barquero, 91..95 el Abad, 101..118 Gavran).
     */
    public static boolean valida(int linea) {
        if (esJinete(linea)) return linea - BASE_JINETE < JINETE.length;
        if (esBufon(linea)) return linea - BASE_BUFON < BUFON.length;
        if (esDragon(linea)) return linea - BASE_DRAGON < DRAGON.length;
        if (esIsolde(linea)) return linea - BASE_ISOLDE < ISOLDE.length;
        if (esCapataz(linea)) return linea - BASE_CAPATAZ < CAPATAZ.length;
        if (esOsmund(linea)) return linea - BASE_OSMUND < OSMUND.length;
        if (esGalen(linea)) return linea - BASE_GALEN < GALEN.length;
        if (esGigante(linea)) return linea - BASE_GIGANTE < GIGANTE.length;
        if (esAldric(linea)) return linea - BASE_ALDRIC < ALDRIC.length;
        if (esCondestable(linea)) return linea - BASE_CONDESTABLE < CONDESTABLE.length;
        if (esRectora(linea)) return linea - BASE_RECTORA < RECTORA.length;
        if (esBallenero(linea)) return linea - BASE_BALLENERO < BALLENERO.length;
        if (esPontifice(linea)) return linea - BASE_PONTIFICE < PONTIFICE.length;
        if (esCapitan(linea)) return linea - BASE_CAPITAN < CAPITAN.length;
        if (esMolinera(linea)) return linea - BASE_MOLINERA < MOLINERA.length;
        if (esVerdugo(linea)) return linea - BASE_VERDUGO < VERDUGO.length;
        if (esBarquero(linea)) return linea - BASE_BARQUERO < BARQUERO.length;
        if (esAbad(linea)) return linea - BASE_ABAD < ABAD.length;
        return esGavran(linea) ? linea - BASE_GAVRAN < GAVRAN.length : linea > 0 && linea < REY.length;
    }

    /** Segundos que tarda en decir la frase (hasta que acaba de hablar). */
    public static float seg(int linea) {
        if (esJinete(linea)) {
            int n = linea - BASE_JINETE;
            return n > 0 && n < JINETE.length ? JINETE[n] : 4f;
        }
        if (esBufon(linea)) {
            int n = linea - BASE_BUFON;
            return n > 0 && n < BUFON.length ? BUFON[n] : 4f;
        }
        if (esDragon(linea)) {
            int n = linea - BASE_DRAGON;
            return n > 0 && n < DRAGON.length ? DRAGON[n] : 4f;
        }
        if (esIsolde(linea)) {
            int n = linea - BASE_ISOLDE;
            return n > 0 && n < ISOLDE.length ? ISOLDE[n] : 4f;
        }
        if (esCapataz(linea)) {
            int n = linea - BASE_CAPATAZ;
            return n > 0 && n < CAPATAZ.length ? CAPATAZ[n] : 4f;
        }
        if (esOsmund(linea)) {
            int n = linea - BASE_OSMUND;
            return n > 0 && n < OSMUND.length ? OSMUND[n] : 4f;
        }
        if (esGalen(linea)) {
            int n = linea - BASE_GALEN;
            return n > 0 && n < GALEN.length ? GALEN[n] : 4f;
        }
        if (esGigante(linea)) {
            int n = linea - BASE_GIGANTE;
            return n > 0 && n < GIGANTE.length ? GIGANTE[n] : 4f;
        }
        if (esAldric(linea)) {
            int n = linea - BASE_ALDRIC;
            return n > 0 && n < ALDRIC.length ? ALDRIC[n] : 4f;
        }
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
        if (esPontifice(linea)) {
            int n = linea - BASE_PONTIFICE;
            return n > 0 && n < PONTIFICE.length ? PONTIFICE[n] : 4f;
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
        if (esJinete(linea)) return linea - BASE_JINETE == 4;
        if (esDragon(linea)) {
            int n = linea - BASE_DRAGON;
            return n == 3 || n == 4;
        }
        if (esIsolde(linea)) {
            int n = linea - BASE_ISOLDE;
            return n == 2 || n == 4;
        }
        if (esCapataz(linea)) {
            int n = linea - BASE_CAPATAZ;
            return n == 3 || n == 4;
        }
        if (esOsmund(linea)) {
            int n = linea - BASE_OSMUND;
            return n == 3 || n == 4;
        }
        if (esGalen(linea)) {
            int n = linea - BASE_GALEN;
            return n == 3 || n == 4;
        }
        if (esBufon(linea)) {
            int n = linea - BASE_BUFON;
            return n == 3 || n == 4;
        }
        if (esAldric(linea)) {
            int n = linea - BASE_ALDRIC;
            return n == 3 || n == 4;
        }
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
        if (esPontifice(linea)) {
            int n = linea - BASE_PONTIFICE;
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
