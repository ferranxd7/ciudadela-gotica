package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class EruditoAnims {
    private EruditoAnims() {}

    public static final int PLUMAZO = 1;
    public static final int TINTERO = 2;
    public static final int ORBE = 3;
    public static final int RUNAS = 4;
    public static final int PARPADEO = 5;
    public static final int TOMAZO = 6;
    public static final int CADENAZO = 7;
    public static final int ATURDIDO = 8;
    public static final int MORIR = 9;
    public static final int LEVANTARSE = 10;
    public static final int ALZARSE = 11;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 22, 26, 30, 32, 24, 32, 30, 24, 44, 30, 20};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {7, 12}, {13}, {15}, {16}, {}, {17}, {14}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "plumazo", "tintero", "orbe", "runas", "parpadeo", "tomazo", "cadenazo", "aturdido", "morir", "levantarse", "alzarse"};
}
