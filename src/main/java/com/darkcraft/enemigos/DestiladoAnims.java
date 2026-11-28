package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class DestiladoAnims {
    private DestiladoAnims() {}

    public static final int FRASCO = 1;
    public static final int MORTERO = 2;
    public static final int MORTERO_BARRIDO = 3;
    public static final int ZARPAZO = 4;
    public static final int ABALANZARSE = 5;
    public static final int ESCUPITAJO = 6;
    public static final int PUNETAZO = 7;
    public static final int EMBESTIDA = 8;
    public static final int DERRAME = 9;
    public static final int ATURDIDO = 10;
    public static final int MORIR = 11;
    public static final int LEVANTARSE = 12;
    public static final int VOLVERSE = 13;
    public static final int ALZARSE = 14;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 28, 26, 22, 20, 30, 26, 26, 32, 40, 30, 36, 26, 18, 40};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {17}, {16}, {10}, {11}, {18}, {15}, {15}, {17}, {18}, {}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "frasco", "mortero", "mortero_barrido", "zarpazo", "abalanzarse", "escupitajo", "punetazo", "embestida", "derrame", "aturdido", "morir", "levantarse", "volverse", "alzarse"};
}
