package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Calcinado;
import com.darkcraft.enemigos.Condestable;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Puestos;
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
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
 * El Patio del Juramento (parte 7n): donde la guardia de la Capital juraba ante la piedra del Juramento ("guardar la puerta,
 * no dormir, no morir"), al este de la plaza. La noche del cielo rojo el Condestable mando cerrar todas las puertas de la
 * ciudad (libro perdido 22) y se quedo de rodillas en medio del patio con el montante clavado delante: sigue alli, ardiendo
 * dentro de su armadura ({@code enemigos/Condestable}). Por esta puerta no pasa nadie... y nadie sale.
 * <p>
 * Desde la plaza, la <b>calle del Juramento</b> lleva al <b>cuerpo de la puerta</b> (el paso abovedado, con el rastrillo
 * subido: se le ven los dientes); dentro, el <b>patio</b> cerrado por muros con sus cuatro torres de esquina: el estrado con
 * la mano alzada en el suelo, la <b>piedra del Juramento</b> con su bandera, cuatro braseros y los montones de ceniza (de alli
 * se alza la guardia cuando la llama). Al este, fuera del muro, la <b>torre del Condestable</b>: su reja esta bajada mientras el
 * viva; dentro, su mesa, el arcon y el atril con su ultima orden (libro perdido 23); una escalera de mano sube a lo alto.
 * <p>
 * <b>El rastrillo</b> cae en cuanto empieza la pelea (detras de quien entra) y se alza despacio al acabar: si cae el Condestable,
 * cuando dice "abrid las puertas"; si caen todos o se van, al volver el a su sitio ({@link #tick}). Lo que esta bajado se mira
 * en el mundo (asi, si el servidor se para a media pelea, al volver se alza solo). Al caer el Condestable se alza tambien la
 * reja de su torre, se abre por fin el rastrillo de la Puerta Oeste (lo que quedaba bajado) y la guardia de la puerta y los
 * que querian salir descansan ({@link #DESCANSAN}: ya no vuelven).
 * <p>
 * Se dibuja como la capital (chunk a chunk al adornar, despues de ella; en su caja {@link Adorno} no pone nada).
 */
public final class Juramento {
    public static final long SEMILLA = 0x7E4A_3E27_0B1DL;
    /** El eje: el de la capital (u = x - CX, v = z - CZ). */
    public static final int CX = Capital.CX, CZ = Capital.CZ;
    public static final int SUELO = Capital.SUELO, PISO = Capital.PISO;
    /** Desde donde se allana el suelo (lo de antes es de la capital: su calle llega ya pavimentada hasta aqui). */
    static final int U_SUELO = 119;
    /** La calle del Juramento (de la plaza al cuerpo de la puerta, |v| <= 3). */
    public static final int CALLE_V = 3;
    /** El cuerpo de la puerta (u 128..135, |v| <= 6), su paso (|v| <= 2, seis de alto) y la fila del rastrillo (u 132). */
    public static final int PUERTA_U0 = 128, PUERTA_U1 = 135, PUERTA_V = 6, PASO_V = 2, PASO_ALTO = 6, RASTRILLO_U = 132;
    /** El patio (lo que se pisa dentro): u 136..184, |v| <= 24; el centro (el estrado, donde espera el Condestable) en u 160. */
    public static final int PATIO_U0 = 136, PATIO_U1 = 184, PATIO_V = 24, CENTRO_U = 160;
    /** Los muros: dos de grueso (el de la puerta en u 134..135, el del este en 185..186, los otros en |v| 25..26). */
    static final int MURO_O = 134, MURO_E = 185, MURO_V = 25, MURO_ALTO = PISO + 9;
    /** La piedra del Juramento (u del centro) y la asta de la bandera. */
    static final int PIEDRA_U = 168, ASTA_U = 172;
    /** La torre del Condestable, fuera del muro del este: u 187..199, |v| <= 6; su puerta en |v| <= 1 con la reja en u 186. */
    public static final int TORRE_U0 = 187, TORRE_U1 = 199, TORRE_V = 6, REJA_U = 186, TORRE_ALTO = PISO + 20;
    /** Lo allanado (hasta donde llega lo de la torre y las torres de esquina) y lo que se funde alrededor. */
    static final int U_FIN = TORRE_U1 + 4, V_FIN = 31;
    static final double FUNDE = 8;
    /** Lo que toca. */
    public static final int X0 = CX + U_SUELO - 10, X1 = CX + U_FIN + 10, Z0 = CZ - V_FIN - 10, Z1 = CZ + V_FIN + 10;
    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/juramento");
    /** Los grupos de calcinados de la capital que descansan al caer el Condestable: la guardia de la puerta y los que querian salir. */
    public static final Set<String> DESCANSAN = Set.of("fuera", "puerta", "muralla", "camara");

    /** Los braseros del patio (u, v del fuego) y los montones de ceniza (de donde se alza la guardia). */
    static final int[][] BRASEROS = {{CENTRO_U - 11, -11}, {CENTRO_U + 11, -11}, {CENTRO_U - 11, 11}, {CENTRO_U + 11, 11}};
    static final int[][] MONTONES = {{CENTRO_U - 8, -14}, {CENTRO_U + 8, -14}, {CENTRO_U - 8, 14}, {CENTRO_U + 8, 14}, {CENTRO_U - 15, 0},
            {CENTRO_U + 3, -19}};

    /** Un enemigo del sitio: donde espera (y: lo que pisa), hacia donde mira, su variante, como espera, cuanto ronda y su grupo. */
    public record Espera(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    private static Juramento unico;

    public static synchronized Juramento get() {
        if (unico == null) unico = new Juramento();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    /** Lo que hay (para la prueba y las vistas). */
    public int torres, almenas, estandartes, escombros;

    private Juramento() {
        elementos.add(new Suelo());
        Pilotes.Lista mu = new Pilotes.Lista();
        muros(mu);
        elementos.add(mu.pieza(3));
        Pilotes.Lista pu = new Pilotes.Lista();
        puerta(pu);
        elementos.add(pu.pieza(4));
        Pilotes.Lista pa = new Pilotes.Lista();
        patio(pa);
        elementos.add(pa.pieza(4));
        Pilotes.Lista to = new Pilotes.Lista();
        torre(to);
        elementos.add(to.pieza(4));
        Pilotes.Lista ca = new Pilotes.Lista();
        calle(ca);
        elementos.add(ca.pieza(4));
        trozos();
        // ---- los sitios con nombre
        sitio("calle", 114, PISO, 0);
        sitio("puerta", PUERTA_U0 - 3, PISO, 0);
        sitio("rastrillo", RASTRILLO_U, PISO, 0);
        sitio("patio", PATIO_U0 + 4, PISO, 0);
        sitio("ante_condestable", CENTRO_U - 12, PISO, 0);
        sitio("condestable", CENTRO_U, PISO, 0);
        sitio("piedra", PIEDRA_U - 3, PISO, 3);
        int k = 0;
        for (int[] b : BRASEROS) sitio("brasero_" + k++, b[0] + (b[0] < CENTRO_U ? 2 : -2), PISO, b[1] + (b[1] < 0 ? 2 : -2));
        sitio("torre", PATIO_U1 - 1, PISO, 0);
        sitio("dentro_torre", TORRE_U0 + 4, PISO, 0);
        sitio("arcon", TORRE_U1 - 3, PISO, 3);
        sitio("atril", TORRE_U1 - 4, PISO, -3);
        sitio("alto_torre", TORRE_U0 + 5, TORRE_ALTO + 1, -1);
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), q -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo del patio (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en lo del patio: ahi {@link Adorno} no pone nada. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= X0 && x0 <= X1 && z0 + 15 >= Z0 && z0 <= Z1;
    }

    /** Si (x, z) esta en el patio, en el cuerpo de la puerta o en la torre (para el nombre del sitio). */
    public static boolean dentro(double x, double z) {
        double u = x - CX, v = z - CZ;
        return u >= PUERTA_U0 - 1 && u <= TORRE_U1 + 1 && Math.abs(v) <= MURO_V + 2;
    }

    /** Si (x, z) esta dentro del patio cerrado (pasado el rastrillo): ahi pelea el Condestable, y su logro. */
    public static boolean enPatio(double x, double z) {
        double u = x - CX, v = z - CZ;
        return u >= RASTRILLO_U + 1 && u < MURO_E && Math.abs(v) < MURO_V;
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} del patio del Juramento en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los que esperan en el patio y a su puerta (los apunta {@link MorvainEnemigos}; el primero, el Condestable). */
    public List<Espera> esperas() {
        return esperas;
    }

    /** Los sitios con nombre (la puerta, el rastrillo, el patio, el Condestable, la torre, el arcon...): {x, y, z} (y: lo que se pisa). */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    private void sitio(String nombre, int u, int y, int v) {
        sitios.put(nombre, new int[]{x(u), y, z(v)});
    }

    /** Donde estan los montones de ceniza del patio ({x, z}): de alli se alza la guardia que llama el Condestable. */
    public static List<int[]> montones() {
        List<int[]> l = new ArrayList<>();
        for (int[] m : MONTONES) l.add(new int[]{x(m[0]), z(m[1])});
        return l;
    }

    /** Lo alto de los muros del patio, por dentro (de alli tiran la brea los que guardan el muro): {x, y, z} cada cuatro. */
    public static List<int[]> almenasDeDentro() {
        List<int[]> l = new ArrayList<>();
        for (int u = PATIO_U0 + 4; u <= PATIO_U1 - 4; u += 4) {
            l.add(new int[]{x(u), MURO_ALTO + 1, z(-MURO_V)});
            l.add(new int[]{x(u), MURO_ALTO + 1, z(MURO_V)});
        }
        // (por el lado de dentro de cada muro: las almenas estan en el de fuera)
        for (int v = -PATIO_V + 4; v <= PATIO_V - 4; v += 4) {
            if (Math.abs(v) > PUERTA_V + 1) l.add(new int[]{x(MURO_O + 1), MURO_ALTO + 1, z(v)});
            if (Math.abs(v) > 2) l.add(new int[]{x(MURO_E), MURO_ALTO + 1, z(v)});
        }
        return l;
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

    private static BlockState b(Block k) {
        return k.defaultBlockState();
    }

    /** Lo mas bajo del terreno bajo (u, v) (para que lo macizo baje hasta el). */
    private static int base(int u, int v) {
        return Math.min(SUELO, Capital.alto(x(u), z(v))) - 1;
    }

    /** El rastrillo de hierro negro (fuera del juego, en las vistas, barrotes de hierro). */
    public static BlockState rastrillo() {
        try {
            return DarkCraftMod.RASTRILLO.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.Z);
        } catch (Throwable t) {
            return Pal.b(Blocks.IRON_BARS);
        }
    }

    /** La piedra de los muros del patio: la de la muralla, algo mas oscura (aqui ardio mas). */
    static BlockState piedra(int x, int y, int z) {
        int p = Hash.pct(x, y, z, SEMILLA + 1);
        if (p < 14) return b(Blocks.POLISHED_BLACKSTONE_BRICKS);
        if (p < 22) return b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
        return Capital.piedra(x, y, z);
    }

    // ------------------------------------------------------------------ el suelo: allanado y fundido con el terreno

    /** Lo alto del suelo en (x, z): {@link #SUELO} en lo del patio, fundido con el terreno alrededor; MIN_VALUE fuera (o en la capital). */
    static int objetivo(int x, int z) {
        int u = x - CX, v = z - CZ;
        if (u < U_SUELO) return Integer.MIN_VALUE;
        double du = u > U_FIN ? u - U_FIN : 0, dv = Math.abs(v) > V_FIN ? Math.abs(v) - V_FIN : 0;
        double d = Math.hypot(du, dv);
        if (d <= 0) return SUELO;
        if (d >= FUNDE) return Integer.MIN_VALUE;
        double t = Ruido.suave(0, FUNDE, d);
        return (int) Math.round(Ruido.lerp(t, SUELO, Capital.alto(x, z)));
    }

    /** Si (u, v) es la calle del Juramento (de la capital a la puerta) o el paso de la puerta. */
    static boolean calle(int u, int v) {
        return u >= U_SUELO && u <= PUERTA_U1 && Math.abs(v) <= (u < PUERTA_U0 ? CALLE_V : PASO_V);
    }

    private final class Suelo extends Element {
        Suelo() {
            super(SEMILLA + 11);
            this.layer = 1;
            bounds(X0, 60, Z0, X1, 140, Z1);
        }

        @Override
        public void build(Canvas c) {
            for (int x = Math.max(c.minX, x0); x <= Math.min(c.maxX, x1); x++)
                for (int z = Math.max(c.minZ, z0); z <= Math.min(c.maxZ, z1); z++) {
                    int u = x - CX, v = z - CZ;
                    int o = objetivo(x, z);
                    if (o == Integer.MIN_VALUE) continue;
                    int t = Capital.alto(x, z);
                    if (t < Terreno.AGUA) continue;
                    for (int y = o + 1; y <= Math.max(t, o) + 6; y++) c.set(x, y, z, Pal.AIR);
                    for (int y = t + 1; y < o; y++) c.set(x, y, z, Hash.pct(x, y, z, seed) < 70 ? Pal.DIRT : Pal.STONE);
                    int p = Hash.pct(x, o, z, seed + 1);
                    BlockState s;
                    boolean patio = u >= PATIO_U0 && u <= PATIO_U1 && Math.abs(v) <= PATIO_V;
                    double r = Math.hypot(u - CENTRO_U, v);
                    if (patio && r <= 7.5) {
                        // el estrado: el borde de sillares negros, y dentro losas con la mano alzada en oro
                        if (r > 6.5) s = b(Blocks.POLISHED_BLACKSTONE_BRICKS);
                        else if (r > 5.5) s = b(Blocks.POLISHED_DEEPSLATE);
                        else if (mano(u - CENTRO_U, v)) s = (u + v & 1) == 0 ? b(Blocks.GILDED_BLACKSTONE) : b(Blocks.GOLD_BLOCK);
                        else s = p < 70 ? b(Blocks.DEEPSLATE_TILES) : b(Blocks.CRACKED_DEEPSLATE_TILES);
                    } else if (patio) {
                        // las losas del patio, con su cenefa junto a los muros
                        boolean cenefa = u == PATIO_U0 + 1 || u == PATIO_U1 - 1 || Math.abs(v) == PATIO_V - 1;
                        if (cenefa) s = b(Blocks.POLISHED_ANDESITE);
                        else s = p < 36 ? Pal.STONE_BRICKS : p < 52 ? b(Blocks.CRACKED_STONE_BRICKS) : p < 64 ? Pal.ANDESITE
                                : p < 76 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS) : p < 86 ? b(Blocks.TUFF) : p < 94 ? Pal.b(Blocks.COBBLESTONE) : Pal.GRAVEL;
                    } else if (calle(u, v)) {
                        s = p < 34 ? Pal.STONE_BRICKS : p < 50 ? b(Blocks.CRACKED_STONE_BRICKS) : p < 62 ? Pal.b(Blocks.COBBLESTONE)
                                : p < 72 ? Pal.ANDESITE : p < 82 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS) : p < 90 ? b(Blocks.TUFF) : Pal.GRAVEL;
                    } else if (u >= TORRE_U0 && u <= TORRE_U1 && Math.abs(v) <= TORRE_V) {
                        s = Pal.DO_PLANKS;
                    } else s = p < 30 ? Pal.COARSE_DIRT : p < 48 ? Pal.GRAVEL : p < 62 ? b(Blocks.TUFF) : p < 74 ? Pal.DIRT
                            : p < 86 ? b(Blocks.PACKED_MUD) : b(Blocks.SOUL_SOIL);
                    c.set(x, o, z, s);
                    // ceniza por el suelo (en el patio poca: es donde se pelea; nada en el estrado)
                    int q = Hash.pct(x, o + 1, z, seed + 2);
                    if (r > 8 && q < (patio ? 6 : calle(u, v) ? 9 : 16) && !(u >= TORRE_U0 && u <= TORRE_U1 && Math.abs(v) <= TORRE_V))
                        c.set(x, o + 1, z, Capital.ceniza(x, o + 1, z));
                }
        }
    }

    /** La mano alzada del Juramento en el suelo del estrado (u, v desde el centro; los dedos hacia el oeste, hacia la puerta). */
    static boolean mano(int du, int dv) {
        // la palma (3 x 3), cuatro dedos hacia la puerta y el pulgar a un lado
        if (du >= 0 && du <= 2 && Math.abs(dv) <= 1) return true;
        if (du >= -3 && du <= -1 && (dv == -2 || dv == 0 || dv == 2)) return true;
        if (du == -1 && dv == -1) return false;
        if (du >= -2 && du <= -1 && dv == 1) return false;
        return du == 1 && dv == 2 || du == 0 && dv == 3;
    }

    // ------------------------------------------------------------------ los muros y sus torres

    private void muros(Pilotes.Lista m) {
        // el del oeste (salvo el cuerpo de la puerta), el del este (salvo la puerta de la torre) y los del norte y el sur
        for (int v = -MURO_V - 1; v <= MURO_V + 1; v++) {
            if (Math.abs(v) > PUERTA_V) muro(m, MURO_O, v, true, false);
            muro(m, MURO_E, v, false, Math.abs(v) <= 1);
        }
        for (int u = MURO_O; u <= MURO_E + 1; u++) {
            muroV(m, u, -MURO_V - 1, -MURO_V);
            muroV(m, u, MURO_V + 1, MURO_V);
        }
        // las cuatro torres de las esquinas
        for (int su = 0; su <= 1; su++)
            for (int sv = -1; sv <= 1; sv += 2) torreEsquina(m, su == 0 ? MURO_O : MURO_E + 1, sv * (MURO_V + 1));
        // estandartes negros por dentro de los muros del norte y del sur
        for (int u = PATIO_U0 + 6; u <= PATIO_U1 - 6; u += 8) {
            put(m, u, PISO + 6, -MURO_V + 1, b(Blocks.BLACK_WALL_BANNER).setValue(WallBannerBlock.FACING, Direction.SOUTH));
            put(m, u, PISO + 6, MURO_V - 1, b(Blocks.BLACK_WALL_BANNER).setValue(WallBannerBlock.FACING, Direction.NORTH));
            estandartes += 2;
        }
        // y rojos a los lados de la puerta de la torre (por dentro del patio)
        for (int s = -1; s <= 1; s += 2) {
            put(m, MURO_E - 1, PISO + 5, s * 4, b(Blocks.RED_WALL_BANNER).setValue(WallBannerBlock.FACING, Direction.WEST));
            estandartes++;
        }
    }

    /** Una columna de muro de oeste a este (el del oeste o el del este), dos de grueso; con hueco para la puerta de la torre. */
    private void muro(Pilotes.Lista m, int u0, int v, boolean oeste, boolean hueco) {
        for (int u = u0; u <= u0 + 1; u++) {
            for (int y = base(u, v); y <= MURO_ALTO; y++) {
                if (hueco && y >= PISO && y <= PISO + 3) continue;
                put(m, u, y, v, piedra(x(u), y, z(v)));
            }
            if (hueco) {
                for (int y = PISO; y <= PISO + 3; y++) put(m, u, y, v, Pal.AIR);
                // el arco sobre la puerta de la torre
                if (Math.abs(v) == 1) put(m, u, PISO + 3, v, Capital.escalera(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS,
                        v < 0 ? Direction.NORTH : Direction.SOUTH, true));
            }
        }
        // las almenas por fuera (una si y otra no)
        int uf = oeste ? u0 : u0 + 1;
        if ((v & 1) == 0) {
            put(m, uf, MURO_ALTO + 1, v, piedra(x(uf), MURO_ALTO + 1, z(v)));
            almenas++;
        }
    }

    /** Una fila de muro de norte o de sur (vf: la cara de fuera, vd: la de dentro). */
    private void muroV(Pilotes.Lista m, int u, int vf, int vd) {
        for (int v : new int[]{vf, vd})
            for (int y = base(u, v); y <= MURO_ALTO; y++) put(m, u, y, v, piedra(x(u), y, z(v)));
        if ((u & 1) == 0) {
            put(m, u, MURO_ALTO + 1, vf, piedra(x(u), MURO_ALTO + 1, z(vf)));
            almenas++;
        }
    }

    /** Una torre de esquina: maciza, siete de lado, cinco mas alta que el muro, con sus almenas. */
    private void torreEsquina(Pilotes.Lista m, int uc, int vc) {
        torres++;
        int cima = MURO_ALTO + 5;
        for (int u = uc - 3; u <= uc + 3; u++)
            for (int v = vc - 3; v <= vc + 3; v++) {
                for (int y = base(u, v); y <= cima; y++) put(m, u, y, v, piedra(x(u), y, z(v)));
                boolean borde = Math.abs(u - uc) == 3 || Math.abs(v - vc) == 3;
                if (borde && ((u + v) & 1) == 0) put(m, u, cima + 1, v, piedra(x(u), cima + 1, z(v)));
            }
        // las troneras (por fuera, a dos alturas)
        for (int y : new int[]{PISO + 5, PISO + 11}) {
            put(m, uc, y, vc + (vc < 0 ? -3 : 3), b(Blocks.IRON_BARS));
            put(m, uc + (uc < CENTRO_U ? -3 : 3), y, vc, b(Blocks.IRON_BARS));
        }
    }

    // ------------------------------------------------------------------ el cuerpo de la puerta

    /**
     * La puerta del patio: un cuerpo de sillares negros (u 128..135, |v| <= 6) de catorce de alto con almenas; el paso abovedado
     * (cinco de ancho, seis de alto) con su arco en las dos caras; el rastrillo subido (solo asoman sus dientes, en u 132), las
     * ranuras por las que baja, faroles de almas en el paso, estandartes negros fuera y la mano alzada de oro sobre el arco.
     */
    private void puerta(Pilotes.Lista m) {
        int alto = PISO + 13;
        for (int u = PUERTA_U0; u <= PUERTA_U1; u++)
            for (int v = -PUERTA_V; v <= PUERTA_V; v++) {
                boolean paso = Math.abs(v) <= PASO_V;
                for (int y = base(u, v); y <= alto; y++) {
                    if (paso && y >= PISO && y < PISO + PASO_ALTO) continue;
                    put(m, u, y, v, piedra(x(u), y, z(v)));
                }
                if (paso) for (int y = PISO; y < PISO + PASO_ALTO; y++) put(m, u, y, v, Pal.AIR);
                boolean borde = Math.abs(v) == PUERTA_V || u == PUERTA_U0 || u == PUERTA_U1;
                if (borde && ((u + v) & 1) == 0) {
                    put(m, u, alto + 1, v, piedra(x(u), alto + 1, z(v)));
                    almenas++;
                }
            }
        // el arco en las dos caras: las esquinas de arriba del paso redondeadas y la clave
        for (int u : new int[]{PUERTA_U0, PUERTA_U1}) {
            for (int s = -1; s <= 1; s += 2)
                put(m, u, PISO + PASO_ALTO - 1, s * PASO_V, Capital.escalera(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS,
                        s < 0 ? Direction.NORTH : Direction.SOUTH, true));
            put(m, u, PISO + PASO_ALTO, 0, b(Blocks.CHISELED_POLISHED_BLACKSTONE));
        }
        // el rastrillo subido: sus dientes asoman por arriba (lo de mas abajo lo baja la pelea) y las ranuras de los lados
        for (int v = -PASO_V; v <= PASO_V; v++) put(m, RASTRILLO_U, PISO + PASO_ALTO - 1, v, rastrillo());
        for (int s = -1; s <= 1; s += 2)
            for (int y = PISO; y < PISO + PASO_ALTO; y++) put(m, RASTRILLO_U, y, s * (PASO_V + 1), b(Blocks.POLISHED_BLACKSTONE));
        // faroles de almas colgados en el paso
        put(m, PUERTA_U0 + 1, PISO + PASO_ALTO - 1, 0, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        put(m, PUERTA_U1 - 1, PISO + PASO_ALTO - 1, 0, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        // estandartes negros fuera, a los lados del arco
        for (int s = -1; s <= 1; s += 2) {
            put(m, PUERTA_U0 - 1, PISO + 7, s * 4, b(Blocks.BLACK_WALL_BANNER).setValue(WallBannerBlock.FACING, Direction.WEST));
            estandartes++;
        }
        // la mano alzada de oro sobre el arco (en relieve, por fuera): la palma y los dedos hacia arriba
        int uf = PUERTA_U0 - 1;
        int[][] mano = {{0, -1}, {0, 0}, {0, 1}, {1, -1}, {1, 0}, {1, 1}, {2, -1}, {2, 0}, {2, 1}, {3, -1}, {3, 1}, {4, -1}, {3, 0}, {4, 0}, {2, 2}};
        for (int[] q : mano) put(m, uf, PISO + 8 + q[0], q[1], (q[0] + q[1] & 1) == 0 ? b(Blocks.GILDED_BLACKSTONE) : b(Blocks.GOLD_BLOCK));
        for (int v = -2; v <= 3; v++) put(m, uf, PISO + 7, v, b(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB));
    }

    // ------------------------------------------------------------------ el patio

    /**
     * Lo del patio: la piedra del Juramento detras del estrado (al este: el Condestable espera de rodillas de espaldas a ella,
     * de cara a la puerta) con la asta y la bandera quemada; los cuatro braseros encendidos; los montones de ceniza; escombros
     * junto a los muros y algun rescoldo.
     */
    private void patio(Pilotes.Lista m) {
        // ---- la piedra del Juramento: el zocalo, el bloque tallado y la columna con la mano y el remate de oro
        int pu = PIEDRA_U;
        for (int u = pu - 1; u <= pu + 1; u++)
            for (int v = -1; v <= 1; v++) {
                put(m, u, PISO, v, b(Blocks.POLISHED_BLACKSTONE));
                put(m, u, PISO + 1, v, Math.abs(u - pu) + Math.abs(v) == 2 ? Capital.losa(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.BOTTOM)
                        : b(Blocks.CHISELED_STONE_BRICKS));
            }
        for (int y = PISO + 2; y <= PISO + 5; y++) put(m, pu, y, 0, y == PISO + 3 ? b(Blocks.CHISELED_POLISHED_BLACKSTONE) : b(Blocks.POLISHED_BLACKSTONE_BRICKS));
        put(m, pu - 1, PISO + 3, 0, b(Blocks.GILDED_BLACKSTONE));
        put(m, pu, PISO + 6, 0, b(Blocks.GOLD_BLOCK));
        put(m, pu, PISO + 7, 0, Capital.losa(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.BOTTOM));
        // velas consumidas y un rescoldo al pie
        put(m, pu - 2, PISO, -1, Blocks.CANDLE.defaultBlockState().setValue(BlockStateProperties.CANDLES, 3).setValue(BlockStateProperties.LIT, true));
        put(m, pu - 2, PISO, 1, Blocks.CANDLE.defaultBlockState().setValue(BlockStateProperties.CANDLES, 2).setValue(BlockStateProperties.LIT, true));
        // ---- la asta de la bandera, detras: el poste de hierro negro y la bandera roja quemada (de lana) que cuelga al norte
        for (int y = PISO; y <= PISO + 12; y++) une(m, ASTA_U, y, 0, y == PISO ? b(Blocks.POLISHED_BLACKSTONE) : b(Blocks.POLISHED_BLACKSTONE_WALL));
        put(m, ASTA_U, PISO + 13, 0, b(Blocks.GOLD_BLOCK));
        for (int dv = 1; dv <= 6; dv++)
            for (int y = PISO + 7; y <= PISO + 11; y++) {
                int p = Hash.pct(dv, y, 0, SEMILLA + 31);
                // el borde de abajo y la punta quemados (jirones)
                boolean roto = dv >= 5 && p < 30 + (dv - 5) * 40 || y == PISO + 7 && p < 45;
                if (roto) continue;
                boolean manoB = dv >= 2 && dv <= 4 && y >= PISO + 8 && y <= PISO + 10 && !(dv == 3 && y == PISO + 8);
                BlockState s = manoB ? b(Blocks.YELLOW_WOOL) : (y == PISO + 7 || dv == 6) && p < 70 ? b(Blocks.BLACK_WOOL) : b(Blocks.RED_WOOL);
                put(m, ASTA_U, y, -dv, s);
            }
        // ---- los braseros: un pedestal con el cuenco de fuego y cadenas
        for (int[] q : BRASEROS) {
            int u = q[0], v = q[1];
            for (int du = -1; du <= 1; du++)
                for (int dv = -1; dv <= 1; dv++)
                    if (Math.abs(du) + Math.abs(dv) <= 1) put(m, u + du, PISO, v + dv, b(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    else put(m, u + du, PISO, v + dv, Capital.losa(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, SlabType.BOTTOM));
            put(m, u, PISO + 1, v, Capital.rescoldo());
        }
        // ---- los montones de ceniza (alli esperan, enterrados, los de la guardia que llama el Condestable)
        for (int[] q : MONTONES) monton(m, q[0], q[1]);
        // ---- escombros junto a los muros y algun rescoldo
        int[][] esc = {{PATIO_U0 + 3, -18}, {PATIO_U0 + 9, 20}, {PATIO_U1 - 4, -20}, {PATIO_U1 - 6, 17}, {150, -22}, {175, 22}};
        for (int[] q : esc) escombro(m, q[0], q[1]);
        put(m, PATIO_U0 + 2, PISO, -PATIO_V + 2, Capital.rescoldo());
        put(m, PATIO_U1 - 2, PISO, PATIO_V - 3, Capital.rescoldo());
    }

    private void monton(Pilotes.Lista m, int u, int v) {
        for (int du = -2; du <= 2; du++)
            for (int dv = -2; dv <= 2; dv++) {
                double d = Math.hypot(du, dv);
                if (d > 2.4) continue;
                put(m, u + du, PISO, v + dv, Capital.ceniza(x(u + du), PISO, z(v + dv)));
            }
        put(m, u, PISO, v, b(Blocks.LIGHT_GRAY_CARPET));
        put(m, u + 2, PISO, v + 1, b(Blocks.GRAY_CONCRETE_POWDER));
        put(m, u - 2, PISO, v - 1, b(Blocks.LIGHT_GRAY_CONCRETE_POWDER));
    }

    private void escombro(Pilotes.Lista m, int u, int v) {
        escombros++;
        for (int du = -1; du <= 1; du++)
            for (int dv = -1; dv <= 1; dv++) {
                int p = Hash.pct(u + du, 3, v + dv, SEMILLA + 41);
                if (p < 30) continue;
                BlockState s = p < 50 ? Pal.b(Blocks.COBBLESTONE) : p < 64 ? Pal.GRAVEL : p < 78 ? b(Blocks.BLACKSTONE)
                        : Capital.losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM);
                put(m, u + du, PISO, v + dv, s);
                if (du == 0 && dv == 0 && p > 60) put(m, u, PISO + 1, v, Capital.losa(Blocks.COBBLESTONE_SLAB, SlabType.BOTTOM));
            }
    }

    // ------------------------------------------------------------------ la torre del Condestable

    /**
     * La torre: trece de lado, muros de dos, veinte de alto con almenas; tres pisos (el suelo de tablas en PISO + 6 y PISO + 13
     * con su hueco) y lo alto; una escalera de mano pegada al muro del sur, de abajo arriba. La puerta da al patio por el muro
     * del este (tres de ancho, cuatro de alto), con la reja bajada (la quita {@link #tick} al caer el Condestable). Abajo, su
     * sala: la mesa larga con el mapa y las velas, el atril con su ultima orden (libro 23), el arcon, el hogar en el muro del
     * este, el yunque y la piedra de afilar, un estandarte rojo.
     */
    private void torre(Pilotes.Lista m) {
        int u0 = TORRE_U0, u1 = TORRE_U1, vt = TORRE_V;
        for (int u = u0; u <= u1; u++)
            for (int v = -vt; v <= vt; v++) {
                boolean muro = u <= u0 + 1 || u >= u1 - 1 || Math.abs(v) >= vt - 1;
                for (int y = base(u, v); y <= SUELO; y++) put(m, u, y, v, muro ? piedra(x(u), y, z(v)) : Pal.DO_PLANKS);
                for (int y = PISO; y <= TORRE_ALTO; y++) {
                    if (muro) put(m, u, y, v, piedra(x(u), y, z(v)));
                    else if (y == PISO + 6 || y == PISO + 13 || y == TORRE_ALTO) put(m, u, y, v, y == TORRE_ALTO ? Pal.STONE_BRICKS : Pal.DO_PLANKS);
                    else put(m, u, y, v, Pal.AIR);
                }
                boolean borde = u == u0 || u == u1 || Math.abs(v) == vt;
                if (borde && ((u + v) & 1) == 0) {
                    put(m, u, TORRE_ALTO + 1, v, piedra(x(u), TORRE_ALTO + 1, z(v)));
                    almenas++;
                }
                // lo alto: el suelo tambien sobre el muro (se anda hasta las almenas)
                if (muro && !borde) put(m, u, TORRE_ALTO, v, Pal.STONE_BRICKS);
            }
        // la puerta (al oeste, al patio): el hueco en el muro de la torre y el arco
        for (int u = u0; u <= u0 + 1; u++)
            for (int v = -1; v <= 1; v++) {
                for (int y = PISO; y <= PISO + 3; y++) put(m, u, y, v, Pal.AIR);
                if (Math.abs(v) == 1) put(m, u, PISO + 3, v, Capital.escalera(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS,
                        v < 0 ? Direction.NORTH : Direction.SOUTH, true));
            }
        // la reja, bajada (en el hueco del muro del patio)
        for (int v = -1; v <= 1; v++)
            for (int y = PISO; y <= PISO + 2; y++) une(m, REJA_U, y, v, b(Blocks.IRON_BARS));
        une(m, REJA_U, PISO + 3, 0, b(Blocks.IRON_BARS));
        // las ventanas (barrotes) de los pisos de arriba, a cada lado
        for (int y : new int[]{PISO + 9, PISO + 16})
            for (int k = -1; k <= 1; k += 2)
                for (int yy = y; yy <= y + 1; yy++) {
                    une(m, u0 + 6 + k * 2, yy, -vt, b(Blocks.IRON_BARS));
                    put(m, u0 + 6 + k * 2, yy, -vt + 1, Pal.AIR);
                    une(m, u0 + 6 + k * 2, yy, vt, b(Blocks.IRON_BARS));
                    put(m, u0 + 6 + k * 2, yy, vt - 1, Pal.AIR);
                    une(m, u1, yy, k * 2, b(Blocks.IRON_BARS));
                    put(m, u1 - 1, yy, k * 2, Pal.AIR);
                    une(m, u0, yy, k * 3, b(Blocks.IRON_BARS));
                    put(m, u0 + 1, yy, k * 3, Pal.AIR);
                }
        // la escalera de mano (pegada al muro del sur, por dentro) y sus huecos en los suelos
        int ue = u0 + 6, ve = vt - 2;
        for (int y = PISO; y <= TORRE_ALTO; y++)
            put(m, ue, y, ve, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH));
        // ---- la sala de abajo: la mesa larga (losas altas) con el mapa, las velas y un farol
        for (int u = u0 + 3; u <= u0 + 7; u++) put(m, u, PISO, -3, Capital.losa(Blocks.DARK_OAK_SLAB, SlabType.TOP));
        put(m, u0 + 3, PISO + 1, -3, Blocks.CANDLE.defaultBlockState().setValue(BlockStateProperties.CANDLES, 2).setValue(BlockStateProperties.LIT, true));
        put(m, u0 + 5, PISO + 1, -3, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
        put(m, u0 + 7, PISO + 1, -3, Blocks.CANDLE.defaultBlockState().setValue(BlockStateProperties.CANDLES, 4).setValue(BlockStateProperties.LIT, true));
        // la silla (una escalera de roble oscuro) delante de la mesa
        put(m, u0 + 5, PISO, -2, Capital.escalera(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, false));
        // el hogar en el muro del este: el hueco con el fuego y la chimenea por dentro del muro
        for (int v = -1; v <= 1; v++)
            for (int y = PISO; y <= PISO + 2; y++) put(m, u1 - 1, y, v, Pal.AIR);
        put(m, u1 - 1, PISO, 0, Capital.rescoldo());
        put(m, u1 - 1, PISO + 2, -1, Capital.escalera(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.SOUTH, true));
        put(m, u1 - 1, PISO + 2, 1, Capital.escalera(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.NORTH, true));
        // el yunque y la piedra de afilar (junto a la puerta, al norte), barriles y un estandarte rojo en el muro del norte
        put(m, u0 + 2, PISO, -4, b(Blocks.CHIPPED_ANVIL));
        put(m, u0 + 3, PISO, -4, b(Blocks.GRINDSTONE));
        put(m, u1 - 2, PISO, 4, b(Blocks.BARREL));
        put(m, u0 + 6, PISO + 3, -vt + 2, b(Blocks.RED_WALL_BANNER).setValue(WallBannerBlock.FACING, Direction.SOUTH));
        estandartes++;
        // arriba: un brasero en lo alto (de senales)
        put(m, u0 + 6, TORRE_ALTO + 1, -2, Capital.rescoldo());
        // un farol en cada piso
        put(m, u0 + 3, PISO + 7, 3, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
        put(m, u0 + 9, PISO + 14, -3, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
        put(m, u0 + 4, PISO + 5, 0, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }

    // ------------------------------------------------------------------ la calle del Juramento

    /** La calle de la plaza a la puerta: muretes quemados a los lados (lo que queda de las casas), escombros y faroles. */
    private void calle(Pilotes.Lista m) {
        for (int s = -1; s <= 1; s += 2)
            for (int u = U_SUELO; u < PUERTA_U0 - 1; u++) {
                int v = s * (CALLE_V + 2);
                int p = Hash.pct(u, 5, v, SEMILLA + 51);
                int h = p < 15 ? 0 : 1 + p % 3;
                for (int y = PISO; y < PISO + h; y++) put(m, u, y, v, piedra(x(u), y, z(v)));
                if (h > 0 && p % 4 == 0) put(m, u, PISO + h, v, Capital.losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
            }
        // los faroles de la calle (en postes) junto a la puerta
        for (int s = -1; s <= 1; s += 2) {
            une(m, PUERTA_U0 - 2, PISO, s * (CALLE_V + 1), Pal.DO_FENCE);
            une(m, PUERTA_U0 - 2, PISO + 1, s * (CALLE_V + 1), Pal.DO_FENCE);
            put(m, PUERTA_U0 - 2, PISO + 2, s * (CALLE_V + 1), Blocks.SOUL_LANTERN.defaultBlockState());
        }
        put(m, U_SUELO + 3, PISO, -(CALLE_V + 1), Capital.rescoldo());
    }

    // ------------------------------------------------------------------ lo que lleva datos: el arcon, el atril

    private void trozos() {
        int[] ar = {TORRE_U1 - 3, 3}, at = {TORRE_U1 - 4, -3};
        trozo(ar[0], PISO, ar[1], c -> c.chest(x(ar[0]), PISO, z(ar[1]), Direction.WEST, BOTIN));
        trozo(at[0], PISO, at[1], c -> c.lectern(x(at[0]), PISO, z(at[1]), Direction.SOUTH, Perdidos.libro(23)));
        trozo(TORRE_U1 - 3, PISO, 4, c -> c.barrel(x(TORRE_U1 - 3), PISO, z(4), Direction.UP, Capital.BOTIN));
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
     * El Condestable (el primero: de rodillas en medio del estrado, de cara a la puerta) y, fuera, la guardia de su puerta: dos
     * agazapados junto al arco y un ceniciento enterrado en la calle.
     */
    private void ponerEsperas() {
        en(0, CENTRO_U, PISO, 0, OESTE, Enemigo.REZA, 0, Puestos.JEFE);
        en(Calcinado.GUARDIA, PUERTA_U0 - 2, PISO, -2, OESTE, Enemigo.REZA, 0, "puerta_juramento");
        en(Calcinado.GUARDIA, PUERTA_U0 - 2, PISO, 2, OESTE, Enemigo.REZA, 0, "puerta_juramento");
        en(Calcinado.CENICIENTO, U_SUELO + 2, PISO, 1, OESTE, Enemigo.TUMBADO, 0, "calle_juramento");
    }

    // ------------------------------------------------------------------ el rastrillo, la reja y la paz

    /** Si el Condestable ha caido (la Capital ya no guarda sus puertas). */
    public static boolean enPaz(net.minecraft.server.MinecraftServer srv) {
        return Puestos.muerto(srv, MorvainEnemigos.JURAMENTO, MorvainEnemigos.condestable());
    }

    /** Las filas del rastrillo bajadas ahora mismo (sin los dientes: 0 subido, {@code PASO_ALTO - 1} del todo) y desde cuando. */
    private static int filas = -1;
    /** Un reloj propio (las llamadas a {@link #tick}: asi va igual en las pruebas, que llaman muchas veces en un tick del mundo). */
    private static long reloj, movido;
    /** Para las pruebas: las veces que ha caido y que se ha alzado entero. */
    public static int CAIDAS, ALZADOS;

    /** Lo que tarda en alzarse cada fila (el torno, despacio) y lo que espera el Condestable caido para abrir (dice su frase). */
    static final int SUBE = 16, ABRIR_TRAS = 50;

    /**
     * El Condestable que se vio por ultima vez: caido, {@link Puestos#enemigo} ya no lo da, y hay que saber cuanto lleva en el
     * suelo (mientras dice su ultima frase el rastrillo sigue bajado).
     */
    private static Condestable visto;

    /** Si el rastrillo deberia estar bajado ahora: el Condestable peleando, o recien caido (aun no ha dicho "abrid las puertas"). */
    static boolean cerrar(ServerLevel m) {
        Enemigo e = Puestos.enemigo(MorvainEnemigos.JURAMENTO, MorvainEnemigos.condestable());
        if (e instanceof Condestable k && !k.isRemoved()) visto = k;
        Condestable c = visto;
        if (c == null || c.isRemoved() || c.level() != m) {
            visto = null;
            return false;
        }
        if (c.isAlive()) return c.enPelea();
        return c.deathTime < ABRIR_TRAS;
    }

    /** Cuantas filas del rastrillo estan bajadas en el mundo (mira la columna de en medio). */
    static int filasEnElMundo(ServerLevel m) {
        int n = 0;
        for (int k = 1; k < PASO_ALTO; k++) {
            BlockState s = m.getBlockState(new BlockPos(x(RASTRILLO_U), PISO + PASO_ALTO - 1 - k, z(0)));
            if (esRastrillo(s)) n = k;
        }
        return n;
    }

    private static boolean esRastrillo(BlockState s) {
        try {
            return s.is(DarkCraftMod.RASTRILLO.get());
        } catch (Throwable t) {
            return s.is(Blocks.IRON_BARS);
        }
    }

    /**
     * Cada tick con alguien cerca (lo llama {@link Morvain#onTick}): el rastrillo de la puerta baja de golpe (una fila por tick,
     * y antes saca del paso a quien este debajo, hacia dentro) o sube despacio (una fila cada {@link #SUBE} ticks, con el torno);
     * y cada segundo, con el Condestable caido, la reja de su torre y lo de la paz.
     */
    public static void tick(ServerLevel m) {
        BlockPos centro = new BlockPos(x(RASTRILLO_U), PISO, z(0));
        if (!m.isLoaded(centro)) return;
        long ahora = ++reloj;
        int quiere = cerrar(m) ? PASO_ALTO - 1 : 0;
        // lo que hay en el mundo manda (al cargar, o si algo lo ha cambiado: otra partida, un comando)
        if (filas < 0 || filas == quiere && ahora % 100 == 0) filas = filasEnElMundo(m);
        if (filas < quiere) {
            if (filas == 0) {
                CAIDAS++;
                sonar(m, DarkCraftMod.SFX_CONDESTABLE_RASTRILLO.get(), 1.8f, 0.95f);
                DarkCraftMod.LOGGER.info("[DarkCraft] El rastrillo del Juramento cae");
            }
            filas++;
            int y = PISO + PASO_ALTO - 1 - filas;
            if (y <= PISO + 2) sacarDelPaso(m);
            for (int v = -PASO_V; v <= PASO_V; v++) {
                BlockPos p = new BlockPos(x(RASTRILLO_U), y, z(v));
                BlockState s = m.getBlockState(p);
                if (s.isAir() || s.canBeReplaced() || esRastrillo(s)) m.setBlock(p, rastrillo(), 3);
            }
            if (filas == quiere) {
                m.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE_BRICKS.defaultBlockState()),
                        x(RASTRILLO_U) + 0.5, PISO + 0.2, z(0) + 0.5, 30, 0.4, 0.1, 2.2, 0.1);
                m.playSound(null, x(RASTRILLO_U) + 0.5, PISO, z(0) + 0.5, net.minecraft.sounds.SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.8f, 0.5f);
            }
            movido = ahora;
        } else if (filas > quiere && ahora - movido >= SUBE) {
            if (ahora - movido > SUBE * 2L) {
                sonar(m, DarkCraftMod.SFX_CONDESTABLE_ABRIR.get(), 1.6f, 1.0f);
                DarkCraftMod.LOGGER.info("[DarkCraft] El rastrillo del Juramento se alza");
            }
            int y = PISO + PASO_ALTO - 1 - filas;
            for (int v = -PASO_V; v <= PASO_V; v++) {
                BlockPos p = new BlockPos(x(RASTRILLO_U), y, z(v));
                if (esRastrillo(m.getBlockState(p))) m.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            }
            m.playSound(null, x(RASTRILLO_U) + 0.5, y, z(0) + 0.5, net.minecraft.sounds.SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.9f, 0.6f);
            filas--;
            movido = ahora;
            if (filas == 0) ALZADOS++;
        }
        if (ahora % 20 == 3) paz(m);
    }

    /** Si el rastrillo del patio esta bajado del todo (pruebas). */
    public static boolean rastrilloBajado(ServerLevel m) {
        return filasEnElMundo(m) == PASO_ALTO - 1;
    }

    /** Para las pruebas y el comando: el rastrillo arriba ya (sin esperar al torno). */
    public static void alzarYa(ServerLevel m) {
        for (int k = 1; k < PASO_ALTO; k++)
            for (int v = -PASO_V; v <= PASO_V; v++) {
                BlockPos p = new BlockPos(x(RASTRILLO_U), PISO + PASO_ALTO - 1 - k, z(v));
                if (esRastrillo(m.getBlockState(p))) m.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            }
        filas = 0;
    }

    /** Antes de que baje lo de abajo: quien este bajo el rastrillo pasa al lado de dentro (al patio). */
    private static void sacarDelPaso(ServerLevel m) {
        AABB caja = new AABB(x(RASTRILLO_U) - 0.3, PISO - 0.5, z(-PASO_V), x(RASTRILLO_U) + 1.3, PISO + PASO_ALTO, z(PASO_V) + 1);
        for (Entity e : m.getEntities((Entity) null, caja, q -> q instanceof net.minecraft.world.entity.LivingEntity && q.isAlive()))
            e.teleportTo(x(RASTRILLO_U) + 1.8, e.getY(), e.getZ());
    }

    private static void sonar(ServerLevel m, net.minecraft.sounds.SoundEvent ev, float vol, float tono) {
        for (ServerPlayer p : m.players())
            if (p.distanceToSqr(x(RASTRILLO_U), PISO, z(0)) < 80 * 80)
                p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(net.minecraft.core.Holder.direct(ev), SoundSource.BLOCKS,
                        x(RASTRILLO_U) + 0.5, PISO + 3, z(0) + 0.5, vol, tono, m.random.nextLong()));
    }

    /**
     * Cada segundo: con el Condestable caido, la reja de su torre arriba, el rastrillo de la Puerta Oeste abierto (lo que
     * quedaba bajado) y los de la puerta que seguian esperando, que descansen; con el vivo, la reja bajada.
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
        BlockPos oeste = new BlockPos(Capital.CX - 3, PISO + 4, Capital.CZ);
        if (m.isLoaded(oeste) && m.getBlockState(oeste).is(Blocks.IRON_BARS)) abrirPuertaOeste(m);
        // los de la puerta oeste que seguian esperando (no los que pelean) se deshacen en ceniza
        if (Puestos.sitio(MorvainEnemigos.CAPITAL) != null)
            for (int i = 0; i < MorvainEnemigos.capital().size(); i++) {
                if (!DESCANSAN.contains(MorvainEnemigos.capital().get(i).grupo())) continue;
                Enemigo e = Puestos.enemigo(MorvainEnemigos.CAPITAL, i);
                if (e != null && e.isAlive() && e.getTarget() == null && e.accion() == 0) {
                    m.sendParticles(ParticleTypes.ASH, e.getX(), e.getY() + 1.0, e.getZ(), 30, 0.4, 0.7, 0.4, 0.02);
                    m.sendParticles(ParticleTypes.SMALL_FLAME, e.getX(), e.getY() + 1.0, e.getZ(), 8, 0.3, 0.5, 0.3, 0.01);
                    e.disolver();
                }
            }
    }

    /** La reja de la torre: bajada (con los barrotes unidos entre si) o quitada. */
    public static void reja(ServerLevel m, boolean bajar) {
        List<BlockPos> l = new ArrayList<>();
        for (int v = -1; v <= 1; v++)
            for (int y = PISO; y <= PISO + 2; y++) l.add(new BlockPos(x(REJA_U), y, z(v)));
        l.add(new BlockPos(x(REJA_U), PISO + 3, z(0)));
        if (bajar) {
            for (BlockPos p : l) if (m.getBlockState(p).isAir()) m.setBlock(p, Blocks.IRON_BARS.defaultBlockState(), 3);
            for (BlockPos p : l) {
                BlockState s = m.getBlockState(p);
                if (s.is(Blocks.IRON_BARS)) m.setBlock(p, Block.updateFromNeighbourShapes(s, m, p), 2);
            }
        } else {
            for (BlockPos p : l) if (m.getBlockState(p).is(Blocks.IRON_BARS)) m.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            m.playSound(null, x(REJA_U) + 0.5, PISO + 1, z(0) + 0.5, net.minecraft.sounds.SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 1.0f, 0.7f);
            m.sendParticles(ParticleTypes.SMOKE, x(REJA_U) + 0.5, PISO + 1.5, z(0) + 0.5, 20, 0.2, 1.0, 1.2, 0.02);
        }
    }

    /**
     * "Abrid las puertas": el rastrillo de la Puerta Oeste (medio bajado desde la noche del cielo rojo) se alza por fin: se
     * quitan sus barrotes de abajo (quedan los dientes de arriba) y los que estaban caidos en el paso. Lo oye quien este cerca.
     */
    public static void abrirPuertaOeste(ServerLevel m) {
        int n = 0;
        for (int v = -3; v <= 3; v++)
            for (int y = PISO; y <= PISO + 5; y++) {
                BlockPos p = new BlockPos(Capital.CX - 3, y, Capital.CZ + v);
                if (m.getBlockState(p).is(Blocks.IRON_BARS)) {
                    m.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    n++;
                }
            }
        BlockPos caido = new BlockPos(Capital.CX - 2, PISO, Capital.CZ - 1);
        if (m.getBlockState(caido).is(Blocks.IRON_BARS)) m.setBlock(caido, Blocks.AIR.defaultBlockState(), 3);
        for (ServerPlayer p : m.players())
            if (p.distanceToSqr(Capital.CX, PISO, Capital.CZ) < 64 * 64)
                p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                        net.minecraft.core.Holder.direct(DarkCraftMod.SFX_CONDESTABLE_ABRIR.get()), SoundSource.BLOCKS,
                        Capital.CX - 2.5, PISO + 4, Capital.CZ + 0.5, 1.4f, 1.0f, m.random.nextLong()));
        DarkCraftMod.LOGGER.info("[DarkCraft] La Puerta Oeste de la Capital se abre por fin ({} barrotes)", n);
    }

    /** Si el rastrillo de la Puerta Oeste sigue medio bajado (pruebas). */
    public static int barrotesPuertaOeste(ServerLevel m) {
        int n = 0;
        for (int v = -3; v <= 3; v++)
            for (int y = PISO; y <= PISO + 5; y++) if (m.getBlockState(new BlockPos(Capital.CX - 3, y, Capital.CZ + v)).is(Blocks.IRON_BARS)) n++;
        return n;
    }

    /** Para los comandos y las pruebas: vuelve a dejar la Puerta Oeste como estaba (medio bajada) y la reja de la torre bajada. */
    public static void deshacerPaz(ServerLevel m) {
        for (int v = -3; v <= 3; v++) {
            int abajo = PISO + 3 + (v == 1 ? 2 : 0) + (v == -2 ? -1 : 0);
            for (int y = abajo; y <= PISO + 5; y++) {
                BlockPos p = new BlockPos(Capital.CX - 3, y, Capital.CZ + v);
                if (m.getBlockState(p).isAir()) m.setBlock(p, Blocks.IRON_BARS.defaultBlockState(), 3);
            }
        }
        for (int v = -3; v <= 3; v++)
            for (int y = PISO; y <= PISO + 6; y++) {
                BlockPos p = new BlockPos(Capital.CX - 3, y, Capital.CZ + v);
                BlockState s = m.getBlockState(p);
                if (s.is(Blocks.IRON_BARS)) m.setBlock(p, Block.updateFromNeighbourShapes(s, m, p), 2);
            }
        if (m.isLoaded(new BlockPos(x(REJA_U), PISO, z(0)))) reja(m, true);
    }

    // ------------------------------------------------------------------ para la prueba

    /** Lo que hay en el patio ya generado (para la prueba): el arcon, el atril con el libro 23, la reja, el rastrillo, la piedra. */
    public static String resumen(ServerLevel m) {
        Juramento a = get();
        int[] at = a.sitio("atril"), ar = a.sitio("arcon");
        BlockPos pa = new BlockPos(at[0], at[1], at[2]);
        boolean libro = m.getBlockEntity(pa) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le && Perdidos.de(le.getBook()) == 23;
        boolean arcon = m.getBlockState(new BlockPos(ar[0], ar[1], ar[2])).is(Blocks.CHEST);
        int reja = 0;
        for (int v = -1; v <= 1; v++)
            for (int y = PISO; y <= PISO + 3; y++) if (m.getBlockState(new BlockPos(x(REJA_U), y, z(v))).is(Blocks.IRON_BARS)) reja++;
        int dientes = 0;
        for (int v = -PASO_V; v <= PASO_V; v++) if (esRastrillo(m.getBlockState(new BlockPos(x(RASTRILLO_U), PISO + PASO_ALTO - 1, z(v))))) dientes++;
        boolean piedra = m.getBlockState(new BlockPos(x(PIEDRA_U), PISO + 6, z(0))).is(Blocks.GOLD_BLOCK);
        int fuegos = 0;
        for (int[] q : BRASEROS) if (m.getBlockState(new BlockPos(x(q[0]), PISO + 1, z(q[1]))).is(Blocks.CAMPFIRE)) fuegos++;
        int suelo = 0, muestras = 0;
        for (int u = PATIO_U0 + 2; u <= PATIO_U1 - 2; u += 3)
            for (int v = -PATIO_V + 2; v <= PATIO_V - 2; v += 3) {
                muestras++;
                BlockPos p = new BlockPos(x(u), PISO, z(v));
                BlockState s = m.getBlockState(p);
                if (!m.getBlockState(p.below()).isAir() && (s.isAir() || s.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock)) suelo++;
            }
        return "atril con el libro 23 " + (libro ? "SI" : "NO") + ", arcon " + (arcon ? "SI" : "NO") + ", reja de la torre " + reja + "/10, dientes del rastrillo "
                + dientes + "/" + (2 * PASO_V + 1) + ", filas bajadas " + filasEnElMundo(m) + ", piedra del Juramento " + (piedra ? "SI" : "NO")
                + ", braseros " + fuegos + "/" + BRASEROS.length + ", suelo del patio libre " + suelo + "/" + muestras + ", rastrillo de la Puerta Oeste "
                + barrotesPuertaOeste(m) + " barrotes";
    }
}
