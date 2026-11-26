package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class MolineraAnims {
    private MolineraAnims() {}

    public static final int SIEGA = 1;
    public static final int TAJO = 2;
    public static final int GIRO = 3;
    public static final int ESTOCADA = 4;
    public static final int PISOTON = 5;
    public static final int GRITO = 6;
    public static final int ATURDIDO = 7;
    public static final int MORIR = 8;
    public static final int LEVANTARSE = 9;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 40, 42, 56, 36, 26, 44, 30, 60, 36};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {21}, {22}, {22, 37}, {18}, {11}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "siega", "tajo", "giro", "estocada", "pisoton", "grito", "aturdido", "morir", "levantarse"};
}
