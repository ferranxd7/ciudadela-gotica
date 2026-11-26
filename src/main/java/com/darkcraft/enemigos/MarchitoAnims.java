package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class MarchitoAnims {
    private MarchitoAnims() {}

    public static final int GUADANA_SIEGA = 1;
    public static final int GUADANA_TAJO = 2;
    public static final int FAROL_TAJOS = 3;
    public static final int FAROL_GOLPE = 4;
    public static final int CAMPANA_GOLPE = 5;
    public static final int CAMPANA_TOCAR = 6;
    public static final int ATURDIDO = 7;
    public static final int MORIR = 8;
    public static final int LEVANTARSE = 9;
    public static final int ALZARSE = 10;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 38, 34, 30, 38, 32, 44, 30, 38, 28, 40};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {20}, {18}, {7, 13}, {20}, {16}, {11, 19, 27}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "guadana_siega", "guadana_tajo", "farol_tajos", "farol_golpe", "campana_golpe", "campana_tocar", "aturdido", "morir", "levantarse", "alzarse"};
}
