package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class GalenAnims {
    private GalenAnims() {}

    public static final int JERINGA = 1;
    public static final int BISTURI = 2;
    public static final int FRASCOS = 3;
    public static final int VAPOR = 4;
    public static final int ESTALLIDO = 5;
    public static final int PREPARAR = 6;
    public static final int BEBER = 7;
    public static final int VOLVERSE = 8;
    public static final int ATURDIDO = 9;
    public static final int MORIR = 10;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 34, 32, 40, 36, 38, 40, 92, 26, 30, 90};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {21}, {11, 19}, {20}, {17}, {25}, {29}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "jeringa", "bisturi", "frascos", "vapor", "estallido", "preparar", "beber", "volverse", "aturdido", "morir"};
}
