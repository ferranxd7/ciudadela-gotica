package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Capataz;
import com.darkcraft.enemigos.CapatazAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * El dibujante de Hakon, el Capataz: el de los enemigos de siempre, a su escala (grande y ancho: 1,5); la capa de brillo (el candil,
 * el farol, el ojo y los cristales de la veta) late despacio; con la veta bebida late mas fuerte y mas deprisa; al caer, su farol se
 * va apagando mientras se queda de rodillas.
 */
public class CapatazRenderer extends EnemigoRenderer<Capataz, CapatazModelo> {
    public static final float ESCALA = 1.5f;

    public CapatazRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new CapatazModelo(ctx.bakeLayer(CapatazModelo.CAPA)), "capataz", 1, 1.1f, ESCALA);
    }

    @Override
    public void render(Capataz e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (e.accionVista() == CapatazAnims.MORIR || e.isDeadOrDying()) {
            float t = (e.tickCount + pt - e.accionDesde) / (float) CapatazAnims.TICKS[CapatazAnims.MORIR];
            brillo = Mth.clamp(1f - (t - 0.5f) / 0.9f, 0f, 1f);
        } else if (e.veta()) brillo = 0.85f + 0.15f * Mth.sin((e.tickCount + pt) * 0.3f);
        else brillo = 0.72f + 0.2f * Mth.sin((e.tickCount + pt) * 0.1f);
        super.render(e, yaw, pt, ps, buf, luz);
    }
}
