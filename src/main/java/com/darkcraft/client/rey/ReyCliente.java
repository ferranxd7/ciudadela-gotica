package com.darkcraft.client.rey;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.client.ClientCine;
import com.darkcraft.client.ClientMusic;
import com.darkcraft.net.Net;
import com.darkcraft.rey.Rey;
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
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
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
            AVISOS.removeIf(av -> ticks - av.start() > av.a().dur() + 9);
            // primero los rojos y despues los dorados por encima, para que se vean bien
            for (int pasada = 0; pasada < 2; pasada++)
                for (AvisoC av : AVISOS) {
                    boolean oro = av.a().color() == Net.Aviso.ORO;
                    if (oro != (pasada == 1)) continue;
                    float age = ticks - av.start() + pt;
                    if (age > av.a().dur() + 8) continue;
                    dibujarAviso(vc, m, av.a(), age);
                }
            buf.endBatch(Tipos.BRILLO);
            ps.popPose();
        }
        if (!CAPAS.isEmpty()) dibujarCapas(mc, ps, buf, cam, pt);
        if (cofre != null) dibujarCofre(mc, ps, buf, cam, pt);
        if (llave != null) dibujarLlave(mc, ps, buf, cam, pt);
    }

    private static float[] color(int c) {
        return switch (c) {
            case Net.Aviso.ORO -> new float[]{1f, 0.78f, 0.2f};
            case Net.Aviso.NARANJA -> new float[]{1f, 0.42f, 0.05f};
            case Net.Aviso.VERDE -> new float[]{0.3f, 1f, 0.42f};
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
                if (gold && a.b() > 0) {
                    // zona que salva del golpe final: muy brillante, con borde grueso y una columna de luz
                    float sube = Mth.clamp(age / 6f, 0f, 1f);
                    float yy = (float) y + 0.06f;
                    disco(vc, m, a.x(), yy, a.z(), 0, a.a(), c, (0.38f + 0.12f * pulse) * after * sube);
                    disco(vc, m, a.x(), yy + 0.01f, a.z(), Math.max(0, a.a() - 0.45f), a.a() + 0.05f, c, (0.85f + 0.15f * pulse) * after * sube);
                    disco(vc, m, a.x(), yy + 0.01f, a.z(), a.a() * 0.55f, a.a() * 0.55f + 0.2f, c, 0.55f * after * sube);
                    columna(vc, m, a.x(), yy, a.z(), a.a(), 12f * sube, c, (0.36f + 0.14f * pulse) * after);
                    columna(vc, m, a.x(), yy, a.z(), a.a() * 0.35f, 18f * sube, new float[]{1f, 0.95f, 0.7f}, 0.22f * after);
                    break;
                }
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
            case Net.Aviso.ONDA -> {
                // anillo que se abre por el suelo a a.b() bloques por tick hasta a.a(): hay que saltarlo
                float r = Math.min(a.a(), a.b() * age);
                float fin = Mth.clamp(1f - (r - a.a() * 0.85f) / (a.a() * 0.15f), 0f, 1f) * (age > dur ? 0f : 1f);
                disco(vc, m, a.x(), y, a.z(), Math.max(0, r - 1.1f), r, c, 0.55f * fin);
                disco(vc, m, a.x(), y + 0.01, a.z(), Math.max(0, r - 0.25f), r + 0.1f, c, 0.95f * fin);
                disco(vc, m, a.x(), y, a.z(), Math.max(0, r - 3.5f), Math.max(0, r - 1.1f), c, 0.12f * fin);
                columna(vc, m, a.x(), y, a.z(), r, 1.1f, c, 0.35f * fin);
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

    /** Pared de luz alrededor de un circulo: brilla abajo y se desvanece hacia arriba. */
    private static void columna(VertexConsumer vc, Matrix4f m, double x, double y, double z, float r, float h, float[] c, float a) {
        if (a <= 0.003f || h <= 0.05f) return;
        int n = Math.max(16, Math.min(64, (int) (r * 8)));
        for (int i = 0; i < n; i++) {
            double a0 = Math.PI * 2 * i / n, a1 = Math.PI * 2 * (i + 1) / n;
            float x0 = (float) (x + Math.cos(a0) * r), z0 = (float) (z + Math.sin(a0) * r);
            float x1 = (float) (x + Math.cos(a1) * r), z1 = (float) (z + Math.sin(a1) * r);
            vc.vertex(m, x0, (float) y, z0).color(c[0], c[1], c[2], a).endVertex();
            vc.vertex(m, x1, (float) y, z1).color(c[0], c[1], c[2], a).endVertex();
            vc.vertex(m, x1, (float) y + h, z1).color(c[0], c[1], c[2], 0f).endVertex();
            vc.vertex(m, x0, (float) y + h, z0).color(c[0], c[1], c[2], 0f).endVertex();
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
    /** Tick (de este reloj) en que acaba la frase que esta sonando. */
    private static int vozHasta;

    /** Una frase que espera su turno: no se corta la que suena. */
    private record Espera(int linea, int llego) {}

    private static final java.util.ArrayDeque<Espera> COLA = new java.util.ArrayDeque<>();

    /**
     * Una frase de un jefe (las de Gavran van de 101 a 118). Si todavia esta sonando otra, espera a que acabe (con una
     * pausa); las de pelea, si ya no pegan cuando les toca, no se dicen.
     */
    public static void habla(Net.ReyHabla m) {
        if (!com.darkcraft.rey.Voces.valida(m.linea())) return;
        if (hablando()) {
            if (COLA.size() < 4) COLA.add(new Espera(m.linea(), ticks));
            return;
        }
        sonar(m.linea());
    }

    private static boolean hablando() {
        return voz != null && ticks < vozHasta + com.darkcraft.rey.Voces.PAUSA
                && (ticks < vozHasta || Minecraft.getInstance().getSoundManager().isActive(voz));
    }

    /** Cada tick: si ya ha acabado la frase, la siguiente de la cola. */
    private static void cola() {
        while (!COLA.isEmpty() && !hablando()) {
            Espera e = COLA.poll();
            int espera = ticks - e.llego();
            // las de pelea pierden su momento en 2 s; las de las cinematicas se dicen siempre (si no es muy tarde)
            if (espera <= (com.darkcraft.rey.Voces.deRelleno(e.linea()) ? 40 : 200)) {
                sonar(e.linea());
                return;
            }
        }
    }

    private static void sonar(int linea) {
        Minecraft mc = Minecraft.getInstance();
        boolean gavran = com.darkcraft.rey.Voces.esGavran(linea), capitan = com.darkcraft.rey.Voces.esCapitan(linea),
                molinera = com.darkcraft.rey.Voces.esMolinera(linea), verdugo = com.darkcraft.rey.Voces.esVerdugo(linea),
                barquero = com.darkcraft.rey.Voces.esBarquero(linea), abad = com.darkcraft.rey.Voces.esAbad(linea),
                condestable = com.darkcraft.rey.Voces.esCondestable(linea), rectora = com.darkcraft.rey.Voces.esRectora(linea),
                ballenero = com.darkcraft.rey.Voces.esBallenero(linea), pontifice = com.darkcraft.rey.Voces.esPontifice(linea);
        int n = gavran ? linea - com.darkcraft.rey.Voces.BASE_GAVRAN : capitan ? linea - com.darkcraft.rey.Voces.BASE_CAPITAN
                : molinera ? linea - com.darkcraft.rey.Voces.BASE_MOLINERA : verdugo ? linea - com.darkcraft.rey.Voces.BASE_VERDUGO
                : barquero ? linea - com.darkcraft.rey.Voces.BASE_BARQUERO : abad ? linea - com.darkcraft.rey.Voces.BASE_ABAD
                : condestable ? linea - com.darkcraft.rey.Voces.BASE_CONDESTABLE : rectora ? linea - com.darkcraft.rey.Voces.BASE_RECTORA
                : ballenero ? linea - com.darkcraft.rey.Voces.BASE_BALLENERO : pontifice ? linea - com.darkcraft.rey.Voces.BASE_PONTIFICE : linea;
        var tabla = gavran ? DarkCraftMod.GAVRAN_VOZ : capitan ? DarkCraftMod.CAPITAN_VOZ : molinera ? DarkCraftMod.MOLINERA_VOZ
                : verdugo ? DarkCraftMod.VERDUGO_VOZ : barquero ? DarkCraftMod.BARQUERO_VOZ : abad ? DarkCraftMod.ABAD_VOZ
                : condestable ? DarkCraftMod.CONDESTABLE_VOZ : rectora ? DarkCraftMod.RECTORA_VOZ : ballenero ? DarkCraftMod.BALLENERO_VOZ
                : pontifice ? DarkCraftMod.PONTIFICE_VOZ : DarkCraftMod.REY_VOZ;
        if (n < 1 || n >= tabla.length) return;
        // la anterior ya ha acabado de hablar: se deja sonar la cola de su eco (solo se corta si aun estaba hablando)
        if (voz != null && ticks < vozHasta) mc.getSoundManager().stop(voz);
        voz = new SimpleSoundInstance(tabla[n].get().getLocation(), SoundSource.MASTER, 1.0f, 1.0f,
                SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true);
        mc.getSoundManager().play(voz);
        vozHasta = ticks + com.darkcraft.rey.Voces.ticks(linea);
        subLinea = linea;
        subFin = ticks + dur(linea);
    }

    /** Lo que se ve el subtitulo de la linea: lo que dura la frase y un rato mas para leerlo. */
    private static int dur(int linea) {
        return com.darkcraft.rey.Voces.ticks(linea) + com.darkcraft.rey.Voces.SUBTITULO_EXTRA;
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
            com.darkcraft.client.ui.Marco.bandas(g, w, h, bh);
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
        float a = Mth.clamp((subFin + 10 - ticks - pt) / 10f, 0f, 1f) * Mth.clamp((ticks - (subFin - dur(subLinea)) + pt) / 6f, 0f, 1f);
        int alpha = Math.round(a * 255);
        if (alpha <= 8) return;
        Font font = mc.font;
        boolean gavran = subLinea > com.darkcraft.gavran.Gavran.VOZ_BASE, capitan = com.darkcraft.rey.Voces.esCapitan(subLinea),
                molinera = com.darkcraft.rey.Voces.esMolinera(subLinea), verdugo = com.darkcraft.rey.Voces.esVerdugo(subLinea),
                barquero = com.darkcraft.rey.Voces.esBarquero(subLinea), abad = com.darkcraft.rey.Voces.esAbad(subLinea),
                condestable = com.darkcraft.rey.Voces.esCondestable(subLinea), rectora = com.darkcraft.rey.Voces.esRectora(subLinea),
                ballenero = com.darkcraft.rey.Voces.esBallenero(subLinea), pontifice = com.darkcraft.rey.Voces.esPontifice(subLinea);
        Component name = Component.translatable(gavran ? "darkcraft.gavran.nombre_corto" : capitan ? "darkcraft.capitan.nombre_corto"
                : molinera ? "darkcraft.molinera.nombre_corto" : verdugo ? "darkcraft.verdugo.nombre_corto"
                : barquero ? "darkcraft.barquero.nombre_corto" : abad ? "darkcraft.abad.nombre_corto"
                : condestable ? "darkcraft.condestable.nombre_corto" : rectora ? "darkcraft.rectora.nombre_corto"
                : ballenero ? "darkcraft.ballenero.nombre_corto" : pontifice ? "darkcraft.pontifice.nombre_corto" : "darkcraft.rey.nombre_corto");
        Component line = Component.translatable(gavran ? "darkcraft.gavran.voz." + (subLinea - com.darkcraft.gavran.Gavran.VOZ_BASE)
                : capitan ? "darkcraft.capitan.voz." + (subLinea - com.darkcraft.rey.Voces.BASE_CAPITAN)
                : molinera ? "darkcraft.molinera.voz." + (subLinea - com.darkcraft.rey.Voces.BASE_MOLINERA)
                : verdugo ? "darkcraft.verdugo.voz." + (subLinea - com.darkcraft.rey.Voces.BASE_VERDUGO)
                : barquero ? "darkcraft.barquero.voz." + (subLinea - com.darkcraft.rey.Voces.BASE_BARQUERO)
                : abad ? "darkcraft.abad.voz." + (subLinea - com.darkcraft.rey.Voces.BASE_ABAD)
                : condestable ? "darkcraft.condestable.voz." + (subLinea - com.darkcraft.rey.Voces.BASE_CONDESTABLE)
                : rectora ? "darkcraft.rectora.voz." + (subLinea - com.darkcraft.rey.Voces.BASE_RECTORA)
                : ballenero ? "darkcraft.ballenero.voz." + (subLinea - com.darkcraft.rey.Voces.BASE_BALLENERO)
                : pontifice ? "darkcraft.pontifice.voz." + (subLinea - com.darkcraft.rey.Voces.BASE_PONTIFICE) : "darkcraft.rey.voz." + subLinea);
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
        g.drawCenteredString(font, name, 0, 0, (alpha << 24) | (gavran ? 0x6FD08A : capitan ? 0x7FD8CC : 0xC8A040));
        g.pose().popPose();
        for (int i = 0; i < lines.size(); i++)
            g.drawCenteredString(font, lines.get(i), 0, 9 + i * 10, (alpha << 24) | 0xF0E6D8);
        g.pose().popPose();
    }

    // ------------------------------------------------------------------ barra de vida propia

    @SubscribeEvent
    public static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress e) {
        Component n = e.getBossEvent().getName();
        if (!(n.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents tc)) return;
        if (tc.getKey().equals("entity.darkcraft.gavran")) {
            com.darkcraft.client.gavran.GavranCliente.barra(e, ticks);
            return;
        }
        if (tc.getKey().equals("entity.darkcraft.capitan")) {
            com.darkcraft.client.enemigos.CapitanCliente.barra(e, ticks);
            return;
        }
        if (tc.getKey().equals("entity.darkcraft.brandt") || tc.getKey().equals("entity.darkcraft.corteza") || tc.getKey().equals("entity.darkcraft.molinera")
                || tc.getKey().equals("entity.darkcraft.verdugo") || tc.getKey().equals("entity.darkcraft.barquero") || tc.getKey().equals("entity.darkcraft.abad")
                || tc.getKey().equals("entity.darkcraft.condestable") || tc.getKey().equals("entity.darkcraft.rectora")
                || tc.getKey().equals("entity.darkcraft.ballenero") || tc.getKey().equals("entity.darkcraft.pontifice")) {
            com.darkcraft.client.enemigos.GuardianCliente.barra(e, ticks, tc.getKey().substring("entity.darkcraft.".length()));
            return;
        }
        if (!tc.getKey().equals("entity.darkcraft.rey"))
            return;
        if (!e.isCancelable()) return;
        e.setCanceled(true);
        GuiGraphics g = e.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        float p = Mth.clamp(e.getBossEvent().getProgress(), 0f, 1f);
        if (p < barraP - 0.0001f) barraGolpe = ticks;
        if (p > barraRastro) barraRastro = p;
        barraP = p;
        int fase = barraFase > 0 ? barraFase : (p > 0.667f ? 1 : (p > 0.334f ? 2 : 3));
        float sc = Math.min(1f, (sw - 12) / 256f);
        int y0 = e.getY() + 6;
        g.pose().pushPose();
        g.pose().translate(sw / 2f, y0, 0);
        g.pose().scale(sc, sc, 1f);
        g.pose().translate(-128, 0, 0);
        // nombre encima de la corona: el Rey que Espera, y desde la fase 2 el Rey que Arde
        Component title = Component.translatable(fase >= 2 ? "darkcraft.rey.barra_arde" : "entity.darkcraft.rey");
        g.drawCenteredString(mc.font, title, 128, -8, fase >= 2 ? 0xFFF0A040 : 0xFFE6C068);
        // hueco vacio, rastro del ultimo dano (dorado palido) y relleno de la fase
        g.blit(BARRA, 8, 19, 0, 104, 240, 7, 256, 128);
        int wf = Math.round(240 * p), wr = Math.round(240 * Math.max(p, barraRastro));
        if (wr > wf) g.blit(BARRA, 8 + wf, 19, wf, 112, wr - wf, 7, 256, 128);
        int cuadro = (ticks / 3) & 3;
        int v = fase == 1 ? 32 : (fase == 2 ? 40 + 8 * cuadro : 72 + 8 * cuadro);
        if (wf > 0) g.blit(BARRA, 8, 19, 0, v, wf, 7, 256, 128);
        // el marco (hierro, oro, corona y gemas) por encima
        g.blit(BARRA, 0, 0, 0, 0, 256, 32, 256, 128);
        g.pose().popPose();
        e.setIncrement(Math.round(42 * sc) + 4);
    }

    private static final net.minecraft.resources.ResourceLocation BARRA = new net.minecraft.resources.ResourceLocation(DarkCraftMod.MODID, "textures/gui/rey_barra.png");
    private static float barraP = 1f, barraRastro = 1f;
    private static int barraGolpe, barraFase;

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
        mc.getItemRenderer().renderStatic(new net.minecraft.world.item.ItemStack(DarkCraftMod.LLAVE_ARBOL.get()),
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

    // ------------------------------------------------------------------ fase 3: crece y se arranca la chaqueta

    /** Tick (de ReyCliente) en que cada Rey empezo a crecer; y las banderas vistas la ultima vez. */
    private static final java.util.Map<Integer, Integer> CRECE = new java.util.HashMap<>();
    private static final java.util.Map<Integer, Integer> VISTOS = new java.util.HashMap<>();

    /** Tamano del Rey: 1, o de 1 a 2 en un segundo y medio (con un pequeno rebote) cuando crece. */
    public static float escala(Rey e, float pt) {
        if (!e.flag(Rey.F_GRANDE)) return 1f;
        Integer t0 = CRECE.get(e.getId());
        if (t0 == null) return 2f;
        float k = Mth.clamp((ticks - t0 + pt) / 30f, 0f, 1f);
        float c1 = 1.70158f, c3 = c1 + 1f;
        float back = 1f + c3 * (float) Math.pow(k - 1, 3) + c1 * (float) Math.pow(k - 1, 2);
        return 1f + back;
    }

    private static void vigilar(Minecraft mc) {
        java.util.Set<Integer> vivos = new java.util.HashSet<>();
        for (Entity en : mc.level.entitiesForRendering()) {
            if (!(en instanceof Rey r)) continue;
            int id = r.getId();
            vivos.add(id);
            int f = (r.flag(Rey.F_SIN_CHAQUETA) ? 1 : 0) | (r.flag(Rey.F_GRANDE) ? 2 : 0);
            Integer antes = VISTOS.put(id, f);
            if (antes == null) {
                // recien aparecido: si ya es grande, ya lo es del todo
                if ((f & 2) != 0) CRECE.put(id, ticks - 100);
                continue;
            }
            if ((f & 1) != 0 && (antes & 1) == 0) arrancar(r);
            if ((f & 2) != 0 && (antes & 2) == 0) CRECE.put(id, ticks);
        }
        VISTOS.keySet().retainAll(vivos);
        CRECE.keySet().retainAll(vivos);
    }

    private static final class Capa {
        double x, y, z, vx, vy, vz, suelo;
        float yaw, giro, vgiro;
        int edad;
        boolean posada;
    }

    private static final List<Capa> CAPAS = new ArrayList<>();
    private static net.minecraft.client.model.geom.ModelPart capaModelo;
    private static final net.minecraft.resources.ResourceLocation CAPA_TEX = new net.minecraft.resources.ResourceLocation(DarkCraftMod.MODID, "textures/entity/rey/rey_3.png");

    /** La chaqueta sale volando hacia un lado, dando vueltas. */
    private static void arrancar(Rey r) {
        Capa c = new Capa();
        float yaw = r.yBodyRot;
        double lado = r.getRandom().nextBoolean() ? 1 : -1;
        double rad = Math.toRadians(yaw);
        double fx = -Math.sin(rad), fz = Math.cos(rad), sx = -fz * lado, sz = fx * lado;
        c.x = r.getX();
        c.y = r.getY() + 1.15;
        c.z = r.getZ();
        c.vx = sx * 0.32 + fx * 0.08;
        c.vz = sz * 0.32 + fz * 0.08;
        c.vy = 0.42;
        c.yaw = yaw;
        c.vgiro = (float) (lado * 16);
        c.suelo = r.getY();
        CAPAS.add(c);
    }

    private static void moverCapas(Minecraft mc) {
        Iterator<Capa> it = CAPAS.iterator();
        RandomSource rnd = mc.level.random;
        while (it.hasNext()) {
            Capa c = it.next();
            c.edad++;
            if (!c.posada) {
                c.x += c.vx;
                c.y += c.vy;
                c.z += c.vz;
                c.vy -= 0.04;
                c.vx *= 0.97;
                c.vz *= 0.97;
                c.giro += c.vgiro;
                if (rnd.nextInt(2) == 0) mc.level.addParticle(ParticleTypes.FLAME, c.x, c.y + 0.5, c.z, 0, 0.02, 0);
                if (c.y <= c.suelo + 0.05 && c.vy < 0) {
                    c.posada = true;
                    c.y = c.suelo + 0.02;
                    c.giro = 90f;
                    mc.level.playLocalSound(c.x, c.y, c.z, net.minecraft.sounds.SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1f, 0.7f, false);
                }
            } else {
                // arde en el suelo y se va haciendo ceniza
                if (c.edad < 260) {
                    for (int i = 0; i < 2; i++)
                        mc.level.addParticle(ParticleTypes.FLAME, c.x + (rnd.nextDouble() - 0.5) * 1.6, c.y + 0.1, c.z + (rnd.nextDouble() - 0.5) * 1.6, 0, 0.03, 0);
                    if (rnd.nextInt(3) == 0) mc.level.addParticle(ParticleTypes.SMOKE, c.x + (rnd.nextDouble() - 0.5), c.y + 0.3, c.z + (rnd.nextDouble() - 0.5), 0, 0.04, 0);
                }
                if (c.edad > 200 && rnd.nextInt(2) == 0)
                    mc.level.addParticle(ParticleTypes.WHITE_ASH, c.x + (rnd.nextDouble() - 0.5) * 1.5, c.y + 0.2, c.z + (rnd.nextDouble() - 0.5) * 1.5, 0, 0.02, 0);
            }
            if (c.edad > 320) it.remove();
        }
    }

    private static void dibujarCapas(Minecraft mc, PoseStack ps, MultiBufferSource.BufferSource buf, Vec3 cam, float pt) {
        if (capaModelo == null) {
            var root = ReyGeometria.crear().bakeRoot();
            var cadera = root.getChild("raiz").getChild("cadera");
            cadera.skipDraw = true;
            cadera.getChild("muslo_d").visible = false;
            cadera.getChild("muslo_i").visible = false;
            var torso = cadera.getChild("torso");
            torso.getChild("cabeza").visible = false;
            torso.getChild("brazo_d").visible = false;
            torso.getChild("brazo_i").visible = false;
            capaModelo = cadera;
        }
        VertexConsumer vc = buf.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(CAPA_TEX));
        for (Capa c : CAPAS) {
            double x = c.x + (c.posada ? 0 : c.vx * pt), y = c.y + (c.posada ? 0 : c.vy * pt), z = c.z + (c.posada ? 0 : c.vz * pt);
            float quema = Mth.clamp((c.edad - 120) / 140f, 0f, 1f);
            float col = 1f - 0.75f * quema;
            float hund = c.posada ? Mth.clamp((c.edad - 220) / 100f, 0f, 1f) : 0f;
            ps.pushPose();
            ps.translate(x - cam.x, y - cam.y - hund * 0.3, z - cam.z);
            ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180f - c.yaw));
            if (c.posada) {
                ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90f));
                ps.translate(0, -0.25, 0);
            } else ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(c.giro + (c.vgiro * pt)));
            ps.scale(-1f, -1f, 1f);
            ps.scale(1f, 1f - 0.6f * hund, 1f);
            ps.translate(0f, 18f / 16f - 0.55f, 0f);
            int luz = c.posada ? 0xF000B0 : 0xF000F0;
            capaModelo.render(ps, vc, luz, OverlayTexture.NO_OVERLAY, col, col * (1f - 0.3f * quema), col * (1f - 0.4f * quema), 1f);
            ps.popPose();
        }
        buf.endBatch();
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
        float rise = Mth.clamp(age / com.darkcraft.rey.Victoria.SUBIDA, 0f, 1f);
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
        cola();
        vigilar(mc);
        if (!CAPAS.isEmpty()) moverCapas(mc);
        // el rastro de la barra de vida baja despues de un momento
        if (barraRastro > barraP && ticks - barraGolpe > 14) barraRastro = Math.max(barraP, barraRastro - 0.01f);
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
            barraFase = fase;
            // Ser Gavran (temas 11 y 12) si no hay un rey luchando cerca
            if (fase == 0)
                for (Entity en : mc.level.entitiesForRendering())
                    if (en instanceof com.darkcraft.gavran.Gavran g && g.distanceToSqr(mc.player) < 96 * 96) {
                        if (g.flag(com.darkcraft.gavran.Gavran.F_CAMBIO)) {
                            // el cambio de fase: silencio mientras le envuelven las raices; la musica de la fase 2
                            // empieza cuando revienta el capullo
                            fase = g.fase() >= 2 ? 12 : 0;
                            suave = false;
                            break;
                        }
                        if (g.luchando() || g.flag(com.darkcraft.gavran.Gavran.F_INTRO)) {
                            suave = !g.luchando();
                            fase = 10 + g.fase();
                            break;
                        }
                    }
            // el Capitan de Brumavela (tema 21), si no hay otro jefe luchando cerca
            if (fase == 0)
                for (Entity en : mc.level.entitiesForRendering())
                    if (en instanceof com.darkcraft.enemigos.Capitan c && c.isAlive() && c.enPelea() && c.distanceToSqr(mc.player) < 72 * 72) {
                        fase = 21;
                        suave = false;
                        break;
                    }
            // los guardianes de Valdrenn (temas 31 y 32)
            if (fase == 0)
                for (Entity en : mc.level.entitiesForRendering())
                    if (en instanceof com.darkcraft.enemigos.Guardian gu && gu.isAlive() && gu.enPelea() && gu.distanceToSqr(mc.player) < 72 * 72) {
                        fase = gu.musica();
                        suave = false;
                        break;
                    }
            ClientMusic.boss(fase, suave);
        }
        // chispas doradas del cofre
        if (cofre != null && ticks % 2 == 0) {
            for (int i = 0; i < 4; i++) {
                double a = mc.level.random.nextDouble() * Math.PI * 2, r = 0.3 + mc.level.random.nextDouble() * 1.4;
                mc.level.addParticle(ParticleTypes.END_ROD, cofre.x() + 0.5 + Math.cos(a) * r, cofre.y() + mc.level.random.nextDouble() * 3,
                        cofre.z() + 0.5 + Math.sin(a) * r, 0, 0.05, 0);
                mc.level.addParticle(com.darkcraft.rey.Rey.polvoOro(), cofre.x() + 0.5 + Math.cos(a) * r, cofre.y() + mc.level.random.nextDouble() * 4,
                        cofre.z() + 0.5 + Math.sin(a) * r, 0, 0.02, 0);
            }
        }
    }

    @SubscribeEvent
    public static void onLeave(ClientPlayerNetworkEvent.LoggingOut e) {
        AVISOS.clear();
        llave = null;
        focoId = -1;
        COLA.clear();
        voz = null;
        vozHasta = 0;
        subLinea = 0;
        cofre = null;
    }
}
