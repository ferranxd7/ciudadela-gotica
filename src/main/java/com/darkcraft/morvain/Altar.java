package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Penitente;
import com.darkcraft.enemigos.Puestos;
import com.darkcraft.fortaleza.Perdidos;
import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Pal;
import com.darkcraft.pueblo.gen.Trozo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
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
 * El Altar Profanado (parte 7t), en la Tierra sin Fe, a la puerta del santuario del Altar —(-1864, -1600), al sur—: la basilica
 * donde Anselmo, el Pontifice Sangrante, llevo a los peregrinos a dar su sangre (libro 28: "se los lleva al noroeste, al Altar
 * de la Sangre"). u = x - {@link #CX}, v = z - {@link #CZ} (el norte, v negativa).
 * <p>
 * De sur a norte: el <b>camino</b> del santuario; el <b>atrio</b> enlosado con sus columnas rotas; la <b>fachada</b> con la
 * puerta grande apuntada, las dos pequenas y el rosetón roto, y el <b>campanario</b> medio caido a su derecha (con su campana);
 * la <b>nave</b> sin techo (se hundio: quedan los arcos que la cruzaban, las arcadas sobre los pilares y escombros), con los
 * bancos rotos en las naves de los lados y el <b>canal de sangre</b> por el medio; el <b>crucero</b> con sus dos brazos: en cada
 * uno una <b>pila</b> llena de sangre (de ella baja otro canal al del medio) y, al fondo, tras un muro con su puerta, la
 * <b>sacristia</b> al este (el atril con el <b>libro perdido 29</b> y el arcon) y el <b>osario</b> al oeste (huesos, calaveras
 * y velas); y el <b>abside</b> con el presbiterio alzado: el <b>altar de la sangre</b> (la pila del altar llena, calaveras y
 * velas rojas) y detras la gran cruz rota con sus cadenas. Fuera: el <b>campamento</b> de los peregrinos al este (tiendas, la
 * hoguera fria, barriles) y sus <b>tumbas</b> al oeste.
 * <p>
 * Los puestos los apunta el propio plano ({@link #esperas}: Anselmo de rodillas ante el altar —el puesto 0— y los penitentes
 * del atrio, la nave, el campamento, las tumbas y la sacristia). Se dibuja chunk a chunk al adornar
 * ({@link MorvainGenerador#applyBiomeDecoration}); en su caja {@link Adorno} no pone nada; la tarima del santuario no se toca.
 * La sangre es agua: en la Tierra sin Fe el agua es roja (el color de su bioma).
 */
public final class Altar {
    public static final long SEMILLA = 0xA17A_5A26_0E5AL;
    /** El centro del plano (el pie de la nave, junto al crucero). */
    public static final int CX = -1864, CZ = -1652;
    /** El suelo allanado (lo de dentro) y lo que se pisa. */
    public static final int SUELO = 114, PISO = SUELO + 1;
    /** El santuario del Altar. */
    static final int SANT_U = -1864 - CX, SANT_V = -1600 - CZ, SANT_R = 6;
    /** La nave: lo de dentro llega a |u| = NAVE_U (los muros, a 12 y 13), de v = -8 a la fachada (v 30 y 31). */
    static final int NAVE_U = 11, FACHADA_V = 30;
    /** El crucero: lo de dentro, de v = -19 a -9, hasta |u| = 22 (los muros del fondo, a 23 y 24). */
    static final int CRUCERO_V0 = -19, CRUCERO_V1 = -9, BRAZO_U = 22;
    /** El muro de la sacristia (al este) y del osario (al oeste), con su puerta en v = -14. */
    static final int CUARTO_U = 18;
    /** El abside: el centro (0, -20) y lo de dentro, hasta 11 (el muro, de 11,5 a 13,5). */
    static final int ABSIDE_V = -20;
    static final double ABSIDE_R = 11.5;
    /** El altar (v -25 a -23) y las pilas de los brazos (en |u| 13, v -14). */
    static final int ALTAR_V = -24, PILA_U = 13, PILA_V = -14;
    /** Los pilares de la nave (en |u| = 6) y donde estan. */
    static final int PILAR_U = 6;
    static final int[] PILARES_V = {26, 21, 16, 11, 6, 1, -4};
    /** El campanario: de u 15 a 20, de v 24 a 29. */
    static final int TORRE_U0 = 15, TORRE_U1 = 20, TORRE_V0 = 24, TORRE_V1 = 29;
    /** Lo allanado (y lo que se funde alrededor). */
    static final int U_LLANO = 33, V_LLANO0 = -37, V_LLANO1 = 47;
    static final double FUNDE = 8;
    public static final int X0 = CX - U_LLANO - 9, X1 = CX + U_LLANO + 9, Z0 = CZ + V_LLANO0 - 9, Z1 = CZ + V_LLANO1 + 9;
    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/altar"),
            BOTIN_BUENO = new ResourceLocation(DarkCraftMod.MODID, "chests/altar_bueno");
    private static final int[] LADOS = {-1, 1};
    private static final Ruido RUIDO = new Ruido(SEMILLA);

    private static final int AW = X1 - X0 + 1, AD = Z1 - Z0 + 1;
    private static final int[] ALTO = new int[AW * AD];

    static {
        Arrays.fill(ALTO, Integer.MIN_VALUE);
    }

    /** El bloque de arriba del terreno de Morvain en (x, z) (sin el altar). */
    public static int alto(int x, int z) {
        if (x < X0 || x > X1 || z < Z0 || z > Z1) return (int) Math.floor(Terreno.columna(x, z).altura());
        int i = (z - Z0) * AW + (x - X0);
        int v = ALTO[i];
        if (v == Integer.MIN_VALUE) ALTO[i] = v = (int) Math.floor(Terreno.columna(x, z).altura());
        return v;
    }

    /** Uno del altar: donde espera (y: lo que pisa), hacia donde mira, su variante, como espera, cuanto ronda y su grupo. */
    public record Espera(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    private static Altar unico;

    public static synchronized Altar get() {
        if (unico == null) unico = new Altar();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    /** Lo que hay (para la prueba y las vistas). */
    public int bancos, velas, tumbas, tiendas, escombros, arcos;

    private Altar() {
        elementos.add(new Terraza());
        elementos.add(new Basilica());
        Pilotes.Lista ba = new Pilotes.Lista();
        arcadas(ba);
        roseton(ba);
        campanario(ba);
        presbiterio(ba);
        elementos.add(ba.pieza(3));
        Pilotes.Lista de = new Pilotes.Lista();
        bancos(de);
        escombros(de);
        velas(de);
        cuartos(de);
        atrio(de);
        campamento(de);
        tumbas(de);
        elementos.add(de.pieza(4));
        trozos();
        // ---- los sitios con nombre
        sitio("entrada", 0, PISO, 44);
        sitio("atrio", 0, PISO, 38);
        sitio("puerta", 0, PISO, FACHADA_V + 3);
        sitio("nave", 0, PISO, 14);
        sitio("crucero", 0, PISO, -11);
        sitio("pontifice", 0, PISO, -17);
        sitio("altar", 0, PISO + 1, -21);
        sitio("pila_este", PILA_U, PISO, PILA_V + 3);
        sitio("pila_oeste", -PILA_U, PISO, PILA_V + 3);
        sitio("sacristia", 20, PISO, -14);
        sitio("atril", 21, PISO, -17);
        sitio("arcon", 21, PISO, -11);
        sitio("osario", -20, PISO, -14);
        sitio("campanario", 17, PISO, 21);
        sitio("campamento", 24, PISO, 37);
        sitio("tumbas", -24, PISO, 37);
        sitio("alto", 0, PISO + 26, 10);
        // ---- los puestos
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo del altar (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en lo del altar: ahi {@link Adorno} no pone nada. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= X0 && x0 <= X1 && z0 + 15 >= Z0 && z0 <= Z1;
    }

    /** Lo lejos de (x, z) al crucero. */
    public static double distancia(double x, double z) {
        return Math.hypot(x - CX - 0.5, z - (CZ - 12) - 0.5);
    }

    /** Si (x, z) esta en lo del altar (la basilica y lo de alrededor): para el nombre del sitio. */
    public static boolean dentro(double x, double z) {
        double u = x - CX, v = z - CZ;
        return Math.abs(u) <= U_LLANO && v >= V_LLANO0 && v <= V_LLANO1 && Math.hypot(u - SANT_U, v - SANT_V) > SANT_R;
    }

    /** Si (x, z) esta dentro de la basilica (la nave, el crucero y el abside; no los cuartos del fondo de los brazos). */
    public static boolean enNave(double x, double z) {
        int u = (int) Math.floor(x) - CX, v = (int) Math.floor(z) - CZ;
        return interior(u, v) && Math.abs(u) < CUARTO_U;
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} del altar en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Anselmo y los penitentes del altar (los apunta {@link MorvainEnemigos}); Anselmo es el primero. */
    public List<Espera> esperas() {
        return esperas;
    }

    /** Los sitios con nombre (la entrada, el atrio, la nave, el crucero, el altar, la sacristia...): {x, y, z} (y: lo que se pisa). */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    private void sitio(String nombre, int u, int y, int v) {
        sitios.put(nombre, new int[]{CX + u, y, CZ + v});
    }

    /** Donde salen los de la procesion (del canal de sangre de la nave, a los dos lados): {x, z}. */
    public static List<int[]> procesion() {
        List<int[]> l = new ArrayList<>();
        for (int v : new int[]{-2, 4, 9, 14, 19})
            for (int s : LADOS) l.add(new int[]{CX + 2 * s, CZ + v});
        return l;
    }

    // ------------------------------------------------------------------ el plano

    /** Si (u, v) es de dentro de la basilica (suelo con techo de cielo: la nave, el crucero con sus cuartos y el abside). */
    static boolean interior(int u, int v) {
        int a = Math.abs(u);
        if (a <= NAVE_U && v >= -8 && v < FACHADA_V) return true;
        if (a <= BRAZO_U && v >= CRUCERO_V0 && v <= CRUCERO_V1 && a != CUARTO_U) return true;
        return v <= ABSIDE_V && Math.hypot(u, v - ABSIDE_V) <= ABSIDE_R - 0.5 + 0.01;
    }

    /** Si (u, v) es muro de la basilica (dos de grueso; el de los cuartos, uno). */
    static boolean muro(int u, int v) {
        int a = Math.abs(u);
        if (interior(u, v)) return false;
        if (a >= 12 && a <= 13 && v >= -6 && v <= FACHADA_V + 1) return true;          // los de la nave
        if (v >= FACHADA_V && v <= FACHADA_V + 1 && a <= 13) return true;              // la fachada
        if (v >= -8 && v <= -7 && a >= 12 && a <= BRAZO_U + 2) return true;            // el sur de los brazos
        if (a >= BRAZO_U + 1 && a <= BRAZO_U + 2 && v >= -21 && v <= -7) return true;   // el fondo de los brazos
        if (v >= -21 && v <= -20 && a >= 12 && a <= BRAZO_U + 2) return true;          // el norte de los brazos
        if (a == CUARTO_U && v >= CRUCERO_V0 && v <= CRUCERO_V1) return true;          // los de los cuartos
        double r = Math.hypot(u, v - ABSIDE_V);
        return v <= ABSIDE_V && r > ABSIDE_R - 0.5 && r <= ABSIDE_R + 2.0;              // el abside
    }

    /** Lo alto del muro en (u, v) (el ultimo bloque): la nave y el abside 15; los brazos 14; la fachada sube al hastial. */
    static int altoMuro(int u, int v) {
        int a = Math.abs(u);
        if (v >= FACHADA_V && a <= 13) return PISO + 17 + (int) Math.round(7.0 * (1 - a / 13.0));
        if (a >= 14 && v <= -7) return PISO + 13;
        if (a == CUARTO_U) return PISO + 5;
        return PISO + 15;
    }

    /** Si en (u, y, v) hay un hueco del muro: las puertas y las ventanas (el roseton va aparte). */
    static boolean hueco(int u, int y, int v) {
        int a = Math.abs(u), h = y - PISO;
        if (v >= FACHADA_V) {
            // la puerta grande (apuntada) y las dos pequenas
            if (a <= 2 && h < 6) return true;
            if (h == 6 && a <= 1 || h == 7 && a == 0) return true;
            if (a >= 7 && a <= 9 && h < 4 || a == 8 && h == 4) return true;
            return false;
        }
        if (a == CUARTO_U) return v == PILA_V && h < 3;
        if (a >= 12 && a <= 13 && v >= -6) {
            // las ventanas de la nave: dos de ancho, de 6 a 11 (apuntadas arriba)
            for (int w : new int[]{3, 8, 13, 18, 23})
                if (v >= w && v <= w + 1 && h >= 6 && h <= 11) return true;
            return false;
        }
        if (a >= BRAZO_U + 1 && a <= BRAZO_U + 2) return (v == -16 || v == -12) && h >= 5 && h <= 10;
        double r = Math.hypot(u, v - ABSIDE_V);
        if (v <= ABSIDE_V && r > ABSIDE_R - 0.5) {
            // cinco ventanas altas y estrechas alrededor del abside
            double ang = Math.toDegrees(Math.atan2(u, ABSIDE_V - v));
            for (double w : new double[]{-60, -30, 0, 30, 60})
                if (Math.abs(ang - w) < 3.2 && h >= 5 && h <= 12) return true;
        }
        return false;
    }

    /** Si (u, v) es del presbiterio alzado (lo que se pisa, a PISO + 1). */
    static boolean presbiterio(int u, int v) {
        return v <= ABSIDE_V && interior(u, v);
    }

    /** Los canales de sangre: el del medio de la nave (u = 0) y el que viene de las pilas (v = -14). */
    static boolean canal(int u, int v) {
        if (u == 0 && v >= PILA_V && v <= FACHADA_V - 2) return true;
        return v == PILA_V && Math.abs(u) <= PILA_U - 3;
    }

    /** Si (u, v) es de una pila (3 x 3 de sangre con su brocal alrededor): 0 no, 1 el brocal, 2 la sangre. */
    static int pila(int u, int v) {
        int d = Math.max(Math.abs(Math.abs(u) - PILA_U), Math.abs(v - PILA_V));
        return d <= 1 ? 2 : d == 2 ? 1 : 0;
    }

    /** Si en (u, v) hay un pilar de la nave o del crucero. */
    static boolean pilar(int u, int v) {
        if (Math.abs(u) != PILAR_U) return false;
        if (v == -8 || v == ABSIDE_V) return true;
        for (int w : PILARES_V) if (v == w) return true;
        return false;
    }

    // ------------------------------------------------------------------ bloques

    static BlockState b(Block k) {
        return k.defaultBlockState();
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

    private static boolean junto(int u, int v, int r) {
        return Math.hypot(u - SANT_U, v - SANT_V) < r;
    }

    /** La piedra de los muros: ladrillo de pizarra, mucho agrietado, con piedra negra. */
    static BlockState piedra(int x, int y, int z) {
        // las impostas: una banda de piedra negra pulida a media altura y otra arriba
        if (y == PISO + 5 || y == PISO + 12) return Hash.pct(x, y, z, SEMILLA + 4) < 85 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS) : b(Blocks.CHISELED_POLISHED_BLACKSTONE);
        int p = Hash.pct(x, y, z, SEMILLA + 3);
        return p < 52 ? b(Blocks.DEEPSLATE_BRICKS) : p < 72 ? b(Blocks.CRACKED_DEEPSLATE_BRICKS) : p < 88 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS)
                : p < 96 ? b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS) : b(Blocks.DEEPSLATE_TILES);
    }

    /** El suelo de dentro: losas de pizarra con la sangre seca a manchas (mas cerca de los canales). */
    static BlockState losas(int u, int v, int p) {
        boolean cerca = Math.abs(u) <= 2 || Math.abs(v - PILA_V) <= 2 || presbiterio(u, v);
        if (p < (cerca ? 9 : 3)) return b(Blocks.NETHER_WART_BLOCK);
        if (Math.abs(u) == 1 && v > -9) return p < 70 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS) : b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
        return p < 60 ? b(Blocks.DEEPSLATE_TILES) : p < 82 ? b(Blocks.CRACKED_DEEPSLATE_TILES) : p < 94 ? b(Blocks.POLISHED_DEEPSLATE)
                : b(Blocks.POLISHED_BLACKSTONE);
    }

    /** La tierra de fuera: roja y agrietada (como la del Puente de la Grieta), con alguna cresta de piedra negra. */
    private static BlockState tierra(int u, int v, int p) {
        if (RUIDO.crestas(u + 300, v - 200, 34, 2) > 0.94) return p < 60 ? b(Blocks.BLACKSTONE) : Varadero.tronco(Blocks.BASALT, Direction.Axis.Y);
        double n = RUIDO.ruido(u / 11.0 + 5, v / 11.0 - 9);
        if (n > 0.3) return p < 85 ? b(Blocks.RED_SAND) : b(Blocks.COARSE_DIRT);
        if (n > -0.15) return p < 80 ? b(Blocks.COARSE_DIRT) : p < 92 ? b(Blocks.RED_SAND) : b(Blocks.GRAVEL);
        return p < 55 ? b(Blocks.SOUL_SOIL) : p < 90 ? b(Blocks.BROWN_TERRACOTTA) : b(Blocks.COARSE_DIRT);
    }

    static BlockState vela(int n, boolean roja) {
        return (roja ? Blocks.RED_CANDLE : Blocks.BLACK_CANDLE).defaultBlockState().setValue(CandleBlock.CANDLES, Math.max(1, Math.min(4, n)))
                .setValue(CandleBlock.LIT, true);
    }

    // ------------------------------------------------------------------ el suelo

    /** Lo alto del suelo en (x, z): lo allanado (y lo que se funde) o MIN_VALUE donde no se toca. */
    static int objetivo(int x, int z) {
        int u = x - CX, v = z - CZ;
        double du = Math.max(0, Math.abs(u) - U_LLANO);
        double dv = v < V_LLANO0 ? V_LLANO0 - v : v > V_LLANO1 ? v - V_LLANO1 : 0;
        double d = Math.hypot(du, dv);
        if (d <= 0) return SUELO;
        if (d >= FUNDE) return Integer.MIN_VALUE;
        double t = Ruido.suave(0, FUNDE, d);
        return (int) Math.round(Ruido.lerp(t, SUELO, alto(x, z)));
    }

    /** Lo que se pisa en (x, z) (lo allanado o el terreno). */
    static int pie(int x, int z) {
        int o = objetivo(x, z);
        return (o != Integer.MIN_VALUE ? o : alto(x, z)) + 1;
    }

    /** Si (u, v) cae en algo de fuera que lleva su propio suelo (el atrio, el camino). */
    static boolean enlosado(int u, int v) {
        if (Math.abs(u) <= 14 && v >= FACHADA_V + 2 && v <= 44) return true;
        return Math.abs(u) <= 1 && v > 44 && !junto(u, v, SantuariosMorvain.R + 1);
    }

    /**
     * La terraza: corta lo que sobra hasta el suelo y rellena por debajo; fuera, la tierra roja con matas secas, huesos y
     * piedras; el atrio y el camino, enlosados. La tarima del santuario no se toca.
     */
    private final class Terraza extends Element {
        Terraza() {
            super(SEMILLA + 51);
            this.layer = 1;
            bounds(X0, 60, Z0, X1, 160, Z1);
        }

        @Override
        public void build(Canvas c) {
            for (int x = Math.max(c.minX, x0); x <= Math.min(c.maxX, x1); x++)
                for (int z = Math.max(c.minZ, z0); z <= Math.min(c.maxZ, z1); z++) {
                    int u = x - CX, v = z - CZ;
                    if (junto(u, v, SANT_R)) continue;
                    int o = objetivo(x, z);
                    if (o == Integer.MIN_VALUE) continue;
                    int t = alto(x, z);
                    for (int y = o + 1; y <= Math.max(t, o) + 6; y++) c.set(x, y, z, Pal.AIR);
                    for (int y = t + 1; y < o; y++) c.set(x, y, z, y >= o - 2 ? b(Blocks.COARSE_DIRT) : b(Blocks.BLACKSTONE));
                    int p = Hash.pct(x, o, z, seed);
                    if (o == SUELO && enlosado(u, v)) {
                        // el atrio y el camino: losas de piedra negra rotas, con tierra donde faltan
                        c.set(x, o, z, p < 10 ? tierra(u, v, p) : p < 55 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS)
                                : p < 80 ? b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS) : b(Blocks.DEEPSLATE_TILES));
                        continue;
                    }
                    c.set(x, o, z, tierra(u, v, p));
                    if (o != SUELO || interior(u, v) || muro(u, v)) continue;
                    int q = Hash.pct(x, o + 1, z, seed + 1);
                    if (q < 2) c.set(x, o + 1, z, Pal.b(Blocks.DEAD_BUSH));
                    else if (q == 2) c.set(x, o + 1, z, Varadero.hueso(Direction.Axis.X));
                    else if (q == 3) c.set(x, o + 1, z, b(Blocks.BLACKSTONE));
                }
        }
    }

    /**
     * La basilica, columna a columna: el suelo de dentro (con los canales de sangre en losas inundadas, las pilas y el
     * presbiterio alzado con su escalon), los muros con sus puertas y ventanas, y los pilares.
     */
    private final class Basilica extends Element {
        Basilica() {
            super(SEMILLA + 61);
            this.layer = 2;
            bounds(CX - BRAZO_U - 2, SUELO - 3, CZ - 34, CX + BRAZO_U + 2, PISO + 26, CZ + FACHADA_V + 1);
        }

        @Override
        public void build(Canvas c) {
            for (int x = Math.max(c.minX, x0); x <= Math.min(c.maxX, x1); x++)
                for (int z = Math.max(c.minZ, z0); z <= Math.min(c.maxZ, z1); z++) {
                    int u = x - CX, v = z - CZ;
                    boolean dentro = interior(u, v), pared = muro(u, v);
                    if (!dentro && !pared) continue;
                    // los cimientos
                    for (int y = SUELO - 3; y < SUELO; y++) c.set(x, y, z, b(Blocks.DEEPSLATE));
                    if (pared) {
                        int top = altoMuro(u, v);
                        for (int y = SUELO; y <= top; y++) {
                            if (y >= PISO && hueco(u, y, v)) continue;
                            // lo alto de los muros, mellado (se hundio el tejado)
                            if (v < FACHADA_V && y > top - 3 && Hash.pct(x, y, z, seed + 7) < 18 * (y - (top - 3))) break;
                            c.set(x, y, z, y == SUELO ? b(Blocks.POLISHED_DEEPSLATE) : piedra(x, y, z));
                        }
                        // las vidrieras rotas de las ventanas (una de cada tres, entera)
                        for (int y = PISO + 5; y <= PISO + 12; y++)
                            if (v < FACHADA_V && hueco(u, y, v) && Hash.pct(x, y, z, seed + 9) < 34)
                                c.unir(x, y, z, b(Hash.pct(x, y, z, seed + 10) < 75 ? Blocks.RED_STAINED_GLASS_PANE : Blocks.BLACK_STAINED_GLASS_PANE));
                        continue;
                    }
                    int p = Hash.pct(x, SUELO, z, seed);
                    for (int y = PISO; y <= PISO + 26; y++) c.set(x, y, z, Pal.AIR);
                    if (canal(u, v)) {
                        c.set(x, SUELO, z, Pilotes.losa(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.BOTTOM, true));
                        continue;
                    }
                    c.set(x, SUELO, z, losas(u, v, p));
                    int pi = pila(u, v);
                    if (pi > 0 && v >= CRUCERO_V0 && v <= CRUCERO_V1 && Math.abs(u) < CUARTO_U) {
                        c.set(x, PISO, z, pi == 1 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS) : Pal.WATER);
                        if (pi == 2) c.set(x, SUELO, z, b(Blocks.POLISHED_BLACKSTONE));
                        continue;
                    }
                    if (presbiterio(u, v)) {
                        // el presbiterio, un escalon mas alto (el borde del sur, escalones)
                        boolean borde = v == ABSIDE_V;
                        c.set(x, PISO, z, borde && !pilar(u, v) ? Pilotes.escalera(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.NORTH, false, false)
                                : p < 12 ? b(Blocks.NETHER_WART_BLOCK) : p < 70 ? b(Blocks.POLISHED_BLACKSTONE) : b(Blocks.POLISHED_BLACKSTONE_BRICKS));
                        if (pilar(u, v)) for (int y = PISO + 1; y <= PISO + 10; y++) c.set(x, y, z, columna(u, y, v));
                        continue;
                    }
                    if (pilar(u, v)) for (int y = PISO; y <= PISO + 9; y++) c.set(x, y, z, columna(u, y, v));
                }
        }
    }

    /** Un pilar: la basa y el capitel de pizarra cincelada; el fuste de pizarra pulida (de pie). */
    static BlockState columna(int u, int y, int v) {
        int base = presbiterio(u, v) ? PISO + 1 : PISO;
        if (y == base || y == base + 8) return b(Blocks.CHISELED_DEEPSLATE);
        if (y > base + 8) return piedra(x(u), y, z(v));
        return Pilotes.tronco(Blocks.POLISHED_BASALT, Direction.Axis.Y);
    }

    // ------------------------------------------------------------------ lo de arriba: arcos, roseton, campanario, presbiterio

    /**
     * Los arcos: las arcadas apuntadas sobre los pilares de la nave (de uno al siguiente) y los arcos que cruzaban la nave
     * de pilar a pilar (casi todos rotos: el tejado se hundio).
     */
    private void arcadas(Pilotes.Lista m) {
        for (int s : LADOS) {
            for (int k = 0; k + 1 < PILARES_V.length; k++) {
                int va = PILARES_V[k + 1], vb = PILARES_V[k];
                for (int v = va + 1; v < vb; v++) {
                    double t = (v - va) / (double) (vb - va);
                    int h = 8 + (int) Math.round(2.4 * Math.sin(Math.PI * t));
                    for (int y = PISO + h; y <= PISO + 11; y++) put(m, s * PILAR_U, y, v, piedra(x(s * PILAR_U), y, z(v)));
                }
            }
            for (int v = -7; v < -4; v++) for (int y = PISO + 9; y <= PISO + 11; y++) put(m, s * PILAR_U, y, v, piedra(x(s * PILAR_U), y, z(v)));
        }
        // los arcos de traves: de un pilar al otro por encima de la nave, apuntados (de 10 en el arranque a 14 en la clave)
        for (int w : new int[]{21, 11, 1, -8}) {
            boolean roto = Hash.pct(w, 3, 7, SEMILLA + 17) < 45;
            int corte = roto ? -3 + Hash.pct(w, 5, 7, SEMILLA + 18) % 7 : 99;
            arcos++;
            for (int u = -PILAR_U + 1; u <= PILAR_U - 1; u++) {
                if (roto && Math.abs(u - corte) <= 1) continue;
                double t = 1 - Math.abs(u) / (double) PILAR_U;
                int h = 10 + (int) Math.round(4.0 * Math.sqrt(t));
                put(m, u, PISO + h, w, piedra(x(u), PISO + h, z(w)));
                put(m, u, PISO + h + 1, w, piedra(x(u), PISO + h + 1, z(w)));
            }
            for (int s : LADOS) for (int y = PISO + 10; y <= PISO + 11; y++) put(m, s * PILAR_U, y, w, piedra(x(s * PILAR_U), y, z(w)));
        }
        // el arco triunfal: del crucero al abside, sobre los pilares de su entrada
        for (int u = -PILAR_U; u <= PILAR_U; u++) {
            double t = 1 - Math.abs(u) / (double) PILAR_U;
            int h = 11 + (int) Math.round(4.0 * Math.sqrt(t));
            for (int y = PISO + h; y <= PISO + 16; y++) put(m, u, y, ABSIDE_V, piedra(x(u), y, z(ABSIDE_V)));
        }
        // los contrafuertes: por fuera de los muros de la nave (en cada pilar) y alrededor del abside (entre las ventanas)
        for (int s : LADOS)
            for (int w : PILARES_V) {
                if (s > 0 && w >= TORRE_V0 - 1 || w < 0) continue;
                for (int y = PISO; y <= PISO + 11; y++) put(m, s * 14, y, w, piedra(x(s * 14), y, z(w)));
                for (int y = PISO; y <= PISO + 6; y++) put(m, s * 15, y, w, piedra(x(s * 15), y, z(w)));
                put(m, s * 15, PISO + 7, w, Pilotes.escalera(Blocks.DEEPSLATE_BRICK_STAIRS, s > 0 ? Direction.WEST : Direction.EAST, false, false));
                put(m, s * 14, PISO + 12, w, Pilotes.escalera(Blocks.DEEPSLATE_BRICK_STAIRS, s > 0 ? Direction.WEST : Direction.EAST, false, false));
            }
        for (double ang : new double[]{-75, -45, -15, 15, 45, 75}) {
            double a = Math.toRadians(ang);
            for (int k = 0; k <= 1; k++) {
                double r = ABSIDE_R + 2.5 + k;
                int u = (int) Math.round(Math.sin(a) * r), v = ABSIDE_V - (int) Math.round(Math.cos(a) * r);
                int top = k == 0 ? 11 : 6;
                for (int y = PISO; y <= PISO + top; y++) put(m, u, y, v, piedra(x(u), y, z(v)));
            }
        }
        // los muros de encima de las arcadas (el triforio) en los extremos, donde quedan en pie
        for (int s : LADOS) for (int v = 22; v <= 28; v++) for (int y = PISO + 12; y <= PISO + 14; y++)
            if (Hash.pct(v, y, s, SEMILLA + 19) < 60) put(m, s * PILAR_U, y, v, piedra(x(s * PILAR_U), y, z(v)));
    }

    /** El roseton de la fachada: un circulo de radio 3,6 sobre la puerta, con su tracería y la vidriera rota. */
    private void roseton(Pilotes.Lista m) {
        int cy = PISO + 13;
        for (int u = -4; u <= 4; u++)
            for (int y = cy - 4; y <= cy + 4; y++) {
                double r = Math.hypot(u, y - cy);
                if (r > 3.7) continue;
                for (int v = FACHADA_V; v <= FACHADA_V + 1; v++) {
                    boolean traza = r > 3.0 || r < 0.8 || u == 0 || y == cy;
                    if (v == FACHADA_V + 1 || !traza) {
                        // la cara de fuera: hueco (la vidriera va en la de dentro)
                        if (v == FACHADA_V + 1) put(m, u, y, v, traza ? b(Blocks.POLISHED_DEEPSLATE) : Pal.AIR);
                        else if (Hash.pct(u, y, v, SEMILLA + 21) < 45)
                            une(m, u, y, v, b(Hash.pct(u, y, v, SEMILLA + 22) < 70 ? Blocks.RED_STAINED_GLASS_PANE : Blocks.BLACK_STAINED_GLASS_PANE));
                        else put(m, u, y, v, Pal.AIR);
                    } else put(m, u, y, v, b(Blocks.POLISHED_DEEPSLATE));
                }
            }
        // los pinaculos de las esquinas de la fachada
        for (int s : LADOS) {
            for (int y = PISO + 15; y <= PISO + 20; y++) put(m, s * 14, y, FACHADA_V + 1, piedra(x(s * 14), y, z(FACHADA_V + 1)));
            put(m, s * 14, PISO + 21, FACHADA_V + 1, b(Blocks.POLISHED_BLACKSTONE_WALL));
            for (int y = PISO; y <= PISO + 14; y++) put(m, s * 14, y, FACHADA_V + 1, piedra(x(s * 14), y, z(FACHADA_V + 1)));
            for (int y = PISO; y <= PISO + 8; y++) put(m, s * 14, y, FACHADA_V + 2, piedra(x(s * 14), y, z(FACHADA_V + 2)));
        }
        // la cruz de lo alto del hastial (torcida: le falta un brazo)
        int top = PISO + 24;
        for (int y = top + 1; y <= top + 4; y++) put(m, 0, y, FACHADA_V, b(Blocks.POLISHED_BLACKSTONE_WALL));
        put(m, -1, top + 3, FACHADA_V, b(Blocks.POLISHED_BLACKSTONE_WALL));
    }

    /**
     * El campanario, a la derecha de la fachada: una torre cuadrada de seis, medio caida (lo alto mellado), con los arcos
     * del campanil y la campana colgada de su viga. La puerta, tapiada de escombros.
     */
    private void campanario(Pilotes.Lista m) {
        int top = PISO + 30;
        for (int u = TORRE_U0; u <= TORRE_U1; u++)
            for (int v = TORRE_V0; v <= TORRE_V1; v++) {
                boolean borde = u == TORRE_U0 || u == TORRE_U1 || v == TORRE_V0 || v == TORRE_V1;
                put(m, u, SUELO, v, b(Blocks.POLISHED_DEEPSLATE));
                if (!borde) continue;
                int alto = top - Hash.pct(u, 1, v, SEMILLA + 31) % 9;
                boolean esquina = (u == TORRE_U0 || u == TORRE_U1) && (v == TORRE_V0 || v == TORRE_V1);
                for (int y = PISO; y <= alto; y++) {
                    int h = y - PISO;
                    // los arcos del campanil (dos de ancho en medio de cada cara) y las saeteras
                    boolean medio = (u == TORRE_U0 || u == TORRE_U1) ? (v == 26 || v == 27) : (u == 17 || u == 18);
                    if (!esquina && medio && h >= 20 && h <= 24) continue;
                    if (!esquina && medio && (h == 8 || h == 9 || h == 14 || h == 15)) continue;
                    put(m, u, y, v, esquina ? b(Blocks.POLISHED_DEEPSLATE) : piedra(x(u), y, z(v)));
                }
            }
        // la viga y la campana
        for (int u = TORRE_U0 + 1; u <= TORRE_U1 - 1; u++) put(m, u, PISO + 25, 26, Pilotes.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        put(m, 17, PISO + 24, 26, b(Blocks.BELL).setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING).setValue(BellBlock.FACING, Direction.NORTH));
        // la puerta, tapiada con escombros
        for (int y = PISO; y <= PISO + 2; y++) put(m, 17, y, TORRE_V0, y == PISO + 2 ? b(Blocks.COBBLED_DEEPSLATE) : b(Blocks.GRAVEL));
    }

    /**
     * El presbiterio: el altar de la sangre (piedra negra, la pila del altar llena de sangre, dos calaveras y las velas), la gran
     * cruz rota detras con sus cadenas (le falta un brazo, tirado en el suelo) y la sangre que baja del altar por el escalon.
     */
    private void presbiterio(Pilotes.Lista m) {
        int y = PISO + 1;
        for (int u = -2; u <= 2; u++)
            for (int v = ALTAR_V - 1; v <= ALTAR_V + 1; v++) {
                boolean frente = v == ALTAR_V + 1;
                put(m, u, y, v, frente && u == 0 ? b(Blocks.CHISELED_POLISHED_BLACKSTONE) : frente ? b(Blocks.POLISHED_BLACKSTONE_BRICKS)
                        : b(Blocks.POLISHED_BLACKSTONE));
            }
        // encima: la pila del altar (sangre), las calaveras y las velas rojas
        put(m, 0, y + 1, ALTAR_V, b(Blocks.WATER_CAULDRON).setValue(LayeredCauldronBlock.LEVEL, 3));
        put(m, -2, y + 1, ALTAR_V, b(Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, 8));
        put(m, 2, y + 1, ALTAR_V, b(Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, 8));
        put(m, -1, y + 1, ALTAR_V + 1, vela(4, true));
        put(m, 1, y + 1, ALTAR_V + 1, vela(3, true));
        put(m, -1, y + 1, ALTAR_V - 1, vela(2, true));
        put(m, 1, y + 1, ALTAR_V - 1, vela(4, true));
        put(m, -2, y + 1, ALTAR_V + 1, vela(1, false));
        put(m, 2, y + 1, ALTAR_V - 1, vela(2, false));
        velas += 6;
        // la gran cruz contra el abside: el madero y el travesano (sin el brazo derecho, que esta tirado al pie)
        int vc = ABSIDE_V - 10;
        for (int h = 0; h <= 13; h++) put(m, 0, y + h, vc, Pilotes.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
        for (int u = -4; u <= 2; u++) if (u != 0) put(m, u, y + 9, vc, Pilotes.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        for (int u = 3; u <= 5; u++) put(m, u, y, vc + 2, Pilotes.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        // las cadenas que cuelgan del travesano, y las velas negras al pie
        for (int h = 5; h <= 8; h++) put(m, -4, y + h, vc, b(Blocks.CHAIN));
        for (int h = 6; h <= 8; h++) put(m, 2, y + h, vc, b(Blocks.CHAIN));
        put(m, -1, y, vc + 1, vela(3, false));
        put(m, 1, y, vc + 1, vela(2, true));
        velas += 2;
        // la sangre que baja del altar: manchas en el presbiterio y en el escalon
        for (int v = ALTAR_V + 2; v <= ABSIDE_V; v++)
            for (int u = -1; u <= 1; u++)
                if (Hash.pct(u, 9, v, SEMILLA + 41) < 55) put(m, u, y, v, b(Blocks.REDSTONE_WIRE));
    }

    // ------------------------------------------------------------------ lo de dentro: bancos, escombros, velas, cuartos

    /** Los bancos rotos (escaleras de roble oscuro mirando al altar) en las naves de los lados y al fondo de la del medio. */
    private void bancos(Pilotes.Lista m) {
        for (int s : LADOS)
            for (int v : new int[]{25, 22, 19, 14, 9, 4}) {
                for (int a = 7; a <= 10; a++) {
                    int p = Hash.pct(s * a, 2, v, SEMILLA + 51);
                    if (p < 22) continue;
                    boolean volcado = p > 90;
                    put(m, s * a, PISO, v, Pilotes.escalera(Blocks.DARK_OAK_STAIRS, volcado ? Direction.NORTH : Direction.SOUTH, volcado, false));
                    bancos++;
                }
            }
        for (int s : LADOS)
            for (int v : new int[]{27, 24}) {
                for (int a = 2; a <= 4; a++) {
                    if (Hash.pct(s * a, 4, v, SEMILLA + 52) < 30) continue;
                    put(m, s * a, PISO, v, Pilotes.escalera(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, false, false));
                    bancos++;
                }
            }
    }

    /** Los escombros del tejado hundido: montones contra los muros de las naves de los lados y del crucero, y trozos sueltos. */
    private void escombros(Pilotes.Lista m) {
        int[][] montones = {{-10, 12}, {10, 6}, {-10, -1}, {9, 26}, {-18, -10}, {16, -18}, {-14, -18}, {11, -11}};
        for (int[] mo : montones) {
            escombros++;
            for (int du = -2; du <= 2; du++)
                for (int dv = -2; dv <= 2; dv++) {
                    int u = mo[0] + du, v = mo[1] + dv;
                    if (!interior(u, v) || canal(u, v) || pila(u, v) > 0 && Math.abs(u) < CUARTO_U || Math.abs(u) <= 2) continue;
                    double d = Math.hypot(du, dv);
                    int p = Hash.pct(u, 6, v, SEMILLA + 61);
                    if (d > 2.4 || p < 25) continue;
                    BlockState s = p < 45 ? b(Blocks.COBBLED_DEEPSLATE) : p < 65 ? b(Blocks.GRAVEL) : p < 85 ? piedra(x(u), PISO, z(v))
                            : Pilotes.losa(Blocks.DEEPSLATE_BRICK_SLAB, SlabType.BOTTOM, false);
                    put(m, u, PISO, v, s);
                    if (d < 1.2 && p > 50) put(m, u, PISO + 1, v, Pilotes.losa(Blocks.COBBLED_DEEPSLATE_SLAB, SlabType.BOTTOM, false));
                }
        }
    }

    /** Las velas: a los pies de los pilares, en los brocales de las pilas y en el escalon del presbiterio. */
    private void velas(Pilotes.Lista m) {
        for (int s : LADOS) {
            for (int w : PILARES_V) {
                int p = Hash.pct(s, 7, w, SEMILLA + 71);
                if (p < 40) continue;
                put(m, s * (PILAR_U - 1), PISO, w, vela(1 + p % 4, p % 3 != 0));
                velas++;
            }
            // las cuatro esquinas del brocal de cada pila
            for (int du : LADOS)
                for (int dv : LADOS) {
                    put(m, s * PILA_U + 2 * du, PISO + 1, PILA_V + 2 * dv, vela(2 + ((du + dv) & 1), true));
                    velas++;
                }
            // el escalon del presbiterio
            for (int a : new int[]{3, 5}) {
                put(m, s * a, PISO + 1, ABSIDE_V - 1, vela(a == 3 ? 4 : 2, true));
                velas++;
            }
        }
    }

    /**
     * Los cuartos del fondo de los brazos: la sacristia al este (los armarios —estanterias—, la mesa de los ornamentos, el
     * atril y el arcon los pone {@link #trozos}) y el osario al oeste (huesos apilados, calaveras y velas negras).
     */
    private void cuartos(Pilotes.Lista m) {
        // la sacristia: estanterias contra el fondo y la mesa
        for (int v = -18; v <= -10; v++)
            if (v != PILA_V && Hash.pct(v, 1, 3, SEMILLA + 81) < 75)
                for (int y = PISO; y <= PISO + 2; y++) put(m, BRAZO_U, y, v, b(Blocks.BOOKSHELF));
        put(m, 20, PISO, -19, Pilotes.losa(Blocks.DARK_OAK_SLAB, SlabType.TOP, false));
        put(m, 19, PISO, -19, Pilotes.losa(Blocks.DARK_OAK_SLAB, SlabType.TOP, false));
        put(m, 19, PISO + 1, -19, vela(2, true));
        velas++;
        // el osario: los huesos de los peregrinos amontonados contra las paredes, calaveras y velas negras
        for (int u = -BRAZO_U; u <= -CUARTO_U - 1; u++)
            for (int v = CRUCERO_V0; v <= CRUCERO_V1; v++) {
                int pared = Math.min(Math.min(u + BRAZO_U, v - CRUCERO_V0), CRUCERO_V1 - v);
                if (pared > 1 || v == PILA_V && u >= -CUARTO_U - 2) continue;
                int p = Hash.pct(u, 8, v, SEMILLA + 82);
                int alto = pared == 0 ? 1 + p % 3 : p < 50 ? 1 : 0;
                for (int k = 0; k < alto; k++) put(m, u, PISO + k, v, Varadero.hueso(k % 2 == 0 ? Direction.Axis.Y : Direction.Axis.X));
                if (alto > 0 && p > 70) put(m, u, PISO + alto, v, b(Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, p % 16));
                else if (alto > 0 && p < 12) {
                    put(m, u, PISO + alto, v, vela(1 + p % 3, false));
                    velas++;
                }
            }
    }

    /** El atrio: las columnas rotas a los lados y dos basas caidas. */
    private void atrio(Pilotes.Lista m) {
        for (int s : LADOS)
            for (int v : new int[]{35, 40}) {
                int alto = 3 + Hash.pct(s, 2, v, SEMILLA + 91) % 5;
                put(m, s * 13, PISO, v, b(Blocks.CHISELED_DEEPSLATE));
                for (int y = PISO + 1; y < PISO + alto; y++) put(m, s * 13, y, v, b(Blocks.POLISHED_DEEPSLATE));
                if (alto < 6) {
                    // el trozo de fuste caido al lado
                    for (int k = 1; k <= 3; k++) put(m, s * (13 - k), PISO, v + 1, Pilotes.tronco(Blocks.POLISHED_BASALT, Direction.Axis.X));
                }
            }
    }

    /**
     * El ultimo campamento de los peregrinos, al este del atrio: tres tiendas (lona sucia con manchas de sangre sobre sus
     * palos), la hoguera fria con sus troncos y los barriles.
     */
    private void campamento(Pilotes.Lista m) {
        int[][] tiendas_ = {{22, 34, 0}, {29, 38, 1}, {21, 44, 0}};
        for (int[] t : tiendas_) {
            tiendas++;
            tienda(m, t[0], t[1], t[2] == 1);
        }
        put(m, 25, PISO, 39, b(Blocks.CAMPFIRE).setValue(CampfireBlock.LIT, false));
        for (int[] d : new int[][]{{24, 37}, {27, 40}, {23, 41}}) put(m, d[0], PISO, d[1], Pilotes.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.Z));
        put(m, 30, PISO + 1, 44, b(Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, 3));
        put(m, 30, PISO, 44, Varadero.hueso(Direction.Axis.Y));
    }

    /** Una tienda: dos paredes de lona inclinadas (escalones y losas de lana no hay: lana) sobre su palo, abierta por delante. */
    private void tienda(Pilotes.Lista m, int u0, int v0, boolean girada) {
        for (int k = -2; k <= 2; k++)
            for (int l = 0; l <= 3; l++) {
                int u = girada ? u0 + l : u0 + k, v = girada ? v0 + k : v0 + l;
                int h = 2 - Math.abs(k);
                if (h < 0) continue;
                int p = Hash.pct(u, h, v, SEMILLA + 101);
                BlockState lona = p < 12 ? b(Blocks.RED_WOOL) : p < 70 ? b(Blocks.WHITE_WOOL) : b(Blocks.LIGHT_GRAY_WOOL);
                if (Math.abs(k) == 2) put(m, u, PISO, v, lona);
                else if (Math.abs(k) == 1) put(m, u, PISO + 1, v, lona);
                else put(m, u, PISO + 2, v, Pilotes.tronco(Blocks.DARK_OAK_LOG, girada ? Direction.Axis.X : Direction.Axis.Z));
            }
    }

    /** Las tumbas de los peregrinos, al oeste del atrio: en filas, un monticulo de tierra y su cruz de palos. */
    private void tumbas(Pilotes.Lista m) {
        for (int u = -30; u <= -18; u += 3)
            for (int v = 32; v <= 46; v += 4) {
                if (Hash.pct(u, 3, v, SEMILLA + 111) < 15) continue;
                tumbas++;
                put(m, u, PISO, v, b(Blocks.COARSE_DIRT));
                put(m, u, PISO, v + 1, b(Blocks.ROOTED_DIRT));
                int p = Hash.pct(u, 4, v, SEMILLA + 112);
                if (p < 20) continue;    // la cruz caida
                une(m, u, PISO, v - 1, b(Blocks.DARK_OAK_FENCE));
                une(m, u, PISO + 1, v - 1, b(Blocks.DARK_OAK_FENCE));
                une(m, u - 1, PISO + 1, v - 1, b(Blocks.DARK_OAK_FENCE));
                une(m, u + 1, PISO + 1, v - 1, b(Blocks.DARK_OAK_FENCE));
                une(m, u, PISO + 2, v - 1, b(Blocks.DARK_OAK_FENCE));
                if (p > 85) put(m, u, PISO + 1, v, vela(1, true));
            }
    }

    private void trozos() {
        // en la sacristia: el atril de cara a la puerta (al oeste) y el arcon junto a la entrada
        trozo(21, PISO, -17, c -> c.lectern(x(21), PISO, z(-17), Direction.WEST, Perdidos.libro(29)));
        trozo(21, PISO, -11, c -> c.chest(x(21), PISO, z(-11), Direction.WEST, BOTIN_BUENO));
        // en el osario, un arcon entre los huesos; en el campamento, dos barriles
        trozo(-21, PISO, -12, c -> c.chest(x(-21), PISO, z(-12), Direction.EAST, BOTIN));
        trozo(30, PISO, 35, c -> c.barrel(x(30), PISO, z(35), Direction.UP, BOTIN));
        trozo(19, PISO, 46, c -> c.barrel(x(19), PISO, z(46), Direction.UP, BOTIN));
    }

    private void trozo(int u, int y, int v, Trozo.Obra o) {
        elementos.add(new Trozo(6, x(u), y, z(v), x(u), y, z(v), o));
    }

    // ------------------------------------------------------------------ Anselmo y los penitentes

    private static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;

    private void ponerEsperas() {
        int F = Penitente.FLAGELANTE, P = Penitente.PORTADOR, T = Penitente.TURIFERARIO;
        int R = Enemigo.REZA, D = Enemigo.DE_PIE, Tu = Enemigo.TUMBADO;
        // Anselmo, de rodillas ante el altar (de espaldas a quien entra): el puesto 0
        en(0, 0, PISO, -17, NORTE, R, 0, Puestos.JEFE);
        // en la nave, los que rezan en los bancos de atras (lejos de el: no le despiertan) y uno de pie junto a la puerta
        en(F, -8, PISO, 23, NORTE, R, 0, "nave");
        en(F, 9, PISO, 20, NORTE, R, 0, "nave");
        en(P, 8, PISO, 12, NORTE, R, 0, "nave");
        en(T, -3, PISO, 26, SUR, D, 2, "nave");
        // en el atrio
        en(F, -6, PISO, 38, SUR, D, 4, "atrio");
        en(P, 6, PISO, 40, SUR, D, 0, "atrio");
        en(T, 0, PISO, 34, NORTE, R, 0, "atrio");
        // el campamento
        en(F, 23, PISO, 38, OESTE, Tu, 0, "campamento");
        en(P, 26, PISO, 36, SUR, R, 0, "campamento");
        en(F, 28, PISO, 43, OESTE, D, 3, "campamento");
        // las tumbas
        en(F, -24, PISO, 38, SUR, R, 0, "tumbas");
        en(P, -25, PISO, 42, ESTE, Tu, 0, "tumbas");
        // la sacristia: el que guarda el libro
        en(T, 20, PISO, -13, OESTE, D, 0, "sacristia");
    }

    /** El hacia donde mira de Minecraft (0 = sur, 90 = oeste) para una direccion (du, dv). */
    static float giro(double du, double dv) {
        return (float) (Math.toDegrees(Math.atan2(dv, du)) - 90.0);
    }

    private void en(int variante, int u, int y, int v, float giro, int postura, int ronda, String grupo) {
        esperas.add(new Espera(variante, x(u), y, z(v), giro, postura, ronda, grupo));
    }

    // ------------------------------------------------------------------ al caer Anselmo

    /** Para las pruebas: los penitentes que se han deshecho al caer Anselmo. */
    public static int DESCANSAN;

    /** Si Anselmo ha caido (y no ha vuelto). */
    public static boolean enPaz(net.minecraft.server.MinecraftServer srv) {
        return Puestos.muerto(srv, MorvainEnemigos.ALTAR, MorvainEnemigos.pontifice());
    }

    /**
     * Cada segundo: con Anselmo caido, la Tierra sin Fe descansa: los penitentes que seguian rezando de rodillas (en el Puente de
     * la Grieta y en el altar; no los que pelean) se deshacen en sangre y ya no vuelven.
     */
    public static void paz(ServerLevel m) {
        if (!enPaz(m.getServer())) return;
        descansar(m, MorvainEnemigos.GRIETA, MorvainEnemigos.grieta());
        descansar(m, MorvainEnemigos.ALTAR, MorvainEnemigos.altar());
    }

    private static void descansar(ServerLevel m, String sitio, List<Puestos.Puesto> puestos) {
        if (Puestos.sitio(sitio) == null) return;
        for (int i = 0; i < puestos.size(); i++) {
            Puestos.Puesto p = puestos.get(i);
            if (p.postura() != Enemigo.REZA || Puestos.JEFE.equals(p.grupo())) continue;
            Enemigo e = Puestos.enemigo(sitio, i);
            if (e == null || e.level() != m || e.getTarget() != null || e.accion() != 0) continue;
            m.sendParticles(com.darkcraft.enemigos.Sangrado.CHORRO, e.getX(), e.getY() + 1.0, e.getZ(), 30, 0.4, 0.8, 0.4, 0.0);
            m.sendParticles(ParticleTypes.SOUL, e.getX(), e.getY() + 1.2, e.getZ(), 10, 0.3, 0.6, 0.3, 0.03);
            e.disolver();
            DESCANSAN++;
        }
    }

    // ------------------------------------------------------------------ para la prueba

    /** Lo que hay en el altar ya generado (para la prueba): el atril con el libro 29, el arcon, la pila, los canales... */
    public static String resumen(ServerLevel m) {
        Altar a = get();
        int[] at = a.sitio("atril"), ar = a.sitio("arcon");
        boolean libro = m.getBlockEntity(new BlockPos(at[0], at[1], at[2])) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le
                && Perdidos.de(le.getBook()) == 29;
        boolean arcon = m.getBlockState(new BlockPos(ar[0], ar[1], ar[2])).is(Blocks.CHEST);
        boolean pila = m.getBlockState(new BlockPos(CX, PISO + 2, CZ + ALTAR_V)).is(Blocks.WATER_CAULDRON);
        int canales = 0, sangre = 0;
        for (int v = PILA_V; v <= FACHADA_V - 2; v += 4) {
            BlockState q = m.getBlockState(new BlockPos(CX, SUELO, CZ + v));
            if (q.is(Blocks.POLISHED_BLACKSTONE_SLAB)) canales++;
            if (q.getFluidState().is(net.minecraft.tags.FluidTags.WATER)) sangre++;
        }
        int pilas = 0;
        for (int s : LADOS) if (m.getBlockState(new BlockPos(CX + s * PILA_U, PISO, CZ + PILA_V)).getFluidState().is(net.minecraft.tags.FluidTags.WATER)) pilas++;
        int libres = 0, total = 0;
        for (int u = -5; u <= 5; u++)
            for (int v = -18; v <= 20; v += 2) {
                if (canal(u, v) || pilar(u, v)) continue;
                total++;
                if (m.getBlockState(new BlockPos(CX + u, PISO, CZ + v)).isAir() && m.getBlockState(new BlockPos(CX + u, PISO + 1, CZ + v)).isAir()) libres++;
            }
        boolean campana = m.getBlockState(new BlockPos(CX + 17, PISO + 24, CZ + 26)).is(Blocks.BELL);
        boolean puerta = m.getBlockState(new BlockPos(CX, PISO + 3, CZ + FACHADA_V)).isAir()
                && !m.getBlockState(new BlockPos(CX + 4, PISO + 3, CZ + FACHADA_V)).isAir();
        return "atril con el libro 29 " + (libro ? "SI" : "NO") + ", arcon " + (arcon ? "SI" : "NO") + ", la pila del altar " + (pila ? "SI" : "NO")
                + ", el canal " + canales + "/11 (con sangre " + sangre + "), pilas con sangre " + pilas + "/2, la puerta " + (puerta ? "SI" : "NO")
                + ", la campana " + (campana ? "SI" : "NO") + ", suelo de la nave y el crucero libre " + libres + "/" + total;
    }
}
