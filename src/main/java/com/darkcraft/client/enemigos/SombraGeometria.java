package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class SombraGeometria {
    private SombraGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(72, 60).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(88, 60).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(104, 60).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(0, 73).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(70, 0).addBox(-5F, -14F, -2.5F, 10F, 14F, 5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(50, 29).addBox(-4F, -9F, -4F, 8F, 9F, 8F), PartPose.offset(0F, -14F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(86, 46).addBox(-1.5F, -2F, -1.5F, 3F, 10F, 3F), PartPose.offset(-6F, -12F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(98, 46).addBox(-1.5F, 0F, -1.5F, 3F, 10F, 3F), PartPose.offset(0F, 8F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 9F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(110, 46).addBox(-1.5F, -2F, -1.5F, 3F, 10F, 3F), PartPose.offset(6F, -12F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(0, 60).addBox(-1.5F, 0F, -1.5F, 3F, 10F, 3F), PartPose.offset(0F, 8F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 9F, 0F));
        PartDefinition p_tunica = p_cadera.addOrReplaceChild("tunica", CubeListBuilder.create().texOffs(0, 46).addBox(-5.8F, 0F, -3.4F, 11.6F, 7F, 6.8F), PartPose.offset(0F, -0.5F, 0F));
        PartDefinition p_tunica_bajo = p_tunica.addOrReplaceChild("tunica_bajo", CubeListBuilder.create().texOffs(0, 29).addBox(-7.6F, 0F, -4.4F, 15.2F, 7.5F, 8.8F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_capa = p_torso.addOrReplaceChild("capa", CubeListBuilder.create().texOffs(0, 0).addBox(-6.6F, 0F, 0F, 13.2F, 28F, 0.8F), PartPose.offsetAndRotation(0F, -13.5F, 2.6F, 0.1396F, 0F, 0F));
        PartDefinition p_manto = p_torso.addOrReplaceChild("manto", CubeListBuilder.create().texOffs(38, 46).addBox(-8F, -1.6F, -3.6F, 16F, 4.2F, 7.2F).texOffs(106, 73).addBox(-8.6F, -5.4F, -0.6F, 1.2F, 3.8F, 1.2F).texOffs(16, 83).addBox(-6.8F, -4.4F, 1F, 1F, 2.8F, 1F).texOffs(114, 73).addBox(7.4F, -5.4F, -0.6F, 1.2F, 3.8F, 1.2F).texOffs(20, 83).addBox(5.8F, -4.4F, 1F, 1F, 2.8F, 1F), PartPose.offset(0F, -13F, 0F));
        PartDefinition p_costillas = p_torso.addOrReplaceChild("costillas", CubeListBuilder.create().texOffs(52, 60).addBox(-4.4F, -12.6F, -3.1F, 8.8F, 9.2F, 0.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_corazon = p_torso.addOrReplaceChild("corazon", CubeListBuilder.create().texOffs(80, 73).addBox(-1.6F, -1.6F, -1.6F, 3.2F, 3.2F, 3.2F), PartPose.offset(0F, -8.4F, -1.4F));
        PartDefinition p_capucha = p_cabeza.addOrReplaceChild("capucha", CubeListBuilder.create().texOffs(30, 0).addBox(-5F, -10.4F, -4.6F, 10F, 10.8F, 9.6F).texOffs(16, 73).addBox(-3F, -12.2F, -0.4F, 6F, 2.6F, 5.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_corona = p_cabeza.addOrReplaceChild("corona", CubeListBuilder.create().texOffs(12, 60).addBox(-4.6F, -1.4F, -4.6F, 9.2F, 1.8F, 9.2F).texOffs(10, 83).addBox(-0.7F, -5F, -4.9F, 1.4F, 3.8F, 1F).texOffs(24, 83).addBox(-3.9F, -3.8F, -4.8F, 1F, 2.6F, 1F).texOffs(38, 83).addBox(2.9F, -3.2F, -4.8F, 1F, 2F, 1F).texOffs(28, 83).addBox(-4.8F, -3.6F, -0.5F, 1F, 2.4F, 1F).texOffs(42, 83).addBox(3.8F, -2.4F, -0.5F, 1F, 1.2F, 1F).texOffs(32, 83).addBox(-0.8F, -1F, -5F, 1.6F, 1.4F, 0.6F), PartPose.offsetAndRotation(0F, -14.4F, -0.4F, 0F, 0F, -0.1571F));
        PartDefinition p_grillete_d = p_antebrazo_d.addOrReplaceChild("grillete_d", CubeListBuilder.create().texOffs(40, 73).addBox(-2.2F, 0F, -2.2F, 4.4F, 2.4F, 4.4F), PartPose.offset(0F, 6.6F, 0F));
        PartDefinition p_eslabones_d = p_antebrazo_d.addOrReplaceChild("eslabones_d", CubeListBuilder.create().texOffs(96, 73).addBox(-0.6F, 0F, -1.3F, 1.2F, 2.6F, 2.6F).texOffs(0, 83).addBox(-1.3F, 2.2F, -0.6F, 2.6F, 2.6F, 1.2F).texOffs(96, 73).addBox(-0.6F, 4.4F, -1.3F, 1.2F, 2.6F, 2.6F), PartPose.offset(0F, 9F, -1.8F));
        PartDefinition p_garra_d = p_mano_d.addOrReplaceChild("garra_d", CubeListBuilder.create().texOffs(82, 29).addBox(-2.4F, -0.8F, -2.4F, 4.8F, 9.6F, 4.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_grillete_i = p_antebrazo_i.addOrReplaceChild("grillete_i", CubeListBuilder.create().texOffs(60, 73).addBox(-2.2F, 0F, -2.2F, 4.4F, 2.4F, 4.4F), PartPose.offset(0F, 6.6F, 0F));
        PartDefinition p_eslabones_i = p_antebrazo_i.addOrReplaceChild("eslabones_i", CubeListBuilder.create().texOffs(96, 73).addBox(-0.6F, 0F, -1.3F, 1.2F, 2.6F, 2.6F).texOffs(0, 83).addBox(-1.3F, 2.2F, -0.6F, 2.6F, 2.6F, 1.2F).texOffs(96, 73).addBox(-0.6F, 4.4F, -1.3F, 1.2F, 2.6F, 2.6F), PartPose.offset(0F, 9F, -1.8F));
        PartDefinition p_garra_i = p_mano_i.addOrReplaceChild("garra_i", CubeListBuilder.create().texOffs(102, 29).addBox(-2.4F, -0.8F, -2.4F, 4.8F, 9.6F, 4.8F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"muslo_d", "muslo_i", "pierna_d", "pierna_i"};

    public static final String[] OCULTAS_1 = {"muslo_d", "muslo_i", "pierna_d", "pierna_i"};
}
