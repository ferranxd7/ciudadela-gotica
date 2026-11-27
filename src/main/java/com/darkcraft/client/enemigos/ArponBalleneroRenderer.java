package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.ArponBallenero;
import com.darkcraft.enemigos.Ballenero;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * El arpon de Brann en vuelo (o clavado, o volviendo): el dibujo del arpon de los ahogados
 * ({@code textures/entity/enemigos/arpon.png}) en dos planos cruzados, mas grande (el de Brann), y su <b>cabo</b>: una cuerda
 * (como la de una correa del juego, mas gruesa y del color del canamo viejo) de la cola del arpon a la mano derecha de Brann, que
 * cuelga un poco. Todo con los tipos de dibujo del juego (con shaders tambien se ve).
 */
public class ArponBalleneroRenderer extends EntityRenderer<ArponBallenero> {
    private static final ResourceLocation TEX = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/enemigos/arpon.png");
    private static final float LARGO = 35f, ALTO = 5f, TEX_W = 64f, TEX_H = 8f, DELANTE = 5f;
    /** Lo grande que es el de Brann (el de los ahogados x 1,6). */
    private static final float ESCALA = 1.6f;
    /** Lo grueso del cabo (bloques). */
    private static final float GRUESO = 0.05f;

    public ArponBalleneroRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(ArponBallenero e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        float giro = Mth.lerp(pt, e.yRotO, e.getYRot()), cabeceo = Mth.lerp(pt, e.xRotO, e.getXRot());
        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(giro - 90f));
        ps.mulPose(Axis.ZP.rotationDegrees(cabeceo));
        ps.mulPose(Axis.XP.rotationDegrees(45f));
        ps.scale(ESCALA / 16f, ESCALA / 16f, ESCALA / 16f);
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
        // el cabo: de la cola del arpon a la mano de Brann
        Entity d = e.getOwner();
        if (d instanceof Ballenero b && b.isAlive()) {
            // hacia donde apunta (los giros de las flechas: yRot = atan2(x, z), xRot = atan2(y, horizontal))
            Vec3 dir = Vec3.directionFromRotation(-cabeceo, -giro);
            // la cola (tras los giros de arriba: x hacia donde vuela, la cola en x0 * ESCALA / 16)
            float cola = (DELANTE - LARGO) * ESCALA / 16f;
            Vec3 desde = dir.scale(cola);
            if (e.clavado() != null) desde = Vec3.ZERO;
            Vec3 yo = new Vec3(Mth.lerp(pt, e.xo, e.getX()), Mth.lerp(pt, e.yo, e.getY()), Mth.lerp(pt, e.zo, e.getZ()));
            Vec3 mano = mano(b, pt).subtract(yo);
            cabo(ps, buf, e, desde, mano, yo);
        }
        super.render(e, yaw, pt, ps, buf, luz);
    }

    /** Donde esta la mano derecha de Brann (entre dos ticks): como {@link Ballenero#mano}, pero suave. */
    static Vec3 mano(Ballenero b, float pt) {
        float giro = Mth.rotLerp(pt, b.yBodyRotO, b.yBodyRot);
        Vec3 d = Vec3.directionFromRotation(0, giro);
        double x = Mth.lerp(pt, b.xo, b.getX()), y = Mth.lerp(pt, b.yo, b.getY()), z = Mth.lerp(pt, b.zo, b.getZ());
        return new Vec3(x + d.x * 0.9 - d.z * 0.75, y + 2.1, z + d.z * 0.9 + d.x * 0.75);
    }

    /** La cuerda de {@code a} a {@code b} (relativos a la entidad), que cuelga un poco; dos tiras cruzadas, como las correas. */
    private static void cabo(PoseStack ps, MultiBufferSource buf, ArponBallenero e, Vec3 a, Vec3 b, Vec3 yo) {
        VertexConsumer vc = buf.getBuffer(RenderType.leash());
        Matrix4f m = ps.last().pose();
        float dx = (float) (b.x - a.x), dy = (float) (b.y - a.y), dz = (float) (b.z - a.z);
        float largo = Mth.sqrt(dx * dx + dz * dz);
        float nx = largo < 1e-4f ? GRUESO / 2f : dz / largo * GRUESO / 2f, nz = largo < 1e-4f ? 0f : dx / largo * GRUESO / 2f;
        int luz = LevelRenderer.getLightColor(e.level(), BlockPos.containing(yo.add(a.add(b).scale(0.5))));
        // cuelga mas cuanto mas largo y menos tenso (clavado o en vuelo largo)
        float cuelga = Math.min(1.6f, largo * 0.06f);
        for (int pase = 0; pase < 2; pase++) {
            for (int i = 0; i <= 24; i++) {
                int k = pase == 0 ? i : 24 - i;
                float f = k / 24f;
                float x = (float) a.x + dx * f, z = (float) a.z + dz * f;
                float y = (float) a.y + dy * f - cuelga * 4f * f * (1f - f);
                float tono = (k % 2 == (pase == 0 ? 0 : 1)) ? 0.72f : 1f;
                float r = 0.46f * tono, g = 0.38f * tono, bl = 0.24f * tono;
                if (pase == 0) {
                    vc.vertex(m, x - nx, y + GRUESO, z + nz).color(r, g, bl, 1f).uv2(luz).endVertex();
                    vc.vertex(m, x + nx, y, z - nz).color(r, g, bl, 1f).uv2(luz).endVertex();
                } else {
                    vc.vertex(m, x - nx, y, z + nz).color(r, g, bl, 1f).uv2(luz).endVertex();
                    vc.vertex(m, x + nx, y + GRUESO, z - nz).color(r, g, bl, 1f).uv2(luz).endVertex();
                }
            }
        }
    }

    private static void v(VertexConsumer vc, Matrix4f m, Matrix3f n, float x, float y, float z, float u, float v, int luz) {
        vc.vertex(m, x, y, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(luz).normal(n, 0f, 1f, 0f).endVertex();
    }

    @Override
    public boolean shouldRender(ArponBallenero e, net.minecraft.client.renderer.culling.Frustum f, double x, double y, double z) {
        // la cuerda llega a Brann aunque el arpon este fuera de la vista
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(ArponBallenero e) {
        return TEX;
    }
}
