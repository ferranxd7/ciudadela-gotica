package com.ciudadela.client.rey;

import com.ciudadela.CiudadelaMod;
import com.ciudadela.rey.Volcan;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Lo que se ve en la dimension de los volcanes: cielo y niebla rojo oscuro, humo saliendo de los
 * crateres y meteoritos que caen al mar de lava (solo de adorno: no hacen nada).
 */
@Mod.EventBusSubscriber(modid = CiudadelaMod.MODID, value = Dist.CLIENT)
public final class VolcanCliente {
    private VolcanCliente() {}

    public static final ResourceLocation EFECTOS = new ResourceLocation(CiudadelaMod.MODID, "volcanes");

    /** Cielo: sin sol ni nubes, todo del color de la niebla (rojo oscuro). */
    public static final class Efectos extends DimensionSpecialEffects {
        public Efectos() {
            super(Float.NaN, true, SkyType.NONE, false, false);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 color, float brightness) {
            return color;
        }

        @Override
        public boolean isFoggyAt(int x, int z) {
            return false;
        }

        /** El cielo: el anillo de volcanes gigantes del horizonte, con humo, erupciones y meteoritos. */
        @Override
        public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, net.minecraft.client.Camera camera,
                                 org.joml.Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
            FondoVolcanes.dibujar(poseStack, ticks + partialTick);
            return true;
        }
    }

    // ------------------------------------------------------------------ meteoritos

    private static final class Meteoro {
        double x0, y0, z0, x1, y1, z1;
        int t, dur;
        float giro, tam;

        Vec3 at(float pt) {
            double k = (t + pt) / dur;
            k = k * k * 0.35 + k * 0.65;
            return new Vec3(x0 + (x1 - x0) * k, y0 + (y1 - y0) * k, z0 + (z1 - z0) * k);
        }
    }

    private static final List<Meteoro> METEOROS = new ArrayList<>();
    private static int siguiente;

    private static boolean aqui(Minecraft mc) {
        return mc.level != null && mc.level.dimension() == Volcan.NIVEL;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (!aqui(mc) || mc.isPaused()) {
            if (mc.level == null || !aqui(mc)) METEOROS.clear();
            return;
        }
        ClientLevel l = mc.level;
        RandomSource r = l.random;
        // meteoritos nuevos
        if (--siguiente <= 0) {
            siguiente = 12 + r.nextInt(30);
            Meteoro m = new Meteoro();
            double a = r.nextDouble() * Math.PI * 2, d = 40 + r.nextDouble() * 110;
            m.x1 = Math.cos(a) * d;
            m.z1 = Math.sin(a) * d;
            m.y1 = Volcan.MAR;
            double a0 = a + (r.nextDouble() - 0.5) * 1.2, back = 60 + r.nextDouble() * 60;
            m.x0 = m.x1 + Math.cos(a0) * back;
            m.z0 = m.z1 + Math.sin(a0) * back;
            m.y0 = 150 + r.nextDouble() * 50;
            m.dur = 45 + r.nextInt(35);
            m.giro = r.nextFloat() * 360;
            m.tam = 1.5f + r.nextFloat() * 2.5f;
            METEOROS.add(m);
        }
        Iterator<Meteoro> it = METEOROS.iterator();
        while (it.hasNext()) {
            Meteoro m = it.next();
            m.t++;
            Vec3 p = m.at(0);
            for (int i = 0; i < 3; i++)
                l.addAlwaysVisibleParticle(ParticleTypes.FLAME, true, p.x + (r.nextDouble() - 0.5) * m.tam, p.y + (r.nextDouble() - 0.5) * m.tam,
                        p.z + (r.nextDouble() - 0.5) * m.tam, 0, 0, 0);
            l.addAlwaysVisibleParticle(ParticleTypes.LARGE_SMOKE, true, p.x, p.y + 0.5, p.z, 0, 0.02, 0);
            if (r.nextInt(3) == 0) l.addAlwaysVisibleParticle(ParticleTypes.LAVA, true, p.x, p.y, p.z, 0, 0, 0);
            if (m.t >= m.dur) {
                // choca contra el mar de lava
                l.addAlwaysVisibleParticle(ParticleTypes.EXPLOSION_EMITTER, true, m.x1, m.y1 + 1, m.z1, 0, 0, 0);
                for (int i = 0; i < 24; i++)
                    l.addAlwaysVisibleParticle(ParticleTypes.LAVA, true, m.x1 + (r.nextDouble() - 0.5) * 4, m.y1 + 1, m.z1 + (r.nextDouble() - 0.5) * 4, 0, 0, 0);
                for (int i = 0; i < 10; i++)
                    l.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, m.x1 + (r.nextDouble() - 0.5) * 5, m.y1 + 1, m.z1 + (r.nextDouble() - 0.5) * 5,
                            0, 0.08, 0);
                double dist = mc.player == null ? 100 : Math.sqrt(mc.player.distanceToSqr(m.x1, m.y1, m.z1));
                float vol = (float) Math.max(0.15, Math.min(1.5, 60 / Math.max(1, dist)));
                l.playLocalSound(m.x1, m.y1, m.z1, SoundEvents.GENERIC_EXPLODE, SoundSource.AMBIENT, vol * 2f, 0.55f + r.nextFloat() * 0.2f, false);
                it.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent e) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || METEOROS.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (!aqui(mc)) return;
        float pt = e.getPartialTick();
        Vec3 cam = e.getCamera().getPosition();
        PoseStack ps = e.getPoseStack();
        MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
        for (Meteoro m : METEOROS) {
            Vec3 p = m.at(pt);
            ps.pushPose();
            ps.translate(p.x - cam.x, p.y - cam.y, p.z - cam.z);
            ps.mulPose(Axis.YP.rotationDegrees(m.giro + (m.t + pt) * 9f));
            ps.mulPose(Axis.XP.rotationDegrees((m.t + pt) * 13f));
            ps.scale(m.tam, m.tam, m.tam);
            ps.translate(-0.5, -0.5, -0.5);
            mc.getBlockRenderer().renderSingleBlock(Blocks.MAGMA_BLOCK.defaultBlockState(), ps, buf, 0xF000F0, OverlayTexture.NO_OVERLAY);
            ps.popPose();
        }
        buf.endBatch();
    }
}
