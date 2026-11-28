package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Osmund;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

/**
 * Fray Osmund (parte 7at): esperando ({@code REZA}) de rodillas, contando huesos; de pie, encorvado con el incensario colgando por
 * delante y la campanilla al costado (su "porte", que se suma al quieto) o en guardia con el incensario meciendose a su derecha; a
 * media vida flota (las piernas colgando) sobre el haz de brazos de hueso que le sostiene, que solo se ve entonces. Con la corona
 * fuera (las calaveras de su aureola orbitando o volando) el aro se queda sin ellas.
 */
public class OsmundModelo extends EnemigoModelo<Osmund> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "osmund"), "main");

    private final ModelPart[] calaveras = new ModelPart[5];
    private final ModelPart[] sosten;

    public OsmundModelo(ModelPart root) {
        super(root, OsmundGeometria.OCULTAS_0);
        for (int i = 0; i < calaveras.length; i++) calaveras[i] = pieza("calavera_" + i);
        String[] s = {"sosten_a", "sosten_b", "sosten_c", "sosten_d", "sosten_e", "sosten_craneos"};
        sosten = new ModelPart[s.length];
        for (int i = 0; i < s.length; i++) sosten[i] = pieza(s[i]);
    }

    @Override
    protected boolean animar(Osmund e, float paso, float cuanto, float edad, float guardia) {
        // las calaveras de la aureola: fuera mientras orbitan o vuelan
        boolean corona = e.corona();
        for (ModelPart p : calaveras) p.visible = !corona;
        // lo que le sostiene en el aire: solo alzado (y no mientras se muere: se le cae)
        boolean alzado = e.alzado() && !e.isDeadOrDying();
        for (ModelPart p : sosten) p.visible = alzado;
        int accion = e.accionVista();
        if (accion == 0) {
            if (e.postura() != Enemigo.DE_PIE) poner(e.reloj, OsmundAnimaciones.CONTANDO, edad, 1f);
            else if (alzado) poner(e.reloj, OsmundAnimaciones.FLOTANDO, edad, 1f);
            else {
                poner(e.reloj, OsmundAnimaciones.QUIETO, edad, 1f - guardia);
                poner(e.reloj, OsmundAnimaciones.PORTE, edad, 1f - guardia);
                poner(e.reloj, OsmundAnimaciones.LISTO, edad, guardia);
                animateWalk(OsmundAnimaciones.ANDAR, paso, cuanto, 1.2f, 2.0f);
            }
        }
        for (int i = 1; i < OsmundAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], OsmundAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }
}
