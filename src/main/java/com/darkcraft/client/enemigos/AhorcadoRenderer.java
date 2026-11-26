package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Ahorcado;
import com.darkcraft.enemigos.AhorcadoAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * El dibujante del ahorcado: el de los enemigos de siempre y, mientras lanza el lazo o tira de quien ha enganchado, la
 * soga desde su mano hasta el (como la correa del juego, con su comba, pero mas gruesa y del color del canamo).
 */
public class AhorcadoRenderer extends EnemigoRenderer<Ahorcado, AhorcadoModelo> {
    public AhorcadoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new AhorcadoModelo(ctx.bakeLayer(AhorcadoModelo.CAPA)), "ahorcado", 2, 0.5f, AhorcadoAnims.ESCALA);
    }

    @Override
    public boolean shouldRender(Ahorcado e, Frustum f, double x, double y, double z) {
        if (super.shouldRender(e, f, x, y, z)) return true;
        Entity t = e.lazo();
        return t != null && f.isVisible(t.getBoundingBoxForCulling());
    }

    @Override
    public void render(Ahorcado e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        super.render(e, yaw, pt, ps, buf, luz);
        Entity t = e.lazo();
        if (t == null || e.accionVista() != AhorcadoAnims.LAZO) return;
        // la mano: delante y a la derecha, a la altura del hombro (la del lanzamiento)
        float giro = Mth.lerp(pt, e.yBodyRotO, e.yBodyRot) * Mth.DEG_TO_RAD;
        double fx = -Mth.sin(giro), fz = Mth.cos(giro);
        Vec3 mano = new Vec3(fx * 0.9 - fz * 0.3, 1.75, fz * 0.9 + fx * 0.3);
        Vec3 yo = e.getPosition(pt);
        Vec3 alla = t.getPosition(pt).add(0, t.getBbHeight() * 0.62, 0).subtract(yo);
        // mientras vuela (los ticks 14 a 18 de la accion) la punta va de la mano a quien persigue
        float vuela = (e.tickCount + pt - e.accionDesde - 14f) / 4f;
        if (vuela < 0f) return;
        if (vuela < 1f) alla = mano.add(alla.subtract(mano).scale(vuela));
        soga(e, t, pt, ps, buf, mano, alla);
    }

    /** La soga entre dos puntos (respecto al ahorcado): dos tiras cruzadas con comba, en 24 trozos. */
    private void soga(Ahorcado e, Entity t, float pt, PoseStack ps, MultiBufferSource buf, Vec3 a, Vec3 b) {
        ps.pushPose();
        ps.translate(a.x, a.y, a.z);
        float dx = (float) (b.x - a.x), dy = (float) (b.y - a.y), dz = (float) (b.z - a.z);
        VertexConsumer vc = buf.getBuffer(RenderType.leash());
        Matrix4f m = ps.last().pose();
        float ancho = 0.055f;
        float inv = Mth.invSqrt(dx * dx + dz * dz + 1e-6f) * ancho / 2f;
        float wx = dz * inv, wz = dx * inv;
        BlockPos pa = BlockPos.containing(e.getEyePosition(pt)), pb = BlockPos.containing(t.getEyePosition(pt));
        int bl0 = e.level().getBrightness(LightLayer.BLOCK, pa), bl1 = e.level().getBrightness(LightLayer.BLOCK, pb);
        int sl0 = e.level().getBrightness(LightLayer.SKY, pa), sl1 = e.level().getBrightness(LightLayer.SKY, pb);
        for (int i = 0; i <= 24; i++) par(vc, m, dx, dy, dz, bl0, bl1, sl0, sl1, ancho, ancho, wx, wz, i, false);
        for (int i = 24; i >= 0; i--) par(vc, m, dx, dy, dz, bl0, bl1, sl0, sl1, ancho, 0f, wx, wz, i, true);
        ps.popPose();
    }

    private static void par(VertexConsumer vc, Matrix4f m, float dx, float dy, float dz, int bl0, int bl1, int sl0, int sl1, float desplaza,
                            float ancho, float wx, float wz, int i, boolean otra) {
        float f = i / 24f;
        int luz = LightTexture.pack((int) Mth.lerp(f, bl0, bl1), (int) Mth.lerp(f, sl0, sl1));
        // canamo: un trozo claro y otro oscuro (el torcido)
        float k = i % 2 == (otra ? 1 : 0) ? 0.72f : 1f;
        float r = 0.52f * k, g = 0.42f * k, bz = 0.27f * k;
        float x = dx * f;
        // la comba: cuelga un poco por el medio
        float y = (dy > 0f ? dy * f * f : dy - dy * (1f - f) * (1f - f)) - 0.35f * f * (1f - f) * 4f * 0.25f;
        float z = dz * f;
        vc.vertex(m, x - wx, y + desplaza, z + wz).color(r, g, bz, 1f).uv2(luz).endVertex();
        vc.vertex(m, x + wx, y + ancho - desplaza, z - wz).color(r, g, bz, 1f).uv2(luz).endVertex();
    }
}
