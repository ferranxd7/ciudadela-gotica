package com.darkcraft.world;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.fortaleza.Fortalezas;
import com.darkcraft.fortaleza.Puente;
import com.darkcraft.gen.CastlePlan;
import com.darkcraft.gen.FortalezaPlan;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * El camino de nubes provisional (parte 3a; el de verdad, largo y con paradas, llega con la torre nueva en la parte 5):
 * de la puerta de la Torre de Ascenso de cada Reino de Valdrenn hasta donde la llave de hueso levanta la escalera de su
 * Fortaleza de Hueso. Tres de ancho de nube firme a la altura {@link NubesGenerador#CAMINO}, sobre un banco de nube; por
 * encima y a los lados se despeja la nube (donde el mar de nubes sube mas que el camino queda una zanja, y el camino
 * siempre asoma un bloque sobre la nube de al lado) y cada {@link #FAROL} bloques, a un lado y a otro, un farol de almas
 * sobre un hueso. Sale hacia el sur de la torre, rodea la isla y sus islotes a {@link #RODEO} bloques del centro hasta
 * quedar de cara a la fortaleza, y llega a la escalera por el sur (si viene del norte, rodea la isla de la fortaleza).
 * Delante de la puerta de la torre y donde se levanta la escalera, una plazoleta redonda.
 * <p>
 * Se pone al hacer la superficie de cada chunk ({@link NubesGenerador#buildSurface}), antes que el castillo: lo que el
 * castillo ponga encima (la torre, su zocalo) manda.
 */
public final class CaminoNubes {
    private CaminoNubes() {}

    static final int Y = NubesGenerador.CAMINO;
    /** Medio ancho del camino (sale de 3) y hasta donde se despeja la nube a los lados. */
    static final double ANCHO = 1.6, DESPEJE = 4.5;
    /** El radio del rodeo alrededor del centro del reino (la isla y sus islotes llegan a 400). */
    public static final int RODEO = 445;
    /** Cada cuanto (a lo largo del camino) un farol, alternando el lado. */
    static final int FAROL = 28;
    /** Lo que se aparta como mucho la cuenta rapida (sin los planos) de la de verdad. */
    private static final int MARGEN = 64;

    /** La ruta de un reino: los puntos del camino (centros de bloque), las plazoletas {x, z, radio} y los faroles {x, z}. */
    public record Ruta(double[] px, double[] pz, int[][] plazas, int[][] faroles, int x0, int z0, int x1, int z1) {
        /** Lo largo del camino, en bloques. */
        public double largo() {
            double l = 0;
            for (int i = 0; i + 1 < px.length; i++) l += Math.hypot(px[i + 1] - px[i], pz[i + 1] - pz[i]);
            return l;
        }

        /** El punto a {@code s} bloques del principio (sobre el camino), {x, z}. */
        public double[] punto(double s) {
            for (int i = 0; i + 1 < px.length; i++) {
                double l = Math.hypot(px[i + 1] - px[i], pz[i + 1] - pz[i]);
                if (s <= l || i + 2 == px.length) {
                    double t = l <= 0 ? 0 : Math.min(1, s / l);
                    return new double[]{px[i] + (px[i + 1] - px[i]) * t, pz[i] + (pz[i + 1] - pz[i]) * t};
                }
                s -= l;
            }
            return new double[]{px[0], pz[0]};
        }
    }

    private static final Map<String, Ruta> RUTAS = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Ruta> e) {
            return size() > 64;
        }
    };

    /** La ruta del camino del reino que esta en (sx, sz), o null si no tiene (sin Torre de Ascenso). */
    public static Ruta ruta(long seed, int sx, int sz) {
        String k = seed + ":" + sx + ":" + sz;
        synchronized (RUTAS) {
            if (RUTAS.containsKey(k)) return RUTAS.get(k);
            Ruta r = null;
            try {
                r = calcular(seed, sx, sz);
            } catch (Throwable t) {
                DarkCraftMod.LOGGER.error("No se pudo trazar el camino de nubes del reino {},{}", sx, sz, t);
            }
            RUTAS.put(k, r);
            return r;
        }
    }

    private static Ruta calcular(long seed, int sx, int sz) {
        CastlePlan plan = PlanCache.get(seed, sx, sz, (x, z) -> NubesGenerador.SUELO);
        CastlePlan.Cine ci = plan.cine();
        if (!ci.asc()) return null;
        int[] f = Fortalezas.de(seed, sx, sz);
        FortalezaPlan fp = Fortalezas.plan(seed, f[0], f[1]);
        int[] b = Puente.centro(fp);
        List<double[]> p = puntos(seed, sx, sz, ci.ascX(), ci.ascZ(), f, b);
        int n = p.size();
        double[] px = new double[n], pz = new double[n];
        double minX = Double.MAX_VALUE, minZ = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            px[i] = p.get(i)[0];
            pz[i] = p.get(i)[1];
            minX = Math.min(minX, px[i]);
            maxX = Math.max(maxX, px[i]);
            minZ = Math.min(minZ, pz[i]);
            maxZ = Math.max(maxZ, pz[i]);
        }
        int[][] plazas = {{ci.ascX(), ci.ascZ() + 12, 5}, {b[0], b[1], 10}};
        // los faroles: cada FAROL bloques, a un lado y a otro, nunca encima del camino (en las esquinas) ni junto a la
        // torre o la escalera
        List<int[]> fl = new ArrayList<>();
        double total = 0;
        for (int i = 0; i + 1 < n; i++) total += Math.hypot(px[i + 1] - px[i], pz[i + 1] - pz[i]);
        Ruta sin = new Ruta(px, pz, plazas, new int[0][], 0, 0, 0, 0);
        for (int k = 1; k * FAROL < total - 18; k++) {
            double s = k * FAROL;
            if (s < 26) continue;
            double[] q = sin.punto(s), q2 = sin.punto(s + 0.5);
            double ux = q2[0] - q[0], uz = q2[1] - q[1], l = Math.hypot(ux, uz);
            if (l < 1e-6) continue;
            double lado = (k & 1) == 0 ? 2.6 : -2.6;
            int fx = (int) Math.floor(q[0] - uz / l * lado), fz = (int) Math.floor(q[1] + ux / l * lado);
            if (distancia(px, pz, fx + 0.5, fz + 0.5) < 2.2) continue;
            if (Math.hypot(fx - b[0], fz - b[1]) < 15) continue;
            fl.add(new int[]{fx, fz});
        }
        int m = 16;
        return new Ruta(px, pz, plazas, fl.toArray(new int[0][]), (int) Math.floor(minX) - m, (int) Math.floor(minZ) - m,
                (int) Math.ceil(maxX) + m, (int) Math.ceil(maxZ) + m);
    }

    /**
     * Los puntos del camino (centros de bloque): de la puerta de la torre (ax, az) al sur hasta el rodeo, el rodeo hacia
     * la fortaleza f si hace falta, la vuelta por la isla de la fortaleza si viene del norte y la escalera b por el sur;
     * y por fuera de los demas reinos y fortalezas ({@link #esquivar}). Lo de antes de llegar a la fortaleza no depende de
     * los planos (la torre solo cambia el primer tramo y la escalera los de junto a la fortaleza): asi la cuenta rapida
     * ({@link #cerca}) va por el mismo sitio.
     */
    static List<double[]> puntos(long seed, int sx, int sz, int ax, int az, int[] f, int[] b) {
        // el centro de la isla de la fortaleza (FortalezaPlan: cz - 3)
        double cfx = f[0] + 0.5, cfz = f[1] - 2.5;
        List<double[]> p = new ArrayList<>();
        p.add(new double[]{ax + 0.5, az + 5.5});
        double[] s1 = {ax + 0.5, sz + RODEO + 0.5};
        p.add(s1);
        double tT = Math.atan2(s1[1] - sz, s1[0] - sx), tF = Math.atan2(f[1] - sz, f[0] - sx);
        double d = tF - tT;
        while (d > Math.PI) d -= 2 * Math.PI;
        while (d <= -Math.PI) d += 2 * Math.PI;
        if (Math.abs(d) > Math.toRadians(50)) {
            int n = (int) Math.ceil(Math.abs(d) / Math.toRadians(12));
            for (int i = 1; i <= n; i++) {
                double a = tT + d * i / n;
                p.add(new double[]{sx + 0.5 + Math.cos(a) * RODEO, sz + 0.5 + Math.sin(a) * RODEO});
            }
        }
        // a la escalera se llega por el sur: si el camino pasaria bajo la isla, la rodea por el lado por el que viene
        double[] b1a = {f[0] - 4.5, f[1] + 105.5};
        double[] b1 = {b[0] + 0.5, b[1] + 40.5};
        double[] u = p.get(p.size() - 1);
        if (distSeg(cfx, cfz, u[0], u[1], b1a[0], b1a[1]) < 92) {
            int s = u[0] >= cfx ? 1 : -1;
            p.add(new double[]{cfx + s * 104, cfz - 12});
            p.add(new double[]{cfx + s * 104, b1[1]});
        }
        p.add(b1);
        p.add(new double[]{b[0] + 0.5, b[1] + 0.5});
        esquivar(seed, sx, sz, f, p);
        return p;
    }

    /**
     * Que el camino no pase por debajo de otro reino (su isla, sus islotes, sus cascadas, el pie de su torre: a menos de
     * {@link #RODEO} de su centro) ni de otra fortaleza (112): donde un tramo se mete, se le anade un punto empujado
     * hacia fuera, hasta que ninguno se mete. Ni el primer tramo (junto a la torre) ni el ultimo (a la escalera) se tocan,
     * ni los que ya empiezan o acaban dentro (dos fortalezas casi juntas).
     */
    private static void esquivar(long seed, int sx, int sz, int[] f, List<double[]> p) {
        List<double[]> obs = new ArrayList<>();
        int rx0 = Math.floorDiv(sx, CastleSites.REGION), rz0 = Math.floorDiv(sz, CastleSites.REGION);
        for (int rx = rx0 - 2; rx <= rx0 + 2; rx++)
            for (int rz = rz0 - 2; rz <= rz0 + 2; rz++) {
                int[] o = CastleSites.site(seed, rx, rz);
                if (o == null || (o[0] == sx && o[1] == sz)) continue;
                obs.add(new double[]{o[0] + 0.5, o[1] + 0.5, RODEO});
                int[] of = Fortalezas.de(seed, o[0], o[1]);
                if (of[0] != f[0] || of[1] != f[1]) obs.add(new double[]{of[0] + 0.5, of[1] - 2.5, 112});
            }
        for (int it = 0; it < 80; it++) {
            boolean cambio = false;
            for (int i = 1; i + 2 < p.size() && !cambio; i++) {
                double[] a = p.get(i), c = p.get(i + 1);
                for (double[] o : obs) {
                    if (distSeg(o[0], o[1], a[0], a[1], c[0], c[1]) >= o[2]) continue;
                    // si una punta ya cae dentro (dos fortalezas casi juntas) no hay rodeo que valga: se deja
                    if (Math.hypot(a[0] - o[0], a[1] - o[1]) < o[2] || Math.hypot(c[0] - o[0], c[1] - o[1]) < o[2]) continue;
                    double vx = c[0] - a[0], vz = c[1] - a[1], l2 = vx * vx + vz * vz;
                    double t = l2 <= 0 ? 0 : Math.max(0, Math.min(1, ((o[0] - a[0]) * vx + (o[1] - a[1]) * vz) / l2));
                    double qx = a[0] + vx * t - o[0], qz = a[1] + vz * t - o[1], ql = Math.hypot(qx, qz);
                    if (ql < 1e-3) {
                        double l = Math.max(1e-6, Math.sqrt(l2));
                        qx = -vz / l;
                        qz = vx / l;
                        ql = 1;
                    }
                    p.add(i + 1, new double[]{o[0] + qx / ql * (o[2] + 16), o[1] + qz / ql * (o[2] + 16)});
                    cambio = true;
                    break;
                }
            }
            if (!cambio) break;
        }
    }

    /** Distancia de (x, z) al segmento (ax, az)-(bx, bz). */
    static double distSeg(double x, double z, double ax, double az, double bx, double bz) {
        double vx = bx - ax, vz = bz - az, l2 = vx * vx + vz * vz;
        double t = l2 <= 0 ? 0 : Math.max(0, Math.min(1, ((x - ax) * vx + (z - az) * vz) / l2));
        double qx = ax + vx * t - x, qz = az + vz * t - z;
        return Math.sqrt(qx * qx + qz * qz);
    }

    static double distancia(double[] px, double[] pz, double x, double z) {
        double d = Double.MAX_VALUE;
        for (int i = 0; i + 1 < px.length; i++) d = Math.min(d, distSeg(x, z, px[i], pz[i], px[i + 1], pz[i + 1]));
        return d;
    }

    /** Los caminos de la cuenta rapida (sin los planos), por reino: {px, pz}. */
    private static final Map<String, double[][]> APROX = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, double[][]> e) {
            return size() > 160;
        }
    };

    /** Cuenta rapida, sin los planos del castillo ni de la fortaleza: si el chunk de centro (mx, mz) puede tocar el camino. */
    private static boolean cerca(long seed, int[] s, double mx, double mz) {
        if (Math.hypot(mx - s[0], mz - s[1]) <= RODEO + MARGEN) return true;
        int[] f = Fortalezas.de(seed, s[0], s[1]);
        // junto a la fortaleza los ultimos puntos dependen de donde cae la escalera de verdad
        if (Math.hypot(mx - f[0], mz - f[1]) <= 240) return true;
        double[][] q;
        String k = seed + ":" + s[0] + ":" + s[1];
        synchronized (APROX) {
            q = APROX.get(k);
            if (q == null) {
                List<double[]> p = puntos(seed, s[0], s[1], s[0], s[1] + 318, f, new int[]{f[0] - 5, f[1] + 65});
                q = new double[2][p.size()];
                for (int i = 0; i < p.size(); i++) {
                    q[0][i] = p.get(i)[0];
                    q[1][i] = p.get(i)[1];
                }
                APROX.put(k, q);
            }
        }
        return distancia(q[0], q[1], mx, mz) <= MARGEN;
    }

    /** Pone en el chunk lo que le toque de los caminos de los reinos de alrededor. */
    public static void construir(long seed, ChunkAccess c) {
        int x0 = c.getPos().getMinBlockX(), z0 = c.getPos().getMinBlockZ();
        double mx = x0 + 8, mz = z0 + 8;
        int alc = Fortalezas.DIST + RODEO + 300;
        int rx0 = Math.floorDiv(x0 - alc, CastleSites.REGION), rx1 = Math.floorDiv(x0 + 15 + alc, CastleSites.REGION);
        int rz0 = Math.floorDiv(z0 - alc, CastleSites.REGION), rz1 = Math.floorDiv(z0 + 15 + alc, CastleSites.REGION);
        for (int rx = rx0; rx <= rx1; rx++)
            for (int rz = rz0; rz <= rz1; rz++) {
                int[] s = CastleSites.site(seed, rx, rz);
                if (s == null || !cerca(seed, s, mx, mz)) continue;
                Ruta r = ruta(seed, s[0], s[1]);
                if (r == null || x0 + 15 < r.x0() || x0 > r.x1() || z0 + 15 < r.z0() || z0 > r.z1()) continue;
                pintar(c, r, x0, z0);
            }
    }

    private static void pintar(ChunkAccess c, Ruta r, int x0, int z0) {
        double[] px = r.px(), pz = r.pz();
        // los tramos que llegan a este chunk
        List<Integer> tramos = new ArrayList<>();
        double ex = DESPEJE + 1;
        for (int i = 0; i + 1 < px.length; i++) {
            if (Math.max(px[i], px[i + 1]) + ex < x0 || Math.min(px[i], px[i + 1]) - ex > x0 + 16) continue;
            if (Math.max(pz[i], pz[i + 1]) + ex < z0 || Math.min(pz[i], pz[i + 1]) - ex > z0 + 16) continue;
            tramos.add(i);
        }
        List<int[]> plazas = new ArrayList<>();
        for (int[] pl : r.plazas())
            if (pl[0] + pl[2] + 4 >= x0 && pl[0] - pl[2] - 4 <= x0 + 15 && pl[1] + pl[2] + 4 >= z0 && pl[1] - pl[2] - 4 <= z0 + 15) plazas.add(pl);
        boolean faroles = false;
        for (int[] f : r.faroles()) if (f[0] >= x0 && f[0] <= x0 + 15 && f[1] >= z0 && f[1] <= z0 + 15) faroles = true;
        if (tramos.isEmpty() && plazas.isEmpty() && !faroles) return;
        BlockState firme = DarkCraftMod.NUBE_FIRME.get().defaultBlockState();
        BlockState[] nube = NubesGenerador.nubes();
        BlockState aire = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dx = 0; dx < 16; dx++)
            for (int dz = 0; dz < 16; dz++) {
                int x = x0 + dx, z = z0 + dz;
                double cx = x + 0.5, cz = z + 0.5;
                double d = Double.MAX_VALUE, dp = Double.MAX_VALUE;
                for (int i : tramos) d = Math.min(d, distSeg(cx, cz, px[i], pz[i], px[i + 1], pz[i + 1]));
                for (int[] pl : plazas) dp = Math.min(dp, Math.hypot(cx - (pl[0] + 0.5), cz - (pl[1] + 0.5)) - pl[2]);
                boolean camino = d <= ANCHO || dp <= 0;
                if (!camino && d > DESPEJE && dp > 3) continue;
                int arriba = NubesGenerador.cima(x, z);
                // se despeja la nube de encima (y a los lados tambien la de la altura del camino: asi asoma un bloque)
                for (int y = camino ? Y + 1 : Y; y <= Math.max(arriba, Y + 4); y++)
                    if (c.getBlockState(m.set(x, y, z)).is(DarkCraftMod.NUBE.get())) c.setBlockState(m, aire, false);
                if (!camino) {
                    // lo que queda arriba en la zanja vuelve a ser "lo alto" de su columna (el tono claro)
                    if (arriba >= Y)
                        for (int y = Y - 1; y >= Y - 6; y--)
                            if (c.getBlockState(m.set(x, y, z)).is(DarkCraftMod.NUBE.get())) c.setBlockState(m, nube[NubeBlock.tono(Y - 1 - y)], false);
                    continue;
                }
                BlockState ahora = c.getBlockState(m.set(x, Y, z));
                if (ahora.isAir() || ahora.is(DarkCraftMod.NUBE.get())) c.setBlockState(m, firme, false);
                // el banco de nube bajo el camino, si el mar de nubes queda mas abajo
                for (int y = Y - 1; y > arriba; y--) {
                    if (!c.getBlockState(m.set(x, y, z)).isAir()) break;
                    c.setBlockState(m, nube[NubeBlock.tono(Y - y)], false);
                }
            }
        BlockState hueso = Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState farol = Blocks.SOUL_LANTERN.defaultBlockState();
        for (int[] f : r.faroles()) {
            if (f[0] < x0 || f[0] > x0 + 15 || f[1] < z0 || f[1] > z0 + 15) continue;
            BlockState ahora = c.getBlockState(m.set(f[0], Y, f[1]));
            if (!ahora.isAir() && !ahora.is(DarkCraftMod.NUBE.get())) continue;
            c.setBlockState(m, firme, false);
            c.setBlockState(m.set(f[0], Y + 1, f[1]), hueso, false);
            c.setBlockState(m.set(f[0], Y + 2, f[1]), farol, false);
        }
    }
}
