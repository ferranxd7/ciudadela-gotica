package com.darkcraft.mision;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** La campana del peregrino (parte 7ag): objeto clave; la de Oswin, que se cogio del Arbol de la Horca ({@link Oswin}). */
public class CampanaPeregrino extends Item {
    public CampanaPeregrino(Properties p) {
        super(p);
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.darkcraft.campana_peregrino.desc").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
