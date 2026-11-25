package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Guardia;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/** La guardia de Valdrenn: espada y escudo, alabarda y ballesta; de guardia, velando de rodillas o tirada entre los muertos. */
public class GuardiaModelo extends EnemigoModelo<Guardia> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "guardia"), "main");

    private static final AnimationDefinition[] PORTE = {GuardiaAnimaciones.PORTE_ESPADA, GuardiaAnimaciones.PORTE_ALABARDA, GuardiaAnimaciones.PORTE_BALLESTA};
    private static final AnimationDefinition[] LISTO = {GuardiaAnimaciones.LISTO_ESPADA, GuardiaAnimaciones.LISTO_ALABARDA, GuardiaAnimaciones.LISTO_BALLESTA};

    public GuardiaModelo(ModelPart root) {
        super(root, GuardiaGeometria.OCULTAS_0, GuardiaGeometria.OCULTAS_1, GuardiaGeometria.OCULTAS_2);
    }

    @Override
    protected boolean animar(Guardia e, float paso, float cuanto, float edad, float guardia) {
        int v = Math.max(0, Math.min(2, e.variante()));
        int accion = e.accionVista();
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, GuardiaAnimaciones.VELAR, edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, GuardiaAnimaciones.TUMBADO, edad, 1f);
                default -> {
                    poner(e.reloj, GuardiaAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, PORTE[v], edad, 1f - guardia);
                    poner(e.reloj, LISTO[v], edad, guardia);
                    animateWalk(GuardiaAnimaciones.ANDAR, paso, cuanto, 1.5f, 2.2f);
                }
            }
        }
        for (int i = 1; i < GuardiaAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], GuardiaAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
