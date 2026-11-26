package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Erudito;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * El erudito de la Academia Espectral (parte 7o): flota y se desliza como los espectros; el aprendiz con el calamo y el
 * tintero, el maestro con la vara y el libro, el custodio con su tomo colgado de la cadena. Leyendo (la postura de rezar) o
 * caido entre los libros. Se dibuja medio transparente y algo violeta: el dibujante le dice cuanto ({@link #alfa}).
 */
public class EruditoModelo extends EnemigoModelo<Erudito> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "erudito"), "main");

    private static final AnimationDefinition[] PORTE = {EruditoAnimaciones.PORTE_CALAMO, EruditoAnimaciones.PORTE_VARA, EruditoAnimaciones.PORTE_TOMO};
    private static final AnimationDefinition[] LISTO = {EruditoAnimaciones.LISTO_CALAMO, EruditoAnimaciones.LISTO_VARA, EruditoAnimaciones.LISTO_TOMO};

    /** Lo opaco que se dibuja ahora (lo pone el dibujante antes de cada vez). */
    public float alfa = 0.72f;

    public EruditoModelo(ModelPart root) {
        super(root, RenderType::entityTranslucent, EruditoGeometria.OCULTAS_0, EruditoGeometria.OCULTAS_1, EruditoGeometria.OCULTAS_2);
    }

    @Override
    protected boolean animar(Erudito e, float paso, float cuanto, float edad, float guardia) {
        int v = Math.max(0, Math.min(2, e.variante()));
        int accion = e.accionVista();
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, EruditoAnimaciones.LEER, edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, EruditoAnimaciones.TUMBADO, edad, 1f);
                default -> {
                    poner(e.reloj, EruditoAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, PORTE[v], edad, 1f - guardia);
                    poner(e.reloj, LISTO[v], edad, guardia);
                    animateWalk(EruditoAnimaciones.ANDAR, paso, cuanto, 1.0f, 1.6f);
                }
            }
        }
        for (int i = 1; i < EruditoAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], EruditoAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int luz, int capa, float r, float g, float b, float a) {
        // algo violeta y frio, y medio transparente
        super.renderToBuffer(ps, vc, luz, capa, r * 0.92f, g * 0.88f, b, a * alfa);
    }
}
