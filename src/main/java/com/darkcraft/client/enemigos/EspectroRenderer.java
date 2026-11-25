package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Espectro;
import com.darkcraft.enemigos.EspectroAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * El espectro: medio transparente (y respira: la luz sube y baja despacio), se apaga al desvanecerse y se vuelve a
 * encender donde aparece, y al morir se va deshaciendo mientras sube.
 */
public class EspectroRenderer extends EnemigoRenderer<Espectro, EspectroModelo> {
    public EspectroRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new EspectroModelo(ctx.bakeLayer(EspectroModelo.CAPA)), "espectro", 3, 0.0f, 1.0f);
    }

    /** Lo opaco que se ve ahora (0-1). */
    public static float alfa(Espectro e, float pt) {
        float base = 0.62f + 0.08f * Mth.sin((e.tickCount + pt) * 0.08f + e.getId());
        int a = e.accionVista();
        float t = e.accionDesde < 0 ? 0f : e.tickCount + pt - e.accionDesde;
        if (a == EspectroAnims.DESVANECERSE) {
            // se apaga del tick 3 al 11 y se enciende del 12 al 19
            if (t < 3f) return base;
            if (t < 12f) return base * Mth.clamp(1f - (t - 3f) / 8f, 0f, 1f);
            return base * Mth.clamp((t - 12f) / 7f, 0f, 1f);
        }
        if (a == EspectroAnims.MORIR) return base * Mth.clamp(1f - (t - 8f) / 34f, 0f, 1f);
        if (e.muriendo()) return base * Mth.clamp(1f - (e.deathTime + pt - 8f) / 34f, 0f, 1f);
        return base;
    }

    @Override
    public void render(Espectro e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        float a = alfa(e, pt);
        if (a <= 0.02f) return;
        getModel().alfa = a;
        brillo = a;
        super.render(e, yaw, pt, ps, buf, luz);
    }
}
