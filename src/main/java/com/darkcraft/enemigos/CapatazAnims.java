package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class CapatazAnims {
    private CapatazAnims() {}

    public static final int PICOTAZO = 1;
    public static final int BARRIDO = 2;
    public static final int EMBESTIDA = 3;
    public static final int VETA = 4;
    public static final int BARRENOS = 5;
    public static final int DERRUMBE = 6;
    public static final int SILBATO = 7;
    public static final int ATURDIDO = 8;
    public static final int MORIR = 9;
    public static final int VOLVERSE = 10;
    public static final int ABSORBER = 11;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 38, 30, 30, 40, 36, 48, 44, 30, 80, 24, 52};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {22}, {14}, {12}, {24}, {22}, {16, 32}, {}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "picotazo", "barrido", "embestida", "veta", "barrenos", "derrumbe", "silbato", "aturdido", "morir", "volverse", "absorber"};
}
