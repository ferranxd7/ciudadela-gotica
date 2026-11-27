package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class GargolaGeometria {
    private GargolaGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create().texOffs(64, 0).addBox(-4.5F, -3.5F, -3F, 9F, 6F, 6F), PartPose.offset(0F, -13F, 1F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-5.5F, -12F, -3.5F, 11F, 12F, 7F).texOffs(52, 41).addBox(-4F, -8.5F, -4.2F, 8F, 7F, 1.2F).texOffs(92, 59).addBox(-0.8F, -12.6F, 3F, 1.6F, 1.6F, 1.8F).texOffs(92, 59).addBox(-0.8F, -9.9F, 3F, 1.6F, 1.6F, 2.4F).texOffs(92, 59).addBox(-0.8F, -7.2F, 3F, 1.6F, 1.6F, 1.8F).texOffs(92, 59).addBox(-0.8F, -4.5F, 3F, 1.6F, 1.6F, 2.4F), PartPose.offset(0F, -3F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(36, 0).addBox(-3.5F, -6F, -4F, 7F, 6F, 6.5F).texOffs(84, 41).addBox(-2.5F, -3.4F, -7.4F, 5F, 3.4F, 3.6F).texOffs(20, 59).addBox(-3.8F, -6.5F, -4.4F, 7.6F, 1.5F, 2.2F).texOffs(12, 59).addBox(-4.8F, -7.6F, -0.6F, 1.2F, 3.4F, 1.6F).texOffs(12, 59).addBox(3.6F, -7.6F, -0.6F, 1.2F, 3.4F, 1.6F), PartPose.offset(0F, -12F, -1F));
        PartDefinition p_mandibula = p_cabeza.addOrReplaceChild("mandibula", CubeListBuilder.create().texOffs(12, 51).addBox(-2.3F, 0F, -4.6F, 4.6F, 1.8F, 4.8F).texOffs(0, 65).addBox(-2.1F, -1.4F, -4.4F, 0.8F, 1.4F, 0.8F).texOffs(0, 65).addBox(1.3F, -1.4F, -4.4F, 0.8F, 1.4F, 0.8F), PartPose.offset(0F, -0.6F, -2.6F));
        PartDefinition p_cuerno_d = p_cabeza.addOrReplaceChild("cuerno_d", CubeListBuilder.create().texOffs(66, 51).addBox(-0.9F, -0.9F, 0F, 1.8F, 1.8F, 4.6F), PartPose.offsetAndRotation(-2.4F, -5.6F, -1.2F, 0.8378F, -0.384F, 0F));
        PartDefinition p_cuerno2_d = p_cuerno_d.addOrReplaceChild("cuerno2_d", CubeListBuilder.create().texOffs(0, 59).addBox(-0.6F, -0.6F, 0F, 1.2F, 1.2F, 3.8F), PartPose.offsetAndRotation(0F, 0F, 4.2F, 0.733F, -0.1396F, 0F));
        PartDefinition p_cuerno_i = p_cabeza.addOrReplaceChild("cuerno_i", CubeListBuilder.create().texOffs(66, 51).addBox(-0.9F, -0.9F, 0F, 1.8F, 1.8F, 4.6F), PartPose.offsetAndRotation(2.4F, -5.6F, -1.2F, 0.8378F, 0.384F, 0F));
        PartDefinition p_cuerno2_i = p_cuerno_i.addOrReplaceChild("cuerno2_i", CubeListBuilder.create().texOffs(0, 59).addBox(-0.6F, -0.6F, 0F, 1.2F, 1.2F, 3.8F), PartPose.offsetAndRotation(0F, 0F, 4.2F, 0.733F, 0.1396F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(94, 0).addBox(-1.8F, -1.2F, -1.8F, 3.6F, 8F, 3.6F), PartPose.offset(-6.6F, -10.6F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(40, 41).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 6.8F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create().texOffs(50, 51).addBox(-1.8F, 0F, -1.8F, 3.6F, 2.4F, 3.6F).texOffs(108, 59).addBox(-1.7F, 2F, -2F, 0.9F, 2.6F, 0.9F).texOffs(112, 59).addBox(-0.45F, 2F, -2.2F, 0.9F, 2.9F, 0.9F).texOffs(108, 59).addBox(0.8F, 2F, -2F, 0.9F, 2.6F, 0.9F), PartPose.offset(0F, 6.6F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(94, 0).addBox(-1.8F, -1.2F, -1.8F, 3.6F, 8F, 3.6F), PartPose.offset(6.6F, -10.6F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(40, 41).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 6.8F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create().texOffs(50, 51).addBox(-1.8F, 0F, -1.8F, 3.6F, 2.4F, 3.6F).texOffs(108, 59).addBox(-1.7F, 2F, -2F, 0.9F, 2.6F, 0.9F).texOffs(112, 59).addBox(-0.45F, 2F, -2.2F, 0.9F, 2.9F, 0.9F).texOffs(108, 59).addBox(0.8F, 2F, -2F, 0.9F, 2.6F, 0.9F), PartPose.offset(0F, 6.6F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(38, 30).addBox(-1.9F, -1F, -2F, 3.8F, 7F, 4F), PartPose.offset(-2.9F, 1.5F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(72, 41).addBox(-1.4F, 0F, -1.4F, 2.8F, 6F, 2.8F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_pata_d = p_pierna_d.addOrReplaceChild("pata_d", CubeListBuilder.create().texOffs(0, 51).addBox(-1.3F, 0F, -1.3F, 2.6F, 4.4F, 2.6F).texOffs(32, 51).addBox(-1.9F, 3.6F, -3.8F, 3.8F, 1.3F, 4.8F).texOffs(116, 59).addBox(-1.8F, 3.9F, -4.9F, 0.9F, 1F, 1.4F).texOffs(122, 59).addBox(-0.45F, 3.9F, -5.1F, 0.9F, 1F, 1.6F).texOffs(116, 59).addBox(0.9F, 3.9F, -4.9F, 0.9F, 1F, 1.4F), PartPose.offset(0F, 5.6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(38, 30).addBox(-1.9F, -1F, -2F, 3.8F, 7F, 4F), PartPose.offset(2.9F, 1.5F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(72, 41).addBox(-1.4F, 0F, -1.4F, 2.8F, 6F, 2.8F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_pata_i = p_pierna_i.addOrReplaceChild("pata_i", CubeListBuilder.create().texOffs(0, 51).addBox(-1.3F, 0F, -1.3F, 2.6F, 4.4F, 2.6F).texOffs(32, 51).addBox(-1.9F, 3.6F, -3.8F, 3.8F, 1.3F, 4.8F).texOffs(116, 59).addBox(-1.8F, 3.9F, -4.9F, 0.9F, 1F, 1.4F).texOffs(122, 59).addBox(-0.45F, 3.9F, -5.1F, 0.9F, 1F, 1.6F).texOffs(116, 59).addBox(0.9F, 3.9F, -4.9F, 0.9F, 1F, 1.4F), PartPose.offset(0F, 5.6F, 0F));
        PartDefinition p_cola1 = p_cadera.addOrReplaceChild("cola1", CubeListBuilder.create().texOffs(0, 41).addBox(-1.4F, -1.4F, -0.5F, 2.8F, 2.8F, 6.5F), PartPose.offsetAndRotation(0F, 1.2F, 2.6F, -0.3491F, 0F, 0F));
        PartDefinition p_cola2 = p_cola1.addOrReplaceChild("cola2", CubeListBuilder.create().texOffs(20, 41).addBox(-1.1F, -1.1F, -0.5F, 2.2F, 2.2F, 6.5F), PartPose.offsetAndRotation(0F, 0F, 5.6F, -0.1745F, 0F, 0F));
        PartDefinition p_cola3 = p_cola2.addOrReplaceChild("cola3", CubeListBuilder.create().texOffs(102, 41).addBox(-0.8F, -0.8F, -0.5F, 1.6F, 1.6F, 6F), PartPose.offsetAndRotation(0F, 0F, 5.6F, 0.1745F, 0F, 0F));
        PartDefinition p_punta = p_cola3.addOrReplaceChild("punta", CubeListBuilder.create().texOffs(106, 51).addBox(-2.4F, -0.4F, -0.4F, 4.8F, 0.8F, 4.6F), PartPose.offset(0F, 0F, 5.2F));
        PartDefinition p_ala_d = p_torso.addOrReplaceChild("ala_d", CubeListBuilder.create().texOffs(80, 51).addBox(-9F, -1.2F, -1.2F, 9.6F, 2.4F, 2.4F).texOffs(54, 30).addBox(-9F, -0.25F, 1F, 9F, 0.5F, 9F), PartPose.offset(-3.6F, -10.4F, 2.8F));
        PartDefinition p_ala2_d = p_ala_d.addOrReplaceChild("ala2_d", CubeListBuilder.create().texOffs(42, 59).addBox(-10F, -0.9F, -0.9F, 10.4F, 1.8F, 1.8F).texOffs(0, 19).addBox(-10F, -0.25F, 0.7F, 10F, 0.5F, 10F).texOffs(100, 59).addBox(-10.6F, -2.6F, -0.8F, 1.4F, 2F, 1.6F), PartPose.offset(-9F, 0F, 0F));
        PartDefinition p_ala3_d = p_ala2_d.addOrReplaceChild("ala3_d", CubeListBuilder.create().texOffs(68, 59).addBox(-9F, -0.6F, -0.6F, 9.4F, 1.2F, 1.2F).texOffs(80, 19).addBox(-9F, -0.25F, 0.4F, 9F, 0.5F, 10F), PartPose.offset(-10F, 0F, 0F));
        PartDefinition p_ala_i = p_torso.addOrReplaceChild("ala_i", CubeListBuilder.create().texOffs(80, 51).addBox(-0.6F, -1.2F, -1.2F, 9.6F, 2.4F, 2.4F).texOffs(90, 30).addBox(0F, -0.25F, 1F, 9F, 0.5F, 9F), PartPose.offset(3.6F, -10.4F, 2.8F));
        PartDefinition p_ala2_i = p_ala_i.addOrReplaceChild("ala2_i", CubeListBuilder.create().texOffs(42, 59).addBox(-0.4F, -0.9F, -0.9F, 10.4F, 1.8F, 1.8F).texOffs(40, 19).addBox(0F, -0.25F, 0.7F, 10F, 0.5F, 10F).texOffs(100, 59).addBox(9.2F, -2.6F, -0.8F, 1.4F, 2F, 1.6F), PartPose.offset(9F, 0F, 0F));
        PartDefinition p_ala3_i = p_ala2_i.addOrReplaceChild("ala3_i", CubeListBuilder.create().texOffs(68, 59).addBox(-0.4F, -0.6F, -0.6F, 9.4F, 1.2F, 1.2F).texOffs(0, 30).addBox(0F, -0.25F, 0.4F, 9F, 0.5F, 10F), PartPose.offset(10F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {};
}
