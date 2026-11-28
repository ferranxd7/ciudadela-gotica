package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class EnterradoAnims {
    private EnterradoAnims() {}

    public static final int PICOTAZO = 1;
    public static final int BARRIDO = 2;
    public static final int LANZAR = 3;
    public static final int MECHAZO = 4;
    public static final int ALMADENAZO = 5;
    public static final int BARRIDO_ALMADENA = 6;
    public static final int EMBESTIDA = 7;
    public static final int ATURDIDO = 8;
    public static final int MORIR = 9;
    public static final int LEVANTARSE = 10;
    public static final int ALZARSE = 11;
    public static final int VOLVERSE = 12;
    public static final int INCORPORARSE = 13;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 34, 26, 28, 20, 44, 36, 28, 30, 40, 22, 32, 18, 32};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {20}, {12}, {16}, {10}, {26}, {19}, {11}, {}, {}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "picotazo", "barrido", "lanzar", "mechazo", "almadenazo", "barrido_almadena", "embestida", "aturdido", "morir", "levantarse", "alzarse", "volverse", "incorporarse"};
}
