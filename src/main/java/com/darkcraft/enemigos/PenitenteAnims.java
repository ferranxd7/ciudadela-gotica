package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class PenitenteAnims {
    private PenitenteAnims() {}

    public static final int AZOTE = 1;
    public static final int AZOTES = 2;
    public static final int PENITENCIA = 3;
    public static final int CRUZAZO = 4;
    public static final int BARRIDO = 5;
    public static final int EMBESTIDA = 6;
    public static final int INCENSARIO = 7;
    public static final int HUMO = 8;
    public static final int CANTICO = 9;
    public static final int ATURDIDO = 10;
    public static final int MORIR = 11;
    public static final int LEVANTARSE = 12;
    public static final int ALZARSE = 13;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 22, 36, 48, 38, 34, 32, 30, 32, 44, 30, 34, 26, 32};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {11}, {8, 16, 26}, {10, 19, 28, 36}, {19}, {17}, {10, 16, 21}, {13}, {21}, {20}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "azote", "azotes", "penitencia", "cruzazo", "barrido", "embestida", "incensario", "humo", "cantico", "aturdido", "morir", "levantarse", "alzarse"};
}
