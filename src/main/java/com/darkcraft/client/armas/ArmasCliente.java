package com.darkcraft.client.armas;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.Combos;
import com.darkcraft.armas.EspadaRey;
import com.darkcraft.net.Net;
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

/**
 * Combos con Shift / Ctrl + clic izquierdo (la energia, que es el mana, se ve en la barra azul del HUD). El combate
 * propio pregunta por este mismo evento antes de cada golpe ({@code ClientCombate.permitido}): si aqui sale un combo,
 * no hay golpe normal.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class ArmasCliente {
    private ArmasCliente() {}

    private static int energia, aviso;

    public static void energia(int v) {
        energia = v;
    }

    /** La energia que sabe el cliente (para la prueba de la CI). */
    public static int energiaCliente() {
        return energia;
    }

    @SubscribeEvent
    public static void onAttack(InputEvent.InteractionKeyMappingTriggered e) {
        if (!e.isAttack()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !(EspadaRey.es(mc.player.getMainHandItem()) || com.darkcraft.armas.MandobleJuramento.es(mc.player.getMainHandItem()))) return;
        boolean shift = mc.options.keyShift.isDown();
        // Ctrl corriendo es el ataque corriendo del combate (el Juicio, con Ctrl estando quieto o andando)
        boolean ctrl = (mc.options.keySprint.isDown() || Screen.hasControlDown()) && !mc.player.isSprinting();
        if (!shift && !ctrl) return;
        int tipo = ctrl ? Combos.JUICIO : Combos.TAJO_REAL;
        int coste = tipo == Combos.JUICIO ? Combos.COSTE_2 : Combos.COSTE_1;
        if (energia < coste && !mc.player.isCreative()) {
            if (aviso <= 0) {
                mc.player.displayClientMessage(Component.translatable("darkcraft.espada.sin_energia", coste), true);
                aviso = 40;
            }
            return;
        }
        Net.CH.sendToServer(new Net.Combo(tipo));
        e.setCanceled(true);
        e.setSwingHand(false);
    }

    /** La energia sale en la barra azul del HUD del mod (client/ui/Hud); aqui solo se cuenta cuando se puede volver a avisar. */
    @SubscribeEvent
    public static void onTick(net.minecraftforge.event.TickEvent.ClientTickEvent e) {
        if (e.phase == net.minecraftforge.event.TickEvent.Phase.END && aviso > 0) aviso--;
    }
}
