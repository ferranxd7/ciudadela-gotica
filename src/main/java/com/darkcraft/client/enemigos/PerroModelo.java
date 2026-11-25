package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Perro;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/** El perro de hueso: trota o galopa segun lo que corre; de guardia agachado; dormido (echado) o tirado entre los huesos. */
public class PerroModelo extends EnemigoModelo<Perro> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "perro"), "main");

    public PerroModelo(ModelPart root) {
        super(root, PerroGeometria.OCULTAS_0);
    }

    @Override
    protected boolean animar(Perro e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, PerroAnimaciones.ECHADO, edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, PerroAnimaciones.TUMBADO, edad, 1f);
                default -> {
                    poner(e.reloj, PerroAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, PerroAnimaciones.LISTO, edad, guardia);
                    // al paso, trote; deprisa, galope
                    float g = Math.min(1f, Math.max(0f, (cuanto - 0.45f) / 0.3f));
                    if (g < 1f) animateWalk(PerroAnimaciones.ANDAR, paso, cuanto * (1f - g), 1.6f, 2.0f);
                    if (g > 0f) animateWalk(PerroAnimaciones.CORRER, paso, cuanto * g, 1.1f, 2.0f);
                }
            }
        }
        for (int i = 1; i < PerroAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], PerroAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
