package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class EnsambladoAnims {
    private EnsambladoAnims() {}

    public static final int CANDELAZO = 1;
    public static final int BARRIDO_CIRIO = 2;
    public static final int PALADA = 3;
    public static final int TIERRA = 4;
    public static final int MAZAZO = 5;
    public static final int BARRIDO_MAZA = 6;
    public static final int PISOTON = 7;
    public static final int ATURDIDO = 8;
    public static final int MORIR = 9;
    public static final int DESHACERSE = 10;
    public static final int REHACERSE = 11;
    public static final int LEVANTARSE = 12;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 32, 28, 28, 30, 40, 34, 30, 30, 32, 22, 40, 22};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {19}, {13}, {16}, {16}, {24}, {18}, {16}, {}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "candelazo", "barrido_cirio", "palada", "tierra", "mazazo", "barrido_maza", "pisoton", "aturdido", "morir", "deshacerse", "rehacerse", "levantarse"};
}
