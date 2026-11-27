package com.darkcraft.client.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.cine.Desenlace;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * El cielo de Morvain despues del final (parte 7ad; lo manda {@code morvain/Epilogo} con {@code Net.Final}). Con el alba (se golpeo
 * el corazon de la Sombra) el ocaso dorado y rojo de siempre se vuelve un amanecer: un velo azul palido arriba y rosado en el
 * horizonte, la niebla calida y clara, el resplandor del sol rosa y dorado y la luz del cielo algo mas viva. Con la nueva Sombra
 * (se tomo) se queda gris y frio: velo y niebla grises, sin resplandor, la luz sin color. Cambia poco a poco desde {@code desde}
 * ({@link Desenlace#FUNDIDO} segundos; asi se ve cambiar en la cinematica del final). Como el cielo rojo del mundo de las nubes,
 * el velo es una caja sin profundidad alrededor de la camara justo despues de dibujar el cielo, y con shaders no se dibuja (queda
 * la niebla). Los fuegos de los santuarios arden frios con la nueva Sombra ({@link #frio}, en todas partes).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class CieloFinal {
    private CieloFinal() {}

    private static int cual;
    private static long desde = -1;

    /** Del servidor: como acabo (0 aun no, 1 el alba, 2 la nueva Sombra) y desde que tick cambia el cielo (-1: ya). */
    public static void recibir(int c, long d) {
        cual = c;
        desde = d;
    }

    public static int cual() {
        return cual;
    }

    /** Si los fuegos de los santuarios arden frios (se tomo el corazon de la Sombra). */
    public static boolean frio() {
        return cual == 2;
    }

    /** Lo que ha cambiado ya (0-1), con el fundido. */
    public static float k(float pt) {
        Minecraft mc = Minecraft.getInstance();
        if (cual == 0 || mc.level == null) return 0f;
        if (desde < 0) return 1f;
        float t = (float) ((mc.level.getGameTime() + pt - desde) / (Desenlace.FUNDIDO * 20f));
        if (t <= 0f) return desde > mc.level.getGameTime() + 2400 ? 1f : 0f;
        if (t >= 1f) return 1f;
        return t * t * (3 - 2 * t);
    }

    /** Lo mismo, solo en Morvain (el cielo de las otras dimensiones no cambia). */
    public static float cielo(float pt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.dimension() != com.darkcraft.morvain.Morvain.NIVEL) return 0f;
        return k(pt);
    }

    /** Para las pruebas. */
    public static String resumen() {
        return "final " + (cual == 0 ? "ninguno" : cual == 1 ? "alba" : "sombra") + ", desde " + desde + String.format(java.util.Locale.ROOT, ", cambiado %.2f (en Morvain %.2f)", k(1f), cielo(1f));
    }

    // ------------------------------------------------------------------ lo que piden los efectos de Morvain (MorvainCliente.Efectos)

    /** El resplandor del sol en el horizonte: rosa y dorado con el alba; con la nueva Sombra se apaga y pierde el color. */
    public static float[] ocaso(float[] c, float pt) {
        if (c == null) return null;
        float k = cielo(pt);
        if (k <= 0f) return c;
        if (cual == 1) {
            c[0] = Mth.lerp(k, c[0], 1.0f);
            c[1] = Mth.lerp(k, c[1], 0.66f);
            c[2] = Mth.lerp(k, c[2], 0.62f);
            c[3] = Math.min(1f, c[3] * (1f + 0.2f * k));
        } else {
            float g = (c[0] + c[1] + c[2]) / 3f;
            c[0] = Mth.lerp(k, c[0], g * 0.95f);
            c[1] = Mth.lerp(k, c[1], g);
            c[2] = Mth.lerp(k, c[2], g * 1.08f);
            c[3] = c[3] * (1f - 0.85f * k);
        }
        return c;
    }

    /** La luz del cielo ({@code cielo} 0-15): algo mas viva y calida con el alba; sin color con la nueva Sombra. */
    public static void luz(float pt, int cielo, Vector3f c) {
        float k = cielo(pt);
        if (k <= 0f) return;
        float s = cielo / 15f * k;
        if (cual == 1) {
            c.set(Math.min(1f, c.x() * (1f + 0.16f * s) + 0.03f * s), Math.min(1f, c.y() * (1f + 0.10f * s) + 0.02f * s),
                    Math.min(1f, c.z() * (1f + 0.05f * s)));
        } else {
            float g = (c.x() + c.y() + c.z()) / 3f, m = 0.6f * s;
            c.set(Mth.lerp(m, c.x(), g * 0.95f), Mth.lerp(m, c.y(), g * 0.99f), Mth.lerp(m, c.z(), Math.min(1f, g * 1.07f)));
        }
    }

    // ------------------------------------------------------------------ la niebla

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onNiebla(ViewportEvent.ComputeFogColor e) {
        float k = cielo((float) e.getPartialTick());
        if (k <= 0f || e.getCamera().getFluidInCamera() != FogType.NONE) return;
        float r = e.getRed(), g = e.getGreen(), b = e.getBlue();
        float tr, tg, tb, m;
        if (cual == 1) {
            // un amanecer: melocoton claro
            tr = 0.86f;
            tg = 0.68f;
            tb = 0.58f;
            m = 0.78f * k;
        } else {
            float br = Math.max(0.10f, (r + g + b) / 3f);
            tr = 0.30f + br * 0.25f;
            tg = 0.32f + br * 0.25f;
            tb = 0.36f + br * 0.27f;
            m = 0.82f * k;
        }
        e.setRed(Mth.lerp(m, r, tr));
        e.setGreen(Mth.lerp(m, g, tg));
        e.setBlue(Mth.lerp(m, b, tb));
    }

    // ------------------------------------------------------------------ el velo sobre el cielo

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent e) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
        float k = cielo(e.getPartialTick());
        if (k <= 0f || com.darkcraft.client.Lejania.shadersEnUso()) return;
        Matrix4f m = e.getPoseStack().last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bb = Tesselator.getInstance().getBuilder();
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        // {r, g, b, a} arriba, en el horizonte y abajo
        float[][] c = cual == 1
                ? new float[][]{{0.50f, 0.66f, 0.90f, 0.42f}, {1.00f, 0.74f, 0.60f, 0.32f}, {0.86f, 0.62f, 0.52f, 0.26f}}
                : new float[][]{{0.40f, 0.42f, 0.47f, 0.56f}, {0.50f, 0.52f, 0.56f, 0.50f}, {0.30f, 0.32f, 0.36f, 0.46f}};
        float s = 60f;
        // el techo
        quad(bb, m, c[0], k, -s, s, -s, s, s, -s, s, s, s, -s, s, s);
        // el suelo
        quad(bb, m, c[2], k, -s, -s, s, -s, -s, -s, s, -s, -s, s, -s, s);
        // los cuatro lados, en dos mitades (de arriba al horizonte y del horizonte abajo)
        float[][] lados = {{-s, -s, s, -s}, {s, -s, s, s}, {s, s, -s, s}, {-s, s, -s, -s}};
        for (float[] l : lados) {
            lado(bb, m, l, s, 0f, c[0], c[1], k);
            lado(bb, m, l, 0f, -s, c[1], c[2], k);
        }
        BufferUploader.drawWithShader(bb.end());
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void quad(BufferBuilder bb, Matrix4f m, float[] c, float k, float... v) {
        for (int i = 0; i < 4; i++) bb.vertex(m, v[i * 3], v[i * 3 + 1], v[i * 3 + 2]).color(c[0], c[1], c[2], c[3] * k).endVertex();
    }

    /** Un lado de (x0, z0) a (x1, z1), de la altura {@code y0} (color {@code a}) a {@code y1} (color {@code b}). */
    private static void lado(BufferBuilder bb, Matrix4f m, float[] l, float y0, float y1, float[] a, float[] b, float k) {
        bb.vertex(m, l[0], y0, l[1]).color(a[0], a[1], a[2], a[3] * k).endVertex();
        bb.vertex(m, l[2], y0, l[3]).color(a[0], a[1], a[2], a[3] * k).endVertex();
        bb.vertex(m, l[2], y1, l[3]).color(b[0], b[1], b[2], b[3] * k).endVertex();
        bb.vertex(m, l[0], y1, l[1]).color(b[0], b[1], b[2], b[3] * k).endVertex();
    }
}
