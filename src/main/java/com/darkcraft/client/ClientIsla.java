package com.darkcraft.client;

import com.darkcraft.DarkCraftMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * La tormenta de la isla en el cliente: los rayos se siguen viendo, pero sin el destello que ilumina toda la
 * pantalla, y el trueno suena lejano (mas bajo y mas grave) en vez de retumbar encima.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class ClientIsla {
    private ClientIsla() {}

    private static boolean dentro;

    public static void dentro(boolean v) {
        dentro = v;
    }

    public static boolean dentro() {
        return dentro;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !dentro) return;
        Minecraft mc = Minecraft.getInstance();
        // el destello de los rayos (los de las cinematicas de los capitulos se dejan: son un momento concreto)
        if (mc.level != null && mc.level.getSkyFlashTime() > 0 && !ClientCine.active()) mc.level.setSkyFlashTime(0);
    }

    @SubscribeEvent
    public static void onSound(PlaySoundEvent e) {
        if (!dentro || e.getSound() == null || ClientCine.active()) return;
        var loc = e.getSound().getLocation();
        if (loc.equals(SoundEvents.LIGHTNING_BOLT_THUNDER.getLocation())) {
            float pitch = 0.6f + (float) Math.random() * 0.2f;
            e.setSound(new SimpleSoundInstance(loc, SoundSource.WEATHER, 0.35f, pitch, SoundInstance.createUnseededRandom(), false, 0,
                    SoundInstance.Attenuation.NONE, 0, 0, 0, true));
        } else if (loc.equals(SoundEvents.LIGHTNING_BOLT_IMPACT.getLocation())) {
            e.setSound(null);
        }
    }

    @SubscribeEvent
    public static void onLeave(ClientPlayerNetworkEvent.LoggingOut e) {
        dentro = false;
    }
}
