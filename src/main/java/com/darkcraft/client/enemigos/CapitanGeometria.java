package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class CapitanGeometria {
    private CapitanGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(64, 46).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(80, 46).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(96, 46).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(112, 46).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(46, 0).addBox(-4F, -12F, -2.5F, 8F, 12F, 5F).texOffs(72, 0).addBox(-4F, -12F, -2.5F, 8F, 12F, 5F, new CubeDeformation(0.45F, 0.45F, 0.45F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(0, 30).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(0, 59).addBox(-2F, -2F, -2F, 4F, 6F, 4F), PartPose.offset(-6F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(16, 59).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(32, 59).addBox(-2F, -2F, -2F, 4F, 6F, 4F), PartPose.offset(6F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(48, 59).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_faldon_tras = p_cadera.addOrReplaceChild("faldon_tras", CubeListBuilder.create().texOffs(44, 46).addBox(-4.5F, -0.5F, 1.6F, 9F, 10F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon_d = p_muslo_d.addOrReplaceChild("faldon_d", CubeListBuilder.create().texOffs(112, 30).addBox(-3F, -0.5F, -2.5F, 1F, 8F, 5F).texOffs(96, 59).addBox(-2.4F, -0.5F, -3F, 3F, 7F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_charretera_d = p_brazo_d.addOrReplaceChild("charretera_d", CubeListBuilder.create().texOffs(0, 69).addBox(-2.5F, -1F, -2.5F, 5F, 2F, 5F, new CubeDeformation(0.15F, 0.15F, 0.15F)), PartPose.offset(0F, -2F, 0F));
        PartDefinition p_faldon_i = p_muslo_i.addOrReplaceChild("faldon_i", CubeListBuilder.create().texOffs(0, 46).addBox(2F, -0.5F, -2.5F, 1F, 8F, 5F).texOffs(104, 59).addBox(-0.6F, -0.5F, -3F, 3F, 7F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_charretera_i = p_brazo_i.addOrReplaceChild("charretera_i", CubeListBuilder.create().texOffs(0, 69).addBox(-2.5F, -1F, -2.5F, 5F, 2F, 5F, new CubeDeformation(0.15F, 0.15F, 0.15F)), PartPose.offset(0F, -2F, 0F));
        PartDefinition p_sombrero = p_cabeza.addOrReplaceChild("sombrero", CubeListBuilder.create().texOffs(60, 30).addBox(-6.5F, -1F, -6.5F, 13F, 1F, 13F).texOffs(12, 46).addBox(-4F, -5F, -4F, 8F, 4F, 8F, new CubeDeformation(0.3F, 0.3F, 0.3F)).texOffs(34, 69).addBox(-5F, -4F, 5.5F, 10F, 3F, 1F), PartPose.offsetAndRotation(0F, -7.4F, 0F, -0.0698F, 0F, 0F));
        PartDefinition p_vuelta_d = p_sombrero.addOrReplaceChild("vuelta_d", CubeListBuilder.create().texOffs(32, 30).addBox(-0.5F, -3F, -8F, 1F, 3F, 13F), PartPose.offsetAndRotation(-6F, -1F, 1F, 0F, -0.4189F, 0.1396F));
        PartDefinition p_vuelta_i = p_sombrero.addOrReplaceChild("vuelta_i", CubeListBuilder.create().texOffs(32, 30).addBox(-0.5F, -3F, -8F, 1F, 3F, 13F), PartPose.offsetAndRotation(6F, -1F, 1F, 0F, 0.4189F, -0.1396F));
        PartDefinition p_barba = p_cabeza.addOrReplaceChild("barba", CubeListBuilder.create().texOffs(82, 59).addBox(-3F, -2F, -4.7F, 6F, 7F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_sable = p_mano_d.addOrReplaceChild("sable", CubeListBuilder.create().texOffs(20, 69).addBox(-0.5F, -0.5F, -2.5F, 1F, 1F, 6F).texOffs(56, 69).addBox(-2F, -1.5F, -4F, 4F, 3F, 1F).texOffs(0, 0).addBox(0F, -4F, -27F, 0F, 7F, 23F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_farol = p_mano_i.addOrReplaceChild("farol", CubeListBuilder.create().texOffs(66, 69).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 3F).texOffs(74, 69).addBox(-1.5F, -1.5F, -3F, 3F, 3F, 1F).texOffs(64, 59).addBox(-2F, -2F, -8F, 4F, 4F, 5F).texOffs(82, 69).addBox(-1F, -1F, -9F, 2F, 2F, 1F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {};
}
