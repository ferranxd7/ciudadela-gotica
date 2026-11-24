package com.darkcraft.client.ui;

import net.minecraft.resources.ResourceLocation;

/** Los trozos de la textura del HUD nuevo (lo escribe tools/ui: no tocar a mano). */
public final class HudTex {
    private HudTex() {}

    public static final ResourceLocation TEX = new ResourceLocation("darkcraft", "textures/gui/hud.png");
    public static final int TW = 256, TH = 128;

    /** Un trozo de la textura: u, v, ancho y alto. */
    public record T(int u, int v, int w, int h) {}

    public static final T EMBLEMA = new T(0, 0, 24, 24);
    public static final T BARRA_IZQ = new T(25, 0, 3, 9);
    public static final T BARRA_MED = new T(29, 0, 14, 9);
    public static final T BARRA_PUNTA = new T(44, 0, 8, 9);
    public static final T BARRA_FONDO = new T(53, 0, 8, 5);
    public static final T VIDA = new T(62, 0, 32, 5);
    public static final T MANA = new T(95, 0, 32, 5);
    public static final T ESTAMINA = new T(128, 0, 32, 5);
    public static final T RASTRO = new T(161, 0, 8, 5);
    public static final T BRILLO = new T(170, 0, 14, 5);
    public static final T ECO_0 = new T(185, 0, 16, 16);
    public static final T ECO_1 = new T(202, 0, 16, 16);
    public static final T ECO_2 = new T(219, 0, 16, 16);
    public static final T PLACA_IZQ = new T(0, 25, 24, 22);
    public static final T PLACA_MED = new T(25, 25, 18, 22);
    public static final T PLACA_DER = new T(44, 25, 16, 22);
    public static final T TECLA_IZQ = new T(61, 25, 4, 13);
    public static final T TECLA_MED = new T(66, 25, 4, 13);
    public static final T TECLA_DER = new T(71, 25, 4, 13);
    public static final T AVISO_IZQ = new T(76, 25, 12, 19);
    public static final T AVISO_MED = new T(89, 25, 8, 19);
    public static final T AVISO_DER = new T(98, 25, 12, 19);
    public static final T FLORITURA = new T(111, 25, 72, 11);
    public static final T CENEFA = new T(184, 25, 32, 9);
    public static final T CINE_CENTRO = new T(0, 48, 64, 17);
    public static final T BRUJULA_IZQ = new T(65, 48, 24, 13);
    public static final T BRUJULA_MED = new T(90, 48, 8, 13);
    public static final T BRUJULA_DER = new T(99, 48, 24, 13);
    public static final T BRUJULA_AGUJA = new T(124, 48, 9, 6);
    public static final T MARCA_OBJETIVO = new T(134, 48, 7, 7);
    public static final T MARCA_ESPADA = new T(142, 48, 7, 11);
    public static final T BOTON_IZQ = new T(150, 48, 8, 20);
    public static final T BOTON_MED = new T(159, 48, 16, 20);
    public static final T BOTON_DER = new T(176, 48, 8, 20);
    public static final T BOTON_LUZ_IZQ = new T(185, 48, 8, 20);
    public static final T BOTON_LUZ_MED = new T(194, 48, 16, 20);
    public static final T BOTON_LUZ_DER = new T(211, 48, 8, 20);
    public static final T BANDA = new T(220, 48, 32, 16);
}
