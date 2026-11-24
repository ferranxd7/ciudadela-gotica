package com.darkcraft.client.ui;

import com.darkcraft.net.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * El menu del santuario (se abre al descansar): una losa de piedra a la izquierda con el nombre del santuario, el nivel
 * y los ecos del jugador y lo que se puede hacer alli: subir de nivel, repartir los frascos entre vida y mana, y
 * marcharse. Con el raton o con el teclado (arriba y abajo, la tecla del aviso o INTRO para elegir, izquierda y derecha
 * para repartir, ESC para salir). El juego no se para: si pegan al jugador, se cierra solo.
 */
public class SantuarioScreen extends Screen {
    private static final int FILA = 18, CABECERA = 54, PIE = 40;
    private static final String[] OPCIONES = {"nivel", "frascos", "salir"};
    private static final int NIVEL = 0, FRASCOS = 1, SALIR = 2;

    private final BlockPos pos;
    private final Component nombre;
    private int sel, t;

    public static void abrir(Net.Santuario m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.setScreen(new SantuarioScreen(m.pos(), Component.translatable(m.nombre())));
        Panel.abre();
    }

    SantuarioScreen(BlockPos pos, Component nombre) {
        super(nombre);
        this.pos = pos;
        this.nombre = nombre;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        t++;
        // si le pegan o le apartan del santuario, fuera el menu (que pueda defenderse)
        if (minecraft.player == null || !minecraft.player.isAlive() || minecraft.player.hurtTime > 0
                || minecraft.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 8.5 * 8.5)
            minecraft.setScreen(null);
    }

    // ------------------------------------------------------------------ medidas

    private int ancho() {
        return Mth.clamp(Letra.anchoM(font, nombre) + 50, 208, Math.max(208, width - 16));
    }

    private int alto() {
        return CABECERA + OPCIONES.length * FILA + PIE;
    }

    private int px() {
        return Mth.clamp(width / 24, 6, 28);
    }

    private int py() {
        return Math.max(4, (height - alto()) / 2 - 12);
    }

    private int filaY(int i) {
        return py() + CABECERA + i * FILA;
    }

    private int sobre(double mx, double my) {
        if (mx < px() + 6 || mx >= px() + ancho() - 6) return -1;
        for (int i = 0; i < OPCIONES.length; i++) if (my >= filaY(i) && my < filaY(i) + FILA) return i;
        return -1;
    }

    /** Donde empieza, en la fila de los frascos, el reparto: flecha, frasco rojo, cuantos, frasco azul, cuantos, flecha. */
    private int repartoX() {
        return px() + ancho() - 104;
    }

    // ------------------------------------------------------------------ dibujo

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        int x = px(), y = py(), w = ancho(), h = alto();
        float abre = Mth.clamp((t + pt) / 5f, 0f, 1f);
        // la pantalla se oscurece por la izquierda, donde va la losa
        int tope = Math.min(width, x + w + 150);
        for (int i = 0; i < 30; i++) {
            int a = Math.round(170 * abre * (1f - i / 30f));
            if (a > 3) g.fill(tope * i / 30, 0, tope * (i + 1) / 30, height, (a << 24) | 0x050408);
        }
        g.pose().pushPose();
        g.pose().translate(-(1f - abre) * 14f, 0f, 0f);
        Pix.blanco();
        Panel.losa(g, x, y, w, h);
        // cabecera: la hoguera, el nombre, y debajo el nivel y los ecos
        Panel.t(g, MenuTex.SANTUARIO, x + 12, y + 11);
        Letra.mediana(g, font, nombre, x + 34, y + 12, 0xFFE9C766, 0xFF050408);
        Component nivel = Component.translatable("darkcraft.menu.nivel").append(" " + ClientRol.nivel());
        g.drawString(font, nivel, x + 14, y + 34, 0xFFEADBC4, true);
        String ecos = Hud.numero(ClientRol.ecos);
        int ew = font.width(ecos);
        g.drawString(font, ecos, x + w - 14 - ew, y + 34, 0xFFE2BF62, true);
        Pix.blanco();
        Pix.t(g, HudTex.ECO_0, x + w - 14 - ew - 18, y + 29);
        Panel.raya(g, x + 10, x + w - 10, y + 47);
        // las opciones
        int encima = sobre(mx, my);
        for (int i = 0; i < OPCIONES.length; i++) {
            int fy = filaY(i);
            boolean elegida = i == sel;
            if (elegida) {
                Pix.blanco();
                Panel.fila(g, x + 8, fy, w - 16);
            }
            g.drawString(font, Component.translatable("darkcraft.menu.santuario." + OPCIONES[i]), x + 18, fy + 5,
                    elegida ? 0xFFFFF2C4 : i == encima ? 0xFFEADBC4 : 0xFFC8B89A, true);
            if (i == FRASCOS) reparto(g, fy, mx, my, elegida);
        }
        // pie: lo que hace la opcion elegida
        Panel.raya(g, x + 10, x + w - 10, y + CABECERA + OPCIONES.length * FILA + 4);
        List<FormattedCharSequence> lineas = font.split(Component.translatable("darkcraft.menu.santuario." + OPCIONES[sel] + ".desc"), w - 28);
        for (int i = 0; i < Math.min(3, lineas.size()); i++)
            g.drawString(font, lineas.get(i), x + 14, y + CABECERA + OPCIONES.length * FILA + 10 + i * 9, 0xFFB8A888, false);
        g.pose().popPose();
        // las teclas, debajo de la losa
        int ty = Math.min(height - 16, y + h + 6), tx = x + 4;
        Component arriba = Component.literal("↑↓");
        tx = Panel.tecla(g, font, arriba, Component.translatable("darkcraft.menu.tecla.mover"), tx, ty, 0xFFB8A888) + 10;
        tx = Panel.tecla(g, font, minecraft.options.keyInventory.getTranslatedKeyMessage(), Component.translatable("darkcraft.menu.tecla.elegir"), tx, ty, 0xFFB8A888) + 10;
        Panel.tecla(g, font, Component.literal("Esc"), Component.translatable("darkcraft.menu.tecla.salir"), tx, ty, 0xFFB8A888);
        Pix.blanco();
        Pix.mezcla();
    }

    private void reparto(GuiGraphics g, int fy, int mx, int my, boolean elegida) {
        int x0 = repartoX();
        int total = ClientRol.frascosVida + ClientRol.frascosMana;
        boolean masVida = ClientRol.frascosVida < total, masMana = ClientRol.frascosMana < total;
        Pix.blanco();
        Panel.t(g, !masVida ? MenuTex.FLECHA_IZQ_GRIS : enFlecha(mx, my, fy, false) ? MenuTex.FLECHA_IZQ_LUZ : MenuTex.FLECHA_IZQ, x0, fy + 5);
        Panel.t(g, ClientRol.frascosVida > 0 ? MenuTex.FRASCO_VIDA : MenuTex.FRASCO_VIDA_VACIO, x0 + 11, fy + 1);
        g.drawString(font, Integer.toString(ClientRol.frascosVida), x0 + 29, fy + 5, elegida ? 0xFFFFF2C4 : 0xFFE2BF62, true);
        Pix.blanco();
        Panel.t(g, ClientRol.frascosMana > 0 ? MenuTex.FRASCO_MANA : MenuTex.FRASCO_MANA_VACIO, x0 + 44, fy + 1);
        g.drawString(font, Integer.toString(ClientRol.frascosMana), x0 + 62, fy + 5, elegida ? 0xFFFFF2C4 : 0xFFE2BF62, true);
        Pix.blanco();
        Panel.t(g, !masMana ? MenuTex.FLECHA_DER_GRIS : enFlecha(mx, my, fy, true) ? MenuTex.FLECHA_DER_LUZ : MenuTex.FLECHA_DER, x0 + 79, fy + 5);
    }

    private boolean enFlecha(double mx, double my, int fy, boolean derecha) {
        int x0 = repartoX() + (derecha ? 79 : 0);
        return mx >= x0 - 3 && mx < x0 + 10 && my >= fy && my < fy + FILA;
    }

    // ------------------------------------------------------------------ lo que se hace

    /** Pasa un frasco al lado de la vida (-1) o al del mana (+1). */
    private void repartir(int hacia) {
        int total = ClientRol.frascosVida + ClientRol.frascosMana;
        int vida = Mth.clamp(ClientRol.frascosVida - hacia, 0, total);
        if (vida == ClientRol.frascosVida) {
            Panel.nada();
            return;
        }
        // se apunta ya aqui (el servidor lo confirma con la ficha)
        ClientRol.frascosVida = ClientRol.quedanVida = vida;
        ClientRol.frascosMana = ClientRol.quedanMana = total - vida;
        Net.CH.sendToServer(new Net.RepartoFrascos(vida));
        Panel.mueve();
    }

    private void elegir(int i) {
        switch (i) {
            case NIVEL -> {
                Panel.acepta();
                minecraft.setScreen(new NivelScreen(this));
            }
            case FRASCOS -> Panel.nada();
            default -> onClose();
        }
    }

    private void mover(int d) {
        int n = Math.floorMod(sel + d, OPCIONES.length);
        if (n != sel) {
            sel = n;
            Panel.mueve();
        }
    }

    @Override
    public void onClose() {
        Panel.cancela();
        super.onClose();
    }

    @Override
    public void mouseMoved(double mx, double my) {
        int i = sobre(mx, my);
        if (i >= 0 && i != sel) {
            sel = i;
            Panel.mueve();
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int boton) {
        if (boton != 0) return false;
        int i = sobre(mx, my);
        if (i < 0) return false;
        sel = i;
        if (i == FRASCOS) {
            if (enFlecha(mx, my, filaY(i), false)) repartir(-1);
            else if (enFlecha(mx, my, filaY(i), true)) repartir(1);
            return true;
        }
        elegir(i);
        return true;
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int mods) {
        var o = minecraft.options;
        if (tecla == GLFW.GLFW_KEY_UP || o.keyUp.matches(tecla, scan)) {
            mover(-1);
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_DOWN || o.keyDown.matches(tecla, scan)) {
            mover(1);
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_LEFT || o.keyLeft.matches(tecla, scan)) {
            if (sel == FRASCOS) repartir(-1);
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_RIGHT || o.keyRight.matches(tecla, scan)) {
            if (sel == FRASCOS) repartir(1);
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_ENTER || tecla == GLFW.GLFW_KEY_KP_ENTER || tecla == GLFW.GLFW_KEY_SPACE || o.keyInventory.matches(tecla, scan)) {
            elegir(sel);
            return true;
        }
        return super.keyPressed(tecla, scan, mods);
    }

    /** Para la prueba de la CI. */
    public String resumen() {
        return "santuario '" + nombre.getString() + "' opcion " + OPCIONES[sel];
    }
}
