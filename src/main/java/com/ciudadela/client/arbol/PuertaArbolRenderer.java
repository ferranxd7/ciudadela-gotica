package com.ciudadela.client.arbol;

import com.ciudadela.CiudadelaMod;
import com.ciudadela.arbol.PuertaArbol;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Las dos hojas de la puerta de la Sala del Arbol. El origen de la entidad esta en el centro del umbral, en
 * el plano medio de las hojas; cada hoja gira sobre su bisagra (en los bordes de fuera) hacia la sala (norte).
 * Delante (cara de la recepcion, +z) van los tres cerrojos de hierro.
 */
public class PuertaArbolRenderer extends EntityRenderer<PuertaArbol> {
    private static final ResourceLocation TEX = new ResourceLocation(CiudadelaMod.MODID, "textures/entity/puerta_arbol.png");
    private static final ResourceLocation GLOW = new ResourceLocation(CiudadelaMod.MODID, "textures/entity/puerta_arbol_brillo.png");
    private static final float T = 256f;
    private static final float H = PuertaArbol.ALTO, G = PuertaArbol.GROSOR / 2, B = G;           // medio grosor, bisagra metida
    private static final float L = PuertaArbol.HOJA;                                            // ancho de cada hoja

    public PuertaArbolRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0f;
    }

    @Override
    public void render(PuertaArbol e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int light0) {
        BlockPos delante = BlockPos.containing(e.getX(), e.getY() + 3, e.getZ() + 1.2);
        int light = LevelRenderer.getLightColor(e.level(), delante);
        float ang = e.abertura(pt) * 88f;
        float br = e.brillo(pt);
        // primero toda la madera y el hierro, luego el brillo: cada getBuffer de otro tipo cierra el anterior
        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(TEX));
        dosHojas(e, pt, ps, vc, null, 0f, ang, light);
        if (br > 0.01f) dosHojas(e, pt, ps, null, buf.getBuffer(RenderType.eyes(GLOW)), br, ang, light);
        super.render(e, yaw, pt, ps, buf, light0);
    }

    /** Las dos hojas (con vc la madera y los cerrojos; con gl solo el brillo de la cara de la recepcion). */
    private static void dosHojas(PuertaArbol e, float pt, PoseStack ps, VertexConsumer vc, VertexConsumer gl, float br, float ang, int light) {
        // hoja izquierda (oeste): bisagra en x = -3,5 + B; se extiende hacia +x
        ps.pushPose();
        ps.translate(-L + B, 0, 0);
        ps.mulPose(Axis.YP.rotationDegrees(ang));
        hoja(ps, vc, gl, br, -B, L - B, 0f, 56f, 224f, 168f, light);
        if (vc != null) for (int i = 0; i < 3; i++) cerrojo(ps, vc, i, e.cerrojo(i, pt), L - B, light);
        ps.popPose();
        // hoja derecha (este): bisagra en x = +3,5 - B; se extiende hacia -x
        ps.pushPose();
        ps.translate(L - B, 0, 0);
        ps.mulPose(Axis.YP.rotationDegrees(-ang));
        hoja(ps, vc, gl, br, -(L - B), B, 56f, 112f, 168f, 112f, light);
        if (vc != null) for (int i = 0; i < 3; i++) pestillo(ps, vc, i, -(L - B), light);
        ps.popPose();
    }

    /**
     * Una hoja de x0 a x1 (locales), de 0 a H de alto y de -G a G de grueso. Delante (+z) la cara de la recepcion
     * (u de uf0 a uf1 de x0 a x1); detras la de la sala (u de ub0 a ub1 de x0 a x1).
     */
    private static void hoja(PoseStack ps, VertexConsumer vc, VertexConsumer gl, float br, float x0, float x1,
                             float uf0, float uf1, float ub0, float ub1, int light) {
        Matrix4f m = ps.last().pose();
        Matrix3f n = ps.last().normal();
        if (gl != null) {
            quad(gl, m, n, x0, 0, G + 0.004f, x1, 0, G + 0.004f, x1, H, G + 0.004f, x0, H, G + 0.004f,
                    uf0, 160, uf1, 160, uf1, 0, uf0, 0, LightTexture.FULL_BRIGHT, br, 0, 0, 1);
            return;
        }
        // delante (recepcion)
        quad(vc, m, n, x0, 0, G, x1, 0, G, x1, H, G, x0, H, G, uf0, 160, uf1, 160, uf1, 0, uf0, 0, light, 1f, 0, 0, 1);
        // detras (sala)
        quad(vc, m, n, x1, 0, -G, x0, 0, -G, x0, H, -G, x1, H, -G, ub1, 160, ub0, 160, ub0, 0, ub1, 0, light, 0.9f, 0, 0, -1);
        // cantos
        quad(vc, m, n, x0, 0, -G, x0, 0, G, x0, H, G, x0, H, -G, 224, 160, 230, 160, 230, 0, 224, 0, light, 0.75f, -1, 0, 0);
        quad(vc, m, n, x1, 0, G, x1, 0, -G, x1, H, -G, x1, H, G, 224, 160, 230, 160, 230, 0, 224, 0, light, 0.75f, 1, 0, 0);
        quad(vc, m, n, x0, H, G, x1, H, G, x1, H, -G, x0, H, -G, 0, 166, 56, 166, 56, 160, 0, 160, light, 0.8f, 0, 1, 0);
        quad(vc, m, n, x0, 0, -G, x1, 0, -G, x1, 0, G, x0, 0, G, 0, 166, 56, 166, 56, 160, 0, 160, light, 0.6f, 0, -1, 0);
    }

    /** Cerrojo i (0 arriba): una barra de hierro que cruza la junta y se descorre hacia la hoja izquierda. */
    private static void cerrojo(PoseStack ps, VertexConsumer vc, int i, float desc, float junta, int light) {
        float y = 7.5f - i * 2.5f;
        float xa = junta - 0.75f - desc * 1.0f, xb = junta + 0.75f - desc * 1.0f;
        caja(ps, vc, xa, y - 0.11f, G, xb, y + 0.11f, G + 0.22f, 0, 176, 24, 182, light);
        // guias en la hoja (no se mueven)
        caja(ps, vc, junta - 1.55f, y - 0.2f, G, junta - 1.35f, y + 0.2f, G + 0.3f, 32, 176, 40, 184, light);
        caja(ps, vc, junta - 0.55f, y - 0.2f, G, junta - 0.35f, y + 0.2f, G + 0.3f, 32, 176, 40, 184, light);
    }

    /** El cerradero de la hoja derecha donde entra cada cerrojo. */
    private static void pestillo(PoseStack ps, VertexConsumer vc, int i, float junta, int light) {
        float y = 7.5f - i * 2.5f;
        caja(ps, vc, junta + 0.45f, y - 0.2f, G, junta + 0.7f, y + 0.2f, G + 0.3f, 32, 176, 40, 184, light);
    }

    private static void caja(PoseStack ps, VertexConsumer vc, float x0, float y0, float z0, float x1, float y1, float z1,
                             float u0, float v0, float u1, float v1, int light) {
        Matrix4f m = ps.last().pose();
        Matrix3f n = ps.last().normal();
        quad(vc, m, n, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, u0, v1, u1, v1, u1, v0, u0, v0, light, 1f, 0, 0, 1);
        quad(vc, m, n, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, u0, v1, u1, v1, u1, v0, u0, v0, light, 1f, 0, 1, 0);
        quad(vc, m, n, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, u0, v1, u1, v1, u1, v0, u0, v0, light, 0.6f, 0, -1, 0);
        quad(vc, m, n, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, u0, v1, u0 + 2, v1, u0 + 2, v0, u0, v0, light, 0.8f, -1, 0, 0);
        quad(vc, m, n, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, u1 - 2, v1, u1, v1, u1, v0, u1 - 2, v0, light, 0.8f, 1, 0, 0);
    }

    private static void quad(VertexConsumer vc, Matrix4f m, Matrix3f n,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz,
                             float ua, float va, float ub, float vb, float uc, float vcc, float ud, float vd,
                             int light, float k, float nx, float ny, float nz) {
        float c = Mth.clamp(k, 0f, 1f);
        vc.vertex(m, ax, ay, az).color(c, c, c, 1f).uv(ua / T, va / T).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(n, nx, ny, nz).endVertex();
        vc.vertex(m, bx, by, bz).color(c, c, c, 1f).uv(ub / T, vb / T).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(n, nx, ny, nz).endVertex();
        vc.vertex(m, cx, cy, cz).color(c, c, c, 1f).uv(uc / T, vcc / T).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(n, nx, ny, nz).endVertex();
        vc.vertex(m, dx, dy, dz).color(c, c, c, 1f).uv(ud / T, vd / T).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(n, nx, ny, nz).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(PuertaArbol e) {
        return TEX;
    }
}
