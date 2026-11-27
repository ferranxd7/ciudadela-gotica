package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Ballenero;
import com.darkcraft.enemigos.BalleneroAnims;
import com.darkcraft.enemigos.Enemigo;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/**
 * Brann, el Ballenero (parte 7r): de vigia (la postura de rezar: de rodilla en la arena, el arpon de pie a su lado y la cabeza
 * alta, mirando al mar que no esta) o de pie con el arpon y el farol. Mientras el arpon esta fuera (lanzado, atado a su cabo)
 * no se dibuja en la mano; el farol, desde que lo lanza hasta que acaba (entonces enciende otro).
 */
public class BalleneroModelo extends EnemigoModelo<Ballenero> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "ballenero"), "main");

    private final ModelPart arpon, farol;

    public BalleneroModelo(ModelPart root) {
        super(root, BalleneroGeometria.OCULTAS_0);
        arpon = pieza("arpon");
        farol = pieza("farol");
    }

    @Override
    protected boolean animar(Ballenero e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0) {
            if (e.postura() != Enemigo.DE_PIE) poner(e.reloj, BalleneroAnimaciones.VIGIA, edad, 1f);
            else {
                poner(e.reloj, BalleneroAnimaciones.QUIETO, edad, 1f - guardia);
                poner(e.reloj, BalleneroAnimaciones.LISTO, edad, guardia);
                animateWalk(BalleneroAnimaciones.ANDAR, paso, cuanto, 1.3f, 2.0f);
            }
        }
        for (int i = 1; i < BalleneroAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], BalleneroAnimaciones.ACCIONES[i], edad, 1f);
        arpon.visible = !e.arponFuera();
        farol.visible = true;
        if (accion == BalleneroAnims.FAROL) {
            long ms = e.acciones[accion].getAccumulatedTime();
            if (ms >= BalleneroAnims.GOLPES[accion][0] * 50L && ms < (BalleneroAnims.TICKS[accion] - 3) * 50L) farol.visible = false;
        }
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
