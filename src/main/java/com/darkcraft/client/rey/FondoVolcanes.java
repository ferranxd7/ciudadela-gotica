package com.darkcraft.client.rey;

import com.darkcraft.rey.Volcan;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

import java.util.Random;

/**
 * El horizonte de la dimension de los volcanes: un anillo de volcanes kilometricos dibujados en el cielo (estan "en el
 * infinito": no se acercan ni se alejan), con rios de lava que bajan desde los crateres, columnas de humo, erupciones
 * y meteoritos que salen disparados y caen a lo lejos. Debajo, el resplandor del mar de lava.
 */
public final class FondoVolcanes {
    private FondoVolcanes() {}

    /** Todo se dibuja a escala pequena (dentro del plano lejano de la camara); lo que cuenta es el angulo. */
    private static final float S = 0.34f;
    private static final int N = 56, M = 14;
    private static float[][][] malla;   // [volcan][vertice][x, y, z, r, g, b]

    private static float ruido(int v, double a) {
        double s = 0;
        for (int k = 1; k <= 4; k++) s += Math.sin(a * (2 + k * 1.7) + v * 3.1 + k * 1.3) / k;
        return (float) (s / 2.1);
    }

    /** Angulos (radianes, respecto al volcan) de los rios de lava de cada volcan. */
    private static double[] rios(int v) {
        Random r = new Random(500 + v);
        int n = 3 + r.nextInt(3);
        double[] a = new double[n];
        for (int i = 0; i < n; i++) a[i] = r.nextDouble() * Math.PI * 2;
        return a;
    }

    private static void construir() {
        float[][] L = Volcan.LEJANOS;
        malla = new float[L.length][][];
        for (int v = 0; v < L.length; v++) {
            double ang = Math.toRadians(L[v][0]);
            double cx = Math.cos(ang) * L[v][1], cz = Math.sin(ang) * L[v][1];
            double h = L[v][2], rb = L[v][3], base = -0.06 * L[v][1];
            double[] rs = rios(v);
            float[][] vs = new float[(M + 1) * (N + 1)][];
            for (int j = 0; j <= M; j++) {
                double t = j / (double) M;
                double rr = rb * Math.pow(1 - t, 1.35) * (1 - 0.15 * t) + rb * 0.1;
                double y = base + t * (h - base);
                for (int i = 0; i <= N; i++) {
                    double phi = i * Math.PI * 2 / N;
                    double rad = rr * (1 + 0.13 * ruido(v, phi * 3) * (1 - t * 0.6));
                    double x = cx + Math.cos(phi) * rad, z = cz + Math.sin(phi) * rad;
                    // roca: muy oscura, con el resplandor rojo del mar de lava abajo
                    double low = Math.pow(1 - t, 2.2);
                    float r = (float) (0.06 + 0.30 * low), g = (float) (0.045 + 0.06 * low), b = (float) (0.045 + 0.01 * low);
                    // vetas mas claras en las crestas
                    double cresta = Math.max(0, ruido(v + 7, phi * 5));
                    r += 0.04 * cresta;
                    g += 0.03 * cresta;
                    b += 0.03 * cresta;
                    // rios de lava: bajan serpenteando desde el crater hasta casi el pie
                    for (double ra : rs) {
                        double centro = ra + 0.18 * Math.sin(t * 7 + ra * 3);
                        double d = Math.abs(Math.atan2(Math.sin(phi - centro), Math.cos(phi - centro)));
                        double ancho = 0.05 + 0.06 * (1 - t);
                        if (d < ancho && t > 0.08) {
                            double k = (1 - d / ancho) * Math.min(1, (t - 0.08) * 6);
                            r = (float) (r + (1.0 - r) * k);
                            g = (float) (g + (0.42 + 0.2 * t - g) * k);
                            b = (float) (b + (0.06 - b) * k);
                        }
                    }
                    // el borde del crater brilla
                    if (t > 0.9) {
                        double k = (t - 0.9) / 0.1;
                        r = (float) (r + (1.0 - r) * k * 0.85);
                        g = (float) (g + (0.55 - g) * k * 0.85);
                        b = (float) (b + (0.12 - b) * k * 0.85);
                    }
                    vs[j * (N + 1) + i] = new float[]{(float) x * S, (float) y * S, (float) z * S, r, g, b};
                }
            }
            malla[v] = vs;
        }
    }

    public static void dibujar(PoseStack ps, float tiempo) {
        if (malla == null) construir();
        Matrix4f m = ps.last().pose();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tes = Tesselator.getInstance();
        BufferBuilder bb = tes.getBuilder();

        // 1) resplandor del mar de lava en el horizonte
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int n = 64;
        float rad = 110 * S * 3, y0 = -16 * S * 3, y1 = 6 * S * 3, y2 = 40 * S * 3;
        for (int i = 0; i < n; i++) {
            double a0 = i * Math.PI * 2 / n, a1 = (i + 1) * Math.PI * 2 / n;
            float x0 = (float) Math.cos(a0) * rad, z0 = (float) Math.sin(a0) * rad, x1 = (float) Math.cos(a1) * rad, z1 = (float) Math.sin(a1) * rad;
            bb.vertex(m, x0, y0, z0).color(1f, 0.35f, 0.05f, 0.55f).endVertex();
            bb.vertex(m, x1, y0, z1).color(1f, 0.35f, 0.05f, 0.55f).endVertex();
            bb.vertex(m, x1, y1, z1).color(0.9f, 0.18f, 0.03f, 0.25f).endVertex();
            bb.vertex(m, x0, y1, z0).color(0.9f, 0.18f, 0.03f, 0.25f).endVertex();
            bb.vertex(m, x0, y1, z0).color(0.9f, 0.18f, 0.03f, 0.25f).endVertex();
            bb.vertex(m, x1, y1, z1).color(0.9f, 0.18f, 0.03f, 0.25f).endVertex();
            bb.vertex(m, x1, y2, z1).color(0.5f, 0.05f, 0.02f, 0f).endVertex();
            bb.vertex(m, x0, y2, z0).color(0.5f, 0.05f, 0.02f, 0f).endVertex();
        }
        tes.end();

        // 2) los volcanes (opacos)
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (float[][] vs : malla) {
            for (int j = 0; j < M; j++)
                for (int i = 0; i < N; i++) {
                    v(bb, m, vs[j * (N + 1) + i], 1f);
                    v(bb, m, vs[j * (N + 1) + i + 1], 1f);
                    v(bb, m, vs[(j + 1) * (N + 1) + i + 1], 1f);
                    v(bb, m, vs[(j + 1) * (N + 1) + i], 1f);
                }
            // tapa del crater: lago de lava
            float[] c = vs[M * (N + 1)];
            float cx = 0, cy = 0, cz = 0;
            for (int i = 0; i < N; i++) {
                cx += vs[M * (N + 1) + i][0];
                cy += vs[M * (N + 1) + i][1];
                cz += vs[M * (N + 1) + i][2];
            }
            cx /= N;
            cy /= N;
            cz /= N;
            float pul = 0.85f + 0.15f * (float) Math.sin(tiempo * 0.07);
            for (int i = 0; i < N; i++) {
                float[] a = vs[M * (N + 1) + i], b = vs[M * (N + 1) + i + 1];
                bb.vertex(m, cx, cy - 0.4f, cz).color(1f, 0.85f * pul, 0.3f, 1f).endVertex();
                bb.vertex(m, cx, cy - 0.4f, cz).color(1f, 0.85f * pul, 0.3f, 1f).endVertex();
                bb.vertex(m, b[0], b[1], b[2]).color(1f, 0.5f, 0.1f, 1f).endVertex();
                bb.vertex(m, a[0], a[1], a[2]).color(1f, 0.5f, 0.1f, 1f).endVertex();
            }
        }
        tes.end();

        // 3) humo, erupciones y meteoritos
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float[][] L = Volcan.LEJANOS;
        for (int v = 0; v < L.length; v++) humo(bb, m, v, L[v], tiempo);
        tes.end();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int v = 0; v < L.length; v++) erupcion(bb, m, v, L[v], tiempo);
        tes.end();

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
    }

    private static void v(BufferBuilder bb, Matrix4f m, float[] p, float a) {
        bb.vertex(m, p[0], p[1], p[2]).color(p[3], p[4], p[5], a).endVertex();
    }

    /**
     * Mancha redonda y suave mirando hacia el centro (la camara): opaca en el centro y transparente en el borde,
     * con el color de arriba (cr..) y el de abajo (cr2..) mezclados por la altura.
     */
    private static void bill(BufferBuilder bb, Matrix4f m, double ang, double x, double y, double z, double r,
                             float cr, float cg, float cb, float ca, float cr2, float cg2, float cb2, float ca2) {
        float rx = (float) (-Math.sin(ang)), rz = (float) (Math.cos(ang));
        float fx = (float) x, fy = (float) y, fz = (float) z;
        float a0 = Math.max(ca, ca2);
        if (a0 <= 0.003f) return;
        float mr = (cr + cr2) / 2, mg = (cg + cg2) / 2, mb = (cb + cb2) / 2;
        int n = r > 2 ? 14 : 8;
        for (int i = 0; i < n; i++) {
            double t0 = i * Math.PI * 2 / n, t1 = (i + 1) * Math.PI * 2 / n;
            float s0 = (float) Math.sin(t0), s1 = (float) Math.sin(t1);
            float h0 = (float) (Math.cos(t0) * r), h1 = (float) (Math.cos(t1) * r), v0 = (float) (s0 * r), v1 = (float) (s1 * r);
            float k0 = (s0 + 1) / 2, k1 = (s1 + 1) / 2;
            bb.vertex(m, fx, fy, fz).color(mr, mg, mb, a0).endVertex();
            bb.vertex(m, fx, fy, fz).color(mr, mg, mb, a0).endVertex();
            bb.vertex(m, fx + rx * h1, fy + v1, fz + rz * h1).color(cr2 + (cr - cr2) * k1, cg2 + (cg - cg2) * k1, cb2 + (cb - cb2) * k1, 0f).endVertex();
            bb.vertex(m, fx + rx * h0, fy + v0, fz + rz * h0).color(cr2 + (cr - cr2) * k0, cg2 + (cg - cg2) * k0, cb2 + (cb - cb2) * k0, 0f).endVertex();
        }
    }

    private static double[] crater(float[] L) {
        double ang = Math.toRadians(L[0]);
        return new double[]{ang, Math.cos(ang) * L[1] * S, L[2] * S, Math.sin(ang) * L[1] * S};
    }

    private static void humo(BufferBuilder bb, Matrix4f m, int v, float[] L, float tiempo) {
        double[] c = crater(L);
        int puffs = 14;
        for (int k = 0; k < puffs; k++) {
            // cada bocanada sube, se agranda y se desvanece; empiezan desfasadas
            double ciclo = 900;
            double f = ((tiempo + k * ciclo / puffs + v * 137) % ciclo) / ciclo;
            double y = c[2] + f * L[2] * S * 0.9;
            double deriva = f * f * L[3] * S * 0.5;
            double x = c[1] + Math.cos(c[0] + 1.3) * deriva, z = c[3] + Math.sin(c[0] + 1.3) * deriva;
            double r = L[3] * S * (0.17 + f * 0.62);
            float a = (float) (Math.sin(f * Math.PI) * 0.72);
            float lit = (float) Math.max(0, 1 - f * 3);
            bill(bb, m, c[0], x, y, z, r, 0.1f + 0.25f * lit, 0.08f + 0.06f * lit, 0.08f, a,
                    0.14f + 0.5f * lit, 0.09f + 0.12f * lit, 0.07f, a);
        }
    }

    private static void erupcion(BufferBuilder bb, Matrix4f m, int v, float[] L, float tiempo) {
        double[] c = crater(L);
        // resplandor sobre el crater
        float pul = 0.7f + 0.3f * (float) Math.sin(tiempo * 0.05 + v);
        bill(bb, m, c[0], c[1], c[2] + L[3] * S * 0.05, c[3], L[3] * S * 0.35, 1f, 0.45f, 0.08f, 0f, 1f, 0.5f, 0.1f, 0.5f * pul);
        // erupciones: cada cierto tiempo salen gotas de lava en arco
        double periodo = 110 + v * 23;
        int b = (int) Math.floor((tiempo + v * 61) / periodo);
        double edad = ((tiempo + v * 61) % periodo);
        Random r = new Random(b * 31L + v * 7919L);
        int gotas = 14;
        double g = 0.0045 * L[2] * S;
        for (int i = 0; i < gotas; i++) {
            double va = r.nextDouble() * Math.PI * 2, vh = (0.25 + r.nextDouble() * 0.5) * L[3] * S / 40.0, vy = (0.6 + r.nextDouble() * 0.6) * L[2] * S / 20.0;
            double life = 55 + r.nextDouble() * 25;
            if (edad > life) continue;
            for (int k = 0; k < 3; k++) {
                double tt = Math.max(0, edad - k * 2);
                double x = c[1] + Math.cos(va) * vh * tt, z = c[3] + Math.sin(va) * vh * tt, y = c[2] + vy * tt - g * tt * tt;
                float a = (float) ((1 - edad / life) * (1 - k * 0.3));
                double sz = L[3] * S * (0.022 - k * 0.005);
                bill(bb, m, c[0], x, y, z, sz, 1f, 0.75f, 0.3f, a, 1f, 0.45f, 0.08f, a);
            }
        }
        // meteorito: de vez en cuando una roca ardiendo sale disparada hacia la plataforma y cae a lo lejos
        double pm = 260 + v * 37;
        int bm = (int) Math.floor((tiempo + v * 97) / pm);
        double em = (tiempo + v * 97) % pm;
        Random rm = new Random(bm * 131L + v * 17L);
        double life = 70 + rm.nextDouble() * 30;
        if (em < life) {
            double k = em / life;
            // del crater hacia el centro (la camara) y abajo, sin llegar
            double ex = c[1] * (0.45 + rm.nextDouble() * 0.15), ez = c[3] * (0.45 + rm.nextDouble() * 0.15), ey = -4 * S;
            double lado = (rm.nextDouble() - 0.5) * L[3] * S;
            ex += -Math.sin(c[0]) * lado;
            ez += Math.cos(c[0]) * lado;
            for (int s = 0; s < 6; s++) {
                double kk = Math.max(0, k - s * 0.025);
                double x = c[1] + (ex - c[1]) * kk, z = c[3] + (ez - c[3]) * kk;
                double y = c[2] + (ey - c[2]) * kk + Math.sin(kk * Math.PI) * L[2] * S * 0.35;
                float a = (float) (1 - s / 6.0) * (float) Math.min(1, (1 - k) * 4);
                double sz = L[3] * S * (0.05 - s * 0.006);
                bill(bb, m, c[0], x, y, z, sz, 1f, 0.8f, 0.4f, a, 1f, 0.35f, 0.05f, a);
            }
        }
    }
}
