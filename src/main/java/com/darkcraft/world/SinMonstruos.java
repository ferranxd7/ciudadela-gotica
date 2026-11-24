package com.darkcraft.world;

import com.darkcraft.DarkCraftMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Los monstruos de Minecraft ya no existen (lo pidio el usuario: "quitalos a todos", siempre y en todas partes): solo
 * quedan los enemigos del mod, en sus sitios. Se corta todo lo que sale solo (de noche, en cuevas, con los chunks, en
 * estructuras, patrullas, asaltos, generadores, refuerzos y jinetes) y los que quedaran de antes se van al cargar su
 * chunk; lo que invoca el mod (los escuderos de Ser Gavran son esqueletos del juego), los comandos y los huevos siguen
 * funcionando.
 */
public final class SinMonstruos {
    private SinMonstruos() {}

    /** Etiqueta del monstruo que no debe llegar a entrar en el mundo (por si quien lo crea no hace caso del evento). */
    private static final String FUERA = "darkcraft_fuera";

    private static boolean delMod(Mob m) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(m.getType());
        return id != null && id.getNamespace().equals(DarkCraftMod.MODID);
    }

    private static boolean solo(MobSpawnType t) {
        return t == MobSpawnType.NATURAL || t == MobSpawnType.CHUNK_GENERATION || t == MobSpawnType.STRUCTURE
                || t == MobSpawnType.PATROL || t == MobSpawnType.SPAWNER || t == MobSpawnType.REINFORCEMENT
                || t == MobSpawnType.JOCKEY || t == MobSpawnType.EVENT;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSpawn(MobSpawnEvent.FinalizeSpawn e) {
        Mob m = e.getEntity();
        if (!(m instanceof Enemy) || delMod(m) || !solo(e.getSpawnType())) return;
        e.setSpawnCancelled(true);
        m.addTag(FUERA);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onJoin(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide() || !(e.getEntity() instanceof Mob m)) return;
        if (m.getTags().contains(FUERA)) {
            e.setCanceled(true);
            return;
        }
        // los que ya estaban en el mundo de antes (o salieron con el terreno): fuera al cargar su chunk, salvo los que
        // puso el mod (llevan una etiqueta suya) o alguien a proposito (con nombre)
        if (!e.loadedFromDisk() || !(m instanceof Enemy) || delMod(m) || m.hasCustomName()) return;
        for (String t : m.getTags()) if (t.startsWith("darkcraft")) return;
        e.setCanceled(true);
    }
}
