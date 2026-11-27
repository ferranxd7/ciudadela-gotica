package com.darkcraft.rey;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Lo que encuentra en el cofre del capitulo 3 quien ayudo a matar al Rey (cada uno su copia). */
public final class Recompensa {
    private Recompensa() {}

    public static void llenar(ServerPlayer sp, ServerLevel lvl, BlockPos pos, NonNullList<ItemStack> items) {
        Sala s = Sala.near(lvl, pos.getX(), pos.getZ());
        if (s == null || !ReyEstado.get(lvl.getServer()).gano(s.key(), sp.getUUID())) return;
        items.set(3, new ItemStack(com.darkcraft.DarkCraftMod.ESPADA_DIESTRA.get()));
        items.set(5, new ItemStack(com.darkcraft.DarkCraftMod.ESPADA_SINIESTRA.get()));
        items.set(4, new ItemStack(com.darkcraft.DarkCraftMod.LLAVE_ARBOL.get()));
        items.set(10, new ItemStack(Items.NETHERITE_INGOT, 4));
        items.set(16, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 2));
        items.set(19, new ItemStack(Items.EXPERIENCE_BOTTLE, 32));
        items.set(25, new ItemStack(Items.TOTEM_OF_UNDYING, 1));
        // el Rey ardia: su talisman guarda del fuego (y vencerle abre el segundo hueco de talisman)
        items.set(13, new ItemStack(com.darkcraft.DarkCraftMod.TALISMANES.get(com.darkcraft.rol.Talisman.Clase.BRASA).get()));
        items.set(12, new ItemStack(com.darkcraft.DarkCraftMod.FRUTOS.get(com.darkcraft.rol.Fruto.Clase.BRASA).get(), 3));
        // piedras de forja para Dorvan
        items.set(14, new ItemStack(com.darkcraft.DarkCraftMod.PIEDRA_FORJA_2.get(), 3));
        // y la ceniza del fuego del Rey
        items.set(22, new ItemStack(com.darkcraft.DarkCraftMod.CENIZAS.get(com.darkcraft.combate.Habilidad.LLAMAS).get()));
        for (int slot : new int[]{20, 21, 23})
            items.set(slot, EnchantmentHelper.enchantItem(lvl.random, new ItemStack(Items.BOOK), 30, true));
        // y su armadura, la del Rey Corvin (parte 7al): yelmo, peto, guantes y grebas en la fila del medio
        int[] huecos = {9, 11, 15, 17};
        for (int i = 0; i < 4; i++) items.set(huecos[i], com.darkcraft.DarkCraftMod.armadura(com.darkcraft.armadura.Conjunto.REY, i));
    }
}
