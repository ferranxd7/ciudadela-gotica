package com.darkcraft.armas;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Una flecha de fuego, de sangre o de escarcha (parte 7aj): la disparan el arco y la ballesta del juego (van en la etiqueta
 * {@code #minecraft:arrows}). Como en Minecraft, el arco tira primero las que se lleven en la mano izquierda: para elegir cuales,
 * se ponen ahi (la mano izquierda tiene tres huecos que se turnan). La flecha que sale es una {@link FlechaEspecial}; con el
 * encantamiento de infinidad no se ahorran (solo las de siempre).
 */
public class FlechaItem extends ArrowItem {
    public final FlechaEspecial.Clase clase;

    public FlechaItem(FlechaEspecial.Clase clase, Properties p) {
        super(p);
        this.clase = clase;
    }

    @Override
    public AbstractArrow createArrow(Level l, ItemStack st, LivingEntity tirador) {
        return new FlechaEspecial(l, tirador, clase);
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.darkcraft.flecha_" + clase.id + ".desc").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tip.add(Component.translatable("darkcraft.flecha.mano").withStyle(ChatFormatting.DARK_GRAY));
    }
}
