package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class JineteAnims {
    private JineteAnims() {}

    public static final int TAJO = 1;
    public static final int BARRIDO = 2;
    public static final int CARGA = 3;
    public static final int PISOTEA = 4;
    public static final int CUERNO = 5;
    public static final int ATURDIDO = 6;
    public static final int APARECE = 7;
    public static final int DESAPARECE = 8;
    public static final int MORIR = 9;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 24, 34, 60, 40, 48, 60, 30, 40, 90};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {13}, {18}, {}, {24}, {20}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "tajo", "barrido", "carga", "pisotea", "cuerno", "aturdido", "aparece", "desaparece", "morir"};
}
