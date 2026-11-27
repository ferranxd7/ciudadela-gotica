package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Penitente;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** El dibujante del penitente: el de los enemigos de siempre (el portador, mas grande). */
public class PenitenteRenderer extends EnemigoRenderer<Penitente, PenitenteModelo> {
    public PenitenteRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new PenitenteModelo(ctx.bakeLayer(PenitenteModelo.CAPA)), "penitente", 3, 0.5f, 1.0f);
    }

    @Override
    protected void scale(Penitente e, PoseStack ps, float pt) {
        super.scale(e, ps, pt);
        if (e.variante() == Penitente.PORTADOR) ps.scale(1.25f, 1.25f, 1.25f);
    }
}
