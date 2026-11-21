package com.ciudadela.story;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Lo que guarda cada cofre de la cronica para cada jugador (cada uno tiene su propia copia). */
public final class StoryData extends SavedData {
    private final Map<String, CompoundTag> inv = new HashMap<>();

    public static StoryData get(ServerLevel any) {
        return any.getServer().overworld().getDataStorage().computeIfAbsent(StoryData::load, StoryData::new, "ciudadela_cronica");
    }

    private static String key(BlockPos pos, UUID player) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ() + "|" + player;
    }

    public CompoundTag items(BlockPos pos, UUID player) {
        return inv.get(key(pos, player));
    }

    public void setItems(BlockPos pos, UUID player, CompoundTag items) {
        inv.put(key(pos, player), items);
        setDirty();
    }

    public void forgetPlayer(UUID player) {
        inv.keySet().removeIf(k -> k.endsWith("|" + player));
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag all = new CompoundTag();
        for (var e : inv.entrySet()) all.put(e.getKey(), e.getValue());
        tag.put("cofres", all);
        return tag;
    }

    private static StoryData load(CompoundTag tag) {
        StoryData d = new StoryData();
        CompoundTag all = tag.getCompound("cofres");
        for (String k : all.getAllKeys()) d.inv.put(k, all.getCompound(k));
        return d;
    }
}
