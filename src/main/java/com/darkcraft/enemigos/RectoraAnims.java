package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class RectoraAnims {
    private RectoraAnims() {}

    public static final int BACULO = 1;
    public static final int ORBES = 2;
    public static final int ESTRELLAS = 3;
    public static final int CONSTELACION = 4;
    public static final int PARPADEO = 5;
    public static final int GRITO = 6;
    public static final int ALZARSE = 7;
    public static final int ATURDIDO = 8;
    public static final int MORIR = 9;
    public static final int LEVANTARSE = 10;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 32, 36, 52, 48, 24, 48, 60, 28, 80, 32};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {16}, {19}, {26}, {20}, {}, {}, {30}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "baculo", "orbes", "estrellas", "constelacion", "parpadeo", "grito", "alzarse", "aturdido", "morir", "levantarse"};

    public static final float ESCALA = 1.6f;
}
