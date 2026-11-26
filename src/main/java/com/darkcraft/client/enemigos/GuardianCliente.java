package com.darkcraft.client.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Las barras de vida de los dos guardianes de Valdrenn (parte 4d), como la del Capitan: Ser Brandt (hierro negro con
 * filos rojos, el paves con la corona; sangre que corre y chispas al recibir dano) y Vareth (corteza con musgo y raices,
 * el corazon verde; savia que corre y hojas que caen); y la de la Molinera de Velmor (parte 7f: la viga del molino con
 * espigas y la piedra de moler; brasas que corren y ceniza que cae) y la del Verdugo de la Arboleda (parte 7h: la viga de la
 * horca con el lazo y la capucha; una soga con sangre que gira y hebras que caen) y la del Barquero de la Laguna Negra
 * (parte 7j: el remo con el garfio, las monedas del peaje y la linterna de almas; agua negra que corre y gotas que caen).
 * Las texturas las pinta tools/hud/barra_guardianes.py.
 */
public final class GuardianCliente {
    private GuardianCliente() {}

    private static ResourceLocation BARRA = new ResourceLocation(DarkCraftMod.MODID, "textures/gui/brandt_barra.png");
    private static String actual = "";
    private static float p = 1f, rastro = 1f;
    private static int golpe, ultimo;
    private static final Random R = new Random();

    /** Una gota que cae de la barra (coordenadas de la barra, sin escalar). */
    private static final class Gota {
        float x, y, vx, vy;
        int edad, tipo;
    }

    private static final List<Gota> GOTAS = new ArrayList<>();

    /** {@code nombre}: "brandt", "corteza", "molinera", "verdugo" o "barquero" (el de la clave de su nombre). */
    public static void barra(CustomizeGuiOverlayEvent.BossEventProgress e, int ticks, String nombre) {
        if (!e.isCancelable()) return;
        if (!nombre.equals(actual)) {
            actual = nombre;
            BARRA = new ResourceLocation(DarkCraftMod.MODID, "textures/gui/" + nombre + "_barra.png");
            ultimo = ticks - 100;
        }
        boolean hojas = nombre.equals("corteza"), ceniza = nombre.equals("molinera"), soga = nombre.equals("verdugo"), agua = nombre.equals("barquero");
        e.setCanceled(true);
        GuiGraphics g = e.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        float pt = mc.getFrameTime();
        float np = Mth.clamp(e.getBossEvent().getProgress(), 0f, 1f);
        // cada tick (no en cada fotograma): el rastro y las gotas
        if (ticks != ultimo) {
            // una barra nueva (otra pelea): sin rastro de la anterior
            if (Math.abs(ticks - ultimo) > 40) {
                p = rastro = np;
                GOTAS.clear();
            }
            ultimo = ticks;
            if (np < p - 0.0001f) {
                golpe = ticks;
                int n = 2 + Math.min(6, Math.round((p - np) * 240));
                for (int i = 0; i < n; i++) {
                    Gota h = new Gota();
                    h.x = 8 + 240 * np + R.nextFloat() * Math.max(2, (p - np) * 240);
                    h.y = 22 + R.nextFloat() * 4;
                    h.vx = (R.nextFloat() - 0.5f) * 0.5f;
                    h.vy = 0.2f + R.nextFloat() * 0.5f;
                    h.tipo = R.nextInt(4);
                    GOTAS.add(h);
                }
            }
            if (np > rastro) rastro = np;
            if (rastro > np && ticks - golpe > 14) rastro = Math.max(np, rastro - 0.01f);
            p = np;
            Iterator<Gota> it = GOTAS.iterator();
            while (it.hasNext()) {
                Gota h = it.next();
                h.edad++;
                if (actual.equals("corteza") || actual.equals("molinera") || actual.equals("verdugo") || actual.equals("barquero")) {
                    h.vy = Math.min(0.9f, h.vy + 0.05f);
                    h.x += h.vx + (float) Math.sin((h.edad + h.tipo * 7) * 0.35) * 0.45f;
                } else {
                    h.vy = Math.min(2.4f, h.vy + 0.2f);
                    h.x += h.vx * 2.2f;
                }
                h.y += h.vy;
                if (h.edad > 32) it.remove();
            }
            if (GOTAS.size() > 60) GOTAS.subList(0, GOTAS.size() - 60).clear();
        }
        float sc = Math.min(1f, (sw - 12) / 256f);
        int y0 = e.getY() + 6;
        g.pose().pushPose();
        g.pose().translate(sw / 2f, y0, 0);
        g.pose().scale(sc, sc, 1f);
        g.pose().translate(-128, 0, 0);
        g.drawCenteredString(mc.font, Component.translatable("entity.darkcraft." + nombre), 128, -8,
                hojas ? 0xFFB9F0C4 : ceniza ? 0xFFF2DDB8 : soga ? 0xFFE6D2B0 : agua ? 0xFFBFEAF2 : 0xFFF0C8B8);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        // hueco, rastro y relleno (la bruma corre)
        g.blit(BARRA, 8, 19, 0, 80, 240, 7, 256, 128);
        int wf = Math.round(240 * p), wr = Math.round(240 * Math.max(p, rastro));
        if (wr > wf) g.blit(BARRA, 8 + wf, 19, wf, 88, wr - wf, 7, 256, 128);
        int cuadro = (ticks / 5) & 3;
        if (wf > 0) g.blit(BARRA, 8, 19, 0, 48 + 8 * cuadro, wf, 7, 256, 128);
        // destello al recibir un golpe
        if (ticks - golpe < 4 && wf > 0)
            g.fill(8 + Math.max(0, wf - 6), 19, 8 + wf, 26, hojas ? 0x80E6FFF0 : ceniza ? 0x80FFF4E0 : soga ? 0x80FFE8D8 : agua ? 0x80E0FAFF : 0x80FFE4DA);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        g.blit(BARRA, 0, 0, 0, 0, 256, 40, 256, 128);
        // las gotas que caen
        for (Gota h : GOTAS) {
            float a = Mth.clamp(1f - (h.edad + pt - 20) / 12f, 0f, 1f);
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1, 1, 1, a);
            g.pose().pushPose();
            g.pose().translate(h.x + h.vx * pt, h.y + h.vy * pt, 0);
            g.blit(BARRA, -4, -4, h.tipo * 8, 104, 8, 8, 256, 128);
            g.pose().popPose();
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1, 1, 1, 1);
        }
        g.pose().popPose();
        e.setIncrement(Math.round(44 * sc) + 4);
    }
}
