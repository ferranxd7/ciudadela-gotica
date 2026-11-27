package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Pontifice;
import com.darkcraft.enemigos.PontificeAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * El dibujante de Anselmo, el Pontifice Sangrante: el de los enemigos de siempre, a su escala (alto y enjuto: 1,45); la luz roja
 * de los ojos y la sangre del caliz (la capa de brillo) laten despacio y, al caer, se van apagando mientras se queda de rodillas.
 */
public class PontificeRenderer extends EnemigoRenderer<Pontifice, PontificeModelo> {
    public static final float ESCALA = 1.45f;

    public PontificeRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new PontificeModelo(ctx.bakeLayer(PontificeModelo.CAPA)), "pontifice", 1, 1.0f, ESCALA);
    }

    @Override
    public void render(Pontifice e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (e.accionVista() == PontificeAnims.MORIR || e.isDeadOrDying()) {
            float t = (e.tickCount + pt - e.accionDesde) / (float) PontificeAnims.TICKS[PontificeAnims.MORIR];
            brillo = Mth.clamp(1f - (t - 0.9f) / 0.8f, 0f, 1f);
        } else brillo = 0.8f + 0.2f * Mth.sin((e.tickCount + pt) * 0.12f);
        super.render(e, yaw, pt, ps, buf, luz);
    }
}
