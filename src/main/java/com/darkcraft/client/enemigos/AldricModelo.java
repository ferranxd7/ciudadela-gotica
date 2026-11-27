package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Aldric;
import com.darkcraft.enemigos.Enemigo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Aldric, el Primer Marcado (parte 7v): de rodillas en lo alto del ancla con la cabeza gacha (la postura de rezar) o de pie con
 * la cadena enrollada al brazo; quieto respira hondo, en guardia se mece y andando arrastra la cadena por el suelo (se mezcla
 * segun lo que anda). Hay dos: el de siempre y el del reflejo (medio transparente y oscuro: lo elige el dibujante).
 */
public class AldricModelo extends EnemigoModelo<Aldric> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "aldric"), "main");

    private final boolean reflejo;
    /** Lo opaco que se dibuja el reflejo (lo pone el dibujante). */
    public float alfa = 0.5f;

    public AldricModelo(ModelPart root) {
        super(root, AldricGeometria.OCULTAS_0);
        reflejo = false;
    }

    /** El del reflejo: medio transparente. */
    public AldricModelo(ModelPart root, boolean reflejo) {
        super(root, RenderType::entityTranslucent, AldricGeometria.OCULTAS_0);
        this.reflejo = reflejo;
    }

    @Override
    protected boolean animar(Aldric e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0) {
            if (e.postura() != Enemigo.DE_PIE) poner(e.reloj, AldricAnimaciones.REZANDO, edad, 1f);
            else {
                // andando arrastra la cadena (pesa mas cuanto mas anda); parado, quieto o en guardia
                float anda = Mth.clamp(cuanto * 2.4f, 0f, 1f);
                poner(e.reloj, AldricAnimaciones.QUIETO, edad, (1f - guardia) * (1f - anda));
                poner(e.reloj, AldricAnimaciones.LISTO, edad, guardia * (1f - anda));
                poner(e.reloj, AldricAnimaciones.ARRASTRA, edad, anda);
                animateWalk(AldricAnimaciones.ANDAR, paso, cuanto, 1.3f, 2.0f);
            }
        }
        for (int i = 1; i < AldricAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], AldricAnimaciones.ACCIONES[i], edad, 1f);
        return accion == 0 && e.postura() == Enemigo.DE_PIE;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int luz, int capa, float r, float g, float b, float a) {
        // el reflejo: una sombra negra con un poco de rojo, medio transparente
        if (reflejo) super.renderToBuffer(ps, vc, luz, capa, r * 0.3f, g * 0.1f, b * 0.12f, a * alfa);
        else super.renderToBuffer(ps, vc, luz, capa, r, g, b, a);
    }
}
