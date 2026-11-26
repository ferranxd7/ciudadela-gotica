package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Barquero;
import com.darkcraft.enemigos.BarqueroAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * El dibujante del Barquero: el de los enemigos de siempre (a su escala); al morir, el fuego de la linterna y de los ojos se
 * apaga poco a poco mientras cae (la capa de brillo).
 */
public class BarqueroRenderer extends EnemigoRenderer<Barquero, BarqueroModelo> {
    public BarqueroRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new BarqueroModelo(ctx.bakeLayer(BarqueroModelo.CAPA)), "barquero", 1, 1.0f, BarqueroAnims.ESCALA);
    }

    @Override
    public void render(Barquero e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (e.accionVista() == BarqueroAnims.MORIR) {
            float t = (e.tickCount + pt - e.accionDesde) / (float) BarqueroAnims.TICKS[BarqueroAnims.MORIR];
            brillo = Mth.clamp(1f - (t - 0.25f) / 0.6f, 0f, 1f);
        } else brillo = 1f;
        super.render(e, yaw, pt, ps, buf, luz);
    }
}
