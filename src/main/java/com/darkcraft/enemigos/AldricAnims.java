package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class AldricAnims {
    private AldricAnims() {}

    public static final int CADENAZO = 1;
    public static final int LATIGAZO = 2;
    public static final int ESTRELLA = 3;
    public static final int TORBELLINO = 4;
    public static final int SALTO = 5;
    public static final int ENCADENAR = 6;
    public static final int RUGIDO = 7;
    public static final int MARCA = 8;
    public static final int ATURDIDO = 9;
    public static final int MORIR = 10;
    public static final int LEVANTARSE = 11;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 32, 34, 40, 40, 44, 36, 48, 50, 30, 120, 32};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {18}, {20}, {24}, {14, 26}, {28}, {20}, {20}, {24}, {}, {}, {}};

    public static final String[] NOMBRES = {"", "cadenazo", "latigazo", "estrella", "torbellino", "salto", "encadenar", "rugido", "marca", "aturdido", "morir", "levantarse"};
}
