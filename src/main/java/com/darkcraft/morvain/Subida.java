package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.fortaleza.Perdidos;
import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
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
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
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
 * La subida por la cadena del sur (parte 7aa): el camino de tablones que los que subieron antes ataron por lo alto de la cadena
 * {@code Ciudadela.CADENAS[2]}, de lo alto del ancla (donde rezaba Aldric) a la isla de la ciudadela.
 * <ul>
 * <li>El <b>camino</b> (cuatro de ancho, de x {@link #X0} a {@link #X1}: la cadena va por x = 0) va un bloque por encima de lo
 * alto de los eslabones de pie (el camino {@link #OFF} por encima del eje de la cadena) y sube como ella (casi medio bloque por
 * cada uno): losas de roble oscuro (y algo de abeto), media altura cada paso; sobre los eslabones tumbados es un puente con sus
 * travesanos, sus caballetes sobre los costados del eslabon y su barandilla de cuerda (cadenas entre postes). Al pie, los
 * peldanos tallados en el ultimo eslabon (el lazo de lo alto del ancla) y la escalera que sube hasta lo alto de la cadena.</li>
 * <li>Rotos aqui y alla: <b>huecos</b> (dos tablones que faltan: se salta), trozos <b>estrechos</b> (dos de ancho, sin barandilla)
 * y barandillas caidas.</li>
 * <li>Los <b>rellanos</b> ({@link #RELLANOS}): tarimas llanas (con su escalera por el lado de abajo), con barandilla, barriles,
 * velas, calaveras, faroles y pedestales de piedra donde esperan las gargolas; el <b>Descansillo</b>, a media cadena: la tarima
 * grande con el santuario de la Mitad de la Cadena, la choza de tablones del campamento de los que subieron (los petates, la mesa
 * con velas, el atril con el <b>libro perdido 34</b> y un arcon).</li>
 * <li>Arriba, la <b>Boca</b>: donde la cadena entra en la isla se abre en la roca una boca con dientes (estalactitas de piedra
 * puntiaguda arriba y abajo) y dos ojos rojos en la torre de encima; dentro, la <b>Garganta</b> (roja, con carne de verrugas,
 * enredaderas que cuelgan y luz de setas), que sube por peldanos de piedra hasta la planta baja de la torre del sur de la muralla;
 * su puerta del norte da al patio de la ciudadela.</li>
 * </ul>
 * Se dibuja chunk a chunk al adornar (despues del cerco). u = x, z como en el mundo (la cadena va de norte, la isla, a sur, el ancla).
 */
public final class Subida {
    public static final long SEMILLA = 0x5B1DA_CADE_4A5L;

    /** Uno de la cadena: donde espera (y: donde tiene los pies), hacia donde mira, su tipo, variante, como espera, cuanto ronda y su grupo. */
    public record Espera(String tipo, int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    /** La cadena del sur. */
    static final Ciudadela.Cadena C = Ciudadela.CADENAS[2];
    /** Lo que sube la cadena por cada bloque hacia el norte. */
    public static final double PENDIENTE = -C.uy / C.uz;
    /** El camino, por encima del eje de la cadena (un bloque mas que lo alto de los eslabones de pie). */
    static final double OFF = 12.6;
    /** El ancho del camino (x). */
    public static final int X0 = -2, X1 = 1;
    /** Donde empieza: lo alto del ancla (donde rezaba Aldric) y lo que se pisa alli. */
    public static final int Z_ANCLA = Cerco.CZ + 3, Y_ANCLA = Cerco.PISO + 8;
    /** Los peldanos tallados en el lazo del ultimo eslabon (de Z_PELDANOS a Z_ANCLA - 2). */
    static final int Z_PELDANOS = 856;
    /** Arriba: la cara de la Boca (donde se acaba la madera), la Garganta, donde empiezan los peldanos que suben a la torre, la torre y su puerta. */
    public static final int Z_BOCA = 101, Z_GARGANTA = 96, Z_SUBE = 89, Z_TORRE = 85, Z_PUERTA = 77;
    /** El suelo de la planta baja de la torre del sur (lo que se pisa). */
    public static final int Y_TORRE = Ciudadela.TOP + 1;
    /** El centro de la torre del sur de la muralla. */
    static final double TORRE_Z = Ciudadela.RV;
    static final int ZMIN = Z_PUERTA - 8, ZMAX = Z_ANCLA + 2;

    /** Los rellanos: {z del centro, medio largo, medio ancho, tipo} (0 rellano, 1 tarima rota, 2 el Descansillo, 3 el de la Boca). */
    public static final int[][] RELLANOS = {
            {764, 5, 6, 0}, {652, 5, 6, 0}, {568, 4, 5, 1}, {484, 8, 9, 2}, {372, 5, 6, 0}, {260, 5, 6, 0}, {150, 6, 7, 3}};
    /** El del Descansillo (en RELLANOS). */
    public static final int DESCANSILLO = 3;
    /** Los huecos: {z sur, largo} (faltan los tablones de todo el ancho). */
    static final int[][] HUECOS = {{735, 2}, {612, 2}, {530, 2}, {420, 2}, {330, 2}, {216, 2}, {188, 2}};
    /** Los trozos estrechos: {z sur, z norte} (solo quedan x -1 y 0). */
    static final int[][] ESTRECHOS = {{700, 690}, {596, 584}, {452, 440}, {300, 289}, {236, 226}};

    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/cadena");
    /** El libro perdido del Descansillo. */
    public static final int LIBRO = 34;

    public static final int BX0 = -16, BX1 = 16, BZ0 = ZMIN, BZ1 = ZMAX;

    private static Subida unico;

    /**
     * Que se vuelva a hacer el plano la proxima vez: el primero se hace al construir el mod (los puestos de los de la cadena), antes de
     * que esten registrados los bloques del mod, y el santuario y las gemas salian como sus sustitutos de las vistas (piedra iman...).
     */
    public static synchronized void rehacer() {
        unico = null;
    }

    public static synchronized Subida get() {
        if (unico == null) unico = new Subida();
        return unico;
    }

    // ------------------------------------------------------------------ la cadena

    private static final double[][] CENTROS = new double[C.eslabones][];

    static {
        for (int i = 0; i < C.eslabones; i++) CENTROS[i] = C.centro(i);
    }

    /** Si el bloque (x, y, z) es hierro de la cadena del sur. */
    public static boolean hierro(int x, int y, int z) {
        double px = x + 0.5, py = y + 0.5, pz = z + 0.5;
        double s = (px - C.ax) * C.ux + (py - C.ay) * C.uy + (pz - C.az) * C.uz;
        int i0 = (int) Math.floor((s - C.s0) / Ciudadela.PASO);
        for (int i = i0 - 1; i <= i0 + 2; i++)
            if (i >= 0 && i < C.eslabones && C.hierro(i, CENTROS[i], px, py, pz)) return true;
        return false;
    }

    /** Lo mas alto del hierro de la cadena en la columna (x, z) por debajo de y (o Integer.MIN_VALUE). */
    static int hierroBajo(int x, int z, int y, int hondo) {
        for (int k = y; k >= y - hondo; k--) if (hierro(x, k, z)) return k;
        return Integer.MIN_VALUE;
    }

    /** El eje de la cadena en z (su y). */
    public static double eje(double z) {
        return C.ay + (z - C.az) * (C.uy / C.uz);
    }

    /** La linea del camino (la superficie que se pisa, sin redondear) en z. */
    public static double linea(double z) {
        return eje(z + 0.5) + OFF;
    }

    // ------------------------------------------------------------------ el perfil del camino

    /** Que hay en cada z del camino. */
    static final byte NADA = 0, MADERA = 1, RELLANO = 2, PELDANO = 3, TALLADO = 4, PIEDRA = 5, TORRE = 6;

    /** Lo que se pisa en cada z, en medios bloques (2 * y), y que es. */
    private final int[] h2 = new int[ZMAX - ZMIN + 1];
    private final byte[] zona = new byte[ZMAX - ZMIN + 1];
    private final int[] rellanoDe = new int[ZMAX - ZMIN + 1];
    /** La altura de cada rellano (lo que se pisa). */
    private final int[] alturaRellano = new int[RELLANOS.length];

    private final Map<Integer, Pilotes.Lista> trozos = new HashMap<>();
    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    /** Lo que hay (para la prueba y las vistas). */
    public int tablones, peldanos, tallados, postes, cuerdas, faroles, velas, calaveras, barriles, dientes, pedestales, hierroPisado;

    private Subida() {
        perfil();
        java.util.Arrays.fill(rellanoDe, -1);
        for (int k = 0; k < RELLANOS.length; k++) {
            int[] r = RELLANOS[k];
            for (int z = r[0] - r[1]; z <= r[0] + r[1]; z++) rellanoDe[z - ZMIN] = k;
        }
        camino();
        for (int k = 0; k < RELLANOS.length; k++) rellano(k);
        descansillo();
        boca();
        for (Pilotes.Lista l : trozos.values()) elementos.add(l.pieza(5));
        trozos();
        // ---- los sitios con nombre
        int[] d = RELLANOS[DESCANSILLO];
        sitio("ancla", 0, Y_ANCLA, Z_ANCLA);
        sitio("inicio", 0, pisa(Z_ANCLA - 16), Z_ANCLA - 16);
        for (int k = 0; k < RELLANOS.length; k++) {
            String n = k == DESCANSILLO ? "descansillo" : RELLANOS[k][3] == 3 ? "rellano_boca" : RELLANOS[k][3] == 1 ? "tarima" : "rellano" + (k < DESCANSILLO ? k + 1 : k);
            sitio(n, 0, alturaRellano[k], RELLANOS[k][0]);
        }
        sitio("santuario", SANT_X, alturaRellano[DESCANSILLO], SANT_Z + 2);
        sitio("atril", CHOZA_X0 + 2, alturaRellano[DESCANSILLO], d[0] - 4);
        sitio("arcon", CHOZA_X0 + 3, alturaRellano[DESCANSILLO], d[0] - 1);
        sitio("choza", CHOZA_X0 + 1, alturaRellano[DESCANSILLO], d[0] - 2);
        sitio("puente", 0, pisa(706), 706);
        sitio("hueco", 0, pisa(HUECOS[0][0] + 3), HUECOS[0][0] + 3);
        sitio("estrecho", 0, pisa(ESTRECHOS[0][0]), ESTRECHOS[0][0]);
        sitio("alto_cadena", 0, pisa(820), 820);
        sitio("medio", 0, pisa(420), 420);
        sitio("arriba", 0, pisa(180), 180);
        sitio("boca", 0, pisa(Z_BOCA + 3), Z_BOCA + 3);
        sitio("garganta", 0, pisa(Z_GARGANTA - 3), Z_GARGANTA - 3);
        sitio("torre", 0, Y_TORRE, Z_TORRE - 1);
        sitio("puerta", 0, Y_TORRE, Z_PUERTA);
        sitio("patio", 0, Y_TORRE, Z_PUERTA - 7);
        // para mirar desde fuera: de lado, a la altura de la mitad de la cadena
        sitio("lado", 46, pisa(470) + 14, 470);
        sitio("lado_boca", 34, pisa(140) + 8, 132);
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Lo que se pisa en z (bloque entero: donde tiene los pies alguien que esta alli). */
    public int pisa(int z) {
        if (z < ZMIN || z > ZMAX) return Y_ANCLA;
        return (int) Math.ceil(h2[z - ZMIN] / 2.0);
    }

    /** Lo que se pisa en z (con medio bloque). */
    public double superficie(int z) {
        if (z < ZMIN || z > ZMAX) return Y_ANCLA;
        return h2[z - ZMIN] / 2.0;
    }

    /** Si en z hay camino (o rellano, o peldanos). */
    public boolean hayCamino(int z) {
        return z >= ZMIN && z <= ZMAX && zona[z - ZMIN] != NADA;
    }

    private void perfil() {
        double[] base = new double[h2.length];
        boolean[] escalera = new boolean[h2.length];
        for (int z = ZMIN; z <= ZMAX; z++) {
            int i = z - ZMIN;
            if (z >= Z_ANCLA - 1) {
                zona[i] = NADA;
                continue;
            }
            if (z < Z_TORRE) {
                zona[i] = NADA;
                continue;
            }
            base[i] = linea(z);
            zona[i] = z >= Z_PELDANOS ? TALLADO : z > Z_BOCA ? MADERA : z >= Z_SUBE ? PIEDRA : TORRE;
        }
        // al pie: los peldanos tallados en el lazo y la escalera que sube a lo alto de la cadena (uno por bloque)
        for (int z = Z_ANCLA - 2; z >= ZMIN; z--) {
            int i = z - ZMIN;
            if (zona[i] == NADA) continue;
            double esc = Y_ANCLA + 1 + (Z_ANCLA - 2 - z);
            if (esc < base[i]) {
                base[i] = esc;
                escalera[i] = true;
                if (zona[i] == MADERA) zona[i] = PELDANO;
            }
        }
        // los rellanos: llanos (a lo que pide su lado mas alto, el del norte) y con su escalera por el sur
        for (int k = 0; k < RELLANOS.length; k++) {
            int[] r = RELLANOS[k];
            int zl = r[0], L = r[1];
            int hl = alturaDeRellano(k);
            alturaRellano[k] = hl;
            for (int z = zl - L; z <= zl + L; z++) {
                base[z - ZMIN] = hl;
                zona[z - ZMIN] = RELLANO;
            }
            for (int z = zl + L + 1; z <= ZMAX; z++) {
                int i = z - ZMIN;
                if (zona[i] == NADA) break;
                double esc = hl - (z - zl - L);
                if (esc <= base[i]) break;
                base[i] = esc;
                escalera[i] = true;
                if (zona[i] == MADERA) zona[i] = PELDANO;
            }
        }
        // arriba: los peldanos de piedra que suben de la Garganta a la planta baja de la torre
        for (int z = Z_SUBE - 1; z >= Z_TORRE; z--) {
            int i = z - ZMIN;
            base[i] = Math.max(base[i + 1] + 1, Y_TORRE - (z - Z_TORRE));
            escalera[i] = true;
        }
        base[Z_TORRE - ZMIN] = Y_TORRE;
        // el hierro de debajo: el camino siempre por encima (losa sobre el hierro), salvo los peldanos tallados en el lazo
        for (int z = ZMIN; z <= ZMAX; z++) {
            int i = z - ZMIN;
            if (zona[i] == NADA || zona[i] == TALLADO || zona[i] == RELLANO) continue;
            int alto = (int) Math.ceil(base[i]) + 1;
            for (int x = X0; x <= X1; x++) {
                int hb = hierroBajo(x, z, alto, 40);
                if (hb != Integer.MIN_VALUE) base[i] = Math.max(base[i], hb + 1.5);
            }
        }
        // a medios bloques, sin bajar hacia el norte y subiendo como mucho medio bloque por paso (uno entero en las escaleras,
        // y siempre de bloque entero a bloque entero)
        for (int i = 0; i < h2.length; i++) h2[i] = zona[i] == NADA ? 0 : (int) Math.ceil(base[i] * 2 - 1e-6);
        for (int i = 0; i < h2.length; i++)
            if (zona[i] != NADA && (escalera[i] || zona[i] == RELLANO || zona[i] == TALLADO || zona[i] == TORRE) && (h2[i] & 1) == 1) h2[i]++;
        for (int vuelta = 0; vuelta < 6; vuelta++) {
            // hacia el norte no baja
            for (int i = h2.length - 2; i >= 0; i--)
                if (zona[i] != NADA && zona[i + 1] != NADA && h2[i] < h2[i + 1]) h2[i] = h2[i + 1];
            // subiendo hacia el norte, como mucho lo que deje el peldano
            for (int i = 0; i < h2.length - 1; i++) {
                if (zona[i] == NADA || zona[i + 1] == NADA) continue;
                boolean esc = (escalera[i] || zona[i] == TORRE || zona[i] == TALLADO) && (h2[i] & 1) == 0;
                int max = esc ? 2 : 1;
                if (h2[i] - h2[i + 1] > max) {
                    h2[i + 1] = h2[i] - max;
                    if (esc) escalera[i + 1] = true;
                }
                if (esc && h2[i] - h2[i + 1] == 2 && (h2[i + 1] & 1) == 1) h2[i + 1]++;
            }
        }
    }

    /** Si en z el camino sube un bloque entero desde el sur (va un escalon). */
    private boolean escalon(int z) {
        int i = z - ZMIN;
        if (i + 1 >= h2.length || zona[i + 1] == NADA) return (h2[i] & 1) == 0 && zona[i] != RELLANO && h2[i] / 2 > Y_ANCLA;
        return h2[i] - h2[i + 1] == 2;
    }

    // ------------------------------------------------------------------ poner bloques

    private Pilotes.Lista trozo(int z) {
        return trozos.computeIfAbsent(Math.floorDiv(z, 32), k -> new Pilotes.Lista());
    }

    private void put(int x, int y, int z, BlockState s) {
        if (!s.isAir() && hierro(x, y, z)) {
            hierroPisado++;
            return;
        }
        trozo(z).put(x, y, z, s);
    }

    /** Como put, pero tambien donde hay hierro (los peldanos tallados en el lazo). */
    private void tallar(int x, int y, int z, BlockState s) {
        trozo(z).put(x, y, z, s);
    }

    private void une(int x, int y, int z, BlockState s) {
        if (hierro(x, y, z)) return;
        trozo(z).une(x, y, z, s);
    }

    private boolean hay(int x, int y, int z) {
        Pilotes.Lista l = trozos.get(Math.floorDiv(z, 32));
        return l != null && l.hay(x, y, z);
    }

    static BlockState b(Block k) {
        return k.defaultBlockState();
    }

    static int pct(int x, int y, int z, long sem) {
        return (int) Math.floorMod(Ciudadela.h(x * 7 + (int) sem, y, z * 3 - (int) (sem * 13)), 100);
    }

    static BlockState losa(Block k, SlabType t) {
        return k.defaultBlockState().setValue(SlabBlock.TYPE, t);
    }

    static BlockState escalera(Block k, Direction f, boolean alReves) {
        return k.defaultBlockState().setValue(StairBlock.FACING, f).setValue(StairBlock.HALF, alReves ? Half.TOP : Half.BOTTOM);
    }

    static BlockState tronco(Block k, Direction.Axis a) {
        return k.defaultBlockState().setValue(RotatedPillarBlock.AXIS, a);
    }

    /** La madera de cada tablon (sobre todo roble oscuro; algo de abeto). */
    private static Block[] madera(int x, int z) {
        int p = pct(x, 0, z, 3);
        return p < 72 ? new Block[]{Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_PLANKS}
                : p < 92 ? new Block[]{Blocks.SPRUCE_SLAB, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_PLANKS}
                : new Block[]{Blocks.MANGROVE_SLAB, Blocks.MANGROVE_STAIRS, Blocks.MANGROVE_PLANKS};
    }

    private static Block[] piedra(int x, int y, int z) {
        int p = pct(x, y, z, 5);
        return p < 60 ? new Block[]{Blocks.POLISHED_BLACKSTONE_SLAB, Blocks.POLISHED_BLACKSTONE_STAIRS, Blocks.POLISHED_BLACKSTONE}
                : p < 85 ? new Block[]{Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICKS}
                : new Block[]{Blocks.BLACKSTONE_SLAB, Blocks.BLACKSTONE_STAIRS, Blocks.BLACKSTONE};
    }

    /** Si en (x, z) falta el tablon (los huecos y lo que queda de los trozos estrechos). */
    static boolean falta(int x, int z) {
        for (int[] h : HUECOS) if (z <= h[0] && z > h[0] - h[1]) return true;
        for (int[] e : ESTRECHOS) if (z <= e[0] && z >= e[1] && (x < -1 || x > 0)) return true;
        return false;
    }

    static boolean estrecho(int z) {
        for (int[] e : ESTRECHOS) if (z <= e[0] + 1 && z >= e[1] - 1) return true;
        return false;
    }

    static boolean enHueco(int z) {
        for (int[] h : HUECOS) if (z <= h[0] + 1 && z >= h[0] - h[1]) return true;
        return false;
    }

    /** El bloque del camino en (x, z): media losa, losa de arriba (bloque entero de alto) o escalon. Devuelve la y donde va. */
    private int tablon(int x, int z, Block[] mat) {
        int i = z - ZMIN, h = h2[i];
        if (escalon(z)) {
            put(x, h / 2 - 1, z, escalera(mat[1], Direction.NORTH, false));
            return h / 2 - 1;
        }
        if ((h & 1) == 0) {
            put(x, h / 2 - 1, z, losa(mat[0], SlabType.TOP));
            return h / 2 - 1;
        }
        put(x, (h - 1) / 2, z, losa(mat[0], SlabType.BOTTOM));
        return (h - 1) / 2;
    }

    // ------------------------------------------------------------------ el camino

    private void camino() {
        for (int z = ZMIN; z <= ZMAX; z++) {
            int i = z - ZMIN;
            byte q = zona[i];
            if (q == NADA || q == RELLANO) continue;
            if (q == TALLADO) {
                // los peldanos tallados en el hierro del lazo (con su hueco encima)
                int y = h2[i] / 2 - 1;
                for (int x = X0; x <= X1; x++) {
                    tallar(x, y, z, escalera(Blocks.POLISHED_BLACKSTONE_STAIRS, Direction.NORTH, false));
                    for (int k = 1; k <= 3; k++) if (hierro(x, y + k, z)) tallar(x, y + k, z, Pal.AIR);
                    tallados++;
                }
                continue;
            }
            if (q == PIEDRA || q == TORRE) continue;    // los de la Garganta y la torre, en boca()
            int y0 = Integer.MAX_VALUE;
            for (int x = X0; x <= X1; x++) {
                if (falta(x, z)) continue;
                int y = tablon(x, z, madera(x, z));
                y0 = Math.min(y0, y);
                if (escalon(z)) peldanos++;
                else tablones++;
            }
            if (y0 == Integer.MAX_VALUE) y0 = (int) Math.floor(superficie(z)) - 1;
            // lo de debajo: un travesano cada cuatro (si no hay hierro justo debajo) y, sobre los eslabones tumbados, los caballetes
            boolean puente = hierroBajo(-1, z, y0 - 1, 3) == Integer.MIN_VALUE && hierroBajo(0, z, y0 - 1, 3) == Integer.MIN_VALUE;
            if (Math.floorMod(z, 4) == 0 && !enHueco(z))
                for (int x = X0 - 1; x <= X1 + 1; x++)
                    if (!hierro(x, y0 - 1, z) && !hay(x, y0 - 1, z)) put(x, y0 - 1, z, tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
            if (puente && Math.floorMod(z, 12) == 6 && !estrecho(z) && !enHueco(z)) caballete(z, y0);
            // la barandilla: en los puentes, postes cada tres con la cuerda (cadena) entre medias; en lo alto de los eslabones de
            // pie, nada (y algun poste roto)
            if (estrecho(z) || enHueco(z) || z >= Z_ANCLA - 24) continue;
            int pisa = (int) Math.ceil(superficie(z));
            for (int lado : new int[]{X0 - 1, X1 + 1}) {
                boolean roto = pct(lado, 0, z / 9, 7) < 26;
                if (!puente) {
                    if (Math.floorMod(z, 9) == 0 && pct(lado, 1, z, 8) < 30) {
                        une(lado, pisa - 1, z, b(Blocks.DARK_OAK_FENCE));
                        postes++;
                    }
                    continue;
                }
                if (Math.floorMod(z, 3) == 0) {
                    une(lado, pisa - 1, z, b(Blocks.DARK_OAK_FENCE));
                    une(lado, pisa, z, b(Blocks.DARK_OAK_FENCE));
                    postes++;
                    if (Math.floorMod(z, 24) == 0 && !roto) {
                        put(lado, pisa + 1, z, b(Blocks.SOUL_LANTERN));
                        faroles++;
                    }
                } else if (!roto) {
                    put(lado, pisa, z, Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
                    cuerdas++;
                }
            }
        }
        // los peldanos de la escalera del pie: sus postes, con una vela roja en cada tres
        for (int z = Z_PELDANOS - 1; z >= ZMIN; z--) {
            int i = z - ZMIN;
            if (zona[i] != PELDANO || z < Z_ANCLA - 40) continue;
            int pisa = (int) Math.ceil(superficie(z));
            if (Math.floorMod(z, 3) == 0)
                for (int lado : new int[]{X0 - 1, X1 + 1}) {
                    une(lado, pisa - 1, z, b(Blocks.DARK_OAK_FENCE));
                    if (Math.floorMod(z, 6) == 0) {
                        put(lado, pisa, z, Blocks.RED_CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 2).setValue(CandleBlock.LIT, true));
                        velas++;
                    }
                }
            // y los pies derechos hasta el suelo del cerco (la escalera va sobre el hueco del ultimo eslabon)
            if (Math.floorMod(z, 8) == 2 && z < Z_PELDANOS - 2) {
                int y0 = (int) Math.floor(superficie(z)) - 2;
                for (int lado : new int[]{X0 - 1, X1 + 1}) {
                    for (int y = Cerco.PISO; y <= y0 + 1; y++) put(lado, y, z, tronco(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
                    postes++;
                }
                for (int y = Cerco.PISO + 6; y <= y0; y += 7)
                    for (int x = X0; x <= X1; x++) une(x, y, z, b(Blocks.DARK_OAK_FENCE));
            }
        }
    }

    /** Un caballete: dos pies sobre los costados del eslabon tumbado de debajo y el travesano bajo el camino. */
    private void caballete(int z, int y0) {
        int[] pies = {-8, 7};
        boolean alguno = false;
        for (int x : pies) {
            int hb = hierroBajo(x, z, y0 - 1, 18);
            if (hb == Integer.MIN_VALUE) continue;
            for (int y = hb + 1; y <= y0 - 1; y++) put(x, y, z, tronco(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
            alguno = true;
            postes++;
        }
        if (!alguno) return;
        for (int x = pies[0]; x <= pies[1]; x++)
            if (!hay(x, y0 - 1, z)) put(x, y0 - 1, z, tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        // las riostras: una cadena colgando de cada pie hacia el camino
        for (int x : pies) put(x, y0, z, b(Blocks.DARK_OAK_FENCE));
    }

    // ------------------------------------------------------------------ los rellanos

    private void rellano(int k) {
        int[] r = RELLANOS[k];
        int zl = r[0], L = r[1], W = r[2], tipo = r[3];
        int y = alturaRellano[k] - 1;                  // las losas de arriba (lo que se pisa, y + 1)
        for (int z = zl - L; z <= zl + L; z++)
            for (int x = -W; x <= W - 1; x++) {
                boolean borde = x == -W || x == W - 1 || z == zl - L || z == zl + L;
                // la tarima rota: le falta una esquina
                if (tipo == 1 && x >= W - 3 && z <= zl - L + 2) continue;
                if (tipo == 1 && x <= -W + 1 && z >= zl + L - 1 && pct(x, 2, z, 9) < 60) continue;
                if (borde) {
                    boolean largo = z == zl - L || z == zl + L;
                    put(x, y, z, tronco(Blocks.STRIPPED_DARK_OAK_LOG, largo ? Direction.Axis.X : Direction.Axis.Z));
                } else {
                    Block[] m = madera(x, z);
                    put(x, y, z, pct(x, y, z, 10) < 8 ? b(m[2]) : losa(m[0], SlabType.TOP));
                }
                tablones++;
            }
        // por debajo: travesanos y pies hasta el hierro
        for (int z = zl - L; z <= zl + L; z += 3)
            for (int x = -W; x <= W - 1; x++) if (!hay(x, y - 1, z)) put(x, y - 1, z, tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        for (int z : new int[]{zl - L, zl, zl + L})
            for (int x : new int[]{-W, W - 1}) {
                int hb = hierroBajo(x, z, y - 2, 26);
                int hasta = hb == Integer.MIN_VALUE ? y - 9 : hb + 1;
                for (int yy = hasta; yy <= y - 2; yy++) put(x, yy, z, tronco(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
            }
        // la barandilla (abierta donde entra y sale el camino) con los postes de las esquinas mas altos
        for (int z = zl - L; z <= zl + L; z++)
            for (int x = -W; x <= W - 1; x++) {
                boolean borde = x == -W || x == W - 1 || z == zl - L || z == zl + L;
                if (!borde || !hay(x, y, z)) continue;
                boolean entrada = (z == zl - L || z == zl + L) && x >= X0 - 1 && x <= X1 + 1;
                if (entrada) continue;
                if (tipo != 2 && pct(x, 3, z, 11) < (tipo == 1 ? 45 : 16)) continue;   // barandilla rota
                une(x, y + 1, z, b(Blocks.DARK_OAK_FENCE));
                boolean esquina = (x == -W || x == W - 1) && (z == zl - L || z == zl + L);
                if (esquina) {
                    une(x, y + 2, z, b(Blocks.DARK_OAK_FENCE));
                    put(x, y + 3, z, b(Blocks.SOUL_LANTERN));
                    faroles++;
                }
                postes++;
            }
        // lo de encima
        if (tipo == 2) return;   // el Descansillo, en descansillo()
        int p = alturaRellano[k];
        if (tipo == 0 || tipo == 1) {
            put(-W + 1, p, zl - 1, b(Blocks.BARREL));
            barriles++;
            if (tipo == 0) {
                put(-W + 1, p, zl, b(Blocks.BARREL));
                put(-W + 1, p + 1, zl, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3).setValue(CandleBlock.LIT, true));
                put(W - 2, p, zl + 2, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, 4));
                put(W - 3, p, zl + 1, b(Blocks.BONE_BLOCK));
                barriles++;
                velas++;
                calaveras++;
                // el poste de las cadenas: un pie alto con dos cadenas que cuelgan y una calavera
                for (int yy = p; yy <= p + 4; yy++) une(W - 2, yy, zl + 3, b(Blocks.DARK_OAK_FENCE));
                put(W - 3, p + 4, zl + 3, b(Blocks.DARK_OAK_FENCE));
                put(W - 3, p + 3, zl + 3, b(Blocks.CHAIN));
                put(W - 3, p + 2, zl + 3, b(Blocks.CHAIN));
                put(W - 3, p + 1, zl + 3, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, 8));
                calaveras++;
            }
        }
        // los pedestales de las gargolas (piedra negra), en las esquinas del norte
        if (tipo == 0 || tipo == 3) {
            int[][] ped = tipo == 3 ? new int[][]{{-W + 1, zl - L + 1}, {W - 2, zl - L + 1}, {-W + 1, zl + 1}, {W - 2, zl + 1}}
                    : new int[][]{{W - 2, zl - L + 1}};
            for (int[] q : ped) {
                put(q[0], p, q[1], b(Blocks.POLISHED_BLACKSTONE_BRICKS));
                put(q[0], p + 1, q[1], b(Blocks.CHISELED_POLISHED_BLACKSTONE));
                pedestales++;
            }
        }
    }

    /** Lo alto de un pedestal de gargola (donde tiene los pies). */
    public int[] pedestal(int k, int n) {
        int[] r = RELLANOS[k];
        int zl = r[0], L = r[1], W = r[2];
        int[][] ped = r[3] == 3 ? new int[][]{{-W + 1, zl - L + 1}, {W - 2, zl - L + 1}, {-W + 1, zl + 1}, {W - 2, zl + 1}}
                : new int[][]{{W - 2, zl - L + 1}};
        int[] q = ped[Math.min(n, ped.length - 1)];
        return new int[]{q[0], alturaRellano[k] + 2, q[1]};
    }

    // ------------------------------------------------------------------ el Descansillo

    /** El santuario de la Mitad de la Cadena (en el Descansillo, al oeste del camino). */
    public static final int SANT_X = -5;
    public static final int SANT_Z = RELLANOS[DESCANSILLO][0] + 3;
    /** La choza (al este del camino): de x CHOZA_X0 a CHOZA_X1 y de z (centro - 6) a (centro). */
    static final int CHOZA_X0 = 3, CHOZA_X1 = 8;

    /** El suelo del santuario (lo que se pisa esta en SANT_Y + 1); sin levantar la subida entera (lo usa la lista de santuarios). */
    public static int santuarioY() {
        return alturaDeRellano(DESCANSILLO) - 1;
    }

    /** Lo que se pisa en el rellano k: llano, a lo que pide su lado mas alto (el del norte) y por encima del hierro que tenga debajo. */
    static int alturaDeRellano(int k) {
        int[] r = RELLANOS[k];
        int zl = r[0], L = r[1], W = r[2];
        double alto = linea(zl - L);
        for (int z = zl - L; z <= zl + L; z++)
            for (int x = -W; x <= W - 1; x++) {
                int hb = hierroBajo(x, z, (int) Math.ceil(alto) + 2, 30);
                if (hb != Integer.MIN_VALUE) alto = Math.max(alto, hb + 2);
            }
        return (int) Math.ceil(alto);
    }

    private void descansillo() {
        int[] r = RELLANOS[DESCANSILLO];
        int zl = r[0], p = alturaRellano[DESCANSILLO];
        // el santuario (apagado) con su corro de losas de piedra
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) > 3) continue;
                put(SANT_X + dx, p - 1, SANT_Z + dz, Math.abs(dx) + Math.abs(dz) == 3 ? b(Blocks.POLISHED_BASALT) : b(Blocks.POLISHED_BLACKSTONE));
            }
        put(SANT_X, p, SANT_Z, santuarioBloque());
        // dos farolas de almas a los lados del santuario
        for (int dx : new int[]{-2, 2}) {
            put(SANT_X + dx, p, SANT_Z - 2, b(Blocks.POLISHED_BLACKSTONE));
            put(SANT_X + dx, p + 1, SANT_Z - 2, b(Blocks.POLISHED_BLACKSTONE_WALL));
            put(SANT_X + dx, p + 2, SANT_Z - 2, b(Blocks.SOUL_LANTERN));
            faroles++;
        }
        // la choza: muros de tablones con pies de tronco, abierta al oeste, techo a un agua
        int z0 = zl - 6, z1 = zl;
        for (int z = z0; z <= z1; z++)
            for (int x = CHOZA_X0; x <= CHOZA_X1; x++) {
                boolean pared = x == CHOZA_X1 || z == z0 || z == z1;
                boolean esquina = (x == CHOZA_X1 || x == CHOZA_X0) && (z == z0 || z == z1);
                int alto = 3 + (x - CHOZA_X0) / 3;                 // el techo baja hacia el oeste
                if (esquina) for (int y = p; y < p + alto; y++) put(x, y, z, tronco(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
                else if (pared && !(x == CHOZA_X0 && (z == z0 || z == z1))) {
                    for (int y = p; y < p + alto; y++) {
                        boolean ventana = x == CHOZA_X1 && (z == zl - 3) && y == p + 1;
                        put(x, y, z, ventana ? Pal.AIR : pct(x, y, z, 12) < 70 ? b(Blocks.DARK_OAK_PLANKS) : b(Blocks.SPRUCE_PLANKS));
                    }
                }
                put(x, p + alto, z, escalera(Blocks.DARK_OAK_STAIRS, Direction.EAST, false));
            }
        // dentro: los dos petates, la mesa con velas, el atril (libro 34) y el arcon (en trozos())
        put(CHOZA_X1 - 1, p, z0 + 1, b(Blocks.BROWN_CARPET));
        put(CHOZA_X1 - 2, p, z0 + 1, b(Blocks.BROWN_CARPET));
        put(CHOZA_X1 - 1, p, z1 - 1, b(Blocks.GRAY_CARPET));
        put(CHOZA_X1 - 2, p, z1 - 1, b(Blocks.GRAY_CARPET));
        put(CHOZA_X1 - 1, p, zl - 3, b(Blocks.DARK_OAK_FENCE));
        put(CHOZA_X1 - 1, p + 1, zl - 3, losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
        put(CHOZA_X1 - 1, p + 1, zl - 2, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 2).setValue(CandleBlock.LIT, true));
        put(CHOZA_X1 - 2, p, zl - 2, b(Blocks.BARREL));
        put(CHOZA_X1 - 2, p + 1, zl - 2, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 1).setValue(CandleBlock.LIT, true));
        velas += 2;
        barriles++;
        // la hoguera fria del campamento, delante de la choza, y la ropa tendida en una cuerda
        put(CHOZA_X0 - 1, p, zl + 4, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
        put(CHOZA_X0 - 1, p, zl + 6, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, 2));
        calaveras++;
        // el tronco donde dejo sus marcas el primero (las muescas de los dias que estuvo)
        put(CHOZA_X0 + 2, p, zl + 5, tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Z));
        put(CHOZA_X0 + 2, p, zl + 6, tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Z));
    }

    private static BlockState santuarioBloque() {
        try {
            return DarkCraftMod.SANTUARIO.get().defaultBlockState();
        } catch (Throwable t) {
            return Blocks.LODESTONE.defaultBlockState();
        }
    }

    private static BlockState gema() {
        return com.darkcraft.gen.TorreCupula.gema(true);
    }

    // ------------------------------------------------------------------ la Boca, la Garganta y la torre

    /** Medio ancho de la Garganta, su alto (sobre lo que se pisa) y lo mas alto que se vacia en la roca (la isla, encima, intacta). */
    static final int GARGANTA_W = 3, GARGANTA_ALTO = 5, TECHO_ROCA = Ciudadela.TOP - 2;
    /** Las mandibulas de la Boca: de la cara de la isla (Z_BOCA) hacia fuera, cuanto salen y su medio ancho. */
    static final int BOCA_SALE = 5, BOCA_W = 7;

    /** Si (x, z) cae en la planta baja de la torre del sur (por dentro del muro). */
    static boolean enTorre(int x, int z) {
        return Math.hypot(x + 0.5, z + 0.5 - TORRE_Z) < Ciudadela.RT - 2;
    }

    /** Si (x, z) cae en la torre del sur (muro incluido). */
    static boolean bajoTorre(int x, int z) {
        return Math.hypot(x + 0.5, z + 0.5 - TORRE_Z) <= Ciudadela.RT + 0.5;
    }

    private void boca() {
        // ---- la Garganta: de la cara de la isla hacia dentro, por la roca (lo de encima de la isla no se toca) y, ya bajo la torre,
        // los peldanos que salen por un hueco en el suelo de su planta baja
        for (int z = Z_TORRE; z <= Z_BOCA; z++) {
            int pisa = (int) Math.ceil(superficie(z));
            boolean dentroTorre = enTorre(0, z);
            int techo = dentroTorre ? Y_TORRE + 3 : Math.min(pisa + GARGANTA_ALTO - 1, TECHO_ROCA);
            for (int x = -GARGANTA_W - 1; x <= GARGANTA_W; x++)
                for (int y = pisa - 2; y <= techo + 1; y++) {
                    boolean hueco = x >= -GARGANTA_W && x <= GARGANTA_W - 1 && y >= pisa && y <= techo;
                    if (dentroTorre) {
                        // en la torre: el hueco de la escalera (solo lo del camino) y nada mas
                        if (x >= X0 && x <= X1 && y >= pisa && y <= techo) put(x, y, z, Pal.AIR);
                        continue;
                    }
                    if (hueco) {
                        put(x, y, z, Pal.AIR);
                        continue;
                    }
                    if (y >= Ciudadela.TOP - 1) continue;                          // lo de arriba de la isla, como esta
                    if (y == pisa - 1 && x >= X0 && x <= X1) continue;             // donde va el peldano
                    // las paredes: carne (verrugas y nilio carmesi), roca negra, alguna seta que alumbra y alguna brasa
                    int q = pct(x, y, z, 13);
                    put(x, y, z, q < 44 ? b(Blocks.NETHER_WART_BLOCK) : q < 60 ? b(Blocks.CRIMSON_NYLIUM) : q < 65 ? b(Blocks.SHROOMLIGHT)
                            : q < 86 ? b(Blocks.BLACKSTONE) : b(Blocks.MAGMA_BLOCK));
                }
            // lo que se pisa: piedra (la madera llega hasta la cara de la isla)
            for (int x = X0; x <= X1; x++) {
                if (z < Z_SUBE) put(x, pisa - 1, z, escalera(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.NORTH, false));
                else tablon(x, z, piedra(x, z, z));
                peldanos++;
            }
            // el suelo de la garganta a los lados del camino (la lengua) y las enredaderas que cuelgan del techo
            if (!dentroTorre) {
                for (int x = -GARGANTA_W; x <= GARGANTA_W - 1; x++)
                    if (x < X0 || x > X1) put(x, pisa - 1, z, pct(x, 0, z, 14) < 60 ? b(Blocks.NETHER_WART_BLOCK) : b(Blocks.CRIMSON_NYLIUM));
                if (Math.floorMod(z, 2) == 0 && techo - pisa >= 3)
                    for (int x = -GARGANTA_W; x <= GARGANTA_W - 1; x += 5) {
                        int largo = 1 + pct(x, 1, z, 15) % 2;
                        for (int k = 0; k < largo; k++)
                            put(x, techo - k, z, k == largo - 1 ? b(Blocks.WEEPING_VINES) : b(Blocks.WEEPING_VINES_PLANT));
                    }
                if (Math.floorMod(z, 4) == 1 && !bajoTorre(0, z))
                    put(GARGANTA_W - 1, techo, z, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
        }
        // ---- las mandibulas de la Boca, por fuera de la cara de la isla, alrededor del final del camino: la de arriba (con su encia de
        // verrugas debajo) y la de abajo (bajo los tablones), mas estrechas hacia delante; los colmillos, colgando de la de arriba y
        // saliendo de la de abajo a los lados del camino
        for (int dz = -1; dz <= BOCA_SALE; dz++) {
            int z = Z_BOCA + dz;
            int p = (int) Math.ceil(superficie(z));
            int w = BOCA_W - Math.max(0, dz - 2);
            for (int x = -w; x <= w - 1; x++) {
                boolean centro = x >= -w + 1 && x <= w - 2;
                for (int y = p + 5; y <= p + 7; y++) {
                    if (y > TECHO_ROCA && dz <= 0) continue;
                    BlockState st = y == p + 5 ? b(Blocks.NETHER_WART_BLOCK) : pct(x, y, z, 16) < 25 ? b(Blocks.GILDED_BLACKSTONE)
                            : pct(x, y, z, 16) < 60 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS) : b(Blocks.BLACKSTONE);
                    if (y == p + 7 && !centro) continue;
                    put(x, y, z, st);
                }
                for (int y = p - 4; y <= p - 2; y++) {
                    if (y == p - 4 && !centro) continue;
                    BlockState st = y == p - 2 && (x < X0 - 1 || x > X1 + 1) ? b(Blocks.NETHER_WART_BLOCK)
                            : pct(x, y, z, 17) < 30 ? b(Blocks.GILDED_BLACKSTONE) : b(Blocks.BLACKSTONE);
                    if (y == p - 2 && x >= X0 - 1 && x <= X1 + 1) continue;      // bajo los tablones, el hueco de los travesanos
                    put(x, y, z, st);
                }
            }
        }
        int zf = Z_BOCA + BOCA_SALE - 1, pf = (int) Math.ceil(superficie(zf));
        for (int x = -BOCA_W + 1; x <= BOCA_W - 2; x++) {
            boolean junto = x >= X0 - 1 && x <= X1 + 1;
            int largo = junto ? 2 : Math.floorMod(x, 2) == 0 ? 3 : 2;
            colmillo(x, pf + 4, zf, Direction.DOWN, largo);
            colmillo(x, pf + 4, zf - 2, Direction.DOWN, Math.max(1, largo - 1));
            if (!junto) colmillo(x, pf - 1, zf, Direction.UP, Math.floorMod(x, 2) == 0 ? 2 : 1);
        }
        // ---- los ojos: dos gemas rojas de dos por dos en la cara del sur de la torre de encima
        int zt = (int) Math.floor(TORRE_Z + Ciudadela.RT) - 1;
        for (int x : new int[]{-5, 3})
            for (int dx = 0; dx <= 1; dx++)
                for (int dy = 0; dy <= 1; dy++) put(x + dx, Ciudadela.TOP + 7 + dy, zt, gema());
        // ---- la torre: la barandilla del hueco en su suelo y la puerta del norte al patio (en arco)
        for (int z = Z_TORRE + 1; z <= Z_SUBE + 1; z++) {
            if (!enTorre(X0 - 1, z) || !enTorre(X1 + 1, z)) continue;
            for (int lado : new int[]{X0 - 1, X1 + 1}) une(lado, Y_TORRE, z, b(Blocks.POLISHED_BLACKSTONE_WALL));
        }
        int zp0 = (int) Math.floor(TORRE_Z - Ciudadela.RT), zp1 = (int) Math.floor(TORRE_Z - Ciudadela.RT + 2.2);
        for (int z = zp0 - 1; z <= zp1; z++)
            for (int x = X0; x <= X1; x++)
                for (int y = Y_TORRE; y <= Y_TORRE + 3; y++) {
                    boolean arco = y == Y_TORRE + 3 && (x == X0 || x == X1);
                    put(x, y, z, arco ? escalera(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, x == X0 ? Direction.EAST : Direction.WEST, true) : Pal.AIR);
                }
        // dentro de la torre: faroles en las paredes y cadenas que cuelgan del piso de arriba
        put(-4, Y_TORRE + 2, (int) TORRE_Z, b(Blocks.SOUL_LANTERN));
        put(3, Y_TORRE + 2, (int) TORRE_Z, b(Blocks.SOUL_LANTERN));
        for (int y = Y_TORRE + 4; y <= Y_TORRE + 9; y++) {
            put(-3, y, (int) TORRE_Z - 3, b(Blocks.CHAIN));
            put(3, y, (int) TORRE_Z - 3, b(Blocks.CHAIN));
        }
        faroles += 2;
    }

    /** Un diente: una estalactita (hacia abajo) o estalagmita (hacia arriba) de piedra puntiaguda de n bloques desde (x, y, z). */
    private void colmillo(int x, int y, int z, Direction d, int n) {
        for (int k = 0; k < n; k++) {
            DripstoneThickness t = k == n - 1 ? DripstoneThickness.TIP : k == n - 2 ? DripstoneThickness.FRUSTUM
                    : k == 0 ? DripstoneThickness.BASE : DripstoneThickness.MIDDLE;
            if (n == 1) t = DripstoneThickness.TIP;
            put(x, d == Direction.DOWN ? y - k : y + k, z, Blocks.POINTED_DRIPSTONE.defaultBlockState()
                    .setValue(PointedDripstoneBlock.TIP_DIRECTION, d).setValue(PointedDripstoneBlock.THICKNESS, t));
            dientes++;
        }
    }

    // ------------------------------------------------------------------ el atril y el arcon

    private void trozos() {
        int[] r = RELLANOS[DESCANSILLO];
        int zl = r[0], p = alturaRellano[DESCANSILLO];
        // el atril del Descansillo (en la choza, de cara al oeste) y el arcon del campamento
        trozo(CHOZA_X0 + 2, p, zl - 4, c -> c.lectern(CHOZA_X0 + 2, p, zl - 4, Direction.WEST, Perdidos.libro(LIBRO)));
        trozo(CHOZA_X0 + 3, p, zl - 1, c -> c.chest(CHOZA_X0 + 3, p, zl - 1, Direction.WEST, BOTIN));
    }

    private void trozo(int x, int y, int z, Trozo.Obra o) {
        elementos.add(new Trozo(6, x, y, z, x, y, z, o));
    }

    // ------------------------------------------------------------------ los sitios

    private void sitio(String nombre, int x, int y, int z) {
        sitios.put(nombre, new int[]{x, y, z});
    }

    /** Los sitios con nombre: {x, y, z} (y: lo que se pisa). */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    public List<Espera> esperas() {
        return esperas;
    }

    public List<Element> elementos() {
        return elementos;
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    // ------------------------------------------------------------------ escribir un chunk

    /** Si el chunk tiene algo de la subida. */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < BX0 || x0 > BX1 || z0 + 15 < BZ0 || z0 > BZ1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} de la subida en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    // ------------------------------------------------------------------ los de la cadena

    private static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;

    private void ponerEsperas() {
        // los rellanos: los marcados que subieron y se quedaron (rompedores de rodillas golpeando la cadena, caballeros de guardia,
        // cazadores que tiran hacia abajo) y las gargolas en sus pedestales
        int R = 0, K = 1, Z = 2, Re = com.darkcraft.enemigos.Enemigo.REZA, D = com.darkcraft.enemigos.Enemigo.DE_PIE;
        int[] r1 = RELLANOS[0], r2 = RELLANOS[1], r3 = RELLANOS[2], r4 = RELLANOS[4], r5 = RELLANOS[5], r6 = RELLANOS[6];
        // el primer rellano: dos rompedores de rodillas y un caballero
        marcado(R, -4, alturaRellano[0], r1[0] - 2, OESTE, Re, "rellano1");
        marcado(R, 4, alturaRellano[0], r1[0] + 1, ESTE, Re, "rellano1");
        marcado(K, 0, alturaRellano[0], r1[0] - 4, SUR, D, "rellano1");
        gargola(0, pedestal(0, 0), SUR, Re, "rellano1");
        // el segundo: los cazadores (tiran escaleras abajo) y una gargola
        marcado(Z, -3, alturaRellano[1], r2[0] - 3, SUR, D, "rellano2");
        marcado(Z, 3, alturaRellano[1], r2[0] - 2, SUR, D, "rellano2");
        marcado(K, 0, alturaRellano[1], r2[0], SUR, D, "rellano2");
        gargola(0, pedestal(1, 0), SUR, Re, "rellano2");
        // la tarima rota: un caballero y una gargola que ronda
        marcado(K, -2, alturaRellano[2], r3[0], SUR, D, "tarima");
        gargola(0, new int[]{3, alturaRellano[2] + 6, r3[0] + 8}, SUR, D, "tarima");
        // por el camino: gargolas que rondan entre rellanos y alguna posada en lo alto de un eslabon
        gargola(0, new int[]{0, pisa(706) + 5, 706}, SUR, D, "camino_bajo");
        gargola(0, new int[]{-2, pisa(620) + 6, 620}, SUR, D, "camino_bajo");
        // el tercero (tras el Descansillo): dos caballeros, un cazador y una gargola
        marcado(K, -3, alturaRellano[4], r4[0] + 2, SUR, D, "rellano3");
        marcado(K, 3, alturaRellano[4], r4[0] - 1, SUR, D, "rellano3");
        marcado(Z, 0, alturaRellano[4], r4[0] - 4, SUR, D, "rellano3");
        gargola(0, pedestal(4, 0), SUR, Re, "rellano3");
        gargola(0, new int[]{4, pisa(415) + 6, 415}, SUR, D, "camino_alto");
        gargola(0, new int[]{-3, pisa(320) + 7, 320}, SUR, D, "camino_alto");
        // el cuarto: rompedores de rodillas y un cazador, y una gargola
        marcado(R, -4, alturaRellano[5], r5[0], OESTE, Re, "rellano4");
        marcado(R, 4, alturaRellano[5], r5[0] - 2, ESTE, Re, "rellano4");
        marcado(Z, 0, alturaRellano[5], r5[0] - 4, SUR, D, "rellano4");
        gargola(0, pedestal(5, 0), SUR, Re, "rellano4");
        gargola(0, new int[]{2, pisa(205) + 6, 205}, SUR, D, "camino_alto");
        // el de la Boca: las cuatro gargolas en sus pedestales y un caballero ante la boca
        for (int n = 0; n < 4; n++) gargola(0, pedestal(6, n), SUR, Re, "rellano_boca");
        marcado(K, 0, alturaRellano[6], r6[0] - 4, SUR, D, "rellano_boca");
        // en la garganta, dos caballeros de guardia
        marcado(K, -1, pisa(Z_GARGANTA - 4), Z_GARGANTA - 4, SUR, D, "garganta");
        marcado(K, 1, pisa(Z_SUBE + 2), Z_SUBE + 2, SUR, D, "garganta");
    }

    private void marcado(int variante, int x, int y, int z, float giro, int postura, String grupo) {
        esperas.add(new Espera("marcado", variante, x, y, z, giro, postura, 0, grupo));
    }

    private void gargola(int variante, int[] p, float giro, int postura, String grupo) {
        esperas.add(new Espera("gargola", variante, p[0], p[1], p[2], giro, postura, postura == com.darkcraft.enemigos.Enemigo.DE_PIE ? 6 : 0, grupo));
    }

    // ------------------------------------------------------------------ donde se esta

    /** Si (x, y, z) esta sobre la cadena del sur (cerca del camino, por encima del ancla y por debajo de la isla). */
    public static boolean enCadena(double x, double y, double z) {
        if (z < Z_GARGANTA || z > Z_ANCLA - 1 || Math.abs(x) > 40) return false;
        double l = linea(z);
        return y > l - 40 && y < l + 30;
    }

    /** Si (x, y, z) esta sobre la cadena (y no abajo, en el Cerco del Ancla, junto a su pie): ahi no sube el corcel. */
    public static boolean sobreCadena(double x, double y, double z) {
        return enCadena(x, y, z) && y > Y_ANCLA - 2;
    }

    /** Si (x, y, z) esta en el camino o justo encima (de pie en el, o saltando). */
    public static boolean enCamino(double x, double y, double z) {
        int zi = (int) Math.floor(z);
        if (zi < ZMIN || zi > ZMAX || !get().hayCamino(zi)) return false;
        int k = get().rellanoDe[zi - ZMIN];
        double w = k >= 0 ? RELLANOS[k][2] + 1 : 3.5;
        if (x < -w - 0.5 || x > w + 0.5) return false;
        double s = get().superficie(zi);
        return y > s - 1.5 && y < s + 4;
    }

    /** Si (x, y, z) esta en el Descansillo (alli no sopla el viento). */
    public static boolean enDescansillo(double x, double y, double z) {
        int[] r = RELLANOS[DESCANSILLO];
        return Math.abs(z - r[0] - 0.5) < r[1] + 2 && x > -r[2] - 2 && x < r[2] + 2 && Math.abs(y - get().alturaRellano[DESCANSILLO]) < 8;
    }

    /** Si (x, y, z) esta en la Boca o en la Garganta. */
    public static boolean enBoca(double x, double y, double z) {
        return z >= Z_TORRE && z < Z_BOCA + BOCA_SALE + 1 && Math.abs(x + 0.5) < BOCA_W + 1 && y > linea(z) - 3 && y < Y_TORRE + 1;
    }

    /** Si (x, y, z) esta en la isla de la ciudadela (dentro de la muralla, sobre el patio). */
    public static boolean enCiudadela(double x, double y, double z) {
        // (con la planta baja de las torres de la muralla: la del sur, por donde se entra, va hasta z = A + 7)
        return Math.hypot(x, z) < Ciudadela.A + 8 && y > Ciudadela.TOP - 2 && y < Ciudadela.TOP + 120;
    }

    /** Lo que se pisa en z (para el viento y las caidas: la linea si no hay camino). */
    public static double suelo(double z) {
        int zi = (int) Math.floor(z);
        Subida s = get();
        return s.hayCamino(zi) ? s.superficie(zi) : linea(z);
    }

    /** Para la prueba: lo que hay de la subida ya generado. */
    public static String resumen(ServerLevel m) {
        Subida s = get();
        int[] at = s.sitio("atril"), ar = s.sitio("arcon"), sa = s.sitio("santuario");
        boolean libro = m.getBlockEntity(new BlockPos(at[0], at[1], at[2])) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le
                && Perdidos.de(le.getBook()) == LIBRO;
        boolean arcon = m.getBlockState(new BlockPos(ar[0], ar[1], ar[2])).is(Blocks.CHEST);
        boolean santuario = m.getBlockState(new BlockPos(SANT_X, s.alturaRellano[DESCANSILLO], SANT_Z)).is(DarkCraftMod.SANTUARIO.get());
        // el camino: cuantos tablones hay donde toca (cada 10 z, en x -1 y 0, fuera de huecos)
        int bien = 0, total = 0, hierroEnCamino = 0;
        for (int z = Z_BOCA + 2; z < Z_ANCLA - 2; z += 10) {
            if (enHueco(z)) continue;
            for (int x = -1; x <= 0; x++) {
                total++;
                double sup = s.superficie(z);
                int y = (sup == Math.floor(sup)) ? (int) sup - 1 : (int) Math.floor(sup);
                BlockState st = m.getBlockState(new BlockPos(x, y, z));
                if (!st.isAir()) bien++;
                if (st.is(com.darkcraft.gen.TorreCupula.hierroNegro().getBlock())) hierroEnCamino++;
            }
        }
        int[] bo = s.sitio("boca"), to = s.sitio("torre"), pu = s.sitio("puerta");
        boolean boca = m.getBlockState(new BlockPos(bo[0], bo[1], bo[2])).getCollisionShape(m, BlockPos.ZERO).isEmpty()
                && m.getBlockState(new BlockPos(bo[0], bo[1] + 1, bo[2])).getCollisionShape(m, BlockPos.ZERO).isEmpty();
        boolean torre = m.getBlockState(new BlockPos(to[0], to[1], to[2])).isAir() && m.getBlockState(new BlockPos(to[0], to[1] + 1, to[2])).isAir();
        boolean puerta = m.getBlockState(new BlockPos(pu[0], pu[1], pu[2])).isAir() && m.getBlockState(new BlockPos(pu[0], pu[1] + 1, pu[2])).isAir();
        return "atril con el libro " + LIBRO + " " + (libro ? "SI" : "NO") + ", arcon " + (arcon ? "SI" : "NO") + ", santuario " + (santuario ? "SI" : "NO")
                + ", tablones en su sitio " + bien + "/" + total + " (hierro donde va el camino " + hierroEnCamino + "), la boca abierta " + (boca ? "SI" : "NO")
                + ", la planta baja de la torre " + (torre ? "SI" : "NO") + ", la puerta al patio " + (puerta ? "SI" : "NO");
    }
}
