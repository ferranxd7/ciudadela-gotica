package com.darkcraft.client.montura;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.montura.Corcel;
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
 * El dibujante del corcel: el hueso, la silla y la manta ({@code corcel_0.png}) y, encima, a plena luz, lo que es niebla y
 * fuego ({@code corcel_0_brillo.png}: los ojos, la crin, la cola y el fuego de los cascos), que se apaga mientras se
 * deshace.
 */
public class CorcelRenderer extends MobRenderer<Corcel, CorcelModelo> {
    private static final ResourceLocation TEXTURA = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/enemigos/corcel_0.png");
    private static final ResourceLocation BRILLO = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/enemigos/corcel_0_brillo.png");

    public CorcelRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new CorcelModelo(ctx.bakeLayer(CorcelModelo.CAPA)), 0.75f);
        addLayer(new Brillo(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Corcel c) {
        return TEXTURA;
    }

    @Override
    protected float getFlipDegrees(Corcel c) {
        return 0f;
    }

    @Override
    protected boolean shouldShowName(Corcel c) {
        return false;
    }

    @Override
    public void render(Corcel c, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        shadowStrength = 1f - CorcelModelo.deshecho(c, pt);
        super.render(c, yaw, pt, ps, buf, luz);
    }

    private static final class Brillo extends RenderLayer<Corcel, CorcelModelo> {
        Brillo(RenderLayerParent<Corcel, CorcelModelo> padre) {
            super(padre);
        }

        @Override
        public void render(PoseStack ps, MultiBufferSource buf, int luz, Corcel c, float paso, float cuanto, float pt, float edad, float giro,
                           float inclinacion) {
            if (c.isInvisible()) return;
            float b = 1f - CorcelModelo.deshecho(c, pt) * 0.85f;
            // la niebla respira un poco
            b *= 0.86f + 0.14f * (float) Math.sin(edad * 0.12f);
            VertexConsumer vc = buf.getBuffer(RenderType.eyes(BRILLO));
            getParentModel().renderToBuffer(ps, vc, 0xF000F0, OverlayTexture.NO_OVERLAY, b, b, b, 1f);
        }
    }
}
