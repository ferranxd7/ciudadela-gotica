package com.darkcraft.client.morvain;

import com.darkcraft.DarkCraftMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * El viaje entre santuarios en el cliente (parte 7c): a donde se puede viajar (lo manda el servidor con el menu del
 * santuario) y el fundido a negro mientras se le lleva (`Net.Fundido`): baja en 8 ticks, se queda en negro hasta pasado el
 * corte y vuelve poco a poco.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class ClientViaje {
    private ClientViaje() {}

    /** A donde puede viajar desde el santuario en el que descansa (ids) y cual es ese ("" si no es uno de Morvain). */
    public static List<String> destinos = new ArrayList<>();
    public static String aqui = "";
    /** Los santuarios de Morvain que conoce (para la brujula): los manda el servidor al entrar y al encender uno. */
    public static final java.util.Set<String> CONOCIDOS = new java.util.HashSet<>();

    private static int fundido, total;

    public static void santuario(List<String> d, String a) {
        destinos = new ArrayList<>(d);
        aqui = a;
        if (!a.isEmpty()) CONOCIDOS.addAll(d);
    }

    public static void conocidos(List<String> ids) {
        CONOCIDOS.clear();
        CONOCIDOS.addAll(ids);
    }

    @SubscribeEvent
    public static void onSalir(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut e) {
        CONOCIDOS.clear();
        destinos = new ArrayList<>();
        aqui = "";
        fundido = 0;
    }

    public static void fundido(int ticks) {
        total = Math.max(10, ticks);
        fundido = total;
    }

    /** Lo oscuro que esta la pantalla ahora (0..1). */
    public static float negro(float pt) {
        if (fundido <= 0) return 0f;
        float t = total - fundido + pt;
        if (t < 8) return Mth.clamp(t / 8f, 0f, 1f);
        float vuelta = total - 14f;
        return t < 14 ? 1f : Mth.clamp(1f - (t - 14) / Math.max(1f, vuelta), 0f, 1f);
    }

    public static boolean fundiendo() {
        return fundido > 0;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.END && fundido > 0) fundido--;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGui(RenderGuiEvent.Post e) {
        float n = negro(e.getPartialTick());
        if (n <= 0.001f) return;
        Minecraft mc = Minecraft.getInstance();
        e.getGuiGraphics().fill(0, 0, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(),
                (Math.round(255 * n) << 24) | 0x050408);
    }
}
