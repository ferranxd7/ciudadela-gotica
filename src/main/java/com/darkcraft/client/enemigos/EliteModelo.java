package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Elite;
import com.darkcraft.enemigos.Enemigo;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/** El capitan de elite de la guardia (parte 4c): de guardia con el mandoble de punta en el suelo, velando o tirado. */
public class EliteModelo extends EnemigoModelo<Elite> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "elite"), "main");

    public EliteModelo(ModelPart root) {
        super(root, EliteGeometria.OCULTAS_0);
    }

    @Override
    protected boolean animar(Elite e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, EliteAnimaciones.VELAR, edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, EliteAnimaciones.TUMBADO, edad, 1f);
                default -> {
                    poner(e.reloj, EliteAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, EliteAnimaciones.PORTE_MANDOBLE, edad, 1f - guardia);
                    poner(e.reloj, EliteAnimaciones.LISTO_MANDOBLE, edad, guardia);
                    animateWalk(EliteAnimaciones.ANDAR, paso, cuanto, 1.4f, 2.2f);
                }
            }
        }
        for (int i = 1; i < EliteAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], EliteAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
