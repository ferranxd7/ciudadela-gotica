package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class CortezaGeometria {
    private CortezaGeometria() {}

    public static final int TEX_W = 128, TEX_H = 64;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(92, 18).addBox(-2.5F, 0F, -2.5F, 5F, 6F, 5F), PartPose.offset(-2.5F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(0, 33).addBox(-2.5F, 0F, -2.5F, 5F, 6F, 5F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(20, 33).addBox(-2.5F, 0F, -2.5F, 5F, 6F, 5F), PartPose.offset(2.5F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(40, 33).addBox(-2.5F, 0F, -2.5F, 5F, 6F, 5F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-5F, -12F, -3F, 10F, 12F, 6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(96, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(60, 33).addBox(-2.5F, -2F, -2.5F, 5F, 6F, 5F), PartPose.offset(-7.5F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(80, 33).addBox(-2.5F, 0F, -2.5F, 5F, 6F, 5F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(100, 33).addBox(-2.5F, -2F, -2.5F, 5F, 6F, 5F), PartPose.offset(7.5F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(0, 44).addBox(-2.5F, 0F, -2.5F, 5F, 6F, 5F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_coraza = p_torso.addOrReplaceChild("coraza", CubeListBuilder.create().texOffs(32, 0).addBox(-5F, -12F, -3F, 10F, 12F, 6F, new CubeDeformation(0.65F, 0.65F, 0.65F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon = p_cadera.addOrReplaceChild("faldon", CubeListBuilder.create().texOffs(0, 18).addBox(-5F, -0.5F, -3F, 10F, 9F, 6F, new CubeDeformation(0.55F, 0.55F, 0.55F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hombrera_d = p_brazo_d.addOrReplaceChild("hombrera_d", CubeListBuilder.create().texOffs(32, 18).addBox(-3.6F, -4.6F, -3.4F, 7.2F, 5.2F, 6.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_rama_d = p_hombrera_d.addOrReplaceChild("rama_d", CubeListBuilder.create().texOffs(16, 55).addBox(-0.8F, -5F, -0.8F, 1.6F, 5F, 1.6F), PartPose.offsetAndRotation(-1.5F, -4.4F, 1F, -0.3491F, 0F, -0.3491F));
        PartDefinition p_dedo_d0 = p_mano_d.addOrReplaceChild("dedo_d0", CubeListBuilder.create().texOffs(20, 44).addBox(-0.6F, -0.6F, -6.5F, 1.2F, 1.2F, 7F), PartPose.offsetAndRotation(-1.6F, 0.5F, -0.5F, -0.1745F, 0F, 0F));
        PartDefinition p_dedo_d1 = p_mano_d.addOrReplaceChild("dedo_d1", CubeListBuilder.create().texOffs(38, 44).addBox(-0.6F, -0.6F, -6.5F, 1.2F, 1.2F, 7F), PartPose.offsetAndRotation(0F, 0.5F, -0.5F, -0.1745F, 0F, 0F));
        PartDefinition p_dedo_d2 = p_mano_d.addOrReplaceChild("dedo_d2", CubeListBuilder.create().texOffs(56, 44).addBox(-0.6F, -0.6F, -6.5F, 1.2F, 1.2F, 7F), PartPose.offsetAndRotation(1.6F, 0.5F, -0.5F, -0.1745F, 0F, 0F));
        PartDefinition p_hombrera_i = p_brazo_i.addOrReplaceChild("hombrera_i", CubeListBuilder.create().texOffs(62, 18).addBox(-3.6F, -4.6F, -3.4F, 7.2F, 5.2F, 6.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_rama_i = p_hombrera_i.addOrReplaceChild("rama_i", CubeListBuilder.create().texOffs(24, 55).addBox(-0.8F, -5F, -0.8F, 1.6F, 5F, 1.6F), PartPose.offsetAndRotation(1.5F, -4.4F, 1F, -0.3491F, 0F, 0.3491F));
        PartDefinition p_dedo_i0 = p_mano_i.addOrReplaceChild("dedo_i0", CubeListBuilder.create().texOffs(74, 44).addBox(-0.6F, -0.6F, -6.5F, 1.2F, 1.2F, 7F), PartPose.offsetAndRotation(-1.6F, 0.5F, -0.5F, -0.1745F, 0F, 0F));
        PartDefinition p_dedo_i1 = p_mano_i.addOrReplaceChild("dedo_i1", CubeListBuilder.create().texOffs(92, 44).addBox(-0.6F, -0.6F, -6.5F, 1.2F, 1.2F, 7F), PartPose.offsetAndRotation(0F, 0.5F, -0.5F, -0.1745F, 0F, 0F));
        PartDefinition p_dedo_i2 = p_mano_i.addOrReplaceChild("dedo_i2", CubeListBuilder.create().texOffs(110, 44).addBox(-0.6F, -0.6F, -6.5F, 1.2F, 1.2F, 7F), PartPose.offsetAndRotation(1.6F, 0.5F, -0.5F, -0.1745F, 0F, 0F));
        PartDefinition p_yelmo = p_cabeza.addOrReplaceChild("yelmo", CubeListBuilder.create().texOffs(64, 0).addBox(-4F, -8.4F, -4F, 8F, 8.4F, 8F, new CubeDeformation(0.7F, 0.7F, 0.7F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cuerno_d = p_yelmo.addOrReplaceChild("cuerno_d", CubeListBuilder.create().texOffs(0, 55).addBox(-0.8F, -6F, -0.8F, 1.6F, 6F, 1.6F), PartPose.offsetAndRotation(-3F, -8.4F, 0F, 0.1745F, 0F, -0.4538F));
        PartDefinition p_cuerno2_d = p_cuerno_d.addOrReplaceChild("cuerno2_d", CubeListBuilder.create().texOffs(32, 55).addBox(-0.6F, -4F, -0.6F, 1.2F, 4F, 1.2F), PartPose.offsetAndRotation(0F, -6F, 0F, 0F, 0F, -0.5236F));
        PartDefinition p_cuerno_i = p_yelmo.addOrReplaceChild("cuerno_i", CubeListBuilder.create().texOffs(8, 55).addBox(-0.8F, -6F, -0.8F, 1.6F, 6F, 1.6F), PartPose.offsetAndRotation(3F, -8.4F, 0F, 0.1745F, 0F, 0.4538F));
        PartDefinition p_cuerno2_i = p_cuerno_i.addOrReplaceChild("cuerno2_i", CubeListBuilder.create().texOffs(40, 55).addBox(-0.6F, -4F, -0.6F, 1.2F, 4F, 1.2F), PartPose.offsetAndRotation(0F, -6F, 0F, 0F, 0F, 0.5236F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {};
}
