package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class IsoldeGeometria {
    private IsoldeGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(100, 56).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-1.5F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(0, 67).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(16, 67).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(1.5F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(32, 67).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 27).addBox(-3.5F, -12F, -2F, 7F, 12F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(64, 27).addBox(-3.5F, -8F, -3.5F, 7F, 8F, 7F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(28, 56).addBox(-1.5F, -2F, -1.5F, 3F, 7.5F, 3F), PartPose.offset(-4.6F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(40, 56).addBox(-1.5F, 0F, -1.5F, 3F, 7.5F, 3F), PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(52, 56).addBox(-1.5F, -2F, -1.5F, 3F, 7.5F, 3F), PartPose.offset(4.6F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(64, 56).addBox(-1.5F, 0F, -1.5F, 3F, 7.5F, 3F), PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_vestido = p_cadera.addOrReplaceChild("vestido", CubeListBuilder.create().texOffs(92, 27).addBox(-4.6F, 0F, -2.8F, 9.2F, 7.5F, 5.6F), PartPose.offset(0F, -0.5F, 0F));
        PartDefinition p_vestido_bajo = p_vestido.addOrReplaceChild("vestido_bajo", CubeListBuilder.create().texOffs(66, 0).addBox(-6.4F, 0F, -3.8F, 12.8F, 8.6F, 7.6F), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_cola = p_cadera.addOrReplaceChild("cola", CubeListBuilder.create().texOffs(42, 0).addBox(-5.4F, 0F, 0F, 10.8F, 18F, 0.8F), PartPose.offsetAndRotation(0F, 0.5F, 2.6F, 0.5236F, 0F, 0F));
        PartDefinition p_corpino = p_torso.addOrReplaceChild("corpino", CubeListBuilder.create().texOffs(22, 27).addBox(-3.5F, -12F, -2F, 7F, 12F, 4F, new CubeDeformation(0.35F, 0.35F, 0.35F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cuello = p_torso.addOrReplaceChild("cuello", CubeListBuilder.create().texOffs(0, 56).addBox(-6.2F, -9F, 0F, 12.4F, 9.4F, 0.6F), PartPose.offsetAndRotation(0F, -11.6F, 1.6F, -0.3142F, 0F, 0F));
        PartDefinition p_manga_d = p_antebrazo_d.addOrReplaceChild("manga_d", CubeListBuilder.create().texOffs(0, 43).addBox(-2.6F, 0F, -2.6F, 5.2F, 6.4F, 5.2F), PartPose.offset(0F, 1.6F, 0F));
        PartDefinition p_garra_d = p_mano_d.addOrReplaceChild("garra_d", CubeListBuilder.create().texOffs(76, 56).addBox(-1.5F, -0.6F, -1.5F, 3F, 7.2F, 3F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_manga_i = p_antebrazo_i.addOrReplaceChild("manga_i", CubeListBuilder.create().texOffs(24, 43).addBox(-2.6F, 0F, -2.6F, 5.2F, 6.4F, 5.2F), PartPose.offset(0F, 1.6F, 0F));
        PartDefinition p_garra_i = p_mano_i.addOrReplaceChild("garra_i", CubeListBuilder.create().texOffs(88, 56).addBox(-1.5F, -0.6F, -1.5F, 3F, 7.2F, 3F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_pelo = p_cabeza.addOrReplaceChild("pelo", CubeListBuilder.create().texOffs(22, 0).addBox(-4F, 0F, 0F, 8F, 18F, 1.2F), PartPose.offset(0F, -7.4F, 3F));
        PartDefinition p_corona = p_cabeza.addOrReplaceChild("corona", CubeListBuilder.create().texOffs(48, 43).addBox(-4.1F, -9.2F, -4.1F, 8.2F, 1.6F, 8.2F).texOffs(48, 67).addBox(-0.6F, -12.6F, -4.5F, 1.2F, 3.6F, 1F).texOffs(60, 67).addBox(-3.6F, -11.4F, -4.4F, 1F, 2.4F, 1F).texOffs(64, 67).addBox(2.6F, -11.4F, -4.4F, 1F, 2.4F, 1F).texOffs(74, 67).addBox(-4.4F, -11F, -0.5F, 1F, 2F, 1F).texOffs(78, 67).addBox(3.4F, -11F, -0.5F, 1F, 2F, 1F).texOffs(54, 67).addBox(-0.6F, -11.4F, 3.4F, 1.2F, 2.4F, 1F).texOffs(68, 67).addBox(-0.7F, -8.6F, -4.6F, 1.4F, 1.4F, 0.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_velo_frente = p_cabeza.addOrReplaceChild("velo_frente", CubeListBuilder.create().texOffs(44, 27).addBox(-4.3F, -0.6F, -0.5F, 8.6F, 14.5F, 0.5F), PartPose.offset(0F, -8F, -4.4F));
        PartDefinition p_velo_espalda = p_cabeza.addOrReplaceChild("velo_espalda", CubeListBuilder.create().texOffs(0, 0).addBox(-4.6F, -0.6F, 0F, 9.2F, 26F, 0.6F), PartPose.offsetAndRotation(0F, -8F, 4F, 0.1396F, 0F, 0F));
        PartDefinition p_velo_roto = p_cabeza.addOrReplaceChild("velo_roto", CubeListBuilder.create().texOffs(84, 43).addBox(-4F, -8.9F, -4F, 8F, 2.6F, 8F, new CubeDeformation(0.25F, 0.25F, 0.25F)), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"muslo_d", "muslo_i", "pierna_d", "pierna_i", "velo_roto"};

    public static final String[] OCULTAS_1 = {"muslo_d", "muslo_i", "pierna_d", "pierna_i", "velo_frente", "velo_espalda"};
}
