package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class CalcinadoGeometria {
    private CalcinadoGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(104, 55).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(0, 67).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(16, 67).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(32, 67).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(34, 22).addBox(-4F, -12F, -2.5F, 8F, 12F, 5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(16, 39).addBox(-3.5F, -8F, -3.5F, 7F, 8F, 7F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(48, 67).addBox(-1.5F, -2F, -1.5F, 3F, 7F, 3F), PartPose.offset(-5.1F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(60, 67).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(72, 67).addBox(-1.5F, -2F, -1.5F, 3F, 7F, 3F), PartPose.offset(5.1F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(84, 67).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_yelmo = p_cabeza.addOrReplaceChild("yelmo", CubeListBuilder.create().texOffs(84, 0).addBox(-4.2F, -8.6F, -4.2F, 8.4F, 8.8F, 8.4F).texOffs(20, 77).addBox(-0.6F, -10.2F, -3F, 1.2F, 1.8F, 6.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hombrera_d = p_brazo_d.addOrReplaceChild("hombrera_d", CubeListBuilder.create().texOffs(96, 67).addBox(-2.3F, -2.8F, -2.3F, 4.6F, 4F, 4.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_guantelete_d = p_antebrazo_d.addOrReplaceChild("guantelete_d", CubeListBuilder.create().texOffs(38, 77).addBox(-1.8F, 2.6F, -1.8F, 3.6F, 4.4F, 3.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_greba_d = p_pierna_d.addOrReplaceChild("greba_d", CubeListBuilder.create().texOffs(106, 39).addBox(-2.3F, -0.2F, -2.3F, 4.6F, 6.4F, 4.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hombrera_i = p_brazo_i.addOrReplaceChild("hombrera_i", CubeListBuilder.create().texOffs(0, 77).addBox(-2.3F, -2.8F, -2.3F, 4.6F, 4F, 4.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_guantelete_i = p_antebrazo_i.addOrReplaceChild("guantelete_i", CubeListBuilder.create().texOffs(54, 77).addBox(-1.8F, 2.6F, -1.8F, 3.6F, 4.4F, 3.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_greba_i = p_pierna_i.addOrReplaceChild("greba_i", CubeListBuilder.create().texOffs(0, 55).addBox(-2.3F, -0.2F, -2.3F, 4.6F, 6.4F, 4.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_peto = p_torso.addOrReplaceChild("peto", CubeListBuilder.create().texOffs(88, 22).addBox(-4.4F, -12.3F, -2.9F, 8.8F, 9.2F, 5.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldar = p_cadera.addOrReplaceChild("faldar", CubeListBuilder.create().texOffs(44, 39).addBox(-4.5F, -3.2F, -2.95F, 9F, 6.4F, 5.9F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_sobreveste = p_torso.addOrReplaceChild("sobreveste", CubeListBuilder.create().texOffs(60, 22).addBox(-3F, 0F, -3.15F, 6F, 15.5F, 0.3F).texOffs(74, 22).addBox(-3F, 0F, 2.85F, 6F, 15.5F, 0.3F), PartPose.offset(0F, -11.6F, 0F));
        PartDefinition p_escudo = p_mano_i.addOrReplaceChild("escudo", CubeListBuilder.create().texOffs(30, 0).addBox(-5F, 0F, -8.5F, 10F, 1.2F, 17F).texOffs(80, 86).addBox(-1F, -1.2F, -1.2F, 2F, 1.2F, 2.4F), PartPose.offset(-1.2F, 0.4F, 0F));
        PartDefinition p_espada = p_mano_d.addOrReplaceChild("espada", CubeListBuilder.create().texOffs(0, 22).addBox(-0.55F, -0.9F, -17F, 1.1F, 1.8F, 14.8F).texOffs(106, 86).addBox(-2.8F, -0.7F, -2.6F, 5.6F, 1.4F, 1F).texOffs(32, 86).addBox(-0.7F, -0.7F, -1.6F, 1.4F, 1.4F, 3.4F).texOffs(90, 86).addBox(-1F, -1F, 1.8F, 2F, 2F, 1.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_trapo = p_cabeza.addOrReplaceChild("trapo", CubeListBuilder.create().texOffs(74, 39).addBox(-3.8F, -4.4F, -3.8F, 7.6F, 3.2F, 7.6F).texOffs(20, 55).addBox(-3.8F, -8.4F, -3.8F, 7.6F, 1.6F, 7.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_delantal = p_torso.addOrReplaceChild("delantal", CubeListBuilder.create().texOffs(0, 39).addBox(-3.4F, 0F, -2.85F, 6.8F, 15F, 0.4F), PartPose.offset(0F, -9.5F, 0F));
        PartDefinition p_cinturon = p_torso.addOrReplaceChild("cinturon", CubeListBuilder.create().texOffs(70, 77).addBox(-4.3F, -0.4F, -2.8F, 8.6F, 1.4F, 5.6F).texOffs(44, 86).addBox(-5.6F, 0.8F, -1.4F, 2.2F, 2.8F, 2.6F).texOffs(56, 86).addBox(3.4F, 0.8F, -1.4F, 2.2F, 2.8F, 2.6F).texOffs(68, 86).addBox(-1.3F, 0.8F, 2.6F, 2.6F, 2.8F, 2.2F), PartPose.offset(0F, -1.6F, 0F));
        PartDefinition p_cantaro = p_mano_d.addOrReplaceChild("cantaro", CubeListBuilder.create().texOffs(84, 55).addBox(-2.4F, -2.4F, -6.4F, 4.8F, 4.8F, 4.8F).texOffs(98, 86).addBox(-1.2F, -1.2F, -7.4F, 2.4F, 2.4F, 1F).texOffs(120, 86).addBox(-0.4F, -0.4F, -9.2F, 0.8F, 0.8F, 1.8F), PartPose.offset(0F, 1.2F, 0F));
        PartDefinition p_llama = p_cantaro.addOrReplaceChild("llama", CubeListBuilder.create().texOffs(100, 77).addBox(-1.4F, -1.4F, -3.6F, 2.8F, 2.8F, 3.6F), PartPose.offset(0F, 0F, -9F));
        PartDefinition p_harapos = p_torso.addOrReplaceChild("harapos", CubeListBuilder.create().texOffs(0, 0).addBox(-4.3F, -12.2F, -2.8F, 8.6F, 15.5F, 5.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ceniza_cabeza = p_cabeza.addOrReplaceChild("ceniza_cabeza", CubeListBuilder.create().texOffs(52, 55).addBox(-3.8F, -0.9F, -3.8F, 7.6F, 1.6F, 7.6F), PartPose.offset(0F, -8F, 0F));
        PartDefinition p_ceniza_d = p_brazo_d.addOrReplaceChild("ceniza_d", CubeListBuilder.create().texOffs(0, 86).addBox(-1.9F, -2.6F, -1.9F, 3.8F, 1.4F, 3.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ceniza_i = p_brazo_i.addOrReplaceChild("ceniza_i", CubeListBuilder.create().texOffs(16, 86).addBox(-1.9F, -2.6F, -1.9F, 3.8F, 1.4F, 3.8F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"trapo", "delantal", "cinturon", "cantaro", "llama", "harapos", "ceniza_cabeza", "ceniza_d", "ceniza_i"};

    public static final String[] OCULTAS_1 = {"yelmo", "hombrera_d", "hombrera_i", "guantelete_d", "guantelete_i", "greba_d", "greba_i", "peto", "faldar", "sobreveste", "escudo", "espada", "harapos", "ceniza_cabeza", "ceniza_d", "ceniza_i"};

    public static final String[] OCULTAS_2 = {"yelmo", "hombrera_d", "hombrera_i", "guantelete_d", "guantelete_i", "greba_d", "greba_i", "peto", "faldar", "sobreveste", "escudo", "espada", "trapo", "delantal", "cinturon", "cantaro", "llama"};
}
