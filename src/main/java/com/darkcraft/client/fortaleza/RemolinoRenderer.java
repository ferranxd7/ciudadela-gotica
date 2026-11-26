package com.darkcraft.client.fortaleza;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.fortaleza.Remolino;
import com.darkcraft.gen.TorreCupula;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * El remolino del portal: la superficie del hueco del arco (fila a fila, con el escalonado del arco de bloques), con el
 * dibujo de la espiral ({@code textures/entity/remolino.png}) girando hacia el centro en dos capas (la de detras mas
 * grande y al reves) y con luz propia. Crece desde el centro al abrirse.
 */
public class RemolinoRenderer extends EntityRenderer<Remolino> {
    private static final ResourceLocation TEX = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/remolino.png");
    /** Las nubes que se arremolinan sobre el portal en el capitulo 6 (un circulo con brazos de nube; "clamp"). */
    private static final ResourceLocation NUBES = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/nubes_remolino.png");
    /** Donde esta el ojo de la espiral (sobre el pie del hueco). */
    private static final float CENTRO = 6.0f;

    public RemolinoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        shadowRadius = 0f;
    }

    @Override
    public boolean shouldRender(Remolino e, Frustum f, double x, double y, double z) {
        // (la caja de la entidad es pequena: lo que se ve es todo el arco)
        return e.shouldRenderAtSqrDistance(e.distanceToSqr(x, y, z));
    }

    @Override
    public void render(Remolino e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        tormenta(e, pt, ps, buf);
        float abierto = e.abiertoEn(pt);
        if (abierto <= 0.01f) return;
        float t = (e.tickCount + pt) / 20f;
        VertexConsumer vc = buf.getBuffer(RenderType.entityTranslucentEmissive(TEX));
        ps.pushPose();
        // (el giro de la entidad: el arco mira al norte o al sur; el remolino es plano en x-y)
        ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-e.getYRot()));
        Matrix4f m = ps.last().pose();
        Matrix3f n = ps.last().normal();
        float latido = 0.86f + 0.14f * Mth.sin(t * 2.2f);
        // (el dibujo es un circulo de radio 0,43 en la textura, con el borde transparente y "clamp": la capa de delante, a
        // 1/20, cubre todo el arco; la de detras, a 1/13, es un remolino mas grande y lento alrededor del ojo)
        // la capa de detras: girando al reves, mas apagada
        capa(vc, m, n, abierto, -t * 0.45f, 1 / 13f, 0.02f, latido * 0.75f, 0.55f);
        // la de delante
        capa(vc, m, n, abierto, t * 0.9f, 1 / 20f, -0.02f, latido, 0.92f);
        ps.popPose();
        super.render(e, yaw, pt, ps, buf, luz);
    }

    /** Una capa: las filas del hueco (cada una un cuadro), por las dos caras, con la espiral girada {@code ang}. */
    private static void capa(VertexConsumer vc, Matrix4f m, Matrix3f n, float abierto, float ang, float escala, float dz, float luz, float alfa) {
        float c = Mth.cos(ang), s = Mth.sin(ang);
        int a = (int) (alfa * 255);
        int col = (int) (Mth.clamp(luz, 0f, 1f) * 255);
        for (int h = 0; h < TorreCupula.AALTO; h++) {
            int w = TorreCupula.arcoPortal(h);
            if (w < 0) continue;
            float x0 = -w - 0.5f, x1 = w + 0.5f, y0 = h, y1 = h + 1;
            // (crece desde el ojo de la espiral)
            float[][] q = {{x0, y0}, {x1, y0}, {x1, y1}, {x0, y1}};
            float[][] p = new float[4][2];
            for (int i = 0; i < 4; i++) {
                p[i][0] = q[i][0] * abierto;
                p[i][1] = CENTRO + (q[i][1] - CENTRO) * abierto;
            }
            // delante (hacia +z) y detras
            for (int cara = 0; cara < 2; cara++) {
                float z = cara == 0 ? dz : -dz;
                for (int k = 0; k < 4; k++) {
                    int i = cara == 0 ? k : 3 - k;
                    float dx = q[i][0], dy = q[i][1] - CENTRO;
                    float u = 0.5f + (dx * c - dy * s) * escala, v = 0.5f + (dx * s + dy * c) * escala;
                    vc.vertex(m, p[i][0], p[i][1], z).color(col, col, col, a).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY)
                            .uv2(0xF000F0).normal(n, 0f, 0f, cara == 0 ? 1f : -1f).endVertex();
                }
            }
        }
    }

    // ------------------------------------------------------------------ el capitulo 6: las nubes y el rayo rojo

    private static float suave(float x) {
        x = Mth.clamp(x, 0f, 1f);
        return x * x * (3 - 2 * x);
    }

    /**
     * Las nubes que se arremolinan sobre el portal (desde {@link Remolino#tormenta}): un disco de nubes que crece en cuatro
     * segundos y gira hacia el centro (dos capas al reves); desde el rayo se enrojece y al final se deshace. Y el rayo rojo
     * ({@link Remolino#rayo}): un zigzag del centro de las nubes a la punta del arco, que parpadea un segundo y medio.
     */
    private static void tormenta(Remolino e, float pt, PoseStack ps, MultiBufferSource buf) {
        long t0 = e.tormenta();
        if (t0 <= 0) return;
        double now = e.level().getGameTime() + pt;
        float tor = (float) ((now - t0) / 20.0);
        if (tor < 0) return;
        long r0 = e.rayo();
        float desde = r0 > 0 ? (float) ((now - r0) / 20.0) : -1f;
        float fin = desde < 0 ? 1f : 1f - suave((desde - (Remolino.DURA_TORMENTA - 6f)) / 6f);
        if (fin <= 0.01f) return;
        float crece = suave(tor / 4f), rojo = desde < 0 ? 0f : suave(desde / 3f);
        float H = Remolino.ALTO_NUBES, R = 22f + 104f * crece;
        VertexConsumer vc = buf.getBuffer(RenderType.entityTranslucentEmissive(NUBES));
        ps.pushPose();
        ps.translate(0, H, 0);
        Matrix4f m = ps.last().pose();
        Matrix3f n = ps.last().normal();
        // (de color: gris de tormenta; desde el rayo, rojo encendido)
        float cr = Mth.lerp(rojo, 0.42f, 0.95f), cg = Mth.lerp(rojo, 0.38f, 0.22f), cb = Mth.lerp(rojo, 0.42f, 0.16f);
        disco(vc, m, n, R, tor * 0.32f, 0.43f, cr, cg, cb, 0.9f * fin * Math.min(1f, tor / 1.5f), 0f);
        disco(vc, m, n, R * 0.8f, -tor * 0.18f + 1.3f, 0.43f, cr * 0.8f, cg * 0.8f, cb * 0.8f, 0.6f * fin * Math.min(1f, tor / 2f), -3f);
        ps.popPose();
        if (desde >= 0 && desde < 1.5f) {
            // el destello en el cielo (como el de un rayo de verdad) y el rayo
            if (desde < 0.12f && e.level() instanceof net.minecraft.client.multiplayer.ClientLevel cl) cl.setSkyFlashTime(3);
            boolean se = desde < 0.3f || ((int) (desde * 20) % 4) != 3;
            if (se) rayo(buf.getBuffer(RenderType.lightning()), ps.last().pose(), r0, H, TorreCupula.AALTO + 3.5f, 1f - suave((desde - 1.1f) / 0.4f));
        }
    }

    /** Un disco horizontal de radio R a la altura dy (anillos de cuadros), con la textura girada ang (vista desde abajo y desde arriba). */
    private static void disco(VertexConsumer vc, Matrix4f m, Matrix3f n, float R, float ang, float esc, float r, float g, float b, float a, float dy) {
        int N = 40, M = 6;
        int ir = (int) (r * 255), ig = (int) (g * 255), ib = (int) (b * 255), ia = (int) (Mth.clamp(a, 0f, 1f) * 255);
        for (int j = 0; j < M; j++)
            for (int i = 0; i < N; i++) {
                float r0 = R * j / M, r1 = R * (j + 1) / M;
                float a0 = Mth.TWO_PI * i / N, a1 = Mth.TWO_PI * (i + 1) / N;
                float[][] q = {{r0, a0}, {r1, a0}, {r1, a1}, {r0, a1}};
                for (int cara = 0; cara < 2; cara++)
                    for (int k = 0; k < 4; k++) {
                        float[] p = q[cara == 0 ? k : 3 - k];
                        float x = p[0] * Mth.cos(p[1]), z = p[0] * Mth.sin(p[1]);
                        float u = 0.5f + p[0] / R * esc * Mth.cos(p[1] + ang), v = 0.5f + p[0] / R * esc * Mth.sin(p[1] + ang);
                        vc.vertex(m, x, dy, z).color(ir, ig, ib, ia).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0)
                                .normal(n, 0f, cara == 0 ? -1f : 1f, 0f).endVertex();
                    }
            }
    }

    /** El rayo rojo: un zigzag (siempre el mismo para ese rayo) con tres ramas; tres capas de grosor, de rojo a casi blanco. */
    private static void rayo(VertexConsumer vc, Matrix4f m, long semilla, float y0, float y1, float a) {
        java.util.Random r = new java.util.Random(semilla * 31 + 7);
        int N = 16;
        float[][] p = new float[N + 1][3];
        float x = 0, z = 0;
        for (int i = 0; i <= N; i++) {
            float k = i / (float) N;
            if (i > 0 && i < N) {
                float am = 3.2f * (1 - k * 0.85f);
                x = Mth.clamp(x + (r.nextFloat() - 0.5f) * am * 1.6f, -am * 1.5f, am * 1.5f);
                z = Mth.clamp(z + (r.nextFloat() - 0.5f) * am * 1.6f, -am * 1.5f, am * 1.5f);
            }
            if (i == N) x = z = 0;
            p[i] = new float[]{x, Mth.lerp(k, y0, y1), z};
        }
        java.util.List<float[][]> tramos = new java.util.ArrayList<>();
        for (int i = 0; i < N; i++) tramos.add(new float[][]{p[i], p[i + 1], {1f}});
        // las ramas: de un punto de arriba, hacia un lado y hacia abajo
        for (int b = 0; b < 3; b++) {
            int de = 2 + r.nextInt(N / 2);
            float bx = p[de][0], by = p[de][1], bz = p[de][2];
            float dx = (r.nextFloat() - 0.5f) * 5f, dz = (r.nextFloat() - 0.5f) * 5f;
            for (int k = 0; k < 5; k++) {
                float nx = bx + dx + (r.nextFloat() - 0.5f) * 2f, ny = by - (2.5f + r.nextFloat() * 2f), nz = bz + dz + (r.nextFloat() - 0.5f) * 2f;
                tramos.add(new float[][]{{bx, by, bz}, {nx, ny, nz}, {0.55f}});
                bx = nx;
                by = ny;
                bz = nz;
            }
        }
        float[][] capas = {{1.1f, 0.85f, 0.08f, 0.05f, 0.30f}, {0.5f, 1f, 0.22f, 0.12f, 0.55f}, {0.16f, 1f, 0.85f, 0.75f, 0.95f}};
        for (float[] c : capas)
            for (float[][] t : tramos) caja(vc, m, t[0], t[1], c[0] * t[2][0], c[1], c[2], c[3], c[4] * a);
    }

    /** Un tramo del rayo: una caja de lado 2w de un punto a otro (las cuatro caras de los lados). */
    private static void caja(VertexConsumer vc, Matrix4f m, float[] a, float[] b, float w, float r, float g, float bl, float al) {
        float[][] o = {{-w, -w}, {w, -w}, {w, w}, {-w, w}};
        for (int k = 0; k < 4; k++) {
            float[] u = o[k], v = o[(k + 1) % 4];
            vc.vertex(m, a[0] + u[0], a[1], a[2] + u[1]).color(r, g, bl, al).endVertex();
            vc.vertex(m, a[0] + v[0], a[1], a[2] + v[1]).color(r, g, bl, al).endVertex();
            vc.vertex(m, b[0] + v[0], b[1], b[2] + v[1]).color(r, g, bl, al).endVertex();
            vc.vertex(m, b[0] + u[0], b[1], b[2] + u[1]).color(r, g, bl, al).endVertex();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Remolino e) {
        return TEX;
    }
}
