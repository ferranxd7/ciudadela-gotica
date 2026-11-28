package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Sigrun;
import com.darkcraft.enemigos.SigrunAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Sigrun: a quien ya acabo su duelo no se le dibuja ({@link Sigrun#OCULTA}); se apaga al deshacerse en nieve (al final de su caida o de
 * su marcha) y se enciende al volver a su puesto.
 */
public class SigrunRenderer extends EnemigoRenderer<Sigrun, SigrunModelo> {
    public SigrunRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new SigrunModelo(ctx.bakeLayer(SigrunModelo.CAPA)), "sigrun", 1, 0.55f, SigrunAnims.ESCALA);
    }

    @Override
    public boolean shouldRender(Sigrun e, Frustum f, double x, double y, double z) {
        return !Sigrun.OCULTA.test(e) && super.shouldRender(e, f, x, y, z);
    }

    @Override
    public void render(Sigrun e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (Sigrun.OCULTA.test(e)) return;
        float a = e.alfa(pt);
        if (a <= 0.02f) return;
        getModel().alfa = a;
        brillo = a;
        super.render(e, yaw, pt, ps, buf, luz);
    }
}
