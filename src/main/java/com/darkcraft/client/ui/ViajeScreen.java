package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.SantuariosMorvain;
import com.darkcraft.net.Net;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Viajar entre los santuarios de Morvain (parte 7c), desde el menu del santuario: a la izquierda la losa con los que se
 * conocen (el de aqui, marcado) y a la derecha el mapa de Morvain en pergamino ({@code textures/gui/mapa_morvain.png},
 * de {@code tools/morvain/Pergamino.java}) con cada uno marcado y el elegido latiendo con su nombre. Teclado (arriba y
 * abajo, la tecla del aviso o INTRO para viajar, ESC para volver) y raton (clic en la lista o en el mapa; la rueda baja la
 * lista).
 */
public class ViajeScreen extends Screen {
    public static final ResourceLocation MAPA = new ResourceLocation(DarkCraftMod.MODID, "textures/gui/mapa_morvain.png");
    private static final int TEX = 1024;
    private static final int FILA = 15, CABECERA = 38, PIE = 34, ANCHO = 196;

    private final Screen antes;
    private final List<SantuariosMorvain.Sitio> sitios = new ArrayList<>();
    private final String aqui;
    private int sel, t, desde;

    public ViajeScreen(Screen antes, List<String> destinos, String aqui) {
        super(Component.translatable("darkcraft.viaje.titulo"));
        this.antes = antes;
        this.aqui = aqui;
        for (String id : destinos) {
            SantuariosMorvain.Sitio s = SantuariosMorvain.porId(id);
            if (s != null) sitios.add(s);
        }
        for (int i = 0; i < sitios.size(); i++)
            if (!sitios.get(i).id().equals(aqui)) {
                sel = i;
                break;
            }
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

    // ------------------------------------------------------------------ medidas

    private int px() {
        return Mth.clamp(width / 24, 6, 28);
    }

    private int visibles() {
        return Math.max(3, Math.min(sitios.size(), (height - 24 - CABECERA - PIE) / FILA));
    }

    private int alto() {
        return CABECERA + visibles() * FILA + PIE;
    }

    private int py() {
        return Math.max(4, (height - alto()) / 2 - 8);
    }

    private int filaY(int i) {
        return py() + CABECERA + (i - desde) * FILA;
    }

    /** El cuadrado del mapa: {x, y, lado}. */
    private int[] mapa() {
        int x0 = px() + ANCHO + 14, libre = width - x0 - 10;
        int lado = Math.max(60, Math.min(height - 20, libre));
        return new int[]{x0 + Math.max(0, (libre - lado) / 2), (height - lado) / 2, lado};
    }

    private float[] enMapa(SantuariosMorvain.Sitio s, int[] m) {
        float k = m[2] / 16000f;
        return new float[]{m[0] + (s.x() + 8000) * k, m[1] + (s.z() + 8000) * k};
    }

    private int sobre(double mx, double my) {
        if (mx < px() + 6 || mx >= px() + ANCHO - 6) return -1;
        for (int i = desde; i < Math.min(sitios.size(), desde + visibles()); i++)
            if (my >= filaY(i) && my < filaY(i) + FILA) return i;
        return -1;
    }

    private int sobreMapa(double mx, double my) {
        int[] m = mapa();
        int mejor = -1;
        double d = 8 * 8;
        for (int i = 0; i < sitios.size(); i++) {
            float[] p = enMapa(sitios.get(i), m);
            double dd = (p[0] - mx) * (p[0] - mx) + (p[1] - my) * (p[1] - my);
            if (dd < d) {
                d = dd;
                mejor = i;
            }
        }
        return mejor;
    }

    // ------------------------------------------------------------------ dibujo

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        float abre = Mth.clamp((t + pt) / 6f, 0f, 1f);
        g.fill(0, 0, width, height, (Math.round(190 * abre) << 24) | 0x050408);
        int x = px(), y = py(), h = alto();
        // la losa con la lista
        Pix.blanco();
        Panel.losa(g, x, y, ANCHO, h);
        Panel.t(g, MenuTex.SANTUARIO, x + 12, y + 10);
        Letra.mediana(g, font, title, x + 34, y + 11, 0xFFE9C766, 0xFF050408);
        Panel.raya(g, x + 10, x + ANCHO - 10, y + 31);
        int encima = sobre(mx, my);
        for (int i = desde; i < Math.min(sitios.size(), desde + visibles()); i++) {
            SantuariosMorvain.Sitio s = sitios.get(i);
            int fy = filaY(i);
            boolean elegida = i == sel, esAqui = s.id().equals(aqui);
            if (elegida) {
                Pix.blanco();
                Panel.fila(g, x + 8, fy - 1, ANCHO - 16);
            }
            String nombre = font.plainSubstrByWidth(Component.translatable(s.nombre()).getString(), ANCHO - (esAqui ? 64 : 34));
            int color = esAqui ? 0xFF8FC6CC : elegida ? 0xFFFFF2C4 : i == encima ? 0xFFEADBC4 : 0xFFC8B89A;
            g.drawString(font, nombre, x + 16, fy + 3, color, true);
            if (esAqui) {
                Component a = Component.translatable("darkcraft.viaje.aqui");
                g.drawString(font, a, x + ANCHO - 16 - font.width(a), fy + 3, 0xFF6FA6AC, true);
            }
        }
        // flechitas si hay mas por arriba o por abajo
        if (desde > 0) g.drawString(font, "▲", x + ANCHO / 2 - 3, y + CABECERA - 9, 0xFFC0963C, false);
        if (desde + visibles() < sitios.size()) g.drawString(font, "▼", x + ANCHO / 2 - 3, y + CABECERA + visibles() * FILA - 2, 0xFFC0963C, false);
        // pie: la region del elegido y lo lejos que esta
        Panel.raya(g, x + 10, x + ANCHO - 10, y + h - PIE + 2);
        if (sel >= 0 && sel < sitios.size()) {
            SantuariosMorvain.Sitio s = sitios.get(sel);
            g.drawString(font, Component.translatable("darkcraft.zona.morvain." + s.region().id), x + 14, y + h - PIE + 8, 0xFFE2BF62, true);
            SantuariosMorvain.Sitio a = SantuariosMorvain.porId(aqui);
            if (a != null && a != s) {
                int d = (int) Math.round(Math.hypot(s.x() - a.x(), s.z() - a.z()));
                g.drawString(font, Component.translatable("darkcraft.viaje.lejos", Hud.numero(d)), x + 14, y + h - PIE + 19, 0xFFB8A888, false);
            }
        }
        // el mapa
        int[] m = mapa();
        g.fill(m[0] - 3, m[1] - 3, m[0] + m[2] + 3, m[1] + m[2] + 3, 0xFF050408);
        Pix.blanco();
        Pix.mezcla();
        g.blit(MAPA, m[0], m[1], m[2], m[2], 0f, 0f, TEX, TEX, TEX, TEX);
        float late = 0.5f + 0.5f * Mth.sin((t + pt) * 0.25f);
        for (int i = 0; i < sitios.size(); i++) {
            SantuariosMorvain.Sitio s = sitios.get(i);
            float[] p = enMapa(s, m);
            int cx = Math.round(p[0]), cy = Math.round(p[1]);
            boolean elegida = i == sel, esAqui = s.id().equals(aqui);
            if (elegida) {
                int r = 5 + Math.round(late * 2);
                int c = (Math.round(150 + 105 * late) << 24) | 0xF6E3A0;
                g.fill(cx - r, cy - r, cx + r + 1, cy - r + 1, c);
                g.fill(cx - r, cy + r, cx + r + 1, cy + r + 1, c);
                g.fill(cx - r, cy - r, cx - r + 1, cy + r + 1, c);
                g.fill(cx + r, cy - r, cx + r + 1, cy + r + 1, c);
            }
            // un fuego de almas pequeno (rombo), rojo el de aqui
            int fuera = 0xFF050408, dentro = esAqui ? 0xFFE8302A : 0xFF8FE6F0;
            g.fill(cx - 1, cy - 3, cx + 2, cy + 4, fuera);
            g.fill(cx - 3, cy - 1, cx + 4, cy + 2, fuera);
            g.fill(cx - 2, cy - 2, cx + 3, cy + 3, fuera);
            g.fill(cx, cy - 2, cx + 1, cy + 3, dentro);
            g.fill(cx - 2, cy, cx + 3, cy + 1, dentro);
            g.fill(cx - 1, cy - 1, cx + 2, cy + 2, dentro);
        }
        // el nombre del elegido junto a su marca
        if (sel >= 0 && sel < sitios.size()) {
            float[] p = enMapa(sitios.get(sel), m);
            Component n = Component.translatable(sitios.get(sel).nombre());
            int w = font.width(n), lx = Mth.clamp(Math.round(p[0]) - w / 2, m[0] + 2, m[0] + m[2] - w - 2);
            int ly = Math.round(p[1]) - 18 < m[1] + 2 ? Math.round(p[1]) + 10 : Math.round(p[1]) - 18;
            g.fill(lx - 3, ly - 2, lx + w + 3, ly + 10, 0xC8050408);
            g.drawString(font, n, lx, ly, 0xFFF6E3A0, true);
        }
        // las teclas, debajo de la losa
        int ty = Math.min(height - 16, y + h + 6), tx = x + 4;
        tx = Panel.tecla(g, font, Component.literal("↑↓"), Component.translatable("darkcraft.menu.tecla.mover"), tx, ty, 0xFFB8A888) + 10;
        tx = Panel.tecla(g, font, minecraft.options.keyInventory.getTranslatedKeyMessage(), Component.translatable("darkcraft.viaje.tecla"), tx, ty,
                0xFFB8A888) + 10;
        Panel.tecla(g, font, Component.literal("Esc"), Component.translatable("darkcraft.viaje.volver"), tx, ty, 0xFFB8A888);
        Pix.blanco();
        Pix.mezcla();
    }

    // ------------------------------------------------------------------ lo que se hace

    private void viajar() {
        if (sel < 0 || sel >= sitios.size() || sitios.get(sel).id().equals(aqui)) {
            Panel.nada();
            return;
        }
        Panel.acepta();
        Net.CH.sendToServer(new Net.Viajar(sitios.get(sel).id()));
        minecraft.setScreen(null);
    }

    private void elegir(int i) {
        if (i < 0 || i >= sitios.size() || i == sel) return;
        sel = i;
        if (sel < desde) desde = sel;
        if (sel >= desde + visibles()) desde = sel - visibles() + 1;
        Panel.mueve();
    }

    @Override
    public void onClose() {
        Panel.cancela();
        minecraft.setScreen(antes);
    }

    @Override
    public void mouseMoved(double mx, double my) {
        int i = sobre(mx, my);
        if (i >= 0) elegir(i);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int boton) {
        if (boton != 0) return false;
        int i = sobre(mx, my);
        if (i < 0) i = sobreMapa(mx, my);
        if (i < 0) return false;
        if (i == sel) viajar();
        else elegir(i);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double d) {
        int n = Mth.clamp(desde - (int) Math.signum(d), 0, Math.max(0, sitios.size() - visibles()));
        if (n != desde) {
            desde = n;
            if (sel < desde) sel = desde;
            if (sel >= desde + visibles()) sel = desde + visibles() - 1;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int mods) {
        var o = minecraft.options;
        if (tecla == GLFW.GLFW_KEY_UP || o.keyUp.matches(tecla, scan)) {
            elegir(Math.floorMod(sel - 1, Math.max(1, sitios.size())));
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_DOWN || o.keyDown.matches(tecla, scan)) {
            elegir(Math.floorMod(sel + 1, Math.max(1, sitios.size())));
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_ENTER || tecla == GLFW.GLFW_KEY_KP_ENTER || tecla == GLFW.GLFW_KEY_SPACE || o.keyInventory.matches(tecla, scan)) {
            viajar();
            return true;
        }
        return super.keyPressed(tecla, scan, mods);
    }

    /** Para la prueba de la CI: viaja al de este id (si esta en la lista). */
    public void elegirYViajar(String id) {
        for (int i = 0; i < sitios.size(); i++)
            if (sitios.get(i).id().equals(id)) {
                sel = i;
                viajar();
                return;
            }
    }

    /** Para la prueba de la CI. */
    public String resumen() {
        StringBuilder b = new StringBuilder("viaje: " + sitios.size() + " santuarios (aqui " + aqui + "), elegido ");
        b.append(sel >= 0 && sel < sitios.size() ? sitios.get(sel).id() : "-");
        return b.toString();
    }
}
