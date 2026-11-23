package com.darkcraft.client.gavran;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gavran.Gavran;
import com.darkcraft.gavran.GavranAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Modelo de Ser Gavran: piezas de GavranGeometria y animaciones de GavranAnimaciones (generados por tools/gavran). */
public class GavranModel extends HierarchicalModel<Gavran> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "gavran"), "main");

    private final ModelPart root;
    final ModelPart raiz, cadera, torso, cabeza, capa, capa2, brazoD, antebrazoD, espada;
    public float alpha = 1f;

    public GavranModel(ModelPart root) {
        this.root = root;
        this.raiz = root.getChild("raiz");
        this.cadera = raiz.getChild("cadera");
        this.torso = cadera.getChild("torso");
        this.cabeza = torso.getChild("cabeza");
        this.capa = torso.getChild("capa");
        this.capa2 = capa.getChild("capa2");
        this.brazoD = torso.getChild("brazo_d");
        this.antebrazoD = brazoD.getChild("antebrazo_d");
        this.espada = antebrazoD.getChild("espada");
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(Gavran e, float limbSwing, float limbSwingAmount, float age, float headYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        int pose = e.pose();
        boolean acting = e.acting();
        if (pose == Gavran.POSE_POSADO && !acting) {
            animate(e.posadoAnim, GavranAnimaciones.POSADO, age);
        } else {
            if (pose == Gavran.POSE_CARGA) animate(e.chargeAnim, GavranAnimaciones.CARGA, age);
            else if (pose == Gavran.POSE_GUARDIA && !acting) animate(e.guardAnim, GavranAnimaciones.GUARDIA, age);
            else if (!acting) animate(e.idleAnim, GavranAnimaciones.QUIETO, age);
            int id = e.actionId();
            if (pose != Gavran.POSE_CARGA && id != GavranAnims.MUERTE && id != GavranAnims.DESCENDER && id != GavranAnims.SUBIR)
                animateWalk(GavranAnimaciones.ANDAR, limbSwing, limbSwingAmount, 1.25f, 2.0f);
        }
        animate(e.talkAnim, GavranAnimaciones.HABLAR, age);
        for (int i = 1; i < GavranAnimaciones.ACCIONES.length; i++) animate(e.actions[i], GavranAnimaciones.ACCIONES[i], age);
        if (!acting && pose != Gavran.POSE_CARGA) {
            float k = pose == Gavran.POSE_POSADO ? (e.flag(Gavran.F_ALERTA) ? 0.9f : 0f) : 0.6f;
            cabeza.yRot += Mth.clamp(headYaw, -45f, 45f) * Mth.DEG_TO_RAD * k;
            cabeza.xRot += Mth.clamp(headPitch, -35f, 45f) * Mth.DEG_TO_RAD * k;
        }
        // la capa se levanta un poco al andar y ondea despacio
        float onda = Mth.sin(age * 0.07f) * 0.035f;
        capa.xRot += Math.min(0.5f, limbSwingAmount * 0.45f) + onda;
        capa2.xRot += Math.min(0.3f, limbSwingAmount * 0.25f) + onda * 1.4f;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
        super.renderToBuffer(ps, vc, light, overlay, r, g, b, a * alpha);
    }
}
