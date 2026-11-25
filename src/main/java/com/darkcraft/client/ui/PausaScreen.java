package com.darkcraft.client.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * La pausa (ESC), en pixel art (parte 2f): una losa con el nombre de la partida y lo que se lleva (nivel y ecos) y los
 * botones de piedra y oro: volver, el menu (R), opciones, mods, abrir a LAN (solo en un mundo de un jugador) y salir.
 * Sustituye a la pausa de Minecraft (la de F3 + ESC, sin menu, se deja: {@link Pantallas}).
 */
public class PausaScreen extends Screen {
    private static final int ANCHO = 190, BOTON_W = 160, PASO = 24;
    private static final int ORO = 0xFFE2BF62, LUZ = 0xFFFFF2C4, APAGADO = 0xFFB8A888;

    private record Opcion(String clave, Runnable accion) {}

    private final List<Opcion> opciones = new ArrayList<>();
    private int sel, t;
    private boolean saliendo;

    public PausaScreen() {
        super(Component.translatable("menu.game"));
    }

    @Override
    protected void init() {
        opciones.clear();
        opciones.add(new Opcion("darkcraft.pausa.volver", () -> minecraft.setScreen(null)));
        opciones.add(new Opcion("darkcraft.pausa.menu", () -> MenuScreen.abrir(MenuScreen.EQUIPO)));
        opciones.add(new Opcion("darkcraft.pausa.opciones", () -> minecraft.setScreen(new OpcionesScreen(this))));
        opciones.add(new Opcion("darkcraft.pausa.mods", () -> minecraft.setScreen(new ModsScreen(this))));
        if (minecraft.hasSingleplayerServer() && minecraft.getSingleplayerServer() != null && !minecraft.getSingleplayerServer().isPublished())
            opciones.add(new Opcion("darkcraft.pausa.lan", () -> minecraft.setScreen(new ShareToLanScreen(this))));
        opciones.add(new Opcion(minecraft.isLocalServer() ? "darkcraft.pausa.salir" : "darkcraft.pausa.desconectar", this::salir));
        sel = Mth.clamp(sel, 0, opciones.size() - 1);
    }

    @Override
    public void tick() {
        t++;
    }

    /** Guardar y salir al titulo (en un servidor, desconectar y a la lista de servidores). */
    private void salir() {
        if (saliendo || minecraft.level == null) return;
        saliendo = true;
        boolean local = minecraft.isLocalServer();
        minecraft.level.disconnect();
        if (local) minecraft.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
        else minecraft.clearLevel();
        TitleScreen titulo = new TitleScreen();
        minecraft.setScreen(local ? titulo : new JoinMultiplayerScreen(titulo));
    }

    private int alto() {
        return 64 + opciones.size() * PASO + 8;
    }

    private int x0() {
        return (width - ANCHO) / 2;
    }

    private int y0() {
        return Math.max(4, (height - alto()) / 2);
    }

    private int botonY(int i) {
        return y0() + 62 + i * PASO;
    }

    private int en(double mx, double my) {
        int bx = (width - BOTON_W) / 2;
        if (mx < bx || mx >= bx + BOTON_W) return -1;
        for (int i = 0; i < opciones.size(); i++) if (my >= botonY(i) && my < botonY(i) + 20) return i;
        return -1;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        float a = Mth.clamp((t + pt) / 4f, 0f, 1f);
        g.fill(0, 0, width, height, (Math.round(90 * a) << 24) | 0x050408);
        int x = x0(), y = y0();
        Pix.blanco();
        Panel.losa(g, x, y, ANCHO, alto());
        Component titulo = Component.translatable("darkcraft.pausa.titulo");
        Letra.grande(g, font, titulo, (width - Letra.anchoG(font, titulo)) / 2, y + 10, 0xFFE9C766, 0xFF050408);
        // la partida y lo que se lleva
        String nombre = minecraft.hasSingleplayerServer() && minecraft.getSingleplayerServer() != null
                ? minecraft.getSingleplayerServer().getWorldData().getLevelName()
                : minecraft.getCurrentServer() != null ? minecraft.getCurrentServer().name : "";
        Component linea = Component.translatable("darkcraft.pausa.linea", nombre, ClientRol.nivel(), Hud.numero(ClientRol.ecos));
        String l = linea.getString();
        while (font.width(l) > ANCHO - 20 && l.length() > 4) l = l.substring(0, l.length() - 4) + "...";
        g.drawString(font, l, (width - font.width(l)) / 2, y + 34, APAGADO, true);
        Panel.raya(g, x + 12, x + ANCHO - 12, y + 48);
        int encima = en(mx, my);
        for (int i = 0; i < opciones.size(); i++) {
            int bx = (width - BOTON_W) / 2, by = botonY(i);
            boolean luz = i == sel || i == encima;
            Pix.blanco();
            if (luz) Pix.tres(g, HudTex.BOTON_LUZ_IZQ, HudTex.BOTON_LUZ_MED, HudTex.BOTON_LUZ_DER, bx, by, BOTON_W);
            else Pix.tres(g, HudTex.BOTON_IZQ, HudTex.BOTON_MED, HudTex.BOTON_DER, bx, by, BOTON_W);
            Pix.blanco();
            Component c = Component.translatable(opciones.get(i).clave());
            g.drawString(font, c, bx + (BOTON_W - font.width(c)) / 2, by + 6, luz ? LUZ : ORO, true);
        }
        Pix.blanco();
        Pix.mezcla();
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int mods) {
        if (tecla == GLFW.GLFW_KEY_UP || minecraft.options.keyUp.matches(tecla, scan)) {
            sel = Math.floorMod(sel - 1, opciones.size());
            Panel.mueve();
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_DOWN || minecraft.options.keyDown.matches(tecla, scan)) {
            sel = Math.floorMod(sel + 1, opciones.size());
            Panel.mueve();
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_ENTER || tecla == GLFW.GLFW_KEY_KP_ENTER || tecla == GLFW.GLFW_KEY_SPACE
                || minecraft.options.keyInventory.matches(tecla, scan)) {
            elegir(sel);
            return true;
        }
        return super.keyPressed(tecla, scan, mods);
    }

    @Override
    public void mouseMoved(double mx, double my) {
        int i = en(mx, my);
        if (i >= 0 && i != sel) {
            sel = i;
            Panel.mueve();
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        int i = en(mx, my);
        if (i >= 0 && b == 0) {
            elegir(i);
            return true;
        }
        return super.mouseClicked(mx, my, b);
    }

    private void elegir(int i) {
        if (i < 0 || i >= opciones.size()) return;
        sel = i;
        Panel.acepta();
        opciones.get(i).accion().run();
    }

    /** Para la prueba de la CI. */
    public String resumen() {
        StringBuilder b = new StringBuilder("pausa:");
        for (Opcion o : opciones) b.append(' ').append(o.clave().replace("darkcraft.pausa.", ""));
        return b + " (elegida " + sel + ")";
    }
}
