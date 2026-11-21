package com.ciudadela.client.rey;

import com.ciudadela.CiudadelaMod;
import com.ciudadela.rey.ReyProyectil;
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

/** El tajo (media luna de luz) y las bolas de fuego del Rey. La ola de fuego solo son particulas. */
public class ProyectilRenderer extends EntityRenderer<ReyProyectil> {
    private static final ResourceLocation TAJO = new ResourceLocation(CiudadelaMod.MODID, "textures/entity/rey/tajo.png");
    private static final ResourceLocation BOLA = new ResourceLocation(CiudadelaMod.MODID, "textures/entity/rey/bola.png");

    public ProyectilRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(ReyProyectil e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int light) {
        int t = e.tipo();
        if (t == ReyProyectil.OLA) return;
        ps.pushPose();
        float[] c = switch (e.fase()) {
            case 2 -> new float[]{1f, 0.55f, 0.2f};
            case 3 -> new float[]{1f, 0.75f, 0.3f};
            default -> new float[]{0.75f, 0.85f, 1f};
        };
        if (t == ReyProyectil.TAJO) {
            ps.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(pt, e.yRotO, e.getYRot())));
            ps.mulPose(Axis.ZP.rotationDegrees(20f));
            VertexConsumer vc = buf.getBuffer(RenderType.eyes(TAJO));
            // media luna plana, 3,4 x 1,7 bloques, cruzada para que se vea de lado
            quad(ps, vc, -1.7f, -0.85f, 1.7f, 0.85f, c, 0);
            quad(ps, vc, -1.7f, -0.85f, 1.7f, 0.85f, c, 1);
        } else {
            ps.mulPose(entityRenderDispatcher.cameraOrientation());
            ps.mulPose(Axis.YP.rotationDegrees(180f));
            ps.mulPose(Axis.ZP.rotationDegrees((e.tickCount + pt) * 20f));
            VertexConsumer vc = buf.getBuffer(RenderType.eyes(BOLA));
            quad(ps, vc, -0.6f, -0.6f, 0.6f, 0.6f, new float[]{1f, 1f, 1f}, 2);
        }
        ps.popPose();
        super.render(e, yaw, pt, ps, buf, light);
    }

    /** plano 0 = horizontal, 1 = vertical, 2 = de cara a la camara. */
    private static void quad(PoseStack ps, VertexConsumer vc, float x0, float y0, float x1, float y1, float[] c, int plano) {
        Matrix4f m = ps.last().pose();
        Matrix3f n = ps.last().normal();
        float[][] p = switch (plano) {
            case 0 -> new float[][]{{x0, 0, y0}, {x1, 0, y0}, {x1, 0, y1}, {x0, 0, y1}};
            case 1 -> new float[][]{{x0, y0, 0}, {x1, y0, 0}, {x1, y1, 0}, {x0, y1, 0}};
            default -> new float[][]{{x0, y0, 0}, {x1, y0, 0}, {x1, y1, 0}, {x0, y1, 0}};
        };
        float[][] uv = {{0, 1}, {1, 1}, {1, 0}, {0, 0}};
        for (int pass = 0; pass < 2; pass++)
            for (int i = 0; i < 4; i++) {
                int k = pass == 0 ? i : 3 - i;
                vc.vertex(m, p[k][0], p[k][1], p[k][2]).color(c[0], c[1], c[2], 1f).uv(uv[k][0], uv[k][1])
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(n, 0, 1, 0).endVertex();
            }
    }

    @Override
    public ResourceLocation getTextureLocation(ReyProyectil e) {
        return e.tipo() == ReyProyectil.BOLA ? BOLA : TAJO;
    }
}
