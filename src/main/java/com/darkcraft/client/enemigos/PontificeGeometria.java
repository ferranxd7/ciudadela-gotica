package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class PontificeGeometria {
    private PontificeGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -13F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(36, 63).addBox(-2F, 0F, -2F, 4F, 6.5F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(52, 63).addBox(-2F, 0F, -2F, 4F, 6.5F, 4F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(68, 63).addBox(-2F, 0F, -2F, 4F, 6.5F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(84, 63).addBox(-2F, 0F, -2F, 4F, 6.5F, 4F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(96, 0).addBox(-4F, -13F, -2F, 8F, 13F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(30, 36).addBox(-3.5F, -8F, -3.5F, 7F, 8F, 7F), PartPose.offset(0F, -13F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(100, 74).addBox(-1.5F, -2F, -1.5F, 3F, 7F, 3F), PartPose.offset(-5.5F, -11F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(112, 74).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(0, 84).addBox(-1.5F, -2F, -1.5F, 3F, 7F, 3F), PartPose.offset(5.5F, -11F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(12, 84).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F), PartPose.offset(0F, 5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_tiara = p_cabeza.addOrReplaceChild("tiara", CubeListBuilder.create().texOffs(0, 51).addBox(-3.8F, -3.2F, -3.8F, 7.6F, 3.2F, 7.6F).texOffs(32, 74).addBox(-3.4F, -6.2F, -3.4F, 6.8F, 3F, 6.8F).texOffs(58, 84).addBox(-2.9F, -8.9F, -2.9F, 5.8F, 2.7F, 5.8F).texOffs(82, 84).addBox(-2.1F, -10.5F, -2.1F, 4.2F, 1.6F, 4.2F).texOffs(78, 94).addBox(-0.8F, -11.7F, -0.8F, 1.6F, 1.2F, 1.6F).texOffs(118, 94).addBox(-0.35F, -14.1F, -0.35F, 0.7F, 2.4F, 0.7F).texOffs(6, 100).addBox(-1.1F, -13.5F, -0.35F, 2.2F, 0.7F, 0.7F).texOffs(0, 63).addBox(-4.1F, -1.3F, -4.1F, 8.2F, 1.3F, 8.2F).texOffs(0, 74).addBox(-3.7F, -4.4F, -3.7F, 7.4F, 1.2F, 7.4F).texOffs(30, 84).addBox(-3.2F, -7.2F, -3.2F, 6.4F, 1.1F, 6.4F), PartPose.offset(0F, -7.6F, 0F));
        PartDefinition p_infulas = p_cabeza.addOrReplaceChild("infulas", CubeListBuilder.create().texOffs(100, 63).addBox(-2.6F, 0F, 0F, 1.6F, 9.5F, 0.45F).texOffs(106, 63).addBox(1F, 0F, 0F, 1.6F, 9.5F, 0.45F), PartPose.offsetAndRotation(0F, -7.4F, 3.5F, 0.1745F, 0F, 0F));
        PartDefinition p_barba = p_cabeza.addOrReplaceChild("barba", CubeListBuilder.create().texOffs(0, 94).addBox(-2.6F, 0F, -0.8F, 5.2F, 4.6F, 0.9F), PartPose.offsetAndRotation(0F, -2F, -3.5F, -0.0698F, 0F, 0F));
        PartDefinition p_muceta = p_torso.addOrReplaceChild("muceta", CubeListBuilder.create().texOffs(32, 51).addBox(-5F, -0.5F, -2.8F, 10F, 6F, 5.6F, new CubeDeformation(0.25F, 0.25F, 0.25F)), PartPose.offset(0F, -13F, 0F));
        PartDefinition p_broche = p_torso.addOrReplaceChild("broche", CubeListBuilder.create().texOffs(86, 94).addBox(-1.3F, 0F, -0.5F, 2.6F, 2.2F, 0.6F), PartPose.offset(0F, -11.4F, -3.15F));
        PartDefinition p_palio = p_torso.addOrReplaceChild("palio", CubeListBuilder.create().texOffs(24, 84).addBox(-0.8F, 0F, -0.35F, 1.6F, 8.6F, 0.35F), PartPose.offset(0F, -9.2F, -3F));
        PartDefinition p_capa = p_torso.addOrReplaceChild("capa", CubeListBuilder.create().texOffs(72, 0).addBox(-5.2F, 0F, 0F, 10.4F, 25.4F, 0.8F), PartPose.offsetAndRotation(0F, -12.6F, 2.3F, 0.0698F, 0F, 0F));
        PartDefinition p_alba_d = p_muslo_d.addOrReplaceChild("alba_d", CubeListBuilder.create().texOffs(58, 36).addBox(-2.7F, -0.6F, -2.7F, 5.4F, 7.2F, 5.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_alba_bajo_d = p_pierna_d.addOrReplaceChild("alba_bajo_d", CubeListBuilder.create().texOffs(64, 51).addBox(-2.6F, 0F, -2.6F, 5.2F, 5.9F, 5.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_manga_d = p_antebrazo_d.addOrReplaceChild("manga_d", CubeListBuilder.create().texOffs(60, 74).addBox(-2.3F, 0F, -2.3F, 4.6F, 5F, 4.6F), PartPose.offset(0F, 1F, 0F));
        PartDefinition p_alba_i = p_muslo_i.addOrReplaceChild("alba_i", CubeListBuilder.create().texOffs(82, 36).addBox(-2.65F, -0.6F, -2.65F, 5.3F, 7.2F, 5.3F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_alba_bajo_i = p_pierna_i.addOrReplaceChild("alba_bajo_i", CubeListBuilder.create().texOffs(88, 51).addBox(-2.55F, 0F, -2.55F, 5.1F, 5.9F, 5.1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_manga_i = p_antebrazo_i.addOrReplaceChild("manga_i", CubeListBuilder.create().texOffs(80, 74).addBox(-2.3F, 0F, -2.3F, 4.6F, 5F, 4.6F), PartPose.offset(0F, 1F, 0F));
        PartDefinition p_ferula = p_mano_d.addOrReplaceChild("ferula", CubeListBuilder.create().texOffs(0, 0).addBox(-0.7F, -0.7F, -21F, 1.4F, 1.4F, 34F).texOffs(94, 94).addBox(-0.95F, -0.95F, 13F, 1.9F, 1.9F, 1.2F).texOffs(14, 94).addBox(-1.2F, -1.2F, -22.8F, 2.4F, 2.4F, 1.8F).texOffs(0, 36).addBox(-0.65F, -0.65F, -35F, 1.3F, 1.3F, 12.2F).texOffs(64, 94).addBox(-2.3F, -0.65F, -33.6F, 4.6F, 1.3F, 1.1F).texOffs(46, 94).addBox(-3.3F, -0.65F, -31F, 6.6F, 1.3F, 1.1F).texOffs(24, 94).addBox(-4.3F, -0.65F, -28.4F, 8.6F, 1.3F, 1.1F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_caliz = p_mano_i.addOrReplaceChild("caliz", CubeListBuilder.create().texOffs(102, 94).addBox(-1.5F, -1.5F, 0.4F, 3F, 3F, 0.7F).texOffs(122, 94).addBox(-0.5F, -0.5F, -1.4F, 1F, 1F, 1.8F).texOffs(0, 100).addBox(-0.9F, -0.9F, -2F, 1.8F, 1.8F, 0.6F).texOffs(102, 84).addBox(-1.8F, -1.8F, -4.8F, 3.6F, 3.6F, 2.8F).texOffs(110, 94).addBox(-1.5F, -1.5F, -4.95F, 3F, 3F, 0.3F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {};
}
