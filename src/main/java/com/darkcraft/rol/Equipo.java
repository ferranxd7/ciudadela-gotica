package com.darkcraft.rol;

import com.darkcraft.DarkCraftMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * El equipo del jugador, como en Elden Ring, montado (de momento) sobre el inventario de Minecraft:
 * <ul>
 * <li><b>mano derecha</b>: los huecos 0, 1 y 2 de la barra (el que se lleva en la mano es uno de esos tres);</li>
 * <li><b>mano izquierda</b>: la otra mano y dos de repuesto (huecos 3 y 4), que se turnan;</li>
 * <li><b>objetos rapidos</b>: los huecos 5 a 8 (se usan con su tecla sin dejar el arma) y, ademas, los frascos;</li>
 * <li><b>armadura</b>: yelmo, pecho, piernas y botas;</li>
 * <li><b>la bolsa</b>: los 27 huecos de dentro. Lo que se recoge va a la bolsa, no a la mano.</li>
 * </ul>
 * La barra de Minecraft ya no se ve en supervivencia (en su sitio va la cruz del HUD); todo se cambia desde el menu del
 * mod. Aqui estan los numeros de cada hueco y lo que el servidor hace cuando el menu pide mover o tirar algo.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Equipo {
    private Equipo() {}

    public static final int[] DERECHA = {0, 1, 2};
    /** La otra mano (hueco 40) y sus dos repuestos. */
    public static final int OTRA_MANO = 40;
    public static final int[] IZQUIERDA = {OTRA_MANO, 3, 4};
    public static final int[] RAPIDOS = {5, 6, 7, 8};
    public static final int BOTAS = 36, PIERNAS = 37, PECHO = 38, YELMO = 39;
    public static final int[] ARMADURA = {YELMO, PECHO, PIERNAS, BOTAS};
    public static final int BOLSA_DESDE = 9, BOLSA_HASTA = 35, HUECOS = 41;

    /** La pieza de armadura que va en un hueco (o null si el hueco no es de armadura). */
    public static EquipmentSlot pieza(int hueco) {
        return switch (hueco) {
            case YELMO -> EquipmentSlot.HEAD;
            case PECHO -> EquipmentSlot.CHEST;
            case PIERNAS -> EquipmentSlot.LEGS;
            case BOTAS -> EquipmentSlot.FEET;
            default -> null;
        };
    }

    public static boolean enBolsa(int hueco) {
        return hueco >= BOLSA_DESDE && hueco <= BOLSA_HASTA;
    }

    /** Si ese objeto se puede poner en ese hueco (en los de armadura solo cabe su pieza; en los demas, cualquier cosa). */
    public static boolean cabe(Player p, int hueco, ItemStack st) {
        if (hueco < 0 || hueco >= HUECOS) return false;
        EquipmentSlot pieza = pieza(hueco);
        return pieza == null || st.isEmpty() || st.canEquip(pieza, p);
    }

    /** El primer hueco libre de la bolsa (o -1). */
    public static int libre(Player p) {
        Inventory inv = p.getInventory();
        for (int i = BOLSA_DESDE; i <= BOLSA_HASTA; i++) if (inv.getItem(i).isEmpty()) return i;
        return -1;
    }

    // ------------------------------------------------------------------ lo que pide el menu (en el servidor)

    /** Cambia entre si lo que hay en dos huecos (equipar, quitar, reordenar). */
    public static void mover(ServerPlayer sp, int a, int b) {
        if (a == b || !sp.isAlive() || sp.isSpectator() || Frascos.bebiendo(sp)) return;
        Inventory inv = sp.getInventory();
        if (a < 0 || a >= HUECOS || b < 0 || b >= HUECOS) return;
        ItemStack sa = inv.getItem(a), sb = inv.getItem(b);
        if (!cabe(sp, b, sa) || !cabe(sp, a, sb)) return;
        sp.stopUsingItem();
        inv.setItem(a, sb);
        inv.setItem(b, sa);
        inv.setChanged();
    }

    /** Quita lo de un hueco del equipo y lo deja en la bolsa (si cabe). */
    public static void guardar(ServerPlayer sp, int hueco) {
        if (enBolsa(hueco)) return;
        int libre = libre(sp);
        if (libre >= 0) mover(sp, hueco, libre);
    }

    /** Tira al suelo lo que hay en un hueco. */
    public static void tirar(ServerPlayer sp, int hueco) {
        if (hueco < 0 || hueco >= HUECOS || !sp.isAlive() || sp.isSpectator()) return;
        ItemStack st = sp.getInventory().removeItemNoUpdate(hueco);
        if (!st.isEmpty()) sp.drop(st, false, true);
    }

    /** La mano izquierda pasa al repuesto que se pide (hueco 3 o 4), o al siguiente que tenga algo si se pide -1. */
    public static void manoIzquierda(ServerPlayer sp, int repuesto) {
        Inventory inv = sp.getInventory();
        if (repuesto != 3 && repuesto != 4) {
            repuesto = !inv.getItem(3).isEmpty() ? 3 : !inv.getItem(4).isEmpty() ? 4 : -1;
            if (repuesto < 0) return;
            // con dos repuestos se turnan los tres: lo de la mano pasa al final de la cola
            if (repuesto == 3 && !inv.getItem(4).isEmpty()) {
                ItemStack mano = inv.getItem(OTRA_MANO), r3 = inv.getItem(3), r4 = inv.getItem(4);
                if (Frascos.bebiendo(sp) || !sp.isAlive()) return;
                sp.stopUsingItem();
                inv.setItem(OTRA_MANO, r3);
                inv.setItem(3, r4);
                inv.setItem(4, mano);
                inv.setChanged();
                return;
            }
        }
        mover(sp, OTRA_MANO, repuesto);
    }

    // ------------------------------------------------------------------ lo que se recoge va a la bolsa

    /**
     * Al recoger algo del suelo: primero se junta con lo que ya se lleva igual (este donde este) y lo demas va a un
     * hueco libre de la bolsa; asi nada de lo que se recoge se mete solo en la mano ni en los objetos rapidos. Con la
     * bolsa llena se deja que el juego lo ponga donde quepa.
     */
    @SubscribeEvent
    public static void onRecoger(EntityItemPickupEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp) || sp.isCreative()) return;
        ItemEntity ie = e.getItem();
        ItemStack st = ie.getItem();
        if (st.isEmpty()) return;
        Inventory inv = sp.getInventory();
        int antes = st.getCount();
        // juntar con montones iguales que no esten llenos
        if (st.isStackable())
            for (int i = 0; i < HUECOS && !st.isEmpty(); i++) {
                ItemStack hay = inv.getItem(i);
                if (hay.isEmpty() || !ItemStack.isSameItemSameTags(hay, st)) continue;
                int cabe = Math.min(hay.getMaxStackSize(), inv.getMaxStackSize()) - hay.getCount();
                if (cabe <= 0) continue;
                int n = Math.min(cabe, st.getCount());
                hay.grow(n);
                hay.setPopTime(5);
                st.shrink(n);
            }
        // lo que quede, a la bolsa
        while (!st.isEmpty()) {
            int libre = libre(sp);
            if (libre < 0) break;
            int n = Math.min(st.getCount(), st.getMaxStackSize());
            ItemStack parte = st.copy();
            parte.setCount(n);
            parte.setPopTime(5);
            inv.setItem(libre, parte);
            st.shrink(n);
        }
        if (st.getCount() == antes) return;          // no cabia nada: que lo intente el juego a su manera
        ItemStack cogido = ie.getItem().copy();
        cogido.setCount(antes - st.getCount());
        com.darkcraft.net.Net.to(sp, new com.darkcraft.net.Net.Conseguido(cogido));
        inv.setChanged();
        ie.setItem(st);
        // el juego da lo recogido por bueno (el sonido, la estadistica, y quita la entidad si se ha quedado vacia)
        e.setResult(Event.Result.ALLOW);
    }
}
