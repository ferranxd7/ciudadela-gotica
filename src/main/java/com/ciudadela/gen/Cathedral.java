package com.ciudadela.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Catedral gotica: nave central con boveda, naves laterales, arbotantes, rosetón, portada,
 * dos campanarios con agujas y un abside con altar y organo. Fachada mirando al sur (+z).
 */
public class Cathedral extends Element {
    private final int cx, zN, zS, Y;
    private static final int NAVE = 11, AISLE = 24, HN = 40, HA = 20, BAY = 8;
    private final GreatTower west, east;

    public Cathedral(long seed, int cx, int zN, int zS, int groundY) {
        super(seed);
        this.cx = cx;
        this.zN = zN;
        this.zS = zS;
        this.Y = groundY;
        int tz = zS - 7;
        west = new GreatTower(seed + 1, cx - 17, tz, Y, 7, Y + 92, 42, true);
        east = new GreatTower(seed + 2, cx + 17, tz, Y, 7, Y + 92, 42, true);
        west.door(Direction.EAST, Y);
        east.door(Direction.WEST, Y);
        bounds(cx - AISLE - 4, Y - 2, zN - NAVE - 3, cx + AISLE + 4, Y + 92 + 42 + 10, zS + 3);
    }

    private int vault(int adx) {
        double k = adx / 10.5;
        return Y + 30 + (int) Math.round(8 * (1 - k * k));
    }

    @Override
    public void build(Canvas c) {
        for (int x = Math.max(x0, c.minX); x <= Math.min(x1, c.maxX); x++)
            for (int z = Math.max(z0, c.minZ); z <= Math.min(z1, c.maxZ); z++) column(c, x, z);
        interior(c);
        if (west.touchesChunk(c.minX >> 4, c.minZ >> 4)) west.build(c);
        if (east.touchesChunk(c.minX >> 4, c.minZ >> 4)) east.build(c);
        facade(c);
    }

    private void column(Canvas c, int x, int z) {
        int dxc = x - cx, adx = Math.abs(dxc);
        int pos = Math.floorMod(zS - z, BAY);
        boolean inTowerZone = z >= zS - 14 && adx >= 10;
        if (inTowerZone) return; // los campanarios se construyen aparte

        if (z < zN) {
            apse(c, x, z, dxc);
            return;
        }
        if (z > zS) return;
        // suelo
        if (adx <= AISLE) c.set(x, Y - 1, z, ((x + z) & 1) == 0 ? Pal.PB : Pal.DS_POLISHED);
        if (adx <= AISLE) c.column(x, z, Y - 5, Y - 2, Pal::foundation);

        if (adx <= NAVE - 1) {
            int v = vault(adx);
            c.column(x, z, Y, v - 1, Pal.AIR);
            c.column(x, z, v, Y + HN, (a, b, cc) -> Pal.wall(a, b, cc));
            // nervios de la boveda
            if (pos == 0) c.column(x, z, v, v + 1, Pal.trim(x, v, z));
            naveRoof(c, x, z, adx);
        } else if (adx == NAVE) {
            boolean pier = pos <= 1 || pos == 7;
            for (int y = Y; y <= Y + HN; y++) {
                BlockState s = Pal.wall(x, y, z);
                if (pier) s = (y == Y || y == Y + 14) ? Pal.PB_CHISELED : Pal.BASALT;
                else {
                    int ry = y - Y;
                    boolean arch = ry < 12 || (ry < 14 && pos >= 3 && pos <= 5) || (ry == 14 && pos == 4);
                    if (arch) s = Pal.AIR;
                    boolean clere = ry >= 23 && (ry <= 33 || (ry <= 35 && pos >= 3 && pos <= 5) || (ry == 36 && pos == 4)) && pos >= 2 && pos <= 6;
                    if (clere) s = Pal.glass(x, y / 3, z);
                }
                c.set(x, y, z, s);
            }
            naveRoof(c, x, z, adx);
            aisleRoof(c, x, z, adx);
        } else if (adx < AISLE) {
            c.column(x, z, Y, Y + HA - 3, Pal.AIR);
            c.column(x, z, Y + HA - 2, Y + HA, Pal::wall);
            if (z == zN) c.column(x, z, Y, Y + HA, Pal::wall);
            aisleRoof(c, x, z, adx);
            flying(c, x, z, adx, pos);
        } else if (adx == AISLE) {
            for (int y = Y - 1; y <= Y + HA; y++) {
                int ry = y - Y;
                boolean win = pos >= 3 && pos <= 5 && ry >= 3 && (ry <= 13 || (ry <= 15 && pos == 4));
                c.set(x, y, z, win ? Pal.glass(x, y / 3, z) : Pal.wall(x, y, z));
            }
            aisleRoof(c, x, z, adx);
            flying(c, x, z, adx, pos);
        } else if (adx <= AISLE + 2) {
            if (pos <= 1) {
                c.column(x, z, Y - 4, Y + HA + 4, Pal::wall);
                c.set(x, Y + HA + 5, z, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
                flying(c, x, z, adx, pos);
            } else {
                c.column(x, z, Y - 4, Y, Pal::foundation);
            }
        }
    }

    private void naveRoof(Canvas c, int x, int z, int adx) {
        int base = Y + HN + 1;
        int h = base + (NAVE + 1 - adx);
        for (int y = Math.max(base, h - 2); y < h; y++) c.set(x, y, z, Pal.roof(x, y, z));
        if (adx == 0) {
            c.set(x, h, z, Pal.slab(Blocks.DEEPSLATE_TILE_SLAB, false));
            if (Math.floorMod(z, 6) == 0) c.set(x, h + 1, z, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
        } else c.set(x, h, z, Pal.stairs(Blocks.DEEPSLATE_TILE_STAIRS, (x < cx) ? Direction.EAST : Direction.WEST, false));
        c.column(x, z, Y + HN, base - 1, Pal::wall);
    }

    private void aisleRoof(Canvas c, int x, int z, int adx) {
        if (adx < NAVE) return;
        int h = Y + HA + 1 + (AISLE + 1 - adx) / 2;
        for (int y = Y + HA + 1; y < h; y++) c.set(x, y, z, Pal.roof(x, y, z));
        boolean half = ((AISLE + 1 - adx) & 1) == 1;
        c.set(x, h, z, half ? Pal.slab(Blocks.DEEPSLATE_TILE_SLAB, false) : Pal.stairs(Blocks.DEEPSLATE_TILE_STAIRS, (x < cx) ? Direction.EAST : Direction.WEST, false));
    }

    /** Arbotantes: arcos desde el contrafuerte exterior hasta el muro de la nave. */
    private void flying(Canvas c, int x, int z, int adx, int pos) {
        if (pos > 1 || adx <= NAVE) return;
        int y = Y + HA + 5 + (int) Math.round((AISLE + 2 - adx) * 10.0 / 15.0);
        c.set(x, y, z, Pal.wall(x, y, z));
        c.set(x, y - 1, z, Pal.stairs(Blocks.DEEPSLATE_BRICK_STAIRS, (x < cx) ? Direction.WEST : Direction.EAST, true));
    }

    private void apse(Canvas c, int x, int z, int dxc) {
        double dz = z - zN;
        double d = Math.sqrt(dxc * dxc + dz * dz);
        if (d > NAVE + 0.6) return;
        c.column(x, z, Y - 5, Y - 2, Pal::foundation);
        c.set(x, Y - 1, z, ((x + z) & 1) == 0 ? Pal.PB : Pal.DS_POLISHED);
        if (d <= NAVE - 0.5) {
            int v = vault((int) Math.round(d));
            c.column(x, z, Y, v - 1, Pal.AIR);
            c.column(x, z, v, Y + HN, Pal::wall);
        } else {
            double ang = Math.atan2(dz, dxc);
            int bucket = (int) Math.floor((ang + Math.PI) / Math.PI * 7);
            double frac = (ang + Math.PI) / Math.PI * 7 - bucket;
            for (int y = Y; y <= Y + HN; y++) {
                int ry = y - Y;
                boolean win = frac > 0.3 && frac < 0.7 && ry >= 4 && ry <= 30;
                c.set(x, y, z, win ? Pal.glass(x, y / 3, z) : Pal.wall(x, y, z));
            }
        }
        // tejado semiconico
        double base = NAVE + 1.6;
        int h = Y + HN + 1 + (int) Math.round(base - d);
        for (int y = Y + HN + 1; y <= h; y++) c.set(x, y, z, Pal.roof(x, y, z));
    }

    private void facade(Canvas c) {
        int zf0 = zS - 2;
        int top = Y + HN + 12;
        for (int x = cx - 9; x <= cx + 9; x++) {
            int dxc = x - cx, adx = Math.abs(dxc);
            for (int z = zf0; z <= zS; z++) {
                if (!c.inXZ(x, z)) continue;
                int gable = top - adx;
                for (int y = Y - 1; y <= gable; y++) {
                    BlockState s = Pal.wall(x, y, z);
                    int ry = y - Y;
                    // roseton
                    double rx = dxc, rr = y - (Y + 29);
                    double rd = Math.sqrt(rx * rx + rr * rr);
                    if (rd <= 8.4) {
                        double ang = Math.atan2(rr, rx);
                        int spoke = (int) Math.round((ang + Math.PI) / (Math.PI * 2) * 12);
                        double spokeAng = spoke / 12.0 * Math.PI * 2 - Math.PI;
                        boolean onSpoke = Math.abs(ang - spokeAng) * rd < 0.6 && rd > 2.5;
                        if (rd > 7.6) s = Pal.PB_CHISELED;
                        else if (rd < 1.5) s = Pal.GLASS_YELLOW;
                        else if (onSpoke) s = Pal.PB;
                        else if (rd < 4.5) s = (spoke % 2 == 0) ? Pal.GLASS_RED : Pal.GLASS_ORANGE;
                        else s = (spoke % 3 == 0) ? Pal.GLASS_BLUE : (spoke % 3 == 1 ? Pal.GLASS_PURPLE : Pal.GLASS_RED);
                        if (z != zS - 1) s = rd > 7.6 ? Pal.PB_CHISELED : (z == zS ? Pal.AIR : Pal.AIR);
                        if (z == zS - 1 || rd > 7.6) {
                            c.set(x, y, z, s);
                            continue;
                        }
                    }
                    // portada
                    boolean portal = adx <= 4 && (ry < 10 || adx <= 4 - (ry - 9)) && ry >= 0;
                    boolean frame = (adx == 5 && ry <= 11) || (adx == 4 - (ry - 10) && ry >= 10 && ry <= 15 && z == zS);
                    if (portal) s = Pal.AIR;
                    else if (frame) s = Pal.PB_CHISELED;
                    // galeria de reyes (hornacinas)
                    if (ry >= 18 && ry <= 20 && adx % 3 == 1 && z == zS) s = Pal.AIR;
                    if (y == gable && z == zS) s = Pal.stairs(Blocks.DEEPSLATE_BRICK_STAIRS, dxc < 0 ? Direction.EAST : Direction.WEST, false);
                    c.set(x, y, z, s);
                }
                if (z == zS && adx % 3 == 1) {
                    c.set(x, Y + 18, z, Pal.PB);
                    c.set(x, Y + 19, z, Pal.b(Blocks.SKELETON_SKULL));
                }
            }
        }
        c.set(cx, top + 1, zS - 1, Pal.GILDED);
        c.column(cx, zS - 1, top + 2, top + 4, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
        // escalinata
        for (int k = 1; k <= 3; k++)
            for (int x = cx - 6 - k; x <= cx + 6 + k; x++)
                c.set(x, Y - 1, zS + k, Pal.PB_BRICKS);
    }

    private void interior(Canvas c) {
        // bancos
        for (int z = zN + 22; z <= zS - 17; z += 2)
            for (int x = cx - 8; x <= cx + 8; x++) {
                if (Math.abs(x - cx) <= 1) {
                    c.set(x, Y, z, Pal.b(Blocks.RED_CARPET));
                    c.set(x, Y, z + 1, Pal.b(Blocks.RED_CARPET));
                    continue;
                }
                c.set(x, Y, z, Pal.stairs(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, false));
            }
        for (int z = zN + 1; z <= zS - 2; z++) if (z < zN + 22 || z > zS - 17) c.set(cx, Y, z, Pal.b(Blocks.RED_CARPET));
        // presbiterio y altar
        for (int x = cx - 9; x <= cx + 9; x++)
            for (int z = zN - 8; z <= zN + 10; z++) {
                double d = Math.sqrt((x - cx) * (x - cx) + Math.max(0, zN - z) * Math.max(0, zN - z));
                if (d > NAVE - 1.5) continue;
                c.set(x, Y, z, z == zN + 10 ? Pal.stairs(Blocks.POLISHED_BLACKSTONE_STAIRS, Direction.NORTH, false) : Pal.PB_BRICKS);
            }
        int ay = Y + 1, az = zN - 2;
        for (int x = cx - 3; x <= cx + 3; x++) {
            c.set(x, ay, az, Pal.PB_CHISELED);
            c.set(x, ay + 1, az, Pal.candles(Math.abs(x - cx) == 3 ? Blocks.RED_CANDLE : Blocks.WHITE_CANDLE, 4));
        }
        c.set(cx, ay + 1, az, Pal.GILDED);
        c.column(cx, az - 3, ay, ay + 9, Pal.PB);
        for (int x = cx - 3; x <= cx + 3; x++) c.set(x, ay + 7, az - 3, Pal.PB);
        c.set(cx, ay + 10, az - 3, Pal.GILDED);
        // organo: tubos en el abside
        for (int k = -8; k <= 8; k++) {
            double ang = Math.PI * (0.5 + k / 20.0);
            int x = cx + (int) Math.round(Math.cos(ang) * 9.3);
            int z = zN - (int) Math.round(Math.sin(ang) * 9.3);
            int hh = 6 + (8 - Math.abs(k)) * 2 + (k & 1) * 2;
            c.column(x, z, ay, ay + hh, Pal.b(Blocks.POLISHED_DEEPSLATE_WALL));
            c.set(x, ay + hh + 1, z, Pal.GILDED);
        }
        // candelabros colgantes
        for (int z = zN + 6; z <= zS - 18; z += 12) {
            int v = vault(0);
            for (int y = v - 1; y >= Y + 20; y--) c.set(cx, y, z, Pal.CHAIN);
            int y0 = Y + 19;
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) > 3) continue;
                    c.set(cx + dx, y0, z + dz, Pal.slab(Blocks.POLISHED_BLACKSTONE_SLAB, true));
                    if ((Math.abs(dx) == 2 || Math.abs(dz) == 2)) c.set(cx + dx, y0 + 1, z + dz, Pal.candles(Blocks.WHITE_CANDLE, 4));
                }
            c.set(cx, y0 - 1, z, Pal.SOUL_LANTERN_HANG);
        }
        // tumbas y velas en las naves laterales
        for (int z = zN + 4; z <= zS - 18; z += BAY) {
            for (int s = -1; s <= 1; s += 2) {
                int x = cx + s * 20;
                c.set(x, Y, z, Pal.PB_BRICKS);
                c.set(x, Y, z + 1, Pal.PB_BRICKS);
                c.set(x, Y + 1, z, Pal.b(Blocks.SKELETON_SKULL));
                c.set(x, Y + 1, z + 1, Pal.slab(Blocks.POLISHED_BLACKSTONE_SLAB, false));
                c.set(x + s * 3, Y, z + 3, Pal.candles(Blocks.BLACK_CANDLE, 3));
                c.set(cx + s * 17, Y + HA - 3, z + 2, Pal.SOUL_LANTERN_HANG);
            }
        }
        c.chest(cx + 5, Y + 1, zN + 2, Direction.WEST, Decor.loot("tesoro"));
    }
}
