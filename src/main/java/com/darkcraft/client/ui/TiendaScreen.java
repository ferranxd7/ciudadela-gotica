package com.darkcraft.client.ui;

import com.darkcraft.dialogo.Dialogos;
import com.darkcraft.net.Net;
import com.darkcraft.tienda.Tienda;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * La tienda de Kerrick: a la izquierda lo que vende (icono, nombre y precio; lo agotado, apagado), a la derecha lo
 * elegido (para que sirve, cuantas quedan, cuantas se llevan, cuantas comprar y el total) y los botones. Abajo, lo que
 * dice Kerrick ({@link Bocadillo}): al comprar, la frase 3 (no siempre); si no llegan los ecos, la 4. El servidor lo
 * comprueba todo ({@link Tienda#comprar}).
 */
public class TiendaScreen extends Screen {
    private static final int ANCHO = 396, ALTO = 182, FILA = 20, FILAS = 6, IZQ = 196, LISTA_Y = 40;
    private static final int CLARO = 0xFFEADBC4, ORO = 0xFFE2BF62, LUZ = 0xFFFFF2C4, APAGADO = 0xFFB8A888, ROJO = 0xFFE0564E;

    private final int entidad;
    private final int[] comprados;
    private final Bocadillo kerrick = new Bocadillo(Dialogos.MERCADER);
    private int sel, arriba, veces = 1, t, esperando, destello = -100, destelloFila = -1;

    private TiendaScreen(int entidad, int[] comprados) {
        super(Component.translatable("darkcraft.tienda.titulo"));
        this.entidad = entidad;
        this.comprados = new int[Tienda.KERRICK.size()];
        System.arraycopy(comprados, 0, this.comprados, 0, Math.min(comprados.length, this.comprados.length));
    }

    public static void abrir(Net.AbrirTienda m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.setScreen(new TiendaScreen(m.entidad(), m.comprados()));
        Panel.abre();
    }

    public static void hecho(Net.TiendaHecho m) {
        if (Minecraft.getInstance().screen instanceof TiendaScreen s && s.entidad == m.entidad()) s.resultado(m);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        kerrick.parar();
        super.removed();
    }

    @Override
    public void tick() {
        t++;
        kerrick.tick();
        if (minecraft.player == null || !minecraft.player.isAlive() || minecraft.player.hurtTime > 0) {
            minecraft.setScreen(null);
            return;
        }
        if (esperando > 0) esperando--;
        veces = Mth.clamp(veces, 1, maxVeces());
    }

    // ------------------------------------------------------------------ comprar

    private Tienda.Oferta oferta() {
        return Tienda.KERRICK.get(sel);
    }

    private int quedan(int i) {
        return Tienda.quedan(Tienda.KERRICK.get(i), comprados[i]);
    }

    private int maxVeces() {
        return Math.max(1, Math.min(Tienda.TANDA, quedan(sel)));
    }

    private boolean gasta() {
        return !minecraft.player.isCreative();
    }

    private void comprar() {
        if (esperando > 0 || quedan(sel) <= 0) {
            Panel.nada();
            return;
        }
        long total = (long) oferta().precio() * veces;
        if (gasta() && total > ClientRol.ecos) {
            kerrick.decirSiHaceRato(4, 60);
            Panel.nada();
            return;
        }
        Net.CH.sendToServer(new Net.Comprar(entidad, sel, veces));
        esperando = 40;
        Panel.acepta();
    }

    private void resultado(Net.TiendaHecho m) {
        esperando = 0;
        if (m.oferta() >= 0 && m.oferta() < comprados.length) comprados[m.oferta()] = m.comprado();
        switch (m.codigo()) {
            case Tienda.HECHO -> {
                destello = t;
                destelloFila = m.oferta();
                veces = 1;
                kerrick.decirSiHaceRato(3, 500);
            }
            case Tienda.FALTA -> kerrick.decirSiHaceRato(4, 60);
            default -> Panel.nada();
        }
    }

    private void cambiarVeces(int d) {
        int n = Mth.clamp(veces + d, 1, maxVeces());
        if (n == veces) {
            Panel.nada();
            return;
        }
        veces = n;
        Panel.mueve();
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
        for (int i = arriba; i < Math.min(Tienda.KERRICK.size(), arriba + FILAS); i++) if (my >= filaY(i) && my < filaY(i) + FILA) return i;
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

    /** Las flechas de la cantidad (en el detalle): x de la izquierda y de la derecha, y su fila. */
    private int flechaY() {
        return py() + LISTA_Y - 3 + 89;
    }

    private int flechaX(boolean derecha) {
        int dx = px() + 10 + IZQ + 10, dw = ANCHO - 14 - (10 + IZQ + 10);
        return dx + dw - 7 - (derecha ? 7 : 47);
    }

    private boolean enFlecha(double mx, double my, boolean derecha) {
        int x = flechaX(derecha);
        return mx >= x - 3 && mx < x + 10 && my >= flechaY() - 4 && my < flechaY() + 12;
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
        String ecos = Hud.numero(ClientRol.ecos);
        Pix.mezcla();
        Pix.t(g, HudTex.ECO_0, x + ANCHO - 16 - font.width(ecos) - 18, y + 14);
        g.drawString(font, ecos, x + ANCHO - 16 - font.width(ecos), y + 18, ORO, true);

        // ---- a la izquierda: la mercancia
        Pix.blanco();
        Panel.hueco(g, x + 10, y + LISTA_Y - 3, IZQ, FILAS * FILA + 6);
        int encima = filaEn(mx, my);
        List<Tienda.Oferta> of = Tienda.KERRICK;
        for (int i = arriba; i < Math.min(of.size(), arriba + FILAS); i++) {
            int fy = filaY(i);
            Tienda.Oferta o = of.get(i);
            boolean agotada = quedan(i) <= 0;
            Pix.blanco();
            if (i == sel) Panel.fila(g, x + 12, fy + 1, IZQ - 4);
            ItemStack st = o.muestra();
            g.renderItem(st, x + 16, fy + 2);
            g.renderItemDecorations(font, st, x + 16, fy + 2);
            String precio = agotada ? Component.translatable("darkcraft.tienda.agotado").getString() : Hud.numero(o.precio());
            int pw = font.width(precio);
            String nombre = recortar(st.getHoverName().getString(), IZQ - 44 - pw - 6);
            int color = agotada ? 0xFF7A705E : i == sel ? LUZ : i == encima ? CLARO : 0xFFC8B89A;
            g.drawString(font, nombre, x + 36, fy + 6, color, true);
            g.drawString(font, precio, x + 10 + IZQ - 8 - pw, fy + 6, agotada ? 0xFF7A705E : !gasta() || o.precio() <= ClientRol.ecos ? ORO : ROJO, true);
            if (i == destelloFila && t - destello < 24) {
                Pix.suma();
                Pix.color(1f, 0.85f, 0.45f, 0.55f * (1f - (e - destello) / 24f));
                g.fill(x + 13, fy + 1, x + 10 + IZQ - 3, fy + FILA - 1, 0xFFFFFFFF);
                Pix.blanco();
                Pix.mezcla();
            }
        }
        if (arriba > 0) g.drawString(font, "▲", x + 10 + IZQ - 12, y + LISTA_Y - 12, APAGADO, false);
        if (arriba + FILAS < of.size()) g.drawString(font, "▼", x + 10 + IZQ - 12, y + LISTA_Y + FILAS * FILA + 4, APAGADO, false);

        // ---- a la derecha: lo elegido
        int dx = x + 10 + IZQ + 10, dw = ANCHO - 14 - (10 + IZQ + 10);
        Pix.blanco();
        Panel.hueco(g, dx, y + LISTA_Y - 3, dw, FILAS * FILA + 6);
        detalle(g, dx, y + LISTA_Y - 3, dw, mx, my);

        // ---- abajo
        boolean puede = quedan(sel) > 0 && esperando <= 0;
        boton(g, 0, Component.translatable("darkcraft.tienda.comprar"), puede, enBoton(mx, my, 0));
        boton(g, 1, Component.translatable("darkcraft.menu.volver"), true, enBoton(mx, my, 1));
        int ty = botonY() + 4, tx = x + 14;
        tx = Panel.tecla(g, font, Component.literal("↑↓"), Component.translatable("darkcraft.menu.tecla.elegir"), tx, ty, APAGADO) + 8;
        tx = Panel.tecla(g, font, Component.literal("←→"), Component.translatable("darkcraft.tienda.tecla.cantidad"), tx, ty, APAGADO) + 8;
        Component k = minecraft.options.keyInventory.getTranslatedKeyMessage();
        if (tx + Panel.anchoTecla(font, k, Component.translatable("darkcraft.tienda.comprar")) < botonX(0) - 6)
            Panel.tecla(g, font, k, Component.translatable("darkcraft.tienda.comprar"), tx, ty, APAGADO);

        kerrick.render(g, font, x, y + ALTO + 4, ANCHO, pt);
        Pix.blanco();
        Pix.mezcla();
    }

    private void detalle(GuiGraphics g, int x, int y, int w, int mx, int my) {
        Tienda.Oferta o = oferta();
        ItemStack st = o.muestra();
        g.pose().pushPose();
        g.pose().translate(x + 8, y + 7, 0);
        g.pose().scale(2f, 2f, 1f);
        g.renderItem(st, 0, 0);
        g.renderItemDecorations(font, st, 0, 0);
        g.pose().popPose();
        Panel.nombre(g, font, st.getHoverName(), x + 46, y + 9, w - 54);
        boolean dos = font.width(Letra.m(st.getHoverName())) > w - 54 && font.split(st.getHoverName(), w - 54).size() > 1;
        // para que sirve (lo que diria su recuadro, sin el nombre): hasta tres lineas (dos si el nombre ocupa dos)
        List<FormattedCharSequence> desc = new ArrayList<>();
        List<Component> tip = st.getTooltipLines(minecraft.player, TooltipFlag.Default.NORMAL);
        for (int i = 1; i < tip.size() && desc.size() < 3; i++) {
            if (tip.get(i).getString().isBlank()) continue;
            desc.addAll(font.split(tip.get(i), w - 54));
        }
        int dy = dos ? y + 26 : y + 20, caben = dos ? 2 : 3;
        for (int i = 0; i < Math.min(caben, desc.size()); i++) g.drawString(font, desc.get(i), x + 46, dy + i * 9, APAGADO, false);
        Panel.raya(g, x + 6, x + w - 6, y + 48);
        int ly = y + 53;
        // precio, cuantas quedan y cuantas se llevan
        dato(g, x, w, ly, "darkcraft.tienda.precio", Hud.numero(o.precio()), ORO);
        int q = quedan(sel);
        dato(g, x, w, ly + 12, "darkcraft.tienda.quedan", o.limitada() ? Integer.toString(q) : Component.translatable("darkcraft.tienda.sin_limite").getString(),
                q > 0 ? ORO : ROJO);
        dato(g, x, w, ly + 24, "darkcraft.tienda.llevas", Integer.toString(minecraft.player.getInventory().countItem(o.item())), CLARO);
        // la cantidad, con sus flechas
        g.drawString(font, Component.translatable("darkcraft.tienda.cantidad"), x + 7, ly + 37, CLARO, true);
        boolean menos = veces > 1, mas = veces < maxVeces() && q > 0;
        Pix.blanco();
        Panel.t(g, !menos ? MenuTex.FLECHA_IZQ_GRIS : enFlecha(mx, my, false) ? MenuTex.FLECHA_IZQ_LUZ : MenuTex.FLECHA_IZQ, flechaX(false), flechaY());
        Panel.t(g, !mas ? MenuTex.FLECHA_DER_GRIS : enFlecha(mx, my, true) ? MenuTex.FLECHA_DER_LUZ : MenuTex.FLECHA_DER, flechaX(true), flechaY());
        String v = Integer.toString(veces);
        int centro = (flechaX(false) + 7 + flechaX(true)) / 2;
        g.drawString(font, v, centro - font.width(v) / 2, ly + 37, LUZ, true);
        // el total (en rojo si no llegan los ecos)
        long total = (long) o.precio() * veces;
        String tt = Hud.numero((int) Math.min(Integer.MAX_VALUE, total));
        int ttw = font.width(tt);
        g.drawString(font, Component.translatable("darkcraft.tienda.total"), x + 7, ly + 50, CLARO, true);
        g.drawString(font, tt, x + w - 7 - ttw, ly + 50, !gasta() || total <= ClientRol.ecos ? LUZ : ROJO, true);
        Pix.mezcla();
        Pix.t(g, HudTex.ECO_0, x + w - 7 - ttw - 18, ly + 46);
        Pix.blanco();
    }

    private void dato(GuiGraphics g, int x, int w, int y, String clave, String valor, int color) {
        g.drawString(font, Component.translatable(clave), x + 7, y, CLARO, true);
        g.drawString(font, valor, x + w - 7 - font.width(valor), y, color, true);
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
        return font.plainSubstrByWidth(s, Math.max(0, ancho - font.width("…"))) + "…";
    }

    // ------------------------------------------------------------------ raton y teclado

    private void elegir(int i) {
        int n = Mth.clamp(i, 0, Tienda.KERRICK.size() - 1);
        if (n != sel) {
            Panel.mueve();
            veces = 1;
        }
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
            comprar();
            return true;
        }
        if (enBoton(mx, my, 1)) {
            volver();
            return true;
        }
        if (enFlecha(mx, my, false)) {
            cambiarVeces(-1);
            return true;
        }
        if (enFlecha(mx, my, true)) {
            cambiarVeces(1);
            return true;
        }
        int i = filaEn(mx, my);
        if (i >= 0) elegir(i);
        return i >= 0;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double rueda) {
        if (rueda == 0) return false;
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
        if (tecla == GLFW.GLFW_KEY_LEFT || o.keyLeft.matches(tecla, scan)) {
            cambiarVeces(-1);
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_RIGHT || o.keyRight.matches(tecla, scan)) {
            cambiarVeces(1);
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_ENTER || tecla == GLFW.GLFW_KEY_KP_ENTER || o.keyInventory.matches(tecla, scan)) {
            comprar();
            return true;
        }
        return super.keyPressed(tecla, scan, mods);
    }

    // ------------------------------------------------------------------ para la prueba de la CI

    public String resumen() {
        Tienda.Oferta o = oferta();
        return "tienda elegida " + sel + " " + o.id() + " x" + veces + " (quedan " + (o.limitada() ? quedan(sel) : "sin limite") + ", comprados "
                + java.util.Arrays.toString(comprados) + ") | kerrick dice " + kerrick.numero() + (kerrick.hablando() ? " (sonando)" : "")
                + (esperando > 0 ? " (esperando)" : "");
    }

    /** Elige la oferta {@code i} y cuantas veces comprarla. */
    public void pulsar(int i, int n) {
        elegir(i);
        veces = Mth.clamp(n, 1, maxVeces());
    }

    public void pulsarComprar() {
        comprar();
    }
}
