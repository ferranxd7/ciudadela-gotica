package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class BalleneroAnims {
    private BalleneroAnims() {}

    public static final int ARPONAZO = 1;
    public static final int TAJO = 2;
    public static final int ARPON = 3;
    public static final int TIRON = 4;
    public static final int FAROL = 5;
    public static final int GRITO = 6;
    public static final int LLAMAR = 7;
    public static final int SALTO = 8;
    public static final int ATURDIDO = 9;
    public static final int MORIR = 10;
    public static final int LEVANTARSE = 11;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 32, 44, 34, 26, 36, 46, 56, 42, 28, 72, 32};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {17}, {15, 26}, {20}, {9}, {19}, {14}, {27}, {24}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "arponazo", "tajo", "arpon", "tiron", "farol", "grito", "llamar", "salto", "aturdido", "morir", "levantarse"};
}
