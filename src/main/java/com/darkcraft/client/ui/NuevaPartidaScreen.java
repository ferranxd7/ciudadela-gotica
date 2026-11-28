package com.darkcraft.client.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.lwjgl.glfw.GLFW;

/**
 * La nueva partida (parte 8a): sustituye a la pantalla de crear mundo de Minecraft (modos de juego, trucos, tipos de
 * mundo...). Solo se elige el nombre del mapa y la dificultad; el mundo es siempre el de Dark Craft (el mar de nubes, en
 * modo aventura, sin trucos y con una semilla al azar). Al empezar se crea y se entra; dentro sale la eleccion de clase.
 */
public class NuevaPartidaScreen extends Screen {
    private static final int ORO = 0xFFE2BF62, LUZ = 0xFFFFF2C4, CLARO = 0xFFEADBC4, APAGADO = 0xFFB8A888, GRIS = 0xFF6F6656,
            ROJO = 0xFFDE3A3A;
    private static final Difficulty[] DIFICULTADES = {Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD};
    private static final String[] DESCRIPCION = {"darkcraft.nueva.facil", "darkcraft.nueva.normal", "darkcraft.nueva.dificil"};
    private static final int BOTON_H = 20;

    private final Screen volver;
    private EditBox nombre;
    private String nombreAntes;
    private int dificultad = 1;
    private boolean creando;
    private Component aviso;

    public NuevaPartidaScreen(Screen volver) {
        super(Component.translatable("darkcraft.nueva.titulo"));
        this.volver = volver;
    }

    // ------------------------------------------------------------------ medidas

    private int ancho() {
        return Math.min(width - 12, 360);
    }

    private int alto() {
        return Math.min(height - 12, 206);
    }

    private int x0() {
        return (width - ancho()) / 2;
    }

    private int y0() {
        return (height - alto()) / 2;
    }

    /** La fila de la dificultad (las flechas a los lados). */
    private int dy() {
        return y0() + 112;
    }

    private int flechaIzq() {
        return x0() + 40;
    }

    private int flechaDer() {
        return x0() + ancho() - 47;
    }

    private int bw() {
        return Math.min(120, (ancho() - 40) / 2);
    }

    private int bx(int i) {
        int total = 2 * bw() + 10;
        return (width - total) / 2 + i * (bw() + 10);
    }

    private int by() {
        return y0() + alto() - 30;
    }

    @Override
    protected void init() {
        String antes = nombre != null ? nombre.getValue() : nombreAntes != null ? nombreAntes
                : Component.translatable("darkcraft.nueva.por_defecto").getString();
        int x = x0(), y = y0(), w = ancho();
        nombre = new EditBox(font, x + 30, y + 64, w - 60, 12, Component.translatable("darkcraft.nueva.nombre"));
        nombre.setMaxLength(32);
        nombre.setBordered(false);
        nombre.setTextColor(LUZ);
        nombre.setValue(antes);
        nombre.setResponder(s -> aviso = null);
        addRenderableWidget(nombre);
        setInitialFocus(nombre);
    }

    // ------------------------------------------------------------------ dibujo

    @Override
    public void renderBackground(GuiGraphics g) {
        if (minecraft.level == null) TituloScreen.fondoMenus(g, width, height);
        else super.renderBackground(g);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int x = x0(), y = y0(), w = ancho(), h = alto();
        Pix.blanco();
        Panel.losa(g, x, y, w, h);
        Letra.grande(g, font, title, (width - Letra.anchoG(font, title)) / 2, y + 9, 0xFFE9C766, 0xFF050408);
        Panel.raya(g, x + 12, x + w - 12, y + 31);
        // el nombre del mapa
        Component et = Component.translatable("darkcraft.nueva.nombre");
        g.drawString(font, et, x + 24, y + 44, CLARO, true);
        Pix.blanco();
        Panel.hueco(g, x + 22, y + 56, w - 44, 24);
        if (nombre.isFocused()) Pix.franja(g, x + 24, x + w - 24, y + 78, y + 79, 0xC0963C, 0.9f);
        // la dificultad: flechas a los lados, el nombre en el centro y lo que quiere decir debajo
        Component ed = Component.translatable("darkcraft.nueva.dificultad");
        g.drawString(font, ed, x + 24, y + 92, CLARO, true);
        Pix.blanco();
        Panel.hueco(g, x + 22, dy() - 6, w - 44, 22);
        boolean izq = dentro(mx, my, flechaIzq() - 4, dy() - 4, 15, 17), der = dentro(mx, my, flechaDer() - 4, dy() - 4, 15, 17);
        Pix.blanco();
        Panel.t(g, dificultad > 0 ? izq ? MenuTex.FLECHA_IZQ_LUZ : MenuTex.FLECHA_IZQ : MenuTex.FLECHA_IZQ_GRIS, flechaIzq(), dy());
        Panel.t(g, dificultad < DIFICULTADES.length - 1 ? der ? MenuTex.FLECHA_DER_LUZ : MenuTex.FLECHA_DER : MenuTex.FLECHA_DER_GRIS, flechaDer(), dy());
        Component dn = DIFICULTADES[dificultad].getDisplayName();
        Letra.mediana(g, font, dn, (width - Letra.anchoM(font, dn)) / 2, dy() - 2, nombre.isFocused() ? ORO : LUZ, 0xFF050408);
        Component dd = Component.translatable(DESCRIPCION[dificultad]);
        g.drawString(font, dd, (width - font.width(dd)) / 2, dy() + 22, APAGADO, true);
        // el aviso (sin nombre) o "creando"
        Component ab = creando ? Component.translatable("darkcraft.nueva.creando") : aviso;
        if (ab != null) g.drawString(font, ab, (width - font.width(ab)) / 2, by() - 14, creando ? ORO : ROJO, true);
        boton(g, 0, Component.translatable("darkcraft.nueva.empezar"), !creando, mx, my);
        boton(g, 1, Component.translatable("darkcraft.opciones.volver"), !creando, mx, my);
        Pix.blanco();
        Pix.mezcla();
        super.render(g, mx, my, pt);
    }

    private void boton(GuiGraphics g, int i, Component texto, boolean activo, int mx, int my) {
        int x = bx(i), y = by(), w = bw();
        boolean luz = activo && dentro(mx, my, x, y, w, BOTON_H);
        Pix.blanco();
        if (!activo) Pix.color(0.55f, 0.55f, 0.55f, 1f);
        if (luz) Pix.tres(g, HudTex.BOTON_LUZ_IZQ, HudTex.BOTON_LUZ_MED, HudTex.BOTON_LUZ_DER, x, y, w);
        else Pix.tres(g, HudTex.BOTON_IZQ, HudTex.BOTON_MED, HudTex.BOTON_DER, x, y, w);
        Pix.blanco();
        String s = texto.getString();
        g.drawString(font, s, x + (w - font.width(s)) / 2, y + 6, !activo ? GRIS : luz ? LUZ : ORO, true);
    }

    private static boolean dentro(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // ------------------------------------------------------------------ manejo

    private void cambiar(int d) {
        int n = Math.max(0, Math.min(DIFICULTADES.length - 1, dificultad + d));
        if (n == dificultad) {
            Panel.nada();
            return;
        }
        dificultad = n;
        Panel.mueve();
    }

    /** Crea el mundo y entra (lo de Minecraft: la pantalla de carga y despues el mundo). */
    private void empezar() {
        if (creando) return;
        String n = nombre.getValue().trim();
        if (n.isEmpty()) {
            aviso = Component.translatable("darkcraft.nueva.sin_nombre");
            Panel.nada();
            return;
        }
        creando = true;
        Panel.acepta();
        String carpeta;
        try {
            carpeta = net.minecraft.FileUtil.findAvailableName(minecraft.getLevelSource().getBaseDir(), n, "");
        } catch (Exception ex) {
            carpeta = "darkcraft_" + System.currentTimeMillis();
        }
        LevelSettings ajustes = new LevelSettings(n, GameType.ADVENTURE, false, DIFICULTADES[dificultad], false, new GameRules(),
                WorldDataConfiguration.DEFAULT);
        com.darkcraft.DarkCraftMod.LOGGER.info("[DarkCraft] nueva partida '{}' (carpeta {}), dificultad {}", n, carpeta, DIFICULTADES[dificultad].getKey());
        minecraft.createWorldOpenFlows().createFreshLevel(carpeta, ajustes, WorldOptions.defaultWithRandomSeed(), WorldPresets::createNormalWorldDimensions);
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int mods) {
        if (creando) return true;
        if (tecla == GLFW.GLFW_KEY_ENTER || tecla == GLFW.GLFW_KEY_KP_ENTER) {
            empezar();
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_ESCAPE) {
            Panel.cancela();
            onClose();
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_UP || tecla == GLFW.GLFW_KEY_DOWN || tecla == GLFW.GLFW_KEY_TAB) {
            setFocused(nombre.isFocused() ? null : nombre);
            Panel.mueve();
            return true;
        }
        if (!nombre.isFocused() && (tecla == GLFW.GLFW_KEY_LEFT || tecla == GLFW.GLFW_KEY_A)) {
            cambiar(-1);
            return true;
        }
        if (!nombre.isFocused() && (tecla == GLFW.GLFW_KEY_RIGHT || tecla == GLFW.GLFW_KEY_D)) {
            cambiar(1);
            return true;
        }
        return super.keyPressed(tecla, scan, mods);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        if (creando) return true;
        if (b == 0) {
            if (dentro(mx, my, flechaIzq() - 4, dy() - 4, 15, 17)) {
                cambiar(-1);
                return true;
            }
            if (dentro(mx, my, flechaDer() - 4, dy() - 4, 15, 17)) {
                cambiar(1);
                return true;
            }
            if (dentro(mx, my, x0() + 22, dy() - 6, ancho() - 44, 22)) {
                // en el centro de la fila: la siguiente (y vuelta a empezar)
                dificultad = (dificultad + 1) % DIFICULTADES.length;
                setFocused(null);
                Panel.mueve();
                return true;
            }
            if (dentro(mx, my, bx(0), by(), bw(), BOTON_H)) {
                empezar();
                return true;
            }
            if (dentro(mx, my, bx(1), by(), bw(), BOTON_H)) {
                Panel.cancela();
                onClose();
                return true;
            }
        }
        return super.mouseClicked(mx, my, b);
    }

    @Override
    public void resize(net.minecraft.client.Minecraft mc, int w, int h) {
        nombreAntes = nombre == null ? null : nombre.getValue();
        super.resize(mc, w, h);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(volver);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ------------------------------------------------------------------ para las pruebas

    public void probarNombre(String n) {
        nombre.setValue(n);
    }

    public void probarDificultad(int d) {
        setFocused(null);
        cambiar(d);
    }

    public void probarEmpezar() {
        empezar();
    }

    public String resumen() {
        return "nueva partida: nombre '" + nombre.getValue() + "', dificultad " + DIFICULTADES[dificultad].getKey() + (creando ? " (creando)" : "")
                + (aviso != null ? " aviso " + aviso.getString() : "");
    }
}
