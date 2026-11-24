package com.darkcraft.client.fortaleza;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.client.espejo.EspejoHud;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * El libro del acertijo del maestre Velan: tres paginas (la ultima con el sello) y, al pulsar el sello, la forja del
 * amuleto dibujada (la receta: diamante, netherita, diamante / netherita, llave de hueso, netherita).
 */
public class AcertijoScreen extends Screen {
    private int pag = 0, paginas = 3;
    private boolean forja;
    private PageButton atras, adelante;
    private long abierto = net.minecraft.Util.getMillis();

    public AcertijoScreen() {
        super(Component.translatable("darkcraft.acertijo.titulo"));
    }

    public static void abrir() {
        Minecraft.getInstance().setScreen(new AcertijoScreen());
    }

    /** Para la prueba de la CI: ir a una pagina y ver (o no) la forja. */
    public void probar(int pagina, boolean verForja) {
        pag = Math.max(0, Math.min(paginas - 1, pagina));
        forja = verForja;
        actualizar();
    }

    @Override
    protected void init() {
        try {
            paginas = Math.max(1, Integer.parseInt(I18n.get("darkcraft.acertijo.paginas")));
        } catch (NumberFormatException e) {
            paginas = 3;
        }
        int x = (width - 192) / 2, y = 2;
        atras = addRenderableWidget(new PageButton(x + 43, y + 157, false, b -> pasar(-1), true));
        adelante = addRenderableWidget(new PageButton(x + 116, y + 157, true, b -> pasar(1), true));
        actualizar();
    }

    private void pasar(int d) {
        if (forja) forja = false;
        else pag = Math.max(0, Math.min(paginas - 1, pag + d));
        actualizar();
    }

    private void actualizar() {
        if (atras != null) atras.visible = forja || pag > 0;
        if (adelante != null) adelante.visible = !forja && pag < paginas - 1;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int[] sello() {
        int x = (width - 192) / 2, y = 2;
        return new int[]{x + 96 - 9, y + 122, 18};
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        if (!forja && pag == paginas - 1) {
            int[] s = sello();
            if (mx >= s[0] - 4 && mx < s[0] + s[2] + 4 && my >= s[1] - 4 && my < s[1] + s[2] + 14) {
                forja = true;
                actualizar();
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 0.8f));
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 0.7f));
                return true;
            }
        }
        return super.mouseClicked(mx, my, b);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int x = (width - 192) / 2, y = 2;
        g.blit(BookViewScreen.BOOK_LOCATION, x, y, 0, 0, 192, 192);
        int tx = x + 36, ty = y + 30, tw = 114;
        if (forja) {
            dibujarForja(g, x, y, mx, my);
        } else {
            Component pg = Component.translatable("book.pageIndicator", pag + 1, paginas);
            g.drawString(font, pg, x + 148 - font.width(pg), y + 16, 0x000000, false);
            int yy = ty;
            if (pag == 0) {
                Component tit = Component.translatable("darkcraft.acertijo.titulo").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
                for (FormattedCharSequence l : font.split(tit, tw)) {
                    g.drawString(font, l, tx, yy, 0x8A1010, false);
                    yy += 9;
                }
                Component aut = Component.translatable("darkcraft.acertijo.autor").withStyle(ChatFormatting.ITALIC);
                g.drawString(font, aut, tx, yy, 0x5A2A10, false);
                yy += 14;
            }
            List<FormattedCharSequence> ls = font.split(Component.translatable("darkcraft.acertijo.p" + (pag + 1)), tw);
            for (FormattedCharSequence l : ls) {
                if (yy > y + 150) break;
                g.drawString(font, l, tx, yy, 0x000000, false);
                yy += 9;
            }
            if (pag == paginas - 1) {
                int[] s = sello();
                boolean sobre = mx >= s[0] - 4 && mx < s[0] + s[2] + 4 && my >= s[1] - 4 && my < s[1] + s[2] + 14;
                float lat = 1f + 0.06f * (float) Math.sin((net.minecraft.Util.getMillis() - abierto) / 260.0);
                float l = s[2] * (sobre ? 1.15f : lat);
                EspejoHud.mezcla();
                com.darkcraft.client.espejo.EspejoHud.icono(g, 28, s[0] + s[2] / 2f, s[1] + s[2] / 2f, l);
                Component bt = Component.translatable("darkcraft.acertijo.boton");
                g.drawString(font, bt, x + 96 - font.width(bt) / 2, s[1] + s[2] + 3, sobre ? 0xA01818 : 0x5A2A10, false);
            }
        }
        super.render(g, mx, my, pt);
    }

    /** La forja dibujada: la cuadricula de la mesa de crafteo con los objetos y el amuleto que sale. */
    private void dibujarForja(GuiGraphics g, int x, int y, int mx, int my) {
        // el titulo, centrado en la pagina y mas pequeno si no cabe (en negrita se salia por la derecha)
        Component tit = Component.translatable("darkcraft.acertijo.receta").withStyle(ChatFormatting.BOLD);
        int tw = font.width(tit);
        float sc = Math.min(1f, 112f / Math.max(1, tw));
        g.pose().pushPose();
        g.pose().translate(x + 93, y + 22 + (1 - sc) * 4, 0);
        g.pose().scale(sc, sc, 1f);
        g.drawString(font, tit, -tw / 2, 0, 0x8A1010, false);
        g.pose().popPose();
        ItemStack D = new ItemStack(Items.DIAMOND), N = new ItemStack(Items.NETHERITE_INGOT), K = new ItemStack(DarkCraftMod.LLAVE_HUESO.get());
        ItemStack[] rejilla = {D, N, D, N, K, N, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
        int gx = x + 40, gy = y + 46, c = 20;
        // fondo de la mesa (tinta sobre el pergamino)
        g.fill(gx - 3, gy - 3, gx + 3 * c + 1, gy + 3 * c + 1, 0x40301808);
        ItemStack sobre = ItemStack.EMPTY;
        for (int i = 0; i < 9; i++) {
            int sx = gx + (i % 3) * c, sy = gy + (i / 3) * c;
            g.fill(sx, sy, sx + c - 2, sy + c - 2, 0x30000000);
            g.fill(sx, sy + c - 3, sx + c - 2, sy + c - 2, 0x40000000);
            if (!rejilla[i].isEmpty()) {
                g.renderItem(rejilla[i], sx + 1, sy + 1);
                if (mx >= sx && mx < sx + c - 2 && my >= sy && my < sy + c - 2) sobre = rejilla[i];
            }
        }
        // flecha y el amuleto
        int ax = gx + 3 * c + 6, ay = gy + c + 4;
        for (int k = 0; k < 14; k++) g.fill(ax + k, ay + 4, ax + k + 1, ay + 6, 0xFF5A2A10);
        for (int k = 0; k < 5; k++) g.fill(ax + 13 - k, ay + 5 - k, ax + 14 - k, ay + 5 + k + 1, 0xFF5A2A10);
        int rx = ax + 20, ry = gy + c - 1;
        g.fill(rx - 3, ry - 3, rx + 21, ry + 21, 0x50701010);
        ItemStack am = new ItemStack(DarkCraftMod.AMULETO_HUESO.get());
        g.renderItem(am, rx + 1, ry + 1);
        if (mx >= rx - 3 && mx < rx + 21 && my >= ry - 3 && my < ry + 21) sobre = am;
        // la cuenta, en palabras: dos diamantes, tres lingotes de netherita y la llave
        Component lista = Component.translatable("darkcraft.acertijo.lista");
        int yy = gy + 3 * c + 10;
        for (FormattedCharSequence l : font.split(lista, 114)) {
            g.drawString(font, l, x + 36, yy, 0x3A2010, false);
            yy += 9;
        }
        if (!sobre.isEmpty()) g.renderTooltip(font, sobre, mx, my);
    }
}
