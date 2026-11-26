package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class AhorcadoAnims {
    private AhorcadoAnims() {}

    public static final int ZARPAZO = 1;
    public static final int DOBLE = 2;
    public static final int SALTO = 3;
    public static final int LATIGAZO = 4;
    public static final int LAZO = 5;
    public static final int CAER = 6;
    public static final int ATURDIDO = 7;
    public static final int MORIR = 8;
    public static final int LEVANTARSE = 9;
    public static final int ALZARSE = 10;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 22, 30, 32, 32, 40, 28, 30, 48, 32, 40};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {11}, {10, 19}, {21}, {17}, {18}, {12}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "zarpazo", "doble", "salto", "latigazo", "lazo", "caer", "aturdido", "morir", "levantarse", "alzarse"};
}
