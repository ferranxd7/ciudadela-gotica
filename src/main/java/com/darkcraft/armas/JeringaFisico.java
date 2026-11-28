package com.darkcraft.armas;

import com.darkcraft.DarkCraftMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * La Jeringa del Fisico (parte 7av): la jeringa enorme de laton y cristal de Maese Galen, con lo que queda del elixir dentro. La deja
 * al caer. Para el combate es una lanza ({@code Armas.de}: sus estocadas) que escala con la Destreza y el Arcano ({@code Escalado.de}:
 * C y B). Cada golpe envenena un poco ({@link #VENENO} ticks); al {@link #GOLPES_SANGRE}-o golpe seguido al mismo enemigo (sin pasar
 * {@link #ESPERA} ticks entre uno y otro) la jeringa se llena de su sangre y le devuelve vida a quien la lleva ({@link #CURA} mas un
 * {@link #CURA_PARTE} de su vida maxima).
 */
public class JeringaFisico extends SwordItem {
    public static final int GOLPES_SANGRE = 3, ESPERA = 140, VENENO = 60;
    public static final float CURA = 2f, CURA_PARTE = 0.06f;
    private static final String CLAVE = "darkcraft_jeringa", CLAVE_T = "darkcraft_jeringa_t";
    /** Para la prueba: los golpes que han envenenado y las veces que la jeringa ha curado desde que arranco el juego. */
    public static int GOLPES, CURAS;

    public JeringaFisico(Properties p) {
        super(Tiers.IRON, 3, -2.7f, p);
    }

    @Override
    public boolean hurtEnemy(ItemStack st, LivingEntity a, LivingEntity quien) {
        if (!a.level().isClientSide && a.isAlive()) pinchar(a, quien);
        return super.hurtEnemy(st, a, quien);
    }

    /** Un golpe con la jeringa: veneno y, al tercero seguido al mismo, la sangre que cura a quien la lleva. Devuelve si ha curado. */
    public static boolean pinchar(LivingEntity a, LivingEntity quien) {
        if (!(a.level() instanceof ServerLevel sl)) return false;
        GOLPES++;
        a.addEffect(new MobEffectInstance(MobEffects.POISON, VENENO, 0), quien);
        CompoundTag t = a.getPersistentData();
        long ahora = sl.getGameTime();
        int n = ahora - t.getLong(CLAVE_T) > ESPERA ? 0 : t.getInt(CLAVE);
        t.putLong(CLAVE_T, ahora);
        n++;
        double y = a.getY() + a.getBbHeight() * 0.6;
        sl.sendParticles(com.darkcraft.enemigos.Galen.ELIXIR, a.getX(), y, a.getZ(), 4 + n * 2, 0.25, 0.3, 0.25, 0.0);
        if (n < GOLPES_SANGRE || quien == null) {
            t.putInt(CLAVE, n);
            return false;
        }
        t.putInt(CLAVE, 0);
        CURAS++;
        quien.heal(CURA + quien.getMaxHealth() * CURA_PARTE);
        sl.playSound(null, quien.getX(), quien.getY() + 1, quien.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.9f, 0.7f);
        for (int k = 0; k < 8; k++) {
            double s = k / 8.0;
            sl.sendParticles(com.darkcraft.enemigos.Sangrado.GOTA, a.getX() + (quien.getX() - a.getX()) * s, y + (quien.getY() + 1.0 - y) * s,
                    a.getZ() + (quien.getZ() - a.getZ()) * s, 1, 0.05, 0.05, 0.05, 0.0);
        }
        sl.sendParticles(ParticleTypes.HEART, quien.getX(), quien.getY() + quien.getBbHeight() + 0.2, quien.getZ(), 2, 0.3, 0.1, 0.3, 0.0);
        sl.playSound(null, a.getX(), a.getY() + 1, a.getZ(), DarkCraftMod.SFX_GALEN_JERINGA.get(), SoundSource.PLAYERS, 0.6f, 1.3f);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.darkcraft.jeringa_fisico.desc").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tip.add(Component.translatable("item.darkcraft.jeringa_fisico.sangre").withStyle(ChatFormatting.GOLD));
    }
}
