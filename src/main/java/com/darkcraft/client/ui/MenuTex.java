package com.darkcraft.client.ui;

import net.minecraft.resources.ResourceLocation;

/** Los trozos de la textura de los menus (lo escribe tools/ui: no tocar a mano). */
public final class MenuTex {
    private MenuTex() {}

    public static final ResourceLocation TEX = new ResourceLocation("darkcraft", "textures/gui/menu.png");
    public static final int TW = 256, TH = 128;

    /** Un trozo de la textura: u, v, ancho y alto. */
    public record T(int u, int v, int w, int h) {}

    public static final T PANEL_AI = new T(0, 0, 12, 12);
    public static final T PANEL_A = new T(13, 0, 32, 12);
    public static final T PANEL_AD = new T(46, 0, 12, 12);
    public static final T PANEL_I = new T(59, 0, 12, 32);
    public static final T PANEL_C = new T(72, 0, 32, 32);
    public static final T PANEL_D = new T(105, 0, 12, 32);
    public static final T PANEL_BI = new T(118, 0, 12, 12);
    public static final T PANEL_B = new T(131, 0, 32, 12);
    public static final T PANEL_BD = new T(164, 0, 12, 12);
    public static final T HUECO_AI = new T(177, 0, 4, 4);
    public static final T HUECO_A = new T(182, 0, 16, 4);
    public static final T HUECO_AD = new T(199, 0, 4, 4);
    public static final T HUECO_I = new T(204, 0, 4, 16);
    public static final T HUECO_C = new T(209, 0, 16, 16);
    public static final T HUECO_D = new T(226, 0, 4, 16);
    public static final T HUECO_BI = new T(231, 0, 4, 4);
    public static final T HUECO_B = new T(236, 0, 16, 4);
    public static final T HUECO_BD = new T(0, 33, 4, 4);
    public static final T FILA_IZQ = new T(5, 33, 10, 18);
    public static final T FILA_MED = new T(16, 33, 8, 18);
    public static final T FILA_DER = new T(25, 33, 10, 18);
    public static final T FLECHA_IZQ = new T(36, 33, 7, 9);
    public static final T FLECHA_DER = new T(44, 33, 7, 9);
    public static final T FLECHA_IZQ_LUZ = new T(52, 33, 7, 9);
    public static final T FLECHA_DER_LUZ = new T(60, 33, 7, 9);
    public static final T FLECHA_IZQ_GRIS = new T(68, 33, 7, 9);
    public static final T FLECHA_DER_GRIS = new T(76, 33, 7, 9);
    public static final T AT_VIGOR = new T(84, 33, 16, 16);
    public static final T AT_MENTE = new T(101, 33, 16, 16);
    public static final T AT_AGUANTE = new T(118, 33, 16, 16);
    public static final T AT_FUERZA = new T(135, 33, 16, 16);
    public static final T AT_DESTREZA = new T(152, 33, 16, 16);
    public static final T AT_INTELIGENCIA = new T(169, 33, 16, 16);
    public static final T AT_FE = new T(186, 33, 16, 16);
    public static final T AT_ARCANO = new T(203, 33, 16, 16);
    public static final T FRASCO_VIDA = new T(220, 33, 16, 16);
    public static final T FRASCO_VIDA_VACIO = new T(237, 33, 16, 16);
    public static final T FRASCO_MANA = new T(0, 52, 16, 16);
    public static final T FRASCO_MANA_VACIO = new T(17, 52, 16, 16);
    public static final T SANTUARIO = new T(34, 52, 16, 16);
    public static final T RANURA = new T(51, 52, 22, 22);
    public static final T RANURA_LUZ = new T(74, 52, 22, 22);
    public static final T RANURA_CERRADA = new T(97, 52, 22, 22);
    public static final T ICO_ESPADA = new T(120, 52, 16, 16);
    public static final T ICO_ESCUDO = new T(137, 52, 16, 16);
    public static final T ICO_YELMO = new T(154, 52, 16, 16);
    public static final T ICO_PECHO = new T(171, 52, 16, 16);
    public static final T ICO_PIERNAS = new T(188, 52, 16, 16);
    public static final T ICO_BOTAS = new T(205, 52, 16, 16);
    public static final T ICO_TALISMAN = new T(222, 52, 16, 16);
    public static final T ICO_HECHIZO = new T(239, 52, 16, 16);
    public static final T ICO_OBJETO = new T(0, 75, 16, 16);
    public static final T ICO_MATERIAL = new T(17, 75, 16, 16);
    public static final T ICO_LLAVE = new T(34, 75, 16, 16);
    public static final T ICO_TODO = new T(51, 75, 16, 16);
    public static final T SIL_ESPADA = new T(68, 75, 16, 16);
    public static final T SIL_ESCUDO = new T(85, 75, 16, 16);
    public static final T SIL_YELMO = new T(102, 75, 16, 16);
    public static final T SIL_PECHO = new T(119, 75, 16, 16);
    public static final T SIL_PIERNAS = new T(136, 75, 16, 16);
    public static final T SIL_BOTAS = new T(153, 75, 16, 16);
    public static final T SIL_TALISMAN = new T(170, 75, 16, 16);
    public static final T SIL_HECHIZO = new T(187, 75, 16, 16);
    public static final T SIL_OBJETO = new T(204, 75, 16, 16);
    public static final T CURSOR_AI = new T(221, 75, 7, 7);
    public static final T CURSOR_AD = new T(229, 75, 7, 7);
    public static final T CURSOR_BI = new T(237, 75, 7, 7);
    public static final T CURSOR_BD = new T(245, 75, 7, 7);
    public static final T PUNTO = new T(0, 92, 5, 5);
    public static final T PUNTO_LUZ = new T(6, 92, 5, 5);
    public static final T CANDADO = new T(12, 92, 9, 11);
    public static final T FIJAR = new T(22, 92, 9, 9);
    public static final T FIJAR_LUZ = new T(32, 92, 9, 9);
    public static final T EQUIPADO = new T(42, 92, 7, 7);
    public static final T PESTANA = new T(50, 92, 41, 7);
    public static final T CRUZ_CENTRO = new T(92, 92, 22, 22);
    public static final T LOGRO_IZQ = new T(115, 92, 12, 20);
    public static final T LOGRO_MED = new T(128, 92, 8, 20);
    public static final T LOGRO_DER = new T(137, 92, 12, 20);
}
