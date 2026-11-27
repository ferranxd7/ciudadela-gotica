package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Aldric;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Marcado;
import com.darkcraft.enemigos.Puestos;
import com.darkcraft.fortaleza.Perdidos;
import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Pal;
import com.darkcraft.gen.TorreCupula;
import com.darkcraft.pueblo.gen.Trozo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * El Cerco del Ancla (parte 7u), en la ladera de dentro del Crater de Sangre, al pie del ancla del sur (la de la cadena que
 * sube a la ciudadela, {@link Ciudadela#CADENAS}[2]), a la puerta del santuario del Ancla del Sur —(50, 1080), 220 al sur—:
 * donde los marcados golpean la cadena desde hace mil inviernos. u = x - {@link #CX}, v = z - {@link #CZ} (el norte, v
 * negativa: hacia el lago y la ciudadela).
 * <p>
 * Del santuario baja el <b>camino de los marcados</b> (losas negras, las <b>estelas</b> de los que vinieron antes y dos
 * <b>jaulas</b> colgadas) hasta la <b>puerta</b> del cerco, entre sus dos torres. Dentro de la <b>muralla</b> redonda (con
 * sus torres y una banda de piedra de sombra con las runas que laten) esta el <b>ancla</b> (la piramide escalonada de la
 * ciudadela, que no se toca), con la <b>escalera</b> tallada en su cara del sur hasta lo alto, donde empieza la cadena;
 * alrededor, el suelo roto a golpes; la <b>fragua</b> al este (el hogar, los yunques, los altos hornos, el pilon y los
 * mazos rotos), el <b>campamento</b> al oeste (los petates, la hoguera fria, barriles) y la <b>choza del primero</b> al
 * sureste (el atril con el <b>libro perdido 30</b> y el arcon). Se dibuja chunk a chunk al adornar; en su caja {@link Adorno}
 * no pone nada.
 */
public final class Cerco {
    public static final long SEMILLA = 0xCE2C_0A2C_7A50L;
    /** El ancla del sur: el centro del plano y lo alto del suelo (el de la piramide). */
    public static final int CX = 0, CZ = 860;
    public static final int SUELO = Ciudadela.CADENAS[2].suelo, PISO = SUELO + 1;
    /** El santuario del Ancla del Sur, en (u, v). */
    static final int SANT_U = 50, SANT_V = 220, SANT_R = 6;
    /** Lo allanado (radio) y lo que se funde con la ladera; la muralla (de dentro a fuera) y su alto. */
    static final double R_LLANO = 39, FUNDE = 10, R_MURO0 = 32.5, R_MURO1 = 34.5;
    static final int ALTO_MURO = 6, ALTO_TORRE = 10;
    /** Hasta donde (del ancla) se arrodillan los marcados al rugido de Aldric (parte 7v): el patio, las torres y la puerta. */
    public static final double R_RENDIR = R_LLANO + 3;
    /** La piramide del ancla (lo que no se toca): |u| y |v| hasta aqui. */
    static final int ANCLA = 11;
    /** Las torres de la muralla: {u, v}. Las dos de la puerta y las cuatro de las esquinas. */
    static final int[][] TORRES = {{-6, 34}, {6, 34}, {-24, 24}, {24, 24}, {-24, -24}, {24, -24}};
    static final double R_TORRE = 3.2;
    /** El camino: de la puerta al santuario. */
    static final double CAMINO_U0 = 0, CAMINO_V0 = 37, CAMINO_U1 = 46, CAMINO_V1 = 212;
    public static final int X0 = CX - 50, X1 = CX + 70, Z0 = CZ - 50, Z1 = CZ + 225;

    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/cerco"),
            BOTIN_BUENO = new ResourceLocation(DarkCraftMod.MODID, "chests/cerco_bueno");

    private static final int[] LADOS = {-1, 1};
    private static final int AW = X1 - X0 + 1, AD = Z1 - Z0 + 1;
    private static final int[] ALTO = new int[AW * AD];

    static {
        java.util.Arrays.fill(ALTO, Integer.MIN_VALUE);
    }

    /** El bloque de arriba del terreno de Morvain en (x, z) (sin el cerco). */
    public static int alto(int x, int z) {
        if (x < X0 || x > X1 || z < Z0 || z > Z1) return (int) Math.floor(Terreno.columna(x, z).altura());
        int i = (z - Z0) * AW + (x - X0);
        int v = ALTO[i];
        if (v == Integer.MIN_VALUE) ALTO[i] = v = (int) Math.floor(Terreno.columna(x, z).altura());
        return v;
    }

    /** Uno del cerco: donde espera (y: lo que pisa), hacia donde mira, su variante, como espera, cuanto ronda y su grupo. */
    public record Espera(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    private static Cerco unico;

    /** Que se vuelva a hacer el plano la proxima vez (con los bloques del mod ya registrados: ver {@link Subida#rehacer}). */
    public static synchronized void rehacer() {
        unico = null;
    }

    public static synchronized Cerco get() {
        if (unico == null) unico = new Cerco();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    /** Lo que hay (para la prueba y las vistas). */
    public int estelas, jaulas, yunques, petates, farolas;

    private Cerco() {
        elementos.add(new Terraza());
        elementos.add(new Muralla());
        Pilotes.Lista de = new Pilotes.Lista();
        escalera(de);
        golpes(de);
        fragua(de);
        campamento(de);
        choza(de);
        farolas(de);
        camino(de);
        elementos.add(de.pieza(4));
        trozos();
        // ---- los sitios con nombre
        double[] fin = punto(1.0);
        sitio("entrada", (int) Math.round(fin[0]), alto(x((int) Math.round(fin[0])), z((int) Math.round(fin[1]))) + 1, (int) Math.round(fin[1]));
        double[] med = punto(0.5);
        sitio("camino", (int) Math.round(med[0]), alto(x((int) Math.round(med[0])), z((int) Math.round(med[1]))) + 1, (int) Math.round(med[1]));
        sitio("puerta", 0, PISO, 38);
        sitio("patio", 0, PISO, 22);
        sitio("ancla", 0, PISO, 14);
        sitio("cima", 0, PISO + 8, 3);
        sitio("fragua", 16, PISO, -3);
        sitio("campamento", -16, PISO, 2);
        sitio("choza", 14, PISO, 19);
        sitio("atril", 21, PISO, 18);
        sitio("arcon", 21, PISO, 21);
        sitio("torre_este", 6, PISO + ALTO_TORRE, 34);
        sitio("torre_oeste", -6, PISO + ALTO_TORRE, 34);
        sitio("alto", 30, PISO + 40, 70);
        // ---- los puestos
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo del cerco (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en lo del cerco: ahi {@link Adorno} no pone nada. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= X0 && x0 <= X1 && z0 + 15 >= Z0 && z0 <= Z1;
    }

    /** Lo lejos de (x, z) al ancla. */
    public static double distancia(double x, double z) {
        return Math.hypot(x - CX - 0.5, z - CZ - 0.5);
    }

    /** Si (x, z) esta en el cerco (lo allanado o el camino): para el nombre del sitio. */
    public static boolean dentro(double x, double z) {
        double u = x - CX - 0.5, v = z - CZ - 0.5;
        if (Math.hypot(u, v) <= R_LLANO) return true;
        double[] c = cercaDelCamino(u, v);
        return c[0] < 4 && c[1] > 0 && c[1] < 1 && Math.hypot(u - SANT_U, v - SANT_V) > SANT_R + 2;
    }

    /** Si (x, z) esta dentro de la muralla (para el logro). */
    public static boolean enCerco(double x, double z) {
        return Math.hypot(x - CX - 0.5, z - CZ - 0.5) < R_MURO0 - 0.5;
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} del cerco en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los marcados del cerco (los apunta {@link MorvainEnemigos}). */
    public List<Espera> esperas() {
        return esperas;
    }

    /** Los sitios con nombre (la entrada, el camino, la puerta, el ancla, la cima, la fragua...): {x, y, z} (y: lo que se pisa). */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    private void sitio(String nombre, int u, int y, int v) {
        sitios.put(nombre, new int[]{CX + u, y, CZ + v});
    }

    // ------------------------------------------------------------------ el plano

    static int x(int u) {
        return CX + u;
    }

    static int z(int v) {
        return CZ + v;
    }

    static BlockState b(Block k) {
        return k.defaultBlockState();
    }

    private static void put(Pilotes.Lista m, int u, int y, int v, BlockState s) {
        m.put(x(u), y, z(v), s);
    }

    private static void une(Pilotes.Lista m, int u, int y, int v, BlockState s) {
        m.une(x(u), y, z(v), s);
    }

    /** Si (u, v) es de la piramide del ancla (lo que no se toca). */
    static boolean ancla(int u, int v) {
        return Math.abs(u) <= ANCLA && Math.abs(v) <= ANCLA;
    }

    /** Si en (u, v) hay piramide al nivel del suelo (lado 10 y las puas de las esquinas): lo que la terraza no toca. */
    static boolean piramide(int u, int v) {
        return Math.max(Math.abs(u), Math.abs(v)) <= ANCLA - 1 || Math.abs(u) == ANCLA && Math.abs(v) == ANCLA;
    }

    /** Lo lejos de (u, v) al centro. */
    static double r(double u, double v) {
        return Math.hypot(u, v);
    }

    /** Si (u, v) es de la muralla. */
    static boolean muro(int u, int v) {
        double d = r(u, v);
        return d >= R_MURO0 && d < R_MURO1;
    }

    /** Si (u, v) es de una torre: su indice en {@link #TORRES} o -1. */
    static int torre(int u, int v) {
        for (int i = 0; i < TORRES.length; i++) if (Math.hypot(u - TORRES[i][0], v - TORRES[i][1]) <= R_TORRE) return i;
        return -1;
    }

    /** Si (u, y, v) es el hueco de la puerta (en la muralla del sur, apuntada). */
    static boolean puerta(int u, int y, int v) {
        if (v <= 0 || Math.abs(u) > 2) return false;
        int h = y - PISO;
        return h < 5 || h == 5 && Math.abs(u) <= 1 || h == 6 && u == 0;
    }

    /** El punto del camino a la parte t (0 = la puerta, 1 = junto al santuario): {u, v}. */
    static double[] punto(double t) {
        return new double[]{CAMINO_U0 + (CAMINO_U1 - CAMINO_U0) * t, CAMINO_V0 + (CAMINO_V1 - CAMINO_V0) * t};
    }

    /** Lo lejos de (u, v) a la linea del camino y por donde cae (0-1): {distancia, t}. */
    static double[] cercaDelCamino(double u, double v) {
        double dx = CAMINO_U1 - CAMINO_U0, dz = CAMINO_V1 - CAMINO_V0, l2 = dx * dx + dz * dz;
        double t = ((u - CAMINO_U0) * dx + (v - CAMINO_V0) * dz) / l2;
        double tc = Math.max(0, Math.min(1, t));
        return new double[]{Math.hypot(u - (CAMINO_U0 + dx * tc), v - (CAMINO_V0 + dz * tc)), t};
    }

    /** Lo alto del suelo en (x, z): lo allanado (y lo que se funde) o MIN_VALUE donde no se toca. */
    static int objetivo(int x, int z) {
        int u = x - CX, v = z - CZ;
        double d = r(u, v) - R_LLANO;
        if (d <= 0) return SUELO;
        if (d >= FUNDE) return Integer.MIN_VALUE;
        double t = Ruido.suave(0, FUNDE, d);
        return (int) Math.round(Ruido.lerp(t, SUELO, alto(x, z)));
    }

    /** Lo que se pisa en (x, z) (lo allanado o el terreno). */
    public static int pie(int x, int z) {
        int o = objetivo(x, z);
        return (o != Integer.MIN_VALUE ? o : alto(x, z)) + 1;
    }

    static boolean junto(int u, int v, int r) {
        return Math.hypot(u - SANT_U, v - SANT_V) < r;
    }

    /** La piedra de la muralla y las torres: ladrillo de pizarra con piedra negra; una banda de piedra de sombra. */
    static BlockState piedra(int x, int y, int z) {
        if (y == PISO + 3) return Hash.pct(x, y, z, SEMILLA + 5) < 70 ? TorreCupula.piedraSombra() : b(Blocks.POLISHED_BLACKSTONE_BRICKS);
        int p = Hash.pct(x, y, z, SEMILLA + 3);
        return p < 46 ? b(Blocks.DEEPSLATE_BRICKS) : p < 66 ? b(Blocks.CRACKED_DEEPSLATE_BRICKS) : p < 86 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS)
                : p < 95 ? b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS) : b(Blocks.BLACKSTONE);
    }

    /** El suelo de dentro: piedra negra, ceniza y grava; al pie del ancla, roto a golpes. */
    static BlockState suelo(int u, int v, int p) {
        int m = Math.max(Math.abs(u), Math.abs(v));
        if (m <= ANCLA + 3) {
            if (p < 30) return b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
            if (p < 50) return b(Blocks.COBBLED_DEEPSLATE);
            if (p < 62) return b(Blocks.GRAVEL);
            return p < 85 ? b(Blocks.POLISHED_BLACKSTONE) : b(Blocks.CRACKED_DEEPSLATE_TILES);
        }
        if (p < 34) return b(Blocks.BLACKSTONE);
        if (p < 52) return b(Blocks.CRACKED_DEEPSLATE_TILES);
        if (p < 66) return b(Blocks.SOUL_SOIL);
        if (p < 80) return b(Blocks.GRAVEL);
        return p < 92 ? b(Blocks.POLISHED_BLACKSTONE) : b(Blocks.COARSE_DIRT);
    }

    /** La tierra de fuera, la de la ladera del crater: ceniza, grava, tierra roja y piedra negra. */
    static BlockState tierra(int u, int v, int p) {
        if (p < 30) return b(Blocks.COARSE_DIRT);
        if (p < 52) return b(Blocks.GRAVEL);
        if (p < 70) return b(Blocks.SOUL_SOIL);
        if (p < 86) return b(Blocks.BLACKSTONE);
        return b(Blocks.RED_SAND);
    }

    static BlockState vela(int n, boolean roja) {
        return (roja ? Blocks.RED_CANDLE : Blocks.BLACK_CANDLE).defaultBlockState().setValue(CandleBlock.CANDLES, Math.max(1, Math.min(4, n)))
                .setValue(CandleBlock.LIT, true);
    }

    static BlockState cadena(Direction.Axis a) {
        return Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS, a);
    }

    // ------------------------------------------------------------------ el suelo y la muralla

    /**
     * La terraza: corta lo que sobra hasta el suelo y rellena por debajo (por el norte la ladera baja: se rellena); dentro,
     * el suelo de piedra negra roto; fuera, la ladera de ceniza. El ancla no se toca, ni la tarima del santuario.
     */
    private final class Terraza extends Element {
        Terraza() {
            super(SEMILLA + 51);
            this.layer = 1;
            bounds(CX - 50, SUELO - 40, CZ - 50, CX + 50, SUELO + 30, CZ + 50);
        }

        @Override
        public void build(Canvas c) {
            for (int x = Math.max(c.minX, x0); x <= Math.min(c.maxX, x1); x++)
                for (int z = Math.max(c.minZ, z0); z <= Math.min(c.maxZ, z1); z++) {
                    int u = x - CX, v = z - CZ;
                    if (piramide(u, v) || junto(u, v, SANT_R)) continue;
                    int o = objetivo(x, z);
                    if (o == Integer.MIN_VALUE) continue;
                    int t = alto(x, z);
                    // (por encima de lo allanado, hasta 6 de lo que habia; nunca donde pasa la cadena: va por encima de PISO + 6)
                    for (int y = o + 1; y <= Math.min(Math.max(t, o) + 6, PISO + 5); y++) c.set(x, y, z, Pal.AIR);
                    for (int y = t + 1; y < o; y++) c.set(x, y, z, y >= o - 1 ? b(Blocks.COARSE_DIRT) : b(Blocks.BLACKSTONE));
                    int p = Hash.pct(x, o, z, seed);
                    double d = r(u, v);
                    if (o == SUELO && d < R_MURO0) {
                        c.set(x, o, z, suelo(u, v, p));
                        int q = Hash.pct(x, o + 1, z, seed + 1);
                        if (Math.max(Math.abs(u), Math.abs(v)) > ANCLA + 3 && q < 2) c.set(x, o + 1, z, Varadero.hueso(Direction.Axis.X));
                        continue;
                    }
                    c.set(x, o, z, tierra(u, v, p));
                    if (o == SUELO) continue;
                    int q = Hash.pct(x, o + 1, z, seed + 2);
                    if (q < 2) c.set(x, o + 1, z, Pal.b(Blocks.DEAD_BUSH));
                    else if (q == 2) c.set(x, o + 1, z, b(Blocks.BLACKSTONE));
                }
        }
    }

    /** La muralla redonda con su puerta, sus almenas y sus torres (mas altas, con sus merlones). */
    private final class Muralla extends Element {
        Muralla() {
            super(SEMILLA + 61);
            this.layer = 2;
            bounds(CX - 38, SUELO - 6, CZ - 38, CX + 38, PISO + ALTO_TORRE + 2, CZ + 38);
        }

        @Override
        public void build(Canvas c) {
            for (int x = Math.max(c.minX, x0); x <= Math.min(c.maxX, x1); x++)
                for (int z = Math.max(c.minZ, z0); z <= Math.min(c.maxZ, z1); z++) {
                    int u = x - CX, v = z - CZ;
                    int ti = torre(u, v);
                    if (ti >= 0) {
                        // la torre: maciza, con sus merlones arriba
                        double dt = Math.hypot(u - TORRES[ti][0], v - TORRES[ti][1]);
                        for (int y = SUELO - 6; y <= PISO + ALTO_TORRE - 1; y++) c.set(x, y, z, y < SUELO ? b(Blocks.BLACKSTONE) : piedra(x, y, z));
                        c.set(x, PISO + ALTO_TORRE - 1, z, dt <= R_TORRE - 1 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS) : piedra(x, PISO + ALTO_TORRE - 1, z));
                        if (dt > R_TORRE - 1) {
                            double a = Math.atan2(v - TORRES[ti][1], u - TORRES[ti][0]);
                            if (((int) Math.floor((a + Math.PI) * 9 / Math.PI)) % 2 == 0) c.set(x, PISO + ALTO_TORRE, z, piedra(x, PISO + ALTO_TORRE, z));
                        }
                        continue;
                    }
                    if (!muro(u, v)) continue;
                    for (int y = SUELO - 6; y <= PISO + ALTO_TORRE + 1; y++) {
                        if (y > PISO + ALTO_MURO) break;
                        if (y >= PISO && puerta(u, y, v)) continue;
                        c.set(x, y, z, y < SUELO ? b(Blocks.BLACKSTONE) : y == SUELO ? b(Blocks.POLISHED_DEEPSLATE) : piedra(x, y, z));
                    }
                    // las almenas: una si, una no, en la cara de fuera; con la puerta no
                    if (r(u, v) >= R_MURO0 + 1 && !(v > 0 && Math.abs(u) <= 3)) {
                        double a = Math.atan2(v, u);
                        if (((int) Math.floor((a + Math.PI) * 34 / Math.PI)) % 2 == 0) c.set(x, PISO + ALTO_MURO + 1, z, piedra(x, PISO + ALTO_MURO + 1, z));
                    }
                }
        }
    }

    // ------------------------------------------------------------------ lo de dentro

    /**
     * La escalera tallada en la cara del sur del ancla (tres de ancho): del suelo a lo alto de la piramide, donde empieza
     * la cadena. Es lo unico que toca el ancla.
     */
    private void escalera(Pilotes.Lista m) {
        for (int k = 1; k <= 8; k++)
            for (int u = -1; u <= 1; u++) {
                int v = 12 - k;
                put(m, u, PISO + k - 1, v, Varadero.escalera(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.NORTH, false));
                for (int y = PISO + k; y <= PISO + k + 2 && y <= PISO + 8; y++) put(m, u, y, v, Pal.AIR);
            }
        // los dos lados de la escalera, con su vela roja en cada peldano de los de abajo
        for (int s : LADOS)
            for (int k = 1; k <= 4; k += 3) put(m, s * 2, PISO + k - 1, 12 - k, vela(1 + k % 3, true));
    }

    /** Al pie del ancla: los golpes de los rompedores (piedra suelta, cadenas rotas tiradas) y velas. */
    private void golpes(Pilotes.Lista m) {
        int[][] trozos = {{-13, -5, 0}, {13, -6, 1}, {-12, 6, 1}, {12, 7, 0}, {-5, -13, 0}, {6, -13, 1}, {-14, 1, 1}, {14, 2, 0}};
        for (int[] t : trozos) {
            put(m, t[0], PISO, t[1], cadena(t[2] == 0 ? Direction.Axis.X : Direction.Axis.Z));
            put(m, t[0] + (t[2] == 0 ? 1 : 0), PISO, t[1] + (t[2] == 0 ? 0 : 1), cadena(t[2] == 0 ? Direction.Axis.X : Direction.Axis.Z));
        }
        for (int[] q : new int[][]{{-12, -12}, {12, -12}, {-12, 12}, {12, 12}}) {
            put(m, q[0], PISO, q[1], b(Blocks.COBBLED_DEEPSLATE));
            put(m, q[0], PISO + 1, q[1], vela(3, true));
        }
    }

    /**
     * La fragua, al este: el hogar de ladrillo con las brasas (magma) y su chimenea, dos yunques rotos, dos altos hornos, el
     * pilon con el agua (roja: la sangre) y los mazos rotos apoyados en la muralla (hierro y cadenas).
     */
    private void fragua(Pilotes.Lista m) {
        // el hogar: 3 x 3 con las brasas, y la chimenea contra la muralla
        for (int u = 21; u <= 23; u++)
            for (int v = -6; v <= -4; v++) {
                boolean centro = u == 22 && v == -5;
                put(m, u, PISO, v, centro ? b(Blocks.MAGMA_BLOCK) : b(Blocks.POLISHED_BLACKSTONE_BRICKS));
                if (!centro && (u == 23 || v != -5)) put(m, u, PISO + 1, v, Varadero.losa(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, SlabType.BOTTOM));
            }
        for (int y = PISO + 1; y <= PISO + 6; y++) une(m, 24, y, -5, b(Blocks.DEEPSLATE_BRICK_WALL));
        put(m, 24, PISO + 7, -5, b(Blocks.CAMPFIRE).setValue(CampfireBlock.LIT, true).setValue(CampfireBlock.SIGNAL_FIRE, false));
        // los yunques (rotos) delante del hogar
        put(m, 19, PISO, -5, b(Blocks.DAMAGED_ANVIL).setValue(AnvilBlock.FACING, Direction.NORTH));
        put(m, 19, PISO, -1, b(Blocks.CHIPPED_ANVIL).setValue(AnvilBlock.FACING, Direction.EAST));
        yunques += 2;
        // los altos hornos y el pilon
        put(m, 23, PISO, -9, b(Blocks.BLAST_FURNACE).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST));
        put(m, 23, PISO, -8, b(Blocks.BLAST_FURNACE).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST));
        put(m, 23, PISO, -1, b(Blocks.WATER_CAULDRON).setValue(LayeredCauldronBlock.LEVEL, 3));
        put(m, 23, PISO, 1, b(Blocks.GRINDSTONE).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST));
        // los mazos rotos: cabezas de hierro tiradas y mangos (barrotes) apoyados
        for (int[] q : new int[][]{{26, -9}, {27, -6}, {27, -2}, {26, 2}}) {
            put(m, q[0], PISO, q[1], TorreCupula.hierroNegro());
            une(m, q[0], PISO + 1, q[1], b(Blocks.IRON_BARS));
        }
        // montones de cadena y carbon
        for (int[] q : new int[][]{{20, -9}, {21, 2}}) {
            put(m, q[0], PISO, q[1], cadena(Direction.Axis.X));
            put(m, q[0] + 1, PISO, q[1], b(Blocks.COAL_BLOCK));
        }
    }

    /** El campamento, al oeste: los petates (alfombras con su almohada), la hoguera fria con sus troncos y barriles. */
    private void campamento(Pilotes.Lista m) {
        for (int v : new int[]{-7, -3, 1, 5}) {
            petates++;
            put(m, -24, PISO, v, b(Blocks.RED_CARPET));
            put(m, -23, PISO, v, b(Blocks.RED_CARPET));
            put(m, -25, PISO, v, b(Blocks.WHITE_CARPET));
        }
        put(m, -18, PISO, -1, b(Blocks.CAMPFIRE).setValue(CampfireBlock.LIT, false));
        for (int[] d : new int[][]{{-16, -1, 0}, {-20, -1, 0}, {-18, 1, 1}, {-18, -3, 1}})
            put(m, d[0], PISO, d[1], Varadero.tronco(Blocks.DARK_OAK_LOG, d[2] == 0 ? Direction.Axis.Z : Direction.Axis.X));
        put(m, -27, PISO, 8, b(Blocks.BARREL).setValue(BlockStateProperties.FACING, Direction.UP));
        put(m, -26, PISO, 9, b(Blocks.BARREL).setValue(BlockStateProperties.FACING, Direction.UP));
        put(m, -27, PISO + 1, 8, b(Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, 5));
    }

    /**
     * La choza del primero, al sureste: piedra de pizarra con su tejado de losas negras, la puerta al oeste; dentro, la cama
     * (lana roja), la mesa con velas, el atril y el arcon (los pone {@link #trozos}).
     */
    private void choza(Pilotes.Lista m) {
        int u0 = 16, u1 = 22, v0 = 16, v1 = 22;
        for (int u = u0; u <= u1; u++)
            for (int v = v0; v <= v1; v++) {
                boolean borde = u == u0 || u == u1 || v == v0 || v == v1;
                if (!borde) {
                    for (int y = PISO; y <= PISO + 2; y++) put(m, u, y, v, Pal.AIR);
                    put(m, u, PISO + 3, v, Varadero.losa(Blocks.BLACKSTONE_SLAB, SlabType.BOTTOM));
                    continue;
                }
                for (int y = PISO; y <= PISO + 2; y++) {
                    if (u == u0 && v == 19 && y <= PISO + 1) {
                        put(m, u, y, v, Pal.AIR);       // la puerta
                        continue;
                    }
                    int p = Hash.pct(u, y, v, SEMILLA + 71);
                    put(m, u, y, v, p < 60 ? b(Blocks.COBBLED_DEEPSLATE) : p < 85 ? b(Blocks.DEEPSLATE_BRICKS) : b(Blocks.CRACKED_DEEPSLATE_BRICKS));
                }
                put(m, u, PISO + 3, v, Varadero.losa(Blocks.BLACKSTONE_SLAB, SlabType.BOTTOM));
            }
        // las ventanucos
        put(m, u1, PISO + 1, 18, b(Blocks.IRON_BARS));
        put(m, 19, PISO + 1, v1, b(Blocks.IRON_BARS));
        // la cama y la mesa
        put(m, 17, PISO, 21, b(Blocks.RED_WOOL));
        put(m, 18, PISO, 21, b(Blocks.RED_WOOL));
        put(m, 17, PISO + 1, 21, b(Blocks.WHITE_CARPET));
        put(m, 19, PISO, 17, b(Blocks.DARK_OAK_FENCE));
        put(m, 19, PISO + 1, 17, Varadero.losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
        put(m, 19, PISO + 1, 17, vela(2, true));
        put(m, 17, PISO, 17, b(Blocks.BARREL).setValue(BlockStateProperties.FACING, Direction.UP));
    }

    /** Farolas de almas en postes de piedra negra, por el patio. */
    private void farolas(Pilotes.Lista m) {
        int[][] f = {{-8, 26}, {8, 26}, {-26, 10}, {26, 10}, {-20, -16}, {20, -16}, {-10, 16}, {10, 16}};
        for (int[] q : f) {
            farolas++;
            for (int y = PISO; y <= PISO + 2; y++) une(m, q[0], y, q[1], b(Blocks.POLISHED_BLACKSTONE_WALL));
            put(m, q[0], PISO + 3, q[1], b(Blocks.SOUL_LANTERN).setValue(LanternBlock.HANGING, false));
        }
    }

    /**
     * El camino de los marcados, del santuario a la puerta (siguiendo la ladera): losas negras rotas de tres de ancho; a
     * los lados, las estelas (un poste de piedra negra con su remate, una calavera o una vela al pie) y dos jaulas colgadas
     * de su horca.
     */
    private void camino(Pilotes.Lista m) {
        double largo = Math.hypot(CAMINO_U1 - CAMINO_U0, CAMINO_V1 - CAMINO_V0);
        double nx = -(CAMINO_V1 - CAMINO_V0) / largo, nz = (CAMINO_U1 - CAMINO_U0) / largo;
        for (double s = 0; s <= largo; s += 0.5) {
            double[] p = punto(s / largo);
            for (int k = -1; k <= 1; k++) {
                int u = (int) Math.round(p[0] + nx * k), v = (int) Math.round(p[1] + nz * k);
                if (r(u, v) < R_MURO1 + 1 || junto(u, v, SANT_R)) continue;
                int xx = x(u), zz = z(v);
                int o = objetivo(xx, zz);
                int y = o != Integer.MIN_VALUE ? o : alto(xx, zz);
                int q = Hash.pct(xx, y, zz, SEMILLA + 81);
                if (q < 14) continue;     // donde faltan losas, la tierra
                put(m, u, y, v, q < 60 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS) : q < 85 ? b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS)
                        : b(Blocks.POLISHED_BLACKSTONE));
            }
        }
        // las estelas, cada 18 bloques, a un lado y a otro
        int n = 0;
        for (double s = 12; s <= largo - 10; s += 18, n++) {
            double[] p = punto(s / largo);
            int lado = n % 2 == 0 ? 1 : -1;
            int u = (int) Math.round(p[0] + nx * 3 * lado), v = (int) Math.round(p[1] + nz * 3 * lado);
            if (r(u, v) < R_MURO1 + 3 || junto(u, v, SANT_R + 2)) continue;
            int y = pie(x(u), z(v));
            estelas++;
            put(m, u, y - 1, v, b(Blocks.POLISHED_BLACKSTONE));
            une(m, u, y, v, b(Blocks.POLISHED_BLACKSTONE_WALL));
            une(m, u, y + 1, v, b(Blocks.POLISHED_BLACKSTONE_WALL));
            put(m, u, y + 2, v, b(Blocks.CHISELED_POLISHED_BLACKSTONE));
            int pu = (int) Math.round(p[0] + nx * 2 * lado), pv = (int) Math.round(p[1] + nz * 2 * lado);
            if (n % 3 == 0) put(m, pu, pie(x(pu), z(pv)), pv, b(Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, (n * 5) % 16));
            else put(m, pu, pie(x(pu), z(pv)), pv, vela(1 + n % 3, true));
        }
        // las dos jaulas: la horca (un poste con su brazo) y la jaula de barrotes colgando de una cadena, con sus huesos
        for (double t : new double[]{0.33, 0.7}) {
            double[] p = punto(t);
            int lado = t < 0.5 ? -1 : 1;
            int u = (int) Math.round(p[0] + nx * 5 * lado), v = (int) Math.round(p[1] + nz * 5 * lado);
            int y = pie(x(u), z(v));
            jaulas++;
            for (int h = 0; h <= 6; h++) put(m, u, y + h, v, Varadero.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
            int bu = u - lado, bv = v;
            put(m, bu, y + 6, bv, Varadero.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
            int cu = u - 2 * lado;
            put(m, cu, y + 6, bv, Varadero.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
            put(m, cu, y + 5, bv, cadena(Direction.Axis.Y));
            for (int h = 2; h <= 4; h++)
                for (int[] d : new int[][]{{0, 0}, {-lado, 0}, {0, 1}, {-lado, 1}}) {
                    int ju = cu + d[0], jv = bv + d[1];
                    if (h == 4 || h == 2) put(m, ju, y + h, jv, b(Blocks.DARK_OAK_SLAB));
                    else une(m, ju, y + h, jv, b(Blocks.IRON_BARS));
                }
            put(m, cu, y + 3, bv, b(Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, lado > 0 ? 4 : 12));
        }
    }

    private void trozos() {
        // en la choza del primero: el atril de cara a la puerta (al oeste) y el arcon en el rincon
        trozo(21, PISO, 18, c -> c.lectern(x(21), PISO, z(18), Direction.WEST, Perdidos.libro(30)));
        trozo(21, PISO, 21, c -> c.chest(x(21), PISO, z(21), Direction.WEST, BOTIN_BUENO));
        // en el campamento y en la fragua, arcones y barriles de los de siempre
        trozo(-27, PISO, -9, c -> c.chest(x(-27), PISO, z(-9), Direction.EAST, BOTIN));
        trozo(26, PISO, -12, c -> c.barrel(x(26), PISO, z(-12), Direction.UP, BOTIN));
    }

    private void trozo(int u, int y, int v, Trozo.Obra o) {
        elementos.add(new Trozo(6, x(u), y, z(v), x(u), y, z(v), o));
    }

    // ------------------------------------------------------------------ los marcados

    private static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;

    /** El hacia donde mira de Minecraft (0 = sur, 90 = oeste) para una direccion (du, dv). */
    static float giro(double du, double dv) {
        return (float) (Math.toDegrees(Math.atan2(dv, du)) - 90.0);
    }

    private void ponerEsperas() {
        int R = Marcado.ROMPEDOR, C = Marcado.CABALLERO, Z = Marcado.CAZADOR;
        int Re = Enemigo.REZA, D = Enemigo.DE_PIE, Tu = Enemigo.TUMBADO;
        // Aldric, el Primer Marcado (parte 7v): de rodillas en lo alto del ancla, donde empieza la cadena, de cara a la puerta
        en(0, 0, PISO + 8, 3, SUR, Re, 0, Puestos.JEFE);
        // por el camino: de rodillas mirando a la cadena (al norte) y alguno caido junto a las estelas
        enCamino(R, 0.82, 0, NORTE, Re, "camino");
        enCamino(Z, 0.62, 1, NORTE, Re, "camino");
        enCamino(C, 0.42, -1, NORTE, Re, "camino");
        enCamino(C, 0.72, 2, NORTE, Tu, "camino");
        enCamino(R, 0.52, -2, NORTE, Tu, "camino");
        // la puerta: dos caballeros de guardia y los dos cazadores de lo alto de sus torres
        en(C, -3, PISO, 39, SUR, D, 0, "puerta");
        en(C, 3, PISO, 39, SUR, D, 0, "puerta");
        en(Z, -6, PISO + ALTO_TORRE, 34, SUR, D, 0, "en_alto");
        en(Z, 6, PISO + ALTO_TORRE, 34, SUR, D, 0, "en_alto");
        // al pie del ancla, los rompedores de rodillas golpeandola
        for (int[] q : new int[][]{{-14, -3}, {14, -3}, {-12, 9}, {12, 9}, {-3, -14}})
            en(R, q[0], PISO, q[1], giro(-q[0], -q[1]), Re, 0, "ancla");
        // el patio: dos caballeros de ronda; el de la puerta de la choza; el cazador de lo alto del ancla
        en(C, -9, PISO, 22, SUR, D, 5, "patio");
        en(C, 9, PISO, -22, ESTE, D, 5, "patio");
        en(C, 14, PISO, 19, OESTE, D, 0, "choza");
        // los cazadores de las torres de las esquinas del norte
        en(Z, -24, PISO + ALTO_TORRE, -24, giro(24, 24), D, 0, "en_alto");
        en(Z, 24, PISO + ALTO_TORRE, -24, giro(-24, 24), D, 0, "en_alto");
        // la fragua: un rompedor de rodillas golpeando el yunque y otro de pie
        en(R, 17, PISO, -5, ESTE, Re, 0, "fragua");
        en(R, 18, PISO, 2, OESTE, D, 2, "fragua");
        // el campamento: dos caidos en los petates y uno de rodillas junto a la hoguera
        en(Z, -24, PISO, -3, OESTE, Tu, 0, "campamento");
        en(C, -24, PISO, 5, OESTE, Tu, 0, "campamento");
        en(C, -15, PISO, -1, OESTE, Re, 0, "campamento");
    }

    private void en(int variante, int u, int y, int v, float giro, int postura, int ronda, String grupo) {
        esperas.add(new Espera(variante, x(u), y, z(v), giro, postura, ronda, grupo));
    }

    /** Uno en el camino, en la parte t, a k bloques de su linea (a la derecha si k es positivo). */
    private void enCamino(int variante, double t, int k, float giro, int postura, String grupo) {
        double largo = Math.hypot(CAMINO_U1 - CAMINO_U0, CAMINO_V1 - CAMINO_V0);
        double nx = -(CAMINO_V1 - CAMINO_V0) / largo, nz = (CAMINO_U1 - CAMINO_U0) / largo;
        double[] p = punto(t);
        int u = (int) Math.round(p[0] + nx * k), v = (int) Math.round(p[1] + nz * k);
        esperas.add(new Espera(variante, x(u), pie(x(u), z(v)), z(v), giro, postura, 0, grupo));
    }

    // ------------------------------------------------------------------ Aldric: la cadena que cierra la puerta y la paz (parte 7v)

    /** Lo que tarda en abrirse la puerta desde que cae Aldric (dice su ultima frase). */
    public static final int ABRIR_TRAS = 120;
    /** Para las pruebas: las veces que se ha cerrado y abierto la puerta, y los marcados que han descansado. */
    public static int SELLADOS, ABIERTOS, DESCANSAN;
    private static Aldric visto;
    private static long reloj;

    /** Si Aldric ha caido (y no ha vuelto): la cadena del sur queda libre y los marcados que seguian de rodillas descansan. */
    public static boolean enPaz(net.minecraft.server.MinecraftServer srv) {
        return Puestos.muerto(srv, MorvainEnemigos.CERCO, MorvainEnemigos.aldric());
    }

    /** Las celdas de la cadena que cierra la puerta: la cara de dentro (barrera) y la de fuera (las cadenas que se ven). */
    static List<BlockPos> celdasSello(boolean fuera) {
        List<BlockPos> l = new ArrayList<>();
        int v = fuera ? 34 : 33;
        for (int u = -2; u <= 2; u++)
            for (int y = PISO; y <= PISO + 6; y++)
                if (puerta(u, y, v)) l.add(new BlockPos(x(u), y, z(v)));
        return l;
    }

    /** Si la puerta deberia estar cerrada ahora: Aldric peleando, o recien caido (aun habla). */
    static boolean quiereCerrar(ServerLevel m) {
        Enemigo e = Puestos.enemigo(MorvainEnemigos.CERCO, MorvainEnemigos.aldric());
        if (e instanceof Aldric a && !a.isRemoved() && !a.reflejo()) visto = a;
        Aldric a = visto;
        if (a == null || a.isRemoved() || a.level() != m) {
            visto = null;
            return false;
        }
        if (a.isAlive()) return a.enPelea();
        return a.deathTime < ABRIR_TRAS;
    }

    /** Si la puerta esta cerrada en el mundo (mira la celda de en medio de la cara de dentro). */
    public static boolean sellado(ServerLevel m) {
        return m.getBlockState(new BlockPos(x(0), PISO + 1, z(33))).is(Blocks.BARRIER);
    }

    /**
     * Cada tick con alguien cerca (lo llama {@link Morvain#onTick}): la cadena cierra la puerta en cuanto empieza la pelea con
     * Aldric (y antes saca hacia dentro a quien este en la puerta) y se rompe al acabar. Lo que esta cerrado se mira en el mundo:
     * si el servidor se para a media pelea, al volver se abre solo.
     */
    public static void tick(ServerLevel m) {
        BlockPos centro = new BlockPos(x(0), PISO, z(33));
        if (!m.isLoaded(centro)) return;
        long ahora = ++reloj;
        boolean quiere = quiereCerrar(m), hay = sellado(m);
        if (quiere && !hay) {
            sacarDeLaPuerta(m);
            for (BlockPos p : celdasSello(false)) {
                BlockState st = m.getBlockState(p);
                if (st.isAir() || st.canBeReplaced()) m.setBlock(p, Blocks.BARRIER.defaultBlockState(), 3);
            }
            for (BlockPos p : celdasSello(true)) {
                BlockState st = m.getBlockState(p);
                if (st.isAir() || st.canBeReplaced()) m.setBlock(p, cadena(Direction.Axis.Y), 3);
            }
            sonar(m, DarkCraftMod.SFX_ALDRIC_ENCADENAR.get(), 2.0f, 0.8f);
            m.sendParticles(Marcado.SOMBRA, x(0) + 0.5, PISO + 2.5, z(34) + 0.5, 40, 1.2, 1.4, 0.3, 0.02);
            m.sendParticles(Marcado.MARCA, x(0) + 0.5, PISO + 2.5, z(33) + 0.5, 30, 1.2, 1.4, 0.2, 0.05);
            SELLADOS++;
            DarkCraftMod.LOGGER.info("[DarkCraft] La cadena cierra la puerta del cerco");
        } else if (!quiere && hay) abrir(m, true);
        // mientras esta cerrada, chispas rojas por la cadena
        if (hay && ahora % 5 == 0) m.sendParticles(Marcado.MARCA, x(0) + 0.5, PISO + 1.0 + m.random.nextDouble() * 4.0, z(33) + 0.6, 2, 1.0, 0.3, 0.1, 0.0);
    }

    /** La cadena de la puerta se rompe: quita la barrera y las cadenas (con su ruido, si {@code sonido}). */
    public static void abrir(ServerLevel m, boolean sonido) {
        int n = 0;
        for (BlockPos p : celdasSello(false))
            if (m.getBlockState(p).is(Blocks.BARRIER)) {
                m.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                n++;
            }
        for (BlockPos p : celdasSello(true))
            if (m.getBlockState(p).is(Blocks.CHAIN)) m.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        if (n == 0) return;
        ABIERTOS++;
        if (sonido) {
            sonar(m, DarkCraftMod.SFX_SELLO_ROMPE.get(), 1.8f, 0.8f);
            m.sendParticles(Marcado.SOMBRA, x(0) + 0.5, PISO + 2.5, z(34) + 0.5, 50, 1.2, 1.4, 0.3, 0.05);
            m.sendParticles(ParticleTypes.SOUL, x(0) + 0.5, PISO + 2.5, z(34) + 0.5, 16, 1.0, 1.2, 0.3, 0.03);
        }
        DarkCraftMod.LOGGER.info("[DarkCraft] La cadena de la puerta del cerco se rompe");
    }

    /** Antes de cerrarse: quien este en el hueco de la puerta pasa al lado de dentro (al patio). */
    private static void sacarDeLaPuerta(ServerLevel m) {
        AABB caja = new AABB(x(-2) - 0.3, PISO - 0.5, z(33) - 0.3, x(2) + 1.3, PISO + 7, z(34) + 1.3);
        for (net.minecraft.world.entity.Entity e : m.getEntities((net.minecraft.world.entity.Entity) null, caja,
                q -> q instanceof net.minecraft.world.entity.LivingEntity && q.isAlive()))
            e.teleportTo(e.getX(), e.getY(), z(32) + 0.4);
    }

    private static void sonar(ServerLevel m, net.minecraft.sounds.SoundEvent ev, float vol, float tono) {
        for (ServerPlayer p : m.players())
            if (p.distanceToSqr(x(0), PISO, z(33)) < 80 * 80)
                p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(net.minecraft.core.Holder.direct(ev), SoundSource.BLOCKS,
                        x(0) + 0.5, PISO + 2, z(33) + 0.5, vol, tono, m.random.nextLong()));
    }

    /**
     * Cada segundo: con Aldric caido, los marcados del cerco que seguian de rodillas (los que golpeaban el ancla, los del camino) y
     * los que se rindieron a su rugido descansan: se deshacen en sombra. Los de rodillas ya no vuelven; los que se rindieron, al
     * descansar alguien en un santuario (como cualquier caido).
     */
    public static void paz(ServerLevel m) {
        if (Puestos.sitio(MorvainEnemigos.CERCO) == null || !enPaz(m.getServer())) return;
        List<Puestos.Puesto> puestos = MorvainEnemigos.cerco();
        for (int i = 0; i < puestos.size(); i++) {
            Puestos.Puesto p = puestos.get(i);
            if (Puestos.JEFE.equals(p.grupo())) continue;
            Enemigo e = Puestos.enemigo(MorvainEnemigos.CERCO, i);
            if (!(e instanceof Marcado mk) || e.level() != m || mk.reflejo()) continue;
            if (!(mk.postura() == Enemigo.REZA || mk.rendido()) || mk.getTarget() != null || mk.accion() != 0) continue;
            m.sendParticles(Marcado.SOMBRA, mk.getX(), mk.getY() + 0.8, mk.getZ(), 30, 0.4, 0.6, 0.4, 0.02);
            m.sendParticles(Marcado.MARCA, mk.getX(), mk.getY() + 1.1, mk.getZ(), 10, 0.3, 0.4, 0.3, 0.05);
            mk.disolver();
            if (p.postura() != Enemigo.REZA) Puestos.marcar(m.getServer(), MorvainEnemigos.CERCO, i, true);
            DESCANSAN++;
        }
    }

    // ------------------------------------------------------------------ para la prueba

    /** Lo que hay en el cerco ya generado (para la prueba): el atril con el libro 30, el arcon, la escalera, la puerta... */
    public static String resumen(ServerLevel m) {
        Cerco a = get();
        int[] at = a.sitio("atril"), ar = a.sitio("arcon");
        boolean libro = m.getBlockEntity(new BlockPos(at[0], at[1], at[2])) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le
                && Perdidos.de(le.getBook()) == 30;
        boolean arcon = m.getBlockState(new BlockPos(ar[0], ar[1], ar[2])).is(Blocks.CHEST);
        int peldanos = 0;
        for (int k = 1; k <= 8; k++) if (m.getBlockState(new BlockPos(CX, PISO + k - 1, CZ + 12 - k)).is(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS)) peldanos++;
        boolean puerta = m.getBlockState(new BlockPos(CX, PISO + 2, CZ + 33)).isAir() && !m.getBlockState(new BlockPos(CX + 4, PISO + 2, CZ + 33)).isAir();
        boolean ancla = !m.getBlockState(new BlockPos(CX + 6, PISO + 2, CZ)).isAir();
        boolean pilon = m.getBlockState(new BlockPos(CX + 23, PISO, CZ - 1)).is(Blocks.WATER_CAULDRON);
        int libres = 0, total = 0;
        for (int u = -28; u <= 28; u += 2)
            for (int v = -28; v <= 28; v += 2) {
                if (r(u, v) > 30 || Math.max(Math.abs(u), Math.abs(v)) <= ANCLA + 1 || torre(u, v) >= 0) continue;
                total++;
                if (!m.getBlockState(new BlockPos(CX + u, SUELO, CZ + v)).isAir()) libres++;
            }
        return "atril con el libro 30 " + (libro ? "SI" : "NO") + ", arcon " + (arcon ? "SI" : "NO") + ", la escalera del ancla " + peldanos
                + "/8, la puerta " + (puerta ? "SI" : "NO") + ", el ancla " + (ancla ? "SI" : "NO") + ", el pilon " + (pilon ? "SI" : "NO")
                + ", suelo del patio " + libres + "/" + total;
    }
}
