package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class PodridoAnims {
    private PodridoAnims() {}

    public static final int ZARPAZO = 1;
    public static final int MORDISCO = 2;
    public static final int VOMITO = 3;
    public static final int GOLPE_FATUO = 4;
    public static final int LANZAR_FATUO = 5;
    public static final int APLASTAR = 6;
    public static final int ERUCTO = 7;
    public static final int ALZARSE = 8;
    public static final int LEVANTARSE = 9;
    public static final int ATURDIDO = 10;
    public static final int MORIR = 11;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 26, 30, 38, 26, 34, 48, 40, 36, 24, 30, 48};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {11}, {14}, {15}, {11}, {16}, {20}, {19}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "zarpazo", "mordisco", "vomito", "golpe_fatuo", "lanzar_fatuo", "aplastar", "eructo", "alzarse", "levantarse", "aturdido", "morir"};
}
