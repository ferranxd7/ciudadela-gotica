package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Destilado;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** El dibujante del destilado: el de los enemigos de siempre y la cuba mas grande ({@link Destilado#GRANDE}). */
public class DestiladoRenderer extends EnemigoRenderer<Destilado, DestiladoModelo> {
    public DestiladoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new DestiladoModelo(ctx.bakeLayer(DestiladoModelo.CAPA)), "destilado", 3, 0.5f, 1.0f);
    }

    @Override
    protected void scale(Destilado e, PoseStack ps, float pt) {
        super.scale(e, ps, pt);
        if (e.variante() == Destilado.CUBA) ps.scale(Destilado.GRANDE, Destilado.GRANDE, Destilado.GRANDE);
    }
}
