package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Barquero;
import com.darkcraft.enemigos.Enemigo;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/** El Barquero de la Laguna Negra (parte 7j): de vigia en su barca con la linterna en alto, o en pie con el remo como un baston. */
public class BarqueroModelo extends EnemigoModelo<Barquero> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "barquero"), "main");

    public BarqueroModelo(ModelPart root) {
        super(root, BarqueroGeometria.OCULTAS_0);
    }

    @Override
    protected boolean animar(Barquero e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0) {
            if (e.postura() != Enemigo.DE_PIE) poner(e.reloj, BarqueroAnimaciones.VIGIA, edad, 1f);
            else {
                poner(e.reloj, BarqueroAnimaciones.QUIETO, edad, 1f - guardia);
                poner(e.reloj, BarqueroAnimaciones.PORTE_BARQUERO, edad, 1f - guardia);
                poner(e.reloj, BarqueroAnimaciones.LISTO_BARQUERO, edad, guardia);
                animateWalk(BarqueroAnimaciones.ANDAR, paso, cuanto, 1.2f, 2.2f);
            }
        }
        for (int i = 1; i < BarqueroAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], BarqueroAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
