package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class MarchitoGeometria {
    private MarchitoGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -13F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(40, 39).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(-1.5F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(52, 39).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(64, 39).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(1.5F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(76, 39).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(96, 0).addBox(-3.5F, -13F, -2F, 7F, 13F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(60, 24).addBox(-3.5F, -8F, -3.5F, 7F, 8F, 7F), PartPose.offset(0F, -13F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(88, 39).addBox(-1.5F, -2F, -1.5F, 3F, 7F, 3F), PartPose.offset(-5F, -11F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(100, 39).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(112, 39).addBox(-1.5F, -2F, -1.5F, 3F, 7F, 3F), PartPose.offset(5F, -11F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(0, 53).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_harapo = p_cadera.addOrReplaceChild("harapo", CubeListBuilder.create().texOffs(0, 39).addBox(-4F, -0.5F, -2.5F, 8F, 9F, 5F, new CubeDeformation(0.35F, 0.35F, 0.35F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_capucha = p_cabeza.addOrReplaceChild("capucha", CubeListBuilder.create().texOffs(88, 24).addBox(-3.5F, -8F, -3.5F, 7F, 8F, 7F, new CubeDeformation(0.6F, 0.6F, 0.6F)).texOffs(40, 53).addBox(-2F, -6F, 3.5F, 4F, 6F, 2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cuello = p_torso.addOrReplaceChild("cuello", CubeListBuilder.create().texOffs(12, 53).addBox(-4.5F, 0F, -2.5F, 9F, 3F, 5F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(0F, -13F, 0F));
        PartDefinition p_guadana = p_mano_d.addOrReplaceChild("guadana", CubeListBuilder.create().texOffs(28, 0).addBox(-0.5F, -0.5F, -24F, 1F, 1F, 16F).texOffs(0, 24).addBox(-0.5F, -0.5F, -8F, 1F, 1F, 14F).texOffs(32, 63).addBox(-1.5F, -0.5F, -12F, 2F, 1F, 1F).texOffs(0, 0).addBox(0F, 0F, -27F, 0F, 10F, 14F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cuchillo = p_mano_d.addOrReplaceChild("cuchillo", CubeListBuilder.create().texOffs(66, 53).addBox(-0.5F, -0.5F, -3F, 1F, 1F, 5F).texOffs(26, 39).addBox(0F, -2F, -10F, 0F, 3F, 7F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_baculo = p_mano_d.addOrReplaceChild("baculo", CubeListBuilder.create().texOffs(62, 0).addBox(-0.5F, -0.5F, -22F, 1F, 1F, 16F).texOffs(30, 24).addBox(-0.5F, -0.5F, -6F, 1F, 1F, 14F).texOffs(18, 63).addBox(-3F, -0.5F, -22F, 6F, 1F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_campana = p_baculo.addOrReplaceChild("campana", CubeListBuilder.create().texOffs(38, 63).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 1F).texOffs(78, 53).addBox(-1.5F, -1.5F, 1F, 3F, 3F, 3F).texOffs(90, 53).addBox(-2F, -2F, 3.6F, 4F, 4F, 0.8F), PartPose.offset(2.5F, 0F, -21F));
        PartDefinition p_farol = p_mano_i.addOrReplaceChild("farol", CubeListBuilder.create().texOffs(10, 63).addBox(-0.5F, -0.5F, -3F, 1F, 1F, 3F).texOffs(100, 53).addBox(-2F, -2F, -3.8F, 4F, 4F, 0.8F).texOffs(52, 53).addBox(-1.5F, -1.5F, -7.8F, 3F, 3F, 4F).texOffs(110, 53).addBox(-2F, -2F, -8.6F, 4F, 4F, 0.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_llama_farol = p_farol.addOrReplaceChild("llama_farol", CubeListBuilder.create().texOffs(0, 63).addBox(-1F, -1F, -1.5F, 2F, 2F, 3F), PartPose.offset(0F, 0F, -5.8F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"cuello", "cuchillo", "baculo", "campana", "farol", "llama_farol"};

    public static final String[] OCULTAS_1 = {"capucha", "guadana", "baculo", "campana"};

    public static final String[] OCULTAS_2 = {"cuello", "guadana", "cuchillo", "farol", "llama_farol"};
}
