package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Helado;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/**
 * El helado de las Montanas Nevadas: de pie, tieso y encogido de frio, con lo suyo en la derecha (el incensario colgando, el
 * martillo al hombro o el baston apoyado en el suelo: su "porte", que se suma al quieto); de rodillas helado rezando
 * ({@code REZA}, cada uno con lo suyo) o enterrado boca arriba en la nieve ({@code TUMBADO}).
 */
public class HeladoModelo extends EnemigoModelo<Helado> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "helado"), "main");

    public HeladoModelo(ModelPart root) {
        super(root, HeladoGeometria.OCULTAS_0, HeladoGeometria.OCULTAS_1, HeladoGeometria.OCULTAS_2);
    }

    private static AnimationDefinition de(int v, AnimationDefinition monje, AnimationDefinition penitente, AnimationDefinition peregrino) {
        return v == Helado.PENITENTE ? penitente : v == Helado.PEREGRINO ? peregrino : monje;
    }

    @Override
    protected boolean animar(Helado e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        int v = e.variante();
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, de(v, HeladoAnimaciones.REZANDO_MONJE, HeladoAnimaciones.REZANDO_PENITENTE,
                        HeladoAnimaciones.REZANDO_PEREGRINO), edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, HeladoAnimaciones.ENTERRADO, edad, 1f);
                default -> {
                    poner(e.reloj, HeladoAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, de(v, HeladoAnimaciones.PORTE_MONJE, HeladoAnimaciones.PORTE_PENITENTE, HeladoAnimaciones.PORTE_PEREGRINO),
                            edad, 1f - guardia);
                    poner(e.reloj, de(v, HeladoAnimaciones.LISTO_MONJE, HeladoAnimaciones.LISTO_PENITENTE, HeladoAnimaciones.LISTO_PEREGRINO),
                            edad, guardia);
                    animateWalk(HeladoAnimaciones.ANDAR, paso, cuanto, 1.6f, 2.4f);
                }
            }
        }
        for (int i = 1; i < HeladoAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], HeladoAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
