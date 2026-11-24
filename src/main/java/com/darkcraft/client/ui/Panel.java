package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

import java.util.Locale;

/**
 * Las piezas de los menus del mod, en pixel art ({@link MenuTex}, tools/ui/menu.py): la losa de piedra con marco de oro
 * de los paneles, los rebajes oscuros, la barra de la fila elegida, las teclas dibujadas y los sonidos de los menus.
 */
public final class Panel {
    private Panel() {}

    /** Un trozo entero de la textura de los menus en (x, y). */
    public static void t(GuiGraphics g, MenuTex.T t, int x, int y) {
        Pix.mezcla();
        g.blit(MenuTex.TEX, x, y, t.u(), t.v(), t.w(), t.h(), MenuTex.TW, MenuTex.TH);
    }

    /** El trozo repetido hasta llenar w x h (el ultimo de cada fila y de cada columna, cortado). */
    private static void mosaico(GuiGraphics g, MenuTex.T t, int x, int y, int w, int h) {
        for (int j = 0; j < h; j += t.h())
            for (int i = 0; i < w; i += t.w())
                g.blit(MenuTex.TEX, x + i, y + j, t.u(), t.v(), Math.min(t.w(), w - i), Math.min(t.h(), h - j), MenuTex.TW, MenuTex.TH);
    }

    private static void nueve(GuiGraphics g, int x, int y, int w, int h, MenuTex.T ai, MenuTex.T a, MenuTex.T ad, MenuTex.T i, MenuTex.T c,
                              MenuTex.T d, MenuTex.T bi, MenuTex.T b, MenuTex.T bd) {
        Pix.mezcla();
        int e = ai.w(), f = ai.h();
        mosaico(g, c, x + e, y + f, w - 2 * e, h - 2 * f);
        mosaico(g, a, x + e, y, w - 2 * e, f);
        mosaico(g, b, x + e, y + h - f, w - 2 * e, f);
        mosaico(g, i, x, y + f, e, h - 2 * f);
        mosaico(g, d, x + w - e, y + f, e, h - 2 * f);
        t(g, ai, x, y);
        t(g, ad, x + w - e, y);
        t(g, bi, x, y + h - f);
        t(g, bd, x + w - e, y + h - f);
    }

    /** La losa de un panel: piedra oscura con doble marco de oro y tachuelas en las esquinas (minimo 24 x 24). */
    public static void losa(GuiGraphics g, int x, int y, int w, int h) {
        nueve(g, x, y, w, h, MenuTex.PANEL_AI, MenuTex.PANEL_A, MenuTex.PANEL_AD, MenuTex.PANEL_I, MenuTex.PANEL_C, MenuTex.PANEL_D,
                MenuTex.PANEL_BI, MenuTex.PANEL_B, MenuTex.PANEL_BD);
    }

    /** Un rebaje oscuro dentro de la losa (minimo 8 x 8). */
    public static void hueco(GuiGraphics g, int x, int y, int w, int h) {
        nueve(g, x, y, w, h, MenuTex.HUECO_AI, MenuTex.HUECO_A, MenuTex.HUECO_AD, MenuTex.HUECO_I, MenuTex.HUECO_C, MenuTex.HUECO_D,
                MenuTex.HUECO_BI, MenuTex.HUECO_B, MenuTex.HUECO_BD);
    }

    /** La barra roja con filos de oro que marca la fila elegida (18 de alto). */
    public static void fila(GuiGraphics g, int x, int y, int w) {
        Pix.mezcla();
        t(g, MenuTex.FILA_IZQ, x, y);
        mosaico(g, MenuTex.FILA_MED, x + MenuTex.FILA_IZQ.w(), y, w - MenuTex.FILA_IZQ.w() - MenuTex.FILA_DER.w(), MenuTex.FILA_MED.h());
        t(g, MenuTex.FILA_DER, x + w - MenuTex.FILA_DER.w(), y);
    }

    /** Una raya de oro que se apaga hacia los lados (separa la cabecera de un panel). */
    public static void raya(GuiGraphics g, int x0, int x1, int y) {
        Pix.franja(g, x0, x1, y, y + 1, 0xC0963C, 1f);
        Pix.franja(g, x0, x1, y + 1, y + 2, 0x4A3312, 1f);
    }

    /** Lo que ocupa a lo ancho una tecla dibujada con su texto detras ("[E] Elegir"). */
    public static int anchoTecla(Font f, Component tecla, Component texto) {
        String kt = tecla.getString().toUpperCase(Locale.ROOT);
        return Math.max(13, f.width(kt) + 8) + 4 + f.width(texto);
    }

    /** Una tecla dibujada como un boton de piedra y, detras, lo que hace. Devuelve la x donde acaba. */
    public static int tecla(GuiGraphics g, Font f, Component tecla, Component texto, int x, int y, int color) {
        String kt = tecla.getString().toUpperCase(Locale.ROOT);
        int wk = Math.max(13, f.width(kt) + 8);
        Pix.blanco();
        Pix.tres(g, HudTex.TECLA_IZQ, HudTex.TECLA_MED, HudTex.TECLA_DER, x, y, wk);
        g.drawString(f, kt, x + (wk - f.width(kt)) / 2 + 1, y + 2, 0xFFF6E3A0, false);
        g.drawString(f, texto, x + wk + 4, y + 3, color, true);
        Pix.mezcla();
        return x + wk + 4 + f.width(texto);
    }

    // ------------------------------------------------------------------ los sonidos de los menus

    public static void abre() {
        suena(DarkCraftMod.SFX_MENU_ABRE.get(), 1.0f, 0.7f);
    }

    public static void mueve() {
        suena(DarkCraftMod.SFX_MENU_MUEVE.get(), 1.0f, 0.55f);
    }

    public static void acepta() {
        suena(DarkCraftMod.SFX_MENU_ACEPTA.get(), 1.0f, 0.7f);
    }

    public static void cancela() {
        suena(DarkCraftMod.SFX_MENU_CANCELA.get(), 1.0f, 0.6f);
    }

    /** Un "no se puede" (mas grave y bajito). */
    public static void nada() {
        suena(DarkCraftMod.SFX_MENU_CANCELA.get(), 0.75f, 0.45f);
    }

    private static void suena(SoundEvent s, float tono, float volumen) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(s, tono, volumen));
    }
}
