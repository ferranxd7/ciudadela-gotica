package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class InsomneGeometria {
    private InsomneGeometria() {}

    public static final int TEX_W = 128, TEX_H = 256;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(20, 110).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(36, 110).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(52, 110).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(68, 110).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(34, 63).addBox(-4F, -12F, -2.5F, 8F, 12F, 5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(0, 80).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(84, 110).addBox(-1.5F, -2F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(-5.1F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(96, 110).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(108, 110).addBox(-1.5F, -2F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(5.1F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(0, 120).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_faldon_d = p_muslo_d.addOrReplaceChild("faldon_d", CubeListBuilder.create().texOffs(40, 96).addBox(-2.35F, -0.5F, -2.5F, 4.7F, 6.4F, 5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon_i = p_muslo_i.addOrReplaceChild("faldon_i", CubeListBuilder.create().texOffs(60, 96).addBox(-2.35F, -0.5F, -2.5F, 4.7F, 6.4F, 5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cinto = p_torso.addOrReplaceChild("cinto", CubeListBuilder.create().texOffs(48, 130).addBox(-4.35F, -2.3F, -2.85F, 8.7F, 1.5F, 5.7F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_capacete = p_cabeza.addOrReplaceChild("capacete", CubeListBuilder.create().texOffs(64, 80).addBox(-6.2F, -7.6F, -6.2F, 12.4F, 0.8F, 12.4F).texOffs(0, 96).addBox(-4.6F, -10.8F, -4.6F, 9.2F, 3.6F, 9.2F).texOffs(80, 96).addBox(-0.5F, -11.5F, -4.7F, 1F, 1F, 9.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_almofar = p_cabeza.addOrReplaceChild("almofar", CubeListBuilder.create().texOffs(60, 63).addBox(-4.5F, -7.2F, -4F, 0.6F, 7.6F, 8.5F).texOffs(80, 63).addBox(3.9F, -7.2F, -4F, 0.6F, 7.6F, 8.5F).texOffs(46, 120).addBox(-4.5F, -7.2F, 3.9F, 9F, 7.6F, 0.6F).texOffs(56, 139).addBox(-4.5F, -1.2F, -4.5F, 9F, 1.8F, 1.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lanza = p_mano_d.addOrReplaceChild("lanza", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6F, -0.6F, -26F, 1.2F, 1.2F, 38F).texOffs(94, 130).addBox(-1F, -0.35F, -31.6F, 2F, 0.7F, 5.6F).texOffs(40, 144).addBox(-0.8F, -0.8F, -26.6F, 1.6F, 1.6F, 1F).texOffs(16, 130).addBox(-0.12F, 0.6F, -25.6F, 0.24F, 3.4F, 4.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cuerno = p_torso.addOrReplaceChild("cuerno", CubeListBuilder.create().texOffs(78, 139).addBox(4.35F, -2.9F, -2.9F, 1.1F, 1.1F, 1.7F).texOffs(18, 139).addBox(4.15F, -3.15F, -1.3F, 1.5F, 1.6F, 2.1F).texOffs(48, 139).addBox(3.9F, -3.45F, 0.8F, 2F, 2.2F, 1.7F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cuerno_mano = p_mano_i.addOrReplaceChild("cuerno_mano", CubeListBuilder.create().texOffs(46, 144).addBox(-0.5F, -0.5F, -1.2F, 1F, 1F, 2F).texOffs(28, 139).addBox(-0.8F, -0.8F, -3.4F, 1.6F, 1.6F, 2.2F).texOffs(38, 139).addBox(-1.2F, -1.2F, -5.4F, 2.4F, 2.4F, 2F), PartPose.offset(0F, 0.4F, 0F));
        PartDefinition p_ballesta = p_mano_d.addOrReplaceChild("ballesta", CubeListBuilder.create().texOffs(0, 40).addBox(-0.9F, -0.8F, -14F, 1.8F, 2.2F, 20F).texOffs(0, 144).addBox(-9F, -0.5F, -14.6F, 18F, 1F, 1.4F).texOffs(64, 144).addBox(-8.4F, -0.15F, -7.4F, 16.8F, 0.3F, 0.3F).texOffs(86, 139).addBox(-1.5F, -0.9F, -16.6F, 3F, 2.6F, 0.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_virote_armado = p_ballesta.addOrReplaceChild("virote_armado", CubeListBuilder.create().texOffs(0, 110).addBox(-0.3F, -1.5F, -15.2F, 0.6F, 0.7F, 8.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_paves = p_torso.addOrReplaceChild("paves", CubeListBuilder.create().texOffs(84, 40).addBox(-5.2F, -1.6F, 0F, 10.4F, 15.4F, 1.3F), PartPose.offsetAndRotation(0F, -12F, 2.6F, -0.1047F, 0F, 0F));
        PartDefinition p_carcaj = p_torso.addOrReplaceChild("carcaj", CubeListBuilder.create().texOffs(28, 130).addBox(-6F, -7F, 0.4F, 2F, 5.4F, 2.2F).texOffs(94, 139).addBox(-5.8F, -8.4F, 0.6F, 1.6F, 1.4F, 1.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_torno = p_torso.addOrReplaceChild("torno", CubeListBuilder.create().texOffs(110, 130).addBox(3.9F, -4.6F, -1.4F, 1.8F, 2.6F, 2.8F).texOffs(52, 144).addBox(5.6F, -4F, -0.4F, 0.8F, 0.8F, 1.9F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bacinete = p_cabeza.addOrReplaceChild("bacinete", CubeListBuilder.create().texOffs(44, 40).addBox(-4.6F, -8.8F, -4.6F, 9.2F, 9.4F, 9.2F).texOffs(102, 96).addBox(-0.45F, -9.5F, -4.8F, 0.9F, 1F, 9.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_peto = p_torso.addOrReplaceChild("peto", CubeListBuilder.create().texOffs(32, 80).addBox(-4.5F, -12.3F, -3.05F, 9F, 8.6F, 6.1F).texOffs(12, 120).addBox(-4.6F, -4.1F, -3.1F, 9.2F, 2F, 6.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hombrera_d = p_brazo_d.addOrReplaceChild("hombrera_d", CubeListBuilder.create().texOffs(66, 120).addBox(-2.45F, -2.6F, -2.45F, 4.9F, 3.6F, 4.9F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_brazal_d = p_antebrazo_d.addOrReplaceChild("brazal_d", CubeListBuilder.create().texOffs(106, 120).addBox(-1.85F, 0.6F, -1.85F, 3.7F, 4.6F, 3.7F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hombrera_i = p_brazo_i.addOrReplaceChild("hombrera_i", CubeListBuilder.create().texOffs(86, 120).addBox(-2.45F, -2.6F, -2.45F, 4.9F, 3.6F, 4.9F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_brazal_i = p_antebrazo_i.addOrReplaceChild("brazal_i", CubeListBuilder.create().texOffs(0, 130).addBox(-1.85F, 0.6F, -1.85F, 3.7F, 4.6F, 3.7F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_escudo = p_antebrazo_i.addOrReplaceChild("escudo", CubeListBuilder.create().texOffs(80, 0).addBox(-5.6F, -9.5F, -3.2F, 12F, 22F, 1.4F).texOffs(102, 139).addBox(-0.7F, -1.5F, -1.9F, 1.4F, 3F, 0.5F), PartPose.offset(0F, 3F, 0F));
        PartDefinition p_maza = p_mano_d.addOrReplaceChild("maza", CubeListBuilder.create().texOffs(0, 63).addBox(-0.6F, -0.6F, -11.6F, 1.2F, 1.2F, 14.6F).texOffs(78, 130).addBox(-1.5F, -1.5F, -15.6F, 3F, 3F, 4.2F).texOffs(0, 139).addBox(-2.5F, -0.45F, -15.3F, 5F, 0.9F, 3.6F).texOffs(38, 130).addBox(-0.45F, -2.5F, -15.3F, 0.9F, 5F, 3.6F).texOffs(58, 144).addBox(-0.4F, -0.4F, -17F, 0.8F, 0.8F, 1.5F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"ballesta", "virote_armado", "paves", "carcaj", "torno", "bacinete", "peto", "hombrera_d", "hombrera_i", "brazal_d", "brazal_i", "escudo", "maza", "cuerno_mano"};

    public static final String[] OCULTAS_1 = {"lanza", "cuerno", "cuerno_mano", "bacinete", "peto", "hombrera_d", "hombrera_i", "brazal_d", "brazal_i", "escudo", "maza"};

    public static final String[] OCULTAS_2 = {"lanza", "cuerno", "cuerno_mano", "ballesta", "virote_armado", "paves", "carcaj", "torno", "capacete", "almofar"};
}
