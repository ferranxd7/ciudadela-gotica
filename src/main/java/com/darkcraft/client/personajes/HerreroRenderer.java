package com.darkcraft.client.personajes;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.personajes.Herrero;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Dorvan: el modelo de jugador de brazos anchos con su piel (barba y delantal en las capas de encima) y el martillo en la
 * derecha. Martillea: sube el martillo despacio, lo baja de golpe sobre el yunque (el golpe cae en el tick
 * {@link Herrero#GOLPE} de cada {@link Herrero#CICLO}, cuando el cliente pone las chispas y el sonido) y descansa un
 * momento; la izquierda sujeta la pieza con las tenazas y la cabeza mira al yunque. Si alguien se acerca, deja el
 * martillo abajo y le mira ({@link Herrero#atencion}).
 */
public class HerreroRenderer extends LivingEntityRenderer<Herrero, HerreroRenderer.Modelo> {
    private static final ResourceLocation PIEL = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/herrero.png");

    public HerreroRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new Modelo(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        addLayer(new ItemInHandLayer<>(this, ctx.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(Herrero h) {
        return PIEL;
    }

    @Override
    protected boolean shouldShowName(Herrero h) {
        return false;
    }

    /** Lo alto del brazo del martillo (xRot) en cada momento del martillazo (f de 0 a 1). */
    static float martillo(float f) {
        if (f < 0.55f) {
            float k = f / 0.55f;
            k = k * k * (3 - 2 * k);
            return Mth.lerp(k, -0.25f, -2.55f);
        }
        if (f < 0.667f) {
            float k = (f - 0.55f) / 0.117f;
            return Mth.lerp(k * k, -2.55f, -0.25f);
        }
        if (f < 0.75f) return -0.25f - 0.12f * Mth.sin((f - 0.667f) / 0.083f * Mth.PI);
        return -0.25f;
    }

    public static class Modelo extends PlayerModel<Herrero> {
        public Modelo(ModelPart raiz) {
            super(raiz, false);
        }

        @Override
        public void setupAnim(Herrero h, float paso, float rapidez, float edad, float cabezaY, float cabezaX) {
            riding = false;
            super.setupAnim(h, 0f, 0f, edad, cabezaY, cabezaX);
            float pt = Mth.clamp(edad - h.tickCount, 0f, 1f);
            float a = Mth.lerp(pt, h.atencionO, h.atencion);
            a = a * a * (3 - 2 * a);
            float f = ((h.ciclo + (a < 0.02f ? pt : 0f)) % Herrero.CICLO) / Herrero.CICLO;
            float respira = Mth.sin(edad * 0.07f) * 0.02f;
            // martilleando (a = 0) o mirando a quien llega (a = 1)
            rightArm.xRot = Mth.lerp(a, martillo(f), -0.12f + respira);
            rightArm.yRot = Mth.lerp(a, -0.10f, 0f);
            rightArm.zRot = Mth.lerp(a, 0.08f, 0.07f);
            leftArm.xRot = Mth.lerp(a, -0.85f + respira, -0.05f - respira);
            leftArm.yRot = Mth.lerp(a, 0.45f, 0f);
            leftArm.zRot = Mth.lerp(a, 0f, -0.07f);
            head.xRot = Mth.lerp(a, 0.55f, head.xRot);
            head.yRot = Mth.lerp(a, 0.08f, head.yRot);
            hat.copyFrom(head);
            body.xRot = Mth.lerp(a, 0.12f, 0f);
            rightSleeve.copyFrom(rightArm);
            leftSleeve.copyFrom(leftArm);
            rightPants.copyFrom(rightLeg);
            leftPants.copyFrom(leftLeg);
            jacket.copyFrom(body);
        }
    }
}
