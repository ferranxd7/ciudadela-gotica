package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class ArqueroAnims {
    private ArqueroAnims() {}

    public static final int DISPARO = 1;
    public static final int PATADA = 2;
    public static final int ARCAZO = 3;
    public static final int ATURDIDO = 4;
    public static final int MORIR = 5;
    public static final int LEVANTARSE = 6;
    public static final int ALZARSE = 7;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 44, 20, 24, 28, 40, 24, 40};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {22}, {8}, {10}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "disparo", "patada", "arcazo", "aturdido", "morir", "levantarse", "alzarse"};
}
