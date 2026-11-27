package com.darkcraft.client.mision;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.mision.FarolBrann;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * El farol de Brann (parte 7an): un farol de aceite grande, de ballenero (base y tapa de laton verdoso, el cristal ahumado con sus
 * barrotes, el tejadillo y el asa), del tamano que le va a un gigante. Tumbado de lado en la arena del Costillar y apagado (la variante 0);
 * de pie en el pretil del muelle y encendido (la 1): la llama ambar va en otra capa con luz propia. Lo que no le toca ver a quien juega,
 * no se dibuja ({@link FarolBrann#visto}). Texturas de {@code tools/mision/farol.py}.
 */
public class FarolBrannRenderer extends EntityRenderer<FarolBrann> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "farol_brann"), "main");
    public static final ResourceLocation TEXTURA = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/farol_brann.png"),
            BRILLO = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/farol_brann_brillo.png");
    /** Lo grande que es (el modelo esta en pixeles de bloque). */
    static final float ESCALA = 1.4f;

    private final ModelPart raiz;

    public FarolBrannRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        raiz = ctx.bakeLayer(CAPA);
        shadowRadius = 0.25f;
    }

    /** Las medidas, en pixeles (y hacia abajo; la base apoya en y 0). Las mismas cajas que {@code farol.py}. */
    public static LayerDefinition crear() {
        MeshDefinition m = new MeshDefinition();
        PartDefinition r = m.getRoot();
        r.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-3f, -1f, -3f, 6, 1, 6), PartPose.ZERO);
        r.addOrReplaceChild("cristal", CubeListBuilder.create().texOffs(0, 8).addBox(-2.5f, -6f, -2.5f, 5, 5, 5), PartPose.ZERO);
        r.addOrReplaceChild("tapa", CubeListBuilder.create().texOffs(24, 0).addBox(-3f, -7f, -3f, 6, 1, 6), PartPose.ZERO);
        r.addOrReplaceChild("tejadillo", CubeListBuilder.create().texOffs(24, 8).addBox(-2f, -8f, -2f, 4, 1, 4), PartPose.ZERO);
        r.addOrReplaceChild("asa", CubeListBuilder.create().texOffs(48, 0).addBox(-0.5f, -10f, -2.5f, 1, 2, 1)
                .texOffs(48, 0).addBox(-0.5f, -10f, 1.5f, 1, 2, 1)
                .texOffs(48, 4).addBox(-0.5f, -11f, -2.5f, 1, 1, 5), PartPose.ZERO);
        return LayerDefinition.create(m, 64, 32);
    }

    @Override
    public void render(FarolBrann e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (e.visto()) {
            ps.pushPose();
            ps.mulPose(Axis.YP.rotationDegrees(-e.getYRot()));
            boolean tumbado = e.variante() == 0;
            if (tumbado) {
                // de lado en la arena: apoyado en el canto de la base y la tapa
                ps.translate(0, 3f / 16f * ESCALA, 0);
                ps.mulPose(Axis.ZP.rotationDegrees(90f));
            }
            ps.scale(ESCALA, ESCALA, ESCALA);
            ps.scale(-1f, -1f, 1f);
            raiz.render(ps, buf.getBuffer(RenderType.entityCutoutNoCull(TEXTURA)), luz, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
            if (!tumbado) {
                // encendido: la llama tiembla
                float t = e.tickCount + pt, k = 0.8f + 0.2f * Mth.sin(t * 0.37f) * Mth.sin(t * 0.17f + 0.8f);
                raiz.render(ps, buf.getBuffer(RenderType.eyes(BRILLO)), 0xF000F0, OverlayTexture.NO_OVERLAY, k, k, k, 1f);
            }
            ps.popPose();
        }
        super.render(e, yaw, pt, ps, buf, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(FarolBrann e) {
        return TEXTURA;
    }
}
