package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class InsomneAnims {
    private InsomneAnims() {}

    public static final int ESTOCADA = 1;
    public static final int BARRIDO = 2;
    public static final int CUERNO = 3;
    public static final int DISPARO = 4;
    public static final int RECARGA = 5;
    public static final int CULATAZO = 6;
    public static final int MAZAZO = 7;
    public static final int ESCUDAZO = 8;
    public static final int CARGA = 9;
    public static final int BLOQUEA = 10;
    public static final int ATURDIDO = 11;
    public static final int MORIR = 12;
    public static final int LEVANTARSE = 13;
    public static final int VOLVERSE = 14;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 24, 26, 40, 20, 48, 22, 30, 22, 36, 12, 30, 44, 28, 18};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {12}, {13}, {32}, {13}, {44}, {10}, {17}, {11}, {22}, {}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "estocada", "barrido", "cuerno", "disparo", "recarga", "culatazo", "mazazo", "escudazo", "carga", "bloquea", "aturdido", "morir", "levantarse", "volverse"};
}
