package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class DestiladoGeometria {
    private DestiladoGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(22, 73).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(38, 73).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(54, 73).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(70, 73).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(62, 0).addBox(-4F, -12F, -2.5F, 8F, 12F, 5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(22, 21).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(86, 73).addBox(-1.5F, -2F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(-5.1F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(98, 73).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(110, 73).addBox(-1.5F, -2F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(5.1F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(0, 83).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_sombrero = p_cabeza.addOrReplaceChild("sombrero", CubeListBuilder.create().texOffs(54, 21).addBox(-6.5F, -7.7F, -6.5F, 13F, 0.8F, 13F).texOffs(0, 38).addBox(-4.3F, -11.4F, -4.3F, 8.6F, 3.8F, 8.6F).texOffs(0, 63).addBox(-4.45F, -8.5F, -4.45F, 8.9F, 0.9F, 8.9F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_pico = p_cabeza.addOrReplaceChild("pico", CubeListBuilder.create().texOffs(0, 73).addBox(-1.6F, -1.6F, -6.4F, 3.2F, 3F, 6.6F).texOffs(112, 93).addBox(-0.9F, -0.9F, -9.2F, 1.8F, 2F, 2.9F), PartPose.offsetAndRotation(0F, -3F, -4F, 0.2793F, 0F, 0F));
        PartDefinition p_lentes = p_cabeza.addOrReplaceChild("lentes", CubeListBuilder.create().texOffs(32, 101).addBox(-3.5F, -6F, -4.5F, 2.5F, 2.5F, 0.6F).texOffs(40, 101).addBox(1F, -6F, -4.5F, 2.5F, 2.5F, 0.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_capelina = p_torso.addOrReplaceChild("capelina", CubeListBuilder.create().texOffs(62, 38).addBox(-4.9F, -12.5F, -3.1F, 9.8F, 4.6F, 6.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cinturon = p_torso.addOrReplaceChild("cinturon", CubeListBuilder.create().texOffs(0, 93).addBox(-4.4F, -2.2F, -2.9F, 8.8F, 1.4F, 5.8F).texOffs(0, 101).addBox(-3.4F, -1.6F, -3.7F, 1.2F, 2.2F, 1.2F).texOffs(8, 101).addBox(-1.7F, -1.6F, -3.7F, 1.2F, 2.2F, 1.2F).texOffs(16, 101).addBox(2.3F, -1.6F, -3.7F, 1.2F, 2.2F, 1.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_zurron = p_torso.addOrReplaceChild("zurron", CubeListBuilder.create().texOffs(30, 93).addBox(3.7F, -3.8F, -1.6F, 2.2F, 3.4F, 3.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon_d = p_muslo_d.addOrReplaceChild("faldon_d", CubeListBuilder.create().texOffs(88, 0).addBox(-2.4F, -0.6F, -2.6F, 4.8F, 10.4F, 5.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon_i = p_muslo_i.addOrReplaceChild("faldon_i", CubeListBuilder.create().texOffs(0, 21).addBox(-2.4F, -0.6F, -2.6F, 4.8F, 10.4F, 5.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_mortero = p_mano_d.addOrReplaceChild("mortero", CubeListBuilder.create().texOffs(36, 38).addBox(-0.7F, -0.7F, -9.4F, 1.4F, 1.4F, 11F).texOffs(36, 83).addBox(-1.7F, -1.7F, -13.6F, 3.4F, 3.4F, 4.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_frasco_mano = p_mano_i.addOrReplaceChild("frasco_mano", CubeListBuilder.create().texOffs(70, 93).addBox(-1.5F, -1.5F, -3.2F, 3F, 3F, 3F).texOffs(48, 101).addBox(-0.6F, -0.6F, -4.6F, 1.2F, 1.2F, 1.5F).texOffs(4, 106).addBox(-0.75F, -0.75F, -5.2F, 1.5F, 1.5F, 0.6F), PartPose.offset(0F, 0.6F, 0F));
        PartDefinition p_faldon_bata_d = p_muslo_d.addOrReplaceChild("faldon_bata_d", CubeListBuilder.create().texOffs(96, 38).addBox(-2.3F, -0.5F, -2.45F, 4.6F, 6.6F, 4.9F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon_bata_i = p_muslo_i.addOrReplaceChild("faldon_bata_i", CubeListBuilder.create().texOffs(0, 51).addBox(-2.3F, -0.5F, -2.45F, 4.6F, 6.6F, 4.9F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_venda_cabeza = p_cabeza.addOrReplaceChild("venda_cabeza", CubeListBuilder.create().texOffs(40, 51).addBox(-4.3F, -7.7F, -4.3F, 8.6F, 1.7F, 8.6F).texOffs(106, 93).addBox(1.6F, -7F, 4.2F, 1.6F, 4.4F, 0.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_venda_brazo = p_antebrazo_d.addOrReplaceChild("venda_brazo", CubeListBuilder.create().texOffs(54, 83).addBox(-1.8F, 0.4F, -1.8F, 3.6F, 4.6F, 3.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bubas = p_torso.addOrReplaceChild("bubas", CubeListBuilder.create().texOffs(10, 106).addBox(-3.3F, -9.8F, -3.3F, 2F, 2F, 0.9F).texOffs(16, 106).addBox(1.3F, -5.6F, -3.2F, 1.6F, 1.6F, 0.8F).texOffs(56, 101).addBox(-1F, -8.6F, 2.4F, 2.2F, 2.2F, 1F).texOffs(22, 106).addBox(1.9F, -11F, 2.4F, 1.6F, 1.6F, 0.8F).texOffs(88, 101).addBox(3.9F, -3.4F, -0.8F, 0.8F, 1.6F, 1.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bubas_cabeza = p_cabeza.addOrReplaceChild("bubas_cabeza", CubeListBuilder.create().texOffs(28, 106).addBox(1.8F, -2.6F, -4.6F, 1.4F, 1.4F, 0.7F).texOffs(94, 101).addBox(-4.6F, -5F, -1.2F, 0.7F, 1.6F, 1.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bubas_brazo = p_brazo_i.addOrReplaceChild("bubas_brazo", CubeListBuilder.create().texOffs(100, 101).addBox(1.2F, 0.4F, -1F, 0.8F, 1.8F, 1.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bubas_pierna = p_muslo_d.addOrReplaceChild("bubas_pierna", CubeListBuilder.create().texOffs(34, 106).addBox(-1F, 2.2F, -2.7F, 1.8F, 1.8F, 0.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_garras_d = p_mano_d.addOrReplaceChild("garras_d", CubeListBuilder.create().texOffs(106, 101).addBox(-1.4F, 0.4F, -1.9F, 0.5F, 2.4F, 0.5F).texOffs(110, 101).addBox(-0.25F, 0.6F, -1.9F, 0.5F, 2.6F, 0.5F).texOffs(114, 101).addBox(0.9F, 0.4F, -1.9F, 0.5F, 2.4F, 0.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_garras_i = p_mano_i.addOrReplaceChild("garras_i", CubeListBuilder.create().texOffs(118, 101).addBox(-1.4F, 0.4F, -1.9F, 0.5F, 2.4F, 0.5F).texOffs(122, 101).addBox(-0.25F, 0.6F, -1.9F, 0.5F, 2.6F, 0.5F).texOffs(0, 106).addBox(0.9F, 0.4F, -1.9F, 0.5F, 2.4F, 0.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cuba = p_torso.addOrReplaceChild("cuba", CubeListBuilder.create().texOffs(0, 0).addBox(-4.6F, -3.4F, 0F, 9.2F, 14F, 7F).texOffs(36, 63).addBox(-5F, -3.9F, -0.4F, 10F, 1.2F, 7.8F).texOffs(72, 63).addBox(-5F, 9.8F, -0.4F, 10F, 1.2F, 7.8F).texOffs(44, 93).addBox(-3.6F, -4.7F, 1F, 7.2F, 0.9F, 5F).texOffs(64, 101).addBox(-0.8F, -6.2F, 2.7F, 1.6F, 1.6F, 1.6F), PartPose.offset(0F, -12F, 2.5F));
        PartDefinition p_liquido = p_cuba.addOrReplaceChild("liquido", CubeListBuilder.create().texOffs(34, 0).addBox(-4F, -0.4F, 0.6F, 8F, 10.4F, 5.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tubos = p_torso.addOrReplaceChild("tubos", CubeListBuilder.create().texOffs(82, 93).addBox(-3.9F, -15.6F, -1.3F, 1F, 1F, 4.4F).texOffs(24, 101).addBox(-3.9F, -15F, -1.5F, 1F, 3.8F, 1F).texOffs(94, 93).addBox(2.9F, -15.6F, -1.3F, 1F, 1F, 4.4F).texOffs(28, 101).addBox(2.9F, -15F, -1.5F, 1F, 3.8F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_mandil = p_torso.addOrReplaceChild("mandil", CubeListBuilder.create().texOffs(20, 51).addBox(-4.2F, -10.4F, -2.95F, 8.4F, 10.8F, 0.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_mandil_d = p_muslo_d.addOrReplaceChild("mandil_d", CubeListBuilder.create().texOffs(12, 83).addBox(-2.1F, 0F, -2.6F, 4.2F, 8.6F, 0.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_guante_d = p_antebrazo_d.addOrReplaceChild("guante_d", CubeListBuilder.create().texOffs(70, 83).addBox(-2F, 2.2F, -2F, 4F, 4.8F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_mandil_i = p_muslo_i.addOrReplaceChild("mandil_i", CubeListBuilder.create().texOffs(24, 83).addBox(-2.1F, 0F, -2.6F, 4.2F, 8.6F, 0.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_guante_i = p_antebrazo_i.addOrReplaceChild("guante_i", CubeListBuilder.create().texOffs(86, 83).addBox(-2F, 2.2F, -2F, 4F, 4.8F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gafas = p_cabeza.addOrReplaceChild("gafas", CubeListBuilder.create().texOffs(76, 51).addBox(-4.25F, -6.2F, -4.25F, 8.5F, 1.4F, 8.5F).texOffs(72, 101).addBox(-3.5F, -6.7F, -4.75F, 2.7F, 2.4F, 0.6F).texOffs(80, 101).addBox(0.8F, -6.7F, -4.75F, 2.7F, 2.4F, 0.6F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"faldon_bata_d", "faldon_bata_i", "venda_cabeza", "venda_brazo", "bubas", "bubas_cabeza", "bubas_brazo", "bubas_pierna", "garras_d", "garras_i", "cuba", "tubos", "mandil", "mandil_d", "mandil_i", "guante_d", "guante_i", "gafas"};

    public static final String[] OCULTAS_1 = {"sombrero", "pico", "lentes", "capelina", "cinturon", "zurron", "faldon_d", "faldon_i", "mortero", "frasco_mano", "cuba", "tubos", "mandil", "mandil_d", "mandil_i", "guante_d", "guante_i", "gafas"};

    public static final String[] OCULTAS_2 = {"sombrero", "pico", "lentes", "capelina", "cinturon", "zurron", "faldon_d", "faldon_i", "mortero", "frasco_mano", "faldon_bata_d", "faldon_bata_i", "venda_cabeza", "venda_brazo", "bubas", "bubas_cabeza", "bubas_brazo", "bubas_pierna", "garras_d", "garras_i"};
}
