package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.fortaleza.Perdidos;
import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Pal;
import com.darkcraft.pueblo.gen.Trozo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * La torre del homenaje de la ciudadela por dentro (parte 7ab): lo que {@link Ciudadela} dejo hueco (los muros, las ventanas rojas
 * y los suelos cada catorce bloques) se amuebla aqui, chunk a chunk al adornar (despues de la subida).
 * <ul>
 * <li>La <b>puerta</b> en la cara del sur (donde estaba el contrafuerte del centro): un arco apuntado de seis de ancho con su portico
 * de dos pilares, la gema encima y el <b>rastrillo</b> de barrotes (cerrado al generarse: se abre la primera vez que alguien llega,
 * lo hace {@link CasaSombra}). En el patio, el camino de la torre del sur a la puerta, los braseros y cuatro estatuas.</li>
 * <li>Seis plantas ({@link #SUELO}): la planta baja, el <b>Salon de la Guardia</b> (dos filas de pilares, la alfombra roja, las
 * armaduras de pie a los lados); la <b>Armeria</b>; el <b>Archivo</b> (estanterias, mesas y el atril con el libro perdido 35, el
 * diario de la reina Isolde); la <b>Prision</b> (celdas a los dos lados, jaulas colgando); la <b>Capilla Negra</b> (bancos, el altar y
 * el ultimo santuario, la Ultima Llama, con Maelis al lado); y arriba el <b>Salon del Trono</b> (la reina en su trono, al sur, de cara
 * a quien sube; espejos rotos en las paredes).</li>
 * <li>Se sube por <b>escaleras</b> de piedra pegadas a los muros, cada una en el lado contrario a la anterior (hay que cruzar cada sala):
 * al norte (subiendo hacia el oeste) y al sur (hacia el este), con su baranda; arriba, el hueco en el suelo con barandilla. Del salon
 * del trono a la azotea, la del muro del este, tras una reja que no se abre hasta que cae la reina ({@link #rejaAzotea()}).</li>
 * <li>En la <b>azotea</b>, la puerta de la aguja, cerrada (la Sombra, parte 7ac).</li>
 * </ul>
 */
public final class Homenaje {
    public static final long SEMILLA = 0x15_01DE_CA5AL;

    /** Uno de la casa: donde espera (y: donde tiene los pies), hacia donde mira, su tipo, variante, como espera, cuanto ronda y su grupo. */
    public record Espera(String tipo, int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    static final int TOP = Ciudadela.TOP;
    /** El bloque del suelo de cada planta (0 la planta baja: el patio; 6 la azotea). */
    public static final int[] SUELO = {TOP, TOP + 15, TOP + 29, TOP + 43, TOP + 57, TOP + 71, TOP + Ciudadela.K};
    public static final int BAJA = 0, ARMERIA = 1, ARCHIVO = 2, PRISION = 3, CAPILLA = 4, TRONO = 5, AZOTEA = 6;
    /** Las salas por dentro de los muros: de -LADO a LADO - 1 en x y en z. */
    public static final int LADO = 23;
    /** La puerta del sur: de x PX0 a PX1, la cara de dentro del muro (PZ_DENTRO) a la de fuera del portico (PZ_FUERA). */
    public static final int PX0 = -3, PX1 = 2, PZ_DENTRO = 23, PZ_MURO = 25, PZ_FUERA = 27;
    /** El rastrillo va en la cara de fuera del muro. */
    static final int PZ_REJA = PZ_MURO;

    /** El libro perdido del archivo (el diario de la reina Isolde). */
    public static final int LIBRO = 35;
    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/homenaje");
    public static final ResourceLocation TESORO = new ResourceLocation(DarkCraftMod.MODID, "chests/homenaje_tesoro");

    /** Lo que ocupa (para saber que chunks tocan): la torre con su portico y el camino del patio. */
    public static final int BX0 = -30, BX1 = 29, BZ0 = -30, BZ1 = 75;

    /** El ultimo santuario: la Ultima Llama, en el centro de la capilla (lo que se pisa en SANT_Y + 1). */
    public static final int SANT_X = -1, SANT_Z = 1;
    public static final int SANT_Y = SUELO[CAPILLA];
    /** Donde se sienta Maelis (junto al fuego, mirando hacia donde se llega). */
    public static final int GUIA_X = 2, GUIA_Z = 3;
    /** El trono (el asiento de la reina): en el estrado del sur, de cara al norte. */
    public static final int TRONO_X = 0, TRONO_Z = 17;

    private static Homenaje unico;

    /** Que se vuelva a hacer el plano la proxima vez (con los bloques del mod ya registrados: ver {@link Subida#rehacer}). */
    public static synchronized void rehacer() {
        unico = null;
    }

    public static synchronized Homenaje get() {
        if (unico == null) unico = new Homenaje();
        return unico;
    }

    private final Map<Long, Pilotes.Lista> listas = new HashMap<>();
    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    /** Lo que hay (para las pruebas y las vistas). */
    public int pilares, braseros, faroles, velas, estanterias, celdas, bancos, espejos, estatuas, peldanos;

    private Homenaje() {
        patio();
        puerta();
        baja();
        armeria();
        archivo();
        prision();
        capilla();
        trono();
        azotea();
        for (int k = 0; k <= 5; k++) escalera(k);
        for (Pilotes.Lista l : listas.values()) elementos.add(l.pieza(5));
        trozos();
        ponerSitios();
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    // ------------------------------------------------------------------ donde se esta

    /** Lo que se pisa en la planta k (los pies). */
    public static int pisa(int k) {
        return SUELO[k] + 1;
    }

    /** Lo mas alto de la planta k que es aire (justo debajo del suelo de la de encima). */
    static int techo(int k) {
        return SUELO[Math.min(k + 1, AZOTEA)] - 1;
    }

    /** Si (x, z) cae en las salas (por dentro de los muros y de las esquinas cortadas, y fuera de los cuatro torreones). */
    public static boolean dentro(int x, int z) {
        double ax = Math.abs(x + 0.5), az = Math.abs(z + 0.5);
        if (Math.max(ax, az) > Ciudadela.H - 3) return false;
        if ((Ciudadela.CHAFLAN - (ax + az)) / Math.sqrt(2) < 3) return false;
        return Math.hypot(ax - Ciudadela.TT, az - Ciudadela.TT) > Ciudadela.RTO + 0.2;
    }

    /** La planta en la que esta quien tiene los pies a la altura y (-1 si no es ninguna: fuera, o por encima de la azotea). */
    public static int planta(double y) {
        for (int k = AZOTEA; k >= 0; k--) if (y >= SUELO[k] + 0.5) return k == AZOTEA && y > SUELO[k] + 40 ? -1 : k;
        return -1;
    }

    /** Si (x, y, z) esta dentro de la torre del homenaje (en alguna de sus salas). */
    public static boolean enHomenaje(double x, double y, double z) {
        int k = planta(y);
        return k >= 0 && k < AZOTEA && dentro((int) Math.floor(x), (int) Math.floor(z)) && y < techo(k) + 1;
    }

    /** Si (x, y, z) esta en el salon del trono. */
    public static boolean enSalaTrono(double x, double y, double z) {
        return planta(y) == TRONO && dentro((int) Math.floor(x), (int) Math.floor(z));
    }

    /** Si (x, y, z) esta en la azotea (sobre el tejado del homenaje). */
    public static boolean enAzotea(double x, double y, double z) {
        return Math.abs(x) < Ciudadela.H + 1 && Math.abs(z) < Ciudadela.H + 1 && y >= SUELO[AZOTEA] + 0.5 && y < SUELO[AZOTEA] + 12;
    }

    /** Si (x, y, z) esta en la capilla. */
    public static boolean enCapilla(double x, double y, double z) {
        return planta(y) == CAPILLA && dentro((int) Math.floor(x), (int) Math.floor(z));
    }

    /** El nombre de la sala (para el Espejo): la clave de idioma de cada planta. */
    public static String sala(int k) {
        return "darkcraft.morvain.lugar.homenaje_" + switch (k) {
            case BAJA -> "guardia";
            case ARMERIA -> "armeria";
            case ARCHIVO -> "archivo";
            case PRISION -> "prision";
            case CAPILLA -> "capilla";
            case TRONO -> "trono";
            default -> "azotea";
        };
    }

    // ------------------------------------------------------------------ poner bloques

    private Pilotes.Lista lista(int x, int z) {
        return listas.computeIfAbsent(Velmor.clave(x >> 4, z >> 4), k -> new Pilotes.Lista());
    }

    private void put(int x, int y, int z, BlockState s) {
        lista(x, z).put(x, y, z, s);
    }

    private void une(int x, int y, int z, BlockState s) {
        lista(x, z).une(x, y, z, s);
    }

    private boolean hay(int x, int y, int z) {
        Pilotes.Lista l = listas.get(Velmor.clave(x >> 4, z >> 4));
        return l != null && l.hay(x, y, z);
    }

    static BlockState b(Block k) {
        return k.defaultBlockState();
    }

    static int pct(int x, int y, int z, long s) {
        return Subida.pct(x, y, z, s + 90);
    }

    private static BlockState escalon(Block k, Direction f) {
        return Subida.escalera(k, f, false);
    }

    private static BlockState alReves(Block k, Direction f) {
        return Subida.escalera(k, f, true);
    }

    private static BlockState losa(Block k, SlabType t) {
        return Subida.losa(k, t);
    }

    private static BlockState farolColgado() {
        return Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
    }

    private static BlockState vela(Block k, int n) {
        return k.defaultBlockState().setValue(CandleBlock.CANDLES, n).setValue(CandleBlock.LIT, true);
    }

    private static BlockState hoguera() {
        return Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true);
    }

    private static BlockState gema() {
        return com.darkcraft.gen.TorreCupula.gema(true);
    }

    private static BlockState hierroNegro() {
        return com.darkcraft.gen.TorreCupula.hierroNegro();
    }

    private static BlockState santuarioBloque() {
        try {
            return DarkCraftMod.SANTUARIO.get().defaultBlockState();
        } catch (Throwable t) {
            return Blocks.LODESTONE.defaultBlockState();
        }
    }

    private static BlockState banderola(Direction f, boolean roja) {
        return (roja ? Blocks.RED_WALL_BANNER : Blocks.BLACK_WALL_BANNER).defaultBlockState().setValue(WallBannerBlock.FACING, f);
    }

    /** Piedra negra de la torre (variada). */
    private static BlockState negra(int x, int y, int z) {
        int q = pct(x, y, z, 1);
        return q < 62 ? Pal.PB_BRICKS : q < 80 ? Pal.PB_BRICKS_CRACKED : q < 92 ? Pal.PB : Pal.BLACKSTONE;
    }

    /** Un pilar cuadrado de dos por dos (esquina de abajo a la izquierda en x, z), de y0 a y1, con basa y capitel. */
    private void pilar(int x, int z, int y0, int y1) {
        for (int dx = 0; dx <= 1; dx++)
            for (int dz = 0; dz <= 1; dz++)
                for (int y = y0; y <= y1; y++) {
                    BlockState st = y == y0 ? Pal.PB : y >= y1 - 1 ? Pal.PB_CHISELED : (y - y0) % 4 == 2 ? Pal.DS_TILES : negra(x + dx, y, z + dz);
                    put(x + dx, y, z + dz, st);
                }
        pilares++;
    }

    /** Un brasero: pedestal de piedra con fuego de almas encima. */
    private void brasero(int x, int y, int z) {
        put(x, y, z, Pal.PB_CHISELED);
        put(x, y + 1, z, hoguera());
        braseros++;
    }

    /** Una lampara: cadena del techo hacia abajo y el farol colgando. */
    private void lampara(int x, int z, int techo, int largo) {
        for (int y = techo; y > techo - largo; y--) put(x, y, z, b(Blocks.CHAIN));
        put(x, techo - largo, z, farolColgado());
        faroles++;
    }

    /** El suelo de una planta: un dibujo segun la sala (donde no haya escalera ni hueco). */
    private void suelo(int k, java.util.function.BiFunction<Integer, Integer, BlockState> dibujo) {
        int y = SUELO[k];
        for (int x = -LADO; x <= LADO - 1; x++)
            for (int z = -LADO; z <= LADO - 1; z++) {
                if (!dentro(x, z) || ocupado(k, x, z)) continue;
                BlockState st = dibujo.apply(x, z);
                if (st != null) put(x, y, z, st);
            }
    }

    // ------------------------------------------------------------------ las escaleras

    /** Cuantos peldanos tiene la escalera que sube de la planta k (lo que sube). */
    static int peldanos(int k) {
        return SUELO[k + 1] - SUELO[k];
    }

    /** Si la escalera que sube de la planta k va por el muro del norte (0, 2, 4), del sur (1, 3) o del este (5, a la azotea). */
    static int lado(int k) {
        return k == 5 ? 2 : (k & 1);
    }

    /**
     * El peldano i de la escalera que sube de la planta k: {la coordenada a lo largo (x, o z en la del este), la altura del bloque}.
     * Las del norte suben hacia el oeste desde x = 8; las del sur hacia el este desde x = -7; la de la azotea hacia el norte desde
     * z = 12.
     */
    static int[] peldano(int k, int i) {
        int y = SUELO[k] + 1 + i;
        return switch (lado(k)) {
            case 0 -> new int[]{8 - i, y};
            case 1 -> new int[]{-7 + i, y};
            default -> new int[]{12 - i, y};
        };
    }

    /** Las filas de la escalera que sube de la planta k (las z de las del norte y del sur, las x de la del este); la ultima es la de la baranda. */
    static int[] filas(int k) {
        return switch (lado(k)) {
            case 0 -> new int[]{-23, -22, -21, -20};
            case 1 -> new int[]{22, 21, 20, 19};
            default -> new int[]{22, 21, 20, 19};
        };
    }

    /** Hacia donde se sube. */
    static Direction sube(int k) {
        return switch (lado(k)) {
            case 0 -> Direction.WEST;
            case 1 -> Direction.EAST;
            default -> Direction.NORTH;
        };
    }

    /** (x, z) de la celda (a lo largo u, fila f) de la escalera de la planta k. */
    static int[] celda(int k, int u, int f) {
        return lado(k) == 2 ? new int[]{f, u} : new int[]{u, f};
    }

    /** Si en (x, z) de la planta k hay escalera (la que sube de ella, con un paso de margen al pie) o el hueco de la que llega. */
    static boolean ocupado(int k, int x, int z) {
        if (k <= 5 && enTramo(k, x, z, 1)) return true;
        return k >= 1 && enHueco(k - 1, x, z, 1);
    }

    /** Si (x, z) cae en la escalera que sube de la planta k (con m de margen a lo largo y por el lado de la sala). */
    static boolean enTramo(int k, int x, int z, int m) {
        int n = peldanos(k);
        int a = peldano(k, 0)[0], b = peldano(k, n - 1)[0];
        int u = lado(k) == 2 ? z : x, f = lado(k) == 2 ? x : z;
        int[] fs = filas(k);
        int f0 = Math.min(fs[0], fs[3]) - (fs[3] < fs[0] ? m : 0), f1 = Math.max(fs[0], fs[3]) + (fs[3] > fs[0] ? m : 0);
        return u >= Math.min(a, b) - m && u <= Math.max(a, b) + m && f >= f0 && f <= f1;
    }

    /** Si (x, z) cae en el hueco que deja en el suelo de la planta k + 1 la escalera que sube de la k (con m de margen). */
    static boolean enHueco(int k, int x, int z, int m) {
        int n = peldanos(k);
        int a = peldano(k, n - 5)[0], b = peldano(k, n - 2)[0];
        int u = lado(k) == 2 ? z : x, f = lado(k) == 2 ? x : z;
        int[] fs = filas(k);
        int f0 = Math.min(fs[0], fs[3]) - (fs[3] < fs[0] ? m : 0), f1 = Math.max(fs[0], fs[3]) + (fs[3] > fs[0] ? m : 0);
        int top = peldano(k, n - 1)[0];
        return u >= Math.min(Math.min(a, b), top) - m && u <= Math.max(Math.max(a, b), top) + m && f >= f0 && f <= f1;
    }

    /**
     * La escalera que sube de la planta k: los peldanos (escalones de piedra negra, macizos por debajo), la baranda por el lado de la
     * sala (desde el tercer peldano), el hueco en el suelo de arriba y su barandilla.
     */
    private void escalera(int k) {
        int n = peldanos(k);
        int[] fs = filas(k);
        Direction d = sube(k);
        for (int i = 0; i < n; i++) {
            int[] p = peldano(k, i);
            for (int j = 0; j < 4; j++) {
                int[] c = celda(k, p[0], fs[j]);
                // lo de debajo, macizo; el peldano; y aire por encima hasta el techo (lo que sube por el hueco)
                for (int y = SUELO[k] + 1; y < p[1]; y++) put(c[0], y, c[1], negra(c[0], y, c[1]));
                put(c[0], p[1], c[1], escalon(i % 5 == 4 ? Blocks.POLISHED_BLACKSTONE_STAIRS : Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, d));
                for (int y = p[1] + 1; y <= p[1] + 4 && y <= techo(k + 1); y++) if (!(j == 3 && y == p[1] + 1 && i >= 2)) put(c[0], y, c[1], Pal.AIR);
                peldanos++;
            }
            // la baranda, sobre la fila de fuera
            if (i >= 2) {
                int[] c = celda(k, p[0], fs[3]);
                une(c[0], p[1] + 1, c[1], b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            }
        }
        // el hueco en el suelo de arriba (sobre los peldanos n-5 .. n-2) y su barandilla (por el lado de la sala y por el extremo de abajo)
        int arriba = SUELO[k + 1];
        for (int i = n - 5; i <= n - 2; i++) {
            int[] p = peldano(k, i);
            for (int j = 0; j < 4; j++) {
                int[] c = celda(k, p[0], fs[j]);
                put(c[0], arriba, c[1], Pal.AIR);
            }
        }
        // la fila de fuera de la escalera, ya en la sala (las del norte tienen la sala al sur; las del sur, al norte; la del este, al oeste)
        int fuera = switch (lado(k)) {
            case 0 -> fs[3] + 1;
            case 1 -> fs[3] - 1;
            default -> fs[3] - 1;
        };
        int bajo = peldano(k, n - 6)[0];                       // el extremo de abajo del hueco (un peldano antes)
        for (int i = n - 6; i <= n - 1; i++) {
            int[] c = celda(k, peldano(k, i)[0], fuera);
            une(c[0], arriba + 1, c[1], b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
        }
        for (int j = 0; j < 4; j++) {
            int[] c = celda(k, bajo, fs[j]);
            une(c[0], arriba + 1, c[1], b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            put(c[0], arriba, c[1], Pal.PB);
        }
        int[] c = celda(k, bajo, fuera);
        une(c[0], arriba + 1, c[1], b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
    }

    // ------------------------------------------------------------------ el patio y la puerta

    /** El camino del patio (de la puerta de la torre del sur a la del homenaje), los braseros y las estatuas. */
    private void patio() {
        int y = TOP;
        for (int z = PZ_FUERA + 1; z <= Subida.Z_PUERTA - 3; z++) {
            for (int x = -3; x <= 2; x++) {
                boolean borde = x == -3 || x == 2;
                put(x, y, z, borde ? Pal.DS_TILES : Math.floorMod(z, 6) == 0 ? Pal.PB_CHISELED : Pal.DS_POLISHED);
            }
            // los bordillos
            put(-4, y + 1, z, Math.floorMod(z, 6) == 3 ? Pal.PB_CHISELED : losa(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, SlabType.BOTTOM));
            put(3, y + 1, z, Math.floorMod(z, 6) == 3 ? Pal.PB_CHISELED : losa(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, SlabType.BOTTOM));
        }
        // los braseros, cada nueve a los dos lados (un hueco en el bordillo donde va cada uno)
        for (int z = 33; z <= 69; z += 9) {
            for (int x : new int[]{-6, 5}) {
                put(x, y + 1, z, Pal.PB_BRICKS);
                put(x, y + 2, z, Pal.PB_CHISELED);
                put(x, y + 3, z, hoguera());
                braseros++;
            }
        }
        // cuatro estatuas de las damas de la reina, de rodillas, de cara al camino
        estatua(-11, 41, Direction.EAST);
        estatua(10, 41, Direction.WEST);
        estatua(-11, 59, Direction.EAST);
        estatua(10, 59, Direction.WEST);
    }

    /**
     * Una estatua de una dama de la reina: de rodillas en su zocalo, la cabeza baja bajo el velo y las manos juntas; mira a {@code f}.
     * (a: a lo ancho, hacia su derecha; b: hacia delante.)
     */
    private void estatua(int sx, int sz, Direction f) {
        int y = TOP + 1;
        Direction der = f.getClockWise();
        java.util.function.BiFunction<Integer, Integer, int[]> en = (a, b) -> new int[]{sx + der.getStepX() * a + f.getStepX() * b, sz + der.getStepZ() * a + f.getStepZ() * b};
        // el zocalo: tres por tres de piedra con el borde cincelado
        for (int a = -1; a <= 1; a++)
            for (int b = -1; b <= 1; b++) {
                int[] c = en.apply(a, b);
                put(c[0], y, c[1], Math.abs(a) + Math.abs(b) == 2 ? Pal.PB_CHISELED : Pal.PB_BRICKS);
            }
        // las rodillas y el vestido (por delante, mas bajo) y el cuerpo
        int[] c = en.apply(0, 0);
        put(c[0], y + 1, c[1], b(Blocks.SMOOTH_BASALT));
        put(c[0], y + 2, c[1], b(Blocks.SMOOTH_BASALT));
        put(c[0], y + 3, c[1], b(Blocks.POLISHED_BASALT));
        int[] fr = en.apply(0, 1);
        put(fr[0], y + 1, fr[1], escalon(Blocks.BLACKSTONE_STAIRS, f.getOpposite()));
        for (int a : new int[]{-1, 1}) {
            int[] l = en.apply(a, 0);
            put(l[0], y + 1, l[1], escalon(Blocks.BLACKSTONE_STAIRS, a < 0 ? der : der.getOpposite()));
        }
        // las manos juntas, delante del pecho, y la cabeza baja con su velo (la cara hacia el suelo)
        put(fr[0], y + 2, fr[1], alReves(Blocks.POLISHED_BLACKSTONE_STAIRS, f.getOpposite()));
        put(c[0], y + 4, c[1], b(Blocks.CALCITE));
        put(fr[0], y + 4, fr[1], losa(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.TOP));
        int[] at = en.apply(0, -1);
        put(at[0], y + 2, at[1], escalon(Blocks.BLACKSTONE_STAIRS, f));
        put(at[0], y + 3, at[1], escalon(Blocks.BLACKSTONE_STAIRS, f));
        put(c[0], y + 5, c[1], losa(Blocks.BLACKSTONE_SLAB, SlabType.BOTTOM));
        // una vela a sus pies
        int[] v = en.apply(1, 1);
        put(v[0], y + 1, v[1], vela(Blocks.BLACK_CANDLE, 2));
        velas++;
        estatuas++;
    }

    /** La puerta del sur: fuera el contrafuerte del centro, el paso por el muro con su arco, el portico con su gema y el rastrillo. */
    private void puerta() {
        // fuera el contrafuerte del centro de la cara (y lo que escalonaba hacia fuera)
        for (int z = 26; z <= 28; z++)
            for (int y = TOP + 1; y <= TOP + Ciudadela.K - 12; y++) put(0, y, z, Pal.AIR);
        // las ventanas de la planta baja de esta cara se tapan (las del portico)
        for (int x : new int[]{-4, -3, 5, 6})
            for (int z = PZ_DENTRO; z <= PZ_MURO; z++)
                for (int y = TOP + 4; y <= TOP + 11; y++) put(x, y, z, negra(x, y, z));
        // el paso: seis de ancho y nueve de alto con el arco apuntado, por el muro y por el portico
        for (int z = PZ_DENTRO; z <= PZ_FUERA; z++)
            for (int x = PX0; x <= PX1; x++)
                for (int y = TOP + 1; y <= TOP + 9; y++) {
                    int dx = Math.min(x - PX0, PX1 - x);          // 0 en los lados, 2 en el centro
                    boolean paso = y <= TOP + 7 || y == TOP + 8 && dx >= 1 || y == TOP + 9 && dx >= 2;
                    if (paso) put(x, y, z, Pal.AIR);
                    else if (y == TOP + 8 && dx == 0) put(x, y, z, alReves(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, x == PX0 ? Direction.EAST : Direction.WEST));
                    else if (y == TOP + 9 && dx == 1) put(x, y, z, alReves(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, x < 0 ? Direction.EAST : Direction.WEST));
                }
        // el portico: dos pilares de dos de ancho a los lados (z 26 y 27), el arco por encima y el frontis con la gema
        for (int z = 26; z <= PZ_FUERA; z++) {
            for (int x : new int[]{PX0 - 2, PX0 - 1, PX1 + 1, PX1 + 2})
                for (int y = TOP + 1; y <= TOP + 12; y++)
                    put(x, y, z, y == TOP + 1 ? Pal.PB : y == TOP + 11 ? Pal.PB_CHISELED : y == TOP + 12 ? Pal.PB : negra(x, y, z));
            for (int x = PX0; x <= PX1; x++)
                for (int y = TOP + 8; y <= TOP + 12; y++) {
                    int dx = Math.min(x - PX0, PX1 - x);
                    boolean hueco = y == TOP + 8 && dx >= 1 || y == TOP + 9 && dx >= 2;
                    boolean arco = y == TOP + 8 && dx == 0 || y == TOP + 9 && dx == 1;
                    if (hueco || arco) continue;                              // (el arco ya esta puesto)
                    put(x, y, z, y == TOP + 12 ? Pal.PB : negra(x, y, z));
                }
        }
        // el frontis: en pico encima del arco, con la gema en medio y dos farolas de almas a los lados
        for (int x = PX0 - 2; x <= PX1 + 2; x++) {
            int dx = Math.min(x - (PX0 - 2), (PX1 + 2) - x);
            for (int y = TOP + 13; y <= TOP + 13 + dx / 2; y++) put(x, y, 26, y == TOP + 13 + dx / 2 ? escalon(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, x < 0 ? Direction.EAST : Direction.WEST) : negra(x, y, 26));
        }
        put(-1, TOP + 13, PZ_FUERA, gema());
        put(0, TOP + 13, PZ_FUERA, gema());
        put(-1, TOP + 14, 26, gema());
        put(0, TOP + 14, 26, gema());
        for (int x : new int[]{PX0 - 3, PX1 + 3}) {
            put(x, TOP + 1, PZ_FUERA, Pal.PB_CHISELED);
            une(x, TOP + 2, PZ_FUERA, b(Blocks.POLISHED_BLACKSTONE_WALL));
            une(x, TOP + 3, PZ_FUERA, b(Blocks.POLISHED_BLACKSTONE_WALL));
            put(x, TOP + 4, PZ_FUERA, b(Blocks.SOUL_LANTERN));
            faroles++;
        }
        // el rastrillo: barrotes en la cara de fuera del muro (lo abre CasaSombra)
        for (BlockPos p : rastrillo()) une(p.getX(), p.getY(), p.getZ(), b(Blocks.IRON_BARS));
        // el suelo del paso
        for (int z = PZ_DENTRO; z <= PZ_FUERA; z++)
            for (int x = PX0; x <= PX1; x++) put(x, TOP, z, z == PZ_REJA ? Pal.PB_CHISELED : Pal.DS_TILES);
    }

    /** Los barrotes del rastrillo de la puerta (de abajo arriba). */
    public static List<BlockPos> rastrillo() {
        List<BlockPos> l = new ArrayList<>();
        for (int y = TOP + 1; y <= TOP + 9; y++)
            for (int x = PX0; x <= PX1; x++) {
                int dx = Math.min(x - PX0, PX1 - x);
                if (y <= TOP + 7 || y == TOP + 8 && dx >= 1 || y == TOP + 9 && dx >= 2) l.add(new BlockPos(x, y, PZ_REJA));
            }
        return l;
    }

    // ------------------------------------------------------------------ la planta baja: el Salon de la Guardia

    private static final int[][] PARES = {{-17, -16}, {-11, -10}, {-5, -4}, {3, 4}, {9, 10}, {15, 16}};

    private void baja() {
        int y0 = pisa(BAJA), t = techo(BAJA);
        suelo(BAJA, (x, z) -> {
            if (Math.floorMod(x + z, 7) == 0) return Pal.PB_CHISELED;
            return pct(x, 0, z, 2) < 18 ? Pal.PB_BRICKS_CRACKED : Pal.PB_BRICKS;
        });
        // la alfombra roja de la puerta a la escalera, con su orla negra
        for (int z = -15; z <= PZ_DENTRO - 1; z++) {
            for (int x = -2; x <= 1; x++) put(x, y0, z, b(Blocks.RED_CARPET));
            put(-3, y0, z, b(Blocks.BLACK_CARPET));
            put(2, y0, z, b(Blocks.BLACK_CARPET));
        }
        // las dos filas de pilares, con una banderola negra hacia la alfombra
        for (int[] p : PARES) {
            pilar(-9, p[0], y0, t);
            pilar(7, p[0], y0, t);
            put(-7, y0 + 7, p[0], banderola(Direction.EAST, (p[0] & 1) == 0));
            put(6, y0 + 7, p[0], banderola(Direction.WEST, (p[0] & 1) == 0));
        }
        // los braseros, por fuera de los pilares
        for (int z : new int[]{-14, -1, 12}) {
            brasero(-12, y0, z);
            brasero(11, y0, z);
        }
        // las lamparas sobre la alfombra
        for (int z : new int[]{-12, -2, 8, 18}) lampara(-1, z, t, 5);
        // dos armeros vacios junto a la puerta, y los escudos rotos (losas) en el suelo
        for (int x : new int[]{-6, 5}) {
            put(x, y0, 20, Pal.PB_CHISELED);
            put(x, y0 + 1, 20, b(Blocks.LIGHTNING_ROD));
        }
    }

    // ------------------------------------------------------------------ la armeria

    private void armeria() {
        int y0 = pisa(ARMERIA), t = techo(ARMERIA);
        suelo(ARMERIA, (x, z) -> Math.floorMod(x, 6) == 0 || Math.floorMod(z, 6) == 0 ? Pal.PB
                : pct(x, 1, z, 3) < 15 ? Pal.DS_TILES_CRACKED : Pal.DS_TILES);
        // la mesa larga del centro (de oeste a este), con yunques, piedras de afilar y velas
        for (int x = -10; x <= 9; x++) {
            put(x, y0, 0, x % 5 == 0 ? Pal.PB_CHISELED : Pal.PB_BRICKS);
            BlockState encima = switch (Math.floorMod(x, 6)) {
                case 1 -> b(Blocks.DAMAGED_ANVIL);
                case 3 -> b(Blocks.GRINDSTONE);
                case 5 -> vela(Blocks.CANDLE, 3);
                default -> null;
            };
            if (encima != null) put(x, y0 + 1, 0, encima);
            if (Math.floorMod(x, 6) == 5) velas++;
        }
        // la fragua (al noreste): dos altos hornos encendidos, el caldero de lava y la chimenea hasta el techo
        for (int x = 11; x <= 14; x++)
            for (int z = -14; z <= -11; z++) {
                boolean borde = x == 11 || x == 14 || z == -14 || z == -11;
                boolean boca = z == -11 && (x == 12 || x == 13);
                if (!dentro(x, z)) continue;
                for (int y = y0; y <= t; y++) {
                    if (boca && y >= y0 + 1 && y <= y0 + 2) put(x, y, z, Pal.AIR);
                    else if (borde) put(x, y, z, y <= y0 + 3 ? negra(x, y, z) : Pal.DS_BRICKS);
                    else put(x, y, z, y == y0 ? b(Blocks.MAGMA_BLOCK) : Pal.AIR);
                }
            }
        put(12, y0, -10, b(Blocks.BLAST_FURNACE).setValue(net.minecraft.world.level.block.BlastFurnaceBlock.FACING, Direction.SOUTH)
                .setValue(net.minecraft.world.level.block.BlastFurnaceBlock.LIT, true));
        put(13, y0, -10, b(Blocks.BLAST_FURNACE).setValue(net.minecraft.world.level.block.BlastFurnaceBlock.FACING, Direction.SOUTH)
                .setValue(net.minecraft.world.level.block.BlastFurnaceBlock.LIT, true));
        put(10, y0, -10, b(Blocks.LAVA_CAULDRON));
        put(15, y0, -10, b(Blocks.SMITHING_TABLE));
        put(10, y0, -8, b(Blocks.ANVIL));
        // los armeros de las paredes del oeste y del este: postes de piedra con lanzas, espadas (barras) y escudos (trampillas)
        for (int z = -14; z <= 14; z += 2) {
            for (int x : new int[]{-22, 21}) {
                if (!dentro(x, z) || ocupado(ARMERIA, x, z)) continue;
                put(x, y0, z, Pal.PB_CHISELED);
                BlockState arma = switch (Math.floorMod(z / 2, 3)) {
                    case 0 -> b(Blocks.LIGHTNING_ROD);
                    case 1 -> b(Blocks.IRON_BARS);
                    default -> b(Blocks.CHAIN);
                };
                if (arma.is(Blocks.IRON_BARS)) une(x, y0 + 1, z, arma);
                else put(x, y0 + 1, z, arma);
                put(x, y0 + 2, z, b(Blocks.LIGHTNING_ROD));
            }
        }
        // barriles y cofres viejos en el rincon del suroeste; montones de cadenas
        for (int[] p : new int[][]{{-16, 14}, {-15, 14}, {-16, 13}, {-14, 15}})
            if (dentro(p[0], p[1])) put(p[0], y0, p[1], b(Blocks.BARREL));
        for (int[] p : new int[][]{{-12, 8}, {6, -6}, {14, 6}}) {
            put(p[0], y0, p[1], b(Blocks.CHAIN).setValue(net.minecraft.world.level.block.ChainBlock.AXIS, Direction.Axis.X));
            put(p[0] + 1, y0, p[1], b(Blocks.CHAIN).setValue(net.minecraft.world.level.block.ChainBlock.AXIS, Direction.Axis.Z));
        }
        // las lamparas
        for (int[] p : new int[][]{{-12, -8}, {-12, 8}, {10, 8}, {-1, 0}}) lampara(p[0], p[1], t, 4);
        // vigas de roble oscuro bajo el techo (de norte a sur), cada ocho
        for (int x = -16; x <= 16; x += 8)
            for (int z = -LADO; z <= LADO - 1; z++)
                if (dentro(x, z) && !enTramo(ARMERIA, x, z, 0) && !enHueco(BAJA, x, z, 0) && !enHueco(ARMERIA, x, z, 1))
                    put(x, t, z, Subida.tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Z));
    }

    // ------------------------------------------------------------------ el archivo

    private static final int[] FILAS_ARCHIVO = {-15, -11, -7, 7, 11, 15};

    private void archivo() {
        int y0 = pisa(ARCHIVO), t = techo(ARCHIVO);
        suelo(ARCHIVO, (x, z) -> Math.floorMod(x, 8) == 0 || Math.floorMod(z, 8) == 0 ? b(Blocks.SPRUCE_PLANKS) : b(Blocks.DARK_OAK_PLANKS));
        // las filas de estanterias (de oeste a este), a los dos lados del pasillo del centro; de tres de alto, con una vela encima
        for (int z : FILAS_ARCHIVO)
            for (int x = -18; x <= 17; x++) {
                if (x >= -5 && x <= 4) continue;
                if (!dentro(x, z) || ocupado(ARCHIVO, x, z)) continue;
                for (int y = y0; y <= y0 + 2; y++) put(x, y, z, pct(x, y, z, 4) < 25 ? b(Blocks.CHISELED_BOOKSHELF) : b(Blocks.BOOKSHELF));
                if (Math.floorMod(x, 5) == 0) {
                    put(x, y0 + 3, z, vela(Blocks.CANDLE, 1 + Math.floorMod(x, 3)));
                    velas++;
                }
                estanterias++;
            }
        // las paredes del oeste y del este, forradas de estanterias de cinco de alto
        for (int z = -14; z <= 14; z++)
            for (int x : new int[]{-23, 22}) {
                if (!dentro(x, z) || ocupado(ARCHIVO, x, z)) continue;
                for (int y = y0; y <= y0 + 4; y++) put(x, y, z, b(Blocks.BOOKSHELF));
                estanterias++;
            }
        // las mesas de lectura del centro: losas sobre vallas, con velas y libros (atriles vacios)
        for (int[] m : new int[][]{{-4, -3}, {3, -3}, {-4, 3}, {3, 3}}) {
            for (int dx = 0; dx <= 1; dx++) {
                une(m[0] + dx - (m[0] > 0 ? 1 : 0), y0, m[1], b(Blocks.DARK_OAK_FENCE));
                put(m[0] + dx - (m[0] > 0 ? 1 : 0), y0 + 1, m[1], losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
            }
            put(m[0], y0 + 2, m[1], vela(Blocks.WHITE_CANDLE, 2));
            velas++;
        }
        // la gran mesa del archivero, al norte del pasillo, donde esta el atril del diario de la reina (en trozos())
        for (int x = -3; x <= 2; x++) {
            put(x, y0, -9, Pal.PB_BRICKS);
            put(x, y0 + 1, -9, losa(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, SlabType.BOTTOM));
        }
        put(-3, y0 + 1, -9, vela(Blocks.RED_CANDLE, 3));
        put(2, y0 + 1, -9, vela(Blocks.RED_CANDLE, 3));
        velas += 2;
        // las lamparas sobre el pasillo y entre las filas
        for (int z : new int[]{-13, -9, -1, 5, 13}) lampara(-1, z, t, 4);
        for (int z : new int[]{-13, -9, 9, 13}) {
            lampara(-12, z, t, 5);
            lampara(11, z, t, 5);
        }
    }

    // ------------------------------------------------------------------ la prision

    /** Las celdas: {z0, z1} de cada una (a los dos lados: oeste de x -22 a -17, este de 16 a 21; los barrotes en x -16 y 15). */
    private static final int[][] CELDAS = {{-14, -11}, {-9, -6}, {-4, -1}, {1, 4}, {6, 9}, {11, 14}};
    static final int CELDA_O0 = -22, CELDA_O1 = -17, REJA_O = -16, CELDA_E0 = 16, CELDA_E1 = 21, REJA_E = 15;

    private void prision() {
        int y0 = pisa(PRISION), t = techo(PRISION);
        suelo(PRISION, (x, z) -> {
            int q = pct(x, 2, z, 5);
            return q < 40 ? Pal.DS_COBBLED : q < 62 ? b(Blocks.MOSSY_COBBLESTONE) : q < 80 ? Pal.BLACKSTONE : q < 90 ? b(Blocks.SOUL_SOIL) : Pal.DS_TILES_CRACKED;
        });
        for (int lado = 0; lado < 2; lado++) {
            int x0 = lado == 0 ? CELDA_O0 : CELDA_E0, x1 = lado == 0 ? CELDA_O1 : CELDA_E1, reja = lado == 0 ? REJA_O : REJA_E;
            // los muros entre celdas y el de cada extremo, de seis de alto, con su tejado de losas
            for (int z = -15; z <= 15; z++) {
                boolean muro = z == -15 || z == 15 || z == -10 || z == -5 || z == 0 || z == 5 || z == 10;
                for (int x = Math.min(x0, reja); x <= Math.max(x1, reja); x++) {
                    if (!dentro(x, z)) continue;
                    for (int y = y0; y <= y0 + 5; y++) {
                        if (muro) put(x, y, z, negra(x, y, z));
                        else if (x == reja) {
                            // los barrotes de delante; en algunas, la puerta abierta (falta un trozo)
                            int celda = celdaDe(z);
                            boolean abierta = celda >= 0 && (celda + lado) % 3 == 0 && y <= y0 + 1 && (z == CELDAS[celda][0] + 1 || z == CELDAS[celda][0] + 2);
                            if (abierta) put(x, y, z, Pal.AIR);
                            else une(x, y, z, y == y0 + 5 ? b(Blocks.POLISHED_BLACKSTONE_WALL) : b(Blocks.IRON_BARS));
                        }
                    }
                    put(x, y0 + 6, z, losa(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, SlabType.BOTTOM));
                }
            }
            // lo de dentro de cada celda: cadenas que cuelgan, una calavera, telaranas y el banco de piedra
            for (int[] c : CELDAS) {
                int fondo = lado == 0 ? x0 : x1;
                if (!dentro(fondo, c[0]) || !dentro(fondo, c[1])) continue;
                for (int y = y0 + 2; y <= y0 + 5; y++) put(fondo, y, c[0] + 1, b(Blocks.CHAIN));
                put(fondo, y0, c[1] - 1, losa(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.BOTTOM));
                put(fondo + (lado == 0 ? 1 : -1), y0, c[1], losa(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.BOTTOM));
                put(fondo, y0 + 5, c[1], b(Blocks.COBWEB));
                put(fondo + (lado == 0 ? 2 : -2), y0, c[0] + 2, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, lado == 0 ? 4 : 12));
                celdas++;
            }
        }
        // en el centro: la hoguera de almas en su pozo, el potro y las jaulas que cuelgan del techo
        for (int x = -2; x <= 1; x++)
            for (int z = -2; z <= 1; z++) {
                boolean borde = x == -2 || x == 1 || z == -2 || z == 1;
                put(x, SUELO[PRISION], z, borde ? Pal.PB_BRICKS : b(Blocks.SOUL_SOIL));
                if (!borde) put(x, y0, z, b(Blocks.SOUL_FIRE));
                else une(x, y0, z, b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            }
        braseros++;
        // el potro (al este del pozo)
        for (int z = -1; z <= 0; z++) {
            put(6, y0, z, Subida.tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.X));
            put(7, y0, z, Subida.tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.X));
        }
        put(5, y0, -1, b(Blocks.DARK_OAK_FENCE));
        put(5, y0 + 1, -1, b(Blocks.CHAIN));
        put(8, y0, 0, b(Blocks.DARK_OAK_FENCE));
        put(8, y0 + 1, 0, b(Blocks.CHAIN));
        // las jaulas: de barrotes, colgando de cadenas, con huesos dentro
        for (int[] j : new int[][]{{-7, -9}, {-7, 8}, {6, -10}, {6, 9}}) {
            for (int y = t; y >= t - 4; y--) put(j[0], y, j[1], b(Blocks.CHAIN));
            put(j[0], t - 5, j[1], losa(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.BOTTOM));
            for (int y = t - 8; y <= t - 6; y++) une(j[0], y, j[1], b(Blocks.IRON_BARS));
            put(j[0], t - 9, j[1], losa(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.TOP));
        }
        // las lamparas
        for (int[] p : new int[][]{{-10, -12}, {-10, 12}, {9, -12}, {9, 12}, {-1, -14}, {-1, 14}}) lampara(p[0], p[1], t, 3);
    }

    /** La celda en la que cae z (o -1). */
    private static int celdaDe(int z) {
        for (int i = 0; i < CELDAS.length; i++) if (z >= CELDAS[i][0] && z <= CELDAS[i][1]) return i;
        return -1;
    }

    // ------------------------------------------------------------------ la capilla negra

    private void capilla() {
        int y0 = pisa(CAPILLA), t = techo(CAPILLA);
        suelo(CAPILLA, (x, z) -> Math.floorMod(x + z, 2) == 0 ? Pal.PB : Pal.DS_TILES);
        // la nave: alfombra negra del sur al altar
        for (int z = -13; z <= 18; z++)
            for (int x = -2; x <= 1; x++) put(x, y0, z, b(Blocks.BLACK_CARPET));
        // el estrado del altar, al norte (un escalon), y el altar con sus velas negras y rojas y la gema
        for (int x = -7; x <= 6; x++)
            for (int z = -19; z <= -14; z++) {
                if (ocupado(CAPILLA, x, z)) continue;
                put(x, y0, z, z == -14 ? escalon(Blocks.POLISHED_BLACKSTONE_STAIRS, Direction.NORTH) : losa(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.TOP));
            }
        for (int x = -3; x <= 2; x++) {
            put(x, y0 + 1, -17, x == -3 || x == 2 ? Pal.PB_CHISELED : Pal.PB_BRICKS);
            if (x == -2 || x == 1) put(x, y0 + 2, -17, vela(Blocks.RED_CANDLE, 4));
            else if (x == -1 || x == 0) put(x, y0 + 2, -17, vela(Blocks.BLACK_CANDLE, 3));
            velas++;
        }
        // tras el altar: el retablo (una pared negra con la marca en gemas)
        for (int x = -4; x <= 3; x++)
            for (int y = y0 + 1; y <= y0 + 8; y++) {
                int dx = Math.min(x + 4, 3 - x);
                if (y > y0 + 5 + dx) continue;
                put(x, y, -19, y == y0 + 4 && (x == -1 || x == 0) || y == y0 + 6 && (x == -2 || x == 1) ? gema() : negra(x, y, -19));
            }
        // los bancos: filas de escalones de roble oscuro mirando al altar (el respaldo al sur), a los dos lados de la nave
        for (int z : new int[]{-11, -8, -5, 6, 9, 12})
            for (int x = -10; x <= 9; x++) {
                if (x >= -3 && x <= 2) continue;
                if (!dentro(x, z) || ocupado(CAPILLA, x, z)) continue;
                put(x, y0, z, escalon(Blocks.DARK_OAK_STAIRS, Direction.SOUTH));
                bancos++;
            }
        // candelabros altos en los lados de la nave y velas por los rincones
        for (int z : new int[]{-12, -3, 4, 14})
            for (int x : new int[]{-14, 13}) {
                une(x, y0, z, b(Blocks.POLISHED_BLACKSTONE_WALL));
                une(x, y0 + 1, z, b(Blocks.POLISHED_BLACKSTONE_WALL));
                put(x, y0 + 2, z, vela(Blocks.BLACK_CANDLE, 4));
                velas++;
            }
        // el ultimo santuario, en el centro, con su corro de losas y dos farolas de almas
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) > 3) continue;
                put(SANT_X + dx, SANT_Y, SANT_Z + dz, Math.abs(dx) + Math.abs(dz) == 3 ? Pal.BASALT : Pal.PB);
                if (Math.abs(dx) + Math.abs(dz) <= 3) put(SANT_X + dx, y0, SANT_Z + dz, Pal.AIR);
            }
        put(SANT_X, y0, SANT_Z, santuarioBloque());
        for (int dx : new int[]{-3, 3}) {
            put(SANT_X + dx, y0, SANT_Z - 2, Pal.PB_CHISELED);
            une(SANT_X + dx, y0 + 1, SANT_Z - 2, b(Blocks.POLISHED_BLACKSTONE_WALL));
            put(SANT_X + dx, y0 + 2, SANT_Z - 2, b(Blocks.SOUL_LANTERN));
            faroles++;
        }
        // banderolas negras en las paredes y lamparas
        for (int z = -10; z <= 10; z += 5) {
            put(-LADO, y0 + 6, z, banderola(Direction.EAST, false));
            put(LADO - 1, y0 + 6, z, banderola(Direction.WEST, false));
        }
        for (int[] p : new int[][]{{-8, -10}, {7, -10}, {-8, 9}, {7, 9}, {-1, -6}}) lampara(p[0], p[1], t, 4);
    }

    // ------------------------------------------------------------------ el salon del trono

    private static final int[][] PARES_TRONO = {{-15, -14}, {-7, -6}, {1, 2}, {9, 10}};

    private void trono() {
        int y0 = pisa(TRONO), t = techo(TRONO);
        suelo(TRONO, (x, z) -> {
            int dx = Math.abs(x), dz = Math.abs(z - TRONO_Z / 2);
            if ((dx + dz) % 6 == 0) return Pal.GILDED;
            return Math.floorMod(x + z, 2) == 0 ? Pal.PB_BRICKS : Pal.PB;
        });
        // la alfombra roja de la escalera al estrado
        for (int z = -18; z <= 12; z++)
            for (int x = -1; x <= 1; x++) put(x, y0, z, b(Blocks.RED_CARPET));
        // los pilares (dos filas) con lamparas entre ellos
        for (int[] p : PARES_TRONO) {
            pilar(-10, p[0], y0, t);
            pilar(9, p[0], y0, t);
        }
        // el estrado al sur: un escalon y la tarima, con el trono en medio
        for (int x = -6; x <= 6; x++)
            for (int z = 13; z <= 20; z++) {
                if (!dentro(x, z)) continue;
                put(x, y0, z, z == 13 ? escalon(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.SOUTH) : Pal.PB_BRICKS);
            }
        int ts = y0 + 1;                                                   // lo que se pisa en la tarima
        // el trono: el asiento (de cara al norte), los brazos y el respaldo altisimo con la corona de gemas
        put(TRONO_X, ts, TRONO_Z, Pal.AIR);                                // (el asiento: flota sentada en el)
        une(TRONO_X - 1, ts, TRONO_Z, b(Blocks.POLISHED_BLACKSTONE_WALL));
        une(TRONO_X + 1, ts, TRONO_Z, b(Blocks.POLISHED_BLACKSTONE_WALL));
        for (int x = TRONO_X - 1; x <= TRONO_X + 1; x++)
            for (int y = ts; y <= ts + 6; y++) {
                boolean centro = x == TRONO_X;
                if (y > ts + 4 && !centro) continue;
                put(x, y, TRONO_Z + 1, y == ts + 6 ? gema() : y == ts + 5 || y == ts + 4 && !centro ? Pal.GILDED : negra(x, y, TRONO_Z + 1));
            }
        put(TRONO_X - 1, ts + 5, TRONO_Z + 1, gema());
        put(TRONO_X + 1, ts + 5, TRONO_Z + 1, gema());
        // la cortina roja detras del trono y las banderolas
        for (int x = -5; x <= 5; x++)
            for (int y = ts; y <= ts + 7; y++)
                if (Math.abs(x) >= 2 || y > ts + 6) put(x, y, TRONO_Z + 3, b(Blocks.RED_WOOL));
        // los candelabros del estrado
        for (int x : new int[]{-5, 5}) {
            une(x, ts, 14, b(Blocks.POLISHED_BLACKSTONE_WALL));
            une(x, ts + 1, 14, b(Blocks.POLISHED_BLACKSTONE_WALL));
            put(x, ts + 2, 14, vela(Blocks.RED_CANDLE, 4));
            velas++;
        }
        // braseros a los lados de la alfombra
        for (int z = -14; z <= 10; z += 8) {
            brasero(-4, y0, z);
            brasero(4, y0, z);
        }
        // los espejos rotos de la reina en las paredes del oeste y del este (vidrio tintado con marco de oro; a algunos les falta)
        for (int z : new int[]{-11, -3, 5}) espejo(-LADO, z, y0);
        for (int z : new int[]{-14, -6}) espejo(LADO - 1, z, y0);
        // las lamparas grandes
        for (int z : new int[]{-12, -4, 4}) lampara(0, z, t, 4);
        for (int z : new int[]{-12, 4}) {
            lampara(-7, z, t, 3);
            lampara(7, z, t, 3);
        }
        // la reja de la escalera de la azotea (no se abre hasta que cae la reina: CasaSombra)
        for (BlockPos p : rejaAzotea()) une(p.getX(), p.getY(), p.getZ(), b(Blocks.IRON_BARS));
        // y el marco de piedra de esa puerta
        for (int x = 18; x <= LADO - 1; x++) put(x, y0 + 3, 13, Pal.PB_CHISELED);
        put(18, y0 + 3, 12, Pal.PB_CHISELED);
        put(18, y0 + 3, 11, Pal.PB_CHISELED);
    }

    /** Un espejo de la reina: tres de ancho y cinco de alto, el marco de oro y el vidrio tintado (con huecos: esta roto). */
    private void espejo(int x, int z, int y0) {
        for (int dz = -2; dz <= 2; dz++)
            for (int y = y0 + 1; y <= y0 + 7; y++) {
                if (!dentro(x, z + dz)) continue;
                boolean marco = Math.abs(dz) == 2 || y == y0 + 1 || y == y0 + 7;
                if (marco) put(x, y, z + dz, Pal.GILDED);
                else put(x, y, z + dz, pct(x, y, z + dz, 7) < 22 ? Pal.PB : b(Blocks.TINTED_GLASS));
            }
        espejos++;
    }

    /** Los barrotes de la puerta de la escalera de la azotea: por el sur (z 13) y por el lado de los primeros peldanos (x 18). */
    public static List<BlockPos> rejaAzotea() {
        List<BlockPos> l = new ArrayList<>();
        int y0 = pisa(TRONO);
        for (int y = y0; y <= y0 + 2; y++) {
            for (int x = 19; x <= LADO - 1; x++) l.add(new BlockPos(x, y, 13));
            for (int z = 11; z <= 13; z++) l.add(new BlockPos(18, y, z));
        }
        return l;
    }

    // ------------------------------------------------------------------ la azotea

    /** La puerta de la aguja (cerrada hasta la parte 7ac): en su cara del sur. */
    public static final int AGUJA_Z = 10;

    private void azotea() {
        int y0 = pisa(AZOTEA);
        // la puerta de la aguja: un arco cegado de piedra negra con las runas rojas (cerrada: la Sombra, parte 7ac)
        for (int x = -2; x <= 1; x++)
            for (int z = 9; z <= 11; z++)
                for (int y = y0; y <= y0 + 5; y++) {
                    double rho = Math.hypot(x + 0.5, z + 0.5);
                    if (rho <= Ciudadela.RAG - 2 || rho > Ciudadela.RAG + 0.5) continue;
                    boolean hueco = x >= -1 && x <= 0 && y <= y0 + 3;
                    boolean marco = !hueco && (x == -2 || x == 1 || y == y0 + 4);
                    if (hueco) put(x, y, z, z == 9 ? b(Blocks.CRYING_OBSIDIAN) : y == y0 + 2 ? gema() : Pal.PB_CHISELED);
                    else if (marco) put(x, y, z, Pal.GILDED);
                }
        // dos braseros a los lados de la puerta
        brasero(-4, y0, 13);
        brasero(3, y0, 13);
        // la barandilla del hueco de la escalera ya la pone escalera(5)
    }

    // ------------------------------------------------------------------ el atril y los arcones

    private void trozos() {
        int y = pisa(ARCHIVO);
        // el atril con el diario de la reina, en la gran mesa del archivero (de cara al sur, al pasillo)
        trozo(-1, y, -8, c -> c.lectern(-1, y, -8, Direction.SOUTH, Perdidos.libro(LIBRO)));
        // el arcon de la armeria, el del archivo y el tesoro de la reina (tras el trono)
        int ya = pisa(ARMERIA);
        trozo(-17, ya, 14, c -> c.chest(-17, ya, 14, Direction.NORTH, BOTIN));
        trozo(20, y, -15, c -> c.chest(20, y, -15, Direction.WEST, BOTIN));
        int yt = pisa(TRONO) + 1;
        trozo(3, yt, TRONO_Z + 2, c -> c.chest(3, yt, TRONO_Z + 2, Direction.NORTH, TESORO));
    }

    private void trozo(int x, int y, int z, Trozo.Obra o) {
        elementos.add(new Trozo(6, x, y, z, x, y, z, o));
    }

    // ------------------------------------------------------------------ los sitios

    private void sitio(String nombre, int x, int y, int z) {
        sitios.put(nombre, new int[]{x, y, z});
    }

    private void ponerSitios() {
        sitio("patio", 0, pisa(BAJA), Subida.Z_PUERTA - 7);
        sitio("camino", 0, pisa(BAJA), 45);
        sitio("puerta", -1, pisa(BAJA), PZ_FUERA + 2);
        sitio("vestibulo", -1, pisa(BAJA), PZ_DENTRO - 3);
        sitio("salon", -1, pisa(BAJA), 0);
        sitio("escalera_baja", 10, pisa(BAJA), -21);
        sitio("armeria", -1, pisa(ARMERIA), -6);
        sitio("arcon_armeria", -17, pisa(ARMERIA), 13);
        sitio("escalera_armeria", -9, pisa(ARMERIA), 20);
        sitio("archivo", -1, pisa(ARCHIVO), 0);
        sitio("atril", -1, pisa(ARCHIVO), -7);
        sitio("arcon_archivo", 19, pisa(ARCHIVO), -15);
        sitio("escalera_archivo", 10, pisa(ARCHIVO), -21);
        sitio("prision", -1, pisa(PRISION), 6);
        sitio("celda", -19, pisa(PRISION), 2);
        sitio("escalera_prision", -9, pisa(PRISION), 20);
        sitio("capilla", -1, pisa(CAPILLA), 10);
        sitio("santuario", SANT_X, pisa(CAPILLA), SANT_Z + 3);
        sitio("altar", -1, pisa(CAPILLA) + 1, -15);
        sitio("escalera_capilla", 10, pisa(CAPILLA), -21);
        sitio("trono_entrada", -8, pisa(TRONO), -20);
        sitio("sala_trono", 0, pisa(TRONO), -4);
        sitio("trono", TRONO_X, pisa(TRONO) + 1, TRONO_Z - 2);
        sitio("tesoro", 3, pisa(TRONO) + 1, TRONO_Z + 1);
        sitio("reja_azotea", 20, pisa(TRONO), 15);
        sitio("azotea", 20, pisa(AZOTEA), -3);
        sitio("puerta_sombra", -1, pisa(AZOTEA), 13);
        // para mirar de lejos
        sitio("frente", 0, pisa(BAJA) + 6, 62);
    }

    /** Los sitios con nombre: {x, y, z} (y: lo que se pisa). */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    public List<Espera> esperas() {
        return esperas;
    }

    public List<Element> elementos() {
        return elementos;
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    // ------------------------------------------------------------------ los de la casa

    private static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;
    /** El puesto de la reina (el primero: el 0). */
    public static final int ISOLDE = 0;

    private void ponerEsperas() {
        int Re = com.darkcraft.enemigos.Enemigo.REZA, D = com.darkcraft.enemigos.Enemigo.DE_PIE, T = com.darkcraft.enemigos.Enemigo.TUMBADO;
        int b = pisa(BAJA);
        // la reina en su trono (sentada, de cara a quien sube): el puesto 0
        esperas.add(new Espera("isolde", 0, TRONO_X, pisa(TRONO) + 1, TRONO_Z, NORTE, Re, 0, com.darkcraft.enemigos.Puestos.JEFE));
        // el patio: dos armaduras a los lados de la puerta (de pie, quietas) y dos de ronda por el camino
        centinela(0, -6, b, PZ_FUERA + 3, SUR, Re, 0, "patio");
        centinela(1, 5, b, PZ_FUERA + 3, SUR, Re, 0, "patio");
        centinela(0, -8, b, 50, SUR, D, 5, "patio");
        centinela(1, 7, b, 57, SUR, D, 5, "patio");
        // el Salon de la Guardia: armaduras a los lados de la alfombra, de cara a ella (se despiertan al pasar) y dos tiradas
        // como un monton de hierro junto a la escalera
        centinela(0, -6, b, -13, ESTE, Re, 0, "salon");
        centinela(1, 5, b, -7, OESTE, Re, 0, "salon");
        centinela(0, -6, b, -1, ESTE, Re, 0, "salon");
        centinela(1, 5, b, 6, OESTE, Re, 0, "salon");
        centinela(0, -6, b, 12, ESTE, Re, 0, "salon");
        centinela(0, 4, b, -17, SUR, T, 0, "salon");
        centinela(1, -4, b, -18, SUR, T, 0, "salon");
        // la armeria: las armaduras guardadas en dos filas (quietas), una de ronda y otra tirada
        int a = pisa(ARMERIA);
        centinela(0, -17, a, -8, ESTE, Re, 0, "armeria");
        centinela(1, -17, a, 0, ESTE, Re, 0, "armeria");
        centinela(0, -17, a, 8, ESTE, Re, 0, "armeria");
        centinela(1, 16, a, -4, OESTE, Re, 0, "armeria");
        centinela(0, 16, a, 6, OESTE, Re, 0, "armeria");
        centinela(0, 2, a, 8, NORTE, D, 6, "armeria");
        centinela(1, -6, a, -14, SUR, T, 0, "armeria");
        // el archivo: un cortesano leyendo en una mesa (velando), una dama y un cortesano entre las estanterias, y dos armaduras
        // a los lados de la mesa del archivero
        int r = pisa(ARCHIVO);
        esperas.add(new Espera("espectro", 0, -4, r, 2, NORTE, Re, 0, "archivo"));
        esperas.add(new Espera("espectro", 1, -12, r, -9, ESTE, D, 4, "archivo"));
        esperas.add(new Espera("espectro", 0, 11, r, 9, OESTE, D, 4, "archivo"));
        centinela(0, -5, r, -10, SUR, Re, 0, "archivo");
        centinela(1, 4, r, -10, SUR, Re, 0, "archivo");
        // la prision: dos marcados de rodillas en sus celdas, dos perros dormidos junto al pozo y dos carceleros
        int p = pisa(PRISION);
        esperas.add(new Espera("marcado", 0, -19, p, -12, ESTE, Re, 0, "prision"));
        esperas.add(new Espera("marcado", 0, 18, p, 12, OESTE, Re, 0, "prision"));
        esperas.add(new Espera("perro", 0, -4, p, 4, SUR, Re, 0, "prision"));
        esperas.add(new Espera("perro", 0, 3, p, -4, NORTE, Re, 0, "prision"));
        centinela(1, -12, p, 0, ESTE, Re, 0, "prision");
        centinela(0, 10, p, -6, OESTE, D, 6, "prision");
    }

    private void centinela(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {
        esperas.add(new Espera("centinela", variante, x, y, z, giro, postura, ronda, grupo));
    }

    // ------------------------------------------------------------------ escribir un chunk

    /** Si el chunk tiene algo del homenaje. */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < BX0 || x0 > BX1 || z0 + 15 < BZ0 || z0 > BZ1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} del homenaje en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    /** Para la prueba: si estan el atril con su libro, el santuario, los arcones, el rastrillo y la reja de la azotea como tocan. */
    public static String resumen(net.minecraft.server.level.ServerLevel m) {
        Homenaje h = get();
        int[] at = h.sitio("atril");
        boolean libro = m.getBlockEntity(new BlockPos(-1, at[1], -8)) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le
                && Perdidos.de(le.getBook()) == LIBRO;
        boolean santuario = m.getBlockState(new BlockPos(SANT_X, pisa(CAPILLA), SANT_Z)).is(DarkCraftMod.SANTUARIO.get());
        int arcones = 0;
        for (BlockPos q : new BlockPos[]{new BlockPos(-17, pisa(ARMERIA), 14), new BlockPos(20, pisa(ARCHIVO), -15), new BlockPos(3, pisa(TRONO) + 1, TRONO_Z + 2)})
            if (m.getBlockState(q).is(Blocks.CHEST)) arcones++;
        int barrotes = 0, reja = 0;
        for (BlockPos q : rastrillo()) if (m.getBlockState(q).is(Blocks.IRON_BARS)) barrotes++;
        for (BlockPos q : rejaAzotea()) if (m.getBlockState(q).is(Blocks.IRON_BARS)) reja++;
        return "atril con el libro " + LIBRO + " " + (libro ? "SI" : "NO") + ", santuario " + (santuario ? "SI" : "NO") + ", arcones " + arcones
                + "/3, rastrillo " + barrotes + "/" + rastrillo().size() + " barrotes, reja de la azotea " + reja + "/" + rejaAzotea().size();
    }
}
