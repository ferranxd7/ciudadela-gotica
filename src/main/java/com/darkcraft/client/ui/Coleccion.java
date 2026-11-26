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
    public static final String[] BESTIARIO = {"hueco_horca", "hueco_hoz", "hueco_antorcha", "ahogado_sable", "ahogado_arpon", "capitan",
            "escudero", "caballero_espada", "caballero_alabarda", "caballero_ballesta", "arquero", "perro", "espectro_cortesano",
            "espectro_dama", "espectro_vigia", "capitan_elite", "brandt", "corteza", "rey", "gavran", "marchito_guadana", "marchito_farol",
            "marchito_campana", "molinera", "ahorcado_garras", "ahorcado_soga", "verdugo", "podrido", "podrido_fatuo", "podrido_hinchado", "barquero"};

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
            case "hueco_horca", "hueco_hoz", "hueco_antorcha", "ahogado_sable", "ahogado_arpon", "marchito_guadana", "marchito_farol",
                 "marchito_campana", "ahorcado_garras", "ahorcado_soga", "podrido_fatuo", "podrido_hinchado" -> "darkcraft.bestiario." + c;
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
                case "hueco_horca", "hueco_hoz", "hueco_antorcha" -> {
                    var h = DarkCraftMod.HUECO.get().create(lvl);
                    if (h != null) h.variante(c.endsWith("horca") ? 0 : c.endsWith("hoz") ? 1 : 2);
                    yield h;
                }
                case "ahogado_sable", "ahogado_arpon" -> {
                    var m = DarkCraftMod.AHOGADO.get().create(lvl);
                    if (m != null) m.variante(c.endsWith("arpon") ? 1 : 0);
                    yield m;
                }
                case "capitan" -> DarkCraftMod.CAPITAN.get().create(lvl);
                case "marchito_guadana", "marchito_farol", "marchito_campana" -> {
                    var w = DarkCraftMod.MARCHITO.get().create(lvl);
                    if (w != null) w.variante(c.endsWith("guadana") ? com.darkcraft.enemigos.Marchito.GUADANA
                            : c.endsWith("farol") ? com.darkcraft.enemigos.Marchito.FAROL : com.darkcraft.enemigos.Marchito.CAMPANA);
                    yield w;
                }
                case "ahorcado_garras", "ahorcado_soga" -> {
                    var a = DarkCraftMod.AHORCADO.get().create(lvl);
                    if (a != null) a.variante(c.endsWith("soga") ? com.darkcraft.enemigos.Ahorcado.SOGA : com.darkcraft.enemigos.Ahorcado.GARRAS);
                    yield a;
                }
                case "podrido", "podrido_fatuo", "podrido_hinchado" -> {
                    var p = DarkCraftMod.PODRIDO.get().create(lvl);
                    if (p != null) p.variante(c.endsWith("fatuo") ? com.darkcraft.enemigos.Podrido.FATUO
                            : c.endsWith("hinchado") ? com.darkcraft.enemigos.Podrido.HINCHADO : com.darkcraft.enemigos.Podrido.PODRIDO);
                    yield p;
                }
                case "rey" -> DarkCraftMod.REY.get().create(lvl);
                case "gavran" -> DarkCraftMod.GAVRAN.get().create(lvl);
                case "caballero_espada", "caballero_alabarda", "caballero_ballesta" -> {
                    // (desde la parte 4a, la guardia de Valdrenn con su modelo propio)
                    var k = DarkCraftMod.GUARDIA.get().create(lvl);
                    if (k != null) k.variante(c.equals("caballero_espada") ? com.darkcraft.enemigos.Guardia.ESPADA
                            : c.equals("caballero_alabarda") ? com.darkcraft.enemigos.Guardia.ALABARDA : com.darkcraft.enemigos.Guardia.BALLESTA);
                    yield k;
                }
                case "arquero" -> DarkCraftMod.ARQUERO.get().create(lvl);
                case "perro" -> DarkCraftMod.PERRO.get().create(lvl);
                case "capitan_elite" -> DarkCraftMod.ELITE.get().create(lvl);
                case "brandt" -> DarkCraftMod.BRANDT.get().create(lvl);
                case "molinera" -> DarkCraftMod.MOLINERA.get().create(lvl);
                case "verdugo" -> DarkCraftMod.VERDUGO.get().create(lvl);
                case "barquero" -> DarkCraftMod.BARQUERO.get().create(lvl);
                case "corteza" -> DarkCraftMod.CORTEZA.get().create(lvl);
                case "espectro_cortesano", "espectro_dama", "espectro_vigia" -> {
                    var s = DarkCraftMod.ESPECTRO.get().create(lvl);
                    if (s != null) s.variante(c.endsWith("dama") ? com.darkcraft.enemigos.Espectro.GARRAS
                            : c.endsWith("vigia") ? com.darkcraft.enemigos.Espectro.VIGIA : com.darkcraft.enemigos.Espectro.ESTOQUE);
                    yield s;
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
