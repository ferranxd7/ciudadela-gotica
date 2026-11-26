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
 * igual en un servidor dedicado): los Ecos de la Sombra que lleva, la espada de luz que dejo al morir, las zonas que ha
 * descubierto, sus atributos (de ellos sale el nivel), sus frascos y los santuarios en los que ha descansado.
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

    // ------------------------------------------------------------------ lo hablado con los personajes

    /** Lo que ya se ha hablado con un personaje (marcas de bits; NBT {@code dialogos}). */
    public static int dialogo(Player p, String personaje) {
        return leer(p).getCompound("dialogos").getInt(personaje);
    }

    public static void dialogo(Player p, String personaje, int marcas) {
        CompoundTag f = abrir(p);
        CompoundTag d = f.getCompound("dialogos");
        d.putInt(personaje, marcas);
        f.put("dialogos", d);
    }

    /** Cuantas se han comprado de una cosa de la tienda (las que tienen limite; clave = el id del objeto). */
    public static int comprado(Player p, String clave) {
        return leer(p).getCompound("compras").getInt(clave);
    }

    public static void comprado(Player p, String clave, int n) {
        CompoundTag f = abrir(p);
        CompoundTag d = f.getCompound("compras");
        if (n <= 0) d.remove(clave);
        else d.putInt(clave, n);
        f.put("compras", d);
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

    // ------------------------------------------------------------------ atributos y nivel

    /** Los ocho atributos (en el orden de {@link Atributo}); quien aun no tiene ficha, los de base. */
    public static int[] atributos(Player p) {
        int[] a = leer(p).getIntArray("atributos");
        if (a.length != Atributo.N) return Atributo.deBase();
        int[] r = a.clone();
        for (int i = 0; i < r.length; i++) r[i] = Math.max(1, Math.min(Atributo.TOPE, r[i]));
        return r;
    }

    static void atributos(Player p, int[] a) {
        abrir(p).putIntArray("atributos", a.clone());
    }

    public static int nivel(Player p) {
        return Nivel.de(atributos(p));
    }

    /** La clase con la que empezo ("" si aun no ha elegido; {@link Clase#SIN_CLASE} si ya jugaba antes de las clases). */
    public static String clase(Player p) {
        return abrir(p).getString("clase");
    }

    static void clase(Player p, String c) {
        abrir(p).putString("clase", c == null ? "" : c);
    }

    // ------------------------------------------------------------------ frascos

    /** Los frascos del jugador: cuantos lleva de cada (se rellenan al descansar), cuantos le quedan y su mejora. */
    public record Frascos(int vidaMax, int manaMax, int vida, int mana, int mejora) {
        public static final int VIDA = 0, MANA = 1;
    }

    public static Frascos frascos(Player p) {
        CompoundTag f = leer(p);
        if (!f.contains("frascos", Tag.TAG_COMPOUND)) return new Frascos(3, 1, 3, 1, 0);
        CompoundTag t = f.getCompound("frascos");
        int vm = Math.max(0, t.getInt("vida_max")), mm = Math.max(0, t.getInt("mana_max"));
        return new Frascos(vm, mm, Math.max(0, Math.min(vm, t.getInt("vida"))), Math.max(0, Math.min(mm, t.getInt("mana"))), t.getInt("mejora"));
    }

    static void frascos(Player p, Frascos fr) {
        CompoundTag t = new CompoundTag();
        t.putInt("vida_max", fr.vidaMax());
        t.putInt("mana_max", fr.manaMax());
        t.putInt("vida", fr.vida());
        t.putInt("mana", fr.mana());
        t.putInt("mejora", fr.mejora());
        abrir(p).put("frascos", t);
    }

    // ------------------------------------------------------------------ santuarios

    /** Los santuarios que conoce (los que ha encendido o en los que ha descansado): "dimension@x,y,z". */
    public static java.util.Set<String> santuariosVistos(Player p) {
        ListTag l = leer(p).getList("santuarios", Tag.TAG_STRING);
        java.util.Set<String> r = new java.util.HashSet<>();
        for (int i = 0; i < l.size(); i++) r.add(l.getString(i));
        return r;
    }

    /** Apunta el santuario (dimension y sitio) y dice si es la primera vez que este jugador descansa en el. */
    public static boolean santuarioNuevo(Player p, ResourceKey<Level> dim, net.minecraft.core.BlockPos pos) {
        String clave = dim.location() + "@" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
        CompoundTag f = abrir(p);
        ListTag l = f.getList("santuarios", Tag.TAG_STRING);
        for (int i = 0; i < l.size(); i++) if (l.getString(i).equals(clave)) return false;
        l.add(StringTag.valueOf(clave));
        f.put("santuarios", l);
        return true;
    }

    /** Olvida los santuarios cuya clave empieza por {@code prefijo} (el comando de las pruebas); dice cuantos. */
    public static int olvidarSantuarios(Player p, String prefijo) {
        CompoundTag f = abrir(p);
        ListTag l = f.getList("santuarios", Tag.TAG_STRING), r = new ListTag();
        for (int i = 0; i < l.size(); i++) if (!l.getString(i).startsWith(prefijo)) r.add(StringTag.valueOf(l.getString(i)));
        f.put("santuarios", r);
        return l.size() - r.size();
    }

    public static int santuarios(Player p) {
        return leer(p).getList("santuarios", Tag.TAG_STRING).size();
    }

    // ------------------------------------------------------------------ los mapas de Morvain (parte 7d)

    /** Las regiones de Morvain cuyo mapa ha copiado de su estela (ids de {@code morvain.Region}). */
    public static java.util.Set<String> mapas(Player p) {
        ListTag l = leer(p).getList("mapas", Tag.TAG_STRING);
        java.util.Set<String> r = new java.util.HashSet<>();
        for (int i = 0; i < l.size(); i++) r.add(l.getString(i));
        return r;
    }

    /** Apunta el mapa de esa region y dice si no lo tenia. */
    public static boolean mapaNuevo(Player p, String region) {
        CompoundTag f = abrir(p);
        ListTag l = f.getList("mapas", Tag.TAG_STRING);
        for (int i = 0; i < l.size(); i++) if (l.getString(i).equals(region)) return false;
        l.add(StringTag.valueOf(region));
        f.put("mapas", l);
        return true;
    }

    /** Le quita todos los mapas (el comando de las pruebas); dice cuantos tenia. */
    public static int olvidarMapas(Player p) {
        CompoundTag f = abrir(p);
        int n = f.getList("mapas", Tag.TAG_STRING).size();
        f.remove("mapas");
        return n;
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
