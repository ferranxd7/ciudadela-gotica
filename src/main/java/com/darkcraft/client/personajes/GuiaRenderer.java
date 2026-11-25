package com.darkcraft.client.personajes;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.personajes.Guia;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Maelis: el modelo de jugador con brazos finos y su piel (capucha, manto y tunica en las capas de encima), sentada en el
 * suelo con las piernas hacia delante, las manos en el regazo y el farol de almas en la derecha. Se dibuja 0,75 mas
 * abajo (la cadera en el suelo).
 */
public class GuiaRenderer extends LivingEntityRenderer<Guia, GuiaRenderer.Modelo> {
    private static final ResourceLocation PIEL = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/guia.png");

    public GuiaRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new Modelo(ctx.bakeLayer(ModelLayers.PLAYER_SLIM)), 0.45f);
        addLayer(new ItemInHandLayer<>(this, ctx.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(Guia g) {
        return PIEL;
    }

    @Override
    public void render(Guia g, float giro, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        ps.pushPose();
        ps.translate(0, -0.75, 0);
        super.render(g, giro, pt, ps, buf, luz);
        ps.popPose();
    }

    @Override
    protected boolean shouldShowName(Guia g) {
        return false;
    }

    /** El modelo de jugador en la postura de sentada (la de ir montado, con los brazos en el regazo). */
    public static class Modelo extends PlayerModel<Guia> {
        public Modelo(ModelPart raiz) {
            super(raiz, true);
        }

        @Override
        public void setupAnim(Guia g, float paso, float rapidez, float edad, float cabezaY, float cabezaX) {
            riding = true;
            super.setupAnim(g, 0f, 0f, edad, cabezaY, cabezaX);
            // las manos al regazo: la derecha con el farol un poco por delante, la izquierda sobre la rodilla
            float respira = (float) Math.sin(edad * 0.06f) * 0.025f;
            rightArm.xRot = -0.95f + respira;
            rightArm.yRot = -0.12f;
            rightArm.zRot = 0.05f;
            leftArm.xRot = -0.78f + respira;
            leftArm.yRot = 0.32f;
            leftArm.zRot = -0.06f;
            rightLeg.yRot = 0.22f;
            leftLeg.yRot = -0.22f;
            body.xRot = 0.08f;
            rightSleeve.copyFrom(rightArm);
            leftSleeve.copyFrom(leftArm);
            rightPants.copyFrom(rightLeg);
            leftPants.copyFrom(leftLeg);
            jacket.copyFrom(body);
        }
    }
}
