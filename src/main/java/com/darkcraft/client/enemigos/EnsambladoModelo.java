package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Ensamblado;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/**
 * El ensamblado del Osario de San Ulfo (parte 7as): de pie, los huesos sueltos y la mandibula colgando, con lo suyo (el candelabro
 * derecho, la pala al hombro, la maza arrastrando: su "porte", que se suma al quieto); esperando ({@code REZA}) el portacirios reza
 * de rodillas y el sepulturero cava; hecho un monton de huesos ({@code TUMBADO}: esperando o deshecho), el monton.
 */
public class EnsambladoModelo extends EnemigoModelo<Ensamblado> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "ensamblado"), "main");

    public EnsambladoModelo(ModelPart root) {
        super(root, EnsambladoGeometria.OCULTAS_0, EnsambladoGeometria.OCULTAS_1, EnsambladoGeometria.OCULTAS_2);
    }

    private static AnimationDefinition de(int v, AnimationDefinition portacirios, AnimationDefinition sepulturero, AnimationDefinition coloso) {
        return v == Ensamblado.SEPULTURERO ? sepulturero : v == Ensamblado.COLOSO ? coloso : portacirios;
    }

    @Override
    protected boolean animar(Ensamblado e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        int v = e.variante();
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, v == Ensamblado.SEPULTURERO ? EnsambladoAnimaciones.CAVANDO : EnsambladoAnimaciones.REZANDO, edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, EnsambladoAnimaciones.MONTON, edad, 1f);
                default -> {
                    poner(e.reloj, EnsambladoAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, de(v, EnsambladoAnimaciones.PORTE_PORTACIRIOS, EnsambladoAnimaciones.PORTE_SEPULTURERO, EnsambladoAnimaciones.PORTE_COLOSO),
                            edad, 1f - guardia);
                    poner(e.reloj, de(v, EnsambladoAnimaciones.LISTO_PORTACIRIOS, EnsambladoAnimaciones.LISTO_SEPULTURERO, EnsambladoAnimaciones.LISTO_COLOSO),
                            edad, guardia);
                    animateWalk(EnsambladoAnimaciones.ANDAR, paso, cuanto, v == Ensamblado.COLOSO ? 1.3f : 1.7f, 2.4f);
                }
            }
        }
        for (int i = 1; i < EnsambladoAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], EnsambladoAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
