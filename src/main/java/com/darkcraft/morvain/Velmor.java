package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.fortaleza.Perdidos;
import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Landscape;
import com.darkcraft.gen.Pal;
import com.darkcraft.gen.Rect;
import com.darkcraft.gen.Rng;
import com.darkcraft.gen.Town;
import com.darkcraft.pueblo.gen.Casa;
import com.darkcraft.pueblo.gen.Mat;
import com.darkcraft.pueblo.gen.Trozo;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Velmor (parte 7e): el pueblo marchito de las Llanuras Palidas, en la orilla este del rio palido, al norte del santuario
 * del Vado. Era un pueblo de campos y de una capilla con campanario; cuando la Sombra volvio a Morvain su gente se quedo
 * donde estaba y se fue secando por dentro hasta quedar en ceniza que anda: los Marchitos ({@code enemigos/Marchito}).
 * <p>
 * Dos calles se cruzan en la plaza del pozo (la horca al lado): la mayor de norte a sur, de la capilla a la puerta del sur
 * (la bajada al Vado), y la del puente de este a oeste, del puente roto sobre el rio a la puerta del este. Al norte la
 * capilla en ruinas (sin tejado, el campanario en pie con su campana, el atril con el libro perdido 18) y al oeste de ella
 * el cementerio con el panteon; los campos secos al oeste y al sureste; un vado de piedras cruza el rio al sur.
 * <p>
 * Se dibuja como Brumavela (las casas son {@link Casa}, con su barro y su madera) sobre el terreno de Morvain, chunk a
 * chunk al adornarlo ({@link MorvainGenerador#applyBiomeDecoration}): cada chunk ve lo mismo y no hace falta semilla.
 */
public final class Velmor {
    public static final long SEMILLA = 0x7E1A0E5A11L;
    /** El cruce de las dos calles: el centro de la plaza del pozo. */
    public static final int X = 850, Z = 4040, PLAZA_R = 11;
    /** El pueblo (el nombre del sitio): de la cabeza del puente a la puerta del este, del cementerio a la puerta del sur. */
    public static final int PX0 = 736, PX1 = 968, PZ0 = 3893, PZ1 = 4158;
    /** Todo lo que se dibuja (con el puente, el vado y los caminos): ahi {@link Adorno} solo pone hierba. */
    static final int X0 = 600, X1 = 990, Z0 = 3884, Z1 = 4200;
    /** El puente: de una orilla a la otra, el tablero (lo que se pisa esta encima) y su eje. */
    static final int PUENTE_X0 = 644, PUENTE_X1 = 734, TABLERO = 75, PUENTE_Z = 4040;
    /** Las pilas del puente (x del centro) y el trozo hundido del tablero. */
    static final int[] PILAS = {665, 685, 705};
    static final int HUECO_X0 = 690, HUECO_X1 = 699;
    /** El vado de piedras, al sur. */
    static final int VADO_Z = 4186;
    /** El cementerio, la capilla (la nave y el campanario) y los campos. */
    static final Rect CEMENTERIO = new Rect(806, 3897, 831, 3930);
    static final Rect NAVE = new Rect(840, 3901, 860, 3927), TORRE = new Rect(834, 3922, 839, 3927);
    static final Rect PANTEON = new Rect(808, 3899, 815, 3906);
    static final Rect[] CAMPOS = {new Rect(760, 3946, 798, 3994), new Rect(902, 4062, 952, 4120)};

    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/velmor"),
            BOTIN_BUENO = new ResourceLocation(DarkCraftMod.MODID, "chests/velmor_bueno");

    /** Las calles: {x0, z0, x1, z1, medio ancho, 0 empedrada o 1 de tierra}. */
    static final double[][] CALLES = {
            {850, 3929, 850, 4154, 2.3, 0},   // la calle mayor: del atrio de la capilla a la puerta del sur
            {735, 4040, 964, 4040, 2.0, 0},   // la calle del puente: de la cabeza del puente a la puerta del este
            {850, 4154, 858, 4166, 1.5, 1},   // la bajada al santuario del Vado
            {845, 3930, 832, 3929, 1.2, 1},   // al cementerio
            {801, 4037, 801, 3996, 1.2, 1},   // a los campos del oeste
            {916, 4043, 916, 4061, 1.2, 1},   // a los campos del este
            {964, 4040, 990, 4047, 1.4, 1},   // el camino del este, fuera del pueblo
            {643, 4040, 600, 4036, 1.4, 1},   // al otro lado del puente, hacia el arco
    };

    // ------------------------------------------------------------------ el terreno

    private static final int AW = X1 - X0 + 1, AD = Z1 - Z0 + 1;
    private static final int[] ALTO = new int[AW * AD];

    static {
        Arrays.fill(ALTO, Integer.MIN_VALUE);
    }

    /** El bloque de arriba del terreno de Morvain en (x, z) (sin nada construido): lo de debajo de cada casa y calle. */
    public static int alto(int x, int z) {
        if (x < X0 || x > X1 || z < Z0 || z > Z1) return (int) Math.floor(Terreno.columna(x, z).altura());
        int i = (z - Z0) * AW + (x - X0);
        int v = ALTO[i];
        if (v == Integer.MIN_VALUE) ALTO[i] = v = (int) Math.floor(Terreno.columna(x, z).altura());
        return v;
    }

    /** Lo que se pisa en (x, z): encima del terreno. */
    static int pie(int x, int z) {
        return alto(x, z) + 1;
    }

    // ------------------------------------------------------------------ el plano

    private static Velmor unico;

    public static synchronized Velmor get() {
        if (unico == null) unico = new Velmor();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Rect> ocupado = new ArrayList<>();
    public final List<Casa> casas = new ArrayList<>();
    public Casa taberna, herreria, alcaldia, granero;
    /** El suelo de la capilla (lo que se pisa en la nave). */
    public final int capillaY;

    private Velmor() {
        Rng rng = new Rng(SEMILLA);
        elementos.add(new Calles());
        casas(rng);
        capillaY = sueloCaja(NAVE.grow(1));
        elementos.add(new Capilla(rng.fork(), capillaY));
        ocupar(NAVE.grow(2));
        ocupar(TORRE.grow(1));
        cementerio(rng.fork());
        for (int i = 0; i < CAMPOS.length; i++) campo(CAMPOS[i], i);
        plaza(rng.fork());
        farolas(rng.fork());
        puertas();
        carros(rng);
        arboles(rng);
        elementos.add(new Puente(rng.fork()));
        vado();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(clave(x, z), k -> new ArrayList<>()).add(e);
    }

    static long clave(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) ^ (chunkZ & 0xffffffffL);
    }

    /** Si el chunk tiene algo de Velmor (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en lo de Velmor: ahi no se ponen arboles ni rocas sueltos. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= X0 && x0 <= X1 && z0 + 15 >= Z0 && z0 <= Z1;
    }

    /** Si (x, z) esta dentro del pueblo (para el nombre del sitio). */
    public static boolean dentro(double x, double z) {
        return x >= PX0 && x <= PX1 && z >= PZ0 && z <= PZ1;
    }

    /** Lo dibuja en el chunk que se esta adornando. */
    static void chunk(WorldGenLevel nivel, ChunkAccess c) {
        int cx = c.getPos().x, cz = c.getPos().z;
        get().construir(nivel, cx, cz, RandomSource.create(clave(cx, cz) ^ SEMILLA));
    }

    private final java.util.Set<String> avisados = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public void construir(WorldGenLevel nivel, int chunkX, int chunkZ, RandomSource r) {
        List<Element> l = indice.get(clave(chunkX, chunkZ));
        if (l == null) return;
        Canvas c = new Canvas(nivel, chunkX, chunkZ, r);
        for (Element e : l) {
            try {
                e.build(c);
            } catch (Throwable t) {
                String k = e.getClass().getSimpleName();
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} de Velmor en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public java.util.Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los sitios con nombre (el atril, la campana, el pozo, las puertas...): {x, y, z} del bloque (y: lo que se pisa). */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    private void sitio(String nombre, int x, int y, int z) {
        sitios.put(nombre, new int[]{x, y, z});
    }

    private void ocupar(Rect r) {
        ocupado.add(r);
    }

    private boolean libre(int x, int z, int margen) {
        for (Rect r : ocupado)
            if (x >= r.x0 - margen && x <= r.x1 + margen && z >= r.z0 - margen && z <= r.z1 + margen) return false;
        if (enPlaza(x, z, margen)) return false;
        return alto(x, z) >= Terreno.AGUA + 2;
    }

    /** Lo mas alto del terreno en una caja, mas uno: el suelo de lo que se levanta encima. */
    private static int sueloCaja(Rect r) {
        int a = Integer.MIN_VALUE;
        for (int x = r.x0; x <= r.x1; x++)
            for (int z = r.z0; z <= r.z1; z++) a = Math.max(a, alto(x, z));
        return a + 1;
    }

    private void trozo(int capa, int x0, int y0, int z0, int x1, int y1, int z1, Trozo.Obra o) {
        elementos.add(new Trozo(capa, x0, y0, z0, x1, y1, z1, o));
    }

    // ------------------------------------------------------------------ las calles

    static double distSeg(double x, double z, double ax, double az, double bx, double bz) {
        double vx = bx - ax, vz = bz - az, l2 = vx * vx + vz * vz;
        double t = l2 <= 0 ? 0 : Math.max(0, Math.min(1, ((x - ax) * vx + (z - az) * vz) / l2));
        return Math.hypot(ax + vx * t - x, az + vz * t - z);
    }

    /** -1 si (x, z) no esta en ninguna calle; si no, el tipo de la calle (0 empedrada, 1 de tierra). */
    static int calle(int x, int z) {
        int tipo = -1;
        double borde = (Hash.pct(x, 7, z, SEMILLA + 1) % 3 - 1) * 0.35;
        for (double[] c : CALLES) {
            double d = distSeg(x, z, c[0], c[1], c[2], c[3]);
            if (d <= c[4] + borde) {
                int t = (int) c[5];
                if (tipo < 0 || t < tipo) tipo = t;
            }
        }
        return tipo;
    }

    static boolean enPlaza(int x, int z, int margen) {
        return Math.hypot(x - X, z - Z) <= PLAZA_R + 0.4 + margen;
    }

    /** El suelo de las calles, de la plaza y de los caminos: empedrado roto, tierra pisada y grava. */
    private final class Calles extends Element {
        Calles() {
            super(SEMILLA + 2);
            this.layer = 1;
            bounds(X0, 50, Z0, X1, 90, Z1);
        }

        @Override
        public void build(Canvas c) {
            for (int x = c.minX; x <= c.maxX; x++)
                for (int z = c.minZ; z <= c.maxZ; z++) {
                    if (x < X0 || x > X1 || z < Z0 || z > Z1) continue;
                    boolean plaza = enPlaza(x, z, 0);
                    int tipo = plaza ? 2 : calle(x, z);
                    if (tipo < 0) continue;
                    int top = alto(x, z);
                    if (top < Terreno.AGUA) continue;
                    int p = Hash.pct(x, top, z, seed);
                    BlockState s;
                    if (tipo == 2) s = p < 34 ? Pal.b(Blocks.COBBLESTONE) : p < 52 ? Pal.STONE_BRICKS : p < 66 ? Pal.b(Blocks.MOSSY_COBBLESTONE)
                            : p < 78 ? Pal.ANDESITE : p < 88 ? Pal.b(Blocks.CRACKED_STONE_BRICKS) : p < 95 ? Pal.GRAVEL : Pal.COARSE_DIRT;
                    else if (tipo == 0) s = p < 30 ? Pal.b(Blocks.COBBLESTONE) : p < 46 ? Pal.b(Blocks.MOSSY_COBBLESTONE) : p < 62 ? Pal.GRAVEL
                            : p < 76 ? Pal.COARSE_DIRT : p < 86 ? Pal.ANDESITE : p < 94 ? Pal.b(Blocks.DIRT_PATH) : Pal.STONE_BRICKS;
                    else s = p < 46 ? Pal.b(Blocks.DIRT_PATH) : p < 70 ? Pal.COARSE_DIRT : p < 86 ? Pal.GRAVEL : Pal.ROOTED_DIRT;
                    c.set(x, top, z, s);
                    // fuera la hierba y los matojos de encima
                    for (int k = 1; k <= 2; k++) if (!c.isAir(x, top + k, z)) c.set(x, top + k, z, Pal.AIR);
                }
        }
    }

    // ------------------------------------------------------------------ las casas

    /** Las casas de Velmor: las de Brumavela con barro, tierra y madera vieja (aqui no hay cal). */
    static final class CasaVelmor extends Casa {
        CasaVelmor(long seed, Rect fp, Direction frente, int plantas, int ruina, Estilo estilo, int tejado, Tipo tipo) {
            super(seed, Velmor::alto, fp, frente, plantas, ruina, estilo, tejado, tipo);
        }

        @Override
        protected BlockState relleno(int x, int yy, int z) {
            if (estilo != Estilo.ENTRAMADO) return super.relleno(x, yy, z);
            int p = Hash.pct(x, yy, z, seed + 2);
            if (p < 42) return Pal.b(Blocks.PACKED_MUD);
            if (p < 60) return Pal.b(Blocks.MUD_BRICKS);
            if (p < 74) return Pal.b(Blocks.LIGHT_GRAY_TERRACOTTA);
            if (p < 86) return Pal.TUFF;
            return Pal.SP_PLANKS;
        }

        @Override
        protected BlockState zocalo(int x, int yy, int z) {
            int p = Hash.pct(x, yy, z, seed + 1);
            if (p < 36) return Pal.b(Blocks.COBBLESTONE);
            if (p < 56) return Pal.b(Blocks.MOSSY_COBBLESTONE);
            if (p < 72) return Pal.TUFF;
            if (p < 86) return Pal.STONE_BRICKS;
            return Pal.ANDESITE;
        }
    }

    private Casa casa(Rng rng, Rect fp, Direction frente, int plantas, int ruina, Casa.Estilo estilo, int tejado, Casa.Tipo tipo, int arcones) {
        Casa k = new CasaVelmor(rng.fork(), fp, frente, plantas, ruina, estilo, tejado, tipo);
        // arcones: 0 ninguno, 1 los normales, 2 tambien el bueno
        k.botin(arcones >= 1 ? BOTIN : null, arcones >= 2 ? BOTIN_BUENO : null);
        elementos.add(k);
        casas.add(k);
        ocupar(fp.grow(2));
        return k;
    }

    private static Rect R(int x0, int z0, int x1, int z1) {
        return new Rect(x0, z0, x1, z1);
    }

    private void casas(Rng rng) {
        final Casa.Estilo E = Casa.Estilo.ENTRAMADO, P = Casa.Estilo.PIEDRA, M = Casa.Estilo.MADERA;
        final Casa.Tipo H = Casa.Tipo.HOGAR;
        final Direction ES = Direction.EAST, OE = Direction.WEST, SU = Direction.SOUTH, NO = Direction.NORTH;
        // la calle mayor, al oeste (miran al este)
        casa(rng, R(834, 3938, 844, 3948), ES, 1, 1, E, 0, H, 1);
        casa(rng, R(833, 3954, 844, 3965), ES, 2, 0, E, 1, H, 1);
        casa(rng, R(835, 3971, 844, 3980), ES, 1, 2, P, 0, H, 0);
        taberna = casa(rng, R(832, 3986, 844, 3998), ES, 2, 1, E, 2, Casa.Tipo.TABERNA, 2);
        casa(rng, R(834, 4005, 844, 4016), ES, 1, 2, M, 0, H, 0);
        casa(rng, R(833, 4058, 844, 4069), ES, 1, 1, P, 1, H, 1);
        casa(rng, R(834, 4075, 844, 4085), ES, 2, 0, E, 0, H, 1);
        casa(rng, R(832, 4091, 844, 4103), ES, 1, 2, E, 0, H, 0);
        casa(rng, R(835, 4109, 844, 4118), ES, 1, 1, M, 1, H, 1);
        casa(rng, R(833, 4124, 844, 4135), ES, 1, 2, P, 0, H, 0);
        // la calle mayor, al este (miran al oeste)
        casa(rng, R(856, 3940, 866, 3950), OE, 1, 1, P, 2, H, 1);
        casa(rng, R(856, 3956, 868, 3968), OE, 2, 1, E, 1, H, 0);
        casa(rng, R(856, 3974, 865, 3983), OE, 1, 2, E, 0, H, 0);
        herreria = casa(rng, R(856, 3989, 868, 4000), OE, 1, 1, P, 2, Casa.Tipo.HERRERIA, 2);
        casa(rng, R(856, 4006, 866, 4017), OE, 1, 2, M, 0, H, 0);
        granero = casa(rng, R(856, 4057, 868, 4070), OE, 1, 1, M, 0, Casa.Tipo.ALMACEN, 1);
        casa(rng, R(856, 4076, 866, 4086), OE, 2, 1, E, 0, H, 1);
        casa(rng, R(856, 4092, 865, 4102), OE, 1, 2, P, 0, H, 0);
        casa(rng, R(856, 4108, 867, 4119), OE, 1, 0, E, 2, H, 1);
        casa(rng, R(856, 4125, 866, 4136), OE, 1, 2, E, 0, H, 0);
        // la calle del puente, al norte (miran al sur)
        casa(rng, R(806, 4024, 817, 4035), SU, 2, 1, E, 1, H, 1);
        casa(rng, R(786, 4025, 796, 4035), SU, 1, 2, P, 0, H, 0);
        casa(rng, R(764, 4024, 775, 4035), SU, 1, 2, E, 0, H, 0);
        casa(rng, R(880, 4024, 891, 4035), SU, 1, 1, E, 0, H, 1);
        alcaldia = casa(rng, R(900, 4023, 912, 4035), SU, 2, 0, P, 2, Casa.Tipo.CAPITANIA, 2);
        casa(rng, R(920, 4024, 930, 4035), SU, 1, 1, M, 1, H, 0);
        casa(rng, R(940, 4025, 949, 4035), SU, 1, 2, P, 0, H, 0);
        // la calle del puente, al sur (miran al norte)
        casa(rng, R(806, 4045, 817, 4055), NO, 1, 1, P, 0, H, 1);
        casa(rng, R(784, 4045, 796, 4057), NO, 1, 2, E, 0, H, 0);
        casa(rng, R(764, 4046, 774, 4056), NO, 1, 2, M, 0, H, 0);
        casa(rng, R(880, 4045, 892, 4056), NO, 1, 0, E, 1, H, 1);
        casa(rng, R(898, 4045, 908, 4055), NO, 1, 2, P, 0, H, 0);
        casa(rng, R(922, 4046, 932, 4056), NO, 1, 1, E, 0, H, 0);
    }

    // ------------------------------------------------------------------ la capilla

    /**
     * La capilla de Velmor: la nave sin tejado (quedan tres cerchas), la esquina del noreste caida, los bancos rotos, el
     * altar con sus velas apagadas y el atril con el libro de su campanero; el campanario al suroeste, en pie, con su campana
     * y un arcon arriba (se sube por la escalera de mano).
     */
    private final class Capilla extends Element {
        final int g;

        Capilla(long seed, int g) {
            super(seed);
            this.g = g;
            this.layer = 3;
            bounds(TORRE.x0 - 1, g - 6, NAVE.z0 - 1, NAVE.x1 + 2, g + 28, NAVE.z1 + 5);
            sitio("capilla", 850, g, 3929);
            sitio("atril", 846, g + 1, 3904);
            sitio("altar", 850, g + 1, 3903);
            sitio("campana", 837, g + 19, 3925);
            sitio("cofre_campanario", 838, g + 17, 3926);
            sitio("cofre_sacristia", 858, g + 1, 3903);
        }

        private BlockState piedra(int x, int y, int z) {
            return Mat.sillar(x, y, z, seed);
        }

        /** Hasta donde queda el muro de la nave en esa columna (la esquina del noreste se vino abajo). */
        private int cima(int x, int z) {
            int alto = g + 9;
            double d = Math.hypot(x - (NAVE.x1 + 1), z - (NAVE.z0 - 1));
            if (d < 9) alto = g - 1 + (int) Math.round(d * 1.1) - Hash.pct(x, 1, z, seed + 3) % 2;
            else if (Hash.pct(x, 2, z, seed + 4) < 14) alto -= 1 + Hash.pct(x, 3, z, seed + 4) % 2;
            return alto;
        }

        @Override
        public void build(Canvas c) {
            // los cimientos y el suelo (de losas, con escombros)
            for (int x = NAVE.x0; x <= NAVE.x1; x++)
                for (int z = NAVE.z0; z <= NAVE.z1; z++) {
                    for (int y = alto(x, z) - 1; y <= g - 2; y++) c.set(x, y, z, piedra(x, y, z));
                    c.set(x, g - 1, z, Mat.losas(x, g - 1, z, seed));
                    c.column(x, z, g, g + 22, Pal.AIR);
                }
            // el atrio, delante
            for (int x = 845; x <= 855; x++)
                for (int z = NAVE.z1 + 1; z <= NAVE.z1 + 4; z++) {
                    for (int y = alto(x, z) - 1; y <= g - 2; y++) c.set(x, y, z, piedra(x, y, z));
                    c.set(x, g - 1, z, Mat.losas(x, g - 1, z, seed + 1));
                    c.column(x, z, g, g + 3, Pal.AIR);
                }
            muros(c);
            contrafuertes(c);
            fachada(c);
            dentro(c);
            torre(c);
        }

        private void muros(Canvas c) {
            for (int x = NAVE.x0; x <= NAVE.x1; x++)
                for (int z = NAVE.z0; z <= NAVE.z1; z++) {
                    boolean borde = x == NAVE.x0 || x == NAVE.x1 || z == NAVE.z0 || z == NAVE.z1;
                    if (!borde) continue;
                    int top = cima(x, z);
                    for (int y = g - 1; y <= top; y++) c.set(x, y, z, piedra(x, y, z));
                    // las ventanas de los lados: altas y estrechas, con lo que queda de sus rejas
                    boolean lado = x == NAVE.x0 || x == NAVE.x1;
                    if (lado && (z == 3905 || z == 3911 || z == 3917 || (z == 3923 && x == NAVE.x1)))
                        for (int y = g + 3; y <= g + 6 && y <= top; y++)
                            c.set(x, y, z, Hash.pct(x, y, z, seed + 5) < 40 ? Pal.IRON_BARS : Pal.AIR);
                    // el ventanal del fondo
                    if (z == NAVE.z0 && x >= 848 && x <= 852 && top >= g + 3)
                        for (int y = g + 3; y <= g + 8 && y <= top; y++) {
                            boolean parteluz = x == 850;
                            c.set(x, y, z, parteluz ? piedra(x, y, z) : Hash.pct(x, y, z, seed + 6) < 30 ? Pal.b(Blocks.RED_STAINED_GLASS_PANE) : Pal.AIR);
                        }
                }
            // la cornisa rota encima de lo que queda en pie
            for (int x = NAVE.x0; x <= NAVE.x1; x++)
                for (int z = NAVE.z0; z <= NAVE.z1; z++) {
                    boolean borde = x == NAVE.x0 || x == NAVE.x1 || z == NAVE.z0 || z == NAVE.z1;
                    if (!borde || cima(x, z) < g + 9) continue;
                    if (Hash.pct(x, 4, z, seed + 7) < 55) c.set(x, g + 10, z, Pal.slab(Blocks.STONE_BRICK_SLAB, false));
                }
        }

        private void contrafuertes(Canvas c) {
            for (int z : new int[]{3904, 3910, 3916, 3922})
                for (int lado = 0; lado < 2; lado++) {
                    int x = lado == 0 ? NAVE.x0 - 1 : NAVE.x1 + 1;
                    if (lado == 0 && z >= TORRE.z0 - 1) continue;
                    int top = Math.min(g + 6, cima(lado == 0 ? NAVE.x0 : NAVE.x1, z) - 1);
                    for (int y = alto(x, z) - 1; y <= top; y++) c.set(x, y, z, piedra(x, y, z));
                    if (top >= g + 4) c.set(x, top + 1, z, Pal.stairs(Blocks.STONE_BRICK_STAIRS, lado == 0 ? Direction.EAST : Direction.WEST, false));
                }
        }

        private void fachada(Canvas c) {
            int z = NAVE.z1;
            // el hastial: sube en escalones hasta la punta (y se corta arriba)
            for (int x = NAVE.x0; x <= NAVE.x1; x++) {
                int k = Math.min(x - NAVE.x0, NAVE.x1 - x);
                int top = Math.min(g + 10 + k, g + 17);
                for (int y = g + 9; y <= top; y++) c.set(x, y, z, piedra(x, y, z));
                if (top < g + 17) c.set(x, top + 1, z, Pal.stairs(Blocks.STONE_BRICK_STAIRS, x < 850 ? Direction.EAST : Direction.WEST, false));
            }
            c.set(850, g + 18, z, Pal.b(Blocks.CHISELED_STONE_BRICKS));
            c.set(850, g + 19, z, Pal.SOUL_LANTERN);
            // la puerta (sin hoja) con su arco
            for (int x = 849; x <= 851; x++)
                for (int y = g; y <= g + 3; y++) c.set(x, y, z, Pal.AIR);
            c.set(849, g + 3, z, Pal.stairs(Blocks.STONE_BRICK_STAIRS, Direction.EAST, true));
            c.set(851, g + 3, z, Pal.stairs(Blocks.STONE_BRICK_STAIRS, Direction.WEST, true));
            c.set(850, g + 4, z, Pal.b(Blocks.CHISELED_STONE_BRICKS));
            // el roseton: un anillo de vidrieras rojas rotas
            for (int dx = -2; dx <= 2; dx++)
                for (int dy = -2; dy <= 2; dy++) {
                    double d = Math.hypot(dx, dy);
                    if (d > 2.3) continue;
                    int x = 850 + dx, y = g + 8 + dy;
                    if (d < 0.5) c.set(x, y, z, Pal.b(Blocks.CHISELED_STONE_BRICKS));
                    else c.set(x, y, z, Hash.pct(x, y, z, seed + 8) < 45 ? Pal.b(Blocks.RED_STAINED_GLASS_PANE) : Pal.AIR);
                }
        }

        private void dentro(Canvas c) {
            // el presbiterio, un escalon mas alto
            for (int x = NAVE.x0 + 1; x <= NAVE.x1 - 1; x++)
                for (int z = NAVE.z0 + 1; z <= NAVE.z0 + 5; z++) c.set(x, g, z, Mat.losas(x, g, z, seed + 2));
            for (int x = 846; x <= 854; x++) c.set(x, g, NAVE.z0 + 6, Pal.stairs(Blocks.STONE_BRICK_STAIRS, Direction.NORTH, false));
            // el altar, el retablo y las velas apagadas
            for (int x = 848; x <= 852; x++) c.set(x, g + 1, 3903, Pal.b(Blocks.POLISHED_BLACKSTONE_BRICKS));
            c.set(848, g + 2, 3903, Mat.velas(3));
            c.set(852, g + 2, 3903, Mat.velas(2));
            c.set(850, g + 2, 3903, Mat.calavera(8));
            for (int y = g + 1; y <= g + 5; y++) c.set(850, y, 3902, Pal.b(Blocks.CHISELED_STONE_BRICKS));
            c.set(850, g + 6, 3902, Pal.SOUL_LANTERN);
            // el atril con el libro del campanero
            c.lectern(846, g + 1, 3904, Direction.SOUTH, Perdidos.libro(18));
            // el arcon de la sacristia, entre los escombros del noreste
            c.chest(858, g + 1, 3903, Direction.WEST, BOTIN_BUENO);
            // la alfombra del pasillo, comida a trozos
            for (int z = NAVE.z0 + 7; z <= NAVE.z1 - 1; z++)
                for (int x = 849; x <= 851; x++)
                    if (Hash.pct(x, 5, z, seed + 9) < 62) c.set(x, g, z, Pal.b(Blocks.RED_CARPET));
            // los bancos: de cara al altar, muchos rotos o caidos
            for (int z = 3910; z <= 3922; z += 3)
                for (int x = NAVE.x0 + 2; x <= NAVE.x1 - 2; x++) {
                    if (x >= 848 && x <= 852) continue;
                    int p = Hash.pct(x, 6, z, seed + 10);
                    if (p < 22) continue;
                    c.set(x, g, z, p < 36 ? Pal.slab(Blocks.SPRUCE_SLAB, false) : Pal.stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH, false));
                }
            // escombros de la esquina caida, telaranas y huesos
            for (int x = NAVE.x0 + 1; x <= NAVE.x1 - 1; x++)
                for (int z = NAVE.z0 + 1; z <= NAVE.z1 - 1; z++) {
                    double d = Math.hypot(x - (NAVE.x1 + 1), z - (NAVE.z0 - 1));
                    int p = Hash.pct(x, 7, z, seed + 11);
                    if (d < 8 && p < 70 - d * 7 && !(x == 858 && z == 3903)) {
                        int h = 1 + (d < 4 ? p % 3 : p % 2);
                        int base = z <= NAVE.z0 + 5 ? g + 1 : g;
                        for (int k = 0; k < h; k++)
                            c.set(x, base + k, z, k == h - 1 && p % 3 == 0 ? Pal.slab(Blocks.COBBLESTONE_SLAB, false)
                                    : p % 4 == 0 ? Pal.GRAVEL : Mat.sillar(x, base + k, z, seed));
                    } else if (p > 97) c.set(x, z <= NAVE.z0 + 5 ? g + 1 : g, z, Mat.calavera(p));
                    else if (p > 94) c.set(x, z <= NAVE.z0 + 5 ? g + 1 : g, z, Pal.b(Blocks.BONE_BLOCK));
                }
            for (int[] t : new int[][]{{NAVE.x0 + 1, NAVE.z1 - 1}, {NAVE.x0 + 1, NAVE.z0 + 1}, {NAVE.x1 - 1, NAVE.z1 - 1}})
                for (int y = g + 6; y <= g + 8; y++) if (Hash.pct(t[0], y, t[1], seed + 12) < 60) c.set(t[0], y, t[1], Pal.COBWEB);
            // las tres cerchas que quedan, rotas, y una lampara colgando de la del medio
            for (int z : new int[]{3908, 3914, 3920})
                for (int x = NAVE.x0 + 1; x <= NAVE.x1 - 1; x++) {
                    if (Math.hypot(x - (NAVE.x1 + 1), z - (NAVE.z0 - 1)) < 11) continue;
                    if (Hash.pct(x, 8, z, seed + 13) < 18 + (z == 3908 ? 30 : 0)) continue;
                    c.set(x, g + 9, z, Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.X));
                }
            Mat.farolColgado(c, 850, g + 8, 3914, 2);
        }

        private void torre(Canvas c) {
            Rect t = TORRE;
            // el campanario: 22 de alto (la nave, 10), el suelo de las campanas a g + 16 y el chapitel encima
            int arriba = g + 21, suelo = g + 16;
            for (int x = t.x0; x <= t.x1; x++)
                for (int z = t.z0; z <= t.z1; z++) {
                    boolean borde = x == t.x0 || x == t.x1 || z == t.z0 || z == t.z1;
                    for (int y = alto(x, z) - 1; y <= g - 1; y++) c.set(x, y, z, piedra(x, y, z));
                    if (!borde) {
                        c.column(x, z, g, arriba - 1, Pal.AIR);
                        c.set(x, suelo, z, Mat.tabla(x, suelo, z, seed));
                        c.set(x, arriba, z, piedra(x, arriba, z));
                        continue;
                    }
                    for (int y = g; y <= arriba; y++) c.set(x, y, z, piedra(x, y, z));
                    // las troneras del campanario: dos huecos en cada lado, y una saetera a media altura
                    boolean centroX = x == t.x0 + 2 || x == t.x0 + 3, centroZ = z == t.z0 + 2 || z == t.z0 + 3;
                    if ((z == t.z0 || z == t.z1) && centroX || (x == t.x0 || x == t.x1) && centroZ) {
                        for (int y = suelo + 1; y <= suelo + 3; y++) c.set(x, y, z, Pal.AIR);
                        if (x == t.x0 + 2 && z == t.z1 || z == t.z0 + 2 && x == t.x0) c.set(x, g + 9, z, Pal.AIR);
                    }
                }
            // el chapitel de pizarra, con su remate
            for (int k = 0; k <= 2; k++) {
                int y = arriba + 1 + k;
                for (int x = t.x0 + k; x <= t.x1 - k; x++)
                    for (int z = t.z0 + k; z <= t.z1 - k; z++) {
                        boolean borde = x == t.x0 + k || x == t.x1 - k || z == t.z0 + k || z == t.z1 - k;
                        if (!borde && k < 2) continue;
                        Direction cae = x == t.x0 + k ? Direction.EAST : x == t.x1 - k ? Direction.WEST : z == t.z0 + k ? Direction.SOUTH : Direction.NORTH;
                        boolean esquina = (x == t.x0 + k || x == t.x1 - k) && (z == t.z0 + k || z == t.z1 - k);
                        c.set(x, y, z, k == 2 || esquina ? Pal.DS_TILES : Pal.stairs(Blocks.DEEPSLATE_TILE_STAIRS, cae, false));
                    }
            }
            c.set(836, arriba + 4, 3924, Pal.DS_TILES);
            c.set(836, arriba + 5, 3924, Pal.b(Blocks.STONE_BRICK_WALL));
            c.set(836, arriba + 6, 3924, Pal.SOUL_LANTERN);
            // la puerta desde la nave (los dos muros pegados)
            for (int y = g; y <= g + 1; y++) {
                c.set(t.x1, y, 3925, Pal.AIR);
                c.set(NAVE.x0, y, 3925, Pal.AIR);
            }
            // la escalera de mano, pegada al muro del oeste, hasta el hueco del suelo de las campanas
            for (int y = g; y <= suelo; y++) c.set(t.x0 + 1, y, 3924, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST));
            // la campana, colgada de su viga
            for (int x = t.x0 + 1; x <= t.x1 - 1; x++) c.set(x, suelo + 4, 3925, Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.X));
            c.set(837, suelo + 3, 3925, Blocks.BELL.defaultBlockState().setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING)
                    .setValue(BellBlock.FACING, Direction.NORTH));
            c.chest(838, suelo + 1, 3926, Direction.WEST, BOTIN_BUENO);
            c.set(835, suelo + 1, 3926, Mat.calavera(3));
            c.set(838, suelo + 3, 3923, Pal.COBWEB);
            c.set(835, g + 6, 3926, Pal.COBWEB);
        }
    }

    // ------------------------------------------------------------------ el cementerio y el panteon

    private BlockState lapida(int x, int z, long s) {
        int p = Hash.pct(x, 3, z, s);
        if (p < 30) return Pal.b(Blocks.COBBLESTONE_WALL);
        if (p < 55) return Pal.b(Blocks.MOSSY_COBBLESTONE_WALL);
        if (p < 75) return Pal.b(Blocks.STONE_BRICK_WALL);
        if (p < 90) return Pal.stairs(Blocks.STONE_BRICK_STAIRS, Direction.NORTH, false);
        return Pal.b(Blocks.MOSSY_STONE_BRICK_WALL);
    }

    private void cementerio(long s) {
        Rect r = CEMENTERIO;
        ocupar(r);
        sitio("cementerio", r.x1 - 3, pie(r.x1 - 3, 3926), 3926);
        sitio("panteon", PANTEON.x0 + 2, sueloCaja(PANTEON), PANTEON.cz());
        trozo(4, r.x0 - 1, 50, r.z0 - 1, r.x1 + 1, 100, r.z1 + 1, c -> {
            // la tapia baja, caida a trozos, con la cancela al este
            for (int x = r.x0; x <= r.x1; x++)
                for (int z = r.z0; z <= r.z1; z++) {
                    boolean borde = x == r.x0 || x == r.x1 || z == r.z0 || z == r.z1;
                    if (!borde) continue;
                    if (x == r.x1 && (z == 3928 || z == 3929)) continue;
                    int y = pie(x, z), p = Hash.pct(x, 1, z, s + 1);
                    boolean pilar = x == r.x1 && (z == 3927 || z == 3930);
                    if (pilar) {
                        c.set(x, y, z, Pal.STONE_BRICKS);
                        c.set(x, y + 1, z, Pal.b(Blocks.CHISELED_STONE_BRICKS));
                        c.set(x, y + 2, z, z == 3927 ? Pal.SOUL_LANTERN : Pal.slab(Blocks.STONE_BRICK_SLAB, false));
                        continue;
                    }
                    if (p < 16) continue;
                    c.unir(x, y, z, Pal.b(p < 50 ? Blocks.COBBLESTONE_WALL : p < 80 ? Blocks.MOSSY_COBBLESTONE_WALL : Blocks.STONE_BRICK_WALL));
                }
            // las tumbas en hileras; algunas abiertas (la tierra al lado y un craneo en el fondo)
            for (int z = r.z0 + 3; z <= r.z1 - 4; z += 5)
                for (int x = r.x0 + 3; x <= r.x1 - 3; x += 3) {
                    if (x >= PANTEON.x0 - 1 && x <= PANTEON.x1 + 1 && z >= PANTEON.z0 - 1 && z <= PANTEON.z1 + 2) continue;
                    if ((x == 822 && z == 3913) || (x == 828 && z == 3923)) continue;
                    int p = Hash.pct(x, 2, z, s + 2);
                    if (p < 12) continue;
                    int y = pie(x, z);
                    c.set(x, y, z, lapida(x, z, s + 3));
                    if (p > 82) {
                        // abierta
                        for (int k = 1; k <= 2; k++) {
                            int yy = pie(x, z + k);
                            c.set(x, yy - 1, z + k, Pal.AIR);
                            c.set(x, yy - 2, z + k, Pal.AIR);
                            c.set(x, yy - 3, z + k, Pal.COARSE_DIRT);
                        }
                        c.set(x, pie(x, z + 1) - 2, z + 1, Mat.calavera(p));
                        c.set(x + 1, pie(x + 1, z + 1), z + 1, Pal.COARSE_DIRT);
                        c.set(x + 1, pie(x + 1, z + 2), z + 2, Pal.slab(Blocks.MUD_BRICK_SLAB, false));
                    } else {
                        c.set(x, pie(x, z + 1) - 1, z + 1, p < 50 ? Pal.COARSE_DIRT : Pal.PODZOL);
                        c.set(x, pie(x, z + 2) - 1, z + 2, p < 50 ? Pal.COARSE_DIRT : Pal.PODZOL);
                        if (p > 70) c.set(x, pie(x, z + 1), z + 1, Pal.b(p > 76 ? Blocks.WITHER_ROSE : Blocks.DEAD_BUSH));
                        else if (p < 20) c.set(x, pie(x, z + 1), z + 1, Mat.velas(1));
                    }
                }
            panteon(c, s + 4);
        });
        elementos.add(new Landscape.DeadTree(s + 5, 822, pie(822, 3913), 3913, true).sinHojas());
        elementos.add(new Landscape.DeadTree(s + 6, 828, pie(828, 3923), 3923, false).sinHojas());
    }

    /** El panteon de la familia que fundo Velmor: piedra negra, la puerta de reja rota, la tumba y un arcon. */
    private void panteon(Canvas c, long s) {
        Rect r = PANTEON;
        int y = sueloCaja(r);
        for (int x = r.x0; x <= r.x1; x++)
            for (int z = r.z0; z <= r.z1; z++) {
                boolean borde = x == r.x0 || x == r.x1 || z == r.z0 || z == r.z1;
                for (int yy = alto(x, z) - 1; yy <= y - 1; yy++) c.set(x, yy, z, Mat.cripta(x, yy, z, s));
                if (!borde) {
                    c.column(x, z, y, y + 3, Pal.AIR);
                    c.set(x, y + 4, z, Pal.DS_TILES);
                } else for (int yy = y; yy <= y + 4; yy++) c.set(x, yy, z, Mat.cripta(x, yy, z, s));
                // el tejadillo
                boolean esquina = (x == r.x0 || x == r.x1) && (z == r.z0 || z == r.z1);
                c.set(x, y + 5, z, esquina ? Pal.b(Blocks.DEEPSLATE_TILE_WALL) : Pal.slab(Blocks.DEEPSLATE_TILE_SLAB, false));
            }
        // la puerta al este, con lo que queda de la reja
        for (int z = 3902; z <= 3903; z++)
            for (int yy = y; yy <= y + 2; yy++) c.set(r.x1, yy, z, yy == y + 2 && z == 3903 ? Pal.IRON_BARS : Pal.AIR);
        c.set(r.x1, y + 3, 3902, Pal.b(Blocks.CHISELED_DEEPSLATE));
        // la tumba de piedra con sus velas, el arcon y un farol colgado
        c.set(811, y, 3902, Pal.b(Blocks.POLISHED_DEEPSLATE));
        c.set(812, y, 3902, Pal.b(Blocks.POLISHED_DEEPSLATE));
        c.set(811, y + 1, 3902, Pal.slab(Blocks.POLISHED_DEEPSLATE_SLAB, false));
        c.set(812, y + 1, 3902, Mat.velas(2));
        c.chest(809, y, 3904, Direction.EAST, BOTIN_BUENO);
        c.set(809, y, 3900, Mat.calavera(6));
        Mat.farolColgado(c, 812, y + 3, 3904, 1);
        c.set(r.x0 + 1, y + 3, r.z0 + 1, Pal.COBWEB);
    }

    // ------------------------------------------------------------------ los campos

    private void campo(Rect r, int n) {
        ocupar(r);
        long s = SEMILLA + 40 + n;
        trozo(1, r.x0, 50, r.z0, r.x1, 100, r.z1, c -> {
            for (int x = r.x0; x <= r.x1; x++)
                for (int z = r.z0; z <= r.z1; z++) {
                    int top = alto(x, z);
                    if (top < Terreno.AGUA + 1) continue;
                    boolean borde = x == r.x0 || x == r.x1 || z == r.z0 || z == r.z1;
                    int p = Hash.pct(x, 4, z, s);
                    for (int k = 1; k <= 2; k++) if (!c.isAir(x, top + k, z)) c.set(x, top + k, z, Pal.AIR);
                    if (borde) {
                        // la cerca, caida a trozos
                        c.set(x, top, z, Pal.COARSE_DIRT);
                        if (p >= 38) c.unir(x, top + 1, z, Pal.SP_FENCE);
                        continue;
                    }
                    boolean surco = (n == 0 ? x : z) % 2 == 0;
                    if (surco) {
                        c.set(x, top, z, Pal.COARSE_DIRT);
                        continue;
                    }
                    // el trigo seco (o nada: un matojo)
                    if (p < 62) {
                        c.set(x, top, z, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 0));
                        c.set(x, top + 1, z, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 2 + p % 6));
                    } else {
                        c.set(x, top, z, p < 80 ? Pal.COARSE_DIRT : Pal.ROOTED_DIRT);
                        if (p > 88) c.set(x, top + 1, z, Pal.b(Blocks.DEAD_BUSH));
                    }
                }
            // las gavillas que nadie recogio y el espantapajaros
            for (int k = 0; k < 4; k++) {
                int x = r.x0 + 3 + Hash.pct(k, 5, n, s) % (r.w() - 6), z = r.z0 + 3 + Hash.pct(k, 6, n, s) % (r.d() - 6);
                int top = alto(x, z);
                c.set(x, top + 1, z, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Y));
                if (k % 2 == 0) c.set(x, top + 2, z, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.X));
            }
            int ex = r.cx(), ez = r.cz(), et = alto(ex, ez);
            c.set(ex, et, ez, Pal.COARSE_DIRT);
            c.set(ex, et + 1, ez, Pal.SP_FENCE);
            c.set(ex, et + 2, ez, Pal.SP_FENCE);
            c.set(ex, et + 3, ez, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Y));
            c.unir(ex - 1, et + 3, ez, Pal.SP_FENCE);
            c.unir(ex + 1, et + 3, ez, Pal.SP_FENCE);
            c.set(ex, et + 4, ez, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, n == 0 ? Direction.EAST : Direction.NORTH));
        });
    }

    // ------------------------------------------------------------------ la plaza: el pozo, la horca y un puesto

    private void plaza(long s) {
        int y = pie(X, Z);
        sitio("pozo", X, y, Z);
        sitio("horca", 857, pie(857, 4032), 4032);
        ocupar(new Rect(X - 2, Z - 2, X + 2, Z + 2));
        trozo(4, X - 2, y - 8, Z - 2, X + 2, y + 5, Z + 2, c -> {
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++) {
                    boolean centro = dx == 0 && dz == 0, esquina = dx != 0 && dz != 0;
                    if (centro) {
                        for (int yy = y - 5; yy <= y + 2; yy++) c.set(X, yy, Z, Pal.AIR);
                        c.set(X, y - 6, Z, Pal.WATER);
                        c.set(X, y - 7, Z, Pal.b(Blocks.MOSSY_COBBLESTONE));
                        continue;
                    }
                    for (int yy = y - 6; yy <= y; yy++) c.set(X + dx, yy, Z + dz, Mat.muro(X + dx, yy, Z + dz, s));
                    if (esquina) {
                        c.set(X + dx, y + 1, Z + dz, Pal.DO_FENCE);
                        c.set(X + dx, y + 2, Z + dz, Pal.DO_FENCE);
                    }
                    c.set(X + dx, y + 3, Z + dz, Hash.pct(X + dx, y, Z + dz, s + 1) < 30 ? Pal.AIR : Pal.slab(Blocks.DARK_OAK_SLAB, false));
                }
            c.set(X, y + 3, Z, Pal.DO_PLANKS);
            c.set(X, y + 2, Z, Pal.CHAIN);
            c.set(X, y + 1, Z, Pal.CHAIN);
            c.set(X, y, Z, Pal.b(Blocks.CAULDRON));
        });
        // la horca, al noreste de la plaza: un tablado, dos postes y la viga con su cadena
        int hx = 857, hz = 4032, hy = pie(hx, hz);
        ocupar(new Rect(hx - 2, hz - 2, hx + 2, hz + 2));
        trozo(4, hx - 2, hy - 2, hz - 2, hx + 2, hy + 7, hz + 2, c -> {
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++) {
                    for (int yy = alto(hx + dx, hz + dz) + 1; yy < hy; yy++) c.set(hx + dx, yy, hz + dz, Pal.DO_PLANKS);
                    c.set(hx + dx, hy, hz + dz, Hash.pct(hx + dx, 0, hz + dz, s + 2) < 15 ? Pal.slab(Blocks.SPRUCE_SLAB, false) : Pal.SP_PLANKS);
                }
            c.set(hx - 2, hy, hz, Pal.stairs(Blocks.SPRUCE_STAIRS, Direction.EAST, false));
            for (int dx : new int[]{-1, 1})
                for (int yy = hy + 1; yy <= hy + 4; yy++) c.set(hx + dx, yy, hz, Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
            for (int dx = -1; dx <= 1; dx++) c.set(hx + dx, hy + 5, hz, Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.X));
            c.set(hx, hy + 4, hz, Pal.CHAIN);
            c.set(hx, hy + 3, hz, Pal.CHAIN);
            c.set(hx + 1, hy + 1, hz + 1, Mat.calavera(5));
        });
        // un puesto del mercado, medio caido
        int px = 843, pz = 4046, py = pie(px, pz);
        ocupar(new Rect(px - 2, pz - 2, px + 2, pz + 2));
        Direction mira = Direction.EAST, lado = mira.getClockWise();
        trozo(4, px - 3, py - 1, pz - 3, px + 3, py + 4, pz + 3, c -> {
            for (int a = -2; a <= 2; a++)
                for (int b = -1; b <= 1; b++) {
                    int x = px + lado.getStepX() * a + mira.getStepX() * b, z = pz + lado.getStepZ() * a + mira.getStepZ() * b;
                    boolean poste = Math.abs(a) == 2 && Math.abs(b) == 1;
                    int h = Hash.pct(x, py, z, s + 3);
                    if (poste) {
                        c.set(x, py, z, Pal.SP_FENCE);
                        if (!(a == 2 && b == 1)) c.set(x, py + 1, z, Pal.SP_FENCE);
                        else continue;
                    }
                    if (h >= 36 && a <= 0) c.set(x, py + 2, z, Pal.b((a + 2) % 2 == 0 ? Blocks.GRAY_WOOL : Blocks.BROWN_WOOL));
                    if (b == 1 && !poste) c.set(x, py, z, h < 35 ? Mat.barril(Direction.UP) : h < 80 ? Pal.SP_PLANKS : Pal.slab(Blocks.SPRUCE_SLAB, false));
                }
            c.set(px - mira.getStepX(), py, pz - mira.getStepZ(), Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Y));
            c.set(px + lado.getStepX() * 2 - mira.getStepX(), py + 1, pz + lado.getStepZ() * 2 - mira.getStepZ(), Pal.COBWEB);
        });
    }

    // ------------------------------------------------------------------ farolas, puertas, carros y arboles

    private void farolas(long s) {
        // {x, z, brazo (0 norte, 1 este, 2 sur, 3 oeste)}
        int[][] f = {{846, 3958, 1}, {854, 3984, 3}, {846, 4010, 1}, {854, 4024, 3}, {846, 4074, 1}, {854, 4098, 3}, {846, 4122, 1},
                {854, 4146, 3}, {760, 4036, 2}, {800, 4044, 0}, {830, 4036, 2}, {874, 4044, 0}, {896, 4036, 2}, {936, 4044, 0},
                {960, 4036, 2}, {739, 4044, 0}};
        Direction[] dir = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (int[] k : f) {
            int x = k[0], z = k[1], y = pie(x, z);
            if (!libre(x, z, 0)) continue;
            ocupar(new Rect(x, z, x, z));
            Direction b = dir[k[2]];
            int p = Hash.pct(x, 0, z, s);
            trozo(4, x - 1, y, z - 1, x + 1, y + 5, z + 1, c -> {
                if (p < 20) {
                    // caida: solo el pie y un trozo de poste
                    c.set(x, y, z, Pal.b(Blocks.COBBLESTONE_WALL));
                    c.set(x, y + 1, z, Pal.SP_FENCE);
                    return;
                }
                Mat.farola(c, x, y, z, 4, b);
                // apagada: sin farol
                if (p < 40) c.set(x + b.getStepX(), y + 2, z + b.getStepZ(), Pal.CHAIN);
            });
        }
    }

    private void puertas() {
        // la del sur (hacia el Vado) y la del este: dos pilares de piedra y la viga caida
        int[][] q = {{850, 4152, 0}, {962, 4040, 1}};
        for (int[] k : q) {
            boolean ns = k[2] == 0;
            int cx = k[0], cz = k[1];
            sitio(ns ? "puerta_sur" : "puerta_este", cx, pie(cx, cz), cz);
            for (int sgn = -1; sgn <= 1; sgn += 2) {
                int x = ns ? cx + sgn * 4 : cx, z = ns ? cz : cz + sgn * 4;
                ocupar(new Rect(x, z, x, z));
            }
            trozo(4, cx - 5, pie(cx, cz) - 2, cz - 5, cx + 5, pie(cx, cz) + 7, cz + 5, c -> {
                for (int sgn = -1; sgn <= 1; sgn += 2) {
                    int x = ns ? cx + sgn * 4 : cx, z = ns ? cz : cz + sgn * 4, y = pie(x, z);
                    int h = sgn < 0 ? 5 : 3;
                    for (int yy = y - 1; yy < y + h; yy++) c.set(x, yy, z, Mat.muro(x, yy, z, SEMILLA + 60));
                    c.set(x, y + h, z, sgn < 0 ? Pal.SOUL_LANTERN : Pal.slab(Blocks.STONE_BRICK_SLAB, false));
                }
                // la viga, caida de un lado: apoyada en el pilar alto, en el suelo del otro
                int y = pie(cx, cz);
                for (int i = -3; i <= 1; i++) {
                    int x = ns ? cx + i : cx, z = ns ? cz : cz + i;
                    int yy = i <= -2 ? y + 4 : i <= 0 ? y + 2 : y;
                    c.set(x, yy, z, Pal.log(Blocks.DARK_OAK_LOG, ns ? Direction.Axis.X : Direction.Axis.Z));
                }
            });
        }
    }

    private void carros(Rng rng) {
        int[][] k = {{853, 4112, 0}, {742, 4044, 1}, {826, 4041, 3}, {953, 4044, 1}};
        Direction[] dir = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (int[] q : k) {
            elementos.add(new Town.Cart(rng.fork(), q[0], pie(q[0], q[1]), q[1], dir[q[2]]).layer(4));
            ocupar(new Rect(q[0] - 2, q[1] - 2, q[0] + 2, q[1] + 2));
        }
    }

    private void arboles(Rng rng) {
        int[][] k = {{826, 3962}, {874, 3986}, {823, 4088}, {876, 4122}, {779, 4066}, {918, 4016}, {950, 4064}, {770, 4006}, {940, 4014},
                {812, 4070}, {873, 4096}, {826, 4120}, {804, 3946}, {889, 3958}, {960, 4110}, {752, 3978}, {741, 4090}, {975, 4030}};
        for (int[] q : k) {
            int x = q[0], z = q[1];
            if (!libre(x, z, 2) || calle(x, z) >= 0) continue;
            ocupar(new Rect(x - 1, z - 1, x + 1, z + 1));
            elementos.add(new Landscape.DeadTree(rng.fork(), x, pie(x, z), z, rng.chance(0.3)).sinHojas());
        }
    }

    // ------------------------------------------------------------------ el puente roto y el vado

    /**
     * El puente de Velmor sobre el rio palido: cuatro arcos de piedra sobre tres pilas; el tercero se hundio y deja un
     * hueco de diez bloques (a pie no se salta; el corcel si). Pretiles a los lados, faroles en las cabezas y los
     * cascotes en el agua.
     */
    private final class Puente extends Element {
        Puente(long seed) {
            super(seed);
            this.layer = 5;
            bounds(PUENTE_X0 - 2, 50, PUENTE_Z - 6, PUENTE_X1 + 2, TABLERO + 6, PUENTE_Z + 6);
            sitio("puente_oeste", PUENTE_X0 + 2, TABLERO + 1, PUENTE_Z);
            sitio("puente_este", PUENTE_X1 - 2, TABLERO + 1, PUENTE_Z);
            sitio("puente_hueco", (HUECO_X0 + HUECO_X1) / 2, TABLERO + 1, PUENTE_Z);
        }

        private boolean pila(int x) {
            for (int p : PILAS) if (Math.abs(x - p) <= 1) return true;
            return false;
        }

        /** Hasta donde llega por debajo el arco en esa columna (lo de encima, hasta el tablero, es piedra). */
        private int intrados(int x) {
            if (x <= PUENTE_X0 + 6 || x >= PUENTE_X1 - 8 || pila(x)) return Integer.MIN_VALUE;
            int a = PUENTE_X0 + 7, b = PUENTE_X1 - 9;
            for (int p : PILAS) {
                if (x < p - 1) {
                    b = p - 2;
                    break;
                }
                a = p + 2;
            }
            double cx = (a + b) / 2.0, half = (b - a + 1) / 2.0;
            double u = Math.min(1, Math.abs(x - cx) / half);
            return TABLERO - 8 + (int) Math.round(6 * Math.sqrt(1 - u * u));
        }

        private boolean hundido(int x, int z) {
            if (x < HUECO_X0 || x > HUECO_X1) return false;
            // los bordes del hueco, mordidos
            return !(x == HUECO_X0 && Hash.pct(x, 1, z, seed) < 40) && !(x == HUECO_X1 && Hash.pct(x, 2, z, seed) < 40);
        }

        @Override
        public void build(Canvas c) {
            for (int x = PUENTE_X0; x <= PUENTE_X1; x++)
                for (int z = PUENTE_Z - 5; z <= PUENTE_Z + 5; z++) {
                    int dz = Math.abs(z - PUENTE_Z);
                    int suelo = alto(x, z);
                    boolean pilaAqui = pila(x);
                    // las pilas, con su tajamar en punta aguas arriba y abajo
                    if (pilaAqui && dz <= 4) {
                        int ancho = dz <= 3 ? 1 : 0;
                        if (Math.abs(x - pilaMasCerca(x)) > ancho) continue;
                        int top = dz <= 3 ? TABLERO - 1 : Terreno.AGUA + 2;
                        for (int y = suelo - 1; y <= top; y++) c.set(x, y, z, Mat.sillar(x, y, z, seed));
                        if (dz == 4) c.set(x, top + 1, z, Pal.slab(Blocks.STONE_BRICK_SLAB, false));
                        if (dz > 3) continue;
                    }
                    if (dz > 3) continue;
                    if (hundido(x, z)) continue;
                    // las cabezas: macizas hasta el suelo
                    int desde = intrados(x);
                    if (desde == Integer.MIN_VALUE) desde = suelo - 1;
                    for (int y = Math.max(desde + 1, suelo - 1); y <= TABLERO - 1; y++) c.set(x, y, z, Mat.sillar(x, y, z, seed + 1));
                    // el tablero y los pretiles
                    if (dz <= 2) {
                        int p = Hash.pct(x, 3, z, seed + 2);
                        boolean roto = Math.abs(x - HUECO_X0) <= 2 || Math.abs(x - HUECO_X1) <= 2;
                        c.set(x, TABLERO, z, roto && p < 30 ? Pal.GRAVEL : Mat.losas(x, TABLERO, z, seed));
                        c.column(x, z, TABLERO + 1, TABLERO + 3, Pal.AIR);
                    } else {
                        c.set(x, TABLERO, z, Mat.sillar(x, TABLERO, z, seed + 3));
                        int p = Hash.pct(x, 4, z, seed + 4);
                        boolean junto = x < HUECO_X0 - 2 || x > HUECO_X1 + 2;
                        if (p >= 18 && junto) c.unir(x, TABLERO + 1, z, Pal.b(p < 60 ? Blocks.STONE_BRICK_WALL : p < 85 ? Blocks.MOSSY_STONE_BRICK_WALL : Blocks.COBBLESTONE_WALL));
                    }
                }
            // los cascotes del arco hundido, en el agua
            for (int x = HUECO_X0 - 3; x <= HUECO_X1 + 3; x++)
                for (int z = PUENTE_Z - 5; z <= PUENTE_Z + 5; z++) {
                    int p = Hash.pct(x, 5, z, seed + 5);
                    if (p < 55) continue;
                    int fondo = alto(x, z);
                    int h = 1 + p % 3 + (Math.abs(z - PUENTE_Z) <= 2 && p > 85 ? 5 : 0);
                    for (int k = 1; k <= h; k++) c.set(x, fondo + k, z, k == h && p % 2 == 0 ? Pal.b(Blocks.STONE_BRICK_STAIRS) : Mat.sillar(x, fondo + k, z, seed + 6));
                }
            // los faroles de las cabezas
            for (int x : new int[]{PUENTE_X0 + 1, PUENTE_X1 - 1})
                for (int z : new int[]{PUENTE_Z - 3, PUENTE_Z + 3}) {
                    if (Hash.pct(x, 6, z, seed + 7) < 30) continue;
                    c.set(x, TABLERO + 1, z, Pal.STONE_BRICKS);
                    c.set(x, TABLERO + 2, z, Pal.b(Blocks.STONE_BRICK_WALL));
                    c.set(x, TABLERO + 3, z, Pal.SOUL_LANTERN);
                }
        }

        private int pilaMasCerca(int x) {
            int m = PILAS[0];
            for (int p : PILAS) if (Math.abs(x - p) < Math.abs(x - m)) m = p;
            return m;
        }
    }

    /** El vado: piedras planas que cruzan el rio a saltos, al sur del pueblo (de ahi el nombre del santuario). */
    private void vado() {
        sitio("vado", 700, Terreno.AGUA + 2, VADO_Z);
        trozo(4, 640, 50, VADO_Z - 1, 740, 70, VADO_Z + 2, c -> {
            for (int x = 640; x <= 740; x++) {
                if (Math.floorMod(x, 3) == 2) continue;
                for (int z = VADO_Z; z <= VADO_Z + 1; z++) {
                    int fondo = alto(x, z);
                    if (fondo >= Terreno.AGUA) continue;
                    for (int y = fondo; y <= Terreno.AGUA + 1; y++)
                        c.set(x, y, z, y == Terreno.AGUA + 1 ? (Hash.pct(x, 0, z, SEMILLA + 70) < 50 ? Pal.b(Blocks.MOSSY_COBBLESTONE) : Pal.STONE)
                                : Pal.b(Blocks.COBBLESTONE));
                }
            }
        });
    }

    /** Para la prueba: cuantas casas, si el atril tiene su libro, la campana y los arcones. */
    public static String resumen(net.minecraft.server.level.ServerLevel m) {
        Velmor v = get();
        int[] a = v.sitio("atril"), cb = v.sitio("campana");
        net.minecraft.core.BlockPos pa = new net.minecraft.core.BlockPos(a[0], a[1], a[2]), pc = new net.minecraft.core.BlockPos(cb[0], cb[1], cb[2]);
        boolean libro = m.getBlockEntity(pa) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le && Perdidos.de(le.getBook()) == 18;
        int arcones = 0, en = 0;
        for (Casa k : v.casas) {
            for (int x = k.fp.x0; x <= k.fp.x1; x++)
                for (int z = k.fp.z0; z <= k.fp.z1; z++)
                    for (int y = k.y - 1; y <= k.y + 6; y++) {
                        net.minecraft.core.BlockPos p = new net.minecraft.core.BlockPos(x, y, z);
                        if (!m.isLoaded(p)) continue;
                        en++;
                        if (m.getBlockState(p).is(Blocks.CHEST)) arcones++;
                    }
        }
        return "Velmor: " + v.casas.size() + " casas, " + v.elementos.size() + " piezas, " + v.indice.size() + " chunks; atril "
                + m.getBlockState(pa).getBlock().getDescriptionId().replace("block.minecraft.", "") + (libro ? " con el libro 18" : " SIN el libro")
                + ", campana " + (m.getBlockState(pc).is(Blocks.BELL) ? "SI" : "NO") + "; arcones en las casas " + arcones
                + (en == 0 ? " (sin cargar)" : "");
    }
}
