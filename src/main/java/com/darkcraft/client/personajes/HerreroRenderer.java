package com.darkcraft.client.personajes;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.personajes.Herrero;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Los herreros: el modelo de jugador de brazos anchos con la piel de cada uno ({@code textures/entity/<id>.png}: barba y
 * delantal en las capas de encima) y el martillo en la derecha, a su tamano ({@link Herrero.Quien#escala}). Martillean: suben
 * el martillo despacio, lo bajan de golpe sobre el yunque (el golpe cae en el tick {@link Herrero.Quien#golpe} de cada
 * {@link Herrero.Quien#ciclo}, cuando el cliente pone las chispas y el sonido) y descansan un momento; la izquierda sujeta la
 * pieza con las tenazas y la cabeza mira al yunque. Cada {@link Herrero#CADA_MIRA} martillazos, uno sin golpe: alzan la pieza
 * con la izquierda hasta la cara, la miran ladeando la cabeza y la vuelven a dejar. Si alguien se acerca, dejan el martillo
 * abajo y le miran ({@link Herrero#atencion}).
 */
public class HerreroRenderer extends LivingEntityRenderer<Herrero, HerreroRenderer.Modelo> {
    private static final ResourceLocation[] PIELES = new ResourceLocation[Herrero.Quien.values().length];

    static {
        for (Herrero.Quien q : Herrero.Quien.values())
            PIELES[q.ordinal()] = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/" + q.id + ".png");
    }

    public HerreroRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new Modelo(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        addLayer(new ItemInHandLayer<>(this, ctx.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(Herrero h) {
        return PIELES[h.quien().ordinal()];
    }

    @Override
    protected void scale(Herrero h, PoseStack ps, float pt) {
        float s = h.quien().escala;
        if (s != 1f) ps.scale(s, s, s);
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

    /** Lo que se alza la pieza para mirarla (0 a 1 a 0) a lo largo del martillazo sin golpe. */
    static float alza(float f) {
        float k = f < 0.25f ? f / 0.25f : f > 0.8f ? (1f - f) / 0.2f : 1f;
        k = Mth.clamp(k, 0f, 1f);
        return k * k * (3 - 2 * k);
    }

    public static class Modelo extends PlayerModel<Herrero> {
        public Modelo(ModelPart raiz) {
            super(raiz, false);
        }

        @Override
        public void setupAnim(Herrero h, float paso, float rapidez, float edad, float cabezaY, float cabezaX) {
            riding = false;
            super.setupAnim(h, 0f, 0f, edad, cabezaY, cabezaX);
            Herrero.Quien q = h.quien();
            float pt = Mth.clamp(edad - h.tickCount, 0f, 1f);
            float a = Mth.lerp(pt, h.atencionO, h.atencion);
            a = a * a * (3 - 2 * a);
            float c = h.ciclo + (a < 0.02f ? pt : 0f);
            float f = (c % q.ciclo) / q.ciclo;
            boolean mira = h.mirando((int) c);
            float respira = Mth.sin(edad * 0.07f) * 0.02f;
            // martilleando (a = 0) o mirando a quien llega (a = 1); un martillazo de cada tantos, mirando la pieza
            float alza = mira ? alza(f) : 0f;
            float brazo = mira ? Mth.lerp(alza, -0.25f, -0.55f) : martillo(f);
            rightArm.xRot = Mth.lerp(a, brazo, -0.12f + respira);
            rightArm.yRot = Mth.lerp(a, -0.10f, 0f);
            rightArm.zRot = Mth.lerp(a, 0.08f, 0.07f);
            leftArm.xRot = Mth.lerp(a, Mth.lerp(alza, -0.85f + respira, -1.55f), -0.05f - respira);
            leftArm.yRot = Mth.lerp(a, Mth.lerp(alza, 0.45f, 0.15f), 0f);
            leftArm.zRot = Mth.lerp(a, 0f, -0.07f);
            head.xRot = Mth.lerp(a, Mth.lerp(alza, 0.55f, 0.05f), head.xRot);
            head.yRot = Mth.lerp(a, Mth.lerp(alza, 0.08f, 0.28f), head.yRot);
            head.zRot = Mth.lerp(a, alza * 0.12f, 0f);
            hat.copyFrom(head);
            // Ulrik se echa mas sobre el yunque; Tobin, mas tieso
            float encorva = q == Herrero.Quien.ULRIK ? 0.18f : q == Herrero.Quien.TOBIN ? 0.07f : 0.12f;
            body.xRot = Mth.lerp(a, Mth.lerp(alza, encorva, 0.03f), 0f);
            rightSleeve.copyFrom(rightArm);
            leftSleeve.copyFrom(leftArm);
            rightPants.copyFrom(rightLeg);
            leftPants.copyFrom(leftLeg);
            jacket.copyFrom(body);
        }
    }
}
