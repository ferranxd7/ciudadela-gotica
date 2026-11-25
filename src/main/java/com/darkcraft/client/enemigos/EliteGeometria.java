package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class EliteGeometria {
    private EliteGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(72, 37).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(88, 37).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(104, 37).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(0, 48).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 21).addBox(-4F, -12F, -2F, 8F, 12F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(90, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(16, 48).addBox(-2F, -2F, -2F, 4F, 6F, 4F), PartPose.offset(-6F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(32, 48).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(48, 48).addBox(-2F, -2F, -2F, 4F, 6F, 4F), PartPose.offset(6F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(64, 48).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_coraza = p_torso.addOrReplaceChild("coraza", CubeListBuilder.create().texOffs(24, 21).addBox(-4F, -12F, -2F, 8F, 12F, 4F, new CubeDeformation(0.55F, 0.55F, 0.55F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon = p_cadera.addOrReplaceChild("faldon", CubeListBuilder.create().texOffs(0, 37).addBox(-4F, -0.5F, -2F, 8F, 7F, 4F, new CubeDeformation(0.45F, 0.45F, 0.45F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hombrera_d = p_brazo_d.addOrReplaceChild("hombrera_d", CubeListBuilder.create().texOffs(24, 37).addBox(-3F, -3.4F, -2.9F, 6F, 3.6F, 5.8F).texOffs(88, 48).addBox(-3.2F, -1.2F, -3.1F, 6.4F, 1.6F, 6.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hombrera_i = p_brazo_i.addOrReplaceChild("hombrera_i", CubeListBuilder.create().texOffs(48, 37).addBox(-3F, -3.4F, -2.9F, 6F, 3.6F, 5.8F).texOffs(0, 58).addBox(-3.2F, -1.2F, -3.1F, 6.4F, 1.6F, 6.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_yelmo = p_cabeza.addOrReplaceChild("yelmo", CubeListBuilder.create().texOffs(58, 0).addBox(-4F, -8.5F, -4F, 8F, 8.5F, 8F, new CubeDeformation(0.65F, 0.65F, 0.65F)).texOffs(48, 21).addBox(-4.8F, -10.2F, -4.8F, 9.6F, 1.4F, 9.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_penacho = p_yelmo.addOrReplaceChild("penacho", CubeListBuilder.create().texOffs(88, 21).addBox(-0.7F, -2.8F, -3.4F, 1.4F, 2.8F, 8.2F).texOffs(80, 48).addBox(-0.7F, -1.4F, 4.8F, 1.4F, 7.5F, 1.4F), PartPose.offset(0F, -10.2F, 0F));
        PartDefinition p_capa = p_torso.addOrReplaceChild("capa", CubeListBuilder.create().texOffs(0, 0).addBox(-5F, 0F, 0F, 10F, 20F, 1F), PartPose.offsetAndRotation(0F, -11.6F, 2.7F, 0.1047F, 0F, 0F));
        PartDefinition p_mandoble = p_mano_d.addOrReplaceChild("mandoble", CubeListBuilder.create().texOffs(28, 58).addBox(-0.6F, -0.6F, -2.5F, 1.2F, 1.2F, 6F).texOffs(44, 58).addBox(-1.2F, -1.2F, 3.5F, 2.4F, 2.4F, 1.6F).texOffs(54, 58).addBox(-4.5F, -0.8F, -3.6F, 9F, 1.6F, 1.4F).texOffs(22, 0).addBox(-0.6F, -1.4F, -19.5F, 1.2F, 2.8F, 16F).texOffs(76, 58).addBox(-0.6F, -0.8F, -21.5F, 1.2F, 1.6F, 2F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {};
}
