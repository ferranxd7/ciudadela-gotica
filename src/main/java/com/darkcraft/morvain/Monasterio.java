package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Helado;
import com.darkcraft.fortaleza.Perdidos;
import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Pal;
import com.darkcraft.pueblo.gen.Trozo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * El Monasterio de la Helada (parte 7k), en las Montanas Nevadas, al noroeste del santuario de la Helada: los monjes que
 * rezaban aqui se quedaron helados cuando el cielo se puso rojo, y los peregrinos que subian a verlos, por el camino. Una
 * <b>terraza</b> cortada en la ladera (por el norte la montana queda por encima de la muralla; por el sur, el muro de
 * contencion y una escalera hasta la puerta); dentro de la <b>muralla</b> almenada: el <b>patio de entrada</b>, el
 * <b>claustro</b> (galerias de arcos alrededor de un patio nevado con la <b>fuente helada</b>), al norte la <b>iglesia</b>
 * (nave alta con tejado de pizarra, bancos, las vidrieras rotas, el altar con su cruz de hielo, el <b>atril con el libro
 * perdido 21</b> y un arcon) y su <b>campanario</b> con la campana rota; al oeste las <b>celdas</b>, al este el
 * <b>refectorio</b> (la mesa larga, el hogar frio, la cocina); en la esquina del noroeste el <b>cementerio</b> de los
 * monjes y en la del noreste el <b>huerto</b> con el pozo helado. Fuera, el <b>camino</b> hasta el santuario.
 * <p>
 * Los helados ({@code enemigos/Helado}) los apunta el propio plano ({@link #esperas}): de rodillas rezando en los bancos, en
 * el claustro, en las celdas y en el refectorio; enterrados en la nieve del patio, del cementerio y del camino; de pie en la
 * puerta y de ronda por las galerias. Se dibuja como la aldea de los pilotes: chunk a chunk al adornar
 * ({@link MorvainGenerador#applyBiomeDecoration}); en su caja {@link Adorno} no pone nada.
 */
public final class Monasterio {
    public static final long SEMILLA = 0x4E1A_DA00_7CL;
    /** El centro del claustro (la fuente). */
    public static final int CX = 3312, CZ = -5452;
    /** El suelo de la terraza (lo que se pisa esta en {@link #PISO}). */
    public static final int SUELO = 294, PISO = SUELO + 1;
    /** La terraza (u = x - CX, v = z - CZ): la muralla va por su borde. */
    static final int U0 = -23, U1 = 23, V0 = -31, V1 = 21;
    /** Lo que toca (la terraza, la escalera y el camino hasta el santuario de la Helada). */
    static final int X0 = CX - 26, X1 = CX + 42, Z0 = CZ - 34, Z1 = CZ + 54;
    /** La altura de los muros: la muralla (y las almenas encima), las alas, la iglesia. */
    static final int ALTO_MURALLA = 6, ALTO_ALA = 5, ALTO_IGLESIA = 11;
    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/monasterio"),
            BOTIN_BUENO = new ResourceLocation(DarkCraftMod.MODID, "chests/monasterio_bueno");

    private static final int AW = X1 - X0 + 1, AD = Z1 - Z0 + 1;
    private static final int[] ALTO = new int[AW * AD];

    static {
        Arrays.fill(ALTO, Integer.MIN_VALUE);
    }

    /** El bloque de arriba del terreno de Morvain en (x, z) (sin el monasterio). */
    public static int alto(int x, int z) {
        if (x < X0 || x > X1 || z < Z0 || z > Z1) return (int) Math.floor(Terreno.columna(x, z).altura());
        int i = (z - Z0) * AW + (x - X0);
        int v = ALTO[i];
        if (v == Integer.MIN_VALUE) ALTO[i] = v = (int) Math.floor(Terreno.columna(x, z).altura());
        return v;
    }

    /** Un helado del monasterio: donde espera (y: lo que pisa), hacia donde mira, su variante, como espera, cuanto ronda y su grupo. */
    public record Espera(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    private static Monasterio unico;

    public static synchronized Monasterio get() {
        if (unico == null) unico = new Monasterio();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    /** Lo que hay (para la prueba y las vistas). */
    public int bancos, ventanas, tumbas, escalones;

    private Monasterio() {
        elementos.add(new Terraza());
        Pilotes.Lista m = new Pilotes.Lista();
        claustro(m);
        murallas(m);
        puerta(m);
        iglesia(m);
        campanario(m);
        celdas(m);
        refectorio(m);
        cementerio(m);
        huerto(m);
        elementos.add(m.pieza(3));
        Pilotes.Lista c = new Pilotes.Lista();
        camino(c);
        elementos.add(c.pieza(2));
        trozos();
        // ---- los sitios con nombre
        sitio("entrada", 33, pie(CX + 33, CZ + 45), 45);
        sitio("escalera", 0, pie(CX, CZ + 22 + escalones + 1), 22 + escalones + 1);
        sitio("puerta", 0, PISO, 21);
        sitio("patio", 0, PISO, 16);
        sitio("claustro", -5, PISO, 6);
        sitio("fuente", 0, PISO, 5);
        sitio("iglesia", 0, PISO, -14);
        sitio("altar", 0, PISO + 1, -28);
        sitio("atril", 4, PISO + 1, -28);
        sitio("arcon", -4, PISO + 1, -30);
        sitio("campana", -12, SUELO + 20, -29);
        sitio("celdas", -14, PISO, 0);
        sitio("refectorio", 15, PISO, 0);
        sitio("cementerio", -18, PISO, -12);
        sitio("huerto", 16, PISO, -24);
        // ---- los helados
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo del monasterio (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en lo del monasterio: ahi {@link Adorno} no pone nada. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= X0 && x0 <= X1 && z0 + 15 >= Z0 && z0 <= Z1;
    }

    /** Si (x, z) esta en el monasterio (para el nombre del sitio y su logro). */
    public static boolean dentro(double x, double z) {
        double u = x - CX, v = z - CZ;
        return u >= U0 - 1 && u <= U1 + 1 && v >= V0 - 1 && v <= V1 + 6;
    }

    static void chunk(WorldGenLevel nivel, ChunkAccess c) {
        int cx = c.getPos().x, cz = c.getPos().z;
        get().construir(nivel, cx, cz, RandomSource.create(Velmor.clave(cx, cz) ^ SEMILLA));
    }

    private final Set<String> avisados = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public void construir(WorldGenLevel nivel, int chunkX, int chunkZ, RandomSource r) {
        List<Element> l = indice.get(Velmor.clave(chunkX, chunkZ));
        if (l == null) return;
        Canvas c = new Canvas(nivel, chunkX, chunkZ, r);
        for (Element e : l) {
            try {
                e.build(c);
            } catch (Throwable t) {
                String k = e.getClass().getSimpleName();
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} del monasterio en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los helados del monasterio (los apunta {@link MorvainEnemigos}). */
    public List<Espera> esperas() {
        return esperas;
    }

    /** Los sitios con nombre (la puerta, el claustro, la iglesia, el atril, el campanario...): {x, y, z} (y: lo que se pisa). */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    private void sitio(String nombre, int u, int y, int v) {
        sitios.put(nombre, new int[]{CX + u, y, CZ + v});
    }

    /** Lo que se pisa en (x, z) fuera de la terraza (el terreno). */
    static int pie(int x, int z) {
        return alto(x, z) + 1;
    }

    // ------------------------------------------------------------------ bloques

    static BlockState b(Block k) {
        return k.defaultBlockState();
    }

    static BlockState escalera(Block k, Direction f, boolean alReves) {
        return k.defaultBlockState().setValue(StairBlock.FACING, f).setValue(StairBlock.HALF, alReves ? Half.TOP : Half.BOTTOM);
    }

    static BlockState losa(Block k, SlabType t) {
        return k.defaultBlockState().setValue(SlabBlock.TYPE, t);
    }

    static BlockState nieve(int capas) {
        return Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, Math.max(1, Math.min(8, capas)));
    }

    static BlockState vela(int n, boolean encendida) {
        return Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, n).setValue(CandleBlock.LIT, encendida);
    }

    /** La piedra de los muros: ladrillo de piedra con alguno agrietado, andesita y piedra. */
    static BlockState piedra(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 3);
        if (p < 58) return Pal.STONE_BRICKS;
        if (p < 76) return b(Blocks.CRACKED_STONE_BRICKS);
        if (p < 86) return Pal.ANDESITE;
        if (p < 93) return b(Blocks.POLISHED_ANDESITE);
        return Pal.STONE;
    }

    private static int x(int u) {
        return CX + u;
    }

    private static int z(int v) {
        return CZ + v;
    }

    private static void put(Pilotes.Lista m, int u, int y, int v, BlockState s) {
        m.put(x(u), y, z(v), s);
    }

    private static void une(Pilotes.Lista m, int u, int y, int v, BlockState s) {
        m.une(x(u), y, z(v), s);
    }

    /** Un muro de piedra de (u0, v0) a (u1, v1) (una fila recta) de y0 a y1. */
    private static void muro(Pilotes.Lista m, int u0, int v0, int u1, int v1, int y0, int y1) {
        for (int u = Math.min(u0, u1); u <= Math.max(u0, u1); u++)
            for (int v = Math.min(v0, v1); v <= Math.max(v0, v1); v++)
                for (int y = y0; y <= y1; y++) put(m, u, y, v, piedra(x(u), y, z(v)));
    }

    /** Lo mas bajo del terreno bajo una fila de la muralla (para que el muro baje hasta el). */
    private static int base(int u, int v) {
        return Math.min(SUELO, alto(x(u), z(v))) - 1;
    }

    /**
     * Un tejado a dos aguas de pizarra (escalones de losa de pizarra) sobre u0..u1 (con los aleros incluidos), con la
     * cumbrera a lo largo de v (de v0 a v1), que empieza en y0 en los aleros; los hastiales (v0 y v1) se cierran de piedra
     * hasta el tejado si {@code hastiales}. Nieve encima.
     */
    private void tejado(Pilotes.Lista m, int u0, int u1, int v0, int v1, int y0, boolean hastiales, int vHastial0, int vHastial1) {
        for (int u = u0; u <= u1; u++) {
            int a = u - u0, bb = u1 - u;
            int h = y0 + Math.min(a, bb);
            for (int v = v0; v <= v1; v++) {
                BlockState s;
                if (a == bb) s = losa(Blocks.DEEPSLATE_TILE_SLAB, SlabType.DOUBLE);
                else s = escalera(Blocks.DEEPSLATE_TILE_STAIRS, a < bb ? Direction.EAST : Direction.WEST, false);
                une(m, u, h, v, s);
                if (Hash.pct(x(u), h, z(v), SEMILLA + 9) < 70) put(m, u, h + 1, v, nieve(1 + Hash.pct(x(u), h, z(v), SEMILLA + 10) % 2));
            }
            if (hastiales)
                for (int y = y0 + 1; y < h; y++) {
                    put(m, u, y, vHastial0, piedra(x(u), y, z(vHastial0)));
                    put(m, u, y, vHastial1, piedra(x(u), y, z(vHastial1)));
                }
        }
    }

    // ------------------------------------------------------------------ las zonas de la terraza

    enum Zona { IGLESIA, CLAUSTRO, PATIO, ENTRADA, CELDAS, REFECTORIO, CEMENTERIO, HUERTO, MURO }

    static Zona zona(int u, int v) {
        if (u <= U0 || u >= U1 || v <= V0 || v >= V1) return Zona.MURO;
        if (u >= -10 && u <= 10 && v <= -12) return Zona.IGLESIA;
        if (u <= -11 && v <= -10) return Zona.CEMENTERIO;
        if (u >= 11 && v <= -10) return Zona.HUERTO;
        if (u <= -12 && v <= 12) return Zona.CELDAS;
        if (u >= 12 && v <= 12) return Zona.REFECTORIO;
        if (u >= -8 && u <= 8 && v >= -8 && v <= 9) return Zona.PATIO;
        if (v >= 14) return Zona.ENTRADA;
        return Zona.CLAUSTRO;
    }

    /** Si en (u, v) se esta al aire libre (con nieve en el suelo). */
    static boolean abierto(Zona z) {
        return z == Zona.PATIO || z == Zona.ENTRADA || z == Zona.CEMENTERIO || z == Zona.HUERTO;
    }

    /**
     * La terraza: corta la ladera hasta el suelo (por encima, aire) y rellena por debajo; el suelo de cada zona (nieve al aire
     * libre, losas en el claustro y la iglesia, tablas en las alas) con la nieve encima donde no hay techo.
     */
    private final class Terraza extends Element {
        Terraza() {
            super(SEMILLA + 51);
            this.layer = 1;
            bounds(x(U0), 200, z(V0), x(U1), 360, z(V1 + 1));
        }

        @Override
        public void build(Canvas c) {
            for (int x = c.minX; x <= c.maxX; x++)
                for (int z = c.minZ; z <= c.maxZ; z++) {
                    int u = x - CX, v = z - CZ;
                    if (u < U0 || u > U1 || v < V0 || v > V1) continue;
                    int t = alto(x, z);
                    for (int y = SUELO + 1; y <= Math.max(t, SUELO) + 6; y++) c.set(x, y, z, Pal.AIR);
                    for (int y = t + 1; y < SUELO; y++) c.set(x, y, z, Pal.STONE);
                    Zona zn = zona(u, v);
                    int p = Hash.pct(x, SUELO, z, seed);
                    BlockState s = switch (zn) {
                        case PATIO, ENTRADA, CEMENTERIO, HUERTO -> b(Blocks.SNOW_BLOCK);
                        case IGLESIA -> (u + v & 1) == 0 ? b(Blocks.POLISHED_ANDESITE) : p < 12 ? b(Blocks.PACKED_ICE) : Pal.STONE_BRICKS;
                        case CELDAS, REFECTORIO -> p < 8 ? b(Blocks.SNOW_BLOCK) : Pal.SP_PLANKS;
                        default -> p < 70 ? Pal.STONE_BRICKS : p < 90 ? b(Blocks.CRACKED_STONE_BRICKS) : b(Blocks.PACKED_ICE);
                    };
                    c.set(x, SUELO, z, s);
                    if (abierto(zn)) {
                        // nieve encima: una capa (no estorba al andar), algo mas junto a los muros
                        boolean junto = u <= U0 + 1 || u >= U1 - 1 || v <= V0 + 1 || v >= V1 - 1;
                        if (p < 88) c.set(x, SUELO + 1, z, nieve(junto && p < 50 ? 2 : 1));
                    }
                }
        }
    }

    // ------------------------------------------------------------------ la muralla y la puerta

    private void murallas(Pilotes.Lista m) {
        for (int u = U0; u <= U1; u++)
            for (int v = V0; v <= V1; v++) {
                if (u != U0 && u != U1 && v != V0 && v != V1) continue;
                // la iglesia y el campanario ponen su muro del norte; la puerta, el suyo
                if (v == V0 && u >= -14 && u <= 10) continue;
                if (v == V1 && u >= -3 && u <= 3) continue;
                // donde estan las alas, la muralla es su muro de atras (y el tejado va encima)
                boolean ala = (u == U0 || u == U1) && v >= -9 && v <= 12;
                int alto = ala ? ALTO_ALA : ALTO_MURALLA;
                for (int y = base(u, v); y <= SUELO + alto; y++) put(m, u, y, v, piedra(x(u), y, z(v)));
                if (ala) continue;
                // las almenas, con nieve; en las esquinas, un pilar mas alto con su remate
                boolean esquina = (u == U0 || u == U1) && (v == V0 || v == V1);
                if (esquina) {
                    for (int y = SUELO + alto + 1; y <= SUELO + alto + 3; y++) put(m, u, y, v, Pal.STONE_BRICKS);
                    put(m, u, SUELO + alto + 4, v, losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
                } else if ((u + v & 1) == 0) {
                    put(m, u, SUELO + alto + 1, v, piedra(x(u), SUELO + alto + 1, z(v)));
                    put(m, u, SUELO + alto + 2, v, nieve(2));
                } else put(m, u, SUELO + alto + 1, v, nieve(1));
                // carambanos bajo el canto de fuera, aqui y alla
                if (Hash.pct(x(u), 7, z(v), SEMILLA + 11) < 7) {
                    int du = u == U0 ? -1 : u == U1 ? 1 : 0, dv = v == V0 ? -1 : v == V1 ? 1 : 0;
                    if (SUELO + alto - 1 > alto(x(u + du), z(v + dv)) + 2) put(m, u + du, SUELO + alto - 1, v + dv, b(Blocks.ICE));
                }
            }
        // contrafuertes por fuera del muro del sur (donde la terraza queda en alto)
        for (int u = U0 + 4; u <= U1 - 4; u += 6) {
            if (Math.abs(u) <= 4) continue;
            for (int y = base(u, V1 + 1); y <= SUELO + 2; y++) put(m, u, y, V1 + 1, piedra(x(u), y, z(V1 + 1)));
            put(m, u, SUELO + 3, V1 + 1, escalera(Blocks.STONE_BRICK_STAIRS, Direction.NORTH, false));
        }
    }

    /** La puerta del sur: un cuerpo de puerta con arco y almenas, faroles a los lados y la escalera que baja al camino. */
    private void puerta(Pilotes.Lista m) {
        for (int v = V1 - 1; v <= V1 + 1; v++) {
            for (int u = -3; u <= 3; u++) {
                int y0 = Math.min(base(u, v), base(u, V1));
                int tope = Math.abs(u) == 3 ? SUELO + 9 : SUELO + 8;
                for (int y = y0; y <= tope; y++) {
                    boolean paso = Math.abs(u) <= 1 && y >= SUELO + 1 && y <= SUELO + 3 || u == 0 && y == SUELO + 4 && v != V1;
                    if (paso) put(m, u, y, v, Pal.AIR);
                    else if (Math.abs(u) <= 1 && y == SUELO) put(m, u, y, v, Pal.STONE_BRICKS);
                    else put(m, u, y, v, piedra(x(u), y, z(v)));
                }
                // en el centro del paso, el techo un bloque mas arriba
                if (v == V1 && Math.abs(u) <= 1) put(m, u, SUELO + 4, v, Pal.AIR);
            }
            // el arco de las dos caras
            if (v != V1) {
                put(m, -1, SUELO + 4, v, escalera(Blocks.STONE_BRICK_STAIRS, Direction.WEST, true));
                put(m, 1, SUELO + 4, v, escalera(Blocks.STONE_BRICK_STAIRS, Direction.EAST, true));
            }
            // almenas y nieve
            for (int u = -3; u <= 3; u++) {
                int tope = Math.abs(u) == 3 ? SUELO + 9 : SUELO + 8;
                if ((u & 1) == 0 && v != V1) {
                    put(m, u, tope + 1, v, piedra(x(u), tope + 1, z(v)));
                    put(m, u, tope + 2, v, nieve(2));
                } else put(m, u, tope + 1, v, nieve(1));
            }
        }
        // el escudo del monasterio sobre el arco (una cruz de hielo en la piedra)
        put(m, 0, SUELO + 6, V1 + 1, b(Blocks.PACKED_ICE));
        put(m, 0, SUELO + 7, V1 + 1, b(Blocks.PACKED_ICE));
        put(m, -1, SUELO + 7, V1 + 1, b(Blocks.PACKED_ICE));
        put(m, 1, SUELO + 7, V1 + 1, b(Blocks.PACKED_ICE));
        put(m, 0, SUELO + 8, V1 + 1, b(Blocks.PACKED_ICE));
        // la escalera: de la puerta al terreno, tres de ancho, con pretil de piedra a los lados
        int n = 0;
        for (int i = 1; i <= 8; i++) {
            int v = V1 + 1 + i, y = SUELO - i;
            int t = Math.max(alto(x(-1), z(v)), Math.max(alto(x(0), z(v)), alto(x(1), z(v))));
            if (y <= t) break;
            n = i;
            for (int u = -2; u <= 2; u++) {
                int tu = alto(x(u), z(v));
                for (int yy = tu + 1; yy < y; yy++) put(m, u, yy, v, Pal.STONE);
                if (Math.abs(u) <= 1) {
                    put(m, u, y, v, escalera(Blocks.STONE_BRICK_STAIRS, Direction.NORTH, false));
                    for (int yy = y + 1; yy <= y + 3; yy++) put(m, u, yy, v, Pal.AIR);
                } else {
                    put(m, u, y, v, piedra(x(u), y, z(v)));
                    une(m, u, y + 1, v, b(Blocks.STONE_BRICK_WALL));
                }
            }
        }
        escalones = n;
        // dos faroles al pie de la escalera
        int vp = V1 + 2 + n;
        for (int u : new int[]{-2, 2}) {
            int t = alto(x(u), z(vp));
            une(m, u, t + 1, vp, Pal.SP_FENCE);
            une(m, u, t + 2, vp, Pal.SP_FENCE);
            put(m, u, t + 3, vp, Pal.LANTERN);
        }
    }

    // ------------------------------------------------------------------ la iglesia y el campanario

    private void iglesia(Pilotes.Lista m) {
        int top = SUELO + ALTO_IGLESIA;
        // los muros: los lados (u = -10 y 10) y los hastiales (v = -31, el de la muralla, y v = -12, el de la puerta)
        for (int v = V0; v <= -12; v++)
            for (int u : new int[]{-10, 10}) for (int y = base(u, v); y <= top; y++) put(m, u, y, v, piedra(x(u), y, z(v)));
        for (int u = -9; u <= 9; u++)
            for (int v : new int[]{V0, -12}) for (int y = base(u, v); y <= top; y++) put(m, u, y, v, piedra(x(u), y, z(v)));
        // contrafuertes en los lados, entre las ventanas
        for (int v = -30; v <= -13; v += 4)
            for (int u : new int[]{-11, 11}) {
                if (u == -11 && v <= -27) continue;   // ahi esta el campanario
                for (int y = base(u, v); y <= SUELO + 6; y++) put(m, u, y, v, piedra(x(u), y, z(v)));
                put(m, u, SUELO + 7, v, escalera(Blocks.STONE_BRICK_STAIRS, u < 0 ? Direction.EAST : Direction.WEST, false));
            }
        // el tejado (con los hastiales)
        tejado(m, -11, 11, V0 - 1, -11, top, true, V0, -12);
        // las ventanas altas de los lados: vidrieras azules, alguna rota (con nieve dentro, debajo)
        for (int v = -28; v <= -14; v += 4)
            for (int u : new int[]{-10, 10}) {
                boolean rota = Hash.pct(x(u), 3, z(v), SEMILLA + 21) < 35;
                for (int y = SUELO + 4; y <= SUELO + 8; y++) {
                    if (rota && y <= SUELO + 6) put(m, u, y, v, Pal.AIR);
                    else une(m, u, y, v, b(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE));
                }
                ventanas++;
                if (rota) {
                    int du = u < 0 ? 1 : -1;
                    put(m, u + du, PISO, v, nieve(3));
                    put(m, u + du, PISO, v - 1, nieve(2));
                    put(m, u + du, PISO, v + 1, nieve(2));
                    put(m, u + du * 2, PISO, v, nieve(1));
                }
                // carambanos bajo el alfeizar, por fuera
                int fu = u < 0 ? -1 : 1;
                if (!(u == -10 && v <= -27)) put(m, u + fu, SUELO + 3, v, b(Blocks.ICE));
            }
        // la puerta (en el hastial del sur) con su arco, y encima el rosetón
        for (int u = -1; u <= 1; u++) for (int y = PISO; y <= SUELO + 3; y++) put(m, u, y, -12, Pal.AIR);
        put(m, 0, SUELO + 4, -12, Pal.AIR);
        put(m, -1, SUELO + 4, -12, escalera(Blocks.STONE_BRICK_STAIRS, Direction.WEST, true));
        put(m, 1, SUELO + 4, -12, escalera(Blocks.STONE_BRICK_STAIRS, Direction.EAST, true));
        for (int[] q : new int[][]{{0, 7}, {0, 8}, {0, 9}, {-1, 8}, {1, 8}}) une(m, q[0], SUELO + q[1], -12, b(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE));
        // dentro: el pasillo, los bancos (mirando al altar), las lamparas colgando de la cumbrera
        for (int v = -24; v <= -15; v++) {
            int p = Hash.pct(x(0), PISO, z(v), SEMILLA + 23);
            if (p < 80) for (int u = -1; u <= 1; u++) put(m, u, PISO, v, b(Blocks.RED_CARPET));
        }
        for (int v = -25; v <= -15; v += 2)
            for (int u = -8; u <= 8; u++) {
                if (Math.abs(u) < 3) continue;
                if (Hash.pct(x(u), PISO, z(v), SEMILLA + 24) < 8) continue;   // alguno roto
                put(m, u, PISO, v, escalera(Blocks.SPRUCE_STAIRS, Direction.SOUTH, false));
                bancos++;
            }
        for (int v : new int[]{-24, -17}) {
            int yc = SUELO + 21 - 1;
            for (int y = SUELO + 12; y <= yc; y++) put(m, 0, y, v, Pal.CHAIN);
            put(m, 0, SUELO + 11, v, Pal.LANTERN_HANG);
        }
        // el presbiterio: una grada (escalones) y el suelo alzado; el altar con velas y la cruz de hielo en el muro
        for (int u = -9; u <= 9; u++) {
            put(m, u, PISO, -26, escalera(Blocks.POLISHED_ANDESITE_STAIRS, Direction.NORTH, false));
            for (int v = -30; v <= -27; v++) put(m, u, PISO, v, (u + v & 1) == 0 ? b(Blocks.POLISHED_ANDESITE) : b(Blocks.POLISHED_DIORITE));
        }
        for (int u = -2; u <= 2; u++) {
            put(m, u, PISO + 1, -29, Math.abs(u) == 2 ? b(Blocks.CHISELED_STONE_BRICKS) : b(Blocks.SMOOTH_STONE));
            if ((u & 1) == 0) put(m, u, PISO + 2, -29, vela(u == 0 ? 4 : 3, true));
        }
        for (int y = PISO + 2; y <= SUELO + 9; y++) put(m, 0, y, -30, b(Blocks.PACKED_ICE));
        for (int u = -2; u <= 2; u++) put(m, u, SUELO + 7, -30, b(Blocks.PACKED_ICE));
        put(m, 0, SUELO + 10, -30, b(Blocks.BLUE_ICE));
        // velas en el suelo, junto a los bancos de delante
        put(m, -9, PISO, -25, vela(2, true));
        put(m, 9, PISO, -25, vela(2, true));
    }

    /** El campanario, en la esquina del noroeste de la iglesia: la campana rota arriba, entre cuatro arcos. */
    private void campanario(Pilotes.Lista m) {
        int u0 = -14, u1 = -10, v0 = V0, v1 = V0 + 4;
        int sala = SUELO + 17, cubierta = SUELO + 21;
        for (int u = u0; u <= u1; u++)
            for (int v = v0; v <= v1; v++) {
                boolean borde = u == u0 || u == u1 || v == v0 || v == v1;
                if (!borde) {
                    put(m, u, sala, v, Pal.STONE_BRICKS);
                    put(m, u, cubierta, v, Pal.STONE_BRICKS);
                    continue;
                }
                for (int y = base(u, v); y <= cubierta; y++) {
                    boolean esquina = (u == u0 || u == u1) && (v == v0 || v == v1);
                    boolean hueco = !esquina && y > sala && y < cubierta && !(u == u1 && v > v0 && v < v1 && y < SUELO + ALTO_IGLESIA + 10);
                    put(m, u, y, v, hueco ? Pal.AIR : piedra(x(u), y, z(v)));
                }
                // el arco de cada hueco (la fila de arriba)
            }
        for (int k = -1; k <= 1; k += 2) {
            put(m, -12 + k, cubierta - 1, v0, escalera(Blocks.STONE_BRICK_STAIRS, k < 0 ? Direction.WEST : Direction.EAST, true));
            put(m, -12 + k, cubierta - 1, v1, escalera(Blocks.STONE_BRICK_STAIRS, k < 0 ? Direction.WEST : Direction.EAST, true));
            put(m, u0, cubierta - 1, v0 + 2 + k, escalera(Blocks.STONE_BRICK_STAIRS, k < 0 ? Direction.NORTH : Direction.SOUTH, true));
        }
        // el chapitel: tres hiladas de pizarra y la cruz de hierro
        for (int k = 0; k <= 2; k++) {
            int y = cubierta + 1 + k;
            for (int u = u0 + k; u <= u1 - k; u++)
                for (int v = v0 + k; v <= v1 - k; v++) {
                    boolean borde = u == u0 + k || u == u1 - k || v == v0 + k || v == v1 - k;
                    if (!borde || u0 + k == u1 - k) {
                        put(m, u, y, v, Pal.DS_TILES);
                        continue;
                    }
                    Direction f = u == u0 + k ? Direction.EAST : u == u1 - k ? Direction.WEST : v == v0 + k ? Direction.SOUTH : Direction.NORTH;
                    une(m, u, y, v, escalera(Blocks.DEEPSLATE_TILE_STAIRS, f, false));
                }
        }
        for (int y = cubierta + 4; y <= cubierta + 6; y++) une(m, -12, y, v0 + 2, Pal.IRON_BARS);
        une(m, -13, cubierta + 5, v0 + 2, Pal.IRON_BARS);
        une(m, -11, cubierta + 5, v0 + 2, Pal.IRON_BARS);
        // nieve en la sala de la campana (entra por los arcos)
        put(m, -13, sala + 1, v0 + 1, nieve(2));
        put(m, -11, sala + 1, v0 + 3, nieve(1));
    }

    // ------------------------------------------------------------------ el claustro y la fuente

    private void claustro(Pilotes.Lista m) {
        int techo = SUELO + 5;
        // las cuatro arquerias: columnas cada tres, arcos entre ellas y el arquitrabe encima
        arqueria(m, -9, -9, 9, -9, true, 2, 0);     // la del norte (abierta ante la puerta de la iglesia)
        arqueria(m, -9, 10, 9, 10, true, 2, 0);     // la del sur (abierta hacia la entrada)
        arqueria(m, -9, -9, -9, 10, false, 0, 0);   // la del oeste
        arqueria(m, 9, -9, 9, 10, false, 0, 0);     // la del este
        arqueria(m, -11, 13, 11, 13, true, 1, 2);   // el portico que da al patio de entrada
        // los techos de las galerias: losas de pizarra con nieve
        for (int u = -11; u <= 11; u++)
            for (int v = -11; v <= 13; v++) {
                boolean galeria = v <= -10 || v >= 11 || u <= -10 || u >= 10;
                if (!galeria) continue;
                if (v == 13 || v == 10 && Math.abs(u) <= 9 || v == -9 && Math.abs(u) <= 9 || Math.abs(u) == 9 && v >= -9 && v <= 10) continue;
                put(m, u, techo, v, losa(Blocks.DEEPSLATE_TILE_SLAB, SlabType.BOTTOM));
                if (Hash.pct(x(u), techo, z(v), SEMILLA + 31) < 75) put(m, u, techo + 1, v, nieve(1));
            }
        // faroles colgando bajo las galerias
        for (int[] q : new int[][]{{-10, -10}, {10, -10}, {-10, 12}, {10, 12}, {0, -10}, {0, 12}, {-10, 0}, {10, 0}, {-6, 12}, {6, 12}})
            put(m, q[0], techo - 1, q[1], Pal.LANTERN_HANG);
        // la fuente helada en medio del patio
        for (int u = -2; u <= 2; u++)
            for (int v = -1; v <= 3; v++) {
                boolean borde = Math.abs(u) == 2 || v == -1 || v == 3;
                if (borde) {
                    put(m, u, SUELO, v, Pal.STONE_BRICKS);
                    put(m, u, PISO, v, losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
                } else put(m, u, SUELO, v, b(Blocks.ICE));
            }
        put(m, 0, PISO, 1, b(Blocks.CHISELED_STONE_BRICKS));
        une(m, 0, PISO + 1, 1, b(Blocks.STONE_BRICK_WALL));
        put(m, 0, PISO + 2, 1, losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
        put(m, 0, PISO + 3, 1, b(Blocks.PACKED_ICE));
        put(m, 0, PISO + 4, 1, b(Blocks.BLUE_ICE));
        for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            put(m, d[0], PISO + 2, 1 + d[1], b(Blocks.ICE));
            put(m, d[0], PISO + 1, 1 + d[1], b(Blocks.ICE));
        }
        // arbustos secos asomando de la nieve en los rincones del patio
        for (int[] q : new int[][]{{-7, -7}, {7, -7}, {-7, 8}, {7, 8}, {-5, -4}, {6, 5}}) put(m, q[0], PISO, q[1], b(Blocks.DEAD_BUSH));
    }

    /**
     * Una arqueria recta de (u0, v0) a (u1, v1) (a lo largo de u si {@code enU}): columnas (muretes) donde |k| % 3 == fase y
     * en los extremos, con su capitel; arcos (escalones del reves) entre ellas y el arquitrabe corrido encima. {@code hueco}:
     * el paso del medio que queda abierto (0, ninguno; 1, tres de ancho; 2, cinco de ancho).
     */
    private static void arqueria(Pilotes.Lista m, int u0, int v0, int u1, int v1, boolean enU, int hueco, int fase) {
        int a = enU ? u0 : v0, bb = enU ? u1 : v1;
        int techo = SUELO + 5;
        for (int k = a; k <= bb; k++) {
            int u = enU ? k : u0, v = enU ? v0 : k;
            put(m, u, techo, v, piedra(x(u), techo, z(v)));
            if (Math.abs(k) <= hueco) continue;
            if (columna(k, a, bb, hueco, fase)) {
                for (int y = PISO; y <= SUELO + 3; y++) une(m, u, y, v, b(Blocks.STONE_BRICK_WALL));
                put(m, u, SUELO + 4, v, Pal.STONE_BRICKS);
            } else {
                // el arco: el escalon del reves con su lado lleno hacia la columna de al lado
                boolean antes = columna(k - 1, a, bb, hueco, fase);
                Direction f = enU ? (antes ? Direction.WEST : Direction.EAST) : (antes ? Direction.NORTH : Direction.SOUTH);
                put(m, u, SUELO + 4, v, escalera(Blocks.STONE_BRICK_STAIRS, f, true));
            }
        }
    }

    private static boolean columna(int k, int a, int bb, int hueco, int fase) {
        if (Math.abs(k) <= hueco) return false;
        return k == a || k == bb || Math.abs(k) % 3 == fase;
    }

    // ------------------------------------------------------------------ las celdas y el refectorio

    private void celdas(Pilotes.Lista m) {
        int v0 = -9, v1 = 12, u1 = -12, top = SUELO + ALTO_ALA;
        // los muros: el del claustro (u = -12) y los del norte y el sur; el de atras es la muralla
        for (int v = v0; v <= v1; v++) for (int y = base(u1, v); y <= top; y++) put(m, u1, y, v, piedra(x(u1), y, z(v)));
        for (int u = U0 + 1; u < u1; u++)
            for (int v : new int[]{v0, v1}) for (int y = base(u, v); y <= top; y++) put(m, u, y, v, piedra(x(u), y, z(v)));
        tejado(m, U0 - 1, u1 + 1, v0 - 1, v1 + 1, SUELO + ALTO_ALA, true, v0, v1);
        // la puerta del claustro
        for (int v = 0; v <= 1; v++) for (int y = PISO; y <= SUELO + 3; y++) put(m, u1, y, v, Pal.AIR);
        // el pasillo (u -15..-13) y las cuatro celdas (u -22..-17) con su muro (u = -16) y sus tabiques
        int[][] celdas = {{-8, -5}, {-3, 0}, {2, 5}, {7, 11}};
        for (int v = v0 + 1; v < v1; v++) for (int y = PISO; y <= top; y++) put(m, -16, y, v, piedra(x(-16), y, z(v)));
        for (int v : new int[]{-4, 1, 6}) for (int u = U0 + 1; u < -16; u++) for (int y = PISO; y <= top; y++) put(m, u, y, v, piedra(x(u), y, z(v)));
        for (int i = 0; i < celdas.length; i++) {
            int va = celdas[i][0], vb = celdas[i][1], vm = (va + vb) / 2;
            // la puerta (sin hoja: se la llevo el frio)
            put(m, -16, PISO, vm, Pal.AIR);
            put(m, -16, PISO + 1, vm, Pal.AIR);
            // el jergon (dos de alfombra blanca), una vela y, en dos de ellas, un barril
            put(m, -22, PISO, va, b(Blocks.WHITE_CARPET));
            put(m, -22, PISO, va + 1, b(Blocks.WHITE_CARPET));
            put(m, -21, PISO, va, vela(1, i % 2 == 0));
            put(m, -22, PISO, vb, nieve(2));
            // el ventanuco con barrotes, en la muralla
            une(m, U0, SUELO + 3, vm, Pal.IRON_BARS);
        }
        // faroles en el pasillo
        put(m, -14, top, -6, Pal.LANTERN_HANG);
        put(m, -14, top, 4, Pal.LANTERN_HANG);
    }

    private void refectorio(Pilotes.Lista m) {
        int v0 = -9, v1 = 12, u0 = 12, top = SUELO + ALTO_ALA;
        for (int v = v0; v <= v1; v++) for (int y = base(u0, v); y <= top; y++) put(m, u0, y, v, piedra(x(u0), y, z(v)));
        for (int u = u0 + 1; u < U1; u++)
            for (int v : new int[]{v0, v1}) for (int y = base(u, v); y <= top; y++) put(m, u, y, v, piedra(x(u), y, z(v)));
        tejado(m, u0 - 1, U1 + 1, v0 - 1, v1 + 1, SUELO + ALTO_ALA, true, v0, v1);
        for (int v = 0; v <= 1; v++) for (int y = PISO; y <= SUELO + 3; y++) put(m, u0, y, v, Pal.AIR);
        // la mesa larga (tablero de losas, patas de tronco en los extremos) y los bancos
        for (int v = -5; v <= 8; v++) {
            for (int u = 17; u <= 18; u++) {
                if (v == -5 || v == 8) put(m, u, PISO, v, b(Blocks.STRIPPED_SPRUCE_LOG));
                else put(m, u, PISO, v, losa(Blocks.SPRUCE_SLAB, SlabType.TOP));
            }
            if (v > -5 && v < 8) {
                put(m, 16, PISO, v, losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
                put(m, 19, PISO, v, losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
            }
        }
        put(m, 17, PISO + 1, -2, vela(3, true));
        put(m, 18, PISO + 1, 3, vela(2, true));
        put(m, 17, PISO + 1, 6, vela(1, false));
        // el hogar frio en el muro del norte, con su chimenea
        for (int u = 16; u <= 19; u++) {
            put(m, u, PISO, -8, u == 16 || u == 19 ? b(Blocks.COBBLESTONE) : b(Blocks.CAMPFIRE).setValue(CampfireBlock.LIT, false));
            put(m, u, PISO + 2, -8, u == 16 || u == 19 ? b(Blocks.COBBLESTONE) : losa(Blocks.COBBLESTONE_SLAB, SlabType.TOP));
        }
        for (int y = PISO; y <= SUELO + 13; y++) for (int u = 17; u <= 18; u++) put(m, u, y, -9, y <= PISO + 2 ? b(Blocks.COBBLESTONE) : piedra(x(u), y, z(-9)));
        put(m, 17, SUELO + 14, -9, nieve(2));
        put(m, 18, SUELO + 14, -9, nieve(1));
        // la cocina: barriles y un caldero lleno de nieve en polvo
        put(m, 22, PISO, 9, b(Blocks.POWDER_SNOW_CAULDRON).setValue(LayeredCauldronBlock.LEVEL, 3));
        // los ventanucos de la muralla
        for (int v = -5; v <= 10; v += 5) une(m, U1, SUELO + 3, v, Pal.IRON_BARS);
        // faroles
        put(m, 15, top, -3, Pal.LANTERN_HANG);
        put(m, 20, top, 5, Pal.LANTERN_HANG);
    }

    // ------------------------------------------------------------------ el cementerio y el huerto

    private void cementerio(Pilotes.Lista m) {
        // tres filas de tumbas: la lapida (murete) y el monton de nieve delante
        for (int v = -28; v <= -14; v += 5)
            for (int u = -21; u <= -15; u += 3) {
                if (u >= -15 && v <= -26) continue;   // junto al campanario
                if (Hash.pct(x(u), 5, z(v), SEMILLA + 41) < 15) continue;
                une(m, u, PISO, v, b(Blocks.STONE_BRICK_WALL));
                if (Hash.pct(x(u), 6, z(v), SEMILLA + 41) < 50) put(m, u, PISO + 1, v, nieve(1));
                put(m, u, PISO, v + 1, nieve(3));
                put(m, u, PISO, v + 2, nieve(2));
                tumbas++;
            }
        // un arbol muerto
        int ua = -13, va = -17;
        for (int y = PISO; y <= PISO + 5; y++) put(m, ua, y, va, Pal.SP_LOG);
        put(m, ua - 1, PISO + 4, va, Pal.SP_LOG.setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
        put(m, ua - 2, PISO + 5, va, Pal.SP_LOG.setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
        put(m, ua, PISO + 3, va + 1, Pal.SP_LOG.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
        put(m, ua, PISO + 6, va, nieve(1));
    }

    private void huerto(Pilotes.Lista m) {
        // el pozo helado
        int up = 16, vp = -20;
        for (int du = -1; du <= 1; du++)
            for (int dv = -1; dv <= 1; dv++) {
                if (du == 0 && dv == 0) {
                    put(m, up, SUELO, vp, b(Blocks.ICE));
                    put(m, up, SUELO - 1, vp, Pal.WATER);
                    continue;
                }
                put(m, up + du, PISO, vp + dv, Pal.STONE_BRICKS);
            }
        for (int dv : new int[]{-1, 1}) {
            une(m, up, PISO + 1, vp + dv, Pal.SP_FENCE);
            une(m, up, PISO + 2, vp + dv, Pal.SP_FENCE);
        }
        put(m, up, PISO + 3, vp - 1, losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
        put(m, up, PISO + 3, vp, losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
        put(m, up, PISO + 3, vp + 1, losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
        put(m, up, PISO + 2, vp, Pal.CHAIN);
        // los bancales del huerto, helados (tierra con nieve) y alguna planta seca
        for (int v = -28; v <= -24; v++)
            for (int u = 13; u <= 21; u++) {
                if (v == -26) continue;
                put(m, u, SUELO, v, b(Blocks.COARSE_DIRT));
                put(m, u, PISO, v, Hash.pct(x(u), PISO, z(v), SEMILLA + 51) < 30 ? b(Blocks.DEAD_BUSH) : nieve(1));
            }
        // la lenera: troncos apilados bajo un tejadillo
        for (int v = -16; v <= -13; v++) {
            for (int y = PISO; y <= PISO + 1; y++) put(m, 21, y, v, Pal.SP_LOG.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
            put(m, 21, PISO + 2, v, losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
            put(m, 21, PISO + 3, v, nieve(1));
        }
    }

    // ------------------------------------------------------------------ el camino hasta el santuario

    /** Del pie de la escalera al santuario de la Helada: tres de ancho, de grava y piedra pisadas, con faroles. */
    private void camino(Pilotes.Lista m) {
        int v0 = V1 + 2 + escalones;
        double ax = 0, az = v0, bx = 34, bz = 46;
        double l = Math.hypot(bx - ax, bz - az);
        double dx = (bx - ax) / l, dz = (bz - az) / l;
        int n = 0;
        for (double s = 0; s <= l; s += 0.5) {
            double cu = ax + dx * s, cv = az + dz * s;
            for (int k = -1; k <= 1; k++) {
                int u = (int) Math.round(cu - dz * k), v = (int) Math.round(cv + dx * k);
                if (Pilotes.cercaSantuario(x(u), z(v))) continue;
                int t = alto(x(u), z(v));
                int p = Hash.pct(x(u), t, z(v), SEMILLA + 61);
                BlockState st = p < 45 ? Pal.GRAVEL : p < 65 ? b(Blocks.COBBLESTONE) : p < 82 ? Pal.STONE : b(Blocks.SNOW_BLOCK);
                put(m, u, t, v, st);
                put(m, u, t + 1, v, Pal.AIR);
                put(m, u, t + 2, v, Pal.AIR);
            }
            // un farol cada once bloques, a un lado y a otro
            if ((int) (s * 2) % 22 == 11) {
                int lado = (n++ & 1) == 0 ? 3 : -3;
                int u = (int) Math.round(cu - dz * lado), v = (int) Math.round(cv + dx * lado);
                if (!Pilotes.cercaSantuario(x(u), z(v))) {
                    int t = alto(x(u), z(v));
                    une(m, u, t + 1, v, Pal.SP_FENCE);
                    une(m, u, t + 2, v, Pal.SP_FENCE);
                    put(m, u, t + 3, v, Pal.LANTERN);
                    put(m, u, t + 4, v, nieve(1));
                }
            }
        }
    }

    // ------------------------------------------------------------------ lo que lleva datos: el atril, los arcones, la campana

    private void trozos() {
        int ya = PISO + 1;
        trozo(4, ya, -28, c -> c.lectern(x(4), ya, z(-28), Direction.SOUTH, Perdidos.libro(21)));
        trozo(-4, ya, -30, c -> c.chest(x(-4), ya, z(-30), Direction.SOUTH, BOTIN_BUENO));
        // la campana del campanario, colgada del techo de su sala
        int yb = SUELO + 20;
        trozo(-12, yb, V0 + 2, c -> c.set(x(-12), yb, z(V0 + 2), b(Blocks.BELL).setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING)
                .setValue(BellBlock.FACING, Direction.NORTH)));
        // los barriles de las celdas y de la cocina
        trozo(-22, PISO, -5, c -> c.barrel(x(-22), PISO, z(-5), Direction.UP, BOTIN));
        trozo(-22, PISO, 5, c -> c.barrel(x(-22), PISO, z(5), Direction.UP, BOTIN));
        trozo(22, PISO, 11, c -> c.barrel(x(22), PISO, z(11), Direction.UP, BOTIN));
        trozo(21, PISO, 11, c -> c.barrel(x(21), PISO, z(11), Direction.UP, BOTIN));
        // un arcon en la lenera del huerto
        trozo(20, PISO, -16, c -> c.chest(x(20), PISO, z(-16), Direction.WEST, BOTIN));
    }

    private void trozo(int u, int y, int v, Trozo.Obra o) {
        elementos.add(new Trozo(6, x(u), y, z(v), x(u), y, z(v), o));
    }

    // ------------------------------------------------------------------ los helados

    /** El hacia donde mira de Minecraft (0 = sur, 90 = oeste) para una direccion (du, dv). */
    static float giro(double du, double dv) {
        return (float) (Math.toDegrees(Math.atan2(dv, du)) - 90.0);
    }

    private static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;

    private void ponerEsperas() {
        int M = Helado.MONJE, P = Helado.PENITENTE, G = Helado.PEREGRINO;
        int R = Enemigo.REZA, T = Enemigo.TUMBADO, D = Enemigo.DE_PIE;
        // por el camino: peregrinos que no llegaron (de rodillas y enterrados) y un monje enterrado al pie de la escalera
        fuera(G, 22, 42, giro(-22, -42), R, "camino");
        fuera(G, 14, 37, giro(-14, -37), T, "camino");
        fuera(M, 6, V1 + 4 + escalones, giro(-6, -10), T, "camino");
        fuera(G, 24, 47, giro(-24, -47), R, "camino");
        // la puerta y el patio de entrada
        en(P, 0, PISO, 17, SUR, D, 0, "puerta");
        en(M, -7, PISO, 16, NORTE, R, 0, "entrada");
        en(G, 9, PISO, 16, SUR, D, 3, "entrada");
        // el claustro: uno de rodillas ante la puerta de la iglesia, dos de ronda, dos rezando a la fuente, uno enterrado
        en(P, 0, PISO, -10, NORTE, R, 0, "claustro");
        en(M, -10, PISO, 2, ESTE, D, 3, "claustro");
        en(M, 10, PISO, -3, OESTE, D, 3, "claustro");
        en(M, -4, PISO, 1, ESTE, R, 0, "claustro");
        en(M, 4, PISO, 2, OESTE, R, 0, "claustro");
        en(G, 5, PISO, -5, giro(-1, 1), T, 0, "claustro");
        // la iglesia: rezando entre los bancos, el penitente ante la grada y un peregrino en el presbiterio
        for (int[] q : new int[][]{{-5, -22}, {5, -22}, {-6, -18}, {4, -18}, {-4, -14}}) en(M, q[0], PISO, q[1], NORTE, R, 0, "iglesia");
        en(P, 0, PISO, -25, NORTE, R, 0, "iglesia");
        en(G, 6, PISO + 1, -28, NORTE, R, 0, "iglesia");
        // las celdas: dos de rodillas en la suya y uno de ronda por el pasillo
        en(M, -20, PISO, -6, OESTE, R, 0, "celdas");
        en(M, -20, PISO, 4, OESTE, R, 0, "celdas");
        en(G, -14, PISO, -2, SUR, D, 4, "celdas");
        // el refectorio: a la mesa (de rodillas, mirandola) y un penitente junto a la puerta de la cocina
        en(M, 15, PISO, -2, ESTE, R, 0, "refectorio");
        en(M, 20, PISO, 0, OESTE, R, 0, "refectorio");
        en(M, 15, PISO, 4, ESTE, R, 0, "refectorio");
        en(P, 14, PISO, 9, NORTE, D, 0, "refectorio");
        // el cementerio: dos enterrados entre las tumbas y un penitente de rodillas ante ellas
        en(M, -19, PISO, -24, SUR, T, 0, "cementerio");
        en(G, -20, PISO, -16, SUR, T, 0, "cementerio");
        en(P, -16, PISO, -20, OESTE, R, 0, "cementerio");
        // el huerto
        en(P, 16, PISO, -15, SUR, D, 4, "huerto");
    }

    private void fuera(int variante, int u, int v, float giro, int postura, String grupo) {
        esperas.add(new Espera(variante, x(u), pie(x(u), z(v)), z(v), giro, postura, 0, grupo));
    }

    private void en(int variante, int u, int y, int v, float giro, int postura, int ronda, String grupo) {
        esperas.add(new Espera(variante, x(u), y, z(v), giro, postura, ronda, grupo));
    }

    // ------------------------------------------------------------------ la campana que suena sola

    /** Cada 5 s (lo llama {@link Morvain}): de vez en cuando la campana rota da un toque, para quien este cerca. */
    public static void campana(ServerLevel m) {
        if (m.random.nextInt(18) != 0) return;
        int[] q = get().sitio("campana");
        boolean alguien = false;
        for (ServerPlayer p : m.players())
            if (p.distanceToSqr(q[0] + 0.5, q[1], q[2] + 0.5) < 140 * 140) alguien = true;
        if (alguien) m.playSound(null, q[0] + 0.5, q[1], q[2] + 0.5, DarkCraftMod.SFX_MONASTERIO_CAMPANA.get(), SoundSource.AMBIENT, 4.0f,
                0.9f + m.random.nextFloat() * 0.1f);
    }

    // ------------------------------------------------------------------ para la prueba

    /** Lo que hay en el monasterio ya generado (para la prueba): el atril con el libro, el arcon, la campana, la fuente y los bancos. */
    public static String resumen(ServerLevel m) {
        Monasterio a = get();
        int[] at = a.sitio("atril"), ar = a.sitio("arcon"), ca = a.sitio("campana");
        BlockPos pa = new BlockPos(at[0], at[1], at[2]), pr = new BlockPos(ar[0], ar[1], ar[2]);
        boolean libro = m.getBlockEntity(pa) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le && Perdidos.de(le.getBook()) == 21;
        boolean arcon = m.getBlockState(pr).is(Blocks.CHEST);
        boolean campana = m.getBlockState(new BlockPos(ca[0], ca[1], ca[2])).is(Blocks.BELL);
        boolean fuente = m.getBlockState(new BlockPos(CX, PISO + 3, CZ + 1)).is(Blocks.PACKED_ICE);
        int bancos = 0, nieve = 0, muestras = 0;
        for (int v = -25; v <= -15; v += 2)
            for (int u = -8; u <= 8; u++)
                if (m.getBlockState(new BlockPos(CX + u, PISO, CZ + v)).is(Blocks.SPRUCE_STAIRS)) bancos++;
        for (int u = -7; u <= 7; u += 2)
            for (int v = -7; v <= 8; v += 3) {
                muestras++;
                if (m.getBlockState(new BlockPos(CX + u, SUELO, CZ + v)).is(Blocks.SNOW_BLOCK)) nieve++;
            }
        return "atril con el libro 21 " + (libro ? "SI" : "NO") + ", arcon " + (arcon ? "SI" : "NO") + ", campana " + (campana ? "SI" : "NO")
                + ", fuente helada " + (fuente ? "SI" : "NO") + ", bancos " + bancos + "/" + a.bancos + ", nieve en el patio " + nieve + "/" + muestras;
    }
}
