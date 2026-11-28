package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class GalenGeometria {
    private GalenGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -14F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(20, 29).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(-1.5F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(32, 29).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(44, 29).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(1.5F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(56, 29).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -12F, -2.5F, 8F, 12F, 5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(26, 0).addBox(-3.5F, -8F, -3.5F, 7F, 8F, 7F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(68, 29).addBox(-1.5F, -2F, -1.5F, 3F, 7F, 3F), PartPose.offset(-5F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(80, 29).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(92, 29).addBox(-1.5F, -2F, -1.5F, 3F, 7F, 3F), PartPose.offset(5F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(104, 29).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_pelo = p_cabeza.addOrReplaceChild("pelo", CubeListBuilder.create().texOffs(0, 17).addBox(-3.5F, -6.6F, -0.6F, 7F, 6.2F, 4.1F, new CubeDeformation(0.35F, 0.35F, 0.35F)).texOffs(104, 64).addBox(-3F, -1F, 3.5F, 6F, 2.6F, 0.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nariz = p_cabeza.addOrReplaceChild("nariz", CubeListBuilder.create().texOffs(100, 69).addBox(-0.5F, -4.4F, -4.3F, 1F, 2.4F, 0.9F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gafas = p_cabeza.addOrReplaceChild("gafas", CubeListBuilder.create().texOffs(14, 69).addBox(-3.2F, -5.5F, -3.95F, 2.6F, 2.2F, 0.45F).texOffs(22, 69).addBox(0.6F, -5.5F, -3.95F, 2.6F, 2.2F, 0.45F).texOffs(32, 73).addBox(-0.6F, -5F, -3.9F, 1.2F, 0.4F, 0.35F).texOffs(110, 57).addBox(-3.75F, -5F, -3.6F, 0.35F, 0.4F, 3.4F).texOffs(0, 64).addBox(3.4F, -5F, -3.6F, 0.35F, 0.4F, 3.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cuello = p_torso.addOrReplaceChild("cuello", CubeListBuilder.create().texOffs(10, 39).addBox(-4.3F, -14.2F, -2.8F, 8.6F, 2.8F, 5.6F, new CubeDeformation(0.15F, 0.15F, 0.15F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_mandil = p_torso.addOrReplaceChild("mandil", CubeListBuilder.create().texOffs(88, 17).addBox(-3.6F, -9.8F, -2.95F, 7.2F, 10F, 0.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_correas = p_torso.addOrReplaceChild("correas", CubeListBuilder.create().texOffs(26, 57).addBox(-3.6F, -12.45F, -2.85F, 1.1F, 1F, 5.7F).texOffs(42, 57).addBox(2.5F, -12.45F, -2.85F, 1.1F, 1F, 5.7F).texOffs(68, 49).addBox(-3.6F, -12F, -3.05F, 1.1F, 6.6F, 0.4F).texOffs(74, 49).addBox(2.5F, -12F, -3.05F, 1.1F, 6.6F, 0.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cinto = p_torso.addOrReplaceChild("cinto", CubeListBuilder.create().texOffs(98, 39).addBox(-4.4F, -2.1F, -2.9F, 8.8F, 1.3F, 5.8F).texOffs(20, 64).addBox(1.2F, -1.4F, -3.8F, 1.2F, 2.4F, 1.2F).texOffs(28, 64).addBox(2.6F, -1.4F, -3.8F, 1.2F, 2.4F, 1.2F).texOffs(36, 64).addBox(-3.8F, -1.4F, -3.8F, 1.2F, 2.4F, 1.2F).texOffs(48, 49).addBox(3.9F, -2.6F, -1.8F, 1.4F, 3.4F, 3.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_mascara = p_torso.addOrReplaceChild("mascara", CubeListBuilder.create().texOffs(84, 57).addBox(-2.1F, -1.8F, -1.1F, 4.2F, 3.4F, 1.1F).texOffs(60, 49).addBox(-0.9F, 1.2F, -1.9F, 1.8F, 5.2F, 1.5F).texOffs(104, 69).addBox(-1.9F, -1.2F, -1.35F, 1.4F, 1.4F, 0.3F).texOffs(110, 69).addBox(0.5F, -1.2F, -1.35F, 1.4F, 1.4F, 0.3F), PartPose.offsetAndRotation(0F, -8.6F, -3F, -0.2094F, 0F, 0F));
        PartDefinition p_cordel = p_torso.addOrReplaceChild("cordel", CubeListBuilder.create().texOffs(84, 64).addBox(-2.4F, -12.2F, -2.85F, 0.4F, 3.6F, 0.3F).texOffs(88, 64).addBox(2F, -12.2F, -2.85F, 0.4F, 3.6F, 0.3F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon_d = p_muslo_d.addOrReplaceChild("faldon_d", CubeListBuilder.create().texOffs(76, 0).addBox(-2.5F, -0.8F, -1.35F, 5F, 8.2F, 3.95F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bajo_d = p_pierna_d.addOrReplaceChild("bajo_d", CubeListBuilder.create().texOffs(40, 39).addBox(-2.6F, 0F, -1.3F, 5.2F, 4.6F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_mandil_d = p_muslo_d.addOrReplaceChild("mandil_d", CubeListBuilder.create().texOffs(116, 29).addBox(-1.9F, 0F, -2.1F, 3.8F, 8.6F, 0.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_manga_d = p_antebrazo_d.addOrReplaceChild("manga_d", CubeListBuilder.create().texOffs(106, 17).addBox(-2.2F, 0F, -2.2F, 4.4F, 4.2F, 4.4F), PartPose.offset(0F, 0.2F, 0F));
        PartDefinition p_faldon_i = p_muslo_i.addOrReplaceChild("faldon_i", CubeListBuilder.create().texOffs(94, 0).addBox(-2.5F, -0.8F, -1.35F, 5F, 8.2F, 3.95F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bajo_i = p_pierna_i.addOrReplaceChild("bajo_i", CubeListBuilder.create().texOffs(60, 39).addBox(-2.6F, 0F, -1.3F, 5.2F, 4.6F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_mandil_i = p_muslo_i.addOrReplaceChild("mandil_i", CubeListBuilder.create().texOffs(0, 39).addBox(-1.9F, 0F, -2.1F, 3.8F, 8.6F, 0.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_manga_i = p_antebrazo_i.addOrReplaceChild("manga_i", CubeListBuilder.create().texOffs(0, 29).addBox(-2.2F, 0F, -2.2F, 4.4F, 4.2F, 4.4F), PartPose.offset(0F, 0.2F, 0F));
        PartDefinition p_alambique = p_torso.addOrReplaceChild("alambique", CubeListBuilder.create().texOffs(48, 17).addBox(-3.6F, -5.2F, 0F, 7.2F, 10.2F, 0.9F).texOffs(54, 0).addBox(-3F, -4.4F, 0.9F, 6F, 7.6F, 5F).texOffs(80, 49).addBox(-3.3F, -4.8F, 0.7F, 6.6F, 0.8F, 5.4F).texOffs(0, 57).addBox(-3.3F, 2.8F, 0.7F, 6.6F, 0.8F, 5.4F).texOffs(98, 57).addBox(-1F, -7.8F, 2.4F, 2F, 3.4F, 2F).texOffs(0, 69).addBox(-1.6F, -8.8F, 1.9F, 3.2F, 1F, 3F).texOffs(24, 73).addBox(2F, -7.2F, 2.9F, 2.4F, 0.9F, 0.9F), PartPose.offset(0F, -6.6F, 2.5F));
        PartDefinition p_elixir = p_alambique.addOrReplaceChild("elixir", CubeListBuilder.create().texOffs(66, 17).addBox(-2.6F, -2.6F, 1.3F, 5.2F, 5.6F, 4.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tubos = p_torso.addOrReplaceChild("tubos", CubeListBuilder.create().texOffs(0, 49).addBox(3.65F, -16F, -0.2F, 0.9F, 0.9F, 6.3F).texOffs(92, 64).addBox(3.65F, -16F, -0.6F, 0.9F, 3.6F, 0.9F).texOffs(16, 49).addBox(-4.55F, -16F, -0.2F, 0.9F, 0.9F, 6.3F).texOffs(96, 64).addBox(-4.55F, -16F, -0.6F, 0.9F, 3.6F, 0.9F).texOffs(4, 73).addBox(-1.95F, -16F, 5.2F, 3.9F, 0.9F, 0.9F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tubos_rotos = p_torso.addOrReplaceChild("tubos_rotos", CubeListBuilder.create().texOffs(106, 57).addBox(1.3F, -15.4F, 4.9F, 0.9F, 4.6F, 0.9F).texOffs(100, 64).addBox(-2.2F, -15.4F, 5.3F, 0.9F, 3.2F, 0.9F).texOffs(116, 69).addBox(3.55F, -13.6F, -0.6F, 0.9F, 1.4F, 0.9F).texOffs(120, 69).addBox(-4.45F, -13.6F, -0.6F, 0.9F, 1.4F, 0.9F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cristal_espalda = p_torso.addOrReplaceChild("cristal_espalda", CubeListBuilder.create().texOffs(44, 64).addBox(-4.4F, -11.8F, 1.4F, 1.6F, 2.6F, 1.6F).texOffs(52, 64).addBox(3F, -10F, 1.8F, 1.4F, 2.2F, 1.4F).texOffs(30, 69).addBox(-4.6F, -6F, 0.8F, 1.2F, 1.8F, 1.2F).texOffs(38, 69).addBox(-1F, -1.6F, 2.4F, 1.6F, 1.4F, 1.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cristal_hombro_d = p_brazo_d.addOrReplaceChild("cristal_hombro_d", CubeListBuilder.create().texOffs(60, 64).addBox(-2.6F, -1.6F, -0.8F, 1.4F, 2.8F, 1.6F).texOffs(124, 69).addBox(-2F, 1.6F, 0.6F, 1F, 1.8F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cristal_hombro_i = p_brazo_i.addOrReplaceChild("cristal_hombro_i", CubeListBuilder.create().texOffs(68, 64).addBox(1.2F, -1.8F, -0.6F, 1.6F, 3F, 1.6F).texOffs(0, 73).addBox(1F, 2F, -1.4F, 1F, 1.4F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cristal_brazo = p_antebrazo_i.addOrReplaceChild("cristal_brazo", CubeListBuilder.create().texOffs(76, 64).addBox(1F, 0.8F, -0.4F, 1.4F, 2.2F, 1.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cristal_cabeza = p_cabeza.addOrReplaceChild("cristal_cabeza", CubeListBuilder.create().texOffs(46, 69).addBox(1.6F, -9.6F, 0.2F, 1.4F, 2F, 1.4F).texOffs(54, 69).addBox(-3F, -9F, 1.4F, 1.2F, 1.4F, 1.2F).texOffs(94, 69).addBox(-4.1F, -3.4F, -1.6F, 0.8F, 1.6F, 1.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_jeringa = p_mano_d.addOrReplaceChild("jeringa", CubeListBuilder.create().texOffs(62, 69).addBox(-1.2F, -1.2F, 3.2F, 2.4F, 2.4F, 0.6F).texOffs(70, 69).addBox(-0.4F, -0.4F, 0.4F, 0.8F, 0.8F, 2.8F).texOffs(14, 73).addBox(-2F, -0.5F, -0.4F, 4F, 1F, 0.8F).texOffs(24, 17).addBox(-1.3F, -1.3F, -9.4F, 2.6F, 2.6F, 9F).texOffs(78, 69).addBox(-1.5F, -1.5F, -10.4F, 3F, 3F, 1F).texOffs(58, 57).addBox(-0.25F, -0.25F, -16.4F, 0.5F, 0.5F, 6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_jeringa_elixir = p_jeringa.addOrReplaceChild("jeringa_elixir", CubeListBuilder.create().texOffs(80, 39).addBox(-1F, -1F, -9F, 2F, 2F, 6.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bisturi = p_mano_i.addOrReplaceChild("bisturi", CubeListBuilder.create().texOffs(10, 64).addBox(-0.4F, -0.4F, -3.2F, 0.8F, 0.8F, 3.6F).texOffs(72, 57).addBox(-0.15F, -0.75F, -7.8F, 0.3F, 1.5F, 4.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_frasco_grande = p_mano_i.addOrReplaceChild("frasco_grande", CubeListBuilder.create().texOffs(32, 49).addBox(-1.7F, -1.7F, -3.8F, 3.4F, 3.4F, 3.8F).texOffs(86, 69).addBox(-0.7F, -0.7F, -5.3F, 1.4F, 1.4F, 1.6F), PartPose.offset(0F, 0.4F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"cristal_espalda", "cristal_hombro_d", "cristal_hombro_i", "cristal_brazo", "cristal_cabeza", "tubos_rotos", "frasco_grande"};

    public static final String[] OCULTAS_1 = {"tubos", "frasco_grande"};
}
