package com.darkcraft.client.ui;

import com.darkcraft.rol.Equipo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

/**
 * La cruz del equipo, abajo a la izquierda (como en Elden Ring), en vez de la barra de Minecraft: arriba el hechizo
 * (todavia nada), a la derecha el arma de la mano derecha, a la izquierda lo de la mano izquierda y abajo el objeto
 * rapido (los frascos y lo que se lleve en los huecos rapidos). Los puntitos dicen cuantos hay para turnarse y cual es.
 * Al cambiar algo, sale su nombre un momento.
 */
public final class Cruz {
    private Cruz() {}

    public static final int LADO = 22, ANCHO = 66, ALTO = 66;

    static void dibujar(Minecraft mc, GuiGraphics g, float t, int h) {
        Font f = mc.font;
        Inventory inv = mc.player.getInventory();
        int x0 = 4, y0 = h - 4 - ALTO;
        float hace = t - ClientEquipo.cambioTick;
        boolean reciente = hace >= 0 && hace < 7;
        Pix.blanco();
        Panel.t(g, MenuTex.CRUZ_CENTRO, x0 + LADO, y0 + LADO);

        // arriba: el hechizo elegido de los memorizados (con puntos si hay mas de uno)
        com.darkcraft.hechizos.Hechizo hz = ClientRol.hechizo();
        ItemStack hst = hz == null ? ItemStack.EMPTY : new ItemStack(com.darkcraft.DarkCraftMod.HECHIZOS.get(hz).get());
        float hHace = t - ClientRol.hechizoCambioTick;
        ranura(g, f, x0 + LADO, y0, hst, MenuTex.SIL_HECHIZO, hHace >= 0 && hHace < 7, hz == null ? 0.55f : 1f);
        int nm = ClientRol.memoria.size();
        if (nm > 1) for (int i = 0; i < Math.min(5, nm); i++) punto(g, x0 + LADO + 11 - Math.min(5, nm) * 3 + i * 6, y0 - 4, i == Math.floorMod(ClientRol.hechizoElegido, nm), true);

        // derecha: lo de la mano derecha
        int mano = Mth.clamp(inv.selected, 0, 8);
        ItemStack der = inv.getItem(mano);
        ranura(g, f, x0 + 2 * LADO, y0 + LADO, der, MenuTex.SIL_ESPADA, reciente && ClientEquipo.cambio == 0, 1f);
        int hay = 0;
        for (int i : Equipo.DERECHA) if (!inv.getItem(i).isEmpty()) hay++;
        if (hay > 1) for (int i = 0; i < 3; i++) punto(g, x0 + 2 * LADO + 3 + i * 6, y0 + 2 * LADO + 1, i == mano, !inv.getItem(i).isEmpty());

        // izquierda: lo de la otra mano
        ItemStack izq = inv.getItem(Equipo.OTRA_MANO);
        ranura(g, f, x0, y0 + LADO, izq, MenuTex.SIL_ESCUDO, reciente && ClientEquipo.cambio == 1, 1f);
        hay = 0;
        for (int i : Equipo.IZQUIERDA) if (!inv.getItem(i).isEmpty()) hay++;
        if (hay > 1) for (int i = 0; i < 3; i++) punto(g, x0 + 3 + i * 6, y0 + 2 * LADO + 1, i == 0, !inv.getItem(Equipo.IZQUIERDA[i]).isEmpty());

        // abajo: el objeto rapido
        int q = ClientEquipo.rapido();
        int ax = x0 + LADO, ay = y0 + 2 * LADO;
        boolean luz = reciente && ClientEquipo.cambio == 2;
        float sacude = t - Bebida.sacudeTick;
        if (sacude >= 0 && sacude < 9 && (q == ClientEquipo.FRASCO_VIDA || q == ClientEquipo.FRASCO_MANA)
                && Bebida.sacudeTipo == (q == ClientEquipo.FRASCO_MANA ? 1 : 0))
            ax += Math.round(Mth.sin(sacude * 2.4f) * 2.2f * (1f - sacude / 9f));
        String tecla = Teclas.OBJETO_USAR.getTranslatedKeyMessage().getString();
        if (q == ClientEquipo.FRASCO_VIDA || q == ClientEquipo.FRASCO_MANA) {
            boolean mana = q == ClientEquipo.FRASCO_MANA;
            int quedan = mana ? ClientRol.quedanMana : ClientRol.quedanVida;
            ranura(g, f, ax, ay, ItemStack.EMPTY, null, luz, 1f);
            Pix.blanco();
            Panel.t(g, quedan > 0 ? (mana ? MenuTex.FRASCO_MANA : MenuTex.FRASCO_VIDA) : (mana ? MenuTex.FRASCO_MANA_VACIO : MenuTex.FRASCO_VIDA_VACIO), ax + 3, ay + 2);
            float trago = t - Bebida.tragoTick;
            if (Bebida.tragoTipo == (mana ? 1 : 0) && trago >= 0 && trago < 18) {
                Pix.suma();
                if (mana) Pix.color(0.35f, 0.6f, 1f, 0.75f * (1f - trago / 18f));
                else Pix.color(1f, 0.3f, 0.22f, 0.75f * (1f - trago / 18f));
                g.blit(Hud.BRILLO, ax - 5, ay - 5, 32, 32, 0, 0, 64, 64, 64, 64);
                Pix.blanco();
                Pix.mezcla();
            }
            String n = Integer.toString(quedan);
            g.pose().pushPose();
            g.pose().translate(0, 0, 200);
            g.drawString(f, n, ax + 20 - f.width(n), ay + 12, quedan > 0 ? 0xFFF6E3A0 : 0xFFB03038, true);
            g.pose().popPose();
            tecla = (mana ? Teclas.FRASCO_MANA : Teclas.FRASCO_VIDA).getTranslatedKeyMessage().getString();
        } else {
            ranura(g, f, ax, ay, q >= 0 ? inv.getItem(q) : ItemStack.EMPTY, MenuTex.SIL_OBJETO, luz, 1f);
        }
        List<Integer> todos = ClientEquipo.rapidos();
        if (todos.size() > 1) for (int i = 0; i < todos.size(); i++) punto(g, x0 + 2 * LADO + 2 + i * 6, ay + 14, todos.get(i) == q, true);
        // la tecla del objeto rapido, pequena, a su izquierda
        tecla = tecla.toUpperCase(Locale.ROOT);
        if (q != ClientEquipo.NADA && f.width(tecla) <= 18) g.drawString(f, tecla, x0 + LADO - 3 - f.width(tecla), ay + 12, 0xC0B8A888, true);

        // el nombre de lo que se acaba de cambiar
        if (hace >= 0 && hace < 44 && ClientEquipo.cambio >= 0) {
            float a = hace > 32 ? (44 - hace) / 12f : Math.min(1f, hace / 3f);
            int alfa = Math.round(255 * a);
            Component nombre = switch (ClientEquipo.cambio) {
                case 0 -> der.isEmpty() ? null : der.getHoverName();
                case 1 -> izq.isEmpty() ? null : izq.getHoverName();
                default -> q == ClientEquipo.FRASCO_VIDA ? Component.translatable("item.darkcraft.frasco_vida")
                        : q == ClientEquipo.FRASCO_MANA ? Component.translatable("item.darkcraft.frasco_mana")
                        : q >= 0 && !inv.getItem(q).isEmpty() ? inv.getItem(q).getHoverName() : null;
            };
            if (nombre != null && alfa >= 8) g.drawString(f, nombre, x0 + ANCHO + 6, y0 + LADO + 7, (alfa << 24) | 0xEADBC4, true);
        }
        Pix.blanco();
        Pix.mezcla();
    }

    /** Una ranura con su objeto (con su numero y su barra de desgaste) o, vacia, con su silueta. */
    static void ranura(GuiGraphics g, Font f, int x, int y, ItemStack st, MenuTex.T silueta, boolean luz, float alfa) {
        Pix.color(1f, 1f, 1f, alfa);
        Panel.t(g, luz ? MenuTex.RANURA_LUZ : MenuTex.RANURA, x, y);
        if (st.isEmpty()) {
            if (silueta != null) Panel.t(g, silueta, x + 3, y + 3);
            Pix.blanco();
            return;
        }
        Pix.blanco();
        g.renderItem(st, x + 3, y + 3);
        g.renderItemDecorations(f, st, x + 3, y + 3);
        Pix.mezcla();
    }

    private static void punto(GuiGraphics g, int x, int y, boolean elegido, boolean lleno) {
        Pix.color(1f, 1f, 1f, lleno ? 1f : 0.4f);
        Panel.t(g, elegido ? MenuTex.PUNTO_LUZ : MenuTex.PUNTO, x, y);
        Pix.blanco();
    }
}
