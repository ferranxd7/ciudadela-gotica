package com.darkcraft.client.ui;

import com.darkcraft.rol.Partida;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Las partidas guardadas (los mundos de la carpeta saves) con lo que el mod apunta de cada una al guardar
 * ({@link Partida}): nivel, zona, ecos, clase, dia y horas. Las mas recientes primero.
 */
public final class Guardadas {
    private Guardadas() {}

    /** Una partida: el mundo y lo del mod (nivel 0 si nunca se guardo con el mod). */
    public record Una(LevelSummary mundo, int nivel, String zona, String clase, int ecos, long dia, double horas) {
        public String id() {
            return mundo.getLevelId();
        }

        public String nombre() {
            String n = mundo.getLevelName();
            return n == null || n.isBlank() ? mundo.getLevelId() : n;
        }

        public boolean jugable() {
            return !mundo.isLocked() && !mundo.isDisabled() && !mundo.requiresManualConversion();
        }

        /** "Nivel 12 · Torre de Ascenso" (o el modo de juego si el mod no sabe nada de ella). */
        public Component linea() {
            if (nivel <= 0) return mundo.getGameMode() == null ? Component.empty()
                    : Component.translatable("gameMode." + mundo.getGameMode().getName());
            Component z = zona == null || zona.isBlank() ? Component.translatable("darkcraft.partidas.sin_zona") : Component.translatable(zona);
            return Component.translatable("darkcraft.partidas.linea", nivel, z);
        }
    }

    /** Las partidas (en el hilo del juego cuando acaba). Si no se puede leer la carpeta, una lista vacia. */
    public static CompletableFuture<List<Una>> cargar(Minecraft mc) {
        LevelStorageSource src = mc.getLevelSource();
        LevelStorageSource.LevelCandidates candidatos;
        try {
            candidatos = src.findLevelCandidates();
        } catch (Exception e) {
            return CompletableFuture.completedFuture(List.of());
        }
        return src.loadLevelSummaries(candidatos).thenApply(lista -> {
            List<Una> out = new ArrayList<>();
            for (LevelSummary s : lista) out.add(con(src, s));
            out.sort(Comparator.comparingLong((Una u) -> u.mundo().getLastPlayed()).reversed());
            return out;
        }).exceptionally(e -> List.of());
    }

    private static Una con(LevelStorageSource src, LevelSummary s) {
        int nivel = 0, ecos = 0;
        long dia = 0;
        double horas = 0;
        String zona = "", clase = "";
        try {
            Path f = src.getBaseDir().resolve(s.getLevelId()).resolve(Partida.FICHERO);
            if (Files.isRegularFile(f)) {
                JsonObject o = JsonParser.parseString(Files.readString(f, StandardCharsets.UTF_8)).getAsJsonObject();
                nivel = o.has("nivel") ? o.get("nivel").getAsInt() : 0;
                ecos = o.has("ecos") ? o.get("ecos").getAsInt() : 0;
                dia = o.has("dia") ? o.get("dia").getAsLong() : 0;
                horas = o.has("horas") ? o.get("horas").getAsDouble() : 0;
                zona = o.has("zona") ? o.get("zona").getAsString() : "";
                clase = o.has("clase") ? o.get("clase").getAsString() : "";
            }
        } catch (Exception ignorada) {
            // un fichero roto: como si no hubiera
        }
        return new Una(s, nivel, zona, clase, ecos, dia, horas);
    }

    /** La carpeta del icono del mundo (icon.png), si lo tiene. */
    public static Path icono(Una u) {
        Path p = u.mundo().getIcon();
        return p != null && Files.isRegularFile(p) ? p : null;
    }
}
