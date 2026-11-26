package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Verdugo;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/** Maese Odran, el Verdugo de la Arboleda (parte 7h): apoyado en el hacha en el patibulo, o en pie con el hacha al hombro. */
public class VerdugoModelo extends EnemigoModelo<Verdugo> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "verdugo"), "main");

    public VerdugoModelo(ModelPart root) {
        super(root, VerdugoGeometria.OCULTAS_0);
    }

    @Override
    protected boolean animar(Verdugo e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0) {
            if (e.postura() != Enemigo.DE_PIE) poner(e.reloj, VerdugoAnimaciones.APOYADO, edad, 1f);
            else {
                poner(e.reloj, VerdugoAnimaciones.QUIETO, edad, 1f - guardia);
                poner(e.reloj, VerdugoAnimaciones.PORTE_VERDUGO, edad, 1f - guardia);
                poner(e.reloj, VerdugoAnimaciones.LISTO_VERDUGO, edad, guardia);
                animateWalk(VerdugoAnimaciones.ANDAR, paso, cuanto, 1.3f, 2.2f);
            }
        }
        for (int i = 1; i < VerdugoAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], VerdugoAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
