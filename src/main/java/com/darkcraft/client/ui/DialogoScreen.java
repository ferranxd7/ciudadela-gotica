package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.dialogo.Dialogos;
import com.darkcraft.net.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hablar con un personaje: la caja de piedra abajo con su retrato en pixel art, su nombre en letra gotica y la frase, que
 * se escribe al ritmo de su voz (lo que dura la frase, de {@link Dialogos#habla}); [E] (o Intro, Espacio, clic) acaba de
 * escribirla o pasa a la siguiente. En los menus, 2-4 respuestas (flechas o raton; las ya preguntadas, mas apagadas).
 * Al acabar una frase con accion se la pide al servidor ({@link Net.DialogoAccion}). Se cierra con ESC o si pegan al
 * jugador.
 */
public class DialogoScreen extends Screen {
    private static final int ALTO = 86, RETRATO = 64;

    private final int entidad;
    private final Dialogos.Personaje pj;
    private Dialogos.Nodo nodo;
    private final Set<Integer> preguntadas = new HashSet<>();
    private SoundInstance voz;
    private List<String> lineas = List.of();
    private int letras, t, abierto, sel = 0, encima = -1;
    /** Se ha pedido algo al servidor (la forja, la tienda): se espera a que abra su pantalla (o se cierra sola). */
    private int esperando;
    private float escritas, ritmo;

    private DialogoScreen(int entidad, Dialogos.Personaje pj) {
        super(Component.translatable(pj.nombre()));
        this.entidad = entidad;
        this.pj = pj;
    }

    public static void abrir(Net.Dialogo m) {
        Minecraft mc = Minecraft.getInstance();
        Dialogos.Personaje p = Dialogos.de(m.personaje());
        if (p == null || mc.player == null) return;
        DialogoScreen s = new DialogoScreen(m.entidad(), p);
        mc.setScreen(s);
        s.ir(m.nodo());
        Panel.abre();
    }

    /** Lo pide la guia al acabar "dame tus ecos": subir de nivel. */
    public static void abrirNivel() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.setScreen(new NivelScreen(null));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ------------------------------------------------------------------ medidas

    private int ancho() {
        return Math.min(width - 16, 430);
    }

    private int x0() {
        return (width - ancho()) / 2;
    }

    private int y0() {
        return height - ALTO - 8;
    }

    private int tx() {
        return x0() + 14 + RETRATO + 12;
    }

    private int anchoTexto() {
        return ancho() - (tx() - x0()) - 16;
    }

    private int filaRespuesta(int i) {
        return y0() + 30 + i * 13;
    }

    // ------------------------------------------------------------------ ir de una frase a otra

    private void ir(int n) {
        pararVoz();
        if (n == Dialogos.FIN || pj.nodo(n) == null) {
            cerrar();
            return;
        }
        nodo = pj.nodo(n);
        t = 0;
        escritas = 0;
        if (nodo.menu()) {
            sel = 0;
            for (int i = 0; i < nodo.respuestas().length; i++)
                if (!preguntadas.contains(i)) {
                    sel = i;
                    break;
                }
            return;
        }
        String texto = Component.translatable(pj.linea(nodo.voz())).getString();
        lineas = partir(texto);
        letras = 0;
        for (String l : lineas) letras += l.length();
        float seg = Dialogos.habla(pj.id(), nodo.voz());
        // al ritmo de la voz (un poco por delante, que el ojo lee antes de oir); sin saberlo, ~32 letras por segundo
        ritmo = seg > 0.3f ? letras / Math.max(10f, seg * 20f * 0.92f) : 1.6f;
        voz = new SimpleSoundInstance(new ResourceLocation(DarkCraftMod.MODID, pj.sonido(nodo.voz())), SoundSource.VOICE, 1f, 1f,
                RandomSource.create(), false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true);
        minecraft.getSoundManager().play(voz);
    }

    private List<String> partir(String texto) {
        List<String> r = new ArrayList<>();
        for (FormattedText ft : font.getSplitter().splitLines(texto, Math.max(80, anchoTexto()), Style.EMPTY)) r.add(ft.getString());
        return r;
    }

    private boolean escrita() {
        return nodo != null && !nodo.menu() && escritas >= letras;
    }

    /** [E]: acaba de escribir la frase, pasa a la siguiente o elige la respuesta. */
    private void seguir() {
        if (nodo == null) return;
        if (nodo.menu()) {
            elegir(sel);
            return;
        }
        if (!escrita()) {
            escritas = letras;
            return;
        }
        Panel.mueve();
        if (nodo.accion() != Dialogos.Accion.NADA) Net.CH.sendToServer(new Net.DialogoAccion(entidad, nodo.accion().id()));
        ir(nodo.siguiente());
    }

    private void elegir(int i) {
        if (nodo == null || !nodo.menu() || i < 0 || i >= nodo.respuestas().length || esperando > 0) return;
        preguntadas.add(i);
        Panel.acepta();
        Dialogos.Respuesta r = nodo.respuestas()[i];
        if (r.accion() != Dialogos.Accion.NADA) {
            Net.CH.sendToServer(new Net.DialogoAccion(entidad, r.accion().id()));
            // lo que se responde a quien da una mision (parte 7ag): el servidor lo apunta y la conversacion sigue
            if (r.destino() != Dialogos.FIN) {
                ir(r.destino());
                return;
            }
            // la forja o la tienda: la abre el servidor (esta pantalla se queda hasta entonces)
            esperando = 30;
            return;
        }
        ir(r.destino());
    }

    private void pararVoz() {
        if (voz != null) minecraft.getSoundManager().stop(voz);
        voz = null;
    }

    private void cerrar() {
        pararVoz();
        if (minecraft.screen == this) minecraft.setScreen(null);
    }

    @Override
    public void removed() {
        pararVoz();
        super.removed();
    }

    @Override
    public void tick() {
        abierto++;
        t++;
        if (minecraft.player == null || !minecraft.player.isAlive() || minecraft.player.hurtTime > 0) {
            cerrar();
            return;
        }
        if (nodo != null && !nodo.menu() && escritas < letras) escritas = Math.min(letras, escritas + ritmo);
        if (esperando > 0 && --esperando == 0) cerrar();
    }

    // ------------------------------------------------------------------ dibujar

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (nodo == null) return;
        float entra = Mth.clamp((abierto + pt) / 6f, 0f, 1f);
        // penumbra por abajo
        for (int i = 0; i < 24; i++) {
            int a = Math.round(150 * entra * (i / 24f));
            int y = height - 140 + i * 140 / 24;
            g.fill(0, y, width, y + 140 / 24 + 1, (a << 24) | 0x040306);
        }
        int x = x0(), y = y0() + Math.round((1 - entra) * 12), w = ancho();
        Pix.blanco();
        Panel.losa(g, x, y, w, ALTO);
        // el retrato en su hueco
        int rx = x + 12, ry = y + (ALTO - RETRATO) / 2;
        Panel.hueco(g, rx - 3, ry - 3, RETRATO + 6, RETRATO + 6);
        Pix.mezcla();
        g.blit(new ResourceLocation(DarkCraftMod.MODID, "textures/gui/retratos/" + pj.id() + ".png"), rx, ry, 0, 0, RETRATO, RETRATO, RETRATO, RETRATO);
        // el nombre en gotica y su titulo
        int tx = tx();
        Component nombre = Component.translatable(pj.nombre());
        Letra.mediana(g, font, nombre, tx, y + 8, 0xFFE9C766, 0xFF050408);
        Component titulo = Component.translatable(pj.titulo());
        int tw = font.width(titulo);
        if (tx + Letra.anchoM(font, nombre) + 10 + tw < x + w - 14)
            g.drawString(font, titulo, x + w - 14 - tw, y + 11, 0xFF9A8F7E, false);
        Panel.raya(g, tx - 2, x + w - 12, y + 25);
        Pix.blanco();
        if (nodo.menu()) respuestas(g, mx, my, tx, x + w - 14);
        else frase(g, tx, y);
        Pix.blanco();
        Pix.mezcla();
    }

    private void frase(GuiGraphics g, int tx, int y) {
        int quedan = (int) escritas;
        for (int i = 0; i < lineas.size() && quedan > 0; i++) {
            String l = lineas.get(i);
            String vis = l.substring(0, Math.min(l.length(), quedan));
            quedan -= l.length();
            g.drawString(font, vis, tx, y + 31 + i * 11, 0xFFEADBC4, true);
        }
        // [E] para seguir, latiendo cuando la frase ya esta escrita
        if (escrita()) {
            float lat = 0.6f + 0.4f * Mth.sin((abierto) * 0.25f);
            int a = Math.round(255 * lat);
            Component tecla = minecraft.options.keyInventory.getTranslatedKeyMessage();
            Component que = Component.translatable(nodo.siguiente() == Dialogos.FIN ? "darkcraft.dialogo.terminar" : "darkcraft.dialogo.seguir");
            int ancho = Panel.anchoTecla(font, tecla, que);
            Pix.color(1f, 1f, 1f, lat);
            Panel.tecla(g, font, tecla, que, x0() + ancho() - 14 - ancho, y + ALTO - 17, (a << 24) | 0xB8A888);
            Pix.blanco();
        }
    }

    private void respuestas(GuiGraphics g, int mx, int my, int tx, int fin) {
        Dialogos.Respuesta[] r = nodo.respuestas();
        encima = -1;
        for (int i = 0; i < r.length; i++) {
            int fy = filaRespuesta(i);
            boolean sobre = mx >= tx - 4 && mx < fin && my >= fy - 2 && my < fy + 11;
            if (sobre) encima = i;
            if (i == sel) {
                Pix.blanco();
                Panel.fila(g, tx - 6, fy - 4, fin - tx + 8);
            }
            int color = i == sel ? 0xFFFFF2C4 : preguntadas.contains(i) ? 0xFF8A8072 : sobre ? 0xFFEADBC4 : 0xFFC8B89A;
            g.drawString(font, "◆", tx, fy + 1, i == sel ? 0xFFE2BF62 : 0xFF6E5A3A, false);
            g.drawString(font, Component.translatable(r[i].clave()), tx + 10, fy + 1, color, true);
        }
    }

    // ------------------------------------------------------------------ teclas y raton

    @Override
    public boolean keyPressed(int tecla, int scan, int mods) {
        var o = minecraft.options;
        if (tecla == GLFW.GLFW_KEY_ESCAPE) {
            Panel.cancela();
            cerrar();
            return true;
        }
        if (nodo != null && nodo.menu()) {
            int n = nodo.respuestas().length;
            if (tecla == GLFW.GLFW_KEY_UP || o.keyUp.matches(tecla, scan)) {
                sel = (sel + n - 1) % n;
                Panel.mueve();
                return true;
            }
            if (tecla == GLFW.GLFW_KEY_DOWN || o.keyDown.matches(tecla, scan)) {
                sel = (sel + 1) % n;
                Panel.mueve();
                return true;
            }
        }
        if (tecla == GLFW.GLFW_KEY_ENTER || tecla == GLFW.GLFW_KEY_KP_ENTER || tecla == GLFW.GLFW_KEY_SPACE || o.keyInventory.matches(tecla, scan)) {
            seguir();
            return true;
        }
        return super.keyPressed(tecla, scan, mods);
    }

    @Override
    public void mouseMoved(double mx, double my) {
        if (nodo != null && nodo.menu() && encima >= 0 && encima != sel) {
            sel = encima;
            Panel.mueve();
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        if (b != 0 || nodo == null) return false;
        if (nodo.menu()) {
            if (encima >= 0) elegir(encima);
            return true;
        }
        seguir();
        return true;
    }

    /** La entidad con la que se habla (para la prueba de la CI y para que el herrero sepa que le miran). */
    public int entidad() {
        return entidad;
    }

    /** Para la prueba de la CI. */
    public String resumen() {
        return pj.id() + " nodo " + (nodo == null ? "-" : nodo.id() + (nodo.menu() ? " (menu, elegida " + sel + ", preguntadas " + preguntadas + ")"
                : " voz " + nodo.voz() + " escritas " + (int) escritas + "/" + letras + " ritmo " + String.format(java.util.Locale.ROOT, "%.2f", ritmo)
                + (voz != null && minecraft.getSoundManager().isActive(voz) ? " (sonando)" : "")));
    }

    /** Para la prueba de la CI: el nodo en el que esta (-1 si ninguno) y si ya se ha escrito entera su frase. */
    public int nodoActual() {
        return nodo == null ? -1 : nodo.id();
    }

    public boolean escritaYa() {
        return escrita();
    }

    /** Para la prueba de la CI: pulsar [E]. */
    public void pulsarSeguir() {
        seguir();
    }

    /** Para la prueba de la CI: elegir la respuesta {@code i}. */
    public void pulsarRespuesta(int i) {
        sel = i;
        elegir(i);
    }
}
