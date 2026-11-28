package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Insomne;
import com.darkcraft.enemigos.InsomneAnims;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/**
 * El Insomne del Fuerte de la Marca (parte 7aw): de pie, cansado (la cabeza se le cae y la levanta de golpe), con lo suyo (la lanza
 * de pie, la ballesta cruzada, el escudo y la maza: su "porte", que se suma al quieto) o en guardia; esperando ({@code REZA}) el
 * centinela y el ballestero vigilan (la cabeza a un lado y a otro) y el escudero se apoya en el escudo; sentado ({@code TUMBADO}), el
 * ballestero contra el muro. El cuerno va a la cadera y pasa a la mano mientras toca; el virote se ve en la ballesta cuando esta
 * cargada.
 */
public class InsomneModelo extends EnemigoModelo<Insomne> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "insomne"), "main");
    private final ModelPart cuerno, cuernoMano, virote;

    public InsomneModelo(ModelPart root) {
        super(root, InsomneGeometria.OCULTAS_0, InsomneGeometria.OCULTAS_1, InsomneGeometria.OCULTAS_2);
        cuerno = pieza("cuerno");
        cuernoMano = pieza("cuerno_mano");
        virote = pieza("virote_armado");
    }

    private static AnimationDefinition de(int v, AnimationDefinition centinela, AnimationDefinition ballestero, AnimationDefinition escudero) {
        return v == Insomne.BALLESTERO ? ballestero : v == Insomne.ESCUDERO ? escudero : centinela;
    }

    @Override
    protected boolean animar(Insomne e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        int v = e.variante();
        if (v == Insomne.CENTINELA) {
            // el cuerno en la mano desde que se lo lleva a la boca hasta que lo baja
            float t = accion == InsomneAnims.CUERNO && e.accionDesde >= 0 ? (edad - e.accionDesde) / 20f : -1f;
            boolean toca = t >= 0.3f && t < 1.8f;
            cuerno.visible = !toca;
            cuernoMano.visible = toca;
        }
        if (v == Insomne.BALLESTERO) virote.visible = e.cargada() && !e.muriendo();
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> {
                    if (v == Insomne.ESCUDERO) poner(e.reloj, InsomneAnimaciones.APOYADO, edad, 1f);
                    else {
                        poner(e.reloj, InsomneAnimaciones.VIGILANDO, edad, 1f);
                        poner(e.reloj, de(v, InsomneAnimaciones.PORTE_CENTINELA, InsomneAnimaciones.PORTE_BALLESTERO, InsomneAnimaciones.PORTE_ESCUDERO), edad, 1f);
                    }
                }
                case Enemigo.TUMBADO -> poner(e.reloj, InsomneAnimaciones.SENTADO, edad, 1f);
                default -> {
                    poner(e.reloj, InsomneAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, de(v, InsomneAnimaciones.PORTE_CENTINELA, InsomneAnimaciones.PORTE_BALLESTERO, InsomneAnimaciones.PORTE_ESCUDERO), edad, 1f - guardia);
                    poner(e.reloj, de(v, InsomneAnimaciones.LISTO_CENTINELA, InsomneAnimaciones.LISTO_BALLESTERO, InsomneAnimaciones.LISTO_ESCUDERO), edad, guardia);
                    animateWalk(InsomneAnimaciones.ANDAR, paso, cuanto, v == Insomne.ESCUDERO ? 1.4f : 1.7f, 2.4f);
                }
            }
        }
        for (int i = 1; i < InsomneAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], InsomneAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
