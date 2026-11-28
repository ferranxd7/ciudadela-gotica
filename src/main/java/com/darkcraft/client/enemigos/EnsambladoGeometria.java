package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class EnsambladoGeometria {
    private EnsambladoGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(78, 75).addBox(-1F, 0F, -1F, 2F, 6F, 2F), PartPose.offset(-1F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(86, 75).addBox(-1F, 0F, -1F, 2F, 6F, 2F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(94, 75).addBox(-1F, 0F, -1F, 2F, 6F, 2F), PartPose.offset(1F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(102, 75).addBox(-1F, 0F, -1F, 2F, 6F, 2F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(32, 45).addBox(-4F, -12F, -2F, 8F, 12F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(70, 28).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(110, 75).addBox(-1F, -2F, -1F, 2F, 6F, 2F), PartPose.offset(-4F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(118, 75).addBox(-1F, 0F, -1F, 2F, 6F, 2F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(0, 85).addBox(-1F, -2F, -1F, 2F, 6F, 2F), PartPose.offset(4F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(8, 85).addBox(-1F, 0F, -1F, 2F, 6F, 2F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_columna = p_torso.addOrReplaceChild("columna", CubeListBuilder.create().texOffs(20, 61).addBox(-1F, -12F, 0.2F, 2F, 12F, 1.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_pelvis = p_cadera.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(18, 93).addBox(-3.5F, -1.5F, -1.5F, 7F, 3F, 3F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_mandibula = p_cabeza.addOrReplaceChild("mandibula", CubeListBuilder.create().texOffs(38, 93).addBox(-3F, 0F, -3F, 6F, 2F, 4F), PartPose.offset(0F, -1F, -1F));
        PartDefinition p_capucha = p_cabeza.addOrReplaceChild("capucha", CubeListBuilder.create().texOffs(0, 45).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.55F, 0.55F, 0.55F)).texOffs(64, 75).addBox(-2.5F, -5F, 4.5F, 5F, 6F, 1.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_escapulario = p_torso.addOrReplaceChild("escapulario", CubeListBuilder.create().texOffs(86, 45).addBox(-2F, -11.6F, -2.7F, 4F, 14F, 0.6F).texOffs(28, 61).addBox(-2F, -11.6F, 2.1F, 4F, 12F, 0.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cordon = p_torso.addOrReplaceChild("cordon", CubeListBuilder.create().texOffs(16, 85).addBox(-4.3F, -1.6F, -2.4F, 8.6F, 1.2F, 4.8F).texOffs(14, 93).addBox(1.6F, -0.6F, -2.7F, 0.8F, 6F, 0.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon_d = p_muslo_d.addOrReplaceChild("faldon_d", CubeListBuilder.create().texOffs(96, 45).addBox(-2.3F, -0.4F, -2.5F, 4.6F, 9F, 5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon_i = p_muslo_i.addOrReplaceChild("faldon_i", CubeListBuilder.create().texOffs(0, 61).addBox(-2.3F, -0.4F, -2.5F, 4.6F, 9F, 5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_candelabro = p_mano_d.addOrReplaceChild("candelabro", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6F, -0.6F, -21F, 1.2F, 1.2F, 26F).texOffs(70, 93).addBox(-4.6F, -0.6F, -20.6F, 9.2F, 1.2F, 1.2F).texOffs(106, 93).addBox(-4.6F, -0.6F, -21.8F, 1.2F, 1.2F, 1.4F).texOffs(114, 93).addBox(3.4F, -0.6F, -21.8F, 1.2F, 1.2F, 1.4F).texOffs(0, 100).addBox(-4.5F, -0.5F, -24.2F, 1F, 1F, 2.6F).texOffs(8, 100).addBox(3.5F, -0.5F, -24.2F, 1F, 1F, 2.6F).texOffs(16, 100).addBox(-0.5F, -0.5F, -26.6F, 1F, 1F, 2.6F).texOffs(24, 100).addBox(-4.3F, -0.3F, -25.2F, 0.6F, 0.6F, 1F).texOffs(28, 100).addBox(3.7F, -0.3F, -25.2F, 0.6F, 0.6F, 1F).texOffs(32, 100).addBox(-0.3F, -0.3F, -27.6F, 0.6F, 0.6F, 1F).texOffs(58, 93).addBox(-1.2F, -1.2F, -24F, 2.4F, 2.4F, 2.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_capa = p_torso.addOrReplaceChild("capa", CubeListBuilder.create().texOffs(98, 0).addBox(-4.5F, 0F, 0F, 9F, 17F, 1F), PartPose.offsetAndRotation(0F, -12F, 2.6F, 0.1047F, 0F, 0F));
        PartDefinition p_pala = p_mano_d.addOrReplaceChild("pala", CubeListBuilder.create().texOffs(56, 0).addBox(-0.6F, -0.6F, -17F, 1.2F, 1.2F, 19F).texOffs(94, 93).addBox(-1.8F, -0.6F, 1.4F, 3.6F, 1.2F, 1.2F).texOffs(44, 85).addBox(-2.4F, -0.4F, -22.6F, 4.8F, 0.8F, 5.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_costillar = p_torso.addOrReplaceChild("costillar", CubeListBuilder.create().texOffs(34, 28).addBox(-5.6F, -12.6F, -3F, 11.2F, 10F, 6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_craneos = p_torso.addOrReplaceChild("craneos", CubeListBuilder.create().texOffs(66, 85).addBox(-1.7F, -9.4F, -3.9F, 3.4F, 3.4F, 2.2F).texOffs(80, 85).addBox(-1.6F, -11.4F, 2.8F, 3.2F, 3.2F, 2.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_craneo_hombro = p_brazo_i.addOrReplaceChild("craneo_hombro", CubeListBuilder.create().texOffs(48, 75).addBox(-1.9F, -4.6F, -1.9F, 3.8F, 3.6F, 3.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_haz_brazo_d = p_brazo_d.addOrReplaceChild("haz_brazo_d", CubeListBuilder.create().texOffs(38, 61).addBox(-1.7F, -2F, -1.7F, 3.4F, 6.2F, 3.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_haz_antebrazo_d = p_antebrazo_d.addOrReplaceChild("haz_antebrazo_d", CubeListBuilder.create().texOffs(70, 61).addBox(-1.6F, 0F, -1.6F, 3.2F, 6F, 3.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_haz_muslo_d = p_muslo_d.addOrReplaceChild("haz_muslo_d", CubeListBuilder.create().texOffs(86, 61).addBox(-1.7F, 0F, -1.7F, 3.4F, 6F, 3.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_haz_pierna_d = p_pierna_d.addOrReplaceChild("haz_pierna_d", CubeListBuilder.create().texOffs(102, 61).addBox(-1.6F, 0F, -1.6F, 3.2F, 6F, 3.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_haz_brazo_i = p_brazo_i.addOrReplaceChild("haz_brazo_i", CubeListBuilder.create().texOffs(54, 61).addBox(-1.7F, -2F, -1.7F, 3.4F, 6.2F, 3.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_haz_antebrazo_i = p_antebrazo_i.addOrReplaceChild("haz_antebrazo_i", CubeListBuilder.create().texOffs(0, 75).addBox(-1.6F, 0F, -1.6F, 3.2F, 6F, 3.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_haz_muslo_i = p_muslo_i.addOrReplaceChild("haz_muslo_i", CubeListBuilder.create().texOffs(16, 75).addBox(-1.7F, 0F, -1.7F, 3.4F, 6F, 3.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_haz_pierna_i = p_pierna_i.addOrReplaceChild("haz_pierna_i", CubeListBuilder.create().texOffs(32, 75).addBox(-1.6F, 0F, -1.6F, 3.2F, 6F, 3.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_maza = p_mano_d.addOrReplaceChild("maza", CubeListBuilder.create().texOffs(0, 28).addBox(-0.8F, -0.8F, -13F, 1.6F, 1.6F, 15F).texOffs(56, 45).addBox(-2.7F, -2.7F, -21.4F, 5.4F, 5.4F, 8.6F).texOffs(94, 85).addBox(-2.9F, -2.9F, -17.2F, 5.8F, 5.8F, 1F).texOffs(108, 85).addBox(-2.9F, -2.9F, -20.4F, 5.8F, 5.8F, 1F).texOffs(0, 93).addBox(-1.7F, -1.7F, -24F, 3.4F, 3.4F, 2.6F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"capa", "pala", "costillar", "craneos", "craneo_hombro", "maza", "haz_brazo_d", "haz_brazo_i", "haz_antebrazo_d", "haz_antebrazo_i", "haz_muslo_d", "haz_muslo_i", "haz_pierna_d", "haz_pierna_i"};

    public static final String[] OCULTAS_1 = {"escapulario", "cordon", "faldon_d", "faldon_i", "candelabro", "costillar", "craneos", "craneo_hombro", "maza", "haz_brazo_d", "haz_brazo_i", "haz_antebrazo_d", "haz_antebrazo_i", "haz_muslo_d", "haz_muslo_i", "haz_pierna_d", "haz_pierna_i"};

    public static final String[] OCULTAS_2 = {"escapulario", "cordon", "faldon_d", "faldon_i", "candelabro", "capa", "pala", "capucha"};
}
