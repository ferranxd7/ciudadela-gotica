package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.hechizos.Hechizo;
import com.darkcraft.hechizos.HechizoItem;
import com.darkcraft.net.Net;
import com.darkcraft.rol.Atributo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Memorizar hechizos (desde el menu del santuario): la losa de piedra con los hechizos que se saben (los memorizados,
 * marcados con oro, y los que se llevan en el inventario sin memorizar). INTRO (o clic) memoriza o quita el elegido, si
 * cabe en los huecos de memoria (segun la Mente). Lo comprueba y lo guarda el servidor ({@link Net.Memorizar}).
 */
public class MemoriaScreen extends Screen {
    private static final int FILA = 18, CABECERA = 50, PIE = 44, FILAS = 8;

    private final Screen volver;
    private int sel, desde, t;

    public MemoriaScreen(Screen volver) {
        super(Component.translatable("darkcraft.menu.santuario.memoria"));
        this.volver = volver;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        t++;
        if (minecraft.player == null || !minecraft.player.isAlive() || minecraft.player.hurtTime > 0) minecraft.setScreen(null);
    }

    /**
     * Lo que sale en la lista: los memorizados y los que se llevan sin memorizar, siempre en el mismo orden (el de los
     * hechizos), para que la fila elegida no cambie de hechizo al memorizar o quitar uno.
     */
    private List<Hechizo> lista() {
        List<Hechizo> l = new ArrayList<>();
        Inventory inv = minecraft.player.getInventory();
        for (Hechizo h : Hechizo.values()) {
            boolean esta = ClientRol.memoria.contains(h);
            for (int i = 0; i < inv.getContainerSize() && !esta; i++) esta = HechizoItem.de(inv.getItem(i)) == h;
            if (esta) l.add(h);
        }
        return l;
    }

    private int huecos() {
        return Hechizo.memoria(ClientRol.atributos[Atributo.MENTE.ordinal()]);
    }

    private int ancho() {
        return Mth.clamp(width / 2, 230, 300);
    }

    private int alto() {
        return CABECERA + FILAS * FILA + PIE;
    }

    private int px() {
        return Mth.clamp(width / 24, 6, 28);
    }

    private int py() {
        return Math.max(4, (height - alto()) / 2 - 12);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        int x = px(), y = py(), w = ancho(), h = alto();
        int tope = Math.min(width, x + w + 150);
        for (int i = 0; i < 30; i++) {
            int a = Math.round(170 * (1f - i / 30f));
            if (a > 3) g.fill(tope * i / 30, 0, tope * (i + 1) / 30, height, (a << 24) | 0x050408);
        }
        Pix.blanco();
        Panel.losa(g, x, y, w, h);
        Panel.t(g, MenuTex.ICO_HECHIZO, x + 12, y + 10);
        Letra.mediana(g, font, getTitle(), x + 34, y + 12, 0xFFE9C766, 0xFF050408);
        List<Hechizo> l = lista();
        int usados = 0;
        for (Hechizo m : ClientRol.memoria) usados += m.huecos;
        String mem = usados + " / " + huecos();
        g.drawString(font, Component.translatable("darkcraft.menu.memoria"), x + 14, y + 32, 0xFFEADBC4, true);
        g.drawString(font, mem, x + w - 14 - font.width(mem), y + 32, usados <= huecos() ? 0xFFE2BF62 : 0xFFE0564E, true);
        Panel.raya(g, x + 10, x + w - 10, y + 44);
        sel = Mth.clamp(sel, 0, Math.max(0, l.size() - 1));
        if (sel < desde) desde = sel;
        if (sel >= desde + FILAS) desde = sel - FILAS + 1;
        if (l.isEmpty()) g.drawString(font, Component.translatable("darkcraft.menu.memoria.vacia"), x + 16, y + CABECERA + 5, 0xFF8A8072, true);
        for (int i = 0; i < Math.min(FILAS, l.size() - desde); i++) {
            int k = desde + i, fy = y + CABECERA + i * FILA;
            Hechizo hz = l.get(k);
            boolean memorizado = ClientRol.memoria.contains(hz), elegida = k == sel;
            Pix.blanco();
            if (elegida) Panel.fila(g, x + 8, fy, w - 16);
            g.renderItem(new ItemStack(DarkCraftMod.HECHIZOS.get(hz).get()), x + 14, fy + 1);
            if (memorizado) {
                Pix.blanco();
                g.pose().pushPose();
                g.pose().translate(0, 0, 200);
                Panel.t(g, MenuTex.EQUIPADO, x + 13, fy);
                g.pose().popPose();
            }
            g.drawString(font, Component.translatable("item.darkcraft.hechizo_" + hz.id), x + 36, fy + 5,
                    elegida ? 0xFFFFF2C4 : memorizado ? 0xFFE9C766 : 0xFFC8B89A, true);
            String coste = hz.huecos + "  ·  " + hz.mana;
            g.drawString(font, coste, x + w - 16 - font.width(coste), fy + 5, 0xFF9BC4F0, true);
        }
        // pie: lo que hace el elegido
        int py0 = y + CABECERA + FILAS * FILA + 4;
        Panel.raya(g, x + 10, x + w - 10, py0);
        if (!l.isEmpty()) {
            Hechizo hz = l.get(sel);
            List<FormattedCharSequence> lineas = font.split(Component.translatable("item.darkcraft.hechizo_" + hz.id + ".desc"), w - 28);
            for (int i = 0; i < Math.min(2, lineas.size()); i++) g.drawString(font, lineas.get(i), x + 14, py0 + 6 + i * 9, 0xFFB8A888, false);
            boolean llega = ClientRol.atributos[hz.escuela.atributo.ordinal()] >= hz.requisito;
            Component req = Component.translatable(hz.escuela == Hechizo.Escuela.HECHICERIA ? "darkcraft.hechizo.hechiceria" : "darkcraft.hechizo.milagro",
                    hz.mana, hz.huecos, hz.requisito);
            g.drawString(font, req, x + 14, py0 + 26, llega ? 0xFF9A9080 : 0xFFE0564E, false);
        }
        int ty = Math.min(height - 16, y + h + 6), tx = x + 4;
        tx = Panel.tecla(g, font, Component.literal("↑↓"), Component.translatable("darkcraft.menu.tecla.mover"), tx, ty, 0xFFB8A888) + 10;
        tx = Panel.tecla(g, font, minecraft.options.keyInventory.getTranslatedKeyMessage(), Component.translatable("darkcraft.menu.memorizar"), tx, ty, 0xFFB8A888) + 10;
        Panel.tecla(g, font, Component.literal("Esc"), Component.translatable("darkcraft.menu.volver"), tx, ty, 0xFFB8A888);
        Pix.blanco();
        Pix.mezcla();
    }

    /** Memoriza o quita el elegido (si al memorizar no cabe, no hace nada). */
    private void alternar() {
        List<Hechizo> l = lista();
        if (l.isEmpty()) {
            Panel.nada();
            return;
        }
        Hechizo h = l.get(sel);
        List<Hechizo> nueva = new ArrayList<>(ClientRol.memoria);
        if (nueva.contains(h)) nueva.remove(h);
        else {
            int usados = 0;
            for (Hechizo m : nueva) usados += m.huecos;
            if (usados + h.huecos > huecos()) {
                Carteles.aviso(Component.translatable("darkcraft.hechizo.sin_memoria"));
                Panel.nada();
                return;
            }
            nueva.add(h);
        }
        Net.CH.sendToServer(new Net.Memorizar(nueva.stream().map(m -> m.id).toArray(String[]::new)));
        // se apunta ya aqui (el servidor lo confirma con la ficha)
        ClientRol.memoria = nueva;
        Panel.acepta();
    }

    private int sobre(double mx, double my) {
        int x = px(), y = py();
        if (mx < x + 6 || mx >= x + ancho() - 6) return -1;
        int i = (int) Math.floor((my - (y + CABECERA)) / FILA);
        if (i < 0 || i >= FILAS) return -1;
        int k = desde + i;
        return k < lista().size() ? k : -1;
    }

    @Override
    public void mouseMoved(double mx, double my) {
        int k = sobre(mx, my);
        if (k >= 0 && k != sel) {
            sel = k;
            Panel.mueve();
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        int k = sobre(mx, my);
        if (k < 0 || b != 0) return false;
        sel = k;
        alternar();
        return true;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double r) {
        sel = Mth.clamp(sel + (r > 0 ? -1 : 1), 0, Math.max(0, lista().size() - 1));
        return true;
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int mods) {
        var o = minecraft.options;
        if (tecla == GLFW.GLFW_KEY_UP || o.keyUp.matches(tecla, scan)) {
            if (sel > 0) {
                sel--;
                Panel.mueve();
            }
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_DOWN || o.keyDown.matches(tecla, scan)) {
            if (sel < lista().size() - 1) {
                sel++;
                Panel.mueve();
            }
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_ENTER || tecla == GLFW.GLFW_KEY_KP_ENTER || tecla == GLFW.GLFW_KEY_SPACE || o.keyInventory.matches(tecla, scan)) {
            alternar();
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_ESCAPE) {
            Panel.cancela();
            minecraft.setScreen(volver);
            return true;
        }
        return super.keyPressed(tecla, scan, mods);
    }

    /** Para la prueba de la CI. */
    public String resumen() {
        return "memoria " + ClientRol.memoria + " lista " + lista() + " elegido " + sel;
    }
}
