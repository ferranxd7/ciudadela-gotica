package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Podrido;
import com.darkcraft.fortaleza.Perdidos;
import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Pal;
import com.darkcraft.pueblo.gen.Trozo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * La Aldea de los Pilotes (parte 7i), en el Pantano Podrido, al noroeste del santuario de los Juncos: una aldea de
 * pescadores y barqueros levantada sobre pilotes en una <b>laguna</b> (la aldea la cava en el pantano: dos o tres de agua,
 * islotes de barro con juncos y mangles muertos). En medio, la <b>plaza</b> (un tablado con la hoguera de almas, la campana y
 * las redes); alrededor, <b>ocho chozas</b> sobre sus pilotes (un escalon por encima de las pasarelas; alguna medio hundida
 * o sin tejado) unidas por <b>pasarelas</b> de tablas a ras de agua con postes de amarre y faroles; al norte la <b>capilla
 * hundida</b> (el suelo bajo un palmo de agua, los bancos sumergidos, medio tejado caido, el campanario partido y la campana
 * en el fondo; en el presbiterio, seco, el altar, el atril con el <b>libro perdido 20</b> y un arcon); al sur el
 * <b>embarcadero</b> largo que acaba en un muelle y la pasarela que va hasta el santuario. Barcas varadas o hundidas.
 * <p>
 * Los podridos ({@code enemigos/Podrido}) los apunta el propio plano ({@link #esperas}): flotando boca abajo junto a las
 * pasarelas, en la plaza, dentro de las chozas, rezando en el presbiterio, en el embarcadero. Se dibuja como la arboleda:
 * chunk a chunk al adornar ({@link MorvainGenerador#applyBiomeDecoration}); en su caja {@link Adorno} no pone nada.
 */
public final class Pilotes {
    public static final long SEMILLA = 0x9170_7E5A_11L;
    /** El centro de la plaza. */
    public static final int CX = -5650, CZ = -770;
    /** La laguna (con el lobulo de la capilla) y lo que tarda en fundirse con el pantano. */
    static final int R_LAGUNA = 34, R_FUNDE = 12;
    /** Todo lo que se dibuja: ahi {@link Adorno} no pone nada. */
    static final int X0 = CX - 50, X1 = CX + 48, Z0 = CZ - 62, Z1 = CZ + 46;
    /** Las tablas de las pasarelas (se pisa en PASO + 1, a un bloque del agua) y las de las chozas (un escalon mas). */
    public static final int PASO = Terreno.AGUA + 1, CHOZA = PASO + 1;
    /** La capilla: el suelo de la nave (bajo un palmo de agua) y el del presbiterio (seco). */
    public static final int CAP_SUELO = Terreno.AGUA - 1, CAP_ALTAR = Terreno.AGUA;
    /** La capilla: de u -6 a 6 y de v -40 a -24 (respecto a la plaza); la puerta, al sur. */
    static final int CAP_V0 = -40, CAP_V1 = -24, CAP_U = 6;

    private static final Ruido RUIDO = new Ruido(SEMILLA);
    /** Los islotes de barro: {u, v, radio}. */
    static final int[][] ISLOTES = {{-28, 8, 4}, {26, -16, 3}, {-26, -26, 4}, {11, -35, 3}, {24, 31, 3}, {-22, 27, 4}, {-30, -6, 3}};

    // ------------------------------------------------------------------ el terreno

    private static final int AW = X1 - X0 + 1, AD = Z1 - Z0 + 1;
    private static final int[] ALTO = new int[AW * AD], SUELO = new int[AW * AD];

    static {
        Arrays.fill(ALTO, Integer.MIN_VALUE);
        Arrays.fill(SUELO, Integer.MIN_VALUE);
    }

    /** El bloque de arriba del terreno de Morvain en (x, z) (sin la laguna). */
    public static int alto(int x, int z) {
        if (x < X0 || x > X1 || z < Z0 || z > Z1) return (int) Math.floor(Terreno.columna(x, z).altura());
        int i = (z - Z0) * AW + (x - X0);
        int v = ALTO[i];
        if (v == Integer.MIN_VALUE) ALTO[i] = v = (int) Math.floor(Terreno.columna(x, z).altura());
        return v;
    }

    /** Lo que mide de la orilla de la laguna (negativo: dentro). */
    static double orilla(int x, int z) {
        double u = x - CX, v = z - CZ;
        return Math.min(Math.hypot(u, v) - R_LAGUNA, Math.hypot(u, v + 32) - 15) + RUIDO.fbm(x, z, 22, 2) * 4.5;
    }

    /** El bloque de arriba del suelo en (x, z) con la laguna: dentro, el fondo del agua; lejos, el pantano de siempre. */
    public static int suelo(int x, int z) {
        if (x < X0 || x > X1 || z < Z0 || z > Z1) return alto(x, z);
        int i = (z - Z0) * AW + (x - X0);
        int s = SUELO[i];
        if (s == Integer.MIN_VALUE) SUELO[i] = s = calcularSuelo(x, z);
        return s;
    }

    private static int calcularSuelo(int x, int z) {
        int t = alto(x, z);
        double e = orilla(x, z);
        if (e >= R_FUNDE) return t;
        double u = x - CX, v = z - CZ;
        double f = 60.3 + Math.min(1.0, Math.max(0.0, (Math.hypot(u, v) - 8) / 20)) * 1.3 + RUIDO.fbm(x + 700, z - 300, 9, 2) * 0.9;
        for (int[] k : ISLOTES) {
            double d = Math.hypot(u - k[0], v - k[1]) + RUIDO.fbm(x, z + 900, 5, 1) * 0.8;
            if (d < k[2]) f = Math.max(f, Terreno.AGUA + (d < k[2] * 0.45 ? 0.6 : 0.2));
            else if (d < k[2] + 3) f = Math.max(f, Ruido.lerp((d - k[2]) / 3, Terreno.AGUA - 0.2, f));
        }
        if (e <= 0) return (int) Math.floor(f);
        return (int) Math.floor(Ruido.lerp(Ruido.suave(0, R_FUNDE, e), f, t + 0.5));
    }

    /** Lo que se pisa en (x, z) (en el agua: el fondo). */
    public static int pie(int x, int z) {
        return suelo(x, z) + 1;
    }

    // ------------------------------------------------------------------ el plano

    /** Un podrido de la aldea: donde espera (y: lo que pisa), hacia donde mira, su variante, como espera, cuanto ronda y su grupo. */
    public record Espera(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    /** Una choza: su centro (u, v), lo que mide por dentro (impar), hacia donde da la puerta, lo rota que esta y su nombre. */
    record Choza(String nombre, int u, int v, int w, int d, Direction puerta, int ruina, boolean arcon) {}

    static final Choza[] CHOZAS = {
            new Choza("redes", -19, 0, 7, 5, Direction.EAST, 0, false),
            new Choza("ahumadero", -17, 16, 5, 5, Direction.NORTH, 1, false),
            new Choza("barquero", 19, -3, 7, 5, Direction.WEST, 0, true),
            new Choza("curandera", 18, 13, 5, 5, Direction.NORTH, 2, false),
            new Choza("pescador", 15, -19, 5, 5, Direction.SOUTH, 1, false),
            new Choza("viuda", -15, -18, 5, 5, Direction.SOUTH, 2, false),
            new Choza("cestero", -8, 20, 5, 5, Direction.EAST, 1, false),
            new Choza("calafate", 9, 18, 5, 5, Direction.WEST, 0, false),
    };

    /** Las pasarelas: rectangulos de tablas {u0, v0, u1, v1} (dos de ancho) a la altura {@link #PASO}. */
    static final int[][] PASARELAS = {
            {-14, 0, -7, 1},                 // de la plaza a la casa de las redes (oeste)
            {-17, 4, -16, 12},               // de la casa de las redes al ahumadero
            {7, -3, 14, -2},                 // de la plaza a la casa del barquero (este)
            {18, 1, 19, 9},                  // de la casa del barquero a la de la curandera
            {4, -10, 5, -7}, {4, -11, 15, -10}, {14, -15, 15, -12},       // al pescador (noreste)
            {-5, -10, -4, -7}, {-15, -11, -4, -10}, {-16, -14, -15, -12}, // a la viuda (noroeste)
            {0, -23, 1, -7},                 // a la capilla (norte)
            {0, 7, 1, 25},                   // el embarcadero (sur)
            {-4, 19, -1, 20},                // al cestero
            {2, 17, 5, 18},                  // al calafate
            {2, 24, 35, 25},                 // el embarcadero, hacia el este
            {40, 29, 41, 38}, {42, 37, 44, 38}, // la pasarela del muelle al santuario
    };
    /** Los escalones de las pasarelas a las chozas: {u, v, direccion (0 N, 1 E, 2 S, 3 O)} (se sube hacia esa direccion). */
    static final int[][] ESCALONES = {
            {-14, 0, 3}, {-14, 1, 3},
            {-17, 4, 0}, {-16, 4, 0}, {-17, 12, 2}, {-16, 12, 2},
            {14, -3, 1}, {14, -2, 1},
            {18, 1, 0}, {19, 1, 0}, {18, 9, 2}, {19, 9, 2},
            {14, -15, 0}, {15, -15, 0},
            {-16, -14, 0}, {-15, -14, 0},
            {-4, 19, 3}, {-4, 20, 3},
            {5, 17, 1}, {5, 18, 1},
    };
    /** El muelle del embarcadero: {u0, v0, u1, v1}. */
    static final int[] MUELLE = {36, 21, 42, 28};

    private static Pilotes unico;

    public static synchronized Pilotes get() {
        if (unico == null) unico = new Pilotes();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    /** Las columnas donde no van juncos, nenufares ni hierba (bajo lo construido). */
    private final Set<Long> ocupadas = new HashSet<>();
    /** Lo que hay (para la prueba y las vistas). */
    public int tablas, pilotes, faroles;

    private Pilotes() {
        elementos.add(new Laguna());
        Lista m = new Lista();
        plaza(m);
        for (Choza k : CHOZAS) choza(m, k);
        for (int[] p : PASARELAS) pasarela(m, p[0], p[1], p[2], p[3]);
        muelle(m);
        for (int[] e : ESCALONES) escalon(m, e[0], e[1], e[2]);
        // el final de la pasarela del santuario: un escalon para bajar al barro (si hace falta)
        if (suelo(CX + 45, CZ + 37) < PASO) {
            m.put(CX + 45, PASO, CZ + 37, escalera(Blocks.SPRUCE_STAIRS, Direction.WEST, false, false));
            m.put(CX + 45, PASO, CZ + 38, escalera(Blocks.SPRUCE_STAIRS, Direction.WEST, false, false));
            ocupar(CX + 45, CZ + 37, 0);
            ocupar(CX + 45, CZ + 38, 0);
        }
        elementos.add(m.pieza(3));
        Lista cap = new Lista();
        capilla(cap);
        elementos.add(cap.pieza(3));
        Lista d = new Lista();
        barca(d, 8, 0, false, false);
        barca(d, 44, 21, false, false);
        barca(d, -11, -32, false, true);
        barca(d, -24, 14, true, true);
        for (int[] k : new int[][]{{-28, 8, 6}, {26, -16, 5}, {-26, -26, 7}, {11, -35, 5}, {24, 31, 6}, {-22, 27, 5}}) arbolMuerto(d, k[0], k[1], k[2]);
        elementos.add(d.pieza(4));
        trozos();
        // ---- los sitios con nombre
        sitio("plaza", 0, PASO + 1, 3);
        sitio("fuego", 0, PASO + 1, 0);
        sitio("pasarela", 0, PASO + 1, 15);
        sitio("muelle", 39, PASO + 1, 24);
        sitio("entrada", 46, pie(CX + 46, CZ + 38), 38);
        sitio("capilla", 0, CAP_SUELO + 1, -28);
        sitio("puerta_capilla", 0, PASO + 1, -22);
        sitio("presbiterio", 0, CAP_ALTAR + 1, -35);
        sitio("atril", 0, CAP_ALTAR + 1, -37);
        sitio("arcon", 4, CAP_ALTAR + 1, -37);
        sitio("campana", -9, pie(CX - 9, CZ - 21), -21);
        for (Choza k : CHOZAS) sitio("choza_" + k.nombre(), k.u(), CHOZA + 1, k.v());
        // ---- los podridos
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo de la aldea (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en lo de la aldea: ahi {@link Adorno} no pone nada. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= X0 && x0 <= X1 && z0 + 15 >= Z0 && z0 <= Z1;
    }

    /** Si (x, z) esta en la aldea (para el nombre del sitio y su logro). */
    public static boolean dentro(double x, double z) {
        return Math.hypot(x - CX, z - CZ) < R_LAGUNA + 6 || Math.hypot(x - CX, z - (CZ - 32)) < 18
                || x >= CX + 30 && x <= CX + 46 && z >= CZ + 18 && z <= CZ + 40;
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} de la aldea de los pilotes en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los podridos de la aldea (los apunta {@link MorvainEnemigos}). */
    public List<Espera> esperas() {
        return esperas;
    }

    /** Los sitios con nombre (la plaza, la capilla, el atril, el muelle, cada choza...): {x, y, z} (y: lo que se pisa). */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    private void sitio(String nombre, int u, int y, int v) {
        sitios.put(nombre, new int[]{CX + u, y, CZ + v});
    }

    /** Aparta las columnas de alrededor de (x, z): ahi no van juncos ni nenufares. */
    private void ocupar(int x, int z, int r) {
        for (int dx = -r; dx <= r; dx++)
            for (int dz = -r; dz <= r; dz++) ocupadas.add(BlockPos.asLong(x + dx, 0, z + dz));
    }

    boolean ocupada(int x, int z) {
        return ocupadas.contains(BlockPos.asLong(x, 0, z));
    }

    /** El hacia donde mira de Minecraft (0 = sur, 90 = oeste) para una direccion (dx, dz). */
    static float giro(double dx, double dz) {
        return (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
    }

    // ------------------------------------------------------------------ bloques

    static BlockState b(Block k) {
        return k.defaultBlockState();
    }

    static BlockState escalera(Block k, Direction f, boolean alReves, boolean agua) {
        BlockState s = k.defaultBlockState().setValue(StairBlock.FACING, f).setValue(StairBlock.HALF, alReves ? Half.TOP : Half.BOTTOM);
        return s.hasProperty(BlockStateProperties.WATERLOGGED) ? s.setValue(BlockStateProperties.WATERLOGGED, agua) : s;
    }

    static BlockState losa(Block k, SlabType t, boolean agua) {
        return k.defaultBlockState().setValue(SlabBlock.TYPE, t).setValue(SlabBlock.WATERLOGGED, agua && t != SlabType.DOUBLE);
    }

    static BlockState tronco(Block k, Direction.Axis a) {
        BlockState s = k.defaultBlockState();
        return s.hasProperty(RotatedPillarBlock.AXIS) ? s.setValue(RotatedPillarBlock.AXIS, a) : s;
    }

    private static final Direction[] DIR = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    /** Las tablas de las pasarelas: abeto casi siempre, alguna de mangle o de roble oscuro, alguna podrida (media). */
    private BlockState tabla(int x, int z) {
        int p = Hash.pct(x, PASO, z, SEMILLA + 11);
        if (p < 5) return losa(Blocks.SPRUCE_SLAB, SlabType.TOP, false);
        if (p < 17) return Pal.MG_PLANKS;
        if (p < 24) return Pal.DO_PLANKS;
        return Pal.SP_PLANKS;
    }

    /** Un pilote: un tronco desde el fondo (o el barro) hasta justo debajo de {@code hasta}. */
    private void pilote(Lista L, int x, int z, int hasta) {
        int f = suelo(x, z);
        for (int y = f + 1; y < hasta; y++) L.put(x, y, z, Hash.pct(x, 1, z, SEMILLA + 13) < 50 ? Pal.SP_LOG : b(Blocks.MANGROVE_LOG));
        pilotes++;
    }

    /** Un poste de amarre: un tronco desde el fondo hasta un bloque por encima de las tablas, con su farol (o no). */
    private void poste(Lista L, int x, int z, boolean farol) {
        int f = suelo(x, z);
        for (int y = f + 1; y <= PASO + 1; y++) L.put(x, y, z, Pal.SP_LOG);
        if (farol) {
            L.put(x, PASO + 2, z, Pal.SOUL_LANTERN);
            faroles++;
        } else if (Hash.pct(x, 2, z, SEMILLA + 14) < 40) L.put(x, PASO + 2, z, losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM, false));
        ocupar(x, z, 0);
    }

    // ------------------------------------------------------------------ la plaza, las pasarelas y el muelle

    /** La plaza: un tablado de 13 x 13 con la hoguera de almas en medio, la campana, las redes, barriles y faroles. */
    private void plaza(Lista L) {
        for (int u = -6; u <= 6; u++)
            for (int v = -6; v <= 6; v++) {
                int x = CX + u, z = CZ + v;
                L.put(x, PASO, z, tabla(x, z));
                tablas++;
                if (u % 3 == 0 && v % 3 == 0) pilote(L, x, z, PASO);
                ocupar(x, z, 1);
            }
        // la hoguera: un cerco de losas de barro y el fuego de almas
        for (int u = -1; u <= 1; u++)
            for (int v = -1; v <= 1; v++)
                if (u != 0 || v != 0) L.put(CX + u, PASO + 1, CZ + v, losa(Blocks.MUD_BRICK_SLAB, SlabType.BOTTOM, false));
        L.put(CX, PASO, CZ, b(Blocks.MUD_BRICKS));
        // las esquinas: postes con faroles (fuera del tablado)
        poste(L, CX - 7, CZ - 7, true);
        poste(L, CX + 7, CZ - 7, false);
        poste(L, CX - 7, CZ + 7, false);
        poste(L, CX + 7, CZ + 7, true);
        // las redes tendidas: dos postes de valla con la red (telaranas) entre ellos
        for (int u = -5; u <= -2; u++) {
            boolean poste = u == -5 || u == -2;
            L.une(CX + u, PASO + 1, CZ + 5, poste ? Pal.SP_FENCE : Pal.COBWEB);
            L.une(CX + u, PASO + 2, CZ + 5, poste ? Pal.SP_FENCE : (u == -4 ? Pal.COBWEB : Pal.AIR));
        }
        // la cuerda de secar: postes y pescado podrido (no: una cadena)
        L.une(CX + 3, PASO + 1, CZ + 5, Pal.SP_FENCE);
        L.une(CX + 5, PASO + 1, CZ + 5, Pal.SP_FENCE);
        L.put(CX + 4, PASO + 1, CZ + 5, b(Blocks.HAY_BLOCK));
        // la campana de la aldea: un poste con su travesano
        L.put(CX + 5, PASO + 1, CZ - 5, Pal.DO_LOG);
        L.put(CX + 5, PASO + 2, CZ - 5, Pal.DO_LOG);
        L.put(CX + 5, PASO + 3, CZ - 5, Pal.DO_LOG);
        L.put(CX + 4, PASO + 3, CZ - 5, tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        L.put(CX + 4, PASO + 2, CZ - 5, b(Blocks.BELL).setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING).setValue(BellBlock.FACING, Direction.NORTH));
        // cajas y barriles por los bordes
        L.put(CX - 6, PASO + 1, CZ - 6, b(Blocks.BARREL));
        L.put(CX - 5, PASO + 1, CZ - 6, b(Blocks.BARREL));
        L.put(CX - 6, PASO + 2, CZ - 6, losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM, false));
        L.put(CX + 6, PASO + 1, CZ + 2, b(Blocks.BARREL));
        L.put(CX + 6, PASO + 1, CZ - 1, b(Blocks.CAULDRON));
    }

    /** Una pasarela: el rectangulo de tablas, los pilotes cada tres y postes de amarre a los lados cada siete. */
    private void pasarela(Lista L, int u0, int v0, int u1, int v1) {
        boolean enX = u1 - u0 > v1 - v0;
        int k = 0;
        for (int u = u0; u <= u1; u++)
            for (int v = v0; v <= v1; v++) {
                int x = CX + u, z = CZ + v;
                if (!L.hay(x, PASO, z)) {
                    L.put(x, PASO, z, tabla(x, z));
                    tablas++;
                    if ((u + v) % 3 == 0) pilote(L, x, z, PASO);
                }
                ocupar(x, z, 1);
            }
        // postes de amarre a un lado y a otro (fuera de las tablas), con farol uno de cada dos
        int largo = enX ? u1 - u0 : v1 - v0;
        for (int i = 3; i < largo - 1; i += 7) {
            boolean izq = (i / 7) % 2 == 0;
            int x = enX ? CX + u0 + i : (izq ? CX + u0 - 1 : CX + u1 + 1);
            int z = enX ? (izq ? CZ + v0 - 1 : CZ + v1 + 1) : CZ + v0 + i;
            if (L.hay(x, PASO, z) || L.hay(x, PASO + 1, z)) continue;
            poste(L, x, z, k++ % 2 == 0);
        }
    }

    /** El muelle del embarcadero: un tablado mas ancho con sus postes, barriles y una caja. */
    private void muelle(Lista L) {
        for (int u = MUELLE[0]; u <= MUELLE[2]; u++)
            for (int v = MUELLE[1]; v <= MUELLE[3]; v++) {
                int x = CX + u, z = CZ + v;
                if (!L.hay(x, PASO, z)) {
                    L.put(x, PASO, z, tabla(x, z));
                    tablas++;
                }
                if (u % 3 == 0 && v % 3 == 0) pilote(L, x, z, PASO);
                ocupar(x, z, 1);
            }
        poste(L, CX + MUELLE[0] - 1, CZ + MUELLE[1] - 1, true);
        poste(L, CX + MUELLE[2] + 1, CZ + MUELLE[1] - 1, false);
        poste(L, CX + MUELLE[2] + 1, CZ + MUELLE[3] + 1, true);
        L.put(CX + 42, PASO + 1, CZ + 21, b(Blocks.BARREL));
        L.put(CX + 41, PASO + 1, CZ + 21, b(Blocks.BARREL));
        L.put(CX + 42, PASO + 2, CZ + 21, b(Blocks.BARREL));
        L.put(CX + 36, PASO + 1, CZ + 28, Pal.COBWEB);
    }

    /** Un escalon de una pasarela a una choza (dos tablas mas arriba): se sube hacia {@code dir}. */
    private void escalon(Lista L, int u, int v, int dir) {
        L.put(CX + u, PASO + 1, CZ + v, escalera(Blocks.SPRUCE_STAIRS, DIR[dir], false, false));
    }

    // ------------------------------------------------------------------ las chozas

    /** Una choza sobre sus pilotes: el tablado, las paredes de mangle con la puerta y ventanas, el tejado a cuatro aguas. */
    private void choza(Lista L, Choza k) {
        int a = k.w() / 2, bb = k.d() / 2;
        int cx = CX + k.u(), cz = CZ + k.v();
        long sem = SEMILLA + k.nombre().hashCode();
        // ---- el tablado y los pilotes
        for (int dx = -a - 1; dx <= a + 1; dx++)
            for (int dz = -bb - 1; dz <= bb + 1; dz++) {
                int x = cx + dx, z = cz + dz;
                // la que esta medio hundida ha perdido una esquina del tablado
                if (k.ruina() >= 2 && dx >= a && dz >= bb) continue;
                L.put(x, CHOZA, z, Hash.pct(x, CHOZA, z, sem) < 15 ? Pal.MG_PLANKS : Pal.SP_PLANKS);
                boolean borde = Math.abs(dx) == a + 1 || Math.abs(dz) == bb + 1;
                if (borde && (dx + dz) % 2 == 0 || dx == 0 && dz == 0) pilote(L, x, z, CHOZA);
                ocupar(x, z, 1);
            }
        // ---- las paredes (de CHOZA + 1 a CHOZA + 3): troncos en las esquinas, tablas de mangle o de abeto, puerta y ventanas
        int puertaX = cx + k.puerta().getStepX() * a, puertaZ = cz + k.puerta().getStepZ() * bb;
        for (int dx = -a; dx <= a; dx++)
            for (int dz = -bb; dz <= bb; dz++) {
                boolean bx = Math.abs(dx) == a, bz = Math.abs(dz) == bb;
                if (!bx && !bz) continue;
                int x = cx + dx, z = cz + dz;
                boolean esquina = bx && bz;
                for (int h = 1; h <= 3; h++) {
                    int y = CHOZA + h;
                    if (x == puertaX && z == puertaZ && h <= 2) continue;
                    // las ventanas: en el medio de cada pared (no en la de la puerta), a media altura
                    boolean medio = bx ? dz == 0 : dx == 0;
                    boolean paredPuerta = k.puerta().getStepX() != 0 ? bx && dx == k.puerta().getStepX() * a : bz && dz == k.puerta().getStepZ() * bb;
                    if (!esquina && medio && h == 2 && !paredPuerta) continue;
                    // lo roto: huecos arriba (ruina 1) o media pared caida (ruina 2)
                    int p = Hash.pct(x, y, z, sem + 1);
                    if (k.ruina() >= 1 && h == 3 && p < 30) continue;
                    if (k.ruina() >= 2 && dx > 0 && dz > 0 && h >= 2) continue;
                    BlockState s = esquina ? b(Blocks.STRIPPED_MANGROVE_LOG) : p < 55 ? Pal.MG_PLANKS : p < 80 ? Pal.SP_PLANKS : b(Blocks.MUD_BRICKS);
                    L.put(x, y, z, s);
                }
            }
        // ---- el tejado a cuatro aguas (escalones de roble oscuro mirando hacia dentro; arriba, losas)
        for (int n = 0; ; n++) {
            int ax = a + 1 - n, az = bb + 1 - n, y = CHOZA + 4 + n;
            if (ax < 0 || az < 0) break;
            boolean ultimo = ax <= 1 || az <= 1;
            for (int dx = -ax; dx <= ax; dx++)
                for (int dz = -az; dz <= az; dz++) {
                    boolean bx = Math.abs(dx) == ax, bz = Math.abs(dz) == az;
                    if (!ultimo && !bx && !bz) continue;
                    int x = cx + dx, z = cz + dz;
                    int p = Hash.pct(x, y, z, sem + 2);
                    if (k.ruina() >= 1 && p < 12 + 10 * k.ruina()) continue;
                    if (k.ruina() >= 2 && dx > 0 && dz > 0) continue;
                    BlockState s;
                    if (ultimo) s = losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM, false);
                    else if (bz) s = escalera(Blocks.DARK_OAK_STAIRS, dz < 0 ? Direction.SOUTH : Direction.NORTH, false, false);
                    else s = escalera(Blocks.DARK_OAK_STAIRS, dx < 0 ? Direction.EAST : Direction.WEST, false, false);
                    L.put(x, y, z, s);
                }
            if (ultimo) break;
        }
        // ---- dentro: telaranas en un rincon, un farol colgado, barriles (y el arcon del barquero)
        int fx = -k.puerta().getStepX(), fz = -k.puerta().getStepZ();
        L.put(cx - a + 1, CHOZA + 3, cz - bb + 1, Pal.COBWEB);
        // el farol, colgado de una cadena desde lo alto del tejado (a un lado: en medio espera quien vive alli)
        L.put(cx + 1, CHOZA + 3, cz - 1, Pal.SOUL_LANTERN_HANG);
        L.put(cx + 1, CHOZA + 4, cz - 1, Pal.CHAIN);
        L.put(cx + 1, CHOZA + 5, cz - 1, Pal.CHAIN);
        faroles++;
        int rx = cx + (fx != 0 ? fx * (a - 1) : a - 1), rz = cz + (fz != 0 ? fz * (bb - 1) : bb - 1);
        L.put(rx, CHOZA + 1, rz, b(Blocks.BARREL));
        if (k.nombre().equals("redes")) {
            L.put(cx - a + 1, CHOZA + 1, cz + bb - 1, Pal.COBWEB);
            L.put(cx - a + 1, CHOZA + 2, cz + bb - 1, Pal.COBWEB);
            L.une(cx - 1, CHOZA + 1, cz - bb + 1, Pal.SP_FENCE);
        }
        if (k.nombre().equals("ahumadero")) L.put(cx + 1, CHOZA + 1, cz + 1, b(Blocks.CAMPFIRE).setValue(BlockStateProperties.LIT, false));
        if (k.nombre().equals("curandera")) {
            L.put(cx - 1, CHOZA + 1, cz - 1, b(Blocks.CAULDRON));
            L.put(cx + 1, CHOZA + 1, cz - 1, b(Blocks.BREWING_STAND));
        }
    }

    // ------------------------------------------------------------------ la capilla hundida

    /**
     * La capilla: el zocalo de piedra en la laguna, la nave inundada (un palmo de agua sobre el suelo, los bancos
     * sumergidos), el presbiterio seco al norte (el altar con velas, el atril con el libro 20 y el arcon), los muros con
     * ventanas y huecos, medio tejado (el del norte) y lo demas caido; fuera, el campanario partido.
     */
    private void capilla(Lista L) {
        long sem = SEMILLA + 31;
        for (int u = -CAP_U - 1; u <= CAP_U + 1; u++)
            for (int v = CAP_V0 - 1; v <= CAP_V1 + 1; v++) {
                int x = CX + u, z = CZ + v;
                ocupar(x, z, 1);
                boolean muro = Math.abs(u) == CAP_U || v == CAP_V0 || v == CAP_V1;
                boolean fuera = Math.abs(u) > CAP_U || v < CAP_V0 || v > CAP_V1;
                // el zocalo, desde el fondo
                int f = suelo(x, z);
                for (int y = f + 1; y < CAP_SUELO; y++) L.put(x, y, z, piedra(x, y, z, sem));
                if (fuera) {
                    // el borde del zocalo asoma un poco del agua
                    L.put(x, CAP_SUELO, z, piedra(x, CAP_SUELO, z, sem));
                    if (Hash.pct(x, 0, z, sem + 3) < 45) L.put(x, CAP_SUELO + 1, z, losa(Blocks.MOSSY_STONE_BRICK_SLAB, SlabType.BOTTOM, true));
                    continue;
                }
                L.put(x, CAP_SUELO, z, Hash.pct(x, 0, z, sem + 4) < 35 ? b(Blocks.MOSSY_COBBLESTONE) : Hash.pct(x, 1, z, sem + 4) < 50
                        ? Pal.MOSSY_STONE_BRICKS : b(Blocks.CRACKED_STONE_BRICKS));
                if (muro) {
                    for (int y = CAP_SUELO + 1; y <= CAP_SUELO + 9; y++) {
                        int h = y - CAP_SUELO;
                        // la puerta (al sur): el umbral a la altura de las pasarelas y el hueco encima
                        if (v == CAP_V1 && Math.abs(u) <= 1) {
                            if (y == PASO) L.put(x, y, z, Pal.STONE_BRICKS);
                            else if (y > PASO && (y <= PASO + 3 || u == 0 && y == PASO + 4)) L.put(x, y, z, Pal.AIR);
                            else if (y < PASO) L.put(x, y, z, piedra(x, y, z, sem));
                            else L.put(x, y, z, piedra(x, y, z, sem));
                            continue;
                        }
                        // las ventanas de los muros largos
                        boolean ventana = Math.abs(u) == CAP_U && (v == -37 || v == -32 || v == -27) && h >= 4 && h <= 6;
                        if (ventana) continue;
                        // lo que se ha caido: arriba, mas cuanto mas al sur
                        int p = Hash.pct(x, y, z, sem + 5);
                        if (h >= 7 && p < 18 + (v - CAP_V0) * 3) continue;
                        L.put(x, y, z, piedra(x, y, z, sem));
                    }
                    continue;
                }
                // dentro: el presbiterio (seco) al norte; la nave con un palmo de agua
                boolean presb = v <= -35;
                if (presb) L.put(x, CAP_ALTAR, z, Hash.pct(x, 2, z, sem + 6) < 40 ? b(Blocks.POLISHED_ANDESITE) : Pal.STONE_BRICKS);
                else L.put(x, CAP_SUELO + 1, z, Pal.WATER);
            }
        // ---- la escalera de la puerta (bajando a la nave) y el escalon del presbiterio
        for (int u = -1; u <= 1; u++) {
            L.put(CX + u, CAP_SUELO + 1, CZ + CAP_V1 - 1, escalera(Blocks.STONE_BRICK_STAIRS, Direction.SOUTH, false, true));
            L.put(CX + u, CAP_ALTAR, CZ - 34, escalera(Blocks.STONE_BRICK_STAIRS, Direction.NORTH, false, true));
        }
        for (int u = -5; u <= 5; u++)
            if (Math.abs(u) > 1) L.put(CX + u, CAP_ALTAR, CZ - 34, escalera(Blocks.STONE_BRICK_STAIRS, Direction.NORTH, false, true));
        // ---- los bancos, sumergidos (miran al altar)
        for (int v : new int[]{-27, -29, -31})
            for (int u = 2; u <= 4; u++) {
                if (Hash.pct(u, v, 0, sem + 7) < 22) continue;
                L.put(CX + u, CAP_SUELO + 1, CZ + v, escalera(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, false, true));
                L.put(CX - u, CAP_SUELO + 1, CZ + v, escalera(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, false, true));
            }
        // ---- el altar con sus velas (apagadas)
        for (int u = -2; u <= 2; u++) L.put(CX + u, CAP_ALTAR + 1, CZ - 39, u == 0 ? b(Blocks.CHISELED_STONE_BRICKS) : Pal.STONE_BRICKS);
        for (int u = -2; u <= 2; u += 2)
            L.put(CX + u, CAP_ALTAR + 2, CZ - 39, b(Blocks.CANDLE).setValue(CandleBlock.CANDLES, 1 + Math.abs(u) / 2 + 1).setValue(CandleBlock.LIT, false));
        // ---- el tejado del norte (a dos aguas, a lo largo); el del sur, caido: vigas sueltas
        int arriba = CAP_SUELO + 9;
        for (int v = CAP_V0 - 1; v <= -32; v++)
            for (int n = 0; n <= CAP_U + 1; n++) {
                int y = arriba + n;
                for (int s = -1; s <= 1; s += 2) {
                    int u = s * (CAP_U + 1 - n);
                    int x = CX + u, z = CZ + v;
                    if (Hash.pct(x, y, z, sem + 8) < 10 + (v + 41) * 4) continue;
                    if (u == 0) L.put(x, y, z, losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM, false));
                    else L.put(x, y, z, escalera(Blocks.DARK_OAK_STAIRS, s < 0 ? Direction.EAST : Direction.WEST, false, false));
                    if (u == 0) break;
                }
            }
        // el hastial del norte, bajo el tejado
        for (int n = 1; n <= CAP_U; n++)
            for (int u = -(CAP_U - n); u <= CAP_U - n; u++) {
                int x = CX + u, y = arriba + n, z = CZ + CAP_V0;
                if (Hash.pct(x, y, z, sem + 11) < 15) continue;
                L.put(x, y, z, piedra(x, y, z, sem));
            }
        for (int v : new int[]{-29, -26})
            for (int u = -CAP_U; u <= CAP_U; u++)
                if (Hash.pct(u, v, 1, sem + 9) < 55) L.put(CX + u, arriba - 1, CZ + v, tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        // los cascotes del tejado y de los muros, en el agua de la nave
        for (int[] q : new int[][]{{-3, -30}, {-4, -26}, {3, -25}, {4, -28}, {-1, -33}, {2, -31}})
            L.put(CX + q[0], CAP_SUELO + 1, CZ + q[1], Hash.pct(q[0], q[1], 2, sem) < 50 ? b(Blocks.MOSSY_COBBLESTONE) : losa(Blocks.COBBLESTONE_SLAB, SlabType.BOTTOM, true));
        // telaranas y cadenas colgando de lo que queda del tejado
        L.put(CX - 4, arriba + 1, CZ - 38, Pal.COBWEB);
        L.put(CX + 5, arriba, CZ - 36, Pal.COBWEB);
        L.put(CX, arriba + 5, CZ - 36, Pal.CHAIN);
        L.put(CX, arriba + 4, CZ - 36, Pal.CHAIN);
        L.put(CX, arriba + 3, CZ - 36, Pal.SOUL_LANTERN_HANG);
        faroles++;
        // ---- el campanario partido (fuera, en la esquina del noreste)
        for (int u = CAP_U + 1; u <= CAP_U + 3; u++)
            for (int v = CAP_V0 - 1; v <= CAP_V0 + 1; v++) {
                int x = CX + u, z = CZ + v;
                int f = suelo(x, z);
                int alto = CAP_SUELO + 9 + Hash.pct(x, 0, z, sem + 10) % 4;
                for (int y = f + 1; y <= alto; y++) L.put(x, y, z, piedra(x, y, z, sem));
                ocupar(x, z, 1);
            }
    }

    private static BlockState piedra(int x, int y, int z, long sem) {
        int p = Hash.pct(x, y, z, sem);
        return p < 40 ? Pal.MOSSY_STONE_BRICKS : p < 62 ? b(Blocks.CRACKED_STONE_BRICKS) : p < 72 ? b(Blocks.MOSSY_COBBLESTONE) : Pal.STONE_BRICKS;
    }

    // ------------------------------------------------------------------ barcas y mangles

    /** Una barca de tablas de tres por seis (a lo largo de z, o de x), varada o medio hundida. */
    private void barca(Lista L, int u, int v, boolean enX, boolean hundida) {
        int y = hundida ? Terreno.AGUA - 1 : Terreno.AGUA;
        for (int i = 0; i < 6; i++)
            for (int j = -1; j <= 1; j++) {
                int x = CX + (enX ? u + i : u + j), z = CZ + (enX ? v + j : v + i);
                int yy = hundida && i >= 4 ? y + 1 : y;
                boolean agua = yy <= Terreno.AGUA;
                boolean punta = i == 0 || i == 5;
                if (punta && j != 0) continue;
                BlockState s;
                if (punta) {
                    Direction d = enX ? (i == 0 ? Direction.EAST : Direction.WEST) : (i == 0 ? Direction.SOUTH : Direction.NORTH);
                    s = escalera(Blocks.SPRUCE_STAIRS, d, false, agua);
                } else if (j == 0) s = losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM, agua);
                else {
                    Direction d = enX ? (j < 0 ? Direction.SOUTH : Direction.NORTH) : (j < 0 ? Direction.EAST : Direction.WEST);
                    s = escalera(Blocks.SPRUCE_STAIRS, d, false, agua);
                }
                L.put(x, yy, z, s);
                ocupar(x, z, 0);
            }
        // el banco en medio
        int bx = CX + (enX ? u + 2 : u), bz = CZ + (enX ? v : v + 2);
        if (!hundida) L.put(bx, y + 1, bz, losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM, false));
    }

    /** Un mangle muerto: el tronco, las raices metidas en el agua o el barro, dos o tres ramas y alguna telarana. */
    private void arbolMuerto(Lista L, int u, int v, int alto) {
        int x = CX + u, z = CZ + v;
        int f = suelo(x, z);
        long sem = SEMILLA + 41 + u * 31L + v;
        for (int y = f + 1; y <= f + alto; y++) L.put(x, y, z, b(Blocks.MANGROVE_LOG));
        // las raices
        for (Direction d : DIR) {
            int rx = x + d.getStepX(), rz = z + d.getStepZ();
            int rf = suelo(rx, rz);
            for (int y = rf + 1; y <= f + 2; y++)
                L.put(rx, y, rz, b(Blocks.MANGROVE_ROOTS).setValue(BlockStateProperties.WATERLOGGED, y <= Terreno.AGUA));
            ocupar(rx, rz, 0);
            int dx = x + d.getStepX() + d.getClockWise().getStepX(), dz = z + d.getStepZ() + d.getClockWise().getStepZ();
            if (Hash.pct(dx, 0, dz, sem) < 50)
                L.put(dx, suelo(dx, dz) + 1, dz, b(Blocks.MANGROVE_ROOTS).setValue(BlockStateProperties.WATERLOGGED, suelo(dx, dz) + 1 <= Terreno.AGUA));
        }
        // las ramas
        for (int i = 0; i < 3; i++) {
            Direction d = DIR[(int) ((sem >>> (i * 3)) & 3)];
            int h = f + alto - 2 + i;
            int largo = 2 + i % 2;
            for (int k = 1; k <= largo; k++)
                L.put(x + d.getStepX() * k, h + (k == largo ? 1 : 0), z + d.getStepZ() * k, tronco(Blocks.MANGROVE_LOG, d.getAxis()));
            if (i == 1) L.put(x + d.getStepX(), h - 1, z + d.getStepZ(), Pal.COBWEB);
        }
        ocupar(x, z, 1);
    }

    // ------------------------------------------------------------------ lo que lleva datos: el atril, el arcon, los barriles

    private void trozos() {
        int ya = CAP_ALTAR + 1;
        trozo(CX, ya, CZ - 37, c -> c.lectern(CX, ya, CZ - 37, Direction.SOUTH, Perdidos.libro(20)));
        trozo(CX + 4, ya, CZ - 37, c -> c.chest(CX + 4, ya, CZ - 37, Direction.SOUTH, Velmor.BOTIN_BUENO));
        // la campana de la capilla, caida en el fondo de la laguna
        int[] q = {CX - 9, CZ - 21};
        int fy = suelo(q[0], q[1]) + 1;
        trozo(q[0], fy, q[1], c -> c.set(q[0], fy, q[1], b(Blocks.BELL).setValue(BellBlock.ATTACHMENT, BellAttachType.FLOOR)
                .setValue(BellBlock.FACING, Direction.EAST)));
        ocupar(q[0], q[1], 0);
        // el fuego de almas de la plaza
        trozo(CX, PASO + 1, CZ, c -> c.set(CX, PASO + 1, CZ, Pal.SOUL_CAMPFIRE));
        // los barriles de las chozas con algo dentro (el arcon del barquero, bueno)
        for (Choza k : CHOZAS) {
            int a = k.w() / 2, bb = k.d() / 2;
            int fx = -k.puerta().getStepX(), fz = -k.puerta().getStepZ();
            int rx = CX + k.u() + (fx != 0 ? fx * (a - 1) : a - 1), rz = CZ + k.v() + (fz != 0 ? fz * (bb - 1) : bb - 1);
            int y = CHOZA + 1;
            boolean bueno = k.arcon();
            trozo(rx, y, rz, c -> {
                if (bueno) c.chest(rx, y, rz, k.puerta(), Velmor.BOTIN_BUENO);
                else c.barrel(rx, y, rz, Direction.UP, Velmor.BOTIN);
            });
        }
    }

    private void trozo(int x, int y, int z, Trozo.Obra o) {
        elementos.add(new Trozo(6, x, y, z, x, y, z, o));
    }

    // ------------------------------------------------------------------ los podridos

    private void ponerEsperas() {
        int P = Podrido.PODRIDO, F = Podrido.FATUO, H = Podrido.HINCHADO;
        int T = Enemigo.TUMBADO, R = Enemigo.REZA, D = Enemigo.DE_PIE;
        // flotando boca abajo junto a las pasarelas (se alzan al pasar)
        agua(P, 4, 12, "laguna");
        agua(P, -3, 10, "laguna");
        agua(H, 20, 21, "laguna");
        agua(P, -10, -3, "laguna");
        agua(H, 10, -14, "laguna");
        agua(P, -3, -19, "laguna");
        agua(P, 4, -20, "laguna");
        agua(P, -13, 8, "laguna");
        // la plaza: un hinchado de pie, uno de rodillas ante el fuego y el del fuego fatuo
        en(H, 3, PASO + 1, -3, giro(-3, 3), D, 2, "plaza");
        en(P, -2, PASO + 1, 2, giro(2, -2), R, 0, "plaza");
        en(F, -4, PASO + 1, -3, giro(4, 3), D, 2, "plaza");
        // dentro de las chozas
        int[][] dentro = {{P, D}, {H, D}, {F, D}, {P, R}, {F, D}, {P, D}, {P, R}, {H, D}};
        for (int i = 0; i < CHOZAS.length; i++) {
            Choza k = CHOZAS[i];
            Direction p = k.puerta();
            en(dentro[i][0], k.u(), CHOZA + 1, k.v(), giro(p.getStepX(), p.getStepZ()), dentro[i][1], 0, "choza_" + k.nombre());
        }
        // la capilla: dos rezando en el presbiterio (mirando al altar) y un hinchado en el agua de la nave
        en(P, -2, CAP_ALTAR + 1, -36, 180f, R, 0, "capilla");
        en(P, 2, CAP_ALTAR + 1, -36, 180f, R, 0, "capilla");
        en(H, 0, CAP_SUELO + 1, -29, 0f, D, 0, "capilla");
        // el embarcadero: el del fuego en el muelle y uno que ronda por la pasarela del santuario
        en(F, 38, PASO + 1, 24, 90f, D, 2, "muelle");
        en(P, 40, PASO + 1, 33, 180f, D, 3, "muelle");
    }

    private void agua(int variante, int u, int v, String grupo) {
        int x = CX + u, z = CZ + v;
        esperas.add(new Espera(variante, x, pie(x, z), z, giro(-u, -v), Enemigo.TUMBADO, 0, grupo));
        ocupar(x, z, 1);
    }

    private void en(int variante, int u, int y, int v, float giro, int postura, int ronda, String grupo) {
        esperas.add(new Espera(variante, CX + u, y, CZ + v, giro, postura, ronda, grupo));
    }

    // ------------------------------------------------------------------ piezas ya decididas

    /** Una lista de bloques ya decididos: cada chunk escribe lo que le cae. */
    static final class Lista {
        final LinkedHashMap<Long, BlockState> b = new LinkedHashMap<>();
        final Set<Long> une = new HashSet<>();

        void put(int x, int y, int z, BlockState s) {
            long k = BlockPos.asLong(x, y, z);
            b.remove(k);
            b.put(k, s);
            une.remove(k);
        }

        void une(int x, int y, int z, BlockState s) {
            put(x, y, z, s);
            une.add(BlockPos.asLong(x, y, z));
        }

        boolean hay(int x, int y, int z) {
            return b.containsKey(BlockPos.asLong(x, y, z));
        }

        Element pieza(int capa) {
            int n = b.size();
            long[] pos = new long[n];
            BlockState[] st = new BlockState[n];
            boolean[] u = new boolean[n];
            int i = 0;
            int x0 = Integer.MAX_VALUE, y0 = Integer.MAX_VALUE, z0 = Integer.MAX_VALUE, x1 = Integer.MIN_VALUE, y1 = Integer.MIN_VALUE, z1 = Integer.MIN_VALUE;
            for (Map.Entry<Long, BlockState> e : b.entrySet()) {
                long k = e.getKey();
                pos[i] = k;
                st[i] = e.getValue();
                u[i] = une.contains(k);
                i++;
                int x = BlockPos.getX(k), y = BlockPos.getY(k), z = BlockPos.getZ(k);
                x0 = Math.min(x0, x);
                y0 = Math.min(y0, y);
                z0 = Math.min(z0, z);
                x1 = Math.max(x1, x);
                y1 = Math.max(y1, y);
                z1 = Math.max(z1, z);
            }
            return new Trozo(capa, x0, y0, z0, x1, y1, z1, c -> {
                for (int j = 0; j < pos.length; j++) {
                    long k = pos[j];
                    int x = BlockPos.getX(k), z = BlockPos.getZ(k);
                    if (!c.inXZ(x, z)) continue;
                    if (u[j]) c.unir(x, BlockPos.getY(k), z, st[j]);
                    else c.set(x, BlockPos.getY(k), z, st[j]);
                }
            });
        }
    }

    // ------------------------------------------------------------------ la laguna

    /**
     * El suelo: dentro de la laguna, el fondo (barro, arcilla y grava) con el agua hasta arriba; los islotes y la orilla de
     * barro, con juncos donde toca el agua y hierba; nenufares y algas en el agua. Nada cerca de un santuario.
     */
    private final class Laguna extends Element {
        Laguna() {
            super(SEMILLA + 51);
            this.layer = 1;
            bounds(X0, 40, Z0, X1, 140, Z1);
        }

        @Override
        public void build(Canvas c) {
            for (int x = c.minX; x <= c.maxX; x++)
                for (int z = c.minZ; z <= c.maxZ; z++) {
                    if (x < X0 || x > X1 || z < Z0 || z > Z1) continue;
                    if (cercaSantuario(x, z)) continue;
                    int t = alto(x, z), s = suelo(x, z);
                    // cavar o rellenar hasta el suelo nuevo
                    if (s < t) for (int y = s + 1; y <= t; y++) c.set(x, y, z, y <= Terreno.AGUA ? Pal.WATER : Pal.AIR);
                    else if (s > t) for (int y = t + 1; y <= s; y++) c.set(x, y, z, b(Blocks.MUD));
                    if (s < Terreno.AGUA && s >= t) for (int y = s + 1; y <= Terreno.AGUA; y++) c.set(x, y, z, Pal.WATER);
                    boolean bajo = s < Terreno.AGUA;
                    int p = Hash.pct(x, s, z, seed);
                    if (s != t || orilla(x, z) < R_FUNDE) {
                        BlockState top = bajo ? (p < 55 ? b(Blocks.MUD) : p < 80 ? b(Blocks.CLAY) : Pal.GRAVEL)
                                : (p < 45 ? b(Blocks.MUD) : p < 62 ? b(Blocks.MUDDY_MANGROVE_ROOTS) : p < 80 ? Pal.GRASS_BLOCK : Pal.MOSS);
                        c.set(x, s, z, top);
                    }
                    if (ocupada(x, z)) continue;
                    int q = Hash.pct(x, s + 1, z, seed + 1);
                    if (bajo) {
                        // nenufares en el agua y algas en el fondo
                        if (q < 7 && s >= Terreno.AGUA - 3) c.set(x, Terreno.AGUA + 1, z, b(Blocks.LILY_PAD));
                        else if (q > 88 && s <= Terreno.AGUA - 2) c.set(x, s + 1, z, b(Blocks.SEAGRASS));
                        continue;
                    }
                    // en la orilla y los islotes: juncos (cana) donde toca el agua; hierba y helechos
                    if (s == Terreno.AGUA && q < 45 && tocaAgua(x, z)) {
                        int h = 1 + q % 3;
                        c.set(x, s, z, b(Blocks.MUD));
                        for (int k = 1; k <= h; k++) c.set(x, s + k, z, b(Blocks.SUGAR_CANE));
                    } else if (q < 22) c.set(x, s + 1, z, b(Blocks.GRASS));
                    else if (q < 28) c.set(x, s + 1, z, b(Blocks.FERN));
                    else if (q < 30) c.set(x, s + 1, z, b(Blocks.DEAD_BUSH));
                }
        }
    }

    /** Si junto a (x, z) hay agua de la laguna (para los juncos). */
    static boolean tocaAgua(int x, int z) {
        return suelo(x + 1, z) < Terreno.AGUA || suelo(x - 1, z) < Terreno.AGUA || suelo(x, z + 1) < Terreno.AGUA || suelo(x, z - 1) < Terreno.AGUA;
    }

    /** Lo de alrededor de un santuario de Morvain no se toca (su tarima se funde con el terreno hasta 10). */
    static boolean cercaSantuario(int x, int z) {
        for (SantuariosMorvain.Sitio s : SantuariosMorvain.todos())
            if (Math.abs(s.x() - x) <= 12 && Math.abs(s.z() - z) <= 12 && Math.hypot(s.x() - x, s.z() - z) <= 11.5) return true;
        return false;
    }

    // ------------------------------------------------------------------ para la prueba

    /** Lo que hay en la aldea ya generada (para la prueba): el atril con el libro, el arcon, el fuego, las tablas y el agua. */
    public static String resumen(net.minecraft.server.level.ServerLevel m) {
        Pilotes a = get();
        int[] at = a.sitio("atril"), ar = a.sitio("arcon");
        BlockPos pa = new BlockPos(at[0], at[1], at[2]), pr = new BlockPos(ar[0], ar[1], ar[2]);
        boolean libro = m.getBlockEntity(pa) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le && Perdidos.de(le.getBook()) == 20;
        boolean arcon = m.getBlockState(pr).is(Blocks.CHEST);
        boolean fuego = m.getBlockState(new BlockPos(CX, PASO + 1, CZ)).is(Blocks.SOUL_CAMPFIRE);
        int agua = 0, tablas = 0, muestras = 0;
        for (int u = -30; u <= 30; u += 6)
            for (int v = -30; v <= 30; v += 6) {
                BlockPos q = new BlockPos(CX + u, Terreno.AGUA, CZ + v);
                muestras++;
                if (m.getFluidState(q).is(net.minecraft.tags.FluidTags.WATER)) agua++;
            }
        for (int v = 7; v <= 25; v++) if (m.getBlockState(new BlockPos(CX, PASO, CZ + v)).is(net.minecraft.tags.BlockTags.PLANKS)
                || m.getBlockState(new BlockPos(CX, PASO, CZ + v)).getBlock() instanceof SlabBlock) tablas++;
        return "atril con el libro 20 " + (libro ? "SI" : "NO") + ", arcon " + (arcon ? "SI" : "NO") + ", fuego " + (fuego ? "SI" : "NO")
                + ", agua en la laguna " + agua + "/" + muestras + ", tablas del embarcadero " + tablas + "/19";
    }
}
