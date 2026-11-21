package com.ciudadela.client.rey;

import com.ciudadela.CiudadelaMod;
import com.ciudadela.rey.Rey;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Vector4f;

/** Dibuja al Rey: textura segun la fase, capa que brilla (ojos, brasas, grietas de lava) y llamas en las espadas. */
public class ReyRenderer extends MobRenderer<Rey, ReyModel> {
    private static final ResourceLocation[] TEX = new ResourceLocation[3];
    private static final ResourceLocation[] GLOW = new ResourceLocation[3];

    static {
        for (int i = 0; i < 3; i++) {
            TEX[i] = new ResourceLocation(CiudadelaMod.MODID, "textures/entity/rey/rey_" + (i + 1) + ".png");
            GLOW[i] = new ResourceLocation(CiudadelaMod.MODID, "textures/entity/rey/brillo_" + (i + 1) + ".png");
        }
    }

    public ReyRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ReyModel(ctx.bakeLayer(ReyModel.LAYER)), 1.0f);
        addLayer(new Brillo(this));
    }

    private static int idx(Rey e) {
        return Mth.clamp(e.fase(), 1, 3) - 1;
    }

    @Override
    public ResourceLocation getTextureLocation(Rey e) {
        return TEX[idx(e)];
    }

    @Override
    public void render(Rey e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int light) {
        this.model.alpha = e.alpha(pt);
        if (this.model.alpha <= 0.01f) return;
        super.render(e, yaw, pt, ps, buf, light);
        llamas(e, pt);
    }

    @Override
    protected RenderType getRenderType(Rey e, boolean body, boolean translucent, boolean glowing) {
        if (e.alpha(0) < 0.999f) return RenderType.entityTranslucent(getTextureLocation(e));
        return super.getRenderType(e, body, translucent, glowing);
    }

    @Override
    protected float getFlipDegrees(Rey e) {
        return 0f;
    }

    /** Ojos, brasas y lava que brillan en la oscuridad. */
    static class Brillo extends RenderLayer<Rey, ReyModel> {
        Brillo(ReyRenderer r) {
            super(r);
        }

        @Override
        public void render(PoseStack ps, MultiBufferSource buf, int light, Rey e, float limbSwing, float limbAmt, float pt, float age, float headYaw, float headPitch) {
            if (e.isInvisible()) return;
            float k = switch (e.fase()) {
                case 2 -> 0.85f + 0.15f * Mth.sin(age * 0.35f);
                case 3 -> 0.75f + 0.25f * Mth.sin(age * 0.2f);
                default -> 0.9f;
            };
            float a = getParentModel().alpha;
            getParentModel().renderToBuffer(ps, buf.getBuffer(RenderType.eyes(GLOW[idx(e)])), 0xF000F0, OverlayTexture.NO_OVERLAY, k * a, k * a, k * a, 1f);
        }
    }

    // ------------------------------------------------------------------ llamas y lava en las hojas

    private void llamas(Rey e, float pt) {
        int f = e.fase();
        if (f < 2 || e.lastFx == e.tickCount || e.alpha(pt) < 0.5f) return;
        e.lastFx = e.tickCount;
        Level lvl = e.level();
        boolean swing = e.acting();
        for (int side = 0; side < 2; side++) {
            PoseStack m = new PoseStack();
            m.translate(Mth.lerp(pt, e.xo, e.getX()), Mth.lerp(pt, e.yo, e.getY()), Mth.lerp(pt, e.zo, e.getZ()));
            m.mulPose(Axis.YP.rotationDegrees(180f - Mth.rotLerp(pt, e.yBodyRotO, e.yBodyRot)));
            m.scale(-1f, -1f, 1f);
            m.translate(0f, -1.501f, 0f);
            ModelPart[] chain = side == 0
                    ? new ModelPart[]{model.raiz, model.cadera, model.torso, model.brazoD, model.antebrazoD, model.espadaD}
                    : new ModelPart[]{model.raiz, model.cadera, model.torso, model.brazoI, model.antebrazoI, model.espadaI};
            for (ModelPart p : chain) p.translateAndRotate(m);
            int n = swing ? 5 : 2;
            for (int i = 0; i < n; i++) {
                float along = -(3f + e.getRandom().nextFloat() * 26f) / 16f;
                Vector4f v = new Vector4f(0f, 0f, along, 1f).mul(m.last().pose());
                if (f == 2) {
                    lvl.addParticle(e.getRandom().nextInt(3) == 0 ? ParticleTypes.SMALL_FLAME : ParticleTypes.FLAME, v.x(), v.y(), v.z(),
                            0, 0.02 + e.getRandom().nextFloat() * 0.02, 0);
                } else {
                    if (e.getRandom().nextInt(swing ? 2 : 4) == 0) lvl.addParticle(ParticleTypes.DRIPPING_LAVA, v.x(), v.y(), v.z(), 0, 0, 0);
                    if (e.getRandom().nextInt(swing ? 3 : 8) == 0) lvl.addParticle(ParticleTypes.LAVA, v.x(), v.y(), v.z(), 0, 0, 0);
                    lvl.addParticle(ParticleTypes.SMOKE, v.x(), v.y(), v.z(), 0, 0.01, 0);
                }
            }
        }
        if (f == 3 && e.getRandom().nextInt(4) == 0) {
            // grietas que echan humo
            lvl.addParticle(ParticleTypes.SMOKE, e.getX() + (e.getRandom().nextDouble() - 0.5), e.getY() + 1 + e.getRandom().nextDouble() * 1.5,
                    e.getZ() + (e.getRandom().nextDouble() - 0.5), 0, 0.03, 0);
        }
    }

    static Minecraft mc() {
        return Minecraft.getInstance();
    }
}
