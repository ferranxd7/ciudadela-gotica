package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.net.Net;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

/**
 * La congelacion (parte 7aj: las flechas de escarcha y la ceniza de la escarcha), como el {@link Sangrado}: cada golpe de frio
 * suma en quien lo recibe; si se llega a {@link #TOPE}, <b>se congela</b>: un golpe de frio del 10 % de la vida maxima (a los
 * jefes el 4 %; 3 como poco), se queda helado (la escarcha del juego: tiembla y frena; {@link Helado#helar}), lento 5 s y
 * durante {@link #DEBIL_TICKS} recibe un 20 % mas de dano ({@link #DEBIL}). Lo sumado se va solo si en 3 s no se suma mas (2
 * por tick). A los jefes les entra la mitad. A los helados, a la Sombra y a lo que el juego no deja congelarse
 * ({@code canFreeze}: los esqueletos de la nieve...), nada. El golpe es de frio (lo frenan el fruto y el talisman de la escarcha).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Congelacion {
    private Congelacion() {}

    public static final float TOPE = 100f;
    public static final float PARTE = 0.10f, PARTE_JEFE = 0.04f, MINIMO = 3f;
    /** Lo que recibe de mas quien se acaba de congelar, y cuanto le dura (ticks). */
    public static final float DEBIL = 1.20f;
    public static final int DEBIL_TICKS = 120;
    private static final String CLAVE = "darkcraft_frio", CUANDO = "darkcraft_frio_t", HASTA = "darkcraft_frio_hasta";
    public static final DustParticleOptions ESCARCHA = new DustParticleOptions(new Vector3f(0.72f, 0.9f, 1f), 0.9f),
            HIELO = new DustParticleOptions(new Vector3f(0.6f, 0.84f, 1f), 1.6f);

    /** Para las pruebas: las congelaciones que ha habido. */
    public static int CONGELACIONES;

    /** Lo sumado ahora (con lo que se haya ido desde la ultima vez). */
    public static float nivel(LivingEntity le) {
        CompoundTag d = le.getPersistentData();
        long quieto = le.level().getGameTime() - d.getLong(CUANDO);
        return Math.max(0f, d.getFloat(CLAVE) - (quieto > 60 ? (quieto - 60) * 2f : 0f));
    }

    /** Los que no se congelan. */
    public static boolean inmune(LivingEntity le) {
        return le instanceof Helado || le instanceof Sombra || !le.canFreeze();
    }

    /** Suma {@code n} de frio; devuelve si se ha congelado. */
    public static boolean sumar(LivingEntity le, float n, LivingEntity causa) {
        if (le.level().isClientSide || !le.isAlive() || n <= 0) return false;
        if (le instanceof Player p && (p.isCreative() || p.isSpectator())) return false;
        if (inmune(le)) return false;
        if (Sangrado.jefe(le)) n *= Sangrado.JEFE;
        CompoundTag d = le.getPersistentData();
        float v = nivel(le) + n;
        d.putLong(CUANDO, le.level().getGameTime());
        if (le.level() instanceof ServerLevel sl)
            sl.sendParticles(ParticleTypes.SNOWFLAKE, le.getX(), le.getY() + le.getBbHeight() * 0.6, le.getZ(), 3 + (int) (v / 12),
                    le.getBbWidth() * 0.35, le.getBbHeight() * 0.25, le.getBbWidth() * 0.35, 0.01);
        if (v < TOPE) {
            d.putFloat(CLAVE, v);
            Net.cerca(le, new Net.Acumula(le.getId(), Net.Acumula.FRIO, v, false));
            return false;
        }
        d.putFloat(CLAVE, 0f);
        Net.cerca(le, new Net.Acumula(le.getId(), Net.Acumula.FRIO, 0f, true));
        congelar(le, causa);
        return true;
    }

    /** Lo sumado vuelve a 0 (al descansar, en las pruebas). */
    public static void limpiar(LivingEntity le) {
        CompoundTag d = le.getPersistentData();
        boolean habia = d.contains(CLAVE);
        d.remove(CLAVE);
        d.remove(CUANDO);
        d.remove(HASTA);
        if (habia && !le.level().isClientSide) Net.cerca(le, new Net.Acumula(le.getId(), Net.Acumula.FRIO, 0f, false));
    }

    /** Si se acaba de congelar (y recibe mas dano). */
    public static boolean debil(LivingEntity le) {
        return le.level().getGameTime() < le.getPersistentData().getLong(HASTA);
    }

    private static void congelar(LivingEntity le, LivingEntity causa) {
        CONGELACIONES++;
        float dano = Math.max(MINIMO, le.getMaxHealth() * (Sangrado.jefe(le) ? PARTE_JEFE : PARTE));
        le.invulnerableTime = 0;
        le.hurt(Helado.frio(le.level(), causa, causa), dano);
        Helado.helar(le, 200);
        le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
        le.getPersistentData().putLong(HASTA, le.level().getGameTime() + DEBIL_TICKS);
        if (!(le.level() instanceof ServerLevel sl)) return;
        double y = le.getY() + le.getBbHeight() * 0.55;
        sl.sendParticles(HIELO, le.getX(), y, le.getZ(), 36, 0.35, 0.5, 0.35, 0.2);
        sl.sendParticles(ParticleTypes.SNOWFLAKE, le.getX(), y, le.getZ(), 40, 0.4, 0.5, 0.4, 0.08);
        sl.sendParticles(ParticleTypes.ITEM_SNOWBALL, le.getX(), y, le.getZ(), 18, 0.3, 0.4, 0.3, 0.15);
        sl.playSound(null, le.getX(), y, le.getZ(), DarkCraftMod.SFX_CONGELACION.get(), SoundSource.HOSTILE, 1.2f, 0.95f + le.getRandom().nextFloat() * 0.1f);
    }

    /** Quien se acaba de congelar recibe un 20 % mas de todo (menos de lo que le congela). */
    @SubscribeEvent
    public static void onHerido(LivingHurtEvent e) {
        LivingEntity le = e.getEntity();
        if (le.level().isClientSide || !debil(le)) return;
        e.setAmount(e.getAmount() * DEBIL);
        if (le.level() instanceof ServerLevel sl && le.tickCount % 2 == 0)
            sl.sendParticles(ParticleTypes.SNOWFLAKE, le.getX(), le.getY() + le.getBbHeight() * 0.6, le.getZ(), 6, 0.3, 0.3, 0.3, 0.05);
    }
}
