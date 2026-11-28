package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Sigrun;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Sigrun, la Guardiana del Paso (parte 7ap; modelo de {@code tools/enemigos/sigrun.py}): segun lo que hace ({@link Sigrun#modo}), de pie
 * guardando el paso con la espada clavada en la nieve (respira y mira a un lado y a otro), andando con la espada colgando, en guardia
 * tras el escudo en el duelo, de rodillas apoyada en la espada (rendida) o tendida en la nieve con la espada en el pecho (caida); al
 * marcharse ya no lleva el escudo (te lo ha dado). Se dibuja con algo de transparencia solo cuando se deshace en nieve o aparece
 * ({@link #alfa}, lo pone el dibujante).
 */
public class SigrunModelo extends EnemigoModelo<Sigrun> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "sigrun"), "main");

    private final ModelPart escudo;
    /** Lo opaca que se dibuja ahora (lo pone el dibujante antes de cada vez). */
    public float alfa = 1f;

    public SigrunModelo(ModelPart root) {
        super(root, RenderType::entityTranslucent, SigrunGeometria.OCULTAS_0);
        escudo = pieza("escudo");
    }

    @Override
    protected boolean animar(Sigrun e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        int modo = e.modo();
        escudo.visible = modo != Sigrun.MARCHA;
        if (accion == 0 && !e.muriendo()) {
            switch (modo) {
                case Sigrun.RENDIDA -> poner(e.reloj, SigrunAnimaciones.RODILLAS, edad, 1f);
                case Sigrun.CAIDA -> poner(e.reloj, SigrunAnimaciones.CAIDA, edad, 1f);
                case Sigrun.DUELO -> {
                    poner(e.reloj, SigrunAnimaciones.LISTO, edad, 1f);
                    animateWalk(SigrunAnimaciones.ANDAR, paso, cuanto, 1.6f, 2.0f);
                }
                default -> {
                    if (modo == Sigrun.PAZ && e.clavada()) poner(e.reloj, SigrunAnimaciones.QUIETO, edad, 1f);
                    else {
                        poner(e.reloj, SigrunAnimaciones.PORTE, edad, 1f);
                        animateWalk(SigrunAnimaciones.ANDAR, paso, cuanto, 1.6f, 2.0f);
                    }
                }
            }
        }
        for (int i = 1; i < SigrunAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], SigrunAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && (modo == Sigrun.PAZ || modo == Sigrun.DUELO || modo == Sigrun.RENDIDA);
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int luz, int capa, float r, float g, float b, float a) {
        super.renderToBuffer(ps, vc, luz, capa, r, g, b, a * alfa);
    }
}
