package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class HuecoGeometria {
    private HuecoGeometria() {}

    public static final int TEX_W = 128, TEX_H = 64;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(88, 35).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(104, 35).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(0, 47).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(16, 47).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(32, 19).addBox(-4F, -12F, -2F, 8F, 12F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(90, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(32, 47).addBox(-1.5F, -2F, -2F, 3F, 6F, 4F), PartPose.offset(-5.5F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(46, 47).addBox(-1.5F, 0F, -2F, 3F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(60, 47).addBox(-1.5F, -2F, -2F, 3F, 6F, 4F), PartPose.offset(5.5F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(74, 47).addBox(-1.5F, 0F, -2F, 3F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_faldon = p_cadera.addOrReplaceChild("faldon", CubeListBuilder.create().texOffs(88, 47).addBox(-4F, -0.5F, -2F, 8F, 5F, 4F, new CubeDeformation(0.4F, 0.4F, 0.4F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_capucha = p_cabeza.addOrReplaceChild("capucha", CubeListBuilder.create().texOffs(0, 19).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.55F, 0.55F, 0.55F)).texOffs(28, 57).addBox(-2F, -6.5F, 4F, 4F, 5F, 2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_sombrero = p_cabeza.addOrReplaceChild("sombrero", CubeListBuilder.create().texOffs(56, 19).addBox(-7F, -1F, -7F, 14F, 1F, 14F).texOffs(56, 35).addBox(-4F, -4F, -4F, 8F, 3F, 8F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offsetAndRotation(0F, -7.6F, 0F, -0.0873F, 0F, 0.0698F));
        PartDefinition p_venda = p_cabeza.addOrReplaceChild("venda", CubeListBuilder.create().texOffs(0, 35).addBox(-4F, -7F, -4F, 8F, 4F, 8F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_horca = p_mano_d.addOrReplaceChild("horca", CubeListBuilder.create().texOffs(20, 0).addBox(-0.5F, -0.5F, -21F, 1F, 1F, 17F).texOffs(56, 0).addBox(-0.5F, -0.5F, -4F, 1F, 1F, 16F).texOffs(68, 57).addBox(-2.5F, -0.5F, -22F, 5F, 1F, 1F).texOffs(40, 57).addBox(-2.5F, -0.5F, -27F, 1F, 1F, 5F).texOffs(40, 57).addBox(1.5F, -0.5F, -27F, 1F, 1F, 5F).texOffs(0, 57).addBox(-0.5F, -0.5F, -28F, 1F, 1F, 6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hoz = p_mano_d.addOrReplaceChild("hoz", CubeListBuilder.create().texOffs(112, 47).addBox(-0.5F, -0.5F, -4F, 1F, 1F, 7F).texOffs(0, 0).addBox(0F, -8F, -13F, 0F, 9F, 10F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_antorcha = p_mano_d.addOrReplaceChild("antorcha", CubeListBuilder.create().texOffs(32, 35).addBox(-0.5F, -0.5F, -8F, 1F, 1F, 11F).texOffs(52, 57).addBox(-1F, -1F, -11F, 2F, 2F, 3F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_llama = p_antorcha.addOrReplaceChild("llama", CubeListBuilder.create().texOffs(14, 57).addBox(-1.5F, -1.5F, -4F, 3F, 3F, 4F).texOffs(62, 57).addBox(-0.5F, -0.5F, -6F, 1F, 1F, 2F), PartPose.offset(0F, 0F, -11F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"sombrero", "venda", "hoz", "antorcha"};

    public static final String[] OCULTAS_1 = {"capucha", "venda", "horca", "antorcha"};

    public static final String[] OCULTAS_2 = {"capucha", "sombrero", "horca", "hoz"};
}
