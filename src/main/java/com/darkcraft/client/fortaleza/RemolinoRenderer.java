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

    @Override
    public ResourceLocation getTextureLocation(Remolino e) {
        return TEX;
    }
}
