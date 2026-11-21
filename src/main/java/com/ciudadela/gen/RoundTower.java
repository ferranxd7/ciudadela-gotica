package com.ciudadela.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Torre redonda con escalera de caracol, plantas, troneras y tejado conico o almenado.
 * Con tree() se convierte en "torre-arbol" como la gran torre central: madera en espiral que sube
 * por los muros, ramas con hojas, raices en la base y una copa en lo alto.
 */
public class RoundTower extends Element {
    public enum Top { CONE, PLATFORM }

    public final int cx, cz, groundY, radius, topY;
    public final Top top;
    private final List<Integer> regular = new ArrayList<>();
    private final TreeSet<Integer> doorFloors = new TreeSet<>();
    private final List<double[]> doors = new ArrayList<>(); // {angulo, y, ancho}
    private final List<Double> avoid = new ArrayList<>();    // angulos que hay que dejar libres
    private Room.Type roomType = Room.Type.STORAGE;
    private boolean mossy = false;
    private double coneSlope = 2.3;
    private boolean tree = false;
    private Noise tn;

    public RoundTower(long seed, int cx, int cz, int groundY, int radius, int height, Top top) {
        super(seed);
        this.cx = cx;
        this.cz = cz;
        this.groundY = groundY;
        this.radius = radius;
        this.topY = groundY + height;
        this.top = top;
        for (int y = groundY; y < topY - 6; y += 10) regular.add(y);
        doorFloors.add(groundY);
        updateBounds();
    }

    private void updateBounds() {
        int coneH = (int) Math.round((radius + 2) * coneSlope) + 6;
        if (!tree) {
            bounds(cx - radius - 3, groundY - 8, cz - radius - 3, cx + radius + 3, topY + coneH, cz + radius + 3);
        } else {
            int reach = radius + radius / 2 + 17;
            bounds(cx - reach, groundY - 8, cz - reach, cx + reach, topY + Math.max(coneH, 6) + 16, cz + reach);
        }
    }

    public RoundTower door(double angleRad, int y) {
        doors.add(new double[]{angleRad, y, 1.6});
        avoid.add(angleRad);
        doorFloors.add(y);
        return this;
    }

    /** Puerta mirando hacia un punto. */
    public RoundTower doorToward(int x, int z, int y) {
        return door(Math.atan2(z - cz, x - cx), y);
    }

    /** Deja libre de raices, ramas y bultos de madera la direccion hacia un punto (puentes...). */
    public RoundTower avoidToward(int x, int z) {
        avoid.add(Math.atan2(z - cz, x - cx));
        return this;
    }

    public RoundTower rooms(Room.Type t) {
        this.roomType = t;
        return this;
    }

    public RoundTower mossy() {
        this.mossy = true;
        return this;
    }

    public RoundTower slope(double s) {
        this.coneSlope = s;
        updateBounds();
        return this;
    }

    /** Convierte la torre en torre-arbol. */
    public RoundTower tree() {
        this.tree = true;
        this.tn = new Noise(seed ^ 0x7EEL);
        updateBounds();
        return this;
    }

    /**
     * Plantas de la torre: las de las puertas siempre, y las normales (cada 10 bloques) solo si no
     * quedan a menos de 5 bloques de otra. Asi nunca hay un suelo a la altura de la cabeza.
     */
    public List<Integer> floors() {
        TreeSet<Integer> all = new TreeSet<>();
        // dos puertas muy juntas en altura: la de arriba no lleva planta entera, solo pasarela
        for (int y : doorFloors) {
            Integer lo = all.floor(y);
            if (lo == null || y - lo >= 5) all.add(y);
        }
        for (int y : regular) {
            Integer lo = all.floor(y), hi = all.ceiling(y);
            if ((lo == null || y - lo >= 5) && (hi == null || hi - y >= 5)) all.add(y);
        }
        return new ArrayList<>(all);
    }

    /** Alturas de puerta que no tienen planta entera (llevan descansillo y pasarela). */
    private List<Integer> catwalks() {
        List<Integer> f = floors();
        List<Integer> out = new ArrayList<>();
        for (int y : doorFloors) if (!f.contains(y)) out.add(y);
        return out;
    }

    /** Puertas: {angulo, altura} (para las pruebas automaticas). */
    public List<double[]> doorList() {
        List<double[]> l = new ArrayList<>();
        for (double[] d : doors) l.add(new double[]{d[0], d[1]});
        return l;
    }

    public int lastFloor() {
        List<Integer> f = floors();
        return f.get(f.size() - 1);
    }

    private static double angDiff(double a, double b) {
        double d = Math.abs(a - b) % (Math.PI * 2);
        return d > Math.PI ? Math.PI * 2 - d : d;
    }

    private boolean avoided(double ang, double tolBlocks) {
        for (double a : avoid) if (angDiff(ang, a) * radius < tolBlocks) return true;
        return false;
    }

    private BlockState wallState(int x, int y, int z) {
        return mossy ? Pal.oldWall(x, y, z) : Pal.wall(x, y, z);
    }

    // ------------------------------------------------------------ madera de la torre-arbol

    private double woodiness(int x, int y, int z) {
        double t = (y - groundY) / (double) Math.max(1, topY - groundY);
        double ang = Math.atan2(z - cz, x - cx);
        double phase = (seed & 255) / 40.0;
        double spiral = Math.sin(ang * 2 + y * 0.11 + phase);
        double n = tn.value(x * 0.2, y * 0.1, z * 0.2) * 0.25;
        return t * 1.15 - 0.3 + 0.45 * spiral + n;
    }

    private BlockState bark(int x, int y, int z, Direction.Axis axis) {
        int p = Hash.pct(x, y, z, seed + 5);
        BlockState s;
        if (p < 55) s = Pal.b(Blocks.DARK_OAK_WOOD);
        else if (p < 80) s = Pal.b(Blocks.MANGROVE_WOOD);
        else if (p < 90) s = Pal.b(Blocks.DARK_OAK_LOG);
        else if (p < 96) s = Pal.MOSS;
        else s = Pal.b(Blocks.MANGROVE_ROOTS);
        if (s.hasProperty(RotatedPillarBlock.AXIS)) s = s.setValue(RotatedPillarBlock.AXIS, axis);
        return s;
    }

    private BlockState leaf(int x, int y, int z) {
        int p = Hash.pct(x, y, z, seed + 9);
        if (p < 50) return Pal.leaves(Blocks.DARK_OAK_LEAVES);
        if (p < 70) return Pal.leaves(Blocks.AZALEA_LEAVES);
        if (p < 80) return Pal.leaves(Blocks.FLOWERING_AZALEA_LEAVES);
        if (p < 92) return Pal.leaves(Blocks.MANGROVE_LEAVES);
        return Pal.leaves(Blocks.SPRUCE_LEAVES);
    }

    // ------------------------------------------------------------ construccion

    @Override
    public void build(Canvas c) {
        List<Integer> floorYs = floors();
        int lastFloor = floorYs.get(floorYs.size() - 1);
        double R = radius + 0.5;
        double inner = radius - 1.5;
        int reach = tree ? radius + 4 : radius + 2;
        for (int x = Math.max(cx - reach, c.minX); x <= Math.min(cx + reach, c.maxX); x++)
            for (int z = Math.max(cz - reach, c.minZ); z <= Math.min(cz + reach, c.maxZ); z++) {
                int dx = x - cx, dz = z - cz;
                double d = Math.sqrt(dx * dx + dz * dz);
                double ang = Math.atan2(dz, dx);
                if (d > R + 1.2) {
                    if (tree) bulge(c, x, z, d, R, ang);
                    continue;
                }
                if (d > R) {
                    // zocalo y matacanes
                    c.set(x, groundY - 1, z, Pal.trim(x, groundY, z));
                    c.set(x, groundY, z, Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Pal.dirOf(-dx, -dz), false));
                    if (top == Top.PLATFORM) {
                        c.set(x, topY - 1, z, Pal.stairs(Blocks.DEEPSLATE_BRICK_STAIRS, Pal.dirOf(-dx, -dz), true));
                        c.set(x, topY, z, Pal.DS_TILES);
                        boolean wood = tree && woodiness(x, topY, z) > 0.6;
                        c.set(x, topY + 1, z, wood ? bark(x, topY + 1, z, Direction.Axis.Y) : wallState(x, topY + 1, z));
                        int bucket = (int) Math.floor((ang + Math.PI) / (Math.PI * 2) * (radius * 2.5));
                        if ((bucket & 1) == 0) c.set(x, topY + 2, z, wood ? bark(x, topY + 2, z, Direction.Axis.Y) : wallState(x, topY + 2, z));
                    } else {
                        c.set(x, topY - 1, z, Pal.stairs(Blocks.DEEPSLATE_BRICK_STAIRS, Pal.dirOf(-dx, -dz), true));
                    }
                    if (tree) bulge(c, x, z, d, R, ang);
                    continue;
                }
                c.column(x, z, groundY - 8, groundY - 1, Pal::foundation);
                if (d > inner) {
                    // muro
                    for (int y = groundY; y <= topY; y++) {
                        boolean slit = false;
                        for (int fy : floorYs) {
                            if ((y == fy + 1 || y == fy + 2) && d > inner + 0.6) {
                                int bucket = (int) Math.floor((ang + Math.PI) / (Math.PI * 2) * 8 + 0.5);
                                double target = bucket / 8.0 * Math.PI * 2 - Math.PI;
                                if (angDiff(ang, target) < 0.55 / radius) slit = true;
                            }
                        }
                        boolean band = (y - groundY) % 10 == 0 && y > groundY;
                        BlockState s;
                        if (slit) s = Pal.AIR;
                        else if (tree && woodiness(x, y, z) > 0.55) s = bark(x, y, z, Direction.Axis.Y);
                        else s = band ? Pal.trim(x, y, z) : wallState(x, y, z);
                        c.set(x, y, z, s);
                    }
                } else {
                    c.column(x, z, groundY, topY - 1, Pal.AIR);
                    boolean well = Spiral.inWell(dx, dz);
                    for (int fy : floorYs) {
                        if (well) continue;
                        c.set(x, fy - 1, z, fy == groundY ? Pal.DS_TILES : Pal.woodFloor(x, fy, z));
                    }
                    c.set(x, topY, z, (well && top == Top.PLATFORM) ? Pal.AIR : Pal.DS_TILES);
                }
                // puertas
                for (double[] door : doors) {
                    if (d <= inner - 1.0) continue;
                    if (angDiff(ang, door[0]) * Math.max(d, 1) <= door[2]) {
                        int y = (int) door[1];
                        for (int k = 0; k < 3; k++) c.set(x, y + k, z, Pal.AIR);
                        c.set(x, y - 1, z, Pal.DS_TILES);
                        if (d > R - 0.8) c.set(x, y + 3, z, Pal.PB_CHISELED);
                    }
                }
            }
        // descansillos y pasarelas hasta las puertas que no tienen planta propia
        for (int cy : catwalks()) {
            for (int x = Math.max(cx - radius, c.minX); x <= Math.min(cx + radius, c.maxX); x++)
                for (int z = Math.max(cz - radius, c.minZ); z <= Math.min(cz + radius, c.maxZ); z++) {
                    int dx = x - cx, dz = z - cz;
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d > inner || Spiral.inWell(dx, dz)) continue;
                    boolean landing = Math.max(Math.abs(dx), Math.abs(dz)) == 3;
                    boolean path = false;
                    for (double[] door : doors) {
                        if ((int) door[1] != cy) continue;
                        double t = dx * Math.cos(door[0]) + dz * Math.sin(door[0]);
                        double perp = Math.abs(-dx * Math.sin(door[0]) + dz * Math.cos(door[0]));
                        if (t >= 2.5 && perp <= 1.3) path = true;
                    }
                    if (landing || path) c.set(x, cy - 1, z, Pal.DS_TILES);
                }
        }
        // escalera
        int yEnd = top == Top.PLATFORM ? topY : lastFloor;
        Spiral.build(c, cx, cz, groundY, yEnd, (int) (seed & 15), Blocks.DEEPSLATE_BRICK_STAIRS, Pal::wall);
        if (top == Top.PLATFORM) {
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++) c.set(cx + dx, topY + 4, cz + dz, Pal.AIR);
            BlockState pit = ModDeco.firePit();
            c.set(cx, topY + 4, cz, pit != null ? pit : Pal.SOUL_CAMPFIRE);
            BlockState flag = ModDeco.flag(Direction.SOUTH, (seed & 1) == 0);
            if (flag != null) {
                c.set(cx + radius - 1, topY + 3, cz, Pal.PB_BRICKS);
                c.set(cx + radius - 1, topY + 4, cz, flag);
            }
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++) c.set(cx + dx, topY + 3, cz + dz, Pal.PB_BRICKS);
        } else {
            cone(c);
        }
        furnish(c, floorYs);
        if (tree) treeParts(c);
    }

    /** Corteza que engorda la torre por fuera, sin tapar puertas ni la union con las murallas. */
    private void bulge(Canvas c, int x, int z, double d, double R, double ang) {
        if (avoided(ang, 5.5)) return;
        for (int y = groundY + 1; y <= topY - 2; y++) {
            double w = woodiness(x, y, z);
            int b = (int) Math.floor((w - 0.5) * 6 * (0.6 + 0.4 * tn.value(x * 0.3, y * 0.1, z * 0.3)));
            if (b > 3) b = 3;
            if (d - R <= b) c.set(x, y, z, bark(x, y, z, Direction.Axis.Y));
        }
    }

    private void cone(Canvas c) {
        double base = radius + 1.6;
        for (int x = Math.max(cx - radius - 2, c.minX); x <= Math.min(cx + radius + 2, c.maxX); x++)
            for (int z = Math.max(cz - radius - 2, c.minZ); z <= Math.min(cz + radius + 2, c.maxZ); z++) {
                int dx = x - cx, dz = z - cz;
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > base) continue;
                int h = topY + 1 + (int) Math.round((base - d) * coneSlope);
                for (int y = Math.max(topY + 1, h - 3); y <= h; y++) c.set(x, y, z, Pal.roof(x, y, z));
            }
        int apex = topY + 1 + (int) Math.round(base * coneSlope);
        c.column(cx, cz, apex + 1, apex + 3, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
        c.set(cx, apex + 4, cz, Pal.b(Blocks.LIGHTNING_ROD));
        // buhardillas en el cono
        for (int i = 0; i < 4; i++) {
            Direction d = Direction.from2DDataValue(i);
            int dist = radius - 1;
            int x = cx + d.getStepX() * dist, z = cz + d.getStepZ() * dist;
            int y = topY + 2 + (int) Math.round((base - dist) * coneSlope) / 2;
            c.set(x, y, z, Pal.GLASS_BLACK);
            c.set(x, y + 1, z, Pal.stairs(Blocks.DEEPSLATE_TILE_STAIRS, d.getOpposite(), false));
        }
    }

    private void furnish(Canvas c, List<Integer> floorYs) {
        Rng rng = new Rng(seed ^ 0xF00D);
        double inner = radius - 1.5;
        if (inner < 4) return;
        for (int fi = 0; fi < floorYs.size(); fi++) {
            int fy = floorYs.get(fi);
            int ceil = fi + 1 < floorYs.size() ? floorYs.get(fi + 1) - 1 : topY;
            boolean chestDone = rng.chance(0.4);
            // farol colgando del techo de esta planta
            if (ceil - fy >= 5) {
                c.set(cx + 3, ceil - 1, cz, Pal.CHAIN);
                c.set(cx + 3, ceil - 2, cz, Pal.SOUL_LANTERN_HANG);
            }
            for (int i = 0; i < 6 + radius; i++) {
                double a = rng.nextDouble() * Math.PI * 2;
                int x = cx + (int) Math.round(Math.cos(a) * (inner - 0.6));
                int z = cz + (int) Math.round(Math.sin(a) * (inner - 0.6));
                int dx = x - cx, dz = z - cz;
                if (Spiral.inWell(dx, dz) || dx * dx + dz * dz > (inner - 0.3) * (inner - 0.3)) continue;
                boolean nearDoor = false;
                for (double[] door : doors) if (Math.abs(door[1] - fy) < 1 && angDiff(a, door[0]) < 0.6) nearDoor = true;
                if (nearDoor || !c.isAir(x, fy, z)) continue;
                int roll = rng.nextInt(10);
                Direction in = Pal.dirOf(-dx, -dz);
                if (!chestDone && roll == 0) {
                    c.chest(x, fy, z, in, Decor.loot(roomType == Room.Type.ARMORY ? "armeria" : "comun"));
                    chestDone = true;
                } else if (roll < 2) {
                    BlockState m = ModDeco.clutter(i + fy, false);
                    c.set(x, fy, z, m != null ? m : Pal.b(Blocks.BARREL));
                } else if (roll < 4) c.set(x, fy, z, Pal.b(Blocks.BARREL));
                else if (roll < 6) c.set(x, fy, z, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Y));
                else if (roll < 7) {
                    // velas siempre encima de algo: un barril
                    c.set(x, fy, z, Pal.b(Blocks.BARREL));
                    if (c.isAir(x, fy + 1, z)) c.set(x, fy + 1, z, Pal.candles(Blocks.BLACK_CANDLE, 3));
                }
                else if (roll < 8) c.set(x, fy, z, Pal.facing(Blocks.ANVIL, in.getClockWise()));
                else if (roll < 9) c.set(x, fy, z, Pal.COBWEB);
                else c.set(x, fy, z, Pal.BOOKSHELF);
            }
        }
    }

    // ------------------------------------------------------------ partes de arbol

    private void treeParts(Canvas c) {
        Rng r = new Rng(seed ^ 0x7EE5);
        List<double[]> blobs = new ArrayList<>();
        List<int[]> limbs = new ArrayList<>();
        List<int[]> hangs = new ArrayList<>();
        // ramas cerca de lo alto
        int nb = radius >= 9 ? 4 : 3;
        int made = 0;
        for (int tries = 0; tries < 30 && made < nb; tries++) {
            double a = r.nextDouble() * Math.PI * 2;
            if (avoided(a, 7)) continue;
            int sy = topY - 3 - made * 4 - r.nextInt(3);
            if (sy < groundY + 8) break;
            int len = radius / 2 + r.range(5, 9);
            int sx = cx + (int) Math.round(Math.cos(a) * (radius + 0.5)), sz = cz + (int) Math.round(Math.sin(a) * (radius + 0.5));
            int ex = cx + (int) Math.round(Math.cos(a) * (radius + len)), ez = cz + (int) Math.round(Math.sin(a) * (radius + len));
            int ey = sy + r.range(3, 6);
            limbs.add(new int[]{sx, sy, sz, ex, ey, ez});
            blobs.add(new double[]{ex, ey + 2, ez, 3.5 + r.nextDouble() * 1.5, 0.6});
            for (int k = 0; k < 3; k++) hangs.add(new int[]{ex + r.range(-3, 3), ey, ez + r.range(-3, 3), r.range(2, 6), k % 2});
            made++;
        }
        // copa
        if (top == Top.CONE) {
            double base = radius + 1.6;
            int coneH = (int) Math.round(base * coneSlope);
            int n = 5;
            for (int i = 0; i < n; i++) {
                double a = i * Math.PI * 2 / n + r.nextDouble() * 0.5;
                double rr = radius * 0.75;
                blobs.add(new double[]{cx + Math.cos(a) * rr, topY + 2 + r.nextInt(3), cz + Math.sin(a) * rr, Math.max(3.5, radius * 0.5) + 1, 0.55});
            }
            blobs.add(new double[]{cx, topY + 1 + coneH * 0.35, cz, radius * 0.6 + 1.5, 0.6});
        } else {
            // tres troncos sobre el parapeto que sostienen una copa plana encima del mirador
            int pillars = 0;
            for (int tries = 0; tries < 30 && pillars < 3; tries++) {
                double a = pillars * Math.PI * 2 / 3 + r.nextDouble() * 0.6;
                if (avoided(a, 6)) continue;
                int px = cx + (int) Math.round(Math.cos(a) * (radius + 0.6)), pz = cz + (int) Math.round(Math.sin(a) * (radius + 0.6));
                limbs.add(new int[]{px, topY + 1, pz, px, topY + 8, pz});
                pillars++;
            }
            blobs.add(new double[]{cx, topY + 10, cz, radius + 3.5, 0.28});
            blobs.add(new double[]{cx, topY + 12, cz, radius * 0.6 + 1, 0.5});
            for (int k = 0; k < 6; k++) {
                double a = r.nextDouble() * Math.PI * 2;
                hangs.add(new int[]{cx + (int) Math.round(Math.cos(a) * (radius + 2)), topY + 9, cz + (int) Math.round(Math.sin(a) * (radius + 2)), r.range(2, 5), k % 2});
            }
        }
        // hojas (solo en el aire: no tocan muros ni tejado)
        for (double[] b : blobs) {
            int rr = (int) Math.ceil(b[3]);
            int ry = Math.max(1, (int) Math.ceil(b[3] * b[4]));
            int x0 = Math.max((int) Math.floor(b[0]) - rr, c.minX), x1 = Math.min((int) Math.floor(b[0]) + rr, c.maxX);
            int z0 = Math.max((int) Math.floor(b[2]) - rr, c.minZ), z1 = Math.min((int) Math.floor(b[2]) + rr, c.maxZ);
            for (int x = x0; x <= x1; x++)
                for (int z = z0; z <= z1; z++)
                    for (int y = (int) b[1] - ry; y <= (int) b[1] + ry; y++) {
                        double nx = (x + 0.5 - b[0]) / b[3], ny = (y - b[1]) / (b[3] * b[4]), nz = (z + 0.5 - b[2]) / b[3];
                        double d = nx * nx + ny * ny + nz * nz + tn.value(x * 0.2, y * 0.2, z * 0.2) * 0.2;
                        if (d > 1.0 || Hash.f(x, y, z, seed + 3) > 1.0 - d * d * 0.5) continue;
                        // nunca dentro de la torre, ni dentro del cono, ni sobre el mirador
                        double dc = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
                        if (dc <= radius + 0.5 && y <= topY + (top == Top.PLATFORM ? 5 : 0)) continue;
                        if (top == Top.CONE) {
                            double base = radius + 1.6;
                            if (dc <= base && y <= topY + 1 + (base - dc) * coneSlope) continue;
                        }
                        c.setIfAir(x, y, z, leaf(x, y, z));
                    }
        }
        // ramas y troncos
        for (int[] l : limbs) {
            int dx = l[3] - l[0], dy = l[4] - l[1], dz = l[5] - l[2];
            int n = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
            Direction.Axis axis = Math.abs(dy) >= Math.max(Math.abs(dx), Math.abs(dz)) ? Direction.Axis.Y : (Math.abs(dx) >= Math.abs(dz) ? Direction.Axis.X : Direction.Axis.Z);
            for (int i = 0; i <= n; i++) {
                double t = n == 0 ? 0 : i / (double) n;
                int x = (int) Math.round(l[0] + dx * t), y = (int) Math.round(l[1] + dy * t), z = (int) Math.round(l[2] + dz * t);
                c.set(x, y, z, bark(x, y, z, axis));
                if (t < 0.5 && radius >= 9) {
                    c.set(x, y + 1, z, bark(x, y + 1, z, axis));
                }
            }
        }
        // raices en la base (bajas, se pueden pasar por encima)
        int nr = 5;
        int roots = 0;
        for (int tries = 0; tries < 40 && roots < nr; tries++) {
            double a = roots * Math.PI * 2 / nr + r.nextDouble() * 0.8;
            if (avoided(a, 7)) continue;
            int len = r.range(4, 7);
            for (int k = 0; k <= len + 1; k++) {
                double dist = radius + 0.5 + k;
                int x = cx + (int) Math.round(Math.cos(a) * dist), z = cz + (int) Math.round(Math.sin(a) * dist);
                int y = k <= 1 ? groundY + 1 : (k < len ? groundY : groundY - 1);
                c.set(x, y, z, bark(x, y, z, Direction.Axis.Y));
                if (k <= 1) c.set(x, groundY, z, bark(x, groundY, z, Direction.Axis.Y));
            }
            roots++;
        }
        // colgantes
        for (int[] h : hangs) {
            if (!c.inXZ(h[0], h[2])) continue;
            int start = Integer.MIN_VALUE;
            for (int y = h[1] + 8; y >= h[1] - 3; y--) {
                if (!c.isAir(h[0], y, h[2])) {
                    start = y - 1;
                    break;
                }
            }
            if (start == Integer.MIN_VALUE) continue;
            for (int k = 0; k < h[3]; k++) {
                int y = start - k;
                if (!c.isAir(h[0], y, h[2])) break;
                boolean tip = k == h[3] - 1;
                BlockState s = h[4] == 0 ? (tip ? Pal.SOUL_LANTERN_HANG : Pal.CHAIN)
                        : (tip ? Blocks.CAVE_VINES.defaultBlockState().setValue(CaveVines.BERRIES, true)
                        : Blocks.CAVE_VINES_PLANT.defaultBlockState().setValue(CaveVines.BERRIES, false));
                c.set(h[0], y, h[2], s);
            }
        }
    }
}
