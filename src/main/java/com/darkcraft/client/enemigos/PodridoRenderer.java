package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Podrido;
import com.darkcraft.enemigos.PodridoAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FluidState;

/**
 * El dibujante del podrido: el de los enemigos de siempre (el hinchado, algo mas grande) y, mientras espera flotando boca
 * abajo ({@code TUMBADO} en el agua), el cuerpo subido hasta que la espalda asoma por encima del agua (la entidad se queda
 * abajo: es su caja la que recibe los golpes). Al alzarse vuelve a bajar mientras se da la vuelta.
 */
public class PodridoRenderer extends EnemigoRenderer<Podrido, PodridoModelo> {
    /** Lo que mide el cuerpo tendido boca abajo, de la tripa a la espalda (bloques, a su escala). */
    private static final double GRUESO = 0.42;

    public PodridoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new PodridoModelo(ctx.bakeLayer(PodridoModelo.CAPA)), "podrido", 3, 0.5f, 1.12f);
    }

    @Override
    protected void scale(Podrido e, PoseStack ps, float pt) {
        super.scale(e, ps, pt);
        if (e.variante() == Podrido.HINCHADO) ps.scale(1.12f, 1.08f, 1.12f);
    }

    /** Cuanta agua tiene por encima de los pies (bloques; 0 si no esta en el agua). */
    static double agua(Podrido e, float pt) {
        double y = Mth.lerp(pt, e.yo, e.getY());
        BlockPos p = BlockPos.containing(e.getX(), y, e.getZ());
        double arriba = -1;
        for (int k = 0; k < 4; k++) {
            BlockPos q = p.above(k);
            FluidState f = e.level().getFluidState(q);
            if (!f.is(FluidTags.WATER)) break;
            arriba = q.getY() + f.getHeight(e.level(), q);
        }
        return arriba < 0 ? 0 : Math.max(0, arriba - y);
    }

    /** Lo que se sube el cuerpo: flotando, hasta que asoma la espalda; al alzarse, de vuelta abajo en el primer segundo. */
    static double subida(Podrido e, float pt, double agua) {
        if (agua <= 0) return 0;
        double s = Math.max(0, agua - GRUESO + 0.1);
        int a = e.accionVista();
        if (a == 0) return e.postura() == Enemigo.TUMBADO ? s : 0;
        if (a == PodridoAnims.ALZARSE && e.accionDesde >= 0) {
            float t = (e.tickCount + pt - e.accionDesde) / 18f;
            if (t >= 1f) return 0;
            float k = 1f - t;
            return s * k * k * (3 - 2 * k);
        }
        return 0;
    }

    @Override
    public void render(Podrido e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        double agua = agua(e, pt);
        getModel().flota = agua > 0.3;
        double s = subida(e, pt, agua);
        if (s <= 0) {
            super.render(e, yaw, pt, ps, buf, luz);
            return;
        }
        ps.pushPose();
        ps.translate(0, s, 0);
        super.render(e, yaw, pt, ps, buf, luz);
        ps.popPose();
    }
}
