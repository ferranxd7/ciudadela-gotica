package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Penitente;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/**
 * El penitente de la Tierra sin Fe: de pie, la cabeza gacha y la izquierda sobre el pecho, con lo suyo en la derecha (la
 * disciplina colgando, la cruz a rastras o el incensario colgando por delante: su "porte", que se suma al quieto); de
 * rodillas cada uno lo suyo ({@code REZA}: el flagelante se azota, el portador reza con las manos en la cruz plantada, el
 * turiferario mece el incensario) o postrado en cruz, boca abajo ({@code TUMBADO}).
 */
public class PenitenteModelo extends EnemigoModelo<Penitente> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "penitente"), "main");

    public PenitenteModelo(ModelPart root) {
        super(root, PenitenteGeometria.OCULTAS_0, PenitenteGeometria.OCULTAS_1, PenitenteGeometria.OCULTAS_2);
    }

    private static AnimationDefinition de(int v, AnimationDefinition flagelante, AnimationDefinition portador, AnimationDefinition turiferario) {
        return v == Penitente.PORTADOR ? portador : v == Penitente.TURIFERARIO ? turiferario : flagelante;
    }

    @Override
    protected boolean animar(Penitente e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        int v = e.variante();
        if (accion == 0) {
            switch (e.postura()) {
                case Enemigo.REZA -> poner(e.reloj, de(v, PenitenteAnimaciones.REZANDO_DISCIPLINA, PenitenteAnimaciones.REZANDO_CRUZ,
                        PenitenteAnimaciones.REZANDO_INCENSARIO), edad, 1f);
                case Enemigo.TUMBADO -> poner(e.reloj, PenitenteAnimaciones.POSTRADO, edad, 1f);
                default -> {
                    poner(e.reloj, PenitenteAnimaciones.QUIETO, edad, 1f - guardia);
                    poner(e.reloj, de(v, PenitenteAnimaciones.PORTE_DISCIPLINA, PenitenteAnimaciones.PORTE_CRUZ,
                            PenitenteAnimaciones.PORTE_INCENSARIO), edad, 1f - guardia);
                    poner(e.reloj, de(v, PenitenteAnimaciones.LISTO_DISCIPLINA, PenitenteAnimaciones.LISTO_CRUZ,
                            PenitenteAnimaciones.LISTO_INCENSARIO), edad, guardia);
                    animateWalk(PenitenteAnimaciones.ANDAR, paso, cuanto, 1.5f, 2.3f);
                }
            }
        }
        for (int i = 1; i < PenitenteAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], PenitenteAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
