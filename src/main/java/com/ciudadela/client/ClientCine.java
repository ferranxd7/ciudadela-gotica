package com.ciudadela.client;

import com.ciudadela.CiudadelaMod;
import com.ciudadela.cine.Shot;
import com.ciudadela.cine.Timeline;
import com.ciudadela.net.Net;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.Input;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Cinematica de los capitulos de la cronica: bandas negras de cine, "CAPITULO N" en rojo sangre
 * letra a letra con resplandor y gotas, rayo con destello y temblor, campana funebre, brasas, y en el
 * capitulo 1 la voz del narrador con subtitulos mientras la camara vuela por la isla.
 * Se salta manteniendo ESPACIO.
 */
@Mod.EventBusSubscriber(modid = CiudadelaMod.MODID, value = Dist.CLIENT)
public final class ClientCine {
    private ClientCine() {}

    private static final ResourceLocation OVERLAY = new ResourceLocation(CiudadelaMod.MODID, "cinematica");
    private static final ResourceLocation VIGNETTE = new ResourceLocation(CiudadelaMod.MODID, "textures/gui/vinieta.png");
    private static final int SKIP_TICKS = 24;

    private static boolean active;
    private static int chapter, ticks, skipHold, camShot = -1, boltId = -2_000_000;
    private static float total, flashT = -99, flashK = 0, shakeT = -99, shakeK = 0;
    private static List<Shot> shots = List.of();
    private static double[] bolts = new double[0];
    private static boolean[] boltDone = new boolean[0];
    private static boolean[] bellDone = new boolean[3];
    private static boolean titleBolt, narrStarted, camOn;
    private static CineCamera cam;
    private static CameraType savedCam;
    private static float lockYaw, lockPitch;
    private static SoundInstance narr;

    public static boolean active() {
        return active;
    }

    // ------------------------------------------------------------------ empezar y acabar

    public static void start(Net.CineStart m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (active) stop(false);
        active = true;
        chapter = m.chapter();
        shots = m.shots();
        bolts = m.bolts();
        boltDone = new boolean[bolts.length / 4];
        bellDone = new boolean[Timeline.BELLS.length];
        ticks = 0;
        skipHold = 0;
        camShot = -1;
        total = Timeline.total(chapter);
        flashT = shakeT = -99;
        titleBolt = narrStarted = camOn = false;
        narr = null;
        lockYaw = mc.player.getYRot();
        lockPitch = mc.player.getXRot();
        savedCam = mc.options.getCameraType();
        mc.options.setCameraType(CameraType.FIRST_PERSON);
        mc.getMusicManager().stopPlaying();
        if (mc.screen != null) mc.setScreen(null);
    }

    /** Termina la cinematica (al acabar, al saltarla o porque lo dice el servidor). */
    public static void stop(boolean skipped) {
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        active = false;
        if (narr != null) mc.getSoundManager().stop(narr);
        narr = null;
        if (camOn && mc.player != null) mc.setCameraEntity(mc.player);
        camOn = false;
        cam = null;
        if (savedCam != null) mc.options.setCameraType(savedCam);
        savedCam = null;
        if (skipped) Net.CH.sendToServer(new Net.CineSkip());
    }

    private static float time(float pt) {
        return (ticks + pt) / 20f;
    }

    // ------------------------------------------------------------------ cada tick

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !active) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            active = false;
            return;
        }
        if (mc.isPaused()) return;
        ticks++;
        float t = ticks / 20f;
        // saltar: mantener ESPACIO un momento
        if (mc.screen == null && mc.options.keyJump.isDown() && t > 0.5f) {
            if (++skipHold >= SKIP_TICKS) {
                stop(true);
                return;
            }
        } else skipHold = Math.max(0, skipHold - 2);
        // el rayo del titulo, delante del jugador
        if (!titleBolt && t >= Timeline.BOLT) {
            titleBolt = true;
            Vec3 eye = mc.player.getEyePosition();
            Vec3 look = mc.player.getLookAngle().multiply(1, 0, 1).normalize();
            Vec3 side = new Vec3(-look.z, 0, look.x);
            Vec3 p = eye.add(look.scale(26)).add(side.scale(7));
            int ground = mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(p.x), Mth.floor(p.z));
            double y = ground < eye.y - 30 ? eye.y - 3 : ground;
            bolt(mc, p.x, y, p.z, 1.0f);
        }
        // campana funebre
        for (int i = 0; i < Timeline.BELLS.length; i++) {
            if (bellDone[i] || t < Timeline.BELLS[i]) continue;
            bellDone[i] = true;
            mc.getSoundManager().play(SimpleSoundInstance.forUI(CiudadelaMod.CAMPANA.get(), 1.0f, 1.0f - i * 0.15f));
        }
        // la voz del narrador (capitulo 1)
        if (chapter == 1 && !narrStarted && t >= Timeline.NARR_START) {
            narrStarted = true;
            // en la categoria general (MASTER): suena aunque la barra de "Voz" este bajada
            ResourceLocation loc = CiudadelaMod.NARRACION_1.get().getLocation();
            narr = new SimpleSoundInstance(loc, SoundSource.MASTER, 1.0f, 1.0f,
                    SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true);
            mc.getSoundManager().play(narr);
            CiudadelaMod.LOGGER.info("[Ciudadela] Narracion del capitulo 1: {} (audio en el mod: {}, volumen general {})",
                    mc.getSoundManager().getSoundEvent(loc) != null ? "reproduciendo" : "NO ENCONTRADA", Timeline.hasAudio(),
                    mc.options.getSoundSourceVolume(SoundSource.MASTER));
        }
        // rayos programados por el servidor
        for (int k = 0; k < boltDone.length; k++) {
            if (boltDone[k] || t < bolts[k * 4]) continue;
            boltDone[k] = true;
            bolt(mc, bolts[k * 4 + 1], bolts[k * 4 + 2], bolts[k * 4 + 3], 0.55f);
        }
        // camara de cine
        if (!shots.isEmpty()) updateCamera(mc, t);
        if (camOn) mc.options.setCameraType(CameraType.FIRST_PERSON);
        else {
            mc.player.setYRot(lockYaw);
            mc.player.setXRot(lockPitch);
            mc.player.yRotO = lockYaw;
            mc.player.xRotO = lockPitch;
        }
        if (t >= total) stop(false);
    }

    private static void bolt(Minecraft mc, double x, double y, double z, float flash) {
        LightningBolt b = EntityType.LIGHTNING_BOLT.create(mc.level);
        if (b != null) {
            b.moveTo(x, y, z);
            b.setVisualOnly(true);
            b.setId(boltId--);
            mc.level.putNonPlayerEntity(b.getId(), b);
        }
        float t = ticks / 20f;
        flashT = t;
        flashK = flash;
        shakeT = t;
        shakeK = flash;
    }

    private static int shotIndex(float t) {
        if (t >= Timeline.back(chapter)) return -1;
        for (int i = shots.size() - 1; i >= 0; i--) if (t >= shots.get(i).t0) return t < shots.get(i).t1 + 0.5f ? i : -1;
        return -1;
    }

    private static void updateCamera(Minecraft mc, float t) {
        int idx = shotIndex(t);
        if (idx < 0) {
            if (camOn) {
                mc.setCameraEntity(mc.player);
                camOn = false;
            }
            return;
        }
        Shot s = shots.get(idx);
        double[] a = s.at(t);
        float[] ang = Shot.angles(a);
        if (!camOn || cam == null) {
            cam = new CineCamera(mc.level);
            camShot = -1;
        }
        boolean snap = idx != camShot;
        if (snap) {
            cam.moveTo(a[0], a[1] - CineCamera.EYE, a[2], ang[0], ang[1]);
            cam.setOldPosAndRot();
        } else {
            cam.setOldPosAndRot();
            float yaw = cam.getYRot() + Mth.wrapDegrees(ang[0] - cam.getYRot());
            cam.setPos(a[0], a[1] - CineCamera.EYE, a[2]);
            cam.setYRot(yaw);
            cam.setXRot(ang[1]);
        }
        camShot = idx;
        if (!camOn) {
            mc.setCameraEntity(cam);
            camOn = true;
        }
    }

    // ------------------------------------------------------------------ que no se mueva nada

    @SubscribeEvent
    public static void onOverlay(RenderGuiOverlayEvent.Pre e) {
        if (active && !e.getOverlay().id().equals(OVERLAY)) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onHand(RenderHandEvent e) {
        if (active) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Pre e) {
        if (active && camOn) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onInput(MovementInputUpdateEvent e) {
        if (!active) return;
        Input in = e.getInput();
        in.forwardImpulse = 0;
        in.leftImpulse = 0;
        in.up = in.down = in.left = in.right = false;
        in.jumping = false;
        in.shiftKeyDown = false;
    }

    @SubscribeEvent
    public static void onInteract(InputEvent.InteractionKeyMappingTriggered e) {
        if (!active) return;
        e.setCanceled(true);
        e.setSwingHand(false);
    }

    @SubscribeEvent
    public static void onScreen(ScreenEvent.Opening e) {
        if (active && (e.getNewScreen() instanceof InventoryScreen || e.getNewScreen() instanceof ChatScreen)) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLeave(ClientPlayerNetworkEvent.LoggingOut e) {
        active = false;
        camOn = false;
        cam = null;
        narr = null;
        if (savedCam != null) Minecraft.getInstance().options.setCameraType(savedCam);
        savedCam = null;
    }

    @SubscribeEvent
    public static void onAngles(ViewportEvent.ComputeCameraAngles e) {
        if (!active) return;
        float t = time((float) e.getPartialTick());
        float roll = 0;
        if (camOn && camShot >= 0 && camShot < shots.size()) {
            Shot s = shots.get(camShot);
            double[] a = s.at(t - 0.05f);
            float[] ang = Shot.angles(a);
            e.setYaw(ang[0]);
            e.setPitch(ang[1]);
            roll = s.roll;
        } else if (!camOn) {
            e.setYaw(lockYaw);
            e.setPitch(lockPitch);
            roll = -1.5f * smooth(t / 3f) * (1 - smooth((t - (total - 1.2f)) / 1.2f));
        }
        float k = shakeK * (float) Math.exp(-Math.max(0, t - shakeT) * 5.0) * (t >= shakeT ? 1 : 0);
        if (k > 0.002f) {
            e.setYaw(e.getYaw() + 1.1f * k * Mth.sin(t * 53f));
            e.setPitch(e.getPitch() + 0.8f * k * Mth.cos(t * 61f));
            roll += 1.4f * k * Mth.sin(t * 47f);
        }
        e.setRoll(e.getRoll() + roll);
    }

    @SubscribeEvent
    public static void onFov(ViewportEvent.ComputeFov e) {
        if (!active || !e.usedConfiguredFov()) return;
        float t = time((float) e.getPartialTick());
        if (camOn && camShot >= 0 && camShot < shots.size()) e.setFOV(shots.get(camShot).fov);
        else if (chapter != 1) e.setFOV(e.getFOV() * (1.0 - 0.12 * smooth(t / total)));
    }

    // ------------------------------------------------------------------ dibujo

    private static float clamp(float v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }

    private static float smooth(float v) {
        v = clamp(v);
        return v * v * (3 - 2 * v);
    }

    private static float easeOut(float v) {
        v = clamp(v);
        return 1 - (1 - v) * (1 - v) * (1 - v);
    }

    private static int argb(float a, int r, int g, int b) {
        int al = Math.round(clamp(a) * 255);
        return (al << 24) | (r << 16) | (g << 8) | b;
    }

    private static boolean visible(float a) {
        return a * 255 >= 4;
    }

    public static void renderOverlay(ForgeGui gui, GuiGraphics g, float pt, int w, int h) {
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        float t = time(pt);
        boolean ch1 = chapter == 1;
        float in = smooth(t / 0.8f), out = 1 - smooth((t - (total - 0.8f)) / 0.8f);
        float frame = Math.min(in, out);
        // mundo mas oscuro y vineta
        if (!ch1) g.fill(0, 0, w, h, argb(0.32f * frame, 8, 0, 0));
        else if (t < Timeline.NARR_START + 0.5f) g.fill(0, 0, w, h, argb(0.3f * Math.min(in, 1 - smooth((t - Timeline.OUT) / 1.0f)), 8, 0, 0));
        vignette(g, w, h, (ch1 ? 0.7f : 0.9f) * frame);
        // brasas que suben
        float emb = Math.min(smooth((t - 0.6f) / 1.0f), 1 - smooth((t - Timeline.OUT) / 1.2f));
        if (visible(emb)) embers(g, w, h, t, emb);
        // el titulo
        drawTitle(g, font, w, h, t);
        // fundidos a negro (cortes de plano y vuelta al jugador)
        float black = black(t);
        if (visible(black)) g.fill(0, 0, w, h, argb(black, 0, 0, 0));
        // bandas de cine
        int bh = Math.round(h * 0.12f * easeOut(t / 0.9f) * out);
        if (bh > 0) {
            g.fill(0, 0, w, bh, 0xFF000000);
            g.fill(0, h - bh, w, h, 0xFF000000);
            g.fill(0, bh, w, bh + 1, argb(0.35f, 90, 0, 0));
            g.fill(0, h - bh - 1, w, h - bh, argb(0.35f, 90, 0, 0));
        }
        if (ch1) subtitles(g, font, w, h, t, bh);
        // destello del rayo
        float fl = 0;
        if (t >= flashT) {
            float d = t - flashT;
            fl = flashK * (0.85f * (float) Math.exp(-d * 6.5) + (d > 0.22f ? 0.4f * (float) Math.exp(-(d - 0.22f) * 9) : 0));
        }
        if (visible(fl)) g.fill(0, 0, w, h, argb(fl, 235, 240, 255));
        skipHint(g, font, w, h, t, bh);
    }

    private static void vignette(GuiGraphics g, int w, int h, float a) {
        if (!visible(a)) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, clamp(a));
        g.blit(VIGNETTE, 0, 0, w, h, 0, 0, 256, 256, 256, 256);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    private static float hash(int i, int k) {
        float v = Mth.sin(i * 12.9898f + k * 78.233f) * 43758.547f;
        return v - Mth.floor(v);
    }

    private static void embers(GuiGraphics g, int w, int h, float t, float vis) {
        for (int i = 0; i < 46; i++) {
            float speed = 14 + 34 * hash(i, 1), phase = hash(i, 2);
            float y = h + 10 - ((t * speed + phase * (h + 20)) % (h + 20));
            float x = hash(i, 3) * w + Mth.sin(t * (0.6f + hash(i, 4)) + i) * 14;
            float flick = 0.45f + 0.55f * Mth.sin(t * (6 + 5 * hash(i, 5)) + i * 3);
            float a = vis * clamp(flick) * (0.35f + 0.5f * hash(i, 6)) * clamp(y / (h * 0.25f));
            if (!visible(a)) continue;
            int s = hash(i, 7) < 0.25f ? 2 : 1;
            int r = 255, gg = 90 + (int) (110 * hash(i, 8)), b = 20;
            g.fill((int) x, (int) y, (int) x + s, (int) y + s, argb(a, r, gg, b));
        }
    }

    /** "CAPITULO N" letra a letra, linea de adorno y el titulo del capitulo. */
    private static void drawTitle(GuiGraphics g, Font font, int w, int h, float t) {
        float fade = 1 - smooth((t - Timeline.OUT) / 0.7f);
        if (fade <= 0 || t < Timeline.LETTERS) return;
        float rise = smooth((t - Timeline.OUT) / 0.7f) * -6;
        PoseStack pose = g.pose();
        String head = I18n.get("ciudadela.capitulo", chapter);
        float S1 = Mth.clamp(w / 125f, 2.5f, 5.5f);
        int fw = font.width(Component.literal(head).withStyle(ChatFormatting.BOLD));
        float x0 = w / 2f - fw * S1 / 2f, cy = h * 0.40f + rise;
        float xc = 0;
        for (int i = 0; i < head.length(); i++) {
            String ch = head.substring(i, i + 1);
            Component cc = Component.literal(ch).withStyle(ChatFormatting.BOLD);
            int cw = font.width(cc);
            float a0 = Timeline.LETTERS + i * 0.11f;
            float p = clamp((t - a0) / 0.35f);
            if (p > 0 && !ch.isBlank()) {
                float a = p * fade;
                float sc = S1 * (1.8f - 0.8f * easeOut(p));
                float dy = (1 - easeOut(p)) * -8;
                pose.pushPose();
                pose.translate(x0 + (xc + cw / 2f) * S1, cy + dy, 0);
                pose.scale(sc, sc, 1);
                // resplandor rojo oscuro alrededor
                int glow = argb(0.28f * a, 70, 0, 0);
                if (visible(0.28f * a)) {
                    for (int ox = -1; ox <= 1; ox++)
                        for (int oy = -1; oy <= 1; oy++) {
                            if (ox == 0 && oy == 0) continue;
                            pose.pushPose();
                            pose.translate(ox * 0.6f, oy * 0.6f, 0);
                            g.drawString(font, cc, -cw / 2, -4, glow, false);
                            pose.popPose();
                        }
                }
                pose.pushPose();
                pose.translate(0.35f, 0.45f, 0);
                g.drawString(font, cc, -cw / 2, -4, argb(a, 35, 0, 0), false);
                pose.popPose();
                g.drawString(font, cc, -cw / 2, -4, argb(a, 158, 8, 8), false);
                pose.popPose();
                // gotas de sangre que escurren de algunas letras
                if (i % 3 == 1) drip(g, x0 + (xc + cw / 2f + (i % 2 == 0 ? -1 : 1)) * S1, cy + 4 * S1, S1, t - a0 - 0.5f, i, fade);
            }
            xc += cw;
        }
        // linea de adorno
        float lp = easeOut((t - Timeline.LINE) / 0.8f);
        if (lp > 0) {
            float half = fw * S1 * 0.55f * lp;
            int ly = Math.round(cy + 6.5f * S1);
            int col = argb(0.85f * fade, 120, 10, 10);
            g.fill(Math.round(w / 2f - half), ly, Math.round(w / 2f + half), ly + Math.max(1, Math.round(S1 / 3)), col);
            int d = Math.max(2, Math.round(S1 * 0.7f));
            for (int s = -1; s <= 1; s += 2) {
                int ex = Math.round(w / 2f + s * half);
                g.fill(ex - d / 2, ly - d / 2, ex + d / 2 + 1, ly + d / 2 + 1, col);
            }
            g.fill(w / 2 - d, ly - d, w / 2 + d + 1, ly + d + 1, col);
        }
        // titulo del capitulo, despacio y abriendo las letras
        float tp = clamp((t - Timeline.SUBTITLE) / 1.4f);
        if (tp > 0) {
            String title = I18n.get("ciudadela.cap." + chapter + ".titulo");
            float S2 = S1 * 0.5f;
            float spacing = 1.6f * easeOut(tp);
            float tw = 0;
            for (int i = 0; i < title.length(); i++) tw += font.width(title.substring(i, i + 1)) + spacing;
            tw -= spacing;
            float tx = w / 2f - tw * S2 / 2f, ty = cy + 10.5f * S1 + (1 - easeOut(tp)) * 4;
            float a = smooth(tp) * fade;
            pose.pushPose();
            pose.translate(tx, ty, 0);
            pose.scale(S2, S2, 1);
            float cx = 0;
            for (int i = 0; i < title.length(); i++) {
                String ch = title.substring(i, i + 1);
                pose.pushPose();
                pose.translate(cx, 0, 0);
                g.drawString(font, ch, 0, 0, argb(a * 0.7f, 30, 0, 0), false);
                pose.translate(-0.5f, -0.5f, 0);
                g.drawString(font, ch, 0, 0, argb(a, 196, 40, 32), false);
                pose.popPose();
                cx += font.width(ch) + spacing;
            }
            pose.popPose();
        }
    }

    private static void drip(GuiGraphics g, float x, float y, float S, float dt, int i, float fade) {
        if (dt <= 0) return;
        float maxLen = (3 + (i * 7) % 5) * S;
        float len = maxLen * easeOut(dt / 1.6f);
        int w = Math.max(1, Math.round(S * 0.55f));
        int col = argb(0.9f * fade, 110, 0, 0);
        g.fill(Math.round(x), Math.round(y), Math.round(x) + w, Math.round(y + len), col);
        g.fill(Math.round(x) - (w > 1 ? 1 : 0), Math.round(y + len) - w, Math.round(x) + w + (w > 1 ? 1 : 0), Math.round(y + len) + w / 2, col);
        float fall = dt - 1.6f;
        if (fall > 0 && fall < 1.2f) {
            float fy = y + len + 0.5f * 420 * fall * fall / 4f;
            g.fill(Math.round(x), Math.round(fy), Math.round(x) + w, Math.round(fy) + w + 1, argb(0.85f * fade * (1 - fall / 1.2f), 110, 0, 0));
        }
    }

    /** Negro en los cortes entre planos y para volver al jugador al final del capitulo 1. */
    private static float black(float t) {
        if (chapter != 1 || shots.isEmpty()) return 0;
        float a = 0;
        for (int i = 1; i < shots.size(); i++) {
            float tc = shots.get(i).t0;
            if (t >= tc - 0.5f && t < tc) a = Math.max(a, smooth(1 - (tc - t) / 0.5f));
            else if (t >= tc && t < tc + 1.0f) a = Math.max(a, 1 - smooth((t - tc) / 1.0f));
        }
        float back = Timeline.back(1);
        if (t >= back - 0.8f && t < back) a = Math.max(a, smooth(1 - (back - t) / 0.8f));
        else if (t >= back && t < total - 0.8f) a = 1;
        else if (t >= total - 0.8f) a = Math.max(a, 1 - smooth((t - (total - 0.8f)) / 0.8f));
        return a;
    }

    private static void subtitles(GuiGraphics g, Font font, int w, int h, float t, int bh) {
        float[][] lines = Timeline.lines();
        float n = t - Timeline.NARR_START;
        for (int i = 0; i < lines.length; i++) {
            float s = lines[i][0] - 0.15f, e = lines[i][1] + 0.5f;
            if (n < s || n > e) continue;
            float a = Math.min(clamp((n - s) / 0.25f), clamp((e - n) / 0.3f));
            if (!visible(a)) continue;
            Component line = Component.translatable("ciudadela.narracion.1." + (i + 1));
            List<FormattedCharSequence> parts = font.split(line, (int) (w * 0.8f));
            int lh = 10, y0 = h - Math.max(bh, 24) / 2 - parts.size() * lh / 2 + 1;
            for (int k = 0; k < parts.size(); k++) {
                FormattedCharSequence p = parts.get(k);
                int x = (w - font.width(p)) / 2;
                g.drawString(font, p, x + 1, y0 + k * lh + 1, argb(a * 0.8f, 20, 0, 0), false);
                g.drawString(font, p, x, y0 + k * lh, argb(a, 236, 226, 208), false);
            }
        }
    }

    private static void skipHint(GuiGraphics g, Font font, int w, int h, float t, int bh) {
        float a = Math.min(clamp((t - 1.5f) / 0.6f), 1 - clamp((t - (total - 1.2f)) / 0.4f)) * 0.6f;
        if (!visible(a) || bh < 12) return;
        String s = I18n.get("ciudadela.cine.saltar");
        PoseStack pose = g.pose();
        float sc = 0.75f;
        int tw = font.width(s);
        pose.pushPose();
        pose.translate(w - 8 - tw * sc, h - bh / 2f - 5, 0);
        pose.scale(sc, sc, 1);
        g.drawString(font, s, 0, 0, argb(a, 200, 190, 180), false);
        if (skipHold > 0) {
            float p = Math.min(1, skipHold / (float) SKIP_TICKS);
            g.fill(0, 11, tw, 12, argb(0.5f, 60, 60, 60));
            g.fill(0, 11, Math.round(tw * p), 12, argb(0.95f, 180, 20, 20));
        }
        pose.popPose();
    }
}
