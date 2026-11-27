package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Gargola;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * La gargola de la cadena del sur (parte 7aa; modelo de {@code tools/enemigos/gargola.py}). Posada en su pedestal, una estatua
 * ({@code POSADA}, sin brillo: {@link GargolaRenderer}); en tierra, quieta o en guardia y andando; en el aire, cerniendose si va
 * despacio o con el vuelo tendido si avanza (lo que se ve que avanza: el paso del juego, que el cliente calcula de lo que se
 * mueve). Las acciones van solas (sus poses ya llevan las alas).
 */
public class GargolaModelo extends EnemigoModelo<Gargola> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "gargola"), "main");

    public GargolaModelo(ModelPart root) {
        super(root, GargolaGeometria.OCULTAS_0);
    }

    @Override
    protected boolean animar(Gargola e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0 && !e.muriendo()) {
            if (e.vuela()) {
                float avanza = Mth.clamp(cuanto * 1.4f, 0f, 1f);
                poner(e.reloj, GargolaAnimaciones.VUELO, edad, avanza);
                poner(e.reloj, GargolaAnimaciones.CERNE, edad, 1f - avanza);
            } else if (e.postura() != Enemigo.DE_PIE) {
                poner(e.reloj, GargolaAnimaciones.POSADA, edad, 1f);
            } else {
                poner(e.reloj, GargolaAnimaciones.QUIETO, edad, 1f - guardia);
                poner(e.reloj, GargolaAnimaciones.LISTO, edad, guardia);
                animateWalk(GargolaAnimaciones.ANDAR, paso, cuanto, 1.6f, 2.4f);
            }
        }
        for (int i = 1; i < GargolaAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], GargolaAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && !e.vuela() && e.postura() == Enemigo.DE_PIE;
    }
}
