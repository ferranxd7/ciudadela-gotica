package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Salobre;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
 * El Puerto Varado (parte 7q), en la Costa de los Huesos, junto al santuario del Faro de los Huesos: un puerto de pescadores
 * que la noche del cielo rojo se quedo en seco. El mar se retiro de golpe ("como si alguien se lo bebiera") y no volvio; la
 * bahia es hoy una hondonada de arena, sal y barro con los barcos varados, charcos y el esqueleto de una ballena. La gente
 * bajo a la arena a recoger lo que el mar dejaba y la sal se los fue comiendo (los {@code enemigos/Salobre}: rederos,
 * bicheros y salineros).
 * <p>
 * El <b>pueblo</b> (al oeste del muelle, todo allanado): la entrada bajo el <b>arco de costillas</b> de ballena, la <b>plaza</b>
 * con el ancla del monumento y el pozo, la <b>lonja</b> (la nave abierta donde se vendia el pescado), las casas de los
 * pescadores, los <b>secaderos</b> (las varas con las algas), las <b>salinas</b> (los estanques de sal entre sus caballones), el
 * <b>varadero</b> (la grada que baja a la bahia con un barco a medio hacer) y la <b>casa del farero</b> (el atril con el
 * <b>libro perdido 26</b> y el arcon). El <b>muelle</b>: un muro de sillares que cae a la bahia seca, con sus norays y dos
 * escaleras. La <b>bahia</b>: arena, costras de sal, barro y charcos; cinco barcas varadas o volcadas y la <b>ballena</b> (las
 * costillas en arco, el espinazo por encima y la calavera con sus quijadas medio enterradas). El <b>espigon</b> sale del muelle
 * hacia el este hasta la roca del <b>Faro de los Huesos</b>: una torre de hueso con costillas por fuera, quijadas a la puerta,
 * la escalera de caracol por dentro (tres vueltas), la galeria y la linterna con su fuego frio.
 * <p>
 * Los salobres los apunta el propio plano ({@link #esperas}). Se dibuja chunk a chunk al adornar
 * ({@link MorvainGenerador#applyBiomeDecoration}); en su caja {@link Adorno} no pone nada; la tarima del santuario (que se
 * escribe antes, con el terreno) no se toca.
 */
public final class Varadero {
    public static final long SEMILLA = 0x7ADE_40C0_57A1L;
    /** El eje del puerto (u = x - CX hacia el este, v = z - CZ hacia el sur): el muro del muelle va en u = MUELLE_U. */
    public static final int CX = 5640, CZ = 4740;
    /** El suelo del pueblo (lo que se pisa esta en {@link #PISO}): el de la tarima del santuario. */
    public static final int SUELO = 65, PISO = SUELO + 1;
    /** El fondo de la bahia seca (lo que se pisa esta en FONDO + 1). */
    public static final int FONDO = 57;
    /** El muro del muelle (u = MUELLE_U y MUELLE_U + 1), de v = MUELLE_V0 a MUELLE_V1. */
    public static final int MUELLE_U = 22, MUELLE_V0 = -56, MUELLE_V1 = 52;
    /** El espigon: |v - ESPIGON_V| <= 3 (el pretil en 3), del muelle hasta la roca del faro. */
    public static final int ESPIGON_V = -20, ESPIGON_U1 = 79;
    /** El faro: su centro, el radio de su roca, lo que sube la escalera (tres vueltas) y lo que se pisa en la linterna. */
    public static final int FARO_U = 86, FARO_V = -20, ROCA_R = 8, FARO_ALTO = 36, LINTERNA = PISO + FARO_ALTO;
    /** La grada del varadero: |v - GRADA_V| <= 3, de u = GRADA_U0 (arriba, en el pueblo) a GRADA_U1 (abajo, en el fondo). */
    public static final int GRADA_V = 40, GRADA_U0 = 8, GRADA_U1 = 38;
    /** La ballena: su eje (v), donde acaba la cola y donde la punta del hocico. */
    public static final int BALLENA_V = 30, BALLENA_U0 = 36, BALLENA_U1 = 88;
    /** Lo allanado del pueblo: de u = U_LLANO hasta el muelle, de v = V_LLANO0 a V_LLANO1; y lo que se funde alrededor. */
    static final int U_LLANO = -54, V_LLANO0 = -60, V_LLANO1 = 56;
    static final double FUNDE = 7;
    /** La bahia: media elipse desde el muelle (u = BAHIA_U) hacia el este; hasta BAHIA_HONDO, el fondo llano. */
    static final double BAHIA_U = MUELLE_U + 2, BAHIA_V = -2, BAHIA_A = 100, BAHIA_B = 60, BAHIA_HONDO = 0.78;
    /** El santuario del Faro de los Huesos (su tarima no se toca). */
    static final int SANT_U = 5612 - CX, SANT_V = 4779 - CZ, SANT_R = 11;
    /** Lo que toca. */
    public static final int X0 = CX + U_LLANO - 9, X1 = CX + 128, Z0 = CZ - 70, Z1 = CZ + 66;
    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/puerto"),
            BOTIN_BUENO = new ResourceLocation(DarkCraftMod.MODID, "chests/puerto_bueno");
    private static final int[] LADOS = {-1, 1};
    private static final Ruido RUIDO = new Ruido(SEMILLA);

    private static final int AW = X1 - X0 + 1, AD = Z1 - Z0 + 1;
    private static final int[] ALTO = new int[AW * AD];

    static {
        Arrays.fill(ALTO, Integer.MIN_VALUE);
    }

    /** El bloque de arriba del terreno de Morvain en (x, z) (sin el puerto). */
    public static int alto(int x, int z) {
        if (x < X0 || x > X1 || z < Z0 || z > Z1) return (int) Math.floor(Terreno.columna(x, z).altura());
        int i = (z - Z0) * AW + (x - X0);
        int v = ALTO[i];
        if (v == Integer.MIN_VALUE) ALTO[i] = v = (int) Math.floor(Terreno.columna(x, z).altura());
        return v;
    }

    /** Un salobre del puerto: donde espera (y: lo que pisa), hacia donde mira, su variante, como espera, cuanto ronda y su grupo. */
    public record Espera(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    private static Varadero unico;

    public static synchronized Varadero get() {
        if (unico == null) unico = new Varadero();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    /** Lo que hay (para la prueba y las vistas). */
    public int casas, barcas, costillas, norays, secaderos, estanques, montones, faroles, escalones;

    private Varadero() {
        elementos.add(new Terraza());
        Pilotes.Lista ca = new Pilotes.Lista();
        calles(ca);
        elementos.add(ca.pieza(2));
        Pilotes.Lista mu = new Pilotes.Lista();
        muelle(mu);
        espigon(mu);
        grada(mu);
        elementos.add(mu.pieza(3));
        Pilotes.Lista fa = new Pilotes.Lista();
        roca(fa);
        faro(fa);
        elementos.add(fa.pieza(4));
        Pilotes.Lista pu = new Pilotes.Lista();
        arcoCostillas(pu);
        plaza(pu);
        lonja(pu);
        casas(pu);
        casaFarero(pu);
        secaderos(pu);
        salinas(pu);
        varadero(pu);
        elementos.add(pu.pieza(4));
        Pilotes.Lista ba = new Pilotes.Lista();
        ballena(ba);
        barcas(ba);
        montonesBahia(ba);
        elementos.add(ba.pieza(3));
        trozos();
        // ---- los sitios con nombre
        sitio("entrada", SANT_U + 2, PISO, SANT_V - 8);
        sitio("arco", -26, PISO, 26);
        sitio("plaza", -12, PISO, 5);
        sitio("lonja", -14, PISO, -22);
        sitio("casa_farero", 9, PISO, -24);
        sitio("atril", 9, PISO, -33);
        sitio("arcon", 12, PISO, -34);
        sitio("secadero", 10, PISO, -11);
        sitio("salinas", 4, SUELO, 20);
        sitio("muelle", 20, PISO, -6);
        sitio("varadero", 4, PISO, 34);
        sitio("bahia", 40, FONDO + 1, -4);
        sitio("barcas", 46, FONDO + 1, 4);
        sitio("ballena", 58, FONDO + 1, BALLENA_V);
        sitio("espigon", 50, PISO, ESPIGON_V);
        sitio("faro", FARO_U - 6, PISO, FARO_V);
        sitio("faro_alto", FARO_U - 4, LINTERNA, FARO_V);
        // ---- los salobres
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo del puerto (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en lo del puerto: ahi {@link Adorno} no pone nada. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= X0 && x0 <= X1 && z0 + 15 >= Z0 && z0 <= Z1;
    }

    /** Si (x, z) esta en el puerto (el pueblo, el muelle y la bahia): para el nombre del sitio y su logro. */
    public static boolean dentro(double x, double z) {
        double u = x - CX, v = z - CZ;
        if (u >= U_LLANO - 2 && u < BAHIA_U && v >= V_LLANO0 - 2 && v <= V_LLANO1 + 2) return Math.hypot(u - SANT_U, v - SANT_V) > SANT_R - 3;
        return u >= BAHIA_U && bahia((int) Math.floor(u), (int) Math.floor(v)) < 1.0;
    }

    /** Si (x, z) esta en la roca del faro (o en la torre). */
    public static boolean enFaro(double x, double z) {
        return Math.hypot(x - CX - FARO_U - 0.5, z - CZ - FARO_V - 0.5) <= ROCA_R + 1;
    }

    /** Si alguien en (x, y, z) esta arriba del faro (en la linterna o en su galeria). */
    public static boolean arribaDelFaro(double x, double y, double z) {
        return Math.hypot(x - CX - FARO_U - 0.5, z - CZ - FARO_V - 0.5) <= 6.5 && y >= LINTERNA - 0.5 && y < LINTERNA + 6;
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} del puerto varado en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los salobres del puerto (los apunta {@link MorvainEnemigos}). */
    public List<Espera> esperas() {
        return esperas;
    }

    /** Los sitios con nombre (la plaza, la lonja, el atril, la bahia, el faro...): {x, y, z} (y: lo que se pisa). */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    private void sitio(String nombre, int u, int y, int v) {
        sitios.put(nombre, new int[]{CX + u, y, CZ + v});
    }

    /** Lo que se pisa en (x, z) (el pueblo, la bahia o el terreno). */
    static int pie(int x, int z) {
        return superficie(x, z) + 1;
    }

    /** El bloque de arriba en (x, z) con el puerto: lo allanado, la bahia (y lo que se funde) o el terreno. */
    static int superficie(int x, int z) {
        int o = objetivo(x, z);
        return o != Integer.MIN_VALUE ? o : alto(x, z);
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

    static BlockState tronco(Block k, Direction.Axis a) {
        return k.defaultBlockState().setValue(RotatedPillarBlock.AXIS, a);
    }

    static BlockState hueso(Direction.Axis a) {
        return Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, a);
    }

    static BlockState vela(int n, boolean encendida) {
        return Blocks.WHITE_CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, Math.max(1, Math.min(4, n))).setValue(CandleBlock.LIT, encendida);
    }

    static BlockState barril(Direction d) {
        return Blocks.BARREL.defaultBlockState().setValue(BlockStateProperties.FACING, d);
    }

    static BlockState nieve(int capas) {
        return Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, Math.max(1, Math.min(8, capas)));
    }

    static BlockState trampilla(Block k, Direction f, boolean abierta, boolean arriba) {
        return k.defaultBlockState().setValue(TrapDoorBlock.FACING, f).setValue(TrapDoorBlock.OPEN, abierta)
                .setValue(TrapDoorBlock.HALF, arriba ? Half.TOP : Half.BOTTOM);
    }

    /** El sillar del muelle, del espigon y de los zocalos: piedra gastada por el mar, con algas secas abajo. */
    static BlockState sillar(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 3);
        if (y <= FONDO + 2) return p < 40 ? b(Blocks.MOSSY_STONE_BRICKS) : p < 60 ? b(Blocks.DARK_PRISMARINE) : p < 80 ? b(Blocks.MOSSY_COBBLESTONE) : b(Blocks.STONE_BRICKS);
        if (y <= FONDO + 4) return p < 30 ? b(Blocks.MOSSY_STONE_BRICKS) : p < 45 ? b(Blocks.PRISMARINE_BRICKS) : p < 80 ? b(Blocks.STONE_BRICKS) : b(Blocks.CRACKED_STONE_BRICKS);
        return p < 58 ? b(Blocks.STONE_BRICKS) : p < 74 ? b(Blocks.CRACKED_STONE_BRICKS) : p < 88 ? b(Blocks.POLISHED_ANDESITE) : b(Blocks.ANDESITE);
    }

    /** La roca del faro. */
    static BlockState peña(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 4);
        return p < 36 ? b(Blocks.STONE) : p < 54 ? b(Blocks.ANDESITE) : p < 68 ? b(Blocks.COBBLESTONE) : p < 80 ? b(Blocks.TUFF)
                : p < 92 ? b(Blocks.MOSSY_COBBLESTONE) : b(Blocks.BONE_BLOCK);
    }

    /** El hueso de la torre del faro: hueso con alguna piedra de arena. */
    static BlockState huesoMuro(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 5);
        return p < 78 ? hueso(Direction.Axis.Y) : p < 90 ? b(Blocks.SMOOTH_SANDSTONE) : b(Blocks.CALCITE);
    }

    /** La madera de las casas y las barcas (cada una la suya): picea, roble oscuro o roble. */
    static Block[] madera(int k) {
        return switch (Math.floorMod(k, 3)) {
            case 0 -> new Block[]{Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB, Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_FENCE};
            case 1 -> new Block[]{Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_SLAB, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_FENCE};
            default -> new Block[]{Blocks.OAK_PLANKS, Blocks.OAK_STAIRS, Blocks.OAK_SLAB, Blocks.STRIPPED_OAK_LOG, Blocks.OAK_FENCE};
        };
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

    // ------------------------------------------------------------------ el suelo: el pueblo allanado y la bahia cavada

    /** Lo lejos que esta (u, v) del borde de la bahia: menos de 1, dentro (0 en el muelle, en medio). */
    static double bahia(int u, int v) {
        return Math.hypot((u - BAHIA_U) / BAHIA_A, (v - BAHIA_V) / BAHIA_B) + 0.05 * RUIDO.ruido(u / 17.0, v / 17.0);
    }

    /** Lo alto del suelo en (x, z): el pueblo, la bahia (con su orilla) o MIN_VALUE donde no se toca. */
    static int objetivo(int x, int z) {
        int u = x - CX, v = z - CZ;
        if (u >= BAHIA_U) {
            double e = bahia(u, v);
            if (e >= 1.0) return Integer.MIN_VALUE;
            if (e <= BAHIA_HONDO) return FONDO;
            double t = Ruido.suave(BAHIA_HONDO, 1.0, e);
            return (int) Math.round(Ruido.lerp(t, FONDO, Math.min(alto(x, z), SUELO + 1)));
        }
        double du = Math.max(0, U_LLANO - u), dv = v < V_LLANO0 ? V_LLANO0 - v : v > V_LLANO1 ? v - V_LLANO1 : 0;
        double d = Math.hypot(du, dv);
        if (d <= 0) return SUELO;
        if (d >= FUNDE) return Integer.MIN_VALUE;
        double t = Ruido.suave(0, FUNDE, d);
        return (int) Math.round(Ruido.lerp(t, SUELO, alto(x, z)));
    }

    enum Zona { PUEBLO, BAHIA, ORILLA, FUERA }

    static Zona zona(int u, int v) {
        if (u >= BAHIA_U) {
            double e = bahia(u, v);
            return e <= BAHIA_HONDO ? Zona.BAHIA : e < 1.0 ? Zona.ORILLA : Zona.FUERA;
        }
        return u >= U_LLANO && v >= V_LLANO0 && v <= V_LLANO1 ? Zona.PUEBLO : Zona.FUERA;
    }

    /** El suelo del fondo de la bahia: arena, costras de sal (donde se seco el ultimo agua), barro y charcos. */
    private static BlockState fondo(int u, int v, int p) {
        double n = RUIDO.ruido(u / 11.0 + 40, v / 11.0 - 13);
        if (n > 0.32) return p < 42 ? b(Blocks.CALCITE) : p < 72 ? b(Blocks.WHITE_CONCRETE_POWDER) : p < 92 ? b(Blocks.SAND) : b(Blocks.SMOOTH_QUARTZ);
        if (n < -0.62) return b(Blocks.WATER);
        if (n < -0.42) return p < 48 ? b(Blocks.MUD) : p < 70 ? b(Blocks.CLAY) : p < 90 ? b(Blocks.SAND) : b(Blocks.PACKED_MUD);
        return p < 70 ? b(Blocks.SAND) : p < 76 ? Pal.GRAVEL : p < 84 ? b(Blocks.SANDSTONE) : p < 93 ? b(Blocks.SAND) : p < 96 ? b(Blocks.DEAD_BRAIN_CORAL_BLOCK) : p < 98 ? b(Blocks.DEAD_TUBE_CORAL_BLOCK) : b(Blocks.BONE_BLOCK);
    }

    /** El suelo del pueblo (lo que no pisa una calle ni una casa): arena con tierra, grava y algo de hierba seca. */
    private static BlockState tierra(int p) {
        return p < 72 ? b(Blocks.SAND) : p < 84 ? Pal.COARSE_DIRT : p < 91 ? Pal.GRAVEL : p < 98 ? b(Blocks.SANDSTONE) : Pal.DIRT;
    }

    private static final Block[] CORAL_MUERTO = {Blocks.DEAD_BRAIN_CORAL_FAN, Blocks.DEAD_TUBE_CORAL_FAN, Blocks.DEAD_FIRE_CORAL_FAN,
            Blocks.DEAD_HORN_CORAL_FAN, Blocks.DEAD_BUBBLE_CORAL_FAN, Blocks.DEAD_BRAIN_CORAL, Blocks.DEAD_HORN_CORAL, Blocks.DEAD_TUBE_CORAL};

    /** Lo que hay encima: en la bahia, corales muertos y matas secas; en el pueblo, alguna mata seca. */
    private static BlockState encima(Zona zn, BlockState suelo, int q) {
        if (zn == Zona.BAHIA || zn == Zona.ORILLA) {
            if (suelo.is(Blocks.WATER) || suelo.is(Blocks.MUD)) return null;
            if (q < 2) return b(CORAL_MUERTO[(q * 5 + (suelo.hashCode() & 3)) % CORAL_MUERTO.length]);
            if (q < 3 && zn == Zona.ORILLA) return Pal.b(Blocks.DEAD_BUSH);
            return null;
        }
        return q < 3 ? Pal.b(Blocks.DEAD_BUSH) : null;
    }

    /**
     * La terraza: en el pueblo corta lo que sobra hasta el suelo y rellena por debajo; en la bahia cava hasta el fondo (y la
     * orilla sube hasta el terreno); el suelo de cada zona y lo de encima donde toca. La tarima del santuario no se toca.
     */
    private final class Terraza extends Element {
        Terraza() {
            super(SEMILLA + 51);
            this.layer = 1;
            bounds(X0, 40, Z0, X1, 120, Z1);
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
                    for (int y = t + 1; y < o; y++) c.set(x, y, z, y >= o - 2 ? b(Blocks.SAND) : b(Blocks.SANDSTONE));
                    Zona zn = zona(u, v);
                    int p = Hash.pct(x, o, z, seed);
                    BlockState s = switch (zn) {
                        case BAHIA -> fondo(u, v, p);
                        case ORILLA -> p < 70 ? b(Blocks.SAND) : p < 85 ? b(Blocks.SANDSTONE) : p < 95 ? Pal.GRAVEL : b(Blocks.CALCITE);
                        default -> tierra(p);
                    };
                    if (s.is(Blocks.WATER)) c.set(x, o - 1, z, b(Blocks.CLAY));
                    c.set(x, o, z, s);
                    BlockState e = encima(zn, s, Hash.pct(x, o + 1, z, seed + 1));
                    if (e != null) c.set(x, o + 1, z, e);
                }
        }
    }

    // ------------------------------------------------------------------ las calles

    /** Una calle recta de (u0, v0) a (u1, v1), de {@code ancho} (impar): adoquines, grava y losas a la altura del suelo. */
    private static void calle(Pilotes.Lista m, int u0, int v0, int u1, int v1, int ancho) {
        int n = Math.max(Math.abs(u1 - u0), Math.abs(v1 - v0));
        int r = ancho / 2;
        for (int i = 0; i <= n; i++) {
            double t = n == 0 ? 0 : (double) i / n;
            int uc = (int) Math.round(u0 + (u1 - u0) * t), vc = (int) Math.round(v0 + (v1 - v0) * t);
            for (int du = -r; du <= r; du++)
                for (int dv = -r; dv <= r; dv++) {
                    int u = uc + du, v = vc + dv;
                    if (junto(u, v, SANT_R)) continue;
                    int p = Hash.pct(x(u), 7, z(v), SEMILLA + 11);
                    BlockState s = p < 34 ? b(Blocks.COBBLESTONE) : p < 56 ? Pal.GRAVEL : p < 74 ? b(Blocks.STONE_BRICKS)
                            : p < 86 ? b(Blocks.MOSSY_COBBLESTONE) : p < 94 ? b(Blocks.ANDESITE) : b(Blocks.SAND);
                    put(m, u, SUELO, v, s);
                    put(m, u, PISO, v, Pal.AIR);
                }
        }
    }

    private void calles(Pilotes.Lista m) {
        // del santuario al arco y del arco a la plaza
        calle(m, SANT_U + 2, SANT_V - 10, -26, 28, 3);
        calle(m, -26, 28, -26, 18, 3);
        calle(m, -26, 18, -17, 8, 3);
        // la calle mayor: de la plaza al muelle
        calle(m, -3, 0, 18, 0, 3);
        // a la lonja, a las casas del oeste y a la casa del farero
        calle(m, -12, -9, -12, -17, 3);
        calle(m, -21, 0, -37, 3, 3);
        calle(m, -12, -9, -40, -10, 3);
        calle(m, 2, -10, 9, -25, 3);
        calle(m, 9, -25, 20, -20, 3);
        // al varadero y a las salinas
        calle(m, -6, 8, 4, 30, 3);
        // el paseo del muelle
        for (int v = MUELLE_V0 + 2; v <= MUELLE_V1 - 2; v++)
            for (int u = MUELLE_U - 3; u < MUELLE_U; u++) {
                if (Math.abs(v - GRADA_V) <= 4) continue;
                int p = Hash.pct(x(u), 9, z(v), SEMILLA + 12);
                put(m, u, SUELO, v, p < 55 ? b(Blocks.STONE_BRICKS) : p < 75 ? b(Blocks.CRACKED_STONE_BRICKS) : p < 90 ? b(Blocks.POLISHED_ANDESITE) : Pal.GRAVEL);
                put(m, u, PISO, v, Pal.AIR);
            }
    }

    // ------------------------------------------------------------------ el muelle, el espigon y la grada

    /** Lo que hay al pie del muelle (lo que pisa la bahia junto al muro). */
    private static int alPie(int v) {
        int o = objetivo(x(MUELLE_U + 2), z(v));
        return o == Integer.MIN_VALUE ? FONDO : Math.min(o, SUELO);
    }

    private void muelle(Pilotes.Lista m) {
        for (int v = MUELLE_V0; v <= MUELLE_V1; v++) {
            if (Math.abs(v - GRADA_V) <= 3) continue;   // ahi baja la grada
            int pie = alPie(v);
            for (int u = MUELLE_U; u <= MUELLE_U + 1; u++) {
                for (int y = FONDO - 2; y < SUELO; y++) put(m, u, y, v, sillar(x(u), y, z(v)));
                put(m, u, SUELO, v, u == MUELLE_U + 1 ? b(Blocks.POLISHED_ANDESITE) : b(Blocks.STONE_BRICKS));
                put(m, u, PISO, v, Pal.AIR);
            }
            // el pie del muro: algas secas y conchas (corales muertos), y alguna cadena colgando de su argolla
            if (pie == FONDO && Hash.pct(x(MUELLE_U + 2), 3, z(v), SEMILLA + 21) < 18)
                put(m, MUELLE_U + 2, FONDO + 1, v, b(CORAL_MUERTO[Hash.h(v, 1, 2, SEMILLA) & 7]));
            if (Math.floorMod(v, 9) == 4 && v > MUELLE_V0 + 3 && v < MUELLE_V1 - 3) {
                put(m, MUELLE_U + 2, SUELO - 2, v, Pal.CHAIN);
                put(m, MUELLE_U + 2, SUELO - 3, v, Pal.CHAIN);
            }
            // los norays
            if (Math.floorMod(v, 8) == 2 && Math.abs(v - ESPIGON_V) > 5 && !escaleraMuelle(v)) {
                put(m, MUELLE_U + 1, PISO, v, b(Blocks.ANDESITE_WALL));
                norays++;
            }
        }
        // las dos escaleras que bajan a la bahia, pegadas al muro, hacia el sur
        for (int vs : ESCALERAS) escaleraAlFondo(m, MUELLE_U + 2, MUELLE_U + 3, vs, true);
    }

    /** Donde empiezan las escaleras del muelle (bajan hacia el sur). */
    static final int[] ESCALERAS = {-10, 22};

    private static boolean escaleraMuelle(int v) {
        for (int vs : ESCALERAS) if (v >= vs - 1 && v <= vs + (SUELO - FONDO) + 1) return true;
        return false;
    }

    /**
     * Una escalera de {@code SUELO - FONDO} peldanos de (ua..ub, v0 + 1) bajando hacia el sur (o el norte) hasta el fondo; el
     * relleno por debajo, de sillar.
     */
    private void escaleraAlFondo(Pilotes.Lista m, int ua, int ub, int v0, boolean alSur) {
        int n = SUELO - FONDO;
        for (int i = 1; i <= n; i++) {
            int v = alSur ? v0 + i : v0 - i, y = SUELO - i;
            for (int u = ua; u <= ub; u++) {
                for (int yy = FONDO - 1; yy < y; yy++) put(m, u, yy, v, sillar(x(u), yy, z(v)));
                put(m, u, y, v, escalera(Blocks.STONE_BRICK_STAIRS, alSur ? Direction.NORTH : Direction.SOUTH, false));
                for (int yy = y + 1; yy <= y + 3; yy++) if (yy > FONDO) put(m, u, yy, v, Pal.AIR);
                escalones++;
            }
        }
    }

    private void espigon(Pilotes.Lista m) {
        for (int u = MUELLE_U + 2; u <= ESPIGON_U1; u++)
            for (int dv = -3; dv <= 3; dv++) {
                int v = ESPIGON_V + dv;
                int pie = superficie(x(u), z(v));
                for (int y = Math.min(pie, FONDO) - 1; y < SUELO; y++)
                    put(m, u, y, v, Math.abs(dv) == 3 ? sillar(x(u), y, z(v)) : Hash.pct(x(u), y, z(v), SEMILLA + 31) < 60 ? b(Blocks.COBBLESTONE) : b(Blocks.STONE));
                int p = Hash.pct(x(u), 5, z(v), SEMILLA + 32);
                put(m, u, SUELO, v, dv == 0 ? b(Blocks.POLISHED_ANDESITE) : Math.abs(dv) == 3 ? b(Blocks.STONE_BRICKS)
                        : p < 60 ? b(Blocks.STONE_BRICKS) : p < 80 ? b(Blocks.CRACKED_STONE_BRICKS) : b(Blocks.COBBLESTONE));
                for (int y = PISO; y <= PISO + 3; y++) put(m, u, y, v, Pal.AIR);
                // el pretil, con sus huecos y faroles de almas
                if (Math.abs(dv) == 3) {
                    boolean hueco = Math.floorMod(u, 9) == 0 || dv > 0 && (u == BAJADA_U || u == BAJADA_U + 1);
                    if (!hueco) une(m, u, PISO, v, b(Blocks.STONE_BRICK_WALL));
                    if (!hueco && Math.floorMod(u, 12) == 6) {
                        put(m, u, PISO + 1, v, Pal.SOUL_LANTERN);
                        faroles++;
                    }
                }
            }
        // la bajada del espigon a la bahia (por el sur)
        escaleraAlFondo(m, BAJADA_U, BAJADA_U + 1, ESPIGON_V + 3, true);
    }

    /** Donde baja la escalera del espigon a la bahia. */
    static final int BAJADA_U = 50;

    /** La altura de la grada en u (de SUELO arriba a FONDO abajo). */
    static double gradaAlto(int u) {
        return SUELO - (double) (u - GRADA_U0) * (SUELO - FONDO) / (GRADA_U1 - GRADA_U0);
    }

    /** La grada del varadero: una rampa de losas (con dos carriles de madera) que baja del pueblo a la bahia por el muelle. */
    private void grada(Pilotes.Lista m) {
        for (int u = GRADA_U0; u <= GRADA_U1; u++) {
            double h = gradaAlto(u);
            int yb = (int) Math.floor(h);
            boolean media = h - yb >= 0.5;
            for (int dv = -4; dv <= 4; dv++) {
                int v = GRADA_V + dv;
                int pie = superficie(x(u), z(v));
                if (Math.abs(dv) == 4) {
                    // los muros de los lados: en el pueblo sujetan la tierra; en la bahia, la rampa
                    for (int y = Math.min(pie, FONDO) - 1; y <= (u < BAHIA_U ? SUELO : yb); y++) put(m, u, y, v, sillar(x(u), y, z(v)));
                    if (u < BAHIA_U) put(m, u, PISO, v, Pal.AIR);
                    continue;
                }
                for (int y = Math.min(pie, FONDO) - 1; y < yb; y++) put(m, u, y, v, sillar(x(u), y, z(v)));
                boolean carril = Math.abs(dv) == 1;
                put(m, u, yb, v, carril ? tronco(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.X) : Hash.pct(x(u), yb, z(v), SEMILLA + 41) < 70
                        ? b(Blocks.SMOOTH_STONE) : b(Blocks.STONE_BRICKS));
                int libre = yb + 1;
                if (media) {
                    put(m, u, yb + 1, v, losa(carril ? Blocks.SPRUCE_SLAB : Blocks.SMOOTH_STONE_SLAB, SlabType.BOTTOM));
                    libre = yb + 2;
                }
                for (int y = libre; y <= SUELO + 5; y++) put(m, u, y, v, Pal.AIR);
            }
        }
    }

    // ------------------------------------------------------------------ la roca y el Faro de los Huesos

    /** Lo alto de la roca del faro en (du, dv) del centro (MIN_VALUE fuera). */
    private static int alturaRoca(int du, int dv) {
        double r = Math.hypot(du, dv) + 1.3 * RUIDO.ruido((FARO_U + du) / 4.0, (FARO_V + dv) / 4.0);
        if (du < 0 && Math.abs(dv) <= 3) r = Math.min(r, ROCA_R - 2);   // por donde llega el espigon
        if (r > ROCA_R) return Integer.MIN_VALUE;
        if (r <= ROCA_R - 1.6) return SUELO;
        return SUELO - 1 - (int) Math.round((r - (ROCA_R - 1.6)) * 1.8);
    }

    private void roca(Pilotes.Lista m) {
        for (int du = -ROCA_R - 2; du <= ROCA_R + 2; du++)
            for (int dv = -ROCA_R - 2; dv <= ROCA_R + 2; dv++) {
                int h = alturaRoca(du, dv);
                if (h == Integer.MIN_VALUE) continue;
                int u = FARO_U + du, v = FARO_V + dv;
                int pie = superficie(x(u), z(v));
                for (int y = Math.min(pie, FONDO) - 1; y <= h; y++) put(m, u, y, v, peña(x(u), y, z(v)));
                if (h == SUELO) {
                    int p = Hash.pct(x(u), 3, z(v), SEMILLA + 51);
                    put(m, u, SUELO, v, p < 50 ? b(Blocks.SMOOTH_SANDSTONE) : p < 78 ? b(Blocks.CUT_SANDSTONE) : p < 92 ? b(Blocks.SANDSTONE) : b(Blocks.BONE_BLOCK));
                }
                for (int y = h + 1; y <= h + 4; y++) put(m, u, y, v, Pal.AIR);
            }
        // lo que hay en la roca: una cadena de ancla tirada, barriles y huesos
        for (int k = 0; k < 5; k++) put(m, FARO_U + 2 + k, PISO, FARO_V + 5, Pal.log(Blocks.CHAIN, Direction.Axis.X));
        put(m, FARO_U + 5, PISO, FARO_V - 5, barril(Direction.UP));
        put(m, FARO_U + 6, PISO, FARO_V - 4, barril(Direction.UP));
        put(m, FARO_U + 5, PISO + 1, FARO_V - 5, Pal.COBWEB);
        put(m, FARO_U - 3, PISO, FARO_V + 6, hueso(Direction.Axis.Z));
        put(m, FARO_U - 3, PISO, FARO_V + 5, hueso(Direction.Axis.Z));
    }

    /** El anillo de la escalera (como el faro de Brumavela), en el sentido de las agujas del reloj desde la esquina del noroeste. */
    private static final int[][] ANILLO = new int[16][];

    static {
        int i = 0;
        for (int d = -2; d < 2; d++) ANILLO[i++] = new int[]{d, -2};
        for (int d = -2; d < 2; d++) ANILLO[i++] = new int[]{2, d};
        for (int d = 2; d > -2; d--) ANILLO[i++] = new int[]{d, 2};
        for (int d = 2; d > -2; d--) ANILLO[i++] = new int[]{-2, d};
    }

    /** La celda del anillo de detras de la puerta (al oeste). */
    private static final int ENTRADA = 14;

    private static boolean muroFaro(int du, int dv) {
        return Math.max(Math.abs(du), Math.abs(dv)) == 3 && Math.abs(du) + Math.abs(dv) <= 5;
    }

    private static boolean dentroFaro(int du, int dv) {
        return Math.max(Math.abs(du), Math.abs(dv)) <= 2;
    }

    private void faro(Pilotes.Lista m) {
        int y0 = PISO, g = LINTERNA - 1;
        // el fuste: hueso, con dos bandas de piedra de arena y saeteras
        for (int du = -3; du <= 3; du++)
            for (int dv = -3; dv <= 3; dv++) {
                int u = FARO_U + du, v = FARO_V + dv;
                if (dentroFaro(du, dv)) {
                    for (int y = y0; y <= LINTERNA + 3; y++) put(m, u, y, v, Pal.AIR);
                    continue;
                }
                if (!muroFaro(du, dv)) continue;
                for (int y = y0; y < g; y++) {
                    BlockState s = y <= y0 + 1 ? b(Blocks.CUT_SANDSTONE) : (y - y0) % 12 == 0 || (y - y0) % 12 == 1 ? b(Blocks.SMOOTH_SANDSTONE)
                            : huesoMuro(x(u), y, z(v));
                    int cara = du == 3 ? 0 : dv == 3 ? 1 : du == -3 ? 2 : 3;
                    int k = Math.abs(du) == 3 ? dv : du;
                    if (k == 0 && y > y0 + 4 && y < g - 3 && Math.floorMod(y - y0 - 5 - cara * 2, 8) < 2) {
                        une(m, u, y, v, Pal.IRON_BARS);
                        continue;
                    }
                    put(m, u, y, v, s);
                }
            }
        // la puerta, al oeste
        put(m, FARO_U - 3, y0, FARO_V, Pal.AIR);
        put(m, FARO_U - 3, y0 + 1, FARO_V, Pal.AIR);
        put(m, FARO_U - 3, y0 + 2, FARO_V, b(Blocks.CHISELED_SANDSTONE));
        // el pilar y la escalera de caracol: tres vueltas de doce peldanos con rellanos en las esquinas
        for (int du = -1; du <= 1; du++)
            for (int dv = -1; dv <= 1; dv++)
                for (int y = y0 - 1; y < g; y++)
                    put(m, FARO_U + du, y, FARO_V + dv, du == 0 && dv == 0 ? hueso(Direction.Axis.Y) : y % 6 == 0 ? b(Blocks.SMOOTH_SANDSTONE) : hueso(Direction.Axis.Y));
        for (int[] a : ANILLO) put(m, FARO_U + a[0], y0 - 1, FARO_V + a[1], b(Blocks.SMOOTH_SANDSTONE));
        int idx = ENTRADA, ult = y0 - 1, puestos = 0;
        while (puestos < FARO_ALTO) {
            idx = (idx + 1) & 15;
            int[] a = ANILLO[idx], bb = ANILLO[(idx + 1) & 15];
            int u = FARO_U + a[0], v = FARO_V + a[1];
            if ((idx & 3) == 0) put(m, u, ult, v, b(Blocks.SMOOTH_SANDSTONE));
            else {
                ult++;
                puestos++;
                put(m, u, ult, v, escalera(puestos % 5 == 2 ? Blocks.SANDSTONE_STAIRS : Blocks.SMOOTH_SANDSTONE_STAIRS, Pal.dirOf(bb[0] - a[0], bb[1] - a[1]), false));
                if (puestos % 6 == 2 && ult > y0 + 4) put(m, u, ult - 1, v, Pal.SOUL_LANTERN_HANG);
            }
            for (int k = 1; k <= 3; k++) put(m, u, ult + k, v, Pal.AIR);
        }
        // el suelo de la linterna (menos el hueco de la escalera) y la galeria de fuera, con su barandilla de hierro
        for (int du = -2; du <= 2; du++)
            for (int dv = -2; dv <= 2; dv++) {
                boolean hueco = (du == -2 && dv >= 0) || (dv == 2 && du <= 0);
                if (hueco || Math.max(Math.abs(du), Math.abs(dv)) <= 1) continue;
                put(m, FARO_U + du, g, FARO_V + dv, b(Blocks.SMOOTH_SANDSTONE));
            }
        for (int du = -1; du <= 1; du++)
            for (int dv = -1; dv <= 1; dv++) put(m, FARO_U + du, g, FARO_V + dv, b(Blocks.CHISELED_SANDSTONE));
        for (int du = -5; du <= 5; du++)
            for (int dv = -5; dv <= 5; dv++) {
                int mx = Math.max(Math.abs(du), Math.abs(dv)), s = Math.abs(du) + Math.abs(dv);
                if (mx < 3 || s > 8 || dentroFaro(du, dv)) continue;
                int u = FARO_U + du, v = FARO_V + dv;
                if (muroFaro(du, dv)) {
                    put(m, u, g, v, b(Blocks.SMOOTH_SANDSTONE));
                    continue;
                }
                put(m, u, g, v, mx == 5 || s == 8 ? losa(Blocks.SMOOTH_SANDSTONE_SLAB, SlabType.TOP) : b(Blocks.SMOOTH_SANDSTONE));
                if (mx == 4 && s <= 6) put(m, u, g - 1, v, escalera(Blocks.SMOOTH_SANDSTONE_STAIRS, Pal.dirOf(-du, -dv), true));
                if (mx == 5 || s == 8) une(m, u, g + 1, v, Pal.IRON_BARS);
            }
        // la linterna: postes de hueso, cristales azules, el fuego frio sobre su peana y la piramide del tejado
        for (int du = -3; du <= 3; du++)
            for (int dv = -3; dv <= 3; dv++) {
                if (!muroFaro(du, dv)) continue;
                int u = FARO_U + du, v = FARO_V + dv;
                boolean poste = Math.abs(du) + Math.abs(dv) == 5;
                boolean puerta = (du == -3 && dv == -1) || (du == 3 && dv == 1);
                for (int y = LINTERNA; y <= LINTERNA + 3; y++) {
                    if (poste) put(m, u, y, v, hueso(Direction.Axis.Y));
                    else if (y == LINTERNA + 3) put(m, u, y, v, b(Blocks.SMOOTH_SANDSTONE));
                    else if (puerta && y <= LINTERNA + 1) put(m, u, y, v, Pal.AIR);
                    else if (y == LINTERNA) put(m, u, y, v, b(Blocks.SMOOTH_SANDSTONE));
                    else une(m, u, y, v, b(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE));
                }
            }
        put(m, FARO_U, LINTERNA, FARO_V, b(Blocks.SEA_LANTERN));
        put(m, FARO_U, LINTERNA + 1, FARO_V, Pal.SOUL_CAMPFIRE);
        for (int y = LINTERNA + 4; y <= LINTERNA + 7; y++) put(m, FARO_U, y, FARO_V, Pal.CHAIN);
        for (int k = 0; k <= 4; k++) {
            int mm = 4 - k, y = LINTERNA + 4 + k;
            for (int du = -mm; du <= mm; du++)
                for (int dv = -mm; dv <= mm; dv++) {
                    if (Math.abs(du) != mm && Math.abs(dv) != mm) continue;
                    if (mm == 4 && Math.abs(du) + Math.abs(dv) > 6) continue;
                    int u = FARO_U + du, v = FARO_V + dv;
                    if (mm == 0) {
                        put(m, u, y, v, hueso(Direction.Axis.Y));
                        continue;
                    }
                    Direction d = Math.abs(dv) == mm && Math.abs(du) < mm ? (dv < 0 ? Direction.SOUTH : Direction.NORTH)
                            : Math.abs(du) == mm && Math.abs(dv) < mm ? (du < 0 ? Direction.EAST : Direction.WEST)
                            : (dv < 0 ? Direction.SOUTH : Direction.NORTH);
                    une(m, u, y, v, escalera(Blocks.SMOOTH_SANDSTONE_STAIRS, d, false));
                }
        }
        put(m, FARO_U, LINTERNA + 9, FARO_V, hueso(Direction.Axis.Y));
        put(m, FARO_U, LINTERNA + 10, FARO_V, b(Blocks.END_ROD));
        // las costillas de fuera: cuatro huesos curvos que abrazan la torre (en las diagonales)
        for (int su : LADOS)
            for (int sv : LADOS) {
                int prev = -1;
                for (int k = 0; k <= 16; k++) {
                    double t = k / 16.0;
                    double dh = 5.6 - 2.0 * (1 - Math.cos(t * Math.PI / 2));
                    int y = y0 + (int) Math.round(15 * Math.sin(t * Math.PI / 2));
                    int du = (int) Math.round(su * dh * 0.7071), dv = (int) Math.round(sv * dh * 0.7071);
                    if (muroFaro(du, dv) || dentroFaro(du, dv)) continue;
                    for (int yy = prev < 0 ? y : Math.min(prev + 1, y); yy <= y; yy++) put(m, FARO_U + du, yy, FARO_V + dv, hueso(Direction.Axis.Y));
                    prev = y;
                }
                costillas++;
            }
        // las quijadas de la puerta: dos huesos que se curvan hasta casi tocarse sobre ella
        arcoHueso(m, FARO_U - 5, FARO_V, 3, 8, false, PISO - 1);
    }

    /**
     * Un arco de hueso (dos costillas que casi se tocan arriba): en el plano de v (si {@code enU} es falso: va de v - semi a
     * v + semi en u fijo) o en el de u; su pie en {@code base} + 1.
     */
    private void arcoHueso(Pilotes.Lista m, int uc, int vc, int semi, int alto, boolean enU, int base) {
        int prev = Integer.MIN_VALUE;
        for (int k = -semi; k <= semi; k++) {
            if (k == 0) {
                prev = Integer.MIN_VALUE;
                continue;
            }
            double f = (double) k / (semi + 0.6);
            int y = base + 1 + (int) Math.round(alto * Math.sqrt(Math.max(0, 1 - f * f)));
            int lo = prev == Integer.MIN_VALUE ? base + 1 : Math.min(prev, y);
            int hi = prev == Integer.MIN_VALUE ? y : Math.max(prev, y);
            for (int yy = lo; yy <= hi; yy++) {
                int u = enU ? uc + k : uc, v = enU ? vc : vc + k;
                put(m, u, yy, v, hueso(Direction.Axis.Y));
            }
            prev = y;
        }
    }

    // ------------------------------------------------------------------ el pueblo

    /** La entrada del pueblo: el arco de costillas de ballena sobre el camino del santuario. */
    private void arcoCostillas(Pilotes.Lista m) {
        for (int v = 26; v <= 27; v++) arcoHueso(m, -26, v, 6, 11, true, SUELO);
        // a los pies, huesos sueltos y una vertebra
        put(m, -33, PISO, 25, hueso(Direction.Axis.X));
        put(m, -34, PISO, 25, hueso(Direction.Axis.X));
        put(m, -19, PISO, 28, hueso(Direction.Axis.Z));
        put(m, -19, PISO + 1, 28, b(Blocks.BONE_BLOCK));
    }

    /** La plaza: el empedrado redondo, el ancla del monumento sobre su peana, los bancos y el pozo. */
    private void plaza(Pilotes.Lista m) {
        int uc = -12, vc = 0;
        for (int du = -9; du <= 9; du++)
            for (int dv = -9; dv <= 9; dv++) {
                double r = Math.hypot(du, dv);
                if (r > 9.3) continue;
                int u = uc + du, v = vc + dv;
                int p = Hash.pct(x(u), 2, z(v), SEMILLA + 61);
                BlockState s = r < 3.6 ? (((du + dv) & 1) == 0 ? b(Blocks.POLISHED_ANDESITE) : b(Blocks.STONE_BRICKS))
                        : Math.abs(r - 6.0) < 0.6 ? b(Blocks.SMOOTH_STONE) : p < 45 ? b(Blocks.COBBLESTONE) : p < 70 ? b(Blocks.STONE_BRICKS)
                        : p < 85 ? b(Blocks.MOSSY_COBBLESTONE) : Pal.GRAVEL;
                put(m, u, SUELO, v, s);
                put(m, u, PISO, v, Pal.AIR);
            }
        // la peana y el ancla (de hierro negro: piedra negra pulida)
        for (int du = -1; du <= 1; du++)
            for (int dv = -1; dv <= 1; dv++) put(m, uc + du, PISO, vc + dv, du == 0 && dv == 0 ? b(Blocks.POLISHED_BLACKSTONE) : b(Blocks.CHISELED_STONE_BRICKS));
        for (int y = PISO + 1; y <= PISO + 6; y++) une(m, uc, y, vc, b(Blocks.POLISHED_BLACKSTONE_WALL));
        for (int s : LADOS) {
            une(m, uc + s, PISO + 5, vc, b(Blocks.POLISHED_BLACKSTONE_WALL));
            une(m, uc + 2 * s, PISO + 5, vc, b(Blocks.POLISHED_BLACKSTONE_WALL));
            put(m, uc + s, PISO + 1, vc, escalera(Blocks.POLISHED_BLACKSTONE_STAIRS, s < 0 ? Direction.EAST : Direction.WEST, true));
            une(m, uc + 2 * s, PISO + 1, vc, b(Blocks.POLISHED_BLACKSTONE_WALL));
            une(m, uc + 2 * s, PISO + 2, vc, b(Blocks.POLISHED_BLACKSTONE_WALL));
        }
        put(m, uc, PISO + 7, vc, Pal.CHAIN);
        put(m, uc, PISO + 8, vc, Pal.log(Blocks.CHAIN, Direction.Axis.X));
        // los bancos, mirando al ancla
        for (int[] q : new int[][]{{-6, 0}, {6, 0}, {0, -6}, {0, 6}}) {
            Direction f = Pal.dirOf(-Integer.signum(q[0]), -Integer.signum(q[1])).getOpposite();
            for (int k = -1; k <= 1; k++) {
                int u = uc + q[0] + (q[1] != 0 ? k : 0), v = vc + q[1] + (q[0] != 0 ? k : 0);
                put(m, u, PISO, v, escalera(Blocks.SPRUCE_STAIRS, f, false));
            }
        }
        // el pozo (seco)
        int pu = -4, pv = 8;
        for (int du = -1; du <= 1; du++)
            for (int dv = -1; dv <= 1; dv++) {
                if (du == 0 && dv == 0) {
                    for (int y = SUELO - 6; y <= SUELO; y++) put(m, pu, y, pv, Pal.AIR);
                    put(m, pu, SUELO - 7, pv, b(Blocks.SAND));
                    put(m, pu, SUELO - 6, pv, Pal.COBWEB);
                    continue;
                }
                for (int y = SUELO - 6; y <= SUELO; y++) put(m, pu + du, y, pv + dv, b(Blocks.MOSSY_COBBLESTONE));
                une(m, pu + du, PISO, pv + dv, b(Blocks.MOSSY_COBBLESTONE_WALL));
            }
        for (int s : LADOS) {
            une(m, pu + s, PISO + 1, pv, Pal.SP_FENCE);
            une(m, pu + s, PISO + 2, pv, Pal.SP_FENCE);
        }
        for (int du = -1; du <= 1; du++) put(m, pu + du, PISO + 3, pv, losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
        put(m, pu, PISO + 2, pv, Pal.CHAIN);
        put(m, pu, PISO + 1, pv, Pal.CHAIN);
    }

    /** La lonja: una nave abierta de postes de picea con tejado a dos aguas (hundido por un lado), sus mesas y sus barriles. */
    private void lonja(Pilotes.Lista m) {
        int u0 = -26, u1 = -4, v0 = -28, v1 = -18, alto = 4;
        int vm = (v0 + v1) / 2;
        for (int u = u0; u <= u1; u++)
            for (int v = v0; v <= v1; v++) {
                put(m, u, SUELO, v, Hash.pct(x(u), 1, z(v), SEMILLA + 71) < 70 ? b(Blocks.SMOOTH_STONE) : b(Blocks.STONE_BRICKS));
                for (int y = PISO; y <= PISO + alto + 6; y++) put(m, u, y, v, Pal.AIR);
            }
        // los postes
        for (int u = u0; u <= u1; u += (u1 - u0) / 5)
            for (int v : new int[]{v0, v1})
                for (int y = PISO; y <= PISO + alto - 1; y++) put(m, u, y, v, tronco(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.Y));
        // el tejado: a dos aguas a lo largo de u, la cumbrera en vm; un trozo hundido
        for (int u = u0 - 1; u <= u1 + 1; u++)
            for (int v = v0 - 1; v <= v1 + 1; v++) {
                int d = Math.min(v - (v0 - 1), (v1 + 1) - v);
                int y = PISO + alto + d / 2;
                boolean hundido = u >= -14 && u <= -9 && v > vm && Hash.pct(x(u), 3, z(v), SEMILLA + 72) < 75;
                if (hundido) continue;
                if (v == vm) put(m, u, y, v, losa(Blocks.SPRUCE_SLAB, SlabType.DOUBLE));
                else if (d % 2 == 0) put(m, u, y, v, escalera(Blocks.SPRUCE_STAIRS, v < vm ? Direction.SOUTH : Direction.NORTH, false));
                else put(m, u, y, v, losa(Blocks.SPRUCE_SLAB, SlabType.TOP));
            }
        // las vigas de lado a lado y las mesas de piedra del pescado, en dos filas
        for (int u = u0; u <= u1; u += (u1 - u0) / 5)
            for (int v = v0 + 1; v < v1; v++) put(m, u, PISO + alto - 1, v, tronco(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.Z));
        for (int fila : new int[]{v0 + 3, v1 - 3})
            for (int u = u0 + 3; u <= u1 - 3; u++) {
                if (Math.floorMod(u - u0, 6) == 0) continue;
                put(m, u, PISO, fila, losa(Blocks.SMOOTH_STONE_SLAB, SlabType.TOP));
            }
        // barriles, cajas, la balanza (un caldero), las redes colgadas del tejado y algo de sal en el suelo
        put(m, u0 + 1, PISO, v0 + 1, barril(Direction.UP));
        put(m, u0 + 2, PISO, v0 + 1, barril(Direction.UP));
        put(m, u0 + 1, PISO + 1, v0 + 1, barril(Direction.UP));
        put(m, u1 - 1, PISO, v1 - 1, barril(Direction.NORTH));
        put(m, u1 - 2, PISO, v0 + 1, b(Blocks.CAULDRON));
        put(m, -20, PISO, vm, b(Blocks.COMPOSTER));
        for (int[] q : new int[][]{{-22, -25}, {-17, -21}, {-8, -26}, {-6, -20}}) put(m, q[0], PISO + alto - 2, q[1], Pal.COBWEB);
        for (int[] q : new int[][]{{-11, -22}, {-10, -23}, {-11, -24}}) put(m, q[0], PISO, q[1], nieve(2));
        put(m, -10, PISO, -22, b(Blocks.CALCITE));
    }

    /**
     * Una casa de pescadores: zocalo de piedra, paredes de cal, madera o piedra de arena (segun {@code k}), las esquinas de
     * picea, ventanas, la puerta (un hueco) hacia {@code puerta}, el tejado a dos aguas (con agujeros si esta en ruinas) y por
     * dentro lo de cada una.
     */
    private void casa(Pilotes.Lista m, int u0, int v0, int u1, int v1, Direction puerta, int alto, boolean ruina, int k) {
        casas++;
        Block[] ma = madera(k);
        boolean largoEnU = u1 - u0 >= v1 - v0;
        int pu = (u0 + u1) / 2, pv = (v0 + v1) / 2;
        int du = puerta == Direction.EAST ? u1 : puerta == Direction.WEST ? u0 : pu;
        int dv = puerta == Direction.SOUTH ? v1 : puerta == Direction.NORTH ? v0 : pv;
        for (int u = u0; u <= u1; u++)
            for (int v = v0; v <= v1; v++) {
                boolean borde = u == u0 || u == u1 || v == v0 || v == v1;
                put(m, u, SUELO, v, borde ? b(Blocks.COBBLESTONE) : b(ma[0]));
                for (int y = PISO; y <= PISO + alto + 6; y++) put(m, u, y, v, Pal.AIR);
                if (!borde) continue;
                boolean esquina = (u == u0 || u == u1) && (v == v0 || v == v1);
                int tope = PISO + alto - 1;
                if (ruina && !esquina) tope -= Hash.pct(x(u), 4, z(v), SEMILLA + 81 + k) % 3;
                for (int y = PISO; y <= tope; y++) {
                    BlockState s;
                    if (esquina) s = tronco(ma[3], Direction.Axis.Y);
                    else if (y == PISO) s = Hash.pct(x(u), y, z(v), SEMILLA + 82) < 70 ? b(Blocks.COBBLESTONE) : b(Blocks.MOSSY_COBBLESTONE);
                    else s = pared(k, x(u), y, z(v));
                    put(m, u, y, v, s);
                }
                // ventanas: en las paredes largas, una cada tres, a media altura
                boolean largo = (v == v0 || v == v1) == largoEnU;
                int a = largoEnU ? u : v;
                if (largo && !esquina && Math.floorMod(a - (largoEnU ? u0 : v0), 3) == 2 && tope >= PISO + 1) {
                    if (ruina && Hash.pct(x(u), 6, z(v), SEMILLA + 83) < 40) put(m, u, PISO + 1, v, Pal.AIR);
                    else une(m, u, PISO + 1, v, b(Blocks.GLASS_PANE));
                }
            }
        // la puerta
        put(m, du, PISO, dv, Pal.AIR);
        put(m, du, PISO + 1, dv, Pal.AIR);
        // el tejado: a dos aguas a lo largo del lado largo (la cumbrera en medio); los hastiales de la pared
        int a0 = largoEnU ? v0 : u0, a1 = largoEnU ? v1 : u1, am = (a0 + a1) / 2;
        for (int l = (largoEnU ? u0 : v0) - 1; l <= (largoEnU ? u1 : v1) + 1; l++)
            for (int a = a0 - 1; a <= a1 + 1; a++) {
                int d = Math.min(a - (a0 - 1), (a1 + 1) - a);
                int y = PISO + alto + d;
                int u = largoEnU ? l : a, v = largoEnU ? a : l;
                if (ruina && Hash.pct(x(u), 7, z(v), SEMILLA + 84 + k) < 22) continue;
                Direction f = largoEnU ? (a < am ? Direction.SOUTH : Direction.NORTH) : (a < am ? Direction.EAST : Direction.WEST);
                if (a == am && (a1 - a0) % 2 == 0) put(m, u, y, v, losa(ma[2], SlabType.BOTTOM));
                else put(m, u, y, v, escalera(ma[1], f, false));
                // el hastial: la pared de los lados cortos sube hasta el tejado
                boolean hastial = largoEnU ? (l == u0 || l == u1) : (l == v0 || l == v1);
                if (hastial && a > a0 && a < a1)
                    for (int yy = PISO + alto; yy < y; yy++) put(m, u, yy, v, pared(k, x(u), yy, z(v)));
            }
        // dentro: una mesa con velas, un barril, el hogar apagado, redes en un rincon y la sal que ha entrado
        int iu0 = u0 + 1, iu1 = u1 - 1, iv0 = v0 + 1, iv1 = v1 - 1;
        int mu = puerta == Direction.WEST ? iu1 : iu0, mv = puerta == Direction.NORTH ? iv1 : iv0;
        une(m, mu, PISO, mv, b(ma[4]));
        put(m, mu, PISO + 1, mv, losa(ma[2], SlabType.BOTTOM));
        if (!ruina) put(m, mu, PISO + 2, mv, vela(1 + k % 3, false));
        int bu = puerta == Direction.EAST ? iu0 : iu1, bv = puerta == Direction.SOUTH ? iv0 : iv1;
        put(m, bu, PISO, bv, barril(Direction.UP));
        put(m, (iu0 + iu1) / 2, PISO, puerta == Direction.SOUTH ? iv0 : puerta == Direction.NORTH ? iv1 : (iv0 + iv1) / 2,
                Hash.pct(k, 1, 2, SEMILLA) < 50 ? Pal.CAMPFIRE.setValue(BlockStateProperties.LIT, false) : b(Blocks.CAULDRON));
        put(m, mu, PISO + alto - 1, bv, Pal.COBWEB);
        if (k % 2 == 0) {
            put(m, bu, PISO, mv, b(Blocks.CALCITE));
            put(m, bu, PISO + 1, mv, nieve(3));
        }
    }

    /** La pared de una casa (segun {@code k}): cal (calcita y terracota blanca), madera de picea o piedra de arena. */
    private static BlockState pared(int k, int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 85);
        return switch (Math.floorMod(k, 3)) {
            case 0 -> p < 50 ? b(Blocks.WHITE_TERRACOTTA) : p < 82 ? b(Blocks.CALCITE) : p < 94 ? b(Blocks.SMOOTH_QUARTZ) : b(Blocks.COBBLESTONE);
            case 1 -> p < 66 ? b(Blocks.SPRUCE_PLANKS) : p < 86 ? b(Blocks.STRIPPED_SPRUCE_WOOD) : b(Blocks.DARK_OAK_PLANKS);
            default -> p < 52 ? b(Blocks.SANDSTONE) : p < 80 ? b(Blocks.SMOOTH_SANDSTONE) : p < 94 ? b(Blocks.CUT_SANDSTONE) : b(Blocks.CALCITE);
        };
    }

    private void casas(Pilotes.Lista m) {
        casa(m, -45, -19, -36, -12, Direction.SOUTH, 4, false, 0);
        casa(m, -47, 0, -39, 7, Direction.EAST, 4, true, 1);
        casa(m, -38, 12, -31, 19, Direction.NORTH, 3, false, 2);
        casa(m, -13, -45, -6, -38, Direction.SOUTH, 4, true, 3);
        casa(m, -32, -42, -24, -35, Direction.EAST, 4, false, 4);
        casa(m, -48, -36, -41, -28, Direction.EAST, 3, true, 5);
        casa(m, -2, -44, 6, -38, Direction.SOUTH, 3, false, 6);
        // las barcas volcadas junto a las casas
        barca(m, -22, 12, 5, 3, true, 0, true, false, 7, PISO - 1);
        barca(m, -40, -24, 5, 3, false, 0, true, false, 8, PISO - 1);
    }

    /** La casa del farero, junto al arranque del espigon: el atril con su cuaderno, el arcon, estantes y su catalejo. */
    private void casaFarero(Pilotes.Lista m) {
        int u0 = 4, u1 = 14, v0 = -36, v1 = -27;
        casa(m, u0, v0, u1, v1, Direction.SOUTH, 4, false, 2);
        // los estantes de la pared del norte, la mesa de cartas, el atril (lo pone su trozo), el arcon (su trozo), la cama y la luz
        for (int u = u0 + 1; u <= u1 - 1; u++) {
            if (u == 9) continue;
            put(m, u, PISO, v0 + 1, u % 3 == 0 ? Pal.BOOKSHELF : barril(Direction.UP));
            put(m, u, PISO + 1, v0 + 1, u % 2 == 0 ? Pal.BOOKSHELF : Pal.AIR);
        }
        put(m, 9, PISO, v0 + 1, Pal.AIR);
        put(m, u0 + 1, PISO, v1 - 2, b(Blocks.CARTOGRAPHY_TABLE));
        put(m, u0 + 1, PISO + 1, v1 - 2, vela(2, false));
        put(m, u1 - 1, PISO, v1 - 1, b(Blocks.WHITE_WOOL));
        put(m, u1 - 1, PISO, v1 - 2, b(Blocks.WHITE_WOOL));
        put(m, u1 - 2, PISO, v1 - 1, losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
        put(m, 9, PISO + 3, -32, Pal.SOUL_LANTERN_HANG);
        // el catalejo del farero junto a la ventana del este: un pararrayos sobre su tripode
        une(m, u1 - 1, PISO, v0 + 3, Pal.SP_FENCE);
        put(m, u1 - 1, PISO + 1, v0 + 3, b(Blocks.LIGHTNING_ROD));
        // y por fuera, un ancla vieja apoyada en la pared
        une(m, u1 + 1, PISO, v1 - 2, b(Blocks.POLISHED_BLACKSTONE_WALL));
        une(m, u1 + 1, PISO + 1, v1 - 2, b(Blocks.POLISHED_BLACKSTONE_WALL));
        put(m, u1 + 1, PISO, v1 - 3, escalera(Blocks.POLISHED_BLACKSTONE_STAIRS, Direction.SOUTH, true));
        put(m, u1 + 1, PISO, v1 - 1, escalera(Blocks.POLISHED_BLACKSTONE_STAIRS, Direction.NORTH, true));
    }

    /** Los secaderos: varas de picea en filas con las algas secas encima y las redes colgadas. */
    private void secaderos(Pilotes.Lista m) {
        for (int v : new int[]{-14, -10, -6}) {
            secaderos++;
            for (int u = 2; u <= 18; u++) {
                boolean poste = Math.floorMod(u - 2, 4) == 0;
                if (poste) {
                    une(m, u, PISO, v, Pal.SP_FENCE);
                    une(m, u, PISO + 1, v, Pal.SP_FENCE);
                }
                int p = Hash.pct(x(u), 4, z(v), SEMILLA + 91);
                if (!poste && p < 38) put(m, u, PISO + 2, v, b(Blocks.DRIED_KELP_BLOCK));
                else une(m, u, PISO + 2, v, Pal.SP_FENCE);
                if (!poste && p >= 70 && p < 82) put(m, u, PISO + 1, v, Pal.COBWEB);
            }
        }
        // los cestos y un monton de algas en el suelo
        put(m, 1, PISO, -8, b(Blocks.DRIED_KELP_BLOCK));
        put(m, 1, PISO, -7, b(Blocks.DRIED_KELP_BLOCK));
        put(m, 1, PISO + 1, -8, b(Blocks.DRIED_KELP_BLOCK));
        put(m, 19, PISO, -12, barril(Direction.UP));
        put(m, 19, PISO, -4, b(Blocks.COMPOSTER));
    }

    /** Las salinas: estanques de sal (uno mas bajo que el suelo) entre caballones de barro, dos con salmuera y montones de sal. */
    private void salinas(Pilotes.Lista m) {
        int u0 = -4, u1 = 19, v0 = 12, v1 = 32;
        for (int u = u0; u <= u1; u++)
            for (int v = v0; v <= v1; v++) {
                if (junto(u, v, SANT_R + 2)) continue;
                boolean caballon = Math.floorMod(u - u0, 5) == 0 || Math.floorMod(v - v0, 5) == 0 || u == u1 || v == v1;
                if (caballon) {
                    put(m, u, SUELO, v, Hash.pct(x(u), 2, z(v), SEMILLA + 101) < 60 ? b(Blocks.PACKED_MUD) : b(Blocks.MUD_BRICKS));
                    put(m, u, PISO, v, Pal.AIR);
                    continue;
                }
                int pan = Math.floorDiv(u - u0, 5) * 7 + Math.floorDiv(v - v0, 5);
                put(m, u, SUELO, v, Pal.AIR);
                put(m, u, PISO, v, Pal.AIR);
                if (pan % 5 == 3) {
                    put(m, u, SUELO - 1, v, b(Blocks.WATER));
                    put(m, u, SUELO - 2, v, b(Blocks.CLAY));
                    continue;
                }
                int p = Hash.pct(x(u), 3, z(v), SEMILLA + 102);
                put(m, u, SUELO - 1, v, p < 55 ? b(Blocks.WHITE_CONCRETE_POWDER) : p < 85 ? b(Blocks.CALCITE) : b(Blocks.SAND));
                if (p < 30) put(m, u, SUELO, v, nieve(1 + p % 3));
            }
        estanques = 4 * 4;
        // los montones de sal en los cruces de los caballones
        for (int[] q : new int[][]{{1, 17}, {11, 27}, {16, 12}, {-4, 22}}) {
            montones++;
            for (int du = -1; du <= 1; du++)
                for (int dv = -1; dv <= 1; dv++) {
                    boolean c = du == 0 && dv == 0;
                    put(m, q[0] + du, PISO, q[1] + dv, c ? b(Blocks.CALCITE) : Math.abs(du) + Math.abs(dv) == 1 ? b(Blocks.WHITE_CONCRETE_POWDER) : nieve(4));
                }
            put(m, q[0], PISO + 1, q[1], b(Blocks.WHITE_CONCRETE_POWDER));
            put(m, q[0], PISO + 2, q[1], nieve(3));
        }
        // un rastro apoyado en un monton y una carretilla (un caldero sobre su losa)
        une(m, 2, PISO, 18, Pal.SP_FENCE);
        put(m, 2, PISO + 1, 18, trampilla(Blocks.SPRUCE_TRAPDOOR, Direction.EAST, true, false));
        put(m, 13, PISO, 13, b(Blocks.CAULDRON));
    }

    /** El varadero: el barco a medio hacer sobre la grada (quilla, cuadernas y roda), los andamios y el taller. */
    private void varadero(Pilotes.Lista m) {
        int ku0 = 14, ku1 = 34;
        for (int u = ku0; u <= ku1; u++) {
            double h = gradaAlto(u);
            int yk = (int) Math.floor(h) + (h - Math.floor(h) >= 0.5 ? 2 : 1);
            put(m, u, yk, GRADA_V, tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
            // las cuadernas: costillas de madera cada tres
            if (Math.floorMod(u - ku0, 3) == 1 && u < ku1 - 1) {
                for (int s : LADOS) {
                    put(m, u, yk + 1, GRADA_V + s, tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Y));
                    put(m, u, yk + 2, GRADA_V + 2 * s, tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Y));
                    put(m, u, yk + 3, GRADA_V + 2 * s, tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Y));
                    une(m, u, yk + 4, GRADA_V + 3 * s, Pal.DO_FENCE);
                    une(m, u, yk + 5, GRADA_V + 3 * s, Pal.DO_FENCE);
                }
                costillas++;
            }
            // las tablas de la tablazon, solo abajo y a trozos
            if (u > ku0 + 2 && u < ku1 - 6 && Hash.pct(x(u), 2, 3, SEMILLA + 111) < 55)
                for (int s : LADOS) put(m, u, yk + 1, GRADA_V + 2 * s, b(Blocks.DARK_OAK_PLANKS));
        }
        // la roda: el madero de proa que sube
        double hp = gradaAlto(ku1 + 1);
        int yp = (int) Math.floor(hp) + 1;
        for (int y = yp; y <= yp + 6; y++) put(m, ku1 + 1, y, GRADA_V, tronco(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
        put(m, ku1 + 1, yp + 7, GRADA_V, losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
        // los andamios del lado norte: postes y una pasarela de tablas
        for (int u = ku0; u <= ku1; u += 4) {
            double h = gradaAlto(u);
            int yb = (int) Math.floor(h) + 1;
            for (int y = yb; y <= SUELO + 2; y++) une(m, u, y, GRADA_V - 3, Pal.SP_FENCE);
        }
        for (int u = ku0; u <= ku1; u++) put(m, u, SUELO + 3, GRADA_V - 3, losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
        // el taller junto a la grada: un cobertizo abierto con el banco, la muela y montones de tablas
        int tu0 = -2, tu1 = 5, tv0 = 33, tv1 = 37;
        for (int u = tu0; u <= tu1; u++)
            for (int v = tv0; v <= tv1; v++) {
                if (junto(u, v, SANT_R + 2)) continue;
                put(m, u, SUELO, v, b(Blocks.SPRUCE_PLANKS));
                boolean poste = (u == tu0 || u == tu1) && (v == tv0 || v == tv1);
                if (poste) for (int y = PISO; y <= PISO + 2; y++) put(m, u, y, v, tronco(Blocks.SPRUCE_LOG, Direction.Axis.Y));
                put(m, u, PISO + 3, v, losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
            }
        put(m, tu0 + 1, PISO, tv0 + 1, b(Blocks.CRAFTING_TABLE));
        put(m, tu0 + 2, PISO, tv0 + 1, b(Blocks.GRINDSTONE));
        put(m, tu1 - 1, PISO, tv0 + 1, b(Blocks.STONECUTTER));
        for (int u = tu0 + 1; u <= tu0 + 3; u++) put(m, u, PISO, tv1 - 1, tronco(Blocks.SPRUCE_LOG, Direction.Axis.X));
        put(m, tu0 + 2, PISO + 1, tv1 - 1, tronco(Blocks.SPRUCE_LOG, Direction.Axis.X));
        put(m, tu1 - 1, PISO, tv1 - 1, losa(Blocks.DARK_OAK_SLAB, SlabType.DOUBLE));
    }

    // ------------------------------------------------------------------ la bahia: la ballena y las barcas

    /** La ballena varada: la cola hundida en la arena, las costillas en arco con el espinazo encima y la calavera con sus quijadas. */
    private void ballena(Pilotes.Lista m) {
        int y0 = FONDO + 1;
        // el espinazo: sube de la cola hasta lo alto de las costillas y baja al cuello
        int prev = -1;
        for (int u = BALLENA_U0; u <= 76; u++) {
            int h = u < 44 ? (u - BALLENA_U0) / 3 : u < 52 ? 2 + (u - 44) * 6 / 8 : u <= 70 ? 8 : 8 - (u - 70) / 3;
            int y = y0 + h;
            put(m, u, y, BALLENA_V, hueso(Direction.Axis.X));
            if (prev >= 0 && Math.abs(prev - y) > 1)
                for (int yy = Math.min(prev, y) + 1; yy < Math.max(prev, y); yy++) put(m, u, yy, BALLENA_V, hueso(Direction.Axis.Y));
            // las apofisis: un hueso de pie cada dos
            if (u % 2 == 0 && h >= 2) put(m, u, y + 1, BALLENA_V, b(Blocks.BONE_BLOCK));
            prev = y;
        }
        // la aleta de la cola, medio enterrada
        for (int k = 0; k <= 3; k++)
            for (int s : LADOS) put(m, BALLENA_U0 + k, y0, BALLENA_V + s * (k + 1), hueso(Direction.Axis.Z));
        // las costillas: arcos del espinazo al suelo, a los dos lados (alguna rota)
        for (int u = 50; u <= 68; u += 3) {
            costillas++;
            int rota = Hash.pct(u, 1, 1, SEMILLA + 121);
            for (int s : LADOS) {
                int py = -1;
                for (int dv = 0; dv <= 7; dv++) {
                    double f = dv / 7.4;
                    int y = y0 + (int) Math.round(8 * Math.sqrt(Math.max(0, 1 - f * f)));
                    boolean falta = rota < 30 && s == (rota % 2 == 0 ? 1 : -1) && dv >= 3 && dv <= 5;
                    if (!falta) {
                        int lo = py < 0 ? y : Math.min(py - 1, y), hi = py < 0 ? y : Math.max(py - 1, y);
                        for (int yy = lo; yy <= hi; yy++) put(m, u, yy, BALLENA_V + s * dv, hueso(Direction.Axis.Y));
                    }
                    py = y;
                }
            }
        }
        // la calavera: el craneo (una mole con las cuencas), el hocico y las dos quijadas tiradas en la arena
        for (int u = 74; u <= 79; u++)
            for (int dv = -4; dv <= 4; dv++) {
                int h = 6 - Math.abs(dv) / 2 - (u == 74 || u == 79 ? 1 : 0);
                for (int y = y0; y <= y0 + h; y++) {
                    boolean cuenca = (u == 77 || u == 78) && Math.abs(dv) == 4 && y >= y0 + 2 && y <= y0 + 3;
                    boolean hueco = Math.abs(dv) <= 2 && y >= y0 && y <= y0 + 3 && u > 74;
                    put(m, u, y, BALLENA_V + dv, cuenca || hueco ? Pal.AIR : b(Blocks.BONE_BLOCK));
                }
            }
        for (int u = 80; u <= BALLENA_U1; u++) {
            int w = Math.max(1, 3 - (u - 80) / 3);
            for (int dv = -w; dv <= w; dv++) put(m, u, y0 + 4 - (u - 80) / 4, BALLENA_V + dv, hueso(Direction.Axis.X));
        }
        for (int s : LADOS)
            for (int u = 75; u <= BALLENA_U1; u++) {
                int dv = s * (4 - (u - 75) / 5);
                put(m, u, y0, BALLENA_V + dv, hueso(Direction.Axis.X));
            }
    }

    /**
     * Una barca: el casco (puntiagudo en los dos extremos) de {@code largo} x {@code ancho}, a lo largo de u o de v, hundida un
     * bloque en la arena ({@code base} es el fondo de la quilla), escorada hacia un lado; o volcada (la quilla arriba). Con palo
     * roto si se pide.
     */
    private void barca(Pilotes.Lista m, int uc, int vc, int largo, int ancho, boolean enU, int escora, boolean volcada, boolean palo, int k, int base) {
        barcas++;
        Block[] ma = madera(k);
        double hl = largo / 2.0, hw = ancho / 2.0;
        int alto = ancho >= 5 ? 3 : 2;
        for (int a = (int) -Math.ceil(hl); a <= (int) Math.ceil(hl); a++)
            for (int c = (int) -Math.ceil(hw); c <= (int) Math.ceil(hw); c++) {
                double w = hw * Math.sqrt(Math.max(0, 1 - (2.0 * a / largo) * (2.0 * a / largo)));
                if (Math.abs(c) > w + 0.35) continue;
                int u = enU ? uc + a : uc + c, v = enU ? vc + c : vc + a;
                boolean borde = Math.abs(c) > w - 0.85 || Math.abs(a) >= hl - 0.6;
                int top = base + alto + (c > 0 ? escora : c < 0 ? -escora : 0);
                if (volcada) {
                    if (borde) for (int y = base + 1; y <= base + alto; y++) put(m, u, y, v, b(ma[0]));
                    else put(m, u, base + alto, v, b(ma[0]));
                    if (c == 0) put(m, u, base + alto + 1, v, losa(ma[2], SlabType.BOTTOM));   // la quilla
                    continue;
                }
                put(m, u, base, v, c == 0 ? tronco(ma[3], enU ? Direction.Axis.X : Direction.Axis.Z) : b(ma[0]));
                if (borde) {
                    for (int y = base + 1; y <= Math.max(base + 1, top); y++)
                        if (Hash.pct(x(u), y, z(v), SEMILLA + 131 + k) >= 12) put(m, u, y, v, b(ma[0]));
                } else {
                    for (int y = base + 1; y <= base + alto + 1; y++) put(m, u, y, v, Pal.AIR);
                    if (Math.floorMod(a, 3) == 0) put(m, u, base + 1, v, losa(ma[2], SlabType.TOP));   // los bancos
                }
            }
        if (palo && !volcada) {
            int pu = enU ? uc - (int) (hl / 3) : uc, pv = enU ? vc : vc - (int) (hl / 3);
            int h = 4 + Math.floorMod(k, 3);
            for (int y = base + 1; y <= base + h; y++) put(m, pu, y, pv, tronco(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.Y));
            put(m, pu, base + h + 1, pv, Pal.SP_FENCE);
            // la verga caida, cruzada, y la vela hecha jirones (redes)
            put(m, pu + (enU ? 0 : 1), base + h, pv + (enU ? 1 : 0), Pal.COBWEB);
            put(m, pu + (enU ? 0 : -1), base + h - 1, pv + (enU ? -1 : 0), Pal.COBWEB);
        }
    }

    private void barcas(Pilotes.Lista m) {
        barca(m, 38, 6, 11, 5, true, 1, false, true, 0, FONDO);
        barca(m, 52, -38, 9, 4, false, 0, false, true, 1, FONDO);
        barca(m, 92, 10, 13, 6, true, -1, false, true, 2, FONDO);
        barca(m, 66, -42, 9, 4, true, 0, true, false, 3, FONDO);
        barca(m, 44, 18, 7, 3, false, 0, true, false, 4, FONDO);
    }

    /** Montones de sal y redes viejas por la bahia, y algun ancla perdida. */
    private void montonesBahia(Pilotes.Lista m) {
        for (int[] q : new int[][]{{34, -30}, {72, 2}, {58, -10}, {100, -10}, {80, 44}}) {
            montones++;
            int y0 = pieB(q[0], q[1]);
            for (int du = -2; du <= 2; du++)
                for (int dv = -2; dv <= 2; dv++) {
                    int r = Math.abs(du) + Math.abs(dv);
                    if (r > 3) continue;
                    put(m, q[0] + du, y0, q[1] + dv, r <= 1 ? b(Blocks.CALCITE) : r == 2 ? b(Blocks.WHITE_CONCRETE_POWDER) : nieve(3));
                }
            put(m, q[0], y0 + 1, q[1], b(Blocks.WHITE_CONCRETE_POWDER));
            put(m, q[0], y0 + 2, q[1], nieve(4));
        }
        // un ancla perdida y su cadena
        int au = 64, av = 14, y0 = pieB(au, av);
        une(m, au, y0, av, b(Blocks.POLISHED_BLACKSTONE_WALL));
        une(m, au, y0 + 1, av, b(Blocks.POLISHED_BLACKSTONE_WALL));
        une(m, au, y0 + 2, av, b(Blocks.POLISHED_BLACKSTONE_WALL));
        put(m, au - 1, y0, av, escalera(Blocks.POLISHED_BLACKSTONE_STAIRS, Direction.EAST, false));
        put(m, au + 1, y0, av, escalera(Blocks.POLISHED_BLACKSTONE_STAIRS, Direction.WEST, false));
        for (int k = 1; k <= 6; k++) put(m, au, y0, av - k, Pal.log(Blocks.CHAIN, Direction.Axis.Z));
    }

    // ------------------------------------------------------------------ lo que lleva datos: el atril, los arcones, los barriles

    private void trozos() {
        trozo(9, PISO, -34, c -> c.lectern(x(9), PISO, z(-34), Direction.SOUTH, Perdidos.libro(26)));
        trozo(12, PISO, -35, c -> c.chest(x(12), PISO, z(-35), Direction.SOUTH, BOTIN_BUENO));
        trozo(-25, PISO, -27, c -> c.barrel(x(-25), PISO, z(-27), Direction.UP, BOTIN));
        trozo(-44, PISO, -18, c -> c.barrel(x(-44), PISO, z(-18), Direction.UP, BOTIN));
        trozo(-31, PISO, -36, c -> c.chest(x(-31), PISO, z(-36), Direction.SOUTH, BOTIN));
        trozo(18, PISO, -3, c -> c.barrel(x(18), PISO, z(-3), Direction.UP, BOTIN));
        trozo(92, FONDO + 1, 10, c -> c.chest(x(92), FONDO + 1, z(10), Direction.WEST, BOTIN));
        trozo(FARO_U + 2, LINTERNA, FARO_V - 2, c -> c.chest(x(FARO_U + 2), LINTERNA, z(FARO_V - 2), Direction.WEST, BOTIN_BUENO));
    }

    private void trozo(int u, int y, int v, Trozo.Obra o) {
        elementos.add(new Trozo(6, x(u), y, z(v), x(u), y, z(v), o));
    }

    // ------------------------------------------------------------------ los salobres

    private static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;

    private void ponerEsperas() {
        int R0 = Salobre.REDERO, B = Salobre.BICHERO, S = Salobre.SALINERO;
        int R = Enemigo.REZA, T = Enemigo.TUMBADO, D = Enemigo.DE_PIE;
        int Y = FONDO + 1;
        // la plaza: un redero remendando junto al ancla y otro de ronda
        en(R0, -8, PISO, 4, OESTE, R, 0, "plaza");
        en(R0, -17, PISO, -4, ESTE, D, 4, "plaza");
        // la lonja: un bichero de ronda y dos rederos a las mesas
        en(B, -14, PISO, -21, SUR, D, 3, "lonja");
        en(R0, -20, PISO, -24, SUR, R, 0, "lonja");
        en(R0, -7, PISO, -24, NORTE, R, 0, "lonja");
        // las casas del oeste
        en(R0, -40, PISO, -9, SUR, R, 0, "casas");
        en(B, -33, PISO, 7, ESTE, D, 4, "casas");
        en(S, -28, PISO, -32, SUR, T, 0, "casas");
        // los secaderos: un redero remendando y un bichero de ronda
        en(R0, 8, PISO, -8, NORTE, R, 0, "secadero");
        en(B, 14, PISO, -2, OESTE, D, 3, "secadero");
        // las salinas: tres salineros faenando o tumbados en la sal, y uno de ronda
        en(S, 0, SUELO, 20, NORTE, R, 0, "salinas");
        en(S, 10, SUELO, 29, OESTE, R, 0, "salinas");
        en(S, 14, SUELO, 20, giro(-1, 1), T, 0, "salinas");
        en(S, 6, PISO, 22, SUR, D, 3, "salinas");
        // la casa del farero: uno tirado en el suelo y un bichero en la puerta
        en(R0, 6, PISO, -30, giro(1, 1), T, 0, "farero");
        en(B, 9, PISO, -24, SUR, D, 0, "farero");
        // el muelle: dos bicheros mirando al mar que no esta y un redero sentado en el borde
        en(B, 20, PISO, -40, ESTE, D, 0, "muelle");
        en(B, 20, PISO, 16, ESTE, D, 2, "muelle");
        en(R0, 19, PISO, -14, ESTE, R, 0, "muelle");
        // el varadero: un redero en la grada y un bichero abajo
        en(R0, 12, PISO, 44 - 9, SUR, R, 0, "varadero");
        en(B, 30, pieGrada(30), GRADA_V + 2, OESTE, D, 2, "varadero");
        // el espigon: un redero remendando, un bichero de ronda y el de la puerta del faro
        en(R0, 40, PISO, ESPIGON_V - 1, NORTE, R, 0, "espigon");
        en(B, 62, PISO, ESPIGON_V + 1, OESTE, D, 3, "espigon");
        en(B, FARO_U - 6, PISO, FARO_V + 1, OESTE, D, 0, "faro");
        // arriba del faro, en la galeria
        en(B, FARO_U + 4, LINTERNA, FARO_V + 1, ESTE, D, 0, "faro_alto");
        // la bahia: salineros enterrados en las costras, un bichero entre las barcas, un redero en una barca, junto a la ballena
        en(S, 40, pieB(40, 0), 0, giro(1, -1), T, 0, "bahia");
        en(S, 70, pieB(70, 12), 12, giro(-1, 1), T, 0, "bahia");
        en(S, 88, pieB(88, -40), -40, giro(-1, 1), T, 0, "bahia");
        en(B, 50, pieB(50, -30), -30, NORTE, D, 5, "bahia");
        en(R0, 33, pieB(33, 9), 9, ESTE, R, 0, "bahia");
        en(S, 58, pieB(58, BALLENA_V), BALLENA_V, ESTE, T, 0, "ballena");
        en(S, 62, pieB(62, BALLENA_V + 10), BALLENA_V + 10, NORTE, D, 4, "ballena");
        en(B, 82, pieB(82, BALLENA_V - 7), BALLENA_V - 7, giro(-1, 1), D, 0, "ballena");
    }

    /** Lo que se pisa en la grada en u. */
    static int pieGrada(int u) {
        double h = gradaAlto(u);
        int yb = (int) Math.floor(h);
        return h - yb >= 0.5 ? yb + 2 : yb + 1;
    }

    /** Lo que se pisa en la bahia en (u, v). */
    private static int pieB(int u, int v) {
        return pie(x(u), z(v));
    }

    /** El hacia donde mira de Minecraft (0 = sur, 90 = oeste) para una direccion (du, dv). */
    static float giro(double du, double dv) {
        return (float) (Math.toDegrees(Math.atan2(dv, du)) - 90.0);
    }

    private void en(int variante, int u, int y, int v, float giro, int postura, int ronda, String grupo) {
        esperas.add(new Espera(variante, x(u), y, z(v), giro, postura, ronda, grupo));
    }

    // ------------------------------------------------------------------ para la prueba

    /** Lo que hay en el puerto ya generado (para la prueba): el atril con el libro, el arcon, el muelle, el fondo, la ballena y el faro. */
    public static String resumen(ServerLevel m) {
        Varadero a = get();
        // los sitios del atril y del arcon son donde se pisa, delante (al sur) de cada uno
        int[] at = a.sitio("atril"), ar = a.sitio("arcon");
        boolean libro = m.getBlockEntity(new BlockPos(at[0], at[1], at[2] - 1)) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le
                && Perdidos.de(le.getBook()) == 26;
        boolean arcon = m.getBlockState(new BlockPos(ar[0], ar[1], ar[2] - 1)).is(Blocks.CHEST);
        boolean fuego = m.getBlockState(new BlockPos(CX + FARO_U, LINTERNA + 1, CZ + FARO_V)).is(Blocks.SOUL_CAMPFIRE);
        boolean muro = m.getBlockState(new BlockPos(CX + MUELLE_U + 1, SUELO, CZ)).is(Blocks.POLISHED_ANDESITE);
        boolean fondo = m.getBlockState(new BlockPos(CX + 40, FONDO + 1, CZ - 4)).isAir() && !m.getBlockState(new BlockPos(CX + 40, FONDO, CZ - 4)).isAir();
        int huesos = 0;
        // el pie de cada costilla del lado del sur (a 7 del espinazo: tres por encima del fondo)
        for (int u = 50; u <= 68; u += 3)
            for (int y = FONDO + 1; y <= FONDO + 6; y++)
                if (m.getBlockState(new BlockPos(CX + u, y, CZ + BALLENA_V + 7)).is(Blocks.BONE_BLOCK)) {
                    huesos++;
                    break;
                }
        return "atril con el libro 26 " + (libro ? "SI" : "NO") + ", arcon " + (arcon ? "SI" : "NO") + ", fuego del faro " + (fuego ? "SI" : "NO")
                + ", muelle " + (muro ? "SI" : "NO") + ", fondo seco " + (fondo ? "SI" : "NO") + ", costillas de la ballena " + huesos + "/7";
    }
}
