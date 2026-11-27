package com.darkcraft.rol;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.hechizos.Hechizo;
import com.darkcraft.net.Net;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Las diez clases con las que se empieza (parte 2f-3, como en Elden Ring): sus atributos (el nivel sale de ellos: la
 * suma - 79), lo que llevan puesto y en las manos, los hechizos memorizados y como se reparten los frascos. Al entrar por
 * primera vez en un mundo se elige en {@code ClaseScreen}; se guarda en la ficha ({@link Ficha#clase}). Los jugadores que
 * ya tenian partida no la eligen (se quedan sin clase).
 */
public enum Clase {
    CABALLERO(new int[]{15, 10, 11, 14, 13, 9, 9, 7}, 3, 1),
    VAGABUNDO(new int[]{12, 11, 13, 12, 15, 9, 8, 8}, 3, 1),
    BARBARO(new int[]{14, 9, 12, 16, 9, 7, 8, 11}, 3, 1),
    GUERRERO(new int[]{11, 12, 11, 10, 16, 10, 8, 9}, 3, 1),
    BANDIDO(new int[]{10, 11, 10, 9, 13, 9, 8, 14}, 3, 1),
    HECHICERO(new int[]{9, 15, 9, 8, 12, 16, 7, 9}, 2, 2),
    PROFETA(new int[]{10, 14, 8, 11, 10, 7, 16, 10}, 2, 2),
    CONFESOR(new int[]{10, 13, 10, 12, 12, 9, 14, 9}, 3, 1),
    PRISIONERO(new int[]{11, 12, 11, 11, 14, 14, 6, 9}, 3, 1),
    DESDICHADO(new int[]{10, 10, 10, 10, 10, 10, 10, 10}, 3, 1);

    /** "caballero", "vagabundo"... (comandos, textos: darkcraft.clase.<id> y .desc). */
    public final String id = name().toLowerCase(Locale.ROOT);
    private final int[] atributos;
    public final int frascosVida, frascosMana;

    Clase(int[] atributos, int frascosVida, int frascosMana) {
        this.atributos = atributos;
        this.frascosVida = frascosVida;
        this.frascosMana = frascosMana;
    }

    public int[] atributos() {
        return atributos.clone();
    }

    public int nivel() {
        return Nivel.de(atributos);
    }

    public String clave() {
        return "darkcraft.clase." + id;
    }

    public static Clase de(String id) {
        if (id == null) return null;
        for (Clase c : values()) if (c.id.equals(id)) return c;
        return null;
    }

    // ------------------------------------------------------------------ lo que lleva cada una

    /**
     * Lo que se lleva al empezar: las tres armas de la mano derecha (barra 0-2), la mano izquierda, la armadura (cabeza,
     * pecho, piernas, pies), lo que va a la bolsa y los hechizos que se saben de memoria.
     */
    public record Kit(ItemStack[] derecha, ItemStack izquierda, ItemStack[] armadura, List<ItemStack> bolsa, List<Hechizo> hechizos) {
        /** Todo lo que se ve (para la pantalla): armas, armadura y lo de la bolsa. */
        public List<ItemStack> todo() {
            List<ItemStack> l = new ArrayList<>();
            for (ItemStack s : derecha) if (!s.isEmpty()) l.add(s);
            if (!izquierda.isEmpty()) l.add(izquierda);
            for (ItemStack s : armadura) if (!s.isEmpty()) l.add(s);
            for (ItemStack s : bolsa) if (!s.isEmpty()) l.add(s);
            return l;
        }
    }

    private static ItemStack st(Item i) {
        return new ItemStack(i);
    }

    private static ItemStack st(Item i, int n) {
        return new ItemStack(i, n);
    }

    /**
     * Un conjunto de armadura con modelo propio (parte 7ai) en el orden del kit: cabeza, pecho, piernas y pies (los guantes
     * van en el hueco de los pies).
     */
    private static ItemStack[] conjunto(com.darkcraft.armadura.Conjunto c) {
        return new ItemStack[]{DarkCraftMod.armadura(c, 0), DarkCraftMod.armadura(c, 1), DarkCraftMod.armadura(c, 3),
                DarkCraftMod.armadura(c, 2)};
    }

    private static ItemStack[] nada() {
        return new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    }

    private static ItemStack hechizo(Hechizo h) {
        return new ItemStack(DarkCraftMod.HECHIZOS.get(h).get());
    }

    public Kit kit() {
        ItemStack vacio = ItemStack.EMPTY;
        return switch (this) {
            case CABALLERO -> new Kit(new ItemStack[]{st(Items.IRON_SWORD), vacio, vacio}, st(Items.SHIELD),
                    conjunto(com.darkcraft.armadura.Conjunto.CABALLERO), List.of(), List.of());
            case VAGABUNDO -> new Kit(new ItemStack[]{st(Items.IRON_SWORD), st(Items.BOW), vacio}, vacio,
                    conjunto(com.darkcraft.armadura.Conjunto.VAGABUNDO), List.of(st(Items.ARROW, 32)), List.of());
            case BARBARO -> new Kit(new ItemStack[]{st(Items.IRON_AXE), vacio, vacio}, st(Items.SHIELD),
                    conjunto(com.darkcraft.armadura.Conjunto.BARBARO), List.of(), List.of());
            case GUERRERO -> new Kit(new ItemStack[]{st(Items.IRON_SWORD), vacio, vacio}, st(Items.IRON_SWORD),
                    conjunto(com.darkcraft.armadura.Conjunto.GUERRERO), List.of(), List.of());
            case BANDIDO -> new Kit(new ItemStack[]{st(DarkCraftMod.DAGA.get()), st(Items.BOW), vacio}, vacio,
                    conjunto(com.darkcraft.armadura.Conjunto.BANDIDO), List.of(st(Items.ARROW, 24)), List.of());
            case HECHICERO -> new Kit(new ItemStack[]{st(DarkCraftMod.BASTON.get()), st(Items.STONE_SWORD), vacio}, vacio,
                    conjunto(com.darkcraft.armadura.Conjunto.HECHICERO),
                    List.of(hechizo(Hechizo.GUIJARRO), hechizo(Hechizo.ESPADA), hechizo(Hechizo.ESQUIRLAS)),
                    List.of(Hechizo.GUIJARRO, Hechizo.ESQUIRLAS, Hechizo.ESPADA));
            case PROFETA -> new Kit(new ItemStack[]{st(DarkCraftMod.SELLO_SAGRADO.get()), st(Items.TRIDENT), vacio}, vacio,
                    conjunto(com.darkcraft.armadura.Conjunto.PROFETA),
                    List.of(hechizo(Hechizo.CURAR), hechizo(Hechizo.LLAMA), hechizo(Hechizo.BENDICION)),
                    List.of(Hechizo.CURAR, Hechizo.LLAMA, Hechizo.BENDICION));
            case CONFESOR -> new Kit(new ItemStack[]{st(Items.IRON_SWORD), st(DarkCraftMod.SELLO_SAGRADO.get()), vacio}, st(Items.SHIELD),
                    conjunto(com.darkcraft.armadura.Conjunto.CONFESOR),
                    List.of(hechizo(Hechizo.CURAR), hechizo(Hechizo.BENDICION)), List.of(Hechizo.CURAR, Hechizo.BENDICION));
            case PRISIONERO -> new Kit(new ItemStack[]{st(Items.IRON_SWORD), st(DarkCraftMod.BASTON.get()), vacio}, vacio,
                    conjunto(com.darkcraft.armadura.Conjunto.PRISIONERO),
                    List.of(hechizo(Hechizo.GUIJARRO), hechizo(Hechizo.ESPADA)), List.of(Hechizo.GUIJARRO, Hechizo.ESPADA));
            case DESDICHADO -> new Kit(new ItemStack[]{st(Items.WOODEN_SWORD), vacio, vacio}, vacio, nada(), List.of(), List.of());
        };
    }

    // ------------------------------------------------------------------ elegirla (en el servidor)

    /** Si el jugador ha de elegir clase (aun no tiene: ni elegida ni "sin clase"). */
    public static boolean pendiente(ServerPlayer sp) {
        return Ficha.clase(sp).isEmpty();
    }

    /**
     * Empieza como esa clase: sus atributos, lo que lleva (la bolsa se vacia), sus hechizos, sus frascos, vida y mana
     * llenos. Solo si aun no tenia clase (o con {@code forzar}, para el comando).
     */
    public static boolean elegir(ServerPlayer sp, Clase c, boolean forzar) {
        if (c == null || (!forzar && !pendiente(sp))) return false;
        Ficha.atributos(sp, c.atributos());
        var inv = sp.getInventory();
        inv.clearContent();
        Kit k = c.kit();
        for (int i = 0; i < 3; i++) inv.setItem(i, k.derecha()[i].copy());
        inv.selected = 0;
        sp.setItemSlot(EquipmentSlot.OFFHAND, k.izquierda().copy());
        EquipmentSlot[] piezas = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < 4; i++) sp.setItemSlot(piezas[i], k.armadura()[i].copy());
        int hueco = 9;
        for (ItemStack s : k.bolsa()) if (hueco < 36) inv.setItem(hueco++, s.copy());
        inv.setChanged();
        Memoria.forzar(sp, new ArrayList<>(k.hechizos()));
        Ficha.Frascos f = Ficha.frascos(sp);
        Ficha.frascos(sp, new Ficha.Frascos(c.frascosVida, c.frascosMana, c.frascosVida, c.frascosMana, f.mejora()));
        Ficha.clase(sp, c.id);
        Rol.aplicar(sp);
        sp.setHealth(sp.getMaxHealth());
        com.darkcraft.armas.Combos.llenar(sp);
        Rol.fichaCambiada(sp);
        com.darkcraft.story.Logros.dar(sp, "clase");
        DarkCraftMod.LOGGER.info("{} empieza como {}", sp.getGameProfile().getName(), c.id);
        return true;
    }

    /**
     * Al entrar: quien nunca ha jugado en este mundo elige clase; quien ya tenia partida (nivel, ecos o un rato jugado)
     * se queda sin clase y no se le pregunta.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onEntrar(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp) || !pendiente(sp)) return;
        int jugado = sp.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
        boolean nuevo = jugado < 20 * 60 && Ficha.nivel(sp) <= 1 && Ficha.ecos(sp) == 0;
        if (!nuevo || sp.isCreative() || sp.isSpectator()) {
            Ficha.clase(sp, SIN_CLASE);
            return;
        }
        Net.to(sp, new Net.AbrirClase(true));
    }

    /** Lo que se apunta a quien ya tenia partida cuando llegaron las clases. */
    public static final String SIN_CLASE = "-";

    /** Quien puede volver a elegir aunque ya tenga clase (lo ha pedido con el comando "elegir"). */
    private static final java.util.Set<java.util.UUID> OTRA_VEZ = new java.util.HashSet<>();

    /** El cliente elige (Net.ElegirClase). */
    public static void pedir(ServerPlayer sp, String id) {
        boolean forzar = OTRA_VEZ.remove(sp.getUUID());
        if (!elegir(sp, de(id), forzar)) {
            DarkCraftMod.LOGGER.info("{} no puede elegir la clase {}", sp.getGameProfile().getName(), id);
            return;
        }
        // la primera vez (no al volver a elegir con el comando): la Sombra le habla y despierta en el muelle
        if (!forzar) com.darkcraft.cine.Intro.empezar(sp);
    }

    // ------------------------------------------------------------------ comandos: /darkcraft rol clase ...

    public static LiteralArgumentBuilder<CommandSourceStack> comando() {
        var c = Commands.literal("clase").executes(x -> {
            ServerPlayer p = x.getSource().getPlayerOrException();
            String id = Ficha.clase(p);
            x.getSource().sendSuccess(() -> id.isEmpty() || id.equals(SIN_CLASE) ? Component.literal("sin clase")
                    : Component.translatable("darkcraft.clase." + id), false);
            return 1;
        });
        c.then(Commands.literal("elegir").executes(x -> {
            ServerPlayer p = x.getSource().getPlayerOrException();
            OTRA_VEZ.add(p.getUUID());
            Net.to(p, new Net.AbrirClase(false));
            return 1;
        }));
        c.then(Commands.literal("olvidar").executes(x -> {
            Ficha.clase(x.getSource().getPlayerOrException(), "");
            return 1;
        }));
        c.then(Commands.literal("ninguna").executes(x -> {
            Ficha.clase(x.getSource().getPlayerOrException(), SIN_CLASE);
            return 1;
        }));
        for (Clase k : values())
            c.then(Commands.literal(k.id).executes(x -> elegir(x.getSource().getPlayerOrException(), k, true) ? 1 : 0));
        return c;
    }
}
