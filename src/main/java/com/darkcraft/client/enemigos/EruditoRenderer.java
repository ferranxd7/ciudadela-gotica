package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Erudito;
import com.darkcraft.enemigos.EruditoAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * El erudito: medio transparente (la luz respira), se apaga al parpadear y se vuelve a encender donde aparece, y al morir
 * se va deshaciendo mientras sube. El custodio es mas grande.
 */
public class EruditoRenderer extends EnemigoRenderer<Erudito, EruditoModelo> {
    public EruditoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new EruditoModelo(ctx.bakeLayer(EruditoModelo.CAPA)), "erudito", 3, 0.0f, 1.0f);
    }

    /** Lo opaco que se ve ahora (0-1). */
    public static float alfa(Erudito e, float pt) {
        float base = 0.66f + 0.08f * Mth.sin((e.tickCount + pt) * 0.07f + e.getId());
        int a = e.accionVista();
        float t = e.accionDesde < 0 ? 0f : e.tickCount + pt - e.accionDesde;
        if (a == EruditoAnims.PARPADEO) {
            // se apaga del tick 3 al 10 y se enciende del 11 al 17
            if (t < 3f) return base;
            if (t < 11f) return base * Mth.clamp(1f - (t - 3f) / 7f, 0f, 1f);
            return base * Mth.clamp((t - 11f) / 6f, 0f, 1f);
        }
        if (a == EruditoAnims.MORIR) return base * Mth.clamp(1f - (t - 8f) / 34f, 0f, 1f);
        if (e.muriendo()) return base * Mth.clamp(1f - (e.deathTime + pt - 8f) / 34f, 0f, 1f);
        return base;
    }

    @Override
    protected void scale(Erudito e, PoseStack ps, float pt) {
        if (e.variante() == Erudito.CUSTODIO) ps.scale(1.25f, 1.25f, 1.25f);
    }

    @Override
    public void render(Erudito e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        float a = alfa(e, pt);
        if (a <= 0.02f) return;
        getModel().alfa = a;
        brillo = a;
        super.render(e, yaw, pt, ps, buf, luz);
    }
}
