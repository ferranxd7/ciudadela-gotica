package com.darkcraft.client.espejo;

import com.darkcraft.DarkCraftMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Las marcas que escribe el jugador en su mapa (por mundo y dimension), en marcas.json junto al mapa explorado. */
public final class Marcas {
    /** Colores de tinta para las marcas. */
    public static final int[] COLORES = {0xE23A32, 0xF09A28, 0xF2DC4A, 0x5CC85A, 0x48B8E8, 0x5A6AF0, 0xB45AE8, 0xF2F0EA};
    /** Iconos de marca (en el atlas empiezan en EspejoHud.I_MARCAS). */
    public static final int ICONOS = 12;

    public static final class Marca {
        public String nombre;
        public int icono, color, x, y, z;

        public Marca(String nombre, int icono, int color, int x, int y, int z) {
            this.nombre = nombre;
            this.icono = icono;
            this.color = color;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path fichero;
    public final List<Marca> lista = new ArrayList<>();

    Marcas(Path carpeta) {
        this.fichero = carpeta.resolve("marcas.json");
        if (Files.isRegularFile(fichero)) {
            try {
                List<Marca> l = GSON.fromJson(Files.readString(fichero, StandardCharsets.UTF_8), new TypeToken<List<Marca>>() {}.getType());
                if (l != null) for (Marca m : l) if (m != null && m.nombre != null) lista.add(m);
            } catch (IOException | RuntimeException e) {
                DarkCraftMod.LOGGER.warn("Espejo: no se pudieron leer las marcas: {}", e.toString());
            }
        }
    }

    public void anadir(Marca m) {
        lista.add(m);
        guardar();
    }

    public void quitar(Marca m) {
        lista.remove(m);
        guardar();
    }

    public void guardar() {
        try {
            Files.createDirectories(fichero.getParent());
            Files.writeString(fichero, GSON.toJson(lista), StandardCharsets.UTF_8);
        } catch (IOException e) {
            DarkCraftMod.LOGGER.warn("Espejo: no se pudieron guardar las marcas: {}", e.toString());
        }
    }
}
