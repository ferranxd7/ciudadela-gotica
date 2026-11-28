package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Insomne;
import com.darkcraft.fortaleza.Perdidos;
import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Pal;
import com.darkcraft.pueblo.gen.Trozo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * El Fuerte de la Marca (parte 7aw; respuesta 131 del plan: "fuertes y campamentos"), en las Llanuras Palidas. La noche en que la
 * Peste Gris vacio los pueblos de la llanura, los que huian llegaron a la puerta del fuerte pidiendo asilo; el castellano mando alzar
 * el puente y que nadie abriera. Acamparon al pie del glacis. Cuando quisieron derribar la puerta con un ariete, la guarnicion
 * quemo el campamento desde las murallas. Luego llego el cielo rojo, y nadie vino nunca a relevar a la guardia: los Insomnes
 * ({@link Insomne}) siguen en sus puestos, con los ojos abiertos. u = x - {@link #CX}, v = z - {@link #CZ}; m = max(|u|, |v|).
 * <ul>
 * <li>El fuerte, cuadrado, sobre un monticulo allanado: el <b>patio de armas</b> (m &lt;= {@link #PATIO}), la <b>muralla</b> con su adarve
 * y sus almenas (m {@link #MURO0}..{@link #MURO1}), cuatro <b>torres redondas</b> en las esquinas, el <b>foso seco</b> (m
 * {@link #FOSO0}..{@link #FOSO1}, el fondo en {@link #FOSO}) y la <b>berma</b> de fuera; al sur, el <b>cuerpo de la puerta</b> con el
 * <b>rastrillo</b> y el <b>puente levadizo</b> alzado (la <b>sala del torno</b> encima: la palanca lo baja y sube el rastrillo, y el
 * atajo queda abierto para siempre; el <b>libro perdido 39</b>, "El registro de la puerta").</li>
 * <li>Para entrar con el puente alzado: bajar al foso por los <b>escombros</b> del suroeste, rodear hasta la <b>poterna</b> de la torre
 * del noroeste, el <b>tunel</b> hasta las <b>mazmorras</b> bajo la torre del homenaje (las celdas, el almacen; tras un <b>muro
 * ilusorio</b>, la <b>sala de la paga</b>) y subir a la planta baja del homenaje.</li>
 * <li>El patio: la <b>torre del homenaje</b> (al norte: la sala de armas, la <b>Sala del Castellano</b> —parte 7ax— y la azotea), el
 * <b>campanario de rebato</b>, el <b>cuartel</b> (jergones: en Morvain no hay camas), el <b>comedor</b> y la cocina, la <b>capilla</b>, las
 * <b>cuadras</b> y la <b>fragua</b>, el <b>pozo</b>, el <b>fundibulo</b>, la <b>horca</b> y escaleras al adarve.</li>
 * <li>Fuera, al sur, el <b>campamento quemado</b> de los refugiados (tiendas, carros, la fosa), la barricada de la guarnicion y el
 * <b>ariete</b> abandonado ante el puente; el santuario ("El Fuerte de la Marca") al final del camino.</li>
 * </ul>
 * Los Insomnes que tocan el cuerno llaman a <b>rebato</b> ({@link #rebato}): suena la campana y la guarnicion acude.
 */
public final class Fuerte {
    public static final long SEMILLA = 0x0F0E_57E0_3A2CL;
    /** El centro del patio. */
    public static final int CX = 2250, CZ = 3560;
    /** El suelo del patio (el bloque que se pisa), el del foso, el del adarve y lo alto de las torres (sus suelos). */
    public static final int SUELO = 78, FOSO = 72, A = SUELO + 10, T = SUELO + 16;
    /** El patio, la muralla, el foso y la berma (en m = max(|u|, |v|)). */
    public static final int PATIO = 25, MURO0 = 26, MURO1 = 28, FOSO0 = 29, FOSO1 = 35, BERMA = 40;
    /** Las torres de las esquinas: radios de fuera y de dentro; sus centros (NO, NE, SO, SE). */
    public static final double RTO = 5.5, RTI = 3.5;
    static final int[][] TORRES = {{-27, -27}, {27, -27}, {-27, 27}, {27, 27}};
    /** La puerta: el paso (|u| &lt;= PASO), el rastrillo (en v = RV) y el puente (v PV0..PV1); el cuerpo de la puerta. */
    public static final int PASO = 2, RV = 27, PV0 = 29, PV1 = 35;
    static final int GX = 8, GV0 = 18, GV1 = 28;
    /** La torre del homenaje (|u| &lt;= KX, v KV0..KV1) y sus suelos: el sotano, la planta baja, la Sala del Castellano y la azotea. */
    public static final int KX = 9, KV0 = -25, KV1 = -7;
    public static final int P0 = SUELO, P1 = SUELO + 8, AZOTEA = SUELO + 18;
    /** El santuario del fuerte (en {@link SantuariosMorvain}): al sur, al final del camino que cruza el campamento. */
    public static final int SANT_U = 0, SANT_V = 100;
    public static final int X0 = CX - 64, X1 = CX + 64, Z0 = CZ - 64, Z1 = CZ + 108;
    /** Los chunks donde {@link Adorno} no pone nada. */
    static final int AX0 = CX - 62, AX1 = CX + 62, AZ0 = CZ - 62, AZ1 = CZ + 96;

    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/fuerte"),
            BOTIN_BUENO = new ResourceLocation(DarkCraftMod.MODID, "chests/fuerte_bueno");
    /** El muro ilusorio de las mazmorras (detras, la sala de la paga). */
    public static final String MURO_PAGA = "fuerte_paga";
    /** El libro perdido de la sala del torno. */
    public static final int LIBRO = 39;
    static final int ATRIL_U = -5, ATRIL_V = 21;
    /** La palanca del torno (en la sala de encima de la puerta, en el pilar del torno). */
    public static final int PALANCA_U = 0, PALANCA_V = 21;
    /** La campana de rebato (en lo alto del campanario). */
    static final int CAMP_U = -15, CAMP_V = -22, CAMP_Y = SUELO + 17;
    /** El rebato: lo que dura (ticks) y cada cuanto vuelve a sonar la campana. */
    public static final int REBATO = 45 * 20, CAMPANADA = 150;

    /** Uno del fuerte: donde espera (y: donde tiene los pies), hacia donde mira, su variante, como espera, cuanto ronda y su grupo. */
    public record Espera(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    private static Fuerte unico;

    public static synchronized Fuerte get() {
        if (unico == null) unico = new Fuerte();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    private final Mina.Plano plano = new Mina.Plano();
    /** Lo que hay (para la prueba y las vistas). */
    public int almenas, jergones, tiendas, carros, huesos, faroles, bancos;

    private Fuerte() {
        elementos.add(new Obra());
        murallas();
        torres();
        puerta();
        homenaje();
        sotanos();
        tunel();
        paga();
        escaleras();
        campanario();
        cuartel();
        comedor();
        capilla();
        cuadras();
        pozo();
        fundibulo();
        horca();
        patio();
        foso();
        berma();
        campamento();
        elementos.add(plano.pieza(4, X0, Z0, X1, Z1));
        trozos();
        ponerSitios();
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo del fuerte (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en el fuerte o el campamento: ahi {@link Adorno} no pone nada. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= AX0 && x0 <= AX1 && z0 + 15 >= AZ0 && z0 <= AZ1;
    }

    /** Lo lejos (en m, la distancia "cuadrada") de (x, z) al centro del patio. */
    public static double distancia(double x, double z) {
        return Math.max(Math.abs(x - CX - 0.5), Math.abs(z - CZ - 0.5));
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} del fuerte en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los del fuerte (los apunta {@link MorvainEnemigos}). */
    public List<Espera> esperas() {
        return esperas;
    }

    /** Los sitios con nombre: {x, y, z} (y: los pies). */
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

    /** La distancia "cuadrada" de (u, v) al centro. */
    static int m(int u, int v) {
        return Math.max(Math.abs(u), Math.abs(v));
    }

    /** La torre de la esquina en la que cae (u, v) (dentro de su radio de fuera), o -1. */
    static int torre(int u, int v) {
        for (int k = 0; k < TORRES.length; k++)
            if (Math.hypot(u - TORRES[k][0], v - TORRES[k][1]) < RTO) return k;
        return -1;
    }

    /** Si (u, v) cae en el cuerpo de la puerta. */
    static boolean enPuerta(int u, int v) {
        return Math.abs(u) <= GX && v >= GV0 && v <= GV1;
    }

    /** Si (u, v) cae en la torre del homenaje. */
    static boolean enHomenaje(int u, int v) {
        return Math.abs(u) <= KX && v >= KV0 && v <= KV1;
    }

    static BlockState escalon(Block k, Direction f) {
        return Mina.escalon(k, f);
    }

    static BlockState escalonAlto(Block k, Direction f) {
        return Mina.escalonAlto(k, f);
    }

    static BlockState losa(Block k, SlabType t) {
        return Mina.losa(k, t);
    }

    static BlockState trampilla(Block k, Direction f, boolean arriba, boolean abierta) {
        return k.defaultBlockState().setValue(TrapDoorBlock.FACING, f).setValue(TrapDoorBlock.HALF, arriba ? Half.TOP : Half.BOTTOM)
                .setValue(TrapDoorBlock.OPEN, abierta);
    }

    static BlockState calavera(int giro) {
        return Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, Math.floorMod(giro, 16));
    }

    static BlockState hoguera(boolean lit) {
        return Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, lit);
    }

    static BlockState estandarte(Direction f) {
        return Blocks.YELLOW_WALL_BANNER.defaultBlockState().setValue(WallBannerBlock.FACING, f);
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

    private void caja(int u0, int y0, int v0, int u1, int y1, int v1, BlockState s) {
        for (int u = Math.min(u0, u1); u <= Math.max(u0, u1); u++)
            for (int v = Math.min(v0, v1); v <= Math.max(v0, v1); v++)
                for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) put(u, y, v, s);
    }

    private void hueco(int u0, int y0, int v0, int u1, int y1, int v1) {
        for (int u = Math.min(u0, u1); u <= Math.max(u0, u1); u++)
            for (int v = Math.min(v0, v1); v <= Math.max(v0, v1); v++)
                for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) aire(u, y, v);
    }

    /** Si en (u, y, v) hay escrito aire (un hueco: ahi caben muebles). */
    private boolean libre(int u, int y, int v) {
        BlockState s = plano.en(x(u), y, z(v));
        return s != null && s.isAir();
    }

    private void enLibre(int u, int y, int v, BlockState s) {
        if (libre(u, y, v)) put(u, y, v, s);
    }

    // ------------------------------------------------------------------ los materiales

    /** La piedra del fuerte: sillares claros y gastados, con musgo y grietas cerca del foso. */
    static BlockState piedra(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 6);
        if (y <= FOSO + 3 && p < 34) return p < 14 ? b(Blocks.MOSSY_STONE_BRICKS) : p < 24 ? b(Blocks.MOSSY_COBBLESTONE) : b(Blocks.CRACKED_STONE_BRICKS);
        return p < 40 ? b(Blocks.STONE_BRICKS) : p < 58 ? b(Blocks.CRACKED_STONE_BRICKS) : p < 66 ? b(Blocks.MOSSY_STONE_BRICKS)
                : p < 76 ? b(Blocks.ANDESITE) : p < 84 ? b(Blocks.COBBLESTONE) : p < 92 ? b(Blocks.POLISHED_ANDESITE) : b(Blocks.TUFF);
    }

    /** Lo que se pisa por arriba (el adarve, los suelos de las torres, los caminos del patio). */
    static BlockState losas(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 7);
        return p < 40 ? b(Blocks.STONE_BRICKS) : p < 62 ? b(Blocks.POLISHED_ANDESITE) : p < 78 ? b(Blocks.CRACKED_STONE_BRICKS)
                : p < 90 ? b(Blocks.COBBLESTONE) : b(Blocks.ANDESITE);
    }

    /** El suelo del patio de armas: tierra pisada, grava y barro. */
    static BlockState patioSuelo(int x, int y, int z) {
        double n = Mina.ruido(x * 0.31, z * 0.27) + 0.45 * Mina.ruido(x * 0.8 + 3, z * 0.9 - 7) + (Hash.f(x, y, z, SEMILLA + 8) - 0.5) * 0.6;
        return n < -0.75 ? b(Blocks.MUD) : n < -0.3 ? b(Blocks.PACKED_MUD) : n < 0.25 ? b(Blocks.COARSE_DIRT) : n < 0.6 ? b(Blocks.GRAVEL)
                : n < 0.9 ? b(Blocks.DIRT) : b(Blocks.COBBLESTONE);
    }

    /** El fondo del foso seco: barro seco, grava y huesos. */
    static BlockState fosoSuelo(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 9);
        return p < 30 ? b(Blocks.COARSE_DIRT) : p < 52 ? b(Blocks.PACKED_MUD) : p < 68 ? b(Blocks.GRAVEL) : p < 82 ? b(Blocks.DIRT)
                : p < 94 ? b(Blocks.MUD) : b(Blocks.BONE_BLOCK);
    }

    /** La hierba palida de la berma y el glacis (con calvas de tierra). */
    static BlockState hierba(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 10);
        return p < 70 ? b(Blocks.GRASS_BLOCK) : p < 88 ? b(Blocks.COARSE_DIRT) : b(Blocks.DIRT);
    }

    /** El suelo quemado del campamento: ceniza, tierra negra y brasas apagadas. */
    static BlockState quemado(int x, int y, int z) {
        double n = Mina.ruido(x * 0.23, z * 0.25) + (Hash.f(x, y, z, SEMILLA + 11) - 0.5) * 0.9;
        return n < -0.6 ? b(Blocks.SOUL_SOIL) : n < -0.15 ? b(Blocks.COARSE_DIRT) : n < 0.3 ? b(Blocks.ROOTED_DIRT) : n < 0.6 ? b(Blocks.GRAVEL)
                : n < 0.85 ? b(Blocks.PODZOL) : b(Blocks.GRASS_BLOCK);
    }

    // ------------------------------------------------------------------ el terreno: el monticulo, el foso, la berma y el glacis

    /** La altura natural del terreno (la de antes del fuerte). */
    static int alto(int x, int z) {
        return (int) Math.floor(Terreno.columna(x, z).altura());
    }

    /** Lo alto (el bloque de encima) del camino que baja de la berma al llano, en medios bloques (lo que se pisa por dos). */
    static int rampaMedia(int v) {
        return 2 * (SUELO + 1) - (v - BERMA);
    }

    /** El bloque entero de arriba de la rampa del camino en (u, v) (sus orillas, un poco mas abajo), o MIN_VALUE fuera de ella. */
    static int rampaTope(int u, int v) {
        if (v <= BERMA || Math.abs(u) > 4) return Integer.MIN_VALUE;
        int hb = rampaMedia(v);
        int lleno = hb % 2 == 0 ? hb / 2 - 1 : (hb - 1) / 2 - 1;
        return lleno - Math.max(0, Math.abs(u) - 2);
    }

    /** Lo alto (el bloque de arriba) de lo hecho en (u, v): el patio, el fondo del foso, la berma, el glacis; MIN_VALUE: lo natural. */
    static int superficie(int u, int v) {
        int m = m(u, v);
        if (m <= MURO1) return SUELO;
        if (m <= FOSO1) return FOSO;
        if (m <= BERMA) return SUELO;
        int t = alto(x(u), z(v));
        int g = SUELO - (int) Math.floor((m - BERMA) * 0.7);
        int h = Math.max(g, rampaTope(u, v));
        return h > t ? h : Integer.MIN_VALUE;
    }

    /** Lo que se pisa en (u, v): lo hecho o, fuera, el terreno natural. */
    static int sup(int u, int v) {
        int h = superficie(u, v);
        return h == Integer.MIN_VALUE ? alto(x(u), z(v)) : h;
    }

    private final class Obra extends Element {
        Obra() {
            super(SEMILLA + 51);
            this.layer = 1;
            bounds(X0, FOSO - 8, Z0, X1, 140, Z1);
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
        int h = superficie(u, v);
        if (h == Integer.MIN_VALUE) return;
        int t = alto(x, z);
        int m = m(u, v);
        for (int y = Math.min(t, h) - 3; y < h; y++) c.set(x, y, z, y < h - 3 ? b(Blocks.STONE) : b(Blocks.DIRT));
        BlockState top = m <= PATIO ? patioSuelo(x, h, z) : m <= MURO1 ? piedra(x, h, z) : m <= FOSO1 ? fosoSuelo(x, h, z) : hierba(x, h, z);
        c.set(x, h, z, top);
        for (int y = h + 1; y <= Math.max(t, h) + 6; y++) c.set(x, y, z, Pal.AIR);
    }

    // ------------------------------------------------------------------ la muralla y el adarve

    /** Lo que se cuenta a lo largo de la muralla en (u, v) (para las almenas): u en las de norte y sur, v en las de este y oeste. */
    static int aLoLargo(int u, int v) {
        return Math.abs(v) >= Math.abs(u) ? u : v;
    }

    /** Si en (u, v) del borde de fuera de la muralla hay hueco de almena (a la altura de la cabeza no hay merlon). */
    public static boolean almena(int u, int v) {
        return Math.floorMod(aLoLargo(u, v), 3) == 0;
    }

    private void murallas() {
        for (int u = -MURO1; u <= MURO1; u++)
            for (int v = -MURO1; v <= MURO1; v++) {
                int m = m(u, v);
                if (m < MURO0 || torre(u, v) >= 0 || enPuerta(u, v)) continue;
                for (int y = FOSO - 1; y < A; y++) put(u, y, v, piedra(x(u), y, z(v)));
                if (m <= MURO0 + 1) {
                    // el adarve: lo que se pisa y el aire de encima
                    put(u, A, v, losas(x(u), A, z(v)));
                    for (int y = A + 1; y <= A + 4; y++) aire(u, y, v);
                } else {
                    // el parapeto: a la cintura siempre; a la cabeza, merlones con huecos de almena
                    put(u, A, v, piedra(x(u), A, z(v)));
                    put(u, A + 1, v, piedra(x(u), A + 1, z(v)));
                    if (almena(u, v)) {
                        aire(u, A + 2, v);
                        almenas++;
                    } else put(u, A + 2, v, piedra(x(u), A + 2, z(v)));
                    for (int y = A + 3; y <= A + 4; y++) aire(u, y, v);
                    // aspilleras en la cara de fuera, a media altura
                    if (Math.floorMod(aLoLargo(u, v), 7) == 3 && Math.abs(aLoLargo(u, v)) < 20) aire(u, A - 5, v);
                }
            }
        // faroles en lo alto de algunos merlones y braseros en el adarve, junto al patio
        for (int k = -20; k <= 20; k += 10)
            for (int[] d : new int[][]{{k, MURO1}, {k, -MURO1}, {MURO1, k}, {-MURO1, k}}) {
                int u = d[0], v = d[1];
                if (almena(u, v)) u += Math.abs(v) == MURO1 ? 1 : 0;
                if (almena(u, v)) v += Math.abs(u) == MURO1 ? 1 : 0;
                if (enPuerta(u, v) || torre(u, v) >= 0) continue;
                put(u, A + 3, v, Mina.farol(false, false));
                faroles++;
            }
    }

    // ------------------------------------------------------------------ las torres de las esquinas

    /** Si la celda (dx, dz) del anillo de la torre es hueco de almena (por angulos de 22,5 grados). */
    static boolean almenaTorre(int dx, int dz) {
        double a = Math.toDegrees(Math.atan2(dz, dx));
        return Math.floorMod((int) Math.floor((a + 360) / 22.5), 2) == 1;
    }

    /** La escalera de mano de cada torre: la celda (respecto al centro) y hacia donde mira. */
    static int[] escalaTorre(int k) {
        int sx = Integer.signum(TORRES[k][0]), sz = Integer.signum(TORRES[k][1]);
        return new int[]{2 * sx, 2 * sz};
    }

    static Direction miraEscala(int k) {
        return TORRES[k][0] < 0 ? Direction.EAST : Direction.WEST;
    }

    private void torres() {
        for (int k = 0; k < TORRES.length; k++) {
            int tx = TORRES[k][0], tz = TORRES[k][1];
            int sx = Integer.signum(tx), sz = Integer.signum(tz);
            boolean noroeste = k == 0;
            int[] es = escalaTorre(k);
            for (int dx = -6; dx <= 6; dx++)
                for (int dz = -6; dz <= 6; dz++) {
                    double r = Math.hypot(dx, dz);
                    if (r >= RTO) continue;
                    int u = tx + dx, v = tz + dz;
                    boolean dentro = r < RTI;
                    for (int y = FOSO - 1; y <= T; y++) {
                        if (!dentro) {
                            put(u, y, v, piedra(x(u), y, z(v)));
                            continue;
                        }
                        boolean suelo = y == SUELO || y == A || y == T || (noroeste && y == FOSO);
                        boolean sala = (y > SUELO && y < A) || (y > A && y < T) || (noroeste && y > FOSO && y < SUELO);
                        if (suelo) put(u, y, v, losas(x(u), y, z(v)));
                        else if (sala) aire(u, y, v);
                        else put(u, y, v, piedra(x(u), y, z(v)));
                    }
                    // la azotea: el parapeto en el anillo de fuera
                    if (r >= RTO - 1.0) {
                        put(u, T + 1, v, piedra(x(u), T + 1, z(v)));
                        if (almenaTorre(dx, dz)) aire(u, T + 2, v);
                        else put(u, T + 2, v, piedra(x(u), T + 2, z(v)));
                    } else for (int y = T + 1; y <= T + 3; y++) aire(u, y, v);
                    // la puerta de abajo: hacia el patio (en diagonal, dos de ancho)
                    int ax = -sx * dx, az = -sz * dz;
                    if (!dentro && ax >= 2 && az >= 2 && Math.abs(ax - az) <= 1 && m(u, v) <= PATIO + 1)
                        for (int y = SUELO + 1; y <= SUELO + 3; y++) aire(u, y, v);
                    // las del adarve: donde el anillo cae en lo que se pisa de la muralla
                    int mm = m(u, v);
                    if (!dentro && mm >= MURO0 && mm <= MURO0 + 1 && !enPuerta(u, v))
                        for (int y = A + 1; y <= A + 2; y++) aire(u, y, v);
                    // aspilleras de la sala de arriba, hacia fuera
                    if (!dentro && r >= RTO - 1.2 && sx * dx >= 3 && sz * dz >= 3 && Math.abs(dx) + Math.abs(dz) == 7) aire(u, A + 3, v);
                }
            // la escalera de mano, de abajo a la azotea (los suelos, con su agujero)
            int lu = tx + es[0], lv = tz + es[1];
            for (int y = (noroeste ? FOSO + 1 : SUELO + 1); y <= T; y++) put(lu, y, lv, Mina.escala(miraEscala(k)));
            // un brasero en la azotea y un farol colgado en cada sala
            put(tx - sx, T + 1, tz - sz, hoguera(true));
            put(tx, A - 1, tz, Mina.farol(false, true));
            put(tx, T - 1, tz, Mina.farol(false, true));
            if (noroeste) put(tx + sx, SUELO - 1, tz + sz, Mina.farol(true, true));
            faroles += noroeste ? 3 : 2;
        }
        // la poterna: en la cara del oeste de la torre del noroeste, a la altura del foso
        int[] no = TORRES[0];
        for (int dx = -6; dx <= -3; dx++)
            for (int dz = 0; dz <= 1; dz++)
                for (int y = FOSO + 1; y <= FOSO + 2; y++) aire(no[0] + dx, y, no[1] + dz);
        put(no[0] - 6, FOSO + 3, no[1] - 1, b(Blocks.CHAIN));
        // telaranas en la poterna y en la sala de abajo
        put(no[0] - 3, FOSO + 2, no[1] + 1, b(Blocks.COBWEB));
        put(no[0] + 2, FOSO + 4, no[1] - 1, b(Blocks.COBWEB));
        put(no[0] - 1, FOSO + 1, no[1] + 2, b(Blocks.BONE_BLOCK));
        put(no[0] - 2, FOSO + 1, no[1] + 2, calavera(3));
    }

    // ------------------------------------------------------------------ la puerta: el paso, el rastrillo, el puente y la sala del torno

    private void puerta() {
        // el cuerpo de la puerta, macizo, de abajo del foso a su azotea
        for (int u = -GX; u <= GX; u++)
            for (int v = GV0; v <= GV1; v++) {
                for (int y = FOSO - 1; y <= T; y++) put(u, y, v, piedra(x(u), y, z(v)));
                boolean borde = Math.abs(u) == GX || v == GV0 || v == GV1;
                if (borde) {
                    put(u, T + 1, v, piedra(x(u), T + 1, z(v)));
                    if (Math.floorMod(v == GV0 || v == GV1 ? u : v, 3) == 0) aire(u, T + 2, v);
                    else put(u, T + 2, v, piedra(x(u), T + 2, z(v)));
                } else {
                    put(u, T, v, losas(x(u), T, z(v)));
                    for (int y = T + 1; y <= T + 3; y++) aire(u, y, v);
                }
            }
        // el paso: de lado a lado del cuerpo de la puerta (seis de alto)
        for (int u = -PASO; u <= PASO; u++)
            for (int v = GV0; v <= GV1; v++) {
                put(u, SUELO, v, losas(x(u), SUELO, z(v)));
                for (int y = SUELO + 1; y <= SUELO + 6; y++) aire(u, y, v);
            }
        // el arco de fuera (las esquinas de arriba) y antorchas de almas a los lados del paso
        for (int s : new int[]{-1, 1}) {
            put(s * PASO, SUELO + 6, GV1, escalonAlto(Blocks.STONE_BRICK_STAIRS, s < 0 ? Direction.WEST : Direction.EAST));
            put(s * PASO, SUELO + 6, GV0, escalonAlto(Blocks.STONE_BRICK_STAIRS, s < 0 ? Direction.WEST : Direction.EAST));
            put(s * (PASO + 1), SUELO + 4, GV0 + 2, Mina.farol(false, false));
        }
        // el rastrillo, bajado (y su ranura de encima, en la piedra)
        for (int u = -PASO; u <= PASO; u++) {
            for (int y = SUELO + 1; y <= SUELO + 6; y++) une(u, y, RV, b(Blocks.IRON_BARS));
            for (int y = SUELO + 7; y <= SUELO + 12; y++) aire(u, y, RV);
        }
        // el puente levadizo, alzado (de pie contra la cara de fuera)
        for (BlockPos p : puente(0)) put(p.getX() - CX, p.getY(), p.getZ() - CZ, tabla());
        // la sala del torno, encima del paso (y los dos recodos que llevan a las puertas del adarve)
        hueco(-GX + 1, A + 1, GV0 + 1, GX - 1, A + 5, GV1 - 3);
        for (int u = -GX + 1; u <= GX - 1; u++) {
            if (Math.abs(u) < 3) continue;
            hueco(u, A + 1, GV1 - 2, u, A + 5, GV1 - 2);
        }
        for (int u = -GX + 1; u <= GX - 1; u++)
            for (int v = GV0 + 1; v <= GV1 - 2; v++) if (Math.abs(u) >= 3 || v <= GV1 - 3) put(u, A, v, b(Blocks.SPRUCE_PLANKS));
        for (int s : new int[]{-GX, GX}) hueco(s, A + 1, GV1 - 2, s, A + 2, GV1 - 2);
        // del patio a la sala: la puerta de abajo y la escalera de mano (pegada al muro del oeste); y de la sala a la azotea
        hueco(-GX + 1, SUELO + 1, GV0, -GX + 1, SUELO + 2, GV0 + 1);
        for (int y = SUELO + 1; y <= A; y++) put(-GX + 1, y, GV0 + 2, Mina.escala(Direction.EAST));
        for (int y = A + 1; y <= T; y++) put(GX - 1, y, GV0 + 2, Mina.escala(Direction.WEST));
        // el torno: el tambor, sus soportes, las cadenas que bajan a la ranura y el pilar con la palanca
        for (int u = -2; u <= 2; u++) put(u, A + 2, GV1 - 4, Mina.tronco(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.X));
        for (int s : new int[]{-3, 3}) {
            put(s, A + 1, GV1 - 4, b(Blocks.SPRUCE_FENCE));
            put(s, A + 2, GV1 - 4, b(Blocks.GRINDSTONE));
        }
        for (int u : new int[]{-1, 1}) put(u, A + 1, GV1 - 3, Mina.cadena(Direction.Axis.Y));
        for (int y = A + 1; y <= A + 5; y++) put(PALANCA_U, y, PALANCA_V - 1, b(Blocks.POLISHED_DEEPSLATE));
        put(PALANCA_U, A + 2, PALANCA_V, palancaEstado(false));
        // el registro de la puerta: el atril, la mesa, el brasero, barriles y estandartes
        put(ATRIL_U + 1, A + 1, ATRIL_V, escalon(Blocks.SPRUCE_STAIRS, Direction.WEST));
        put(-6, A + 1, 23, b(Blocks.BARREL));
        put(-6, A + 2, 23, b(Blocks.BARREL));
        put(6, A + 1, 22, b(Blocks.BARREL));
        put(5, A + 1, 20, hoguera(true));
        put(-3, A + 1, 19, trampilla(Blocks.SPRUCE_TRAPDOOR, Direction.SOUTH, true, false));
        put(-2, A + 1, 19, trampilla(Blocks.SPRUCE_TRAPDOOR, Direction.SOUTH, true, false));
        put(-2, A + 2, 19, Lazareto.vela(2, true));
        put(0, A + 5, 23, Mina.farol(false, true));
        put(-4, A + 3, GV0 + 1, estandarte(Direction.SOUTH));
        put(4, A + 3, GV0 + 1, estandarte(Direction.SOUTH));
        // dos braseros en la azotea de la puerta
        put(-4, T + 1, 23, hoguera(true));
        put(4, T + 1, 23, hoguera(true));
    }

    /** La palanca del torno, en el pilar (mira al sur). */
    static BlockState palancaEstado(boolean bajada) {
        return Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.WALL).setValue(LeverBlock.FACING, Direction.SOUTH)
                .setValue(LeverBlock.POWERED, bajada);
    }

    static BlockState tabla() {
        return b(Blocks.DARK_OAK_PLANKS);
    }

    /**
     * Las celdas del puente levadizo en cada momento: 0 alzado (de pie contra la cara de fuera), 1 y 2 bajando, 3 bajado (el tablero
     * sobre el foso, de la puerta a la berma). En el mundo.
     */
    public static List<BlockPos> puente(int fase) {
        int[][] f = switch (fase) {
            case 0 -> new int[][]{{0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, {0, 6}, {0, 7}};
            case 1 -> new int[][]{{0, 0}, {0, 1}, {1, 2}, {1, 3}, {2, 4}, {2, 5}, {3, 6}};
            case 2 -> new int[][]{{0, 0}, {1, 0}, {2, 1}, {3, 1}, {4, 2}, {5, 2}, {6, 3}};
            default -> new int[][]{{0, 0}, {1, 0}, {2, 0}, {3, 0}, {4, 0}, {5, 0}, {6, 0}};
        };
        List<BlockPos> l = new ArrayList<>();
        for (int u = -PASO; u <= PASO; u++) for (int[] c : f) l.add(new BlockPos(x(u), SUELO + c[1], z(PV0 + c[0])));
        return l;
    }

    // ------------------------------------------------------------------ la torre del homenaje

    private void homenaje() {
        for (int u = -KX; u <= KX; u++)
            for (int v = KV0; v <= KV1; v++) {
                boolean muro = Math.abs(u) >= KX - 1 || v <= KV0 + 1 || v >= KV1 - 1;
                for (int y = FOSO - 1; y <= AZOTEA; y++) {
                    if (muro) {
                        put(u, y, v, piedra(x(u), y, z(v)));
                        continue;
                    }
                    if (y == FOSO) put(u, y, v, b(Blocks.COBBLESTONE));
                    else if (y > FOSO && y < SUELO - 1) aire(u, y, v);
                    else if (y == SUELO - 1) put(u, y, v, b(Blocks.STONE_BRICKS));
                    else if (y == P0) put(u, y, v, Hash.pct(x(u), y, z(v), SEMILLA + 21) < 70 ? b(Blocks.SPRUCE_PLANKS) : b(Blocks.STRIPPED_SPRUCE_LOG));
                    else if (y > P0 && y < P1) aire(u, y, v);
                    else if (y == P1) put(u, y, v, b(Blocks.DARK_OAK_PLANKS));
                    else if (y > P1 && y < AZOTEA) aire(u, y, v);
                    else put(u, y, v, losas(x(u), y, z(v)));
                }
                // la azotea: el parapeto alrededor y el aire de encima
                boolean borde = Math.abs(u) == KX || v == KV0 || v == KV1;
                if (borde) {
                    put(u, AZOTEA + 1, v, piedra(x(u), AZOTEA + 1, z(v)));
                    if (Math.floorMod(v == KV0 || v == KV1 ? u : v, 3) == 0) aire(u, AZOTEA + 2, v);
                    else put(u, AZOTEA + 2, v, piedra(x(u), AZOTEA + 2, z(v)));
                } else for (int y = AZOTEA + 1; y <= AZOTEA + 4; y++) aire(u, y, v);
            }
        // la puerta del patio (al sur), con su arco
        hueco(-1, P0 + 1, KV1 - 1, 1, P0 + 4, KV1);
        put(-1, P0 + 4, KV1, escalonAlto(Blocks.STONE_BRICK_STAIRS, Direction.WEST));
        put(1, P0 + 4, KV1, escalonAlto(Blocks.STONE_BRICK_STAIRS, Direction.EAST));
        put(-2, P0 + 3, KV1 + 1, Mina.farol(false, false));
        put(2, P0 + 3, KV1 + 1, Mina.farol(false, false));
        // aspilleras de la planta baja y ventanales con rejas en la Sala del Castellano
        for (int s : new int[]{-KX, KX}) {
            for (int v : new int[]{-20, -16, -12}) {
                aire(s, P0 + 3, v);
                aire(s - Integer.signum(s), P0 + 3, v);
            }
            for (int v : new int[]{-20, -12})
                for (int y = P1 + 3; y <= P1 + 6; y++) {
                    une(s, y, v, b(Blocks.IRON_BARS));
                    aire(s - Integer.signum(s), y, v);
                }
        }
        for (int u : new int[]{-4, 4})
            for (int y = P1 + 3; y <= P1 + 6; y++) {
                une(u, y, KV1, b(Blocks.IRON_BARS));
                aire(u, y, KV1 - 1);
            }
        // de la planta baja a la Sala del Castellano: la escalera pegada al muro del oeste (sube hacia el norte)
        for (int i = 0; i < 8; i++) {
            int v = -10 - i;
            for (int u = -7; u <= -6; u++) {
                put(u, P0 + 1 + i, v, escalon(Blocks.STONE_BRICK_STAIRS, Direction.NORTH));
                for (int y = P0 + 1; y < P0 + 1 + i; y++) put(u, y, v, b(Blocks.STONE_BRICKS));
                if (i >= 2 && i < 7) aire(u, P1, v);
            }
        }
        for (int v = -16; v <= -12; v++) une(-5, P1 + 1, v, b(Blocks.DARK_OAK_FENCE));
        une(-7, P1 + 1, -11, b(Blocks.DARK_OAK_FENCE));
        une(-6, P1 + 1, -11, b(Blocks.DARK_OAK_FENCE));
        // de la Sala del Castellano a la azotea: la escalera de mano de la esquina del noreste
        for (int y = P1 + 1; y <= AZOTEA; y++) put(7, y, -23, Mina.escala(Direction.WEST));
        // ---- la planta baja: la sala de armas
        for (int v : new int[]{-22, -18, -14}) {
            put(7, P0 + 1, v, b(Blocks.BARREL));
            put(7, P0 + 2, v, b(Blocks.SPRUCE_FENCE));
            une(7, P0 + 3, v, b(Blocks.SPRUCE_FENCE));
        }
        for (int u = -2; u <= 2; u++) put(u, P0 + 1, -16, trampilla(Blocks.SPRUCE_TRAPDOOR, Direction.NORTH, true, false));
        put(-1, P0 + 2, -16, Lazareto.vela(3, true));
        put(2, P0 + 2, -16, Lazareto.vela(1, true));
        for (int u : new int[]{-3, 3}) put(u, P0 + 1, -16, escalon(Blocks.SPRUCE_STAIRS, u < 0 ? Direction.EAST : Direction.WEST));
        put(-2, P0 + 1, -22, b(Blocks.ANVIL));
        put(0, P0 + 1, -22, b(Blocks.GRINDSTONE));
        put(2, P0 + 1, -22, b(Blocks.SMITHING_TABLE));
        put(-5, P0 + 1, -22, hoguera(true));
        put(5, P0 + 1, -9, hoguera(true));
        put(0, P0 + 6, -16, Mina.farol(false, true));
        put(0, P0 + 4, -23, estandarte(Direction.SOUTH));
        // ---- la Sala del Castellano (el jefe, parte 7ax): el sitial, la alfombra, los braseros, la mesa y los estandartes
        put(0, P1 + 1, -22, escalon(Blocks.DARK_OAK_STAIRS, Direction.NORTH));
        put(-1, P1 + 1, -22, trampilla(Blocks.DARK_OAK_TRAPDOOR, Direction.EAST, false, true));
        put(1, P1 + 1, -22, trampilla(Blocks.DARK_OAK_TRAPDOOR, Direction.WEST, false, true));
        put(0, P1 + 1, -23, b(Blocks.DARK_OAK_PLANKS));
        put(0, P1 + 2, -23, b(Blocks.DARK_OAK_PLANKS));
        put(0, P1 + 3, -23, b(Blocks.DARK_OAK_FENCE));
        put(0, P1 + 4, -23, estandarte(Direction.SOUTH));
        for (int u : new int[]{-3, 3}) put(u, P1 + 5, -23, estandarte(Direction.SOUTH));
        for (int v = -21; v <= -9; v++) put(0, P1 + 1, v, b(Blocks.RED_CARPET));
        for (int u : new int[]{-4, 4}) {
            put(u, P1 + 1, -20, b(Blocks.POLISHED_DEEPSLATE));
            put(u, P1 + 2, -20, hoguera(true));
        }
        for (int v = -20; v <= -12; v++) put(6, P1 + 1, v, trampilla(Blocks.DARK_OAK_TRAPDOOR, Direction.WEST, true, false));
        put(6, P1 + 2, -18, Lazareto.vela(3, true));
        put(6, P1 + 2, -14, Lazareto.vela(2, true));
        put(7, P1 + 1, -16, b(Blocks.CARTOGRAPHY_TABLE));
        for (int v : new int[]{-18, -14}) {
            put(0, P1 + 9, v, Mina.cadena(Direction.Axis.Y));
            put(0, P1 + 8, v, Mina.farol(false, true));
        }
        for (int v : new int[]{-21, -17, -13}) put(-7, P1 + 4, v, estandarte(Direction.EAST));
        // ---- la azotea: el mastil con la bandera de la Marca
        for (int y = AZOTEA + 1; y <= AZOTEA + 6; y++) put(0, y, -16, b(Blocks.DARK_OAK_FENCE));
        for (int v = -15; v >= -15; v--)
            for (int y = AZOTEA + 5; y <= AZOTEA + 6; y++) {
                put(0, y, -15, b(Blocks.YELLOW_WOOL));
                put(0, y, -14, y == AZOTEA + 6 ? b(Blocks.BLACK_WOOL) : b(Blocks.YELLOW_WOOL));
                put(0, y, -13, b(Blocks.YELLOW_WOOL));
            }
    }

    // ------------------------------------------------------------------ las mazmorras (bajo el homenaje), el tunel y la sala de la paga

    private void sotanos() {
        int y0 = FOSO;
        // las celdas: al norte, tres, con su reja; el pasillo por delante
        int[][] celdas = {{-7, -6}, {-4, -3}, {-1, 0}};
        for (int u = -7; u <= 1; u++) {
            for (int y = y0 + 1; y <= y0 + 4; y++) {
                if (u == -5 || u == -2 || u == 1) for (int v = -23; v <= -19; v++) put(u, y, v, piedra(x(u), y, z(v)));
            }
        }
        for (int[] cel : celdas)
            for (int u = cel[0]; u <= cel[1]; u++) {
                for (int y = y0 + 1; y <= y0 + 4; y++) une(u, y, -19, b(Blocks.IRON_BARS));
            }
        // la puerta abierta de la celda del medio (los barrotes doblados)
        aire(-4, y0 + 1, -19);
        aire(-4, y0 + 2, -19);
        // dentro de las celdas: grilletes, huesos, paja
        put(-7, y0 + 3, -23, Mina.cadena(Direction.Axis.Y));
        put(-6, y0 + 1, -22, b(Blocks.BONE_BLOCK));
        put(-7, y0 + 1, -21, calavera(2));
        put(-3, y0 + 1, -23, b(Blocks.HAY_BLOCK));
        put(-4, y0 + 3, -23, Mina.cadena(Direction.Axis.Y));
        put(0, y0 + 1, -22, calavera(14));
        put(-1, y0 + 1, -23, b(Blocks.BROWN_CARPET));
        put(-1, y0 + 4, -21, b(Blocks.COBWEB));
        huesos += 3;
        // el almacen (al este): barriles, sacos, toneles
        for (int u = 2; u <= 7; u++)
            for (int v = -23; v <= -21; v++) {
                int p = pct(u, y0, v, 31);
                if (p < 35) put(u, y0 + 1, v, b(Blocks.BARREL));
                else if (p < 55) put(u, y0 + 1, v, b(Blocks.HAY_BLOCK));
                else if (p < 65) put(u, y0 + 1, v, b(Blocks.COMPOSTER));
                if (p < 20) put(u, y0 + 2, v, b(Blocks.BARREL));
            }
        aire(6, y0 + 1, -22);
        aire(6, y0 + 2, -22);
        // la escalera de la planta baja (sube hacia el norte por la esquina del sureste)
        for (int i = 0; i < 6; i++) {
            int v = -11 - i;
            for (int u = 6; u <= 7; u++) {
                put(u, y0 + 1 + i, v, escalon(Blocks.STONE_BRICK_STAIRS, Direction.NORTH));
                for (int y = y0 + 1; y < y0 + 1 + i; y++) put(u, y, v, b(Blocks.STONE_BRICKS));
                for (int y = y0 + 2 + i; y <= P0 + 2; y++) if (y >= SUELO - 1) aire(u, y, v);
            }
        }
        for (int v = -15; v <= -11; v++) une(5, P0 + 1, v, b(Blocks.SPRUCE_FENCE));
        une(6, P0 + 1, -10, b(Blocks.SPRUCE_FENCE));
        une(7, P0 + 1, -10, b(Blocks.SPRUCE_FENCE));
        // el cuerpo de guardia de abajo: la mesa, los taburetes, la luz
        put(-2, y0 + 1, -13, b(Blocks.SPRUCE_FENCE));
        put(-2, y0 + 2, -13, b(Blocks.SPRUCE_PRESSURE_PLATE));
        put(-3, y0 + 1, -13, escalon(Blocks.SPRUCE_STAIRS, Direction.EAST));
        put(-2, y0 + 1, -12, escalon(Blocks.SPRUCE_STAIRS, Direction.NORTH));
        put(-2, y0 + 3 + 1, -15, Mina.farol(false, true));
        put(-4, y0 + 4, -17, Mina.farol(true, true));
        put(4, y0 + 4, -18, Mina.farol(false, true));
        put(3, y0 + 1, -9, b(Blocks.BARREL));
        put(-6, y0 + 1, -9, b(Blocks.BONE_BLOCK));
        faroles += 3;
    }

    /** El tunel de la poterna: de la sala de abajo de la torre del noroeste a las mazmorras (en ele). */
    private void tunel() {
        int y0 = FOSO;
        int[][] tramos = {{-26, -23, -12, -22}, {-13, -23, -12, -17}, {-13, -18, -KX + 1, -17}};
        for (int[] t : tramos)
            for (int u = Math.min(t[0], t[2]); u <= Math.max(t[0], t[2]); u++)
                for (int v = Math.min(t[1], t[3]); v <= Math.max(t[1], t[3]); v++) {
                    put(u, y0, v, Hash.pct(x(u), y0, z(v), SEMILLA + 33) < 60 ? b(Blocks.COBBLESTONE) : b(Blocks.GRAVEL));
                    for (int y = y0 + 1; y <= y0 + 3; y++) aire(u, y, v);
                    put(u, y0 + 4, v, piedra(x(u), y0 + 4, z(v)));
                }
        // entibado cada cuatro, faroles de almas, telaranas y lo que se dejo uno que intento salir
        for (int u = -24; u <= -14; u += 4)
            for (int v : new int[]{-23, -22}) put(u, y0 + 3, v, Mina.tronco(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.Z));
        put(-18, y0 + 3, -23, Mina.farol(true, true));
        put(-12, y0 + 3, -20, Mina.farol(true, true));
        put(-17, y0 + 3, -22, b(Blocks.COBWEB));
        put(-15, y0 + 1, -23, b(Blocks.BONE_BLOCK));
        put(-16, y0 + 1, -23, calavera(4));
        put(-10, y0 + 3, -18, b(Blocks.COBWEB));
        faroles += 2;
        huesos++;
    }

    /** El muro ilusorio: dos de ancho y dos de alto en el muro del sur del sotano (las dos capas); detras, la sala de la paga. */
    public static List<BlockPos> muro(String id) {
        List<BlockPos> l = new ArrayList<>();
        if (MURO_PAGA.equals(id))
            for (int u = -4; u <= -3; u++)
                for (int v = KV1 - 1; v <= KV1; v++)
                    for (int y = FOSO + 1; y <= FOSO + 2; y++) l.add(new BlockPos(x(u), y, z(v)));
        return l;
    }

    /** Lo que hay en una celda del muro ilusorio cuando esta cerrado: la piedra del sotano. */
    public static BlockState bloqueMuro(String id, BlockPos p) {
        return piedra(p.getX(), p.getY(), p.getZ());
    }

    /** La sala de la paga: el arca de la soldada que nunca se repartio, al sur del sotano, bajo el patio. */
    private void paga() {
        int y0 = FOSO;
        for (int u = -8; u <= 0; u++)
            for (int v = KV1 + 1; v <= -1; v++) {
                boolean pared = u == -8 || u == 0 || v == -1;
                for (int y = y0; y <= y0 + 4; y++) {
                    if (pared || y == y0 + 4) put(u, y, v, piedra(x(u), y, z(v)));
                    else if (y == y0) put(u, y, v, b(Blocks.POLISHED_ANDESITE));
                    else aire(u, y, v);
                }
            }
        for (BlockPos p : muro(MURO_PAGA)) put(p.getX() - CX, p.getY(), p.getZ() - CZ, bloqueMuro(MURO_PAGA, p));
        put(-7, y0 + 1, -2, b(Blocks.BARREL));
        put(-1, y0 + 1, -2, b(Blocks.BARREL));
        put(-1, y0 + 2, -2, Lazareto.vela(3, true));
        put(-4, y0 + 1, -2, trampilla(Blocks.SPRUCE_TRAPDOOR, Direction.NORTH, true, false));
        put(-4, y0 + 2, -2, Lazareto.vela(1, true));
        put(-3, y0 + 1, -2, escalon(Blocks.SPRUCE_STAIRS, Direction.SOUTH));
        put(-5, y0 + 1, -3, b(Blocks.BONE_BLOCK));
        put(-5, y0 + 2, -3, calavera(8));
        put(-2, y0 + 3, -4, Mina.farol(false, true));
        huesos++;
    }

    // ------------------------------------------------------------------ las escaleras del patio al adarve

    private void escaleras() {
        // junto a la muralla del sur, al oeste de la puerta: sube hacia el este
        for (int i = 0; i < 10; i++) {
            int u = -21 + i;
            for (int v = PATIO - 1; v <= PATIO; v++) {
                put(u, SUELO + 1 + i, v, escalon(Blocks.STONE_BRICK_STAIRS, Direction.EAST));
                for (int y = SUELO + 1; y < SUELO + 1 + i; y++) put(u, y, v, piedra(x(u), y, z(v)));
                for (int y = SUELO + 2 + i; y <= SUELO + 4 + i; y++) aire(u, y, v);
            }
        }
        // junto a la del norte, al este del homenaje: sube hacia el oeste
        for (int i = 0; i < 10; i++) {
            int u = 20 - i;
            for (int v = -PATIO; v <= -PATIO + 1; v++) {
                put(u, SUELO + 1 + i, v, escalon(Blocks.STONE_BRICK_STAIRS, Direction.WEST));
                for (int y = SUELO + 1; y < SUELO + 1 + i; y++) put(u, y, v, piedra(x(u), y, z(v)));
                for (int y = SUELO + 2 + i; y <= SUELO + 4 + i; y++) aire(u, y, v);
            }
        }
    }

    // ------------------------------------------------------------------ el campanario de rebato

    private void campanario() {
        int u0 = CAMP_U - 2, u1 = CAMP_U + 2, v0 = CAMP_V - 2, v1 = CAMP_V + 2;
        int suelo = CAMP_Y - 3;
        for (int u = u0; u <= u1; u++)
            for (int v = v0; v <= v1; v++) {
                boolean esquina = (u == u0 || u == u1) && (v == v0 || v == v1);
                for (int y = SUELO + 1; y <= suelo; y++) put(u, y, v, piedra(x(u), y, z(v)));
                for (int y = suelo + 1; y <= CAMP_Y; y++) {
                    if (esquina) put(u, y, v, b(Blocks.STONE_BRICK_WALL));
                    else aire(u, y, v);
                }
                put(u, CAMP_Y + 1, v, b(Blocks.STONE_BRICKS));
            }
        // el tejadillo en piramide
        for (int u = u0; u <= u1; u++)
            for (int v = v0; v <= v1; v++) {
                int d = Math.max(Math.abs(u - CAMP_U), Math.abs(v - CAMP_V));
                if (d == 1) put(u, CAMP_Y + 2, v, b(Blocks.DEEPSLATE_TILES));
                if (d == 0) {
                    put(u, CAMP_Y + 2, v, b(Blocks.DEEPSLATE_TILES));
                    put(u, CAMP_Y + 3, v, b(Blocks.DEEPSLATE_TILE_WALL));
                }
            }
        for (int u = u0; u <= u1; u++)
            for (int v = v0; v <= v1; v++)
                if (Math.max(Math.abs(u - CAMP_U), Math.abs(v - CAMP_V)) == 2)
                    put(u, CAMP_Y + 2, v, Mina.losa(Blocks.DEEPSLATE_TILE_SLAB, SlabType.BOTTOM));
        // la campana, colgada del techo
        put(CAMP_U, CAMP_Y, CAMP_V, Blocks.BELL.defaultBlockState().setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING)
                .setValue(BellBlock.FACING, Direction.SOUTH));
        // la puerta tapiada de abajo y un farol
        put(CAMP_U, SUELO + 1, v1, b(Blocks.SPRUCE_PLANKS));
        put(CAMP_U, SUELO + 2, v1, b(Blocks.SPRUCE_PLANKS));
        une(u0, SUELO + 1, v1 + 1, b(Blocks.SPRUCE_FENCE));
        une(u0, SUELO + 2, v1 + 1, b(Blocks.SPRUCE_FENCE));
        put(u0, SUELO + 3, v1 + 1, Mina.farol(false, false));
        // la cuerda de la campana, por fuera hasta abajo (una cadena)
        for (int y = SUELO + 4; y < suelo; y++) put(u1 + 1, y, CAMP_V, Mina.cadena(Direction.Axis.Y));
    }

    // ------------------------------------------------------------------ los edificios del patio

    /** Un tejado a dos aguas sobre la caja (u0..u1, v0..v1), con el alero en y0; la cumbrera va a lo largo de v (o de u). */
    private void tejado(int u0, int u1, int v0, int v1, int y0, Block escalones, Block lleno, boolean cumbreraEnV, int muro) {
        int ancho = cumbreraEnV ? u1 - u0 : v1 - v0;
        for (int u = u0 - (cumbreraEnV ? 1 : 0); u <= u1 + (cumbreraEnV ? 1 : 0); u++)
            for (int v = v0 - (cumbreraEnV ? 0 : 1); v <= v1 + (cumbreraEnV ? 0 : 1); v++) {
                if (m(u, v) >= MURO0) continue;
                int a = cumbreraEnV ? u - u0 : v - v0;
                int d = Math.min(a, ancho - a);
                if (d < 0) d = -1;
                int y = y0 + d;
                // el hastial: la pared de las puntas, hasta el tejado
                boolean punta = cumbreraEnV ? (v == v0 || v == v1) : (u == u0 || u == u1);
                if (punta && d >= 0) for (int yy = muro + 1; yy < y; yy++) put(u, yy, v, b(lleno));
                Direction f = cumbreraEnV ? (a * 2 < ancho ? Direction.EAST : Direction.WEST) : (a * 2 < ancho ? Direction.SOUTH : Direction.NORTH);
                if (a * 2 == ancho) put(u, y, v, b(lleno));
                else put(u, y, v, escalon(escalones, f));
            }
    }

    /** Las paredes de un edificio del patio: zocalo de piedra y entramado de madera, y el aire de dentro. */
    private void casa(int u0, int u1, int v0, int v1, int alto, Block madera) {
        for (int u = u0; u <= u1; u++)
            for (int v = v0; v <= v1; v++) {
                boolean pared = u == u0 || u == u1 || v == v0 || v == v1;
                boolean esquina = (u == u0 || u == u1) && (v == v0 || v == v1);
                put(u, SUELO, v, pared ? piedra(x(u), SUELO, z(v)) : b(Blocks.SPRUCE_PLANKS));
                for (int y = SUELO + 1; y <= SUELO + alto; y++) {
                    if (!pared) aire(u, y, v);
                    else if (esquina) put(u, y, v, Mina.tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Y));
                    else if (y == SUELO + 1) put(u, y, v, piedra(x(u), y, z(v)));
                    else if (y == SUELO + alto) put(u, y, v, Mina.tronco(Blocks.STRIPPED_DARK_OAK_LOG, u == u0 || u == u1 ? Direction.Axis.Z : Direction.Axis.X));
                    else put(u, y, v, b(madera));
                }
                if (!pared) for (int y = SUELO + alto + 1; y <= SUELO + alto + 6; y++) aire(u, y, v);
            }
    }

    private void cuartel() {
        int u0 = -25, u1 = -16, v0 = -12, v1 = 10;
        casa(u0, u1, v0, v1, 5, Blocks.SPRUCE_PLANKS);
        tejado(u0, u1, v0, v1, SUELO + 6, Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_PLANKS, true, SUELO + 5);
        // la puerta (al este) y ventanas con postigos rotos
        hueco(u1, SUELO + 1, -1, u1, SUELO + 2, 0);
        for (int v : new int[]{-9, -5, 4, 8}) {
            une(u1, SUELO + 3, v, b(Blocks.GLASS_PANE));
            if (pct(u1, 0, v, 41) < 50) aire(u1, SUELO + 3, v);
        }
        // los jergones: paja con su manta, en dos filas
        for (int v : new int[]{-10, -7, -4, 3, 6, 9}) {
            put(u0 + 1, SUELO + 1, v, b(Blocks.WHITE_CARPET));
            put(u0 + 2, SUELO + 1, v, b(Blocks.BROWN_CARPET));
            put(u0 + 3, SUELO + 1, v, b(Blocks.BROWN_CARPET));
            jergones++;
        }
        for (int v : new int[]{-10, -7, 6, 9}) {
            put(u1 - 1, SUELO + 1, v, b(Blocks.WHITE_CARPET));
            put(u1 - 2, SUELO + 1, v, b(Blocks.BROWN_CARPET));
            jergones++;
        }
        // la mesa del medio, el armero y la luz
        for (int v = -2; v <= 1; v++) put(-20, SUELO + 1, v, trampilla(Blocks.SPRUCE_TRAPDOOR, Direction.NORTH, true, false));
        put(-21, SUELO + 1, -1, escalon(Blocks.SPRUCE_STAIRS, Direction.EAST));
        put(-19, SUELO + 1, 0, escalon(Blocks.SPRUCE_STAIRS, Direction.WEST));
        put(-20, SUELO + 2, 0, Lazareto.vela(2, true));
        put(-24, SUELO + 1, -11, b(Blocks.BARREL));
        put(-24, SUELO + 1, 1, b(Blocks.BARREL));
        for (int y = SUELO + 1; y <= SUELO + 3; y++) une(-17, y, -12 + 1, b(Blocks.SPRUCE_FENCE));
        for (int v : new int[]{-6, 2}) {
            for (int y = SUELO + 7; y <= SUELO + 9; y++) put(-20, y, v, Mina.cadena(Direction.Axis.Y));
            put(-20, SUELO + 6, v, Mina.farol(false, true));
        }
        faroles += 2;
        // los monigotes de entrenar, fuera, junto a la puerta
        for (int v : new int[]{-8, -4}) {
            une(-12, SUELO + 1, v, b(Blocks.SPRUCE_FENCE));
            put(-12, SUELO + 2, v, b(Blocks.HAY_BLOCK));
            put(-12, SUELO + 3, v, calavera(v == -8 ? 4 : 12));
        }
    }

    private void comedor() {
        int u0 = 16, u1 = 25, v0 = -11, v1 = 6;
        casa(u0, u1, v0, v1, 5, Blocks.SPRUCE_PLANKS);
        tejado(u0, u1, v0, v1, SUELO + 6, Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_PLANKS, true, SUELO + 5);
        hueco(u0, SUELO + 1, -3, u0, SUELO + 2, -2);
        for (int v : new int[]{-7, 1, 4}) une(u0, SUELO + 3, v, b(Blocks.GLASS_PANE));
        // las dos mesas largas con sus bancos
        for (int t : new int[]{19, 22})
            for (int v = -5; v <= 3; v++) {
                put(t, SUELO + 1, v, trampilla(Blocks.SPRUCE_TRAPDOOR, Direction.NORTH, true, false));
                put(t - 1, SUELO + 1, v, escalon(Blocks.SPRUCE_STAIRS, Direction.EAST));
                if (t + 1 < u1) put(t + 1, SUELO + 1, v, escalon(Blocks.SPRUCE_STAIRS, Direction.WEST));
                bancos++;
            }
        put(19, SUELO + 2, -2, Lazareto.vela(1, true));
        put(22, SUELO + 2, 1, Lazareto.vela(2, true));
        // la cocina, al norte: el hogar apagado con su campana, el caldero, el ahumador y los barriles
        for (int u = 19; u <= 22; u++) {
            put(u, SUELO + 1, v0 + 1, u == 20 || u == 21 ? hoguera(false) : b(Blocks.COBBLESTONE));
            for (int y = SUELO + 3; y <= SUELO + 5; y++) put(u, y, v0 + 1, b(Blocks.COBBLESTONE));
        }
        for (int y = SUELO + 6; y <= SUELO + 11; y++) {
            put(20, y, v0 + 1, b(Blocks.COBBLESTONE));
            put(21, y, v0 + 1, b(Blocks.COBBLESTONE));
        }
        put(17, SUELO + 1, v0 + 1, b(Blocks.CAULDRON));
        put(18, SUELO + 1, v0 + 1, b(Blocks.SMOKER));
        put(24, SUELO + 1, v0 + 1, b(Blocks.BARREL));
        put(24, SUELO + 1, v0 + 2, b(Blocks.BARREL));
        put(24, SUELO + 2, v0 + 1, b(Blocks.BARREL));
        put(23, SUELO + 1, v1 - 1, b(Blocks.BARREL));
        for (int[] f : new int[][]{{20, -6}, {21, 2}}) {
            for (int y = SUELO + 6; y <= SUELO + 9; y++) put(f[0], y, f[1], Mina.cadena(Direction.Axis.Y));
            put(f[0], SUELO + 5, f[1], Mina.farol(false, true));
        }
        faroles += 2;
    }

    private void capilla() {
        int u0 = 13, u1 = 23, v0 = -22, v1 = -13;
        for (int u = u0; u <= u1; u++)
            for (int v = v0; v <= v1; v++) {
                boolean pared = u == u0 || u == u1 || v == v0 || v == v1;
                put(u, SUELO, v, pared ? piedra(x(u), SUELO, z(v)) : b(Blocks.POLISHED_ANDESITE));
                for (int y = SUELO + 1; y <= SUELO + 6; y++) {
                    if (pared) put(u, y, v, piedra(x(u), y, z(v)));
                    else aire(u, y, v);
                }
            }
        tejado(u0, u1, v0, v1, SUELO + 7, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.DEEPSLATE_TILES, false, SUELO + 6);
        // la puerta (al sur), ventanas de cristal gris en los lados
        hueco(17, SUELO + 1, v1, 19, SUELO + 3, v1);
        put(17, SUELO + 4, v1, escalonAlto(Blocks.STONE_BRICK_STAIRS, Direction.WEST));
        put(19, SUELO + 4, v1, escalonAlto(Blocks.STONE_BRICK_STAIRS, Direction.EAST));
        for (int v : new int[]{-19, -16})
            for (int y = SUELO + 3; y <= SUELO + 4; y++) {
                une(u0, y, v, b(Blocks.GRAY_STAINED_GLASS_PANE));
                une(u1, y, v, b(Blocks.GRAY_STAINED_GLASS_PANE));
            }
        // el altar, las velas, la cruz y los bancos
        for (int u = 16; u <= 20; u++) put(u, SUELO + 1, -20, u == 18 ? b(Blocks.CHISELED_STONE_BRICKS) : b(Blocks.POLISHED_ANDESITE));
        put(16, SUELO + 2, -20, Lazareto.vela(3, true));
        put(20, SUELO + 2, -20, Lazareto.vela(3, true));
        put(18, SUELO + 2, -20, Lazareto.vela(1, true));
        for (int y = SUELO + 2; y <= SUELO + 4; y++) put(18, y, -21, b(Blocks.DARK_OAK_FENCE));
        une(17, SUELO + 4, -21, b(Blocks.DARK_OAK_FENCE));
        une(19, SUELO + 4, -21, b(Blocks.DARK_OAK_FENCE));
        for (int v : new int[]{-17, -15})
            for (int u : new int[]{15, 16, 20, 21}) {
                put(u, SUELO + 1, v, escalon(Blocks.SPRUCE_STAIRS, Direction.SOUTH));
                bancos++;
            }
        for (int y = SUELO + 6; y <= SUELO + 10; y++) put(18, y, -17, Mina.cadena(Direction.Axis.Y));
        put(18, SUELO + 5, -17, Mina.farol(false, true));
        faroles++;
    }

    private void cuadras() {
        int u0 = 13, u1 = 25, v0 = 11, v1 = 21;
        for (int u = u0; u <= u1; u++)
            for (int v = v0; v <= v1; v++) {
                put(u, SUELO, v, u == u1 ? piedra(x(u), SUELO, z(v)) : Hash.pct(x(u), SUELO, z(v), SEMILLA + 51) < 50 ? b(Blocks.COARSE_DIRT) : b(Blocks.PACKED_MUD));
                for (int y = SUELO + 1; y <= SUELO + 4; y++) {
                    if (u == u1) put(u, y, v, piedra(x(u), y, z(v)));
                    else aire(u, y, v);
                }
                // el tejado en vertiente (alto contra la muralla, bajo hacia el patio)
                int y = SUELO + 5 - (u1 - u) / 5;
                put(u, y, v, escalon(Blocks.SPRUCE_STAIRS, Direction.EAST));
            }
        // los postes del frente
        for (int v = v0; v <= v1; v += 3)
            for (int y = SUELO + 1; y <= SUELO + 2; y++) put(u0, y, v, Mina.tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Y));
        // las cuadras: vallas, paja y un pesebre
        for (int v : new int[]{13, 15, 17})
            for (int u = 20; u <= 24; u++) une(u, SUELO + 1, v, b(Blocks.SPRUCE_FENCE));
        for (int v : new int[]{12, 14, 16}) {
            put(24, SUELO + 1, v, b(Blocks.HAY_BLOCK));
            put(23, SUELO + 1, v, b(Blocks.WATER_CAULDRON));
        }
        // la fragua, al sur: el horno, el yunque, la piedra de afilar, el carbon y el fuelle (una trampilla)
        put(24, SUELO + 1, 20, b(Blocks.BLAST_FURNACE));
        put(24, SUELO + 1, 19, hoguera(true));
        put(24, SUELO + 2, 19, b(Blocks.COBBLESTONE_WALL));
        put(22, SUELO + 1, 19, b(Blocks.ANVIL));
        put(20, SUELO + 1, 20, b(Blocks.GRINDSTONE));
        put(23, SUELO + 1, 21, b(Blocks.COAL_BLOCK));
        put(21, SUELO + 1, 21, b(Blocks.BARREL));
    }

    private void pozo() {
        int cu = 0, cv = 4;
        for (int du = -2; du <= 2; du++)
            for (int dv = -2; dv <= 2; dv++) {
                int u = cu + du, v = cv + dv;
                boolean boca = Math.abs(du) <= 1 && Math.abs(dv) <= 1;
                if (boca) {
                    for (int y = SUELO - 9; y <= SUELO + 4; y++) {
                        if (y <= SUELO - 8) put(u, y, v, Pal.WATER);
                        else aire(u, y, v);
                    }
                    put(u, SUELO - 10, v, b(Blocks.GRAVEL));
                } else {
                    for (int y = SUELO - 9; y <= SUELO; y++) put(u, y, v, piedra(x(u), y, z(v)));
                    if (Math.abs(du) == 2 && dv == 0) {
                        for (int y = SUELO + 1; y <= SUELO + 3; y++) put(u, y, v, b(Blocks.SPRUCE_FENCE));
                    } else une(u, SUELO + 1, v, b(Blocks.COBBLESTONE_WALL));
                }
            }
        for (int u = cu - 2; u <= cu + 2; u++) put(u, SUELO + 4, cv, Mina.tronco(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.X));
        for (int y = SUELO + 1; y <= SUELO + 3; y++) put(cu, y, cv, Mina.cadena(Direction.Axis.Y));
    }

    private void fundibulo() {
        // el fundibulo: dos caballetes, el eje, el brazo largo en alto, el contrapeso y el monton de piedras
        for (int v : new int[]{10, 14}) {
            for (int y = SUELO + 1; y <= SUELO + 6; y++) put(-8, y, v, Mina.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
            put(-9, SUELO + 1, v, escalon(Blocks.DARK_OAK_STAIRS, Direction.EAST));
            put(-7, SUELO + 1, v, escalon(Blocks.DARK_OAK_STAIRS, Direction.WEST));
        }
        for (int v = 10; v <= 14; v++) put(-8, SUELO + 7, v, Mina.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.Z));
        int[][] brazo = {{-9, 8}, {-10, 9}, {-11, 10}, {-12, 11}, {-7, 6}, {-6, 5}};
        for (int[] c : brazo) put(c[0], SUELO + c[1], 12, b(Blocks.STRIPPED_DARK_OAK_WOOD));
        for (int u = -6; u <= -4; u++)
            for (int v = 11; v <= 13; v++)
                for (int y = SUELO + 2; y <= SUELO + 4; y++) put(u, y, v, y == SUELO + 4 ? b(Blocks.SPRUCE_PLANKS) : b(Blocks.COBBLESTONE));
        for (int y = SUELO + 8; y <= SUELO + 10; y++) put(-12, y, 12, Mina.cadena(Direction.Axis.Y));
        put(-11, SUELO + 1, 16, b(Blocks.COBBLESTONE));
        put(-12, SUELO + 1, 16, b(Blocks.COBBLESTONE));
        put(-11, SUELO + 2, 16, b(Blocks.MOSSY_COBBLESTONE));
        put(-10, SUELO + 1, 17, Mina.losa(Blocks.COBBLESTONE_SLAB, SlabType.BOTTOM));
    }

    private void horca() {
        for (int u = -23; u <= -19; u++)
            for (int v = 15; v <= 17; v++) put(u, SUELO + 1, v, b(Blocks.SPRUCE_PLANKS));
        put(-21, SUELO + 1, 18, escalon(Blocks.SPRUCE_STAIRS, Direction.NORTH));
        for (int u : new int[]{-22, -20})
            for (int y = SUELO + 2; y <= SUELO + 6; y++) put(u, y, 16, Mina.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
        for (int u = -22; u <= -20; u++) put(u, SUELO + 7, 16, Mina.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        put(-21, SUELO + 6, 16, Mina.cadena(Direction.Axis.Y));
        put(-21, SUELO + 5, 16, Mina.cadena(Direction.Axis.Y));
        put(-21, SUELO + 4, 16, calavera(0));
    }

    // ------------------------------------------------------------------ el patio de armas: los caminos y lo que hay por el

    private void patio() {
        // el camino de la puerta al homenaje y el de lado a lado
        for (int v = KV1 + 1; v < GV0; v++)
            for (int u = -2; u <= 2; u++) {
                if (v >= 1 && v <= 7) continue;
                if (!plano.hay(x(u), SUELO, z(v))) put(u, SUELO, v, losas(x(u), SUELO, z(v)));
            }
        for (int u = -15; u <= 15; u++)
            for (int v = -2; v <= 0; v++) if (!plano.hay(x(u), SUELO, z(v))) put(u, SUELO, v, losas(x(u), SUELO, z(v)));
        // braseros en piedra por el patio, matas secas y algun charco
        for (int[] p : new int[][]{{-6, -4}, {6, -4}, {5, 15}, {-14, 6}, {10, -12}, {-11, 2}}) {
            put(p[0], SUELO + 1, p[1], b(Blocks.POLISHED_ANDESITE));
            put(p[0], SUELO + 2, p[1], hoguera(true));
        }
        for (int u = -PATIO; u <= PATIO; u++)
            for (int v = -PATIO; v <= PATIO; v++) {
                if (plano.hay(x(u), SUELO + 1, z(v)) || plano.hay(x(u), SUELO, z(v)) || m(u, v) > PATIO) continue;
                if (!patioSuelo(x(u), SUELO, z(v)).is(net.minecraft.tags.BlockTags.DIRT)) continue;
                int p = pct(u, SUELO, v, 61);
                if (p < 4) put(u, SUELO + 1, v, b(Blocks.DEAD_BUSH));
                else if (p < 7) put(u, SUELO + 1, v, b(Blocks.GRASS));
            }
    }

    // ------------------------------------------------------------------ el foso: la contraescarpa, los escombros del suroeste y los muertos

    /** Lo alto (el bloque de arriba) de los escombros del suroeste en (u, v), o MIN_VALUE fuera de ellos. */
    static int escombro(int u, int v) {
        if (u < -21 || u > -15 || v < FOSO0 + 2 || v > BERMA - 3) return Integer.MIN_VALUE;
        return FOSO + (v - (FOSO0 + 2));
    }

    private void foso() {
        // la contraescarpa: el muro de piedra del lado de fuera (en m = FOSO1 + 1)
        for (int u = -FOSO1 - 1; u <= FOSO1 + 1; u++)
            for (int v = -FOSO1 - 1; v <= FOSO1 + 1; v++) {
                if (m(u, v) != FOSO1 + 1) continue;
                if (escombro(u, v) != Integer.MIN_VALUE) continue;
                for (int y = FOSO + 1; y <= SUELO; y++) put(u, y, v, piedra(x(u), y, z(v)));
            }
        // los escombros del suroeste: la contraescarpa se vino abajo y se puede bajar al foso
        for (int u = -21; u <= -15; u++)
            for (int v = FOSO0 + 2; v <= BERMA - 3; v++) {
                int h = escombro(u, v);
                for (int y = FOSO; y < h; y++) put(u, y, v, Hash.pct(x(u), y, z(v), SEMILLA + 71) < 50 ? b(Blocks.COBBLESTONE) : b(Blocks.GRAVEL));
                boolean medio = u >= -20 && u <= -16;
                int p = pct(u, h, v, 72);
                if (h > FOSO && medio) put(u, h, v, escalon(p < 50 ? Blocks.COBBLESTONE_STAIRS : Blocks.MOSSY_COBBLESTONE_STAIRS, Direction.SOUTH));
                else put(u, h, v, p < 40 ? b(Blocks.COBBLESTONE) : p < 70 ? b(Blocks.ANDESITE) : b(Blocks.MOSSY_COBBLESTONE));
                for (int y = h + 1; y <= SUELO + 3; y++) aire(u, y, v);
                if (!medio && pct(u, h + 1, v, 73) < 35) put(u, h + 1, v, p < 50 ? Mina.losa(Blocks.COBBLESTONE_SLAB, SlabType.BOTTOM) : b(Blocks.STONE_BRICKS));
            }
        // los que murieron en el foso intentando cruzarlo (al sur, bajo la puerta) y estacas
        for (int u = -14; u <= 14; u++)
            for (int v = FOSO0; v <= FOSO1; v++) {
                if (Math.abs(u) <= PASO + 1 && v <= FOSO0) continue;
                int p = pct(u, FOSO, v, 74);
                if (p < 5) {
                    put(u, FOSO + 1, v, b(Blocks.BONE_BLOCK));
                    huesos++;
                } else if (p < 8) put(u, FOSO + 1, v, calavera(p * 3));
                else if (p < 10) une(u, FOSO + 1, v, b(Blocks.SPRUCE_FENCE));
            }
        for (int k = 0; k < 40; k++) {
            int u = (int) Math.round((Hash.f(k, 0, 0, SEMILLA + 75) - 0.5) * 2 * (FOSO1 - 1));
            int v = (int) Math.round((Hash.f(k, 1, 0, SEMILLA + 75) - 0.5) * 2 * (FOSO1 - 1));
            int mm = m(u, v);
            if (mm < FOSO0 + 1 || mm > FOSO1 - 1 || torre(u, v) >= 0 || escombro(u, v) != Integer.MIN_VALUE) continue;
            if (Math.abs(u) <= PASO && v > 0) continue;
            put(u, FOSO + 1, v, Hash.pct(k, 2, 0, SEMILLA + 75) < 60 ? b(Blocks.DEAD_BUSH) : b(Blocks.BONE_BLOCK));
        }
    }

    // ------------------------------------------------------------------ la berma: la barricada de la guarnicion y el ariete

    private void berma() {
        // el estribo del puente (el escalon de la berma donde apoya el tablero)
        for (int u = -PASO - 1; u <= PASO + 1; u++) put(u, SUELO, FOSO1 + 1, losas(x(u), SUELO, z(FOSO1 + 1)));
        // la barricada: estacas y troncos a los lados del camino, en lo alto de la berma
        for (int s : new int[]{-1, 1})
            for (int a = 5; a <= 14; a++) {
                int u = s * a, v = BERMA;
                int p = pct(u, SUELO, v, 81);
                if (p < 15) continue;
                if (a % 3 == 0) {
                    put(u, SUELO + 1, v, Mina.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.X));
                    une(u, SUELO + 2, v, b(Blocks.DARK_OAK_FENCE));
                } else {
                    une(u, SUELO + 1, v, b(Blocks.DARK_OAK_FENCE));
                    if (p < 60) une(u, SUELO + 2, v, b(Blocks.DARK_OAK_FENCE));
                }
            }
        // el ariete de los refugiados, abandonado junto al estribo, de cara al puente alzado
        for (int v = FOSO1 + 1; v <= FOSO1 + 4; v++) put(6, SUELO + 2, v, Mina.tronco(Blocks.OAK_LOG, Direction.Axis.Z));
        put(6, SUELO + 2, FOSO1, b(Blocks.IRON_BLOCK));
        for (int u : new int[]{5, 7})
            for (int v : new int[]{FOSO1 + 1, FOSO1 + 4}) {
                une(u, SUELO + 1, v, b(Blocks.SPRUCE_FENCE));
                une(u, SUELO + 2, v, b(Blocks.SPRUCE_FENCE));
            }
        for (int u = 5; u <= 7; u++)
            for (int v = FOSO1 + 1; v <= FOSO1 + 4; v++)
                put(u, SUELO + 3, v, u == 6 ? b(Blocks.SPRUCE_PLANKS) : escalon(Blocks.SPRUCE_STAIRS, u < 6 ? Direction.EAST : Direction.WEST));
        for (int v : new int[]{FOSO1 + 1, FOSO1 + 4}) {
            put(4, SUELO + 1, v, trampilla(Blocks.DARK_OAK_TRAPDOOR, Direction.WEST, false, true));
            put(8, SUELO + 1, v, trampilla(Blocks.DARK_OAK_TRAPDOOR, Direction.EAST, false, true));
        }
    }

    // ------------------------------------------------------------------ el campamento quemado de los refugiados

    static final int[][] TIENDAS = {{-22, 58}, {-13, 66}, {-26, 73}, {-11, 80}, {13, 58}, {24, 63}, {11, 84}, {26, 79}};
    static final int[][] CARROS = {{-7, 70}, {8, 64}, {-28, 63}};

    private void campamento() {
        // el suelo quemado y el camino del santuario a la rampa
        for (int u = -36; u <= 36; u++)
            for (int v = BERMA + 4; v <= 94; v++) {
                if (superficie(u, v) != Integer.MIN_VALUE) continue;
                if (Math.hypot(u - SANT_U, v - SANT_V) <= SantuariosMorvain.R + 6.5) continue;
                int t = alto(x(u), z(v));
                double d = Math.max(Math.abs(u) / 36.0, Math.abs(v - 70) / 26.0);
                if (Math.abs(u) <= 2) {
                    put(u, t, v, pct(u, t, v, 91) < 70 ? b(Blocks.DIRT_PATH) : b(Blocks.GRAVEL));
                    continue;
                }
                if (pct(u, t, v, 92) / 100.0 > 1.15 - d) continue;
                put(u, t, v, quemado(x(u), t, z(v)));
                int p = pct(u, t + 1, v, 93);
                if (p < 4) put(u, t + 1, v, b(Blocks.DEAD_BUSH));
                else if (p < 6) put(u, t + 1, v, b(Blocks.BONE_BLOCK));
                else if (p < 7) put(u, t + 1, v, calavera(p * 5));
            }
        // la rampa del camino: medio escalon donde hace falta
        for (int v = BERMA + 1; v <= BERMA + 30; v++) {
            int hb = rampaMedia(v);
            for (int u = -2; u <= 2; u++) {
                if (rampaTope(u, v) <= alto(x(u), z(v))) continue;
                if (hb % 2 == 1) put(u, (hb - 1) / 2, v, losa(Blocks.COBBLESTONE_SLAB, SlabType.BOTTOM));
            }
        }
        for (int[] t : TIENDAS) tienda(t[0], t[1]);
        for (int i = 0; i < CARROS.length; i++) carro(CARROS[i][0], CARROS[i][1], i == 2);
        fosa();
    }

    /** Una tienda de lona quemada (a lo largo de v): la cumbrera, la lona a jirones, el petate y lo que dejaron. */
    private void tienda(int cu, int cv) {
        int t = alto(x(cu), z(cv));
        for (int dv = 0; dv <= 4; dv++)
            for (int du = -2; du <= 2; du++) {
                int u = cu + du, v = cv + dv;
                int y = t + 3 - Math.abs(du);
                int p = pct(u, y, v, 101);
                if (du == 0) {
                    if (dv == 0 || dv == 4) for (int yy = t + 1; yy <= t + 2; yy++) put(u, yy, v, Mina.tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Y));
                    put(u, t + 3, v, p < 70 ? Mina.tronco(Blocks.DARK_OAK_LOG, Direction.Axis.Z) : Pal.AIR);
                    continue;
                }
                if (p < 35) continue;
                put(u, y, v, p < 55 ? b(Blocks.BLACK_WOOL) : p < 75 ? b(Blocks.GRAY_WOOL) : p < 88 ? b(Blocks.BROWN_WOOL) : b(Blocks.COAL_BLOCK));
            }
        put(cu, t + 1, cv + 2, b(Blocks.BROWN_CARPET));
        put(cu, t + 1, cv + 3, b(Blocks.BONE_BLOCK));
        put(cu - 1, t + 1, cv + 1, pct(cu, t, cv, 102) < 50 ? b(Blocks.DECORATED_POT) : calavera(6));
        tiendas++;
    }

    /** Un carro de los refugiados (o volcado): la caja, las ruedas y las varas; con sus bultos. */
    private void carro(int cu, int cv, boolean volcado) {
        int t = alto(x(cu), z(cv));
        if (volcado) {
            for (int dv = 0; dv <= 2; dv++)
                for (int y = t + 1; y <= t + 2; y++) put(cu, y, cv + dv, b(Blocks.SPRUCE_PLANKS));
            put(cu + 1, t + 3, cv, trampilla(Blocks.DARK_OAK_TRAPDOOR, Direction.EAST, false, true));
            put(cu + 1, t + 1, cv + 2, b(Blocks.BARREL));
            put(cu - 1, t + 1, cv + 1, b(Blocks.HAY_BLOCK));
        } else {
            for (int du = -1; du <= 1; du++)
                for (int dv = 0; dv <= 2; dv++) put(cu + du, t + 1, cv + dv, losa(Blocks.SPRUCE_SLAB, SlabType.TOP));
            for (int dv : new int[]{0, 2}) {
                put(cu - 2, t + 1, cv + dv, trampilla(Blocks.DARK_OAK_TRAPDOOR, Direction.WEST, false, true));
                put(cu + 2, t + 1, cv + dv, trampilla(Blocks.DARK_OAK_TRAPDOOR, Direction.EAST, false, true));
            }
            une(cu - 1, t + 1, cv - 1, b(Blocks.SPRUCE_FENCE));
            une(cu + 1, t + 1, cv - 1, b(Blocks.SPRUCE_FENCE));
            put(cu, t + 2, cv + 2, b(Blocks.BARREL));
            put(cu + 1, t + 2, cv + 1, b(Blocks.HAY_BLOCK));
        }
        carros++;
    }

    /** La fosa: el hoyo donde echaron a los muertos, con su monton de tierra y unas cruces. */
    private void fosa() {
        for (int u = 13; u <= 20; u++)
            for (int v = 70; v <= 74; v++) {
                int t = alto(x(u), z(v));
                boolean borde = u == 13 || u == 20 || v == 70 || v == 74;
                if (borde) {
                    put(u, t, v, b(Blocks.COARSE_DIRT));
                    continue;
                }
                put(u, t, v, Pal.AIR);
                put(u, t - 1, v, Pal.AIR);
                int p = pct(u, t, v, 111);
                put(u, t - 2, v, p < 40 ? b(Blocks.BONE_BLOCK) : p < 70 ? b(Blocks.SOUL_SOIL) : b(Blocks.ROOTED_DIRT));
                if (p < 15) put(u, t - 1, v, calavera(p));
                huesos++;
            }
        for (int u = 14; u <= 19; u++) {
            int t = alto(x(u), z(76));
            put(u, t + 1, 76, b(Blocks.COARSE_DIRT));
        }
        for (int u : new int[]{14, 17, 20}) {
            int t = alto(x(u), z(68));
            une(u, t + 1, 68, b(Blocks.SPRUCE_FENCE));
            put(u, t + 2, 68, losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
        }
    }

    // ------------------------------------------------------------------ lo de dentro con bloque-entidad (atril, arcones)

    private void trozos() {
        int y = A + 1;
        trozo(ATRIL_U, y, ATRIL_V, c -> c.lectern(x(ATRIL_U), y, z(ATRIL_V), Direction.SOUTH, Perdidos.libro(LIBRO)));
        trozo(-24, SUELO + 1, -1, c -> c.chest(x(-24), SUELO + 1, z(-1), Direction.EAST, BOTIN));
        trozo(24, SUELO + 1, -9, c -> c.barrel(x(24), SUELO + 1, z(-9), Direction.UP, BOTIN));
        trozo(23, SUELO + 1, 20, c -> c.barrel(x(23), SUELO + 1, z(20), Direction.UP, BOTIN));
        trozo(6, FOSO + 1, -22, c -> c.chest(x(6), FOSO + 1, z(-22), Direction.SOUTH, BOTIN));
        trozo(6, P0 + 1, -22, c -> c.chest(x(6), P0 + 1, z(-22), Direction.WEST, BOTIN));
        trozo(-6, FOSO + 1, -3, c -> c.chest(x(-6), FOSO + 1, z(-3), Direction.EAST, BOTIN_BUENO));
        int[] ca = CARROS[0];
        int tc = alto(x(ca[0]), z(ca[1]));
        trozo(ca[0] - 1, tc + 2, ca[1] + 1, c -> c.chest(x(ca[0] - 1), tc + 2, z(ca[1] + 1), Direction.WEST, BOTIN));
    }

    private void trozo(int u, int y, int v, Trozo.Obra o) {
        elementos.add(new Trozo(6, x(u), y, z(v), x(u), y, z(v), o));
    }

    // ------------------------------------------------------------------ los sitios y los de alli

    private void ponerSitios() {
        sitio("entrada", 0, sup(0, 88) + 1, 88);
        sitio("campamento", 4, sup(4, 72) + 1, 72);
        sitio("fosa", 16, sup(16, 77) + 1, 77);
        sitio("puente", 0, SUELO + 1, BERMA - 2);
        sitio("ariete", 3, SUELO + 1, FOSO1 + 4);
        sitio("escombros", -18, SUELO + 1, BERMA - 1);
        sitio("foso", -18, FOSO + 1, FOSO0 + 1);
        sitio("poterna", -34, FOSO + 1, -27);
        sitio("torre_no", -27, FOSO + 1, -26);
        sitio("tunel", -18, FOSO + 1, -22);
        sitio("mazmorras", -4, FOSO + 1, -17);
        sitio("almacen", 4, FOSO + 1, -20);
        sitio("paga", -4, FOSO + 1, -4);
        sitio("homenaje", 0, P0 + 1, -12);
        sitio("castellano", 0, P1 + 1, -12);
        sitio("azotea", 3, AZOTEA + 1, -12);
        sitio("patio", 0, SUELO + 1, 8);
        sitio("pozo", 3, SUELO + 1, 4);
        sitio("cuartel", -20, SUELO + 1, 4);
        sitio("comedor", 20, SUELO + 1, 5);
        sitio("capilla", 18, SUELO + 1, -16);
        sitio("cuadras", 17, SUELO + 1, 15);
        sitio("campanario", CAMP_U, SUELO + 1, CAMP_V + 4);
        sitio("adarve", -15, A + 1, MURO0);
        sitio("torno", 0, A + 1, 23);
        sitio("puerta", 0, SUELO + 1, GV0 - 2);
        sitio("torre_so", -27, T + 1, 27);
    }

    private static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;

    private void ponerEsperas() {
        int C = Insomne.CENTINELA, B = Insomne.BALLESTERO, E = Insomne.ESCUDERO;
        int R = Enemigo.REZA, D = Enemigo.DE_PIE, S = Enemigo.TUMBADO;
        // ---- en las almenas (el ballestero, de pie en el hueco: desde el adarve el muro le taparia lo de abajo)
        en(B, -15, A + 2, MURO1, SUR, D, 0, "murallas");
        en(B, 15, A + 2, MURO1, SUR, D, 0, "murallas");
        en(B, 0, T + 2, GV1, SUR, D, 0, "murallas");
        en(B, MURO1, A + 2, 0, ESTE, D, 0, "murallas");
        en(B, -MURO1, A + 2, -3, OESTE, D, 0, "murallas");
        en(B, 15, A + 2, -MURO1, NORTE, D, 0, "murallas");
        // ---- en lo alto de las torres
        en(B, -28, T + 1, 28, 45f, D, 0, "torres");
        en(B, 28, T + 1, 28, -45f, D, 0, "torres");
        en(B, -28, T + 1, -28, 135f, D, 0, "torres");
        // ---- de ronda por el adarve
        en(C, -6 - 6, A + 1, MURO0, SUR, D, 4, "adarve");
        en(C, -MURO0, A + 1, 8, OESTE, D, 4, "adarve");
        // ---- la guardia que se quedo fuera al alzar el puente: en la barricada, ante el estribo
        en(C, -3, SUELO + 1, BERMA - 2, SUR, D, 0, "barricada");
        en(C, 3, SUELO + 1, BERMA - 3, SUR, D, 0, "barricada");
        en(E, 0, SUELO + 1, BERMA - 1, SUR, D, 0, "barricada");
        // ---- en el foso: al pie de los escombros y junto a la poterna
        en(C, -24, FOSO + 1, FOSO0 + 3, ESTE, D, 4, "foso");
        en(E, -32, FOSO + 1, -12, SUR, D, 3, "foso");
        // ---- la torre del noroeste, el tunel y las mazmorras
        en(E, -27, FOSO + 1, -27, OESTE, R, 0, "mazmorras");
        en(C, -12, FOSO + 1, -20, NORTE, D, 0, "mazmorras");
        en(E, -4, FOSO + 1, -17, OESTE, D, 2, "mazmorras");
        en(B, 4, FOSO + 1, -19, OESTE, S, 0, "mazmorras");
        en(C, 2, FOSO + 1, -12, OESTE, R, 0, "mazmorras");
        // ---- la sala de armas del homenaje
        en(E, -4, P0 + 1, -12, SUR, D, 0, "homenaje");
        en(E, 3, P0 + 1, -10, SUR, D, 0, "homenaje");
        en(C, 4, P0 + 1, -19, OESTE, R, 0, "homenaje");
        // ---- el patio
        en(C, 3, SUELO + 1, 4, SUR, D, 4, "patio");
        en(C, CAMP_U, SUELO + 1, CAMP_V + 4, SUR, D, 0, "patio");
        en(E, -1, SUELO + 1, GV0 - 2, SUR, D, 0, "patio");
        en(E, 1, SUELO + 1, GV0 - 2, SUR, D, 0, "patio");
        en(E, -21, SUELO + 1, 13, ESTE, R, 0, "patio");
        en(E, 19, SUELO + 1, 19, OESTE, D, 0, "patio");
        // ---- el cuartel, el comedor y la capilla
        en(B, -22, SUELO + 1, -5, ESTE, S, 0, "cuartel");
        en(E, -20, SUELO + 1, -9, ESTE, R, 0, "cuartel");
        en(C, 20, SUELO + 1, 4, OESTE, R, 0, "comedor");
        en(C, 18, SUELO + 1, -18, NORTE, R, 0, "capilla");
        // ---- la sala del torno
        en(E, 3, A + 1, 23, OESTE, D, 0, "torno");
        en(C, -3, A + 1, 22, ESTE, R, 0, "torno");
    }

    private void en(int variante, int u, int y, int v, float giro, int postura, int ronda, String grupo) {
        esperas.add(new Espera(variante, x(u), y, z(v), giro, postura, ronda, grupo));
    }

    // ------------------------------------------------------------------ el torno: el puente levadizo y el rastrillo

    /** Para las pruebas: las veces que se ha bajado el puente con la palanca, los rebatos y los que acudieron. */
    public static int BAJADAS, REBATOS, ACUDEN;
    /** El tick de la bajada en curso (o -1). */
    private static int bajando = -1;
    private static long rebatoHasta = Long.MIN_VALUE / 4;
    private static int campanadaT;

    public static BlockPos palanca() {
        return new BlockPos(x(PALANCA_U), A + 2, z(PALANCA_V));
    }

    /** El sitio de la campana de rebato (en el mundo). */
    public static BlockPos campana() {
        return new BlockPos(x(CAMP_U), CAMP_Y, z(CAMP_V));
    }

    /** Si el puente esta bajado (el tablero sobre el foso lo dice). */
    public static boolean puenteBajado(ServerLevel m) {
        BlockPos p = new BlockPos(x(0), SUELO, z(PV0 + 4));
        return m.isLoaded(p) && m.getBlockState(p).is(Blocks.DARK_OAK_PLANKS);
    }

    /** Si el rastrillo esta alzado (los barrotes de abajo no estan). */
    public static boolean rastrilloAlzado(ServerLevel m) {
        BlockPos p = new BlockPos(x(0), SUELO + 1, z(RV));
        return m.isLoaded(p) && !m.getBlockState(p).is(Blocks.IRON_BARS);
    }

    /** Pone el puente en una de sus fases (0 alzado .. 3 bajado). */
    static void ponerPuente(ServerLevel m, int fase) {
        for (int u = -PASO; u <= PASO; u++)
            for (int v = PV0; v <= PV1; v++)
                for (int y = SUELO; y <= SUELO + 7; y++) {
                    BlockPos p = new BlockPos(x(u), y, z(v));
                    if (m.getBlockState(p).is(Blocks.DARK_OAK_PLANKS)) m.setBlock(p, Pal.AIR, 3);
                }
        for (BlockPos p : puente(fase)) m.setBlock(p, tabla(), 3);
    }

    /** Sube (o baja) el rastrillo hasta tener {@code n} filas alzadas (0 bajado .. 6 alzado del todo). */
    static void ponerRastrillo(ServerLevel m, int n) {
        BlockState barras = b(Blocks.IRON_BARS);
        for (int u = -PASO; u <= PASO; u++)
            for (int y = SUELO + 1; y <= SUELO + 12; y++) {
                boolean hay = y >= SUELO + 1 + n && y <= SUELO + 6 + n;
                BlockPos p = new BlockPos(x(u), y, z(RV));
                BlockState s = m.getBlockState(p);
                if (hay && !s.is(Blocks.IRON_BARS)) m.setBlock(p, barras, 3);
                if (!hay && s.is(Blocks.IRON_BARS)) m.setBlock(p, Pal.AIR, 3);
            }
    }

    /** Baja el puente y sube el rastrillo de golpe (comando y pruebas) o con la bajada de siempre. */
    public static void bajarPuente(ServerLevel m, boolean yaMismo) {
        if (yaMismo) {
            ponerRastrillo(m, 6);
            ponerPuente(m, 3);
            palanca(m, true);
            bajando = -1;
        } else empezarBajada(m);
    }

    /** Alza el puente y baja el rastrillo (comando y pruebas): como estaba al hacerse el fuerte. */
    public static void alzarPuente(ServerLevel m) {
        ponerPuente(m, 0);
        ponerRastrillo(m, 0);
        palanca(m, false);
        bajando = -1;
    }

    static void palanca(ServerLevel m, boolean bajada) {
        BlockState s = m.getBlockState(palanca());
        if (s.is(Blocks.LEVER) && s.getValue(LeverBlock.POWERED) != bajada) {
            m.setBlock(palanca(), s.setValue(LeverBlock.POWERED, bajada), 3);
            m.updateNeighborsAt(palanca(), Blocks.LEVER);
        }
    }

    private static void empezarBajada(ServerLevel m) {
        bajando = 0;
        BAJADAS++;
        BlockPos pu = new BlockPos(x(0), SUELO + 3, z(PV0));
        m.playSound(null, pu, DarkCraftMod.SFX_PUENTE_LEVADIZO.get(), SoundSource.BLOCKS, 4.0f, 1.0f);
        m.playSound(null, new BlockPos(x(0), SUELO + 3, z(RV)), DarkCraftMod.SFX_RASTRILLO.get(), SoundSource.BLOCKS, 3.0f, 1.0f);
        for (ServerPlayer p : m.players())
            if (p.distanceToSqr(x(0), A, z(PALANCA_V)) < 40 * 40) {
                com.darkcraft.net.Net.to(p, new com.darkcraft.net.Net.Cartel(com.darkcraft.net.Net.Cartel.AVISO, "darkcraft.fuerte.puente", 0));
                if (p.distanceToSqr(x(0), A, z(PALANCA_V)) < 24 * 24) com.darkcraft.story.Logros.dar(p, "rastrillo");
            }
    }

    /** Un paso de la bajada: el rastrillo sube fila a fila y el puente cae en tres golpes. */
    private static void bajada(ServerLevel m) {
        int t = bajando++;
        if (t > 0 && t <= 48 && t % 8 == 0) ponerRastrillo(m, t / 8);
        if (t == 60) ponerPuente(m, 1);
        if (t == 110) ponerPuente(m, 2);
        if (t == 160) {
            ponerPuente(m, 3);
            m.playSound(null, x(0), SUELO + 1, z(PV0 + 3), DarkCraftMod.SFX_GOLPE_PESADO.get(), SoundSource.BLOCKS, 3.0f, 0.6f);
            m.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()), x(0) + 0.5, SUELO + 1, z(PV1) + 0.5, 60, 2.2, 0.2, 0.6, 0.2);
            bajando = -1;
        }
        if (t % 10 == 5 && bajando > 0) m.sendParticles(ParticleTypes.SMOKE, x(0) + 0.5, SUELO + 4, z(PV0) + 0.5, 6, 2.0, 2.0, 0.3, 0.01);
    }

    /**
     * Cada tick con alguien cerca (lo llama {@link Morvain}): la palanca del torno (si la han bajado, empieza la bajada del puente y
     * sube el rastrillo), la bajada en curso y la campana mientras tocan a rebato.
     */
    public static void tick(ServerLevel m) {
        BlockPos pa = palanca();
        if (!m.isLoaded(pa)) return;
        if (bajando >= 0) bajada(m);
        else if (m.getGameTime() % 10 == 0) revisarPalanca(m);
        if (rebatoActivo(m) && --campanadaT <= 0) {
            campanadaT = CAMPANADA;
            BlockPos c = campana();
            m.playSound(null, c, DarkCraftMod.SFX_CAMPANA_REBATO.get(), SoundSource.HOSTILE, 8.0f, 1.0f);
        }
    }

    /** La palanca: si esta bajada y el puente alzado (y no se esta bajando), empieza la bajada. Devuelve si el puente esta bajado. */
    public static boolean revisarPalanca(ServerLevel m) {
        if (puenteBajado(m)) return true;
        BlockState s = m.getBlockState(palanca());
        if (!s.is(Blocks.LEVER) || !s.getValue(LeverBlock.POWERED)) return false;
        if (bajando < 0) empezarBajada(m);
        return false;
    }

    // ------------------------------------------------------------------ el rebato

    /** Si estan tocando a rebato en el fuerte (solo en el servidor). */
    public static boolean rebatoActivo(Level l) {
        if (l == null || l.isClientSide) return false;
        long t = l.getGameTime();
        return t < rebatoHasta && rebatoHasta - t <= REBATO;
    }

    /**
     * Un centinela ha tocado el cuerno entero: suena la campana del campanario y la guarnicion acude a por {@code presa} (los que estan
     * a menos de 48 bloques y no estan ya peleando, como mucho doce, los mas cercanos primero; mientras dura, persiguen de punta a punta
     * del fuerte: {@link Insomne#correa}). Dura {@link #REBATO} y la campana vuelve a sonar cada {@link #CAMPANADA}.
     */
    public static void rebato(ServerLevel m, LivingEntity presa, Insomne quien) {
        REBATOS++;
        rebatoHasta = m.getGameTime() + REBATO;
        campanadaT = 0;
        BlockPos c = campana();
        m.playSound(null, c, DarkCraftMod.SFX_CAMPANA_REBATO.get(), SoundSource.HOSTILE, 8.0f, 1.0f);
        campanadaT = CAMPANADA;
        if (presa != null && presa.isAlive()) {
            double ox = quien == null ? c.getX() + 0.5 : quien.getX(), oy = quien == null ? SUELO + 1 : quien.getY(), oz = quien == null ? c.getZ() + 0.5 : quien.getZ();
            List<Insomne> l = m.getEntitiesOfClass(Insomne.class, new AABB(ox - 48, oy - 24, oz - 48, ox + 48, oy + 24, oz + 48),
                    e -> e.isAlive() && e != quien && e.getTarget() == null && !e.muriendo());
            l.sort(java.util.Comparator.comparingDouble(e -> e.distanceToSqr(ox, oy, oz)));
            int n = 0;
            for (Insomne e : l) {
                if (n >= 12) break;
                if (e.acudir(presa)) {
                    n++;
                    ACUDEN++;
                    e.animo(0.15, REBATO);
                }
            }
        }
        for (ServerPlayer p : m.players())
            if (distancia(p.getX(), p.getZ()) < 90 && Math.abs(p.getY() - SUELO) < 60)
                com.darkcraft.net.Net.to(p, new com.darkcraft.net.Net.Cartel(com.darkcraft.net.Net.Cartel.AVISO, "darkcraft.fuerte.rebato", 0));
    }

    /** Para las pruebas y el comando: que acabe el rebato. */
    public static void sinRebato() {
        rebatoHasta = Long.MIN_VALUE / 4;
    }

    // ------------------------------------------------------------------ lo que hay, donde se esta

    /** Si (o el hogar de un ballestero) esta en lo alto: las murallas, las torres, la azotea de la puerta (no bajan a perseguir). */
    public static boolean enLoAlto(BlockPos p) {
        int u = p.getX() - CX, v = p.getZ() - CZ;
        return p.getY() > A && m(u, v) <= MURO1 + 6 && !enHomenaje(u, v);
    }

    /** Lo que hay en el fuerte ya generado (para la prueba): el puente, el rastrillo, la palanca, el atril, el muro, la campana... */
    public static String resumen(ServerLevel m) {
        boolean libro = m.getBlockEntity(new BlockPos(x(ATRIL_U), A + 1, z(ATRIL_V))) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le
                && Perdidos.de(le.getBook()) == LIBRO;
        var pa = m.getBlockState(palanca());
        int muro = 0;
        for (BlockPos q : muro(MURO_PAGA)) if (!m.getBlockState(q).isAir()) muro++;
        boolean bueno = m.getBlockState(new BlockPos(x(-6), FOSO + 1, z(-3))).is(Blocks.CHEST);
        int alzado = 0;
        for (BlockPos p : puente(0)) if (m.getBlockState(p).is(Blocks.DARK_OAK_PLANKS)) alzado++;
        int barras = 0;
        for (int u = -PASO; u <= PASO; u++) for (int y = SUELO + 1; y <= SUELO + 6; y++) if (m.getBlockState(new BlockPos(x(u), y, z(RV))).is(Blocks.IRON_BARS)) barras++;
        return "puente " + (puenteBajado(m) ? "bajado" : alzado == puente(0).size() ? "alzado" : "a medias (" + alzado + ")") + ", rastrillo "
                + (barras == 0 ? "alzado" : barras + "/" + (2 * PASO + 1) * 6 + " barrotes") + ", palanca " + (pa.is(Blocks.LEVER) ? pa.getValue(LeverBlock.POWERED) ? "bajada" : "alzada" : "NO")
                + ", atril con el libro " + LIBRO + " " + (libro ? "SI" : "NO") + ", arcon de la paga " + (bueno ? "SI" : "NO") + ", muro de la paga " + muro + "/"
                + muro(MURO_PAGA).size() + ", campana " + (m.getBlockState(campana()).is(Blocks.BELL) ? "SI" : "NO") + ", poterna "
                + (m.getBlockState(new BlockPos(x(-32), FOSO + 1, z(-27))).isAir() ? "abierta" : "TAPADA") + ", foso "
                + (m.getBlockState(new BlockPos(x(-10), FOSO, z(32))).isAir() ? "SIN FONDO" : "seco") + (rebatoActivo(m) ? ", tocan a rebato" : "");
    }

    /** El nombre del sitio del fuerte donde esta (x, y, z) (null: fuera). */
    public static String lugar(double x, double y, double z) {
        int u = (int) Math.floor(x) - CX, v = (int) Math.floor(z) - CZ;
        int mm = m(u, v);
        if (y < FOSO - 4 || y > AZOTEA + 14) return null;
        if (mm <= PATIO + 3 && y < SUELO - 0.5) {
            if (u >= -8 && u <= 0 && v >= KV1 + 1 && v <= -1) return "darkcraft.morvain.lugar.fuerte_paga";
            return "darkcraft.morvain.lugar.fuerte_mazmorras";
        }
        if (torre(u, v) == 0 && y < SUELO - 0.5) return "darkcraft.morvain.lugar.fuerte_mazmorras";
        if (enHomenaje(u, v)) return y >= P1 && y < AZOTEA ? "darkcraft.morvain.lugar.fuerte_castellano" : "darkcraft.morvain.lugar.fuerte_homenaje";
        if (enPuerta(u, v) && y > A - 0.5 && y < T) return "darkcraft.morvain.lugar.fuerte_torno";
        if (mm <= MURO1 + 6 && y > A - 0.5) return "darkcraft.morvain.lugar.fuerte_adarve";
        if (mm <= PATIO) return "darkcraft.morvain.lugar.fuerte_patio";
        if (mm >= FOSO0 && mm <= FOSO1 + 1 && y < SUELO - 1) return "darkcraft.morvain.lugar.fuerte_foso";
        if (v > BERMA + 2 && v < 96 && Math.abs(u) < 40) return "darkcraft.morvain.lugar.fuerte_campamento";
        if (mm <= BERMA + 8) return "darkcraft.morvain.lugar.fuerte";
        return null;
    }

    /** Si (x, y, z) esta dentro del fuerte (el patio, las torres, el homenaje, las mazmorras): para el logro. */
    public static boolean dentro(double x, double y, double z) {
        int u = (int) Math.floor(x) - CX, v = (int) Math.floor(z) - CZ;
        return m(u, v) <= PATIO && y > FOSO && y < AZOTEA + 8;
    }

    /** Si (x, y, z) esta en el fuerte o al pie de sus murallas (para el viento de las almenas). */
    public static boolean enFuerte(double x, double y, double z) {
        return distancia(x, z) < BERMA + 4 && y > FOSO - 3 && y < AZOTEA + 30;
    }
}
