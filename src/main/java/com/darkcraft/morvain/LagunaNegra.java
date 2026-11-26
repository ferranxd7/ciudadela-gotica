package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Podrido;
import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Pal;
import com.darkcraft.pueblo.gen.Trozo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.darkcraft.morvain.Pilotes.b;
import static com.darkcraft.morvain.Pilotes.escalera;
import static com.darkcraft.morvain.Pilotes.giro;
import static com.darkcraft.morvain.Pilotes.losa;
import static com.darkcraft.morvain.Pilotes.tronco;

/**
 * La Laguna Negra (parte 7j), en el Pantano Podrido, al este del santuario del Embarcadero Podrido: donde espera el
 * <b>Barquero</b> ({@code enemigos/Barquero}), el que la hermana Ilse oia remar bajo la capilla (libro perdido 20). Una laguna
 * honda y negra (la cava en el pantano: tres o cuatro de agua) con islotes de barro, juncos, mangles muertos, barcas
 * hundidas y estacas con calaveras (los que no pagaron); en medio, el <b>embarcadero</b>: un tablado octogonal sobre pilotes
 * (radio {@value #R_PLATAFORMA}, a un bloque del agua) con postes de amarre y faroles de almas, la barandilla rota y rollos de
 * cabo; desde la orilla del oeste llega el <b>muelle</b> (tres de ancho) y hasta el va un camino de tierra desde el santuario;
 * al este, amarrada, <b>la barca del Barquero</b> (cinco por once, el farol en la proa y el arcon del peaje en la popa); en
 * la otra orilla, un arco de piedra medio caido. El Barquero espera de pie en su barca con la linterna en alto; los podridos
 * de la laguna (sus pasajeros) flotan boca abajo alrededor ({@link #esperas}).
 * <p>
 * Al caer el Barquero ({@link #liberar}) los pasajeros que siguen flotando descansan y los faroles de almas se vuelven
 * faroles de verdad ({@link #paz}, cada 5 s con alguien cerca: tambien los de chunks que no estaban cargados).
 * Se dibuja como la aldea de los pilotes: chunk a chunk al adornar; en su caja {@link Adorno} no pone nada.
 */
public final class LagunaNegra {
    public static final long SEMILLA = 0xB4A2_C0E7_01L;
    /** El centro del embarcadero. */
    public static final int CX = -3955, CZ = -1880;
    /** La laguna, el embarcadero (octogono) y lo que tarda la laguna en fundirse con el pantano. */
    static final int R_LAGUNA = 30, R_FUNDE = 12;
    public static final int R_PLATAFORMA = 11;
    /** Todo lo que se dibuja (con el camino desde el santuario): ahi {@link Adorno} no pone nada. */
    static final int X0 = -4013, X1 = CX + 46, Z0 = CZ - 46, Z1 = CZ + 46;
    /** Las tablas del embarcadero y del muelle (se pisa en PASO + 1, a un bloque del agua). */
    public static final int PASO = Terreno.AGUA + 1;
    /** El muelle: de la orilla del oeste al embarcadero, tres de ancho (v -1..1). */
    static final int MUELLE_U0 = -40, MUELLE_U1 = -11;
    /** La barca del Barquero: el centro (u, v), a lo largo de z (la proa al norte); el suelo, a ras de agua. */
    public static final int BARCA_U = 16, BARCA_V = 0, BARCA_SUELO = Terreno.AGUA;
    /** El camino desde el santuario del Embarcadero Podrido: de (x, z) a (x, z). */
    static final int[] CAMINO = {-4010, -1893, CX + MUELLE_U0, CZ};
    static final double CAMINO_ANCHO = 1.3;

    private static final Ruido RUIDO = new Ruido(SEMILLA);
    /** Los islotes de barro: {u, v, radio}. */
    static final int[][] ISLOTES = {{-6, -24, 4}, {22, 19, 3}, {-23, 16, 4}, {25, -17, 3}, {6, 25, 3}};

    // ------------------------------------------------------------------ el terreno

    private static final int AW = X1 - X0 + 1, AD = Z1 - Z0 + 1;
    private static final int[] ALTO = new int[AW * AD], SUELO = new int[AW * AD];

    static {
        Arrays.fill(ALTO, Integer.MIN_VALUE);
        Arrays.fill(SUELO, Integer.MIN_VALUE);
    }

    /** El bloque de arriba del terreno de Morvain en (x, z) (sin la laguna). */
    public static int alto(int x, int z) {
        if (x < X0 || x > X1 || z < Z0 || z > Z1) return (int) Math.floor(Terreno.columna(x, z).altura());
        int i = (z - Z0) * AW + (x - X0);
        int v = ALTO[i];
        if (v == Integer.MIN_VALUE) ALTO[i] = v = (int) Math.floor(Terreno.columna(x, z).altura());
        return v;
    }

    /** Lo que mide de la orilla de la laguna (negativo: dentro). */
    static double orilla(int x, int z) {
        double u = x - CX, v = z - CZ;
        return Math.hypot(u, v) - R_LAGUNA + RUIDO.fbm(x, z, 20, 2) * 4.0;
    }

    /** El bloque de arriba del suelo en (x, z) con la laguna: dentro, el fondo del agua; lejos, el pantano de siempre. */
    public static int suelo(int x, int z) {
        if (x < X0 || x > X1 || z < Z0 || z > Z1) return alto(x, z);
        int i = (z - Z0) * AW + (x - X0);
        int s = SUELO[i];
        if (s == Integer.MIN_VALUE) SUELO[i] = s = calcularSuelo(x, z);
        return s;
    }

    private static int calcularSuelo(int x, int z) {
        int t = alto(x, z);
        double e = orilla(x, z);
        if (e >= R_FUNDE) return t;
        double u = x - CX, v = z - CZ;
        // honda (el fondo a 59-60) y mas aun bajo el embarcadero y la barca
        double f = 59.3 + Math.min(1.0, Math.max(0.0, (Math.hypot(u, v) - 14) / 14)) * 1.4 + RUIDO.fbm(x + 400, z - 800, 9, 2) * 0.8;
        for (int[] k : ISLOTES) {
            double d = Math.hypot(u - k[0], v - k[1]) + RUIDO.fbm(x, z + 600, 5, 1) * 0.8;
            if (d < k[2]) f = Math.max(f, Terreno.AGUA + (d < k[2] * 0.45 ? 0.6 : 0.2));
            else if (d < k[2] + 3) f = Math.max(f, Ruido.lerp((d - k[2]) / 3, Terreno.AGUA - 0.2, f));
        }
        if (e <= 0) return (int) Math.floor(f);
        return (int) Math.floor(Ruido.lerp(Ruido.suave(0, R_FUNDE, e), f, t + 0.5));
    }

    /** Lo que se pisa en (x, z) (en el agua: el fondo). */
    public static int pie(int x, int z) {
        return suelo(x, z) + 1;
    }

    /** Si (u, v) es del embarcadero (el octogono). */
    static boolean plataforma(int u, int v) {
        return Math.abs(u) <= R_PLATAFORMA && Math.abs(v) <= R_PLATAFORMA && Math.abs(u) + Math.abs(v) <= R_PLATAFORMA + 5;
    }

    /** Si (u, v) es del borde del embarcadero. */
    static boolean borde(int u, int v) {
        return plataforma(u, v) && (!plataforma(u + 1, v) || !plataforma(u - 1, v) || !plataforma(u, v + 1) || !plataforma(u, v - 1));
    }

    // ------------------------------------------------------------------ el plano

    /** Un podrido de la laguna: donde espera (y: lo que pisa), hacia donde mira, su variante, como espera, cuanto ronda y su grupo. */
    public record Espera(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    private static LagunaNegra unico;

    public static synchronized LagunaNegra get() {
        if (unico == null) unico = new LagunaNegra();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    /** Las columnas donde no van juncos, nenufares ni hierba (bajo lo construido). */
    private final Set<Long> ocupadas = new HashSet<>();
    /** Donde estan los faroles de almas (al caer el Barquero se vuelven faroles de verdad). */
    private final List<BlockPos> faroles = new ArrayList<>();
    /** Lo que hay (para la prueba y las vistas). */
    public int tablas, pilotes, postes;

    private LagunaNegra() {
        elementos.add(new Laguna());
        elementos.add(new Camino());
        Pilotes.Lista m = new Pilotes.Lista();
        embarcadero(m);
        muelle(m);
        caminoFaroles(m);
        elementos.add(m.pieza(3));
        Pilotes.Lista bc = new Pilotes.Lista();
        barca(bc);
        elementos.add(bc.pieza(4));
        Pilotes.Lista d = new Pilotes.Lista();
        hundida(d, -19, -12, false);
        hundida(d, 18, 23, true);
        hundida(d, -27, 6, true);
        for (int[] k : new int[][]{{-6, -24, 7}, {-23, 16, 6}, {25, -17, 5}}) arbolMuerto(d, k[0], k[1], k[2]);
        estaca(d, -4, -22);
        estaca(d, -8, -26);
        estaca(d, 22, 17);
        estaca(d, -21, 18);
        arco(d);
        elementos.add(d.pieza(4));
        trozos();
        // ---- los sitios con nombre
        int[] bs = barcaPos();
        sitio("barquero", bs[0] - CX, BARCA_SUELO + 1, bs[1] - CZ);
        sitio("embarcadero", 0, PASO + 1, 0);
        sitio("ante_embarcadero", -1, PASO + 1, 0);
        sitio("muelle", -20, PASO + 1, 0);
        sitio("orilla", MUELLE_U0 - 1, pie(CX + MUELLE_U0 - 1, CZ), 0);
        sitio("entrada", CAMINO[0] - CX, pie(CAMINO[0], CAMINO[1]), CAMINO[1] - CZ);
        sitio("barca", BARCA_U, BARCA_SUELO + 1, BARCA_V + 3);
        sitio("arco", 34, pie(CX + 34, CZ - 3), -3);
        // ---- los podridos
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
    }

    /** Donde espera el Barquero en su barca: {x, z}. */
    static int[] barcaPos() {
        return new int[]{CX + BARCA_U, CZ + BARCA_V + 1};
    }

    /** Si el chunk tiene algo de la laguna (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en lo de la laguna: ahi {@link Adorno} no pone nada. */
    static boolean cerca(int x0, int z0) {
        return x0 + 15 >= X0 && x0 <= X1 && z0 + 15 >= Z0 && z0 <= Z1;
    }

    /** Si (x, z) esta en la laguna (para el nombre del sitio). */
    public static boolean dentro(double x, double z) {
        return Math.hypot(x - CX, z - CZ) < R_LAGUNA + 6;
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} de la Laguna Negra en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los podridos de la laguna (los apunta {@link MorvainEnemigos}). */
    public List<Espera> esperas() {
        return esperas;
    }

    /** Los sitios con nombre (el embarcadero, la barca, el muelle, la entrada...): {x, y, z} (y: lo que se pisa). */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    private void sitio(String nombre, int u, int y, int v) {
        sitios.put(nombre, new int[]{CX + u, y, CZ + v});
    }

    private void ocupar(int x, int z, int r) {
        for (int dx = -r; dx <= r; dx++)
            for (int dz = -r; dz <= r; dz++) ocupadas.add(BlockPos.asLong(x + dx, 0, z + dz));
    }

    boolean ocupada(int x, int z) {
        return ocupadas.contains(BlockPos.asLong(x, 0, z));
    }

    private void farol(Pilotes.Lista L, int x, int y, int z, boolean colgado) {
        L.put(x, y, z, colgado ? Pal.SOUL_LANTERN_HANG : Pal.SOUL_LANTERN);
        faroles.add(new BlockPos(x, y, z));
    }

    // ------------------------------------------------------------------ el embarcadero y el muelle

    /** Las tablas: abeto y roble oscuro, alguna de mangle, alguna podrida (media). */
    private BlockState tabla(int x, int z) {
        int p = Hash.pct(x, PASO, z, SEMILLA + 11);
        if (p < 4) return losa(Blocks.DARK_OAK_SLAB, SlabType.TOP, false);
        if (p < 16) return Pal.MG_PLANKS;
        if (p < 52) return Pal.DO_PLANKS;
        return Pal.SP_PLANKS;
    }

    private void pilote(Pilotes.Lista L, int x, int z, int hasta) {
        int f = suelo(x, z);
        for (int y = f + 1; y < hasta; y++) L.put(x, y, z, Hash.pct(x, 1, z, SEMILLA + 13) < 60 ? Pal.DO_LOG : b(Blocks.MANGROVE_LOG));
        pilotes++;
    }

    /** Un poste de amarre: un tronco desde el fondo hasta uno por encima de las tablas, con su farol de almas (o un remate). */
    private void poste(Pilotes.Lista L, int x, int z, boolean conFarol) {
        int f = suelo(x, z);
        for (int y = f + 1; y <= PASO + 1; y++) L.put(x, y, z, Pal.DO_LOG);
        if (conFarol) farol(L, x, PASO + 2, z, false);
        else L.put(x, PASO + 2, z, losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM, false));
        ocupar(x, z, 0);
        postes++;
    }

    /**
     * El embarcadero: el octogono de tablas a {@link #PASO} sobre pilotes (en el borde cada dos y por dentro cada cuatro),
     * un dibujo de roble oscuro en el centro (dos anillos), los ocho postes de amarre de las esquinas con su farol, la
     * barandilla rota y rollos de cabo, cadenas y velas en el borde.
     */
    private void embarcadero(Pilotes.Lista L) {
        for (int u = -R_PLATAFORMA; u <= R_PLATAFORMA; u++)
            for (int v = -R_PLATAFORMA; v <= R_PLATAFORMA; v++) {
                if (!plataforma(u, v)) continue;
                int x = CX + u, z = CZ + v;
                double r = Math.hypot(u, v);
                BlockState t = tabla(x, z);
                if (Math.abs(r - 4.0) < 0.5 || Math.abs(r - 7.5) < 0.5) t = Pal.DO_PLANKS;
                if (r < 1.5) t = b(Blocks.STRIPPED_DARK_OAK_WOOD);
                L.put(x, PASO, z, t);
                tablas++;
                ocupar(x, z, 0);
                boolean b0 = borde(u, v);
                if (b0 ? (u + v) % 2 == 0 : (u % 4 == 0 && v % 4 == 0)) pilote(L, x, z, PASO);
            }
        // los ocho postes de amarre (en las esquinas del octogono, por fuera, en el agua) con su farol
        int[][] esquinas = {{-12, -5}, {-12, 5}, {12, -5}, {12, 5}, {-5, -12}, {5, -12}, {-5, 12}, {5, 12}};
        for (int[] e : esquinas) poste(L, CX + e[0], CZ + e[1], true);
        // la barandilla rota: valla de roble oscuro en el borde, con huecos (y abierta donde llegan el muelle y la barca)
        for (int u = -R_PLATAFORMA; u <= R_PLATAFORMA; u++)
            for (int v = -R_PLATAFORMA; v <= R_PLATAFORMA; v++) {
                if (!borde(u, v)) continue;
                if (u <= -R_PLATAFORMA + 1 && Math.abs(v) <= 2) continue;   // el muelle
                if (u >= R_PLATAFORMA - 1 && Math.abs(v) <= 6) continue;    // la barca
                if (Hash.pct(CX + u, PASO + 1, CZ + v, SEMILLA + 17) < 45) L.une(CX + u, PASO + 1, CZ + v, Pal.DO_FENCE);
            }
        // rollos de cabo (balas de heno con cadena), cadenas tiradas y velas en el borde
        int[][] cosas = {{-8, -7, 0}, {7, -8, 1}, {-7, 8, 1}, {8, 7, 0}, {-10, 3, 2}, {3, -10, 2}, {0, 10, 2}};
        for (int[] k : cosas) {
            int x = CX + k[0], z = CZ + k[1];
            switch (k[2]) {
                case 0 -> L.put(x, PASO + 1, z, tronco(Blocks.HAY_BLOCK, Direction.Axis.Y));
                case 1 -> L.put(x, PASO + 1, z, b(Blocks.BARREL).setValue(BlockStateProperties.FACING, Direction.UP));
                default -> L.put(x, PASO + 1, z, b(Blocks.CANDLE).setValue(CandleBlock.CANDLES, 2).setValue(CandleBlock.LIT, true));
            }
        }
        L.put(CX - 9, PASO + 1, CZ - 6, tronco(Blocks.CHAIN, Direction.Axis.X));
        L.put(CX + 6, PASO + 1, CZ + 9, tronco(Blocks.CHAIN, Direction.Axis.Z));
    }

    /** El muelle: tres de ancho desde la orilla del oeste hasta el embarcadero, con pilotes y postes con farol a los lados. */
    private void muelle(Pilotes.Lista L) {
        for (int u = MUELLE_U0; u <= MUELLE_U1; u++)
            for (int v = -1; v <= 1; v++) {
                int x = CX + u, z = CZ + v;
                if (alto(x, z) >= PASO && suelo(x, z) >= PASO) continue;   // en tierra firme: ahi sigue el camino
                L.put(x, PASO, z, tabla(x, z));
                tablas++;
                ocupar(x, z, 0);
                if (v != 0 && (u - MUELLE_U0) % 3 == 0 && suelo(x, z) < PASO - 1) pilote(L, x, z, PASO);
            }
        // los postes de amarre a los lados, cada seis, alternando (el farol en uno de cada dos)
        int k = 0;
        for (int u = MUELLE_U0 + 4; u <= MUELLE_U1 - 3; u += 6, k++) {
            int z = CZ + (k % 2 == 0 ? -2 : 2), x = CX + u;
            if (suelo(x, z) >= PASO) continue;
            poste(L, x, z, k % 2 == 0);
        }
        // donde el muelle sale de la orilla: si la tierra queda mas baja que las tablas, un escalon
        for (int v = -1; v <= 1; v++) {
            int u = MUELLE_U0 - 1, x = CX + u, z = CZ + v;
            if (suelo(x, z) == PASO - 1 && !L.hay(x, PASO, z)) L.put(x, PASO, z, escalera(Blocks.DARK_OAK_STAIRS, Direction.EAST, false, false));
        }
    }

    /** Los faroles del camino desde el santuario: un poste de abeto con su farol de almas cada nueve bloques, a un lado y al otro. */
    private void caminoFaroles(Pilotes.Lista L) {
        double x0 = CAMINO[0], z0 = CAMINO[1], x1 = CAMINO[2], z1 = CAMINO[3];
        double largo = Math.hypot(x1 - x0, z1 - z0), nx = -(z1 - z0) / largo, nz = (x1 - x0) / largo;
        int k = 0;
        for (double s = 4; s < largo - 2; s += 9, k++) {
            double lado = k % 2 == 0 ? 2.6 : -2.6;
            int x = (int) Math.floor(x0 + (x1 - x0) * s / largo + nx * lado), z = (int) Math.floor(z0 + (z1 - z0) * s / largo + nz * lado);
            if (Pilotes.cercaSantuario(x, z)) continue;
            int y = suelo(x, z);
            if (y < Terreno.AGUA) continue;
            L.put(x, y + 1, z, Pal.SP_FENCE);
            L.put(x, y + 2, z, Pal.SP_FENCE);
            farol(L, x, y + 3, z, false);
            ocupar(x, z, 0);
        }
    }

    // ------------------------------------------------------------------ la barca del Barquero

    /**
     * La barca: cinco de ancho por once de largo (a lo largo de z, la proa al norte), amarrada al este del embarcadero. El
     * casco de roble oscuro hasta un bloque bajo el agua, el suelo a ras de agua ({@link #BARCA_SUELO}), la borda de medias
     * losas, la proa y la popa de escaleras, el poste de la proa con su farol de almas y una estaca de amarre al embarcadero.
     */
    private void barca(Pilotes.Lista L) {
        int cx = CX + BARCA_U, cz = CZ + BARCA_V, y0 = BARCA_SUELO;
        for (int du = -2; du <= 2; du++)
            for (int dv = -5; dv <= 5; dv++) {
                int x = cx + du, z = cz + dv;
                boolean punta = Math.abs(dv) == 5, lado = Math.abs(du) == 2;
                if (punta && lado) continue;
                // el casco por debajo (en el agua)
                L.put(x, y0 - 1, z, Pal.DO_PLANKS);
                if (punta) {
                    // proa y popa: escaleras que suben hacia fuera
                    L.put(x, y0, z, escalera(Blocks.DARK_OAK_STAIRS, dv < 0 ? Direction.SOUTH : Direction.NORTH, false, false));
                    L.put(x, y0 + 1, z, losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM, false));
                } else if (lado) {
                    L.put(x, y0, z, Pal.DO_PLANKS);
                    L.put(x, y0 + 1, z, losa(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM, false));
                } else L.put(x, y0, z, (dv + du) % 3 == 0 ? Pal.DO_PLANKS : Pal.SP_PLANKS);
                ocupar(x, z, 0);
            }
        // los bancos de remar
        L.put(cx - 1, y0 + 1, cz - 3, losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM, false));
        L.put(cx + 1, y0 + 1, cz - 3, losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM, false));
        // el poste de la proa con su farol colgando sobre el agua
        L.put(cx, y0 + 1, cz - 5, Pal.DO_FENCE);
        L.put(cx, y0 + 2, cz - 5, Pal.DO_FENCE);
        L.put(cx, y0 + 3, cz - 5, Pal.DO_FENCE);
        L.une(cx, y0 + 3, cz - 6, Pal.DO_FENCE);
        farol(L, cx, y0 + 2, cz - 6, true);
        // un remo de repuesto tirado en el fondo y unas cadenas
        L.put(cx - 1, y0 + 1, cz + 2, tronco(Blocks.CHAIN, Direction.Axis.Z));
        // la estaca de amarre en el agua, entre la barca y el embarcadero
        int ex = CX + R_PLATAFORMA + 2, ez = cz - 3;
        int f = suelo(ex, ez);
        for (int y = f + 1; y <= y0 + 1; y++) L.put(ex, y, ez, Pal.DO_LOG);
        L.put(ex, y0 + 2, ez, tronco(Blocks.CHAIN, Direction.Axis.Y));
        ocupar(ex, ez, 0);
    }

    // ------------------------------------------------------------------ barcas hundidas, mangles, estacas y el arco

    /** Una barca vieja de tres por seis, medio hundida (a lo largo de z, o de x). */
    private void hundida(Pilotes.Lista L, int u, int v, boolean enX) {
        int y = Terreno.AGUA - 1;
        for (int i = 0; i < 6; i++)
            for (int j = -1; j <= 1; j++) {
                int x = CX + (enX ? u + i : u + j), z = CZ + (enX ? v + j : v + i);
                int yy = i >= 4 ? y + 1 : y;
                boolean agua = yy <= Terreno.AGUA;
                boolean punta = i == 0 || i == 5;
                if (punta && j != 0) continue;
                BlockState s;
                if (punta) {
                    Direction d = enX ? (i == 0 ? Direction.EAST : Direction.WEST) : (i == 0 ? Direction.SOUTH : Direction.NORTH);
                    s = escalera(Blocks.SPRUCE_STAIRS, d, false, agua);
                } else if (j == 0) s = losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM, agua);
                else {
                    Direction d = enX ? (j < 0 ? Direction.SOUTH : Direction.NORTH) : (j < 0 ? Direction.EAST : Direction.WEST);
                    s = escalera(Blocks.SPRUCE_STAIRS, d, false, agua);
                }
                L.put(x, yy, z, s);
                ocupar(x, z, 0);
            }
    }

    /** Un mangle muerto en un islote: el tronco, las raices y dos o tres ramas con alguna telarana. */
    private void arbolMuerto(Pilotes.Lista L, int u, int v, int alto) {
        int x = CX + u, z = CZ + v;
        int f = suelo(x, z);
        long sem = SEMILLA + 41 + u * 31L + v;
        for (int y = f + 1; y <= f + alto; y++) L.put(x, y, z, b(Blocks.MANGROVE_LOG));
        for (Direction d : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
            int rx = x + d.getStepX(), rz = z + d.getStepZ();
            int rf = suelo(rx, rz);
            for (int y = rf + 1; y <= f + 2; y++)
                L.put(rx, y, rz, b(Blocks.MANGROVE_ROOTS).setValue(BlockStateProperties.WATERLOGGED, y <= Terreno.AGUA));
            ocupar(rx, rz, 0);
        }
        Direction[] dir = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (int i = 0; i < 3; i++) {
            Direction d = dir[(int) ((sem >>> (i * 3)) & 3)];
            int h = f + alto - 2 + i;
            int largo = 2 + i % 2;
            for (int k = 1; k <= largo; k++)
                L.put(x + d.getStepX() * k, h + (k == largo ? 1 : 0), z + d.getStepZ() * k, tronco(Blocks.MANGROVE_LOG, d.getAxis()));
            if (i == 1) L.put(x + d.getStepX(), h - 1, z + d.getStepZ(), Pal.COBWEB);
        }
        ocupar(x, z, 1);
    }

    /** Una estaca con una calavera (los que no pagaron el peaje). */
    private void estaca(Pilotes.Lista L, int u, int v) {
        int x = CX + u, z = CZ + v;
        int f = suelo(x, z);
        L.put(x, f + 1, z, Pal.SP_FENCE);
        L.put(x, f + 2, z, Pal.SP_FENCE);
        L.put(x, f + 3, z, b(Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, (int) (Hash.pct(x, f, z, SEMILLA + 5) * 16 / 100)));
        ocupar(x, z, 0);
    }

    /** En la otra orilla (al este): un arco apuntado de piedra medio caido, con musgo y lianas: la orilla a la que nunca llega. */
    private void arco(Pilotes.Lista L) {
        int u = 34, v0 = -3;
        int y0 = Math.max(Terreno.AGUA, suelo(CX + u, CZ + v0));
        long sem = SEMILLA + 61;
        for (int dv = -3; dv <= 3; dv++) {
            int x = CX + u, z = CZ + v0 + dv;
            int f = suelo(x, z);
            boolean pilar = Math.abs(dv) == 3;
            // el cimiento hasta el suelo
            for (int y = f + 1; y <= y0; y++) L.put(x, y, z, Pal.MOSSY_STONE_BRICKS);
            if (pilar) {
                int alto = dv < 0 ? 7 : 4;   // el del norte entero, el del sur partido
                for (int h = 1; h <= alto; h++) L.put(x, y0 + h, z, Hash.pct(x, h, z, sem) < 50 ? Pal.MOSSY_STONE_BRICKS : b(Blocks.CRACKED_STONE_BRICKS));
            } else if (dv <= 0) {
                // medio arco que queda (del norte)
                int h = 7 + (dv == -2 ? 0 : dv == -1 ? 1 : 1);
                L.put(x, y0 + h, z, dv == -2 ? escalera(Blocks.MOSSY_STONE_BRICK_STAIRS, Direction.NORTH, true, false) : Pal.MOSSY_STONE_BRICKS);
                if (dv == 0) L.put(x, y0 + 9, z, Pal.MOSSY_STONE_BRICKS);
            }
            ocupar(x, z, 0);
        }
        // las piedras caidas del lado del sur
        L.put(CX + u - 1, y0 + 1, CZ + v0 + 2, b(Blocks.MOSSY_COBBLESTONE));
        L.put(CX + u + 1, y0 + 1, CZ + v0 + 1, b(Blocks.CRACKED_STONE_BRICKS));
        L.put(CX + u - 2, Math.max(suelo(CX + u - 2, CZ + v0), Terreno.AGUA) + 1, CZ + v0 - 1, Pal.MOSSY_STONE_BRICKS);
        for (int h = 1; h <= 3; h++) L.put(CX + u, y0 + 7 - h, CZ + v0 - 2, b(Blocks.VINE).setValue(BlockStateProperties.SOUTH, true));
    }

    // ------------------------------------------------------------------ lo que lleva datos: el arcon del peaje

    private void trozos() {
        int cx = CX + BARCA_U, cz = CZ + BARCA_V, y = BARCA_SUELO + 1;
        trozo(cx, y, cz + 4, c -> c.chest(cx, y, cz + 4, Direction.NORTH, Velmor.BOTIN_BUENO));
    }

    private void trozo(int x, int y, int z, Trozo.Obra o) {
        elementos.add(new Trozo(6, x, y, z, x, y, z, o));
    }

    // ------------------------------------------------------------------ los podridos (sus pasajeros)

    private void ponerEsperas() {
        int P = Podrido.PODRIDO, F = Podrido.FATUO, H = Podrido.HINCHADO;
        // los dos del muelle: flotando boca abajo junto a el (se alzan al pasar)
        agua(P, -26, 3, "muelle");
        agua(H, -17, -3, "muelle");
        // los pasajeros: flotando alrededor del embarcadero, lejos de sus tablas (no se alzan con la pelea: los llama el)
        agua(P, 0, -18, "pasajeros");
        agua(P, -16, 9, "pasajeros");
        agua(H, 3, 18, "pasajeros");
        agua(P, 21, -12, "pasajeros");
        // el del fuego fatuo, de pie en el islote del norte, entre las estacas
        int fx = CX - 4, fz = CZ - 26;
        esperas.add(new Espera(F, fx, Math.max(pie(fx, fz), Terreno.AGUA + 1), fz, giro(CX - fx, CZ - fz), Enemigo.DE_PIE, 2, "pasajeros"));
    }

    private void agua(int variante, int u, int v, String grupo) {
        int x = CX + u, z = CZ + v;
        esperas.add(new Espera(variante, x, pie(x, z), z, giro(-u, -v), Enemigo.TUMBADO, 0, grupo));
        ocupar(x, z, 1);
    }

    // ------------------------------------------------------------------ el camino y la laguna

    /** Lo lejos que esta (x, z) del camino del santuario. */
    static double alCamino(double x, double z) {
        double x0 = CAMINO[0], z0 = CAMINO[1], x1 = CAMINO[2], z1 = CAMINO[3];
        double dx = x1 - x0, dz = z1 - z0, l2 = dx * dx + dz * dz;
        double t = Math.max(0, Math.min(1, ((x - x0) * dx + (z - z0) * dz) / l2));
        return Math.hypot(x - (x0 + dx * t), z - (z0 + dz * t));
    }

    /** El camino de tierra pisada y barro seco del santuario al muelle (en la caja de la laguna y fuera del agua). */
    private final class Camino extends Element {
        Camino() {
            super(SEMILLA + 71);
            this.layer = 2;
            bounds(Math.min(CAMINO[0], CAMINO[2]) - 3, 40, Math.min(CAMINO[1], CAMINO[3]) - 3,
                    Math.max(CAMINO[0], CAMINO[2]) + 3, 140, Math.max(CAMINO[1], CAMINO[3]) + 3);
        }

        @Override
        public void build(Canvas c) {
            for (int x = Math.max(c.minX, x0); x <= Math.min(c.maxX, x1); x++)
                for (int z = Math.max(c.minZ, z0); z <= Math.min(c.maxZ, z1); z++) {
                    double d = alCamino(x + 0.5, z + 0.5) + RUIDO.fbm(x + 90, z, 3, 1) * 0.35;
                    if (d > CAMINO_ANCHO || Pilotes.cercaSantuario(x, z)) continue;
                    int s = suelo(x, z);
                    if (s < Terreno.AGUA) continue;
                    int p = Hash.pct(x, s, z, seed);
                    c.set(x, s, z, p < 40 ? b(Blocks.PACKED_MUD) : p < 70 ? b(Blocks.COARSE_DIRT) : p < 88 ? b(Blocks.ROOTED_DIRT) : b(Blocks.MUD));
                    // lo que crecia encima, fuera
                    if (!c.get(x, s + 1, z).isAir() && !c.get(x, s + 1, z).isSolid()) c.set(x, s + 1, z, Pal.AIR);
                }
        }
    }

    /**
     * El suelo: dentro de la laguna, el fondo (barro, arcilla y grava) con el agua hasta arriba; los islotes y la orilla de
     * barro, con juncos donde toca el agua y hierba; nenufares y algas en el agua. Nada cerca de un santuario.
     */
    private final class Laguna extends Element {
        Laguna() {
            super(SEMILLA + 51);
            this.layer = 1;
            bounds(CX - R_LAGUNA - R_FUNDE - 6, 40, Z0, X1, 140, Z1);
        }

        @Override
        public void build(Canvas c) {
            for (int x = c.minX; x <= c.maxX; x++)
                for (int z = c.minZ; z <= c.maxZ; z++) {
                    if (x < x0 || x > x1 || z < z0 || z > z1) continue;
                    if (Pilotes.cercaSantuario(x, z)) continue;
                    int t = alto(x, z), s = suelo(x, z);
                    if (s == t && orilla(x, z) >= R_FUNDE) continue;
                    if (s < t) for (int y = s + 1; y <= t; y++) c.set(x, y, z, y <= Terreno.AGUA ? Pal.WATER : Pal.AIR);
                    else if (s > t) for (int y = t + 1; y <= s; y++) c.set(x, y, z, b(Blocks.MUD));
                    if (s < Terreno.AGUA && s >= t) for (int y = s + 1; y <= Terreno.AGUA; y++) c.set(x, y, z, Pal.WATER);
                    boolean bajo = s < Terreno.AGUA;
                    int p = Hash.pct(x, s, z, seed);
                    BlockState top = bajo ? (p < 50 ? b(Blocks.MUD) : p < 75 ? b(Blocks.CLAY) : p < 90 ? Pal.GRAVEL : b(Blocks.SOUL_SOIL))
                            : (p < 45 ? b(Blocks.MUD) : p < 62 ? b(Blocks.MUDDY_MANGROVE_ROOTS) : p < 80 ? Pal.GRASS_BLOCK : Pal.MOSS);
                    c.set(x, s, z, top);
                    // lo que crecia encima del suelo viejo (hierba, setas), fuera
                    if (s < t && !c.get(x, t + 1, z).isAir() && !c.get(x, t + 1, z).isSolid()) c.set(x, t + 1, z, Pal.AIR);
                    if (ocupada(x, z) || alCamino(x + 0.5, z + 0.5) < CAMINO_ANCHO + 0.6) continue;
                    int q = Hash.pct(x, s + 1, z, seed + 1);
                    if (bajo) {
                        if (q < 6 && s >= Terreno.AGUA - 3) c.set(x, Terreno.AGUA + 1, z, b(Blocks.LILY_PAD));
                        else if (q > 86 && s <= Terreno.AGUA - 2) c.set(x, s + 1, z, b(Blocks.SEAGRASS));
                        continue;
                    }
                    if (s == Terreno.AGUA && q < 45 && tocaAgua(x, z)) {
                        int h = 1 + q % 3;
                        for (int k = 1; k <= h; k++) c.set(x, s + k, z, b(Blocks.SUGAR_CANE));
                    } else if (q < 20) c.set(x, s + 1, z, b(Blocks.GRASS));
                    else if (q < 26) c.set(x, s + 1, z, b(Blocks.FERN));
                    else if (q < 29) c.set(x, s + 1, z, b(Blocks.DEAD_BUSH));
                }
        }
    }

    /** Si junto a (x, z) hay agua de la laguna (para los juncos). */
    static boolean tocaAgua(int x, int z) {
        return suelo(x + 1, z) < Terreno.AGUA || suelo(x - 1, z) < Terreno.AGUA || suelo(x, z + 1) < Terreno.AGUA || suelo(x, z - 1) < Terreno.AGUA;
    }

    // ------------------------------------------------------------------ al caer el Barquero

    /** Si el Barquero ha caido: sus pasajeros ya no vuelven y la laguna esta en paz. */
    public static boolean enPaz(net.minecraft.server.MinecraftServer srv) {
        return com.darkcraft.enemigos.Puestos.muerto(srv, MorvainEnemigos.BARCA, MorvainEnemigos.barquero());
    }

    /**
     * Ha caido el Barquero: los pasajeros que siguen flotando descansan (se deshacen en almas; los que ya andan sueltos siguen
     * peleando) y los faroles de almas de la laguna se vuelven faroles de verdad.
     */
    public static void liberar(ServerLevel m) {
        int n = 0;
        if (com.darkcraft.enemigos.Puestos.sitio(MorvainEnemigos.LAGUNA) != null)
            for (int i = 0; i < MorvainEnemigos.laguna().size(); i++)
                if (com.darkcraft.enemigos.Puestos.enemigo(MorvainEnemigos.LAGUNA, i) instanceof Podrido p && p.isAlive()
                        && p.postura() == Enemigo.TUMBADO && p.accion() == 0 && "pasajeros".equals(MorvainEnemigos.laguna().get(i).grupo())) {
                    m.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL, p.getX(), p.getY() + 0.8, p.getZ(), 14, 0.35, 0.4, 0.35, 0.03);
                    m.playSound(null, p.getX(), p.getY() + 0.5, p.getZ(), net.minecraft.sounds.SoundEvents.SOUL_ESCAPE, net.minecraft.sounds.SoundSource.HOSTILE, 1.6f, 0.7f);
                    p.disolver();
                    n++;
                }
        int f = encender(m);
        DarkCraftMod.LOGGER.info("[DarkCraft] El Barquero cae: descansan {} pasajeros, {} faroles se encienden de verdad", n, f);
    }

    /** Cambia los faroles de almas de la laguna por faroles de verdad (los de chunks cargados); devuelve cuantos ha cambiado. */
    private static int encender(ServerLevel m) {
        int n = 0;
        for (BlockPos p : get().faroles) {
            if (!m.isLoaded(p)) continue;
            BlockState s = m.getBlockState(p);
            if (!s.is(Blocks.SOUL_LANTERN)) continue;
            m.setBlock(p, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, s.getValue(LanternBlock.HANGING)), 2);
            n++;
        }
        return n;
    }

    /** Lo contrario (el comando que hace volver al Barquero): los faroles de la laguna, otra vez de almas. */
    public static int almas(ServerLevel m) {
        int n = 0;
        for (BlockPos p : get().faroles) {
            if (!m.isLoaded(p)) continue;
            BlockState s = m.getBlockState(p);
            if (!s.is(Blocks.LANTERN)) continue;
            m.setBlock(p, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, s.getValue(LanternBlock.HANGING)), 2);
            n++;
        }
        return n;
    }

    /** Cada 5 s, con alguien cerca: con el Barquero vencido, los faroles de la laguna (los de chunks que no estaban). */
    public static void paz(ServerLevel m) {
        boolean alguien = false;
        for (var p : m.players())
            if (p.distanceToSqr(CX, p.getY(), CZ) < 110 * 110) {
                alguien = true;
                break;
            }
        if (alguien && enPaz(m.getServer())) encender(m);
    }

    /** Para la prueba y el comando: cuantos faroles de la laguna son de almas y cuantos de verdad. */
    public static String faroles(ServerLevel m) {
        int almas = 0, verdad = 0, nada = 0;
        for (BlockPos p : get().faroles) {
            if (!m.isLoaded(p)) {
                nada++;
                continue;
            }
            BlockState s = m.getBlockState(p);
            if (s.is(Blocks.SOUL_LANTERN)) almas++;
            else if (s.is(Blocks.LANTERN)) verdad++;
            else nada++;
        }
        return "faroles de la laguna: " + almas + " de almas, " + verdad + " de verdad" + (nada > 0 ? ", " + nada + " sin ver" : "");
    }

    // ------------------------------------------------------------------ para la prueba

    /** Lo que hay en la laguna ya generada (para la prueba): el arcon de la barca, el agua, las tablas del muelle y del embarcadero. */
    public static String resumen(ServerLevel m) {
        LagunaNegra a = get();
        int[] bs = barcaPos();
        boolean arcon = m.getBlockState(new BlockPos(CX + BARCA_U, BARCA_SUELO + 1, CZ + BARCA_V + 4)).is(Blocks.CHEST);
        boolean barca = m.getBlockState(new BlockPos(bs[0], BARCA_SUELO, bs[1])).is(net.minecraft.tags.BlockTags.PLANKS);
        int agua = 0, muestras = 0, tablas = 0, hay = 0;
        for (int u = -28; u <= 28; u += 4)
            for (int v = -28; v <= 28; v += 4) {
                if (Math.hypot(u, v) < R_PLATAFORMA + 3 || Math.hypot(u, v) > R_LAGUNA - 6) continue;
                if (Math.abs(v) <= 2 && u < 0) continue;
                muestras++;
                if (m.getFluidState(new BlockPos(CX + u, Terreno.AGUA, CZ + v)).is(net.minecraft.tags.FluidTags.WATER)) agua++;
            }
        for (int u = -R_PLATAFORMA; u <= R_PLATAFORMA; u++)
            for (int v = -R_PLATAFORMA; v <= R_PLATAFORMA; v++) {
                if (!plataforma(u, v)) continue;
                hay++;
                BlockState s = m.getBlockState(new BlockPos(CX + u, PASO, CZ + v));
                if (s.is(net.minecraft.tags.BlockTags.PLANKS) || s.getBlock() instanceof net.minecraft.world.level.block.SlabBlock
                        || s.is(Blocks.STRIPPED_DARK_OAK_WOOD)) tablas++;
            }
        return "arcon de la barca " + (arcon ? "SI" : "NO") + ", suelo de la barca " + (barca ? "SI" : "NO") + ", agua en la laguna " + agua + "/" + muestras
                + ", tablas del embarcadero " + tablas + "/" + hay + "; " + faroles(m);
    }
}
