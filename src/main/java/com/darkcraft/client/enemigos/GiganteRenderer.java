package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Gigante;
import com.darkcraft.enemigos.GiganteAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * El dibujante de los gigantes errantes: el de los enemigos de siempre a su escala (1,8: el modelo esta al doble de lo normal,
 * unos ocho bloques), con su sombra grande; la luz de los ojos (y de las lapas de Ulfgar) se apaga al caer de bruces.
 */
public class GiganteRenderer extends EnemigoRenderer<Gigante, GiganteModelo> {
    public static final float ESCALA = 1.8f;

    public GiganteRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new GiganteModelo(ctx.bakeLayer(GiganteModelo.CAPA)), "gigante", 3, 2.6f, ESCALA);
    }

    @Override
    public void render(Gigante e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (e.accionVista() == GiganteAnims.MORIR || e.isDeadOrDying()) {
            float t = (e.tickCount + pt - e.accionDesde) / (float) GiganteAnims.TICKS[GiganteAnims.MORIR];
            brillo = Mth.clamp(1f - (t - 0.78f) / 0.12f, 0f, 1f);
        } else brillo = 0.85f + 0.1f * Mth.sin((e.tickCount + pt) * 0.05f);
        super.render(e, yaw, pt, ps, buf, luz);
    }
}
