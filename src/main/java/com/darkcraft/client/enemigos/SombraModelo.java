package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Sombra;
import com.darkcraft.enemigos.SombraAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * La Sombra sin Nombre (parte 7ac; modelo de {@code tools/enemigos/sombra.py}): colgada de sus cadenas con los brazos en cruz mientras
 * espera ({@code REZA}), quieta o en guardia flotando, de rodillas en el aire cuando se rinde (y entonces su corazon ya no esta en su
 * pecho: flota delante, {@code morvain.Corazon}). El corazon del pecho late. Se dibuja algo transparente (para la niebla y al irse): el
 * dibujante le dice cuanto ({@link #alfa}).
 */
public class SombraModelo extends EnemigoModelo<Sombra> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "sombra"), "main");

    /** Lo opaco que se dibuja ahora (lo pone el dibujante antes de cada vez). */
    public float alfa = 1f;
    private final ModelPart corazon;

    public SombraModelo(ModelPart root) {
        super(root, RenderType::entityTranslucent, SombraGeometria.OCULTAS_0, SombraGeometria.OCULTAS_1);
        corazon = pieza("corazon");
    }

    /** Una pieza por su nombre (para el dibujante: las munecas y los ojos). */
    public ModelPart parte(String nombre) {
        return pieza(nombre);
    }

    @Override
    protected boolean animar(Sombra e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        boolean rendida = e.rendida() || e.deathTime > 0;
        if (accion == 0) {
            if (rendida) poner(e.reloj, SombraAnimaciones.RODILLAS, edad, 1f);
            else if (e.postura() == Enemigo.REZA) poner(e.reloj, SombraAnimaciones.ATADA, edad, 1f);
            else {
                poner(e.reloj, SombraAnimaciones.QUIETO, edad, 1f - guardia);
                poner(e.reloj, SombraAnimaciones.LISTO, edad, guardia);
                animateWalk(SombraAnimaciones.ANDAR, paso, cuanto, 1.2f, 1.8f);
            }
        }
        for (int i = 1; i < SombraAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], SombraAnimaciones.ACCIONES[i], edad, 1f);
        // el corazon late (un golpe doble cada segundo y algo); rendida, ya no esta en su pecho
        float t = (edad % 22f) / 22f;
        float latido = (float) (Math.exp(-Math.pow((t - 0.06f) / 0.05f, 2)) + 0.7 * Math.exp(-Math.pow((t - 0.24f) / 0.06f, 2)));
        float s = 1f + 0.18f * Mth.clamp(latido, 0f, 1f) * (e.furia() ? 1.4f : 1f);
        corazon.xScale = corazon.yScale = corazon.zScale = s;
        corazon.visible = !(rendida && (accion != SombraAnims.CAE || e.accionDesde < 0 || edad - e.accionDesde > Sombra.SALE_CORAZON));
        return accion == 0 && e.postura() == Enemigo.DE_PIE && !rendida;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int luz, int capa, float r, float g, float b, float a) {
        super.renderToBuffer(ps, vc, luz, capa, r, g, b, a * alfa);
    }
}
