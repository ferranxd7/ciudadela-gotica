package com.darkcraft.client.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

/**
 * La letra gotica en pixel art de los titulos del mod (tools/ui/fuente.py): {@code darkcraft:gotica} (mayusculas de 17 px)
 * para los carteles grandes y {@code darkcraft:gotica_m} (mayusculas de 13 px) para cabeceras, nombres y numeros. Lo que
 * la fuente no trae (japones, por ejemplo) sale con la letra normal del juego. Los textos largos van siempre con la
 * letra de Minecraft.
 */
public final class Letra {
    private Letra() {}

    public static final ResourceLocation GOTICA = new ResourceLocation("darkcraft", "gotica"),
            GOTICA_M = new ResourceLocation("darkcraft", "gotica_m");
    private static final Style GRANDE = Style.EMPTY.withFont(GOTICA), MEDIANA = Style.EMPTY.withFont(GOTICA_M);
    /** Alto de las mayusculas de cada tamano y cuanto hay que bajar el texto para que su borde de arriba caiga en y. */
    public static final int ALTO_G = 17, ALTO_M = 13, BAJA_G = 10, BAJA_M = 6;

    public static MutableComponent g(Component c) {
        return c.copy().withStyle(GRANDE);
    }

    public static MutableComponent g(String s) {
        return Component.literal(s).withStyle(GRANDE);
    }

    public static MutableComponent m(Component c) {
        return c.copy().withStyle(MEDIANA);
    }

    public static MutableComponent m(String s) {
        return Component.literal(s).withStyle(MEDIANA);
    }

    /**
     * Escribe con la letra grande: {@code yArriba} es donde cae el borde de arriba de las mayusculas. Con sombra dura de
     * un pixel abajo a la derecha y contorno oscuro (se lee encima de cualquier cosa).
     */
    public static void grande(GuiGraphics g, Font f, Component c, int x, int yArriba, int color, int borde) {
        contorno(g, f, g(c), x, yArriba + BAJA_G, color, borde);
    }

    public static void mediana(GuiGraphics g, Font f, Component c, int x, int yArriba, int color, int borde) {
        contorno(g, f, m(c), x, yArriba + BAJA_M, color, borde);
    }

    public static int anchoG(Font f, Component c) {
        return f.width(g(c));
    }

    public static int anchoM(Font f, Component c) {
        return f.width(m(c));
    }

    /** El texto con un pixel de contorno alrededor (y dos por debajo, que hace de sombra). */
    public static void contorno(GuiGraphics g, Font f, Component c, int x, int y, int color, int borde) {
        int a = color >>> 24;
        if (a < 8) return;
        int b = ((borde >>> 24) * a / 255) << 24 | (borde & 0xFFFFFF);
        if ((b >>> 24) >= 8) {
            g.drawString(f, c, x - 1, y, b, false);
            g.drawString(f, c, x + 1, y, b, false);
            g.drawString(f, c, x, y - 1, b, false);
            g.drawString(f, c, x, y + 1, b, false);
            g.drawString(f, c, x + 1, y + 1, b, false);
            g.drawString(f, c, x, y + 2, b, false);
        }
        g.drawString(f, c, x, y, color, false);
    }
}
