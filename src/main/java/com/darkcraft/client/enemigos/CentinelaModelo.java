package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Centinela;
import com.darkcraft.enemigos.Enemigo;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/**
 * El centinela de la casa de la Sombra (parte 7ab; modelo de {@code tools/enemigos/centinela.py}): de pie e inmovil con la cabeza
 * caida mientras duerme, tirado hecho un monton de hierro, y despierto quieto o en guardia con su espadon o su alabarda.
 */
public class CentinelaModelo extends EnemigoModelo<Centinela> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "centinela"), "main");

    private static final AnimationDefinition[] PORTE = {CentinelaAnimaciones.PORTE_ESPADON, CentinelaAnimaciones.PORTE_ALABARDA};
    private static final AnimationDefinition[] LISTO = {CentinelaAnimaciones.LISTO_ESPADON, CentinelaAnimaciones.LISTO_ALABARDA};
    private static final AnimationDefinition[] DORMIDO = {CentinelaAnimaciones.DORMIDO_ESPADON, CentinelaAnimaciones.DORMIDO_ALABARDA};

    public CentinelaModelo(ModelPart root) {
        super(root, CentinelaGeometria.OCULTAS_0, CentinelaGeometria.OCULTAS_1);
    }

    @Override
    protected boolean animar(Centinela e, float paso, float cuanto, float edad, float guardia) {
        int v = e.variante() == Centinela.ALABARDA ? 1 : 0;
        int accion = e.accionVista();
        if (accion == 0 && !e.muriendo()) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, DORMIDO[v], edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, CentinelaAnimaciones.TUMBADO, edad, 1f);
                default -> {
                    poner(e.reloj, CentinelaAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, PORTE[v], edad, 1f - guardia);
                    poner(e.reloj, LISTO[v], edad, guardia);
                    animateWalk(CentinelaAnimaciones.ANDAR, paso, cuanto, 1.4f, 2.2f);
                }
            }
        }
        for (int i = 1; i < CentinelaAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], CentinelaAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
