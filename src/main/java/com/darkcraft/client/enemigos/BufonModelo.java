package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Bufon;
import com.darkcraft.enemigos.BufonAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Pell, el Bufon del Rey (parte 7y): quieto da saltitos sin moverse del sitio y ladea la cabeza; en guardia, agachado con las dagas
 * por delante; andando, a saltitos con las rodillas altas (se mezcla segun lo que anda). Las tres bolas de fuego solo se ven en
 * los malabares (hasta que las tira). Hay dos: el de siempre y el de sus ilusiones (medio transparente: casi no se nota).
 */
public class BufonModelo extends EnemigoModelo<Bufon> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "bufon"), "main");

    private final boolean ilusion;
    private final ModelPart malabar;
    /** Lo opaco que se dibuja la ilusion (lo pone el dibujante). */
    public float alfa = 0.82f;

    public BufonModelo(ModelPart root) {
        super(root, BufonGeometria.OCULTAS_0);
        ilusion = false;
        malabar = pieza("malabar");
    }

    /** El de las ilusiones: medio transparente. */
    public BufonModelo(ModelPart root, boolean ilusion) {
        super(root, RenderType::entityTranslucent, BufonGeometria.OCULTAS_0);
        this.ilusion = ilusion;
        malabar = pieza("malabar");
    }

    @Override
    protected boolean animar(Bufon e, float paso, float cuanto, float edad, float guardia) {
        int accion = e.accionVista();
        if (accion == 0) {
            float anda = Mth.clamp(cuanto * 2.2f, 0f, 1f);
            poner(e.reloj, BufonAnimaciones.QUIETO, edad, (1f - guardia) * (1f - anda));
            poner(e.reloj, BufonAnimaciones.LISTO, edad, guardia * (1f - anda));
            animateWalk(BufonAnimaciones.ANDAR, paso, cuanto, 1.6f, 2.4f);
        }
        for (int i = 1; i < BufonAnimaciones.ACCIONES.length; i++) poner(e.acciones[i], BufonAnimaciones.ACCIONES[i], edad, 1f);
        // las bolas de los malabares: hasta que tira la ultima
        if (accion == BufonAnims.MALABARES) {
            float hecho = e.accionDesde < 0 ? 0f : edad - e.accionDesde;
            malabar.visible = hecho < BufonAnims.GOLPES[BufonAnims.MALABARES][2];
        }
        return accion == 0;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int luz, int capa, float r, float g, float b, float a) {
        if (ilusion) super.renderToBuffer(ps, vc, luz, capa, r, g, b, a * alfa);
        else super.renderToBuffer(ps, vc, luz, capa, r, g, b, a);
    }
}
