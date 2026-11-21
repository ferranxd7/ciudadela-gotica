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
        if (Calib.done) return Calib.report();
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

    private static BlockState cal(String key) {
        return Calib.done ? Calib.get(key) : null;
    }

    private static BlockState color(BlockState s, String col) {
        return s == null ? null : Mods.prop(s, "color", col);
    }

    public static BlockState chair(Direction look, int variant) {
        for (int k = 0; k < Calib.SEATS.length; k++) {
            String id = Calib.SEATS[Math.floorMod(variant + k, Calib.SEATS.length)];
            BlockState s = cal(Calib.seat(id, look, 0));
            if (s != null) return color(s, "red");
        }
        return null;
    }

    /** Sofa: part 0 = suelto, 1..3 = izquierda/centro/derecha de una fila. */
    public static BlockState sofa(Direction look, boolean black, int part) {
        String[] ids = black ? new String[]{"another_furniture:black_sofa", "handcrafted:dark_oak_couch"}
                : new String[]{"handcrafted:dark_oak_couch", "another_furniture:red_sofa"};
        for (String id : ids) {
            BlockState s = cal(Calib.seat(id, look, part));
            if (s == null && part != 0) s = cal(Calib.seat(id, look, 0));
            if (s != null) return color(s, black ? "black" : "red");
        }
        return null;
    }

    public static BlockState sofa(Direction look, boolean black) {
        return sofa(look, black, 0);
    }

    public static BlockState bench(Direction look) {
        return cal(Calib.seat("handcrafted:dark_oak_dining_bench", look, 0));
    }

    /** facing = hacia donde queda el cabecero. */
    public static BlockState bed(Direction facing, boolean head, String col) {
        return color(cal(Calib.bed("handcrafted:dark_oak_fancy_bed", facing, head)), col);
    }

    private static BlockState frontOf(Direction front, String... ids) {
        for (String id : ids) {
            BlockState s = cal(Calib.front(id, front));
            if (s != null) return s;
        }
        return null;
    }

    public static BlockState nightstand(Direction front) {
        return color(frontOf(front, "handcrafted:dark_oak_nightstand", "mcwfurnitures:dark_oak_drawer", "another_furniture:dark_oak_drawer"), "red");
    }

    public static BlockState desk(Direction front) {
        return color(frontOf(front, "handcrafted:dark_oak_desk", "mcwfurnitures:dark_oak_double_drawer"), "red");
    }

    public static BlockState cupboard(Direction front) {
        return frontOf(front, "handcrafted:dark_oak_cupboard", "mcwfurnitures:dark_oak_double_drawer");
    }

    public static BlockState drawer(Direction front, int v) {
        return v % 2 == 0 ? frontOf(front, "mcwfurnitures:dark_oak_triple_drawer", "handcrafted:dark_oak_drawer")
                : frontOf(front, "handcrafted:dark_oak_drawer", "another_furniture:dark_oak_drawer", "mcwfurnitures:dark_oak_drawer");
    }

    public static BlockState sideTable() {
        return null;
    }

    public static BlockState tableSingle() {
        BlockState s = cal(Calib.row("handcrafted:dark_oak_table", Direction.Axis.X, 0));
        if (s != null) return color(s, "red");
        return cal(Calib.row("mcwfurnitures:dark_oak_table", Direction.Axis.X, 0));
    }

    /** Mesa larga: cada casilla copia la pieza (extremo/centro) calibrada. */
    public static BlockState tableLong(Direction.Axis axis, boolean startEnd, boolean endEnd) {
        int part = startEnd ? 1 : (endEnd ? 3 : 2);
        BlockState s = cal(Calib.row("handcrafted:dark_oak_table", axis, part));
        if (s != null) return color(s, "red");
        return cal(Calib.row("mcwfurnitures:dark_oak_table", axis, part));
    }

    public static BlockState counter(Direction front, int v) {
        return switch (v % 4) {
            case 0 -> Mods.prop(frontOf(front, "handcrafted:dark_oak_counter", "mcwfurnitures:dark_oak_counter"), "counter", "blackstone");
            case 1 -> Mods.prop(frontOf(front, "mcwfurnitures:dark_oak_kitchen_sink", "mcwfurnitures:dark_oak_counter"), "water", "true");
            case 2 -> Mods.prop(frontOf(front, "handcrafted:oven", "mcwfurnitures:dark_oak_counter"), "lit", "true");
            default -> frontOf(front, "mcwfurnitures:dark_oak_counter", "handcrafted:dark_oak_counter");
        };
    }

    public static BlockState kitchenHood(Direction wall) {
        return null;
    }

    private static BlockState wallOf(Direction wall, String... ids) {
        for (String id : ids) {
            BlockState s = cal(Calib.wall(id, wall));
            if (s != null) return s;
        }
        return null;
    }

    public static BlockState wallLight(Direction wall, int v) {
        BlockState s = switch (Math.floorMod(v, 3)) {
            case 0 -> wallOf(wall, "supplementaries:sconce_wall_soul", "mcwlights:iron_wall_candle_holder", "supplementaries:sconce_wall");
            case 1 -> wallOf(wall, "mcwlights:iron_wall_candle_holder", "supplementaries:sconce_wall", "mcwlights:wall_lantern");
            default -> wallOf(wall, "mcwlights:tavern_wall_lantern", "mcwlights:wall_lantern", "supplementaries:sconce_wall_soul");
        };
        return Mods.prop(s, "lit", "true");
    }

    public static BlockState trophy(Direction wall, int v) {
        String[] t = {"handcrafted:wither_skeleton_trophy", "handcrafted:spider_trophy", "handcrafted:skeleton_horse_trophy", "handcrafted:wolf_trophy"};
        return wallOf(wall, t[Math.floorMod(v, t.length)]);
    }

    public static BlockState wallShelf(Direction wall) {
        return wallOf(wall, "supplementaries:item_shelf", "another_furniture:dark_oak_shelf");
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
        return frontOf(front, "supplementaries:safe");
    }

    public static BlockState cannon(Direction out) {
        BlockState s = frontOf(out, "supplementaries:cannon");
        if (s == null) return null;
        // si la calibracion lo deja apuntando al cielo, lo orientamos a mano hacia fuera
        return Mods.prop(s, "facing", out.getName());
    }

    public static BlockState awning(Direction f, boolean red) {
        return null;
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
        int total = yTop - yBottom + 1, n, i;
        if (total <= 8) {
            n = total;
            i = y - yBottom;
        } else {
            n = 8;
            i = y == yBottom ? 0 : (y == yTop ? 7 : 3);
        }
        return cal(Calib.column("mcwwindows:" + col + "_mosaic_glass", n, i));
    }

    /** Puerta de Macaw's (o null). */
    public static BlockState door(Direction facing, boolean upper, int v) {
        return cal(Calib.door(Calib.DOORS[Math.floorMod(v, Calib.DOORS.length)], facing, upper));
    }

    public static BlockState gothicFence(Direction.Axis axis, boolean top) {
        BlockState s = cal(Calib.column("mcwfences:gothic_metal_fence", 2, top ? 1 : 0));
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
