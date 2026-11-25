package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class AhogadoGeometria {
    private AhogadoGeometria() {}

    public static final int TEX_W = 128, TEX_H = 64;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(108, 21).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(0, 36).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(16, 36).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(32, 36).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(64, 0).addBox(-4F, -12F, -2.5F, 8F, 12F, 5F).texOffs(38, 46).addBox(-3.5F, -7.5F, -4F, 7F, 6F, 2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(90, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(48, 36).addBox(-2F, -2F, -2F, 4F, 6F, 4F), PartPose.offset(-6F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(64, 36).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(80, 36).addBox(-2F, -2F, -2F, 4F, 6F, 4F), PartPose.offset(6F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(96, 36).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_faldon = p_cadera.addOrReplaceChild("faldon", CubeListBuilder.create().texOffs(0, 46).addBox(-4F, -0.5F, -2.5F, 8F, 4F, 5F, new CubeDeformation(0.35F, 0.35F, 0.35F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_panuelo = p_cabeza.addOrReplaceChild("panuelo", CubeListBuilder.create().texOffs(76, 21).addBox(-4F, -8F, -4F, 8F, 4F, 8F, new CubeDeformation(0.4F, 0.4F, 0.4F)).texOffs(68, 46).addBox(2.5F, -5F, 4F, 2F, 5F, 1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_pelo = p_cabeza.addOrReplaceChild("pelo", CubeListBuilder.create().texOffs(32, 0).addBox(-4F, -8F, -4F, 8F, 9F, 8F, new CubeDeformation(0.45F, 0.45F, 0.45F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_alga_hombro = p_torso.addOrReplaceChild("alga_hombro", CubeListBuilder.create().texOffs(26, 46).addBox(-1.5F, 0F, -3F, 3F, 9F, 0F), PartPose.offsetAndRotation(3.5F, -12.2F, 0F, 0F, 0F, -0.1047F));
        PartDefinition p_alga_brazo = p_antebrazo_d.addOrReplaceChild("alga_brazo", CubeListBuilder.create().texOffs(32, 46).addBox(0F, 0F, -1.5F, 0F, 6F, 3F), PartPose.offset(-2.1F, 1F, 0F));
        PartDefinition p_sable = p_mano_d.addOrReplaceChild("sable", CubeListBuilder.create().texOffs(56, 46).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 5F).texOffs(74, 46).addBox(-1.5F, -1F, -3F, 3F, 2F, 1F).texOffs(0, 0).addBox(0F, -3F, -19F, 0F, 5F, 16F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_arpon = p_mano_d.addOrReplaceChild("arpon", CubeListBuilder.create().texOffs(0, 21).addBox(-0.5F, -0.5F, -16F, 1F, 1F, 14F).texOffs(30, 21).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 14F).texOffs(60, 21).addBox(0F, -2.5F, -23F, 0F, 5F, 8F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"pelo", "arpon"};

    public static final String[] OCULTAS_1 = {"panuelo", "sable"};
}
