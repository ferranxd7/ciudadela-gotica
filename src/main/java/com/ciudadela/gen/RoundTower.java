package com.ciudadela.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** Torre redonda con escalera de caracol, plantas, troneras y tejado conico o almenado. */
public class RoundTower extends Element {
    public enum Top { CONE, PLATFORM }

    public final int cx, cz, groundY, radius, topY;
    private final Top top;
    private final List<Integer> floorYs = new ArrayList<>();
    private final List<double[]> doors = new ArrayList<>(); // {angulo, y, ancho}
    private Room.Type roomType = Room.Type.STORAGE;
    private boolean mossy = false;
    private double coneSlope = 2.3;

    public RoundTower(long seed, int cx, int cz, int groundY, int radius, int height, Top top) {
        super(seed);
        this.cx = cx;
        this.cz = cz;
        this.groundY = groundY;
        this.radius = radius;
        this.topY = groundY + height;
        this.top = top;
        for (int y = groundY; y < topY - 6; y += 10) floorYs.add(y);
        int coneH = (int) Math.round((radius + 2) * coneSlope) + 6;
        bounds(cx - radius - 3, groundY - 8, cz - radius - 3, cx + radius + 3, topY + coneH, cz + radius + 3);
    }

    public RoundTower door(double angleRad, int y) {
        doors.add(new double[]{angleRad, y, 1.6});
        if (!floorYs.contains(y)) floorYs.add(y);
        return this;
    }

    /** Puerta mirando hacia un punto. */
    public RoundTower doorToward(int x, int z, int y) {
        return door(Math.atan2(z - cz, x - cx), y);
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
        int coneH = (int) Math.round((radius + 2) * coneSlope) + 6;
        bounds(cx - radius - 3, groundY - 8, cz - radius - 3, cx + radius + 3, topY + coneH, cz + radius + 3);
        return this;
    }

    private static double angDiff(double a, double b) {
        double d = Math.abs(a - b) % (Math.PI * 2);
        return d > Math.PI ? Math.PI * 2 - d : d;
    }

    private BlockState wallState(int x, int y, int z) {
        return mossy ? Pal.oldWall(x, y, z) : Pal.wall(x, y, z);
    }

    @Override
    public void build(Canvas c) {
        double R = radius + 0.5;
        double inner = radius - 1.5;
        int lastFloor = floorYs.stream().max(Integer::compare).orElse(groundY);
        for (int x = Math.max(cx - radius - 2, c.minX); x <= Math.min(cx + radius + 2, c.maxX); x++)
            for (int z = Math.max(cz - radius - 2, c.minZ); z <= Math.min(cz + radius + 2, c.maxZ); z++) {
                int dx = x - cx, dz = z - cz;
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > R + 1.2) continue;
                double ang = Math.atan2(dz, dx);
                if (d > R) {
                    // zocalo y matacanes
                    c.set(x, groundY - 1, z, Pal.trim(x, groundY, z));
                    c.set(x, groundY, z, Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Pal.dirOf(-dx, -dz), false));
                    if (top == Top.PLATFORM) {
                        c.set(x, topY - 1, z, Pal.stairs(Blocks.DEEPSLATE_BRICK_STAIRS, Pal.dirOf(-dx, -dz), true));
                        c.set(x, topY, z, Pal.DS_TILES);
                        c.set(x, topY + 1, z, wallState(x, topY + 1, z));
                        int bucket = (int) Math.floor((ang + Math.PI) / (Math.PI * 2) * (radius * 2.5));
                        if ((bucket & 1) == 0) c.set(x, topY + 2, z, wallState(x, topY + 2, z));
                    } else {
                        c.set(x, topY - 1, z, Pal.stairs(Blocks.DEEPSLATE_BRICK_STAIRS, Pal.dirOf(-dx, -dz), true));
                    }
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
                        c.set(x, y, z, slit ? Pal.AIR : band ? Pal.trim(x, y, z) : wallState(x, y, z));
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
        furnish(c);
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

    private void furnish(Canvas c) {
        Rng rng = new Rng(seed ^ 0xF00D);
        double inner = radius - 1.5;
        if (inner < 4) return;
        for (int fy : floorYs) {
            boolean chestDone = rng.chance(0.4);
            int fh = 1;
            // farol colgante
            c.set(cx + 3, fy + 6, cz, Pal.CHAIN);
            c.set(cx + 3, fy + 5, cz, Pal.SOUL_LANTERN_HANG);
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
                else if (roll < 7) c.set(x, fy, z, Pal.candles(Blocks.BLACK_CANDLE, 3));
                else if (roll < 8) c.set(x, fy, z, Pal.facing(Blocks.ANVIL, in.getClockWise()));
                else if (roll < 9) c.set(x, fy, z, Pal.COBWEB);
                else c.set(x, fy, z, Pal.BOOKSHELF);
            }
            if (fh == 0) break;
        }
    }
}
