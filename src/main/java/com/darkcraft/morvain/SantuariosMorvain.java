package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Los santuarios de Morvain (parte 7c, respuesta 92 del plan: "santuarios con teletransporte"): diecinueve repartidos por
 * las regiones (dos o tres por region, uno en el crater) mas el del Arco del Umbral. Cada uno es una tarima redonda de
 * piedra (de la piedra de su region) a la altura del terreno, con el santuario en el centro, dos pilares rotos y dos
 * farolas de almas; se escriben al generar el chunk (los ve Distant Horizons). Los sitios se eligieron sin abrir el juego
 * ({@code tools/morvain}: el sitio mas llano y seco cerca de donde se queria, en su region; el pantano, en un islote).
 * <p>
 * Encenderlos y descansar es lo de siempre ({@code Santuarios}); su nombre sale de aqui
 * ({@code darkcraft.santuario.morvain_<id>}) y entre los que un jugador ha descansado puede viajar ({@link Viaje}).
 */
public final class SantuariosMorvain {
    private SantuariosMorvain() {}

    /** Un santuario: su id, el centro de la tarima, su suelo (lo que se pisa esta en y + 1) y su region. */
    public record Sitio(String id, int x, int z, int y, Region region) {
        /** Donde esta el bloque del santuario. */
        public BlockPos pos() {
            return new BlockPos(x, y + 1, z);
        }

        public String nombre() {
            return "darkcraft.santuario.morvain_" + id;
        }
    }

    private static Sitio s(String id, int x, int z) {
        Terreno.Columna c = Terreno.columna(x, z);
        return new Sitio(id, x, z, (int) Math.floor(c.altura()), c.region());
    }

    /** El del Arco del Umbral (lo escribe {@link Llegada}): tambien se viaja a el. */
    public static final Sitio UMBRAL = new Sitio("llegada", Llegada.X, Llegada.Z, Llegada.Y, Region.LLANURAS);

    /** Los demas (los sitios salen de tools/morvain: llanos y secos, en su region). */
    public static final Sitio[] SITIOS = {
            s("encrucijada", -1132, 5099), s("vado", 868, 4181),
            s("ahorcados", -4832, 3200), s("raiz_hueca", -5674, 5712),
            s("juncos", -5600, -720), s("embarcadero", -4023, -1897),
            s("colmillo", -3132, -3588), s("coloso", 886, -4922), s("helada", 3352, -5400),
            s("puerta_academia", 4110, -3176), s("observatorio", 5577, -3543),
            s("puerta_capital", 3177, 343), s("juramento", 5088, -824),
            s("faro_huesos", 5612, 4779), s("costillar", 5244, 3000),
            s("grieta", 24, 2181), s("altar", -1864, -1600), s("mirador", 2120, -480),
            s("ancla_sur", 50, 1080),
    };

    /** El radio de la tarima y hasta donde se funde con el terreno. */
    static final int R = 4, MEZCLA = 10;

    /** Todos, el del arco el primero. */
    public static Sitio[] todos() {
        return TODOS;
    }

    private static final Sitio[] TODOS = new Sitio[SITIOS.length + 1];

    static {
        TODOS[0] = UMBRAL;
        System.arraycopy(SITIOS, 0, TODOS, 1, SITIOS.length);
    }

    public static Sitio porId(String id) {
        for (Sitio s : todos()) if (s.id.equals(id)) return s;
        return null;
    }

    /** El santuario de Morvain que esta en {@code pos} (o null). */
    public static Sitio en(BlockPos pos) {
        for (Sitio s : todos()) if (s.pos().equals(pos)) return s;
        return null;
    }

    /** El mas cercano a (x, z) a menos de {@code r} (o null). */
    public static Sitio cerca(double x, double z, double r) {
        Sitio mejor = null;
        double d = r * r;
        for (Sitio s : todos()) {
            double dd = (s.x - x) * (s.x - x) + (s.z - z) * (s.z - z);
            if (dd <= d) {
                d = dd;
                mejor = s;
            }
        }
        return mejor;
    }

    // ------------------------------------------------------------------ escribir un chunk

    /** Si el chunk que empieza en (x0, z0) tiene algo de alguna tarima. */
    static boolean toca(int x0, int z0) {
        for (Sitio s : SITIOS) if (toca(s, x0, z0, MEZCLA + 2)) return true;
        return false;
    }

    private static boolean toca(Sitio s, int x0, int z0, int r) {
        return x0 + 15 >= s.x - r && x0 <= s.x + r && z0 + 15 >= s.z - r && z0 <= s.z + r;
    }

    /** Si el chunk esta junto a una tarima (ahi no se ponen arboles ni rocas). */
    static boolean libre(int x0, int z0) {
        for (Sitio s : SITIOS) if (toca(s, x0, z0, MEZCLA + 4)) return true;
        return false;
    }

    static void chunk(MorvainGenerador.Escritor e, int x0, int z0, int[] tops) {
        for (Sitio s : SITIOS) if (toca(s, x0, z0, MEZCLA + 2)) tarima(e, s, x0, z0, tops);
    }

    private static BlockState b(Block x) {
        return x.defaultBlockState();
    }

    private static BlockState santuarioBloque() {
        try {
            return DarkCraftMod.SANTUARIO.get().defaultBlockState();
        } catch (Throwable t) {
            return Blocks.LODESTONE.defaultBlockState();
        }
    }

    /** La piedra de cada region: {losa, losa vieja, anillo, pilar, remate}. */
    private static BlockState[] piedra(Region r) {
        return switch (r) {
            case BOSQUE -> new BlockState[]{b(Blocks.MOSSY_STONE_BRICKS), b(Blocks.MOSSY_COBBLESTONE), b(Blocks.POLISHED_ANDESITE),
                    b(Blocks.MOSSY_STONE_BRICKS), b(Blocks.MOSSY_STONE_BRICK_SLAB)};
            case PANTANO -> new BlockState[]{b(Blocks.MUD_BRICKS), b(Blocks.PACKED_MUD), b(Blocks.MOSSY_STONE_BRICKS), b(Blocks.MUD_BRICKS),
                    b(Blocks.MUD_BRICK_SLAB)};
            case MONTANAS -> new BlockState[]{b(Blocks.STONE_BRICKS), b(Blocks.SNOW_BLOCK), b(Blocks.POLISHED_DIORITE), b(Blocks.STONE_BRICKS),
                    b(Blocks.STONE_BRICK_SLAB)};
            case ACADEMIA -> new BlockState[]{b(Blocks.DEEPSLATE_TILES), b(Blocks.CRACKED_DEEPSLATE_TILES), b(Blocks.AMETHYST_BLOCK),
                    b(Blocks.POLISHED_DEEPSLATE), b(Blocks.DEEPSLATE_TILE_SLAB)};
            case CAPITAL -> new BlockState[]{b(Blocks.POLISHED_ANDESITE), b(Blocks.CRACKED_STONE_BRICKS), b(Blocks.CHISELED_STONE_BRICKS),
                    b(Blocks.STONE_BRICKS), b(Blocks.STONE_BRICK_SLAB)};
            case COSTA -> new BlockState[]{b(Blocks.SMOOTH_SANDSTONE), b(Blocks.SANDSTONE), b(Blocks.BONE_BLOCK), b(Blocks.CUT_SANDSTONE),
                    b(Blocks.SMOOTH_SANDSTONE_SLAB)};
            case SIN_FE -> new BlockState[]{b(Blocks.POLISHED_BLACKSTONE_BRICKS), b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS),
                    b(Blocks.NETHER_BRICKS), b(Blocks.POLISHED_BLACKSTONE_BRICKS), b(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB)};
            case CENTRO -> new BlockState[]{b(Blocks.POLISHED_BASALT), b(Blocks.BASALT), b(Blocks.BLACKSTONE), b(Blocks.POLISHED_BLACKSTONE),
                    b(Blocks.POLISHED_BLACKSTONE_SLAB)};
            default -> new BlockState[]{b(Blocks.STONE_BRICKS), b(Blocks.CRACKED_STONE_BRICKS), b(Blocks.POLISHED_BLACKSTONE_BRICKS),
                    b(Blocks.STONE_BRICKS), b(Blocks.STONE_BRICK_SLAB)};
        };
    }

    /** La tierra que asoma entre las losas y la de debajo, por region. */
    private static BlockState tierra(Region r) {
        return switch (r) {
            case PANTANO -> b(Blocks.MUD);
            case MONTANAS -> b(Blocks.SNOW_BLOCK);
            case COSTA -> b(Blocks.SAND);
            case SIN_FE, CENTRO -> b(Blocks.SOUL_SOIL);
            case ACADEMIA -> b(Blocks.DEEPSLATE);
            default -> b(Blocks.COARSE_DIRT);
        };
    }

    private static void tarima(MorvainGenerador.Escritor e, Sitio s, int x0, int z0, int[] tops) {
        BlockState[] p = piedra(s.region);
        BlockState suelo = tierra(s.region);
        for (int dz = 0; dz < 16; dz++)
            for (int dx = 0; dx < 16; dx++) {
                int x = x0 + dx, z = z0 + dz;
                double r = Math.hypot(x - s.x, z - s.z);
                if (r >= MEZCLA) continue;
                int nat = tops[dz * 16 + dx];
                if (r <= R + 0.5) {
                    allanar(e, x, z, nat, s.y, suelo);
                    int k = Ciudadela.h(x, 11, z);
                    BlockState l = r > R - 0.6 ? p[2] : r < 1.5 ? p[3] : k % 6 == 0 ? p[1] : p[0];
                    e.poner(x, s.y, z, l);
                } else {
                    int h = (int) Math.round(Ruido.lerp(Ruido.suave(R + 0.5, MEZCLA, r), s.y, nat));
                    if (h != nat) allanar(e, x, z, nat, h, suelo);
                    // alguna losa suelta que se ha ido rodando
                    if (r < R + 2.5 && Ciudadela.h(x, 5, z) % 4 == 0) e.poner(x, h, z, p[1]);
                }
            }
        // el santuario (apagado), dos pilares rotos y dos farolas de almas
        e.poner(s.x, s.y + 1, s.z, santuarioBloque());
        int giro = Ciudadela.h(s.x, 1, s.z) % 4;
        for (int i = 0; i < 4; i++) {
            double a = Math.toRadians(45 + 90 * ((i + giro) % 4));
            int px = (int) Math.round(s.x + Math.cos(a) * (R - 0.6)), pz = (int) Math.round(s.z + Math.sin(a) * (R - 0.6));
            if (i < 2) {
                int alto = 2 + Ciudadela.h(px, 2, pz) % 3;
                for (int y = 1; y <= alto; y++) e.poner(px, s.y + y, pz, p[3]);
                if (alto == 4) e.poner(px, s.y + alto + 1, pz, p[4]);
            } else {
                e.poner(px, s.y + 1, pz, b(Blocks.POLISHED_BLACKSTONE));
                e.poner(px, s.y + 2, pz, b(Blocks.POLISHED_BLACKSTONE_WALL));
                e.poner(px, s.y + 3, pz, b(Blocks.SOUL_LANTERN));
            }
        }
    }

    /** Sube o baja la columna hasta h (relleno con la tierra de su region). */
    private static void allanar(MorvainGenerador.Escritor e, int x, int z, int nat, int h, BlockState suelo) {
        for (int y = h + 1; y <= nat; y++) e.poner(x, y, z, b(Blocks.AIR));
        for (int y = nat + 1; y <= h; y++) e.poner(x, y, z, suelo);
    }
}
