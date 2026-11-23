package com.darkcraft.armas;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * La llave de hueso que guardaba Ser Gavran: "te mostrara el camino hacia fortalezas que flotan mas alla de las
 * nubes". Ese camino llega en una parte futura del mod; de momento tiembla, brilla y lo dice.
 */
public class LlaveHueso extends Item {
    public LlaveHueso(Properties p) {
        super(p);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level lvl, Player p, InteractionHand hand) {
        ItemStack st = p.getItemInHand(hand);
        if (lvl instanceof ServerLevel sl) {
            sl.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0f, 0.6f);
            sl.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SKELETON_AMBIENT, SoundSource.PLAYERS, 0.4f, 0.5f);
            sl.sendParticles(com.darkcraft.gavran.Raices.VERDE, p.getX(), p.getEyeY() - 0.3, p.getZ(), 12, 0.3, 0.3, 0.3, 0);
            p.displayClientMessage(Component.translatable("item.darkcraft.llave_hueso.aun_no").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            p.getCooldowns().addCooldown(this, 40);
        }
        return InteractionResultHolder.sidedSuccess(st, lvl.isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack s) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.darkcraft.llave_hueso.lore").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
