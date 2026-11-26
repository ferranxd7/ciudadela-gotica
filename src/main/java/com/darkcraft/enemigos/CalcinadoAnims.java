package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class CalcinadoAnims {
    private CalcinadoAnims() {}

    public static final int TAJO = 1;
    public static final int ESCUDAZO = 2;
    public static final int BLOQUEO = 3;
    public static final int LANZAR = 4;
    public static final int TEA = 5;
    public static final int AGARRON = 6;
    public static final int ZARPAZO = 7;
    public static final int ATURDIDO = 8;
    public static final int MORIR = 9;
    public static final int LEVANTARSE = 10;
    public static final int ALZARSE = 11;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 32, 26, 12, 30, 22, 36, 20, 30, 44, 24, 32};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {19}, {12}, {}, {17}, {11}, {14}, {9}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "tajo", "escudazo", "bloqueo", "lanzar", "tea", "agarron", "zarpazo", "aturdido", "morir", "levantarse", "alzarse"};
}
