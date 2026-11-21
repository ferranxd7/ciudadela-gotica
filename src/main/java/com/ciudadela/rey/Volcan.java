package com.ciudadela.rey;

import com.ciudadela.CiudadelaMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** La dimension de los volcanes (fase 3 del Rey). */
public final class Volcan {
    private Volcan() {}

    public static final ResourceKey<Level> NIVEL = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(CiudadelaMod.MODID, "volcanes"));
    public static final Vec3 CENTRO = new Vec3(0.5, 65, 0.5);

    public static boolean es(ServerLevel sl) {
        return sl.dimension() == NIVEL;
    }

    /** Si la dimension existe en este mundo. */
    public static boolean disponible(ServerLevel sl) {
        return false;
    }

    /** Lleva al Rey y a los jugadores a la plataforma de los volcanes. */
    public static void llevar(ServerLevel from, Rey rey, List<ServerPlayer> who) {
    }

    /** Devuelve a un jugador al salon del trono de la ciudadela mas cercana a su punto de reaparicion. */
    public static void devolver(ServerPlayer p) {
        ServerLevel ow = p.getServer().overworld();
        var pos = ow.getSharedSpawnPos();
        Sala s = Sala.near(ow, pos.getX(), pos.getZ());
        if (s != null) {
            Vec3 c = s.center();
            p.teleportTo(ow, c.x, s.t.y(), c.z + 4, 180f, 0f);
        } else p.teleportTo(ow, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0f, 0f);
    }

    public static void devolverTodos(ServerLevel sl) {
        for (ServerPlayer p : List.copyOf(sl.players())) devolver(p);
    }
}
