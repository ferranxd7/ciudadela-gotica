package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class OsmundAnims {
    private OsmundAnims() {}

    public static final int INCENSAZO = 1;
    public static final int GIRO = 2;
    public static final int CORONA = 3;
    public static final int MURO = 4;
    public static final int LLAMAR = 5;
    public static final int GUIRNALDAS = 6;
    public static final int ATURDIDO = 7;
    public static final int MORIR = 8;
    public static final int LEVANTARSE = 9;
    public static final int ALZARSE = 10;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 38, 36, 32, 40, 48, 36, 30, 80, 28, 52};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {23}, {20}, {18}, {24}, {}, {20}, {}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "incensazo", "giro", "corona", "muro", "llamar", "guirnaldas", "aturdido", "morir", "levantarse", "alzarse"};
}
