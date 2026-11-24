package com.darkcraft.client.ui;

import net.minecraft.client.gui.GuiGraphics;

/**
 * El marco de las cinematicas, en pixel art, en vez de las bandas negras con la linea roja: dos bandas de piedra casi
 * negra con una cenefa en el borde de dentro (filo de piedra, oro con tachuelas y una linea roja, y una sombra tramada
 * que cae sobre la imagen) y, colgando del centro de la de arriba, el ojo de la Sombra entre dos alas de oro.
 */
public final class Marco {
    private Marco() {}

    /** Alto de la cenefa que queda dentro de la banda (sus dos ultimas filas son la sombra, ya sobre la imagen). */
    private static final int DENTRO = 7;

    /** Las dos bandas, de {@code bh} pixeles de alto cada una. */
    public static void bandas(GuiGraphics g, int w, int h, int bh) {
        if (bh <= 0) return;
        Pix.blanco();
        Pix.mosaico(g, HudTex.BANDA, 0, 0, w, bh);
        Pix.mosaico(g, HudTex.BANDA, 0, h - bh, w, bh);
        if (bh < DENTRO + 3) {
            // mientras entran o salen (aun muy finas): solo el filo de oro
            g.fill(0, bh - 1, w, bh, 0xFF96702A);
            g.fill(0, h - bh, w, h - bh + 1, 0xFF96702A);
            Pix.mezcla();
            return;
        }
        int yc = bh - DENTRO;
        Pix.tira(g, HudTex.CENEFA, 0, yc, w);
        Pix.tiraVolteada(g, HudTex.CENEFA, 0, h - bh - (HudTex.CENEFA.h() - DENTRO), w);
        // el ojo, con la linea de oro de sus alas sobre la de la cenefa
        Pix.t(g, HudTex.CINE_CENTRO, w / 2 - HudTex.CINE_CENTRO.w() / 2, yc - 2);
        Pix.mezcla();
    }
}
