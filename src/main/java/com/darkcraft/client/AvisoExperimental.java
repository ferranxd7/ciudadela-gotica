package com.darkcraft.client;

import com.darkcraft.DarkCraftMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

/**
 * La dimension de los volcanes (la fase 3 del Rey) viene en el propio mod como datapack, y Minecraft marca
 * por eso cualquier mundo como "con ajustes experimentales" y pregunta antes de abrirlo o crearlo. No hay nada
 * experimental de verdad, asi que el aviso se acepta solo (pulsando "Continuar" en el siguiente tick).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class AvisoExperimental {
    private AvisoExperimental() {}

    private static final Set<String> TITULOS = Set.of(
            "selectWorld.backupQuestion.experimental",
            "selectWorld.warning.experimental.title");

    private static Screen pantalla;
    private static Button si;

    @SubscribeEvent
    public static void onInit(ScreenEvent.Init.Post e) {
        if (!(e.getScreen() instanceof ConfirmScreen cs)) return;
        if (!(cs.getTitle().getContents() instanceof TranslatableContents tc) || !TITULOS.contains(tc.getKey())) return;
        Button izq = null;
        for (GuiEventListener l : e.getListenersList())
            if (l instanceof Button b && (izq == null || b.getX() < izq.getX())) izq = b;
        if (izq == null) return;
        pantalla = cs;
        si = izq;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END || si == null) return;
        Screen s = pantalla;
        Button b = si;
        pantalla = null;
        si = null;
        if (Minecraft.getInstance().screen != s) return;
        DarkCraftMod.LOGGER.info("[DarkCraft] Aviso de ajustes experimentales (por la dimension de los volcanes): se continua sin preguntar");
        b.onPress();
    }
}
