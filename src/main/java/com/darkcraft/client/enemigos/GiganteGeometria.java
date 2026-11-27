package com.darkcraft.client.enemigos;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/enemigos/generar.py: no tocar a mano. Piezas y cajas del modelo. */
public final class GiganteGeometria {
    private GiganteGeometria() {}

    public static final int TEX_W = 256, TEX_H = 512;

    public static LayerDefinition crear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_raiz = root.addOrReplaceChild("raiz", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cadera = p_raiz.addOrReplaceChild("cadera", CubeListBuilder.create(), PartPose.offset(0F, -28F, 0F));
        PartDefinition p_muslo_d = p_cadera.addOrReplaceChild("muslo_d", CubeListBuilder.create().texOffs(52, 117).addBox(-6F, 0F, -6F, 12F, 14F, 12F), PartPose.offset(-6F, 0F, 0F));
        PartDefinition p_pierna_d = p_muslo_d.addOrReplaceChild("pierna_d", CubeListBuilder.create().texOffs(100, 117).addBox(-6F, 0F, -6F, 12F, 14F, 12F), PartPose.offset(0F, 14F, 0F));
        PartDefinition p_muslo_i = p_cadera.addOrReplaceChild("muslo_i", CubeListBuilder.create().texOffs(148, 117).addBox(-6F, 0F, -6F, 12F, 14F, 12F), PartPose.offset(6F, 0F, 0F));
        PartDefinition p_pierna_i = p_muslo_i.addOrReplaceChild("pierna_i", CubeListBuilder.create().texOffs(196, 117).addBox(-6F, 0F, -6F, 12F, 14F, 12F), PartPose.offset(0F, 14F, 0F));
        PartDefinition p_torso = p_cadera.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(64, 0).addBox(-12F, -30F, -7F, 24F, 30F, 14F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cabeza = p_torso.addOrReplaceChild("cabeza", CubeListBuilder.create().texOffs(174, 90).addBox(-6.5F, -13F, -6.5F, 13F, 13F, 13F), PartPose.offset(0F, -30F, -3F));
        PartDefinition p_brazo_d = p_torso.addOrReplaceChild("brazo_d", CubeListBuilder.create().texOffs(90, 62).addBox(-5F, -2F, -5F, 10F, 18F, 10F), PartPose.offset(-15F, -28F, 0F));
        PartDefinition p_antebrazo_d = p_brazo_d.addOrReplaceChild("antebrazo_d", CubeListBuilder.create().texOffs(130, 62).addBox(-5F, 0F, -5F, 10F, 18F, 10F), PartPose.offset(0F, 16F, 0F));
        PartDefinition p_mano_d = p_antebrazo_d.addOrReplaceChild("mano_d", CubeListBuilder.create(), PartPose.offset(0F, 17F, 0F));
        PartDefinition p_brazo_i = p_torso.addOrReplaceChild("brazo_i", CubeListBuilder.create().texOffs(170, 62).addBox(-5F, -2F, -5F, 10F, 18F, 10F), PartPose.offset(15F, -28F, 0F));
        PartDefinition p_antebrazo_i = p_brazo_i.addOrReplaceChild("antebrazo_i", CubeListBuilder.create().texOffs(210, 62).addBox(-5F, 0F, -5F, 10F, 18F, 10F), PartPose.offset(0F, 16F, 0F));
        PartDefinition p_mano_i = p_antebrazo_i.addOrReplaceChild("mano_i", CubeListBuilder.create(), PartPose.offset(0F, 17F, 0F));
        PartDefinition p_trapecio = p_torso.addOrReplaceChild("trapecio", CubeListBuilder.create().texOffs(0, 206).addBox(-11F, -35F, -3F, 22F, 7F, 11F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_panza = p_torso.addOrReplaceChild("panza", CubeListBuilder.create().texOffs(52, 224).addBox(-10F, -16F, -9.4F, 20F, 14F, 2.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cinto = p_torso.addOrReplaceChild("cinto", CubeListBuilder.create().texOffs(0, 143).addBox(-12.4F, -4.2F, -9.8F, 24.8F, 5F, 17.4F, new CubeDeformation(0.2F, 0.2F, 0.2F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_faldon_f = p_cadera.addOrReplaceChild("faldon_f", CubeListBuilder.create().texOffs(134, 187).addBox(-8F, 0F, -1.2F, 16F, 17F, 1.2F), PartPose.offsetAndRotation(0F, 0.4F, -9.6F, -0.0524F, 0F, 0F));
        PartDefinition p_faldon_t = p_cadera.addOrReplaceChild("faldon_t", CubeListBuilder.create().texOffs(174, 206).addBox(-10F, 0F, 0F, 20F, 16F, 1.2F), PartPose.offsetAndRotation(0F, 0.4F, 7.6F, 0.0698F, 0F, 0F));
        PartDefinition p_pie_d = p_pierna_d.addOrReplaceChild("pie_d", CubeListBuilder.create().texOffs(146, 143).addBox(-6.6F, 9F, -11F, 13.2F, 5F, 17F).texOffs(44, 255).addBox(-6.2F, 11F, -13.6F, 12.4F, 3F, 2.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_puno_d = p_mano_d.addOrReplaceChild("puno_d", CubeListBuilder.create().texOffs(54, 166).addBox(-6F, -1F, -6F, 12F, 9F, 12F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_pie_i = p_pierna_i.addOrReplaceChild("pie_i", CubeListBuilder.create().texOffs(146, 143).addBox(-6.6F, 9F, -11F, 13.2F, 5F, 17F).texOffs(44, 255).addBox(-6.2F, 11F, -13.6F, 12.4F, 3F, 2.6F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_puno_i = p_mano_i.addOrReplaceChild("puno_i", CubeListBuilder.create().texOffs(54, 166).addBox(-6F, -1F, -6F, 12F, 9F, 12F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_corona_pelo = p_cabeza.addOrReplaceChild("corona_pelo", CubeListBuilder.create().texOffs(0, 187).addBox(-6.5F, -9.5F, -6.5F, 13F, 6F, 13F, new CubeDeformation(0.45F, 0.45F, 0.45F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_barba_o = p_cabeza.addOrReplaceChild("barba_o", CubeListBuilder.create().texOffs(0, 241).addBox(-5.5F, 0F, -1.6F, 11F, 12F, 1.8F), PartPose.offsetAndRotation(0F, -4.6F, -6.9F, -0.1047F, 0F, 0F));
        PartDefinition p_trenza = p_barba_o.addOrReplaceChild("trenza", CubeListBuilder.create().texOffs(26, 241).addBox(-1.6F, 0F, -1F, 3.2F, 10F, 2F), PartPose.offsetAndRotation(0F, 11.6F, -0.6F, -0.0698F, 0F, 0F));
        PartDefinition p_manto = p_torso.addOrReplaceChild("manto", CubeListBuilder.create().texOffs(0, 62).addBox(-13.2F, -36F, -8.2F, 26.4F, 10F, 17.4F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_capucha = p_manto.addOrReplaceChild("capucha", CubeListBuilder.create().texOffs(38, 241).addBox(-6.5F, -4F, -1F, 13F, 5F, 6F), PartPose.offsetAndRotation(0F, -36.4F, 6.6F, 0.2793F, 0F, 0F));
        PartDefinition p_cuerda_f = p_torso.addOrReplaceChild("cuerda_f", CubeListBuilder.create().texOffs(140, 0).addBox(-1F, -19F, -0.8F, 2F, 38F, 1.2F), PartPose.offsetAndRotation(0F, -15F, -9.6F, 0F, 0F, 0.6632F));
        PartDefinition p_cuerda_t = p_torso.addOrReplaceChild("cuerda_t", CubeListBuilder.create().texOffs(148, 0).addBox(-1F, -19F, -0.4F, 2F, 38F, 1.2F), PartPose.offsetAndRotation(0F, -15F, 7.4F, 0F, 0F, 0.6632F));
        PartDefinition p_viga = p_mano_d.addOrReplaceChild("viga", CubeListBuilder.create().texOffs(40, 0).addBox(-3F, -44F, -6.5F, 6F, 52F, 6F).texOffs(76, 241).addBox(-3.7F, -42.5F, -7.2F, 7.4F, 1.6F, 7.4F).texOffs(108, 241).addBox(-3.7F, -37.5F, -7.2F, 7.4F, 1.6F, 7.4F).texOffs(108, 241).addBox(-3.7F, 2.5F, -7.2F, 7.4F, 1.6F, 7.4F), PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        PartDefinition p_campana = p_viga.addOrReplaceChild("campana", CubeListBuilder.create().texOffs(214, 241).addBox(-2.5F, -1F, -2.5F, 5F, 3F, 5F).texOffs(122, 206).addBox(-6.5F, 2F, -6.5F, 13F, 5F, 13F).texOffs(106, 90).addBox(-8.5F, 7F, -8.5F, 17F, 9F, 17F).texOffs(22, 90).addBox(-10.5F, 16F, -10.5F, 21F, 5F, 21F), PartPose.offsetAndRotation(0F, -40F, -6.5F, -1.5708F, 0F, 0F));
        PartDefinition p_badajo = p_campana.addOrReplaceChild("badajo", CubeListBuilder.create().texOffs(150, 224).addBox(-1.5F, 0F, -1.5F, 3F, 12F, 3F).texOffs(166, 241).addBox(-2.5F, 11F, -2.5F, 5F, 4.5F, 5F), PartPose.offset(0F, 7F, 0F));
        PartDefinition p_melena = p_cabeza.addOrReplaceChild("melena", CubeListBuilder.create().texOffs(0, 117).addBox(-6.5F, -13F, -6.5F, 13F, 13F, 13F, new CubeDeformation(0.6F, 0.6F, 0.6F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_grenas = p_cabeza.addOrReplaceChild("grenas", CubeListBuilder.create().texOffs(170, 187).addBox(-7.4F, 0F, 0F, 14.8F, 17F, 2F), PartPose.offsetAndRotation(0F, -7F, 6.6F, 0.2094F, 0F, 0F));
        PartDefinition p_barba_u = p_cabeza.addOrReplaceChild("barba_u", CubeListBuilder.create().texOffs(140, 241).addBox(-5.5F, 0F, -1.4F, 11F, 8F, 1.6F), PartPose.offsetAndRotation(0F, -4.4F, -6.9F, -0.0698F, 0F, 0F));
        PartDefinition p_collar_u = p_torso.addOrReplaceChild("collar_u", CubeListBuilder.create().texOffs(0, 255).addBox(-9.5F, -31F, -9.4F, 19F, 3F, 2.2F).texOffs(242, 241).addBox(-7.6F, -28.4F, -9.2F, 2F, 4.2F, 1.4F).texOffs(242, 241).addBox(-3.8F, -28F, -9.6F, 2F, 4.2F, 1.4F).texOffs(234, 241).addBox(-1F, -28.6F, -9.8F, 2F, 5.4F, 1.6F).texOffs(242, 241).addBox(1.8F, -28F, -9.6F, 2F, 4.2F, 1.4F).texOffs(242, 241).addBox(5.6F, -28.4F, -9.2F, 2F, 4.2F, 1.4F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_omoplato = p_brazo_i.addOrReplaceChild("omoplato", CubeListBuilder.create().texOffs(0, 166).addBox(-4.2F, -5.6F, -7.2F, 11.4F, 6F, 14.4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -0.1396F));
        PartDefinition p_costilla_1 = p_mano_d.addOrReplaceChild("costilla_1", CubeListBuilder.create().texOffs(0, 90).addBox(-2.5F, -15F, -6.5F, 5F, 21F, 6F), PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        PartDefinition p_costilla_2 = p_costilla_1.addOrReplaceChild("costilla_2", CubeListBuilder.create().texOffs(118, 143).addBox(-3F, -14.4F, -3.8F, 6F, 14.8F, 7.6F), PartPose.offsetAndRotation(0F, -15F, -3.5F, 0.1396F, 0F, 0F));
        PartDefinition p_costilla_3 = p_costilla_2.addOrReplaceChild("costilla_3", CubeListBuilder.create().texOffs(86, 143).addBox(-3.2F, -13.4F, -4.3F, 6.4F, 13.8F, 8.6F), PartPose.offsetAndRotation(0F, -14F, 0F, 0.1571F, 0F, 0F));
        PartDefinition p_costilla_4 = p_costilla_3.addOrReplaceChild("costilla_4", CubeListBuilder.create().texOffs(204, 187).addBox(-2.6F, -10F, -3.6F, 5.2F, 10.4F, 7.2F), PartPose.offsetAndRotation(0F, -13F, 0F, 0.192F, 0F, 0F));
        PartDefinition p_pelo_h = p_cabeza.addOrReplaceChild("pelo_h", CubeListBuilder.create().texOffs(0, 224).addBox(-6.5F, -13F, -6.5F, 13F, 4F, 13F, new CubeDeformation(0.45F, 0.45F, 0.45F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cinta = p_cabeza.addOrReplaceChild("cinta", CubeListBuilder.create().texOffs(98, 224).addBox(-6.5F, -9.8F, -6.5F, 13F, 1.6F, 13F, new CubeDeformation(0.75F, 0.75F, 0.75F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_barba_h = p_cabeza.addOrReplaceChild("barba_h", CubeListBuilder.create().texOffs(186, 241).addBox(-5.6F, 0F, -1.4F, 11.2F, 7F, 1.6F), PartPose.offset(0F, -4.4F, -6.9F));
        PartDefinition p_mandil = p_torso.addOrReplaceChild("mandil", CubeListBuilder.create().texOffs(102, 166).addBox(-9.6F, -24F, -10.2F, 19.2F, 20F, 0.8F), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_mandil_b = p_cadera.addOrReplaceChild("mandil_b", CubeListBuilder.create().texOffs(52, 187).addBox(-9.6F, 0F, -0.9F, 19.2F, 18F, 0.9F), PartPose.offsetAndRotation(0F, 0.6F, -10.4F, -0.0524F, 0F, 0F));
        PartDefinition p_gozne_d = p_antebrazo_d.addOrReplaceChild("gozne_d", CubeListBuilder.create().texOffs(94, 187).addBox(-5F, 5F, -5F, 10F, 9F, 10F, new CubeDeformation(0.5F, 0.5F, 0.5F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gozne_i = p_antebrazo_i.addOrReplaceChild("gozne_i", CubeListBuilder.create().texOffs(94, 187).addBox(-5F, 5F, -5F, 10F, 9F, 10F, new CubeDeformation(0.5F, 0.5F, 0.5F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_columna = p_mano_d.addOrReplaceChild("columna", CubeListBuilder.create().texOffs(0, 0).addBox(-5F, -46F, -8.5F, 10F, 52F, 10F).texOffs(66, 206).addBox(-7F, -50F, -10.5F, 14F, 4F, 14F).texOffs(144, 166).addBox(-8.5F, -53F, -12F, 17F, 3F, 17F).texOffs(162, 224).addBox(-6F, -6.5F, -9.5F, 12F, 2F, 12F), PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        PartDefinition p_roca_mano = p_mano_i.addOrReplaceChild("roca_mano", CubeListBuilder.create().texOffs(156, 0).addBox(-8F, 1F, -8F, 16F, 14F, 16F).texOffs(210, 224).addBox(-6F, 13F, -5F, 11F, 4F, 10F), PartPose.offset(0F, 0F, 0F));
        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    public static final String[] OCULTAS_0 = {"melena", "grenas", "barba_u", "collar_u", "omoplato", "costilla_1", "costilla_2", "costilla_3", "costilla_4", "pelo_h", "cinta", "barba_h", "mandil", "mandil_b", "gozne_d", "gozne_i", "columna", "roca_mano"};

    public static final String[] OCULTAS_1 = {"manto", "capucha", "corona_pelo", "barba_o", "trenza", "cuerda_f", "cuerda_t", "viga", "campana", "badajo", "pelo_h", "cinta", "barba_h", "mandil", "mandil_b", "gozne_d", "gozne_i", "columna", "roca_mano"};

    public static final String[] OCULTAS_2 = {"manto", "capucha", "corona_pelo", "barba_o", "trenza", "cuerda_f", "cuerda_t", "viga", "campana", "badajo", "melena", "grenas", "barba_u", "collar_u", "omoplato", "costilla_1", "costilla_2", "costilla_3", "costilla_4", "roca_mano"};
}
