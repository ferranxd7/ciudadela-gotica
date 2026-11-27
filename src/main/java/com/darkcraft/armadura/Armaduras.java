package com.darkcraft.armadura;

import com.darkcraft.DarkCraftMod;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Locale;

/**
 * Lo de las armaduras con modelo propio que no es de una pieza (parte 7ai): ponerse un conjunto entero, quitarse la armadura
 * y el comando {@code /darkcraft armadura}.
 */
public final class Armaduras {
    private Armaduras() {}

    /** El hueco de Minecraft de cada pieza (0 yelmo, 1 peto, 2 guantes en los pies, 3 grebas). */
    public static final EquipmentSlot[] HUECOS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.FEET, EquipmentSlot.LEGS};

    /** Pone el conjunto entero (lo que se llevaba va a la bolsa). */
    public static void vestir(ServerPlayer sp, Conjunto c) {
        for (int i = 0; i < 4; i++) {
            ItemStack antes = sp.getItemBySlot(HUECOS[i]);
            sp.setItemSlot(HUECOS[i], DarkCraftMod.armadura(c, i));
            if (!antes.isEmpty()) com.darkcraft.rol.Equipo.dar(sp, antes);
        }
        sp.getInventory().setChanged();
    }

    /** Quita la armadura entera (a la bolsa). */
    public static void quitar(ServerPlayer sp) {
        for (EquipmentSlot s : HUECOS) {
            ItemStack antes = sp.getItemBySlot(s);
            sp.setItemSlot(s, ItemStack.EMPTY);
            if (!antes.isEmpty()) com.darkcraft.rol.Equipo.dar(sp, antes);
        }
        sp.getInventory().setChanged();
    }

    /** El conjunto que lleva entero el jugador, o null si no lleva las cuatro piezas de uno. */
    public static Conjunto entero(Player p) {
        Conjunto c = null;
        for (int i = 0; i < 4; i++) {
            PiezaArmadura pa = PiezaArmadura.de(p.getItemBySlot(HUECOS[i]));
            if (pa == null || pa.pieza != i || (c != null && pa.conjunto != c)) return null;
            c = pa.conjunto;
        }
        return c;
    }

    /** Lo que lleva puesto, para los comandos y las pruebas: "yelmo caballero_yelmo, ... | defensa 17, peso 21,0". */
    public static String resumen(Player p) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            ItemStack st = p.getItemBySlot(HUECOS[i]);
            sb.append(i == 0 ? "" : ", ").append(Conjunto.PIEZAS[i]).append(' ')
                    .append(st.isEmpty() ? "-" : net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(st.getItem()).getPath());
        }
        float peso = 0f;
        for (EquipmentSlot s : HUECOS) peso += com.darkcraft.combate.Peso.item(p.getItemBySlot(s));
        sb.append(String.format(Locale.ROOT, " | defensa %d, peso %.1f", p.getArmorValue(), peso));
        Conjunto c = entero(p);
        if (c != null) sb.append(", conjunto ").append(c.id);
        return sb.toString();
    }

    private static int decir(CommandSourceStack src, String s) {
        src.sendSuccess(() -> Component.literal(s), false);
        return 1;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> comando() {
        return Commands.literal("armadura")
                .requires(s -> s.hasPermission(2))
                .executes(c -> {
                    StringBuilder sb = new StringBuilder("Conjuntos:");
                    for (Conjunto k : Conjunto.values())
                        sb.append(String.format(Locale.ROOT, " %s (defensa %d, peso %.1f)", k.id, k.defensaTotal(), k.peso));
                    ServerPlayer sp = c.getSource().getPlayer();
                    if (sp != null) sb.append(" | puesto: ").append(resumen(sp));
                    return decir(c.getSource(), sb.toString());
                })
                .then(Commands.literal("quitar").executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    quitar(sp);
                    return decir(c.getSource(), resumen(sp));
                }))
                .then(Commands.argument("conjunto", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(Conjunto.values()).map(k -> k.id), b))
                        .executes(c -> {
                            ServerPlayer sp = c.getSource().getPlayerOrException();
                            Conjunto k = Conjunto.de(StringArgumentType.getString(c, "conjunto"));
                            if (k == null) return decir(c.getSource(), "No hay ese conjunto");
                            for (int i = 0; i < 4; i++) com.darkcraft.rol.Equipo.dar(sp, DarkCraftMod.armadura(k, i));
                            return decir(c.getSource(), "Dado el conjunto " + k.id);
                        })
                        .then(Commands.literal("poner").executes(c -> {
                            ServerPlayer sp = c.getSource().getPlayerOrException();
                            Conjunto k = Conjunto.de(StringArgumentType.getString(c, "conjunto"));
                            if (k == null) return decir(c.getSource(), "No hay ese conjunto");
                            vestir(sp, k);
                            return decir(c.getSource(), resumen(sp));
                        })));
    }
}
