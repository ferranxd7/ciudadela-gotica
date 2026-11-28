package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Galen;
import com.darkcraft.enemigos.GalenAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * El dibujante de Maese Galen: el de los enemigos de siempre, a su escala (alto y flaco: 1,35) y con sus dos texturas (antes y despues
 * de beberse el elixir); la capa de brillo (las gafas, el elixir de su alambique y de la jeringa y, bebido, el cristal) late despacio,
 * mas deprisa bebido; al caer se va apagando.
 */
public class GalenRenderer extends EnemigoRenderer<Galen, GalenModelo> {
    public static final float ESCALA = 1.35f;

    public GalenRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new GalenModelo(ctx.bakeLayer(GalenModelo.CAPA)), "galen", 2, 0.9f, ESCALA);
    }

    @Override
    public void render(Galen e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (e.accionVista() == GalenAnims.MORIR || e.isDeadOrDying()) {
            float t = (e.tickCount + pt - e.accionDesde) / (float) GalenAnims.TICKS[GalenAnims.MORIR];
            brillo = Mth.clamp(1f - (t - 0.5f) / 0.9f, 0.15f, 1f);
        } else if (e.bebido()) brillo = 0.84f + 0.16f * Mth.sin((e.tickCount + pt) * 0.25f);
        else brillo = 0.75f + 0.15f * Mth.sin((e.tickCount + pt) * 0.09f);
        super.render(e, yaw, pt, ps, buf, luz);
    }
}
