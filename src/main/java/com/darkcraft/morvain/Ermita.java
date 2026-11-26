package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Helado;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.darkcraft.morvain.Monasterio.b;
import static com.darkcraft.morvain.Monasterio.escalera;
import static com.darkcraft.morvain.Monasterio.losa;
import static com.darkcraft.morvain.Monasterio.nieve;
import static com.darkcraft.morvain.Monasterio.piedra;
import static com.darkcraft.morvain.Monasterio.vela;

/**
 * La Ermita de la Cumbre (parte 7l): donde espera <b>Hrodric, el Abad de la Cumbre</b> ({@code enemigos/Abad}), el jefe de
 * las Montanas Nevadas, el abad del Monasterio de la Helada que "subio a la cumbre a pedirle al frio que nos perdonara, y el
 * frio le contesto" (libro perdido 21). En un circo de la montana al norte del monasterio (el centro en ({@value #CX},
 * {@value #CZ}), {@value #SUELO}):
 * <ul>
 * <li>la <b>escalera de los penitentes</b>: sale del huerto del monasterio por el postigo de la muralla del norte y sube
 * recta por la ladera, tres de ancho, hasta el circo; donde la montana se empina va cortada en la roca entre dos muros (con
 * hornacinas con faroles y las estaciones heladas: cruces de hielo en el muro) y donde no, con su pretil y faroles en
 * postes; helados de rodillas en los rellanos;</li>
 * <li>el <b>calvario</b>: una plaza redonda (radio {@value #R_PLAZA}) enlosada y nevada, con la rosa de hielo en el centro
 * (donde el abad reza de rodillas, de cara a la ermita), cuatro <b>braseros</b> encendidos (con la ventisca, el unico sitio
 * donde no se hiela uno: {@link #braseros}), ocho pilares rotos alrededor, un murete caido y la puerta de dos pilares por
 * donde llega la escalera;</li>
 * <li>la <b>ermita</b> al norte, en su terraza cortada en la ladera: muros de piedra, tejado de pizarra con nieve, la
 * espadana con su campana, dentro el altar con velas y la cruz de hielo, el arcon y dos reclinatorios;</li>
 * <li>detras, en la ladera, la <b>gran cruz de hielo</b>.</li>
 * </ul>
 * Se dibuja como el monasterio, chunk a chunk al adornar; en su caja {@link Adorno} no pone nada. Al caer el abad
 * ({@link #liberar}) los hermanos que seguian de rodillas en la iglesia del monasterio descansan por fin y la campana da su
 * ultimo toque; despues ya no vuelven ni la campana suena sola.
 */
public final class Ermita {
    public static final long SEMILLA = 0xE4A1_7A00_3DL;
    /** El centro del calvario (la rosa de hielo, donde reza el abad). */
    public static final int CX = 3326, CZ = -5612;
    /** El suelo del calvario (lo que se pisa esta en {@link #PISO}). */
    public static final int SUELO = 385, PISO = SUELO + 1;
    public static final int R_PLAZA = 16;
    /** Lo que tarda el calvario en fundirse con la ladera. */
    static final double FUNDE = 12.5;
    /** La terraza de la ermita (u = x - CX, v = z - CZ) y la ermita (los muros), con su suelo un escalon por encima. */
    static final int PU0 = -8, PU1 = 8, PV0 = -39, PV1 = -20;
    static final int EU0 = -5, EU1 = 5, EV0 = -35, EV1 = -23, E_SUELO = SUELO + 1;
    /** La gran cruz de hielo, en la ladera detras de la ermita. */
    static final int CRUZ_V = -46;
    /** La escalera: tres de ancho (x ESC_X-1..ESC_X+1), del postigo del monasterio (ESC_Z0) al calvario (ESC_Z1). */
    public static final int ESC_X = CX, ESC_Z0 = Monasterio.CZ + Monasterio.V0 - 1, ESC_Z1 = CZ + R_PLAZA;
    /** Lo que toca: el circo, la ermita, la cruz y la escalera. */
    public static final int X0 = CX - 34, X1 = CX + 34, Z0 = CZ - 56, Z1 = ESC_Z0;
    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/ermita");

    private static final Ruido RUIDO = new Ruido(SEMILLA);
    private static final int AW = X1 - X0 + 1, AD = Z1 - Z0 + 1;
    private static final int[] ALTO = new int[AW * AD];
    /** Lo que se pisa en cada escalon de la escalera (de ESC_Z1 a ESC_Z0). */
    private static final int[] ESC_H = new int[ESC_Z0 - ESC_Z1 + 1];

    static {
        Arrays.fill(ALTO, Integer.MIN_VALUE);
        // la escalera: sube con la ladera (un bloque por escalon como mucho); donde la ladera se empina se queda atras y va
        // cortada en la roca; al final tiene que llegar al calvario (si no, se levanta antes)
        int prev = Monasterio.PISO;
        for (int z = ESC_Z0; z >= ESC_Z1; z--) {
            int t = (int) Math.floor(Terreno.columna(ESC_X, z).altura()) + 1;
            int h = Math.max(prev, Math.min(t, prev + 1));
            h = Math.min(h, PISO);
            ESC_H[z - ESC_Z1] = h;
            prev = h;
        }
        ESC_H[0] = PISO;
        for (int z = ESC_Z1 + 1; z <= ESC_Z0; z++) ESC_H[z - ESC_Z1] = Math.max(ESC_H[z - ESC_Z1], ESC_H[z - 1 - ESC_Z1] - 1);
    }

    /** El bloque de arriba del terreno de Morvain en (x, z) (sin la ermita). */
    public static int alto(int x, int z) {
        if (x < X0 || x > X1 || z < Z0 || z > Z1) return (int) Math.floor(Terreno.columna(x, z).altura());
        int i = (z - Z0) * AW + (x - X0);
        int v = ALTO[i];
        if (v == Integer.MIN_VALUE) ALTO[i] = v = (int) Math.floor(Terreno.columna(x, z).altura());
        return v;
    }

    /** Lo que se pisa en el escalon z de la escalera. */
    public static int escalon(int z) {
        if (z < ESC_Z1) return PISO;
        if (z > ESC_Z0) return Monasterio.PISO;
        return ESC_H[z - ESC_Z1];
    }

    static boolean plataforma(int u, int v) {
        return u >= PU0 && u <= PU1 && v >= PV0 && v <= PV1;
    }

    private static double aRect(int u, int v) {
        double du = u < PU0 ? PU0 - u : u > PU1 ? u - PU1 : 0, dv = v < PV0 ? PV0 - v : v > PV1 ? v - PV1 : 0;
        return Math.hypot(du, dv);
    }

    /** Lo alto del suelo que deja la terraza del calvario en (x, z), o MIN_VALUE donde no toca el terreno. */
    static int objetivo(int x, int z) {
        int u = x - CX, v = z - CZ;
        if (plataforma(u, v)) return E_SUELO;
        double dDisco = Math.max(0, Math.hypot(u, v) - (R_PLAZA + 1.5)), dRect = aRect(u, v);
        if (dDisco <= 0) return SUELO;
        double d = Math.min(dDisco, dRect) + RUIDO.fbm(x, z, 7, 2) * 1.4;
        if (d >= FUNDE) return Integer.MIN_VALUE;
        int base = dRect < dDisco ? E_SUELO : SUELO;
        if (d <= 0) return base;
        int t = alto(x, z);
        return (int) Math.round(Ruido.lerp(Ruido.suave(0, FUNDE, d), base, t));
    }

    /** El bloque de arriba del suelo en (x, z) con la terraza del calvario (sin la escalera). */
    public static int superficie(int x, int z) {
        int o = objetivo(x, z);
        return o == Integer.MIN_VALUE ? alto(x, z) : o;
    }

    /** Lo que se pisa en (x, z): en la escalera, su escalon; si no, el suelo. */
    public static int pie(int x, int z) {
        if (Math.abs(x - ESC_X) <= 1 && z >= ESC_Z1 && z <= ESC_Z0) return escalon(z);
        return superficie(x, z) + 1;
    }

    // ------------------------------------------------------------------ el plano

    /** Un helado de la escalera: donde espera (y: lo que pisa), hacia donde mira, su variante, como espera, cuanto ronda y su grupo. */
    public record Espera(int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {}

    private static Ermita unico;

    public static synchronized Ermita get() {
        if (unico == null) unico = new Ermita();
        return unico;
    }

    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Map<String, int[]> sitios = new LinkedHashMap<>();
    private final List<Espera> esperas = new ArrayList<>();
    /** Donde esta el fuego de cada brasero (con la ventisca, junto a ellos no se hiela uno). */
    private final List<BlockPos> braseros = new ArrayList<>();
    /** Lo que hay (para la prueba y las vistas). */
    public int escalones, cortados, pilares, hornacinas, estaciones;

    private Ermita() {
        elementos.add(new Terraza());
        elementos.add(new Escalera());
        Pilotes.Lista m = new Pilotes.Lista();
        calvario(m);
        pilares(m);
        braseros(m);
        ermita(m);
        cruz(m);
        elementos.add(m.pieza(3));
        trozos();
        for (int z = ESC_Z1; z <= ESC_Z0; z++)
            if (escalon(z) > escalon(z + 1) || z == ESC_Z0) escalones++;
        // ---- los sitios con nombre
        sitio("postigo", ESC_X, Monasterio.PISO, ESC_Z0 + 2);
        sitio("escalera", ESC_X, escalon(ESC_Z0 - 2), ESC_Z0 - 2);
        int mitad = (ESC_Z0 + ESC_Z1) / 2;
        sitio("mitad", ESC_X, escalon(mitad), mitad);
        int hondo = ESC_Z0, cuanto = -1;
        for (int z = ESC_Z1; z <= ESC_Z0; z++) {
            int c = Math.min(superficie(ESC_X - 2, z), superficie(ESC_X + 2, z)) + 1 - escalon(z);
            if (c > cuanto) {
                cuanto = c;
                hondo = z;
            }
        }
        sitio("corte", ESC_X, escalon(hondo), hondo);
        sitio("entrada", CX, PISO, CZ + R_PLAZA + 1);
        sitio("ante_abad", CX, PISO, CZ + 9);
        sitio("abad", CX, PISO, CZ);
        sitio("calvario", CX - 8, PISO, CZ + 3);
        sitio("ermita", CX, E_SUELO + 1, CZ + EV1 - 4);
        sitio("puerta_ermita", CX, PISO, CZ + EV1 + 2);
        sitio("altar", CX, E_SUELO + 1, CZ + EV0 + 3);
        sitio("arcon", CX + 3, E_SUELO + 1, CZ + EV0 + 2);
        sitio("cruz", CX, superficie(CX, CZ + CRUZ_V + 4) + 1, CZ + CRUZ_V + 4);
        int k = 0;
        for (BlockPos b : braseros) sitio("brasero_" + k++, b.getX() + (b.getX() < CX ? 2 : -2), PISO, b.getZ() + (b.getZ() < CZ ? 2 : -2));
        // ---- los helados de la escalera
        ponerEsperas();
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(Velmor.clave(x, z), q -> new ArrayList<>()).add(e);
    }

    /** Si el chunk tiene algo de la ermita (sin crear el plano si el chunk esta lejos). */
    public static boolean toca(int chunkX, int chunkZ) {
        int x0 = chunkX << 4, z0 = chunkZ << 4;
        if (x0 + 15 < X0 || x0 > X1 || z0 + 15 < Z0 || z0 > Z1) return false;
        return get().indice.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) cae en lo de la ermita (el circo o la escalera): ahi {@link Adorno} no pone nada. */
    static boolean cerca(int x0, int z0) {
        boolean circo = x0 + 15 >= X0 && x0 <= X1 && z0 + 15 >= Z0 && z0 <= CZ + R_PLAZA + 16;
        boolean escalera = x0 + 15 >= ESC_X - 8 && x0 <= ESC_X + 8 && z0 + 15 >= ESC_Z1 && z0 <= ESC_Z0;
        return circo || escalera;
    }

    /** Si (x, z) esta en el calvario o la ermita (para el nombre del sitio). */
    public static boolean dentro(double x, double z) {
        double u = x - CX, v = z - CZ;
        return Math.hypot(u, v) < R_PLAZA + 6 || (u >= PU0 - 2 && u <= PU1 + 2 && v >= PV0 - 9 && v <= PV1);
    }

    /** Si (x, z) esta en la escalera de los penitentes. */
    public static boolean enEscalera(double x, double z) {
        return Math.abs(x - (ESC_X + 0.5)) < 4 && z > ESC_Z1 + 2 && z < ESC_Z0 + 1;
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
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} de la ermita en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public List<Element> elementos() {
        return elementos;
    }

    /** Los helados de la escalera (los apunta {@link MorvainEnemigos}). */
    public List<Espera> esperas() {
        return esperas;
    }

    /** Donde arde cada brasero. */
    public List<BlockPos> braseros() {
        return braseros;
    }

    /** Los sitios con nombre (el postigo, la escalera, el calvario, el abad, la ermita, el altar...): {x, y, z} (y: lo que se pisa). */
    public int[] sitio(String nombre) {
        return sitios.get(nombre);
    }

    public Map<String, int[]> sitios() {
        return sitios;
    }

    private void sitio(String nombre, int x, int y, int z) {
        sitios.put(nombre, new int[]{x, y, z});
    }

    private static int x(int u) {
        return CX + u;
    }

    private static int z(int v) {
        return CZ + v;
    }

    // ------------------------------------------------------------------ el calvario

    /**
     * El suelo del calvario: el borde de sillares, un anillo de andesita pulida, la rosa de hielo en el centro (donde reza el
     * abad) y lo demas a trozos (sillares, agrietados, nieve prensada, hielo); nieve encima (no en la rosa); el murete caido
     * alrededor; los escalones de la terraza de la ermita.
     */
    private void calvario(Pilotes.Lista m) {
        for (int u = -R_PLAZA - 1; u <= R_PLAZA + 1; u++)
            for (int v = -R_PLAZA - 1; v <= R_PLAZA + 1; v++) {
                double r = Math.hypot(u, v);
                if (r > R_PLAZA + 0.5 || plataforma(u, v)) continue;
                int x = x(u), z = z(v);
                int p = Hash.pct(x, SUELO, z, SEMILLA + 3);
                BlockState s;
                boolean rosa = r <= 2.6;
                if (r >= R_PLAZA - 1.2) s = p < 80 ? Pal.STONE_BRICKS : b(Blocks.CRACKED_STONE_BRICKS);
                else if (Math.abs(r - 7.0) < 0.55) s = b(Blocks.POLISHED_ANDESITE);
                else if (rosa) s = r <= 1.2 ? b(Blocks.BLUE_ICE) : ((u + v & 1) == 0 ? b(Blocks.PACKED_ICE) : b(Blocks.BLUE_ICE));
                else if (r <= 3.4) s = Pal.STONE_BRICKS;
                else s = p < 46 ? Pal.STONE_BRICKS : p < 62 ? b(Blocks.CRACKED_STONE_BRICKS) : p < 80 ? b(Blocks.SNOW_BLOCK) : p < 90 ? b(Blocks.PACKED_ICE)
                        : b(Blocks.POLISHED_ANDESITE);
                m.put(x, SUELO, z, s);
                // nieve encima (y en la rosa, donde reza el abad, ni la de la terraza)
                m.put(x, PISO, z, !rosa && p % 7 != 0 ? nieve(r > R_PLAZA - 2.5 && p < 40 ? 2 : 1) : Pal.AIR);
            }
        // el murete caido alrededor (con huecos), menos por la entrada y la terraza de la ermita
        for (int u = -24; u <= 24; u++)
            for (int v = -24; v <= 24; v++) {
                double r = Math.hypot(u, v);
                if (r < 21.2 || r > 22.2) continue;
                if (v > 0 && Math.abs(u) <= 4 || u >= PU0 - 1 && u <= PU1 + 1 && v < 0) continue;
                int x = x(u), z = z(v);
                int p = Hash.pct(x, 1, z, SEMILLA + 5);
                if (p < 34) continue;
                int s = superficie(x, z);
                m.une(x, s + 1, z, p < 60 ? b(Blocks.COBBLESTONE_WALL) : b(Blocks.STONE_BRICK_WALL));
                if (p > 85) m.une(x, s + 2, z, b(Blocks.STONE_BRICK_WALL));
            }
        // los escalones de la terraza de la ermita (uno), anchos delante de la puerta
        for (int u = PU0; u <= PU1; u++) {
            int x = x(u), z = z(PV1);
            if (Math.abs(u) <= 3) m.put(x, E_SUELO, z, escalera(Blocks.STONE_BRICK_STAIRS, Direction.SOUTH, false));
            else m.put(x, E_SUELO, z, Pal.STONE_BRICKS);
        }
    }

    /** Ocho pilares de 2 x 2 alrededor (rotos a distintas alturas, con carambanos y nieve) y los dos de la entrada. */
    private void pilares(Pilotes.Lista m) {
        int[] altos = {9, 6, 11, 4, 10, 7, 3, 8};
        for (int k = 0; k < 8; k++) {
            double a = Math.toRadians(22.5 + 45 * k);
            int pu = (int) Math.round(Math.cos(a) * 19.0), pv = (int) Math.round(Math.sin(a) * 19.0);
            pilar(m, pu, pv, altos[k], k);
        }
        // la entrada: dos pilares altos con su dintel roto
        pilar(m, -4, R_PLAZA + 1, 10, 20);
        pilar(m, 3, R_PLAZA + 1, 10, 21);
        for (int u = -2; u <= 2; u++) {
            if (u == 1) continue;   // el trozo que se cayo
            int y = PISO + 9;
            m.put(x(u), y, z(R_PLAZA + 1), Pal.STONE_BRICKS);
            m.put(x(u), y, z(R_PLAZA + 2), Pal.STONE_BRICKS);
            m.put(x(u), y + 1, z(R_PLAZA + 1), nieve(2));
            m.put(x(u), y + 1, z(R_PLAZA + 2), nieve(1));
        }
    }

    private void pilar(Pilotes.Lista m, int pu, int pv, int alto, int k) {
        pilares++;
        int y0 = Integer.MAX_VALUE;
        for (int du = 0; du <= 1; du++)
            for (int dv = 0; dv <= 1; dv++) y0 = Math.min(y0, superficie(x(pu + du), z(pv + dv)));
        for (int du = 0; du <= 1; du++)
            for (int dv = 0; dv <= 1; dv++) {
                int x = x(pu + du), z = z(pv + dv);
                for (int y = y0 - 2; y <= PISO + alto; y++) {
                    int p = Hash.pct(x, y, z, SEMILLA + 7 + k);
                    BlockState s = y <= PISO ? Pal.STONE_BRICKS : p < 62 ? Pal.STONE_BRICKS : p < 84 ? b(Blocks.CRACKED_STONE_BRICKS)
                            : p < 94 ? b(Blocks.POLISHED_ANDESITE) : b(Blocks.PACKED_ICE);
                    m.put(x, y, z, s);
                }
                boolean roto = alto < 8;
                int yTop = PISO + alto + 1;
                if (!roto) m.put(x, yTop, z, losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
                // roto: un bloque de mas en una esquina, como un munon
                else if (du == 0 && dv == 0) m.put(x, yTop, z, b(Blocks.CRACKED_STONE_BRICKS));
                if (roto && du == 0 && dv == 0) m.put(x, yTop + 1, z, nieve(3));
                else if (!roto || du + dv > 0) m.put(x, roto ? yTop : yTop + 1, z, nieve(2));
            }
        // carambanos colgando de un lado, y lo que se cayo del roto
        int p = Hash.pct(x(pu), 3, z(pv), SEMILLA + 13);
        if (alto >= 6) m.put(x(pu) - 1, PISO + alto - 1, z(pv), b(Blocks.ICE));
        if (alto < 8) {
            int du = p % 2 == 0 ? 2 : -1, dv = p % 3 == 0 ? 1 : 0;
            int x = x(pu + du), z = z(pv + dv);
            int s = superficie(x, z);
            if (Math.hypot(pu + du, pv + dv) > R_PLAZA + 1 || s == SUELO) {
                m.put(x, s + 1, z, b(Blocks.CRACKED_STONE_BRICKS));
                m.put(x + (du > 0 ? 1 : -1), s + 1, z, Pal.STONE_BRICKS);
            }
        }
    }

    /**
     * Los cuatro braseros (en las diagonales, a unos 12 del centro): un zocalo de sillares de 3 x 3 con sus escalones y la
     * hoguera encendida encima (su fuego, en {@link #braseros}).
     */
    private void braseros(Pilotes.Lista m) {
        for (int[] q : new int[][]{{-9, -9}, {9, -9}, {-9, 9}, {9, 9}}) {
            int bu = q[0], bv = q[1];
            for (int du = -1; du <= 1; du++)
                for (int dv = -1; dv <= 1; dv++) {
                    int x = x(bu + du), z = z(bv + dv);
                    if (du == 0 && dv == 0) m.put(x, PISO, z, b(Blocks.CHISELED_STONE_BRICKS));
                    else if (du == 0 || dv == 0) {
                        Direction f = du == -1 ? Direction.EAST : du == 1 ? Direction.WEST : dv == -1 ? Direction.SOUTH : Direction.NORTH;
                        m.put(x, PISO, z, escalera(Blocks.STONE_BRICK_STAIRS, f, false));
                    } else m.put(x, PISO, z, losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
                }
            m.put(x(bu), PISO + 1, z(bv), Pal.CAMPFIRE.setValue(CampfireBlock.LIT, true));
            braseros.add(new BlockPos(x(bu), PISO + 1, z(bv)));
        }
    }

    // ------------------------------------------------------------------ la ermita

    /**
     * La ermita: en su terraza (un escalon por encima del calvario), muros de piedra de 7, tejado a dos aguas de pizarra con
     * nieve (la cumbrera de norte a sur), la puerta con su arco al sur (al calvario), ventanucos con barrotes, la espadana con
     * la campana sobre el hastial de delante. Dentro: el suelo de andesita con escarcha, el altar con velas, la cruz de hielo
     * en el muro del fondo, dos reclinatorios y un farol colgando de la cumbrera; nieve que ha entrado por la puerta.
     */
    private void ermita(Pilotes.Lista m) {
        int y0 = E_SUELO, alto = 7;
        for (int u = EU0; u <= EU1; u++)
            for (int v = EV0; v <= EV1; v++) {
                int x = x(u), z = z(v);
                boolean muro = u == EU0 || u == EU1 || v == EV0 || v == EV1;
                // el suelo (por dentro) y lo que tenga debajo
                m.put(x, y0, z, muro ? Pal.STONE_BRICKS : Hash.pct(x, y0, z, SEMILLA + 21) < 14 ? b(Blocks.PACKED_ICE) : b(Blocks.POLISHED_ANDESITE));
                if (!muro) {
                    for (int y = y0 + 1; y <= y0 + alto + 6; y++) m.put(x, y, z, Pal.AIR);
                    continue;
                }
                for (int y = y0 + 1; y <= y0 + alto; y++) m.put(x, y, z, piedra(x, y, z));
            }
        // la puerta (tres de ancho y cuatro de alto, con el arco) y la nieve que ha entrado
        for (int u = -1; u <= 1; u++)
            for (int y = y0 + 1; y <= y0 + 4; y++) m.put(x(u), y, z(EV1), Pal.AIR);
        m.put(x(-1), y0 + 4, z(EV1), escalera(Blocks.STONE_BRICK_STAIRS, Direction.EAST, true));
        m.put(x(1), y0 + 4, z(EV1), escalera(Blocks.STONE_BRICK_STAIRS, Direction.WEST, true));
        m.put(x(0), y0 + 5, z(EV1), b(Blocks.CHISELED_STONE_BRICKS));
        for (int u = -2; u <= 2; u++) m.put(x(u), y0 + 1, z(EV1 - 1), nieve(Math.abs(u) == 2 ? 1 : 2));
        m.put(x(0), y0 + 1, z(EV1 - 2), nieve(1));
        // los ventanucos de los lados, con barrotes
        for (int v : new int[]{EV1 - 4, EV0 + 4})
            for (int u : new int[]{EU0, EU1}) {
                m.une(x(u), y0 + 3, z(v), Pal.IRON_BARS);
                m.une(x(u), y0 + 4, z(v), Pal.IRON_BARS);
            }
        // el roseton del hastial de delante (un circulo de vidrio azul) y el del fondo
        m.une(x(0), y0 + alto + 2, z(EV1), Pal.GLASS_BLUE);
        m.une(x(0), y0 + alto + 2, z(EV0), Pal.GLASS_BLUE);
        // el tejado a dos aguas (cumbrera de norte a sur), con aleros de un bloque y los hastiales de piedra
        int u0 = EU0 - 1, u1 = EU1 + 1, cumbre = 0;
        for (int u = u0; u <= u1; u++) {
            int a = u - u0, bb = u1 - u;
            int h = y0 + alto + Math.min(a, bb);
            cumbre = Math.max(cumbre, h);
            for (int v = EV0 - 1; v <= EV1 + 1; v++) {
                BlockState s = a == bb ? losa(Blocks.DEEPSLATE_TILE_SLAB, SlabType.DOUBLE)
                        : escalera(Blocks.DEEPSLATE_TILE_STAIRS, a < bb ? Direction.EAST : Direction.WEST, false);
                m.une(x(u), h, z(v), s);
                if (Hash.pct(x(u), h, z(v), SEMILLA + 23) < 80) m.put(x(u), h + 1, z(v), nieve(1 + Hash.pct(x(u), h, z(v), SEMILLA + 24) % 2));
            }
            for (int y = y0 + alto + 1; y < h; y++) {
                if (y == y0 + alto + 2 && u == 0) continue;   // el roseton
                m.put(x(u), y, z(EV1), piedra(x(u), y, z(EV1)));
                m.put(x(u), y, z(EV0), piedra(x(u), y, z(EV0)));
            }
        }
        // la espadana: un muro de tres sobre el hastial de delante con su hueco y la campana; encima, una cruz de hielo
        int ye = cumbre + 1;
        for (int u = -1; u <= 1; u++)
            for (int y = ye; y <= ye + 3; y++) {
                if (u == 0 && (y == ye + 1 || y == ye + 2)) continue;
                m.put(x(u), y, z(EV1), Pal.STONE_BRICKS);
            }
        m.put(x(0), ye + 3, z(EV1), Pal.STONE_BRICKS);
        m.put(x(-1), ye + 4, z(EV1), losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
        m.put(x(1), ye + 4, z(EV1), losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
        m.put(x(0), ye + 4, z(EV1), b(Blocks.PACKED_ICE));
        m.put(x(0), ye + 5, z(EV1), b(Blocks.PACKED_ICE));
        m.put(x(0), ye + 6, z(EV1), b(Blocks.PACKED_ICE));
        m.une(x(-1), ye + 5, z(EV1), b(Blocks.ICE));
        m.une(x(1), ye + 5, z(EV1), b(Blocks.ICE));
        // dentro: el altar (tres de sillar cincelado con velas), la cruz de hielo en el muro del fondo, los reclinatorios y el farol
        int va = EV0 + 2;
        for (int u = -1; u <= 1; u++) {
            m.put(x(u), y0 + 1, z(va), u == 0 ? b(Blocks.CHISELED_STONE_BRICKS) : b(Blocks.POLISHED_ANDESITE));
            m.put(x(u), y0 + 2, z(va), vela(u == 0 ? 3 : 2, true));
        }
        for (int y = y0 + 2; y <= y0 + 6; y++) m.put(x(0), y, z(EV0 + 1), b(Blocks.PACKED_ICE));
        m.put(x(-1), y0 + 5, z(EV0 + 1), b(Blocks.PACKED_ICE));
        m.put(x(1), y0 + 5, z(EV0 + 1), b(Blocks.PACKED_ICE));
        for (int u : new int[]{-2, 2}) {
            m.put(x(u), y0 + 1, z(va + 4), escalera(Blocks.SPRUCE_STAIRS, Direction.SOUTH, false));
            m.put(x(u), y0 + 1, z(va + 6), escalera(Blocks.SPRUCE_STAIRS, Direction.SOUTH, false));
        }
        int yc = cumbre - 1;
        m.put(x(0), yc, z((EV0 + EV1) / 2), Pal.CHAIN);
        m.put(x(0), yc - 1, z((EV0 + EV1) / 2), Pal.LANTERN_HANG);
        // escarcha en los rincones de dentro
        for (int[] q : new int[][]{{EU0 + 1, EV0 + 1}, {EU1 - 1, EV0 + 1}, {EU0 + 1, EV1 - 1}, {EU1 - 1, EV1 - 1}}) m.put(x(q[0]), y0 + 1, z(q[1]), nieve(3));
        // la terraza de la ermita: el borde de sillares por delante y a los lados, con dos faroles junto a la puerta
        for (int u : new int[]{-3, 3}) {
            m.une(x(u), y0 + 1, z(EV1 + 1), b(Blocks.STONE_BRICK_WALL));
            m.put(x(u), y0 + 2, z(EV1 + 1), Pal.LANTERN);
        }
    }

    /** La gran cruz de hielo en la ladera, detras de la ermita: su peana de sillares, el mastil y los brazos, con carambanos. */
    private void cruz(Pilotes.Lista m) {
        int x0 = x(0), zc = z(CRUZ_V);
        int base = Integer.MAX_VALUE;
        for (int du = -1; du <= 1; du++)
            for (int dv = -1; dv <= 1; dv++) base = Math.min(base, superficie(x0 + du, zc + dv));
        for (int du = -1; du <= 1; du++)
            for (int dv = -1; dv <= 1; dv++)
                for (int y = base - 2; y <= base + 2; y++) {
                    int top = superficie(x0 + du, zc + dv);
                    if (y <= base + 1 || du == 0 && dv == 0) m.put(x0 + du, y, zc + dv, y == base + 2 ? b(Blocks.CHISELED_STONE_BRICKS) : Pal.STONE_BRICKS);
                    else if (y > top) m.put(x0 + du, y, zc + dv, losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
                }
        int y1 = base + 3, y2 = base + 18;
        for (int y = y1; y <= y2; y++) m.put(x0, y, zc, (y & 1) == 0 ? b(Blocks.PACKED_ICE) : b(Blocks.BLUE_ICE));
        int yb = base + 14;
        for (int du = -5; du <= 5; du++) {
            if (du == 0) continue;
            m.put(x0 + du, yb, zc, Math.abs(du) == 5 ? b(Blocks.BLUE_ICE) : b(Blocks.PACKED_ICE));
            if (Math.abs(du) % 2 == 1) m.put(x0 + du, yb - 1, zc, b(Blocks.ICE));
        }
        m.put(x0, y2 + 1, zc, nieve(2));
        for (int du = -5; du <= 5; du++) if (du != 0) m.put(x0 + du, yb + 1, zc, nieve(1));
        sitio("cruz_alta", x0, y2, zc);
    }

    // ------------------------------------------------------------------ lo que lleva datos: el arcon y la campana

    private void trozos() {
        int ya = E_SUELO + 1, va = EV0 + 2;
        trozo(x(3), ya, z(va), c -> c.chest(x(3), ya, z(va), Direction.SOUTH, BOTIN));
        trozo(x(-3), ya, z(va), c -> c.barrel(x(-3), ya, z(va), Direction.UP, Monasterio.BOTIN));
        // la campana de la espadana, colgada del techo de su hueco (ye + 1; lo de encima, ye + 3, es piedra)
        int ye = E_SUELO + 7 + (EU1 + 1 - (EU0 - 1)) / 2 + 1;
        trozo(x(0), ye + 1, z(EV1), c -> c.set(x(0), ye + 2, z(EV1), b(Blocks.BELL).setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING)
                .setValue(BellBlock.FACING, Direction.SOUTH)));
    }

    private void trozo(int x, int y, int z, Trozo.Obra o) {
        elementos.add(new Trozo(6, x, y, z, x, y + 1, z, o));
    }

    // ------------------------------------------------------------------ los helados de la escalera

    /**
     * Penitentes y monjes de rodillas por la escalera, rezando hacia la cumbre: cada quince o veinte escalones, en lo llano si lo
     * hay (en medio) y en el tramo empinado en un escalon cualquiera (a un lado, que deje pasar); y dos peregrinos enterrados
     * al llegar al calvario.
     */
    private void ponerEsperas() {
        int[] variantes = {Helado.PENITENTE, Helado.MONJE, Helado.PENITENTE, Helado.MONJE, Helado.PEREGRINO, Helado.PENITENTE};
        int n = 0, ultimo = ESC_Z0 + 6;
        for (int z = ESC_Z0 - 9; z > ESC_Z1 + 10 && n < variantes.length; z--) {
            int h = escalon(z);
            boolean llano = escalon(z + 1) == h;
            if (ultimo - z < (llano ? 15 : 19)) continue;
            int x = llano ? ESC_X : ESC_X + ((n & 1) == 0 ? -1 : 1);
            esperas.add(new Espera(variantes[n], x, h, z, 180f, Enemigo.REZA, 0, "escalera"));
            n++;
            ultimo = z;
        }
        // dos peregrinos enterrados en la nieve a los lados de la entrada del calvario (de cara a la escalera)
        for (int u : new int[]{-7, 7}) {
            int v = R_PLAZA + 4;
            int x = x(u), z = z(v);
            esperas.add(new Espera(Helado.PEREGRINO, x, superficie(x, z) + 1, z, 0f, Enemigo.TUMBADO, 0, "calvario"));
        }
    }

    // ------------------------------------------------------------------ el suelo: la terraza del calvario y de la ermita

    /**
     * La terraza: corta la ladera del circo hasta el suelo del calvario (y la de la ermita un escalon mas arriba) y rellena
     * por debajo; alrededor se funde con la ladera ({@link #FUNDE}). Nieve prensada y piedra, con nieve encima.
     */
    private final class Terraza extends Element {
        Terraza() {
            super(SEMILLA + 51);
            this.layer = 1;
            bounds(X0, 300, Z0, X1, 470, CZ + R_PLAZA + 16);
        }

        @Override
        public void build(Canvas c) {
            for (int x = Math.max(c.minX, x0); x <= Math.min(c.maxX, x1); x++)
                for (int z = Math.max(c.minZ, z0); z <= Math.min(c.maxZ, z1); z++) {
                    int o = objetivo(x, z);
                    if (o == Integer.MIN_VALUE) continue;
                    int t = alto(x, z);
                    for (int y = o + 1; y <= Math.max(t, o) + 4; y++) c.set(x, y, z, Pal.AIR);
                    for (int y = t + 1; y < o; y++) c.set(x, y, z, Pal.STONE);
                    int p = Hash.pct(x, o, z, seed);
                    c.set(x, o, z, p < 72 ? b(Blocks.SNOW_BLOCK) : p < 88 ? Pal.STONE : b(Blocks.PACKED_ICE));
                    if (p % 5 != 0) c.set(x, o + 1, z, nieve(1 + p % 2));
                }
        }
    }

    // ------------------------------------------------------------------ la escalera de los penitentes

    /**
     * La escalera: tres de ancho, de sillares, del postigo del monasterio al calvario ({@link #escalon}). Donde va por debajo
     * de la ladera, entre dos muros hasta arriba (con hornacinas con faroles y, cada tanto, una estacion: una cruz de hielo
     * metida en el muro); donde va por encima, sobre su relleno con pretil; donde va a ras, con su bordillo y faroles en postes.
     */
    private final class Escalera extends Element {
        Escalera() {
            super(SEMILLA + 61);
            this.layer = 2;
            bounds(ESC_X - 4, 280, ESC_Z1, ESC_X + 4, 470, ESC_Z0);
        }

        @Override
        public void build(Canvas c) {
            for (int z = Math.max(c.minZ, z0); z <= Math.min(c.maxZ, z1); z++) {
                int h = escalon(z), hs = escalon(z + 1);
                boolean sube = h > hs;
                int k = ESC_Z0 - z;
                // los tres de ancho
                for (int dx = -1; dx <= 1; dx++) {
                    int x = ESC_X + dx;
                    if (!c.inXZ(x, z)) continue;
                    int s = superficie(x, z);
                    for (int y = s + 1; y <= h - 2; y++) c.set(x, y, z, Pal.STONE_BRICKS);
                    for (int y = h; y <= Math.max(s, h + 3) + 2; y++) c.set(x, y, z, Pal.AIR);
                    int p = Hash.pct(x, h, z, seed);
                    if (sube) c.set(x, h - 1, z, escalera(Blocks.STONE_BRICK_STAIRS, Direction.NORTH, false));
                    else {
                        c.set(x, h - 1, z, p < 70 ? Pal.STONE_BRICKS : p < 90 ? b(Blocks.CRACKED_STONE_BRICKS) : b(Blocks.POLISHED_ANDESITE));
                        if (p < 22) c.set(x, h, z, nieve(1));
                    }
                }
                // los lados
                for (int lado = -1; lado <= 1; lado += 2) {
                    int x = ESC_X + 2 * lado;
                    if (!c.inXZ(x, z)) continue;
                    int s = superficie(x, z);
                    if (s >= h + 1) {
                        // cortada en la roca: el muro hasta arriba (con su hornacina con farol cada doce y alguna estacion)
                        for (int y = h - 1; y <= s; y++) c.set(x, y, z, muro(x, y, z));
                        c.set(x, s + 1, z, nieve(1));
                        if (k % 12 == 6 && s >= h + 2 && lado == ((k / 12 & 1) == 0 ? 1 : -1)) {
                            c.set(x, h + 1, z, Pal.AIR);
                            c.set(x, h + 1, z, Pal.LANTERN);
                        }
                        if (k % 24 == 18 && s >= h + 4) {
                            c.set(x, h + 1, z, b(Blocks.PACKED_ICE));
                            c.set(x, h + 2, z, b(Blocks.PACKED_ICE));
                            c.set(x, h + 3, z, b(Blocks.PACKED_ICE));
                            if (c.inXZ(x, z - 1)) c.set(x, h + 2, z - 1, b(Blocks.BLUE_ICE));
                            if (c.inXZ(x, z + 1)) c.set(x, h + 2, z + 1, b(Blocks.BLUE_ICE));
                        }
                    } else if (s <= h - 2) {
                        // por encima de la ladera: el relleno y el pretil
                        for (int y = s + 1; y <= h - 1; y++) c.set(x, y, z, Pal.STONE_BRICKS);
                        c.unir(x, h, z, b(Blocks.STONE_BRICK_WALL));
                        int xo = ESC_X + 3 * lado;
                        if (k % 6 == 0 && c.inXZ(xo, z)) {
                            int so = superficie(xo, z);
                            for (int y = so + 1; y <= h - 2; y++) c.set(xo, y, z, Pal.STONE_BRICKS);
                            c.set(xo, h - 1, z, escalera(Blocks.STONE_BRICK_STAIRS, lado > 0 ? Direction.WEST : Direction.EAST, false));
                        }
                    } else {
                        // a ras: el bordillo
                        c.set(x, h - 1, z, Pal.STONE_BRICKS);
                        if (s >= h) c.set(x, h, z, Pal.STONE_BRICKS);
                        for (int y = Math.max(h, s + 1); y <= h + 3; y++) if (y > h || s < h) c.set(x, y, z, Pal.AIR);
                        if (s >= h) c.set(x, h, z, Pal.STONE_BRICKS);
                    }
                    // un farol en su poste cada doce, a un lado y a otro (donde no va entre muros)
                    if (k % 12 == 6 && s < h + 1 && lado == ((k / 12 & 1) == 0 ? 1 : -1)) {
                        c.unir(x, h, z, b(Blocks.STONE_BRICK_WALL));
                        c.unir(x, h + 1, z, b(Blocks.STONE_BRICK_WALL));
                        c.set(x, h + 2, z, Pal.LANTERN);
                    }
                }
            }
        }

        private BlockState muro(int x, int y, int z) {
            int p = Hash.pct(x, y, z, SEMILLA + 63);
            return p < 55 ? Pal.STONE_BRICKS : p < 75 ? b(Blocks.CRACKED_STONE_BRICKS) : p < 88 ? b(Blocks.COBBLESTONE) : p < 95 ? Pal.STONE : b(Blocks.PACKED_ICE);
        }
    }

    /** Para la prueba y las vistas: cuantos escalones van entre muros (cortados en la roca). */
    public int cortados() {
        int n = 0;
        for (int z = ESC_Z1; z <= ESC_Z0; z++)
            if (Math.min(superficie(ESC_X - 2, z), superficie(ESC_X + 2, z)) >= escalon(z) + 1) n++;
        return n;
    }

    // ------------------------------------------------------------------ al caer el abad

    /** Si el abad ha caido: la cumbre esta en paz (los hermanos de la iglesia ya no vuelven; la campana ya no suena sola). */
    public static boolean enPaz(net.minecraft.server.MinecraftServer srv) {
        return com.darkcraft.enemigos.Puestos.muerto(srv, MorvainEnemigos.CUMBRE, MorvainEnemigos.abad());
    }

    /**
     * Ha caido el abad: los hermanos que seguian de rodillas en la iglesia del monasterio descansan (se deshacen en escarcha;
     * los que ya andaban sueltos siguen peleando) y la campana del monasterio da su ultimo toque para todos los de alrededor.
     */
    public static void liberar(ServerLevel m) {
        int n = 0;
        if (com.darkcraft.enemigos.Puestos.sitio(MorvainEnemigos.MONASTERIO) != null)
            for (int i = 0; i < MorvainEnemigos.monasterio().size(); i++)
                if (com.darkcraft.enemigos.Puestos.enemigo(MorvainEnemigos.MONASTERIO, i) instanceof Helado h && h.isAlive()
                        && h.postura() != Enemigo.DE_PIE && h.accion() == 0 && "iglesia".equals(MorvainEnemigos.monasterio().get(i).grupo())) {
                    m.sendParticles(ParticleTypes.SNOWFLAKE, h.getX(), h.getY() + 1.0, h.getZ(), 24, 0.4, 0.6, 0.4, 0.02);
                    m.sendParticles(ParticleTypes.END_ROD, h.getX(), h.getY() + 1.2, h.getZ(), 6, 0.2, 0.5, 0.2, 0.02);
                    h.disolver();
                    n++;
                }
        int[] q = Monasterio.get().sitio("campana");
        int oyen = 0;
        for (ServerPlayer p : m.players())
            if (p.distanceToSqr(q[0] + 0.5, q[1], q[2] + 0.5) < 320 * 320 || p.distanceToSqr(CX, PISO, CZ) < 200 * 200) {
                p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                        net.minecraft.core.Holder.direct(DarkCraftMod.SFX_MONASTERIO_CAMPANA.get()), SoundSource.AMBIENT, p.getX(), p.getY() + 30, p.getZ(),
                        2.0f, 0.82f, m.random.nextLong()));
                oyen++;
            }
        DarkCraftMod.LOGGER.info("[DarkCraft] El Abad de la Cumbre cae: descansan {} hermanos de la iglesia; la campana la oyen {}", n, oyen);
    }

    /** Cada 5 s, con alguien cerca (lo llama {@link Morvain}): en paz, los de la iglesia que se pusieron antes de caer el abad se van. */
    public static void paz(ServerLevel m) {
        if (!enPaz(m.getServer()) || com.darkcraft.enemigos.Puestos.sitio(MorvainEnemigos.MONASTERIO) == null) return;
        for (int i = 0; i < MorvainEnemigos.monasterio().size(); i++)
            if ("iglesia".equals(MorvainEnemigos.monasterio().get(i).grupo())
                    && com.darkcraft.enemigos.Puestos.enemigo(MorvainEnemigos.MONASTERIO, i) instanceof Helado h && h.isAlive() && h.postura() != Enemigo.DE_PIE
                    && h.getTarget() == null && h.accion() == 0)
                h.disolver();
    }

    // ------------------------------------------------------------------ para la prueba

    /** Lo que hay en la ermita ya generada (para la prueba): los braseros encendidos, el arcon, la campana, la rosa, la cruz. */
    public static String resumen(ServerLevel m) {
        Ermita a = get();
        int fuegos = 0;
        for (BlockPos b : a.braseros) {
            BlockState s = m.getBlockState(b);
            if (s.is(Blocks.CAMPFIRE) && s.getValue(CampfireBlock.LIT)) fuegos++;
        }
        int[] ar = a.sitio("arcon");
        boolean arcon = m.getBlockState(new BlockPos(ar[0], ar[1], ar[2])).is(Blocks.CHEST);
        int ye = E_SUELO + 7 + (EU1 + 1 - (EU0 - 1)) / 2 + 1;
        boolean campana = m.getBlockState(new BlockPos(CX, ye + 2, CZ + EV1)).is(Blocks.BELL);
        boolean rosa = m.getBlockState(new BlockPos(CX, SUELO, CZ)).is(Blocks.BLUE_ICE);
        int[] cr = a.sitio("cruz_alta");
        boolean cruz = m.getBlockState(new BlockPos(cr[0], cr[1], cr[2])).is(net.minecraft.tags.BlockTags.ICE);
        int suelo = 0, muestras = 0;
        for (int u = -14; u <= 14; u += 2)
            for (int v = -14; v <= 14; v += 2) {
                if (Math.hypot(u, v) > 14) continue;
                muestras++;
                BlockPos p = new BlockPos(CX + u, PISO, CZ + v);
                BlockState s = m.getBlockState(p);
                if (!m.getBlockState(p.below()).isAir() && (s.isAir() || s.is(Blocks.SNOW))) suelo++;
            }
        return "braseros encendidos " + fuegos + "/" + a.braseros.size() + ", arcon " + (arcon ? "SI" : "NO") + ", campana " + (campana ? "SI" : "NO")
                + ", rosa de hielo " + (rosa ? "SI" : "NO") + ", cruz " + (cruz ? "SI" : "NO") + ", suelo del calvario " + suelo + "/" + muestras
                + ", escalones " + a.escalones + " (" + a.cortados() + " entre muros)";
    }
}
