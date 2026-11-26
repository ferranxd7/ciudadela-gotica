package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class MolineraGeometria {
    private MolineraGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -13F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(34, 72).addBox(-2F, 0F, -2F, 4F, 6.5F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(50, 72).addBox(-2F, 0F, -2F, 4F, 6.5F, 4F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(66, 72).addBox(-2F, 0F, -2F, 4F, 6.5F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(82, 72).addBox(-2F, 0F, -2F, 4F, 6.5F, 4F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(80, 34).addBox(-4.5F, -14F, -2.5F, 9F, 14F, 5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(0, 56).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -14F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(64, 56).addBox(-2F, -2F, -2F, 4F, 8F, 4F), PartPose.offset(-6.5F, -12F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(80, 56).addBox(-2F, 0F, -2F, 4F, 8F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(96, 56).addBox(-2F, -2F, -2F, 4F, 8F, 4F), PartPose.offset(6.5F, -12F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(112, 56).addBox(-2F, 0F, -2F, 4F, 8F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_saya = p_cadera.addOrReplaceChild("saya", CubeListBuilder.create().texOffs(44, 34).addBox(-5.5F, -0.5F, -3.5F, 11F, 13F, 7F, new CubeDeformation(0.5F, 0.5F, 0.5F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_delantal = p_cadera.addOrReplaceChild("delantal", CubeListBuilder.create().texOffs(98, 72).addBox(-3.5F, -0.2F, -4.2F, 7F, 11F, 0F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chal = p_torso.addOrReplaceChild("chal", CubeListBuilder.create().texOffs(0, 72).addBox(-5.5F, -0.5F, -3F, 11F, 5F, 6F, new CubeDeformation(0.4F, 0.4F, 0.4F)).texOffs(112, 72).addBox(-3.5F, 4F, 2.6F, 7F, 6F, 1F), PartPose.offset(0F, -14F, 0F));
        PartDefinition p_panuelo = p_cabeza.addOrReplaceChild("panuelo", CubeListBuilder.create().texOffs(32, 56).addBox(-4F, -8F, -4F, 8F, 6F, 8F, new CubeDeformation(0.55F, 0.55F, 0.55F)).texOffs(0, 83).addBox(-1.5F, -4.5F, 4.2F, 3F, 2F, 2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_guadana = p_mano_d.addOrReplaceChild("guadana", CubeListBuilder.create().texOffs(0, 34).addBox(-0.75F, -0.75F, -32F, 1.5F, 1.5F, 20F).texOffs(40, 0).addBox(-0.75F, -0.75F, -12F, 1.5F, 1.5F, 22F).texOffs(10, 83).addBox(-2F, -0.75F, -16F, 3F, 1.5F, 1.5F).texOffs(20, 83).addBox(-1F, -1F, -33.5F, 2F, 2F, 2F).texOffs(0, 0).addBox(0F, 0F, -37F, 0F, 14F, 20F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {};
}
