package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class BarqueroAnims {
    private BarqueroAnims() {}

    public static final int REMAZO = 1;
    public static final int BARRIDO = 2;
    public static final int BICHERO = 3;
    public static final int GOLPE_LINTERNA = 4;
    public static final int LINTERNA = 5;
    public static final int GRITO = 6;
    public static final int SALTO = 7;
    public static final int ATURDIDO = 8;
    public static final int MORIR = 9;
    public static final int LEVANTARSE = 10;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 44, 38, 42, 30, 48, 48, 34, 30, 64, 28};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {22}, {20}, {18}, {14}, {21}, {}, {21}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "remazo", "barrido", "bichero", "golpe_linterna", "linterna", "grito", "salto", "aturdido", "morir", "levantarse"};

    public static final float ESCALA = 1.55f;
}
