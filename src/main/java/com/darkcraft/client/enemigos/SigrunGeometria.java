package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class SigrunGeometria {
    private SigrunGeometria() {}

    public static final int TEX_W = 128, TEX_H = 64;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(32, 39).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(48, 39).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(64, 39).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(80, 39).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 23).addBox(-4F, -12F, -2F, 8F, 12F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(80, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(96, 39).addBox(-1.5F, -2F, -1.5F, 3F, 6F, 3F), PartPose.offset(-5.1F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(108, 39).addBox(-1.5F, 0F, -1.5F, 3F, 6F, 3F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(0, 49).addBox(-1.5F, -2F, -1.5F, 3F, 6F, 3F), PartPose.offset(5.1F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(12, 49).addBox(-1.5F, 0F, -1.5F, 3F, 6F, 3F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_coraza = p_torso.addOrReplaceChild("coraza", CubeListBuilder.create().texOffs(24, 23).addBox(-4F, -12F, -2F, 8F, 12F, 4F, new CubeDeformation(0.5F, 0.5F, 0.5F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon = p_cadera.addOrReplaceChild("faldon", CubeListBuilder.create().texOffs(80, 23).addBox(-4F, -0.5F, -2F, 8F, 7F, 4F, new CubeDeformation(0.42F, 0.42F, 0.42F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_manto = p_torso.addOrReplaceChild("manto", CubeListBuilder.create().texOffs(0, 39).addBox(-5F, -12.9F, -2.9F, 10F, 3.2F, 5.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_capa = p_torso.addOrReplaceChild("capa", CubeListBuilder.create().texOffs(58, 0).addBox(-4.6F, 0F, 0F, 9.2F, 18F, 0.8F), PartPose.offsetAndRotation(0F, -10.4F, 2.5F, 0.1222F, 0F, 0F));
        PartDefinition p_hombrera_d = p_brazo_d.addOrReplaceChild("hombrera_d", CubeListBuilder.create().texOffs(24, 49).addBox(-2.3F, -2.8F, -2.3F, 4.6F, 3F, 4.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hombrera_i = p_brazo_i.addOrReplaceChild("hombrera_i", CubeListBuilder.create().texOffs(44, 49).addBox(-2.3F, -2.8F, -2.3F, 4.6F, 3F, 4.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_yelmo = p_cabeza.addOrReplaceChild("yelmo", CubeListBuilder.create().texOffs(48, 23).addBox(-4F, -8F, -4F, 8F, 3.5F, 8F, new CubeDeformation(0.5F, 0.5F, 0.5F)).texOffs(110, 49).addBox(-1F, -9.6F, -1F, 2F, 1.4F, 2F).texOffs(6, 58).addBox(-0.5F, -4.5F, -4.7F, 1F, 2.6F, 0.5F).texOffs(64, 49).addBox(-4.65F, -4.5F, -2.6F, 0.5F, 3.2F, 3.8F).texOffs(74, 49).addBox(4.15F, -4.5F, -2.6F, 0.5F, 3.2F, 3.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_trenza = p_cabeza.addOrReplaceChild("trenza", CubeListBuilder.create().texOffs(84, 49).addBox(-0.8F, 0F, -0.8F, 1.6F, 6F, 1.6F), PartPose.offsetAndRotation(0F, -2.6F, 4.2F, -0.1396F, 0F, 0F));
        PartDefinition p_trenza_b = p_trenza.addOrReplaceChild("trenza_b", CubeListBuilder.create().texOffs(92, 49).addBox(-0.65F, 0F, -0.65F, 1.3F, 3.5F, 1.3F).texOffs(10, 58).addBox(-0.8F, 3.5F, -0.8F, 1.6F, 0.8F, 1.6F), PartPose.offsetAndRotation(0F, 6F, 0F, -0.0698F, 0F, 0F));
        PartDefinition p_espada = p_mano_d.addOrReplaceChild("espada", CubeListBuilder.create().texOffs(100, 49).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 4F).texOffs(118, 49).addBox(-1F, -1F, 2F, 2F, 2F, 1.5F).texOffs(24, 58).addBox(-3.5F, -0.5F, -3F, 7F, 1F, 1F).texOffs(22, 0).addBox(-0.5F, -1F, -20F, 1F, 2F, 17F).texOffs(18, 58).addBox(-0.5F, -0.5F, -21.5F, 1F, 1F, 1.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_escudo = p_antebrazo_i.addOrReplaceChild("escudo", CubeListBuilder.create().texOffs(0, 0).addBox(1.6F, -3.6F, -5F, 1F, 13F, 10F).texOffs(0, 58).addBox(2.6F, 0.6F, -1F, 0.6F, 2F, 2F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {};
}
