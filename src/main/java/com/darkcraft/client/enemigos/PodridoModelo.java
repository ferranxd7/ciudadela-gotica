package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Podrido;
import com.darkcraft.enemigos.PodridoAnims;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/**
 * El podrido del pantano: de pie, encorvado y con los brazos colgando (el del fuego lo lleva delante, en la palma); flotando
 * boca abajo en el agua o tendido en el barro ({@code TUMBADO}, segun {@link #flota}); de rodillas.
 */
public class PodridoModelo extends EnemigoModelo<Podrido> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "podrido"), "main");

    private final ModelPart fatuo, halo;
    /** Lo pone el dibujante antes de cada dibujo: si esta en el agua (flota) o en tierra (tendido). */
    boolean flota;

    public PodridoModelo(ModelPart root) {
        super(root, PodridoGeometria.OCULTAS_0, PodridoGeometria.OCULTAS_1, PodridoGeometria.OCULTAS_2);
        fatuo = pieza("fatuo");
        halo = pieza("fatuo_halo");
    }

    @Override
    protected boolean animar(Podrido e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, PodridoAnimaciones.RODILLAS, edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, flota ? PodridoAnimaciones.HUNDIDO : PodridoAnimaciones.TENDIDO, edad, 1f);
                default -> {
                    poner(e.reloj, PodridoAnimaciones.QUIETO, edad, 1f - guardia);
                    if (e.variante() == Podrido.FATUO) poner(e.reloj, PodridoAnimaciones.PORTE_FATUO, edad, 1f - guardia);
                    poner(e.reloj, PodridoAnimaciones.LISTO_PODRIDO, edad, guardia);
                    animateWalk(PodridoAnimaciones.ANDAR, paso, cuanto, 1.6f, 2.4f);
                }
            }
        }
        for (int i = 1; i < PodridoAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], PodridoAnimaciones.ACCIONES[i], edad, 1f);
        // el fuego que acaba de lanzar no esta en la mano hasta que acaba la accion (le vuelve a prender despues)
        if (accion == PodridoAnims.LANZAR_FATUO && e.accionDesde >= 0 && edad - e.accionDesde >= PodridoAnims.GOLPES[PodridoAnims.LANZAR_FATUO][0]) {
            fatuo.visible = false;
            halo.visible = false;
        }
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
