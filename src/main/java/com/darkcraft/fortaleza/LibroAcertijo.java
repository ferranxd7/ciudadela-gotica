package com.darkcraft.fortaleza;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.List;

/**
 * El libro del acertijo del maestre Velan (en el cofre de la planta baja de la torre del homenaje): el acertijo en verso
 * del Amuleto de Hueso y un sello que, al pulsarlo, dibuja la forja (la receta).
 */
public class LibroAcertijo extends Item {
    public LibroAcertijo(Properties p) {
        super(p);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level lvl, Player p, InteractionHand hand) {
        ItemStack st = p.getItemInHand(hand);
        if (p instanceof ServerPlayer sp) FortalezaEventos.leerAcertijo(sp);
        if (lvl.isClientSide) DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.darkcraft.client.fortaleza.AcertijoScreen.abrir());
        return InteractionResultHolder.sidedSuccess(st, lvl.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("darkcraft.acertijo.autor").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
