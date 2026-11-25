package com.darkcraft.client;

import com.darkcraft.DarkCraftMod;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * El mundo de nubes en el cliente (parte 3a): con la camara dentro de una nube (cayendo a traves de ella, o volando en
 * creativo) no se ven las caras de los bloques por dentro sino una niebla blanca muy cerrada, como dentro de una nube de
 * verdad.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class ClientNubes {
    private ClientNubes() {}

    private static boolean dentro(Camera c) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || c == null) return false;
        return mc.level.getBlockState(BlockPos.containing(c.getPosition())).is(DarkCraftMod.NUBE.get());
    }

    @SubscribeEvent
    public static void onNiebla(ViewportEvent.RenderFog e) {
        if (!dentro(e.getCamera())) return;
        e.setNearPlaneDistance(0f);
        e.setFarPlaneDistance(6f);
        e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onColor(ViewportEvent.ComputeFogColor e) {
        if (!dentro(e.getCamera())) return;
        e.setRed(0.84f);
        e.setGreen(0.83f);
        e.setBlue(0.89f);
    }
}
