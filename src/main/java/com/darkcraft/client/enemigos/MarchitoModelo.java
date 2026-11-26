package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Marchito;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/** El marchito de Morvain: sus tres variantes (guadana, farol, campana), cada una con como lleva lo suyo y su guardia. */
public class MarchitoModelo extends EnemigoModelo<Marchito> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "marchito"), "main");

    private static final AnimationDefinition[] PORTE = {MarchitoAnimaciones.PORTE_GUADANA, MarchitoAnimaciones.PORTE_FAROL, MarchitoAnimaciones.PORTE_CAMPANA};
    private static final AnimationDefinition[] LISTO = {MarchitoAnimaciones.LISTO_GUADANA, MarchitoAnimaciones.LISTO_FAROL, MarchitoAnimaciones.LISTO_CAMPANA};

    public MarchitoModelo(ModelPart root) {
        super(root, MarchitoGeometria.OCULTAS_0, MarchitoGeometria.OCULTAS_1, MarchitoGeometria.OCULTAS_2);
    }

    @Override
    protected boolean animar(Marchito e, float paso, float cuanto, float edad, float guardia) {
        int v = Math.max(0, Math.min(2, e.variante()));
        int accion = e.accionVista();
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, MarchitoAnimaciones.REZAR, edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, MarchitoAnimaciones.TUMBADO, edad, 1f);
                default -> {
                    poner(e.reloj, MarchitoAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, PORTE[v], edad, 1f - guardia);
                    poner(e.reloj, LISTO[v], edad, guardia);
                    animateWalk(MarchitoAnimaciones.ANDAR, paso, cuanto, 1.5f, 2.4f);
                }
            }
        }
        for (int i = 1; i < MarchitoAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], MarchitoAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
