package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Ensamblado;
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
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BellAttachType;
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
 * El Osario de San Ulfo (parte 7as; respuesta 131 del plan: "criptas de huesos"), en el Bosque Muerto. Ochenta anos antes del
 * cielo rojo la Peste Gris lleno el camposanto de la capilla de San Ulfo; Fray Osmund, el sacristan, desenterro a los cuarenta mil
 * y ordeno sus huesos en la cripta, bajo la capilla, para hacer sitio a los que seguian muriendo. La noche del cielo rojo los
 * huesos se levantaron, pero mezclados: los Ensamblados ({@link Ensamblado}), la calavera de uno y los brazos de otros.
 * u = x - {@link #CX}, v = z - {@link #CZ}; el norte (v negativa) es donde baja la cripta.
 * <ul>
 * <li>Arriba, en un otero entre arboles muertos: el <b>Camposanto de San Ulfo</b> (su tapia rota, la puerta de las calaveras, las
 * tumbas abiertas y, al noroeste, el de los pobres: cruces de palo), la <b>capilla</b> (la nave sin tejado, los bancos rotos, el
 * altar, el abside con la cruz y la boca de la cripta detras del altar) y su <b>campanario</b> (la escala, la campana rota y, arriba,
 * un arcon). Fuera de la tapia, al noreste, el <b>panteon</b>: su escotilla de hierro solo se abre desde abajo (el atajo).</li>
 * <li>La <b>Cripta de los Hermanos</b> ({@link #C1}): el pasillo de los nichos de los frailes y sus cuatro capillas.</li>
 * <li>La <b>Capilla de los Huesos</b> ({@link #C2}): la boveda de huesos, la gran lampara de huesos en medio, las guirnaldas de
 * calaveras, las cuatro piramides de huesos de las esquinas, el altar de craneos con la cruz de huesos y el escudo de huesos.</li>
 * <li>Al oeste, las <b>catacumbas</b>: el <b>Taller del Sacristan</b> (el atril con el <b>libro perdido 37</b>), la cripta de los
 * priores (detras de un <b>muro ilusorio</b>: el arcon bueno) y el <b>osario menor</b> (los huesos sin ordenar).</li>
 * <li>Al este, el <b>Pozo de los Huesos</b> (doce bloques de huesos hacia abajo), la escala del panteon y, al norte, el pasillo que
 * baja al <b>Relicario</b> (donde espera Fray Osmund: parte 7at).</li>
 * </ul>
 * Se dibuja chunk a chunk al adornar; en su caja {@link Adorno} no pone nada (los arboles muertos de alrededor los pone el).
 */
public final class Osario {
    public static final long SEMILLA = 0x05A7_1005_A11FL;
    /** El centro del camposanto (la puerta de la capilla queda a v = -1). */
    public static final int CX = -3860, CZ = 4500;
    /** El otero allanado (lo que se pisa esta en {@link #PISO}). */
    public static final int SUELO = 98, PISO = SUELO + 1;
    /** Los suelos de abajo: la Cripta de los Hermanos, la Capilla de los Huesos (y las catacumbas), el fondo del Pozo y el Relicario. */
    public static final int C1 = SUELO - 10, C2 = SUELO - 20, POZO = SUELO - 32, RELICARIO = SUELO - 24;
    /** La Capilla de los Huesos: su interior (u -9..9, v de {@link #V2N} a {@link #V2S}) y la fila de sus dos salidas. */
    public static final int V2S = -61, V2N = -81, VX = -71;
    /** El pozo: su hueco (u y v). */
    static final int POZO_U0 = 23, POZO_U1 = 29, POZO_V0 = -72, POZO_V1 = -66;
    /** La escala del panteon (sube de la Sala del Pozo a su escotilla): contra la pared del sur, mirando al norte. */
    static final int ESCALA_U = 26, ESCALA_V = -57;
    /** El Relicario: su centro y su radio; donde espera Fray Osmund (parte 7at), de rodillas ante el relicario de San Ulfo. */
    public static final int REL_U = 26, REL_V = -110, OSMUND_U = 26, OSMUND_V = -105;
    public static final double REL_R = 11.3;
    /** Los montones de huesos del borde del Relicario (de ahi se levantan los muertos de Fray Osmund): grados y radio. */
    static final int[] MONTONES = {0, 45, 135, 180, 225, 270, 315};
    static final double R_MONTON = 10.2, R_MUERTO = 8.8;
    /** El santuario del Osario (en {@link SantuariosMorvain}): al sur de la puerta del camposanto. */
    public static final int SANT_U = 0, SANT_V = 42;
    public static final int X0 = CX - 50, X1 = CX + 52, Z0 = CZ - 124, Z1 = CZ + 60;
    /** Los chunks donde {@link Adorno} no pone nada (los arboles de alrededor los pone el plano): de -244 a -239 y de 277 a 284. */
    static final int AX0 = CX - 44, AX1 = CX + 51, AZ0 = CZ - 68, AZ1 = CZ + 59;

    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/osario"),
            BOTIN_BUENO = new ResourceLocation(DarkCraftMod.MODID, "chests/osario_bueno");
    /** El muro ilusorio del fondo del pasillo de los priores (en las catacumbas). */
    public static final String MURO_PRIORES = "osario_priores";
    /** El libro perdido que esta en el atril del Taller del Sacristan. */
    public static final int LIBRO = 37;
    /** El atril del libro. */
    public static final int ATRIL_U = -20, ATRIL_V = -97;

    /** Uno del osario: donde espera (y: donde tiene los pies), hacia donde mira, su variante, como espera, cuanto ronda y su grupo. */
    public record Espera(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    private static Osario unico;

    public static synchronized Osario get() {
        if (unico == null) unico = new Osario();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    private final Mina.Plano plano = new Mina.Plano();
    /** Lo que hay (para la prueba y las vistas). */
    public int tumbas, abiertas, cruces, nichos, calaveras, velas, arboles, bancos;

    private Osario() {
        elementos.add(new Obra());
        capilla();
        campanario();
        cavar();
        paredesHuesos();
        suelos();
        plano.forrar(Osario::forro);
        cripta();
        huesos();
        catacumbas();
        taller();
        priores();
        osarioMenor();
        salaPozo();
        relicario();
        camposanto();
        panteon();
        bosque();
        elementos.add(plano.pieza(4, X0, Z0, X1, Z1));
        trozos();
        ponerSitios();
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo del osario (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en lo de arriba del osario: ahi {@link Adorno} no pone nada. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= AX0 && x0 <= AX1 && z0 + 15 >= AZ0 && z0 <= AZ1;
    }

    /** Lo lejos de (x, z) al centro del camposanto. */
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} del osario en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los del osario (los apunta {@link MorvainEnemigos}). */
    public List<Espera> esperas() {
        return esperas;
    }

    /** Los sitios con nombre (la puerta, la capilla, la cripta, el taller...): {x, y, z} (y: los pies). */
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

    static BlockState vela(int n, boolean blanca) {
        return (blanca ? Blocks.WHITE_CANDLE : Blocks.CANDLE).defaultBlockState().setValue(CandleBlock.CANDLES, Math.max(1, Math.min(4, n)))
                .setValue(CandleBlock.LIT, true);
    }

    /** Una calavera suelta (giro: 0 mira al sur, 4 al oeste, 8 al norte, 12 al este). */
    static BlockState calavera(int giro) {
        return Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, Math.floorMod(giro, 16));
    }

    /** Una calavera colgada de una pared (mira hacia donde se dice; la pared queda detras). */
    static BlockState calaveraPared(Direction mira) {
        return Blocks.SKELETON_WALL_SKULL.defaultBlockState().setValue(WallSkullBlock.FACING, mira);
    }

    static int giro(Direction d) {
        return d.get2DDataValue() * 4;
    }

    static BlockState hueso(Direction.Axis a) {
        return Mina.tronco(Blocks.BONE_BLOCK, a);
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

    /** Pone algo solo si (u, y, v) es un hueco del plano (para no tapar paredes con lo de adorno). */
    private void enHueco(int u, int y, int v, BlockState s) {
        if (hueco(u, y, v)) put(u, y, v, s);
    }

    /** Un hueco de (u0..u1, v0..v1) de y0 a y1. */
    private void hueco(int u0, int v0, int u1, int v1, int y0, int y1) {
        for (int u = Math.min(u0, u1); u <= Math.max(u0, u1); u++)
            for (int v = Math.min(v0, v1); v <= Math.max(v0, v1); v++)
                for (int y = y0; y <= y1; y++) aire(u, y, v);
    }

    // ------------------------------------------------------------------ los materiales

    /** Los sillares de la capilla y de la tapia: piedra vieja con musgo. */
    static BlockState sillar(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 6);
        return p < 38 ? b(Blocks.STONE_BRICKS) : p < 58 ? b(Blocks.MOSSY_STONE_BRICKS) : p < 72 ? b(Blocks.CRACKED_STONE_BRICKS)
                : p < 82 ? b(Blocks.COBBLESTONE) : p < 92 ? b(Blocks.MOSSY_COBBLESTONE) : b(Blocks.ANDESITE);
    }

    /** La piedra de la cripta: sillares viejos, musgo, toba y pizarra (mas oscuro cuanto mas hondo). */
    static BlockState piedra(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 7);
        if (y < C2 + 8)
            return p < 24 ? b(Blocks.STONE_BRICKS) : p < 38 ? b(Blocks.CRACKED_STONE_BRICKS) : p < 48 ? b(Blocks.MOSSY_STONE_BRICKS)
                    : p < 60 ? b(Blocks.TUFF) : p < 70 ? b(Blocks.DEEPSLATE_BRICKS) : p < 79 ? b(Blocks.COBBLED_DEEPSLATE) : p < 87 ? b(Blocks.COBBLESTONE)
                    : p < 94 ? b(Blocks.ANDESITE) : b(Blocks.CRACKED_DEEPSLATE_BRICKS);
        return p < 32 ? b(Blocks.STONE_BRICKS) : p < 46 ? b(Blocks.MOSSY_STONE_BRICKS) : p < 58 ? b(Blocks.CRACKED_STONE_BRICKS)
                : p < 68 ? b(Blocks.COBBLESTONE) : p < 76 ? b(Blocks.MOSSY_COBBLESTONE) : p < 84 ? b(Blocks.TUFF) : p < 92 ? b(Blocks.ANDESITE)
                : b(Blocks.POLISHED_ANDESITE);
    }

    /** Huesos: bloques de hueso en todas direcciones, calcita y diorita (la Capilla de los Huesos, el pozo, el osario menor). */
    static BlockState huesos(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 8);
        Direction.Axis a = p % 3 == 0 ? Direction.Axis.Y : p % 3 == 1 ? Direction.Axis.X : Direction.Axis.Z;
        return p < 60 ? hueso(a) : p < 74 ? b(Blocks.CALCITE) : p < 86 ? b(Blocks.POLISHED_DIORITE) : p < 94 ? b(Blocks.DIORITE)
                : b(Blocks.SMOOTH_STONE);
    }

    /** La piedra del Relicario: pizarra pulida y labrada. */
    static BlockState reliquia(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 9);
        return p < 30 ? b(Blocks.POLISHED_DEEPSLATE) : p < 54 ? b(Blocks.DEEPSLATE_TILES) : p < 72 ? b(Blocks.DEEPSLATE_BRICKS)
                : p < 82 ? b(Blocks.CRACKED_DEEPSLATE_TILES) : p < 88 ? b(Blocks.CHISELED_DEEPSLATE) : b(Blocks.POLISHED_BLACKSTONE_BRICKS);
    }

    /** El suelo de la cripta: losas, sillares rotos y algo de tierra. */
    static BlockState losas(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 10);
        return p < 26 ? b(Blocks.STONE_BRICKS) : p < 42 ? b(Blocks.POLISHED_ANDESITE) : p < 56 ? b(Blocks.CRACKED_STONE_BRICKS)
                : p < 66 ? b(Blocks.MOSSY_STONE_BRICKS) : p < 76 ? b(Blocks.COBBLESTONE) : p < 84 ? b(Blocks.TUFF) : p < 92 ? b(Blocks.COARSE_DIRT)
                : b(Blocks.PACKED_MUD);
    }

    /** La roca de alrededor de los huecos (el forro): huesos en las salas de huesos, pizarra en el relicario y piedra en lo demas. */
    static BlockState forro(int x, int y, int z) {
        int u = x - CX, v = z - CZ;
        if (enHuesos(u, y, v) || enPozo(u, y, v) || enOsarioMenor(u, y, v)) return huesos(x, y, z);
        if (enRelicario(u, y, v) || u >= 23 && u <= 29 && v <= -85 && v >= -99 && y < C2) return reliquia(x, y, z);
        return piedra(x, y, z);
    }

    // ------------------------------------------------------------------ el otero

    /** Cuanto se allana (u, v) a {@link #SUELO}: 1 en el camposanto, el camino del santuario y el panteon; 0 lejos. */
    static double peso(int u, int v) {
        double d = Mina.caja(u, v + 1, 27, 30, 6);
        d = Math.min(d, Mina.caja(u, v - 38, 7, 11, 3));
        d = Math.min(d, Mina.caja(u - 26, v + 57, 6, 6, 2));
        d += Mina.ruido(u * 1.7, v * 1.7) * 1.1;
        return d <= 0 ? 1.0 : 1.0 - Ruido.suave(0, 8, d);
    }

    /** La altura natural del terreno (la de antes del osario). */
    static int alto(int x, int z) {
        return (int) Math.floor(Terreno.columna(x, z).altura());
    }

    /** Lo alto (el bloque de arriba) de la columna (u, v) con el otero ya allanado. */
    static int superficie(int u, int v) {
        int t = alto(x(u), z(v));
        double w = peso(u, v);
        return w <= 0 ? t : (int) Math.round(Ruido.lerp(w, t, SUELO));
    }

    private final class Obra extends Element {
        Obra() {
            super(SEMILLA + 51);
            this.layer = 1;
            bounds(AX0, POZO - 4, AZ0, AX1, 140, AZ1);
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
        double w = peso(u, v);
        if (w <= 0) return;
        int t = alto(x, z);
        int h = (int) Math.round(Ruido.lerp(w, t, SUELO));
        for (int y = h + 1; y <= Math.max(t, h) + 3; y++) c.set(x, y, z, Pal.AIR);
        for (int y = t + 1; y < h; y++) c.set(x, y, z, y < h - 3 ? b(Blocks.STONE) : b(Blocks.DIRT));
        c.set(x, h, z, manto(x, h, z));
    }

    /** Lo de arriba del bosque: podsol, tierra gruesa, musgo y algo de hierba (a manchas). */
    static BlockState manto(int x, int y, int z) {
        double n = Mina.ruido(x * 0.31, z * 0.29) + 0.35 * Mina.ruido(x * 0.9 + 7, z * 0.8 - 3) + (Hash.f(x, y, z, SEMILLA + 4) - 0.5) * 0.5;
        return n < -0.6 ? b(Blocks.MOSS_BLOCK) : n < -0.1 ? b(Blocks.PODZOL) : n < 0.3 ? b(Blocks.COARSE_DIRT) : n < 0.65 ? b(Blocks.GRASS_BLOCK)
                : b(Blocks.ROOTED_DIRT);
    }

    // ------------------------------------------------------------------ la capilla de San Ulfo (arriba)

    /** El abside: el medio circulo del norte de la nave (centro (0, -19), radio 4.5). */
    static boolean enAbside(int u, int v) {
        return v <= -19 && Math.hypot(u, v + 19) <= 4.5;
    }

    static boolean muroAbside(int u, int v) {
        double r = Math.hypot(u, v + 19);
        return v <= -19 && r > 4.5 && r <= 5.7;
    }

    /** Lo de dentro de la capilla (la nave, u -4..4, v -18..-2, y el abside). */
    static boolean enNave(int u, int v) {
        return Math.abs(u) <= 4 && v <= -2 && v >= -18 || enAbside(u, v);
    }

    static boolean muroNave(int u, int v) {
        if (muroAbside(u, v)) return true;
        if (v > -1 || v < -19) return false;
        return Math.abs(u) == 5 || v == -1 && Math.abs(u) <= 5;
    }

    /** Lo alto del muro de la capilla en (u, v) (bloques por encima del suelo): siete, menos donde se ha caido. */
    static int altoMuro(int u, int v) {
        // el muro del oeste se ha venido abajo a media nave y el abside, por el noreste
        double n = Mina.ruido(u * 0.5 + v * 0.13, v * 0.41) * 0.8 + (pct(u, 3, v, 21) - 50) / 70.0;
        if (u == -5 && v <= -7 && v >= -15) return Math.max(1, (int) Math.round(3 + n * 1.5 + Math.abs(v + 11) * 0.5));
        if (muroAbside(u, v) && u > 0) return Math.max(2, (int) Math.round(5 + n * 1.5));
        if (v == -1) return 7;
        return n > 0.75 ? 6 : 7;
    }

    private void capilla() {
        for (int u = -6; u <= 6; u++)
            for (int v = -25; v <= -1; v++) {
                boolean muro = muroNave(u, v), dentro = enNave(u, v);
                if (!muro && !dentro) continue;
                put(u, SUELO, v, dentro ? sueloCapilla(u, v) : sillar(x(u), SUELO, z(v)));
                if (!muro) continue;
                int alto = altoMuro(u, v);
                for (int k = 0; k < alto; k++) put(u, PISO + k, v, sillar(x(u), PISO + k, z(v)));
                // donde el muro se ha roto, cascotes al pie (dentro y fuera)
                if (alto < 6) {
                    for (int d = -1; d <= 1; d += 2) {
                        int uu = v == -1 ? u : u + d, vv = v == -1 ? v + d : v;
                        if (pct(uu, 0, vv, 22) < 45 && !muroNave(uu, vv))
                            put(uu, PISO, vv, pct(uu, 1, vv, 22) < 50 ? Mina.losa(Blocks.COBBLESTONE_SLAB, SlabType.BOTTOM) : b(Blocks.MOSSY_COBBLESTONE));
                    }
                }
            }
        // la puerta (u -1..1 de la fachada) con su arco y, encima, el hastial con el rosetón
        for (int u = -1; u <= 1; u++)
            for (int y = PISO; y <= PISO + 2; y++) put(u, y, -1, Pal.AIR);
        put(0, PISO + 3, -1, Pal.AIR);
        put(-1, PISO + 3, -1, Mina.escalonAlto(Blocks.STONE_BRICK_STAIRS, Direction.WEST));
        put(1, PISO + 3, -1, Mina.escalonAlto(Blocks.STONE_BRICK_STAIRS, Direction.EAST));
        put(0, PISO + 4, -1, b(Blocks.CHISELED_STONE_BRICKS));
        for (int y = PISO + 7; y <= PISO + 11; y++)
            for (int u = -4; u <= 4; u++) if (Math.abs(u) <= PISO + 11 - y) put(u, y, -1, sillar(x(u), y, z(-1)));
        put(0, PISO + 8, -1, Pal.AIR);
        une(-1, PISO + 8, -1, b(Blocks.IRON_BARS));
        une(1, PISO + 8, -1, b(Blocks.IRON_BARS));
        une(0, PISO + 7, -1, b(Blocks.IRON_BARS));
        une(0, PISO + 9, -1, b(Blocks.IRON_BARS));
        // las ventanas de la nave (de tres de alto; unas con barrotes, otras rotas) y la del abside, detras de la cruz
        for (int v : new int[]{-5, -9, -13, -17})
            for (int s = -1; s <= 1; s += 2) {
                if (altoMuro(5 * s, v) < 6) continue;
                for (int y = PISO + 2; y <= PISO + 4; y++)
                    if (pct(s, y, v, 23) < 45) une(5 * s, y, v, b(Blocks.IRON_BARS));
                    else put(5 * s, y, v, Pal.AIR);
            }
        for (int y = PISO + 2; y <= PISO + 5; y++) une(0, y, -24, b(Blocks.IRON_BARS));
        for (int y = PISO + 2; y <= PISO + 4; y++) {
            put(-4, y, -22, Pal.AIR);
            une(-4, y, -22, b(Blocks.IRON_BARS));
        }
        // los contrafuertes de fuera
        for (int v : new int[]{-3, -7, -11, -15, -19})
            for (int s = -1; s <= 1; s += 2) {
                if (s > 0 && v >= -7) continue;
                int u = 6 * s;
                put(u, SUELO, v, sillar(x(u), SUELO, z(v)));
                for (int y = PISO; y <= PISO + 3; y++) put(u, y, v, sillar(x(u), y, z(v)));
                put(u, PISO + 4, v, Mina.escalon(Blocks.STONE_BRICK_STAIRS, s > 0 ? Direction.WEST : Direction.EAST));
            }
        // lo que queda del tejado: tres vigas de lado a lado (una partida) y un trozo del faldon del este junto a la fachada
        for (int v : new int[]{-4, -10, -16})
            for (int u = -5; u <= 5; u++) {
                if (v == -10 && u > 0) continue;
                if (altoMuro(5 * Integer.signum(u == 0 ? 1 : u), v) < 7 && Math.abs(u) == 5) continue;
                put(u, PISO + 7, v, Mina.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
            }
        // la viga partida, caida en la nave
        for (int k = 0; k < 4; k++) put(1 + k, PISO, -10 - (k / 2), Mina.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        for (int v = -8; v <= -2; v++)
            for (int u = 1; u <= 6; u++) {
                if (pct(u, 4, v, 24) < 22) continue;
                int y = PISO + 7 + (5 - Math.min(u, 5));
                put(u, y, v, u == 6 ? Mina.escalonAlto(Blocks.DARK_OAK_STAIRS, Direction.EAST) : Mina.escalon(Blocks.DARK_OAK_STAIRS, Direction.WEST));
            }
        for (int v = -4; v <= -2; v++)
            for (int u = -5; u <= 0; u++) {
                if (pct(u, 5, v, 24) < 30) continue;
                int y = PISO + 7 + (5 - Math.min(-u, 5));
                put(u, y, v, u == 0 ? Mina.losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM) : Mina.escalon(Blocks.DARK_OAK_STAIRS, Direction.EAST));
            }
        // los bancos (de cara al altar: el respaldo al sur), muchos rotos o volcados
        for (int v = -4; v >= -14; v -= 2)
            for (int u = -4; u <= 4; u++) {
                if (Math.abs(u) < 2) continue;
                int p = pct(u, 6, v, 25);
                if (p < 22) continue;
                BlockState st = p < 34 ? Mina.losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM)
                        : p < 40 ? Mina.escalon(Blocks.SPRUCE_STAIRS, u < 0 ? Direction.WEST : Direction.EAST)
                        : Mina.escalon(Blocks.SPRUCE_STAIRS, Direction.SOUTH);
                put(u, PISO, v, st);
                bancos++;
            }
        // los cascotes del tejado caido (en medio de la nave)
        for (int v = -9; v >= -16; v--)
            for (int u = -4; u <= 4; u++) {
                if (macizo(u, PISO, v)) continue;
                int p = pct(u, 7, v, 26);
                if (p < 16) put(u, PISO, v, Mina.losa(Blocks.COBBLESTONE_SLAB, SlabType.BOTTOM));
                else if (p < 22) put(u, PISO, v, b(Blocks.MOSSY_COBBLESTONE));
                else if (p < 26) put(u, PISO, v, Mina.escalon(Blocks.STONE_BRICK_STAIRS, Direction.from2DDataValue(p % 4)));
            }
        // el altar (con su calavera y sus velas), la boca de la cripta detras (con su baranda) y la cruz del abside
        put(-1, PISO, -17, b(Blocks.POLISHED_ANDESITE));
        put(0, PISO, -17, b(Blocks.CHISELED_STONE_BRICKS));
        put(1, PISO, -17, b(Blocks.POLISHED_ANDESITE));
        put(-1, PISO + 1, -17, vela(3, false));
        put(1, PISO + 1, -17, vela(2, true));
        put(0, PISO + 1, -17, calavera(0));
        velas += 2;
        calaveras++;
        for (int v = -19; v >= -21; v--)
            for (int s = -2; s <= 2; s += 4) une(s, PISO, v, b(Blocks.IRON_BARS));
        for (int u = -2; u <= 2; u++) une(u, PISO, -22, b(Blocks.IRON_BARS));
        for (int y = PISO; y <= PISO + 5; y++) put(0, y, -23, Mina.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
        put(-1, PISO + 4, -23, Mina.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        put(1, PISO + 4, -23, Mina.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        put(-3, PISO, -20, vela(4, true));
        put(3, PISO, -20, vela(3, false));
        put(-2, PISO, -23, vela(2, false));
        velas += 3;
        // telaranas en lo alto de los rincones
        for (int[] q : new int[][]{{-4, -2}, {4, -2}, {-4, -18}, {4, -18}, {3, -22}})
            if (!macizo(q[0], PISO + 5, q[1])) put(q[0], PISO + 5, q[1], b(Blocks.COBWEB));
    }

    /** El suelo de la nave: sillares, losas y, en el pasillo del medio, las lapidas de los priores. */
    static BlockState sueloCapilla(int u, int v) {
        int p = pct(u, SUELO, v, 27);
        if (u == 0 && v % 3 == 0) return p < 50 ? b(Blocks.CHISELED_STONE_BRICKS) : b(Blocks.POLISHED_ANDESITE);
        if (Math.abs(u) <= 1) return p < 60 ? b(Blocks.POLISHED_ANDESITE) : p < 80 ? b(Blocks.STONE_BRICKS) : b(Blocks.CRACKED_STONE_BRICKS);
        return p < 34 ? b(Blocks.STONE_BRICKS) : p < 50 ? b(Blocks.CRACKED_STONE_BRICKS) : p < 64 ? b(Blocks.MOSSY_STONE_BRICKS)
                : p < 74 ? b(Blocks.COBBLESTONE) : p < 84 ? b(Blocks.MOSS_BLOCK) : p < 92 ? b(Blocks.COARSE_DIRT) : b(Blocks.POLISHED_ANDESITE);
    }

    /** El campanario (u 5..9, v -6..-2: su muro del oeste es el de la nave): la escala, la campana rota y, arriba, un arcon. */
    private void campanario() {
        int top = PISO + 16;
        for (int u = 5; u <= 9; u++)
            for (int v = -6; v <= -2; v++) {
                boolean borde = u == 5 || u == 9 || v == -6 || v == -2;
                put(u, SUELO, v, borde ? sillar(x(u), SUELO, z(v)) : b(Blocks.STONE_BRICKS));
                if (!borde) continue;
                for (int y = PISO; y <= top; y++) put(u, y, v, sillar(x(u), y, z(v)));
            }
        // la puerta desde la nave
        for (int y = PISO; y <= PISO + 2; y++) put(5, y, -4, Pal.AIR);
        // la escala (contra la pared del norte) hasta el suelo de la campana
        int piso2 = PISO + 12;
        for (int y = PISO; y <= piso2; y++) put(8, y, -5, Mina.escala(Direction.SOUTH));
        for (int u = 6; u <= 8; u++)
            for (int v = -5; v <= -3; v++) if (u != 8 || v != -5) put(u, piso2, v, b(Blocks.SPRUCE_PLANKS));
        // los vanos de la campana
        for (int y = piso2 + 1; y <= piso2 + 3; y++) {
            put(7, y, -6, Pal.AIR);
            put(7, y, -2, Pal.AIR);
            put(9, y, -4, Pal.AIR);
            put(5, y, -4, Pal.AIR);
        }
        // la viga y la campana (rota: le falta un trozo del borde, pero suena)
        for (int u = 6; u <= 8; u++) put(u, piso2 + 4, -4, Mina.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        put(7, piso2 + 3, -4, Blocks.BELL.defaultBlockState().setValue(BellBlock.FACING, Direction.NORTH).setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING));
        // el tejado: la losa, la aguja y la cruz de palo (con un agujero)
        for (int u = 5; u <= 9; u++)
            for (int v = -6; v <= -2; v++) if (u != 8 || v != -3) put(u, top + 1, v, b(Blocks.STONE_BRICKS));
        for (int u = 6; u <= 8; u++)
            for (int v = -5; v <= -3; v++) if (u != 8 || v != -3) put(u, top + 2, v, Mina.losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
        put(7, top + 2, -4, b(Blocks.CHISELED_STONE_BRICKS));
        une(7, top + 3, -4, b(Blocks.DARK_OAK_FENCE));
        une(7, top + 4, -4, b(Blocks.DARK_OAK_FENCE));
        une(6, top + 4, -4, b(Blocks.DARK_OAK_FENCE));
        une(8, top + 4, -4, b(Blocks.DARK_OAK_FENCE));
        une(7, top + 5, -4, b(Blocks.DARK_OAK_FENCE));
        put(6, piso2 + 1, -5, b(Blocks.COBWEB));
    }

    /** Donde esta la campana del campanario. */
    public static BlockPos campana() {
        return new BlockPos(x(7), PISO + 15, z(-4));
    }

    // ------------------------------------------------------------------ lo de abajo: los huecos

    /** Lo alto (el primer bloque macizo) de la boveda de la Capilla de los Huesos en la fila u. */
    static int techoHuesos(int u) {
        return C2 + 6 + (int) Math.round(5.5 * Math.sqrt(Math.max(0, 1 - (u / 10.5) * (u / 10.5))));
    }

    /** Lo alto de la cupula del Relicario a la distancia r del centro (el primer bloque macizo). */
    static int cupula(double r) {
        return RELICARIO + 5 + (int) Math.round(7 * Math.sqrt(Math.max(0, 1 - (r / 12.5) * (r / 12.5))));
    }

    /**
     * Una escalera recta (de u0 a u1) que baja hacia el norte: el primer peldano en v0, un bloque menos por fila hasta el suelo de
     * abajo (el ultimo peldano, al nivel de ese suelo); tres de aire encima de cada peldano.
     */
    private void bajada(int u0, int u1, int v0, int arriba, int abajo, Block peldano, long sem) {
        for (int k = 0; k < arriba - abajo; k++) {
            int v = v0 - k, y = arriba - 1 - k;
            for (int u = u0; u <= u1; u++) {
                for (int yy = y + 1; yy <= y + 3; yy++) aire(u, yy, v);
                put(u, y, v, pct(u, y, v, sem) < 20 ? Mina.escalon(Blocks.MOSSY_STONE_BRICK_STAIRS, Direction.SOUTH)
                        : Mina.escalon(peldano, Direction.SOUTH));
            }
        }
    }

    private void cavar() {
        // la escalera de la cripta, detras del altar: baja del abside (v -19) al pasillo de los hermanos (v -29)
        bajada(-1, 1, -19, SUELO, C1, Blocks.STONE_BRICK_STAIRS, 30);
        // la Cripta de los Hermanos: la boveda apuntada (mas alta en medio), los nichos de las paredes y sus cuatro capillas
        for (int v = -29; v >= -50; v--)
            for (int u = -2; u <= 2; u++) for (int y = C1 + 1; y <= C1 + (Math.abs(u) <= 1 ? 5 : 4); y++) aire(u, y, v);
        for (int v = -30; v >= -49; v--) {
            if (capillaC1(v)) continue;
            if (v % 2 != 0) continue;
            for (int s = -1; s <= 1; s += 2) for (int y : new int[]{C1 + 1, C1 + 3}) aire(3 * s, y, v);
        }
        for (int v0 : new int[]{-35, -43}) {
            hueco(3, v0, 5, v0 - 2, C1 + 1, C1 + 4);
            hueco(-5, v0, -3, v0 - 2, C1 + 1, C1 + 4);
        }
        // la escalera de la Capilla de los Huesos: baja del fondo del pasillo (v -51) a la capilla (v -60)
        bajada(-1, 1, -51, C1, C2, Blocks.STONE_BRICK_STAIRS, 31);
        // la Capilla de los Huesos (la boveda de canon) y sus dos salidas
        for (int u = -9; u <= 9; u++)
            for (int v = V2S; v >= V2N; v--) for (int y = C2 + 1; y < techoHuesos(u); y++) aire(u, y, v);
        hueco(-10, VX - 1, -10, VX + 1, C2 + 1, C2 + 4);
        hueco(10, VX - 1, 10, VX + 1, C2 + 1, C2 + 4);
        // las catacumbas del oeste: el pasillo, sus nichos y sus ramales (al taller, a los priores) y el osario menor
        hueco(-11, VX - 1, -34, VX + 1, C2 + 1, C2 + 4);
        for (int u = -12; u >= -33; u--) {
            if (u % 2 != 0) continue;
            for (int y : new int[]{C2 + 1, C2 + 3}) {
                if (u > -23 || u < -17) aire(u, y, VX - 2);
                if (u > -31 || u < -25) aire(u, y, VX + 2);
            }
        }
        hueco(-21, VX - 2, -19, -85, C2 + 1, C2 + 4);
        hueco(-27, -86, -13, -98, C2 + 1, C2 + 5);
        hueco(-29, VX + 2, -27, -59, C2 + 1, C2 + 4);
        hueco(-33, -57, -23, -48, C2 + 1, C2 + 5);
        hueco(-46, -63, -35, -79, C2 + 1, C2 + 6);
        // al este: el pasillo, la Sala del Pozo, el pozo y la chimenea del panteon (la escala)
        hueco(11, VX - 1, 17, VX + 1, C2 + 1, C2 + 4);
        hueco(18, -57, 34, -80, C2 + 1, C2 + 7);
        hueco(POZO_U0, POZO_V1, POZO_U1, POZO_V0, POZO + 1, C2);
        hueco(ESCALA_U, ESCALA_V, ESCALA_U + 1, ESCALA_V, C2 + 8, SUELO - 1);
        // el pasillo del Relicario: llano, cuatro peldanos y llano otra vez; y el Relicario (la cupula)
        hueco(25, -81, 27, -84, C2 + 1, C2 + 4);
        bajada(25, 27, -85, C2, RELICARIO, Blocks.DEEPSLATE_BRICK_STAIRS, 32);
        hueco(25, -89, 27, -99, RELICARIO + 1, RELICARIO + 4);
        for (int u = REL_U - 12; u <= REL_U + 12; u++)
            for (int v = REL_V - 12; v <= REL_V + 12; v++) {
                double r = Math.hypot(u - REL_U, v - REL_V);
                if (r > REL_R) continue;
                for (int y = RELICARIO + 1; y < cupula(r); y++) aire(u, y, v);
            }
    }

    /** Si la fila v del pasillo de los hermanos es la de una de sus capillas (sin nichos). */
    static boolean capillaC1(int v) {
        return v <= -35 && v >= -37 || v <= -43 && v >= -45;
    }

    /** La boveda y las paredes de la Capilla de los Huesos (antes del forro: lo demas lo forra la piedra). */
    private void paredesHuesos() {
        for (int u = -10; u <= 10; u++)
            for (int v = V2S + 1; v >= V2N - 1; v--) {
                boolean dentro = Math.abs(u) <= 9 && v <= V2S && v >= V2N;
                int desde = dentro ? techoHuesos(u) : C2 + 1;
                for (int y = desde; y <= C2 + 13; y++) {
                    if (hueco(u, y, v)) continue;
                    put(u, y, v, bovedaHuesos(u, y, v, dentro));
                }
            }
    }

    /** Lo de la boveda (y las paredes) de la Capilla de los Huesos: el zocalo de piedra, la banda de femures y las costillas. */
    private BlockState bovedaHuesos(int u, int y, int v, boolean dentro) {
        int p = pct(u, y, v, 28);
        if (!dentro) {
            boolean lado = Math.abs(u) == 10;
            if (y <= C2 + 2) return p < 50 ? b(Blocks.POLISHED_DEEPSLATE) : p < 80 ? b(Blocks.DEEPSLATE_BRICKS) : b(Blocks.CRACKED_DEEPSLATE_BRICKS);
            if (y == C2 + 3 || y == C2 + 5) return hueso(lado ? Direction.Axis.Z : Direction.Axis.X);
            if (y == C2 + 4) return p < 60 ? b(Blocks.CALCITE) : hueso(Direction.Axis.Y);
            return huesos(x(u), y, z(v));
        }
        // las costillas de la boveda, cada cuatro filas
        if (Math.floorMod(v - V2S, 4) == 1) return hueso(Direction.Axis.X);
        if (u == 0) return hueso(Direction.Axis.Z);
        return p < 46 ? hueso(p % 2 == 0 ? Direction.Axis.Y : Direction.Axis.Z) : p < 70 ? b(Blocks.CALCITE) : p < 86 ? b(Blocks.POLISHED_DIORITE)
                : b(Blocks.DIORITE);
    }

    // ------------------------------------------------------------------ donde hay huesos (para el forro y el nombre del sitio)

    /** Si (u, y, v) esta en la caja de la Capilla de los Huesos (con sus paredes). */
    static boolean enHuesos(int u, double y, int v) {
        return Math.abs(u) <= 10 && v <= V2S + 1 && v >= V2N - 1 && y >= C2 - 1 && y <= C2 + 14;
    }

    static boolean enPozo(int u, double y, int v) {
        return u >= POZO_U0 - 1 && u <= POZO_U1 + 1 && v >= POZO_V0 - 1 && v <= POZO_V1 + 1 && y >= POZO - 1 && y <= C2;
    }

    static boolean enOsarioMenor(int u, double y, int v) {
        return u >= -47 && u <= -34 && v <= -62 && v >= -80 && y >= C2 - 1 && y <= C2 + 7;
    }

    static boolean enRelicario(int u, double y, int v) {
        return Math.hypot(u - REL_U, v - REL_V) <= REL_R + 1.2 && y >= RELICARIO - 1 && y <= RELICARIO + 14;
    }

    static boolean enSalaPozo(int u, double y, int v) {
        return u >= 18 && u <= 34 && v <= -57 && v >= -80 && y >= POZO && y <= C2 + 8;
    }

    static boolean enTaller(int u, double y, int v) {
        return u >= -27 && u <= -13 && v <= -86 && v >= -98 && y >= C2 && y <= C2 + 6;
    }

    // ------------------------------------------------------------------ los suelos

    private void suelos() {
        // la cripta de los hermanos (con las lapidas del medio)
        for (int v = -29; v >= -50; v--)
            for (int u = -5; u <= 5; u++) {
                if (!hueco(u, C1 + 1, v) || macizo(u, C1, v) || hueco(u, C1, v)) continue;
                put(u, C1, v, u == 0 && Math.floorMod(v, 3) == 0 ? b(Blocks.CHISELED_STONE_BRICKS) : losas(x(u), C1, z(v)));
            }
        // la Capilla de los Huesos: losas de pizarra y, en el pasillo del medio, pizarra pulida
        for (int u = -10; u <= 10; u++)
            for (int v = V2S + 1; v >= V2N; v--) {
                if (!hueco(u, C2 + 1, v) || macizo(u, C2, v) || hueco(u, C2, v)) continue;
                int p = pct(u, C2, v, 29);
                put(u, C2, v, Math.abs(u) <= 1 ? (p < 70 ? b(Blocks.POLISHED_DEEPSLATE) : b(Blocks.DEEPSLATE_TILES))
                        : p < 40 ? b(Blocks.DEEPSLATE_TILES) : p < 62 ? b(Blocks.POLISHED_ANDESITE) : p < 80 ? b(Blocks.CRACKED_DEEPSLATE_TILES)
                        : p < 90 ? b(Blocks.STONE_BRICKS) : hueso(Direction.Axis.X));
            }
        // las catacumbas, el taller, los priores, el osario menor, la Sala del Pozo y su pasillo del norte
        for (int u = -47; u <= 35; u++)
            for (int v = -48; v >= -99; v--) {
                if (enHuesos(u, C2 + 1, v) || !hueco(u, C2 + 1, v) || macizo(u, C2, v) || hueco(u, C2, v)) continue;
                if (u >= POZO_U0 && u <= POZO_U1 && v >= POZO_V0 && v <= POZO_V1) continue;
                put(u, C2, v, enOsarioMenor(u, C2, v) ? huesos(x(u), C2, z(v)) : losas(x(u), C2, z(v)));
            }
        // el fondo del pozo (huesos) y el suelo del Relicario y su pasillo (pizarra)
        for (int u = POZO_U0; u <= POZO_U1; u++)
            for (int v = POZO_V0; v <= POZO_V1; v++) put(u, POZO, v, huesos(x(u), POZO, z(v)));
        for (int u = REL_U - 12; u <= REL_U + 12; u++)
            for (int v = -89; v >= REL_V - 12; v--) {
                if (!hueco(u, RELICARIO + 1, v) || macizo(u, RELICARIO, v) || hueco(u, RELICARIO, v)) continue;
                double r = Math.hypot(u - REL_U, v - REL_V);
                put(u, RELICARIO, v, r < 3.5 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS) : Math.abs(r - 7.5) < 0.6 ? b(Blocks.CHISELED_DEEPSLATE)
                        : reliquia(x(u), RELICARIO, z(v)));
            }
    }

    // ------------------------------------------------------------------ la Cripta de los Hermanos

    /** Lo que hay en un nicho: una calavera, huesos, velas, telarana, una lapida o nada. */
    private void nicho(int u, int y, int v, Direction mira, long sem) {
        int p = pct(u, y, v, sem);
        nichos++;
        if (p < 30) {
            put(u, y, v, calavera(giro(mira) + (p % 3) - 1));
            calaveras++;
        } else if (p < 52) put(u, y, v, hueso(mira.getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X));
        else if (p < 64) {
            put(u, y, v, vela(1 + p % 3, p % 2 == 0));
            velas++;
        } else if (p < 72) put(u, y, v, b(Blocks.COBWEB));
        else if (p < 84) put(u, y, v, b(Blocks.CHISELED_STONE_BRICKS));
        else if (p < 90) put(u, y, v, Mina.losa(Blocks.SMOOTH_STONE_SLAB, SlabType.BOTTOM));
    }

    private void cripta() {
        for (int v = -30; v >= -49; v--) {
            if (capillaC1(v) || v % 2 != 0) continue;
            for (int s = -1; s <= 1; s += 2)
                for (int y : new int[]{C1 + 1, C1 + 3}) nicho(3 * s, y, v, s < 0 ? Direction.EAST : Direction.WEST, 33);
        }
        // los faroles de almas colgados de la boveda y telaranas en lo alto
        for (int v = -32; v >= -50; v -= 6) put(0, C1 + 5, v, Mina.farol(true, true));
        for (int v = -29; v >= -50; v--)
            for (int s = -1; s <= 1; s += 2) if (pct(s, C1 + 4, v, 34) < 9) enHueco(2 * s, C1 + 4, v, b(Blocks.COBWEB));
        // las cuatro capillas: el altar con velas, un sepulcro, la de los huesos y la del arcon (este, al norte)
        capillaAltar(-4, -36, Direction.EAST);
        sepulcro(4, -36);
        for (int v = -43; v >= -45; v--)
            for (int y = C1 + 1; y <= C1 + 2; y++)
                if (pct(-5, y, v, 35) < 70) put(-5, y, v, huesos(x(-5), y, z(v)));
        put(-4, C1 + 1, -45, calavera(12));
        put(-5, C1 + 3, -44, calavera(12));
        calaveras += 2;
        put(5, C1 + 1, -43, b(Blocks.CHISELED_STONE_BRICKS));
        put(5, C1 + 2, -43, vela(2, false));
        velas++;
        // la boca de arriba: la escalera desde el abside
        put(-2, C1 + 3, -28, b(Blocks.COBWEB));
    }

    /** Una capilla con su altar contra la pared (u0: el medio de la capilla; el altar en la pared del fondo). */
    private void capillaAltar(int u0, int v0, Direction mira) {
        int ua = u0 - mira.getStepX();
        put(ua, C1 + 1, v0, b(Blocks.CHISELED_STONE_BRICKS));
        put(ua, C1 + 1, v0 - 1, b(Blocks.POLISHED_ANDESITE));
        put(ua, C1 + 1, v0 + 1, b(Blocks.POLISHED_ANDESITE));
        put(ua, C1 + 2, v0, calavera(giro(mira)));
        put(ua, C1 + 2, v0 - 1, vela(3, true));
        put(ua, C1 + 2, v0 + 1, vela(2, false));
        put(ua, C1 + 3, v0, b(Blocks.COBWEB));
        calaveras++;
        velas += 2;
    }

    /** Un sepulcro de piedra en medio de una capilla (dos de largo, de norte a sur). */
    private void sepulcro(int u, int v) {
        put(u, C1 + 1, v, b(Blocks.POLISHED_DEEPSLATE));
        put(u, C1 + 1, v + 1, b(Blocks.POLISHED_DEEPSLATE));
        put(u, C1 + 2, v, Mina.losa(Blocks.POLISHED_DEEPSLATE_SLAB, SlabType.BOTTOM));
        put(u, C1 + 2, v + 1, Mina.losa(Blocks.POLISHED_DEEPSLATE_SLAB, SlabType.BOTTOM));
        put(u, C1 + 1, v - 1, vela(1, true));
        velas++;
    }

    // ------------------------------------------------------------------ la Capilla de los Huesos

    private void huesos() {
        int vc = VX;
        // las dos filas de calaveras de las paredes de los lados y las de las paredes del norte y del sur
        for (int v = V2S; v >= V2N; v--)
            for (int s = -1; s <= 1; s += 2) {
                int y = Math.floorMod(v, 2) == 0 ? C2 + 4 : C2 + 6;
                if (Math.abs(v - vc) <= 2 && y == C2 + 4) continue;
                if (hueco(9 * s, y, v)) {
                    put(9 * s, y, v, calaveraPared(s < 0 ? Direction.EAST : Direction.WEST));
                    calaveras++;
                }
            }
        for (int u = -8; u <= 8; u += 2) {
            if (Math.abs(u) <= 2) continue;
            put(u, C2 + 4, V2N, calaveraPared(Direction.SOUTH));
            if (Math.abs(u) >= 3) put(u, C2 + 4, V2S, calaveraPared(Direction.NORTH));
            calaveras += 2;
        }
        // la gran lampara de huesos, colgada en medio de la boveda: la cruz de huesos, las velas encima y las calaveras debajo
        int ly = techoHuesos(0) - 4;
        for (int y = ly + 1; y < techoHuesos(0); y++) put(0, y, vc, Mina.cadena(Direction.Axis.Y));
        put(0, ly, vc, hueso(Direction.Axis.Y));
        for (int k = 1; k <= 3; k++) {
            put(k, ly, vc, hueso(Direction.Axis.X));
            put(-k, ly, vc, hueso(Direction.Axis.X));
            put(0, ly, vc + k, hueso(Direction.Axis.Z));
            put(0, ly, vc - k, hueso(Direction.Axis.Z));
        }
        for (Direction d : Direction.Plane.HORIZONTAL) {
            int du = d.getStepX(), dv = d.getStepZ();
            put(3 * du, ly + 1, vc + 3 * dv, vela(4, true));
            put(du, ly + 1, vc + dv, vela(2, false));
            put(3 * du, ly - 1, vc + 3 * dv, calavera(giro(d)));
            put(2 * du, ly - 1, vc + 2 * dv, Mina.cadena(Direction.Axis.Y));
            put(2 * du, ly - 2, vc + 2 * dv, calavera(giro(d)));
            calaveras += 2;
            velas += 2;
        }
        put(0, ly - 1, vc, Mina.cadena(Direction.Axis.Y));
        put(0, ly - 2, vc, hueso(Direction.Axis.Y));
        put(0, ly - 3, vc, calavera(0));
        calaveras++;
        // las guirnaldas de calaveras: una cadena a lo largo de la boveda a cada lado, con calaveras colgando cada dos
        for (int s = -1; s <= 1; s += 2) {
            int u = 6 * s, gy = techoHuesos(u) - 1;
            for (int v = V2S; v >= V2N; v--) {
                put(u, gy, v, Mina.cadena(Direction.Axis.Z));
                if (Math.floorMod(v, 2) == 1) {
                    put(u, gy - 1, v, Mina.cadena(Direction.Axis.Y));
                    put(u, gy - 2, v, calavera(s < 0 ? 12 : 4));
                    calaveras++;
                }
            }
        }
        // y dos de lado a lado (sobre las piramides), siguiendo la boveda
        for (int v : new int[]{V2S - 3, V2N + 3})
            for (int u = -8; u <= 8; u++) {
                int gy = techoHuesos(u) - 1;
                if (Math.abs(u) == 6) continue;
                put(u, gy, v, Mina.cadena(Direction.Axis.X));
                if (Math.floorMod(u, 3) == 0 && u != 0) {
                    put(u, gy - 1, v, calavera(v > vc ? 8 : 0));
                    calaveras++;
                }
            }
        // las cuatro piramides de huesos de las esquinas (con su verja delante)
        for (int su = -1; su <= 1; su += 2)
            for (int sv = -1; sv <= 1; sv += 2) piramide(6 * su, sv < 0 ? V2N + 3 : V2S - 3, su);
        // el altar de los craneos al fondo (norte) y, en la pared, la cruz de huesos sobre la pizarra
        for (int u = -2; u <= 2; u++) {
            put(u, C2 + 1, V2N + 1, Math.abs(u) == 2 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS) : b(Blocks.POLISHED_BLACKSTONE));
            put(u, C2 + 2, V2N + 1, Math.abs(u) == 2 ? vela(4, true) : calavera(0));
            if (Math.abs(u) < 2) calaveras++;
            else velas++;
        }
        put(0, C2 + 3, V2N + 1, calavera(0));
        for (int u = -3; u <= 3; u++)
            for (int y = C2 + 3; y <= C2 + 10; y++) {
                boolean cruz = u == 0 && y >= C2 + 4 || y == C2 + 8 && Math.abs(u) <= 2;
                put(u, y, V2N - 1, cruz ? hueso(u == 0 ? Direction.Axis.Y : Direction.Axis.X) : b(Blocks.POLISHED_BLACKSTONE_BRICKS));
            }
        put(-3, C2 + 8, V2N, calaveraPared(Direction.SOUTH));
        put(3, C2 + 8, V2N, calaveraPared(Direction.SOUTH));
        // el escudo de huesos en la pared del sur, al oeste de la escalera (con su corona de varas de luz)
        escudo(-6, V2S + 1);
        // la roseta de calaveras al este de la escalera
        for (int du = -1; du <= 1; du++)
            for (int dy = -1; dy <= 1; dy++) put(6 + du, C2 + 6 + dy, V2S + 1, du == 0 && dy == 0 ? b(Blocks.POLISHED_BLACKSTONE) : hueso(Direction.Axis.Z));
        put(6, C2 + 6, V2S, calaveraPared(Direction.NORTH));
        put(4, C2 + 6, V2S, calaveraPared(Direction.NORTH));
        put(8, C2 + 6, V2S, calaveraPared(Direction.NORTH));
        calaveras += 3;
        // faroles de almas en los rincones del suelo y velas por las cornisas
        for (int[] q : new int[][]{{-8, V2S}, {8, V2S}, {-8, V2N + 6}, {8, V2N + 6}, {-3, vc}, {3, vc}}) {
            if (macizo(q[0], C2 + 1, q[1])) continue;
            put(q[0], C2 + 1, q[1], Math.abs(q[0]) == 3 ? vela(4, false) : Mina.farol(true, false));
        }
        velas += 2;
    }

    /** Una piramide de huesos (de cinco de ancho, como una campana) con una calavera en lo alto y una verja hacia el pasillo. */
    private void piramide(int u0, int v0, int lado) {
        for (int du = -2; du <= 2; du++)
            for (int dv = -2; dv <= 2; dv++) {
                int m = Math.max(Math.abs(du), Math.abs(dv));
                if (Math.abs(du) == 2 && Math.abs(dv) == 2) continue;
                int alto = m == 0 ? 4 : m == 1 ? 3 : 1 + (pct(u0 + du, 0, v0 + dv, 36) < 40 ? 1 : 0);
                for (int k = 1; k <= alto; k++) put(u0 + du, C2 + k, v0 + dv, huesos(x(u0 + du), C2 + k, z(v0 + dv)));
                if (m == 1 && pct(u0 + du, 1, v0 + dv, 36) < 50) {
                    put(u0 + du, C2 + alto + 1, v0 + dv, calavera(Math.floorMod(du * 4 + dv * 8, 16)));
                    calaveras++;
                }
            }
        put(u0, C2 + 5, v0, calavera(lado < 0 ? 12 : 4));
        calaveras++;
        // la verja: del lado del pasillo del medio
        int uv = u0 - 3 * lado;
        for (int dv = -2; dv <= 2; dv++) une(uv, C2 + 1, v0 + dv, b(Blocks.IRON_BARS));
    }

    /** El escudo de huesos (como el de Sedlec): el contorno de huesos, cuartelado de calcita y pizarra, con su calavera y su corona. */
    private void escudo(int u0, int v) {
        int[][] filas = {{-2, 2}, {-2, 2}, {-2, 2}, {-2, 2}, {-1, 1}, {0, 0}};
        for (int i = 0; i < filas.length; i++) {
            int y = C2 + 8 - i;
            for (int du = filas[i][0]; du <= filas[i][1]; du++) {
                boolean borde = du == filas[i][0] || du == filas[i][1] || i == 0 || i == filas.length - 1;
                boolean claro = (du < 0) == (i < 3);
                put(u0 + du, y, v, borde ? hueso(Direction.Axis.Y) : claro ? b(Blocks.CALCITE) : b(Blocks.POLISHED_BLACKSTONE));
            }
        }
        put(u0, C2 + 6, v - 1, calaveraPared(Direction.NORTH));
        calaveras++;
        for (int du = -2; du <= 2; du += 2) put(u0 + du, C2 + 9, v, b(Blocks.END_ROD));
    }

    // ------------------------------------------------------------------ las catacumbas del oeste

    private void catacumbas() {
        for (int u = -12; u >= -33; u--) {
            if (u % 2 != 0) continue;
            for (int y : new int[]{C2 + 1, C2 + 3}) {
                if (hueco(u, y, VX - 2) && !hueco(u, y, VX - 3)) nicho(u, y, VX - 2, Direction.SOUTH, 37);
                if (hueco(u, y, VX + 2) && !hueco(u, y, VX + 3)) nicho(u, y, VX + 2, Direction.NORTH, 37);
            }
        }
        for (int u = -14; u >= -34; u -= 7) put(u, C2 + 4, VX, Mina.farol(true, true));
        for (int u = -11; u >= -34; u--)
            for (int dv = -1; dv <= 1; dv += 2) if (pct(u, C2 + 4, VX + dv, 38) < 12) enHueco(u, C2 + 4, VX + dv, b(Blocks.COBWEB));
        // cascotes y huesos sueltos por el suelo
        for (int u = -12; u >= -33; u--)
            for (int dv = -1; dv <= 1; dv++) {
                int p = pct(u, C2 + 1, VX + dv, 39);
                if (p < 4) enHueco(u, C2 + 1, VX + dv, calavera(p * 5));
                else if (p < 7 && dv != 0) enHueco(u, C2 + 1, VX + dv, Mina.losa(Blocks.COBBLESTONE_SLAB, SlabType.BOTTOM));
            }
        // los ramales: telaranas y un farol
        put(-20, C2 + 4, -80, Mina.farol(true, true));
        put(-28, C2 + 4, -63, Mina.farol(true, true));
        for (int v = VX - 3; v >= -85; v--) if (pct(-21, C2 + 4, v, 40) < 15) enHueco(-21, C2 + 4, v, b(Blocks.COBWEB));
    }

    /** El Taller del Sacristan: donde Fray Osmund limpiaba, ordenaba y ensartaba los huesos (sin cama: un jergon de paja). */
    private void taller() {
        int s = C2 + 1;
        // las vigas del techo
        for (int v = -88; v >= -96; v -= 4)
            for (int u = -27; u <= -13; u++) put(u, C2 + 5, v, Mina.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        // la mesa larga del medio (tablones y losas) con los huesos a medio ordenar, las velas y las cadenas para ensartarlos
        for (int u = -23; u <= -17; u++)
            for (int v = -91; v >= -92; v--) {
                put(u, s, v, u == -23 || u == -17 ? b(Blocks.STRIPPED_SPRUCE_LOG) : Mina.losa(Blocks.SPRUCE_SLAB, SlabType.TOP));
                int p = pct(u, s + 1, v, 41);
                if (p < 30) put(u, s + 1, v, calavera(p % 16));
                else if (p < 50) put(u, s + 1, v, Mina.losa(Blocks.SMOOTH_STONE_SLAB, SlabType.BOTTOM));
                else if (p < 62) put(u, s + 1, v, vela(1 + p % 3, true));
                else if (p < 74) put(u, s + 1, v, Mina.cadena(Direction.Axis.X));
            }
        // los bancos de trabajo de las paredes: la muela (para pulir), el banco, la olla (para hervirlos) y el yunque roto
        put(-26, s, -88, Blocks.GRINDSTONE.defaultBlockState());
        put(-26, s, -90, b(Blocks.CRAFTING_TABLE));
        put(-26, s, -92, b(Blocks.CAULDRON));
        put(-26, s, -94, Blocks.CHIPPED_ANVIL.defaultBlockState());
        put(-14, s, -88, b(Blocks.SMITHING_TABLE));
        put(-14, s, -90, b(Blocks.FLETCHING_TABLE));
        // las pilas de huesos ya ordenados contra la pared del este: femures, costillas y una de calaveras
        for (int v = -93; v >= -97; v--)
            for (int y = s; y <= s + 2; y++) {
                if (y == s + 2 && v % 2 == 0) continue;
                put(-14, y, v, v >= -95 ? hueso(Direction.Axis.X) : hueso(Direction.Axis.Y));
            }
        for (int v = -86; v >= -87; v--) for (int u = -27; u <= -25; u++) if (u != -25 || v != -86) put(u, s, v, hueso(Direction.Axis.Z));
        put(-15, s, -97, calavera(8));
        put(-15, s + 1, -97, calavera(9));
        put(-16, s, -97, calavera(7));
        // las estanterias con calaveras encima (los craneos con nombre)
        for (int u = -24; u <= -16; u++) {
            if (u == ATRIL_U) continue;
            put(u, s, -98, b(Blocks.BOOKSHELF));
            put(u, s + 1, -98, b(Blocks.BOOKSHELF));
            if (u % 2 == 0) {
                put(u, s + 2, -98, calavera(0));
                calaveras++;
            } else put(u, s + 2, -98, vela(1, false));
        }
        // el jergon de Fray Osmund (paja y una manta), en el rincon del suroeste, y su capucha colgada (una cadena)
        put(-27, s, -97, b(Blocks.HAY_BLOCK));
        put(-27, s, -96, b(Blocks.HAY_BLOCK));
        put(-27, s + 1, -97, b(Blocks.BROWN_CARPET));
        put(-27, s + 1, -96, b(Blocks.BROWN_CARPET));
        put(-27, s + 3, -94, Mina.cadena(Direction.Axis.Y));
        // los faroles y alguna telarana
        put(-20, C2 + 4, -89, Mina.farol(false, true));
        put(-20, C2 + 4, -95, Mina.farol(true, true));
        put(-13, C2 + 4, -86, b(Blocks.COBWEB));
        put(-27, C2 + 4, -98, b(Blocks.COBWEB));
        velas += 4;
    }

    /** La cripta de los priores (detras del muro ilusorio): sus tres sepulcros y el arcon bueno al fondo. */
    private void priores() {
        int s = C2 + 1;
        for (int u : new int[]{-31, -28, -25})
            for (int v = -52; v >= -53; v--) {
                put(u, s, v, b(Blocks.POLISHED_DEEPSLATE));
                put(u, s + 1, v, Mina.losa(Blocks.POLISHED_DEEPSLATE_SLAB, SlabType.BOTTOM));
            }
        put(-31, s, -51, vela(3, true));
        put(-25, s, -51, vela(3, true));
        put(-28, s, -54, calavera(0));
        for (int u = -33; u <= -23; u++) {
            if (u == -28) continue;
            if (pct(u, s, -48, 42) < 50) put(u, s, -48, hueso(Direction.Axis.X));
        }
        put(-33, C2 + 5, -57, b(Blocks.COBWEB));
        put(-23, C2 + 5, -48, b(Blocks.COBWEB));
        velas += 2;
        calaveras++;
    }

    /** El osario menor (al fondo de las catacumbas): los huesos sin ordenar, a montones. */
    private void osarioMenor() {
        for (int u = -46; u <= -35; u++)
            for (int v = -63; v >= -79; v--) {
                double m = 2.6 * (Mina.ruido(u * 0.45 + 3, v * 0.41) + 0.4) - Math.max(0, 2 - Math.hypot(u + 40, v + 71) * 0.5) * 2;
                int alto = (int) Math.round(m);
                if (u >= -37 && Math.abs(v - VX) <= 1) alto = 0;
                for (int k = 1; k <= Math.min(3, alto); k++) put(u, C2 + k, v, huesos(x(u), C2 + k, z(v)));
                int p = pct(u, C2 + 1, v, 43);
                if (alto >= 0 && alto < 3 && p < 9) {
                    put(u, C2 + 1 + Math.max(0, alto), v, calavera(p * 2));
                    calaveras++;
                }
            }
        put(-40, C2 + 5, -66, Mina.farol(true, true));
        put(-41, C2 + 5, -76, Mina.farol(true, true));
        for (int[] q : new int[][]{{-46, -63}, {-46, -79}, {-35, -79}, {-35, -63}}) put(q[0], C2 + 6, q[1], b(Blocks.COBWEB));
    }

    // ------------------------------------------------------------------ al este: la Sala del Pozo

    private void salaPozo() {
        int s = C2 + 1;
        // las vigas del techo, con cadenas colgando
        for (int v = -60; v >= -78; v -= 6)
            for (int u = 18; u <= 34; u++) put(u, C2 + 7, v, Mina.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        for (int u = 20; u <= 32; u += 4) for (int v = -63; v >= -75; v -= 6) put(u, C2 + 6, v, Mina.cadena(Direction.Axis.Y));
        // el brocal roto del pozo (un murete con huecos) y, por dentro, las paredes de huesos con calaveras
        for (int u = POZO_U0 - 1; u <= POZO_U1 + 1; u++)
            for (int v = POZO_V0 - 1; v <= POZO_V1 + 1; v++) {
                boolean borde = u == POZO_U0 - 1 || u == POZO_U1 + 1 || v == POZO_V0 - 1 || v == POZO_V1 + 1;
                if (!borde) continue;
                if (u == ESCALA_U && v == POZO_V0 - 1) continue;
                if (pct(u, s, v, 44) < 62) une(u, s, v, b(Blocks.COBBLED_DEEPSLATE_WALL));
            }
        for (int y = POZO + 1; y <= C2 - 1; y++)
            for (int u = POZO_U0; u <= POZO_U1; u++)
                for (int v = POZO_V0; v <= POZO_V1; v++) {
                    boolean borde = u == POZO_U0 || u == POZO_U1 || v == POZO_V0 || v == POZO_V1;
                    if (!borde || pct(u, y, v, 45) >= 7) continue;
                    if (u == ESCALA_U && v == POZO_V0) continue;
                    Direction d = u == POZO_U0 ? Direction.EAST : u == POZO_U1 ? Direction.WEST : v == POZO_V0 ? Direction.SOUTH : Direction.NORTH;
                    put(u, y, v, calaveraPared(d));
                    calaveras++;
                }
        // la escala del pozo (contra su pared del norte) y el monton de huesos del fondo
        for (int y = POZO + 1; y <= C2; y++) put(ESCALA_U, y, POZO_V0, Mina.escala(Direction.SOUTH));
        for (int u = POZO_U0; u <= POZO_U1; u++)
            for (int v = POZO_V0; v <= POZO_V1; v++) {
                if (u == ESCALA_U && v == POZO_V0) continue;
                double m = 2.2 - Math.hypot(u - 26.5, v - POZO_V1 + 1.5) * 0.55 + Mina.ruido(u * 0.9, v * 0.9) * 0.5;
                int alto = v <= POZO_V0 + 1 || u == 24 && v == POZO_V1 ? 0 : (int) Math.round(m);
                for (int k = 1; k <= alto; k++) put(u, POZO + k, v, huesos(x(u), POZO + k, z(v)));
                if (alto >= 0 && pct(u, POZO, v, 46) < 14) {
                    put(u, POZO + Math.max(0, alto) + 1, v, calavera(pct(u, 1, v, 46) % 16));
                    calaveras++;
                }
            }
        put(26, C2 + 6, -69, Mina.cadena(Direction.Axis.Y));
        // la escala de la chimenea del panteon: de la sala a la escotilla (la palanca, debajo)
        for (int y = s; y <= SUELO - 1; y++) put(ESCALA_U, y, ESCALA_V, Mina.escala(Direction.NORTH));
        put(ESCALA_U + 1, SUELO - 1, ESCALA_V, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.CEILING)
                .setValue(LeverBlock.FACING, Direction.NORTH).setValue(LeverBlock.POWERED, false));
        // huesos y calaveras por los rincones, faroles
        for (int[] q : new int[][]{{18, -57}, {34, -57}, {18, -80}, {34, -80}, {19, -57}, {34, -58}, {18, -79}})
            put(q[0], s, q[1], huesos(x(q[0]), s, z(q[1])));
        put(33, s, -79, calavera(6));
        put(19, s, -79, calavera(10));
        put(22, C2 + 6, -60, Mina.farol(true, true));
        put(31, C2 + 6, -75, Mina.farol(true, true));
        put(20, C2 + 6, -76, Mina.farol(true, true));
        for (int[] q : new int[][]{{18, -57}, {34, -80}, {34, -57}}) put(q[0], C2 + 7, q[1], b(Blocks.COBWEB));
        calaveras += 2;
    }

    // ------------------------------------------------------------------ el Relicario (la sala de Fray Osmund: parte 7at)

    private void relicario() {
        // el pasillo: faroles de almas
        put(26, RELICARIO + 4, -93, Mina.farol(true, true));
        put(26, C2 + 4, -82, Mina.farol(true, true));
        // los ocho pilares de pizarra y hueso, en corro
        for (int k = 0; k < 8; k++) {
            double a = k * Math.PI / 4 + Math.PI / 8;
            int u = REL_U + (int) Math.round(8 * Math.cos(a)), v = REL_V + (int) Math.round(8 * Math.sin(a));
            int tope = cupula(Math.hypot(u - REL_U, v - REL_V));
            for (int y = RELICARIO + 1; y < tope; y++)
                put(u, y, v, y == RELICARIO + 4 || y == RELICARIO + 8 ? hueso(Direction.Axis.Y) : b(Blocks.POLISHED_DEEPSLATE));
            put(u, RELICARIO + 6, v + (v > REL_V ? -1 : 1), calaveraPared(v > REL_V ? Direction.NORTH : Direction.SOUTH));
            calaveras++;
        }
        // el relicario de San Ulfo en medio: la grada, el arca de cristal con su calavera, las velas de alrededor
        for (int du = -2; du <= 2; du++)
            for (int dv = -2; dv <= 2; dv++) {
                if (Math.abs(du) == 2 && Math.abs(dv) == 2) continue;
                put(REL_U + du, RELICARIO + 1, REL_V + dv, Math.max(Math.abs(du), Math.abs(dv)) == 2
                        ? Mina.losa(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, SlabType.BOTTOM) : b(Blocks.POLISHED_BLACKSTONE_BRICKS));
            }
        put(REL_U, RELICARIO + 2, REL_V, b(Blocks.GOLD_BLOCK));
        put(REL_U, RELICARIO + 3, REL_V, calavera(0));
        for (Direction d : Direction.Plane.HORIZONTAL) {
            une(REL_U + d.getStepX(), RELICARIO + 3, REL_V + d.getStepZ(), b(Blocks.GLASS_PANE));
            put(REL_U + d.getStepX(), RELICARIO + 2, REL_V + d.getStepZ(), vela(4, true));
        }
        put(REL_U, RELICARIO + 4, REL_V, Mina.losa(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.BOTTOM));
        for (int k = 0; k < 4; k++) {
            int du = k < 2 ? -1 : 1, dv = k % 2 == 0 ? -1 : 1;
            put(REL_U + du, RELICARIO + 2, REL_V + dv, b(Blocks.POLISHED_BLACKSTONE_BRICKS));
            une(REL_U + du, RELICARIO + 3, REL_V + dv, b(Blocks.IRON_BARS));
        }
        // los montones de huesos del borde (entre los pilares, menos en la entrada): de ahi se levantan los muertos de Fray Osmund
        for (int a : MONTONES) {
            double r = Math.toRadians(a);
            int u = REL_U + (int) Math.round(R_MONTON * Math.cos(r)), v = REL_V + (int) Math.round(R_MONTON * Math.sin(r));
            put(u, RELICARIO + 1, v, hueso(Direction.Axis.Y));
            put(u, RELICARIO + 2, v, calavera((int) Math.round((a + 90) / 22.5) & 15));
            int du = (int) Math.signum(Math.round(Math.sin(r) * 2)), dv = -(int) Math.signum(Math.round(Math.cos(r) * 2));
            if (!macizo(u + du, RELICARIO + 1, v + dv)) put(u + du, RELICARIO + 1, v + dv, hueso(du != 0 ? Direction.Axis.X : Direction.Axis.Z));
            calaveras++;
        }
        // la lampara de la cupula, apagada (solo la cadena y su aro)
        int ty = cupula(0);
        for (int y = ty - 4; y < ty; y++) put(REL_U, y, REL_V, Mina.cadena(Direction.Axis.Y));
        for (Direction d : Direction.Plane.HORIZONTAL) put(REL_U + d.getStepX(), ty - 5, REL_V + d.getStepZ(), hueso(d.getAxis()));
        put(REL_U, ty - 5, REL_V, hueso(Direction.Axis.Y));
        velas += 4;
        calaveras++;
    }

    // ------------------------------------------------------------------ el camposanto (arriba)

    /** Lo alto de la tapia en (u, v): dos y la albardilla, menos donde se ha caido (0: no queda nada). */
    static int altoTapia(int u, int v) {
        // el trozo caido del norte (por donde se vuelve del panteon) y el del suroeste
        if (v == -27 && u >= 16 && u <= 19) return 0;
        if (u == -23 && v >= 10 && v <= 12) return 0;
        double n = Mina.ruido(u * 0.23 + 3.1, v * 0.19 - 1.7) + (pct(u, 0, v, 31) - 50) / 120.0;
        return n > 0.8 ? 0 : n > 0.55 ? 1 : n > 0.35 ? 2 : 3;
    }

    private void camposanto() {
        // la tapia
        for (int u = -23; u <= 23; u++)
            for (int v = -27; v <= 25; v++) {
                if (u != -23 && u != 23 && v != -27 && v != 25) continue;
                if (v == 25 && Math.abs(u) <= 1) continue;
                if (Math.abs(u) == 2 && v == 25) continue;
                int alto = altoTapia(u, v);
                put(u, SUELO, v, sillar(x(u), SUELO, z(v)));
                for (int k = 0; k < Math.min(2, alto); k++) put(u, PISO + k, v, sillar(x(u), PISO + k, z(v)));
                if (alto == 3) put(u, PISO + 2, v, pct(u, 2, v, 32) < 60 ? Mina.losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM)
                        : Mina.losa(Blocks.MOSSY_COBBLESTONE_SLAB, SlabType.BOTTOM));
                if (alto <= 1 && pct(u, 3, v, 32) < 55) {
                    int du = u == -23 ? 1 : u == 23 ? -1 : 0, dv = v == -27 ? 1 : v == 25 ? -1 : 0;
                    put(u + du, PISO, v + dv, pct(u, 4, v, 32) < 50 ? Mina.losa(Blocks.COBBLESTONE_SLAB, SlabType.BOTTOM) : b(Blocks.MOSSY_COBBLESTONE));
                }
            }
        // la puerta: dos pilares con su calavera, el dintel partido y un farol de almas
        for (int s = -2; s <= 2; s += 4) {
            put(s, SUELO, 25, b(Blocks.STONE_BRICKS));
            for (int y = PISO; y <= PISO + 3; y++) put(s, y, 25, sillar(x(s), y, z(25)));
            put(s, PISO + 4, 25, b(Blocks.CHISELED_STONE_BRICKS));
            put(s, PISO + 5, 25, calavera(0));
            calaveras++;
        }
        put(-1, PISO + 4, 25, b(Blocks.STONE_BRICKS));
        put(0, PISO + 4, 25, Mina.losa(Blocks.STONE_BRICK_SLAB, SlabType.TOP));
        put(-1, PISO + 3, 25, Mina.farol(true, true));
        put(1, PISO, 26, Mina.losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
        // los caminos: de la puerta a la capilla y del santuario a la puerta (grava, empedrado y tierra pisada)
        for (int v = SANT_V - SantuariosMorvain.R - 1; v >= 0; v--)
            for (int u = -1; u <= 1; u++) {
                int p = pct(u, SUELO, v, 33);
                if (p < 12) continue;
                put(u, SUELO, v, p < 40 ? b(Blocks.GRAVEL) : p < 64 ? b(Blocks.COBBLESTONE) : p < 82 ? b(Blocks.COARSE_DIRT)
                        : p < 92 ? b(Blocks.MOSSY_COBBLESTONE) : b(Blocks.DIRT_PATH));
            }
        // las tumbas: dos campos al sur, el de los pobres al noroeste y otro al noreste
        int[] us = {-21, -18, -15, -12, -9, -6, 6, 9, 12, 15, 18, 21};
        for (int u : us)
            for (int v = 2; v <= 22; v += 4) tumba(u, v);
        for (int u = -21; u <= -9; u += 3)
            for (int v = -25; v <= -5; v += 4) tumba(u, v);
        for (int u = 12; u <= 21; u += 3)
            for (int v = -25; v <= -9; v += 4) tumba(u, v);
        // los arboles muertos de dentro (grandes, retorcidos)
        arbol(-19, 23, 9, 4, Blocks.DARK_OAK_LOG);
        arbol(20, 24, 7, 3, Blocks.DARK_OAK_LOG);
        arbol(-21, -2, 8, 3, Blocks.SPRUCE_LOG);
        arbol(21, -26, 10, 4, Blocks.DARK_OAK_LOG);
        // hierba, helechos y setas entre las tumbas (no en los caminos ni en la capilla)
        for (int u = -22; u <= 22; u++)
            for (int v = -26; v <= 24; v++) {
                if (Math.abs(u) <= 1 && v >= 0 || u >= -6 && u <= 10 && v >= -25 && v <= 0) continue;
                if (plano.hay(x(u), PISO, z(v))) continue;
                int p = pct(u, PISO, v, 34);
                if (p < 7) put(u, PISO, v, b(Blocks.GRASS));
                else if (p < 10) put(u, PISO, v, b(Blocks.FERN));
                else if (p < 12) put(u, PISO, v, b(Blocks.DEAD_BUSH));
                else if (p < 13) put(u, PISO, v, b(Blocks.BROWN_MUSHROOM));
            }
    }

    /** Las tumbas que han abierto los sepultureros (junto a ellas cavan): su cabecera (u, v). */
    static final int[][] CAVAN = {{-12, 10}, {15, -9}, {-18, -17}};

    /** Una tumba: la cabecera en (u, v) y el monton (o el hoyo) en v + 1 y v + 2. */
    private void tumba(int u, int v) {
        int p = pct(u, 1, v, 40);
        boolean pobres = u < -7 && v < 0, cavan = false;
        for (int[] q : CAVAN) if (q[0] == u && q[1] == v) cavan = true;
        int tipo;
        if (cavan) tipo = 2;
        else if (pobres) tipo = p < 40 ? 1 : p < 76 ? 2 : p < 88 ? 5 : 6;
        else tipo = p < 28 ? 0 : p < 42 ? 1 : p < 58 ? 2 : p < 68 ? 3 : p < 78 ? 4 : p < 92 ? 5 : 6;
        if (tipo == 6) return;
        tumbas++;
        int q = pct(u, 2, v, 40);
        // el monton de tierra (o el hoyo, con los huesos que quedan en el fondo y la tierra sacada al lado)
        for (int dv = 1; dv <= 2; dv++) {
            if (tipo == 2) {
                put(u, SUELO, v + dv, Pal.AIR);
                int r = pct(u, 3, v + dv, 40);
                put(u, SUELO - 1, v + dv, r < 30 ? hueso(Direction.Axis.Z) : r < 70 ? b(Blocks.COARSE_DIRT) : b(Blocks.ROOTED_DIRT));
                if (r >= 85) put(u, SUELO, v + dv, calavera(r % 16));
                int lado = cavan ? -1 : q % 2 == 0 ? 1 : -1;
                if (dv == 1 || q < 50) put(u + lado, PISO, v + dv, b(Blocks.COARSE_DIRT));
            } else {
                int r = pct(u, 4, v + dv, 40);
                put(u, SUELO, v + dv, r < 40 ? b(Blocks.PODZOL) : r < 70 ? b(Blocks.COARSE_DIRT) : b(Blocks.ROOTED_DIRT));
                if (r >= 88) put(u, PISO, v + dv, b(Blocks.MOSS_CARPET));
                else if (r >= 80) put(u, PISO, v + dv, b(Blocks.DEAD_BUSH));
            }
        }
        if (tipo == 2) abiertas++;
        // la cabecera
        switch (tipo) {
            case 0 -> {
                Block[] muros = {Blocks.COBBLESTONE_WALL, Blocks.MOSSY_COBBLESTONE_WALL, Blocks.STONE_BRICK_WALL, Blocks.ANDESITE_WALL,
                        Blocks.MOSSY_STONE_BRICK_WALL};
                une(u, PISO, v, b(muros[q % muros.length]));
                if (q < 12) {
                    put(u, PISO + 1, v, calavera(0));
                    calaveras++;
                }
            }
            case 1 -> {
                une(u, PISO, v, b(Blocks.SPRUCE_FENCE));
                une(u, PISO + 1, v, b(Blocks.SPRUCE_FENCE));
                if (q % 3 != 0) {
                    une(u - 1, PISO + 1, v, b(Blocks.SPRUCE_FENCE));
                    une(u + 1, PISO + 1, v, b(Blocks.SPRUCE_FENCE));
                }
                if (q % 4 != 0) une(u, PISO + 2, v, b(Blocks.SPRUCE_FENCE));
                cruces++;
            }
            case 2 -> put(u, PISO, v, q < 50 ? Mina.losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM)
                    : Mina.escalon(Blocks.STONE_BRICK_STAIRS, q % 2 == 0 ? Direction.EAST : Direction.WEST));
            case 3 -> {
                put(u, PISO, v, b(Blocks.CHISELED_STONE_BRICKS));
                une(u, PISO + 1, v, b(Blocks.STONE_BRICK_WALL));
                if (q < 40) {
                    une(u, PISO + 2, v, b(Blocks.STONE_BRICK_WALL));
                    put(u, PISO + 3, v, calavera(q % 16));
                    calaveras++;
                } else {
                    put(u, PISO + 2, v, b(Blocks.CHISELED_STONE_BRICKS));
                    put(u, PISO + 3, v, vela(1 + q % 3, false));
                    velas++;
                }
            }
            case 4 -> put(u, PISO, v, Mina.losa(q < 50 ? Blocks.ANDESITE_SLAB : Blocks.MOSSY_STONE_BRICK_SLAB, SlabType.BOTTOM));
            default -> {
            }
        }
    }

    /** Un arbol muerto: tronco de {@code alto} y {@code ramas} ramas torcidas, sin hojas (como los del bosque). */
    private void arbol(int u, int v, int alto, int ramas, Block madera) {
        int base = superficie(u, v);
        for (int k = 1; k <= alto; k++) put(u, base + k, v, Mina.tronco(madera, Direction.Axis.Y));
        Direction[] lados = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (int k = 0; k < ramas; k++) {
            int p = pct(u, k, v, 47 + k);
            Direction d = lados[(p + k) % 4];
            int by = base + alto / 2 + 1 + p % Math.max(1, alto / 2);
            int largo = 1 + p % 3;
            for (int i = 1; i <= largo; i++) {
                int y = by + i / 2;
                if (y > base + alto + 1) break;
                put(u + d.getStepX() * i, y, v + d.getStepZ() * i, Mina.tronco(madera, d.getAxis()));
            }
        }
        arboles++;
    }

    /** El panteon de los Ulfing, fuera de la tapia al noreste: su escotilla de hierro solo se abre desde abajo (el atajo). */
    private void panteon() {
        int u0 = 23, u1 = 29, v0 = -60, v1 = -54;
        for (int u = u0 - 1; u <= u1 + 1; u++)
            for (int v = v0 - 1; v <= v1 + 1; v++) {
                boolean dentro = u >= u0 && u <= u1 && v >= v0 && v <= v1;
                put(u, SUELO, v, dentro ? (Math.floorMod(u + v, 2) == 0 ? b(Blocks.POLISHED_ANDESITE) : b(Blocks.STONE_BRICKS)) : sillar(x(u), SUELO, z(v)));
                if (!dentro) continue;
                boolean borde = u == u0 || u == u1 || v == v0 || v == v1;
                if (borde) for (int y = PISO; y <= PISO + 4; y++) put(u, y, v, pct(u, y, v, 48) < 45 ? b(Blocks.POLISHED_ANDESITE) : sillar(x(u), y, z(v)));
            }
        // la puerta (al sur, hacia el camposanto) entre dos columnas, y el tejado con su fronton
        for (int y = PISO; y <= PISO + 2; y++) put(26, y, v1, Pal.AIR);
        for (int s = -2; s <= 2; s += 4) {
            for (int y = PISO; y <= PISO + 3; y++) une(26 + s, y, v1 + 1, b(Blocks.STONE_BRICK_WALL));
            put(26 + s, PISO + 4, v1 + 1, b(Blocks.CHISELED_STONE_BRICKS));
        }
        for (int u = u0 - 1; u <= u1 + 1; u++)
            for (int v = v0 - 1; v <= v1 + 1; v++) put(u, PISO + 5, v, Mina.losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
        for (int u = 24; u <= 28; u++) put(u, PISO + 5, v1 + 1, b(Blocks.STONE_BRICKS));
        for (int u = 25; u <= 27; u++) put(u, PISO + 6, v1 + 1, u == 26 ? b(Blocks.CHISELED_STONE_BRICKS) : Mina.losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
        put(26, PISO + 7, v1 + 1, calavera(0));
        // dentro: los dos sepulcros, el altar del fondo y la escotilla
        for (int u : new int[]{24, 28})
            for (int v = v0 + 1; v <= v0 + 2; v++) {
                put(u, PISO, v, b(Blocks.POLISHED_DEEPSLATE));
                put(u, PISO + 1, v, Mina.losa(Blocks.POLISHED_DEEPSLATE_SLAB, SlabType.BOTTOM));
            }
        put(26, PISO, v0 + 1, b(Blocks.CHISELED_STONE_BRICKS));
        put(26, PISO + 1, v0 + 1, vela(2, true));
        put(25, PISO, v0 + 1, vela(1, false));
        put(ESCALA_U, SUELO, ESCALA_V, Blocks.IRON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.NORTH)
                .setValue(TrapDoorBlock.HALF, Half.TOP).setValue(TrapDoorBlock.OPEN, false));
        put(28, PISO + 4, v0 + 1, b(Blocks.COBWEB));
        velas += 2;
        calaveras++;
    }

    /** Los arboles muertos de alrededor (en los chunks donde {@link Adorno} no pone nada) y sus helechos y setas. */
    private void bosque() {
        for (int i = 0; (AX0 + i * 7) <= AX1; i++)
            for (int j = 0; (AZ0 + j * 7) <= AZ1; j++) {
                int p = Hash.pct(i, 0, j, SEMILLA + 60);
                if (p >= 72) continue;
                int u = AX0 - CX + i * 7 + p % 5, v = AZ0 - CZ + j * 7 + (p / 5) % 5;
                if (x(u) > AX1 || z(v) > AZ1) continue;
                if (peso(u, v) > 0.02 || Math.hypot(u - SANT_U, v - SANT_V) < SantuariosMorvain.R + SantuariosMorvain.MEZCLA + 2) continue;
                arbol(u, v, 6 + p % 7, 2 + p % 3, p % 5 < 3 ? Blocks.DARK_OAK_LOG : Blocks.SPRUCE_LOG);
            }
        // algun tronco caido
        for (int[] q : new int[][]{{-34, 12}, {38, -8}, {-30, -44}, {42, 36}}) {
            int u = q[0], v = q[1];
            if (peso(u, v) > 0.02) continue;
            int y = superficie(u, v) + 1;
            for (int k = 0; k < 5; k++) put(u + k, y, v, Mina.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        }
        for (int u = AX0 - CX; u <= AX1 - CX; u++)
            for (int v = AZ0 - CZ; v <= AZ1 - CZ; v++) {
                int p = pct(u, 0, v, 61);
                if (p >= 4 || peso(u, v) > 0.3) continue;
                int y = superficie(u, v) + 1;
                if (plano.hay(x(u), y, z(v))) continue;
                put(u, y, v, p < 2 ? b(Blocks.FERN) : p < 3 ? b(Blocks.BROWN_MUSHROOM) : b(Blocks.GRASS));
            }
    }

    // ------------------------------------------------------------------ cofres, atril y muro ilusorio

    private void trozos() {
        // el atril del taller, con el libro de los huesos de Fray Osmund
        trozo(ATRIL_U, C2 + 1, ATRIL_V, c -> c.lectern(x(ATRIL_U), C2 + 1, z(ATRIL_V), Direction.SOUTH, Perdidos.libro(LIBRO)));
        trozo(5, C1 + 1, -45, c -> c.chest(x(5), C1 + 1, z(-45), Direction.WEST, BOTIN));
        trozo(-45, C2 + 1, -64, c -> c.chest(x(-45), C2 + 1, z(-64), Direction.EAST, BOTIN));
        trozo(-26, C2 + 1, -95, c -> c.barrel(x(-26), C2 + 1, z(-95), Direction.UP, BOTIN));
        trozo(-14, C2 + 1, -92, c -> c.barrel(x(-14), C2 + 1, z(-92), Direction.UP, BOTIN));
        trozo(24, POZO + 1, POZO_V1, c -> c.chest(x(24), POZO + 1, z(POZO_V1), Direction.NORTH, BOTIN));
        trozo(-28, C2 + 1, -48, c -> c.chest(x(-28), C2 + 1, z(-48), Direction.SOUTH, BOTIN_BUENO));
        trozo(6, PISO + 13, -3, c -> c.chest(x(6), PISO + 13, z(-3), Direction.EAST, BOTIN));
    }

    private void trozo(int u, int y, int v, Trozo.Obra o) {
        elementos.add(new Trozo(6, x(u), y, z(v), x(u), y, z(v), o));
    }

    /** Las celdas de un muro ilusorio del osario (el del fondo del pasillo de los priores). */
    public static List<BlockPos> muro(String id) {
        List<BlockPos> l = new ArrayList<>();
        if (MURO_PRIORES.equals(id))
            for (int u = -29; u <= -27; u++)
                for (int y = C2 + 1; y <= C2 + 3; y++) l.add(new BlockPos(x(u), y, z(-58)));
        return l;
    }

    /** Lo que hay en una celda del muro ilusorio cuando esta cerrado. */
    public static BlockState bloqueMuro(String id, BlockPos p) {
        return piedra(p.getX(), p.getY(), p.getZ());
    }

    // ------------------------------------------------------------------ los sitios y los de alli

    private void ponerSitios() {
        sitio("entrada", SANT_U, PISO, SANT_V - 6);
        sitio("puerta", 0, PISO, 22);
        sitio("camposanto", -12, PISO, 6);
        sitio("pobres", -15, PISO, -15);
        sitio("capilla", 0, PISO, -6);
        sitio("altar", -3, PISO, -18);
        sitio("campanario", 7, PISO + 13, -4);
        sitio("cripta", 0, C1 + 1, -31);
        sitio("hermanos", 0, C1 + 1, -40);
        sitio("bajada", 0, C1 + 1, -50);
        sitio("huesos", 0, C2 + 1, V2S);
        sitio("lampara", 0, C2 + 1, VX + 3);
        sitio("altar_huesos", 0, C2 + 1, V2N + 3);
        sitio("catacumbas", -16, C2 + 1, VX);
        sitio("taller", -20, C2 + 1, -88);
        sitio("priores_pasillo", -28, C2 + 1, -60);
        sitio("priores", -28, C2 + 1, -52);
        sitio("osario_menor", -36, C2 + 1, VX);
        sitio("pozo", 20, C2 + 1, -64);
        sitio("fondo_pozo", 27, POZO + 1, POZO_V0 + 1);
        sitio("escala", ESCALA_U, C2 + 1, ESCALA_V + -1);
        sitio("panteon", 26, PISO, -52);
        sitio("pasillo_relicario", 26, RELICARIO + 1, -95);
        sitio("relicario", REL_U, RELICARIO + 1, REL_V + 7);
        sitio("osmund", OSMUND_U, RELICARIO + 1, OSMUND_V);
        sitio("alto", 30, PISO + 40, 50);
    }

    private static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;

    private void ponerEsperas() {
        int P = Ensamblado.PORTACIRIOS, S = Ensamblado.SEPULTURERO, K = Ensamblado.COLOSO;
        int R = Enemigo.REZA, D = Enemigo.DE_PIE, T = Enemigo.TUMBADO;
        // ---- el primero (puesto 0): Fray Osmund, de rodillas ante el relicario de San Ulfo, contando huesos, de espaldas a la
        // entrada (parte 7at)
        en(0, OSMUND_U, RELICARIO + 1, OSMUND_V, NORTE, R, 0, com.darkcraft.enemigos.Puestos.JEFE);
        // ---- el camposanto: los sepultureros cavando junto a las tumbas abiertas, dos montones entre las tumbas, el portacirios de
        // la puerta de la capilla y uno que ronda el camino
        for (int[] q : CAVAN) en(S, q[0] + 1, PISO, q[1] + 1, OESTE, R, 0, "camposanto");
        en(P, 9, PISO, 13, SUR, T, 0, "camposanto");
        en(S, -6, PISO, 19, SUR, T, 0, "camposanto");
        en(P, 2, PISO, 1, SUR, D, 0, "camposanto");
        en(P, 0, PISO, 12, SUR, D, 4, "camposanto");
        // ---- la capilla: uno rezando ante el altar, otro en los bancos y un monton en la nave
        en(P, 0, PISO, -15, NORTE, R, 0, "capilla");
        en(P, -3, PISO, -11, NORTE, R, 0, "capilla");
        en(S, 3, PISO, -7, SUR, T, 0, "capilla");
        // ---- la Cripta de los Hermanos
        en(P, 0, C1 + 1, -33, SUR, D, 3, "hermanos");
        en(S, -1, C1 + 1, -40, SUR, T, 0, "hermanos");
        en(S, 4, C1 + 1, -44, ESTE, R, 0, "hermanos");
        en(P, -4, C1 + 1, -36, OESTE, R, 0, "hermanos");
        en(P, 1, C1 + 1, -48, SUR, T, 0, "hermanos");
        // ---- la Capilla de los Huesos: el coloso guarda el altar; dos rezan a las piramides y dos montones
        en(K, 0, C2 + 1, V2N + 5, SUR, D, 0, "huesos");
        en(P, -2, C2 + 1, V2S - 3, OESTE, R, 0, "huesos");
        en(P, 2, C2 + 1, V2N + 3, ESTE, R, 0, "huesos");
        en(S, 4, C2 + 1, VX + 4, SUR, T, 0, "huesos");
        en(P, -4, C2 + 1, VX - 3, SUR, T, 0, "huesos");
        // ---- las catacumbas del oeste
        en(S, -16, C2 + 1, VX, ESTE, D, 4, "catacumbas");
        en(P, -25, C2 + 1, VX, ESTE, T, 0, "catacumbas");
        en(P, -31, C2 + 1, VX + 1, ESTE, D, 0, "catacumbas");
        // ---- el taller: uno a la mesa (ordenando) y otro rezando ante las calaveras de las estanterias
        en(S, -20, C2 + 1, -90, NORTE, R, 0, "taller");
        en(P, -23, C2 + 1, -96, NORTE, R, 0, "taller");
        // ---- el osario menor: un coloso entre los montones y dos montones mas
        en(K, -40, C2 + 1, VX, ESTE, D, 0, "osario_menor");
        en(S, -43, C2 + 1, -66, ESTE, T, 0, "osario_menor");
        en(P, -38, C2 + 1, -76, ESTE, T, 0, "osario_menor");
        // ---- la Sala del Pozo: dos portacirios, el sepulturero del fondo y el coloso que guarda el paso al Relicario
        en(P, 21, C2 + 1, -60, OESTE, D, 0, "pozo");
        en(P, 32, C2 + 1, -69, OESTE, R, 0, "pozo");
        en(S, 27, POZO + 1, POZO_V0 + 1, SUR, R, 0, "pozo");
        en(K, 26, C2 + 1, -78, SUR, D, 0, "pozo");
        // ---- el pasillo del Relicario
        en(P, 26, RELICARIO + 1, -94, SUR, D, 0, "relicario");
    }

    private void en(int variante, int u, int y, int v, float giro, int postura, int ronda, String grupo) {
        esperas.add(new Espera(variante, x(u), y, z(v), giro, postura, ronda, grupo));
    }

    // ------------------------------------------------------------------ lo que pasa: la campana, los susurros, la escotilla

    /** Para las pruebas: las veces que ha sonado la campana rota y los susurros de la Capilla de los Huesos. */
    public static int CAMPANADAS, SUSURROS;

    /** De vez en cuando (lo llama {@link Morvain} cada cinco segundos): la campana rota del campanario tañe sola. */
    public static void campana(ServerLevel m) {
        BlockPos c = campana();
        boolean alguien = false;
        for (ServerPlayer p : m.players()) if (!p.isSpectator() && p.distanceToSqr(c.getX(), c.getY(), c.getZ()) < 140 * 140) alguien = true;
        if (!alguien || m.random.nextInt(6) != 0 || !m.isLoaded(c)) return;
        tocar(m);
    }

    /** La campana tañe (una vez): para el comando y las pruebas. */
    public static void tocar(ServerLevel m) {
        BlockPos c = campana();
        m.playSound(null, c.getX() + 0.5, c.getY() + 0.5, c.getZ() + 0.5, DarkCraftMod.SFX_OSARIO_CAMPANA.get(), SoundSource.AMBIENT, 6.0f,
                0.95f + m.random.nextFloat() * 0.1f);
        m.sendParticles(ParticleTypes.ASH, c.getX() + 0.5, c.getY() + 0.5, c.getZ() + 0.5, 24, 1.2, 0.6, 1.2, 0.01);
        CAMPANADAS++;
    }

    /** Cada pocos segundos: quien esta en la Capilla de los Huesos oye, a veces, a los cuarenta mil susurrar sus nombres. */
    public static void susurros(ServerLevel m) {
        for (ServerPlayer p : m.players()) {
            if (p.isSpectator() || !enHuesos((int) Math.floor(p.getX()) - CX, p.getY(), (int) Math.floor(p.getZ()) - CZ)) continue;
            if (m.random.nextInt(4) != 0) continue;
            m.playSound(null, x(0) + 0.5, C2 + 5, z(VX) + 0.5, DarkCraftMod.SFX_OSARIO_SUSURROS.get(), SoundSource.AMBIENT, 1.6f, 1.0f);
            SUSURROS++;
            return;
        }
    }

    /** Quien toca la escotilla del panteon por arriba: esta cerrada por debajo (la palanca esta dentro de la chimenea). */
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

    public static boolean escotillaAbierta(ServerLevel m) {
        var st = m.getBlockState(new BlockPos(x(ESCALA_U), SUELO, z(ESCALA_V)));
        return st.is(Blocks.IRON_TRAPDOOR) && st.getValue(TrapDoorBlock.OPEN);
    }

    /** Lo que hay en el osario ya generado (para la prueba): la escotilla, la palanca, la escala, el atril, los arcones, el muro... */
    public static String resumen(ServerLevel m) {
        boolean libro = m.getBlockEntity(new BlockPos(x(ATRIL_U), C2 + 1, z(ATRIL_V))) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le
                && Perdidos.de(le.getBook()) == LIBRO;
        var tr = m.getBlockState(new BlockPos(x(ESCALA_U), SUELO, z(ESCALA_V)));
        var pa = m.getBlockState(new BlockPos(x(ESCALA_U + 1), SUELO - 1, z(ESCALA_V)));
        int escala = 0;
        for (int y = C2 + 1; y <= SUELO - 1; y++) if (m.getBlockState(new BlockPos(x(ESCALA_U), y, z(ESCALA_V))).is(Blocks.LADDER)) escala++;
        int muro = 0;
        for (BlockPos q : muro(MURO_PRIORES)) if (!m.getBlockState(q).isAir()) muro++;
        boolean bueno = m.getBlockState(new BlockPos(x(-28), C2 + 1, z(-48))).is(Blocks.CHEST);
        boolean campana = m.getBlockState(campana()).is(Blocks.BELL);
        int huesosLampara = 0;
        for (int k = -3; k <= 3; k++) if (m.getBlockState(new BlockPos(x(k), techoHuesos(0) - 4, z(VX))).is(Blocks.BONE_BLOCK)) huesosLampara++;
        return "escotilla " + (tr.is(Blocks.IRON_TRAPDOOR) ? (tr.getValue(TrapDoorBlock.OPEN) ? "abierta" : "cerrada") : "NO") + ", palanca "
                + (pa.is(Blocks.LEVER) ? "SI" : "NO") + ", escala " + escala + "/" + (SUELO - 1 - C2) + ", atril con el libro " + LIBRO + " "
                + (libro ? "SI" : "NO") + ", arcon bueno " + (bueno ? "SI" : "NO") + ", muro de los priores " + muro + "/" + muro(MURO_PRIORES).size()
                + ", campana " + (campana ? "SI" : "NO") + ", lampara de huesos " + huesosLampara + "/7";
    }

    /** El nombre del sitio del osario donde esta (x, y, z) (null: fuera). */
    public static String lugar(double x, double y, double z) {
        int u = (int) Math.floor(x) - CX, v = (int) Math.floor(z) - CZ;
        if (x < X0 || x > X1 + 1 || z < Z0 || z > Z1 + 1) return null;
        if (y < SUELO - 2) {
            if (y < POZO - 3) return null;
            if (enRelicario(u, y, v)) return "darkcraft.morvain.lugar.relicario";
            if (enHuesos(u, y, v)) return "darkcraft.morvain.lugar.capilla_huesos";
            if (enSalaPozo(u, y, v)) return "darkcraft.morvain.lugar.pozo_huesos";
            if (enTaller(u, y, v)) return "darkcraft.morvain.lugar.taller_sacristan";
            return "darkcraft.morvain.lugar.osario";
        }
        if (y > SUELO + 30) return null;
        if (enNave(u, v) || u >= 5 && u <= 9 && v >= -6 && v <= -2) return "darkcraft.morvain.lugar.capilla_ulfo";
        if (u >= -23 && u <= 23 && v >= -27 && v <= 25) return "darkcraft.morvain.lugar.camposanto";
        return null;
    }

    // ------------------------------------------------------------------ Fray Osmund, el Sacristan (parte 7at)

    /** Si (x, y, z) esta en el Relicario (donde pelea Fray Osmund). */
    public static boolean enRelicario(double x, double y, double z) {
        return enRelicario((int) Math.floor(x) - CX, y, (int) Math.floor(z) - CZ);
    }

    /** Donde se levantan los muertos cuando Fray Osmund toca la campanilla (delante de cada monton de huesos del borde): {x, z}. */
    public static List<int[]> muertos() {
        List<int[]> l = new ArrayList<>();
        for (int a : MONTONES) {
            double r = Math.toRadians(a);
            l.add(new int[]{CX + REL_U + (int) Math.round(R_MUERTO * Math.cos(r)), CZ + REL_V + (int) Math.round(R_MUERTO * Math.sin(r))});
        }
        return l;
    }

    /** El aro de la lampara de la cupula del Relicario (en el mundo): {x, y, z} del centro. */
    public static double[] lampara() {
        return new double[]{x(REL_U) + 0.5, cupula(0) - 4.5, z(REL_V) + 0.5};
    }

    /** Si Fray Osmund ha caido (y no ha vuelto: es del grupo de los jefes). */
    public static boolean enPaz(net.minecraft.server.MinecraftServer srv) {
        return com.darkcraft.enemigos.Puestos.muerto(srv, MorvainEnemigos.OSARIO, MorvainEnemigos.osmund());
    }

    /** Para las pruebas: los Ensamblados que han descansado al caer Fray Osmund. */
    public static int DESCANSAN;

    /**
     * Cada segundo: con Fray Osmund caido, los muertos del osario descansan: los Ensamblados que no estan peleando se deshacen (sube su
     * alma) y ya no vuelven ({@code MorvainEnemigos}: sus puestos dejan de estar en uso).
     */
    public static void paz(ServerLevel m) {
        if (com.darkcraft.enemigos.Puestos.sitio(MorvainEnemigos.OSARIO) == null || !enPaz(m.getServer())) return;
        List<com.darkcraft.enemigos.Puestos.Puesto> ps = MorvainEnemigos.osario();
        for (int i = 0; i < ps.size(); i++) {
            if (com.darkcraft.enemigos.Puestos.JEFE.equals(ps.get(i).grupo())) continue;
            Enemigo e = com.darkcraft.enemigos.Puestos.enemigo(MorvainEnemigos.OSARIO, i);
            if (e == null || e.level() != m || e.getTarget() != null || e.accion() != 0) continue;
            m.sendParticles(ParticleTypes.END_ROD, e.getX(), e.getY() + 1.2, e.getZ(), 0, 0, 1, 0, 0.08);
            m.sendParticles(ParticleTypes.SOUL, e.getX(), e.getY() + 1.2, e.getZ(), 8, 0.3, 0.6, 0.3, 0.03);
            e.disolver();
            DESCANSAN++;
        }
    }

    /** Si (x, y, z) esta abajo, en la cripta (para su logro y el aire del osario). */
    public static boolean dentro(double x, double y, double z) {
        int u = (int) Math.floor(x) - CX, v = (int) Math.floor(z) - CZ;
        return y < SUELO - 2 && y > POZO - 2 && u >= X0 - CX && u <= X1 - CX && v >= Z0 - CZ && v <= -24;
    }
}
