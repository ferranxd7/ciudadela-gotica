package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Destilado;
import com.darkcraft.enemigos.Enemigo;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * El Lazareto de Maese Galen (parte 7au; respuesta 131 del plan: "torres de mago"), en el sur del Pantano Podrido. Cuando la Peste
 * Gris lleno el camposanto de San Ulfo, Maese Galen, el fisico y alquimista que la Academia echo por abrir a los muertos, se llevo a
 * los enfermos en barca a su torre de la laguna y les dio a beber lo que destilaba de su sangre y del agua negra: las bubas se
 * hicieron de cristal y no murieron. La noche del cielo rojo hirvieron los alambiques: los Destilados ({@link Destilado}).
 * u = x - {@link #CX}, v = z - {@link #CZ}; la torre esta en (0, 0) y la calzada llega por el sur.
 * <ul>
 * <li>La <b>laguna</b> (cavada en el pantano) con el <b>islote</b>: la <b>calzada</b> de piedra desde la orilla del sur (el santuario),
 * el huerto de simples muerto (norte), las tumbas de los enfermos (sureste) y el embarcadero roto con la barca hundida (oeste).</li>
 * <li>La <b>torre</b> (redonda, de piedra oscura con bandas de cobre): el <b>sotano del atanor</b> ({@link #PS}: el horno bajo el pozo,
 * su parrilla, la palanca, las cisternas), el <b>vestibulo de los frascos</b>, la <b>sala de los enfermos</b>, el <b>herbolario</b>, la
 * <b>biblioteca</b> (el <b>libro perdido 38</b>; tras una estanteria, un <b>muro ilusorio</b> y la celda de Galen), el
 * <b>laboratorio</b> y la <b>Sala del Elixir</b> bajo la cupula de cristal verde (el gran alambique: parte 7av). Una escalera de caracol
 * en la torreta del este une las plantas, pero entre la sala de los enfermos y el herbolario esta hundida: hay que <b>encender el
 * atanor</b> (la palanca del sotano) y subir por <b>el tiro</b> del pozo del centro ({@link #tick}: quien esta en el pozo con el atanor
 * encendido sube; agachado, baja despacio). Encendido se queda (lo dicen las hogueras de la parrilla).</li>
 * </ul>
 * Se dibuja chunk a chunk al adornar; en su caja {@link Adorno} no pone nada (los arboles muertos los pone el).
 */
public final class Lazareto {
    public static final long SEMILLA = 0x1A2A_7E70_6A1EL;
    /** El centro de la torre (el del pozo del tiro). */
    public static final int CX = -4600, CZ = 1500;
    /** Lo alto del agua de la laguna (el ultimo bloque de agua) y el suelo del islote (el bloque de arriba: el de la planta baja). */
    public static final int AGUA = 63, ISLA = 66;
    /** Lo alto de cada planta y el suelo del sotano (el bloque que se pisa). */
    public static final int ALTO = 10, PS = ISLA - ALTO;
    /** Las plantas de arriba: la Sala del Elixir y el tejado (lo alto de los muros). */
    public static final int P5 = ISLA + 5 * ALTO, TEJADO = ISLA + 6 * ALTO;
    /** Los radios de la torre: el muro (de fuera y de dentro), el pozo del tiro y la cupula. */
    public static final double RT = 11.5, RI = 9.5, RP = 2.5, RD = 8.5;
    /** La torreta de la escalera (al este, en v = 0) y su radio. */
    public static final int TX = 13;
    public static final double RTU = 3.6;
    /** El islote y la laguna (radios medios: los bordes ondulan). */
    public static final double R_ISLA = 22, R_LAGUNA = 46;
    /** El santuario del Lazareto (en {@link SantuariosMorvain}): en la orilla del sur, al final de la calzada. */
    public static final int SANT_U = 0, SANT_V = 62;
    public static final int X0 = CX - 62, X1 = CX + 62, Z0 = CZ - 62, Z1 = CZ + 76;
    /** Los chunks donde {@link Adorno} no pone nada. */
    static final int AX0 = CX - 58, AX1 = CX + 58, AZ0 = CZ - 58, AZ1 = CZ + 58;

    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/lazareto"),
            BOTIN_BUENO = new ResourceLocation(DarkCraftMod.MODID, "chests/lazareto_bueno");
    /** El muro ilusorio de la biblioteca (dos estanterias del oeste: detras, la celda de Galen). */
    public static final String MURO_CELDA = "lazareto_celda";
    /** El libro perdido del atril de la biblioteca. */
    public static final int LIBRO = 38;
    public static final int ATRIL_U = -5, ATRIL_V = 2;
    /** La palanca del atanor (en el sotano, a la derecha de la puerta del horno) y el centro de la parrilla. */
    public static final int PALANCA_U = 1, PALANCA_V = -5;
    /** Los angulos (grados, de u hacia v) de las ventanas de cada planta. */
    static final double[] VENTANAS = {22.5, 67.5, 112.5, 157.5, 202.5, 247.5, 292.5, 337.5};
    /** Escalones por vuelta de la escalera de caracol (una vuelta, una planta: los descansillos quedan siempre al oeste). */
    static final int PASOS = ALTO;

    /** Uno del lazareto: donde espera (y: donde tiene los pies), hacia donde mira, su variante, como espera, cuanto ronda y su grupo. */
    public record Espera(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    private static Lazareto unico;

    public static synchronized Lazareto get() {
        if (unico == null) unico = new Lazareto();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    private final Mina.Plano plano = new Mina.Plano();
    /** Lo que hay (para la prueba y las vistas). */
    public int camillas, estanterias, alambiques, tumbas, ventanas, arboles, velas;

    private Lazareto() {
        elementos.add(new Obra());
        torre();
        torreta();
        sotano();
        vestibulo();
        enfermeria();
        herbolario();
        biblioteca();
        celda();
        laboratorio();
        elixir();
        cupula();
        islote();
        calzada();
        elementos.add(plano.pieza(4, X0, Z0, X1, Z1));
        trozos();
        ponerSitios();
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo del lazareto (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en la laguna o el islote: ahi {@link Adorno} no pone nada. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= AX0 && x0 <= AX1 && z0 + 15 >= AZ0 && z0 <= AZ1;
    }

    /** Lo lejos de (x, z) al centro de la torre. */
    public static double distancia(double x, double z) {
        return Math.hypot(x - CX - 0.5, z - CZ - 0.5);
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} del lazareto en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los del lazareto (los apunta {@link MorvainEnemigos}). */
    public List<Espera> esperas() {
        return esperas;
    }

    /** Los sitios con nombre (la calzada, la puerta, cada planta, la palanca, el atril...): {x, y, z} (y: los pies). */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    private void sitio(String nombre, int u, int y, int v) {
        sitios.put(nombre, new int[]{CX + u, y, CZ + v});
    }

    // ------------------------------------------------------------------ utiles

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

    /** El suelo de la planta k (el bloque que se pisa): -1 el sotano, 0 la planta baja... 5 la Sala del Elixir, 6 el tejado. */
    public static int piso(int k) {
        return ISLA + ALTO * k;
    }

    static double r(int u, int v) {
        return Math.hypot(u, v);
    }

    /** El angulo de (u, v) en grados, de 0 a 360 (0 al este, 90 al sur). */
    static double ang(int u, int v) {
        double a = Math.toDegrees(Math.atan2(v, u));
        return a < 0 ? a + 360 : a;
    }

    /** Lo lejos (en grados) entre dos angulos. */
    static double dif(double a, double b) {
        double d = Math.abs(a - b) % 360;
        return d > 180 ? 360 - d : d;
    }

    static BlockState vela(int n, boolean lit) {
        return Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, Math.max(1, Math.min(4, n))).setValue(CandleBlock.LIT, lit);
    }

    static BlockState calavera(int giro) {
        return Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, Math.floorMod(giro, 16));
    }

    static BlockState trampilla(Block k, Direction f, boolean arriba, boolean abierta) {
        return k.defaultBlockState().setValue(TrapDoorBlock.FACING, f).setValue(TrapDoorBlock.HALF, arriba ? Half.TOP : Half.BOTTOM)
                .setValue(TrapDoorBlock.OPEN, abierta);
    }

    static BlockState rayo(Direction f) {
        return Blocks.LIGHTNING_ROD.defaultBlockState().setValue(LightningRodBlock.FACING, f);
    }

    private void put(int u, int y, int v, BlockState s) {
        plano.put(x(u), y, z(v), s);
    }

    private void une(int u, int y, int v, BlockState s) {
        plano.une(x(u), y, z(v), s);
    }

    private void aire(int u, int y, int v) {
        plano.aire(x(u), y, z(v));
    }

    /** Si en (u, y, v) hay algo escrito que no es aire ni agua. */
    private boolean macizo(int u, int y, int v) {
        BlockState s = plano.en(x(u), y, z(v));
        return s != null && !s.isAir() && !s.is(Blocks.WATER);
    }

    /** Si (u, y, v) es aire escrito (un hueco de la torre: ahi se pueden poner muebles). */
    private boolean libre(int u, int y, int v) {
        BlockState s = plano.en(x(u), y, z(v));
        return s != null && s.isAir();
    }

    private void enLibre(int u, int y, int v, BlockState s) {
        if (libre(u, y, v)) put(u, y, v, s);
    }

    // ------------------------------------------------------------------ los materiales

    /** La piedra de la torre: pizarra oscura, ladrillo de pizarra agrietado y algo de toba; con musgo y barro cerca del agua. */
    static BlockState piedra(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 6);
        if (y <= ISLA + 3 && p < 30)
            return p < 12 ? b(Blocks.MOSSY_COBBLESTONE) : p < 20 ? b(Blocks.MUD_BRICKS) : b(Blocks.MOSSY_STONE_BRICKS);
        return p < 36 ? b(Blocks.DEEPSLATE_BRICKS) : p < 52 ? b(Blocks.CRACKED_DEEPSLATE_BRICKS) : p < 66 ? b(Blocks.DEEPSLATE_TILES)
                : p < 76 ? b(Blocks.POLISHED_DEEPSLATE) : p < 84 ? b(Blocks.COBBLED_DEEPSLATE) : p < 92 ? b(Blocks.TUFF)
                : b(Blocks.CRACKED_DEEPSLATE_TILES);
    }

    /** La piedra del islote y la calzada: sillares viejos con musgo. */
    static BlockState sillar(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 7);
        return p < 34 ? b(Blocks.STONE_BRICKS) : p < 58 ? b(Blocks.MOSSY_STONE_BRICKS) : p < 74 ? b(Blocks.CRACKED_STONE_BRICKS)
                : p < 86 ? b(Blocks.MOSSY_COBBLESTONE) : b(Blocks.ANDESITE);
    }

    /** El cobre de las bandas, las costillas de la cupula y los alambiques (encerado: no se pone verde mas de lo que esta). */
    static BlockState cobre(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 8);
        return p < 55 ? b(Blocks.WAXED_OXIDIZED_CUT_COPPER) : p < 85 ? b(Blocks.WAXED_WEATHERED_CUT_COPPER) : b(Blocks.WAXED_EXPOSED_CUT_COPPER);
    }

    /** El cristal de la cupula: verde, verde lima y ahumado. */
    static BlockState cristal(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 9);
        return p < 45 ? b(Blocks.GREEN_STAINED_GLASS) : p < 70 ? b(Blocks.LIME_STAINED_GLASS) : p < 90 ? b(Blocks.TINTED_GLASS)
                : b(Blocks.GLASS);
    }

    /** Lo de arriba del islote: barro, tierra gruesa, musgo y podsol (a manchas). */
    static BlockState manto(int x, int y, int z) {
        double n = Mina.ruido(x * 0.33, z * 0.29) + 0.4 * Mina.ruido(x * 0.9 + 5, z * 0.8 - 2) + (Hash.f(x, y, z, SEMILLA + 4) - 0.5) * 0.5;
        return n < -0.55 ? b(Blocks.MUD) : n < -0.1 ? b(Blocks.MOSS_BLOCK) : n < 0.3 ? b(Blocks.COARSE_DIRT) : n < 0.65 ? b(Blocks.PODZOL)
                : b(Blocks.GRASS_BLOCK);
    }

    // ------------------------------------------------------------------ el terreno: la laguna, el islote y la orilla

    /** Lo lejos de (u, v) al centro, con el borde ondulado (para el islote y la laguna). */
    static double borde(int u, int v) {
        return r(u, v) + Mina.ruido(u * 0.21, v * 0.21) * 2.4;
    }

    /** La altura natural del terreno (la de antes del lazareto). */
    static int alto(int x, int z) {
        return (int) Math.floor(Terreno.columna(x, z).altura());
    }

    /** Lo alto (el bloque de arriba) del islote, de la laguna (su fondo) o de la orilla en (u, v); y si hay agua encima. */
    static int superficie(int u, int v) {
        double d = borde(u, v);
        int t = alto(x(u), z(v));
        if (d < R_ISLA - 3) return ISLA;
        if (d < R_ISLA) return (int) Math.round(Ruido.lerp((d - (R_ISLA - 3)) / 3.0, ISLA, AGUA + 1));
        if (d < R_LAGUNA) return fondo(u, v);
        if (d < R_LAGUNA + 9) return Math.max(AGUA + 1, (int) Math.round(Ruido.lerp((d - R_LAGUNA) / 9.0, AGUA + 1, t)));
        return t;
    }

    /** El fondo de la laguna (de 59 a 61, mas hondo hacia el medio del agua). */
    static int fondo(int u, int v) {
        double d = borde(u, v);
        double m = Math.min(d - R_ISLA, R_LAGUNA - d);
        int f = AGUA - 2 - (int) Math.min(2, Math.max(0, m / 5));
        return f - (Hash.pct(x(u), 0, z(v), SEMILLA + 11) < 25 ? 1 : 0);
    }

    static boolean enLaguna(int u, int v) {
        double d = borde(u, v);
        return d >= R_ISLA && d < R_LAGUNA;
    }

    private final class Obra extends Element {
        Obra() {
            super(SEMILLA + 51);
            this.layer = 1;
            bounds(X0, PS - 4, Z0, X1, 140, Z1);
        }

        @Override
        public void build(Canvas c) {
            for (int x = Math.max(c.minX, x0); x <= Math.min(c.maxX, x1); x++)
                for (int z = Math.max(c.minZ, z0); z <= Math.min(c.maxZ, z1); z++) columna(c, x, z);
        }
    }

    static void columna(Canvas c, int x, int z) {
        int u = x - CX, v = z - CZ;
        if (Math.hypot(u - SANT_U, v - SANT_V) <= SantuariosMorvain.R + 0.5) return;
        double d = borde(u, v);
        if (d >= R_LAGUNA + 9) return;
        int t = alto(x, z);
        if (enLaguna(u, v)) {
            int f = fondo(u, v);
            for (int y = Math.min(t, f) - 2; y < f; y++) c.set(x, y, z, b(Blocks.DIRT));
            int p = Hash.pct(x, f, z, SEMILLA + 12);
            c.set(x, f, z, p < 50 ? b(Blocks.MUD) : p < 75 ? b(Blocks.CLAY) : p < 90 ? b(Blocks.GRAVEL) : b(Blocks.MUDDY_MANGROVE_ROOTS));
            for (int y = f + 1; y <= AGUA; y++) c.set(x, y, z, Pal.WATER);
            for (int y = AGUA + 1; y <= Math.max(t, AGUA) + 4; y++) c.set(x, y, z, Pal.AIR);
            return;
        }
        int h = superficie(u, v);
        for (int y = h + 1; y <= Math.max(t, h) + 4; y++) c.set(x, y, z, Pal.AIR);
        for (int y = Math.min(t, h) - 2; y < h; y++) c.set(x, y, z, y < h - 3 ? b(Blocks.STONE) : b(Blocks.DIRT));
        c.set(x, h, z, d < R_ISLA ? manto(x, h, z) : Hash.pct(x, h, z, SEMILLA + 13) < 40 ? b(Blocks.MUD) : manto(x, h, z));
    }

    // ------------------------------------------------------------------ la torre

    /** Si (u, v) cae en el muro de la torre / dentro / en el pozo. */
    static boolean muro(int u, int v) {
        double d = r(u, v);
        return d >= RI && d < RT;
    }

    static boolean dentro(int u, int v) {
        return r(u, v) < RI;
    }

    static boolean pozo(int u, int v) {
        return r(u, v) < RP;
    }

    /** Si (u, v) cae en la torreta (su muro incluido). */
    static boolean enTorreta(int u, int v) {
        return Math.hypot(u - TX, v) <= RTU + 0.5;
    }

    private void torre() {
        int R = (int) Math.ceil(RT);
        for (int u = -R; u <= R; u++)
            for (int v = -R; v <= R; v++) {
                double d = r(u, v);
                if (d >= RT) continue;
                // los cimientos
                for (int y = PS - 4; y < PS - 2; y++) put(u, y, v, piedra(x(u), y, z(v)));
                if (muro(u, v)) {
                    for (int y = PS - 2; y <= TEJADO; y++) put(u, y, v, piedra(x(u), y, z(v)));
                    // las bandas de cobre de fuera, en cada planta
                    if (d >= RT - 0.75)
                        for (int k = 0; k <= 6; k++) put(u, piso(k), v, cobre(x(u), piso(k), z(v)));
                    // las almenas: merlones en el borde de fuera, uno si y otro no
                    if (d >= RT - 1.1) {
                        int merlon = ((int) Math.floor(ang(u, v) / 9.0)) % 2;
                        if (merlon == 0) {
                            put(u, TEJADO + 1, v, piedra(x(u), TEJADO + 1, z(v)));
                            put(u, TEJADO + 2, v, Mina.losa(Blocks.DEEPSLATE_BRICK_SLAB, SlabType.BOTTOM));
                        } else put(u, TEJADO + 1, v, Mina.losa(Blocks.DEEPSLATE_TILE_SLAB, SlabType.BOTTOM));
                    }
                    continue;
                }
                // dentro: el suelo del sotano (con la parrilla del atanor en el pozo) y los suelos de cada planta
                put(u, PS - 2, v, piedra(x(u), PS - 2, z(v)));
                if (pozo(u, v)) {
                    put(u, PS - 2, v, Mina.tronco(Blocks.HAY_BLOCK, Direction.Axis.Y));
                    put(u, PS - 1, v, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false).setValue(CampfireBlock.SIGNAL_FIRE, true));
                    put(u, PS, v, trampilla(Blocks.IRON_TRAPDOOR, Direction.NORTH, true, false));
                } else {
                    put(u, PS - 1, v, piedra(x(u), PS - 1, z(v)));
                    put(u, PS, v, sueloSotano(u, v));
                }
                for (int k = 0; k <= 5; k++) {
                    int y = piso(k);
                    if (pozo(u, v) && k < 5) aire(u, y, v);
                    else put(u, y, v, suelo(k, u, v));
                    for (int h = 1; h < ALTO; h++) aire(u, y + h, v);
                }
                for (int h = 1; h < ALTO; h++) aire(u, PS + h, v);
            }
        // la puerta de la planta baja: al sur, un arco apuntado de tres de ancho
        for (int v = 8; v <= 12; v++)
            for (int u = -1; u <= 1; u++) {
                for (int y = ISLA + 1; y <= ISLA + 3; y++) aire(u, y, v);
                if (u == 0) aire(u, ISLA + 4, v);
                if (v >= 9) put(u, ISLA, v, sillar(x(u), ISLA, z(v)));
            }
        // el dintel de cobre y dos faroles a los lados de la puerta
        put(0, ISLA + 5, 11, cobre(x(0), ISLA + 5, z(11)));
        put(-2, ISLA + 3, 12, Mina.farol(true, false));
        put(2, ISLA + 3, 12, Mina.farol(true, false));
        put(-2, ISLA + 2, 12, piedra(x(-2), ISLA + 2, z(12)));
        put(2, ISLA + 2, 12, piedra(x(2), ISLA + 2, z(12)));
        // las ventanas de cada planta (de la planta baja a la Sala del Elixir), altas y estrechas, con vidriera verde o ahumada
        for (int k = 0; k <= 5; k++)
            for (double a : VENTANAS) {
                if (k == 0 && dif(a, 90) < 30) continue;
                double rad = Math.toRadians(a);
                Set<Long> hechas = new HashSet<>();
                for (double d = RI - 0.4; d <= RT + 0.4; d += 0.35) {
                    int u = (int) Math.round(d * Math.cos(rad)), v = (int) Math.round(d * Math.sin(rad));
                    if (!muro(u, v) || enTorreta(u, v) || !hechas.add(BlockPos.asLong(u, 0, v))) continue;
                    boolean fuera = r(u, v) >= RT - 1.0;
                    for (int y = piso(k) + 3; y <= piso(k) + 6; y++) {
                        int p = pct(u, y, v, 21 + k);
                        BlockState s = !fuera ? Pal.AIR : k == 5 ? b(Blocks.GREEN_STAINED_GLASS_PANE)
                                : p < 18 ? Pal.AIR : p < 60 ? b(Blocks.GRAY_STAINED_GLASS_PANE) : b(Blocks.GREEN_STAINED_GLASS_PANE);
                        if (s.isAir()) aire(u, y, v);
                        else une(u, y, v, s);
                    }
                    // el arco de arriba: un escalon boca abajo hacia dentro
                    put(u, piso(k) + 7, v, piedra(x(u), piso(k) + 7, z(v)));
                }
                ventanas++;
            }
    }

    /** El suelo de la planta k en (u, v). */
    static BlockState suelo(int k, int u, int v) {
        int x = x(u), y = piso(k), z = z(v);
        int p = Hash.pct(x, y, z, SEMILLA + 30 + k);
        double d = r(u, v);
        return switch (k) {
            case 0 -> d >= 5 && d < 5.7 ? cobre(x, y, z) : (u + v & 1) == 0 ? b(Blocks.POLISHED_ANDESITE) : p < 70 ? b(Blocks.STONE_BRICKS)
                    : b(Blocks.CRACKED_STONE_BRICKS);
            case 1, 2 -> p < 70 ? b(Blocks.SPRUCE_PLANKS) : p < 88 ? b(Blocks.DARK_OAK_PLANKS) : b(Blocks.STRIPPED_SPRUCE_WOOD);
            case 3 -> p < 80 ? b(Blocks.DARK_OAK_PLANKS) : b(Blocks.SPRUCE_PLANKS);
            case 4 -> (u + v & 1) == 0 ? b(Blocks.POLISHED_DEEPSLATE) : p < 80 ? b(Blocks.DEEPSLATE_TILES) : b(Blocks.CRACKED_DEEPSLATE_TILES);
            default -> d >= 5.5 && d < 6.2 || (Math.abs(u) == 0 || Math.abs(v) == 0) && d >= 3 && d < 5.5 ? cobre(x, y, z)
                    : p < 75 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS) : b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
        };
    }

    static BlockState sueloSotano(int u, int v) {
        int p = pct(u, PS, v, 31);
        return p < 40 ? b(Blocks.COBBLED_DEEPSLATE) : p < 65 ? b(Blocks.DEEPSLATE_BRICKS) : p < 80 ? b(Blocks.MUD_BRICKS) : p < 92 ? b(Blocks.TUFF)
                : b(Blocks.PACKED_MUD);
    }

    /** La barandilla del pozo en la planta k (barrotes; en las de arriba, una cancela al norte para salir del tiro). */
    private void barandilla(int k) {
        int y = piso(k) + 1;
        for (int u = -3; u <= 3; u++)
            for (int v = -3; v <= 3; v++) {
                double d = r(u, v);
                if (d < RP || d >= 3.5) continue;
                if (k >= 1 && u == 0 && v == -3)
                    put(u, y, v, Blocks.OAK_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING, Direction.NORTH));
                else une(u, y, v, b(Blocks.IRON_BARS));
            }
    }

    // ------------------------------------------------------------------ la torreta de la escalera (al este)

    /** Donde se pisa el escalon del sector s (0..PASOS-1, angulo creciente de u hacia v) en la vuelta n (la planta n). */
    static int pisaEscalon(int s, int n) {
        return piso(n) + 1 + (s - PASOS / 2);
    }

    /** El tramo hundido: los escalones que faltan entre la sala de los enfermos y el herbolario. */
    static boolean hundido(int s, int n) {
        return n == 1 && s >= 7 || n == 2 && s <= 2;
    }

    private void torreta() {
        int y0 = PS, y1 = TEJADO + 3;
        for (int u = TX - 5; u <= TX + 5; u++)
            for (int v = -5; v <= 5; v++) {
                double d = Math.hypot(u - TX, v);
                if (d > RTU + 0.5) continue;
                boolean borde = d > RTU - 0.9;
                for (int y = y0 - 2; y <= y1; y++) {
                    if (borde || y == y0 - 2 || y == y0 - 1) put(u, y, v, piedra(x(u), y, z(v)));
                    else if (d < 1.0) put(u, y, v, y >= y1 - 1 ? Mina.farol(true, false) : b(Blocks.POLISHED_DEEPSLATE));
                    else if (y == y0) put(u, y, v, sueloSotano(u, v));
                    else aire(u, y, v);
                }
                // el tejado conico (pizarra) y la punta de cobre
                int alto = (int) Math.round((RTU + 0.5 - d) * 2.3);
                for (int y = y1 + 1; y <= y1 + 1 + alto; y++)
                    put(u, y, v, d > RTU - 0.4 ? Mina.losa(Blocks.DEEPSLATE_TILE_SLAB, SlabType.BOTTOM) : b(Blocks.DEEPSLATE_TILES));
                if (d < 0.5) {
                    put(u, y1 + 2 + alto, v, cobre(x(u), y1 + 2 + alto, z(v)));
                    put(u, y1 + 3 + alto, v, rayo(Direction.UP));
                }
            }
        // los escalones (del sotano al tejado), salvo el tramo hundido entre la planta 1 y la 2
        for (int u = TX - 4; u <= TX + 4; u++)
            for (int v = -4; v <= 4; v++) {
                double d = Math.hypot(u - TX, v);
                if (d < 1.0 || d > RTU - 0.9) continue;
                double a = Math.atan2(v, u - TX);
                if (a < 0) a += Math.PI * 2;
                int sct = (int) (a / (Math.PI * 2 / PASOS));
                Direction mira = sube((sct + 0.5) * Math.PI * 2 / PASOS);
                for (int n = -1; n <= 6; n++) {
                    int y = pisaEscalon(sct, n);
                    if (y < PS + 1 || y > TEJADO + 1 || hundido(sct, n)) continue;
                    put(u, y - 1, v, sct == PASOS / 2 ? b(Blocks.POLISHED_DEEPSLATE) : Mina.escalon(Blocks.DEEPSLATE_BRICK_STAIRS, mira));
                }
                // arriba (despues del descansillo del tejado), suelo
                if (sct > PASOS / 2) put(u, TEJADO, v, b(Blocks.POLISHED_DEEPSLATE));
            }
        // las puertas: al oeste (hacia la torre), una en cada planta, en el sotano y en el tejado
        for (int n = -1; n <= 6; n++) {
            int y = piso(n);
            for (int u = 8; u <= TX - 2; u++) {
                if (muro(u, 0) || enTorreta(u, 0) && Math.hypot(u - TX, 0) > RTU - 0.9) for (int h = 1; h <= 3; h++) aire(u, y + h, 0);
                if (muro(u, 0)) put(u, y, 0, b(Blocks.POLISHED_DEEPSLATE));
            }
        }
        // las saeteras de la torreta (al este), en cada planta
        for (int n = -1; n <= 6; n++)
            for (int h = 5; h <= 6; h++) une(TX + 3, piso(n) + h, 0, b(Blocks.IRON_BARS));
    }

    /** Hacia donde mira un escalon (la direccion en que se sube) en el angulo a (sentido de u hacia v). */
    private static Direction sube(double a) {
        double tx = -Math.sin(a), tz = Math.cos(a);
        if (Math.abs(tx) > Math.abs(tz)) return tx > 0 ? Direction.EAST : Direction.WEST;
        return tz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    // ------------------------------------------------------------------ el sotano del atanor

    private void sotano() {
        int y = PS + 1;
        // el atanor: el anillo de ladrillo alrededor de la parrilla, con su puerta al norte y mirillas con barrotes
        for (int u = -5; u <= 5; u++)
            for (int v = -5; v <= 5; v++) {
                double d = r(u, v);
                if (d < RP || d >= 4.6) continue;
                boolean puerta = u == 0 && v <= -3;
                for (int h = 0; h <= 3; h++) {
                    if (puerta && h <= 2) continue;
                    int p = pct(u, y + h, v, 51);
                    BlockState s = p < 55 ? b(Blocks.BRICKS) : p < 80 ? b(Blocks.MUD_BRICKS) : b(Blocks.NETHER_BRICKS);
                    if (h == 1 && !puerta && (dif(ang(u, v), 0) < 12 || dif(ang(u, v), 90) < 12 || dif(ang(u, v), 180) < 12) && d >= 3.5)
                        s = b(Blocks.IRON_BARS);
                    put(u, y + h, v, s);
                }
                // la corona del horno: piedra del nether (ahi arde el fuego cuando se enciende)
                put(u, y + 4, v, d < 3.6 ? b(Blocks.NETHERRACK) : Mina.losa(Blocks.BRICK_SLAB, SlabType.BOTTOM));
            }
        // la palanca, a la derecha de la puerta del horno
        put(PALANCA_U, y + 1, PALANCA_V, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.WALL)
                .setValue(LeverBlock.FACING, Direction.NORTH).setValue(LeverBlock.POWERED, false));
        // los tubos de cobre que suben del horno al techo (pararrayos de pie) y cadenas
        for (double a : new double[]{45, 135, 225, 315}) {
            double rad = Math.toRadians(a);
            int u = (int) Math.round(8.3 * Math.cos(rad)), v = (int) Math.round(8.3 * Math.sin(rad));
            for (int h = 0; h < ALTO - 1; h++) put(u, y + h, v, rayo(Direction.UP));
        }
        // las cisternas (agua parada en el suelo, al este y al oeste) y el borde
        for (int u = -8; u <= 8; u++)
            for (int v = -3; v <= 3; v++) {
                if (u > -6 || r(u, v) >= RI - 0.8) continue;
                put(u, PS, v, Pal.WATER);
                put(u, PS - 1, v, b(Blocks.MUD_BRICKS));
            }
        // el carbon, los barriles y una mesa de trabajo
        for (int[] q : new int[][]{{5, 6}, {6, 5}, {4, 7}, {6, 6}}) {
            put(q[0], y, q[1], b(Blocks.COAL_BLOCK));
            if ((q[0] + q[1]) % 2 == 0) put(q[0], y + 1, q[1], Mina.losa(Blocks.BLACKSTONE_SLAB, SlabType.BOTTOM));
        }
        put(-5, y, 6, b(Blocks.BARREL));
        put(-6, y, 5, b(Blocks.BARREL));
        put(-5, y + 1, 6, b(Blocks.BARREL));
        put(-4, y, 7, b(Blocks.CRAFTING_TABLE));
        put(-3, y, 7, b(Blocks.SMITHING_TABLE));
        // las telaranas y las cadenas con farol de los rincones
        for (int[] q : new int[][]{{-7, -5}, {7, -5}, {-7, 5}, {6, -6}}) {
            put(q[0], piso(0) - 1, q[1], b(Blocks.COBWEB));
        }
        for (double a : new double[]{0, 90, 180, 270}) {
            double rad = Math.toRadians(a + 30);
            int u = (int) Math.round(6.5 * Math.cos(rad)), v = (int) Math.round(6.5 * Math.sin(rad));
            put(u, piso(0) - 1, v, Mina.cadena(Direction.Axis.Y));
            put(u, piso(0) - 2, v, Mina.farol(true, true));
        }
    }

    /** Lo que se cambia al encender el atanor: las hogueras de la parrilla y el fuego de la corona. */
    static List<BlockPos> hogueras() {
        List<BlockPos> l = new ArrayList<>();
        for (int u = -2; u <= 2; u++)
            for (int v = -2; v <= 2; v++) if (pozo(u, v)) l.add(new BlockPos(x(u), PS - 1, z(v)));
        return l;
    }

    static List<BlockPos> corona() {
        List<BlockPos> l = new ArrayList<>();
        for (int u = -4; u <= 4; u++)
            for (int v = -4; v <= 4; v++) {
                double d = r(u, v);
                if (d >= RP && d < 3.6 && !(u == 0 && v <= -3)) l.add(new BlockPos(x(u), PS + 6, z(v)));
            }
        return l;
    }

    // ------------------------------------------------------------------ la planta baja: el vestibulo de los frascos

    private void vestibulo() {
        int y = piso(0) + 1;
        barandilla(0);
        // las estanterias de los frascos, contra el muro del oeste
        for (int u = -10; u <= 10; u++)
            for (int v = -10; v <= 10; v++) {
                double d = r(u, v), a = ang(u, v);
                if (d < 8.2 || d >= RI || a < 125 || a > 235) continue;
                if (ventanaEn(a, 0)) continue;
                int p = pct(u, y, v, 61);
                put(u, y, v, p < 50 ? b(Blocks.BARREL) : Mina.escalonAlto(Blocks.SPRUCE_STAIRS, haciaDentro(u, v)));
                put(u, y + 1, v, frasco(u, y + 1, v));
                put(u, y + 2, v, Mina.losa(Blocks.SPRUCE_SLAB, SlabType.TOP));
                put(u, y + 3, v, frasco(u, y + 3, v));
                put(u, y + 4, v, Mina.losa(Blocks.SPRUCE_SLAB, SlabType.TOP));
                if (pct(u, y + 5, v, 62) < 50) put(u, y + 5, v, frasco(u, y + 5, v));
                estanterias++;
            }
        // los bancos de los que esperaban (al noreste y al sureste, de cara al pozo)
        for (double a : new double[]{30, 45, 315, 330}) {
            double rad = Math.toRadians(a);
            int u = (int) Math.round(7.4 * Math.cos(rad)), v = (int) Math.round(7.4 * Math.sin(rad));
            enLibre(u, y, v, Mina.escalon(Blocks.SPRUCE_STAIRS, haciaFuera(u, v)));
        }
        // dos camillas al noreste, con su sabana
        camilla(0, 300);
        camilla(0, 245);
        // el mostrador del fisico, al norte, con un atril vacio y velas
        for (int u = -3; u <= 3; u++) {
            int v = -7;
            if (Math.abs(u) == 3) put(u, y, v, Mina.escalonAlto(Blocks.SPRUCE_STAIRS, Direction.SOUTH));
            else put(u, y, v, b(Blocks.SPRUCE_PLANKS));
            if (u == -2) put(u, y + 1, v, vela(3, false));
            if (u == 2) put(u, y + 1, v, Blocks.BREWING_STAND.defaultBlockState());
            if (u == 0) put(u, y + 1, v, Blocks.POTTED_DEAD_BUSH.defaultBlockState());
        }
        put(0, y, -8, Blocks.LECTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LecternBlock.FACING, Direction.SOUTH));
        // faroles colgados y telaranas
        faroles(0);
        for (int[] q : new int[][]{{-6, -6}, {6, 7}, {-7, 6}}) enLibre(q[0], piso(1) - 1, q[1], b(Blocks.COBWEB));
    }

    /** Un frasco de las estanterias: tiestos con lo seco, alambiques pequenos, velas, jarras... */
    private static BlockState frasco(int u, int y, int v) {
        int p = pct(u, y, v, 63);
        return p < 22 ? Blocks.BREWING_STAND.defaultBlockState() : p < 40 ? Blocks.POTTED_DEAD_BUSH.defaultBlockState()
                : p < 52 ? Blocks.POTTED_BROWN_MUSHROOM.defaultBlockState() : p < 62 ? Blocks.DECORATED_POT.defaultBlockState()
                : p < 72 ? vela(1 + p % 3, false) : p < 80 ? Blocks.POTTED_CRIMSON_FUNGUS.defaultBlockState() : p < 86 ? calavera(p % 16)
                : Pal.AIR;
    }

    static boolean ventanaEn(double a, int k) {
        for (double w : VENTANAS) if (dif(a, w) < 7 && !(k == 0 && dif(w, 90) < 30)) return true;
        return false;
    }

    /** La direccion (de las cuatro) mas cercana de (u, v) hacia el centro / hacia fuera. */
    static Direction haciaDentro(int u, int v) {
        return Math.abs(u) > Math.abs(v) ? (u > 0 ? Direction.WEST : Direction.EAST) : (v > 0 ? Direction.NORTH : Direction.SOUTH);
    }

    static Direction haciaFuera(int u, int v) {
        return haciaDentro(u, v).getOpposite();
    }

    /** Una camilla en la planta k, a lo largo del radio del angulo a: dos de largo (r 5 y 6), con su sabana y un poste a la cabeza. */
    private void camilla(int k, double a) {
        int y = piso(k) + 1;
        double rad = Math.toRadians(a);
        for (double d : new double[]{5.0, 6.0}) {
            int u = (int) Math.round(d * Math.cos(rad)), v = (int) Math.round(d * Math.sin(rad));
            if (!libre(u, y, v)) continue;
            put(u, y, v, Mina.losa(Blocks.SPRUCE_SLAB, SlabType.TOP));
            put(u, y + 1, v, pct(u, y, v, 64) < 70 ? b(Blocks.WHITE_CARPET) : b(Blocks.LIGHT_GRAY_CARPET));
        }
        int u = (int) Math.round(7.0 * Math.cos(rad)), v = (int) Math.round(7.0 * Math.sin(rad));
        enLibre(u, y, v, b(Blocks.SPRUCE_FENCE));
        camillas++;
    }

    /** Faroles colgados del techo de la planta k (cuatro, en r 6). */
    private void faroles(int k) {
        int techo = piso(k + 1) - 1;
        for (double a : new double[]{60, 150, 240, 330}) {
            double rad = Math.toRadians(a);
            int u = (int) Math.round(6.0 * Math.cos(rad)), v = (int) Math.round(6.0 * Math.sin(rad));
            if (!libre(u, techo, v)) continue;
            put(u, techo, v, Mina.cadena(Direction.Axis.Y));
            put(u, techo - 1, v, Mina.farol(true, true));
        }
    }

    // ------------------------------------------------------------------ planta 1: la sala de los enfermos

    private void enfermeria() {
        int y = piso(1) + 1;
        barandilla(1);
        for (double a : new double[]{30, 60, 120, 150, 210, 240, 300, 330}) camilla(1, a);
        // las cortinas entre las camillas (cristal blanco, del suelo a la altura de un hombre)
        for (double a : new double[]{45, 135, 225, 315}) {
            double rad = Math.toRadians(a);
            for (double d = 4.4; d <= 8.0; d += 0.5) {
                int u = (int) Math.round(d * Math.cos(rad)), v = (int) Math.round(d * Math.sin(rad));
                for (int h = 0; h <= 1; h++) if (libre(u, y + h, v)) une(u, y + h, v, b(Blocks.WHITE_STAINED_GLASS_PANE));
            }
        }
        // los calderos y un balde; un arcon junto al muro del sur
        enLibre(0, y, 7, Blocks.CAULDRON.defaultBlockState());
        enLibre(-7, y, -1, Blocks.CAULDRON.defaultBlockState());
        enLibre(-7, y, 1, Blocks.WATER_CAULDRON.defaultBlockState());
        faroles(1);
        for (int[] q : new int[][]{{-5, -7}, {7, 5}, {-8, 3}}) enLibre(q[0], piso(2) - 1, q[1], b(Blocks.COBWEB));
    }

    // ------------------------------------------------------------------ planta 2: el herbolario

    private void herbolario() {
        int y = piso(2) + 1;
        barandilla(2);
        // los secaderos: postes con manojos (hojas de azalea) y raices colgando del techo
        for (double a : new double[]{20, 70, 110, 160, 200, 250, 290, 340}) {
            double rad = Math.toRadians(a);
            int u = (int) Math.round(6.6 * Math.cos(rad)), v = (int) Math.round(6.6 * Math.sin(rad));
            if (!libre(u, y, v)) continue;
            for (int h = 0; h <= 2; h++) une(u, y + h, v, b(Blocks.SPRUCE_FENCE));
            put(u, y + 3, v, (pct(u, y, v, 71) < 50 ? b(Blocks.AZALEA_LEAVES) : b(Blocks.FLOWERING_AZALEA_LEAVES))
                    .setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true));
        }
        int techo = piso(3) - 1;
        for (int u = -9; u <= 9; u++)
            for (int v = -9; v <= 9; v++) {
                double d = r(u, v);
                if (d < 4 || d >= 8.5 || pct(u, techo, v, 72) > 22) continue;
                enLibre(u, techo, v, b(Blocks.HANGING_ROOTS));
            }
        // los sacos y los barriles contra el muro, las balas de heno, el mortero grande
        for (int u = -10; u <= 10; u++)
            for (int v = -10; v <= 10; v++) {
                double d = r(u, v), a = ang(u, v);
                if (d < 8.3 || d >= RI || ventanaEn(a, 2) || dif(a, 0) < 20) continue;
                int p = pct(u, y, v, 73);
                put(u, y, v, p < 35 ? b(Blocks.BROWN_WOOL) : p < 60 ? b(Blocks.BARREL) : p < 80 ? Mina.tronco(Blocks.HAY_BLOCK, Direction.Axis.Y)
                        : b(Blocks.COMPOSTER));
                if (p < 45) put(u, y + 1, v, p < 20 ? b(Blocks.BROWN_WOOL) : Mina.tronco(Blocks.HAY_BLOCK, Direction.Axis.Y));
            }
        enLibre(-4, y, 4, Blocks.CAULDRON.defaultBlockState());
        enLibre(-5, y, 4, Blocks.GRINDSTONE.defaultBlockState());
        enLibre(4, y, -5, Blocks.STONECUTTER.defaultBlockState());
        for (int[] q : new int[][]{{5, 4}, {-4, -5}, {6, 2}}) enLibre(q[0], y, q[1], Blocks.POTTED_FERN.defaultBlockState());
        faroles(2);
    }

    // ------------------------------------------------------------------ planta 3: la biblioteca (y el muro de la celda)

    private void biblioteca() {
        int y = piso(3) + 1;
        barandilla(3);
        // las estanterias contra el muro, de cuatro de alto, entre las ventanas (las dos del oeste en v = 0 son el muro ilusorio)
        for (int u = -10; u <= 10; u++)
            for (int v = -10; v <= 10; v++) {
                double d = r(u, v), a = ang(u, v);
                if (d < 8.3 || d >= RI || ventanaEn(a, 3) || dif(a, 0) < 14) continue;
                for (int h = 0; h <= 3; h++) put(u, y + h, v, b(Blocks.BOOKSHELF));
                estanterias++;
            }
        // detras de las dos estanterias del muro ilusorio, el paso por el muro hasta la celda
        for (int u = -11; u <= -10; u++)
            for (int h = 0; h <= 1; h++) aire(u, y + h, 0);
        // el anillo de dentro: estanterias de dos de alto en cuatro trozos
        for (double a0 : new double[]{25, 115, 205, 295}) {
            for (double a = a0; a <= a0 + 40; a += 4) {
                double rad = Math.toRadians(a);
                int u = (int) Math.round(6.2 * Math.cos(rad)), v = (int) Math.round(6.2 * Math.sin(rad));
                if (!libre(u, y, v)) continue;
                put(u, y, v, b(Blocks.BOOKSHELF));
                put(u, y + 1, v, b(Blocks.BOOKSHELF));
                put(u, y + 2, v, Mina.losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
            }
        }
        // la mesa de lectura con velas y una calavera, la alfombra, el atril del libro (lo pone un trozo) y otro vacio
        for (int u = 3; u <= 5; u++) {
            put(u, y, 3, Mina.losa(Blocks.DARK_OAK_SLAB, SlabType.TOP));
            if (u == 3) put(u, y + 1, 3, vela(2, false));
            if (u == 5) put(u, y + 1, 3, calavera(6));
        }
        put(4, y, 4, Mina.escalon(Blocks.DARK_OAK_STAIRS, Direction.SOUTH));
        put(2, y, -4, Blocks.LECTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LecternBlock.FACING, Direction.NORTH));
        for (int u = -9; u <= 9; u++)
            for (int v = -9; v <= 9; v++) {
                double d = r(u, v);
                if (d >= 3.6 && d < 5.4 && dif(ang(u, v), 270) > 20) enLibre(u, y, v, b(Blocks.RED_CARPET));
            }
        // una escala en las estanterias del sur
        enLibre(0, y, 8, Mina.escala(Direction.NORTH));
        enLibre(0, y + 1, 8, Mina.escala(Direction.NORTH));
        faroles(3);
    }

    /** Las celdas del muro ilusorio de la biblioteca: las dos estanterias del oeste en v = 0. */
    public static List<BlockPos> muro(String id) {
        List<BlockPos> l = new ArrayList<>();
        if (MURO_CELDA.equals(id))
            for (int h = 1; h <= 2; h++) l.add(new BlockPos(x(-9), piso(3) + h, z(0)));
        return l;
    }

    /** Lo que hay en una celda del muro ilusorio cuando esta cerrado: una estanteria mas. */
    public static BlockState bloqueMuro(String id, BlockPos p) {
        return b(Blocks.BOOKSHELF);
    }

    /** La celda de Galen: un cuarto colgado del muro del oeste a la altura de la biblioteca (sobre mensulas), con el arcon bueno. */
    private void celda() {
        int y0 = piso(3);
        for (int u = -16; u <= -11; u++)
            for (int v = -3; v <= 3; v++) {
                if (muro(u, v)) continue;
                boolean pared = u == -16 || Math.abs(v) == 3;
                put(u, y0, v, pared ? piedra(x(u), y0, z(v)) : b(Blocks.DARK_OAK_PLANKS));
                for (int h = 1; h <= 3; h++) {
                    if (pared) put(u, y0 + h, v, h == 2 && u == -16 && Math.abs(v) <= 1 ? b(Blocks.IRON_BARS) : piedra(x(u), y0 + h, z(v)));
                    else aire(u, y0 + h, v);
                }
                put(u, y0 + 4, v, Mina.losa(Blocks.DEEPSLATE_TILE_SLAB, SlabType.BOTTOM));
                put(u, y0 - 1, v, pared ? Mina.escalonAlto(Blocks.DEEPSLATE_BRICK_STAIRS, Direction.EAST) : piedra(x(u), y0 - 1, z(v)));
            }
        // las mensulas de debajo
        for (int v = -3; v <= 3; v += 3) {
            put(-12, y0 - 2, v, Mina.escalonAlto(Blocks.DEEPSLATE_BRICK_STAIRS, Direction.EAST));
            put(-12, y0 - 3, v, piedra(x(-12), y0 - 3, z(v)));
        }
        int y = y0 + 1;
        put(-15, y, -2, Mina.tronco(Blocks.HAY_BLOCK, Direction.Axis.X));
        put(-14, y, -2, Mina.tronco(Blocks.HAY_BLOCK, Direction.Axis.X));
        put(-15, y, 2, Mina.losa(Blocks.DARK_OAK_SLAB, SlabType.TOP));
        put(-15, y + 1, 2, Blocks.BREWING_STAND.defaultBlockState());
        put(-14, y, 2, Mina.losa(Blocks.DARK_OAK_SLAB, SlabType.TOP));
        put(-14, y + 1, 2, vela(4, false));
        put(-13, y + 2, 2, b(Blocks.COBWEB));
        put(-12, y, -2, calavera(4));
    }

    // ------------------------------------------------------------------ planta 4: el laboratorio

    private void laboratorio() {
        int y = piso(4) + 1;
        barandilla(4);
        // las mesas del anillo con lo de destilar encima
        for (double a = 0; a < 360; a += 7.5) {
            if (dif(a, 0) < 20 || dif(a, 270) < 18 || (a > 120 && a < 150) || (a > 210 && a < 240)) continue;
            double rad = Math.toRadians(a);
            int u = (int) Math.round(7.2 * Math.cos(rad)), v = (int) Math.round(7.2 * Math.sin(rad));
            if (!libre(u, y, v)) continue;
            put(u, y, v, Mina.losa(Blocks.POLISHED_ANDESITE_SLAB, SlabType.TOP));
            int p = pct(u, y, v, 81);
            put(u, y + 1, v, p < 30 ? Blocks.BREWING_STAND.defaultBlockState() : p < 45 ? vela(1 + p % 4, false)
                    : p < 58 ? Blocks.DECORATED_POT.defaultBlockState() : p < 68 ? b(Blocks.GLASS_PANE) : Pal.AIR);
        }
        // los dos alambiques grandes (al suroeste y al noroeste): base de cobre, panza de cristal verde y su tubo
        alambique(-5, 4);
        alambique(-5, -4);
        // los hornos encendidos contra el muro del este (a los lados de la puerta de la torreta)
        for (int v : new int[]{-4, 4}) {
            int u = (int) Math.round(Math.sqrt(8.6 * 8.6 - v * v));
            put(u, y, v, Blocks.FURNACE.defaultBlockState().setValue(net.minecraft.world.level.block.FurnaceBlock.FACING, Direction.WEST)
                    .setValue(net.minecraft.world.level.block.FurnaceBlock.LIT, true));
            put(u, y + 1, v, Blocks.BLAST_FURNACE.defaultBlockState().setValue(net.minecraft.world.level.block.BlastFurnaceBlock.FACING, Direction.WEST));
        }
        // los calderos con agua, la mesa de disecciones con sus cadenas y lo derramado (alfombras verdes) y cristales rotos
        enLibre(3, y, 7, Blocks.WATER_CAULDRON.defaultBlockState());
        enLibre(4, y, 6, Blocks.WATER_CAULDRON.defaultBlockState());
        for (int u = 3; u <= 5; u++) {
            put(u, y, -6, Mina.losa(Blocks.SMOOTH_STONE_SLAB, SlabType.TOP));
            put(u, y + 1, -6, u == 4 ? b(Blocks.RED_CARPET) : b(Blocks.WHITE_CARPET));
        }
        put(3, piso(5) - 1, -6, Mina.cadena(Direction.Axis.Y));
        put(5, piso(5) - 1, -6, Mina.cadena(Direction.Axis.Y));
        for (int u = -8; u <= 8; u++)
            for (int v = -8; v <= 8; v++) {
                int p = pct(u, y, v, 82);
                if (p < 7) enLibre(u, y, v, p < 4 ? b(Blocks.LIME_CARPET) : b(Blocks.GREEN_CARPET));
                else if (p < 9) enLibre(u, y, v, b(Blocks.GLASS_PANE));
            }
        faroles(4);
    }

    /** Un alambique grande con el centro en (u, v) de la planta 4. */
    private void alambique(int u0, int v0) {
        int y = piso(4) + 1;
        for (int du = -1; du <= 1; du++)
            for (int dv = -1; dv <= 1; dv++) {
                int u = u0 + du, v = v0 + dv;
                put(u, y, v, cobre(x(u), y, z(v)));
                boolean esquina = Math.abs(du) == 1 && Math.abs(dv) == 1;
                put(u, y + 1, v, esquina ? Pal.AIR : b(Blocks.LIME_STAINED_GLASS));
                put(u, y + 2, v, esquina ? Pal.AIR : b(Blocks.GREEN_STAINED_GLASS));
            }
        put(u0, y + 3, v0, b(Blocks.GLASS));
        put(u0, y + 4, v0, rayo(Direction.UP));
        alambiques++;
    }

    // ------------------------------------------------------------------ planta 5: la Sala del Elixir (el gran alambique)

    private void elixir() {
        int y = P5 + 1;
        // el gran alambique sobre el pozo: base de cobre, panza de cristal verde, cuello y el tubo hasta la cupula
        for (int u = -3; u <= 3; u++)
            for (int v = -3; v <= 3; v++) {
                double d = r(u, v);
                if (d < 2.6) put(u, y, v, cobre(x(u), y, z(v)));
                if (d < 2.3) {
                    for (int h = 1; h <= 4; h++) put(u, y + h, v, d < 1.5 ? b(Blocks.LIME_STAINED_GLASS) : b(Blocks.GREEN_STAINED_GLASS));
                }
                if (d < 1.2) {
                    put(u, y + 5, v, b(Blocks.GREEN_STAINED_GLASS));
                    put(u, y + 6, v, b(Blocks.GLASS));
                }
            }
        for (int yy = y + 7; yy <= TEJADO + (int) RD; yy++) put(0, yy, 0, rayo(Direction.UP));
        // los cuatro braseros (hogueras de almas encendidas) y la silla de Galen, de cara al alambique
        for (double a : new double[]{45, 135, 225, 315}) {
            double rad = Math.toRadians(a);
            int u = (int) Math.round(7.0 * Math.cos(rad)), v = (int) Math.round(7.0 * Math.sin(rad));
            put(u, y, v, Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        }
        put(0, y, -7, Mina.escalon(Blocks.DARK_OAK_STAIRS, Direction.NORTH));
        put(-1, y, -7, Mina.escalonAlto(Blocks.DARK_OAK_STAIRS, Direction.WEST));
        put(1, y, -7, Mina.escalonAlto(Blocks.DARK_OAK_STAIRS, Direction.EAST));
        // atriles y velas por el borde
        for (double a : new double[]{100, 260}) {
            double rad = Math.toRadians(a);
            int u = (int) Math.round(8.0 * Math.cos(rad)), v = (int) Math.round(8.0 * Math.sin(rad));
            enLibre(u, y, v, Blocks.LECTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LecternBlock.FACING, haciaDentro(u, v)));
        }
        for (double a = 10; a < 360; a += 40) {
            double rad = Math.toRadians(a);
            int u = (int) Math.round(8.6 * Math.cos(rad)), v = (int) Math.round(8.6 * Math.sin(rad));
            enLibre(u, y, v, vela(2 + (int) a % 3, false));
        }
    }

    // ------------------------------------------------------------------ la cupula de cristal verde y el tejado

    private void cupula() {
        int R = (int) Math.ceil(RI) + 1;
        for (int u = -R; u <= R; u++)
            for (int v = -R; v <= R; v++) {
                double d = r(u, v);
                if (d >= RI) continue;
                // el anillo del tejado entre la cupula y el muro (el adarve se pisa en TEJADO)
                if (d >= RD) {
                    put(u, TEJADO, v, piedra(x(u), TEJADO, z(v)));
                    continue;
                }
                for (int y = TEJADO; y <= TEJADO + (int) RD + 1; y++) {
                    double e = Math.sqrt(d * d + (y - TEJADO) * (y - TEJADO));
                    if (e < RD - 0.85) aire(u, y, v);
                    else if (e < RD + 0.15) {
                        double a = ang(u, v);
                        boolean costilla = false;
                        for (double c = 0; c < 360; c += 45) if (dif(a, c) * Math.PI / 180 * Math.max(1, d) < 0.6) costilla = true;
                        put(u, y, v, costilla || y == TEJADO ? cobre(x(u), y, z(v)) : cristal(x(u), y, z(v)));
                    }
                }
            }
        // el adarve: suelo sobre el muro (lo pone torre()) y faroles en cuatro merlones
        for (double a : new double[]{45, 135, 225, 315}) {
            double rad = Math.toRadians(a);
            int u = (int) Math.round((RT - 0.6) * Math.cos(rad)), v = (int) Math.round((RT - 0.6) * Math.sin(rad));
            put(u, TEJADO + 2, v, Mina.farol(true, false));
        }
    }

    // ------------------------------------------------------------------ el islote: el camino, el huerto, las tumbas y el embarcadero

    private void islote() {
        // el camino de la playa a la puerta
        for (int v = 12; v <= 21; v++)
            for (int u = -1; u <= 1; u++) {
                int h = superficie(u, v);
                put(u, h, v, pct(u, h, v, 91) < 60 ? b(Blocks.GRAVEL) : sillar(x(u), h, z(v)));
            }
        // faroles en postes a los lados del camino
        for (int v : new int[]{14, 19}) {
            for (int u : new int[]{-3, 3}) {
                int h = superficie(u, v);
                une(u, h + 1, v, b(Blocks.DEEPSLATE_BRICK_WALL));
                put(u, h + 2, v, Mina.farol(true, false));
            }
        }
        // el huerto de simples (al norte): bancales con matas secas y una cerca rota
        for (int u = -8; u <= 8; u++)
            for (int v = -20; v <= -13; v++) {
                int h = superficie(u, v);
                if (h != ISLA) continue;
                boolean cerca = Math.abs(u) == 8 || v == -20 || v == -13;
                if (cerca) {
                    if (pct(u, h, v, 92) < 70 && !(v == -13 && Math.abs(u) <= 1)) une(u, h + 1, v, b(Blocks.SPRUCE_FENCE));
                    continue;
                }
                if (v % 2 == 0) {
                    put(u, h, v, b(Blocks.COARSE_DIRT));
                    int p = pct(u, h, v, 93);
                    put(u, h + 1, v, p < 45 ? b(Blocks.DEAD_BUSH) : p < 65 ? b(Blocks.FERN) : Pal.AIR);
                }
            }
        // las tumbas de los enfermos (al sureste): montones con su piedra a la cabeza
        for (int i = 0; i < 12; i++) {
            int u = 7 + (i % 4) * 3, v = 13 + (i / 4) * 3;
            int h = superficie(u, v);
            if (h < ISLA - 1) continue;
            put(u, h, v, b(Blocks.COARSE_DIRT));
            put(u, h, v + 1, b(Blocks.ROOTED_DIRT));
            int p = pct(u, h, v, 94);
            if (p < 60) une(u, h + 1, v - 1, p < 30 ? b(Blocks.COBBLESTONE_WALL) : b(Blocks.MOSSY_COBBLESTONE_WALL));
            else put(u, h + 1, v - 1, p < 80 ? Blocks.SPRUCE_FENCE.defaultBlockState() : calavera(p % 16));
            tumbas++;
        }
        // tres arboles muertos
        arbol(-14, -10, 6, 3);
        arbol(16, -12, 5, 2);
        arbol(-12, 14, 7, 3);
        // el embarcadero roto (al oeste) y la barca hundida
        for (int u = -34; u <= -18; u++)
            for (int v = -1; v <= 1; v++) {
                if (superficie(u, v) > AGUA + 1 && !enLaguna(u, v)) continue;
                if (u < -29 && v != 0 && pct(u, 0, v, 95) < 40) continue;
                put(u, AGUA + 1, v, b(Blocks.SPRUCE_PLANKS));
                if (Math.abs(v) == 1 && u % 3 == 0) {
                    for (int y = fondo(u, v); y <= AGUA; y++) put(u, y, v, Mina.tronco(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.Y));
                    une(u, AGUA + 2, v, b(Blocks.SPRUCE_FENCE));
                }
            }
        for (int u = -38; u <= -35; u++)
            for (int v = 2; v <= 4; v++) {
                int f = fondo(u, v);
                boolean casco = u == -38 || u == -35 || v == 2 || v == 4;
                put(u, f + 1, v, casco ? b(Blocks.SPRUCE_PLANKS) : b(Blocks.SPRUCE_SLAB));
                if (casco && pct(u, f, v, 96) < 60) put(u, f + 2, v, Mina.losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
            }
    }

    private void arbol(int u, int v, int alto, int ramas) {
        int base = superficie(u, v);
        for (int k = 1; k <= alto; k++) put(u, base + k, v, Mina.tronco(Blocks.MANGROVE_LOG, Direction.Axis.Y));
        Direction[] lados = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (int k = 0; k < ramas; k++) {
            int p = pct(u, k, v, 47 + k);
            Direction d = lados[(p + k) % 4];
            int by = base + alto / 2 + 1 + p % Math.max(1, alto / 2);
            int largo = 1 + p % 3;
            for (int i = 1; i <= largo; i++) {
                int y = by + i / 2;
                if (y > base + alto + 1) break;
                put(u + d.getStepX() * i, y, v + d.getStepZ() * i, Mina.tronco(Blocks.MANGROVE_LOG, d.getAxis()));
            }
        }
        arboles++;
    }

    // ------------------------------------------------------------------ la calzada (de la orilla del sur al islote)

    private void calzada() {
        int v0 = 17, v1 = (int) R_LAGUNA + 6;
        for (int v = v0; v <= v1; v++)
            for (int u = -2; u <= 2; u++) {
                int h = superficie(u, v);
                boolean borde = Math.abs(u) == 2;
                // de la playa del islote para arriba se sube por la playa; en el agua la calzada va un bloque por encima
                int top = Math.max(AGUA + 1, Math.min(h, AGUA + 1));
                if (h > AGUA + 1) top = h;
                boolean agua = enLaguna(u, v);
                if (agua) {
                    if (borde && v % 5 == 0) for (int y = fondo(u, v); y < AGUA + 1; y++) put(u, y, v, sillar(x(u), y, z(v)));
                    // algun trozo del borde se ha caido al agua
                    if (borde && pct(u, 0, v, 97) < 12) continue;
                }
                put(u, top, v, sillar(x(u), top, z(v)));
                for (int y = top + 1; y <= top + 3; y++) aire(u, y, v);
                if (borde && agua) {
                    int p = pct(u, 1, v, 98);
                    if (v % 8 == 0) {
                        une(u, top + 1, v, b(Blocks.STONE_BRICK_WALL));
                        put(u, top + 2, v, Mina.farol(true, false));
                    } else if (p < 55) une(u, top + 1, v, p < 30 ? b(Blocks.MOSSY_STONE_BRICK_WALL) : b(Blocks.STONE_BRICK_WALL));
                }
            }
    }

    // ------------------------------------------------------------------ lo de dentro con bloque-entidad (atril, arcones)

    private void trozos() {
        trozo(ATRIL_U, piso(3) + 1, ATRIL_V, c -> c.lectern(x(ATRIL_U), piso(3) + 1, z(ATRIL_V), Direction.EAST, Perdidos.libro(LIBRO)));
        trozo(-6, PS + 1, -6, c -> c.chest(x(-6), PS + 1, z(-6), Direction.EAST, BOTIN));
        trozo(-7, piso(2) + 1, 3, c -> c.chest(x(-7), piso(2) + 1, z(3), Direction.EAST, BOTIN));
        trozo(6, piso(4) + 1, 4, c -> c.barrel(x(6), piso(4) + 1, z(4), Direction.UP, BOTIN));
        trozo(-15, piso(3) + 1, 0, c -> c.chest(x(-15), piso(3) + 1, z(0), Direction.EAST, BOTIN_BUENO));
    }

    private void trozo(int u, int y, int v, Trozo.Obra o) {
        elementos.add(new Trozo(6, x(u), y, z(v), x(u), y, z(v), o));
    }

    // ------------------------------------------------------------------ los sitios y los de alli

    private void ponerSitios() {
        sitio("entrada", 0, AGUA + 2, (int) R_LAGUNA + 4);
        sitio("calzada", 0, AGUA + 2, 34);
        sitio("isla", 0, ISLA + 1, 17);
        sitio("puerta", 0, ISLA + 1, 13);
        sitio("vestibulo", 0, piso(0) + 1, 6);
        sitio("escalera", 9, piso(0) + 1, 0);
        sitio("atanor", 0, PS + 1, -6);
        sitio("parrilla", 0, PS + 1, 0);
        sitio("palanca", PALANCA_U, PS + 1, PALANCA_V - 1);
        sitio("enfermeria", 0, piso(1) + 1, 6);
        sitio("herbolario", 0, piso(2) + 1, 6);
        sitio("biblioteca", 0, piso(3) + 1, 6);
        sitio("atril", ATRIL_U + 1, piso(3) + 1, ATRIL_V);
        sitio("celda", -13, piso(3) + 1, 0);
        sitio("laboratorio", 0, piso(4) + 1, 6);
        sitio("elixir", 0, P5 + 1, 6);
        sitio("tejado", 0, TEJADO + 1, 9);
        sitio("huerto", 0, ISLA + 1, -16);
        sitio("tumbas", 10, ISLA + 1, 17);
        sitio("embarcadero", -24, AGUA + 2, 0);
    }

    private static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;

    private void ponerEsperas() {
        int M = Destilado.MANCEBO, E = Destilado.ENFERMO, C = Destilado.CUBA;
        int R = Enemigo.REZA, D = Enemigo.DE_PIE, T = Enemigo.TUMBADO;
        // ---- el islote: la cuba de la puerta, uno en el huerto, dos en las tumbas y el mancebo del embarcadero
        en(C, 0, ISLA + 1, 15, SUR, D, 0, "islote");
        en(E, -3, ISLA + 1, -17, SUR, D, 4, "islote");
        en(E, 10, ISLA + 1, 15, SUR, T, 0, "islote");
        en(E, 8, ISLA + 1, 15, NORTE, R, 0, "islote");
        en(M, -22, AGUA + 2, 0, OESTE, D, 0, "islote");
        // ---- el vestibulo: los que esperan turno, uno tumbado junto a una camilla y el mancebo tras el mostrador
        en(E, 6, piso(0) + 1, -2, OESTE, R, 0, "vestibulo");
        en(E, 6, piso(0) + 1, 3, OESTE, R, 0, "vestibulo");
        en(E, 5, piso(0) + 1, -3, SUR, T, 0, "vestibulo");
        en(M, 0, piso(0) + 1, -6, SUR, D, 0, "vestibulo");
        // ---- el sotano: la cuba que guarda la palanca, un mancebo junto al carbon y dos enfermos
        en(C, 3, PS + 1, -7, NORTE, D, 0, "sotano");
        en(M, 4, PS + 1, 5, OESTE, D, 3, "sotano");
        en(E, -4, PS + 1, -6, ESTE, R, 0, "sotano");
        en(E, -4, PS + 1, 4, ESTE, T, 0, "sotano");
        // ---- la sala de los enfermos: tumbados junto a las camillas, el mancebo que los atiende y una cuba al oeste
        en(E, 4, piso(1) + 1, 1, OESTE, T, 0, "enfermeria");
        en(E, -2, piso(1) + 1, 5, NORTE, T, 0, "enfermeria");
        en(E, -5, piso(1) + 1, -1, ESTE, T, 0, "enfermeria");
        en(E, 2, piso(1) + 1, -5, SUR, R, 0, "enfermeria");
        en(M, 0, piso(1) + 1, 6, NORTE, D, 4, "enfermeria");
        en(C, -5, piso(1) + 1, 0, ESTE, D, 0, "enfermeria");
        // ---- el herbolario
        en(M, 4, piso(2) + 1, 4, NORTE, D, 3, "herbolario");
        en(M, -4, piso(2) + 1, -4, SUR, D, 0, "herbolario");
        en(E, -5, piso(2) + 1, 2, ESTE, R, 0, "herbolario");
        // ---- la biblioteca: dos leyendo y un enfermo
        en(M, 4, piso(3) + 1, 2, NORTE, R, 0, "biblioteca");
        en(M, -4, piso(3) + 1, -3, ESTE, R, 0, "biblioteca");
        en(E, 3, piso(3) + 1, -3, OESTE, D, 3, "biblioteca");
        // ---- el laboratorio: dos mancebos a sus mesas, la cuba y el de la mesa de disecciones
        en(M, -3, piso(4) + 1, 2, OESTE, R, 0, "laboratorio");
        en(M, 3, piso(4) + 1, 3, ESTE, D, 3, "laboratorio");
        en(C, -4, piso(4) + 1, -1, ESTE, D, 0, "laboratorio");
        en(E, 4, piso(4) + 1, -4, NORTE, T, 0, "laboratorio");
        // ---- el tejado: un mancebo en el adarve
        en(M, -9, TEJADO + 1, 3, ESTE, D, 4, "tejado");
    }

    private void en(int variante, int u, int y, int v, float giro, int postura, int ronda, String grupo) {
        esperas.add(new Espera(variante, x(u), y, z(v), giro, postura, ronda, grupo));
    }

    // ------------------------------------------------------------------ el atanor y su tiro

    /** Para las pruebas: las veces que se ha encendido el atanor y los que ha subido el tiro. */
    public static int ENCENDIDOS, SUBEN;

    public static BlockPos palanca() {
        return new BlockPos(x(PALANCA_U), PS + 2, z(PALANCA_V));
    }

    /** Si el atanor esta encendido (las hogueras de la parrilla lo dicen: se queda asi para siempre). */
    public static boolean encendido(ServerLevel m) {
        BlockPos c = new BlockPos(x(0), PS - 1, z(0));
        if (!m.isLoaded(c)) return false;
        var s = m.getBlockState(c);
        return s.is(Blocks.CAMPFIRE) && s.getValue(CampfireBlock.LIT);
    }

    /** Enciende (o apaga, para el comando y las pruebas) el atanor: las hogueras, el fuego de la corona y la palanca. */
    public static void encender(ServerLevel m, boolean si) {
        for (BlockPos p : hogueras()) {
            var s = m.getBlockState(p);
            if (s.is(Blocks.CAMPFIRE)) m.setBlock(p, s.setValue(CampfireBlock.LIT, si), 3);
        }
        for (BlockPos f : corona()) {
            if (si && m.getBlockState(f.below()).is(Blocks.NETHERRACK) && m.getBlockState(f).isAir()) m.setBlock(f, b(Blocks.FIRE), 3);
            if (!si && m.getBlockState(f).is(Blocks.FIRE)) m.setBlock(f, Pal.AIR, 3);
        }
        var pa = m.getBlockState(palanca());
        if (pa.is(Blocks.LEVER) && pa.getValue(LeverBlock.POWERED) != si) {
            m.setBlock(palanca(), pa.setValue(LeverBlock.POWERED, si), 3);
            m.updateNeighborsAt(palanca(), Blocks.LEVER);
        }
        if (si) {
            ENCENDIDOS++;
            // a los de alrededor: el aire caliente sube por el pozo
            for (ServerPlayer p : m.players())
                if (p.distanceToSqr(x(0) + 0.5, PS + 2, z(0) + 0.5) < 26 * 26)
                    com.darkcraft.net.Net.to(p, new com.darkcraft.net.Net.Cartel(com.darkcraft.net.Net.Cartel.AVISO, "darkcraft.lazareto.atanor", 0));
            m.playSound(null, x(0) + 0.5, PS + 2, z(0) + 0.5, DarkCraftMod.SFX_ATANOR_ENCIENDE.get(), SoundSource.BLOCKS, 3.0f, 1.0f);
            m.sendParticles(ParticleTypes.FLAME, x(0) + 0.5, PS + 2, z(0) + 0.5, 60, 1.6, 0.6, 1.6, 0.05);
            m.sendParticles(ParticleTypes.LARGE_SMOKE, x(0) + 0.5, PS + 6, z(0) + 0.5, 30, 1.2, 1.0, 1.2, 0.03);
            tiroSuena = 0;
        }
    }

    /** Si (x, y, z) cae en el pozo del tiro (de la parrilla al pie del gran alambique). */
    public static boolean enTiro(double x, double y, double z) {
        return Math.hypot(x - CX - 0.5, z - CZ - 0.5) < RP - 0.1 && y >= PS && y < P5;
    }

    private static final Set<UUID> SUBIENDO = new HashSet<>();
    /** A quien ya se le dijo que la escalera esta hundida (una vez por sesion). */
    private static final Set<UUID> AVISADOS = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static int tiroSuena;

    /**
     * Cada tick con alguien cerca (lo llama {@link Morvain}): la palanca del sotano enciende el atanor; con el atanor encendido, quien
     * esta en el pozo sube (levitacion; agachado, baja despacio) y al salir del pozo cae despacio un momento. Humo y el rugido del tiro.
     */
    public static void tick(ServerLevel m) {
        BlockPos pa = palanca();
        if (!m.isLoaded(pa)) return;
        boolean lit = encendido(m);
        if (!lit && m.getGameTime() % 10 == 0) lit = revisarPalanca(m);
        // la escalera hundida: a quien llega al ultimo escalon que queda (con el atanor apagado), una vez por sesion
        if (!lit && m.getGameTime() % 10 == 3)
            for (ServerPlayer p : m.players()) {
                int u = (int) Math.floor(p.getX()) - CX, v = (int) Math.floor(p.getZ()) - CZ;
                if (!p.isSpectator() && enTorreta(u, v) && p.getY() > piso(1) + 1.5 && p.getY() < piso(1) + 4 && AVISADOS.add(p.getUUID()))
                    com.darkcraft.net.Net.to(p, new com.darkcraft.net.Net.Cartel(com.darkcraft.net.Net.Cartel.AVISO, "darkcraft.lazareto.hundida", 0));
            }
        for (ServerPlayer p : m.players()) tiro(p, lit);
        if (!lit) return;
        // el humo y las pavesas que suben por el pozo, y el rugido del tiro (cada 28 s)
        boolean cerca = false;
        for (ServerPlayer p : m.players()) if (distancia(p.getX(), p.getZ()) < 48) cerca = true;
        if (!cerca) return;
        if (m.getGameTime() % 4 == 0)
            for (int k = 0; k < 3; k++) {
                double y = PS + 1 + m.random.nextDouble() * (P5 - PS - 2);
                m.sendParticles(m.random.nextInt(3) == 0 ? ParticleTypes.ASH : ParticleTypes.SMOKE, x(0) + 0.5 + (m.random.nextDouble() - 0.5) * 3,
                        y, z(0) + 0.5 + (m.random.nextDouble() - 0.5) * 3, 0, 0, 1, 0, 0.12);
            }
        if (tiroSuena-- <= 0) {
            tiroSuena = 28 * 20;
            m.playSound(null, x(0) + 0.5, piso(2), z(0) + 0.5, DarkCraftMod.SFX_ATANOR_TIRO.get(), SoundSource.BLOCKS, 2.4f, 1.0f);
        }
    }

    /**
     * La palanca del sotano: si esta bajada y el atanor apagado, lo enciende (y el logro a los de alrededor). Devuelve si esta encendido.
     * (Lo mira {@link #tick} cada medio segundo; las pruebas, cuando quieren.)
     */
    public static boolean revisarPalanca(ServerLevel m) {
        if (encendido(m)) return true;
        BlockPos pa = palanca();
        var s = m.getBlockState(pa);
        if (!s.is(Blocks.LEVER) || !s.getValue(LeverBlock.POWERED)) return false;
        encender(m, true);
        for (ServerPlayer p : m.players())
            if (p.distanceToSqr(pa.getX(), pa.getY(), pa.getZ()) < 24 * 24) com.darkcraft.story.Logros.dar(p, "atanor");
        return true;
    }

    /** El tiro del pozo para un jugador: con el atanor encendido, en el pozo sube (agachado, baja despacio); al salir, cae despacio. */
    public static void tiro(ServerPlayer p, boolean lit) {
        boolean en = lit && !p.isSpectator() && enTiro(p.getX(), p.getY(), p.getZ());
        if (en) {
            p.fallDistance = 0;
            if (p.isShiftKeyDown()) {
                p.removeEffect(MobEffects.LEVITATION);
                p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 12, 0, false, false, false));
            } else {
                var l = p.getEffect(MobEffects.LEVITATION);
                // nivel VI: sube unos seis bloques por segundo (del sotano al laboratorio, unos diez segundos)
                if (l == null || l.getDuration() < 6) p.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 14, 5, false, false, false));
            }
            if (SUBIENDO.add(p.getUUID())) SUBEN++;
        } else if (SUBIENDO.remove(p.getUUID())) {
            p.removeEffect(MobEffects.LEVITATION);
            p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30, 0, false, false, false));
        }
    }

    // ------------------------------------------------------------------ lo que hay, donde se esta

    /** Lo que hay en el lazareto ya generado (para la prueba): el atanor, la palanca, el atril, los arcones, el muro... */
    public static String resumen(ServerLevel m) {
        boolean libro = m.getBlockEntity(new BlockPos(x(ATRIL_U), piso(3) + 1, z(ATRIL_V))) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le
                && Perdidos.de(le.getBook()) == LIBRO;
        var pa = m.getBlockState(palanca());
        int hog = 0, encendidas = 0;
        for (BlockPos p : hogueras()) {
            var s = m.getBlockState(p);
            if (s.is(Blocks.CAMPFIRE)) {
                hog++;
                if (s.getValue(CampfireBlock.LIT)) encendidas++;
            }
        }
        int muro = 0;
        for (BlockPos q : muro(MURO_CELDA)) if (!m.getBlockState(q).isAir()) muro++;
        boolean bueno = m.getBlockState(new BlockPos(x(-15), piso(3) + 1, z(0))).is(Blocks.CHEST);
        int escalones = 0;
        for (int n = 0; n <= 6; n++) if (m.getBlockState(new BlockPos(x(TX - 1), piso(n), z(0))).is(Blocks.POLISHED_DEEPSLATE)) escalones++;
        int cancelas = 0;
        for (int k = 1; k <= 4; k++) if (m.getBlockState(new BlockPos(x(0), piso(k) + 1, z(-3))).is(Blocks.OAK_FENCE_GATE)) cancelas++;
        return "atanor " + (encendido(m) ? "encendido" : "apagado") + " (hogueras " + encendidas + "/" + hog + "), palanca " + (pa.is(Blocks.LEVER) ? "SI" : "NO")
                + ", atril con el libro " + LIBRO + " " + (libro ? "SI" : "NO") + ", arcon bueno " + (bueno ? "SI" : "NO") + ", muro de la celda " + muro + "/"
                + muro(MURO_CELDA).size() + ", descansillos " + escalones + "/7, cancelas del pozo " + cancelas + "/4, agua de la laguna "
                + (m.getBlockState(new BlockPos(x(0), AGUA, z(30))).is(Blocks.WATER) ? "SI" : "NO");
    }

    /** El nombre del sitio del lazareto donde esta (x, y, z) (null: fuera). */
    public static String lugar(double x, double y, double z) {
        int u = (int) Math.floor(x) - CX, v = (int) Math.floor(z) - CZ;
        double d = r(u, v);
        boolean torre = d < RT + 0.5 || enTorreta(u, v) || u <= -11 && u >= -16 && Math.abs(v) <= 3 && y > piso(3) - 1 && y < piso(3) + 5;
        if (torre && y >= PS && y < TEJADO + 1) {
            if (y < piso(0)) return "darkcraft.morvain.lugar.lazareto_atanor";
            int k = (int) Math.floor((y - piso(0)) / ALTO);
            return switch (k) {
                case 0 -> "darkcraft.morvain.lugar.lazareto_vestibulo";
                case 1 -> "darkcraft.morvain.lugar.lazareto_enfermeria";
                case 2 -> "darkcraft.morvain.lugar.lazareto_herbolario";
                case 3 -> "darkcraft.morvain.lugar.lazareto_biblioteca";
                case 4 -> "darkcraft.morvain.lugar.lazareto_laboratorio";
                default -> "darkcraft.morvain.lugar.lazareto_elixir";
            };
        }
        if (borde(u, v) < R_LAGUNA + 4 && y > AGUA - 4 && y < TEJADO + 12) return "darkcraft.morvain.lugar.lazareto";
        return null;
    }

    /** Si (x, y, z) esta dentro de la torre (para el aire de la torre y el logro). */
    public static boolean dentro(double x, double y, double z) {
        int u = (int) Math.floor(x) - CX, v = (int) Math.floor(z) - CZ;
        return (r(u, v) < RI + 0.3 || enTorreta(u, v)) && y >= PS && y < TEJADO + 9;
    }
}
