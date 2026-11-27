package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class MarcadoAnims {
    private MarcadoAnims() {}

    public static final int MAZAZO = 1;
    public static final int BARRIDO = 2;
    public static final int TAJO = 3;
    public static final int ESTOCADA = 4;
    public static final int ESCUDAZO = 5;
    public static final int BLOQUEO = 6;
    public static final int DISPARO = 7;
    public static final int FLECHA_NEGRA = 8;
    public static final int PUNZADA = 9;
    public static final int DESLIZ = 10;
    public static final int ATURDIDO = 11;
    public static final int MORIR = 12;
    public static final int LEVANTARSE = 13;
    public static final int ALZARSE = 14;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 38, 32, 22, 26, 19, 12, 26, 40, 17, 15, 30, 36, 26, 32};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {20}, {17}, {11}, {13}, {8}, {}, {17}, {29}, {8}, {7}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "mazazo", "barrido", "tajo", "estocada", "escudazo", "bloqueo", "disparo", "flecha_negra", "punzada", "desliz", "aturdido", "morir", "levantarse", "alzarse"};
}
