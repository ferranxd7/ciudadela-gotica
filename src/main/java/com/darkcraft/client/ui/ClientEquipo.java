package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.client.ClientCine;
import com.darkcraft.net.Net;
import com.darkcraft.rol.Equipo;
import com.darkcraft.rol.Frascos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * El equipo en el cliente (ver {@code rol.Equipo}): en supervivencia la barra de Minecraft no se ve ni se maneja como
 * antes. La rueda y las teclas 1-3 cambian el arma de la mano derecha (solo entre sus tres huecos); las teclas 4 y 5 (o
 * la tecla de la mano izquierda) cambian lo de la izquierda; los objetos rapidos se eligen con su tecla (o con 6-9) y se
 * usan con otra sin soltar el arma; la tecla de tirar ya no tira lo que se lleva en la mano; y la tecla del inventario
 * abre el menu del mod en vez de la pantalla de Minecraft. En creativo todo sigue como en Minecraft.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class ClientEquipo {
    private ClientEquipo() {}

    /** Lo que puede haber en el hueco de abajo de la cruz: un frasco o uno de los huecos de objetos rapidos. */
    public static final int FRASCO_VIDA = -1, FRASCO_MANA = -2, NADA = -9;

    /** El objeto rapido elegido (uno de los de arriba o un hueco 5..8). */
    private static int rapido = FRASCO_VIDA;
    /** Mientras se usa un objeto rapido: su hueco y el del arma a la que hay que volver. */
    private static int usando = -1, volver;
    /** Lo ultimo que se cambio en la cruz (0 derecha, 1 izquierda, 2 abajo) y cuando: sale su nombre un momento. */
    static int cambio = -1, cambioTick = -1000;

    /** Si manda el equipo del mod: en supervivencia y aventura (en creativo y espectador, la barra de siempre). */
    public static boolean activo() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.gameMode != null && mc.gameMode.canHurtPlayer() && !mc.player.isSpectator();
    }

    // ------------------------------------------------------------------ los objetos rapidos

    /** Lo que se puede elegir abajo: los frascos que se lleven y los huecos de objetos rapidos que tengan algo. */
    public static List<Integer> rapidos() {
        Minecraft mc = Minecraft.getInstance();
        List<Integer> l = new ArrayList<>();
        if (ClientRol.frascosVida > 0) l.add(FRASCO_VIDA);
        if (ClientRol.frascosMana > 0) l.add(FRASCO_MANA);
        if (mc.player != null)
            for (int h : Equipo.RAPIDOS) if (!mc.player.getInventory().getItem(h).isEmpty()) l.add(h);
        return l;
    }

    /** El objeto rapido elegido ahora (si el que estaba ya no existe, el primero que haya). */
    public static int rapido() {
        List<Integer> l = rapidos();
        if (l.isEmpty()) return NADA;
        if (!l.contains(rapido)) rapido = l.get(0);
        return rapido;
    }

    /** Para la prueba de la CI: deja elegido el objeto rapido del hueco {@code h} (si tiene algo). */
    public static void elegirRapido(int h) {
        if (rapidos().contains(h)) rapido = h;
    }

    private static void rapidoSiguiente() {
        List<Integer> l = rapidos();
        if (l.size() < 2) return;
        rapido = l.get((l.indexOf(rapido()) + 1) % l.size());
        cambiado(2);
    }

    private static void cambiado(int que) {
        cambio = que;
        cambioTick = ClientRol.ticks;
        Panel.mueve();
    }

    // ------------------------------------------------------------------ la mano derecha

    private static void derecha(Inventory inv, int paso) {
        for (int k = 1; k <= 3; k++) {
            int h = Math.floorMod(inv.selected + paso * k, 3);
            if (h != inv.selected && !inv.getItem(h).isEmpty()) {
                inv.selected = h;
                cambiado(0);
                return;
            }
        }
    }

    // ------------------------------------------------------------------ cada tick, antes de que el juego mire sus teclas

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        Options o = mc.options;
        // la tecla del menu
        boolean menu = false;
        while (Teclas.MENU.consumeClick()) menu = true;
        if (menu && mc.screen == null && !ClientCine.active() && mc.player.isAlive()) {
            MenuScreen.abrir(MenuScreen.EQUIPO);
            return;
        }
        if (!activo()) {
            soltar(mc);
            return;
        }
        Inventory inv = mc.player.getInventory();
        if (mc.screen != null || ClientCine.active() || !mc.player.isAlive()) {
            if (usando >= 0) soltar(mc);
            return;
        }
        // las teclas de la barra: 1-3 siguen eligiendo el arma de la derecha (lo hace el juego); 4 y 5, la izquierda; 6-9,
        // el objeto rapido
        for (int i = 3; i < 9; i++) {
            boolean pulsada = false;
            while (o.keyHotbarSlots[i].consumeClick()) pulsada = true;
            if (!pulsada) continue;
            if (i <= 4) {
                if (!inv.getItem(i).isEmpty() || !inv.getItem(Equipo.OTRA_MANO).isEmpty()) {
                    Net.CH.sendToServer(new Net.EquipoAccion(Net.EquipoAccion.IZQUIERDA, i, 0));
                    cambiado(1);
                }
            } else if (!inv.getItem(i).isEmpty()) {
                rapido = i;
                cambiado(2);
            }
        }
        for (int i = 0; i < 3; i++) if (o.keyHotbarSlots[i].isDown() && inv.selected != i && usando < 0) cambiado(0);
        // lo que se lleva en la mano no se tira con una tecla (se tira desde el menu: la Q fija el objetivo), y coger el
        // bloque que se mira no mete nada en las manos
        while (o.keyDrop.consumeClick()) {
        }
        // la rueda del raton (pulsarla) fija el objetivo, como la Q
        boolean fijar = false;
        while (o.keyPickItem.consumeClick()) fijar = true;
        if (fijar) com.darkcraft.client.combate.Fijar.alternar();
        while (Teclas.MANO_DERECHA.consumeClick()) if (usando < 0) derecha(inv, 1);
        // la tecla de cambiar de mano (F) ya no cambia el arma de mano: turna la izquierda, como su propia tecla
        boolean izquierda = false;
        while (o.keySwapOffhand.consumeClick()) izquierda = true;
        while (Teclas.MANO_IZQUIERDA.consumeClick()) izquierda = true;
        if (izquierda) {
            if (!inv.getItem(3).isEmpty() || !inv.getItem(4).isEmpty()) {
                Net.CH.sendToServer(new Net.EquipoAccion(Net.EquipoAccion.IZQUIERDA, -1, 0));
                cambiado(1);
            }
        }
        while (Teclas.OBJETO_SIGUIENTE.consumeClick()) rapidoSiguiente();
        while (Teclas.HECHIZO_SIGUIENTE.consumeClick()) ClientRol.siguienteHechizo();
        // usar el objeto rapido: un frasco se bebe; lo demas se pone en la mano mientras se mantiene la tecla y se usa como
        // lo usaria el juego (comer, beber, lanzar), y al soltar vuelve el arma
        boolean pulsada = false;
        while (Teclas.OBJETO_USAR.consumeClick()) pulsada = true;
        if (pulsada && usando < 0 && !Bebida.yo()) {
            int q = rapido();
            if (q == FRASCO_VIDA || q == FRASCO_MANA) ClientRol.pedirFrasco(q == FRASCO_MANA ? Frascos.MANA : Frascos.VIDA);
            else if (q >= 0) {
                usando = q;
                volver = inv.selected <= 2 ? inv.selected : 0;
                inv.selected = q;
            }
        }
        if (usando >= 0) {
            boolean sigue = Teclas.OBJETO_USAR.isDown() && !inv.getItem(usando).isEmpty() && inv.selected == usando;
            o.keyUse.setDown(sigue);
            if (!sigue && !mc.player.isUsingItem()) soltar(mc);
        } else if (inv.selected > 2) {
            // la mano derecha solo lleva uno de sus tres huecos
            int h = 0;
            for (int i = 0; i < 3; i++)
                if (!inv.getItem(i).isEmpty()) {
                    h = i;
                    break;
                }
            inv.selected = h;
        }
    }

    /** Deja de usar el objeto rapido y vuelve el arma a la mano. */
    private static void soltar(Minecraft mc) {
        if (usando < 0) return;
        mc.options.keyUse.setDown(false);
        if (mc.player != null && mc.player.getInventory().selected == usando) mc.player.getInventory().selected = volver;
        usando = -1;
    }

    /** La rueda cambia el arma de la mano derecha (no recorre la barra entera). */
    @SubscribeEvent
    public static void onRueda(InputEvent.MouseScrollingEvent e) {
        Minecraft mc = Minecraft.getInstance();
        if (!activo() || mc.screen != null || e.getScrollDelta() == 0) return;
        e.setCanceled(true);
        if (usando < 0 && !ClientCine.active()) derecha(mc.player.getInventory(), e.getScrollDelta() > 0 ? -1 : 1);
    }

    // ------------------------------------------------------------------ fuera la barra y el inventario de Minecraft

    @SubscribeEvent
    public static void onOverlay(RenderGuiOverlayEvent.Pre e) {
        if (!activo()) return;
        var o = e.getOverlay();
        if (o == VanillaGuiOverlay.HOTBAR.type() || o == VanillaGuiOverlay.ITEM_NAME.type()) e.setCanceled(true);
    }

    /** El chat, por encima de la cruz. */
    @SubscribeEvent
    public static void onChat(CustomizeGuiOverlayEvent.Chat e) {
        if (activo()) e.setPosY(e.getPosY() - 34);
    }

    /** En supervivencia, la tecla del inventario abre el menu del mod (por el inventario) en vez de la pantalla de Minecraft. */
    @SubscribeEvent
    public static void onPantalla(ScreenEvent.Opening e) {
        if (e.getNewScreen() instanceof InventoryScreen && activo()) {
            // en una cinematica, nada (como hacia ya con el inventario de siempre)
            if (ClientCine.active()) {
                e.setCanceled(true);
                return;
            }
            e.setNewScreen(new MenuScreen(MenuScreen.INVENTARIO));
            Panel.abre();
        }
    }

    // ------------------------------------------------------------------ las clases de objeto del inventario

    public enum Clase {
        TODO("todo", MenuTex.ICO_TODO), ARMAS("armas", MenuTex.ICO_ESPADA), ARMADURAS("armaduras", MenuTex.ICO_PECHO),
        TALISMANES("talismanes", MenuTex.ICO_TALISMAN), HECHIZOS("hechizos", MenuTex.ICO_HECHIZO), OBJETOS("objetos", MenuTex.ICO_OBJETO),
        MATERIALES("materiales", MenuTex.ICO_MATERIAL), CLAVE("clave", MenuTex.ICO_LLAVE);

        public final String clave;
        public final MenuTex.T icono;

        Clase(String clave, MenuTex.T icono) {
            this.clave = clave;
            this.icono = icono;
        }

        public static Clase de(ItemStack st) {
            Item it = st.getItem();
            if (it instanceof com.darkcraft.rol.Talisman) return TALISMANES;
            if (it instanceof com.darkcraft.hechizos.HechizoItem) return HECHIZOS;
            if (it instanceof com.darkcraft.hechizos.Catalizador) return ARMAS;
            if (it instanceof com.darkcraft.rol.Fruto || it == DarkCraftMod.CAMPANA_DIFUNTOS.get()) return OBJETOS;
            Equipable eq = Equipable.get(st);
            if (it instanceof ArmorItem || it instanceof ElytraItem || (eq != null && eq.getEquipmentSlot().isArmor() && !(it instanceof ShieldItem)))
                return ARMADURAS;
            if (it instanceof TieredItem || it instanceof ProjectileWeaponItem || it instanceof TridentItem || it instanceof ShieldItem
                    || it instanceof ArrowItem)
                return ARMAS;
            if (it == DarkCraftMod.LLAVE_HUESO.get() || it == DarkCraftMod.LLAVE_ARBOL.get() || it == DarkCraftMod.AMULETO_HUESO.get()
                    || it == DarkCraftMod.LIBRO_ACERTIJO.get() || it == DarkCraftMod.MAPA.get() || it == DarkCraftMod.SILBATO_CORCEL.get()
                    || it == DarkCraftMod.CAMPANA_PEREGRINO.get() || it == DarkCraftMod.BUFANDA_ROJA.get()
                    || it == DarkCraftMod.FAROL_YLVA.get() || it == DarkCraftMod.FAROL_BALLENERO.get() || it == DarkCraftMod.YESQUERO.get()
                    || it instanceof WrittenBookItem
                    || it instanceof MapItem || it == Items.WRITABLE_BOOK)
                return CLAVE;
            if (st.isEdible() || it instanceof PotionItem || it instanceof EnderpearlItem || it instanceof SnowballItem || it instanceof EggItem
                    || it instanceof ExperienceBottleItem || it instanceof MilkBucketItem || it instanceof FireworkRocketItem
                    || it == Items.TOTEM_OF_UNDYING)
                return OBJETOS;
            return MATERIALES;
        }
    }

    /** Para la prueba de la CI. */
    public static String resumen() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return "-";
        Inventory inv = mc.player.getInventory();
        return "activo=" + activo() + " mano=" + inv.selected + " rapido=" + rapido() + " de " + rapidos() + (usando >= 0 ? " USANDO " + usando : "");
    }
}
