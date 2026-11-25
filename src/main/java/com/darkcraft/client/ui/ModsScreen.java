package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Los mods que hay (parte 2f): la lista a la izquierda y la ficha del elegido a la derecha (nombre, version, autores y
 * lo que dice de si mismo), con sus opciones si las tiene ("Configurar") y la carpeta de los mods. Sustituye a la
 * lista de mods de Forge.
 */
public class ModsScreen extends Screen {
    private static final int ORO = 0xFFE2BF62, LUZ = 0xFFFFF2C4, CLARO = 0xFFEADBC4, APAGADO = 0xFFB8A888, GRIS = 0xFF6F6656;
    private static final int FILA = 14, BOTON_H = 20;

    private final Screen volver;
    private final List<IModInfo> mods = new ArrayList<>();
    private int sel, arriba, t;

    public ModsScreen(Screen volver) {
        super(Component.translatable("darkcraft.mods.titulo"));
        this.volver = volver;
    }

    @Override
    protected void init() {
        mods.clear();
        mods.addAll(ModList.get().getMods());
        // Dark Craft el primero; los demas por su nombre
        mods.sort(Comparator.<IModInfo>comparingInt(m -> m.getModId().equals(DarkCraftMod.MODID) ? 0 : 1)
                .thenComparing(m -> m.getDisplayName().toLowerCase(Locale.ROOT)));
        sel = Mth.clamp(sel, 0, Math.max(0, mods.size() - 1));
        ajustar();
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
        return Mth.clamp(ancho() * 36 / 100, 120, 170);
    }

    private int ly() {
        return y0() + 36;
    }

    private int lh() {
        return alto() - 36 - 34;
    }

    private int caben() {
        return Math.max(1, (lh() - 8) / FILA);
    }

    private int px() {
        return lx() + lw() + 8;
    }

    private int pw() {
        return x0() + ancho() - 10 - px();
    }

    private int bw() {
        return (pw() - 24) / 2;
    }

    private int by() {
        return ly() + lh() - BOTON_H - 7;
    }

    private int configX() {
        return px() + 9;
    }

    private int carpetaX() {
        return px() + pw() - 9 - bw();
    }

    private int volverX() {
        return (width - 120) / 2;
    }

    private int volverY() {
        return y0() + alto() - 27;
    }

    private void ajustar() {
        int c = caben();
        if (sel < arriba) arriba = sel;
        if (sel >= arriba + c) arriba = sel - c + 1;
        arriba = Mth.clamp(arriba, 0, Math.max(0, mods.size() - c));
    }

    // ------------------------------------------------------------------ lo de cada mod

    private IModInfo elegido() {
        return mods.isEmpty() ? null : mods.get(sel);
    }

    private static Optional<BiFunction<net.minecraft.client.Minecraft, Screen, Screen>> config(IModInfo m) {
        try {
            return ConfigScreenHandler.getScreenFactoryFor(m);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static String autores(IModInfo m) {
        try {
            return m.getConfig().getConfigElement("authors").map(Object::toString).orElse("");
        } catch (RuntimeException e) {
            return "";
        }
    }

    private static String limpio(String s) {
        String r = ChatFormatting.stripFormatting(s == null ? "" : s);
        return r == null ? "" : r.replace("\r", "").trim();
    }

    // ------------------------------------------------------------------ dibujar

    @Override
    public void tick() {
        t++;
    }

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
        Component cuantos = Component.translatable("darkcraft.mods.cuantos", mods.size());
        g.drawString(font, cuantos, x + w - 14 - font.width(cuantos), y + 15, GRIS, true);
        Panel.raya(g, x + 12, x + w - 12, y + 31);
        // la lista
        Pix.blanco();
        Panel.hueco(g, lx(), ly(), lw(), lh());
        int c = caben(), encima = enLista(mx, my);
        for (int i = 0; i < c && arriba + i < mods.size(); i++) {
            int idx = arriba + i, iy = ly() + 4 + i * FILA;
            IModInfo m = mods.get(idx);
            if (idx == sel) {
                Pix.blanco();
                Panel.fila(g, lx() + 2, iy - 2, lw() - 4);
            } else if (idx == encima) {
                g.fill(lx() + 3, iy - 1, lx() + lw() - 3, iy + FILA - 1, 0x40E2BF62);
            }
            String n = m.getDisplayName();
            while (font.width(n) > lw() - 16 && n.length() > 3) n = n.substring(0, n.length() - 2);
            if (!n.equals(m.getDisplayName())) n = n + "…";
            g.drawString(font, n, lx() + 8, iy + 2, idx == sel ? LUZ : idx == encima ? CLARO : ORO, true);
        }
        if (mods.size() > c) {
            int bx = lx() + lw() - 6, bh = c * FILA, ty = ly() + 4 + (bh - Math.max(8, bh * c / mods.size())) * arriba / Math.max(1, mods.size() - c);
            g.fill(bx, ly() + 4, bx + 2, ly() + 4 + bh, 0xFF0B090F);
            g.fill(bx, ty, bx + 2, ty + Math.max(8, bh * c / mods.size()), 0xFFC0963C);
        }
        // la ficha
        Pix.blanco();
        Panel.hueco(g, px(), ly(), pw(), lh());
        IModInfo m = elegido();
        if (m != null) {
            int fy = ly() + 8;
            Panel.nombre(g, font, Component.literal(m.getDisplayName()), px() + 10, fy + 4, pw() - 20);
            fy += 24;
            g.drawString(font, Component.translatable("darkcraft.mods.version", m.getVersion().toString()), px() + 10, fy, APAGADO, true);
            fy += 11;
            String au = autores(m);
            if (!au.isEmpty()) {
                for (FormattedCharSequence l : font.split(Component.translatable("darkcraft.mods.autores", au), pw() - 20)) {
                    if (fy > by() - 20) break;
                    g.drawString(font, l, px() + 10, fy, APAGADO, true);
                    fy += 10;
                }
            }
            Panel.raya(g, px() + 10, px() + pw() - 10, fy + 2);
            fy += 8;
            String d = limpio(m.getDescription());
            if (d.isEmpty()) d = Component.translatable("darkcraft.mods.sin_descripcion").getString();
            for (FormattedCharSequence l : font.split(Component.literal(d), pw() - 20)) {
                if (fy > by() - 12) break;
                g.drawString(font, l, px() + 10, fy, CLARO, true);
                fy += 10;
            }
            boolean conf = config(m).isPresent();
            boton(g, configX(), by(), bw(), Component.translatable(conf ? "darkcraft.mods.configurar" : "darkcraft.mods.sin_config"),
                    conf, conf && dentro(mx, my, configX(), by(), bw(), BOTON_H));
            boton(g, carpetaX(), by(), bw(), Component.translatable("darkcraft.mods.carpeta"), true,
                    dentro(mx, my, carpetaX(), by(), bw(), BOTON_H));
        }
        boton(g, volverX(), volverY(), 120, Component.translatable("darkcraft.opciones.volver"), true,
                dentro(mx, my, volverX(), volverY(), 120, BOTON_H));
        Pix.blanco();
        Pix.mezcla();
        super.render(g, mx, my, pt);
    }

    private void boton(GuiGraphics g, int x, int y, int w, Component texto, boolean activo, boolean luz) {
        Pix.blanco();
        if (!activo) Pix.color(0.55f, 0.55f, 0.55f, 1f);
        if (luz) Pix.tres(g, HudTex.BOTON_LUZ_IZQ, HudTex.BOTON_LUZ_MED, HudTex.BOTON_LUZ_DER, x, y, w);
        else Pix.tres(g, HudTex.BOTON_IZQ, HudTex.BOTON_MED, HudTex.BOTON_DER, x, y, w);
        Pix.blanco();
        String s = texto.getString();
        while (font.width(s) > w - 10 && s.length() > 3) s = s.substring(0, s.length() - 2);
        g.drawString(font, s, x + (w - font.width(s)) / 2, y + 6, !activo ? GRIS : luz ? LUZ : ORO, true);
    }

    private static boolean dentro(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private int enLista(double mx, double my) {
        if (mx < lx() || mx >= lx() + lw() || my < ly() + 4) return -1;
        int i = (int) ((my - ly() - 4) / FILA);
        if (i < 0 || i >= caben() || arriba + i >= mods.size()) return -1;
        return arriba + i;
    }

    // ------------------------------------------------------------------ manejo

    private void configurar() {
        IModInfo m = elegido();
        if (m == null) return;
        var f = config(m);
        if (f.isEmpty()) {
            Panel.nada();
            return;
        }
        Panel.acepta();
        minecraft.setScreen(f.get().apply(minecraft, this));
    }

    private void carpeta() {
        Panel.acepta();
        Util.getPlatform().openFile(FMLPaths.MODSDIR.get().toFile());
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        if (b == 0) {
            int i = enLista(mx, my);
            if (i >= 0) {
                if (i != sel) Panel.mueve();
                sel = i;
                return true;
            }
            if (dentro(mx, my, configX(), by(), bw(), BOTON_H)) {
                configurar();
                return true;
            }
            if (dentro(mx, my, carpetaX(), by(), bw(), BOTON_H)) {
                carpeta();
                return true;
            }
            if (dentro(mx, my, volverX(), volverY(), 120, BOTON_H)) {
                Panel.cancela();
                onClose();
                return true;
            }
        }
        return super.mouseClicked(mx, my, b);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double d) {
        if (mx >= lx() && mx < lx() + lw()) {
            arriba = Mth.clamp(arriba - (int) Math.signum(d), 0, Math.max(0, mods.size() - caben()));
            return true;
        }
        return super.mouseScrolled(mx, my, d);
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int mods_) {
        if (!mods.isEmpty() && (tecla == GLFW.GLFW_KEY_UP || tecla == GLFW.GLFW_KEY_DOWN)) {
            sel = Math.floorMod(sel + (tecla == GLFW.GLFW_KEY_UP ? -1 : 1), mods.size());
            ajustar();
            Panel.mueve();
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_ENTER || tecla == GLFW.GLFW_KEY_KP_ENTER) {
            configurar();
            return true;
        }
        return super.keyPressed(tecla, scan, mods_);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(volver);
    }

    // ------------------------------------------------------------------ para la prueba de la CI

    public boolean probar(String id) {
        for (int i = 0; i < mods.size(); i++)
            if (mods.get(i).getModId().equals(id)) {
                sel = i;
                ajustar();
                return true;
            }
        return false;
    }

    public String resumen() {
        IModInfo m = elegido();
        return "mods: " + mods.size() + " | elegido " + (m == null ? "-" : m.getModId() + " " + m.getVersion() + " config="
                + config(m).isPresent());
    }
}
