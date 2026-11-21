package com.ciudadela.gen;

import com.ciudadela.CiudadelaMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Calibracion de muebles de otros mods. Al arrancar el servidor coloca cada mueble como lo haria un
 * jugador (en una zona escondida en lo alto del mundo), lee como ha quedado (orientacion, conexiones
 * con sus vecinos, cabecero/pies de la cama...) y lo guarda. La generacion del castillo copia esos
 * estados, asi los muebles salen exactamente como si los hubiera puesto una persona.
 */
public final class Calib {
    private Calib() {}

    private static final Map<String, BlockState> M = new ConcurrentHashMap<>();
    public static volatile boolean done = false;
    private static final StringBuilder LOG = new StringBuilder();

    private static final Direction[] H = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public static BlockState get(String key) {
        return M.get(key);
    }

    public static String report() {
        return LOG.toString();
    }

    // ------------------------------------------------------------------ claves

    public static String seat(String id, Direction look, int part) {
        return "seat|" + id + "|" + look.getName() + "|" + part;
    }

    public static String bed(String id, Direction head, boolean isHead) {
        return "bed|" + id + "|" + head.getName() + "|" + isHead;
    }

    public static String front(String id, Direction front) {
        return "front|" + id + "|" + front.getName();
    }

    public static String wall(String id, Direction wall) {
        return "wall|" + id + "|" + wall.getName();
    }

    public static String row(String id, Direction.Axis axis, int part) {
        return "row|" + id + "|" + axis.getName() + "|" + part;
    }

    public static String column(String id, int n, int i) {
        return "col|" + id + "|" + n + "|" + i;
    }

    public static String door(String id, Direction facing, boolean upper) {
        return "door|" + id + "|" + facing.getName() + "|" + upper;
    }

    // ------------------------------------------------------------------ simulacion

    private static ServerLevel lvl;
    private static FakePlayer player;
    private static BlockPos origin;

    private static Item item(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null || !ForgeRegistries.ITEMS.containsKey(rl)) return null;
        Item it = ForgeRegistries.ITEMS.getValue(rl);
        return (it == null || it == Items.AIR) ? null : it;
    }

    private static String idOf(BlockState s) {
        ResourceLocation k = ForgeRegistries.BLOCKS.getKey(s.getBlock());
        return k == null ? "" : k.toString();
    }

    private static void clear() {
        BlockPos.betweenClosed(origin.offset(-6, -1, -6), origin.offset(6, 6, 6)).forEach(p -> lvl.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16));
        BlockPos.betweenClosed(origin.offset(-6, -1, -6), origin.offset(6, -1, 6)).forEach(p -> lvl.setBlock(p, Blocks.STONE.defaultBlockState(), 2 | 16));
    }

    /** Usa el objeto sobre la cara "face" del bloque "support", mirando hacia "look". */
    private static boolean use(Item it, BlockPos support, Direction face, Direction look) {
        try {
            BlockPos target = support.relative(face);
            player.setPos(target.getX() + 0.5 - look.getStepX() * 2.0, target.getY(), target.getZ() + 0.5 - look.getStepZ() * 2.0);
            player.setYRot(look.toYRot());
            player.setYHeadRot(look.toYRot());
            player.setXRot(0);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(it, 16));
            Vec3 hit = Vec3.atCenterOf(support).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
            player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(hit, face, support, false)));
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean family(BlockState s, String id) {
        if (s == null || s.isAir()) return false;
        String got = idOf(s);
        String base = id.replace("_wall", "");
        return got.equals(id) || got.replace("_wall", "").equals(base);
    }

    // ------------------------------------------------------------------ pruebas

    /** Asiento cuyo frente mira hacia look: el jugador se pone delante mirando hacia el asiento. */
    private static void testSeat(String id) {
        Item it = item(id);
        if (it == null) return;
        for (Direction look : H) {
            clear();
            BlockPos t = origin;
            use(it, t.below(), Direction.UP, look.getOpposite());
            BlockState s = lvl.getBlockState(t);
            if (family(s, id)) M.put(seat(id, look, 0), s);
        }
        log(id, seat(id, Direction.NORTH, 0));
    }

    /** Fila de 3 asientos (sofas, bancos) para sacar los extremos y el centro conectados. */
    private static void testSeatRow(String id) {
        Item it = item(id);
        if (it == null) return;
        for (Direction look : H) {
            clear();
            Direction side = look.getClockWise();
            BlockPos[] ps = {origin.relative(side, -1), origin, origin.relative(side, 1)};
            for (BlockPos p : ps) use(it, p.below(), Direction.UP, look.getOpposite());
            boolean ok = true;
            for (int i = 0; i < 3; i++) if (!family(lvl.getBlockState(ps[i]), id)) ok = false;
            if (ok) for (int i = 0; i < 3; i++) M.put(seat(id, look, i + 1), lvl.getBlockState(ps[i]));
            clear();
            use(it, origin.below(), Direction.UP, look.getOpposite());
            if (family(lvl.getBlockState(origin), id)) M.put(seat(id, look, 0), lvl.getBlockState(origin));
        }
        log(id, seat(id, Direction.NORTH, 0));
    }

    private static void testBed(String id) {
        Item it = item(id);
        if (it == null) return;
        for (Direction head : H) {
            clear();
            use(it, origin.below(), Direction.UP, head);
            BlockState foot = lvl.getBlockState(origin), hd = lvl.getBlockState(origin.relative(head));
            if (family(foot, id) && family(hd, id)) {
                M.put(bed(id, head, false), foot);
                M.put(bed(id, head, true), hd);
            } else {
                // por si el mod pone la cabecera en el bloque pulsado
                BlockState back = lvl.getBlockState(origin.relative(head.getOpposite()));
                if (family(foot, id) && family(back, id)) {
                    M.put(bed(id, head, true), foot);
                    M.put(bed(id, head, false), back);
                }
            }
        }
        log(id, bed(id, Direction.NORTH, true));
    }

    /** Mueble cuyo frente mira hacia front (el jugador lo pone mirandolo de frente). */
    private static void testFront(String id) {
        Item it = item(id);
        if (it == null) return;
        for (Direction f : H) {
            clear();
            use(it, origin.below(), Direction.UP, f.getOpposite());
            BlockState s = lvl.getBlockState(origin);
            if (family(s, id)) M.put(front(id, f), s);
        }
        log(id, front(id, Direction.NORTH));
    }

    /** Objeto de pared: se pulsa la cara de un bloque de piedra que hace de pared. */
    private static boolean exact(BlockState s, String id) {
        return s != null && idOf(s).equals(id);
    }

    private static void testWall(String id, String itemId) {
        Item it = item(itemId);
        if (it == null) return;
        for (Direction wall : H) {
            clear();
            BlockPos w = origin.relative(wall);
            lvl.setBlock(w, Blocks.STONE.defaultBlockState(), 2 | 16);
            use(it, w, wall.getOpposite(), wall);
            BlockState s = lvl.getBlockState(origin);
            if (exact(s, id)) M.put(wall(id, wall), s);
        }
        log(id, wall(id, Direction.NORTH));
    }

    private static void testRow(String id) {
        Item it = item(id);
        if (it == null) return;
        for (Direction.Axis ax : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            clear();
            Direction d = ax == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
            BlockPos[] ps = {origin.relative(d, -1), origin, origin.relative(d, 1)};
            for (BlockPos p : ps) use(it, p.below(), Direction.UP, Direction.NORTH);
            boolean ok = true;
            for (BlockPos p : ps) if (!family(lvl.getBlockState(p), id)) ok = false;
            if (ok) for (int i = 0; i < 3; i++) M.put(row(id, ax, i + 1), lvl.getBlockState(ps[i]));
        }
        clear();
        use(it, origin.below(), Direction.UP, Direction.NORTH);
        if (family(lvl.getBlockState(origin), id)) M.put(row(id, Direction.Axis.X, 0), lvl.getBlockState(origin));
        log(id, row(id, Direction.Axis.X, 2));
    }

    /** Columnas verticales (vidrieras de varias piezas). */
    private static void testColumn(String id, int maxN) {
        Item it = item(id);
        if (it == null) return;
        for (int n = 1; n <= maxN; n++) {
            clear();
            for (int i = 0; i < n; i++) use(it, origin.above(i - 1), Direction.UP, Direction.NORTH);
            boolean ok = true;
            for (int i = 0; i < n; i++) if (!family(lvl.getBlockState(origin.above(i)), id)) ok = false;
            if (ok) for (int i = 0; i < n; i++) M.put(column(id, n, i), lvl.getBlockState(origin.above(i)));
        }
        log(id, column(id, 3, 1));
    }

    private static void testDoor(String id) {
        Item it = item(id);
        if (it == null) return;
        for (Direction f : H) {
            clear();
            use(it, origin.below(), Direction.UP, f);
            BlockState lo = lvl.getBlockState(origin), up = lvl.getBlockState(origin.above());
            if (family(lo, id) && family(up, id)) {
                M.put(door(id, f, false), lo);
                M.put(door(id, f, true), up);
            }
        }
        log(id, door(id, Direction.NORTH, false));
    }

    private static void log(String id, String key) {
        BlockState s = M.get(key);
        LOG.append(id).append(" -> ").append(s == null ? "NO" : s.toString()).append('\n');
    }

    // ------------------------------------------------------------------ catalogo a calibrar

    public static final String[] SEATS = {"handcrafted:dark_oak_chair", "mcwfurnitures:dark_oak_chair", "another_furniture:dark_oak_chair"};
    public static final String[] STOOLS = {"mcwfurnitures:dark_oak_stool_chair"};
    public static final String[] SOFAS = {"handcrafted:dark_oak_couch", "another_furniture:red_sofa", "another_furniture:black_sofa", "handcrafted:dark_oak_dining_bench"};
    public static final String[] BEDS = {"handcrafted:dark_oak_fancy_bed"};
    public static final String[] FRONTS = {"handcrafted:dark_oak_nightstand", "handcrafted:dark_oak_desk", "handcrafted:dark_oak_cupboard",
            "handcrafted:dark_oak_drawer", "handcrafted:dark_oak_counter", "handcrafted:oven", "mcwfurnitures:dark_oak_triple_drawer",
            "mcwfurnitures:dark_oak_drawer", "mcwfurnitures:dark_oak_double_drawer", "mcwfurnitures:dark_oak_kitchen_sink",
            "mcwfurnitures:dark_oak_counter", "another_furniture:dark_oak_drawer", "supplementaries:safe", "supplementaries:globe", "supplementaries:cannon",
            "mcwfurnitures:dark_oak_cupboard_counter", "mcwfurnitures:dark_oak_drawer_counter", "mcwfurnitures:dark_oak_double_drawer_counter",
            "mcwfurnitures:dark_oak_kitchen_cabinet", "mcwfurnitures:dark_oak_glass_kitchen_cabinet", "mcwfurnitures:dark_oak_double_kitchen_cabinet",
            "handcrafted:kitchen_hood", "handcrafted:kitchen_hood_pipe"};
    public static final String[][] WALLS = {
            {"supplementaries:sconce_wall_soul", "supplementaries:sconce_soul"},
            {"supplementaries:sconce_wall", "supplementaries:sconce"},
            {"mcwlights:iron_wall_candle_holder", "mcwlights:iron_wall_candle_holder"},
            {"mcwlights:tavern_wall_lantern", "mcwlights:tavern_lantern"},
            {"mcwlights:wall_lantern", "mcwlights:wall_lantern"},
            {"handcrafted:wither_skeleton_trophy", "handcrafted:wither_skeleton_trophy"},
            {"handcrafted:spider_trophy", "handcrafted:spider_trophy"},
            {"handcrafted:skeleton_horse_trophy", "handcrafted:skeleton_horse_trophy"},
            {"handcrafted:wolf_trophy", "handcrafted:wolf_trophy"},
            {"supplementaries:item_shelf", "supplementaries:item_shelf"},
            {"another_furniture:dark_oak_shelf", "another_furniture:dark_oak_shelf"},
            {"supplementaries:faucet", "supplementaries:faucet"},
    };
    public static final String[] ROWS = {"handcrafted:dark_oak_table", "mcwfurnitures:dark_oak_table", "mcwfences:gothic_metal_fence"};
    public static final String[] COLUMNS = {"mcwwindows:red_mosaic_glass", "mcwwindows:purple_mosaic_glass", "mcwwindows:black_mosaic_glass",
            "mcwwindows:blue_mosaic_glass", "mcwwindows:orange_mosaic_glass", "mcwwindows:gray_mosaic_glass", "mcwfences:gothic_metal_fence"};
    public static final String[] DOORS = {"mcwdoors:dark_oak_classic_door", "mcwdoors:dark_oak_mystic_door", "mcwdoors:dark_oak_nether_door",
            "mcwdoors:dark_oak_cottage_door", "mcwdoors:dark_oak_whispering_door"};

    public static synchronized void run(ServerLevel level) {
        if (done) return;
        long t0 = System.currentTimeMillis();
        try {
            lvl = level;
            BlockPos sp = level.getSharedSpawnPos();
            origin = new BlockPos(sp.getX(), level.getMaxBuildHeight() - 10, sp.getZ());
            level.getChunkAt(origin);
            player = FakePlayerFactory.getMinecraft(level);
            for (String id : SEATS) testSeat(id);
            for (String id : STOOLS) testSeat(id);
            for (String id : SOFAS) testSeatRow(id);
            for (String id : BEDS) testBed(id);
            for (String id : FRONTS) testFront(id);
            for (String[] w : WALLS) testWall(w[0], w[1]);
            for (String id : ROWS) testRow(id);
            for (String id : COLUMNS) testColumn(id, 8);
            for (String id : DOORS) testDoor(id);
            clear();
            BlockPos.betweenClosed(origin.offset(-6, -1, -6), origin.offset(6, -1, 6)).forEach(p -> lvl.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16));
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        } catch (Throwable t) {
            CiudadelaMod.LOGGER.error("Calibracion de muebles fallida", t);
        } finally {
            done = true;
            lvl = null;
            player = null;
            CiudadelaMod.LOGGER.info("Calibracion de muebles: {} estados en {} ms", M.size(), System.currentTimeMillis() - t0);
        }
    }

    /** Evita avisos de compilador por clases no usadas. */
    static boolean isBlockItem(Item i) {
        return i instanceof BlockItem;
    }
}
