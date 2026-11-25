package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class BrandtGeometria {
    private BrandtGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(86, 54).addBox(-2.5F, 0F, -2.5F, 5F, 6F, 5F), PartPose.offset(-2.5F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(106, 54).addBox(-2.5F, 0F, -2.5F, 5F, 6F, 5F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(0, 66).addBox(-2.5F, 0F, -2.5F, 5F, 6F, 5F), PartPose.offset(2.5F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(20, 66).addBox(-2.5F, 0F, -2.5F, 5F, 6F, 5F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 23).addBox(-5F, -12F, -3F, 10F, 12F, 6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(96, 23).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(40, 66).addBox(-2.5F, -2F, -2.5F, 5F, 6F, 5F), PartPose.offset(-7.5F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(60, 66).addBox(-2.5F, 0F, -2.5F, 5F, 6F, 5F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(80, 66).addBox(-2.5F, -2F, -2.5F, 5F, 6F, 5F), PartPose.offset(7.5F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(100, 66).addBox(-2.5F, 0F, -2.5F, 5F, 6F, 5F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_coraza = p_torso.addOrReplaceChild("coraza", CubeListBuilder.create().texOffs(32, 23).addBox(-5F, -12F, -3F, 10F, 12F, 6F, new CubeDeformation(0.6F, 0.6F, 0.6F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gorjal = p_torso.addOrReplaceChild("gorjal", CubeListBuilder.create().texOffs(52, 54).addBox(-4.5F, -13.6F, -3.6F, 9F, 2.6F, 7.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon = p_cadera.addOrReplaceChild("faldon", CubeListBuilder.create().texOffs(0, 41).addBox(-5F, -0.5F, -3F, 10F, 7F, 6F, new CubeDeformation(0.5F, 0.5F, 0.5F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hombrera_d = p_brazo_d.addOrReplaceChild("hombrera_d", CubeListBuilder.create().texOffs(72, 41).addBox(-3.7F, -4.4F, -3.5F, 7.4F, 4.8F, 7F).texOffs(0, 77).addBox(-3.9F, -1.6F, -3.7F, 7.8F, 1.8F, 7.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hombrera_i = p_brazo_i.addOrReplaceChild("hombrera_i", CubeListBuilder.create().texOffs(0, 54).addBox(-3.7F, -4.4F, -3.5F, 7.4F, 4.8F, 7F).texOffs(32, 77).addBox(-3.9F, -1.6F, -3.7F, 7.8F, 1.8F, 7.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_yelmo = p_cabeza.addOrReplaceChild("yelmo", CubeListBuilder.create().texOffs(64, 23).addBox(-4F, -8.6F, -4F, 8F, 8.6F, 8F, new CubeDeformation(0.7F, 0.7F, 0.7F)).texOffs(32, 41).addBox(-4.9F, -10.6F, -4.9F, 9.8F, 1.8F, 9.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_pua_0 = p_yelmo.addOrReplaceChild("pua_0", CubeListBuilder.create().texOffs(90, 77).addBox(-0.6F, -2.2F, -0.6F, 1.2F, 2.2F, 1.2F), PartPose.offset(-3.6F, -10.6F, -4.2F));
        PartDefinition p_pua_1 = p_yelmo.addOrReplaceChild("pua_1", CubeListBuilder.create().texOffs(98, 77).addBox(-0.6F, -2.2F, -0.6F, 1.2F, 2.2F, 1.2F), PartPose.offset(0F, -10.6F, -4.6F));
        PartDefinition p_pua_2 = p_yelmo.addOrReplaceChild("pua_2", CubeListBuilder.create().texOffs(106, 77).addBox(-0.6F, -2.2F, -0.6F, 1.2F, 2.2F, 1.2F), PartPose.offset(3.6F, -10.6F, -4.2F));
        PartDefinition p_pua_3 = p_yelmo.addOrReplaceChild("pua_3", CubeListBuilder.create().texOffs(114, 77).addBox(-0.6F, -2.2F, -0.6F, 1.2F, 2.2F, 1.2F), PartPose.offset(-4.2F, -10.6F, 0F));
        PartDefinition p_pua_4 = p_yelmo.addOrReplaceChild("pua_4", CubeListBuilder.create().texOffs(0, 87).addBox(-0.6F, -2.2F, -0.6F, 1.2F, 2.2F, 1.2F), PartPose.offset(4.2F, -10.6F, 0F));
        PartDefinition p_escudo = p_antebrazo_i.addOrReplaceChild("escudo", CubeListBuilder.create().texOffs(46, 0).addBox(-6.5F, 5F, -10F, 13F, 1.6F, 19F).texOffs(64, 77).addBox(-2F, 6.6F, -2F, 4F, 1F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_martillo = p_mano_d.addOrReplaceChild("martillo", CubeListBuilder.create().texOffs(0, 0).addBox(-0.7F, -0.7F, -15F, 1.4F, 1.4F, 21F).texOffs(16, 87).addBox(-1F, -1F, 6F, 2F, 2F, 1.6F).texOffs(30, 54).addBox(-2.6F, -3.2F, -19.8F, 5.2F, 6.4F, 4.8F).texOffs(80, 77).addBox(-0.8F, -0.8F, -22.2F, 1.6F, 1.6F, 2.4F).texOffs(8, 87).addBox(-0.7F, -6.2F, -18.4F, 1.4F, 3F, 2F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {};
}
