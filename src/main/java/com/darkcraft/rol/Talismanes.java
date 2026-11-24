package com.darkcraft.rol;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.net.Net;
import com.darkcraft.story.Logros;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Los talismanes que lleva cada jugador: van en su ficha (no en el inventario), en hasta cuatro huecos. El primero esta
 * abierto desde el principio y se abre otro por cada gran jefe vencido (el Rey, Ser Gavran...). Se ponen y se quitan
 * desde el menu ({@link Net.TalismanAccion}); lo que dan lo miran los que lo usan con {@link #lleva}.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Talismanes {
    private Talismanes() {}

    public static final int MAX = 4;
    /** Los jefes que abren un hueco cada uno (sus logros). */
    private static final String[] JEFES = {"rey", "gavran"};

    /** Lo que lleva cada jugador conectado (para no leer la ficha en cada golpe). */
    private static final Map<UUID, ItemStack[]> LLEVA = new HashMap<>();

    /** Los huecos abiertos del jugador. */
    public static int huecos(ServerPlayer sp) {
        int n = 1;
        for (String j : JEFES) if (Logros.tiene(sp, j)) n++;
        return Math.min(MAX, n);
    }

    /** Los talismanes que lleva (los cuatro huecos; vacios si no hay). */
    public static ItemStack[] llevados(Player p) {
        return LLEVA.computeIfAbsent(p.getUUID(), k -> leer(p));
    }

    private static ItemStack[] leer(Player p) {
        ItemStack[] r = new ItemStack[MAX];
        java.util.Arrays.fill(r, ItemStack.EMPTY);
        ListTag l = Ficha.leer(p).getList("talismanes", Tag.TAG_COMPOUND);
        for (int i = 0; i < l.size(); i++) {
            CompoundTag t = l.getCompound(i);
            int h = t.getInt("hueco");
            if (h >= 0 && h < MAX) r[h] = ItemStack.of(t.getCompound("item"));
        }
        return r;
    }

    private static void guardar(Player p, ItemStack[] r) {
        ListTag l = new ListTag();
        for (int h = 0; h < MAX; h++) {
            if (r[h].isEmpty()) continue;
            CompoundTag t = new CompoundTag();
            t.putInt("hueco", h);
            t.put("item", r[h].save(new CompoundTag()));
            l.add(t);
        }
        Ficha.abrir(p).put("talismanes", l);
        LLEVA.put(p.getUUID(), r);
    }

    /** Si lleva puesto un talisman de esa clase (en un hueco abierto). */
    public static boolean lleva(Player p, Talisman.Clase c) {
        return lleva(llevados(p), c);
    }

    public static boolean lleva(ItemStack[] lista, Talisman.Clase c) {
        if (lista == null) return false;
        for (ItemStack st : lista) if (Talisman.de(st) == c) return true;
        return false;
    }

    /** Los nombres de los objetos que lleva, para el cliente ("" = hueco vacio). */
    public static String[] nombres(Player p) {
        ItemStack[] r = llevados(p);
        String[] n = new String[MAX];
        for (int i = 0; i < MAX; i++)
            n[i] = r[i].isEmpty() ? "" : String.valueOf(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(r[i].getItem()));
        return n;
    }

    // ------------------------------------------------------------------ poner y quitar (lo pide el menu)

    /** Pone el talisman del hueco {@code desde} del inventario en el hueco de talisman {@code hueco} (cambiandolo por el que hubiera). */
    public static void poner(ServerPlayer sp, int hueco, int desde) {
        if (hueco < 0 || hueco >= huecos(sp)) return;
        Inventory inv = sp.getInventory();
        if (desde < 0 || desde >= inv.getContainerSize()) return;
        ItemStack st = inv.getItem(desde);
        Talisman.Clase c = Talisman.de(st);
        if (c == null) return;
        ItemStack[] r = llevados(sp).clone();
        // dos iguales no
        for (int h = 0; h < MAX; h++)
            if (h != hueco && Talisman.de(r[h]) == c) {
                Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.talisman.repetido", 0));
                return;
            }
        ItemStack sale = r[hueco];
        r[hueco] = st.split(1);
        inv.setItem(desde, st.isEmpty() ? sale : st);
        if (!st.isEmpty() && !sale.isEmpty() && !inv.add(sale)) sp.drop(sale, false);
        cambiado(sp, r);
    }

    /** Quita el talisman del hueco y lo devuelve a la bolsa (si no cabe, no se quita). */
    public static void quitar(ServerPlayer sp, int hueco) {
        if (hueco < 0 || hueco >= MAX) return;
        ItemStack[] r = llevados(sp).clone();
        if (r[hueco].isEmpty()) return;
        int libre = Equipo.libre(sp);
        if (libre < 0) {
            Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.menu.bolsa_llena", 0));
            return;
        }
        sp.getInventory().setItem(libre, r[hueco]);
        r[hueco] = ItemStack.EMPTY;
        cambiado(sp, r);
    }

    private static void cambiado(ServerPlayer sp, ItemStack[] r) {
        guardar(sp, r);
        Rol.aplicar(sp);
        sp.level().playSound(null, sp.getX(), sp.getY() + 1, sp.getZ(), DarkCraftMod.SFX_TALISMAN.get(), SoundSource.PLAYERS, 0.8f, 1.0f);
        sp.inventoryMenu.broadcastChanges();
    }

    /** Para el comando y la prueba: pone un talisman en un hueco sin pasar por el inventario. */
    public static void ponerDirecto(ServerPlayer sp, int hueco, ItemStack st) {
        if (hueco < 0 || hueco >= MAX) return;
        ItemStack[] r = llevados(sp).clone();
        r[hueco] = st;
        cambiado(sp, r);
    }

    @SubscribeEvent
    public static void onSalir(PlayerEvent.PlayerLoggedOutEvent e) {
        LLEVA.remove(e.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onEntrar(PlayerEvent.PlayerLoggedInEvent e) {
        LLEVA.remove(e.getEntity().getUUID());
    }

    /** Al reaparecer el jugador es otra entidad: la ficha se copia, la cache se lee de nuevo. */
    @SubscribeEvent
    public static void onClon(PlayerEvent.Clone e) {
        LLEVA.remove(e.getEntity().getUUID());
    }

    /** Para la prueba de la CI. */
    public static String resumen(ServerPlayer sp) {
        StringBuilder b = new StringBuilder("huecos=" + huecos(sp) + " [");
        ItemStack[] r = llevados(sp);
        for (int i = 0; i < MAX; i++) b.append(i > 0 ? ", " : "").append(r[i].isEmpty() ? "-" : Talisman.de(r[i]));
        return b.append("]").toString();
    }
}
