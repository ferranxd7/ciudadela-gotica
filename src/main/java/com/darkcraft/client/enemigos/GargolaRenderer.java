package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Gargola;
import com.darkcraft.enemigos.GargolaAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * La gargola: la textura de piedra y su capa de brillo (los ojos y las grietas rojas). De estatua, en su pedestal, el brillo esta
 * apagado (es piedra y nada mas); al despertar se le encienden los ojos poco a poco y al morir se le van apagando.
 */
public class GargolaRenderer extends EnemigoRenderer<Gargola, GargolaModelo> {
    public GargolaRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new GargolaModelo(ctx.bakeLayer(GargolaModelo.CAPA)), "gargola", 1, 0.7f, 1.05f);
    }

    /** Lo que luce ahora (0 = piedra apagada, 1 = despierta). */
    public static float brillo(Gargola e, float pt) {
        if (e.muriendo()) return Mth.clamp(1f - (e.deathTime + pt) / 34f, 0f, 1f);
        if (e.accionVista() == GargolaAnims.DESPIERTA) return Mth.clamp((e.tickCount - e.accionDesde + pt - 4f) / 22f, 0f, 1f);
        return e.postura() == Enemigo.REZA ? 0f : 1f;
    }

    @Override
    public void render(Gargola e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        brillo = brillo(e, pt);
        super.render(e, yaw, pt, ps, buf, luz);
    }
}
