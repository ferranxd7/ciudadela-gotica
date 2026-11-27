package com.darkcraft.mision;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * La Lanza de la Puerta Oeste (parte 7ah): la de Tam, que estuvo atravesada en la rueda del torno del rastrillo; se saca tras decirle
 * la verdad a Hedda. Para el combate es una lanza ({@code Armas.de}: sus golpes, su estocada) que escala mejor que las demas con la
 * Fuerza y la Destreza ({@code Escalado.de}: C y B).
 */
public class LanzaTam extends SwordItem {
    public LanzaTam(Properties p) {
        super(Tiers.IRON, 4, -2.7f, p);
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.darkcraft.lanza_tam.desc").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
