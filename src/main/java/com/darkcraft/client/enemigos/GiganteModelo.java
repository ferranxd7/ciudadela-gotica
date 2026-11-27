package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Gigante;
import com.darkcraft.enemigos.GiganteAnims;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Los gigantes errantes de Morvain (parte 7w): quieto respira hondo con lo suyo (la viga al hombro con la campana colgando a
 * la espalda, la costilla a rastras o la columna al hombro: su "porte", que se suma al quieto); en guardia, agachado con el arma
 * baja; andando, el paso lento de un gigante al ritmo de lo que avanza (un paso cada 3,2 bloques: el tiempo de la animacion es
 * lo andado). La roca que arranca solo se ve en su mano desde que la arranca hasta que la tira.
 */
public class GiganteModelo extends EnemigoModelo<Gigante> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "gigante"), "main");
    /** Lo que avanza la animacion de andar por cada punto del paso del juego (lo calcula {@code GiganteCliente}). */
    public static final float ESCALA_PASO = 2.5f;

    private static final AnimationDefinition[] PORTE = {GiganteAnimaciones.PORTE_CAMPANA, GiganteAnimaciones.PORTE_COSTILLA,
            GiganteAnimaciones.PORTE_COLUMNA};
    private final ModelPart roca;

    public GiganteModelo(ModelPart root) {
        super(root, GiganteGeometria.OCULTAS_0, GiganteGeometria.OCULTAS_1, GiganteGeometria.OCULTAS_2);
        roca = pieza("roca_mano");
    }

    @Override
    protected boolean animar(Gigante e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        int v = Mth.clamp(e.variante(), 0, 2);
        if (accion == 0) {
            poner(e.reloj, GiganteAnimaciones.QUIETO, edad, 1f - guardia);
            poner(e.reloj, PORTE[v], edad, 1f - guardia);
            poner(e.reloj, GiganteAnimaciones.LISTO, edad, guardia);
            animateWalk(GiganteAnimaciones.ANDAR, paso, cuanto, 2.5f, ESCALA_PASO);
        }
        for (int i = 1; i < GiganteAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], GiganteAnimaciones.ACCIONES[i], edad, 1f);
        // la roca: en la mano desde que la arranca hasta que la tira
        roca.visible = false;
        if (accion == GiganteAnims.ROCA && e.accionDesde >= 0) {
            float t = edad - e.accionDesde;
            int[] g = GiganteAnims.GOLPES[GiganteAnims.ROCA];
            roca.visible = t >= g[0] && t < g[1];
        }
        return accion == 0;
    }
}
