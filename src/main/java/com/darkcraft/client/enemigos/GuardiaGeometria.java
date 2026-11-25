package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class GuardiaGeometria {
    private GuardiaGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(90, 89).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(106, 89).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(0, 100).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(16, 100).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(64, 58).addBox(-4F, -12F, -2F, 8F, 12F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(36, 39).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(32, 100).addBox(-2F, -2F, -2F, 4F, 6F, 4F), PartPose.offset(-6F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(48, 100).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(64, 100).addBox(-2F, -2F, -2F, 4F, 6F, 4F), PartPose.offset(6F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(80, 100).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_coraza = p_torso.addOrReplaceChild("coraza", CubeListBuilder.create().texOffs(88, 58).addBox(-4F, -12F, -2F, 8F, 12F, 4F, new CubeDeformation(0.5F, 0.5F, 0.5F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon = p_cadera.addOrReplaceChild("faldon", CubeListBuilder.create().texOffs(18, 89).addBox(-4F, -0.5F, -2F, 8F, 6F, 4F, new CubeDeformation(0.42F, 0.42F, 0.42F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hombrera_d = p_brazo_d.addOrReplaceChild("hombrera_d", CubeListBuilder.create().texOffs(42, 89).addBox(-2.7F, -3.1F, -2.6F, 5.4F, 3.4F, 5.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hombrera_i = p_brazo_i.addOrReplaceChild("hombrera_i", CubeListBuilder.create().texOffs(66, 89).addBox(-2.7F, -3.1F, -2.6F, 5.4F, 3.4F, 5.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_yelmo = p_cabeza.addOrReplaceChild("yelmo", CubeListBuilder.create().texOffs(68, 39).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.6F, 0.6F, 0.6F)).texOffs(0, 89).addBox(-0.5F, -11F, -3.5F, 1F, 2.4F, 8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cofia = p_cabeza.addOrReplaceChild("cofia", CubeListBuilder.create().texOffs(0, 58).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_capacete = p_cabeza.addOrReplaceChild("capacete", CubeListBuilder.create().texOffs(78, 74).addBox(-4F, -8.5F, -4F, 8F, 4F, 8F, new CubeDeformation(0.62F, 0.62F, 0.62F)).texOffs(30, 74).addBox(-6F, -5F, -6F, 12F, 1F, 12F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_capucha = p_cabeza.addOrReplaceChild("capucha", CubeListBuilder.create().texOffs(32, 58).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.66F, 0.66F, 0.66F)).texOffs(14, 110).addBox(-2F, -6.5F, 4.4F, 4F, 5F, 1.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_espada = p_mano_d.addOrReplaceChild("espada", CubeListBuilder.create().texOffs(26, 110).addBox(-0.5F, -0.5F, -2.5F, 1F, 1F, 5F).texOffs(76, 110).addBox(-1F, -1F, 2.5F, 2F, 2F, 1.5F).texOffs(112, 110).addBox(-3F, -0.5F, -3.5F, 6F, 1F, 1F).texOffs(0, 39).addBox(-0.5F, -1F, -20.5F, 1F, 2F, 17F).texOffs(106, 110).addBox(-0.5F, -0.5F, -22.5F, 1F, 1F, 2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_alabarda = p_mano_d.addOrReplaceChild("alabarda", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -0.5F, -24F, 1F, 1F, 38F).texOffs(110, 74).addBox(-0.5F, -6F, -23F, 1F, 5.5F, 6F).texOffs(38, 110).addBox(-0.5F, 0.5F, -21.5F, 1F, 2.5F, 2.5F).texOffs(0, 110).addBox(-0.5F, -0.5F, -30F, 1F, 1F, 6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ballesta = p_mano_d.addOrReplaceChild("ballesta", CubeListBuilder.create().texOffs(0, 74).addBox(-1F, -1F, -9F, 2F, 2F, 13F).texOffs(46, 110).addBox(-6.5F, -0.6F, -9.5F, 13F, 1.2F, 1.5F).texOffs(98, 110).addBox(-1F, -0.5F, -11F, 2F, 1F, 1.5F).texOffs(106, 100).addBox(-0.4F, -1.7F, -8.5F, 0.8F, 0.7F, 7F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_escudo = p_antebrazo_i.addOrReplaceChild("escudo", CubeListBuilder.create().texOffs(78, 0).addBox(2.3F, -3.5F, -4.5F, 1F, 12F, 9F).texOffs(92, 110).addBox(3.3F, 1F, -1F, 0.6F, 2F, 2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aljaba = p_cadera.addOrReplaceChild("aljaba", CubeListBuilder.create().texOffs(96, 100).addBox(-5.9F, -2F, 0F, 1.6F, 6.5F, 2.4F).texOffs(84, 110).addBox(-5.7F, -3.4F, 0.3F, 1.2F, 1.4F, 1.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0.2094F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"cofia", "capacete", "capucha", "alabarda", "ballesta", "aljaba"};

    public static final String[] OCULTAS_1 = {"yelmo", "capucha", "espada", "escudo", "ballesta", "aljaba"};

    public static final String[] OCULTAS_2 = {"yelmo", "capacete", "espada", "escudo", "alabarda"};
}
