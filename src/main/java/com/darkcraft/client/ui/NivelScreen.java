package com.darkcraft.client.ui;

import com.darkcraft.net.Net;
import com.darkcraft.rol.Atributo;
import com.darkcraft.rol.Nivel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Subir de nivel (desde el menu del santuario): a la izquierda los ocho atributos con sus flechas; a la derecha el nivel,
 * los ecos que se tienen, lo que cuesta lo elegido y lo que cambia (vida, mana, estamina y dano de las armas, antes y
 * despues), y debajo para que sirve el atributo elegido. No se gasta nada hasta confirmar; el servidor lo comprueba todo.
 */
public class NivelScreen extends Screen {
    private static final int ANCHO = 396, ALTO = 224, FILA = 18, FILAS_Y = 40, IZQ = 196;
    private static final MenuTex.T[] ICONOS = {MenuTex.AT_VIGOR, MenuTex.AT_MENTE, MenuTex.AT_AGUANTE, MenuTex.AT_FUERZA, MenuTex.AT_DESTREZA,
            MenuTex.AT_INTELIGENCIA, MenuTex.AT_FE, MenuTex.AT_ARCANO};
    private static final int CLARO = 0xFFEADBC4, ORO = 0xFFE2BF62, LUZ = 0xFFFFF2C4, APAGADO = 0xFFB8A888, VERDE = 0xFF9BE08A, ROJO = 0xFFE0564E;

    private final Screen padre;
    private int[] base, nuevo;
    private int version, sel, t;
    /** Acaba de subir de nivel (un destello en el numero) y se espera la respuesta del servidor. */
    private int subioTick = -1000, esperando;

    NivelScreen(Screen padre) {
        super(Component.translatable("darkcraft.menu.santuario.nivel"));
        this.padre = padre;
        leer();
    }

    private void leer() {
        base = ClientRol.atributos.clone();
        nuevo = base.clone();
        version = ClientRol.fichaVersion;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        t++;
        if (minecraft.player == null || !minecraft.player.isAlive() || minecraft.player.hurtTime > 0) {
            minecraft.setScreen(null);
            return;
        }
        if (esperando > 0) esperando--;
        // la ficha ha cambiado en el servidor (se ha subido de nivel): se parte de lo nuevo
        if (version != ClientRol.fichaVersion) {
            boolean subio = Nivel.de(ClientRol.atributos) > Nivel.de(base);
            leer();
            esperando = 0;
            if (subio) subioTick = t;
        }
    }

    // ------------------------------------------------------------------ cuentas

    private int nivelBase() {
        return Nivel.de(base);
    }

    private int nivelNuevo() {
        return Nivel.de(nuevo);
    }

    private long coste() {
        return Nivel.coste(nivelBase(), nivelNuevo());
    }

    private boolean cambiado() {
        return !Arrays.equals(base, nuevo);
    }

    private boolean puedeSubir(int i) {
        return nuevo[i] < Atributo.TOPE && Nivel.coste(nivelBase(), nivelNuevo() + 1) <= ClientRol.ecos;
    }

    private boolean puedeBajar(int i) {
        return nuevo[i] > base[i];
    }

    private void cambiar(int i, int d) {
        if (esperando > 0) return;
        if (d > 0 ? !puedeSubir(i) : !puedeBajar(i)) {
            Panel.nada();
            return;
        }
        nuevo[i] += d;
        Panel.mueve();
    }

    private void confirmar() {
        if (!cambiado() || esperando > 0) {
            Panel.nada();
            return;
        }
        Net.CH.sendToServer(new Net.SubirNivel(nuevo.clone()));
        esperando = 40;
        Panel.acepta();
    }

    private void volver() {
        Panel.cancela();
        minecraft.setScreen(padre);
    }

    // ------------------------------------------------------------------ medidas

    private int px() {
        return (width - ANCHO) / 2;
    }

    private int py() {
        return Math.max(2, (height - ALTO) / 2);
    }

    private int filaY(int i) {
        return py() + FILAS_Y + i * FILA;
    }

    private int filaEn(double mx, double my) {
        if (mx < px() + 10 || mx >= px() + 10 + IZQ) return -1;
        for (int i = 0; i < Atributo.N; i++) if (my >= filaY(i) && my < filaY(i) + FILA) return i;
        return -1;
    }

    private int flechaX(boolean derecha) {
        return px() + 10 + IZQ - (derecha ? 16 : 58);
    }

    private boolean enFlecha(double mx, double my, int i, boolean derecha) {
        int x = flechaX(derecha);
        return mx >= x - 4 && mx < x + 11 && my >= filaY(i) && my < filaY(i) + FILA;
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
        // el titulo, entre dos florituras
        int tw = Letra.anchoG(font, title);
        Letra.grande(g, font, title, x + (ANCHO - tw) / 2, y + 11, 0xFFE9C766, 0xFF050408);
        Pix.blanco();
        if (tw + 2 * 82 < ANCHO - 30) {
            Pix.t(g, HudTex.FLORITURA, x + (ANCHO - tw) / 2 - 10 - 72, y + 14);
            Pix.tEspejo(g, HudTex.FLORITURA, x + (ANCHO + tw) / 2 + 10, y + 14);
        }
        Panel.raya(g, x + 12, x + ANCHO - 12, y + 33);

        // ---- a la izquierda: los atributos
        int encima = filaEn(mx, my);
        for (int i = 0; i < Atributo.N; i++) {
            int fy = filaY(i);
            boolean elegida = i == sel;
            Pix.blanco();
            if (elegida) Panel.fila(g, x + 10, fy, IZQ);
            Panel.t(g, ICONOS[i], x + 16, fy + 1);
            g.drawString(font, Component.translatable("darkcraft.atributo." + Atributo.values()[i].clave), x + 38, fy + 5,
                    elegida ? LUZ : i == encima ? CLARO : 0xFFC8B89A, true);
            boolean sube = nuevo[i] > base[i];
            Pix.blanco();
            Panel.t(g, !puedeBajar(i) ? MenuTex.FLECHA_IZQ_GRIS : enFlecha(mx, my, i, false) ? MenuTex.FLECHA_IZQ_LUZ : MenuTex.FLECHA_IZQ, flechaX(false), fy + 5);
            Panel.t(g, !puedeSubir(i) ? MenuTex.FLECHA_DER_GRIS : enFlecha(mx, my, i, true) ? MenuTex.FLECHA_DER_LUZ : MenuTex.FLECHA_DER, flechaX(true), fy + 5);
            Component v = Component.literal(Integer.toString(nuevo[i]));
            int vw = Letra.anchoM(font, v), centro = (flechaX(false) + 7 + flechaX(true)) / 2;
            Letra.mediana(g, font, v, centro - vw / 2, fy + 3, sube ? LUZ : ORO, 0xFF050408);
        }

        // ---- a la derecha: el nivel, los ecos y lo que cambia
        int dx = x + 10 + IZQ + 10, dw = ANCHO - 14 - (10 + IZQ + 10);
        Pix.blanco();
        Panel.hueco(g, dx, y + FILAS_Y, dw, 49);
        int ly = y + FILAS_Y + 6;
        boolean destello = t - subioTick < 24;
        dato(g, dx, dw, ly, "darkcraft.menu.nivel", Integer.toString(nivelBase()), nivelNuevo() > nivelBase() ? Integer.toString(nivelNuevo()) : null,
                destello ? LUZ : ORO, true);
        dato(g, dx, dw, ly + 13, "darkcraft.menu.ecos", Hud.numero(ClientRol.ecos), null, ORO, false);
        long coste = coste();
        dato(g, dx, dw, ly + 26, "darkcraft.menu.coste", Hud.numero((int) Math.min(Integer.MAX_VALUE, coste)), null, coste > 0 ? LUZ : APAGADO, false);
        if (destello) {
            Pix.suma();
            Pix.color(1f, 0.85f, 0.45f, 0.5f * (1f - (t + pt - subioTick) / 24f));
            g.fill(dx + 2, y + FILAS_Y + 2, dx + dw - 2, y + FILAS_Y + 17, 0xFFFFFFFF);
            Pix.blanco();
            Pix.mezcla();
        }

        Pix.blanco();
        Panel.hueco(g, dx, y + FILAS_Y + 53, dw, 60);
        int sy = y + FILAS_Y + 59;
        int vig = Atributo.VIGOR.ordinal(), men = Atributo.MENTE.ordinal(), agu = Atributo.AGUANTE.ordinal(), fue = Atributo.FUERZA.ordinal(),
                des = Atributo.DESTREZA.ordinal();
        stat(g, dx, dw, sy, "darkcraft.menu.vida", vida(base[vig]), vida(nuevo[vig]));
        stat(g, dx, dw, sy + 13, "darkcraft.menu.mana", Integer.toString(Nivel.mana(base[men])), Integer.toString(Nivel.mana(nuevo[men])));
        stat(g, dx, dw, sy + 26, "darkcraft.menu.estamina", Integer.toString(Math.round(Nivel.estamina(base[agu]))),
                Integer.toString(Math.round(Nivel.estamina(nuevo[agu]))));
        // lo que pega el arma de la mano derecha (con sus letras): antes y despues
        net.minecraft.world.item.ItemStack arma = minecraft.player != null ? minecraft.player.getMainHandItem() : net.minecraft.world.item.ItemStack.EMPTY;
        float b0 = com.darkcraft.combate.Escalado.base(arma);
        stat(g, dx, dw, sy + 39, "darkcraft.menu.ataque", String.format(java.util.Locale.ROOT, "%.1f", com.darkcraft.combate.Escalado.total(arma, b0, base)),
                String.format(java.util.Locale.ROOT, "%.1f", com.darkcraft.combate.Escalado.total(arma, b0, nuevo)));

        // para que sirve el atributo elegido
        List<FormattedCharSequence> lineas = font.split(Component.translatable("darkcraft.atributo." + Atributo.values()[sel].clave + ".desc"), dw - 4);
        for (int i = 0; i < Math.min(3, lineas.size()); i++) g.drawString(font, lineas.get(i), dx + 2, y + FILAS_Y + 119 + i * 9, APAGADO, false);

        // ---- abajo: las teclas y los botones
        boton(g, 0, Component.translatable("darkcraft.menu.confirmar"), cambiado() && esperando <= 0, enBoton(mx, my, 0));
        boton(g, 1, Component.translatable("darkcraft.menu.volver"), true, enBoton(mx, my, 1));
        int ty = botonY() + 4, tx = x + 14;
        tx = Panel.tecla(g, font, Component.literal("←→"), Component.translatable("darkcraft.menu.tecla.cambiar"), tx, ty, APAGADO) + 8;
        if (tx + Panel.anchoTecla(font, minecraft.options.keyInventory.getTranslatedKeyMessage(), Component.translatable("darkcraft.menu.confirmar")) < botonX(0) - 6)
            Panel.tecla(g, font, minecraft.options.keyInventory.getTranslatedKeyMessage(), Component.translatable("darkcraft.menu.confirmar"), tx, ty, APAGADO);
        Pix.blanco();
        Pix.mezcla();
    }

    /** La vida en "puntos de vida" (cada medio corazon de los de antes, 5: mas facil de leer que 20, 22, 24...). */
    private static String vida(int vigor) {
        return Integer.toString(Math.round(Nivel.vida(vigor) * 5f));
    }

    private static String porciento(float f) {
        return String.format(Locale.ROOT, "%d %%", Math.round(f * 100f));
    }

    /** Una linea "Nombre ...... valor" (con "→ nuevo" si cambia). */
    private void dato(GuiGraphics g, int x, int w, int y, String clave, String valor, String despues, int color, boolean gotica) {
        g.drawString(font, Component.translatable(clave), x + 7, y, CLARO, true);
        String s = despues == null ? valor : valor + " → " + despues;
        g.drawString(font, s, x + w - 7 - font.width(s), y, despues != null ? LUZ : color, true);
    }

    private void stat(GuiGraphics g, int x, int w, int y, String clave, String antes, String despues) {
        g.drawString(font, Component.translatable(clave), x + 7, y, CLARO, true);
        if (antes.equals(despues)) {
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

    // ------------------------------------------------------------------ raton y teclado

    @Override
    public void mouseMoved(double mx, double my) {
        int i = filaEn(mx, my);
        if (i >= 0 && i != sel) {
            sel = i;
            Panel.mueve();
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        if (b != 0) return false;
        if (enBoton(mx, my, 0)) {
            confirmar();
            return true;
        }
        if (enBoton(mx, my, 1)) {
            volver();
            return true;
        }
        int i = filaEn(mx, my);
        if (i < 0) return false;
        sel = i;
        if (enFlecha(mx, my, i, false)) cambiar(i, -1);
        else if (enFlecha(mx, my, i, true)) cambiar(i, 1);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double rueda) {
        int i = filaEn(mx, my);
        if (i < 0 || rueda == 0) return false;
        sel = i;
        cambiar(i, rueda > 0 ? 1 : -1);
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
            sel = Math.floorMod(sel - 1, Atributo.N);
            Panel.mueve();
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_DOWN || o.keyDown.matches(tecla, scan)) {
            sel = Math.floorMod(sel + 1, Atributo.N);
            Panel.mueve();
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_LEFT || o.keyLeft.matches(tecla, scan)) {
            cambiar(sel, -1);
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_RIGHT || o.keyRight.matches(tecla, scan)) {
            cambiar(sel, 1);
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_ENTER || tecla == GLFW.GLFW_KEY_KP_ENTER || o.keyInventory.matches(tecla, scan)) {
            confirmar();
            return true;
        }
        return super.keyPressed(tecla, scan, mods);
    }

    /** Para la prueba de la CI. */
    public String resumen() {
        return "nivel " + nivelBase() + " -> " + nivelNuevo() + " coste " + coste() + " de " + ClientRol.ecos + " nuevo=" + Arrays.toString(nuevo)
                + " fila " + Atributo.values()[sel].clave + (esperando > 0 ? " (esperando)" : "");
    }

    /** Para la prueba de la CI: sube n puntos el atributo i como si se pulsara la flecha. */
    public void pulsar(int i, int n) {
        sel = i;
        for (int k = 0; k < Math.abs(n); k++) cambiar(i, n > 0 ? 1 : -1);
    }

    public void pulsarConfirmar() {
        confirmar();
    }
}
