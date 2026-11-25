package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Ahogado;
import com.darkcraft.enemigos.AhogadoAnims;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/**
 * El marinero ahogado: el del sable y el del arpon. El arponero no lleva arpon en la mano desde que lo suelta hasta
 * que saca otro de la espalda (el que vuela es {@code Arpon}).
 */
public class AhogadoModelo extends EnemigoModelo<Ahogado> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "ahogado"), "main");

    private static final AnimationDefinition[] PORTE = {AhogadoAnimaciones.PORTE_SABLE, AhogadoAnimaciones.PORTE_ARPON};
    private static final AnimationDefinition[] LISTO = {AhogadoAnimaciones.LISTO_SABLE, AhogadoAnimaciones.LISTO_ARPON};

    private final ModelPart arpon;

    public AhogadoModelo(ModelPart root) {
        super(root, AhogadoGeometria.OCULTAS_0, AhogadoGeometria.OCULTAS_1);
        arpon = pieza("arpon");
    }

    @Override
    protected boolean animar(Ahogado e, float paso, float cuanto, float edad, float guardia) {
        int v = Math.max(0, Math.min(1, e.variante()));
        int accion = e.accionVista();
        if (accion == 0) {
            poner(e.reloj, AhogadoAnimaciones.QUIETO, edad, 1f - guardia);
            poner(e.reloj, PORTE[v], edad, 1f - guardia);
            poner(e.reloj, LISTO[v], edad, guardia);
            animateWalk(AhogadoAnimaciones.ANDAR, paso, cuanto, 1.4f, 2.2f);
        }
        for (int i = 1; i < AhogadoAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], AhogadoAnimaciones.ACCIONES[i], edad, 1f);
        if (accion == AhogadoAnims.ARPON_LANZAR) {
            long ms = e.acciones[accion].getAccumulatedTime();
            if (ms >= AhogadoAnims.GOLPES[accion][0] * 50L && ms < AhogadoAnims.ARPON_OTRO * 50L) arpon.visible = false;
        }
        return accion == 0;
    }
}
