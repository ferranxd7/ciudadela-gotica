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
 * El mapa del libro de la Sombra (tecla M): un libro viejo de cuero negro con costuras rojas con lo explorado en cada
 * dimension (con niebla donde no se ha estado), los lugares, cofres, jefes, amigos y las marcas propias, que se escriben
 * con la pluma. La historia, el diario y los logros estan en el menu (R): {@code client.ui.MenuScreen} (parte 2e-4).
 */
public final class LibroScreen extends Screen {
    static final ResourceLocation LIBRO = EspejoHud.rl("textures/gui/libro.png"), PESTANAS = EspejoHud.rl("textures/gui/libro_pestanas.png"),
            VINIETA = EspejoHud.rl("textures/gui/vinieta.png"), PERGAMINO = EspejoHud.rl("textures/gui/pergamino.png");
    static final int LW = 512, LH = 320;
    /** Lo que dura la escena de escribir una marca (ms). */
    static final long DURA = 2600;
    static final int TINTA = 0xFF3A2414, ROJO = 0xFF8E1418, GRIS = 0xFF8C7C66, ORO = 0xFF7A4E10, CLARO = 0xFFB8A27E;


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
        // el libro abierto cabe en la pantalla
        int maxW = Math.min(width - 24, 560), maxH = height - 22;
        bw = maxW;
        bh = Math.round(bw * LH / (float) LW);
        if (bh > maxH) {
            bh = maxH;
            bw = Math.round(bh * LW / (float) LH);
        }
        k = bw / (float) LW;
        bx = (width - bw) / 2;
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
        if (marca == null) {
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
        if (marca != null) return marca.clic(mx, my, b);
        return vista.clic(mx, my, b);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int b) {
        if (vista != null) vista.soltarRaton();
        return super.mouseReleased(mx, my, b);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int b, double dx, double dy) {
        if (marca == null && vista.arrastrar(mx, my, dx, dy)) return true;
        return super.mouseDragged(mx, my, b, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double d) {
        if (marca != null) return true;
        return vista.zoom(d > 0 ? 1 : -1, mx, my);
    }

    void pasarPagina() {
        if (minecraft != null) minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0f));
    }

    // ------------------------------------------------------------------ dibujo general

    @Override
    public void renderBackground(GuiGraphics g) {
        float t = (net.minecraft.Util.getMillis() - abierto) / 1000f;
        g.fill(0, 0, width, height, 0xB8070306);
        EspejoHud.mezcla();
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
        if (version != EspejoCliente.version()) version = EspejoCliente.version();
        renderBackground(g);
        EspejoHud.mezcla();
        float p = Mth.clamp((net.minecraft.Util.getMillis() - abierto) / 220f, 0, 1);
        float e = 0.94f + 0.06f * (1 - (1 - p) * (1 - p));
        g.pose().pushPose();
        g.pose().translate(bx + bw / 2f, by + bh / 2f, 0);
        g.pose().scale(e, e, 1);
        g.pose().translate(-(bx + bw / 2f), -(by + bh / 2f), 0);
        RenderSystem.enableBlend();
        if (minecraft != null) minecraft.getTextureManager().getTexture(LIBRO).setFilter(true, false);
        g.blit(LIBRO, bx, by, bw, bh, 0, 0, LW, LH, LW, LH);
        if (marca != null) marca.render(g, mx, my, pt);
        else vista.render(g, mx, my, pt);
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

    private void flecha(GuiGraphics g, int x, int y, String s, boolean activa, int mx, int my) {
        boolean sobre = activa && mx >= x - 3 && mx < x + 10 && my >= y - 3 && my < y + 11;
        g.drawString(font, s, x, y, !activa ? 0x40000000 | (CLARO & 0xffffff) : sobre ? ROJO : TINTA, false);
    }

    Component recortar(Component c, int w) {
        if (font.width(c) <= w) return c;
        String s = c.getString();
        while (s.length() > 1 && font.width(s + "...") > w) s = s.substring(0, s.length() - 1);
        return Component.literal(s + "...").withStyle(c.getStyle());
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
            Component tam = Component.translatable("darkcraft.sombra.mapa.tamano",
                    Component.translatable("darkcraft.sombra.mapa.tam." + EspejoCliente.tamano));
            int tamX = X(478) - font.width(tam);
            botones[2] = boton(g, tamX, selY, tam, mx, my);
            Component tit = Component.translatable("darkcraft.sombra.mapa").withStyle(ChatFormatting.BOLD);
            centro(g, tit, (X(272) + tamX - 8) / 2, selY, ROJO);
            // el mapa
            g.enableScissor(x0, y0, x1, y1);
            g.fill(x0, y0, x1, y1, 0x5a140608);
            EspejoHud.mezcla();
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
                        // las esquinas salen de la misma cuenta que las de la region de al lado: sin rendijas entre regiones
                        EspejoHud.cuadro(g, tx.id, sx(rx * (double) Exploracion.R), sy(rz * (double) Exploracion.R),
                                sx((rx + 1) * (double) Exploracion.R), sy((rz + 1) * (double) Exploracion.R));
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
                        EspejoHud.suma();
                        RenderSystem.setShaderColor(1f, 0.2f, 0.1f, 0.8f * lat);
                        float r = l * 1.5f;
                        g.blit(EspejoHud.BRILLO, Math.round(x - r), Math.round(y - r), Math.round(r * 2), Math.round(r * 2), 0, 0, 64, 64, 64, 64);
                        EspejoHud.mezcla();
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
                EspejoHud.mezcla();
                PlayerFaceRenderer.draw(g, EspejoHud.piel(mc, a), Math.round(x - lado / 2f), Math.round(y - lado / 2f), lado);
                EspejoHud.mezcla(); // la cara (el sombrero de la piel) apaga la mezcla al acabar
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

    /** Abre el dialogo de una marca nueva en la posicion del jugador con ese nombre (y la escribe si escribir). */
    public void probarMarca(String nombre, int icono, int color, boolean escribir) {
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
