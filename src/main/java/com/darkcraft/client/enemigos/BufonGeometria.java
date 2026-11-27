package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class BufonGeometria {
    private BufonGeometria() {}

    public static final int TEX_W = 128, TEX_H = 64;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -13F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(26, 16).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(-1.5F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(38, 16).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(50, 16).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(1.5F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(62, 16).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(32, 0).addBox(-3.5F, -11F, -2F, 7F, 11F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -11F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(74, 16).addBox(-1.5F, -2F, -1.5F, 3F, 6F, 3F), PartPose.offset(-5F, -9F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(86, 16).addBox(-1.5F, 0F, -1.5F, 3F, 6F, 3F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(98, 16).addBox(-1.5F, -2F, -1.5F, 3F, 6F, 3F), PartPose.offset(5F, -9F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(110, 16).addBox(-1.5F, 0F, -1.5F, 3F, 6F, 3F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_mascara = p_cabeza.addOrReplaceChild("mascara", CubeListBuilder.create().texOffs(84, 26).addBox(-4F, -7.5F, -4.6F, 8F, 6F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gorro = p_cabeza.addOrReplaceChild("gorro", CubeListBuilder.create().texOffs(54, 0).addBox(-4.3F, -1F, -4.3F, 8.6F, 1.5F, 8.6F, new CubeDeformation(0.1F, 0.1F, 0.1F)).texOffs(90, 0).addBox(-3.5F, -4F, -3.5F, 7F, 3F, 7F), PartPose.offset(0F, -8F, 0F));
        PartDefinition p_punta_d1 = p_gorro.addOrReplaceChild("punta_d1", CubeListBuilder.create().texOffs(0, 26).addBox(-1.5F, -6F, -1.5F, 3F, 6F, 3F), PartPose.offsetAndRotation(-2F, -3.5F, 0F, 0F, 0F, 0.8727F));
        PartDefinition p_punta_d2 = p_punta_d1.addOrReplaceChild("punta_d2", CubeListBuilder.create().texOffs(68, 26).addBox(-1F, -6F, -1F, 2F, 6F, 2F).texOffs(102, 26).addBox(-1.2F, -8.2F, -1.2F, 2.4F, 2.4F, 2.4F), PartPose.offsetAndRotation(0F, -6F, 0F, 0F, 0F, 1.1519F));
        PartDefinition p_punta_i1 = p_gorro.addOrReplaceChild("punta_i1", CubeListBuilder.create().texOffs(0, 26).addBox(-1.5F, -6F, -1.5F, 3F, 6F, 3F), PartPose.offsetAndRotation(2F, -3.5F, 0F, 0F, 0F, -0.8727F));
        PartDefinition p_punta_i2 = p_punta_i1.addOrReplaceChild("punta_i2", CubeListBuilder.create().texOffs(68, 26).addBox(-1F, -6F, -1F, 2F, 6F, 2F).texOffs(102, 26).addBox(-1.2F, -8.2F, -1.2F, 2.4F, 2.4F, 2.4F), PartPose.offsetAndRotation(0F, -6F, 0F, 0F, 0F, -1.1519F));
        PartDefinition p_punta_c1 = p_gorro.addOrReplaceChild("punta_c1", CubeListBuilder.create().texOffs(12, 26).addBox(-1.5F, -6F, -1.5F, 3F, 6F, 3F), PartPose.offsetAndRotation(0F, -3.5F, 1F, -0.6981F, 0F, 0F));
        PartDefinition p_punta_c2 = p_punta_c1.addOrReplaceChild("punta_c2", CubeListBuilder.create().texOffs(76, 26).addBox(-1F, -6F, -1F, 2F, 6F, 2F).texOffs(102, 26).addBox(-1.2F, -8.2F, -1.2F, 2.4F, 2.4F, 2.4F), PartPose.offsetAndRotation(0F, -6F, 0F, -1.2566F, 0F, 0F));
        PartDefinition p_gorguera = p_torso.addOrReplaceChild("gorguera", CubeListBuilder.create().texOffs(24, 26).addBox(-5F, -1.2F, -3F, 10F, 2F, 6F, new CubeDeformation(0.2F, 0.2F, 0.2F)), PartPose.offset(0F, -11F, 0F));
        PartDefinition p_faldon = p_cadera.addOrReplaceChild("faldon", CubeListBuilder.create().texOffs(0, 16).addBox(-4F, -1.5F, -2.5F, 8F, 4.5F, 5F, new CubeDeformation(0.25F, 0.25F, 0.25F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_zapato_d = p_pierna_d.addOrReplaceChild("zapato_d", CubeListBuilder.create().texOffs(114, 26).addBox(-1F, -1F, -3.5F, 2F, 1.5F, 3.5F).texOffs(22, 35).addBox(-0.8F, -2.4F, -4.3F, 1.6F, 1.6F, 1.6F), PartPose.offsetAndRotation(0F, 6F, -1.5F, -0.4887F, 0F, 0F));
        PartDefinition p_zapato_i = p_pierna_i.addOrReplaceChild("zapato_i", CubeListBuilder.create().texOffs(114, 26).addBox(-1F, -1F, -3.5F, 2F, 1.5F, 3.5F).texOffs(22, 35).addBox(-0.8F, -2.4F, -4.3F, 1.6F, 1.6F, 1.6F), PartPose.offsetAndRotation(0F, 6F, -1.5F, -0.4887F, 0F, 0F));
        PartDefinition p_daga_d = p_mano_d.addOrReplaceChild("daga_d", CubeListBuilder.create().texOffs(12, 35).addBox(-0.5F, -0.5F, -1.5F, 1F, 1F, 3.5F).texOffs(30, 35).addBox(-1.5F, -0.5F, -2.5F, 3F, 1F, 1F).texOffs(56, 26).addBox(0F, -1F, -8.5F, 0F, 2F, 6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_daga_i = p_mano_i.addOrReplaceChild("daga_i", CubeListBuilder.create().texOffs(12, 35).addBox(-0.5F, -0.5F, -1.5F, 1F, 1F, 3.5F).texOffs(30, 35).addBox(-1.5F, -0.5F, -2.5F, 3F, 1F, 1F).texOffs(56, 26).addBox(0F, -1F, -8.5F, 0F, 2F, 6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_malabar = p_torso.addOrReplaceChild("malabar", CubeListBuilder.create().texOffs(0, 35).addBox(-1.25F, -7.25F, -1.25F, 2.5F, 2.5F, 2.5F).texOffs(0, 35).addBox(4.95F, 2.75F, -1.25F, 2.5F, 2.5F, 2.5F).texOffs(0, 35).addBox(-7.45F, 2.75F, -1.25F, 2.5F, 2.5F, 2.5F), PartPose.offset(0F, -6.5F, -7F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"malabar"};
}
