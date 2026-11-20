package com.ciudadela.gen;

import com.ciudadela.CiudadelaMod;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;

import java.util.*;
import java.util.function.IntBinaryOperator;

/**
 * Plano completo de una ciudadela. Se calcula una vez (es barato: solo describe las piezas) y luego
 * cada chunk construye la parte que le toca.
 */
public final class CastlePlan {
    public static final int Y0 = 380;            // nivel del suelo de la isla (se pisa aqui)
    public static final int OCT = 217;            // apotema del octogono de la muralla exterior
    public static final int INNER = 130;          // semilado de la muralla interior
    public static final int FOOTPRINT = 400;      // radio maximo ocupado (800 x 800)

    public final int cx, cz;
    private final List<Element> elements = new ArrayList<>();
    private final Map<Long, List<Element>> index = new HashMap<>();
    private final List<Rect> occupied = new ArrayList<>();
    private Island main;
    private final Set<String> warned = Collections.synchronizedSet(new HashSet<>());

    private CastlePlan(int cx, int cz) {
        this.cx = cx;
        this.cz = cz;
    }

    private static long key(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) ^ (chunkZ & 0xFFFFFFFFL);
    }

    private <T extends Element> T add(T e) {
        elements.add(e);
        return e;
    }

    private void finish() {
        for (Element e : elements) {
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++)
                    index.computeIfAbsent(key(x, z), k -> new ArrayList<>()).add(e);
        }
    }

    public boolean touches(int chunkX, int chunkZ) {
        return index.containsKey(key(chunkX, chunkZ));
    }

    public void buildChunk(WorldGenLevel level, int chunkX, int chunkZ, RandomSource random) {
        List<Element> list = index.get(key(chunkX, chunkZ));
        if (list == null) return;
        Canvas c = new Canvas(level, chunkX, chunkZ, random);
        for (Element e : list) {
            try {
                e.build(c);
            } catch (Throwable t) {
                String k = e.getClass().getSimpleName();
                if (warned.add(k)) CiudadelaMod.LOGGER.error("Error construyendo {} en chunk {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public int elementCount() {
        return elements.size();
    }

    // ===================================================================== diseno

    public static CastlePlan create(long seed, int cx, int cz, IntBinaryOperator groundHeight) {
        CastlePlan p = new CastlePlan(cx, cz);
        p.design(seed, groundHeight);
        p.finish();
        return p;
    }

    private int X(int lx) {
        return cx + lx;
    }

    private int Z(int lz) {
        return cz + lz;
    }

    private Rect R(int ax, int az, int bx, int bz) {
        return new Rect(cx + ax, cz + az, cx + bx, cz + bz);
    }

    private static int cheb(int lx, int lz) {
        return Math.max(Math.abs(lx), Math.abs(lz));
    }

    private static boolean insideOct(int lx, int lz, double margin) {
        return Math.abs(lx) <= OCT - margin && Math.abs(lz) <= OCT - margin && Math.abs(lx) + Math.abs(lz) <= OCT * Math.sqrt(2) - margin * 1.42;
    }

    private boolean onRoad(int lx, int lz) {
        if (Math.abs(lx) <= 4 && lz >= INNER - 4) return true;                      // camino principal al sur
        int ch = cheb(lx, lz);
        if (ch >= 138 && ch <= 142 && insideOct(lx, lz, 6)) return true;          // ronda interior
        return false;
    }

    private boolean gardenBed(int lx, int lz) {
        int ax = Math.abs(lx);
        return ax >= 16 && ax <= 56 && lz >= 40 && lz <= 112 && !(lz >= 70 && lz <= 86);
    }

    private void design(long seed, IntBinaryOperator groundHeight) {
        Rng rng = new Rng(seed);
        final int Y = Y0;

        // ------------------------------------------------ isla principal
        main = new Island(rng.fork(), cx, cz, Y - 1, 305, 0.06, 28, 6, 112);
        double[] falls = {10, 125, 200, 290};
        for (double a : falls) main.waterfall(Math.toRadians(a), 2.5);
        main.surface((x, z, top) -> {
            int lx = x - cx, lz = z - cz;
            if (top != Y - 1) return null;
            if (cheb(lx, lz) <= INNER - 4) {
                if (gardenBed(lx, lz)) {
                    int pp = Hash.pct(x, top, z, 71);
                    return pp < 70 ? Pal.GRASS_BLOCK : pp < 85 ? Pal.MOSS : Pal.PODZOL;
                }
                return Pal.paving(x, top, z);
            }
            if (onRoad(lx, lz)) return Landscape.road(x, top, z);
            return null;
        }).plants((x, z) -> {
            int lx = x - cx, lz = z - cz;
            if (cheb(lx, lz) <= INNER - 4 && !gardenBed(lx, lz)) return false;
            return !onRoad(lx, lz);
        });
        add(main);

        // ------------------------------------------------ islas satelite y puentes
        double[] satAngles = {35, 65, 150, 175, 230, 260, 315, 345};
        for (int i = 0; i < satAngles.length; i++) {
            double a = Math.toRadians(satAngles[i]);
            int rs = rng.range(19, 25);
            double Rm = main.radiusAt(a);
            double dist = Math.min(Rm + 26 + rs, FOOTPRINT - rs - 6);
            int sx = cx + (int) Math.round(Math.cos(a) * dist), sz = cz + (int) Math.round(Math.sin(a) * dist);
            int sy = Y - 1 + rng.range(-16, 10);
            Island sat = add(new Island(rng.fork(), sx, sz, sy, rs, 0.12, 6, 2, (int) (rs * 1.7)));
            satellite(i, sat, rng);
            // puente
            int bsx = sx - (int) Math.round(Math.cos(a) * (rs - 5)), bsz = sz - (int) Math.round(Math.sin(a) * (rs - 5));
            int ex = cx + (int) Math.round(Math.cos(a) * (Rm - 34)), ez = cz + (int) Math.round(Math.sin(a) * (Rm - 34));
            add(new Landscape.Bridge(rng.fork(), bsx, sy, bsz, ex, main.topAt(ex, ez), ez, 4 + rng.nextInt(4), 3));
        }

        // ------------------------------------------------ paisaje del recinto exterior
        add(new Landscape.Cemetery(rng.fork(), R(-205, -60, -150, 60), Y));
        add(new Landscape.HedgeMaze(rng.fork(), R(58, -196, 100, -146), Y));
        add(new Landscape.TrainingYard(rng.fork(), R(84, 146, 118, 176), Y));
        occupied.add(R(-207, -62, -148, 62));
        occupied.add(R(56, -198, 102, -144));
        occupied.add(R(82, 144, 120, 178));

        // patio interior
        add(new Landscape.Fountain(rng.fork(), X(0), Y, Z(78), 9));
        for (int lz : new int[]{44, 62, 98, 116}) {
            add(new Landscape.Statue(rng.fork(), X(-14), Y, Z(lz), Direction.EAST));
            add(new Landscape.Statue(rng.fork(), X(14), Y, Z(lz), Direction.WEST));
        }
        for (int lz = 36; lz <= 124; lz += 9) {
            add(new Landscape.LampPost(rng.fork(), X(-11), Y, Z(lz)));
            add(new Landscape.LampPost(rng.fork(), X(11), Y, Z(lz)));
        }
        for (int lz = 146; lz <= 268; lz += 10) {
            if (lz > 200 && lz < 236) continue;
            add(new Landscape.LampPost(rng.fork(), X(-6), Y, Z(lz)));
            add(new Landscape.LampPost(rng.fork(), X(6), Y, Z(lz)));
        }
        // jardin privado al norte del torreon
        add(new Landscape.Fountain(rng.fork(), X(-12), Y, Z(-96), 6));
        add(new Landscape.Statue(rng.fork(), X(-24), Y, Z(-96), Direction.EAST));
        add(new Landscape.Statue(rng.fork(), X(0), Y, Z(-96), Direction.WEST));

        // ------------------------------------------------ edificios del recinto exterior
        Room.Type[] none = {};
        building(new Building(rng.fork(), R(30, 150, 72, 172), Y, new int[]{8, 8}, 0, 6, Building.Roof.GABLE, 1,
                Building.simple(6, 14, none, new Room.Type[]{Room.Type.BARRACKS, Room.Type.DINING, Room.Type.ARMORY},
                        new Room.Type[]{Room.Type.BARRACKS, Room.Type.STORAGE}), Direction.NORTH).turret(1));
        building(new Building(rng.fork(), R(-72, 150, -40, 170), Y, new int[]{7, 7}, 0, 6, Building.Roof.GABLE, 2,
                Building.simple(6, 12, none, new Room.Type[]{Room.Type.KITCHEN, Room.Type.SERVANTS, Room.Type.STORAGE},
                        new Room.Type[]{Room.Type.SERVANTS, Room.Type.BEDROOM}), Direction.NORTH).turret(2));
        building(new Building(rng.fork(), R(-120, 144, -86, 164), Y, new int[]{10}, 1, 6, Building.Roof.GABLE, 1,
                Building.simple(7, 16, new Room.Type[]{Room.Type.CELLAR, Room.Type.WINE_CELLAR},
                        new Room.Type[]{Room.Type.KITCHEN, Room.Type.DINING, Room.Type.KITCHEN, Room.Type.STORAGE}), Direction.NORTH).turret(3));
        building(new Building(rng.fork(), R(-44, -192, -12, -168), Y, new int[]{9}, 2, 6, Building.Roof.GABLE, 1,
                Building.simple(7, 15, new Room.Type[]{Room.Type.WINE_CELLAR, Room.Type.WINE_CELLAR, Room.Type.CELLAR},
                        new Room.Type[]{Room.Type.DINING, Room.Type.STORAGE}), Direction.SOUTH).turret(0));
        building(new Building(rng.fork(), R(12, -194, 46, -170), Y, new int[]{9}, 1, 6, Building.Roof.HIP, 1,
                Building.simple(8, 18, new Room.Type[]{Room.Type.STORAGE, Room.Type.CELLAR},
                        new Room.Type[]{Room.Type.BATH, Room.Type.BATH}), Direction.SOUTH).turret(1));
        building(new Building(rng.fork(), R(150, -44, 172, 6), Y, new int[]{8}, 0, 6, Building.Roof.GABLE, 2,
                Building.simple(7, 16, none, new Room.Type[]{Room.Type.STABLE, Room.Type.SMITHY, Room.Type.STABLE}), Direction.WEST).turret(-1));
        building(new Building(rng.fork(), R(150, 24, 174, 54), Y, new int[]{8, 7}, 0, 6, Building.Roof.GABLE, 1,
                Building.simple(7, 14, none, new Room.Type[]{Room.Type.STORAGE, Room.Type.SMITHY},
                        new Room.Type[]{Room.Type.STORAGE, Room.Type.SERVANTS}), Direction.WEST).turret(3));
        // capilla del cementerio y mausoleos
        building(new Building(rng.fork(), R(-186, -16, -168, 14), Y, new int[]{12}, 1, 6, Building.Roof.GABLE, 2,
                Building.simple(8, 40, new Room.Type[]{Room.Type.CRYPT}, new Room.Type[]{Room.Type.CHAPEL}), Direction.EAST).turret(0));
        for (int lz : new int[]{-46, 34}) {
            building(new Building(rng.fork(), R(-203, lz, -194, lz + 9), Y, new int[]{7}, 0, 6, Building.Roof.HIP, 1,
                    Building.simple(3, 30, none, new Room.Type[]{Room.Type.CRYPT}), Direction.EAST).turret(-1).noTimber());
        }

        // ------------------------------------------------ muralla exterior (octogono)
        int wallH = 26, galY = Y + wallH / 2 - 2;
        int[][] verts = new int[8][2];
        double vr = OCT / Math.cos(Math.PI / 8);
        for (int k = 0; k < 8; k++) {
            double a = Math.PI / 8 + k * Math.PI / 4;
            verts[k][0] = (int) Math.round(Math.cos(a) * vr);
            verts[k][1] = (int) Math.round(Math.sin(a) * vr);
        }
        for (int k = 0; k < 8; k++) {
            int[] a = verts[k], b = verts[(k + 1) % 8];
            add(new CurtainWall(rng.fork(), X(a[0]), Z(a[1]), X(b[0]), Z(b[1]), Y, wallH, 7, X(a[0] + b[0]), Z(a[1] + b[1])));
        }
        // gatehouse sur (lado k=1 entre vertices 1 y 2 es el lado sur, mirando a +z)
        add(new Gatehouse(rng.fork(), X(0), Z(OCT), Y, 16, 11, 14, new int[]{9, 9}));
        for (int s = -1; s <= 1; s += 2) {
            RoundTower t = new RoundTower(rng.fork(), X(21 * s), Z(OCT + 5), Y, 9, 48, RoundTower.Top.PLATFORM);
            t.doorToward(X(0), Z(OCT + 5), Y + 14).doorToward(X(60 * s), Z(OCT), Y + wallH).doorToward(X(60 * s), Z(OCT), galY)
                    .doorToward(X(21 * s), Z(0), Y);
            add(t);
        }
        for (int k = 0; k < 8; k++) {
            int[] v = verts[k], prev = verts[(k + 7) % 8], next = verts[(k + 1) % 8];
            RoundTower t = new RoundTower(rng.fork(), X(v[0]), Z(v[1]), Y, 11, 54, (k % 2 == 0) ? RoundTower.Top.CONE : RoundTower.Top.PLATFORM);
            t.doorToward(X(prev[0]), Z(prev[1]), Y + wallH).doorToward(X(next[0]), Z(next[1]), Y + wallH)
                    .doorToward(X(prev[0]), Z(prev[1]), galY).doorToward(X(next[0]), Z(next[1]), galY)
                    .doorToward(X(0), Z(0), Y).rooms(Room.Type.ARMORY);
            add(t);
        }
        for (int k = 0; k < 8; k++) {
            if (k == 2) continue; // la puerta
            double a = k * Math.PI / 4;
            int mx = (int) Math.round(Math.cos(a) * OCT), mz = (int) Math.round(Math.sin(a) * OCT);
            double ta = a + Math.PI / 2;
            int tx = (int) Math.round(Math.cos(ta) * 50), tz = (int) Math.round(Math.sin(ta) * 50);
            RoundTower t = new RoundTower(rng.fork(), X(mx), Z(mz), Y, 8, 44, RoundTower.Top.CONE);
            t.doorToward(X(mx + tx), Z(mz + tz), Y + wallH).doorToward(X(mx - tx), Z(mz - tz), Y + wallH)
                    .doorToward(X(mx + tx), Z(mz + tz), galY).doorToward(X(mx - tx), Z(mz - tz), galY)
                    .doorToward(X(0), Z(0), Y);
            add(t);
        }

        // ------------------------------------------------ muralla interior (cuadrada)
        int iwH = 36, igal = Y + iwH / 2 - 2;
        int I = INNER;
        int[][] corners = {{-I, -I}, {I, -I}, {I, I}, {-I, I}};
        for (int k = 0; k < 4; k++) {
            int[] a = corners[k], b = corners[(k + 1) % 4];
            add(new CurtainWall(rng.fork(), X(a[0]), Z(a[1]), X(b[0]), Z(b[1]), Y, iwH, 9, X((a[0] + b[0]) * 2), Z((a[1] + b[1]) * 2)));
        }
        add(new Gatehouse(rng.fork(), X(0), Z(I), Y, 15, 12, 16, new int[]{10, 10}));
        for (int s = -1; s <= 1; s += 2) {
            RoundTower t = new RoundTower(rng.fork(), X(20 * s), Z(I + 6), Y, 8, 60, RoundTower.Top.CONE);
            t.doorToward(X(0), Z(I + 6), Y + 16).doorToward(X(60 * s), Z(I), Y + iwH).doorToward(X(60 * s), Z(I), igal)
                    .doorToward(X(20 * s), Z(I + 40), Y);
            add(t);
        }
        for (int k = 0; k < 4; k++) {
            int[] v = corners[k], prev = corners[(k + 3) % 4], next = corners[(k + 1) % 4];
            RoundTower t = new RoundTower(rng.fork(), X(v[0]), Z(v[1]), Y, 13, 70, RoundTower.Top.CONE).slope(2.8);
            t.doorToward(X(prev[0]), Z(prev[1]), Y + iwH).doorToward(X(next[0]), Z(next[1]), Y + iwH)
                    .doorToward(X(prev[0]), Z(prev[1]), igal).doorToward(X(next[0]), Z(next[1]), igal)
                    .doorToward(X(0), Z(0), Y).doorToward(X(v[0] * 2), Z(v[1] * 2), Y).rooms(Room.Type.ARMORY);
            add(t);
        }
        int[][] mids = {{I, 0}, {-I, 0}, {0, -I}};
        for (int[] m : mids) {
            int tx = m[0] == 0 ? 50 : 0, tz = m[1] == 0 ? 50 : 0;
            RoundTower t = new RoundTower(rng.fork(), X(m[0]), Z(m[1]), Y, 9, 54, RoundTower.Top.PLATFORM);
            t.doorToward(X(m[0] + tx), Z(m[1] + tz), Y + iwH).doorToward(X(m[0] - tx), Z(m[1] - tz), Y + iwH)
                    .doorToward(X(m[0] + tx), Z(m[1] + tz), igal).doorToward(X(m[0] - tx), Z(m[1] - tz), igal)
                    .doorToward(X(0), Z(0), Y).doorToward(X(m[0] * 2), Z(m[1] * 2), Y);
            add(t);
        }

        // ------------------------------------------------ torreon (keep)
        Building.Planner keepPlanner = (f, r) -> {
            Rect in = f.interior;
            if (f.index == 0) {
                f.add(new Rect(cx - 30, in.z0, cx + 30, cz - 21), Room.Type.THRONE);
                f.add(new Rect(cx - 30, cz - 19, cx + 30, in.z1), Room.Type.GREAT_HALL);
                f.add(new Rect(in.x0, in.z0, cx - 32, cz - 12), Room.Type.DINING);
                f.add(new Rect(in.x0, cz - 10, cx - 32, in.z1), Room.Type.KITCHEN);
                f.add(new Rect(cx + 32, in.z0, in.x1, cz - 5), Room.Type.BALLROOM);
                f.add(new Rect(cx + 32, cz - 3, in.x1, in.z1), Room.Type.ARMORY);
                return;
            }
            Room.Type[] t;
            if (f.index == -2) t = new Room.Type[]{Room.Type.PRISON, Room.Type.CRYPT, Room.Type.TREASURY, Room.Type.PRISON, Room.Type.CRYPT};
            else if (f.index == -1) t = new Room.Type[]{Room.Type.CELLAR, Room.Type.WINE_CELLAR, Room.Type.STORAGE, Room.Type.WINE_CELLAR};
            else if (f.index == 1) t = new Room.Type[]{Room.Type.LIBRARY, Room.Type.STUDY, Room.Type.GALLERY, Room.Type.CHAPEL, Room.Type.DINING, Room.Type.BATH};
            else if (f.index == 2) t = new Room.Type[]{Room.Type.BEDROOM, Room.Type.BEDROOM, Room.Type.BATH, Room.Type.STUDY, Room.Type.GALLERY};
            else if (f.index == 3) t = new Room.Type[]{Room.Type.BEDROOM, Room.Type.BEDROOM, Room.Type.BATH, Room.Type.STORAGE, Room.Type.ALCHEMY};
            else t = new Room.Type[]{Room.Type.SERVANTS, Room.Type.STORAGE, Room.Type.BEDROOM, Room.Type.TREASURY, Room.Type.GALLERY};
            int i = r.nextInt(t.length);
            for (Rect rr : Building.bsp(in, 7, 18, r)) f.add(rr, t[i++ % t.length]);
        };
        Building keep = new Building(rng.fork(), R(-50, -55, 50, 25), Y, new int[]{14, 8, 8, 8, 8}, 2, 7, Building.Roof.TERRACE, 1,
                keepPlanner, Direction.SOUTH).turret(0).noTimber();
        int[][] kc = {{50, -55}, {50, 25}, {-50, 25}};
        for (int[] k : kc) keep.reserve(X(k[0]), Z(k[1]), 11.5);
        building(keep);
        for (int[] k : kc) {
            RoundTower t = new RoundTower(rng.fork(), X(k[0]), Z(k[1]), Y, 9, 82, RoundTower.Top.CONE).slope(2.6);
            for (int fy : keep.floorLevels()) t.doorToward(X(-15), Z(-15), fy);
            t.doorToward(X(0), Z(-15), keep.topY + 1);
            add(t);
        }
        add(new GreatTower(rng.fork(), X(0), Z(-15), keep.topY + 1, 13, Y + 200, 92, false).door(Direction.SOUTH, keep.topY + 1));
        occupied.add(R(-62, -67, 62, 37));

        // ------------------------------------------------ catedral
        add(new Cathedral(rng.fork(), X(92), Z(-80), Z(80), Y));
        occupied.add(R(64, -95, 120, 84));

        // ------------------------------------------------ biblioteca con atrio
        Building.Planner libPlanner = (f, r) -> {
            Rect in = f.interior;
            if (f.index == 0) {
                f.add(in, Room.Type.LIBRARY_GRAND);
                return;
            }
            Rect v = in.grow(-9);
            f.voidRect = v;
            Room n = f.add(new Rect(in.x0, in.z0, in.x1, v.z0 - 1), Room.Type.LIBRARY);
            n.open = 2;
            Room s = f.add(new Rect(in.x0, v.z1 + 1, in.x1, in.z1), Room.Type.LIBRARY);
            s.open = 1;
            Room w = f.add(new Rect(in.x0, v.z0, v.x0 - 1, v.z1), Room.Type.LIBRARY);
            w.open = 1 | 2 | 8;
            Room e = f.add(new Rect(v.x1 + 1, v.z0, in.x1, v.z1), Room.Type.LIBRARY);
            e.open = 1 | 2 | 4;
        };
        building(new Building(rng.fork(), R(-118, -70, -68, 10), Y, new int[]{10, 9, 9, 9}, 0, 6, Building.Roof.GABLE, 1,
                libPlanner, Direction.EAST, Direction.SOUTH).turret(1).noTimber());

        // ------------------------------------------------ torres del alquimista y observatorio
        add(new RoundTower(rng.fork(), X(-90), Z(-100), Y, 12, 104, RoundTower.Top.CONE).slope(2.5)
                .doorToward(X(0), Z(0), Y).rooms(Room.Type.ALCHEMY));
        add(new RoundTower(rng.fork(), X(30), Z(-100), Y, 10, 88, RoundTower.Top.PLATFORM)
                .doorToward(X(0), Z(0), Y).rooms(Room.Type.OBSERVATORY));
        occupied.add(R(-104, -114, -76, -86));
        occupied.add(R(18, -112, 42, -88));

        // ------------------------------------------------ arboles muertos
        int trees = 0;
        for (int tries = 0; tries < 4000 && trees < 320; tries++) {
            double a = rng.nextDouble() * Math.PI * 2;
            double rr = Math.sqrt(rng.nextDouble()) * 300;
            int lx = (int) Math.round(Math.cos(a) * rr), lz = (int) Math.round(Math.sin(a) * rr);
            if (!treeSpot(lx, lz)) continue;
            int top = main.topAt(X(lx), Z(lz));
            if (top == Integer.MIN_VALUE) continue;
            boolean thick = rng.chance(0.25);
            add(new Landscape.DeadTree(rng.fork(), X(lx), top + 1, Z(lz), thick));
            occupied.add(R(lx - 3, lz - 3, lx + 3, lz + 3));
            trees++;
        }

        // ------------------------------------------------ torre de ascenso desde el suelo
        double asc = Math.toRadians(90);
        double Ra = main.radiusAt(asc);
        int ax = cx + (int) Math.round(Math.cos(asc) * (Ra + 13)), az = cz + (int) Math.round(Math.sin(asc) * (Ra + 13));
        int ground = groundHeight.applyAsInt(ax, az);
        if (ground < Y - 60) {
            RoundTower t = new RoundTower(rng.fork(), ax, az, ground, 7, (Y - 1) - ground, RoundTower.Top.PLATFORM);
            t.doorToward(ax, az + 50, ground);
            add(t);
            int ex = cx + (int) Math.round(Math.cos(asc) * (Ra - 34)), ez = cz + (int) Math.round(Math.sin(asc) * (Ra - 34));
            add(new Landscape.Bridge(rng.fork(), ax, Y - 1, az - 4, ex, Y - 1, ez, 0, 3));
        }
    }

    private boolean treeSpot(int lx, int lz) {
        int ch = cheb(lx, lz);
        double r = Math.sqrt(lx * lx + lz * lz);
        if (ch <= INNER + 14) {
            // dentro del recinto interior solo en los parterres del patio
            return gardenBed(lx, lz) && (Math.abs(lx) - 16) % 12 > 3;
        }
        if (onRoad(lx, lz) || Math.abs(lx) <= 9 && lz > 0) return false;
        // lejos de la muralla exterior
        boolean inOct = insideOct(lx, lz, 16);
        boolean outOct = !insideOct(lx, lz, -16);
        if (!inOct && !outOct) return false;
        if (outOct && r > 285) return false;
        for (Rect o : occupied) if (o.grow(2).contains(X(lx), Z(lz))) return false;
        return true;
    }

    private void building(Building b) {
        add(b);
        occupied.add(b.fp.grow(5));
    }

    // ------------------------------------------------ estructuras de las islas satelite
    private void satellite(int i, Island sat, Rng rng) {
        int sx = sat.cx, sz = sat.cz, gy = sat.y0 + 1;
        Direction toMain = Pal.dirOf(cx - sx, cz - sz);
        Room.Type[] none = {};
        switch (i % 8) {
            case 0 -> add(new RoundTower(rng.fork(), sx, sz, gy, 6, 34, RoundTower.Top.CONE).mossy().doorToward(cx, cz, gy));
            case 1 -> add(new Building(rng.fork(), new Rect(sx - 6, sz - 7, sx + 6, sz + 7), gy, new int[]{8}, 1, 6, Building.Roof.HIP, 1,
                    Building.simple(4, 30, new Room.Type[]{Room.Type.CRYPT}, new Room.Type[]{Room.Type.CHAPEL}), toMain).turret(2).noTimber());
            case 2 -> add(new Building(rng.fork(), new Rect(sx - 7, sz - 11, sx + 7, sz + 11), gy, new int[]{11}, 0, 6, Building.Roof.GABLE, 2,
                    Building.simple(5, 40, none, new Room.Type[]{Room.Type.CHAPEL}), toMain).turret(-1).mossy());
            case 3 -> add(new Building(rng.fork(), new Rect(sx - 9, sz - 11, sx + 9, sz + 11), gy, new int[]{8, 7}, 1, 6, Building.Roof.GABLE, 1,
                    Building.simple(6, 12, new Room.Type[]{Room.Type.WINE_CELLAR, Room.Type.CELLAR},
                            new Room.Type[]{Room.Type.DINING, Room.Type.KITCHEN, Room.Type.STUDY},
                            new Room.Type[]{Room.Type.BEDROOM, Room.Type.BEDROOM, Room.Type.BATH}), toMain).turret(2));
            case 4 -> {
                add(new Landscape.Cemetery(rng.fork(), new Rect(sx - 11, sz - 11, sx + 11, sz + 11), gy));
                add(new Landscape.Statue(rng.fork(), sx, gy, sz, toMain));
            }
            case 5 -> add(new RoundTower(rng.fork(), sx, sz, gy, 7, 28, RoundTower.Top.PLATFORM).doorToward(cx, cz, gy));
            case 6 -> add(new Building(rng.fork(), new Rect(sx - 6, sz - 6, sx + 6, sz + 6), gy, new int[]{8}, 0, 6, Building.Roof.HIP, 1,
                    Building.simple(4, 30, none, new Room.Type[]{Room.Type.ALCHEMY}), toMain).turret(-1));
            default -> {
                add(new Landscape.Fountain(rng.fork(), sx, gy, sz, 5));
                add(new Landscape.Statue(rng.fork(), sx + 8, gy, sz, Direction.WEST));
                add(new Landscape.Statue(rng.fork(), sx - 8, gy, sz, Direction.EAST));
                add(new Landscape.LampPost(rng.fork(), sx, gy, sz + 8));
                add(new Landscape.LampPost(rng.fork(), sx, gy, sz - 8));
            }
        }
        // unos arboles en el borde del islote
        int n = 2 + rng.nextInt(3);
        for (int k = 0; k < n; k++) {
            double a = rng.nextDouble() * Math.PI * 2;
            double d = sat.rimAngleRadius(a) - 5;
            int tx = sx + (int) Math.round(Math.cos(a) * d), tz = sz + (int) Math.round(Math.sin(a) * d);
            int top = sat.topAt(tx, tz);
            if (top == Integer.MIN_VALUE) continue;
            add(new Landscape.DeadTree(rng.fork(), tx, top + 1, tz, rng.chance(0.3)));
        }
    }

    /** Para las pruebas: describe el plano. */
    public String describe() {
        return "Ciudadela en " + cx + "," + cz + " con " + elements.size() + " piezas";
    }
}
