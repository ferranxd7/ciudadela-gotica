package com.ciudadela.gen;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Amuebla cada sala segun su tipo. Todo es determinista para que cada chunk vea lo mismo. */
public final class Decor {
    private final Canvas c;
    private final Room room;
    private final Rect r;
    private final Rng rng;
    private final int F, T;
    private final Set<Long> used = new HashSet<>();

    private Decor(Canvas c, Room room, long seed) {
        this.c = c;
        this.room = room;
        this.r = room.r;
        this.rng = new Rng(seed);
        this.F = room.floorY;
        this.T = room.top();
    }

    public static ResourceLocation loot(String name) {
        return new ResourceLocation("ciudadela", "chests/" + name);
    }

    public static void decorate(Canvas c, Room room, long seed) {
        if (room.r.w() < 3 || room.r.d() < 3 || room.height < 3) return;
        new Decor(c, room, seed).run();
    }

    // ------------------------------------------------------------------ utilidades

    private record WallCell(int x, int z, Direction in) {}

    private List<WallCell> wallCells() {
        List<WallCell> l = new ArrayList<>();
        int o = room.open;
        for (int x = r.x0; x <= r.x1; x++) {
            if ((o & 1) == 0) l.add(new WallCell(x, r.z0, Direction.SOUTH));
            if ((o & 2) == 0) l.add(new WallCell(x, r.z1, Direction.NORTH));
        }
        for (int z = r.z0 + 1; z < r.z1; z++) {
            if ((o & 4) == 0) l.add(new WallCell(r.x0, z, Direction.EAST));
            if ((o & 8) == 0) l.add(new WallCell(r.x1, z, Direction.WEST));
        }
        return l;
    }

    private boolean free(int x, int z) {
        return r.contains(x, z) && !room.clear(x, z) && !used.contains(Room.key(x, z));
    }

    private void use(int x, int z) {
        used.add(Room.key(x, z));
    }

    private boolean big() {
        return r.w() >= 14 && r.d() >= 14;
    }

    private void floor(Canvas.StateFn f) {
        c.fill(r.x0, F - 1, r.z0, r.x1, F - 1, r.z1, f);
    }

    private void checker(BlockState a, BlockState b, int cell) {
        floor((x, y, z) -> ((Math.floorDiv(x, cell) + Math.floorDiv(z, cell)) & 1) == 0 ? a : b);
    }

    private void chandelier(int x, int z, boolean large) {
        int len = Math.max(1, Math.min(room.height / 3, large ? 6 : 2));
        int y0 = T - len;
        if (y0 < F + 3) {
            c.set(x, T, z, Pal.SOUL_LANTERN_HANG);
            return;
        }
        for (int y = T; y > y0; y--) c.set(x, y, z, Pal.CHAIN);
        if (!large) {
            c.set(x, y0, z, rng.chance(0.5) ? Pal.LANTERN_HANG : Pal.SOUL_LANTERN_HANG);
            return;
        }
        BlockState center = Pal.DO_FENCE.setValue(BlockStateProperties.NORTH, true).setValue(BlockStateProperties.SOUTH, true)
                .setValue(BlockStateProperties.EAST, true).setValue(BlockStateProperties.WEST, true);
        c.set(x, y0, z, center);
        c.set(x, y0 - 1, z, Pal.SOUL_LANTERN_HANG);
        Direction[] dirs = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (Direction d : dirs) {
            int ax = x + d.getStepX(), az = z + d.getStepZ();
            c.set(ax, y0, az, Pal.DO_FENCE.setValue(prop(d.getOpposite()), true));
            c.set(ax, y0 + 1, az, Pal.candles(Blocks.BLACK_CANDLE, 3));
            c.set(ax, y0 - 1, az, Pal.LANTERN_HANG);
        }
    }

    private static net.minecraft.world.level.block.state.properties.BooleanProperty prop(Direction d) {
        return switch (d) {
            case NORTH -> BlockStateProperties.NORTH;
            case SOUTH -> BlockStateProperties.SOUTH;
            case EAST -> BlockStateProperties.EAST;
            default -> BlockStateProperties.WEST;
        };
    }

    private void chandeliers(boolean large) {
        int step = large ? 8 : 7;
        int nx = Math.max(1, r.w() / step), nz = Math.max(1, r.d() / step);
        for (int i = 0; i < nx; i++)
            for (int j = 0; j < nz; j++) {
                int x = r.x0 + (int) ((i + 0.5) * r.w() / nx);
                int z = r.z0 + (int) ((j + 0.5) * r.d() / nz);
                chandelier(x, z, large);
            }
    }

    private void torches(int every) {
        int i = 0;
        for (WallCell w : wallCells()) {
            if (i++ % every != every / 2) continue;
            if (room.clear(w.x, w.z)) continue;
            c.setIfAir(w.x, F + 2, w.z, Blocks.SOUL_WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, w.in));
        }
    }

    private void banners(int every) {
        int i = 0;
        for (WallCell w : wallCells()) {
            if (i++ % every != 0 || room.height < 6) continue;
            Block b = (i / every) % 2 == 0 ? Blocks.RED_WALL_BANNER : Blocks.BLACK_WALL_BANNER;
            c.setIfAir(w.x, F + room.height - 3, w.z, b.defaultBlockState().setValue(WallBannerBlock.FACING, w.in));
        }
    }

    private void table(int x0, int z0, int x1, int z1, Block slab) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++)
            for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
                c.set(x, F, z, Pal.slab(slab, true));
                use(x, z);
            }
    }

    /** Silla mirando hacia "look" (el respaldo queda al lado contrario). */
    private void chair(int x, int z, Direction look, Block stairs) {
        if (!free(x, z)) return;
        c.set(x, F, z, Pal.stairs(stairs, look.getOpposite(), false));
        use(x, z);
    }

    private void bed(int hx, int hz, Direction in, Block bed) {
        int fx = hx + in.getStepX(), fz = hz + in.getStepZ();
        if (!free(hx, hz) || !free(fx, fz)) return;
        Direction facing = in.getOpposite();
        c.set(hx, F, hz, bed.defaultBlockState().setValue(BedBlock.FACING, facing).setValue(BedBlock.PART, BedPart.HEAD));
        c.set(fx, F, fz, bed.defaultBlockState().setValue(BedBlock.FACING, facing).setValue(BedBlock.PART, BedPart.FOOT));
        use(hx, hz);
        use(fx, fz);
    }

    private void chestAt(int x, int z, Direction in, String lootName) {
        if (!free(x, z)) return;
        c.chest(x, F, z, in, loot(lootName));
        use(x, z);
    }

    private void pillar(int x, int z, Canvas.StateFn body) {
        if (room.clear(x, z)) return;
        c.column(x, z, F, T, body);
        c.set(x, F, z, Pal.PB_CHISELED);
        c.set(x, T, z, Pal.PB_CHISELED);
        use(x, z);
    }

    private void rug(int x0, int z0, int x1, int z1, Block inner, Block border) {
        for (int x = x0; x <= x1; x++)
            for (int z = z0; z <= z1; z++) {
                boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                if (c.isAir(x, F, z)) c.set(x, F, z, (edge ? border : inner).defaultBlockState());
            }
    }

    private void standing(int x, int z, BlockState s) {
        if (!free(x, z)) return;
        c.set(x, F, z, s);
        use(x, z);
    }

    private void armorStand(int x, int z, Direction look, boolean dark) {
        if (!free(x, z) || !c.inXZ(x, z)) return;
        use(x, z);
        ArmorStand st = EntityType.ARMOR_STAND.create(c.level.getLevel());
        if (st == null) return;
        st.moveTo(x + 0.5, F, z + 0.5, look.toYRot(), 0);
        if (dark) {
            st.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
            st.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
        } else {
            st.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            st.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            st.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
        }
        st.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
        c.addEntity(st);
    }

    private void cobwebs(double p) {
        for (int x = r.x0; x <= r.x1; x++)
            for (int z = r.z0; z <= r.z1; z++) {
                boolean edge = x == r.x0 || x == r.x1 || z == r.z0 || z == r.z1;
                if (edge && Hash.f(x, T, z, 77) < p) c.setIfAir(x, T, z, Pal.COBWEB);
            }
    }

    /** Muebles a lo largo de las paredes, eligiendo de una lista. */
    private void alongWalls(double p, BlockState... options) {
        for (WallCell w : wallCells()) {
            if (!free(w.x, w.z) || !rng.chance(p)) continue;
            BlockState s = options[rng.nextInt(options.length)];
            if (s.hasProperty(HorizontalDirectionalBlock.FACING)) s = s.setValue(HorizontalDirectionalBlock.FACING, w.in);
            if (s.hasProperty(BarrelBlock.FACING)) s = s.setValue(BarrelBlock.FACING, Direction.UP);
            c.set(w.x, F, w.z, s);
            use(w.x, w.z);
        }
    }

    // ------------------------------------------------------------------ salas

    private void run() {
        switch (room.type) {
            case GREAT_HALL -> greatHall();
            case THRONE -> throne();
            case DINING -> dining();
            case KITCHEN -> kitchen();
            case LIBRARY -> library(false);
            case LIBRARY_GRAND -> library(true);
            case BEDROOM -> bedroom();
            case BATH -> bath();
            case CELLAR -> cellar();
            case WINE_CELLAR -> wineCellar();
            case ARMORY -> armory();
            case CHAPEL -> chapel();
            case STUDY -> study();
            case STORAGE -> storage();
            case GALLERY -> gallery();
            case TREASURY -> treasury();
            case PRISON -> prison();
            case CRYPT -> crypt();
            case ALCHEMY -> alchemy();
            case BALLROOM -> ballroom();
            case SERVANTS -> servants();
            case BARRACKS -> barracks();
            case SMITHY -> smithy();
            case STABLE -> stable();
            case OBSERVATORY -> observatory();
            default -> {
            }
        }
    }

    private void greatHall() {
        checker(Pal.DS_POLISHED, Pal.PB, 2);
        boolean alongX = r.w() >= r.d();
        int inset = 4;
        // dos filas de columnas
        if (alongX) {
            for (int x = r.x0 + 3; x <= r.x1 - 3; x += 6) {
                pillar(x, r.z0 + inset, (a, b, cc) -> Pal.BASALT);
                pillar(x, r.z1 - inset, (a, b, cc) -> Pal.BASALT);
                c.setIfAir(x, F + room.height - 4, r.z0 + inset + 1, Blocks.RED_WALL_BANNER.defaultBlockState().setValue(WallBannerBlock.FACING, Direction.SOUTH));
                c.setIfAir(x, F + room.height - 4, r.z1 - inset - 1, Blocks.RED_WALL_BANNER.defaultBlockState().setValue(WallBannerBlock.FACING, Direction.NORTH));
            }
            int zc = r.cz();
            for (int x = r.x0; x <= r.x1; x++)
                for (int z = zc - 1; z <= zc + 1; z++) c.set(x, F, z, (z == zc ? Blocks.RED_CARPET : Blocks.BLACK_CARPET).defaultBlockState());
            // mesas largas a los lados del pasillo central
            for (int side = -1; side <= 1; side += 2) {
                int tz = zc + side * 4;
                if (r.d() < 14) break;
                table(r.x0 + 4, tz, r.x1 - 4, tz, Blocks.DARK_OAK_SLAB);
                for (int x = r.x0 + 4; x <= r.x1 - 4; x++) {
                    chair(x, tz - 1, Direction.SOUTH, Blocks.DARK_OAK_STAIRS);
                    chair(x, tz + 1, Direction.NORTH, Blocks.DARK_OAK_STAIRS);
                    if ((x - r.x0) % 3 == 0) c.set(x, F + 1, tz, Pal.candles(Blocks.RED_CANDLE, 1 + Hash.h(x, F, tz, 3) % 3));
                }
            }
        } else {
            for (int z = r.z0 + 3; z <= r.z1 - 3; z += 6) {
                pillar(r.x0 + inset, z, (a, b, cc) -> Pal.BASALT);
                pillar(r.x1 - inset, z, (a, b, cc) -> Pal.BASALT);
            }
            int xc = r.cx();
            for (int z = r.z0; z <= r.z1; z++)
                for (int x = xc - 1; x <= xc + 1; x++) c.set(x, F, z, (x == xc ? Blocks.RED_CARPET : Blocks.BLACK_CARPET).defaultBlockState());
        }
        // chimeneas en los extremos
        fireplace(alongX ? r.x0 : r.cx(), alongX ? r.cz() : r.z0, alongX ? Direction.EAST : Direction.SOUTH);
        fireplace(alongX ? r.x1 : r.cx(), alongX ? r.cz() : r.z1, alongX ? Direction.WEST : Direction.NORTH);
        chandeliers(true);
        torches(5);
    }

    private void fireplace(int x, int z, Direction in) {
        Direction side = in.getClockWise();
        for (int k = -2; k <= 2; k++) {
            int px = x + side.getStepX() * k, pz = z + side.getStepZ() * k;
            boolean edge = Math.abs(k) == 2;
            for (int y = F; y <= F + 4; y++) {
                if (edge || y >= F + 3) c.set(px, y, pz, Pal.PB_BRICKS);
            }
            if (!edge) {
                c.set(px, F - 1, pz, Pal.NETHER_BRICKS);
                if (k == 0) c.set(px, F, pz, Pal.CAMPFIRE.setValue(CampfireBlock.FACING, in));
            }
            c.set(px, F + 5, pz, Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, in.getOpposite(), true));
            used.add(Room.key(px, pz));
        }
    }

    private void throne() {
        checker(Pal.PB, Pal.DS_TILES, 3);
        boolean northEnd = r.d() >= r.w();
        int xc = r.cx(), zc = r.cz();
        // alfombra roja hasta el trono
        if (northEnd) {
            for (int z = r.z0 + 6; z <= r.z1; z++)
                for (int x = xc - 2; x <= xc + 2; x++)
                    c.set(x, F, z, (Math.abs(x - xc) == 2 ? Blocks.BLACK_CARPET : Blocks.RED_CARPET).defaultBlockState());
            // tarima escalonada
            for (int step = 0; step < 3; step++) {
                int zEdge = r.z0 + 5 - step;
                for (int x = xc - 7 + step; x <= xc + 7 - step; x++) {
                    for (int z = r.z0; z < zEdge; z++) c.set(x, F + step, z, Pal.PB_BRICKS);
                    c.set(x, F + step, zEdge, Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.NORTH, false));
                }
            }
            int ty = F + 3, tz = r.z0 + 1;
            c.set(xc, ty, tz, Pal.stairs(Blocks.POLISHED_BLACKSTONE_STAIRS, Direction.NORTH, false));
            c.set(xc - 1, ty, tz, Pal.PB_CHISELED);
            c.set(xc + 1, ty, tz, Pal.PB_CHISELED);
            c.set(xc - 1, ty + 1, tz, Pal.candles(Blocks.RED_CANDLE, 3));
            c.set(xc + 1, ty + 1, tz, Pal.candles(Blocks.RED_CANDLE, 3));
            for (int y = ty; y <= ty + 5; y++) c.set(xc, y, r.z0, y == ty + 5 ? Pal.GILDED : Pal.PB_CHISELED);
            c.set(xc, ty + 6, r.z0, Pal.b(Blocks.WITHER_SKELETON_SKULL));
            // braseros
            for (int s = -1; s <= 1; s += 2) {
                int bx = xc + s * 6;
                c.set(bx, F + 2, r.z0 + 4, Pal.PB_CHISELED);
                c.set(bx, F + 3, r.z0 + 4, Pal.SOUL_CAMPFIRE);
            }
            for (int z = r.z0 + 8; z <= r.z1 - 3; z += 6) {
                pillar(xc - 8, z, (a, b, cc) -> Pal.BASALT);
                pillar(xc + 8, z, (a, b, cc) -> Pal.BASALT);
                c.set(xc - 8, F + 1, z + 1, Pal.SOUL_CAMPFIRE);
                c.set(xc + 8, F + 1, z + 1, Pal.SOUL_CAMPFIRE);
            }
        } else {
            for (int x = r.x0 + 6; x <= r.x1; x++)
                for (int z = zc - 2; z <= zc + 2; z++)
                    c.set(x, F, z, (Math.abs(z - zc) == 2 ? Blocks.BLACK_CARPET : Blocks.RED_CARPET).defaultBlockState());
            for (int step = 0; step < 3; step++) {
                int xEdge = r.x0 + 5 - step;
                for (int z = zc - 7 + step; z <= zc + 7 - step; z++) {
                    for (int x = r.x0; x < xEdge; x++) c.set(x, F + step, z, Pal.PB_BRICKS);
                    c.set(xEdge, F + step, z, Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.WEST, false));
                }
            }
            c.set(r.x0 + 1, F + 3, zc, Pal.stairs(Blocks.POLISHED_BLACKSTONE_STAIRS, Direction.WEST, false));
            for (int y = F + 3; y <= F + 8; y++) c.set(r.x0, y, zc, Pal.PB_CHISELED);
        }
        banners(4);
        chandeliers(true);
    }

    private void dining() {
        floor(Pal::woodFloor);
        boolean alongX = r.w() >= r.d();
        int len = alongX ? r.w() : r.d();
        if (len >= 7) {
            int a0 = (alongX ? r.x0 : r.z0) + 2, a1 = (alongX ? r.x1 : r.z1) - 2;
            int mid = alongX ? r.cz() : r.cx();
            for (int a = a0; a <= a1; a++) {
                int x = alongX ? a : mid, z = alongX ? mid : a;
                table(x, z, x, z, Blocks.DARK_OAK_SLAB);
                if ((a - a0) % 3 == 1) c.set(x, F + 1, z, Pal.candles(Blocks.WHITE_CANDLE, 1 + Hash.h(x, 0, z, 4) % 4));
                if (alongX) {
                    chair(x, z - 1, Direction.SOUTH, Blocks.DARK_OAK_STAIRS);
                    chair(x, z + 1, Direction.NORTH, Blocks.DARK_OAK_STAIRS);
                } else {
                    chair(x - 1, z, Direction.EAST, Blocks.DARK_OAK_STAIRS);
                    chair(x + 1, z, Direction.WEST, Blocks.DARK_OAK_STAIRS);
                }
            }
            // silla del senor en la cabecera
            if (alongX) chair(a0 - 1, mid, Direction.EAST, Blocks.POLISHED_BLACKSTONE_STAIRS);
            else chair(mid, a0 - 1, Direction.SOUTH, Blocks.POLISHED_BLACKSTONE_STAIRS);
        }
        alongWalls(0.25, Pal.b(Blocks.BARREL), Pal.b(Blocks.SPRUCE_PLANKS), Pal.BOOKSHELF);
        chestAt(r.x0, r.z0, Direction.SOUTH, "cocina");
        chandeliers(big());
        torches(6);
    }

    private void kitchen() {
        floor((x, y, z) -> Hash.pct(x, y, z, 5) < 60 ? Pal.DS_POLISHED : Pal.STONE_BRICKS);
        fireplace(r.cx(), r.z0, Direction.SOUTH);
        BlockState[] counter = {
                Pal.facing(Blocks.FURNACE, Direction.NORTH), Pal.facing(Blocks.SMOKER, Direction.NORTH),
                Pal.b(Blocks.CRAFTING_TABLE), Pal.b(Blocks.BARREL), Pal.slab(Blocks.POLISHED_BLACKSTONE_SLAB, true),
                Pal.slab(Blocks.POLISHED_BLACKSTONE_SLAB, true), Pal.b(Blocks.WATER_CAULDRON).setValue(LayeredCauldronBlock.LEVEL, 3),
                Pal.b(Blocks.COMPOSTER)
        };
        alongWalls(0.7, counter);
        if (r.w() >= 7 && r.d() >= 7) {
            table(r.cx() - 1, r.cz(), r.cx() + 1, r.cz() + 1, Blocks.SPRUCE_SLAB);
            c.set(r.cx(), F + 1, r.cz(), Pal.b(Blocks.CAKE));
        }
        chestAt(r.x1, r.z1, Direction.NORTH, "cocina");
        for (int x = r.x0 + 1; x < r.x1; x += 3) c.setIfAir(x, T, r.cz(), Pal.b(Blocks.HAY_BLOCK));
        chandeliers(false);
    }

    private void library(boolean grand) {
        floor((x, y, z) -> Hash.pct(x, y, z, 6) < 80 ? Pal.DO_PLANKS : Pal.SP_PLANKS);
        int shelfH = Math.min(room.height - 1, grand ? 6 : 4);
        for (WallCell w : wallCells()) {
            if (room.clear(w.x, w.z)) continue;
            for (int y = F; y < F + shelfH; y++) {
                boolean chis = Hash.pct(w.x, y, w.z, 9) < 12;
                c.set(w.x, y, w.z, chis ? Pal.facing(Blocks.CHISELED_BOOKSHELF, w.in) : Pal.BOOKSHELF);
            }
            use(w.x, w.z);
        }
        boolean alongX = r.w() >= r.d();
        int rows = alongX ? r.d() : r.w();
        // estanterias centrales en filas con pasillos
        for (int k = 3; k < rows - 3; k += 3) {
            for (int a = 3; a < (alongX ? r.w() : r.d()) - 3; a++) {
                if (grand && Math.abs(a - (alongX ? r.w() : r.d()) / 2) < 4) continue;
                int x = alongX ? r.x0 + a : r.x0 + k;
                int z = alongX ? r.z0 + k : r.z0 + a;
                if (!free(x, z)) continue;
                for (int y = F; y < F + Math.min(3, room.height - 1); y++) c.set(x, y, z, Pal.BOOKSHELF);
                use(x, z);
            }
        }
        int cx = r.cx(), cz = r.cz();
        if (grand) {
            // Mesa de encantamientos en el centro rodeada de estanterias
            c.set(cx, F, cz, Pal.b(Blocks.ENCHANTING_TABLE));
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) != 2 && Math.abs(dz) != 2) continue;
                    if (dx == 0 || dz == 0) continue;
                    c.set(cx + dx, F, cz + dz, Pal.BOOKSHELF);
                    c.set(cx + dx, F + 1, cz + dz, Pal.BOOKSHELF);
                    c.set(cx + dx, F + 2, cz + dz, Pal.candles(Blocks.PURPLE_CANDLE, 2));
                }
            c.set(cx + 3, F, cz, Pal.facing(Blocks.LECTERN, Direction.WEST));
            c.set(cx - 3, F, cz, Pal.facing(Blocks.LECTERN, Direction.EAST));
        } else if (free(cx, cz)) {
            table(cx, cz, cx + (alongX ? 1 : 0), cz + (alongX ? 0 : 1), Blocks.DARK_OAK_SLAB);
            c.set(cx, F + 1, cz, Pal.candles(Blocks.WHITE_CANDLE, 3));
        }
        chestAt(r.x0 + 1, r.z0 + 1, Direction.SOUTH, "biblioteca");
        if (grand) chestAt(r.x1 - 1, r.z1 - 1, Direction.NORTH, "biblioteca");
        chandeliers(grand);
    }

    private void bedroom() {
        floor(Pal::woodFloor);
        Block[] beds = {Blocks.RED_BED, Blocks.BLACK_BED, Blocks.GRAY_BED, Blocks.PURPLE_BED};
        Block bedB = beds[rng.nextInt(beds.length)];
        Block carpet = rng.chance(0.5) ? Blocks.RED_CARPET : Blocks.GRAY_CARPET;
        rug(r.x0 + 2, r.z0 + 2, r.x1 - 2, r.z1 - 2, carpet, Blocks.BLACK_CARPET);
        int hx = r.cx(), hz = r.z0;
        bed(hx, hz, Direction.SOUTH, bedB);
        if (r.w() >= 11) bed(hx + 3, hz, Direction.SOUTH, bedB);
        // mesillas con velas
        for (int s = -1; s <= 1; s += 2) {
            int nx = hx + s * (s > 0 && r.w() >= 11 ? 4 : 1);
            if (free(nx, hz)) {
                c.set(nx, F, hz, Pal.b(Blocks.SPRUCE_PLANKS));
                c.set(nx, F + 1, hz, Pal.candles(Blocks.BLACK_CANDLE, 1 + rng.nextInt(3)));
                use(nx, hz);
            }
        }
        // armario
        if (free(r.x0, r.z1) && free(r.x0 + 1, r.z1)) {
            for (int x = r.x0; x <= r.x0 + 1; x++) {
                for (int y = F; y <= F + 2 && y <= T; y++) c.set(x, y, r.z1, Pal.DO_STRIPPED);
                c.set(x, F + 1, r.z1 - 1, Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.NORTH)
                        .setValue(TrapDoorBlock.OPEN, true));
                use(x, r.z1);
            }
        }
        chestAt(hx, hz + 2, Direction.SOUTH, "dormitorio");
        alongWalls(0.12, Pal.BOOKSHELF, Pal.b(Blocks.BARREL), Pal.b(Blocks.POTTED_WITHER_ROSE));
        chandelier(r.cx(), r.cz(), false);
    }

    private void bath() {
        floor((x, y, z) -> ((x + z) & 1) == 0 ? Pal.DS_POLISHED : Pal.SMOOTH_BASALT);
        if (r.w() >= 7 && r.d() >= 7) {
            int px0 = r.x0 + 2, px1 = r.x1 - 2, pz0 = r.z0 + 2, pz1 = r.z1 - 2;
            for (int x = px0; x <= px1; x++)
                for (int z = pz0; z <= pz1; z++) {
                    boolean edge = x == px0 || x == px1 || z == pz0 || z == pz1;
                    c.set(x, F, z, edge ? Pal.DS_POLISHED : Pal.WATER);
                    use(x, z);
                }
            for (int[] k : new int[][]{{px0, pz0}, {px1, pz0}, {px0, pz1}, {px1, pz1}}) {
                c.set(k[0], F + 1, k[1], Pal.candles(Blocks.WHITE_CANDLE, 4));
            }
        } else {
            standing(r.cx(), r.cz(), Pal.b(Blocks.WATER_CAULDRON).setValue(LayeredCauldronBlock.LEVEL, 3));
        }
        alongWalls(0.3, Pal.b(Blocks.WATER_CAULDRON).setValue(LayeredCauldronBlock.LEVEL, 3),
                Pal.stairs(Blocks.POLISHED_DEEPSLATE_STAIRS, Direction.NORTH, false), Pal.b(Blocks.BARREL));
        chandelier(r.cx(), r.cz(), false);
        torches(5);
    }

    private void cellar() {
        floor((x, y, z) -> Hash.pct(x, y, z, 7) < 50 ? Pal.DS_COBBLED : Pal.STONE);
        for (int x = r.x0 + 3; x <= r.x1 - 3; x += 6)
            for (int z = r.z0 + 3; z <= r.z1 - 3; z += 6) pillar(x, z, (a, b, cc) -> Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
        for (WallCell w : wallCells()) {
            if (!free(w.x, w.z)) continue;
            int roll = Hash.pct(w.x, F, w.z, 31);
            if (roll < 45) {
                c.barrel(w.x, F, w.z, Direction.UP, roll < 8 ? loot("comun") : null);
                if (roll < 25) c.set(w.x, F + 1, w.z, Pal.b(Blocks.BARREL));
            } else if (roll < 55) {
                c.set(w.x, F, w.z, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Y));
            } else if (roll < 60) {
                c.chest(w.x, F, w.z, w.in, loot("comun"));
            }
            use(w.x, w.z);
        }
        cobwebs(0.25);
        chandelier(r.cx(), r.cz(), false);
    }

    private void wineCellar() {
        floor((x, y, z) -> Hash.pct(x, y, z, 8) < 70 ? Pal.STONE_BRICKS : Pal.DS_BRICKS);
        boolean alongX = r.w() >= r.d();
        for (WallCell w : wallCells()) {
            if (!free(w.x, w.z)) continue;
            Direction lay = w.in;
            int hgt = Math.min(3, room.height - 1);
            for (int y = F; y < F + hgt; y++) c.set(w.x, y, w.z, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, lay));
            use(w.x, w.z);
        }
        // filas centrales de toneles
        int rows = alongX ? r.d() : r.w();
        for (int k = 3; k < rows - 3; k += 4) {
            for (int a = 2; a < (alongX ? r.w() : r.d()) - 2; a++) {
                int x = alongX ? r.x0 + a : r.x0 + k;
                int z = alongX ? r.z0 + k : r.z0 + a;
                if (!free(x, z)) continue;
                Direction lay = alongX ? Direction.NORTH : Direction.EAST;
                c.set(x, F, z, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, lay));
                if (a % 2 == 0) c.set(x, F + 1, z, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, lay));
                use(x, z);
            }
        }
        for (int a = r.x0 + 2; a <= r.x1 - 2; a += 4) c.setIfAir(a, T, r.cz(), Pal.log(Blocks.SPRUCE_LOG, Direction.Axis.Z));
        chestAt(r.cx(), r.cz(), Direction.SOUTH, "bodega");
        cobwebs(0.35);
        chandeliers(false);
    }

    private void armory() {
        floor((x, y, z) -> Pal.DS_TILES);
        BlockState[] stuff = {Pal.facing(Blocks.ANVIL, Direction.NORTH), Pal.b(Blocks.GRINDSTONE), Pal.b(Blocks.SMITHING_TABLE),
                Pal.b(Blocks.FLETCHING_TABLE), Pal.b(Blocks.BARREL)};
        alongWalls(0.25, stuff);
        int i = 0;
        for (int x = r.x0 + 2; x <= r.x1 - 2; x += 2) {
            armorStand(x, r.cz(), (i++ % 2 == 0) ? Direction.NORTH : Direction.SOUTH, i % 3 == 0);
        }
        chestAt(r.x0, r.z0, Direction.SOUTH, "armeria");
        chestAt(r.x1, r.z1, Direction.NORTH, "armeria");
        banners(5);
        chandeliers(false);
    }

    private void chapel() {
        floor((x, y, z) -> ((x + z) & 1) == 0 ? Pal.DS_POLISHED : Pal.PB);
        boolean alongZ = r.d() >= r.w();
        int cx = r.cx(), cz = r.cz();
        if (alongZ) {
            // altar al norte
            for (int x = cx - 2; x <= cx + 2; x++) {
                c.set(x, F, r.z0 + 1, Pal.PB_CHISELED);
                c.set(x, F + 1, r.z0 + 1, Pal.candles(x == cx ? Blocks.RED_CANDLE : Blocks.WHITE_CANDLE, 2 + (x & 1)));
                use(x, r.z0 + 1);
            }
            c.set(cx, F, r.z0 + 3, Pal.facing(Blocks.LECTERN, Direction.SOUTH));
            for (int z = r.z0 + 5; z <= r.z1 - 2; z += 2) {
                for (int x = r.x0 + 1; x <= r.x1 - 1; x++) {
                    if (Math.abs(x - cx) <= 1) {
                        c.set(x, F, z, Pal.b(Blocks.RED_CARPET));
                        c.set(x, F, z + 1, Pal.b(Blocks.RED_CARPET));
                        continue;
                    }
                    chair(x, z, Direction.NORTH, Blocks.DARK_OAK_STAIRS);
                }
            }
        } else {
            for (int z = cz - 2; z <= cz + 2; z++) {
                c.set(r.x0 + 1, F, z, Pal.PB_CHISELED);
                c.set(r.x0 + 1, F + 1, z, Pal.candles(Blocks.WHITE_CANDLE, 3));
                use(r.x0 + 1, z);
            }
            for (int x = r.x0 + 4; x <= r.x1 - 2; x += 2)
                for (int z = r.z0 + 1; z <= r.z1 - 1; z++) {
                    if (Math.abs(z - cz) <= 1) continue;
                    chair(x, z, Direction.WEST, Blocks.DARK_OAK_STAIRS);
                }
        }
        chandeliers(big());
        torches(4);
    }

    private void study() {
        floor(Pal::woodFloor);
        rug(r.x0 + 1, r.z0 + 1, r.x1 - 1, r.z1 - 1, Blocks.RED_CARPET, Blocks.RED_CARPET);
        for (int x = r.x0; x <= r.x1; x++) {
            if (!free(x, r.z0)) continue;
            for (int y = F; y < F + Math.min(4, room.height - 1); y++) c.set(x, y, r.z0, Pal.BOOKSHELF);
            use(x, r.z0);
        }
        int cx = r.cx(), cz = r.cz();
        table(cx - 1, cz, cx + 1, cz, Blocks.DARK_OAK_SLAB);
        c.set(cx - 1, F + 1, cz, Pal.candles(Blocks.WHITE_CANDLE, 2));
        c.set(cx + 1, F + 1, cz, Pal.b(Blocks.POTTED_DEAD_BUSH));
        chair(cx, cz + 1, Direction.NORTH, Blocks.DARK_OAK_STAIRS);
        standing(r.x1, r.z1, Pal.b(Blocks.CARTOGRAPHY_TABLE));
        standing(r.x0, r.z1, Pal.facing(Blocks.LECTERN, Direction.EAST));
        chestAt(r.x1, r.z0 + 1, Direction.WEST, "biblioteca");
        chandelier(cx, cz, false);
    }

    private void storage() {
        floor((x, y, z) -> Pal.SP_PLANKS);
        for (int x = r.x0; x <= r.x1; x++)
            for (int z = r.z0; z <= r.z1; z++) {
                if (!free(x, z)) continue;
                boolean edge = x == r.x0 || x == r.x1 || z == r.z0 || z == r.z1;
                int roll = Hash.pct(x, F, z, 41);
                if (!edge && ((x - r.x0) % 4 != 2 || roll < 40)) continue;
                if (roll < 6) c.chest(x, F, z, Pal.dirOf(r.cx() - x, r.cz() - z), loot("comun"));
                else if (roll < 50) {
                    c.set(x, F, z, Pal.b(Blocks.BARREL));
                    if (roll < 30) c.set(x, F + 1, z, Pal.b(Blocks.BARREL));
                } else if (roll < 65) c.set(x, F, z, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.X));
                else if (roll < 75) c.set(x, F, z, Pal.b(Blocks.BROWN_WOOL));
                use(x, z);
            }
        cobwebs(0.2);
        chandelier(r.cx(), r.cz(), false);
    }

    private void gallery() {
        floor((x, y, z) -> ((x + z) & 1) == 0 ? Pal.DS_TILES : Pal.PB);
        boolean alongX = r.w() >= r.d();
        if (alongX) {
            for (int x = r.x0; x <= r.x1; x++) c.set(x, F, r.cz(), Pal.b(Blocks.RED_CARPET));
            for (int x = r.x0 + 2; x <= r.x1 - 2; x += 5) {
                armorStand(x, r.z0, Direction.SOUTH, true);
                armorStand(x, r.z1, Direction.NORTH, false);
            }
        } else {
            for (int z = r.z0; z <= r.z1; z++) c.set(r.cx(), F, z, Pal.b(Blocks.RED_CARPET));
            for (int z = r.z0 + 2; z <= r.z1 - 2; z += 5) {
                armorStand(r.x0, z, Direction.EAST, true);
                armorStand(r.x1, z, Direction.WEST, false);
            }
        }
        banners(4);
        chandeliers(false);
    }

    private void treasury() {
        floor((x, y, z) -> Hash.pct(x, y, z, 42) < 15 ? Pal.GILDED : Pal.PB);
        chestAt(r.cx(), r.z0, Direction.SOUTH, "tesoro");
        chestAt(r.x0, r.cz(), Direction.EAST, "tesoro");
        chestAt(r.x1, r.cz(), Direction.WEST, "tesoro");
        int n = 0;
        for (int x = r.x0 + 1; x <= r.x1 - 1 && n < 4; x += 2)
            if (free(x, r.z1)) {
                c.set(x, F, r.z1, Pal.b(n % 2 == 0 ? Blocks.GOLD_BLOCK : Blocks.RAW_GOLD_BLOCK));
                use(x, r.z1);
                n++;
            }
        for (int x = r.x0; x <= r.x1; x++) c.setIfAir(x, T, r.z0, Pal.COBWEB);
        chandelier(r.cx(), r.cz(), false);
    }

    private void prison() {
        floor((x, y, z) -> Pal.foundation(x, y, z));
        boolean alongX = r.w() >= r.d();
        if (alongX && r.d() >= 7) {
            int barZ = r.z0 + 3;
            for (int x = r.x0; x <= r.x1; x++) {
                boolean divider = (x - r.x0) % 4 == 0;
                for (int y = F; y <= T; y++) {
                    if (divider) for (int z = r.z0; z < barZ; z++) c.set(x, y, z, Pal.DS_BRICKS);
                    c.set(x, y, barZ, divider ? Pal.DS_BRICKS : Pal.connected(Blocks.IRON_BARS, Direction.Axis.X));
                }
                if (!divider) {
                    if (Hash.pct(x, 0, barZ, 3) < 30) c.set(x, F, r.z0, Pal.b(Blocks.SKELETON_SKULL));
                    else if (Hash.pct(x, 0, barZ, 3) < 45) c.set(x, F, r.z0, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.X));
                    c.set(x, T, r.z0 + 1, Pal.CHAIN);
                    c.set(x, T - 1, r.z0 + 1, Pal.CHAIN);
                }
                for (int z = r.z0; z <= barZ; z++) use(x, z);
            }
        }
        cobwebs(0.4);
        torches(6);
    }

    private void crypt() {
        floor((x, y, z) -> Hash.pct(x, y, z, 43) < 80 ? Pal.DS_TILES : Pal.DS_TILES_CRACKED);
        boolean alongX = r.w() >= r.d();
        for (int a = 2; a < (alongX ? r.w() : r.d()) - 2; a += 3) {
            for (int side = 0; side < 2; side++) {
                int b = side == 0 ? 2 : (alongX ? r.d() : r.w()) - 3;
                int x = alongX ? r.x0 + a : r.x0 + b;
                int z = alongX ? r.z0 + b : r.z0 + a;
                // sarcofago de 1x2
                int x2 = alongX ? x : x + (side == 0 ? 1 : -1);
                int z2 = alongX ? z + (side == 0 ? 1 : -1) : z;
                if (!free(x, z) || !free(x2, z2)) continue;
                c.set(x, F, z, Pal.PB_BRICKS);
                c.set(x2, F, z2, Pal.PB_BRICKS);
                c.set(x, F + 1, z, Pal.slab(Blocks.POLISHED_BLACKSTONE_SLAB, false));
                c.set(x2, F + 1, z2, Hash.pct(x, F, z, 5) < 50 ? Pal.b(Blocks.SKELETON_SKULL) : Pal.candles(Blocks.BLACK_CANDLE, 2));
                use(x, z);
                use(x2, z2);
            }
        }
        for (int x = r.x0; x <= r.x1; x++)
            for (int z = r.z0; z <= r.z1; z++)
                if (Hash.pct(x, F, z, 44) < 4 && c.isAir(x, F, z)) c.set(x, F, z, Pal.b(Blocks.SCULK_VEIN).setValue(BlockStateProperties.DOWN, true));
        chestAt(r.cx(), r.z1, Direction.NORTH, "cripta");
        cobwebs(0.5);
        torches(7);
    }

    private void alchemy() {
        floor((x, y, z) -> ((x + z) & 1) == 0 ? Pal.DS_POLISHED : Pal.PB);
        for (WallCell w : wallCells()) {
            if (!free(w.x, w.z)) continue;
            int roll = Hash.pct(w.x, F, w.z, 45);
            if (roll < 30) {
                c.set(w.x, F, w.z, Pal.slab(Blocks.POLISHED_BLACKSTONE_SLAB, true));
                c.set(w.x, F + 1, w.z, roll < 12 ? Pal.b(Blocks.BREWING_STAND) : Pal.b(roll < 20 ? Blocks.POTTED_CRIMSON_FUNGUS : Blocks.POTTED_WITHER_ROSE));
            } else if (roll < 45) {
                c.set(w.x, F, w.z, Pal.b(Blocks.CAULDRON));
            } else if (roll < 60) {
                c.set(w.x, F, w.z, Pal.BOOKSHELF);
                c.set(w.x, F + 1, w.z, Pal.BOOKSHELF);
            }
            use(w.x, w.z);
        }
        standing(r.cx(), r.cz(), Pal.b(Blocks.ENCHANTING_TABLE));
        chestAt(r.x0 + 1, r.z1, Direction.NORTH, "alquimia");
        chandelier(r.cx() + 1, r.cz() + 1, false);
    }

    private void ballroom() {
        checker(Pal.CALCITE, Pal.PB, 1);
        for (int x = r.x0 + 2; x <= r.x1 - 2; x += 5) {
            pillar(x, r.z0 + 1, (a, b, cc) -> Pal.BASALT);
            pillar(x, r.z1 - 1, (a, b, cc) -> Pal.BASALT);
        }
        // escenario de musicos
        for (int x = r.cx() - 3; x <= r.cx() + 3; x++)
            for (int z = r.z0 + 3; z <= r.z0 + 5; z++) {
                c.set(x, F, z, Pal.slab(Blocks.DARK_OAK_SLAB, false));
                use(x, z);
            }
        c.set(r.cx(), F, r.z0 + 4, Pal.b(Blocks.JUKEBOX));
        c.set(r.cx() - 2, F, r.z0 + 4, Pal.b(Blocks.NOTE_BLOCK));
        c.set(r.cx() + 2, F, r.z0 + 4, Pal.b(Blocks.NOTE_BLOCK));
        chandeliers(true);
        banners(5);
    }

    private void servants() {
        floor((x, y, z) -> Pal.SP_PLANKS);
        for (int x = r.x0; x + 1 <= r.x1; x += 3) {
            bed(x, r.z0, Direction.SOUTH, Blocks.BROWN_BED);
            if (r.d() >= 6) bed(x, r.z1, Direction.NORTH, Blocks.BROWN_BED);
            if (Hash.pct(x, 0, r.z0, 8) < 40) chestAt(x + 1, r.z0, Direction.SOUTH, "dormitorio");
        }
        chandeliers(false);
    }

    private void barracks() {
        floor((x, y, z) -> Pal.SP_PLANKS);
        for (int x = r.x0; x <= r.x1; x += 2) {
            bed(x, r.z0, Direction.SOUTH, Blocks.RED_BED);
            if (r.d() >= 6) bed(x, r.z1, Direction.NORTH, Blocks.RED_BED);
        }
        int i = 0;
        for (int x = r.x0 + 2; x <= r.x1 - 2; x += 4) armorStand(x, r.cz(), (i++ & 1) == 0 ? Direction.NORTH : Direction.SOUTH, false);
        chestAt(r.x0, r.cz(), Direction.EAST, "armeria");
        chandeliers(false);
        torches(6);
    }

    private void smithy() {
        floor((x, y, z) -> Pal.foundation(x, y, z));
        fireplace(r.cx(), r.z0, Direction.SOUTH);
        alongWalls(0.5, Pal.facing(Blocks.ANVIL, Direction.NORTH), Pal.facing(Blocks.CHIPPED_ANVIL, Direction.EAST),
                Pal.facing(Blocks.BLAST_FURNACE, Direction.NORTH), Pal.b(Blocks.GRINDSTONE), Pal.b(Blocks.SMITHING_TABLE),
                Pal.b(Blocks.WATER_CAULDRON).setValue(LayeredCauldronBlock.LEVEL, 3));
        chestAt(r.x1, r.z1, Direction.NORTH, "armeria");
        chandeliers(false);
    }

    private void stable() {
        floor((x, y, z) -> Hash.pct(x, y, z, 46) < 50 ? Pal.COARSE_DIRT : Pal.PODZOL);
        boolean alongX = r.w() >= r.d();
        if (alongX) {
            for (int x = r.x0; x <= r.x1; x++) {
                boolean div = (x - r.x0) % 4 == 0;
                for (int z = r.z0; z <= r.z0 + 3 && z <= r.z1; z++) {
                    if (div) c.set(x, F, z, Pal.connected(Blocks.DARK_OAK_FENCE, Direction.Axis.Z));
                    else if (z == r.z0) c.set(x, F, z, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.X));
                    use(x, z);
                }
                if (!div && (x - r.x0) % 4 == 2) c.set(x, F, r.z0 + 1, Pal.b(Blocks.WATER_CAULDRON).setValue(LayeredCauldronBlock.LEVEL, 3));
            }
        }
        chandeliers(false);
    }

    private void observatory() {
        floor((x, y, z) -> ((x + z) & 1) == 0 ? Pal.DS_POLISHED : Pal.CALCITE);
        standing(r.cx(), r.cz(), Pal.b(Blocks.LODESTONE));
        c.set(r.cx(), F + 1, r.cz(), Pal.b(Blocks.AMETHYST_CLUSTER));
        alongWalls(0.4, Pal.b(Blocks.CARTOGRAPHY_TABLE), Pal.BOOKSHELF, Pal.facing(Blocks.LECTERN, Direction.NORTH));
        chestAt(r.x0, r.z0, Direction.SOUTH, "biblioteca");
    }
}
