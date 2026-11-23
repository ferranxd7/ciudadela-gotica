package com.darkcraft.gavran;

/** GENERADO por tools/gavran/generar.py: numeros, duraciones y golpes (en ticks) de las animaciones de Gavran. */
public final class GavranAnims {
    private GavranAnims() {}

    public static final int DESCENDER = 1;
    public static final int SALUDO = 2;
    public static final int COMBO3 = 3;
    public static final int COMBO5 = 4;
    public static final int CONTRAATAQUE = 5;
    public static final int RAICES = 6;
    public static final int CLAVAR = 7;
    public static final int INVOCAR = 8;
    public static final int BLOQUEO = 9;
    public static final int ATURDIDO = 10;
    public static final int SUBIR = 11;
    public static final int MUERTE = 12;
    public static final int ALZARSE = 13;
    public static final int JUICIO = 14;
    public static final int CAER = 15;
    public static final int SUBIR_BALCON = 16;
    public static final int LEVANTARSE = 17;

    /** Duracion de cada accion en ticks (por numero). */
    public static final int[] TICKS = {0, 80, 60, 58, 80, 26, 46, 62, 54, 12, 84, 38, 120, 80, 112, 48, 44, 22};

    /** Ticks (desde que empieza la animacion) en los que la espada llega: ahi pega el servidor. */
    public static final int[] GOLPES_DESCENDER = {29, 55};
    public static final int[] GOLPES_COMBO3 = {11, 21, 32};
    public static final int[] GOLPES_COMBO5 = {11, 21, 30, 45, 61};
    public static final int[] GOLPES_CONTRAATAQUE = {10};
    public static final int[] GOLPES_RAICES = {14};
    public static final int[] GOLPES_CLAVAR = {17};
    public static final int[] GOLPES_INVOCAR = {26};
    public static final int[] GOLPES_SUBIR = {11};
    public static final int[] GOLPES_ALZARSE = {34};
    public static final int[] GOLPES_JUICIO = {82};
    public static final int[] GOLPES_CAER = {10, 29};
    public static final int[] GOLPES_SUBIR_BALCON = {11, 28};
}
