package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class GargolaAnims {
    private GargolaAnims() {}

    public static final int DESPIERTA = 1;
    public static final int GRITO = 2;
    public static final int ZARPAZO = 3;
    public static final int COLETAZO = 4;
    public static final int DESPEGA = 5;
    public static final int ATERRIZA = 6;
    public static final int PICADO = 7;
    public static final int GARRAS = 8;
    public static final int ATURDIDO = 9;
    public static final int MORIR = 10;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 44, 28, 26, 24, 18, 16, 32, 20, 30, 40};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {}, {}, {9, 17}, {12}, {}, {4}, {}, {11}, {}, {}};

    public static final String[] NOMBRES = {"", "despierta", "grito", "zarpazo", "coletazo", "despega", "aterriza", "picado", "garras", "aturdido", "morir"};

    /** El despegue: el tick en que deja el suelo. */
    public static final int DESPEGA_SALTO = 9;
    /** El picado: de que tick a que tick se lanza (lo de antes es prepararse). */
    public static final int PICADO_DESDE = 12, PICADO_HASTA = 30;
}
