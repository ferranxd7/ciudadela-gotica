package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class CondestableGeometria {
    private CondestableGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -14F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(0, 77).addBox(-2.5F, 0F, -2.5F, 5F, 7F, 5F), PartPose.offset(-2.5F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(20, 77).addBox(-2.5F, 0F, -2.5F, 5F, 7F, 5F), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(40, 77).addBox(-2.5F, 0F, -2.5F, 5F, 7F, 5F), PartPose.offset(2.5F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(60, 77).addBox(-2.5F, 0F, -2.5F, 5F, 7F, 5F), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(40, 28).addBox(-5.5F, -14F, -3F, 11F, 14F, 6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(0, 48).addBox(-4F, -9F, -4F, 8F, 9F, 8F), PartPose.offset(0F, -14F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(100, 77).addBox(-2F, -2F, -2F, 4F, 8F, 4F), PartPose.offset(-7.1F, -12F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(0, 89).addBox(-2F, 0F, -2F, 4F, 8F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(16, 89).addBox(-2F, -2F, -2F, 4F, 8F, 4F), PartPose.offset(7.1F, -12F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(32, 89).addBox(-2F, 0F, -2F, 4F, 8F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_yelmo = p_cabeza.addOrReplaceChild("yelmo", CubeListBuilder.create().texOffs(0, 28).addBox(-4.6F, -9.6F, -4.6F, 9.2F, 10F, 9.2F).texOffs(0, 65).addBox(-0.7F, -10.6F, -4.8F, 1.4F, 1.2F, 9.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cimera = p_yelmo.addOrReplaceChild("cimera", CubeListBuilder.create().texOffs(80, 77).addBox(-0.9F, -3.4F, -3.8F, 1.8F, 3.4F, 7.2F).texOffs(112, 112).addBox(-0.9F, -1.8F, 3.4F, 1.8F, 7F, 1.6F), PartPose.offset(0F, -10.6F, 0F));
        PartDefinition p_gola = p_torso.addOrReplaceChild("gola", CubeListBuilder.create().texOffs(48, 89).addBox(-4.6F, -1.6F, -3.6F, 9.2F, 3F, 7.2F), PartPose.offset(0F, -14F, 0F));
        PartDefinition p_coraza = p_torso.addOrReplaceChild("coraza", CubeListBuilder.create().texOffs(74, 28).addBox(-5.9F, -13.4F, -3.6F, 11.8F, 10.2F, 7.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldar = p_cadera.addOrReplaceChild("faldar", CubeListBuilder.create().texOffs(32, 48).addBox(-6F, -3.4F, -3.5F, 12F, 6.6F, 7F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_sobreveste = p_torso.addOrReplaceChild("sobreveste", CubeListBuilder.create().texOffs(70, 0).addBox(-4.2F, 0F, -3.95F, 8.4F, 21F, 0.4F).texOffs(90, 0).addBox(-4.2F, 0F, 3.55F, 8.4F, 21F, 0.4F), PartPose.offset(0F, -12.4F, 0F));
        PartDefinition p_capa = p_torso.addOrReplaceChild("capa", CubeListBuilder.create().texOffs(0, 0).addBox(-6.6F, 0F, 0F, 13.2F, 27F, 0.9F), PartPose.offsetAndRotation(0F, -13.8F, 3.9F, 0.1047F, 0F, 0F));
        PartDefinition p_hombrera_d = p_brazo_d.addOrReplaceChild("hombrera_d", CubeListBuilder.create().texOffs(84, 89).addBox(-3.2F, -3.8F, -3.2F, 6.4F, 4F, 6.4F).texOffs(0, 112).addBox(-3.4F, -0.4F, -3.4F, 6.8F, 1.8F, 6.8F).texOffs(28, 112).addBox(-3.5F, 1.2F, -3.5F, 7F, 1.6F, 7F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_guantelete_d = p_antebrazo_d.addOrReplaceChild("guantelete_d", CubeListBuilder.create().texOffs(24, 65).addBox(-2.6F, 3F, -2.6F, 5.2F, 5.4F, 5.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_quijote_d = p_muslo_d.addOrReplaceChild("quijote_d", CubeListBuilder.create().texOffs(70, 48).addBox(-2.9F, 0.6F, -2.9F, 5.8F, 6.4F, 5.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_greba_d = p_pierna_d.addOrReplaceChild("greba_d", CubeListBuilder.create().texOffs(48, 65).addBox(-2.9F, -0.4F, -2.9F, 5.8F, 5.6F, 5.8F).texOffs(0, 101).addBox(-2.8F, 5F, -4.4F, 5.6F, 2.2F, 7.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hombrera_i = p_brazo_i.addOrReplaceChild("hombrera_i", CubeListBuilder.create().texOffs(28, 101).addBox(-3.2F, -3.8F, -3.2F, 6.4F, 4F, 6.4F).texOffs(56, 112).addBox(-3.4F, -0.4F, -3.4F, 6.8F, 1.8F, 6.8F).texOffs(84, 112).addBox(-3.5F, 1.2F, -3.5F, 7F, 1.6F, 7F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_guantelete_i = p_antebrazo_i.addOrReplaceChild("guantelete_i", CubeListBuilder.create().texOffs(72, 65).addBox(-2.6F, 3F, -2.6F, 5.2F, 5.4F, 5.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_quijote_i = p_muslo_i.addOrReplaceChild("quijote_i", CubeListBuilder.create().texOffs(94, 48).addBox(-2.9F, 0.6F, -2.9F, 5.8F, 6.4F, 5.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_greba_i = p_pierna_i.addOrReplaceChild("greba_i", CubeListBuilder.create().texOffs(96, 65).addBox(-2.9F, -0.4F, -2.9F, 5.8F, 5.6F, 5.8F).texOffs(56, 101).addBox(-2.8F, 5F, -4.4F, 5.6F, 2.2F, 7.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_montante = p_mano_d.addOrReplaceChild("montante", CubeListBuilder.create().texOffs(84, 101).addBox(-0.8F, -0.8F, -2.2F, 1.6F, 1.6F, 7.6F).texOffs(10, 121).addBox(-1.4F, -1.4F, 5.4F, 2.8F, 2.8F, 2.2F).texOffs(32, 121).addBox(-6.4F, -1F, -3.8F, 12.8F, 2F, 1.6F).texOffs(0, 121).addBox(-0.9F, -1.6F, -6.4F, 1.8F, 3.2F, 2.6F).texOffs(30, 0).addBox(-0.7F, -2.2F, -24.4F, 1.4F, 4.4F, 18F).texOffs(22, 121).addBox(-0.7F, -1.3F, -27F, 1.4F, 2.6F, 2.6F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {};
}
