package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Puestos;
import com.darkcraft.enemigos.Salobre;
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
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
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
 * El Costillar de la Ballena (parte 7r), en la Costa de los Huesos, a la puerta del santuario del Costillar —(5244, 3000), 62
 * al oeste—: el esqueleto de una ballena colosal varada en las dunas, la cabeza hacia el mar que ya no esta. Alli sigue el viejo
 * Brann, el Ballenero, arponeando a la Vieja (libro 26: "el viejo Brann sigue arponeando una ballena que hace mucho que no
 * esta"). u = x - {@link #CX}, v = z - {@link #CZ}.
 * <p>
 * El <b>esqueleto</b>: el <b>espinazo</b> (las vertebras con sus apofisis por encima) de la cola a la nuca; las <b>costillas</b>
 * (trece pares, dos de gruesas, en arco del espinazo a la arena, alguna rota con su trozo caido) que cierran la <b>sala</b>
 * donde se pelea; la <b>cola</b>, que baja del lomo a la arena y se tuerce al sur (lejos del santuario) haciendose cada vez mas
 * pequena; la <b>pelvis</b> (dos huesos sueltos); las <b>aletas</b> (la escapula contra las costillas y los huesos de los
 * dedos tirados en la arena, a los dos lados); y la <b>calavera</b> al este (el craneo como un escudo con sus cuencas, el
 * hocico largo y las dos quijadas tiradas a los lados). Dentro, lo de Brann: la <b>fundidora</b> (el hogar de ladrillo con los
 * dos calderos del aceite y su chimenea), los <b>barriles</b> de aceite, la <b>barca</b> volcada, los <b>arpones</b> de
 * repuesto, rollos de cuerda, sal y huesos. Fuera, al suroeste, la <b>choza</b> de Brann (tablas de barcas, el tejado de un
 * casco del reves): su cama, la mesa, el arcon y el <b>atril con el libro perdido 27</b> (su bitacora).
 * <p>
 * Los puestos los apunta el propio plano ({@link #esperas}: Brann de rodilla en medio de la sala mirando al mar y unos
 * salobres fuera). Se dibuja chunk a chunk al adornar ({@link MorvainGenerador#applyBiomeDecoration}); en su caja {@link Adorno}
 * no pone nada; la tarima del santuario (que se escribe antes, con el terreno) no se toca.
 */
public final class Costillar {
    public static final long SEMILLA = 0xC057_1A12_BA11L;
    /** El centro de la sala (entre las costillas). */
    public static final int CX = 5306, CZ = 3000;
    /** El suelo allanado y lo que se pisa. */
    public static final int SUELO = 70, PISO = SUELO + 1;
    /** Las costillas: la primera y la ultima (cada una ocupa u y u + 1) y cada cuanto. */
    public static final int COSTILLA_U0 = -26, COSTILLA_U1 = 22, COSTILLA_PASO = 4;
    /** La calavera: de su nuca a la punta del hocico. */
    public static final int CRANEO_U0 = 25, HOCICO_U1 = 62;
    /** La sala donde se pelea (lo de dentro de las costillas). */
    public static final int SALA_U0 = -27, SALA_U1 = 23;
    /** El santuario del Costillar. */
    static final int SANT_U = 5244 - CX, SANT_V = 3000 - CZ, SANT_R = 11;
    /** Lo allanado (y lo que se funde con el terreno alrededor). */
    static final int U_LLANO0 = -48, U_LLANO1 = 66, V_LLANO = 32;
    static final double FUNDE = 8;
    public static final int X0 = CX + U_LLANO0 - 9, X1 = CX + U_LLANO1 + 9, Z0 = CZ - V_LLANO - 9, Z1 = CZ + V_LLANO + 9;
    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/costillar"),
            BOTIN_BUENO = new ResourceLocation(DarkCraftMod.MODID, "chests/costillar_bueno");
    private static final int[] LADOS = {-1, 1};
    private static final Ruido RUIDO = new Ruido(SEMILLA);

    private static final int AW = X1 - X0 + 1, AD = Z1 - Z0 + 1;
    private static final int[] ALTO = new int[AW * AD];

    static {
        Arrays.fill(ALTO, Integer.MIN_VALUE);
    }

    /** El bloque de arriba del terreno de Morvain en (x, z) (sin el costillar). */
    public static int alto(int x, int z) {
        if (x < X0 || x > X1 || z < Z0 || z > Z1) return (int) Math.floor(Terreno.columna(x, z).altura());
        int i = (z - Z0) * AW + (x - X0);
        int v = ALTO[i];
        if (v == Integer.MIN_VALUE) ALTO[i] = v = (int) Math.floor(Terreno.columna(x, z).altura());
        return v;
    }

    /** Uno del costillar: donde espera (y: lo que pisa), hacia donde mira, su variante, como espera, cuanto ronda y su grupo. */
    public record Espera(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    private static Costillar unico;

    public static synchronized Costillar get() {
        if (unico == null) unico = new Costillar();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    /** Lo que hay (para la prueba y las vistas). */
    public int costillas, rotas, vertebras, huesosCola, dedos, barriles, arpones;

    private Costillar() {
        elementos.add(new Terraza());
        Pilotes.Lista es = new Pilotes.Lista();
        espinazo(es);
        costillas(es);
        cola(es);
        pelvis(es);
        aletas(es);
        calavera(es);
        elementos.add(es.pieza(3));
        Pilotes.Lista ca = new Pilotes.Lista();
        fundidora(ca);
        barca(ca);
        enseres(ca);
        choza(ca);
        elementos.add(ca.pieza(4));
        trozos();
        // ---- los sitios con nombre
        sitio("entrada", SANT_U + 16, PISO, 3);
        sitio("costillar", SALA_U0 + 3, PISO, 0);
        sitio("centro", 0, PISO, 0);
        sitio("brann", 3, PISO, 0);
        sitio("fundidora", -12, PISO, 6);
        sitio("barca", -8, PISO, -7);
        sitio("craneo", 21, PISO, 0);
        sitio("hocico", 46, PISO, 12);
        sitio("aleta", 13, PISO, -21);
        sitio("cola", -50, PISO, -2);
        sitio("choza", -40, PISO, 13);
        sitio("atril", -40, PISO, 20);
        sitio("arcon", -37, PISO, 20);
        sitio("alto", 30, PISO + 12, 0);
        // ---- los puestos
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo del costillar (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en lo del costillar: ahi {@link Adorno} no pone nada. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= X0 && x0 <= X1 && z0 + 15 >= Z0 && z0 <= Z1;
    }

    /** Lo lejos de (x, z) al centro de la sala. */
    public static double distancia(double x, double z) {
        return Math.hypot(x - CX - 0.5, z - CZ - 0.5);
    }

    /** Si (x, z) esta en lo del costillar (el esqueleto y la arena de alrededor): para el nombre del sitio. */
    public static boolean dentro(double x, double z) {
        double u = x - CX, v = z - CZ;
        return u >= U_LLANO0 && u <= U_LLANO1 && Math.abs(v) <= V_LLANO && Math.hypot(u - SANT_U, v - SANT_V) > SANT_R;
    }

    /** Si (x, z) esta dentro de las costillas (la sala donde se pelea con Brann). */
    public static boolean enSala(double x, double z) {
        double u = x - CX, v = z - CZ;
        if (u < SALA_U0 || u > SALA_U1 + 1) return false;
        return Math.abs(v) <= radio(u) - 1.6;
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} del costillar en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Brann y los salobres del costillar (los apunta {@link MorvainEnemigos}); Brann es el primero. */
    public List<Espera> esperas() {
        return esperas;
    }

    /** Los sitios con nombre (la entrada, la sala, la calavera, la choza, el atril...): {x, y, z} (y: lo que se pisa). */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    private void sitio(String nombre, int u, int y, int v) {
        sitios.put(nombre, new int[]{CX + u, y, CZ + v});
    }

    // ------------------------------------------------------------------ medidas del esqueleto

    /** Lo alto del espinazo (la cara de arriba de las vertebras) sobre la sala en u. */
    static double lomo(double u) {
        return PISO + 21 - (u / 32.0) * (u / 32.0) * 7;
    }

    /** Lo que se abren las costillas (de v = 0 a donde tocan la arena) en u. */
    static double radio(double u) {
        return 16 - (u / 30.0) * (u / 30.0) * 4;
    }

    /** Por donde va la cola en u (la v de su centro), al oeste de la sala: se tuerce al sur. */
    static double colaV(double u) {
        if (u >= -34) return 0;
        double t = Math.min(1.0, (-34 - u) / 32.0);
        return -24 * Math.pow(t, 1.6);
    }

    /** Lo alto de la cola en u: baja del lomo a la arena entre u -30 y -50. */
    static double colaY(double u) {
        if (u >= -30) return lomo(u);
        if (u <= -50) return PISO;
        double t = (u + 50) / 20.0;
        return PISO + (lomo(-30) - PISO) * Math.pow(t, 1.3);
    }

    // ------------------------------------------------------------------ bloques

    static BlockState b(Block k) {
        return k.defaultBlockState();
    }

    static BlockState hueso(Direction.Axis a) {
        return Varadero.hueso(a);
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

    private static boolean hay(Pilotes.Lista m, int u, int y, int v) {
        return m.hay(x(u), y, z(v));
    }

    private static boolean junto(int u, int v, int r) {
        return Math.hypot(u - SANT_U, v - SANT_V) < r;
    }

    /** Hueso viejo: casi todo hueso, con algo de calcita y piedra de arena (lo comido por la sal). */
    static BlockState huesoViejo(int x, int y, int z, Direction.Axis a) {
        int p = Hash.pct(x, y, z, SEMILLA + 5);
        return p < 84 ? hueso(a) : p < 93 ? b(Blocks.CALCITE) : b(Blocks.SMOOTH_SANDSTONE);
    }

    // ------------------------------------------------------------------ el suelo

    /** Lo alto del suelo en (x, z): lo allanado (y lo que se funde) o MIN_VALUE donde no se toca. */
    static int objetivo(int x, int z) {
        int u = x - CX, v = z - CZ;
        double du = u < U_LLANO0 ? U_LLANO0 - u : u > U_LLANO1 ? u - U_LLANO1 : 0;
        double dv = Math.max(0, Math.abs(v) - V_LLANO);
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

    /** La arena de la sala y de alrededor: arena gris con costras de sal, grava, conchas (hueso) y alguna mata seca. */
    private static BlockState arena(int u, int v, int p) {
        double n = RUIDO.ruido(u / 9.0 + 7, v / 9.0 - 3);
        if (n > 0.38) return p < 45 ? b(Blocks.CALCITE) : p < 75 ? b(Blocks.WHITE_CONCRETE_POWDER) : b(Blocks.SAND);
        if (n < -0.5) return p < 50 ? Pal.GRAVEL : p < 80 ? b(Blocks.SAND) : b(Blocks.SANDSTONE);
        return p < 80 ? b(Blocks.SAND) : p < 88 ? b(Blocks.SANDSTONE) : p < 91 ? Pal.GRAVEL : p < 98 ? b(Blocks.SAND) : b(Blocks.BONE_BLOCK);
    }

    /**
     * La terraza: corta lo que sobra hasta el suelo y rellena por debajo; arena con costras de sal; matas secas y sal suelta
     * fuera de la sala. La tarima del santuario no se toca.
     */
    private final class Terraza extends Element {
        Terraza() {
            super(SEMILLA + 51);
            this.layer = 1;
            bounds(X0, 50, Z0, X1, 120, Z1);
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
                    for (int y = o + 1; y <= Math.max(t, o) + 4; y++) c.set(x, y, z, Pal.AIR);
                    for (int y = t + 1; y < o; y++) c.set(x, y, z, y >= o - 2 ? b(Blocks.SAND) : b(Blocks.SANDSTONE));
                    int p = Hash.pct(x, o, z, seed);
                    c.set(x, o, z, arena(u, v, p));
                    if (o != SUELO) continue;
                    boolean sala = enSala(x + 0.5, z + 0.5);
                    int q = Hash.pct(x, o + 1, z, seed + 1);
                    if (!sala && q < 1) c.set(x, o + 1, z, Pal.b(Blocks.DEAD_BUSH));
                    else if (q >= 98) c.set(x, o + 1, z, Varadero.nieve(1 + (q & 1)));
                }
        }
    }

    // ------------------------------------------------------------------ el esqueleto

    /** El espinazo: las vertebras (3 x 3) de la nuca a la grupa, con su apofisis por encima cada dos y las de los lados. */
    private void espinazo(Pilotes.Lista m) {
        for (int u = -30; u <= CRANEO_U0; u++) {
            int y = (int) Math.round(lomo(u));
            vertebras++;
            for (int dv = -1; dv <= 1; dv++)
                for (int dy = -2; dy <= 0; dy++) {
                    // las juntas entre vertebras, mas estrechas
                    if (Math.floorMod(u, 2) == 1 && Math.abs(dv) == 1 && dy != -1) continue;
                    put(m, u, y + dy, dv, huesoViejo(x(u), y + dy, z(dv), Direction.Axis.X));
                }
            if (Math.floorMod(u, 2) == 0) {
                // la apofisis de arriba (mas alta en medio) y las de los lados
                int alto = 2 + (int) Math.round(2.0 * (1 - Math.abs(u) / 30.0));
                for (int k = 1; k <= alto; k++) put(m, u, y + k, 0, hueso(Direction.Axis.Y));
                for (int s : LADOS) put(m, u, y - 1, 2 * s, hueso(Direction.Axis.Z));
            }
        }
    }

    /**
     * Las costillas: cada una un arco grueso (dos de ancho en u y dos de grueso) del espinazo a la arena, a los dos lados; se
     * abren algo hacia fuera a media altura. Alguna rota (le falta un trozo, que esta tirado en la arena al lado).
     */
    private void costillas(Pilotes.Lista m) {
        for (int u0 = COSTILLA_U0; u0 <= COSTILLA_U1; u0 += COSTILLA_PASO) {
            costillas++;
            for (int s : LADOS) {
                int rota = Hash.pct(u0, s, 7, SEMILLA + 121);
                boolean quebrada = rota < 22;
                double desde = 0.30 + (rota % 5) * 0.08, hasta = desde + 0.16;
                if (quebrada) rotas++;
                for (int du = 0; du <= 1; du++) {
                    int u = u0 + du;
                    double top = lomo(u) - 1, R = radio(u), H = top - SUELO;
                    for (int v = 1; v <= (int) Math.ceil(R) + 1; v++)
                        for (int y = SUELO; y <= (int) Math.ceil(top); y++) {
                            double ev = (v - 1.5) / (R - 1.5), ey = (y - SUELO) / H;
                            double e = ev * ev + ey * ey;
                            if (e < 0.80 || e > 1.0 || v < 2) continue;
                            // donde esta rota: el angulo (de 0 arriba a 1 en la arena)
                            double ang = Math.atan2(ev, ey) / (Math.PI / 2);
                            if (quebrada && ang >= desde && ang <= hasta) continue;
                            // el eje del hueso: de lado arriba, de pie abajo
                            Direction.Axis eje = ang < 0.4 ? Direction.Axis.Z : Direction.Axis.Y;
                            put(m, u, y, s * v, huesoViejo(x(u), y, z(s * v), eje));
                        }
                }
                if (quebrada) {
                    // el trozo que falta, tirado en la arena junto a la costilla (por dentro de la sala)
                    int vv = (int) Math.round(radio(u0) * 0.55) * s;
                    for (int k = 0; k < 4; k++) put(m, u0 - 1 - k, PISO, vv, hueso(Direction.Axis.X));
                    put(m, u0 - 2, PISO + 1, vv, Varadero.nieve(2));
                }
            }
        }
    }

    /** La cola: baja de la grupa a la arena y se tuerce al sur, cada vez mas pequena (lejos del santuario). */
    private void cola(Pilotes.Lista m) {
        for (int u = -31; u >= -66; u--) {
            double cy = colaY(u), cv = colaV(u);
            int ancho = u > -44 ? 1 : 0;
            int y = (int) Math.round(cy), v = (int) Math.round(cv);
            huesosCola++;
            for (int dv = -ancho; dv <= ancho; dv++)
                for (int dy = (u > -50 ? -2 : 0); dy <= 0; dy++) {
                    if (Math.floorMod(u, 2) == 1 && Math.abs(dv) == 1 && dy != -1) continue;
                    put(m, u, y + dy, v + dv, huesoViejo(x(u), y + dy, z(v + dv), Direction.Axis.X));
                }
            // las apofisis, cada vez mas bajas, y los huesos de los lados (en la arena)
            if (Math.floorMod(u, 2) == 0 && u > -58) put(m, u, y + 1, v, hueso(Direction.Axis.Y));
            if (Math.floorMod(u, 3) == 0 && u <= -50 && u > -62)
                for (int s : LADOS) put(m, u, PISO, v + s * (ancho + 1), hueso(Direction.Axis.Z));
        }
        // los primeros huesos de la cola, sueltos, se han ido rodando cuesta abajo (al sur)
        put(m, -60, PISO, -16, hueso(Direction.Axis.X));
        put(m, -64, PISO, -19, b(Blocks.BONE_BLOCK));
    }

    /** La pelvis: dos huesos sueltos a los lados de la grupa, medio enterrados. */
    private void pelvis(Pilotes.Lista m) {
        for (int s : LADOS)
            for (int k = 0; k < 5; k++) put(m, -33 + k, PISO, s * (5 + k / 2), hueso(Direction.Axis.X));
    }

    /** Las aletas: la escapula contra las costillas y, tirados en la arena hacia atras, el brazo y los huesos de los dedos. */
    private void aletas(Pilotes.Lista m) {
        int u0 = 12;
        for (int s : LADOS) {
            double R = radio(u0);
            int vEsc = (int) Math.ceil(R) + 1;
            // la escapula: una plancha de hueso apoyada en la costilla
            for (int du = 0; du <= 3; du++)
                for (int y = PISO; y <= PISO + 4 - du / 2; y++) put(m, u0 + du, y, s * vEsc, huesoViejo(x(u0 + du), y, z(s * vEsc), Direction.Axis.Y));
            // el brazo (corto) y los cuatro dedos, que se abren
            for (int k = 1; k <= 4; k++) put(m, u0 + 1, PISO, s * (vEsc + k), hueso(Direction.Axis.Z));
            for (int dedo = 0; dedo < 4; dedo++) {
                double ang = Math.toRadians(-60 + dedo * 22);
                for (int k = 1; k <= 7 + (dedo == 1 || dedo == 2 ? 2 : 0); k++) {
                    int uu = u0 + 1 + (int) Math.round(Math.sin(ang) * k);
                    int vv = s * (vEsc + 4 + (int) Math.round(Math.cos(ang) * k));
                    if (k % 3 == 0) continue;   // las juntas
                    put(m, uu, PISO, vv, hueso(Math.abs(Math.sin(ang)) > 0.6 ? Direction.Axis.X : Direction.Axis.Z));
                    dedos++;
                }
            }
        }
    }

    /**
     * La calavera, al este (hacia el mar): el craneo como un escudo que sube desde la nuca (con las cuencas de los ojos a los
     * lados y un hueco detras, donde entra el espinazo), el hocico largo que se estrecha y, en la arena, las dos quijadas.
     */
    private void calavera(Pilotes.Lista m) {
        for (int u = CRANEO_U0; u <= HOCICO_U1; u++) {
            double w, top, base;
            if (u <= 40) {
                w = 11 - (u - CRANEO_U0) * 0.25;
                top = PISO + 12 - (u - CRANEO_U0) * 0.45;
                base = SUELO;
            } else {
                w = Math.max(1.5, 7.2 - (u - 40) * 0.26);
                top = PISO + 5.2 - (u - 40) * 0.13;
                base = u >= 44 ? PISO + 1 : SUELO;
            }
            for (int v = -(int) Math.ceil(w); v <= (int) Math.ceil(w); v++) {
                double fv = Math.abs(v) / w;
                if (fv > 1.02) continue;
                int alto = (int) Math.round(top - (top - base) * 0.55 * fv * fv);
                for (int y = (int) base; y <= alto; y++) {
                    // las cuencas de los ojos y el hueco de la nuca
                    boolean cuenca = u >= 31 && u <= 33 && Math.abs(v) >= (int) Math.ceil(w) - 1 && y >= PISO + 3 && y <= PISO + 5;
                    boolean nuca = u <= CRANEO_U0 + 1 && Math.abs(v) <= 2 && y >= PISO + 4 && y <= PISO + 7;
                    // por dentro, hueco (que no pese tanto el chunk): solo la cascara de dos
                    boolean dentroCraneo = u > CRANEO_U0 + 2 && u < 39 && Math.abs(v) < w - 2.5 && y > base + 1 && y < alto - 1;
                    if (cuenca || nuca || dentroCraneo) {
                        put(m, u, y, v, Pal.AIR);
                        continue;
                    }
                    Direction.Axis eje = y >= alto - 1 ? Direction.Axis.X : Direction.Axis.Y;
                    put(m, u, y, v, huesoViejo(x(u), y, z(v), eje));
                }
            }
        }
        // las quijadas: dos huesos largos tirados en la arena, que se abren hacia atras
        for (int s : LADOS)
            for (int u = 30; u <= HOCICO_U1 + 3; u++) {
                double t = (u - 30) / (double) (HOCICO_U1 + 3 - 30);
                int v = s * (int) Math.round(12 - t * 9);
                int arco = (int) Math.round(Math.sin(t * Math.PI) * 1.2);
                for (int dv = 0; dv <= 1; dv++) put(m, u, PISO + arco, v + s * dv, hueso(Direction.Axis.X));
                if (arco > 0) put(m, u, PISO, v, Pal.AIR);
            }
        // unos dientes sueltos en la arena junto al hocico
        for (int k = 0; k < 5; k++) put(m, 48 + k * 3, PISO, (k % 2 == 0 ? 1 : -1) * (5 + k % 3), b(Blocks.POINTED_DRIPSTONE));
    }

    // ------------------------------------------------------------------ lo de Brann

    /** La fundidora: el hogar de ladrillo con los dos calderos del aceite encima, las brasas debajo y la chimenea. */
    private void fundidora(Pilotes.Lista m) {
        int u0 = -16, v0 = 7;
        for (int du = 0; du <= 5; du++)
            for (int dv = 0; dv <= 2; dv++)
                for (int y = PISO; y <= PISO + 1; y++) {
                    boolean boca = dv == 0 && y == PISO && (du == 1 || du == 4);
                    put(m, u0 + du, y, v0 + dv, boca ? b(Blocks.CAMPFIRE) : b(Hash.pct(x(u0 + du), y, z(v0 + dv), SEMILLA + 31) < 80 ? Blocks.BRICKS : Blocks.MUD_BRICKS));
                }
        // los dos calderos con el aceite (agua) encima del hogar
        for (int du : new int[]{1, 4})
            put(m, u0 + du, PISO + 2, v0 + 1, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 2 + (du & 1)));
        // la chimenea (al fondo) y el humo lo hacen las brasas de debajo
        for (int y = PISO + 2; y <= PISO + 6; y++) put(m, u0 + 2, y, v0 + 2, b(y == PISO + 6 ? Blocks.MUD_BRICK_WALL : Blocks.BRICKS));
        put(m, u0 + 3, PISO + 2, v0 + 2, b(Blocks.BRICK_SLAB));
        // los barriles del aceite, alrededor
        int[][] bs = {{-18, 8}, {-18, 9}, {-17, 10}, {-9, 9}, {-9, 10}, {-10, 11}};
        for (int[] q : bs) {
            put(m, q[0], PISO, q[1], Varadero.barril(Direction.UP));
            barriles++;
        }
        put(m, -18, PISO + 1, 9, Varadero.barril(Direction.EAST));
        barriles++;
    }

    /** La barca de Brann, volcada contra las costillas del sur, con un remo roto al lado. */
    private void barca(Pilotes.Lista m) {
        int uc = -8, vc = -10, largo = 11;
        Block[] ma = Varadero.madera(1);
        double hl = largo / 2.0, hw = 2.0;
        for (int a = -(int) Math.ceil(hl); a <= (int) Math.ceil(hl); a++)
            for (int c = -2; c <= 2; c++) {
                double w = hw * Math.sqrt(Math.max(0, 1 - (2.0 * a / largo) * (2.0 * a / largo)));
                if (Math.abs(c) > w + 0.35) continue;
                int u = uc + a, v = vc + c;
                boolean borde = Math.abs(c) > w - 0.85 || Math.abs(a) >= hl - 0.6;
                if (borde) for (int y = PISO; y <= PISO + 1; y++) put(m, u, y, v, b(ma[0]));
                else put(m, u, PISO + 1, v, b(ma[0]));
                if (c == 0) put(m, u, PISO + 2, v, Varadero.losa(ma[2], SlabType.BOTTOM));   // la quilla
            }
        // el remo roto en la arena
        for (int k = 0; k < 4; k++) put(m, uc - 2 + k, PISO, vc + 4, b(Blocks.STRIPPED_SPRUCE_LOG).setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.X));
        put(m, uc + 2, PISO, vc + 4, Varadero.losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
    }

    /** Lo demas de la sala: los arpones de repuesto de pie, rollos de cuerda, cajas, montones de sal y huesos sueltos. */
    private void enseres(Pilotes.Lista m) {
        // los arpones de repuesto, de pie contra la costilla del norte (pararrayos: hierro y cobre viejo)
        for (int k = 0; k < 5; k++) {
            int u = 8 + k, v = 10 + (k % 2);
            put(m, u, PISO, v, b(Blocks.SPRUCE_FENCE));
            put(m, u, PISO + 1, v, Blocks.LIGHTNING_ROD.defaultBlockState().setValue(LightningRodBlock.FACING, Direction.UP));
            arpones++;
        }
        // rollos de cuerda (lana marron con su alfombra) y cajas de la pesca
        int[][] rollos = {{-4, 9}, {16, -9}, {-20, -6}};
        for (int[] q : rollos) {
            put(m, q[0], PISO, q[1], b(Blocks.BROWN_WOOL));
            put(m, q[0], PISO + 1, q[1], b(Blocks.BROWN_CARPET));
            put(m, q[0] + 1, PISO, q[1], b(Blocks.BROWN_CARPET));
        }
        put(m, 17, PISO, -8, Varadero.barril(Direction.UP));
        put(m, -21, PISO, -8, b(Blocks.SPRUCE_PLANKS));
        put(m, -21, PISO + 1, -8, Varadero.losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
        barriles++;
        // sal y huesos sueltos por la sala (lejos del centro: ahi se pelea)
        int[][] sal = {{-23, 4}, {-22, -3}, {18, 5}, {19, -4}, {6, 11}, {-6, -12}};
        for (int[] q : sal) put(m, q[0], PISO, q[1], Varadero.nieve(2 + Math.floorMod(q[0] + q[1], 3)));
        put(m, 20, PISO, 8, hueso(Direction.Axis.Z));
        put(m, -24, PISO, -9, hueso(Direction.Axis.X));
    }

    /**
     * La choza de Brann, al suroeste (fuera de las costillas): paredes de tablas de barcas, el tejado de un casco del reves, la
     * puerta al norte (hacia las costillas) y una ventana; dentro la cama, la mesa con el farol, el arcon, un barril y el atril con su bitacora.
     */
    private void choza(Pilotes.Lista m) {
        int u0 = -44, u1 = -36, v0 = 14, v1 = 21;
        Block[] ma = Varadero.madera(0);
        for (int u = u0; u <= u1; u++)
            for (int v = v0; v <= v1; v++) {
                boolean muro = u == u0 || u == u1 || v == v0 || v == v1;
                put(m, u, SUELO, v, b(Blocks.SPRUCE_PLANKS));
                for (int y = PISO; y <= PISO + 3; y++) {
                    if (!muro) {
                        put(m, u, y, v, Pal.AIR);
                        continue;
                    }
                    boolean esquina = (u == u0 || u == u1) && (v == v0 || v == v1);
                    boolean puerta = v == v0 && u == -40 && y <= PISO + 1;
                    boolean ventana = u == u1 && v == 17 && y == PISO + 1;
                    if (puerta) continue;
                    if (ventana) {
                        une(m, u, y, v, b(Blocks.SPRUCE_FENCE));
                        continue;
                    }
                    put(m, u, y, v, esquina ? b(Blocks.STRIPPED_DARK_OAK_LOG) : b(Hash.pct(x(u), y, z(v), SEMILLA + 41) < 70 ? ma[0] : Blocks.DARK_OAK_PLANKS));
                }
            }
        // el tejado: un casco del reves (escalones hacia fuera y la quilla arriba)
        for (int u = u0 - 1; u <= u1 + 1; u++)
            for (int v = v0 - 1; v <= v1 + 1; v++) {
                int dv = Math.min(v - (v0 - 1), (v1 + 1) - v);
                int y = PISO + 4 + Math.min(dv, 2);
                if (dv >= 3) y = PISO + 6;
                Direction cara = v - (v0 - 1) <= (v1 + 1) - v ? Direction.SOUTH : Direction.NORTH;
                put(m, u, y, v, dv >= 3 ? b(Blocks.DARK_OAK_PLANKS) : Varadero.escalera(Blocks.DARK_OAK_STAIRS, cara, false));
                for (int yy = PISO + 4; yy < y; yy++) if (u >= u0 && u <= u1 && v >= v0 && v <= v1) put(m, u, yy, v, b(Blocks.DARK_OAK_PLANKS));
            }
        // la puerta
        BlockState puerta = Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH);
        put(m, -40, PISO, v0, puerta.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        put(m, -40, PISO + 1, v0, puerta.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        // dentro: la cama (al oeste), la mesa con el farol, un barril y el farol colgado del techo
        BlockState cama = Blocks.BROWN_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH);
        put(m, u0 + 1, PISO, 18, cama.setValue(BedBlock.PART, BedPart.HEAD));
        put(m, u0 + 1, PISO, 19, cama.setValue(BedBlock.PART, BedPart.FOOT));
        put(m, u1 - 1, PISO, 15, b(Blocks.SPRUCE_FENCE));
        put(m, u1 - 1, PISO + 1, 15, Varadero.losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
        put(m, u1 - 1, PISO + 1, 16, Pal.b(Blocks.LANTERN));
        put(m, u0 + 1, PISO, 15, Varadero.barril(Direction.UP));
        put(m, -40, PISO + 3, 17, Pal.b(Blocks.CHAIN));
        put(m, -40, PISO + 2, 17, Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true));
        // fuera, contra la pared del este: redes secandose y un arpon
        put(m, u1 + 1, PISO, 15, b(Blocks.SPRUCE_FENCE));
        put(m, u1 + 1, PISO + 1, 15, Pal.COBWEB);
        put(m, u1 + 1, PISO, 19, Blocks.LIGHTNING_ROD.defaultBlockState().setValue(LightningRodBlock.FACING, Direction.UP));
    }

    // ------------------------------------------------------------------ lo que lleva datos: el atril, el arcon, los barriles

    private void trozos() {
        // contra la pared del sur, de cara a la puerta (al norte)
        trozo(-40, PISO, 20, c -> c.lectern(x(-40), PISO, z(20), Direction.NORTH, Perdidos.libro(27)));
        trozo(-37, PISO, 20, c -> c.chest(x(-37), PISO, z(20), Direction.NORTH, BOTIN_BUENO));
        trozo(-9, PISO, 9, c -> c.barrel(x(-9), PISO, z(9), Direction.UP, BOTIN));
        trozo(17, PISO, -8, c -> c.barrel(x(17), PISO, z(-8), Direction.UP, BOTIN));
        trozo(-43, PISO, 15, c -> c.barrel(x(-43), PISO, z(15), Direction.UP, BOTIN));
    }

    private void trozo(int u, int y, int v, Trozo.Obra o) {
        elementos.add(new Trozo(6, x(u), y, z(v), x(u), y, z(v), o));
    }

    // ------------------------------------------------------------------ Brann y los salobres

    private static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;

    private void ponerEsperas() {
        int R0 = Salobre.REDERO, B = Salobre.BICHERO, S = Salobre.SALINERO;
        int R = Enemigo.REZA, T = Enemigo.TUMBADO, D = Enemigo.DE_PIE;
        // Brann, de rodilla en medio de la sala, mirando al mar (al este): el puesto 0
        en(0, 3, PISO, 0, ESTE, R, 0, Puestos.JEFE);
        // fuera: dos rederos remendando junto a la choza, un bichero de ronda por la cola y otro junto a la calavera, y un
        // salinero enterrado en la sal junto a la aleta del sur
        en(R0, -36, PISO, 11, SUR, R, 0, "choza");
        en(R0, -46, PISO, 11, ESTE, R, 0, "choza");
        en(B, -34, PISO, -16, OESTE, D, 4, "cola");
        en(B, 36, PISO, 17, SUR, D, 3, "calavera");
        en(S, 10, PISO, -27, NORTE, T, 0, "aleta");
    }

    /** El hacia donde mira de Minecraft (0 = sur, 90 = oeste) para una direccion (du, dv). */
    static float giro(double du, double dv) {
        return (float) (Math.toDegrees(Math.atan2(dv, du)) - 90.0);
    }

    private void en(int variante, int u, int y, int v, float giro, int postura, int ronda, String grupo) {
        esperas.add(new Espera(variante, x(u), y, z(v), giro, postura, ronda, grupo));
    }

    // ------------------------------------------------------------------ al caer Brann

    /** Para las pruebas: los salobres que se han deshecho en sal al caer Brann. */
    public static int DESCANSAN;

    /** Si Brann ha caido (y no ha vuelto). */
    public static boolean enPaz(net.minecraft.server.MinecraftServer srv) {
        return Puestos.muerto(srv, MorvainEnemigos.COSTILLAR, MorvainEnemigos.ballenero());
    }

    /**
     * Cada segundo: con Brann caido, "la marea vuelve": los salobres que seguian faenando de rodillas (en el Puerto Varado y en
     * el costillar; no los que pelean) se deshacen en sal y ya no vuelven.
     */
    public static void paz(ServerLevel m) {
        if (!enPaz(m.getServer())) return;
        descansar(m, MorvainEnemigos.PUERTO, MorvainEnemigos.puerto());
        descansar(m, MorvainEnemigos.COSTILLAR, MorvainEnemigos.costillar());
    }

    private static void descansar(ServerLevel m, String sitio, List<Puestos.Puesto> puestos) {
        if (Puestos.sitio(sitio) == null) return;
        for (int i = 0; i < puestos.size(); i++) {
            Puestos.Puesto p = puestos.get(i);
            if (p.postura() != Enemigo.REZA || Puestos.JEFE.equals(p.grupo())) continue;
            Enemigo e = Puestos.enemigo(sitio, i);
            if (e == null || e.level() != m || e.getTarget() != null || e.accion() != 0) continue;
            m.sendParticles(Salobre.POLVO, e.getX(), e.getY() + 1.0, e.getZ(), 40, 0.4, 0.8, 0.4, 0.1);
            m.sendParticles(ParticleTypes.SPLASH, e.getX(), e.getY() + 1.2, e.getZ(), 30, 0.4, 0.6, 0.4, 0.1);
            m.sendParticles(ParticleTypes.CLOUD, e.getX(), e.getY() + 1.0, e.getZ(), 8, 0.3, 0.5, 0.3, 0.02);
            e.disolver();
            DESCANSAN++;
        }
    }

    // ------------------------------------------------------------------ para la prueba

    /** Lo que hay en el costillar ya generado (para la prueba): el atril con el libro 27, el arcon, el espinazo, las costillas. */
    public static String resumen(ServerLevel m) {
        Costillar a = get();
        int[] at = a.sitio("atril"), ar = a.sitio("arcon");
        boolean libro = m.getBlockEntity(new BlockPos(at[0], at[1], at[2])) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le
                && Perdidos.de(le.getBook()) == 27;
        boolean arcon = m.getBlockState(new BlockPos(ar[0], ar[1], ar[2])).is(Blocks.CHEST);
        boolean lomo = m.getBlockState(new BlockPos(CX, (int) Math.round(lomo(0)), CZ)).is(Blocks.BONE_BLOCK)
                || m.getBlockState(new BlockPos(CX, (int) Math.round(lomo(0)), CZ)).is(Blocks.CALCITE);
        int pies = 0;
        for (int u0 = COSTILLA_U0; u0 <= COSTILLA_U1; u0 += COSTILLA_PASO)
            for (int s : LADOS) {
                int v = (int) Math.ceil(radio(u0)) ;
                for (int dv = -1; dv <= 1; dv++) {
                    BlockState q = m.getBlockState(new BlockPos(CX + u0, PISO, CZ + s * (v + dv)));
                    if (q.is(Blocks.BONE_BLOCK) || q.is(Blocks.CALCITE) || q.is(Blocks.SMOOTH_SANDSTONE)) {
                        pies++;
                        break;
                    }
                }
            }
        // (la sal suelta —capas de nieve— no estorba)
        int libre = 0;
        for (int u = -18; u <= 18; u += 3)
            for (int v = -6; v <= 6; v += 3) {
                BlockState q = m.getBlockState(new BlockPos(CX + u, PISO, CZ + v));
                if (q.isAir() || q.is(Blocks.SNOW)) libre++;
            }
        // la cascara de la calavera por arriba (por dentro es hueca)
        BlockState cr = m.getBlockState(new BlockPos(CX + 30, PISO + 9, CZ));
        boolean craneo = cr.is(Blocks.BONE_BLOCK) || cr.is(Blocks.CALCITE) || cr.is(Blocks.SMOOTH_SANDSTONE);
        return "atril con el libro 27 " + (libro ? "SI" : "NO") + ", arcon " + (arcon ? "SI" : "NO") + ", espinazo " + (lomo ? "SI" : "NO")
                + ", costillas en la arena " + pies + "/" + (2 * ((COSTILLA_U1 - COSTILLA_U0) / COSTILLA_PASO + 1)) + ", calavera " + (craneo ? "SI" : "NO")
                + ", suelo de la sala libre " + libre + "/65";
    }
}
