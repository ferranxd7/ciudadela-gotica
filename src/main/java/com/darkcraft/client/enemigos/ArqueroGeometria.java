package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class ArqueroGeometria {
    private ArqueroGeometria() {}

    public static final int TEX_W = 128, TEX_H = 64;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(32, 46).addBox(-1F, 0F, -1F, 2F, 6F, 2F), PartPose.offset(-1F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(40, 46).addBox(-1F, 0F, -1F, 2F, 6F, 2F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(48, 46).addBox(-1F, 0F, -1F, 2F, 6F, 2F), PartPose.offset(1F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(56, 46).addBox(-1F, 0F, -1F, 2F, 6F, 2F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(32, 30).addBox(-4F, -12F, -2F, 8F, 12F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(80, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(64, 46).addBox(-1F, -2F, -1F, 2F, 6F, 2F), PartPose.offset(-4F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(72, 46).addBox(-1F, 0F, -1F, 2F, 6F, 2F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(80, 46).addBox(-1F, -2F, -1F, 2F, 6F, 2F), PartPose.offset(4F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(88, 46).addBox(-1F, 0F, -1F, 2F, 6F, 2F), PartPose.offset(0F, 4F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_columna = p_torso.addOrReplaceChild("columna", CubeListBuilder.create().texOffs(96, 30).addBox(-1F, -12F, 0.2F, 2F, 12F, 1.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_pelvis = p_cadera.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(0, 55).addBox(-3.5F, -1.5F, -1.5F, 7F, 3F, 3F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_mandibula = p_cabeza.addOrReplaceChild("mandibula", CubeListBuilder.create().texOffs(20, 55).addBox(-3F, 0F, -3F, 6F, 2F, 4F), PartPose.offset(0F, -1F, -1F));
        PartDefinition p_capucha = p_cabeza.addOrReplaceChild("capucha", CubeListBuilder.create().texOffs(0, 30).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.55F, 0.55F, 0.55F)).texOffs(18, 46).addBox(-2.5F, -5F, 4.5F, 5F, 6F, 1.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_capa = p_torso.addOrReplaceChild("capa", CubeListBuilder.create().texOffs(60, 0).addBox(-4.5F, 0F, 0F, 9F, 17F, 1F), PartPose.offsetAndRotation(0F, -12F, 2.6F, 0.1047F, 0F, 0F));
        PartDefinition p_aljaba = p_torso.addOrReplaceChild("aljaba", CubeListBuilder.create().texOffs(84, 30).addBox(-1.5F, -6F, 0F, 3F, 11F, 2.4F).texOffs(40, 55).addBox(-1.2F, -8.6F, 0.3F, 2.4F, 2.6F, 1.8F), PartPose.offsetAndRotation(1.5F, -6F, 3.2F, 0F, 0F, -0.4189F));
        PartDefinition p_arco = p_mano_i.addOrReplaceChild("arco", CubeListBuilder.create().texOffs(50, 55).addBox(-0.5F, -0.6F, -1.5F, 1F, 1.2F, 3F).texOffs(104, 30).addBox(-0.5F, -0.2F, -9.5F, 1F, 1F, 8F).texOffs(96, 46).addBox(-0.5F, 0.5F, -14.5F, 1F, 1F, 5.2F).texOffs(0, 46).addBox(-0.5F, -0.2F, 1.5F, 1F, 1F, 8F).texOffs(110, 46).addBox(-0.5F, 0.5F, 9.3F, 1F, 1F, 5.2F).texOffs(0, 0).addBox(-0.1F, 1.45F, -14.2F, 0.2F, 0.2F, 28.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_flecha = p_mano_d.addOrReplaceChild("flecha", CubeListBuilder.create().texOffs(56, 30).addBox(-0.25F, -0.25F, -10.5F, 0.5F, 0.5F, 13F).texOffs(66, 55).addBox(-0.5F, -0.5F, -12F, 1F, 1F, 1.6F).texOffs(58, 55).addBox(-0.05F, -0.9F, 1F, 0.1F, 1.8F, 2.2F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {};
}
