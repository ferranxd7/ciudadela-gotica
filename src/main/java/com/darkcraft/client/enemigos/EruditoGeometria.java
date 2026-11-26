package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class EruditoGeometria {
    private EruditoGeometria() {}

    public static final int TEX_W = 128, TEX_H = 128;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(40, 56).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(56, 56).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(72, 56).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(2F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(88, 56).addBox(-2F, 0F, -2F, 4F, 6F, 4F), PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(88, 0).addBox(-4F, -12F, -2F, 8F, 12F, 4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(56, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(0, 66).addBox(-1.5F, -2F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(-5F, -10F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(12, 66).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(24, 66).addBox(-1.5F, -2F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(5F, -10F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(36, 66).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F), PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_toga = p_cadera.addOrReplaceChild("toga", CubeListBuilder.create().texOffs(74, 19).addBox(-4.6F, 0F, -2.6F, 9.2F, 7F, 5.2F), PartPose.offset(0F, -0.5F, 0F));
        PartDefinition p_toga_baja = p_toga.addOrReplaceChild("toga_baja", CubeListBuilder.create().texOffs(0, 19).addBox(-5.6F, 0F, -3.3F, 11.2F, 7F, 6.6F), PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_manga_d = p_antebrazo_d.addOrReplaceChild("manga_d", CubeListBuilder.create().texOffs(0, 56).addBox(-2.4F, 0F, -2.4F, 4.8F, 4.4F, 4.8F), PartPose.offset(0F, 2.5F, 0F));
        PartDefinition p_manga_i = p_antebrazo_i.addOrReplaceChild("manga_i", CubeListBuilder.create().texOffs(20, 56).addBox(-2.4F, 0F, -2.4F, 4.8F, 4.4F, 4.8F), PartPose.offset(0F, 2.5F, 0F));
        PartDefinition p_esclavina = p_torso.addOrReplaceChild("esclavina", CubeListBuilder.create().texOffs(36, 45).addBox(-5.2F, -0.4F, -2.9F, 10.4F, 4.2F, 5.8F), PartPose.offset(0F, -12F, 0F));
        PartDefinition p_cartera = p_torso.addOrReplaceChild("cartera", CubeListBuilder.create().texOffs(64, 66).addBox(0F, 0F, -1.6F, 1.8F, 3.6F, 3.2F), PartPose.offset(4.2F, -3.6F, 0F));
        PartDefinition p_calamo = p_mano_d.addOrReplaceChild("calamo", CubeListBuilder.create().texOffs(72, 76).addBox(-0.45F, -0.45F, -1.4F, 0.9F, 0.9F, 3.2F).texOffs(48, 66).addBox(-0.3F, -0.3F, -7.6F, 0.6F, 0.6F, 6.2F).texOffs(76, 66).addBox(-0.15F, -1.8F, 1.6F, 0.3F, 2.4F, 4.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tintero = p_mano_i.addOrReplaceChild("tintero", CubeListBuilder.create().texOffs(60, 76).addBox(-1.2F, 0.4F, -1.2F, 2.4F, 2.6F, 2.4F).texOffs(106, 76).addBox(-0.6F, -0.3F, -0.6F, 1.2F, 0.8F, 1.2F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_birrete = p_cabeza.addOrReplaceChild("birrete", CubeListBuilder.create().texOffs(0, 45).addBox(-4.4F, -1.4F, -4.4F, 8.8F, 1.6F, 8.8F).texOffs(0, 33).addBox(-5.4F, -2.2F, -5.4F, 10.8F, 0.8F, 10.8F).texOffs(88, 76).addBox(4.8F, -2F, -0.4F, 0.6F, 3.8F, 0.6F), PartPose.offset(0F, -8F, 0F));
        PartDefinition p_barba = p_cabeza.addOrReplaceChild("barba", CubeListBuilder.create().texOffs(104, 56).addBox(-3F, -2.6F, -1.1F, 6F, 7.4F, 1.2F), PartPose.offset(0F, 0F, -4F));
        PartDefinition p_estola = p_torso.addOrReplaceChild("estola", CubeListBuilder.create().texOffs(44, 0).addBox(-3.3F, 0F, -0.5F, 1.5F, 17F, 0.5F).texOffs(50, 0).addBox(1.8F, 0F, -0.5F, 1.5F, 17F, 0.5F), PartPose.offset(0F, -12F, -2.2F));
        PartDefinition p_vara = p_mano_d.addOrReplaceChild("vara", CubeListBuilder.create().texOffs(44, 33).addBox(-0.35F, -0.35F, -9.2F, 0.7F, 0.7F, 10.8F).texOffs(92, 76).addBox(-0.85F, -0.85F, -10.8F, 1.7F, 1.7F, 1.7F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_libro = p_mano_i.addOrReplaceChild("libro", CubeListBuilder.create().texOffs(24, 76).addBox(-3.2F, -1F, -2.2F, 3.1F, 0.5F, 4.4F).texOffs(42, 76).addBox(0.1F, -1F, -2.2F, 3.1F, 0.5F, 4.4F).texOffs(0, 76).addBox(-3.4F, -0.5F, -2.4F, 6.8F, 0.4F, 4.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_capucha = p_cabeza.addOrReplaceChild("capucha", CubeListBuilder.create().texOffs(68, 33).addBox(-4.8F, -0.8F, -5F, 9.6F, 1F, 9.8F).texOffs(0, 0).addBox(-4.8F, -0.8F, -5F, 0.8F, 8.8F, 9.8F).texOffs(22, 0).addBox(4F, -0.8F, -5F, 0.8F, 8.8F, 9.8F).texOffs(70, 45).addBox(-4.8F, -0.8F, 4F, 9.6F, 9.6F, 0.8F), PartPose.offset(0F, -8F, 0F));
        PartDefinition p_llaves = p_torso.addOrReplaceChild("llaves", CubeListBuilder.create().texOffs(88, 66).addBox(-4.3F, -0.6F, -2.3F, 8.6F, 1F, 4.6F).texOffs(100, 76).addBox(-3.2F, 0.3F, -2.7F, 2F, 2.6F, 0.4F), PartPose.offset(0F, -1.6F, 0F));
        PartDefinition p_cadena = p_mano_d.addOrReplaceChild("cadena", CubeListBuilder.create().texOffs(92, 45).addBox(-0.4F, -0.4F, -9F, 0.8F, 0.8F, 9.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tomo = p_cadena.addOrReplaceChild("tomo", CubeListBuilder.create().texOffs(38, 19).addBox(-4F, -2F, -10F, 8F, 4F, 10F).texOffs(82, 76).addBox(3.6F, -1.2F, -7F, 0.8F, 2.4F, 1.6F), PartPose.offset(0F, 0F, -9F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"muslo_d", "muslo_i", "pierna_d", "pierna_i", "birrete", "barba", "estola", "vara", "libro", "capucha", "llaves", "cadena", "tomo"};

    public static final String[] OCULTAS_1 = {"muslo_d", "muslo_i", "pierna_d", "pierna_i", "cartera", "calamo", "tintero", "capucha", "llaves", "cadena", "tomo"};

    public static final String[] OCULTAS_2 = {"muslo_d", "muslo_i", "pierna_d", "pierna_i", "cartera", "calamo", "tintero", "birrete", "barba", "estola", "vara", "libro"};
}
