package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class AbadAnims {
    private AbadAnims() {}

    public static final int BACULAZO = 1;
    public static final int BARRIDO = 2;
    public static final int PISOTON = 3;
    public static final int ALIENTO = 4;
    public static final int CARAMBANOS = 5;
    public static final int GRITO = 6;
    public static final int ATURDIDO = 7;
    public static final int MORIR = 8;
    public static final int LEVANTARSE = 9;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 50, 42, 38, 60, 52, 52, 32, 80, 44};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {25}, {21}, {18}, {16}, {24}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "baculazo", "barrido", "pisoton", "aliento", "carambanos", "grito", "aturdido", "morir", "levantarse"};

    public static final float ESCALA = 2.0f;
}
