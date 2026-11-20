package com.ciudadela.gen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Decoracion con bloques de mods (Handcrafted, Macaw's, Supplementaries, Another Furniture).
 * Cada funcion devuelve null si ningun mod adecuado esta instalado; quien llama pone entonces la
 * version vanilla. La orientacion se calcula midiendo la forma del bloque, asi no dependemos de
 * como haya decidido cada mod su propiedad "facing".
 */
public final class ModDeco {
    private ModDeco() {}

    private static final ConcurrentHashMap<String, Direction> REF = new ConcurrentHashMap<>();
    private static final Direction NONE = Direction.UP;

    // ------------------------------------------------------------------ medicion de formas

    private static List<AABB> boxes(BlockState s) {
        try {
            VoxelShape v = s.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            return v.toAabbs();
        } catch (Throwable t) {
            return List.of();
        }
    }

    /** Lado donde esta el respaldo (la parte alta) con facing=north. */
    private static Direction backFor(BlockState northState) {
        double sx = 0, sz = 0, w = 0;
        for (AABB b : boxes(northState)) {
            if (b.maxY < 0.8) continue;
            double vol = (b.maxX - b.minX) * (b.maxZ - b.minZ) * (b.maxY - Math.max(b.minY, 0.6));
            if (vol <= 0) continue;
            sx += (b.minX + b.maxX) / 2 * vol;
            sz += (b.minZ + b.maxZ) / 2 * vol;
            w += vol;
        }
        if (w <= 0) return NONE;
        double dx = sx / w - 0.5, dz = sz / w - 0.5;
        if (Math.abs(dx) < 0.06 && Math.abs(dz) < 0.06) return NONE;
        return Pal.dirOf((int) Math.round(dx * 100), (int) Math.round(dz * 100));
    }

    /** Lado que toca la pared con facing=north (para apliques, estantes, trofeos...). */
    private static Direction attachFor(BlockState northState) {
        double minX = 1, minZ = 1, maxX = 0, maxZ = 0;
        List<AABB> l = boxes(northState);
        if (l.isEmpty()) return NONE;
        for (AABB b : l) {
            minX = Math.min(minX, b.minX);
            minZ = Math.min(minZ, b.minZ);
            maxX = Math.max(maxX, b.maxX);
            maxZ = Math.max(maxZ, b.maxZ);
        }
        double[] gap = {minZ, 1 - maxZ, minX, 1 - maxX}; // N S W E
        Direction[] dirs = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        Direction[] opp = {Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST};
        int best = -1;
        for (int i = 0; i < 4; i++) {
            double o = gap[i ^ 1];
            if (gap[i] < 0.02 && o > 0.1 && (best < 0 || gap[i] < gap[best])) best = i;
        }
        return best < 0 ? NONE : dirs[best];
    }

    /** Gira el estado para que el lado medido (con facing=north) quede mirando hacia "want". */
    private static BlockState turn(BlockState s, Direction refForNorth, Direction want) {
        if (s == null || want == null) return s;
        if (refForNorth == NONE) return Mods.prop(s, "facing", want.getName());
        int k = Math.floorMod(want.get2DDataValue() - refForNorth.get2DDataValue(), 4);
        Direction f = Direction.from2DDataValue(Math.floorMod(Direction.NORTH.get2DDataValue() + k, 4));
        return Mods.prop(s, "facing", f.getName());
    }

    private static Direction ref(String id, BlockState north, boolean back, Direction fallback) {
        return REF.computeIfAbsent(id + (back ? "#b" : "#a"), k -> {
            Direction d = back ? backFor(north) : attachFor(north);
            return d == NONE ? fallback : d;
        });
    }

    private static BlockState first(String... ids) {
        for (String id : ids) {
            BlockState s = Mods.state(id);
            if (s != null) return s;
        }
        return null;
    }

    private static String idOf(BlockState s) {
        var k = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(s.getBlock());
        return k == null ? "?" : k.toString();
    }

    /** Coloca un asiento cuyo frente mire hacia "look" (respaldo al lado contrario). */
    private static BlockState seat(BlockState s, Direction look) {
        if (s == null) return null;
        BlockState north = Mods.prop(s, "facing", "north");
        Direction back = ref(idOf(s), north, true, Direction.SOUTH);
        return turn(s, back, look.getOpposite());
    }

    /** Coloca algo pegado a la pared que esta en la direccion "wall". */
    private static BlockState onWall(BlockState s, Direction wall) {
        if (s == null) return null;
        BlockState north = Mods.prop(s, "facing", "north");
        Direction att = ref(idOf(s), north, false, Direction.SOUTH);
        return turn(s, att, wall);
    }

    /** Muebles tipo cofre: el frente hacia "front". */
    private static BlockState front(BlockState s, Direction front) {
        return s == null ? null : Mods.prop(s, "facing", front.getName());
    }

    public static String report() {
        StringBuilder sb = new StringBuilder();
        String[] seats = {"handcrafted:dark_oak_chair", "handcrafted:dark_oak_couch", "another_furniture:red_sofa", "mcwfurnitures:dark_oak_chair",
                "another_furniture:dark_oak_chair", "handcrafted:dark_oak_bench", "handcrafted:dark_oak_dining_bench"};
        String[] walls = {"supplementaries:sconce_wall_soul", "mcwlights:iron_wall_candle_holder", "mcwlights:tavern_wall_lantern",
                "handcrafted:wither_skeleton_trophy", "handcrafted:dark_oak_shelf", "another_furniture:dark_oak_shelf", "supplementaries:item_shelf",
                "mcwlights:wall_lantern", "handcrafted:kitchen_hood", "supplementaries:flag_red"};
        for (String id : seats) {
            BlockState s = Mods.state(id, "facing=north");
            sb.append(id).append(" respaldo(north)=").append(s == null ? "-" : backFor(s)).append('\n');
        }
        for (String id : walls) {
            BlockState s = Mods.state(id, "facing=north");
            sb.append(id).append(" pared(north)=").append(s == null ? "-" : attachFor(s)).append('\n');
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ catalogo

    public static BlockState chair(Direction look, int variant) {
        BlockState s = switch (variant % 3) {
            case 0 -> Mods.state("handcrafted:dark_oak_chair", "color=red");
            case 1 -> Mods.state("mcwfurnitures:dark_oak_chair");
            default -> Mods.state("another_furniture:dark_oak_chair");
        };
        if (s == null) s = first("handcrafted:dark_oak_chair", "mcwfurnitures:dark_oak_chair", "another_furniture:dark_oak_chair");
        return seat(s, look);
    }

    public static BlockState sofa(Direction look, boolean black) {
        BlockState s = Mods.state("handcrafted:dark_oak_couch", "color=" + (black ? "black" : "red"), "shape=single");
        if (s == null) s = Mods.state("another_furniture:" + (black ? "black" : "red") + "_sofa", "type=single");
        return seat(s, look);
    }

    public static BlockState bench(Direction look) {
        BlockState s = Mods.state("handcrafted:dark_oak_dining_bench", "shape=single");
        return seat(s, look);
    }

    public static BlockState bed(Direction facing, boolean head, String color) {
        BlockState s = Mods.state("handcrafted:dark_oak_fancy_bed", "color=" + color, "part=" + (head ? "head" : "foot"), "shape=single");
        return s == null ? null : Mods.prop(s, "facing", facing.getName());
    }

    public static BlockState nightstand(Direction front) {
        return front(first("handcrafted:dark_oak_nightstand", "mcwfurnitures:dark_oak_drawer", "another_furniture:dark_oak_drawer"), front);
    }

    public static BlockState desk(Direction front) {
        return front(first("handcrafted:dark_oak_desk"), front);
    }

    public static BlockState cupboard(Direction front) {
        return front(first("handcrafted:dark_oak_cupboard", "mcwfurnitures:dark_oak_double_drawer"), front);
    }

    public static BlockState drawer(Direction front, int v) {
        BlockState s = v % 2 == 0 ? Mods.state("mcwfurnitures:dark_oak_triple_drawer") : Mods.state("handcrafted:dark_oak_drawer", "type=" + (1 + v % 4));
        if (s == null) s = first("mcwfurnitures:dark_oak_drawer", "another_furniture:dark_oak_drawer");
        return front(s, front);
    }

    public static BlockState sideTable() {
        return first("handcrafted:dark_oak_side_table", "mcwfurnitures:dark_oak_end_table");
    }

    public static BlockState tableSingle() {
        BlockState s = Mods.state("handcrafted:dark_oak_table", "color=red", "shape=single");
        return s != null ? s : first("mcwfurnitures:dark_oak_table", "another_furniture:dark_oak_table");
    }

    /** Mesa larga de Macaw's: conectada con sus vecinas en el eje dado. */
    public static BlockState tableLong(Direction.Axis axis, boolean startEnd, boolean endEnd) {
        BlockState s = Mods.state("mcwfurnitures:dark_oak_table");
        if (s == null) return null;
        if (axis == Direction.Axis.X) {
            s = Mods.prop(s, "west", String.valueOf(!startEnd));
            s = Mods.prop(s, "east", String.valueOf(!endEnd));
        } else {
            s = Mods.prop(s, "north", String.valueOf(!startEnd));
            s = Mods.prop(s, "south", String.valueOf(!endEnd));
        }
        return s;
    }

    public static BlockState counter(Direction front, int v) {
        BlockState s = switch (v % 4) {
            case 0 -> Mods.state("handcrafted:dark_oak_counter", "counter=blackstone");
            case 1 -> Mods.state("mcwfurnitures:dark_oak_kitchen_sink", "water=true");
            case 2 -> Mods.state("handcrafted:oven", "lit=true");
            default -> Mods.state("mcwfurnitures:dark_oak_counter");
        };
        if (s == null) s = first("mcwfurnitures:dark_oak_counter", "handcrafted:dark_oak_counter");
        return front(s, front);
    }

    public static BlockState kitchenHood(Direction wall) {
        return onWall(Mods.state("handcrafted:kitchen_hood"), wall);
    }

    public static BlockState wallLight(Direction wall, int v) {
        BlockState s = switch (v % 3) {
            case 0 -> Mods.state("supplementaries:sconce_wall_soul", "lit=true");
            case 1 -> Mods.state("mcwlights:iron_wall_candle_holder", "lit=true");
            default -> Mods.state("mcwlights:tavern_wall_lantern");
        };
        if (s == null) s = first("supplementaries:sconce_wall", "mcwlights:wall_lantern");
        if (s != null) s = Mods.prop(s, "lit", "true");
        return onWall(s, wall);
    }

    public static BlockState trophy(Direction wall, int v) {
        String[] t = {"handcrafted:wither_skeleton_trophy", "handcrafted:spider_trophy", "handcrafted:skeleton_horse_trophy", "handcrafted:wolf_trophy"};
        return onWall(Mods.state(t[Math.floorMod(v, t.length)]), wall);
    }

    public static BlockState wallShelf(Direction wall) {
        return onWall(first("supplementaries:item_shelf", "another_furniture:dark_oak_shelf"), wall);
    }

    public static BlockState chandelier(boolean gold, boolean small) {
        String id = "mcwlights:" + (gold ? "golden" : "iron") + (small ? "_small" : "") + "_chandelier";
        return Mods.prop(Mods.state(id), "lit", "true");
    }

    public static BlockState hangingLantern(int v) {
        String[] ids = {"mcwlights:tavern_lantern", "mcwlights:chain_lantern", "mcwlights:cross_lantern", "mcwlights:bell_lantern"};
        BlockState s = Mods.state(ids[Math.floorMod(v, ids.length)]);
        return Mods.prop(s, "hanging", "true");
    }

    public static BlockState standingLantern(int v) {
        String[] ids = {"mcwlights:tavern_lantern", "mcwlights:cross_lantern", "mcwlights:covered_lantern"};
        BlockState s = Mods.state(ids[Math.floorMod(v, ids.length)]);
        return Mods.prop(s, "hanging", "false");
    }

    public static BlockState candleHolder(Direction f, int v) {
        BlockState s = v % 2 == 0 ? Mods.state("mcwlights:iron_triple_candle_holder", "lit=true")
                : Mods.state("supplementaries:candle_holder_black", "candles=3", "face=floor", "lit=true");
        if (s == null) s = Mods.state("supplementaries:candle_holder_red", "candles=4", "face=floor", "lit=true");
        return s == null ? null : Mods.prop(s, "facing", f.getName());
    }

    public static BlockState firePit() {
        return Mods.state("supplementaries:fire_pit", "lit=true");
    }

    /** Cosas pequenas encima de una mesa. */
    public static BlockState tableware(int v) {
        int k = Math.floorMod(v, 9);
        return switch (k) {
            case 0 -> Mods.state("handcrafted:wood_plate", "pieces=" + (1 + v % 3));
            case 1 -> Mods.state("handcrafted:white_cup", "pieces=" + (1 + v % 2));
            case 2 -> Mods.state("handcrafted:wood_bowl", "pieces=1");
            case 3 -> Mods.state("supplementaries:goblet");
            case 4 -> Mods.state("handcrafted:terracotta_crockery_combo");
            case 5 -> Mods.state("supplementaries:candle_holder_red", "candles=3", "face=floor", "lit=true");
            case 6 -> Mods.state("handcrafted:white_plate", "pieces=" + (1 + v % 4));
            case 7 -> Mods.state("supplementaries:jar");
            default -> Mods.state("handcrafted:berry_jam_jar", "jars=" + (1 + v % 3));
        };
    }

    /** Cosas de estudio/biblioteca encima de una mesa. */
    public static BlockState studyItem(int v) {
        int k = Math.floorMod(v, 6);
        return switch (k) {
            case 0 -> Mods.state("supplementaries:book_pile", "books=" + (1 + v % 4));
            case 1 -> Mods.state("handcrafted:stackable_book", "books=" + (1 + v % 4));
            case 2 -> Mods.state("supplementaries:globe");
            case 3 -> Mods.state("supplementaries:hourglass", "facing=up");
            case 4 -> Mods.state("supplementaries:book_pile_horizontal", "books=" + (1 + v % 4));
            default -> Mods.state("supplementaries:candle_holder_black", "candles=2", "face=floor", "lit=true");
        };
    }

    /** Bultos para almacenes, sotanos y criptas. */
    public static BlockState clutter(int v, boolean crypt) {
        if (crypt) {
            int k = Math.floorMod(v, 4);
            return switch (k) {
                case 0 -> Mods.state("supplementaries:urn", "treasure=" + (v % 7 == 0));
                case 1 -> Mods.state("supplementaries:candle_holder_black", "candles=4", "face=floor", "lit=true");
                case 2 -> Mods.state("handcrafted:terracotta_thick_pot");
                default -> Mods.state("supplementaries:urn");
            };
        }
        int k = Math.floorMod(v, 8);
        return switch (k) {
            case 0, 1 -> Mods.state("supplementaries:sack");
            case 2 -> Mods.state("supplementaries:jar");
            case 3 -> Mods.state("supplementaries:urn");
            case 4 -> Mods.state("handcrafted:terracotta_wide_pot");
            case 5 -> Mods.state("supplementaries:cannonball", "balls=" + (1 + v % 4));
            case 6 -> Mods.state("supplementaries:fodder", "layers=" + (2 + v % 6));
            default -> Mods.state("handcrafted:terracotta_medium_pot");
        };
    }

    public static BlockState safe(Direction front) {
        return front(Mods.state("supplementaries:safe"), front);
    }

    public static BlockState cannon(Direction out) {
        return Mods.prop(Mods.state("supplementaries:cannon"), "facing", out.getName());
    }

    public static BlockState awning(Direction f, boolean red) {
        return Mods.prop(Mods.state("supplementaries:awning_" + (red ? "red" : "black")), "facing", f.getName());
    }

    public static BlockState flag(Direction f, boolean red) {
        return Mods.prop(Mods.state("supplementaries:flag_" + (red ? "red" : "black")), "facing", f.getName());
    }

    public static BlockState stool(int v) {
        String[] c = {"red", "black", "purple", "brown"};
        return Mods.state("another_furniture:" + c[Math.floorMod(v, c.length)] + "_stool");
    }

    public static BlockState lamp(int v) {
        String[] c = {"red", "black", "purple"};
        return Mods.state("another_furniture:" + c[Math.floorMod(v, c.length)] + "_lamp", "lit=true", "base=true", "facing=up");
    }

    public static BlockState pot(int v) {
        String[] p = {"handcrafted:terracotta_wide_pot", "handcrafted:terracotta_thick_pot", "handcrafted:white_glazed_medium_pot",
                "supplementaries:planter", "handcrafted:golden_thin_pot"};
        return Mods.state(p[Math.floorMod(v, p.length)]);
    }

    /** Vidriera (mosaico de Macaw's) para una columna de ventana con partes abajo/medio/arriba. */
    public static BlockState mosaic(int x, int z, int y, int yBottom, int yTop) {
        String[] colors = {"red", "purple", "black", "blue", "red", "orange", "gray"};
        String col = colors[Hash.h(x / 3, 0, z / 3, 404) % colors.length];
        String part = yBottom == yTop ? "base" : (y == yBottom ? "bottom" : (y == yTop ? "top" : "middle"));
        return Mods.state("mcwwindows:" + col + "_mosaic_glass", "part=" + part);
    }

    /** Puerta de Macaw's (o null). */
    public static BlockState door(Direction facing, boolean upper, int v) {
        String[] ids = {"mcwdoors:dark_oak_classic_door", "mcwdoors:dark_oak_mystic_door", "mcwdoors:dark_oak_nether_door",
                "mcwdoors:spruce_cottage_door", "mcwdoors:dark_oak_whispering_door"};
        BlockState s = Mods.state(ids[Math.floorMod(v, ids.length)]);
        if (s == null) return null;
        s = Mods.prop(s, "facing", facing.getName());
        return Mods.prop(s, "half", upper ? "upper" : "lower");
    }

    public static BlockState gothicFence(Direction.Axis axis, boolean top) {
        BlockState s = Mods.state("mcwfences:gothic_metal_fence", "fencepart=" + (top ? "top" : "bottom"));
        if (s == null) return null;
        if (axis == Direction.Axis.X) return Mods.prop(Mods.prop(s, "east", "true"), "west", "true");
        return Mods.prop(Mods.prop(s, "north", "true"), "south", "true");
    }

    public static BlockState paving(int x, int y, int z) {
        int p = Hash.pct(x, y, z, 901);
        String id = p < 35 ? "mcwpaths:blackstone_square_paving" : p < 60 ? "mcwpaths:deepslate_flagstone"
                : p < 80 ? "mcwpaths:cobbled_deepslate_basket_weave_paving" : "mcwpaths:blackstone_flagstone";
        return Mods.state(id);
    }

    public static Block anyBlock(String id) {
        Block b = Mods.block(id);
        return b != null ? b : Blocks.AIR;
    }
}
