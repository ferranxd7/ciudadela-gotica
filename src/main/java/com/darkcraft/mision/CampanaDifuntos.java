package com.darkcraft.mision;

import com.darkcraft.DarkCraftMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * La Campana de los difuntos (parte 7ag): la de Oswin, para quien se la quedo ({@link Oswin}). Se toca (usarla, o la G desde los
 * objetos rapidos): los muertos de alrededor ({@link #RADIO} bloques; los jefes no) se apartan de quien la toca, se quedan torpes y
 * debiles {@link #EFECTO} ticks. Vuelve a sonar a los {@link #ESPERA} ticks.
 */
public class CampanaDifuntos extends Item {
    public static final double RADIO = 9;
    public static final int EFECTO = 120, ESPERA = 600;
    /** Los que tienen mas vida que esto son jefes: a esos no los aparta. */
    static final float JEFE = 200;

    public CampanaDifuntos(Properties p) {
        super(p);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level lvl, Player p, InteractionHand mano) {
        ItemStack st = p.getItemInHand(mano);
        if (p.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(st);
        p.getCooldowns().addCooldown(this, ESPERA);
        if (lvl instanceof ServerLevel sl) {
            sl.playSound(null, p.getX(), p.getY() + 1, p.getZ(), DarkCraftMod.SFX_CAMPANA_TOQUE.get(), SoundSource.PLAYERS, 1.6f, 0.82f);
            sl.sendParticles(ParticleTypes.SOUL, p.getX(), p.getY() + 1.2, p.getZ(), 24, 1.6, 0.6, 1.6, 0.03);
            int n = 0;
            for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(RADIO))) {
                if (!(e instanceof Enemy) || e.getMaxHealth() > JEFE || !e.isAlive()) continue;
                Vec3 lejos = e.position().subtract(p.position()).multiply(1, 0, 1);
                if (lejos.lengthSqr() < 1e-4) continue;
                lejos = lejos.normalize().scale(0.9);
                e.push(lejos.x, 0.25, lejos.z);
                e.hurtMarked = true;
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EFECTO, 3));
                e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EFECTO, 1));
                sl.sendParticles(ParticleTypes.SCULK_SOUL, e.getX(), e.getY() + e.getBbHeight() * 0.6, e.getZ(), 5, 0.2, 0.3, 0.2, 0.02);
                n++;
            }
            DarkCraftMod.LOGGER.info("[DarkCraft] {} toca la campana de los difuntos: {} muertos se apartan", p.getGameProfile().getName(), n);
        }
        return InteractionResultHolder.sidedSuccess(st, lvl.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.darkcraft.campana_difuntos.desc").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
