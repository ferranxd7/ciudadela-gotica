package com.darkcraft.rol;

import java.util.Locale;

/**
 * Los ocho atributos del rol (como en Elden Ring). De momento hacen esto: Vigor, la vida; Mente, el mana; Aguante, la
 * estamina; Fuerza y Destreza, lo que pegan las armas. Inteligencia, Fe y Arcano se guardan ya para los hechizos y los
 * objetos que vendran (su efecto llega con ellos).
 */
public enum Atributo {
    VIGOR, MENTE, AGUANTE, FUERZA, DESTREZA, INTELIGENCIA, FE, ARCANO;

    /** Con lo que se empieza en cada uno (hasta que haya clases) y lo maximo que se puede subir. */
    public static final int BASE = 10, TOPE = 99;
    public static final int N = values().length;

    /** "vigor", "mente"... (para las claves de los textos y los iconos). */
    public final String clave = name().toLowerCase(Locale.ROOT);

    public static int[] deBase() {
        int[] a = new int[N];
        java.util.Arrays.fill(a, BASE);
        return a;
    }
}
