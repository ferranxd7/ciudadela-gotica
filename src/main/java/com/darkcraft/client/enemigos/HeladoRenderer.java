package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Helado;
import com.darkcraft.enemigos.HeladoAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * El dibujante del helado: el de los enemigos de siempre (el penitente, algo mas grande) y, mientras espera enterrado en la
 * nieve ({@code TUMBADO}), el cuerpo un poco hundido (asoma de la nieve); al alzarse sale en el primer medio segundo.
 */
public class HeladoRenderer extends EnemigoRenderer<Helado, HeladoModelo> {
    /** Lo que se hunde en la nieve, enterrado (bloques). */
    private static final double HUNDIDO = 0.2;

    public HeladoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HeladoModelo(ctx.bakeLayer(HeladoModelo.CAPA)), "helado", 3, 0.5f, 1.1f);
    }

    @Override
    protected void scale(Helado e, PoseStack ps, float pt) {
        super.scale(e, ps, pt);
        if (e.variante() == Helado.PENITENTE) ps.scale(1.06f, 1.06f, 1.06f);
    }

    /** Lo que se baja el cuerpo: enterrado, hundido; al alzarse, de vuelta arriba en los primeros 10 ticks. */
    static double bajada(Helado e, float pt) {
        int a = e.accionVista();
        if (a == 0) return e.postura() == Enemigo.TUMBADO ? HUNDIDO : 0;
        if (a == HeladoAnims.ALZARSE && e.accionDesde >= 0) {
            float t = (e.tickCount + pt - e.accionDesde) / 10f;
            if (t >= 1f) return 0;
            float k = 1f - t;
            return HUNDIDO * k * k * (3 - 2 * k);
        }
        return 0;
    }

    @Override
    public void render(Helado e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
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
