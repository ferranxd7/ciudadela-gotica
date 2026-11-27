package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Isolde;
import com.darkcraft.enemigos.IsoldeAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * La reina Isolde: casi opaca (respira un poco), se apaga al desvanecerse y se vuelve a encender donde aparece (detras de su
 * presa), y al caer, mientras dice su ultima frase de rodillas en el aire, se queda; al acabar se va apagando mientras sube.
 */
public class IsoldeRenderer extends EnemigoRenderer<Isolde, IsoldeModelo> {
    public static final float ESCALA = 1.5f;

    public IsoldeRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new IsoldeModelo(ctx.bakeLayer(IsoldeModelo.CAPA)), "isolde", 2, 0.7f, ESCALA);
    }

    /** Lo que se apaga y sube al irse (0 hasta que empieza a irse; 1 al acabar). */
    public static float yendose(Isolde e, float pt) {
        if (!e.muriendo()) return 0f;
        int fin = e.fin();
        return Mth.clamp((e.deathTime + pt - (fin - 40)) / 38f, 0f, 1f);
    }

    /** Lo opaco que se ve ahora (0-1). */
    public static float alfa(Isolde e, float pt) {
        float base = 0.88f + 0.06f * Mth.sin((e.tickCount + pt) * 0.07f);
        if (e.muriendo()) return base * (1f - yendose(e, pt));
        if (e.accionVista() == IsoldeAnims.DESVANECE && e.accionDesde >= 0) {
            float t = e.tickCount + pt - e.accionDesde;
            float apaga = Isolde.APAGA - 1, aparece = Isolde.APARECE - 1;
            if (t < apaga - 4f) return base;
            if (t < apaga) return base * Mth.clamp((apaga - t) / 4f, 0f, 1f);
            if (t < aparece) return 0f;
            return base * Mth.clamp((t - aparece) / 4f, 0f, 1f);
        }
        return base;
    }

    @Override
    public void render(Isolde e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        float a = alfa(e, pt);
        if (a <= 0.02f) return;
        getModel().alfa = a;
        brillo = a;
        float sube = yendose(e, pt) * 1.6f;
        ps.pushPose();
        if (sube > 0f) ps.translate(0, sube, 0);
        super.render(e, yaw, pt, ps, buf, luz);
        ps.popPose();
    }
}
