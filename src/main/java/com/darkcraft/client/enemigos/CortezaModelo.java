package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Corteza;
import com.darkcraft.enemigos.Enemigo;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/** Vareth, el Guardian de Corteza (parte 4d): de rodillas en la base de la torre del arbol, con las garras abiertas. */
public class CortezaModelo extends EnemigoModelo<Corteza> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "corteza"), "main");

    public CortezaModelo(ModelPart root) {
        super(root, CortezaGeometria.OCULTAS_0);
    }

    @Override
    protected boolean animar(Corteza e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, CortezaAnimaciones.VELAR, edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, CortezaAnimaciones.TUMBADO, edad, 1f);
                default -> {
                    poner(e.reloj, CortezaAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, CortezaAnimaciones.PORTE_GARRAS, edad, 1f - guardia);
                    poner(e.reloj, CortezaAnimaciones.LISTO_GARRAS, edad, guardia);
                    animateWalk(CortezaAnimaciones.ANDAR, paso, cuanto, 1.3f, 2.0f);
                }
            }
        }
        for (int i = 1; i < CortezaAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], CortezaAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
