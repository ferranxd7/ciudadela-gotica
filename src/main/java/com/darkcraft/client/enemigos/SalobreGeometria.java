package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class SalobreGeometria {
    private SalobreGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(100, 76).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(0, 87).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(16, 87).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(32, 87).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 48).addBox(-4F, -12F, -2F, 8F, 12F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(78, 31).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(48, 87).addBox(-2F, -2F, -2F, 4F, 6F, 4F), PartPose.offset(-6F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(64, 87).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(80, 87).addBox(-2F, -2F, -2F, 4F, 6F, 4F), PartPose.offset(6F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(96, 87).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_faldon = p_cadera.addOrReplaceChild("faldon", CubeListBuilder.create().texOffs(32, 76).addBox(-4.3F, -0.5F, -2.3F, 8.6F, 6F, 4.6F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bota_d = p_pierna_d.addOrReplaceChild("bota_d", CubeListBuilder.create().texOffs(60, 76).addBox(-2.3F, 0F, -2.3F, 4.6F, 4.9F, 4.6F), PartPose.offset(0F, 1.4F, 0F));
        PartDefinition p_bota_i = p_pierna_i.addOrReplaceChild("bota_i", CubeListBuilder.create().texOffs(80, 76).addBox(-2.3F, 0F, -2.3F, 4.6F, 4.9F, 4.6F), PartPose.offset(0F, 1.4F, 0F));
        PartDefinition p_sal_hombros = p_torso.addOrReplaceChild("sal_hombros", CubeListBuilder.create().texOffs(44, 97).addBox(-6.2F, -2F, -1.4F, 2.4F, 2.6F, 2.4F).texOffs(72, 105).addBox(-4.6F, -2.8F, 0.4F, 1.4F, 2F, 1.4F).texOffs(28, 105).addBox(3.8F, -1.6F, -1F, 2.2F, 2.2F, 2F).texOffs(80, 105).addBox(5F, -2.4F, 0.8F, 1.2F, 1.6F, 1.2F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_sal_cabeza = p_cabeza.addOrReplaceChild("sal_cabeza", CubeListBuilder.create().texOffs(88, 105).addBox(1.6F, -1.6F, -1.8F, 1.6F, 1.8F, 1.6F).texOffs(96, 105).addBox(-3F, -1F, 1.2F, 1.2F, 1.2F, 1.2F), PartPose.offset(0F, -8F, 0F));
        PartDefinition p_gorro = p_cabeza.addOrReplaceChild("gorro", CubeListBuilder.create().texOffs(0, 76).addBox(-4F, -1.6F, -4F, 8F, 2.8F, 8F, new CubeDeformation(0.35F, 0.35F, 0.35F)).texOffs(0, 97).addBox(-3.4F, -2.6F, -3.4F, 6.8F, 1F, 6.8F), PartPose.offset(0F, -8F, 0F));
        PartDefinition p_red_hombro = p_torso.addOrReplaceChild("red_hombro", CubeListBuilder.create().texOffs(72, 64).addBox(-5.2F, 0F, 0F, 10.4F, 8.5F, 2.6F).texOffs(80, 97).addBox(-5.4F, 7.6F, -0.2F, 10.8F, 1.2F, 3F), PartPose.offsetAndRotation(0F, -11.5F, 2F, 0F, 0F, 0.2094F));
        PartDefinition p_cuerda = p_mano_d.addOrReplaceChild("cuerda", CubeListBuilder.create().texOffs(28, 97).addBox(-0.3F, -0.3F, -6F, 0.6F, 0.6F, 6.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_plomo = p_cuerda.addOrReplaceChild("plomo", CubeListBuilder.create().texOffs(56, 97).addBox(-1.3F, -1.3F, -2.6F, 2.6F, 2.6F, 2.8F), PartPose.offset(0F, 0F, -6F));
        PartDefinition p_sueste = p_cabeza.addOrReplaceChild("sueste", CubeListBuilder.create().texOffs(24, 48).addBox(-5.2F, -0.8F, -5F, 10.4F, 0.9F, 11.2F).texOffs(0, 64).addBox(-4.2F, -3.4F, -4.2F, 8.4F, 2.8F, 8.4F), PartPose.offsetAndRotation(0F, -7.6F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_bichero = p_mano_d.addOrReplaceChild("bichero", CubeListBuilder.create().texOffs(62, 0).addBox(-0.5F, -0.5F, -22F, 1F, 1F, 27F).texOffs(68, 97).addBox(-0.65F, -0.65F, -25.5F, 1.3F, 1.3F, 3.6F).texOffs(68, 105).addBox(-0.5F, -3.6F, -24.4F, 1F, 3.4F, 1F).texOffs(104, 105).addBox(-0.5F, -4F, -23.2F, 1F, 1F, 1.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_sombrero = p_cabeza.addOrReplaceChild("sombrero", CubeListBuilder.create().texOffs(18, 31).addBox(-7.5F, -1F, -7.5F, 15F, 1F, 15F).texOffs(36, 64).addBox(-4.2F, -3.8F, -4.2F, 8.4F, 3F, 8.4F, new CubeDeformation(0.2F, 0.2F, 0.2F)), PartPose.offsetAndRotation(0F, -7.6F, 0F, -0.0698F, 0F, 0.0524F));
        PartDefinition p_delantal = p_torso.addOrReplaceChild("delantal", CubeListBuilder.create().texOffs(0, 31).addBox(-3.9F, 0F, -0.5F, 7.8F, 15.5F, 0.5F), PartPose.offset(0F, -9F, -2.3F));
        PartDefinition p_saco = p_torso.addOrReplaceChild("saco", CubeListBuilder.create().texOffs(70, 48).addBox(-3.6F, 0F, 0F, 7.2F, 8.5F, 3.8F, new CubeDeformation(0.2F, 0.2F, 0.2F)), PartPose.offset(0F, -11.5F, 2.1F));
        PartDefinition p_rastro = p_mano_d.addOrReplaceChild("rastro", CubeListBuilder.create().texOffs(0, 0).addBox(-0.55F, -0.55F, -24F, 1.1F, 1.1F, 29F).texOffs(0, 105).addBox(-5.5F, -1F, -26.4F, 11F, 2F, 2.4F).texOffs(38, 105).addBox(-5F, 1F, -26F, 1F, 2.4F, 1.6F).texOffs(44, 105).addBox(-2.5F, 1F, -26F, 1F, 2.4F, 1.6F).texOffs(50, 105).addBox(0F, 1F, -26F, 1F, 2.4F, 1.6F).texOffs(56, 105).addBox(2.5F, 1F, -26F, 1F, 2.4F, 1.6F).texOffs(62, 105).addBox(4.5F, 1F, -26F, 1F, 2.4F, 1.6F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"sueste", "bichero", "sombrero", "delantal", "saco", "rastro"};

    public static final String[] OCULTAS_1 = {"gorro", "red_hombro", "cuerda", "plomo", "sombrero", "delantal", "saco", "rastro", "sal_cabeza"};

    public static final String[] OCULTAS_2 = {"gorro", "red_hombro", "cuerda", "plomo", "sueste", "bichero", "sal_cabeza"};
}
