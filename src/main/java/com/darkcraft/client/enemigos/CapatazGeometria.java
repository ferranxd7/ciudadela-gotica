package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class CapatazGeometria {
    private CapatazGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(0, 59).addBox(-2.5F, 0F, -2.5F, 5F, 6F, 5F), PartPose.offset(-2.5F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(20, 59).addBox(-2.5F, 0F, -2.5F, 5F, 6F, 5F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(40, 59).addBox(-2.5F, 0F, -2.5F, 5F, 6F, 5F), PartPose.offset(2.5F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(60, 59).addBox(-2.5F, 0F, -2.5F, 5F, 6F, 5F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(64, 0).addBox(-5F, -13F, -3F, 10F, 13F, 6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(96, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -13F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(0, 47).addBox(-2F, -2F, -2F, 4F, 8F, 4F), PartPose.offset(-6.7F, -11F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(16, 47).addBox(-2F, 0F, -2F, 4F, 8F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(32, 47).addBox(-2F, -2F, -2F, 4F, 8F, 4F), PartPose.offset(6.7F, -11F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(48, 47).addBox(-2F, 0F, -2F, 4F, 8F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_casco = p_cabeza.addOrReplaceChild("casco", CubeListBuilder.create().texOffs(58, 32).addBox(-4.4F, -9.6F, -4.4F, 8.8F, 3.2F, 8.8F).texOffs(8, 86).addBox(-4.6F, -6.6F, -5.7F, 9.2F, 0.7F, 1.6F).texOffs(36, 70).addBox(-0.6F, -10.2F, -4.2F, 1.2F, 0.8F, 8.4F).texOffs(40, 80).addBox(-1.1F, -9.4F, -5.9F, 2.2F, 2.4F, 1.6F).texOffs(40, 86).addBox(-0.6F, -10.9F, -5.6F, 1.2F, 1.5F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_barba = p_cabeza.addOrReplaceChild("barba", CubeListBuilder.create().texOffs(70, 70).addBox(-3.9F, 0F, -1.4F, 7.8F, 6.6F, 1.6F), PartPose.offsetAndRotation(0F, -3.4F, -4.1F, -0.1047F, 0F, 0F));
        PartDefinition p_cuello = p_torso.addOrReplaceChild("cuello", CubeListBuilder.create().texOffs(74, 47).addBox(-5.4F, -2F, -3.4F, 10.8F, 3.2F, 6.8F, new CubeDeformation(0.1F, 0.1F, 0.1F)), PartPose.offset(0F, -13F, 0F));
        PartDefinition p_faldon = p_torso.addOrReplaceChild("faldon", CubeListBuilder.create().texOffs(94, 32).addBox(-5.3F, 0F, 0F, 10.6F, 11F, 0.8F), PartPose.offsetAndRotation(0F, -1.2F, 3.1F, 0.1047F, 0F, 0F));
        PartDefinition p_faldon_d = p_muslo_d.addOrReplaceChild("faldon_d", CubeListBuilder.create().texOffs(0, 32).addBox(-2.9F, -0.8F, -2.9F, 5.8F, 8.2F, 5.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon_i = p_muslo_i.addOrReplaceChild("faldon_i", CubeListBuilder.create().texOffs(24, 32).addBox(-2.9F, -0.8F, -2.9F, 5.8F, 8.2F, 5.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cinturon = p_torso.addOrReplaceChild("cinturon", CubeListBuilder.create().texOffs(0, 70).addBox(-5.35F, -2.2F, -3.35F, 10.7F, 2.2F, 6.7F).texOffs(116, 80).addBox(-1.3F, -2.5F, -3.75F, 2.6F, 2.8F, 0.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_silbato = p_torso.addOrReplaceChild("silbato", CubeListBuilder.create().texOffs(36, 80).addBox(-0.2F, 0F, -0.25F, 0.4F, 4.2F, 0.3F).texOffs(32, 86).addBox(-0.8F, 4F, -0.9F, 1.6F, 0.9F, 1.4F), PartPose.offset(0F, -11.5F, -3.2F));
        PartDefinition p_llaves = p_torso.addOrReplaceChild("llaves", CubeListBuilder.create().texOffs(12, 80).addBox(-0.5F, 0F, -1.2F, 0.5F, 2.6F, 2.4F), PartPose.offset(-5.4F, -1.5F, -1F));
        PartDefinition p_farol = p_torso.addOrReplaceChild("farol", CubeListBuilder.create().texOffs(46, 86).addBox(-0.25F, 0F, -0.25F, 0.5F, 1.6F, 0.5F).texOffs(92, 80).addBox(-1.5F, 1.6F, -1.5F, 3F, 0.7F, 3F).texOffs(0, 80).addBox(-1.3F, 2.3F, -1.3F, 2.6F, 3F, 2.6F).texOffs(104, 80).addBox(-1.5F, 5.3F, -1.5F, 3F, 0.6F, 3F), PartPose.offsetAndRotation(6.2F, -1.4F, 0.4F, 0F, 0F, 0.1047F));
        PartDefinition p_veta_hombro = p_brazo_i.addOrReplaceChild("veta_hombro", CubeListBuilder.create().texOffs(90, 70).addBox(-1F, -5.6F, -1F, 2F, 5.6F, 2F).texOffs(20, 80).addBox(-0.7F, -3.8F, -2.6F, 1.4F, 3.8F, 1.4F).texOffs(60, 80).addBox(0.6F, -3F, 0.9F, 1.4F, 3F, 1.4F), PartPose.offsetAndRotation(1.2F, -2.2F, 0.2F, 0F, 0F, 0.4189F));
        PartDefinition p_veta_hombro2 = p_brazo_i.addOrReplaceChild("veta_hombro2", CubeListBuilder.create().texOffs(112, 70).addBox(-0.8F, -4.4F, -0.8F, 1.6F, 4.4F, 1.6F), PartPose.offsetAndRotation(2F, 0.8F, -0.6F, 0.2443F, 0F, 1.2217F));
        PartDefinition p_veta_brazo = p_antebrazo_i.addOrReplaceChild("veta_brazo", CubeListBuilder.create().texOffs(80, 59).addBox(-2.4F, 0.6F, -2.4F, 4.8F, 5.4F, 4.8F).texOffs(68, 80).addBox(1.6F, 1.4F, -0.7F, 1.8F, 2.4F, 1.4F).texOffs(0, 86).addBox(-0.7F, 2.6F, -3.4F, 1.4F, 2F, 1.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_veta_espalda = p_torso.addOrReplaceChild("veta_espalda", CubeListBuilder.create().texOffs(58, 70).addBox(-1.1F, -6.4F, -1.1F, 2.2F, 6.4F, 2.2F), PartPose.offsetAndRotation(1.5F, -9F, 3F, -0.5934F, 0F, 0.3142F));
        PartDefinition p_veta_espalda2 = p_torso.addOrReplaceChild("veta_espalda2", CubeListBuilder.create().texOffs(120, 70).addBox(-0.9F, -4.6F, -0.9F, 1.8F, 4.6F, 1.8F), PartPose.offsetAndRotation(3.6F, -5.5F, 3F, -0.8727F, 0F, 0.6981F));
        PartDefinition p_veta_espalda3 = p_torso.addOrReplaceChild("veta_espalda3", CubeListBuilder.create().texOffs(28, 80).addBox(-0.7F, -3.4F, -0.7F, 1.4F, 3.4F, 1.4F), PartPose.offsetAndRotation(-1.8F, -11F, 3F, -0.4887F, 0F, -0.1745F));
        PartDefinition p_pico = p_mano_d.addOrReplaceChild("pico", CubeListBuilder.create().texOffs(0, 0).addBox(-0.9F, -0.9F, -25F, 1.8F, 1.8F, 30F).texOffs(50, 80).addBox(-1.1F, -1.1F, 4.6F, 2.2F, 2.2F, 1.4F).texOffs(98, 70).addBox(-1.4F, -1.4F, -27.6F, 2.8F, 2.8F, 3.4F).texOffs(48, 32).addBox(-1F, -13F, -27.2F, 2F, 11.8F, 2.6F).texOffs(64, 47).addBox(-1F, 1.2F, -27.2F, 2F, 9F, 2.6F).texOffs(76, 80).addBox(-0.6F, -15.4F, -26.8F, 1.2F, 2.6F, 1.8F).texOffs(84, 80).addBox(-0.6F, 10F, -26.8F, 1.2F, 2.2F, 1.8F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {};
}
