package com.ciudadela.gen;

import java.util.ArrayList;
import java.util.List;

/**
 * Dibujo esquematico de la ciudadela para el mapa de pergamino: islas, murallas, torres, edificios,
 * catedral, el arbol, fuentes, jardines, puentes y cascadas. Todo relativo al centro y en enteros
 * para mandarlo en un paquete pequeno.
 */
public final class MapShapes {
    public static final int ISLAND = 1, SAT = 2, BRIDGE = 3, WALL = 4, TOWER = 5, BUILDING = 6, CATHEDRAL = 7,
            TREE_TOWER = 8, FOUNTAIN = 9, AREA = 10, ROAD = 11, TREE = 13, FALL = 14;

    public record Data(int[] shapes, String[] labels, int[] labelPos, int[] chapters) {}

    private MapShapes() {}

    public static Data build(CastlePlan p) {
        int cx = p.cx, cz = p.cz;
        List<Integer> o = new ArrayList<>();
        Island main = p.island();
        // isla principal: contorno
        int n = 96;
        o.add(ISLAND);
        o.add(n);
        for (int i = 0; i < n; i++) {
            double a = i * Math.PI * 2 / n;
            double r = main.radiusAt(a);
            o.add((int) Math.round(Math.cos(a) * r));
            o.add((int) Math.round(Math.sin(a) * r));
        }
        // cascadas
        for (double deg : new double[]{10, 125, 200, 290}) {
            double a = Math.toRadians(deg), r = main.radiusAt(a);
            o.add(FALL);
            o.add((int) Math.round(Math.cos(a) * r));
            o.add((int) Math.round(Math.sin(a) * r));
        }
        // murallas: octogono exterior y cuadrado interior
        o.add(WALL);
        o.add(8);
        double vr = CastlePlan.OCT / Math.cos(Math.PI / 8);
        for (int k = 0; k < 8; k++) {
            double a = Math.PI / 8 + k * Math.PI / 4;
            o.add((int) Math.round(Math.cos(a) * vr));
            o.add((int) Math.round(Math.sin(a) * vr));
        }
        int I = CastlePlan.INNER;
        o.add(WALL);
        o.add(4);
        for (int[] c : new int[][]{{-I, -I}, {I, -I}, {I, I}, {-I, I}}) {
            o.add(c[0]);
            o.add(c[1]);
        }
        // camino principal
        o.add(ROAD);
        o.add(0); o.add(26); o.add(0); o.add((int) Math.round(main.radiusAt(Math.PI / 2)) - 34); o.add(5);
        int treeCount = 0;
        for (Element e : p.elements()) {
            if (e instanceof Island isl && isl != main) {
                o.add(SAT); o.add(isl.cx - cx); o.add(isl.cz - cz); o.add((int) Math.round(isl.rimAngleRadius(0)));
            } else if (e instanceof Landscape.Bridge b) {
                int[] q = b.ends();
                o.add(BRIDGE); o.add(q[0] - cx); o.add(q[1] - cz); o.add(q[2] - cx); o.add(q[3] - cz);
            } else if (e instanceof TreeTower t) {
                o.add(TREE_TOWER); o.add(t.centerX() - cx); o.add(t.centerZ() - cz); o.add(TreeTower.HALL_R);
            } else if (e instanceof RoundTower t) {
                o.add(TOWER); o.add(t.cx - cx); o.add(t.cz - cz); o.add(t.radius);
            } else if (e instanceof Cathedral c) {
                int[] a = c.axis();
                o.add(CATHEDRAL); o.add(a[0] - cx); o.add(a[1] - cz); o.add(a[2] - cz);
            } else if (e instanceof Building b) {
                int kind = b.fp.w() >= 60 ? 2 : (b.fp.area() > 500 ? 1 : 0);
                o.add(BUILDING); o.add(b.fp.x0 - cx); o.add(b.fp.z0 - cz); o.add(b.fp.x1 - cx); o.add(b.fp.z1 - cz); o.add(kind);
            } else if (e instanceof Landscape.Fountain f) {
                o.add(FOUNTAIN); o.add((f.x0 + f.x1) / 2 - cx); o.add((f.z0 + f.z1) / 2 - cz); o.add(Math.max(1, (f.x1 - f.x0) / 2 - 1));
            } else if (e instanceof Landscape.Cemetery || e instanceof Landscape.HedgeMaze || e instanceof Landscape.TrainingYard
                    || e instanceof Town.Garden) {
                int kind = e instanceof Landscape.Cemetery ? 1 : e instanceof Landscape.HedgeMaze ? 2 : e instanceof Landscape.TrainingYard ? 3 : 4;
                o.add(AREA); o.add(e.x0 - cx); o.add(e.z0 - cz); o.add(e.x1 - cx); o.add(e.z1 - cz); o.add(kind);
            } else if (e instanceof Tree t && (treeCount++ % 3 == 0)) {
                o.add(TREE); o.add((t.x0 + t.x1) / 2 - cx); o.add((t.z0 + t.z1) / 2 - cz);
            }
        }
        int[] shapes = o.stream().mapToInt(Integer::intValue).toArray();
        // nombres de los lugares
        CastlePlan.Cine ci = p.cine();
        Object[][] lab = {
                {"torre_ascenso", ci.ascX() - cx, ci.ascZ() - cz + 16},
                {"puerta_exterior", 0, CastlePlan.OCT + 18},
                {"puerta_interior", 0, CastlePlan.INNER + 22},
                {"fuente", 0, 101},
                {"torreon", 0, 34},
                {"arbol", 0, -15 - TreeTower.HALL_R - 6},
                {"catedral", 92, 0},
                {"biblioteca", -93, -30},
                {"cementerio", -177, 70},
                {"laberinto", 79, -140},
                {"patio_armas", 101, 186},
                {"alquimista", -90, -122},
                {"observatorio", 30, -118},
                {"jardin", -12, -80},
                {"pueblo", -175, 178},
        };
        String[] labels = new String[lab.length];
        int[] pos = new int[lab.length * 2];
        for (int i = 0; i < lab.length; i++) {
            labels[i] = (String) lab[i][0];
            pos[i * 2] = (Integer) lab[i][1];
            pos[i * 2 + 1] = (Integer) lab[i][2];
        }
        int[] ch = new int[10];
        for (int c = 1; c <= 5; c++) {
            int[] s = p.storySpot(c);
            ch[(c - 1) * 2] = s == null ? 0 : s[0] - cx;
            ch[(c - 1) * 2 + 1] = s == null ? 0 : s[2] - cz;
        }
        return new Data(shapes, labels, pos, ch);
    }
}
