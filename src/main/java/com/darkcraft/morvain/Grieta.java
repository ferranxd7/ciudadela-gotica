package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Penitente;
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
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallBannerBlock;
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
 * El Puente de la Grieta (parte 7s), en la Tierra sin Fe, a la puerta del santuario de la Grieta —(24, 2181), al suroeste—: la
 * ultima travesia de los peregrinos que iban al crater a pedir perdon. u = x - {@link #CX}, v = z - {@link #CZ}; el norte (v < 0)
 * es el lado del crater.
 * <p>
 * La <b>grieta</b> ({@link #hueco}): una sima de oeste a este que baja hasta la sangre ({@link #FONDO}: el agua roja de la Tierra
 * sin Fe), de paredes en vetas de terracota, piedra negra y basalto que se estrechan al bajar, con salientes, huesos de los que
 * cayeron y enredaderas rojas colgando; se cierra poco a poco en sus dos puntas. El <b>puente</b> la cruza de sur a norte
 * (siete de ancho, sobre un arco de piedra negra, con sus pretiles, velas rojas y faroles) y en medio tiene su <b>espadana</b>
 * con la campana que tocaban por cada peregrino que cruzaba. Al sur: el <b>camino</b> que viene del santuario, la
 * <b>hospederia</b> de los peregrinos (el refectorio y la capilla, con el atril del libro perdido 28 y el arcon) y el
 * <b>campamento</b> de los penitentes (tiendas, la hoguera, los postes de los azotes, la olla, la cruz de guia y estandartes).
 * Al norte: la <b>puerta</b> (dos torres y el arco con la reja medio alzada), el <b>paso</b> abandonado en mitad del camino (la
 * imagen tapada con su manto morado, sus velas y sus varales), el <b>cementerio</b> de los peregrinos, el <b>humilladero</b> con
 * su cruz y, al este, lo que queda del <b>puente viejo</b>, el de cuerdas, colgando de las dos orillas.
 * <p>
 * Los puestos los apunta el propio plano ({@link #esperas}). Se dibuja chunk a chunk al adornar
 * ({@link MorvainGenerador#applyBiomeDecoration}); en su caja {@link Adorno} no pone nada; la tarima del santuario no se toca.
 */
public final class Grieta {
    public static final long SEMILLA = 0x6E1E_7A5F_0B1DL;
    /** El centro: donde el puente cruza la grieta. */
    public static final int CX = 135, CZ = 2095;
    /** El suelo allanado y lo que se pisa. */
    public static final int SUELO = 122, PISO = SUELO + 1;
    /** El fondo de la grieta (bajo la sangre) y lo alto de la sangre. */
    public static final int FONDO = 56, SANGRE = Terreno.AGUA;
    /** Donde empieza y acaba la grieta (u) y lo que se estrecha al bajar (bloques de ancho por bloque de hondo, a cada lado). */
    public static final int G_U0 = -62, G_U1 = 102;
    static final double PEND = 0.12;
    /** El puente: de punta a punta (v) y su medio ancho con los pretiles (u). */
    public static final int PUENTE_V0 = -19, PUENTE_V1 = 19, PUENTE_U = 3;
    /** La puerta del norte: su muro (v -25..-23) y sus torres (u 6..10, v -26..-22). */
    public static final int PUERTA_V = -24;
    /** La hospederia (los muros de fuera) y el muro que separa el refectorio de la capilla. */
    public static final int HOSP_U0 = -46, HOSP_U1 = -18, HOSP_V0 = 16, HOSP_V1 = 28, HOSP_TABIQUE = -27;
    /** El santuario de la Grieta. */
    static final int SANT_U = 24 - CX, SANT_V = 2181 - CZ, SANT_R = 12;
    /** Lo allanado (y lo que se funde con el terreno alrededor). */
    static final int U_LLANO0 = -52, U_LLANO1 = 92, V_LLANO0 = -62, V_LLANO1 = 50;
    static final double FUNDE = 8;
    /** El camino: del santuario a la cabeza sur del puente (u, v). */
    static final double[][] CAMINO = {{SANT_U, SANT_V}, {-90, 65}, {-62, 46}, {-35, 40}, {-12, 36}, {0, 26}, {0, 19}};
    public static final int X0 = CX + SANT_U - 6, X1 = CX + G_U1 + 6, Z0 = CZ + V_LLANO0 - 18, Z1 = CZ + SANT_V + 4;
    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/grieta"),
            BOTIN_BUENO = new ResourceLocation(DarkCraftMod.MODID, "chests/grieta_bueno");
    private static final int[] LADOS = {-1, 1};
    private static final Ruido RUIDO = new Ruido(SEMILLA);

    private static final int AW = X1 - X0 + 1, AD = Z1 - Z0 + 1;
    private static final int[] ALTO = new int[AW * AD];

    static {
        Arrays.fill(ALTO, Integer.MIN_VALUE);
    }

    /** El bloque de arriba del terreno de Morvain en (x, z) (sin la grieta). */
    public static int alto(int x, int z) {
        if (x < X0 || x > X1 || z < Z0 || z > Z1) return (int) Math.floor(Terreno.columna(x, z).altura());
        int i = (z - Z0) * AW + (x - X0);
        int v = ALTO[i];
        if (v == Integer.MIN_VALUE) ALTO[i] = v = (int) Math.floor(Terreno.columna(x, z).altura());
        return v;
    }

    /** Uno de la grieta: donde espera (y: lo que pisa), hacia donde mira, su variante, como espera, cuanto ronda y su grupo. */
    public record Espera(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    private static Grieta unico;

    public static synchronized Grieta get() {
        if (unico == null) unico = new Grieta();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    /** Lo que hay (para la prueba y las vistas). */
    public int tumbas, cruces, tiendas, postes, velas;

    private Grieta() {
        elementos.add(new Terraza());
        elementos.add(new Sima());
        elementos.add(new Camino());
        Pilotes.Lista pu = new Pilotes.Lista();
        puente(pu);
        puenteViejo(pu);
        elementos.add(pu.pieza(3));
        Pilotes.Lista no = new Pilotes.Lista();
        puerta(no);
        cementerio(no);
        humilladero(no);
        elementos.add(no.pieza(3));
        Pilotes.Lista pa = new Pilotes.Lista();
        paso(pa);
        elementos.add(pa.pieza(4));
        Pilotes.Lista su = new Pilotes.Lista();
        hospederia(su);
        campamento(su);
        cruzDeTermino(su);
        elementos.add(su.pieza(3));
        trozos();
        // ---- los sitios con nombre
        int[] en = puntoCamino(16);
        sitio("entrada", en[0], pie(CX + en[0], CZ + en[1]), en[1]);
        sitio("camino", -35, PISO, 40);
        sitio("hospederia", -36, PISO, 31);
        sitio("refectorio", -38, PISO, 25);
        sitio("capilla", -23, PISO, 20);
        sitio("atril", -20, PISO, 25);
        sitio("arcon", -20, PISO, 19);
        sitio("campamento", 20, PISO, 28);
        sitio("hoguera", 30, PISO, 28);
        sitio("postes", 15, PISO, 31);
        sitio("cruz", 40, PISO, 37);
        sitio("puente_sur", 0, PISO, 17);
        sitio("puente", 0, PISO, 5);
        sitio("campana", 0, PISO + 7, 0);
        sitio("puente_norte", 0, PISO, -17);
        sitio("puerta", 0, PISO, -24);
        sitio("paso", 0, PISO, -31);
        sitio("cementerio", -14, PISO, -45);
        sitio("humilladero", 19, PISO + 1, -36);
        sitio("puente_viejo", 60, PISO, (int) Math.floor(centro(60) - ancho(60)) - 3);
        sitio("alto", 44, PISO + 34, 52);
        sitio("fondo", 30, SANGRE + 1, (int) Math.round(centro(30)));
        // ---- los puestos
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo de la grieta (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /**
     * Si el chunk que empieza en (x0, z0) cae en lo allanado o en la grieta: ahi {@link Adorno} no pone nada (en el camino del
     * santuario si: el camino quita lo que le caiga encima).
     */
    static boolean cerca(int x0, int z0) {
        int a0 = CX + Math.min(U_LLANO0, G_U0) - (int) FUNDE - 2, a1 = CX + Math.max(U_LLANO1, G_U1) + (int) FUNDE + 2;
        int b0 = CZ + V_LLANO0 - (int) FUNDE - 2, b1 = CZ + V_LLANO1 + (int) FUNDE + 2;
        return x0 + 15 >= a0 && x0 <= a1 && z0 + 15 >= b0 && z0 <= b1;
    }

    /** Lo lejos de (x, z) al medio del puente. */
    public static double distancia(double x, double z) {
        return Math.hypot(x - CX - 0.5, z - CZ - 0.5);
    }

    /** Si (x, z) esta en lo de la grieta (lo allanado, el puente y lo de alrededor): para el nombre del sitio. */
    public static boolean dentro(double x, double z) {
        double u = x - CX, v = z - CZ;
        return u >= U_LLANO0 - 4 && u <= U_LLANO1 + 4 && v >= V_LLANO0 - 4 && v <= V_LLANO1 + 4;
    }

    /** Si (x, z) esta en el medio del puente (sobre lo mas hondo): para el logro. */
    public static boolean enPuente(double x, double z) {
        double u = x - CX - 0.5, v = z - CZ - 0.5;
        return Math.abs(u) <= PUENTE_U && Math.abs(v) <= 4.5;
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} de la grieta en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los penitentes de la grieta (los apunta {@link MorvainEnemigos}). */
    public List<Espera> esperas() {
        return esperas;
    }

    /** Los sitios con nombre (la entrada, el puente, la campana, la hospederia, el atril...): {x, y, z} (y: lo que se pisa). */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    private void sitio(String nombre, int u, int y, int v) {
        sitios.put(nombre, new int[]{CX + u, y, CZ + v});
    }

    // ------------------------------------------------------------------ la grieta

    /** Por donde va la grieta en u (la v de su medio). */
    public static double centro(double u) {
        return 4 * Math.sin(u / 23.0) + 2 * Math.sin(u / 9.0);
    }

    /** Lo que se abre la grieta arriba (de su medio a cada orilla) en u: se cierra poco a poco en las dos puntas. */
    public static double ancho(double u) {
        if (u <= G_U0 || u >= G_U1) return 0;
        double w = 11 + 2 * Math.sin(u / 17.0);
        return w * Ruido.suave(G_U0, G_U0 + 28, u) * Ruido.suave(G_U1, G_U1 - 30, u);
    }

    /** Lo que se abre la grieta en u a la altura y, del lado s (-1 el norte, 1 el sur): se estrecha al bajar, con salientes. */
    static double media(double u, int y, int s) {
        double w = ancho(u);
        if (w <= 0) return -1;
        double h = SUELO - y;
        double n = RUIDO.ruido(u / 8.0 + (s > 0 ? 40 : 0), y / 5.0) * 1.5 + RUIDO.ruido(u / 3.0 + 7, y / 2.5 + (s > 0 ? 19 : 0)) * 0.5;
        return w - PEND * h + n * Math.min(1, w / 6);
    }

    /** Si (u, y, v) es hueco de la grieta (aire, o la sangre por debajo de {@link #SANGRE}); nunca por debajo del fondo. */
    public static boolean hueco(int u, int v, int y) {
        if (y <= FONDO || u <= G_U0 || u >= G_U1) return false;
        double d = v + 0.5 - centro(u + 0.5);
        return Math.abs(d) < media(u + 0.5, y, d < 0 ? -1 : 1);
    }

    /** Lo que se pisa en cada orilla en u (la primera v fuera del hueco, por arriba): para poner cosas en el borde. */
    static int orilla(int u, int s) {
        int v = (int) Math.round(centro(u));
        while (hueco(u, v, SUELO) && Math.abs(v) < 40) v += s;
        return v;
    }

    /** Las vetas de las paredes de la grieta (cada una con su piedra), con huesos de los que cayeron y la sangre abajo. */
    static BlockState veta(int x, int y, int z, int u) {
        int p = Hash.pct(x, y, z, SEMILLA + 9);
        if (p < 1) return Varadero.hueso(p == 0 && (x & 1) == 0 ? Direction.Axis.X : Direction.Axis.Z);
        if (y <= SANGRE + 2) return p < 50 ? b(Blocks.NETHERRACK) : p < 78 ? b(Blocks.NETHER_WART_BLOCK) : b(Blocks.RED_TERRACOTTA);
        double t = y + 2.2 * RUIDO.ruido(u / 31.0 + 3, 5.5) + 0.8 * RUIDO.ruido(u / 7.0, y / 9.0);
        int k = Math.floorMod((int) Math.floor(t / 3.0), 9);
        BlockState s = switch (k) {
            case 0, 5 -> b(Blocks.RED_TERRACOTTA);
            case 1, 7 -> b(Blocks.TERRACOTTA);
            case 2 -> b(Blocks.BROWN_TERRACOTTA);
            case 3 -> b(Blocks.BLACKSTONE);
            case 4 -> Varadero.tronco(Blocks.BASALT, Direction.Axis.Y);
            case 6 -> b(Blocks.ORANGE_TERRACOTTA);
            default -> b(Blocks.RED_TERRACOTTA);
        };
        // algun bloque suelto distinto (que la veta no sea una raya perfecta)
        if (p > 95) return b(Blocks.BLACKSTONE);
        if (p > 91) return b(Blocks.TERRACOTTA);
        return s;
    }

    /**
     * La grieta: en cada columna, del suelo hacia abajo, el hueco (aire y, por debajo de {@link #SANGRE}, la sangre); las paredes
     * con sus vetas (las dos de arriba son de la tierra), el fondo bajo la sangre y enredaderas rojas colgando de los salientes.
     */
    private final class Sima extends Element {
        Sima() {
            super(SEMILLA + 61);
            this.layer = 2;
            bounds(CX + G_U0 - 1, FONDO - 1, CZ - 26, CX + G_U1 + 1, SUELO + 10, CZ + 26);
        }

        @Override
        public void build(Canvas c) {
            for (int x = Math.max(c.minX, x0); x <= Math.min(c.maxX, x1); x++) {
                int u = x - CX;
                double w = ancho(u + 0.5);
                if (w <= 0) continue;
                double cv = centro(u + 0.5);
                for (int z = Math.max(c.minZ, z0); z <= Math.min(c.maxZ, z1); z++) {
                    int v = z - CZ;
                    double d = Math.abs(v + 0.5 - cv);
                    if (d > w + 5) continue;
                    int o = objetivo(x, z);
                    int top = o != Integer.MIN_VALUE ? o : alto(x, z);
                    boolean antes = false;
                    for (int y = top + 3; y > FONDO - 1; y--) {
                        boolean h = hueco(u, v, y);
                        if (h) {
                            c.set(x, y, z, y <= SANGRE ? Pal.WATER : Pal.AIR);
                            // las enredaderas rojas que cuelgan de un saliente (el bloque de encima es pared)
                            if (!antes && y < top - 2 && y > SANGRE + 6 && Hash.pct(x, y, z, seed + 3) < 9) {
                                int largo = 1 + Hash.pct(x, y, z, seed + 4) % 5;
                                for (int k = 0; k < largo; k++) {
                                    if (!hueco(u, v, y - k) || y - k <= SANGRE + 2) break;
                                    boolean punta = k == largo - 1 || !hueco(u, v, y - k - 1);
                                    c.set(x, y - k, z, punta ? b(Blocks.WEEPING_VINES) : b(Blocks.WEEPING_VINES_PLANT));
                                }
                            }
                        } else if (y < top - 1 && y <= top) {
                            // la pared: las vetas donde se ven (a menos de tres del hueco)
                            double s = media(u + 0.5, y, v + 0.5 - cv < 0 ? -1 : 1);
                            if (d - s < 2.6 || y == FONDO) c.set(x, y, z, y == FONDO ? fondo(x, y, z) : veta(x, y, z, u));
                        }
                        antes = h;
                    }
                }
            }
        }
    }

    /** El fondo de la grieta, bajo la sangre: tierra de almas, roca del infierno y huesos. */
    static BlockState fondo(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 13);
        return p < 40 ? b(Blocks.SOUL_SAND) : p < 75 ? b(Blocks.NETHERRACK) : p < 92 ? b(Blocks.SOUL_SOIL) : Varadero.hueso(Direction.Axis.X);
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

    /** Una vela roja (de una a cuatro), encendida. */
    static BlockState vela(int n) {
        return Blocks.RED_CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, Math.max(1, Math.min(4, n))).setValue(CandleBlock.LIT, true);
    }

    static BlockState farol(boolean colgado) {
        return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, colgado);
    }

    /** La piedra negra labrada del puente y la puerta: ladrillo de piedra negra pulida, con alguno agrietado. */
    static BlockState sillar(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 21);
        return p < 66 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS) : p < 88 ? b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS) : p < 95 ? b(Blocks.BLACKSTONE)
                : b(Blocks.POLISHED_BLACKSTONE);
    }

    /** El muro de la hospederia: adobe y terracota parda. */
    static BlockState adobe(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 23);
        return p < 58 ? b(Blocks.MUD_BRICKS) : p < 80 ? b(Blocks.PACKED_MUD) : p < 94 ? b(Blocks.BROWN_TERRACOTTA) : b(Blocks.TERRACOTTA);
    }

    // ------------------------------------------------------------------ el suelo

    /** Lo alto del suelo en (x, z): lo allanado (y lo que se funde) o MIN_VALUE donde no se toca. */
    static int objetivo(int x, int z) {
        int u = x - CX, v = z - CZ;
        double du = u < U_LLANO0 ? U_LLANO0 - u : u > U_LLANO1 ? u - U_LLANO1 : 0;
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

    /** La tierra de la Tierra sin Fe: arena roja, tierra gruesa, tierra de almas y terracota parda; las grietas finas, negras. */
    private static BlockState tierra(int u, int v, int p) {
        if (RUIDO.crestas(u + 300, v - 200, 34, 2) > 0.94) return p < 60 ? b(Blocks.BLACKSTONE) : Varadero.tronco(Blocks.BASALT, Direction.Axis.Y);
        double n = RUIDO.ruido(u / 11.0 + 5, v / 11.0 - 9);
        if (n > 0.3) return p < 85 ? b(Blocks.RED_SAND) : b(Blocks.COARSE_DIRT);
        if (n > -0.15) return p < 80 ? b(Blocks.COARSE_DIRT) : p < 92 ? b(Blocks.RED_SAND) : b(Blocks.GRAVEL);
        return p < 55 ? b(Blocks.SOUL_SOIL) : p < 90 ? b(Blocks.BROWN_TERRACOTTA) : b(Blocks.COARSE_DIRT);
    }

    /**
     * La terraza: corta lo que sobra hasta el suelo y rellena por debajo; la tierra roja y agrietada, con matas secas, huesos y
     * piedras negras sueltas. La tarima del santuario no se toca.
     */
    private final class Terraza extends Element {
        Terraza() {
            super(SEMILLA + 51);
            this.layer = 1;
            bounds(CX + U_LLANO0 - (int) FUNDE - 1, 60, CZ + V_LLANO0 - (int) FUNDE - 1, CX + U_LLANO1 + (int) FUNDE + 1, 150,
                    CZ + V_LLANO1 + (int) FUNDE + 1);
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
                    for (int y = t + 1; y < o; y++) c.set(x, y, z, y >= o - 2 ? b(Blocks.COARSE_DIRT) : b(Blocks.TERRACOTTA));
                    int p = Hash.pct(x, o, z, seed);
                    c.set(x, o, z, tierra(u, v, p));
                    if (o != SUELO || ocupado(u, v)) continue;
                    int q = Hash.pct(x, o + 1, z, seed + 1);
                    if (q < 2) c.set(x, o + 1, z, Pal.b(Blocks.DEAD_BUSH));
                    else if (q == 2 && Hash.pct(x, o, z, seed + 2) < 30) c.set(x, o + 1, z, Varadero.hueso(Direction.Axis.X));
                }
        }
    }

    /** Si en (u, v) hay algo del plano (el puente y sus cabezas, la puerta, el paso, el cementerio, el humilladero, la hospederia, el campamento). */
    static boolean ocupado(int u, int v) {
        if (Math.abs(u) <= 12 && v >= PUERTA_V - 22 && v <= PUENTE_V1 + 6) return true;
        if (u >= -48 && u <= -10 && v >= -60 && v <= -30) return true;
        if (u >= 14 && u <= 24 && v >= -44 && v <= -34) return true;
        if (u >= HOSP_U0 - 2 && u <= HOSP_U1 + 2 && v >= HOSP_V0 - 2 && v <= 38) return true;
        if (u >= 6 && u <= 54 && v >= 19 && v <= 48) return true;
        return alCamino(u + 0.5, v + 0.5) < 4;
    }

    // ------------------------------------------------------------------ el camino

    /** El punto del camino que esta a {@code desde} bloques del santuario (u, v). */
    static int[] puntoCamino(double desde) {
        double hecho = 0;
        for (int i = 0; i + 1 < CAMINO.length; i++) {
            double[] a = CAMINO[i], b = CAMINO[i + 1];
            double l = Math.hypot(b[0] - a[0], b[1] - a[1]);
            if (hecho + l >= desde) {
                double t = (desde - hecho) / l;
                return new int[]{(int) Math.round(a[0] + (b[0] - a[0]) * t), (int) Math.round(a[1] + (b[1] - a[1]) * t)};
            }
            hecho += l;
        }
        double[] f = CAMINO[CAMINO.length - 1];
        return new int[]{(int) f[0], (int) f[1]};
    }

    /** Lo lejos de (u, v) al camino del sur (del santuario al puente). */
    static double alCamino(double u, double v) {
        double m = Double.MAX_VALUE;
        for (int i = 0; i + 1 < CAMINO.length; i++) {
            double[] a = CAMINO[i], b = CAMINO[i + 1];
            double dx = b[0] - a[0], dz = b[1] - a[1], l2 = dx * dx + dz * dz;
            double t = Math.max(0, Math.min(1, ((u - a[0]) * dx + (v - a[1]) * dz) / l2));
            m = Math.min(m, Math.hypot(u - (a[0] + dx * t), v - (a[1] + dz * t)));
        }
        return m;
    }

    /** Si (u, v) es del camino del norte (de la puerta hacia el crater: cinco de ancho, se pierde al salir de lo allanado). */
    static boolean caminoNorte(int u, int v) {
        if (v > PUERTA_V - 2 || v < V_LLANO0 - 14) return false;
        double borde = 2.4 + 0.5 * RUIDO.ruido(v / 5.0, 3);
        if (v < V_LLANO0) borde -= (V_LLANO0 - v) * 0.12;
        return Math.abs(u + 0.5 * Math.sin(v / 13.0)) <= borde;
    }

    /** Las losas gastadas del camino de los peregrinos (dentro de lo allanado) y la tierra pisada (fuera). */
    static BlockState losa(int x, int y, int z, boolean fuera) {
        int p = Hash.pct(x, y, z, SEMILLA + 31);
        if (fuera) return p < 45 ? b(Blocks.COARSE_DIRT) : p < 70 ? b(Blocks.GRAVEL) : p < 88 ? b(Blocks.PACKED_MUD) : b(Blocks.BLACKSTONE);
        return p < 40 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS) : p < 58 ? b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS) : p < 74 ? b(Blocks.BLACKSTONE)
                : p < 86 ? b(Blocks.GRAVEL) : b(Blocks.COARSE_DIRT);
    }

    /**
     * Los caminos: el del sur, del santuario a la cabeza del puente (cinco de ancho dentro de lo allanado, tres fuera, siguiendo el
     * terreno), y el del norte, de la puerta hacia el crater. Quita lo que crecia encima.
     */
    private final class Camino extends Element {
        Camino() {
            super(SEMILLA + 71);
            this.layer = 2;
            bounds(X0, 60, Z0, CX + 12, 150, Z1);
        }

        @Override
        public void build(Canvas c) {
            for (int x = Math.max(c.minX, x0); x <= Math.min(c.maxX, x1); x++)
                for (int z = Math.max(c.minZ, z0); z <= Math.min(c.maxZ, z1); z++) {
                    int u = x - CX, v = z - CZ;
                    if (Pilotes.cercaSantuario(x, z) || hueco(u, v, SUELO)) continue;
                    boolean dentro = objetivo(x, z) == SUELO;
                    double d = alCamino(u + 0.5, v + 0.5) + RUIDO.ruido(u / 4.0, v / 4.0 + 50) * 0.4;
                    boolean sur = d <= (dentro ? 2.5 : 1.6), norte = caminoNorte(u, v);
                    if (!sur && !norte) continue;
                    int y = pie(x, z) - 1;
                    c.set(x, y, z, losa(x, y, z, !dentro));
                    for (int k = 1; k <= 7; k++) if (!c.get(x, y + k, z).isAir()) c.set(x, y + k, z, Pal.AIR);
                }
        }
    }

    // ------------------------------------------------------------------ el puente

    /** Lo de abajo del arco del puente en v (por encima de esto, piedra): mas hondo hacia las orillas. */
    static int bajo(int v) {
        int a = Math.abs(v);
        if (a <= 10) return SUELO - 3 - (int) Math.round(11.0 * (a / 10.0) * (a / 10.0));
        return SUELO - 34;
    }

    /**
     * El puente: el tablero de piedra negra (cinco de paso y los pretiles de muro con sus pilares, velas y faroles), el arco por
     * debajo (las dovelas lisas, la enjuta de sillar y los estribos pegados a las paredes de la grieta), la cornisa sobre sus
     * mensulas, la espadana con la campana en medio y una jaula colgada bajo el arco.
     */
    private void puente(Pilotes.Lista m) {
        for (int v = PUENTE_V0; v <= PUENTE_V1; v++)
            for (int u = -PUENTE_U; u <= PUENTE_U; u++) {
                int x = x(u), z = z(v);
                // el tablero
                int p = Hash.pct(x, SUELO, z, SEMILLA + 41);
                BlockState losa = Math.abs(u) == PUENTE_U ? sillar(x, SUELO, z)
                        : p < 62 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS) : p < 84 ? b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS)
                        : p < 92 ? b(Blocks.POLISHED_BLACKSTONE) : p < 97 ? b(Blocks.CHISELED_POLISHED_BLACKSTONE) : b(Blocks.NETHER_WART_BLOCK);
                put(m, u, SUELO, v, losa);
                // el arco, la enjuta y los estribos: donde la grieta es hueco
                int ab = bajo(v);
                for (int y = ab; y < SUELO; y++) {
                    if (!hueco(u, v, y) && !(Math.abs(v) <= 10 && y >= ab)) continue;
                    boolean dovela = Math.abs(v) <= 10 && y <= ab + 1;
                    BlockState s = dovela ? (Math.abs(u) == PUENTE_U && Math.floorMod(v, 3) == 0 ? b(Blocks.CHISELED_POLISHED_BLACKSTONE)
                            : b(Blocks.POLISHED_BLACKSTONE)) : sillar(x, y, z);
                    put(m, u, y, v, s);
                }
                // por encima, aire (salvo el pretil)
                for (int y = PISO; y <= PISO + 5; y++) put(m, u, y, v, Pal.AIR);
            }
        // los pretiles: muro con pilares (velas rojas encima; faroles en las puntas)
        for (int s : LADOS)
            for (int v = PUENTE_V0; v <= PUENTE_V1; v++) {
                if (Math.abs(v) <= 1) continue;   // la espadana
                int a = Math.abs(v);
                boolean pilar = a == 6 || a == 12 || a == 17;
                if (pilar) {
                    put(m, s * PUENTE_U, PISO, v, sillar(x(s * PUENTE_U), PISO, z(v)));
                    put(m, s * PUENTE_U, PISO + 1, v, b(Blocks.CHISELED_POLISHED_BLACKSTONE));
                    put(m, s * PUENTE_U, PISO + 2, v, a == 17 ? farol(false) : vela(2 + Math.floorMod(v * s, 3)));
                    velas++;
                } else une(m, s * PUENTE_U, PISO, v, b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            }
        // la cornisa: las mensulas (escalones del reves contra el puente) y la losa encima, por donde el puente vuela
        for (int s : LADOS)
            for (int v = -13; v <= 13; v++) {
                if (!hueco(s * (PUENTE_U + 1), v, SUELO - 1)) continue;
                put(m, s * (PUENTE_U + 1), SUELO - 1, v, Varadero.escalera(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, s < 0 ? Direction.EAST : Direction.WEST, true));
                if (Math.floorMod(v, 2) == 0) put(m, s * (PUENTE_U + 1), SUELO, v, Varadero.losa(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, SlabType.BOTTOM));
            }
        espadana(m);
        jaula(m, 0, 4);
        // las cabezas del puente: losas hasta el camino y la puerta
        for (int u = -5; u <= 5; u++) {
            for (int v = PUENTE_V1 + 1; v <= PUENTE_V1 + 4; v++)
                if (Math.abs(u) <= 3 || v <= PUENTE_V1 + 2) put(m, u, SUELO, v, losa(x(u), SUELO, z(v), false));
            for (int v = PUERTA_V + 2; v < PUENTE_V0; v++) put(m, u, SUELO, v, losa(x(u), SUELO, z(v), false));
        }
        // dos mojones con velas a la entrada del sur
        for (int s : LADOS) {
            put(m, s * 4, PISO, PUENTE_V1 + 1, sillar(x(s * 4), PISO, z(PUENTE_V1 + 1)));
            put(m, s * 4, PISO + 1, PUENTE_V1 + 1, vela(3));
            velas++;
        }
    }

    /**
     * La espadana sobre el medio del puente: dos pilares en los pretiles, el dintel por encima del paso (cuatro de alto) y el muro
     * de la campana con su hueco, su tejadillo y la cruz; dos faroles colgados bajo el dintel.
     */
    private void espadana(Pilotes.Lista m) {
        for (int s : LADOS)
            for (int v = -1; v <= 1; v++)
                for (int y = PISO; y <= PISO + 5; y++) put(m, s * PUENTE_U, y, v, sillar(x(s * PUENTE_U), y, z(v)));
        // el dintel: tres de grueso, con los arranques del arco
        for (int v = -1; v <= 1; v++) {
            for (int u = -2; u <= 2; u++) put(m, u, PISO + 5, v, sillar(x(u), PISO + 5, z(v)));
            for (int s : LADOS) put(m, s * 2, PISO + 4, v, Varadero.escalera(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, s < 0 ? Direction.WEST : Direction.EAST, true));
        }
        // el muro de la campana (en v = 0)
        for (int u = -3; u <= 3; u++) put(m, u, PISO + 6, 0, sillar(x(u), PISO + 6, z(0)));
        for (int u = -3; u <= 3; u++) if (u != 0) put(m, u, PISO + 7, 0, sillar(x(u), PISO + 7, z(0)));
        for (int u = -2; u <= 2; u++) put(m, u, PISO + 8, 0, sillar(x(u), PISO + 8, z(0)));
        for (int s : LADOS) put(m, s * 3, PISO + 8, 0, Varadero.escalera(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, s < 0 ? Direction.EAST : Direction.WEST, false));
        for (int u = -1; u <= 1; u++) put(m, u, PISO + 9, 0, sillar(x(u), PISO + 9, z(0)));
        for (int s : LADOS) put(m, s * 2, PISO + 9, 0, Varadero.escalera(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, s < 0 ? Direction.EAST : Direction.WEST, false));
        put(m, 0, PISO + 10, 0, b(Blocks.CHISELED_POLISHED_BLACKSTONE));
        for (int s : LADOS) put(m, s, PISO + 10, 0, Varadero.escalera(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, s < 0 ? Direction.EAST : Direction.WEST, false));
        // la cruz de hierro de arriba
        for (int y = PISO + 11; y <= PISO + 13; y++) une(m, 0, y, 0, b(Blocks.IRON_BARS));
        for (int s : LADOS) une(m, s, PISO + 12, 0, b(Blocks.IRON_BARS));
        // los faroles bajo el dintel
        for (int s : LADOS) {
            put(m, s, PISO + 4, s, b(Blocks.CHAIN));
            put(m, s, PISO + 3, s, farol(true));
        }
        // la campana (la pone su trozo: tiene entidad de bloque)
    }

    /** Una jaula de barrotes colgada de una cadena bajo el arco, con una calavera dentro. */
    private void jaula(Pilotes.Lista m, int uc, int vc) {
        int techo = bajo(vc) - 1, y0 = techo - 9;
        for (int y = y0 + 3; y <= techo; y++) put(m, uc, y, vc, b(Blocks.CHAIN));
        for (int du = -1; du <= 1; du++)
            for (int dv = -1; dv <= 1; dv++)
                for (int y = y0; y <= y0 + 2; y++) {
                    boolean borde = Math.abs(du) == 1 || Math.abs(dv) == 1;
                    if (y == y0) put(m, uc + du, y, vc + dv, Varadero.losa(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.TOP));
                    else if (y == y0 + 2) put(m, uc + du, y, vc + dv, Varadero.losa(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.BOTTOM));
                    else if (borde) une(m, uc + du, y, vc + dv, b(Blocks.IRON_BARS));
                    else put(m, uc + du, y, vc + dv, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, 3));
                }
    }

    /**
     * Lo que queda del puente viejo, el de cuerdas, al este: dos postes en cada orilla con las cuerdas (cadenas) colgando por la
     * pared y alguna tabla todavia atada.
     */
    private void puenteViejo(Pilotes.Lista m) {
        int u0 = 60;
        for (int s : LADOS) {
            int v = orilla(u0, s);
            int vb = v + s;
            for (int du : new int[]{-2, 2}) {
                int u = u0 + du;
                for (int y = PISO; y <= PISO + 2; y++) put(m, u, y, vb, b(Blocks.DARK_OAK_FENCE));
                put(m, u, PISO + 3, vb, b(Blocks.DARK_OAK_SLAB));
            }
            // las cuerdas cuelgan por la pared de la grieta (donde hay hueco), con alguna tabla
            for (int du : new int[]{-2, 2}) {
                int u = u0 + du;
                int vv = v - s;
                for (int k = 0; k < 9; k++) {
                    int y = SUELO - k;
                    if (!hueco(u, vv, y)) {
                        vv -= s;
                        if (!hueco(u, vv, y)) continue;
                    }
                    put(m, u, y, vv, b(Blocks.CHAIN));
                }
            }
            for (int u = u0 - 1; u <= u0 + 1; u++) {
                int vv = v - s;
                if (hueco(u, vv, SUELO - 3)) put(m, u, SUELO - 3, vv, Varadero.trampilla(Blocks.DARK_OAK_TRAPDOOR, s < 0 ? Direction.SOUTH : Direction.NORTH, true, false));
            }
        }
    }

    // ------------------------------------------------------------------ la puerta del norte

    /**
     * La puerta: dos torres de cinco en lado con sus almenas y el muro entre ellas con el arco apuntado (cinco de paso), la reja medio
     * alzada colgando en lo alto del arco, la cruz labrada encima y los estandartes rojos.
     */
    private void puerta(Pilotes.Lista m) {
        int alto = PISO + 13, muro = PISO + 9;
        for (int s : LADOS)
            for (int u = 6; u <= 10; u++)
                for (int v = PUERTA_V - 2; v <= PUERTA_V + 2; v++) {
                    for (int y = SUELO; y <= alto; y++) put(m, s * u, y, v, sillar(x(s * u), y, z(v)));
                    boolean borde = u == 6 || u == 10 || v == PUERTA_V - 2 || v == PUERTA_V + 2;
                    // las almenas: en el borde, una si y otra no
                    if (borde && Math.floorMod(u + v, 2) == 0) put(m, s * u, alto + 1, v, sillar(x(s * u), alto + 1, z(v)));
                }
        // la cornisa de las torres: escalones del reves por fuera, bajo las almenas
        for (int s : LADOS) {
            for (int v = PUERTA_V - 2; v <= PUERTA_V + 2; v++)
                put(m, s * 11, alto - 1, v, Varadero.escalera(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, s < 0 ? Direction.EAST : Direction.WEST, true));
            for (int u = 6; u <= 10; u++) {
                put(m, s * u, alto - 1, PUERTA_V - 3, Varadero.escalera(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.SOUTH, true));
                put(m, s * u, alto - 1, PUERTA_V + 3, Varadero.escalera(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.NORTH, true));
            }
        }
        // las saeteras de las torres (al sur)
        for (int s : LADOS)
            for (int y : new int[]{PISO + 4, PISO + 9}) {
                put(m, s * 8, y, PUERTA_V + 2, Pal.AIR);
                put(m, s * 8, y + 1, PUERTA_V + 2, Pal.AIR);
            }
        // el muro entre las torres, con el arco
        for (int u = -5; u <= 5; u++)
            for (int v = PUERTA_V - 1; v <= PUERTA_V + 1; v++)
                for (int y = SUELO; y <= muro; y++) {
                    int a = Math.abs(u);
                    int hueco = a <= 1 ? PISO + 5 - a : a == 2 ? PISO + 3 : -1;
                    if (y == SUELO) {
                        put(m, u, y, v, a <= 2 ? losa(x(u), y, z(v), false) : sillar(x(u), y, z(v)));
                        continue;
                    }
                    if (hueco >= 0 && y <= hueco) {
                        put(m, u, y, v, Pal.AIR);
                        continue;
                    }
                    // las dovelas alrededor del hueco
                    boolean dovela = a <= 3 && y <= PISO + 6 && (hueco < 0 || y == hueco + 1);
                    put(m, u, y, v, dovela ? b(Blocks.POLISHED_BLACKSTONE) : sillar(x(u), y, z(v)));
                }
        for (int u = -5; u <= 5; u++)
            if (Math.floorMod(u, 2) == 1) for (int v = PUERTA_V - 1; v <= PUERTA_V + 1; v += 2) put(m, u, muro + 1, v, sillar(x(u), muro + 1, z(v)));
        // la reja, medio alzada: asoma por lo alto del arco (en medio del muro)
        for (int u = -2; u <= 2; u++) une(m, u, PISO + 3, PUERTA_V, b(Blocks.IRON_BARS));
        for (int u = -1; u <= 1; u++) une(m, u, PISO + 4, PUERTA_V, b(Blocks.IRON_BARS));
        une(m, 0, PISO + 5, PUERTA_V, b(Blocks.IRON_BARS));
        // la cruz labrada sobre el arco (en la cara del sur, que mira al puente)
        for (int y = PISO + 6; y <= PISO + 9; y++) put(m, 0, y, PUERTA_V + 2, b(Blocks.CHISELED_POLISHED_BLACKSTONE));
        for (int s : LADOS) put(m, s, PISO + 8, PUERTA_V + 2, b(Blocks.CHISELED_POLISHED_BLACKSTONE));
        // los estandartes rojos en las torres y las velas al pie
        for (int s : LADOS) {
            put(m, s * 8, PISO + 7, PUERTA_V + 3, b(Blocks.RED_WALL_BANNER).setValue(WallBannerBlock.FACING, Direction.SOUTH));
            put(m, s * 8, PISO + 7, PUERTA_V - 3, b(Blocks.BLACK_WALL_BANNER).setValue(WallBannerBlock.FACING, Direction.NORTH));
            put(m, s * 5, PISO, PUERTA_V + 3, vela(4));
            put(m, s * 4, PISO, PUERTA_V + 3, vela(2));
            velas += 2;
            // los faroles a los lados del arco
            put(m, s * 4, PISO + 4, PUERTA_V + 2, b(Blocks.CHAIN));
            put(m, s * 4, PISO + 3, PUERTA_V + 2, farol(true));
        }
    }

    // ------------------------------------------------------------------ el paso abandonado

    /**
     * El paso que dejaron en mitad del camino: la parihuela de roble oscuro con su canasto, los varales tirados a los lados, las
     * velas rojas por todo el borde, cuatro faroles en las esquinas y la imagen tapada con su manto morado delante de la cruz.
     */
    private void paso(Pilotes.Lista m) {
        int v0 = -42, v1 = -36;
        for (int u = -2; u <= 2; u++)
            for (int v = v0; v <= v1; v++) {
                boolean borde = Math.abs(u) == 2 || v == v0 || v == v1;
                put(m, u, PISO, v, borde ? Varadero.tronco(Blocks.STRIPPED_DARK_OAK_LOG, Math.abs(u) == 2 ? Direction.Axis.Z : Direction.Axis.X)
                        : b(Blocks.DARK_OAK_PLANKS));
                if (borde) {
                    boolean esquina = Math.abs(u) == 2 && (v == v0 || v == v1);
                    if (esquina) {
                        for (int y = PISO + 1; y <= PISO + 2; y++) put(m, u, y, v, b(Blocks.DARK_OAK_FENCE));
                        put(m, u, PISO + 3, v, farol(false));
                    } else {
                        put(m, u, PISO + 1, v, vela(1 + Math.floorMod(u * 7 + v * 3, 4)));
                        velas++;
                    }
                }
            }
        // la imagen: la peana, el manto morado (cuatro de alto) y la cruz detras
        put(m, 0, PISO + 1, -39, b(Blocks.DARK_OAK_PLANKS));
        for (int y = PISO + 2; y <= PISO + 4; y++) put(m, 0, y, -39, b(Blocks.PURPLE_WOOL));
        put(m, 0, PISO + 5, -39, b(Blocks.PURPLE_CARPET));
        for (int s : LADOS) put(m, s, PISO + 1, -39, b(Blocks.PURPLE_CARPET));
        for (int y = PISO + 1; y <= PISO + 6; y++) put(m, 0, y, -41, Varadero.tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Y));
        for (int s : LADOS) put(m, s, PISO + 5, -41, Varadero.tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.X));
        // flores marchitas en el canasto
        put(m, -1, PISO + 1, -37, b(Blocks.WITHER_ROSE));
        put(m, 1, PISO + 1, -38, b(Blocks.WITHER_ROSE));
        put(m, 1, PISO + 1, -40, b(Blocks.DEAD_BUSH));
        // los varales, tirados en el suelo a los lados
        for (int s : LADOS)
            for (int v = v0 - 2; v <= v1 + 2; v++) put(m, s * 3, PISO, v, Varadero.tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Z));
    }

    // ------------------------------------------------------------------ el cementerio de los peregrinos

    /**
     * El cementerio, al noroeste: la tapia baja de piedra negra (rota a trozos) con su entrada hacia el camino, las tumbas en filas
     * (tierra removida, una cruz a la cabeza —de madera, de piedra, rota o caida—, velas, alguna abierta con su calavera) y un arbol
     * seco de basalto.
     */
    private void cementerio(Pilotes.Lista m) {
        int u0 = -46, u1 = -12, v0 = -58, v1 = -32;
        for (int u = u0; u <= u1; u++)
            for (int v = v0; v <= v1; v++) {
                boolean borde = u == u0 || u == u1 || v == v0 || v == v1;
                if (!borde) continue;
                if (u == u1 && v >= -47 && v <= -43) continue;   // la entrada
                int p = Hash.pct(u, v, 5, SEMILLA + 51);
                boolean esquina = (u == u0 || u == u1) && (v == v0 || v == v1);
                boolean pilar = esquina || u == u1 && (v == -48 || v == -42);
                if (pilar) {
                    put(m, u, PISO, v, sillar(x(u), PISO, z(v)));
                    put(m, u, PISO + 1, v, p < 50 ? b(Blocks.POLISHED_BLACKSTONE_SLAB) : vela(2));
                } else if (p < 82) une(m, u, PISO, v, b(p < 60 ? Blocks.BLACKSTONE_WALL : Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                else if (p < 90) put(m, u, PISO, v, Varadero.losa(Blocks.BLACKSTONE_SLAB, SlabType.BOTTOM));
            }
        for (int v = v0 + 4; v <= v1 - 4; v += 4)
            for (int u = u0 + 4; u <= u1 - 4; u += 4) {
                int p = Hash.pct(u, v, 9, SEMILLA + 53);
                if (p < 8) continue;   // un hueco en la fila
                tumba(m, u, v, p);
            }
        // el arbol seco, en la esquina del noroeste
        int au = u0 + 3, av = v0 + 3;
        for (int y = PISO; y <= PISO + 6; y++) put(m, au, y, av, Varadero.tronco(Blocks.BASALT, Direction.Axis.Y));
        put(m, au + 1, PISO + 4, av, Varadero.tronco(Blocks.BASALT, Direction.Axis.X));
        put(m, au + 2, PISO + 5, av, Varadero.tronco(Blocks.BASALT, Direction.Axis.Y));
        put(m, au, PISO + 5, av - 1, Varadero.tronco(Blocks.BASALT, Direction.Axis.Z));
        put(m, au, PISO + 6, av - 2, Varadero.tronco(Blocks.BASALT, Direction.Axis.Y));
    }

    /** Una tumba: la tierra removida (dos de largo, al sur de la cruz), la cruz a la cabeza y, a veces, velas o la tumba abierta. */
    private void tumba(Pilotes.Lista m, int u, int v, int p) {
        tumbas++;
        int q = Hash.pct(u, v, 11, SEMILLA + 55);
        boolean abierta = q < 12;
        for (int dv = 1; dv <= 2; dv++) {
            if (abierta) {
                put(m, u, SUELO, v + dv, Pal.AIR);
                put(m, u, SUELO - 1, v + dv, b(Blocks.SOUL_SOIL));
            } else put(m, u, SUELO, v + dv, b(q % 3 == 0 ? Blocks.ROOTED_DIRT : q % 3 == 1 ? Blocks.COARSE_DIRT : Blocks.PODZOL));
        }
        if (abierta) put(m, u, SUELO, v + 1, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, q % 16));
        // la cruz
        if (p < 55) {
            // de madera
            une(m, u, PISO, v, b(Blocks.DARK_OAK_FENCE));
            une(m, u, PISO + 1, v, b(Blocks.DARK_OAK_FENCE));
            for (int s : LADOS) une(m, u + s, PISO + 1, v, b(Blocks.DARK_OAK_FENCE));
            cruces++;
        } else if (p < 80) {
            // de piedra
            une(m, u, PISO, v, b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            une(m, u, PISO + 1, v, b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            for (int s : LADOS) une(m, u + s, PISO + 1, v, b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            cruces++;
        } else if (p < 92) {
            // rota: solo el palo
            une(m, u, PISO, v, b(Blocks.DARK_OAK_FENCE));
        } else {
            // caida: el palo tirado en la tierra
            put(m, u, PISO, v, Varadero.losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
        }
        if (!abierta && q > 70) {
            put(m, u, PISO, v + 1, vela(1 + q % 4));
            velas++;
        }
    }

    // ------------------------------------------------------------------ el humilladero

    /**
     * El humilladero, al noreste del camino: la grada de dos escalones, cuatro pilares, el tejadillo de piedra negra en piramide con
     * su cruz y, dentro, la cruz de piedra sobre su peana con velas al pie.
     */
    private void humilladero(Pilotes.Lista m) {
        int uc = 19, vc = -39;
        for (int du = -3; du <= 3; du++)
            for (int dv = -3; dv <= 3; dv++) {
                boolean dentro = Math.abs(du) <= 2 && Math.abs(dv) <= 2;
                put(m, uc + du, PISO, vc + dv, dentro ? b(Blocks.POLISHED_BLACKSTONE) : Varadero.losa(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.BOTTOM));
                if (dentro) for (int y = PISO + 1; y <= PISO + 4; y++) put(m, uc + du, y, vc + dv, Pal.AIR);
            }
        for (int su : LADOS)
            for (int sv : LADOS) {
                for (int y = PISO + 1; y <= PISO + 4; y++) put(m, uc + 2 * su, y, vc + 2 * sv, sillar(x(uc + 2 * su), y, z(vc + 2 * sv)));
            }
        // el tejadillo
        for (int du = -3; du <= 3; du++)
            for (int dv = -3; dv <= 3; dv++) {
                int a = Math.max(Math.abs(du), Math.abs(dv));
                if (a == 3) {
                    Direction f = Math.abs(du) == 3 ? (du < 0 ? Direction.EAST : Direction.WEST) : (dv < 0 ? Direction.SOUTH : Direction.NORTH);
                    put(m, uc + du, PISO + 5, vc + dv, Varadero.escalera(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, f, false));
                } else put(m, uc + du, PISO + 5, vc + dv, sillar(x(uc + du), PISO + 5, z(vc + dv)));
                if (a == 2) {
                    Direction f = Math.abs(du) == 2 ? (du < 0 ? Direction.EAST : Direction.WEST) : (dv < 0 ? Direction.SOUTH : Direction.NORTH);
                    put(m, uc + du, PISO + 6, vc + dv, Varadero.escalera(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, f, false));
                } else if (a <= 1) put(m, uc + du, PISO + 6, vc + dv, sillar(x(uc + du), PISO + 6, z(vc + dv)));
            }
        for (int y = PISO + 7; y <= PISO + 8; y++) une(m, uc, y, vc, b(Blocks.IRON_BARS));
        for (int s : LADOS) une(m, uc + s, PISO + 8, vc, b(Blocks.IRON_BARS));
        // la cruz de piedra sobre su peana (al norte del medio: delante se reza)
        put(m, uc, PISO + 1, vc - 1, b(Blocks.CHISELED_POLISHED_BLACKSTONE));
        for (int y = PISO + 2; y <= PISO + 4; y++) une(m, uc, y, vc - 1, b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
        for (int s : LADOS) une(m, uc + s, PISO + 3, vc - 1, b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
        for (int s : LADOS) {
            put(m, uc + s, PISO + 1, vc - 1, vela(2 + (s > 0 ? 1 : 0)));
            velas++;
        }
    }

    // ------------------------------------------------------------------ la hospederia

    /**
     * La hospederia de los peregrinos, al oeste del camino del sur: zocalo de piedra negra, muros de adobe con su entramado de roble
     * oscuro, tejado a dos aguas (hundido sobre el refectorio) con la chimenea y la cruz; dentro el refectorio (la mesa larga con sus
     * bancos y velas, el hogar, la olla, los barriles, los jergones de paja y la lampara) y, tras el tabique, la capilla (el altar con
     * su cruz, los bancos, las vidrieras rojas, el atril del libro perdido 28 y el arcon).
     */
    private void hospederia(Pilotes.Lista m) {
        int u0 = HOSP_U0, u1 = HOSP_U1, v0 = HOSP_V0, v1 = HOSP_V1, techo = PISO + 4;
        int vm = (v0 + v1) / 2;
        for (int u = u0; u <= u1; u++)
            for (int v = v0; v <= v1; v++) {
                boolean muro = u == u0 || u == u1 || v == v0 || v == v1;
                boolean capilla = u > HOSP_TABIQUE;
                put(m, u, SUELO, v, muro ? sillar(x(u), SUELO, z(v)) : capilla ? b(Blocks.POLISHED_BLACKSTONE_BRICKS)
                        : b(Hash.pct(u, v, 3, SEMILLA + 61) < 70 ? Blocks.PACKED_MUD : Blocks.MUD_BRICKS));
                for (int y = PISO; y <= techo; y++) {
                    if (!muro) {
                        put(m, u, y, v, u == HOSP_TABIQUE ? adobe(x(u), y, z(v)) : Pal.AIR);
                        continue;
                    }
                    boolean poste = (u == u0 || u == u1) && (v == v0 || v == v1) || (v == v0 || v == v1) && Math.floorMod(u - u0, 7) == 0
                            || (u == u0 || u == u1) && v == vm;
                    BlockState s = y == PISO ? sillar(x(u), y, z(v))
                            : poste ? Varadero.tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Y)
                            : y == techo ? Varadero.tronco(Blocks.DARK_OAK_LOG, v == v0 || v == v1 ? Direction.Axis.X : Direction.Axis.Z)
                            : adobe(x(u), y, z(v));
                    put(m, u, y, v, s);
                }
            }
        // la puerta (al sur, hacia el camino: dos de ancho, tres de alto, con su dintel) y la del tabique
        for (int u = -37; u <= -36; u++)
            for (int y = PISO; y <= PISO + 2; y++) put(m, u, y, v1, Pal.AIR);
        for (int u = -38; u <= -35; u++) put(m, u, PISO + 3, v1, Varadero.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        for (int y = PISO; y <= PISO + 2; y++) put(m, HOSP_TABIQUE, y, vm, Pal.AIR);
        put(m, HOSP_TABIQUE, PISO + 3, vm, Varadero.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.Z));
        // las ventanas del refectorio (barrotes) y las vidrieras de la capilla (cristal rojo)
        for (int u : new int[]{-43, -31})
            for (int y = PISO + 2; y <= PISO + 3; y++) une(m, u, y, v1, b(Blocks.IRON_BARS));
        for (int u : new int[]{-43, -37, -31})
            for (int y = PISO + 2; y <= PISO + 3; y++) une(m, u, y, v0, b(Blocks.IRON_BARS));
        for (int v : new int[]{v0, v1})
            for (int u : new int[]{-24, -21})
                for (int y = PISO + 1; y <= PISO + 3; y++) une(m, u, y, v, b(Blocks.RED_STAINED_GLASS_PANE));
        for (int v = vm - 1; v <= vm + 1; v++) une(m, u1, PISO + 3, v, b(Blocks.RED_STAINED_GLASS_PANE));
        une(m, u1, PISO + 4, vm, b(Blocks.RED_STAINED_GLASS_PANE));
        // el tejado: a dos aguas, la cumbrera a lo largo (en vm), los hastiales de adobe; hundido sobre el refectorio
        for (int u = u0 - 1; u <= u1 + 1; u++)
            for (int v = v0 - 1; v <= v1 + 1; v++) {
                int k = Math.min(v - (v0 - 1), (v1 + 1) - v);
                int y = techo + k;
                boolean hundido = u >= -41 && u <= -39 && v >= v0 + 1 && v <= v0 + 3;
                if (hundido) continue;
                if (v == vm) put(m, u, y, v, Varadero.losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
                else put(m, u, y, v, Varadero.escalera(Blocks.DARK_OAK_STAIRS, v < vm ? Direction.SOUTH : Direction.NORTH, false));
                if (u == u0 || u == u1) for (int yy = techo + 1; yy < y; yy++) put(m, u, yy, v, adobe(x(u), yy, z(v)));
                if (u == HOSP_TABIQUE && v > v0 && v < v1) for (int yy = techo + 1; yy < y; yy++) put(m, u, yy, v, adobe(x(u), yy, z(v)));
            }
        // lo que cayo del tejado hundido: tablas y telaranas, contra el muro del norte
        put(m, -40, PISO, v0 + 1, Varadero.losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
        put(m, -41, PISO, v0 + 1, b(Blocks.DARK_OAK_STAIRS));
        put(m, -39, PISO + 3, v0 + 1, Pal.COBWEB);
        // la chimenea: el hogar en el muro del oeste, la campana de ladrillo y el tiro hasta encima de la cumbrera
        put(m, u0 + 1, PISO, vm, b(Blocks.CAMPFIRE));
        for (int dv = -1; dv <= 1; dv++) {
            put(m, u0 + 1, PISO + 2, vm + dv, b(Blocks.BRICKS));
            put(m, u0 + 1, PISO + 3, vm + dv, b(Blocks.BRICKS));
            if (dv != 0) put(m, u0 + 1, PISO, vm + dv, b(Blocks.BRICKS));
        }
        for (int y = PISO + 1; y <= techo + 9; y++) put(m, u0, y, vm, y > techo + 7 ? b(Blocks.BRICKS) : adobe(x(u0), y, z(vm)));
        une(m, u0, techo + 10, vm, b(Blocks.MUD_BRICK_WALL));
        // la cruz de la capilla, sobre el hastial del este
        for (int y = techo + 8; y <= techo + 10; y++) une(m, u1, y, vm, b(Blocks.DARK_OAK_FENCE));
        for (int s : LADOS) une(m, u1, techo + 9, vm + s, b(Blocks.DARK_OAK_FENCE));
        refectorio(m, vm);
        capilla(m, vm);
        // fuera: el pozo junto a la puerta y el sendero hasta el camino
        pozo(m, -29, 33);
        for (int v = v1 + 1; v <= 37; v++)
            for (int u = -37; u <= -36; u++) put(m, u, SUELO, v, losa(x(u), SUELO, z(v), true));
    }

    private void refectorio(Pilotes.Lista m, int vm) {
        // la mesa larga (losas altas sobre el suelo) con sus velas y los bancos a los dos lados
        for (int u = -42; u <= -31; u++) {
            put(m, u, PISO, vm, Varadero.losa(Blocks.DARK_OAK_SLAB, SlabType.TOP));
            if (u != -42 && u != -31 && Math.floorMod(u, 2) == 0) {
                put(m, u, PISO + 1, vm, vela(1 + Math.floorMod(u, 3)));
                velas++;
            }
            if (u > -42 && u < -31) {
                put(m, u, PISO, vm - 1, Varadero.escalera(Blocks.DARK_OAK_STAIRS, Direction.NORTH, false));
                put(m, u, PISO, vm + 1, Varadero.escalera(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, false));
            }
        }
        // la olla junto al hogar y los barriles
        put(m, HOSP_U0 + 1, PISO, HOSP_V0 + 2, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 2));
        put(m, HOSP_U0 + 1, PISO, HOSP_V1 - 1, Varadero.barril(Direction.UP));
        put(m, HOSP_U0 + 2, PISO, HOSP_V1 - 1, Varadero.barril(Direction.UP));
        put(m, HOSP_U0 + 1, PISO + 1, HOSP_V1 - 1, Varadero.barril(Direction.EAST));
        // los jergones de paja contra el muro del norte (donde no cayo el tejado)
        for (int u = -36; u <= -30; u += 2) put(m, u, PISO, HOSP_V0 + 1, b(Blocks.HAY_BLOCK));
        for (int u = -35; u <= -31; u += 2) put(m, u, PISO, HOSP_V0 + 1, b(Blocks.BROWN_CARPET));
        // la lampara: una cadena de la cumbrera con su farol
        for (int y = PISO + 5; y <= PISO + 10; y++) put(m, -37, y, vm, b(Blocks.CHAIN));
        put(m, -37, PISO + 4, vm, farol(true));
    }

    private void capilla(Pilotes.Lista m, int vm) {
        int ua = HOSP_U1 - 1;
        // el altar contra el muro del este, con su paño rojo, las velas y el caliz (una rosa marchita)
        for (int v = vm - 1; v <= vm + 1; v++) put(m, ua, PISO, v, v == vm ? b(Blocks.CHISELED_POLISHED_BLACKSTONE) : b(Blocks.POLISHED_BLACKSTONE));
        put(m, ua, PISO + 1, vm, b(Blocks.POTTED_WITHER_ROSE));
        for (int s : LADOS) {
            put(m, ua, PISO + 1, vm + s, vela(3));
            velas++;
        }
        // la cruz en el muro, encima del altar (delante de la vidriera)
        for (int y = PISO + 3; y <= PISO + 6; y++) put(m, ua, y, vm, Varadero.tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Y));
        for (int s : LADOS) put(m, ua, PISO + 5, vm + s, Varadero.tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Z));
        // los bancos (mirando al altar), a los dos lados del pasillo
        for (int u = -25; u <= -24; u++)
            for (int v = HOSP_V0 + 2; v <= HOSP_V1 - 2; v++)
                if (Math.abs(v - vm) >= 2) put(m, u, PISO, v, Varadero.escalera(Blocks.DARK_OAK_STAIRS, Direction.WEST, false));
        // velas en el suelo, en los rincones del altar
        put(m, ua - 1, PISO, HOSP_V0 + 1, vela(4));
        put(m, ua - 1, PISO, HOSP_V1 - 1, vela(3));
        velas += 2;
        // la lampara
        for (int y = PISO + 5; y <= PISO + 10; y++) put(m, -22, y, vm, b(Blocks.CHAIN));
        put(m, -22, PISO + 4, vm, farol(true));
    }

    /** El pozo: el brocal de piedra negra, la sangre dentro, dos postes y el tejadillo. */
    private void pozo(Pilotes.Lista m, int uc, int vc) {
        for (int du = -1; du <= 1; du++)
            for (int dv = -1; dv <= 1; dv++) {
                boolean medio = du == 0 && dv == 0;
                put(m, uc + du, SUELO, vc + dv, medio ? Pal.WATER : sillar(x(uc + du), SUELO, z(vc + dv)));
                if (medio) {
                    put(m, uc, SUELO - 1, vc, Pal.WATER);
                    put(m, uc, SUELO - 2, vc, b(Blocks.SOUL_SAND));
                } else if (du == 0 || dv == 0) put(m, uc + du, PISO, vc + dv, Varadero.losa(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.BOTTOM));
                else une(m, uc + du, PISO, vc + dv, b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            }
        for (int s : LADOS) {
            une(m, uc + s, PISO + 1, vc, b(Blocks.DARK_OAK_FENCE));
            une(m, uc + s, PISO + 2, vc, b(Blocks.DARK_OAK_FENCE));
        }
        for (int du = -1; du <= 1; du++) put(m, uc + du, PISO + 3, vc, Varadero.losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
        put(m, uc, PISO + 2, vc, b(Blocks.CHAIN));
    }

    // ------------------------------------------------------------------ el campamento de los penitentes

    /**
     * El campamento, al este del camino del sur: la hoguera en medio con su cerco, cinco tiendas (lana parda, negra, roja y gris,
     * con su jergon dentro), los postes de los azotes (la tierra empapada alrededor), la olla sobre el fuego, la cruz de guia sobre su
     * grada con velas, braseros en las esquinas, sacos, barriles y los estandartes a la entrada.
     */
    private void campamento(Pilotes.Lista m) {
        // la hoguera
        int hu = 30, hv = 31;
        for (int du = -1; du <= 1; du++)
            for (int dv = -1; dv <= 1; dv++)
                put(m, hu + du, PISO, hv + dv, du == 0 && dv == 0 ? b(Blocks.CAMPFIRE) : Varadero.losa(Blocks.BLACKSTONE_SLAB, SlabType.BOTTOM));
        // las tiendas
        tienda(m, 15, 24, false, Blocks.BROWN_WOOL);
        tienda(m, 22, 41, false, Blocks.BLACK_WOOL);
        tienda(m, 40, 43, true, Blocks.RED_WOOL);
        tienda(m, 48, 26, false, Blocks.GRAY_WOOL);
        tienda(m, 36, 23, true, Blocks.BLACK_WOOL);
        // los postes de los azotes, con la tierra empapada
        int[][] ps = {{12, 29}, {12, 34}, {17, 36}};
        for (int[] q : ps) {
            postes++;
            for (int du = -2; du <= 2; du++)
                for (int dv = -2; dv <= 2; dv++)
                    if (du * du + dv * dv <= 5 && Hash.pct(q[0] + du, q[1] + dv, 3, SEMILLA + 71) < 62)
                        put(m, q[0] + du, SUELO, q[1] + dv, b(Hash.pct(q[0] + du, q[1] + dv, 4, SEMILLA + 72) < 60 ? Blocks.NETHER_WART_BLOCK : Blocks.CRIMSON_NYLIUM));
            for (int y = PISO; y <= PISO + 2; y++) put(m, q[0], y, q[1], Varadero.tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Y));
            une(m, q[0] + 1, PISO + 2, q[1], b(Blocks.DARK_OAK_FENCE));
            put(m, q[0] + 1, PISO + 1, q[1], b(Blocks.CHAIN));
        }
        // la olla sobre el fuego
        put(m, 34, PISO, 36, b(Blocks.CAMPFIRE));
        put(m, 34, PISO + 1, 36, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        for (int s : LADOS)
            for (int y = PISO; y <= PISO + 2; y++) une(m, 34 + s, y, 36, b(Blocks.DARK_OAK_FENCE));
        for (int u = 33; u <= 35; u++) une(m, u, PISO + 3, 36, b(Blocks.DARK_OAK_FENCE));
        put(m, 34, PISO + 2, 36, b(Blocks.CHAIN));
        // la cruz de guia sobre su grada, con velas
        int cu = 43, cv = 33;
        for (int du = -1; du <= 1; du++)
            for (int dv = -1; dv <= 1; dv++) put(m, cu + du, PISO, cv + dv, Varadero.losa(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.BOTTOM));
        put(m, cu, PISO, cv, b(Blocks.POLISHED_BLACKSTONE));
        for (int y = PISO + 1; y <= PISO + 7; y++) put(m, cu, y, cv, Varadero.tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Y));
        for (int s : LADOS) put(m, cu, PISO + 5, cv + s, Varadero.tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Z));
        put(m, cu, PISO + 4, cv - 1, b(Blocks.RED_WOOL));   // el sudario colgado del brazo
        // las velas, en la tierra al pie de la grada
        for (int[] q : new int[][]{{-1, -1}, {-1, 1}, {1, 0}}) {
            put(m, cu + q[0], PISO, cv + q[1], vela(2 + Math.floorMod(q[1], 3)));
            velas++;
        }
        // los braseros en las esquinas
        for (int[] q : new int[][]{{9, 23}, {9, 40}, {52, 22}, {52, 44}}) {
            put(m, q[0], PISO, q[1], b(Blocks.POLISHED_BLACKSTONE));
            put(m, q[0], PISO + 1, q[1], b(Blocks.CAMPFIRE));
        }
        // sacos y barriles
        for (int[] q : new int[][]{{20, 23}, {21, 23}, {45, 39}, {27, 44}})
            put(m, q[0], PISO, q[1], Hash.pct(q[0], q[1], 1, SEMILLA + 73) < 50 ? b(Blocks.HAY_BLOCK) : b(Blocks.BROWN_WOOL));
        for (int[] q : new int[][]{{26, 23}, {46, 39}, {28, 44}}) put(m, q[0], PISO, q[1], Varadero.barril(Direction.UP));
        // los estandartes a la entrada (mirando al camino, al oeste)
        for (int[] q : new int[][]{{8, 26, 0}, {8, 33, 1}}) {
            Block k = q[2] == 0 ? Blocks.RED_BANNER : Blocks.BLACK_BANNER;
            put(m, q[0], PISO, q[1], b(k).setValue(BannerBlock.ROTATION, 4));
        }
    }

    /**
     * Una tienda: lana en dos aguas (cinco de ancho, cinco de largo), la boca abierta al sur (o al este si va de lado), la cumbrera con
     * dos palos que asoman y el jergon dentro.
     */
    private void tienda(Pilotes.Lista m, int uc, int vc, boolean deLado, Block lana) {
        tiendas++;
        for (int a = -2; a <= 2; a++)
            for (int l = -2; l <= 2; l++) {
                int u = deLado ? uc + l : uc + a, v = deLado ? vc + a : vc + l;
                int alto = 2 - Math.abs(a);
                boolean fondo = l == -2;
                for (int y = PISO; y <= PISO + 2; y++) {
                    int k = y - PISO;
                    if (k == alto) put(m, u, y, v, b(lana));
                    else if (k < alto) put(m, u, y, v, fondo ? b(lana) : Pal.AIR);
                }
            }
        // los palos de la cumbrera
        for (int l : new int[]{-2, 2}) {
            int u = deLado ? uc + l : uc, v = deLado ? vc : vc + l;
            put(m, u, PISO + 3, v, b(Blocks.DARK_OAK_FENCE));
        }
        // el jergon
        for (int l = -1; l <= 0; l++) put(m, deLado ? uc + l : uc, PISO, deLado ? vc : vc + l, b(Blocks.BROWN_CARPET));
    }

    /** La cruz de termino junto al camino del sur (a medio camino del santuario), donde reza un portador. */
    private void cruzDeTermino(Pilotes.Lista m) {
        int u = -66, v = 43;
        int y = pie(x(u), z(v));
        put(m, u, y - 1, v, sillar(x(u), y - 1, z(v)));
        put(m, u, y, v, b(Blocks.CHISELED_POLISHED_BLACKSTONE));
        for (int k = 1; k <= 3; k++) une(m, u, y + k, v, b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
        for (int s : LADOS) une(m, u + s, y + 2, v, b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
        put(m, u + 1, y, v, vela(2));
        velas++;
    }

    // ------------------------------------------------------------------ lo que lleva datos: el atril, el arcon, la campana

    private void trozos() {
        int vm = (HOSP_V0 + HOSP_V1) / 2;
        // en la capilla: el atril junto al altar (de cara a quien llega por el pasillo) y el arcon al otro lado
        trozo(-20, PISO, vm + 3, c -> c.lectern(x(-20), PISO, z(vm + 3), Direction.WEST, Perdidos.libro(28)));
        trozo(-20, PISO, vm - 3, c -> c.chest(x(-20), PISO, z(vm - 3), Direction.WEST, BOTIN_BUENO));
        // los barriles del refectorio y del campamento
        trozo(HOSP_U0 + 1, PISO, HOSP_V1 - 1, c -> c.barrel(x(HOSP_U0 + 1), PISO, z(HOSP_V1 - 1), Direction.UP, BOTIN));
        trozo(26, PISO, 23, c -> c.barrel(x(26), PISO, z(23), Direction.UP, BOTIN));
        trozo(46, PISO, 39, c -> c.barrel(x(46), PISO, z(39), Direction.UP, BOTIN));
        // la campana de la espadana
        trozo(0, PISO + 7, 0, c -> c.set(x(0), PISO + 7, z(0), b(Blocks.BELL).setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING)
                .setValue(BellBlock.FACING, Direction.NORTH)));
    }

    private void trozo(int u, int y, int v, Trozo.Obra o) {
        elementos.add(new Trozo(6, x(u), y, z(v), x(u), y, z(v), o));
    }

    // ------------------------------------------------------------------ los penitentes

    private static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;

    private void ponerEsperas() {
        int F = Penitente.FLAGELANTE, P = Penitente.PORTADOR, T = Penitente.TURIFERARIO;
        int R = Enemigo.REZA, Y = Enemigo.TUMBADO, D = Enemigo.DE_PIE;
        int vm = (HOSP_V0 + HOSP_V1) / 2;
        // ---- el puente: dos flagelantes de rodillas y el turiferario bajo la espadana; un portador en la cabeza del sur
        en(F, 1, PISO, 9, SUR, R, 0, "puente");
        en(F, -1, PISO, -8, SUR, R, 0, "puente");
        en(T, 0, PISO, -3, SUR, D, 0, "puente");
        en(P, 2, PISO, PUENTE_V1 + 3, SUR, D, 0, "puente_sur");
        // ---- el camino del sur: uno que va y viene y un portador rezando ante la cruz de termino
        en(F, -20, PISO, 38, OESTE, D, 6, "camino");
        en(P, -66, pie(x(-66), z(45)), 45, NORTE, R, 0, "camino");
        // ---- el campamento: tres flagelantes en los postes, el de la olla, dos postrados ante la cruz de guia, uno a la puerta de una
        // tienda y otro de ronda
        en(F, 13, PISO, 30, OESTE, R, 0, "campamento");
        en(F, 13, PISO, 35, OESTE, R, 0, "campamento");
        en(F, 17, PISO, 37, NORTE, R, 0, "campamento");
        en(T, 34, PISO, 38, NORTE, D, 3, "campamento");
        en(F, 41, PISO, 33, ESTE, Y, 0, "campamento");
        en(P, 43, PISO, 36, NORTE, Y, 0, "campamento");
        en(P, 22, PISO, 44, SUR, D, 0, "campamento");
        en(F, 30, PISO, 40, OESTE, D, 5, "campamento");
        // ---- la hospederia: dos rezando en el refectorio, uno echado en la paja; en la capilla un turiferario de rodillas ante el
        // altar y uno postrado en el pasillo
        en(F, -40, PISO, vm - 2, SUR, R, 0, "hospederia");
        en(F, -34, PISO, vm + 2, NORTE, R, 0, "hospederia");
        en(P, -33, PISO, HOSP_V0 + 2, ESTE, Y, 0, "hospederia");
        en(T, -22, PISO, vm, ESTE, R, 0, "hospederia");
        en(F, -24, PISO, vm, ESTE, Y, 0, "hospederia");
        // ---- la puerta del norte: dos portadores de guardia por dentro
        en(P, -2, PISO, PUERTA_V - 4, SUR, D, 0, "puerta");
        en(P, 2, PISO, PUERTA_V - 4, SUR, D, 0, "puerta");
        // ---- el paso: los cuatro portadores de rodillas junto a los varales y el turiferario que lo guia
        for (int s : LADOS) {
            en(P, s * 4, PISO, -42, s < 0 ? ESTE : OESTE, R, 0, "paso");
            en(P, s * 4, PISO, -36, s < 0 ? ESTE : OESTE, R, 0, "paso");
        }
        en(T, 0, PISO, -32, SUR, D, 0, "paso");
        // ---- el cementerio: dos de rodillas ante sus tumbas y uno postrado sobre otra
        en(F, -30, PISO, -43, NORTE, R, 0, "cementerio");
        en(F, -22, PISO, -51, NORTE, R, 0, "cementerio");
        en(P, -37, PISO, -49, NORTE, Y, 0, "cementerio");
        // ---- el humilladero: un turiferario rezando ante la cruz
        en(T, 19, PISO + 1, -37, NORTE, R, 0, "humilladero");
    }

    private void en(int variante, int u, int y, int v, float giro, int postura, int ronda, String grupo) {
        esperas.add(new Espera(variante, x(u), y, z(v), giro, postura, ronda, grupo));
    }

    // ------------------------------------------------------------------ la campana

    /** Cada 5 s (desde {@link Morvain#onTick}): de vez en cuando la campana del puente tañe sola, para quien este cerca. */
    public static void campana(ServerLevel m) {
        if (m.random.nextInt(14) != 0) return;
        boolean alguien = false;
        for (ServerPlayer p : m.players())
            if (p.distanceToSqr(CX + 0.5, PISO + 7, CZ + 0.5) < 150 * 150) alguien = true;
        if (alguien) tanir(m, 3.5f);
    }

    /** Lo que llama la campana (bloques) y cada cuanto se la puede tocar para que llame (ticks). */
    public static final double LLAMA = 72;
    public static final int ESPERA_LLAMADA = 20 * 8;
    /** Para la prueba: las veces que la han tocado para llamar y los penitentes que han acudido. */
    public static int LLAMADAS, ACUDEN;
    private static long ultimaLlamada = -100000;

    /**
     * Alguien toca la campana del puente: tañe fuerte y los penitentes de la grieta que esten a menos de {@link #LLAMA} bloques (y no
     * demasiado lejos de su puesto) dejan lo que hacen y van a por el. Cada {@link #ESPERA_LLAMADA} ticks como mucho.
     */
    public static int llamar(ServerLevel m, ServerPlayer p) {
        long t = m.getGameTime();
        if (t - ultimaLlamada < ESPERA_LLAMADA && t >= ultimaLlamada) return 0;
        ultimaLlamada = t;
        LLAMADAS++;
        tanir(m, 5.0f);
        int n = 0;
        if (p.isCreative() || p.isSpectator()) return 0;
        java.util.List<Enemigo> l = m.getEntitiesOfClass(Enemigo.class, p.getBoundingBox().inflate(LLAMA, 40, LLAMA),
                e -> e instanceof Penitente && MorvainEnemigos.GRIETA.equals(e.sitio()) && e.isAlive());
        for (Enemigo e : l)
            if (e.getTarget() == null && e.acudir(p)) n++;
        ACUDEN += n;
        return n;
    }

    /** La campana del puente tañe (lo oye quien este a menos de ~150 bloques). */
    public static void tanir(ServerLevel m, float fuerza) {
        m.playSound(null, CX + 0.5, PISO + 7, CZ + 0.5, DarkCraftMod.SFX_GRIETA_CAMPANA.get(), SoundSource.AMBIENT, fuerza,
                0.92f + m.random.nextFloat() * 0.08f);
    }

    // ------------------------------------------------------------------ para la prueba

    /**
     * Lo que hay en la grieta ya generada (para la prueba): el atril con el libro 28, el arcon, la campana, el tablero del puente, el
     * hueco y la sangre de la grieta, el arco de la puerta y la imagen del paso.
     */
    public static String resumen(ServerLevel m) {
        Grieta a = get();
        int[] at = a.sitio("atril"), ar = a.sitio("arcon"), ca = a.sitio("campana");
        boolean libro = m.getBlockEntity(new BlockPos(at[0], at[1], at[2])) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le
                && Perdidos.de(le.getBook()) == 28;
        boolean arcon = m.getBlockState(new BlockPos(ar[0], ar[1], ar[2])).is(Blocks.CHEST);
        boolean campana = m.getBlockState(new BlockPos(ca[0], ca[1], ca[2])).is(Blocks.BELL);
        int tablero = 0;
        for (int v = PUENTE_V0; v <= PUENTE_V1; v += 2)
            for (int u = -2; u <= 2; u++) {
                BlockState q = m.getBlockState(new BlockPos(CX + u, SUELO, CZ + v));
                if (!q.isAir() && q.isSolidRender(m, new BlockPos(CX + u, SUELO, CZ + v))) tablero++;
            }
        // el hueco de la grieta a media altura y la sangre abajo, a un lado y a otro del puente
        int aire = 0, sangre = 0, muestras = 0;
        for (int u : new int[]{-30, -15, 15, 30, 50}) {
            int v = (int) Math.floor(centro(u + 0.5));
            muestras++;
            if (m.getBlockState(new BlockPos(CX + u, SUELO - 25, CZ + v)).isAir()) aire++;
            if (m.getFluidState(new BlockPos(CX + u, SANGRE - 1, CZ + v)).is(net.minecraft.tags.FluidTags.WATER)) sangre++;
        }
        boolean arco = !m.getBlockState(new BlockPos(CX, PISO + 6, CZ + PUERTA_V)).isAir() && m.getBlockState(new BlockPos(CX, PISO + 1, CZ + PUERTA_V)).isAir();
        boolean imagen = m.getBlockState(new BlockPos(CX, PISO + 3, CZ - 39)).is(Blocks.PURPLE_WOOL);
        return "atril con el libro 28 " + (libro ? "SI" : "NO") + ", arcon " + (arcon ? "SI" : "NO") + ", campana " + (campana ? "SI" : "NO")
                + ", tablero del puente " + tablero + "/" + (5 * ((PUENTE_V1 - PUENTE_V0) / 2 + 1)) + ", hueco de la grieta " + aire + "/" + muestras
                + ", sangre " + sangre + "/" + muestras + ", arco de la puerta " + (arco ? "SI" : "NO") + ", la imagen del paso " + (imagen ? "SI" : "NO");
    }
}
