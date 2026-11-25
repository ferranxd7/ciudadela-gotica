package com.darkcraft.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * La montana de la Fortaleza de Hueso (parte 5b, respuestas 36 y 37 del plan): un pico de roca oscura al sur de la isla
 * con un espolon, el "hombro", que baja hacia el sur. Por el hombro sube el sendero en zigzag (tramos a lo ancho, a un
 * bloque de bajada por cada tres, y recodos con medias losas), con restos de torres en los recodos, hasta el mirador de
 * la cima: alli el arco de colmillos mira a la fortaleza y la llave de hueso tiende el puente largo hasta la isla
 * (fortaleza/Puente).
 * <p>
 * Es del mundo de nubes (los mundos con suelo de antes no hace falta que funcionen): el sendero empieza a la altura de
 * los caminos de nube firme ({@link #NUBES}), en una plazoleta donde llega el camino de nubes; el hombro acaba ahi en un
 * cortado y por debajo la roca cuelga como la de las islas, hasta {@link #FONDO}.
 */
public final class Montana extends Element {
    /** El centro de la cima y la altura que se pisa en el mirador. */
    public final int mx, mz, cima;
    /** Radio del mirador (llano) y medio ancho del hombro. */
    public static final int RP = 9, HOMBRO = 22;
    /** Pendientes: la del cono (bajada por bloque) y la del hombro (hacia el sur). */
    static final double CONO = 2.6, PEND = 0.8;
    /** El sendero: un bloque de bajada por cada TRAMO de largo en los tramos (los recodos van a la pendiente del hombro). */
    static final double TRAMO = 3.0;
    /** Medio largo de los tramos (en x) y radio de los recodos. */
    public static final int MEDIO = 16, RECODO = 3;
    /** En el mundo de nubes: el sendero empieza aqui (lo que se pisa), la montana se corta en CORTE y cuelga hasta FONDO. */
    public static final int NUBES = 251, CORTE = 232, FONDO = 190;

    private final Noise ruido;
    private boolean preparada;
    /** El sendero: puntos {x, z, altura que se pisa} cada medio bloque, de la cima al pie. */
    private final List<double[]> sendero = new ArrayList<>();
    /** Columna -> altura que se pisa del sendero. */
    private final Map<Long, Double> pisa = new HashMap<>();
    /** Columnas del pretil (al lado del sendero) -> altura del sendero junto a ellas. */
    private final Map<Long, Double> pretil = new HashMap<>();
    /** Los recodos: {x, z, altura, lado (-1 oeste, 1 este)} de cada giro (de arriba abajo). */
    private final List<double[]> recodos = new ArrayList<>();
    /** Donde acaba el hombro por el sur (z): justo despues de la plazoleta del pie. */
    private double finHombro = Double.MAX_VALUE;
    /** El pie del sendero: {x, z, altura que se pisa}. */
    private double[] pie;
    /** Agujas de roca alrededor de la cima (para que no sea una loma): {x, z, alto, radio}. */
    private final List<double[]> agujas = new ArrayList<>();

    public Montana(long seed, int mx, int mz, int cima) {
        super(seed);
        this.mx = mx;
        this.mz = mz;
        this.cima = cima;
        this.layer = 0;
        this.ruido = new Noise(seed);
        // agujas por los lados del cono, nunca en el hombro (al sur) ni donde sale el puente (al norte)
        Rng r0 = new Rng(seed ^ 0xA6A);
        for (int i = 0; i < 40 && agujas.size() < 8; i++) {
            double a = r0.nextDouble() * Math.PI * 2;
            double ds = Math.abs(Math.IEEEremainder(a - Math.PI / 2, Math.PI * 2)), dn = Math.abs(Math.IEEEremainder(a + Math.PI / 2, Math.PI * 2));
            if (ds < 1.2 || dn < 0.5) continue;
            double rr = RP + 5 + r0.nextDouble() * 11;
            agujas.add(new double[]{mx + Math.cos(a) * rr, mz + Math.sin(a) * rr, 7 + r0.nextDouble() * 13, 2.2 + r0.nextDouble() * 2.2});
        }
        // el cono hasta CORTE (con sus crestas) y el hombro hasta la plazoleta del pie y su cortado
        int r = (int) Math.ceil(((cima - CORTE) / CONO + RP) * 1.25) + 8;
        int sur = (int) Math.ceil((cima - NUBES) / PEND) + RP + 40;
        bounds(mx - Math.max(r, HOMBRO + 12), FONDO - 10, mz - r, mx + Math.max(r, HOMBRO + 12), cima + 24, mz + Math.max(r, sur));
    }

    // ------------------------------------------------------------------ la forma

    /** La roca del cono en (x, z): lo que se pisa (sin el hombro ni la aspereza). */
    private double cono(double x, double z) {
        double dx = x - mx, dz = z - mz, r = Math.hypot(dx, dz);
        double a = Math.atan2(dz, dx);
        double cr = ruido.fbm(Math.cos(a) * 1.7 + 5.3, Math.sin(a) * 1.7 - 2.9, 3);
        double re = r / (1 + 0.18 * cr);
        return cima - CONO * Math.max(0, re - RP);
    }

    /** Lo ancho del hombro (medio) a la altura z: varia un poco. */
    private double ancho(double z) {
        return HOMBRO + ruido.fbm(z * 0.05 + 11.1, 3.3, 2) * 3;
    }

    /** El hombro: baja hacia el sur desde el mirador; sus lados caen como el cono (y en el mundo de nubes, su final). */
    private double hombro(double x, double z) {
        double z0 = mz + RP - 2;
        if (z < z0 - 4) return Double.NEGATIVE_INFINITY;
        double h = cima - PEND * Math.max(0, z - z0) - CONO * Math.max(0, Math.abs(x - mx) - ancho(z));
        if (z < z0) h -= CONO * (z0 - z) * 3;
        if (z > finHombro) h -= CONO * (z - finHombro);
        return h;
    }

    /** Lo alto de la roca en (x, z) (lo que se pisa): el cono o el hombro, con aspereza fuera del mirador. */
    public double alto(double x, double z) {
        double h = Math.max(cono(x, z), hombro(x, z));
        for (double[] g : agujas) {
            double d = Math.hypot(x - g[0], z - g[1]);
            if (d < g[3]) h = Math.max(h, cono(g[0], g[1]) + g[2] * Math.pow(1 - d / g[3], 1.2));
        }
        double d = Math.hypot(x - mx, z - mz) - RP;
        if (d > 0) h += ruido.fbm(x * 0.09 + 1.7, z * 0.09 - 4.2, 2) * 2.4 * Math.min(1, d / 6);
        return h;
    }

    /** Lo mas bajo de la roca en (x, z) (cuelga como una isla), o MAX si ahi la roca no llega a CORTE. */
    private int bajo(int x, int z, double h) {
        if (h < CORTE) return Integer.MAX_VALUE;
        // el radio de lo que llega a CORTE en esa direccion, y la punta colgando hasta FONDO en el centro
        double r = Math.hypot(x - mx, z - mz);
        double rc = RP + (cima - CORTE) / CONO;
        double k = Math.min(1, r / rc);
        double y = FONDO + (CORTE - FONDO) * Math.pow(k, 1.8) + ruido.fbm(x * 0.12, z * 0.12, 2) * 6;
        return (int) Math.round(Math.min(y, h - 3));
    }

    // ------------------------------------------------------------------ el sendero

    /** Traza el sendero (una vez; lo piden la construccion y los que preguntan por sus sitios). */
    private synchronized void preparar() {
        if (preparada) return;
        preparada = true;
        trazar();
    }

    /** El plano del hombro en (x, z) (sin aspereza): donde va el sendero. */
    private double plano(double z) {
        return cima - PEND * Math.max(0, z - (mz + RP - 2));
    }

    private void trazar() {
        sendero.clear();
        double z = mz + RP - 2, x = mx;
        // del mirador al hombro, recto hacia el sur unos bloques
        for (double s = 0; s <= 4; s += 0.5) punto(x, z + s, plano(z + s));
        z += 4;
        int lado = 1;
        double largo = MEDIO; // el primero sale del centro: medio tramo
        boolean fin = false;
        for (int k = 0; k < 64 && !fin; k++) {
            // el tramo: de x a mx + lado * MEDIO, bajando 1 de cada TRAMO y avanzando hacia el sur para seguir el plano
            double x1 = mx + lado * MEDIO;
            double dzdx = 1.0 / TRAMO / PEND;
            int n = (int) Math.ceil(largo * 2);
            for (int i = 1; i <= n; i++) {
                double t = i / (double) n;
                double px = x + (x1 - x) * t, pz = z + largo * dzdx * t;
                double h = plano(pz);
                if (h <= NUBES) {
                    fin = true;
                    x = px;
                    z = pz;
                    break;
                }
                punto(px, pz, h);
            }
            if (fin) break;
            z += largo * dzdx;
            x = x1;
            // el recodo: media vuelta hacia fuera, de 2*RECODO hacia el sur
            double cxr = x, czr = z + RECODO;
            recodos.add(new double[]{cxr + lado * RECODO, czr, plano(czr), lado});
            int m = (int) Math.ceil(Math.PI * RECODO * 2);
            for (int i = 1; i <= m; i++) {
                double a = -Math.PI / 2 + Math.PI * i / m;
                double px = cxr + lado * Math.cos(a) * RECODO, pz = czr + Math.sin(a) * RECODO;
                double h = plano(pz);
                if (h <= NUBES) {
                    fin = true;
                    x = px;
                    z = pz;
                    break;
                }
                punto(px, pz, h);
            }
            if (fin) break;
            z += 2 * RECODO;
            lado = -lado;
            largo = 2 * MEDIO;
        }
        // la plazoleta del pie (llana, a la altura de los caminos de nube firme), un poco mas alla de donde acaba el
        // sendero (en la direccion en que iba): asi no se come su ultimo tramo, que baja hasta ella
        double hp = NUBES;
        double[] u = sendero.get(sendero.size() - 1), v = sendero.get(Math.max(0, sendero.size() - 3));
        double ex = u[0] - v[0], ez = u[1] - v[1], el = Math.hypot(ex, ez);
        if (el < 1e-6) {
            ex = 0;
            ez = 1;
            el = 1;
        }
        double px = x + ex / el * 5, pz = z + ez / el * 5;
        pie = new double[]{px, pz, hp};
        for (int dx = -5; dx <= 5; dx++)
            for (int dz = -5; dz <= 5; dz++)
                if (dx * dx + dz * dz <= 26) pisa.putIfAbsent(col((int) Math.floor(px) + dx, (int) Math.floor(pz) + dz), hp);
        finHombro = Math.max(z, pz) + 7;
        // el pretil: a los lados de cada punto, fuera del sendero
        for (int i = 0; i + 1 < sendero.size(); i++) {
            double[] p = sendero.get(i), q = sendero.get(i + 1);
            double ux = q[0] - p[0], uz = q[1] - p[1], l = Math.hypot(ux, uz);
            if (l < 1e-6) continue;
            for (int s = -1; s <= 1; s += 2) {
                long c = col((int) Math.floor(p[0] - uz / l * 2.6 * s), (int) Math.floor(p[1] + ux / l * 2.6 * s));
                if (!pisa.containsKey(c)) pretil.merge(c, p[2], Math::max);
            }
        }
        pretil.keySet().removeAll(pisa.keySet());
    }

    /** Un punto del sendero: lo apunta y marca las columnas de su anchura (3). */
    private void punto(double x, double z, double h) {
        sendero.add(new double[]{x, z, h});
        double hq = Math.round(h * 2) / 2.0;
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) {
                if (Math.hypot(dx, dz) > 1.45) continue;
                pisa.merge(col((int) Math.floor(x + dx * 0.95), (int) Math.floor(z + dz * 0.95)), hq, Math::max);
            }
    }

    /** La nube del mar de nubes (por su clase: fuera del juego no hay bloques del mod registrados). */
    private static boolean nube(BlockState s) {
        return s.getBlock() instanceof com.darkcraft.world.NubeBlock;
    }

    private static long col(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    /** El pie del sendero {x, z, altura que se pisa}: la plazoleta donde llega el camino de nubes. */
    public double[] pie() {
        preparar();
        return pie.clone();
    }

    /** El sendero, de la cima al pie: {x, z, altura que se pisa}. */
    public List<double[]> sendero() {
        preparar();
        return Collections.unmodifiableList(sendero);
    }

    /** Los recodos (con su torre en ruinas al lado), de arriba abajo: {x, z, altura, lado}. */
    public List<double[]> recodos() {
        preparar();
        return Collections.unmodifiableList(recodos);
    }

    /** Lo lejos que esta (x, z) de la montana (0 encima): del cono a la altura de la nube o del hombro hasta su pie. */
    public double distancia(double x, double z) {
        double rc = RP + (cima - NUBES) / CONO;
        double dc = Math.hypot(x - mx, z - mz) - rc;
        double zf = pie()[1];
        double dzh = z < mz ? mz - z : z > zf + 6 ? z - zf - 6 : 0;
        double dh = Math.hypot(Math.max(0, Math.abs(x - mx) - HOMBRO), dzh);
        return Math.max(0, Math.min(dc, dh));
    }

    /** El arco de colmillos del mirador (de donde sale el puente): {x, y que se pisa, z}. */
    public int[] arco() {
        return new int[]{mx, cima, mz - RP + 1};
    }

    // ------------------------------------------------------------------ los bloques

    /** La roca: oscura, con vetas de basalto en los cortados y algo de toba y grava arriba. */
    private BlockState roca(int x, int y, int z, int prof) {
        if (prof <= 1) {
            int p = Hash.pct(x, y, z, seed ^ 0x40CA);
            if (p < 30) return Pal.BLACKSTONE;
            if (p < 50) return Pal.DS_COBBLED;
            if (p < 64) return Pal.DEEPSLATE;
            if (p < 80) return Pal.TUFF;
            if (p < 92) return Pal.SMOOTH_BASALT;
            return Pal.b(Blocks.BASALT);
        }
        if (Math.floorMod(y + (int) Math.round(ruido.value(x * 0.05, z * 0.05) * 3), 7) == 0) return Pal.SMOOTH_BASALT;
        return Hash.pct(x, y, z, seed ^ 0x77) < 70 ? Pal.DEEPSLATE : Pal.BLACKSTONE;
    }

    private BlockState piso(int x, int y, int z) {
        int p = Hash.pct(x, y, z, seed ^ 0x5E);
        return p < 55 ? Pal.PB_BRICKS : p < 75 ? Pal.PB_BRICKS_CRACKED : p < 90 ? Pal.DS_POLISHED : Pal.DS_TILES;
    }

    private BlockState losa(int x, int y, int z) {
        return Hash.pct(x, y, z, seed ^ 0x5E) < 70 ? Pal.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, false) : Pal.slab(Blocks.POLISHED_DEEPSLATE_SLAB, false);
    }

    @Override
    public void build(Canvas c) {
        preparar();
        int ax = Math.max(x0, c.minX), bx = Math.min(x1, c.maxX), az = Math.max(z0, c.minZ), bz = Math.min(z1, c.maxZ);
        if (ax > bx || az > bz) return;
        // 1) la roca
        for (int x = ax; x <= bx; x++)
            for (int z = az; z <= bz; z++) {
                double hd = alto(x + 0.5, z + 0.5);
                int h = (int) Math.floor(hd);
                int y0 = bajo(x, z, hd);
                if (y0 == Integer.MAX_VALUE) continue;
                for (int y = y0; y < h; y++) c.set(x, y, z, roca(x, y, z, h - 1 - y));
                // lo de encima de la roca: fuera (las nubes de alrededor no)
                for (int y = h; y <= h + 3 && y <= cima + 20; y++) {
                    BlockState s = c.get(x, y, z);
                    if (!s.isAir() && !nube(s)) c.set(x, y, z, Pal.AIR);
                }
                // matojos secos, huesos y calaveras en lo llano
                if (Math.hypot(x - mx, z - mz) > RP + 1) {
                    int p = Hash.h(x, 3, z, seed) % 1000;
                    if (p < 14) {
                        c.set(x, h - 1, z, Pal.COARSE_DIRT);
                        c.set(x, h, z, Pal.b(Blocks.DEAD_BUSH));
                    } else if (p < 20) c.set(x, h, z, Pal.b(Blocks.SKELETON_SKULL));
                    else if (p < 28) c.set(x, h, z, Pal.slab(Blocks.COBBLED_DEEPSLATE_SLAB, false));
                    else if (p < 31) c.set(x, h, z, TorreCupula.hueso(Hash.pct(x, 4, z, seed) < 50 ? Direction.Axis.X : Direction.Axis.Z));
                }
            }
        // 2) el mirador de la cima
        mirador(c);
        // 3) el sendero: lo que se pisa, lo de encima despejado y relleno por debajo hasta la roca
        for (int x = ax; x <= bx; x++)
            for (int z = az; z <= bz; z++) {
                Double hp = pisa.get(col(x, z));
                if (hp == null) continue;
                int n = (int) Math.floor(hp);
                boolean media = hp - n > 0.25;
                int top = (int) Math.ceil(Math.max(alto(x + 0.5, z + 0.5), hp)) + 1;
                if (media) {
                    c.set(x, n, z, losa(x, n, z));
                    c.set(x, n - 1, z, piso(x, n - 1, z));
                    for (int y = n + 1; y <= Math.max(n + 4, top); y++) c.set(x, y, z, Pal.AIR);
                } else {
                    c.set(x, n - 1, z, piso(x, n - 1, z));
                    for (int y = n; y <= Math.max(n + 3, top); y++) c.set(x, y, z, Pal.AIR);
                }
                for (int y = n - 2; y >= n - 30; y--) {
                    if (!c.isAir(x, y, z) && !nube(c.get(x, y, z))) break;
                    c.set(x, y, z, Pal.wall(x, y, z));
                }
            }
        // 4) el pretil (donde hay caida) y los faroles
        for (Map.Entry<Long, Double> e : pretil.entrySet()) {
            int x = (int) (e.getKey() >> 32), z = (int) (long) e.getKey();
            if (!c.inXZ(x, z)) continue;
            double hp = e.getValue();
            if (alto(x + 0.5, z + 0.5) > hp - 0.6) continue;
            int n = (int) Math.floor(hp);
            for (int y = n - 1; y >= n - 30; y--) {
                if (!c.isAir(x, y, z) && !nube(c.get(x, y, z))) break;
                c.set(x, y, z, Pal.wall(x, y, z));
            }
            if (Hash.pct(x, n, z, seed ^ 0x9A) < 82) c.unir(x, n, z, Pal.b(Blocks.COBBLED_DEEPSLATE_WALL));
            if (Math.floorMod(x * 7 + z * 13, 23) == 0) {
                c.set(x, n, z, TorreCupula.HUESO);
                c.set(x, n + 1, z, Pal.SOUL_LANTERN);
            }
        }
        // 5) los restos de torres junto a los recodos
        for (int i = 0; i < recodos.size(); i++) {
            if (i % 2 == 1 && i != recodos.size() - 1) continue;
            double[] r = recodos.get(i);
            torre(c, (int) Math.floor(r[0] + r[3] * 5), (int) Math.floor(r[1]), (int) Math.floor(r[2]), i);
        }
    }

    /** El mirador: llano enlosado, pretil con dos aberturas (el sendero al sur, el arco al norte) y el arco de colmillos. */
    private void mirador(Canvas c) {
        int y = cima;
        for (int x = mx - RP - 1; x <= mx + RP + 1; x++)
            for (int z = mz - RP - 1; z <= mz + RP + 1; z++) {
                if (!c.inXZ(x, z)) continue;
                double r = Math.hypot(x - mx, z - mz);
                if (r > RP + 0.5) continue;
                boolean anillo = r > RP - 2.2 && r <= RP - 1.2;
                c.set(x, y - 1, z, anillo ? TorreCupula.hueso(Math.abs(x - mx) > Math.abs(z - mz) ? Direction.Axis.Z : Direction.Axis.X)
                        : ((x + z) & 1) == 0 ? Pal.PB_BRICKS : Pal.DS_TILES);
                for (int yy = y; yy <= y + 4; yy++) c.set(x, yy, z, Pal.AIR);
                if (r > RP - 0.6 && Math.abs(x - mx) > 2) c.unir(x, y, z, Pal.b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            }
        c.set(mx, y - 1, mz, Pal.PB_CHISELED);
        // dos faroles en postes de hueso a los lados de la bajada
        for (int s = -1; s <= 1; s += 2) {
            int x = mx + s * 3, z = mz + RP - 1;
            if (!c.inXZ(x, z)) continue;
            for (int yy = y; yy <= y + 2; yy++) c.set(x, yy, z, TorreCupula.HUESO);
            c.set(x, y + 3, z, Pal.SOUL_LANTERN);
        }
        // el arco de colmillos, de cara a la fortaleza (como el de la isla, mas grande)
        int za = mz - RP + 1;
        for (int s = -1; s <= 1; s += 2)
            for (int i = 0; i <= 40; i++) {
                double t = i / 40.0;
                double x = mx + s * (5.5 - t * 4.2), yy = y - 1 + Math.sin(t * Math.PI * 0.55) * 15;
                int bx = (int) Math.round(x), by = (int) Math.round(yy);
                if (!c.inXZ(bx, za)) continue;
                c.set(bx, by, za, TorreCupula.HUESO);
                if (t < 0.55) c.set(bx, by, za + 1, TorreCupula.HUESO);
                if (t < 0.25) c.set(bx, by, za - 1, TorreCupula.HUESO);
            }
        if (c.inXZ(mx, za)) {
            c.set(mx, y + 14, za, Pal.b(Blocks.SKELETON_SKULL));
            c.set(mx - 1, y + 13, za, Pal.CHAIN);
            c.set(mx + 1, y + 13, za, Pal.CHAIN);
            c.set(mx, y + 12, za, Pal.CHAIN);
            c.set(mx, y + 11, za, Pal.SOUL_LANTERN_HANG);
        }
    }

    /** Una torre redonda en ruinas (radio 3) junto a un recodo, con su puerta hacia el sendero y cascotes alrededor. */
    private void torre(Canvas c, int tx, int tz, int y, int k) {
        int alto = 6 + Hash.h(k, 5, 1, seed) % 8;
        for (int x = tx - 5; x <= tx + 5; x++)
            for (int z = tz - 5; z <= tz + 5; z++) {
                if (!c.inXZ(x, z)) continue;
                double d = Math.hypot(x - tx, z - tz);
                if (d > 3.5) {
                    // cascotes alrededor
                    if (d < 5.5 && Hash.pct(x, k, z, seed ^ 0x3C) < 18 && c.isAir(x, y, z) && !c.isAir(x, y - 1, z))
                        c.set(x, y, z, Hash.pct(x, 1, z, seed) < 50 ? Pal.DS_COBBLED : Pal.slab(Blocks.COBBLED_DEEPSLATE_SLAB, false));
                    continue;
                }
                // los cimientos hasta la roca
                for (int yy = y - 1; yy >= y - 24; yy--) {
                    if (!c.isAir(x, yy, z) && !nube(c.get(x, yy, z))) break;
                    c.set(x, yy, z, Pal.foundation(x, yy, z));
                }
                c.set(x, y - 1, z, d < 2.5 ? Pal.DS_TILES_CRACKED : Pal.DS_BRICKS);
                boolean muro = d > 2.4;
                // lo alto, mellado
                int arriba = alto - (int) Math.round(Math.abs(ruido.value(x * 0.7 + k, z * 0.7)) * 6);
                for (int h = 0; h <= alto + 1; h++) {
                    if (!muro) {
                        c.set(x, y + h, z, Pal.AIR);
                        continue;
                    }
                    if (h > arriba) continue;
                    // la puerta (hacia el centro de la montana, donde esta el sendero) y alguna aspillera
                    double a = Math.atan2(z - tz, x - tx), hacia = Math.atan2(mz - tz, mx - tx);
                    double da = Math.abs(Math.IEEEremainder(a - hacia, Math.PI * 2));
                    if (da < 0.45 && h < 3) {
                        c.set(x, y + h, z, Pal.AIR);
                        continue;
                    }
                    if (h == 4 && Math.abs(Math.IEEEremainder(a - hacia - Math.PI, Math.PI * 2)) < 0.3) {
                        c.set(x, y + h, z, Pal.AIR);
                        continue;
                    }
                    c.set(x, y + h, z, Hash.pct(x, y + h, z, seed ^ k) < 30 ? Pal.DS_BRICKS_CRACKED : Hash.pct(x, y + h, z, seed) < 20 ? Pal.PB_BRICKS_CRACKED : Pal.DS_BRICKS);
                }
            }
        if (c.inXZ(tx, tz)) {
            c.set(tx, y, tz, Pal.b(Blocks.SKELETON_SKULL));
            c.set(tx + 1, y, tz, Pal.slab(Blocks.COBBLED_DEEPSLATE_SLAB, false));
        }
    }
}
