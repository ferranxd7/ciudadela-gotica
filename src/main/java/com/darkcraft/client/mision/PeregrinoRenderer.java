package com.darkcraft.client.mision;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.client.personajes.ViajeroRenderer;
import com.darkcraft.mision.Peregrino;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Los peregrinos muertos (parte 7ag): el modelo de Oswin sin bordon ({@link ViajeroRenderer.Modelo}), con su piel palida de muerto
 * ({@code textures/entity/peregrino.png}, de {@code tools/personajes/oswin.py}), medio transparentes ({@link Peregrino#alfa}) y con su
 * propia luz; andan rezando hacia el norte y se apagan.
 */
public class PeregrinoRenderer extends LivingEntityRenderer<Peregrino, ViajeroRenderer.Modelo<Peregrino>> {
    private static final ResourceLocation PIEL = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/peregrino.png");

    public PeregrinoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ViajeroRenderer.Modelo<>(ctx.bakeLayer(ViajeroRenderer.CAPA)), 0f);
    }

    @Override
    public void render(Peregrino p, float giro, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        float a = p.alfa(pt);
        if (a <= 0.02f) return;
        getModel().alfa = a;
        getModel().toca = 0f;
        super.render(p, giro, pt, ps, buf, LightTexture.FULL_BRIGHT);
    }

    @Override
    public ResourceLocation getTextureLocation(Peregrino p) {
        return PIEL;
    }

    @Override
    protected boolean shouldShowName(Peregrino p) {
        return false;
    }
}
