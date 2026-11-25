package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Brandt;
import com.darkcraft.enemigos.Enemigo;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/** Ser Brandt, el Muro del Rey (parte 4d): de rodillas ante las puertas del trono, en guardia tras su paves. */
public class BrandtModelo extends EnemigoModelo<Brandt> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "brandt"), "main");

    public BrandtModelo(ModelPart root) {
        super(root, BrandtGeometria.OCULTAS_0);
    }

    @Override
    protected boolean animar(Brandt e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, BrandtAnimaciones.VELAR, edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, BrandtAnimaciones.TUMBADO, edad, 1f);
                default -> {
                    poner(e.reloj, BrandtAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, BrandtAnimaciones.PORTE_MARTILLO, edad, 1f - guardia);
                    poner(e.reloj, BrandtAnimaciones.LISTO_MARTILLO, edad, guardia);
                    animateWalk(BrandtAnimaciones.ANDAR, paso, cuanto, 1.3f, 2.0f);
                }
            }
        }
        for (int i = 1; i < BrandtAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], BrandtAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
