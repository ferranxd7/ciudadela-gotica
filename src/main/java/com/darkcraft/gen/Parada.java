package com.darkcraft.gen;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Una parada del camino de nubes (parte 5d, respuesta 81 del plan: "largo, a pie, con paradas: islotes con ruinas,
 * enemigos y algun cofre"). Un islote de roca a la altura del camino (lo que se pisa en {@link #Y}), con el camino
 * enlosado de lado a lado (nunca se tapa: lo de cada parada se aparta de el) y, a un lado, lo de su tipo:
 * <ul>
 * <li>{@link #ATALAYA}: una torre de vigia en ruinas con su escalera de mano y la plataforma de arriba (el cofre dentro);</li>
 * <li>{@link #CAPILLA}: la capilla del peregrino, sin techo, con su altar; delante, un santuario (sin enemigos);</li>
 * <li>{@link #CARAVANA}: dos carros (uno volcado), barriles, heno y los huesos del caballo;</li>
 * <li>{@link #ARCO}: un arco de piedra sobre el camino con dos estatuas y lo que queda de la muralla de una puerta vieja;</li>
 * <li>{@link #CEMENTERIO}: tumbas en filas, un arbol seco y la entrada de una cripta pequena (con el cofre);</li>
 * <li>{@link #CAMPAMENTO}: tiendas de la guardia, la hoguera, armas y estandartes; al llegar, el ultimo santuario.</li>
 * </ul>
 * Se construye chunk a chunk (como la fortaleza: {@link #construir}); quien las pone en su sitio, cuando se levantan y
 * sus enemigos esta en {@code camino/Paradas}.
 */
public final class Parada {
    public static final int ATALAYA = 0, CAPILLA = 1, CARAVANA = 2, ARCO = 3, CEMENTERIO = 4, CAMPAMENTO = 5;
    public static final String[] NOMBRES = {"atalaya", "capilla", "caravana", "arco", "cementerio", "campamento"};
    /** Radio de cada islote. */
    public static final int[] RADIOS = {15, 16, 16, 14, 18, 19};
    /** Lo que se pisa del camino (el bloque): NubesGenerador.CAMINO. Los pies van en Y + 1. */
    public static final int Y = 250;
    /** Hasta donde se despeja la nube encima del islote. */
    static final int DESPEJE = 278;

    public final int tipo, cx, cz, r;
    /** A lo largo del camino (hacia la fortaleza) y de lado (hacia donde va lo de la parada: lado * (vx, vz)). */
    public final double ux, uz, vx, vz;
    public final int lado;
    private final double[] px, pz;
    private final long seed;
    private final Island isla;
    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    /** Donde va el cofre {x, y, z} y donde el santuario (o null). */
    private int[] cofre, santuario;

    /**
     * @param px, pz el camino de nubes (sus puntos): lo que queda a menos de 1,6 se enlosa y nada se pone encima.
     */
    public Parada(long seed, int tipo, int cx, int cz, double ux, double uz, int lado, double[] px, double[] pz) {
        this.seed = seed;
        this.tipo = tipo;
        this.cx = cx;
        this.cz = cz;
        double l = Math.hypot(ux, uz);
        this.ux = ux / l;
        this.uz = uz / l;
        this.vx = -this.uz;
        this.vz = this.ux;
        this.lado = lado >= 0 ? 1 : -1;
        this.px = px;
        this.pz = pz;
        this.r = RADIOS[tipo];
        Rng rng = new Rng(seed);
        isla = new Island(rng.fork(), cx, cz, Y, r, 0.12, 3, 2, 18);
        isla.surface(this::suelo).subsurface((x, y, z, d) -> d <= 1 ? (Hash.pct(x, y, z, 31) < 50 ? Pal.TUFF : Pal.DS_COBBLED) : null)
                .plants((x, z) -> false);
        elementos.add(isla);
        elementos.add(new Obra(rng.fork()));
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) indice.computeIfAbsent(FortalezaPlan.clave(x, z), k -> new ArrayList<>()).add(e);
        // lo que se calcula sin construir: el cofre y el santuario
        new Obra(0).sitios();
    }

    public Set<Long> chunks() {
        return indice.keySet();
    }

    public void construir(WorldGenLevel level, int chunkX, int chunkZ, RandomSource random) {
        List<Element> l = indice.get(FortalezaPlan.clave(chunkX, chunkZ));
        if (l == null) return;
        Canvas c = new Canvas(level, chunkX, chunkZ, random);
        for (Element e : l) {
            try {
                e.build(c);
            } catch (Throwable t) {
                DarkCraftMod.LOGGER.error("Error construyendo la parada {} en {},{}", NOMBRES[tipo], chunkX, chunkZ, t);
            }
        }
    }

    /** El cofre {x, y, z} (o null). */
    public int[] cofre() {
        return cofre;
    }

    /** Donde va el santuario {x, y de los pies, z} (o null: solo la capilla y el campamento). */
    public int[] santuario() {
        return santuario;
    }

    /** El punto (bloque) a {@code u} a lo largo del camino y {@code v} de lado (v positivo = hacia lo de la parada). */
    public int[] en(double u, double v) {
        return new int[]{(int) Math.floor(cx + 0.5 + u * ux + v * lado * vx), (int) Math.floor(cz + 0.5 + u * uz + v * lado * vz)};
    }

    /** Lo cerca que pasa el camino de (x, z) (centro del bloque). */
    public double alCamino(int x, int z) {
        double mejor = Double.MAX_VALUE, qx = x + 0.5, qz = z + 0.5;
        for (int i = 0; i + 1 < px.length; i++) {
            double ax = px[i], az = pz[i], bx = px[i + 1], bz = pz[i + 1];
            double dx = bx - ax, dz = bz - az, l2 = dx * dx + dz * dz;
            double t = l2 <= 0 ? 0 : Math.max(0, Math.min(1, ((qx - ax) * dx + (qz - az) * dz) / l2));
            mejor = Math.min(mejor, Math.hypot(qx - ax - dx * t, qz - az - dz * t));
        }
        return mejor;
    }

    /**
     * El punto del camino de nubes a {@code d} bloques (con signo: hacia atras o hacia delante) de donde el camino pasa mas
     * cerca del centro del islote, siguiendo sus tramos (en una curva, la recta por el centro se sale del camino).
     */
    public int[] porCamino(double d) {
        double mejor = Double.MAX_VALUE, s0 = 0, acum = 0, qx = cx + 0.5, qz = cz + 0.5;
        for (int i = 0; i + 1 < px.length; i++) {
            double ax = px[i], az = pz[i], dx = px[i + 1] - ax, dz = pz[i + 1] - az, l2 = dx * dx + dz * dz, l = Math.sqrt(l2);
            double t = l2 <= 0 ? 0 : Math.max(0, Math.min(1, ((qx - ax) * dx + (qz - az) * dz) / l2));
            double dd = Math.hypot(qx - ax - dx * t, qz - az - dz * t);
            if (dd < mejor) {
                mejor = dd;
                s0 = acum + t * l;
            }
            acum += l;
        }
        double s = Math.max(0, Math.min(acum, s0 + d));
        acum = 0;
        for (int i = 0; i + 1 < px.length; i++) {
            double ax = px[i], az = pz[i], dx = px[i + 1] - ax, dz = pz[i + 1] - az, l = Math.hypot(dx, dz);
            if (acum + l >= s || i + 2 == px.length) {
                double t = l <= 0 ? 0 : Math.max(0, Math.min(1, (s - acum) / l));
                return new int[]{(int) Math.floor(ax + dx * t), (int) Math.floor(az + dz * t)};
            }
            acum += l;
        }
        return new int[]{cx, cz};
    }

    /** Si (x, z) es del islote (de su roca). */
    public boolean enIsla(int x, int z) {
        return isla.topAt(x, z) != Integer.MIN_VALUE;
    }

    private BlockState suelo(int x, int z, int top) {
        double m = isla.mancha(x, z);
        int p = Hash.pct(x, top, z, 0x7A1);
        if (m > 0.3) return p < 45 ? Pal.MOSS : p < 75 ? Pal.COARSE_DIRT : Pal.PODZOL;
        if (m < -0.35) return p < 50 ? Pal.b(Blocks.ANDESITE) : p < 80 ? Pal.TUFF : Pal.GRAVEL;
        if (p < 30) return Pal.COARSE_DIRT;
        if (p < 52) return Pal.TUFF;
        if (p < 68) return Pal.MOSS;
        if (p < 82) return Pal.GRAVEL;
        return Pal.b(Blocks.ANDESITE);
    }

    // ------------------------------------------------------------------ lo de encima del islote

    /** Lo de cada parada, en un marco girado a la direccion cardinal mas cercana (los edificios, rectos). */
    private final class Obra extends Element {
        Obra(long s) {
            super(s);
            layer = 3;
            int R = r + 12;
            bounds(cx - R, Y - 30, cz - R, cx + R, DESPEJE, cz + R);
        }

        /** Un marco: origen y hacia donde mira el frente (hacia el camino); a = a la derecha, b = hacia el frente. */
        private final class Marco {
            final int ox, oz;
            final Direction frente, derecha;

            Marco(int[] o, Direction frente) {
                this.ox = o[0];
                this.oz = o[1];
                this.frente = frente;
                this.derecha = frente.getClockWise();
            }

            int x(int a, int b) {
                return ox + a * derecha.getStepX() + b * frente.getStepX();
            }

            int z(int a, int b) {
                return oz + a * derecha.getStepZ() + b * frente.getStepZ();
            }

            /** k: 0 el frente, 1 la derecha, 2 atras, 3 la izquierda. */
            Direction dir(int k) {
                Direction d = frente;
                for (int i = 0; i < k; i++) d = d.getClockWise();
                return d;
            }

            void set(Canvas c, int a, int y, int b, BlockState s) {
                int x = x(a, b), z = z(a, b);
                if (!c.inXZ(x, z) || (y <= Y + 4 && alCamino(x, z) < 2.4)) return;
                c.set(x, y, z, s);
            }

            /** Cimientos: del suelo de la obra hacia abajo hasta la roca (o 20 bloques). */
            void cimiento(Canvas c, int a, int b) {
                int x = x(a, b), z = z(a, b);
                if (!c.inXZ(x, z) || alCamino(x, z) < 2.4) return;
                for (int y = Y; y > Y - 20; y--) {
                    BlockState s = c.get(x, y, z);
                    if (y < Y && !s.isAir() && !nube(s)) break;
                    c.set(x, y, z, y == Y ? Pal.paving(x, y, z) : Pal.foundation(x, y, z));
                }
            }
        }

        /** El marco de lo principal: a {@code v} del camino (hacia su lado) y {@code u} a lo largo, mirando al camino. */
        private Marco marco(double u, double v) {
            double fx = -lado * vx, fz = -lado * vz;
            Direction fr = Math.abs(fx) > Math.abs(fz) ? (fx > 0 ? Direction.EAST : Direction.WEST) : (fz > 0 ? Direction.SOUTH : Direction.NORTH);
            return new Marco(en(u, v), fr);
        }

        /** Calcula (sin construir) donde van el cofre y el santuario. */
        void sitios() {
            switch (tipo) {
                case ATALAYA -> {
                    Marco m = marco(2, 9);
                    cofre = new int[]{m.x(-2, -1), Y + 1, m.z(-2, -1)};
                }
                case CAPILLA -> {
                    Marco m = marco(0, 5);
                    cofre = new int[]{m.x(2, -9), Y + 1, m.z(2, -9)};
                    santuario = new int[]{m.x(3, 2), Y + 1, m.z(3, 2)};
                }
                case CARAVANA -> {
                    Marco m = marco(1, 7);
                    cofre = new int[]{m.x(2, 0), Y + 1, m.z(2, 0)};
                }
                case CEMENTERIO -> {
                    Marco m = marco(0, 6);
                    cofre = new int[]{m.x(0, -7), Y - 3, m.z(0, -7)};
                }
                case CAMPAMENTO -> {
                    Marco m = marco(4, 8);
                    cofre = new int[]{m.x(-3, -6), Y + 1, m.z(-3, -6)};
                    int[] sa = en(-(r - 4), 3.2);
                    santuario = new int[]{sa[0], Y + 1, sa[1]};
                }
                default -> cofre = null;
            }
        }

        @Override
        public void build(Canvas c) {
            despejar(c);
            camino(c);
            switch (tipo) {
                case ATALAYA -> atalaya(c);
                case CAPILLA -> capilla(c);
                case CARAVANA -> caravana(c);
                case ARCO -> arco(c);
                case CEMENTERIO -> cementerio(c);
                default -> campamento(c);
            }
            vegetacion(c);
            if (santuario != null && c.inXZ(santuario[0], santuario[2])) {
                // (el santuario lo pone camino/Paradas con su nombre: aqui, su losa y el sitio libre)
                c.set(santuario[0], Y, santuario[2], Pal.PB_CHISELED);
                for (int y = Y + 1; y <= Y + 3; y++) c.set(santuario[0], y, santuario[2], Pal.AIR);
            }
        }

        /** Fuera la nube de encima del islote y, alrededor, mas baja cuanto mas cerca (el islote queda en un claro). */
        private void despejar(Canvas c) {
            for (int x = Math.max(x0, c.minX); x <= Math.min(x1, c.maxX); x++)
                for (int z = Math.max(z0, c.minZ); z <= Math.min(z1, c.maxZ); z++) {
                    double d = Math.hypot(x - cx, z - cz);
                    if (d <= r + 2) {
                        for (int y = Y + 1; y <= DESPEJE; y++) if (!c.isAir(x, y, z)) c.set(x, y, z, Pal.AIR);
                        continue;
                    }
                    if (d > r + 12) continue;
                    int h = Y + 1 + (int) Math.round((d - r - 2) * 1.4);
                    for (int y = h; y <= DESPEJE; y++) if (nube(c.get(x, y, z))) c.set(x, y, z, Pal.AIR);
                }
        }

        /** El camino enlosado de lado a lado del islote (a la altura del de nube). */
        private void camino(Canvas c) {
            for (int x = Math.max(x0, c.minX); x <= Math.min(x1, c.maxX); x++)
                for (int z = Math.max(z0, c.minZ); z <= Math.min(z1, c.maxZ); z++) {
                    if (alCamino(x, z) > 1.6) continue;
                    int top = isla.topAt(x, z);
                    if (top == Integer.MIN_VALUE) continue;
                    c.set(x, Y, z, Hash.pct(x, 7, z, seed) < 14 ? Pal.PB_BRICKS_CRACKED : Pal.paving(x, Y, z));
                    for (int y = Y - 1; y > top - 1 && y > Y - 6; y--) c.set(x, y, z, Pal.foundation(x, y, z));
                    for (int y = Y + 1; y <= Y + 4; y++) c.set(x, y, z, Pal.AIR);
                }
        }

        /** Algo de vida muerta: matas secas, helechos, musgo, alguna roca (nunca en el camino ni junto a lo construido). */
        private void vegetacion(Canvas c) {
            for (int x = Math.max(cx - r, c.minX); x <= Math.min(cx + r, c.maxX); x++)
                for (int z = Math.max(cz - r, c.minZ); z <= Math.min(cz + r, c.maxZ); z++) {
                    if (alCamino(x, z) < 2.6 || isla.topAt(x, z) != Y) continue;
                    if (!c.isAir(x, Y + 1, z) || !c.isAir(x, Y + 2, z)) continue;
                    BlockState s = c.get(x, Y, z);
                    if (!(s.is(Blocks.MOSS_BLOCK) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.PODZOL))) continue;
                    int p = Hash.pct(x, 3, z, seed ^ 0x5E7);
                    if (p < 7) c.set(x, Y + 1, z, Pal.b(Blocks.DEAD_BUSH));
                    else if (p < 13) c.set(x, Y + 1, z, Pal.b(Blocks.FERN));
                    else if (p < 18 && s.is(Blocks.MOSS_BLOCK)) c.set(x, Y + 1, z, Pal.b(Blocks.MOSS_CARPET));
                    else if (p < 20) c.set(x, Y + 1, z, Pal.slab(Blocks.COBBLED_DEEPSLATE_SLAB, false));
                }
        }

        // ---------------------------------------------------------------- las obras

        /** La atalaya: torre redonda en ruinas (radio 3,5) con su puerta al camino, escalera de mano y plataforma. */
        private void atalaya(Canvas c) {
            Marco m = marco(2, 9);
            int alto = 14;
            int plat = Y + 10;
            for (int a = -5; a <= 5; a++)
                for (int b = -5; b <= 5; b++) {
                    double d = Math.hypot(a, b);
                    int x = m.x(a, b), z = m.z(a, b);
                    if (!c.inXZ(x, z)) continue;
                    if (d > 3.6) {
                        // cascotes alrededor
                        if (d < 5.6 && Hash.pct(x, 1, z, seed) < 22 && c.isAir(x, Y + 1, z) && alCamino(x, z) > 2.6)
                            c.set(x, Y + 1, z, Hash.pct(x, 2, z, seed) < 50 ? Pal.DS_COBBLED : Pal.slab(Blocks.COBBLED_DEEPSLATE_SLAB, false));
                        continue;
                    }
                    m.cimiento(c, a, b);
                    boolean muro = d > 2.5;
                    int arriba = alto - (int) Math.round(Math.abs(Hash.f(a + 9, b + 9, 3, seed) - 0.5) * 8);
                    for (int h = 1; h <= alto + 1; h++) {
                        int y = Y + h;
                        if (!muro) {
                            // dentro: aire, salvo la plataforma de arriba
                            m.set(c, a, y, b, y == plat ? Pal.DO_PLANKS : Pal.AIR);
                            continue;
                        }
                        if (h > arriba) continue;
                        // la puerta al frente (dos de ancho, tres de alto) y las aspilleras
                        if (b >= 2 && Math.abs(a) <= 1 && h <= 3) {
                            m.set(c, a, y, b, Pal.AIR);
                            continue;
                        }
                        if ((h == 6 || h == 12) && (a == 0 || b == 0)) {
                            m.set(c, a, y, b, Pal.AIR);
                            continue;
                        }
                        // las almenas de arriba: un hueco de cada dos
                        if (y > plat + 1 && Math.floorMod(a + b, 2) == 0) {
                            m.set(c, a, y, b, Pal.AIR);
                            continue;
                        }
                        m.set(c, a, y, b, Hash.pct(x, y, z, seed) < 25 ? Pal.DS_BRICKS_CRACKED : Hash.pct(x, y, z, seed ^ 3) < 18 ? Pal.MOSSY_STONE_BRICKS : Pal.DS_BRICKS);
                    }
                }
            // la escalera de mano, pegada al muro de atras, hasta la plataforma (el hueco de la plataforma, encima)
            for (int y = Y + 1; y <= plat; y++) m.set(c, 0, y, -2, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, m.dir(0)));
            for (int y = Y + 1; y <= plat + 1; y++) m.set(c, 0, y, -3, Pal.DS_BRICKS);
            m.set(c, 0, plat + 1, -2, Pal.AIR);
            // dentro: el cofre, barriles y una antorcha de almas; arriba, un farol
            try {
                if (c.inXZ(cofre[0], cofre[2])) c.chest(cofre[0], cofre[1], cofre[2], m.dir(0), new ResourceLocation(DarkCraftMod.MODID, "chests/camino"));
            } catch (Throwable t) {
                m.set(c, -2, Y + 1, -1, Pal.facing(Blocks.CHEST, m.dir(0)));
            }
            m.set(c, 2, Y + 1, -1, Pal.b(Blocks.BARREL));
            m.set(c, 2, Y + 2, -1, Pal.b(Blocks.BARREL));
            m.set(c, -2, plat + 1, 1, Pal.SOUL_LANTERN);
            m.set(c, 1, plat + 1, -1, Pal.b(Blocks.SKELETON_SKULL));
            // un muro roto al otro lado del camino
            Marco o = new Marco(en(-5, -6), m.frente.getOpposite());
            for (int a = -3; a <= 3; a++) {
                int h = 1 + Hash.h(a + 5, 7, 1, seed) % 4;
                o.cimiento(c, a, 0);
                for (int y = Y + 1; y <= Y + h; y++) o.set(c, a, y, 0, Hash.pct(a, y, 4, seed) < 30 ? Pal.MOSSY_STONE_BRICKS : Pal.DS_BRICKS_CRACKED);
            }
        }

        /** La capilla del peregrino: nave de 7 x 11 sin techo, el altar al fondo, bancos rotos; delante, el santuario. */
        private void capilla(Canvas c) {
            Marco m = marco(0, 5);
            for (int a = -3; a <= 3; a++)
                for (int b = -10; b <= 0; b++) {
                    m.cimiento(c, a, b);
                    int x = m.x(a, b), z = m.z(a, b);
                    if (!c.inXZ(x, z)) continue;
                    boolean muro = Math.abs(a) == 3 || b == -10 || b == 0;
                    int alto = muro ? 7 - (int) Math.round(Math.abs(Hash.f(a + 4, b + 11, 9, seed) - 0.5) * 7) : 0;
                    // el frente: la puerta en arco (tres de ancho) y mas alto en el centro (el hastial)
                    if (b == 0) alto = Math.abs(a) <= 1 ? 9 - Math.abs(a) : 7 - Math.abs(a) / 2;
                    if (b == -10) alto = 9 - Math.abs(a);
                    for (int h = 1; h <= 9; h++) {
                        int y = Y + h;
                        if (!muro || h > alto) {
                            m.set(c, a, y, b, Pal.AIR);
                            continue;
                        }
                        if (b == 0 && Math.abs(a) <= 1 && h <= (a == 0 ? 4 : 3)) {
                            m.set(c, a, y, b, Pal.AIR);
                            continue;
                        }
                        // ventanas en los lados y el roseton del fondo
                        if (Math.abs(a) == 3 && (b == -3 || b == -7) && h >= 3 && h <= 5) {
                            m.set(c, a, y, b, Pal.IRON_BARS);
                            continue;
                        }
                        if (b == -10 && Math.abs(a) <= 1 && h >= 5 && h <= 7) {
                            m.set(c, a, y, b, a == 0 && h == 6 ? Pal.GLASS_RED : Pal.GLASS_BLACK);
                            continue;
                        }
                        m.set(c, a, y, b, Hash.pct(x, y, z, seed) < 22 ? Pal.STONE_BRICKS : Hash.pct(x, y, z, seed ^ 5) < 30 ? Pal.MOSSY_STONE_BRICKS : Pal.PB_BRICKS);
                    }
                }
            // el suelo de la nave, enlosado, y el altar al fondo
            for (int a = -2; a <= 2; a++)
                for (int b = -9; b <= -1; b++) m.set(c, a, Y, b, Hash.pct(a, b, 3, seed) < 25 ? Pal.DS_TILES_CRACKED : Pal.DS_TILES);
            for (int a = -1; a <= 1; a++) m.set(c, a, Y + 1, -9, Pal.PB_CHISELED);
            m.set(c, -1, Y + 2, -9, Pal.candles(Blocks.WHITE_CANDLE, 3));
            m.set(c, 1, Y + 2, -9, Pal.candles(Blocks.WHITE_CANDLE, 2));
            m.set(c, 0, Y + 2, -9, Pal.b(Blocks.LECTERN));
            // los bancos, rotos (mirando al altar), y el cofre del peregrino junto al altar
            for (int b = -7; b <= -3; b += 2)
                for (int a = -2; a <= 2; a++) {
                    if (a == 0 || Hash.pct(a, b, 5, seed) < 30) continue;
                    m.set(c, a, Y + 1, b, Pal.stairs(Blocks.SPRUCE_STAIRS, m.dir(0), false));
                }
            try {
                if (c.inXZ(cofre[0], cofre[2])) c.chest(cofre[0], cofre[1], cofre[2], m.dir(3), new ResourceLocation(DarkCraftMod.MODID, "chests/camino"));
            } catch (Throwable t) {
                m.set(c, 2, Y + 1, -9, Pal.facing(Blocks.CHEST, m.dir(3)));
            }
            // dos vigas que quedan del tejado, y faroles en la puerta
            for (int a = -3; a <= 3; a++) {
                m.set(c, a, Y + 7, -4, Pal.log(Blocks.DARK_OAK_LOG, axis(m.derecha)));
                if (Hash.pct(a, 11, 1, seed) < 70) m.set(c, a, Y + 7, -8, Pal.log(Blocks.DARK_OAK_LOG, axis(m.derecha)));
            }
            m.set(c, -2, Y + 1, 1, Pal.SOUL_LANTERN);
            m.set(c, 2, Y + 1, 1, Pal.LANTERN);
        }

        /** La caravana: un carro de pie con su toldo, otro volcado, la carga por el suelo y los huesos del caballo. */
        private void caravana(Canvas c) {
            Marco m = marco(1, 7);
            Direction.Axis largo = axis(m.frente);
            // el carro de pie: caja de tablas a un bloque del suelo, ruedas, toldo de lana roto
            for (int a = -1; a <= 1; a++)
                for (int b = -2; b <= 2; b++) {
                    m.cimiento(c, a, b);
                    m.set(c, a, Y + 2, b, Pal.DO_PLANKS);
                    if (Math.abs(b) == 2 && Math.abs(a) == 1) m.set(c, a, Y + 1, b, Pal.DO_FENCE);
                    if (Math.abs(a) == 1 && Hash.pct(a, b, 2, seed) < 85) m.set(c, a, Y + 3, b, Pal.b(Blocks.WHITE_WOOL));
                    if (Math.abs(a) == 1 && Hash.pct(a, b, 3, seed) < 70) m.set(c, a, Y + 4, b, Pal.b(Blocks.WHITE_WOOL));
                    if (a == 0 && Hash.pct(a, b, 4, seed) < 75) m.set(c, a, Y + 5, b, Pal.b(Blocks.WHITE_WOOL));
                }
            for (int b = -1; b <= 1; b += 2)
                for (int a = -2; a <= 2; a += 4) m.set(c, a, Y + 1, b, Pal.log(Blocks.STRIPPED_DARK_OAK_LOG, axis(m.derecha)));
            m.set(c, 0, Y + 3, 0, Pal.b(Blocks.BARREL));
            // la lanza del carro, por delante
            for (int b = 3; b <= 4; b++) m.set(c, 0, Y + 1, b, Pal.log(Blocks.DARK_OAK_LOG, largo));
            // el carro volcado, al lado: las tablas de canto y una rueda suelta
            for (int b = -2; b <= 2; b++)
                for (int h = 1; h <= 3; h++) if (Hash.pct(b, h, 6, seed) < 85) m.set(c, -5, Y + h, b, Pal.DO_PLANKS);
            for (int b = -2; b <= 2; b += 4) m.set(c, -4, Y + 1, b, Pal.log(Blocks.STRIPPED_DARK_OAK_LOG, axis(m.derecha)));
            m.set(c, -6, Y + 1, 3, Pal.log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Y));
            // la carga por el suelo: barriles, heno, sacos (lana marron)
            int[][] carga = {{-3, -3}, {-3, 1}, {-2, 3}, {3, -2}, {3, 2}, {-7, -1}};
            for (int i = 0; i < carga.length; i++) {
                BlockState s = i % 3 == 0 ? Pal.b(Blocks.BARREL) : i % 3 == 1 ? Pal.b(Blocks.HAY_BLOCK) : Pal.b(Blocks.BROWN_WOOL);
                m.cimiento(c, carga[i][0], carga[i][1]);
                m.set(c, carga[i][0], Y + 1, carga[i][1], s);
            }
            // el cofre (lo que llevaban de valor)
            try {
                if (c.inXZ(cofre[0], cofre[2])) c.chest(cofre[0], cofre[1], cofre[2], m.dir(1), new ResourceLocation(DarkCraftMod.MODID, "chests/camino"));
            } catch (Throwable t) {
                m.set(c, 2, Y + 1, 0, Pal.facing(Blocks.CHEST, m.dir(1)));
            }
            // los huesos del caballo y su craneo (lejos del camino)
            for (int b = -6; b <= -4; b++) m.set(c, 5, Y + 1, b, TorreCupula.hueso(largo));
            m.set(c, 5, Y + 1, -7, Pal.b(Blocks.SKELETON_SKULL));
            m.set(c, 6, Y + 1, -5, TorreCupula.hueso(axis(m.derecha)));
            // un farol en un poste
            m.cimiento(c, 4, -3);
            for (int h = 1; h <= 2; h++) m.set(c, 4, Y + h, -3, Pal.DO_FENCE);
            m.set(c, 4, Y + 3, -3, Pal.LANTERN);
        }

        /** El arco sobre el camino (la puerta vieja), sus estatuas y lo que queda de la muralla a los lados. */
        private void arco(Canvas c) {
            // el arco, recto en la direccion cardinal mas cercana a la de traves del camino
            Direction trav = Math.abs(vx) > Math.abs(vz) ? (vx > 0 ? Direction.EAST : Direction.WEST) : (vz > 0 ? Direction.SOUTH : Direction.NORTH);
            Marco m = new Marco(new int[]{cx, cz}, trav.getCounterClockWise());
            // (a = de traves del camino; b = a lo largo)
            int pie = 5, alto = 9;
            for (int s = -1; s <= 1; s += 2)
                for (int a0 = 0; a0 <= 1; a0++)
                    for (int b = -1; b <= 0; b++) {
                        int a = s * (pie + a0);
                        m.cimiento(c, a, b);
                        for (int h = 1; h <= alto; h++) m.set(c, a, Y + h, b, h == 1 || h == alto ? Pal.PB_CHISELED : Pal.PB_BRICKS);
                    }
            // el arco apuntado de una pata a otra (por encima del camino: nada baja de Y + 6)
            for (int i = -pie; i <= pie; i++) {
                double t = Math.abs(i) / (double) pie;
                int h = alto + (int) Math.round(Math.sqrt(Math.max(0, 1 - t * t)) * 4);
                for (int b = -1; b <= 0; b++) {
                    for (int y = Y + alto; y <= Y + h; y++) c.set(m.x(i, b), y, m.z(i, b), i == 0 && y == Y + h ? Pal.PB_CHISELED : Pal.PB_BRICKS);
                    if (Math.abs(i) >= pie - 1) continue;
                    c.set(m.x(i, b), Y + alto - 1, m.z(i, b), Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, i < 0 ? m.derecha : m.derecha.getOpposite(), true));
                }
            }
            c.set(m.x(0, -2), Y + alto + 3, m.z(0, -2), Pal.b(Blocks.SKELETON_SKULL));
            c.set(m.x(0, 1), Y + alto + 2, m.z(0, 1), Pal.SOUL_LANTERN_HANG);
            // las estatuas: un caballero con su espada clavada a cada lado, mirando al camino
            for (int s = -1; s <= 1; s += 2) {
                int a = s * 9, b = 2;
                m.cimiento(c, a, b);
                m.set(c, a, Y + 1, b, Pal.PB_CHISELED);
                m.set(c, a, Y + 2, b, Pal.b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                m.set(c, a, Y + 3, b, Pal.b(Blocks.POLISHED_BLACKSTONE));
                m.set(c, a, Y + 4, b, Pal.b(Blocks.CHISELED_POLISHED_BLACKSTONE));
                m.set(c, a - s, Y + 2, b, Pal.b(Blocks.IRON_BARS));
                m.set(c, a - s, Y + 3, b, Pal.b(Blocks.POLISHED_BLACKSTONE_BUTTON));
            }
            // la muralla, rota, de las patas hacia fuera
            for (int s = -1; s <= 1; s += 2)
                for (int a = pie + 2; a <= r + 2; a++) {
                    int x = m.x(s * a, 0), z = m.z(s * a, 0);
                    if (!enIsla(x, z)) continue;
                    int h = Math.max(0, 6 - (a - pie) / 2 - Hash.h(a, s + 3, 2, seed) % 3);
                    m.cimiento(c, s * a, 0);
                    m.cimiento(c, s * a, -1);
                    for (int y = Y + 1; y <= Y + h; y++)
                        for (int b = -1; b <= 0; b++)
                            m.set(c, s * a, y, b, Hash.pct(a, y, b + 5, seed) < 30 ? Pal.MOSSY_STONE_BRICKS : Pal.STONE_BRICKS);
                    if (h > 0 && Hash.pct(a, 3, s + 2, seed) < 20) m.set(c, s * a, Y + 1, 2, Pal.slab(Blocks.STONE_BRICK_SLAB, false));
                }
        }

        /** El cementerio: tumbas en filas, la verja, el arbol seco y la cripta pequena con el cofre. */
        private void cementerio(Canvas c) {
            Marco m = marco(0, 6);
            // las tumbas: tres filas de cuatro a los lados del paseo del centro (el monticulo y la lapida a la cabeza)
            int[] as = {-9, -5, 5, 9};
            for (int fila = 0; fila < 3; fila++)
                for (int k = 0; k < as.length; k++) {
                    int a = as[k], b = -1 - fila * 3;
                    int x = m.x(a, b), z = m.z(a, b);
                    if (!enIsla(x, z)) continue;
                    m.cimiento(c, a, b);
                    m.cimiento(c, a, b - 1);
                    int p = Hash.pct(a, b, 7, seed);
                    m.set(c, a, Y, b, Pal.PODZOL);
                    m.set(c, a, Y, b + 1, Pal.COARSE_DIRT);
                    m.set(c, a, Y + 1, b - 1, p < 35 ? Pal.b(Blocks.STONE_BRICK_WALL) : p < 70 ? Pal.b(Blocks.ANDESITE_WALL) : Pal.MOSSY_STONE_BRICKS);
                    if (p > 85) m.set(c, a, Y + 2, b - 1, Pal.candles(Blocks.BLACK_CANDLE, 1 + p % 3));
                    else if (p % 4 == 0) m.set(c, a, Y + 1, b, Pal.b(Blocks.WITHER_ROSE));
                }
            // la verja de barrotes alrededor (con su entrada hacia el camino)
            for (int a = -11; a <= 11; a++)
                for (int b = -11; b <= 1; b++) {
                    boolean borde = Math.abs(a) == 11 || b == -11 || b == 1;
                    if (!borde || (b == 1 && Math.abs(a) <= 1)) continue;
                    int x = m.x(a, b), z = m.z(a, b);
                    if (!enIsla(x, z) || Hash.pct(a, b, 8, seed) < 25) continue;
                    m.cimiento(c, a, b);
                    Direction.Axis eje = Math.abs(a) == 11 ? axis(m.frente) : axis(m.derecha);
                    m.set(c, a, Y + 1, b, Pal.connected(Blocks.IRON_BARS, eje));
                }
            // la cripta: escalones que bajan (b = -1..-4) a una salita (b = -5..-8) con el cofre
            for (int b = -1; b >= -9; b--)
                for (int a = -3; a <= 3; a++) {
                    int x = m.x(a, b), z = m.z(a, b);
                    if (!c.inXZ(x, z)) continue;
                    if (b >= -4) {
                        if (Math.abs(a) > 1 && b > -4) continue;
                        int ys = Y + b + 1;
                        for (int y = Y - 5; y <= Y; y++) {
                            BlockState st;
                            if (Math.abs(a) >= 1) st = Pal.DS_BRICKS;
                            else if (y < ys) st = Pal.DS_BRICKS;
                            else if (y == ys) st = Pal.stairs(Blocks.POLISHED_DEEPSLATE_STAIRS, m.dir(0), false);
                            else st = Pal.AIR;
                            c.set(x, y, z, st);
                        }
                        if (a == 0) for (int y = Y + 1; y <= Y + 3; y++) c.set(x, y, z, Pal.AIR);
                        else if (Math.abs(a) == 1 && b >= -3) c.set(x, Y + 1, z, Pal.b(Blocks.STONE_BRICK_WALL));
                    } else {
                        boolean pared = Math.abs(a) == 3 || b == -9;
                        for (int y = Y - 5; y <= Y; y++)
                            c.set(x, y, z, y <= Y - 4 || y == Y ? Pal.DS_TILES : pared ? Pal.DS_BRICKS : Pal.AIR);
                    }
                }
            try {
                if (c.inXZ(cofre[0], cofre[2])) c.chest(cofre[0], cofre[1], cofre[2], m.dir(0), new ResourceLocation(DarkCraftMod.MODID, "chests/camino"));
            } catch (Throwable t) {
                if (c.inXZ(cofre[0], cofre[2])) c.set(cofre[0], cofre[1], cofre[2], Pal.facing(Blocks.CHEST, m.dir(0)));
            }
            int[][] sala = {{-2, -8}, {2, -8}, {2, -5}};
            BlockState[] cosas = {Pal.SOUL_LANTERN, Pal.b(Blocks.SKELETON_SKULL), Pal.COBWEB};
            for (int i = 0; i < sala.length; i++) {
                int x = m.x(sala[i][0], sala[i][1]), z = m.z(sala[i][0], sala[i][1]);
                if (c.inXZ(x, z)) c.set(x, i == 2 ? Y - 1 : Y - 3, z, cosas[i]);
            }
            // la losa apartada, junto a la entrada
            m.set(c, 2, Y + 1, 0, Pal.slab(Blocks.POLISHED_DEEPSLATE_SLAB, false));
            // el arbol seco, en una esquina
            arbol(c, m.x(-9, -10), m.z(-9, -10), 2);
        }

        /** El campamento de la guardia: tiendas, la hoguera, armas y estandartes; al llegar, el santuario. */
        private void campamento(Canvas c) {
            Marco m = marco(4, 8);
            // tres tiendas (de dos aguas, de lana roja y negra), con la entrada hacia la hoguera
            int[][] tiendas = {{-6, -2}, {0, -5}, {6, -2}};
            for (int t = 0; t < tiendas.length; t++) {
                int ta = tiendas[t][0], tb = tiendas[t][1];
                Block lana = t == 1 ? Blocks.BLACK_WOOL : Blocks.RED_WOOL;
                for (int b = tb - 2; b <= tb + 2; b++)
                    for (int a = -2; a <= 2; a++) {
                        m.cimiento(c, ta + a, b);
                        int h = 3 - Math.abs(a);
                        if (b == tb + 2 && Math.abs(a) <= 1 && h >= 2) continue;
                        if (Math.abs(a) == 2 || b == tb - 2 || b == tb + 2) {
                            for (int y = 1; y <= h; y++) m.set(c, ta + a, Y + y, b, Pal.b(lana));
                        } else m.set(c, ta + a, Y + h, b, Pal.b(lana));
                    }
                m.set(c, ta, Y + 1, tb - 1, Pal.b(Blocks.WHITE_WOOL));
                m.set(c, ta + 1, Y + 1, tb, Pal.b(Blocks.BARREL));
            }
            // la hoguera con troncos alrededor
            m.cimiento(c, 0, 2);
            m.set(c, 0, Y + 1, 2, Pal.CAMPFIRE);
            for (int[] q : new int[][]{{-2, 2}, {2, 2}, {0, 4}})
                m.set(c, q[0], Y + 1, q[1], Pal.log(Blocks.STRIPPED_SPRUCE_LOG, q[1] == 2 ? axis(m.frente) : axis(m.derecha)));
            // las armas: un yunque, piedra de afilar, y estandartes en postes
            m.set(c, -4, Y + 1, 3, Pal.b(Blocks.ANVIL));
            m.set(c, 4, Y + 1, 3, Pal.b(Blocks.GRINDSTONE));
            for (int s = -1; s <= 1; s += 2) {
                m.cimiento(c, s * 9, 0);
                for (int h = 1; h <= 4; h++) m.set(c, s * 9, Y + h, 0, Pal.DO_FENCE);
                m.set(c, s * 9, Y + 4, 1, Pal.b(Blocks.RED_WOOL));
                m.set(c, s * 9, Y + 3, 1, Pal.b(Blocks.RED_WOOL));
                m.set(c, s * 9, Y + 5, 0, Pal.LANTERN);
            }
            // el cofre (la paga de la guardia), entre las tiendas
            m.cimiento(c, -3, -6);
            try {
                if (c.inXZ(cofre[0], cofre[2])) c.chest(cofre[0], cofre[1], cofre[2], m.dir(0), new ResourceLocation(DarkCraftMod.MODID, "chests/camino"));
            } catch (Throwable t) {
                m.set(c, -3, Y + 1, -6, Pal.facing(Blocks.CHEST, m.dir(0)));
            }
            // un poste de guardia al llegar (sin nadie: junto al santuario no hay enemigos)
            int[] p = en(-(r - 7), -4);
            if (c.inXZ(p[0], p[1]) && enIsla(p[0], p[1]) && alCamino(p[0], p[1]) > 2.6) {
                for (int h = 1; h <= 3; h++) c.set(p[0], Y + h, p[1], Pal.DO_FENCE);
                c.set(p[0], Y + 4, p[1], Pal.SOUL_LANTERN);
            }
        }

        /** Un arbol seco (como los del jardin de la cupula). */
        private void arbol(Canvas c, int x0, int z0, int k) {
            if (!enIsla(x0, z0) || alCamino(x0, z0) < 3) return;
            int alto = 6 + Hash.h(k, 1, 1, seed) % 3;
            for (int i = 1; i <= alto; i++) c.set(x0, Y + i, z0, Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
            for (int b = 0; b < 4; b++) {
                double a = b * Math.PI / 2 + k;
                double bx = x0, bz = z0;
                int by = Y + alto - b % 2;
                for (int i = 1; i <= 3; i++) {
                    bx += Math.cos(a);
                    bz += Math.sin(a);
                    if (i % 2 == 0) by++;
                    c.set((int) Math.round(bx), by, (int) Math.round(bz),
                            Pal.log(Blocks.DARK_OAK_LOG, Math.abs(Math.cos(a)) > Math.abs(Math.sin(a)) ? Direction.Axis.X : Direction.Axis.Z));
                }
            }
        }
    }

    private static Direction.Axis axis(Direction d) {
        return d.getAxis();
    }

    /** La nube (por su clase: fuera del juego no hay bloques del mod registrados). */
    static boolean nube(BlockState s) {
        return s.getBlock() instanceof com.darkcraft.world.NubeBlock;
    }
}
