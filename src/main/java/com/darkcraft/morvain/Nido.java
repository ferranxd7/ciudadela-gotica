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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * El Nido de Ceniza (parte 7z): la repisa de las Montanas Nevadas donde vive Vorthak, el Dragon de Ceniza. u = x - {@link #CX},
 * v = z - {@link #CZ}; la montana sube al noroeste (v y u negativas), el valle baja al este.
 * <ul>
 * <li>La <b>repisa</b>: un llano redondo (radio {@link #R_ARENA}) cortado en la ladera, de suelo quemado (piedra negra, basalto,
 * carbon, grava y alguna brasa), con el corte de roca al noroeste y la nieve que vuelve en el borde (hasta {@link #R_MEZCLA});
 * cuatro <b>penascos</b> (detras de ellos no llega el fuego del dragon), troncos quemados y los huesos de los que subieron a
 * matarlo (calaveras, montones de hueso y lanzas rotas).</li>
 * <li>El <b>tesoro</b>, al fondo (noroeste): un monticulo de oro con campanas, velas y el arcon del tesoro; encima duerme
 * Vorthak.</li>
 * <li>La <b>Atalaya del Cuervo</b>, al borde de la repisa por el este (mirando al valle): la torre redonda de la guarnicion que
 * guardaba el paso, quemada y medio caida; dentro, el atril con el <b>libro perdido 33</b> (el cuaderno del vigia) y un arcon, y
 * una escalera de mano a lo que queda del piso de arriba.</li>
 * <li>Al sur, el camino que sube del santuario del Nido a la repisa.</li>
 * </ul>
 * Se dibuja chunk a chunk al adornar; en su caja {@link Adorno} no pone nada. Los calcinados (la guarnicion de la atalaya, que
 * ardio con ella) esperan en el camino y en la torre; Vorthak, dormido sobre su tesoro.
 */
public final class Nido {
    public static final long SEMILLA = 0x0D7A_6011_D0E5L;
    /** El centro de la repisa. */
    public static final int CX = -5040, CZ = -4200;
    /** El suelo de la repisa (lo que se pisa esta en {@link #PISO}). */
    public static final int SUELO = 285, PISO = SUELO + 1;
    /** La repisa: lo llano (radio) y hasta donde se funde con la montana. */
    public static final double R_ARENA = 30, R_MEZCLA = 46;
    /** El tesoro (el monticulo de oro donde duerme el dragon): su centro (u, v), su radio y lo alto que llega. */
    public static final int TESORO_U = -13, TESORO_V = -13;
    public static final double R_TESORO = 7.5;
    static final int ALTO_TESORO = 2;
    /** La Atalaya del Cuervo: su centro (u, v), el radio de fuera, lo alto del piso de arriba (sobre el suelo) y de los muros. */
    public static final int ATALAYA_U = 32, ATALAYA_V = -4;
    static final double R_TORRE = 4.5;
    static final int PISO_TORRE = 7, ALTO_TORRE = 14;
    /** El santuario del Nido (en {@link SantuariosMorvain}): al sur, al pie del camino. */
    public static final int SANT_U = 18, SANT_V = 62;
    public static final int X0 = CX - 50, X1 = CX + 50, Z0 = CZ - 50, Z1 = CZ + 49;

    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/nido"),
            BOTIN_TESORO = new ResourceLocation(DarkCraftMod.MODID, "chests/nido_tesoro");
    /** El libro perdido que esta en el atril de la atalaya. */
    public static final int LIBRO = 33;

    /** Los penascos de la repisa: {u, v, radio, alto}. */
    static final double[][] PENASCOS = {{-4, 14, 2.6, 4}, {15, -9, 2.4, 3}, {-19, 6, 2.5, 4}, {9, 19, 2.2, 3}};
    /** Los troncos quemados: {u, v, alto}. */
    static final int[][] TRONCOS = {{-27, 18, 5}, {24, 23, 4}, {-33, -6, 6}, {36, 17, 3}, {-8, -36, 5}, {27, -24, 4}, {-38, 24, 4}};

    /** Uno del nido: donde espera (y: donde tiene los pies), hacia donde mira, su variante, como espera, cuanto ronda y su grupo. */
    public record Espera(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    private static Nido unico;

    public static synchronized Nido get() {
        if (unico == null) unico = new Nido();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    /** Lo que hay (para la prueba y las vistas). */
    public int calaveras, huesos, lanzas, campanas, velas;

    private Nido() {
        elementos.add(new Obra());
        Pilotes.Lista de = new Pilotes.Lista();
        tesoro(de);
        campanas(de);
        atalaya(de);
        restos(de);
        elementos.add(de.pieza(4));
        trozos();
        // ---- los sitios con nombre
        sitio("santuario", SANT_U - 2, alto(SANT_U - 2, SANT_V - 7) + 1, SANT_V - 7);
        sitio("camino", 12, altura(12, 40) + 1, 40);
        sitio("entrada", 8, PISO, 27);
        sitio("centro", 0, PISO, 0);
        sitio("tesoro", TESORO_U, PISO + ALTO_TESORO, TESORO_V);
        sitio("dragon", TESORO_U, PISO + ALTO_TESORO, TESORO_V);
        sitio("atalaya", ATALAYA_U - 7, PISO, ATALAYA_V);
        sitio("torre", ATALAYA_U, PISO, ATALAYA_V);
        sitio("torre_alto", ATALAYA_U + 1, PISO + PISO_TORRE, ATALAYA_V + 1);
        sitio("atril", ATALAYA_U, PISO, ATALAYA_V - 3);
        sitio("arcon", ATALAYA_U, PISO, ATALAYA_V + 3);
        sitio("arcon_tesoro", TESORO_U + 6, PISO, TESORO_V + 4);
        sitio("alto", 30, PISO + 26, 40);
        // ---- los puestos
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo del nido (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en lo del nido: ahi {@link Adorno} no pone nada. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= X0 && x0 <= X1 && z0 + 15 >= Z0 && z0 <= Z1;
    }

    /** Lo lejos de (x, z) al centro de la repisa. */
    public static double distancia(double x, double z) {
        return Math.hypot(x - CX - 0.5, z - CZ - 0.5);
    }

    /** Si (x, z) es de la repisa (para el nombre del sitio y el logro). */
    public static boolean dentro(double x, double z) {
        return distancia(x, z) < R_ARENA + 4;
    }

    /**
     * Para que el dragon no se suba andando a lo que hay en la repisa: aparta (x, z) de los penascos, los troncos quemados y la
     * atalaya (a su radio mas {@code margen}, lo que ocupa el). Devuelve {x, z}.
     */
    public static double[] apartar(double x, double z, double margen) {
        double u = x - CX - 0.5, v = z - CZ - 0.5;
        double[] q = {u, v};
        for (double[] p : PENASCOS) empujar(q, p[0], p[1], p[2] + margen);
        for (int[] t : TRONCOS) empujar(q, t[0], t[1], 0.8 + margen);
        empujar(q, ATALAYA_U, ATALAYA_V, R_TORRE + margen);
        return new double[]{q[0] + CX + 0.5, q[1] + CZ + 0.5};
    }

    private static void empujar(double[] q, double u, double v, double r) {
        double du = q[0] - u, dv = q[1] - v, d = Math.hypot(du, dv);
        if (d >= r) return;
        if (d < 1e-4) {
            du = 1;
            dv = 0;
            d = 1;
        }
        q[0] = u + du / d * r;
        q[1] = v + dv / d * r;
    }

    /** Si (x, y, z) esta en la Atalaya del Cuervo (o a su puerta). */
    public static boolean enAtalaya(double x, double y, double z) {
        return Math.hypot(x - CX - ATALAYA_U - 0.5, z - CZ - ATALAYA_V - 0.5) < R_TORRE + 2 && y > SUELO - 2 && y < PISO + ALTO_TORRE + 2;
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} del nido en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los del nido (los apunta {@link MorvainEnemigos}): Vorthak el primero. */
    public List<Espera> esperas() {
        return esperas;
    }

    /** Los sitios con nombre (la entrada, el centro, el tesoro, la atalaya, el atril...): {x, y, z} (y: los pies). */
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

    static int pct(int u, int y, int v, long sem) {
        return Hash.pct(x(u), y, z(v), SEMILLA + sem);
    }

    /** Lo alto del terreno natural en (u, v) (el bloque de arriba). */
    static int alto(int u, int v) {
        return Cerco.alto(x(u), z(v));
    }

    static double suave(double t) {
        t = Math.max(0, Math.min(1, t));
        return t * t * (3 - 2 * t);
    }

    /** Lo alto de lo que se pisa en (u, v) despues de la obra (sin lo que se pone encima): la repisa y su borde. */
    static int altura(int u, int v) {
        double r = Math.hypot(u, v);
        if (r < R_ARENA) return SUELO;
        int n = alto(u, v);
        if (r >= R_MEZCLA) return n;
        double t = suave((r - R_ARENA) / (R_MEZCLA - R_ARENA));
        // cuesta arriba (la montana) se corta mas tieso; cuesta abajo se funde mas despacio
        if (n > SUELO) t = Math.pow(t, 0.8);
        return (int) Math.round(SUELO + (n - SUELO) * t);
    }

    /** Si (u, v) esta junto al santuario (su tarima la pone {@link SantuariosMorvain}): ahi la obra no toca nada. */
    static boolean junto(int u, int v) {
        return Math.hypot(u - SANT_U, v - SANT_V) < 13;
    }

    /** Lo quemado de una columna: 1 en la repisa, baja en el borde (alli vuelve la nieve). */
    static double quemado(int u, int v) {
        double r = Math.hypot(u, v);
        if (r < R_ARENA - 3) return 1;
        return 1 - suave((r - R_ARENA + 3) / (R_MEZCLA - R_ARENA - 2));
    }

    /** El suelo quemado de la repisa. */
    static BlockState sueloQuemado(int u, int v) {
        double n = Suelo.P.fbm(x(u), z(v), 7, 2);
        int p = pct(u, SUELO, v, 1);
        double r = Math.hypot(u, v);
        if (r > 6 && n > 0.48 && p < 30 && !camino(u, v)) return b(Blocks.MAGMA_BLOCK);
        if (n > 0.3) return p < 50 ? b(Blocks.BLACKSTONE) : b(Blocks.BASALT);
        if (n > 0.05) return p < 40 ? b(Blocks.COAL_BLOCK) : p < 70 ? b(Blocks.BLACKSTONE) : b(Blocks.SMOOTH_BASALT);
        if (n > -0.25) return p < 45 ? b(Blocks.TUFF) : p < 70 ? b(Blocks.GRAVEL) : b(Blocks.BLACKSTONE);
        return p < 40 ? b(Blocks.COARSE_DIRT) : p < 70 ? b(Blocks.GRAVEL) : b(Blocks.SOUL_SOIL);
    }

    /** La roca del corte de la montana (a capas, chamuscada abajo). */
    static BlockState roca(int u, int y, int v) {
        int p = pct(u, y, v, 3);
        int capa = Math.floorMod(y / 2 + (u + v) / 7, 5);
        if (y <= SUELO + 2 && p < 45) return p < 25 ? b(Blocks.BLACKSTONE) : b(Blocks.BASALT);
        return switch (capa) {
            case 0 -> b(Blocks.STONE);
            case 1 -> p < 50 ? b(Blocks.ANDESITE) : b(Blocks.STONE);
            case 2 -> b(Blocks.TUFF);
            case 3 -> p < 60 ? b(Blocks.STONE) : b(Blocks.COBBLESTONE);
            default -> p < 50 ? b(Blocks.DEEPSLATE) : b(Blocks.ANDESITE);
        };
    }

    /** El camino del santuario a la repisa: de (SANT_U - 2, SANT_V - 8) a la entrada (8, 27). */
    static boolean camino(int u, int v) {
        double ax = SANT_U - 2, az = SANT_V - 8, bx = 8, bz = 27;
        double dx = bx - ax, dz = bz - az, l2 = dx * dx + dz * dz;
        double t = Math.max(0, Math.min(1, ((u - ax) * dx + (v - az) * dz) / l2));
        double px = ax + dx * t - u, pz = az + dz * t - v;
        return px * px + pz * pz <= 1.6 * 1.6;
    }

    /** Todo lo grande, columna a columna: la repisa (se corta o se rellena), su borde fundido con la montana y el camino. */
    private final class Obra extends Element {
        Obra() {
            super(SEMILLA + 51);
            this.layer = 1;
            bounds(X0, SUELO - 6, Z0, X1, 360, Z1);
        }

        @Override
        public void build(Canvas c) {
            for (int x = Math.max(c.minX, x0); x <= Math.min(c.maxX, x1); x++)
                for (int z = Math.max(c.minZ, z0); z <= Math.min(c.maxZ, z1); z++) columna(c, x, z);
        }
    }

    static void columna(Canvas c, int x, int z) {
        int u = x - CX, v = z - CZ;
        if (junto(u, v)) return;
        double r = Math.hypot(u, v);
        int n = alto(u, v);
        boolean senda = camino(u, v);
        if (r >= R_MEZCLA && !senda) return;
        int top = r >= R_MEZCLA ? n : altura(u, v);
        // la atalaya va sobre su propio llano
        double rt = Math.hypot(u - ATALAYA_U, v - ATALAYA_V);
        if (rt < R_TORRE + 3) top = SUELO;
        else if (rt < R_TORRE + 6 && top < SUELO) top = (int) Math.round(SUELO - (SUELO - top) * suave((rt - R_TORRE - 3) / 3));
        // lo de encima, fuera; lo de debajo, relleno
        for (int y = top + 1; y <= Math.max(n, top) + 3; y++) c.set(x, y, z, Pal.AIR);
        for (int y = Math.min(n, top) - 2; y < top; y++) {
            if (y <= top - 3) c.set(x, y, z, roca(u, y, v));
            else c.set(x, y, z, r < R_ARENA ? b(Blocks.BLACKSTONE) : roca(u, y, v));
        }
        double q = quemado(u, v);
        int p = pct(u, top, v, 5);
        // el corte de la montana: donde el borde sube deprisa, roca
        int dn = Math.abs(altura(u + 1, v) - altura(u - 1, v)) + Math.abs(altura(u, v + 1) - altura(u, v - 1));
        BlockState arriba;
        if (senda && r >= R_ARENA - 1) arriba = p < 45 ? b(Blocks.GRAVEL) : p < 75 ? b(Blocks.COBBLESTONE) : b(Blocks.STONE_BRICKS);
        else if (dn >= 4) arriba = roca(u, top, v);
        else if (p < q * 100) arriba = sueloQuemado(u, v);
        else arriba = p % 3 == 0 ? b(Blocks.STONE) : b(Blocks.SNOW_BLOCK);
        c.set(x, top, z, arriba);
        // la nieve que vuelve en el borde (capas finas encima de lo que no esta quemado)
        if (r > R_ARENA && q < 0.5 && dn < 4 && !senda && arriba.is(Blocks.SNOW_BLOCK) && p % 4 == 1)
            c.set(x, top + 1, z, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1 + p % 3));
        // las cenizas: alguna capa de nieve gris (la ceniza que cae) no; humo no: solo bloques
    }

    // ------------------------------------------------------------------ el tesoro

    /** Lo alto del monticulo del tesoro en (u, v) (0 fuera; 1 o 2 encima del suelo). */
    static int monticulo(int u, int v) {
        double r = Math.hypot(u - TESORO_U, v - TESORO_V);
        if (r >= R_TESORO) return 0;
        double h = ALTO_TESORO * (1 - (r / R_TESORO) * (r / R_TESORO)) + 0.35;
        return (int) Math.floor(h);
    }

    private static void put(Pilotes.Lista m, int u, int y, int v, BlockState s) {
        m.put(x(u), y, z(v), s);
    }

    private static void une(Pilotes.Lista m, int u, int y, int v, BlockState s) {
        m.une(x(u), y, z(v), s);
    }

    private void tesoro(Pilotes.Lista m) {
        int R = (int) Math.ceil(R_TESORO);
        for (int du = -R; du <= R; du++)
            for (int dv = -R; dv <= R; dv++) {
                int u = TESORO_U + du, v = TESORO_V + dv;
                double r = Math.hypot(du, dv);
                if (r >= R_TESORO) continue;
                int h = monticulo(u, v);
                for (int k = 0; k <= h; k++) {
                    int y = SUELO + k;
                    int p = pct(u, y, v, 11);
                    BlockState s = p < 34 ? b(Blocks.GOLD_BLOCK) : p < 60 ? b(Blocks.RAW_GOLD_BLOCK) : p < 78 ? b(Blocks.GILDED_BLACKSTONE)
                            : p < 88 ? b(Blocks.GOLD_BLOCK) : b(Blocks.BLACKSTONE);
                    // por el borde, ya sobre el suelo quemado: el oro suelto
                    if (k == 0 && r > R_TESORO - 1.2 && p % 3 != 0) s = p < 60 ? b(Blocks.GILDED_BLACKSTONE) : b(Blocks.BLACKSTONE);
                    put(m, u, y, v, s);
                }
                // encima: velas, alguna calavera y alguna campana (cosas que trajeron los que vinieron a robarle)
                int y = SUELO + h + 1, p = pct(u, y, v, 12);
                if (r < 2.2) continue;                                   // donde duerme
                if (p < 5) {
                    put(m, u, y, v, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 1 + p % 4).setValue(CandleBlock.LIT, true));
                    velas++;
                } else if (p < 8) {
                    put(m, u, y, v, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, p * 5 % 16));
                    calaveras++;
                }
            }
    }

    /** Las campanas que se trajeron de los pueblos del valle (dos, en el monticulo). */
    private void campanas(Pilotes.Lista m) {
        int[][] c = {{TESORO_U + 3, TESORO_V - 3, 1}, {TESORO_U - 4, TESORO_V + 2, 2}};
        for (int[] q : c) {
            put(m, q[0], SUELO + monticulo(q[0], q[1]) + 1, q[1], Blocks.BELL.defaultBlockState().setValue(BellBlock.ATTACHMENT, BellAttachType.FLOOR)
                    .setValue(BellBlock.FACING, Direction.from2DDataValue(q[2])));
            campanas++;
        }
    }

    // ------------------------------------------------------------------ la Atalaya del Cuervo

    /** Lo alto (sobre el suelo) del muro de la torre en el angulo a (grados desde el este): rota; mas por el lado de la repisa. */
    static int altoMuro(double a) {
        // el lado del oeste (de cara a la repisa, a 180 grados) se vino abajo con el fuego
        double oeste = Math.abs(((a - 180 + 540) % 360) - 180);
        int h = ALTO_TORRE - (int) Math.round(2.5 * (1 + Math.sin(Math.toRadians(a * 3 + 40))));
        if (oeste < 55) h = Math.min(h, 3 + (int) Math.round(oeste / 12.0));
        return h;
    }

    /** La piedra de la torre (piedra de la guarnicion, negra donde la quemo el dragon: mas arriba y por el oeste). */
    static BlockState piedraTorre(int u, int y, int v, double a) {
        int p = pct(u, y, v, 21);
        double oeste = Math.abs(((a - 180 + 540) % 360) - 180);
        double neg = (y - SUELO) / (double) ALTO_TORRE * 60 + (oeste < 70 ? 35 : 0);
        if (p < neg) return p % 3 == 0 ? b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS) : b(Blocks.POLISHED_BLACKSTONE_BRICKS);
        return p % 4 == 0 ? b(Blocks.CRACKED_STONE_BRICKS) : p % 7 == 0 ? b(Blocks.COBBLESTONE) : b(Blocks.STONE_BRICKS);
    }

    private void atalaya(Pilotes.Lista m) {
        int R = (int) Math.ceil(R_TORRE);
        for (int du = -R; du <= R; du++)
            for (int dv = -R; dv <= R; dv++) {
                double r = Math.hypot(du, dv);
                if (r > R_TORRE) continue;
                int u = ATALAYA_U + du, v = ATALAYA_V + dv;
                double a = Math.toDegrees(Math.atan2(dv, du));
                if (r > R_TORRE - 1.05) {
                    // el muro (la puerta al oeste: dos de ancho y tres de alto; aspilleras)
                    int h = altoMuro(a);
                    for (int k = 0; k <= h; k++) {
                        int y = SUELO + k;
                        boolean puerta = du <= -3 && Math.abs(dv) <= 0 && k >= 1 && k <= 3;
                        boolean aspillera = (k == 5 || k == 11) && (Math.abs(dv) == 0 && du >= 3 || Math.abs(du) == 0 && Math.abs(dv) >= 3);
                        put(m, u, y, v, puerta || aspillera ? Pal.AIR : piedraTorre(u, y, v, a));
                    }
                } else {
                    // el suelo de dentro y el piso de arriba (medio quemado: le faltan tablas)
                    put(m, u, SUELO, v, pct(u, SUELO, v, 22) < 70 ? b(Blocks.STONE_BRICKS) : b(Blocks.COBBLESTONE));
                    for (int k = 1; k <= ALTO_TORRE; k++) put(m, u, SUELO + k, v, Pal.AIR);
                    int p = pct(u, SUELO + PISO_TORRE, v, 23);
                    boolean hueco = du <= -1 && dv <= 1 && p < 70;            // el trozo que se hundio, al oeste
                    if (!hueco && (p < 82 || du >= 0))
                        put(m, u, SUELO + PISO_TORRE, v, p < 30 ? b(Blocks.DARK_OAK_PLANKS) : p < 60 ? b(Blocks.SPRUCE_PLANKS) : b(Blocks.DARK_OAK_SLAB));
                }
            }
        // la escalera de mano (en el muro del este, por dentro) del suelo al piso de arriba
        for (int k = 1; k <= PISO_TORRE; k++)
            put(m, ATALAYA_U + 3, SUELO + k, ATALAYA_V, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST));
        put(m, ATALAYA_U + 3, SUELO + PISO_TORRE, ATALAYA_V, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST));
        // arriba, el brasero de las senales (frio) y una campana caida
        put(m, ATALAYA_U + 1, SUELO + PISO_TORRE + 1, ATALAYA_V + 2, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
        put(m, ATALAYA_U - 1, SUELO + PISO_TORRE + 1, ATALAYA_V - 2, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, 6));
        // abajo: escombros, cadenas, una calavera; delante de la puerta, la piedra que se cayo
        put(m, ATALAYA_U - 2, SUELO + 1, ATALAYA_V + 2, b(Blocks.COBBLESTONE));
        put(m, ATALAYA_U + 1, SUELO + 1, ATALAYA_V - 2, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, 12));
        put(m, ATALAYA_U - 1, SUELO + 6, ATALAYA_V - 3, b(Blocks.CHAIN));
        calaveras += 2;
        int[][] caidas = {{-6, 2, 2}, {-7, -2, 1}, {-6, -3, 1}, {-8, 1, 1}, {-5, 4, 1}};
        for (int[] q : caidas)
            for (int k = 1; k <= q[2]; k++)
                put(m, ATALAYA_U + q[0], SUELO + k, ATALAYA_V + q[1], pct(q[0], k, q[1], 24) < 50 ? b(Blocks.COBBLESTONE)
                        : b(Blocks.POLISHED_BLACKSTONE_BRICKS));
    }

    // ------------------------------------------------------------------ lo de la repisa

    private void restos(Pilotes.Lista m) {
        // los penascos (detras, a salvo del fuego)
        for (double[] q : PENASCOS) {
            int R = (int) Math.ceil(q[2]);
            for (int du = -R; du <= R; du++)
                for (int dv = -R; dv <= R; dv++) {
                    double r = Math.hypot(du, dv) / q[2];
                    if (r > 1) continue;
                    int u = (int) q[0] + du, v = (int) q[1] + dv;
                    int h = (int) Math.round(q[3] * Math.sqrt(1 - r * r) + 0.3);
                    for (int k = 1; k <= h; k++) {
                        int p = pct(u, SUELO + k, v, 31);
                        put(m, u, SUELO + k, v, p < 30 ? b(Blocks.BLACKSTONE) : p < 55 ? b(Blocks.BASALT) : p < 80 ? b(Blocks.STONE) : b(Blocks.COBBLED_DEEPSLATE));
                    }
                }
        }
        // los troncos quemados (de basalto, con alguna rama)
        for (int[] t : TRONCOS) {
            int u = t[0], v = t[1], y0 = altura(u, v) + 1;
            for (int k = 0; k < t[2]; k++) put(m, u, y0 + k, v, Blocks.BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
            int p = pct(u, y0, v, 32);
            Direction d = Direction.from2DDataValue(p % 4);
            put(m, u + d.getStepX(), y0 + t[2] - 2, v + d.getStepZ(), Blocks.BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, d.getAxis()));
            une(m, u + d.getStepX() * 2, y0 + t[2] - 1, v + d.getStepZ() * 2, b(Blocks.DARK_OAK_FENCE));
            Direction e = d.getOpposite();
            une(m, u + e.getStepX(), y0 + t[2] - 1, v + e.getStepZ(), b(Blocks.DARK_OAK_FENCE));
        }
        // los huesos de los que subieron a matarlo: calaveras, montones de hueso y lanzas rotas clavadas
        for (int k = 0; k < 64; k++) {
            int p = Hash.pct(k, 7, 3, SEMILLA + 41);
            double a = Math.toRadians(Hash.pct(k, 1, 9, SEMILLA + 42) * 3.6), r = 6 + Hash.pct(k, 2, 4, SEMILLA + 43) * 0.23;
            int u = (int) Math.round(Math.cos(a) * r), v = (int) Math.round(Math.sin(a) * r);
            if (monticulo(u, v) > 0 || camino(u, v) || cercaPenasco(u, v) || Math.hypot(u - ATALAYA_U, v - ATALAYA_V) < R_TORRE + 2) continue;
            int y = PISO;
            if (p < 40) {
                put(m, u, y, v, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, (k * 5) % 16));
                calaveras++;
            } else if (p < 70) {
                put(m, u, y, v, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, p % 2 == 0 ? Direction.Axis.X : Direction.Axis.Z));
                if (p % 5 == 0) put(m, u, y + 1, v, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, (k * 7) % 16));
                huesos++;
            } else if (p < 85) {
                // una lanza rota (barrotes de hierro, de uno o dos)
                une(m, u, y, v, b(Blocks.IRON_BARS));
                if (p % 2 == 0) une(m, u, y + 1, v, b(Blocks.IRON_BARS));
                lanzas++;
            }
        }
    }

    static boolean cercaPenasco(int u, int v) {
        for (double[] q : PENASCOS) if (Math.hypot(u - q[0], v - q[1]) < q[2] + 1.5) return true;
        return false;
    }

    private void trozos() {
        // el atril del vigia (al norte de la torre, de cara al sur) y el arcon de la guarnicion (al sur)
        trozo(ATALAYA_U, PISO, ATALAYA_V - 3, c -> c.lectern(x(ATALAYA_U), PISO, z(ATALAYA_V - 3), Direction.SOUTH, Perdidos.libro(LIBRO)));
        trozo(ATALAYA_U, PISO, ATALAYA_V + 3, c -> c.chest(x(ATALAYA_U), PISO, z(ATALAYA_V + 3), Direction.NORTH, BOTIN));
        // el arcon del tesoro, al pie del monticulo, de cara a la repisa
        trozo(TESORO_U + 6, PISO, TESORO_V + 4, c -> c.chest(x(TESORO_U + 6), PISO, z(TESORO_V + 4), Direction.SOUTH, BOTIN_TESORO));
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
        // Vorthak, el Dragon de Ceniza (el puesto 0): dormido sobre su tesoro, de cara a la repisa (al sureste)
        en(0, TESORO_U, PISO + ALTO_TESORO, TESORO_V, giro(-TESORO_U, -TESORO_V), Re, 0, Puestos.JEFE);
        // el camino: los de la guarnicion que no llegaron a la torre, entre la nieve
        en(C, 14, altura(14, 45) + 1, 45, giro(-6, 16), Tu, 0, "camino");
        en(C, 10, altura(10, 33) + 1, 33, NORTE, Tu, 0, "camino");
        // la atalaya: a la puerta, dentro y arriba (el que tira cantaros a la repisa)
        en(G, ATALAYA_U - 7, PISO, ATALAYA_V + 2, OESTE, D, 0, "atalaya");
        en(C, ATALAYA_U - 1, PISO, ATALAYA_V + 1, ESTE, Re, 0, "atalaya");
        en(I, ATALAYA_U + 1, PISO + PISO_TORRE, ATALAYA_V + 1, OESTE, D, 0, "atalaya");
    }

    private void en(int variante, int u, int y, int v, float giro, int postura, int ronda, String grupo) {
        esperas.add(new Espera(variante, x(u), y, z(v), giro, postura, ronda, grupo));
    }

    // ------------------------------------------------------------------ lo que pasa

    /** Lo que hay en el nido ya generado (para la prueba): el atril con su libro, los arcones, el oro, la repisa llana... */
    public static String resumen(ServerLevel m) {
        Nido a = get();
        int[] at = a.sitio("atril"), ar = a.sitio("arcon"), ab = a.sitio("arcon_tesoro");
        boolean libro = m.getBlockEntity(new BlockPos(at[0], at[1], at[2])) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le
                && Perdidos.de(le.getBook()) == LIBRO;
        boolean arcon = m.getBlockState(new BlockPos(ar[0], ar[1], ar[2])).is(Blocks.CHEST);
        boolean tesoro = m.getBlockState(new BlockPos(ab[0], ab[1], ab[2])).is(Blocks.CHEST);
        int oro = 0, llano = 0, total = 0, aire = 0;
        for (int du = -8; du <= 8; du++)
            for (int dv = -8; dv <= 8; dv++) {
                BlockState s = m.getBlockState(new BlockPos(x(TESORO_U + du), SUELO + 1, z(TESORO_V + dv)));
                if (s.is(Blocks.GOLD_BLOCK) || s.is(Blocks.RAW_GOLD_BLOCK) || s.is(Blocks.GILDED_BLACKSTONE)) oro++;
            }
        for (int k = 0; k < 24; k++) {
            double ang = Math.PI * 2 * k / 24;
            int u = (int) Math.round(Math.cos(ang) * 22), v = (int) Math.round(Math.sin(ang) * 22);
            if (monticulo(u, v) > 0 || cercaPenasco(u, v)) continue;
            total++;
            BlockPos p = new BlockPos(x(u), SUELO, z(v));
            if (!m.getBlockState(p).isAir() && m.getBlockState(p.above()).getCollisionShape(m, p.above()).isEmpty()) llano++;
            if (m.getBlockState(p.above(3)).isAir() && m.getBlockState(p.above(8)).isAir()) aire++;
        }
        int muro = 0;
        for (int k = 0; k < 16; k++) {
            double ang = Math.PI * 2 * k / 16;
            BlockPos p = new BlockPos(x(ATALAYA_U + (int) Math.round(Math.cos(ang) * 4)), SUELO + 2, z(ATALAYA_V + (int) Math.round(Math.sin(ang) * 4)));
            if (!m.getBlockState(p).isAir()) muro++;
        }
        return "atril con el libro " + LIBRO + " " + (libro ? "SI" : "NO") + ", arcon " + (arcon ? "SI" : "NO") + ", arcon del tesoro " + (tesoro ? "SI" : "NO")
                + ", oro del tesoro " + oro + ", la repisa llana " + llano + "/" + total + " (despejada encima " + aire + "), el muro de la atalaya "
                + muro + "/16";
    }
}
