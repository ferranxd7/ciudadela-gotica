package com.darkcraft.client.ui;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.lwjgl.glfw.GLFW;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Las partidas guardadas (parte 2f-2): cada mundo como una partida, con su dibujo, su nombre, el nivel y la zona donde
 * se quedo, y cuando se jugo por ultima vez; jugar, nueva partida, borrar (con pregunta) y volver. Sustituye a la lista
 * de mundos de Minecraft.
 */
public class PartidasScreen extends Screen {
    private static final int ORO = 0xFFE2BF62, LUZ = 0xFFFFF2C4, CLARO = 0xFFEADBC4, APAGADO = 0xFFB8A888, GRIS = 0xFF6F6656,
            ROJO = 0xFFDE3A3A;
    private static final int FILA = 38, BOTON_H = 20;

    private final Screen volver;
    private List<Guardadas.Una> partidas;
    private final Map<String, FaviconTexture> iconos = new HashMap<>();
    private int sel, arriba, t;
    private boolean cargando;

    public PartidasScreen(Screen volver) {
        super(Component.translatable("darkcraft.partidas.titulo"));
        this.volver = volver;
    }

    @Override
    protected void init() {
        if (partidas == null && !cargando) recargar();
    }

    private void recargar() {
        cargando = true;
        Guardadas.cargar(minecraft).thenAcceptAsync(l -> {
            partidas = l;
            cargando = false;
            sel = Mth.clamp(sel, 0, Math.max(0, l.size() - 1));
            ajustar();
        }, minecraft);
    }

    @Override
    public void tick() {
        t++;
    }

    // ------------------------------------------------------------------ medidas

    private int ancho() {
        return Math.min(width - 12, 470);
    }

    private int alto() {
        return Math.min(height - 12, 300);
    }

    private int x0() {
        return (width - ancho()) / 2;
    }

    private int y0() {
        return (height - alto()) / 2;
    }

    private int lx() {
        return x0() + 10;
    }

    private int lw() {
        return ancho() - 20;
    }

    private int ly() {
        return y0() + 36;
    }

    private int lh() {
        return alto() - 36 - 34;
    }

    private int caben() {
        return Math.max(1, (lh() - 6) / FILA);
    }

    private int bw() {
        return Math.min(104, (ancho() - 20 - 3 * 6) / 4);
    }

    private int bx(int i) {
        int total = 4 * bw() + 3 * 6;
        return (width - total) / 2 + i * (bw() + 6);
    }

    private int by() {
        return y0() + alto() - 27;
    }

    private void ajustar() {
        int c = caben();
        if (sel < arriba) arriba = sel;
        if (sel >= arriba + c) arriba = sel - c + 1;
        arriba = Mth.clamp(arriba, 0, Math.max(0, (partidas == null ? 0 : partidas.size()) - c));
    }

    private Guardadas.Una elegida() {
        return partidas == null || partidas.isEmpty() ? null : partidas.get(Mth.clamp(sel, 0, partidas.size() - 1));
    }

    // ------------------------------------------------------------------ dibujo

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int x = x0(), y = y0(), w = ancho(), h = alto();
        Pix.blanco();
        Panel.losa(g, x, y, w, h);
        Letra.grande(g, font, title, (width - Letra.anchoG(font, title)) / 2, y + 9, 0xFFE9C766, 0xFF050408);
        Panel.raya(g, x + 12, x + w - 12, y + 31);
        Pix.blanco();
        Panel.hueco(g, lx(), ly(), lw(), lh());
        if (partidas == null) {
            Component c = Component.translatable("darkcraft.partidas.cargando");
            g.drawString(font, c, (width - font.width(c)) / 2, ly() + lh() / 2 - 4, APAGADO, true);
        } else if (partidas.isEmpty()) {
            Component c = Component.translatable("darkcraft.partidas.ninguna");
            g.drawString(font, c, (width - font.width(c)) / 2, ly() + lh() / 2 - 4, APAGADO, true);
        } else {
            int encima = enLista(mx, my);
            for (int i = 0; i < caben() && arriba + i < partidas.size(); i++) {
                int idx = arriba + i;
                fila(g, partidas.get(idx), lx() + 3, ly() + 3 + i * FILA, lw() - 6, idx == sel, idx == encima);
            }
            if (partidas.size() > caben()) {
                int bx = lx() + lw() - 5, bh = caben() * FILA, n = partidas.size(), c = caben();
                int th = Math.max(10, bh * c / n), ty = ly() + 3 + (bh - th) * arriba / Math.max(1, n - c);
                g.fill(bx, ly() + 3, bx + 2, ly() + 3 + bh, 0xFF0B090F);
                g.fill(bx, ty, bx + 2, ty + th, 0xFFC0963C);
            }
        }
        Guardadas.Una e = elegida();
        boolean jugable = e != null && e.jugable();
        boton(g, 0, Component.translatable("darkcraft.partidas.jugar"), jugable, mx, my);
        boton(g, 1, Component.translatable("darkcraft.partidas.nueva"), true, mx, my);
        boton(g, 2, Component.translatable("darkcraft.partidas.borrar"), e != null, mx, my);
        boton(g, 3, Component.translatable("darkcraft.opciones.volver"), true, mx, my);
        Pix.blanco();
        Pix.mezcla();
        super.render(g, mx, my, pt);
    }

    private void fila(GuiGraphics g, Guardadas.Una u, int x, int y, int w, boolean elegida, boolean encima) {
        if (elegida) {
            Pix.blanco();
            Panel.fila(g, x, y + 10, w);
        } else if (encima) {
            g.fill(x + 1, y + 1, x + w - 1, y + FILA - 1, 0x30E2BF62);
        }
        // el dibujo del mundo en su hueco
        Pix.blanco();
        Panel.hueco(g, x + 2, y + 1, 36, 36);
        FaviconTexture ic = icono(u);
        Pix.mezcla();
        if (ic != null) g.blit(ic.textureLocation(), x + 4, y + 3, 0, 0, 32, 32, 32, 32);
        else Letra.grande(g, font, Component.literal(u.nombre().substring(0, 1).toUpperCase()), x + 13, y + 10, 0xFF8A6A30, 0xFF050408);
        // nombre, nivel y zona, y cuando
        int tx = x + 46, tw = w - 52;
        Component nombre = Component.literal(u.nombre());
        if (Letra.anchoM(font, nombre) <= tw) Letra.mediana(g, font, nombre, tx, y + 3, elegida ? LUZ : ORO, 0xFF050408);
        else g.drawString(font, recortar(u.nombre(), tw), tx, y + 4, elegida ? LUZ : ORO, true);
        String linea = u.linea().getString();
        if (u.horas() > 0) linea += " · " + Component.translatable("darkcraft.partidas.horas", String.format("%.1f", u.horas())).getString();
        g.drawString(font, recortar(linea, tw), tx, y + 18, elegida ? CLARO : APAGADO, true);
        String cuando = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(u.mundo().getLastPlayed()));
        if (u.mundo().isHardcore()) cuando += " · " + Component.translatable("darkcraft.partidas.extremo").getString();
        if (!u.jugable()) cuando += " · " + Component.translatable("darkcraft.partidas.no_se_abre").getString();
        g.drawString(font, recortar(cuando, tw), tx, y + 28, u.jugable() ? GRIS : ROJO, true);
    }

    private String recortar(String s, int ancho) {
        if (font.width(s) <= ancho) return s;
        return font.plainSubstrByWidth(s, Math.max(0, ancho - font.width("…"))) + "…";
    }

    /** El dibujo del mundo (icon.png, de 64 x 64), cargado una vez. */
    private FaviconTexture icono(Guardadas.Una u) {
        if (iconos.containsKey(u.id())) return iconos.get(u.id());
        FaviconTexture f = null;
        Path p = Guardadas.icono(u);
        if (p != null) {
            try (InputStream in = Files.newInputStream(p)) {
                NativeImage img = NativeImage.read(in);
                f = FaviconTexture.forWorld(minecraft.getTextureManager(), u.id());
                f.upload(img);
            } catch (Exception ex) {
                if (f != null) f.close();
                f = null;
            }
        }
        iconos.put(u.id(), f);
        return f;
    }

    private void boton(GuiGraphics g, int i, Component texto, boolean activo, int mx, int my) {
        int x = bx(i), y = by(), w = bw();
        boolean luz = activo && mx >= x && mx < x + w && my >= y && my < y + BOTON_H;
        Pix.blanco();
        if (!activo) Pix.color(0.55f, 0.55f, 0.55f, 1f);
        if (luz) Pix.tres(g, HudTex.BOTON_LUZ_IZQ, HudTex.BOTON_LUZ_MED, HudTex.BOTON_LUZ_DER, x, y, w);
        else Pix.tres(g, HudTex.BOTON_IZQ, HudTex.BOTON_MED, HudTex.BOTON_DER, x, y, w);
        Pix.blanco();
        String s = recortar(texto.getString(), w - 8);
        g.drawString(font, s, x + (w - font.width(s)) / 2, y + 6, !activo ? GRIS : luz ? LUZ : ORO, true);
    }

    // ------------------------------------------------------------------ manejo

    private int enLista(double mx, double my) {
        if (partidas == null || mx < lx() || mx >= lx() + lw() || my < ly() + 3) return -1;
        int i = (int) ((my - ly() - 3) / FILA);
        if (i < 0 || i >= caben() || arriba + i >= partidas.size()) return -1;
        return arriba + i;
    }

    private int enBoton(double mx, double my) {
        for (int i = 0; i < 4; i++) if (mx >= bx(i) && mx < bx(i) + bw() && my >= by() && my < by() + BOTON_H) return i;
        return -1;
    }

    private void jugar() {
        Guardadas.Una u = elegida();
        if (u == null || !u.jugable()) {
            Panel.nada();
            return;
        }
        Panel.acepta();
        minecraft.createWorldOpenFlows().loadLevel(this, u.id());
    }

    private void borrar() {
        Guardadas.Una u = elegida();
        if (u == null) return;
        Panel.acepta();
        minecraft.setScreen(new ConfirmScreen(si -> {
            if (si) {
                LevelStorageSource src = minecraft.getLevelSource();
                try (LevelStorageSource.LevelStorageAccess a = src.createAccess(u.id())) {
                    a.deleteLevel();
                } catch (Exception ex) {
                    com.darkcraft.DarkCraftMod.LOGGER.warn("No se pudo borrar la partida {}: {}", u.id(), ex.toString());
                }
                partidas = null;
                FaviconTexture f = iconos.remove(u.id());
                if (f != null) f.close();
            }
            minecraft.setScreen(this);
        }, Component.translatable("darkcraft.partidas.borrar_pregunta"),
                Component.translatable("darkcraft.partidas.borrar_aviso", u.nombre()),
                Component.translatable("darkcraft.partidas.borrar"), Component.translatable("gui.cancel")));
    }

    private void accion(int b) {
        switch (b) {
            case 0 -> jugar();
            case 1 -> {
                Panel.acepta();
                CreateWorldScreen.openFresh(minecraft, this);
            }
            case 2 -> borrar();
            case 3 -> {
                Panel.cancela();
                onClose();
            }
            default -> {
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        if (b == 0) {
            int i = enLista(mx, my);
            if (i >= 0) {
                if (i == sel && t - ultimoClic < 8) jugar();
                else {
                    sel = i;
                    Panel.mueve();
                }
                ultimoClic = t;
                return true;
            }
            int k = enBoton(mx, my);
            if (k >= 0) {
                accion(k);
                return true;
            }
        }
        return super.mouseClicked(mx, my, b);
    }

    private int ultimoClic = -100;

    @Override
    public boolean mouseScrolled(double mx, double my, double d) {
        if (partidas != null && mx >= lx() && mx < lx() + lw()) {
            arriba = Mth.clamp(arriba - (int) Math.signum(d), 0, Math.max(0, partidas.size() - caben()));
            return true;
        }
        return super.mouseScrolled(mx, my, d);
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int mods) {
        if (partidas != null && !partidas.isEmpty() && (tecla == GLFW.GLFW_KEY_UP || tecla == GLFW.GLFW_KEY_DOWN)) {
            sel = Math.floorMod(sel + (tecla == GLFW.GLFW_KEY_UP ? -1 : 1), partidas.size());
            ajustar();
            Panel.mueve();
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_ENTER || tecla == GLFW.GLFW_KEY_KP_ENTER) {
            jugar();
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_DELETE) {
            borrar();
            return true;
        }
        return super.keyPressed(tecla, scan, mods);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(volver);
    }

    @Override
    public void removed() {
        for (FaviconTexture f : iconos.values()) if (f != null) f.close();
        iconos.clear();
    }

    // ------------------------------------------------------------------ para la prueba de la CI

    public String resumen() {
        if (partidas == null) return "partidas: cargando";
        StringBuilder b = new StringBuilder("partidas: " + partidas.size());
        for (Guardadas.Una u : partidas)
            b.append(" | ").append(u.id()).append(" nivel ").append(u.nivel()).append(" zona '").append(u.zona()).append("' ecos ")
                    .append(u.ecos()).append(" horas ").append(u.horas());
        return b.toString();
    }
}
