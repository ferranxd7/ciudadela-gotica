package com.ciudadela.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** Elementos pequenos de paisaje: arboles muertos, cementerio, laberinto, fuente, farolas, estatuas, puentes. */
public final class Landscape {
    private Landscape() {}

    // ------------------------------------------------------------------ arbol muerto
    public static final class DeadTree extends Element {
        private final int x, y, z, h;
        private final boolean leaves, thick;
        private final List<int[]> branches = new ArrayList<>(); // {dx, dz, startY, len, rise}

        public DeadTree(long seed, int x, int y, int z, boolean thick) {
            super(seed);
            Rng r = new Rng(seed);
            this.x = x;
            this.y = y;
            this.z = z;
            this.thick = thick;
            this.h = thick ? r.range(11, 18) : r.range(6, 11);
            this.leaves = r.chance(0.45);
            int nb = r.range(3, thick ? 7 : 5);
            for (int i = 0; i < nb; i++) {
                int dir = r.nextInt(8);
                int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
                branches.add(new int[]{dirs[dir][0], dirs[dir][1], y + h / 2 + r.nextInt(h / 2), r.range(2, thick ? 7 : 4), r.range(1, 3)});
            }
            bounds(x - 9, y - 2, z - 9, x + 10, y + h + 8, z + 10);
        }

        @Override
        public void build(Canvas c) {
            int t = thick ? 1 : 0;
            for (int dx = 0; dx <= t; dx++)
                for (int dz = 0; dz <= t; dz++) {
                    c.column(x + dx, z + dz, y - 1, y + h, Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
                    c.set(x + dx, y - 1, z + dz, Pal.ROOTED_DIRT);
                }
            // raices
            if (thick) {
                c.set(x - 1, y, z, Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.X));
                c.set(x + 2, y, z + 1, Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.X));
                c.set(x, y, z + 2, Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.Z));
                c.set(x + 1, y, z - 1, Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.Z));
            }
            for (int[] b : branches) {
                int bx = x + (b[0] > 0 ? t : 0), bz = z + (b[1] > 0 ? t : 0), by = b[2];
                for (int k = 1; k <= b[3]; k++) {
                    bx += b[0];
                    bz += b[1];
                    if (k % (4 - b[4]) == 0) by++;
                    Direction.Axis ax = b[0] != 0 && b[1] == 0 ? Direction.Axis.X : (b[1] != 0 && b[0] == 0 ? Direction.Axis.Z : Direction.Axis.Y);
                    c.set(bx, by, bz, Pal.log(Blocks.DARK_OAK_LOG, ax));
                }
                if (leaves) {
                    for (int dx = -1; dx <= 1; dx++)
                        for (int dz = -1; dz <= 1; dz++)
                            for (int dy = 0; dy <= 1; dy++)
                                if (Hash.pct(bx + dx, by + dy, bz + dz, seed) < 55)
                                    c.setIfAir(bx + dx, by + dy, bz + dz, Pal.leaves(Blocks.DARK_OAK_LEAVES));
                } else if (Hash.pct(bx, by, bz, seed) < 30) {
                    c.setIfAir(bx, by - 1, bz, Pal.CHAIN);
                    c.setIfAir(bx, by - 2, bz, Pal.SOUL_LANTERN_HANG);
                }
            }
            if (leaves) {
                for (int dx = -2; dx <= 2 + t; dx++)
                    for (int dz = -2; dz <= 2 + t; dz++)
                        for (int dy = 0; dy <= 2; dy++)
                            if (Hash.pct(x + dx, y + h + dy, z + dz, seed + 1) < 45 - dy * 10)
                                c.setIfAir(x + dx, y + h + dy, z + dz, Pal.leaves(Blocks.DARK_OAK_LEAVES));
            }
        }
    }

    // ------------------------------------------------------------------ cementerio
    public static final class Cemetery extends Element {
        private final Rect r;
        private final int y;

        public Cemetery(long seed, Rect r, int y) {
            super(seed);
            this.r = r;
            this.y = y;
            bounds(r.x0 - 1, y - 3, r.z0 - 1, r.x1 + 1, y + 6, r.z1 + 1);
        }

        @Override
        public void build(Canvas c) {
            for (int x = r.x0; x <= r.x1; x++)
                for (int z = r.z0; z <= r.z1; z++) {
                    boolean edge = x == r.x0 || x == r.x1 || z == r.z0 || z == r.z1;
                    int p = Hash.pct(x, y, z, seed);
                    c.set(x, y - 1, z, p < 40 ? Pal.COARSE_DIRT : p < 70 ? Pal.PODZOL : p < 85 ? Pal.SOUL_SOIL : Pal.GRAVEL);
                    c.column(x, z, y, y + 3, Pal.AIR);
                    if (edge) {
                        boolean gate = (x == r.cx() || x == r.cx() + 1) && z == r.z1;
                        if (gate) continue;
                        boolean post = (x + z) % 5 == 0;
                        if (post) {
                            c.set(x, y, z, Pal.PB_BRICKS);
                            c.set(x, y + 1, z, Pal.PB_BRICKS);
                            c.set(x, y + 2, z, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
                        } else {
                            Direction.Axis ax = (x == r.x0 || x == r.x1) ? Direction.Axis.Z : Direction.Axis.X;
                            BlockState gb = ModDeco.gothicFence(ax, false), gt = ModDeco.gothicFence(ax, true);
                            c.set(x, y, z, gb != null ? gb : Pal.connected(Blocks.IRON_BARS, ax));
                            c.set(x, y + 1, z, gt != null ? gt : Pal.connected(Blocks.IRON_BARS, ax));
                        }
                        continue;
                    }
                    int lx = x - r.x0, lz = z - r.z0;
                    if (lx % 4 == 2 && lz % 5 == 2 && lx > 1 && lz > 1 && x < r.x1 - 1 && z < r.z1 - 1) {
                        // lapida + tumba
                        int g = Hash.pct(x, 0, z, seed + 5);
                        if (g < 35) {
                            c.set(x, y, z, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
                        } else if (g < 60) {
                            c.set(x, y, z, Pal.b(Blocks.MOSSY_STONE_BRICK_WALL));
                        } else if (g < 75) {
                            c.set(x, y, z, Pal.PB_BRICKS);
                            c.set(x, y + 1, z, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
                            c.set(x, y + 2, z, Pal.candles(Blocks.RED_CANDLE, 1));
                        } else if (g < 85) {
                            c.set(x, y, z, Pal.b(Blocks.DEEPSLATE_BRICK_WALL));
                            c.set(x, y + 1, z, Pal.b(Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, g % 16));
                        } else {
                            c.set(x, y, z, Pal.b(Blocks.STONE_BRICK_WALL));
                            c.set(x, y + 1, z, Pal.candles(Blocks.BLACK_CANDLE, 2));
                        }
                        c.set(x, y - 1, z + 1, Pal.COARSE_DIRT);
                        c.set(x, y - 1, z + 2, Pal.COARSE_DIRT);
                        if (g % 7 == 0) {
                            // tumba abierta
                            c.set(x, y - 1, z + 1, Pal.AIR);
                            c.set(x, y - 2, z + 1, Pal.SOUL_SOIL);
                            c.set(x, y - 1, z + 1, Pal.COBWEB);
                        }
                    } else if (Hash.pct(x, y, z, seed + 9) < 3) {
                        c.set(x, y, z, Pal.b(Blocks.WITHER_ROSE));
                    } else if (Hash.pct(x, y, z, seed + 10) < 2) {
                        c.set(x, y, z, Pal.b(Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, Hash.pct(x, 0, z, 3) % 16));
                    }
                }
        }
    }

    // ------------------------------------------------------------------ laberinto de setos
    public static final class HedgeMaze extends Element {
        private final Rect r;
        private final int y, cells, cellsZ;
        private final boolean[][] open; // pasillos
        private static final int CELL = 3;

        public HedgeMaze(long seed, Rect area, int y) {
            super(seed);
            this.y = y;
            this.cells = (area.w() - 1) / (CELL + 1);
            this.cellsZ = (area.d() - 1) / (CELL + 1);
            this.r = new Rect(area.x0, area.z0, area.x0 + cells * (CELL + 1), area.z0 + cellsZ * (CELL + 1));
            int gw = r.w(), gd = r.d();
            open = new boolean[gw][gd];
            Rng rng = new Rng(seed);
            boolean[][] seen = new boolean[cells][cellsZ];
            int[] stack = new int[cells * cellsZ * 2 + 4];
            int sp = 0;
            stack[sp++] = 0;
            stack[sp++] = 0;
            seen[0][0] = true;
            carveCell(0, 0);
            while (sp > 0) {
                int cz = stack[sp - 1], cx = stack[sp - 2];
                int[][] nbs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                List<int[]> opts = new ArrayList<>();
                for (int[] n : nbs) {
                    int nx = cx + n[0], nz = cz + n[1];
                    if (nx >= 0 && nz >= 0 && nx < cells && nz < cellsZ && !seen[nx][nz]) opts.add(n);
                }
                if (opts.isEmpty()) {
                    sp -= 2;
                    continue;
                }
                int[] n = rng.pick(opts);
                int nx = cx + n[0], nz = cz + n[1];
                seen[nx][nz] = true;
                carveCell(nx, nz);
                // pared entre celdas
                int wx = 1 + cx * (CELL + 1) + (n[0] > 0 ? CELL : n[0] < 0 ? -1 : 0);
                int wz = 1 + cz * (CELL + 1) + (n[1] > 0 ? CELL : n[1] < 0 ? -1 : 0);
                for (int k = 0; k < CELL; k++) {
                    int ox = n[0] != 0 ? wx : wx + k, oz = n[1] != 0 ? wz : wz + k;
                    open[ox][oz] = true;
                }
                stack[sp++] = nx;
                stack[sp++] = nz;
            }
            // entradas
            for (int k = 0; k < CELL; k++) {
                open[1 + k][0] = true;
                open[gw - 2 - k][gd - 1] = true;
            }
            // claro central
            int mx = gw / 2, mz = gd / 2;
            for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
                int ox = mx + dx, oz = mz + dz;
                if (ox > 0 && oz > 0 && ox < gw - 1 && oz < gd - 1) open[ox][oz] = true;
            }
            bounds(r.x0, y - 1, r.z0, r.x1, y + 5, r.z1);
        }

        private void carveCell(int cx, int cz) {
            for (int dx = 0; dx < CELL; dx++)
                for (int dz = 0; dz < CELL; dz++) open[1 + cx * (CELL + 1) + dx][1 + cz * (CELL + 1) + dz] = true;
        }

        @Override
        public void build(Canvas c) {
            int mx = r.x0 + r.w() / 2, mz = r.z0 + r.d() / 2;
            for (int x = Math.max(r.x0, c.minX); x <= Math.min(r.x1, c.maxX); x++)
                for (int z = Math.max(r.z0, c.minZ); z <= Math.min(r.z1, c.maxZ); z++) {
                    boolean o = open[x - r.x0][z - r.z0];
                    if (o) {
                        c.set(x, y - 1, z, Hash.pct(x, y, z, seed) < 70 ? Pal.GRAVEL : Pal.COARSE_DIRT);
                        c.column(x, z, y, y + 3, Pal.AIR);
                    } else {
                        c.set(x, y - 1, z, Pal.DIRT);
                        c.column(x, z, y, y + 2, Pal.leaves(Blocks.DARK_OAK_LEAVES));
                        if (Hash.pct(x, y, z, seed + 1) < 15) c.set(x, y + 1, z, Pal.leaves(Blocks.AZALEA_LEAVES));
                    }
                }
            // estatua central
            new Statue(seed + 1, mx, y, mz, Direction.SOUTH).build(c);
        }
    }

    // ------------------------------------------------------------------ fuente
    public static final class Fountain extends Element {
        private final int x, y, z, rad;

        public Fountain(long seed, int x, int y, int z, int rad) {
            super(seed);
            this.x = x;
            this.y = y;
            this.z = z;
            this.rad = rad;
            bounds(x - rad - 1, y - 2, z - rad - 1, x + rad + 1, y + 10, z + rad + 1);
        }

        @Override
        public void build(Canvas c) {
            for (int dx = -rad - 1; dx <= rad + 1; dx++)
                for (int dz = -rad - 1; dz <= rad + 1; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    int px = x + dx, pz = z + dz;
                    if (d > rad + 0.5) continue;
                    c.set(px, y - 2, pz, Pal.DS_POLISHED);
                    if (d > rad - 0.7) {
                        c.set(px, y - 1, pz, Pal.PB_BRICKS);
                        c.set(px, y, pz, Pal.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, false));
                    } else {
                        c.set(px, y - 1, pz, Pal.WATER);
                        c.set(px, y, pz, Pal.AIR);
                    }
                }
            // columna central con cuencos
            c.column(x, z, y - 1, y + 4, Pal.BASALT);
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    c.set(x + dx, y + 3, z + dz, Pal.slab(Blocks.POLISHED_BLACKSTONE_SLAB, true));
                }
            c.set(x, y + 5, z, Pal.PB_CHISELED);
            c.flowingWater(x, y + 6, z);
            c.set(x + 1, y + 4, z, Pal.WATER);
            c.set(x - 1, y + 4, z, Pal.WATER);
            c.set(x, y + 4, z + 1, Pal.WATER);
            c.set(x, y + 4, z - 1, Pal.WATER);
        }
    }

    // ------------------------------------------------------------------ farola
    public static final class LampPost extends Element {
        private final int x, y, z;

        public LampPost(long seed, int x, int y, int z) {
            super(seed);
            this.x = x;
            this.y = y;
            this.z = z;
            bounds(x - 1, y, z - 1, x + 1, y + 5, z + 1);
        }

        @Override
        public void build(Canvas c) {
            c.set(x, y, z, Pal.PB_BRICKS);
            c.column(x, z, y + 1, y + 3, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
            c.set(x, y + 4, z, Pal.PB_CHISELED);
            boolean soul = Hash.pct(x, y, z, 2) < 60;
            BlockState mod = ModDeco.standingLantern(Hash.h(x, y, z, 5));
            c.set(x, y + 5, z, mod != null ? mod : (soul ? Pal.SOUL_LANTERN : Pal.LANTERN));
        }
    }

    // ------------------------------------------------------------------ estatua (gargola / caballero de piedra)
    public static final class Statue extends Element {
        private final int x, y, z;
        private final Direction face;

        public Statue(long seed, int x, int y, int z, Direction face) {
            super(seed);
            this.x = x;
            this.y = y;
            this.z = z;
            this.face = face;
            bounds(x - 2, y, z - 2, x + 2, y + 7, z + 2);
        }

        @Override
        public void build(Canvas c) {
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++) {
                    c.set(x + dx, y, z + dz, Pal.PB_BRICKS);
                    c.set(x + dx, y + 1, z + dz, (dx == 0 || dz == 0) ? Pal.PB : Pal.stairs(Blocks.POLISHED_BLACKSTONE_STAIRS, Pal.dirOf(-dx, -dz), true));
                }
            boolean gargoyle = (seed & 1) == 0;
            Direction side = face.getClockWise();
            if (gargoyle) {
                c.set(x, y + 2, z, Pal.stairs(Blocks.DEEPSLATE_BRICK_STAIRS, face.getOpposite(), false));
                c.set(x, y + 3, z, Pal.DS_BRICKS);
                c.set(x + side.getStepX(), y + 3, z + side.getStepZ(), Pal.stairs(Blocks.DEEPSLATE_TILE_STAIRS, side.getOpposite(), true));
                c.set(x - side.getStepX(), y + 3, z - side.getStepZ(), Pal.stairs(Blocks.DEEPSLATE_TILE_STAIRS, side, true));
                c.set(x + side.getStepX(), y + 4, z + side.getStepZ(), Pal.slab(Blocks.DEEPSLATE_TILE_SLAB, false));
                c.set(x - side.getStepX(), y + 4, z - side.getStepZ(), Pal.slab(Blocks.DEEPSLATE_TILE_SLAB, false));
                c.set(x, y + 4, z, Pal.b(Blocks.WITHER_SKELETON_SKULL).setValue(SkullBlock.ROTATION, face.get2DDataValue() * 4));
            } else {
                c.set(x, y + 2, z, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
                c.set(x, y + 3, z, Pal.DS_POLISHED);
                c.set(x, y + 4, z, Pal.DS_POLISHED);
                c.set(x + side.getStepX(), y + 4, z + side.getStepZ(), Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
                c.set(x - side.getStepX(), y + 4, z - side.getStepZ(), Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
                c.set(x, y + 5, z, Pal.b(Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, face.get2DDataValue() * 4));
                c.set(x - side.getStepX(), y + 5, z - side.getStepZ(), Pal.b(Blocks.LIGHTNING_ROD));
            }
        }
    }

    // ------------------------------------------------------------------ patio de entrenamiento
    public static final class TrainingYard extends Element {
        private final Rect r;
        private final int y;

        public TrainingYard(long seed, Rect r, int y) {
            super(seed);
            this.r = r;
            this.y = y;
            bounds(r.x0, y - 1, r.z0, r.x1, y + 4, r.z1);
        }

        @Override
        public void build(Canvas c) {
            for (int x = r.x0; x <= r.x1; x++)
                for (int z = r.z0; z <= r.z1; z++) {
                    c.set(x, y - 1, z, Hash.pct(x, y, z, seed) < 60 ? Pal.GRAVEL : Pal.COARSE_DIRT);
                    c.column(x, z, y, y + 3, Pal.AIR);
                    boolean edge = x == r.x0 || x == r.x1 || z == r.z0 || z == r.z1;
                    if (edge && !(Math.abs(x - r.cx()) <= 1)) {
                        Direction.Axis ax = (x == r.x0 || x == r.x1) ? Direction.Axis.Z : Direction.Axis.X;
                        c.set(x, y, z, Pal.connected(Blocks.DARK_OAK_FENCE, ax));
                    }
                }
            for (int x = r.x0 + 3; x <= r.x1 - 3; x += 5) {
                int z = r.z0 + 3;
                c.set(x, y, z, Pal.DO_FENCE);
                c.set(x, y + 1, z, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Y));
                c.set(x, y + 2, z, Pal.facing(Blocks.CARVED_PUMPKIN, Direction.SOUTH));
                c.set(x, y, r.z1 - 2, Pal.b(Blocks.TARGET));
                c.set(x, y + 1, r.z1 - 2, Pal.b(Blocks.TARGET));
            }
        }
    }

    // ------------------------------------------------------------------ puente colgante
    public static final class Bridge extends Element {
        private final double ax, ay, az, bx, by, bz, len, ux, uz;
        private final double sag;
        private final double halfW;

        public Bridge(long seed, int ax, int ay, int az, int bx, int by, int bz, double sag, int width) {
            super(seed);
            this.ax = ax;
            this.ay = ay;
            this.az = az;
            this.bx = bx;
            this.by = by;
            this.bz = bz;
            double dx = bx - ax, dz = bz - az;
            this.len = Math.max(1, Math.sqrt(dx * dx + dz * dz));
            this.ux = dx / len;
            this.uz = dz / len;
            this.sag = sag;
            this.halfW = width / 2.0;
            int pad = width + 3;
            bounds(Math.min(ax, bx) - pad, Math.min(ay, by) - (int) sag - 8, Math.min(az, bz) - pad,
                    Math.max(ax, bx) + pad, Math.max(ay, by) + 6, Math.max(az, bz) + pad);
        }

        private int deckY(double t) {
            double k = Math.max(0, Math.min(1, t / len));
            return (int) Math.round(ay + (by - ay) * k - sag * Math.sin(Math.PI * k));
        }

        @Override
        public void build(Canvas c) {
            for (int x = Math.max(x0, c.minX); x <= Math.min(x1, c.maxX); x++)
                for (int z = Math.max(z0, c.minZ); z <= Math.min(z1, c.maxZ); z++) {
                    double px = x - ax, pz = z - az;
                    double t = px * ux + pz * uz;
                    double s = -px * uz + pz * ux;
                    if (t < 0 || t > len) continue;
                    double as = Math.abs(s);
                    int yd = deckY(t);
                    if (as <= halfW) {
                        c.set(x, yd, z, Math.floorMod((int) t, 5) == 0 ? Pal.DO_STRIPPED : Pal.DO_PLANKS);
                        c.column(x, z, yd + 1, yd + 4, Pal.AIR);
                        if (Math.floorMod((int) t, 7) == 3 && as < 0.6) {
                            c.set(x, yd - 1, z, Pal.CHAIN);
                            c.set(x, yd - 2, z, Pal.CHAIN);
                            c.set(x, yd - 3, z, Pal.SOUL_LANTERN_HANG);
                        }
                    } else if (as <= halfW + 1.0) {
                        c.set(x, yd, z, Pal.DO_PLANKS);
                        boolean post = Math.floorMod((int) t, 8) == 0;
                        if (post) {
                            c.column(x, z, yd + 1, yd + 2, Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
                            c.set(x, yd + 3, z, Hash.pct(x, yd, z, 3) < 50 ? Pal.SOUL_LANTERN : Pal.LANTERN);
                        } else {
                            c.set(x, yd + 1, z, Pal.DO_FENCE);
                        }
                    }
                }
        }
    }

    /** Calles de tierra del pueblo. */
    public static BlockState lane(int x, int y, int z) {
        BlockState mod = Mods.state("mcwpaths:cobbled_deepslate_basket_weave_paving");
        int p = Hash.pct(x, y, z, 334);
        if (mod != null && p < 25) return mod;
        if (p < 45) return Pal.b(Blocks.DIRT_PATH);
        if (p < 65) return Pal.COARSE_DIRT;
        if (p < 82) return Pal.GRAVEL;
        return Pal.DS_COBBLED;
    }

    /** Bloque de superficie de piedra (para caminos). */
    public static BlockState road(int x, int y, int z) {
        int p = Hash.pct(x, y, z, 333);
        if (p < 55) {
            BlockState mod = ModDeco.paving(x, y, z);
            if (mod != null) return mod;
        }
        if (p < 35) return Pal.PB_BRICKS;
        if (p < 55) return Pal.DS_TILES;
        if (p < 70) return Pal.DS_BRICKS;
        if (p < 80) return Pal.PB_BRICKS_CRACKED;
        if (p < 90) return Pal.DS_COBBLED;
        return Pal.GRAVEL;
    }
}
