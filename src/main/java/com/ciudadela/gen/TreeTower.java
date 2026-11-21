package com.ciudadela.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * La gran torre central convertida en "torre-arbol": un torreon de piedra envuelto por un arbol
 * gigantesco. La madera sube en espiral por la torre, le salen ramas con hojas, raices que bajan por
 * el torreon y, arriba del todo, la SALA DEL JEFE: una gran sala circular con cupula, sostenida por
 * el arbol, con el trono del caballero no-muerto. Encima, la copa del arbol atravesada por la aguja.
 */
public class TreeTower extends Element {
    private final int cx, cz, baseY, half, towerTop, groundY;
    private final Rect keep;
    /** Sala del jefe. */
    public final int hallFloor;          // nivel que se pisa en la sala
    public static final int HALL_R = 21;  // radio exterior del muro
    public static final int HALL_H = 17;  // altura de los muros por dentro
    private static final int DOME = 9;    // altura extra de la cupula
    private final int domeTop, crownBase;
    private final List<double[]> blobs = new ArrayList<>();   // x,y,z,r,aplastado
    private final List<int[]> limbs = new ArrayList<>();      // x0,y0,z0,x1,y1,z1,grosor
    private final List<int[]> hangs = new ArrayList<>();      // x,y,z,largo,tipo
    private final Noise noise;
    private static final int SPIRE_R = 5, SPIRE_H = 62;

    public TreeTower(long seed, int cx, int cz, int baseY, int half, int towerTop, Rect keep, int groundY) {
        super(seed);
        this.cx = cx;
        this.cz = cz;
        this.baseY = baseY;
        this.half = half;
        this.towerTop = towerTop;
        this.keep = keep;
        this.groundY = groundY;
        this.hallFloor = towerTop + 1;
        this.domeTop = hallFloor + HALL_H + DOME;
        this.crownBase = domeTop;
        this.noise = new Noise(seed);
        Rng r = new Rng(seed);
        // ramas a media altura por el lado de madera
        for (int y = towerTop - 95; y <= towerTop - 28; y += 13) {
            double a = woodAngle(y) + (r.nextDouble() - 0.5) * 0.6;
            int len = r.range(11, 18);
            int sx = cx + (int) Math.round(Math.cos(a) * (half + 1)), sz = cz + (int) Math.round(Math.sin(a) * (half + 1));
            int ex = cx + (int) Math.round(Math.cos(a) * (half + len)), ez = cz + (int) Math.round(Math.sin(a) * (half + len));
            int ey = y + r.range(5, 10);
            limbs.add(new int[]{sx, y, sz, ex, ey, ez, 2});
            blobs.add(new double[]{ex, ey + 2, ez, 5 + r.nextDouble() * 2, 0.6});
            for (int k = 0; k < 4; k++) hangs.add(new int[]{ex + r.range(-4, 4), ey, ez + r.range(-4, 4), r.range(3, 8), k % 2});
        }
        // ramas que salen del borde de la sala hacia la copa
        int nb = 8;
        for (int i = 0; i < nb; i++) {
            double a = i * Math.PI * 2 / nb + r.nextDouble() * 0.45;
            int len = r.range(30, 38);
            int sy = hallFloor + r.range(8, 14);
            int sx = cx + (int) Math.round(Math.cos(a) * (HALL_R + 1)), sz = cz + (int) Math.round(Math.sin(a) * (HALL_R + 1));
            int ex = cx + (int) Math.round(Math.cos(a) * len), ez = cz + (int) Math.round(Math.sin(a) * len);
            int ey = domeTop + r.range(4, 12);
            limbs.add(new int[]{sx, sy, sz, ex, ey, ez, 2});
            blobs.add(new double[]{ex, ey + 3, ez, 9 + r.nextDouble() * 3, 0.55});
            for (int k = 0; k < 8; k++) {
                double aa = r.nextDouble() * Math.PI * 2, dd = r.nextDouble() * 9;
                hangs.add(new int[]{ex + (int) Math.round(Math.cos(aa) * dd), ey - 1, ez + (int) Math.round(Math.sin(aa) * dd), r.range(4, 11), k % 3});
            }
        }
        // ramas que suben desde la cupula hacia el centro de la copa
        for (int i = 0; i < 4; i++) {
            double a = i * Math.PI / 2 + Math.PI / 4;
            int ex = cx + (int) Math.round(Math.cos(a) * 14), ez = cz + (int) Math.round(Math.sin(a) * 14);
            limbs.add(new int[]{cx + (int) Math.round(Math.cos(a) * 8), domeTop - 2, cz + (int) Math.round(Math.sin(a) * 8), ex, domeTop + 12, ez, 2});
        }
        blobs.add(new double[]{cx, domeTop + 14, cz, 21, 0.42});
        blobs.add(new double[]{cx, domeTop + 24, cz, 12, 0.55});
        int reach = 50;
        bounds(Math.min(cx - reach, keep.x0 - 9), groundY - 2, Math.min(cz - reach, keep.z0 - 9),
                Math.max(cx + reach, keep.x1 + 9), domeTop + SPIRE_H + 12, Math.max(cz + reach, keep.z1 + 9));
    }

    /** Donde aparecera el jefe (fase 3): en la tarima, delante del trono. */
    public int[] bossSpot() {
        return new int[]{cx, hallFloor + 2, cz - HALL_R + 7};
    }

    public int centerX() {
        return cx;
    }

    public int centerZ() {
        return cz;
    }

    public int base() {
        return baseY;
    }

    /** Angulo donde la madera es mas abundante a la altura y (gira en espiral). */
    private double woodAngle(int y) {
        return (Math.PI / 2 - y * 0.045) / 2;
    }

    /** 0..1: cuanto "arbol" hay en este bloque del muro. */
    private double woodiness(int x, int y, int z) {
        double t = (y - baseY) / (double) (towerTop - baseY);
        double ang = Math.atan2(z - cz, x - cx);
        double spiral = Math.sin(ang * 2 + y * 0.045);
        double n = noise.value(x * 0.15, y * 0.08, z * 0.15) * 0.25;
        return t * 1.25 - 0.2 + 0.45 * spiral + n;
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
        if (p < 45) return Pal.leaves(Blocks.DARK_OAK_LEAVES);
        if (p < 65) return Pal.leaves(Blocks.AZALEA_LEAVES);
        if (p < 77) return Pal.leaves(Blocks.FLOWERING_AZALEA_LEAVES);
        if (p < 90) return Pal.leaves(Blocks.MANGROVE_LEAVES);
        BlockState mod = Mods.state("biomesoplenty:magic_leaves", "persistent=true");
        if (mod != null && p < 96) return mod;
        return Pal.leaves(Blocks.SPRUCE_LEAVES);
    }

    @Override
    public void build(Canvas c) {
        canopy(c);
        tower(c);
        bowl(c);
        hall(c);
        for (int[] l : limbs) limb(c, l);
        roots(c);
        spire(c);
        hanging(c);
    }

    // ------------------------------------------------------------ torre con madera en espiral
    private void tower(Canvas c) {
        int H = half;
        for (int x = Math.max(cx - H - 4, c.minX); x <= Math.min(cx + H + 4, c.maxX); x++)
            for (int z = Math.max(cz - H - 4, c.minZ); z <= Math.min(cz + H + 4, c.maxZ); z++) {
                int dx = x - cx, dz = z - cz;
                int ax = Math.abs(dx), az = Math.abs(dz);
                int m = Math.max(ax, az);
                boolean cornerZone = ax >= H - 1 && az >= H - 1;
                if (m > H) {
                    // corteza que engorda el tronco por fuera
                    for (int y = baseY - 1; y <= towerTop; y++) {
                        double w = woodiness(x, y, z);
                        int bulge = (int) Math.floor((w - 0.45) * 7 * (0.6 + 0.4 * noise.value(x * 0.3, y * 0.1, z * 0.3)));
                        if (m - H <= bulge) c.set(x, y, z, bark(x, y, z, Direction.Axis.Y));
                    }
                    continue;
                }
                if (m >= H - 1) {
                    for (int y = baseY - 1; y <= towerTop; y++) {
                        int rel = y - baseY;
                        double w = woodiness(x, y, z);
                        BlockState s;
                        if (w > 0.5) s = bark(x, y, z, Direction.Axis.Y);
                        else {
                            s = rel % 12 == 0 ? Pal.trim(x, y, z) : (cornerZone ? Pal.BASALT : Pal.wall(x, y, z));
                            int aa = Math.abs(ax >= az ? dz : dx);
                            int fr = rel % 12;
                            if (!cornerZone && (aa % 6 == 3) && fr >= 3 && fr <= 8 && rel > 4 && w < 0.35) s = Pal.glass(x, y / 4, z);
                        }
                        c.set(x, y, z, s);
                    }
                } else {
                    c.column(x, z, baseY, towerTop - 1, Pal.AIR);
                    boolean well = Spiral.inWell(dx, dz);
                    for (int y = baseY + 12; y < towerTop - 4; y += 12)
                        if (!well) c.set(x, y - 1, z, Pal.woodFloor(x, y, z));
                    c.set(x, baseY - 1, z, Pal.DS_TILES);
                }
            }
        // puerta sur
        for (int k = -1; k <= 1; k++)
            for (int d = H - 2; d <= H + 6; d++) {
                int x = cx + k, z = cz + d;
                for (int y = baseY; y <= baseY + 3; y++) c.set(x, y, z, Pal.AIR);
                c.set(x, baseY - 1, z, Pal.DS_TILES);
            }
        // la escalera sube hasta el suelo de la sala del jefe (el ultimo escalon queda a ras del suelo)
        Spiral.build(c, cx, cz, baseY, towerTop, (int) (seed & 15), Blocks.DEEPSLATE_TILE_STAIRS, Pal::wall);
        for (int y = baseY + 12; y < towerTop - 4; y += 12) {
            for (int[] p : new int[][]{{6, 6}, {-6, 6}, {6, -6}, {-6, -6}}) {
                c.set(cx + p[0], y + 9, cz + p[1], Pal.CHAIN);
                c.set(cx + p[0], y + 8, cz + p[1], Pal.SOUL_LANTERN_HANG);
            }
        }
    }

    // ------------------------------------------------------------ cuenco de madera que sostiene la sala
    private void bowl(Canvas c) {
        int depth = 16;
        int y0 = towerTop - depth;
        for (int x = Math.max(cx - HALL_R - 2, c.minX); x <= Math.min(cx + HALL_R + 2, c.maxX); x++)
            for (int z = Math.max(cz - HALL_R - 2, c.minZ); z <= Math.min(cz + HALL_R + 2, c.maxZ); z++) {
                int dx = x - cx, dz = z - cz;
                if (Math.max(Math.abs(dx), Math.abs(dz)) <= half) continue; // la torre ya esta
                double d = Math.sqrt(dx * dx + dz * dz);
                double ang = Math.atan2(dz, dx);
                for (int y = y0; y < towerTop; y++) {
                    double k = (y - y0) / (double) depth;
                    double rr = half * 1.05 + Math.pow(k, 0.8) * (HALL_R + 0.6 - half * 1.05) + noise.value(x * 0.2, y * 0.2, z * 0.2) * 1.2;
                    if (d > rr) continue;
                    // costillas de piedra cada 45 grados, el resto madera viva
                    boolean rib = Math.abs(Math.sin(ang * 4)) < 0.08 + 0.02 * k;
                    c.set(x, y, z, rib ? Pal.PB_BRICKS : bark(x, y, z, Direction.Axis.Y));
                }
            }
    }

    // ------------------------------------------------------------ SALA DEL JEFE
    private int ceiling(double d) {
        double k = Math.min(1, d / (HALL_R - 1.0));
        return hallFloor + HALL_H - 1 + (int) Math.round(DOME * Math.sqrt(Math.max(0, 1 - k * k)));
    }

    private void hall(Canvas c) {
        int F = hallFloor;
        double R = HALL_R + 0.5;
        int[] dais = {cx, cz - HALL_R + 3};
        for (int x = Math.max(cx - HALL_R - 3, c.minX); x <= Math.min(cx + HALL_R + 3, c.maxX); x++)
            for (int z = Math.max(cz - HALL_R - 3, c.minZ); z <= Math.min(cz + HALL_R + 3, c.maxZ); z++) {
                int dx = x - cx, dz = z - cz;
                double d = Math.sqrt(dx * dx + dz * dz);
                double ang = Math.atan2(dz, dx);
                if (d > R + 1.6) continue;
                if (d > R) {
                    // costillas de madera que trepan por fuera del muro
                    for (int y = F - 1; y <= F + HALL_H + 2; y++)
                        if (Math.sin(ang * 5 + (y - F) * 0.16 + noise.value(x * 0.2, y * 0.1, z * 0.2)) > 0.86)
                            c.set(x, y, z, bark(x, y, z, Direction.Axis.Y));
                    continue;
                }
                int well = Math.max(Math.abs(dx), Math.abs(dz));
                // suelo (con el hueco de la escalera de 5x5 en el centro)
                if (well > 2) c.set(x, F - 1, z, hallFloorBlock(x, z, d, dx, dz));
                if (d > R - 2) {
                    // muro: pilastras cada 30 grados, ventanales goticos entre ellas
                    double sector = Math.PI / 6;
                    double rel = ang / sector;
                    double fracToPil = Math.abs(rel - Math.round(rel)) * sector * d; // distancia en bloques a la pilastra
                    double fracToWin = Math.abs((rel - Math.floor(rel)) - 0.5) * sector * d;
                    boolean outer = d > R - 1;
                    for (int y = F - 1; y <= F + HALL_H; y++) {
                        int ry = y - F;
                        BlockState s;
                        if (fracToPil < 1.6) {
                            s = (ry == 0 || ry == HALL_H - 1) ? Pal.PB_CHISELED : Pal.BASALT;
                        } else {
                            boolean win = ry >= 3 && ry <= 12 && fracToWin < 2.6 && fracToWin > 0.5;
                            boolean arch = (ry == 13 && fracToWin < 1.6 && fracToWin > 0.5) || (ry == 14 && fracToWin < 0.9);
                            if ((win || arch) && outer) {
                                BlockState mos = ModDeco.mosaic(x, z, y, F + 3, F + 13);
                                s = mos != null ? mos : (Hash.pct(x, y / 3, z, 7) < 55 ? Pal.GLASS_RED : Pal.GLASS_PURPLE);
                            } else if (win || arch) {
                                s = Pal.AIR;
                            } else {
                                double w = 0.5 + 0.38 * Math.sin(ang * 3 + y * 0.21) + noise.value(x * 0.25, y * 0.12, z * 0.25) * 0.3;
                                s = w > 0.8 ? bark(x, y, z, Direction.Axis.Y) : (ry % 8 == 7 ? Pal.trim(x, y, z) : Pal.wall(x, y, z));
                            }
                        }
                        c.set(x, y, z, s);
                    }
                    // muro de la cupula hasta el arranque
                    for (int y = F + HALL_H + 1; y <= ceiling(d) + 1; y++) c.set(x, y, z, Pal.wall(x, y, z));
                    continue;
                }
                // interior: aire hasta la cupula
                int ceil = ceiling(d);
                if (well > 2) c.column(x, z, F, ceil - 1, Pal.AIR);
                else c.column(x, z, F + 3, ceil - 1, Pal.AIR);
                // cupula de dos bloques con nervios de madera
                double sector = Math.PI / 6;
                double rel = ang / sector;
                boolean rib = Math.abs(rel - Math.round(rel)) * sector * Math.max(d, 1) < 0.9;
                for (int y = ceil; y <= ceil + 1; y++)
                    c.set(x, y, z, rib ? Pal.log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Y) : Pal.roof(x, y, z));
            }
        hallFurniture(c, F, dais);
    }

    private BlockState hallFloorBlock(int x, int z, double d, int dx, int dz) {
        if (d >= 8.5 && d < 9.5) return Pal.GILDED;
        if (d >= 15.5 && d < 16.5) return Pal.DS_TILES;
        if (Math.abs(dx) <= 1 && dz < -3 && dz > -HALL_R + 9) return Pal.b(Blocks.RED_NETHER_BRICKS);
        return ((Math.floorDiv(x, 2) + Math.floorDiv(z, 2)) & 1) == 0 ? Pal.DS_POLISHED : Pal.PB;
    }

    private void hallFurniture(Canvas c, int F, int[] dais) {
        // recinto de la escalera: barandilla de 9x9 con una abertura al sur
        for (int dx = -4; dx <= 4; dx++)
            for (int dz = -4; dz <= 4; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) != 4) continue;
                int x = cx + dx, z = cz + dz;
                if (dz == 4 && Math.abs(dx) <= 1) continue; // salida
                boolean corner = Math.abs(dx) == 4 && Math.abs(dz) == 4;
                Direction.Axis ax = Math.abs(dz) == 4 ? Direction.Axis.X : Direction.Axis.Z;
                BlockState low = ModDeco.gothicFence(ax, false), high = ModDeco.gothicFence(ax, true);
                c.set(x, F, z, corner ? Pal.PB_BRICKS : (low != null ? low : Pal.connected(Blocks.IRON_BARS, ax)));
                c.set(x, F + 1, z, corner ? Pal.PB_CHISELED : (high != null ? high : Pal.connected(Blocks.IRON_BARS, ax)));
                if (corner) c.set(x, F + 2, z, Pal.SOUL_LANTERN);
            }
        // el pilar central de la escalera termina en un farol
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) c.set(cx + dx, F + 2, cz + dz, Pal.PB_CHISELED);
        c.set(cx, F + 3, cz, Pal.SOUL_LANTERN);

        // tarima semicircular con el trono al norte
        int tx = dais[0], tz = dais[1];
        for (int x = cx - 11; x <= cx + 11; x++)
            for (int z = cz - HALL_R + 1; z <= tz + 8; z++) {
                double dd = Math.sqrt((x - tx) * (x - tx) + (z - tz) * (z - tz));
                double dHall = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
                if (dHall > HALL_R - 2.5 || dd > 9) continue;
                if (dd > 6.5) {
                    c.set(x, F, z, Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Pal.dirOf(tx - x, tz - z), false));
                } else {
                    c.set(x, F, z, Pal.PB_BRICKS);
                    c.set(x, F + 1, z, dd > 4.5 ? Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Pal.dirOf(tx - x, tz - z), false)
                            : (dd < 1.6 ? Pal.GILDED : Pal.PB_BRICKS));
                }
            }
        // trono gigante del caballero no-muerto
        int ty = F + 2;
        c.set(tx, ty, tz, Pal.stairs(Blocks.POLISHED_BLACKSTONE_STAIRS, Direction.NORTH, false));
        c.set(tx - 1, ty, tz, Pal.PB_CHISELED);
        c.set(tx + 1, ty, tz, Pal.PB_CHISELED);
        c.set(tx - 1, ty + 1, tz, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
        c.set(tx + 1, ty + 1, tz, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
        for (int y = ty; y <= ty + 8; y++) {
            c.set(tx, y, tz - 1, y == ty + 8 ? Pal.GILDED : Pal.PB_CHISELED);
            if (y >= ty + 2) {
                c.set(tx - 1, y, tz - 1, y >= ty + 6 ? Pal.GILDED : Pal.PB_BRICKS);
                c.set(tx + 1, y, tz - 1, y >= ty + 6 ? Pal.GILDED : Pal.PB_BRICKS);
            }
        }
        c.set(tx, ty + 9, tz - 1, Pal.b(Blocks.WITHER_SKELETON_SKULL));
        c.set(tx - 1, ty + 7, tz - 1, Pal.b(Blocks.WITHER_SKELETON_SKULL));
        c.set(tx + 1, ty + 7, tz - 1, Pal.b(Blocks.WITHER_SKELETON_SKULL));
        // marca de aparicion del jefe (se usara en la fase 3)
        int[] b = bossSpot();
        c.set(b[0], b[1] - 1, b[2], Pal.CRYING_OBSIDIAN);
        // grandes braseros
        BlockState pit = ModDeco.firePit();
        for (int k = 0; k < 8; k++) {
            double a = Math.PI / 8 + k * Math.PI / 4;
            int x = cx + (int) Math.round(Math.cos(a) * (HALL_R - 4.5)), z = cz + (int) Math.round(Math.sin(a) * (HALL_R - 4.5));
            c.set(x, F, z, Pal.PB_CHISELED);
            c.set(x, F + 1, z, Pal.PB_BRICKS);
            c.set(x, F + 2, z, pit != null ? pit : Pal.SOUL_CAMPFIRE);
        }
        // estandartes y apliques en las pilastras de los puntos cardinales
        Direction[] dirs = {Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH};
        for (Direction out : dirs) {
            int px = cx + out.getStepX() * (HALL_R - 2), pz = cz + out.getStepZ() * (HALL_R - 2);
            Direction in = out.getOpposite();
            c.set(px, F + 9, pz, (out == Direction.NORTH ? Blocks.RED_WALL_BANNER : Blocks.BLACK_WALL_BANNER).defaultBlockState()
                    .setValue(WallBannerBlock.FACING, in));
            Direction side = out.getClockWise();
            for (int s = -1; s <= 1; s += 2) {
                int lx = px + side.getStepX() * 2 * s, lz = pz + side.getStepZ() * 2 * s;
                BlockState l = ModDeco.wallLight(out, s + 4);
                // solo si detras hay muro de verdad
                c.set(lx + out.getStepX(), F + 4, lz + out.getStepZ(), Pal.BASALT);
                c.set(lx, F + 4, lz, l != null ? l : Blocks.SOUL_WALL_TORCH.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.WallTorchBlock.FACING, in));
            }
        }
        // lamparas de arana colgando de la cupula
        for (int k = 0; k < 5; k++) {
            int x = k == 4 ? cx : cx + (int) Math.round(Math.cos(k * Math.PI / 2 + Math.PI / 4) * 10);
            int z = k == 4 ? cz : cz + (int) Math.round(Math.sin(k * Math.PI / 2 + Math.PI / 4) * 10);
            int ceil = ceiling(Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz)));
            int bottom = F + 10;
            for (int y = ceil - 1; y > bottom; y--) c.set(x, y, z, Pal.CHAIN);
            BlockState ch = ModDeco.chandelier(true, false);
            if (ch != null) c.set(x, bottom, z, ch);
            else {
                c.set(x, bottom, z, Pal.SOUL_LANTERN_HANG);
                for (Direction d : dirs) {
                    c.set(x + d.getStepX(), bottom + 1, z + d.getStepZ(), Pal.CHAIN);
                    c.set(x + d.getStepX(), bottom, z + d.getStepZ(), Pal.LANTERN_HANG);
                }
            }
        }
        // cofre del tesoro del jefe detras del trono
        c.chest(tx + 3, F + 2, tz - 1, Direction.SOUTH, Decor.loot("tesoro"));
        c.chest(tx - 3, F + 2, tz - 1, Direction.SOUTH, Decor.loot("tesoro"));
    }

    // ------------------------------------------------------------ ramas, copa y colgantes
    private void limb(Canvas c, int[] l) {
        int dx = l[3] - l[0], dy = l[4] - l[1], dz = l[5] - l[2];
        int n = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        Direction.Axis axis = Math.abs(dy) >= Math.max(Math.abs(dx), Math.abs(dz)) ? Direction.Axis.Y : (Math.abs(dx) >= Math.abs(dz) ? Direction.Axis.X : Direction.Axis.Z);
        double thick = l[6];
        for (int i = 0; i <= n; i++) {
            double t = n == 0 ? 0 : i / (double) n;
            double px = l[0] + dx * t, py = l[1] + dy * t, pz = l[2] + dz * t;
            double rad = thick * (1 - t * 0.5);
            int rr = (int) Math.ceil(rad);
            for (int ox = -rr; ox <= rr; ox++)
                for (int oy = -rr; oy <= rr; oy++)
                    for (int oz = -rr; oz <= rr; oz++) {
                        if (ox * ox + oy * oy + oz * oz > rad * rad + 0.3) continue;
                        int x = (int) Math.round(px) + ox, y = (int) Math.round(py) + oy, z = (int) Math.round(pz) + oz;
                        if (!c.inXZ(x, z) || insideHall(x, y, z)) continue;
                        c.set(x, y, z, bark(x, y, z, axis));
                    }
        }
    }

    /** True si el bloque esta dentro del espacio interior de la sala del jefe (no se toca). */
    private boolean insideHall(int x, int y, int z) {
        double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
        return d <= HALL_R - 1.5 && y >= hallFloor - 1 && y <= ceiling(d) + 1;
    }

    private void canopy(Canvas c) {
        for (double[] b : blobs) {
            int rr = (int) Math.ceil(b[3]);
            int ry = Math.max(1, (int) Math.ceil(b[3] * b[4]));
            int x0 = Math.max((int) b[0] - rr, c.minX), x1 = Math.min((int) b[0] + rr, c.maxX);
            int z0 = Math.max((int) b[2] - rr, c.minZ), z1 = Math.min((int) b[2] + rr, c.maxZ);
            for (int x = x0; x <= x1; x++)
                for (int z = z0; z <= z1; z++)
                    for (int y = (int) b[1] - ry; y <= (int) b[1] + ry; y++) {
                        double nx = (x - b[0]) / b[3], ny = (y - b[1]) / (b[3] * b[4]), nz = (z - b[2]) / b[3];
                        double d = nx * nx + ny * ny + nz * nz;
                        d += noise.value(x * 0.18, y * 0.18, z * 0.18) * 0.25;
                        if (d > 1.0) continue;
                        if (Hash.f(x, y, z, seed + 3) > 1.0 - d * d * 0.5) continue;
                        c.setIfAir(x, y, z, leaf(x, y, z));
                    }
        }
    }

    private void hanging(Canvas c) {
        for (int[] h : hangs) {
            if (!c.inXZ(h[0], h[2])) continue;
            int start = Integer.MIN_VALUE;
            for (int y = h[1] + 12; y >= h[1] - 4; y--) {
                if (!c.isAir(h[0], y, h[2])) {
                    start = y - 1;
                    break;
                }
            }
            if (start == Integer.MIN_VALUE) continue;
            for (int k = 0; k < h[3]; k++) {
                int y = start - k;
                if (!c.isAir(h[0], y, h[2]) || insideHall(h[0], y, h[2])) break;
                boolean tip = k == h[3] - 1;
                BlockState s;
                if (h[4] == 0) s = tip ? Pal.SOUL_LANTERN_HANG : Pal.CHAIN;
                else if (h[4] == 1) s = tip ? Blocks.CAVE_VINES.defaultBlockState().setValue(CaveVines.BERRIES, true)
                        : Blocks.CAVE_VINES_PLANT.defaultBlockState().setValue(CaveVines.BERRIES, k % 3 == 0);
                else s = tip ? Pal.b(Blocks.WEEPING_VINES) : Pal.b(Blocks.WEEPING_VINES_PLANT);
                c.set(h[0], y, h[2], s);
            }
        }
    }

    // ------------------------------------------------------------ raices por el torreon
    private void roots(Canvas c) {
        int top = baseY - 1;
        int[][] anchors = {
                {-25, keep.z0 - cz, 0, -1}, {25, keep.z0 - cz, 0, -1},
                {keep.x1 - cx, -30, 1, 0}, {keep.x1 - cx, 8, 1, 0},
                {keep.x0 - cx, -30, -1, 0}, {keep.x0 - cx, 8, -1, 0},
                {-30, keep.z1 - cz, 0, 1}, {30, keep.z1 - cz, 0, 1}
        };
        for (int[] a : anchors) {
            int ex = cx + a[0] + a[2], ez = cz + a[1] + a[3];
            double ang = Math.atan2(ez - cz, ex - cx);
            int sx = cx + (int) Math.round(Math.cos(ang) * (half + 1)), sz = cz + (int) Math.round(Math.sin(ang) * (half + 1));
            rootSeg(c, sx, top + 7, sz, ex, top + 1, ez, 2.2);
            rootSeg(c, ex, top + 1, ez, ex + a[2], (top + groundY) / 2, ez + a[3], 1.7);
            rootSeg(c, ex + a[2], (top + groundY) / 2, ez + a[3], ex + a[2] * 3, groundY + 1, ez + a[3] * 3, 1.5);
            rootSeg(c, ex + a[2] * 3, groundY, ez + a[3] * 3, ex + a[2] * 9 + a[3] * 3, groundY - 1, ez + a[3] * 9 + a[2] * 3, 1.2);
            rootSeg(c, ex + a[2] * 3, groundY, ez + a[3] * 3, ex + a[2] * 9 - a[3] * 3, groundY - 1, ez + a[3] * 9 - a[2] * 3, 1.2);
        }
    }

    private void rootSeg(Canvas c, int ax, int ay, int az, int bx, int by, int bz, double rad) {
        int dx = bx - ax, dy = by - ay, dz = bz - az;
        int n = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        int rr = (int) Math.ceil(rad);
        for (int i = 0; i <= n; i++) {
            double t = n == 0 ? 0 : i / (double) n;
            double wob = noise.value(ax * 0.1 + i * 0.2, ay * 0.1, az * 0.1) * 1.2;
            int px = (int) Math.round(ax + dx * t + wob), py = (int) Math.round(ay + dy * t), pz = (int) Math.round(az + dz * t - wob);
            for (int ox = -rr; ox <= rr; ox++)
                for (int oy = -rr; oy <= rr; oy++)
                    for (int oz = -rr; oz <= rr; oz++) {
                        if (ox * ox + oy * oy + oz * oz > rad * rad) continue;
                        int x = px + ox, y = py + oy, z = pz + oz;
                        if (!c.inXZ(x, z)) continue;
                        boolean insideKeep = x > keep.x0 && x < keep.x1 && z > keep.z0 && z < keep.z1 && y < baseY - 1;
                        if (insideKeep) continue;
                        c.set(x, y, z, bark(x, y, z, Direction.Axis.Y));
                    }
        }
    }

    // ------------------------------------------------------------ aguja que atraviesa la copa
    private void spire(Canvas c) {
        int sb = domeTop - 1;
        for (int x = Math.max(cx - SPIRE_R - 1, c.minX); x <= Math.min(cx + SPIRE_R + 1, c.maxX); x++)
            for (int z = Math.max(cz - SPIRE_R - 1, c.minZ); z <= Math.min(cz + SPIRE_R + 1, c.maxZ); z++) {
                int m = GreatTower.oct(x - cx, z - cz);
                for (int y = sb; y <= sb + SPIRE_H; y++) {
                    double rad = SPIRE_R * (1 - (y - sb) / (double) SPIRE_H);
                    if (m <= rad + 0.5 && m > rad - 1.6) {
                        int rel = y - sb;
                        BlockState s = rel % 10 == 9 ? Pal.trim(x, y, z) : Pal.roof(x, y, z);
                        if (rel % 10 == 4 && Math.abs(Math.abs(x - cx) - Math.abs(z - cz)) == 0) s = Pal.GILDED;
                        c.set(x, y, z, s);
                    }
                }
            }
        int apex = sb + SPIRE_H;
        c.column(cx, cz, apex - 2, apex + 1, Pal.GILDED);
        c.column(cx, cz, apex + 2, apex + 4, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
        c.set(cx, apex + 5, cz, Pal.b(Blocks.LIGHTNING_ROD));
    }
}
