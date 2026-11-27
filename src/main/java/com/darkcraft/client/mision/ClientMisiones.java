package com.darkcraft.client.mision;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.mision.Campana;
import com.darkcraft.mision.Oswin;
import com.darkcraft.net.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * Lo que sabe el cliente de las misiones (parte 7ag): la lista que manda el servidor ({@code Net.Misiones}: cada una con sus marcas
 * y adonde ir ahora), para el diario de la Historia, la brujula y el mapa; si toca ver la campana del Arbol de la Horca
 * ({@link Campana#oculta}) y si se la tiene delante para cogerla con la E ({@link #campana}).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class ClientMisiones {
    private ClientMisiones() {}

    public static final List<Net.MisionE> LISTA = new ArrayList<>();
    /** La campana que se tiene delante para cogerla con la E (o -1). */
    public static int campana = -1;

    public static void recibir(Net.Misiones m) {
        LISTA.clear();
        LISTA.addAll(m.lista());
        int k = marcas(Oswin.ID);
        Campana.oculta = (k & (Oswin.LLEVA | Oswin.DEVUELTA | Oswin.QUEDADA)) != 0;
    }

    public static Net.MisionE de(String id) {
        for (Net.MisionE e : LISTA) if (e.id().equals(id)) return e;
        return null;
    }

    public static int marcas(String id) {
        Net.MisionE e = de(id);
        return e == null ? 0 : e.marcas();
    }

    /** Si la mision esta acabada (de una manera o de otra). */
    public static boolean acabada(Net.MisionE e) {
        return Oswin.ID.equals(e.id()) && (e.marcas() & (Oswin.DEVUELTA | Oswin.QUEDADA)) != 0;
    }

    /**
     * Lo que dice el diario de como va (la clave de su texto): {@code darkcraft.mision.oswin.<estado>}: devuelta, quedada, volver
     * (lleva la campana y conoce a Oswin), encontrada (la lleva sin conocerle), buscar (aceptada) o pendiente (le conocio y no dijo que
     * si, o se le perdio la campana).
     */
    public static String estado(Net.MisionE e) {
        int k = e.marcas();
        if ((k & Oswin.DEVUELTA) != 0) return "devuelta";
        if ((k & Oswin.QUEDADA) != 0) return "quedada";
        if ((k & Oswin.LLEVA) != 0) return (k & Oswin.CONOCIDA) != 0 ? "volver" : "encontrada";
        if ((k & Oswin.ACEPTADA) != 0) return "buscar";
        return "pendiente";
    }

    /** Cada tick del cliente: la campana que se tiene delante (a menos de 3,6 bloques, mirandola). */
    public static void tick(Minecraft mc) {
        campana = -1;
        if (Campana.oculta || mc.player == null || mc.level == null || !mc.player.isAlive() || mc.player.isSpectator()) return;
        Vec3 ojo = mc.player.getEyePosition(), mira = mc.player.getLookAngle();
        double mejor = Math.cos(Math.toRadians(24));
        for (Campana c : mc.level.getEntitiesOfClass(Campana.class, mc.player.getBoundingBox().inflate(5))) {
            Vec3 d = c.position().add(0, Campana.ALTO * 0.5, 0).subtract(ojo);
            double l = d.length();
            if (l > 3.6 || l < 1e-3) continue;
            double cos = d.dot(mira) / l + (l < 1.5 ? 0.1 : 0);
            if (cos > mejor) {
                mejor = cos;
                campana = c.getId();
            }
        }
    }

    /** La E con la campana delante: se coge (como el clic derecho: el servidor decide). */
    public static void coger(Minecraft mc) {
        if (campana < 0 || mc.level == null || mc.player == null || mc.gameMode == null) return;
        Entity e = mc.level.getEntity(campana);
        if (e instanceof Campana) mc.gameMode.interact(mc.player, e, InteractionHand.MAIN_HAND);
    }

    @SubscribeEvent
    public static void onSalir(ClientPlayerNetworkEvent.LoggingOut e) {
        LISTA.clear();
        campana = -1;
        Campana.oculta = false;
    }
}
