package com.darkcraft.bloques;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

/** GENERADO por tools/bloques/bloques.py: no tocar a mano. Los materiales de los bloques nuevos y sus formas. */
public final class CatalogoBloques {
    private CatalogoBloques() {}

    /** Un material: su id, sus formas (B bloque, E escalera, L losa, M muro, C columna, V valla, P cancela, D puerta,
     *  T trampilla), su sonido, su color en el mapa, lo que cuesta romperlo, los sonidos de sus puertas y trampillas
     *  (juego) y de su cancela (madera) y si hace falta un pico para que suelte algo (la piedra; la madera, no). */
    public record Material(String id, String formas, SoundType sonido, MapColor mapa, float dureza, BlockSetType juego,
                           WoodType madera, boolean pico) {}

    public static final Material[] MATERIALES = {
            new Material("sillar_gris", "BELM", SoundType.STONE, MapColor.STONE, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("sillar_palido", "BELM", SoundType.TUFF, MapColor.SAND, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("sillar_oscuro", "BELM", SoundType.DEEPSLATE_BRICKS, MapColor.COLOR_BLACK, 2.0F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("sillar_musgoso", "BELM", SoundType.STONE, MapColor.COLOR_GREEN, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("sillar_agrietado", "BELM", SoundType.STONE, MapColor.STONE, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("sillar_nevado", "BELM", SoundType.STONE, MapColor.ICE, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("sillar_verdin", "BELM", SoundType.MUD_BRICKS, MapColor.COLOR_GREEN, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("marmol_blanco", "BELM", SoundType.CALCITE, MapColor.QUARTZ, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("marmol_negro", "BELM", SoundType.POLISHED_DEEPSLATE, MapColor.COLOR_BLACK, 2.0F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("sillar_rojo", "BELM", SoundType.STONE, MapColor.COLOR_RED, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("ladrillo_viejo", "BELM", SoundType.MUD_BRICKS, MapColor.COLOR_RED, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("ladrillo_oscuro", "BELM", SoundType.NETHER_BRICKS, MapColor.COLOR_BROWN, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("adoquin", "BELM", SoundType.STONE, MapColor.STONE, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("enlosado", "BELM", SoundType.STONE, MapColor.STONE, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("piedra_tallada", "BELM", SoundType.STONE, MapColor.SAND, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("mamposteria", "BELM", SoundType.STONE, MapColor.STONE, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("columna_gris", "C", SoundType.STONE, MapColor.STONE, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("columna_palida", "C", SoundType.STONE, MapColor.SAND, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("columna_marmol", "C", SoundType.CALCITE, MapColor.QUARTZ, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("columna_oscura", "C", SoundType.DEEPSLATE_BRICKS, MapColor.COLOR_BLACK, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("madera_negra", "BELVPDT", SoundType.WOOD, MapColor.COLOR_BLACK, 2.0F, BlockSetType.DARK_OAK, WoodType.DARK_OAK, false),
            new Material("madera_roble", "BELVPDT", SoundType.WOOD, MapColor.WOOD, 2.0F, BlockSetType.OAK, WoodType.OAK, false),
            new Material("madera_gris", "BELVPDT", SoundType.WOOD, MapColor.COLOR_GRAY, 2.0F, BlockSetType.SPRUCE, WoodType.SPRUCE, false),
            new Material("madera_ceniza", "BELVPDT", SoundType.WOOD, MapColor.SAND, 2.0F, BlockSetType.BIRCH, WoodType.BIRCH, false),
            new Material("madera_podrida", "BELVPDT", SoundType.WOOD, MapColor.COLOR_GREEN, 2.0F, BlockSetType.MANGROVE, WoodType.MANGROVE, false),
            new Material("madera_quemada", "BELVP", SoundType.WOOD, MapColor.COLOR_BLACK, 2.0F, BlockSetType.CRIMSON, WoodType.CRIMSON, false),
            new Material("madera_caoba", "BELVPDT", SoundType.WOOD, MapColor.COLOR_RED, 2.0F, BlockSetType.JUNGLE, WoodType.JUNGLE, false),
            new Material("viga_negra", "C", SoundType.WOOD, MapColor.COLOR_BLACK, 2.0F, BlockSetType.STONE, WoodType.OAK, false),
            new Material("viga_roble", "C", SoundType.WOOD, MapColor.WOOD, 2.0F, BlockSetType.STONE, WoodType.OAK, false),
            new Material("viga_gris", "C", SoundType.WOOD, MapColor.COLOR_GRAY, 2.0F, BlockSetType.STONE, WoodType.OAK, false),
            new Material("viga_podrida", "C", SoundType.WOOD, MapColor.COLOR_GREEN, 2.0F, BlockSetType.STONE, WoodType.OAK, false),
            new Material("entramado", "B", SoundType.WOOD, MapColor.TERRACOTTA_WHITE, 1.5F, BlockSetType.STONE, WoodType.OAK, false),
            new Material("entramado_cruz", "B", SoundType.WOOD, MapColor.TERRACOTTA_WHITE, 1.5F, BlockSetType.STONE, WoodType.OAK, false),
            new Material("entramado_diagonal", "B", SoundType.WOOD, MapColor.TERRACOTTA_WHITE, 1.5F, BlockSetType.STONE, WoodType.OAK, false),
            new Material("entramado_contra", "B", SoundType.WOOD, MapColor.TERRACOTTA_WHITE, 1.5F, BlockSetType.STONE, WoodType.OAK, false),
            new Material("entramado_poste", "B", SoundType.WOOD, MapColor.TERRACOTTA_WHITE, 1.5F, BlockSetType.STONE, WoodType.OAK, false),
            new Material("entramado_travesano", "B", SoundType.WOOD, MapColor.TERRACOTTA_WHITE, 1.5F, BlockSetType.STONE, WoodType.OAK, false),
            new Material("enlucido", "BELM", SoundType.STONE, MapColor.TERRACOTTA_WHITE, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("tejado_pizarra", "BEL", SoundType.DEEPSLATE_TILES, MapColor.COLOR_GRAY, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("tejado_escamas", "BEL", SoundType.DEEPSLATE_TILES, MapColor.COLOR_BLUE, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("tejado_barro", "BEL", SoundType.STONE, MapColor.TERRACOTTA_RED, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("tejado_musgo", "BEL", SoundType.STONE, MapColor.COLOR_GREEN, 1.5F, BlockSetType.STONE, WoodType.OAK, true),
            new Material("tejado_tejamanil", "BEL", SoundType.WOOD, MapColor.COLOR_GRAY, 1.5F, BlockSetType.STONE, WoodType.OAK, false)
    };
}
