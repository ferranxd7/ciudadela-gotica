package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class BalleneroGeometria {
    private BalleneroGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(0, 59).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(16, 69).addBox(-2.2F, 0F, -2.2F, 4.4F, 1.8F, 4.4F).texOffs(74, 69).addBox(-1F, 1.8F, -1F, 2F, 3.4F, 2F).texOffs(86, 77).addBox(-0.7F, 5.2F, -0.7F, 1.4F, 0.8F, 1.4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(16, 59).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(32, 59).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(82, 0).addBox(-4.5F, -12F, -2.5F, 9F, 12F, 5F).texOffs(0, 30).addBox(-4.5F, -12F, -2.5F, 9F, 12F, 5F, new CubeDeformation(0.5F, 0.5F, 0.5F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(28, 30).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(48, 59).addBox(-2F, -2F, -2F, 4F, 6F, 4F), PartPose.offset(-6.5F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(64, 59).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(80, 59).addBox(-2F, -2F, -2F, 4F, 6F, 4F), PartPose.offset(6.5F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(96, 59).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_bota_i = p_pierna_i.addOrReplaceChild("bota_i", CubeListBuilder.create().texOffs(98, 47).addBox(-2.3F, 0F, -2.3F, 4.6F, 5F, 4.6F), PartPose.offset(0F, 1F, 0F));
        PartDefinition p_cuello_chaqueton = p_torso.addOrReplaceChild("cuello_chaqueton", CubeListBuilder.create().texOffs(64, 47).addBox(-5F, -2F, -3.1F, 10F, 2.6F, 6.2F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_faldon_tras = p_cadera.addOrReplaceChild("faldon_tras", CubeListBuilder.create().texOffs(0, 47).addBox(-5F, -0.5F, 1.9F, 10F, 10.5F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon_d = p_muslo_d.addOrReplaceChild("faldon_d", CubeListBuilder.create().texOffs(60, 30).addBox(-3F, -0.5F, -2.6F, 1F, 9.5F, 5.2F).texOffs(112, 59).addBox(-2.4F, -0.5F, -3.1F, 3F, 8.5F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon_i = p_muslo_i.addOrReplaceChild("faldon_i", CubeListBuilder.create().texOffs(74, 30).addBox(2F, -0.5F, -2.6F, 1F, 9.5F, 5.2F).texOffs(120, 59).addBox(-0.6F, -0.5F, -3.1F, 3F, 8.5F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bandolera = p_torso.addOrReplaceChild("bandolera", CubeListBuilder.create().texOffs(60, 0).addBox(-0.9F, -8.6F, -3.3F, 1.8F, 17.2F, 6.6F), PartPose.offsetAndRotation(0F, -6.2F, 0F, 0F, 0F, -0.5934F));
        PartDefinition p_rollo = p_cadera.addOrReplaceChild("rollo", CubeListBuilder.create().texOffs(0, 69).addBox(-2.6F, -2.6F, -0.9F, 5.2F, 5.2F, 1.8F), PartPose.offsetAndRotation(-5.4F, 1.2F, 0.4F, 0F, 1.5708F, 0.1396F));
        PartDefinition p_sal_hombros = p_torso.addOrReplaceChild("sal_hombros", CubeListBuilder.create().texOffs(50, 69).addBox(-6.4F, -2.2F, -1.2F, 2.4F, 2.6F, 2.4F).texOffs(10, 77).addBox(-5F, -3F, 0.6F, 1.4F, 2F, 1.4F).texOffs(96, 69).addBox(4.2F, -1.8F, -0.8F, 2.2F, 2.2F, 2F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_sal_cabeza = p_cabeza.addOrReplaceChild("sal_cabeza", CubeListBuilder.create().texOffs(18, 77).addBox(1.4F, -1.4F, -1.6F, 1.6F, 1.6F, 1.6F).texOffs(26, 77).addBox(-2.8F, -0.9F, 1F, 1.2F, 1.2F, 1.2F), PartPose.offset(0F, -8F, 0F));
        PartDefinition p_barba = p_cabeza.addOrReplaceChild("barba", CubeListBuilder.create().texOffs(22, 47).addBox(-3.6F, -3F, -4.7F, 7.2F, 9.6F, 1.6F).texOffs(66, 77).addBox(-4.1F, -3.8F, -4.5F, 8.2F, 1.6F, 0.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_melena = p_cabeza.addOrReplaceChild("melena", CubeListBuilder.create().texOffs(88, 30).addBox(-4.5F, -6F, -0.4F, 9F, 8.4F, 4.9F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_arpon_espalda = p_torso.addOrReplaceChild("arpon_espalda", CubeListBuilder.create().texOffs(78, 0).addBox(-0.5F, -13F, -0.5F, 1F, 22F, 1F).texOffs(116, 69).addBox(-0.35F, -16.5F, -0.35F, 0.7F, 3.5F, 0.7F).texOffs(34, 77).addBox(-1.3F, -19F, -0.4F, 2.6F, 2.5F, 0.8F), PartPose.offsetAndRotation(0.5F, -5.5F, 3.3F, 0F, 0F, 0.6632F));
        PartDefinition p_arpon = p_mano_d.addOrReplaceChild("arpon", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6F, -0.6F, -18F, 1.2F, 1.2F, 28F).texOffs(42, 77).addBox(-0.8F, -0.8F, -18.6F, 1.6F, 1.6F, 1.4F).texOffs(36, 69).addBox(-0.45F, -0.45F, -24F, 0.9F, 0.9F, 6F).texOffs(82, 69).addBox(-1.5F, -0.5F, -27.6F, 3F, 1F, 3.8F).texOffs(0, 77).addBox(-2.7F, -0.5F, -25.4F, 1.3F, 1F, 2.2F).texOffs(0, 77).addBox(1.4F, -0.5F, -25.4F, 1.3F, 1F, 2.2F).texOffs(106, 69).addBox(-0.9F, -0.9F, 7.6F, 1.8F, 1.8F, 2.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_farol = p_mano_i.addOrReplaceChild("farol", CubeListBuilder.create().texOffs(50, 77).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 3F).texOffs(62, 69).addBox(-1.9F, -1.9F, -3.2F, 3.8F, 3.8F, 1.2F).texOffs(42, 47).addBox(-2.2F, -2.2F, -8.4F, 4.4F, 4.4F, 5.2F).texOffs(58, 77).addBox(-1.5F, -1.5F, -9.4F, 3F, 3F, 1F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {};
}
