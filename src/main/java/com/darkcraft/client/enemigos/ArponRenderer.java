package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Arpon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * El arpon en vuelo: su dibujo ({@code textures/entity/enemigos/arpon.png}, de tools/enemigos/ahogado.py: la cola a la
 * izquierda y la punta a la derecha) en dos planos cruzados, a su tamano (el mismo que en la mano del marinero).
 */
public class ArponRenderer extends EntityRenderer<Arpon> {
    private static final ResourceLocation TEX = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/enemigos/arpon.png");
    /** Lo que mide el dibujo en la textura (64 x 8), en pixeles; un pixel es 1/16 de bloque. */
    private static final float LARGO = 35f, ALTO = 5f, TEX_W = 64f, TEX_H = 8f;
    /** Cuanto asoma la punta por delante de donde esta la entidad. */
    private static final float DELANTE = 5f;

    public ArponRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(Arpon e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        ps.pushPose();
        // como las flechas: tras estos giros, +X es hacia donde vuela
        ps.mulPose(Axis.YP.rotationDegrees(Mth.lerp(pt, e.yRotO, e.getYRot()) - 90f));
        ps.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(pt, e.xRotO, e.getXRot())));
        float tiembla = e.shakeTime - pt;
        if (tiembla > 0f) ps.mulPose(Axis.ZP.rotationDegrees(-Mth.sin(tiembla * 3f) * tiembla));
        ps.mulPose(Axis.XP.rotationDegrees(45f));
        ps.scale(1f / 16f, 1f / 16f, 1f / 16f);
        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(TEX));
        float x0 = DELANTE - LARGO, x1 = DELANTE, h = ALTO / 2f, u1 = LARGO / TEX_W, v1 = ALTO / TEX_H;
        for (int k = 0; k < 2; k++) {
            PoseStack.Pose p = ps.last();
            Matrix4f m = p.pose();
            Matrix3f n = p.normal();
            v(vc, m, n, x0, -h, 0f, 0f, v1, luz);
            v(vc, m, n, x1, -h, 0f, u1, v1, luz);
            v(vc, m, n, x1, h, 0f, u1, 0f, luz);
            v(vc, m, n, x0, h, 0f, 0f, 0f, luz);
            ps.mulPose(Axis.XP.rotationDegrees(90f));
        }
        ps.popPose();
        super.render(e, yaw, pt, ps, buf, luz);
    }

    private static void v(VertexConsumer vc, Matrix4f m, Matrix3f n, float x, float y, float z, float u, float v, int luz) {
        vc.vertex(m, x, y, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(luz).normal(n, 0f, 1f, 0f).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(Arpon e) {
        return TEX;
    }
}
