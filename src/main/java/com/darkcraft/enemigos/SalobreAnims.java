package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class SalobreAnims {
    private SalobreAnims() {}

    public static final int PLOMADA = 1;
    public static final int RED = 2;
    public static final int GARFIO = 3;
    public static final int BICHAZO = 4;
    public static final int RASTRILLAZO = 5;
    public static final int SAL = 6;
    public static final int ATURDIDO = 7;
    public static final int MORIR = 8;
    public static final int LEVANTARSE = 9;
    public static final int ALZARSE = 10;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 26, 32, 32, 34, 36, 26, 30, 34, 26, 32};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {12}, {18}, {15}, {17}, {18}, {13}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "plomada", "red", "garfio", "bichazo", "rastrillazo", "sal", "aturdido", "morir", "levantarse", "alzarse"};
}
