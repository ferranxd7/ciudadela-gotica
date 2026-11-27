package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class CentinelaGeometria {
    private CentinelaGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(76, 83).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(92, 83).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(108, 83).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(0, 94).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 67).addBox(-4F, -12F, -2F, 8F, 12F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(96, 44).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(16, 94).addBox(-2F, -2F, -2F, 4F, 6F, 4F), PartPose.offset(-6F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(32, 94).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(48, 94).addBox(-2F, -2F, -2F, 4F, 6F, 4F), PartPose.offset(6F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(64, 94).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_coraza = p_torso.addOrReplaceChild("coraza", CubeListBuilder.create().texOffs(24, 67).addBox(-4F, -12F, -2F, 8F, 12F, 4F, new CubeDeformation(0.6F, 0.6F, 0.6F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gola = p_torso.addOrReplaceChild("gola", CubeListBuilder.create().texOffs(104, 104).addBox(-3.4F, -1.4F, -2.4F, 6.8F, 2F, 4.8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_faldon = p_cadera.addOrReplaceChild("faldon", CubeListBuilder.create().texOffs(0, 83).addBox(-4F, -0.5F, -2F, 8F, 7F, 4F, new CubeDeformation(0.5F, 0.5F, 0.5F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_sobrevesta = p_cadera.addOrReplaceChild("sobrevesta", CubeListBuilder.create().texOffs(90, 67).addBox(-2.6F, 0F, -3F, 5.2F, 11.5F, 0.6F).texOffs(104, 67).addBox(-2.6F, 0F, 2.4F, 5.2F, 10.5F, 0.6F), PartPose.offset(0F, -0.5F, 0F));
        PartDefinition p_hombrera_d = p_brazo_d.addOrReplaceChild("hombrera_d", CubeListBuilder.create().texOffs(24, 83).addBox(-3.2F, -3.6F, -3F, 6.4F, 3.8F, 6F).texOffs(80, 94).addBox(-3.4F, -1.2F, -3.2F, 6.8F, 1.8F, 6.4F).texOffs(30, 113).addBox(-1.4F, -6.2F, -0.6F, 1.2F, 2.8F, 1.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_guante_d = p_antebrazo_d.addOrReplaceChild("guante_d", CubeListBuilder.create().texOffs(28, 104).addBox(-2.3F, 3F, -2.3F, 4.6F, 3.4F, 4.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hombrera_i = p_brazo_i.addOrReplaceChild("hombrera_i", CubeListBuilder.create().texOffs(50, 83).addBox(-3.2F, -3.6F, -3F, 6.4F, 3.8F, 6F).texOffs(0, 104).addBox(-3.4F, -1.2F, -3.2F, 6.8F, 1.8F, 6.4F).texOffs(38, 113).addBox(0.2F, -6.2F, -0.6F, 1.2F, 2.8F, 1.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_guante_i = p_antebrazo_i.addOrReplaceChild("guante_i", CubeListBuilder.create().texOffs(48, 104).addBox(-2.3F, 3F, -2.3F, 4.6F, 3.4F, 4.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_yelmo = p_cabeza.addOrReplaceChild("yelmo", CubeListBuilder.create().texOffs(64, 44).addBox(-4F, -8.5F, -4F, 8F, 8.5F, 8F, new CubeDeformation(0.7F, 0.7F, 0.7F)).texOffs(66, 67).addBox(-0.6F, -11.2F, -4.6F, 1.2F, 2.4F, 9.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_capa = p_torso.addOrReplaceChild("capa", CubeListBuilder.create().texOffs(42, 44).addBox(-5F, 0F, 0F, 10F, 19F, 1F), PartPose.offsetAndRotation(0F, -11.6F, 2.7F, 0.1047F, 0F, 0F));
        PartDefinition p_espadon = p_mano_d.addOrReplaceChild("espadon", CubeListBuilder.create().texOffs(68, 104).addBox(-0.6F, -0.6F, -2.5F, 1.2F, 1.2F, 7F).texOffs(10, 113).addBox(-1.3F, -1.3F, 4.5F, 2.6F, 2.6F, 1.6F).texOffs(46, 113).addBox(-5F, -0.9F, -3.8F, 10F, 1.8F, 1.5F).texOffs(0, 44).addBox(-0.7F, -1.8F, -22.5F, 1.4F, 3.6F, 19F).texOffs(20, 113).addBox(-0.7F, -1F, -25F, 1.4F, 2F, 2.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_alabarda = p_mano_d.addOrReplaceChild("alabarda", CubeListBuilder.create().texOffs(0, 0).addBox(-0.55F, -0.55F, -27F, 1.1F, 1.1F, 42F).texOffs(48, 67).addBox(-0.55F, -7F, -26F, 1.1F, 6.5F, 7F).texOffs(0, 113).addBox(-0.55F, 0.5F, -24.5F, 1.1F, 3F, 2.5F).texOffs(86, 104).addBox(-0.55F, -0.55F, -34F, 1.1F, 1.1F, 7F).texOffs(70, 113).addBox(-0.8F, -0.8F, 15F, 1.6F, 1.6F, 1.5F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"alabarda"};

    public static final String[] OCULTAS_1 = {"espadon"};
}
