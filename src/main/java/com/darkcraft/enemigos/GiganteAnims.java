package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class GiganteAnims {
    private GiganteAnims() {}

    public static final int MAZAZO = 1;
    public static final int BARRIDO = 2;
    public static final int PISOTON = 3;
    public static final int PATADA = 4;
    public static final int ROCA = 5;
    public static final int RUGIDO = 6;
    public static final int ATURDIDO = 7;
    public static final int MORIR = 8;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 64, 48, 40, 32, 64, 56, 60, 180};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {30}, {26}, {22}, {16}, {20, 42}, {20}, {}, {}};

    public static final String[] NOMBRES = {"", "mazazo", "barrido", "pisoton", "patada", "roca", "rugido", "aturdido", "morir"};
}
