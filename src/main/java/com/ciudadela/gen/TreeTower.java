package com.ciudadela.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * La gran torre central convertida en "torre-arbol": un torreon de piedra envuelto por un arbol
 * gigantesco. La madera sube en espiral por la torre, cada vez ocupa mas, le salen ramas con hojas,
 * raices que bajan por el torreon hasta el suelo y, arriba, una copa enorme atravesada por la aguja.
 */
public class TreeTower extends Element {
    private final int cx, cz, baseY, half, towerTop, crownBase, groundY;
    private final Rect keep;
    private final List<double[]> blobs = new ArrayList<>();   // x,y,z,r,aplastado
    private final List<int[]> limbs = new ArrayList<>();      // x0,y0,z0,x1,y1,z1,grosor
    private final List<int[]> hangs = new ArrayList<>();      // x,y,z,largo,tipo
    private final Noise noise;
    private static final int SPIRE_R = 6, SPIRE_H = 96;

    public TreeTower(long seed, int cx, int cz, int baseY, int half, int towerTop, Rect keep, int groundY) {
        super(seed);
        this.cx = cx;
        this.cz = cz;
        this.baseY = baseY;
        this.half = half;
        this.towerTop = towerTop;
        this.crownBase = towerTop + 28;
        this.keep = keep;
        this.groundY = groundY;
        this.noise = new Noise(seed);
        Rng r = new Rng(seed);
        // ramas a media altura por el lado de madera
        for (int y = towerTop - 90; y <= towerTop - 6; y += 13) {
            double a = woodAngle(y) + (r.nextDouble() - 0.5) * 0.6;
            int len = r.range(12, 20);
            int sx = cx + (int) Math.round(Math.cos(a) * (half + 1)), sz = cz + (int) Math.round(Math.sin(a) * (half + 1));
            int ex = cx + (int) Math.round(Math.cos(a) * (half + len)), ez = cz + (int) Math.round(Math.sin(a) * (half + len));
            int ey = y + r.range(5, 10);
            limbs.add(new int[]{sx, y, sz, ex, ey, ez, 2});
            blobs.add(new double[]{ex, ey + 2, ez, 5 + r.nextDouble() * 2, 0.6});
            for (int k = 0; k < 4; k++) hangs.add(new int[]{ex + r.range(-4, 4), ey, ez + r.range(-4, 4), r.range(3, 8), k % 2});
        }
        // grandes ramas de la copa
        int nb = 8;
        for (int i = 0; i < nb; i++) {
            double a = i * Math.PI * 2 / nb + r.nextDouble() * 0.5;
            int len = r.range(24, 36);
            int ey = crownBase + r.range(10, 22);
            int ex = cx + (int) Math.round(Math.cos(a) * len), ez = cz + (int) Math.round(Math.sin(a) * len);
            int mx = cx + (int) Math.round(Math.cos(a) * len * 0.45), mz = cz + (int) Math.round(Math.sin(a) * len * 0.45);
            limbs.add(new int[]{cx + (int) Math.round(Math.cos(a) * 6), crownBase - 4, cz + (int) Math.round(Math.sin(a) * 6), mx, crownBase + 6, mz, 3});
            limbs.add(new int[]{mx, crownBase + 6, mz, ex, ey, ez, 2});
            blobs.add(new double[]{ex, ey + 3, ez, 10 + r.nextDouble() * 4, 0.55});
            blobs.add(new double[]{mx, crownBase + 12, mz, 9 + r.nextDouble() * 3, 0.55});
            for (int k = 0; k < 10; k++) {
                double aa = r.nextDouble() * Math.PI * 2, dd = r.nextDouble() * 11;
                hangs.add(new int[]{ex + (int) Math.round(Math.cos(aa) * dd), ey - 2, ez + (int) Math.round(Math.sin(aa) * dd), r.range(4, 12), k % 3});
            }
        }
        blobs.add(new double[]{cx, crownBase + 26, cz, 20, 0.5});
        blobs.add(new double[]{cx, crownBase + 38, cz, 13, 0.55});
        int reach = 48;
        bounds(Math.min(cx - reach, keep.x0 - 9), groundY - 2, Math.min(cz - reach, keep.z0 - 9),
                Math.max(cx + reach, keep.x1 + 9), towerTop + 1 + SPIRE_H + 8, Math.max(cz + reach, keep.z1 + 9));
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
        upperTrunk(c);
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
                    // almenas solo en la parte de piedra
                    if (woodiness(x, towerTop, z) <= 0.5) {
                        c.set(x, towerTop + 1, z, Pal.wall(x, towerTop + 1, z));
                        if (((ax + az) & 2) == 0) c.set(x, towerTop + 2, z, Pal.wall(x, towerTop + 2, z));
                    }
                } else {
                    c.column(x, z, baseY, towerTop - 1, Pal.AIR);
                    boolean well = Spiral.inWell(dx, dz);
                    for (int y = baseY + 12; y < towerTop; y += 12)
                        if (!well) c.set(x, y - 1, z, Pal.woodFloor(x, y, z));
                    c.set(x, baseY - 1, z, Pal.DS_TILES);
                    c.set(x, towerTop, z, well ? Pal.AIR : Pal.DS_TILES);
                }
            }
        // puerta sur
        for (int k = -1; k <= 1; k++)
            for (int d = H - 2; d <= H + 6; d++) {
                int x = cx + k, z = cz + d;
                for (int y = baseY; y <= baseY + 3; y++) c.set(x, y, z, Pal.AIR);
                c.set(x, baseY - 1, z, Pal.DS_TILES);
            }
        Spiral.build(c, cx, cz, baseY, towerTop, (int) (seed & 15), Blocks.DEEPSLATE_TILE_STAIRS, Pal::wall);
        // farolillos en las plantas
        for (int y = baseY + 12; y < towerTop; y += 12) {
            for (int[] p : new int[][]{{6, 6}, {-6, 6}, {6, -6}, {-6, -6}}) {
                c.set(cx + p[0], y + 9, cz + p[1], Pal.CHAIN);
                c.set(cx + p[0], y + 8, cz + p[1], Pal.SOUL_LANTERN_HANG);
            }
        }
    }

    // ------------------------------------------------------------ tronco sobre la torre
    private void upperTrunk(Canvas c) {
        int H = half;
        for (int x = Math.max(cx - H - 2, c.minX); x <= Math.min(cx + H + 2, c.maxX); x++)
            for (int z = Math.max(cz - H - 2, c.minZ); z <= Math.min(cz + H + 2, c.maxZ); z++) {
                int dx = x - cx, dz = z - cz;
                double d = Math.sqrt(dx * dx + dz * dz);
                for (int y = towerTop + 1; y <= crownBase + 4; y++) {
                    double k = (y - towerTop) / (double) (crownBase + 4 - towerTop);
                    double rt = (H + 1.5) * (1 - k) + 7.5 * k + noise.value(x * 0.2, y * 0.15, z * 0.2) * 1.2;
                    double ang = Math.atan2(dz, dx);
                    // el tronco se retuerce: huecos en espiral que dejan ver la aguja
                    boolean gap = Math.sin(ang * 3 + y * 0.12) > 0.82 && d > rt - 2.5 && k < 0.7;
                    if (d <= rt && d > SPIRE_R + 0.5 && !gap) c.set(x, y, z, bark(x, y, z, Direction.Axis.Y));
                }
            }
    }

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
                        if (c.inXZ(x, z)) c.set(x, y, z, bark(x, y, z, axis));
                    }
        }
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
            int start = h[1];
            // bajar desde la primera hoja
            for (int y = h[1] + 12; y >= h[1] - 4; y--) {
                if (!c.isAir(h[0], y, h[2])) {
                    start = y - 1;
                    break;
                }
            }
            for (int k = 0; k < h[3]; k++) {
                int y = start - k;
                if (!c.isAir(h[0], y, h[2])) break;
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
            // sobre la azotea
            rootSeg(c, sx, top + 7, sz, ex, top + 1, ez, 2.2);
            // bajando por la fachada
            rootSeg(c, ex, top + 1, ez, ex + a[2], (top + groundY) / 2, ez + a[3], 1.7);
            rootSeg(c, ex + a[2], (top + groundY) / 2, ez + a[3], ex + a[2] * 3, groundY + 1, ez + a[3] * 3, 1.5);
            // se abre en el suelo
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
                        // no tapar puertas ni ventanas del torreon: solo por fuera o sobre la azotea
                        boolean insideKeep = x > keep.x0 && x < keep.x1 && z > keep.z0 && z < keep.z1 && y < baseY - 1;
                        if (insideKeep) continue;
                        c.set(x, y, z, bark(x, y, z, Direction.Axis.Y));
                    }
        }
    }

    // ------------------------------------------------------------ aguja que atraviesa la copa
    private void spire(Canvas c) {
        int sb = towerTop + 1;
        for (int x = Math.max(cx - SPIRE_R - 1, c.minX); x <= Math.min(cx + SPIRE_R + 1, c.maxX); x++)
            for (int z = Math.max(cz - SPIRE_R - 1, c.minZ); z <= Math.min(cz + SPIRE_R + 1, c.maxZ); z++) {
                int m = GreatTower.oct(x - cx, z - cz);
                for (int y = sb; y <= sb + SPIRE_H; y++) {
                    double rad = y < crownBase + 30 ? SPIRE_R : SPIRE_R * (1 - (y - crownBase - 30) / (double) (sb + SPIRE_H - crownBase - 30));
                    if (m <= rad + 0.5 && m > rad - 1.6) {
                        int rel = y - sb;
                        BlockState s = rel % 10 == 9 ? Pal.trim(x, y, z) : Pal.roof(x, y, z);
                        if (rel % 10 == 4 && Math.abs(Math.abs(x - cx) - Math.abs(z - cz)) == 0) s = Pal.GILDED;
                        if (y < crownBase + 30 && rel % 10 >= 3 && rel % 10 <= 6 && (x == cx || z == cz)) s = Pal.GLASS_PURPLE;
                        c.set(x, y, z, s);
                    } else if (m < rad - 1.6 && y < crownBase + 30) {
                        c.set(x, y, z, Pal.AIR);
                    }
                }
            }
        int apex = sb + SPIRE_H;
        c.column(cx, cz, apex - 2, apex + 1, Pal.GILDED);
        c.column(cx, cz, apex + 2, apex + 4, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
        c.set(cx, apex + 5, cz, Pal.b(Blocks.LIGHTNING_ROD));
        // mirador dentro de la copa
        for (int y = sb; y < crownBase + 30; y += 10) {
            c.fill(cx - SPIRE_R + 2, y - 1, cz - SPIRE_R + 2, cx + SPIRE_R - 2, y - 1, cz + SPIRE_R - 2, Pal.DO_PLANKS);
            c.set(cx, y + 6, cz, Pal.CHAIN);
            c.set(cx, y + 5, cz, Pal.LANTERN_HANG);
        }
    }
}
