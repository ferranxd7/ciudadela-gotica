package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class EspectroAnims {
    private EspectroAnims() {}

    public static final int ESTOCADA = 1;
    public static final int TAJO_DOBLE = 2;
    public static final int ZARPAZO = 3;
    public static final int LAMENTO = 4;
    public static final int DESVANECERSE = 5;
    public static final int ATURDIDO = 6;
    public static final int MORIR = 7;
    public static final int LEVANTARSE = 8;
    public static final int ALZARSE = 9;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 26, 32, 22, 40, 26, 28, 44, 30, 24};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {12}, {9, 19}, {10}, {18}, {}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "estocada", "tajo_doble", "zarpazo", "lamento", "desvanecerse", "aturdido", "morir", "levantarse", "alzarse"};
}
