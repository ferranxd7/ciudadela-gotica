package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class PerroAnims {
    private PerroAnims() {}

    public static final int MORDISCO = 1;
    public static final int SALTO = 2;
    public static final int AULLIDO = 3;
    public static final int ATURDIDO = 4;
    public static final int MORIR = 5;
    public static final int LEVANTARSE = 6;
    public static final int ALZARSE = 7;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 20, 30, 40, 30, 40, 28, 24};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {9}, {16}, {}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "mordisco", "salto", "aullido", "aturdido", "morir", "levantarse", "alzarse"};
}
