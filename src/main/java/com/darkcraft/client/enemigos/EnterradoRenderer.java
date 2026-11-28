package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Enterrado;
import com.darkcraft.enemigos.EnterradoAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * El dibujante del enterrado: el de los enemigos de siempre, el cargador mas grande ({@link Enterrado#GRANDE}) y, mientras espera
 * enterrado en los escombros ({@code TUMBADO}), el cuerpo un poco hundido (asoma del monton); al alzarse sale en el primer medio
 * segundo.
 */
public class EnterradoRenderer extends EnemigoRenderer<Enterrado, EnterradoModelo> {
    /** Lo que se hunde en los escombros, enterrado (bloques). */
    private static final double HUNDIDO = 0.3;

    public EnterradoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new EnterradoModelo(ctx.bakeLayer(EnterradoModelo.CAPA)), "enterrado", 3, 0.5f, 1.0f);
    }

    @Override
    protected void scale(Enterrado e, PoseStack ps, float pt) {
        super.scale(e, ps, pt);
        if (e.variante() == Enterrado.CARGADOR) ps.scale(Enterrado.GRANDE, Enterrado.GRANDE, Enterrado.GRANDE);
    }

    static double bajada(Enterrado e, float pt) {
        int a = e.accionVista();
        if (a == 0) return e.postura() == Enemigo.TUMBADO ? HUNDIDO : 0;
        if (a == EnterradoAnims.ALZARSE && e.accionDesde >= 0) {
            float t = (e.tickCount + pt - e.accionDesde) / 10f;
            if (t >= 1f) return 0;
            float k = 1f - t;
            return HUNDIDO * k * k * (3 - 2 * k);
        }
        return 0;
    }

    @Override
    public void render(Enterrado e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
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
