package com.darkcraft.client.mision;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.mision.Baliza;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Una baliza de la niebla (parte 7ao): el poste de roble negro en su base de piedras, el brazo de arriba que sale hacia la laguna, la
 * cadena corta y el farol grande de hierro (tejadillo, jaula de cuatro cristales, suelo y la mecha dentro). Apagada, los cristales
 * negros de hollin; encendida para quien juega ({@link Baliza#encendida}), la llama y los cristales calientes van en otra capa con luz
 * propia, y el farol se mece un poco con el viento. Texturas de {@code tools/mision/baliza.py}.
 */
public class BalizaRenderer extends EntityRenderer<Baliza> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "baliza"), "main");
    public static final ResourceLocation TEXTURA = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/baliza.png"),
            BRILLO = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/baliza_brillo.png");

    private final ModelPart raiz, farol;

    public BalizaRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        raiz = ctx.bakeLayer(CAPA);
        farol = raiz.getChild("farol");
        shadowRadius = 0.3f;
    }

    /**
     * Las medidas, en pixeles (y hacia abajo; la base apoya en y 0; el brazo y el farol hacia -z, que es hacia donde mira la baliza). Las
     * mismas cajas que {@code baliza.py}. El farol cuelga de la punta del brazo (su pivote, para mecerse).
     */
    public static LayerDefinition crear() {
        MeshDefinition m = new MeshDefinition();
        PartDefinition r = m.getRoot();
        r.addOrReplaceChild("base", CubeListBuilder.create().texOffs(12, 39).addBox(-3f, -2f, -3f, 6, 2, 6), PartPose.ZERO);
        r.addOrReplaceChild("poste", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5f, -42f, -1.5f, 3, 40, 3), PartPose.ZERO);
        r.addOrReplaceChild("brazo", CubeListBuilder.create().texOffs(12, 0).addBox(-1f, -41f, -10f, 2, 2, 8), PartPose.ZERO);
        PartDefinition f = r.addOrReplaceChild("farol", CubeListBuilder.create()
                .texOffs(40, 10).addBox(-0.5f, 0f, -0.5f, 1, 2, 1)
                .texOffs(12, 10).addBox(-3.5f, 2f, -3.5f, 7, 1, 7)
                .texOffs(12, 26).addBox(-3f, 3f, -3f, 6, 7, 6)
                .texOffs(12, 18).addBox(-3.5f, 10f, -3.5f, 7, 1, 7), PartPose.offset(0f, -39f, -9f));
        f.addOrReplaceChild("llama", CubeListBuilder.create().texOffs(36, 26).addBox(-1f, 5f, -1f, 2, 3, 2), PartPose.ZERO);
        return LayerDefinition.create(m, 64, 64);
    }

    @Override
    public void render(Baliza e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(180f - e.getYRot()));
        ps.scale(-1f, -1f, 1f);
        boolean luce = e.encendida();
        float t = e.tickCount + pt + e.getId() * 13;
        // el farol se mece un poco con el viento de la laguna
        farol.xRot = Mth.sin(t * 0.045f) * 0.06f;
        farol.zRot = Mth.sin(t * 0.031f + 1.1f) * 0.05f;
        // (encendida, la baliza entera se ve con la luz de su farol: getBlockLightLevel)
        raiz.render(ps, buf.getBuffer(RenderType.entityCutoutNoCull(TEXTURA)), luz, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        if (luce) {
            float k = 0.82f + 0.18f * Mth.sin(t * 0.37f) * Mth.sin(t * 0.19f + 0.6f);
            raiz.render(ps, buf.getBuffer(RenderType.eyes(BRILLO)), 0xF000F0, OverlayTexture.NO_OVERLAY, k, k, k, 1f);
        }
        ps.popPose();
        super.render(e, yaw, pt, ps, buf, luz);
    }

    @Override
    protected int getBlockLightLevel(Baliza e, BlockPos p) {
        return e.encendida() ? Math.max(12, super.getBlockLightLevel(e, p)) : super.getBlockLightLevel(e, p);
    }

    @Override
    public ResourceLocation getTextureLocation(Baliza e) {
        return TEXTURA;
    }
}
