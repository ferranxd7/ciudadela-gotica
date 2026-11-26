package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Calcinado;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.fortaleza.Perdidos;
import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Pal;
import com.darkcraft.gen.Rect;
import com.darkcraft.gen.Rng;
import com.darkcraft.pueblo.gen.Casa;
import com.darkcraft.pueblo.gen.Trozo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;
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
 * La Puerta Oeste de la Capital (parte 7m): por donde se entra a la Capital de Morvain viniendo del santuario de la Puerta de
 * la Capital, y el barrio que quedo detras, quemado. La noche que el cielo se puso rojo el Condestable del Juramento mando
 * cerrar las puertas para que nadie saliera; el fuego empezo en el palacio, al otro lado del rio, y el viento lo trajo. La
 * guardia de la puerta no abrio (libro perdido 22): la gente ardio en las calles y los soldados en la muralla, y no se
 * apagaron del todo: son los calcinados ({@code enemigos/Calcinado}).
 * <p>
 * La muralla corre de norte a sur (sillares ennegrecidos, almenas, el adarve, cuatro torres y dos trozos caidos); en medio,
 * la <b>puerta</b>: dos torres, el paso abovedado con el rastrillo medio bajado y, encima, la sala de la guardia (el atril
 * con el libro 22), a la que se sube por la torre del norte. Fuera, el camino del santuario con los carros quemados de los
 * que quisieron salir. Dentro, la <b>calle mayor</b> hacia el este entre casas quemadas (sin tejado casi todas, el
 * entramado negro), la calle del cruce de norte a sur, la ronda pegada a la muralla, y al final la <b>plaza del Juramento</b>
 * con la estatua del caballero de la mano alzada derribada y, al norte, el <b>cuartel del Juramento</b>. Montones de ceniza
 * (alli esperan los cenicientos), escombros y rescoldos por todas partes.
 * <p>
 * Se dibuja como la aldea de los pilotes (chunk a chunk al adornar; en su caja {@link Adorno} no pone nada).
 */
public final class Capital {
    public static final long SEMILLA = 0xCA_9172_7A1L;
    /** El eje: la muralla del oeste (x) y el camino de la puerta (z). */
    public static final int CX = 3240, CZ = 345;
    /** El suelo de la ciudad (lo que se pisa esta en {@link #PISO}). */
    public static final int SUELO = 93, PISO = SUELO + 1;
    /** La ciudad (u = x - CX, v = z - CZ): de la muralla (u -2..1) hacia el este. */
    static final int U0 = -2, U1 = 118, V0 = -72, V1 = 72;
    /** Lo que se funde con el terreno alrededor de la ciudad. */
    static final double FUNDE = 10;
    /** La plaza del Juramento (centro y radio) y el cruce. */
    public static final int PLAZA_U = 96, PLAZA_R = 15, CRUCE_U = 44;
    /** La muralla: lo alto de lo macizo (el adarve se pisa encima) y las almenas. */
    static final int ADARVE = PISO + 8, ALMENAS = PISO + 10;
    /** El santuario de la Puerta de la Capital (de SantuariosMorvain). */
    static final int SANT_X = 3177, SANT_Z = 343;
    /** Lo que toca: la ciudad, lo que se funde y el camino del santuario. */
    public static final int X0 = CX - 72, X1 = CX + 130, Z0 = CZ - 84, Z1 = CZ + 84;
    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/capital"),
            BOTIN_BUENO = new ResourceLocation(DarkCraftMod.MODID, "chests/capital_bueno");

    private static final int AW = X1 - X0 + 1, AD = Z1 - Z0 + 1;
    private static final int[] ALTO = new int[AW * AD];

    static {
        Arrays.fill(ALTO, Integer.MIN_VALUE);
    }

    /** El bloque de arriba del terreno de Morvain en (x, z) (sin la ciudad). */
    public static int alto(int x, int z) {
        if (x < X0 || x > X1 || z < Z0 || z > Z1) return (int) Math.floor(Terreno.columna(x, z).altura());
        int i = (z - Z0) * AW + (x - X0);
        int v = ALTO[i];
        if (v == Integer.MIN_VALUE) ALTO[i] = v = (int) Math.floor(Terreno.columna(x, z).altura());
        return v;
    }

    /** Lo alto del suelo de la ciudad en (x, z): {@link #SUELO} dentro, fundido con el terreno alrededor; MIN_VALUE lejos. */
    static int objetivo(int x, int z) {
        double u = x - CX, v = z - CZ;
        double du = u < U0 ? U0 - u : u > U1 ? u - U1 : 0, dv = v < V0 ? V0 - v : v > V1 ? v - V1 : 0;
        double d = Math.hypot(du, dv);
        if (d <= 0) return SUELO;
        if (d >= FUNDE) return Integer.MIN_VALUE;
        double t = Ruido.suave(0, FUNDE, d);
        return (int) Math.round(Ruido.lerp(t, SUELO, alto(x, z)));
    }

    /** Lo que se pisa en (x, z): el suelo de la ciudad o el terreno. */
    static int pie(int x, int z) {
        int o = objetivo(x, z);
        return (o == Integer.MIN_VALUE ? alto(x, z) : o) + 1;
    }

    /** Un calcinado de la ciudad: donde espera (y: lo que pisa), hacia donde mira, su variante, como espera, cuanto ronda y su grupo. */
    public record Espera(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    private static Capital unico;

    public static synchronized Capital get() {
        if (unico == null) unico = new Capital();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    private final List<Rect> ocupado = new ArrayList<>();
    public final List<Casa> casas = new ArrayList<>();
    /** Lo que hay (para la prueba y las vistas). */
    public int torres, montones, escombros, rescoldos, carros, solaresHechos, arboles;
    /** Los montones de ceniza (u, v): alli esperan enterrados los cenicientos. */
    private final List<int[]> cenizas = new ArrayList<>();

    private Capital() {
        Rng rng = new Rng(SEMILLA);
        elementos.add(new Suelo());
        Pilotes.Lista mu = new Pilotes.Lista();
        muralla(mu);
        elementos.add(mu.pieza(3));
        Pilotes.Lista pu = new Pilotes.Lista();
        puerta(pu);
        elementos.add(pu.pieza(3));
        ocupar(new Rect(x(-6), z(V0 - 2), x(6), z(V1 + 2)));
        Pilotes.Lista cu = new Pilotes.Lista();
        cuartel(cu);
        elementos.add(cu.pieza(3));
        Pilotes.Lista pl = new Pilotes.Lista();
        plaza(pl);
        elementos.add(pl.pieza(4));
        casas(new Rng(rng.fork()));
        Pilotes.Lista so = new Pilotes.Lista();
        solares(so, new Rng(rng.fork()));
        elementos.add(so.pieza(3));
        Pilotes.Lista fu = new Pilotes.Lista();
        fuera(fu, new Rng(rng.fork()));
        elementos.add(fu.pieza(4));
        Pilotes.Lista es = new Pilotes.Lista();
        escombrosYCeniza(es, new Rng(rng.fork()));
        elementos.add(es.pieza(5));
        trozos();
        // ---- los sitios con nombre
        sitio("entrada", -52, pie(x(-52), z(-1)), -1);
        sitio("puerta", -9, PISO, 0);
        sitio("paso", 0, PISO, 0);
        sitio("camara", 1, ADARVE + 1, 0);
        sitio("atril", 0, ADARVE + 1, 2);
        sitio("arcon_camara", -3, ADARVE + 1, -2);
        sitio("adarve", -1, ADARVE + 1, -16);
        sitio("calle", 26, PISO, 0);
        sitio("cruce", CRUCE_U, PISO, 0);
        sitio("plaza", PLAZA_U - PLAZA_R + 3, PISO, 0);
        sitio("estatua", PLAZA_U, PISO, 0);
        sitio("cuartel", 96, PISO, -24);
        sitio("dentro_cuartel", 96, PISO, -33);
        sitio("arcon", 104, PISO, -42);
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo de la ciudad (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en la ciudad: ahi {@link Adorno} no pone nada. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= X0 && x0 <= X1 && z0 + 15 >= Z0 && z0 <= Z1;
    }

    /** Si (x, z) esta en la puerta o en el barrio (para el nombre del sitio y su logro). */
    public static boolean dentro(double x, double z) {
        double u = x - CX, v = z - CZ;
        return u >= -16 && u <= U1 + 2 && v >= V0 - 2 && v <= V1 + 2;
    }

    /** Si (x, z) esta ya dentro de la muralla (pasado el paso de la puerta o un trozo caido): para el logro de la puerta. */
    public static boolean intramuros(double x, double z) {
        double u = x - CX, v = z - CZ;
        return u >= 4 && u <= U1 + 2 && v >= V0 - 2 && v <= V1 + 2;
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} de la capital en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los calcinados de la ciudad (los apunta {@link MorvainEnemigos}). */
    public List<Espera> esperas() {
        return esperas;
    }

    /** Los sitios con nombre (la puerta, la camara, el atril, la plaza, el cuartel...): {x, y, z} (y: lo que se pisa). */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    private void sitio(String nombre, int u, int y, int v) {
        sitios.put(nombre, new int[]{x(u), y, z(v)});
    }

    private void ocupar(Rect r) {
        ocupado.add(r);
    }

    private boolean libre(Rect r, int margen) {
        for (Rect o : ocupado)
            if (r.x0 <= o.x1 + margen && r.x1 >= o.x0 - margen && r.z0 <= o.z1 + margen && r.z1 >= o.z0 - margen) return false;
        return true;
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

    /** Una hoguera encendida (sin humo de senal): los rescoldos. */
    static BlockState rescoldo() {
        return Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true).setValue(CampfireBlock.SIGNAL_FIRE, false);
    }

    /** La piedra de la muralla y las torres: sillares ennegrecidos por el fuego (mas hollin cuanto mas abajo y por dentro). */
    static BlockState piedra(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 3);
        int hollin = Hash.pct(x >> 1, y >> 1, z >> 1, SEMILLA + 4);
        if (hollin < 9) return p < 60 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS) : b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
        if (p < 36) return Pal.STONE_BRICKS;
        if (p < 54) return b(Blocks.CRACKED_STONE_BRICKS);
        if (p < 66) return b(Blocks.DEEPSLATE_BRICKS);
        if (p < 76) return b(Blocks.CRACKED_DEEPSLATE_BRICKS);
        if (p < 86) return Pal.ANDESITE;
        if (p < 94) return b(Blocks.TUFF);
        return b(Blocks.COBBLED_DEEPSLATE);
    }

    /** Ceniza en el suelo: una alfombra gris (fina) o gris clara. */
    static BlockState ceniza(int x, int y, int z) {
        return Hash.pct(x, y, z, SEMILLA + 5) < 60 ? b(Blocks.GRAY_CARPET) : b(Blocks.LIGHT_GRAY_CARPET);
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

    /** Lo mas bajo del terreno bajo (u, v) (para que lo macizo baje hasta el). */
    private static int base(int u, int v) {
        return Math.min(SUELO, alto(x(u), z(v))) - 1;
    }

    // ------------------------------------------------------------------ el suelo: la ciudad allanada, las calles y la plaza

    /** Si (u, v) es calle: la mayor (de la puerta a la plaza), la del cruce, la ronda junto a la muralla o la plaza. */
    static boolean calle(int u, int v) {
        if (Math.hypot(u - PLAZA_U, v) <= PLAZA_R + 0.4) return true;
        if (u >= 2 && u <= PLAZA_U && Math.abs(v) <= 3) return true;
        if (Math.abs(u - CRUCE_U) <= 2 && v >= V0 + 4 && v <= V1 - 4) return true;
        if (u >= 2 && u <= 6 && v >= V0 + 2 && v <= V1 - 2) return true;
        // del cuartel a la plaza
        return Math.abs(u - 96) <= 2 && v >= -22 && v <= -PLAZA_R + 2;
    }

    /** El camino del santuario a la puerta (fuera): {du, dv} a lo largo, medio ancho 2,4. */
    static boolean camino(int u, int v) {
        if (u > -5 || u < SANT_X - CX + 5 || Pilotes.cercaSantuario(x(u), z(v))) return false;
        double t = (u - (SANT_X - CX)) / (double) (-5 - (SANT_X - CX));
        double cv = Ruido.lerp(t, SANT_Z - CZ, 0);
        return Math.abs(v - cv) <= 2.4 + (Hash.pct(u, 0, v, SEMILLA + 6) % 3 - 1) * 0.4;
    }

    private final class Suelo extends Element {
        Suelo() {
            super(SEMILLA + 11);
            this.layer = 1;
            bounds(X0, 60, Z0, X1, 130, Z1);
        }

        @Override
        public void build(Canvas c) {
            for (int x = Math.max(c.minX, x0); x <= Math.min(c.maxX, x1); x++)
                for (int z = Math.max(c.minZ, z0); z <= Math.min(c.maxZ, z1); z++) {
                    int u = x - CX, v = z - CZ;
                    int o = objetivo(x, z);
                    boolean cam = camino(u, v);
                    if (o == Integer.MIN_VALUE && !cam) continue;
                    int t = alto(x, z);
                    if (o == Integer.MIN_VALUE) o = t;
                    if (t < Terreno.AGUA) continue;
                    for (int y = o + 1; y <= Math.max(t, o) + 4; y++) c.set(x, y, z, Pal.AIR);
                    for (int y = t + 1; y < o; y++) c.set(x, y, z, Hash.pct(x, y, z, seed) < 70 ? Pal.DIRT : Pal.STONE);
                    int p = Hash.pct(x, o, z, seed + 1);
                    BlockState s;
                    if (calle(u, v)) {
                        boolean plaza = Math.hypot(u - PLAZA_U, v) <= PLAZA_R + 0.4;
                        if (plaza && Math.abs(Math.hypot(u - PLAZA_U, v) - 9.0) < 0.55) s = b(Blocks.POLISHED_ANDESITE);
                        else s = p < 34 ? Pal.STONE_BRICKS : p < 50 ? b(Blocks.CRACKED_STONE_BRICKS) : p < 62 ? Pal.b(Blocks.COBBLESTONE)
                                : p < 72 ? Pal.ANDESITE : p < 82 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS) : p < 90 ? b(Blocks.TUFF) : Pal.GRAVEL;
                    } else if (cam) s = p < 40 ? Pal.GRAVEL : p < 64 ? Pal.b(Blocks.DIRT_PATH) : p < 82 ? Pal.COARSE_DIRT : Pal.b(Blocks.COBBLESTONE);
                    else s = p < 30 ? Pal.COARSE_DIRT : p < 48 ? Pal.GRAVEL : p < 62 ? b(Blocks.TUFF) : p < 74 ? Pal.DIRT : p < 86 ? b(Blocks.PACKED_MUD)
                            : b(Blocks.SOUL_SOIL);
                    c.set(x, o, z, s);
                    // ceniza por el suelo (en las calles, a trozos)
                    int q = Hash.pct(x, o + 1, z, seed + 2);
                    if (q < (calle(u, v) ? 9 : 18)) c.set(x, o + 1, z, ceniza(x, o + 1, z));
                }
        }
    }

    // ------------------------------------------------------------------ la muralla y sus torres

    /** Los trozos caidos de la muralla: {v0, v1}. */
    static final int[][] CAIDOS = {{-49, -41}, {33, 40}};
    /** Las torres de la muralla (v del centro). */
    static final int[] TORRES = {-62, -26, 26, 62};

    /** Si la muralla esta caida en v (y cuanto queda en pie: de 0 al borde a 1 en medio). */
    static double caida(int v) {
        for (int[] k : CAIDOS)
            if (v >= k[0] - 1 && v <= k[1] + 1) {
                double m = (k[0] + k[1]) / 2.0, r = (k[1] - k[0]) / 2.0 + 1;
                return 1 - Math.abs(v - m) / r;
            }
        return 0;
    }

    private void muralla(Pilotes.Lista m) {
        for (int v = V0; v <= V1; v++) {
            if (Math.abs(v) <= 9) continue;           // la puerta
            boolean torre = false;
            for (int t : TORRES) if (Math.abs(v - t) <= 3) torre = true;
            if (torre) continue;
            double k = caida(v);
            int cima = ADARVE;
            if (k > 0) cima = ADARVE - (int) Math.round(k * 7) - Hash.pct(v, 0, 0, SEMILLA + 12) % 2;
            for (int u = -2; u <= 1; u++) {
                for (int y = base(u, v); y <= cima; y++) put(m, u, y, v, piedra(x(u), y, z(v)));
                if (k > 0) continue;
                // las almenas por fuera (una si y otra no) y el pretil por dentro
                if (u == -2) {
                    put(m, u, ADARVE + 1, v, piedra(x(u), ADARVE + 1, z(v)));
                    if ((v & 1) == 0) put(m, u, ADARVE + 2, v, piedra(x(u), ADARVE + 2, z(v)));
                }
                if (u == 1) une(m, u, ADARVE + 1, v, b(Blocks.STONE_BRICK_WALL));
            }
            if (k > 0) {
                // los sillares caidos: una rampa de escombros a los dos lados
                for (int u = -6; u <= 5; u++) {
                    if (u >= -2 && u <= 1) continue;
                    int d = u < -2 ? -2 - u : u - 1;
                    int h = (int) Math.round((cima - SUELO) * k * 0.7) - d;
                    for (int y = SUELO + 1; y <= SUELO + h; y++) {
                        int p = Hash.pct(u, y, v, SEMILLA + 13);
                        put(m, u, y, v, p < 40 ? piedra(x(u), y, z(v)) : p < 70 ? Pal.b(Blocks.COBBLESTONE) : p < 85 ? Pal.GRAVEL : b(Blocks.BLACKSTONE));
                    }
                }
            }
        }
        // las escaleras de subida al adarve, pegadas por dentro (al norte y al sur de la puerta)
        for (int s = -1; s <= 1; s += 2)
            for (int i = 0; i <= ADARVE - SUELO - 1; i++) {
                int v = s * (16 + i);
                int y = SUELO + 1 + i;
                for (int u = 2; u <= 3; u++) {
                    for (int yy = SUELO + 1; yy < y; yy++) put(m, u, yy, v, piedra(x(u), yy, z(v)));
                    put(m, u, y, v, escalera(Blocks.STONE_BRICK_STAIRS, s < 0 ? Direction.NORTH : Direction.SOUTH, false));
                }
            }
        // las torres
        for (int t : TORRES) torre(m, t, t == -26);
    }

    /** Una torre de la muralla, hueca, siete de lado, con su puerta al adarve; la de {@code rota}, sin la mitad de arriba. */
    private void torre(Pilotes.Lista m, int vc, boolean rota) {
        torres++;
        int cima = PISO + 14;
        for (int u = -4; u <= 2; u++)
            for (int v = vc - 3; v <= vc + 3; v++) {
                boolean borde = u == -4 || u == 2 || v == vc - 3 || v == vc + 3;
                int top = cima;
                if (rota) top = ADARVE + 2 + Hash.pct(u, 0, v, SEMILLA + 14) % 4 - (u + 4) / 2;
                for (int y = base(u, v); y <= SUELO; y++) put(m, u, y, v, piedra(x(u), y, z(v)));
                if (borde) {
                    for (int y = SUELO + 1; y <= top; y++) put(m, u, y, v, piedra(x(u), y, z(v)));
                    // almenas
                    if (!rota && ((u + v) & 1) == 0) put(m, u, cima + 1, v, piedra(x(u), cima + 1, z(v)));
                } else {
                    for (int y = SUELO + 1; y <= top; y++) put(m, u, y, v, Pal.AIR);
                    // el suelo del adarve y el de arriba
                    put(m, u, ADARVE, v, Pal.DO_PLANKS);
                    if (!rota) put(m, u, cima, v, Hash.pct(u, cima, v, SEMILLA + 15) < 80 ? Pal.DO_PLANKS : Pal.AIR);
                }
            }
        // las puertas al adarve (norte y sur) y la de abajo (por dentro, al este)
        for (int s = -1; s <= 1; s += 2)
            for (int y = ADARVE + 1; y <= ADARVE + 2; y++)
                for (int u = -1; u <= 0; u++) put(m, u, y, vc + s * 3, Pal.AIR);
        for (int y = PISO; y <= PISO + 1; y++) put(m, 2, y, vc, Pal.AIR);
        // una escalera de mano del suelo al adarve, por dentro
        for (int y = PISO; y <= ADARVE + 1; y++)
            put(m, 1, y, vc + 2, Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.WEST));
        put(m, 1, ADARVE, vc + 2, Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.WEST));
        // un estandarte quemado colgando por fuera
        if (!rota) put(m, -5, ADARVE - 1, vc, b(Blocks.BLACK_WALL_BANNER).setValue(WallBannerBlock.FACING, Direction.WEST));
    }

    // ------------------------------------------------------------------ la puerta

    /**
     * La puerta: dos torres (u -5..5, v -9..-4 y 4..9) de dieciseis de alto, el paso abovedado entre ellas (v -3..3, siete
     * de alto), el rastrillo medio bajado (u -3), las hojas quemadas caidas por dentro y, encima del paso, la sala de la
     * guardia (el suelo en lo alto del adarve) con el atril; se sube por la torre del norte (la puerta por dentro, al este).
     */
    private void puerta(Pilotes.Lista m) {
        int alto = PISO + 16;
        for (int u = -5; u <= 5; u++)
            for (int v = -9; v <= 9; v++) {
                boolean paso = Math.abs(v) <= 3;
                for (int y = base(u, v); y <= SUELO; y++) put(m, u, y, v, piedra(x(u), y, z(v)));
                if (paso) {
                    // la boveda: siete de alto en medio, seis en los lados
                    int techo = PISO + 6 - (Math.abs(v) == 3 ? 1 : 0);
                    for (int y = PISO; y <= techo; y++) put(m, u, y, v, Pal.AIR);
                    for (int y = techo + 1; y < ADARVE; y++) put(m, u, y, v, piedra(x(u), y, z(v)));
                    if (Math.abs(v) == 3) put(m, u, techo + 1, v, escalera(Blocks.STONE_BRICK_STAIRS, v > 0 ? Direction.SOUTH : Direction.NORTH, true));
                    put(m, u, PISO - 1, v, Hash.pct(u, 0, v, SEMILLA + 16) < 60 ? Pal.STONE_BRICKS : b(Blocks.CRACKED_STONE_BRICKS));
                    // la sala de la guardia, encima
                    put(m, u, ADARVE, v, Math.abs(u) == 5 ? piedra(x(u), ADARVE, z(v)) : Pal.DO_PLANKS);
                    for (int y = ADARVE + 1; y < alto - 2; y++) put(m, u, y, v, Math.abs(u) == 5 ? piedra(x(u), y, z(v)) : Pal.AIR);
                    put(m, u, alto - 2, v, piedra(x(u), alto - 2, z(v)));
                    if (Math.abs(u) == 5 && ((v & 1) == 0)) put(m, u, alto - 1, v, piedra(x(u), alto - 1, z(v)));
                } else {
                    // las torres: huecas por dentro
                    boolean borde = Math.abs(u) == 5 || Math.abs(v) == 9 || Math.abs(v) == 4;
                    for (int y = PISO; y <= alto; y++) put(m, u, y, v, borde ? piedra(x(u), y, z(v)) : Pal.AIR);
                    if (borde && ((u + v) & 1) == 0) put(m, u, alto + 1, v, piedra(x(u), alto + 1, z(v)));
                    if (!borde) {
                        put(m, u, PISO - 1, v, Pal.DO_PLANKS);
                        put(m, u, alto, v, Pal.DO_PLANKS);
                    }
                }
            }
        // las troneras de las torres (por fuera, a dos alturas) y las ventanas de la sala
        for (int s = -1; s <= 1; s += 2) {
            for (int y : new int[]{PISO + 4, PISO + 11}) put(m, -5, y, s * 6, Pal.AIR);
            put(m, -5, ADARVE + 2, s * 1, Pal.AIR);
            put(m, 5, ADARVE + 2, s * 1, Pal.AIR);
        }
        // el paso de la torre del norte a la sala y de la sala a la torre del sur (a lo alto del adarve, junto a lo alto de
        // la escalera)
        for (int s = -1; s <= 1; s += 2)
            for (int y = ADARVE + 1; y <= ADARVE + 2; y++) put(m, -4, y, s * 4, Pal.AIR);
        // la torre del norte: la puerta de abajo (por dentro, al este) y la escalera recta que sube al oeste
        for (int y = PISO; y <= PISO + 2; y++) put(m, 5, y, -6, Pal.AIR);
        for (int i = 0; i <= 8; i++) {
            int u = 4 - i, y = PISO + i;
            for (int v = -6; v <= -5; v++) {
                for (int yy = PISO; yy < y; yy++) put(m, u, yy, v, piedra(x(u), yy, z(v)));
                put(m, u, y, v, escalera(Blocks.STONE_BRICK_STAIRS, Direction.WEST, false));
                for (int yy = y + 1; yy <= y + 3; yy++) put(m, u, yy, v, Pal.AIR);
            }
        }
        // en la torre del norte, un piso a lo alto del adarve al lado de la escalera (de la puerta del adarve a lo alto de la
        // escalera); en la del sur, el piso entero (del adarve del sur a la sala)
        for (int u = -4; u <= 4; u++) {
            for (int v = -8; v <= -7; v++) put(m, u, ADARVE, v, Pal.DO_PLANKS);
            for (int v = 5; v <= 8; v++) put(m, u, ADARVE, v, Pal.DO_PLANKS);
        }
        // las puertas de las torres al adarve de la muralla (norte y sur), a lo alto del adarve
        for (int s = -1; s <= 1; s += 2)
            for (int y = ADARVE + 1; y <= ADARVE + 2; y++) for (int u = -1; u <= 0; u++) put(m, u, y, s * 9, Pal.AIR);
        // el rastrillo medio bajado (barrotes de hierro de PISO + 3 al techo de la boveda; alguno doblado o caido)
        for (int v = -3; v <= 3; v++) {
            int abajo = PISO + 3 + (v == 1 ? 2 : 0) + (v == -2 ? -1 : 0);
            int techo = PISO + 6 - (Math.abs(v) == 3 ? 1 : 0);
            for (int y = abajo; y <= techo; y++) une(m, -3, y, v, b(Blocks.IRON_BARS));
        }
        une(m, -3, PISO, 2, b(Blocks.IRON_BARS));
        une(m, -2, PISO, -1, b(Blocks.IRON_BARS));
        // las cadenas del rastrillo en la sala, y el torno
        for (int v = -2; v <= 2; v += 4) for (int y = ADARVE + 1; y <= ADARVE + 3; y++) put(m, -3, y, v, b(Blocks.CHAIN));
        put(m, -2, ADARVE + 1, 0, b(Blocks.GRINDSTONE));
        // las hojas de la puerta, quemadas, caidas por dentro
        for (int u = 6; u <= 9; u++)
            for (int v = -3; v <= 3; v++)
                if (Hash.pct(u, 1, v, SEMILLA + 17) < (u < 8 ? 85 : 45)) put(m, u, PISO, v, Hash.pct(u, 2, v, SEMILLA + 17) < 60
                        ? losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM) : b(Blocks.BLACKSTONE_SLAB));
        // la sala de la guardia: la mesa, una silla caida, el brasero frio y el atril (lo pone su trozo)
        put(m, 2, ADARVE + 1, -2, Blocks.DARK_OAK_FENCE.defaultBlockState());
        put(m, 2, ADARVE + 2, -2, losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
        put(m, 3, ADARVE + 2, -2, losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
        une(m, 3, ADARVE + 1, -2, Blocks.DARK_OAK_FENCE.defaultBlockState());
        put(m, 3, ADARVE + 1, 1, escalera(Blocks.DARK_OAK_STAIRS, Direction.EAST, true));
        put(m, -2, ADARVE + 1, 2, b(Blocks.CAULDRON));
        put(m, 4, ADARVE + 1, 3, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
        put(m, 0, ADARVE + 4, 0, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        put(m, 0, ADARVE + 5, 0, b(Blocks.CHAIN));
        for (int v = -3; v <= 3; v++) if (Hash.pct(v, 3, 0, SEMILLA + 18) < 35) put(m, 4, ADARVE + 1, v, ceniza(x(4), ADARVE + 1, z(v)));
        // estandartes negros a los lados del arco, por fuera
        for (int s = -1; s <= 1; s += 2) put(m, -6, PISO + 7, s * 5, b(Blocks.BLACK_WALL_BANNER).setValue(WallBannerBlock.FACING, Direction.WEST));
        // faroles en el paso
        put(m, 0, PISO + 5, -3, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        put(m, 0, PISO + 5, 3, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }

    // ------------------------------------------------------------------ fuera: el camino y los carros quemados

    private void fuera(Pilotes.Lista m, Rng r) {
        // los carros quemados y las barricadas delante de la puerta (los que quisieron salir)
        int[][] carros = {{-12, -5, 0}, {-17, 4, 1}, {-25, -3, 1}, {-34, 5, 0}};
        for (int[] k : carros) carro(m, k[0], k[1], k[2] == 1);
        // los faroles del camino hasta el santuario (cada diez, a un lado y a otro)
        for (int u = -12; u >= SANT_X - CX + 10; u -= 10) {
            double t = (u - (SANT_X - CX)) / (double) (-5 - (SANT_X - CX));
            int cv = (int) Math.round(Ruido.lerp(t, SANT_Z - CZ, 0)) + ((u / 10) % 2 == 0 ? 4 : -4);
            int y = pie(x(u), z(cv));
            une(m, u, y, cv, Pal.DO_FENCE);
            une(m, u, y + 1, cv, Pal.DO_FENCE);
            put(m, u, y + 2, cv, Blocks.SOUL_LANTERN.defaultBlockState());
        }
    }

    /** Un carro quemado: la caja negra, una rueda caida, la carga desparramada. */
    private void carro(Pilotes.Lista m, int u0, int v0, boolean volcado) {
        carros++;
        int y = pie(x(u0), z(v0));
        for (int u = u0; u <= u0 + 3; u++)
            for (int v = v0; v <= v0 + 1; v++) {
                if (volcado && u == u0 + 3) continue;
                put(m, u, y, v, Hash.pct(u, 0, v, SEMILLA + 19) < 60 ? losa(Blocks.DARK_OAK_SLAB, SlabType.TOP) : losa(Blocks.BLACKSTONE_SLAB, SlabType.TOP));
                if (!volcado && (u == u0 || u == u0 + 3)) une(m, u, y + 1, v, Pal.DO_FENCE);
            }
        put(m, u0 + 1, y, v0 - 1, b(Blocks.DARK_OAK_TRAPDOOR).setValue(net.minecraft.world.level.block.TrapDoorBlock.FACING, Direction.NORTH)
                .setValue(net.minecraft.world.level.block.TrapDoorBlock.OPEN, true));
        put(m, u0 + 4, y, v0 + 1, b(Blocks.BARREL));
        put(m, u0 - 1, y, v0 + 2, ceniza(x(u0 - 1), y, z(v0 + 2)));
        if (volcado) put(m, u0 + 2, y + 1, v0, rescoldo());
    }

    // ------------------------------------------------------------------ las casas quemadas

    /** Las casas de la capital: las de Brumavela de piedra y entramado, ennegrecidas, con el tejado hundido casi siempre. */
    static final class CasaCapital extends Casa {
        CasaCapital(long seed, Rect fp, Direction frente, int plantas, int ruina, Estilo estilo, int tejado, Tipo tipo) {
            super(seed, Capital::alto0, fp, frente, plantas, ruina, estilo, tejado, tipo);
        }

        @Override
        protected BlockState relleno(int x, int yy, int z) {
            int p = Hash.pct(x, yy, z, seed + 2);
            if (estilo == Estilo.PIEDRA) return piedra(x, yy, z);
            if (p < 30) return Pal.b(Blocks.GRAY_TERRACOTTA);
            if (p < 50) return Pal.b(Blocks.BLACK_TERRACOTTA);
            if (p < 66) return Pal.b(Blocks.BLACKSTONE);
            if (p < 80) return Pal.b(Blocks.CRACKED_DEEPSLATE_BRICKS);
            if (p < 90) return Pal.b(Blocks.TUFF);
            return Pal.DO_PLANKS;
        }

        @Override
        protected BlockState zocalo(int x, int yy, int z) {
            return piedra(x, yy, z);
        }

        @Override
        protected BlockState poste() {
            return Pal.DO_LOG;
        }

        @Override
        protected BlockState viga(Direction.Axis eje) {
            return Pal.log(Blocks.DARK_OAK_LOG, eje);
        }

        @Override
        protected BlockState suelo(int x, int z) {
            int p = Hash.pct(x, y, z, seed + 4);
            if (p < 20) return Pal.b(Blocks.TUFF);
            if (p < 34) return Pal.COARSE_DIRT;
            if (p < 44) return Pal.b(Blocks.BLACKSTONE);
            return Pal.DO_PLANKS;
        }

        @Override
        protected Block escalonTejado() {
            return Blocks.DEEPSLATE_TILE_STAIRS;
        }

        @Override
        protected BlockState cumbrera() {
            return Pal.slab(Blocks.DEEPSLATE_TILE_SLAB, false);
        }
    }

    /** El terreno para las casas: el suelo de la ciudad. */
    static int alto0(int x, int z) {
        int o = objetivo(x, z);
        return o == Integer.MIN_VALUE ? alto(x, z) : o;
    }

    private void casa(Rng r, int u0, int v0, int u1, int v1, Direction frente, int arcones) {
        Rect fp = new Rect(x(u0), z(v0), x(u1), z(v1));
        if (!libre(fp, 1)) return;
        int ruina = r.chance(0.2) ? 0 : r.chance(0.6) ? 1 : 2;
        Casa.Estilo e = r.chance(0.45) ? Casa.Estilo.PIEDRA : Casa.Estilo.ENTRAMADO;
        Casa k = new CasaCapital(r.fork(), fp, frente, ruina == 2 ? 1 : 2, ruina, e, 2, Casa.Tipo.HOGAR);
        k.botin(arcones >= 1 ? BOTIN : null, arcones >= 2 ? BOTIN_BUENO : null);
        elementos.add(k);
        casas.add(k);
        ocupar(fp);
    }

    /** Las casas: a los dos lados de la calle mayor y de la del cruce, de cara a la calle; alguna en la ronda. */
    private void casas(Rng r) {
        // la calle mayor (de la ronda a la plaza): al norte (v -16..-5) y al sur (5..16), dejando el cruce
        for (int lado = -1; lado <= 1; lado += 2) {
            int u = 8;
            while (u < PLAZA_U - PLAZA_R - 4) {
                int w = 7 + r.nextInt(4);
                if (u <= CRUCE_U + 3 && u + w >= CRUCE_U - 3) {
                    u = CRUCE_U + 4;
                    continue;
                }
                if (u + w > PLAZA_U - PLAZA_R - 2) break;
                int fondo = 9 + r.nextInt(3);
                int va = lado < 0 ? -5 - fondo : 5, vb = lado < 0 ? -5 : 5 + fondo;
                casa(r, u, va, u + w - 1, vb, lado < 0 ? Direction.SOUTH : Direction.NORTH, r.chance(0.35) ? 1 : 0);
                u += w + 1 + r.nextInt(2);
            }
        }
        // la calle del cruce: al oeste (u 33..41) y al este (47..55), de v 19 a 66 y de -66 a -19
        for (int lado = -1; lado <= 1; lado += 2)
            for (int sv = -1; sv <= 1; sv += 2) {
                int v = 19;
                while (v < 64) {
                    int w = 7 + r.nextInt(3);
                    if (v + w > 66) break;
                    int fondo = 8 + r.nextInt(2);
                    int ua = lado < 0 ? CRUCE_U - 3 - fondo : CRUCE_U + 3, ub = lado < 0 ? CRUCE_U - 3 : CRUCE_U + 3 + fondo;
                    int va = sv < 0 ? -(v + w - 1) : v, vb = sv < 0 ? -v : v + w - 1;
                    casa(r, ua, va, ub, vb, lado < 0 ? Direction.EAST : Direction.WEST, r.chance(0.3) ? 1 : 0);
                    v += w + 1 + r.nextInt(2);
                }
            }
        // detras de la calle mayor, hacia la plaza (u 58..78) a los dos lados, la segunda fila
        for (int lado = -1; lado <= 1; lado += 2) {
            int u = 56;
            while (u < 80) {
                int w = 7 + r.nextInt(3);
                int va = lado < 0 ? -30 : 19, vb = lado < 0 ? -20 : 29;
                casa(r, u, va, u + w - 1, vb, lado < 0 ? Direction.SOUTH : Direction.NORTH, r.chance(0.4) ? 1 : 2);
                u += w + 2;
            }
        }
    }

    // ------------------------------------------------------------------ los solares quemados

    /**
     * Donde no queda casa: los solares de las que ardieron del todo (el arranque de los muros, a trozos de uno a tres de
     * alto, una viga caida, escombros y ceniza dentro) y algun arbol quemado. Por toda la ciudad, donde haya hueco.
     */
    private void solares(Pilotes.Lista m, Rng r) {
        for (int u0 = 9; u0 <= U1 - 8; u0 += 12)
            for (int v0 = V0 + 4; v0 <= V1 - 10; v0 += 12) {
                int u = u0 + r.nextInt(3), v = v0 + r.nextInt(3), w = 6 + r.nextInt(4), d = 6 + r.nextInt(4);
                Rect fp = new Rect(x(u), z(v), x(u + w - 1), z(v + d - 1));
                if (!libre(fp, 2) || pisaCalle(u - 1, v - 1, u + w, v + d)) {
                    // si no cabe un solar, quizas un arbol quemado
                    int au = u + w / 2, av = v + d / 2;
                    if (r.chance(0.3) && libre(new Rect(x(au - 1), z(av - 1), x(au + 1), z(av + 1)), 1) && !pisaCalle(au - 2, av - 2, au + 2, av + 2))
                        arbolQuemado(m, au, av, r);
                    continue;
                }
                solar(m, u, v, u + w - 1, v + d - 1, r);
                ocupar(fp);
            }
    }

    /** Si el rectangulo (u0..u1, v0..v1) toca alguna calle. */
    private static boolean pisaCalle(int u0, int v0, int u1, int v1) {
        for (int u = u0; u <= u1; u++)
            for (int v = v0; v <= v1; v++) if (calle(u, v)) return true;
        return false;
    }

    private void solar(Pilotes.Lista m, int ua, int va, int ub, int vb, Rng r) {
        solaresHechos++;
        long sem = r.fork();
        for (int u = ua; u <= ub; u++)
            for (int v = va; v <= vb; v++) {
                boolean borde = u == ua || u == ub || v == va || v == vb;
                int p = Hash.pct(u, 7, v, sem);
                if (borde) {
                    int h = p < 18 ? 0 : 1 + p % 3;
                    for (int y = PISO; y < PISO + h; y++) put(m, u, y, v, piedra(x(u), y, z(v)));
                    if (h > 0 && p % 5 == 0) put(m, u, PISO + h, v, losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
                } else {
                    put(m, u, SUELO, v, p < 40 ? Pal.DO_PLANKS : p < 60 ? b(Blocks.TUFF) : p < 80 ? Pal.COARSE_DIRT : b(Blocks.BLACKSTONE));
                    if (p > 84) put(m, u, PISO, v, ceniza(x(u), PISO, z(v)));
                    else if (p < 8) put(m, u, PISO, v, p < 4 ? Pal.b(Blocks.COBBLESTONE) : Pal.GRAVEL);
                }
            }
        // una viga caida en diagonal (tumbada en el suelo) y, a veces, un rescoldo
        int largo = Math.min(ub - ua, vb - va) - 1;
        boolean ex = r.chance(0.5);
        for (int i = 0; i < largo; i++) {
            int u = ex ? ua + 1 + i : ua + 1 + largo / 2, v = ex ? va + 1 + largo / 2 : va + 1 + i;
            put(m, u, PISO, v, Pal.log(Blocks.DARK_OAK_LOG, ex ? Direction.Axis.X : Direction.Axis.Z));
        }
        if (r.chance(0.35)) {
            put(m, ua + 2, PISO, vb - 2, rescoldo());
            rescoldos++;
        }
    }

    /** Un arbol quemado: el tronco negro con dos ramas peladas. */
    private void arbolQuemado(Pilotes.Lista m, int u, int v, Rng r) {
        arboles++;
        int h = 4 + r.nextInt(3);
        for (int y = PISO; y < PISO + h; y++) put(m, u, y, v, y == PISO + h - 1 ? b(Blocks.BLACKSTONE_WALL) : Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
        int y1 = PISO + h - 2;
        Direction d1 = Direction.from2DDataValue(r.nextInt(4)), d2 = d1.getClockWise();
        put(m, u + d1.getStepX(), y1, v + d1.getStepZ(), Pal.log(Blocks.DARK_OAK_LOG, d1.getAxis()));
        une(m, u + d1.getStepX() * 2, y1 + 1, v + d1.getStepZ() * 2, Pal.DO_FENCE);
        une(m, u + d2.getStepX(), y1 - 1, v + d2.getStepZ(), Pal.DO_FENCE);
        put(m, u, PISO, v + 1, ceniza(x(u), PISO, z(v + 1)));
    }

    // ------------------------------------------------------------------ la plaza del Juramento

    /**
     * La plaza: el anillo de andesita, el pedestal con lo que queda en pie de la estatua (las piernas del caballero) y el
     * resto caido al este (el torso, el yelmo y la mano alzada, rotos), puestos del mercado quemados alrededor.
     */
    private void plaza(Pilotes.Lista m) {
        int pu = PLAZA_U;
        // el pedestal (5 x 5, tres de alto, con su cornisa)
        for (int u = -2; u <= 2; u++)
            for (int v = -2; v <= 2; v++) {
                for (int y = PISO; y <= PISO + 1; y++) put(m, pu + u, y, v, Math.abs(u) == 2 || Math.abs(v) == 2 ? b(Blocks.POLISHED_DEEPSLATE) : b(Blocks.DEEPSLATE_TILES));
                put(m, pu + u, PISO + 2, v, Math.abs(u) == 2 || Math.abs(v) == 2 ? losa(Blocks.POLISHED_DEEPSLATE_SLAB, SlabType.BOTTOM)
                        : b(Blocks.POLISHED_DEEPSLATE));
            }
        // las piernas en pie (y la punta de la espada clavada entre ellas)
        for (int s = -1; s <= 1; s += 2)
            for (int y = PISO + 3; y <= PISO + 5; y++) put(m, pu, y, s, y == PISO + 5 ? b(Blocks.POLISHED_BLACKSTONE) : b(Blocks.BLACKSTONE));
        put(m, pu, PISO + 3, 0, b(Blocks.POLISHED_BLACKSTONE_WALL));
        // lo caido: el torso tumbado, el yelmo, el brazo con la mano alzada (ahora en el suelo)
        for (int u = 4; u <= 7; u++)
            for (int v = -1; v <= 1; v++) put(m, pu + u, PISO, v, u == 7 && v != 0 ? Pal.AIR : b(Blocks.POLISHED_BLACKSTONE));
        put(m, pu + 5, PISO + 1, 0, b(Blocks.POLISHED_BLACKSTONE_BRICKS));
        put(m, pu + 8, PISO, 1, b(Blocks.CHISELED_POLISHED_BLACKSTONE));
        for (int v = -2; v >= -5; v--) put(m, pu + 5, PISO, v, b(Blocks.POLISHED_BLACKSTONE_WALL));
        put(m, pu + 5, PISO, -6, b(Blocks.GILDED_BLACKSTONE));
        put(m, pu + 6, PISO, -6, b(Blocks.GILDED_BLACKSTONE));
        // los puestos quemados del mercado (postes y toldos de losa), en el borde de la plaza
        for (int k = 0; k < 6; k++) {
            double a = Math.toRadians(30 + k * 60);
            int u = pu + (int) Math.round(Math.cos(a) * (PLAZA_R - 3)), v = (int) Math.round(Math.sin(a) * (PLAZA_R - 3));
            // (ni en la entrada de la calle mayor ni en la del cuartel)
            if (Math.abs(v) <= 4 && u < pu || Math.abs(u - pu) <= 3 && v < 0) continue;
            for (int du = -1; du <= 1; du++)
                for (int dv = -1; dv <= 1; dv++) {
                    if (Math.abs(du) == 1 && Math.abs(dv) == 1) {
                        une(m, u + du, PISO, v + dv, Pal.DO_FENCE);
                        if (Hash.pct(u + du, k, v + dv, SEMILLA + 20) < 60) une(m, u + du, PISO + 1, v + dv, Pal.DO_FENCE);
                    }
                    if (Hash.pct(u + du, 2, v + dv, SEMILLA + 21) < 55) put(m, u + du, PISO + 2, v + dv, losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
                }
            put(m, u, PISO, v, k % 2 == 0 ? b(Blocks.BARREL) : b(Blocks.COMPOSTER));
        }
        // los rescoldos que aun arden alrededor de la estatua
        put(m, pu - 4, PISO, 4, rescoldo());
        put(m, pu + 3, PISO, -5, rescoldo());
        rescoldos += 2;
    }

    // ------------------------------------------------------------------ el cuartel del Juramento

    /**
     * El cuartel, al norte de la plaza (u 84..108, v -46..-24): muros de sillar de ocho de alto, el tejado hundido en medio,
     * dentro las literas quemadas, el armero, la mesa larga y el arcon; delante, el patio de armas con los muñecos de
     * practicar.
     */
    private void cuartel(Pilotes.Lista m) {
        int ua = 84, ub = 108, va = -46, vb = -24, alto = PISO + 8;
        for (int u = ua; u <= ub; u++)
            for (int v = va; v <= vb; v++) {
                boolean borde = u == ua || u == ub || v == va || v == vb;
                for (int y = base(u, v); y <= SUELO; y++) put(m, u, y, v, piedra(x(u), y, z(v)));
                put(m, u, SUELO, v, borde ? piedra(x(u), SUELO, z(v)) : (Hash.pct(u, 0, v, SEMILLA + 22) < 75 ? Pal.DO_PLANKS : b(Blocks.TUFF)));
                // los muros (con un trozo caido en la esquina del noreste)
                int top = alto;
                if (u > ub - 6 && v < va + 6) top = PISO + Hash.pct(u, 1, v, SEMILLA + 23) % 4;
                for (int y = PISO; y <= alto + 4; y++) put(m, u, y, v, borde && y <= top ? piedra(x(u), y, z(v)) : Pal.AIR);
                // el tejado: losas de pizarra a dos aguas, hundido en medio
                int bo = Math.min(u - ua, ub - u);
                boolean hundido = Math.hypot(u - 96, v - (-35)) < 7 || (u > ub - 6 && v < va + 6);
                if (!hundido) {
                    int h = alto + 1 + Math.min(bo, 5);
                    if (bo >= 5) put(m, u, h, v, losa(Blocks.DEEPSLATE_TILE_SLAB, SlabType.BOTTOM));
                    else une(m, u, h, v, escalera(Blocks.DEEPSLATE_TILE_STAIRS, u - ua < ub - u ? Direction.EAST : Direction.WEST, false));
                } else if (Hash.pct(u, 2, v, SEMILLA + 24) < 30 && !borde) {
                    // lo que cayo del tejado
                    put(m, u, PISO, v, Hash.pct(u, 3, v, SEMILLA + 24) < 50 ? losa(Blocks.DEEPSLATE_TILE_SLAB, SlabType.BOTTOM) : b(Blocks.COBBLED_DEEPSLATE));
                }
            }
        // la puerta (al sur, hacia la plaza) y las ventanas
        for (int u = 95; u <= 97; u++) for (int y = PISO; y <= PISO + 3; y++) put(m, u, y, vb, Pal.AIR);
        for (int u = 87; u <= 105; u += 6)
            for (int v : new int[]{va, vb}) {
                if (v == vb && u >= 94 && u <= 98) continue;
                for (int y = PISO + 3; y <= PISO + 4; y++) une(m, u, y, v, b(Blocks.IRON_BARS));
            }
        // las literas quemadas a los lados, la mesa larga en medio y el armero al fondo
        for (int v = va + 3; v <= vb - 4; v += 4)
            for (int u : new int[]{ua + 2, ub - 2}) {
                put(m, u, PISO, v, losa(Blocks.DARK_OAK_SLAB, SlabType.TOP));
                put(m, u, PISO, v + 1, losa(Blocks.DARK_OAK_SLAB, SlabType.TOP));
                une(m, u, PISO + 1, v, Pal.DO_FENCE);
                put(m, u, PISO + 2, v, losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
                put(m, u, PISO + 2, v + 1, losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
                if (Hash.pct(u, 4, v, SEMILLA + 25) < 50) put(m, u, PISO + 1, v + 1, ceniza(x(u), PISO + 1, z(v + 1)));
            }
        for (int v = va + 6; v <= vb - 6; v++) {
            une(m, 96, PISO, v, Pal.DO_FENCE);
            put(m, 96, PISO + 1, v, losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
            if (v % 3 == 0) {
                put(m, 95, PISO, v, escalera(Blocks.DARK_OAK_STAIRS, Direction.EAST, false));
                put(m, 97, PISO, v, escalera(Blocks.DARK_OAK_STAIRS, Direction.WEST, false));
            }
        }
        for (int u = 90; u <= 102; u += 3) {
            put(m, u, PISO, va + 1, b(Blocks.GRINDSTONE));
            put(m, u + 1, PISO, va + 1, b(Blocks.SMITHING_TABLE));
        }
        put(m, 96, PISO + 6, -35, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        put(m, 90, PISO, -30, rescoldo());
        rescoldos++;
        // el patio de armas: los munecos de practicar (un poste con su cabeza de calabaza tallada y el escudo)
        for (int u = 86; u <= 106; u += 5) {
            if (u >= 94 && u <= 98) continue;
            une(m, u, PISO, -19, Pal.DO_FENCE);
            une(m, u, PISO + 1, -19, Pal.DO_FENCE);
            put(m, u, PISO + 2, -19, b(Blocks.HAY_BLOCK));
            put(m, u, PISO + 3, -19, b(Blocks.CARVED_PUMPKIN).setValue(net.minecraft.world.level.block.CarvedPumpkinBlock.FACING, Direction.SOUTH));
        }
        ocupar(new Rect(x(ua - 1), z(va - 1), x(ub + 1), z(-16)));
    }

    // ------------------------------------------------------------------ escombros, ceniza y rescoldos

    /** Por las calles: montones de escombros, montones de ceniza (alli esperan los cenicientos) y rescoldos. */
    private void escombrosYCeniza(Pilotes.Lista m, Rng r) {
        // los montones de ceniza: en la calle mayor, en el cruce, en la plaza y fuera, junto a los carros
        int[][] montonesUV = {{20, 1}, {58, -2}, {70, 2}, {CRUCE_U, -30}, {CRUCE_U, 34}, {92, 5}, {100, -10}, {-16, -6}, {-22, 7}, {CRUCE_U + 1, 56}};
        for (int[] k : montonesUV) {
            monton(m, k[0], k[1]);
            cenizas.add(k);
        }
        // escombros en las calles (sin tapar el paso del todo)
        for (int i = 0; i < 26; i++) {
            int u = 6 + r.nextInt(PLAZA_U - 6), v = r.nextInt(2) == 0 ? -2 - r.nextInt(2) : 2 + r.nextInt(2);
            if (i % 3 == 1) {
                u = CRUCE_U + (r.nextInt(2) == 0 ? -2 : 2);
                v = (r.nextInt(2) == 0 ? -1 : 1) * (8 + r.nextInt(56));
            }
            escombro(m, u, v, r);
        }
        // rescoldos por el barrio (en las calles, junto a las casas)
        for (int i = 0; i < 10; i++) {
            int u = 10 + r.nextInt(80), v = (r.nextInt(2) == 0 ? -1 : 1) * 4;
            if (Math.abs(u - CRUCE_U) <= 3) continue;
            put(m, u, PISO, v, rescoldo());
            rescoldos++;
        }
    }

    /** Un monton de ceniza: polvo gris en medio, alfombras alrededor. */
    private void monton(Pilotes.Lista m, int u, int v) {
        montones++;
        int y = pie(x(u), z(v));
        for (int du = -2; du <= 2; du++)
            for (int dv = -2; dv <= 2; dv++) {
                double d = Math.hypot(du, dv);
                if (d > 2.4) continue;
                if (d < 1.2 && !(du == 0 && dv == 0)) put(m, u + du, y, v + dv, ceniza(x(u + du), y, z(v + dv)));
                else if (d >= 1.2) put(m, u + du, y, v + dv, ceniza(x(u + du), y, z(v + dv)));
            }
        // en medio (donde esta enterrado) solo alfombra: que quepa
        put(m, u, y, v, b(Blocks.LIGHT_GRAY_CARPET));
        put(m, u + 2, y, v + 1, b(Blocks.GRAY_CONCRETE_POWDER));
        put(m, u - 2, y, v - 1, b(Blocks.LIGHT_GRAY_CONCRETE_POWDER));
    }

    /** Un monton de escombros (piedra, grava y algun sillar negro), uno o dos de alto. */
    private void escombro(Pilotes.Lista m, int u, int v, Rng r) {
        escombros++;
        int y = pie(x(u), z(v));
        for (int du = -1; du <= 1; du++)
            for (int dv = -1; dv <= 1; dv++) {
                if (r.chance(0.35)) continue;
                int p = Hash.pct(u + du, y, v + dv, SEMILLA + 26);
                BlockState s = p < 30 ? Pal.b(Blocks.COBBLESTONE) : p < 50 ? Pal.GRAVEL : p < 65 ? b(Blocks.BLACKSTONE)
                        : p < 80 ? escalera(Blocks.STONE_BRICK_STAIRS, Direction.Plane.HORIZONTAL.getRandomDirection(RandomSource.create(p)), false)
                        : losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM);
                put(m, u + du, y, v + dv, s);
                if (du == 0 && dv == 0 && r.chance(0.4)) put(m, u, y + 1, v, losa(Blocks.COBBLESTONE_SLAB, SlabType.BOTTOM));
            }
    }

    // ------------------------------------------------------------------ lo que lleva datos: el atril, los arcones

    private void trozos() {
        int ya = ADARVE + 1;
        trozo(0, ya, 2, c -> c.lectern(x(0), ya, z(2), Direction.NORTH, Perdidos.libro(22)));
        trozo(-3, ya, -2, c -> c.chest(x(-3), ya, z(-2), Direction.EAST, BOTIN));
        trozo(104, PISO, -42, c -> c.chest(x(104), PISO, z(-42), Direction.NORTH, BOTIN_BUENO));
        trozo(88, PISO, -42, c -> c.barrel(x(88), PISO, z(-42), Direction.UP, BOTIN));
        trozo(-13, pie(x(-13), z(-6)), -6, c -> c.barrel(x(-13), pie(x(-13), z(-6)), z(-6), Direction.UP, BOTIN));
    }

    private void trozo(int u, int y, int v, Trozo.Obra o) {
        elementos.add(new Trozo(6, x(u), y, z(v), x(u), y, z(v), o));
    }

    // ------------------------------------------------------------------ los calcinados

    private static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;

    private void en(int variante, int u, int y, int v, float giro, int postura, int ronda, String grupo) {
        esperas.add(new Espera(variante, x(u), y, z(v), giro, postura, ronda, grupo));
    }

    private void ponerEsperas() {
        int G = Calcinado.GUARDIA, I = Calcinado.INCENDIARIO, C = Calcinado.CENICIENTO;
        int R = Enemigo.REZA, T = Enemigo.TUMBADO, D = Enemigo.DE_PIE;
        // fuera: los cenicientos enterrados junto a los carros y la guardia en el paso de la puerta
        en(C, -16, pie(x(-16), z(-6)), -6, ESTE, T, 0, "fuera");
        en(C, -22, pie(x(-22), z(7)), 7, ESTE, T, 0, "fuera");
        en(G, 1, PISO, 1, OESTE, D, 0, "puerta");
        // en el adarve: dos incendiarios de cara al camino y una guardia de ronda
        en(I, -1, ADARVE + 1, -14, OESTE, D, 0, "muralla");
        en(I, -1, ADARVE + 1, 15, OESTE, D, 0, "muralla");
        en(G, 0, ADARVE + 1, 50, NORTE, D, 6, "muralla");
        // la sala de la guardia: uno agazapado junto al atril
        en(G, 3, ADARVE + 1, 2, OESTE, R, 0, "camara");
        // la calle mayor: cenicientos en los montones, guardias de ronda, un incendiario
        en(C, 20, PISO, 1, OESTE, T, 0, "calle");
        en(C, 58, PISO, -2, OESTE, T, 0, "calle");
        en(C, 70, PISO, 2, OESTE, T, 0, "calle");
        en(G, 30, PISO, 0, OESTE, D, 5, "calle");
        en(G, 64, PISO, 0, OESTE, D, 5, "calle");
        en(I, 38, PISO, -3, OESTE, R, 0, "calle");
        // la calle del cruce
        en(C, CRUCE_U, PISO, -30, SUR, T, 0, "cruce");
        en(C, CRUCE_U, PISO, 34, NORTE, T, 0, "cruce");
        en(C, CRUCE_U + 1, PISO, 56, NORTE, T, 0, "cruce");
        en(I, CRUCE_U, PISO, -52, SUR, D, 0, "cruce");
        en(G, CRUCE_U, PISO, 48, NORTE, D, 4, "cruce");
        // la plaza del Juramento
        en(G, PLAZA_U - 6, PISO, -6, OESTE, R, 0, "plaza");
        en(G, PLAZA_U + 6, PISO, 6, OESTE, D, 0, "plaza");
        en(G, PLAZA_U, PISO, 10, OESTE, D, 3, "plaza");
        en(I, PLAZA_U + 10, PISO, -8, OESTE, D, 0, "plaza");
        en(I, PLAZA_U - 10, PISO, 9, OESTE, D, 0, "plaza");
        en(C, 92, PISO, 5, OESTE, T, 0, "plaza");
        en(C, 100, PISO, -10, OESTE, T, 0, "plaza");
        // el cuartel: dos agazapados junto a las literas, uno de pie a la puerta y un incendiario al fondo
        en(G, 88, PISO, -36, ESTE, R, 0, "cuartel");
        en(G, 104, PISO, -32, OESTE, R, 0, "cuartel");
        en(G, 96, PISO, -27, SUR, D, 0, "cuartel");
        en(I, 100, PISO, -42, SUR, D, 0, "cuartel");
        // dentro de alguna casa de la calle mayor, agazapados (los dos primeros con dos plantas)
        int n = 0;
        for (Casa k : casas) {
            if (n >= 2 || k.ruina == 2) continue;
            // junto a la puerta, por dentro (lo de dentro lleva muebles)
            int a = k.puertaA(), bb = 1;
            en(n == 0 ? G : I, k.lx(a, bb) - CX, k.y, k.lz(a, bb) - CZ, giroDe(k.frente), R, 0, "casas");
            n++;
        }
    }

    /** Hacia donde mira quien esta de cara a la puerta de una casa (hacia fuera). */
    private static float giroDe(Direction frente) {
        return switch (frente) {
            case NORTH -> NORTE;
            case EAST -> ESTE;
            case WEST -> OESTE;
            default -> SUR;
        };
    }

    // ------------------------------------------------------------------ para la prueba

    /** Lo que hay en la ciudad ya generada (para la prueba): el atril con el libro, los arcones, el rastrillo, la estatua. */
    public static String resumen(ServerLevel m) {
        Capital a = get();
        int[] at = a.sitio("atril"), ar = a.sitio("arcon"), ac = a.sitio("arcon_camara");
        BlockPos pa = new BlockPos(at[0], at[1], at[2]);
        boolean libro = m.getBlockEntity(pa) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le && Perdidos.de(le.getBook()) == 22;
        boolean arcon = m.getBlockState(new BlockPos(ar[0], ar[1], ar[2])).is(Blocks.CHEST);
        boolean arconCamara = m.getBlockState(new BlockPos(ac[0], ac[1], ac[2])).is(Blocks.CHEST);
        int rastrillo = 0;
        for (int v = -3; v <= 3; v++)
            for (int y = PISO; y <= PISO + 6; y++) if (m.getBlockState(new BlockPos(x(-3), y, z(v))).is(Blocks.IRON_BARS)) rastrillo++;
        boolean estatua = m.getBlockState(new BlockPos(x(PLAZA_U), PISO + 3, z(-1))).is(Blocks.BLACKSTONE);
        int calle = 0, muestras = 0;
        for (int u = 4; u <= 90; u += 3) {
            muestras++;
            BlockPos p = new BlockPos(x(u), PISO, z(0));
            BlockState s = m.getBlockState(p);
            if (!m.getBlockState(p.below()).isAir() && s.getCollisionShape(m, p).isEmpty()) calle++;
        }
        return "atril con el libro 22 " + (libro ? "SI" : "NO") + ", arcon del cuartel " + (arcon ? "SI" : "NO") + ", arcon de la camara "
                + (arconCamara ? "SI" : "NO") + ", rastrillo " + rastrillo + " barrotes, estatua " + (estatua ? "SI" : "NO") + ", calle mayor libre "
                + calle + "/" + muestras + ", casas " + a.casas.size() + ", torres " + a.torres + ", montones de ceniza " + a.montones + ", escombros "
                + a.escombros + ", rescoldos " + a.rescoldos + ", carros " + a.carros;
    }
}
