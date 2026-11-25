package com.darkcraft.client.ui;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.story.Story;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/**
 * Lo que sale en los Logros del menu (R): la coleccion de objetos del mod que se han tenido y el bestiario (las criaturas
 * vencidas, dibujadas en 3D). Las claves son las que apunta el servidor ({@code espejo.Espejo}).
 */
public final class Coleccion {
    private Coleccion() {}

    public static final String[] COLECCION = {"mapa_valdrenn", "libro1", "libro2", "espada_diestra", "espada_siniestra", "libro3", "libro4",
            "llave_arbol", "mandoble_juramento", "llave_hueso", "libro5", "alabarda", "libro_acertijo", "amuleto_hueso", "libro6"};
    public static final String[] BESTIARIO = {"escudero", "caballero_espada", "caballero_alabarda", "caballero_ballesta", "rey", "gavran"};

    /** El objeto de una clave de la coleccion (los libros de la cronica, con su texto). */
    public static ItemStack objeto(String clave) {
        if (clave.startsWith("libro") && clave.length() > 5 && Character.isDigit(clave.charAt(5))) {
            try {
                return Story.book(Integer.parseInt(clave.substring(5)));
            } catch (NumberFormatException e) {
                return new ItemStack(Items.WRITTEN_BOOK);
            }
        }
        Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation(DarkCraftMod.MODID, clave));
        return i == null || i == Items.AIR ? new ItemStack(Items.BARRIER) : new ItemStack(i);
    }

    /** El icono de un logro (el id de un objeto). */
    public static ItemStack icono(String id) {
        Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        return i == null || i == Items.AIR ? new ItemStack(Items.BOOK) : new ItemStack(i);
    }

    public static String nombreBestia(String c) {
        return switch (c) {
            case "rey" -> "entity.darkcraft.rey";
            case "gavran" -> "entity.darkcraft.gavran";
            case "escudero" -> "darkcraft.gavran.escudero";
            default -> "entity.darkcraft." + c;
        };
    }

    private static final Map<String, LivingEntity> CRIATURAS = new HashMap<>();
    private static Level nivel;

    /** La criatura del bestiario para dibujarla (una por clave y por mundo). */
    public static LivingEntity criatura(String clave) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;
        if (nivel != mc.level) {
            CRIATURAS.clear();
            nivel = mc.level;
        }
        return CRIATURAS.computeIfAbsent(clave, c -> {
            Level lvl = mc.level;
            return switch (c) {
                case "rey" -> DarkCraftMod.REY.get().create(lvl);
                case "gavran" -> DarkCraftMod.GAVRAN.get().create(lvl);
                case "caballero_espada", "caballero_alabarda", "caballero_ballesta" -> {
                    var k = c.equals("caballero_espada") ? DarkCraftMod.CABALLERO_ESPADA.get().create(lvl)
                            : c.equals("caballero_alabarda") ? DarkCraftMod.CABALLERO_ALABARDA.get().create(lvl) : DarkCraftMod.CABALLERO_BALLESTA.get().create(lvl);
                    if (k != null) {
                        k.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(c.equals("caballero_espada") ? Items.IRON_SWORD
                                : c.equals("caballero_alabarda") ? DarkCraftMod.ALABARDA.get() : Items.CROSSBOW));
                        if (c.equals("caballero_espada")) k.setItemSlot(EquipmentSlot.OFFHAND, com.darkcraft.caballeros.Caballeros.escudo());
                    }
                    yield k;
                }
                default -> {
                    var s = EntityType.SKELETON.create(lvl);
                    if (s != null) {
                        s.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                        s.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
                        ItemStack casco = new ItemStack(Items.LEATHER_HELMET);
                        ((DyeableLeatherItem) Items.LEATHER_HELMET).setColor(casco, 0x2E4A2C);
                        s.setItemSlot(EquipmentSlot.HEAD, casco);
                    }
                    yield s;
                }
            };
        });
    }
}
