package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** La pantalla de libro y la del atril de Minecraft no se abren nunca: en su lugar sale el codice ({@link CodiceScreen}). */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class ClientCodice {
    private ClientCodice() {}

    @SubscribeEvent
    public static void alAbrir(ScreenEvent.Opening e) {
        Screen nueva = CodiceScreen.alAbrir(e.getNewScreen());
        if (nueva != e.getNewScreen()) e.setNewScreen(nueva);
    }
}
