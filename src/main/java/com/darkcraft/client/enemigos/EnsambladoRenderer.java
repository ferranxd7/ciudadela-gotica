package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Ensamblado;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** El dibujante del ensamblado: el de los enemigos de siempre y el coloso mas grande ({@link Ensamblado#GRANDE}). */
public class EnsambladoRenderer extends EnemigoRenderer<Ensamblado, EnsambladoModelo> {
    public EnsambladoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new EnsambladoModelo(ctx.bakeLayer(EnsambladoModelo.CAPA)), "ensamblado", 3, 0.5f, 1.0f);
    }

    @Override
    protected void scale(Ensamblado e, PoseStack ps, float pt) {
        super.scale(e, ps, pt);
        if (e.variante() == Ensamblado.COLOSO) ps.scale(Ensamblado.GRANDE, Ensamblado.GRANDE, Ensamblado.GRANDE);
    }
}
