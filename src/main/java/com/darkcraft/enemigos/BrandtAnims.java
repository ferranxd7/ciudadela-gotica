package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class BrandtAnims {
    private BrandtAnims() {}

    public static final int MARTILLAZO = 1;
    public static final int GOLPE = 2;
    public static final int ESCUDAZO = 3;
    public static final int EMBESTIDA = 4;
    public static final int MURO = 5;
    public static final int TERREMOTO = 6;
    public static final int RUGIDO = 7;
    public static final int ATURDIDO = 8;
    public static final int MORIR = 9;
    public static final int LEVANTARSE = 10;
    public static final int ALZARSE = 11;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 44, 26, 26, 40, 36, 52, 40, 32, 60, 40, 32};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {21}, {12}, {12}, {23}, {}, {26}, {}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "martillazo", "golpe", "escudazo", "embestida", "muro", "terremoto", "rugido", "aturdido", "morir", "levantarse", "alzarse"};
}
