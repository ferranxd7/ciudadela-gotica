package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Calcinado;
import com.darkcraft.enemigos.Enemigo;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/**
 * El calcinado de la Capital en ruinas: de pie, encorvado y humeando, con lo suyo (la guardia, la espada baja y el escudo
 * embrazado delante; el incendiario, el cantaro encendido en la palma; el ceniciento, los brazos colgando: su "porte", que
 * se suma al quieto); agazapado junto a los rescoldos ({@code REZA}) o enterrado de bruces en la ceniza ({@code TUMBADO}).
 */
public class CalcinadoModelo extends EnemigoModelo<Calcinado> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "calcinado"), "main");

    public CalcinadoModelo(ModelPart root) {
        super(root, CalcinadoGeometria.OCULTAS_0, CalcinadoGeometria.OCULTAS_1, CalcinadoGeometria.OCULTAS_2);
    }

    private static AnimationDefinition de(int v, AnimationDefinition guardia, AnimationDefinition incendiario, AnimationDefinition ceniciento) {
        return v == Calcinado.INCENDIARIO ? incendiario : v == Calcinado.CENICIENTO ? ceniciento : guardia;
    }

    @Override
    protected boolean animar(Calcinado e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        int v = e.variante();
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, de(v, CalcinadoAnimaciones.AGAZAPADO_GUARDIA, CalcinadoAnimaciones.AGAZAPADO_INCENDIARIO,
                        CalcinadoAnimaciones.AGAZAPADO_CENICIENTO), edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, CalcinadoAnimaciones.ENTERRADO, edad, 1f);
                default -> {
                    poner(e.reloj, CalcinadoAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, de(v, CalcinadoAnimaciones.PORTE_GUARDIA, CalcinadoAnimaciones.PORTE_INCENDIARIO, CalcinadoAnimaciones.PORTE_CENICIENTO),
                            edad, 1f - guardia);
                    poner(e.reloj, de(v, CalcinadoAnimaciones.LISTO_GUARDIA, CalcinadoAnimaciones.LISTO_INCENDIARIO, CalcinadoAnimaciones.LISTO_CENICIENTO),
                            edad, guardia);
                    animateWalk(CalcinadoAnimaciones.ANDAR, paso, cuanto, 1.6f, 2.4f);
                }
            }
        }
        for (int i = 1; i < CalcinadoAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], CalcinadoAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
