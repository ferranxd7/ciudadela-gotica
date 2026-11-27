package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class BufonAnims {
    private BufonAnims() {}

    public static final int REVERENCIA = 1;
    public static final int TAJO = 2;
    public static final int PIRUETA = 3;
    public static final int CUCHILLOS = 4;
    public static final int MALABARES = 5;
    public static final int HUMO = 6;
    public static final int APARECE = 7;
    public static final int VOLTERETA = 8;
    public static final int ILUSION = 9;
    public static final int RISA = 10;
    public static final int ATURDIDO = 11;
    public static final int MORIR = 12;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 52, 22, 28, 26, 52, 18, 14, 18, 32, 36, 28, 80};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {24}, {9, 14}, {11, 17}, {11, 14, 17}, {34, 38, 42}, {8}, {}, {}, {18}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "reverencia", "tajo", "pirueta", "cuchillos", "malabares", "humo", "aparece", "voltereta", "ilusion", "risa", "aturdido", "morir"};
}
