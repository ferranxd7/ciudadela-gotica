package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Espectro;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * El espectro de la corte: flota (ni anda ni corre: se desliza), con el estoque bajo o las manos juntas; en guardia, de
 * perfil con el estoque o con las garras abiertas; velando o acurrucado. Se dibuja medio transparente y azulado: el
 * dibujante le dice cuanto ({@link #alfa}).
 */
public class EspectroModelo extends EnemigoModelo<Espectro> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "espectro"), "main");

    private static final AnimationDefinition[] PORTE = {EspectroAnimaciones.PORTE_ESTOQUE, EspectroAnimaciones.PORTE_GARRAS};
    private static final AnimationDefinition[] LISTO = {EspectroAnimaciones.LISTO_ESTOQUE, EspectroAnimaciones.LISTO_GARRAS};

    /** Lo opaco que se dibuja ahora (lo pone el dibujante antes de cada vez). */
    public float alfa = 0.7f;

    public EspectroModelo(ModelPart root) {
        super(root, RenderType::entityTranslucent, EspectroGeometria.OCULTAS_0, EspectroGeometria.OCULTAS_1);
    }

    @Override
    protected boolean animar(Espectro e, float paso, float cuanto, float edad, float guardia) {
        int v = Math.max(0, Math.min(1, e.variante()));
        int accion = e.accionVista();
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, EspectroAnimaciones.VELAR, edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, EspectroAnimaciones.TUMBADO, edad, 1f);
                default -> {
                    poner(e.reloj, EspectroAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, PORTE[v], edad, 1f - guardia);
                    poner(e.reloj, LISTO[v], edad, guardia);
                    animateWalk(EspectroAnimaciones.ANDAR, paso, cuanto, 1.0f, 1.6f);
                }
            }
        }
        for (int i = 1; i < EspectroAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], EspectroAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int luz, int capa, float r, float g, float b, float a) {
        // algo azulado y frio, y medio transparente
        super.renderToBuffer(ps, vc, luz, capa, r * 0.86f, g * 0.95f, b, a * alfa);
    }
}
