package com.darkcraft.montura;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * El silbato del corcel (parte 7b): hueso tallado que Maelis guardaba de sus hermanas. Usarlo (o la tecla H con el
 * silbato encima) llama al corcel en Morvain ({@link Montura#llamar}); montado, lo despide.
 */
public class SilbatoCorcel extends Item {
    public SilbatoCorcel(Properties p) {
        super(p);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level l, Player p, InteractionHand mano) {
        ItemStack st = p.getItemInHand(mano);
        if (!l.isClientSide && p instanceof ServerPlayer sp) Montura.llamar(sp);
        p.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.sidedSuccess(st, l.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack st, @Nullable Level l, List<Component> lineas, TooltipFlag f) {
        lineas.add(Component.translatable("item.darkcraft.silbato_corcel.desc").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
