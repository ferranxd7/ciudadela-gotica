package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class VerdugoGeometria {
    private VerdugoGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -13F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(78, 50).addBox(-2.5F, 0F, -2.5F, 5F, 6.5F, 5F), PartPose.offset(-2.5F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(98, 50).addBox(-2.5F, 0F, -2.5F, 5F, 6.5F, 5F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(0, 63).addBox(-2.5F, 0F, -2.5F, 5F, 6.5F, 5F), PartPose.offset(2.5F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(20, 63).addBox(-2.5F, 0F, -2.5F, 5F, 6.5F, 5F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 30).addBox(-5F, -14F, -3F, 10F, 14F, 6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(64, 30).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -14F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(96, 30).addBox(-2.5F, -2F, -2.5F, 5F, 7.5F, 5F), PartPose.offset(-7.5F, -12F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(0, 50).addBox(-2.5F, 0F, -2.5F, 5F, 7.5F, 5F), PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(20, 50).addBox(-2.5F, -2F, -2.5F, 5F, 7.5F, 5F), PartPose.offset(7.5F, -12F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(40, 50).addBox(-2.5F, 0F, -2.5F, 5F, 7.5F, 5F), PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_capucha = p_cabeza.addOrReplaceChild("capucha", CubeListBuilder.create().texOffs(32, 30).addBox(-4F, -8.5F, -4F, 8F, 9F, 8F, new CubeDeformation(0.5F, 0.5F, 0.5F)).texOffs(96, 75).addBox(-2F, -11F, -1.5F, 4F, 3F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_esclavina = p_torso.addOrReplaceChild("esclavina", CubeListBuilder.create().texOffs(40, 63).addBox(-6F, -0.5F, -3.5F, 12F, 4F, 7F, new CubeDeformation(0.35F, 0.35F, 0.35F)), PartPose.offset(0F, -14F, 0F));
        PartDefinition p_mandil = p_cadera.addOrReplaceChild("mandil", CubeListBuilder.create().texOffs(60, 50).addBox(-4.5F, -6F, -3.6F, 9F, 13F, 0F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cinto = p_cadera.addOrReplaceChild("cinto", CubeListBuilder.create().texOffs(78, 63).addBox(-5.5F, -1.8F, -3.5F, 11F, 2F, 7F, new CubeDeformation(0.25F, 0.25F, 0.25F)).texOffs(80, 75).addBox(4.8F, -1F, -2F, 2F, 4F, 4F).texOffs(112, 75).addBox(-6.4F, -0.5F, -1F, 1F, 5F, 2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_brazal_d = p_antebrazo_d.addOrReplaceChild("brazal_d", CubeListBuilder.create().texOffs(0, 75).addBox(-2.5F, 1F, -2.5F, 5F, 4F, 5F, new CubeDeformation(0.35F, 0.35F, 0.35F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bota_d = p_pierna_d.addOrReplaceChild("bota_d", CubeListBuilder.create().texOffs(20, 75).addBox(-2.5F, 2.5F, -2.5F, 5F, 4F, 5F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_brazal_i = p_antebrazo_i.addOrReplaceChild("brazal_i", CubeListBuilder.create().texOffs(40, 75).addBox(-2.5F, 1F, -2.5F, 5F, 4F, 5F, new CubeDeformation(0.35F, 0.35F, 0.35F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bota_i = p_pierna_i.addOrReplaceChild("bota_i", CubeListBuilder.create().texOffs(60, 75).addBox(-2.5F, 2.5F, -2.5F, 5F, 4F, 5F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hacha = p_mano_d.addOrReplaceChild("hacha", CubeListBuilder.create().texOffs(26, 0).addBox(-0.9F, -0.9F, -36F, 1.8F, 1.8F, 22F).texOffs(74, 0).addBox(-0.9F, -0.9F, -14F, 1.8F, 1.8F, 20F).texOffs(0, 84).addBox(-1.3F, -1.3F, 5.5F, 2.6F, 2.6F, 2.5F).texOffs(12, 84).addBox(-1.2F, -1.2F, -37.5F, 2.4F, 2.4F, 3F).texOffs(0, 0).addBox(0F, -5F, -39F, 0F, 17F, 13F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lazo_mano = p_mano_i.addOrReplaceChild("lazo_mano", CubeListBuilder.create().texOffs(24, 84).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F).texOffs(92, 75).addBox(-0.5F, 0F, -0.5F, 1F, 7F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lazo_i = p_lazo_mano.addOrReplaceChild("lazo_i", CubeListBuilder.create().texOffs(36, 84).addBox(-2.5F, 0F, -0.5F, 1F, 5F, 1F).texOffs(40, 84).addBox(1.5F, 0F, -0.5F, 1F, 5F, 1F).texOffs(44, 84).addBox(-2.5F, 4F, -0.5F, 5F, 1F, 1F), PartPose.offset(0F, 7F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {};
}
