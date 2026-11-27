package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class AldricGeometria {
    private AldricGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -13F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(0, 40).addBox(-2.5F, 0F, -2.5F, 5F, 6.5F, 5F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(20, 40).addBox(-2.5F, 0F, -2.5F, 5F, 6.5F, 5F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(40, 40).addBox(-2.5F, 0F, -2.5F, 5F, 6.5F, 5F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(60, 40).addBox(-2.5F, 0F, -2.5F, 5F, 6.5F, 5F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(22, 0).addBox(-4.5F, -13F, -2.5F, 9F, 13F, 5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(78, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -13F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(100, 40).addBox(-2F, -2F, -2F, 4F, 7F, 4F), PartPose.offset(-6.5F, -11F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(0, 52).addBox(-2F, 0F, -2F, 4F, 7F, 4F), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(16, 52).addBox(-2F, -2F, -2F, 4F, 7F, 4F), PartPose.offset(6.5F, -11F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(32, 52).addBox(-2F, 0F, -2F, 4F, 7F, 4F), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_pelo = p_cabeza.addOrReplaceChild("pelo", CubeListBuilder.create().texOffs(0, 24).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.45F, 0.45F, 0.45F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_melena = p_cabeza.addOrReplaceChild("melena", CubeListBuilder.create().texOffs(92, 52).addBox(-4.2F, 0F, 0F, 8.4F, 8.6F, 1F), PartPose.offsetAndRotation(0F, -2.2F, 3.9F, 0.1745F, 0F, 0F));
        PartDefinition p_barba = p_cabeza.addOrReplaceChild("barba", CubeListBuilder.create().texOffs(0, 63).addBox(-3.3F, 0F, -1F, 6.6F, 7.6F, 1.1F), PartPose.offsetAndRotation(0F, -1.4F, -4.1F, -0.1222F, 0F, 0F));
        PartDefinition p_collar = p_torso.addOrReplaceChild("collar", CubeListBuilder.create().texOffs(36, 63).addBox(-4.8F, -14.3F, -2.9F, 9.6F, 2F, 5.8F).texOffs(26, 73).addBox(-0.6F, -12.5F, -3.5F, 1.2F, 3F, 0.7F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_coraza = p_torso.addOrReplaceChild("coraza", CubeListBuilder.create().texOffs(50, 0).addBox(-4.5F, -13F, -2.5F, 9F, 13F, 5F, new CubeDeformation(0.5F, 0.5F, 0.5F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hombrera = p_brazo_i.addOrReplaceChild("hombrera", CubeListBuilder.create().texOffs(86, 24).addBox(-2.6F, -3.7F, -3.1F, 6F, 4.2F, 6.2F).texOffs(64, 52).addBox(-2.8F, -0.7F, -3.3F, 6.4F, 2.2F, 6.6F).texOffs(0, 73).addBox(0.4F, -6.5F, -0.6F, 1.2F, 2.8F, 1.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_brazal = p_antebrazo_i.addOrReplaceChild("brazal", CubeListBuilder.create().texOffs(48, 52).addBox(-2F, 0.4F, -2F, 4F, 6.6F, 4F, new CubeDeformation(0.35F, 0.35F, 0.35F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon = p_cadera.addOrReplaceChild("faldon", CubeListBuilder.create().texOffs(32, 24).addBox(-4.5F, -0.6F, -2.5F, 9F, 7.2F, 5F, new CubeDeformation(0.42F, 0.42F, 0.42F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_greba_d = p_pierna_d.addOrReplaceChild("greba_d", CubeListBuilder.create().texOffs(80, 40).addBox(-2.5F, 0.2F, -2.5F, 5F, 6.3F, 5F, new CubeDeformation(0.32F, 0.32F, 0.32F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_greba_i = p_pierna_i.addOrReplaceChild("greba_i", CubeListBuilder.create().texOffs(80, 40).addBox(-2.5F, 0.2F, -2.5F, 5F, 6.3F, 5F, new CubeDeformation(0.32F, 0.32F, 0.32F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_capa = p_torso.addOrReplaceChild("capa", CubeListBuilder.create().texOffs(0, 0).addBox(-4.8F, 0F, 0F, 9.6F, 23F, 0.7F), PartPose.offsetAndRotation(0F, -12.4F, 2.9F, 0.1047F, 0F, 0F));
        PartDefinition p_vueltas = p_antebrazo_d.addOrReplaceChild("vueltas", CubeListBuilder.create().texOffs(78, 63).addBox(-2F, 1.2F, -2F, 4F, 1F, 4F, new CubeDeformation(0.28F, 0.28F, 0.28F)).texOffs(94, 63).addBox(-2F, 4F, -2F, 4F, 1F, 4F, new CubeDeformation(0.28F, 0.28F, 0.28F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_vuelta_alta = p_brazo_d.addOrReplaceChild("vuelta_alta", CubeListBuilder.create().texOffs(94, 63).addBox(-2F, 2.6F, -2F, 4F, 1F, 4F, new CubeDeformation(0.28F, 0.28F, 0.28F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_c1 = p_mano_d.addOrReplaceChild("c1", CubeListBuilder.create().texOffs(110, 63).addBox(-1.1F, -0.45F, -3.3F, 2.2F, 0.9F, 3.6F).texOffs(68, 63).addBox(-0.45F, -1.1F, -6.3F, 0.9F, 2.2F, 3.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_c2 = p_c1.addOrReplaceChild("c2", CubeListBuilder.create().texOffs(110, 63).addBox(-1.1F, -0.45F, -3.3F, 2.2F, 0.9F, 3.6F).texOffs(68, 63).addBox(-0.45F, -1.1F, -6.3F, 0.9F, 2.2F, 3.6F), PartPose.offset(0F, 0F, -6F));
        PartDefinition p_c3 = p_c2.addOrReplaceChild("c3", CubeListBuilder.create().texOffs(110, 63).addBox(-1.1F, -0.45F, -3.3F, 2.2F, 0.9F, 3.6F).texOffs(68, 63).addBox(-0.45F, -1.1F, -6.3F, 0.9F, 2.2F, 3.6F), PartPose.offset(0F, 0F, -6F));
        PartDefinition p_c4 = p_c3.addOrReplaceChild("c4", CubeListBuilder.create().texOffs(110, 63).addBox(-1.1F, -0.45F, -3.3F, 2.2F, 0.9F, 3.6F).texOffs(68, 63).addBox(-0.45F, -1.1F, -6.3F, 0.9F, 2.2F, 3.6F), PartPose.offset(0F, 0F, -6F));
        PartDefinition p_eslabon = p_c4.addOrReplaceChild("eslabon", CubeListBuilder.create().texOffs(60, 24).addBox(-3F, -0.8F, -10.6F, 1.6F, 1.6F, 10.6F).texOffs(18, 63).addBox(1.4F, -0.8F, -10.6F, 1.6F, 1.6F, 6.2F).texOffs(8, 73).addBox(-1.4F, -0.8F, -10.6F, 2.8F, 1.6F, 1.6F).texOffs(18, 73).addBox(-1.4F, -0.8F, -1.6F, 1.6F, 1.6F, 1.6F), PartPose.offset(0F, 0F, -6F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {};
}
