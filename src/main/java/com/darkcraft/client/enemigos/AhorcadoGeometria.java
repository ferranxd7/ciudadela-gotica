package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class AhorcadoGeometria {
    private AhorcadoGeometria() {}

    public static final int TEX_W = 128, TEX_H = 64;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -14F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(50, 17).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(-1.5F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(62, 17).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(74, 17).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(1.5F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(86, 17).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -13F, -2F, 7F, 13F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(50, 0).addBox(-3.5F, -8F, -3.5F, 7F, 8F, 7F), PartPose.offset(0F, -14.5F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(104, 0).addBox(-1.5F, -2F, -1.5F, 3F, 9F, 3F), PartPose.offset(-5F, -11F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(116, 0).addBox(-1.5F, 0F, -1.5F, 3F, 9F, 3F), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 8F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(0, 17).addBox(-1.5F, -2F, -1.5F, 3F, 9F, 3F), PartPose.offset(5F, -11F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(12, 17).addBox(-1.5F, 0F, -1.5F, 3F, 9F, 3F), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 8F, 0F));
        PartDefinition p_faldon = p_cadera.addOrReplaceChild("faldon", CubeListBuilder.create().texOffs(24, 17).addBox(-4F, -0.5F, -2.5F, 8F, 5F, 5F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_soga_cuello = p_cabeza.addOrReplaceChild("soga_cuello", CubeListBuilder.create().texOffs(98, 17).addBox(-3.5F, -1.5F, -3.5F, 7F, 2F, 7F, new CubeDeformation(0.25F, 0.25F, 0.25F)).texOffs(58, 29).addBox(2.6F, -2.2F, -1F, 2F, 2F, 2F), PartPose.offset(0F, 1F, 0F));
        PartDefinition p_cabo = p_torso.addOrReplaceChild("cabo", CubeListBuilder.create().texOffs(18, 29).addBox(-0.5F, 0F, 0F, 1F, 7F, 1F), PartPose.offset(0F, -13F, 2.2F));
        PartDefinition p_saco = p_cabeza.addOrReplaceChild("saco", CubeListBuilder.create().texOffs(22, 0).addBox(-3.5F, -8.5F, -3.5F, 7F, 9F, 7F, new CubeDeformation(0.55F, 0.55F, 0.55F)).texOffs(46, 29).addBox(-1.5F, -10F, -1.5F, 3F, 2F, 3F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_unas_d = p_mano_d.addOrReplaceChild("unas_d", CubeListBuilder.create().texOffs(74, 29).addBox(-1.5F, -0.5F, -0.5F, 3F, 4F, 0F).texOffs(86, 29).addBox(-1.5F, -0.5F, 0.5F, 3F, 3F, 0F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_unas_i = p_mano_i.addOrReplaceChild("unas_i", CubeListBuilder.create().texOffs(80, 29).addBox(-1.5F, -0.5F, -0.5F, 3F, 4F, 0F).texOffs(92, 29).addBox(-1.5F, -0.5F, 0.5F, 3F, 3F, 0F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_soga_mano = p_mano_d.addOrReplaceChild("soga_mano", CubeListBuilder.create().texOffs(78, 0).addBox(-0.5F, -0.5F, -12F, 1F, 1F, 12F).texOffs(0, 29).addBox(-0.5F, -0.5F, -20F, 1F, 1F, 8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lazo = p_soga_mano.addOrReplaceChild("lazo", CubeListBuilder.create().texOffs(98, 29).addBox(-2.5F, -0.5F, -5F, 5F, 1F, 1F).texOffs(22, 29).addBox(-2.5F, -0.5F, -5F, 1F, 1F, 5F).texOffs(34, 29).addBox(1.5F, -0.5F, -5F, 1F, 1F, 5F).texOffs(66, 29).addBox(-1F, -1F, -0.5F, 2F, 2F, 1.5F), PartPose.offset(0F, 0F, -20F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"saco", "soga_mano", "lazo"};

    public static final String[] OCULTAS_1 = {"unas_d"};
}
