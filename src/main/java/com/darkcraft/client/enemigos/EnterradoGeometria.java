package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class EnterradoGeometria {
    private EnterradoGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(14, 57).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(30, 57).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(46, 57).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(62, 57).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(30, 28).addBox(-4F, -12F, -2.5F, 8F, 12F, 5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(56, 28).addBox(-3.5F, -8F, -3.5F, 7F, 8F, 7F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(78, 57).addBox(-1.5F, -2F, -1.5F, 3F, 7F, 3F), PartPose.offset(-5.1F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(90, 57).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(102, 57).addBox(-1.5F, -2F, -1.5F, 3F, 7F, 3F), PartPose.offset(5.1F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(114, 57).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_gorro = p_cabeza.addOrReplaceChild("gorro", CubeListBuilder.create().texOffs(0, 45).addBox(-3.9F, -8.7F, -3.9F, 7.8F, 3.2F, 7.8F).texOffs(102, 76).addBox(-3.9F, -5.6F, 3.2F, 7.8F, 0.6F, 1.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_candil = p_cabeza.addOrReplaceChild("candil", CubeListBuilder.create().texOffs(54, 76).addBox(-1F, -8.2F, -5.5F, 2F, 2.2F, 1.6F).texOffs(122, 76).addBox(-0.55F, -9.6F, -5.2F, 1.1F, 1.4F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_rodillera_d = p_pierna_d.addOrReplaceChild("rodillera_d", CubeListBuilder.create().texOffs(30, 76).addBox(-2.3F, -0.6F, -2.7F, 4.6F, 3.2F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_rodillera_i = p_pierna_i.addOrReplaceChild("rodillera_i", CubeListBuilder.create().texOffs(42, 76).addBox(-2.3F, -0.6F, -2.7F, 4.6F, 3.2F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_pico = p_mano_d.addOrReplaceChild("pico", CubeListBuilder.create().texOffs(56, 0).addBox(-0.7F, -0.7F, -21F, 1.4F, 1.4F, 24F).texOffs(108, 0).addBox(-0.9F, -8.6F, -22.9F, 1.8F, 17.2F, 2.4F).texOffs(62, 76).addBox(-0.55F, -10.8F, -22.4F, 1.1F, 2.4F, 1.4F).texOffs(70, 76).addBox(-0.55F, 8.4F, -22.4F, 1.1F, 2.4F, 1.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gorra = p_cabeza.addOrReplaceChild("gorra", CubeListBuilder.create().texOffs(32, 45).addBox(-3.8F, -8.8F, -3.8F, 7.6F, 2.6F, 7.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_panuelo = p_cabeza.addOrReplaceChild("panuelo", CubeListBuilder.create().texOffs(64, 45).addBox(-3.8F, -4.4F, -3.95F, 7.6F, 2.6F, 7.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bandolera = p_torso.addOrReplaceChild("bandolera", CubeListBuilder.create().texOffs(0, 68).addBox(-4.35F, -9.6F, -2.85F, 8.7F, 1.8F, 5.7F).texOffs(86, 76).addBox(-3.2F, -10.9F, -3.3F, 1F, 3F, 0.6F).texOffs(90, 76).addBox(-1.6F, -10.9F, -3.3F, 1F, 3F, 0.6F).texOffs(94, 76).addBox(0.6F, -10.9F, -3.3F, 1F, 3F, 0.6F).texOffs(98, 76).addBox(2.2F, -10.9F, -3.3F, 1F, 3F, 0.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_morral = p_torso.addOrReplaceChild("morral", CubeListBuilder.create().texOffs(16, 76).addBox(4.1F, -3.4F, -1.9F, 2.4F, 3.8F, 3.8F).texOffs(30, 68).addBox(-4.3F, -1.2F, -2.7F, 8.6F, 1.2F, 5.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cartucho = p_mano_d.addOrReplaceChild("cartucho", CubeListBuilder.create().texOffs(0, 76).addBox(-0.8F, -0.8F, -5.4F, 1.6F, 1.6F, 5.2F).texOffs(0, 84).addBox(-0.25F, -0.25F, -7F, 0.5F, 0.5F, 1.7F).texOffs(78, 76).addBox(-0.75F, -0.75F, -8.3F, 1.5F, 1.5F, 1.4F), PartPose.offset(0F, 1F, 0F));
        PartDefinition p_panuelo_cabeza = p_cabeza.addOrReplaceChild("panuelo_cabeza", CubeListBuilder.create().texOffs(96, 45).addBox(-3.75F, -8.6F, -3.75F, 7.5F, 2.2F, 7.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_correas = p_torso.addOrReplaceChild("correas", CubeListBuilder.create().texOffs(104, 28).addBox(-3.4F, -12.2F, -2.75F, 1.3F, 12F, 0.4F).texOffs(110, 28).addBox(2.1F, -12.2F, -2.75F, 1.3F, 12F, 0.4F).texOffs(60, 68).addBox(-4.3F, -4.2F, -2.75F, 8.6F, 1.3F, 5.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cuevano = p_torso.addOrReplaceChild("cuevano", CubeListBuilder.create().texOffs(0, 28).addBox(-4.3F, 0F, 0F, 8.6F, 11F, 5.2F).texOffs(90, 68).addBox(-3.9F, -2.6F, 0.4F, 7.8F, 2.8F, 4.4F), PartPose.offset(0F, -11.6F, 2.5F));
        PartDefinition p_almadena = p_mano_d.addOrReplaceChild("almadena", CubeListBuilder.create().texOffs(0, 0).addBox(-0.8F, -0.8F, -23F, 1.6F, 1.6F, 26F).texOffs(84, 28).addBox(-2.5F, -4.6F, -27.6F, 5F, 9.2F, 5F).texOffs(0, 57).addBox(-2.7F, -4.8F, -25.6F, 5.4F, 9.6F, 1F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"gorra", "panuelo", "bandolera", "morral", "cartucho", "panuelo_cabeza", "correas", "cuevano", "almadena"};

    public static final String[] OCULTAS_1 = {"gorro", "candil", "rodillera_d", "rodillera_i", "pico", "panuelo_cabeza", "correas", "cuevano", "almadena"};

    public static final String[] OCULTAS_2 = {"gorro", "candil", "rodillera_d", "rodillera_i", "pico", "gorra", "panuelo", "bandolera", "morral", "cartucho"};
}
