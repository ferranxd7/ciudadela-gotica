package com.darkcraft.client.gavran;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gavran.Gavran;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * La barra de vida de Ser Gavran: marco de corteza y espinas, relleno verde que brilla, el rastro del ultimo
 * golpe, hojas que se caen de la barra cuando le hacen dano y, debajo, la postura (cuando se llena se le rompe
 * la guardia). La textura la pinta tools/hud/barra_gavran.py.
 */
public final class GavranCliente {
    private GavranCliente() {}

    private static final ResourceLocation BARRA = new ResourceLocation(DarkCraftMod.MODID, "textures/gui/gavran_barra.png");
    private static float p = 1f, rastro = 1f;
    private static int golpe, ultimo;
    private static float postura;
    private static final Random R = new Random();

    /** Una hoja que se cae de la barra (coordenadas de la barra, sin escalar). */
    private static final class Hoja {
        float x, y, vx, vy, rot, vr;
        int edad, tipo;
    }

    private static final List<Hoja> HOJAS = new ArrayList<>();

    public static void barra(CustomizeGuiOverlayEvent.BossEventProgress e, int ticks) {
        if (!e.isCancelable()) return;
        e.setCanceled(true);
        GuiGraphics g = e.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        float pt = mc.getFrameTime();
        float np = Mth.clamp(e.getBossEvent().getProgress(), 0f, 1f);
        // cada tick (no en cada fotograma): rastro, hojas y postura
        if (ticks != ultimo) {
            ultimo = ticks;
            if (np < p - 0.0001f) {
                golpe = ticks;
                int n = 2 + Math.min(6, Math.round((p - np) * 240));
                for (int i = 0; i < n; i++) {
                    Hoja h = new Hoja();
                    h.x = 8 + 240 * np + R.nextFloat() * Math.max(2, (p - np) * 240);
                    h.y = 20 + R.nextFloat() * 5;
                    h.vx = (R.nextFloat() - 0.5f) * 1.2f;
                    h.vy = -0.6f - R.nextFloat() * 0.8f;
                    h.rot = R.nextFloat() * 360;
                    h.vr = (R.nextFloat() - 0.5f) * 18;
                    h.tipo = R.nextInt(4);
                    HOJAS.add(h);
                }
            }
            if (np > rastro) rastro = np;
            if (rastro > np && ticks - golpe > 14) rastro = Math.max(np, rastro - 0.01f);
            p = np;
            Iterator<Hoja> it = HOJAS.iterator();
            while (it.hasNext()) {
                Hoja h = it.next();
                h.edad++;
                h.vy = Math.min(1.1f, h.vy + 0.12f);
                h.vx += (float) Math.sin((h.edad + h.tipo * 7) * 0.3f) * 0.08f;
                h.x += h.vx;
                h.y += h.vy;
                h.rot += h.vr;
                if (h.edad > 45) it.remove();
            }
            if (HOJAS.size() > 60) HOJAS.subList(0, HOJAS.size() - 60).clear();
            float want = 0;
            if (mc.level != null && mc.player != null)
                for (Entity en : mc.level.entitiesForRendering())
                    if (en instanceof Gavran gv && gv.luchando() && gv.distanceToSqr(mc.player) < 100 * 100) want = gv.postura() / 100f;
            postura += (want - postura) * 0.35f;
        }
        float sc = Math.min(1f, (sw - 12) / 256f);
        int y0 = e.getY() + 6;
        g.pose().pushPose();
        g.pose().translate(sw / 2f, y0, 0);
        g.pose().scale(sc, sc, 1f);
        g.pose().translate(-128, 0, 0);
        g.drawCenteredString(mc.font, Component.translatable("entity.darkcraft.gavran"), 128, -8, 0xFF9FE6A6);
        // hueco, rastro y relleno (la savia corre)
        g.blit(BARRA, 8, 19, 0, 80, 240, 7, 256, 128);
        int wf = Math.round(240 * p), wr = Math.round(240 * Math.max(p, rastro));
        if (wr > wf) g.blit(BARRA, 8 + wf, 19, wf, 88, wr - wf, 7, 256, 128);
        int cuadro = (ticks / 4) & 3;
        if (wf > 0) g.blit(BARRA, 8, 19, 0, 48 + 8 * cuadro, wf, 7, 256, 128);
        // destello al recibir un golpe
        if (ticks - golpe < 4 && wf > 0) g.fill(8 + Math.max(0, wf - 6), 19, 8 + wf, 26, 0x80E6FFE8);
        g.blit(BARRA, 0, 0, 0, 0, 256, 40, 256, 128);
        // postura: crece desde el centro hacia los lados; parpadea cuando esta a punto de romperse
        if (postura > 0.01f) {
            g.blit(BARRA, 8, 34, 0, 100, 240, 3, 256, 128);
            int w = Math.round(120 * Mth.clamp(postura, 0f, 1f));
            boolean casi = postura > 0.8f && (ticks / 3) % 2 == 0;
            if (w > 0) {
                g.blit(BARRA, 128 - w, 34, 120 - w, 96, 2 * w, 3, 256, 128);
                if (casi) g.fill(128 - w, 34, 128 + w, 37, 0x60FFFFFF);
            }
        }
        // hojas que caen
        for (Hoja h : HOJAS) {
            float a = Mth.clamp(1f - (h.edad + pt - 30) / 15f, 0f, 1f);
            g.pose().pushPose();
            g.pose().translate(h.x + h.vx * pt, h.y + h.vy * pt, 0);
            g.pose().mulPose(Axis.ZP.rotationDegrees(h.rot + h.vr * pt));
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1, 1, 1, a);
            g.blit(BARRA, -4, -4, h.tipo * 8, 104, 8, 8, 256, 128);
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1, 1, 1, 1);
            g.pose().popPose();
        }
        g.pose().popPose();
        e.setIncrement(Math.round(48 * sc) + 4);
    }
}
