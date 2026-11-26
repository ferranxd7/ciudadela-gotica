package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Abad;
import com.darkcraft.enemigos.Enemigo;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/** Hrodric, el Abad de la Cumbre (parte 7l): de rodillas rezando con el baculo delante, o en pie con el baculo como un baston. */
public class AbadModelo extends EnemigoModelo<Abad> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "abad"), "main");

    public AbadModelo(ModelPart root) {
        super(root, AbadGeometria.OCULTAS_0);
    }

    @Override
    protected boolean animar(Abad e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0) {
            if (e.postura() != Enemigo.DE_PIE) poner(e.reloj, AbadAnimaciones.REZANDO, edad, 1f);
            else {
                poner(e.reloj, AbadAnimaciones.QUIETO, edad, 1f - guardia);
                poner(e.reloj, AbadAnimaciones.PORTE_ABAD, edad, 1f - guardia);
                poner(e.reloj, AbadAnimaciones.LISTO_ABAD, edad, guardia);
                animateWalk(AbadAnimaciones.ANDAR, paso, cuanto, 1.0f, 2.0f);
            }
        }
        for (int i = 1; i < AbadAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], AbadAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
