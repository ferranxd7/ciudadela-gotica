package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Enterrado;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/**
 * El enterrado de las Minas de Hierro Negro (parte 7aq): de pie, encorvado y con la cabeza ladeada (escucha), con lo suyo (el pico a
 * dos manos, el cartucho alzado, la almadena al hombro: su "porte", que se suma al quieto); esperando ({@code REZA}) el picador pica
 * la roca, el barrenero esta agazapado y el cargador sentado; enterrado en los escombros ({@code TUMBADO}), de bruces.
 */
public class EnterradoModelo extends EnemigoModelo<Enterrado> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "enterrado"), "main");

    public EnterradoModelo(ModelPart root) {
        super(root, EnterradoGeometria.OCULTAS_0, EnterradoGeometria.OCULTAS_1, EnterradoGeometria.OCULTAS_2);
    }

    private static AnimationDefinition de(int v, AnimationDefinition picador, AnimationDefinition barrenero, AnimationDefinition cargador) {
        return v == Enterrado.BARRENERO ? barrenero : v == Enterrado.CARGADOR ? cargador : picador;
    }

    @Override
    protected boolean animar(Enterrado e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        int v = e.variante();
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, de(v, EnterradoAnimaciones.PICANDO, EnterradoAnimaciones.AGAZAPADO, EnterradoAnimaciones.SENTADO), edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, EnterradoAnimaciones.ENTERRADO, edad, 1f);
                default -> {
                    poner(e.reloj, EnterradoAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, de(v, EnterradoAnimaciones.PORTE_PICADOR, EnterradoAnimaciones.PORTE_BARRENERO, EnterradoAnimaciones.PORTE_CARGADOR),
                            edad, 1f - guardia);
                    poner(e.reloj, de(v, EnterradoAnimaciones.LISTO_PICADOR, EnterradoAnimaciones.LISTO_BARRENERO, EnterradoAnimaciones.LISTO_CARGADOR),
                            edad, guardia);
                    animateWalk(EnterradoAnimaciones.ANDAR, paso, cuanto, v == Enterrado.CARGADOR ? 1.3f : 1.6f, 2.4f);
                }
            }
        }
        for (int i = 1; i < EnterradoAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], EnterradoAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
