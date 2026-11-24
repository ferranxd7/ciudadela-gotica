package com.darkcraft.rol;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * La ficha de rol de cada jugador, guardada con el (NBT persistente {@code darkcraft_rol}: sobrevive a la muerte y vale
 * igual en un servidor dedicado). De momento: los Ecos de la Sombra que lleva, la espada de luz que dejo al morir y las
 * zonas que ya ha descubierto. Aqui iran el nivel, los atributos y lo demas del rol.
 */
public final class Ficha {
    private Ficha() {}

    private static final String KEY = "darkcraft_rol";
    /** Tope de ecos (para que la suma no se desborde nunca). */
    public static final int TOPE = 999_999_999;

    static CompoundTag leer(Player p) {
        return p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(KEY);
    }

    /** La ficha para escribir en ella (queda enganchada a los datos del jugador). */
    static CompoundTag abrir(Player p) {
        CompoundTag root = p.getPersistentData();
        CompoundTag pers = root.getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag f = pers.getCompound(KEY);
        pers.put(KEY, f);
        root.put(Player.PERSISTED_NBT_TAG, pers);
        return f;
    }

    // ------------------------------------------------------------------ ecos

    public static int ecos(Player p) {
        return leer(p).getInt("ecos");
    }

    static void ecos(Player p, int v) {
        abrir(p).putInt("ecos", Math.max(0, Math.min(TOPE, v)));
    }

    // ------------------------------------------------------------------ la espada de luz

    /** Donde se quedaron los ecos al morir. */
    public record Espada(ResourceKey<Level> dim, double x, double y, double z, int ecos) {
        public double distancia2(Player p) {
            double dx = p.getX() - x, dy = p.getY() - y, dz = p.getZ() - z;
            return dx * dx + dy * dy + dz * dz;
        }
    }

    public static Espada espada(Player p) {
        CompoundTag f = leer(p);
        if (!f.contains("espada", Tag.TAG_COMPOUND)) return null;
        CompoundTag e = f.getCompound("espada");
        ResourceLocation dim = ResourceLocation.tryParse(e.getString("dim"));
        if (dim == null || e.getInt("ecos") <= 0) return null;
        return new Espada(ResourceKey.create(Registries.DIMENSION, dim), e.getDouble("x"), e.getDouble("y"), e.getDouble("z"), e.getInt("ecos"));
    }

    static void espada(Player p, Espada e) {
        CompoundTag f = abrir(p);
        if (e == null) {
            f.remove("espada");
            return;
        }
        CompoundTag t = new CompoundTag();
        t.putString("dim", e.dim().location().toString());
        t.putDouble("x", e.x());
        t.putDouble("y", e.y());
        t.putDouble("z", e.z());
        t.putInt("ecos", e.ecos());
        f.put("espada", t);
    }

    // ------------------------------------------------------------------ zonas descubiertas

    /** Apunta la zona (clave de su nombre) y dice si es la primera vez que el jugador entra en ella. */
    static boolean zonaNueva(Player p, String clave) {
        CompoundTag f = abrir(p);
        ListTag l = f.getList("zonas", Tag.TAG_STRING);
        for (int i = 0; i < l.size(); i++) if (l.getString(i).equals(clave)) return false;
        l.add(StringTag.valueOf(clave));
        f.put("zonas", l);
        return true;
    }

    static boolean zonaVista(Player p, String clave) {
        ListTag l = leer(p).getList("zonas", Tag.TAG_STRING);
        for (int i = 0; i < l.size(); i++) if (l.getString(i).equals(clave)) return true;
        return false;
    }
}
