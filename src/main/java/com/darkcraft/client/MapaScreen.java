package com.darkcraft.client;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.MapShapes;
import com.darkcraft.net.Net;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapa de pergamino de la ciudadela, dibujado a tinta: la isla con sus acantilados y cascadas, las
 * murallas, torres, casas, la catedral y el Arbol de la Corona, los nombres de los lugares, donde
 * estas tu, los capitulos ya leidos tachados y una calavera que late y brilla en la siguiente pagina
 * de la cronica, con la pista para encontrarla.
 */
public final class MapaScreen extends Screen {
    private static final ResourceLocation PARCH = new ResourceLocation(DarkCraftMod.MODID, "textures/gui/pergamino.png");
    private static final ResourceLocation SKULL = new ResourceLocation(DarkCraftMod.MODID, "textures/gui/calavera.png");
    private static final ResourceLocation GLOW = new ResourceLocation(DarkCraftMod.MODID, "textures/gui/brillo.png");
    private static final int INK = 0x3A2414, WATER = 0x2F5F80, GREEN = 0x3C5626, RED = 0x9A1010;
    private static final float RANGE = 415;

    private final Net.MapOpen d;
    private final long opened = Util.getMillis();
    private final int next;
    private final List<int[]> shapes = new ArrayList<>();
    private final List<int[]> island = new ArrayList<>();

    private float k, ox, oy, mapL, mapT, mapS;
    private int px0, py0, pw, ph;
    private BufferBuilder bb;
    private Matrix4f mat;

    private MapaScreen(Net.MapOpen d) {
        super(Component.translatable("item.darkcraft.mapa_valdrenn"));
        this.d = d;
        int n = 1;
        while (n <= 5 && (d.read() & (1 << n)) != 0) n++;
        this.next = n;
        int[] s = d.shapes();
        int i = 0;
        try {
            while (i < s.length) {
                int type = s[i++];
                int len = switch (type) {
                    case MapShapes.ISLAND, MapShapes.WALL -> 1 + 2 * s[i];
                    case MapShapes.SAT, MapShapes.TOWER, MapShapes.TREE_TOWER, MapShapes.FOUNTAIN, MapShapes.CATHEDRAL -> 3;
                    case MapShapes.BRIDGE -> 4;
                    case MapShapes.BUILDING, MapShapes.AREA, MapShapes.ROAD -> 5;
                    case MapShapes.TREE, MapShapes.FALL -> 2;
                    default -> -1;
                };
                if (len < 0 || i + len > s.length) break;
                int[] rec = new int[len + 1];
                rec[0] = type;
                System.arraycopy(s, i, rec, 1, len);
                i += len;
                if (type == MapShapes.ISLAND) island.add(rec);
                else shapes.add(rec);
            }
        } catch (RuntimeException ignored) {
        }
    }

    public static void open(Net.MapOpen d) {
        Minecraft.getInstance().setScreen(new MapaScreen(d));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (minecraft != null && minecraft.options.keyInventory.matches(key, scan)) {
            onClose();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    // ------------------------------------------------------------------ primitivas de tinta

    private void begin(GuiGraphics g) {
        g.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bb = Tesselator.getInstance().getBuilder();
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        mat = g.pose().last().pose();
    }

    private void end() {
        BufferUploader.drawWithShader(bb.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void v(float x, float y, int rgb, float a) {
        bb.vertex(mat, x, y, 0).color((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, Math.round(Mth.clamp(a, 0, 1) * 255)).endVertex();
    }

    private void quad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, int rgb, float a) {
        v(x1, y1, rgb, a);
        v(x2, y2, rgb, a);
        v(x3, y3, rgb, a);
        v(x4, y4, rgb, a);
    }

    private void tri(float ax, float ay, float bx, float by, float cx, float cy, int rgb, float a) {
        quad(ax, ay, bx, by, cx, cy, cx, cy, rgb, a);
    }

    private void rect(float x0, float y0, float x1, float y1, int rgb, float a) {
        quad(x0, y0, x0, y1, x1, y1, x1, y0, rgb, a);
    }

    private void line(float x1, float y1, float x2, float y2, float w, int rgb, float a) {
        float dx = x2 - x1, dy = y2 - y1, l = Mth.sqrt(dx * dx + dy * dy);
        if (l < 0.01f) return;
        float nx = -dy / l * w / 2, ny = dx / l * w / 2;
        quad(x1 + nx, y1 + ny, x1 - nx, y1 - ny, x2 - nx, y2 - ny, x2 + nx, y2 + ny, rgb, a);
    }

    private void dashed(float x1, float y1, float x2, float y2, float w, float dash, int rgb, float a) {
        float dx = x2 - x1, dy = y2 - y1, l = Mth.sqrt(dx * dx + dy * dy);
        int n = Math.max(1, (int) (l / dash));
        for (int i = 0; i < n; i += 2) {
            float t0 = i / (float) n, t1 = Math.min(1, (i + 1) / (float) n);
            line(x1 + dx * t0, y1 + dy * t0, x1 + dx * t1, y1 + dy * t1, w, rgb, a);
        }
    }

    private void disc(float cx, float cy, float r, int rgb, float a) {
        int n = Math.max(8, Math.min(40, (int) (r * 2.5f)));
        for (int i = 0; i < n; i++) {
            float a0 = i * Mth.TWO_PI / n, a1 = (i + 1) * Mth.TWO_PI / n;
            tri(cx, cy, cx + Mth.cos(a0) * r, cy + Mth.sin(a0) * r, cx + Mth.cos(a1) * r, cy + Mth.sin(a1) * r, rgb, a);
        }
    }

    private void ring(float cx, float cy, float r, float w, int rgb, float a) {
        int n = Math.max(10, Math.min(48, (int) (r * 2.5f)));
        for (int i = 0; i < n; i++) {
            float a0 = i * Mth.TWO_PI / n, a1 = (i + 1) * Mth.TWO_PI / n;
            line(cx + Mth.cos(a0) * r, cy + Mth.sin(a0) * r, cx + Mth.cos(a1) * r, cy + Mth.sin(a1) * r, w, rgb, a);
        }
    }

    private void box(float x0, float y0, float x1, float y1, float w, int rgb, float a) {
        line(x0, y0, x1, y0, w, rgb, a);
        line(x1, y0, x1, y1, w, rgb, a);
        line(x1, y1, x0, y1, w, rgb, a);
        line(x0, y1, x0, y0, w, rgb, a);
    }

    private float sx(float bx) {
        return ox + bx * k;
    }

    private float sy(float bz) {
        return oy + bz * k;
    }

    // ------------------------------------------------------------------ pantalla

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        float t = (Util.getMillis() - opened) / 1000f;
        float unroll = 1 - (1 - Mth.clamp(t / 0.35f, 0, 1)) * (1 - Mth.clamp(t / 0.35f, 0, 1));
        ph = Math.min(height - 12, (int) ((width - 12) / 1.42f));
        pw = (int) (ph * 1.42f);
        px0 = (width - pw) / 2;
        py0 = (height - ph) / 2;
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(width / 2f, height / 2f, 0);
        pose.scale(1, Math.max(0.05f, unroll), 1);
        pose.translate(-width / 2f, -height / 2f, 0);
        RenderSystem.enableBlend();
        g.blit(PARCH, px0, py0, pw, ph, 0, 0, 512, 512, 512, 512);
        RenderSystem.disableBlend();
        // zona del dibujo (a la izquierda) y columna de la cronica (a la derecha)
        mapS = ph * 0.80f;
        mapL = px0 + pw * 0.055f;
        mapT = py0 + ph * 0.135f;
        k = mapS / (2 * RANGE);
        ox = mapL + mapS / 2;
        oy = mapT + mapS / 2;
        drawMap(g, t);
        drawLabels(g);
        drawMarkers(g, t);
        drawPlayer(g, t, pt);
        drawTitle(g);
        drawChronicle(g, t);
        pose.popPose();
        if (unroll >= 1) tooltip(g, mx, my);
    }

    private void drawMap(GuiGraphics g, float t) {
        begin(g);
        // isla: relleno, contorno y acantilados
        for (int[] isl : island) {
            int n = isl[1];
            float cx = sx(0), cy = sy(0);
            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;
                float x0 = sx(isl[2 + i * 2]), y0 = sy(isl[3 + i * 2]), x1 = sx(isl[2 + j * 2]), y1 = sy(isl[3 + j * 2]);
                tri(cx, cy, x0, y0, x1, y1, 0x7A5A30, 0.13f);
            }
            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;
                float x0 = sx(isl[2 + i * 2]), y0 = sy(isl[3 + i * 2]), x1 = sx(isl[2 + j * 2]), y1 = sy(isl[3 + j * 2]);
                line(x0, y0, x1, y1, 1.5f, INK, 0.9f);
                float mx = (x0 + x1) / 2, my = (y0 + y1) / 2, dx = mx - cx, dy = my - cy, l = Mth.sqrt(dx * dx + dy * dy);
                if (l > 1) for (int h = 0; h < 2; h++) {
                    float f = 0.3f + 0.4f * h;
                    float hx = x0 + (x1 - x0) * f, hy = y0 + (y1 - y0) * f;
                    line(hx, hy, hx + dx / l * 3.2f, hy + dy / l * 3.2f, 0.6f, INK, 0.55f);
                }
            }
        }
        for (int[] s : shapes) {
            switch (s[0]) {
                case MapShapes.SAT -> {
                    float cx = sx(s[1]), cy = sy(s[2]), r = Math.max(3, s[3] * k);
                    disc(cx, cy, r, 0x7A5A30, 0.13f);
                    ring(cx, cy, r, 1.1f, INK, 0.85f);
                    for (int h = 0; h < 10; h++) {
                        float a = h * Mth.TWO_PI / 10;
                        line(cx + Mth.cos(a) * r, cy + Mth.sin(a) * r, cx + Mth.cos(a) * (r + 2.4f), cy + Mth.sin(a) * (r + 2.4f), 0.5f, INK, 0.5f);
                    }
                }
                case MapShapes.FALL -> {
                    float cx = sx(s[1]), cy = sy(s[2]), dx = s[1], dy = s[2], l = Mth.sqrt(dx * dx + dy * dy);
                    if (l < 1) break;
                    float ux = dx / l, uy = dy / l;
                    for (int w = -1; w <= 1; w++) {
                        float bx = cx - uy * w * 1.6f, by = cy + ux * w * 1.6f;
                        for (int q = 0; q < 5; q++) {
                            float a0 = q * 2.2f, a1 = (q + 1) * 2.2f;
                            float o0 = Mth.sin(t * 6 + q + w) * 0.8f, o1 = Mth.sin(t * 6 + q + 1 + w) * 0.8f;
                            line(bx + ux * a0 - uy * o0, by + uy * a0 + ux * o0, bx + ux * a1 - uy * o1, by + uy * a1 + ux * o1, 0.7f, WATER, 0.8f - q * 0.12f);
                        }
                    }
                }
                case MapShapes.BRIDGE -> {
                    float x0 = sx(s[1]), y0 = sy(s[2]), x1 = sx(s[3]), y1 = sy(s[4]);
                    float dx = x1 - x0, dy = y1 - y0, l = Mth.sqrt(dx * dx + dy * dy);
                    if (l < 1) break;
                    float nx = -dy / l * 1.1f, ny = dx / l * 1.1f;
                    line(x0 + nx, y0 + ny, x1 + nx, y1 + ny, 0.6f, INK, 0.85f);
                    line(x0 - nx, y0 - ny, x1 - nx, y1 - ny, 0.6f, INK, 0.85f);
                }
                case MapShapes.TREE -> {
                    float cx = sx(s[1]), cy = sy(s[2]);
                    ring(cx, cy, 1.4f, 0.5f, GREEN, 0.55f);
                    rect(cx - 0.3f, cy - 0.3f, cx + 0.3f, cy + 0.3f, GREEN, 0.7f);
                }
                case MapShapes.AREA -> {
                    float x0 = sx(s[1]), y0 = sy(s[2]), x1 = sx(s[3]), y1 = sy(s[4]);
                    switch (s[5]) {
                        case 1 -> { // cementerio: crucecitas
                            box(x0, y0, x1, y1, 0.6f, INK, 0.6f);
                            for (float x = x0 + 3; x < x1 - 2; x += 4)
                                for (float y = y0 + 3; y < y1 - 2; y += 4.5f) {
                                    line(x, y - 1.2f, x, y + 1.2f, 0.5f, INK, 0.7f);
                                    line(x - 0.8f, y - 0.4f, x + 0.8f, y - 0.4f, 0.5f, INK, 0.7f);
                                }
                        }
                        case 2 -> { // laberinto
                            box(x0, y0, x1, y1, 0.8f, GREEN, 0.75f);
                            for (float y = y0 + 2; y < y1 - 1; y += 2.2f) {
                                float off = ((int) ((y - y0) / 2.2f) % 2 == 0) ? 0 : (x1 - x0) * 0.3f;
                                line(x0 + 1 + off, y, x1 - 1 - (x1 - x0) * 0.3f + off, y, 0.5f, GREEN, 0.6f);
                            }
                        }
                        case 3 -> dashedBox(x0, y0, x1, y1);
                        default -> {
                            box(x0, y0, x1, y1, 0.5f, GREEN, 0.55f);
                            for (float y = y0 + 1.5f; y < y1 - 0.5f; y += 1.8f) dashed(x0 + 1, y, x1 - 1, y, 0.4f, 1.2f, GREEN, 0.5f);
                        }
                    }
                }
                default -> {
                }
            }
        }
        // camino principal
        for (int[] s : shapes)
            if (s[0] == MapShapes.ROAD) {
                float w = Math.max(1.2f, s[5] * k);
                dashed(sx(s[1]) - w, sy(s[2]), sx(s[3]) - w, sy(s[4]), 0.5f, 2.2f, INK, 0.6f);
                dashed(sx(s[1]) + w, sy(s[2]), sx(s[3]) + w, sy(s[4]), 0.5f, 2.2f, INK, 0.6f);
            }
        // murallas: trazo doble y grueso
        for (int[] s : shapes)
            if (s[0] == MapShapes.WALL) {
                int n = s[1];
                for (int i = 0; i < n; i++) {
                    int j = (i + 1) % n;
                    float x0 = sx(s[2 + i * 2]), y0 = sy(s[3 + i * 2]), x1 = sx(s[2 + j * 2]), y1 = sy(s[3 + j * 2]);
                    line(x0, y0, x1, y1, 2.6f, INK, 0.9f);
                    line(x0, y0, x1, y1, 1.0f, 0xD8C090, 0.8f);
                }
            }
        // edificios
        for (int[] s : shapes)
            if (s[0] == MapShapes.BUILDING) {
                float x0 = sx(s[1]), y0 = sy(s[2]), x1 = sx(s[3] + 1), y1 = sy(s[4] + 1);
                float fill = s[5] == 2 ? 0.42f : (s[5] == 1 ? 0.33f : 0.26f);
                rect(x0, y0, x1, y1, INK, fill);
                box(x0, y0, x1, y1, s[5] == 2 ? 1.3f : 0.7f, INK, 0.9f);
            }
        // catedral: naves, abside y una cruz
        for (int[] s : shapes)
            if (s[0] == MapShapes.CATHEDRAL) {
                float cx = sx(s[1]), yN = sy(s[2]), yS = sy(s[3]);
                rect(cx - 24 * k, yN, cx + 24 * k, yS, INK, 0.2f);
                rect(cx - 11 * k, yN, cx + 11 * k, yS, INK, 0.28f);
                box(cx - 24 * k, yN, cx + 24 * k, yS, 0.9f, INK, 0.9f);
                for (int i = 0; i < 12; i++) {
                    float a0 = Mth.PI + i * Mth.PI / 12, a1 = Mth.PI + (i + 1) * Mth.PI / 12;
                    tri(cx, yN, cx + Mth.cos(a0) * 11 * k, yN + Mth.sin(a0) * 11 * k, cx + Mth.cos(a1) * 11 * k, yN + Mth.sin(a1) * 11 * k, INK, 0.3f);
                }
                float my = (yN + yS) / 2;
                line(cx, my - 5, cx, my + 6, 1.4f, 0xE8D8B0, 0.9f);
                line(cx - 3.5f, my - 2, cx + 3.5f, my - 2, 1.4f, 0xE8D8B0, 0.9f);
            }
        // fuentes
        for (int[] s : shapes)
            if (s[0] == MapShapes.FOUNTAIN) {
                float cx = sx(s[1]), cy = sy(s[2]), r = Math.max(1.6f, s[3] * k);
                disc(cx, cy, r, WATER, 0.45f);
                ring(cx, cy, r, 0.8f, INK, 0.9f);
            }
        // torres
        for (int[] s : shapes)
            if (s[0] == MapShapes.TOWER) {
                float cx = sx(s[1]), cy = sy(s[2]), r = Math.max(1.8f, s[3] * k);
                disc(cx, cy, r, INK, 0.8f);
                disc(cx, cy, r * 0.45f, 0xC8A870, 0.6f);
            }
        // el Arbol de la Corona
        for (int[] s : shapes)
            if (s[0] == MapShapes.TREE_TOWER) {
                float cx = sx(s[1]), cy = sy(s[2]), r = Math.max(5, s[3] * k);
                disc(cx, cy, r * 1.15f, GREEN, 0.35f);
                for (int i = 0; i < 12; i++) {
                    float a = i * Mth.TWO_PI / 12 + 0.2f * Mth.sin(i * 3.1f);
                    float l = r * (1.3f + 0.25f * Mth.sin(i * 1.7f));
                    line(cx, cy, cx + Mth.cos(a) * l, cy + Mth.sin(a) * l, 0.8f, GREEN, 0.85f);
                    disc(cx + Mth.cos(a) * l, cy + Mth.sin(a) * l, 1.3f, GREEN, 0.7f);
                }
                ring(cx, cy, r * 0.5f, 1.1f, INK, 0.9f);
                disc(cx, cy, r * 0.25f, INK, 0.85f);
            }
        // rosa de los vientos
        float rx = mapL + mapS - 14, ry = mapT + 12, rr = 9;
        for (int i = 0; i < 8; i++) {
            float a = i * Mth.PI / 4 - Mth.HALF_PI, l = i % 2 == 0 ? rr : rr * 0.55f;
            float a1 = a + 0.35f, a2 = a - 0.35f;
            tri(rx, ry, rx + Mth.cos(a) * l, ry + Mth.sin(a) * l, rx + Mth.cos(a1) * l * 0.3f, ry + Mth.sin(a1) * l * 0.3f, INK, i == 0 ? 0.95f : 0.7f);
            tri(rx, ry, rx + Mth.cos(a) * l, ry + Mth.sin(a) * l, rx + Mth.cos(a2) * l * 0.3f, ry + Mth.sin(a2) * l * 0.3f, i == 0 ? RED : INK, 0.5f);
        }
        ring(rx, ry, rr * 0.75f, 0.5f, INK, 0.6f);
        end();
        text(g, "N", rx, ry - rr - 7, 0.8f, INK, 1f, true);
    }

    private void dashedBox(float x0, float y0, float x1, float y1) {
        dashed(x0, y0, x1, y0, 0.6f, 1.6f, INK, 0.6f);
        dashed(x1, y0, x1, y1, 0.6f, 1.6f, INK, 0.6f);
        dashed(x1, y1, x0, y1, 0.6f, 1.6f, INK, 0.6f);
        dashed(x0, y1, x0, y0, 0.6f, 1.6f, INK, 0.6f);
    }

    private void text(GuiGraphics g, String s, float cx, float y, float scale, int rgb, float a, boolean center) {
        if (a * 255 < 4) return;
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(cx, y, 0);
        pose.scale(scale, scale, 1);
        int x = center ? -font.width(s) / 2 : 0;
        g.drawString(font, s, x, 0, (Math.round(a * 255) << 24) | rgb, false);
        pose.popPose();
    }

    private void text(GuiGraphics g, Component s, float x, float y, float scale, int argb) {
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(scale, scale, 1);
        g.drawString(font, s, 0, 0, argb, false);
        pose.popPose();
    }

    private void drawLabels(GuiGraphics g) {
        String[] labels = d.labels();
        int[] pos = d.labelPos();
        float sc = Mth.clamp(mapS / 520f, 0.5f, 0.85f);
        for (int i = 0; i < labels.length && i * 2 + 1 < pos.length; i++) {
            String s = I18n.get("darkcraft.mapa.lugar." + labels[i]);
            float x = sx(pos[i * 2]), y = sy(pos[i * 2 + 1]);
            if (x < mapL - 20 || x > mapL + mapS + 20 || y < mapT - 10 || y > mapT + mapS + 10) continue;
            text(g, s, x + 0.5f, y + 0.5f, sc, 0xE8D4A8, 0.8f, true);
            text(g, s, x, y, sc, INK, 1f, true);
        }
    }

    private float[] chapterXY(int c) {
        int[] ch = d.chapters();
        return new float[]{sx(ch[(c - 1) * 2]), sy(ch[(c - 1) * 2 + 1])};
    }

    private void drawMarkers(GuiGraphics g, float t) {
        // capitulos: leidos (tachados), siguiente (calavera que late y brilla) y futuros
        begin(g);
        for (int c = 1; c <= 5; c++) {
            float[] p = chapterXY(c);
            boolean read = (d.read() & (1 << c)) != 0;
            if (c == next) continue;
            if (read) {
                disc(p[0], p[1], 4.2f, 0xE0C898, 0.9f);
                ring(p[0], p[1], 4.2f, 0.8f, INK, 0.9f);
            } else {
                ring(p[0], p[1], 3.6f, 0.6f, INK, 0.35f);
            }
        }
        if (next <= 5) {
            float[] p = chapterXY(next);
            // ondas que se abren desde la calavera
            for (int w = 0; w < 2; w++) {
                float ph = ((t * 0.8f) + w * 0.5f) % 1f;
                ring(p[0], p[1], 6 + ph * 16, 1.1f, RED, 0.55f * (1 - ph));
            }
        }
        end();
        for (int c = 1; c <= 5; c++) {
            if (c == next) continue;
            float[] p = chapterXY(c);
            boolean read = (d.read() & (1 << c)) != 0;
            text(g, String.valueOf(c), p[0] + 0.5f, p[1] - 3.2f, 0.75f, INK, read ? 1f : 0.4f, true);
            if (read) {
                begin(g);
                line(p[0] - 4.5f, p[1] - 4.5f, p[0] + 4.5f, p[1] + 4.5f, 1.3f, RED, 0.9f);
                line(p[0] - 4.5f, p[1] + 4.5f, p[0] + 4.5f, p[1] - 4.5f, 1.3f, RED, 0.9f);
                end();
            }
        }
        if (next <= 5) {
            float[] p = chapterXY(next);
            float beat = 0.5f + 0.5f * Mth.sin(t * 5.2f);
            float pulse = 1 + 0.18f * beat;
            // brillo rojo detras
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA, com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
            RenderSystem.setShaderColor(1, 1, 1, 0.55f + 0.4f * beat);
            int gs = Math.round(30 * pulse);
            g.blit(GLOW, Math.round(p[0] - gs / 2f), Math.round(p[1] - gs / 2f), gs, gs, 0, 0, 64, 64, 64, 64);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.defaultBlendFunc();
            // la calavera
            PoseStack pose = g.pose();
            pose.pushPose();
            pose.translate(p[0], p[1], 0);
            pose.scale(pulse * 0.95f, pulse * 0.95f, 1);
            g.blit(SKULL, -8, -8, 0, 0, 16, 16, 16, 16);
            pose.popPose();
            RenderSystem.disableBlend();
        }
    }

    private void drawPlayer(GuiGraphics g, float t, float pt) {
        var pl = Minecraft.getInstance().player;
        if (pl == null) return;
        float bx = (float) (Mth.lerp(pt, pl.xo, pl.getX()) - d.cx()), bz = (float) (Mth.lerp(pt, pl.zo, pl.getZ()) - d.cz());
        bx = Mth.clamp(bx, -RANGE, RANGE);
        bz = Mth.clamp(bz, -RANGE, RANGE);
        float x = sx(bx), y = sy(bz);
        float yaw = pl.getViewYRot(pt) * Mth.DEG_TO_RAD;
        float fx = -Mth.sin(yaw), fy = Mth.cos(yaw);
        float rx = -fy, ry = fx;
        begin(g);
        float s = 6.5f;
        tri(x + fx * s * 1.15f, y + fy * s * 1.15f, x - fx * s * 0.6f + rx * s * 0.7f, y - fy * s * 0.6f + ry * s * 0.7f,
                x - fx * s * 0.6f - rx * s * 0.7f, y - fy * s * 0.6f - ry * s * 0.7f, INK, 0.9f);
        s = 4.8f;
        tri(x + fx * s * 1.1f, y + fy * s * 1.1f, x - fx * s * 0.45f + rx * s * 0.6f, y - fy * s * 0.45f + ry * s * 0.6f,
                x - fx * s * 0.45f - rx * s * 0.6f, y - fy * s * 0.45f - ry * s * 0.6f, 0xE02020, 1f);
        end();
        text(g, I18n.get("darkcraft.mapa.tu"), x, y + 6, 0.6f, 0x7A0A0A, 1f, true);
    }

    private void drawTitle(GuiGraphics g) {
        String title = I18n.get("darkcraft.mapa.titulo");
        float sc = Mth.clamp(ph / 190f, 1.1f, 2.0f);
        float y = py0 + ph * 0.045f;
        float w = font.width(title) * sc;
        float cx = px0 + pw / 2f;
        text(g, title, cx + 0.8f, y + 0.8f, sc, 0xC8A878, 1f, true);
        text(g, title, cx, y, sc, INK, 1f, true);
        begin(g);
        float ly = y + 9 * sc + 2;
        line(cx - w / 2 - 6, ly, cx + w / 2 + 6, ly, 0.8f, INK, 0.8f);
        line(cx - w / 2 - 20, y + 4.5f * sc, cx - w / 2 - 6, y + 4.5f * sc, 0.8f, INK, 0.7f);
        line(cx + w / 2 + 6, y + 4.5f * sc, cx + w / 2 + 20, y + 4.5f * sc, 0.8f, INK, 0.7f);
        end();
    }

    /** Columna derecha: los capitulos de la cronica y la pista de la siguiente pagina. */
    private void drawChronicle(GuiGraphics g, float t) {
        float x = mapL + mapS + pw * 0.035f, right = px0 + pw * 0.95f, colW = right - x;
        float y = mapT + 4;
        float sc = Mth.clamp(colW / 150f, 0.7f, 1.0f);
        text(g, Component.translatable("darkcraft.mapa.cronica").withStyle(ChatFormatting.BOLD), x, y, sc * 1.25f, 0xFF000000 | INK);
        y += 16 * sc;
        begin(g);
        line(x, y - 3, right - 4, y - 3, 0.7f, INK, 0.7f);
        end();
        for (int c = 1; c <= 5; c++) {
            boolean read = (d.read() & (1 << c)) != 0;
            MutableComponent line;
            int col;
            if (read || c == next) {
                line = Component.literal(c + ". ").append(Component.translatable("darkcraft.cap." + c + ".titulo"));
                if (read) {
                    line = line.withStyle(ChatFormatting.STRIKETHROUGH);
                    col = 0xFF000000 | 0x5A4030;
                } else {
                    line = line.withStyle(ChatFormatting.BOLD);
                    col = 0xFF000000 | RED;
                }
            } else {
                line = Component.literal(c + ". ???");
                col = 0x99000000 | INK;
            }
            List<FormattedCharSequence> parts = font.split(line, (int) ((colW - 12) / sc));
            float lx = x + (c == next ? 11 : 0);
            for (FormattedCharSequence p : parts) {
                PoseStack pose = g.pose();
                pose.pushPose();
                pose.translate(lx, y, 0);
                pose.scale(sc, sc, 1);
                g.drawString(font, p, 0, 0, col, false);
                pose.popPose();
                y += 10 * sc;
            }
            if (c == next) {
                float beat = 1 + 0.12f * Mth.sin(t * 5.2f);
                PoseStack pose = g.pose();
                pose.pushPose();
                pose.translate(x + 4, y - 10 * sc * parts.size() + 3.5f * sc, 0);
                pose.scale(0.55f * beat, 0.55f * beat, 1);
                RenderSystem.enableBlend();
                g.blit(SKULL, -8, -8, 0, 0, 16, 16, 16, 16);
                RenderSystem.disableBlend();
                pose.popPose();
            }
            y += 3 * sc;
        }
        // pista de la siguiente pagina
        y += 8 * sc;
        begin(g);
        line(x, y - 4, right - 4, y - 4, 0.7f, INK, 0.7f);
        end();
        if (next <= 5) {
            text(g, Component.translatable("darkcraft.mapa.siguiente").withStyle(ChatFormatting.BOLD), x, y, sc, 0xFF000000 | RED);
            y += 12 * sc;
        }
        Component hint = Component.translatable("darkcraft.pista." + next).withStyle(ChatFormatting.ITALIC);
        for (FormattedCharSequence p : font.split(hint, (int) ((colW - 4) / sc))) {
            PoseStack pose = g.pose();
            pose.pushPose();
            pose.translate(x, y, 0);
            pose.scale(sc, sc, 1);
            g.drawString(font, p, 0, 0, 0xFF000000 | 0x4A1A0A, false);
            pose.popPose();
            y += 10 * sc;
        }
        String close = I18n.get("darkcraft.mapa.cerrar");
        text(g, close, px0 + pw * 0.95f - font.width(close) * 0.6f, py0 + ph * 0.93f, 0.6f, INK, 0.55f, false);
    }

    private void tooltip(GuiGraphics g, int mx, int my) {
        for (int c = 1; c <= 5; c++) {
            float[] p = chapterXY(c);
            if (Math.abs(mx - p[0]) > 8 || Math.abs(my - p[1]) > 8) continue;
            boolean known = (d.read() & (1 << c)) != 0 || c == next;
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("darkcraft.capitulo", c).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
            lines.add(known ? Component.translatable("darkcraft.cap." + c + ".titulo").withStyle(ChatFormatting.GOLD) : Component.literal("???").withStyle(ChatFormatting.GRAY));
            if (c == next) lines.add(Component.translatable("darkcraft.pista." + c).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            g.renderComponentTooltip(font, lines, mx, my);
            return;
        }
    }
}
