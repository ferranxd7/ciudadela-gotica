package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class AhorcadoAnims {
    private AhorcadoAnims() {}

    public static final int ZARPAZO = 1;
    public static final int DOBLE = 2;
    public static final int SALTO = 3;
    public static final int LATIGAZO = 4;
    public static final int LAZO = 5;
    public static final int CAER = 6;
    public static final int ATURDIDO = 7;
    public static final int MORIR = 8;
    public static final int LEVANTARSE = 9;
    public static final int ALZARSE = 10;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 22, 30, 32, 32, 40, 26, 30, 48, 32, 40};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {11}, {10, 19}, {21}, {17}, {18}, {8}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "zarpazo", "doble", "salto", "latigazo", "lazo", "caer", "aturdido", "morir", "levantarse", "alzarse"};

    /** El nudo de la soga en la postura de colgado (px del modelo, sin escalar) y lo que sube la soga del modelo. */
    public static final float NUDO_X = 4.4f, NUDO_Y = -5.25f, NUDO_Z = 0.0f, SOGA_ARRIBA = 12.0f;
    public static final float ESCALA = 1.1f;
}
