package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Calcinado;
import com.darkcraft.enemigos.CalcinadoAnims;
import com.darkcraft.enemigos.Enemigo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * El dibujante del calcinado: el de los enemigos de siempre y, mientras espera enterrado en la ceniza ({@code TUMBADO}), el
 * cuerpo un poco hundido (asoma del monton); al alzarse sale en el primer medio segundo.
 */
public class CalcinadoRenderer extends EnemigoRenderer<Calcinado, CalcinadoModelo> {
    /** Lo que se hunde en la ceniza, enterrado (bloques). */
    private static final double HUNDIDO = 0.25;

    public CalcinadoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new CalcinadoModelo(ctx.bakeLayer(CalcinadoModelo.CAPA)), "calcinado", 3, 0.5f, 1.05f);
    }

    /** Lo que se baja el cuerpo: enterrado, hundido; al alzarse, de vuelta arriba en los primeros 10 ticks. */
    static double bajada(Calcinado e, float pt) {
        int a = e.accionVista();
        if (a == 0) return e.postura() == Enemigo.TUMBADO ? HUNDIDO : 0;
        if (a == CalcinadoAnims.ALZARSE && e.accionDesde >= 0) {
            float t = (e.tickCount + pt - e.accionDesde) / 10f;
            if (t >= 1f) return 0;
            float k = 1f - t;
            return HUNDIDO * k * k * (3 - 2 * k);
        }
        return 0;
    }

    @Override
    public void render(Calcinado e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
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
