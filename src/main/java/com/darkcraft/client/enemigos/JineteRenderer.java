package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Jinete;
import com.darkcraft.enemigos.JineteAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * El dibujante del Jinete Nocturno: el de los enemigos de siempre a su escala (1,15), casi opaco y respirando un poco; al
 * aparecer se va haciendo visible desde la niebla, al amanecer se apaga y al morir se deshace al final de su caida.
 */
public class JineteRenderer extends EnemigoRenderer<Jinete, JineteModelo> {
    public static final float ESCALA = 1.15f;

    public JineteRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new JineteModelo(ctx.bakeLayer(JineteModelo.CAPA)), "jinete", 1, 1.1f, ESCALA);
    }

    /** Lo opaco que se ve ahora (0-1). */
    public static float alfa(Jinete e, float pt) {
        float base = 0.9f + 0.05f * Mth.sin((e.tickCount + pt) * 0.07f);
        if (e.muriendo()) return base * Mth.clamp(1f - (e.deathTime + pt - 68f) / 22f, 0f, 1f);
        int a = e.accionVista();
        float t = e.accionDesde < 0 ? 0f : e.tickCount + pt - e.accionDesde;
        if (a == JineteAnims.APARECE) return base * Mth.clamp(t / 22f, 0f, 1f);
        if (a == JineteAnims.DESAPARECE) return base * Mth.clamp(1f - (t - 8f) / 30f, 0f, 1f);
        return base;
    }

    @Override
    public void render(Jinete e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        float a = alfa(e, pt);
        if (a <= 0.02f) return;
        getModel().alfa = a;
        brillo = Math.min(1f, a * 1.06f);
        super.render(e, yaw, pt, ps, buf, luz);
    }
}
