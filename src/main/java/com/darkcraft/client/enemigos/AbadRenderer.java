package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Abad;
import com.darkcraft.enemigos.AbadAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * El dibujante del Abad de la Cumbre: el de los enemigos de siempre (a su escala); al morir, la luz de los ojos, de la mitra
 * y del cristal del baculo se apaga poco a poco mientras se queda de rodillas (la capa de brillo).
 */
public class AbadRenderer extends EnemigoRenderer<Abad, AbadModelo> {
    public AbadRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new AbadModelo(ctx.bakeLayer(AbadModelo.CAPA)), "abad", 1, 1.4f, AbadAnims.ESCALA);
    }

    @Override
    public void render(Abad e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (e.accionVista() == AbadAnims.MORIR || e.isDeadOrDying()) {
            float t = (e.tickCount + pt - e.accionDesde) / (float) AbadAnims.TICKS[AbadAnims.MORIR];
            brillo = Mth.clamp(1f - (t - 0.4f) / 0.7f, 0f, 1f);
        } else brillo = 1f;
        super.render(e, yaw, pt, ps, buf, luz);
    }
}
