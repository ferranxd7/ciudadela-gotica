package com.ciudadela.rey;

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
        items.set(10, new ItemStack(Items.NETHERITE_INGOT, 4));
        items.set(16, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 2));
        items.set(19, new ItemStack(Items.EXPERIENCE_BOTTLE, 32));
        items.set(25, new ItemStack(Items.TOTEM_OF_UNDYING, 1));
        for (int slot : new int[]{20, 21, 23})
            items.set(slot, EnchantmentHelper.enchantItem(lvl.random, new ItemStack(Items.BOOK), 30, true));
    }
}
