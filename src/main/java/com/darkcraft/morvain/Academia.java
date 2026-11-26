package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Erudito;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
 * La Academia Espectral (parte 7o), en la meseta de caliza del noreste, a la puerta del santuario de la Puerta de la
 * Academia: aqui estudiaban los eruditos del reino, y la noche del cielo rojo siguieron estudiando (los
 * {@code enemigos/Erudito}: aprendices con su calamo y su tintero, maestros con su vara de cristal, custodios con su tomo
 * encadenado). Un <b>recinto</b> amurallado de caliza blanca con zocalo y cornisas de pizarra, sus cuatro torres de esquina
 * y, al sur, el <b>cuerpo de la puerta</b> (el paso con su arco apuntado entre dos torres con chapitel, los pendones
 * morados, el ojo que llora sobre el arco y una hoja de la puerta caida en el camino). Dentro: el <b>patio de entrada</b>
 * (pebeteros de almas y arboles secos); el <b>claustro</b> (cuatro galerias de arcos apuntados alrededor del jardin, con la
 * <b>fuente de amatista</b> en el centro); al norte la <b>Gran Biblioteca</b> (la nave alta con tejado de pizarra, los
 * ventanales morados y los dos rosetones; filas de estantes, mesas de lectura con velas, el altillo con sus escalas, las
 * lamparas de almas colgando de la cumbrera y, al fondo, el <b>estrado del grimorio</b>: el atril con el <b>libro perdido
 * 24</b>, el arcon y el relicario de amatista); al oeste el <b>scriptorium</b> (los pupitres de los copistas) y al este el
 * <b>aula</b> (la catedra con su pizarra y los bancos); al noreste la <b>torre del archivo</b> (lo mas alto de la meseta: una
 * escala por dentro hasta la sala del archivo, bajo su aguja) y las <b>ruinas del aula vieja</b>; al noroeste la <b>veta</b>:
 * el afloramiento de amatista sobre el que se levanto la academia. Fuera, el <b>camino</b> hasta el santuario.
 * <p>
 * Los eruditos los apunta el propio plano ({@link #esperas}): leyendo en las galerias, a las mesas y en los pupitres,
 * tumbados entre los libros y en la hierba, de ronda por el claustro, entre los estantes y por el altillo, en la catedra y
 * de guardia en la puerta y ante el grimorio. Se dibuja como el monasterio: chunk a chunk al adornar
 * ({@link MorvainGenerador#applyBiomeDecoration}); en su caja {@link Adorno} no pone nada.
 */
public final class Academia {
    public static final long SEMILLA = 0xACADE_3170_7AL;
    /** El eje del recinto (u = x - CX hacia el este, v = z - CZ hacia el sur): el claustro y la biblioteca van en u = 0. */
    public static final int CX = 4110, CZ = -3250;
    /** El suelo del recinto (lo que se pisa esta en {@link #PISO}). */
    public static final int SUELO = 242, PISO = SUELO + 1;
    /** El recinto: la muralla va por |u| = 36, v = -60 (norte) y v = 44 (sur); su alto. */
    static final int U_MURO = 36, V_N = -60, V_S = 44, ALTO_MURO = 8;
    /** Lo allanado (el recinto y algo de fuera) y lo que se funde alrededor con el terreno. */
    static final int U_LLANO = 39, V_LLANO_N = -63, V_LLANO_S = 50;
    static final double FUNDE = 7;
    /** El cuerpo de la puerta: v 40..47 (la cara de fuera en 47), el paso |u| <= 2; las torres |u| 5..11, v 41..47. */
    public static final int PUERTA_V0 = 40, PUERTA_V1 = 47;
    /** El claustro (|u| <= 22, v -2..28; su muro del sur en v 29) y su jardin (|u| <= 17, v 3..23); la fuente en v 13. */
    public static final int CLAUSTRO_U = 22, CLAUSTRO_V0 = -2, CLAUSTRO_V1 = 29, JARDIN_U = 17, JARDIN_V0 = 3, JARDIN_V1 = 23, FUENTE_V = 13;
    /** La biblioteca: los muros en |u| = 16, v = -51 y v = -3; su alto; el altillo (el suelo en {@link #ALTILLO}). */
    public static final int BIB_U = 16, BIB_V0 = -51, BIB_V1 = -3, BIB_ALTO = 16, ALTILLO = SUELO + 7;
    /** Las filas de estantes de la biblioteca (|u| 4..11). */
    static final int[] FILAS = {-41, -37, -33, -29, -25, -21, -17, -13, -9};
    /** Las alas: el scriptorium al oeste y el aula al este (|u| 23..34, v -3..29). */
    public static final int ALA_U0 = 23, ALA_U1 = 34, ALA_V0 = -3, ALA_V1 = 29, ALA_ALTO = 7;
    /** La torre del archivo (u 21..29, v -57..-49) y su alto (la sala del archivo, bajo la aguja, pisa en SUELO + 32). */
    public static final int TORRE_U0 = 21, TORRE_U1 = 29, TORRE_V0 = -57, TORRE_V1 = -49, TORRE_ALTO = SUELO + 38, ARCHIVO = SUELO + 32;
    /** Lo que toca (el recinto, lo que se funde y el camino hasta el santuario). */
    public static final int X0 = CX - 46, X1 = CX + 46, Z0 = CZ - 70, Z1 = CZ + 64;
    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/academia"),
            BOTIN_BUENO = new ResourceLocation(DarkCraftMod.MODID, "chests/academia_bueno");
    private static final int[] LADOS = {-1, 1};

    private static final int AW = X1 - X0 + 1, AD = Z1 - Z0 + 1;
    private static final int[] ALTO = new int[AW * AD];

    static {
        Arrays.fill(ALTO, Integer.MIN_VALUE);
    }

    /** El bloque de arriba del terreno de Morvain en (x, z) (sin la academia). */
    public static int alto(int x, int z) {
        if (x < X0 || x > X1 || z < Z0 || z > Z1) return (int) Math.floor(Terreno.columna(x, z).altura());
        int i = (z - Z0) * AW + (x - X0);
        int v = ALTO[i];
        if (v == Integer.MIN_VALUE) ALTO[i] = v = (int) Math.floor(Terreno.columna(x, z).altura());
        return v;
    }

    /** Un erudito de la academia: donde espera (y: lo que pisa), hacia donde mira, su variante, como espera, cuanto ronda y su grupo. */
    public record Espera(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    private static Academia unico;

    public static synchronized Academia get() {
        if (unico == null) unico = new Academia();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    /** Lo que hay (para la prueba y las vistas). */
    public int estantes, mesas, atriles, ventanas, bancos, pupitres, cristales, almenas;

    private Academia() {
        elementos.add(new Terraza());
        Pilotes.Lista mu = new Pilotes.Lista();
        muralla(mu);
        puerta(mu);
        camino(mu);
        elementos.add(mu.pieza(3));
        Pilotes.Lista cl = new Pilotes.Lista();
        patio(cl);
        claustro(cl);
        elementos.add(cl.pieza(3));
        Pilotes.Lista bi = new Pilotes.Lista();
        biblioteca(bi);
        elementos.add(bi.pieza(4));
        Pilotes.Lista al = new Pilotes.Lista();
        ala(al, -1);
        ala(al, 1);
        scriptorium(al);
        aula(al);
        elementos.add(al.pieza(4));
        Pilotes.Lista to = new Pilotes.Lista();
        torre(to);
        elementos.add(to.pieza(4));
        Pilotes.Lista ve = new Pilotes.Lista();
        veta(ve);
        ruinas(ve);
        elementos.add(ve.pieza(3));
        trozos();
        // ---- los sitios con nombre
        sitio("entrada", 0, pie(CX, CZ + 56), 56);
        sitio("puerta", 0, PISO, 44);
        sitio("patio", 0, PISO, 34);
        sitio("claustro", -20, PISO, FUENTE_V);
        sitio("jardin", 4, PISO, 19);
        sitio("fuente", 0, PISO, FUENTE_V + 6);
        sitio("biblioteca", 0, PISO, BIB_V1 - 3);
        sitio("pasillo", 0, PISO, -27);
        sitio("altillo", -13, ALTILLO + 1, -27);
        sitio("estrado", 0, PISO + 1, -46);
        sitio("atril", 0, PISO + 1, -48);
        sitio("arcon", 5, PISO + 1, -49);
        sitio("scriptorium", -28, PISO, 13);
        sitio("aula", 28, PISO, 13);
        sitio("catedra", 28, PISO + 1, -1);
        sitio("torre", 25, PISO, TORRE_V1 + 3);
        sitio("archivo", 25, ARCHIVO, -52);
        sitio("veta", -27, PISO, -19);
        sitio("ruinas", 26, PISO, -26);
        // ---- los eruditos
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo de la academia (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en lo de la academia: ahi {@link Adorno} no pone nada. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= X0 && x0 <= X1 && z0 + 15 >= Z0 && z0 <= Z1;
    }

    /** Si (x, z) esta dentro del recinto (o en el paso de la puerta): para el nombre del sitio y su logro. */
    public static boolean dentro(double x, double z) {
        double u = x - CX, v = z - CZ;
        return Math.abs(u) <= U_MURO + 2 && v >= V_N - 2 && v <= PUERTA_V1 + 1;
    }

    /** Si (x, z) esta dentro de la Gran Biblioteca. */
    public static boolean enBiblioteca(double x, double z) {
        double u = x - CX, v = z - CZ;
        return Math.abs(u) < BIB_U && v > BIB_V0 && v < BIB_V1;
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} de la academia en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los eruditos de la academia (los apunta {@link MorvainEnemigos}). */
    public List<Espera> esperas() {
        return esperas;
    }

    /** Los sitios con nombre (la puerta, el claustro, la biblioteca, el atril, el archivo...): {x, y, z} (y: lo que se pisa). */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    private void sitio(String nombre, int u, int y, int v) {
        sitios.put(nombre, new int[]{CX + u, y, CZ + v});
    }

    /** Lo que se pisa en (x, z) fuera del recinto (el terreno, o lo allanado donde se funde). */
    static int pie(int x, int z) {
        return superficie(x, z) + 1;
    }

    /** El bloque de arriba en (x, z) con la academia: lo allanado (y lo que se funde) o el terreno. */
    static int superficie(int x, int z) {
        int o = objetivo(x, z);
        return o != Integer.MIN_VALUE ? o : alto(x, z);
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

    static BlockState vela(int n, boolean encendida) {
        return Blocks.PURPLE_CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, Math.max(1, Math.min(4, n))).setValue(CandleBlock.LIT, encendida);
    }

    static BlockState racimo(Block k, Direction f) {
        return k.defaultBlockState().setValue(AmethystClusterBlock.FACING, f);
    }

    static BlockState atril(Direction f) {
        return Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, f);
    }

    static BlockState escala(Direction f) {
        return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, f);
    }

    static BlockState tronco(Direction.Axis a) {
        return Blocks.DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, a);
    }

    static BlockState columnaCaida(Direction.Axis a) {
        return Blocks.QUARTZ_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, a);
    }

    static BlockState pendon(Direction f) {
        return Blocks.PURPLE_WALL_BANNER.defaultBlockState().setValue(WallBannerBlock.FACING, f);
    }

    static BlockState estanteVacio(Direction f) {
        return Blocks.CHISELED_BOOKSHELF.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, f);
    }

    static final BlockState VIDRIERA = Blocks.PURPLE_STAINED_GLASS_PANE.defaultBlockState();

    /** La piedra de la academia: la caliza de la meseta (calcita y diorita) con algo de toba y basalto. */
    static BlockState piedra(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 3);
        if (p < 54) return b(Blocks.CALCITE);
        if (p < 76) return b(Blocks.POLISHED_DIORITE);
        if (p < 88) return b(Blocks.DIORITE);
        if (p < 97) return b(Blocks.TUFF);
        return b(Blocks.SMOOTH_BASALT);
    }

    /** La piedra oscura de los zocalos, las cornisas, los marcos y los arcos: pizarra labrada. */
    static BlockState oscura(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 4);
        if (p < 58) return b(Blocks.DEEPSLATE_BRICKS);
        if (p < 74) return b(Blocks.CRACKED_DEEPSLATE_BRICKS);
        if (p < 88) return Pal.DS_TILES;
        return b(Blocks.POLISHED_DEEPSLATE);
    }

    static int x(int u) {
        return CX + u;
    }

    static int z(int v) {
        return CZ + v;
    }

    private static void put(Pilotes.Lista m, int u, int y, int v, BlockState s) {
        m.put(x(u), y, z(v), s);
    }

    private static void une(Pilotes.Lista m, int u, int y, int v, BlockState s) {
        m.une(x(u), y, z(v), s);
    }

    /** Lo mas bajo del terreno bajo (u, v) (para que los muros de fuera bajen hasta el). */
    private static int base(int u, int v) {
        return Math.min(SUELO, superficie(x(u), z(v))) - 1;
    }

    /** Un sillar de muro: el zocalo oscuro hasta el piso, la cornisa oscura en {@code top} y, si {@code imposta}, otra franja. */
    private static BlockState sillar(int u, int y, int v, int top, int imposta) {
        if (y <= PISO || y == top || y == imposta) return oscura(x(u), y, z(v));
        return piedra(x(u), y, z(v));
    }

    /** Un escalon del reves del arco: con su lado lleno hacia {@code -} (oeste/norte) o hacia {@code +} (este/sur). */
    private static BlockState dovela(boolean enU, boolean haciaMenos) {
        Direction f = enU ? (haciaMenos ? Direction.WEST : Direction.EAST) : (haciaMenos ? Direction.NORTH : Direction.SOUTH);
        return escalera(Blocks.DEEPSLATE_BRICK_STAIRS, f, true);
    }

    /**
     * Un hueco con arco apuntado en un muro: a lo largo de u (en la fila v) si {@code enU}, o a lo largo de v (en la columna
     * u); de ancho 2 * mitad + 1 centrado en {@code c}, abierto de y0 a y1 y el arco encima. Con {@code cristal}, en vez de
     * aire, la vidriera morada (una ventana; {@code rota}: le faltan los cristales de abajo).
     */
    private static void hueco(Pilotes.Lista m, boolean enU, int fila, int c, int mitad, int y0, int y1, boolean cristal, boolean rota) {
        for (int d = -mitad; d <= mitad; d++)
            for (int y = y0; y <= y1; y++) {
                int u = enU ? c + d : fila, v = enU ? fila : c + d;
                if (!cristal || rota && y <= y0 + 1 && Hash.pct(x(u), y, z(v), SEMILLA + 5) < 70) put(m, u, y, v, Pal.AIR);
                else une(m, u, y, v, VIDRIERA);
            }
        for (int k = 1; k <= mitad; k++) {
            int y = y1 + k;
            for (int d = -mitad; d <= mitad; d++) {
                int a = Math.abs(d);
                int u = enU ? c + d : fila, v = enU ? fila : c + d;
                if (a < mitad - k + 1) {
                    if (cristal) une(m, u, y, v, VIDRIERA);
                    else put(m, u, y, v, Pal.AIR);
                } else if (a == mitad - k + 1) put(m, u, y, v, dovela(enU, d < 0));
            }
        }
    }

    /** Un tejado a dos aguas de pizarra sobre u0..u1 (con los aleros), la cumbrera de norte a sur, desde y0; los hastiales en vh0 y vh1. */
    private static void tejado(Pilotes.Lista m, int u0, int u1, int v0, int v1, int y0, int vh0, int vh1) {
        for (int u = u0; u <= u1; u++) {
            int a = u - u0, bb = u1 - u;
            int h = y0 + Math.min(a, bb);
            for (int v = v0; v <= v1; v++) {
                BlockState s = a == bb ? losa(Blocks.DEEPSLATE_TILE_SLAB, SlabType.DOUBLE)
                        : escalera(Blocks.DEEPSLATE_TILE_STAIRS, a < bb ? Direction.EAST : Direction.WEST, false);
                une(m, u, h, v, s);
            }
            for (int y = y0 + 1; y < h; y++) {
                put(m, u, y, vh0, piedra(x(u), y, z(vh0)));
                put(m, u, y, vh1, piedra(x(u), y, z(vh1)));
            }
        }
    }

    /** Un chapitel de pizarra sobre el cuadrado u0..u1, v0..v1, desde y (un anillo de escalones por hilada). */
    private static void chapitel(Pilotes.Lista m, int u0, int v0, int u1, int v1, int y) {
        for (int k = 0; u0 + k <= u1 - k && v0 + k <= v1 - k; k++) {
            int yy = y + k;
            for (int u = u0 + k; u <= u1 - k; u++)
                for (int v = v0 + k; v <= v1 - k; v++) {
                    boolean borde = u == u0 + k || u == u1 - k || v == v0 + k || v == v1 - k;
                    if (!borde || u0 + k == u1 - k || v0 + k == v1 - k) {
                        put(m, u, yy, v, Pal.DS_TILES);
                        continue;
                    }
                    Direction f = u == u0 + k ? Direction.EAST : u == u1 - k ? Direction.WEST : v == v0 + k ? Direction.SOUTH : Direction.NORTH;
                    une(m, u, yy, v, escalera(Blocks.DEEPSLATE_TILE_STAIRS, f, false));
                }
        }
    }

    /** Un contrafuerte por fuera de un muro: de lo bajo hasta {@code top}, rematado con un escalon que mira al muro. */
    private static void contrafuerte(Pilotes.Lista m, int u, int v, Direction alMuro, int top) {
        for (int y = base(u, v); y < top; y++) put(m, u, y, v, y <= PISO ? oscura(x(u), y, z(v)) : piedra(x(u), y, z(v)));
        put(m, u, top, v, escalera(Blocks.DEEPSLATE_BRICK_STAIRS, alMuro, false));
    }

    /** Un arbol seco (roble oscuro sin hojas) de {@code alto} con dos o tres ramas torcidas. */
    private static void arbol(Pilotes.Lista m, int u, int v, int alto) {
        int y0 = pie(x(u), z(v));
        for (int y = y0; y < y0 + alto; y++) put(m, u, y, v, Pal.DO_LOG);
        int p = Hash.pct(x(u), 3, z(v), SEMILLA + 91);
        int[][] dirs = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        for (int k = 0; k < 3; k++) {
            int[] d = dirs[(p + k * 3) % 4];
            int y = y0 + alto - 1 - k;
            int largo = 2 + (p >> k & 1);
            for (int i = 1; i <= largo; i++)
                put(m, u + d[0] * i, y + (i == largo ? 1 : 0), v + d[1] * i, i == largo ? Pal.DO_LOG : tronco(d[0] != 0 ? Direction.Axis.X : Direction.Axis.Z));
        }
    }

    /** Un pebetero: el pie de pizarra labrada y el fuego de almas encima. */
    private static void pebetero(Pilotes.Lista m, int u, int v) {
        put(m, u, PISO, v, b(Blocks.CHISELED_DEEPSLATE));
        put(m, u, PISO + 1, v, Pal.SOUL_CAMPFIRE);
    }

    /** Un farol de almas sobre su poste de pizarra (de pie en el suelo que haya). */
    private static void farol(Pilotes.Lista m, int u, int v) {
        int y = pie(x(u), z(v));
        une(m, u, y, v, b(Blocks.POLISHED_DEEPSLATE_WALL));
        une(m, u, y + 1, v, b(Blocks.POLISHED_DEEPSLATE_WALL));
        put(m, u, y + 2, v, Pal.SOUL_LANTERN);
    }

    // ------------------------------------------------------------------ el suelo: allanado y fundido con el terreno

    /** Lo alto del suelo en (x, z): {@link #SUELO} en lo allanado, fundido con el terreno alrededor; MIN_VALUE fuera. */
    static int objetivo(int x, int z) {
        int u = x - CX, v = z - CZ;
        double du = Math.max(0, Math.abs(u) - U_LLANO), dv = v < V_LLANO_N ? V_LLANO_N - v : v > V_LLANO_S ? v - V_LLANO_S : 0;
        double d = Math.hypot(du, dv);
        if (d <= 0) return SUELO;
        if (d >= FUNDE) return Integer.MIN_VALUE;
        double t = Ruido.suave(0, FUNDE, d);
        return (int) Math.round(Ruido.lerp(t, SUELO, alto(x, z)));
    }

    enum Zona { FUERA, CAMINO, MURO, PATIO, CLAUSTRO, JARDIN, BIBLIOTECA, SCRIPTORIUM, AULA, TORRE, VETA, RUINAS }

    static Zona zona(int u, int v) {
        if (Math.abs(u) > U_MURO || v < V_N || v > V_S) return v > V_S && v <= 62 && Math.abs(u) <= 2 ? Zona.CAMINO : Zona.FUERA;
        if (Math.abs(u) == U_MURO || v == V_N || v == V_S) return Zona.MURO;
        if (Math.abs(u) <= BIB_U && v >= BIB_V0 && v <= BIB_V1) return Zona.BIBLIOTECA;
        if (Math.abs(u) <= CLAUSTRO_U && v >= CLAUSTRO_V0 && v <= CLAUSTRO_V1)
            return Math.abs(u) <= JARDIN_U && v >= JARDIN_V0 && v <= JARDIN_V1 ? Zona.JARDIN : Zona.CLAUSTRO;
        if (Math.abs(u) >= ALA_U0 && Math.abs(u) <= ALA_U1 && v >= ALA_V0 && v <= ALA_V1) return u < 0 ? Zona.SCRIPTORIUM : Zona.AULA;
        if (u >= TORRE_U0 && u <= TORRE_U1 && v >= TORRE_V0 && v <= TORRE_V1) return Zona.TORRE;
        if (v < ALA_V0) return u < 0 ? Zona.VETA : Zona.RUINAS;
        return Zona.PATIO;
    }

    /** Si (u, v) es un sendero del jardin del claustro (la cruz de la fuente). */
    static boolean sendero(int u, int v) {
        return Math.abs(u) <= 1 || Math.abs(v - FUENTE_V) <= 1;
    }

    private static BlockState losas(int p) {
        return p < 40 ? b(Blocks.CALCITE) : p < 66 ? b(Blocks.POLISHED_DIORITE) : p < 84 ? Pal.GRAVEL : b(Blocks.POLISHED_ANDESITE);
    }

    private static BlockState hierba(int p) {
        return p < 46 ? Pal.b(Blocks.GRASS_BLOCK) : p < 80 ? b(Blocks.MOSS_BLOCK) : p < 92 ? Pal.COARSE_DIRT : b(Blocks.CALCITE);
    }

    private static BlockState suelo(int u, int v, int p) {
        return switch (zona(u, v)) {
            case CAMINO -> losas(p);
            case MURO -> b(Blocks.CALCITE);
            case PATIO -> Math.abs(u) <= 3 ? losas(p) : hierba(p);
            case CLAUSTRO -> p < 8 ? Pal.DS_TILES_CRACKED : (u + v & 1) == 0 ? b(Blocks.POLISHED_DEEPSLATE) : Pal.DS_TILES;
            case JARDIN -> sendero(u, v) ? losas(p) : p < 52 ? b(Blocks.MOSS_BLOCK) : p < 92 ? Pal.b(Blocks.GRASS_BLOCK) : Pal.COARSE_DIRT;
            case BIBLIOTECA -> Math.abs(u) <= 3 ? (p < 80 ? b(Blocks.POLISHED_DEEPSLATE) : Pal.DS_TILES) : Pal.DO_PLANKS;
            case SCRIPTORIUM -> p < 60 ? Pal.DS_TILES : p < 85 ? b(Blocks.POLISHED_DEEPSLATE) : Pal.DS_TILES_CRACKED;
            case AULA -> Pal.DO_PLANKS;
            case TORRE -> b(Blocks.POLISHED_DEEPSLATE);
            case VETA -> p < 44 ? b(Blocks.MOSS_BLOCK) : p < 70 ? Pal.b(Blocks.GRASS_BLOCK) : p < 86 ? b(Blocks.CALCITE) : b(Blocks.SMOOTH_BASALT);
            case RUINAS -> p < 28 ? Pal.GRAVEL : p < 46 ? b(Blocks.COBBLED_DEEPSLATE) : p < 70 ? b(Blocks.MOSS_BLOCK) : Pal.b(Blocks.GRASS_BLOCK);
            case FUERA -> p < 55 ? Pal.b(Blocks.GRASS_BLOCK) : p < 88 ? b(Blocks.MOSS_BLOCK) : Pal.COARSE_DIRT;
        };
    }

    /** Lo que crece encima: matas de hierba y alguna zarza seca donde hay hierba; papeles por el suelo de la biblioteca. */
    private static BlockState encima(int u, int v, int q) {
        Zona zn = zona(u, v);
        boolean verde = zn == Zona.FUERA || zn == Zona.VETA || zn == Zona.RUINAS || zn == Zona.PATIO && Math.abs(u) > 3
                || zn == Zona.JARDIN && !sendero(u, v);
        if (verde) return q < 9 ? Pal.b(Blocks.GRASS) : q < 12 ? Pal.b(Blocks.DEAD_BUSH) : null;
        if (zn == Zona.BIBLIOTECA && Math.abs(u) >= 2 && Math.abs(u) <= 3 && v > -44 && v < BIB_V1 - 1 && q < 6) return b(Blocks.WHITE_CARPET);
        return null;
    }

    /**
     * La terraza: corta lo que sobra hasta el suelo (por encima, aire) y rellena por debajo (tierra y caliza); el suelo de cada
     * zona y la hierba encima donde toca.
     */
    private final class Terraza extends Element {
        Terraza() {
            super(SEMILLA + 51);
            this.layer = 1;
            bounds(X0, 200, Z0, X1, 300, Z1);
        }

        @Override
        public void build(Canvas c) {
            for (int x = Math.max(c.minX, x0); x <= Math.min(c.maxX, x1); x++)
                for (int z = Math.max(c.minZ, z0); z <= Math.min(c.maxZ, z1); z++) {
                    int o = objetivo(x, z);
                    if (o == Integer.MIN_VALUE) continue;
                    int u = x - CX, v = z - CZ;
                    int t = alto(x, z);
                    for (int y = o + 1; y <= Math.max(t, o) + 6; y++) c.set(x, y, z, Pal.AIR);
                    for (int y = t + 1; y < o; y++) c.set(x, y, z, y >= o - 3 ? Pal.DIRT : b(Blocks.CALCITE));
                    c.set(x, o, z, suelo(u, v, Hash.pct(x, o, z, seed)));
                    BlockState e = encima(u, v, Hash.pct(x, o + 1, z, seed + 1));
                    if (e != null) c.set(x, o + 1, z, e);
                }
        }
    }

    // ------------------------------------------------------------------ la muralla y sus torres

    private static boolean enEsquina(int u, int v) {
        return Math.abs(Math.abs(u) - U_MURO) <= 2 && (Math.abs(v - V_N) <= 2 || Math.abs(v - V_S) <= 2);
    }

    private void muralla(Pilotes.Lista m) {
        int alto = SUELO + ALTO_MURO;
        for (int u = -U_MURO; u <= U_MURO; u++)
            for (int v = V_N; v <= V_S; v++) {
                if (Math.abs(u) != U_MURO && v != V_N && v != V_S) continue;
                if (v == V_S && Math.abs(u) <= 11) continue;   // ahi esta el cuerpo de la puerta
                if (enEsquina(u, v)) continue;                 // y ahi las torres de las esquinas
                for (int y = base(u, v); y <= alto; y++) put(m, u, y, v, sillar(u, y, v, alto, -1));
                // las almenas: un merlon si y otro no, con su albardilla de losa oscura
                if ((u + v & 1) == 0) {
                    put(m, u, alto + 1, v, piedra(x(u), alto + 1, z(v)));
                    put(m, u, alto + 2, v, losa(Blocks.DEEPSLATE_BRICK_SLAB, SlabType.BOTTOM));
                    almenas++;
                } else put(m, u, alto + 1, v, losa(Blocks.DEEPSLATE_BRICK_SLAB, SlabType.BOTTOM));
            }
        // los contrafuertes por fuera, cada ocho
        for (int v = V_N + 8; v <= V_S - 6; v += 8) {
            contrafuerte(m, -U_MURO - 1, v, Direction.EAST, SUELO + 5);
            contrafuerte(m, U_MURO + 1, v, Direction.WEST, SUELO + 5);
        }
        for (int u = -U_MURO + 8; u <= U_MURO - 8; u += 8) {
            contrafuerte(m, u, V_N - 1, Direction.SOUTH, SUELO + 5);
            if (Math.abs(u) > 14) contrafuerte(m, u, V_S + 1, Direction.NORTH, SUELO + 5);
        }
        // las cuatro torres de las esquinas: cuadradas, mas altas que el muro, con su saetera y su chapitel de pizarra
        for (int su : LADOS)
            for (int vc : new int[]{V_N, V_S}) {
                int uc = su * U_MURO, top = SUELO + 12;
                for (int u = uc - 2; u <= uc + 2; u++)
                    for (int v = vc - 2; v <= vc + 2; v++)
                        for (int y = base(u, v); y <= top; y++) put(m, u, y, v, sillar(u, y, v, top, SUELO + ALTO_MURO + 1));
                int sv = vc == V_N ? -1 : 1;
                for (int y = SUELO + 5; y <= SUELO + 6; y++) {
                    put(m, uc + su * 2, y, vc, Pal.AIR);
                    put(m, uc, y, vc + sv * 2, Pal.AIR);
                }
                chapitel(m, uc - 2, vc - 2, uc + 2, vc + 2, top + 1);
                put(m, uc, top + 4, vc, b(Blocks.END_ROD));
            }
    }

    /**
     * La puerta del sur: el cuerpo macizo con el paso de cinco (el arco apuntado de punta a punta), dos torres con chapitel,
     * los pendones, el ojo que llora sobre el arco, faroles de almas a los lados y las hojas de la puerta (una abierta contra
     * el paso, la otra caida en el camino).
     */
    private void puerta(Pilotes.Lista m) {
        int medio = SUELO + 14, torre = SUELO + 20;
        for (int u = -11; u <= 11; u++)
            for (int v = PUERTA_V0; v <= PUERTA_V1; v++) {
                boolean esTorre = Math.abs(u) >= 5;
                if (esTorre && v == PUERTA_V0) continue;
                int tope = esTorre ? torre : medio;
                for (int y = base(u, v); y <= tope; y++) put(m, u, y, v, sillar(u, y, v, tope, esTorre ? SUELO + ALTO_MURO + 1 : -1));
            }
        // el paso, con su boveda apuntada
        for (int v = PUERTA_V0; v <= PUERTA_V1; v++) hueco(m, true, v, 0, 2, PISO, SUELO + 4, false, false);
        // las dos caras: las jambas y la rosca del arco de sillares oscuros, con la clave labrada
        for (int v : new int[]{PUERTA_V0, PUERTA_V1}) {
            for (int s : LADOS) {
                for (int y = PISO; y <= SUELO + 5; y++) put(m, 3 * s, y, v, oscura(x(3 * s), y, z(v)));
                put(m, 2 * s, SUELO + 6, v, oscura(x(2 * s), SUELO + 6, z(v)));
                put(m, s, SUELO + 7, v, oscura(x(s), SUELO + 7, z(v)));
            }
            put(m, 0, SUELO + 7, v, b(Blocks.CHISELED_DEEPSLATE));
        }
        // el ojo que llora: la obsidiana llorosa en su medallon, sobre el arco de fuera
        int ye = SUELO + 10;
        put(m, 0, ye, PUERTA_V1, b(Blocks.CRYING_OBSIDIAN));
        for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) put(m, d[0], ye + d[1], PUERTA_V1, b(Blocks.CHISELED_DEEPSLATE));
        for (int[] d : new int[][]{{1, 1}, {-1, 1}, {1, -1}, {-1, -1}}) put(m, d[0], ye + d[1], PUERTA_V1, b(Blocks.POLISHED_DEEPSLATE));
        // las almenas del cuerpo del medio
        for (int u = -4; u <= 4; u++)
            for (int v : new int[]{PUERTA_V0, PUERTA_V1}) {
                if ((u & 1) == 0) {
                    put(m, u, medio + 1, v, piedra(x(u), medio + 1, z(v)));
                    put(m, u, medio + 2, v, losa(Blocks.DEEPSLATE_BRICK_SLAB, SlabType.BOTTOM));
                } else put(m, u, medio + 1, v, losa(Blocks.DEEPSLATE_BRICK_SLAB, SlabType.BOTTOM));
            }
        // las torres: su ventana de vidriera, el chapitel y la aguja con su luz
        for (int s : LADOS) {
            int u0 = s < 0 ? -11 : 5, u1 = s < 0 ? -5 : 11, uc = 8 * s;
            hueco(m, true, PUERTA_V1, uc, 0, SUELO + 15, SUELO + 17, true, false);
            hueco(m, false, u0 == -11 ? -11 : 11, 44, 0, SUELO + 15, SUELO + 17, true, false);
            chapitel(m, u0, PUERTA_V0 + 1, u1, PUERTA_V1, torre + 1);
            une(m, uc, torre + 5, 44, Pal.IRON_BARS);
            une(m, uc, torre + 6, 44, Pal.IRON_BARS);
            put(m, uc, torre + 7, 44, b(Blocks.END_ROD));
            // los pendones morados, por fuera y por dentro
            put(m, uc, SUELO + 12, PUERTA_V1 + 1, pendon(Direction.SOUTH));
            put(m, uc, SUELO + 12, PUERTA_V0, pendon(Direction.NORTH));
            // los faroles de almas a los lados del paso, fuera y dentro
            farol(m, 4 * s, PUERTA_V1 + 2);
            farol(m, 4 * s, PUERTA_V0 - 2);
        }
        // las hojas de la puerta: una abierta contra el paso; la otra, caida de bruces en el camino
        for (int v = PUERTA_V0; v <= PUERTA_V0 + 1; v++)
            for (int y = PISO; y <= SUELO + 4; y++) put(m, 2, y, v, Pal.DO_PLANKS);
        for (int v = PUERTA_V1 + 2; v <= PUERTA_V1 + 6; v++)
            for (int u = -3; u <= -2; u++)
                put(m, u, pie(x(u), z(v)), v, Blocks.DARK_OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.SOUTH)
                        .setValue(TrapDoorBlock.HALF, Half.BOTTOM));
    }

    /** Del paso de la puerta al santuario de la Puerta de la Academia: cinco de ancho, de losas de caliza y grava, con faroles. */
    private void camino(Pilotes.Lista m) {
        for (int v = PUERTA_V1 + 1; v <= 62; v++)
            for (int u = -2; u <= 2; u++) {
                if (Pilotes.cercaSantuario(x(u), z(v))) continue;
                int y = superficie(x(u), z(v));
                if (u <= -2 && v >= PUERTA_V1 + 2 && v <= PUERTA_V1 + 6) continue;   // ahi esta la hoja caida
                put(m, u, y, v, losas(Hash.pct(x(u), y, z(v), SEMILLA + 61)));
                for (int k = 1; k <= 3; k++) put(m, u, y + k, v, Pal.AIR);
            }
        for (int v : new int[]{54, 60})
            for (int s : LADOS)
                if (!Pilotes.cercaSantuario(x(4 * s), z(v))) farol(m, 4 * s, v);
    }

    // ------------------------------------------------------------------ el patio de entrada y el claustro

    private void patio(Pilotes.Lista m) {
        for (int s : LADOS) {
            pebetero(m, 6 * s, 35);
            arbol(m, 15 * s, 34, 6);
        }
        arbol(m, -27, 38, 5);
        arbol(m, 29, 37, 4);
    }

    private static boolean galeria(int u, int v) {
        if (Math.abs(u) > CLAUSTRO_U || v < CLAUSTRO_V0 || v >= CLAUSTRO_V1) return false;
        boolean arcada = Math.abs(u) == JARDIN_U + 1 && v >= JARDIN_V0 - 1 && v <= JARDIN_V1 + 1
                || (v == JARDIN_V0 - 1 || v == JARDIN_V1 + 1) && Math.abs(u) <= JARDIN_U + 1;
        boolean jardin = Math.abs(u) <= JARDIN_U && v >= JARDIN_V0 && v <= JARDIN_V1;
        return !arcada && !jardin;
    }

    private void claustro(Pilotes.Lista m) {
        int techo = SUELO + 6;
        // el muro del sur (la fachada al patio de entrada), con almenas y el portal apuntado
        int fachada = SUELO + 9;
        for (int u = -CLAUSTRO_U; u <= CLAUSTRO_U; u++) {
            for (int y = SUELO; y <= fachada; y++) put(m, u, y, CLAUSTRO_V1, sillar(u, y, CLAUSTRO_V1, fachada, -1));
            if ((u & 1) == 0) {
                put(m, u, fachada + 1, CLAUSTRO_V1, piedra(x(u), fachada + 1, z(CLAUSTRO_V1)));
                put(m, u, fachada + 2, CLAUSTRO_V1, losa(Blocks.DEEPSLATE_BRICK_SLAB, SlabType.BOTTOM));
            } else put(m, u, fachada + 1, CLAUSTRO_V1, losa(Blocks.DEEPSLATE_BRICK_SLAB, SlabType.BOTTOM));
        }
        hueco(m, true, CLAUSTRO_V1, 0, 2, PISO, SUELO + 4, false, false);
        for (int s : LADOS) {
            for (int y = PISO; y <= SUELO + 5; y++) put(m, 3 * s, y, CLAUSTRO_V1, oscura(x(3 * s), y, z(CLAUSTRO_V1)));
            put(m, 2 * s, SUELO + 6, CLAUSTRO_V1, oscura(x(2 * s), SUELO + 6, z(CLAUSTRO_V1)));
            put(m, s, SUELO + 7, CLAUSTRO_V1, oscura(x(s), SUELO + 7, z(CLAUSTRO_V1)));
            put(m, 5 * s, SUELO + 6, CLAUSTRO_V1 + 1, pendon(Direction.SOUTH));
            // las ventanas de la fachada, a los lados del portal
            for (int k : new int[]{10, 17}) hueco(m, true, CLAUSTRO_V1, k * s, 0, SUELO + 3, SUELO + 5, true, Hash.pct(k, s, 9, SEMILLA + 7) < 40);
        }
        put(m, 0, SUELO + 7, CLAUSTRO_V1, b(Blocks.CHISELED_DEEPSLATE));
        put(m, 0, SUELO + 8, CLAUSTRO_V1, b(Blocks.CRYING_OBSIDIAN));
        // los trozos del muro del norte que no son la biblioteca (entre ella y las alas)
        for (int a = BIB_U + 1; a <= CLAUSTRO_U; a++)
            for (int s : LADOS) for (int y = SUELO; y <= SUELO + 8; y++) put(m, s * a, y, BIB_V1, sillar(s * a, y, BIB_V1, SUELO + 8, -1));
        // las galerias: el techo (sillares) con su cubierta de losas de pizarra
        for (int u = -CLAUSTRO_U; u <= CLAUSTRO_U; u++)
            for (int v = CLAUSTRO_V0; v < CLAUSTRO_V1; v++) {
                if (!galeria(u, v)) continue;
                put(m, u, techo, v, oscura(x(u), techo, z(v)));
                put(m, u, techo + 1, v, losa(Blocks.DEEPSLATE_TILE_SLAB, SlabType.BOTTOM));
            }
        // las cuatro arquerias (el arco del medio, mas ancho, donde llegan los senderos del jardin)
        arqueria(m, true, JARDIN_V0 - 1, -JARDIN_U - 1, JARDIN_U + 1);
        arqueria(m, true, JARDIN_V1 + 1, -JARDIN_U - 1, JARDIN_U + 1);
        arqueria(m, false, -JARDIN_U - 1, JARDIN_V0 - 1, JARDIN_V1 + 1);
        arqueria(m, false, JARDIN_U + 1, JARDIN_V0 - 1, JARDIN_V1 + 1);
        // faroles de almas colgando en las galerias
        for (int u : new int[]{-15, -5, 5, 15}) {
            put(m, u, techo - 1, 0, Pal.SOUL_LANTERN_HANG);
            put(m, u, techo - 1, CLAUSTRO_V1 - 3, Pal.SOUL_LANTERN_HANG);
        }
        for (int v : new int[]{6, FUENTE_V, 20})
            for (int s : LADOS) put(m, 20 * s, techo - 1, v, Pal.SOUL_LANTERN_HANG);
        // bancos de piedra contra el muro de fuera de las galerias del oeste y del este
        for (int v : new int[]{6, 7, 19, 20})
            for (int s : LADOS) put(m, CLAUSTRO_U * s, PISO, v, escalera(Blocks.DEEPSLATE_BRICK_STAIRS, s < 0 ? Direction.WEST : Direction.EAST, false));
        // el jardin: la fuente de amatista en el centro, los pebeteros en la entrada de los senderos y dos arboles secos
        fuente(m);
        for (int s : LADOS) {
            pebetero(m, 3 * s, JARDIN_V0 + 1);
            pebetero(m, 3 * s, JARDIN_V1 - 1);
        }
        arbol(m, -12, 18, 5);
        arbol(m, 12, 7, 4);
    }

    /**
     * Una arqueria recta de columnas de pizarra (cada tres, y en los extremos) con su capitel labrado, los arcos apuntados entre
     * ellas (cada escalon mira a su columna mas cercana; el del medio de un vano impar queda abierto), el arquitrabe encima y
     * su pretil de merlones bajos. En el medio no hay columna: ahi entra el sendero, por un arco de cinco.
     */
    private static void arqueria(Pilotes.Lista m, boolean enU, int fila, int a, int bb) {
        int c = (a + bb) / 2, techo = SUELO + 6;
        for (int k = a; k <= bb; k++) {
            int u = enU ? k : fila, v = enU ? fila : k, d = k - c;
            put(m, u, techo, v, oscura(x(u), techo, z(v)));
            put(m, u, techo + 1, v, (d & 1) == 0 ? oscura(x(u), techo + 1, z(v)) : losa(Blocks.DEEPSLATE_BRICK_SLAB, SlabType.BOTTOM));
            if (columna(k, a, bb, c)) {
                for (int y = PISO; y <= SUELO + 4; y++) une(m, u, y, v, b(Blocks.POLISHED_DEEPSLATE_WALL));
                put(m, u, SUELO + 5, v, b(Blocks.CHISELED_DEEPSLATE));
                continue;
            }
            int antes = 1, despues = 1;
            while (!columna(k - antes, a, bb, c)) antes++;
            while (!columna(k + despues, a, bb, c)) despues++;
            put(m, u, SUELO + 5, v, antes == despues ? Pal.AIR : dovela(enU, antes < despues));
        }
    }

    private static boolean columna(int k, int a, int bb, int c) {
        if (k <= a || k >= bb) return true;
        int d = k - c;
        return d != 0 && Math.abs(d) % 3 == 0;
    }

    /** La fuente del jardin: un pilon ochavado de pizarra con el agua y, en medio, el obelisco de amatista con la obsidiana que llora. */
    private void fuente(Pilotes.Lista m) {
        int fv = FUENTE_V;
        for (int du = -4; du <= 4; du++)
            for (int dv = -4; dv <= 4; dv++) {
                int r = Math.abs(du) + Math.abs(dv);
                if (r > 6) continue;
                boolean borde = Math.abs(du) == 4 || Math.abs(dv) == 4 || r == 6;
                if (borde) {
                    put(m, du, SUELO, fv + dv, b(Blocks.POLISHED_DEEPSLATE));
                    put(m, du, PISO, fv + dv, losa(Blocks.POLISHED_DEEPSLATE_SLAB, SlabType.BOTTOM));
                } else {
                    put(m, du, SUELO - 1, fv + dv, Pal.DS_TILES);
                    put(m, du, SUELO, fv + dv, Pal.WATER);
                }
            }
        put(m, 0, SUELO, fv, b(Blocks.CHISELED_DEEPSLATE));
        put(m, 0, PISO, fv, b(Blocks.POLISHED_DEEPSLATE));
        put(m, 0, PISO + 1, fv, b(Blocks.AMETHYST_BLOCK));
        put(m, 0, PISO + 2, fv, b(Blocks.AMETHYST_BLOCK));
        put(m, 0, PISO + 3, fv, b(Blocks.CRYING_OBSIDIAN));
        put(m, 0, PISO + 4, fv, racimo(Blocks.AMETHYST_CLUSTER, Direction.UP));
        put(m, 1, PISO + 1, fv, racimo(Blocks.LARGE_AMETHYST_BUD, Direction.EAST));
        put(m, -1, PISO + 1, fv, racimo(Blocks.LARGE_AMETHYST_BUD, Direction.WEST));
        put(m, 0, PISO + 2, fv - 1, racimo(Blocks.MEDIUM_AMETHYST_BUD, Direction.NORTH));
        put(m, 0, PISO + 2, fv + 1, racimo(Blocks.MEDIUM_AMETHYST_BUD, Direction.SOUTH));
        cristales += 5;
    }

    // ------------------------------------------------------------------ la Gran Biblioteca

    private void biblioteca(Pilotes.Lista m) {
        int top = SUELO + BIB_ALTO;
        // los muros (el zocalo oscuro, la imposta a la altura del altillo y la cornisa)
        for (int v = BIB_V0; v <= BIB_V1; v++)
            for (int s : LADOS) for (int y = SUELO; y <= top; y++) put(m, s * BIB_U, y, v, sillar(s * BIB_U, y, v, top, ALTILLO));
        for (int u = -BIB_U + 1; u <= BIB_U - 1; u++)
            for (int v : new int[]{BIB_V0, BIB_V1}) for (int y = SUELO; y <= top; y++) put(m, u, y, v, sillar(u, y, v, top, ALTILLO));
        // el tejado a dos aguas (la cumbrera de norte a sur) con los hastiales de piedra, y su remate en las puntas
        tejado(m, -BIB_U - 1, BIB_U + 1, BIB_V0 - 1, BIB_V1 + 1, top, BIB_V0, BIB_V1);
        int cumbre = top + BIB_U + 1;
        for (int v : new int[]{BIB_V0, BIB_V1}) {
            put(m, 0, cumbre + 1, v, b(Blocks.CHISELED_DEEPSLATE));
            put(m, 0, cumbre + 2, v, b(Blocks.END_ROD));
        }
        // los contrafuertes de los lados (de dos escalones) y los del hastial del norte
        for (int v = BIB_V0; v < BIB_V1; v += 6)
            for (int s : LADOS) {
                Direction al = s < 0 ? Direction.EAST : Direction.WEST;
                for (int y = SUELO; y <= SUELO + 13; y++) put(m, s * (BIB_U + 1), y, v, y <= PISO ? oscura(x(s * (BIB_U + 1)), y, z(v)) : piedra(x(s * (BIB_U + 1)), y, z(v)));
                put(m, s * (BIB_U + 1), SUELO + 14, v, escalera(Blocks.DEEPSLATE_BRICK_STAIRS, al, false));
                for (int y = SUELO; y <= SUELO + 8; y++) put(m, s * (BIB_U + 2), y, v, y <= PISO ? oscura(x(s * (BIB_U + 2)), y, z(v)) : piedra(x(s * (BIB_U + 2)), y, z(v)));
                put(m, s * (BIB_U + 2), SUELO + 9, v, escalera(Blocks.DEEPSLATE_BRICK_STAIRS, al, false));
            }
        for (int s : LADOS)
            for (int a : new int[]{6, 12}) {
                for (int y = SUELO; y <= SUELO + 13; y++) put(m, s * a, y, BIB_V0 - 1, y <= PISO ? oscura(x(s * a), y, z(BIB_V0 - 1)) : piedra(x(s * a), y, z(BIB_V0 - 1)));
                put(m, s * a, SUELO + 14, BIB_V0 - 1, escalera(Blocks.DEEPSLATE_BRICK_STAIRS, Direction.SOUTH, false));
                for (int y = SUELO; y <= SUELO + 8; y++) put(m, s * a, y, BIB_V0 - 2, y <= PISO ? oscura(x(s * a), y, z(BIB_V0 - 2)) : piedra(x(s * a), y, z(BIB_V0 - 2)));
                put(m, s * a, SUELO + 9, BIB_V0 - 2, escalera(Blocks.DEEPSLATE_BRICK_STAIRS, Direction.SOUTH, false));
            }
        // los ventanales: arriba, sobre el altillo, de tres con su punta; abajo, una lanceta; alguno roto
        for (int w = BIB_V0 + 3; w < BIB_V1; w += 6)
            for (int s : LADOS) {
                boolean rota = Hash.pct(w, s, 3, SEMILLA + 21) < 30;
                hueco(m, false, s * BIB_U, w, 1, SUELO + 9, SUELO + 13, true, rota);
                hueco(m, false, s * BIB_U, w, 0, SUELO + 2, SUELO + 4, true, false);
                ventanas += 2;
            }
        // la puerta (desde la galeria del norte del claustro) y el ventanal encima
        hueco(m, true, BIB_V1, 0, 2, PISO, SUELO + 4, false, false);
        for (int s : LADOS) for (int y = PISO; y <= SUELO + 5; y++) put(m, 3 * s, y, BIB_V1, oscura(x(3 * s), y, z(BIB_V1)));
        hueco(m, true, BIB_V1, 0, 1, SUELO + 9, SUELO + 13, true, false);
        // los dos rosetones de los hastiales
        roseton(m, BIB_V0, SUELO + 23, 4.5);
        roseton(m, BIB_V1, SUELO + 23, 3.5);
        ventanas += 3;
        // ---- dentro: la alfombra morada del pasillo (gastada)
        for (int v = -43; v <= BIB_V1 - 1; v++)
            for (int u = -1; u <= 1; u++)
                if (Hash.pct(x(u), PISO, z(v), SEMILLA + 41) < 82) put(m, u, PISO, v, b(Blocks.PURPLE_CARPET));
        // las filas de estantes (con alguno vacio o caido; en dos se vino abajo el medio y los libros estan por el suelo)
        for (int f = 0; f < FILAS.length; f++) {
            int v = FILAS[f];
            for (int s : LADOS) {
                boolean hundida = s < 0 ? f == 1 : f == 6;
                for (int a = 4; a <= 11; a++) {
                    int u = s * a;
                    boolean punta = a == 4 || a == 11;
                    boolean hueca = hundida && a >= 6 && a <= 8;
                    for (int y = PISO; y <= SUELO + 4; y++) {
                        BlockState e;
                        int p = Hash.pct(x(u), y, z(v), SEMILLA + 43);
                        if (punta) e = b(Blocks.STRIPPED_DARK_OAK_LOG);
                        else if (hueca && y > PISO) e = Pal.AIR;
                        else e = p < 7 ? estanteVacio(p % 2 == 0 ? Direction.NORTH : Direction.SOUTH) : p < 10 ? Pal.AIR : Pal.BOOKSHELF;
                        put(m, u, y, v, e);
                    }
                    put(m, u, SUELO + 5, v, hueca ? Pal.AIR : losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
                    if (!punta) estantes++;
                }
                if (hundida) {
                    int dv = s < 0 ? 1 : -1;
                    for (int a = 6; a <= 8; a++) put(m, s * a, PISO, v + dv, Pal.BOOKSHELF);
                    put(m, s * 7, PISO, v + 2 * dv, Pal.BOOKSHELF);
                    put(m, s * 6, PISO + 1, v + dv, Pal.BOOKSHELF);
                }
                // los atriles al final de las filas, mirando al pasillo (en una fila si y otra no)
                if ((f & 1) == 0) {
                    put(m, 3 * s, PISO, v, atril(s < 0 ? Direction.EAST : Direction.WEST));
                    atriles++;
                }
            }
        }
        // las mesas de lectura entre las filas, con velas
        for (int vt : new int[]{-39, -31, -23, -15})
            for (int s : LADOS)
                for (int a = 5; a <= 10; a++) {
                    put(m, s * a, PISO, vt, losa(Blocks.DARK_OAK_SLAB, SlabType.TOP));
                    int p = Hash.pct(x(s * a), PISO + 1, z(vt), SEMILLA + 44);
                    if (p < 26) put(m, s * a, PISO + 1, vt, vela(1 + p % 3, p % 4 != 0));
                    mesas++;
                }
        // el altillo: el suelo de tablas sobre los pilares, la baranda con velas, los estantes contra los muros y las escalas
        for (int v = BIB_V0 + 1; v <= BIB_V1 - 1; v++)
            for (int s : LADOS) {
                for (int a = 12; a <= 15; a++) put(m, s * a, ALTILLO, v, Pal.DO_PLANKS);
                une(m, s * 12, ALTILLO + 1, v, Pal.DO_FENCE);
                if ((v & 3) == 0 && Hash.pct(v, s, 5, SEMILLA + 45) < 60) put(m, s * 12, ALTILLO + 2, v, vela(1 + (v & 1), true));
                boolean ventana = false;
                for (int w = BIB_V0 + 3; w < BIB_V1; w += 6) if (Math.abs(v - w) <= 1) ventana = true;
                if (!ventana && v < BIB_V1 - 2) for (int y = ALTILLO + 1; y <= ALTILLO + 4; y++) put(m, s * 15, y, v, Pal.BOOKSHELF);
            }
        for (int v = BIB_V0 + 6; v < BIB_V1; v += 6)
            for (int s : LADOS) for (int y = PISO; y < ALTILLO; y++) put(m, s * 12, y, v, Pal.DO_LOG);
        for (int s : LADOS) for (int y = PISO; y <= ALTILLO; y++) put(m, s * 13, y, BIB_V1 - 1, escala(Direction.NORTH));
        // las lamparas: colgando de la cumbrera, una cruz de roble con cinco faroles de almas
        for (int vc : new int[]{-40, -27, -14}) {
            int yl = SUELO + 13;
            for (int y = yl + 1; y < cumbre; y++) put(m, 0, y, vc, Pal.CHAIN);
            une(m, 0, yl, vc, Pal.DO_FENCE);
            put(m, 0, yl - 1, vc, Pal.SOUL_LANTERN_HANG);
            for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                une(m, d[0], yl, vc + d[1], Pal.DO_FENCE);
                put(m, d[0], yl - 1, vc + d[1], Pal.SOUL_LANTERN_HANG);
            }
        }
        // ---- el estrado del grimorio, al fondo: el escalon, el suelo ajedrezado, el muro de libros y el relicario de amatista
        for (int u = -11; u <= 11; u++) {
            put(m, u, PISO, -44, escalera(Blocks.POLISHED_DEEPSLATE_STAIRS, Direction.NORTH, false));
            for (int v = BIB_V0 + 1; v <= -45; v++) put(m, u, PISO, v, (u + v & 1) == 0 ? b(Blocks.POLISHED_DEEPSLATE) : b(Blocks.CALCITE));
        }
        for (int a = 2; a <= 11; a++)
            for (int s : LADOS) for (int y = PISO + 1; y <= SUELO + 7; y++) put(m, s * a, y, BIB_V0 + 1, Pal.BOOKSHELF);
        put(m, 0, PISO + 1, BIB_V0 + 1, b(Blocks.AMETHYST_BLOCK));
        put(m, 0, PISO + 2, BIB_V0 + 1, b(Blocks.AMETHYST_BLOCK));
        put(m, 0, PISO + 3, BIB_V0 + 1, b(Blocks.CRYING_OBSIDIAN));
        put(m, 0, PISO + 4, BIB_V0 + 1, racimo(Blocks.AMETHYST_CLUSTER, Direction.UP));
        for (int s : LADOS) {
            put(m, s, PISO + 1, BIB_V0 + 1, b(Blocks.AMETHYST_BLOCK));
            put(m, s, PISO + 2, BIB_V0 + 1, racimo(Blocks.LARGE_AMETHYST_BUD, Direction.UP));
            // las velas a los lados del atril, en sus pies de pizarra labrada
            put(m, 2 * s, PISO + 1, -48, b(Blocks.CHISELED_DEEPSLATE));
            put(m, 2 * s, PISO + 2, -48, vela(3, true));
            // los pendones sobre el muro de libros
            put(m, 6 * s, SUELO + 12, BIB_V0 + 1, pendon(Direction.SOUTH));
        }
        cristales += 4;
    }

    /** Un rosetón en el hastial (fila v): el aro de pizarra, la cruz de la traceria y la vidriera morada entre medias. */
    private static void roseton(Pilotes.Lista m, int v, int yc, double r) {
        int ri = (int) Math.ceil(r) + 1;
        for (int du = -ri; du <= ri; du++)
            for (int dy = -ri; dy <= ri; dy++) {
                double d = Math.hypot(du, dy);
                if (d > r + 0.75) continue;
                if (d > r - 0.35) put(m, du, yc + dy, v, oscura(x(du), yc + dy, z(v)));
                else if (du == 0 && dy == 0) put(m, du, yc + dy, v, b(Blocks.CHISELED_DEEPSLATE));
                else if (du == 0 || dy == 0) put(m, du, yc + dy, v, b(Blocks.POLISHED_DEEPSLATE));
                else une(m, du, yc + dy, v, VIDRIERA);
            }
    }

    // ------------------------------------------------------------------ las alas: el scriptorium y el aula

    /** El cascaron de un ala (s = -1, el scriptorium al oeste; s = 1, el aula al este): muros, puerta, ventanas, vigas y tejado. */
    private void ala(Pilotes.Lista m, int s) {
        int ui = s * ALA_U0, ue = s * ALA_U1, top = SUELO + ALA_ALTO;
        for (int v = ALA_V0; v <= ALA_V1; v++)
            for (int y = SUELO; y <= top; y++) {
                put(m, ui, y, v, sillar(ui, y, v, top, -1));
                put(m, ue, y, v, sillar(ue, y, v, top, -1));
            }
        for (int a = ALA_U0 + 1; a < ALA_U1; a++)
            for (int v : new int[]{ALA_V0, ALA_V1}) for (int y = SUELO; y <= top; y++) put(m, s * a, y, v, sillar(s * a, y, v, top, -1));
        // la puerta desde la galeria del claustro
        hueco(m, false, ui, FUENTE_V, 1, PISO, SUELO + 3, false, false);
        // las ventanas de fuera y una en cada hastial
        for (int v : new int[]{1, 5, 9, 17, 21, 25}) {
            hueco(m, false, ue, v, 0, SUELO + 2, SUELO + 4, true, false);
            ventanas++;
        }
        int uh = s * (ALA_U0 + ALA_U1) / 2;
        for (int v : new int[]{ALA_V0, ALA_V1}) hueco(m, true, v, uh, 0, top + 2, top + 4, true, false);
        // las vigas de roble a lo alto de los muros, y un farol de almas colgando de cada una
        for (int v = 1; v <= ALA_V1 - 4; v += 6) {
            for (int a = ALA_U0 + 1; a < ALA_U1; a++) put(m, s * a, top, v, tronco(Direction.Axis.X));
            put(m, s * (ALA_U0 + 5), top - 1, v, Pal.SOUL_LANTERN_HANG);
        }
        // el tejado a dos aguas, con sus hastiales
        tejado(m, Math.min(ui, ue) - 1, Math.max(ui, ue) + 1, ALA_V0 - 1, ALA_V1 + 1, top, ALA_V0, ALA_V1);
    }

    /** El scriptorium: los pupitres de los copistas junto a las ventanas (con su silla y sus velas), la mesa larga y los estantes. */
    private void scriptorium(Pilotes.Lista m) {
        for (int v : new int[]{1, 5, 9, 17, 21, 25}) {
            put(m, -33, PISO, v, atril(Direction.EAST));
            if (v != 5 && v != 17) put(m, -32, PISO, v, escalera(Blocks.DARK_OAK_STAIRS, Direction.EAST, false));
            put(m, -33, PISO, v + 1, vela(2, (v & 2) == 0));
            pupitres++;
        }
        // la mesa larga del medio (con un paso enfrente de la puerta), con velas y papeles
        for (int v = 3; v <= 23; v++) {
            if (Math.abs(v - FUENTE_V) <= 1) continue;
            put(m, -28, PISO, v, losa(Blocks.DARK_OAK_SLAB, SlabType.TOP));
            int p = Hash.pct(x(-28), PISO + 1, z(v), SEMILLA + 47);
            if (p < 18) put(m, -28, PISO + 1, v, vela(1 + p % 3, true));
            else if (p < 34) put(m, -28, PISO + 1, v, b(Blocks.WHITE_CARPET));
        }
        // los estantes contra el muro de dentro (menos en la puerta) y papeles y manchas de tinta por el suelo
        for (int v = ALA_V0 + 1; v < ALA_V1; v++) {
            if (Math.abs(v - FUENTE_V) <= 2) continue;
            for (int y = PISO; y <= SUELO + 3; y++) put(m, -(ALA_U0 + 1), y, v, Hash.pct(v, y, 1, SEMILLA + 48) < 12 ? Pal.AIR : Pal.BOOKSHELF);
        }
        for (int v = ALA_V0 + 1; v < ALA_V1; v++)
            for (int u = -31; u <= -29; u++) {
                int p = Hash.pct(x(u), PISO, z(v), SEMILLA + 49);
                if (p < 5) put(m, u, PISO, v, b(Blocks.WHITE_CARPET));
                else if (p < 8) put(m, u, PISO, v, b(Blocks.BLACK_CARPET));
            }
    }

    /** El aula: la catedra al norte (la tarima, el atril y la pizarra) y los bancos con sus pupitres mirandola. */
    private void aula(Pilotes.Lista m) {
        for (int u = ALA_U0 + 1; u < ALA_U1; u++) {
            for (int v = ALA_V0 + 1; v <= 1; v++) put(m, u, PISO, v, Pal.DO_PLANKS);
            put(m, u, PISO, 2, escalera(Blocks.DARK_OAK_STAIRS, Direction.NORTH, false));
        }
        put(m, 28, PISO + 1, 0, atril(Direction.SOUTH));
        put(m, 25, PISO + 1, -2, vela(3, true));
        put(m, 32, PISO + 1, -2, vela(2, true));
        for (int u = 25; u <= 32; u++) for (int y = SUELO + 3; y <= SUELO + 5; y++) put(m, u, y, ALA_V0, b(Blocks.POLISHED_BLACKSTONE));
        for (int r : new int[]{6, 10, 14, 18, 22, 26})
            for (int u = 25; u <= 32; u++) {
                if (u == 28 || u == 29) continue;
                put(m, u, PISO, r - 1, losa(Blocks.DARK_OAK_SLAB, SlabType.TOP));
                put(m, u, PISO, r, escalera(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, false));
                int p = Hash.pct(x(u), PISO + 1, z(r - 1), SEMILLA + 46);
                if (p < 14) put(m, u, PISO + 1, r - 1, vela(1, true));
                else if (p < 30) put(m, u, PISO + 1, r - 1, b(Blocks.WHITE_CARPET));
                bancos++;
            }
    }

    // ------------------------------------------------------------------ la torre del archivo

    private void torre(Pilotes.Lista m) {
        int u0 = TORRE_U0, u1 = TORRE_U1, v0 = TORRE_V0, v1 = TORRE_V1, top = TORRE_ALTO;
        int uc = (u0 + u1) / 2, vc = (v0 + v1) / 2;
        int[] pisos = {SUELO + 12, SUELO + 24, ARCHIVO - 1};
        for (int u = u0; u <= u1; u++)
            for (int v = v0; v <= v1; v++) {
                boolean borde = u == u0 || u == u1 || v == v0 || v == v1;
                if (!borde) {
                    for (int yf : pisos) put(m, u, yf, v, Pal.DO_PLANKS);
                    continue;
                }
                boolean esquina = (u == u0 || u == u1) && (v == v0 || v == v1);
                for (int y = base(u, v); y <= top; y++) {
                    boolean franja = y <= PISO || y == top || y == SUELO + 12 || y == SUELO + 24 || y == ARCHIVO - 1;
                    put(m, u, y, v, franja || esquina ? oscura(x(u), y, z(v)) : piedra(x(u), y, z(v)));
                }
            }
        // la puerta al sur y la escala por dentro, pegada al muro del norte, de abajo al archivo
        hueco(m, true, v1, uc, 1, PISO, SUELO + 3, false, false);
        for (int y = PISO; y <= ARCHIVO - 1; y++) put(m, uc, y, v0 + 1, escala(Direction.SOUTH));
        // las saeteras (al este, al oeste y al sur)
        for (int y : new int[]{SUELO + 7, SUELO + 18, SUELO + 28})
            for (int k = 0; k <= 1; k++) {
                put(m, u0, y + k, vc, Pal.AIR);
                put(m, u1, y + k, vc, Pal.AIR);
                put(m, uc, y + k, v1, Pal.AIR);
            }
        // el archivo: un ventanal en cada cara, los estantes alrededor, el arcon (ver trozos), un atril y velas
        hueco(m, true, v0, uc, 1, ARCHIVO + 1, ARCHIVO + 3, true, false);
        hueco(m, true, v1, uc, 1, ARCHIVO + 1, ARCHIVO + 3, true, true);
        hueco(m, false, u0, vc, 1, ARCHIVO + 1, ARCHIVO + 3, true, false);
        hueco(m, false, u1, vc, 1, ARCHIVO + 1, ARCHIVO + 3, true, false);
        ventanas += 4;
        for (int u = u0 + 1; u < u1; u++)
            for (int v = v0 + 1; v < v1; v++) {
                boolean pegado = u == u0 + 1 || u == u1 - 1 || v == v0 + 1 || v == v1 - 1;
                boolean libre = Math.abs(u - uc) <= 1 && (v == v0 + 1 || v == v1 - 1) || Math.abs(v - vc) <= 1 && (u == u0 + 1 || u == u1 - 1);
                if (!pegado || libre) continue;
                for (int y = ARCHIVO; y <= ARCHIVO + 2; y++) put(m, u, y, v, Pal.BOOKSHELF);
            }
        put(m, uc, ARCHIVO, vc, atril(Direction.SOUTH));
        put(m, uc + 2, ARCHIVO, vc + 2, vela(4, true));
        put(m, uc - 2, ARCHIVO, vc + 2, vela(2, true));
        put(m, uc, ARCHIVO + 5, vc, Pal.SOUL_LANTERN_HANG);
        for (int y = ARCHIVO + 6; y <= top; y++) put(m, uc, y, vc, Pal.CHAIN);
        // la aguja: el chapitel, el mastil de hierro y la luz
        chapitel(m, u0, v0, u1, v1, top + 1);
        for (int y = top + 6; y <= top + 8; y++) une(m, uc, y, vc, Pal.IRON_BARS);
        put(m, uc, top + 9, vc, b(Blocks.END_ROD));
    }

    // ------------------------------------------------------------------ la veta de amatista y las ruinas del aula vieja

    /** El afloramiento de la veta de amatista (al noroeste): un lomo de calcita, toba y basalto con la amatista asomando. */
    private void veta(Pilotes.Lista m) {
        int cu = -27, cv = -33;
        for (int u = -35; u <= -19; u++)
            for (int v = -48; v <= -18; v++) {
                double d = Math.hypot(u - cu, (v - cv) * 0.6) + 2.0 * (Hash.pct(u >> 1, 0, v >> 1, SEMILLA + 71) / 100.0 - 0.5);
                int h = (int) Math.round(6.5 - d * 0.9);
                if (h <= 0) continue;
                for (int y = PISO; y <= SUELO + h; y++) {
                    int p = Hash.pct(x(u), y, z(v), SEMILLA + 72);
                    boolean arriba = y >= SUELO + h - 1;
                    BlockState s = arriba && p < 30 ? b(Blocks.AMETHYST_BLOCK) : p < 52 ? b(Blocks.CALCITE)
                            : p < 72 ? b(Blocks.SMOOTH_BASALT) : p < 90 ? b(Blocks.TUFF) : b(Blocks.AMETHYST_BLOCK);
                    put(m, u, y, v, s);
                }
                int ty = SUELO + h + 1, q = Hash.pct(x(u), ty, z(v), SEMILLA + 73);
                if (q < 7) {
                    put(m, u, ty, v, racimo(Blocks.AMETHYST_CLUSTER, Direction.UP));
                    cristales++;
                } else if (q < 15) {
                    put(m, u, ty, v, racimo(Blocks.LARGE_AMETHYST_BUD, Direction.UP));
                    cristales++;
                } else if (q < 24) put(m, u, ty, v, b(Blocks.MOSS_CARPET));
            }
        // las agujas de amatista que salen del lomo
        int[][] agujas = {{-27, -33, 5}, {-24, -38, 3}, {-30, -28, 4}, {-26, -41, 2}, {-29, -37, 3}};
        for (int[] a : agujas) {
            int u = a[0], v = a[1];
            double d = Math.hypot(u - cu, (v - cv) * 0.6) + 2.0 * (Hash.pct(u >> 1, 0, v >> 1, SEMILLA + 71) / 100.0 - 0.5);
            int y0 = SUELO + Math.max(1, (int) Math.round(6.5 - d * 0.9)) + 1;
            for (int y = y0; y < y0 + a[2]; y++) put(m, u, y, v, b(Blocks.AMETHYST_BLOCK));
            put(m, u, y0 + a[2], v, racimo(Blocks.AMETHYST_CLUSTER, Direction.UP));
            put(m, u + 1, y0 + a[2] - 1, v, racimo(Blocks.MEDIUM_AMETHYST_BUD, Direction.EAST));
            put(m, u, y0 + a[2] - 2, v - 1, racimo(Blocks.SMALL_AMETHYST_BUD, Direction.NORTH));
            cristales += 3;
        }
        arbol(m, -21, -12, 5);
        arbol(m, -33, -52, 6);
        arbol(m, -20, -54, 4);
    }

    /** Las ruinas del aula vieja (al noreste, entre la biblioteca y la muralla): muros rotos, columnas caidas y escombros. */
    private void ruinas(Pilotes.Lista m) {
        int u0 = 20, u1 = 32, v0 = -43, v1 = -12;
        for (int u = u0; u <= u1; u++)
            for (int v = v0; v <= v1; v++) {
                boolean borde = u == u0 || u == u1 || v == v0 || v == v1;
                if (!borde) continue;
                int seg = (u + v + 400) / 4;
                int p = Hash.pct(seg, 7, u == u0 || u == u1 ? 1 : 2, SEMILLA + 81);
                boolean esquina = (u == u0 || u == u1) && (v == v0 || v == v1);
                int h = esquina ? 7 + p % 3 : p < 25 ? 0 : p < 55 ? 1 + p % 2 : p < 85 ? 3 + p % 3 : 5 + p % 3;
                if (u == u0 && Math.abs(v + 28) <= 1) h = 0;   // la puerta del aula vieja, hacia la biblioteca
                for (int y = SUELO; y <= SUELO + h; y++) put(m, u, y, v, y <= PISO ? oscura(x(u), y, z(v)) : piedra(x(u), y, z(v)));
                if (h > 1 && Hash.pct(x(u), 5, z(v), SEMILLA + 82) < 35)
                    put(m, u, SUELO + h + 1, v, losa(Blocks.POLISHED_DIORITE_SLAB, SlabType.BOTTOM));
            }
        // las columnas: tocones de cuarzo en pie y dos tumbadas
        int[][] tocones = {{26, -40, 3}, {29, -31, 2}, {24, -18, 4}, {30, -19, 1}, {23, -30, 2}};
        for (int[] t : tocones) {
            for (int y = PISO; y < PISO + t[2]; y++) put(m, t[0], y, t[1], b(Blocks.QUARTZ_PILLAR));
            if (t[2] >= 3) put(m, t[0], PISO + t[2], t[1], b(Blocks.CHISELED_QUARTZ_BLOCK));
        }
        for (int v = -36; v <= -32; v++) put(m, 24, PISO, v, columnaCaida(Direction.Axis.Z));
        for (int u = 25; u <= 29; u++) put(m, u, PISO, -22, columnaCaida(Direction.Axis.X));
        // montones de escombros
        int[][] montones = {{22, -25}, {31, -38}, {27, -14}, {21, -40}};
        for (int[] q : montones)
            for (int du = -2; du <= 2; du++)
                for (int dv = -2; dv <= 2; dv++) {
                    int r = Math.abs(du) + Math.abs(dv);
                    if (r > 3) continue;
                    int p = Hash.pct(x(q[0] + du), 1, z(q[1] + dv), SEMILLA + 83);
                    int h = r <= 1 ? 2 : 1;
                    if (p < 20) continue;
                    for (int y = PISO; y < PISO + h; y++)
                        put(m, q[0] + du, y, q[1] + dv, p < 45 ? b(Blocks.COBBLED_DEEPSLATE) : p < 70 ? Pal.GRAVEL : p < 88 ? b(Blocks.CALCITE) : b(Blocks.TUFF));
                }
        arbol(m, 33, -8, 5);
        arbol(m, 18, -46, 4);
    }

    // ------------------------------------------------------------------ lo que lleva datos: el atril, los arcones, los barriles

    private void trozos() {
        int ya = PISO + 1;
        trozo(0, ya, -48, c -> c.lectern(x(0), ya, z(-48), Direction.SOUTH, Perdidos.libro(24)));
        trozo(5, ya, -49, c -> c.chest(x(5), ya, z(-49), Direction.SOUTH, BOTIN_BUENO));
        trozo(-5, ya, -49, c -> c.barrel(x(-5), ya, z(-49), Direction.UP, BOTIN));
        trozo(-33, PISO, ALA_V0 + 1, c -> c.barrel(x(-33), PISO, z(ALA_V0 + 1), Direction.UP, BOTIN));
        trozo(-33, PISO, ALA_V1 - 1, c -> c.barrel(x(-33), PISO, z(ALA_V1 - 1), Direction.UP, BOTIN));
        trozo(33, PISO, ALA_V1 - 1, c -> c.chest(x(33), PISO, z(ALA_V1 - 1), Direction.WEST, BOTIN));
        trozo(23, ARCHIVO, -55, c -> c.chest(x(23), ARCHIVO, z(-55), Direction.SOUTH, BOTIN));
    }

    private void trozo(int u, int y, int v, Trozo.Obra o) {
        elementos.add(new Trozo(6, x(u), y, z(v), x(u), y, z(v), o));
    }

    // ------------------------------------------------------------------ los eruditos

    private static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;

    private void ponerEsperas() {
        int A = Erudito.APRENDIZ, M = Erudito.MAESTRO, K = Erudito.CUSTODIO;
        int R = Enemigo.REZA, T = Enemigo.TUMBADO, D = Enemigo.DE_PIE;
        // fuera, junto al camino: un aprendiz leyendo
        esperas.add(new Espera(A, x(-6), pie(x(-6), z(55)), z(55), ESTE, R, 0, "camino"));
        // la puerta: el custodio en el paso
        en(K, 0, PISO, 43, SUR, D, 0, "puerta");
        // el patio de entrada: dos aprendices de ronda
        en(A, -9, PISO, 35, ESTE, D, 4, "patio");
        en(A, 10, PISO, 33, OESTE, D, 4, "patio");
        // el claustro: dos maestros de ronda por las galerias y tres aprendices leyendo
        en(M, -20, PISO, 8, NORTE, D, 4, "claustro");
        en(M, 20, PISO, 18, SUR, D, 4, "claustro");
        en(A, -8, PISO, 0, SUR, R, 0, "claustro");
        en(A, 9, PISO, 27, NORTE, R, 0, "claustro");
        en(A, 21, PISO, 5, OESTE, R, 0, "claustro");
        // el jardin: un maestro ante la fuente y dos aprendices tumbados en la hierba
        en(M, 0, PISO, FUENTE_V + 6, NORTE, D, 0, "jardin");
        en(A, -10, PISO, 8, giro(1, 1), T, 0, "jardin");
        en(A, 11, PISO, 20, giro(-1, -1), T, 0, "jardin");
        // la biblioteca: custodios de ronda entre los estantes, aprendices leyendo y tumbados entre los libros
        en(K, -8, PISO, -27, ESTE, D, 3, "biblioteca");
        en(K, 8, PISO, -35, OESTE, D, 3, "biblioteca");
        en(A, -7, PISO, -11, SUR, R, 0, "biblioteca");
        en(A, 6, PISO, -27, NORTE, R, 0, "biblioteca");
        en(A, -6, PISO, -38, NORTE, R, 0, "biblioteca");
        en(A, 9, PISO, -19, giro(-1, 1), T, 0, "biblioteca");
        en(A, -10, PISO, -35, giro(1, -1), T, 0, "biblioteca");
        // el altillo: dos maestros de ronda
        en(M, -13, ALTILLO + 1, -20, NORTE, D, 2, "altillo");
        en(M, 13, ALTILLO + 1, -36, SUR, D, 2, "altillo");
        // el estrado del grimorio: un custodio de guardia
        en(K, 0, PISO + 1, -46, SUR, D, 0, "grimorio");
        // el scriptorium: los copistas en sus pupitres y a la mesa, y un maestro de ronda
        en(A, -32, PISO, 5, OESTE, R, 0, "scriptorium");
        en(A, -32, PISO, 17, OESTE, R, 0, "scriptorium");
        en(A, -27, PISO, 10, OESTE, R, 0, "scriptorium");
        en(M, -26, PISO, 20, NORTE, D, 3, "scriptorium");
        // el aula: los aprendices en los bancos mirando a la catedra y el maestro en ella
        en(A, 26, PISO, 11, NORTE, R, 0, "aula");
        en(A, 31, PISO, 15, NORTE, R, 0, "aula");
        en(A, 25, PISO, 19, NORTE, R, 0, "aula");
        en(A, 32, PISO, 23, NORTE, R, 0, "aula");
        en(M, 28, PISO + 1, -1, SUR, D, 0, "aula");
        // la veta: un custodio y un aprendiz tumbado
        en(K, -24, PISO, -15, SUR, D, 3, "veta");
        en(A, -20, PISO, -44, giro(-1, 1), T, 0, "veta");
        // las ruinas del aula vieja
        en(M, 26, PISO, -26, SUR, D, 4, "ruinas");
        en(A, 30, PISO, -16, giro(-1, -1), T, 0, "ruinas");
        // arriba, en el archivo de la torre
        en(M, 25, ARCHIVO, -51, SUR, D, 0, "archivo");
    }

    /** El hacia donde mira de Minecraft (0 = sur, 90 = oeste) para una direccion (du, dv). */
    static float giro(double du, double dv) {
        return (float) (Math.toDegrees(Math.atan2(dv, du)) - 90.0);
    }

    private void en(int variante, int u, int y, int v, float giro, int postura, int ronda, String grupo) {
        esperas.add(new Espera(variante, x(u), y, z(v), giro, postura, ronda, grupo));
    }

    // ------------------------------------------------------------------ para la prueba

    /** Lo que hay en la academia ya generada (para la prueba): el atril con el libro, el arcon, la fuente, la aguja y los estantes. */
    public static String resumen(ServerLevel m) {
        Academia a = get();
        int[] at = a.sitio("atril"), ar = a.sitio("arcon");
        boolean libro = m.getBlockEntity(new BlockPos(at[0], at[1], at[2])) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le
                && Perdidos.de(le.getBook()) == 24;
        boolean arcon = m.getBlockState(new BlockPos(ar[0], ar[1], ar[2])).is(Blocks.CHEST);
        boolean fuente = m.getBlockState(new BlockPos(CX, PISO + 3, CZ + FUENTE_V)).is(Blocks.CRYING_OBSIDIAN);
        int uc = (TORRE_U0 + TORRE_U1) / 2, vc = (TORRE_V0 + TORRE_V1) / 2;
        boolean aguja = m.getBlockState(new BlockPos(CX + uc, TORRE_ALTO + 9, CZ + vc)).is(Blocks.END_ROD);
        int estantes = 0, muestras = 0;
        for (int v : FILAS)
            for (int u = 5; u <= 10; u++)
                for (int s : LADOS) {
                    muestras++;
                    if (m.getBlockState(new BlockPos(CX + s * u, PISO + 2, CZ + v)).is(Blocks.BOOKSHELF)) estantes++;
                }
        return "atril con el libro 24 " + (libro ? "SI" : "NO") + ", arcon " + (arcon ? "SI" : "NO") + ", fuente de amatista " + (fuente ? "SI" : "NO")
                + ", aguja de la torre " + (aguja ? "SI" : "NO") + ", estantes " + estantes + "/" + muestras + " (" + a.estantes + " en el plano)";
    }
}
