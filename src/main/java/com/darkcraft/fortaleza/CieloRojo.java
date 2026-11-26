package com.darkcraft.fortaleza;

import com.darkcraft.net.Net;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * El cielo rojo (parte 5e, respuestas 63-66 del plan): desde que cae el rayo rojo sobre el portal de la Sombra, en todo el
 * mundo las nubes arden rojas, el cielo y la niebla se tiñen de rojo, sale la luna de sangre y de cada portal abierto sube
 * una columna de luz roja al cielo. Dura hasta vencer a la Sombra (parte 7: {@link #apagar}). Se guarda por mundo y se
 * manda a cada jugador al entrar ({@code client/fortaleza/CieloRojoCliente} lo dibuja).
 */
@Mod.EventBusSubscriber(modid = com.darkcraft.DarkCraftMod.MODID)
public final class CieloRojo extends SavedData {
    public static final String ID = "darkcraft_cielo_rojo";

    /** Si esta encendido, desde que tick del mundo (para el fundido), y si ya se vencio a la Sombra (no vuelve). */
    public boolean activo, vencida;
    public long desde;
    /** Las columnas de luz: {x, y, z} del pie (lo alto del arco de cada portal abierto). */
    public final List<int[]> columnas = new ArrayList<>();

    public static CieloRojo get(MinecraftServer srv) {
        return srv.overworld().getDataStorage().computeIfAbsent(CieloRojo::load, CieloRojo::new, ID);
    }

    private static CieloRojo load(CompoundTag t) {
        CieloRojo c = new CieloRojo();
        c.activo = t.getBoolean("activo");
        c.vencida = t.getBoolean("vencida");
        c.desde = t.getLong("desde");
        ListTag l = t.getList("columnas", Tag.TAG_INT_ARRAY);
        for (int i = 0; i < l.size(); i++) {
            int[] q = l.getIntArray(i);
            if (q.length == 3) c.columnas.add(q);
        }
        return c;
    }

    @Override
    public CompoundTag save(CompoundTag t) {
        t.putBoolean("activo", activo);
        t.putBoolean("vencida", vencida);
        t.putLong("desde", desde);
        ListTag l = new ListTag();
        for (int[] q : columnas) l.add(new IntArrayTag(q));
        t.put("columnas", l);
        return t;
    }

    /** Lo que se manda a los clientes. */
    public Net.CieloRojo paquete() {
        int[] c = new int[columnas.size() * 3];
        for (int i = 0; i < columnas.size(); i++) System.arraycopy(columnas.get(i), 0, c, i * 3, 3);
        return new Net.CieloRojo(activo, desde, c);
    }

    /**
     * Cae el rayo: se enciende el cielo rojo (si no lo estaba y no se vencio ya a la Sombra) y sube la columna de ese
     * portal; se avisa a todos. Devuelve si cambio algo.
     */
    public static boolean encender(MinecraftServer srv, int[] columna) {
        CieloRojo c = get(srv);
        boolean cambia = false;
        if (columna != null && c.columnas.stream().noneMatch(q -> q[0] == columna[0] && q[1] == columna[1] && q[2] == columna[2])) {
            c.columnas.add(columna);
            cambia = true;
        }
        if (!c.activo && !c.vencida) {
            c.activo = true;
            c.desde = srv.overworld().getGameTime();
            cambia = true;
        }
        if (cambia) {
            c.setDirty();
            enviarATodos(srv);
        }
        return cambia;
    }

    /** Vencida la Sombra (parte 7) o el comando de reiniciar ({@code vencida} false): el cielo vuelve a ser el de siempre. */
    public static void apagar(MinecraftServer srv, boolean vencida) {
        CieloRojo c = get(srv);
        c.activo = false;
        c.vencida = vencida;
        c.columnas.clear();
        c.setDirty();
        enviarATodos(srv);
    }

    public static boolean activo(MinecraftServer srv) {
        return get(srv).activo;
    }

    public static void enviarATodos(MinecraftServer srv) {
        Net.CieloRojo pk = get(srv).paquete();
        for (ServerPlayer p : srv.getPlayerList().getPlayers()) Net.to(p, pk);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) Net.to(p, get(p.server).paquete());
    }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        // (solo en el mundo de siempre: en otra dimension, el cielo de alli)
        CieloRojo c = get(p.server);
        Net.to(p, e.getTo() == Level.OVERWORLD ? c.paquete() : new Net.CieloRojo(false, 0, new int[0]));
    }

    /** Para las pruebas y el comando. */
    public static String resumen(MinecraftServer srv) {
        CieloRojo c = get(srv);
        StringBuilder b = new StringBuilder("cielo rojo " + (c.activo ? "SI" : "no") + (c.vencida ? " (Sombra vencida)" : "") + ", desde el tick "
                + c.desde + ", columnas " + c.columnas.size());
        for (int[] q : c.columnas) b.append(" [").append(q[0]).append(',').append(q[1]).append(',').append(q[2]).append(']');
        return b.toString();
    }
}
