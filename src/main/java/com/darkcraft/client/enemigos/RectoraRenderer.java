package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Rectora;
import com.darkcraft.enemigos.RectoraAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * El dibujante de la Rectora: el de los enemigos de siempre (a su escala), medio transparente (respira despacio); se apaga al
 * parpadear y se enciende donde aparece; las estrellas de su manto laten (la capa de brillo) y, al caer, se va deshaciendo
 * despues de mirar arriba por ultima vez.
 */
public class RectoraRenderer extends EnemigoRenderer<Rectora, RectoraModelo> {
    public RectoraRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new RectoraModelo(ctx.bakeLayer(RectoraModelo.CAPA)), "rectora", 1, 0.0f, RectoraAnims.ESCALA);
    }

    /** Lo opaco que se ve ahora (0-1). */
    public static float alfa(Rectora e, float pt) {
        float base = 0.84f + 0.06f * Mth.sin((e.tickCount + pt) * 0.05f);
        int a = e.accionVista();
        float t = e.accionDesde < 0 ? 0f : e.tickCount + pt - e.accionDesde;
        if (a == RectoraAnims.PARPADEO) {
            // se apaga del tick 3 al 10 y se enciende del 11 al 17
            if (t < 3f) return base;
            if (t < 11f) return base * Mth.clamp(1f - (t - 3f) / 7f, 0f, 1f);
            return base * Mth.clamp((t - 11f) / 6f, 0f, 1f);
        }
        if (e.isDeadOrDying()) return base * Mth.clamp(1f - (e.deathTime + pt - 80f) / 38f, 0f, 1f);
        return base;
    }

    @Override
    public void render(Rectora e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        float a = alfa(e, pt);
        if (a <= 0.02f) return;
        getModel().alfa = a;
        brillo = a * (0.8f + 0.2f * Mth.sin((e.tickCount + pt) * 0.13f));
        super.render(e, yaw, pt, ps, buf, luz);
    }
}
