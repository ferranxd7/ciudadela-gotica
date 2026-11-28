package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class OsmundGeometria {
    private OsmundGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -14F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(106, 34).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(-1.5F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(0, 46).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(12, 46).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(1.5F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(24, 46).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(8, 0).addBox(-4F, -13F, -2.5F, 8F, 13F, 5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(44, 0).addBox(-3.5F, -8F, -3.5F, 7F, 8F, 7F), PartPose.offset(0F, -13F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(54, 34).addBox(-1.5F, -2F, -1.5F, 3F, 7.5F, 3F), PartPose.offset(-4.9F, -11F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(66, 34).addBox(-1.5F, 0F, -1.5F, 3F, 7.5F, 3F), PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(78, 34).addBox(-1.5F, -2F, -1.5F, 3F, 7.5F, 3F), PartPose.offset(4.9F, -11F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(90, 34).addBox(-1.5F, 0F, -1.5F, 3F, 7.5F, 3F), PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_capucha = p_cabeza.addOrReplaceChild("capucha", CubeListBuilder.create().texOffs(72, 0).addBox(-3.5F, -8F, -3.5F, 7F, 8F, 7F, new CubeDeformation(0.65F, 0.65F, 0.65F)).texOffs(54, 46).addBox(-2F, -6.4F, 4.1F, 4F, 6.4F, 1.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_mandibula = p_cabeza.addOrReplaceChild("mandibula", CubeListBuilder.create().texOffs(30, 56).addBox(-2.5F, 0F, -2.2F, 5F, 2F, 3.4F), PartPose.offsetAndRotation(0F, -1.2F, -1.4F, 0.1745F, 0F, 0F));
        PartDefinition p_capucho = p_torso.addOrReplaceChild("capucho", CubeListBuilder.create().texOffs(20, 34).addBox(-5F, -0.8F, -3.1F, 10F, 3.6F, 6.2F, new CubeDeformation(0.2F, 0.2F, 0.2F)), PartPose.offset(0F, -13F, 0F));
        PartDefinition p_escapulario = p_torso.addOrReplaceChild("escapulario", CubeListBuilder.create().texOffs(34, 0).addBox(-2F, -12.4F, 2.55F, 4F, 17F, 0.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cordon = p_torso.addOrReplaceChild("cordon", CubeListBuilder.create().texOffs(66, 46).addBox(-4.3F, -2F, -2.8F, 8.6F, 1.3F, 5.6F).texOffs(102, 34).addBox(1.5F, -0.8F, -3.15F, 0.8F, 9.5F, 0.6F).texOffs(104, 46).addBox(-2.9F, -0.8F, -3.1F, 0.7F, 6.6F, 0.6F).texOffs(0, 63).addBox(-3.4F, 5.8F, -3.5F, 1.8F, 1.8F, 1.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_falda_d = p_muslo_d.addOrReplaceChild("falda_d", CubeListBuilder.create().texOffs(100, 0).addBox(-3F, -0.8F, -3.2F, 6F, 8F, 6.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bajo_d = p_pierna_d.addOrReplaceChild("bajo_d", CubeListBuilder.create().texOffs(26, 19).addBox(-3.1F, 0F, -3.3F, 6.2F, 6.4F, 6.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_manga_d = p_antebrazo_d.addOrReplaceChild("manga_d", CubeListBuilder.create().texOffs(108, 19).addBox(-2.5F, 0F, -2.5F, 5F, 6.2F, 5F), PartPose.offset(0F, 0.4F, 0F));
        PartDefinition p_falda_i = p_muslo_i.addOrReplaceChild("falda_i", CubeListBuilder.create().texOffs(0, 19).addBox(-3F, -0.8F, -3.32F, 6F, 8F, 6.64F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bajo_i = p_pierna_i.addOrReplaceChild("bajo_i", CubeListBuilder.create().texOffs(54, 19).addBox(-3.1F, 0F, -3.42F, 6.2F, 6.4F, 6.84F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_manga_i = p_antebrazo_i.addOrReplaceChild("manga_i", CubeListBuilder.create().texOffs(0, 34).addBox(-2.5F, 0F, -2.5F, 5F, 6.2F, 5F), PartPose.offset(0F, 0.4F, 0F));
        PartDefinition p_aro_0 = p_torso.addOrReplaceChild("aro_0", CubeListBuilder.create().texOffs(96, 46).addBox(-0.7F, -2.9F, -0.7F, 1.4F, 5.8F, 1.4F), PartPose.offsetAndRotation(2.6266F, -25.084F, 4.9F, 0F, 0F, -1.2566F));
        PartDefinition p_aro_1 = p_torso.addOrReplaceChild("aro_1", CubeListBuilder.create().texOffs(96, 46).addBox(-0.7F, -2.9F, -0.7F, 1.4F, 5.8F, 1.4F), PartPose.offsetAndRotation(6.8766F, -21.9962F, 4.9F, 0F, 0F, -0.6283F));
        PartDefinition p_aro_2 = p_torso.addOrReplaceChild("aro_2", CubeListBuilder.create().texOffs(96, 46).addBox(-0.7F, -2.9F, -0.7F, 1.4F, 5.8F, 1.4F), PartPose.offset(8.5F, -17F, 4.9F));
        PartDefinition p_aro_3 = p_torso.addOrReplaceChild("aro_3", CubeListBuilder.create().texOffs(96, 46).addBox(-0.7F, -2.9F, -0.7F, 1.4F, 5.8F, 1.4F), PartPose.offsetAndRotation(6.8766F, -12.0038F, 4.9F, 0F, 0F, 0.6283F));
        PartDefinition p_aro_4 = p_torso.addOrReplaceChild("aro_4", CubeListBuilder.create().texOffs(96, 46).addBox(-0.7F, -2.9F, -0.7F, 1.4F, 5.8F, 1.4F), PartPose.offsetAndRotation(2.6266F, -8.916F, 4.9F, 0F, 0F, 1.2566F));
        PartDefinition p_aro_5 = p_torso.addOrReplaceChild("aro_5", CubeListBuilder.create().texOffs(96, 46).addBox(-0.7F, -2.9F, -0.7F, 1.4F, 5.8F, 1.4F), PartPose.offsetAndRotation(-2.6266F, -8.916F, 4.9F, 0F, 0F, 1.885F));
        PartDefinition p_aro_6 = p_torso.addOrReplaceChild("aro_6", CubeListBuilder.create().texOffs(96, 46).addBox(-0.7F, -2.9F, -0.7F, 1.4F, 5.8F, 1.4F), PartPose.offsetAndRotation(-6.8766F, -12.0038F, 4.9F, 0F, 0F, 2.5133F));
        PartDefinition p_aro_7 = p_torso.addOrReplaceChild("aro_7", CubeListBuilder.create().texOffs(96, 46).addBox(-0.7F, -2.9F, -0.7F, 1.4F, 5.8F, 1.4F), PartPose.offsetAndRotation(-8.5F, -17F, 4.9F, 0F, 0F, 3.1416F));
        PartDefinition p_aro_8 = p_torso.addOrReplaceChild("aro_8", CubeListBuilder.create().texOffs(96, 46).addBox(-0.7F, -2.9F, -0.7F, 1.4F, 5.8F, 1.4F), PartPose.offsetAndRotation(-6.8766F, -21.9962F, 4.9F, 0F, 0F, 3.7699F));
        PartDefinition p_aro_9 = p_torso.addOrReplaceChild("aro_9", CubeListBuilder.create().texOffs(96, 46).addBox(-0.7F, -2.9F, -0.7F, 1.4F, 5.8F, 1.4F), PartPose.offsetAndRotation(-2.6266F, -25.084F, 4.9F, 0F, 0F, 4.3982F));
        PartDefinition p_calavera_0 = p_torso.addOrReplaceChild("calavera_0", CubeListBuilder.create().texOffs(48, 56).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F), PartPose.offset(0F, -25.5F, 4.7F));
        PartDefinition p_calavera_1 = p_torso.addOrReplaceChild("calavera_1", CubeListBuilder.create().texOffs(48, 56).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F), PartPose.offset(8.084F, -19.6266F, 4.7F));
        PartDefinition p_calavera_2 = p_torso.addOrReplaceChild("calavera_2", CubeListBuilder.create().texOffs(48, 56).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F), PartPose.offset(4.9962F, -10.1234F, 4.7F));
        PartDefinition p_calavera_3 = p_torso.addOrReplaceChild("calavera_3", CubeListBuilder.create().texOffs(48, 56).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F), PartPose.offset(-4.9962F, -10.1234F, 4.7F));
        PartDefinition p_calavera_4 = p_torso.addOrReplaceChild("calavera_4", CubeListBuilder.create().texOffs(48, 56).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F), PartPose.offset(-8.084F, -19.6266F, 4.7F));
        PartDefinition p_vela_0 = p_torso.addOrReplaceChild("vela_0", CubeListBuilder.create().texOffs(116, 56).addBox(-0.55F, -2.8F, -0.55F, 1.1F, 2.8F, 1.1F).texOffs(22, 63).addBox(-0.3F, -3.9F, -0.3F, 0.6F, 1.1F, 0.6F), PartPose.offset(4.9962F, -24.4766F, 4.9F));
        PartDefinition p_vela_1 = p_torso.addOrReplaceChild("vela_1", CubeListBuilder.create().texOffs(116, 56).addBox(-0.55F, -2.8F, -0.55F, 1.1F, 2.8F, 1.1F).texOffs(22, 63).addBox(-0.3F, -3.9F, -0.3F, 0.6F, 1.1F, 0.6F), PartPose.offset(9.684F, -14.9734F, 4.9F));
        PartDefinition p_vela_2 = p_torso.addOrReplaceChild("vela_2", CubeListBuilder.create().texOffs(116, 56).addBox(-0.55F, -2.8F, -0.55F, 1.1F, 2.8F, 1.1F).texOffs(22, 63).addBox(-0.3F, -3.9F, -0.3F, 0.6F, 1.1F, 0.6F), PartPose.offset(-9.684F, -14.9734F, 4.9F));
        PartDefinition p_vela_3 = p_torso.addOrReplaceChild("vela_3", CubeListBuilder.create().texOffs(116, 56).addBox(-0.55F, -2.8F, -0.55F, 1.1F, 2.8F, 1.1F).texOffs(22, 63).addBox(-0.3F, -3.9F, -0.3F, 0.6F, 1.1F, 0.6F), PartPose.offset(-4.9962F, -24.4766F, 4.9F));
        PartDefinition p_incensario = p_mano_d.addOrReplaceChild("incensario", CubeListBuilder.create().texOffs(82, 19).addBox(-0.35F, -0.35F, -12F, 0.7F, 0.7F, 12F).texOffs(96, 56).addBox(-1.2F, -1.2F, -13.3F, 2.4F, 2.4F, 1.3F).texOffs(36, 46).addBox(-1.9F, -1.9F, -17.5F, 3.8F, 3.8F, 4.2F).texOffs(16, 63).addBox(-1F, -1F, -18.1F, 2F, 2F, 0.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_campanilla = p_mano_i.addOrReplaceChild("campanilla", CubeListBuilder.create().texOffs(8, 63).addBox(-0.45F, -0.45F, -2.6F, 0.9F, 0.9F, 2.6F).texOffs(60, 56).addBox(-1.4F, -1.4F, -5.6F, 2.8F, 2.8F, 3F).texOffs(106, 56).addBox(-1.75F, -1.75F, -6.2F, 3.5F, 3.5F, 0.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_sosten_a = p_raiz.addOrReplaceChild("sosten_a", CubeListBuilder.create().texOffs(0, 0).addBox(-0.7F, 0F, -0.7F, 1.4F, 17F, 1.4F).texOffs(84, 56).addBox(-1.2F, -0.4F, -1.2F, 2.4F, 1.6F, 2.4F), PartPose.offsetAndRotation(-2.6F, 0.4F, -1.2F, 0.1745F, 0F, -0.2094F));
        PartDefinition p_sosten_b = p_raiz.addOrReplaceChild("sosten_b", CubeListBuilder.create().texOffs(0, 0).addBox(-0.7F, 0F, -0.7F, 1.4F, 17F, 1.4F).texOffs(84, 56).addBox(-1.2F, -0.4F, -1.2F, 2.4F, 1.6F, 2.4F), PartPose.offsetAndRotation(2.6F, 0.4F, -1F, 0.1396F, 0F, 0.2094F));
        PartDefinition p_sosten_c = p_raiz.addOrReplaceChild("sosten_c", CubeListBuilder.create().texOffs(0, 0).addBox(-0.7F, 0F, -0.7F, 1.4F, 16.5F, 1.4F).texOffs(84, 56).addBox(-1.2F, -0.4F, -1.2F, 2.4F, 1.6F, 2.4F), PartPose.offsetAndRotation(0F, 0.4F, 2.4F, -0.2443F, 0F, 0F));
        PartDefinition p_sosten_d = p_raiz.addOrReplaceChild("sosten_d", CubeListBuilder.create().texOffs(0, 0).addBox(-0.7F, 0F, -0.7F, 1.4F, 16F, 1.4F).texOffs(84, 56).addBox(-1.2F, -0.4F, -1.2F, 2.4F, 1.6F, 2.4F), PartPose.offsetAndRotation(-1.4F, 0.4F, 1.2F, -0.1047F, 0F, -0.1047F));
        PartDefinition p_sosten_e = p_raiz.addOrReplaceChild("sosten_e", CubeListBuilder.create().texOffs(0, 0).addBox(-0.7F, 0F, -0.7F, 1.4F, 16F, 1.4F).texOffs(84, 56).addBox(-1.2F, -0.4F, -1.2F, 2.4F, 1.6F, 2.4F), PartPose.offsetAndRotation(1.6F, 0.4F, -2.4F, 0.2793F, 0F, 0.1047F));
        PartDefinition p_sosten_craneos = p_raiz.addOrReplaceChild("sosten_craneos", CubeListBuilder.create().texOffs(72, 56).addBox(-3.5F, -1F, -3.5F, 3F, 3F, 3F).texOffs(72, 56).addBox(0.6F, -0.6F, -2.8F, 3F, 3F, 3F).texOffs(72, 56).addBox(-1.4F, -1.6F, 0.8F, 3F, 3F, 3F).texOffs(0, 56).addBox(-4.6F, 1F, -1F, 9.2F, 1.4F, 4.4F), PartPose.offset(0F, 15F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"sosten_a", "sosten_b", "sosten_c", "sosten_d", "sosten_e", "sosten_craneos"};
}
