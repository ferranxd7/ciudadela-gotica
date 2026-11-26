package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class CorcelGeometria {
    private CorcelGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create().texOffs(62, 28).addBox(-4.5F, -2.5F, -2F, 9F, 5F, 7.5F), PartPose.offset(0F, -20F, 9F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-5F, -2.5F, -19F, 10F, 10F, 18F).texOffs(56, 0).addBox(-1F, -3.6F, -21F, 2F, 2F, 23F).texOffs(0, 46).addBox(-5.3F, -3.2F, -12.5F, 10.6F, 6.5F, 6F, new CubeDeformation(0.1F, 0.1F, 0.1F)).texOffs(0, 71).addBox(-4.6F, -4.9F, -13F, 9.2F, 2F, 7F).texOffs(58, 80).addBox(-1.1F, -6.4F, -13.4F, 2.2F, 1.8F, 1.6F), PartPose.offset(0F, -0.5F, -2F));
        PartDefinition p_cuello = p_torso.addOrReplaceChild("cuello", CubeListBuilder.create().texOffs(30, 28).addBox(-2F, -2F, -11F, 4F, 4.5F, 12F).texOffs(0, 28).addBox(-0.7F, -6.2F, -11.5F, 1.4F, 4.4F, 12.5F), PartPose.offsetAndRotation(0F, -1F, -18F, -0.9076F, 0F, 0F));
        PartDefinition p_cabeza = p_cuello.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(0, 59).addBox(-2.6F, -3F, -5.5F, 5.2F, 5.5F, 6F).texOffs(34, 46).addBox(-1.9F, -2.3F, -12.5F, 3.8F, 4.1F, 7.2F).texOffs(28, 80).addBox(-2.2F, -5.2F, -1.6F, 1.2F, 2.6F, 1.1F).texOffs(36, 80).addBox(1F, -5.2F, -1.6F, 1.2F, 2.6F, 1.1F).texOffs(44, 80).addBox(-2.1F, -1F, -9.6F, 4.2F, 1.2F, 1.2F, new CubeDeformation(0.05F, 0.05F, 0.05F)).texOffs(34, 71).addBox(-0.6F, -5.6F, -3F, 1.2F, 3.2F, 3.6F), PartPose.offsetAndRotation(0F, 0.2F, -10.5F, 1.6406F, 0F, 0F));
        PartDefinition p_mandibula = p_cabeza.addOrReplaceChild("mandibula", CubeListBuilder.create().texOffs(24, 59).addBox(-1.6F, 0F, -7.2F, 3.2F, 1.4F, 7.6F), PartPose.offsetAndRotation(0F, 1.6F, -5F, 0.0698F, 0F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(90, 46).addBox(-1.5F, -2F, -1.6F, 3F, 8.5F, 3.2F), PartPose.offset(-3.4F, 6F, -16F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(46, 71).addBox(-1F, 0F, -1F, 2F, 6F, 2F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create().texOffs(68, 80).addBox(-0.9F, 0F, -0.9F, 1.8F, 1.2F, 1.8F).texOffs(62, 71).addBox(-1.6F, 1.2F, -1.9F, 3.2F, 1.3F, 3.6F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(104, 46).addBox(-1.5F, -2F, -1.6F, 3F, 8.5F, 3.2F), PartPose.offset(3.4F, 6F, -16F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(54, 71).addBox(-1F, 0F, -1F, 2F, 6F, 2F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create().texOffs(76, 80).addBox(-0.9F, 0F, -0.9F, 1.8F, 1.2F, 1.8F).texOffs(78, 71).addBox(-1.6F, 1.2F, -1.9F, 3.2F, 1.3F, 3.6F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(58, 46).addBox(-1.8F, -2F, -2F, 3.6F, 8.5F, 4F), PartPose.offsetAndRotation(-3.6F, 1.5F, 2.5F, -0.3491F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(88, 59).addBox(-1.1F, 0F, -1.1F, 2.2F, 7F, 2.2F), PartPose.offsetAndRotation(0F, 6.5F, 0F, 0.7854F, 0F, 0F));
        PartDefinition p_pata_d = p_pierna_d.addOrReplaceChild("pata_d", CubeListBuilder.create().texOffs(12, 80).addBox(-0.9F, 0F, -0.9F, 1.8F, 3.4F, 1.8F).texOffs(94, 71).addBox(-1.6F, 3.4F, -1.9F, 3.2F, 1.3F, 3.6F), PartPose.offsetAndRotation(0F, 7F, 0F, -0.4363F, 0F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(74, 46).addBox(-1.8F, -2F, -2F, 3.6F, 8.5F, 4F), PartPose.offsetAndRotation(3.6F, 1.5F, 2.5F, -0.3491F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(100, 59).addBox(-1.1F, 0F, -1.1F, 2.2F, 7F, 2.2F), PartPose.offsetAndRotation(0F, 6.5F, 0F, 0.7854F, 0F, 0F));
        PartDefinition p_pata_i = p_pierna_i.addOrReplaceChild("pata_i", CubeListBuilder.create().texOffs(20, 80).addBox(-0.9F, 0F, -0.9F, 1.8F, 3.4F, 1.8F).texOffs(110, 71).addBox(-1.6F, 3.4F, -1.9F, 3.2F, 1.3F, 3.6F), PartPose.offsetAndRotation(0F, 7F, 0F, -0.4363F, 0F, 0F));
        PartDefinition p_cola = p_cadera.addOrReplaceChild("cola", CubeListBuilder.create().texOffs(0, 80).addBox(-0.8F, -0.8F, 0F, 1.6F, 1.6F, 4F).texOffs(48, 59).addBox(-1.6F, -1.6F, 2.5F, 3.2F, 3.2F, 6F), PartPose.offsetAndRotation(0F, -2F, 5F, -0.6109F, 0F, 0F));
        PartDefinition p_cola2 = p_cola.addOrReplaceChild("cola2", CubeListBuilder.create().texOffs(68, 59).addBox(-1.3F, -1.3F, 0F, 2.6F, 2.6F, 7F), PartPose.offsetAndRotation(0F, 0F, 8F, -0.4363F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {};
}
