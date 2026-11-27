package com.darkcraft.client.personajes;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.client.mision.CampanaRenderer;
import com.darkcraft.personajes.Viajero;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * Los que dan misiones (parte 7ag): el modelo de jugador de brazos anchos con piezas de mas (textura de 128 x 64; las mismas
 * medidas que {@code tools/personajes/oswin.py}): en la cabeza el sombrero de peregrino (copa baja, ala ancha y la concha delante);
 * en los hombros la esclavina; el zurron a la cadera; y el bordon (con su calabaza colgada de lo alto) delante de el, de pie, con
 * las dos manos encima. Oswin espera asi, con la cabeza un poco gacha (es ciego: escucha); cuando le devuelven la campana la alza con
 * la derecha y la toca ({@link Viajero#toca}). Los peregrinos muertos ({@code PeregrinoRenderer}) usan el mismo modelo sin bordon y
 * con las manos juntas.
 */
public class ViajeroRenderer extends LivingEntityRenderer<Viajero, ViajeroRenderer.Modelo<Viajero>> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "viajero"), "main");
    private static final ResourceLocation[] PIELES = new ResourceLocation[Viajero.Quien.values().length];

    static {
        for (Viajero.Quien q : Viajero.Quien.values()) PIELES[q.ordinal()] = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/" + q.id + ".png");
    }

    public ViajeroRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new Modelo<>(ctx.bakeLayer(CAPA)), 0.5f);
        addLayer(new Campanilla(this, new CampanaRenderer.Modelo(ctx.bakeLayer(CampanaRenderer.CAPA))));
    }

    public static LayerDefinition crear() {
        MeshDefinition m = PlayerModel.createMesh(CubeDeformation.NONE, false);
        PartDefinition raiz = m.getRoot();
        PartDefinition cabeza = raiz.getChild("head");
        cabeza.addOrReplaceChild("copa", CubeListBuilder.create().texOffs(64, 0).addBox(-4.5f, -10.0f, -4.5f, 9, 3, 9), PartPose.ZERO);
        cabeza.addOrReplaceChild("ala", CubeListBuilder.create().texOffs(64, 12).addBox(-8f, -7.5f, -8f, 16, 1, 16), PartPose.ZERO);
        cabeza.addOrReplaceChild("concha", CubeListBuilder.create().texOffs(100, 0).addBox(-1.5f, -10.2f, -5.2f, 3, 3, 1), PartPose.ZERO);
        PartDefinition cuerpo = raiz.getChild("body");
        cuerpo.addOrReplaceChild("esclavina", CubeListBuilder.create().texOffs(64, 30).addBox(-4f, 0f, -2f, 8, 5, 4, new CubeDeformation(0.6f)),
                PartPose.ZERO);
        cuerpo.addOrReplaceChild("zurron", CubeListBuilder.create().texOffs(88, 30).addBox(4.2f, 8f, -1.5f, 2, 3, 3), PartPose.ZERO);
        // el bordon: suelto (no se mueve con el cuerpo), de pie delante de el; la calabaza cuelga de lo alto
        PartDefinition baston = raiz.addOrReplaceChild("baston", CubeListBuilder.create().texOffs(120, 30).addBox(-0.5f, -6f, -0.5f, 1, 30, 1),
                PartPose.offset(0f, 0f, -7.3f));
        baston.addOrReplaceChild("calabaza", CubeListBuilder.create().texOffs(100, 30).addBox(0.5f, -3.5f, -1f, 2, 3, 2), PartPose.ZERO);
        return LayerDefinition.create(m, 128, 64);
    }

    @Override
    public ResourceLocation getTextureLocation(Viajero v) {
        return PIELES[v.quien().ordinal()];
    }

    @Override
    protected boolean shouldShowName(Viajero v) {
        return false;
    }

    /**
     * El modelo (el de Oswin y el de los peregrinos muertos): con el bordon o sin el, y lo transparente que se dibuja ({@link #alfa}, lo
     * pone el dibujante antes de cada vez; el tipo de dibujo de {@code PlayerModel} ya es translucido).
     */
    public static class Modelo<T extends LivingEntity> extends PlayerModel<T> {
        public final ModelPart baston;
        public float alfa = 1f;
        /** Lo alzada que esta la campana (0-1). */
        public float toca;

        public Modelo(ModelPart raiz) {
            super(raiz, false);
            baston = raiz.getChild("baston");
        }

        @Override
        public void setupAnim(T e, float paso, float rapidez, float edad, float cabezaY, float cabezaX) {
            riding = false;
            boolean viajero = e instanceof Viajero;
            super.setupAnim(e, viajero ? 0f : paso, viajero ? 0f : rapidez, edad, cabezaY, cabezaX);
            float respira = Mth.sin(edad * 0.06f) * 0.02f;
            if (viajero) {
                // de pie, las dos manos sobre el bordon, la cabeza algo gacha (escucha); alza la campana con la derecha
                float k = toca;
                rightArm.xRot = Mth.lerp(k, -0.65f + respira, -2.55f + Mth.sin(edad * 1.3f) * 0.16f);
                rightArm.yRot = 0f;
                rightArm.zRot = Mth.lerp(k, -0.47f, -0.12f);
                leftArm.xRot = -0.65f + respira;
                leftArm.yRot = 0f;
                leftArm.zRot = 0.47f;
                head.xRot = Mth.lerp(k, head.xRot + 0.22f, -0.3f);
                body.xRot = 0.06f;
                baston.visible = true;
            } else {
                // los peregrinos muertos: andan despacio con las manos juntas, rezando, la cabeza gacha
                rightArm.xRot = -0.95f + respira;
                rightArm.yRot = -0.42f;
                rightArm.zRot = 0.05f;
                leftArm.xRot = -0.95f + respira;
                leftArm.yRot = 0.42f;
                leftArm.zRot = -0.05f;
                head.xRot = 0.32f;
                baston.visible = false;
            }
            hat.copyFrom(head);
            rightSleeve.copyFrom(rightArm);
            leftSleeve.copyFrom(leftArm);
            rightPants.copyFrom(rightLeg);
            leftPants.copyFrom(leftLeg);
            jacket.copyFrom(body);
        }

        @Override
        public void renderToBuffer(PoseStack ps, VertexConsumer vc, int luz, int capa, float r, float g, float b, float a) {
            super.renderToBuffer(ps, vc, luz, capa, r, g, b, a * alfa);
            if (baston.visible) baston.render(ps, vc, luz, capa, r, g, b, a * alfa);
        }
    }

    @Override
    public void render(Viajero v, float giro, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        getModel().alfa = 1f;
        getModel().toca = v.toca(pt);
        super.render(v, giro, pt, ps, buf, luz);
    }

    /** La campana en la mano derecha mientras la toca (colgando: se le quita el giro del brazo). */
    static final class Campanilla extends RenderLayer<Viajero, Modelo<Viajero>> {
        private final CampanaRenderer.Modelo campana;

        Campanilla(RenderLayerParent<Viajero, Modelo<Viajero>> padre, CampanaRenderer.Modelo campana) {
            super(padre);
            this.campana = campana;
        }

        @Override
        public void render(PoseStack ps, MultiBufferSource buf, int luz, Viajero v, float paso, float rapidez, float pt, float edad, float cabezaY,
                           float cabezaX) {
            float k = v.toca(pt);
            if (k <= 0.01f) return;
            Modelo<Viajero> m = getParentModel();
            ps.pushPose();
            m.rightArm.translateAndRotate(ps);
            ps.translate(-1 / 16f, 10.5f / 16f, 0);
            ps.mulPose(Axis.XP.rotation(-m.rightArm.xRot));
            ps.mulPose(Axis.ZP.rotation(Mth.sin(edad * 1.3f) * 0.35f));
            float s = 0.8f;
            ps.scale(s, s, s);
            campana.badajo(edad * 1.3f);
            VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(CampanaRenderer.TEXTURA));
            campana.renderToBuffer(ps, vc, luz, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
            ps.popPose();
        }
    }
}
