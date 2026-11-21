package com.ciudadela.armas;

import com.ciudadela.rey.Sello;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/** La llave que el Rey guardaba: abre el sello de la Sala del Arbol (y se gasta). */
public class LlaveArbol extends Item {
    public LlaveArbol(Properties p) {
        super(p);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().isClientSide) return InteractionResult.SUCCESS;
        if (ctx.getPlayer() instanceof ServerPlayer sp && Sello.usarLlave(sp, ctx.getClickedPos(), ctx.getItemInHand()))
            return InteractionResult.CONSUME;
        return InteractionResult.PASS;
    }

    @Override
    public boolean isFoil(ItemStack s) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.ciudadela.llave_arbol.lore").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
    }
}
