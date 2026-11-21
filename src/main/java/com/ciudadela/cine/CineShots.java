package com.ciudadela.cine;

import com.ciudadela.gen.CastlePlan;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Los planos de la cinematica del capitulo 1, mientras el narrador cuenta lo que paso en el reino:
 * la torre de ascenso, el pueblo y la catedral, la isla con sus cascadas, la torre-arbol y por
 * ultimo la tormenta vista desde muy alto.
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

    /** Rayos de la cinematica del capitulo 1: {t, x, y, z} por rayo. */
    public static double[] bolts1(CastlePlan.Cine c, long seed) {
        RandomSource r = RandomSource.create(seed);
        float[][] lines = Timeline.lines();
        List<Double> o = new ArrayList<>();
        int Y = c.y0();
        // el pacto: un rayo en la aguja de un campanario de la catedral
        add(o, Timeline.NARR_START + lines[3][0] + 1.4, c.cathX() - 17, Y + 134, c.cathZS() - 7);
        // el caballero en la cima: rayo en la aguja del arbol
        add(o, Timeline.NARR_START + lines[7][0] + 2.0, c.treeX(), c.hallY() + 88, c.treeZ());
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
