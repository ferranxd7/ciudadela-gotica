package com.ciudadela.client.rey;

import com.ciudadela.CiudadelaMod;
import com.ciudadela.client.ClientCine;
import com.ciudadela.client.ClientMusic;
import com.ciudadela.net.Net;
import com.ciudadela.rey.Rey;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.Input;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Cliente del combate con el Rey: avisos en el suelo, la camara mirandole mientras habla, la voz con
 * subtitulos, la musica de cada fase, la barra de vida propia y el cofre que sube en un haz de luz.
 */
@Mod.EventBusSubscriber(modid = CiudadelaMod.MODID, value = Dist.CLIENT)
public final class ReyCliente {
    private ReyCliente() {}

    private static int ticks;

    // ------------------------------------------------------------------ avisos del suelo

    private record AvisoC(Net.Aviso a, int start) {}

    private static final List<AvisoC> AVISOS = new ArrayList<>();

    public static void aviso(Net.Aviso a) {
        AVISOS.add(new AvisoC(a, ticks));
        if (AVISOS.size() > 120) AVISOS.remove(0);
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent e) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        float pt = e.getPartialTick();
        Vec3 cam = e.getCamera().getPosition();
        PoseStack ps = e.getPoseStack();
        MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
        if (!AVISOS.isEmpty()) {
            ps.pushPose();
            ps.translate(-cam.x, -cam.y, -cam.z);
            VertexConsumer vc = buf.getBuffer(Tipos.BRILLO);
            Matrix4f m = ps.last().pose();
            Iterator<AvisoC> it = AVISOS.iterator();
            while (it.hasNext()) {
                AvisoC av = it.next();
                float age = ticks - av.start() + pt;
                if (age > av.a().dur() + 8) {
                    it.remove();
                    continue;
                }
                dibujarAviso(vc, m, av.a(), age);
            }
            buf.endBatch(Tipos.BRILLO);
            ps.popPose();
        }
        if (cofre != null) dibujarCofre(mc, ps, buf, cam, pt);
        if (llave != null) dibujarLlave(mc, ps, buf, cam, pt);
    }

    private static float[] color(int c) {
        return switch (c) {
            case Net.Aviso.ORO -> new float[]{1f, 0.78f, 0.2f};
            case Net.Aviso.NARANJA -> new float[]{1f, 0.42f, 0.05f};
            default -> new float[]{1f, 0.08f, 0.04f};
        };
    }

    private static void dibujarAviso(VertexConsumer vc, Matrix4f m, Net.Aviso a, float age) {
        float[] c = color(a.color());
        float dur = Math.max(1, a.dur());
        float k = Mth.clamp(age / dur, 0f, 1f);
        float after = age > dur ? 1f - (age - dur) / 8f : 1f;
        boolean gold = a.color() == Net.Aviso.ORO;
        float pulse = 0.5f + 0.5f * Mth.sin(age * (gold ? 0.25f : 0.5f + k));
        float base = (gold ? 0.22f : 0.16f + 0.1f * k) * after;
        float edge = (0.55f + 0.35f * pulse) * after;
        double y = a.y() + 0.03;
        switch (a.forma()) {
            case Net.Aviso.CIRCULO -> {
                disco(vc, m, a.x(), y, a.z(), 0, a.a(), c, base);
                if (!gold) disco(vc, m, a.x(), y + 0.005, a.z(), 0, a.a() * k, c, 0.22f * after);
                disco(vc, m, a.x(), y + 0.01, a.z(), Math.max(0, a.a() - 0.18f), a.a(), c, edge);
                if (gold) disco(vc, m, a.x(), y + 0.01, a.z(), 0, a.a() * (0.3f + 0.15f * pulse), c, 0.25f * after);
            }
            case Net.Aviso.ANILLO -> {
                disco(vc, m, a.x(), y, a.z(), a.b(), a.a(), c, base + 0.1f);
                disco(vc, m, a.x(), y + 0.01, a.z(), a.b(), a.b() + 0.15f, c, edge);
                disco(vc, m, a.x(), y + 0.01, a.z(), a.a() - 0.15f, a.a(), c, edge);
            }
            case Net.Aviso.LINEA -> {
                double r = Math.toRadians(a.ang());
                double fx = -Math.sin(r), fz = Math.cos(r), sx = -fz, sz = fx;
                float w = a.b() / 2f, len = a.a();
                quad(vc, m, a.x() + sx * w, y, a.z() + sz * w, a.x() - sx * w, y, a.z() - sz * w,
                        a.x() - sx * w + fx * len, y, a.z() - sz * w + fz * len, a.x() + sx * w + fx * len, y, a.z() + sz * w + fz * len, c, base);
                float l2 = len * k;
                quad(vc, m, a.x() + sx * w, y + 0.005, a.z() + sz * w, a.x() - sx * w, y + 0.005, a.z() - sz * w,
                        a.x() - sx * w + fx * l2, y + 0.005, a.z() - sz * w + fz * l2, a.x() + sx * w + fx * l2, y + 0.005, a.z() + sz * w + fz * l2, c, 0.25f * after);
            }
            case Net.Aviso.SECTOR -> {
                double mid = Math.toRadians(a.ang() + 90);
                double half = Math.toRadians(a.b() / 2);
                int n = 24;
                for (int i = 0; i < n; i++) {
                    double a0 = mid - half + 2 * half * i / n, a1 = mid - half + 2 * half * (i + 1) / n;
                    float r = a.a();
                    quad(vc, m, a.x(), y, a.z(), a.x(), y, a.z(), a.x() + Math.cos(a1) * r, y, a.z() + Math.sin(a1) * r,
                            a.x() + Math.cos(a0) * r, y, a.z() + Math.sin(a0) * r, c, base);
                    float r2 = r * k;
                    quad(vc, m, a.x(), y + 0.005, a.z(), a.x(), y + 0.005, a.z(), a.x() + Math.cos(a1) * r2, y + 0.005, a.z() + Math.sin(a1) * r2,
                            a.x() + Math.cos(a0) * r2, y + 0.005, a.z() + Math.sin(a0) * r2, c, 0.22f * after);
                }
            }
        }
    }

    private static void disco(VertexConsumer vc, Matrix4f m, double x, double y, double z, float r0, float r1, float[] c, float a) {
        if (r1 <= r0 || a <= 0.003f) return;
        int n = Math.max(16, Math.min(96, (int) (r1 * 6)));
        for (int i = 0; i < n; i++) {
            double a0 = Math.PI * 2 * i / n, a1 = Math.PI * 2 * (i + 1) / n;
            double c0 = Math.cos(a0), s0 = Math.sin(a0), c1 = Math.cos(a1), s1 = Math.sin(a1);
            quad(vc, m, x + c0 * r0, y, z + s0 * r0, x + c0 * r1, y, z + s0 * r1, x + c1 * r1, y, z + s1 * r1, x + c1 * r0, y, z + s1 * r0, c, a);
        }
    }

    private static void quad(VertexConsumer vc, Matrix4f m, double x0, double y0, double z0, double x1, double y1, double z1,
                             double x2, double y2, double z2, double x3, double y3, double z3, float[] c, float a) {
        vc.vertex(m, (float) x0, (float) y0, (float) z0).color(c[0], c[1], c[2], a).endVertex();
        vc.vertex(m, (float) x1, (float) y1, (float) z1).color(c[0], c[1], c[2], a).endVertex();
        vc.vertex(m, (float) x2, (float) y2, (float) z2).color(c[0], c[1], c[2], a).endVertex();
        vc.vertex(m, (float) x3, (float) y3, (float) z3).color(c[0], c[1], c[2], a).endVertex();
    }

    // ------------------------------------------------------------------ el rey habla

    private static int subLinea, subFin;
    private static SoundInstance voz;
    /** Duracion de cada frase (ticks): la del audio (rey_voces.json) mas un poco. */
    private static final int[] DUR = duraciones();

    private static int[] duraciones() {
        int[] d = {0, 110, 120, 70, 70, 80, 65, 60, 85, 65, 120, 60, 75};
        try (var in = ReyCliente.class.getResourceAsStream("/assets/ciudadela/rey_voces.json")) {
            if (in == null) return d;
            var o = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonObject("duraciones");
            for (String k : o.keySet()) {
                int i = Integer.parseInt(k);
                if (i > 0 && i < d.length) d[i] = Math.round(o.get(k).getAsFloat() * 20) + 25;
            }
        } catch (Exception ignored) {
        }
        return d;
    }

    public static void habla(Net.ReyHabla m) {
        Minecraft mc = Minecraft.getInstance();
        if (m.linea() < 1 || m.linea() >= CiudadelaMod.REY_VOZ.length) return;
        if (voz != null) mc.getSoundManager().stop(voz);
        voz = new SimpleSoundInstance(CiudadelaMod.REY_VOZ[m.linea()].get().getLocation(), SoundSource.MASTER, 1.0f, 1.0f,
                SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true);
        mc.getSoundManager().play(voz);
        subLinea = m.linea();
        subFin = ticks + DUR[m.linea()];
    }

    // ------------------------------------------------------------------ la camara mira al rey

    private static int focoId = -1, focoFin, focoIni;
    private static float focoYaw, focoPitch;

    public static void foco(Net.ReyFoco m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        focoId = m.entity();
        focoIni = ticks;
        focoFin = ticks + m.ticks();
        focoYaw = mc.player.getYRot();
        focoPitch = mc.player.getXRot();
        if (mc.screen != null) mc.setScreen(null);
    }

    private static boolean enFoco() {
        return focoId >= 0 && ticks < focoFin;
    }

    @SubscribeEvent
    public static void onAngles(ViewportEvent.ComputeCameraAngles e) {
        if (!enFoco()) return;
        Minecraft mc = Minecraft.getInstance();
        Entity r = mc.level == null ? null : mc.level.getEntity(focoId);
        if (r == null || mc.player == null) return;
        float pt = (float) e.getPartialTick();
        Vec3 eye = mc.player.getEyePosition(pt);
        Vec3 head = r.getPosition(pt).add(0, 2.4, 0);
        Vec3 d = head.subtract(eye);
        float yaw = (float) (Mth.atan2(d.z, d.x) * Mth.RAD_TO_DEG) - 90f;
        float pitch = (float) -(Mth.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)) * Mth.RAD_TO_DEG);
        float k = Mth.clamp((ticks - focoIni + pt) / 25f, 0f, 1f);
        k = k * k * (3 - 2 * k);
        float out = Mth.clamp((focoFin - ticks - pt) / 15f, 0f, 1f);
        k *= out;
        e.setYaw(focoYaw + Mth.wrapDegrees(yaw - focoYaw) * k);
        e.setPitch(Mth.lerp(k, focoPitch, pitch));
        if (k > 0.98f && mc.player != null) {
            mc.player.setYRot(yaw);
            mc.player.setXRot(pitch);
        }
    }

    @SubscribeEvent
    public static void onInput(MovementInputUpdateEvent e) {
        if (!enFoco()) return;
        Input in = e.getInput();
        in.forwardImpulse = 0;
        in.leftImpulse = 0;
        in.up = in.down = in.left = in.right = false;
        in.jumping = false;
        in.shiftKeyDown = false;
    }

    @SubscribeEvent
    public static void onInteract(InputEvent.InteractionKeyMappingTriggered e) {
        if (!enFoco()) return;
        e.setCanceled(true);
        e.setSwingHand(false);
    }

    /** Bandas de cine durante la intro y subtitulos del rey. */
    public static void renderOverlay(ForgeGui gui, GuiGraphics g, float pt, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        dibujarRomper(g, w, h, pt);
        if (mc.options.hideGui && !enFoco()) return;
        int bh = 0;
        if (enFoco()) {
            float in = Mth.clamp((ticks - focoIni + pt) / 15f, 0f, 1f), out = Mth.clamp((focoFin - ticks - pt) / 15f, 0f, 1f);
            bh = Math.round(h * 0.11f * Math.min(in, out));
            if (bh > 0) {
                g.fill(0, 0, w, bh, 0xFF000000);
                g.fill(0, h - bh, w, h, 0xFF000000);
                g.fill(0, bh, w, bh + 1, 0x60800000);
                g.fill(0, h - bh - 1, w, h - bh, 0x60800000);
            }
        }
    }

    /**
     * Subtitulos del Rey: grandes y dentro de la franja negra de abajo (en la intro y en las cinematicas), o abajo con
     * fondo oscuro en plena pelea. Va en su propia capa, por encima de las bandas de cine, y se ve aunque la
     * cinematica esconda el resto de la interfaz.
     */
    public static void renderSubtitulos(ForgeGui gui, GuiGraphics g, float pt, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        boolean cine = ClientCine.active();
        if (subLinea <= 0 || ticks >= subFin + 10 || (cine && ClientCine.chapter() < 30)) return;
        if (mc.options.hideGui && !cine && !enFoco()) return;
        float a = Mth.clamp((subFin + 10 - ticks - pt) / 10f, 0f, 1f) * Mth.clamp((ticks - (subFin - DUR[subLinea]) + pt) / 6f, 0f, 1f);
        int alpha = Math.round(a * 255);
        if (alpha <= 8) return;
        Font font = mc.font;
        Component name = Component.translatable("ciudadela.rey.nombre_corto");
        Component line = Component.translatable("ciudadela.rey.voz." + subLinea);
        int band = cine ? Math.round(h * 0.12f) : (enFoco() ? Math.round(h * 0.11f) : 0);
        // tamano: grande, pero que quepa en la franja (o en la parte de abajo de la pantalla)
        float sc = 1.6f;
        List<FormattedCharSequence> lines;
        while (true) {
            lines = font.split(line, Math.round((w - 30) / sc));
            float alto = (lines.size() * 10 + 9) * sc;
            if (band <= 0 || alto <= band - 2 || sc <= 1.0f) break;
            sc -= 0.1f;
        }
        int lw = 0;
        for (FormattedCharSequence l : lines) lw = Math.max(lw, font.width(l));
        float alto = (lines.size() * 10 + 9) * sc;
        float y0 = band > 0 ? h - band / 2f - alto / 2f : h - 64 - alto;
        g.pose().pushPose();
        g.pose().translate(w / 2f, y0, 0);
        g.pose().scale(sc, sc, 1f);
        if (band <= 0)
            g.fill(-lw / 2 - 6, -2, lw / 2 + 6, lines.size() * 10 + 10, (Math.round(a * 150) << 24));
        g.pose().pushPose();
        g.pose().scale(0.75f, 0.75f, 1f);
        g.drawCenteredString(font, name, 0, 0, (alpha << 24) | 0xC8A040);
        g.pose().popPose();
        for (int i = 0; i < lines.size(); i++)
            g.drawCenteredString(font, lines.get(i), 0, 9 + i * 10, (alpha << 24) | 0xF0E6D8);
        g.pose().popPose();
    }

    // ------------------------------------------------------------------ barra de vida propia

    @SubscribeEvent
    public static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress e) {
        Component n = e.getBossEvent().getName();
        if (!(n.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents tc) || !tc.getKey().equals("entity.ciudadela.rey"))
            return;
        if (!e.isCancelable()) return;
        e.setCanceled(true);
        GuiGraphics g = e.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getGuiScaledWidth();
        int bw = Math.min(300, w - 40), x0 = w / 2 - bw / 2, y0 = e.getY() + 10;
        float p = e.getBossEvent().getProgress();
        Component title = Component.translatable("entity.ciudadela.rey");
        g.drawCenteredString(mc.font, title, w / 2, y0 - 11, 0xFFB8202A);
        g.fill(x0 - 2, y0 - 2, x0 + bw + 2, y0 + 8, 0xFF0A0606);
        g.fill(x0 - 1, y0 - 1, x0 + bw + 1, y0 + 7, 0xFF4A3A20);
        g.fill(x0, y0, x0 + bw, y0 + 6, 0xFF1A0808);
        int fill = Math.round(bw * p);
        int col = p > 2f / 3f ? 0xFFA01818 : (p > 1f / 3f ? 0xFFD05010 : 0xFFF09020);
        g.fill(x0, y0, x0 + fill, y0 + 6, col);
        g.fill(x0, y0, x0 + fill, y0 + 2, 0x40FFFFFF);
        for (int k = 1; k <= 2; k++) {
            int xs = x0 + bw * k / 3;
            g.fill(xs - 1, y0 - 3, xs + 1, y0 + 9, 0xFFC8A040);
        }
        e.setIncrement(26);
    }

    // ------------------------------------------------------------------ la llave gira en el sello

    private static Net.Llave llave;
    private static int llaveIni;

    public static void llave(Net.Llave m) {
        llave = m;
        llaveIni = ticks;
    }

    private static void dibujarLlave(Minecraft mc, PoseStack ps, MultiBufferSource.BufferSource buf, Vec3 cam, float pt) {
        Net.Llave k = llave;
        float t = (ticks - llaveIni + pt) / 20f;
        if (t > 3.4f || mc.player == null) {
            if (t > 3.4f) llave = null;
            return;
        }
        Vec3 lock = new Vec3(k.x(), k.y(), k.z());
        Vec3 dir = lock.subtract(mc.player.getEyePosition(pt));
        double yaw = Math.atan2(dir.z, dir.x);
        float in = Mth.clamp(t / 1.2f, 0f, 1f);
        in = in * in * (3 - 2 * in);
        float turn = Mth.clamp((t - 1.3f) / 0.8f, 0f, 1f);
        ps.pushPose();
        ps.translate(k.x() - cam.x - Math.cos(yaw) * (0.7 - 0.62 * in), k.y() - cam.y, k.z() - cam.z - Math.sin(yaw) * (0.7 - 0.62 * in));
        ps.mulPose(com.mojang.math.Axis.YP.rotation((float) (-yaw)));
        ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(turn * 90f));
        ps.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-45f));
        ps.scale(0.7f, 0.7f, 0.7f);
        mc.getItemRenderer().renderStatic(new net.minecraft.world.item.ItemStack(CiudadelaMod.LLAVE_ARBOL.get()),
                net.minecraft.world.item.ItemDisplayContext.FIXED, 0xF000F0, OverlayTexture.NO_OVERLAY, ps, buf, mc.level, 0);
        ps.popPose();
        buf.endBatch();
        if (turn > 0 && turn < 1 && ticks % 2 == 0)
            mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD, k.x(), k.y(), k.z(), 0, 0.02, 0);
    }

    // ------------------------------------------------------------------ la dimension se rompe

    private static int romperIni = -10000, romperDur = 30;

    public static void romper(Net.Romper m) {
        romperIni = ticks;
        romperDur = m.ticks();
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.GLASS_BREAK, 0.5f, 1.0f));
        mc.getSoundManager().play(SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_BREAK, 0.6f, 1.0f));
    }

    private static void dibujarRomper(GuiGraphics g, int w, int h, float pt) {
        float t = (ticks - romperIni + pt) / 20f;
        float dur = romperDur / 20f;
        if (t < 0 || t > dur + 1.2f) return;
        // grietas que salen del centro
        float grow = Mth.clamp(t / (dur * 0.8f), 0f, 1f);
        int cx = w / 2, cy = h / 2;
        RandomSource r = RandomSource.create(1234);
        int alpha = Math.round(255 * Mth.clamp(1 - (t - dur) / 0.4f, 0f, 1f));
        if (alpha > 4)
            for (int ray = 0; ray < 16; ray++) {
                double a = ray * Math.PI * 2 / 16 + r.nextDouble() * 0.3;
                double x = cx, y = cy;
                double len = (Math.max(w, h) * 0.75) * grow;
                int steps = 14;
                for (int i = 0; i < steps; i++) {
                    a += (r.nextDouble() - 0.5) * 0.7;
                    double seg = len / steps;
                    double nx = x + Math.cos(a) * seg, ny = y + Math.sin(a) * seg;
                    int n = (int) Math.max(1, seg / 2);
                    for (int k = 0; k <= n; k++) {
                        int px = (int) (x + (nx - x) * k / n), py = (int) (y + (ny - y) * k / n);
                        g.fill(px - 1, py - 1, px + 1, py + 1, (alpha << 24) | 0xFFF4E8);
                    }
                    if (i == 6 && r.nextInt(2) == 0) {
                        // rama
                        double ba = a + (r.nextBoolean() ? 0.9 : -0.9), bx = nx, by = ny;
                        for (int b = 0; b < 5; b++) {
                            double ex = bx + Math.cos(ba) * seg * 0.6, ey = by + Math.sin(ba) * seg * 0.6;
                            int m = (int) Math.max(1, seg * 0.3);
                            for (int k = 0; k <= m; k++) g.fill((int) (bx + (ex - bx) * k / m), (int) (by + (ey - by) * k / m),
                                    (int) (bx + (ex - bx) * k / m) + 1, (int) (by + (ey - by) * k / m) + 1, (alpha << 24) | 0xE8DCCC);
                            bx = ex;
                            by = ey;
                            ba += (r.nextDouble() - 0.5) * 0.6;
                        }
                    }
                    x = nx;
                    y = ny;
                }
            }
        // destello blanco al romperse
        if (t > dur * 0.8f) {
            float f = Mth.clamp(1 - (t - dur * 0.85f) / 1.0f, 0f, 1f);
            if (t < dur * 0.85f) f = (t - dur * 0.8f) / (dur * 0.05f);
            int a2 = Math.round(Mth.clamp(f, 0f, 1f) * 255);
            if (a2 > 3) g.fill(0, 0, w, h, (a2 << 24) | 0xFFFFFF);
        }
    }

    // ------------------------------------------------------------------ cofre que sube

    private static Net.Cofre cofre;
    private static int cofreIni;

    public static void cofre(Net.Cofre m) {
        cofre = m;
        cofreIni = ticks;
        ClientMusic.boss(0, false);
    }

    private static void dibujarCofre(Minecraft mc, PoseStack ps, MultiBufferSource.BufferSource buf, Vec3 cam, float pt) {
        Net.Cofre c = cofre;
        float age = ticks - cofreIni + pt;
        if (age > c.ticks()) {
            cofre = null;
            return;
        }
        float fade = Mth.clamp((c.ticks() - age) / 40f, 0f, 1f) * Mth.clamp(age / 10f, 0f, 1f);
        ps.pushPose();
        ps.translate(c.x() - cam.x, c.y() - cam.y, c.z() - cam.z);
        // haz de luz dorado
        float[] col = {1.0f * fade + 0.0001f, 0.82f * fade + 0.0001f, 0.3f * fade + 0.0001f};
        BeaconRenderer.renderBeaconBeam(ps, buf, BeaconRenderer.BEAM_LOCATION, pt, 1.0f, mc.level.getGameTime(), 0, 40, col, 0.25f * fade + 0.05f, 0.35f * fade + 0.05f);
        // el cofre subiendo del suelo
        float rise = Mth.clamp(age / com.ciudadela.rey.Victoria.SUBIDA, 0f, 1f);
        if (rise < 1f) {
            float yy = -1f + rise * rise * (3 - 2 * rise);
            ps.pushPose();
            ps.translate(0, yy, 0);
            mc.getBlockRenderer().renderSingleBlock(Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), ps, buf,
                    0xF000F0, OverlayTexture.NO_OVERLAY);
            ps.popPose();
        }
        ps.popPose();
        buf.endBatch();
    }

    // ------------------------------------------------------------------ cada tick: musica y particulas

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused()) return;
        ticks++;
        // la musica del combate: la del rey luchando mas cercano
        if (ticks % 10 == 0) {
            int fase = 0;
            boolean suave = false;
            for (Entity en : mc.level.entitiesForRendering()) {
                boolean intro = en instanceof Rey r0 && r0.flag(Rey.F_INTRO) && !r0.luchando();
                if (en instanceof Rey r && (r.luchando() || intro) && r.distanceToSqr(mc.player) < 90 * 90 && r.fase() >= fase) {
                    // en la intro (puertas cerradas, hablando desde el trono) suena flojita
                    suave = intro;
                    fase = r.fase();
                }
            }
            ClientMusic.boss(fase, suave);
        }
        // chispas doradas del cofre
        if (cofre != null && ticks % 2 == 0) {
            for (int i = 0; i < 4; i++) {
                double a = mc.level.random.nextDouble() * Math.PI * 2, r = 0.3 + mc.level.random.nextDouble() * 1.4;
                mc.level.addParticle(ParticleTypes.END_ROD, cofre.x() + 0.5 + Math.cos(a) * r, cofre.y() + mc.level.random.nextDouble() * 3,
                        cofre.z() + 0.5 + Math.sin(a) * r, 0, 0.05, 0);
                mc.level.addParticle(com.ciudadela.rey.Rey.polvoOro(), cofre.x() + 0.5 + Math.cos(a) * r, cofre.y() + mc.level.random.nextDouble() * 4,
                        cofre.z() + 0.5 + Math.sin(a) * r, 0, 0.02, 0);
            }
        }
    }

    @SubscribeEvent
    public static void onLeave(ClientPlayerNetworkEvent.LoggingOut e) {
        AVISOS.clear();
        llave = null;
        focoId = -1;
        subLinea = 0;
        cofre = null;
    }
}
