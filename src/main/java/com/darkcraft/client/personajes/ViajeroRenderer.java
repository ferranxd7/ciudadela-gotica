package com.darkcraft.client.personajes;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.client.mision.CampanaRenderer;
import com.darkcraft.client.mision.ClientMisiones;
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
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * Los que dan misiones (parte 7ag; 7ah), cada uno con su modelo (texturas de 128 x 64, las mismas medidas que sus scripts de
 * {@code tools/personajes}):
 * <ul>
 * <li><b>Oswin</b> ({@link Modelo}, {@code oswin.py}): el de jugador de brazos anchos con el sombrero de peregrino (copa baja, ala ancha
 * y la concha delante), la esclavina, el zurron y el bordon con su calabaza, de pie delante de el con las dos manos encima, la cabeza
 * un poco gacha (es ciego: escucha); cuando le devuelven la campana la alza con la derecha y la toca ({@link Viajero#toca}). Los
 * peregrinos muertos ({@code PeregrinoRenderer}) usan el mismo modelo sin bordon y con las manos juntas.</li>
 * <li><b>Hedda</b> ({@link ModeloHedda}, {@code hedda.py}): el de brazos finos con el chal, la falda larga que cae de las rodillas,
 * los zapatos que asoman, su taburete y el ovillo en el suelo; teje sin parar (las agujas y la labor roja en las manos), la cabeza
 * inclinada sobre la labor. La variante 1 (la que ve quien le dijo la verdad) esta de rodillas junto a su hijo, con la mano
 * tendida hacia el.</li>
 * </ul>
 * A quien ya no le toca ver a uno no se le dibuja ({@link Viajero#OCULTO}); Hedda se apaga poco a poco cuando acaba su mision con la
 * verdad o la mentira ({@code ClientMisiones.alfa}).
 */
public class ViajeroRenderer extends LivingEntityRenderer<Viajero, ViajeroRenderer.Base<Viajero>> {
    public static final ModelLayerLocation CAPA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "viajero"), "main"),
            CAPA_HEDDA = new ModelLayerLocation(new ResourceLocation(DarkCraftMod.MODID, "viajero_hedda"), "main");
    private static final ResourceLocation[] PIELES = new ResourceLocation[Viajero.Quien.values().length];

    static {
        for (Viajero.Quien q : Viajero.Quien.values()) PIELES[q.ordinal()] = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/" + q.id + ".png");
    }

    private final Base<Viajero>[] modelos;

    @SuppressWarnings("unchecked")
    public ViajeroRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new Modelo<>(ctx.bakeLayer(CAPA)), 0.5f);
        modelos = new Base[Viajero.Quien.values().length];
        modelos[Viajero.Quien.OSWIN.ordinal()] = model;
        modelos[Viajero.Quien.HEDDA.ordinal()] = new ModeloHedda(ctx.bakeLayer(CAPA_HEDDA));
        addLayer(new Campanilla(this, new CampanaRenderer.Modelo(ctx.bakeLayer(CampanaRenderer.CAPA))));
    }

    /** Oswin: el de brazos anchos con el sombrero, la esclavina, el zurron y el bordon. */
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

    /**
     * Hedda (parte 7ah): el de brazos finos con el chal sobre los hombros, el faldon de la falda que cae de las rodillas al suelo, los
     * zapatos, el taburete (asiento y cuatro patas), el ovillo en el suelo a su derecha y, en las manos, las agujas y la labor roja.
     */
    public static LayerDefinition crearHedda() {
        MeshDefinition m = PlayerModel.createMesh(CubeDeformation.NONE, true);
        PartDefinition raiz = m.getRoot();
        PartDefinition cuerpo = raiz.getChild("body");
        cuerpo.addOrReplaceChild("chal", CubeListBuilder.create().texOffs(64, 0).addBox(-4.5f, -0.3f, -2.5f, 9, 7, 5, new CubeDeformation(0.3f)),
                PartPose.ZERO);
        raiz.addOrReplaceChild("faldon", CubeListBuilder.create().texOffs(64, 12).addBox(-4.5f, 0f, -1f, 9, 10, 2), PartPose.offset(0f, 10f, -11.5f));
        raiz.addOrReplaceChild("zapatos", CubeListBuilder.create().texOffs(88, 12).addBox(-3.5f, 0f, -2f, 7, 1, 3), PartPose.offset(0f, 19f, -12.5f));
        raiz.addOrReplaceChild("taburete", CubeListBuilder.create().texOffs(64, 26).addBox(-4.5f, 12f, -5f, 9, 2, 8)
                .texOffs(98, 26).addBox(-4f, 14f, -4.5f, 1, 6, 1).texOffs(98, 26).addBox(3f, 14f, -4.5f, 1, 6, 1)
                .texOffs(98, 26).addBox(-4f, 14f, 1.5f, 1, 6, 1).texOffs(98, 26).addBox(3f, 14f, 1.5f, 1, 6, 1), PartPose.ZERO);
        raiz.addOrReplaceChild("ovillo", CubeListBuilder.create().texOffs(102, 26).addBox(-1.5f, 0f, -1.5f, 3, 3, 3), PartPose.offset(-7.5f, 17f, -3f));
        PartDefinition bd = raiz.getChild("right_arm");
        bd.addOrReplaceChild("aguja_d", CubeListBuilder.create().texOffs(114, 26).addBox(-0.5f, 9.2f, -5.5f, 1, 1, 6), PartPose.ZERO);
        bd.addOrReplaceChild("labor", CubeListBuilder.create().texOffs(64, 38).addBox(-0.5f, 10f, -3.5f, 4, 5, 1), PartPose.ZERO);
        PartDefinition bi = raiz.getChild("left_arm");
        bi.addOrReplaceChild("aguja_i", CubeListBuilder.create().texOffs(114, 26).addBox(-0.5f, 9.2f, -5.5f, 1, 1, 6), PartPose.ZERO);
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

    @Override
    public boolean shouldRender(Viajero v, Frustum f, double x, double y, double z) {
        return !Viajero.OCULTO.test(v) && super.shouldRender(v, f, x, y, z);
    }

    /** Lo comun: el modelo de jugador que se dibuja con transparencia ({@link #alfa}) y lo que baja respecto al suelo (sentado...). */
    public abstract static class Base<T extends LivingEntity> extends PlayerModel<T> {
        public float alfa = 1f;

        protected Base(ModelPart raiz, boolean fino) {
            super(raiz, fino);
        }

        /** Lo que se dibuja por debajo de donde esta (bloques). */
        public float bajar(T e) {
            return 0f;
        }

        /** Las capas de encima siguen a las de debajo. */
        protected void capas() {
            hat.copyFrom(head);
            rightSleeve.copyFrom(rightArm);
            leftSleeve.copyFrom(leftArm);
            rightPants.copyFrom(rightLeg);
            leftPants.copyFrom(leftLeg);
            jacket.copyFrom(body);
        }
    }

    /**
     * El modelo de Oswin y de los peregrinos muertos: con el bordon o sin el, y lo transparente que se dibuja ({@link #alfa}, lo pone el
     * dibujante antes de cada vez; el tipo de dibujo de {@code PlayerModel} ya es translucido).
     */
    public static class Modelo<T extends LivingEntity> extends Base<T> {
        public final ModelPart baston;
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
            capas();
        }

        @Override
        public void renderToBuffer(PoseStack ps, VertexConsumer vc, int luz, int capa, float r, float g, float b, float a) {
            super.renderToBuffer(ps, vc, luz, capa, r, g, b, a * alfa);
            if (baston.visible) baston.render(ps, vc, luz, capa, r, g, b, a * alfa);
        }
    }

    /**
     * Hedda (parte 7ah). Sentada en su taburete: las piernas hacia delante tapadas por la falda que cae de las rodillas, las manos juntas
     * delante tejiendo (las agujas van y vienen) y la cabeza inclinada sobre la labor (algo menos si mira a alguien). De rodillas (variante
     * 1): sentada sobre los talones, la cabeza baja y la derecha tendida hacia su hijo, con la bufanda.
     */
    public static class ModeloHedda extends Base<Viajero> {
        private final ModelPart faldon, zapatos, taburete, ovillo, agujaD, agujaI;

        public ModeloHedda(ModelPart raiz) {
            super(raiz, true);
            faldon = raiz.getChild("faldon");
            zapatos = raiz.getChild("zapatos");
            taburete = raiz.getChild("taburete");
            ovillo = raiz.getChild("ovillo");
            agujaD = raiz.getChild("right_arm").getChild("aguja_d");
            agujaI = raiz.getChild("left_arm").getChild("aguja_i");
        }

        @Override
        public float bajar(Viajero v) {
            // sentada: el asiento del taburete a medio bloque; de rodillas: las piernas por el suelo
            return v.variante() == 1 ? 0.625f : 0.25f;
        }

        @Override
        public void setupAnim(Viajero v, float paso, float rapidez, float edad, float cabezaY, float cabezaX) {
            boolean rodillas = v.variante() == 1;
            riding = !rodillas;
            super.setupAnim(v, 0f, 0f, edad, cabezaY, cabezaX);
            float respira = Mth.sin(edad * 0.06f) * 0.02f;
            if (rodillas) {
                rightLeg.xRot = leftLeg.xRot = 1.5f;
                rightLeg.yRot = 0.12f;
                leftLeg.yRot = -0.12f;
                rightLeg.zRot = leftLeg.zRot = 0f;
                rightArm.xRot = -1.15f + respira;
                rightArm.yRot = -0.15f;
                rightArm.zRot = 0.05f;
                leftArm.xRot = -0.45f + respira;
                leftArm.yRot = 0.35f;
                leftArm.zRot = -0.05f;
                head.xRot = Math.max(head.xRot, 0f) * 0.4f + 0.5f;
                body.xRot = 0.1f;
            } else {
                // las agujas van y vienen
                float t = edad * 0.42f, a = Mth.sin(t) * 0.07f, b = Mth.sin(t * 2f + 1f) * 0.03f;
                rightLeg.xRot = leftLeg.xRot = -1.5f;
                rightLeg.yRot = 0.1f;
                leftLeg.yRot = -0.1f;
                rightLeg.zRot = leftLeg.zRot = 0f;
                rightArm.xRot = -0.88f + a + respira;
                rightArm.yRot = -0.45f + b;
                rightArm.zRot = 0.06f;
                leftArm.xRot = -0.88f - a + respira;
                leftArm.yRot = 0.45f - b;
                leftArm.zRot = -0.06f;
                head.xRot = head.xRot * 0.5f + 0.42f;
                body.xRot = 0.09f;
            }
            faldon.visible = zapatos.visible = taburete.visible = ovillo.visible = agujaD.visible = agujaI.visible = !rodillas;
            capas();
        }

        @Override
        public void renderToBuffer(PoseStack ps, VertexConsumer vc, int luz, int capa, float r, float g, float b, float a) {
            super.renderToBuffer(ps, vc, luz, capa, r, g, b, a * alfa);
            float al = a * alfa;
            if (faldon.visible) faldon.render(ps, vc, luz, capa, r, g, b, al);
            if (zapatos.visible) zapatos.render(ps, vc, luz, capa, r, g, b, al);
            if (taburete.visible) taburete.render(ps, vc, luz, capa, r, g, b, al);
            if (ovillo.visible) ovillo.render(ps, vc, luz, capa, r, g, b, al);
        }
    }

    @Override
    public void render(Viajero v, float giro, float pt, PoseStack ps, MultiBufferSource buf, int luz) {
        if (Viajero.OCULTO.test(v)) return;
        model = modelos[v.quien().ordinal()];
        model.alfa = ClientMisiones.alfa(v, pt);
        if (model.alfa <= 0.02f) return;
        if (model instanceof Modelo<Viajero> o) o.toca = v.toca(pt);
        float baja = model.bajar(v);
        ps.pushPose();
        if (baja != 0f) ps.translate(0, -baja, 0);
        super.render(v, giro, pt, ps, buf, luz);
        ps.popPose();
    }

    /** La campana en la mano derecha de Oswin mientras la toca (colgando: se le quita el giro del brazo). */
    static final class Campanilla extends RenderLayer<Viajero, Base<Viajero>> {
        private final CampanaRenderer.Modelo campana;

        Campanilla(RenderLayerParent<Viajero, Base<Viajero>> padre, CampanaRenderer.Modelo campana) {
            super(padre);
            this.campana = campana;
        }

        @Override
        public void render(PoseStack ps, MultiBufferSource buf, int luz, Viajero v, float paso, float rapidez, float pt, float edad, float cabezaY,
                           float cabezaX) {
            if (v.quien() != Viajero.Quien.OSWIN) return;
            float k = v.toca(pt);
            if (k <= 0.01f) return;
            Base<Viajero> m = getParentModel();
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
