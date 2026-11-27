package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Sombra;
import com.darkcraft.enemigos.SombraAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * La Sombra sin Nombre: casi opaca, se apaga en la niebla y se enciende donde aparece, y al irse sube mientras se apaga. Mientras
 * espera atada (y hasta que las rompe al despertar), sus <b>cadenas</b>: de cada muneca y de la espalda a los grilletes de los pilares
 * (se dibujan aqui, con la textura de la cadena del juego, desde donde esta de verdad cada muneca en la animacion). Durante la
 * <b>mirada</b>, el rayo rojo de sus ojos al suelo, que barre la sala (con la misma cuenta que el servidor: {@link Sombra#rumboMirada}).
 */
public class SombraRenderer extends EnemigoRenderer<Sombra, SombraModelo> {
    public static final float ESCALA = 2.4f;
    private static final ResourceLocation CADENA = new ResourceLocation("minecraft", "textures/block/chain.png");
    /** Lo ancho de las cadenas y lo largo de cada tramo de textura (la cadena del juego: 3 de ancho por 16 de largo). */
    private static final float ANCHO = 0.5f, TRAMO = ANCHO * 16f / 3f;
    private static final String[] MANO_D = {"raiz", "cadera", "torso", "brazo_d", "antebrazo_d", "mano_d"},
            MANO_I = {"raiz", "cadera", "torso", "brazo_i", "antebrazo_i", "mano_i"}, TORSO = {"raiz", "cadera", "torso"},
            CABEZA = {"raiz", "cadera", "torso", "cabeza"};
    private static Vec3[] anclas;

    public SombraRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new SombraModelo(ctx.bakeLayer(SombraModelo.CAPA)), "sombra", 2, 1.6f, ESCALA);
    }

    /** Lo opaco que se ve ahora (0-1). */
    public static float alfa(Sombra e, float pt) {
        float base = 0.96f;
        if (e.deathTime > 0) return base * (1f - e.yendose(pt));
        if (e.accionVista() == SombraAnims.NIEBLA && e.accionDesde >= 0) {
            float t = e.tickCount + pt - e.accionDesde;
            float apaga = Sombra.APAGA - 1, aparece = Sombra.APARECE - 1;
            if (t < apaga - 4f) return base;
            if (t < apaga) return base * Mth.clamp((apaga - t) / 4f, 0f, 1f);
            if (t < aparece) return 0f;
            return base * Mth.clamp((t - aparece) / 4f, 0f, 1f);
        }
        return base;
    }

    @Override
    public void render(Sombra e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        float a = alfa(e, pt);
        if (a > 0.02f) {
            getModel().alfa = a;
            brillo = a;
            float sube = e.yendose(pt) * 1.8f;
            ps.pushPose();
            if (sube > 0f) ps.translate(0, sube, 0);
            super.render(e, yaw, pt, ps, buf, luz);
            ps.popPose();
        }
        // (el modelo ya esta puesto con la pose de este fotograma: de ahi salen las munecas y los ojos)
        int accion = e.accionVista();
        float tAccion = e.accionDesde >= 0 ? e.tickCount + pt - e.accionDesde : 0f;
        if (e.encadenada(tAccion, accion) && e.deathTime <= 0) cadenas(e, pt, ps, buf, luz);
        if (accion == SombraAnims.MIRADA && tAccion >= Sombra.MIRADA_DESDE - 1 && tAccion <= Sombra.MIRADA_HASTA + 1) rayo(e, pt, tAccion, ps, buf);
    }

    /** Un punto del modelo (en pixeles, en la pieza del final del camino) en coordenadas del mundo relativas a la entidad. */
    private Vec3 punto(Sombra e, float pt, String[] camino, float px, float py, float pz) {
        PoseStack t = new PoseStack();
        float giro = Mth.rotLerp(pt, e.yBodyRotO, e.yBodyRot);
        t.mulPose(Axis.YP.rotationDegrees(180f - giro));
        t.scale(-1f, -1f, 1f);
        t.scale(ESCALA, ESCALA, ESCALA);
        t.translate(0f, -1.501f, 0f);
        SombraModelo m = getModel();
        m.root().translateAndRotate(t);
        for (String n : camino) m.parte(n).translateAndRotate(t);
        Vector4f v = t.last().pose().transform(new Vector4f(px / 16f, py / 16f, pz / 16f, 1f));
        return new Vec3(v.x(), v.y(), v.z());
    }

    // ------------------------------------------------------------------ las cadenas

    private void cadenas(Sombra e, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (anclas == null) anclas = Sombra.anclas();
        Vec3 donde = e.getPosition(pt);
        Vec3[] desde = {punto(e, pt, MANO_D, 0f, 0.5f, 0f), punto(e, pt, MANO_I, 0f, 0.5f, 0f), punto(e, pt, TORSO, -3f, -11f, 2.4f),
                punto(e, pt, TORSO, 3f, -11f, 2.4f)};
        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(CADENA));
        for (int i = 0; i < 4; i++) {
            Vec3 hasta = anclas[i].subtract(donde);
            // un poco de vaiven (las cadenas tensas tiemblan)
            cadena(ps, vc, desde[i], hasta, luz);
        }
    }

    /** Una cadena de a a b (coordenadas relativas a la entidad): dos planos cruzados, tramo a tramo. */
    private static void cadena(PoseStack ps, VertexConsumer vc, Vec3 a, Vec3 b, int luz) {
        Vec3 d = b.subtract(a);
        double largo = d.length();
        if (largo < 0.1) return;
        Vec3 u = d.scale(1.0 / largo);
        Vec3 ref = Math.abs(u.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 l1 = u.cross(ref).normalize(), l2 = u.cross(l1).normalize();
        Matrix4f m = ps.last().pose();
        Matrix3f n = ps.last().normal();
        int tramos = (int) Math.ceil(largo / TRAMO);
        for (int k = 0; k < tramos; k++) {
            double t0 = k * TRAMO, t1 = Math.min(largo, (k + 1) * TRAMO);
            Vec3 p0 = a.add(u.scale(t0)), p1 = a.add(u.scale(t1));
            float v1 = (float) ((t1 - t0) / TRAMO);
            plano(vc, m, n, p0, p1, l1, l2, 0f, 3f / 16f, v1, luz);
            plano(vc, m, n, p0, p1, l2, l1, 3f / 16f, 6f / 16f, v1, luz);
        }
    }

    private static void plano(VertexConsumer vc, Matrix4f m, Matrix3f n, Vec3 p0, Vec3 p1, Vec3 lado, Vec3 normal, float u0, float u1, float v1, int luz) {
        Vec3 h = lado.scale(ANCHO / 2);
        float nx = (float) normal.x, ny = (float) normal.y, nz = (float) normal.z;
        v(vc, m, n, p0.subtract(h), u0, 0f, nx, ny, nz, luz);
        v(vc, m, n, p0.add(h), u1, 0f, nx, ny, nz, luz);
        v(vc, m, n, p1.add(h), u1, v1, nx, ny, nz, luz);
        v(vc, m, n, p1.subtract(h), u0, v1, nx, ny, nz, luz);
    }

    private static void v(VertexConsumer vc, Matrix4f m, Matrix3f n, Vec3 p, float u, float vv, float nx, float ny, float nz, int luz) {
        vc.vertex(m, (float) p.x, (float) p.y, (float) p.z).color(255, 255, 255, 255).uv(u, vv).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(luz)
                .normal(n, nx, ny, nz).endVertex();
    }

    // ------------------------------------------------------------------ el rayo de la mirada

    private void rayo(Sombra e, float pt, float tAccion, PoseStack ps, MultiBufferSource buf) {
        float rumbo = Mth.rotLerp(pt, e.yBodyRotO, e.yBodyRot);
        float r = Sombra.rumboMirada(rumbo, tAccion);
        Vec3 ojos = punto(e, pt, CABEZA, 0f, -4.6f, -4.4f);
        Vec3 suelo = Sombra.rayoSuelo(Vec3.ZERO, r);
        // se enciende y se apaga en un par de ticks
        float f = Mth.clamp(Math.min(tAccion - (Sombra.MIRADA_DESDE - 1), Sombra.MIRADA_HASTA + 1 - tAccion) / 2f, 0f, 1f);
        if (f <= 0f) return;
        VertexConsumer vc = buf.getBuffer(RenderType.lightning());
        Matrix4f m = ps.last().pose();
        haz(vc, m, ojos, suelo, 0.95f, 0.75f, 0.05f, 0.03f, 0.35f * f);
        haz(vc, m, ojos, suelo, 0.38f, 1.0f, 0.25f, 0.15f, 0.85f * f);
    }

    /** Un haz de luz de a a b: dos planos cruzados, del color dado. */
    private static void haz(VertexConsumer vc, Matrix4f m, Vec3 a, Vec3 b, float ancho, float r, float g, float bl, float al) {
        Vec3 d = b.subtract(a);
        double l = d.length();
        if (l < 0.1) return;
        Vec3 u = d.scale(1.0 / l);
        Vec3 ref = Math.abs(u.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 l1 = u.cross(ref).normalize().scale(ancho / 2), l2 = u.cross(l1).normalize().scale(ancho / 2);
        for (Vec3 h : new Vec3[]{l1, l2}) {
            c(vc, m, a.subtract(h), r, g, bl, al);
            c(vc, m, a.add(h), r, g, bl, al);
            c(vc, m, b.add(h), r, g, bl, al);
            c(vc, m, b.subtract(h), r, g, bl, al);
        }
    }

    private static void c(VertexConsumer vc, Matrix4f m, Vec3 p, float r, float g, float b, float a) {
        vc.vertex(m, (float) p.x, (float) p.y, (float) p.z).color(r, g, b, a).endVertex();
    }

    @Override
    public boolean shouldRender(Sombra e, net.minecraft.client.renderer.culling.Frustum f, double x, double y, double z) {
        // con sus cadenas o su rayo se sale mucho de su caja
        return true;
    }
}
