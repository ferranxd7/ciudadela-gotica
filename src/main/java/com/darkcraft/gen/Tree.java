package com.darkcraft.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** Arboles procedurales de varios tipos (roble oscuro grande, abeto, sauce, arbusto, frondoso...). */
public class Tree extends Element {
    public enum Kind { OAK, BIG_DARK, SPRUCE, WILLOW, BUSH, AZALEA, MANGROVE, CHERRY, MAGIC }

    private final List<int[]> logs = new ArrayList<>();      // x,y,z,eje(0=y,1=x,2=z)
    private final List<double[]> blobs = new ArrayList<>();  // x,y,z,radio,aplastado
    private final List<int[]> hangs = new ArrayList<>();     // x,y,z,largo
    private BlockState log, wood, leaves, leaves2;
    private final Kind kind;

    public Tree(long seed, Kind kind, int x, int y, int z) {
        super(seed);
        this.layer = 2;
        this.kind = kind;
        Rng r = new Rng(seed);
        Woods.Wood w = Woods.forTree(kind, r);
        this.log = w.log();
        this.wood = w.wood();
        this.leaves = w.leaves();
        this.leaves2 = w.leaves2();
        switch (kind) {
            case BIG_DARK -> bigDark(r, x, y, z);
            case SPRUCE -> spruce(r, x, y, z);
            case WILLOW -> willow(r, x, y, z);
            case BUSH -> bush(r, x, y, z, false);
            case AZALEA -> bush(r, x, y, z, true);
            case MANGROVE -> mangrove(r, x, y, z);
            default -> oak(r, x, y, z);
        }
        int minX = x, maxX = x, minY = y, maxY = y, minZ = z, maxZ = z;
        for (int[] l : logs) {
            minX = Math.min(minX, l[0]); maxX = Math.max(maxX, l[0]);
            minY = Math.min(minY, l[1]); maxY = Math.max(maxY, l[1]);
            minZ = Math.min(minZ, l[2]); maxZ = Math.max(maxZ, l[2]);
        }
        for (double[] b : blobs) {
            int rr = (int) Math.ceil(b[3]) + 1;
            minX = Math.min(minX, (int) b[0] - rr); maxX = Math.max(maxX, (int) b[0] + rr);
            minY = Math.min(minY, (int) b[1] - rr); maxY = Math.max(maxY, (int) b[1] + rr);
            minZ = Math.min(minZ, (int) b[2] - rr); maxZ = Math.max(maxZ, (int) b[2] + rr);
        }
        for (int[] h : hangs) minY = Math.min(minY, h[1] - h[3]);
        bounds(minX, minY - 1, minZ, maxX, maxY, maxZ);
    }

    // ------------------------------------------------------------ formas

    private void trunk(int x, int y, int z, int h, int thick) {
        for (int dx = 0; dx < thick; dx++)
            for (int dz = 0; dz < thick; dz++)
                for (int k = -1; k < h; k++) logs.add(new int[]{x + dx, y + k, z + dz, 0});
    }

    /** Rama en linea recta (Bresenham 3D) desde a hasta b. */
    private void branch(int ax, int ay, int az, int bx, int by, int bz) {
        int dx = bx - ax, dy = by - ay, dz = bz - az;
        int n = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        int axis = Math.abs(dy) >= Math.abs(dx) && Math.abs(dy) >= Math.abs(dz) ? 0 : (Math.abs(dx) >= Math.abs(dz) ? 1 : 2);
        for (int i = 0; i <= n; i++) {
            double t = n == 0 ? 0 : i / (double) n;
            logs.add(new int[]{ax + (int) Math.round(dx * t), ay + (int) Math.round(dy * t), az + (int) Math.round(dz * t), axis});
        }
    }

    private void oak(Rng r, int x, int y, int z) {
        int h = r.range(6, 9);
        trunk(x, y, z, h, 1);
        blobs.add(new double[]{x, y + h, z, r.range(3, 4) + 0.5, 0.6});
        int nb = r.range(2, 4);
        for (int i = 0; i < nb; i++) {
            double a = r.nextDouble() * Math.PI * 2;
            int len = r.range(2, 4);
            int bx = x + (int) Math.round(Math.cos(a) * len), bz = z + (int) Math.round(Math.sin(a) * len);
            int by = y + h - r.range(1, 3);
            branch(x, by - 2, z, bx, by, bz);
            blobs.add(new double[]{bx, by + 1, bz, 2.5 + r.nextDouble(), 0.75});
        }
    }

    private void bigDark(Rng r, int x, int y, int z) {
        int h = r.range(10, 16);
        trunk(x, y, z, h, 2);
        // raices
        int[][] rd = {{-1, 0}, {2, 1}, {0, 2}, {1, -1}, {-1, 1}, {2, 0}};
        for (int[] d : rd) if (r.chance(0.7)) logs.add(new int[]{x + d[0], y, z + d[1], 0});
        int nb = r.range(4, 7);
        for (int i = 0; i < nb; i++) {
            double a = r.nextDouble() * Math.PI * 2;
            int len = r.range(4, 8);
            int sy = y + h / 2 + r.nextInt(h / 2);
            int bx = x + (int) Math.round(Math.cos(a) * len), bz = z + (int) Math.round(Math.sin(a) * len);
            int by = sy + r.range(2, 5);
            branch(x + (Math.cos(a) > 0 ? 1 : 0), sy, z + (Math.sin(a) > 0 ? 1 : 0), bx, by, bz);
            blobs.add(new double[]{bx, by + 1, bz, 3.5 + r.nextDouble() * 1.5, 0.65});
        }
        blobs.add(new double[]{x + 0.5, y + h + 1, z + 0.5, 5.0 + r.nextDouble() * 1.5, 0.6});
        for (int i = 0; i < 6; i++) {
            double a = r.nextDouble() * Math.PI * 2;
            hangs.add(new int[]{x + (int) Math.round(Math.cos(a) * 5), y + h - 1, z + (int) Math.round(Math.sin(a) * 5), r.range(2, 5)});
        }
    }

    private void spruce(Rng r, int x, int y, int z) {
        int h = r.range(12, 22);
        boolean thick = h > 18;
        trunk(x, y, z, h, thick ? 2 : 1);
        double off = thick ? 0.5 : 0;
        int start = y + 3;
        for (int yy = start; yy <= y + h + 1; yy += 2) {
            double k = 1 - (yy - start) / (double) (y + h + 1 - start);
            double rad = 1.0 + k * (thick ? 6.0 : 4.0);
            blobs.add(new double[]{x + off, yy, z + off, rad, 0.35});
        }
        blobs.add(new double[]{x + off, y + h + 2, z + off, 1.2, 1});
    }

    private void willow(Rng r, int x, int y, int z) {
        int h = r.range(6, 9);
        trunk(x, y, z, h, 1);
        for (int i = 0; i < 4; i++) {
            Direction d = Direction.from2DDataValue(i);
            int len = r.range(2, 4);
            branch(x, y + h - 2, z, x + d.getStepX() * len, y + h, z + d.getStepZ() * len);
        }
        blobs.add(new double[]{x, y + h + 1, z, 5.0, 0.55});
        for (int i = 0; i < 26; i++) {
            double a = r.nextDouble() * Math.PI * 2;
            double d = 2.5 + r.nextDouble() * 2.5;
            hangs.add(new int[]{x + (int) Math.round(Math.cos(a) * d), y + h, z + (int) Math.round(Math.sin(a) * d), r.range(3, 7)});
        }
    }

    private void mangrove(Rng r, int x, int y, int z) {
        int h = r.range(8, 12);
        int lift = 3;
        trunk(x, y + lift, z, h, 1);
        int[][] roots = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}, {2, 2}, {-2, -2}};
        for (int[] d : roots) {
            branch(x, y + lift, z, x + d[0], y - 1, z + d[1]);
        }
        blobs.add(new double[]{x, y + lift + h, z, 4.0 + r.nextDouble(), 0.7});
        blobs.add(new double[]{x + r.range(-2, 2), y + lift + h - 2, z + r.range(-2, 2), 3.0, 0.7});
        for (int i = 0; i < 8; i++) {
            double a = r.nextDouble() * Math.PI * 2;
            hangs.add(new int[]{x + (int) Math.round(Math.cos(a) * 3), y + lift + h - 2, z + (int) Math.round(Math.sin(a) * 3), r.range(2, 4)});
        }
    }

    private void bush(Rng r, int x, int y, int z, boolean azalea) {
        logs.add(new int[]{x, y, z, 0});
        blobs.add(new double[]{x, y + 1, z, 1.8 + r.nextDouble() * 1.2, 0.8});
        if (azalea) {
            this.leaves = Pal.leaves(Blocks.AZALEA_LEAVES);
            this.leaves2 = Pal.leaves(Blocks.FLOWERING_AZALEA_LEAVES);
        }
    }

    // ------------------------------------------------------------ dibujo

    @Override
    public void build(Canvas c) {
        for (double[] b : blobs) {
            int rr = (int) Math.ceil(b[3]);
            int ry = Math.max(1, (int) Math.ceil(b[3] * b[4]));
            for (int dx = -rr; dx <= rr; dx++)
                for (int dz = -rr; dz <= rr; dz++)
                    for (int dy = -ry; dy <= ry; dy++) {
                        int px = (int) Math.floor(b[0] + dx), py = (int) Math.floor(b[1] + dy), pz = (int) Math.floor(b[2] + dz);
                        if (!c.inXZ(px, pz)) continue;
                        double nx = (px + 0.5 - b[0] - 0.5) / b[3], ny = (py - b[1]) / Math.max(0.6, b[3] * b[4]), nz = (pz + 0.5 - b[2] - 0.5) / b[3];
                        double d = nx * nx + ny * ny + nz * nz;
                        if (d > 1.0) continue;
                        double keep = 1.0 - d * d * 0.55;
                        if (Hash.f(px, py, pz, seed) > keep) continue;
                        c.setIfAir(px, py, pz, Hash.pct(px, py, pz, seed + 1) < 22 ? leaves2 : leaves);
                    }
        }
        for (int[] l : logs) {
            BlockState s;
            if (l[3] == 0) s = log;
            else s = wood != null ? wood : log;
            if (s.hasProperty(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)) {
                s = s.setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                        l[3] == 0 ? Direction.Axis.Y : (l[3] == 1 ? Direction.Axis.X : Direction.Axis.Z));
            }
            c.set(l[0], l[1], l[2], s);
        }
        for (int[] h : hangs) {
            for (int k = 0; k < h[3]; k++) {
                int py = h[1] - k;
                if (!c.isAir(h[0], py, h[2])) {
                    if (k > 0) break;
                    continue;
                }
                BlockState v = kind == Kind.WILLOW ? leaves : Woods.hanging(kind, k == h[3] - 1);
                c.set(h[0], py, h[2], v);
            }
        }
        // suelo bajo el tronco
        c.setIfAir(logs.get(0)[0], logs.get(0)[1] - 1, logs.get(0)[2], Pal.ROOTED_DIRT);
    }
}
