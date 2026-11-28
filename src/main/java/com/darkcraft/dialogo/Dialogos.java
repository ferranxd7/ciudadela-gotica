package com.darkcraft.dialogo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Las conversaciones con los personajes (las mismas en el servidor y en el cliente). Cada personaje tiene sus nodos: una
 * frase (con su voz {@code darkcraft:voz.<personaje>.<n>} y su subtitulo {@code darkcraft.dialogo.<personaje>.<n>}, lo
 * que viene despues y, a veces, algo que pasa al acabarla) o un menu de respuestas. El servidor decide por donde empieza
 * ({@link Conversaciones}); el cliente lo recorre ({@code client.ui.DialogoScreen}) y solo avisa al servidor de lo que
 * tiene que hacer algo (subir de nivel...).
 */
public final class Dialogos {
    private Dialogos() {}

    /**
     * Lo que puede pasar al acabar una frase o al elegir una respuesta: subir de nivel (la guia), la forja (el herrero),
     * la tienda (el mercader), el silbato del corcel (la guia, en Morvain), adonde ira Silas (parte 7af) o lo que se responde a quien
     * da una mision (parte 7ag: aceptarla, preguntar que se gana —o, con Hedda, por aquella noche—, rechazarla, devolver lo que se
     * busco o quedarselo; parte 7ah: decirle la verdad, mentirle o callar). Su nombre en minusculas es lo que manda el cliente
     * ({@code Net.DialogoAccion}).
     */
    public enum Accion {
        NADA, NIVEL, FORJA, TIENDA, SILBATO, GOLPEAR, TOMAR, RUMBO, ACEPTAR, PRECIO, RECHAZAR, DEVOLVER, QUEDAR, VERDAD, MENTIRA, CALLAR,
        /** Con Sigrun (parte 7ap): empezar el duelo (al acabar su frase 2), acabar con ella (al acabar la 8) o dejarla marchar (la 9). */
        DUELO, REMATAR, SOLTAR;

        public String id() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    /**
     * Fin de la conversacion; el menu de respuestas de cada personaje es el nodo 100 (los que tienen mas de uno, como quien da una
     * mision, los siguen en el 101, 102...).
     */
    public static final int FIN = 0, MENU = 100;

    /**
     * Una respuesta del menu: su texto, a que nodo lleva y, a veces, algo que pasa al elegirla (abrir la forja...). Si lleva accion y
     * tambien a donde ir (las de las misiones), el servidor apunta lo elegido y la conversacion sigue por ahi.
     */
    public record Respuesta(String clave, int destino, Accion accion) {}

    public record Nodo(int id, int voz, int siguiente, Accion accion, Respuesta[] respuestas) {
        public boolean menu() {
            return respuestas != null;
        }
    }

    /** Un personaje: su id (texturas, voces, textos) y sus nodos. */
    public record Personaje(String id, Map<Integer, Nodo> nodos) {
        public Nodo nodo(int n) {
            return nodos.get(n);
        }

        public String nombre() {
            return "darkcraft.personaje." + id;
        }

        public String titulo() {
            return "darkcraft.personaje." + id + ".titulo";
        }

        public String linea(int voz) {
            return "darkcraft.dialogo." + id + "." + voz;
        }

        public String sonido(int voz) {
            return "voz." + id + "." + voz;
        }
    }

    private static final Map<String, Personaje> TODOS = new HashMap<>();

    public static Personaje de(String id) {
        return TODOS.get(id);
    }

    private static final class Hacer {
        final String id;
        final Map<Integer, Nodo> nodos = new HashMap<>();

        Hacer(String id) {
            this.id = id;
        }

        Hacer frase(int voz, int siguiente) {
            return frase(voz, siguiente, Accion.NADA);
        }

        Hacer frase(int voz, int siguiente, Accion a) {
            nodos.put(voz, new Nodo(voz, voz, siguiente, a, null));
            return this;
        }

        /** Una frase con otro numero de nodo que el de su voz (la misma frase seguida de otra cosa). */
        Hacer frase(int id, int voz, int siguiente) {
            nodos.put(id, new Nodo(id, voz, siguiente, Accion.NADA, null));
            return this;
        }

        Hacer menu(Respuesta... r) {
            return menu(MENU, r);
        }

        Hacer menu(int id, Respuesta... r) {
            nodos.put(id, new Nodo(id, 0, FIN, Accion.NADA, r));
            return this;
        }

        Personaje hecho() {
            Personaje p = new Personaje(id, Map.copyOf(nodos));
            TODOS.put(id, p);
            return p;
        }
    }

    private static Respuesta r(String pj, String clave, int destino) {
        return new Respuesta("darkcraft.dialogo." + pj + ".op." + clave, destino, Accion.NADA);
    }

    /** Una respuesta que hace algo al elegirla (y cierra la conversacion). */
    private static Respuesta r(String pj, String clave, Accion a) {
        return new Respuesta("darkcraft.dialogo." + pj + ".op." + clave, FIN, a);
    }

    /** Una respuesta que el servidor apunta (las de las misiones) y despues sigue por {@code destino}. */
    private static Respuesta r(String pj, String clave, int destino, Accion a) {
        return new Respuesta("darkcraft.dialogo." + pj + ".op." + clave, destino, a);
    }

    /**
     * Maelis, la guia de los santuarios. La primera vez: 1 → 2 → 3 → menu. Despues: 8 (o 9 tras vencer al Rey, 10 tras
     * vencer a Ser Gavran, una vez cada una) → menu. En Morvain (parte 7b), la primera vez: 11 → 12 (al acabarla da el
     * silbato del corcel) → 13 → menu; si se ha perdido el silbato, 12 → 13 otra vez; las demas, 14. En la capilla negra de la
     * torre del homenaje (parte 7ab), junto a la Ultima Llama: la primera vez 15 → 16 → menu; despues 16 (con la reina ya caida,
     * 14). El menu: la Sombra (4), por que sigo vivo (5), hacerme mas fuerte (6, y se abre subir de nivel) y despedirse (7). Despues
     * del final (parte 7ad), su epilogo: el alba 17 → 18 → 19 → 20; la nueva Sombra 21 → 22 → 23 (la 17 y la 21 las dice en la
     * cinematica del final: quien la vio empieza por la 18 o la 22).
     */
    public static final Personaje GUIA = new Hacer("guia")
            .frase(1, 2).frase(2, 3).frase(3, MENU)
            .frase(4, MENU).frase(5, MENU)
            .frase(6, FIN, Accion.NIVEL).frase(7, FIN)
            .frase(8, MENU).frase(9, MENU).frase(10, MENU)
            .frase(11, 12).frase(12, 13, Accion.SILBATO).frase(13, MENU).frase(14, MENU)
            .frase(15, 16).frase(16, MENU)
            .frase(17, 18).frase(18, 19).frase(19, 20).frase(20, FIN)
            .frase(21, 22).frase(22, 23).frase(23, FIN)
            .menu(r("guia", "sombra", 4), r("guia", "vivo", 5), r("guia", "fuerte", 6), r("guia", "adios", 7))
            .hecho();

    /**
     * Dorvan, el herrero. La primera vez se presenta (1), despues saluda (2); el menu: mejorar un arma (abre la forja, que
     * le hace decir la 3 y, segun vaya, la 4, la 5 o la 8), lo que paso en Valdrenn (6) y despedirse (7).
     */
    public static final Personaje HERRERO = new Hacer("herrero")
            .frase(1, MENU).frase(2, MENU).frase(6, MENU).frase(7, FIN)
            .menu(r("herrero", "mejorar", Accion.FORJA), r("herrero", "valdrenn", 6), r("herrero", "adios", 7))
            .hecho();

    /**
     * Kerrick, el mercader. La primera vez se presenta (1), despues saluda (2); el menu: ver la mercancia (abre la tienda,
     * que le hace decir la 3 al comprar y la 4 si no llegan los ecos), que hace aqui (5) y despedirse (6).
     */
    public static final Personaje MERCADER = new Hacer("mercader")
            .frase(1, MENU).frase(2, MENU).frase(5, MENU).frase(6, FIN)
            .menu(r("mercader", "comprar", Accion.TIENDA), r("mercader", "aqui", 5), r("mercader", "adios", 6))
            .hecho();

    /**
     * Hagen, el herrero del Refugio de la Fragua (parte 7af), junto a la Encrucijada de los Peregrinos. Como Dorvan: se presenta
     * (1) y luego saluda (2); la forja le hace decir la 3 al abrir, la 4 al mejorar o poner una ceniza, la 5 si falta algo y la 8
     * al maximo. El menu: mejorar, las cenizas de guerra (9), por que esta aqui (6) y despedirse (7).
     */
    public static final Personaje HAGEN = new Hacer("hagen")
            .frase(1, MENU).frase(2, MENU).frase(6, MENU).frase(9, MENU).frase(7, FIN)
            .menu(r("hagen", "mejorar", Accion.FORJA), r("hagen", "cenizas", 9), r("hagen", "refugio", 6), r("hagen", "adios", 7))
            .hecho();

    /** Los herreros de los campamentos de Morvain (parte 7af): Brenna, Ulrik y Tobin. Los mismos numeros que Hagen, sin la 9. */
    private static Personaje campamento(String id) {
        return new Hacer(id)
                .frase(1, MENU).frase(2, MENU).frase(6, MENU).frase(7, FIN)
                .menu(r(id, "mejorar", Accion.FORJA), r(id, "historia", 6), r(id, "adios", 7))
                .hecho();
    }

    public static final Personaje BRENNA = campamento("brenna"), ULRIK = campamento("ulrik"), TOBIN = campamento("tobin");

    /**
     * Silas, el mercader ambulante de Morvain (parte 7af): se presenta (1) y luego saluda (2); la tienda le hace decir la 3 al
     * comprar y la 4 si no llegan los ecos. El menu: la mercancia, adonde ira (5; al acabarla, un aviso con el santuario
     * siguiente de su ruta: {@code morvain.Ambulante.rumbo}) y despedirse (6).
     */
    public static final Personaje SILAS = new Hacer("silas")
            .frase(1, MENU).frase(2, MENU).frase(5, MENU, Accion.RUMBO).frase(6, FIN)
            .menu(r("silas", "comprar", Accion.TIENDA), r("silas", "adonde", 5), r("silas", "adios", 6))
            .hecho();

    /**
     * Oswin, el peregrino ciego de la Encrucijada (parte 7ag, la mision de la campana; {@code mision.Oswin}). La primera vez se
     * presenta (1); si se le dejo con la palabra en la boca, 6. Su menu: buscarla (2, la mision empieza), que se gana (3, y otra vez
     * el menu) o no (4). Con la mision en marcha y sin la campana, 5. Con la campana: 7 y el segundo menu, devolversela (8; si no se
     * pregunto que se ganaba, 9 despues: las limosnas de su orden) o quedarsela (10); para lo segundo, el servidor empieza por el nodo
     * 107 (la misma frase 7, con el menu 102). Despues, 12 (se la devolvio) o 11 (se la quedo).
     */
    public static final Personaje OSWIN = new Hacer("oswin")
            .frase(1, MENU).frase(6, MENU).frase(3, MENU)
            .frase(2, FIN).frase(4, FIN).frase(5, FIN)
            .frase(7, MENU + 1).frase(107, 7, MENU + 2)
            .frase(8, FIN).frase(108, 8, 9).frase(9, FIN)
            .frase(10, FIN).frase(11, FIN).frase(12, FIN)
            .menu(r("oswin", "buscar", 2, Accion.ACEPTAR), r("oswin", "precio", 3, Accion.PRECIO), r("oswin", "no", 4, Accion.RECHAZAR))
            .menu(MENU + 1, r("oswin", "devolver", 8, Accion.DEVOLVER), r("oswin", "quedar", 10, Accion.QUEDAR))
            .menu(MENU + 2, r("oswin", "devolver", 108, Accion.DEVOLVER), r("oswin", "quedar", 10, Accion.QUEDAR))
            .hecho();

    /**
     * Hedda, la madre de la Puerta Oeste (parte 7ah, la mision de la bufanda roja; {@code mision.Hedda}). La primera vez se presenta
     * (1); si se le dejo con la palabra en la boca, 6. Su menu: buscarle (2, la mision empieza), aquella noche (3, y otra vez el menu)
     * o no (4). Con la mision en marcha y sin la bufanda, 5. Con la bufanda: 7 (o 14 si no la conocia) y el segundo menu, que acaba
     * la mision: la verdad (8 y 9), la mentira (10) o callar (11). Despues de callar, 12; la Hedda de rodillas junto a Tam (la de quien
     * le dijo la verdad), 13.
     */
    public static final Personaje HEDDA = new Hacer("hedda")
            .frase(1, MENU).frase(6, MENU).frase(3, MENU)
            .frase(2, FIN).frase(4, FIN).frase(5, FIN)
            .frase(7, MENU + 1).frase(14, MENU + 1)
            .frase(8, 9).frase(9, FIN).frase(10, FIN).frase(11, FIN)
            .frase(12, FIN).frase(13, FIN)
            .menu(r("hedda", "buscar", 2, Accion.ACEPTAR), r("hedda", "noche", 3, Accion.PRECIO), r("hedda", "no", 4, Accion.RECHAZAR))
            .menu(MENU + 1, r("hedda", "verdad", 8, Accion.VERDAD), r("hedda", "mentira", 10, Accion.MENTIRA), r("hedda", "callar", 11, Accion.CALLAR))
            .hecho();

    /**
     * Ylva, la hija del ballenero (parte 7an, la mision de la luz del muelle; {@code mision.Ylva}). La primera vez se presenta (1); si se
     * le dejo con la palabra en la boca, 6. Su menu: llevarle su luz (2, la mision empieza y te da su farol), aquella noche (3, y otra vez
     * el menu) o no (4). Con la mision en marcha y sin el farol de Brann, 5. Con el: 7 (cayo peleando), 8 (vio la luz) o 13 (si no la
     * conocia) y el segundo menu (101), que acaba la mision: darselo (9 y 10) o quedartelo (11). Despues de quedartelo, 12.
     */
    public static final Personaje YLVA = new Hacer("ylva")
            .frase(1, MENU).frase(6, MENU).frase(3, MENU)
            .frase(2, FIN).frase(4, FIN).frase(5, FIN)
            .frase(7, MENU + 1).frase(8, MENU + 1).frase(13, MENU + 1)
            .frase(9, 10).frase(10, FIN).frase(11, FIN).frase(12, FIN)
            .menu(r("ylva", "buscar", 2, Accion.ACEPTAR), r("ylva", "noche", 3, Accion.PRECIO), r("ylva", "no", 4, Accion.RECHAZAR))
            .menu(MENU + 1, r("ylva", "dar", 9, Accion.DEVOLVER), r("ylva", "quedar", 11, Accion.QUEDAR))
            .hecho();

    /**
     * Corwen, el farolero de los Pilotes (parte 7ao, la mision de las balizas de la niebla; {@code mision.Corwen}). La primera vez se
     * presenta (1); si se le dejo con la palabra en la boca, 6. Su menu: encender las balizas (2, la mision empieza y te da su
     * yesquero), aquella noche (3, y otra vez el menu) o ahora no (4). Con la mision en marcha y alguna apagada, 5. Con las tres
     * encendidas, 7 y el segundo menu (101), que acaba la mision: que vaya a recibir las barcas (8) o que se quede a guardar la luz
     * (9). Despues de quedarse, 10.
     */
    public static final Personaje CORWEN = new Hacer("corwen")
            .frase(1, MENU).frase(6, MENU).frase(3, MENU)
            .frase(2, FIN).frase(4, FIN).frase(5, FIN)
            .frase(7, MENU + 1)
            .frase(8, FIN).frase(9, FIN).frase(10, FIN)
            .menu(r("corwen", "buscar", 2, Accion.ACEPTAR), r("corwen", "noche", 3, Accion.PRECIO), r("corwen", "no", 4, Accion.RECHAZAR))
            .menu(MENU + 1, r("corwen", "ir", 8, Accion.DEVOLVER), r("corwen", "quedar", 9, Accion.QUEDAR))
            .hecho();

    /**
     * Sigrun, la Guardiana del Paso (parte 7ap, el duelo del Paso del Colmillo; {@code mision.Duelo}): no es de los personajes de siempre
     * (es un enemigo con el que se habla). La primera vez se presenta (1); las demas, 5. Su menu: el duelo (2; al acabarla empieza el
     * duelo: {@link Accion#DUELO}), por que sigue aqui (3, y otra vez el menu) o no (4). Rendida, de rodillas, 7 y el segundo menu (101):
     * acabar con ella (8; al acabarla, cae: {@link Accion#REMATAR}) o dejarla marchar (9; al acabarla se va: {@link Accion#SOLTAR}). Las
     * frases 6 (se rinde), 10 (has caido) y 11 (a media vida) las dice en el mundo, en el duelo ({@code Net.Cartel.VOZ}).
     */
    public static final Personaje SIGRUN = new Hacer("sigrun")
            .frase(1, MENU).frase(5, MENU).frase(3, MENU)
            .frase(2, FIN, Accion.DUELO).frase(4, FIN)
            .frase(7, MENU + 1)
            .frase(8, FIN, Accion.REMATAR).frase(9, FIN, Accion.SOLTAR)
            .frase(6, FIN).frase(10, FIN).frase(11, FIN)
            .menu(r("sigrun", "duelo", 2), r("sigrun", "porque", 3, Accion.PRECIO), r("sigrun", "no", 4, Accion.RECHAZAR))
            .menu(MENU + 1, r("sigrun", "rematar", 8), r("sigrun", "soltar", 9))
            .hecho();

    /**
     * La Sombra sin Nombre, rendida (parte 7ac): se habla con su corazon, que flota delante de ella. Quien no la oyo ofrecerlo empieza
     * por su frase 30 ("Espera... espera, marcado. Toma mi corazon..."); quien la oyo, por las respuestas: golpearlo, tomarlo o callar.
     */
    public static final Personaje SOMBRA = new Hacer("sombra")
            .frase(30, MENU)
            .menu(r("sombra", "golpear", Accion.GOLPEAR), r("sombra", "tomar", Accion.TOMAR), r("sombra", "callar", FIN))
            .hecho();

    // ------------------------------------------------------------------ lo que dura cada frase

    private static Map<String, Float> habla;

    /**
     * Los segundos que tarda en decir la frase (lo apunta la CI en {@code voces.json} al montar las voces), o -1 si no se
     * sabe. Con eso el texto se escribe al ritmo de la voz.
     */
    public static float habla(String personaje, int voz) {
        if (habla == null) {
            Map<String, Float> m = new HashMap<>();
            try (InputStream in = Dialogos.class.getResourceAsStream("/assets/darkcraft/voces.json")) {
                if (in != null) {
                    com.google.gson.JsonObject o = com.google.gson.JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                    for (String pj : o.keySet())
                        for (Map.Entry<String, com.google.gson.JsonElement> e : o.getAsJsonObject(pj).entrySet())
                            m.put(pj + "." + e.getKey(), e.getValue().getAsFloat());
                }
            } catch (Exception ignorado) {
                // sin el fichero, el texto va a su ritmo de siempre
            }
            habla = m;
        }
        return habla.getOrDefault(personaje + "." + voz, -1f);
    }
}
