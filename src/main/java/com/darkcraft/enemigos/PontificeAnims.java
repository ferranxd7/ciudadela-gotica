package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class PontificeAnims {
    private PontificeAnims() {}

    public static final int FERULAZO = 1;
    public static final int BARRIDO = 2;
    public static final int ASPERSION = 3;
    public static final int COMUNION = 4;
    public static final int JUICIO = 5;
    public static final int SERMON = 6;
    public static final int UNCION = 7;
    public static final int ATURDIDO = 8;
    public static final int MORIR = 9;
    public static final int LEVANTARSE = 10;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 44, 40, 40, 68, 48, 52, 60, 30, 80, 40};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {23}, {20}, {18}, {17}, {26}, {16}, {23}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "ferulazo", "barrido", "aspersion", "comunion", "juicio", "sermon", "uncion", "aturdido", "morir", "levantarse"};
}
