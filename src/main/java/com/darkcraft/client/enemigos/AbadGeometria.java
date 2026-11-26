package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class AbadGeometria {
    private AbadGeometria() {}

    public static final int TEX_W = 128, TEX_H = 256;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -14F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(48, 82).addBox(-2.5F, 0F, -2.5F, 5F, 7F, 5F), PartPose.offset(-2.5F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(68, 82).addBox(-2.5F, 0F, -2.5F, 5F, 7F, 5F), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(88, 82).addBox(-2.5F, 0F, -2.5F, 5F, 7F, 5F), PartPose.offset(2.5F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(108, 82).addBox(-2.5F, 0F, -2.5F, 5F, 7F, 5F), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 47).addBox(-5.5F, -14F, -3F, 11F, 14F, 6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(34, 47).addBox(-4F, -9F, -4F, 8F, 9F, 8F), PartPose.offset(0F, -14F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(0, 95).addBox(-2F, -2F, -2F, 4F, 8F, 4F), PartPose.offset(-7.1F, -12F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(16, 95).addBox(-2F, 0F, -2F, 4F, 8F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(32, 95).addBox(-2F, -2F, -2F, 4F, 8F, 4F), PartPose.offset(7.1F, -12F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(48, 95).addBox(-2F, 0F, -2F, 4F, 8F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_mitra = p_cabeza.addOrReplaceChild("mitra", CubeListBuilder.create().texOffs(76, 95).addBox(-4.5F, -1.6F, -4.5F, 9F, 1.6F, 9F).texOffs(0, 107).addBox(-4F, -10F, -3.4F, 8F, 8.4F, 1.4F).texOffs(20, 107).addBox(-4F, -10F, 2F, 8F, 8.4F, 1.4F).texOffs(52, 107).addBox(-3.4F, -7.6F, -2F, 6.8F, 6F, 4F).texOffs(72, 118).addBox(-0.7F, -13F, -0.7F, 1.4F, 3.6F, 1.4F).texOffs(90, 118).addBox(-3.2F, -10.6F, -0.6F, 1.2F, 2.4F, 1.2F).texOffs(98, 118).addBox(2F, -10.6F, -0.6F, 1.2F, 2.4F, 1.2F), PartPose.offset(0F, -9F, 0F));
        PartDefinition p_infulas = p_cabeza.addOrReplaceChild("infulas", CubeListBuilder.create().texOffs(40, 107).addBox(-3.2F, 0F, 0F, 1.6F, 10F, 0.5F).texOffs(46, 107).addBox(1.6F, 0F, 0F, 1.6F, 10F, 0.5F), PartPose.offsetAndRotation(0F, -8.6F, 4.4F, 0.1396F, 0F, 0F));
        PartDefinition p_barba = p_cabeza.addOrReplaceChild("barba", CubeListBuilder.create().texOffs(94, 107).addBox(-3.4F, 0F, -0.9F, 6.8F, 7.6F, 1.4F).texOffs(8, 127).addBox(-2.6F, 7.6F, -0.7F, 1F, 2.6F, 1F).texOffs(106, 118).addBox(-0.5F, 7.6F, -0.7F, 1F, 3.6F, 1F).texOffs(12, 127).addBox(1.6F, 7.6F, -0.7F, 1F, 2.2F, 1F), PartPose.offsetAndRotation(0F, -2.6F, -4.2F, -0.1047F, 0F, 0F));
        PartDefinition p_capa_hombros = p_torso.addOrReplaceChild("capa_hombros", CubeListBuilder.create().texOffs(84, 67).addBox(-6.9F, -0.8F, -3.9F, 13.8F, 5.4F, 7.8F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(0F, -14F, 0F));
        PartDefinition p_capa = p_torso.addOrReplaceChild("capa", CubeListBuilder.create().texOffs(94, 0).addBox(-6.7F, 0F, 0F, 13.4F, 27F, 0.9F).texOffs(0, 118).addBox(-3.6F, 2F, 0.9F, 7.2F, 7.4F, 0.5F), PartPose.offsetAndRotation(0F, -13.6F, 3.4F, 0.0873F, 0F, 0F));
        PartDefinition p_broche = p_torso.addOrReplaceChild("broche", CubeListBuilder.create().texOffs(114, 118).addBox(-1.6F, 0F, -0.6F, 3.2F, 2.4F, 0.7F), PartPose.offset(0F, -12.6F, -3.4F));
        PartDefinition p_estola = p_torso.addOrReplaceChild("estola", CubeListBuilder.create().texOffs(66, 47).addBox(-3.3F, 0F, -0.4F, 1.8F, 16F, 0.4F).texOffs(72, 47).addBox(1.5F, 0F, -0.4F, 1.8F, 16F, 0.4F), PartPose.offset(0F, -13.2F, -3.25F));
        PartDefinition p_cruz = p_torso.addOrReplaceChild("cruz", CubeListBuilder.create().texOffs(110, 118).addBox(-0.45F, 0F, -0.4F, 0.9F, 3.8F, 0.4F).texOffs(36, 127).addBox(-1.4F, 0.8F, -0.4F, 2.8F, 0.9F, 0.4F), PartPose.offset(0F, -9.4F, -3.7F));
        PartDefinition p_cristal_a = p_torso.addOrReplaceChild("cristal_a", CubeListBuilder.create().texOffs(112, 107).addBox(-1F, -7.5F, -1F, 2F, 7.5F, 2F), PartPose.offsetAndRotation(-4F, -12.5F, 2.6F, -0.4887F, 0F, -0.4189F));
        PartDefinition p_cristal_b = p_torso.addOrReplaceChild("cristal_b", CubeListBuilder.create().texOffs(26, 118).addBox(-0.8F, -5.8F, -0.8F, 1.6F, 5.8F, 1.6F), PartPose.offsetAndRotation(4.4F, -11.8F, 2.6F, -0.5934F, 0F, 0.4538F));
        PartDefinition p_cristal_c = p_torso.addOrReplaceChild("cristal_c", CubeListBuilder.create().texOffs(64, 95).addBox(-1.1F, -8.6F, -1.1F, 2.2F, 8.6F, 2.2F), PartPose.offsetAndRotation(0.6F, -8.6F, 3F, -0.7679F, 0F, 0.1047F));
        PartDefinition p_alba_d = p_muslo_d.addOrReplaceChild("alba_d", CubeListBuilder.create().texOffs(78, 47).addBox(-3.2F, -0.6F, -3.2F, 6.4F, 7.8F, 6.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_alba_bajo_d = p_pierna_d.addOrReplaceChild("alba_bajo_d", CubeListBuilder.create().texOffs(0, 67).addBox(-3.1F, 0F, -3.1F, 6.2F, 7.6F, 6.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_manga_d = p_antebrazo_d.addOrReplaceChild("manga_d", CubeListBuilder.create().texOffs(0, 82).addBox(-2.8F, 0F, -2.8F, 5.6F, 6.2F, 5.6F).texOffs(16, 127).addBox(-1.9F, 6.2F, -1.2F, 0.9F, 2.4F, 0.9F).texOffs(28, 127).addBox(0.9F, 6.2F, 0.6F, 0.9F, 1.8F, 0.9F), PartPose.offset(0F, 1.2F, 0F));
        PartDefinition p_alba_i = p_muslo_i.addOrReplaceChild("alba_i", CubeListBuilder.create().texOffs(28, 67).addBox(-3.2F, -0.6F, -3.2F, 6.4F, 7.8F, 6.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_alba_bajo_i = p_pierna_i.addOrReplaceChild("alba_bajo_i", CubeListBuilder.create().texOffs(56, 67).addBox(-3.1F, 0F, -3.1F, 6.2F, 7.6F, 6.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_manga_i = p_antebrazo_i.addOrReplaceChild("manga_i", CubeListBuilder.create().texOffs(24, 82).addBox(-2.8F, 0F, -2.8F, 5.6F, 6.2F, 5.6F).texOffs(20, 127).addBox(-1.9F, 6.2F, -1.2F, 0.9F, 2.4F, 0.9F).texOffs(32, 127).addBox(0.9F, 6.2F, 0.6F, 0.9F, 1.8F, 0.9F), PartPose.offset(0F, 1.2F, 0F));
        PartDefinition p_baculo = p_mano_d.addOrReplaceChild("baculo", CubeListBuilder.create().texOffs(0, 0).addBox(-0.85F, -0.85F, -30F, 1.7F, 1.7F, 45F).texOffs(80, 118).addBox(-1.05F, -1.05F, 15F, 2.1F, 2.1F, 1.6F).texOffs(48, 118).addBox(-1.35F, -1.35F, -32F, 2.7F, 2.7F, 2.2F).texOffs(74, 107).addBox(-1F, -1F, -39F, 2F, 2F, 7.2F).texOffs(18, 118).addBox(-1F, -6.6F, -41F, 2F, 6.6F, 2F).texOffs(34, 118).addBox(-1F, -7.6F, -39.4F, 2F, 2F, 4.6F).texOffs(0, 127).addBox(-1F, -5.8F, -35.2F, 2F, 1.8F, 1.4F).texOffs(60, 118).addBox(-1.3F, -5.2F, -38.8F, 2.6F, 2.6F, 2.6F).texOffs(44, 127).addBox(-0.45F, -8.4F, -37F, 0.9F, 1F, 0.9F).texOffs(24, 127).addBox(-0.45F, 1F, -34.6F, 0.9F, 2.2F, 0.9F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {};
}
