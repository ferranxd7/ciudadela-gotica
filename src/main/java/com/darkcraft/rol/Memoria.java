package com.darkcraft.rol;

import com.darkcraft.hechizos.Hechizo;
import com.darkcraft.hechizos.HechizoItem;
import com.darkcraft.net.Net;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Los hechizos que tiene memorizados cada jugador (en su ficha, NBT {@code memoria}): se cambian descansando en un
 * santuario, con los hechizos que lleve en el inventario, y caben los que dejen sus huecos de memoria (segun la Mente,
 * {@link Hechizo#memoria}). Solo los memorizados se pueden lanzar.
 */
public final class Memoria {
    private Memoria() {}

    public static List<Hechizo> memorizados(Player p) {
        List<Hechizo> r = new ArrayList<>();
        ListTag l = Ficha.leer(p).getList("memoria", Tag.TAG_STRING);
        for (int i = 0; i < l.size(); i++) {
            Hechizo h = Hechizo.de(l.getString(i));
            if (h != null && !r.contains(h)) r.add(h);
        }
        return r;
    }

    public static String[] nombres(Player p) {
        return memorizados(p).stream().map(h -> h.id).toArray(String[]::new);
    }

    /** Los huecos de memoria del jugador (segun su Mente). */
    public static int huecos(Player p) {
        return Hechizo.memoria(Ficha.atributos(p)[Atributo.MENTE.ordinal()]);
    }

    public static int usados(List<Hechizo> l) {
        int n = 0;
        for (Hechizo h : l) n += h.huecos;
        return n;
    }

    /** Si lleva ese hechizo en el inventario (su pergamino o su plegaria). */
    public static boolean tiene(Player p, Hechizo h) {
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) if (HechizoItem.de(inv.getItem(i)) == h) return true;
        return false;
    }

    /**
     * Deja memorizados esos hechizos (lo pide el menu del santuario): tiene que estar descansando junto a un santuario
     * (o en creativo), llevarlos todos y que quepan. Si algo no vale, no se cambia nada.
     */
    public static boolean memorizar(ServerPlayer sp, String[] ids) {
        // si no vale, se le vuelve a mandar la ficha: el menu ya lo habia apuntado en el cliente
        if (!sp.isCreative() && !com.darkcraft.santuario.Santuarios.junto(sp)) return rechazar(sp, null);
        List<Hechizo> l = new ArrayList<>();
        for (String id : ids) {
            Hechizo h = Hechizo.de(id);
            if (h == null || l.contains(h) || !tiene(sp, h)) return rechazar(sp, null);
            l.add(h);
        }
        if (usados(l) > huecos(sp)) return rechazar(sp, "darkcraft.hechizo.sin_memoria");
        guardar(sp, l);
        Rol.fichaCambiada(sp);
        return true;
    }

    private static boolean rechazar(ServerPlayer sp, String aviso) {
        if (aviso != null) Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, aviso, 0));
        Rol.fichaCambiada(sp);
        return false;
    }

    static void guardar(Player p, List<Hechizo> l) {
        ListTag t = new ListTag();
        for (Hechizo h : l) t.add(StringTag.valueOf(h.id));
        Ficha.abrir(p).put("memoria", t);
    }

    /** Para el comando y la prueba: memoriza sin mirar nada. */
    public static void forzar(ServerPlayer sp, List<Hechizo> l) {
        guardar(sp, l);
        Rol.fichaCambiada(sp);
    }
}
