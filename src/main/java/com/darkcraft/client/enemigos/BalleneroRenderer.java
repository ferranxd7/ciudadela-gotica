package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Ballenero;
import com.darkcraft.enemigos.BalleneroAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * El dibujante de Brann, el Ballenero: el de los enemigos de siempre, a su escala (un gigante: 1,55); la llama del farol y los
 * ojos (la capa de brillo) tiemblan un poco y, al caer, se van apagando mientras se queda de rodillas agarrado al arpon.
 */
public class BalleneroRenderer extends EnemigoRenderer<Ballenero, BalleneroModelo> {
    public static final float ESCALA = 1.55f;

    public BalleneroRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new BalleneroModelo(ctx.bakeLayer(BalleneroModelo.CAPA)), "ballenero", 1, 1.2f, ESCALA);
    }

    @Override
    public void render(Ballenero e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (e.accionVista() == BalleneroAnims.MORIR || e.isDeadOrDying()) {
            float t = (e.tickCount + pt - e.accionDesde) / (float) BalleneroAnims.TICKS[BalleneroAnims.MORIR];
            brillo = Mth.clamp(1f - (t - 0.9f) / 0.8f, 0f, 1f);
        } else brillo = 0.86f + 0.14f * Mth.sin((e.tickCount + pt) * 0.9f) * Mth.sin((e.tickCount + pt) * 0.23f);
        super.render(e, yaw, pt, ps, buf, luz);
    }
}
