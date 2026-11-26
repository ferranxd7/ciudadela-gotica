package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Condestable;
import com.darkcraft.enemigos.Enemigo;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/** El Condestable del Juramento (parte 7n): de rodilla con el montante clavado delante, o en pie con el montante a dos manos. */
public class CondestableModelo extends EnemigoModelo<Condestable> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "condestable"), "main");

    public CondestableModelo(ModelPart root) {
        super(root, CondestableGeometria.OCULTAS_0);
    }

    @Override
    protected boolean animar(Condestable e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0) {
            if (e.postura() != Enemigo.DE_PIE) poner(e.reloj, CondestableAnimaciones.RODILLA, edad, 1f);
            else {
                poner(e.reloj, CondestableAnimaciones.QUIETO, edad, 1f - guardia);
                poner(e.reloj, CondestableAnimaciones.PORTE_MONTANTE, edad, 1f - guardia);
                poner(e.reloj, CondestableAnimaciones.LISTO_MONTANTE, edad, guardia);
                animateWalk(CondestableAnimaciones.ANDAR, paso, cuanto, 1.0f, 2.0f);
            }
        }
        for (int i = 1; i < CondestableAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], CondestableAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
