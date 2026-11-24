package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Las teclas del rol (se pueden cambiar en los controles, en el apartado Dark Craft): los dos frascos. */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class Teclas {
    private Teclas() {}

    public static final KeyMapping FRASCO_VIDA = new KeyMapping("key.darkcraft.frasco_vida", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.darkcraft");
    public static final KeyMapping FRASCO_MANA = new KeyMapping("key.darkcraft.frasco_mana", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.darkcraft");

    @SubscribeEvent
    public static void registrar(RegisterKeyMappingsEvent e) {
        e.register(FRASCO_VIDA);
        e.register(FRASCO_MANA);
    }
}
