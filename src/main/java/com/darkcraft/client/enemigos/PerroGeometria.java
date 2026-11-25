package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class PerroGeometria {
    private PerroGeometria() {}

    public static final int TEX_W = 64, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create().texOffs(0, 53).addBox(-3F, -2F, -1.5F, 6F, 4F, 6F), PartPose.offset(0F, -13F, 6F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -1.5F, -13F, 7F, 8F, 12F).texOffs(0, 20).addBox(-0.75F, -2.6F, -14F, 1.5F, 1.6F, 16F).texOffs(0, 38).addBox(-3.8F, -2F, -12.5F, 7.6F, 5F, 10F, new CubeDeformation(0.15F, 0.15F, 0.15F)), PartPose.offset(0F, -0.5F, -1F));
        PartDefinition p_cuello = p_torso.addOrReplaceChild("cuello", CubeListBuilder.create().texOffs(24, 53).addBox(-1.5F, -1.5F, -6F, 3F, 3F, 6.5F).texOffs(34, 72).addBox(-2.3F, -2.3F, -2.2F, 4.6F, 4.6F, 1.6F).texOffs(48, 87).addBox(-0.4F, -3.7F, -1.8F, 0.8F, 1.5F, 0.8F).texOffs(52, 87).addBox(-3.7F, -0.4F, -1.8F, 1.5F, 0.8F, 0.8F).texOffs(58, 87).addBox(2.2F, -0.4F, -1.8F, 1.5F, 0.8F, 0.8F).texOffs(44, 87).addBox(-0.5F, 2.2F, -1.6F, 1F, 2.6F, 1F), PartPose.offsetAndRotation(0F, 1F, -12F, -0.6632F, 0F, 0F));
        PartDefinition p_cabeza = p_cuello.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(36, 38).addBox(-2.5F, -2.8F, -5F, 5F, 5F, 5.5F).texOffs(44, 53).addBox(-1.6F, -1.2F, -10.5F, 3.2F, 2.6F, 5.6F).texOffs(0, 92).addBox(-1.1F, 1.3F, -10.2F, 0.6F, 1F, 0.6F).texOffs(4, 92).addBox(0.5F, 1.3F, -10.2F, 0.6F, 1F, 0.6F), PartPose.offsetAndRotation(0F, 0F, -5.5F, 0.6632F, 0F, 0F));
        PartDefinition p_mandibula = p_cabeza.addOrReplaceChild("mandibula", CubeListBuilder.create().texOffs(0, 72).addBox(-1.4F, 0F, -5.6F, 2.8F, 1.2F, 6F).texOffs(8, 92).addBox(-1F, -0.8F, -5.4F, 0.5F, 0.8F, 0.5F).texOffs(12, 92).addBox(0.5F, -0.8F, -5.4F, 0.5F, 0.8F, 0.5F), PartPose.offsetAndRotation(0F, 1.5F, -4.5F, 0.1047F, 0F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(0, 63).addBox(-1.1F, -1.5F, -1.2F, 2.2F, 6F, 2.4F), PartPose.offset(-2.6F, 3.5F, -10.5F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(48, 72).addBox(-0.8F, 0F, -0.8F, 1.6F, 4.5F, 1.6F), PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create().texOffs(28, 80).addBox(-1.2F, 0F, -2.4F, 2.4F, 1F, 3.2F), PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(12, 63).addBox(-1.1F, -1.5F, -1.2F, 2.2F, 6F, 2.4F), PartPose.offset(2.6F, 3.5F, -10.5F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(56, 72).addBox(-0.8F, 0F, -0.8F, 1.6F, 4.5F, 1.6F), PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create().texOffs(42, 80).addBox(-1.2F, 0F, -2.4F, 2.4F, 1F, 3.2F), PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(24, 63).addBox(-1.3F, -1.5F, -1.4F, 2.6F, 6F, 2.8F), PartPose.offsetAndRotation(-2.5F, 1.5F, 3F, -0.4363F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(0, 80).addBox(-0.9F, 0F, -0.9F, 1.8F, 4.5F, 1.8F), PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.8727F, 0F, 0F));
        PartDefinition p_pata_d = p_pierna_d.addOrReplaceChild("pata_d", CubeListBuilder.create().texOffs(28, 87).addBox(-0.8F, 0F, -0.8F, 1.6F, 2.6F, 1.6F).texOffs(0, 87).addBox(-1.2F, 2.6F, -2.6F, 2.4F, 1F, 3.2F), PartPose.offsetAndRotation(0F, 4.5F, 0F, -0.4363F, 0F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(36, 63).addBox(-1.3F, -1.5F, -1.4F, 2.6F, 6F, 2.8F), PartPose.offsetAndRotation(2.5F, 1.5F, 3F, -0.4363F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(8, 80).addBox(-0.9F, 0F, -0.9F, 1.8F, 4.5F, 1.8F), PartPose.offsetAndRotation(0F, 4.5F, 0F, 0.8727F, 0F, 0F));
        PartDefinition p_pata_i = p_pierna_i.addOrReplaceChild("pata_i", CubeListBuilder.create().texOffs(36, 87).addBox(-0.8F, 0F, -0.8F, 1.6F, 2.6F, 1.6F).texOffs(14, 87).addBox(-1.2F, 2.6F, -2.6F, 2.4F, 1F, 3.2F), PartPose.offsetAndRotation(0F, 4.5F, 0F, -0.4363F, 0F, 0F));
        PartDefinition p_cola = p_cadera.addOrReplaceChild("cola", CubeListBuilder.create().texOffs(18, 72).addBox(-0.55F, -0.55F, 0F, 1.1F, 1.1F, 6F), PartPose.offsetAndRotation(0F, -1F, 4.5F, -0.3142F, 0F, 0F));
        PartDefinition p_cola2 = p_cola.addOrReplaceChild("cola2", CubeListBuilder.create().texOffs(16, 80).addBox(-0.45F, -0.45F, 0F, 0.9F, 0.9F, 5F), PartPose.offsetAndRotation(0F, 0F, 6F, -0.2793F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {};
}
