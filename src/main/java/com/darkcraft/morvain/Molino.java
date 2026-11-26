package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Landscape;
import com.darkcraft.gen.Pal;
import com.darkcraft.gen.Rng;
import com.darkcraft.gen.Town;
import com.darkcraft.pueblo.gen.Mat;
import com.darkcraft.pueblo.gen.Trozo;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * El molino de Velmor (parte 7f): la guarida de la Molinera, el jefe de las Llanuras Palidas, al este del pueblo, al final
 * del camino que sale por la puerta del este. Una loma de tierra con la <b>era</b> encima (el suelo de piedra donde se
 * trillaba, redondo, con un murete roto alrededor y cuatro farolas de almas: ahi se pelea) y, al este de la era, el
 * <b>molino</b>: una torre de piedra con el cuerpo de madera, el tejado a dos aguas y las <b>aspas</b> rotas mirando al
 * camino (al oeste). En medio de la era, la <b>piedra de moler</b> ante la que espera de rodillas la Molinera, de espaldas
 * a quien llega. Dentro del molino, las muelas, los sacos y un arcon arriba. Campos secos alrededor, un carro, gavillas.
 * <p>
 * Se dibuja como Velmor: chunk a chunk al adornarlo ({@link MorvainGenerador#applyBiomeDecoration}). El santuario del
 * Camino del Molino ({@link SantuariosMorvain}, "molino") queda a la orilla del camino, antes de la loma. Mientras vive la
 * Molinera el molino cruje con el viento ({@link #crujir}); al caer ella, se para.
 */
public final class Molino {
    public static final long SEMILLA = 0x30_11_70_7F5AL;
    /** El centro de la era (donde espera la Molinera, de rodillas mirando al molino) y su radio (lo enlosado). */
    public static final int EX = 1130, EZ = 4064, R_ERA = 17;
    /** El centro de la torre del molino y su radio (por fuera). */
    public static final int MX = 1156, MZ = 4064, R_TORRE = 6;
    /** Lo alto de la piedra (el piso de arriba) y del cuerpo de madera, sobre Y. */
    static final int PIEDRA = 13, CUERPO = 22;
    /** Lo de arriba de la loma (el bloque: se pisa en Y + 1). */
    public static final int Y = 76;
    /** La loma: llana hasta MESETA (alrededor de la era) y MESETA_M (del molino); baja en FALDA bloques. */
    static final double MESETA = 19.5, MESETA_M = 10, FALDA = 9;
    /** Todo lo que se dibuja (con el camino desde Velmor y los campos): ahi {@link Adorno} solo pone hierba. */
    static final int X0 = 990, X1 = 1186, Z0 = 4020, Z1 = 4126;
    /** El cubo de las aspas (donde se cruzan): en la cara del oeste del tejado. */
    public static final int HX = MX - R_TORRE - 2, HY = Y + CUERPO + 3, HZ = MZ;

    /** El camino de Velmor al molino: {x0, z0, x1, z1} (de la puerta del este del pueblo hasta la era). */
    static final int[][] CAMINO = {{990, 4047, 1040, 4056}, {1040, 4056, 1086, 4062}, {1086, 4062, EX - R_ERA + 1, EZ}};
    static final double CAMINO_ANCHO = 1.4;

    // ------------------------------------------------------------------ la loma

    private static final int AW = X1 - X0 + 1, AD = Z1 - Z0 + 1;
    private static final int[] ALTO = new int[AW * AD];

    static {
        Arrays.fill(ALTO, Integer.MIN_VALUE);
    }

    /** El bloque de arriba del terreno de Morvain (sin la loma). */
    static int terreno(int x, int z) {
        if (x < X0 || x > X1 || z < Z0 || z > Z1) return (int) Math.floor(Terreno.columna(x, z).altura());
        int i = (z - Z0) * AW + (x - X0);
        int v = ALTO[i];
        if (v == Integer.MIN_VALUE) ALTO[i] = v = (int) Math.floor(Terreno.columna(x, z).altura());
        return v;
    }

    /** Lo lejos que esta (x, z) de lo llano de la loma (0 o menos: encima). */
    static double fuera(double x, double z) {
        return Math.min(Math.hypot(x - EX, z - EZ) - MESETA, Math.hypot(x - MX, z - MZ) - MESETA_M);
    }

    /** La altura de la loma en (x, z) (con decimales: en el camino se ponen losas donde toca media). */
    static double loma(int x, int z) {
        int t = terreno(x, z);
        double d = fuera(x + 0.5, z + 0.5);
        if (d <= 0) return Math.max(Y, t);
        if (d >= FALDA) return t;
        double f = d / FALDA;
        f = f * f * (3 - 2 * f);
        return Math.max(t, Y + (t - Y) * f);
    }

    /** El bloque de arriba en (x, z) con la loma. */
    public static int alto(int x, int z) {
        return (int) Math.floor(loma(x, z));
    }

    /** Lo que se pisa en (x, z). */
    public static int pie(int x, int z) {
        return alto(x, z) + 1;
    }

    // ------------------------------------------------------------------ el plano

    private static Molino unico;

    public static synchronized Molino get() {
        if (unico == null) unico = new Molino();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();

    private Molino() {
        Rng rng = new Rng(SEMILLA);
        elementos.add(new Loma());
        elementos.add(new Era(rng.fork()));
        elementos.add(new Torre(rng.fork()));
        elementos.add(new Aspas(rng.fork()));
        campo(1106, 4092, 1150, 4118, 0);
        campo(1162, 4026, 1184, 4056, 1);
        campo(1040, 4068, 1080, 4092, 2);
        detalles(rng);
        sitio("molinera", EX, Y + 1, EZ);
        sitio("era", EX, Y + 1, EZ);
        sitio("entrada", EX - R_ERA - 2, Y + 1, EZ);
        sitio("puerta_molino", MX - R_TORRE - 1, Y + 1, MZ);
        sitio("cofre_molino", MX + 2, Y + PIEDRA + 1, MZ + 2);
        sitio("camino", 1060, pie(1060, 4059), 4059);
        sitio("aspas", HX, HY, HZ);
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo del molino (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en lo del molino: ahi no se ponen arboles ni rocas sueltos. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= X0 && x0 <= X1 && z0 + 15 >= Z0 && z0 <= Z1;
    }

    /** Si (x, z) esta en el molino (la loma: para el nombre del sitio). */
    public static boolean dentro(double x, double z) {
        return fuera(x, z) < FALDA;
    }

    static void chunk(WorldGenLevel nivel, ChunkAccess c) {
        int cx = c.getPos().x, cz = c.getPos().z;
        get().construir(nivel, cx, cz, RandomSource.create(Velmor.clave(cx, cz) ^ SEMILLA));
    }

    private final java.util.Set<String> avisados = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public void construir(WorldGenLevel nivel, int chunkX, int chunkZ, RandomSource r) {
        List<Element> l = indice.get(Velmor.clave(chunkX, chunkZ));
        if (l == null) return;
        Canvas c = new Canvas(nivel, chunkX, chunkZ, r);
        for (Element e : l) {
            try {
                e.build(c);
            } catch (Throwable t) {
                String k = e.getClass().getSimpleName();
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} del molino en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public java.util.Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los sitios con nombre (la era, la puerta, el arcon, las aspas...): {x, y, z} (y: lo que se pisa). */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    private void sitio(String nombre, int x, int y, int z) {
        sitios.put(nombre, new int[]{x, y, z});
    }

    private void trozo(int capa, int x0, int y0, int z0, int x1, int y1, int z1, Trozo.Obra o) {
        elementos.add(new Trozo(capa, x0, y0, z0, x1, y1, z1, o));
    }

    /** Si (x, z) esta en el camino de Velmor al molino. */
    static boolean enCamino(int x, int z) {
        double borde = (Hash.pct(x, 9, z, SEMILLA + 1) % 3 - 1) * 0.35;
        for (int[] s : CAMINO) if (Velmor.distSeg(x, z, s[0], s[1], s[2], s[3]) <= CAMINO_ANCHO + borde) return true;
        return false;
    }

    // ------------------------------------------------------------------ la loma y el camino

    /** La tierra de la loma (con la hierba de encima) y el camino que sube desde Velmor. */
    private final class Loma extends Element {
        Loma() {
            super(SEMILLA + 2);
            this.layer = 1;
            bounds(X0, 50, Z0, X1, Y + 4, Z1);
        }

        @Override
        public void build(Canvas c) {
            for (int x = c.minX; x <= c.maxX; x++)
                for (int z = c.minZ; z <= c.maxZ; z++) {
                    if (x < X0 || x > X1 || z < Z0 || z > Z1) continue;
                    int t = terreno(x, z);
                    double h = loma(x, z);
                    int top = (int) Math.floor(h);
                    boolean camino = enCamino(x, z);
                    if (top > t) {
                        // la loma: tierra debajo, hierba (o tierra pisada) encima; fuera lo que hubiera encima del terreno
                        for (int k = 1; k <= 2; k++) if (!c.isAir(x, t + k, z) && t + k > top) c.set(x, t + k, z, Pal.AIR);
                        for (int y = t; y < top; y++) c.set(x, y, z, Hash.pct(x, y, z, seed) < 20 ? Pal.COARSE_DIRT : Pal.DIRT);
                        int p = Hash.pct(x, top, z, seed + 1);
                        c.set(x, top, z, p < 70 ? Pal.GRASS_BLOCK : p < 88 ? Pal.COARSE_DIRT : Pal.ROOTED_DIRT);
                        if (!camino && fuera(x + 0.5, z + 0.5) > 0 && p > 82) c.set(x, top + 1, z, Pal.b(p > 94 ? Blocks.DEAD_BUSH : Blocks.GRASS));
                    }
                    if (!camino || top < Terreno.AGUA) continue;
                    // el camino: tierra pisada y grava; en la cuesta, losas donde la loma va por la mitad
                    int p = Hash.pct(x, top, z, seed + 3);
                    c.set(x, top, z, p < 46 ? Pal.b(Blocks.DIRT_PATH) : p < 72 ? Pal.COARSE_DIRT : p < 88 ? Pal.GRAVEL : Pal.ROOTED_DIRT);
                    for (int k = 1; k <= 2; k++) if (!c.isAir(x, top + k, z)) c.set(x, top + k, z, Pal.AIR);
                    if (h - top >= 0.5 && top < Y) {
                        c.set(x, top + 1, z, Pal.slab(Blocks.COBBLESTONE_SLAB, false));
                    }
                }
        }
    }

    // ------------------------------------------------------------------ la era

    /**
     * La era: el suelo de losas en anillos (donde se trillaba), gastado y con surcos; un murete roto alrededor con su
     * entrada al oeste (el camino), una al norte y otra al sur; cuatro farolas de almas; la piedra de moler en el centro
     * (con la harina y los huesos de alrededor) y los sacos, las gavillas y los cestos que quedaron.
     */
    private final class Era extends Element {
        Era(long seed) {
            super(seed);
            this.layer = 3;
            bounds(EX - R_ERA - 3, Y - 2, EZ - R_ERA - 3, EX + R_ERA + 3, Y + 6, EZ + R_ERA + 3);
        }

        private BlockState losa(int x, int z, double d) {
            int p = Hash.pct(x, Y, z, seed);
            int anillo = (int) Math.floor(d / 2.6);
            // los anillos: uno de losas, otro de canto rodado; el centro, piedra lisa
            if (d < 4.2) return p < 60 ? Pal.b(Blocks.SMOOTH_STONE) : p < 84 ? Pal.b(Blocks.POLISHED_ANDESITE) : Pal.ANDESITE;
            if (anillo % 2 == 0)
                return p < 44 ? Pal.STONE_BRICKS : p < 64 ? Pal.b(Blocks.CRACKED_STONE_BRICKS) : p < 80 ? Pal.b(Blocks.POLISHED_ANDESITE)
                        : p < 92 ? Pal.ANDESITE : Pal.MOSSY_STONE_BRICKS;
            return p < 40 ? Pal.b(Blocks.COBBLESTONE) : p < 58 ? Pal.ANDESITE : p < 72 ? Pal.b(Blocks.MOSSY_COBBLESTONE) : p < 86 ? Pal.GRAVEL
                    : p < 95 ? Pal.COARSE_DIRT : Pal.b(Blocks.TUFF);
        }

        /** Las entradas del murete: al oeste (el camino), al este (la puerta del molino), al norte y al sur (y donde se cayo). */
        private boolean hueco(double a, int x, int z) {
            double grados = Math.toDegrees(a);
            if (Math.abs(Math.toDegrees(Math.atan2(Math.sin(a - Math.PI), Math.cos(a - Math.PI)))) < 13) return true;   // oeste
            if (Math.abs(grados) < 10) return true;   // este
            if (Math.abs(grados - 90) < 9 || Math.abs(grados + 90) < 9) return true;   // sur y norte
            return Hash.pct(x, 3, z, seed + 4) < 24;
        }

        @Override
        public void build(Canvas c) {
            for (int x = EX - R_ERA - 3; x <= EX + R_ERA + 3; x++)
                for (int z = EZ - R_ERA - 3; z <= EZ + R_ERA + 3; z++) {
                    if (!c.inXZ(x, z)) continue;
                    double d = Math.hypot(x - EX, z - EZ);
                    if (d <= R_ERA + 0.4) {
                        c.set(x, Y, z, losa(x, z, d));
                        c.column(x, z, Y + 1, Y + 5, Pal.AIR);
                        // la ceniza y la harina, a manchas
                        int p = Hash.pct(x, 5, z, seed + 1);
                        if (p > 97 && d > 3.2) c.set(x, Y + 1, z, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1));
                    } else if (d <= R_ERA + 1.6) {
                        // el murete: canto rodado y musgo, roto a trozos
                        c.set(x, Y, z, Mat.muro(x, Y, z, seed));
                        double a = Math.atan2(z - EZ, x - EX);
                        if (!hueco(a, x, z)) {
                            int p = Hash.pct(x, 6, z, seed + 2);
                            c.unir(x, Y + 1, z, Pal.b(p < 55 ? Blocks.COBBLESTONE_WALL : p < 85 ? Blocks.MOSSY_COBBLESTONE_WALL : Blocks.STONE_BRICK_WALL));
                        } else if (Hash.pct(x, 7, z, seed + 3) < 30) c.set(x, Y + 1, z, Pal.slab(Blocks.COBBLESTONE_SLAB, false));
                    }
                }
            // la piedra de moler (de canto no: tumbada, en losas) delante de donde espera ella, y su ojo
            int px = EX + 3, pz = EZ;
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++)
                    c.set(px + dx, Y + 1, pz + dz, dx == 0 && dz == 0 ? Pal.slab(Blocks.POLISHED_BLACKSTONE_SLAB, false) : Pal.slab(Blocks.SMOOTH_STONE_SLAB, false));
            c.set(px + 2, Y + 1, pz, Pal.slab(Blocks.SMOOTH_STONE_SLAB, false));
            c.set(px - 2, Y + 1, pz, Pal.AIR);
            // calaveras alrededor de la piedra (lo que molio): bajas, se pasa por encima
            int[][] huesos = {{EX + 5, EZ - 2}, {EX + 4, EZ + 3}, {EX + 1, EZ - 4}, {EX - 2, EZ + 4}, {EX + 6, EZ + 1}};
            for (int i = 0; i < huesos.length; i++) c.set(huesos[i][0], Y + 1, huesos[i][1], Mat.calavera(i * 5 + 3));
            // las cuatro farolas de almas, junto a las entradas
            int[][] f = {{EX - R_ERA - 1, EZ - 3}, {EX - R_ERA - 1, EZ + 3}, {EX - 3, EZ - R_ERA - 1}, {EX + 3, EZ + R_ERA + 1}};
            for (int[] k : f) Mat.farola(c, k[0], Y + 1, k[1], 4, null);
            // los sacos de harina (lana), las gavillas y los cestos que quedaron por la era
            int[][] sacos = {{EX - 9, EZ - 11}, {EX - 8, EZ - 11}, {EX - 9, EZ - 10}, {EX + 10, EZ + 9}, {EX + 11, EZ + 8}, {EX - 13, EZ + 7}};
            for (int i = 0; i < sacos.length; i++) {
                int[] s = sacos[i];
                c.set(s[0], Y + 1, s[1], Pal.b(i % 3 == 2 ? Blocks.LIGHT_GRAY_WOOL : Blocks.WHITE_WOOL));
                if (i == 0) c.set(s[0], Y + 2, s[1], Pal.b(Blocks.WHITE_WOOL));
            }
            c.set(EX + 12, Y + 1, EZ - 9, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Y));
            c.set(EX + 12, Y + 2, EZ - 9, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.X));
            c.set(EX + 13, Y + 1, EZ - 8, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Z));
            c.set(EX - 12, Y + 1, EZ - 6, Mat.barril(Direction.UP));
            c.set(EX - 4, Y + 1, EZ + 13, Pal.b(Blocks.COMPOSTER));
        }
    }

    // ------------------------------------------------------------------ el molino

    /**
     * La torre del molino: abajo de piedra (11 de alto, con la puerta al oeste, a la era), encima el cuerpo de madera con su
     * armazon y el tejado a dos aguas con la cumbrera de este a oeste (el eje de las aspas sale por el hastial del oeste).
     * Dentro: las muelas en el centro, sacos, la escalera de mano al piso de arriba (con el arcon) y el engranaje.
     */
    private final class Torre extends Element {
        Torre(long seed) {
            super(seed);
            this.layer = 3;
            bounds(MX - R_TORRE - 2, Y - 3, MZ - R_TORRE - 2, MX + R_TORRE + 2, Y + CUERPO + R_TORRE + 3, MZ + R_TORRE + 2);
        }

        @Override
        public void build(Canvas c) {
            int piedra = Y + PIEDRA, cuerpo = Y + CUERPO;
            for (int x = MX - R_TORRE - 1; x <= MX + R_TORRE + 1; x++)
                for (int z = MZ - R_TORRE - 1; z <= MZ + R_TORRE + 1; z++) {
                    if (!c.inXZ(x, z)) continue;
                    double d = Math.hypot(x - MX, z - MZ);
                    if (d > R_TORRE + 0.5) continue;
                    boolean muro = d > R_TORRE - 0.6;
                    // los cimientos, hasta el terreno
                    for (int y = terreno(x, z) - 1; y < Y; y++) c.set(x, y, z, Mat.muro(x, y, z, seed));
                    c.set(x, Y, z, muro ? Mat.muro(x, Y, z, seed) : Mat.losas(x, Y, z, seed));
                    // abajo: la piedra
                    for (int y = Y + 1; y <= piedra; y++) c.set(x, y, z, muro ? Mat.muro(x, y, z, seed + 1) : Pal.AIR);
                    // el piso de arriba (tablas), con el hueco de la escalera (pegada al arbol del centro)
                    boolean huecoEscalera = x == MX + 1 && z == MZ;
                    if (!muro) c.set(x, piedra, z, huecoEscalera ? Pal.AIR : Mat.tabla(x, piedra, z, seed));
                    // el cuerpo de madera, algo mas estrecho
                    boolean muroMadera = d > R_TORRE - 1.4 && d <= R_TORRE - 0.4;
                    for (int y = piedra + 1; y <= cuerpo; y++) {
                        if (d > R_TORRE - 0.4) {
                            // la cornisa entre la piedra y la madera
                            if (y == piedra + 1) c.set(x, y, z, Pal.slab(Blocks.STONE_BRICK_SLAB, false));
                            continue;
                        }
                        if (!muroMadera) {
                            c.set(x, y, z, Pal.AIR);
                            continue;
                        }
                        // armazon: pies derechos en ocho sitios y vigas cada cuatro; lo demas, tablas viejas
                        double a = Math.atan2(z - MZ, x - MX);
                        boolean pie = Math.abs(Math.sin(a * 4)) < 0.2;
                        boolean viga = (y - piedra) % 4 == 1;
                        BlockState s = pie ? Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.Y) : viga ? Pal.log(Blocks.STRIPPED_DARK_OAK_LOG, Math.abs(x - MX) > Math.abs(z - MZ) ? Direction.Axis.Z : Direction.Axis.X)
                                : Hash.pct(x, y, z, seed + 2) < 70 ? Pal.SP_PLANKS : Pal.DO_PLANKS;
                        // la madera rota del lado del norte
                        if (!pie && z < MZ - 2 && Hash.pct(x, y, z, seed + 3) < 34) s = Pal.AIR;
                        c.set(x, y, z, s);
                    }
                    // ventanucos en la piedra
                    if (muro && (Math.abs(x - MX) <= 0 || Math.abs(z - MZ) <= 0) && !(x < MX && Math.abs(z - MZ) <= 1)) {
                        c.set(x, Y + 6, z, Pal.AIR);
                        c.set(x, Y + 7, z, Pal.AIR);
                    }
                }
            // la puerta, al oeste (a la era): dos de ancho y tres de alto, con su arco
            int px = MX - R_TORRE;
            for (int z = MZ - 1; z <= MZ; z++) {
                for (int y = Y + 1; y <= Y + 3; y++) {
                    c.set(px, y, z, Pal.AIR);
                    c.set(px + 1, y, z, Pal.AIR);
                }
                c.set(px, Y, z, Mat.losas(px, Y, z, seed));
            }
            c.set(px, Y + 4, MZ - 1, Pal.stairs(Blocks.STONE_BRICK_STAIRS, Direction.SOUTH, true));
            c.set(px, Y + 4, MZ, Pal.stairs(Blocks.STONE_BRICK_STAIRS, Direction.NORTH, true));
            // la reja: cerrada mientras viva la Molinera (la quita {@link #puerta})
            for (int[] r : REJA) c.unir(r[0], r[1], r[2], Pal.IRON_BARS);
            // el tejado a dos aguas (cumbrera de este a oeste) con sus hastiales
            tejado(c, cuerpo);
            // dentro: las muelas, los sacos, la escalera y el arcon de arriba
            // (la muela de abajo, media losa: se sube a ella y de ella a la escalera, pegada al arbol)
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++)
                    if (dx != 0 || dz != 0) c.set(MX + dx, Y + 1, MZ + dz, Pal.slab(Blocks.SMOOTH_STONE_SLAB, false));
            for (int y = Y + 1; y < piedra; y++) c.set(MX, y, MZ, Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
            int[][] sacos = {{MX - 3, MZ - 2}, {MX - 3, MZ + 2}, {MX - 2, MZ + 3}, {MX + 1, MZ - 3}};
            for (int[] s : sacos) c.set(s[0], Y + 1, s[1], Pal.b(Blocks.WHITE_WOOL));
            c.set(MX - 3, Y + 2, MZ - 2, Pal.b(Blocks.WHITE_WOOL));
            c.set(MX - 2, Y + 1, MZ - 3, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 2));
            for (int y = Y + 2; y <= piedra; y++)
                c.set(MX + 1, y, MZ, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST));
            c.chest(MX + 2, piedra + 1, MZ + 2, Direction.WEST, Velmor.BOTIN_BUENO);
            c.set(MX - 2, piedra + 1, MZ - 2, Mat.calavera(6));
            c.set(MX - 3, piedra + 1, MZ + 1, Pal.b(Blocks.WHITE_WOOL));
            c.set(MX + 3, Y + 5, MZ + 2, Pal.COBWEB);
            c.set(MX - 2, piedra - 1, MZ + 3, Pal.COBWEB);
            // el engranaje: la rueda grande en lo alto del arbol y el eje de las aspas
            for (int x = MX - R_TORRE - 2; x <= MX + 1; x++) c.set(x, HY, MZ, Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.X));
            for (int y = piedra + 1; y < HY - 1; y++) c.set(MX, y, MZ, Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
            for (int dz = -2; dz <= 2; dz++) for (int dy = -2; dy <= 2; dy++)
                if (Math.abs(dz) + Math.abs(dy) == 3 || Math.abs(dz) == 2 && dy == 0 || dz == 0 && Math.abs(dy) == 2)
                    c.set(MX - 1, HY + dy, MZ + dz, Pal.log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.X));
            Mat.farolColgado(c, MX + 1, cuerpo - 1, MZ - 2, 2);
        }

        /** El tejado: de pizarra oscura, a dos aguas sobre el cuerpo (de norte a sur baja), con el hastial de madera. */
        private void tejado(Canvas c, int cuerpo) {
            int r = R_TORRE;
            for (int k = 0; k <= r; k++) {
                int y = cuerpo + 1 + k;
                for (int x = MX - r - 1; x <= MX + r + 1; x++) {
                    for (int s = -1; s <= 1; s += 2) {
                        int z = MZ + s * (r - k);
                        boolean cumbrera = k == r;
                        // un trozo hundido del lado del norte
                        if (s < 0 && x > MX - 1 && x < MX + 3 && k >= 1 && k <= 3 && Hash.pct(x, y, z, seed + 5) < 60) continue;
                        c.set(x, y, z, cumbrera ? Pal.slab(Blocks.DEEPSLATE_TILE_SLAB, false)
                                : Pal.stairs(Blocks.DEEPSLATE_TILE_STAIRS, s < 0 ? Direction.SOUTH : Direction.NORTH, false));
                        if (cumbrera) break;
                    }
                    // los hastiales (este y oeste): tablas bajo el tejado
                    if (x == MX - r || x == MX + r)
                        for (int z = MZ - (r - k) + 1; z <= MZ + (r - k) - 1; z++)
                            c.set(x, y, z, (x == MX - r && z == MZ && y == HY) ? Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.X)
                                    : Hash.pct(x, y, z, seed + 6) < 75 ? Pal.SP_PLANKS : Pal.DO_PLANKS);
                }
            }
        }
    }

    /**
     * Las aspas: cuatro brazos en cruz desde el cubo (en el hastial del oeste, mirando al camino), cada uno con su mastil y
     * el armazon de listones a un lado (como un molinete); quedan jirones de lona en algunos huecos. El de abajo se partio:
     * solo queda medio mastil y el armazon caido al pie del molino.
     */
    private final class Aspas extends Element {
        static final int LARGO = 19;

        Aspas(long seed) {
            super(seed);
            this.layer = 5;
            // (con el armazon caido al norte del molino)
            bounds(HX - 1, Y - 2, HZ - LARGO - 4, MX + 4, HY + LARGO + 4, HZ + LARGO + 4);
        }

        private void lona(Canvas c, int x, int y, int z) {
            int p = Hash.pct(x, y, z, seed);
            if (p < 30) c.set(x, y, z, Pal.b(p < 18 ? Blocks.WHITE_WOOL : Blocks.LIGHT_GRAY_WOOL));
            else if (p < 42) c.set(x, y, z, Pal.COBWEB);
        }

        @Override
        public void build(Canvas c) {
            int x = HX;
            // el cubo
            c.set(x, HY, HZ, Pal.b(Blocks.DARK_OAK_WOOD));
            c.set(x + 1, HY, HZ, Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.X));
            // {direccion del mastil (dy, dz), lado del armazon (dy, dz), largo}
            int[][] brazos = {{1, 0, 0, 1, LARGO}, {0, -1, 1, 0, LARGO}, {0, 1, -1, 0, LARGO - 2}, {-1, 0, 0, -1, 6}};
            for (int b = 0; b < brazos.length; b++) {
                int[] k = brazos[b];
                int dy = k[0], dz = k[1], ly = k[2], lz = k[3], largo = k[4];
                boolean roto = b == 3;
                for (int i = 1; i <= largo; i++) {
                    int y = HY + dy * i, z = HZ + dz * i;
                    c.set(x, y, z, dy != 0 ? Pal.log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Y) : Pal.log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Z));
                    if (roto || i < 4) continue;
                    // el armazon: tres de ancho a un lado; listones cada dos y el larguero de fuera
                    for (int j = 1; j <= 3; j++) {
                        int yy = y + ly * j, zz = z + lz * j;
                        boolean larguero = j == 3, liston = (i - 4) % 2 == 0;
                        if (larguero || liston) c.unir(x, yy, zz, Pal.SP_FENCE);
                        else lona(c, x, yy, zz);
                    }
                }
            }
            // el brazo partido: su armazon caido al pie del molino (al norte, fuera de la era)
            for (int i = 0; i < 6; i++)
                for (int j = 0; j < 2; j++) {
                    int xx = MX - 3 + i, zz = MZ - R_TORRE - 3 - j;
                    if (Hash.pct(xx, 1, zz, seed + 1) < 22) continue;
                    c.set(xx, pie(xx, zz), zz, i % 2 == 0 ? Pal.slab(Blocks.SPRUCE_SLAB, false) : Mat.trampilla(Direction.NORTH, false, false));
                }
        }
    }

    // ------------------------------------------------------------------ los campos y lo demas

    private void campo(int x0, int z0, int x1, int z1, int n) {
        long s = SEMILLA + 40 + n;
        trozo(1, x0, 50, z0, x1, 100, z1, c -> {
            for (int x = x0; x <= x1; x++)
                for (int z = z0; z <= z1; z++) {
                    if (!c.inXZ(x, z) || enCamino(x, z) || fuera(x + 0.5, z + 0.5) < FALDA) continue;
                    int top = terreno(x, z);
                    if (top < Terreno.AGUA + 1) continue;
                    boolean borde = x == x0 || x == x1 || z == z0 || z == z1;
                    int p = Hash.pct(x, 4, z, s);
                    for (int k = 1; k <= 2; k++) if (!c.isAir(x, top + k, z)) c.set(x, top + k, z, Pal.AIR);
                    if (borde) {
                        c.set(x, top, z, Pal.COARSE_DIRT);
                        if (p >= 45) c.unir(x, top + 1, z, Pal.SP_FENCE);
                        continue;
                    }
                    if ((n == 1 ? z : x) % 2 == 0) {
                        c.set(x, top, z, Pal.COARSE_DIRT);
                        continue;
                    }
                    if (p < 58) {
                        c.set(x, top, z, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 0));
                        c.set(x, top + 1, z, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 3 + p % 5));
                    } else {
                        c.set(x, top, z, p < 80 ? Pal.COARSE_DIRT : Pal.ROOTED_DIRT);
                        if (p > 90) c.set(x, top + 1, z, Pal.b(Blocks.DEAD_BUSH));
                    }
                }
            // gavillas sin recoger y un espantapajaros
            for (int k = 0; k < 3; k++) {
                int x = x0 + 3 + Hash.pct(k, 5, n, s) % Math.max(1, x1 - x0 - 5), z = z0 + 3 + Hash.pct(k, 6, n, s) % Math.max(1, z1 - z0 - 5);
                if (fuera(x + 0.5, z + 0.5) < FALDA) continue;
                int top = terreno(x, z);
                c.set(x, top + 1, z, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Y));
            }
            int ex = (x0 + x1) / 2, ez = (z0 + z1) / 2;
            if (fuera(ex + 0.5, ez + 0.5) >= FALDA) {
                int et = terreno(ex, ez);
                c.set(ex, et, ez, Pal.COARSE_DIRT);
                c.set(ex, et + 1, ez, Pal.SP_FENCE);
                c.set(ex, et + 2, ez, Pal.SP_FENCE);
                c.set(ex, et + 3, ez, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Y));
                c.unir(ex - 1, et + 3, ez, Pal.SP_FENCE);
                c.unir(ex + 1, et + 3, ez, Pal.SP_FENCE);
                c.set(ex, et + 4, ez, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, Direction.WEST));
            }
        });
    }

    private void detalles(Rng rng) {
        // un carro volcado junto al camino, al pie de la loma, y otro junto al molino
        elementos.add(new Town.Cart(rng.fork(), 1098, pie(1098, 4070), 4070, Direction.EAST).layer(4));
        elementos.add(new Town.Cart(rng.fork(), MX + 1, pie(MX + 1, MZ + 10), MZ + 10, Direction.SOUTH).layer(4));
        // arboles muertos sin hojas por los lados de la loma
        int[][] k = {{1112, 4036}, {1150, 4040}, {1166, 4086}, {1118, 4090}, {1070, 4046}, {1020, 4064}, {1176, 4060}};
        for (int[] q : k) {
            int x = q[0], z = q[1];
            if (enCamino(x, z)) continue;
            elementos.add(new Landscape.DeadTree(rng.fork(), x, pie(x, z), z, rng.chance(0.3)).sinHojas());
        }
        // farolas por el camino (algunas apagadas)
        int[][] f = {{1010, 4053}, {1048, 4060}, {1076, 4057}, {1100, 4067}};
        for (int[] q : f) {
            int x = q[0], z = q[1], y = pie(x, z);
            int p = Hash.pct(x, 0, z, SEMILLA + 7);
            trozo(4, x - 1, y, z - 1, x + 1, y + 5, z + 1, c -> {
                Mat.farola(c, x, y, z, 4, null);
                if (p < 40) c.set(x, y + 4, z, Pal.b(Blocks.STONE_BRICK_WALL));
            });
        }
        // la otra muela, rota, tirada en la falda del norte
        trozo(4, MX - 9, Y - 4, MZ - 12, MX - 5, Y + 3, MZ - 8, c -> {
            int x = MX - 7, z = MZ - 10, y = pie(x, z);
            for (int dy = 0; dy <= 2; dy++)
                for (int dz = -1; dz <= 1; dz++) if (!(dy == 2 && dz != 0) && !(dy == 0 && dz == 1)) c.set(x, y + dy, z + dz, Pal.b(Blocks.SMOOTH_STONE));
        });
    }

    // ------------------------------------------------------------------ la reja de la puerta y el crujido

    /** Los barrotes de la puerta del molino (al oeste, a la era): cerrada mientras vive la Molinera. */
    static final int[][] REJA = {{MX - R_TORRE, Y + 1, MZ - 1}, {MX - R_TORRE, Y + 2, MZ - 1}, {MX - R_TORRE, Y + 3, MZ - 1},
            {MX - R_TORRE, Y + 1, MZ}, {MX - R_TORRE, Y + 2, MZ}, {MX - R_TORRE, Y + 3, MZ}};

    /** Abre (quita la reja) o cierra la puerta del molino; con {@code sonido}, el chirrido y el polvo. Devuelve si cambio algo. */
    public static boolean puerta(ServerLevel m, boolean abierta, boolean sonido) {
        boolean cambio = false;
        for (int[] r : REJA) {
            var b = new net.minecraft.core.BlockPos(r[0], r[1], r[2]);
            if (!m.isLoaded(b)) return false;
            var st = m.getBlockState(b);
            if (abierta && st.is(Blocks.IRON_BARS)) {
                m.setBlock(b, Blocks.AIR.defaultBlockState(), 3);
                cambio = true;
            } else if (!abierta && st.isAir()) {
                m.setBlock(b, Blocks.IRON_BARS.defaultBlockState(), 3);
                cambio = true;
            }
        }
        if (cambio && sonido) {
            m.playSound(null, MX - R_TORRE + 0.5, Y + 2, MZ, net.minecraft.sounds.SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.6f, 0.6f);
            m.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE, MX - R_TORRE + 0.5, Y + 2.5, MZ, 20, 0.4, 0.8, 0.8, 0.02);
        }
        return cambio;
    }

    /** Los barrotes de la reja (para las vistas y las pruebas). */
    public static int[][] reja() {
        return REJA;
    }

    /** Si la Molinera ha caido (su puesto). */
    public static boolean vencida(net.minecraft.server.MinecraftServer srv) {
        return com.darkcraft.enemigos.Puestos.muerto(srv, MorvainEnemigos.MOLINO, MorvainEnemigos.molinera());
    }


    /**
     * Cada 5 s, con alguien cerca: la reja de la puerta como toca (abierta si la Molinera ha caido) y, mientras vive, el
     * crujido del molino con el viento.
     */
    public static void crujir(ServerLevel m) {
        boolean alguien = false;
        for (var p : m.players())
            if (p.distanceToSqr(HX, HY, HZ) < 110 * 110) {
                alguien = true;
                break;
            }
        if (!alguien) return;
        boolean vencida = vencida(m.getServer());
        puerta(m, vencida, false);
        if (vencida) return;
        m.playSound(null, HX + 0.5, HY, HZ + 0.5, DarkCraftMod.SFX_MOLINO_VIENTO.get(), SoundSource.AMBIENT, 4.5f, 0.95f + m.random.nextFloat() * 0.1f);
    }

    /** Para la prueba: que hay en la era y en el molino. */
    public static String resumen(ServerLevel m) {
        int[] cofre = get().sitio("cofre_molino");
        var be = m.getBlockEntity(new net.minecraft.core.BlockPos(cofre[0], cofre[1], cofre[2]));
        int losas = 0, total = 0;
        for (int x = EX - R_ERA; x <= EX + R_ERA; x++)
            for (int z = EZ - R_ERA; z <= EZ + R_ERA; z++) {
                if (Math.hypot(x - EX, z - EZ) > R_ERA - 0.5) continue;
                total++;
                var st = m.getBlockState(new net.minecraft.core.BlockPos(x, Y, z));
                if (!st.isAir() && m.getBlockState(new net.minecraft.core.BlockPos(x, Y + 2, z)).isAir()) losas++;
            }
        var muela = m.getBlockState(new net.minecraft.core.BlockPos(EX + 3, Y + 1, EZ));
        var cubo = m.getBlockState(new net.minecraft.core.BlockPos(HX, HY, HZ));
        return "era " + losas + "/" + total + " despejada, muela " + muela.getBlock().getDescriptionId().replace("block.minecraft.", "")
                + ", cubo de las aspas " + cubo.getBlock().getDescriptionId().replace("block.minecraft.", "")
                + ", arcon " + (be instanceof net.minecraft.world.level.block.entity.ChestBlockEntity ? "SI" : "NO (" + be + ")")
                + ", reja " + (m.getBlockState(new net.minecraft.core.BlockPos(REJA[1][0], REJA[1][1], REJA[1][2])).is(Blocks.IRON_BARS) ? "cerrada" : "abierta");
    }
}
