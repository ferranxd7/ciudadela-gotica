package com.darkcraft.client.mision;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.mision.RestosTam;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Lo que queda de Tam (parte 7ah): un esqueleto sentado en el suelo de espaldas al torno, las piernas de hueso estiradas, los brazos
 * caidos, la cabeza ladeada con su capacete de hierro abollado, las costillas bajo la sobreveste quemada de la guardia, la bufanda roja
 * al cuello y la lanza atravesada en la rueda del torno, a su espalda (textura de 128 x 64 de {@code tools/mision/tam.py}). Lo que quien
 * juega ya cogio no se dibuja ({@link RestosTam#sinBufanda}, {@link RestosTam#sinLanza}).
 */
public class RestosRenderer extends EntityRenderer<RestosTam> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "restos_tam"), "main");
    public static final ResourceLocation TEXTURA = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/tam.png");

    private final ModelPart raiz, bufanda, lanza;

    public RestosRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        raiz = ctx.bakeLayer(CAPA);
        bufanda = raiz.getChild("body").getChild("bufanda");
        lanza = raiz.getChild("lanza");
        shadowRadius = 0.45f;
    }

    /** Las medidas, en pixeles del modelo (y hacia abajo; el suelo de la sala en y 12: se dibuja 0,75 mas abajo, la cadera en el suelo). */
    public static LayerDefinition crear() {
        MeshDefinition m = new MeshDefinition();
        PartDefinition r = m.getRoot();
        // el craneo con su capacete (la copa y el ala), caido hacia delante y a un lado
        r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4f, -8f, -4f, 8, 8, 8)
                        .texOffs(32, 0).addBox(-4.5f, -9.2f, -4.5f, 9, 4, 9)
                        .texOffs(72, 0).addBox(-6f, -5.6f, -6f, 12, 1, 12),
                PartPose.offsetAndRotation(0.6f, 0.4f, 0.4f, 0.55f, -0.25f, 0.35f));
        // las costillas y, encima, la sobreveste quemada; al cuello, la bufanda (con su cabo colgando delante)
        PartDefinition cuerpo = r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4f, 0f, -2f, 8, 12, 4)
                        .texOffs(16, 32).addBox(-4f, 0f, -2f, 8, 11, 4, new CubeDeformation(0.3f)),
                PartPose.rotation(-0.1f, 0f, 0.04f));
        cuerpo.addOrReplaceChild("bufanda", CubeListBuilder.create().texOffs(48, 16).addBox(-4.5f, -1f, -2.6f, 9, 2, 5, new CubeDeformation(0.2f))
                .texOffs(48, 24).addBox(0.8f, 1f, -3.2f, 2, 7, 1), PartPose.ZERO);
        // los brazos caidos: el derecho en el suelo, el izquierdo sobre el regazo
        r.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1f, -1f, -1f, 2, 12, 2),
                PartPose.offsetAndRotation(-5f, 2f, 0f, -0.25f, 0.1f, 0.42f));
        r.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1f, -1f, -1f, 2, 12, 2),
                PartPose.offsetAndRotation(5f, 2f, 0f, -0.95f, -0.35f, -0.12f));
        // las piernas estiradas por el suelo
        r.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1f, 0f, -1f, 2, 12, 2),
                PartPose.offsetAndRotation(-2f, 12f, 0f, -1.5f, 0.22f, 0f));
        r.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1f, 0f, -1f, 2, 12, 2),
                PartPose.offsetAndRotation(2f, 12f, 0f, -1.42f, -0.3f, 0f));
        // la lanza, atravesada en la rueda del torno (un bloque detras de el, a media altura de la rueda), de lado a lado
        r.addOrReplaceChild("lanza", CubeListBuilder.create().texOffs(0, 48).addBox(-14f, -0.5f, -0.5f, 28, 1, 1)
                .texOffs(60, 48).addBox(14f, -1f, -1f, 4, 2, 2), PartPose.offsetAndRotation(0f, 4f, 15.5f, 0.12f, 0.2f, -0.3f));
        return LayerDefinition.create(m, 128, 64);
    }

    @Override
    public void render(RestosTam e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        ps.pushPose();
        // como un ser vivo (la cara del modelo hacia donde mira), sentado en el suelo: 0,75 mas abajo
        ps.translate(0, -0.75, 0);
        ps.mulPose(Axis.YP.rotationDegrees(180f - e.getYRot()));
        ps.scale(-1f, -1f, 1f);
        ps.translate(0, -1.501f, 0);
        bufanda.visible = !RestosTam.sinBufanda;
        lanza.visible = !RestosTam.sinLanza;
        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(TEXTURA));
        raiz.render(ps, vc, luz, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        ps.popPose();
        super.render(e, yaw, pt, ps, buf, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(RestosTam e) {
        return TEXTURA;
    }
}
