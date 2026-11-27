package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Centinela;
import com.darkcraft.enemigos.CentinelaAnims;
import com.darkcraft.enemigos.Enemigo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * El centinela: dormido (de pie o tirado) las brasas del yelmo y la marca estan apagadas; al despertar se encienden poco a poco y
 * al morir se apagan. Mientras cruza hecho humo (la accion del humo, del tick en que se deshace al que vuelve) no se dibuja.
 */
public class CentinelaRenderer extends EnemigoRenderer<Centinela, CentinelaModelo> {
    public CentinelaRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new CentinelaModelo(ctx.bakeLayer(CentinelaModelo.CAPA)), "centinela", 2, 0.8f, 1.3f);
    }

    /** Lo que lucen ahora las brasas y la marca (0 = apagadas). */
    public static float brillo(Centinela e, float pt) {
        if (e.muriendo()) return Mth.clamp(1f - (e.deathTime + pt) / 30f, 0f, 1f);
        int a = e.accionVista();
        if (a == CentinelaAnims.DESPERTAR_ESPADON || a == CentinelaAnims.DESPERTAR_ALABARDA || a == CentinelaAnims.LEVANTARSE_ESPADON
                || a == CentinelaAnims.LEVANTARSE_ALABARDA)
            return Mth.clamp((e.tickCount - e.accionDesde + pt - 4f) / 16f, 0f, 1f);
        return e.postura() == Enemigo.DE_PIE ? 1f : 0f;
    }

    /** Si esta cruzado hecho humo (no se ve). */
    public static boolean enHumo(Centinela e) {
        int a = e.accionVista();
        if (a != CentinelaAnims.HUMO_ESPADON && a != CentinelaAnims.HUMO_ALABARDA || e.accionDesde < 0) return false;
        int t = e.tickCount - e.accionDesde;
        return t >= Centinela.HUMO_DESDE && t <= Centinela.HUMO_HASTA + 1;
    }

    @Override
    public void render(Centinela e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (enHumo(e)) return;
        brillo = brillo(e, pt);
        super.render(e, yaw, pt, ps, buf, luz);
    }
}
