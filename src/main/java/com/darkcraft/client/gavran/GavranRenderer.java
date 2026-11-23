package com.darkcraft.client.gavran;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gavran.Gavran;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** Dibuja a Ser Gavran: la textura de su fase, la capa que brilla (ojos, corazon, runas) y chispas verdes. */
public class GavranRenderer extends MobRenderer<Gavran, GavranModel> {
    private static final ResourceLocation[] TEX = new ResourceLocation[2];
    private static final ResourceLocation[] GLOW = new ResourceLocation[2];
    private static final DustParticleOptions VERDE = new DustParticleOptions(new Vector3f(0.4f, 1f, 0.5f), 0.7f);

    static {
        for (int i = 0; i < 2; i++) {
            TEX[i] = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/gavran/gavran_" + (i + 1) + ".png");
            GLOW[i] = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/gavran/brillo_" + (i + 1) + ".png");
        }
    }

    public GavranRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new GavranModel(ctx.bakeLayer(GavranModel.LAYER)), 1.6f);
        addLayer(new Brillo(this));
    }

    private static int idx(Gavran e) {
        return Mth.clamp(e.fase(), 1, 2) - 1;
    }

    @Override
    public ResourceLocation getTextureLocation(Gavran e) {
        return TEX[idx(e)];
    }

    /** En la fase 2 el arbol le ha hecho crecer. */
    @Override
    protected void scale(Gavran e, PoseStack ps, float pt) {
        if (e.fase() >= 2) ps.scale(Gavran.ESCALA_F2, Gavran.ESCALA_F2, Gavran.ESCALA_F2);
    }

    @Override
    public void render(Gavran e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int light) {
        this.shadowRadius = e.fase() >= 2 ? 1.6f * Gavran.ESCALA_F2 : 1.6f;
        this.model.alpha = e.alpha(pt);
        if (this.model.alpha <= 0.01f) return;
        super.render(e, yaw, pt, ps, buf, light);
        chispas(e, pt);
    }

    @Override
    protected RenderType getRenderType(Gavran e, boolean body, boolean translucent, boolean glowing) {
        if (e.alpha(0) < 0.999f) return RenderType.entityTranslucent(getTextureLocation(e));
        return super.getRenderType(e, body, translucent, glowing);
    }

    /** Armadura negra en una sala oscura: un poco de luz propia para que se le vea la silueta. */
    @Override
    protected int getBlockLightLevel(Gavran e, net.minecraft.core.BlockPos pos) {
        // en la entrada (cinematica, de lejos y en la rama) un poco mas
        return Math.max(super.getBlockLightLevel(e, pos), e.flag(Gavran.F_INTRO) ? 10 : e.fase() >= 2 ? 9 : 6);
    }

    @Override
    protected float getFlipDegrees(Gavran e) {
        return 0f;
    }

    /** Ojos, corazon del arbol, runas de la hoja y esporas: brillan en la oscuridad (el corazon late). */
    static class Brillo extends RenderLayer<Gavran, GavranModel> {
        Brillo(GavranRenderer r) {
            super(r);
        }

        @Override
        public void render(PoseStack ps, MultiBufferSource buf, int light, Gavran e, float limbSwing, float limbAmt, float pt, float age,
                           float headYaw, float headPitch) {
            if (e.isInvisible()) return;
            // latido: dos golpes rapidos y una pausa
            float f = (age % 26f) / 26f;
            float latido = (float) (Math.exp(-Math.pow((f - 0.1) / 0.05, 2)) + 0.7 * Math.exp(-Math.pow((f - 0.3) / 0.05, 2)));
            float k = 0.72f + 0.28f * Mth.clamp(latido, 0f, 1f);
            if (e.flag(Gavran.F_HABLA)) k = Math.min(1f, k + 0.15f);
            float a = getParentModel().alpha;
            getParentModel().renderToBuffer(ps, buf.getBuffer(RenderType.eyes(GLOW[idx(e)])), 0xF000F0, OverlayTexture.NO_OVERLAY,
                    k * a, k * a, k * a, 1f);
        }
    }

    // ------------------------------------------------------------------ chispas verdes de las runas al blandir el mandoble

    private void chispas(Gavran e, float pt) {
        if (e.ultimoFx == e.tickCount || e.alpha(pt) < 0.5f) return;
        e.ultimoFx = e.tickCount;
        Level lvl = e.level();
        boolean blande = e.acting();
        if (!blande && e.getRandom().nextInt(3) != 0) return;
        PoseStack m = new PoseStack();
        m.translate(Mth.lerp(pt, e.xo, e.getX()), Mth.lerp(pt, e.yo, e.getY()), Mth.lerp(pt, e.zo, e.getZ()));
        m.mulPose(Axis.YP.rotationDegrees(180f - Mth.rotLerp(pt, e.yBodyRotO, e.yBodyRot)));
        m.scale(-1f, -1f, 1f);
        m.translate(0f, -1.501f, 0f);
        for (ModelPart p : new ModelPart[]{model.raiz, model.cadera, model.torso, model.brazoD, model.antebrazoD, model.espada})
            p.translateAndRotate(m);
        int n = blande ? 3 : 1;
        for (int i = 0; i < n; i++) {
            float along = -(10f + e.getRandom().nextFloat() * 44f) / 16f;
            Vector4f v = new Vector4f(0f, 0f, along, 1f).mul(m.last().pose());
            lvl.addParticle(VERDE, v.x(), v.y(), v.z(), 0, 0.01, 0);
        }
        if (blande && e.getRandom().nextInt(4) == 0) {
            Vector4f v = new Vector4f(0f, 0f, -(20f + e.getRandom().nextFloat() * 30f) / 16f, 1f).mul(m.last().pose());
            lvl.addParticle(ParticleTypes.COMPOSTER, v.x(), v.y(), v.z(), 0, 0, 0);
        }
    }
}
