package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class JineteGeometria {
    private JineteGeometria() {}

    public static final int TEX_W = 128, TEX_H = 256;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_c_cadera = p_raiz.addOrReplaceChild("c_cadera", CubeListBuilder.create().texOffs(0, 81).addBox(-5F, -3F, -2.5F, 10F, 6.5F, 8.5F).texOffs(0, 63).addBox(-5.6F, -3.4F, -2.2F, 11.2F, 8.2F, 8.6F, new CubeDeformation(0.12F, 0.12F, 0.12F)), PartPose.offset(0F, -21F, 10F));
        PartDefinition p_c_torso = p_c_cadera.addOrReplaceChild("c_torso", CubeListBuilder.create().texOffs(66, 0).addBox(-5.5F, -3F, -20F, 11F, 11.5F, 20F).texOffs(0, 0).addBox(-6.1F, -3.3F, -19.4F, 12.2F, 12.6F, 19.2F, new CubeDeformation(0.1F, 0.1F, 0.1F)).texOffs(12, 146).addBox(-4.9F, -4.8F, -14.5F, 9.8F, 2F, 8F).texOffs(66, 180).addBox(-1.2F, -7F, -15F, 2.4F, 2.4F, 1.8F).texOffs(0, 186).addBox(-3.4F, -6.3F, -7.6F, 6.8F, 1.8F, 1.4F).texOffs(0, 111).addBox(-5.9F, -1.8F, -21.4F, 11.8F, 9.5F, 2F), PartPose.offset(0F, -0.5F, -2.5F));
        PartDefinition p_c_cuello = p_c_torso.addOrReplaceChild("c_cuello", CubeListBuilder.create().texOffs(40, 33).addBox(-2.7F, -2.6F, -12F, 5.4F, 5.8F, 13F).texOffs(50, 81).addBox(-3F, -3F, -11.4F, 6F, 2.6F, 11.6F, new CubeDeformation(0.08F, 0.08F, 0.08F)).texOffs(78, 33).addBox(-0.8F, -7F, -12.5F, 1.6F, 4.6F, 13.5F), PartPose.offsetAndRotation(0F, -1.2F, -19.2F, -0.8727F, 0F, 0F));
        PartDefinition p_c_cabeza = p_c_cuello.addOrReplaceChild("c_cabeza", CubeListBuilder.create().texOffs(64, 97).addBox(-2.9F, -3.2F, -6F, 5.8F, 6F, 6.5F).texOffs(90, 97).addBox(-2.2F, -2.5F, -13.2F, 4.4F, 4.4F, 7.4F).texOffs(32, 97).addBox(-2.5F, -3.7F, -12.8F, 5F, 1.6F, 10.4F).texOffs(76, 180).addBox(-0.55F, -6F, -7F, 1.1F, 2.6F, 1.1F).texOffs(84, 180).addBox(-2.4F, -5.4F, -1.8F, 1.2F, 2.6F, 1.1F).texOffs(92, 180).addBox(1.2F, -5.4F, -1.8F, 1.2F, 2.6F, 1.1F).texOffs(18, 186).addBox(-2.3F, -1F, -10.2F, 4.6F, 1.2F, 1.2F, new CubeDeformation(0.05F, 0.05F, 0.05F)).texOffs(44, 166).addBox(-0.7F, -5.8F, -3.2F, 1.4F, 3.4F, 3.6F), PartPose.offsetAndRotation(0F, 0.2F, -11.5F, 1.6057F, 0F, 0F));
        PartDefinition p_c_mandibula = p_c_cabeza.addOrReplaceChild("c_mandibula", CubeListBuilder.create().texOffs(48, 146).addBox(-1.7F, 0F, -7.6F, 3.4F, 1.4F, 8F), PartPose.offsetAndRotation(0F, 1.7F, -5.2F, 0.0698F, 0F, 0F));
        PartDefinition p_c_brazo_d = p_c_torso.addOrReplaceChild("c_brazo_d", CubeListBuilder.create().texOffs(0, 97).addBox(-1.8F, -2.5F, -1.9F, 3.6F, 9.5F, 3.8F), PartPose.offset(-3.8F, 6F, -17F));
        PartDefinition p_c_antebrazo_d = p_c_brazo_d.addOrReplaceChild("c_antebrazo_d", CubeListBuilder.create().texOffs(72, 157).addBox(-1.2F, 0F, -1.2F, 2.4F, 6F, 2.4F), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_c_mano_d = p_c_antebrazo_d.addOrReplaceChild("c_mano_d", CubeListBuilder.create().texOffs(18, 180).addBox(-1.5F, -0.4F, -1.5F, 3F, 1.8F, 3F).texOffs(0, 174).addBox(-1.8F, 1.4F, -2F, 3.6F, 1.4F, 3.8F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_c_brazo_i = p_c_torso.addOrReplaceChild("c_brazo_i", CubeListBuilder.create().texOffs(16, 97).addBox(-1.8F, -2.5F, -1.9F, 3.6F, 9.5F, 3.8F), PartPose.offset(3.8F, 6F, -17F));
        PartDefinition p_c_antebrazo_i = p_c_brazo_i.addOrReplaceChild("c_antebrazo_i", CubeListBuilder.create().texOffs(84, 157).addBox(-1.2F, 0F, -1.2F, 2.4F, 6F, 2.4F), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_c_mano_i = p_c_antebrazo_i.addOrReplaceChild("c_mano_i", CubeListBuilder.create().texOffs(30, 180).addBox(-1.5F, -0.4F, -1.5F, 3F, 1.8F, 3F).texOffs(16, 174).addBox(-1.8F, 1.4F, -2F, 3.6F, 1.4F, 3.8F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_c_muslo_d = p_c_cadera.addOrReplaceChild("c_muslo_d", CubeListBuilder.create().texOffs(86, 81).addBox(-2F, -2.5F, -2.2F, 4F, 9.5F, 4.4F), PartPose.offsetAndRotation(-3.9F, 2F, 2.6F, -0.3491F, 0F, 0F));
        PartDefinition p_c_pierna_d = p_c_muslo_d.addOrReplaceChild("c_pierna_d", CubeListBuilder.create().texOffs(108, 135).addBox(-1.2F, 0F, -1.2F, 2.4F, 7.5F, 2.4F), PartPose.offsetAndRotation(0F, 7F, 0F, 0.7854F, 0F, 0F));
        PartDefinition p_c_pata_d = p_c_pierna_d.addOrReplaceChild("c_pata_d", CubeListBuilder.create().texOffs(100, 180).addBox(-1F, 0F, -1F, 2F, 2.4F, 2F).texOffs(42, 180).addBox(-1.5F, 2F, -1.5F, 3F, 1.8F, 3F).texOffs(32, 174).addBox(-1.8F, 3.6F, -2F, 3.6F, 1.4F, 3.8F), PartPose.offsetAndRotation(0F, 7.5F, 0F, -0.4363F, 0F, 0F));
        PartDefinition p_c_muslo_i = p_c_cadera.addOrReplaceChild("c_muslo_i", CubeListBuilder.create().texOffs(104, 81).addBox(-2F, -2.5F, -2.2F, 4F, 9.5F, 4.4F), PartPose.offsetAndRotation(3.9F, 2F, 2.6F, -0.3491F, 0F, 0F));
        PartDefinition p_c_pierna_i = p_c_muslo_i.addOrReplaceChild("c_pierna_i", CubeListBuilder.create().texOffs(0, 146).addBox(-1.2F, 0F, -1.2F, 2.4F, 7.5F, 2.4F), PartPose.offsetAndRotation(0F, 7F, 0F, 0.7854F, 0F, 0F));
        PartDefinition p_c_pata_i = p_c_pierna_i.addOrReplaceChild("c_pata_i", CubeListBuilder.create().texOffs(108, 180).addBox(-1F, 0F, -1F, 2F, 2.4F, 2F).texOffs(42, 180).addBox(-1.5F, 2F, -1.5F, 3F, 1.8F, 3F).texOffs(32, 174).addBox(-1.8F, 3.6F, -2F, 3.6F, 1.4F, 3.8F), PartPose.offsetAndRotation(0F, 7.5F, 0F, -0.4363F, 0F, 0F));
        PartDefinition p_c_cola = p_c_cadera.addOrReplaceChild("c_cola", CubeListBuilder.create().texOffs(96, 174).addBox(-0.9F, -0.9F, 0F, 1.8F, 1.8F, 4F).texOffs(24, 135).addBox(-1.7F, -1.7F, 2.5F, 3.4F, 3.4F, 6.5F), PartPose.offsetAndRotation(0F, -2.5F, 5.8F, -0.6109F, 0F, 0F));
        PartDefinition p_c_cola2 = p_c_cola.addOrReplaceChild("c_cola2", CubeListBuilder.create().texOffs(46, 135).addBox(-1.4F, -1.4F, 0F, 2.8F, 2.8F, 7.5F), PartPose.offsetAndRotation(0F, 0F, 8.5F, -0.4363F, 0F, 0F));
        PartDefinition p_cadera = p_c_torso.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -4.7F, -10.6F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(50, 111).addBox(-2.3F, 0F, -2.3F, 4.6F, 6.5F, 4.6F), PartPose.offsetAndRotation(-2.4F, -0.4F, 0F, -0.5236F, 0F, 1.0123F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(70, 111).addBox(-2.3F, 0F, -2.3F, 4.6F, 6.5F, 4.6F).texOffs(0, 166).addBox(-2.5F, 4.6F, -3.4F, 5F, 2F, 5.6F), PartPose.offsetAndRotation(0F, 6.5F, 0F, 0.9599F, 0F, -0.7854F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(90, 111).addBox(-2.3F, 0F, -2.3F, 4.6F, 6.5F, 4.6F), PartPose.offsetAndRotation(2.4F, -0.4F, 0F, -0.5236F, 0F, -1.0123F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(0, 123).addBox(-2.3F, 0F, -2.3F, 4.6F, 6.5F, 4.6F).texOffs(22, 166).addBox(-2.5F, 4.6F, -3.4F, 5F, 2F, 5.6F), PartPose.offsetAndRotation(0F, 6.5F, 0F, 0.9599F, 0F, 0.7854F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(74, 63).addBox(-4.5F, -12F, -2.5F, 9F, 12F, 5F).texOffs(8, 33).addBox(-4.8F, -11.4F, -2.8F, 9.6F, 13.4F, 5.6F, new CubeDeformation(0.12F, 0.12F, 0.12F)).texOffs(96, 157).addBox(-5F, -2.2F, -3F, 10F, 1.6F, 6F, new CubeDeformation(0.2F, 0.2F, 0.2F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gola = p_torso.addOrReplaceChild("gola", CubeListBuilder.create().texOffs(96, 146).addBox(-3.6F, -13.6F, -2.8F, 7.2F, 2.2F, 5.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(42, 63).addBox(-4F, -8.6F, -4F, 8F, 8.6F, 8F).texOffs(60, 123).addBox(-4.4F, -9.8F, -4.4F, 8.8F, 1.6F, 8.8F, new CubeDeformation(0.1F, 0.1F, 0.1F)), PartPose.offset(0F, -12.4F, 0F));
        PartDefinition p_penacho = p_cabeza.addOrReplaceChild("penacho", CubeListBuilder.create().texOffs(28, 111).addBox(-0.9F, -3F, 0F, 1.8F, 3F, 9F), PartPose.offsetAndRotation(0F, -9.6F, 1F, 0.5236F, 0F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(20, 123).addBox(-2.2F, -2F, -2.2F, 4.4F, 6.5F, 4.4F), PartPose.offset(-6.6F, -10F, 0F));
        PartDefinition p_hombrera_d = p_brazo_d.addOrReplaceChild("hombrera_d", CubeListBuilder.create().texOffs(96, 123).addBox(-3F, -3.4F, -3F, 6F, 4.2F, 6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(68, 135).addBox(-2.2F, 0F, -2.2F, 4.4F, 6F, 4.4F).texOffs(0, 157).addBox(-2.6F, 2.6F, -2.6F, 5.2F, 2.2F, 5.2F), PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(40, 123).addBox(-2.2F, -2F, -2.2F, 4.4F, 6.5F, 4.4F), PartPose.offset(6.6F, -10F, 0F));
        PartDefinition p_hombrera_i = p_brazo_i.addOrReplaceChild("hombrera_i", CubeListBuilder.create().texOffs(0, 135).addBox(-3F, -3.4F, -3F, 6F, 4.2F, 6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(88, 135).addBox(-2.2F, 0F, -2.2F, 4.4F, 6F, 4.4F).texOffs(24, 157).addBox(-2.6F, 2.6F, -2.6F, 5.2F, 2.2F, 5.2F), PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_capa = p_torso.addOrReplaceChild("capa", CubeListBuilder.create().texOffs(72, 146).addBox(-5.2F, 0F, 0F, 10.4F, 9F, 1F), PartPose.offsetAndRotation(0F, -11.6F, 2.7F, 0.1396F, 0F, 0F));
        PartDefinition p_capa2 = p_capa.addOrReplaceChild("capa2", CubeListBuilder.create().texOffs(48, 157).addBox(-5.2F, 0F, -0.2F, 10.4F, 8F, 1F), PartPose.offsetAndRotation(0F, 9F, 0.2F, 0.1047F, 0F, 0F));
        PartDefinition p_tubo = p_torso.addOrReplaceChild("tubo", CubeListBuilder.create().texOffs(38, 81).addBox(-1.3F, -6.5F, -1.3F, 2.6F, 13F, 2.6F).texOffs(48, 174).addBox(-1.6F, -7.4F, -1.6F, 3.2F, 1.6F, 3.2F).texOffs(64, 174).addBox(-1.6F, 5.8F, -1.6F, 3.2F, 1.6F, 3.2F).texOffs(32, 186).addBox(-0.8F, -1F, 1.2F, 1.6F, 1.6F, 0.8F), PartPose.offsetAndRotation(0F, -6.5F, 4.6F, 0F, 0F, 0.6632F));
        PartDefinition p_cuerno_cinto = p_torso.addOrReplaceChild("cuerno_cinto", CubeListBuilder.create().texOffs(10, 180).addBox(-0.9F, 0F, -0.9F, 1.8F, 3.6F, 1.8F).texOffs(72, 166).addBox(-1.3F, 3.2F, -1.6F, 2.6F, 2.6F, 3.2F).texOffs(80, 174).addBox(-1.6F, 5.2F, -2.6F, 3.2F, 1.6F, 3.2F), PartPose.offsetAndRotation(5.2F, -1.6F, 0.6F, 0F, 0F, -0.3491F));
        PartDefinition p_cuerno_mano = p_mano_i.addOrReplaceChild("cuerno_mano", CubeListBuilder.create().texOffs(108, 166).addBox(-0.9F, -4F, -1.7F, 1.8F, 5F, 1.8F).texOffs(86, 166).addBox(-1.3F, -6.6F, -2.1F, 2.6F, 3.2F, 2.6F).texOffs(56, 166).addBox(-1.6F, -8.4F, -1.2F, 3.2F, 2.2F, 3.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        PartDefinition p_guja = p_mano_d.addOrReplaceChild("guja", CubeListBuilder.create().texOffs(0, 33).addBox(-1F, -20F, -1.8F, 2F, 28F, 2F).texOffs(108, 174).addBox(-1.3F, 7.5F, -2.1F, 2.6F, 2.2F, 2.6F).texOffs(54, 180).addBox(-1.4F, -21.6F, -2.2F, 2.8F, 2F, 2.8F).texOffs(102, 63).addBox(-0.6F, -32.5F, -2F, 1.2F, 12F, 5F).texOffs(98, 166).addBox(-0.6F, -35.5F, 0F, 1.2F, 3.4F, 2.6F).texOffs(0, 180).addBox(-0.6F, -25.5F, -4.2F, 1.2F, 2.2F, 2.4F), PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"cuerno_mano"};
}
