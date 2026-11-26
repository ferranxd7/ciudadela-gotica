package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Erudito;
import com.darkcraft.enemigos.Puestos;
import com.darkcraft.enemigos.Rectora;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * El Observatorio de la Academia (parte 7p), en la meseta, al este de la Academia Espectral, junto al santuario del
 * Observatorio: aqui subio la Rectora Ysolde con sus maestros la noche del cielo rojo a leer el fuego que caia (libro perdido
 * 24) y aqui sigue, flotando ante el gran telescopio ({@code enemigos/Rectora}). "El cielo la lee a ella."
 * <p>
 * Sobre una <b>terraza</b> de caliza (dos por encima de la meseta, con su balaustrada, los obeliscos de las esquinas y la
 * escalinata del sur) se alza la <b>rotonda</b>: un muro redondo de caliza con zocalo y cornisa de pizarra, ocho lancetas
 * moradas y, encima, la <b>cupula de cobre</b> verdoso con sus nervios de pizarra y la <b>rendija</b> abierta al cielo (del
 * cenit al norte). Dentro, el suelo es una <b>carta del cielo</b> (el disco azul de lapislazuli con sus estrellas de luz y
 * las constelaciones de cuarzo, el anillo de las horas y el de los doce signos); colgado de la cupula, el <b>planetario</b>
 * (el sol y cuatro planetas en sus brazos de cadena); al norte, en su <b>estrado</b>, el <b>gran telescopio</b> de cobre que
 * sale por la rendija, y en el muro cuatro <b>hornacinas</b> con los atriles de los maestros. Al sur, el <b>vestibulo</b>
 * (estantes, atriles y la alfombra morada) con la puerta de la rotonda: en cuanto empieza la pelea la cierra un <b>sello</b>
 * de luz ({@link #tick}); se rompe al caer la Rectora (o al volver ella a su sitio). Al este, la <b>celda de la Rectora</b>
 * (su mesa, el globo celeste, el arcon y el atril con el <b>libro perdido 25</b>), cerrada con una reja mientras ella viva;
 * al oeste, la <b>sala de los instrumentos</b> (el globo celeste grande, las mesas con sus instrumentos y una escala a la
 * <b>azotea</b>, con su anteojo). En la terraza, el <b>reloj de sol</b>; fuera, el <b>camino</b> al santuario con sus faroles y
 * el <b>circulo de piedras</b> (los menhires con que se median las estrellas antes de la academia).
 * <p>
 * Se dibuja como la academia: chunk a chunk al adornar; en su caja {@link Adorno} no pone nada.
 */
public final class Observatorio {
    public static final long SEMILLA = 0x0B5E_7A70_7105L;
    /** El centro de la rotonda (u = x - CX hacia el este, v = z - CZ hacia el sur); el santuario esta en v = 57. */
    public static final int CX = 5577, CZ = -3600;
    /** El suelo de la terraza y de dentro (lo que se pisa esta en {@link #PISO}). */
    public static final int SUELO = 240, PISO = SUELO + 1;
    /** La rotonda: lo que se pisa llega a {@link #R_DENTRO} (del centro del bloque del centro); el muro, hasta {@link #R_FUERA}. */
    public static final double R_DENTRO = 14.5, R_FUERA = 16.5;
    /** Lo alto del muro de la rotonda: de ahi arranca la cupula (su centro). */
    public static final int MURO_ALTO = PISO + 12;
    /** La cupula: semiesfera con el centro en lo alto del muro; por dentro llega a CUP_R0 y por fuera a CUP_R1. */
    static final double CUP_R0 = 15.0, CUP_R1 = 16.5;
    /** La rendija de la cupula: |u| <= 1, v de RENDIJA_V0 a RENDIJA_V1 (del cenit al norte). */
    static final int RENDIJA_V0 = -16, RENDIJA_V1 = -1;
    /** El estrado del telescopio (al norte, un escalon mas alto): v <= ESTRADO_V (la fila de los escalones). */
    public static final int ESTRADO_V = -7;
    /** Donde espera la Rectora (en el estrado, bajo el ocular): u 0, v -9. */
    public static final int RECTORA_V = -9;
    /** El tubo del telescopio: de abajo (el ocular, v -11 en PISO + 5) a arriba (el objetivo, v -14 en PISO + 21, en la rendija). */
    static final int TUBO_Y0 = PISO + 5, TUBO_Y1 = PISO + 21;
    static final double TUBO_V0 = -11, TUBO_V1 = -14;
    /** La puerta del sur (la del sello): |u| <= 1, cuatro de alto y su punta; el sello va en v 15 (la cara de dentro del muro). */
    public static final int PUERTA_U = 1, PUERTA_ALTO = 4, SELLO_V = 15;
    /** El vestibulo: muros en |u| = VES_U y en v = VES_V1 (con la puerta de fuera); lo de dentro, |u| <= 5, hasta v 24. */
    static final int VES_U = 6, VES_V1 = 25, VES_ALTO = PISO + 6;
    /** La celda de la Rectora (al este) y la sala de los instrumentos (al oeste): |u| hasta SALA_U1, |v| <= SALA_V. */
    static final int SALA_U1 = 26, SALA_V = 6, SALA_TECHO = PISO + 6;
    /** La reja de la celda (en el muro de la rotonda: u = 15, |v| <= 1) y el paso al oeste (u -15, -16). */
    public static final int REJA_U = 15;
    /** La terraza (el podio): |u| <= TER_U, v TER_V0..TER_V1; la escalinata del sur, |u| <= 3, en v 32 y 33. */
    static final int TER_U = 30, TER_V0 = -24, TER_V1 = 31;
    /** El camino al santuario (de la escalinata a la tarima del santuario). */
    static final int CAMINO_V0 = 34, CAMINO_V1 = 52;
    /** El circulo de piedras alrededor (lejos de la terraza) y sus diez menhires. */
    static final double R_PIEDRAS = 46;
    /** Lo que toca. */
    public static final int X0 = CX - 52, X1 = CX + 52, Z0 = CZ - 52, Z1 = CZ + 54;
    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/observatorio");
    /** Las hornacinas de los maestros (angulos desde el este hacia el sur): de alli salen los que llama la Rectora. */
    static final double[] HORNACINAS = {45, 135, 225, 315};
    /** Las lancetas del muro (angulos). */
    static final double[] LANCETAS = {22.5, 67.5, 112.5, 157.5, 202.5, 247.5, 292.5, 337.5};
    /** Las estrellas del suelo (u, v): las de cada constelacion van unidas en orden; la del centro es la polar. */
    static final int[][][] CONSTELACIONES = {
            {{-7, -2}, {-5, -5}, {-2, -6}, {1, -4}},
            {{3, -6}, {6, -3}, {7, 1}, {4, 3}},
            {{1, 6}, {-3, 7}, {-6, 4}, {-4, 1}},
            {{2, 1}, {4, -1}},
    };

    private static final int AW = X1 - X0 + 1, AD = Z1 - Z0 + 1;
    private static final int[] ALTO = new int[AW * AD];

    static {
        Arrays.fill(ALTO, Integer.MIN_VALUE);
    }

    /** El bloque de arriba del terreno de Morvain en (x, z) (sin el observatorio). */
    public static int alto(int x, int z) {
        if (x < X0 || x > X1 || z < Z0 || z > Z1) return (int) Math.floor(Terreno.columna(x, z).altura());
        int i = (z - Z0) * AW + (x - X0);
        int v = ALTO[i];
        if (v == Integer.MIN_VALUE) ALTO[i] = v = (int) Math.floor(Terreno.columna(x, z).altura());
        return v;
    }

    /** Un enemigo del observatorio: donde espera (y: lo que pisa), hacia donde mira, su variante, como espera, cuanto ronda y su grupo. */
    public record Espera(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    private static Observatorio unico;

    public static synchronized Observatorio get() {
        if (unico == null) unico = new Observatorio();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    /** Lo que hay (para la prueba y las vistas). */
    public int lancetas, estrellas, planetas, menhires, estantes, faroles;

    private Observatorio() {
        elementos.add(new Terraza());
        Pilotes.Lista te = new Pilotes.Lista();
        terraza(te);
        camino(te);
        circulo(te);
        elementos.add(te.pieza(3));
        Pilotes.Lista ro = new Pilotes.Lista();
        rotonda(ro);
        cupula(ro);
        elementos.add(ro.pieza(3));
        Pilotes.Lista de = new Pilotes.Lista();
        estrado(de);
        telescopio(de);
        planetario(de);
        hornacinas(de);
        elementos.add(de.pieza(4));
        Pilotes.Lista sa = new Pilotes.Lista();
        vestibulo(sa);
        celda(sa);
        instrumentos(sa);
        elementos.add(sa.pieza(4));
        trozos();
        // ---- los sitios con nombre
        sitio("entrada", 0, pie(CX, CZ + 46), 46);
        sitio("camino", 0, pie(CX, CZ + 38), 38);
        sitio("escalinata", 0, PISO, 30);
        sitio("porche", 0, PISO, 27);
        sitio("vestibulo", 0, PISO, 20);
        sitio("puerta", 0, PISO, 13);
        sitio("rotonda", 0, PISO, 9);
        sitio("centro", 0, PISO, 0);
        sitio("estrado", 3, PISO + 1, -9);
        sitio("rectora", 0, PISO + 1, RECTORA_V);
        sitio("telescopio", -3, PISO + 1, -11);
        for (int k = 0; k < HORNACINAS.length; k++) {
            double a = Math.toRadians(HORNACINAS[k]);
            int u = (int) Math.round(Math.cos(a) * 11.0), v = (int) Math.round(Math.sin(a) * 11.0);
            sitio("hornacina_" + k, u, v <= ESTRADO_V ? PISO + 1 : PISO, v);
        }
        sitio("celda", 21, PISO, 0);
        sitio("atril", 23, PISO, -3);
        sitio("arcon", 23, PISO, 3);
        sitio("instrumentos", -21, PISO, -3);
        sitio("azotea", -21, SALA_TECHO + 1, 2);
        sitio("reloj", 21, PISO, -14);
        sitio("circulo", 0, pie(CX, CZ - 41), -41);
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo del observatorio (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en lo del observatorio: ahi {@link Adorno} no pone nada. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= X0 && x0 <= X1 && z0 + 15 >= Z0 && z0 <= Z1;
    }

    /** Lo lejos de (x, z) del centro de la rotonda (el centro del bloque del centro). */
    public static double distancia(double x, double z) {
        return Math.hypot(x - (CX + 0.5), z - (CZ + 0.5));
    }

    /** Si (x, z) esta en la terraza del observatorio (o en su escalinata): para el nombre del sitio. */
    public static boolean dentro(double x, double z) {
        double u = x - CX, v = z - CZ;
        return Math.abs(u) <= TER_U + 1 && v >= TER_V0 - 1 && v <= TER_V1 + 3;
    }

    /** Si (x, z) esta dentro de la rotonda (donde pelea la Rectora, y su logro). */
    public static boolean enRotonda(double x, double z) {
        return distancia(x, z) < R_DENTRO + 0.1;
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} del observatorio en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los que esperan en el observatorio (los apunta {@link MorvainEnemigos}; el primero, la Rectora). */
    public List<Espera> esperas() {
        return esperas;
    }

    /** Los sitios con nombre (la entrada, el vestibulo, la puerta, el centro, el estrado, la celda...): {x, y, z} (y: lo que se pisa). */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    private void sitio(String nombre, int u, int y, int v) {
        sitios.put(nombre, new int[]{CX + u, y, CZ + v});
    }

    /** Donde salen los maestros que llama la Rectora: delante de cada hornacina ({x, y, z}: lo que se pisa). */
    public static List<int[]> hornacinas() {
        List<int[]> l = new ArrayList<>();
        for (int k = 0; k < HORNACINAS.length; k++) l.add(get().sitio("hornacina_" + k));
        return l;
    }

    /** Lo que se pisa en (x, z) fuera de la terraza (el terreno). */
    static int pie(int x, int z) {
        return alto(x, z) + 1;
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

    /** Lo lejos de la celda (u, v) del centro de la rotonda. */
    static double d(int u, int v) {
        return Math.hypot(u, v);
    }

    /** El angulo de (u, v) en grados, de 0 a 360 (desde el este hacia el sur). */
    static double angulo(int u, int v) {
        double a = Math.toDegrees(Math.atan2(v, u));
        return a < 0 ? a + 360 : a;
    }

    /** Lo que se separa (en bloques, a lo largo del muro) la celda (u, v) de la direccion {@code grados}. */
    static double aparte(int u, int v, double grados) {
        double da = Math.abs(angulo(u, v) - grados);
        if (da > 180) da = 360 - da;
        return Math.toRadians(da) * d(u, v);
    }

    /** Si (u, v) es del muro de la rotonda. */
    static boolean muro(int u, int v) {
        double r = d(u, v);
        return r >= R_DENTRO && r <= R_FUERA;
    }

    /** La piedra del observatorio: la caliza de la academia. */
    static BlockState piedra(int u, int y, int v) {
        return Academia.piedra(x(u), y, z(v));
    }

    static BlockState oscura(int u, int y, int v) {
        return Academia.oscura(x(u), y, z(v));
    }

    static BlockState cadena(Direction.Axis a) {
        return Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, a);
    }

    static BlockState varilla(Direction f) {
        return Blocks.END_ROD.defaultBlockState().setValue(DirectionalBlock.FACING, f);
    }

    static BlockState farolColgado() {
        return Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
    }

    /** El cobre de la cupula: verdoso, con algun pano menos gastado. */
    static BlockState cobre(int u, int y, int v) {
        int p = Hash.pct(x(u), y, z(v), SEMILLA + 7);
        if (p < 62) return b(Blocks.OXIDIZED_CUT_COPPER);
        if (p < 86) return b(Blocks.WAXED_WEATHERED_CUT_COPPER);
        return b(Blocks.OXIDIZED_COPPER);
    }

    // ------------------------------------------------------------------ el suelo: la terraza, la escalinata y la carta del cielo

    /** El suelo de dentro de la rotonda: la carta del cielo. */
    static BlockState cartaDelCielo(int u, int v) {
        double r = d(u, v);
        // la polar en el centro y las estrellas y sus lineas
        if (u == 0 && v == 0) return b(Blocks.GOLD_BLOCK);
        if (r <= 10.0) {
            for (int[][] c : CONSTELACIONES)
                for (int[] s : c) if (s[0] == u && s[1] == v) return b(Blocks.SEA_LANTERN);
            for (int[][] c : CONSTELACIONES)
                for (int k = 0; k + 1 < c.length; k++) if (cerca(u, v, c[k], c[k + 1]) < 0.42) return b(Blocks.SMOOTH_QUARTZ);
            int p = Hash.pct(x(u), SUELO, z(v), SEMILLA + 11);
            return p < 62 ? b(Blocks.LAPIS_BLOCK) : p < 88 ? b(Blocks.BLUE_TERRACOTTA) : b(Blocks.BLUE_CONCRETE);
        }
        // el anillo de las horas: pizarra pulida con doce marcas de oro
        if (r <= 11.2) {
            double a = angulo(u, v) % 30.0;
            return (a < 4.5 || a > 25.5) && r > 10.4 ? b(Blocks.GILDED_BLACKSTONE) : b(Blocks.POLISHED_BLACKSTONE);
        }
        // los doce signos: sectores de caliza y de pizarra, con su raya de pizarra labrada
        int sector = (int) (angulo(u, v) / 30.0);
        double a = angulo(u, v) % 30.0;
        if (Math.toRadians(Math.min(a, 30.0 - a)) * r < 0.5) return b(Blocks.DEEPSLATE_BRICKS);
        return sector % 2 == 0 ? b(Blocks.CALCITE) : Pal.DS_TILES;
    }

    /** Lo lejos de (u, v) del segmento a-b. */
    static double cerca(int u, int v, int[] a, int[] bb) {
        double dx = bb[0] - a[0], dy = bb[1] - a[1], L = dx * dx + dy * dy;
        double t = L == 0 ? 0 : Math.max(0, Math.min(1, ((u - a[0]) * dx + (v - a[1]) * dy) / L));
        double qx = a[0] + t * dx, qy = a[1] + t * dy;
        return Math.hypot(u - qx, v - qy);
    }

    /** Si (u, v) es de la terraza (el podio) o de la escalinata. */
    static boolean enTerraza(int u, int v) {
        return Math.abs(u) <= TER_U && v >= TER_V0 && v <= TER_V1;
    }

    /** El suelo de la terraza fuera de los edificios: losas de caliza y diorita, con su cenefa junto al borde. */
    static BlockState losas(int u, int v, int p) {
        boolean borde = Math.abs(u) == TER_U - 1 || v == TER_V0 + 1 || v == TER_V1 - 1;
        if (borde) return b(Blocks.POLISHED_DEEPSLATE);
        return p < 44 ? b(Blocks.CALCITE) : p < 70 ? b(Blocks.POLISHED_DIORITE) : p < 84 ? b(Blocks.DIORITE) : p < 94 ? Pal.DS_TILES
                : b(Blocks.POLISHED_ANDESITE);
    }

    /**
     * La terraza: la levanta sobre la meseta (por debajo, piedra; los lados, sillares de pizarra), corta lo que sobre por
     * encima y pone el suelo de cada sitio (la carta del cielo en la rotonda, la tarima de tablas de la celda...).
     */
    private final class Terraza extends Element {
        Terraza() {
            super(SEMILLA + 51);
            this.layer = 1;
            bounds(CX - TER_U, 200, CZ + TER_V0, CX + TER_U, 300, CZ + TER_V1);
        }

        @Override
        public void build(Canvas c) {
            for (int x = Math.max(c.minX, x0); x <= Math.min(c.maxX, x1); x++)
                for (int z = Math.max(c.minZ, z0); z <= Math.min(c.maxZ, z1); z++) {
                    int u = x - CX, v = z - CZ;
                    int t = alto(x, z);
                    for (int y = SUELO + 1; y <= Math.max(t, SUELO) + 8; y++) c.set(x, y, z, Pal.AIR);
                    boolean lado = Math.abs(u) == TER_U || v == TER_V0 || v == TER_V1;
                    for (int y = Math.min(t, SUELO) - 1; y < SUELO; y++) c.set(x, y, z, lado ? oscura(u, y, v) : Pal.STONE);
                    int p = Hash.pct(x, SUELO, z, seed);
                    BlockState s;
                    double r = d(u, v);
                    if (r < R_DENTRO) s = cartaDelCielo(u, v);
                    else if (Math.abs(u) <= 5 && v >= 16 && v < VES_V1) s = Math.abs(u) <= 1 ? b(Blocks.POLISHED_DEEPSLATE) : Pal.DS_TILES;
                    else if (Math.abs(u) > 16 && Math.abs(u) < SALA_U1 && Math.abs(v) < SALA_V) s = u > 0 ? Pal.DO_PLANKS : Pal.DS_TILES;
                    else if (lado) s = oscura(u, SUELO, v);
                    else s = losas(u, v, p);
                    c.set(x, SUELO, z, s);
                }
        }
    }

    /** La balaustrada, los obeliscos de las esquinas, la escalinata, el reloj de sol y los faroles de la terraza. */
    private void terraza(Pilotes.Lista m) {
        // la balaustrada (muretes de pizarra con un pilar cada cinco), salvo en la escalinata
        for (int u = -TER_U; u <= TER_U; u++)
            for (int v = TER_V0; v <= TER_V1; v++) {
                boolean borde = Math.abs(u) == TER_U || v == TER_V0 || v == TER_V1;
                if (!borde || v == TER_V1 && Math.abs(u) <= 3) continue;
                boolean pilar = (u + v) % 5 == 0 || Math.abs(u) == TER_U && (v == TER_V0 || v == TER_V1);
                if (pilar) {
                    put(m, u, PISO, v, b(Blocks.POLISHED_DEEPSLATE));
                    put(m, u, PISO + 1, v, Academia.losa(Blocks.POLISHED_DEEPSLATE_SLAB, SlabType.BOTTOM));
                } else une(m, u, PISO, v, b(Blocks.DEEPSLATE_BRICK_WALL));
            }
        // los obeliscos de las cuatro esquinas (con su luz)
        for (int su = -1; su <= 1; su += 2)
            for (int vc : new int[]{TER_V0 + 1, TER_V1 - 1}) obelisco(m, su * (TER_U - 1), PISO, vc, 6);
        // la escalinata del sur: dos escalones de pizarra hasta la meseta, y sus pilares con farol
        for (int u = -3; u <= 3; u++) {
            put(m, u, SUELO, TER_V1 + 1, Academia.escalera(Blocks.DEEPSLATE_BRICK_STAIRS, Direction.NORTH, false));
            for (int y = alto(x(u), z(TER_V1 + 1)); y < SUELO; y++) put(m, u, y, TER_V1 + 1, oscura(u, y, TER_V1 + 1));
            put(m, u, SUELO - 1, TER_V1 + 2, Academia.escalera(Blocks.DEEPSLATE_BRICK_STAIRS, Direction.NORTH, false));
            for (int y = alto(x(u), z(TER_V1 + 2)); y < SUELO - 1; y++) put(m, u, y, TER_V1 + 2, oscura(u, y, TER_V1 + 2));
        }
        for (int s = -1; s <= 1; s += 2) {
            put(m, s * 4, PISO, TER_V1, b(Blocks.POLISHED_DEEPSLATE));
            put(m, s * 4, PISO + 1, TER_V1, b(Blocks.POLISHED_DEEPSLATE));
            put(m, s * 4, PISO + 2, TER_V1, Pal.SOUL_LANTERN);
            faroles++;
        }
        // el reloj de sol (al noreste de la terraza): el disco de losas con las doce horas y su gnomon
        int ru = 21, rv = -16;
        for (int du = -3; du <= 3; du++)
            for (int dv = -3; dv <= 3; dv++) {
                double r = Math.hypot(du, dv);
                if (r > 3.3) continue;
                boolean hora = r > 2.4 && ((du == 0) || (dv == 0) || Math.abs(du) == Math.abs(dv));
                put(m, ru + du, PISO, rv + dv, hora ? Academia.losa(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.BOTTOM)
                        : Academia.losa(Blocks.SMOOTH_STONE_SLAB, SlabType.BOTTOM));
            }
        put(m, ru, PISO, rv, b(Blocks.CHISELED_DEEPSLATE));
        une(m, ru, PISO + 1, rv, b(Blocks.POLISHED_DEEPSLATE_WALL));
        put(m, ru, PISO + 2, rv, b(Blocks.LIGHTNING_ROD).setValue(DirectionalBlock.FACING, Direction.UP));
        // al noroeste, un banco de piedra mirando al reloj y dos pebeteros a la puerta del vestibulo
        for (int u = -23; u <= -20; u++) put(m, u, PISO, -17, Academia.escalera(Blocks.POLISHED_DEEPSLATE_STAIRS, Direction.NORTH, false));
        for (int s = -1; s <= 1; s += 2) {
            put(m, s * 3, PISO, VES_V1 + 2, b(Blocks.CHISELED_DEEPSLATE));
            put(m, s * 3, PISO + 1, VES_V1 + 2, Pal.SOUL_CAMPFIRE);
        }
    }

    /** Un obelisco de caliza (con su basa y su remate de pizarra y una varilla de luz en la punta). */
    private void obelisco(Pilotes.Lista m, int u, int y0, int v, int alto) {
        put(m, u, y0, v, b(Blocks.CHISELED_DEEPSLATE));
        for (int y = y0 + 1; y < y0 + alto; y++) put(m, u, y, v, y == y0 + alto - 1 ? b(Blocks.POLISHED_DEEPSLATE) : b(Blocks.CALCITE));
        put(m, u, y0 + alto, v, varilla(Direction.UP));
    }

    /** El camino de la escalinata al santuario: losas de caliza con faroles de almas a los lados. */
    private void camino(Pilotes.Lista m) {
        for (int v = CAMINO_V0; v <= CAMINO_V1; v++)
            for (int u = -1; u <= 1; u++) {
                int y = alto(x(u), z(v));
                int p = Hash.pct(x(u), y, z(v), SEMILLA + 21);
                put(m, u, y, v, p < 40 ? b(Blocks.CALCITE) : p < 68 ? b(Blocks.POLISHED_DIORITE) : p < 86 ? Pal.GRAVEL : b(Blocks.TUFF));
                put(m, u, y + 1, v, Pal.AIR);
            }
        for (int v = CAMINO_V0 + 2; v <= CAMINO_V1; v += 6) {
            int s = (v / 6) % 2 == 0 ? 1 : -1;
            int u = s * 3, y = pie(x(u), z(v));
            une(m, u, y, v, b(Blocks.POLISHED_DEEPSLATE_WALL));
            une(m, u, y + 1, v, b(Blocks.POLISHED_DEEPSLATE_WALL));
            put(m, u, y + 2, v, Pal.SOUL_LANTERN);
            faroles++;
        }
    }

    /**
     * El circulo de piedras: diez menhires de toba y pizarra alrededor de la terraza (el del sur no: ahi va el camino), uno
     * caido; y tres arboles secos.
     */
    private void circulo(Pilotes.Lista m) {
        for (int k = 0; k < 10; k++) {
            double a = Math.toRadians(k * 36.0 + 18.0);
            if (Math.abs(Math.toDegrees(a) - 90) < 20) continue;
            int u = (int) Math.round(Math.cos(a) * R_PIEDRAS), v = (int) Math.round(Math.sin(a) * R_PIEDRAS);
            int y0 = pie(x(u), z(v));
            int alto = 4 + Hash.pct(x(u), 0, z(v), SEMILLA + 31) % 3;
            if (k == 7) {
                // el caido: tumbado hacia fuera
                for (int i = 0; i < alto; i++) {
                    int uu = u + (int) Math.round(Math.cos(a) * i), vv = v + (int) Math.round(Math.sin(a) * i);
                    put(m, uu, pie(x(uu), z(vv)), vv, i % 2 == 0 ? b(Blocks.TUFF) : b(Blocks.POLISHED_DEEPSLATE));
                }
                menhires++;
                continue;
            }
            for (int y = y0 - 1; y < y0 + alto; y++) put(m, u, y, v, y == y0 + alto - 1 ? b(Blocks.POLISHED_DEEPSLATE) : y % 3 == 0 ? Pal.DS_TILES
                    : b(Blocks.TUFF));
            // una marca de luz en lo alto (las estrellas que median)
            if (k % 2 == 0) put(m, u, y0 + alto, v, varilla(Direction.UP));
            menhires++;
        }
        int[][] arboles = {{-38, 18}, {35, -30}, {-30, -36}};
        for (int[] q : arboles) {
            int y0 = pie(x(q[0]), z(q[1]));
            for (int y = y0; y < y0 + 5; y++) put(m, q[0], y, q[1], Pal.DO_LOG);
            put(m, q[0] + 1, y0 + 4, q[1], Academia.tronco(Direction.Axis.X));
            put(m, q[0] + 2, y0 + 5, q[1], Pal.DO_LOG);
            put(m, q[0], y0 + 3, q[1] - 1, Academia.tronco(Direction.Axis.Z));
            put(m, q[0], y0 + 4, q[1] - 2, Pal.DO_LOG);
        }
    }

    // ------------------------------------------------------------------ la rotonda: el muro, las lancetas y las puertas

    /** Si (u, v) es de una de las puertas del muro (la del sur, la de la celda o el paso de los instrumentos) y hasta que alto. */
    static int puerta(int u, int v) {
        if (!muro(u, v)) return -1;
        if (Math.abs(u) <= PUERTA_U && v > 0) return PISO + PUERTA_ALTO - 1;
        if (Math.abs(v) <= 1 && u > 0) return PISO + PUERTA_ALTO - 1;
        if (Math.abs(v) <= 1 && u < 0) return PISO + PUERTA_ALTO;
        return -1;
    }

    private void rotonda(Pilotes.Lista m) {
        for (int u = -17; u <= 17; u++)
            for (int v = -17; v <= 17; v++) {
                if (!muro(u, v)) continue;
                int arriba = puerta(u, v);
                boolean fuera = d(u, v) > R_FUERA - 1.0;
                for (int y = SUELO; y <= MURO_ALTO; y++) {
                    if (arriba >= 0 && y >= PISO && y <= arriba) {
                        put(m, u, y, v, Pal.AIR);
                        continue;
                    }
                    // la punta del arco de cada puerta
                    if (arriba >= 0 && y == arriba + 1 && (u == 0 || v == 0)) {
                        put(m, u, y, v, Pal.AIR);
                        continue;
                    }
                    boolean zocalo = y <= PISO, cornisa = y == MURO_ALTO || y == PISO + 9 && fuera;
                    put(m, u, y, v, zocalo || cornisa ? oscura(u, y, v) : piedra(u, y, v));
                }
                // los arcos de las puertas: el escalon del reves a cada lado de la punta
                if (arriba >= 0 && (u != 0 && v != 0) && (Math.abs(u) == 1 || Math.abs(v) == 1)) {
                    Direction f = Math.abs(u) == 1 && v > 0 ? (u < 0 ? Direction.WEST : Direction.EAST)
                            : (v < 0 ? Direction.NORTH : Direction.SOUTH);
                    put(m, u, arriba + 1, v, Academia.escalera(Blocks.DEEPSLATE_BRICK_STAIRS, f, true));
                }
            }
        // las ocho lancetas moradas (dos de hondo), con su dintel
        for (double a : LANCETAS) {
            for (int u = -17; u <= 17; u++)
                for (int v = -17; v <= 17; v++) {
                    if (!muro(u, v) || aparte(u, v, a) > 0.55) continue;
                    for (int y = PISO + 2; y <= PISO + 8; y++) une(m, u, y, v, Academia.VIDRIERA);
                    put(m, u, PISO + 9, v, b(Blocks.CHISELED_DEEPSLATE));
                }
            lancetas++;
        }
        // la reja de la celda (bajada mientras viva la Rectora: la quita paz)
        for (BlockPos p : celdasReja()) m.une(p.getX(), p.getY(), p.getZ(), Pal.IRON_BARS);
        // los faroles de almas colgados de la cupula, sobre cada lanceta
        for (double a : LANCETAS) {
            double ra = Math.toRadians(a);
            int u = (int) Math.round(Math.cos(ra) * 12.6), v = (int) Math.round(Math.sin(ra) * 12.6);
            int techo = (int) Math.ceil(MURO_ALTO + Math.sqrt(Math.max(0, CUP_R0 * CUP_R0 - d(u, v) * d(u, v))));
            for (int y = MURO_ALTO + 1; y < techo; y++) put(m, u, y, v, cadena(Direction.Axis.Y));
            put(m, u, MURO_ALTO, v, farolColgado());
            faroles++;
        }
    }

    // ------------------------------------------------------------------ la cupula de cobre y su rendija

    private void cupula(Pilotes.Lista m) {
        for (int u = -17; u <= 17; u++)
            for (int v = -17; v <= 17; v++) {
                double dd = d(u, v);
                if (dd > CUP_R1) continue;
                int y0 = (int) Math.ceil(MURO_ALTO + Math.sqrt(Math.max(0, CUP_R0 * CUP_R0 - dd * dd)));
                int y1 = (int) Math.floor(MURO_ALTO + Math.sqrt(CUP_R1 * CUP_R1 - dd * dd));
                y0 = Math.max(y0, MURO_ALTO + 1);
                boolean rendija = Math.abs(u) <= 1 && v >= RENDIJA_V0 && v <= RENDIJA_V1;
                boolean marco = Math.abs(u) == 2 && v >= RENDIJA_V0 && v <= RENDIJA_V1;
                double a = angulo(u, v) % 45.0;
                boolean nervio = dd > 1.5 && (Math.min(a, 45.0 - a) * Math.PI / 180.0 * dd < 0.6);
                for (int y = y0; y <= Math.max(y0, y1); y++) {
                    if (rendija) {
                        put(m, u, y, v, Pal.AIR);
                        continue;
                    }
                    put(m, u, y, v, marco ? b(Blocks.POLISHED_DEEPSLATE) : nervio ? b(Blocks.DEEPSLATE_BRICKS) : cobre(u, y, v));
                }
            }
        // el remate del cenit, al sur de la rendija: una aguja de luz
        int cima = (int) Math.floor(MURO_ALTO + CUP_R1);
        put(m, 0, cima + 1, 1, b(Blocks.POLISHED_DEEPSLATE));
        put(m, 0, cima + 2, 1, varilla(Direction.UP));
    }

    // ------------------------------------------------------------------ el estrado y el gran telescopio

    private void estrado(Pilotes.Lista m) {
        for (int u = -14; u <= 14; u++)
            for (int v = -15; v <= ESTRADO_V; v++) {
                if (d(u, v) >= R_DENTRO - 0.6) continue;
                if (v == ESTRADO_V) put(m, u, PISO, v, Academia.escalera(Blocks.POLISHED_DEEPSLATE_STAIRS, Direction.NORTH, false));
                else put(m, u, PISO, v, (u + v & 1) == 0 ? b(Blocks.POLISHED_DEEPSLATE) : Pal.DS_TILES);
            }
        // velas moradas en el borde del estrado, a los lados
        for (int s = -1; s <= 1; s += 2) {
            put(m, s * 6, PISO + 1, ESTRADO_V - 1, Academia.vela(3, true));
            put(m, s * 9, PISO + 1, ESTRADO_V - 2, Academia.vela(2, true));
        }
        // un atril con las cartas del cielo junto a la Rectora
        put(m, 4, PISO + 1, -11, Academia.atril(Direction.SOUTH));
    }

    /** El centro del tubo en la altura y (v). */
    static double tuboV(int y) {
        double t = (y - TUBO_Y0) / (double) (TUBO_Y1 - TUBO_Y0);
        return TUBO_V0 + (TUBO_V1 - TUBO_V0) * t;
    }

    private void telescopio(Pilotes.Lista m) {
        // la montura: la basa en el estrado, la columna y la horquilla de cobre que sujeta el tubo
        for (int du = -1; du <= 1; du++)
            for (int dv = -1; dv <= 1; dv++) put(m, du, PISO + 1, -14 + dv, b(Blocks.POLISHED_BLACKSTONE_BRICKS));
        put(m, 0, PISO + 2, -14, b(Blocks.CHISELED_POLISHED_BLACKSTONE));
        for (int y = PISO + 3; y <= PISO + 9; y++) une(m, 0, y, -14, b(Blocks.POLISHED_BLACKSTONE_WALL));
        for (int s = -1; s <= 1; s += 2)
            for (int y = PISO + 8; y <= PISO + 11; y++) put(m, s * 2, y, -13, b(Blocks.WAXED_CUT_COPPER));
        put(m, 0, PISO + 10, -14, b(Blocks.WAXED_CUT_COPPER));
        // el tubo: de abajo arriba, en rodajas; aros de pizarra cada cuatro; arriba, el objetivo (cristal azul)
        for (int y = TUBO_Y0; y <= TUBO_Y1; y++) {
            double vc = tuboV(y);
            for (int u = -2; u <= 2; u++)
                for (int v = (int) Math.floor(vc) - 2; v <= (int) Math.ceil(vc) + 2; v++) {
                    double r = Math.hypot(u, v - vc);
                    if (r > 1.55) continue;
                    BlockState s;
                    if (y == TUBO_Y1) s = b(Blocks.LIGHT_BLUE_STAINED_GLASS);
                    else if ((y - TUBO_Y0) % 4 == 3) s = b(Blocks.POLISHED_DEEPSLATE);
                    else s = (y - TUBO_Y0) < 6 ? b(Blocks.WAXED_EXPOSED_CUT_COPPER) : b(Blocks.WAXED_CUT_COPPER);
                    put(m, u, y, v, s);
                }
        }
        // el ocular: asoma hacia la Rectora, sobre su cabeza
        put(m, 0, TUBO_Y0 - 1, (int) Math.round(TUBO_V0) + 1, b(Blocks.LIGHTNING_ROD).setValue(DirectionalBlock.FACING, Direction.DOWN));
    }

    // ------------------------------------------------------------------ el planetario colgado de la cupula

    private void planetario(Pilotes.Lista m) {
        int cenit = (int) Math.ceil(MURO_ALTO + CUP_R0) - 1;
        int sol = PISO + 16;
        // la cadena del cenit hasta el brazo mas bajo, el sol en medio (con sus rayos)
        for (int y = PISO + 12; y <= cenit; y++) put(m, 0, y, 0, y == sol ? b(Blocks.SHROOMLIGHT) : cadena(Direction.Axis.Y));
        put(m, 1, sol, 0, varilla(Direction.EAST));
        put(m, -1, sol, 0, varilla(Direction.WEST));
        put(m, 0, sol, 1, varilla(Direction.SOUTH));
        put(m, 0, sol, -1, varilla(Direction.NORTH));
        // los brazos: hacia el este, el sur, el oeste y el norte, cada uno mas bajo, con su planeta en la punta
        Object[][] brazos = {
                {1, 0, 4, PISO + 15, b(Blocks.AMETHYST_BLOCK)},
                {0, 1, 6, PISO + 14, b(Blocks.LAPIS_BLOCK)},
                {-1, 0, 8, PISO + 13, b(Blocks.MAGMA_BLOCK)},
                {0, -1, 5, PISO + 12, b(Blocks.RAW_COPPER_BLOCK)},
        };
        for (Object[] br : brazos) {
            int du = (int) br[0], dv = (int) br[1], largo = (int) br[2], y = (int) br[3];
            Direction.Axis eje = du != 0 ? Direction.Axis.X : Direction.Axis.Z;
            for (int i = 1; i < largo; i++) put(m, du * i, y, dv * i, cadena(eje));
            put(m, du * largo, y, dv * largo, (BlockState) br[4]);
            planetas++;
        }
        // la luna de la tierra: una perla de luz a su lado
        put(m, 1, PISO + 14, 6, varilla(Direction.EAST));
    }

    // ------------------------------------------------------------------ las hornacinas de los maestros

    private void hornacinas(Pilotes.Lista m) {
        for (double a : HORNACINAS) {
            int mu = 0, mv = 0;
            double mejor = Double.MAX_VALUE;
            for (int u = -16; u <= 16; u++)
                for (int v = -16; v <= 16; v++) {
                    double dd = d(u, v);
                    if (dd < R_DENTRO || dd > R_DENTRO + 1.0 || aparte(u, v, a) > 1.3) continue;
                    for (int y = PISO; y <= PISO + 3; y++) put(m, u, y, v, Pal.AIR);
                    put(m, u, PISO + 4, v, Academia.oscura(x(u), PISO + 4, z(v)));
                    if (aparte(u, v, a) < mejor - 1e-6) {
                        mejor = aparte(u, v, a);
                        mu = u;
                        mv = v;
                    }
                }
            // el atril en el fondo de la hornacina (mirando al centro) y el farol colgado del dintel
            Direction f = Math.abs(mu) >= Math.abs(mv) ? (mu > 0 ? Direction.WEST : Direction.EAST) : (mv > 0 ? Direction.NORTH : Direction.SOUTH);
            put(m, mu, PISO, mv, Academia.atril(f));
            put(m, mu, PISO + 3, mv, farolColgado());
        }
    }

    // ------------------------------------------------------------------ el vestibulo, la celda y la sala de los instrumentos

    /** El vestibulo: muros de caliza, tejado a dos aguas, estantes, atriles, la alfombra y la puerta de fuera. */
    private void vestibulo(Pilotes.Lista m) {
        for (int u = -VES_U; u <= VES_U; u++)
            for (int v = 12; v <= VES_V1; v++) {
                if (muro(u, v) || d(u, v) < R_DENTRO) continue;
                boolean lado = Math.abs(u) == VES_U, frente = v == VES_V1;
                if (lado || frente) {
                    for (int y = SUELO; y <= VES_ALTO; y++) {
                        boolean hueco = frente && Math.abs(u) <= PUERTA_U && y >= PISO && y < PISO + PUERTA_ALTO
                                || frente && u == 0 && y == PISO + PUERTA_ALTO;
                        put(m, u, y, v, hueco ? Pal.AIR : y <= PISO || y == VES_ALTO ? oscura(u, y, v) : piedra(u, y, v));
                    }
                } else for (int y = PISO; y <= VES_ALTO; y++) put(m, u, y, v, Pal.AIR);
            }
        // el arco de la puerta de fuera
        for (int s = -1; s <= 1; s += 2)
            put(m, s, PISO + PUERTA_ALTO, VES_V1, Academia.escalera(Blocks.DEEPSLATE_BRICK_STAIRS, s < 0 ? Direction.WEST : Direction.EAST, true));
        // el tejado a dos aguas (la cumbrera de norte a sur), sin meterse en la rotonda
        for (int u = -VES_U - 1; u <= VES_U + 1; u++) {
            int h = VES_ALTO + 1 + Math.min(u + VES_U + 1, VES_U + 1 - u) / 2;
            for (int v = 14; v <= VES_V1 + 1; v++) {
                if (d(u, v) <= R_FUERA + 0.2) continue;
                BlockState s = u == 0 ? Academia.losa(Blocks.DEEPSLATE_TILE_SLAB, SlabType.DOUBLE)
                        : Academia.escalera(Blocks.DEEPSLATE_TILE_STAIRS, u < 0 ? Direction.EAST : Direction.WEST, false);
                une(m, u, h, v, s);
                for (int y = VES_ALTO + 1; y < h; y++) if (v == VES_V1 && Math.abs(u) <= VES_U) put(m, u, y, v, piedra(u, y, v));
            }
        }
        // dentro: estantes contra los muros, dos atriles, la alfombra morada, faroles y estandartes
        for (int s = -1; s <= 1; s += 2) {
            for (int v = 18; v <= 22; v++)
                for (int y = PISO; y <= PISO + 1; y++) {
                    put(m, s * (VES_U - 1), y, v, Pal.BOOKSHELF);
                    estantes++;
                }
            put(m, s * (VES_U - 1), PISO + 3, 20, Academia.pendon(s < 0 ? Direction.EAST : Direction.WEST));
            put(m, s * 3, PISO, 23, Academia.atril(s < 0 ? Direction.EAST : Direction.WEST));
            put(m, s * (VES_U - 1), PISO, 24, Academia.vela(3, true));
        }
        for (int v = 16; v <= VES_V1; v++) put(m, 0, PISO, v, b(Blocks.PURPLE_CARPET));
        put(m, 0, VES_ALTO - 1, 20, farolColgado());
        for (int y = VES_ALTO; y <= VES_ALTO + 3; y++) put(m, 0, y, 20, Pal.CHAIN);
        // fuera, sobre la puerta: la estrella de la academia (una varilla de luz) y dos pendones
        put(m, 0, PISO + PUERTA_ALTO + 2, VES_V1 + 1, varilla(Direction.SOUTH));
        for (int s = -1; s <= 1; s += 2) put(m, s * 3, PISO + 3, VES_V1 + 1, Academia.pendon(Direction.SOUTH));
    }

    /** Los muros de una de las salas de los lados (la celda al este, s = 1; los instrumentos al oeste, s = -1) y su azotea. */
    private void sala(Pilotes.Lista m, int s) {
        for (int k = 14; k <= SALA_U1; k++)
            for (int v = -SALA_V; v <= SALA_V; v++) {
                int u = s * k;
                if (d(u, v) <= R_FUERA) continue;
                boolean muroSala = k == SALA_U1 || Math.abs(v) == SALA_V;
                for (int y = SUELO; y <= SALA_TECHO + 1; y++) {
                    BlockState st;
                    if (y == SALA_TECHO) st = Pal.DS_TILES;
                    else if (y == SALA_TECHO + 1) st = muroSala && (k + v & 1) == 0 ? piedra(u, y, v) : muroSala ? Academia.losa(Blocks.DEEPSLATE_BRICK_SLAB,
                            SlabType.BOTTOM) : Pal.AIR;
                    else if (muroSala) st = y <= PISO ? oscura(u, y, v) : piedra(u, y, v);
                    else st = y == SUELO ? null : Pal.AIR;
                    if (st != null) put(m, u, y, v, st);
                }
            }
        // las ventanas (moradas) del muro del fondo y de los lados
        for (int y = PISO + 1; y <= PISO + 3; y++) {
            for (int v : new int[]{-3, 3}) une(m, s * SALA_U1, y, v, Academia.VIDRIERA);
            for (int k : new int[]{20, 23}) {
                une(m, s * k, y, -SALA_V, Academia.VIDRIERA);
                une(m, s * k, y, SALA_V, Academia.VIDRIERA);
            }
        }
        // el paso entre la rotonda y la sala (lo que queda entre el muro redondo y la sala)
        for (int k = 14; k <= 18; k++)
            for (int v = -1; v <= 1; v++)
                if (d(s * k, v) > R_FUERA)
                    for (int y = PISO; y < PISO + PUERTA_ALTO; y++) put(m, s * k, y, v, Pal.AIR);
        put(m, s * 20, SALA_TECHO - 1, 0, farolColgado());
    }

    /**
     * La celda de la Rectora (al este, tras la reja): su mesa con las velas y las cartas, la silla, el globo celeste, los
     * estantes del fondo, el arcon y el atril con sus ultimas observaciones (libro perdido 25).
     */
    private void celda(Pilotes.Lista m) {
        sala(m, 1);
        for (int v = -5; v <= 5; v++) {
            if (Math.abs(v) == 3) continue;
            for (int y = PISO; y <= PISO + 2; y++) {
                put(m, SALA_U1 - 1, y, v, Pal.BOOKSHELF);
                estantes++;
            }
        }
        // la mesa (losas altas de roble oscuro) junto al muro del norte, la silla y lo que hay encima
        for (int u = 19; u <= 22; u++) put(m, u, PISO, -4, Academia.losa(Blocks.DARK_OAK_SLAB, SlabType.TOP));
        put(m, 20, PISO, -3, Academia.escalera(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, false));
        put(m, 19, PISO + 1, -4, Academia.vela(3, true));
        put(m, 22, PISO + 1, -4, Academia.vela(2, true));
        put(m, 21, PISO + 1, -4, b(Blocks.AMETHYST_CLUSTER));
        // las cartas del cielo en el muro del norte (estandartes azules)
        for (int u = 19; u <= 22; u++) put(m, u, PISO + 3, -5, b(Blocks.BLUE_WALL_BANNER).setValue(WallBannerBlock.FACING, Direction.SOUTH));
        // el globo celeste: un pie de pizarra y la esfera de cristal azul con el oro dentro
        globo(m, 20, 3, false);
        // un tapiz morado en el suelo
        for (int u = 19; u <= 23; u++)
            for (int v = -1; v <= 1; v++) put(m, u, PISO, v, b(Blocks.PURPLE_CARPET));
    }

    /** Un globo celeste: el pie de pizarra labrada y la esfera de cristal azul con el oro dentro (grande: 3 x 3 x 3 sin esquinas). */
    private void globo(Pilotes.Lista m, int u, int v, boolean grande) {
        put(m, u, PISO, v, b(Blocks.CHISELED_DEEPSLATE));
        if (!grande) {
            put(m, u, PISO + 1, v, b(Blocks.LIGHT_BLUE_STAINED_GLASS));
            return;
        }
        put(m, u, PISO + 1, v, b(Blocks.POLISHED_DEEPSLATE_WALL));
        for (int du = -1; du <= 1; du++)
            for (int dv = -1; dv <= 1; dv++)
                for (int dy = 0; dy <= 2; dy++) {
                    int n = Math.abs(du) + Math.abs(dv) + Math.abs(dy - 1);
                    if (n == 3) continue;
                    put(m, u + du, PISO + 2 + dy, v + dv, n == 0 ? b(Blocks.GOLD_BLOCK) : b(Blocks.LIGHT_BLUE_STAINED_GLASS));
                }
    }

    /**
     * La sala de los instrumentos (al oeste, abierta a la rotonda): el globo celeste grande en medio, las mesas con los
     * instrumentos (astrolabios de cobre, varillas, cristales), estantes y la escala a la azotea, con su anteojo.
     */
    private void instrumentos(Pilotes.Lista m) {
        sala(m, -1);
        globo(m, -21, 2, true);
        // las mesas de los instrumentos contra los muros del norte y del sur
        for (int u = -24; u <= -19; u++) {
            put(m, u, PISO, -5, Academia.losa(Blocks.DARK_OAK_SLAB, SlabType.TOP));
            put(m, u, PISO, 5, Academia.losa(Blocks.DARK_OAK_SLAB, SlabType.TOP));
        }
        put(m, -24, PISO + 1, -5, b(Blocks.LIGHTNING_ROD).setValue(DirectionalBlock.FACING, Direction.UP));
        put(m, -22, PISO + 1, -5, b(Blocks.AMETHYST_CLUSTER));
        put(m, -20, PISO + 1, -5, Academia.vela(2, true));
        put(m, -23, PISO + 1, 5, b(Blocks.BREWING_STAND));
        put(m, -21, PISO + 1, 5, varilla(Direction.UP));
        put(m, -19, PISO + 1, 5, Academia.vela(3, true));
        for (int v = -2; v <= 4; v++)
            if (v != 0) for (int y = PISO; y <= PISO + 2; y++) {
                put(m, -(SALA_U1 - 1), y, v, Pal.BOOKSHELF);
                estantes++;
            }
        // la escala a la azotea (pegada al muro del fondo, en la esquina del norte) y su hueco en el techo
        for (int y = PISO; y <= SALA_TECHO; y++) put(m, -(SALA_U1 - 1), y, -4, Academia.escala(Direction.EAST));
        // en la azotea: el anteojo (sobre su tripode) y dos faroles
        put(m, -21, SALA_TECHO + 1, -2, b(Blocks.POLISHED_BLACKSTONE_WALL));
        put(m, -21, SALA_TECHO + 2, -2, b(Blocks.WAXED_CUT_COPPER));
        put(m, -21, SALA_TECHO + 2, -3, b(Blocks.LIGHTNING_ROD).setValue(DirectionalBlock.FACING, Direction.NORTH));
        put(m, -24, SALA_TECHO + 1, 4, Pal.SOUL_LANTERN);
        put(m, -18, SALA_TECHO + 1, 4, Pal.SOUL_LANTERN);
    }

    // ------------------------------------------------------------------ lo que lleva datos: el arcon y el atril

    private void trozos() {
        trozo(24, PISO, -3, c -> c.lectern(x(24), PISO, z(-3), Direction.WEST, Perdidos.libro(25)));
        trozo(24, PISO, 3, c -> c.chest(x(24), PISO, z(3), Direction.WEST, BOTIN));
        trozo(-24, PISO, 3, c -> c.barrel(x(-24), PISO, z(3), Direction.UP, Academia.BOTIN));
    }

    private void trozo(int u, int y, int v, Trozo.Obra o) {
        elementos.add(new Trozo(6, x(u), y, z(v), x(u), y, z(v), o));
    }

    // ------------------------------------------------------------------ los que esperan

    private static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;

    private void en(int variante, int u, int y, int v, float giro, int postura, int ronda, String grupo) {
        esperas.add(new Espera(variante, x(u), y, z(v), giro, postura, ronda, grupo));
    }

    /**
     * La Rectora (la primera: en el estrado, bajo el ocular, leyendo el cielo), dos custodios leyendo a la puerta del
     * vestibulo, un maestro de ronda entre los instrumentos y dos aprendices en el camino (uno leyendo junto a un farol y otro
     * caido).
     */
    private void ponerEsperas() {
        en(0, 0, PISO + 1, RECTORA_V, NORTE, Enemigo.REZA, 0, Puestos.JEFE);
        en(Erudito.CUSTODIO, -2, PISO, VES_V1 + 3, SUR, Enemigo.REZA, 0, "porche_observatorio");
        en(Erudito.CUSTODIO, 2, PISO, VES_V1 + 3, SUR, Enemigo.REZA, 0, "porche_observatorio");
        en(Erudito.MAESTRO, -20, PISO, -2, ESTE, Enemigo.DE_PIE, 2, "instrumentos");
        en(Erudito.APRENDIZ, 4, pie(x(4), z(41)), 41, SUR, Enemigo.REZA, 0, "camino_observatorio");
        en(Erudito.APRENDIZ, -3, pie(x(-3), z(37)), 37, OESTE, Enemigo.TUMBADO, 0, "camino_observatorio");
    }

    // ------------------------------------------------------------------ el sello de la puerta, la reja y la paz

    /** Si la Rectora ha caido (el cielo ya no la lee). */
    public static boolean enPaz(net.minecraft.server.MinecraftServer srv) {
        return Puestos.muerto(srv, MorvainEnemigos.OBSERVATORIO, MorvainEnemigos.rectora());
    }

    /** Un reloj propio (las llamadas a {@link #tick}: asi va igual en las pruebas, que llaman muchas veces en un tick del mundo). */
    private static long reloj;
    /** Para las pruebas: las veces que se ha cerrado y abierto el sello. */
    public static int SELLADOS, ABIERTOS;
    /** Lo que tarda en romperse el sello desde que cae la Rectora (dice su ultima frase). */
    static final int ABRIR_TRAS = 60;
    private static Rectora vista;

    /** Las celdas del sello: la puerta del sur, por la cara de dentro del muro. */
    static List<BlockPos> celdasSello() {
        List<BlockPos> l = new ArrayList<>();
        for (int u = -PUERTA_U; u <= PUERTA_U; u++)
            for (int y = PISO; y < PISO + PUERTA_ALTO; y++) l.add(new BlockPos(x(u), y, z(SELLO_V)));
        l.add(new BlockPos(x(0), PISO + PUERTA_ALTO, z(SELLO_V)));
        return l;
    }

    /** El bloque del sello: un muro de luz (cristal morado). */
    static BlockState sello() {
        return b(Blocks.PURPLE_STAINED_GLASS);
    }

    /** Si el sello deberia estar cerrado ahora: la Rectora peleando, o recien caida (aun habla). */
    static boolean cerrar(ServerLevel m) {
        Enemigo e = Puestos.enemigo(MorvainEnemigos.OBSERVATORIO, MorvainEnemigos.rectora());
        if (e instanceof Rectora r && !r.isRemoved()) vista = r;
        Rectora r = vista;
        if (r == null || r.isRemoved() || r.level() != m) {
            vista = null;
            return false;
        }
        if (r.isAlive()) return r.enPelea();
        return r.deathTime < ABRIR_TRAS;
    }

    /** Si el sello esta cerrado en el mundo (mira la celda de en medio). */
    public static boolean sellado(ServerLevel m) {
        return m.getBlockState(new BlockPos(x(0), PISO + 1, z(SELLO_V))).is(Blocks.PURPLE_STAINED_GLASS);
    }

    /**
     * Cada tick con alguien cerca (lo llama {@link Morvain#onTick}): el sello se cierra de golpe en cuanto empieza la pelea (y
     * antes saca hacia dentro a quien este en la puerta) y se rompe al acabar; y cada segundo, la reja de la celda y la paz.
     * Lo que esta cerrado se mira en el mundo: si el servidor se para a media pelea, al volver se abre solo.
     */
    public static void tick(ServerLevel m) {
        BlockPos centro = new BlockPos(x(0), PISO, z(SELLO_V));
        if (!m.isLoaded(centro)) return;
        long ahora = ++reloj;
        boolean quiere = cerrar(m), hay = sellado(m);
        if (quiere && !hay) {
            sacarDeLaPuerta(m);
            for (BlockPos p : celdasSello()) {
                BlockState s = m.getBlockState(p);
                if (s.isAir() || s.canBeReplaced()) m.setBlock(p, sello(), 3);
            }
            sonar(m, DarkCraftMod.SFX_OBSERVATORIO_SELLO.get(), 1.8f, 1.0f);
            m.sendParticles(ParticleTypes.END_ROD, x(0) + 0.5, PISO + 2, z(SELLO_V) + 0.5, 40, 1.2, 1.4, 0.2, 0.05);
            m.sendParticles(ParticleTypes.WITCH, x(0) + 0.5, PISO + 2, z(SELLO_V) + 0.5, 30, 1.2, 1.4, 0.2, 0.05);
            SELLADOS++;
            DarkCraftMod.LOGGER.info("[DarkCraft] El sello del Observatorio se cierra");
        } else if (!quiere && hay) abrir(m, true);
        // mientras esta cerrado, la luz del sello tiembla
        if (hay && ahora % 5 == 0) m.sendParticles(ParticleTypes.END_ROD, x(0) + 0.5, PISO + 1.5 + m.random.nextDouble() * 2.5, z(SELLO_V) + 0.5, 2, 1.0, 0.4, 0.1,
                0.01);
        if (ahora % 20 == 5) paz(m);
    }

    /** El sello se rompe: quita su luz de la puerta (con su sonido, si {@code sonido}). */
    public static void abrir(ServerLevel m, boolean sonido) {
        int n = 0;
        for (BlockPos p : celdasSello())
            if (m.getBlockState(p).is(Blocks.PURPLE_STAINED_GLASS)) {
                m.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                n++;
            }
        if (n == 0) return;
        ABIERTOS++;
        if (sonido) {
            sonar(m, DarkCraftMod.SFX_OBSERVATORIO_ABRE.get(), 1.6f, 1.0f);
            m.sendParticles(ParticleTypes.END_ROD, x(0) + 0.5, PISO + 2, z(SELLO_V) + 0.5, 50, 1.2, 1.4, 0.3, 0.12);
        }
        DarkCraftMod.LOGGER.info("[DarkCraft] El sello del Observatorio se rompe");
    }

    /** Antes de cerrarse: quien este en el hueco de la puerta pasa al lado de dentro (a la rotonda). */
    private static void sacarDeLaPuerta(ServerLevel m) {
        AABB caja = new AABB(x(-PUERTA_U) - 0.3, PISO - 0.5, z(SELLO_V) - 0.2, x(PUERTA_U) + 1.3, PISO + PUERTA_ALTO + 1, z(SELLO_V + 1) + 1.0);
        for (Entity e : m.getEntities((Entity) null, caja, q -> q instanceof net.minecraft.world.entity.LivingEntity && q.isAlive()))
            e.teleportTo(e.getX(), e.getY(), z(SELLO_V) - 1.6);
    }

    private static void sonar(ServerLevel m, net.minecraft.sounds.SoundEvent ev, float vol, float tono) {
        for (ServerPlayer p : m.players())
            if (p.distanceToSqr(x(0), PISO, z(SELLO_V)) < 80 * 80)
                p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(net.minecraft.core.Holder.direct(ev), SoundSource.BLOCKS,
                        x(0) + 0.5, PISO + 2, z(SELLO_V) + 0.5, vol, tono, m.random.nextLong()));
    }

    /** Para las pruebas: los eruditos que se han deshecho al quedar el cielo en paz. */
    public static int DESCANSAN;

    /**
     * Cada segundo: con la Rectora caida, la reja de su celda arriba (con ella viva, bajada) y los eruditos que seguian
     * leyendo (en la academia y a la puerta del observatorio; no los que pelean) se deshacen en letras de luz.
     */
    public static void paz(ServerLevel m) {
        boolean paz = enPaz(m.getServer());
        BlockPos reja = new BlockPos(x(REJA_U), PISO, z(0));
        if (m.isLoaded(reja)) {
            boolean hay = m.getBlockState(reja).is(Blocks.IRON_BARS);
            if (paz && hay) reja(m, false);
            else if (!paz && !hay) reja(m, true);
        }
        if (!paz) return;
        descansar(m, MorvainEnemigos.ACADEMIA, MorvainEnemigos.academia());
        descansar(m, MorvainEnemigos.OBSERVATORIO, MorvainEnemigos.observatorio());
    }

    private static void descansar(ServerLevel m, String sitio, List<Puestos.Puesto> puestos) {
        if (Puestos.sitio(sitio) == null) return;
        for (int i = 0; i < puestos.size(); i++) {
            Puestos.Puesto p = puestos.get(i);
            if (p.postura() != Enemigo.REZA || Puestos.JEFE.equals(p.grupo())) continue;
            Enemigo e = Puestos.enemigo(sitio, i);
            if (e == null || e.level() != m || e.getTarget() != null || e.accion() != 0) continue;
            m.sendParticles(ParticleTypes.ENCHANT, e.getX(), e.getY() + 1.4, e.getZ(), 40, 0.4, 0.8, 0.4, 0.8);
            m.sendParticles(Rectora.LUZ, e.getX(), e.getY() + 1.2, e.getZ(), 20, 0.4, 0.7, 0.4, 0.0);
            m.sendParticles(ParticleTypes.END_ROD, e.getX(), e.getY() + 1.0, e.getZ(), 8, 0.3, 0.6, 0.3, 0.03);
            e.disolver();
            DESCANSAN++;
        }
    }

    /** Las celdas de la reja de la celda: el hueco de su puerta en el muro (con la punta del arco). */
    static List<BlockPos> celdasReja() {
        List<BlockPos> l = new ArrayList<>();
        for (int v = -1; v <= 1; v++)
            for (int y = PISO; y < PISO + PUERTA_ALTO; y++) l.add(new BlockPos(x(REJA_U), y, z(v)));
        l.add(new BlockPos(x(REJA_U), PISO + PUERTA_ALTO, z(0)));
        return l;
    }

    /** La reja de la celda: bajada (con los barrotes unidos entre si) o quitada. */
    public static void reja(ServerLevel m, boolean bajar) {
        List<BlockPos> l = celdasReja();
        if (bajar) {
            for (BlockPos p : l) if (m.getBlockState(p).isAir()) m.setBlock(p, Blocks.IRON_BARS.defaultBlockState(), 3);
            for (BlockPos p : l) {
                BlockState s = m.getBlockState(p);
                if (s.is(Blocks.IRON_BARS)) m.setBlock(p, Block.updateFromNeighbourShapes(s, m, p), 2);
            }
        } else {
            for (BlockPos p : l) if (m.getBlockState(p).is(Blocks.IRON_BARS)) m.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            m.playSound(null, x(REJA_U) + 0.5, PISO + 1, z(0) + 0.5, net.minecraft.sounds.SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 1.0f, 0.7f);
            m.sendParticles(ParticleTypes.END_ROD, x(REJA_U) + 0.5, PISO + 1.5, z(0) + 0.5, 20, 0.2, 1.0, 1.2, 0.02);
        }
    }

    /** Para los comandos y las pruebas: la reja de la celda bajada otra vez y el sello abierto. */
    public static void deshacerPaz(ServerLevel m) {
        if (m.isLoaded(new BlockPos(x(REJA_U), PISO, z(0)))) reja(m, true);
        abrir(m, false);
    }

    // ------------------------------------------------------------------ para la prueba

    /** Lo que hay en el observatorio ya generado (para la prueba): el atril con el libro 25, el arcon, la reja, el sello, la cupula. */
    public static String resumen(ServerLevel m) {
        Observatorio o = get();
        int[] at = o.sitio("atril"), ar = o.sitio("arcon");
        BlockPos pa = new BlockPos(at[0] + 1, at[1], at[2]);
        boolean libro = m.getBlockEntity(pa) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le && Perdidos.de(le.getBook()) == 25;
        boolean arcon = m.getBlockState(new BlockPos(ar[0] + 1, ar[1], ar[2])).is(Blocks.CHEST);
        int reja = 0;
        for (BlockPos p : celdasReja()) if (m.getBlockState(p).is(Blocks.IRON_BARS)) reja++;
        int sello = 0;
        for (BlockPos p : celdasSello()) if (m.getBlockState(p).is(Blocks.PURPLE_STAINED_GLASS)) sello++;
        int cobre = 0, rendija = 0;
        for (int v = -16; v <= 16; v++) {
            int y = (int) Math.floor(MURO_ALTO + Math.sqrt(Math.max(0, CUP_R1 * CUP_R1 - v * v))) - 1;
            BlockState s = m.getBlockState(new BlockPos(x(3), y, z(v)));
            if (s.is(Blocks.OXIDIZED_CUT_COPPER) || s.is(Blocks.WAXED_WEATHERED_CUT_COPPER) || s.is(Blocks.OXIDIZED_COPPER)) cobre++;
        }
        for (int v = RENDIJA_V0 + 2; v <= RENDIJA_V1; v++) {
            int y = (int) Math.floor(MURO_ALTO + Math.sqrt(Math.max(0, CUP_R0 * CUP_R0 - v * v))) + 1;
            if (m.getBlockState(new BlockPos(x(0), y, z(v))).isAir()) rendija++;
        }
        boolean sol = m.getBlockState(new BlockPos(x(0), PISO + 16, z(0))).is(Blocks.SHROOMLIGHT);
        boolean objetivo = m.getBlockState(new BlockPos(x(0), TUBO_Y1, z((int) Math.round(tuboV(TUBO_Y1))))).is(Blocks.LIGHT_BLUE_STAINED_GLASS);
        int suelo = 0, muestras = 0;
        for (int u = -12; u <= 12; u += 3)
            for (int v = -6; v <= 12; v += 3) {
                if (d(u, v) > 13) continue;
                muestras++;
                BlockPos p = new BlockPos(x(u), PISO, z(v));
                if (!m.getBlockState(p.below()).isAir() && m.getBlockState(p).isAir() && m.getBlockState(p.above()).isAir()) suelo++;
            }
        return "atril con el libro 25 " + (libro ? "SI" : "NO") + ", arcon " + (arcon ? "SI" : "NO") + ", reja de la celda " + reja + "/"
                + celdasReja().size() + ", sello " + sello + "/" + celdasSello().size() + ", cobre de la cupula " + cobre + ", rendija abierta " + rendija
                + "/" + (RENDIJA_V1 - RENDIJA_V0 - 1) + ", el sol del planetario " + (sol ? "SI" : "NO") + ", el objetivo del telescopio "
                + (objetivo ? "SI" : "NO") + ", suelo de la rotonda libre " + suelo + "/" + muestras;
    }
}
