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

    /** Lo que puede pasar al acabar una frase. */
    public enum Accion { NADA, NIVEL }

    /** Fin de la conversacion; el menu de respuestas de cada personaje es el nodo 100. */
    public static final int FIN = 0, MENU = 100;

    public record Respuesta(String clave, int destino) {}

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

        Hacer menu(Respuesta... r) {
            nodos.put(MENU, new Nodo(MENU, 0, FIN, Accion.NADA, r));
            return this;
        }

        Personaje hecho() {
            Personaje p = new Personaje(id, Map.copyOf(nodos));
            TODOS.put(id, p);
            return p;
        }
    }

    private static Respuesta r(String pj, String clave, int destino) {
        return new Respuesta("darkcraft.dialogo." + pj + ".op." + clave, destino);
    }

    /**
     * Maelis, la guia de los santuarios. La primera vez: 1 → 2 → 3 → menu. Despues: 8 (o 9 tras vencer al Rey, 10 tras
     * vencer a Ser Gavran, una vez cada una) → menu. El menu: la Sombra (4), por que sigo vivo (5), hacerme mas fuerte
     * (6, y se abre subir de nivel) y despedirse (7).
     */
    public static final Personaje GUIA = new Hacer("guia")
            .frase(1, 2).frase(2, 3).frase(3, MENU)
            .frase(4, MENU).frase(5, MENU)
            .frase(6, FIN, Accion.NIVEL).frase(7, FIN)
            .frase(8, MENU).frase(9, MENU).frase(10, MENU)
            .menu(r("guia", "sombra", 4), r("guia", "vivo", 5), r("guia", "fuerte", 6), r("guia", "adios", 7))
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
