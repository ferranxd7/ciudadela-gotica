package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class CondestableAnims {
    private CondestableAnims() {}

    public static final int TAJO = 1;
    public static final int BARRIDO = 2;
    public static final int EMBESTIDA = 3;
    public static final int LLAMARADA = 4;
    public static final int ALZAR = 5;
    public static final int GRITO = 6;
    public static final int JURAMENTO = 7;
    public static final int ATURDIDO = 8;
    public static final int MORIR = 9;
    public static final int LEVANTARSE = 10;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 50, 40, 48, 44, 52, 52, 80, 32, 80, 44};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {25}, {19}, {14}, {18}, {22}, {}, {65}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "tajo", "barrido", "embestida", "llamarada", "alzar", "grito", "juramento", "aturdido", "morir", "levantarse"};

    public static final float ESCALA = 2.0f;
}
