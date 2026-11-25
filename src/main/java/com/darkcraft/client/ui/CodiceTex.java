package com.darkcraft.client.ui;

import net.minecraft.resources.ResourceLocation;

/** El atlas del codice: tapa, letras capitales, adornos, cinta y marco de las miniaturas (lo escribe tools/ui: no tocar a mano). */
public final class CodiceTex {
    private CodiceTex() {}

    public static final ResourceLocation TEX = new ResourceLocation("darkcraft", "textures/gui/codice.png");
    public static final int TW = 256, TH = 256;

    /** Un trozo de la textura: u, v, ancho y alto. */
    public record T(int u, int v, int w, int h) {}

    public static final T TAPA_AI = new T(0, 0, 20, 20);
    public static final T TAPA_A = new T(21, 0, 24, 20);
    public static final T TAPA_AD = new T(46, 0, 20, 20);
    public static final T TAPA_I = new T(67, 0, 20, 24);
    public static final T TAPA_C = new T(88, 0, 24, 24);
    public static final T TAPA_D = new T(113, 0, 20, 24);
    public static final T TAPA_BI = new T(134, 0, 20, 20);
    public static final T TAPA_B = new T(155, 0, 24, 20);
    public static final T TAPA_BD = new T(180, 0, 20, 20);
    public static final T CAP_ROJO = new T(201, 0, 30, 30);
    public static final T CAP_AZUL = new T(0, 31, 30, 30);
    public static final T CAP_NEGRO = new T(31, 31, 30, 30);
    public static final T CAP_VERDE = new T(62, 31, 30, 30);
    public static final T FLORON = new T(93, 31, 77, 9);
    public static final T FIN = new T(171, 31, 15, 9);
    public static final T CINTA = new T(187, 31, 9, 64);
    public static final T MARCO_AI = new T(197, 31, 6, 6);
    public static final T MARCO_A = new T(204, 31, 12, 6);
    public static final T MARCO_AD = new T(217, 31, 6, 6);
    public static final T MARCO_I = new T(224, 31, 6, 12);
    public static final T MARCO_C = new T(231, 31, 12, 12);
    public static final T MARCO_D = new T(244, 31, 6, 12);
    public static final T MARCO_BI = new T(0, 96, 6, 6);
    public static final T MARCO_B = new T(7, 96, 12, 6);
    public static final T MARCO_BD = new T(20, 96, 6, 6);
    public static final T ESQUINA = new T(27, 96, 12, 12);
    public static final T LACRE = new T(40, 96, 13, 13);
}
