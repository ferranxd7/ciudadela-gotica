package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class HeladoGeometria {
    private HeladoGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(108, 97).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(0, 110).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(16, 110).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(32, 110).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(20, 65).addBox(-4F, -12F, -2.5F, 8F, 12F, 5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(46, 65).addBox(-3.5F, -8F, -3.5F, 7F, 8F, 7F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(48, 110).addBox(-1.5F, -2F, -1.5F, 3F, 7F, 3F), PartPose.offset(-5.1F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(60, 110).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(72, 110).addBox(-1.5F, -2F, -1.5F, 3F, 7F, 3F), PartPose.offset(5.1F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(84, 110).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_carambano_d = p_antebrazo_d.addOrReplaceChild("carambano_d", CubeListBuilder.create().texOffs(78, 120).addBox(-1.4F, 0F, 1.6F, 0.8F, 5F, 0F).texOffs(90, 120).addBox(0.4F, 0F, 1.6F, 0.8F, 3.5F, 0F), PartPose.offset(0F, 3F, 0F));
        PartDefinition p_carambano_i = p_antebrazo_i.addOrReplaceChild("carambano_i", CubeListBuilder.create().texOffs(80, 120).addBox(-1.4F, 0F, 1.6F, 0.8F, 5F, 0F).texOffs(92, 120).addBox(0.4F, 0F, 1.6F, 0.8F, 3.5F, 0F), PartPose.offset(0F, 3F, 0F));
        PartDefinition p_barba = p_cabeza.addOrReplaceChild("barba", CubeListBuilder.create().texOffs(46, 120).addBox(-2.5F, -1.5F, -0.2F, 5F, 5.5F, 0.8F), PartPose.offset(0F, 0F, -3.6F));
        PartDefinition p_capucha = p_cabeza.addOrReplaceChild("capucha", CubeListBuilder.create().texOffs(74, 97).addBox(-4F, -0.8F, -4.2F, 8F, 1F, 8.6F).texOffs(108, 38).addBox(-4F, -0.8F, -4.2F, 0.8F, 8.6F, 8.6F).texOffs(0, 65).addBox(3.2F, -0.8F, -4.2F, 0.8F, 8.6F, 8.6F).texOffs(56, 97).addBox(-4F, -0.8F, 3.6F, 8F, 9.6F, 0.8F), PartPose.offset(0F, -8F, 0F));
        PartDefinition p_escapulario = p_torso.addOrReplaceChild("escapulario", CubeListBuilder.create().texOffs(74, 65).addBox(-2.5F, 0F, -2.9F, 5F, 14F, 0.4F).texOffs(86, 65).addBox(-2.5F, 0F, 2.5F, 5F, 14F, 0.4F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_habito_d = p_muslo_d.addOrReplaceChild("habito_d", CubeListBuilder.create().texOffs(0, 83).addBox(-2.7F, -0.6F, -2.7F, 5.4F, 7.6F, 5.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_habito_bajo_d = p_pierna_d.addOrReplaceChild("habito_bajo_d", CubeListBuilder.create().texOffs(96, 83).addBox(-2.6F, 0F, -2.6F, 5.2F, 6.4F, 5.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_habito_i = p_muslo_i.addOrReplaceChild("habito_i", CubeListBuilder.create().texOffs(24, 83).addBox(-2.7F, -0.6F, -2.7F, 5.4F, 7.6F, 5.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_habito_bajo_i = p_pierna_i.addOrReplaceChild("habito_bajo_i", CubeListBuilder.create().texOffs(0, 97).addBox(-2.6F, 0F, -2.6F, 5.2F, 6.4F, 5.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_incensario = p_mano_d.addOrReplaceChild("incensario", CubeListBuilder.create().texOffs(74, 120).addBox(-0.4F, 0F, -0.4F, 0.8F, 5F, 0.8F).texOffs(94, 120).addBox(-1F, 4.5F, -1F, 2F, 0.6F, 2F).texOffs(30, 120).addBox(-1.8F, 5F, -1.8F, 3.6F, 3.2F, 3.6F).texOffs(102, 120).addBox(-1F, 8.2F, -1F, 2F, 0.8F, 2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cadenas = p_torso.addOrReplaceChild("cadenas", CubeListBuilder.create().texOffs(0, 120).addBox(-4.4F, -2.2F, -2.9F, 8.8F, 1.2F, 5.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cadena_a = p_torso.addOrReplaceChild("cadena_a", CubeListBuilder.create().texOffs(76, 38).addBox(-0.6F, -6.4F, -2.9F, 1.2F, 12.8F, 5.8F), PartPose.offsetAndRotation(0F, -6.5F, 0F, 0F, 0F, 0.6632F));
        PartDefinition p_cadena_b = p_torso.addOrReplaceChild("cadena_b", CubeListBuilder.create().texOffs(92, 38).addBox(-0.6F, -6.4F, -2.9F, 1.2F, 12.8F, 5.8F), PartPose.offsetAndRotation(0F, -6.5F, 0F, 0F, 0F, -0.6632F));
        PartDefinition p_cristales = p_torso.addOrReplaceChild("cristales", CubeListBuilder.create().texOffs(58, 120).addBox(-3.5F, -4F, 0F, 2F, 5F, 2F).texOffs(96, 110).addBox(-0.5F, -5.5F, 0.4F, 2.2F, 6.5F, 2.2F).texOffs(66, 120).addBox(2F, -3F, 0F, 1.8F, 4F, 1.8F), PartPose.offset(0F, -9F, 2.5F));
        PartDefinition p_martillo = p_mano_d.addOrReplaceChild("martillo", CubeListBuilder.create().texOffs(0, 38).addBox(-0.8F, -0.8F, -20F, 1.6F, 1.6F, 25F).texOffs(98, 65).addBox(-3F, -3.5F, -26F, 6F, 7F, 6.5F).texOffs(82, 120).addBox(-1F, -1F, 5F, 2F, 2F, 1.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_sombrero = p_cabeza.addOrReplaceChild("sombrero", CubeListBuilder.create().texOffs(48, 83).addBox(-6F, -0.6F, -6F, 12F, 1F, 12F).texOffs(24, 97).addBox(-3.8F, -4.2F, -3.8F, 7.6F, 3.8F, 7.6F), PartPose.offset(0F, -8F, 0F));
        PartDefinition p_capa_viaje = p_torso.addOrReplaceChild("capa_viaje", CubeListBuilder.create().texOffs(54, 38).addBox(-5F, 0F, 0F, 10F, 19F, 0.8F), PartPose.offsetAndRotation(0F, -12F, 2.7F, 0.0698F, 0F, 0F));
        PartDefinition p_baston = p_mano_d.addOrReplaceChild("baston", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6F, -0.6F, -22F, 1.2F, 1.2F, 36F).texOffs(108, 110).addBox(-1.6F, -1.6F, -26F, 3.2F, 3.2F, 4.2F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"cadenas", "cadena_a", "cadena_b", "cristales", "martillo", "sombrero", "capa_viaje", "baston"};

    public static final String[] OCULTAS_1 = {"capucha", "escapulario", "habito_d", "habito_i", "habito_bajo_d", "habito_bajo_i", "incensario", "sombrero", "capa_viaje", "baston"};

    public static final String[] OCULTAS_2 = {"cadenas", "cadena_a", "cadena_b", "cristales", "martillo", "escapulario", "incensario", "capucha"};
}
