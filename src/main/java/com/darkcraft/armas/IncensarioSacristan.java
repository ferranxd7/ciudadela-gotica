package com.darkcraft.armas;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.combate.Armas;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * El Incensario del Sacristan (parte 7at): el incensario de bronce de Fray Osmund, con su cadena; dentro sigue ardiendo el polvo de
 * hueso. Lo deja al caer. Para el combate es una maza ({@link Armas.Tipo#MARTILLO}: sus golpes, romper la guardia; a los Ensamblados
 * los deja en polvo) que escala con la Fe y algo con la Fuerza ({@code Escalado.de}: B y C). Cada golpe esparce polvo de hueso: a
 * quien lo recibe lo frena un momento; al {@link #GOLPES_BRASA}-o golpe seguido (sin pasar {@link #ESPERA} ticks entre uno y otro) las
 * brasas se avivan: prenden a el y a los enemigos de alrededor ({@link #R_BRASA} bloques) y les hacen {@link #DANO_BRASA} de dano.
 */
public class IncensarioSacristan extends Martillo {
    public static final int GOLPES_BRASA = 3, ESPERA = 140;
    public static final float DANO_BRASA = 5f;
    public static final double R_BRASA = 2.4;
    private static final String CLAVE = "darkcraft_brasa", CLAVE_T = "darkcraft_brasa_t";
    /** Para la prueba: los golpes que han esparcido polvo y las veces que se han avivado las brasas desde que arranco el juego. */
    public static int GOLPES, AVIVADAS;

    public IncensarioSacristan(Properties p) {
        super("incensario_sacristan", Armas.Tipo.MARTILLO, 6, -2.8f, p);
    }

    @Override
    public boolean hurtEnemy(ItemStack st, LivingEntity a, LivingEntity quien) {
        if (!a.level().isClientSide && a.isAlive()) brasa(a, quien);
        return super.hurtEnemy(st, a, quien);
    }

    /** Un golpe con el incensario: polvo de hueso (frena) y, al tercero seguido, las brasas. Devuelve si se han avivado. */
    public static boolean brasa(LivingEntity a, LivingEntity quien) {
        if (!(a.level() instanceof ServerLevel sl)) return false;
        GOLPES++;
        CompoundTag t = a.getPersistentData();
        long ahora = sl.getGameTime();
        int n = ahora - t.getLong(CLAVE_T) > ESPERA ? 0 : t.getInt(CLAVE);
        t.putLong(CLAVE_T, ahora);
        n++;
        double y = a.getY() + a.getBbHeight() * 0.6;
        sl.sendParticles(com.darkcraft.enemigos.Osmund.POLVO, a.getX(), y, a.getZ(), 6 + n * 2, 0.3, 0.3, 0.3, 0.0);
        sl.sendParticles(ParticleTypes.WHITE_ASH, a.getX(), y, a.getZ(), 6, 0.3, 0.3, 0.3, 0.01);
        a.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0), quien);
        if (n < GOLPES_BRASA) {
            t.putInt(CLAVE, n);
            return false;
        }
        t.putInt(CLAVE, 0);
        AVIVADAS++;
        sl.playSound(null, a.getX(), a.getY() + 1, a.getZ(), DarkCraftMod.SFX_OSMUND_GIRO.get(), SoundSource.PLAYERS, 0.8f, 1.3f);
        sl.sendParticles(ParticleTypes.FLAME, a.getX(), a.getY() + a.getBbHeight() * 0.5, a.getZ(), 24, 0.6, 0.4, 0.6, 0.06);
        sl.sendParticles(ParticleTypes.LAVA, a.getX(), a.getY() + a.getBbHeight() * 0.5, a.getZ(), 4, 0.4, 0.2, 0.4, 0.0);
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, a.getBoundingBox().inflate(R_BRASA, 1.5, R_BRASA),
                e -> e.isAlive() && e != quien && (e == a || e instanceof Enemy))) {
            if (e != a && e.distanceToSqr(a) > R_BRASA * R_BRASA) continue;
            e.invulnerableTime = 0;
            e.hurt(sl.damageSources().indirectMagic(quien, quien), DANO_BRASA);
            e.setSecondsOnFire(3);
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.darkcraft.incensario_sacristan.desc").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tip.add(Component.translatable("item.darkcraft.incensario_sacristan.brasa").withStyle(ChatFormatting.GOLD));
        tip.add(Component.translatable("darkcraft.martillo.guardia").withStyle(ChatFormatting.GOLD));
    }
}
