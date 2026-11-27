package com.darkcraft.rol;

import com.darkcraft.DarkCraftMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Las resistencias del jugador (como las defensas de Elden Ring): cuanto menos dano le hace cada clase de golpe. Salen
 * de los talismanes ({@link Talisman}) y de los frutos ({@link Efectos}); se suman multiplicando (dos del 40 % = 64 %) y
 * nunca pasan del 90 %. Con el fruto de ceniza el veneno ni entra (y el que se tenia se cura al comerlo).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Resistencias {
    private Resistencias() {}

    public enum Tipo {
        FISICO, FUEGO, HIELO, RAYO, MAGIA, VENENO, SOMBRA;

        public final String id = name().toLowerCase(java.util.Locale.ROOT);
    }

    private static final float TOPE = 0.9f;

    /** De que clase es un golpe (null = de ninguna: caidas, ahogarse, el vacio...). */
    public static Tipo de(DamageSource s) {
        if (s.is(DamageTypeTags.IS_FIRE)) return Tipo.FUEGO;
        if (s.is(DamageTypeTags.IS_FREEZING)) return Tipo.HIELO;
        if (s.is(DamageTypeTags.IS_LIGHTNING)) return Tipo.RAYO;
        if (s.is(DamageTypes.WITHER) || s.is(DamageTypes.WITHER_SKULL)) return Tipo.SOMBRA;
        if (s.is(DamageTypes.MAGIC) || s.is(DamageTypes.INDIRECT_MAGIC) || s.is(DamageTypes.SONIC_BOOM) || s.is(DamageTypes.DRAGON_BREATH)) return Tipo.MAGIA;
        if (s.is(DamageTypeTags.IS_FALL) || s.is(DamageTypeTags.IS_DROWNING) || s.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || s.is(DamageTypes.STARVE) || s.is(DamageTypes.IN_WALL) || s.is(DamageTypes.CRAMMING)) return null;
        return s.getEntity() != null || s.getDirectEntity() != null || s.is(DamageTypeTags.IS_EXPLOSION) || s.is(DamageTypeTags.IS_PROJECTILE)
                ? Tipo.FISICO : null;
    }

    /** La resistencia del jugador a una clase de golpe (0..0,9). Solo en el servidor (el cliente pasa sus talismanes). */
    public static float de(Player p, Tipo t) {
        return de(Talismanes.llevados(p), p, t);
    }

    /** La resistencia con estos talismanes y los efectos que tiene el jugador. */
    public static float de(net.minecraft.world.item.ItemStack[] talismanes, Player p, Tipo t) {
        float queda = 1f;
        Talisman.Clase tal = switch (t) {
            case FUEGO -> Talisman.Clase.BRASA;
            case HIELO -> Talisman.Clase.ESCARCHA;
            case RAYO -> Talisman.Clase.TRUENO;
            case SOMBRA -> Talisman.Clase.SOMBRA;
            case FISICO -> Talisman.Clase.HUESO;
            default -> null;
        };
        if (tal != null && Talismanes.lleva(talismanes, tal)) queda *= 1f - (t == Tipo.FISICO ? 0.10f : 0.40f);
        // la Bufanda de Tam (parte 7ah): la lana que no ardio
        if (t == Tipo.FUEGO && Talismanes.lleva(talismanes, Talisman.Clase.BUFANDA)) queda *= 0.80f;
        MobEffect fruto = switch (t) {
            case FUEGO -> Efectos.BRASA.get();
            case HIELO -> Efectos.ESCARCHA.get();
            case RAYO -> Efectos.TORMENTA.get();
            case MAGIA -> Efectos.LUNAR.get();
            case VENENO -> Efectos.CENIZA.get();
            case SOMBRA -> Efectos.SOMBRA.get();
            case FISICO -> Efectos.HUESO.get();
        };
        if (p.hasEffect(fruto)) queda *= 1f - (t == Tipo.FISICO ? 0.15f : t == Tipo.VENENO ? 1f : 0.50f);
        return Math.min(TOPE, 1f - queda);
    }

    /** Todas, para el menu y la prueba (en el orden de {@link Tipo}). */
    public static float[] todas(Player p) {
        Tipo[] ts = Tipo.values();
        float[] r = new float[ts.length];
        for (int i = 0; i < ts.length; i++) r[i] = de(p, ts[i]);
        return r;
    }

    // ------------------------------------------------------------------ lo que hacen

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGolpe(LivingHurtEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp) || e.getAmount() <= 0f) return;
        Tipo t = de(e.getSource());
        if (t == null) return;
        float r = de(sp, t);
        if (r > 0f) e.setAmount(e.getAmount() * (1f - r));
    }

    /** El veneno y el marchitamiento: con resistencia duran menos; con toda (el fruto de ceniza) el veneno no entra. */
    @SubscribeEvent
    public static void onMal(MobEffectEvent.Applicable e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        MobEffectInstance m = e.getEffectInstance();
        Tipo t = m.getEffect() == MobEffects.POISON ? Tipo.VENENO : m.getEffect() == MobEffects.WITHER ? Tipo.SOMBRA : null;
        if (t == null) return;
        float r = de(sp, t);
        if (r >= 0.89f && t == Tipo.VENENO) e.setResult(Event.Result.DENY);
    }

    /** Con el fruto de escarcha la nieve polvo no congela. */
    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.player.level().isClientSide()) return;
        if (e.player.getTicksFrozen() > 0 && e.player.hasEffect(Efectos.ESCARCHA.get())) e.player.setTicksFrozen(0);
    }
}
