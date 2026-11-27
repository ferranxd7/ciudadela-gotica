package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class MarcadoGeometria {
    private MarcadoGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(24, 91).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(40, 91).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(56, 91).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(72, 91).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(64, 47).addBox(-4F, -12F, -2F, 8F, 12F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(94, 26).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(88, 91).addBox(-2F, -2F, -2F, 4F, 6F, 4F), PartPose.offset(-6F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(104, 91).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(0, 101).addBox(-2F, -2F, -2F, 4F, 6F, 4F), PartPose.offset(6F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(16, 101).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_grillete_d = p_antebrazo_d.addOrReplaceChild("grillete_d", CubeListBuilder.create().texOffs(78, 101).addBox(-2.45F, 3.4F, -2.45F, 4.9F, 1.8F, 4.9F).texOffs(92, 118).addBox(-0.5F, 5.2F, -2.9F, 1F, 2.4F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_grillete_i = p_antebrazo_i.addOrReplaceChild("grillete_i", CubeListBuilder.create().texOffs(98, 101).addBox(-2.45F, 3.4F, -2.45F, 4.9F, 1.8F, 4.9F).texOffs(96, 118).addBox(-0.5F, 5.2F, -2.9F, 1F, 2.4F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_barba = p_cabeza.addOrReplaceChild("barba", CubeListBuilder.create().texOffs(16, 118).addBox(-3.6F, -3F, -4.65F, 7.2F, 3F, 1F).texOffs(64, 111).addBox(-2.6F, 0F, -4.4F, 5.2F, 3.2F, 1.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_peto = p_torso.addOrReplaceChild("peto", CubeListBuilder.create().texOffs(46, 111).addBox(-3.6F, -5F, -2.55F, 7.2F, 5F, 0.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_delantal = p_cadera.addOrReplaceChild("delantal", CubeListBuilder.create().texOffs(44, 79).addBox(-4F, -0.4F, -2.75F, 8F, 10.5F, 0.6F), PartPose.offsetAndRotation(0F, 0F, 0F, -0.0524F, 0F, 0F));
        PartDefinition p_cinto = p_torso.addOrReplaceChild("cinto", CubeListBuilder.create().texOffs(46, 101).addBox(-4.55F, -1.6F, -2.55F, 9.1F, 1.6F, 5.1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_mazo = p_mano_d.addOrReplaceChild("mazo", CubeListBuilder.create().texOffs(52, 0).addBox(-0.75F, -0.75F, -19F, 1.5F, 1.5F, 23F).texOffs(48, 63).addBox(-2.8F, -4.4F, -24.4F, 5.6F, 8.8F, 5.6F).texOffs(34, 118).addBox(-3.1F, -4.7F, -22.4F, 6.2F, 1.2F, 1.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_yelmo = p_cabeza.addOrReplaceChild("yelmo", CubeListBuilder.create().texOffs(0, 47).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.6F, 0.6F, 0.6F)).texOffs(116, 111).addBox(-0.6F, -12.2F, -0.6F, 1.2F, 3.6F, 1.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_espina_d = p_yelmo.addOrReplaceChild("espina_d", CubeListBuilder.create().texOffs(100, 118).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F), PartPose.offsetAndRotation(-2.6F, -8.4F, 0.4F, 0F, 0F, 0.4538F));
        PartDefinition p_espina_i = p_yelmo.addOrReplaceChild("espina_i", CubeListBuilder.create().texOffs(104, 118).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F), PartPose.offsetAndRotation(2.6F, -8.4F, 0.4F, 0F, 0F, -0.4538F));
        PartDefinition p_coraza = p_torso.addOrReplaceChild("coraza", CubeListBuilder.create().texOffs(88, 47).addBox(-4F, -12F, -2F, 8F, 12F, 4F, new CubeDeformation(0.5F, 0.5F, 0.5F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hombrera_d = p_brazo_d.addOrReplaceChild("hombrera_d", CubeListBuilder.create().texOffs(86, 79).addBox(-2.8F, -3.2F, -2.7F, 5.6F, 3.6F, 5.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hombrera_i = p_brazo_i.addOrReplaceChild("hombrera_i", CubeListBuilder.create().texOffs(0, 91).addBox(-2.8F, -3.2F, -2.7F, 5.6F, 3.6F, 5.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_capa = p_torso.addOrReplaceChild("capa", CubeListBuilder.create().texOffs(38, 26).addBox(-4.6F, 0F, 0F, 9.2F, 19F, 0.6F), PartPose.offsetAndRotation(0F, -11.6F, 2.7F, 0.0873F, 0F, 0F));
        PartDefinition p_faldon = p_cadera.addOrReplaceChild("faldon", CubeListBuilder.create().texOffs(62, 79).addBox(-4F, -0.5F, -2F, 8F, 7F, 4F, new CubeDeformation(0.42F, 0.42F, 0.42F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_espada = p_mano_d.addOrReplaceChild("espada", CubeListBuilder.create().texOffs(80, 111).addBox(-0.5F, -0.5F, -2.5F, 1F, 1F, 5F).texOffs(70, 118).addBox(-1F, -1F, 2.5F, 2F, 2F, 1.5F).texOffs(52, 118).addBox(-3.2F, -0.6F, -3.6F, 6.4F, 1.2F, 1.1F).texOffs(0, 26).addBox(-0.5F, -1.1F, -21.5F, 1F, 2.2F, 18F).texOffs(86, 118).addBox(-0.5F, -0.6F, -23.5F, 1F, 1.2F, 2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_escudo = p_antebrazo_i.addOrReplaceChild("escudo", CubeListBuilder.create().texOffs(102, 0).addBox(2.3F, -4F, -5F, 1F, 12F, 10F).texOffs(32, 101).addBox(2.3F, 8F, -3F, 1F, 2.5F, 6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_capucha = p_cabeza.addOrReplaceChild("capucha", CubeListBuilder.create().texOffs(32, 47).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.55F, 0.55F, 0.55F)).texOffs(14, 111).addBox(-2F, -7.2F, 4.5F, 4F, 5F, 1.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_embozo = p_cabeza.addOrReplaceChild("embozo", CubeListBuilder.create().texOffs(26, 111).addBox(-4.3F, -4F, -4.6F, 8.6F, 4.4F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gaban = p_torso.addOrReplaceChild("gaban", CubeListBuilder.create().texOffs(0, 63).addBox(-4F, -12F, -2F, 8F, 12F, 4F, new CubeDeformation(0.42F, 0.42F, 0.42F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon_tras = p_cadera.addOrReplaceChild("faldon_tras", CubeListBuilder.create().texOffs(24, 79).addBox(-4.4F, -0.5F, 1.7F, 8.8F, 11F, 0.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon_d = p_muslo_d.addOrReplaceChild("faldon_d", CubeListBuilder.create().texOffs(24, 63).addBox(-2.75F, -0.5F, -2.4F, 0.9F, 10.4F, 4.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon_i = p_muslo_i.addOrReplaceChild("faldon_i", CubeListBuilder.create().texOffs(36, 63).addBox(1.85F, -0.5F, -2.4F, 0.9F, 10.4F, 4.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aljaba = p_torso.addOrReplaceChild("aljaba", CubeListBuilder.create().texOffs(72, 63).addBox(-1.3F, 0F, 0F, 2.6F, 10F, 2.4F).texOffs(0, 118).addBox(-1.1F, -2.6F, 0.2F, 2.2F, 2.6F, 2F), PartPose.offsetAndRotation(0F, -10.5F, 2.6F, 0F, 0F, 0.4189F));
        PartDefinition p_arco = p_mano_i.addOrReplaceChild("arco", CubeListBuilder.create().texOffs(0, 111).addBox(-0.8F, -0.8F, -2.2F, 1.6F, 1.6F, 4.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_pala_a = p_arco.addOrReplaceChild("pala_a", CubeListBuilder.create().texOffs(84, 63).addBox(-0.65F, -0.65F, -9F, 1.3F, 1.3F, 9.2F), PartPose.offsetAndRotation(0F, 0F, -2F, 0.192F, 0F, 0F));
        PartDefinition p_pala_b = p_arco.addOrReplaceChild("pala_b", CubeListBuilder.create().texOffs(0, 79).addBox(-0.65F, -0.65F, -0.2F, 1.3F, 1.3F, 9.2F), PartPose.offsetAndRotation(0F, 0F, 2F, -0.192F, 0F, 0F));
        PartDefinition p_pala_curva_a = p_pala_a.addOrReplaceChild("pala_curva_a", CubeListBuilder.create().texOffs(92, 111).addBox(-0.55F, -0.55F, -3.2F, 1.1F, 1.1F, 3.4F), PartPose.offsetAndRotation(0F, 0F, -9F, -0.5236F, 0F, 0F));
        PartDefinition p_pala_curva_b = p_pala_b.addOrReplaceChild("pala_curva_b", CubeListBuilder.create().texOffs(104, 111).addBox(-0.55F, -0.55F, -0.2F, 1.1F, 1.1F, 3.4F), PartPose.offsetAndRotation(0F, 0F, 9F, 0.5236F, 0F, 0F));
        PartDefinition p_cuerda = p_arco.addOrReplaceChild("cuerda", CubeListBuilder.create().texOffs(0, 0).addBox(-0.15F, 1.45F, -12.4F, 0.3F, 0.3F, 24.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_flecha = p_mano_d.addOrReplaceChild("flecha", CubeListBuilder.create().texOffs(60, 26).addBox(-0.3F, -0.3F, -12.5F, 0.6F, 0.6F, 16F).texOffs(78, 118).addBox(-0.6F, -0.6F, -14F, 1.2F, 1.2F, 1.6F).texOffs(10, 118).addBox(-0.1F, -1.1F, 1.6F, 0.2F, 2.2F, 2F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"yelmo", "espina_d", "espina_i", "coraza", "hombrera_d", "hombrera_i", "capa", "faldon", "espada", "escudo", "capucha", "embozo", "gaban", "faldon_tras", "faldon_d", "faldon_i", "aljaba", "arco", "pala_a", "pala_b", "pala_curva_a", "pala_curva_b", "cuerda", "flecha"};

    public static final String[] OCULTAS_1 = {"barba", "peto", "delantal", "cinto", "mazo", "capucha", "embozo", "gaban", "faldon_tras", "faldon_d", "faldon_i", "aljaba", "arco", "pala_a", "pala_b", "pala_curva_a", "pala_curva_b", "cuerda", "flecha"};

    public static final String[] OCULTAS_2 = {"barba", "peto", "delantal", "cinto", "mazo", "yelmo", "espina_d", "espina_i", "coraza", "hombrera_d", "hombrera_i", "capa", "faldon", "espada", "escudo"};
}
