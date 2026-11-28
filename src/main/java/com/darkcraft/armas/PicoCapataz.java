package com.darkcraft.armas;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.combate.Armas;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * El Pico del Capataz (parte 7ar): el pico doble de hierro negro de Hakon, con la veta roja incrustada en la cabeza; lo deja al caer.
 * Para el combate es un gran martillo ({@link Armas.Tipo#GRAN_MARTILLO}: sus golpes, romper la guardia) que escala bien con la Fuerza
 * y algo con la Destreza ({@code Escalado.de}: B y D) y, como la veta sigue viva dentro, cada golpe deja cristal en quien lo recibe: al
 * {@link #GOLPES_VETA}-o golpe seguido (sin pasar {@link #ESPERA} ticks entre uno y otro) el cristal revienta: {@link #DANO_VETA} de dano a el
 * y a los enemigos de alrededor ({@link #R_VETA} bloques).
 */
public class PicoCapataz extends Martillo {
    public static final int GOLPES_VETA = 4, ESPERA = 160;
    public static final float DANO_VETA = 7f;
    public static final double R_VETA = 2.6;
    private static final String CLAVE = "darkcraft_veta", CLAVE_T = "darkcraft_veta_t";
    /** Para la prueba: los golpes que han dejado cristal y las veces que ha reventado desde que arranco el juego. */
    public static int GOLPES, ESTALLIDOS;

    public PicoCapataz(Properties p) {
        super("pico_capataz", Armas.Tipo.GRAN_MARTILLO, 9, -3.3f, p);
    }

    @Override
    public boolean hurtEnemy(ItemStack st, LivingEntity a, LivingEntity quien) {
        if (!a.level().isClientSide && a.isAlive()) veta(a, quien);
        return super.hurtEnemy(st, a, quien);
    }

    /** Un golpe con el pico: deja cristal en {@code a}; al cuarto seguido revienta. Devuelve si ha reventado. */
    public static boolean veta(LivingEntity a, LivingEntity quien) {
        if (!(a.level() instanceof ServerLevel sl)) return false;
        GOLPES++;
        CompoundTag t = a.getPersistentData();
        long ahora = sl.getGameTime();
        int n = ahora - t.getLong(CLAVE_T) > ESPERA ? 0 : t.getInt(CLAVE);
        t.putLong(CLAVE_T, ahora);
        n++;
        sl.sendParticles(com.darkcraft.enemigos.Capataz.ROJO, a.getX(), a.getY() + a.getBbHeight() * 0.6, a.getZ(), 3 + n * 2, 0.25, 0.3, 0.25, 0.0);
        if (n < GOLPES_VETA) {
            t.putInt(CLAVE, n);
            return false;
        }
        t.putInt(CLAVE, 0);
        ESTALLIDOS++;
        double y = a.getY();
        sl.playSound(null, a.getX(), y + 1, a.getZ(), DarkCraftMod.SFX_CAPATAZ_VETA.get(), SoundSource.PLAYERS, 0.9f, 1.35f);
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState()), a.getX(), y + a.getBbHeight() * 0.5, a.getZ(),
                30, 0.5, 0.5, 0.5, 0.2);
        for (int k = 0; k < 4; k++) {
            double ang = k * Math.PI / 2 + sl.random.nextDouble() * 0.6;
            EfectosRaiz.estaca(sl, com.darkcraft.gen.TorreCupula.gema(true), a.getX() + Math.cos(ang) * 0.7, y - 0.2, a.getZ() + Math.sin(ang) * 0.7,
                    EfectosRaiz.ang(sl.random), 0.5f * (sl.random.nextFloat() - 0.5f), 1.2f + sl.random.nextFloat() * 0.5f, 0.3f, 0, 14);
        }
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, a.getBoundingBox().inflate(R_VETA, 1.5, R_VETA),
                e -> e.isAlive() && e != quien && (e == a || e instanceof Enemy))) {
            if (e != a && e.distanceToSqr(a) > R_VETA * R_VETA) continue;
            e.invulnerableTime = 0;
            e.hurt(sl.damageSources().indirectMagic(quien, quien), DANO_VETA);
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack s, Level lvl, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.darkcraft.pico_capataz.desc").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tip.add(Component.translatable("item.darkcraft.pico_capataz.veta").withStyle(ChatFormatting.RED));
        tip.add(Component.translatable("darkcraft.martillo.guardia").withStyle(ChatFormatting.GOLD));
    }
}
