package com.darkcraft.enemigos;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.net.Net;
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
 * <p>
 * Desde la parte 7aj tambien lo hacen las flechas de sangre y la ceniza del tajo sangriento: a los jefes les entra la mitad y
 * su hemorragia es del 5 %; los que no tienen sangre (esqueletos, perros de hueso, espectros, gargolas, calcinados, la Sombra)
 * no sangran. Lo sumado se manda a los clientes que lo ven ({@link Net.Acumula}: la barra encima de cada uno).
 */
public final class Sangrado {
    private Sangrado() {}

    /** Lo que hay que sumar para la hemorragia. */
    public static final float TOPE = 100f;
    /** Lo que se lleva la hemorragia (de la vida maxima; a un jefe, {@code PARTE_JEFE}) y lo menos que quita. */
    public static final float PARTE = 0.15f, PARTE_JEFE = 0.05f, MINIMO = 3f;
    /** A los jefes les entra esta parte de lo que se suma. */
    public static final float JEFE = 0.5f;
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

    /** Si es un jefe (los que llevan barra: los guardianes, el Capitan, el Rey, Ser Gavran y los del juego). */
    public static boolean jefe(LivingEntity le) {
        return le instanceof Guardian || le instanceof Capitan || le instanceof com.darkcraft.rey.Rey || le instanceof com.darkcraft.gavran.Gavran
                || le instanceof net.minecraft.world.entity.boss.wither.WitherBoss || le instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon;
    }

    /** Los que no sangran: no les queda sangre o nunca la tuvieron. */
    public static boolean sinSangre(LivingEntity le) {
        return le instanceof Penitente || le instanceof Arquero || le instanceof Perro || le instanceof Espectro || le instanceof Gargola
                || le instanceof Calcinado || le instanceof Sombra || le instanceof net.minecraft.world.entity.monster.AbstractSkeleton
                || le instanceof com.darkcraft.montura.Corcel;
    }

    /** Suma {@code n} de sangre perdida; devuelve si ha habido hemorragia. */
    public static boolean sumar(LivingEntity le, float n, LivingEntity causa) {
        if (le.level().isClientSide || !le.isAlive() || n <= 0) return false;
        if (le instanceof Player p && (p.isCreative() || p.isSpectator())) return false;
        if (sinSangre(le)) return false;
        if (jefe(le)) n *= JEFE;
        CompoundTag d = le.getPersistentData();
        float v = nivel(le) + n;
        d.putLong(CUANDO, le.level().getGameTime());
        if (le.level() instanceof ServerLevel sl)
            sl.sendParticles(GOTA, le.getX(), le.getY() + le.getBbHeight() * 0.6, le.getZ(), 2 + (int) (v / 14), le.getBbWidth() * 0.3,
                    le.getBbHeight() * 0.25, le.getBbWidth() * 0.3, 0.0);
        if (v < TOPE) {
            d.putFloat(CLAVE, v);
            Net.cerca(le, new Net.Acumula(le.getId(), Net.Acumula.SANGRE, v, false));
            return false;
        }
        d.putFloat(CLAVE, 0f);
        Net.cerca(le, new Net.Acumula(le.getId(), Net.Acumula.SANGRE, 0f, true));
        hemorragia(le, causa);
        return true;
    }

    /** Lo sumado vuelve a 0 (al descansar, en las pruebas). */
    public static void limpiar(LivingEntity le) {
        boolean habia = le.getPersistentData().contains(CLAVE);
        le.getPersistentData().remove(CLAVE);
        le.getPersistentData().remove(CUANDO);
        if (habia && !le.level().isClientSide)
            Net.cerca(le, new Net.Acumula(le.getId(), Net.Acumula.SANGRE, 0f, false));
    }

    private static void hemorragia(LivingEntity le, LivingEntity causa) {
        HEMORRAGIAS++;
        float dano = Math.max(MINIMO, le.getMaxHealth() * (jefe(le) ? PARTE_JEFE : PARTE));
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
