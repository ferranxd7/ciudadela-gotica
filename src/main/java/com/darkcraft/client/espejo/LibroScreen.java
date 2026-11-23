package com.darkcraft.client.espejo;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.espejo.Espejo;
import com.darkcraft.net.Net;
import com.darkcraft.story.Story;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * El libro de la Sombra (tecla M): un libro viejo de cuero negro con costuras rojas y tres cintas, Historia (la
 * cronica de Valdrenn, los libros perdidos y el diario), Mapa (lo explorado en cada dimension, con niebla donde no se
 * ha estado, los lugares, cofres, jefes, amigos y las marcas propias, que se escriben con la pluma) y Logros (los
 * logros del mod, la coleccion de objetos y el bestiario).
 */
public final class LibroScreen extends Screen {
    static final ResourceLocation LIBRO = EspejoHud.rl("textures/gui/libro.png"), PESTANAS = EspejoHud.rl("textures/gui/libro_pestanas.png"),
            VINIETA = EspejoHud.rl("textures/gui/vinieta.png"), PERGAMINO = EspejoHud.rl("textures/gui/pergamino.png");
    static final int LW = 512, LH = 320;
    /** Lo que dura la escena de escribir una marca (ms). */
    static final long DURA = 2600;
    static final int TINTA = 0xFF3A2414, ROJO = 0xFF8E1418, GRIS = 0xFF8C7C66, ORO = 0xFF7A4E10, CLARO = 0xFFB8A27E;

    /** La seccion abierta (se recuerda entre aperturas). */
    private static int seccion = 0, subLogros = 0;
    private static int hSel = -1;

    private int bx, by, bw, bh;
    private float k;
    private final long abierto = net.minecraft.Util.getMillis();
    private long version = -1;

    public LibroScreen() {
        super(Component.translatable("darkcraft.sombra.titulo"));
    }

    public static void abrir() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new LibroScreen());
        mc.getSoundManager().play(SimpleSoundInstance.forUI(DarkCraftMod.SFX_LIBRO_ABRE.get(), 1.0f, 0.8f));
        EspejoCliente.accion(0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        int maxW = Math.min(width - 70, 560), maxH = height - 22;
        bw = maxW;
        bh = Math.round(bw * LH / (float) LW);
        if (bh > maxH) {
            bh = maxH;
            bw = Math.round(bh * LW / (float) LH);
        }
        k = bw / (float) LW;
        bx = (width - bw - 46) / 2;
        by = (height - bh) / 2;
        if (marca != null) marca.colocar();
        if (vista == null) vista = new Vista();
    }

    int X(float tx) {
        return bx + Math.round(tx * k);
    }

    int Y(float ty) {
        return by + Math.round(ty * k);
    }

    @Override
    public void removed() {
        if (vista != null) vista.soltar();
        super.removed();
    }

    // ------------------------------------------------------------------ teclas y raton

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (marca != null && marca.nombre != null && marca.nombre.isFocused()) {
            if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER || key == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {
                marca.escribir();
                return true;
            }
            if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                marca.cerrar();
                return true;
            }
            return marca.nombre.keyPressed(key, scan, mods) || true;
        }
        if (minecraft != null && (EspejoCliente.TECLA_LIBRO.matches(key, scan) || minecraft.options.keyInventory.matches(key, scan))) {
            onClose();
            return true;
        }
        if (seccion == 1 && marca == null) {
            if (EspejoCliente.TECLA_ACERCAR.matches(key, scan)) {
                vista.zoom(1, vista.cx(), vista.cy());
                return true;
            }
            if (EspejoCliente.TECLA_ALEJAR.matches(key, scan)) {
                vista.zoom(-1, vista.cx(), vista.cy());
                return true;
            }
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        if (marca != null && marca.nombre != null && marca.nombre.isFocused()) return marca.nombre.charTyped(c, mods);
        return super.charTyped(c, mods);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        return clic(mx, my, b) || super.mouseClicked(mx, my, b);
    }

    private boolean clic(double mx, double my, int b) {
        // cintas
        for (int i = 0; i < 3; i++) {
            int[] r = cinta(i);
            if (mx >= r[0] && mx < r[2] && my >= r[1] && my < r[3]) {
                if (seccion != i) {
                    seccion = i;
                    if (marca != null) marca.cerrar();
                    pasarPagina();
                }
                return true;
            }
        }
        if (marca != null) return marca.clic(mx, my, b);
        return switch (seccion) {
            case 0 -> clicHistoria(mx, my);
            case 1 -> vista.clic(mx, my, b);
            default -> clicLogros(mx, my);
        };
    }

    @Override
    public boolean mouseReleased(double mx, double my, int b) {
        if (seccion == 1 && vista != null) vista.soltarRaton();
        return super.mouseReleased(mx, my, b);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int b, double dx, double dy) {
        if (seccion == 1 && marca == null && vista.arrastrar(mx, my, dx, dy)) return true;
        return super.mouseDragged(mx, my, b, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double d) {
        if (marca != null) return true;
        if (seccion == 1) return vista.zoom(d > 0 ? 1 : -1, mx, my);
        if (seccion == 2) {
            scroll = Math.max(0, scroll - (int) Math.signum(d));
            return true;
        }
        if (seccion == 0) {
            if (d < 0) hPag++;
            else hPag = Math.max(0, hPag - 1);
            return true;
        }
        return false;
    }

    void pasarPagina() {
        if (minecraft != null) minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0f));
    }

    // ------------------------------------------------------------------ dibujo general

    @Override
    public void renderBackground(GuiGraphics g) {
        float t = (net.minecraft.Util.getMillis() - abierto) / 1000f;
        g.fill(0, 0, width, height, 0xB8070306);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(0.45f, 0.02f, 0.04f, 0.85f);
        g.blit(VINIETA, 0, 0, width, height, 0, 0, 256, 256, 256, 256);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        // humo de la Sombra alrededor del libro
        for (int i = 0; i < 26; i++) {
            float fase = (i * 0.618f) % 1f;
            float vida = (t / 6f + fase) % 1f;
            double ang = i * Math.PI * 2 / 26 + Math.sin(t * 0.2 + i) * 0.2;
            float rx = bw * 0.55f + vida * 30, ry = bh * 0.58f + vida * 24;
            float x = bx + bw / 2f + (float) Math.cos(ang) * rx, y = by + bh / 2f + (float) Math.sin(ang) * ry;
            float lado = 60 + 70 * vida;
            boolean rojo = i % 3 == 0;
            RenderSystem.setShaderColor(rojo ? 0.4f : 0.02f, rojo ? 0.02f : 0.01f, rojo ? 0.04f : 0.02f, (float) Math.sin(Math.PI * vida) * 0.5f);
            g.blit(EspejoHud.HUMO, Math.round(x - lado / 2), Math.round(y - lado / 2), Math.round(lado), Math.round(lado), 0, 0, 64, 64, 64, 64);
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!EspejoCliente.activo()) {
            onClose();
            return;
        }
        if (version != EspejoCliente.version()) {
            version = EspejoCliente.version();
            cacheLogros = null;
        }
        renderBackground(g);
        float p = Mth.clamp((net.minecraft.Util.getMillis() - abierto) / 220f, 0, 1);
        float e = 0.94f + 0.06f * (1 - (1 - p) * (1 - p));
        g.pose().pushPose();
        g.pose().translate(bx + bw / 2f, by + bh / 2f, 0);
        g.pose().scale(e, e, 1);
        g.pose().translate(-(bx + bw / 2f), -(by + bh / 2f), 0);
        RenderSystem.enableBlend();
        if (minecraft != null) minecraft.getTextureManager().getTexture(LIBRO).setFilter(true, false);
        g.blit(LIBRO, bx, by, bw, bh, 0, 0, LW, LH, LW, LH);
        cintas(g, mx, my);
        if (marca != null) marca.render(g, mx, my, pt);
        else switch (seccion) {
            case 0 -> historia(g, mx, my);
            case 1 -> vista.render(g, mx, my, pt);
            default -> logros(g, mx, my, pt);
        }
        g.pose().popPose();
        super.render(g, mx, my, pt);
        if (tooltip != null) {
            g.renderTooltip(font, tooltip, mx, my);
            tooltip = null;
        }
    }

    private List<FormattedCharSequence> tooltip;

    void tooltip(List<Component> l) {
        List<FormattedCharSequence> out = new ArrayList<>();
        for (Component c : l) out.addAll(font.split(c, 200));
        tooltip = out;
    }

    /** Rectangulo de la cinta i (en px de la gui). */
    int[] cinta(int i) {
        int x0 = bx + bw - Math.round(8 * k), y0 = by + Math.round((38 + i * 34) * k);
        int w = Math.round(76 * k) + 26, h = Math.max(16, Math.round(24 * k));
        return new int[]{x0, y0, x0 + w, y0 + h};
    }

    private void cintas(GuiGraphics g, int mx, int my) {
        String[] claves = {"darkcraft.sombra.historia", "darkcraft.sombra.mapa", "darkcraft.sombra.logros"};
        int[] iconos = {EspejoHud.I_PERGAMINO, EspejoHud.I_MAPA, EspejoHud.I_TROFEO};
        for (int i = 0; i < 3; i++) {
            int[] r = cinta(i);
            boolean sel = seccion == i, sobre = mx >= r[0] && mx < r[2] && my >= r[1] && my < r[3];
            int extra = sel ? 8 : sobre ? 4 : 0;
            g.blit(PESTANAS, r[0], r[1], r[2] - r[0] + extra, r[3] - r[1], 0, sel ? 22 : 0, 72, 22, 72, 44);
            EspejoHud.icono(g, iconos[i], r[0] + 12 + extra / 2f, (r[1] + r[3]) / 2f, 12);
            g.drawString(font, Component.translatable(claves[i]), r[0] + 21 + extra / 2, (r[1] + r[3]) / 2 - 4, sel ? 0xFFFFF0D8 : 0xFFE0C8A8, true);
        }
    }

    void centro(GuiGraphics g, Component c, int cx, int y, int col) {
        g.drawString(font, c, cx - font.width(c) / 2, y, col, false);
    }

    /** Escribe texto en un ancho, linea a linea; devuelve la y de debajo. */
    int parrafo(GuiGraphics g, Component c, int x, int y, int w, int col) {
        for (FormattedCharSequence l : font.split(c, w)) {
            g.drawString(font, l, x, y, col, false);
            y += 10;
        }
        return y;
    }

    // ------------------------------------------------------------------ Historia

    private int hPag;
    private List<FormattedCharSequence> hLineas;
    private int hLineasDe = Integer.MIN_VALUE;

    private boolean leido(int ch) {
        return (EspejoCliente.estado.leidos() & (1 << ch)) != 0;
    }

    private void historia(GuiGraphics g, int mx, int my) {
        int lx = X(34), ly = Y(26), lw = X(240) - lx;
        centro(g, Component.translatable("darkcraft.sombra.historia").withStyle(ChatFormatting.BOLD), (X(34) + X(240)) / 2, ly, ROJO);
        int y = ly + 16;
        g.drawString(font, Component.translatable("darkcraft.sombra.cronica"), lx, y, ORO, false);
        y += 12;
        for (int ch = 1; ch <= Story.CHAPTERS; ch++) {
            boolean ok = leido(ch);
            Component t = ok ? Component.literal(romano(ch) + ". ").append(Component.translatable("darkcraft.cap." + ch + ".titulo"))
                    : Component.literal(romano(ch) + ". ").append(Component.translatable("darkcraft.sombra.sin_leer"));
            boolean sobre = ok && mx >= lx && mx < lx + lw && my >= y - 1 && my < y + 10;
            int col = !ok ? GRIS : (hSel == ch ? ROJO : sobre ? 0xFF6A2A14 : TINTA);
            if (!ok) EspejoHud.icono(g, EspejoHud.I_CANDADO, lx + 4, y + 3.5f, 8);
            g.drawString(font, recortar(t, lw - 12), lx + 10, y, col, false);
            y += 12;
        }
        y += 6;
        g.drawString(font, Component.translatable("darkcraft.sombra.libros"), lx, y, ORO, false);
        y += 12;
        y = parrafo(g, Component.translatable("darkcraft.sombra.libros.ninguno").withStyle(ChatFormatting.ITALIC), lx + 10, y, lw - 10, GRIS);
        y += 8;
        boolean sobreD = mx >= lx && mx < lx + lw && my >= y - 1 && my < y + 10;
        EspejoHud.icono(g, EspejoHud.I_PLUMA, lx + 4, y + 3.5f, 9);
        g.drawString(font, Component.translatable("darkcraft.sombra.diario"), lx + 10, y, hSel == 10 ? ROJO : sobreD ? 0xFF6A2A14 : ORO, false);
        filasHistoria[0] = y;
        // pagina derecha
        int rx = X(272), ry = Y(26), rw = X(478) - rx;
        int rcx = (X(272) + X(478)) / 2;
        int lineasPag = Math.max(4, (Y(278) - (ry + 18)) / 10);
        List<FormattedCharSequence> lineas = lineasHistoria(rw);
        if (hSel == -1) {
            centro(g, Component.translatable("darkcraft.sombra.titulo").withStyle(ChatFormatting.BOLD), rcx, ry, ROJO);
            int yy = parrafo(g, Component.translatable("darkcraft.sombra.intro"), rx, ry + 20, rw, TINTA);
            EspejoHud.icono(g, EspejoHud.I_SELLO, rcx, Math.min(Y(270), yy + 30), 28);
            return;
        }
        Component titulo = hSel == 10 ? Component.translatable("darkcraft.sombra.diario")
                : Component.translatable("darkcraft.capitulo", romano(hSel));
        centro(g, titulo.copy().withStyle(ChatFormatting.BOLD), rcx, ry, ROJO);
        int paginas = Math.max(1, (lineas.size() + lineasPag - 1) / lineasPag);
        hPag = Mth.clamp(hPag, 0, paginas - 1);
        int yy = ry + 18;
        for (int i = hPag * lineasPag; i < Math.min(lineas.size(), (hPag + 1) * lineasPag); i++) {
            g.drawString(font, lineas.get(i), rx, yy, TINTA, false);
            yy += 10;
        }
        // flechas de pagina
        int fy = Y(284);
        Component pag = Component.translatable("darkcraft.sombra.pagina", hPag + 1, paginas);
        centro(g, pag, rcx, fy, GRIS);
        boolean ant = hPag > 0, sig = hPag < paginas - 1;
        flecha(g, rx + 4, fy, "◀", ant, mx, my);
        flecha(g, X(478) - 10, fy, "▶", sig, mx, my);
        flechas[0] = rx + 4;
        flechas[1] = X(478) - 10;
        flechas[2] = fy;
    }

    private final int[] filasHistoria = new int[1];
    private final int[] flechas = new int[3];

    private void flecha(GuiGraphics g, int x, int y, String s, boolean activa, int mx, int my) {
        boolean sobre = activa && mx >= x - 3 && mx < x + 10 && my >= y - 3 && my < y + 11;
        g.drawString(font, s, x, y, !activa ? 0x40000000 | (CLARO & 0xffffff) : sobre ? ROJO : TINTA, false);
    }

    private List<FormattedCharSequence> lineasHistoria(int w) {
        int clave = hSel * 1000 + w + (int) (EspejoCliente.version() * 7);
        if (hLineas != null && hLineasDe == clave) return hLineas;
        List<FormattedCharSequence> out = new ArrayList<>();
        if (hSel >= 1 && hSel <= Story.CHAPTERS) {
            out.addAll(font.split(Component.translatable("darkcraft.cap." + hSel + ".titulo").withStyle(ChatFormatting.ITALIC), w));
            out.add(FormattedCharSequence.EMPTY);
            for (int p = 1; p <= 20; p++) {
                String key = "darkcraft.cap." + hSel + ".p" + p;
                if (!I18n.exists(key)) break;
                out.addAll(font.split(Component.translatable(key), w));
                out.add(FormattedCharSequence.EMPTY);
            }
        } else if (hSel == 10) {
            List<Net.DiarioE> d = EspejoCliente.estado.diario();
            if (d.isEmpty()) out.addAll(font.split(Component.translatable("darkcraft.sombra.diario.vacio").withStyle(ChatFormatting.ITALIC), w));
            for (Net.DiarioE e : d) {
                out.addAll(font.split(Component.translatable("darkcraft.sombra.dia", e.dia()).withStyle(s -> s.withColor(ROJO & 0xffffff).withBold(true)), w));
                out.addAll(font.split(Component.translatable("darkcraft.diario." + e.clave()), w));
                out.add(FormattedCharSequence.EMPTY);
            }
        }
        hLineas = out;
        hLineasDe = clave;
        return out;
    }

    private boolean clicHistoria(double mx, double my) {
        int lx = X(34), lw = X(240) - lx;
        int y = Y(26) + 16 + 12;
        for (int ch = 1; ch <= Story.CHAPTERS; ch++) {
            if (leido(ch) && mx >= lx && mx < lx + lw && my >= y - 1 && my < y + 10) {
                hSel = ch;
                hPag = 0;
                pasarPagina();
                return true;
            }
            y += 12;
        }
        int yd = filasHistoria[0];
        if (mx >= lx && mx < lx + lw && my >= yd - 1 && my < yd + 10) {
            hSel = 10;
            hPag = 0;
            pasarPagina();
            return true;
        }
        if (hSel != -1 && Math.abs(my - flechas[2] - 3) < 8) {
            if (Math.abs(mx - flechas[0] - 3) < 8 && hPag > 0) {
                hPag--;
                pasarPagina();
                return true;
            }
            if (Math.abs(mx - flechas[1] - 3) < 8) {
                hPag++;
                pasarPagina();
                return true;
            }
        }
        return false;
    }

    static String romano(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            default -> Integer.toString(n);
        };
    }

    Component recortar(Component c, int w) {
        if (font.width(c) <= w) return c;
        String s = c.getString();
        while (s.length() > 1 && font.width(s + "...") > w) s = s.substring(0, s.length() - 1);
        return Component.literal(s + "...").withStyle(c.getStyle());
    }

    // ------------------------------------------------------------------ Logros, coleccion y bestiario

    private int scroll, lSel = 0;
    private List<Object[]> cacheLogros;

    static final String[] COLECCION = {"mapa_valdrenn", "libro1", "libro2", "espada_diestra", "espada_siniestra", "libro3", "libro4",
            "llave_arbol", "mandoble_juramento", "llave_hueso", "libro5"};
    static final String[] BESTIARIO = {"escudero", "rey", "gavran"};

    private static ItemStack objeto(String clave) {
        if (clave.startsWith("libro")) {
            try {
                return Story.book(Integer.parseInt(clave.substring(5)));
            } catch (NumberFormatException e) {
                return new ItemStack(Items.WRITTEN_BOOK);
            }
        }
        Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation(DarkCraftMod.MODID, clave));
        return i == null ? new ItemStack(Items.BARRIER) : new ItemStack(i);
    }

    private static ItemStack icono(String id) {
        Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        return i == null ? new ItemStack(Items.BOOK) : new ItemStack(i);
    }

    private void logros(GuiGraphics g, int mx, int my, float pt) {
        Net.EspejoEstado e = EspejoCliente.estado;
        int lx = X(34), ly = Y(24), lw = X(240) - lx;
        // pestanas de arriba
        String[] subs = {"darkcraft.sombra.logros", "darkcraft.sombra.coleccion", "darkcraft.sombra.bestiario"};
        int hechos = 0;
        for (Net.LogroE l : e.logros()) if (l.hecho()) hechos++;
        int col = 0;
        for (String c : COLECCION) if (e.coleccion().contains(c)) col++;
        int bes = 0;
        for (String b : BESTIARIO) if (e.bestiario().contains(b)) bes++;
        String[] cuenta = {hechos + "/" + e.logros().size(), col + "/" + COLECCION.length, bes + "/" + BESTIARIO.length};
        int x = lx;
        int[] anchos = new int[3];
        for (int i = 0; i < 3; i++) {
            Component t = Component.translatable(subs[i]);
            int w = font.width(t);
            boolean sel = subLogros == i, sobre = mx >= x && mx < x + w && my >= ly - 1 && my < ly + 18;
            g.drawString(font, t, x, ly, sel ? ROJO : sobre ? 0xFF6A2A14 : TINTA, false);
            g.pose().pushPose();
            g.pose().translate(x + w / 2f, ly + 10, 0);
            g.pose().scale(0.75f, 0.75f, 1);
            g.drawString(font, cuenta[i], -font.width(cuenta[i]) / 2, 0, GRIS, false);
            g.pose().popPose();
            if (sel) g.fill(x, ly + 17, x + w, ly + 18, ROJO);
            subX[i] = x;
            anchos[i] = w;
            x += w + 10;
        }
        subW = anchos;
        int top = ly + 24, bottom = Y(296);
        g.fill(lx, top - 3, lx + lw, top - 2, 0x40000000 | (TINTA & 0xffffff));
        switch (subLogros) {
            case 0 -> listaLogros(g, e, lx, top, lw, bottom, mx, my);
            case 1 -> rejillaColeccion(g, e, lx, top, lw, bottom, mx, my);
            default -> listaBestiario(g, e, lx, top, lw, bottom, mx, my);
        }
        detalle(g, e, mx, my, pt);
    }

    private final int[] subX = new int[3];
    private int[] subW = new int[3];

    private void listaLogros(GuiGraphics g, Net.EspejoEstado e, int lx, int top, int lw, int bottom, int mx, int my) {
        int fila = 20, caben = Math.max(1, (bottom - top) / fila);
        scroll = Mth.clamp(scroll, 0, Math.max(0, e.logros().size() - caben));
        int y = top;
        for (int i = scroll; i < Math.min(e.logros().size(), scroll + caben); i++) {
            Net.LogroE l = e.logros().get(i);
            boolean sobre = mx >= lx && mx < lx + lw && my >= y && my < y + fila;
            if (i == lSel) g.fill(lx - 2, y, lx + lw, y + fila - 2, 0x30000000 | (ROJO & 0xffffff));
            else if (sobre) g.fill(lx - 2, y, lx + lw, y + fila - 2, 0x18000000);
            ItemStack ic = icono(l.icono());
            if (!l.hecho()) RenderSystem.setShaderColor(0.25f, 0.22f, 0.2f, 0.55f);
            g.renderItem(ic, lx, y + 1);
            g.flush();
            RenderSystem.setShaderColor(1, 1, 1, 1);
            Component t = l.secreto() && !l.hecho() ? Component.translatable("darkcraft.sombra.secreto") : Component.translatable(l.titulo());
            g.drawString(font, recortar(t, lw - 40), lx + 20, y + 5, l.hecho() ? TINTA : GRIS, false);
            EspejoHud.icono(g, l.hecho() ? EspejoHud.I_SELLO : EspejoHud.I_SELLO_GRIS, lx + lw - 8, y + 9, l.hecho() ? 14 : 11);
            y += fila;
        }
        barra(g, lx + lw + 3, top, bottom, scroll, caben, e.logros().size());
    }

    private void barra(GuiGraphics g, int x, int top, int bottom, int desde, int caben, int total) {
        if (total <= caben) return;
        int h = bottom - top;
        int bh2 = Math.max(10, h * caben / total);
        int y = top + (h - bh2) * desde / Math.max(1, total - caben);
        g.fill(x, top, x + 2, bottom, 0x30000000);
        g.fill(x, y, x + 2, y + bh2, ROJO);
    }

    private void rejillaColeccion(GuiGraphics g, Net.EspejoEstado e, int lx, int top, int lw, int bottom, int mx, int my) {
        int cel = 24, cols = Math.max(1, lw / cel);
        int x0 = lx + (lw - cols * cel) / 2;
        for (int i = 0; i < COLECCION.length; i++) {
            int x = x0 + (i % cols) * cel, y = top + (i / cols) * cel;
            boolean tiene = e.coleccion().contains(COLECCION[i]);
            boolean sobre = mx >= x && mx < x + cel - 2 && my >= y && my < y + cel - 2;
            g.fill(x, y, x + cel - 2, y + cel - 2, i == lSel ? 0x50000000 | (ROJO & 0xffffff) : sobre ? 0x30000000 : 0x1c000000);
            g.fill(x, y + cel - 3, x + cel - 2, y + cel - 2, 0x40000000 | (TINTA & 0xffffff));
            if (!tiene) RenderSystem.setShaderColor(0, 0, 0, 0.7f);
            g.renderItem(objeto(COLECCION[i]), x + (cel - 2 - 16) / 2, y + (cel - 2 - 16) / 2);
            g.flush();
            RenderSystem.setShaderColor(1, 1, 1, 1);
            if (sobre) tooltip(List.of(tiene ? objeto(COLECCION[i]).getHoverName() : Component.literal("???")));
        }
    }

    private void listaBestiario(GuiGraphics g, Net.EspejoEstado e, int lx, int top, int lw, int bottom, int mx, int my) {
        int fila = 22, y = top;
        for (int i = 0; i < BESTIARIO.length; i++) {
            boolean visto = e.bestiario().contains(BESTIARIO[i]);
            boolean sobre = mx >= lx && mx < lx + lw && my >= y && my < y + fila;
            if (i == lSel) g.fill(lx - 2, y, lx + lw, y + fila - 2, 0x30000000 | (ROJO & 0xffffff));
            else if (sobre) g.fill(lx - 2, y, lx + lw, y + fila - 2, 0x18000000);
            EspejoHud.icono(g, EspejoHud.I_BESTIA, lx + 8, y + 9, 14);
            Component n = visto ? Component.translatable(nombreBestia(BESTIARIO[i])) : Component.literal("???");
            g.drawString(font, recortar(n, lw - 44), lx + 20, y + 5, visto ? TINTA : GRIS, false);
            if (visto) EspejoHud.icono(g, EspejoHud.I_SELLO, lx + lw - 8, y + 9, 13);
            y += fila;
        }
    }

    static String nombreBestia(String c) {
        return switch (c) {
            case "rey" -> "entity.darkcraft.rey";
            case "gavran" -> "entity.darkcraft.gavran";
            default -> "darkcraft.gavran.escudero";
        };
    }

    private final Map<String, LivingEntity> criaturas = new HashMap<>();

    private LivingEntity criatura(String clave) {
        if (minecraft == null || minecraft.level == null) return null;
        return criaturas.computeIfAbsent(clave, c -> {
            Level lvl = minecraft.level;
            LivingEntity le = switch (c) {
                case "rey" -> DarkCraftMod.REY.get().create(lvl);
                case "gavran" -> DarkCraftMod.GAVRAN.get().create(lvl);
                default -> {
                    var s = EntityType.SKELETON.create(lvl);
                    if (s != null) {
                        s.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                        s.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
                        ItemStack casco = new ItemStack(Items.LEATHER_HELMET);
                        ((DyeableLeatherItem) Items.LEATHER_HELMET).setColor(casco, 0x2E4A2C);
                        s.setItemSlot(EquipmentSlot.HEAD, casco);
                    }
                    yield s;
                }
            };
            return le;
        });
    }

    /** La pagina derecha: lo elegido en la izquierda, en grande. */
    private void detalle(GuiGraphics g, Net.EspejoEstado e, int mx, int my, float pt) {
        int rx = X(272), ry = Y(26), rw = X(478) - rx, rcx = (X(272) + X(478)) / 2;
        if (subLogros == 0) {
            if (e.logros().isEmpty()) return;
            lSel = Mth.clamp(lSel, 0, e.logros().size() - 1);
            Net.LogroE l = e.logros().get(lSel);
            boolean oculto = l.secreto() && !l.hecho();
            g.pose().pushPose();
            g.pose().translate(rcx - 24, ry + 6, 0);
            g.pose().scale(3, 3, 1);
            if (!l.hecho()) RenderSystem.setShaderColor(0.25f, 0.22f, 0.2f, 0.6f);
            g.renderItem(icono(l.icono()), 0, 0);
            g.flush();
            RenderSystem.setShaderColor(1, 1, 1, 1);
            g.pose().popPose();
            int y = ry + 62;
            Component t = oculto ? Component.translatable("darkcraft.sombra.secreto") : Component.translatable(l.titulo());
            for (FormattedCharSequence s : font.split(t.copy().withStyle(ChatFormatting.BOLD), rw)) {
                g.drawString(font, s, rcx - font.width(s) / 2, y, l.hecho() ? ROJO : GRIS, false);
                y += 10;
            }
            centro(g, Component.translatable("darkcraft.sombra.marco." + Mth.clamp(l.marco(), 0, 2)), rcx, y + 2, ORO);
            y += 18;
            Component d = l.hecho() ? Component.translatable(l.desc()) : Component.literal("???");
            for (FormattedCharSequence s : font.split(d, rw - 10)) {
                g.drawString(font, s, rcx - font.width(s) / 2, y, TINTA, false);
                y += 10;
            }
            EspejoHud.icono(g, l.hecho() ? EspejoHud.I_SELLO : EspejoHud.I_SELLO_GRIS, rcx, Y(262), l.hecho() ? 30 : 24);
            centro(g, Component.translatable(l.hecho() ? "darkcraft.sombra.conseguido" : "darkcraft.sombra.pendiente"), rcx, Y(282),
                    l.hecho() ? ROJO : GRIS);
        } else if (subLogros == 1) {
            lSel = Mth.clamp(lSel, 0, COLECCION.length - 1);
            String c = COLECCION[lSel];
            boolean tiene = e.coleccion().contains(c);
            ItemStack st = objeto(c);
            g.pose().pushPose();
            g.pose().translate(rcx - 32, ry + 8, 0);
            g.pose().scale(4, 4, 1);
            if (!tiene) RenderSystem.setShaderColor(0, 0, 0, 0.75f);
            g.renderItem(st, 0, 0);
            g.flush();
            RenderSystem.setShaderColor(1, 1, 1, 1);
            g.pose().popPose();
            int y = ry + 82;
            Component n = tiene ? st.getHoverName().copy() : Component.literal("???");
            for (FormattedCharSequence s : font.split(n.copy().withStyle(ChatFormatting.BOLD), rw)) {
                g.drawString(font, s, rcx - font.width(s) / 2, y, tiene ? ROJO : GRIS, false);
                y += 10;
            }
            y += 6;
            Component d = !tiene ? Component.translatable("darkcraft.sombra.coleccion.falta")
                    : c.startsWith("libro") ? Component.translatable("darkcraft.coleccion.libro", romano(Integer.parseInt(c.substring(5))))
                    : Component.translatable("darkcraft.coleccion." + c);
            parrafo(g, d, rx + 4, y, rw - 8, tiene ? TINTA : GRIS);
        } else {
            lSel = Mth.clamp(lSel, 0, BESTIARIO.length - 1);
            String c = BESTIARIO[lSel];
            boolean visto = e.bestiario().contains(c);
            LivingEntity le = criatura(c);
            int caja = Math.round(130 * k);
            if (le != null) {
                float giro = (net.minecraft.Util.getMillis() % 12000L) / 12000f * 360f;
                le.yBodyRot = 180 + giro;
                le.yBodyRotO = le.yBodyRot;
                le.setYRot(180 + giro);
                le.yHeadRot = le.getYRot();
                le.yHeadRotO = le.yHeadRot;
                le.setXRot(0);
                int esc = Math.max(8, Math.round(caja / Math.max(1f, le.getBbHeight())));
                if (visto) RenderSystem.setShaderColor(0.92f, 0.84f, 0.72f, 1f);
                else RenderSystem.setShaderColor(0, 0, 0, 1);
                try {
                    InventoryScreen.renderEntityInInventory(g, rcx, ry + 10 + caja, esc, new Quaternionf().rotateZ((float) Math.PI), null, le);
                } catch (RuntimeException ex) {
                    DarkCraftMod.LOGGER.debug("Bestiario: no se pudo dibujar {}: {}", c, ex.toString());
                }
                RenderSystem.setShaderColor(1, 1, 1, 1);
            }
            int y = ry + caja + 18;
            Component n = visto ? Component.translatable(nombreBestia(c)) : Component.literal("???");
            for (FormattedCharSequence s : font.split(n.copy().withStyle(ChatFormatting.BOLD), rw)) {
                g.drawString(font, s, rcx - font.width(s) / 2, y, visto ? ROJO : GRIS, false);
                y += 10;
            }
            y += 4;
            Component d = visto ? Component.translatable("darkcraft.bestiario." + c + ".desc") : Component.translatable("darkcraft.sombra.bestiario.falta");
            y = parrafo(g, d, rx + 4, y, rw - 8, visto ? TINTA : GRIS);
            if (visto) {
                g.pose().pushPose();
                g.pose().translate(X(450), Y(70), 0);
                g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-14));
                Component sello = Component.translatable("darkcraft.sombra.bestiario.derrotado").withStyle(ChatFormatting.BOLD);
                int w = font.width(sello);
                g.fill(-w / 2 - 4, -3, w / 2 + 4, 10, 0x30000000 | (ROJO & 0xffffff));
                g.drawString(font, sello, -w / 2, 0, ROJO, false);
                g.pose().popPose();
            }
        }
    }

    private boolean clicLogros(double mx, double my) {
        int lx = X(34), ly = Y(24), lw = X(240) - lx;
        for (int i = 0; i < 3; i++) {
            if (mx >= subX[i] && mx < subX[i] + subW[i] && my >= ly - 1 && my < ly + 18) {
                if (subLogros != i) {
                    subLogros = i;
                    lSel = 0;
                    scroll = 0;
                    pasarPagina();
                }
                return true;
            }
        }
        int top = ly + 24;
        if (subLogros == 0) {
            int i = scroll + (int) Math.floor((my - top) / 20.0);
            if (mx >= lx && mx < lx + lw && my >= top && i >= 0 && i < EspejoCliente.estado.logros().size()) {
                lSel = i;
                return true;
            }
        } else if (subLogros == 1) {
            int cel = 24, cols = Math.max(1, lw / cel), x0 = lx + (lw - cols * cel) / 2;
            int cx = (int) Math.floor((mx - x0) / cel), cy = (int) Math.floor((my - top) / cel);
            int i = cy * cols + cx;
            if (mx >= x0 && cx < cols && cy >= 0 && i >= 0 && i < COLECCION.length) {
                lSel = i;
                return true;
            }
        } else {
            int i = (int) Math.floor((my - top) / 22.0);
            if (mx >= lx && mx < lx + lw && my >= top && i >= 0 && i < BESTIARIO.length) {
                lSel = i;
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ Mapa

    private Vista vista;
    private Marca marca;

    /** Nombre de una dimension para el selector. */
    static Component nombreDim(String dim) {
        String k = "darkcraft.sombra.dim." + dim.replace(':', '.');
        if (I18n.exists(k)) return Component.translatable(k);
        String p = dim.contains(":") ? dim.substring(dim.indexOf(':') + 1) : dim;
        p = p.replace('_', ' ');
        return Component.literal(p.isEmpty() ? dim : Character.toUpperCase(p.charAt(0)) + p.substring(1));
    }

    /** El mapa del libro: lo explorado (estilo tinta sobre pergamino), niebla, sitios, marcas y jugadores. */
    final class Vista {
        double vx, vz;
        float ppb = 1.0f;
        String dim;
        Exploracion ex;
        Marcas marcas;
        final Map<Long, Tex> texturas = new HashMap<>();
        boolean arrastrando;
        double pulso = -1;

        final class Tex {
            DynamicTexture t;
            ResourceLocation id;
            int version = -1;
            final int rx, rz;

            Tex(int rx, int rz) {
                this.rx = rx;
                this.rz = rz;
            }
        }

        Vista() {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                vx = mc.player.getX();
                vz = mc.player.getZ();
            }
            ponerDim(mc.level != null ? mc.level.dimension().location().toString() : "minecraft:overworld");
        }

        void ponerDim(String d) {
            soltar();
            dim = d;
            ex = EspejoCliente.otra(d);
            marcas = (EspejoCliente.exploracion() == ex && EspejoCliente.marcas() != null) ? EspejoCliente.marcas() : new Marcas(ex.carpeta());
        }

        boolean esLaDelJugador() {
            Minecraft mc = Minecraft.getInstance();
            return mc.level != null && mc.level.dimension().location().toString().equals(dim);
        }

        List<String> dims() {
            List<String> l = new ArrayList<>(Exploracion.dimensionesGuardadas(EspejoCliente.mundo == null ? Exploracion.mundoActual() : EspejoCliente.mundo));
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && !l.contains(mc.level.dimension().location().toString())) l.add(mc.level.dimension().location().toString());
            l.sort((a, b) -> Integer.compare(orden(a), orden(b)));
            return l;
        }

        private int orden(String d) {
            return switch (d) {
                case "minecraft:overworld" -> 0;
                case "minecraft:the_nether" -> 1;
                case "minecraft:the_end" -> 2;
                default -> 3 + Math.abs(d.hashCode() % 1000);
            };
        }

        void soltar() {
            Minecraft mc = Minecraft.getInstance();
            for (Tex t : texturas.values()) if (t.id != null) mc.getTextureManager().release(t.id);
            texturas.clear();
        }

        int x0() {
            return X(30);
        }

        int y0() {
            return Y(34);
        }

        int x1() {
            return X(482);
        }

        int y1() {
            return Y(284);
        }

        double cx() {
            return (x0() + x1()) / 2.0;
        }

        double cy() {
            return (y0() + y1()) / 2.0;
        }

        float sx(double wx) {
            return (float) (cx() + (wx - vx) * ppb);
        }

        float sy(double wz) {
            return (float) (cy() + (wz - vz) * ppb);
        }

        boolean dentro(double mx, double my) {
            return mx >= x0() && mx < x1() && my >= y0() && my < y1();
        }

        boolean zoom(int d, double mx, double my) {
            if (!dentro(mx, my)) {
                mx = cx();
                my = cy();
            }
            double wx = vx + (mx - cx()) / ppb, wz = vz + (my - cy()) / ppb;
            float nuevo = Mth.clamp(d > 0 ? ppb * 1.4f : ppb / 1.4f, 0.04f, 6f);
            if (nuevo == ppb) return true;
            ppb = nuevo;
            vx = wx - (mx - cx()) / ppb;
            vz = wz - (my - cy()) / ppb;
            return true;
        }

        private double ultX, ultY;

        boolean arrastrar(double mx, double my, double dx, double dy) {
            if (!arrastrando) return false;
            vx -= dx / ppb;
            vz -= dy / ppb;
            return true;
        }

        void soltarRaton() {
            arrastrando = false;
        }

        /** Estilo del libro: tinta sobre pergamino (los colores del mundo, apagados y con un poco de sepia). */
        int estilo(int argb) {
            if ((argb >>> 24) == 0) return 0;
            if (argb == Exploracion.VACIO) return 0xff1a1410;
            int r = (argb >> 16) & 255, g = (argb >> 8) & 255, b = argb & 255;
            float l = 0.3f * r + 0.59f * g + 0.11f * b;
            float rr = r + (l * 1.04f + 18 - r) * 0.42f, gg = g + (l * 0.95f + 8 - g) * 0.42f, bb = b + (l * 0.78f - b) * 0.42f;
            rr = rr * 0.86f + 0xD8 * 0.10f;
            gg = gg * 0.86f + 0xC3 * 0.10f;
            bb = bb * 0.86f + 0x9A * 0.10f;
            return 0xff000000 | (EspejoHud.cl(bb) << 16) | (EspejoHud.cl(gg) << 8) | EspejoHud.cl(rr);
        }

        private int creadas;

        Tex textura(int rx, int rz) {
            long k2 = ((long) rx << 32) ^ (rz & 0xffffffffL);
            Tex t = texturas.get(k2);
            Exploracion.Region r = ex.region(rx, rz, false);
            if (r == null) return null;
            if (t == null) {
                if (creadas >= 3) return null; // unas pocas por fotograma
                creadas++;
                t = new Tex(rx, rz);
                texturas.put(k2, t);
            }
            if (t.version != r.version || t.t == null) {
                if (t.t == null) {
                    t.t = new DynamicTexture(Exploracion.R, Exploracion.R, true);
                    t.id = Minecraft.getInstance().getTextureManager().register("darkcraft_libro_mapa", t.t);
                }
                NativeImage img = t.t.getPixels();
                if (img != null) {
                    for (int z = 0; z < Exploracion.R; z++)
                        for (int x = 0; x < Exploracion.R; x++) img.setPixelRGBA(x, z, estilo(r.px[z * Exploracion.R + x]));
                    t.t.upload();
                }
                t.version = r.version;
            }
            return t;
        }

        void render(GuiGraphics g, int mx, int my, float pt) {
            Minecraft mc = Minecraft.getInstance();
            creadas = 0;
            int x0 = x0(), y0 = y0(), x1 = x1(), y1 = y1();
            // arriba: el selector de dimension y el titulo
            List<String> ds = dims();
            int idx = Math.max(0, ds.indexOf(dim));
            Component nd = nombreDim(dim);
            int selX = X(34), selY = Y(19);
            boolean varias = ds.size() > 1;
            flecha(g, selX, selY, "◀", varias, mx, my);
            g.drawString(font, nd, selX + 12, selY, ROJO, false);
            flecha(g, selX + 16 + font.width(nd), selY, "▶", varias, mx, my);
            selector[0] = selX;
            selector[1] = selX + 16 + font.width(nd);
            selector[2] = selY;
            Component tit = Component.translatable("darkcraft.sombra.mapa").withStyle(ChatFormatting.BOLD);
            centro(g, tit, (X(272) + X(478)) / 2, selY, ROJO);
            // el mapa
            g.enableScissor(x0, y0, x1, y1);
            g.fill(x0, y0, x1, y1, 0x5a140608);
            float t = (net.minecraft.Util.getMillis() - abierto) / 1000f;
            for (int i = 0; i < 14; i++) {
                float hx = x0 + ((i * 97 + t * 3) % (x1 - x0 + 80)) - 40, hy = y0 + ((i * 61) % (y1 - y0 + 60)) - 30;
                float lado = 70 + (i % 4) * 22;
                RenderSystem.setShaderColor(0.05f, 0.01f, 0.02f, 0.28f);
                g.blit(EspejoHud.HUMO, Math.round(hx - lado / 2), Math.round(hy - lado / 2), Math.round(lado), Math.round(lado), 0, 0, 64, 64, 64, 64);
            }
            RenderSystem.setShaderColor(1, 1, 1, 1);
            double wx0 = vx + (x0 - cx()) / ppb, wx1 = vx + (x1 - cx()) / ppb, wz0 = vz + (y0 - cy()) / ppb, wz1 = vz + (y1 - cy()) / ppb;
            int rx0 = Math.floorDiv((int) Math.floor(wx0), Exploracion.R), rx1 = Math.floorDiv((int) Math.floor(wx1), Exploracion.R);
            int rz0 = Math.floorDiv((int) Math.floor(wz0), Exploracion.R), rz1 = Math.floorDiv((int) Math.floor(wz1), Exploracion.R);
            if ((rx1 - rx0 + 1) * (rz1 - rz0 + 1) <= 400) {
                for (int rx = rx0; rx <= rx1; rx++)
                    for (int rz = rz0; rz <= rz1; rz++) {
                        Tex tx = textura(rx, rz);
                        if (tx == null || tx.id == null) continue;
                        g.pose().pushPose();
                        g.pose().translate(sx(rx * (double) Exploracion.R), sy(rz * (double) Exploracion.R), 0);
                        g.pose().scale(ppb, ppb, 1);
                        g.blit(tx.id, 0, 0, 0, 0, Exploracion.R, Exploracion.R, Exploracion.R, Exploracion.R);
                        g.pose().popPose();
                    }
            }
            // sombra del lomo del libro por encima del mapa
            int gx = X(256);
            for (int i = 0; i < 10; i++) {
                int a = (int) (60 * (1 - i / 10f));
                g.fill(gx - i - 1, y0, gx - i, y1, a << 24);
                g.fill(gx + i, y0, gx + i + 1, y1, a << 24);
            }
            List<Component> hover = new ArrayList<>();
            marcadores(g, mc, mx, my, pt, hover);
            g.disableScissor();
            // abajo: botones y ayuda
            int by2 = Y(290);
            botones[0] = boton(g, X(34), by2, Component.translatable("darkcraft.sombra.mapa.centrar"), mx, my);
            botones[1] = boton(g, botones[0][2] + 10, by2, Component.translatable("darkcraft.sombra.mapa.nueva"), mx, my);
            Component tam = Component.translatable("darkcraft.sombra.mapa.tamano",
                    Component.translatable("darkcraft.sombra.mapa.tam." + EspejoCliente.tamano));
            botones[2] = boton(g, X(272), by2, tam, mx, my);
            g.pose().pushPose();
            g.pose().translate(X(478), by2 + 1, 0);
            g.pose().scale(0.75f, 0.75f, 1);
            Component ay = Component.translatable("darkcraft.sombra.mapa.ayuda");
            g.drawString(font, ay, -font.width(ay), 0, GRIS, false);
            g.pose().popPose();
            if (!hover.isEmpty() && marca == null && borrar == null) tooltip(hover);
            if (borrar != null) confirmarBorrar(g, mx, my);
        }

        final int[] selector = new int[3];
        final int[][] botones = new int[3][];
        Marcas.Marca borrar;
        int[] siNo;

        int[] boton(GuiGraphics g, int x, int y, Component c, int mx, int my) {
            int w = font.width(c);
            boolean sobre = mx >= x - 2 && mx < x + w + 2 && my >= y - 2 && my < y + 10;
            g.drawString(font, c, x, y, sobre ? ROJO : TINTA, false);
            g.fill(x, y + 9, x + w, y + 10, sobre ? ROJO : 0x60000000 | (TINTA & 0xffffff));
            return new int[]{x - 2, y - 2, x + w + 2, y + 11};
        }

        boolean en(int[] r, double mx, double my) {
            return r != null && mx >= r[0] && mx < r[2] && my >= r[1] && my < r[3];
        }

        private void marcadores(GuiGraphics g, Minecraft mc, int mx, int my, float pt, List<Component> hover) {
            float l = Mth.clamp(10 + ppb * 2.5f, 10, 16);
            float t = (net.minecraft.Util.getMillis() - abierto) / 1000f;
            Net.EspejoEstado e = EspejoCliente.estado;
            int siguiente = Story.next(e.leidos());
            if (dim.equals(Level.OVERWORLD.location().toString())) {
                for (Net.LugarE lu : e.lugares()) {
                    float x = sx(lu.x() + 0.5), y = sy(lu.z() + 0.5);
                    if (x < x0() - 20 || x > x1() + 20 || y < y0() - 20 || y > y1() + 20) continue;
                    int ic = lu.icono();
                    if (lu.tipo() == Espejo.COFRE && lu.estado() == Espejo.HECHO) ic = EspejoHud.I_COFRE_LEIDO;
                    boolean objetivo = lu.tipo() == Espejo.COFRE && lu.cap() == siguiente && lu.estado() != Espejo.HECHO;
                    if (objetivo) {
                        float lat = 0.6f + 0.4f * (float) Math.sin(t * 4);
                        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                        RenderSystem.setShaderColor(1f, 0.2f, 0.1f, 0.8f * lat);
                        float r = l * 1.5f;
                        g.blit(EspejoHud.BRILLO, Math.round(x - r), Math.round(y - r), Math.round(r * 2), Math.round(r * 2), 0, 0, 64, 64, 64, 64);
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.setShaderColor(1, 1, 1, 1);
                    }
                    EspejoHud.icono(g, ic, x, y, l);
                    if (lu.tipo() == Espejo.JEFE && lu.estado() == Espejo.HECHO) {
                        RenderSystem.setShaderColor(0.85f, 0.1f, 0.08f, 1f);
                        EspejoHud.icono(g, EspejoHud.I_EQUIS, x, y, l * 0.85f);
                        RenderSystem.setShaderColor(1, 1, 1, 1);
                    }
                    Component nombre = lu.tipo() == Espejo.COFRE ? Component.translatable(lu.nombre(), romano(lu.cap())) : Component.translatable(lu.nombre());
                    if (lu.tipo() == Espejo.LUGAR && ppb >= 0.55f) {
                        g.pose().pushPose();
                        g.pose().translate(x, y + l / 2 + 1, 0);
                        g.pose().scale(0.7f, 0.7f, 1);
                        int w = font.width(nombre);
                        g.drawString(font, nombre, -w / 2, 0, TINTA, false);
                        g.pose().popPose();
                    }
                    if (Math.abs(mx - x) <= l / 2 && Math.abs(my - y) <= l / 2) {
                        hover.add(nombre.copy().withStyle(ChatFormatting.GOLD));
                        if (lu.tipo() == Espejo.COFRE) hover.add(Component.translatable(lu.estado() == Espejo.HECHO ? "darkcraft.sombra.cofre.leido"
                                : "darkcraft.sombra.cofre.pendiente").withStyle(ChatFormatting.GRAY));
                        if (lu.tipo() == Espejo.JEFE) hover.add(Component.translatable(lu.estado() == Espejo.HECHO ? "darkcraft.sombra.bestiario.derrotado"
                                : "darkcraft.sombra.jefe.vivo").withStyle(ChatFormatting.DARK_RED));
                        hover.add(Component.literal("X " + lu.x() + "  Z " + lu.z()).withStyle(ChatFormatting.DARK_GRAY));
                    }
                }
            }
            // marcas propias
            for (Marcas.Marca m : marcas.lista) {
                float x = sx(m.x + 0.5), y = sy(m.z + 0.5);
                int c = m.color;
                RenderSystem.setShaderColor(((c >> 16) & 255) / 255f, ((c >> 8) & 255) / 255f, (c & 255) / 255f, 1f);
                float lm = l + (m == nueva && t - nuevaT < 2.5f ? 6f * (float) Math.max(0, Math.sin((t - nuevaT) * 6)) : 0);
                EspejoHud.icono(g, EspejoHud.I_MARCAS + Mth.clamp(m.icono, 0, Marcas.ICONOS - 1), x, y, lm);
                RenderSystem.setShaderColor(1, 1, 1, 1);
                if (ppb >= 0.8f) {
                    g.pose().pushPose();
                    g.pose().translate(x, y + l / 2 + 1, 0);
                    g.pose().scale(0.7f, 0.7f, 1);
                    g.drawString(font, m.nombre, -font.width(m.nombre) / 2, 0, 0xff000000 | Exploracion.escala(c, 0.55f), false);
                    g.pose().popPose();
                }
                if (Math.abs(mx - x) <= l / 2 && Math.abs(my - y) <= l / 2) {
                    hover.add(Component.literal(m.nombre).withStyle(s -> s.withColor(c)));
                    hover.add(Component.literal("X " + m.x + "  Y " + m.y + "  Z " + m.z).withStyle(ChatFormatting.DARK_GRAY));
                    hover.add(Component.translatable("darkcraft.sombra.marca.quitar").withStyle(ChatFormatting.GRAY));
                }
            }
            if (!esLaDelJugador()) return;
            // amigos
            for (Net.Amigo a : EspejoCliente.amigos()) {
                double ax = a.x() + 0.5, az = a.z() + 0.5;
                var ent = mc.level.getPlayerByUUID(a.id());
                if (ent != null) {
                    ax = Mth.lerp(pt, ent.xo, ent.getX());
                    az = Mth.lerp(pt, ent.zo, ent.getZ());
                }
                float x = sx(ax), y = sy(az);
                int lado = Math.round(l);
                g.fill(Math.round(x - lado / 2f) - 1, Math.round(y - lado / 2f) - 1, Math.round(x - lado / 2f) + lado + 1, Math.round(y - lado / 2f) + lado + 1, 0xE08A1016);
                PlayerFaceRenderer.draw(g, EspejoHud.piel(mc, a), Math.round(x - lado / 2f), Math.round(y - lado / 2f), lado);
                if (Math.abs(mx - x) <= l / 2 && Math.abs(my - y) <= l / 2) {
                    hover.add(Component.literal(a.nombre()).withStyle(ChatFormatting.WHITE));
                    hover.add(Component.literal("X " + a.x() + "  Y " + a.y() + "  Z " + a.z()).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
            // el jugador: la flecha gira hacia donde mira
            if (mc.player != null) {
                float x = sx(Mth.lerp(pt, mc.player.xo, mc.player.getX())), y = sy(Mth.lerp(pt, mc.player.zo, mc.player.getZ()));
                float yaw = Mth.rotLerp(pt, mc.player.yRotO, mc.player.getYRot());
                g.pose().pushPose();
                g.pose().translate(x, y, 0);
                g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(yaw - 180));
                EspejoHud.icono(g, EspejoHud.I_JUGADOR, 0, 0, l * 1.1f);
                g.pose().popPose();
                if (Math.abs(mx - x) <= l / 2 && Math.abs(my - y) <= l / 2) hover.add(Component.translatable("darkcraft.mapa.tu"));
            }
        }

        Marcas.Marca nueva;
        float nuevaT;

        Marcas.Marca marcaEn(double mx, double my) {
            float l = Mth.clamp(10 + ppb * 2.5f, 10, 16);
            for (Marcas.Marca m : marcas.lista)
                if (Math.abs(mx - sx(m.x + 0.5)) <= l / 2 && Math.abs(my - sy(m.z + 0.5)) <= l / 2) return m;
            return null;
        }

        boolean clic(double mx, double my, int b) {
            if (borrar != null) {
                if (siNo != null && en(new int[]{siNo[0], siNo[1], siNo[2], siNo[3]}, mx, my)) {
                    marcas.quitar(borrar);
                    pasarPagina();
                } else if (siNo != null && en(new int[]{siNo[4], siNo[5], siNo[6], siNo[7]}, mx, my)) {
                    // nada
                } else return true;
                borrar = null;
                return true;
            }
            if (mx >= selector[0] - 3 && mx < selector[0] + 10 && Math.abs(my - selector[2] - 3) < 8) {
                cambiarDim(-1);
                return true;
            }
            if (mx >= selector[1] - 3 && mx < selector[1] + 10 && Math.abs(my - selector[2] - 3) < 8) {
                cambiarDim(1);
                return true;
            }
            Minecraft mc = Minecraft.getInstance();
            if (en(botones[0], mx, my)) {
                if (mc.player != null && esLaDelJugador()) {
                    vx = mc.player.getX();
                    vz = mc.player.getZ();
                }
                pasarPagina();
                return true;
            }
            if (en(botones[1], mx, my)) {
                if (mc.player != null && esLaDelJugador()) marca = new Marca(this, mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ());
                return true;
            }
            if (en(botones[2], mx, my)) {
                EspejoCliente.tamano = (EspejoCliente.tamano + 1) % EspejoCliente.TAMANO.length;
                EspejoCliente.guardarOpciones();
                pasarPagina();
                return true;
            }
            if (!dentro(mx, my)) return false;
            if (b == 1) {
                Marcas.Marca m = marcaEn(mx, my);
                if (m != null) {
                    borrar = m;
                    return true;
                }
                int wx = (int) Math.floor(vx + (mx - cx()) / ppb), wz = (int) Math.floor(vz + (my - cy()) / ppb);
                int wy = mc.player != null ? mc.player.getBlockY() : 64;
                marca = new Marca(this, wx, wy, wz);
                return true;
            }
            if (b == 0) {
                arrastrando = true;
                return true;
            }
            return false;
        }

        void cambiarDim(int d) {
            List<String> ds = dims();
            if (ds.size() < 2) return;
            int i = Math.max(0, ds.indexOf(dim));
            ponerDim(ds.get(Math.floorMod(i + d, ds.size())));
            Minecraft mc = Minecraft.getInstance();
            if (esLaDelJugador() && mc.player != null) {
                vx = mc.player.getX();
                vz = mc.player.getZ();
            } else {
                vx = 0;
                vz = 0;
                for (Exploracion.Region r : ex.cargadas()) {
                    vx = r.rx * Exploracion.R + Exploracion.R / 2.0;
                    vz = r.rz * Exploracion.R + Exploracion.R / 2.0;
                    break;
                }
                List<long[]> todas = ex.todas();
                if (!todas.isEmpty()) {
                    vx = todas.get(0)[0] * Exploracion.R + Exploracion.R / 2.0;
                    vz = todas.get(0)[1] * Exploracion.R + Exploracion.R / 2.0;
                }
            }
            pasarPagina();
        }

        private void confirmarBorrar(GuiGraphics g, int mx, int my) {
            int cx = (x0() + x1()) / 2, cy = (y0() + y1()) / 2;
            Component q = Component.translatable("darkcraft.sombra.marca.borrar", borrar.nombre);
            int w = Math.max(140, font.width(q) + 20);
            g.fill(cx - w / 2 - 2, cy - 24, cx + w / 2 + 2, cy + 24, 0xF0201008);
            g.fill(cx - w / 2, cy - 22, cx + w / 2, cy + 22, 0xF0E6D3A8);
            centro(g, q, cx, cy - 14, TINTA);
            int[] si = boton(g, cx - 50, cy + 6, Component.translatable("darkcraft.sombra.marca.si"), mx, my);
            int[] no = boton(g, cx + 14, cy + 6, Component.translatable("darkcraft.sombra.marca.no"), mx, my);
            siNo = new int[]{si[0], si[1], si[2], si[3], no[0], no[1], no[2], no[3]};
        }
    }

    // ------------------------------------------------------------------ escribir una marca

    /** El dialogo de una marca nueva: nombre, simbolo y tinta; al escribirla, el personaje la escribe con la pluma. */
    final class Marca {
        final Vista v;
        final int x, y, z;
        EditBox nombre;
        int icono = 0, color = 0;
        long escribiendo = -1;
        String texto = "";

        Marca(Vista v, int x, int y, int z) {
            this.v = v;
            this.x = x;
            this.y = y;
            this.z = z;
            colocar();
        }

        void colocar() {
            String antes = nombre != null ? nombre.getValue() : I18n.get("darkcraft.sombra.marca.defecto");
            if (nombre != null) removeWidget(nombre);
            int rx = X(272), rw = X(478) - rx;
            nombre = new EditBox(font, rx + 4, Y(26) + 36, rw - 8, 12, Component.translatable("darkcraft.sombra.marca.nombre"));
            nombre.setMaxLength(32);
            nombre.setBordered(false);
            nombre.setTextColor(0x3A2414);
            nombre.setValue(antes);
            nombre.setFocused(true);
            addRenderableWidget(nombre);
            setFocused(nombre);
        }

        void cerrar() {
            if (nombre != null) removeWidget(nombre);
            nombre = null;
            marca = null;
        }

        void escribir() {
            if (escribiendo >= 0) return;
            texto = nombre.getValue().trim();
            if (texto.isEmpty()) texto = I18n.get("darkcraft.sombra.marca.defecto");
            escribiendo = net.minecraft.Util.getMillis();
            nombre.setFocused(false);
            nombre.setEditable(false);
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(DarkCraftMod.SFX_PLUMA.get(), 1.0f, 0.9f));
        }

        void render(GuiGraphics g, int mx, int my, float pt) {
            int lx = X(34), lw = X(240) - lx, rx = X(272), rw = X(478) - rx, rcx = (X(272) + X(478)) / 2;
            centro(g, Component.translatable("darkcraft.sombra.marca.titulo").withStyle(ChatFormatting.BOLD), rcx, Y(26), ROJO);
            g.pose().pushPose();
            g.pose().translate(rcx, Y(26) + 12, 0);
            g.pose().scale(0.8f, 0.8f, 1);
            String pos = "X " + x + "   Y " + y + "   Z " + z;
            g.drawString(font, pos, -font.width(pos) / 2, 0, GRIS, false);
            g.pose().popPose();
            g.drawString(font, Component.translatable("darkcraft.sombra.marca.nombre"), rx + 4, Y(26) + 25, ORO, false);
            g.fill(rx + 2, Y(26) + 48, rx + rw - 2, Y(26) + 49, 0x80000000 | (TINTA & 0xffffff));
            int yi = Y(26) + 58;
            g.drawString(font, Component.translatable("darkcraft.sombra.marca.icono"), rx + 4, yi, ORO, false);
            int cel = 20, cols = 6;
            int gx = rx + (rw - cols * cel) / 2;
            for (int i = 0; i < Marcas.ICONOS; i++) {
                int cx = gx + (i % cols) * cel, cy = yi + 12 + (i / cols) * cel;
                boolean sobre = escribiendo < 0 && mx >= cx && mx < cx + cel - 2 && my >= cy && my < cy + cel - 2;
                g.fill(cx, cy, cx + cel - 2, cy + cel - 2, i == icono ? 0x60000000 | (ROJO & 0xffffff) : sobre ? 0x30000000 : 0x16000000);
                int c = Marcas.COLORES[color];
                RenderSystem.setShaderColor(((c >> 16) & 255) / 255f, ((c >> 8) & 255) / 255f, (c & 255) / 255f, 1);
                EspejoHud.icono(g, EspejoHud.I_MARCAS + i, cx + (cel - 2) / 2f, cy + (cel - 2) / 2f, 14);
                RenderSystem.setShaderColor(1, 1, 1, 1);
            }
            int yc = yi + 12 + 2 * cel + 6;
            g.drawString(font, Component.translatable("darkcraft.sombra.marca.color"), rx + 4, yc, ORO, false);
            int cw = 16;
            int gx2 = rx + (rw - Marcas.COLORES.length * (cw + 3)) / 2;
            for (int i = 0; i < Marcas.COLORES.length; i++) {
                int cx = gx2 + i * (cw + 3), cy = yc + 12;
                g.fill(cx - 1, cy - 1, cx + cw + 1, cy + cw + 1, i == color ? ROJO : 0xFF2A1A10);
                g.fill(cx, cy, cx + cw, cy + cw, 0xFF000000 | Marcas.COLORES[i]);
            }
            int yb = Y(282);
            if (escribiendo < 0) {
                botonesMarca[0] = v.boton(g, rx + 8, yb, Component.translatable("darkcraft.sombra.marca.escribir").withStyle(ChatFormatting.BOLD), mx, my);
                botonesMarca[1] = v.boton(g, X(478) - 8 - font.width(Component.translatable("darkcraft.sombra.marca.cancelar")), yb,
                        Component.translatable("darkcraft.sombra.marca.cancelar"), mx, my);
            }
            // la escena: el personaje escribiendo en el pergamino
            Escena.render(g, LibroScreen.this, lx, Y(24), lw, Y(296) - Y(24), escribiendo < 0 ? -1f
                    : (net.minecraft.Util.getMillis() - escribiendo) / (float) DURA, escribiendo < 0 ? nombre.getValue() : texto, icono, Marcas.COLORES[color]);
            if (escribiendo >= 0 && net.minecraft.Util.getMillis() - escribiendo > DURA) terminar();
        }

        final int[][] botonesMarca = new int[2][];

        boolean clic(double mx, double my, int b) {
            if (escribiendo >= 0) return true;
            int rx = X(272), rw = X(478) - rx;
            int yi = Y(26) + 58, cel = 20, cols = 6;
            int gx = rx + (rw - cols * cel) / 2;
            for (int i = 0; i < Marcas.ICONOS; i++) {
                int cx = gx + (i % cols) * cel, cy = yi + 12 + (i / cols) * cel;
                if (mx >= cx && mx < cx + cel - 2 && my >= cy && my < cy + cel - 2) {
                    icono = i;
                    return true;
                }
            }
            int yc = yi + 12 + 2 * cel + 6, cw = 16;
            int gx2 = rx + (rw - Marcas.COLORES.length * (cw + 3)) / 2;
            for (int i = 0; i < Marcas.COLORES.length; i++) {
                int cx = gx2 + i * (cw + 3), cy = yc + 12;
                if (mx >= cx && mx < cx + cw && my >= cy && my < cy + cw) {
                    color = i;
                    return true;
                }
            }
            if (v.en(botonesMarca[0], mx, my)) {
                escribir();
                return true;
            }
            if (v.en(botonesMarca[1], mx, my)) {
                cerrar();
                return true;
            }
            if (nombre != null && nombre.mouseClicked(mx, my, b)) {
                nombre.setFocused(true);
                return true;
            }
            return true;
        }

        void terminar() {
            Marcas.Marca m = new Marcas.Marca(texto, icono, Marcas.COLORES[color], x, y, z);
            v.marcas.anadir(m);
            v.nueva = m;
            v.nuevaT = (net.minecraft.Util.getMillis() - abierto) / 1000f;
            EspejoCliente.accion(1);
            cerrar();
        }
    }

    // ------------------------------------------------------------------ para las pruebas de la CI

    /** Abre la seccion s (0 historia, 1 mapa, 2 logros) con la sub-pestana sub y lo elegido sel. */
    public void probar(int s, int sub, int sel) {
        seccion = s;
        if (s == 0) {
            hSel = sel;
            hPag = 0;
        } else if (s == 2) {
            subLogros = sub;
            lSel = sel;
            scroll = 0;
        }
        if (marca != null) marca.cerrar();
    }

    /** Abre el dialogo de una marca nueva en la posicion del jugador con ese nombre (y la escribe si escribir). */
    public void probarMarca(String nombre, int icono, int color, boolean escribir) {
        seccion = 1;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (marca != null) marca.cerrar();
        marca = new Marca(vista, mc.player.getBlockX() + 12, mc.player.getBlockY(), mc.player.getBlockZ() - 9);
        marca.nombre.setValue(nombre);
        marca.icono = icono;
        marca.color = color;
        if (escribir) marca.escribir();
    }

    /** Para las pruebas: el mapa del libro centrado en el jugador con este zoom (px por bloque). */
    public void probarZoom(float ppb) {
        seccion = 1;
        if (vista != null) {
            vista.ppb = ppb;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                vista.vx = mc.player.getX();
                vista.vz = mc.player.getZ();
            }
        }
    }
}
