package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class CentinelaAnims {
    private CentinelaAnims() {}

    public static final int TAJO = 1;
    public static final int GOLPE = 2;
    public static final int ESTOCADA = 3;
    public static final int BARRIDO = 4;
    public static final int HACHAZO = 5;
    public static final int HUMO_ESPADON = 6;
    public static final int HUMO_ALABARDA = 7;
    public static final int DESPERTAR_ESPADON = 8;
    public static final int DESPERTAR_ALABARDA = 9;
    public static final int LEVANTARSE_ESPADON = 10;
    public static final int LEVANTARSE_ALABARDA = 11;
    public static final int ATURDIDO_ESPADON = 12;
    public static final int ATURDIDO_ALABARDA = 13;
    public static final int MORIR_ESPADON = 14;
    public static final int MORIR_ALABARDA = 15;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 36, 40, 34, 40, 40, 30, 30, 32, 32, 44, 44, 32, 32, 48, 48};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {18}, {20}, {19}, {18, 20}, {22}, {18}, {18}, {}, {}, {}, {}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "tajo", "golpe", "estocada", "barrido", "hachazo", "humo_espadon", "humo_alabarda", "despertar_espadon", "despertar_alabarda", "levantarse_espadon", "levantarse_alabarda", "aturdido_espadon", "aturdido_alabarda", "morir_espadon", "morir_alabarda"};
}
