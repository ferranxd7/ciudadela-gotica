package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Marcado;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * El marcado del Crater de Sangre: de pie, con lo suyo (el mazo al hombro, la espada colgando y el escudo delante, o el
 * arco de pie y una flecha en la otra mano: su "porte", que se suma al quieto); en guardia; de rodillas cada uno lo suyo
 * ({@code REZA}: el rompedor golpea el suelo con el mazo, el caballero con la espada clavada y las manos en el pomo, el
 * cazador con el arco de pie y la cabeza gacha) o caido de bruces ({@code TUMBADO}). Hay dos: el de siempre y el del
 * reflejo (medio transparente y oscuro: lo elige el dibujante).
 */
public class MarcadoModelo extends EnemigoModelo<Marcado> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "marcado"), "main");

    private static final AnimationDefinition[] PORTE = {MarcadoAnimaciones.PORTE_MAZO, MarcadoAnimaciones.PORTE_ESPADA, MarcadoAnimaciones.PORTE_ARCO};
    private static final AnimationDefinition[] LISTO = {MarcadoAnimaciones.LISTO_MAZO, MarcadoAnimaciones.LISTO_ESPADA, MarcadoAnimaciones.LISTO_ARCO};
    private static final AnimationDefinition[] REZANDO = {MarcadoAnimaciones.REZANDO_MAZO, MarcadoAnimaciones.REZANDO_ESPADA,
            MarcadoAnimaciones.REZANDO_ARCO};

    private final boolean reflejo;
    /** Lo opaco que se dibuja el reflejo (lo pone el dibujante). */
    public float alfa = 0.5f;

    public MarcadoModelo(ModelPart root) {
        super(root, MarcadoGeometria.OCULTAS_0, MarcadoGeometria.OCULTAS_1, MarcadoGeometria.OCULTAS_2);
        reflejo = false;
    }

    /** El del reflejo: medio transparente. */
    public MarcadoModelo(ModelPart root, boolean reflejo) {
        super(root, RenderType::entityTranslucent, MarcadoGeometria.OCULTAS_0, MarcadoGeometria.OCULTAS_1, MarcadoGeometria.OCULTAS_2);
        this.reflejo = reflejo;
    }

    @Override
    protected boolean animar(Marcado e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        int v = Math.max(0, Math.min(2, e.variante()));
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, REZANDO[v], edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, MarcadoAnimaciones.TUMBADO, edad, 1f);
                default -> {
                    poner(e.reloj, MarcadoAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, PORTE[v], edad, 1f - guardia);
                    poner(e.reloj, LISTO[v], edad, guardia);
                    animateWalk(MarcadoAnimaciones.ANDAR, paso, cuanto, 1.5f, 2.3f);
                }
            }
        }
        for (int i = 1; i < MarcadoAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], MarcadoAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int luz, int capa, float r, float g, float b, float a) {
        // el reflejo: una sombra negra con un poco de rojo, medio transparente
        if (reflejo) super.renderToBuffer(ps, vc, luz, capa, r * 0.32f, g * 0.12f, b * 0.14f, a * alfa);
        else super.renderToBuffer(ps, vc, luz, capa, r, g, b, a);
    }
}
