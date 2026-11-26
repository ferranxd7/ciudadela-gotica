package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Rectora;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Ysolde, la Rectora de la Academia (parte 7p): leyendo el cielo ante el telescopio (la postura de rezar: la cabeza atras y
 * la mano alzada hacia las estrellas), o flotando con el baculo. Se dibuja medio transparente y algo fria, como sus eruditos
 * (pero mas opaca): el dibujante le dice cuanto ({@link #alfa}).
 */
public class RectoraModelo extends EnemigoModelo<Rectora> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "rectora"), "main");

    /** Lo opaco que se dibuja ahora (lo pone el dibujante antes de cada vez). */
    public float alfa = 0.86f;

    public RectoraModelo(ModelPart root) {
        super(root, RenderType::entityTranslucent, RectoraGeometria.OCULTAS_0);
    }

    @Override
    protected boolean animar(Rectora e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0) {
            if (e.postura() != Enemigo.DE_PIE) poner(e.reloj, RectoraAnimaciones.CIELO, edad, 1f);
            else {
                poner(e.reloj, RectoraAnimaciones.QUIETO, edad, 1f - guardia);
                poner(e.reloj, RectoraAnimaciones.PORTE_BACULO, edad, 1f - guardia);
                poner(e.reloj, RectoraAnimaciones.LISTO_BACULO, edad, guardia);
                animateWalk(RectoraAnimaciones.ANDAR, paso, cuanto, 1.0f, 1.8f);
            }
        }
        for (int i = 1; i < RectoraAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], RectoraAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int luz, int capa, float r, float g, float b, float a) {
        // algo fria y medio transparente
        super.renderToBuffer(ps, vc, luz, capa, r * 0.94f, g * 0.94f, b, a * alfa);
    }
}
