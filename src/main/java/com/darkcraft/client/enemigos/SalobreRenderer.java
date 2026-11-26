package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Salobre;
import com.darkcraft.enemigos.SalobreAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * El dibujante del salobre: el de los enemigos de siempre (el salinero, mas grande) y, mientras espera enterrado en la costra
 * de sal ({@code TUMBADO}), el cuerpo un poco hundido (solo asoma); al alzarse sale en el primer medio segundo.
 */
public class SalobreRenderer extends EnemigoRenderer<Salobre, SalobreModelo> {
    /** Lo que se hunde en la sal, enterrado (bloques). */
    private static final double HUNDIDO = 0.22;

    public SalobreRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new SalobreModelo(ctx.bakeLayer(SalobreModelo.CAPA)), "salobre", 3, 0.5f, 1.0f);
    }

    @Override
    protected void scale(Salobre e, PoseStack ps, float pt) {
        super.scale(e, ps, pt);
        if (e.variante() == Salobre.SALINERO) ps.scale(1.15f, 1.15f, 1.15f);
    }

    /** Lo que se baja el cuerpo: enterrado, hundido; al alzarse, de vuelta arriba en los primeros 10 ticks. */
    static double bajada(Salobre e, float pt) {
        int a = e.accionVista();
        if (a == 0) return e.postura() == Enemigo.TUMBADO ? HUNDIDO : 0;
        if (a == SalobreAnims.ALZARSE && e.accionDesde >= 0) {
            float t = (e.tickCount + pt - e.accionDesde) / 10f;
            if (t >= 1f) return 0;
            float k = 1f - t;
            return HUNDIDO * k * k * (3 - 2 * k);
        }
        return 0;
    }

    @Override
    public void render(Salobre e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        double b = bajada(e, pt);
        if (b <= 0) {
            super.render(e, yaw, pt, ps, buf, luz);
            return;
        }
        ps.pushPose();
        ps.translate(0, -b, 0);
        super.render(e, yaw, pt, ps, buf, luz);
        ps.popPose();
    }
}
