package com.darkcraft.mision;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Un objeto clave de una mision (parte 7ah: la bufanda roja de Tam): no hace nada; su descripcion ({@code item.darkcraft.<id>.desc})
 * cuenta lo que es.
 */
public class ObjetoMision extends Item {
    private final String clave;

    public ObjetoMision(String id, Properties p) {
        super(p);
        this.clave = "item.darkcraft." + id + ".desc";
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable(clave).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
