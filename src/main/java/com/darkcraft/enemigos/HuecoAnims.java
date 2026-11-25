package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class HuecoAnims {
    private HuecoAnims() {}

    public static final int HORCA_ESTOCADA = 1;
    public static final int HORCA_BARRIDO = 2;
    public static final int HOZ_TAJO = 3;
    public static final int HOZ_DOBLE = 4;
    public static final int ANTORCHA_GOLPE = 5;
    public static final int ANTORCHA_BARRIDO = 6;
    public static final int ATURDIDO = 7;
    public static final int MORIR = 8;
    public static final int LEVANTARSE = 9;
    public static final int ALZARSE = 10;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 32, 36, 23, 34, 30, 32, 30, 34, 26, 38};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {16}, {18}, {10}, {10, 18}, {15}, {14}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "horca_estocada", "horca_barrido", "hoz_tajo", "hoz_doble", "antorcha_golpe", "antorcha_barrido", "aturdido", "morir", "levantarse", "alzarse"};
}
