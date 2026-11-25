package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ToastAddEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;

/**
 * Los logros con el aviso del mod en vez del de Minecraft (parte 2f): una losa pequena con el icono, "Logro" (o
 * "Hazana" en los de desafio, con su fanfarria) en gotica y el nombre. En el juego sale arriba en el centro, debajo de
 * la brujula; con una pantalla abierta, arriba a la derecha. De uno en uno; mientras hay cinematica, esperan.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class AvisoLogro {
    private AvisoLogro() {}

    private static final long DURA = 5600, ENTRA = 380, SALE = 520;
    private static final int ALTO = 38;

    private record Aviso(ItemStack icono, Component titulo, boolean hazana) {}

    private static final ArrayDeque<Aviso> COLA = new ArrayDeque<>();
    private static Aviso actual;
    private static long inicio = -1, ultimo;
    /** Para la prueba de la CI: cuantos avisos de Minecraft se han cambiado por el del mod. */
    public static int cambiados;

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onAviso(ToastAddEvent e) {
        if (e.isCanceled() || !(e.getToast() instanceof AdvancementToast t)) return;
        Advancement a = avance(t);
        DisplayInfo d = a == null ? null : a.getDisplay();
        if (d == null) return;
        e.setCanceled(true);
        cambiados++;
        poner(d.getIcon(), d.getTitle(), d.getFrame() == FrameType.CHALLENGE);
    }

    public static void poner(ItemStack icono, Component titulo, boolean hazana) {
        if (COLA.size() < 12) COLA.add(new Aviso(icono.copy(), titulo, hazana));
    }

    public static void olvidar() {
        COLA.clear();
        actual = null;
        inicio = -1;
    }

    /** El logro del aviso de Minecraft (su unico campo de ese tipo: se busca por el tipo, los nombres cambian). */
    private static Advancement avance(AdvancementToast t) {
        try {
            for (Field f : AdvancementToast.class.getDeclaredFields())
                if (f.getType() == Advancement.class && !Modifier.isStatic(f.getModifiers())) {
                    f.setAccessible(true);
                    return (Advancement) f.get(t);
                }
        } catch (ReflectiveOperationException | RuntimeException ignorada) {
            // sin el logro se deja el aviso de Minecraft
        }
        return null;
    }

    // ------------------------------------------------------------------ dibujo

    @SubscribeEvent
    public static void onHud(RenderGuiEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) dibujar(mc, e.getGuiGraphics(), false);
    }

    @SubscribeEvent
    public static void onPantalla(ScreenEvent.Render.Post e) {
        dibujar(Minecraft.getInstance(), e.getGuiGraphics(), true);
    }

    /** Al salir del mundo se olvidan los que quedaran por salir (no tienen que verse en el titulo). */
    @SubscribeEvent
    public static void onSalir(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut e) {
        olvidar();
    }

    private static void dibujar(Minecraft mc, GuiGraphics g, boolean conPantalla) {
        if (mc.level == null) return;
        long ahora = Util.getMillis();
        long paso = Math.max(0, Math.min(250, ahora - ultimo));
        ultimo = ahora;
        if (actual == null) {
            actual = COLA.poll();
            inicio = -1;
            if (actual == null) return;
        }
        // en las cinematicas (y con el juego sin interfaz, F1) se espera: el reloj se para
        if (com.darkcraft.client.ClientCine.active() || (!conPantalla && mc.options.hideGui)) {
            if (inicio >= 0) inicio += paso;
            return;
        }
        if (inicio < 0) {
            inicio = ahora;
            sonar(mc, actual);
        }
        long edad = ahora - inicio;
        if (edad > DURA) {
            actual = null;
            return;
        }
        float entra = Mth.clamp(edad / (float) ENTRA, 0f, 1f), sale = Mth.clamp((DURA - edad) / (float) SALE, 0f, 1f);
        float alfa = Math.min(entra, sale);
        float suave = 1f - (1f - entra) * (1f - entra);
        Font f = mc.font;
        Component cab = Component.translatable(actual.hazana() ? "darkcraft.logro.hazana" : "darkcraft.logro.titulo");
        String titulo = actual.titulo().getString();
        int maxTexto = Math.min(260, g.guiWidth() - 70);
        while (f.width(titulo) > maxTexto && titulo.length() > 4) titulo = titulo.substring(0, titulo.length() - 2);
        if (!titulo.equals(actual.titulo().getString())) titulo += "…";
        int w = Math.max(Letra.anchoM(f, cab), f.width(titulo)) + 54;
        int x, y;
        if (conPantalla) {
            x = g.guiWidth() - w - 4 + Math.round((1f - suave) * (w + 8));
            y = 4;
        } else {
            x = (g.guiWidth() - w) / 2;
            y = 28 - Math.round((1f - suave) * 14);
        }
        g.pose().pushPose();
        g.pose().translate(0, 0, 400);
        Pix.color(1f, 1f, 1f, alfa);
        Panel.losa(g, x, y, w, ALTO);
        Pix.color(1f, 1f, 1f, alfa);
        Panel.hueco(g, x + 8, y + 8, 22, 22);
        Pix.blanco();
        if (alfa > 0.6f) g.renderItem(actual.icono(), x + 11, y + 11);
        int al = Math.max(8, Math.round(255 * alfa));
        int oro = actual.hazana() ? 0xF0A23C : 0xE9C766;
        Letra.mediana(g, f, cab, x + 38, y + 5, (al << 24) | oro, (al << 24) | 0x050408);
        g.drawString(f, titulo, x + 38, y + 23, (al << 24) | 0xEADBC4, true);
        g.pose().popPose();
        Pix.blanco();
        Pix.mezcla();
    }

    private static void sonar(Minecraft mc, Aviso a) {
        if (a.hazana()) mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f));
        else mc.getSoundManager().play(SimpleSoundInstance.forUI(DarkCraftMod.SFX_LOGRO.get(), 1f, 0.8f));
    }

    /** Para la prueba de la CI. */
    public static String resumen() {
        return "logros: cambiados " + cambiados + ", en cola " + COLA.size() + (actual == null ? "" : ", viendose '"
                + actual.titulo().getString() + "'" + (actual.hazana() ? " (hazana)" : ""));
    }
}
