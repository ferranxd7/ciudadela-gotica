package com.darkcraft.cine;

import com.darkcraft.gavran.SalaArbol;
import com.darkcraft.gavran.VictoriaGavran;
import com.darkcraft.gen.CastlePlan;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleFunction;

/**
 * Los planos de la cinematica del capitulo 1, mientras el narrador cuenta lo que paso en el reino:
 * la torre de ascenso, el pueblo y la catedral, la isla con sus cascadas, la torre-arbol y por
 * ultimo la tormenta vista desde muy alto. Y los del capitulo 5, en la Sala del Arbol ya en flor.
 */
public final class CineShots {
    private CineShots() {}

    private static double[] p(double... v) {
        return v;
    }

    public static List<Shot> chapter1(CastlePlan.Cine c, Vec3 eye, Vec3 dir) {
        float total = Timeline.total(1);
        float c1 = Timeline.cut(0), c2 = Timeline.cut(1), c3 = Timeline.cut(2), c4 = Timeline.cut(3);
        float back = Timeline.back(1);
        int cx = c.cx(), cz = c.cz(), Y = c.y0();
        List<Shot> l = new ArrayList<>();
        // 1) desde lo alto de la torre de ascenso: la camara se aleja y descubre la ciudadela
        double ax = c.ascX(), az = c.ascZ();
        Vec3 ahead = eye.add(dir.scale(20));
        l.add(new Shot(Timeline.NARR_START - 0.6f, c1, 70, 0, 1,
                p(eye.x, eye.y, eye.z, ax + 4, Y + 6, az + 15, ax + 10, Y + 22, az + 32),
                p(ahead.x, ahead.y, ahead.z, cx, Y + 40, cz + 60, cx, Y + 60, cz - 15)));
        // 2) vuelo sobre el pueblo hasta la fachada de la catedral
        l.add(new Shot(c1, c2, 66, -2, 2,
                p(cx + 150, Y + 90, cz + 200, cx + 112, Y + 74, cz + 168, c.cathX(), Y + 50, cz + 118),
                p(c.cathX() + 10, Y + 30, cz + 40, c.cathX(), Y + 30, c.cathZS() - 4, c.cathX(), Y + 27, c.cathZS())));
        // 3) la isla desde fuera: cascadas cayendo al vacio
        double fa = c.fallAng(), fr = c.fallR();
        double fx = cx + Math.cos(fa) * fr, fz = cz + Math.sin(fa) * fr;
        double a0 = fa - Math.toRadians(13), a1 = fa + Math.toRadians(13);
        l.add(new Shot(c2, c3, 62, 1.5f, 0,
                p(cx + Math.cos(a0) * (fr + 96), Y - 22, cz + Math.sin(a0) * (fr + 96),
                        cx + Math.cos(fa) * (fr + 88), Y - 30, cz + Math.sin(fa) * (fr + 88),
                        cx + Math.cos(a1) * (fr + 92), Y - 42, cz + Math.sin(a1) * (fr + 92)),
                p(fx, Y - 6, fz, fx, Y - 22, fz, fx, Y - 48, fz)));
        // 4) vuelta lenta alrededor de la torre-arbol subiendo hasta la sala del jefe
        double tx = c.treeX(), tz = c.treeZ();
        int n = 7;
        double[] pos = new double[n * 3], look = new double[n * 3];
        for (int i = 0; i < n; i++) {
            double k = i / (double) (n - 1);
            double a = Math.toRadians(180 + 90 * k), r = 77 - 7 * k;
            double y = Y + 96 + (c.hallY() - 6 - (Y + 96)) * k;
            pos[i * 3] = tx + Math.cos(a) * r;
            pos[i * 3 + 1] = y;
            pos[i * 3 + 2] = tz + Math.sin(a) * r;
            look[i * 3] = tx;
            look[i * 3 + 1] = y + 10 + 6 * k;
            look[i * 3 + 2] = tz;
        }
        l.add(new Shot(c3, c4, 68, 0, 0, pos, look));
        // 5) la tormenta desde muy alto, encima de la copa del arbol
        l.add(new Shot(c4, back + 0.05f, 58, 0, 2,
                p(cx + 42, Y + 305, cz + 34, cx + 26, Y + 316, cz + 18, cx + 14, Y + 322, cz + 8),
                p(cx, Y + 40, cz - 10, cx, Y + 30, cz - 14, cx, Y + 20, cz - 15)));
        return l;
    }

    // ------------------------------------------------------------------ capitulo 5

    /** Punto a 'r' bloques del centro de la sala, en el angulo 'grados' (0 = este, 90 = sur, -90 = norte). */
    private static double[] en(SalaArbol s, double grados, double r, double y) {
        Vec3 v = s.enAnillo(Math.toRadians(grados), r, y);
        return new double[]{v.x, v.y, v.z};
    }

    private static double[] cat(double[]... v) {
        double[] o = new double[v.length * 3];
        for (int i = 0; i < v.length; i++) System.arraycopy(v[i], 0, o, i * 3, 3);
        return o;
    }

    /**
     * Planos del capitulo 5 (el cronista se despide), todos cerca de la sala: no hace falta mover al jugador. La
     * tarima del trono, el arbol en flor, la copa encendida en la tormenta, la llave de hueso, el cielo y, al final,
     * el propio jugador (por encima de su hombro, y la camara se eleva). eye y yaw son los del jugador.
     */
    public static List<Shot> chapter5(SalaArbol s, ServerLevel sl, Vec3 eye, float yaw) {
        float t0 = Timeline.NARR_START - 0.6f, back = Timeline.back(5);
        float c1 = Timeline.cut(5, 0), c2 = Timeline.cut(5, 1), c3 = Timeline.cut(5, 2), c4 = Timeline.cut(5, 3), c5 = Timeline.cut(5, 4);
        double cx = s.ccx(), cz = s.ccz(), F = s.F();
        List<Shot> l = new ArrayList<>();
        // 1) "Ser Gavran ha caido": la tarima del trono desde el noreste, la camara baja despacio
        l.add(new Shot(t0, c1, 60, 0, 1,
                cat(en(s, -48, 28.5, F + 11.5), en(s, -56, 27, F + 9.5), en(s, -63, 26, F + 8)),
                p(cx, F + 5.5, cz - 35, cx, F + 5, cz - 35, cx, F + 4.5, cz - 34.5)));
        // 2) "Gracias...": el arbol en flor desde la altura del balcon, con los petalos cayendo
        l.add(new Shot(c1, c2, 64, 0, 1,
                cat(en(s, 197, 29, F + 18), en(s, 207, 29, F + 17), en(s, 217, 29, F + 16)),
                cat(en(s, 200, 14, F + 25), en(s, 207, 14, F + 24), en(s, 214, 14, F + 23))));
        // 3) "La Sombra no vive aqui": fuera, la camara se aleja de la sala y descubre la copa encendida en la tormenta
        l.add(libre(sl, 60, 24, a -> new Shot(c2, c3, 66, 0, 1,
                cat(en(s, a, 56, F + 24), en(s, a + 6, 67, F + 32), en(s, a + 12, 78, F + 40)),
                p(cx, F + 36, cz, cx, F + 46, cz, cx, F + 54, cz))));
        // 4) "una llave de hueso": la llave flota y gira sobre el cofre
        Vec3 k = VictoriaGavran.llavePos(s);
        l.add(new Shot(c3, c4, 50, 0, 1,
                p(k.x + 2.9, k.y - 0.35, k.z + 1.5, k.x + 2.6, k.y - 0.2, k.z + 2.1, k.x + 2.2, k.y - 0.1, k.z + 2.6),
                p(k.x, k.y - 0.1, k.z, k.x, k.y - 0.1, k.z, k.x, k.y - 0.05, k.z)));
        // 5) "fortalezas mas alla de las nubes": sube por fuera de la copa hasta mas alto que la aguja y mira al cielo
        l.add(libre(sl, 235, 22, a -> {
            // al final, casi horizontal: la punta de la aguja y la copa abajo, las estrellas arriba
            Vec3 lejos = s.enAnillo(Math.toRadians(a + 20 + 180), 90, F + 128);
            return new Shot(c4, c5, 68, 0, 1,
                    cat(en(s, a, 58, F + 66), en(s, a + 10, 48, F + 92), en(s, a + 20, 38, F + 120)),
                    p(cx, F + 64, cz, cx, F + 92, cz, lejos.x, lejos.y, lejos.z));
        }));
        // 6) "Busca. Lucha. Cae... y vuelve a levantarte": el jugador, por encima de su hombro; la camara se eleva
        l.add(jugador(sl, eye, yaw, c5, back + 0.05f));
        return l;
    }

    /** Si en (x, y, z) puede ir la camara (aire o luz; lo que no este cargado no se mira). */
    private static boolean aire(ServerLevel sl, double x, double y, double z) {
        BlockPos b = BlockPos.containing(x, y, z);
        if (!sl.isLoaded(b)) return true;
        BlockState st = sl.getBlockState(b);
        return st.isAir() || st.is(Blocks.LIGHT) || st.is(Blocks.BARRIER);
    }

    /** Si la camara del plano recorre solo aire (con un bloque de margen alrededor). */
    private static boolean camaraLibre(ServerLevel sl, Shot sh) {
        for (int i = 0; i <= 20; i++) {
            double[] a = sh.at(sh.t0 + (sh.t1 - sh.t0) * i / 20.0);
            if (!aire(sl, a[0], a[1], a[2])) return false;
            for (int k = 0; k < 6; k++) {
                double dx = k == 0 ? 0.9 : k == 1 ? -0.9 : 0, dy = k == 2 ? 0.9 : k == 3 ? -0.9 : 0, dz = k == 4 ? 0.9 : k == 5 ? -0.9 : 0;
                if (!aire(sl, a[0] + dx, a[1] + dy, a[2] + dz)) return false;
            }
        }
        return true;
    }

    /** El plano del angulo base o, si la camara chocaria con algo (ramas, hojas), el primero libre girando de 'paso' en 'paso' grados. */
    private static Shot libre(ServerLevel sl, double base, double paso, DoubleFunction<Shot> plano) {
        for (int i = 0; i < 15; i++) {
            double a = base + paso * ((i + 1) / 2) * (i % 2 == 0 ? 1 : -1);
            Shot sh = plano.apply(a);
            if (camaraLibre(sl, sh)) return sh;
        }
        return plano.apply(base);
    }

    /** Si entre a y b no hay nada solido (para que se vea al jugador). */
    private static boolean seVe(ServerLevel sl, double[] a, Vec3 b) {
        double dx = b.x - a[0], dy = b.y - a[1], dz = b.z - a[2], len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        for (double u = 0.5; u < len - 0.6; u += 0.4) {
            double x = a[0] + dx * u / len, y = a[1] + dy * u / len, z = a[2] + dz * u / len;
            BlockPos q = BlockPos.containing(x, y, z);
            if (sl.isLoaded(q) && !sl.getBlockState(q).getCollisionShape(sl, q).isEmpty()) return false;
        }
        return true;
    }

    /**
     * El jugador visto por encima del hombro (derecho o izquierdo, y mas cerca si no cabe); la camara se aleja y sube
     * mientras mira a lo que tiene delante. Si no cabe por ningun lado, desde arriba.
     */
    private static Shot jugador(ServerLevel sl, Vec3 eye, float yaw, float t0, float t1) {
        double yr = Math.toRadians(yaw);
        double lx = -Math.sin(yr), lz = Math.cos(yr), rx = -Math.cos(yr), rz = -Math.sin(yr);
        for (double esc : new double[]{1.0, 0.7, 0.45})
            for (int v : new int[]{1, -1}) {
                double[] pos = p(
                        eye.x - lx * 2.8 * esc + rx * 1.0 * v, eye.y + 0.45, eye.z - lz * 2.8 * esc + rz * 1.0 * v,
                        eye.x - lx * 5.5 * esc + rx * 2.2 * v, eye.y + 3.0 * esc, eye.z - lz * 5.5 * esc + rz * 2.2 * v,
                        eye.x - lx * 8.5 * esc + rx * 3.0 * v, eye.y + 6.5 * esc, eye.z - lz * 8.5 * esc + rz * 3.0 * v);
                double[] look = p(eye.x + lx * 6, eye.y - 0.4, eye.z + lz * 6, eye.x + lx * 4, eye.y - 0.8, eye.z + lz * 4,
                        eye.x + lx * 2, eye.y - 1.6, eye.z + lz * 2);
                Shot sh = new Shot(t0, t1, 62, 0, 1, pos, look);
                if (!camaraLibre(sl, sh)) continue;
                boolean ve = true;
                for (int i = 0; i <= 4 && ve; i++) ve = seVe(sl, sh.at(t0 + (t1 - t0) * i / 4.0), eye.add(0, -0.6, 0));
                if (ve) return sh;
            }
        return new Shot(t0, t1, 62, 0, 1, p(eye.x + 0.6, eye.y + 3.5, eye.z + 0.6, eye.x + 0.9, eye.y + 7.0, eye.z + 0.9),
                p(eye.x, eye.y - 1.0, eye.z, eye.x, eye.y - 1.4, eye.z));
    }

    /** Rayos de la cinematica del capitulo 1: {t, x, y, z} por rayo. */
    public static double[] bolts1(CastlePlan.Cine c, long seed) {
        RandomSource r = RandomSource.create(seed);
        float[][] lines = Timeline.lines();
        List<Double> o = new ArrayList<>();
        int Y = c.y0();
        // el pacto: un rayo en la aguja de un campanario de la catedral
        add(o, Timeline.NARR_START + lines[3][0] + 1.4, c.cathX() - 17, Y + 134, c.cathZS() - 7);
        // el caballero en la cima: rayo en la aguja del arbol
        add(o, Timeline.NARR_START + lines[7][0] + 2.0, c.treeX(), c.hallY() + com.darkcraft.gen.TreeTower.PUNTA, c.treeZ());
        // la tormenta: rayos por toda la isla
        float s = Timeline.cut(3);
        for (int i = 0; i < 7; i++) {
            double a = r.nextDouble() * Math.PI * 2, d = 40 + r.nextDouble() * 180;
            add(o, s + 0.8 + i * 1.3 + r.nextDouble() * 0.5, c.cx() + Math.cos(a) * d, Y + 1, c.cz() + Math.sin(a) * d);
        }
        double[] out = new double[o.size()];
        for (int i = 0; i < out.length; i++) out[i] = o.get(i);
        return out;
    }

    private static void add(List<Double> o, double t, double x, double y, double z) {
        o.add(t);
        o.add(x);
        o.add(y);
        o.add(z);
    }
}
