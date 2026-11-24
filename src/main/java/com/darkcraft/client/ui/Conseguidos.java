package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.net.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ToastAddEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * Lo que se va recogiendo: una banda pequena abajo en el centro con el dibujo del objeto, su nombre y cuantos (como el
 * "has conseguido" de Elden Ring). Hasta dos a la vez; las demas esperan su turno. Y de paso se quitan los avisos de
 * Minecraft que ya no pintan nada (recetas nuevas y tutorial).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class Conseguidos {
    private Conseguidos() {}

    private static final int DURA = 64, A_LA_VEZ = 2;

    private static final class Aviso {
        ItemStack st;
        int n, inicio = -1;
    }

    private static final List<Aviso> COLA = new ArrayList<>();

    public static void recibir(Net.Conseguido m) {
        if (m.objeto().isEmpty()) return;
        // lo mismo que algo que aun se esta viendo o esperando: se suma
        for (Aviso a : COLA)
            if (ItemStack.isSameItemSameTags(a.st, m.objeto()) && (a.inicio < 0 || ClientRol.ticks - a.inicio < DURA - 20)) {
                a.n += m.objeto().getCount();
                // si ya se estaba viendo, que siga un rato mas (sin volver a entrar)
                if (a.inicio >= 0 && ClientRol.ticks - a.inicio > 8) a.inicio = ClientRol.ticks - 8;
                return;
            }
        Aviso a = new Aviso();
        a.st = m.objeto().copy();
        a.n = m.objeto().getCount();
        COLA.add(a);
        while (COLA.size() > 8) COLA.remove(COLA.size() - 1);
    }

    static void olvidar() {
        COLA.clear();
    }

    /** Lo llama {@link ClientRol} en cada tick del cliente. */
    static void tick() {
        COLA.removeIf(a -> a.inicio >= 0 && (ClientRol.ticks - a.inicio > DURA || ClientRol.ticks < a.inicio));
        for (int i = 0; i < Math.min(A_LA_VEZ, COLA.size()); i++) if (COLA.get(i).inicio < 0) COLA.get(i).inicio = ClientRol.ticks;
    }

    /** Lo llama {@link Hud}. */
    static void dibujar(Minecraft mc, GuiGraphics g, float t, int w, int h) {
        Font f = mc.font;
        for (int i = 0; i < Math.min(A_LA_VEZ, COLA.size()); i++) {
            Aviso a = COLA.get(i);
            if (a.inicio < 0) continue;
            float edad = t - a.inicio;
            float alfa = Mth.clamp(Math.min(edad / 5f, (DURA - edad) / 12f), 0f, 1f);
            int al = Math.round(255 * alfa);
            if (al < 8) continue;
            Component nombre = a.st.getHoverName();
            String cuantos = a.n > 1 ? "×" + a.n : "";
            int ancho = 24 + 20 + f.width(nombre) + (cuantos.isEmpty() ? 0 : 6 + f.width(cuantos)) + 24;
            int x = (w - ancho) / 2, y = h - 30 - i * 23;
            Pix.color(1f, 1f, 1f, alfa);
            Panel.t(g, MenuTex.LOGRO_IZQ, x, y);
            Panel.tira(g, MenuTex.LOGRO_MED, x + MenuTex.LOGRO_IZQ.w(), y, ancho - MenuTex.LOGRO_IZQ.w() - MenuTex.LOGRO_DER.w());
            Panel.t(g, MenuTex.LOGRO_DER, x + ancho - MenuTex.LOGRO_DER.w(), y);
            Pix.blanco();
            if (alfa > 0.6f) g.renderItem(a.st, x + 22, y + 2);
            g.drawString(f, nombre, x + 44, y + 6, (al << 24) | 0xEADBC4, true);
            if (!cuantos.isEmpty()) g.drawString(f, cuantos, x + 44 + f.width(nombre) + 6, y + 6, (al << 24) | 0xE2BF62, true);
        }
        Pix.blanco();
        Pix.mezcla();
    }

    /** Fuera los avisos de recetas nuevas y los del tutorial de Minecraft. */
    @SubscribeEvent
    public static void onAviso(ToastAddEvent e) {
        if (e.getToast() instanceof RecipeToast || e.getToast() instanceof TutorialToast) e.setCanceled(true);
    }
}
