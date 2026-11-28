package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Capataz;
import com.darkcraft.enemigos.Enemigo;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/**
 * Hakon, el Capataz (parte 7ar): esperando ({@code REZA}) pica el corazon de la veta; de pie, quieto con el pico al hombro (su "porte",
 * que se suma al quieto) o en guardia con el pico a dos manos, y su paso pesado.
 */
public class CapatazModelo extends EnemigoModelo<Capataz> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "capataz"), "main");

    public CapatazModelo(ModelPart root) {
        super(root, CapatazGeometria.OCULTAS_0);
    }

    @Override
    protected boolean animar(Capataz e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0) {
            if (e.postura() != Enemigo.DE_PIE) poner(e.reloj, CapatazAnimaciones.PICANDO, edad, 1f);
            else {
                poner(e.reloj, CapatazAnimaciones.QUIETO, edad, 1f - guardia);
                poner(e.reloj, CapatazAnimaciones.PORTE, edad, 1f - guardia);
                poner(e.reloj, CapatazAnimaciones.LISTO, edad, guardia);
                animateWalk(CapatazAnimaciones.ANDAR, paso, cuanto, 1.3f, 2.2f);
            }
        }
        for (int i = 1; i < CapatazAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], CapatazAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
