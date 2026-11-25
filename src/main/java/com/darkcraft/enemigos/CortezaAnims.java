package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class CortezaAnims {
    private CortezaAnims() {}

    public static final int ZARPAZO = 1;
    public static final int DOBLE = 2;
    public static final int GOLPE_SUELO = 3;
    public static final int ESPINAS = 4;
    public static final int RAICES = 5;
    public static final int RUGIDO = 6;
    public static final int ATURDIDO = 7;
    public static final int MORIR = 8;
    public static final int LEVANTARSE = 9;
    public static final int ALZARSE = 10;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 28, 34, 44, 38, 48, 40, 32, 60, 40, 32};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {13}, {17}, {22}, {19}, {23}, {}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "zarpazo", "doble", "golpe_suelo", "espinas", "raices", "rugido", "aturdido", "morir", "levantarse", "alzarse"};
}
