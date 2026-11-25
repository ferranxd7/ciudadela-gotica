package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class AhogadoAnims {
    private AhogadoAnims() {}

    public static final int SABLE_TAJO = 1;
    public static final int SABLE_COMBO = 2;
    public static final int ARPON_LANZAR = 3;
    public static final int ARPON_PINCHAR = 4;
    public static final int ATURDIDO = 5;
    public static final int MORIR = 6;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 32, 54, 50, 34, 30, 36};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {16}, {15, 26, 39}, {21}, {18}, {}, {}};

    public static final String[] NOMBRES = {"", "sable_tajo", "sable_combo", "arpon_lanzar", "arpon_pinchar", "aturdido", "morir"};

    /** El tick de arpon_lanzar desde el que el arponero vuelve a llevar arpon (saca otro de la espalda). */
    public static final int ARPON_OTRO = 38;
}
