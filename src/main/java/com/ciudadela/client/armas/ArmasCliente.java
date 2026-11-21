package com.ciudadela.client.armas;

import com.ciudadela.CiudadelaMod;
import com.ciudadela.armas.Combos;
import com.ciudadela.armas.EspadaRey;
import com.ciudadela.net.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Combos con Shift / Ctrl + clic izquierdo y la barra de energia encima de la barra de objetos. */
@Mod.EventBusSubscriber(modid = CiudadelaMod.MODID, value = Dist.CLIENT)
public final class ArmasCliente {
    private ArmasCliente() {}

    private static int energia, vista, aviso;

    public static void energia(int v) {
        energia = v;
    }

    @SubscribeEvent
    public static void onAttack(InputEvent.InteractionKeyMappingTriggered e) {
        if (!e.isAttack()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !EspadaRey.es(mc.player.getMainHandItem())) return;
        boolean shift = mc.options.keyShift.isDown();
        boolean ctrl = mc.options.keySprint.isDown() || Screen.hasControlDown();
        if (!shift && !ctrl) return;
        int tipo = ctrl ? Combos.JUICIO : Combos.TAJO_REAL;
        int coste = tipo == Combos.JUICIO ? Combos.COSTE_2 : Combos.COSTE_1;
        if (energia < coste && !mc.player.isCreative()) {
            if (aviso <= 0) {
                mc.player.displayClientMessage(Component.translatable("ciudadela.espada.sin_energia", coste), true);
                aviso = 20;
            }
            return;
        }
        Net.CH.sendToServer(new Net.Combo(tipo));
        e.setCanceled(true);
        e.setSwingHand(false);
    }

    /** Barra de energia (solo con una espada del Rey en la mano). */
    public static void render(ForgeGui gui, GuiGraphics g, float pt, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (aviso > 0 && mc.player != null && mc.player.tickCount % 2 == 0) aviso--;
        if (mc.player == null || mc.options.hideGui || mc.player.isSpectator()) return;
        boolean lleva = EspadaRey.es(mc.player.getMainHandItem()) || EspadaRey.es(mc.player.getOffhandItem());
        if (!lleva) return;
        vista += Mth.clamp(energia - vista, -2, 2);
        int bw = 100, x0 = w / 2 - bw / 2, y0 = h - 32 - (mc.player.isCreative() ? 0 : 8) - 9;
        g.fill(x0 - 1, y0 - 1, x0 + bw + 1, y0 + 4, 0xC0100404);
        int fill = Math.round(bw * vista / (float) Combos.MAX);
        int col = vista >= Combos.COSTE_2 ? 0xFFFF7A10 : (vista >= Combos.COSTE_1 ? 0xFFE04018 : 0xFF902010);
        g.fill(x0, y0, x0 + fill, y0 + 3, col);
        g.fill(x0, y0, x0 + fill, y0 + 1, 0x50FFFFFF);
        for (int c : new int[]{Combos.COSTE_1, Combos.COSTE_2}) {
            int xs = x0 + bw * c / Combos.MAX;
            g.fill(xs, y0 - 2, xs + 1, y0 + 5, 0xFFC8A040);
        }
    }
}
