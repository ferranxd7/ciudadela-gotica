package com.darkcraft.client.mision;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.mision.Campana;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
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
import net.minecraft.util.Mth;

/**
 * La campana del peregrino (parte 7ag) colgada de su soga: bronce con verdin (textura de 32 x 32 de {@code tools/mision/campana.py}),
 * el asa arriba, el hombro, el cuerpo, la boca y el badajo, que se mece algo mas que ella. Se mece siempre un poco y mucho cuando
 * suena ({@link Campana#vaiven}). A quien ya la cogio (o acabo la mision) no se le dibuja ({@link Campana#oculta}). El modelo lo usa
 * tambien Oswin para tocarla ({@code ViajeroRenderer}).
 */
public class CampanaRenderer extends EntityRenderer<Campana> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "campana"), "main");
    public static final ResourceLocation TEXTURA = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/campana.png");

    private final Modelo modelo;

    public CampanaRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        modelo = new Modelo(ctx.bakeLayer(CAPA));
        shadowRadius = 0f;
    }

    /** El modelo: el asa arriba en y 0 y la boca abajo en y 10 (pixeles; y hacia abajo). */
    public static LayerDefinition crear() {
        MeshDefinition m = new MeshDefinition();
        PartDefinition raiz = m.getRoot();
        PartDefinition c = raiz.addOrReplaceChild("campana", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1f, 0f, -1f, 2, 2, 2)
                .texOffs(8, 0).addBox(-1.5f, 2f, -1.5f, 3, 1, 3)
                .texOffs(0, 4).addBox(-2.5f, 3f, -2.5f, 5, 2, 5)
                .texOffs(0, 11).addBox(-3f, 5f, -3f, 6, 3, 6)
                .texOffs(0, 20).addBox(-3.5f, 8f, -3.5f, 7, 2, 7), PartPose.ZERO);
        c.addOrReplaceChild("badajo", CubeListBuilder.create().texOffs(20, 0).addBox(-0.5f, 5f, -0.5f, 1, 3, 1), PartPose.offset(0f, 3f, 0f));
        return LayerDefinition.create(m, 32, 32);
    }

    @Override
    public void render(Campana c, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (Campana.oculta) return;
        float v = c.vaiven(pt);
        ps.pushPose();
        // desde lo alto (donde la ata la soga), meciendose de lado y un poco hacia delante
        ps.translate(0, Campana.ALTO, 0);
        ps.mulPose(Axis.ZP.rotationDegrees(v));
        ps.mulPose(Axis.XP.rotationDegrees(v * 0.45f));
        ps.scale(-1f, -1f, 1f);
        // (algo mas grande que lo que ocupa: que se vea entre las ramas)
        float s = 1.25f * Campana.ALTO / (10f / 16f);
        ps.scale(s, s, s);
        modelo.badajo(c.tickCount + pt + v * 0.4f);
        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(TEXTURA));
        modelo.renderToBuffer(ps, vc, luz, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        ps.popPose();
        super.render(c, yaw, pt, ps, buf, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(Campana c) {
        return TEXTURA;
    }

    /** El modelo de la campana (lo usa tambien Oswin, en la mano). */
    public static class Modelo extends Model {
        private final ModelPart campana, badajo;

        public Modelo(ModelPart raiz) {
            super(RenderType::entityCutoutNoCull);
            campana = raiz.getChild("campana");
            badajo = campana.getChild("badajo");
        }

        /** El vaiven del badajo (algo mas que el de la campana). */
        public void badajo(float t) {
            badajo.zRot = Mth.sin(t * 0.21f) * 0.32f;
            badajo.xRot = Mth.sin(t * 0.17f + 1.1f) * 0.18f;
        }

        @Override
        public void renderToBuffer(PoseStack ps, VertexConsumer vc, int luz, int capa, float r, float g, float b, float a) {
            campana.render(ps, vc, luz, capa, r, g, b, a);
        }
    }
}
