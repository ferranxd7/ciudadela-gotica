package com.darkcraft.forja;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Una piedra de forja (grado I, II o III): Dorvan las pide para mejorar las armas ({@link Forja}). */
public class PiedraForja extends Item {
    public final int grado;

    public PiedraForja(int grado, Properties p) {
        super(p);
        this.grado = grado;
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("darkcraft.forja.piedra.desc", (grado - 1) * 3 + 1, grado * 3).withStyle(ChatFormatting.GRAY));
    }
}
