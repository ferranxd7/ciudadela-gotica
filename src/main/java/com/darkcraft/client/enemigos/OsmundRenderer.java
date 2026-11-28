package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Osmund;
import com.darkcraft.enemigos.OsmundAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * El dibujante de Fray Osmund: el de los enemigos de siempre, a su escala (alto y flaco: 1,5); la capa de brillo (las almas de las
 * cuencas y de entre las costillas, las velas de la aureola y las brasas del incensario) late despacio, mas fuerte alzado; al caer
 * se va apagando mientras se queda de rodillas.
 */
public class OsmundRenderer extends EnemigoRenderer<Osmund, OsmundModelo> {
    public static final float ESCALA = 1.5f;

    public OsmundRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new OsmundModelo(ctx.bakeLayer(OsmundModelo.CAPA)), "osmund", 1, 1.0f, ESCALA);
    }

    @Override
    public void render(Osmund e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (e.accionVista() == OsmundAnims.MORIR || e.isDeadOrDying()) {
            float t = (e.tickCount + pt - e.accionDesde) / (float) OsmundAnims.TICKS[OsmundAnims.MORIR];
            brillo = Mth.clamp(1f - (t - 0.4f) / 0.8f, 0f, 1f);
        } else if (e.alzado()) brillo = 0.86f + 0.14f * Mth.sin((e.tickCount + pt) * 0.22f);
        else brillo = 0.7f + 0.2f * Mth.sin((e.tickCount + pt) * 0.08f);
        super.render(e, yaw, pt, ps, buf, luz);
    }
}
