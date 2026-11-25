package com.darkcraft.rol;

import com.darkcraft.DarkCraftMod;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Lo que la lista de partidas ensena de cada mundo (parte 2f-2): el nivel, la zona donde se quedo el jugador, sus ecos,
 * la clase (desde la 2f-3), el dia y las horas jugadas. Se escribe en {@code darkcraft_partida.json} dentro de la carpeta
 * del mundo al guardar y al salir, solo en los mundos de un jugador (lo de su dueno).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Partida {
    private Partida() {}

    public static final String FICHERO = "darkcraft_partida.json";

    @SubscribeEvent
    public static void onSalir(PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) escribir(sp);
    }

    @SubscribeEvent
    public static void onGuardar(LevelEvent.Save e) {
        if (!(e.getLevel() instanceof ServerLevel l) || l.dimension() != Level.OVERWORLD) return;
        for (ServerPlayer sp : l.getServer().getPlayerList().getPlayers()) escribir(sp);
    }

    public static void escribir(ServerPlayer sp) {
        MinecraftServer srv = sp.server;
        if (srv == null || srv.isDedicatedServer() || !srv.isSingleplayerOwner(sp.getGameProfile())) return;
        try {
            JsonObject o = new JsonObject();
            o.addProperty("jugador", sp.getGameProfile().getName());
            o.addProperty("nivel", Ficha.nivel(sp));
            o.addProperty("ecos", Ficha.ecos(sp));
            String zona = Rol.de(sp).zona;
            o.addProperty("zona", zona == null ? "" : zona);
            o.addProperty("clase", "");
            o.addProperty("dia", sp.level().getDayTime() / 24000L + 1);
            int ticks = sp.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
            o.addProperty("horas", Math.round(ticks / 72000.0 * 10) / 10.0);
            o.addProperty("version", ModList.get().getModContainerById(DarkCraftMod.MODID)
                    .map(c -> c.getModInfo().getVersion().toString()).orElse(""));
            Path dir = srv.getWorldPath(LevelResource.ROOT);
            Path tmp = dir.resolve(FICHERO + ".tmp");
            Files.writeString(tmp, o.toString(), StandardCharsets.UTF_8);
            Files.move(tmp, dir.resolve(FICHERO), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            DarkCraftMod.LOGGER.debug("Partida: no se pudo escribir {}: {}", FICHERO, ex.toString());
        }
    }
}
