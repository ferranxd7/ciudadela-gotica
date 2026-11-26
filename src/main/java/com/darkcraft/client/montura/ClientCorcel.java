package com.darkcraft.client.montura;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.client.ClientCine;
import com.darkcraft.client.ui.Teclas;
import com.darkcraft.montura.Corcel;
import com.darkcraft.net.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * El corcel en el cliente (parte 7b): la tecla H lo llama (o, montado, lo despide); montado, la tecla de saltar salta
 * (y otra vez en el aire: el corcel lo hace en su tick, que lo mueve este cliente) y la de correr va al galope.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class ClientCorcel {
    private ClientCorcel() {}

    private static boolean saltoAntes;
    /** Para la prueba: saltos pedidos al corcel desde este cliente. */
    public static int saltosPedidos;

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        boolean llamar = false;
        while (Teclas.CORCEL.consumeClick()) llamar = true;
        if (p == null || mc.level == null) {
            Corcel.galopeCliente = false;
            return;
        }
        if (llamar && mc.screen == null && !ClientCine.active() && p.isAlive()) Net.CH.sendToServer(new Net.CorcelLlamar());
        boolean salto = mc.options.keyJump.isDown() && mc.screen == null;
        if (p.getVehicle() instanceof Corcel c && c.getControllingPassenger() == p) {
            if (salto && !saltoAntes) pedirSalto(c);
            Corcel.galopeCliente = mc.options.keySprint.isDown();
        } else Corcel.galopeCliente = false;
        saltoAntes = salto;
    }

    /** Pide un salto al corcel que se lleva (lo hace en su tick: el primero en el suelo, el segundo en el aire). */
    public static void pedirSalto(Corcel c) {
        c.saltoPedido = true;
        saltosPedidos++;
    }
}
