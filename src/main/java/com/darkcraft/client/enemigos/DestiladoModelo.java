package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Destilado;
import com.darkcraft.enemigos.Enemigo;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/**
 * El destilado del Lazareto (parte 7au): de pie, encorvado, con lo suyo (el mortero y el frasco del mancebo, las garras colgando del
 * enfermo, los guantes de la cuba: su "porte", que se suma al quieto); esperando ({@code REZA}) el mancebo muele en su mesa, el enfermo
 * se mece sentado en el suelo y la cuba jadea; tendido ({@code TUMBADO}), boca arriba. A la cuba, al morir, se le va el destilado de
 * dentro (la cuba se queda vacia).
 */
public class DestiladoModelo extends EnemigoModelo<Destilado> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "destilado"), "main");
    private final ModelPart liquido;

    public DestiladoModelo(ModelPart root) {
        super(root, DestiladoGeometria.OCULTAS_0, DestiladoGeometria.OCULTAS_1, DestiladoGeometria.OCULTAS_2);
        liquido = pieza("liquido");
    }

    private static AnimationDefinition de(int v, AnimationDefinition mancebo, AnimationDefinition enfermo, AnimationDefinition cuba) {
        return v == Destilado.ENFERMO ? enfermo : v == Destilado.CUBA ? cuba : mancebo;
    }

    @Override
    protected boolean animar(Destilado e, float paso, float cuanto, float edad, float guardia) {
        liquido.visible = !e.cubaRota();
        int accion = e.accionVista();
        int v = e.variante();
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, de(v, DestiladoAnimaciones.MOLIENDO, DestiladoAnimaciones.ENCOGIDO, DestiladoAnimaciones.JADEANDO), edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, DestiladoAnimaciones.TENDIDO, edad, 1f);
                default -> {
                    poner(e.reloj, DestiladoAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, de(v, DestiladoAnimaciones.PORTE_MANCEBO, DestiladoAnimaciones.PORTE_ENFERMO, DestiladoAnimaciones.PORTE_CUBA), edad, 1f - guardia);
                    poner(e.reloj, de(v, DestiladoAnimaciones.LISTO_MANCEBO, DestiladoAnimaciones.LISTO_ENFERMO, DestiladoAnimaciones.LISTO_CUBA), edad, guardia);
                    animateWalk(DestiladoAnimaciones.ANDAR, paso, cuanto, v == Destilado.CUBA ? 1.3f : v == Destilado.ENFERMO ? 2.0f : 1.7f, 2.4f);
                }
            }
        }
        for (int i = 1; i < DestiladoAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], DestiladoAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
