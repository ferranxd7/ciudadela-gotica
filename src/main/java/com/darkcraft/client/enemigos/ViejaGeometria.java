package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class ViejaGeometria {
    private ViejaGeometria() {}

    public static final int TEX_W = 256, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cuerpo = p_raiz.addOrReplaceChild("cuerpo", CubeListBuilder.create().texOffs(0, 0).addBox(-9F, -9F, -14F, 18F, 18F, 30F).texOffs(218, 48).addBox(-2.2F, -11.4F, 7F, 4.4F, 2.6F, 7F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_cabeza = p_cuerpo.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(96, 0).addBox(-8.6F, -9.6F, -26F, 17.2F, 17F, 26F), PartPose.offset(0F, 0F, -14F));
        PartDefinition p_mandibula = p_cabeza.addOrReplaceChild("mandibula", CubeListBuilder.create().texOffs(0, 48).addBox(-2.7F, 0F, -20.5F, 5.4F, 2.8F, 21F), PartPose.offset(0F, 6F, -5F));
        PartDefinition p_aleta_d = p_cuerpo.addOrReplaceChild("aleta_d", CubeListBuilder.create().texOffs(98, 48).addBox(-1.4F, 0F, -2.6F, 1.4F, 10F, 5.2F), PartPose.offsetAndRotation(-8.6F, 5.5F, -9F, 0.4887F, 0F, 0.5236F));
        PartDefinition p_aleta_i = p_cuerpo.addOrReplaceChild("aleta_i", CubeListBuilder.create().texOffs(114, 48).addBox(0F, 0F, -2.6F, 1.4F, 10F, 5.2F), PartPose.offsetAndRotation(8.6F, 5.5F, -9F, 0.4887F, 0F, -0.5236F));
        PartDefinition p_cola1 = p_cuerpo.addOrReplaceChild("cola1", CubeListBuilder.create().texOffs(184, 0).addBox(-7F, -7F, 0F, 14F, 14F, 14F), PartPose.offset(0F, -0.8F, 16F));
        PartDefinition p_cola2 = p_cola1.addOrReplaceChild("cola2", CubeListBuilder.create().texOffs(54, 48).addBox(-4.5F, -4.5F, 0F, 9F, 9F, 13F), PartPose.offset(0F, 0.4F, 14F));
        PartDefinition p_aleta = p_cola2.addOrReplaceChild("aleta", CubeListBuilder.create().texOffs(138, 48).addBox(-15F, -1F, -1F, 30F, 2F, 10F), PartPose.offset(0F, 0.4F, 13F));
        PartDefinition p_arpon_1 = p_cuerpo.addOrReplaceChild("arpon_1", CubeListBuilder.create().texOffs(134, 48).addBox(-0.5F, -12F, -0.5F, 1F, 12F, 1F), PartPose.offsetAndRotation(3F, -9F, -2F, -0.6632F, 0F, 0.4189F));
        PartDefinition p_arpon_2 = p_cuerpo.addOrReplaceChild("arpon_2", CubeListBuilder.create().texOffs(242, 48).addBox(-0.5F, -9F, -0.5F, 1F, 9F, 1F), PartPose.offsetAndRotation(-4F, -9F, 8F, -0.4189F, 0F, -0.5236F));
        PartDefinition p_arpon_3 = p_cola1.addOrReplaceChild("arpon_3", CubeListBuilder.create().texOffs(246, 48).addBox(-0.5F, -7F, -0.5F, 1F, 7F, 1F), PartPose.offsetAndRotation(1.5F, -7F, 5F, -0.8029F, 0F, 0.1745F));
        PartDefinition p_cabo = p_arpon_1.addOrReplaceChild("cabo", CubeListBuilder.create().texOffs(130, 48).addBox(-0.3F, 0F, -0.3F, 0.6F, 13F, 0.6F), PartPose.offsetAndRotation(0F, -11F, 0F, 1.0472F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {};
}
