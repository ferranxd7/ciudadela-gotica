package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Salobre;
import com.darkcraft.enemigos.SalobreAnims;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/**
 * El salobre del Puerto Varado: de pie, cargado de hombros, con lo suyo en la derecha (el plomo colgando de su cuerda, el
 * bichero como un baston o el rastro arrastrando por detras: su "porte", que se suma al quieto); de rodillas faenando
 * ({@code REZA}, cada uno lo suyo) o enterrado boca arriba en la costra de sal ({@code TUMBADO}). El redero no lleva la red al
 * hombro desde que la lanza hasta que acaba (la que vuela es {@code Red}).
 */
public class SalobreModelo extends EnemigoModelo<Salobre> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "salobre"), "main");

    private final ModelPart red;

    public SalobreModelo(ModelPart root) {
        super(root, SalobreGeometria.OCULTAS_0, SalobreGeometria.OCULTAS_1, SalobreGeometria.OCULTAS_2);
        red = pieza("red_hombro");
    }

    private static AnimationDefinition de(int v, AnimationDefinition redero, AnimationDefinition bichero, AnimationDefinition salinero) {
        return v == Salobre.BICHERO ? bichero : v == Salobre.SALINERO ? salinero : redero;
    }

    @Override
    protected boolean animar(Salobre e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        int v = e.variante();
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, de(v, SalobreAnimaciones.FAENANDO_PLOMO, SalobreAnimaciones.FAENANDO_BICHERO,
                        SalobreAnimaciones.FAENANDO_RASTRO), edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, SalobreAnimaciones.ENTERRADO, edad, 1f);
                default -> {
                    poner(e.reloj, SalobreAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, de(v, SalobreAnimaciones.PORTE_PLOMO, SalobreAnimaciones.PORTE_BICHERO, SalobreAnimaciones.PORTE_RASTRO),
                            edad, 1f - guardia);
                    poner(e.reloj, de(v, SalobreAnimaciones.LISTO_PLOMO, SalobreAnimaciones.LISTO_BICHERO, SalobreAnimaciones.LISTO_RASTRO),
                            edad, guardia);
                    animateWalk(SalobreAnimaciones.ANDAR, paso, cuanto, 1.5f, 2.3f);
                }
            }
        }
        for (int i = 1; i < SalobreAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], SalobreAnimaciones.ACCIONES[i], edad, 1f);
        if (accion == SalobreAnims.RED && v == Salobre.REDERO) {
            long ms = e.acciones[accion].getAccumulatedTime();
            if (ms >= SalobreAnims.GOLPES[accion][0] * 50L) red.visible = false;
        }
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
