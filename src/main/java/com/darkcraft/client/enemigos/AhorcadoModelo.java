package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Ahorcado;
import com.darkcraft.enemigos.AhorcadoAnims;
import com.darkcraft.enemigos.Enemigo;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/**
 * El ahorcado del Bosque Muerto: colgado de su soga (girando despacio alrededor del nudo, con la soga del modelo que sube
 * hasta la de bloques de la rama), de rodillas, tirado o de pie; el de la soga, con ella al costado.
 */
public class AhorcadoModelo extends EnemigoModelo<Ahorcado> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "ahorcado"), "main");

    private final ModelPart sogaArriba, cabo;

    public AhorcadoModelo(ModelPart root) {
        super(root, AhorcadoGeometria.OCULTAS_0, AhorcadoGeometria.OCULTAS_1);
        sogaArriba = pieza("soga_arriba");
        cabo = pieza("cabo");
    }

    @Override
    protected boolean animar(Ahorcado e, float paso, float cuanto, float edad, float guardia) {
        boolean soga = e.variante() == Ahorcado.SOGA;
        int accion = e.accionVista();
        // la soga de arriba: mientras cuelga (y el instante en que se parte); despues, el cabo cortado por la espalda
        boolean cuelga = e.postura() == Enemigo.COLGADO && (accion == 0 || (accion == AhorcadoAnims.CAER && edad - e.accionDesde < 1.5f));
        sogaArriba.visible = cuelga;
        cabo.visible = !cuelga;
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.COLGADO -> {
                    poner(e.reloj, AhorcadoAnimaciones.COLGADO, edad, 1f);
                    if (soga) poner(e.reloj, AhorcadoAnimaciones.PORTE_SOGA, edad, 1f);
                }
                case Enemigo.REZA -> {
                    poner(e.reloj, AhorcadoAnimaciones.REZAR, edad, 1f);
                    if (soga) poner(e.reloj, AhorcadoAnimaciones.PORTE_SOGA, edad, 1f);
                }
                case Enemigo.TUMBADO -> poner(e.reloj, AhorcadoAnimaciones.TUMBADO, edad, 1f);
                default -> {
                    poner(e.reloj, AhorcadoAnimaciones.QUIETO, edad, 1f - guardia);
                    if (soga) poner(e.reloj, AhorcadoAnimaciones.PORTE_SOGA, edad, 1f - guardia);
                    poner(e.reloj, AhorcadoAnimaciones.LISTO_AHORCADO, edad, guardia);
                    if (soga) poner(e.reloj, AhorcadoAnimaciones.LISTO_SOGA, edad, guardia);
                    animateWalk(AhorcadoAnimaciones.ANDAR, paso, cuanto, 1.6f, 2.6f);
                }
            }
        }
        for (int i = 1; i < AhorcadoAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], AhorcadoAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
