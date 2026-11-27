package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Ahorcado;
import com.darkcraft.fortaleza.Perdidos;
import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Pal;
import com.darkcraft.pueblo.gen.Mat;
import com.darkcraft.pueblo.gen.Trozo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
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
 * La Arboleda de los Ahorcados (parte 7g), en el Bosque Muerto, al oeste de su santuario (el de la estela del bosque): un
 * claro con el <b>Arbol de la Horca</b> en medio (un roble muerto enorme de tronco triple, con raices al aire y ramas
 * largas de las que cuelgan sogas: unas con su lazo vacio, otras cortadas y otras con un ahorcado), alrededor
 * <b>arboles muertos con sus sogas</b>, la <b>puerta</b> (dos arboles a los lados del camino que viene del santuario, con
 * uno colgado encima del camino), el <b>patibulo</b> al norte (el tablado con su trampilla, la horca de tres sogas, el
 * atril con el <b>libro perdido 19</b> —el registro del verdugo— y un arcon) y al suroeste las <b>tumbas</b> de los
 * ahorcados (cruces de madera). Picas con calaveras por el camino, farolas de almas, telaranas en las ramas, huesos.
 * <p>
 * Los ahorcados ({@code enemigos/Ahorcado}) que cuelgan de las sogas los apunta el propio plano ({@link #colgados}): cada
 * uno bajo la soga de bloques que le toca ({@link SogaBlock}). Se dibuja como Velmor y el molino: chunk a chunk al
 * adornar ({@link MorvainGenerador#applyBiomeDecoration}); en su caja {@link Adorno} solo pone setas y helechos.
 */
public final class Arboleda {
    public static final long SEMILLA = 0x4A_B0_1E_DA7AL;
    /** El Arbol de la Horca: el centro de su tronco de 3 x 3. */
    public static final int GX = -4888, GZ = 3192;
    /** El claro alrededor del arbol (sin otros arboles) y lo que ocupa la arboleda. */
    static final int R_CLARO = 14, R = 44;
    /** Todo lo que se dibuja (hasta el santuario, al este): ahi {@link Adorno} solo pone setas y helechos. */
    static final int X0 = GX - 48, X1 = -4843, Z0 = GZ - 52, Z1 = GZ + 46;
    /** El patibulo: el centro de su tablado (mira al sur, al arbol). */
    public static final int PX = GX + 3, PZ = GZ - 28;

    /** El camino del santuario al claro: {x0, z0, x1, z1}. */
    static final int[][] CAMINO = {{-4843, 3200, -4858, 3198}, {-4858, 3198, -4872, 3195}, {-4872, 3195, GX + R_CLARO - 1, GZ + 2}};
    static final double CAMINO_ANCHO = 1.3;

    /** Lo que cuelga de una soga. */
    static final int COLGADO = 0, LAZO = 1, CABO = 2;
    /** La soga de la campana del peregrino (parte 7ag): baja hasta dos bloques por encima de lo que se pisa y de su cabo cuelga la campana. */
    static final int CAMPANA = 3;

    // ------------------------------------------------------------------ el terreno (el de Morvain, sin tocar)

    private static final int AW = X1 - X0 + 1, AD = Z1 - Z0 + 1;
    private static final int[] ALTO = new int[AW * AD];

    static {
        Arrays.fill(ALTO, Integer.MIN_VALUE);
    }

    /** El bloque de arriba del terreno en (x, z). */
    public static int alto(int x, int z) {
        if (x < X0 || x > X1 || z < Z0 || z > Z1) return (int) Math.floor(Terreno.columna(x, z).altura());
        int i = (z - Z0) * AW + (x - X0);
        int v = ALTO[i];
        if (v == Integer.MIN_VALUE) ALTO[i] = v = (int) Math.floor(Terreno.columna(x, z).altura());
        return v;
    }

    /** Lo que se pisa en (x, z). */
    public static int pie(int x, int z) {
        return alto(x, z) + 1;
    }

    // ------------------------------------------------------------------ el plano

    /** Un ahorcado colgado: el sitio bajo su soga (y: lo que se pisa), hacia donde mira y su variante. */
    public record Colgado(int x, int y, int z, float giro, int variante) {}

    /** Los demas ahorcados: de rodillas al pie del arbol, tumbados entre las tumbas, de pie junto al patibulo y en el camino. */
    public record Otro(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    private static Arboleda unico;

    public static synchronized Arboleda get() {
        if (unico == null) unico = new Arboleda();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Colgado> colgados = new ArrayList<>();
    private final List<Otro> otros = new ArrayList<>();
    /** Las columnas que no se tocan con adornos (bajo las sogas, donde esperan los de rodillas y los tumbados). */
    private final Set<Long> libres = new HashSet<>();
    /** La altura del tablado del patibulo (el bloque del suelo; se pisa en FY + 1). */
    public final int fy;
    /** Las sogas que hay (para la prueba). */
    public int sogas, lazos, cabos;
    /** El cabo de la soga de la campana del peregrino (parte 7ag): {x, y, z} del ultimo bloque de soga (la campana cuelga debajo). */
    private int[] campana;

    private Arboleda() {
        // ---- el patibulo (antes que nada: su altura la usan los puestos)
        int alto = Integer.MIN_VALUE;
        for (int x = PX - 4; x <= PX + 4; x++)
            for (int z = PZ - 3; z <= PZ + 2; z++) alto = Math.max(alto, pie(x, z));
        fy = alto + 2;
        elementos.add(new Suelo());
        elementos.add(new Camino());
        // ---- los que no cuelgan (sus sitios, libres de adornos): tres de rodillas al pie del arbol mirando al tronco,
        // tres tumbados entre las tumbas, uno de pie junto al patibulo y otro que ronda por el camino
        otro(Ahorcado.GARRAS, GX + 1, GZ + 3, 180f, com.darkcraft.enemigos.Enemigo.REZA, 0, "arbol");
        otro(Ahorcado.SOGA, GX - 3, GZ - 2, -90f, com.darkcraft.enemigos.Enemigo.REZA, 0, "arbol");
        otro(Ahorcado.GARRAS, GX + 3, GZ - 1, 90f, com.darkcraft.enemigos.Enemigo.REZA, 0, "arbol");
        otro(Ahorcado.GARRAS, GX - 26, GZ + 14, -90f, com.darkcraft.enemigos.Enemigo.TUMBADO, 0, "tumbas");
        otro(Ahorcado.SOGA, GX - 18, GZ + 14, 90f, com.darkcraft.enemigos.Enemigo.TUMBADO, 0, "tumbas");
        otro(Ahorcado.GARRAS, GX - 22, GZ + 20, 180f, com.darkcraft.enemigos.Enemigo.TUMBADO, 0, "tumbas");
        otro(Ahorcado.SOGA, PX - 6, PZ + 5, 0f, com.darkcraft.enemigos.Enemigo.DE_PIE, 3, "patibulo");
        otro(Ahorcado.GARRAS, GX + 20, GZ + 3, -90f, com.darkcraft.enemigos.Enemigo.DE_PIE, 5, "camino");
        // ---- el Arbol de la Horca
        Lista arbol = new Lista();
        arbolHorca(arbol);
        elementos.add(arbol.pieza(4));
        // ---- los arboles de alrededor, con sus sogas
        Lista otros = new Lista();
        arbol(otros, GX + 20, GZ - 14, 11, false, new Rama(-1, 0, 6, 5, new Soga(3, COLGADO, 0, Ahorcado.GARRAS)), new Rama(0, -1, 8, 3));
        arbol(otros, GX + 24, GZ + 15, 12, true, new Rama(0, -1, 6, 5, new Soga(4, COLGADO, 0, Ahorcado.SOGA)), new Rama(1, 0, 7, 3, new Soga(2, LAZO, 2, 0)));
        arbol(otros, GX + 6, GZ + 25, 10, false, new Rama(-1, 0, 6, 6, new Soga(3, LAZO, 2, 0), new Soga(5, COLGADO, 0, Ahorcado.GARRAS)));
        arbol(otros, GX - 8, GZ + 30, 12, true, new Rama(1, 0, 6, 5, new Soga(4, COLGADO, 0, Ahorcado.GARRAS)), new Rama(0, 1, 8, 3));
        arbol(otros, GX - 31, GZ + 3, 13, false, new Rama(0, 1, 7, 6, new Soga(3, CABO, 1, 0), new Soga(5, LAZO, 3, 0)),
                new Rama(1, 0, 6, 4, new Soga(3, COLGADO, 0, Ahorcado.SOGA)));
        arbol(otros, GX - 26, GZ - 18, 11, true, new Rama(1, 0, 6, 6, new Soga(4, COLGADO, 0, Ahorcado.GARRAS)), new Rama(0, -1, 7, 3));
        arbol(otros, GX - 13, GZ - 23, 10, false, new Rama(0, -1, 7, 4, new Soga(3, LAZO, 2, 0)), new Rama(-1, 0, 6, 3));
        arbol(otros, GX + 17, GZ - 32, 11, true, new Rama(0, 1, 6, 4, new Soga(3, CABO, 2, 0)), new Rama(1, 0, 8, 3));
        arbol(otros, GX - 36, GZ - 6, 9, true, new Rama(0, -1, 6, 3), new Rama(1, 0, 5, 3, new Soga(2, CABO, 1, 0)));
        arbol(otros, GX + 12, GZ + 36, 10, true, new Rama(0, -1, 6, 4, new Soga(3, LAZO, 2, 0)));
        // ---- la puerta: dos arboles a los lados del camino; del de arriba cuelga uno encima del camino
        int qx = GX + 30;
        arbol(otros, qx, GZ + 1, 12, false, new Rama(0, 1, 6, 7, new Soga(5, COLGADO, 0, Ahorcado.GARRAS)), new Rama(0, -1, 8, 3));
        arbol(otros, qx + 1, GZ + 11, 13, false, new Rama(0, -1, 8, 6, new Soga(2, CABO, 1, 0)), new Rama(1, 0, 7, 3, new Soga(2, LAZO, 2, 0)));
        elementos.add(otros.pieza(4));
        // ---- el patibulo, las tumbas, las picas y las farolas
        Lista p = new Lista();
        patibulo(p);
        tumbas(p);
        picas(p);
        elementos.add(p.pieza(5));
        farolas();
        // ---- los sitios con nombre
        sitio("arbol", GX + 3, pie(GX + 3, GZ - 3), GZ - 3);
        sitio("claro", GX + 9, pie(GX + 9, GZ + 6), GZ + 6);
        sitio("puerta", qx + 4, pie(qx + 4, GZ + 6), GZ + 6);
        sitio("entrada", -4845, pie(-4845, 3200), 3200);
        sitio("patibulo", PX, fy + 1, PZ + 2);
        sitio("atril", PX - 3, fy + 1, PZ + 1);
        sitio("arcon", PX + 3, fy + 1, PZ + 1);
        sitio("escalera", PX, pie(PX, PZ + 7), PZ + 7);
        sitio("tumbas", GX - 22, pie(GX - 22, GZ + 15), GZ + 15);
        // el Verdugo (parte 7h): de pie en el tablado, delante de la horca y de la trampilla; y donde se le ve desde el claro
        sitio("verdugo", PX, fy + 1, PZ + 1);
        sitio("ante_patibulo", PX, pie(PX, PZ + 17), PZ + 17);
        // la campana del peregrino (parte 7ag): bajo ella, y donde se la ve (en el camino, al llegar al claro)
        if (campana != null) {
            sitio("campana", campana[0], pie(campana[0], campana[2]), campana[2]);
            sitio("ante_campana", campana[0] + 3, pie(campana[0] + 3, campana[2] + 2), campana[2] + 2);
        }
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo de la arboleda (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en lo de la arboleda: ahi no se ponen arboles ni rocas sueltos. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= X0 && x0 <= X1 && z0 + 15 >= Z0 && z0 <= Z1;
    }

    /** Si (x, z) esta en la arboleda (para el nombre del sitio y su logro). */
    public static boolean dentro(double x, double z) {
        return Math.hypot(x - GX, z - GZ) < R + 2;
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} de la arboleda en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public java.util.Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los ahorcados que cuelgan de las sogas (los apunta {@link MorvainEnemigos}). */
    public List<Colgado> colgados() {
        return colgados;
    }

    /** Los demas ahorcados de la arboleda (los apunta {@link MorvainEnemigos}). */
    public List<Otro> otros() {
        return otros;
    }

    private void otro(int variante, int x, int z, float giro, int postura, int ronda, String grupo) {
        otros.add(new Otro(variante, x, pie(x, z), z, giro, postura, ronda, grupo));
        libre(x, z);
    }

    /** Los sitios con nombre (el arbol, la puerta, el patibulo, el atril...): {x, y, z} (y: lo que se pisa). */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    /** Donde cuelga la campana del peregrino (parte 7ag): {x, y, z} del cabo de su soga; la campana, debajo. */
    public int[] campana() {
        return campana;
    }

    private void sitio(String nombre, int x, int y, int z) {
        sitios.put(nombre, new int[]{x, y, z});
    }

    /** Aparta una columna: ahi no van adornos (setas, huesos, calaveras). */
    private void libre(int x, int z) {
        libres.add(BlockPos.asLong(x, 0, z));
    }

    boolean esLibre(int x, int z) {
        return libres.contains(BlockPos.asLong(x, 0, z));
    }

    /** Si (x, z) esta en el camino del santuario al claro. */
    static boolean enCamino(int x, int z) {
        double borde = (Hash.pct(x, 9, z, SEMILLA + 1) % 3 - 1) * 0.35;
        for (int[] s : CAMINO) if (Velmor.distSeg(x, z, s[0], s[1], s[2], s[3]) <= CAMINO_ANCHO + borde) return true;
        return false;
    }

    /** El bloque de la soga (en las vistas sin juego no hay bloques del mod: una cadena). */
    static BlockState soga(SogaBlock.Parte p) {
        try {
            return DarkCraftMod.SOGA.get().defaultBlockState().setValue(SogaBlock.PARTE, p);
        } catch (Throwable t) {
            return p == SogaBlock.Parte.LAZO ? Blocks.CHAIN.defaultBlockState() : Pal.CHAIN;
        }
    }

    private static BlockState log(BlockState b, Direction.Axis a) {
        return b.hasProperty(RotatedPillarBlock.AXIS) ? b.setValue(RotatedPillarBlock.AXIS, a) : b;
    }

    /** El hacia donde mira de Minecraft (0 = sur, 90 = oeste) para una direccion (dx, dz). */
    static float giro(double dx, double dz) {
        return (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
    }

    // ------------------------------------------------------------------ piezas ya decididas

    /**
     * Una lista de bloques ya decididos (un arbol, el patibulo...): cada chunk escribe lo que le cae. Las sogas se apuntan
     * por su parte y se convierten en el bloque al escribirse: el plano se hace al arrancar el mod (los puestos), antes de que
     * existan los bloques del mod.
     */
    private static final class Lista {
        final LinkedHashMap<Long, BlockState> b = new LinkedHashMap<>();
        final LinkedHashMap<Long, SogaBlock.Parte> sogas = new LinkedHashMap<>();
        final Set<Long> une = new HashSet<>();

        void put(int x, int y, int z, BlockState s) {
            long k = BlockPos.asLong(x, y, z);
            b.remove(k);
            b.put(k, s);
            une.remove(k);
            sogas.remove(k);
        }

        void soga(int x, int y, int z, SogaBlock.Parte p) {
            long k = BlockPos.asLong(x, y, z);
            b.remove(k);
            une.remove(k);
            sogas.put(k, p);
        }

        void une(int x, int y, int z, BlockState s) {
            put(x, y, z, s);
            une.add(BlockPos.asLong(x, y, z));
        }

        boolean hay(int x, int y, int z) {
            long k = BlockPos.asLong(x, y, z);
            return b.containsKey(k) || sogas.containsKey(k);
        }

        Element pieza(int capa) {
            int n = b.size(), ns = sogas.size();
            long[] pos = new long[n + ns];
            BlockState[] st = new BlockState[n];
            SogaBlock.Parte[] partes = new SogaBlock.Parte[ns];
            boolean[] u = new boolean[n];
            int i = 0;
            for (Map.Entry<Long, BlockState> e : b.entrySet()) {
                pos[i] = e.getKey();
                st[i] = e.getValue();
                u[i] = une.contains(e.getKey());
                i++;
            }
            for (Map.Entry<Long, SogaBlock.Parte> e : sogas.entrySet()) {
                pos[i] = e.getKey();
                partes[i - n] = e.getValue();
                i++;
            }
            int x0 = Integer.MAX_VALUE, y0 = Integer.MAX_VALUE, z0 = Integer.MAX_VALUE, x1 = Integer.MIN_VALUE, y1 = Integer.MIN_VALUE, z1 = Integer.MIN_VALUE;
            for (long k : pos) {
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
                    if (j >= n) c.set(x, BlockPos.getY(k), z, Arboleda.soga(partes[j - n]));
                    else if (u[j]) c.unir(x, BlockPos.getY(k), z, st[j]);
                    else c.set(x, BlockPos.getY(k), z, st[j]);
                }
            });
        }
    }

    // ------------------------------------------------------------------ los arboles y sus sogas

    /** Una soga de una rama: a cuantos pasos del tronco, que cuelga y de cuantos bloques (los lazos y los cabos). */
    record Soga(int paso, int tipo, int largo, int variante) {}

    /** Una rama: hacia donde (dx, dz: -1, 0, 1), a que altura sobre el pie del tronco, cuantos pasos y sus sogas. */
    record Rama(int dx, int dz, int h, int largo, Soga... sogas) {}

    /** Donde cae el paso k de una rama (x, y, z) desde un tronco de radio r0 que arranca en la altura y0. */
    private static int[] paso(int tx, int tz, int r0, int y0, Rama r, int k) {
        return new int[]{tx + r.dx * (r0 + k), y0 + r.h + (k - 1) / 4, tz + r.dz * (r0 + k)};
    }

    /** Un arbol muerto de tronco sencillo (de roble oscuro o de abeto) con sus ramas y sus sogas. */
    private void arbol(Lista L, int tx, int tz, int alto, boolean abeto, Rama... ramas) {
        BlockState tronco = (abeto ? Blocks.SPRUCE_LOG : Blocks.DARK_OAK_LOG).defaultBlockState();
        int base = pie(tx, tz);
        for (int y = base - 2; y < base + alto; y++) {
            boolean pelado = Hash.pct(tx, y, tz, SEMILLA + 3) < 12;
            L.put(tx, y, tz, pelado ? log(Pal.b(abeto ? Blocks.STRIPPED_SPRUCE_LOG : Blocks.STRIPPED_DARK_OAK_LOG), Direction.Axis.Y) : tronco);
        }
        // la punta, rota: un palo torcido
        L.une(tx + (Hash.pct(tx, 1, tz, SEMILLA) < 50 ? 1 : 0), base + alto, tz, abeto ? Pal.SP_FENCE : Pal.DO_FENCE);
        // dos raices cortas
        int[][] dirs = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        int a = Hash.h(tx, 0, tz, SEMILLA + 5) & 3;
        for (int k = 0; k < 2; k++) {
            int[] d = dirs[(a + 2 * k + 1) & 3];
            int x = tx + d[0], z = tz + d[1], s = pie(x, z);
            if (Math.abs(s - base) <= 1) L.put(x, s, z, log(tronco, d[0] != 0 ? Direction.Axis.X : Direction.Axis.Z));
        }
        for (Rama r : ramas) rama(L, tx, tz, 0, base, r, tronco, abeto);
        libre(tx, tz);
    }

    /** Una rama con sus sogas (y una telarana de vez en cuando). */
    private void rama(Lista L, int tx, int tz, int r0, int base, Rama r, BlockState tronco, boolean abeto) {
        Direction.Axis eje = r.dx != 0 && r.dz == 0 ? Direction.Axis.X : r.dz != 0 && r.dx == 0 ? Direction.Axis.Z : null;
        int[] ultimo = null;
        // (desde dentro del tronco grueso: asi la rama sale de la columna del centro, la mas alta)
        for (int k = 1 - r0; k <= r.largo; k++) {
            int[] p = paso(tx, tz, r0, base, r, k);
            Direction.Axis e = eje != null ? eje : k % 2 == 0 ? Direction.Axis.X : Direction.Axis.Z;
            L.put(p[0], p[1], p[2], log(tronco, e));
            // en diagonal, el codo que une un paso con el siguiente
            if (eje == null) L.put(p[0], p[1], p[2] - r.dz, log(tronco, Direction.Axis.Z));
            // al subir, el codo de debajo
            if (k > 1 && (k - 1) % 4 == 0) L.put(p[0] - r.dx, p[1], p[2] - r.dz, log(tronco, e));
            ultimo = p;
            if (k < 1) continue;
            boolean soga = false;
            for (Soga s : r.sogas) soga |= s.paso == k;
            if (!soga && Hash.pct(p[0], p[1], p[2], SEMILLA + 7) < 18) L.put(p[0], p[1] - 1, p[2], Pal.COBWEB);
        }
        // la punta: un palo hacia arriba
        if (ultimo != null) {
            L.une(ultimo[0], ultimo[1] + 1, ultimo[2], abeto ? Pal.SP_FENCE : Pal.DO_FENCE);
            if (Hash.pct(ultimo[0], 0, ultimo[2], SEMILLA + 8) < 50) L.une(ultimo[0], ultimo[1] + 2, ultimo[2], abeto ? Pal.SP_FENCE : Pal.DO_FENCE);
        }
        for (Soga s : r.sogas) {
            int[] p = paso(tx, tz, r0, base, r, s.paso);
            soga(L, p[0], p[1] - 1, p[2], s, giro(r.dx, r.dz));
        }
    }

    /** Una soga que baja desde {@code ytop}: hasta la altura de colgar a un ahorcado, o con su lazo o su cabo. */
    private void soga(Lista L, int x, int ytop, int z, Soga s, float giro) {
        soga(L, x, ytop, z, s, giro, pie(x, z));
    }

    /** Igual, sobre un suelo dado (el tablado del patibulo). */
    private void soga(Lista L, int x, int ytop, int z, Soga s, float giro, int suelo) {
        libre(x, z);
        if (s.tipo == CAMPANA) {
            int fondo = suelo + 2;
            for (int y = ytop; y >= fondo; y--) L.soga(x, y, z, SogaBlock.Parte.CUERDA);
            campana = new int[]{x, fondo, z};
            return;
        }
        if (s.tipo == COLGADO) {
            // el ahorcado cuelga con los pies a algo mas de un bloque del suelo: su soga de bloques acaba 4 por encima
            int fondo = suelo + 4;
            if (ytop < fondo) {
                DarkCraftMod.LOGGER.warn("[DarkCraft] arboleda: la rama de {},{} esta baja para colgar (soga {}, fondo {})", x, z, ytop, fondo);
                return;
            }
            for (int y = ytop; y >= fondo; y--) L.soga(x, y, z, SogaBlock.Parte.CUERDA);
            colgados.add(new Colgado(x, suelo, z, giro, s.variante));
            sogas++;
            return;
        }
        int fondo = Math.max(suelo + 1, ytop - Math.max(1, s.largo) + 1);
        for (int y = ytop; y > fondo; y--) L.soga(x, y, z, SogaBlock.Parte.CUERDA);
        L.soga(x, fondo, z, s.tipo == LAZO ? SogaBlock.Parte.LAZO : SogaBlock.Parte.CABO);
        if (s.tipo == LAZO) lazos++;
        else cabos++;
    }

    /**
     * El Arbol de la Horca: tronco de 3 x 3 de roble oscuro (las esquinas mas bajas, la punta sencilla), raices al aire
     * que se hunden en el suelo, seis ramas largas con sus sogas (seis ahorcados) y la copa muerta de ramas que suben.
     */
    private void arbolHorca(Lista L) {
        BlockState tronco = Blocks.DARK_OAK_LOG.defaultBlockState();
        int base = Integer.MAX_VALUE;
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) base = Math.min(base, pie(GX + dx, GZ + dz));
        int alto = 23;
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) {
                int x = GX + dx, z = GZ + dz;
                int h = dx == 0 && dz == 0 ? alto : dx != 0 && dz != 0 ? 9 + (Hash.pct(x, 1, z, SEMILLA) % 4) : 14 + (Hash.pct(x, 2, z, SEMILLA) % 3);
                for (int y = base - 3; y < base + h; y++) {
                    boolean pelado = Hash.pct(x, y, z, SEMILLA + 3) < 10;
                    L.put(x, y, z, pelado ? log(Pal.DO_STRIPPED, Direction.Axis.Y) : tronco);
                }
                libre(x, z);
            }
        // la punta, rota y torcida
        L.une(GX, base + alto, GZ, Pal.DO_FENCE);
        L.une(GX + 1, base + alto, GZ, Pal.DO_FENCE);
        L.une(GX + 1, base + alto + 1, GZ, Pal.DO_FENCE);
        // las raices: entre las ramas, al aire junto al tronco y hundidas mas lejos
        double[] angulos = {22, 112, 202, 292, 157};
        for (int i = 0; i < angulos.length; i++) {
            double a = Math.toRadians(angulos[i]);
            int largo = 5 + (i % 2);
            for (int r = 2; r <= largo; r++) {
                int x = GX + (int) Math.round(Math.cos(a) * r), z = GZ + (int) Math.round(Math.sin(a) * r);
                Direction.Axis e = Math.abs(Math.cos(a)) > Math.abs(Math.sin(a)) ? Direction.Axis.X : Direction.Axis.Z;
                int suelo = pie(x, z);
                L.put(x, r <= 3 ? suelo : suelo - 1, z, log(tronco, e));
                if (r <= 2) L.put(x, suelo + 1, z, log(tronco, e));
            }
        }
        // las ramas de las sogas (seis ahorcados: dos con soga)
        Rama[] ramas = {
                new Rama(1, 0, 7, 9, new Soga(4, COLGADO, 0, Ahorcado.GARRAS), new Soga(6, LAZO, 3, 0), new Soga(8, COLGADO, 0, Ahorcado.SOGA),
                        // la campana del peregrino (parte 7ag): en la punta de la rama que se inclina sobre el camino
                        new Soga(9, CAMPANA, 0, 0)),
                new Rama(1, -1, 11, 6, new Soga(4, LAZO, 4, 0)),
                new Rama(0, -1, 9, 10, new Soga(4, COLGADO, 0, Ahorcado.GARRAS), new Soga(7, CABO, 2, 0), new Soga(9, LAZO, 3, 0)),
                new Rama(-1, 0, 8, 8, new Soga(3, LAZO, 2, 0), new Soga(6, COLGADO, 0, Ahorcado.SOGA)),
                new Rama(-1, 1, 7, 7, new Soga(5, COLGADO, 0, Ahorcado.GARRAS), new Soga(7, CABO, 1, 0)),
                new Rama(0, 1, 10, 9, new Soga(4, LAZO, 4, 0), new Soga(7, COLGADO, 0, Ahorcado.GARRAS)),
                new Rama(1, 1, 13, 5),
                new Rama(-1, -1, 14, 6),
        };
        for (Rama r : ramas) rama(L, GX, GZ, 1, base, r, tronco, false);
        // la copa: ramas que suben torcidas desde lo alto del tronco
        int[][] copa = {{1, 0, 16}, {-1, 0, 18}, {0, 1, 17}, {0, -1, 15}, {1, 1, 19}, {-1, -1, 20}};
        for (int[] q : copa) {
            int x = GX + q[0], z = GZ + q[1], y = base + q[2];
            for (int k = 0; k < 6; k++) {
                if (k % 2 == 0) {
                    x += q[0];
                    z += q[1];
                    L.put(x, y, z, log(tronco, q[0] != 0 && q[1] == 0 ? Direction.Axis.X : Direction.Axis.Z));
                } else {
                    y++;
                    L.put(x, y, z, tronco);
                }
            }
            L.une(x, y + 1, z, Pal.DO_FENCE);
            L.une(x + q[0], y, z + q[1], Pal.DO_FENCE);
        }
        // al pie: huesos medio enterrados y calaveras (lejos de las sogas)
        for (int i = 0; i < 14; i++) {
            double a = Math.toRadians(Hash.h(i, 3, 0, SEMILLA + 11) % 360);
            int r = 3 + Hash.h(i, 4, 0, SEMILLA + 11) % 6;
            int x = GX + (int) Math.round(Math.cos(a) * r), z = GZ + (int) Math.round(Math.sin(a) * r);
            if (esLibre(x, z) || L.hay(x, pie(x, z), z) || L.hay(x, pie(x, z) - 1, z)) continue;
            int suelo = pie(x, z);
            if (i % 3 == 0) L.put(x, suelo, z, Mat.calavera(Hash.h(i, 5, 0, SEMILLA)));
            else L.put(x, suelo - 1, z, i % 3 == 1 ? Pal.b(Blocks.BONE_BLOCK) : Pal.ROOTED_DIRT);
        }
    }

    // ------------------------------------------------------------------ el patibulo

    /**
     * El patibulo: un tablado de roble oscuro de 9 x 6 a tres bloques del suelo sobre postes, con barandilla, la escalera
     * al sur (hacia el arbol), la horca (dos postes y la viga) con tres sogas —un lazo, un cabo y la del ahorcado que
     * cuelga sobre la trampilla—, el atril con el registro del verdugo, el arcon y el tajo; debajo, huesos y telaranas.
     */
    private void patibulo(Lista L) {
        int f = fy;
        for (int x = PX - 4; x <= PX + 4; x++)
            for (int z = PZ - 3; z <= PZ + 2; z++) {
                boolean poste = (x == PX - 4 || x == PX + 4 || x == PX) && (z == PZ - 3 || z == PZ + 2);
                if (poste) for (int y = pie(x, z) - 2; y < f; y++) L.put(x, y, z, Pal.DO_LOG);
                int p = Hash.pct(x, f, z, SEMILLA + 21);
                L.put(x, f, z, p < 70 ? Pal.DO_PLANKS : p < 90 ? Pal.SP_PLANKS : Pal.b(Blocks.STRIPPED_DARK_OAK_WOOD));
                // debajo: huesos, una calavera y telaranas
                int s = pie(x, z);
                int q = Hash.pct(x, 0, z, SEMILLA + 22);
                if (!poste && s < f) {
                    if (q < 8) L.put(x, s, z, Mat.calavera(q));
                    else if (q < 16) L.put(x, s - 1, z, Pal.b(Blocks.BONE_BLOCK));
                    else if (q < 24 && f - 1 > s) L.put(x, f - 1, z, Pal.COBWEB);
                }
            }
        // la trampilla bajo la soga del ahorcado
        L.put(PX, f, PZ - 1, Mat.trampilla(Direction.SOUTH, true, false));
        L.put(PX + 1, f, PZ - 1, Mat.trampilla(Direction.SOUTH, true, false));
        // la barandilla (menos la boca de la escalera), con faroles en las esquinas del norte
        for (int x = PX - 4; x <= PX + 4; x++) {
            L.une(x, f + 1, PZ - 3, Pal.DO_FENCE);
            if (Math.abs(x - PX) > 1) L.une(x, f + 1, PZ + 2, Pal.DO_FENCE);
        }
        for (int z = PZ - 3; z <= PZ + 2; z++) {
            L.une(PX - 4, f + 1, z, Pal.DO_FENCE);
            L.une(PX + 4, f + 1, z, Pal.DO_FENCE);
        }
        L.une(PX - 4, f + 2, PZ - 3, Pal.DO_FENCE);
        L.put(PX - 4, f + 3, PZ - 3, Pal.SOUL_LANTERN);
        L.une(PX + 4, f + 2, PZ - 3, Pal.DO_FENCE);
        L.put(PX + 4, f + 3, PZ - 3, Pal.SOUL_LANTERN);
        // la escalera, al sur, hasta el suelo (con relleno debajo)
        for (int i = 0; ; i++) {
            int z = PZ + 3 + i, y = f - i;
            boolean fin = false;
            for (int x = PX - 1; x <= PX + 1; x++) {
                if (y < pie(x, z)) {
                    fin = true;
                    continue;
                }
                L.put(x, y, z, Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH).setValue(StairBlock.HALF, Half.BOTTOM));
                for (int yy = pie(x, z); yy < y; yy++) L.put(x, yy, z, Pal.DO_PLANKS);
            }
            if (fin || i > 8) break;
        }
        // la horca: dos postes y la viga, con sus tornapuntas
        int viga = f + 6;
        for (int y = f + 1; y < viga; y++) {
            L.put(PX - 3, y, PZ - 1, Pal.DO_LOG);
            L.put(PX + 3, y, PZ - 1, Pal.DO_LOG);
        }
        for (int x = PX - 4; x <= PX + 4; x++) L.put(x, viga, PZ - 1, log(Pal.DO_LOG, Direction.Axis.X));
        // las sogas: el lazo, la del ahorcado (sobre la trampilla) y el cabo
        L.soga(PX - 2, viga - 1, PZ - 1, SogaBlock.Parte.CUERDA);
        L.soga(PX - 2, viga - 2, PZ - 1, SogaBlock.Parte.LAZO);
        lazos++;
        soga(L, PX, viga - 1, PZ - 1, new Soga(0, COLGADO, 0, Ahorcado.SOGA), 0f, f + 1);
        L.soga(PX + 2, viga - 1, PZ - 1, SogaBlock.Parte.CABO);
        cabos++;
        // el atril con el registro del verdugo, el arcon y el tajo
        libre(PX - 3, PZ + 1);
        libre(PX + 3, PZ + 1);
        L.put(PX + 2, f + 1, PZ + 1, log(Pal.b(Blocks.STRIPPED_DARK_OAK_LOG), Direction.Axis.Y));
    }

    /** El atril y el arcon del patibulo (necesitan su bloque con contenido: van aparte). */
    private void atrilYArcon() {
        int f = fy;
        trozo(6, PX - 3, f + 1, PZ + 1, PX - 3, f + 1, PZ + 1, c -> c.lectern(PX - 3, f + 1, PZ + 1, Direction.SOUTH, Perdidos.libro(19)));
        trozo(6, PX + 3, f + 1, PZ + 1, PX + 3, f + 1, PZ + 1, c -> c.chest(PX + 3, f + 1, PZ + 1, Direction.SOUTH, Velmor.BOTIN_BUENO));
    }

    private void trozo(int capa, int x0, int y0, int z0, int x1, int y1, int z1, Trozo.Obra o) {
        elementos.add(new Trozo(capa, x0, y0, z0, x1, y1, z1, o));
    }

    // ------------------------------------------------------------------ las tumbas, las picas y las farolas

    /** Las tumbas de los ahorcados, al suroeste del claro: dos filas de cuatro cruces de madera con su monticulo. */
    private void tumbas(Lista L) {
        int[] xs = {GX - 28, GX - 24, GX - 20, GX - 16};
        int[] zs = {GZ + 11, GZ + 17};
        for (int z : zs)
            for (int x : xs) {
                int s = pie(x, z);
                int p = Hash.pct(x, 0, z, SEMILLA + 31);
                if (p < 12) {
                    // caida: el palo en el suelo
                    L.put(x, s, z, Blocks.DARK_OAK_TRAPDOOR.defaultBlockState());
                } else {
                    L.une(x, s, z, Pal.DO_FENCE);
                    L.une(x, s + 1, z, Pal.DO_FENCE);
                    L.une(x - 1, s + 1, z, Pal.DO_FENCE);
                    L.une(x + 1, s + 1, z, Pal.DO_FENCE);
                    if (p > 70) L.une(x, s + 2, z, Pal.DO_FENCE);
                }
                // el monticulo y lo que dejaron
                for (int k = 1; k <= 2; k++) L.put(x, pie(x, z + k) - 1, z + k, k == 1 ? Pal.ROOTED_DIRT : Pal.COARSE_DIRT);
                if (p % 4 == 0) L.put(x, pie(x, z + 2), z + 2, Mat.velas(1 + p % 3));
                else if (p % 5 == 0) L.put(x, pie(x, z + 2), z + 2, Mat.calavera(p));
                libre(x, z);
                libre(x, z + 1);
                libre(x, z + 2);
            }
        // donde esperan tumbados los tres de las tumbas
        libre(GX - 26, GZ + 14);
        libre(GX - 18, GZ + 14);
        libre(GX - 22, GZ + 20);
    }

    /** Picas con calaveras a los lados del camino y junto al patibulo. */
    private void picas(Lista L) {
        int[][] p = {{-4848, 3197}, {-4853, 3202}, {-4865, 3193}, {-4869, 3199}, {-4877, 3197}, {PX - 3, PZ + 6}, {PX + 3, PZ + 6},
                {GX - 34, GZ + 14}, {GX + 26, GZ - 26}, {GX - 40, GZ - 20}};
        for (int[] q : p) {
            int x = q[0], z = q[1], s = pie(x, z);
            L.une(x, s, z, Pal.DO_FENCE);
            L.une(x, s + 1, z, Pal.DO_FENCE);
            L.put(x, s + 2, z, Mat.calavera(Hash.h(x, 0, z, SEMILLA + 41)));
            libre(x, z);
        }
    }

    /** Farolas de almas por el camino (y el atril y el arcon, que van con su contenido). */
    private void farolas() {
        int[][] f = {{-4850, 3202, 0}, {-4866, 3192, 2}, {GX + 15, GZ + 5, 0}};
        Direction[] dir = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (int[] k : f) {
            int x = k[0], z = k[1], y = pie(x, z);
            Direction b = dir[k[2]];
            libre(x, z);
            trozo(5, x - 1, y, z - 1, x + 1, y + 5, z + 1, c -> Mat.farola(c, x, y, z, 4, b));
        }
        atrilYArcon();
    }

    // ------------------------------------------------------------------ el suelo y el camino

    /** El suelo de la arboleda: tierra, podzol y raices; setas, matojos y helechos; algun hueso y alguna calavera. */
    private final class Suelo extends Element {
        Suelo() {
            super(SEMILLA + 51);
            this.layer = 1;
            bounds(GX - R, 40, GZ - R, GX + R, 140, GZ + R);
        }

        @Override
        public void build(Canvas c) {
            for (int x = c.minX; x <= c.maxX; x++)
                for (int z = c.minZ; z <= c.maxZ; z++) {
                    double d = Math.hypot(x - GX, z - GZ) + (Hash.pct(x, 0, z, seed) - 50) * 0.06;
                    if (d > R || x > X1) continue;
                    int t = alto(x, z);
                    if (t <= Terreno.AGUA) continue;
                    int p = Hash.pct(x, t, z, seed + 1);
                    // la tierra de encima
                    BlockState top = p < 30 ? Pal.PODZOL : p < 52 ? Pal.COARSE_DIRT : p < 64 ? Pal.ROOTED_DIRT : p < 74 ? Pal.MOSS : null;
                    if (top != null) c.set(x, t, z, top);
                    if (esLibre(x, z) || enCamino(x, z)) continue;
                    int q = Hash.pct(x, t + 1, z, seed + 2);
                    BlockState encima = q < 4 ? Pal.b(Blocks.BROWN_MUSHROOM) : q < 5 ? Pal.b(Blocks.RED_MUSHROOM) : q < 8 ? Pal.b(Blocks.DEAD_BUSH)
                            : q < 10 ? Pal.b(Blocks.FERN) : null;
                    if (encima != null && c.isAir(x, t + 1, z)) c.set(x, t + 1, z, encima);
                    else if (q >= 98 && d > R_CLARO && d < R - 4) c.set(x, t, z, Pal.b(Blocks.BONE_BLOCK));
                }
        }
    }

    /** El camino del santuario al claro: tierra pisada, grava y raices. */
    private final class Camino extends Element {
        Camino() {
            super(SEMILLA + 61);
            this.layer = 2;
            bounds(GX + R_CLARO - 4, 40, GZ - 4, X1, 140, 3206);
        }

        @Override
        public void build(Canvas c) {
            for (int x = c.minX; x <= c.maxX; x++)
                for (int z = c.minZ; z <= c.maxZ; z++) {
                    if (x > X1 || !enCamino(x, z)) continue;
                    int t = alto(x, z);
                    int p = Hash.pct(x, t, z, seed);
                    c.set(x, t, z, p < 46 ? Pal.b(Blocks.DIRT_PATH) : p < 72 ? Pal.COARSE_DIRT : p < 88 ? Pal.GRAVEL : Pal.ROOTED_DIRT);
                    for (int k = 1; k <= 2; k++) if (!c.isAir(x, t + k, z) && c.get(x, t + k, z).canBeReplaced()) c.set(x, t + k, z, Pal.AIR);
                }
        }
    }

    // ------------------------------------------------------------------ la cuenta del Verdugo (parte 7h)

    /** Si el Verdugo ha caido: la cuenta se cierra y los colgados ya no vuelven a colgar. */
    public static boolean cuentaCerrada(net.minecraft.server.MinecraftServer srv) {
        return com.darkcraft.enemigos.Puestos.muerto(srv, MorvainEnemigos.PATIBULO, MorvainEnemigos.verdugo());
    }

    /** La soga de bloques de un colgado: la de mas abajo, a menos de nueve bloques de su sitio (o null). */
    private static BlockPos sogaDe(ServerLevel m, Colgado c) {
        for (int dy = 1; dy <= 9; dy++) {
            BlockPos b = new BlockPos(c.x(), c.y() + dy, c.z());
            if (!m.isLoaded(b)) return null;
            if (m.getBlockState(b).getBlock() instanceof SogaBlock) return b;
        }
        return null;
    }

    /**
     * Ha caido el Verdugo: los que siguen colgando descansan (se deshacen en almas) y sus sogas se quedan con el lazo vacio
     * (los que ya andan sueltos siguen peleando; ninguno vuelve a colgar: {@code MorvainEnemigos} los da por fuera de uso).
     */
    public static void liberar(ServerLevel m) {
        int n = 0;
        var sitio = com.darkcraft.enemigos.Puestos.sitio(MorvainEnemigos.ARBOLEDA);
        if (sitio != null)
            for (int i = 0; i < MorvainEnemigos.arboleda().size(); i++)
                if (com.darkcraft.enemigos.Puestos.enemigo(MorvainEnemigos.ARBOLEDA, i) instanceof Ahorcado a && a.postura() == com.darkcraft.enemigos.Enemigo.COLGADO
                        && a.accion() == 0) {
                    m.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL, a.getX(), a.getY() + 1.6, a.getZ(), 14, 0.25, 0.7, 0.25, 0.03);
                    m.playSound(null, a.getX(), a.getY() + 1.5, a.getZ(), net.minecraft.sounds.SoundEvents.SOUL_ESCAPE, net.minecraft.sounds.SoundSource.HOSTILE, 1.6f, 0.7f);
                    a.disolver();
                    n++;
                }
        int lazos = vaciar(m);
        DarkCraftMod.LOGGER.info("[DarkCraft] La cuenta del Verdugo se cierra: descansan {} colgados, {} sogas con el lazo vacio", n, lazos);
    }

    /** Deja con el lazo vacio la soga de cada colgado (la de mas abajo); devuelve cuantas ha cambiado. */
    private static int vaciar(ServerLevel m) {
        int n = 0;
        for (Colgado c : get().colgados) {
            BlockPos b = sogaDe(m, c);
            if (b == null) continue;
            BlockState s = m.getBlockState(b);
            if (s.getValue(SogaBlock.PARTE) == SogaBlock.Parte.LAZO) continue;
            m.setBlock(b, s.setValue(SogaBlock.PARTE, SogaBlock.Parte.LAZO), 2);
            n++;
        }
        return n;
    }

    /** Cada 5 s, con alguien cerca: con la cuenta cerrada, las sogas de los colgados con el lazo vacio (las de chunks que no estaban). */
    public static void cuenta(ServerLevel m) {
        boolean alguien = false;
        for (var p : m.players())
            if (p.distanceToSqr(GX, p.getY(), GZ) < 110 * 110) {
                alguien = true;
                break;
            }
        if (alguien && cuentaCerrada(m.getServer())) vaciar(m);
    }

    /** Para la prueba y el comando: como estan las sogas de los colgados (por su parte de abajo). */
    public static String sogasColgados(ServerLevel m) {
        int cuerda = 0, lazo = 0, cabo = 0, nada = 0;
        for (Colgado c : get().colgados) {
            BlockPos b = sogaDe(m, c);
            if (b == null) {
                nada++;
                continue;
            }
            switch (m.getBlockState(b).getValue(SogaBlock.PARTE)) {
                case CUERDA -> cuerda++;
                case LAZO -> lazo++;
                default -> cabo++;
            }
        }
        return "sogas de los colgados: " + cuerda + " con su colgado, " + lazo + " con el lazo vacio, " + cabo + " cortadas" + (nada > 0 ? ", " + nada + " sin ver" : "");
    }

    // ------------------------------------------------------------------ para la prueba

    /** Si el atril tiene su libro, el arcon, cuantas sogas hay y de cuantos ahorcados. */
    public static String resumen(ServerLevel m) {
        Arboleda a = get();
        int[] at = a.sitio("atril"), ar = a.sitio("arcon");
        BlockPos pa = new BlockPos(at[0], at[1], at[2]), pr = new BlockPos(ar[0], ar[1], ar[2]);
        boolean libro = m.getBlockEntity(pa) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le && Perdidos.de(le.getBook()) == 19;
        boolean arcon = m.getBlockState(pr).is(Blocks.CHEST);
        int bien = 0;
        for (Colgado c : a.colgados) {
            boolean hay = false;
            for (int dy = 1; dy <= 9 && !hay; dy++) hay = m.getBlockState(new BlockPos(c.x(), c.y() + dy, c.z())).getBlock() instanceof SogaBlock;
            if (hay) bien++;
        }
        BlockPos arbol = new BlockPos(GX, pie(GX, GZ) + 10, GZ);
        return "arboleda: " + a.colgados.size() + " colgados (" + bien + " con su soga encima), " + a.sogas + " sogas de colgar, " + a.lazos + " lazos, "
                + a.cabos + " cabos; atril " + (libro ? "con el libro 19" : "SIN LIBRO") + ", arcon " + (arcon ? "SI" : "NO") + ", tronco del arbol "
                + (m.getBlockState(arbol).is(Blocks.DARK_OAK_LOG) || m.getBlockState(arbol).is(Blocks.STRIPPED_DARK_OAK_LOG) ? "SI" : "NO")
                + ", patibulo a Y " + a.fy;
    }
}
