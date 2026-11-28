package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Puestos;
import com.darkcraft.fortaleza.Perdidos;
import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Pal;
import com.darkcraft.gen.Rect;
import com.darkcraft.pueblo.gen.Casa;
import com.darkcraft.pueblo.gen.Trozo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Las Minas de Hierro Negro (parte 7aq; respuesta 131 del plan: "minas y tuneles"): al pie de las Montanas Nevadas, mirando a
 * la Tierra sin Fe. De su hierro negro se forjaron las espadas del Juramento y las cadenas que sujetan la ciudadela; la noche
 * del cielo rojo el turno de noche estaba abajo y no subio nadie: los Enterrados siguen picando. u = x - {@link #CX},
 * v = z - {@link #CZ}; el norte (v negativa) es la montana.
 * <ul>
 * <li>El <b>campamento</b> (la explanada, suelo {@link #SUELO}): el santuario de la Bocamina al sur, la oficina del capataz (el atril
 * con el <b>libro perdido 36</b>, el libro de turnos), la
 * lampisteria (la pared de los ganchos de los faroles: casi todos vacios), la fragua, el barracon hundido y la escombrera.</li>
 * <li><b>La Corta</b>: el tajo abierto, tres escalones de seis bloques con vetas de hierro negro en las paredes, hasta el fondo
 * ({@link #FONDO}); se baja por la escalera de tablones (en zigzag) del suroeste. En el fondo, al norte, la <b>bocamina</b>.</li>
 * <li><b>Nivel 1</b> ({@link #N1}): la Galeria del Rey (entibada, con railes), el polvorin, el comedor, el frente del este, el
 * enganche del pozo maestro (tras unos barrotes) y la rampa que baja al nivel 2; la galeria del rey, hundida al fondo.</li>
 * <li><b>Nivel 2</b> ({@link #N2}): la Gruta del Agua Negra (el lago con su pasarela de tablones y un islote), la galeria del
 * este, las literas, la capilla de los mineros (detras del altar, un <b>muro
 * ilusorio</b> y el arcon bueno) y el enganche de abajo del pozo maestro: su escala sube hasta la boca, bajo el castillete,
 * y la <b>escotilla</b> de arriba solo se abre desde dentro (la palanca de debajo): el atajo de vuelta al campamento.</li>
 * <li><b>Nivel 3</b> ({@link #N3}): por el pozo de la veta, la Caverna del Filon (la veta roja).</li>
 * </ul>
 * Se dibuja chunk a chunk al adornar; en su caja {@link Adorno} no pone nada.
 */
public final class Mina {
    public static final long SEMILLA = 0x3F1A_A0C7_B1E5L;
    /** El centro del campamento (el borde sur de la corta queda a v = -8). */
    public static final int CX = 600, CZ = -3440;
    /** La explanada del campamento (lo que se pisa esta en {@link #PISO}). */
    public static final int SUELO = 189, PISO = SUELO + 1;
    /** La corta: su centro (v) y sus tres contornos (medio ancho en u, medio largo en v, radio de las esquinas). */
    static final int VC = -30;
    static final double[][] CORTA = {{28, 22, 11}, {24, 18, 9}, {20, 14, 7}};
    /** Los dos escalones de la corta y su fondo (bloques: lo que se pisa esta encima). */
    public static final int BANCO1 = SUELO - 6, BANCO2 = SUELO - 12, FONDO = SUELO - 18;
    /** Los suelos de los tres niveles de la mina. */
    public static final int N1 = FONDO, N2 = N1 - 20, N3 = N2 - 20;
    /** El pozo maestro: su hueco (u y v) y la escala (contra la pared del norte, mirando al sur). */
    static final int POZO_U0 = 18, POZO_U1 = 20, POZO_V0 = -86, POZO_V1 = -84, ESCALA_U = 19, ESCALA_V = -86;
    /** El lago de la Gruta del Agua Negra: su centro (v), sus radios y lo alto del agua. */
    static final int LAGO_V = -150, AGUA = N2 - 1;
    static final double LAGO_A = 21, LAGO_B = 14;
    /** La Caverna del Filon: su centro (v) y sus radios. */
    public static final int FILON_V = -194;
    static final double FILON_A = 20, FILON_B = 16;
    /** El santuario de la Bocamina (en {@link SantuariosMorvain}): al sur del campamento. */
    public static final int SANT_U = 0, SANT_V = 40;
    public static final int X0 = CX - 54, X1 = CX + 58, Z0 = CZ - 216, Z1 = CZ + 56;

    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/mina"),
            BOTIN_BUENO = new ResourceLocation(DarkCraftMod.MODID, "chests/mina_bueno");
    /** El muro ilusorio de detras del altar de la capilla de los mineros. */
    public static final String MURO_CAPILLA = "mina_capilla";
    /** El libro perdido que esta en el atril de las literas. */
    public static final int LIBRO = 36;

    /** Uno de la mina: donde espera (y: donde tiene los pies), hacia donde mira, su variante, como espera, cuanto ronda y su grupo. */
    public record Espera(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    private static Mina unico;

    public static synchronized Mina get() {
        if (unico == null) unico = new Mina();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    private final List<Tramo> tramos = new ArrayList<>();
    /** Los techos rotos que se vienen abajo (x, y del techo, z). */
    private final List<int[]> derrumbes = new ArrayList<>();
    private final Plano plano = new Plano();
    /** Lo que hay (para la prueba y las vistas). */
    public int marcos, faroles, rieles, vagonetas, ganchos, literas, estalactitas;

    private Mina() {
        elementos.add(new Obra());
        nivel1();
        nivel2();
        nivel3();
        pozo();
        for (Tramo t : tramos) cavar(t);
        lago();
        filon();
        salas();
        plano.forrar(Mina::pared);
        for (Tramo t : tramos) vestir(t);
        escalera();
        interiores();
        techosRotos();
        bocamina();
        castillete();
        campamento();
        adornos();
        elementos.add(plano.pieza(4, X0, Z0, X1, Z1));
        trozos();
        ponerSitios();
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo de la mina (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en lo de la mina: ahi {@link Adorno} no pone nada. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= X0 && x0 <= X1 && z0 + 15 >= Z0 && z0 <= Z1;
    }

    /** Lo lejos de (x, z) al centro de la corta. */
    public static double distancia(double x, double z) {
        return Math.hypot(x - CX - 0.5, z - CZ - VC - 0.5);
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} de la mina en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los de la mina (los apunta {@link MorvainEnemigos}). */
    public List<Espera> esperas() {
        return esperas;
    }

    /** Los sitios con nombre (la entrada, la bocamina, el lago, el filon...): {x, y, z} (y: los pies). */
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

    static BlockState tronco(Block k, Direction.Axis a) {
        return k.defaultBlockState().setValue(RotatedPillarBlock.AXIS, a);
    }

    static BlockState escalon(Block k, Direction f) {
        return k.defaultBlockState().setValue(StairBlock.FACING, f).setValue(StairBlock.HALF, Half.BOTTOM);
    }

    static BlockState escalonAlto(Block k, Direction f) {
        return k.defaultBlockState().setValue(StairBlock.FACING, f).setValue(StairBlock.HALF, Half.TOP);
    }

    static BlockState losa(Block k, SlabType t) {
        return k.defaultBlockState().setValue(SlabBlock.TYPE, t);
    }

    static BlockState farol(boolean almas, boolean colgado) {
        return (almas ? Blocks.SOUL_LANTERN : Blocks.LANTERN).defaultBlockState().setValue(LanternBlock.HANGING, colgado);
    }

    static BlockState escala(Direction f) {
        return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, f);
    }

    static BlockState cadena(Direction.Axis a) {
        return Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS, a);
    }

    static int pct(int u, int y, int v, long sem) {
        return Hash.pct(x(u), y, z(v), SEMILLA + sem);
    }

    /** Un ruido suave y fijo (para que los bordes no salgan rectos). */
    static double ruido(double u, double v) {
        return 0.6 * Math.sin(u * 0.37 + v * 0.11 + 1.3) + 0.4 * Math.sin(v * 0.43 - u * 0.19 + 0.4);
    }

    /** La distancia (con signo: negativa dentro) a una caja de esquinas redondas centrada en (0, 0). */
    static double caja(double u, double v, double hu, double hv, double r) {
        double qx = Math.abs(u) - (hu - r), qz = Math.abs(v) - (hv - r);
        return Math.hypot(Math.max(qx, 0), Math.max(qz, 0)) + Math.min(Math.max(qx, qz), 0) - r;
    }

    // ------------------------------------------------------------------ la corta y la explanada

    /** En cuantos contornos de la corta cae (u, v): 0 fuera, 1 el primer escalon, 2 el segundo, 3 el fondo. */
    static int nivelCorta(int u, int v) {
        double n = ruido(u, v) * 0.7 * (1 - calma(u, v));
        int k = 0;
        for (double[] c : CORTA) if (caja(u, v - VC, c[0], c[1], c[2]) + n < 0) k++;
        return k;
    }

    /** Junto a la escalera de tablones las paredes del sur de la corta van rectas (1 dentro, 0 lejos): asi encajan sus tramos. */
    static double calma(int u, int v) {
        double d = caja(u + 11, v + 13, 10, 8, 2);
        return 1 - Ruido.suave(0, 4, d);
    }

    /** Lo alto (el bloque de arriba) de la corta en el escalon k. */
    static int altoCorta(int k) {
        return SUELO - 6 * k;
    }

    /** Cuanto se allana (u, v) a la altura del campamento: 1 en la explanada, 0 lejos. */
    static double peso(int u, int v) {
        double d = caja(u, v - VC, CORTA[0][0] + 7, CORTA[0][1] + 7, 14);
        d = Math.min(d, caja(u, v - 22, 38, 26, 10));
        d = Math.min(d, caja(u - 30, v + 58, 8, 34, 4));
        d = Math.min(d, caja(u - 18, v + 85, 16, 13, 5));
        d += ruido(u * 1.7, v * 1.7) * 1.2;
        return d <= 0 ? 1.0 : 1.0 - Ruido.suave(0, 9, d);
    }

    /** La altura natural del terreno (la de antes de la mina). */
    static int alto(int x, int z) {
        return Cerco.alto(x, z);
    }

    private final class Obra extends Element {
        Obra() {
            super(SEMILLA + 51);
            this.layer = 1;
            bounds(X0, N3 - 6, Z0, X1, 230, Z1);
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
        int t = alto(x, z);
        int k = nivelCorta(u, v);
        if (k > 0) {
            corta(c, x, z, u, v, k, t);
            return;
        }
        double w = peso(u, v);
        if (w <= 0) return;
        int h = (int) Math.round(Ruido.lerp(w, t, SUELO));
        for (int y = h + 1; y <= Math.max(t, h) + 2; y++) c.set(x, y, z, Pal.AIR);
        for (int y = t + 1; y < h; y++) c.set(x, y, z, Hash.pct(x, y, z, SEMILLA + 3) < 70 ? b(Blocks.STONE) : b(Blocks.ANDESITE));
        BlockState top = manchas(x, h, z, w > 0.7);
        c.set(x, h, z, top);
        int p = Hash.pct(x, h, z, SEMILLA + 4);
        if (top.is(Blocks.SNOW_BLOCK) || (top.is(Blocks.STONE) && p % 4 == 0)) c.set(x, h + 1, z, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1));
        // la escombrera: el monton de ganga al este de la corta
        int m = escombrera(u, v);
        for (int dk = 1; dk <= m; dk++) {
            int q = Hash.pct(x, h + dk, z, SEMILLA + 13);
            c.set(x, h + dk, z, q < 34 ? b(Blocks.GRAVEL) : q < 56 ? b(Blocks.TUFF) : q < 74 ? b(Blocks.COBBLED_DEEPSLATE) : q < 86 ? b(Blocks.COBBLESTONE)
                    : q < 94 ? b(Blocks.BLACKSTONE) : b(Blocks.COAL_ORE));
        }
        if (m > 0 && p % 3 == 0) c.set(x, h + m + 1, z, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1));
        // la pared de arriba de la corta (del borde al primer escalon), con sus vetas
        if (w >= 1.0 && h == SUELO && nivelCorta(u + 1, v) + nivelCorta(u - 1, v) + nivelCorta(u, v + 1) + nivelCorta(u, v - 1) > 0)
            for (int y = h - 1; y > BANCO1; y--) c.set(x, y, z, roca(x, y, z));
    }

    /**
     * El suelo de arriba a manchas (no bloque a bloque): en el campamento tierra pisada, barro, grava y nieve sucia; fuera, piedra,
     * grava y nieve.
     */
    static BlockState manchas(int x, int y, int z, boolean campamento) {
        double n = ruido(x * 0.31, z * 0.29) + 0.35 * ruido(x * 0.9 + 7, z * 0.8 - 3) + (Hash.f(x, y, z, SEMILLA + 4) - 0.5) * 0.5;
        if (campamento) return n < -0.55 ? b(Blocks.PACKED_MUD) : n < -0.05 ? b(Blocks.COARSE_DIRT) : n < 0.35 ? b(Blocks.GRAVEL)
                : n < 0.62 ? b(Blocks.COARSE_DIRT) : b(Blocks.SNOW_BLOCK);
        return n < -0.3 ? b(Blocks.STONE) : n < 0.2 ? b(Blocks.GRAVEL) : n < 0.5 ? b(Blocks.STONE) : b(Blocks.SNOW_BLOCK);
    }

    /** Lo alto de la escombrera (bloques por encima del suelo) en (u, v): un monton al este de la corta. */
    static int escombrera(int u, int v) {
        double r = Math.hypot((u - 44) / 1.0, (v + 24) / 1.35);
        if (r >= 13) return 0;
        return (int) Math.round(8 * Math.pow(1 - (r / 13) * (r / 13), 0.9) + ruido(u * 0.7, v * 0.7));
    }

    /** Una columna dentro de la corta: lo de encima fuera, el escalon (o el fondo) y su pared con las vetas. */
    static void corta(Canvas c, int x, int z, int u, int v, int k, int t) {
        int y = altoCorta(k);
        for (int yy = y + 1; yy <= Math.max(t, SUELO) + 2; yy++) c.set(x, yy, z, Pal.AIR);
        int p = Hash.pct(x, y, z, SEMILLA + 5);
        double n = ruido(x * 0.37, z * 0.33) + (Hash.f(x, y, z, SEMILLA + 5) - 0.5) * 0.6;
        BlockState s = n < -0.5 ? b(Blocks.COBBLED_DEEPSLATE) : n < -0.1 ? b(Blocks.GRAVEL) : n < 0.3 ? b(Blocks.COARSE_DIRT) : n < 0.6 ? b(Blocks.TUFF)
                : b(Blocks.COBBLESTONE);
        c.set(x, y, z, s);
        // la pared que baja al escalon siguiente: con sus vetas (lo que se ve desde abajo)
        for (int yy = y - 1; yy >= y - 7 && yy > FONDO - 3; yy--) c.set(x, yy, z, roca(x, yy, z));
        // nieve en los escalones de arriba (al fondo no llega)
        if (k < 3 && p % 5 == 0) c.set(x, y + 1, z, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1));
    }

    /** La roca de las paredes de la corta: gris, con las vetas negras del hierro (bandas casi horizontales). */
    static BlockState roca(int x, int y, int z) {
        int banda = Math.floorMod(y + (int) Math.round(2.2 * Math.sin(x * 0.05 + z * 0.031)), 9);
        int p = Hash.pct(x, y, z, SEMILLA + 6);
        if (banda == 0 || banda == 1) return p < 40 ? b(Blocks.BLACKSTONE) : p < 62 ? b(Blocks.DEEPSLATE_IRON_ORE) : p < 80 ? b(Blocks.DEEPSLATE)
                : p < 90 ? b(Blocks.COAL_ORE) : b(Blocks.SMOOTH_BASALT);
        if (banda == 5) return p < 50 ? b(Blocks.TUFF) : p < 80 ? b(Blocks.ANDESITE) : b(Blocks.STONE);
        return p < 52 ? b(Blocks.STONE) : p < 70 ? b(Blocks.ANDESITE) : p < 82 ? b(Blocks.TUFF) : p < 92 ? b(Blocks.COBBLESTONE) : b(Blocks.IRON_ORE);
    }

    /** La roca de las paredes de las galerias (mas oscura cuanto mas hondo). */
    static BlockState pared(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 7);
        // la veta roja de la Caverna del Filon: una banda que cruza la pared del norte en diagonal
        int fu = x - CX, fv = z - CZ;
        if (y <= N3 + 24 && fv < FILON_V + 4 && Math.hypot(fu / FILON_A, (fv - FILON_V) / FILON_B) < 1.25) {
            double banda = Math.abs((y - N3) - 4 - fu * 0.35 - (fv - FILON_V) * 0.25);
            if (banda < 1.6) return p < 34 ? b(Blocks.DEEPSLATE_REDSTONE_ORE) : p < 58 ? b(Blocks.MAGMA_BLOCK) : p < 78 ? b(Blocks.BLACKSTONE)
                    : p < 90 ? b(Blocks.CRYING_OBSIDIAN) : b(Blocks.NETHERITE_BLOCK);
            if (banda < 3.2) return p < 45 ? b(Blocks.BLACKSTONE) : p < 75 ? b(Blocks.DEEPSLATE_IRON_ORE) : b(Blocks.SMOOTH_BASALT);
        }
        double veta = Math.sin(x * 0.11 + z * 0.07 + y * 0.35);
        if (veta > 0.93) return p < 45 ? b(Blocks.DEEPSLATE_IRON_ORE) : p < 75 ? b(Blocks.BLACKSTONE) : b(Blocks.SMOOTH_BASALT);
        if (y < N2 + 8) return p < 32 ? b(Blocks.DEEPSLATE) : p < 52 ? b(Blocks.TUFF) : p < 66 ? b(Blocks.STONE) : p < 78 ? b(Blocks.COBBLED_DEEPSLATE)
                : p < 87 ? b(Blocks.ANDESITE) : p < 94 ? b(Blocks.BLACKSTONE) : b(Blocks.DEEPSLATE_COAL_ORE);
        return p < 44 ? b(Blocks.STONE) : p < 60 ? b(Blocks.ANDESITE) : p < 72 ? b(Blocks.TUFF) : p < 82 ? b(Blocks.COBBLESTONE)
                : p < 89 ? b(Blocks.DEEPSLATE) : p < 95 ? b(Blocks.COAL_ORE) : b(Blocks.IRON_ORE);
    }

    /** El suelo de las galerias: tierra pisada, grava y cascotes. */
    static BlockState sueloGaleria(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 8);
        return p < 30 ? b(Blocks.COARSE_DIRT) : p < 52 ? b(Blocks.GRAVEL) : p < 66 ? b(Blocks.TUFF) : p < 80 ? b(Blocks.COBBLESTONE)
                : p < 90 ? b(Blocks.PACKED_MUD) : b(Blocks.STONE);
    }

    // ------------------------------------------------------------------ el plano de lo de abajo (y lo construido)

    /** Lo que se escribe a mano, por chunks: bloques sueltos, huecos (aire) y el forro de roca de alrededor de los huecos. */
    static final class Plano {
        final Map<Long, Map<Long, BlockState>> porChunk = new HashMap<>();
        final Set<Long> unes = new HashSet<>(), forro = new HashSet<>(), huecos = new HashSet<>();

        private Map<Long, BlockState> de(int x, int z) {
            return porChunk.computeIfAbsent(Velmor.clave(x >> 4, z >> 4), k -> new LinkedHashMap<>());
        }

        void put(int x, int y, int z, BlockState s) {
            long k = BlockPos.asLong(x, y, z);
            de(x, z).put(k, s);
            unes.remove(k);
            forro.remove(k);
            if (!s.isAir() && !s.is(Blocks.WATER)) huecos.remove(k);
        }

        void une(int x, int y, int z, BlockState s) {
            put(x, y, z, s);
            unes.add(BlockPos.asLong(x, y, z));
        }

        void aire(int x, int y, int z) {
            put(x, y, z, Pal.AIR);
            huecos.add(BlockPos.asLong(x, y, z));
        }

        void agua(int x, int y, int z) {
            put(x, y, z, Pal.WATER);
            huecos.add(BlockPos.asLong(x, y, z));
        }

        boolean hueco(int x, int y, int z) {
            return huecos.contains(BlockPos.asLong(x, y, z));
        }

        boolean hay(int x, int y, int z) {
            Map<Long, BlockState> m = porChunk.get(Velmor.clave(x >> 4, z >> 4));
            return m != null && m.containsKey(BlockPos.asLong(x, y, z));
        }

        BlockState en(int x, int y, int z) {
            Map<Long, BlockState> m = porChunk.get(Velmor.clave(x >> 4, z >> 4));
            return m == null ? null : m.get(BlockPos.asLong(x, y, z));
        }

        interface Material {
            BlockState en(int x, int y, int z);
        }

        /** Alrededor de cada hueco, lo que no se ha escrito se forra de roca (solo donde ya hay roca: no tapa la corta). */
        void forrar(Material m) {
            List<Long> l = new ArrayList<>(huecos);
            for (long k : l) {
                int x = BlockPos.getX(k), y = BlockPos.getY(k), z = BlockPos.getZ(k);
                for (Direction d : Direction.values()) {
                    int nx = x + d.getStepX(), ny = y + d.getStepY(), nz = z + d.getStepZ();
                    if (hay(nx, ny, nz)) continue;
                    put(nx, ny, nz, m.en(nx, ny, nz));
                    forro.add(BlockPos.asLong(nx, ny, nz));
                }
            }
        }

        Element pieza(int capa, int ax, int az, int bx, int bz) {
            return new Element(0) {
                {
                    this.layer = capa;
                    bounds(ax, N3 - 8, az, bx, 240, bz);
                }

                @Override
                public void build(Canvas c) {
                    Map<Long, BlockState> m = porChunk.get(Velmor.clave(c.minX >> 4, c.minZ >> 4));
                    if (m == null) return;
                    for (Map.Entry<Long, BlockState> e : m.entrySet()) {
                        long k = e.getKey();
                        int x = BlockPos.getX(k), y = BlockPos.getY(k), z = BlockPos.getZ(k);
                        if (forro.contains(k) && c.get(x, y, z).isAir()) continue;
                        if (unes.contains(k)) c.unir(x, y, z, e.getValue());
                        else c.set(x, y, z, e.getValue());
                    }
                }
            };
        }
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

    private boolean hueco(int u, int y, int v) {
        return plano.hueco(x(u), y, z(v));
    }

    /** Si en (u, y, v) hay algo escrito que no es aire (o la roca de alrededor). */
    private boolean macizo(int u, int y, int v) {
        BlockState s = plano.en(x(u), y, z(v));
        return s != null && !s.isAir() && !s.is(Blocks.WATER);
    }

    // ------------------------------------------------------------------ las galerias

    static final int RIELES = 1, MARCOS = 2, ENTIBADO = 4, FAROLES = 8, TODO = RIELES | MARCOS | FAROLES;

    /**
     * Un tramo de galeria recto: de norte a sur (ns: u fijo, a = v) o de este a oeste (a = u, v fijo); el suelo va de s0 (en a0)
     * a s1 (en a1) —si cambia, es una rampa con escalones—; medio ancho y alto (huecos de s + 1 a s + alto).
     */
    record Tramo(boolean ns, int fijo, int a0, int a1, int s0, int s1, int medio, int alto, int cosas) {
        int suelo(int a) {
            if (a0 == a1 || s0 == s1) return s0;
            double t = (a - a0) / (double) (a1 - a0);
            return (int) Math.round(s0 + t * (s1 - s0));
        }

        int lo() {
            return Math.min(a0, a1);
        }

        int hi() {
            return Math.max(a0, a1);
        }

        int u(int a, int o) {
            return ns ? fijo + o : a;
        }

        int v(int a, int o) {
            return ns ? a : fijo + o;
        }
    }

    private void tramoNS(int u, int v0, int v1, int s0, int s1, int cosas) {
        tramos.add(new Tramo(true, u, v0, v1, s0, s1, 2, 5, cosas));
    }

    private void tramoEO(int v, int u0, int u1, int s, int cosas) {
        tramos.add(new Tramo(false, v, u0, u1, s, s, 2, 5, cosas));
    }

    private void cavar(Tramo t) {
        for (int a = t.lo(); a <= t.hi(); a++) {
            int s = t.suelo(a);
            for (int o = -t.medio; o <= t.medio; o++) {
                int u = t.u(a, o), v = t.v(a, o);
                if (!hueco(u, s, v)) put(u, s, v, sueloGaleria(x(u), s, z(v)));
                for (int y = s + 1; y <= s + t.alto; y++) aire(u, y, v);
            }
        }
    }

    /** Los marcos de madera (cada cuatro), los faroles, los railes y los escalones de las rampas de un tramo. */
    private void vestir(Tramo t) {
        Direction.Axis eje = t.ns ? Direction.Axis.X : Direction.Axis.Z;
        // los escalones de las rampas: donde el suelo baja, un escalon que mira hacia arriba
        if (t.s0 != t.s1) {
            int paso = t.a1 > t.a0 ? 1 : -1;
            Direction arriba = t.ns ? (paso > 0 ? Direction.NORTH : Direction.SOUTH) : (paso > 0 ? Direction.WEST : Direction.EAST);
            if (t.s1 > t.s0) arriba = arriba.getOpposite();
            for (int a = t.a0 + paso; a != t.a1 + paso; a += paso) {
                int s = t.suelo(a), antes = t.suelo(a - paso);
                if (s == antes) continue;
                int bajo = Math.min(s, antes), ab = s < antes ? a : a - paso;
                for (int o = -t.medio; o <= t.medio; o++)
                    put(t.u(ab, o), bajo + 1, t.v(ab, o), escalon(pct(t.u(ab, o), bajo, t.v(ab, o), 9) < 70 ? Blocks.COBBLESTONE_STAIRS : Blocks.SPRUCE_STAIRS, arriba));
            }
        }
        if ((t.cosas & MARCOS) != 0) {
            int n = 0;
            for (int a = t.lo() + 2; a <= t.hi() - 2; a += 4) {
                int s = t.suelo(a);
                if (t.s0 != t.s1 && (t.suelo(a - 1) != s || t.suelo(a + 1) != s)) continue;
                boolean ok = true;
                for (int sg = -1; sg <= 1 && ok; sg += 2)
                    for (int y = s + 1; y <= s + t.alto - 1 && ok; y++) if (hueco(t.u(a, sg * (t.medio + 1)), y, t.v(a, sg * (t.medio + 1)))) ok = false;
                if (!ok) continue;
                n++;
                marcos++;
                int p = pct(t.u(a, 0), s, t.v(a, 0), 10);
                boolean roto = p < 10, caido = p >= 10 && p < 14;
                for (int sg = -1; sg <= 1; sg += 2) {
                    int u = t.u(a, sg * t.medio), v = t.v(a, sg * t.medio);
                    int alto = roto && sg == 1 ? 2 : t.alto - 1;
                    for (int y = s + 1; y <= s + alto; y++) put(u, y, v, tronco(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.Y));
                }
                if (caido) {
                    // el travesano caido: tirado en el suelo de lado
                    for (int o = -t.medio + 1; o <= t.medio - 1; o++) if (o != 0) put(t.u(a, o), s + 1, t.v(a, o), tronco(Blocks.SPRUCE_LOG, eje));
                } else {
                    for (int o = -t.medio; o <= t.medio; o++) {
                        if (roto && o > 0) continue;
                        put(t.u(a, o), s + t.alto, t.v(a, o), tronco(Blocks.SPRUCE_LOG, eje));
                    }
                    if ((t.cosas & FAROLES) != 0 && n % 2 == 0 && !roto) {
                        put(t.u(a, 0), s + t.alto - 1, t.v(a, 0), farol(true, true));
                        faroles++;
                    }
                }
                // el techo entablado entre este marco y el siguiente
                if ((t.cosas & ENTIBADO) != 0 && !roto && !caido)
                    for (int da = 1; da <= 3; da++) {
                        int aa = a + da;
                        if (aa > t.hi() - 1) break;
                        for (int o = -t.medio; o <= t.medio; o++) {
                            int u = t.u(aa, o), v = t.v(aa, o), y = t.suelo(aa) + t.alto + 1;
                            if (hueco(u, y, v)) continue;
                            if (pct(u, y, v, 11) < 82) put(u, y, v, b(Blocks.SPRUCE_PLANKS));
                        }
                    }
            }
        }
        if ((t.cosas & RIELES) != 0 && t.s0 == t.s1) {
            for (int a = t.lo() + 1; a <= t.hi() - 1; a++) {
                int s = t.s0, u = t.u(a, 0), v = t.v(a, 0);
                if (!hueco(u, s + 1, v) || plano.en(x(u), s + 1, z(v)) == null || !plano.en(x(u), s + 1, z(v)).isAir()) continue;
                // en los cruces no: (los lados de fuera tienen que ser pared)
                if (hueco(t.u(a, t.medio + 1), s + 1, t.v(a, t.medio + 1)) || hueco(t.u(a, -t.medio - 1), s + 1, t.v(a, -t.medio - 1))) continue;
                if (pct(u, s, v, 12) < 7) continue;
                put(u, s + 1, v, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, t.ns ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST));
                rieles++;
            }
        }
    }

    // ------------------------------------------------------------------ los tres niveles

    /** Nivel 1: la Galeria del Rey de la bocamina al norte, las dos traviesas, el frente del este y la rampa que baja. */
    private void nivel1() {
        // la Galeria del Rey: de la bocamina al hundimiento
        tramoNS(0, -43, -98, N1, N1, TODO | ENTIBADO);
        // la traviesa del polvorin y del comedor
        tramoEO(-62, -27, 22, N1, TODO);
        // la traviesa del pozo: al este, el enganche (los barrotes del pozo); al oeste, la galeria que lleva a la rampa
        tramoEO(-84, -30, 16, N1, TODO);
        tramoNS(-30, -82, -104, N1, N1, MARCOS | FAROLES);
        // la rampa: baja veinte bloques al norte
        tramoNS(-30, -105, -145, N1, N2, MARCOS);
        // el frente del este: del comedor al norte, sin salida (donde se picaba)
        tramoNS(30, -71, -97, N1, N1, MARCOS | RIELES);
    }

    /** Nivel 2: del pie de la rampa al lago; del lago al este y al sur, al enganche de abajo del pozo; y al norte, el pozo de la veta. */
    private void nivel2() {
        tramoEO(-147, -32, -17, N2, MARCOS | FAROLES);
        tramoEO(-153, 18, 36, N2, TODO);
        tramoNS(36, -151, -86, N2, N2, TODO);
        tramoEO(-86, 22, 34, N2, TODO);
        // al norte del lago, hacia el pozo de la veta
        tramoNS(0, -163, -173, N2, N2, MARCOS);
    }

    /** Nivel 3: del pie del pozo de la veta a la Caverna del Filon. */
    private void nivel3() {
        tramoNS(0, -175, -180, N3, N3, MARCOS);
    }

    /** El pozo maestro (de la boca, bajo el castillete, al enganche del nivel 2) y el pozo de la veta (del nivel 2 al 3). */
    private void pozo() {
        for (int u = POZO_U0; u <= POZO_U1; u++)
            for (int v = POZO_V0; v <= POZO_V1; v++)
                for (int y = N2 + 1; y <= SUELO - 1; y++) aire(u, y, v);
        // el pozo de la veta: 2 x 2, del nivel 2 al 3
        for (int u = 0; u <= 1; u++)
            for (int v = -175; v <= -174; v++)
                for (int y = N3 + 1; y <= N2 + 5; y++) aire(u, y, v);
    }

    // ------------------------------------------------------------------ las cuevas: el lago y el filon

    /** La Gruta del Agua Negra: el lago (hondo en el medio), sus orillas al oeste y al este, y la boveda irregular. */
    private void lago() {
        for (int u = (int) -LAGO_A - 2; u <= LAGO_A + 2; u++)
            for (int v = LAGO_V - (int) LAGO_B - 2; v <= LAGO_V + LAGO_B + 2; v++) {
                double rho = Math.hypot(u / LAGO_A, (v - LAGO_V) / LAGO_B) + ruido(u, v) * 0.06;
                if (rho > 1) continue;
                int techo = N2 + 1 + (int) Math.round(3 + 11 * Math.sqrt(Math.max(0, 1 - rho * rho)) + ruido(u * 1.3, v * 1.3) * 1.5);
                boolean orilla = rho > 0.84 || Math.abs(u) > LAGO_A - 4;
                if (orilla) {
                    put(u, N2, v, pct(u, N2, v, 20) < 50 ? b(Blocks.GRAVEL) : b(Blocks.TUFF));
                    for (int y = N2 + 1; y <= techo; y++) aire(u, y, v);
                } else {
                    double q = Math.min(1, rho / 0.84);
                    int fondo = AGUA - 1 - (int) Math.round(4 * Math.sqrt(Math.max(0, 1 - q * q)));
                    put(u, fondo, v, pct(u, fondo, v, 21) < 45 ? b(Blocks.GRAVEL) : pct(u, fondo, v, 21) < 75 ? b(Blocks.CLAY) : b(Blocks.MUD));
                    for (int y = fondo + 1; y <= AGUA; y++) plano.agua(x(u), y, z(v));
                    for (int y = AGUA + 1; y <= techo; y++) aire(u, y, v);
                }
            }
    }

    /** La Caverna del Filon: el suelo casi llano (alguna repisa), la boveda alta y la veta roja que cruza la pared del norte. */
    private void filon() {
        for (int u = (int) -FILON_A - 2; u <= FILON_A + 2; u++)
            for (int v = FILON_V - (int) FILON_B - 2; v <= FILON_V + FILON_B + 2; v++) {
                double rho = Math.hypot(u / FILON_A, (v - FILON_V) / FILON_B) + ruido(u * 0.8, v * 0.8) * 0.07;
                if (rho > 1) continue;
                int suelo = N3 + (rho > 0.82 ? 1 : 0);
                int techo = N3 + 2 + (int) Math.round(5 + 13 * Math.sqrt(Math.max(0, 1 - rho * rho)) + ruido(u * 1.1, v * 1.4) * 1.8);
                int p = pct(u, suelo, v, 22);
                put(u, suelo, v, p < 35 ? b(Blocks.COBBLED_DEEPSLATE) : p < 60 ? b(Blocks.DEEPSLATE) : p < 80 ? b(Blocks.TUFF) : p < 92 ? b(Blocks.GRAVEL)
                        : b(Blocks.BLACKSTONE));
                for (int y = suelo + 1; y <= techo; y++) aire(u, y, v);
            }
    }

    /** Las salas de los niveles (cajas): el polvorin, el comedor, el hundimiento, las literas, la capilla y su cuarto escondido. */
    private void salas() {
        sala(-37, -68, -28, -56, N1, 5);           // el polvorin
        sala(23, -70, 35, -54, N1, 6);             // el comedor
        sala(21, -89, 27, -81, N2, 5);             // el enganche de abajo (al este del pozo)
        sala(39, -128, 49, -112, N2, 5);           // las literas
        sala(37, -121, 38, -119, N2, 3);           // su puerta
        sala(25, -110, 33, -102, N2, 6);           // la capilla de los mineros
        sala(34, -107, 35, -105, N2, 3);           // su puerta
        sala(16, -109, 23, -103, N2, 4);           // el cuarto de detras del altar
        // el umbral bajo el muro ilusorio (la roca de debajo, por si acaso)
        for (int v = -107; v <= -105; v++) put(24, N2, v, sueloGaleria(x(24), N2, z(v)));
    }

    private void sala(int u0, int v0, int u1, int v1, int s, int alto) {
        for (int u = Math.min(u0, u1); u <= Math.max(u0, u1); u++)
            for (int v = Math.min(v0, v1); v <= Math.max(v0, v1); v++) {
                if (!hueco(u, s, v)) put(u, s, v, sueloGaleria(x(u), s, z(v)));
                for (int y = s + 1; y <= s + alto; y++) aire(u, y, v);
            }
    }

    // ------------------------------------------------------------------ lo de dentro

    /** La escala del pozo maestro, la escotilla de arriba con su palanca, los barrotes del nivel 1 y la escala del pozo de la veta. */
    private void escalera() {
        for (int y = N2 + 1; y <= SUELO - 1; y++) put(ESCALA_U, y, ESCALA_V, escala(Direction.SOUTH));
        // los barrotes del enganche del nivel 1 (se ve el pozo y no se entra)
        for (int v = POZO_V0; v <= POZO_V1; v++)
            for (int y = N1 + 1; y <= N1 + 4; y++) {
                aire(POZO_U0 - 1, y, v);
                une(POZO_U0 - 1, y, v, b(Blocks.IRON_BARS));
            }
        // el pozo de la veta: escala contra la pared del sur (se baja mirando al norte)
        for (int y = N3 + 1; y <= N2; y++) put(0, y, -174, escala(Direction.NORTH));
    }

    /** Lo de dentro de las salas: el polvorin, el comedor, el frente del este, las literas, la capilla, el escondido y el filon. */
    private void interiores() {
        // ---- el polvorin: filas de barriles con los cartuchos encima (velas rojas sin encender) y cajones
        for (int u = -36; u <= -29; u++)
            for (int v = -67; v <= -57; v++) {
                if (v >= -64 && v <= -60) continue;               // el pasillo de la traviesa
                if ((u + 36) % 3 == 2) continue;
                int p = pct(u, N1, v, 50), alto = 1 + p % 2;
                for (int dy = 1; dy <= alto; dy++)
                    put(u, N1 + dy, v, Blocks.BARREL.defaultBlockState().setValue(net.minecraft.world.level.block.BarrelBlock.FACING, Direction.UP));
                if (p < 40) put(u, N1 + alto + 1, v, Blocks.RED_CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 1 + p % 4));
                else if (p < 55) put(u, N1 + alto + 1, v, b(Blocks.SPRUCE_PRESSURE_PLATE));
            }
        put(-29, N1 + 4, -62, farol(false, true));
        put(-29, N1 + 5, -62, cadena(Direction.Axis.Y));
        // ---- el comedor: dos mesas largas con sus bancos, el fogon apagado y el caldero
        for (int mv : new int[]{-66, -58})
            for (int u = 25; u <= 32; u++) {
                une(u, N1 + 1, mv, b(Blocks.SPRUCE_FENCE));
                put(u, N1 + 2, mv, b(Blocks.SPRUCE_PRESSURE_PLATE));
                for (int sg = -1; sg <= 1; sg += 2)
                    if (pct(u, N1, mv + sg, 51) < 75) put(u, N1 + 1, mv + sg, escalon(Blocks.SPRUCE_STAIRS, sg < 0 ? Direction.NORTH : Direction.SOUTH));
            }
        put(34, N1 + 1, -62, b(Blocks.COBBLESTONE));
        put(34, N1 + 2, -62, Blocks.CAMPFIRE.defaultBlockState().setValue(net.minecraft.world.level.block.CampfireBlock.LIT, false));
        put(34, N1 + 1, -60, b(Blocks.CAULDRON));
        put(34, N1 + 1, -64, Blocks.BARREL.defaultBlockState().setValue(net.minecraft.world.level.block.BarrelBlock.FACING, Direction.WEST));
        for (int[] q : new int[][]{{26, -62}, {31, -62}}) {
            put(q[0], N1 + 6, q[1], cadena(Direction.Axis.Y));
            put(q[0], N1 + 5, q[1], farol(false, true));
        }
        // ---- el frente del este: la pared del fondo picada (vetas y carbon), un andamio y la vagoneta
        for (int u = 28; u <= 32; u++)
            for (int y = N1 + 1; y <= N1 + 5; y++) {
                int p = pct(u, y, -98, 52);
                put(u, y, -98, p < 30 ? b(Blocks.DEEPSLATE_IRON_ORE) : p < 48 ? b(Blocks.COAL_ORE) : p < 62 ? b(Blocks.BLACKSTONE) : pared(x(u), y, z(-98)));
            }
        for (int y = N1 + 1; y <= N1 + 3; y++) put(32, y, -96, Blocks.SCAFFOLDING.defaultBlockState()
                .setValue(net.minecraft.world.level.block.ScaffoldingBlock.DISTANCE, 0).setValue(net.minecraft.world.level.block.ScaffoldingBlock.BOTTOM, y == N1 + 1));
        // ---- las literas: dos pisos de tablas con jergones (lana) a lo largo de los dos muros largos, la mesa y faroles
        for (int u : new int[]{39, 49})
            for (int v = -127; v <= -113; v += 3) {
                if (u == 39 && v >= -122 && v <= -118) continue;  // la puerta
                for (int dv = 0; dv <= 1; dv++) {
                    put(u, N2 + 1, v + dv, losa(Blocks.SPRUCE_SLAB, SlabType.TOP));
                    put(u, N2 + 2, v + dv, b(pct(u, N2, v, 53) < 50 ? Blocks.WHITE_WOOL : Blocks.LIGHT_GRAY_WOOL));
                    put(u, N2 + 3, v + dv, losa(Blocks.SPRUCE_SLAB, SlabType.TOP));
                    put(u, N2 + 4, v + dv, pct(u, N2 + 4, v + dv, 54) < 60 ? b(Blocks.BROWN_WOOL) : losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
                }
                une(u, N2 + 1, v + 2, b(Blocks.SPRUCE_FENCE));
                une(u, N2 + 2, v + 2, b(Blocks.SPRUCE_FENCE));
                une(u, N2 + 3, v + 2, b(Blocks.SPRUCE_FENCE));
                literas++;
            }
        for (int v = -124; v <= -116; v++) {
            if (v == -120) continue;
            une(44, N2 + 1, v, b(Blocks.SPRUCE_FENCE));
            put(44, N2 + 2, v, b(Blocks.SPRUCE_PRESSURE_PLATE));
        }
        for (int[] q : new int[][]{{44, -126}, {44, -114}}) {
            put(q[0], N2 + 5, q[1], cadena(Direction.Axis.Y));
            put(q[0], N2 + 4, q[1], farol(false, true));
        }
        put(42, N2 + 1, -127, b(Blocks.CANDLE).setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
        // ---- la capilla de los mineros: el altar contra el muro del oeste (detras, el muro ilusorio), bancos y muchas velas
        for (int v = -107; v <= -105; v++) {
            put(25, N2 + 1, v, b(Blocks.POLISHED_DEEPSLATE));
            put(26, N2 + 1, v, escalonAlto(Blocks.POLISHED_DEEPSLATE_STAIRS, Direction.WEST));
        }
        put(25, N2 + 2, -106, b(Blocks.SKELETON_SKULL).setValue(net.minecraft.world.level.block.SkullBlock.ROTATION, 12));
        put(25, N2 + 2, -107, b(Blocks.CANDLE).setValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 3).setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
        put(25, N2 + 2, -105, b(Blocks.CANDLE).setValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 2).setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
        put(26, N2 + 2, -106, b(Blocks.RED_CANDLE).setValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 4).setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
        for (int u = 28; u <= 32; u += 2)
            for (int v : new int[]{-109, -108, -104, -103}) put(u, N2 + 1, v, escalon(Blocks.SPRUCE_STAIRS, Direction.EAST));
        for (int[] q : new int[][]{{25, -110}, {25, -102}, {33, -110}, {33, -102}, {29, -110}, {29, -102}})
            put(q[0], N2 + 1, q[1], b(pct(q[0], 0, q[1], 55) < 50 ? Blocks.WHITE_CANDLE : Blocks.CANDLE)
                    .setValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 1 + pct(q[0], 1, q[1], 55) % 4).setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
        // el muro ilusorio de detras del altar (la misma roca: escrito a mano para que siempre este)
        for (BlockPos q : muro(MURO_CAPILLA)) plano.put(q.getX(), q.getY(), q.getZ(), bloqueMuro(MURO_CAPILLA, q));
        // ---- el escondido: el arcon bueno, huesos y lo que se guardo alli
        put(17, N2 + 1, -108, b(Blocks.BONE_BLOCK));
        put(17, N2 + 1, -104, b(Blocks.SKELETON_SKULL).setValue(net.minecraft.world.level.block.SkullBlock.ROTATION, 4));
        put(22, N2 + 1, -108, b(Blocks.RAW_IRON_BLOCK));
        put(22, N2 + 1, -104, b(Blocks.RAW_GOLD_BLOCK));
        put(19, N2 + 4, -106, farol(true, true));
        // ---- el enganche de abajo: el banco del enganchador, la campana de senales (sin badajo: un farol) y la jaula caida
        for (int u = POZO_U0; u <= POZO_U1; u++)
            for (int v = POZO_V0 + 1; v <= POZO_V1; v++) {
                put(u, N2 + 1, v, Blocks.IRON_BARS.defaultBlockState());
                if (u != ESCALA_U) put(u, N2 + 2, v, Blocks.IRON_BARS.defaultBlockState());
            }
        put(26, N2 + 4, -85, farol(true, true));
        put(26, N2 + 5, -85, cadena(Direction.Axis.Y));
        put(27, N2 + 1, -88, Blocks.BARREL.defaultBlockState().setValue(net.minecraft.world.level.block.BarrelBlock.FACING, Direction.UP));
        // ---- el enganche del nivel 1: la jaula de mirar y un farol
        put(14, N1 + 4, -84, farol(true, true));
        put(14, N1 + 5, -84, cadena(Direction.Axis.Y));
        // ---- el filon: andamios contra la pared del norte, faroles y la vagoneta volcada (parte 7ar: en el medio no, que alli pica
        // Hakon el corazon de la veta)
        for (int u = -12; u <= 12; u += 4) {
            if (Math.abs(u) < 8) continue;
            int v = -206 + Math.abs(u) / 4;
            for (int y = N3 + 1; y <= N3 + 6; y++) put(u, y, v, tronco(Blocks.SPRUCE_LOG, Direction.Axis.Y));
            for (int du = -2; du <= 2; du++) if (!macizo(u + du, N3 + 4, v + 1)) put(u + du, N3 + 4, v + 1, b(Blocks.SPRUCE_PLANKS));
            put(u, N3 + 3, v + 1, farol(true, true));
        }
        for (int y = N3 + 1; y <= N3 + 3; y++) put(-10, y, -204, escala(Direction.SOUTH));
        put(-3, N3 + 1, -190, Blocks.COMPOSTER.defaultBlockState().setValue(ComposterBlock.LEVEL, 3));
        put(-4, N3 + 1, -191, b(Blocks.RAW_IRON_BLOCK));
        put(5, N3 + 1, -185, b(Blocks.COBBLED_DEEPSLATE));
        put(6, N3 + 1, -186, losa(Blocks.COBBLED_DEEPSLATE_SLAB, SlabType.BOTTOM));
        corazonVeta();
    }

    /** El corazon de la veta (parte 7ar): donde la veta roja baja hasta el suelo en la pared del norte, un bulto de cristal que asoma. */
    public static final double CORAZON_U = 0.5, CORAZON_V = -208.5;

    private void corazonVeta() {
        double cy = N3 + 2.6;
        for (int u = -4; u <= 5; u++)
            for (int v = -212; v <= -205; v++)
                for (int y = N3; y <= N3 + 6; y++) {
                    double d = Math.sqrt(Math.pow((u - CORAZON_U) / 3.6, 2) + Math.pow((y - cy) / 3.0, 2) + Math.pow((v - CORAZON_V) / 2.4, 2));
                    if (d > 1) continue;
                    // lo de dentro, cristal; la costra, mineral rojo, obsidiana que llora, magma (no en el suelo) y alguna gema
                    int p = Hash.pct(x(u), y, z(v), SEMILLA + 41);
                    BlockState st = d < 0.55 ? com.darkcraft.gen.TorreCupula.gema(true)
                            : p < 34 ? b(Blocks.DEEPSLATE_REDSTONE_ORE) : p < 54 ? b(Blocks.CRYING_OBSIDIAN)
                            : p < 72 && y > N3 + 1 ? b(Blocks.MAGMA_BLOCK) : p < 88 ? com.darkcraft.gen.TorreCupula.gema(true) : b(Blocks.BLACKSTONE);
                    put(u, y, v, st);
                }
    }

    /**
     * Los techos rotos (derrumbes): el techo de la galeria suelto (piedra rota, tierra con raices colgando) y, al pasar debajo o
     * con un golpe fuerte cerca, se viene abajo ({@link #tick}).
     */
    private void techosRotos() {
        int[][] t = {{0, N1 + 6, -74}, {-12, N1 + 6, -62}, {-8, N1 + 6, -84}, {-30, N1 + 6, -98}, {36, N2 + 6, -130}, {36, N2 + 6, -104},
                {30, N2 + 6, -153}};
        for (int[] q : t) {
            derrumbes.add(new int[]{x(q[0]), q[1], z(q[2])});
            for (int du = -1; du <= 1; du++)
                for (int dv = -1; dv <= 1; dv++) {
                    int u = q[0] + du, v = q[2] + dv, y = q[1];
                    if (hueco(u, y, v)) continue;
                    int p = pct(u, y, v, 60);
                    put(u, y, v, p < 30 ? b(Blocks.COBBLED_DEEPSLATE) : p < 52 ? b(Blocks.CRACKED_DEEPSLATE_TILES) : p < 76 ? b(Blocks.ROOTED_DIRT)
                            : b(Blocks.COARSE_DIRT));
                    if (p >= 52 && p < 76 && hueco(u, y - 1, v)) put(u, y - 1, v, b(Blocks.HANGING_ROOTS));
                }
        }
        // el monton de escombros del cargador enterrado del fondo de la corta
        for (int du = -2; du <= 2; du++)
            for (int dv = -2; dv <= 2; dv++) {
                if (Math.abs(du) + Math.abs(dv) > 3 || (du == 0 && dv == 0)) continue;
                int p = pct(-6 + du, FONDO, -38 + dv, 61);
                put(-6 + du, FONDO + 1, -38 + dv, p < 50 ? losa(Blocks.COBBLED_DEEPSLATE_SLAB, SlabType.BOTTOM) : p < 80 ? b(Blocks.GRAVEL)
                        : losa(Blocks.COBBLESTONE_SLAB, SlabType.BOTTOM));
            }
    }

    private void bocamina() {
        // el portal: dos postes gordos y un dintel doble a la entrada de la Galeria del Rey
        for (int v = -43; v >= -45; v--) {
            for (int sg = -1; sg <= 1; sg += 2)
                for (int y = N1 + 1; y <= N1 + 4; y++) put(sg * 2, y, v, tronco(Blocks.SPRUCE_LOG, Direction.Axis.Y));
            for (int u = -2; u <= 2; u++) put(u, N1 + 5, v, tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        }
        // el tejadillo de tablones sobre la entrada
        for (int u = -3; u <= 3; u++) {
            put(u, N1 + 6, -43, losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
            put(u, N1 + 6, -42, escalon(Blocks.SPRUCE_STAIRS, Direction.NORTH));
        }
    }

    /** El castillete del pozo maestro, sobre su boca; la escotilla. */
    private void castillete() {
        // la boca: tablones y, sobre la escala, la escotilla de hierro (solo se abre con la palanca de debajo)
        for (int u = POZO_U0 - 1; u <= POZO_U1 + 1; u++)
            for (int v = POZO_V0 - 1; v <= POZO_V1 + 1; v++) put(u, SUELO, v, b(Blocks.SPRUCE_PLANKS));
        put(ESCALA_U, SUELO, ESCALA_V, Blocks.IRON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.SOUTH)
                .setValue(TrapDoorBlock.HALF, Half.TOP).setValue(TrapDoorBlock.OPEN, false));
        put(ESCALA_U + 1, SUELO - 1, ESCALA_V, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.CEILING)
                .setValue(LeverBlock.FACING, Direction.SOUTH).setValue(LeverBlock.POWERED, false));
        // las cuatro patas y los travesanos
        int alto = SUELO + 17;
        for (int u : new int[]{POZO_U0 - 3, POZO_U1 + 3})
            for (int v : new int[]{POZO_V0 - 3, POZO_V1 + 3}) {
                for (int y = SUELO + 1; y <= alto; y++) put(u, y, v, tronco(Blocks.SPRUCE_LOG, Direction.Axis.Y));
                put(u, SUELO, v, b(Blocks.STONE_BRICKS));
            }
        for (int y = SUELO + 5; y <= alto; y += 6) {
            for (int u = POZO_U0 - 3; u <= POZO_U1 + 3; u++)
                for (int v : new int[]{POZO_V0 - 3, POZO_V1 + 3}) put(u, y, v, tronco(Blocks.SPRUCE_LOG, Direction.Axis.X));
            for (int v = POZO_V0 - 3; v <= POZO_V1 + 3; v++)
                for (int u : new int[]{POZO_U0 - 3, POZO_U1 + 3}) put(u, y, v, tronco(Blocks.SPRUCE_LOG, Direction.Axis.Z));
        }
        // los dos puntales inclinados hacia la casa de maquinas (al oeste)
        for (int v : new int[]{POZO_V0 - 3, POZO_V1 + 3})
            for (int k = 0; k <= 12; k++) {
                int u = POZO_U0 - 4 - k / 2, y = alto - 1 - k;
                if (y <= SUELO) break;
                put(u, y, v, tronco(Blocks.SPRUCE_LOG, k % 2 == 0 ? Direction.Axis.Y : Direction.Axis.X));
            }
        // arriba, la plataforma y la rueda (de cara al sur), con la soga que baja a la boca
        for (int u = POZO_U0 - 3; u <= POZO_U1 + 3; u++)
            for (int v = POZO_V0 - 3; v <= POZO_V1 + 3; v++)
                if (u != ESCALA_U || v != POZO_V0 + 1) put(u, alto, v, b(Blocks.SPRUCE_PLANKS));
        int ru = ESCALA_U - 3, ry = alto + 5;
        for (int du = -4; du <= 4; du++)
            for (int dy = -4; dy <= 4; dy++) {
                double r = Math.hypot(du, dy);
                if (Math.abs(r - 3.2) < 0.55) put(ru + du, ry + dy, POZO_V0 + 1, b(Blocks.STRIPPED_DARK_OAK_LOG));
                else if (r < 2.7 && (du == 0 || dy == 0)) put(ru + du, ry + dy, POZO_V0 + 1, b(Blocks.DARK_OAK_FENCE));
            }
        put(ru, ry, POZO_V0 + 1, tronco(Blocks.DARK_OAK_LOG, Direction.Axis.Z));
        for (int y = alto + 1; y < ry; y++) put(ru, y, POZO_V0 + 1, tronco(Blocks.SPRUCE_LOG, Direction.Axis.Y));
        for (int y = SUELO + 1; y <= ry; y++) if (!macizo(ESCALA_U, y, POZO_V0 + 1)) put(ESCALA_U, y, POZO_V0 + 1, cadena(Direction.Axis.Y));
        // dentro del pozo: la soga rota que cuelga de la boca
        for (int y = SUELO - 1; y >= SUELO - 14; y--) put(ESCALA_U, y, POZO_V0 + 1, cadena(Direction.Axis.Y));
    }

    // ------------------------------------------------------------------ las casetas del campamento

    /** Para que se haga cada caseta (lo de dentro). */
    enum Uso { OFICINA, LAMPISTERIA, FRAGUA, BARRACON, MAQUINAS }

    /** Las casetas de la mina: las de Brumavela, de tablas de abeto y zocalo de piedra de la mina, con lo de dentro de cada una. */
    final class Caseta extends Casa {
        final Uso uso;

        Caseta(Uso uso, int u0, int v0, int u1, int v1, Direction frente, int ruina, Casa.Estilo estilo, int tejado, Casa.Tipo tipo) {
            super(SEMILLA + 100 + uso.ordinal() * 17, (x, z) -> SUELO, new Rect(x(u0), z(v0), x(u1), z(v1)), frente, 1, ruina, estilo, tejado, tipo);
            this.uso = uso;
            botin(BOTIN, BOTIN);
        }

        @Override
        protected BlockState zocalo(int x, int yy, int z) {
            int p = Hash.pct(x, yy, z, seed + 1);
            return p < 36 ? b(Blocks.COBBLESTONE) : p < 56 ? b(Blocks.STONE) : p < 72 ? b(Blocks.TUFF) : p < 86 ? b(Blocks.COBBLED_DEEPSLATE) : b(Blocks.ANDESITE);
        }

        @Override
        protected BlockState suelo(int x, int z) {
            int p = Hash.pct(x, y, z, seed + 4);
            if (uso == Uso.FRAGUA || uso == Uso.MAQUINAS) return p < 45 ? b(Blocks.COBBLESTONE) : p < 75 ? b(Blocks.STONE) : b(Blocks.COARSE_DIRT);
            return p < 70 ? b(Blocks.SPRUCE_PLANKS) : p < 85 ? b(Blocks.DARK_OAK_PLANKS) : b(Blocks.COARSE_DIRT);
        }

        @Override
        protected void amueblar(Canvas c) {
            switch (uso) {
                case LAMPISTERIA -> lampisteria(c);
                case MAQUINAS -> maquinas(c);
                default -> super.amueblar(c);
            }
        }

        /**
         * La lampisteria: en el muro del fondo, dos filas de ganchos donde se colgaban los faroles de los mineros al salir del turno
         * (casi todos vacios: el turno de noche no subio); debajo, el mostrador con los tres faroles que quedaron.
         */
        private void lampisteria(Canvas c) {
            int n = 0;
            for (int a = 0; a < A; a++) {
                pon(c, a, B - 1, 0, losa(Blocks.SPRUCE_SLAB, SlabType.TOP));
                for (int dy = 2; dy <= 3; dy++) {
                    pon(c, a, B - 1, dy, Blocks.TRIPWIRE_HOOK.defaultBlockState().setValue(TripWireHookBlock.FACING, frente));
                    n++;
                }
                if (a == 1 || a == A / 2 + 1 || a == A - 2) pon(c, a, B - 1, 1, farol(false, false));
            }
            ganchos = n;
            // la mesa del lampista con su taburete, un barril de aceite y el arcon
            mesa(c, A / 2, B / 2);
            silla(c, A / 2 - 1, B / 2, izquierda());
            pon(c, A - 1, 0, 0, Blocks.BARREL.defaultBlockState().setValue(net.minecraft.world.level.block.BarrelBlock.FACING, Direction.UP));
            pon(c, A - 1, 1, 0, Blocks.BARREL.defaultBlockState().setValue(net.minecraft.world.level.block.BarrelBlock.FACING, Direction.UP));
            arcon(c, 0, B - 2, 0, botin);
            pon(c, 0, 0, 2, b(Blocks.COBWEB));
        }

        /** La casa de maquinas del castillete: el tambor del cable (un rodillo de troncos) y el banco del maquinista. */
        private void maquinas(Canvas c) {
            int bc = B / 2;
            for (int a = 1; a <= A - 2; a++)
                for (int b = bc - 2; b <= bc + 2; b++)
                    for (int dy = 0; dy <= 4; dy++) {
                        double r = Math.hypot(b - bc, dy - 2);
                        if (r > 2.3) continue;
                        Direction.Axis eje = frente.getAxis() == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z;
                        pon(c, a, b, dy, a == 1 || a == A - 2 ? tronco(Blocks.DARK_OAK_LOG, eje) : tronco(Blocks.STRIPPED_SPRUCE_LOG, eje));
                    }
            for (int b = bc - 3; b <= bc + 3; b += 6) for (int dy = 0; dy <= 2; dy++) pon(c, 0, b, dy, b(Blocks.DARK_OAK_FENCE));
            pon(c, A - 1, 0, 0, b(Blocks.SMITHING_TABLE));
            pon(c, A - 1, B - 1, 0, b(Blocks.BLAST_FURNACE));
            arcon(c, 0, 0, 0, botin);
        }
    }

    private void caseta(Uso uso, int u0, int v0, int u1, int v1, Direction frente, int ruina, Casa.Estilo estilo, int tejado, Casa.Tipo tipo) {
        elementos.add(new Caseta(uso, u0, v0, u1, v1, frente, ruina, estilo, tejado, tipo));
    }

    /** Lo de arriba: el camino del santuario y la escalera de tablones de la corta (en zigzag por el suroeste). */
    private void campamento() {
        Casa.Estilo M = Casa.Estilo.MADERA, P = Casa.Estilo.PIEDRA;
        caseta(Uso.OFICINA, -27, 2, -17, 10, Direction.EAST, 0, M, 1, Casa.Tipo.CAPITANIA);
        caseta(Uso.LAMPISTERIA, 15, 2, 25, 10, Direction.WEST, 0, M, 0, Casa.Tipo.CAPITANIA);
        caseta(Uso.FRAGUA, -31, 16, -23, 24, Direction.EAST, 1, M, 2, Casa.Tipo.HERRERIA);
        caseta(Uso.BARRACON, 14, 16, 30, 26, Direction.WEST, 2, M, 0, Casa.Tipo.ALMACEN);
        caseta(Uso.MAQUINAS, 3, -92, 11, -82, Direction.SOUTH, 0, P, 2, Casa.Tipo.CAPITANIA);
        // la valla del borde de la corta (con huecos; abierta donde empieza la escalera)
        for (int u = -36; u <= 36; u++)
            for (int v = -60; v <= 0; v++) {
                if (nivelCorta(u, v) != 0) continue;
                if (nivelCorta(u + 1, v) + nivelCorta(u - 1, v) + nivelCorta(u, v + 1) + nivelCorta(u, v - 1) == 0) continue;
                if (u >= -17 && u <= -3 && v >= -10) continue;
                if (u >= -3 && u <= 3 && v <= -48) continue;
                int p = pct(u, SUELO, v, 31);
                if (p < 30) continue;
                une(u, PISO, v, b(Blocks.SPRUCE_FENCE));
                if (p > 94) put(u, PISO + 1, v, farol(false, false));
            }
        // los railes de la escombrera: del castillete, por el este de la corta, hasta el monton (y la vagoneta al final)
        for (int v = -79; v >= -80; v--) put(31, PISO, v, b(Blocks.AIR));
        for (int v = -78; v <= -26; v++) {
            if (pct(31, PISO, v, 32) < 6) continue;
            put(31, PISO, v, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.NORTH_SOUTH));
            rieles++;
        }
        vagoneta(31, PISO, -25);
        // farolas en el camino del santuario y junto a las casetas
        for (int[] q : new int[][]{{-4, 30}, {3, 18}, {-4, 6}, {3, -4}, {14, -76}, {26, -40}}) {
            une(q[0], PISO, q[1], b(Blocks.SPRUCE_FENCE));
            une(q[0], PISO + 1, q[1], b(Blocks.SPRUCE_FENCE));
            put(q[0], PISO + 2, q[1], farol(true, false));
        }
        // el camino del santuario a la corta
        for (int v = SANT_V - 5; v >= -6; v--)
            for (int u = -2; u <= 1; u++) {
                int p = pct(u, SUELO, v, 30);
                put(u, SUELO, v, p < 45 ? b(Blocks.COBBLESTONE) : p < 75 ? b(Blocks.GRAVEL) : b(Blocks.COARSE_DIRT));
                put(u, SUELO + 1, v, Pal.AIR);
            }
        // la escalera de la corta: tres tramos de seis escalones pegados a la pared (del borde al primer escalon, de este al
        // segundo y del segundo al fondo), sobre postes
        tramoEscalera(-14, -8, BANCO1 + 6, true);   // del borde (v = -8) baja hacia el este
        tramoEscalera(-6, -12, BANCO2 + 6, false);  // del primer escalon baja hacia el oeste
        tramoEscalera(-14, -16, FONDO + 6, true);   // del segundo, al fondo, hacia el este
        // y un poco de tablones donde se baja de un tramo y se sube al siguiente
        for (int[] q : new int[][]{{-14, -8}, {-6, -12}, {-14, -16}}) put(q[0], altoCorta(q[1] == -8 ? 0 : q[1] == -12 ? 1 : 2), q[1],
                b(Blocks.SPRUCE_PLANKS));
    }

    /**
     * Un tramo de la escalera de tablones: empieza en (u0, v) a la altura del escalon de arriba (y0: el bloque que se pisa) y
     * baja seis escalones hacia el este (o el oeste), dos de ancho hacia el norte de la pared, con sus postes y su baranda.
     */
    private void tramoEscalera(int u0, int vPared, int y0, boolean alEste) {
        int du = alEste ? 1 : -1;
        Direction arriba = alEste ? Direction.WEST : Direction.EAST;
        for (int i = 0; i < 6; i++) {
            int u = u0 + du * i, y = y0 - i;
            for (int dv = 1; dv <= 2; dv++) {
                int v = vPared - dv;
                put(u, y, v, escalon(Blocks.SPRUCE_STAIRS, arriba));
                for (int yy = y + 1; yy <= y + 3; yy++) if (!macizo(u, yy, v)) put(u, yy, v, Pal.AIR);
                if (i % 2 == 1 && dv == 2) for (int yy = y - 1; yy > y0 - 6; yy--) une(u, yy, v, b(Blocks.SPRUCE_FENCE));
            }
        }
    }

    /** Los adornos de dentro: el hundimiento de la Galeria del Rey, vagonetas, barriles, el lago y el filon. */
    private void adornos() {
        // el hundimiento: piedra y vigas al fondo de la Galeria del Rey
        for (int v = -91; v >= -98; v--)
            for (int u = -2; u <= 2; u++) {
                int h = Math.min(5, 1 + (-91 - v) + pct(u, N1, v, 40) % 2);
                for (int y = N1 + 1; y <= N1 + h; y++) {
                    int p = pct(u, y, v, 41);
                    put(u, y, v, p < 35 ? b(Blocks.COBBLESTONE) : p < 60 ? b(Blocks.GRAVEL) : p < 78 ? b(Blocks.ANDESITE) : p < 90 ? b(Blocks.TUFF)
                            : tronco(Blocks.SPRUCE_LOG, Direction.Axis.Z));
                }
            }
        // vagonetas (cajones de madera llenos de mineral) al final de los railes
        vagoneta(0, N1 + 1, -88);
        vagoneta(1, N1 + 1, -46);
        vagoneta(-24, N1 + 1, -61);
        vagoneta(36, N2 + 1, -95);
        vagoneta(30, N1 + 1, -95);
        // la pasarela del lago: del oeste al islote y del islote al este (rota en medio)
        pasarela(-18, -4, -147);
        pasarela(2, 18, -153);
        islote(-2, -150);
        // estalactitas en las cuevas
        estalactitas(0, LAGO_V, LAGO_A, LAGO_B, AGUA + 1);
        estalactitas(0, FILON_V, FILON_A, FILON_B, N3 + 1);
    }

    private void vagoneta(int u, int y, int v) {
        put(u, y, v, Blocks.COMPOSTER.defaultBlockState().setValue(ComposterBlock.LEVEL, 7));
        vagonetas++;
    }

    /** Un trozo de pasarela de tablones sobre el lago (de u0 a u1, en v, tres de ancho), con postes, baranda y algun farol. */
    private void pasarela(int u0, int u1, int v) {
        for (int u = u0; u <= u1; u++) {
            // un trozo hundido (tablas medio caidas: se pasa, con cuidado)
            boolean rota = u == 9 || u == 10;
            for (int dv = -1; dv <= 1; dv++) {
                if (rota && dv == 1) continue;
                put(u, N2, v + dv, rota ? losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM)
                        : pct(u, N2, v + dv, 42) < 85 ? b(Blocks.SPRUCE_PLANKS) : losa(Blocks.SPRUCE_SLAB, SlabType.TOP));
                for (int y = N2 + 1; y <= N2 + 3; y++) if (!macizo(u, y, v + dv)) aire(u, y, v + dv);
            }
            if (rota) continue;
            if ((u - u0) % 3 == 0)
                for (int dv = -1; dv <= 1; dv += 2) for (int y = N2 - 1; y >= AGUA - 6; y--) {
                    if (macizo(u, y, v + dv)) break;
                    put(u, y, v + dv, tronco(Blocks.SPRUCE_LOG, Direction.Axis.Y));
                }
            if ((u - u0) % 6 == 3) {
                une(u, N2 + 1, v + 1, b(Blocks.SPRUCE_FENCE));
                put(u, N2 + 2, v + 1, farol(true, false));
                faroles++;
            } else if (pct(u, N2, v, 43) < 60) une(u, N2 + 1, v + 1, b(Blocks.SPRUCE_FENCE));
        }
    }

    private void islote(int u0, int v0) {
        for (int u = u0 - 4; u <= u0 + 4; u++)
            for (int v = v0 - 4; v <= v0 + 4; v++) {
                double r = Math.hypot(u - u0, v - v0) + ruido(u * 2, v * 2) * 0.5;
                if (r > 3.6) continue;
                int top = N2 + (r < 1.5 ? 1 : 0);
                for (int y = AGUA - 6; y <= top; y++) {
                    if (y < AGUA - 6) continue;
                    put(u, y, v, pared(x(u), y, z(v)));
                }
                for (int y = top + 1; y <= top + 3; y++) if (!macizo(u, y, v)) aire(u, y, v);
            }
    }

    /** Estalactitas de goteo colgando del techo de una cueva (y alguna estalagmita) y liquen que luce. */
    private void estalactitas(int uc, int vc, double a, double bb, int sueloMin) {
        for (int u = (int) (uc - a); u <= uc + a; u++)
            for (int v = (int) (vc - bb); v <= vc + bb; v++) {
                int p = pct(u, 0, v, 44);
                if (p >= 7) continue;
                // el techo: el primer hueco de arriba
                int y = sueloMin + 30;
                while (y > sueloMin + 3 && !hueco(u, y, v)) y--;
                if (y <= sueloMin + 3 || hueco(u, y + 1, v)) continue;
                int largo = 1 + p % 3;
                boolean ok = true;
                for (int k = 0; k < largo; k++) if (!hueco(u, y - k, v)) ok = false;
                if (!ok) continue;
                for (int k = 0; k < largo; k++) {
                    DripstoneThickness g = k == largo - 1 ? DripstoneThickness.TIP : k == 0 && largo > 2 ? DripstoneThickness.BASE : DripstoneThickness.FRUSTUM;
                    put(u, y - k, v, Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN)
                            .setValue(PointedDripstoneBlock.THICKNESS, g));
                }
                estalactitas++;
                if (p < 2) put(u, y - largo - 0, v, Pal.AIR);
            }
    }

    // ------------------------------------------------------------------ cofres, atril y muro ilusorio

    private void trozos() {
        // el atril de la oficina del capataz (en el campamento), con el libro de turnos
        trozo(-20, PISO, 3, c -> c.lectern(x(-20), PISO, z(3), Direction.WEST, Perdidos.libro(LIBRO)));
        trozo(41, N2 + 1, -126, c -> c.barrel(x(41), N2 + 1, z(-126), Direction.UP, BOTIN));
        trozo(48, N2 + 1, -126, c -> c.chest(x(48), N2 + 1, z(-126), Direction.WEST, BOTIN));
        trozo(-36, N1 + 1, -67, c -> c.barrel(x(-36), N1 + 1, z(-67), Direction.UP, BOTIN));
        trozo(34, N1 + 1, -69, c -> c.chest(x(34), N1 + 1, z(-69), Direction.WEST, BOTIN));
        trozo(18, N2 + 1, -106, c -> c.chest(x(18), N2 + 1, z(-106), Direction.EAST, BOTIN_BUENO));
        trozo(-15, N3 + 1, -196, c -> c.chest(x(-15), N3 + 1, z(-196), Direction.EAST, BOTIN));
    }

    private void trozo(int u, int y, int v, Trozo.Obra o) {
        elementos.add(new Trozo(6, x(u), y, z(v), x(u), y, z(v), o));
    }

    /** Las celdas de un muro ilusorio de la mina (el de la capilla: la pared del oeste, detras del altar). */
    public static List<BlockPos> muro(String id) {
        List<BlockPos> l = new ArrayList<>();
        if (MURO_CAPILLA.equals(id))
            for (int v = -107; v <= -105; v++)
                for (int y = N2 + 1; y <= N2 + 3; y++) l.add(new BlockPos(x(24), y, z(v)));
        return l;
    }

    /** Lo que hay en una celda del muro ilusorio cuando esta cerrado. */
    public static BlockState bloqueMuro(String id, BlockPos p) {
        return pared(p.getX(), p.getY(), p.getZ());
    }

    // ------------------------------------------------------------------ los sitios y los de alli

    private void ponerSitios() {
        sitio("entrada", SANT_U, PISO, SANT_V - 6);
        sitio("corta", -14, PISO, -6);
        sitio("banco1", -4, BANCO1 + 1, -10);
        sitio("banco2", -16, BANCO2 + 1, -14);
        sitio("fondo", 0, FONDO + 1, -30);
        sitio("bocamina", 0, N1 + 1, -45);
        sitio("galeria", 0, N1 + 1, -62);
        sitio("polvorin", -33, N1 + 1, -62);
        sitio("comedor", 29, N1 + 1, -62);
        sitio("enganche1", 14, N1 + 1, -84);
        sitio("hundimiento", 0, N1 + 1, -88);
        sitio("rampa", -30, N1 + 1, -100);
        sitio("pie_rampa", -30, N2 + 1, -146);
        sitio("lago", -10, N2 + 1, -147);
        sitio("islote", -2, N2 + 2, -150);
        sitio("lago_este", 16, N2 + 1, -153);
        sitio("galeria_este", 36, N2 + 1, -120);
        sitio("literas", 44, N2 + 1, -120);
        sitio("capilla", 29, N2 + 1, -106);
        sitio("escondido", 20, N2 + 1, -106);
        sitio("enganche2", 24, N2 + 1, -85);
        sitio("pozo_veta", 0, N2 + 1, -170);
        sitio("filon", 0, N3 + 1, -190);
        sitio("capataz", 0, N3 + 1, -204);
        sitio("castillete", 19, PISO, -80);
        sitio("escotilla", ESCALA_U, PISO, ESCALA_V);
        sitio("alto", 30, PISO + 40, 60);
    }

    private static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;

    private void ponerEsperas() {
        int P = com.darkcraft.enemigos.Enterrado.PICADOR, B = com.darkcraft.enemigos.Enterrado.BARRENERO, C = com.darkcraft.enemigos.Enterrado.CARGADOR;
        int R = Enemigo.REZA, D = Enemigo.DE_PIE, T = Enemigo.TUMBADO;
        // ---- el primero (puesto 0): Hakon, el Capataz, picando el corazon de la veta en la Caverna del Filon (parte 7ar)
        en(0, 0, N3 + 1, -204, NORTE, R, 0, Puestos.JEFE);
        // ---- la corta: picando en los escalones, un barrenero arriba, el de la bocamina y uno enterrado en el fondo
        en(P, 6, BANCO1 + 1, -51, NORTE, R, 0, "corta");
        en(P, -22, BANCO2 + 1, -30, OESTE, R, 0, "corta");
        en(B, 25, BANCO1 + 1, -28, OESTE, D, 0, "corta");
        en(B, -2, BANCO2 + 1, -14, OESTE, R, 0, "corta");
        en(C, -6, FONDO + 1, -38, SUR, T, 0, "corta");
        en(P, 3, FONDO + 1, -41, SUR, D, 3, "corta");
        // ---- nivel 1: la Galeria del Rey, el polvorin, el comedor, las traviesas, el frente del este y el hundimiento
        en(P, -2, N1 + 1, -55, OESTE, R, 0, "galeria");
        en(P, 2, N1 + 1, -70, ESTE, R, 0, "galeria");
        en(B, -31, N1 + 1, -58, NORTE, R, 0, "polvorin");
        en(C, 30, N1 + 1, -56, SUR, R, 0, "comedor");
        en(P, 24, N1 + 1, -62, OESTE, D, 4, "comedor");
        en(C, 12, N1 + 1, -84, OESTE, D, 0, "traviesa");
        en(B, -20, N1 + 1, -84, ESTE, D, 2, "traviesa");
        en(P, -32, N1 + 1, -95, OESTE, R, 0, "traviesa");
        en(P, 28, N1 + 1, -97, NORTE, R, 0, "frente");
        en(P, 32, N1 + 1, -97, NORTE, R, 0, "frente");
        en(C, 0, N1 + 1, -89, SUR, T, 0, "hundimiento");
        // ---- nivel 2: el pie de la rampa, la pasarela y el islote, la galeria del este, las literas, la capilla y el enganche
        en(B, -30, N2 + 1, -147, SUR, D, 0, "rampa");
        en(P, -24, N2 + 1, -147, ESTE, D, 2, "lago");
        en(P, -12, N2 + 1, -147, OESTE, D, 0, "lago");
        en(B, -2, N2 + 2, -150, OESTE, D, 0, "lago");
        en(P, 12, N2 + 1, -153, OESTE, D, 0, "lago");
        en(C, 19, N2 + 1, -151, OESTE, R, 0, "lago");
        en(P, 27, N2 + 1, -152, NORTE, R, 0, "este");
        en(P, 44, N2 + 1, -115, SUR, D, 3, "literas");
        en(B, 41, N2 + 1, -121, ESTE, R, 0, "literas");
        en(C, 30, N2 + 1, -106, OESTE, R, 0, "capilla");
        en(P, 36, N2 + 1, -98, NORTE, D, 4, "este");
        en(B, 25, N2 + 1, -84, ESTE, D, 0, "enganche");
        // ---- nivel 3: el que guarda el paso a la Caverna del Filon
        en(C, 2, N3 + 1, -182, SUR, D, 0, "veta");
    }

    private void en(int variante, int u, int y, int v, float giro, int postura, int ronda, String grupo) {
        esperas.add(new Espera(variante, x(u), y, z(v), giro, postura, ronda, grupo));
    }

    // ------------------------------------------------------------------ Hakon, el Capataz (parte 7ar)

    /** Si (x, y, z) esta en la Caverna del Filon (donde pelea Hakon). */
    public static boolean enFilon(double x, double y, double z) {
        double u = x - CX, v = z - CZ;
        return y > N3 - 2 && y < N3 + 22 && Math.hypot(u / FILON_A, (v - FILON_V) / FILON_B) < 1.06;
    }

    /** Los escombros de los bordes de la caverna de los que salen los del turno cuando Hakon toca el silbato: {x, z}. */
    public static List<int[]> turno() {
        int[][] uv = {{-14, -190}, {14, -190}, {-12, -200}, {12, -199}, {-8, -184}, {8, -184}};
        List<int[]> l = new ArrayList<>();
        for (int[] q : uv) l.add(new int[]{CX + q[0], CZ + q[1]});
        return l;
    }

    /** Si Hakon ha caido (y no ha vuelto: es del grupo de los jefes). */
    public static boolean enPaz(net.minecraft.server.MinecraftServer srv) {
        return Puestos.muerto(srv, MorvainEnemigos.MINA, MorvainEnemigos.capataz());
    }

    /** Para las pruebas: los enterrados que han descansado al caer Hakon. */
    public static int DESCANSAN;

    /**
     * Cada segundo: con Hakon caido, el turno de noche descansa: los enterrados de la mina que no estan peleando se deshacen (sube su
     * lampara) y ya no vuelven ({@code MorvainEnemigos}: sus puestos dejan de estar en uso).
     */
    public static void paz(ServerLevel m) {
        if (Puestos.sitio(MorvainEnemigos.MINA) == null || !enPaz(m.getServer())) return;
        List<Puestos.Puesto> ps = MorvainEnemigos.mina();
        for (int i = 0; i < ps.size(); i++) {
            if (Puestos.JEFE.equals(ps.get(i).grupo())) continue;
            Enemigo e = Puestos.enemigo(MorvainEnemigos.MINA, i);
            if (e == null || e.level() != m || e.getTarget() != null || e.accion() != 0) continue;
            m.sendParticles(ParticleTypes.END_ROD, e.getX(), e.getY() + 1.2, e.getZ(), 0, 0, 1, 0, 0.08);
            m.sendParticles(ParticleTypes.SOUL, e.getX(), e.getY() + 1.2, e.getZ(), 8, 0.3, 0.6, 0.3, 0.03);
            e.disolver();
            DESCANSAN++;
        }
    }

    // ------------------------------------------------------------------ lo que pasa: los derrumbes

    /** Lo que avisa un techo roto antes de caer, lo que tarda en poder volver a caer, su radio y lo que hace. */
    static final int AVISO = 22, ESPERA_DERRUMBE = 20 * 30;
    static final double R_DERRUMBE = 1.9;
    static final float DANO_DERRUMBE = 8f;
    private static final Map<Integer, Long> CAE = new HashMap<>(), HASTA = new HashMap<>();
    /** Para las pruebas: techos que han caido y a quien han pillado (y de ellos, enterrados). */
    public static int DERRUMBES, DERRUMBE_DAN, DERRUMBE_ENTERRADOS;

    public List<int[]> derrumbes() {
        return derrumbes;
    }

    /** Algo revienta o golpea fuerte el suelo en la mina (un cartucho, un almadenazo): los techos rotos de cerca se vienen abajo. */
    public static void temblor(ServerLevel m, double x, double y, double z) {
        if (m.dimension() != Morvain.NIVEL || Math.abs(x - CX) > 80 || z > Z1 || z < Z0) return;
        Mina a = get();
        long t = m.getGameTime();
        for (int i = 0; i < a.derrumbes.size(); i++) {
            int[] d = a.derrumbes.get(i);
            if (y > d[1] + 1 || y < d[1] - 10) continue;
            if (Math.hypot(d[0] + 0.5 - x, d[2] + 0.5 - z) > 5.5) continue;
            avisar(m, i, d, t);
        }
    }

    private static void avisar(ServerLevel m, int i, int[] d, long t) {
        if (CAE.containsKey(i) || HASTA.getOrDefault(i, Long.MIN_VALUE / 2) > t) return;
        CAE.put(i, t + AVISO);
        m.playSound(null, d[0] + 0.5, d[1] - 1, d[2] + 0.5, DarkCraftMod.SFX_MINA_CRUJE.get(), SoundSource.BLOCKS, 1.4f, 0.9f + m.random.nextFloat() * 0.2f);
        m.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.GRAVEL.defaultBlockState()), d[0] + 0.5, d[1] - 0.6, d[2] + 0.5,
                24, 1.0, 0.1, 1.0, 0.0);
    }

    /** Cada tick con alguien cerca (lo llama {@link Morvain#onTick}): quien pasa bajo un techo roto lo hace caer; el polvo de aviso. */
    public static void tick(ServerLevel m) {
        Mina a = get();
        long t = m.getGameTime();
        for (ServerPlayer p : m.players()) {
            if (p.isSpectator() || p.isCreative()) continue;
            for (int i = 0; i < a.derrumbes.size(); i++) {
                int[] d = a.derrumbes.get(i);
                if (p.getY() > d[1] || p.getY() < d[1] - 7) continue;
                if (Math.hypot(d[0] + 0.5 - p.getX(), d[2] + 0.5 - p.getZ()) < 1.6) avisar(m, i, d, t);
            }
        }
        if (!CAE.isEmpty()) {
            var it = CAE.entrySet().iterator();
            while (it.hasNext()) {
                var e = it.next();
                if (e.getValue() > t) continue;
                it.remove();
                HASTA.put(e.getKey(), t + ESPERA_DERRUMBE);
                caer(m, a.derrumbes.get(e.getKey()));
            }
        }
        // la pista: de los techos rotos cae polvo de vez en cuando
        if (t % 50 == 0)
            for (int[] d : a.derrumbes) {
                boolean cerca = false;
                for (ServerPlayer p : m.players()) if (p.distanceToSqr(d[0] + 0.5, d[1], d[2] + 0.5) < 28 * 28) cerca = true;
                if (cerca) m.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.GRAVEL.defaultBlockState()), d[0] + 0.5, d[1] - 0.6,
                        d[2] + 0.5, 4, 0.8, 0.05, 0.8, 0.0);
            }
    }

    /** El techo se viene abajo: piedras, polvo, el estruendo y dano a quien este debajo (tambien a los enterrados). */
    private static void caer(ServerLevel m, int[] d) {
        DERRUMBES++;
        double x = d[0] + 0.5, z = d[2] + 0.5, y = d[1] - 0.7;
        m.playSound(null, x, y, z, DarkCraftMod.SFX_MINA_DERRUMBE.get(), SoundSource.BLOCKS, 2.0f, 0.9f + m.random.nextFloat() * 0.15f);
        m.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBBLED_DEEPSLATE.defaultBlockState()), x, y, z, 60, 1.1, 0.2, 1.1, 0.05);
        m.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GRAVEL.defaultBlockState()), x, y, z, 50, 1.2, 0.2, 1.2, 0.05);
        m.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.GRAVEL.defaultBlockState()), x, y, z, 40, 1.2, 0.3, 1.2, 0.0);
        m.sendParticles(ParticleTypes.LARGE_SMOKE, x, y - 4, z, 14, 1.2, 0.5, 1.2, 0.02);
        for (net.minecraft.world.entity.LivingEntity le : m.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                new net.minecraft.world.phys.AABB(x - R_DERRUMBE, d[1] - 8, z - R_DERRUMBE, x + R_DERRUMBE, d[1], z + R_DERRUMBE),
                o -> o.isAlive() && !(o instanceof net.minecraft.world.entity.player.Player pl && (pl.isCreative() || pl.isSpectator())))) {
            if (Math.hypot(le.getX() - x, le.getZ() - z) > R_DERRUMBE) continue;
            if (!le.hurt(m.damageSources().fallingBlock(null), DANO_DERRUMBE)) continue;
            DERRUMBE_DAN++;
            if (le instanceof com.darkcraft.enemigos.Enterrado) DERRUMBE_ENTERRADOS++;
            le.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
        }
    }

    /** Para las pruebas y el comando: que todos los techos puedan volver a caer ya. */
    public static void rearmar() {
        CAE.clear();
        HASTA.clear();
    }

    /** Para las pruebas (el mundo no avanza mientras corren): los techos avisados que van a caer, y hacerlos caer ya. */
    public static int avisados() {
        return CAE.size();
    }

    public static void caerYa(ServerLevel m) {
        Mina a = get();
        for (Integer i : new ArrayList<>(CAE.keySet())) {
            CAE.remove(i);
            HASTA.put(i, m.getGameTime() + ESPERA_DERRUMBE);
            caer(m, a.derrumbes.get(i));
        }
    }

    /** Quien toca la escotilla del castillete por arriba: esta cerrada por debajo (la palanca esta dentro del pozo). */
    public static boolean escotilla(BlockPos q) {
        return q.getX() == x(ESCALA_U) && q.getY() == SUELO && q.getZ() == z(ESCALA_V);
    }

    /** Abre o cierra la escotilla (la palanca de debajo y, con ella, la trampilla de hierro): para el comando y las pruebas. */
    public static void escotilla(ServerLevel m, boolean abrir) {
        BlockPos pa = new BlockPos(x(ESCALA_U + 1), SUELO - 1, z(ESCALA_V)), tr = new BlockPos(x(ESCALA_U), SUELO, z(ESCALA_V));
        var st = m.getBlockState(pa);
        if (st.is(Blocks.LEVER) && st.getValue(LeverBlock.POWERED) != abrir) {
            m.setBlock(pa, st.setValue(LeverBlock.POWERED, abrir), 3);
            m.updateNeighborsAt(pa, Blocks.LEVER);
            m.updateNeighborsAt(pa.above(), Blocks.LEVER);
        }
        var t = m.getBlockState(tr);
        if (t.is(Blocks.IRON_TRAPDOOR) && t.getValue(TrapDoorBlock.OPEN) != abrir)
            m.setBlock(tr, t.setValue(TrapDoorBlock.OPEN, abrir).setValue(TrapDoorBlock.POWERED, abrir), 3);
    }

    /** Si la escotilla esta abierta (en un mundo ya generado). */
    public static boolean escotillaAbierta(ServerLevel m) {
        BlockPos q = new BlockPos(x(ESCALA_U), SUELO, z(ESCALA_V));
        var st = m.getBlockState(q);
        return st.is(Blocks.IRON_TRAPDOOR) && st.getValue(TrapDoorBlock.OPEN);
    }

    /** Lo que hay en la mina ya generada (para la prueba): la escotilla, la palanca, el atril con su libro, los arcones, el muro... */
    public static String resumen(ServerLevel m) {
        Mina a = get();
        int[] at = new int[]{x(-20), PISO, z(3)};
        boolean libro = m.getBlockEntity(new BlockPos(at[0], at[1], at[2])) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le
                && Perdidos.de(le.getBook()) == LIBRO;
        var tr = m.getBlockState(new BlockPos(x(ESCALA_U), SUELO, z(ESCALA_V)));
        var pa = m.getBlockState(new BlockPos(x(ESCALA_U + 1), SUELO - 1, z(ESCALA_V)));
        int escala = 0;
        for (int y = N2 + 1; y <= SUELO - 1; y++) if (m.getBlockState(new BlockPos(x(ESCALA_U), y, z(ESCALA_V))).is(Blocks.LADDER)) escala++;
        int muro = 0;
        for (BlockPos q : muro(MURO_CAPILLA)) if (!m.getBlockState(q).isAir()) muro++;
        boolean bueno = m.getBlockState(new BlockPos(x(18), N2 + 1, z(-106))).is(Blocks.CHEST);
        int agua = 0;
        for (int u = -6; u <= 6; u++) if (m.getBlockState(new BlockPos(x(u), AGUA, z(LAGO_V))).is(Blocks.WATER)) agua++;
        return "escotilla " + (tr.is(Blocks.IRON_TRAPDOOR) ? (tr.getValue(TrapDoorBlock.OPEN) ? "abierta" : "cerrada") : "NO") + ", palanca "
                + (pa.is(Blocks.LEVER) ? "SI" : "NO") + ", escala " + escala + "/" + (SUELO - 1 - N2) + ", atril con el libro " + LIBRO + " "
                + (libro ? "SI" : "NO") + ", arcon bueno " + (bueno ? "SI" : "NO") + ", muro de la capilla " + muro + "/" + muro(MURO_CAPILLA).size()
                + ", agua del lago " + agua + "/13, techos rotos " + a.derrumbes.size();
    }

    /** El nombre del sitio de la mina donde esta (x, y, z) (null: fuera). */
    public static String lugar(double x, double y, double z) {
        if (!dentro(x, y, z)) return null;
        int v = (int) Math.floor(z) - CZ;
        if (y >= FONDO - 1 && nivelCorta((int) Math.floor(x) - CX, v) > 0) return "darkcraft.morvain.lugar.corta";
        if (y < N3 + 22 && v < -176) return "darkcraft.morvain.lugar.filon";
        if (y < N2 + 14 && Math.hypot(((int) Math.floor(x) - CX) / LAGO_A, (v - LAGO_V) / LAGO_B) < 1.05) return "darkcraft.morvain.lugar.lago";
        return "darkcraft.morvain.lugar.minas";
    }

    /** Si (x, y, z) esta dentro de la mina (en la corta o bajo tierra en sus galerias): para el nombre del sitio. */
    public static boolean dentro(double x, double y, double z) {
        int u = (int) Math.floor(x) - CX, v = (int) Math.floor(z) - CZ;
        if (nivelCorta(u, v) > 0 && y < SUELO + 4) return true;
        return y < SUELO - 3 && u >= X0 - CX && u <= X1 - CX && v >= Z0 - CZ && v < -40;
    }
}
