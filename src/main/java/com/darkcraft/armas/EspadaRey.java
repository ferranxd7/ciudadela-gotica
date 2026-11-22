package com.darkcraft.armas;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeTier;

import java.util.List;
import java.util.function.Consumer;

/**
 * Las dos espadas del Rey Corvin (una para cada mano). Mas fuertes que la netherite; los golpes llenan
 * la energia que gastan los combos (Shift + clic: Tajo Real; Ctrl + clic: Juicio del Rey). Con el clic
 * derecho se cubre cruzando las espadas.
 */
public class EspadaRey extends SwordItem {
    public static final Tier TIER = new ForgeTier(4, 3200, 9.0f, 10.0f, 18, BlockTags.NEEDS_DIAMOND_TOOL,
            () -> Ingredient.of(Items.NETHERITE_INGOT));

    public final boolean diestra;

    public EspadaRey(boolean diestra, Properties p) {
        // 1 + 10 (nivel) + 3 = 14 de dano, velocidad 1,6
        super(TIER, 3, -2.4f, p);
        this.diestra = diestra;
    }

    public static boolean es(ItemStack s) {
        return s.getItem() instanceof EspadaRey;
    }

    /** 0 normal, 1 fuego, 2 lava (lo pone el combo mientras dura). */
    public static int forma(ItemStack s) {
        return s.hasTag() ? s.getTag().getInt("forma") : 0;
    }

    public static void setForma(ItemStack s, int f) {
        if (f == 0) {
            if (s.hasTag()) s.getTag().remove("forma");
        } else s.getOrCreateTag().putInt("forma", f);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level lvl, Player p, InteractionHand hand) {
        ItemStack s = p.getItemInHand(hand);
        p.startUsingItem(hand);
        return InteractionResultHolder.consume(s);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack s) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack s) {
        return 72000;
    }

    @Override
    public boolean hurtEnemy(ItemStack s, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player p && !p.level().isClientSide) Combos.sumar(p, p.fallDistance > 0 && !p.onGround() ? 12 : 8);
        return super.hurtEnemy(s, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.darkcraft.espada.lore").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        tip.add(Component.empty());
        tip.add(Component.translatable("item.darkcraft.espada.combo1").withStyle(ChatFormatting.GOLD));
        tip.add(Component.translatable("item.darkcraft.espada.combo2").withStyle(ChatFormatting.RED));
        tip.add(Component.translatable("item.darkcraft.espada.bloqueo").withStyle(ChatFormatting.GRAY));
        tip.add(Component.translatable("item.darkcraft.espada.dos").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(com.darkcraft.client.armas.EspadaCliente.INSTANCIA);
    }
}
