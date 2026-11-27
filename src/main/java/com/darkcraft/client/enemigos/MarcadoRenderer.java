package com.darkcraft.client.enemigos;

import com.darkcraft.enemigos.Marcado;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * El dibujante del marcado: el de los enemigos de siempre (el rompedor, algo mas grande); su reflejo, con el modelo medio
 * transparente y oscuro, que se apaga al acabar el golpe, con la marca encendida.
 */
public class MarcadoRenderer extends EnemigoRenderer<Marcado, MarcadoModelo> {
    private final MarcadoModelo normal, sombra;

    public MarcadoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new MarcadoModelo(ctx.bakeLayer(MarcadoModelo.CAPA)), "marcado", 3, 0.5f, 1.04f);
        normal = model;
        sombra = new MarcadoModelo(ctx.bakeLayer(MarcadoModelo.CAPA), true);
    }

    @Override
    protected void scale(Marcado e, PoseStack ps, float pt) {
        super.scale(e, ps, pt);
        if (e.variante() == Marcado.ROMPEDOR) ps.scale(1.12f, 1.12f, 1.12f);
    }

    @Override
    public void render(Marcado e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (!e.reflejo()) {
            model = normal;
            brillo = 1f;
            super.render(e, yaw, pt, ps, buf, luz);
            return;
        }
        // el reflejo sale de la nada en 4 ticks y se apaga en los ultimos 6 de su golpe
        float t = e.tickCount + pt;
        float a = Mth.clamp(t / 4f, 0f, 1f) * 0.55f;
        int accion = e.accionVista();
        if (accion > 0 && accion < com.darkcraft.enemigos.MarcadoAnims.TICKS.length) {
            float largo = com.darkcraft.enemigos.MarcadoAnims.TICKS[accion];
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
    }
}
