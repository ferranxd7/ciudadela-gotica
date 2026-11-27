package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Pontifice;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/**
 * Anselmo, el Pontifice Sangrante (parte 7t): de rodillas ante el altar con el caliz alzado (la postura de rezar) o de pie con la
 * ferula y el caliz; de pie, quieto o en guardia segun pelee, y su paso de procesion.
 */
public class PontificeModelo extends EnemigoModelo<Pontifice> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "pontifice"), "main");

    public PontificeModelo(ModelPart root) {
        super(root, PontificeGeometria.OCULTAS_0);
    }

    @Override
    protected boolean animar(Pontifice e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0) {
            if (e.postura() != Enemigo.DE_PIE) poner(e.reloj, PontificeAnimaciones.REZANDO, edad, 1f);
            else {
                poner(e.reloj, PontificeAnimaciones.QUIETO, edad, 1f - guardia);
                poner(e.reloj, PontificeAnimaciones.LISTO, edad, guardia);
                animateWalk(PontificeAnimaciones.ANDAR, paso, cuanto, 1.4f, 2.0f);
            }
        }
        for (int i = 1; i < PontificeAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], PontificeAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
