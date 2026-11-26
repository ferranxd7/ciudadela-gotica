package com.darkcraft.enemigos;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Numeros, duraciones y golpes (en ticks) de las acciones. */
public final class CorcelAnims {
    private CorcelAnims() {}

    public static final int ENCABRITARSE = 1;
    public static final int DESHACERSE = 2;

    /** Lo que dura cada accion, en ticks (por numero). */
    public static final int[] TICKS = {0, 36, 32};

    /** Los ticks (desde que empieza la accion) en los que el arma llega: ahi pega el servidor. */
    public static final int[][] GOLPES = {{}, {}, {}};

    public static final String[] NOMBRES = {"", "encabritarse", "deshacerse"};
}
