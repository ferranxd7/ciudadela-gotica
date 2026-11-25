package com.darkcraft.client.ui;

import com.darkcraft.dialogo.Dialogos;
import com.darkcraft.forja.Forja;
import com.darkcraft.net.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * La forja de Dorvan: a la izquierda las armas y catalizadores que se llevan (con su mejora), a la derecha la elegida:
 * lo que pega ahora y con la siguiente mejora, las piedras y los ecos que pide (en rojo lo que falta) y los botones.
 * Abajo, lo que va diciendo Dorvan ({@link Bocadillo}): al abrir, la frase 3; al mejorar, la 4; si falta algo, la 5; si
 * ya esta al maximo, la 8. El servidor lo comprueba todo ({@link Forja#mejorar}).
 */
public class ForjaScreen extends Screen {
    private static final int ANCHO = 396, ALTO = 182, FILA = 20, FILAS = 6, IZQ = 186, LISTA_Y = 40;
    private static final int CLARO = 0xFFEADBC4, ORO = 0xFFE2BF62, LUZ = 0xFFFFF2C4, APAGADO = 0xFFB8A888, VERDE = 0xFF9BE08A, ROJO = 0xFFE0564E;
    /** El orden en que salen los huecos: mano derecha, mano izquierda, bolsa y objetos rapidos. */
    private static final int[] ORDEN;

    static {
        List<Integer> o = new ArrayList<>(List.of(0, 1, 2, 40, 3, 4));
        for (int i = 9; i <= 35; i++) o.add(i);
        for (int i = 5; i <= 8; i++) o.add(i);
        ORDEN = o.stream().mapToInt(Integer::intValue).toArray();
    }

    private final int entidad;
    private final Bocadillo dorvan = new Bocadillo(Dialogos.HERRERO);
    private final List<Integer> huecos = new ArrayList<>();
    private int sel, arriba, t, esperando, destello = -100, destelloHueco = -1;
    private boolean saludo;

    private ForjaScreen(int entidad) {
        super(Component.translatable("darkcraft.forja.titulo"));
        this.entidad = entidad;
    }

    public static void abrir(Net.AbrirForja m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.setScreen(new ForjaScreen(m.entidad()));
        Panel.abre();
    }

    public static void hecho(Net.ForjaHecho m) {
        if (Minecraft.getInstance().screen instanceof ForjaScreen f && f.entidad == m.entidad()) f.resultado(m);
    }

    @Override
    protected void init() {
        leer();
        if (!saludo) {
            saludo = true;
            dorvan.decir(3);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        dorvan.parar();
        super.removed();
    }

    /** Lo que se puede mejorar de lo que se lleva (se vuelve a mirar cada tick: el servidor cambia el inventario). */
    private void leer() {
        Integer antes = huecos.isEmpty() ? null : huecos.get(Mth.clamp(sel, 0, huecos.size() - 1));
        huecos.clear();
        Inventory inv = minecraft.player.getInventory();
        for (int h : ORDEN) if (Forja.mejorable(inv.getItem(h))) huecos.add(h);
        if (antes != null && huecos.contains(antes)) sel = huecos.indexOf(antes);
        sel = huecos.isEmpty() ? 0 : Mth.clamp(sel, 0, huecos.size() - 1);
        if (sel < arriba) arriba = sel;
        if (sel >= arriba + FILAS) arriba = sel - FILAS + 1;
        arriba = Mth.clamp(arriba, 0, Math.max(0, huecos.size() - FILAS));
    }

    private ItemStack elegido() {
        return huecos.isEmpty() ? ItemStack.EMPTY : minecraft.player.getInventory().getItem(huecos.get(sel));
    }

    @Override
    public void tick() {
        t++;
        dorvan.tick();
        if (minecraft.player == null || !minecraft.player.isAlive() || minecraft.player.hurtTime > 0) {
            minecraft.setScreen(null);
            return;
        }
        if (esperando > 0) esperando--;
        leer();
    }

    // ------------------------------------------------------------------ mejorar

    private boolean gasta() {
        return !minecraft.player.isCreative();
    }

    private void mejorar() {
        if (esperando > 0 || huecos.isEmpty()) {
            Panel.nada();
            return;
        }
        ItemStack st = elegido();
        int n = Forja.nivel(st);
        if (n >= Forja.MAX) {
            dorvan.decirSiHaceRato(8, 60);
            Panel.nada();
            return;
        }
        int d = n + 1;
        if (gasta() && (minecraft.player.getInventory().countItem(Forja.piedra(Forja.grado(d))) < Forja.piedras(d) || ClientRol.ecos < Forja.ecos(d))) {
            dorvan.decirSiHaceRato(5, 60);
            Panel.nada();
            return;
        }
        Net.CH.sendToServer(new Net.Mejorar(entidad, huecos.get(sel)));
        esperando = 40;
        Panel.acepta();
    }

    private void resultado(Net.ForjaHecho m) {
        esperando = 0;
        switch (m.codigo()) {
            case Forja.HECHO -> {
                destello = t;
                destelloHueco = m.hueco();
                dorvan.decirSiHaceRato(4, 600);
            }
            case Forja.FALTA -> dorvan.decirSiHaceRato(5, 60);
            case Forja.TOPE -> dorvan.decirSiHaceRato(8, 60);
            default -> Panel.nada();
        }
    }

    private void volver() {
        Panel.cancela();
        minecraft.setScreen(null);
    }

    // ------------------------------------------------------------------ medidas

    private int px() {
        return (width - ANCHO) / 2;
    }

    private int py() {
        return Math.max(2, (height - ALTO - Bocadillo.ALTO - 4) / 2);
    }

    private int filaY(int i) {
        return py() + LISTA_Y + (i - arriba) * FILA;
    }

    private int filaEn(double mx, double my) {
        if (mx < px() + 10 || mx >= px() + 10 + IZQ) return -1;
        for (int i = arriba; i < Math.min(huecos.size(), arriba + FILAS); i++) if (my >= filaY(i) && my < filaY(i) + FILA) return i;
        return -1;
    }

    private static final int BOTON_W = 84;

    private int botonX(int i) {
        return px() + ANCHO - 14 - BOTON_W - (1 - i) * (BOTON_W + 6);
    }

    private int botonY() {
        return py() + ALTO - 31;
    }

    private boolean enBoton(double mx, double my, int i) {
        return mx >= botonX(i) && mx < botonX(i) + BOTON_W && my >= botonY() && my < botonY() + 20;
    }

    // ------------------------------------------------------------------ dibujo

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        int x = px(), y = py();
        float e = t + pt;
        g.fill(0, 0, width, height, (Math.round(150 * Mth.clamp(e / 5f, 0f, 1f)) << 24) | 0x050408);
        Pix.blanco();
        Panel.losa(g, x, y, ANCHO, ALTO);
        int tw = Letra.anchoG(font, title);
        Letra.grande(g, font, title, x + (ANCHO - tw) / 2, y + 11, 0xFFE9C766, 0xFF050408);
        Pix.blanco();
        if (tw + 2 * 82 < ANCHO - 30) {
            Pix.t(g, HudTex.FLORITURA, x + (ANCHO - tw) / 2 - 10 - 72, y + 14);
            Pix.tEspejo(g, HudTex.FLORITURA, x + (ANCHO + tw) / 2 + 10, y + 14);
        }
        Panel.raya(g, x + 12, x + ANCHO - 12, y + 33);

        // ---- a la izquierda: lo que se puede mejorar
        Panel.hueco(g, x + 10, y + LISTA_Y - 3, IZQ, FILAS * FILA + 6);
        Inventory inv = minecraft.player.getInventory();
        if (huecos.isEmpty()) {
            List<FormattedCharSequence> l = font.split(Component.translatable("darkcraft.forja.nada"), IZQ - 16);
            for (int i = 0; i < l.size(); i++) g.drawString(font, l.get(i), x + 18, y + LISTA_Y + 8 + i * 10, APAGADO, false);
        }
        int encima = filaEn(mx, my);
        for (int i = arriba; i < Math.min(huecos.size(), arriba + FILAS); i++) {
            int fy = filaY(i);
            ItemStack st = inv.getItem(huecos.get(i));
            Pix.blanco();
            if (i == sel) Panel.fila(g, x + 12, fy + 1, IZQ - 4);
            g.renderItem(st, x + 16, fy + 2);
            int n = Forja.nivel(st);
            String nivel = n >= Forja.MAX ? "+" + n : n > 0 ? "+" + n : "";
            int nw = font.width(nivel);
            String nombre = recortar(st.getHoverName().getString(), IZQ - 44 - nw);
            g.drawString(font, nombre, x + 36, fy + 6, i == sel ? LUZ : i == encima ? CLARO : 0xFFC8B89A, true);
            if (!nivel.isEmpty()) g.drawString(font, nivel, x + 10 + IZQ - 8 - nw, fy + 6, n >= Forja.MAX ? 0xFFFFD866 : ORO, true);
            if (huecos.get(i) == destelloHueco && t - destello < 24) {
                Pix.suma();
                Pix.color(1f, 0.8f, 0.4f, 0.6f * (1f - (e - destello) / 24f));
                g.fill(x + 13, fy + 1, x + 10 + IZQ - 3, fy + FILA - 1, 0xFFFFFFFF);
                Pix.blanco();
                Pix.mezcla();
            }
        }
        // flechitas si hay mas por arriba o por abajo
        if (arriba > 0) g.drawString(font, "▲", x + 10 + IZQ - 12, y + LISTA_Y - 12, APAGADO, false);
        if (arriba + FILAS < huecos.size()) g.drawString(font, "▼", x + 10 + IZQ - 12, y + LISTA_Y + FILAS * FILA + 4, APAGADO, false);

        // ---- a la derecha: la elegida
        int dx = x + 10 + IZQ + 10, dw = ANCHO - 14 - (10 + IZQ + 10);
        Pix.blanco();
        Panel.hueco(g, dx, y + LISTA_Y - 3, dw, FILAS * FILA + 6);
        // los ecos que se llevan, arriba a la derecha del todo
        String ecos = Hud.numero(ClientRol.ecos);
        Pix.mezcla();
        Pix.t(g, HudTex.ECO_0, x + ANCHO - 16 - font.width(ecos) - 18, y + 14);
        g.drawString(font, ecos, x + ANCHO - 16 - font.width(ecos), y + 18, ORO, true);
        if (!huecos.isEmpty()) detalle(g, dx, y + LISTA_Y - 3, dw, e);

        // ---- abajo: botones y teclas
        ItemStack st = elegido();
        boolean puede = !huecos.isEmpty() && Forja.nivel(st) < Forja.MAX && esperando <= 0;
        boton(g, 0, Component.translatable("darkcraft.forja.mejorar"), puede, enBoton(mx, my, 0));
        boton(g, 1, Component.translatable("darkcraft.menu.volver"), true, enBoton(mx, my, 1));
        int ty = botonY() + 4, tx = x + 14;
        tx = Panel.tecla(g, font, Component.literal("↑↓"), Component.translatable("darkcraft.menu.tecla.elegir"), tx, ty, APAGADO) + 8;
        Component k = minecraft.options.keyInventory.getTranslatedKeyMessage();
        if (tx + Panel.anchoTecla(font, k, Component.translatable("darkcraft.forja.mejorar")) < botonX(0) - 6)
            Panel.tecla(g, font, k, Component.translatable("darkcraft.forja.mejorar"), tx, ty, APAGADO);

        // ---- lo que dice Dorvan
        dorvan.render(g, font, x, y + ALTO + 4, ANCHO, pt);
        Pix.blanco();
        Pix.mezcla();
    }

    private void detalle(GuiGraphics g, int x, int y, int w, float e) {
        ItemStack st = elegido();
        Inventory inv = minecraft.player.getInventory();
        int n = Forja.nivel(st);
        // el objeto en grande y su nombre
        g.pose().pushPose();
        g.pose().translate(x + 8, y + 7, 0);
        g.pose().scale(2f, 2f, 1f);
        g.renderItem(st, 0, 0);
        g.pose().popPose();
        Component nombre = Letra.m(st.getHoverName());
        List<FormattedCharSequence> ln = font.split(nombre, w - 54);
        if (!ln.isEmpty()) Letra.contorno(g, font, ln.get(0), x + 46, y + 9, 0xFFE9C766, 0xFF050408);
        String paso = n >= Forja.MAX ? "+" + n : "+" + n + "  →  +" + (n + 1);
        g.drawString(font, paso, x + 46, y + 27, n >= Forja.MAX ? 0xFFFFD866 : LUZ, true);
        Panel.raya(g, x + 6, x + w - 6, y + 42);
        int ly = y + 48;
        boolean catalizador = com.darkcraft.hechizos.Catalizador.de(st) != null;
        if (catalizador) {
            stat(g, x, w, ly, "darkcraft.forja.potencia", String.format(Locale.ROOT, "× %.2f", Forja.mult(n)),
                    n >= Forja.MAX ? null : String.format(Locale.ROOT, "× %.2f", Forja.mult(n + 1)));
        } else {
            float base = com.darkcraft.combate.Escalado.base(st);
            float ahora = com.darkcraft.combate.Escalado.total(st, base, ClientRol.atributos);
            float luego = ahora / Forja.mult(n) * Forja.mult(n + 1);
            stat(g, x, w, ly, "darkcraft.menu.ataque", String.format(Locale.ROOT, "%.1f", ahora),
                    n >= Forja.MAX ? null : String.format(Locale.ROOT, "%.1f", luego));
        }
        if (n >= Forja.MAX) {
            List<FormattedCharSequence> l = font.split(Component.translatable("darkcraft.forja.tope"), w - 14);
            for (int i = 0; i < l.size(); i++) g.drawString(font, l.get(i), x + 7, ly + 20 + i * 10, 0xFFFFD866, true);
            return;
        }
        int d = n + 1;
        Item piedra = Forja.piedra(Forja.grado(d));
        int tiene = inv.countItem(piedra), pide = Forja.piedras(d);
        boolean libre = !gasta();
        // las piedras: el icono, cuantas pide y cuantas se tienen
        g.drawString(font, Component.translatable("darkcraft.forja.piedras"), x + 7, ly + 20, CLARO, true);
        String sp = pide + " / " + tiene;
        int spw = font.width(sp);
        g.drawString(font, sp, x + w - 7 - spw, ly + 20, tiene >= pide || libre ? ORO : ROJO, true);
        g.renderItem(new ItemStack(piedra), x + w - 7 - spw - 19, ly + 16);
        // los ecos
        g.drawString(font, Component.translatable("darkcraft.forja.ecos"), x + 7, ly + 40, CLARO, true);
        String se = Hud.numero(Forja.ecos(d));
        int sew = font.width(se);
        g.drawString(font, se, x + w - 7 - sew, ly + 40, ClientRol.ecos >= Forja.ecos(d) || libre ? ORO : ROJO, true);
        Pix.mezcla();
        Pix.t(g, HudTex.ECO_0, x + w - 7 - sew - 18, ly + 36);
        Pix.blanco();
        // el nombre de la piedra, pequeno
        String np = recortar(new ItemStack(piedra).getHoverName().getString(), w - 14);
        g.drawString(font, np, x + 7, ly + 30, APAGADO, false);
    }

    private void stat(GuiGraphics g, int x, int w, int y, String clave, String antes, String despues) {
        g.drawString(font, Component.translatable(clave), x + 7, y, CLARO, true);
        if (despues == null) {
            g.drawString(font, antes, x + w - 7 - font.width(antes), y, ORO, true);
            return;
        }
        int wd = font.width(despues), wf = font.width(" → "), wa = font.width(antes);
        int rx = x + w - 7 - wd;
        g.drawString(font, despues, rx, y, VERDE, true);
        g.drawString(font, " → ", rx - wf, y, APAGADO, true);
        g.drawString(font, antes, rx - wf - wa, y, ORO, true);
    }

    private void boton(GuiGraphics g, int i, Component texto, boolean activo, boolean encima) {
        int x = botonX(i), y = botonY();
        boolean luz = activo && encima;
        Pix.color(1f, 1f, 1f, activo ? 1f : 0.45f);
        if (luz) Pix.tres(g, HudTex.BOTON_LUZ_IZQ, HudTex.BOTON_LUZ_MED, HudTex.BOTON_LUZ_DER, x, y, BOTON_W);
        else Pix.tres(g, HudTex.BOTON_IZQ, HudTex.BOTON_MED, HudTex.BOTON_DER, x, y, BOTON_W);
        Pix.blanco();
        g.drawString(font, texto, x + (BOTON_W - font.width(texto)) / 2, y + 6, !activo ? 0xFF7A705E : luz ? LUZ : ORO, true);
    }

    private String recortar(String s, int ancho) {
        if (font.width(s) <= ancho) return s;
        return font.plainSubstrByWidth(s, ancho - font.width("…")) + "…";
    }

    // ------------------------------------------------------------------ raton y teclado

    private void elegir(int i) {
        if (huecos.isEmpty()) return;
        int n = Mth.clamp(i, 0, huecos.size() - 1);
        if (n != sel) Panel.mueve();
        sel = n;
        if (sel < arriba) arriba = sel;
        if (sel >= arriba + FILAS) arriba = sel - FILAS + 1;
    }

    @Override
    public void mouseMoved(double mx, double my) {
        int i = filaEn(mx, my);
        if (i >= 0 && i != sel) elegir(i);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        if (b != 0) return false;
        if (enBoton(mx, my, 0)) {
            mejorar();
            return true;
        }
        if (enBoton(mx, my, 1)) {
            volver();
            return true;
        }
        int i = filaEn(mx, my);
        if (i >= 0) elegir(i);
        return i >= 0;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double rueda) {
        if (rueda == 0 || huecos.isEmpty()) return false;
        elegir(sel + (rueda > 0 ? -1 : 1));
        return true;
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int mods) {
        var o = minecraft.options;
        if (tecla == GLFW.GLFW_KEY_ESCAPE) {
            volver();
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_UP || o.keyUp.matches(tecla, scan)) {
            elegir(sel - 1);
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_DOWN || o.keyDown.matches(tecla, scan)) {
            elegir(sel + 1);
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_ENTER || tecla == GLFW.GLFW_KEY_KP_ENTER || o.keyInventory.matches(tecla, scan)) {
            mejorar();
            return true;
        }
        return super.keyPressed(tecla, scan, mods);
    }

    // ------------------------------------------------------------------ para la prueba de la CI

    public String resumen() {
        ItemStack st = elegido();
        return "forja " + huecos.size() + " mejorables, elegida " + (huecos.isEmpty() ? "-" : huecos.get(sel) + " " + st.getItem() + " +" + Forja.nivel(st))
                + " | dorvan dice " + dorvan.numero() + (dorvan.hablando() ? " (sonando)" : "") + (esperando > 0 ? " (esperando)" : "");
    }

    /** Elige el hueco del inventario {@code hueco} (si esta en la lista). */
    public void pulsarHueco(int hueco) {
        int i = huecos.indexOf(hueco);
        if (i >= 0) elegir(i);
    }

    public void pulsarMejorar() {
        mejorar();
    }
}
