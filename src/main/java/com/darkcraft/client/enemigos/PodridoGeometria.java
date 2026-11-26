package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class PodridoGeometria {
    private PodridoGeometria() {}

    public static final int TEX_W = 128, TEX_H = 64;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -11F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(20, 33).addBox(-2F, 0F, -2F, 4F, 5.5F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(36, 33).addBox(-2F, 0F, -2F, 4F, 5.5F, 4F), PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(52, 33).addBox(-2F, 0F, -2F, 4F, 5.5F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(68, 33).addBox(-2F, 0F, -2F, 4F, 5.5F, 4F), PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(40, 0).addBox(-4.5F, -12F, -3F, 9F, 12F, 6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(0, 19).addBox(-3.5F, -7F, -3.5F, 7F, 7F, 7F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(52, 19).addBox(-1.5F, -2F, -1.5F, 3F, 7.5F, 3F), PartPose.offset(-6F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(64, 19).addBox(-1.5F, 0F, -1.5F, 3F, 7.5F, 3F), PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(76, 19).addBox(-1.5F, -2F, -1.5F, 3F, 7.5F, 3F), PartPose.offset(6F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(88, 19).addBox(-1.5F, 0F, -1.5F, 3F, 7.5F, 3F), PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_barriga = p_torso.addOrReplaceChild("barriga", CubeListBuilder.create().texOffs(100, 19).addBox(-4F, -3.5F, -2.5F, 8F, 7F, 3F, new CubeDeformation(0.15F, 0.15F, 0.15F)), PartPose.offset(0F, -5F, -3F));
        PartDefinition p_panza = p_torso.addOrReplaceChild("panza", CubeListBuilder.create().texOffs(0, 0).addBox(-5.5F, -5.5F, -6.5F, 11F, 10F, 9F), PartPose.offset(0F, -5F, -1F));
        PartDefinition p_harapo = p_cadera.addOrReplaceChild("harapo", CubeListBuilder.create().texOffs(70, 0).addBox(-5F, -0.5F, -3.5F, 10F, 7F, 7F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_algas_cabeza = p_cabeza.addOrReplaceChild("algas_cabeza", CubeListBuilder.create().texOffs(0, 43).addBox(-3.6F, -0.3F, 3.7F, 7.2F, 7F, 0F).texOffs(28, 19).addBox(3.7F, -0.3F, -2F, 0F, 6F, 5.5F).texOffs(40, 19).addBox(-3.7F, -0.3F, -2F, 0F, 6F, 5.5F), PartPose.offset(0F, -7F, 0F));
        PartDefinition p_algas_d = p_brazo_d.addOrReplaceChild("algas_d", CubeListBuilder.create().texOffs(84, 33).addBox(-1.8F, -0.4F, -1.8F, 3.6F, 5F, 3.6F, new CubeDeformation(0.2F, 0.2F, 0.2F)), PartPose.offset(0F, -2F, 0F));
        PartDefinition p_algas_i = p_brazo_i.addOrReplaceChild("algas_i", CubeListBuilder.create().texOffs(100, 33).addBox(-1.8F, -0.4F, -1.8F, 3.6F, 5F, 3.6F, new CubeDeformation(0.2F, 0.2F, 0.2F)), PartPose.offset(0F, -2F, 0F));
        PartDefinition p_fatuo = p_mano_i.addOrReplaceChild("fatuo", CubeListBuilder.create().texOffs(16, 43).addBox(-1.5F, -1.5F, -3F, 3F, 3F, 3F), PartPose.offset(0F, 0.5F, -1.5F));
        PartDefinition p_fatuo_halo = p_fatuo.addOrReplaceChild("fatuo_halo", CubeListBuilder.create().texOffs(0, 33).addBox(-2.2F, -2.2F, -2.2F, 4.4F, 4.4F, 4.4F), PartPose.offset(0F, 0F, -1.5F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"panza", "fatuo", "fatuo_halo"};

    public static final String[] OCULTAS_1 = {"panza"};

    public static final String[] OCULTAS_2 = {"barriga", "fatuo", "fatuo_halo"};
}
