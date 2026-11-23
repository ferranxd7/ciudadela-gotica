package com.darkcraft.fortaleza;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * El Amuleto de Hueso: se forja con la llave de hueso, tres lingotes de netherita y dos diamantes (el acertijo del
 * maestre Velan). En el altar de lo mas alto de la Fortaleza de Hueso abre el portal de la Sombra (y se deshace).
 */
public class AmuletoHueso extends Item {
    public AmuletoHueso(Properties p) {
        super(p);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level lvl, Player p, InteractionHand hand) {
        ItemStack st = p.getItemInHand(hand);
        if (p instanceof ServerPlayer sp && !sp.getCooldowns().isOnCooldown(this)) {
            sp.getCooldowns().addCooldown(this, 30);
            FortalezaEventos.usarAmuleto(sp, st);
        }
        return InteractionResultHolder.sidedSuccess(st, lvl.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getPlayer() instanceof ServerPlayer sp && !sp.getCooldowns().isOnCooldown(this)) {
            sp.getCooldowns().addCooldown(this, 30);
            FortalezaEventos.usarAmuleto(sp, ctx.getItemInHand());
        }
        return InteractionResult.sidedSuccess(ctx.getLevel().isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack s) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.darkcraft.amuleto_hueso.lore").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
