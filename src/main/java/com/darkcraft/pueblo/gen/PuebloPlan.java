package com.darkcraft.pueblo.gen;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Pal;
import com.darkcraft.gen.Rect;
import com.darkcraft.gen.Rng;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * El plano de la isla de Brumavela (parte 3b): el pueblo abandonado de los marineros del mar de nubes, donde se despierta.
 * Esta en el origen del mundo y es igual en todos (hecho a mano, con una semilla fija). Se levanta chunk a chunk al generar
 * el mundo ({@code world/PuebloFeature}), como el castillo de Valdrenn.
 * <p>
 * De sur a norte: el muelle bajo en la cala (el almacen, el espigon de madera y el barco roto), la Cuesta que sube por
 * un tajo, la capitania, la plaza con el pozo (la taberna al este, la herreria al oeste), las casas a lo largo de las
 * dos calles, la capilla con su campanario y el cementerio en la colina del noroeste, los campos y el faro en el penon
 * del noreste; al oeste, la bajada al Saliente.
 */
public final class PuebloPlan {
    public static final long SEMILLA = 0xB12A7E1A5EAL;

    private static PuebloPlan unico;

    public static synchronized PuebloPlan get() {
        if (unico == null) unico = new PuebloPlan();
        return unico;
    }

    public final Terreno terreno;
    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Set<String> avisados = new HashSet<>();

    /** La plaza del pozo. */
    public static final int PLAZA_X = 0, PLAZA_Z = 6, PLAZA_R = 11;
    /** Las calles: {x0, z0, x1, z1, medio ancho}. */
    static final double[][] CALLES = {
            {0, -66, 0, 46, 2.4},        // la calle mayor, de norte a sur (sigue por la Cuesta)
            {-86, 6, 88, 6, 2.0},        // la traviesa, de oeste a este
            {0, -42, -28, -44, 1.5},     // a la capilla
            {-28, -44, -37, -46, 1.5},
            {0, -66, 38, -70, 1.5},      // al faro
            {38, -70, 46, -72, 1.5},
            {46, -72, 66, -72, 2.0},
            {44, 6, 46, -32, 1.5},       // a los campos
            {-34, 8, -34, 44, 1.4},      // el callejon del oeste
            {26, 8, 26, 44, 1.4},        // el callejon del este
    };

    /** Los sitios con nombre (la vela, el timon, las campanas, donde se despierta...): {x, y, z} del bloque. */
    private final Map<String, int[]> sitios = new java.util.LinkedHashMap<>();
    /** Lo que ya esta ocupado en planta (edificios, el cementerio, los campos): ahi no van arboles ni farolas. */
    private final List<Rect> ocupado = new ArrayList<>();

    public Capilla capilla;
    public Faro faro;
    public Muelle muelle;
    public Barco barco;

    private PuebloPlan() {
        Rng rng = new Rng(SEMILLA);
        terreno = new Terreno(rng.fork(), this);
        elementos.add(terreno);
        casas(rng);
        capilla = new Capilla(rng.fork(), this);
        add(capilla);
        ocupar(new Rect(Capilla.NX0 - 1, Capilla.NZ0 - 1, Capilla.TX + 4, Capilla.NZ1 + 1));
        faro = new Faro(rng.fork(), this);
        add(faro);
        ocupar(new Rect(Faro.X - 5, Faro.Z - 5, Faro.X + 5, Faro.Z + 5));
        muelle = new Muelle(rng.fork(), this);
        add(muelle);
        barco = new Barco(rng.fork(), false);
        add(barco);
        Barco.sitios(this);
        Detalles.poner(this, rng);
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(clave(x, z), k -> new ArrayList<>()).add(e);
    }

    void add(Element e) {
        elementos.add(e);
    }

    void sitio(String nombre, int x, int y, int z) {
        sitios.put(nombre, new int[]{x, y, z});
    }

    /** Donde esta un sitio con nombre ({x, y, z} del bloque), o null. */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    void ocupar(Rect r) {
        ocupado.add(r);
    }

    /** Si en (x, z) se puede poner algo suelto: hay meseta, y no cae en un edificio, la plaza, la cala ni una cuesta. */
    public boolean libre(int x, int z, int margen) {
        int a = terreno.alto(x, z);
        if (a == Terreno.NADA || a < Terreno.P - 4) return false;
        if (Math.hypot(x - PLAZA_X, z - PLAZA_Z) <= PLAZA_R + margen && margen > 0) return false;
        if (Math.hypot(x - Terreno.CALA_X, z - Terreno.CALA_Z) <= Terreno.CALA_R + 7 + margen) return false;
        if (Math.abs(x) <= Terreno.CUESTA_ANCHO + 2 + margen && z >= Terreno.CUESTA_Z0 - 3) return false;
        if (Math.abs(z - Terreno.BAJADA_Z) <= Terreno.BAJADA_ANCHO + 2 + margen && x <= Terreno.BAJADA_X0 + 2) return false;
        if (Math.abs(z - Terreno.RAMPA_Z) <= Terreno.RAMPA_ANCHO + 4 + margen && x >= Terreno.RAMPA_X0 - 3 && x <= Terreno.RAMPA_X1 + 3) return false;
        if (Math.hypot(x - Terreno.PENON_X, z - Terreno.PENON_Z) <= Terreno.PENON_R + margen) return false;
        for (Rect r : ocupado)
            if (x >= r.x0 - margen && x <= r.x1 + margen && z >= r.z0 - margen && z <= r.z1 + margen) return false;
        return true;
    }

    // ------------------------------------------------------------------ las casas

    public Casa taberna, herreria, capitania, almacen, casaCarta;
    public final List<Casa> casas = new ArrayList<>();

    private static Rect R(int x0, int z0, int x1, int z1) {
        return new Rect(x0, z0, x1, z1);
    }

    private Casa casa(Rng rng, Rect fp, Direction frente, int plantas, int ruina, Casa.Estilo estilo, int tejado, Casa.Tipo tipo) {
        Casa k = new Casa(rng.fork(), terreno, fp, frente, plantas, ruina, estilo, tejado, tipo);
        elementos.add(k);
        casas.add(k);
        ocupar(fp.grow(2));
        return k;
    }

    private void casas(Rng rng) {
        final Casa.Estilo E = Casa.Estilo.ENTRAMADO, P = Casa.Estilo.PIEDRA, M = Casa.Estilo.MADERA;
        final Casa.Tipo H = Casa.Tipo.HOGAR;
        taberna = casa(rng, R(13, -15, 27, 1), Direction.SOUTH, 2, 0, E, 1, Casa.Tipo.TABERNA);
        herreria = casa(rng, R(-27, -10, -15, 1), Direction.SOUTH, 1, 1, P, 2, Casa.Tipo.HERRERIA);
        capitania = casa(rng, R(-18, 27, -6, 40), Direction.EAST, 2, 0, E, 2, Casa.Tipo.CAPITANIA);
        almacen = casa(rng, R(10, 92, 24, 106), Direction.WEST, 1, 0, M, 1, Casa.Tipo.ALMACEN);
        // el suroeste
        casa(rng, R(-16, 12, -6, 22), Direction.EAST, 1, 0, E, 0, H);
        casa(rng, R(-30, 12, -21, 21), Direction.WEST, 1, 1, M, 0, H);
        casaCarta = casa(rng, R(-48, 12, -38, 22), Direction.EAST, 2, 0, E, 1, H);
        casa(rng, R(-49, 28, -38, 38), Direction.EAST, 1, 2, P, 0, H);
        casa(rng, R(-30, 27, -21, 37), Direction.WEST, 2, 1, E, 0, H);
        // el sureste
        casa(rng, R(6, 12, 16, 22), Direction.WEST, 2, 0, E, 2, H);
        casa(rng, R(6, 27, 17, 38), Direction.WEST, 1, 1, P, 1, H);
        casa(rng, R(30, 12, 40, 22), Direction.WEST, 1, 0, M, 1, H);
        casa(rng, R(30, 27, 41, 38), Direction.WEST, 1, 2, E, 0, H);
        casa(rng, R(46, 12, 56, 22), Direction.NORTH, 1, 1, E, 0, H);
        // el noreste
        casa(rng, R(6, -34, 16, -24), Direction.WEST, 2, 0, E, 1, H);
        casa(rng, R(32, -12, 42, -2), Direction.SOUTH, 2, 1, E, 0, H);
        casa(rng, R(52, -24, 62, -14), Direction.WEST, 1, 0, P, 2, H);
        // el noroeste
        casa(rng, R(-16, -34, -6, -24), Direction.EAST, 1, 1, M, 1, H);
        casa(rng, R(-42, -8, -32, 1), Direction.SOUTH, 1, 0, P, 0, H);
        casa(rng, R(-62, -8, -52, 1), Direction.SOUTH, 1, 1, E, 2, H);
        // las afueras: solo ruinas
        casa(rng, R(-76, 12, -66, 21), Direction.NORTH, 1, 2, P, 0, H);
        casa(rng, R(62, 12, 72, 21), Direction.NORTH, 1, 2, E, 0, H);
        casa(rng, R(-9, -93, -1, -85), Direction.SOUTH, 1, 2, P, 0, H);
        casa(rng, R(-72, 46, -62, 55), Direction.EAST, 1, 2, P, 0, H);
        // el granero de los campos del sureste
        casa(rng, R(50, 40, 64, 52), Direction.NORTH, 1, 1, M, 0, Casa.Tipo.ALMACEN);
    }

    public static long clave(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) ^ (chunkZ & 0xffffffffL);
    }

    public boolean toca(int chunkX, int chunkZ) {
        return indice.containsKey(clave(chunkX, chunkZ));
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    public void construir(WorldGenLevel level, int chunkX, int chunkZ, RandomSource random) {
        List<Element> l = indice.get(clave(chunkX, chunkZ));
        if (l == null) return;
        Canvas c = new Canvas(level, chunkX, chunkZ, random);
        for (Element e : l) {
            try {
                e.build(c);
            } catch (Throwable t) {
                String k = e.getClass().getSimpleName();
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} de Brumavela en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    // ------------------------------------------------------------------ el suelo

    static double distSeg(double x, double z, double ax, double az, double bx, double bz) {
        double vx = bx - ax, vz = bz - az, l2 = vx * vx + vz * vz;
        double t = l2 <= 0 ? 0 : Math.max(0, Math.min(1, ((x - ax) * vx + (z - az) * vz) / l2));
        return Math.hypot(ax + vx * t - x, az + vz * t - z);
    }

    /** Cuanto falta para el eje de la calle mas cercana, menos su medio ancho (negativo = dentro de la calle). */
    public double calle(int x, int z) {
        double d = Double.MAX_VALUE;
        for (double[] c : CALLES) d = Math.min(d, distSeg(x, z, c[0], c[1], c[2], c[3]) - c[4]);
        return d;
    }

    public boolean enPlaza(int x, int z) {
        return Math.hypot(x - PLAZA_X, z - PLAZA_Z) <= PLAZA_R + 0.4;
    }

    /** El bloque de arriba del suelo. */
    public BlockState superficie(int x, int z, int top) {
        Terreno t = terreno;
        int p = Hash.pct(x, top, z, 31);
        if (t.enMuelle(x, z)) {
            if (p < 40) return Pal.STONE_BRICKS;
            if (p < 58) return Pal.b(Blocks.CRACKED_STONE_BRICKS);
            if (p < 74) return Pal.MOSSY_STONE_BRICKS;
            if (p < 88) return Pal.b(Blocks.COBBLESTONE);
            return Pal.ANDESITE;
        }
        if (t.enCuesta(x, z) && top < Terreno.P) return p < 60 ? Pal.b(Blocks.COBBLESTONE) : p < 82 ? Pal.b(Blocks.MOSSY_COBBLESTONE) : Pal.ANDESITE;
        if (t.enBajada(x, z) && top < Terreno.P) return p < 50 ? Pal.b(Blocks.COBBLESTONE) : p < 75 ? Pal.GRAVEL : Pal.STONE;
        if (t.enRampa(x, z)) return p < 55 ? Pal.b(Blocks.COBBLESTONE) : p < 80 ? Pal.b(Blocks.MOSSY_COBBLESTONE) : Pal.ANDESITE;
        if (t.escalon(x, z)) return p < 60 ? Pal.b(Blocks.COBBLESTONE) : Pal.COARSE_DIRT;
        if (t.enSaliente(x, z)) return p < 45 ? Pal.STONE : p < 70 ? Pal.GRAVEL : p < 85 ? Pal.b(Blocks.MOSSY_COBBLESTONE) : Pal.ANDESITE;
        if (enPlaza(x, z)) {
            if (p < 42) return Pal.b(Blocks.COBBLESTONE);
            if (p < 62) return Pal.b(Blocks.MOSSY_COBBLESTONE);
            if (p < 80) return Pal.ANDESITE;
            if (p < 92) return Pal.STONE_BRICKS;
            return Pal.GRAVEL;
        }
        if (Detalles.CEMENTERIO.contains(x, z)) {
            if (p < 34) return Pal.PODZOL;
            if (p < 58) return Pal.COARSE_DIRT;
            if (p < 64) return Pal.MOSS;
            return Pal.GRASS_BLOCK;
        }
        double dc = calle(x, z);
        if (dc <= 0) {
            if (p < 46) return Pal.b(Blocks.DIRT_PATH);
            if (p < 68) return Pal.GRAVEL;
            if (p < 86) return Pal.COARSE_DIRT;
            return Pal.b(Blocks.COBBLESTONE);
        }
        // el penon del faro: roca
        double dp = Math.hypot(x - Terreno.PENON_X, z - Terreno.PENON_Z);
        if (dp < Terreno.PENON_R - 1 && top > Terreno.P + 1) return p < 42 ? Pal.STONE : p < 66 ? Pal.ANDESITE : p < 80 ? Pal.TUFF : p < 90 ? Pal.GRAVEL : Pal.GRASS_BLOCK;
        // el borde de la isla: roca pelada a trozos
        if (top < Terreno.P - 1) return p < 45 ? Pal.STONE : p < 65 ? Pal.COARSE_DIRT : p < 80 ? Pal.GRASS_BLOCK : Pal.ANDESITE;
        if (dc <= 1.2 && p < 45) return Pal.COARSE_DIRT;
        // la hierba, en manchas grandes (no a bloques sueltos): hierba, hojarasca y calvas de tierra
        double m = terreno.mancha(x, z);
        double v = m + (p - 50) * 0.004;
        if (v > 0.26) return Pal.PODZOL;
        if (v < -0.30) return Pal.COARSE_DIRT;
        if (v < -0.24 && p < 50) return Pal.ROOTED_DIRT;
        if (p < 3) return Pal.MOSS;
        return Pal.GRASS_BLOCK;
    }

    /** Si en esa columna pueden salir hierbas. */
    public boolean plantas(int x, int z) {
        return calle(x, z) > 0.5 && !enPlaza(x, z);
    }
}
