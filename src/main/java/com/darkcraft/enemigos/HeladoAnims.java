package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class HeladoAnims {
    private HeladoAnims() {}

    public static final int INCENSARIO = 1;
    public static final int ALIENTO = 2;
    public static final int MARTILLAZO = 3;
    public static final int BARRIDO = 4;
    public static final int BASTONAZO = 5;
    public static final int ESQUIRLAS = 6;
    public static final int ATURDIDO = 7;
    public static final int MORIR = 8;
    public static final int LEVANTARSE = 9;
    public static final int ALZARSE = 10;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 32, 44, 44, 36, 28, 40, 30, 48, 32, 32};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {16}, {17}, {23}, {19}, {12}, {19}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "incensario", "aliento", "martillazo", "barrido", "bastonazo", "esquirlas", "aturdido", "morir", "levantarse", "alzarse"};
}
