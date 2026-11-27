package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Jinete;
import com.darkcraft.enemigos.JineteAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/**
 * Ser Lucan, el Jinete Nocturno (parte 7x; modelo de {@code tools/enemigos/jinete.py}): el caballo y el jinete van en bucles
 * aparte que se suman. El caballo: parado, su quieto; andando, el paso y el galope mezclados segun lo que corre, cada ciclo al
 * ritmo de lo que avanza ({@link Jinete#fase}). El jinete: derecho con la guja de pie, o echado hacia delante al galope. Las
 * acciones que solo mueven al jinete (el tajo, el barrido y la carga) van encima del bucle del caballo; las demas (el pisoton,
 * el cuerno, tropezar, aparecer, deshacerse y morir), solas. El cuerno de la mano solo se ve mientras lo toca (el del cinto,
 * entonces, no). Es un fantasma: se dibuja casi opaco, algo azulado, y el dibujante le dice cuanto ({@link #alfa}).
 */
public class JineteModelo extends EnemigoModelo<Jinete> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "jinete"), "main");
    /** Bloques que avanza en un ciclo del paso y en uno del galope. */
    public static final float CICLO_PASO = 2.6f, CICLO_GALOPE = 4.6f;
    private static final Vector3f CACHE = new Vector3f();

    /** Lo opaco que se dibuja ahora (lo pone el dibujante antes de cada vez). */
    public float alfa = 1f;
    private final ModelPart cuernoMano, cuernoCinto;

    public JineteModelo(ModelPart root) {
        super(root, RenderType::entityTranslucent, JineteGeometria.OCULTAS_0);
        cuernoMano = pieza("cuerno_mano");
        cuernoCinto = pieza("cuerno_cinto");
    }

    /** Un bucle en el segundo que diga {@code segundos} (no el reloj del mundo: el de lo que avanza), con su peso. */
    private void bucle(AnimationDefinition d, float segundos, float peso) {
        if (peso <= 0.001f) return;
        KeyframeAnimations.animate(this, d, (long) (segundos * 1000f), peso, CACHE);
    }

    @Override
    protected boolean animar(Jinete e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        float pt = Mth.clamp(edad - e.tickCount, 0f, 1f);
        float v = Mth.lerp(pt, e.velocidadO, e.velocidad);
        float fase = Mth.lerp(pt, e.faseO, e.fase);
        boolean soloJinete = accion == JineteAnims.TAJO || accion == JineteAnims.BARRIDO || accion == JineteAnims.CARGA;
        if ((accion == 0 || soloJinete) && !e.muriendo()) {
            float mov = Mth.clamp(v / 0.06f, 0f, 1f);
            float galope = Mth.clamp((v - 0.24f) / 0.16f, 0f, 1f);
            // el caballo
            poner(e.reloj, JineteAnimaciones.QUIETO, edad, 1f - mov);
            bucle(JineteAnimaciones.ANDAR, fase / CICLO_PASO * 1.4f, mov * (1f - galope));
            bucle(JineteAnimaciones.GALOPE, fase / CICLO_GALOPE * 0.7f, mov * galope);
            // el jinete, si no hace nada
            if (accion == 0) {
                poner(e.reloj, JineteAnimaciones.JINETE_QUIETO, edad, 1f - mov * galope);
                bucle(JineteAnimaciones.JINETE_GALOPE, fase / CICLO_GALOPE * 0.7f, mov * galope);
            }
        }
        for (int i = 1; i < JineteAnimaciones.ACCIONES.length; i++)
            if (JineteAnimaciones.ACCIONES[i] != null) poner(e.acciones[i], JineteAnimaciones.ACCIONES[i], edad, 1f);
        boolean cuerno = accion == JineteAnims.CUERNO;
        cuernoMano.visible = cuerno;
        cuernoCinto.visible = !cuerno;
        return accion == 0;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int luz, int capa, float r, float g, float b, float a) {
        // algo azulado y frio, y casi opaco (un fantasma que pesa)
        super.renderToBuffer(ps, vc, luz, capa, r * 0.9f, g * 0.95f, b, a * alfa);
    }
}
