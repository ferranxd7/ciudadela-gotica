package com.darkcraft.bloques;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/** GENERADO por tools/bloques/bloques.py: no tocar a mano. Los materiales de los bloques nuevos y sus formas. */
public final class CatalogoBloques {
    private CatalogoBloques() {}

    /** Un material: su id, sus formas (B bloque, E escalera, L losa, M muro, C columna), su sonido, su color en el mapa y lo
     *  que cuesta romperlo. */
    public record Material(String id, String formas, SoundType sonido, MapColor mapa, float dureza) {}

    public static final Material[] MATERIALES = {
            new Material("sillar_gris", "BELM", SoundType.STONE, MapColor.STONE, 1.5F),
            new Material("sillar_palido", "BELM", SoundType.TUFF, MapColor.SAND, 1.5F),
            new Material("sillar_oscuro", "BELM", SoundType.DEEPSLATE_BRICKS, MapColor.COLOR_BLACK, 2.0F),
            new Material("sillar_musgoso", "BELM", SoundType.STONE, MapColor.COLOR_GREEN, 1.5F),
            new Material("sillar_agrietado", "BELM", SoundType.STONE, MapColor.STONE, 1.5F),
            new Material("sillar_nevado", "BELM", SoundType.STONE, MapColor.ICE, 1.5F),
            new Material("sillar_verdin", "BELM", SoundType.MUD_BRICKS, MapColor.COLOR_GREEN, 1.5F),
            new Material("marmol_blanco", "BELM", SoundType.CALCITE, MapColor.QUARTZ, 1.5F),
            new Material("marmol_negro", "BELM", SoundType.POLISHED_DEEPSLATE, MapColor.COLOR_BLACK, 2.0F),
            new Material("sillar_rojo", "BELM", SoundType.STONE, MapColor.COLOR_RED, 1.5F),
            new Material("ladrillo_viejo", "BELM", SoundType.MUD_BRICKS, MapColor.COLOR_RED, 1.5F),
            new Material("ladrillo_oscuro", "BELM", SoundType.NETHER_BRICKS, MapColor.COLOR_BROWN, 1.5F),
            new Material("adoquin", "BELM", SoundType.STONE, MapColor.STONE, 1.5F),
            new Material("enlosado", "BELM", SoundType.STONE, MapColor.STONE, 1.5F),
            new Material("piedra_tallada", "BELM", SoundType.STONE, MapColor.SAND, 1.5F),
            new Material("mamposteria", "BELM", SoundType.STONE, MapColor.STONE, 1.5F),
            new Material("columna_gris", "C", SoundType.STONE, MapColor.STONE, 1.5F),
            new Material("columna_palida", "C", SoundType.STONE, MapColor.SAND, 1.5F),
            new Material("columna_marmol", "C", SoundType.CALCITE, MapColor.QUARTZ, 1.5F),
            new Material("columna_oscura", "C", SoundType.DEEPSLATE_BRICKS, MapColor.COLOR_BLACK, 1.5F)
    };
}
