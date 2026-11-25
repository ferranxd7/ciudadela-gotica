package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class EliteAnims {
    private EliteAnims() {}

    public static final int TAJO = 1;
    public static final int BARRIDO = 2;
    public static final int ESTOCADA = 3;
    public static final int EMBESTIDA = 4;
    public static final int GRITO = 5;
    public static final int ATURDIDO = 6;
    public static final int MORIR = 7;
    public static final int LEVANTARSE = 8;
    public static final int ALZARSE = 9;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 40, 36, 28, 32, 36, 32, 52, 32, 28};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {19}, {16}, {13}, {14}, {}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "tajo", "barrido", "estocada", "embestida", "grito", "aturdido", "morir", "levantarse", "alzarse"};
}
