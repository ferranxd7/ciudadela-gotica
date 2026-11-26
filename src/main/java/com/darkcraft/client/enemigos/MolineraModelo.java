package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Molinera;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/** La Molinera de Velmor (parte 7f): de rodillas ante la piedra de moler, o en pie con la guadana al hombro. */
public class MolineraModelo extends EnemigoModelo<Molinera> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "molinera"), "main");

    public MolineraModelo(ModelPart root) {
        super(root, MolineraGeometria.OCULTAS_0);
    }

    @Override
    protected boolean animar(Molinera e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0) {
            if (e.postura() != Enemigo.DE_PIE) poner(e.reloj, MolineraAnimaciones.REZAR, edad, 1f);
            else {
                poner(e.reloj, MolineraAnimaciones.QUIETO, edad, 1f - guardia);
                poner(e.reloj, MolineraAnimaciones.PORTE_MOLINERA, edad, 1f - guardia);
                poner(e.reloj, MolineraAnimaciones.LISTO_MOLINERA, edad, guardia);
                animateWalk(MolineraAnimaciones.ANDAR, paso, cuanto, 1.25f, 2.0f);
            }
        }
        for (int i = 1; i < MolineraAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], MolineraAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
