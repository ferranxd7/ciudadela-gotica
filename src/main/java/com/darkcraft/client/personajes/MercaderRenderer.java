package com.darkcraft.client.personajes;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.personajes.Mercader;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Kerrick: el modelo de jugador de brazos finos con piezas de mas (textura de 128 x 64; mismas medidas que
 * {@code tools/personajes/mercader.py}): en la cabeza, el sombrero de ala ancha (copa y ala); en el cuerpo, la mochila
 * enorme, la manta enrollada encima, una olla colgando a la izquierda y un farol a la derecha. De pie, con los pulgares
 * en las correas, respirando y meciendose un poco. Silas (parte 7af), con su piel, lanza su moneda al aire de vez en cuando.
 */
public class MercaderRenderer extends LivingEntityRenderer<Mercader, MercaderRenderer.Modelo> {
    private static final ResourceLocation PIEL = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/mercader.png");
    /** Silas (parte 7af): las mismas piezas con su piel ({@code tools/personajes/silas.py}). */
    private static final ResourceLocation PIEL_SILAS = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/silas.png");
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "mercader"), "main");

    public MercaderRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new Modelo(ctx.bakeLayer(CAPA)), 0.55f);
        // Silas lleva una moneda en la derecha
        addLayer(new net.minecraft.client.renderer.entity.layers.ItemInHandLayer<>(this, ctx.getItemInHandRenderer()));
    }

    public static LayerDefinition crear() {
        MeshDefinition m = PlayerModel.createMesh(CubeDeformation.NONE, true);
        PartDefinition raiz = m.getRoot();
        PartDefinition cuerpo = raiz.getChild("body");
        cuerpo.addOrReplaceChild("mochila", CubeListBuilder.create().texOffs(64, 0).addBox(-4.5f, 0.0f, 2.0f, 9, 11, 5), PartPose.ZERO);
        cuerpo.addOrReplaceChild("manta", CubeListBuilder.create().texOffs(64, 16).addBox(-5.5f, -3.0f, 4.6f, 11, 3, 3), PartPose.ZERO);
        cuerpo.addOrReplaceChild("olla", CubeListBuilder.create().texOffs(96, 0).addBox(4.5f, 6.0f, 3.0f, 3, 3, 3), PartPose.ZERO);
        cuerpo.addOrReplaceChild("farol", CubeListBuilder.create().texOffs(96, 8).addBox(-6.5f, 5.0f, 3.5f, 2, 3, 2), PartPose.ZERO);
        PartDefinition cabeza = raiz.getChild("head");
        cabeza.addOrReplaceChild("copa", CubeListBuilder.create().texOffs(64, 24).addBox(-4.5f, -10.5f, -4.5f, 9, 3, 9), PartPose.ZERO);
        cabeza.addOrReplaceChild("ala", CubeListBuilder.create().texOffs(64, 40).addBox(-7f, -7.75f, -7f, 14, 1, 14), PartPose.ZERO);
        return LayerDefinition.create(m, 128, 64);
    }

    @Override
    public ResourceLocation getTextureLocation(Mercader m) {
        return m.quien() == Mercader.Quien.SILAS ? PIEL_SILAS : PIEL;
    }

    @Override
    protected boolean shouldShowName(Mercader m) {
        return false;
    }

    public static class Modelo extends PlayerModel<Mercader> {
        public Modelo(ModelPart raiz) {
            super(raiz, true);
        }

        @Override
        public void setupAnim(Mercader m, float paso, float rapidez, float edad, float cabezaY, float cabezaX) {
            riding = false;
            super.setupAnim(m, 0f, 0f, edad, cabezaY, cabezaX);
            float respira = Mth.sin(edad * 0.065f) * 0.03f;
            // los pulgares en las correas de la mochila (Silas, la derecha delante del pecho, lanzando su moneda al aire de vez
            // en cuando: sube y baja la mano y la recoge)
            if (m.quien() == Mercader.Quien.SILAS) {
                float c = (edad % 70f) / 70f;
                float salto = c < 0.16f ? Mth.sin(c / 0.16f * Mth.PI) : 0f;
                rightArm.xRot = -0.95f - 0.35f * salto + respira;
                rightArm.yRot = -0.32f;
                rightArm.zRot = 0.05f;
                head.xRot = Mth.lerp(salto, head.xRot, -0.25f);
                hat.copyFrom(head);
            } else {
                rightArm.xRot = -0.30f + respira;
                rightArm.yRot = 0.05f;
                rightArm.zRot = -0.12f;
            }
            leftArm.xRot = -0.30f + respira;
            leftArm.yRot = -0.05f;
            leftArm.zRot = 0.12f;
            // se mece un poco, como quien espera
            body.yRot = Mth.sin(edad * 0.025f) * 0.05f;
            rightSleeve.copyFrom(rightArm);
            leftSleeve.copyFrom(leftArm);
            rightPants.copyFrom(rightLeg);
            leftPants.copyFrom(leftLeg);
            jacket.copyFrom(body);
        }
    }
}
