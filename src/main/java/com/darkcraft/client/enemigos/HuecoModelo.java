package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Hueco;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/** El aldeano hueco: sus tres variantes (horca, hoz, antorcha), cada una con como lleva lo suyo y como se pone en guardia. */
public class HuecoModelo extends EnemigoModelo<Hueco> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "hueco"), "main");

    private static final AnimationDefinition[] PORTE = {HuecoAnimaciones.PORTE_HORCA, HuecoAnimaciones.PORTE_HOZ, HuecoAnimaciones.PORTE_ANTORCHA};
    private static final AnimationDefinition[] LISTO = {HuecoAnimaciones.LISTO_HORCA, HuecoAnimaciones.LISTO_HOZ, HuecoAnimaciones.LISTO_ANTORCHA};

    public HuecoModelo(ModelPart root) {
        super(root, HuecoGeometria.OCULTAS_0, HuecoGeometria.OCULTAS_1, HuecoGeometria.OCULTAS_2);
    }

    @Override
    protected boolean animar(Hueco e, float paso, float cuanto, float edad, float guardia) {
        int v = Math.max(0, Math.min(2, e.variante()));
        int accion = e.accionVista();
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, HuecoAnimaciones.REZAR, edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, HuecoAnimaciones.TUMBADO, edad, 1f);
                default -> {
                    // de tranquilo a en guardia, mezclando las dos posturas
                    poner(e.reloj, HuecoAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, PORTE[v], edad, 1f - guardia);
                    poner(e.reloj, LISTO[v], edad, guardia);
                    animateWalk(HuecoAnimaciones.ANDAR, paso, cuanto, 1.6f, 2.4f);
                }
            }
        }
        for (int i = 1; i < HuecoAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], HuecoAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
