package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class EspectroGeometria {
    private EspectroGeometria() {}

    public static final int TEX_W = 128, TEX_H = 64;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(18, 42).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(34, 42).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(50, 42).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(66, 42).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(66, 0).addBox(-4F, -12F, -2F, 8F, 12F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(34, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(82, 42).addBox(-1.5F, -2F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(-5F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(94, 42).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(106, 42).addBox(-1.5F, -2F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(5F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(0, 52).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_faldon = p_cadera.addOrReplaceChild("faldon", CubeListBuilder.create().texOffs(38, 17).addBox(-4.6F, 0F, -2.6F, 9.2F, 7F, 5.2F), PartPose.offset(0F, -0.5F, 0F));
        PartDefinition p_faldon_bajo = p_faldon.addOrReplaceChild("faldon_bajo", CubeListBuilder.create().texOffs(0, 17).addBox(-5.6F, 0F, -3.3F, 11.2F, 7F, 6.6F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_manga_d = p_antebrazo_d.addOrReplaceChild("manga_d", CubeListBuilder.create().texOffs(72, 31).addBox(-2.3F, 0F, -2.3F, 4.6F, 4.2F, 4.6F), PartPose.offset(0F, 2.5F, 0F));
        PartDefinition p_manga_i = p_antebrazo_i.addOrReplaceChild("manga_i", CubeListBuilder.create().texOffs(92, 31).addBox(-2.3F, 0F, -2.3F, 4.6F, 4.2F, 4.6F), PartPose.offset(0F, 2.5F, 0F));
        PartDefinition p_gorguera = p_torso.addOrReplaceChild("gorguera", CubeListBuilder.create().texOffs(0, 31).addBox(-5F, -1.6F, -3.6F, 10F, 2.2F, 7.2F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_gorra = p_cabeza.addOrReplaceChild("gorra", CubeListBuilder.create().texOffs(70, 17).addBox(-4.6F, -1.6F, -4.6F, 9.2F, 2F, 9.2F).texOffs(0, 42).addBox(2.7F, -2.9F, -0.6F, 0.8F, 1.8F, 7.2F), PartPose.offsetAndRotation(0F, -8F, 0F, -0.0698F, 0F, 0.1047F));
        PartDefinition p_capa = p_torso.addOrReplaceChild("capa", CubeListBuilder.create().texOffs(110, 0).addBox(-4F, 0F, 0F, 6.5F, 14F, 1F), PartPose.offsetAndRotation(2F, -12F, 2.4F, 0.1047F, 0F, -0.0698F));
        PartDefinition p_estoque = p_mano_d.addOrReplaceChild("estoque", CubeListBuilder.create().texOffs(78, 52).addBox(-0.5F, -0.5F, -1.5F, 1F, 1F, 3.5F).texOffs(88, 52).addBox(-0.8F, -0.8F, 2F, 1.6F, 1.6F, 1.2F).texOffs(66, 52).addBox(-1.8F, -1.8F, -2.6F, 3.6F, 3.6F, 1.1F).texOffs(96, 52).addBox(-2.6F, -0.3F, -2.3F, 5.2F, 0.6F, 0.6F).texOffs(0, 0).addBox(-0.3F, -0.3F, -18.5F, 0.6F, 0.6F, 16F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_toca = p_cabeza.addOrReplaceChild("toca", CubeListBuilder.create().texOffs(36, 31).addBox(-4.4F, -0.6F, -4.4F, 8.8F, 1.3F, 8.8F).texOffs(90, 0).addBox(-4.3F, -0.4F, 3.9F, 8.6F, 15F, 0.8F), PartPose.offset(0F, -8F, 0F));
        PartDefinition p_cuello_alto = p_torso.addOrReplaceChild("cuello_alto", CubeListBuilder.create().texOffs(12, 52).addBox(-4.6F, -2.4F, -2.4F, 9.2F, 3F, 4.8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_garra_d = p_mano_d.addOrReplaceChild("garra_d", CubeListBuilder.create().texOffs(42, 52).addBox(-1.4F, -0.2F, -1.4F, 2.8F, 4.6F, 2.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_garra_i = p_mano_i.addOrReplaceChild("garra_i", CubeListBuilder.create().texOffs(54, 52).addBox(-1.4F, -0.2F, -1.4F, 2.8F, 4.6F, 2.8F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"muslo_d", "muslo_i", "pierna_d", "pierna_i", "toca", "cuello_alto", "garra_d", "garra_i"};

    public static final String[] OCULTAS_1 = {"muslo_d", "muslo_i", "pierna_d", "pierna_i", "gorguera", "gorra", "capa", "estoque"};
}
