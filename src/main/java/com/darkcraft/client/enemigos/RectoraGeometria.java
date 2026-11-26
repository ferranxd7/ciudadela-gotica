package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class RectoraGeometria {
    private RectoraGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(24, 67).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(40, 67).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(56, 67).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(72, 67).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(74, 37).addBox(-4F, -12F, -2F, 8F, 12F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(42, 37).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(88, 67).addBox(-1.5F, -2F, -1.5F, 3F, 7F, 3F), PartPose.offset(-5F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(100, 67).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(112, 67).addBox(-1.5F, -2F, -1.5F, 3F, 7F, 3F), PartPose.offset(5F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(0, 77).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_vestido = p_cadera.addOrReplaceChild("vestido", CubeListBuilder.create().texOffs(0, 54).addBox(-4.6F, 0F, -2.6F, 9.2F, 7F, 5.2F), PartPose.offset(0F, -0.5F, 0F));
        PartDefinition p_vestido_bajo = p_vestido.addOrReplaceChild("vestido_bajo", CubeListBuilder.create().texOffs(0, 37).addBox(-6.2F, 0F, -3.6F, 12.4F, 8.2F, 7.2F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_cinto = p_torso.addOrReplaceChild("cinto", CubeListBuilder.create().texOffs(30, 77).addBox(-4.3F, -0.6F, -2.3F, 8.6F, 1.1F, 4.6F).texOffs(94, 77).addBox(2.4F, 0.5F, -2.9F, 2.4F, 2.4F, 0.5F), PartPose.offset(0F, -1.6F, 0F));
        PartDefinition p_manga_d = p_antebrazo_d.addOrReplaceChild("manga_d", CubeListBuilder.create().texOffs(32, 54).addBox(-2.6F, 0F, -2.6F, 5.2F, 5.6F, 5.2F), PartPose.offset(0F, 2F, 0F));
        PartDefinition p_manga_i = p_antebrazo_i.addOrReplaceChild("manga_i", CubeListBuilder.create().texOffs(56, 54).addBox(-2.6F, 0F, -2.6F, 5.2F, 5.6F, 5.2F), PartPose.offset(0F, 2F, 0F));
        PartDefinition p_cuello = p_torso.addOrReplaceChild("cuello", CubeListBuilder.create().texOffs(0, 67).addBox(-5.2F, -8F, 0F, 10.4F, 8.4F, 0.7F), PartPose.offsetAndRotation(0F, -12F, 1.8F, -0.2094F, 0F, 0F));
        PartDefinition p_capa = p_torso.addOrReplaceChild("capa", CubeListBuilder.create().texOffs(74, 0).addBox(-5.4F, 0F, 0F, 10.8F, 24.5F, 0.8F), PartPose.offsetAndRotation(0F, -11.6F, 2.6F, 0.1047F, 0F, 0F));
        PartDefinition p_pelo = p_cabeza.addOrReplaceChild("pelo", CubeListBuilder.create().texOffs(98, 37).addBox(-4.3F, 0F, 0F, 8.6F, 14F, 1.3F), PartPose.offset(0F, -7.6F, 3.2F));
        PartDefinition p_diadema = p_cabeza.addOrReplaceChild("diadema", CubeListBuilder.create().texOffs(80, 54).addBox(-4.3F, -6.9F, -4.3F, 8.6F, 0.9F, 8.6F).texOffs(102, 77).addBox(-0.7F, -7.2F, -4.7F, 1.4F, 1.4F, 0.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aureola = p_cabeza.addOrReplaceChild("aureola", CubeListBuilder.create().texOffs(108, 77).addBox(5.5F, -0.9F, 0F, 1.8F, 1.8F, 0.5F).texOffs(28, 87).addBox(5.0426F, 2.7F, 0F, 1F, 1F, 0.5F).texOffs(32, 87).addBox(2.7F, 5.0426F, 0F, 1F, 1F, 0.5F).texOffs(114, 77).addBox(-0.9F, 5.5F, 0F, 1.8F, 1.8F, 0.5F).texOffs(36, 87).addBox(-3.7F, 5.0426F, 0F, 1F, 1F, 0.5F).texOffs(40, 87).addBox(-6.0426F, 2.7F, 0F, 1F, 1F, 0.5F).texOffs(120, 77).addBox(-7.3F, -0.9F, 0F, 1.8F, 1.8F, 0.5F).texOffs(44, 87).addBox(-6.0426F, -3.7F, 0F, 1F, 1F, 0.5F).texOffs(48, 87).addBox(-3.7F, -6.0426F, 0F, 1F, 1F, 0.5F).texOffs(0, 87).addBox(-0.9F, -7.3F, 0F, 1.8F, 1.8F, 0.5F).texOffs(52, 87).addBox(2.7F, -6.0426F, 0F, 1F, 1F, 0.5F).texOffs(56, 87).addBox(5.0426F, -3.7F, 0F, 1F, 1F, 0.5F), PartPose.offset(0F, -4.5F, 5.4F));
        PartDefinition p_baculo = p_mano_d.addOrReplaceChild("baculo", CubeListBuilder.create().texOffs(0, 0).addBox(-0.45F, -0.45F, -22F, 0.9F, 0.9F, 36F).texOffs(6, 87).addBox(-2.4F, -0.4F, -22.8F, 4.8F, 0.8F, 0.8F).texOffs(74, 77).addBox(-2.4F, -0.4F, -26.6F, 0.8F, 0.8F, 3.8F).texOffs(84, 77).addBox(1.6F, -0.4F, -26.6F, 0.8F, 0.8F, 3.8F).texOffs(62, 77).addBox(-1.1F, -1.1F, -25.8F, 2.2F, 2.2F, 2.2F).texOffs(12, 77).addBox(-0.4F, -0.4F, -28.6F, 0.8F, 0.8F, 7.8F).texOffs(58, 77).addBox(-0.4F, -2.9F, -25.1F, 0.8F, 5.8F, 0.8F).texOffs(18, 87).addBox(-1.7F, -0.4F, -25.1F, 3.4F, 0.8F, 0.8F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"muslo_d", "muslo_i", "pierna_d", "pierna_i"};
}
