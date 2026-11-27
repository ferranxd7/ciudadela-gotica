package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Calcinado;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Puestos;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * El Teatro Hundido (parte 7y): el teatro de la corte de Morvain, al este de la Capital, excavado en la llanura (se ve de lejos
 * solo el muro del fondo del escenario, que asoma seis bloques). u = x - {@link #CX}, v = z - {@link #CZ}; el norte (v
 * negativa) es el escenario, el sur la grada.
 * <ul>
 * <li>La <b>grada</b> (cavea): diez filas de asientos en semicirculo, de la orquesta (suelo {@link #ORQ}) al suelo de la
 * llanura, con cinco escaleras; un trozo hundido en escombros; arriba, el paseo con lo que queda del portico. Al sur, la
 * entrada y el camino del santuario del Teatro.</li>
 * <li>La <b>orquesta</b> (el circulo de abajo) y los dos <b>pasos</b> que bajan a ella por los lados, entre la grada y el
 * escenario.</li>
 * <li>El <b>escenario</b> (suelo {@link #ESC}), de tablas quemadas, con las candilejas que siguen encendidas, y el <b>muro de
 * la escena</b> (frons scaenae): columnas, cornisa, las dos mascaras (la que rie y la que llora), la corona del rey y tres
 * puertas. La del centro, la puerta real, esta <b>tapiada</b>: es un <b>muro ilusorio</b> ({@link MurosIlusorios}): se deshace
 * al golpearlo. Por las de los lados, los bastidores (el atril con el <b>libro perdido 32</b> y un arcon).</li>
 * <li>Detras de la puerta real, un pasillo que baja a la <b>Sala de las Mascaras</b> (bajo la llanura, redonda, suelo
 * {@link #SALA}): el suelo y las paredes en rombos de arlequin, mascaras por las paredes, un publico de calaveras en
 * estacas, lamparas colgadas; al fondo, el escenario pequeno con su telon rojo y el trono de Pell, el Bufon del Rey (que
 * espera en medio de la sala). Detras del trono, otro <b>muro ilusorio</b>: el camerino, con el arcon bueno.</li>
 * </ul>
 * Se dibuja chunk a chunk al adornar; en su caja {@link Adorno} no pone nada. Los calcinados (la gente que ardio en la grada la
 * noche del cielo rojo) esperan en la grada, el escenario, los pasos y los bastidores; Pell, en su sala.
 */
public final class Teatro {
    public static final long SEMILLA = 0x7EA7_20B0_F0E1L;
    /** El centro de la orquesta. */
    public static final int CX = 3670, CZ = 712;
    /** El suelo de la llanura (lo que se pisa esta en {@link #PISO}). */
    public static final int SUELO = 96, PISO = SUELO + 1;
    /** El suelo de la orquesta, el del escenario, el de la sala y su techo (bloques: lo que se pisa esta encima). */
    public static final int ORQ = 86, ESC = 88, SALA = 82, TECHO = 92;
    /** La grada: de donde empieza a donde acaba (radio) y lo hondo de cada fila. */
    static final double R_ORQ = 9.5, R_GRADA1 = 27.5, PASO_FILA = 1.8, R_SUMMA = 31.5, R_LLANO = 35;
    static final int FILAS = 10;
    /** El escenario (|u| y v), el muro de la escena (v) y los pasos de los lados (|u| y v). */
    static final int ESC_U = 20, ESC_V0 = -14, ESC_V1 = -9, FRONS_V0 = -17, FRONS_V1 = -15, ALA_U = 24;
    static final int PASO_U0 = 10, PASO_U1 = 31, PASO_V0 = -7, PASO_V1 = -3;
    /** La Sala de las Mascaras: su centro (v), su radio por dentro y el de la pared. */
    public static final int SALA_V = -44;
    public static final double R_SALA = 13, R_MURO_SALA = 14.5;
    /** El escenario pequeno del fondo de la sala (v desde aqui al norte, |u| hasta aqui) y su suelo. */
    static final int TABLADO_V = -50, TABLADO_U = 7, TABLADO = 84;
    /** El camerino (detras del trono): |u| y v por dentro, su suelo y su techo. */
    static final int CAM_U = 4, CAM_V0 = -64, CAM_V1 = -59, CAM_SUELO = 84, CAM_TECHO = 89;
    /** El santuario del Teatro (en {@link SantuariosMorvain}): al sur de la grada. */
    public static final int SANT_V = 48;
    public static final int X0 = CX - 37, X1 = CX + 37, Z0 = CZ - 68, Z1 = CZ + 46;

    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/teatro"),
            BOTIN_BUENO = new ResourceLocation(DarkCraftMod.MODID, "chests/teatro_bueno");
    /** Los dos muros ilusorios: la puerta real del muro de la escena y el de detras del trono de Pell. */
    public static final String MURO_PUERTA = "teatro_puerta", MURO_TRONO = "teatro_trono";
    /** El libro perdido que esta en el atril de los bastidores. */
    public static final int LIBRO = 32;

    /** Uno del teatro: donde espera (y: donde tiene los pies), hacia donde mira, su variante, como espera, cuanto ronda y su grupo. */
    public record Espera(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    private static Teatro unico;

    public static synchronized Teatro get() {
        if (unico == null) unico = new Teatro();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    /** Lo que hay (para la prueba y las vistas). */
    public int candilejas, columnas, mascaras, espectadores, lamparas, velas;

    private Teatro() {
        elementos.add(new Obra());
        Pilotes.Lista de = new Pilotes.Lista();
        escena(de);
        sala(de);
        camerino(de);
        bastidores(de);
        superficie(de);
        elementos.add(de.pieza(4));
        trozos();
        // ---- los sitios con nombre
        sitio("entrada", 0, PISO, 40);
        sitio("summa", 0, PISO, 29);
        sitio("orquesta", 0, ORQ + 1, 0);
        sitio("escena", 0, ESC + 1, -11);
        sitio("puerta", 0, ESC + 1, -14);
        sitio("pasillo", 0, ESC + 1, -18);
        sitio("sala", 0, SALA + 1, -34);
        sitio("pell", 0, SALA + 1, SALA_V);
        sitio("trono", 1, TABLADO + 1, -56);
        sitio("camerino", 0, CAM_SUELO + 1, -61);
        sitio("atril", 9, ESC + 1, -21);
        sitio("arcon", -9, ESC + 1, -21);
        sitio("arcon_bueno", 0, CAM_SUELO + 1, -63);
        sitio("paso_este", 24, yPaso(24) + 1, -5);
        sitio("paso_oeste", -24, yPaso(24) + 1, -5);
        sitio("alto", 26, PISO + 34, 58);
        // ---- los puestos
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo del teatro (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en lo del teatro: ahi {@link Adorno} no pone nada. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= X0 && x0 <= X1 && z0 + 15 >= Z0 && z0 <= Z1;
    }

    /** Lo lejos de (x, z) al centro de la orquesta. */
    public static double distancia(double x, double z) {
        return Math.hypot(x - CX - 0.5, z - CZ - 0.5);
    }

    /** Si (x, z) es del teatro (la grada, la orquesta, los pasos, el escenario y su muro): para el nombre del sitio y el logro. */
    public static boolean dentro(double x, double z) {
        double u = x - CX - 0.5, v = z - CZ - 0.5;
        if (v >= -2 && Math.hypot(u, v) < R_SUMMA) return true;
        return v >= FRONS_V0 && v < -1 && Math.abs(u) < PASO_U1 + 1;
    }

    /** Si (x, y, z) esta en la Sala de las Mascaras (o en su camerino). */
    public static boolean enSala(double x, double y, double z) {
        double u = x - CX - 0.5, v = z - CZ - 0.5;
        if (y < SALA - 1 || y > TECHO + 1) return false;
        if (Math.hypot(u, v - SALA_V) < R_MURO_SALA - 0.3) return true;
        return Math.abs(u) < CAM_U + 1.2 && v < CAM_V1 + 1.5 && v > CAM_V0 - 1.2;
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} del teatro en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los del teatro (los apunta {@link MorvainEnemigos}): Pell el primero. */
    public List<Espera> esperas() {
        return esperas;
    }

    /** Los sitios con nombre (la entrada, la orquesta, el escenario, la puerta, la sala, el trono...): {x, y, z} (y: los pies). */
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

    static BlockState escalon(Block k, Direction f) {
        return k.defaultBlockState().setValue(StairBlock.FACING, f).setValue(StairBlock.HALF, Half.BOTTOM);
    }

    static BlockState losa(Block k, SlabType t) {
        return k.defaultBlockState().setValue(net.minecraft.world.level.block.SlabBlock.TYPE, t);
    }

    static BlockState vela(Block k, int n) {
        return k.defaultBlockState().setValue(CandleBlock.CANDLES, Math.max(1, Math.min(4, n))).setValue(CandleBlock.LIT, true);
    }

    private static void put(Pilotes.Lista m, int u, int y, int v, BlockState s) {
        m.put(x(u), y, z(v), s);
    }

    private static void une(Pilotes.Lista m, int u, int y, int v, BlockState s) {
        m.une(x(u), y, z(v), s);
    }

    static int pct(int u, int y, int v, long sem) {
        return Hash.pct(x(u), y, z(v), SEMILLA + sem);
    }

    /** El angulo (grados) de (u, v) desde el sur (v positiva) hacia el este. */
    static double angulo(double u, double v) {
        return Math.toDegrees(Math.atan2(u, v));
    }

    /** La fila de la grada a ese radio (0 la de abajo, 9 la de arriba). */
    static int fila(double r) {
        return Math.max(0, Math.min(FILAS - 1, (int) Math.floor((r - R_ORQ) / PASO_FILA)));
    }

    /** Lo alto (el bloque de arriba) de la fila k. */
    static int altoFila(int k) {
        return ORQ + 1 + k;
    }

    /** Si (u, v) es de la grada. */
    static boolean grada(int u, int v) {
        double r = Math.hypot(u, v);
        return v >= -1 && r >= R_ORQ && r < R_GRADA1;
    }

    /** Si (u, v) cae en una de las cinco escaleras de la grada. */
    static boolean escaleraGrada(int u, int v) {
        double r = Math.hypot(u, v), a = angulo(u, v), w = Math.toDegrees(1.25 / Math.max(r, 1));
        for (double e : new double[]{-62, -26, 0, 26, 62}) if (Math.abs(a - e) < w) return true;
        return false;
    }

    /** El trozo de la grada que se hundio: escombros en vez de asientos. */
    static boolean hundido(int u, int v) {
        double a = angulo(u, v);
        return a > 38 && a < 53 && fila(Math.hypot(u, v)) >= 3;
    }

    /** El suelo de los pasos de los lados (sube del suelo de la orquesta al de la llanura, un bloque cada dos). */
    static int yPaso(int au) {
        return ORQ + Math.max(0, Math.min(10, (au - PASO_U0) / 2));
    }

    /** Si (u, v) es de un paso (sin sus muros). */
    static boolean paso(int u, int v) {
        int au = Math.abs(u);
        return au >= PASO_U0 && au <= PASO_U1 && v >= PASO_V0 && v <= PASO_V1;
    }

    /** El suelo del pasillo de la puerta real a la sala (v): llano tras el muro, la escalera y llano otra vez. */
    static int yPasillo(int v) {
        if (v >= -19) return ESC;
        if (v >= -25) return 107 + v;
        return SALA;
    }

    /** Lo alto del muro de la escena en u (roto: unas columnas mas bajas que otras; mas alto en el centro). */
    static int altoFrons(int u) {
        int au = Math.abs(u);
        int h = 101 + (au <= 4 ? 2 : au <= 8 ? 1 : 0) - (au >= 17 ? 2 : 0);
        int p = Hash.pct(au, 0, 7, SEMILLA + 9);
        if (au > 4 && p < 38) h -= 1 + p % 3;
        return h;
    }

    /** Si en u hay columna delante del muro de la escena. */
    static boolean columna(int u) {
        int au = Math.abs(u);
        return au == 4 || au == 12 || au == 16 || au == 20;
    }

    /** Donde se rompio cada columna (la de abajo: lo alto de lo que queda; 95 = entera). */
    static int altoColumna(int u) {
        int p = Hash.pct(u, 1, 3, SEMILLA + 13);
        return p < 30 ? 90 + p % 4 : 95;
    }

    /** Lo que dibujan las dos mascaras (5 x 5, de arriba abajo): W blanco, B negro, '.' el muro. */
    private static final String[] RIE = {".WWW.", "WBWBW", "WWWWW", "WBWBW", ".WBW."};
    private static final String[] LLORA = {".WWW.", "WBWBW", "WWWWW", "WWBWW", ".BWB."};
    /** La corona del rey sobre la puerta real (5 x 4): G oro, R gema. */
    private static final String[] CORONA = {"G.G.G", "GGGGG", "GRGRG", ".GGG."};

    /** Lo que hay en la cara del muro de la escena (las mascaras y la corona): el bloque, o null si es el muro de siempre. */
    static BlockState dibujoFrons(int u, int y) {
        // la mascara que rie, al oeste (sobre la puerta de ese lado); la que llora, al este
        if (y >= 93 && y <= 97 && Math.abs(u) >= 6 && Math.abs(u) <= 10) {
            String[] m = u < 0 ? RIE : LLORA;
            char ch = m[97 - y].charAt(u < 0 ? u + 10 : u - 6);
            if (ch == 'W') return b(Blocks.CALCITE);
            if (ch == 'B') return b(Blocks.POLISHED_BLACKSTONE);
            return null;
        }
        if (y >= 94 && y <= 97 && Math.abs(u) <= 2) {
            char ch = CORONA[97 - y].charAt(u + 2);
            if (ch == 'G') return b(Blocks.RAW_GOLD_BLOCK);
            if (ch == 'R') return b(Blocks.RED_NETHER_BRICKS);
        }
        return null;
    }

    /** La piedra del muro de la escena por fuera (la cara que mira a la grada): sillares; arriba, negra del fuego. */
    static BlockState piedraFrons(int u, int y, int v) {
        int p = pct(u, y, v, 21);
        if (y == ESC) return b(Blocks.POLISHED_ANDESITE);
        if (y == 96 && v == FRONS_V1) return p < 80 ? b(Blocks.POLISHED_DIORITE) : b(Blocks.DIORITE);
        if (y >= 98 && p < 40) return p < 26 ? b(Blocks.BLACKSTONE) : b(Blocks.COBBLED_DEEPSLATE);
        return p < 55 ? b(Blocks.STONE_BRICKS) : p < 75 ? b(Blocks.CRACKED_STONE_BRICKS) : p < 87 ? b(Blocks.MOSSY_STONE_BRICKS)
                : p < 95 ? b(Blocks.POLISHED_ANDESITE) : b(Blocks.CALCITE);
    }

    /** Lo que tapia la puerta real: el muro ilusorio (ladrillo de piedra y cascote, como un tapiado deprisa). */
    public static BlockState tapiado(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 31);
        return p < 46 ? b(Blocks.STONE_BRICKS) : p < 74 ? b(Blocks.COBBLESTONE) : p < 90 ? b(Blocks.CRACKED_STONE_BRICKS) : b(Blocks.MOSSY_COBBLESTONE);
    }

    /** La pared de la sala: rombos de arlequin rojos y negros, con la cenefa dorada. */
    public static BlockState paredSala(int x, int y, int z) {
        int u = x - CX, v = z - CZ;
        if (y <= SALA - 1 || y >= TECHO) return b(Blocks.POLISHED_BLACKSTONE);
        if (y == 89) return b(Blocks.GILDED_BLACKSTONE);
        double s = Math.atan2(u, v - SALA_V) * 13.5;
        int k = (int) (Math.floor((s + y) / 2.0) + Math.floor((s - y) / 2.0));
        return (k & 1) == 0 ? b(Blocks.RED_NETHER_BRICKS) : b(Blocks.POLISHED_BLACKSTONE_BRICKS);
    }

    /** Si (u, v) es de la pared de la sala. */
    static boolean muroSala(int u, int v) {
        double rs = Math.hypot(u, v - SALA_V);
        return rs >= R_SALA && rs < R_MURO_SALA;
    }

    /** Las celdas de un muro ilusorio. */
    public static List<BlockPos> muro(String id) {
        List<BlockPos> l = new ArrayList<>();
        if (MURO_PUERTA.equals(id)) {
            for (int u = -1; u <= 1; u++) for (int y = ESC + 1; y <= ESC + 4; y++) l.add(new BlockPos(x(u), y, z(FRONS_V1)));
        } else if (MURO_TRONO.equals(id)) {
            for (int u = -1; u <= 1; u++) for (int y = TABLADO + 1; y <= TABLADO + 3; y++) l.add(new BlockPos(x(u), y, z(-57)));
        }
        return l;
    }

    /** Lo que hay en una celda de un muro ilusorio cuando esta cerrado. */
    public static BlockState bloqueMuro(String id, BlockPos p) {
        return MURO_PUERTA.equals(id) ? tapiado(p.getX(), p.getY(), p.getZ()) : paredSala(p.getX(), p.getY(), p.getZ());
    }

    // ------------------------------------------------------------------ la obra: columna a columna

    /** Todo lo grande, columna a columna: lo que se cava, la grada, la orquesta, los pasos, el escenario y su muro, y lo de abajo. */
    private final class Obra extends Element {
        Obra() {
            super(SEMILLA + 51);
            this.layer = 1;
            bounds(X0, SALA - 4, Z0, X1, 104, Z1);
        }

        @Override
        public void build(Canvas c) {
            for (int x = Math.max(c.minX, x0); x <= Math.min(c.maxX, x1); x++)
                for (int z = Math.max(c.minZ, z0); z <= Math.min(c.maxZ, z1); z++) columna(c, x, z);
        }
    }

    private static void aire(Canvas c, int x, int y0, int y1, int z) {
        for (int y = y0; y <= y1; y++) c.set(x, y, z, Pal.AIR);
    }

    private static void relleno(Canvas c, int x, int y0, int y1, int z, long sem) {
        for (int y = y0; y <= y1; y++) {
            int p = Hash.pct(x, y, z, SEMILLA + sem);
            c.set(x, y, z, p < 60 ? b(Blocks.STONE_BRICKS) : p < 80 ? b(Blocks.COBBLESTONE) : b(Blocks.STONE));
        }
    }

    static void columna(Canvas c, int x, int z) {
        int u = x - CX, v = z - CZ, au = Math.abs(u);
        double r = Math.hypot(u, v);
        // ---- lo de abajo (bajo la llanura): el pasillo, la sala, el camerino y los bastidores
        abajo(c, x, z, u, v);
        // ---- lo de arriba
        if (au <= ESC_U && v >= ESC_V0 && v <= ESC_V1) {
            escenario(c, x, z, u, v);
            return;
        }
        if (au <= ESC_U && v >= FRONS_V0 && v <= FRONS_V1) {
            frons(c, x, z, u, v);
            return;
        }
        if (au > ESC_U && au <= ALA_U && v >= FRONS_V0 && v <= ESC_V1) {
            // las alas del muro de la escena: macizas hasta lo alto, rotas
            int top = 97 + Hash.pct(au, 2, v, SEMILLA + 15) % 3;
            relleno(c, x, ORQ - 4, ESC - 1, z, 16);
            for (int y = ESC; y <= top; y++) c.set(x, y, z, piedraFrons(u, y, v));
            aire(c, x, top + 1, 104, z);
            return;
        }
        if (au >= PASO_U0 && au <= PASO_U1 && v >= PASO_V0 - 1 && v <= PASO_V1 + 1) {
            pasoLado(c, x, z, u, v);
            return;
        }
        // la orquesta (y los rincones entre su circulo, los pasos y el escenario: a su altura)
        if (v >= ESC_V1 + 1 && (r < R_ORQ || au < PASO_U0 && v <= -2)) {
            orquesta(c, x, z, u, v, r);
            return;
        }
        if (grada(u, v)) {
            cavea(c, x, z, u, v, r);
            return;
        }
        if (v >= -1 && r < R_SUMMA) {
            summa(c, x, z, u, v, r);
            return;
        }
        llano(c, x, z, u, v, r);
    }

    /** La llanura de alrededor, a la altura del teatro (y el camino del santuario). */
    private static void llano(Canvas c, int x, int z, int u, int v, double r) {
        boolean cerca = r < R_LLANO || v < 0 && v >= Z0 - CZ && Math.abs(u) <= X1 - CX;
        if (!cerca) return;
        int t = Cerco.alto(x, z);
        if (t > SUELO) aire(c, x, SUELO + 1, t, z);
        for (int y = t + 1; y < SUELO; y++) c.set(x, y, z, b(Blocks.DIRT));
        int p = Hash.pct(x, SUELO, z, SEMILLA + 41);
        // el camino del santuario: tierra apisonada y losas sueltas
        if (Math.abs(u) <= 1 && v >= 31 && v <= SANT_V - 5) {
            c.set(x, SUELO, z, p < 40 ? b(Blocks.COARSE_DIRT) : p < 70 ? b(Blocks.GRAVEL) : b(Blocks.COBBLESTONE));
            return;
        }
        if (t < SUELO || c.get(x, SUELO, z).isAir()) c.set(x, SUELO, z, p < 70 ? b(Blocks.GRASS_BLOCK) : b(Blocks.COARSE_DIRT));
    }

    /** El paseo de arriba de la grada (summa) con los restos del portico. */
    private static void summa(Canvas c, int x, int z, int u, int v, double r) {
        int p = pct(u, SUELO, v, 43);
        relleno(c, x, SUELO - 3, SUELO - 1, z, 44);
        c.set(x, SUELO, z, p < 55 ? b(Blocks.STONE_BRICKS) : p < 75 ? b(Blocks.CRACKED_STONE_BRICKS) : p < 87 ? b(Blocks.MOSSY_STONE_BRICKS)
                : b(Blocks.GRASS_BLOCK));
        aire(c, x, PISO, 102, z);
        double a = angulo(u, v);
        // las columnas del portico, cada 9 grados (rotas a distinta altura)
        if (r >= 29.5 && r < 30.5) {
            double k = Math.round(a / 9.0) * 9.0;
            if (Math.abs(a - k) * Math.PI / 180 * r < 0.55 && Math.abs(k) > 4) {
                int h = 1 + Hash.pct((int) k, 3, 0, SEMILLA + 45) % 5;
                for (int y = PISO; y < PISO + h; y++) c.set(x, y, z, y == PISO ? b(Blocks.CHISELED_STONE_BRICKS) : b(Blocks.POLISHED_DIORITE));
                if (h >= 4) c.set(x, PISO + h, z, losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
            }
        }
        // el murete de atras, con su hueco al sur (la entrada)
        if (r >= 30.5 && Math.abs(u) > 2 && p % 10 < 7) c.unir(x, PISO, z, b(Blocks.STONE_BRICK_WALL));
    }

    /** La grada: diez filas; cada una con su escalon delante (un asiento) y la piedra detras. */
    private static void cavea(Canvas c, int x, int z, int u, int v, double r) {
        int k = fila(r), top = altoFila(k);
        relleno(c, x, top - 4, top - 1, z, 46);
        aire(c, x, top + 1, 102, z);
        int p = pct(u, top, v, 47);
        if (hundido(u, v)) {
            // escombros: piedra suelta y tierra, un poco por debajo de donde estaban los asientos
            int y = top - (p < 35 ? 1 : 0);
            c.set(x, y, z, p < 30 ? b(Blocks.COBBLESTONE) : p < 55 ? b(Blocks.MOSSY_COBBLESTONE) : p < 75 ? b(Blocks.GRAVEL) : b(Blocks.COARSE_DIRT));
            if (y < top) c.set(x, top, z, Pal.AIR);
            if (p % 13 == 0) c.set(x, y + 1, z, b(Blocks.DEAD_BUSH));
            return;
        }
        double fondo = r - R_ORQ - k * PASO_FILA;
        boolean delante = fondo < 0.95;
        boolean escalera = escaleraGrada(u, v);
        if (delante) {
            // el escalon (asiento) mira al escenario: lo alto, atras (hacia fuera)
            Direction f = Math.abs(v) >= Math.abs(u) ? Direction.SOUTH : u > 0 ? Direction.EAST : Direction.WEST;
            Block e = escalera ? Blocks.STONE_BRICK_STAIRS : p < 78 ? Blocks.POLISHED_DIORITE_STAIRS : p < 90 ? Blocks.STONE_BRICK_STAIRS
                    : Blocks.MOSSY_STONE_BRICK_STAIRS;
            c.unir(x, top, z, escalon(e, f));
            return;
        }
        if (escalera) c.set(x, top, z, p < 80 ? b(Blocks.STONE_BRICKS) : b(Blocks.CRACKED_STONE_BRICKS));
        else if (k >= FILAS - 2 && p < 16) c.set(x, top, z, b(Blocks.GRASS_BLOCK));
        else c.set(x, top, z, p < 55 ? b(Blocks.POLISHED_DIORITE) : p < 75 ? b(Blocks.DIORITE) : p < 90 ? b(Blocks.CALCITE) : b(Blocks.STONE_BRICKS));
    }

    /** La orquesta: el circulo de abajo, en anillos de piedra clara y oscura, con el centro dorado. */
    private static void orquesta(Canvas c, int x, int z, int u, int v, double r) {
        relleno(c, x, ORQ - 4, ORQ - 1, z, 48);
        aire(c, x, ORQ + 1, 102, z);
        int p = pct(u, ORQ, v, 49);
        BlockState s;
        if (r < 1.5) s = b(Blocks.CALCITE);
        else if (r < 2.5) s = b(Blocks.GILDED_BLACKSTONE);
        else if (r >= R_ORQ - 1) s = b(Blocks.STONE_BRICKS);
        else s = ((int) Math.floor((r - 2.5) / 2.0) & 1) == 0 ? b(Blocks.POLISHED_DIORITE) : b(Blocks.POLISHED_ANDESITE);
        if (r >= 2.5 && p < 12) s = p < 8 ? b(Blocks.CRACKED_STONE_BRICKS) : b(Blocks.COBBLESTONE);
        c.set(x, ORQ, z, s);
    }

    /** Un paso de los lados (y sus dos muros): baja de la llanura a la orquesta. */
    private static void pasoLado(Canvas c, int x, int z, int u, int v) {
        int au = Math.abs(u), y = yPaso(au);
        if (v >= PASO_V0 && v <= PASO_V1) {
            relleno(c, x, y - 4, y - 1, z, 50);
            boolean sube = au >= PASO_U0 + 2 && (au - PASO_U0) % 2 == 0 && au <= PASO_U0 + 20;
            if (sube) c.set(x, y, z, escalon(Blocks.STONE_BRICK_STAIRS, u > 0 ? Direction.EAST : Direction.WEST));
            else c.set(x, y, z, pct(u, y, v, 51) < 70 ? b(Blocks.STONE_BRICKS) : b(Blocks.POLISHED_ANDESITE));
            aire(c, x, y + 1, 102, z);
            return;
        }
        // los muros: el de la grada (v = -2) sigue las filas; el del escenario (v = -8), a ras del escenario o de la llanura
        int top;
        if (v > PASO_V1) {
            int g = Math.hypot(u, -1) < R_GRADA1 ? altoFila(fila(Math.hypot(u, -1))) : SUELO;
            top = Math.max(y + 1, g);
        } else top = Math.max(y + 1, au <= ESC_U ? ESC : SUELO);
        top = Math.min(top, SUELO);
        relleno(c, x, ORQ - 4, top, z, 52);
        aire(c, x, top + 1, 102, z);
        // un pretil donde el muro llega a la llanura
        if (top >= SUELO && pct(u, SUELO, v, 53) < 70) c.unir(x, PISO, z, b(Blocks.STONE_BRICK_WALL));
    }

    /** El escenario: tablas quemadas sobre la piedra; delante, su frente; las columnas del muro de la escena y su cornisa. */
    private static void escenario(Canvas c, int x, int z, int u, int v) {
        relleno(c, x, ORQ - 4, ESC - 1, z, 54);
        int p = pct(u, ESC, v, 55);
        c.set(x, ESC, z, p < 60 ? b(Blocks.DARK_OAK_PLANKS) : p < 80 ? b(Blocks.SPRUCE_PLANKS)
                : p < 92 ? b(Blocks.STRIPPED_DARK_OAK_LOG).setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.X)
                : b(Blocks.COARSE_DIRT));
        // el frente del escenario (mira a la orquesta): sillares y hornacinas
        if (v == ESC_V1) {
            c.set(x, ORQ, z, b(Blocks.POLISHED_ANDESITE));
            c.set(x, ORQ + 1, z, Math.abs(u) % 4 == 2 ? b(Blocks.CHISELED_STONE_BRICKS) : b(Blocks.STONE_BRICKS));
        }
        aire(c, x, ESC + 1, 104, z);
        if (v != ESC_V0) return;
        // pegado al muro de la escena: las columnas (dos pisos) y la cornisa
        if (columna(u)) {
            int alto = altoColumna(u);
            c.set(x, ESC + 1, z, b(Blocks.CHISELED_STONE_BRICKS));
            for (int y = ESC + 2; y <= alto; y++) c.set(x, y, z, y == 95 ? b(Blocks.CHISELED_STONE_BRICKS) : b(Blocks.POLISHED_DIORITE));
            if (alto >= 95 && altoFrons(u) >= 100) for (int y = 97; y <= 100; y++) c.set(x, y, z, y == 100 ? b(Blocks.CHISELED_STONE_BRICKS) : b(Blocks.CALCITE));
        }
        if (pct(u, 96, v, 57) >= 20 && altoFrons(u) >= 97) c.set(x, 96, z, losa(Blocks.STONE_BRICK_SLAB, SlabType.TOP));
    }

    /** El muro de la escena: tres de grueso, con sus tres puertas (la del centro, tapiada: el muro ilusorio). */
    private static void frons(Canvas c, int x, int z, int u, int v) {
        int au = Math.abs(u), top = altoFrons(u);
        relleno(c, x, ORQ - 4, ESC - 1, z, 58);
        for (int y = ESC; y <= top; y++) {
            // la puerta real (el centro): por dentro hueca; en la cara de fuera, el tapiado
            if (au <= 1 && y >= ESC + 1 && y <= ESC + 4) {
                c.set(x, y, z, v == FRONS_V1 ? tapiado(x, y, z) : Pal.AIR);
                continue;
            }
            // las puertas de los lados (la del oeste, medio cegada de cascotes)
            if (au >= 8 && au <= 9 && y >= ESC + 1 && y <= ESC + 3) {
                if (u < 0 && y == ESC + 1 && v >= FRONS_V0 + 1) c.set(x, y, z, pct(u, y, v, 59) < 60 ? b(Blocks.COBBLESTONE) : b(Blocks.MOSSY_COBBLESTONE));
                else c.set(x, y, z, Pal.AIR);
                continue;
            }
            // los dinteles
            if (v == FRONS_V1 && (au <= 2 && y == ESC + 5 || au >= 7 && au <= 10 && y == ESC + 4)) {
                c.set(x, y, z, b(Blocks.POLISHED_ANDESITE));
                continue;
            }
            if (v == FRONS_V1) {
                BlockState d = dibujoFrons(u, y);
                // dos hornacinas arriba (con un busto): en la cara de fuera
                if (d == null && au == 16 && y >= 97 && y <= 98 && top >= 100) {
                    c.set(x, y, z, Pal.AIR);
                    continue;
                }
                c.set(x, y, z, d != null ? d : piedraFrons(u, y, v));
            } else c.set(x, y, z, pct(u, y, v, 60) < 70 ? b(Blocks.STONE_BRICKS) : b(Blocks.COBBLESTONE));
        }
        // lo de arriba, roto: medias losas sueltas
        if (pct(u, top + 1, v, 61) < 30) c.set(x, top + 1, z, losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
        else c.set(x, top + 1, z, Pal.AIR);
        aire(c, x, top + 2, 104, z);
    }

    /** Lo de debajo de la llanura: el pasillo, la sala (y su tablado), el camerino y los bastidores. */
    private static void abajo(Canvas c, int x, int z, int u, int v) {
        int au = Math.abs(u);
        // ---- el pasillo (de la puerta real a la sala): tres de ancho, cuatro de alto; baja por una escalera
        if (au <= 2 && v >= -29 && v <= FRONS_V0 - 1) {
            int y = yPasillo(v);
            boolean escalon = v <= -20 && v >= -25;
            if (au == 2) {
                for (int k = y; k <= y + 5; k++) {
                    int p = Hash.pct(x, k, z, SEMILLA + 62);
                    c.set(x, k, z, p < 60 ? b(Blocks.STONE_BRICKS) : p < 80 ? b(Blocks.CRACKED_STONE_BRICKS) : b(Blocks.MOSSY_STONE_BRICKS));
                }
                return;
            }
            c.set(x, y - 1, z, b(Blocks.STONE_BRICKS));
            c.set(x, y, z, escalon ? escalon(Blocks.STONE_BRICK_STAIRS, Direction.SOUTH) : b(Blocks.POLISHED_ANDESITE));
            aire(c, x, y + 1, y + 4, z);
            c.set(x, y + 5, z, b(Blocks.STONE_BRICKS));
            // donde baja la escalera, el techo de antes (el de arriba) sigue: lo de en medio, piedra
            return;
        }
        // ---- la sala
        double rs = Math.hypot(u, v - SALA_V);
        if (rs < R_MURO_SALA) {
            boolean puertaSala = au <= 1 && v >= -31 && v <= -30;
            boolean pasoTrono = au <= 1 && v == -58;
            if (rs >= R_SALA) {
                for (int y = SALA - 1; y <= TECHO; y++) {
                    if (puertaSala && y >= SALA + 1 && y <= SALA + 4 || pasoTrono && y >= TABLADO + 1 && y <= TABLADO + 3) c.set(x, y, z, Pal.AIR);
                    else c.set(x, y, z, paredSala(x, y, z));
                }
                if (puertaSala) c.set(x, SALA, z, b(Blocks.POLISHED_BLACKSTONE_BRICKS));
                return;
            }
            c.set(x, SALA - 1, z, b(Blocks.POLISHED_BLACKSTONE));
            boolean tablado = v <= TABLADO_V && au <= TABLADO_U;
            int p = Hash.pct(x, SALA, z, SEMILLA + 63);
            BlockState suelo;
            if (rs >= R_SALA - 1) suelo = b(Blocks.POLISHED_BLACKSTONE_BRICKS);
            else {
                boolean blanco = ((Math.floorDiv(u + v, 3) + Math.floorDiv(u - v, 3)) & 1) == 0;
                suelo = blanco ? (p < 7 ? b(Blocks.DIORITE) : b(Blocks.CALCITE)) : (p < 7 ? b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS) : b(Blocks.POLISHED_BLACKSTONE));
            }
            c.set(x, SALA, z, suelo);
            if (tablado) {
                c.set(x, SALA + 1, z, b(Blocks.POLISHED_BLACKSTONE_BRICKS));
                c.set(x, TABLADO, z, b(Blocks.DARK_OAK_PLANKS));
                aire(c, x, TABLADO + 1, TECHO - 1, z);
            } else aire(c, x, SALA + 1, TECHO - 1, z);
            // el techo: tablas con vigas
            c.set(x, TECHO, z, Math.floorMod(v, 4) == 0 ? b(Blocks.STRIPPED_DARK_OAK_LOG).setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                    Direction.Axis.X) : b(Blocks.DARK_OAK_PLANKS));
            return;
        }
        // ---- el camerino (detras del trono): una salita forrada de madera
        if (au <= CAM_U + 1 && v >= CAM_V0 - 1 && v <= CAM_V1 + 1) {
            boolean borde = au == CAM_U + 1 || v == CAM_V0 - 1 || v == CAM_V1 + 1;
            boolean paso = v == CAM_V1 + 1 && au <= 1;
            for (int y = CAM_SUELO - 1; y <= CAM_TECHO; y++) {
                if (borde && !(paso && y >= CAM_SUELO + 1 && y <= CAM_SUELO + 3)) {
                    boolean esquina = au == CAM_U + 1 && (v == CAM_V0 - 1 || v == CAM_V1 + 1);
                    c.set(x, y, z, esquina ? b(Blocks.STRIPPED_DARK_OAK_LOG) : y == CAM_SUELO - 1 || y == CAM_TECHO ? b(Blocks.POLISHED_BLACKSTONE)
                            : b(Blocks.DARK_OAK_PLANKS));
                } else if (y == CAM_SUELO - 1) c.set(x, y, z, b(Blocks.POLISHED_BLACKSTONE));
                else if (y == CAM_SUELO || y == CAM_TECHO) c.set(x, y, z, b(Blocks.DARK_OAK_PLANKS));
                else c.set(x, y, z, Pal.AIR);
            }
            return;
        }
        // ---- los bastidores (a los lados, detras del muro de la escena): una sala de cada lado
        if (au >= 5 && au <= 13 && v >= -23 && v <= FRONS_V0 - 1) {
            boolean borde = au == 5 || au == 13 || v == -23;
            for (int y = ESC - 1; y <= TECHO; y++) {
                if (borde) {
                    int p = Hash.pct(x, y, z, SEMILLA + 64);
                    c.set(x, y, z, p < 65 ? b(Blocks.STONE_BRICKS) : p < 85 ? b(Blocks.CRACKED_STONE_BRICKS) : b(Blocks.MOSSY_STONE_BRICKS));
                } else if (y == ESC - 1) c.set(x, y, z, b(Blocks.STONE_BRICKS));
                else if (y == ESC) c.set(x, y, z, b(Blocks.SPRUCE_PLANKS));
                else if (y == TECHO) c.set(x, y, z, b(Blocks.SPRUCE_PLANKS));
                else c.set(x, y, z, Pal.AIR);
            }
        }
    }

    // ------------------------------------------------------------------ lo pequeno

    /** En el escenario: los escalones desde la orquesta, las candilejas, las colgaduras rojas y las columnas caidas. */
    private void escena(Pilotes.Lista m) {
        for (int s : new int[]{-1, 1})
            for (int u = 4; u <= 5; u++) {
                put(m, s * u, ORQ + 1, ESC_V1 + 1, escalon(Blocks.STONE_BRICK_STAIRS, Direction.NORTH));
                put(m, s * u, ESC, ESC_V1, escalon(Blocks.STONE_BRICK_STAIRS, Direction.NORTH));
            }
        // las candilejas: velas a lo largo del borde del escenario, que siguen encendidas
        for (int u = -18; u <= 18; u += 3) {
            if (Math.abs(u) == 4 || Math.abs(u) == 5) continue;
            int p = pct(u, ESC + 1, -10, 71);
            put(m, u, ESC + 1, -10, vela(p < 70 ? Blocks.CANDLE : Blocks.RED_CANDLE, 1 + p % 3));
            candilejas++;
        }
        // las colgaduras: lo que queda del telon, en la cara del muro de la escena
        for (int u : new int[]{-18, -14, 14, 18})
            put(m, u, 94, ESC_V0, Blocks.RED_WALL_BANNER.defaultBlockState().setValue(WallBannerBlock.FACING, Direction.SOUTH));
        // los tambores de una columna caida
        for (int[] q : new int[][]{{-10, -12}, {-9, -12}, {13, -11}, {14, -12}}) put(m, q[0], ESC + 1, q[1], b(Blocks.POLISHED_DIORITE));
        for (int u = -6; u <= -3; u++) put(m, u, ORQ + 1, 4, b(Blocks.POLISHED_DIORITE));
        // los bustos de las hornacinas
        for (int s : new int[]{-1, 1}) if (altoFrons(16 * s) >= 100) {
            put(m, 16 * s, 97, FRONS_V1, b(Blocks.POLISHED_ANDESITE));
            put(m, 16 * s, 98, FRONS_V1, b(Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, 0));
        }
        for (int u = -20; u <= 20; u++) if (columna(u)) columnas++;
    }

    /**
     * La Sala de las Mascaras: el trono de Pell en su tablado con el telon rojo, las lamparas colgadas, el publico de calaveras en
     * estacas (mirando al tablado), las mascaras de las paredes (calaveras blancas y negras) y velas por todas partes.
     */
    private void sala(Pilotes.Lista m) {
        // el tablado: su escalera delante, la alfombra y el trono
        for (int u = -1; u <= 1; u++) {
            put(m, u, SALA + 1, TABLADO_V + 1, escalon(Blocks.DARK_OAK_STAIRS, Direction.NORTH));
            put(m, u, TABLADO, TABLADO_V, escalon(Blocks.DARK_OAK_STAIRS, Direction.NORTH));
            for (int v = -54; v <= TABLADO_V - 1; v++) put(m, u, TABLADO + 1, v, b(Blocks.RED_CARPET));
        }
        put(m, 0, TABLADO + 1, -55, escalon(Blocks.DARK_OAK_STAIRS, Direction.NORTH));
        put(m, -1, TABLADO + 1, -55, losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
        put(m, 1, TABLADO + 1, -55, losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
        put(m, 0, TABLADO + 1, -56, b(Blocks.RED_WOOL));
        put(m, 0, TABLADO + 2, -56, b(Blocks.BLACK_WOOL));
        put(m, 0, TABLADO + 3, -56, b(Blocks.RED_WOOL));
        put(m, 0, TABLADO + 4, -56, b(Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, 0));
        // el telon: recogido a los lados y la bambalina arriba
        for (int s : new int[]{-1, 1}) {
            for (int u = 5; u <= TABLADO_U; u++)
                for (int y = TABLADO + 1; y <= TECHO - 1; y++) if (u >= 6 || y >= TECHO - 3) put(m, s * u, y, TABLADO_V, b(Blocks.RED_WOOL));
            put(m, s * 4, TABLADO + 1, -55, vela(Blocks.CANDLE, 4));
            put(m, s * 3, TABLADO + 1, -52, vela(Blocks.RED_CANDLE, 3));
        }
        for (int u = -4; u <= 4; u++) put(m, u, TECHO - 1, TABLADO_V, b(Blocks.RED_WOOL));
        // las lamparas: cuatro y la del centro
        for (int k = 0; k < 4; k++) {
            double a = Math.toRadians(45 + 90 * k);
            int u = (int) Math.round(Math.sin(a) * 7), v = (int) Math.round(SALA_V + Math.cos(a) * 7);
            if (v <= TABLADO_V && Math.abs(u) <= TABLADO_U) continue;
            put(m, u, TECHO - 1, v, b(Blocks.CHAIN).setValue(ChainBlock.AXIS, Direction.Axis.Y));
            put(m, u, TECHO - 2, v, b(Blocks.CHAIN).setValue(ChainBlock.AXIS, Direction.Axis.Y));
            put(m, u, TECHO - 3, v, b(Blocks.LANTERN).setValue(LanternBlock.HANGING, true));
            lamparas++;
        }
        put(m, 0, TECHO - 1, SALA_V, b(Blocks.CHAIN).setValue(ChainBlock.AXIS, Direction.Axis.Y));
        put(m, 0, TECHO - 2, SALA_V, b(Blocks.LANTERN).setValue(LanternBlock.HANGING, true));
        lamparas++;
        // el publico: calaveras en estacas, en el medio circulo de atras, mirando al tablado
        for (int g0 = 26; g0 <= 74; g0 += 12) for (int sg : new int[]{-1, 1}) {
            int g = g0 * sg;
            double a = Math.toRadians(g);
            int u = (int) Math.round(Math.sin(a) * 11), v = (int) Math.round(SALA_V + Math.cos(a) * 11);
            une(m, u, SALA + 1, v, b(Blocks.DARK_OAK_FENCE));
            put(m, u, SALA + 2, v, b(Math.abs(g) % 24 == 2 ? Blocks.WITHER_SKELETON_SKULL : Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, 8));
            espectadores++;
        }
        // las mascaras de las paredes y las velas de su pie
        for (int g = 0; g < 360; g += 20) {
            double a = Math.toRadians(g);
            int u = (int) Math.round(Math.sin(a) * 12.4), v = (int) Math.round(SALA_V + Math.cos(a) * 12.4);
            if (Math.abs(u) <= 2 && v > SALA_V) continue;                  // la puerta
            if (v <= TABLADO_V && Math.abs(u) <= TABLADO_U + 1) continue; // el tablado
            if (muroSala(u, v)) continue;
            Direction f = Math.abs(Math.sin(a)) > Math.abs(Math.cos(a)) ? (Math.sin(a) > 0 ? Direction.WEST : Direction.EAST)
                    : (Math.cos(a) > 0 ? Direction.NORTH : Direction.SOUTH);
            int su = u - f.getStepX(), sv = v - f.getStepZ();
            if (!muroSala(su, sv)) continue;
            Block k = (g / 20) % 2 == 0 ? Blocks.SKELETON_WALL_SKULL : Blocks.WITHER_SKELETON_WALL_SKULL;
            put(m, u, SALA + 5, v, k.defaultBlockState().setValue(WallSkullBlock.FACING, f));
            put(m, u, SALA + 1, v, vela((g / 20) % 3 == 0 ? Blocks.RED_CANDLE : Blocks.WHITE_CANDLE, 1 + (g / 20) % 4));
            mascaras++;
            velas++;
        }
    }

    /** El camerino de Pell: el espejo, el tocador con sus velas, los trajes colgados y las mascaras; el arcon bueno al fondo. */
    private void camerino(Pilotes.Lista m) {
        for (int s : new int[]{-1, 1}) {
            // el tocador y el espejo
            put(m, s * 3, CAM_SUELO + 1, CAM_V0, losa(Blocks.DARK_OAK_SLAB, SlabType.TOP));
            put(m, s * 3, CAM_SUELO + 2, CAM_V0, vela(Blocks.WHITE_CANDLE, 2));
            une(m, s * 3, CAM_SUELO + 3, CAM_V0, b(Blocks.WHITE_STAINED_GLASS_PANE));
            // los trajes colgados: una percha con su ropa y su mascara
            une(m, s * CAM_U, CAM_SUELO + 1, -60, b(Blocks.DARK_OAK_FENCE));
            put(m, s * CAM_U, CAM_SUELO + 2, -60, b(s < 0 ? Blocks.RED_WOOL : Blocks.BLACK_WOOL));
            put(m, s * CAM_U, CAM_SUELO + 3, -60, b(Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, s < 0 ? 12 : 4));
            put(m, s * CAM_U, CAM_SUELO + 3, -62, (s < 0 ? Blocks.SKELETON_WALL_SKULL : Blocks.WITHER_SKELETON_WALL_SKULL).defaultBlockState()
                    .setValue(WallSkullBlock.FACING, s < 0 ? Direction.EAST : Direction.WEST));
        }
        // la alfombra a cuadros rojos y amarillos (como el traje)
        for (int u = -2; u <= 2; u++)
            for (int v = -62; v <= -60; v++) put(m, u, CAM_SUELO + 1, v, b(((u + v) & 1) == 0 ? Blocks.RED_CARPET : Blocks.YELLOW_CARPET));
        put(m, 0, CAM_TECHO - 1, -61, b(Blocks.LANTERN).setValue(LanternBlock.HANGING, true));
    }

    /** Los bastidores: al este el atril con el libro 32 (el programa de la ultima funcion), trajes y un espejo roto; al oeste, trastos y el arcon. */
    private void bastidores(Pilotes.Lista m) {
        // al este
        une(m, 12, ESC + 1, -19, b(Blocks.DARK_OAK_FENCE));
        put(m, 12, ESC + 2, -19, b(Blocks.YELLOW_WOOL));
        une(m, 12, ESC + 1, -21, b(Blocks.DARK_OAK_FENCE));
        put(m, 12, ESC + 2, -21, b(Blocks.RED_WOOL));
        une(m, 7, ESC + 2, -22, b(Blocks.GLASS_PANE));
        put(m, 7, ESC + 1, -22, losa(Blocks.SPRUCE_SLAB, SlabType.TOP));
        put(m, 6, ESC + 1, -22, vela(Blocks.CANDLE, 3));
        put(m, 11, ESC + 1, -22, b(Blocks.BARREL).setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, Direction.UP));
        put(m, 10, TECHO - 1, -20, b(Blocks.LANTERN).setValue(LanternBlock.HANGING, true));
        // al oeste: el caballo de madera de las funciones, los cascotes y las mascaras en un estante
        une(m, -11, ESC + 1, -19, b(Blocks.DARK_OAK_FENCE));
        une(m, -11, ESC + 1, -20, b(Blocks.DARK_OAK_FENCE));
        put(m, -11, ESC + 2, -19, losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
        put(m, -11, ESC + 2, -20, b(Blocks.CARVED_PUMPKIN).setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, Direction.NORTH));
        for (int u = -8; u >= -12; u -= 2) put(m, u, ESC + 2, -22, b(Blocks.SKELETON_WALL_SKULL).setValue(WallSkullBlock.FACING, Direction.SOUTH));
        for (int u = -8; u >= -12; u -= 2) put(m, u, ESC + 1, -22, losa(Blocks.SPRUCE_SLAB, SlabType.TOP));
        put(m, -6, ESC + 1, -18, b(Blocks.COBBLESTONE));
        put(m, -7, ESC + 1, -18, b(Blocks.MOSSY_COBBLESTONE));
        put(m, -10, TECHO - 1, -20, b(Blocks.LANTERN).setValue(LanternBlock.HANGING, true));
        // el pasillo de la puerta real: velas a los pies de las paredes
        for (int v : new int[]{-18, -19}) for (int s : new int[]{-1, 1}) put(m, s, ESC + 1, v, vela(Blocks.CANDLE, 1 + (v & 1)));
        for (int v : new int[]{-27, -29}) for (int s : new int[]{-1, 1}) put(m, s, SALA + 1, v, vela(Blocks.RED_CANDLE, 2));
    }

    /** Lo que queda en la llanura detras del muro de la escena: el jardin del teatro (tocones de columna y tambores caidos). */
    private void superficie(Pilotes.Lista m) {
        int[][] t = {{-16, -27, 2}, {-16, -35, 1}, {16, -27, 1}, {16, -35, 3}, {-6, -48, 1}, {9, -52, 2}};
        for (int[] q : t)
            for (int h = 0; h < q[2]; h++) put(m, q[0], PISO + h, q[1], h == 0 ? b(Blocks.CHISELED_STONE_BRICKS) : b(Blocks.POLISHED_DIORITE));
        for (int v = -31; v >= -33; v--) put(m, -12, PISO, v, b(Blocks.POLISHED_DIORITE));
    }

    private void trozos() {
        // el atril de los bastidores del este, de cara a la puerta (al sur)
        trozo(9, ESC + 1, -21, c -> c.lectern(x(9), ESC + 1, z(-21), Direction.SOUTH, Perdidos.libro(LIBRO)));
        trozo(-9, ESC + 1, -21, c -> c.chest(x(-9), ESC + 1, z(-21), Direction.SOUTH, BOTIN));
        trozo(0, CAM_SUELO + 1, -63, c -> c.chest(x(0), CAM_SUELO + 1, z(-63), Direction.SOUTH, BOTIN_BUENO));
        trozo(-3, CAM_SUELO + 1, -59, c -> c.barrel(x(-3), CAM_SUELO + 1, z(-59), Direction.UP, BOTIN));
    }

    private void trozo(int u, int y, int v, Trozo.Obra o) {
        elementos.add(new Trozo(6, x(u), y, z(v), x(u), y, z(v), o));
    }

    // ------------------------------------------------------------------ los de alli

    private static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;

    /** El hacia donde mira de Minecraft (0 = sur, 90 = oeste) para una direccion (du, dv). */
    static float giro(double du, double dv) {
        return (float) (Math.toDegrees(Math.atan2(dv, du)) - 90.0);
    }

    private void ponerEsperas() {
        int G = Calcinado.GUARDIA, I = Calcinado.INCENDIARIO, C = Calcinado.CENICIENTO;
        int Re = Enemigo.REZA, D = Enemigo.DE_PIE, Tu = Enemigo.TUMBADO;
        // Pell, el Bufon del Rey (el puesto 0): en medio de su sala, de cara a la puerta
        en(0, 0, SALA + 1, SALA_V, SUR, D, 0, Puestos.JEFE);
        // la grada: los que ardieron en sus asientos, enterrados en la ceniza
        enGrada(C, 1, -40, Tu, "grada");
        enGrada(C, 3, 12, Tu, "grada");
        enGrada(C, 5, -18, Tu, "grada");
        enGrada(C, 2, 70, Tu, "grada");
        enGrada(C, 6, 30, Tu, "grada");
        enGrada(C, 4, -70, Tu, "grada");
        // la orquesta: uno entre la ceniza
        en(C, 4, ORQ + 1, 2, NORTE, Tu, 0, "orquesta");
        // el escenario: la guardia de la corte, a los lados de la puerta real
        en(G, -7, ESC + 1, -11, SUR, D, 0, "escena");
        en(G, 7, ESC + 1, -11, SUR, D, 0, "escena");
        // arriba de la grada: los que tiran cantaros a la orquesta
        en(I, -18, PISO, 23, giro(18, -23), D, 0, "summa");
        en(I, 18, PISO, 23, giro(-18, -23), D, 0, "summa");
        // los pasos de los lados
        en(I, 23, yPaso(23) + 1, -5, OESTE, D, 2, "pasos");
        en(G, -23, yPaso(23) + 1, -5, ESTE, Re, 0, "pasos");
        // los bastidores
        en(C, 10, ESC + 1, -19, OESTE, Re, 0, "bastidores");
        en(G, -7, ESC + 1, -20, ESTE, D, 0, "bastidores");
    }

    private void en(int variante, int u, int y, int v, float giro, int postura, int ronda, String grupo) {
        esperas.add(new Espera(variante, x(u), y, z(v), giro, postura, ronda, grupo));
    }

    /** Uno en la fila k de la grada, a {@code grados} del sur (en la parte de atras del asiento, no en el escalon). */
    private void enGrada(int variante, int k, double grados, int postura, String grupo) {
        double r = R_ORQ + k * PASO_FILA + 1.4, a = Math.toRadians(grados);
        int u = (int) Math.round(Math.sin(a) * r), v = (int) Math.round(Math.cos(a) * r);
        int y = altoFila(fila(Math.hypot(u, v))) + 1;
        esperas.add(new Espera(variante, x(u), y, z(v), giro(-u, -v), postura, 0, grupo));
    }

    // ------------------------------------------------------------------ lo que pasa

    private static long siguienteRisa;

    /**
     * Cada segundo con alguien cerca (lo llama {@link Morvain#onTick}): mientras la puerta real siga tapiada, a quien esta en el
     * escenario le llega de vez en cuando, detras del muro, una risa ahogada y unos cascabeles (la pista).
     */
    public static void tick(ServerLevel m) {
        if (MurosIlusorios.abierto(m.getServer(), MURO_PUERTA)) return;
        long t = m.getGameTime();
        if (t < siguienteRisa) return;
        BlockPos c = new BlockPos(x(0), ESC + 2, z(FRONS_V1));
        if (!m.isLoaded(c)) return;
        for (ServerPlayer p : m.players()) {
            if (p.isSpectator() || p.distanceToSqr(c.getX() + 0.5, c.getY(), c.getZ() + 0.5) > 14 * 14) continue;
            siguienteRisa = t + 260 + m.random.nextInt(240);
            boolean risa = m.random.nextInt(3) > 0;
            m.playSound(null, c.getX() + 0.5, c.getY(), z(FRONS_V0) + 0.5, risa ? DarkCraftMod.SFX_BUFON_RISA.get() : DarkCraftMod.SFX_BUFON_CASCABELES.get(),
                    SoundSource.HOSTILE, risa ? 0.45f : 0.7f, risa ? 0.95f : 1.0f);
            return;
        }
    }

    /** Lo que hay en el teatro ya generado (para la prueba): el atril con su libro, los arcones, el tapiado, el trono... */
    public static String resumen(ServerLevel m) {
        Teatro a = get();
        int[] at = a.sitio("atril"), ar = a.sitio("arcon"), ab = a.sitio("arcon_bueno");
        boolean libro = m.getBlockEntity(new BlockPos(at[0], at[1], at[2])) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le
                && Perdidos.de(le.getBook()) == LIBRO;
        boolean arcon = m.getBlockState(new BlockPos(ar[0], ar[1], ar[2])).is(Blocks.CHEST);
        boolean bueno = m.getBlockState(new BlockPos(ab[0], ab[1], ab[2])).is(Blocks.CHEST);
        int tapia = 0, trono = 0;
        for (BlockPos p : muro(MURO_PUERTA)) if (!m.getBlockState(p).isAir()) tapia++;
        for (BlockPos p : muro(MURO_TRONO)) if (!m.getBlockState(p).isAir()) trono++;
        int filas = 0;
        for (int k = 0; k < FILAS; k++) {
            double r = R_ORQ + k * PASO_FILA + 1.4;
            if (m.getBlockState(new BlockPos(x(0), altoFila(k), z((int) Math.round(r)))).isSolidRender(m, BlockPos.ZERO)) filas++;
        }
        boolean silla = m.getBlockState(new BlockPos(x(0), TABLADO + 1, z(-55))).is(Blocks.DARK_OAK_STAIRS);
        boolean sala = m.getBlockState(new BlockPos(x(0), SALA + 2, z(SALA_V))).isAir() && !m.getBlockState(new BlockPos(x(0), SALA, z(SALA_V))).isAir();
        return "atril con el libro " + LIBRO + " " + (libro ? "SI" : "NO") + ", arcon " + (arcon ? "SI" : "NO") + ", arcon bueno " + (bueno ? "SI" : "NO")
                + ", la puerta tapiada " + tapia + "/12, el muro del trono " + trono + "/9, filas de la grada " + filas + "/" + FILAS
                + ", el trono " + (silla ? "SI" : "NO") + ", la sala " + (sala ? "SI" : "NO");
    }
}
