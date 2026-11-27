package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class SombraAnims {
    private SombraAnims() {}

    public static final int DESPIERTA = 1;
    public static final int DESPIERTA_CORTA = 2;
    public static final int RUGIDO = 3;
    public static final int ZARPAZOS = 4;
    public static final int CADENA = 5;
    public static final int MANOS = 6;
    public static final int LANZAS = 7;
    public static final int GIRO = 8;
    public static final int NIEBLA = 9;
    public static final int MIRADA = 10;
    public static final int GRITO = 11;
    public static final int CAIDOS = 12;
    public static final int FURIA = 13;
    public static final int ATURDIDO = 14;
    public static final int CAE = 15;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 352, 72, 44, 36, 32, 36, 32, 28, 30, 68, 36, 44, 60, 28, 100};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {332}, {52}, {13}, {11, 20}, {15}, {10}, {15}, {13}, {}, {31}, {16}, {24}, {32}, {}, {}};

    public static final String[] NOMBRES = {"", "despierta", "despierta_corta", "rugido", "zarpazos", "cadena", "manos", "lanzas", "giro", "niebla", "mirada", "grito", "caidos", "furia", "aturdido", "cae"};
}
