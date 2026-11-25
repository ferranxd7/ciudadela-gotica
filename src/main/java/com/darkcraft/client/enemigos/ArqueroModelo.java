package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Arquero;
import com.darkcraft.enemigos.Enemigo;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/** El arquero esqueleto: con el arco en la izquierda; de guardia, velando de rodillas o hecho un monton de huesos. */
public class ArqueroModelo extends EnemigoModelo<Arquero> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "arquero"), "main");

    public ArqueroModelo(ModelPart root) {
        super(root, ArqueroGeometria.OCULTAS_0);
    }

    @Override
    protected boolean animar(Arquero e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, ArqueroAnimaciones.VELAR, edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, ArqueroAnimaciones.TUMBADO, edad, 1f);
                default -> {
                    poner(e.reloj, ArqueroAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, ArqueroAnimaciones.PORTE_ARCO, edad, 1f - guardia);
                    poner(e.reloj, ArqueroAnimaciones.LISTO_ARCO, edad, guardia);
                    animateWalk(ArqueroAnimaciones.ANDAR, paso, cuanto, 1.8f, 2.4f);
                }
            }
        }
        for (int i = 1; i < ArqueroAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], ArqueroAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
