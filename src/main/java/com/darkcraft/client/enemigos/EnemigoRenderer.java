package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * El dibujante de los enemigos de tools/enemigos: una textura por variante ({@code textures/entity/enemigos/NOMBRE_V.png})
 * y encima su capa de lo que brilla ({@code NOMBRE_V_brillo.png}: ojos, llamas), a plena luz. Al morir hacen su
 * animacion: ni la voltereta de lado ni el rojo de Minecraft mientras dura.
 */
public class EnemigoRenderer<T extends Enemigo, M extends EnemigoModelo<T>> extends MobRenderer<T, M> {
    private final ResourceLocation[] texturas, brillos;
    private final float escala;

    public EnemigoRenderer(EntityRendererProvider.Context ctx, M modelo, String nombre, int variantes, float sombra, float escala) {
        super(ctx, modelo, sombra);
        this.escala = escala;
        texturas = new ResourceLocation[variantes];
        brillos = new ResourceLocation[variantes];
        for (int v = 0; v < variantes; v++) {
            texturas[v] = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/enemigos/" + nombre + "_" + v + ".png");
            brillos[v] = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/enemigos/" + nombre + "_" + v + "_brillo.png");
        }
        addLayer(new Brillo(this));
    }

    private int variante(T e) {
        return Math.max(0, Math.min(texturas.length - 1, e.variante()));
    }

    @Override
    public ResourceLocation getTextureLocation(T e) {
        return texturas[variante(e)];
    }

    @Override
    protected float getFlipDegrees(T e) {
        return 0f;
    }

    @Override
    protected void scale(T e, PoseStack ps, float pt) {
        if (escala != 1f) ps.scale(escala, escala, escala);
    }

    @Override
    public void render(T e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        // mientras cae no va en rojo todo el rato (solo el instante del golpe)
        int muerte = e.deathTime;
        if (e.muriendo() && muerte > 5) e.deathTime = 0;
        try {
            super.render(e, yaw, pt, ps, buf, luz);
        } finally {
            e.deathTime = muerte;
        }
    }

    private final class Brillo extends RenderLayer<T, M> {
        Brillo(RenderLayerParent<T, M> padre) {
            super(padre);
        }

        @Override
        public void render(PoseStack ps, MultiBufferSource buf, int luz, T e, float paso, float cuanto, float pt, float edad, float giro, float inclinacion) {
            if (e.isInvisible()) return;
            VertexConsumer vc = buf.getBuffer(RenderType.eyes(brillos[variante(e)]));
            getParentModel().renderToBuffer(ps, vc, 0xF000F0, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        }
    }
}
