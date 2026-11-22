package com.darkcraft.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

/** Piezas del pueblo dentro de las murallas: puestos de mercado, pozos, huertos, carros y plazas. */
public final class Town {
    private Town() {}

    // ------------------------------------------------------------ puesto de mercado
    public static final class Stall extends Element {
        private final int x, y, z;
        private final Direction face;

        public Stall(long seed, int x, int y, int z, Direction face) {
            super(seed);
        this.layer = 4;
            this.x = x;
            this.y = y;
            this.z = z;
            this.face = face;
            bounds(x - 3, y - 1, z - 3, x + 3, y + 5, z + 3);
        }

        @Override
        public void build(Canvas c) {
            Rng r = new Rng(seed);
            Block[] wool = {Blocks.RED_WOOL, Blocks.BLACK_WOOL, Blocks.BROWN_WOOL, Blocks.PURPLE_WOOL, Blocks.GRAY_WOOL};
            Block w1 = wool[r.nextInt(wool.length)], w2 = wool[r.nextInt(wool.length)];
            Direction side = face.getClockWise();
            for (int a = -2; a <= 2; a++)
                for (int b = -1; b <= 1; b++) {
                    int px = x + side.getStepX() * a + face.getStepX() * b, pz = z + side.getStepZ() * a + face.getStepZ() * b;
                    boolean post = Math.abs(a) == 2 && Math.abs(b) == 1;
                    c.column(px, pz, y, y + 3, Pal.AIR);
                    if (post) c.column(px, pz, y, y + 2, Pal.DO_FENCE);
                    BlockState aw = ModDeco.awning(face, ((a + 2) & 1) == 0);
                    c.set(px, y + 3, pz, aw != null ? aw : (((a + 2) & 1) == 0 ? Pal.b(w1) : Pal.b(w2)));
                    if (b == 1) {
                        // mostrador
                        if (!post) {
                            c.set(px, y, pz, Pal.b(r.chance(0.5) ? Blocks.BARREL : Blocks.SPRUCE_PLANKS));
                            int goods = r.nextInt(9);
                            BlockState g = switch (goods) {
                                case 0 -> Pal.b(Blocks.PUMPKIN);
                                case 1 -> Pal.b(Blocks.MELON);
                                case 2 -> Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Y);
                                case 3 -> Pal.b(Blocks.POTTED_RED_MUSHROOM);
                                case 4 -> Pal.candles(Blocks.CANDLE, 3);
                                case 5 -> Pal.b(Blocks.FLOWER_POT);
                                case 6 -> Pal.b(Blocks.DECORATED_POT);
                                case 7 -> Pal.b(Blocks.CAKE);
                                default -> Pal.b(Blocks.POTTED_FERN);
                            };
                            c.set(px, y + 1, pz, g);
                        }
                    }
                }
            c.chest(x - face.getStepX(), y, z - face.getStepZ(), face, Decor.loot("mercado"));
            c.set(x + side.getStepX() * 2, y + 4, z + side.getStepZ() * 2, Pal.LANTERN);
        }
    }

    // ------------------------------------------------------------ pozo
    public static final class Well extends Element {
        private final int x, y, z;

        public Well(long seed, int x, int y, int z) {
            super(seed);
        this.layer = 4;
            this.x = x;
            this.y = y;
            this.z = z;
            bounds(x - 2, y - 8, z - 2, x + 2, y + 5, z + 2);
        }

        @Override
        public void build(Canvas c) {
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++) {
                    boolean rim = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                    int px = x + dx, pz = z + dz;
                    if (rim) {
                        c.column(px, pz, y - 6, y, Pal::oldWall);
                        boolean corner = Math.abs(dx) == 2 && Math.abs(dz) == 2;
                        if (corner) c.column(px, pz, y + 1, y + 2, Pal.SP_FENCE);
                        c.set(px, y + 3, pz, Pal.stairs(Blocks.SPRUCE_STAIRS, Pal.dirOf(-dx, -dz), false));
                    } else {
                        c.column(px, pz, y - 6, y - 2, Pal.WATER);
                        c.column(px, pz, y - 1, y + 2, Pal.AIR);
                        c.set(px, y + 3, pz, Pal.SP_PLANKS);
                    }
                }
            c.set(x, y + 2, z, Pal.CHAIN);
            c.set(x, y + 1, z, Pal.b(Blocks.WATER_CAULDRON).setValue(LayeredCauldronBlock.LEVEL, 3));
            c.set(x, y + 4, z, Pal.LANTERN);
        }
    }

    // ------------------------------------------------------------ huerto
    public static final class Garden extends Element {
        private final Rect r;
        private final int y;

        public Garden(long seed, Rect r, int y) {
            super(seed);
        this.layer = 1;
            this.r = r;
            this.y = y;
            bounds(r.x0, y - 2, r.z0, r.x1, y + 2, r.z1);
        }

        @Override
        public void build(Canvas c) {
            boolean alongX = r.w() >= r.d();
            int mid = alongX ? r.cz() : r.cx();
            Block[] crops = {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS};
            for (int x = r.x0; x <= r.x1; x++)
                for (int z = r.z0; z <= r.z1; z++) {
                    boolean edge = x == r.x0 || x == r.x1 || z == r.z0 || z == r.z1;
                    c.column(x, z, y, y + 2, Pal.AIR);
                    if (edge) {
                        boolean gate = alongX ? (x == r.cx() && z == r.z1) : (z == r.cz() && x == r.x1);
                        c.set(x, y - 1, z, Pal.COARSE_DIRT);
                        if (gate) continue;
                        Direction.Axis ax = (x == r.x0 || x == r.x1) ? Direction.Axis.Z : Direction.Axis.X;
                        c.set(x, y, z, Pal.connected(Blocks.SPRUCE_FENCE, ax));
                        if ((x + z) % 6 == 0) c.set(x, y + 1, z, Pal.LANTERN);
                        continue;
                    }
                    int line = alongX ? z : x;
                    if (line == mid) {
                        c.set(x, y - 1, z, Pal.WATER);
                        continue;
                    }
                    c.set(x, y - 1, z, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7));
                    int band = Math.floorMod((alongX ? x : z) / 3, crops.length);
                    Block crop = crops[band];
                    BlockState cs = crop.defaultBlockState();
                    if (crop instanceof CropBlock cb) cs = cb.getStateForAge(Math.min(cb.getMaxAge(), 3 + Hash.pct(x, y, z, seed) % 5));
                    c.set(x, y, z, cs);
                }
            // espantapajaros
            int sx = r.x0 + 2, sz = r.z0 + 2;
            c.set(sx, y - 1, sz, Pal.COARSE_DIRT);
            c.set(sx, y, sz, Pal.DO_FENCE);
            c.set(sx, y + 1, sz, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Y));
            c.set(sx, y + 2, sz, Pal.facing(Blocks.CARVED_PUMPKIN, Direction.SOUTH));
            c.set(r.x1 - 1, y, r.z0 + 1, Pal.b(Blocks.COMPOSTER));
            c.barrel(r.x1 - 1, y, r.z1 - 1, Direction.UP, Decor.loot("cocina"));
        }
    }

    // ------------------------------------------------------------ carro
    public static final class Cart extends Element {
        private final int x, y, z;
        private final Direction face;

        public Cart(long seed, int x, int y, int z, Direction face) {
            super(seed);
        this.layer = 4;
            this.x = x;
            this.y = y;
            this.z = z;
            this.face = face;
            bounds(x - 3, y, z - 3, x + 3, y + 3, z + 3);
        }

        @Override
        public void build(Canvas c) {
            Direction side = face.getClockWise();
            for (int a = -1; a <= 1; a++) {
                int px = x + face.getStepX() * a, pz = z + face.getStepZ() * a;
                c.set(px, y, pz, Pal.slab(Blocks.SPRUCE_SLAB, true));
                c.set(px + side.getStepX(), y, pz + side.getStepZ(), Pal.slab(Blocks.SPRUCE_SLAB, true));
                if (a != 0) {
                    c.set(px - side.getStepX(), y, pz - side.getStepZ(), Blocks.DARK_OAK_TRAPDOOR.defaultBlockState()
                            .setValue(TrapDoorBlock.FACING, side).setValue(TrapDoorBlock.OPEN, true));
                    c.set(px + side.getStepX() * 2, y, pz + side.getStepZ() * 2, Blocks.DARK_OAK_TRAPDOOR.defaultBlockState()
                            .setValue(TrapDoorBlock.FACING, side.getOpposite()).setValue(TrapDoorBlock.OPEN, true));
                }
            }
            c.set(x, y + 1, z, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Y));
            c.set(x + side.getStepX(), y + 1, z + side.getStepZ(), Pal.b(Blocks.BARREL));
            c.set(x - face.getStepX(), y + 1, z - face.getStepZ(), Pal.b(Blocks.PUMPKIN));
            c.set(x + face.getStepX() * 2, y, z + face.getStepZ() * 2, Pal.DO_FENCE);
        }
    }

    // ------------------------------------------------------------ banco
    public static void bench(Canvas c, int x, int y, int z, Direction look) {
        Direction side = look.getClockWise();
        for (int a = -1; a <= 1; a++) {
            int px = x + side.getStepX() * a, pz = z + side.getStepZ() * a;
            BlockState m = ModDeco.bench(look);
            c.set(px, y, pz, m != null ? m : Pal.stairs(Blocks.DARK_OAK_STAIRS, look.getOpposite(), false));
        }
    }

    // ------------------------------------------------------------ plaza
    public static final class Plaza extends Element {
        private final Rect r;
        private final int y;

        public Plaza(long seed, Rect r, int y) {
            super(seed);
        this.layer = 4;
            this.r = r;
            this.y = y;
            bounds(r.x0, y - 8, r.z0, r.x1, y + 8, r.z1);
        }

        @Override
        public void build(Canvas c) {
            Rng rng = new Rng(seed);
            for (int x = r.x0; x <= r.x1; x++)
                for (int z = r.z0; z <= r.z1; z++) {
                    c.set(x, y - 1, z, Landscape.road(x, y, z));
                    c.column(x, z, y, y + 4, Pal.AIR);
                }
            int cx = r.cx(), cz = r.cz();
            int kind = rng.nextInt(3);
            if (kind == 0) new Well(seed + 1, cx, y, cz).build(c);
            else if (kind == 1) new Landscape.Fountain(seed + 1, cx, y, cz, 3).build(c);
            else new Landscape.Statue(seed + 1, cx, y, cz, Direction.SOUTH).build(c);
            bench(c, cx, y, r.z0 + 1, Direction.SOUTH);
            bench(c, cx, y, r.z1 - 1, Direction.NORTH);
            new Landscape.LampPost(seed + 2, r.x0 + 1, y, r.z0 + 1).build(c);
            new Landscape.LampPost(seed + 3, r.x1 - 1, y, r.z1 - 1).build(c);
            c.set(r.x1 - 1, y, r.z0 + 1, Pal.leaves(Blocks.AZALEA_LEAVES));
            c.set(r.x0 + 1, y, r.z1 - 1, Pal.leaves(Blocks.FLOWERING_AZALEA_LEAVES));
            c.set(r.x0 + 2, y, r.z1 - 1, Pal.b(Blocks.POTTED_WITHER_ROSE));
            c.set(r.x1 - 2, y, r.z0 + 1, Pal.b(Blocks.POTTED_AZALEA));
        }
    }

    // ------------------------------------------------------------ jardinera con arbol
    public static final class Planter extends Element {
        private final int x, y, z;
        private final Tree tree;

        public Planter(long seed, int x, int y, int z, Tree.Kind kind) {
            super(seed);
        this.layer = 4;
            this.x = x;
            this.y = y;
            this.z = z;
            this.tree = new Tree(seed + 1, kind, x, y + 1, z);
            bounds(Math.min(x - 2, tree.x0), y - 1, Math.min(z - 2, tree.z0), Math.max(x + 2, tree.x1), Math.max(y + 2, tree.y1), Math.max(z + 2, tree.z1));
        }

        @Override
        public void build(Canvas c) {
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++) {
                    boolean rim = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                    int px = x + dx, pz = z + dz;
                    if (rim) {
                        boolean corner = Math.abs(dx) == 2 && Math.abs(dz) == 2;
                        c.set(px, y, pz, corner ? Pal.PB_CHISELED : Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Pal.dirOf(-dx, -dz), true));
                        if (corner) {
                            BlockState l = ModDeco.standingLantern(dx * 3 + dz);
                            c.set(px, y + 1, pz, l != null ? l : Pal.SOUL_LANTERN);
                        }
                    } else {
                        c.set(px, y - 1, pz, Pal.DIRT);
                        c.set(px, y, pz, (dx == 0 && dz == 0) ? Pal.ROOTED_DIRT : Pal.MOSS);
                        if (dx != 0 || dz != 0) {
                            int p = Hash.pct(px, y, pz, seed);
                            if (p < 30) c.set(px, y + 1, pz, Pal.b(Blocks.WITHER_ROSE));
                            else if (p < 55) c.set(px, y + 1, pz, Pal.b(Blocks.FERN));
                            else if (p < 70) c.set(px, y + 1, pz, Pal.b(Blocks.LILY_OF_THE_VALLEY));
                            else if (p < 80) c.set(px, y + 1, pz, Pal.b(Blocks.MOSS_CARPET));
                        }
                    }
                }
            tree.build(c);
        }
    }

    // ------------------------------------------------------------ banco suelto
    public static final class BenchElement extends Element {
        private final int x, y, z;
        private final Direction look;

        public BenchElement(long seed, int x, int y, int z, Direction look) {
            super(seed);
        this.layer = 4;
            this.x = x;
            this.y = y;
            this.z = z;
            this.look = look;
            bounds(x - 2, y, z - 2, x + 2, y + 1, z + 2);
        }

        @Override
        public void build(Canvas c) {
            bench(c, x, y, z, look);
        }
    }

    // ------------------------------------------------------------ monton de cajas y barriles
    public static final class Crates extends Element {
        private final int x, y, z;

        public Crates(long seed, int x, int y, int z) {
            super(seed);
        this.layer = 4;
            this.x = x;
            this.y = y;
            this.z = z;
            bounds(x - 2, y, z - 2, x + 2, y + 3, z + 2);
        }

        @Override
        public void build(Canvas c) {
            Rng r = new Rng(seed);
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++) {
                    if (!r.chance(0.7)) continue;
                    int px = x + dx, pz = z + dz;
                    BlockState m = ModDeco.clutter(r.nextInt(100), false);
                    c.set(px, y, pz, m != null && r.chance(0.5) ? m : clutter(px, y, pz));
                    if (r.chance(0.35)) c.set(px, y + 1, pz, Pal.b(Blocks.BARREL));
                }
            if (r.chance(0.5)) c.barrel(x, y, z, Direction.UP, Decor.loot("mercado"));
        }
    }

    /** Bloque decorativo suelto para rellenar (cajas, barriles, sacos de heno...). */
    public static BlockState clutter(int x, int y, int z) {
        int p = Hash.pct(x, y, z, 555);
        if (p < 25) return Pal.b(Blocks.BARREL);
        if (p < 45) return Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Y);
        if (p < 55) return Pal.b(Blocks.COMPOSTER);
        if (p < 65) return Pal.b(Blocks.PUMPKIN);
        if (p < 75) return Pal.leaves(Blocks.AZALEA_LEAVES);
        if (p < 85) return Pal.b(Blocks.DECORATED_POT);
        return Pal.b(Blocks.FLOWERING_AZALEA);
    }
}
