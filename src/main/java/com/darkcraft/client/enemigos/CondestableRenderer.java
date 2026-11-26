package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Condestable;
import com.darkcraft.enemigos.CondestableAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * El dibujante del Condestable del Juramento: el de los enemigos de siempre (a su escala); las brasas de la armadura laten
 * despacio (la capa de brillo) y, al caer, se van apagando mientras se queda de rodilla apoyado en el montante.
 */
public class CondestableRenderer extends EnemigoRenderer<Condestable, CondestableModelo> {
    public CondestableRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new CondestableModelo(ctx.bakeLayer(CondestableModelo.CAPA)), "condestable", 1, 1.4f, CondestableAnims.ESCALA);
    }

    @Override
    public void render(Condestable e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (e.accionVista() == CondestableAnims.MORIR || e.isDeadOrDying()) {
            float t = (e.tickCount + pt - e.accionDesde) / (float) CondestableAnims.TICKS[CondestableAnims.MORIR];
            brillo = Mth.clamp(1f - (t - 0.8f) / 0.9f, 0f, 1f);
        } else brillo = 0.82f + 0.18f * Mth.sin((e.tickCount + pt) * 0.11f);
        super.render(e, yaw, pt, ps, buf, luz);
    }
}
