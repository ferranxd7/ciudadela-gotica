package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Isolde;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * La reina Isolde (parte 7ab; modelo de {@code tools/enemigos/isolde.py}): sentada en su trono con las manos en los brazos (mientras
 * espera, {@code REZA}), de pie con las manos juntas o en guardia con las garras abiertas; no anda: se desliza (el vestido hasta el
 * suelo). Con el velo entero (variante 0) o ya quemado (variante 1: se le ve la cara). Se dibuja algo transparente (lo que la Sombra
 * devolvio no es del todo de este mundo): el dibujante le dice cuanto ({@link #alfa}).
 */
public class IsoldeModelo extends EnemigoModelo<Isolde> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "isolde"), "main");

    /** Lo opaco que se dibuja ahora (lo pone el dibujante antes de cada vez). */
    public float alfa = 0.9f;

    public IsoldeModelo(ModelPart root) {
        super(root, RenderType::entityTranslucent, IsoldeGeometria.OCULTAS_0, IsoldeGeometria.OCULTAS_1);
    }

    @Override
    protected boolean animar(Isolde e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0 && !e.muriendo()) {
            if (e.postura() == Enemigo.REZA) poner(e.reloj, IsoldeAnimaciones.SENTADA, edad, 1f);
            else {
                poner(e.reloj, IsoldeAnimaciones.QUIETO, edad, 1f - guardia);
                poner(e.reloj, IsoldeAnimaciones.LISTO, edad, guardia);
                animateWalk(IsoldeAnimaciones.ANDAR, paso, cuanto, 1.2f, 1.8f);
            }
        }
        for (int i = 1; i < IsoldeAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], IsoldeAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int luz, int capa, float r, float g, float b, float a) {
        super.renderToBuffer(ps, vc, luz, capa, r, g, b, a * alfa);
    }
}
