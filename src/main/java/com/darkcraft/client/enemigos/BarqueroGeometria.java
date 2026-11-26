package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class BarqueroGeometria {
    private BarqueroGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -15F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(58, 66).addBox(-2F, 0F, -2F, 4F, 7.5F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(74, 66).addBox(-2F, 0F, -2F, 4F, 7.5F, 4F), PartPose.offset(0F, 7.5F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(90, 66).addBox(-2F, 0F, -2F, 4F, 7.5F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(106, 66).addBox(-2F, 0F, -2F, 4F, 7.5F, 4F), PartPose.offset(0F, 7.5F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(22, 32).addBox(-4.5F, -14F, -2.5F, 9F, 14F, 5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(50, 32).addBox(-3.5F, -8F, -3.5F, 7F, 8F, 7F), PartPose.offset(0F, -14F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(0, 78).addBox(-1.5F, -2F, -1.5F, 3F, 8.5F, 3F), PartPose.offset(-5.6F, -12F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(12, 78).addBox(-1.5F, 0F, -1.5F, 3F, 8.5F, 3F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 7.5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(24, 78).addBox(-1.5F, -2F, -1.5F, 3F, 8.5F, 3F), PartPose.offset(5.6F, -12F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(36, 78).addBox(-1.5F, 0F, -1.5F, 3F, 8.5F, 3F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 7.5F, 0F));
        PartDefinition p_capucha = p_cabeza.addOrReplaceChild("capucha", CubeListBuilder.create().texOffs(0, 66).addBox(-4.3F, -1.2F, -4.6F, 8.6F, 1.2F, 9.2F).texOffs(88, 0).addBox(-4.3F, -1.2F, -4.6F, 0.9F, 9.6F, 9.2F).texOffs(0, 32).addBox(3.4F, -1.2F, -4.6F, 0.9F, 9.6F, 9.2F).texOffs(38, 66).addBox(-4.3F, -1.2F, 3.7F, 8.6F, 10.6F, 0.9F).texOffs(60, 98).addBox(-4.3F, -1.2F, -5.2F, 8.6F, 1.9F, 0.7F).texOffs(90, 90).addBox(-1.4F, -2.6F, 1.6F, 2.8F, 1.5F, 3.6F), PartPose.offset(0F, -8F, 0F));
        PartDefinition p_manto = p_torso.addOrReplaceChild("manto", CubeListBuilder.create().texOffs(48, 52).addBox(-5.8F, -0.6F, -3.3F, 11.6F, 5.2F, 6.6F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(0F, -14F, 0F));
        PartDefinition p_capa = p_torso.addOrReplaceChild("capa", CubeListBuilder.create().texOffs(64, 0).addBox(-5.4F, 0F, 0F, 10.8F, 26F, 0.8F), PartPose.offsetAndRotation(0F, -13.6F, 3F, 0.0698F, 0F, 0F));
        PartDefinition p_habito_d = p_muslo_d.addOrReplaceChild("habito_d", CubeListBuilder.create().texOffs(78, 32).addBox(-2.75F, -0.6F, -2.75F, 5.5F, 8.4F, 5.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_habito_bajo_d = p_pierna_d.addOrReplaceChild("habito_bajo_d", CubeListBuilder.create().texOffs(0, 52).addBox(-2.65F, 0F, -2.65F, 5.3F, 7.6F, 5.3F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_habito_i = p_muslo_i.addOrReplaceChild("habito_i", CubeListBuilder.create().texOffs(102, 32).addBox(-2.75F, -0.6F, -2.75F, 5.5F, 8.4F, 5.5F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_habito_bajo_i = p_pierna_i.addOrReplaceChild("habito_bajo_i", CubeListBuilder.create().texOffs(24, 52).addBox(-2.65F, 0F, -2.65F, 5.3F, 7.6F, 5.3F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_manga_d = p_antebrazo_d.addOrReplaceChild("manga_d", CubeListBuilder.create().texOffs(48, 78).addBox(-2.2F, 0F, -2.2F, 4.4F, 5F, 4.4F), PartPose.offset(0F, 1.5F, 0F));
        PartDefinition p_manga_i = p_antebrazo_i.addOrReplaceChild("manga_i", CubeListBuilder.create().texOffs(68, 78).addBox(-2.2F, 0F, -2.2F, 4.4F, 5F, 4.4F), PartPose.offset(0F, 1.5F, 0F));
        PartDefinition p_cordel = p_cadera.addOrReplaceChild("cordel", CubeListBuilder.create().texOffs(0, 90).addBox(-4.9F, -1.5F, -2.9F, 9.8F, 1.3F, 5.8F, new CubeDeformation(0.2F, 0.2F, 0.2F)).texOffs(104, 90).addBox(3.6F, -0.6F, -2.6F, 2.2F, 3F, 2.4F).texOffs(86, 90).addBox(-5.3F, -0.5F, -1.2F, 0.6F, 5.5F, 0.6F).texOffs(36, 98).addBox(-5.6F, 4.6F, -1.6F, 1.2F, 1.2F, 1.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_remo = p_mano_d.addOrReplaceChild("remo", CubeListBuilder.create().texOffs(0, 0).addBox(-0.8F, -0.8F, -26F, 1.6F, 1.6F, 30F).texOffs(44, 98).addBox(-1F, -1F, 3.6F, 2F, 2F, 1.4F).texOffs(0, 98).addBox(-1.15F, -1.15F, -27.2F, 2.3F, 2.3F, 1.6F).texOffs(86, 52).addBox(-3.2F, -0.45F, -37.5F, 6.4F, 0.9F, 11.5F).texOffs(72, 90).addBox(-0.55F, -0.55F, -41.5F, 1.1F, 1.1F, 4.4F).texOffs(116, 90).addBox(-0.55F, -3.6F, -38.6F, 1.1F, 3.2F, 1.1F).texOffs(10, 98).addBox(-0.55F, -3.6F, -38F, 1.1F, 1.1F, 2.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cadena = p_mano_i.addOrReplaceChild("cadena", CubeListBuilder.create().texOffs(52, 98).addBox(-0.7F, -0.4F, -0.7F, 1.4F, 1.6F, 1.4F).texOffs(20, 98).addBox(-0.4F, 1F, -0.4F, 0.8F, 3.2F, 0.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_farol = p_cadena.addOrReplaceChild("farol", CubeListBuilder.create().texOffs(24, 98).addBox(-1.2F, -0.8F, -1.2F, 2.4F, 0.9F, 2.4F).texOffs(32, 90).addBox(-2.3F, 0F, -2.3F, 4.6F, 1.2F, 4.6F).texOffs(108, 78).addBox(-1.8F, 1.2F, -1.8F, 3.6F, 4.6F, 3.6F).texOffs(88, 78).addBox(-2.1F, 1.2F, -2.1F, 4.2F, 4.6F, 4.2F).texOffs(52, 90).addBox(-2.3F, 5.8F, -2.3F, 4.6F, 1.2F, 4.6F), PartPose.offset(0F, 4F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {};
}
