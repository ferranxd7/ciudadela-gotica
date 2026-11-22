package com.darkcraft.client.arbol;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.arbol.Ascensor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
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
 * El disco de runas: un disco de piedra negra con aro de oro y runas que brillan (mas cuando alguien espera
 * encima y mucho mientras viaja), con un anillo de luz que gira. Cuando el disco no esta arriba, en el hueco
 * de la recepcion se ve su circulo de runas, tenue, para que se sepa que ahi se le llama.
 */
public class AscensorRenderer extends EntityRenderer<Ascensor> {
    private static final ResourceLocation TEX = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/ascensor.png");
    private static final ResourceLocation GLOW = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/ascensor_brillo.png");
    private static final int N = 48;
    private static final float GROSOR = 0.3f;

    public AscensorRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0f;
    }

    @Override
    public void render(Ascensor e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int light) {
        float t = e.tickCount + pt;
        float r = (float) Ascensor.RADIO - 0.02f;
        boolean viaja = e.enMarcha();
        // el disco
        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(TEX));
        disco(ps, vc, r, 0f, light, 1f, 1f, 1f, true);
        // runas encendidas
        float k = viaja ? 1f : 0.32f + 0.68f * e.carga() + 0.07f * Mth.sin(t * 0.12f);
        VertexConsumer g = buf.getBuffer(RenderType.eyes(GLOW));
        disco(ps, g, r, 0.004f, LightTexture.FULL_BRIGHT, k, k * 0.92f, k * 0.75f, false);
        // anillo de luz girando por encima (al viajar o al despertar)
        float aro = viaja ? 1f : e.carga();
        if (aro > 0.02f) {
            ps.pushPose();
            ps.translate(0, 0.06 + 0.08 * Mth.sin(t * 0.2f), 0);
            ps.mulPose(Axis.YP.rotationDegrees(t * (viaja ? 6f : 3f)));
            disco(ps, g, r * 1.04f, 0f, LightTexture.FULL_BRIGHT, 0.55f * aro, 0.45f * aro, 0.25f * aro, false);
            ps.popPose();
        }
        // el circulo de la recepcion cuando el disco esta abajo o viajando
        double arriba = e.y1() - Mth.lerp(pt, e.yo, e.getY());
        if (arriba > 1.5) {
            ps.pushPose();
            ps.translate(0, arriba + 0.01, 0);
            ps.mulPose(Axis.YP.rotationDegrees(-t * 0.6f));
            float a = 0.22f + 0.06f * Mth.sin(t * 0.1f);
            disco(ps, g, r, 0f, LightTexture.FULL_BRIGHT, a, a * 0.85f, a * 0.6f, false);
            ps.popPose();
        }
        super.render(e, yaw, pt, ps, buf, light);
    }

    /** Disco de radio r: cara de arriba (y la de abajo y el canto si 'solido'), centrado en el origen. */
    private static void disco(PoseStack ps, VertexConsumer vc, float r, float dy, int light, float cr, float cg, float cb, boolean solido) {
        Matrix4f m = ps.last().pose();
        Matrix3f n = ps.last().normal();
        float s = 96f / 128f;
        for (int i = 0; i < N; i++) {
            double a0 = i * Math.PI * 2 / N, a1 = (i + 1) * Math.PI * 2 / N;
            float x0 = (float) Math.cos(a0) * r, z0 = (float) Math.sin(a0) * r;
            float x1 = (float) Math.cos(a1) * r, z1 = (float) Math.sin(a1) * r;
            float u0 = (0.5f + x0 / (2 * r)) * s, v0 = (0.5f + z0 / (2 * r)) * s;
            float u1 = (0.5f + x1 / (2 * r)) * s, v1 = (0.5f + z1 / (2 * r)) * s;
            float uc = 0.5f * s, vcc = 0.5f * s;
            // arriba
            v(vc, m, n, 0, dy, 0, uc, vcc, light, cr, cg, cb, 0, 1, 0);
            v(vc, m, n, x1, dy, z1, u1, v1, light, cr, cg, cb, 0, 1, 0);
            v(vc, m, n, x0, dy, z0, u0, v0, light, cr, cg, cb, 0, 1, 0);
            v(vc, m, n, x0, dy, z0, u0, v0, light, cr, cg, cb, 0, 1, 0);
            if (!solido) continue;
            // abajo
            float yb = dy - GROSOR;
            v(vc, m, n, 0, yb, 0, uc, vcc, light, cr * 0.6f, cg * 0.6f, cb * 0.6f, 0, -1, 0);
            v(vc, m, n, x0, yb, z0, u0, v0, light, cr * 0.6f, cg * 0.6f, cb * 0.6f, 0, -1, 0);
            v(vc, m, n, x1, yb, z1, u1, v1, light, cr * 0.6f, cg * 0.6f, cb * 0.6f, 0, -1, 0);
            v(vc, m, n, x1, yb, z1, u1, v1, light, cr * 0.6f, cg * 0.6f, cb * 0.6f, 0, -1, 0);
            // canto
            float su0 = (float) i / N, su1 = (float) (i + 1) / N;
            float nx = (float) Math.cos((a0 + a1) / 2), nz = (float) Math.sin((a0 + a1) / 2);
            v(vc, m, n, x0, dy, z0, su0, 96f / 128f, light, cr, cg, cb, nx, 0, nz);
            v(vc, m, n, x1, dy, z1, su1, 96f / 128f, light, cr, cg, cb, nx, 0, nz);
            v(vc, m, n, x1, yb, z1, su1, 104f / 128f, light, cr, cg, cb, nx, 0, nz);
            v(vc, m, n, x0, yb, z0, su0, 104f / 128f, light, cr, cg, cb, nx, 0, nz);
        }
    }

    private static void v(VertexConsumer vc, Matrix4f m, Matrix3f n, float x, float y, float z, float u, float vv, int light,
                          float r, float g, float b, float nx, float ny, float nz) {
        vc.vertex(m, x, y, z).color(Mth.clamp(r, 0f, 1f), Mth.clamp(g, 0f, 1f), Mth.clamp(b, 0f, 1f), 1f).uv(u, vv)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(n, nx, ny, nz).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(Ascensor e) {
        return TEX;
    }
}
