package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Capitan;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/** El Capitan de Brumavela: quieto ante su puerta con el sable al hombro, o en guardia; y sus golpes. */
public class CapitanModelo extends EnemigoModelo<Capitan> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "capitan"), "main");

    public CapitanModelo(ModelPart root) {
        super(root, CapitanGeometria.OCULTAS_0);
    }

    @Override
    protected boolean animar(Capitan e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0) {
            poner(e.reloj, CapitanAnimaciones.QUIETO, edad, 1f - guardia);
            poner(e.reloj, CapitanAnimaciones.LISTO, edad, guardia);
            animateWalk(CapitanAnimaciones.ANDAR, paso, cuanto, 1.3f, 2.2f);
        }
        for (int i = 1; i < CapitanAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], CapitanAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0;
    }
}
