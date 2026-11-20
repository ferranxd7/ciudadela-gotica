package com.ciudadela.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Torre cuadrada altisima con aguja octogonal (la gran aguja del castillo).
 * Tambien sirve para los campanarios de la catedral.
 */
public class GreatTower extends Element {
    private final int cx, cz, baseY, half, towerTop, spireH, floorStep;
    private final boolean belfry;
    private Direction doorSide = Direction.SOUTH;
    private int doorY;

    public GreatTower(long seed, int cx, int cz, int baseY, int half, int towerTop, int spireH, boolean belfry) {
        super(seed);
        this.cx = cx;
        this.cz = cz;
        this.baseY = baseY;
        this.half = half;
        this.towerTop = towerTop;
        this.spireH = spireH;
        this.belfry = belfry;
        this.floorStep = 12;
        this.doorY = baseY;
        bounds(cx - half - 3, baseY - 2, cz - half - 3, cx + half + 3, towerTop + spireH + 8, cz + half + 3);
    }

    public GreatTower door(Direction side, int y) {
        this.doorSide = side;
        this.doorY = y;
        return this;
    }

    public static int oct(int dx, int dz) {
        int ax = Math.abs(dx), az = Math.abs(dz);
        return Math.max(Math.max(ax, az), (int) Math.round((ax + az) * 0.7071));
    }

    @Override
    public void build(Canvas c) {
        int H = half;
        for (int x = Math.max(cx - H - 2, c.minX); x <= Math.min(cx + H + 2, c.maxX); x++)
            for (int z = Math.max(cz - H - 2, c.minZ); z <= Math.min(cz + H + 2, c.maxZ); z++) {
                int dx = x - cx, dz = z - cz;
                int ax = Math.abs(dx), az = Math.abs(dz);
                int m = Math.max(ax, az);
                if (m > H + 2) continue;
                boolean cornerZone = ax >= H - 1 && az >= H - 1;
                if (m > H) {
                    // pilastras de esquina que suben como pinaculos
                    if (ax >= H - 1 && az >= H - 1) {
                        c.column(x, z, baseY - 1, towerTop + 6, Pal::wall);
                        c.set(x, towerTop + 7, z, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
                    } else if (m == H + 1 && (Math.min(ax, az) % 6 == 0)) {
                        c.column(x, z, baseY - 1, towerTop - 4, Pal::wall);
                        c.set(x, towerTop - 3, z, Pal.stairs(Blocks.DEEPSLATE_BRICK_STAIRS, Pal.dirOf(-dx * (ax > az ? 1 : 0), -dz * (az > ax ? 1 : 0)), false));
                    }
                    continue;
                }
                if (m >= H - 1) {
                    for (int y = baseY - 1; y <= towerTop; y++) {
                        int rel = y - baseY;
                        boolean band = rel % floorStep == 0;
                        BlockState s = band ? Pal.trim(x, y, z) : (cornerZone ? Pal.BASALT : Pal.wall(x, y, z));
                        // ventanas ojivales en las caras
                        int along = ax >= az ? dz : dx;
                        int aa = Math.abs(along);
                        if (!cornerZone && m == H || (!cornerZone && m == H - 1)) {
                            int fr = rel % floorStep;
                            boolean colOk = aa % 6 == 3 || (aa % 6 == 2 && H >= 10) || (aa % 6 == 4 && H >= 10);
                            int topRow = (aa % 6 == 3) ? 8 : 7;
                            if (colOk && fr >= 3 && fr <= topRow && rel > 4) s = Pal.glass(x, y / 4, z);
                            if (belfry && y >= towerTop - 14 && y <= towerTop - 4 && aa <= H - 4) s = (aa % 3 == 0) ? Pal.wall(x, y, z) : Pal.AIR;
                        }
                        c.set(x, y, z, s);
                    }
                    // almenas
                    c.set(x, towerTop + 1, z, Pal.wall(x, towerTop + 1, z));
                    if (((ax + az) & 2) == 0) c.set(x, towerTop + 2, z, Pal.wall(x, towerTop + 2, z));
                } else {
                    c.column(x, z, baseY, towerTop - 1, Pal.AIR);
                    boolean well = Spiral.inWell(dx, dz);
                    for (int y = baseY + floorStep; y < towerTop; y += floorStep)
                        if (!well) c.set(x, y - 1, z, Pal.woodFloor(x, y, z));
                    c.set(x, baseY - 1, z, Pal.DS_TILES);
                    c.set(x, towerTop, z, well ? Pal.AIR : Pal.DS_TILES);
                }
            }
        // puerta
        Direction side = doorSide.getClockWise();
        for (int k = -1; k <= 1; k++) {
            for (int d = H - 2; d <= H + 2; d++) {
                int x = cx + doorSide.getStepX() * d + side.getStepX() * k, z = cz + doorSide.getStepZ() * d + side.getStepZ() * k;
                for (int y = doorY; y <= doorY + 3; y++) c.set(x, y, z, Pal.AIR);
                c.set(x, doorY - 1, z, Pal.DS_TILES);
            }
        }
        Spiral.build(c, cx, cz, baseY, towerTop, (int) (seed & 15), Blocks.DEEPSLATE_TILE_STAIRS, Pal::wall);
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) c.set(cx + dx, towerTop + 3, cz + dz, Pal.AIR);
        if (belfry) {
            c.set(cx, towerTop - 5, cz + 3, Pal.PB);
            c.set(cx, towerTop - 6, cz + 3, Pal.b(Blocks.BELL).setValue(net.minecraft.world.level.block.BellBlock.ATTACHMENT,
                    net.minecraft.world.level.block.state.properties.BellAttachType.CEILING));
        }
        spire(c);
    }

    private void spire(Canvas c) {
        int sb = towerTop + 1;
        double R0 = half - 1.5;
        for (int x = Math.max(cx - half, c.minX); x <= Math.min(cx + half, c.maxX); x++)
            for (int z = Math.max(cz - half, c.minZ); z <= Math.min(cz + half, c.maxZ); z++) {
                int dx = x - cx, dz = z - cz;
                int m = oct(dx, dz);
                if (m > R0 + 0.5) continue;
                for (int y = sb; y <= sb + spireH; y++) {
                    double rad = R0 * (1 - (y - sb) / (double) spireH);
                    if (m <= rad + 0.5 && m > rad - 1.7) {
                        int rel = y - sb;
                        BlockState s = (rel % 14 == 13) ? Pal.trim(x, y, z) : Pal.roof(x, y, z);
                        boolean edge = Math.abs(Math.abs(dx) - Math.abs(dz)) <= 0 && rel % 5 == 2;
                        if (edge) s = Pal.GILDED;
                        c.set(x, y, z, s);
                    }
                }
            }
        // buhardillas en la base de la aguja
        for (int i = 0; i < 4; i++) {
            Direction d = Direction.from2DDataValue(i);
            int dist = (int) R0 - 1;
            for (int k = -1; k <= 1; k++) {
                Direction s = d.getClockWise();
                int x = cx + d.getStepX() * dist + s.getStepX() * k, z = cz + d.getStepZ() * dist + s.getStepZ() * k;
                for (int y = sb + 3; y <= sb + 6; y++) c.set(x, y, z, k == 0 ? Pal.GLASS_RED : Pal.wall(x, y, z));
                c.set(x, sb + 7, z, Pal.stairs(Blocks.DEEPSLATE_TILE_STAIRS, k == 0 ? d.getOpposite() : (k < 0 ? s : s.getOpposite()), false));
            }
        }
        int apex = sb + spireH;
        c.column(cx, cz, apex - 2, apex + 1, Pal.GILDED);
        c.column(cx, cz, apex + 2, apex + 4, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
        c.set(cx, apex + 5, cz, Pal.b(Blocks.LIGHTNING_ROD));
        c.set(cx + 1, apex + 2, cz, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
        c.set(cx - 1, apex + 2, cz, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
    }
}
