package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Galen;
import com.darkcraft.enemigos.GalenAnims;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/**
 * Maese Galen (parte 7av): esperando ({@code REZA}) destila junto al gran alambique, con la jeringa al trasluz; de pie, encorvado con la
 * jeringa colgando y el bisturi en la izquierda (su "porte", que se suma al quieto) o en guardia con la aguja por delante. Los tubos de
 * su alambique le entran en el cuello hasta que se los arranca (entonces se ven los cabos); despues de beberse el elixir (la variante 1)
 * le brota el cristal. Mientras bebe lleva el frasco grande en la izquierda (y no el bisturi).
 */
public class GalenModelo extends EnemigoModelo<Galen> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "galen"), "main");

    private final ModelPart tubos, tubosRotos, frasco, bisturi;

    public GalenModelo(ModelPart root) {
        super(root, GalenGeometria.OCULTAS_0, GalenGeometria.OCULTAS_1);
        tubos = pieza("tubos");
        tubosRotos = pieza("tubos_rotos");
        frasco = pieza("frasco_grande");
        bisturi = pieza("bisturi");
    }

    @Override
    protected boolean animar(Galen e, float paso, float cuanto, float edad, float guardia) {
        // los tubos: enteros hasta que se los arranca (antes de que le brote el cristal)
        boolean rotos = e.arrancados() || e.bebido();
        tubos.visible = !rotos;
        tubosRotos.visible = rotos;
        int accion = e.accionVista();
        // bebiendo: el frasco grande en la izquierda (del segundo 1,1 al 3,7 de la accion, cuando lo coge del cinto y lo tira)
        boolean bebe = false;
        if (accion == GalenAnims.BEBER) {
            float t = (e.tickCount - e.accionDesde) / 20f;
            bebe = t >= 1.1f && t < 3.75f;
        }
        frasco.visible = bebe;
        bisturi.visible = !bebe;
        if (accion == 0) {
            if (e.postura() != Enemigo.DE_PIE) poner(e.reloj, GalenAnimaciones.DESTILANDO, edad, 1f);
            else {
                poner(e.reloj, GalenAnimaciones.QUIETO, edad, 1f - guardia);
                poner(e.reloj, GalenAnimaciones.PORTE, edad, 1f - guardia);
                poner(e.reloj, GalenAnimaciones.LISTO, edad, guardia);
                animateWalk(GalenAnimaciones.ANDAR, paso, cuanto, e.bebido() ? 1.8f : 1.5f, 2.2f);
            }
        }
        for (int i = 1; i < GalenAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], GalenAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
