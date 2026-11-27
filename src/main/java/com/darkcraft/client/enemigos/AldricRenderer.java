package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Aldric;
import com.darkcraft.enemigos.AldricAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * El dibujante de Aldric, el Primer Marcado: el de los enemigos de siempre, a su escala (un caballero enorme: 1,42); la luz de los
 * ojos, de la marca del pecho y de las grietas del eslabon (la capa de brillo) late despacio, mas fuerte cuando le arde la marca, y
 * al caer se va apagando mientras se queda de rodillas. Su reflejo, con el modelo medio transparente y oscuro, se apaga al acabar
 * el golpe.
 */
public class AldricRenderer extends EnemigoRenderer<Aldric, AldricModelo> {
    public static final float ESCALA = 1.42f;
    private final AldricModelo normal, sombra;

    public AldricRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new AldricModelo(ctx.bakeLayer(AldricModelo.CAPA)), "aldric", 1, 1.0f, ESCALA);
        normal = model;
        sombra = new AldricModelo(ctx.bakeLayer(AldricModelo.CAPA), true);
    }

    @Override
    public void render(Aldric e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (e.reflejo()) {
            // el reflejo sale de la nada en 4 ticks y se apaga en los ultimos 6 de su golpe
            float t = e.tickCount + pt;
            float a = Mth.clamp(t / 4f, 0f, 1f) * 0.55f;
            int accion = e.accionVista();
            if (accion > 0 && accion < AldricAnims.TICKS.length) {
                float largo = AldricAnims.TICKS[accion];
                float hecho = e.accionDesde < 0 ? 0f : e.tickCount + pt - e.accionDesde;
                a *= Mth.clamp((largo - hecho) / 6f, 0f, 1f);
            }
            if (a <= 0.02f) return;
            sombra.alfa = a;
            model = sombra;
            brillo = Math.min(1f, a * 1.6f);
            try {
                super.render(e, yaw, pt, ps, buf, luz);
            } finally {
                model = normal;
                brillo = 1f;
            }
            return;
        }
        model = normal;
        if (e.accionVista() == AldricAnims.MORIR || e.isDeadOrDying()) {
            float t = (e.tickCount + pt - e.accionDesde) / (float) AldricAnims.TICKS[AldricAnims.MORIR];
            brillo = Mth.clamp(1f - (t - 0.85f) / 0.8f, 0f, 1f);
        } else if (e.arde()) brillo = 0.85f + 0.15f * Mth.sin((e.tickCount + pt) * 0.35f);
        else brillo = 0.7f + 0.2f * Mth.sin((e.tickCount + pt) * 0.1f);
        super.render(e, yaw, pt, ps, buf, luz);
    }
}
