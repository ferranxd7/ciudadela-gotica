package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class PenitenteGeometria {
    private PenitenteGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(96, 52).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(112, 52).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(0, 64).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(16, 64).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(32, 36).addBox(-4F, -12F, -2F, 8F, 12F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(96, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(32, 64).addBox(-2F, -2F, -2F, 4F, 6F, 4F), PartPose.offset(-6F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(48, 64).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(64, 64).addBox(-2F, -2F, -2F, 4F, 6F, 4F), PartPose.offset(6F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(80, 64).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_capucha = p_cabeza.addOrReplaceChild("capucha", CubeListBuilder.create().texOffs(0, 36).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.45F, 0.45F, 0.45F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_capirote = p_cabeza.addOrReplaceChild("capirote", CubeListBuilder.create().texOffs(0, 52).addBox(-3.5F, -4.2F, -3.5F, 7F, 4.4F, 7F).texOffs(48, 52).addBox(-2.6F, -8.2F, -2.6F, 5.2F, 4.2F, 5.2F).texOffs(32, 83).addBox(-1.8F, -12F, -1.8F, 3.6F, 4F, 3.6F).texOffs(48, 83).addBox(-1.1F, -15.4F, -1.1F, 2.2F, 3.6F, 2.2F).texOffs(24, 91).addBox(-0.5F, -18F, -0.5F, 1F, 2.8F, 1F), PartPose.offsetAndRotation(0F, -8.4F, 0.3F, -0.1222F, 0F, 0F));
        PartDefinition p_antifaz = p_cabeza.addOrReplaceChild("antifaz", CubeListBuilder.create().texOffs(56, 36).addBox(-4.3F, 0F, -0.5F, 8.6F, 13.4F, 0.55F), PartPose.offsetAndRotation(0F, -8.2F, -4.55F, 0.0698F, 0F, 0F));
        PartDefinition p_esclavina = p_cabeza.addOrReplaceChild("esclavina", CubeListBuilder.create().texOffs(28, 52).addBox(-4.4F, 0F, 0F, 8.8F, 11F, 0.55F), PartPose.offsetAndRotation(0F, -6.5F, 4.5F, -0.1047F, 0F, 0F));
        PartDefinition p_faldon_tras = p_cadera.addOrReplaceChild("faldon_tras", CubeListBuilder.create().texOffs(76, 36).addBox(-4.4F, -0.5F, 1.6F, 8.8F, 12F, 0.9F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon_d = p_muslo_d.addOrReplaceChild("faldon_d", CubeListBuilder.create().texOffs(72, 0).addBox(-2.8F, -0.5F, -2.5F, 0.9F, 11.6F, 4.6F).texOffs(96, 36).addBox(-2.4F, -0.5F, -2.95F, 4.4F, 11.6F, 0.9F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon_i = p_muslo_i.addOrReplaceChild("faldon_i", CubeListBuilder.create().texOffs(84, 0).addBox(1.9F, -0.5F, -2.5F, 0.9F, 11.6F, 4.6F).texOffs(108, 36).addBox(-2F, -0.5F, -2.95F, 4.4F, 11.6F, 0.9F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cingulo = p_torso.addOrReplaceChild("cingulo", CubeListBuilder.create().texOffs(0, 83).addBox(-4.6F, -1.4F, -2.6F, 9.2F, 1.4F, 5.2F).texOffs(72, 52).addBox(1.4F, -0.2F, -2.9F, 0.9F, 9.5F, 0.9F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tunica_caida = p_torso.addOrReplaceChild("tunica_caida", CubeListBuilder.create().texOffs(96, 64).addBox(-4.7F, -3.4F, -2.7F, 9.4F, 2.4F, 5.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_disciplina = p_mano_d.addOrReplaceChild("disciplina", CubeListBuilder.create().texOffs(0, 74).addBox(-0.6F, -0.6F, -4.8F, 1.2F, 1.2F, 6.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cuerda_0 = p_disciplina.addOrReplaceChild("cuerda_0", CubeListBuilder.create().texOffs(18, 74).addBox(-0.3F, -0.3F, -7.6F, 0.6F, 0.6F, 7.6F).texOffs(120, 83).addBox(-0.6F, -0.6F, -8.6F, 1.2F, 1.2F, 1.2F), PartPose.offsetAndRotation(0F, 0F, -4.8F, 0.1745F, 0.2443F, 0F));
        PartDefinition p_cuerda_1 = p_disciplina.addOrReplaceChild("cuerda_1", CubeListBuilder.create().texOffs(36, 74).addBox(-0.3F, -0.3F, -7.6F, 0.6F, 0.6F, 7.6F).texOffs(0, 91).addBox(-0.6F, -0.6F, -8.6F, 1.2F, 1.2F, 1.2F), PartPose.offsetAndRotation(0F, 0F, -4.8F, -0.1047F, 0.1571F, 0F));
        PartDefinition p_cuerda_2 = p_disciplina.addOrReplaceChild("cuerda_2", CubeListBuilder.create().texOffs(54, 74).addBox(-0.3F, -0.3F, -7.6F, 0.6F, 0.6F, 7.6F).texOffs(8, 91).addBox(-0.6F, -0.6F, -8.6F, 1.2F, 1.2F, 1.2F), PartPose.offsetAndRotation(0F, 0F, -4.8F, 0.2443F, -0.1396F, 0F));
        PartDefinition p_cuerda_3 = p_disciplina.addOrReplaceChild("cuerda_3", CubeListBuilder.create().texOffs(72, 74).addBox(-0.3F, -0.3F, -7.6F, 0.6F, 0.6F, 7.6F).texOffs(16, 91).addBox(-0.6F, -0.6F, -8.6F, 1.2F, 1.2F, 1.2F), PartPose.offsetAndRotation(0F, 0F, -4.8F, -0.1745F, -0.2443F, 0F));
        PartDefinition p_cruz = p_mano_d.addOrReplaceChild("cruz", CubeListBuilder.create().texOffs(0, 0).addBox(-1.3F, -1.3F, -27F, 2.6F, 2.6F, 33F).texOffs(60, 83).addBox(-8F, -1.2F, -22.4F, 16F, 2.4F, 2.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cadena = p_mano_d.addOrReplaceChild("cadena", CubeListBuilder.create().texOffs(76, 52).addBox(-0.3F, -0.3F, -8.5F, 0.6F, 0.6F, 9F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_incensario = p_cadena.addOrReplaceChild("incensario", CubeListBuilder.create().texOffs(90, 74).addBox(-1.9F, -1.9F, -4.2F, 3.8F, 3.8F, 4.2F).texOffs(110, 83).addBox(-1.4F, -1.4F, -5.4F, 2.8F, 2.8F, 1.2F).texOffs(28, 91).addBox(-0.7F, -0.7F, -6.4F, 1.4F, 1.4F, 1F).texOffs(98, 83).addBox(-2.3F, -2.3F, -1F, 4.6F, 4.6F, 1F), PartPose.offset(0F, 0F, -8.5F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"cruz", "cadena", "incensario"};

    public static final String[] OCULTAS_1 = {"tunica_caida", "disciplina", "cuerda_0", "cuerda_1", "cuerda_2", "cuerda_3", "cadena", "incensario"};

    public static final String[] OCULTAS_2 = {"tunica_caida", "disciplina", "cuerda_0", "cuerda_1", "cuerda_2", "cuerda_3", "cruz"};
}
