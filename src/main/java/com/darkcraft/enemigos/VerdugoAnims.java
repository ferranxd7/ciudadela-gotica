package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class VerdugoAnims {
    private VerdugoAnims() {}

    public static final int HACHAZO = 1;
    public static final int BARRIDO = 2;
    public static final int DOBLE = 3;
    public static final int LAZO = 4;
    public static final int EJECUCION = 5;
    public static final int PISOTON = 6;
    public static final int GRITO = 7;
    public static final int SALTO = 8;
    public static final int ATURDIDO = 9;
    public static final int MORIR = 10;
    public static final int LEVANTARSE = 11;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 46, 38, 48, 42, 32, 26, 48, 34, 30, 60, 28};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {23}, {20}, {16, 32}, {18}, {13}, {12}, {}, {21}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "hachazo", "barrido", "doble", "lazo", "ejecucion", "pisoton", "grito", "salto", "aturdido", "morir", "levantarse"};

    public static final float ESCALA = 1.5f;
}
