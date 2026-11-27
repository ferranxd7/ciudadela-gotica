package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3f;

/**
 * El sangrado (parte 7s, los penitentes de la Tierra sin Fe): cada azote de la disciplina, el humo del incienso de sangre...
 * suman sangre perdida en quien lo recibe; si se llega a {@link #TOPE}, <b>hemorragia</b>: un golpe de golpe del 15 % de la
 * vida maxima (3 como poco), un chorro de sangre y frena un momento. Lo sumado se va solo si en 3 s no se suma mas (2 por
 * tick). Se guarda en los datos de la entidad (solo en el servidor). A los jugadores en creativo o espectador, nada; a los
 * penitentes tampoco (ya no les queda sangre).
 */
public final class Sangrado {
    private Sangrado() {}

    /** Lo que hay que sumar para la hemorragia. */
    public static final float TOPE = 100f;
    /** Lo que se lleva la hemorragia (de la vida maxima) y lo menos que quita. */
    public static final float PARTE = 0.15f, MINIMO = 3f;
    private static final String CLAVE = "darkcraft_sangre", CUANDO = "darkcraft_sangre_t";
    public static final DustParticleOptions GOTA = new DustParticleOptions(new Vector3f(0.55f, 0.03f, 0.05f), 1.1f),
            CHORRO = new DustParticleOptions(new Vector3f(0.7f, 0.04f, 0.06f), 1.8f);
    static final BlockParticleOption CUAJO = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState());

    /** Para las pruebas: las hemorragias que ha habido. */
    public static int HEMORRAGIAS;

    /** Lo sumado ahora (con lo que se haya ido desde la ultima vez). */
    public static float nivel(LivingEntity le) {
        CompoundTag d = le.getPersistentData();
        return Math.max(0f, d.getFloat(CLAVE) - perdido(le, d));
    }

    private static float perdido(LivingEntity le, CompoundTag d) {
        long quieto = le.level().getGameTime() - d.getLong(CUANDO);
        return quieto > 60 ? (quieto - 60) * 2f : 0f;
    }

    /** Suma {@code n} de sangre perdida; devuelve si ha habido hemorragia. */
    public static boolean sumar(LivingEntity le, float n, LivingEntity causa) {
        if (le.level().isClientSide || !le.isAlive() || n <= 0) return false;
        if (le instanceof Player p && (p.isCreative() || p.isSpectator())) return false;
        if (le instanceof Penitente) return false;
        CompoundTag d = le.getPersistentData();
        float v = nivel(le) + n;
        d.putLong(CUANDO, le.level().getGameTime());
        if (le.level() instanceof ServerLevel sl)
            sl.sendParticles(GOTA, le.getX(), le.getY() + le.getBbHeight() * 0.6, le.getZ(), 2 + (int) (v / 14), le.getBbWidth() * 0.3,
                    le.getBbHeight() * 0.25, le.getBbWidth() * 0.3, 0.0);
        if (v < TOPE) {
            d.putFloat(CLAVE, v);
            return false;
        }
        d.putFloat(CLAVE, 0f);
        hemorragia(le, causa);
        return true;
    }

    /** Lo sumado vuelve a 0 (al descansar, en las pruebas). */
    public static void limpiar(LivingEntity le) {
        le.getPersistentData().remove(CLAVE);
        le.getPersistentData().remove(CUANDO);
    }

    private static void hemorragia(LivingEntity le, LivingEntity causa) {
        HEMORRAGIAS++;
        float dano = Math.max(MINIMO, le.getMaxHealth() * PARTE);
        le.invulnerableTime = 0;
        le.hurt(causa != null && causa.isAlive() ? le.damageSources().mobAttack(causa) : le.damageSources().generic(), dano);
        le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1));
        if (!(le.level() instanceof ServerLevel sl)) return;
        double y = le.getY() + le.getBbHeight() * 0.6;
        sl.sendParticles(CHORRO, le.getX(), y, le.getZ(), 40, 0.35, 0.45, 0.35, 0.25);
        sl.sendParticles(CUAJO, le.getX(), y, le.getZ(), 30, 0.3, 0.4, 0.3, 0.2);
        sl.sendParticles(GOTA, le.getX(), le.getY() + 0.1, le.getZ(), 26, 0.7, 0.02, 0.7, 0.0);
        sl.playSound(null, le.getX(), y, le.getZ(), DarkCraftMod.SFX_PENITENTE_HEMORRAGIA.get(), SoundSource.HOSTILE, 1.3f, 0.9f + le.getRandom().nextFloat() * 0.2f);
    }
}
