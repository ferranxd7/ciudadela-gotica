package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class GuardiaAnims {
    private GuardiaAnims() {}

    public static final int ESPADA_TAJO = 1;
    public static final int ESPADA_COMBO = 2;
    public static final int ESPADA_ESCUDAZO = 3;
    public static final int ESPADA_BLOQUEA = 4;
    public static final int ALABARDA_PICA = 5;
    public static final int ALABARDA_HACHAZO = 6;
    public static final int ALABARDA_BARRIDO = 7;
    public static final int BALLESTA_DISPARO = 8;
    public static final int BALLESTA_CULATAZO = 9;
    public static final int ATURDIDO = 10;
    public static final int MORIR = 11;
    public static final int LEVANTARSE = 12;
    public static final int ALZARSE = 13;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 30, 50, 26, 10, 34, 40, 38, 52, 26, 30, 44, 26, 38};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {16}, {14, 25, 38}, {12}, {}, {19}, {22}, {19}, {18}, {12}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "espada_tajo", "espada_combo", "espada_escudazo", "espada_bloquea", "alabarda_pica", "alabarda_hachazo", "alabarda_barrido", "ballesta_disparo", "ballesta_culatazo", "aturdido", "morir", "levantarse", "alzarse"};
}
