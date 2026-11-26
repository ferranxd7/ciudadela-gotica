package com.darkcraft.fortaleza;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Lo que se guarda de las Fortalezas de Hueso de un mundo: que chunks se han levantado ya, si la puerta esta abierta,
 * si esta el puente y si se abrio el portal; y los libros perdidos ya puestos en cada Reino de Valdrenn.
 */
public final class FortalezaDatos extends SavedData {
    public static final String ID = "darkcraft_fortalezas";
    /**
     * La fortaleza que se levanta ahora: 1 la de antes (murallas cuadradas), 2 la torre bajo la cupula (parte 5a, con la
     * escalera de caracol delante), 3 la misma a la que se sube por la montana y su puente largo (parte 5b), 4 con sus
     * trampas, la cripta bajo la fuente y la sala secreta (parte 5c), 5 con el portal nuevo de lo alto (parte 5e).
     */
    public static final int VERSION = 5;

    public static final class Estado {
        public final Set<Long> hechos = new HashSet<>();
        public boolean completa, puerta, puente, portal;
        /** Altura del suelo donde empieza la escalera del puente (para la corriente de nubes). */
        public int baseEscalera = Integer.MIN_VALUE;
        /** Arreglos ya aplicados a una fortaleza levantada (1 = la escalera de la torre sin atascos). */
        public int parche;
        /** La fortaleza que hay levantada (la de antes se rehace: {@link #VERSION}). */
        public int version = VERSION;
        /** Chunks de la fortaleza de antes que hay que vaciar antes de levantar la nueva. */
        public final Set<Long> porLimpiar = new HashSet<>();
        /** Se rehizo (de una de antes): a quien ya la abrio se le devuelve la llave de hueso, una vez. */
        public boolean rehecha;
        public final Set<String> llaves = new HashSet<>();
        /**
         * La escalera de hueso que queda por quitar: {x, z del centro, altura de su base[, 1 si es la de la parte 5a]}, o
         * null. La de antes de la 5a solo por debajo de la isla; la de la 5a, pieza a pieza.
         */
        public int[] puenteViejo;
        /** El muro falso del sotano ya se deshizo (la sala secreta queda abierta). */
        public boolean muro;
        /** Ya cayo el rayo rojo sobre el portal abierto (parte 5e: desde entonces, el cielo rojo y su columna de luz). */
        public boolean rayo;
    }

    final Map<String, Estado> fortalezas = new HashMap<>();
    /** Libros perdidos ya puestos en cada reino: "sx,sz:N". */
    final Set<String> libros = new HashSet<>();

    public static FortalezaDatos get(MinecraftServer srv) {
        return srv.overworld().getDataStorage().computeIfAbsent(FortalezaDatos::load, FortalezaDatos::new, ID);
    }

    public Estado de(String clave) {
        return fortalezas.computeIfAbsent(clave, k -> new Estado());
    }

    public Estado de(Fortalezas.Sitio s) {
        return de(s.clave());
    }

    static FortalezaDatos load(CompoundTag t) {
        FortalezaDatos d = new FortalezaDatos();
        CompoundTag fs = t.getCompound("fortalezas");
        for (String k : fs.getAllKeys()) {
            CompoundTag c = fs.getCompound(k);
            Estado e = d.de(k);
            for (long l : c.getLongArray("hechos")) e.hechos.add(l);
            e.completa = c.getBoolean("completa");
            e.puerta = c.getBoolean("puerta");
            e.puente = c.getBoolean("puente");
            e.portal = c.getBoolean("portal");
            e.baseEscalera = c.contains("base") ? c.getInt("base") : Integer.MIN_VALUE;
            e.parche = c.getInt("parche");
            e.version = c.contains("version") ? c.getInt("version") : 1;
            for (long l : c.getLongArray("limpiar")) e.porLimpiar.add(l);
            e.rehecha = c.getBoolean("rehecha");
            for (Tag x : c.getList("llaves", Tag.TAG_STRING)) e.llaves.add(x.getAsString());
            if (c.contains("puente_viejo")) e.puenteViejo = c.getIntArray("puente_viejo");
            e.muro = c.getBoolean("muro");
            e.rayo = c.getBoolean("rayo");
        }
        for (Tag x : t.getList("libros", Tag.TAG_STRING)) d.libros.add(x.getAsString());
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag t) {
        CompoundTag fs = new CompoundTag();
        for (Map.Entry<String, Estado> en : fortalezas.entrySet()) {
            Estado e = en.getValue();
            CompoundTag c = new CompoundTag();
            c.put("hechos", new LongArrayTag(e.hechos.stream().mapToLong(Long::longValue).toArray()));
            c.putBoolean("completa", e.completa);
            c.putBoolean("puerta", e.puerta);
            c.putBoolean("puente", e.puente);
            c.putBoolean("portal", e.portal);
            if (e.baseEscalera != Integer.MIN_VALUE) c.putInt("base", e.baseEscalera);
            c.putInt("parche", e.parche);
            c.putInt("version", e.version);
            if (!e.porLimpiar.isEmpty()) c.put("limpiar", new LongArrayTag(e.porLimpiar.stream().mapToLong(Long::longValue).toArray()));
            c.putBoolean("rehecha", e.rehecha);
            ListTag ll = new ListTag();
            for (String u : e.llaves) ll.add(StringTag.valueOf(u));
            c.put("llaves", ll);
            if (e.puenteViejo != null) c.putIntArray("puente_viejo", e.puenteViejo);
            c.putBoolean("muro", e.muro);
            c.putBoolean("rayo", e.rayo);
            fs.put(en.getKey(), c);
        }
        t.put("fortalezas", fs);
        ListTag l = new ListTag();
        for (String s : libros) l.add(StringTag.valueOf(s));
        t.put("libros", l);
        return t;
    }
}
