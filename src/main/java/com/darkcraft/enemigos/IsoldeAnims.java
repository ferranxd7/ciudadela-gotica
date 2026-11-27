package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class IsoldeAnims {
    private IsoldeAnims() {}

    public static final int LEVANTARSE = 1;
    public static final int GARRAS = 2;
    public static final int VELO = 3;
    public static final int ABRAZO = 4;
    public static final int APRIETA = 5;
    public static final int NANA = 6;
    public static final int DESVANECE = 7;
    public static final int LAGRIMAS = 8;
    public static final int GRITO = 9;
    public static final int VELO_ARDE = 10;
    public static final int ATURDIDO = 11;
    public static final int MORIR = 12;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 60, 30, 34, 36, 32, 64, 28, 44, 40, 56, 28, 88};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {}, {11, 17}, {17}, {17}, {8, 16, 26}, {56}, {}, {28}, {14}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "levantarse", "garras", "velo", "abrazo", "aprieta", "nana", "desvanece", "lagrimas", "grito", "velo_arde", "aturdido", "morir"};
}
