package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Insomne;
import com.darkcraft.enemigos.InsomneAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * El dibujante del Insomne: el de los enemigos de siempre y el escudero algo mas grande ({@link Insomne#GRANDE}). Los ojos (la capa
 * de brillo) no parpadean nunca; al morir se le cierran poco a poco (por fin duerme).
 */
public class InsomneRenderer extends EnemigoRenderer<Insomne, InsomneModelo> {
    public InsomneRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new InsomneModelo(ctx.bakeLayer(InsomneModelo.CAPA)), "insomne", 3, 0.5f, 1.0f);
    }

    @Override
    public void render(Insomne e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (e.accionVista() == InsomneAnims.MORIR || e.isDeadOrDying()) {
            float t = (e.tickCount + pt - e.accionDesde) / (float) InsomneAnims.TICKS[InsomneAnims.MORIR];
            brillo = Mth.clamp(1f - (t - 0.45f) / 0.5f, 0f, 1f);
        } else brillo = 1f;
        super.render(e, yaw, pt, ps, buf, luz);
    }

    @Override
    protected void scale(Insomne e, PoseStack ps, float pt) {
        super.scale(e, ps, pt);
        if (e.variante() == Insomne.ESCUDERO) ps.scale(Insomne.GRANDE, Insomne.GRANDE, Insomne.GRANDE);
    }
}
