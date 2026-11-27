package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Pal;
import com.darkcraft.personajes.Herrero;
import com.darkcraft.pueblo.gen.Trozo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Las herrerias de Morvain (parte 7af, respuesta 95 del plan: "herrero en un refugio y VARIOS herreros por el mapa con
 * animacion currada golpeando el yunque"). Cuatro, cada una junto a un santuario y escrita al generar el chunk (las ve
 * Distant Horizons):
 * <ul>
 * <li>el <b>Refugio de la Fragua</b> ({@link #REFUGIO}), 20 al norte de la Encrucijada de los Peregrinos: una casa de piedra
 * con entramado de roble oscuro y tejado de pizarra (un trozo hundido al noroeste, con musgo debajo), la chimenea de ladrillo
 * con su fuego (una hoguera de senales: el humo sale por arriba), alto horno y horno encendidos, el yunque de <b>Hagen</b>
 * delante, la tina de templar, la piedra de afilar, el banco de herrero, las hojas colgadas en la pared, los barriles (uno
 * con botin), la mesa con su farol, el petate y, fuera, la lena, el abrevadero, el heno y dos farolas junto a la puerta;</li>
 * <li>tres <b>campamentos</b> con su tienda de lona, su fragua al aire, su yunque y lo de su sitio: el de <b>Brenna</b>
 * ({@link #FARO}) a la entrada del Puerto Varado, al oeste del Faro de los Huesos (lona de vela, una barca volcada, cadenas
 * y huesos), el de <b>Ulrik</b> ({@link #COLMILLO}) al oeste del Paso del Colmillo (lona con nieve encima, un banco de
 * troncos, pieles y lena) y el de <b>Tobin</b> ({@link #CAPITAL}) al norte de la Puerta de la Capital (lona roja de un
 * estandarte, cascotes, un carro de barriles y el estandarte de la ciudad).</li>
 * </ul>
 * Cada una se dibuja en su marco: (u, v) con la entrada hacia +v (el sur) y girada a donde se abre ({@link Sitio#frente});
 * lo que se pisa esta en y + 1. El suelo se allana a su altura y se funde con el de alrededor en {@link #FUNDE} bloques
 * (solo si hay que cambiarlo: en el llano del puerto no se toca nada fuera). {@code Adorno} no pone arboles en sus chunks.
 * Los herreros se ponen una vez, la primera vez que alguien pasa cerca ({@link #tick}, {@link Morvain.Datos}).
 */
public final class Herrerias {
    private Herrerias() {}

    public static final long SEMILLA = 0x4E7E_77A5_1A9FL;
    /** Lo que tarda el suelo allanado en fundirse con el de alrededor. */
    static final int FUNDE = 7;
    /** A esta distancia de una herreria se pone su herrero (si su chunk esta cargado). */
    static final int CERCA = 56;
    public static final ResourceLocation BOTIN = new ResourceLocation(DarkCraftMod.MODID, "chests/refugio");

    /**
     * Una herreria: su herrero, el santuario de al lado, el centro, su suelo (lo que se pisa esta en y + 1), hacia donde se abre,
     * su caja en el marco (u, v) y donde estan el herrero y su yunque (en el marco).
     */
    public record Sitio(Herrero.Quien quien, String santuario, int x, int z, int y, Direction frente, int u0, int v0, int u1, int v1,
                        int hu, int hv) {
        Rotation giro() {
            return switch (frente) {
                case EAST -> Rotation.COUNTERCLOCKWISE_90;
                case NORTH -> Rotation.CLOCKWISE_180;
                case WEST -> Rotation.CLOCKWISE_90;
                default -> Rotation.NONE;
            };
        }

        public int wx(int u, int v) {
            return x + switch (frente) {
                case EAST -> v;
                case NORTH -> -u;
                case WEST -> -v;
                default -> u;
            };
        }

        public int wz(int u, int v) {
            return z + switch (frente) {
                case EAST -> -u;
                case NORTH -> -v;
                case WEST -> u;
                default -> v;
            };
        }

        public BlockPos pos(int u, int dy, int v) {
            return new BlockPos(wx(u, v), y + dy, wz(u, v));
        }

        /** Donde tiene los pies el herrero y donde esta su yunque (un bloque por delante, hacia la entrada). */
        public BlockPos herrero() {
            return pos(hu, 1, hv);
        }

        public BlockPos yunque() {
            return pos(hu, 1, hv + 1);
        }

        /** La caja del mundo (x0, z0, x1, z1) de lo construido (sin lo que se funde). */
        public int[] caja() {
            int ax = wx(u0, v0), az = wz(u0, v0), bx = wx(u1, v1), bz = wz(u1, v1);
            return new int[]{Math.min(ax, bx), Math.min(az, bz), Math.max(ax, bx), Math.max(az, bz)};
        }

        /** Lo lejos que esta (x, z) de la caja (0 dentro). */
        public double fuera(double px, double pz) {
            int[] k = caja();
            double dx = Math.max(Math.max(k[0] - px, px - k[2]), 0), dz = Math.max(Math.max(k[1] - pz, pz - k[3]), 0);
            return Math.hypot(dx, dz);
        }

        public String id() {
            return quien.id;
        }
    }

    /** El Refugio de la Fragua (Hagen), al norte de la Encrucijada de los Peregrinos, con la puerta al sur. */
    public static final Sitio REFUGIO = new Sitio(Herrero.Quien.HAGEN, "encrucijada", -1132, 5076, 71, Direction.SOUTH, -8, -7, 8, 9, 1, -2);
    /** El campamento de Brenna, en el llano del Puerto Varado, al oeste del Faro de los Huesos (abierto al este). */
    public static final Sitio FARO = new Sitio(Herrero.Quien.BRENNA, "faro_huesos", 5594, 4782, Varadero.SUELO, Direction.EAST, -6, -5, 6, 5, 3, -2);
    /** El campamento de Ulrik, al oeste del Paso del Colmillo (abierto al este). */
    public static final Sitio COLMILLO = new Sitio(Herrero.Quien.ULRIK, "colmillo", -3149, -3590, 247, Direction.EAST, -6, -5, 6, 5, 3, -2);
    /** El campamento de Tobin, al norte de la Puerta de la Capital (abierto al sur). */
    public static final Sitio CAPITAL = new Sitio(Herrero.Quien.TOBIN, "puerta_capital", 3170, 326, Capital.SUELO, Direction.SOUTH, -6, -5, 6, 5, 3, -2);

    public static final Sitio[] TODOS = {REFUGIO, FARO, COLMILLO, CAPITAL};

    public static Sitio de(Herrero.Quien q) {
        for (Sitio s : TODOS) if (s.quien == q) return s;
        return null;
    }

    public static Sitio porId(String id) {
        for (Sitio s : TODOS) if (s.id().equals(id) || s.santuario.equals(id)) return s;
        return null;
    }

    // ------------------------------------------------------------------ escribir los chunks

    private static final Map<Long, List<Element>> INDICE = new HashMap<>();
    private static final List<Element> ELEMENTOS = new ArrayList<>();
    /** Lo que hay (para la prueba y las vistas): bloques de cada una. */
    public static final Map<String, Integer> BLOQUES = new HashMap<>();

    static {
        for (Sitio s : TODOS) {
            ELEMENTOS.add(new Explanada(s));
            Plano p = new Plano(s);
            if (s == REFUGIO) refugio(p);
            else campamento(p);
            ELEMENTOS.add(p.m.pieza(3));
            BLOQUES.put(s.id(), p.m.b.size());
            if (s == REFUGIO) {
                BlockPos q = s.pos(-5, 1, -4);
                ELEMENTOS.add(new Trozo(6, q.getX(), q.getY(), q.getZ(), q.getX(), q.getY(), q.getZ(),
                        c -> c.barrel(q.getX(), q.getY(), q.getZ(), Direction.UP, BOTIN)));
            }
        }
        for (Element e : ELEMENTOS)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) INDICE.computeIfAbsent(Velmor.clave(x, z), k -> new ArrayList<>()).add(e);
        for (List<Element> l : INDICE.values()) l.sort(java.util.Comparator.comparingInt(e -> e.layer));
    }

    /** Si el chunk tiene algo de alguna herreria. */
    public static boolean toca(int chunkX, int chunkZ) {
        return INDICE.containsKey(Velmor.clave(chunkX, chunkZ));
    }

    /** Si el chunk que empieza en (x0, z0) toca una herreria o lo que se funde con ella: ahi {@code Adorno} solo pone hierba. */
    static boolean cerca(int x0, int z0) {
        for (Sitio s : TODOS) {
            int[] k = s.caja();
            if (x0 + 15 >= k[0] - FUNDE - 2 && x0 <= k[2] + FUNDE + 2 && z0 + 15 >= k[1] - FUNDE - 2 && z0 <= k[3] + FUNDE + 2) return true;
        }
        return false;
    }

    static void chunk(WorldGenLevel nivel, ChunkAccess c) {
        int cx = c.getPos().x, cz = c.getPos().z;
        construir(nivel, cx, cz, RandomSource.create(Velmor.clave(cx, cz) ^ SEMILLA));
    }

    private static final java.util.Set<String> AVISADOS = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public static void construir(WorldGenLevel nivel, int chunkX, int chunkZ, RandomSource r) {
        List<Element> l = INDICE.get(Velmor.clave(chunkX, chunkZ));
        if (l == null) return;
        Canvas c = new Canvas(nivel, chunkX, chunkZ, r);
        for (Element e : l) {
            try {
                e.build(c);
            } catch (Throwable t) {
                String k = e.getClass().getSimpleName();
                if (AVISADOS.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} de una herreria en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    public static java.util.Set<Long> chunks() {
        return INDICE.keySet();
    }

    // ------------------------------------------------------------------ el suelo

    /** Allana el suelo de una herreria a su altura y lo funde con el de alrededor (solo donde hay que cambiarlo). */
    private static final class Explanada extends Element {
        final Sitio s;

        Explanada(Sitio s) {
            super(SEMILLA + s.quien.ordinal());
            this.s = s;
            this.layer = 1;
            int[] k = s.caja();
            bounds(k[0] - FUNDE, s.y - 24, k[1] - FUNDE, k[2] + FUNDE, s.y + 30, k[3] + FUNDE);
        }

        @Override
        public void build(Canvas c) {
            for (int x = Math.max(c.minX, x0); x <= Math.min(c.maxX, x1); x++)
                for (int z = Math.max(c.minZ, z0); z <= Math.min(c.maxZ, z1); z++) columna(c, x, z);
        }

        private void columna(Canvas c, int x, int z) {
            double d = s.fuera(x + 0.5, z + 0.5);
            if (d >= FUNDE) return;
            int hay = superficie(c, x, z, s.y);
            int h = d <= 0.75 ? s.y : (int) Math.round(Ruido.lerp(Ruido.suave(0.75, FUNDE, d), s.y, hay));
            if (d > 0.75 && h == hay) return;
            Terreno.Columna col = Terreno.columna(x, z);
            Suelo.Capas capas = Suelo.capas(col, x, z, h, 0);
            int arriba = d <= 0.75 ? h + 14 : Math.max(hay, h) + 3;
            for (int y = h + 1; y <= arriba; y++) c.set(x, y, z, Pal.AIR);
            for (int y = Math.min(hay, h) - 3; y <= h; y++) c.set(x, y, z, capas.en(y));
        }
    }

    /** Lo alto de lo firme en (x, z) cerca de y (sin plantas, nieve, agua ni hojas); y si no hay nada. */
    static int superficie(Canvas c, int x, int z, int y) {
        for (int yy = y + 24; yy >= y - 24; yy--) {
            BlockState st = c.get(x, yy, z);
            if (st.isAir() || st.canBeReplaced() || !st.getFluidState().isEmpty() || st.is(net.minecraft.tags.BlockTags.LEAVES)) continue;
            return yy;
        }
        return y;
    }

    // ------------------------------------------------------------------ los planos

    /** Los bloques de una herreria, en su marco (u, dy, v) y girados a su sitio. */
    private static final class Plano {
        final Sitio s;
        final Rotation r;
        final Pilotes.Lista m = new Pilotes.Lista();
        final long sem;

        Plano(Sitio s) {
            this.s = s;
            this.r = s.giro();
            this.sem = SEMILLA + 97L * s.quien.ordinal();
        }

        void b(int u, int dy, int v, BlockState st) {
            m.put(s.wx(u, v), s.y + dy, s.wz(u, v), st.rotate(r));
        }

        /** Vallas, barrotes y muros: se unen a sus vecinos al acabar de generarse el chunk. */
        void une(int u, int dy, int v, BlockState st) {
            m.une(s.wx(u, v), s.y + dy, s.wz(u, v), st.rotate(r));
        }

        int pct(int u, int dy, int v, int k) {
            return Hash.pct(s.wx(u, v), s.y + dy, s.wz(u, v), sem + k);
        }

        void aire(int u, int dy, int v) {
            b(u, dy, v, Pal.AIR);
        }
    }

    static BlockState b(Block k) {
        return k.defaultBlockState();
    }

    static BlockState escalon(Block k, Direction f, Half h) {
        return k.defaultBlockState().setValue(StairBlock.FACING, f).setValue(StairBlock.HALF, h);
    }

    static BlockState losa(Block k, SlabType t) {
        return k.defaultBlockState().setValue(SlabBlock.TYPE, t);
    }

    static BlockState tronco(Block k, Direction.Axis a) {
        return k.defaultBlockState().setValue(RotatedPillarBlock.AXIS, a);
    }

    static BlockState farol(boolean colgado) {
        return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, colgado);
    }

    static BlockState hoguera() {
        return Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true).setValue(CampfireBlock.SIGNAL_FIRE, false);
    }

    static BlockState horno(Block k) {
        return k.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, Direction.SOUTH).setValue(AbstractFurnaceBlock.LIT, true);
    }

    static BlockState yunque() {
        return Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, Direction.EAST);
    }

    static BlockState tina() {
        return Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
    }

    static BlockState afilar() {
        return Blocks.GRINDSTONE.defaultBlockState().setValue(GrindstoneBlock.FACE, AttachFace.FLOOR).setValue(GrindstoneBlock.FACING, Direction.EAST);
    }

    static BlockState barril(Direction d) {
        return Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, d);
    }

    // ------------------------------------------------------------------ el Refugio de la Fragua

    /** La piedra de los muros del refugio (mezclada). */
    private static BlockState piedra(Plano p, int u, int dy, int v) {
        int k = p.pct(u, dy, v, 1);
        return k < 52 ? b(Blocks.COBBLESTONE) : k < 72 ? b(Blocks.MOSSY_COBBLESTONE) : k < 88 ? b(Blocks.STONE_BRICKS)
                : k < 96 ? b(Blocks.CRACKED_STONE_BRICKS) : b(Blocks.MOSSY_STONE_BRICKS);
    }

    private static void refugio(Plano p) {
        // ---- el suelo de dentro (con hollin junto a la fragua) y los cimientos
        for (int u = -6; u <= 6; u++)
            for (int v = -5; v <= 5; v++) {
                p.b(u, -1, v, b(Blocks.COBBLESTONE));
                boolean muro = Math.abs(u) == 6 || Math.abs(v) == 5;
                if (muro) {
                    p.b(u, 0, v, b(Blocks.COBBLESTONE));
                    continue;
                }
                int k = p.pct(u, 0, v, 2);
                BlockState s;
                if (v <= -3 && u >= -1 && u <= 4)
                    s = k < 45 ? b(Blocks.BRICKS) : k < 80 ? b(Blocks.POLISHED_BLACKSTONE_BRICKS) : b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
                else
                    s = k < 34 ? b(Blocks.STONE_BRICKS) : k < 62 ? b(Blocks.COBBLESTONE) : k < 78 ? b(Blocks.COARSE_DIRT)
                            : k < 90 ? b(Blocks.CRACKED_STONE_BRICKS) : b(Blocks.PACKED_MUD);
                p.b(u, 0, v, s);
            }
        // ---- los muros (esquinas de roble oscuro, viga de madera arriba)
        for (int u = -6; u <= 6; u++)
            for (int v = -5; v <= 5; v++) {
                if (Math.abs(u) != 6 && Math.abs(v) != 5) continue;
                boolean esquina = Math.abs(u) == 6 && Math.abs(v) == 5;
                for (int dy = 1; dy <= 4; dy++) {
                    if (esquina) p.b(u, dy, v, tronco(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
                    else if (dy == 4) p.b(u, dy, v, tronco(Blocks.STRIPPED_DARK_OAK_LOG, Math.abs(v) == 5 ? Direction.Axis.X : Direction.Axis.Z));
                    else p.b(u, dy, v, piedra(p, u, dy, v));
                }
            }
        // la puerta (al sur, tres de ancho) con su dintel
        for (int u = -1; u <= 1; u++)
            for (int dy = 1; dy <= 3; dy++) p.aire(u, dy, 5);
        // las ventanas: dos al oeste, una alta al este
        p.une(-6, 2, -2, b(Blocks.IRON_BARS));
        p.une(-6, 2, 2, b(Blocks.IRON_BARS));
        p.une(6, 2, -1, b(Blocks.IRON_BARS));
        p.une(6, 3, -1, b(Blocks.IRON_BARS));
        // lo alto del muro roto donde se hundio el tejado (noroeste)
        p.aire(-6, 4, -3);
        p.aire(-6, 4, -4);
        p.aire(-5, 4, -5);
        p.aire(-4, 4, -5);
        p.b(-6, 3, -4, b(Blocks.MOSSY_COBBLESTONE));
        // ---- los hastiales (este y oeste) de tablas de roble oscuro, con un ventanuco al este
        for (int dy = 5; dy <= 9; dy++)
            for (int v = -(9 - dy); v <= 9 - dy; v++) {
                if (Math.abs(v) > 4) continue;
                p.b(-6, dy, v, b(Blocks.DARK_OAK_PLANKS));
                p.b(6, dy, v, b(Blocks.DARK_OAK_PLANKS));
            }
        p.aire(6, 7, 0);
        // ---- las vigas de dentro
        for (int v = -4; v <= 4; v++) {
            p.b(-3, 4, v, tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Z));
            p.b(3, 4, v, tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Z));
        }
        p.aire(-3, 4, -3);
        // ---- el tejado de pizarra (dos aguas, cumbrera de este a oeste) con el trozo hundido
        for (int u = -7; u <= 7; u++) {
            for (int a = 1; a <= 6; a++) {
                int dy = 10 - a;
                Block k = p.pct(u, dy, a, 3) < 18 ? Blocks.COBBLED_DEEPSLATE_STAIRS : Blocks.DEEPSLATE_TILE_STAIRS;
                Block k2 = p.pct(u, dy, -a, 3) < 18 ? Blocks.COBBLED_DEEPSLATE_STAIRS : Blocks.DEEPSLATE_TILE_STAIRS;
                p.une(u, dy, a, escalon(k, Direction.NORTH, Half.BOTTOM));
                p.une(u, dy, -a, escalon(k2, Direction.SOUTH, Half.BOTTOM));
            }
            p.b(u, 10, 0, losa(Blocks.DEEPSLATE_TILE_SLAB, SlabType.BOTTOM));
            if (Math.abs(u) <= 6) p.b(u, 9, 0, tronco(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.X));
        }
        for (int u = -5; u <= -3; u++)
            for (int a = 2; a <= 4; a++) p.aire(u, 10 - a, -a);
        p.aire(-4, 9, 0);
        p.aire(-4, 10, 0);
        p.aire(-4, 9, -1);
        // lo que cayo: escombros, una teja y musgo donde entra la lluvia
        p.b(-4, 1, -3, b(Blocks.MOSS_CARPET));
        p.b(-3, 1, -2, b(Blocks.MOSS_CARPET));
        p.b(-5, 1, -2, b(Blocks.MOSS_CARPET));
        p.b(-4, 1, -2, escalon(Blocks.DEEPSLATE_TILE_STAIRS, Direction.EAST, Half.BOTTOM));
        p.b(-3, 1, -3, losa(Blocks.COBBLED_DEEPSLATE_SLAB, SlabType.BOTTOM));
        p.b(-5, 3, -4, b(Blocks.COBWEB));
        p.b(5, 4, -4, b(Blocks.COBWEB));
        p.b(-5, 4, 4, b(Blocks.COBWEB));
        // ---- la chimenea: fuego de senales en su hueco (el humo sale por arriba), alto horno y horno, campana de ladrillo
        p.b(2, 0, -5, b(Blocks.HAY_BLOCK));
        p.b(2, 1, -5, hoguera().setValue(CampfireBlock.SIGNAL_FIRE, true));
        for (int dy = 1; dy <= 12; dy++)
            for (int u = 1; u <= 3; u++)
                for (int v = -6; v <= -4; v++) {
                    if (u == 2 && v == -5) {
                        if (dy >= 2) p.aire(u, dy, v);
                        continue;
                    }
                    if (v == -4 && dy == 1) continue;
                    if (v == -4 && dy == 2) {
                        p.b(u, dy, v, escalon(Blocks.BRICK_STAIRS, Direction.NORTH, Half.TOP));
                        continue;
                    }
                    p.b(u, dy, v, b(Blocks.BRICKS));
                }
        for (int u = 1; u <= 3; u++)
            for (int v = -6; v <= -4; v++) if (u != 2 || v != -5) p.b(u, 13, v, losa(Blocks.BRICK_SLAB, SlabType.BOTTOM));
        p.b(1, 1, -4, horno(Blocks.BLAST_FURNACE));
        p.b(3, 1, -4, horno(Blocks.FURNACE));
        p.aire(2, 1, -4);
        // ---- el yunque de Hagen y lo de la fragua
        p.b(1, 1, -1, yunque());
        p.b(-1, 1, -3, tina());
        p.b(4, 1, -3, afilar());
        p.b(4, 1, 0, b(Blocks.SMITHING_TABLE));
        p.b(5, 2, 1, Blocks.LIGHTNING_ROD.defaultBlockState().setValue(LightningRodBlock.FACING, Direction.WEST));
        p.b(5, 2, 2, Blocks.LIGHTNING_ROD.defaultBlockState().setValue(LightningRodBlock.FACING, Direction.WEST));
        p.b(5, 3, 3, Blocks.LIGHTNING_ROD.defaultBlockState().setValue(LightningRodBlock.FACING, Direction.WEST));
        p.b(5, 1, 3, b(Blocks.CHIPPED_ANVIL).setValue(AnvilBlock.FACING, Direction.NORTH));
        // ---- los barriles (el de abajo de la esquina, con botin: lo pone su Trozo), el petate, la mesa y los faroles
        p.b(-5, 2, -4, barril(Direction.UP));
        p.b(-5, 1, -3, barril(Direction.EAST));
        p.b(-5, 1, 2, b(Blocks.BROWN_CARPET));
        p.b(-5, 1, 3, b(Blocks.BROWN_CARPET));
        p.b(-5, 1, 1, b(Blocks.WHITE_CARPET));
        p.b(-4, 1, 4, farol(false));
        p.une(-2, 1, 1, b(Blocks.DARK_OAK_FENCE));
        p.b(-2, 2, 1, farol(false));
        p.b(-3, 1, 1, escalon(Blocks.DARK_OAK_STAIRS, Direction.WEST, Half.BOTTOM));
        p.b(-1, 1, 1, escalon(Blocks.DARK_OAK_STAIRS, Direction.EAST, Half.BOTTOM));
        p.b(3, 3, 1, farol(true));
        p.b(-3, 3, 3, farol(true));
        // ---- fuera: la lena al este, el abrevadero y el heno al oeste, dos farolas y el camino a la puerta
        for (int v = -3; v <= 1; v++) {
            p.b(7, 1, v, tronco(p.pct(7, 1, v, 4) < 50 ? Blocks.OAK_LOG : Blocks.SPRUCE_LOG, Direction.Axis.Z));
            if (v >= -2 && v <= 0) p.b(7, 2, v, tronco(Blocks.OAK_LOG, Direction.Axis.Z));
        }
        p.b(7, 3, -1, losa(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
        p.b(-7, 1, 0, tina());
        p.b(-7, 1, 1, tina());
        p.b(-7, 1, 3, tronco(Blocks.HAY_BLOCK, Direction.Axis.Y));
        p.b(-7, 1, 4, tronco(Blocks.HAY_BLOCK, Direction.Axis.X));
        p.b(-7, 2, 3, tronco(Blocks.HAY_BLOCK, Direction.Axis.Y));
        for (int s = -1; s <= 1; s += 2) {
            p.une(2 * s, 1, 7, b(Blocks.DARK_OAK_FENCE));
            p.une(2 * s, 2, 7, b(Blocks.DARK_OAK_FENCE));
            p.b(2 * s, 3, 7, farol(false));
        }
        for (int v = 6; v <= 9; v++)
            for (int u = -1; u <= 1; u++) {
                int k = p.pct(u, 0, v, 5);
                p.b(u, 0, v, k < 55 ? b(Blocks.DIRT_PATH) : k < 80 ? b(Blocks.COARSE_DIRT) : b(Blocks.GRAVEL));
            }
    }

    // ------------------------------------------------------------------ los campamentos

    private static void campamento(Plano p) {
        Herrero.Quien q = p.s.quien;
        boolean costa = q == Herrero.Quien.BRENNA, nieve = q == Herrero.Quien.ULRIK;
        // ---- el suelo
        for (int u = -5; u <= 5; u++)
            for (int v = -4; v <= 4; v++) {
                int k = p.pct(u, 0, v, 2);
                BlockState s = costa ? (k < 50 ? b(Blocks.SAND) : k < 75 ? b(Blocks.COARSE_DIRT) : k < 90 ? b(Blocks.GRAVEL) : b(Blocks.SANDSTONE))
                        : nieve ? (k < 40 ? b(Blocks.SNOW_BLOCK) : k < 65 ? b(Blocks.GRAVEL) : k < 88 ? b(Blocks.STONE) : b(Blocks.PACKED_ICE))
                        : (k < 35 ? b(Blocks.COBBLESTONE) : k < 60 ? b(Blocks.COARSE_DIRT) : k < 82 ? b(Blocks.STONE_BRICKS) : b(Blocks.CRACKED_STONE_BRICKS));
                p.b(u, 0, v, s);
                p.b(u, -1, v, nieve ? b(Blocks.STONE) : costa ? b(Blocks.SAND) : b(Blocks.DIRT));
            }
        // ---- la tienda: cuatro palos y la lona en dos alturas (de una vela, nevada o de un estandarte)
        Block valla = nieve ? Blocks.SPRUCE_FENCE : costa ? Blocks.OAK_FENCE : Blocks.DARK_OAK_FENCE;
        for (int u : new int[]{-4, 0}) {
            for (int dy = 1; dy <= 2; dy++) p.une(u, dy, -3, b(valla));
            for (int dy = 1; dy <= 3; dy++) p.une(u, dy, 0, b(valla));
        }
        for (int u = -4; u <= 0; u++)
            for (int v = -3; v <= 0; v++) {
                int dy = v >= -1 ? 4 : 3;
                int k = p.pct(u, dy, v, 3);
                Block lona = costa ? (k < 70 ? Blocks.WHITE_WOOL : k < 90 ? Blocks.LIGHT_GRAY_WOOL : Blocks.BROWN_WOOL)
                        : nieve ? (k < 75 ? Blocks.WHITE_WOOL : Blocks.LIGHT_GRAY_WOOL)
                        : (k < 72 ? Blocks.RED_WOOL : k < 90 ? Blocks.LIGHT_GRAY_WOOL : Blocks.BLACK_WOOL);
                p.b(u, dy, v, b(lona));
                if (nieve) p.b(u, dy + 1, v, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 2));
            }
        // debajo: el petate, un barril y un farol
        p.b(-3, 1, -2, b(Blocks.BROWN_CARPET));
        p.b(-3, 1, -1, b(Blocks.BROWN_CARPET));
        p.b(-3, 1, -3, b(Blocks.WHITE_CARPET));
        p.b(-1, 1, -3, barril(Direction.UP));
        p.b(-2, 1, -3, farol(false));
        // ---- la fragua: alto horno con su chimenea, hoguera, un poyo con farol; el yunque delante del herrero
        Block muro = nieve ? Blocks.STONE_BRICKS : costa ? Blocks.SANDSTONE : Blocks.BRICKS;
        for (int u = 2; u <= 4; u++)
            for (int dy = 1; dy <= 2; dy++) p.b(u, dy, -4, b(muro));
        p.b(3, 3, -4, b(muro));
        p.b(3, 4, -4, b(muro));
        p.b(2, 1, -3, b(muro));
        p.b(2, 2, -3, farol(false));
        p.b(3, 1, -3, horno(Blocks.BLAST_FURNACE));
        p.b(4, 1, -3, hoguera());
        p.b(3, 1, -1, yunque());
        p.b(5, 1, -2, tina());
        p.b(1, 1, -2, afilar());
        // ---- lo de cada sitio
        if (costa) {
            // una barca volcada (la quilla arriba), cadenas colgando de la lona, huesos y redes
            for (int u = -4; u <= -1; u++) {
                p.b(u, 1, 3, escalon(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, Half.TOP));
                p.b(u, 1, 2, escalon(Blocks.DARK_OAK_STAIRS, Direction.NORTH, Half.TOP));
            }
            p.b(-2, 3, 0, b(Blocks.CHAIN));
            p.b(-4, 3, 1, b(Blocks.CHAIN));
            p.b(5, 1, 3, tronco(Blocks.BONE_BLOCK, Direction.Axis.Y));
            p.b(4, 1, 4, tronco(Blocks.BONE_BLOCK, Direction.Axis.X));
            p.b(1, 1, 3, b(Blocks.COBWEB));
            p.b(5, 1, 1, barril(Direction.WEST));
        } else if (nieve) {
            // un banco de troncos, pieles, lena apilada y nieve por los bordes
            p.b(-3, 1, 2, tronco(Blocks.SPRUCE_LOG, Direction.Axis.X));
            p.b(-2, 1, 2, tronco(Blocks.SPRUCE_LOG, Direction.Axis.X));
            p.b(-1, 1, 1, b(Blocks.BROWN_CARPET));
            p.b(0, 1, 2, b(Blocks.BROWN_CARPET));
            for (int v = 1; v <= 3; v++) {
                p.b(5, 1, v, tronco(Blocks.SPRUCE_LOG, Direction.Axis.Z));
                if (v <= 2) p.b(5, 2, v, tronco(Blocks.SPRUCE_LOG, Direction.Axis.Z));
            }
            for (int u = -5; u <= 5; u++)
                for (int v = -4; v <= 4; v++) {
                    boolean borde = Math.abs(u) == 5 || Math.abs(v) == 4;
                    if (borde && !p.m.hay(p.s.wx(u, v), p.s.y + 1, p.s.wz(u, v)) && p.pct(u, 1, v, 6) < 60)
                        p.b(u, 1, v, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1 + p.pct(u, 1, v, 7) % 2));
                }
        } else {
            // cascotes, un carro de barriles y el estandarte rojo de la ciudad
            p.b(-5, 1, 3, escalon(Blocks.STONE_BRICK_STAIRS, Direction.EAST, Half.BOTTOM));
            p.b(5, 1, 3, b(Blocks.CRACKED_STONE_BRICKS));
            p.b(5, 2, 3, losa(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
            p.b(4, 1, 4, losa(Blocks.COBBLESTONE_SLAB, SlabType.BOTTOM));
            p.b(-3, 1, 3, barril(Direction.UP));
            p.b(-2, 1, 3, barril(Direction.NORTH));
            p.b(-3, 2, 3, barril(Direction.UP));
            p.b(-1, 1, 3, Blocks.DARK_OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF, Half.TOP).setValue(TrapDoorBlock.FACING, Direction.WEST)
                    .setValue(TrapDoorBlock.OPEN, true));
            p.b(-5, 1, 1, Blocks.RED_BANNER.defaultBlockState());
        }
    }

    // ------------------------------------------------------------------ los herreros

    private static String clave(Sitio s) {
        return "herrero_" + s.id();
    }

    /** Si ya se puso el herrero de esa herreria. */
    public static boolean puesto(net.minecraft.server.MinecraftServer srv, Sitio s) {
        return Morvain.Datos.get(srv).hecho(clave(s));
    }

    /** Cada segundo: el herrero de cada herreria, una vez, cuando alguien pasa cerca y su chunk esta cargado. */
    static void tick(ServerLevel m) {
        Morvain.Datos d = Morvain.Datos.get(m.getServer());
        for (Sitio s : TODOS) {
            if (d.hecho(clave(s))) continue;
            BlockPos h = s.herrero();
            if (!m.isLoaded(h) || !m.isLoaded(s.yunque())) continue;
            for (ServerPlayer p : m.players())
                if (p.blockPosition().distSqr(h) < CERCA * CERCA) {
                    poner(m, s, d);
                    break;
                }
        }
    }

    private static boolean poner(ServerLevel m, Sitio s, Morvain.Datos d) {
        Herrero h = Herrero.poner(m, s.herrero(), s.yunque(), s.quien);
        if (h == null) return false;
        d.marcar(clave(s));
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: {} en su herreria junto al santuario {}: {}", s.id(), s.santuario, s.herrero().toShortString());
        return true;
    }

    /** Para el comando y la prueba: pone los herreros que falten (cargando sus chunks). Devuelve cuantos puso. */
    public static int ponerTodos(ServerLevel m) {
        Morvain.Datos d = Morvain.Datos.get(m.getServer());
        int n = 0;
        for (Sitio s : TODOS) {
            m.getChunk(s.herrero().getX() >> 4, s.herrero().getZ() >> 4);
            m.getChunk(s.yunque().getX() >> 4, s.yunque().getZ() >> 4);
            if (!d.hecho(clave(s)) || buscar(m, s) == null) if (poner(m, s, d)) n++;
        }
        return n;
    }

    /** El herrero de esa herreria (si su chunk esta cargado y esta cerca de su sitio). */
    public static Herrero buscar(ServerLevel m, Sitio s) {
        BlockPos h = s.herrero();
        if (!m.isLoaded(h)) return null;
        for (Herrero e : m.getEntitiesOfClass(Herrero.class, new net.minecraft.world.phys.AABB(h).inflate(4)))
            if (e.quien() == s.quien) return e;
        return null;
    }

    /** Para la prueba y el comando: quita los herreros (los que esten cargados) y olvida que se pusieron. */
    public static void olvidar(ServerLevel m) {
        Morvain.Datos d = Morvain.Datos.get(m.getServer());
        for (Sitio s : TODOS) {
            Herrero e = buscar(m, s);
            if (e != null) e.discard();
            d.quitar(clave(s));
        }
    }

    // ------------------------------------------------------------------ el comando

    /** /darkcraft herreros [ir &lt;hagen|brenna|ulrik|tobin&gt; | poner | olvidar] (sin mas: como estan). */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comando() {
        return net.minecraft.commands.Commands.literal("herreros")
                .executes(c -> decir(c.getSource(), resumen(Morvain.nivel(c.getSource().getServer()))))
                .then(net.minecraft.commands.Commands.literal("ir").then(net.minecraft.commands.Commands.argument("quien",
                        com.mojang.brigadier.arguments.StringArgumentType.word()).suggests((c, b) -> {
                    for (Sitio s : TODOS) b.suggest(s.id());
                    return b.buildFuture();
                }).executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    ServerLevel m = Morvain.nivel(sp.server);
                    Sitio s = porId(com.mojang.brigadier.arguments.StringArgumentType.getString(c, "quien"));
                    if (m == null || s == null) return 0;
                    llevar(sp, m, s);
                    return decir(c.getSource(), s.id() + ": " + s.herrero().toShortString());
                })))
                .then(net.minecraft.commands.Commands.literal("poner").executes(c -> {
                    ServerLevel m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    ponerTodos(m);
                    return decir(c.getSource(), resumen(m));
                }))
                .then(net.minecraft.commands.Commands.literal("olvidar").executes(c -> {
                    ServerLevel m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    olvidar(m);
                    return decir(c.getSource(), resumen(m));
                }));
    }

    /** Lleva al jugador delante del yunque de esa herreria, mirando al herrero. */
    public static void llevar(ServerPlayer sp, ServerLevel m, Sitio s) {
        BlockPos de = s.pos(s.hu, 1, s.hv + 2), h = s.herrero();
        m.getChunk(de.getX() >> 4, de.getZ() >> 4);
        float giro = (float) (Math.atan2(h.getZ() - de.getZ(), h.getX() - de.getX()) * 180 / Math.PI) - 90f;
        sp.teleportTo(m, de.getX() + 0.5, de.getY() + 0.05, de.getZ() + 0.5, giro, 10f);
    }

    private static int decir(net.minecraft.commands.CommandSourceStack src, String s) {
        src.sendSuccess(() -> net.minecraft.network.chat.Component.literal(s), false);
        return 1;
    }

    /** Si (x, z) esta en el Refugio de la Fragua (para el nombre del sitio y su logro). */
    public static boolean enRefugio(double x, double z) {
        return REFUGIO.fuera(x, z) < 1.5;
    }

    /** Como esta cada herreria: el bloque del yunque, el del fuego y su herrero. */
    public static String resumen(ServerLevel m) {
        StringBuilder sb = new StringBuilder();
        for (Sitio s : TODOS) {
            if (sb.length() > 0) sb.append("; ");
            BlockPos y = s.yunque();
            sb.append(s.id()).append(" junto a ").append(s.santuario).append(" ").append(s.herrero().toShortString());
            if (!m.isLoaded(y)) {
                sb.append(" sin cargar");
                continue;
            }
            Herrero h = buscar(m, s);
            sb.append(" yunque ").append(m.getBlockState(y).getBlock() instanceof AnvilBlock).append(" herrero ")
                    .append(h == null ? "no" : h.quien().id + " mira " + Math.round(h.getYRot())).append(" puesto ").append(puesto(m.getServer(), s));
        }
        return sb.toString();
    }
}
