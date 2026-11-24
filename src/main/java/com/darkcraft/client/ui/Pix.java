package com.darkcraft.client.ui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Utiles para dibujar la interfaz en pixel art con los trozos de {@link HudTex}: cada pixel de la textura cae en un
 * pixel de la interfaz (nada de escalar a medias). Antes de cada dibujo se enciende la mezcla, porque {@code fill} y
 * {@code drawString} la dejan apagada y lo transparente saldria opaco.
 */
public final class Pix {
    private Pix() {}

    public static void mezcla() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    /** Mezcla aditiva (brillos). */
    public static void suma() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
    }

    public static void color(float r, float g, float b, float a) {
        RenderSystem.setShaderColor(r, g, b, a);
    }

    public static void blanco() {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    /** Un trozo entero en (x, y). */
    public static void t(GuiGraphics g, HudTex.T t, int x, int y) {
        mezcla();
        g.blit(HudTex.TEX, x, y, t.u(), t.v(), t.w(), t.h(), HudTex.TW, HudTex.TH);
    }

    /** Un trozo vuelto de izquierda a derecha. */
    public static void tEspejo(GuiGraphics g, HudTex.T t, int x, int y) {
        mezcla();
        g.blit(HudTex.TEX, x, y, t.w(), t.h(), t.u() + t.w(), t.v(), -t.w(), t.h(), HudTex.TW, HudTex.TH);
    }

    /** Un trozo vuelto de arriba abajo. */
    public static void tVolteado(GuiGraphics g, HudTex.T t, int x, int y) {
        mezcla();
        g.blit(HudTex.TEX, x, y, t.w(), t.h(), t.u(), t.v() + t.h(), t.w(), -t.h(), HudTex.TW, HudTex.TH);
    }

    /** El trozo repetido a lo ancho hasta llenar w pixeles (el ultimo, cortado). */
    public static void tira(GuiGraphics g, HudTex.T t, int x, int y, int w) {
        mezcla();
        for (int i = 0; i < w; i += t.w())
            g.blit(HudTex.TEX, x + i, y, t.u(), t.v(), Math.min(t.w(), w - i), t.h(), HudTex.TW, HudTex.TH);
    }

    /** Como {@link #tira} pero empezando a mitad del dibujo (para que un relleno no "resbale" al acortarse). */
    public static void tiraDesde(GuiGraphics g, HudTex.T t, int x, int y, int w, int fase) {
        mezcla();
        int f = Math.floorMod(fase, t.w());
        for (int i = 0; i < w; ) {
            int trozo = Math.min(t.w() - f, w - i);
            g.blit(HudTex.TEX, x + i, y, t.u() + f, t.v(), trozo, t.h(), HudTex.TW, HudTex.TH);
            i += trozo;
            f = 0;
        }
    }

    /** La tira repetida a lo ancho y vuelta de arriba abajo. */
    public static void tiraVolteada(GuiGraphics g, HudTex.T t, int x, int y, int w) {
        mezcla();
        for (int i = 0; i < w; i += t.w()) {
            int trozo = Math.min(t.w(), w - i);
            g.blit(HudTex.TEX, x + i, y, trozo, t.h(), t.u(), t.v() + t.h(), trozo, -t.h(), HudTex.TW, HudTex.TH);
        }
    }

    /** El trozo repetido a lo ancho y a lo alto (un fondo). */
    public static void mosaico(GuiGraphics g, HudTex.T t, int x, int y, int w, int h) {
        mezcla();
        for (int j = 0; j < h; j += t.h())
            for (int i = 0; i < w; i += t.w())
                g.blit(HudTex.TEX, x + i, y + j, t.u(), t.v(), Math.min(t.w(), w - i), Math.min(t.h(), h - j), HudTex.TW, HudTex.TH);
    }

    /** Tres trozos: izquierda, el del medio repetido y derecha, hasta w pixeles de ancho. */
    public static void tres(GuiGraphics g, HudTex.T izq, HudTex.T med, HudTex.T der, int x, int y, int w) {
        t(g, izq, x, y);
        tira(g, med, x + izq.w(), y, w - izq.w() - der.w());
        t(g, der, x + w - der.w(), y);
    }

    /** Una franja horizontal de color que se apaga hacia los lados (para el fondo de los carteles). */
    public static void franja(GuiGraphics g, int x0, int x1, int y0, int y1, int rgb, float alfa) {
        int n = 48, w = x1 - x0;
        for (int i = 0; i < n; i++) {
            float c = (i + 0.5f) / n;                       // 0..1 a lo ancho
            float k = 1f - Math.abs(c * 2f - 1f);           // 0 en los lados, 1 en el centro
            k = Math.min(1f, k * 1.9f);
            k = k * k * (3f - 2f * k);
            int a = Math.round(255f * alfa * k);
            if (a < 3) continue;
            g.fill(x0 + w * i / n, y0, x0 + w * (i + 1) / n, y1, (a << 24) | rgb);
        }
    }
}
