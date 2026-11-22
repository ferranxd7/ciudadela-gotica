package com.darkcraft.client.rey;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.rey.ReyProyectil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Los tajos (medias lunas de energia que brillan, con un nucleo blanco y un halo del color de la fase) y las
 * bolas de fuego (nucleo de luz con llamas de verdad girando alrededor). La ola de fuego solo son particulas.
 */
public class ProyectilRenderer extends EntityRenderer<ReyProyectil> {
    private static final ResourceLocation TAJO = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/rey/tajo.png");

    public ProyectilRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    /** {nucleo r,g,b, halo r,g,b}. Fase 0: las espadas del Rey en manos de un jugador (oro). */
    private static float[] colores(int fase) {
        return switch (fase) {
            case 0 -> new float[]{1f, 0.96f, 0.72f, 1f, 0.68f, 0.14f};
            case 2 -> new float[]{1f, 0.9f, 0.55f, 1f, 0.4f, 0.06f};
            case 3 -> new float[]{1f, 0.7f, 0.3f, 0.95f, 0.16f, 0.02f};
            default -> new float[]{0.96f, 0.98f, 1f, 0.45f, 0.65f, 1f};
        };
    }

    @Override
    public void render(ReyProyectil e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int light) {
        int t = e.tipo();
        if (t == ReyProyectil.TAJO) tajo(e, pt, ps, buf);
        else if (t == ReyProyectil.BOLA) bola(e, pt, ps, buf);
        super.render(e, yaw, pt, ps, buf, light);
    }

    // ------------------------------------------------------------------ media luna

    private void tajo(ReyProyectil e, float pt, PoseStack ps, MultiBufferSource buf) {
        float age = e.tickCount + pt;
        float dis = e.disolviendo(pt);
        float grow = Mth.clamp(age / 3f, 0f, 1f);
        float alpha = (1f - dis) * (1f - dis) * (0.55f + 0.45f * grow);
        if (alpha <= 0.01f) return;
        float esc = (0.6f + 0.4f * grow) * (1f + 0.4f * dis);
        float[] c = colores(e.fase());
        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(pt, e.yRotO, e.getYRot())));
        ps.mulPose(Axis.ZP.rotationDegrees(e.giro()));
        ps.scale(esc, esc, esc);
        VertexConsumer vc = buf.getBuffer(Tipos.BRILLO);
        Matrix4f m = ps.last().pose();
        int n = 28;
        for (int i = 0; i < n; i++) {
            float u0 = -1f + 2f * i / n, u1 = -1f + 2f * (i + 1) / n;
            float sh = 0.85f + 0.15f * Mth.sin(age * 1.4f + i * 0.7f);
            seg(vc, m, u0, u1, c, alpha * sh);
        }
        ps.popPose();
    }

    private static float ancho(float u) {
        float k = 1f - u * u;
        return 0.42f * (float) Math.pow(Math.max(0f, k), 0.7);
    }

    /**
     * Punto del arco a la distancia rad del centro, en la posicion u (-1..1). La media luna mira hacia donde va
     * (quien la recibe ve el arco entero) y el centro del arco va un poco por delante de las puntas; h la
     * engorda en la direccion del viaje (para que de lado tambien se vea).
     */
    private static float[] p(float u, float rad, float h) {
        double[] q = ReyProyectil.arcoLocal(u, rad);
        return new float[]{(float) q[0], (float) q[1], (float) (q[2] + h)};
    }

    private static void v(VertexConsumer vc, Matrix4f m, float[] p, float r, float g, float b, float a) {
        vc.vertex(m, p[0], p[1], p[2]).color(r, g, b, Math.max(0f, Math.min(1f, a))).endVertex();
    }

    /** Un trozo del arco: halo ancho y suave, nucleo blanco fino, y una cinta vertical para que se vea de canto. */
    private static void seg(VertexConsumer vc, Matrix4f m, float u0, float u1, float[] c, float a) {
        float R = ReyProyectil.RADIO;
        float w0 = ancho(u0), w1 = ancho(u1);
        // radios de cada borde: halo de dentro, borde de dentro, nucleo, borde de fuera, halo de fuera
        float[][] rr = {
                {R - 1.9f * w0, R - w0, R - 0.25f * w0, R, R + 0.45f * w0},
                {R - 1.9f * w1, R - w1, R - 0.25f * w1, R, R + 0.45f * w1}};
        float[] al = {0f, 0.22f, 0.6f, 0.42f, 0f};
        for (int k = 0; k < 4; k++) {
            float[] a0 = p(u0, rr[0][k], 0), b0 = p(u0, rr[0][k + 1], 0), a1 = p(u1, rr[1][k], 0), b1 = p(u1, rr[1][k + 1], 0);
            v(vc, m, a0, c[3], c[4], c[5], al[k] * a);
            v(vc, m, b0, c[3], c[4], c[5], al[k + 1] * a);
            v(vc, m, b1, c[3], c[4], c[5], al[k + 1] * a);
            v(vc, m, a1, c[3], c[4], c[5], al[k] * a);
        }
        // nucleo: banda fina y muy brillante junto al borde de fuera
        float c0 = R - 0.25f * w0, c1 = R - 0.25f * w1, h0 = 0.16f * w0 / 0.42f + 0.01f, h1 = 0.16f * w1 / 0.42f + 0.01f;
        float[][] band = {{c0 - h0, c0, c0 + h0}, {c1 - h1, c1, c1 + h1}};
        float[] ab = {0f, 0.95f, 0f};
        for (int k = 0; k < 2; k++) {
            float[] a0 = p(u0, band[0][k], 0), b0 = p(u0, band[0][k + 1], 0), a1 = p(u1, band[1][k], 0), b1 = p(u1, band[1][k + 1], 0);
            v(vc, m, a0, c[0], c[1], c[2], ab[k] * a);
            v(vc, m, b0, c[0], c[1], c[2], ab[k + 1] * a);
            v(vc, m, b1, c[0], c[1], c[2], ab[k + 1] * a);
            v(vc, m, a1, c[0], c[1], c[2], ab[k] * a);
        }
        // cinta a lo largo del viaje (se ve aunque la media luna quede de canto)
        float[][] hv = {{-h0 * 1.6f, 0f, h0 * 1.6f}, {-h1 * 1.6f, 0f, h1 * 1.6f}};
        float[] av = {0f, 0.75f, 0f};
        for (int k = 0; k < 2; k++) {
            float[] a0 = p(u0, c0, hv[0][k]), b0 = p(u0, c0, hv[0][k + 1]), a1 = p(u1, c1, hv[1][k]), b1 = p(u1, c1, hv[1][k + 1]);
            v(vc, m, a0, c[0], c[1], c[2], av[k] * a);
            v(vc, m, b0, c[0], c[1], c[2], av[k + 1] * a);
            v(vc, m, b1, c[0], c[1], c[2], av[k + 1] * a);
            v(vc, m, a1, c[0], c[1], c[2], av[k] * a);
        }
    }

    // ------------------------------------------------------------------ bola de fuego

    private void bola(ReyProyectil e, float pt, PoseStack ps, MultiBufferSource buf) {
        float age = e.tickCount + pt;
        float puls = 1f + 0.08f * Mth.sin(age * 0.9f);
        // nucleo de luz de cara a la camara
        ps.pushPose();
        ps.mulPose(entityRenderDispatcher.cameraOrientation());
        VertexConsumer vc = buf.getBuffer(Tipos.BRILLO);
        Matrix4f m = ps.last().pose();
        disco(vc, m, 1.05f * puls, 1f, 0.42f, 0.06f, 0.5f, 0f);
        disco(vc, m, 0.5f * puls, 1f, 0.92f, 0.55f, 1f, 0.25f);
        ps.popPose();
        // llamas: el fuego animado del juego en cuatro planos que giran alrededor
        TextureAtlasSprite f0 = ModelBakery.FIRE_0.sprite(), f1 = ModelBakery.FIRE_1.sprite();
        VertexConsumer fv = buf.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        for (int k = 0; k < 4; k++) {
            ps.pushPose();
            ps.mulPose(Axis.YP.rotationDegrees(age * 11f + k * 45f));
            ps.mulPose(Axis.XP.rotationDegrees(Mth.sin(age * 0.3f + k) * 12f));
            llama(fv, ps, (k & 1) == 0 ? f0 : f1, 0.62f * puls, -0.55f, 0.85f);
            ps.popPose();
        }
    }

    /** Disco con brillo que se apaga hacia el borde (triangulos como quads con un vertice repetido). */
    private static void disco(VertexConsumer vc, Matrix4f m, float r, float cr, float cg, float cb, float aIn, float aOut) {
        int n = 20;
        for (int i = 0; i < n; i++) {
            double a0 = i * Math.PI * 2 / n, a1 = (i + 1) * Math.PI * 2 / n;
            vc.vertex(m, 0, 0, 0).color(cr, cg, cb, aIn).endVertex();
            vc.vertex(m, 0, 0, 0).color(cr, cg, cb, aIn).endVertex();
            vc.vertex(m, (float) (Math.cos(a0) * r), (float) (Math.sin(a0) * r), 0).color(cr, cg, cb, aOut).endVertex();
            vc.vertex(m, (float) (Math.cos(a1) * r), (float) (Math.sin(a1) * r), 0).color(cr, cg, cb, aOut).endVertex();
        }
    }

    private static void llama(VertexConsumer vc, PoseStack ps, TextureAtlasSprite s, float half, float y0, float y1) {
        Matrix4f m = ps.last().pose();
        Matrix3f n = ps.last().normal();
        float u0 = s.getU0(), u1 = s.getU1(), v0 = s.getV0(), v1 = s.getV1();
        vc.vertex(m, -half, y0, 0).color(255, 255, 255, 255).uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(n, 0, 1, 0).endVertex();
        vc.vertex(m, half, y0, 0).color(255, 255, 255, 255).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(n, 0, 1, 0).endVertex();
        vc.vertex(m, half, y1, 0).color(255, 255, 255, 255).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(n, 0, 1, 0).endVertex();
        vc.vertex(m, -half, y1, 0).color(255, 255, 255, 255).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(n, 0, 1, 0).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(ReyProyectil e) {
        return TAJO;
    }
}
