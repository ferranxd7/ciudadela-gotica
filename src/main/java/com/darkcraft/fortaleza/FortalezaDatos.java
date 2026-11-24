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

    public static final class Estado {
        public final Set<Long> hechos = new HashSet<>();
        public boolean completa, puerta, puente, portal;
        /** Altura del suelo donde empieza la escalera del puente (para la corriente de nubes). */
        public int baseEscalera = Integer.MIN_VALUE;
        /** Arreglos ya aplicados a una fortaleza levantada (1 = la escalera de la torre sin atascos). */
        public int parche;
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
            fs.put(en.getKey(), c);
        }
        t.put("fortalezas", fs);
        ListTag l = new ListTag();
        for (String s : libros) l.add(StringTag.valueOf(s));
        t.put("libros", l);
        return t;
    }
}
