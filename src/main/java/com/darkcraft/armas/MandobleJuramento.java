package com.darkcraft.armas;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;

/**
 * El Mandoble del Juramento, la espada de Ser Gavran: a dos manos (con algo en la otra mano pega mucho menos y no
 * hace sus combos), lento y muy fuerte. Los golpes llenan la misma energia que las espadas del Rey:
 * - Raices del Juramento (Shift + clic): una linea de raices que sale del suelo y atrapa a lo que pilla.
 * - Juicio de Espinas (Ctrl + clic): salta, clava el mandoble y salen espinas y raices en todas direcciones.
 */
public class MandobleJuramento extends SwordItem {
    public MandobleJuramento(Properties p) {
        // 1 + 10 (nivel) + 9 = 20 de dano, 0,9 golpes por segundo
        super(EspadaRey.TIER, 9, -3.1f, p);
    }

    public static boolean es(ItemStack s) {
        return s.getItem() instanceof MandobleJuramento;
    }

    /** Lo lleva bien: en la mano principal y con la otra mano libre. */
    public static boolean dosManos(Player p) {
        return es(p.getMainHandItem()) && p.getOffhandItem().isEmpty();
    }

    @Override
    public boolean hurtEnemy(ItemStack s, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player p && !p.level().isClientSide && dosManos(p)) Combos.sumar(p, p.fallDistance > 0 && !p.onGround() ? 16 : 11);
        return super.hurtEnemy(s, target, attacker);
    }

    @Override
    public boolean isFoil(ItemStack s) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.darkcraft.mandoble_juramento.lore").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC));
        tip.add(Component.empty());
        tip.add(Component.translatable("item.darkcraft.mandoble_juramento.combo1").withStyle(ChatFormatting.GREEN));
        tip.add(Component.translatable("item.darkcraft.mandoble_juramento.combo2").withStyle(ChatFormatting.DARK_GREEN));
        tip.add(Component.translatable("item.darkcraft.mandoble_juramento.dos").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(com.darkcraft.client.armas.MandobleCliente.INSTANCIA);
    }
}
