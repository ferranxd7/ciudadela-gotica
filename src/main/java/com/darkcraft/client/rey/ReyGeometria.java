package com.darkcraft.client.rey;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/rey/generar.py: no tocar a mano. Piezas y cajas del modelo del rey. */
public final class ReyGeometria {
    private ReyGeometria() {}

    public static final int TEX_W = 256, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create().texOffs(0, 59).addBox(-6.5F, -1.5F, -3.5F, 13F, 3F, 7F), PartPose.offset(0F, -18F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(181, 0).addBox(-3F, 0F, -3F, 6F, 9F, 6F, new CubeDeformation(-0.05F, -0.05F, -0.05F)), PartPose.offset(-3.3F, 0.5F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(206, 0).addBox(-3F, 0F, -3F, 6F, 9F, 6F, new CubeDeformation(-0.25F, -0.25F, -0.25F)).texOffs(206, 29).addBox(-3.5F, 4F, -4.5F, 7F, 5F, 8F, new CubeDeformation(-0.25F, -0.25F, -0.25F)), PartPose.offset(0F, 9F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(231, 0).addBox(-3F, 0F, -3F, 6F, 9F, 6F, new CubeDeformation(-0.05F, -0.05F, -0.05F)), PartPose.offset(3.3F, 0.5F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(0, 29).addBox(-3F, 0F, -3F, 6F, 9F, 6F, new CubeDeformation(-0.25F, -0.25F, -0.25F)).texOffs(0, 45).addBox(-3.5F, 4F, -4.5F, 7F, 5F, 8F, new CubeDeformation(-0.25F, -0.25F, -0.25F)), PartPose.offset(0F, 9F, 0F));
        PartDefinition p_falda_d = p_cadera.addOrReplaceChild("falda_d", CubeListBuilder.create().texOffs(31, 45).addBox(-3.4F, 0F, -0.5F, 7F, 12F, 1F), PartPose.offset(-3.6F, -0.5F, -4.6F));
        PartDefinition p_falda_i = p_cadera.addOrReplaceChild("falda_i", CubeListBuilder.create().texOffs(48, 45).addBox(-3.6F, 0F, -0.5F, 7F, 12F, 1F), PartPose.offset(3.6F, -0.5F, -4.6F));
        PartDefinition p_falda_t = p_cadera.addOrReplaceChild("falda_t", CubeListBuilder.create().texOffs(143, 29).addBox(-7.5F, 0F, -0.5F, 15F, 13F, 1F), PartPose.offset(0F, -0.5F, 4.6F));
        PartDefinition p_falda_ld = p_cadera.addOrReplaceChild("falda_ld", CubeListBuilder.create().texOffs(55, 0).addBox(-0.5F, 0F, -4.5F, 1F, 12F, 9F), PartPose.offset(-7.1F, -0.5F, 0F));
        PartDefinition p_falda_li = p_cadera.addOrReplaceChild("falda_li", CubeListBuilder.create().texOffs(76, 0).addBox(-0.5F, 0F, -4.5F, 1F, 12F, 9F), PartPose.offset(7.1F, -0.5F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(25, 29).addBox(-6.5F, -7F, -4F, 13F, 7F, 8F).texOffs(97, 0).addBox(-7F, -17F, -4.5F, 14F, 10F, 9F).texOffs(148, 45).addBox(-7F, -2.5F, -4.5F, 14F, 3F, 9F, new CubeDeformation(0.15F, 0.15F, 0.15F)).texOffs(195, 45).addBox(-7.3F, -18.4F, -4.9F, 15F, 2F, 10F, new CubeDeformation(-0.15F, 0F, -0.1F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(144, 0).addBox(-4.5F, -9F, -4.5F, 9F, 9F, 9F).texOffs(195, 59).addBox(-1F, -5F, -5.5F, 2F, 3F, 1F, new CubeDeformation(-0.15F, -0.15F, -0.15F)).texOffs(0, 70).addBox(-4F, -6.6F, -5F, 8F, 1F, 1F, new CubeDeformation(0F, 0.05F, -0.1F)).texOffs(19, 70).addBox(-3.5F, -3.4F, -5.3F, 7F, 1F, 1F, new CubeDeformation(0F, 0.1F, -0.05F)), PartPose.offset(0F, -18F, -0.3F));
        PartDefinition p_barba = p_cabeza.addOrReplaceChild("barba", CubeListBuilder.create().texOffs(48, 59).addBox(-4F, 0F, -1.6F, 8F, 7F, 2F), PartPose.offset(0F, -2.2F, -4.6F));
        PartDefinition p_barba_punta = p_barba.addOrReplaceChild("barba_punta", CubeListBuilder.create().texOffs(153, 59).addBox(-2.5F, 0F, -0.9F, 5F, 4F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F)), PartPose.offset(0F, 7F, -0.6F));
        PartDefinition p_pelo = p_cabeza.addOrReplaceChild("pelo", CubeListBuilder.create().texOffs(68, 29).addBox(-5F, -3.5F, -0.2F, 10F, 13F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F)), PartPose.offset(0F, -6F, 4.5F));
        PartDefinition p_pelo_d = p_cabeza.addOrReplaceChild("pelo_d", CubeListBuilder.create().texOffs(176, 29).addBox(-0.8F, 0F, -2.5F, 2F, 9F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.2F)), PartPose.offset(-4.7F, -6F, 0.2F));
        PartDefinition p_pelo_i = p_cabeza.addOrReplaceChild("pelo_i", CubeListBuilder.create().texOffs(191, 29).addBox(-1.2F, 0F, -2.5F, 2F, 9F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.2F)), PartPose.offset(4.7F, -6F, 0.2F));
        PartDefinition p_corona = p_cabeza.addOrReplaceChild("corona", CubeListBuilder.create().texOffs(65, 45).addBox(-5F, -1.5F, -5F, 10F, 3F, 10F, new CubeDeformation(0.05F, 0.05F, 0.05F)).texOffs(202, 59).addBox(-1F, -4.5F, -5.4F, 2F, 3F, 1F, new CubeDeformation(-0.3F, 0F, -0.2F)).texOffs(181, 59).addBox(-5.4F, -4.5F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.2F, 0F, -0.3F)).texOffs(188, 59).addBox(4.4F, -4.5F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.2F, 0F, -0.3F)).texOffs(209, 59).addBox(-1F, -4.5F, 4.4F, 2F, 3F, 1F, new CubeDeformation(-0.3F, 0F, -0.2F)).texOffs(225, 59).addBox(-5.2F, -3.5F, -5.2F, 1F, 2F, 1F).texOffs(230, 59).addBox(-5.2F, -3.5F, 4.2F, 1F, 2F, 1F).texOffs(235, 59).addBox(4.2F, -3.5F, 4.2F, 1F, 2F, 1F), PartPose.offset(0F, -9F, 0F));
        PartDefinition p_pico_roto = p_corona.addOrReplaceChild("pico_roto", CubeListBuilder.create().texOffs(240, 59).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F), PartPose.offsetAndRotation(4.6F, -1.5F, -4.6F, -0.2094F, 0F, 0.3142F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(93, 29).addBox(-3.4F, -1.8F, -3F, 6F, 9F, 6F, new CubeDeformation(-0.2F, -0.2F, -0.2F)), PartPose.offset(-8.6F, -15.2F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(106, 45).addBox(-2.5F, 0F, -2.5F, 5F, 8F, 5F).texOffs(69, 59).addBox(-3F, 0.5F, -3F, 6F, 3F, 6F, new CubeDeformation(-0.1F, -0.1F, -0.1F)).texOffs(119, 59).addBox(-2F, 8F, -2F, 4F, 3F, 4F), PartPose.offset(-0.4F, 7.2F, 0F));
        PartDefinition p_espada_d = p_antebrazo_d.addOrReplaceChild("espada_d", CubeListBuilder.create().texOffs(168, 59).addBox(-0.5F, -0.5F, -1.5F, 1F, 1F, 5F, new CubeDeformation(0.12F, 0.12F, 0.12F)).texOffs(216, 59).addBox(-1F, -1F, 3.5F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F)).texOffs(41, 59).addBox(-1F, -4.5F, -2.6F, 2F, 9F, 1F, new CubeDeformation(-0.25F, 0F, -0.1F)).texOffs(0, 0).addBox(-0.5F, -1F, -28.6F, 1F, 2F, 26F, new CubeDeformation(-0.12F, 0.1F, 0F)).texOffs(36, 70).addBox(-0.5F, -0.5F, -29.6F, 1F, 1F, 1F, new CubeDeformation(-0.12F, 0F, 0F)), PartPose.offsetAndRotation(0F, 9.6F, -0.2F, 0.9599F, 0F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(118, 29).addBox(-2.6F, -1.8F, -3F, 6F, 9F, 6F, new CubeDeformation(-0.2F, -0.2F, -0.2F)), PartPose.offset(8.6F, -15.2F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(127, 45).addBox(-2.5F, 0F, -2.5F, 5F, 8F, 5F).texOffs(94, 59).addBox(-3F, 0.5F, -3F, 6F, 3F, 6F, new CubeDeformation(-0.1F, -0.1F, -0.1F)).texOffs(136, 59).addBox(-2F, 8F, -2F, 4F, 3F, 4F), PartPose.offset(0.4F, 7.2F, 0F));
        PartDefinition p_espada_i = p_antebrazo_i.addOrReplaceChild("espada_i", CubeListBuilder.create().texOffs(168, 59).addBox(-0.5F, -0.5F, -1.5F, 1F, 1F, 5F, new CubeDeformation(0.12F, 0.12F, 0.12F)).texOffs(216, 59).addBox(-1F, -1F, 3.5F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F)).texOffs(41, 59).addBox(-1F, -4.5F, -2.6F, 2F, 9F, 1F, new CubeDeformation(-0.25F, 0F, -0.1F)).texOffs(0, 0).addBox(-0.5F, -1F, -28.6F, 1F, 2F, 26F, new CubeDeformation(-0.12F, 0.1F, 0F)).texOffs(36, 70).addBox(-0.5F, -0.5F, -29.6F, 1F, 1F, 1F, new CubeDeformation(-0.12F, 0F, 0F)), PartPose.offsetAndRotation(0F, 9.6F, -0.2F, 0.9599F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }
}
