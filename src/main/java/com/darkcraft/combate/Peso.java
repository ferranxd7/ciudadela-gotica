package com.darkcraft.combate;

import com.darkcraft.rol.Atributo;
import com.darkcraft.rol.Equipo;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TieredItem;

/**
 * El peso del equipo (como en Elden Ring): la armadura puesta y las armas de las dos manos (las tres de cada una)
 * pesan; el Aguante da cuanto se puede llevar. Segun lo cargado que se vaya se rueda de una manera: ligero (rapido y
 * lejos), medio, pesado (lento y corto) o, con mas de lo que se puede llevar, ni se rueda y se anda despacio.
 */
public final class Peso {
    private Peso() {}

    public static final int LIGERO = 0, MEDIO = 1, PESADO = 2, SOBRECARGADO = 3;

    /** Lo que se puede llevar: 45 con Aguante 10, y 1,8 mas por cada punto (un 15 % mas con el Talisman del Porteador). */
    public static float maximo(int[] atributos, boolean porteador) {
        return (45f + (atributos[Atributo.AGUANTE.ordinal()] - 10) * 1.8f) * (porteador ? 1.15f : 1f);
    }

    /** Lo que puede llevar un jugador en el servidor (con sus talismanes). */
    public static float maximo(net.minecraft.world.entity.player.Player p) {
        return maximo(com.darkcraft.rol.Ficha.atributos(p), com.darkcraft.rol.Talismanes.lleva(p, com.darkcraft.rol.Talisman.Clase.PORTEADOR));
    }

    /** Lo que pesa lo que lleva puesto el jugador. */
    public static float de(Inventory inv) {
        float t = 0f;
        for (int h : Equipo.ARMADURA) t += item(inv.getItem(h));
        for (int h : Equipo.DERECHA) t += item(inv.getItem(h));
        for (int h : Equipo.IZQUIERDA) t += item(inv.getItem(h));
        return t;
    }

    /** La clase de carga segun lo que pesa y lo que se puede llevar. */
    public static int clase(float peso, float maximo) {
        float f = peso / Math.max(1f, maximo);
        return f <= 0.3f ? LIGERO : f <= 0.7f ? MEDIO : f <= 1.0f ? PESADO : SOBRECARGADO;
    }

    /** Lo que pesa una cosa (las armas por su tipo; la armadura por su material y su pieza). */
    public static float item(ItemStack st) {
        if (st.isEmpty()) return 0f;
        if (st.getItem() instanceof ArmorItem a) {
            float base;
            if (a.getMaterial() == ArmorMaterials.LEATHER) base = 1.5f;
            else if (a.getMaterial() == ArmorMaterials.CHAIN) base = 3f;
            else if (a.getMaterial() == ArmorMaterials.IRON) base = 4.5f;
            else if (a.getMaterial() == ArmorMaterials.GOLD) base = 3.5f;
            else if (a.getMaterial() == ArmorMaterials.DIAMOND) base = 6f;
            else if (a.getMaterial() == ArmorMaterials.NETHERITE) base = 7.5f;
            else if (a.getMaterial() == ArmorMaterials.TURTLE) base = 3f;
            else base = Math.max(1f, a.getDefense() * 1.2f);
            EquipmentSlot s = a.getEquipmentSlot();
            float pieza = s == EquipmentSlot.CHEST ? 1.6f : s == EquipmentSlot.LEGS ? 1.2f : s == EquipmentSlot.HEAD ? 0.8f : 0.6f;
            return Math.round(base * pieza * 10f) / 10f;
        }
        if (st.getItem() instanceof ElytraItem) return 2f;
        if (st.getItem() instanceof ShieldItem) return 4f;
        if (st.getItem() instanceof CrossbowItem) return 4.5f;
        if (st.getItem() instanceof BowItem) return 3f;
        Armas.Tipo t = Armas.de(st);
        if (t == Armas.Tipo.ESPADA && com.darkcraft.armas.EspadaRey.es(st)) return 5.5f;
        if (t != null) return t.peso;
        if (st.getItem() instanceof TieredItem) return 2.5f;
        return 0.5f;
    }
}
