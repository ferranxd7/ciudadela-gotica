package com.darkcraft.rol;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.Combos;
import com.darkcraft.net.Net;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Los frascos: el rojo cura y el azul da mana. No son objetos del inventario: cada jugador lleva los suyos en su ficha
 * (se ven abajo a la izquierda), se beben con su tecla y se rellenan al descansar en un santuario o al reaparecer. Beber
 * lleva un momento (se levanta el frasco, que lo ven todos) y el trago hace efecto a mitad; mientras tanto no se ataca.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Frascos {
    private Frascos() {}

    public static final int VIDA = Ficha.Frascos.VIDA, MANA = Ficha.Frascos.MANA;
    /** Lo que dura beber (ticks) y el tick en que el trago hace efecto. */
    public static final int DURA = 26, TRAGO = 14;
    /** Entre todos los frascos, como mucho estos (se consiguen mas por el mundo). */
    public static final int TOPE = 14;

    private static final class Bebe {
        int tipo, t;
    }

    private static final Map<UUID, Bebe> BEBIENDO = new HashMap<>();

    public static boolean bebiendo(Player p) {
        return BEBIENDO.containsKey(p.getUUID());
    }

    /** El cliente pide beber (la tecla de cada frasco). */
    public static void pedir(ServerPlayer sp, int tipo) {
        if ((tipo != VIDA && tipo != MANA) || !sp.isAlive() || sp.isSpectator() || sp.isSleeping() || BEBIENDO.containsKey(sp.getUUID())) return;
        Ficha.Frascos f = Ficha.frascos(sp);
        if ((tipo == MANA ? f.mana() : f.vida()) <= 0) {
            Net.to(sp, new Net.Bebe(sp.getId(), tipo, Net.Bebe.VACIO));
            return;
        }
        boolean lleno = tipo == VIDA ? sp.getHealth() >= sp.getMaxHealth() - 0.01f : Combos.energia(sp) >= Rol.manaMax(sp);
        if (lleno) {
            Net.to(sp, new Net.Bebe(sp.getId(), tipo, Net.Bebe.LLENO));
            return;
        }
        sp.stopUsingItem();
        Bebe b = new Bebe();
        b.tipo = tipo;
        BEBIENDO.put(sp.getUUID(), b);
        Net.CH.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> sp), new Net.Bebe(sp.getId(), tipo, Net.Bebe.BEBE));
        sp.level().playSound(null, sp.getX(), sp.getEyeY(), sp.getZ(), DarkCraftMod.SFX_FRASCO_BEBER.get(), SoundSource.PLAYERS, 0.8f, 1.0f);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || BEBIENDO.isEmpty()) return;
        for (Iterator<Map.Entry<UUID, Bebe>> it = BEBIENDO.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Bebe> en = it.next();
            ServerPlayer sp = ev.getServer().getPlayerList().getPlayer(en.getKey());
            Bebe b = en.getValue();
            if (sp == null || !sp.isAlive()) {
                it.remove();
                continue;
            }
            if (++b.t == TRAGO) trago(sp, b.tipo);
            if (b.t >= DURA) it.remove();
        }
    }

    private static final DustColorTransitionOptions ROJO = new DustColorTransitionOptions(new Vector3f(1.0f, 0.35f, 0.3f), new Vector3f(0.6f, 0.05f, 0.08f), 0.9f),
            AZUL = new DustColorTransitionOptions(new Vector3f(0.45f, 0.75f, 1.0f), new Vector3f(0.1f, 0.2f, 0.7f), 0.9f);

    private static void trago(ServerPlayer sp, int tipo) {
        Ficha.Frascos f = Ficha.frascos(sp);
        if (tipo == VIDA) {
            if (f.vida() <= 0) return;
            Ficha.frascos(sp, new Ficha.Frascos(f.vidaMax(), f.manaMax(), f.vida() - 1, f.mana(), f.mejora()));
            // el Rosario del peregrino (parte 7ag): un 25 % mas
            sp.heal(Nivel.curaFrasco(sp.getMaxHealth(), f.mejora()) * (Talismanes.lleva(sp, Talisman.Clase.PEREGRINO) ? 1.25f : 1f));
        } else {
            if (f.mana() <= 0) return;
            Ficha.frascos(sp, new Ficha.Frascos(f.vidaMax(), f.manaMax(), f.vida(), f.mana() - 1, f.mejora()));
            Combos.dar(sp, Nivel.manaFrasco(Rol.manaMax(sp), f.mejora()));
        }
        Rol.fichaCambiada(sp);
        if (sp.level() instanceof ServerLevel sl)
            sl.sendParticles(tipo == VIDA ? ROJO : AZUL, sp.getX(), sp.getY() + 1.0, sp.getZ(), 14, 0.3, 0.5, 0.3, 0.02);
    }

    /** Llena los frascos (al descansar en un santuario y al reaparecer). */
    public static void rellenar(ServerPlayer sp) {
        Ficha.Frascos f = Ficha.frascos(sp);
        if (f.vida() == f.vidaMax() && f.mana() == f.manaMax()) return;
        Ficha.frascos(sp, new Ficha.Frascos(f.vidaMax(), f.manaMax(), f.vidaMax(), f.manaMax(), f.mejora()));
        Rol.fichaCambiada(sp);
    }

    /** En el santuario: cuantos frascos quiere de vida (los demas, de mana). Se dejan llenos. */
    public static void repartir(ServerPlayer sp, int vida) {
        if (!sp.isCreative() && !com.darkcraft.santuario.Santuarios.junto(sp)) return;
        Ficha.Frascos f = Ficha.frascos(sp);
        int total = f.vidaMax() + f.manaMax();
        vida = Math.max(0, Math.min(total, vida));
        Ficha.frascos(sp, new Ficha.Frascos(vida, total - vida, vida, total - vida, f.mejora()));
        Rol.fichaCambiada(sp);
    }

    /** Un frasco mas (encontrado por el mundo): se suma a los de vida, lleno. Devuelve si cabia. */
    public static boolean otro(ServerPlayer sp) {
        Ficha.Frascos f = Ficha.frascos(sp);
        if (f.vidaMax() + f.manaMax() >= TOPE) return false;
        Ficha.frascos(sp, new Ficha.Frascos(f.vidaMax() + 1, f.manaMax(), f.vida() + 1, f.mana(), f.mejora()));
        Rol.fichaCambiada(sp);
        return true;
    }

    /** Sube la mejora de los frascos (cada una hace que curen mas). */
    public static void mejorar(ServerPlayer sp, int mejora) {
        Ficha.Frascos f = Ficha.frascos(sp);
        Ficha.frascos(sp, new Ficha.Frascos(f.vidaMax(), f.manaMax(), f.vida(), f.mana(), Math.max(0, Math.min(12, mejora))));
        Rol.fichaCambiada(sp);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        BEBIENDO.remove(e.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent e) {
        BEBIENDO.clear();
    }
}
