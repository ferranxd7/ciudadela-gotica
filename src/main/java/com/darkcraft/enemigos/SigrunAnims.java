package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class SigrunAnims {
    private SigrunAnims() {}

    public static final int DESENVAINAR = 1;
    public static final int TAJO = 2;
    public static final int COMBO = 3;
    public static final int ESCUDAZO = 4;
    public static final int ESTOCADA = 5;
    public static final int BLOQUEA = 6;
    public static final int GIRO = 7;
    public static final int SALTO = 8;
    public static final int GRITO = 9;
    public static final int ATURDIDO = 10;
    public static final int RENDIRSE = 11;
    public static final int LEVANTARSE = 12;
    public static final int SALUDO = 13;
    public static final int CAER = 14;
    public static final int PLANTAR = 15;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 44, 28, 46, 24, 32, 10, 36, 40, 36, 30, 44, 36, 36, 52, 28};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {}, {15}, {13, 23, 35}, {11}, {18}, {}, {16}, {24}, {}, {}, {}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "desenvainar", "tajo", "combo", "escudazo", "estocada", "bloquea", "giro", "salto", "grito", "aturdido", "rendirse", "levantarse", "saludo", "caer", "plantar"};

    public static final float ESCALA = 1.05f;
}
