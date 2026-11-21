package com.ciudadela.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Edificio gotico generico: varias plantas (y sotanos), salas con puertas, ventanas ojivales con
 * vidrieras, contrafuertes, tejado y torreta de escalera de caracol en una esquina.
 */
public class Building extends Element {
    public enum Roof { GABLE, HIP, TERRACE, NONE }

    public interface Planner {
        void plan(Floor f, Rng rng);
    }

    public static final class Floor {
        public final int index;   // negativo = sotano
        public final int y;       // nivel que se pisa
        public final int h;       // altura total incluida la losa (aire = h-1)
        public final Rect interior;
        public final List<Room> rooms = new ArrayList<>();
        public Rect voidRect;     // hueco abierto (atrio) sin suelo
        final List<int[]> doors = new ArrayList<>(); // {x, z, alto}

        Floor(int index, int y, int h, Rect interior) {
            this.index = index;
            this.y = y;
            this.h = h;
            this.interior = interior;
        }

        public Room add(Rect r, Room.Type t) {
            Room room = new Room(r, t);
            room.floorY = y;
            room.height = h - 1;
            rooms.add(room);
            return room;
        }

        Room roomAt(int x, int z) {
            for (Room r : rooms) if (r.r.contains(x, z)) return r;
            return null;
        }
    }

    public final Rect fp;
    public final int baseY;
    public final int topY;
    protected final List<Floor> floors = new ArrayList<>();
    protected final Roof roof;
    protected final int roofSlope;
    protected boolean timber = true;
    protected boolean mossy = false;
    /** 0 pizarra, 1 roble oscuro, 2 abeto, 3 ladrillo del nether (rojo oscuro), 4 piedra negra. */
    protected int roofStyle = 0;
    protected int turretCorner = 0; // 0..3, -1 sin torreta
    protected final List<Direction> entrances = new ArrayList<>();
    private int ridgeTop;

    public Building(long seed, Rect fp, int baseY, int[] heights, int basements, int basementH, Roof roof, int slope,
                    Planner planner, Direction... entrances) {
        super(seed);
        this.fp = fp;
        this.baseY = baseY;
        this.roof = roof;
        this.roofSlope = slope;
        Rect in = fp.grow(-1);
        Rng rng = new Rng(seed);
        for (int k = basements; k >= 1; k--) floors.add(new Floor(-k, baseY - k * basementH, basementH, in));
        int y = baseY;
        for (int i = 0; i < heights.length; i++) {
            floors.add(new Floor(i, y, heights[i], in));
            y += heights[i];
        }
        this.topY = y - 1;
        for (Floor f : floors) planner.plan(f, new Rng(rng.fork()));
        for (Direction d : entrances) this.entrances.add(d);
        int span = Math.min(fp.w(), fp.d()) + 2;
        int ridge = switch (roof) {
            case GABLE -> topY + 1 + (span / 2) * slope;
            case HIP -> topY + 1 + (span / 2) * slope;
            case TERRACE -> topY + 3;
            default -> topY;
        };
        this.ridgeTop = ridge;
        int low = floors.get(0).y - 2;
        bounds(fp.x0 - 6, low, fp.z0 - 6, fp.x1 + 6, ridge + 8, fp.z1 + 6);
        computeDoors();
        int[] t = turretCenter();
        reserve(t[0], t[1], 6.5);
        prepareRooms();
    }

    /** Ventanas y atrios de cada sala (se calcula al hacer el plano: durante la construccion no se toca nada compartido). */
    private void prepareRooms() {
        for (Floor f : floors) {
            for (Room r : f.rooms) r.windows.clear();
            markWindows(f);
            markAtrium(f);
        }
    }

    public Building noTimber() {
        this.timber = false;
        return this;
    }

    public Building roofStyle(int st) {
        this.roofStyle = st;
        return this;
    }

    private BlockState roofBlock(int x, int y, int z) {
        return switch (roofStyle) {
            case 1 -> Pal.DO_PLANKS;
            case 2 -> Pal.SP_PLANKS;
            case 3 -> Hash.pct(x, y, z, 3) < 70 ? Pal.NETHER_BRICKS : Pal.RED_NETHER_BRICKS;
            case 4 -> Pal.PB_BRICKS;
            default -> Pal.roof(x, y, z);
        };
    }

    private Block roofStairs() {
        return switch (roofStyle) {
            case 1 -> Blocks.DARK_OAK_STAIRS;
            case 2 -> Blocks.SPRUCE_STAIRS;
            case 3 -> Blocks.NETHER_BRICK_STAIRS;
            case 4 -> Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS;
            default -> Blocks.DEEPSLATE_TILE_STAIRS;
        };
    }

    private Block roofSlab() {
        return switch (roofStyle) {
            case 1 -> Blocks.DARK_OAK_SLAB;
            case 2 -> Blocks.SPRUCE_SLAB;
            case 3 -> Blocks.NETHER_BRICK_SLAB;
            case 4 -> Blocks.POLISHED_BLACKSTONE_BRICK_SLAB;
            default -> Blocks.DEEPSLATE_TILE_SLAB;
        };
    }

    public Building mossy() {
        this.mossy = true;
        return this;
    }

    private final List<double[]> reserves = new ArrayList<>();
    private final List<double[]> noDoors = new ArrayList<>();

    /** Reserva un circulo libre de muebles (p.ej. donde se cruza con una torre). */
    public Building reserve(int x, int z, double rad) {
        reserves.add(new double[]{x, z, rad});
        applyReserve(x, z, rad);
        return this;
    }

    /** Ninguna puerta dentro de este circulo (una torre que atraviesa el edificio). */
    public Building noDoorsNear(int x, int z, double rad) {
        noDoors.add(new double[]{x, z, rad});
        redoDoors();
        return this;
    }

    private boolean doorBlocked(int x, int z) {
        for (double[] c : noDoors) {
            double dx = x - c[0], dz = z - c[1];
            if (dx * dx + dz * dz <= c[2] * c[2]) return true;
        }
        return nearTurret(x, z, 7.2);
    }

    /** Vuelve a colocar las puertas (al cambiar la torreta o los circulos sin puertas). */
    private void redoDoors() {
        for (Floor f : floors) {
            f.doors.clear();
            for (Room r : f.rooms) r.keepClear.clear();
        }
        computeDoors();
        for (double[] rs : reserves) applyReserve((int) rs[0], (int) rs[1], rs[2]);
        prepareRooms();
    }

    private void applyReserve(int x, int z, double rad) {
        for (Floor f : floors)
            for (Room r : f.rooms)
                for (int px = r.r.x0; px <= r.r.x1; px++)
                    for (int pz = r.r.z0; pz <= r.r.z1; pz++) {
                        double dx = px - x, dz = pz - z;
                        if (dx * dx + dz * dz <= rad * rad) r.keepClear.add(Room.key(px, pz));
                    }
    }

    /** {x, z, nivel mas bajo, nivel mas alto} de la torreta de escalera, o null si no tiene. */
    public int[] turretInfo() {
        if (turretCorner < 0) return null;
        int[] t = turretCenter();
        int lowest = floors.get(0).y;
        int lastY = roof == Roof.TERRACE ? topY + 1 : floors.get(floors.size() - 1).y;
        return new int[]{t[0], t[1], lowest, lastY};
    }

    public List<Integer> floorLevels() {
        List<Integer> l = new ArrayList<>();
        for (Floor f : floors) if (f.index >= 0) l.add(f.y);
        return l;
    }

    public Building turret(int corner) {
        this.turretCorner = corner;
        if (corner >= 0) {
            int[] t = turretCenter();
            reserves.add(new double[]{t[0], t[1], 6.5});
        }
        redoDoors();
        return this;
    }

    // ------------------------------------------------------------------ planificacion

    /** Particion BSP del interior en salas separadas por muros de 1 bloque. */
    public static List<Rect> bsp(Rect r, int min, int max, Rng rng) {
        List<Rect> out = new ArrayList<>();
        split(r, min, max, rng, out);
        return out;
    }

    private static void split(Rect r, int min, int max, Rng rng, List<Rect> out) {
        boolean canX = r.w() >= 2 * min + 1, canZ = r.d() >= 2 * min + 1;
        boolean must = r.w() > max || r.d() > max;
        if ((!canX && !canZ) || (!must && rng.chance(0.3))) {
            out.add(r);
            return;
        }
        boolean alongX = canX && (!canZ || r.w() > r.d() || (r.w() == r.d() && rng.chance(0.5)));
        if (alongX) {
            int p = rng.range(r.x0 + min, r.x1 - min);
            split(new Rect(r.x0, r.z0, p - 1, r.z1), min, max, rng, out);
            split(new Rect(p + 1, r.z0, r.x1, r.z1), min, max, rng, out);
        } else {
            int p = rng.range(r.z0 + min, r.z1 - min);
            split(new Rect(r.x0, r.z0, r.x1, p - 1), min, max, rng, out);
            split(new Rect(r.x0, p + 1, r.x1, r.z1), min, max, rng, out);
        }
    }

    /** Planificador simple: BSP y un tipo por sala elegido de la lista para cada planta. */
    public static Planner simple(int min, int max, Room.Type[] basement, Room.Type[]... perFloor) {
        return (f, rng) -> {
            Room.Type[] types = f.index < 0 ? basement : perFloor[Math.min(f.index, perFloor.length - 1)];
            if (types == null || types.length == 0) return;
            List<Rect> rects = bsp(f.interior, min, max, rng);
            int i = rng.nextInt(types.length);
            for (Rect r : rects) f.add(r, types[i++ % types.length]);
        };
    }

    private int[] turretCenter() {
        return switch (turretCorner) {
            case 0 -> new int[]{fp.x0, fp.z0, 1, 1};
            case 1 -> new int[]{fp.x1, fp.z0, -1, 1};
            case 2 -> new int[]{fp.x1, fp.z1, -1, -1};
            default -> new int[]{fp.x0, fp.z1, 1, -1};
        };
    }

    private void computeDoors() {
        for (Floor f : floors) {
            for (int i = 0; i < f.rooms.size(); i++)
                for (int j = 0; j < f.rooms.size(); j++) {
                    if (i == j) continue;
                    Rect a = f.rooms.get(i).r, b = f.rooms.get(j).r;
                    if (a.x1 + 2 == b.x0) {
                        int lo = Math.max(a.z0, b.z0), hi = Math.min(a.z1, b.z1);
                        if (hi - lo >= 1) doorAlong(f, a.x1 + 1, lo, hi, true);
                    }
                    if (a.z1 + 2 == b.z0) {
                        int lo = Math.max(a.x0, b.x0), hi = Math.min(a.x1, b.x1);
                        if (hi - lo >= 1) doorAlong(f, a.z1 + 1, lo, hi, false);
                    }
                }
        }
        // entradas exteriores en planta baja
        Floor ground = floorByIndex(0);
        if (ground != null) {
            for (Direction d : entrances) {
                int[] e = entrance(ground, d);
                if (e == null) continue;
                Room r = ground.roomAt(e[2], e[3]);
                if (r != null) {
                    r.markDoor(e[2], e[3]);
                    r.markDoor(e[2] + d.getClockWise().getStepX(), e[3] + d.getClockWise().getStepZ());
                    r.markDoor(e[2] - d.getClockWise().getStepX(), e[3] - d.getClockWise().getStepZ());
                    r.markApproach(e[2], e[3], -d.getStepX(), -d.getStepZ(), 4, entranceHalf() + 1);
                }
            }
        }
    }

    /** Puerta en el muro compartido [lo, hi]: lo mas centrada posible, pero nunca dentro de una torre. */
    private void doorAlong(Floor f, int fixed, int lo, int hi, boolean wallAlongZ) {
        int overlap = hi - lo + 1;
        int w = overlap >= 9 ? 3 : (overlap >= 4 ? 2 : 1);
        int mid = (lo + hi) / 2;
        for (int off = 0; off <= overlap; off++)
            for (int sgn = -1; sgn <= 1; sgn += 2) {
                int c = mid + off * sgn;
                boolean ok = true;
                for (int k = 0; k < w && ok; k++) {
                    int p = c + k - w / 2;
                    // un bloque de margen con las esquinas de las salas
                    if (p < lo || p > hi || (overlap >= 4 && (p == lo || p == hi))) ok = false;
                    else if (doorBlocked(wallAlongZ ? fixed : p, wallAlongZ ? p : fixed)) ok = false;
                }
                if (ok) {
                    addDoor(f, wallAlongZ ? fixed : c, wallAlongZ ? c : fixed, wallAlongZ, overlap);
                    return;
                }
                if (off == 0) break;
            }
    }

    private void addDoor(Floor f, int x, int z, boolean wallAlongZ, int overlap) {
        int w = overlap >= 9 ? 3 : (overlap >= 4 ? 2 : 1);
        int dh = Math.min(f.h - 2, f.h >= 11 ? 5 : 3);
        for (int k = 0; k < w; k++) {
            int dx = wallAlongZ ? 0 : k - w / 2, dz = wallAlongZ ? k - w / 2 : 0;
            int px = x + dx, pz = z + dz;
            f.doors.add(new int[]{px, pz, dh});
            for (Room r : f.rooms) {
                if (r.r.contains(px + 1, pz) || r.r.contains(px - 1, pz) || r.r.contains(px, pz + 1) || r.r.contains(px, pz - 1)) {
                    r.markDoor(px, pz);
                }
                if (r.r.contains(px + 1, pz)) r.markApproach(px + 1, pz, 1, 0, 3, 1);
                if (r.r.contains(px - 1, pz)) r.markApproach(px - 1, pz, -1, 0, 3, 1);
                if (r.r.contains(px, pz + 1)) r.markApproach(px, pz + 1, 0, 1, 3, 1);
                if (r.r.contains(px, pz - 1)) r.markApproach(px, pz - 1, 0, -1, 3, 1);
            }
        }
    }

    protected Floor floorByIndex(int idx) {
        for (Floor f : floors) if (f.index == idx) return f;
        return null;
    }

    /** {xMuro, zMuro, xInterior, zInterior} de la entrada en el lado d. */
    private int[] entrance(Floor f, Direction d) {
        int cx = fp.cx(), cz = fp.cz();
        for (int off = 0; off <= 8; off++) {
            for (int sgn = -1; sgn <= 1; sgn += 2) {
                int o = off * sgn;
                int wx, wz;
                if (d == Direction.NORTH) { wx = cx + o; wz = fp.z0; }
                else if (d == Direction.SOUTH) { wx = cx + o; wz = fp.z1; }
                else if (d == Direction.WEST) { wx = fp.x0; wz = cz + o; }
                else { wx = fp.x1; wz = cz + o; }
                int ix = wx - d.getStepX(), iz = wz - d.getStepZ();
                Direction sd = d.getClockWise();
                int hw = Math.min(fp.w(), fp.d()) >= 20 ? 2 : 1;
                if (doorBlocked(wx + sd.getStepX() * hw, wz + sd.getStepZ() * hw) || doorBlocked(wx - sd.getStepX() * hw, wz - sd.getStepZ() * hw)) continue;
                if (f.roomAt(ix, iz) != null) return new int[]{wx, wz, ix, iz};
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ construccion

    protected BlockState wallAt(int x, int y, int z, int floorIndex, boolean postOrBeam) {
        if (mossy) return Pal.oldWall(x, y, z);
        if (floorIndex < 0) return Pal.foundation(x, y, z);
        if (!timber || floorIndex == 0) return Pal.wall(x, y, z);
        return postOrBeam ? Pal.DO_LOG : Pal.timberInfill(x, y, z);
    }

    @Override
    public void build(Canvas c) {
        Floor lowest = floors.get(0);
        // cimientos
        c.fill(fp.x0, lowest.y - 2, fp.z0, fp.x1, lowest.y - 2, fp.z1, Pal::foundation);
        // plintio alrededor
        for (int x = fp.x0 - 1; x <= fp.x1 + 1; x++)
            for (int z = fp.z0 - 1; z <= fp.z1 + 1; z++) {
                if (fp.contains(x, z)) continue;
                c.set(x, baseY - 1, z, Pal.trim(x, baseY, z));
                c.set(x, baseY, z, Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, outward(x, z).getOpposite(), false));
            }
        for (Floor f : floors) buildFloor(c, f);
        buildWalls(c);
        for (Floor f : floors) decorateFloor(c, f);
        buildWindows(c);
        buildButtresses(c);
        buildEntrances(c);
        buildRoof(c);
        if (turretCorner >= 0) buildTurret(c);
        extra(c);
    }

    /** Para subclases: detalles adicionales. */
    protected void extra(Canvas c) {}

    private Direction outward(int x, int z) {
        int dx = x < fp.x0 ? -1 : (x > fp.x1 ? 1 : 0);
        int dz = z < fp.z0 ? -1 : (z > fp.z1 ? 1 : 0);
        if (dx != 0) return dx < 0 ? Direction.WEST : Direction.EAST;
        return dz < 0 ? Direction.NORTH : Direction.SOUTH;
    }

    private void buildFloor(Canvas c, Floor f) {
        Rect in = f.interior;
        int airTop = f.y + f.h - 2;
        // losa
        for (int x = Math.max(in.x0, c.minX); x <= Math.min(in.x1, c.maxX); x++)
            for (int z = Math.max(in.z0, c.minZ); z <= Math.min(in.z1, c.maxZ); z++) {
                if (f.voidRect != null && f.voidRect.contains(x, z)) {
                    c.column(x, z, f.y - 1, airTop, Pal.AIR);
                    continue;
                }
                BlockState slab = f.index < 0 ? Pal.foundation(x, f.y, z) : Pal.woodFloor(x, f.y, z);
                c.set(x, f.y - 1, z, slab);
                Room r = f.roomAt(x, z);
                if (r == null) {
                    // muro divisorio
                    final int fi = f.index;
                    c.column(x, z, f.y, airTop, (a, b, cc) -> fi <= 0 ? Pal.wall(a, b, cc) : Pal.timberInfill(a, b, cc));
                } else {
                    c.column(x, z, f.y, airTop, Pal.AIR);
                }
            }
        // vigas del techo en muros divisorios
        // puertas interiores
        for (int[] d : f.doors) {
            for (int y = f.y; y < f.y + d[2]; y++) c.set(d[0], y, d[1], Pal.AIR);
            c.set(d[0], f.y + d[2], d[1], f.index <= 0 ? Pal.PB_BRICKS : Pal.DO_STRIPPED);
        }
        // barandilla del atrio
        if (f.voidRect != null) {
            Rect v = f.voidRect;
            for (int x = v.x0 - 1; x <= v.x1 + 1; x++)
                for (int z = v.z0 - 1; z <= v.z1 + 1; z++) {
                    if (v.contains(x, z) || !in.contains(x, z)) continue;
                    boolean xs = x == v.x0 - 1 || x == v.x1 + 1;
                    boolean zs = z == v.z0 - 1 || z == v.z1 + 1;
                    Direction.Axis ax = (xs && !zs) ? Direction.Axis.Z : Direction.Axis.X;
                    c.set(x, f.y, z, Pal.connected(Blocks.DARK_OAK_FENCE, ax));
                }
        }
    }

    private void decorateFloor(Canvas c, Floor f) {
        Rng rng = new Rng(seed ^ (f.index * 7919L));
        Rect chunk = new Rect(c.minX - 1, c.minZ - 1, c.maxX + 1, c.maxZ + 1);
        for (Room r : f.rooms) {
            long rs = rng.fork();
            if (f.voidRect != null && f.voidRect.intersects(r.r)) continue;
            if (!r.r.intersects(chunk)) continue;
            Decor.decorate(c, r, rs);
        }
    }

    /** Apunta en cada sala las casillas que quedan delante de una ventana (misma cuenta que buildWindows). */
    private void markWindows(Floor f) {
        if (f.index < 0 || f.h - 3 < 2) return;
        boolean wide = f.h >= 8;
        for (int side = 0; side < 4; side++) {
            int len = (side % 2 == 0) ? fp.w() : fp.d();
            for (int a = 4; a <= len - 5; a += 8) {
                int wx, wz, ix, iz;
                if (side == 0) { wx = fp.x0 + a; wz = fp.z0; ix = wx; iz = wz + 1; }
                else if (side == 2) { wx = fp.x0 + a; wz = fp.z1; ix = wx; iz = wz - 1; }
                else if (side == 1) { wx = fp.x1; wz = fp.z0 + a; ix = wx - 1; iz = wz; }
                else { wx = fp.x0; wz = fp.z0 + a; ix = wx + 1; iz = wz; }
                if (nearTurret(wx, wz, 6.5)) continue;
                int halfW = wide ? 1 : 0;
                for (int k = -halfW - 1; k <= halfW + 1; k++) {
                    int px = side % 2 == 0 ? ix + k : ix, pz = side % 2 == 0 ? iz : iz + k;
                    Room r = f.roomAt(px, pz);
                    if (r != null) r.windows.add(Room.key(px, pz));
                }
            }
        }
    }

    /** Si encima de esta planta hay un atrio abierto, el techo de verdad es el de arriba del todo. */
    private void markAtrium(Floor f) {
        Rect hole = null;
        int ceil = topY;
        boolean found = false;
        for (Floor g : floors) {
            if (g.index <= f.index) continue;
            if (g.voidRect != null) {
                if (hole == null) hole = g.voidRect;
                found = true;
            } else if (found) {
                ceil = Math.min(ceil, g.y - 1);
                break;
            }
        }
        if (!found) return;
        for (Room r : f.rooms) {
            if (!r.r.intersects(hole)) continue;
            r.atrium = hole;
            r.atriumTop = ceil;
        }
    }

    private void buildWalls(Canvas c) {
        int low = floors.get(0).y - 1;
        for (int x = fp.x0; x <= fp.x1; x++)
            for (int z = fp.z0; z <= fp.z1; z++) {
                boolean edge = x == fp.x0 || x == fp.x1 || z == fp.z0 || z == fp.z1;
                if (!edge || !c.inXZ(x, z)) continue;
                boolean corner = (x == fp.x0 || x == fp.x1) && (z == fp.z0 || z == fp.z1);
                int along = (x == fp.x0 || x == fp.x1) ? z - fp.z0 : x - fp.x0;
                for (int y = low; y <= topY; y++) {
                    Floor f = floorAtY(y);
                    int fi = f == null ? 0 : f.index;
                    boolean beam = f != null && y == f.y - 1;
                    boolean post = corner || along % 4 == 0;
                    BlockState s;
                    if (corner && fi >= 0 && !mossy) s = Pal.BASALT;
                    else if (beam && fi > 0) s = timber && !mossy ? Pal.log(Blocks.DARK_OAK_LOG, (x == fp.x0 || x == fp.x1) ? Direction.Axis.Z : Direction.Axis.X) : Pal.trim(x, y, z);
                    else if (beam && fi == 0) s = Pal.trim(x, y, z);
                    else s = wallAt(x, y, z, fi, post);
                    c.set(x, y, z, s);
                }
            }
    }

    protected Floor floorAtY(int y) {
        Floor best = null;
        for (Floor f : floors) if (y >= f.y - 1 && y <= f.y + f.h - 2) best = f;
        return best;
    }

    private boolean nearTurret(int x, int z, double r) {
        if (turretCorner < 0) return false;
        int[] t = turretCenter();
        double dx = x - t[0], dz = z - t[1];
        return dx * dx + dz * dz < r * r;
    }

    private void buildWindows(Canvas c) {
        for (Floor f : floors) {
            if (f.index < 0) continue;
            int sill = f.y + 1;
            int wh = f.h - 3;
            if (wh < 2) continue;
            boolean wide = f.h >= 8;
            for (int side = 0; side < 4; side++) {
                int len = (side % 2 == 0) ? fp.w() : fp.d();
                for (int a = 4; a <= len - 5; a += 8) {
                    int wx, wz, ix, iz;
                    if (side == 0) { wx = fp.x0 + a; wz = fp.z0; ix = wx; iz = wz + 1; }
                    else if (side == 2) { wx = fp.x0 + a; wz = fp.z1; ix = wx; iz = wz - 1; }
                    else if (side == 1) { wx = fp.x1; wz = fp.z0 + a; ix = wx - 1; iz = wz; }
                    else { wx = fp.x0; wz = fp.z0 + a; ix = wx + 1; iz = wz; }
                    if (nearTurret(wx, wz, 6.5)) continue;
                    if (f.roomAt(ix, iz) == null && (f.voidRect == null || !f.voidRect.contains(ix, iz))) continue;
                    int halfW = wide ? 1 : 0;
                    for (int k = -halfW; k <= halfW; k++) {
                        int px = side % 2 == 0 ? wx + k : wx, pz = side % 2 == 0 ? wz : wz + k;
                        int top = sill + wh - 1 - (Math.abs(k) == 1 ? 1 : 0);
                        for (int y = sill; y <= top; y++) {
                            BlockState g;
                            if (f.index == 0 && !wide) g = Pal.connected(Blocks.IRON_BARS, side % 2 == 0 ? Direction.Axis.X : Direction.Axis.Z);
                            else {
                                BlockState mos = ModDeco.mosaic(wx, wz, y, sill, top);
                                g = mos != null ? mos : Pal.glass(wx, f.y + (y - sill) / 3, wz);
                            }
                            c.set(px, y, pz, g);
                        }
                    }
                    // alfeizar
                    Direction out = side == 0 ? Direction.NORTH : side == 2 ? Direction.SOUTH : side == 1 ? Direction.EAST : Direction.WEST;
                    for (int k = -halfW; k <= halfW; k++) {
                        int px = (side % 2 == 0 ? wx + k : wx) + out.getStepX(), pz = (side % 2 == 0 ? wz : wz + k) + out.getStepZ();
                        c.set(px, sill - 1, pz, Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, out.getOpposite(), true));
                    }
                }
            }
        }
    }

    private void buttress(Canvas c, int x, int z, Direction out) {
        int total = topY - baseY;
        int h1 = baseY + total * 2 / 5, h2 = topY - 2;
        for (int d = 1; d <= 2; d++) {
            int px = x + out.getStepX() * d, pz = z + out.getStepZ() * d;
            int top = d == 1 ? h2 : h1;
            c.column(px, pz, baseY - 1, top, Pal::wall);
            c.set(px, top + 1, pz, Pal.stairs(Blocks.DEEPSLATE_BRICK_STAIRS, out.getOpposite(), false));
        }
    }

    /** True si el contrafuerte en (x, z) del lado "out" taparia una entrada o sus farolas. */
    private boolean nearEntrance(int x, int z, Direction out) {
        for (int[] e : entranceCells()) {
            if (e[2] != out.getStepX() || e[3] != out.getStepZ()) continue;
            int along = out.getAxis() == Direction.Axis.Z ? Math.abs(x - e[0]) : Math.abs(z - e[1]);
            if (along <= e[4] + 3) return true;
        }
        return false;
    }

    private void buildButtresses(Canvas c) {
        if (mossy) return;
        for (int a = 8; a <= fp.w() - 8; a += 8) {
            if (!nearTurret(fp.x0 + a, fp.z0, 6.5) && !nearEntrance(fp.x0 + a, fp.z0, Direction.NORTH)) buttress(c, fp.x0 + a, fp.z0, Direction.NORTH);
            if (!nearTurret(fp.x0 + a, fp.z1, 6.5) && !nearEntrance(fp.x0 + a, fp.z1, Direction.SOUTH)) buttress(c, fp.x0 + a, fp.z1, Direction.SOUTH);
        }
        for (int a = 8; a <= fp.d() - 8; a += 8) {
            if (!nearTurret(fp.x0, fp.z0 + a, 6.5) && !nearEntrance(fp.x0, fp.z0 + a, Direction.WEST)) buttress(c, fp.x0, fp.z0 + a, Direction.WEST);
            if (!nearTurret(fp.x1, fp.z0 + a, 6.5) && !nearEntrance(fp.x1, fp.z0 + a, Direction.EAST)) buttress(c, fp.x1, fp.z0 + a, Direction.EAST);
        }
    }

    /** Medio ancho del hueco de la entrada (1 = puerta de casa, 2 = portalon). */
    private int entranceHalf() {
        return Math.min(fp.w(), fp.d()) >= 20 ? 2 : 1;
    }

    /** {xMuro, zMuro, dx hacia fuera, dz hacia fuera, medio ancho} de cada entrada exterior. */
    public List<int[]> entranceCells() {
        List<int[]> l = new ArrayList<>();
        Floor g = floorByIndex(0);
        if (g == null) return l;
        for (Direction d : entrances) {
            int[] e = entrance(g, d);
            if (e != null) l.add(new int[]{e[0], e[1], d.getStepX(), d.getStepZ(), entranceHalf()});
        }
        return l;
    }

    /** Para las pruebas: {x, y, z, nx, nz} de cada puerta interior (n = normal del muro). */
    public List<int[]> innerDoors() {
        List<int[]> l = new ArrayList<>();
        for (Floor f : floors)
            for (int[] d : f.doors) {
                boolean wallAlongZ = f.roomAt(d[0] + 1, d[1]) != null || f.roomAt(d[0] - 1, d[1]) != null;
                l.add(new int[]{d[0], f.y, d[1], wallAlongZ ? 1 : 0, wallAlongZ ? 0 : 1});
            }
        return l;
    }

    private void buildEntrances(Canvas c) {
        Floor g = floorByIndex(0);
        if (g == null) return;
        for (Direction d : entrances) {
            int[] e = entrance(g, d);
            if (e == null) continue;
            Direction side = d.getClockWise();
            int w = entranceHalf();
            int hgt = Math.min(g.h - 2, w == 2 ? 5 : 3);
            for (int k = -w; k <= w; k++) {
                int px = e[0] + side.getStepX() * k, pz = e[1] + side.getStepZ() * k;
                boolean frame = Math.abs(k) == w;
                for (int y = baseY; y <= baseY + hgt + 1; y++) {
                    if (frame) c.set(px, y, pz, Pal.PB_CHISELED);
                    else if (y <= baseY + hgt - (Math.abs(k) == w - 1 && w > 1 ? 1 : 0)) c.set(px, y, pz, Pal.AIR);
                    else c.set(px, y, pz, Pal.PB_BRICKS);
                }
                // escalon exterior y farolas
                c.set(px + d.getStepX(), baseY, pz + d.getStepZ(), Pal.AIR);
            }
            if (w == 1) {
                // puerta de verdad en las casas pequenas
                Direction facing = d.getOpposite();
                BlockState lo = ModDeco.door(facing, false, (int) (seed & 7)), up = ModDeco.door(facing, true, (int) (seed & 7));
                if (lo == null || up == null) {
                    lo = Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(net.minecraft.world.level.block.DoorBlock.FACING, facing)
                            .setValue(net.minecraft.world.level.block.DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER);
                    up = lo.setValue(net.minecraft.world.level.block.DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER);
                }
                c.set(e[0], baseY, e[1], lo);
                c.set(e[0], baseY + 1, e[1], up);
                BlockState mat = Mods.state("supplementaries:doormat", "facing=" + facing.getName());
                if (mat != null) c.set(e[0] + d.getStepX(), baseY, e[1] + d.getStepZ(), mat);
            }
            int lx = e[0] + d.getStepX() + side.getStepX() * (w + 1), lz = e[1] + d.getStepZ() + side.getStepZ() * (w + 1);
            int rx = e[0] + d.getStepX() - side.getStepX() * (w + 1), rz = e[1] + d.getStepZ() - side.getStepZ() * (w + 1);
            c.set(lx, baseY, lz, Pal.PB_BRICKS);
            c.set(lx, baseY + 1, lz, Pal.SOUL_LANTERN);
            c.set(rx, baseY, rz, Pal.PB_BRICKS);
            c.set(rx, baseY + 1, rz, Pal.SOUL_LANTERN);
        }
    }

    private void buildRoof(Canvas c) {
        if (roof == Roof.NONE) return;
        // losa del techo
        c.fill(fp.x0 + 1, topY, fp.z0 + 1, fp.x1 - 1, topY, fp.z1 - 1, roof == Roof.TERRACE ? Pal.DS_TILES : Pal.DO_PLANKS);
        if (roof == Roof.TERRACE) {
            terrace(c);
            return;
        }
        Rect g = fp.grow(1);
        boolean ridgeX = fp.w() >= fp.d();
        int s = roofSlope;
        for (int x = Math.max(g.x0, c.minX); x <= Math.min(g.x1, c.maxX); x++)
            for (int z = Math.max(g.z0, c.minZ); z <= Math.min(g.z1, c.maxZ); z++) {
                int e;
                Direction up;
                if (roof == Roof.GABLE) {
                    int p = ridgeX ? z - g.z0 : x - g.x0, span = ridgeX ? g.d() : g.w();
                    e = Math.min(p, span - 1 - p);
                    boolean lowSide = p < span - 1 - p;
                    up = ridgeX ? (lowSide ? Direction.SOUTH : Direction.NORTH) : (lowSide ? Direction.EAST : Direction.WEST);
                    if (p == span - 1 - p) up = null;
                } else {
                    int ex = Math.min(x - g.x0, g.x1 - x), ez = Math.min(z - g.z0, g.z1 - z);
                    e = Math.min(ex, ez);
                    if (ex < ez) up = (x - g.x0 < g.x1 - x) ? Direction.EAST : Direction.WEST;
                    else if (ez < ex) up = (z - g.z0 < g.z1 - z) ? Direction.SOUTH : Direction.NORTH;
                    else up = null;
                }
                int h = topY + 1 + e * s;
                for (int y = Math.max(topY + 1, h - 2 * s); y < h; y++) c.set(x, y, z, roofBlock(x, y, z));
                if (up == null) c.set(x, h, z, Pal.slab(roofSlab(), false));
                else c.set(x, h, z, Pal.stairs(roofStairs(), up, false));
                // hastiales
                if (roof == Roof.GABLE) {
                    boolean gableEnd = ridgeX ? (x == fp.x0 || x == fp.x1) : (z == fp.z0 || z == fp.z1);
                    boolean inside = ridgeX ? (z > fp.z0 && z < fp.z1) : (x > fp.x0 && x < fp.x1);
                    if (gableEnd && inside) {
                        int mid = ridgeX ? fp.cz() : fp.cx();
                        int cross = ridgeX ? z : x;
                        for (int y = topY + 1; y < h - s + 1; y++) {
                            int dy = y - (topY + 1 + (Math.min(fp.w(), fp.d()) / 2) * s / 2);
                            int dc = cross - mid;
                            boolean rose = dy * dy + dc * dc <= 4;
                            c.set(x, y, z, rose ? Pal.glass(x, y, z) : Pal.wall(x, y, z));
                        }
                    }
                }
            }
        // chimeneas
        if (roof == Roof.GABLE && fp.area() > 150) {
            Rng rng = new Rng(seed ^ 0xC41);
            int n = 1 + rng.nextInt(2);
            for (int i = 0; i < n; i++) {
                int x = ridgeX ? rng.range(fp.x0 + 3, fp.x1 - 4) : fp.cx() - 3;
                int z = ridgeX ? fp.cz() - 3 : rng.range(fp.z0 + 3, fp.z1 - 4);
                int top = ridgeTop + 3;
                for (int dx = 0; dx <= 1; dx++)
                    for (int dz = 0; dz <= 1; dz++) c.column(x + dx, z + dz, topY + 1, top, Pal::wall);
                c.set(x, top + 1, z, Pal.CAMPFIRE.setValue(CampfireBlock.SIGNAL_FIRE, false));
                c.set(x + 1, top + 1, z + 1, Pal.slab(Blocks.POLISHED_BLACKSTONE_SLAB, false));
            }
        }
    }

    private void terrace(Canvas c) {
        Rng rng = new Rng(seed ^ 0x7E);
        for (int x = fp.x0; x <= fp.x1; x++)
            for (int z = fp.z0; z <= fp.z1; z++) {
                boolean edge = x == fp.x0 || x == fp.x1 || z == fp.z0 || z == fp.z1;
                if (!edge) continue;
                int along = (x == fp.x0 || x == fp.x1) ? z : x;
                c.set(x, topY, z, Pal.trim(x, topY, z));
                c.set(x, topY + 1, z, Pal.wall(x, topY + 1, z));
                if (Math.floorMod(along, 4) < 2) c.set(x, topY + 2, z, Pal.wall(x, topY + 2, z));
                if (Math.floorMod(along, 16) == 0) c.set(x, topY + 3, z, Pal.SOUL_LANTERN);
            }
        // jardineras y bancos en la azotea
        for (int x = fp.x0 + 4; x <= fp.x1 - 4; x += 9)
            for (int z = fp.z0 + 4; z <= fp.z1 - 4; z += 9) {
                if (nearTurret(x, z, 8)) continue;
                int kind = rng.nextInt(3);
                if (kind == 0) {
                    for (int dx = -1; dx <= 1; dx++)
                        for (int dz = -1; dz <= 1; dz++) {
                            boolean rim = dx != 0 || dz != 0;
                            c.set(x + dx, topY + 1, z + dz, rim ? Pal.PB_BRICKS : Pal.ROOTED_DIRT);
                        }
                    c.set(x, topY + 2, z, Pal.leaves(Blocks.AZALEA_LEAVES));
                    c.set(x, topY + 2, z, Pal.b(Blocks.FLOWERING_AZALEA));
                } else if (kind == 1) {
                    c.set(x, topY + 1, z, Pal.stairs(Blocks.DARK_OAK_STAIRS, Direction.NORTH, false));
                    c.set(x + 1, topY + 1, z, Pal.stairs(Blocks.DARK_OAK_STAIRS, Direction.NORTH, false));
                    c.set(x, topY + 1, z - 1, Pal.slab(Blocks.DARK_OAK_SLAB, true));
                } else {
                    c.set(x, topY + 1, z, Pal.PB_CHISELED);
                    c.set(x, topY + 2, z, Pal.SOUL_CAMPFIRE);
                }
            }
    }

    private void buildTurret(Canvas c) {
        int[] t = turretCenter();
        int tx = t[0], tz = t[1], sx = t[2], sz = t[3];
        Floor lowest = floors.get(0);
        int yStart = lowest.y;
        int lastY = roof == Roof.TERRACE ? topY + 1 : floors.get(floors.size() - 1).y;
        int top = (roof == Roof.TERRACE ? topY + 6 : topY + 3);
        double R = 4.6;
        for (int x = tx - 5; x <= tx + 5; x++)
            for (int z = tz - 5; z <= tz + 5; z++) {
                double dx = x - tx, dz = z - tz, d = Math.sqrt(dx * dx + dz * dz);
                if (d > R) continue;
                if (d <= 3.0) {
                    c.column(x, z, yStart, top, Pal.AIR);
                } else {
                    c.column(x, z, yStart - 1, top, Pal::wall);
                }
                c.set(x, yStart - 1, z, Pal.foundation(x, yStart, z));
            }
        Spiral.build(c, tx, tz, yStart, lastY, turretCorner * 4, Blocks.DEEPSLATE_TILE_STAIRS, Pal::wall);
        List<Integer> levels = new ArrayList<>();
        for (Floor f : floors) levels.add(f.y);
        if (roof == Roof.TERRACE) levels.add(topY + 1);
        for (int fy : levels) {
            // descansillo y puerta hacia el edificio
            for (int x = tx - 4; x <= tx + 4; x++)
                for (int z = tz - 4; z <= tz + 4; z++) {
                    int dx = x - tx, dz = z - tz;
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d <= 3.0 && !Spiral.inWell(dx, dz)) c.set(x, fy - 1, z, Pal.DS_TILES);
                }
            int[][] door = {{3, 2}, {2, 3}, {3, 3}, {4, 3}, {3, 4}, {4, 4}};
            for (int[] dd : door) {
                int x = tx + dd[0] * sx, z = tz + dd[1] * sz;
                c.set(x, fy - 1, z, Pal.DS_TILES);
                for (int y = fy; y <= fy + 2; y++) c.set(x, y, z, Pal.AIR);
            }
            // ventana tronera
            c.set(tx - 4 * sx, fy + 1, tz, Pal.AIR);
            c.set(tx - 4 * sx, fy + 2, tz, Pal.AIR);
        }
        // tejado conico
        int baseR = 5;
        for (int x = tx - baseR; x <= tx + baseR; x++)
            for (int z = tz - baseR; z <= tz + baseR; z++) {
                double dx = x - tx, dz = z - tz, d = Math.sqrt(dx * dx + dz * dz);
                if (d > baseR + 0.4) continue;
                int h = top + 1 + (int) Math.round((baseR + 0.4 - d) * 2.2);
                for (int y = Math.max(top + 1, h - 3); y <= h; y++) c.set(x, y, z, Pal.roof(x, y, z));
            }
        int apex = top + 1 + (int) Math.round((baseR + 0.4) * 2.2);
        c.column(tx, tz, apex + 1, apex + 3, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
        if (roof == Roof.TERRACE) {
            // salida a la azotea
            for (int dd = 3; dd <= 5; dd++) {
                int x = tx + dd * sx, z = tz + dd * sz;
                for (int y = topY + 1; y <= topY + 3; y++) c.set(x, y, z, Pal.AIR);
            }
        }
    }

    protected static Block stairsOf(BlockState s) {
        return Blocks.DEEPSLATE_BRICK_STAIRS;
    }
}
