package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class CapitanAnims {
    private CapitanAnims() {}

    public static final int TAJO = 1;
    public static final int DOBLE = 2;
    public static final int ESTOCADA = 3;
    public static final int FAROL = 4;
    public static final int LLAMAR = 5;
    public static final int ATURDIDO = 6;
    public static final int MORIR = 7;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 38, 48, 36, 46, 58, 28, 64};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {19}, {16, 28}, {18}, {20}, {28}, {}, {}};

    public static final String[] NOMBRES = {"", "tajo", "doble", "estocada", "farol", "llamar", "aturdido", "morir"};
}
