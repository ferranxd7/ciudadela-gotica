package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Dragon;
import com.darkcraft.enemigos.DragonAnims;
import com.darkcraft.enemigos.Enemigo;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/**
 * Vorthak, el Dragon de Ceniza (parte 7z; modelo de {@code tools/enemigos/dragon.py}). En tierra: dormido sobre su oro (postura
 * de rodillas), quieto, en guardia (peleando) o andando (el paso al ritmo de lo que avanza: {@link Dragon#fase}). En el aire:
 * aleteando o planeando (planea si va deprisa sin subir; aletea si sube, va despacio o se queda en el aire). El vuelo rasante y
 * escupir solo mueven el cuello: van encima del bucle que toque; las demas acciones, solas. La cabeza no sigue la mirada (el
 * cuello es largo: lo llevan las animaciones).
 */
public class DragonModelo extends EnemigoModelo<Dragon> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "dragon"), "main");
    /** Bloques que avanza en un ciclo del paso (la animacion dura 2,4 s). */
    public static final float CICLO_ANDAR = 5.0f;
    private static final Vector3f CACHE = new Vector3f();

    public DragonModelo(ModelPart root) {
        super(root, DragonGeometria.OCULTAS_0);
    }

    /** Un bucle en el segundo que diga {@code segundos} (no el reloj del mundo: el de lo que avanza), con su peso. */
    private void bucle(AnimationDefinition d, float segundos, float peso) {
        if (peso <= 0.001f) return;
        KeyframeAnimations.animate(this, d, (long) (segundos * 1000f), peso, CACHE);
    }

    /** Lo que planea (0 = aletea, 1 = planea) volando ahora. */
    public static float planea(Dragon e, float pt) {
        float v = Mth.lerp(pt, e.velocidadCO, e.velocidadC);
        return Mth.clamp((v - 0.45f) / 0.2f, 0f, 1f) * Mth.clamp((0.04f - e.subida) / 0.1f, 0f, 1f);
    }

    @Override
    protected boolean animar(Dragon e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        float pt = Mth.clamp(edad - e.tickCount, 0f, 1f);
        boolean encima = accion == DragonAnims.RASANTE || accion == DragonAnims.ESCUPE;
        if ((accion == 0 || encima) && !e.muriendo()) {
            if (e.vuela()) {
                float planea = planea(e, pt);
                poner(e.reloj, DragonAnimaciones.VUELO, edad, 1f - planea);
                poner(e.reloj, DragonAnimaciones.PLANEO, edad, planea);
            } else if (e.postura() != Enemigo.DE_PIE) {
                poner(e.reloj, DragonAnimaciones.DORMIDO, edad, 1f);
            } else {
                float v = Mth.lerp(pt, e.velocidadCO, e.velocidadC);
                float mov = Mth.clamp(v / 0.05f, 0f, 1f);
                float fase = Mth.lerp(pt, e.faseO, e.fase);
                poner(e.reloj, DragonAnimaciones.QUIETO, edad, (1f - guardia) * (1f - mov));
                poner(e.reloj, DragonAnimaciones.LISTO, edad, guardia * (1f - mov));
                bucle(DragonAnimaciones.ANDAR, fase / CICLO_ANDAR * 2.4f, mov);
            }
        }
        for (int i = 1; i < DragonAnimaciones.ACCIONES.length; i++)
            if (DragonAnimaciones.ACCIONES[i] != null) poner(e.acciones[i], DragonAnimaciones.ACCIONES[i], edad, 1f);
        return false;
    }
}
