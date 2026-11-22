package com.darkcraft.client.rey;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.rey.Rey;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Modelo del Rey: piezas de ReyGeometria y animaciones de ReyAnimaciones (generados). */
public class ReyModel extends HierarchicalModel<Rey> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "rey"), "main");

    private final ModelPart root;
    final ModelPart raiz, cadera, torso, cabeza, corona, brazoD, antebrazoD, espadaD, brazoI, antebrazoI, espadaI;
    public float alpha = 1f;

    public ReyModel(ModelPart root) {
        this.root = root;
        this.raiz = root.getChild("raiz");
        this.cadera = raiz.getChild("cadera");
        this.torso = cadera.getChild("torso");
        this.cabeza = torso.getChild("cabeza");
        this.corona = cabeza.getChild("corona");
        this.brazoD = torso.getChild("brazo_d");
        this.antebrazoD = brazoD.getChild("antebrazo_d");
        this.espadaD = antebrazoD.getChild("espada_d");
        this.brazoI = torso.getChild("brazo_i");
        this.antebrazoI = brazoI.getChild("antebrazo_i");
        this.espadaI = antebrazoI.getChild("espada_i");
        this.faldas = new ModelPart[]{cadera.getChild("falda_d"), cadera.getChild("falda_i"), cadera.getChild("falda_t"),
                cadera.getChild("falda_ld"), cadera.getChild("falda_li")};
    }

    /** Los faldones de la chaqueta (fuera cuando el Rey se la arranca en los volcanes). */
    final ModelPart[] faldas;

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(Rey e, float limbSwing, float limbSwingAmount, float age, float headYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        boolean conChaqueta = !e.flag(Rey.F_SIN_CHAQUETA);
        for (ModelPart f : faldas) f.visible = conChaqueta;
        int pose = e.pose();
        boolean acting = e.acting();
        if (pose == Rey.POSE_SENTADO) {
            animate(e.sitAnim, ReyAnimaciones.SENTADO, age);
        } else {
            if (pose == Rey.POSE_CARGA) animate(e.chargeAnim, ReyAnimaciones.EMBESTIDA, age);
            else if (pose == Rey.POSE_GUARDIA && !acting) animate(e.guardAnim, ReyAnimaciones.GUARDIA, age);
            else if (!acting) animate(e.idleAnim, ReyAnimaciones.QUIETO, age);
            if (pose != Rey.POSE_CARGA && e.actionId() != com.darkcraft.rey.ReyAnims.MUERTE)
                animateWalk(ReyAnimaciones.ANDAR, limbSwing, limbSwingAmount, 1.6f, 2.2f);
        }
        animate(e.talkAnim, ReyAnimaciones.HABLAR, age);
        for (int i = 1; i < ReyAnimaciones.ACCIONES.length; i++) animate(e.actions[i], ReyAnimaciones.ACCIONES[i], age);
        if (!acting && pose != Rey.POSE_CARGA) {
            cabeza.yRot += Mth.clamp(headYaw, -50f, 50f) * Mth.DEG_TO_RAD * 0.7f;
            cabeza.xRot += Mth.clamp(headPitch, -30f, 30f) * Mth.DEG_TO_RAD * 0.6f;
        }
        corona.visible = !e.flag(Rey.F_SIN_CORONA) || e.actionId() == com.darkcraft.rey.ReyAnims.MUERTE;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
        super.renderToBuffer(ps, vc, light, overlay, r, g, b, a * alpha);
    }
}
