package com.darkcraft.gen;

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
        return new ResourceLocation("darkcraft", "chests/" + name);
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
        // debajo de un atrio no hay techo en T: la cadena sube hasta el techo de verdad
        int top = (room.atrium != null && room.atrium.contains(x, z)) ? room.atriumTop - 1 : T;
        BlockState mod = large ? ModDeco.chandelier(Hash.pct(x, T, z, 3) < 35, room.height < 8)
                : ModDeco.hangingLantern(Hash.h(x, T, z, 4));
        if (mod != null) {
            int yy = Math.max(F + 3, y0);
            for (int y = top; y > yy; y--) c.set(x, y, z, Pal.CHAIN);
            c.set(x, yy, z, mod);
            return;
        }
        if (y0 < F + 3) {
            for (int y = top; y > T; y--) c.set(x, y, z, Pal.CHAIN);
            c.set(x, T, z, Pal.SOUL_LANTERN_HANG);
            return;
        }
        for (int y = top; y > y0; y--) c.set(x, y, z, Pal.CHAIN);
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
            BlockState mod = ModDeco.wallLight(w.in.getOpposite(), i / every);
            c.setIfAir(w.x, F + 2, w.z, mod != null ? mod : Blocks.SOUL_WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, w.in));
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
        int ax0 = Math.min(x0, x1), ax1 = Math.max(x0, x1), az0 = Math.min(z0, z1), az1 = Math.max(z0, z1);
        boolean single = ax0 == ax1 && az0 == az1;
        boolean line = ax0 == ax1 || az0 == az1;
        for (int x = ax0; x <= ax1; x++)
            for (int z = az0; z <= az1; z++) {
                if (room.clear(x, z) || !r.contains(x, z)) continue;
                BlockState m = null;
                if (single) m = ModDeco.tableSingle();
                else if (line) {
                    boolean alongX = az0 == az1;
                    m = ModDeco.tableLong(alongX ? Direction.Axis.X : Direction.Axis.Z,
                            alongX ? x == ax0 : z == az0, alongX ? x == ax1 : z == az1);
                }
                c.set(x, F, z, m != null ? m : Pal.slab(slab, true));
                use(x, z);
            }
    }

    /** Pone cosas encima de una mesa (platos, copas, velas, libros...). */
    private void onTable(int x, int z, boolean study) {
        if (!used.contains(Room.key(x, z))) return;
        BlockState m = study ? ModDeco.studyItem(Hash.h(x, F, z, 61)) : ModDeco.tableware(Hash.h(x, F, z, 62));
        if (m != null) c.set(x, F + 1, z, m);
        else if (Hash.pct(x, F, z, 63) < 40) c.set(x, F + 1, z, Pal.candles(Blocks.WHITE_CANDLE, 1 + Hash.pct(x, 0, z, 2) % 3));
    }

    /** Apliques, trofeos y estantes en las paredes a media altura. */
    private void wallDecor(int every, boolean trophies) {
        wallDecor(every, trophies, false);
    }

    private void wallDecor(int every, boolean trophies, boolean lightsOnly) {
        if (room.height < 5) return;
        int i = 0;
        for (WallCell w : wallCells()) {
            i++;
            if (i % every != 0 || room.clear(w.x, w.z)) continue;
            int y = F + 2 + (room.height >= 8 ? 1 : 0);
            if (!c.isAir(w.x, y, w.z)) continue;
            BlockState m;
            int k = lightsOnly ? 2 : (i / every) % 3;
            if (trophies && k == 0) m = ModDeco.trophy(w.in.getOpposite(), i);
            else if (k == 1) m = ModDeco.wallShelf(w.in.getOpposite());
            else m = ModDeco.wallLight(w.in.getOpposite(), i);
            if (m != null) c.set(w.x, y, w.z, m);
        }
    }

    private enum Fill { FURNITURE, PLANTS, SIDEBOARD, CLUTTER, CRYPT, ARMORY, ALCHEMY, STABLE }

    private BlockState plant(int v) {
        BlockState m = ModDeco.pot(v);
        if (m != null && v % 3 != 0) return m;
        Block[] potted = {Blocks.POTTED_FERN, Blocks.POTTED_AZALEA, Blocks.POTTED_FLOWERING_AZALEA, Blocks.POTTED_WITHER_ROSE, Blocks.POTTED_DEAD_BUSH};
        return Pal.b(potted[Math.floorMod(v, potted.length)]);
    }

    /** Rellena huecos libres junto a las paredes con cosas que pegan con el tipo de sala. */
    private void fillWalls(double p, Fill kind) {
        int i = 0;
        for (WallCell w : wallCells()) {
            i++;
            if (!free(w.x, w.z) || !c.isAir(w.x, F, w.z) || Hash.f(w.x, F, w.z, 64) > p) continue;
            int k = Hash.h(w.x, F, w.z, 65);
            BlockState m = switch (kind) {
                case FURNITURE -> switch (k % 5) {
                    case 0 -> ModDeco.nightstand(w.in);
                    case 1 -> ModDeco.drawer(w.in, i);
                    case 2 -> ModDeco.cupboard(w.in);
                    case 3 -> ModDeco.lamp(i);
                    default -> plant(i);
                };
                case PLANTS -> k % 4 == 0 ? ModDeco.candleHolder(w.in, i) : plant(i);
                case SIDEBOARD -> k % 3 == 0 ? plant(i) : (k % 3 == 1 ? ModDeco.cupboard(w.in) : ModDeco.drawer(w.in, i));
                case CLUTTER -> ModDeco.clutter(i, false);
                case CRYPT -> ModDeco.clutter(i, true);
                case ARMORY -> k % 3 == 0 ? Mods.state("supplementaries:cannonball", "balls=" + (1 + i % 4)) : (k % 3 == 1 ? Pal.b(Blocks.BARREL) : Blocks.GRINDSTONE.defaultBlockState().setValue(GrindstoneBlock.FACE, net.minecraft.world.level.block.state.properties.AttachFace.FLOOR));
                case ALCHEMY -> k % 3 == 0 ? Mods.state("supplementaries:jar") : (k % 3 == 1 ? plant(i) : ModDeco.candleHolder(w.in, i));
                case STABLE -> k % 2 == 0 ? Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Y) : Pal.b(Blocks.BARREL);
            };
            if (m == null) {
                m = switch (kind) {
                    case PLANTS, SIDEBOARD, FURNITURE -> plant(i);
                    case CRYPT -> k % 2 == 0 ? Pal.candles(Blocks.BLACK_CANDLE, 3) : Pal.b(Blocks.SKELETON_SKULL);
                    case ALCHEMY -> Pal.b(Blocks.BREWING_STAND);
                    default -> Town.clutter(w.x, F, w.z);
                };
            }
            if (isLight(m)) {
                // las lamparas y las velas nunca van en el suelo: siempre encima de un mueble
                if (room.window(w.x, w.z) || room.height < 3) m = plant(i);
                else {
                    c.set(w.x, F, w.z, support(kind, w.in, i));
                    c.set(w.x, F + 1, w.z, m);
                    use(w.x, w.z);
                    continue;
                }
            }
            c.set(w.x, F, w.z, m);
            use(w.x, w.z);
        }
    }

    /** Velas, faroles, candelabros y lamparas (de vanilla o de mods). */
    static boolean isLight(BlockState s) {
        if (s == null) return false;
        Block b = s.getBlock();
        if (b instanceof CandleBlock || b instanceof LanternBlock) return true;
        var k = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(b);
        if (k == null || k.getNamespace().equals("minecraft")) return false;
        String p = k.getPath();
        return p.contains("candle") || p.contains("lamp") || p.contains("lantern") || p.contains("sconce");
    }

    /** Mueble bajo donde apoyar una lampara o unas velas, segun el tipo de sala. */
    private BlockState support(Fill kind, Direction in, int v) {
        BlockState m = switch (kind) {
            case FURNITURE, PLANTS, SIDEBOARD -> v % 2 == 0 ? ModDeco.nightstand(in) : ModDeco.drawer(in, v);
            case CRYPT -> Pal.PB_CHISELED;
            case ALCHEMY -> Pal.slab(Blocks.POLISHED_BLACKSTONE_SLAB, true);
            default -> Pal.b(Blocks.BARREL);
        };
        return m != null ? m : Pal.slab(Blocks.DARK_OAK_SLAB, true);
    }

    /** Mesita con una lampara encima (o velas si no hay mods). */
    private void lampTable(int x, int z, Direction front, int v) {
        if (!free(x, z)) return;
        BlockState t = v % 2 == 0 ? ModDeco.nightstand(front) : ModDeco.drawer(front, v);
        c.set(x, F, z, t != null ? t : Pal.slab(Blocks.DARK_OAK_SLAB, true));
        use(x, z);
        if (room.height < 3) return;
        BlockState l = ModDeco.lamp(v);
        if (l == null) l = ModDeco.candleHolder(front, v);
        if (l == null) l = v % 3 == 0 ? Pal.LANTERN : Pal.candles(v % 2 == 0 ? Blocks.RED_CANDLE : Blocks.WHITE_CANDLE, 3);
        c.set(x, F + 1, z, l);
    }

    /** Un par de sofas mirandose (o mirando al centro). */
    private void sofas(int x, int z, Direction look, int n, boolean black) {
        Direction side = look.getClockWise();
        for (int k = 0; k < n; k++) {
            int px = x + side.getStepX() * k, pz = z + side.getStepZ() * k;
            if (!free(px, pz)) continue;
            int part = n == 1 ? 0 : (k == 0 ? 1 : (k == n - 1 ? 3 : 2));
            BlockState m = ModDeco.sofa(look, black, part);
            c.set(px, F, pz, m != null ? m : Pal.stairs(Blocks.CRIMSON_STAIRS, look.getOpposite(), false));
            use(px, pz);
        }
    }

    /** Silla mirando hacia "look" (el respaldo queda al lado contrario). */
    private void chair(int x, int z, Direction look, Block stairs) {
        if (!free(x, z)) return;
        BlockState m = stairs == Blocks.DARK_OAK_STAIRS ? ModDeco.chair(look, Hash.h(x, F, z, 7)) : null;
        c.set(x, F, z, m != null ? m : Pal.stairs(stairs, look.getOpposite(), false));
        use(x, z);
    }

    private void bed(int hx, int hz, Direction in, Block bed) {
        int fx = hx + in.getStepX(), fz = hz + in.getStepZ();
        if (!free(hx, hz) || !free(fx, fz)) return;
        Direction facing = in.getOpposite();
        String col = bed == Blocks.BLACK_BED ? "black" : bed == Blocks.GRAY_BED ? "gray" : bed == Blocks.PURPLE_BED ? "purple" : bed == Blocks.BROWN_BED ? "brown" : "red";
        BlockState mh = ModDeco.bed(facing, true, col), mf = ModDeco.bed(facing, false, col);
        if (mh != null && mf != null && bed != Blocks.BROWN_BED) {
            c.set(hx, F, hz, mh);
            c.set(fx, F, fz, mf);
        } else {
            c.set(hx, F, hz, bed.defaultBlockState().setValue(BedBlock.FACING, facing).setValue(BedBlock.PART, BedPart.HEAD));
            c.set(fx, F, fz, bed.defaultBlockState().setValue(BedBlock.FACING, facing).setValue(BedBlock.PART, BedPart.FOOT));
        }
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
            case BALLROOM, LIVING -> living();
            case SERVANTS -> servants();
            case BARRACKS -> barracks();
            case SMITHY -> smithy();
            case STABLE -> stable();
            case OBSERVATORY -> observatory();
            default -> {
            }
        }
        if (room.type == Room.Type.EMPTY) return;
        // relleno final: cada tipo de sala solo con cosas que le pegan
        switch (room.type) {
            case THRONE, KITCHEN, LIBRARY, LIBRARY_GRAND, OBSERVATORY -> {
                // la sala del trono queda limpia; cocina y bibliotecas ya van completas
            }
            case BALLROOM, LIVING -> fillWalls(0.6, Fill.PLANTS);
            case GREAT_HALL -> {
                wallDecor(6, true, false);
                fillWalls(0.15, Fill.PLANTS);
            }
            case CHAPEL, GALLERY -> {
                wallDecor(6, false, true);
                fillWalls(0.15, Fill.PLANTS);
            }
            case DINING -> {
                wallDecor(6, false, true);
                fillWalls(0.3, Fill.SIDEBOARD);
            }
            case BEDROOM, STUDY, SERVANTS, BARRACKS -> {
                wallDecor(7, false, false);
                fillWalls(0.3, Fill.FURNITURE);
            }
            case STORAGE, CELLAR, WINE_CELLAR -> {
                wallDecor(9, false, true);
                fillWalls(0.7, Fill.CLUTTER);
            }
            case CRYPT, TREASURY, PRISON -> {
                wallDecor(7, false, true);
                fillWalls(0.35, Fill.CRYPT);
            }
            case ARMORY, SMITHY -> {
                wallDecor(5, true, false);
                fillWalls(0.35, Fill.ARMORY);
            }
            case ALCHEMY -> {
                wallDecor(7, false, false);
                fillWalls(0.4, Fill.ALCHEMY);
            }
            case BATH -> {
                wallDecor(6, false, true);
                fillWalls(0.3, Fill.PLANTS);
            }
            case STABLE -> fillWalls(0.4, Fill.STABLE);
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
                    onTable(x, tz, false);
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
        // chimeneas en los extremos con sofas delante
        fireplace(alongX ? r.x0 : r.cx(), alongX ? r.cz() : r.z0, alongX ? Direction.EAST : Direction.SOUTH);
        fireplace(alongX ? r.x1 : r.cx(), alongX ? r.cz() : r.z1, alongX ? Direction.WEST : Direction.NORTH);
        if (alongX) {
            sofas(r.x0 + 4, r.cz() - 1, Direction.WEST, 3, false);
            sofas(r.x1 - 4, r.cz() + 1, Direction.EAST, 3, false);
        } else {
            sofas(r.cx() + 1, r.z0 + 4, Direction.NORTH, 3, false);
            sofas(r.cx() - 1, r.z1 - 4, Direction.SOUTH, 3, false);
        }
        chandeliers(true);
        torches(5);
    }

    private void fireplace(int x, int z, Direction in) {
        Direction side = in.getClockWise();
        for (int k = -2; k <= 2; k++)
            for (int d = 0; d <= 1; d++) {
                int px = x + side.getStepX() * k + in.getStepX() * d, pz = z + side.getStepZ() * k + in.getStepZ() * d;
                if (room.clear(px, pz)) return;
            }
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
        // el trono siempre al fondo (pared norte), mirando a la puerta del gran salon
        boolean northEnd = r.d() >= 12 || r.d() >= r.w();
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
            // trono a la medida del Rey (unos 3 bloques de alto): asiento de 3, brazos y respaldo alto
            int ty = F + 3, tz = r.z0 + 1;
            for (int dx = -1; dx <= 1; dx++) c.set(xc + dx, ty, tz, Pal.stairs(Blocks.POLISHED_BLACKSTONE_STAIRS, Direction.NORTH, false));
            for (int s = -1; s <= 1; s += 2) {
                c.set(xc + 2 * s, ty, tz, Pal.PB_CHISELED);
                c.set(xc + 2 * s, ty + 1, tz, Pal.slab(Blocks.POLISHED_BLACKSTONE_SLAB, false));
                // velas a los lados, encima de un pedestal
                c.set(xc + 3 * s, ty, tz, Pal.PB_CHISELED);
                BlockState ch1 = ModDeco.candleHolder(Direction.SOUTH, 0);
                c.set(xc + 3 * s, ty + 1, tz, ch1 != null ? ch1 : Pal.candles(Blocks.RED_CANDLE, 3));
            }
            for (int dx = -2; dx <= 2; dx++)
                for (int y = ty; y <= ty + 7; y++) {
                    boolean side = Math.abs(dx) == 2;
                    if (side && y > ty + 5) continue;
                    c.set(xc + dx, y, r.z0, y == ty + 7 || (side && y == ty + 5) ? Pal.GILDED : (side ? Pal.PB_CHISELED : Pal.PB_BRICKS));
                }
            c.set(xc, ty + 8, r.z0, Pal.b(Blocks.WITHER_SKELETON_SKULL));
            // braseros
            for (int s = -1; s <= 1; s += 2) {
                int bx = xc + s * 6;
                c.set(bx, F + 2, r.z0 + 4, Pal.PB_CHISELED);
                c.set(bx, F + 3, r.z0 + 4, Pal.SOUL_CAMPFIRE);
            }
            for (int z = r.z0 + 8; z <= r.z1 - 3; z += 6) {
                pillar(xc - 8, z, (a, b, cc) -> Pal.BASALT);
                pillar(xc + 8, z, (a, b, cc) -> Pal.BASALT);
                for (int sx = -1; sx <= 1; sx += 2) {
                    if (room.clear(xc + sx * 8, z + 1)) continue;
                    c.set(xc + sx * 8, F, z + 1, Pal.PB_CHISELED);
                    c.set(xc + sx * 8, F + 1, z + 1, Pal.SOUL_CAMPFIRE);
                }
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
                onTable(x, z, false);
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

    // ================================================================== COCINA
    private void kitchen() {
        checker(Pal.DS_POLISHED, Pal.DS_TILES, 2);
        // 1) gran hogar en la pared norte (o en la sur si en la norte hay una puerta)
        boolean north = hearthFits(r.z0, Direction.SOUTH);
        boolean south = !north && hearthFits(r.z1, Direction.NORTH);
        if (north) hearth(r.z0, Direction.SOUTH);
        else if (south) hearth(r.z1, Direction.NORTH);
        // 2) vigas en el techo con hierbas secandose
        beams();
        // 3) despensa en una esquina
        pantry(north ? r.z1 : r.z0, north ? Direction.NORTH : Direction.SOUTH);
        // 4) encimeras por todas las paredes: fregadero, hornos con campana, armarios altos
        counters();
        // 5) isla central para preparar la comida
        island();
        // 6) luz
        chandeliers(false);
        chestAt(r.x0, north ? r.z1 : r.z0, north ? Direction.NORTH : Direction.SOUTH, "cocina");
    }

    private int hearthHalf() {
        return r.w() >= 11 ? 3 : (r.w() >= 7 ? 2 : 1);
    }

    private boolean hearthFits(int zWall, Direction in) {
        if (r.d() < 5 || r.w() < 5) return false;
        int half = hearthHalf();
        for (int k = -half; k <= half; k++)
            for (int dz = 0; dz <= 1; dz++)
                if (!free(r.cx() + k, zWall + in.getStepZ() * dz)) return false;
        return true;
    }

    /** Chimenea de cocina: hogar con fuego y ahumadores, campana de ladrillo hasta el techo. */
    private void hearth(int zWall, Direction in) {
        int half = hearthHalf();
        int zf = zWall + in.getStepZ();
        int cx = r.cx();
        BlockState brick = Pal.b(Blocks.BRICKS);
        for (int k = -half; k <= half; k++) {
            int x = cx + k;
            boolean jamb = Math.abs(k) == half;
            // fondo
            c.column(x, zWall, F, T, jamb ? Pal.PB_BRICKS : brick);
            // frente
            if (jamb) {
                c.column(x, zf, F, F + 3, Pal.PB_BRICKS);
            } else {
                c.set(x, F - 1, zf, Pal.NETHER_BRICKS);
                c.set(x, F - 1, zWall, Pal.NETHER_BRICKS);
                c.column(x, zf, F, F + 2, Pal.AIR);
                c.set(x, F + 3, zf, Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, in.getOpposite(), true));
                // fuego en el centro, ahumadores encendidos a los lados
                BlockState inside;
                if (k == 0) inside = Pal.CAMPFIRE.setValue(CampfireBlock.FACING, in);
                else if (Math.abs(k) == 1) inside = Pal.facing(Blocks.SMOKER, in).setValue(AbstractFurnaceBlock.LIT, true);
                else inside = Pal.b(Blocks.WATER_CAULDRON).setValue(LayeredCauldronBlock.LEVEL, 3);
                c.set(x, F, zWall, inside);
            }
            // pecho de la chimenea hasta el techo
            if (T >= F + 4) c.column(x, zf, F + 4, T, jamb ? Pal.PB_BRICKS : brick);
            use(x, zWall);
            use(x, zf);
        }
        // apliques en la pared a los lados del hogar
        if (T >= F + 4) {
            for (int s = -1; s <= 1; s += 2) {
                int x = cx + s * (half + 1);
                if (!r.contains(x, zWall) || room.clear(x, zWall)) continue;
                BlockState l = ModDeco.wallLight(in.getOpposite(), s + 2);
                c.set(x, F + 3, zWall, l != null ? l : Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, in));
            }
        }
    }

    private void beams() {
        if (room.height < 6) return;
        boolean alongX = r.w() >= r.d();
        for (int a = 2; a < (alongX ? r.w() : r.d()) - 1; a += 4) {
            for (int b = 0; b < (alongX ? r.d() : r.w()); b++) {
                int x = alongX ? r.x0 + a : r.x0 + b, z = alongX ? r.z0 + b : r.z0 + a;
                if (!c.isAir(x, T, z)) continue;
                c.set(x, T, z, Pal.log(Blocks.STRIPPED_DARK_OAK_LOG, alongX ? Direction.Axis.Z : Direction.Axis.X));
                // manojos de hierbas secandose
                if (Hash.pct(x, T, z, 81) < 30 && c.isAir(x, T - 1, z)) c.set(x, T - 1, z, Pal.b(Blocks.HANGING_ROOTS));
            }
        }
    }

    /** Despensa en las dos esquinas de la pared contraria al hogar. */
    private void pantry(int zWall, Direction in) {
        int[] xs = {r.x0, r.x1};
        for (int xc : xs) {
            int dirx = xc == r.x0 ? 1 : -1;
            for (int a = 0; a < 3; a++)
                for (int b = 0; b < 2; b++) {
                    int x = xc + dirx * a, z = zWall + in.getStepZ() * b;
                    if (!free(x, z) || (a == 2 && b == 1)) continue;
                    int roll = Hash.pct(x, F, z, 82);
                    BlockState m;
                    if (roll < 30) {
                        m = Pal.b(Blocks.BARREL);
                        if (room.height >= 4) c.set(x, F + 1, z, roll < 15 ? Pal.b(Blocks.BARREL) : Pal.b(Blocks.PUMPKIN));
                    } else if (roll < 45) m = Pal.log(Blocks.HAY_BLOCK, Direction.Axis.Y);
                    else if (roll < 55) m = Pal.b(Blocks.MELON);
                    else {
                        m = ModDeco.clutter(roll, false);
                        if (m == null) m = Pal.b(Blocks.BARREL);
                    }
                    c.set(x, F, z, m);
                    use(x, z);
                }
        }
    }

    /** Encimeras por las paredes libres, con fregadero, hornos y armarios altos. */
    private void counters() {
        List<WallCell> cells = new ArrayList<>();
        for (WallCell w : wallCells()) if (free(w.x, w.z) && c.isAir(w.x, F, w.z)) cells.add(w);
        int i = 0;
        boolean sinkDone = false;
        for (WallCell w : cells) {
            i++;
            int kind;
            if (!sinkDone && i % 7 == 3) {
                kind = 1;
                sinkDone = true;
            } else if (i % 5 == 0) kind = 2;
            else kind = 0;
            Direction out = w.in.getOpposite();
            BlockState m = ModDeco.kitchenCounter(w.in, kind, i);
            if (m == null) {
                m = switch (kind) {
                    case 1 -> Pal.b(Blocks.WATER_CAULDRON).setValue(LayeredCauldronBlock.LEVEL, 3);
                    case 2 -> Pal.facing(i % 2 == 0 ? Blocks.FURNACE : Blocks.SMOKER, w.in).setValue(AbstractFurnaceBlock.LIT, true);
                    default -> i % 4 == 1 ? Pal.b(Blocks.BARREL) : Pal.slab(Blocks.POLISHED_BLACKSTONE_SLAB, true);
                };
            }
            c.set(w.x, F, w.z, m);
            use(w.x, w.z);
            // encima de la encimera
            if (kind == 0 && Hash.pct(w.x, F, w.z, 83) < 60) {
                BlockState top = ModDeco.kitchenItem(Hash.h(w.x, F, w.z, 84));
                if (top == null) top = vanillaKitchenItem(Hash.h(w.x, F, w.z, 85));
                c.set(w.x, F + 1, w.z, top);
            }
            if (room.height < 5) continue;
            if (kind == 1) {
                BlockState tap = ModDeco.faucet(out);
                if (tap != null) c.set(w.x, F + 1, w.z, tap);
            } else if (kind == 2) {
                BlockState hood = ModDeco.kitchenHood(w.in);
                if (hood != null) {
                    c.set(w.x, F + 2, w.z, hood);
                    BlockState pipe = ModDeco.kitchenHoodPipe(w.in);
                    if (pipe != null) for (int y = F + 3; y <= T; y++) c.set(w.x, y, w.z, pipe);
                }
            } else if (Hash.pct(w.x, F, w.z, 86) < 70) {
                BlockState cab = ModDeco.upperCabinet(w.in, i);
                if (cab != null) c.set(w.x, F + 2, w.z, cab);
                else {
                    // balda de madera con tarros y macetas
                    c.set(w.x, F + 2, w.z, Pal.slab(Blocks.DARK_OAK_SLAB, true));
                    if (room.height >= 5) c.set(w.x, F + 3, w.z, vanillaKitchenItem(Hash.h(w.x, F + 3, w.z, 87)));
                }
            }
        }
    }

    private BlockState vanillaKitchenItem(int v) {
        Block[] b = {Blocks.POTTED_FERN, Blocks.DECORATED_POT, Blocks.FLOWER_POT, Blocks.POTTED_RED_MUSHROOM, Blocks.POTTED_BROWN_MUSHROOM};
        int k = Math.floorMod(v, 7);
        if (k == 5) return Pal.candles(Blocks.CANDLE, 1 + Math.floorMod(v, 3));
        if (k == 6) return Pal.b(Blocks.CAKE);
        return Pal.b(b[k % b.length]);
    }

    /** Isla de trabajo en el centro con taburetes y comida encima. */
    private void island() {
        if (r.w() < 9 || r.d() < 9) {
            if (r.w() >= 5 && r.d() >= 5 && free(r.cx(), r.cz())) {
                table(r.cx(), r.cz(), r.cx(), r.cz(), Blocks.SPRUCE_SLAB);
                c.set(r.cx(), F + 1, r.cz(), vanillaKitchenItem(r.cx() * 7 + r.cz()));
                chair(r.cx(), r.cz() + 1, Direction.NORTH, Blocks.SPRUCE_STAIRS);
            }
            return;
        }
        boolean alongX = r.w() >= r.d();
        int len = Math.max(3, Math.min(12, (alongX ? r.w() : r.d()) - 8));
        int wide = (alongX ? r.d() : r.w()) >= 13 ? 2 : 1;
        int a0 = (alongX ? r.cx() : r.cz()) - len / 2;
        int b0 = (alongX ? r.cz() : r.cx()) - (wide - 1) / 2;
        for (int b = 0; b < wide; b++) {
            int bb = b0 + b;
            for (int a = 0; a < len; a++) {
                int x = alongX ? a0 + a : bb, z = alongX ? bb : a0 + a;
                if (!free(x, z)) continue;
                BlockState t = ModDeco.tableLong(alongX ? Direction.Axis.X : Direction.Axis.Z, a == 0, a == len - 1, true);
                c.set(x, F, z, t != null ? t : Pal.slab(Blocks.SPRUCE_SLAB, true));
                use(x, z);
                BlockState top = ModDeco.kitchenItem(Hash.h(x, F, z, 88));
                if (top == null || Hash.pct(x, F, z, 89) < 25) top = (a % 4 == 1) ? Pal.b(Blocks.MELON) : vanillaKitchenItem(Hash.h(x, F, z, 90));
                if (Hash.pct(x, F, z, 91) < 80) c.set(x, F + 1, z, top);
            }
        }
        // taburetes a los lados largos
        for (int a = 1; a < len; a += 2) {
            for (int side = -1; side <= 1; side += 2) {
                int bb = side < 0 ? b0 - 1 : b0 + wide;
                int x = alongX ? a0 + a : bb, z = alongX ? bb : a0 + a;
                if (!free(x, z)) continue;
                BlockState st = ModDeco.stool(a + side);
                if (st != null) {
                    c.set(x, F, z, st);
                    use(x, z);
                } else {
                    Direction look = alongX ? (side < 0 ? Direction.SOUTH : Direction.NORTH) : (side < 0 ? Direction.EAST : Direction.WEST);
                    chair(x, z, look, Blocks.SPRUCE_STAIRS);
                }
            }
        }
        // faroles y cestas colgando sobre la isla
        for (int a = 0; a < len; a += 3) {
            int x = alongX ? a0 + a : b0, z = alongX ? b0 : a0 + a;
            int bottom = Math.max(F + 3, T - 3);
            for (int y = T; y > bottom; y--) c.setIfAir(x, y, z, Pal.CHAIN);
            BlockState basket = Mods.state("supplementaries:lunch_basket", "hanging=true");
            c.setIfAir(x, bottom, z, (a % 6 == 3 && basket != null) ? basket : Pal.LANTERN_HANG);
        }
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
            standing(cx + 3, cz, Pal.facing(Blocks.LECTERN, Direction.WEST));
            standing(cx - 3, cz, Pal.facing(Blocks.LECTERN, Direction.EAST));
            sofas(cx - 2, cz + 5, Direction.SOUTH, 5, true);
            sofas(cx + 2, cz - 5, Direction.NORTH, 5, true);
        } else if (free(cx, cz)) {
            table(cx, cz, cx + (alongX ? 1 : 0), cz + (alongX ? 0 : 1), Blocks.DARK_OAK_SLAB);
            onTable(cx, cz, true);
            onTable(cx + (alongX ? 1 : 0), cz + (alongX ? 0 : 1), true);
            chair(cx, cz + (alongX ? 1 : 0) + (alongX ? 0 : 2), alongX ? Direction.NORTH : Direction.NORTH, Blocks.DARK_OAK_STAIRS);
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
                BlockState ns = ModDeco.nightstand(Direction.SOUTH);
                c.set(nx, F, hz, ns != null ? ns : Pal.b(Blocks.SPRUCE_PLANKS));
                BlockState cand = ModDeco.candleHolder(Direction.SOUTH, nx);
                c.set(nx, F + 1, hz, cand != null ? cand : Pal.candles(Blocks.BLACK_CANDLE, 1 + rng.nextInt(3)));
                use(nx, hz);
            }
        }
        // armario
        if (free(r.x0, r.z1) && free(r.x0 + 1, r.z1)) {
            for (int x = r.x0; x <= r.x0 + 1; x++) {
                BlockState cup = ModDeco.cupboard(Direction.NORTH);
                for (int y = F; y <= F + 2 && y <= T; y++) c.set(x, y, r.z1, cup != null && y == F ? cup : Pal.DO_STRIPPED);
                c.set(x, F + 1, r.z1 - 1, Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.NORTH)
                        .setValue(TrapDoorBlock.OPEN, true));
                use(x, r.z1);
            }
        }
        chestAt(hx, hz + 2, Direction.SOUTH, "dormitorio");
        chestAt(r.x1, r.z1, Direction.NORTH, "dormitorio");
        // escritorio con silla y un par de sofas si cabe
        BlockState desk = ModDeco.desk(Direction.WEST);
        if (desk != null && free(r.x1, r.cz())) {
            c.set(r.x1, F, r.cz(), desk);
            use(r.x1, r.cz());
            BlockState it = ModDeco.studyItem(Hash.h(r.x1, F, r.cz(), 2));
            if (it != null) c.set(r.x1, F + 1, r.cz(), it);
            chair(r.x1 - 1, r.cz(), Direction.EAST, Blocks.DARK_OAK_STAIRS);
        }
        if (r.w() >= 8 && r.d() >= 8) sofas(r.x0 + 1, r.z1 - 1, Direction.NORTH, 2, false);
        alongWalls(0.12, Pal.BOOKSHELF, Pal.b(Blocks.BARREL), Pal.b(Blocks.POTTED_WITHER_ROSE));
        chandelier(r.cx(), r.cz(), false);
    }

    private void bath() {
        floor((x, y, z) -> ((x + z) & 1) == 0 ? Pal.DS_POLISHED : Pal.SMOOTH_BASALT);
        boolean poolOk = r.w() >= 7 && r.d() >= 7;
        for (int x = r.x0 + 2; poolOk && x <= r.x1 - 2; x++)
            for (int z = r.z0 + 2; z <= r.z1 - 2; z++) if (room.clear(x, z)) poolOk = false;
        if (poolOk) {
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
            // altar al norte, o al sur si en el norte hay una puerta
            boolean north = altarFree(cx, r.z0 + 1, true) && free(cx, r.z0 + 3);
            boolean south = !north && altarFree(cx, r.z1 - 1, true) && free(cx, r.z1 - 3);
            int az = north ? r.z0 + 1 : r.z1 - 1, dz = north ? 1 : -1;
            Direction toAltar = north ? Direction.NORTH : Direction.SOUTH;
            if (north || south) {
                for (int x = cx - 2; x <= cx + 2; x++) {
                    c.set(x, F, az, Pal.PB_CHISELED);
                    c.set(x, F + 1, az, Pal.candles(x == cx ? Blocks.RED_CANDLE : Blocks.WHITE_CANDLE, 2 + (x & 1)));
                    use(x, az);
                }
                c.set(cx, F, az + 2 * dz, Pal.facing(Blocks.LECTERN, toAltar.getOpposite()));
                use(cx, az + 2 * dz);
            }
            for (int k = 5; k <= r.d() - 3; k += 2) {
                int z = (north || !south) ? r.z0 + k : r.z1 - k;
                for (int x = r.x0 + 1; x <= r.x1 - 1; x++) {
                    if (Math.abs(x - cx) <= 1) {
                        for (int zz = z; zz != z + 2 * dz; zz += dz) if (r.contains(x, zz) && c.isAir(x, F, zz)) c.set(x, F, zz, Pal.b(Blocks.RED_CARPET));
                        continue;
                    }
                    chair(x, z, toAltar, Blocks.DARK_OAK_STAIRS);
                }
            }
        } else {
            boolean west = altarFree(r.x0 + 1, cz, false);
            boolean east = !west && altarFree(r.x1 - 1, cz, false);
            int ax = west ? r.x0 + 1 : r.x1 - 1;
            if (west || east) {
                for (int z = cz - 2; z <= cz + 2; z++) {
                    c.set(ax, F, z, Pal.PB_CHISELED);
                    c.set(ax, F + 1, z, Pal.candles(Blocks.WHITE_CANDLE, 3));
                    use(ax, z);
                }
            }
            Direction toAltar = (west || !east) ? Direction.WEST : Direction.EAST;
            for (int k = 4; k <= r.w() - 3; k += 2) {
                int x = toAltar == Direction.WEST ? r.x0 + k : r.x1 - k;
                for (int z = r.z0 + 1; z <= r.z1 - 1; z++) {
                    if (Math.abs(z - cz) <= 1) continue;
                    chair(x, z, toAltar, Blocks.DARK_OAK_STAIRS);
                }
            }
        }
        chandeliers(big());
        torches(4);
    }

    /** ¿Caben las 5 casillas del altar (en fila) sin tapar ninguna puerta? */
    private boolean altarFree(int x, int z, boolean alongX) {
        for (int k = -2; k <= 2; k++) if (!free(alongX ? x + k : x, alongX ? z : z + k)) return false;
        return true;
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
        onTable(cx - 1, cz, true);
        onTable(cx, cz, true);
        onTable(cx + 1, cz, true);
        sofas(r.x0 + 1, r.z1, Direction.NORTH, 2, true);
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
        if (r.w() >= 10 && r.d() >= 6) sofas(r.cx() - 1, r.cz() + (r.d() >= r.w() ? 0 : 1), Direction.NORTH, 3, false);
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
        BlockState safe = ModDeco.safe(Direction.SOUTH);
        if (safe != null) standing(r.x0 + 1, r.z0, safe);
        chandelier(r.cx(), r.cz(), false);
    }

    private void prison() {
        floor((x, y, z) -> Pal.foundation(x, y, z));
        boolean alongX = r.w() >= r.d();
        if (alongX && r.d() >= 7) {
            int barZ = r.z0 + 3;
            for (int x = r.x0; x <= r.x1; x++) {
                boolean door = false;
                for (int z = r.z0; z <= barZ + 1; z++) if (room.clear(x, z)) door = true;
                if (door) continue;
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

    // ================================================================== SALA DE ESTAR
    /**
     * Sala de estar: tarima con alfombras, un gran hogar abierto por los cuatro lados en el centro con
     * su campana de piedra hasta el techo, sofas alrededor, estanterias bajas por las paredes con
     * cuadros encima y rincones de lectura y de te a los lados.
     */
    private void living() {
        boolean alongZ = r.d() >= r.w();
        int L = alongZ ? r.d() : r.w(), S = alongZ ? r.w() : r.d();
        // tarima de roble oscuro con franjas de abeto y cenefa
        floor((x, y, z) -> {
            boolean border = x == r.x0 || x == r.x1 || z == r.z0 || z == r.z1;
            if (border) return Pal.log(Blocks.STRIPPED_SPRUCE_LOG, (x == r.x0 || x == r.x1) ? Direction.Axis.Z : Direction.Axis.X);
            int band = alongZ ? Math.floorMod(z - r.z0, 7) : Math.floorMod(x - r.x0, 7);
            return band == 0 ? Pal.SP_PLANKS : Pal.DO_PLANKS;
        });
        int hx = r.cx(), hz = r.cz();
        boolean hearth = S >= 13 && L >= 13 && room.height >= 9;
        // 1) alfombra grande bajo los sofas y hogar con campana
        if (hearth) {
            rug(hx - 6, hz - 6, hx + 6, hz + 6, Blocks.RED_CARPET, Blocks.BLACK_CARPET);
            centralHearth(hx, hz);
            // sofas mirando al fuego, con una mesita y su lampara en cada punta
            sofas(hx + 1, hz - 5, Direction.SOUTH, 3, false);
            sofas(hx - 1, hz + 5, Direction.NORTH, 3, false);
            sofas(hx - 5, hz - 1, Direction.EAST, 3, false);
            sofas(hx + 5, hz + 1, Direction.WEST, 3, false);
            int v = 0;
            for (int s2 = -2; s2 <= 2; s2 += 4) {
                lampTable(hx + s2, hz - 5, Direction.SOUTH, v++);
                lampTable(hx + s2, hz + 5, Direction.NORTH, v++);
                lampTable(hx - 5, hz + s2, Direction.EAST, v++);
                lampTable(hx + 5, hz + s2, Direction.WEST, v++);
            }
        } else {
            rug(r.x0 + 1, r.z0 + 1, r.x1 - 1, r.z1 - 1, Blocks.RED_CARPET, Blocks.BLACK_CARPET);
            fireplace(alongZ ? r.cx() : r.x0, alongZ ? r.z0 : r.cz(), alongZ ? Direction.SOUTH : Direction.EAST);
            if (alongZ) sofas(hx + 1, r.z0 + 4, Direction.NORTH, 3, false);
            else sofas(r.x0 + 4, hz - 1, Direction.WEST, 3, false);
        }
        chestAt(r.x1, r.z1, Direction.NORTH, "dormitorio");
        // 2) estanterias bajas por las paredes, con velas y libros encima
        int shelfH = Math.max(1, Math.min(3, room.height - 4));
        int k = 0;
        for (WallCell w : wallCells()) {
            k++;
            if (!free(w.x, w.z) || room.window(w.x, w.z)) continue;
            for (int y = F; y < F + shelfH; y++) {
                boolean chis = Hash.pct(w.x, y, w.z, 19) < 15;
                c.set(w.x, y, w.z, chis ? Pal.facing(Blocks.CHISELED_BOOKSHELF, w.in) : Pal.BOOKSHELF);
            }
            int top = F + shelfH;
            int roll = Hash.pct(w.x, top, w.z, 23);
            if (roll < 14) c.set(w.x, top, w.z, Pal.candles(roll < 7 ? Blocks.RED_CANDLE : Blocks.WHITE_CANDLE, 1 + roll % 3));
            else if (roll < 30) {
                BlockState it = ModDeco.studyItem(Hash.h(w.x, top, w.z, 24));
                if (it != null && !isLight(it)) c.set(w.x, top, w.z, it);
            } else if (roll < 36) c.set(w.x, top, w.z, plant(k));
            use(w.x, w.z);
        }
        // 3) cuadros encima de las estanterias
        paintings(F + shelfH + 1);
        // 4) rincones a los lados del hogar: lectura en un extremo y mesa de te en el otro
        if (hearth) {
            int gap = (L - 13) / 2; // casillas libres a cada lado de la zona del hogar
            if (gap >= 7) {
                int off = 6 + (gap + 1) / 2 + 1;
                for (int side = -1; side <= 1; side += 2) {
                    int u = (alongZ ? hz : hx) + side * off;
                    for (int q = -1; q <= 1; q += 2) {
                        int vq = (alongZ ? hx : hz) + q * Math.max(3, S / 4);
                        int gx = alongZ ? vq : u, gz = alongZ ? u : vq;
                        corner(gx, gz, side < 0, q * 7 + side);
                        chandelier(gx, gz, false);
                    }
                    chandelier(alongZ ? hx : u, alongZ ? u : hz, true);
                }
            }
        }
    }

    /** Hogar abierto por los cuatro lados: pilares en las esquinas, fuego en medio y campana hasta el techo. */
    private void centralHearth(int hx, int hz) {
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++) {
                int x = hx + dx, z = hz + dz;
                boolean corner = Math.abs(dx) == 2 && Math.abs(dz) == 2;
                boolean ring = Math.max(Math.abs(dx), Math.abs(dz)) == 2;
                c.set(x, F - 1, z, Pal.PB_BRICKS);
                if (corner) {
                    c.column(x, z, F, F + 3, Pal.PB_BRICKS);
                    c.set(x, F, z, Pal.PB_CHISELED);
                    c.set(x, F + 3, z, Pal.PB_CHISELED);
                } else if (ring) {
                    c.set(x, F, z, Pal.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, false));
                    c.column(x, z, F + 1, F + 3, Pal.AIR);
                } else {
                    c.set(x, F - 1, z, Pal.NETHER_BRICKS);
                    Direction face = Direction.from2DDataValue(Math.floorMod(dx * 3 + dz, 4));
                    c.set(x, F, z, Pal.CAMPFIRE.setValue(CampfireBlock.FACING, face));
                    c.column(x, z, F + 1, F + 3, Pal.AIR);
                }
                use(x, z);
            }
        // la campana: un borde de piedra que se estrecha hasta la chimenea
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++) {
                int x = hx + dx, z = hz + dz;
                int ad = Math.max(Math.abs(dx), Math.abs(dz));
                boolean corner = Math.abs(dx) == 2 && Math.abs(dz) == 2;
                if (ad == 2) {
                    c.set(x, F + 4, z, Pal.PB_BRICKS);
                    if (corner) c.set(x, F + 5, z, Pal.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, false));
                    else c.set(x, F + 5, z, Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Pal.dirOf(-dx, -dz), false));
                } else if (ad == 1) {
                    c.set(x, F + 4, z, Pal.AIR);
                    c.set(x, F + 5, z, Pal.PB_BRICKS);
                }
            }
        // chimenea hasta el techo, con una franja tallada cada 3 bloques
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                final int ddx = dx, ddz = dz;
                c.column(hx + dx, hz + dz, F + 6, T, (x, y, z) -> (y - F) % 3 == 0 ? Pal.PB_CHISELED
                        : ((ddx == 0 || ddz == 0) ? Pal.PB_BRICKS : Pal.BASALT));
            }
        // faroles colgando del borde de la campana y escudos con calaveras en la chimenea
        Direction[] dirs = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (Direction d : dirs) {
            c.set(hx + d.getStepX() * 2, F + 3, hz + d.getStepZ() * 2, Pal.LANTERN_HANG);
            int fx = hx + d.getStepX() * 2, fz = hz + d.getStepZ() * 2;
            if (T >= F + 9) {
                c.set(fx, F + 7, fz, Blocks.WITHER_SKELETON_WALL_SKULL.defaultBlockState().setValue(WallSkullBlock.FACING, d));
                c.set(fx, F + 9, fz, (d.getAxis() == Direction.Axis.X ? Blocks.RED_WALL_BANNER : Blocks.BLACK_WALL_BANNER)
                        .defaultBlockState().setValue(WallBannerBlock.FACING, d));
            }
        }
    }

    /** Rincon de la sala: alfombrita, mesa con lampara o libros y dos butacas mirandose. */
    private void corner(int x, int z, boolean reading, int v) {
        if (!free(x, z) || !free(x - 1, z) || !free(x + 1, z)) return;
        rug(x - 2, z - 2, x + 2, z + 2, reading ? Blocks.PURPLE_CARPET : Blocks.RED_CARPET, Blocks.BLACK_CARPET);
        table(x, z, x, z, Blocks.DARK_OAK_SLAB);
        BlockState top = reading ? ModDeco.studyItem(Math.floorMod(v, 6) == 5 ? 0 : v) : ModDeco.tableware(v);
        if (top == null || isLight(top)) {
            BlockState l = ModDeco.lamp(v);
            top = l != null ? l : Pal.candles(reading ? Blocks.WHITE_CANDLE : Blocks.RED_CANDLE, 3);
        }
        c.set(x, F + 1, z, top);
        BlockState a = ModDeco.sofa(Direction.EAST, false, 0), b = ModDeco.sofa(Direction.WEST, false, 0);
        c.set(x - 1, F, z, a != null ? a : Pal.stairs(Blocks.CRIMSON_STAIRS, Direction.WEST, false));
        c.set(x + 1, F, z, b != null ? b : Pal.stairs(Blocks.CRIMSON_STAIRS, Direction.EAST, false));
        use(x - 1, z);
        use(x + 1, z);
        if (!reading) {
            chair(x, z - 1, Direction.SOUTH, Blocks.DARK_OAK_STAIRS);
            chair(x, z + 1, Direction.NORTH, Blocks.DARK_OAK_STAIRS);
        } else {
            lampTable(x, z - 2, Direction.SOUTH, v + 1);
        }
    }

    /** Cuadros colgados en las paredes libres (solo donde la pared de detras es maciza y cabe entero). */
    private void paintings(int yBottom) {
        if (yBottom + 1 > T - 1) return;
        var reg = net.minecraft.core.registries.BuiltInRegistries.PAINTING_VARIANT;
        String[][] bySize = {
                {"skeleton", "donkey_kong"},                            // 4x3
                {"burning_skull", "pigscene", "pointer"},               // 4x4
                {"skull_and_roses", "wither", "void", "bust", "match"}, // 2x2
                {"wanderer", "graham"}                                  // 1x2
        };
        int[][] sizes = {{4, 3}, {4, 4}, {2, 2}, {1, 2}};
        Direction[] sides = {Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST};
        int step = 0;
        for (Direction in : sides) {
            List<WallCell> cells = new ArrayList<>();
            for (WallCell w : wallCells()) if (w.in == in) cells.add(w);
            int lastPlaced = -100;
            for (int idx = 0; idx < cells.size(); idx++) {
                WallCell w = cells.get(idx);
                if (idx - lastPlaced < 7 || !c.inXZ(w.x, w.z)) continue;
                for (int si = 0; si < sizes.length; si++) {
                    int pw = sizes[si][0], ph = sizes[si][1];
                    if (yBottom + ph > T) continue;
                    int y0 = yBottom + (ph <= 2 ? 1 : 0);
                    if (!paintingFits(w.x, y0, w.z, w.in, pw, ph)) continue;
                    String[] names = bySize[si];
                    String name = names[Math.floorMod(Hash.h(w.x, y0, w.z, 31) + step, names.length)];
                    var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.PAINTING_VARIANT,
                            new ResourceLocation("minecraft", name));
                    var holder = reg.getHolder(key);
                    if (holder.isEmpty()) continue;
                    // la posicion del cuadro es su casilla "base"; el resto queda a su izquierda y hacia arriba
                    int i1 = (pw - 1) / -2, j1 = (ph - 1) / -2;
                    Direction ccw = w.in.getCounterClockWise();
                    int bx = w.x - ccw.getStepX() * i1, by = y0 - j1, bz = w.z - ccw.getStepZ() * i1;
                    c.addEntity(new net.minecraft.world.entity.decoration.Painting(c.level.getLevel(),
                            new net.minecraft.core.BlockPos(bx, by, bz), w.in, holder.get()));
                    lastPlaced = idx;
                    step++;
                    break;
                }
            }
        }
    }

    /** ¿Cabe un cuadro de pw x ph con su esquina inferior "izquierda" en (x, y, z), mirando hacia "in"? */
    private boolean paintingFits(int x, int y, int z, Direction in, int pw, int ph) {
        Direction ccw = in.getCounterClockWise();
        for (int a = 0; a < pw; a++)
            for (int b = 0; b < ph; b++) {
                int fx = x + ccw.getStepX() * a, fz = z + ccw.getStepZ() * a, fy = y + b;
                if (!r.contains(fx, fz) || !c.isAir(fx, fy, fz) || room.window(fx, fz)) return false;
                int wx = fx - in.getStepX(), wz = fz - in.getStepZ();
                if (!c.in(wx, fy, wz)) return false;
                BlockState wall = c.get(wx, fy, wz);
                if (wall.isAir() || !wall.isSolidRender(c.level, new net.minecraft.core.BlockPos(wx, fy, wz))) return false;
            }
        // que el cuadro quede del todo en la pared de esta sala (no en una esquina)
        int ex = x + ccw.getStepX() * (pw - 1), ez = z + ccw.getStepZ() * (pw - 1);
        return r.contains(ex, ez);
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
                    if (!free(x, z)) continue;
                    if (div) c.set(x, F, z, Pal.connected(Blocks.DARK_OAK_FENCE, Direction.Axis.Z));
                    else if (z == r.z0) c.set(x, F, z, Pal.log(Blocks.HAY_BLOCK, Direction.Axis.X));
                    use(x, z);
                }
                if (!div && (x - r.x0) % 4 == 2 && !room.clear(x, r.z0 + 1))
                    c.set(x, F, r.z0 + 1, Pal.b(Blocks.WATER_CAULDRON).setValue(LayeredCauldronBlock.LEVEL, 3));
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
