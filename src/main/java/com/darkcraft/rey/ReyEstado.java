package com.darkcraft.rey;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/** Lo que se guarda del Rey en cada mundo: si duerme, si pelea o si ya ha muerto, y quien lo mato. */
public final class ReyEstado extends SavedData {
    public static final int DORMIDO = 0, LUCHA = 1, MUERTO = 2;
    private static final String NAME = "darkcraft_rey";

    private final Map<String, Integer> estado = new HashMap<>();
    private final Map<String, Set<UUID>> vencedores = new HashMap<>();
    private final Set<String> sellosAbiertos = new HashSet<>();

    public static ReyEstado get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(ReyEstado::load, ReyEstado::new, NAME);
    }

    private static ReyEstado load(CompoundTag tag) {
        ReyEstado d = new ReyEstado();
        CompoundTag e = tag.getCompound("estado");
        for (String k : e.getAllKeys()) {
            int v = e.getInt(k);
            // una pelea que estaba en marcha al cerrar el mundo vuelve a empezar
            d.estado.put(k, v == LUCHA ? DORMIDO : v);
        }
        CompoundTag w = tag.getCompound("vencedores");
        for (String k : w.getAllKeys()) {
            Set<UUID> s = new HashSet<>();
            for (Tag t : w.getList(k, Tag.TAG_STRING)) {
                try {
                    s.add(UUID.fromString(t.getAsString()));
                } catch (IllegalArgumentException ignored) {
                }
            }
            d.vencedores.put(k, s);
        }
        for (Tag t : tag.getList("sellos", Tag.TAG_STRING)) d.sellosAbiertos.add(t.getAsString());
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag e = new CompoundTag();
        estado.forEach(e::putInt);
        tag.put("estado", e);
        CompoundTag w = new CompoundTag();
        vencedores.forEach((k, s) -> {
            ListTag l = new ListTag();
            for (UUID u : s) l.add(StringTag.valueOf(u.toString()));
            w.put(k, l);
        });
        tag.put("vencedores", w);
        ListTag sl = new ListTag();
        for (String s : sellosAbiertos) sl.add(StringTag.valueOf(s));
        tag.put("sellos", sl);
        return tag;
    }

    public int get(String key) {
        return estado.getOrDefault(key, DORMIDO);
    }

    public void set(String key, int v) {
        estado.put(key, v);
        setDirty();
    }

    public void ganadores(String key, Collection<UUID> who) {
        vencedores.computeIfAbsent(key, k -> new HashSet<>()).addAll(who);
        setDirty();
    }

    public boolean gano(String key, UUID who) {
        Set<UUID> s = vencedores.get(key);
        return s != null && s.contains(who);
    }

    public boolean selloAbierto(String key) {
        return sellosAbiertos.contains(key);
    }

    public void abrirSello(String key) {
        sellosAbiertos.add(key);
        setDirty();
    }

    /** Para el comando de pruebas: la puerta de la Sala del Arbol vuelve a estar cerrada. */
    public void cerrarSello(String key) {
        sellosAbiertos.remove(key);
        setDirty();
    }

    /** Para el comando de pruebas: todo como nuevo. */
    public void olvidar(String key) {
        estado.remove(key);
        vencedores.remove(key);
        sellosAbiertos.remove(key);
        setDirty();
    }
}
