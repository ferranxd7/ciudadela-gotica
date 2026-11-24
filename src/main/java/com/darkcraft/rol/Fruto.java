package com.darkcraft.rol;

import com.darkcraft.DarkCraftMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

/**
 * Un fruto: se come (rapido, menos de un segundo) y da un rato una resistencia o una habilidad (su efecto,
 * {@link Efectos}). No es comida: no cura ni quita el hambre (que ya no hay). Se llevan en los objetos rapidos (G) o se
 * comen desde la mano.
 */
public final class Fruto extends Item {
    public enum Clase {
        BRASA(Efectos.BRASA, 2400), ESCARCHA(Efectos.ESCARCHA, 2400), TORMENTA(Efectos.TORMENTA, 2400), LUNAR(Efectos.LUNAR, 2400),
        CENIZA(Efectos.CENIZA, 1800), HUESO(Efectos.HUESO, 1200), SOMBRA(Efectos.SOMBRA, 2400), PLUMA(Efectos.PLUMA, 1200);

        public final Supplier<MobEffect> efecto;
        /** Lo que dura (ticks). */
        public final int dura;
        public final String id = name().toLowerCase(java.util.Locale.ROOT);

        Clase(Supplier<MobEffect> efecto, int dura) {
            this.efecto = efecto;
            this.dura = dura;
        }
    }

    public final Clase clase;

    public Fruto(Clase clase, Properties p) {
        super(p);
        this.clase = clase;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack s) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack s) {
        return 18;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level lvl, Player p, InteractionHand mano) {
        return ItemUtils.startUsingInstantly(lvl, p, mano);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack s, Level lvl, LivingEntity e) {
        if (!lvl.isClientSide) {
            e.addEffect(new MobEffectInstance(clase.efecto.get(), clase.dura, 0, false, false, true));
            if (clase == Clase.CENIZA) {
                e.removeEffect(MobEffects.POISON);
                e.removeEffect(MobEffects.WITHER);
            }
            lvl.playSound(null, e.getX(), e.getY() + 1, e.getZ(), DarkCraftMod.SFX_COMER_FRUTO.get(), SoundSource.PLAYERS, 0.9f, 0.95f + lvl.random.nextFloat() * 0.1f);
            if (lvl instanceof ServerLevel sl)
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, e.getX(), e.getY() + 1.2, e.getZ(), 8, 0.35, 0.4, 0.35, 0.02);
        }
        if (!(e instanceof Player p) || !p.getAbilities().instabuild) s.shrink(1);
        return s;
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.darkcraft.fruto_" + clase.id + ".desc").withStyle(ChatFormatting.GOLD));
        tip.add(Component.translatable("darkcraft.fruto.dura", clase.dura / 20).withStyle(ChatFormatting.GRAY));
    }
}
