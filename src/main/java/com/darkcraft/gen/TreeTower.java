package com.darkcraft.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * La gran torre central convertida en "torre-arbol": un torreon de piedra envuelto por un arbol
 * gigantesco. La madera sube en espiral por la torre, le salen ramas con hojas, raices que bajan por
 * el torreon y, arriba del todo, la SALA DEL JEFE: una gran sala circular con cupula, sostenida por
 * el arbol, con el trono del caballero no-muerto. Encima, la copa del arbol atravesada por la aguja.
 */
public class TreeTower extends Element {
    private final int cx, cz, baseY, half, towerTop, groundY;
    private final Rect keep;
    /** Sala del jefe. */
    public final int hallFloor;          // nivel que se pisa en la sala
    public static final int HALL_R = 40;  // radio exterior del muro (unos 81 bloques de ancho)
    public static final int HALL_H = 26;  // altura de los muros por dentro
    private static final int DOME = 14;   // altura extra de la cupula
    /** El arbol gigante del centro de la sala: tronco hueco con la recepcion dentro. */
    public static final int TRUNK_R = 17, RECEP_H = 12;
    public static final double RECEP_R = 14.5;
    /** Hueco del ascensor (5x5) dentro de la torre: dx -2..2, dz 2..6 respecto al centro. */
    public static final int SHAFT_Z = 4;
    /** Puerta de la Sala del Arbol: en el tronco, al norte; 7 de ancho y 10 de alto. */
    public static final int DOOR_W = 7, DOOR_H = 10;
    private final int domeTop, crownBase;
    private final List<double[]> blobs = new ArrayList<>();   // x,y,z,r,aplastado
    private final List<int[]> limbs = new ArrayList<>();      // x0,y0,z0,x1,y1,z1,grosor
    private final List<int[]> hangs = new ArrayList<>();      // x,y,z,largo,tipo
    private final Noise noise;
    private static final int SPIRE_R = 5, SPIRE_H = 62;
    /** Altura del pararrayos de la aguja sobre el suelo de la sala. */
    public static final int PUNTA = HALL_H + DOME - 1 + SPIRE_H + 5;

    public TreeTower(long seed, int cx, int cz, int baseY, int half, int towerTop, Rect keep, int groundY) {
        super(seed);
        this.cx = cx;
        this.cz = cz;
        this.baseY = baseY;
        this.half = half;
        this.towerTop = towerTop;
        this.keep = keep;
        this.groundY = groundY;
        this.hallFloor = towerTop + 1;
        this.domeTop = hallFloor + HALL_H + DOME;
        this.crownBase = domeTop;
        this.noise = new Noise(seed);
        Rng r = new Rng(seed);
        // ramas a media altura por el lado de madera
        for (int y = towerTop - 95; y <= towerTop - 28; y += 13) {
            double a = woodAngle(y) + (r.nextDouble() - 0.5) * 0.6;
            int len = r.range(11, 18);
            int sx = cx + (int) Math.round(Math.cos(a) * (half + 1)), sz = cz + (int) Math.round(Math.sin(a) * (half + 1));
            int ex = cx + (int) Math.round(Math.cos(a) * (half + len)), ez = cz + (int) Math.round(Math.sin(a) * (half + len));
            int ey = y + r.range(5, 10);
            limbs.add(new int[]{sx, y, sz, ex, ey, ez, 2});
            blobs.add(new double[]{ex, ey + 2, ez, 5 + r.nextDouble() * 2, 0.6});
            for (int k = 0; k < 4; k++) hangs.add(new int[]{ex + r.range(-4, 4), ey, ez + r.range(-4, 4), r.range(3, 8), k % 2});
        }
        // ramas que salen del borde de la sala hacia la copa
        int nb = 11;
        for (int i = 0; i < nb; i++) {
            double a = i * Math.PI * 2 / nb + r.nextDouble() * 0.45;
            int len = r.range(HALL_R + 12, HALL_R + 22);
            int sy = hallFloor + r.range(8, 14);
            int sx = cx + (int) Math.round(Math.cos(a) * (HALL_R + 1)), sz = cz + (int) Math.round(Math.sin(a) * (HALL_R + 1));
            int ex = cx + (int) Math.round(Math.cos(a) * len), ez = cz + (int) Math.round(Math.sin(a) * len);
            int ey = domeTop + r.range(4, 12);
            limbs.add(new int[]{sx, sy, sz, ex, ey, ez, 2});
            blobs.add(new double[]{ex, ey + 3, ez, 9 + r.nextDouble() * 3, 0.55});
            for (int k = 0; k < 8; k++) {
                double aa = r.nextDouble() * Math.PI * 2, dd = r.nextDouble() * 9;
                hangs.add(new int[]{ex + (int) Math.round(Math.cos(aa) * dd), ey - 1, ez + (int) Math.round(Math.sin(aa) * dd), r.range(4, 11), k % 3});
            }
        }
        // ramas que suben desde la cupula hacia el centro de la copa
        for (int i = 0; i < 4; i++) {
            double a = i * Math.PI / 2 + Math.PI / 4;
            int ex = cx + (int) Math.round(Math.cos(a) * 26), ez = cz + (int) Math.round(Math.sin(a) * 26);
            limbs.add(new int[]{cx + (int) Math.round(Math.cos(a) * (TRUNK_R - 3)), domeTop - 2, cz + (int) Math.round(Math.sin(a) * (TRUNK_R - 3)), ex, domeTop + 16, ez, 3});
        }
        blobs.add(new double[]{cx, domeTop + 18, cz, 36, 0.42});
        blobs.add(new double[]{cx, domeTop + 32, cz, 20, 0.55});
        int reach = HALL_R + 36;
        bounds(Math.min(cx - reach, keep.x0 - 9), groundY - 2, Math.min(cz - reach, keep.z0 - 9),
                Math.max(cx + reach, keep.x1 + 9), domeTop + SPIRE_H + 12, Math.max(cz + reach, keep.z1 + 9));
    }

    /** Donde aparecera el jefe (fase 3): en la tarima, delante del trono. */
    public int[] bossSpot() {
        return new int[]{cx, hallFloor + 2, cz - HALL_R + 7};
    }

    public int centerX() {
        return cx;
    }

    public int centerZ() {
        return cz;
    }

    public int base() {
        return baseY;
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
        bowl(c);
        hall(c);
        trunk(c);
        hallFurniture(c);
        atrio(c);
        arbolInterior(c);
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
                            if (!cornerZone && (aa % 6 == 3 || aa % 6 == 4) && fr >= 3 && fr <= 8 && rel > 4 && w < 0.35) s = Pal.glass(x, y / 4, z);
                            // luz en la cara de dentro, entre ventana y ventana (el hueco de la escalera es muy alto)
                            if (m == H - 1 && !cornerZone && aa % 6 == 0 && (fr == 6 || fr == 0) && rel > 2 && y < towerTop - 2)
                                s = Pal.b(Blocks.SHROOMLIGHT);
                        }
                        c.set(x, y, z, s);
                    }
                } else {
                    c.column(x, z, baseY, towerTop - 1, Pal.AIR);
                    c.set(x, baseY - 1, z, ((dx + dz) & 1) == 0 ? Pal.DS_TILES : Pal.PB);
                }
            }
        // puerta sur
        for (int k = -1; k <= 1; k++)
            for (int d = H - 2; d <= H + 6; d++) {
                int x = cx + k, z = cz + d;
                for (int y = baseY; y <= baseY + 3; y++) c.set(x, y, z, Pal.AIR);
                c.set(x, baseY - 1, z, Pal.DS_TILES);
            }
    }

    // ------------------------------------------------------------ atrio: escalera segura y hueco del ascensor

    /**
     * El camino de la escalera (linea central, a 10 del centro): {dx, dz, superficie, subida (0 N, 1 O, 2 S, 3 E), tipo}
     * con tipo 0 = peldano (el bloque de escalera esta en superficie-1) y 1 = rellano de esquina. Sube pegada a los
     * muros, 15 peldanos por lado y rellanos en las esquinas, hasta el suelo de la recepcion (dentro del arbol).
     */
    public List<int[]> escalera() {
        List<int[]> out = new ArrayList<>();
        int F = hallFloor, y = baseY;
        int[][] esquina = {{10, -10}, {-10, -10}, {-10, 10}, {10, 10}};
        out.add(new int[]{10, 10, y, 0, 1});
        int side = 0;
        while (y < F) {
            for (int k = 0; k < 15 && y < F; k++) {
                int o = 7 - k;
                y++;
                int[] cell = switch (side) {
                    case 0 -> new int[]{10, o};
                    case 1 -> new int[]{o, -10};
                    case 2 -> new int[]{-10, -o};
                    default -> new int[]{-o, 10};
                };
                out.add(new int[]{cell[0], cell[1], y, side, 0});
            }
            if (y >= F) break;
            out.add(new int[]{esquina[side][0], esquina[side][1], y, side, 1});
            side = (side + 1) & 3;
        }
        return out;
    }

    private static Direction subida(int side) {
        return switch (side) {
            case 0 -> Direction.NORTH;
            case 1 -> Direction.WEST;
            case 2 -> Direction.SOUTH;
            default -> Direction.EAST;
        };
    }

    private void atrio(Canvas c) {
        int F = hallFloor;
        // hueco del ascensor: tubo de cristal con esquinas de piedra negra, anillos de oro y luces
        for (int dx = -3; dx <= 3; dx++)
            for (int dz = SHAFT_Z - 3; dz <= SHAFT_Z + 3; dz++) {
                boolean borde = Math.abs(dx) == 3 || Math.abs(dz - SHAFT_Z) == 3;
                if (!borde) continue;
                boolean esquina = Math.abs(dx) == 3 && Math.abs(dz - SHAFT_Z) == 3;
                for (int y = baseY; y <= F - 2; y++) {
                    int k = (y - baseY) % 12;
                    BlockState st;
                    if (k == 11) st = Pal.GILDED;
                    else if (esquina) st = (k == 5 || k == 2 || k == 8) ? Pal.b(Blocks.SHROOMLIGHT) : Pal.PB_BRICKS;
                    else st = Pal.b(Blocks.GLASS);
                    // entrada abajo, por el sur (mirando a la puerta de la torre)
                    if (y < baseY + 3 && dz - SHAFT_Z == 3 && Math.abs(dx) <= 1) st = Pal.AIR;
                    if (y == baseY + 3 && dz - SHAFT_Z == 3 && Math.abs(dx) <= 1) st = Pal.PB_CHISELED;
                    c.set(cx + dx, y, cz + dz, st);
                }
                // arriba: anillo de oro en el suelo de la recepcion y postes con farol en las esquinas
                c.set(cx + dx, F - 1, cz + dz, Pal.GILDED);
                if (esquina && dz - SHAFT_Z == 3) {
                    c.set(cx + dx, F, cz + dz, Pal.PB_CHISELED);
                    c.set(cx + dx, F + 1, cz + dz, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
                    c.set(cx + dx, F + 2, cz + dz, Pal.SOUL_LANTERN);
                }
            }
        // abajo, el disco se posa en una runa dorada; arriba, el hueco queda tapado con barrera (el disco pasa)
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = SHAFT_Z - 2; dz <= SHAFT_Z + 2; dz++) {
                c.set(cx + dx, baseY - 1, cz + dz, (Math.abs(dx) == 2 || Math.abs(dz - SHAFT_Z) == 2) ? Pal.GILDED : Pal.PB_CHISELED);
                c.set(cx + dx, F - 1, cz + dz, Pal.b(Blocks.BARRIER));
                for (int y = F; y <= F + 2; y++) c.set(cx + dx, y, cz + dz, Pal.AIR);
            }
        // la escalera
        List<int[]> path = escalera();
        java.util.Set<Long> hueco = new java.util.HashSet<>(), ultimo = new java.util.HashSet<>();
        int n = 0;
        for (int[] p : path) {
            int px = p[0], pz = p[1], sup = p[2], side = p[3];
            if (p[4] == 1) {
                int sx = Integer.signum(px), sz = Integer.signum(pz);
                for (int a = 8; a <= 11; a++)
                    for (int b = 8; b <= 11; b++) {
                        int x = cx + sx * a, z = cz + sz * b;
                        c.set(x, sup - 1, z, (a + b) % 2 == 0 ? Pal.PB_BRICKS : Pal.PB);
                        c.set(x, sup - 2, z, Pal.PB_BRICKS);
                        for (int y = sup; y <= Math.min(sup + 3, F - 2); y++) c.set(x, y, z, Pal.AIR);
                    }
                // descansillo: cofre, farol en un poste y un asiento en la esquina de fuera
                if (sup > baseY + 1 && sup + 3 < F - 1) {
                    c.chest(cx + sx * 11, sup, cz + sz * 11, Pal.dirOf(-sx, 0), Decor.loot("tesoro"));
                    c.set(cx + sx * 11, sup, cz + sz * 10, Pal.DO_FENCE);
                    c.set(cx + sx * 11, sup + 1, cz + sz * 10, Pal.LANTERN);
                    c.set(cx + sx * 10, sup, cz + sz * 11, Pal.stairs(Blocks.DARK_OAK_STAIRS, Pal.dirOf(0, sz), false));
                }
                // poste en la esquina de dentro (tapa el hueco en diagonal entre las dos barandillas)
                for (int y = sup - 1; y <= sup + 1; y++) c.set(cx + sx * 7, y, cz + sz * 7, Pal.PB_CHISELED);
                if (sup + 2 < F - 1) c.set(cx + sx * 7, sup + 2, cz + sz * 7, Pal.SOUL_LANTERN);
                continue;
            }
            Direction up = subida(side);
            int ox = side == 0 ? 1 : side == 2 ? -1 : 0, oz = side == 1 ? -1 : side == 3 ? 1 : 0;
            for (int w = -1; w <= 1; w++) {
                int x = cx + px + ox * w, z = cz + pz + oz * w;
                c.set(x, sup - 1, z, Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, up, false));
                c.set(x, sup - 2, z, Pal.PB_BRICKS);
                for (int y = sup; y <= Math.min(sup + 3, F - 2); y++) c.set(x, y, z, Pal.AIR);
                if (sup >= F - 4 && sup <= F - 1) hueco.add(key(x - cx, z - cz));
                if (sup == F) ultimo.add(key(x - cx, z - cz));
            }
            // barandilla de dentro (2 de alto): piedra abajo, barrotes encima; postes con farol cada 5
            int rx = cx + px - ox * 2, rz = cz + pz - oz * 2;
            boolean poste = (n++ % 5) == 0;
            Direction.Axis eje = (side == 0 || side == 2) ? Direction.Axis.Z : Direction.Axis.X;
            if (sup < F) {
                c.set(rx, sup - 1, rz, Pal.PB_BRICKS);
                c.set(rx, sup, rz, poste ? Pal.PB_CHISELED : Pal.PB_BRICKS);
                c.set(rx, sup + 1, rz, poste ? Pal.PB_CHISELED : Pal.connected(Blocks.IRON_BARS, eje));
                if (poste && sup + 2 < F - 1) c.set(rx, sup + 2, rz, Pal.LANTERN);
            }
            // a lo largo del muro de fuera: estandartes y, en el centro de cada lado, un cuadro
            int o = side == 0 ? pz : side == 1 ? px : side == 2 ? -pz : -px;
            int wx = cx + px + ox, wz = cz + pz + oz;
            Direction mira = Pal.dirOf(-ox, -oz);
            if (sup + 4 < F - 1) {
                if (Math.abs(o) == 2) {
                    BlockState l = ModDeco.wallLight(mira.getOpposite(), n);
                    c.set(wx, sup + 2, wz, l != null ? l : Blocks.WALL_TORCH.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.WallTorchBlock.FACING, mira));
                }
                if (o == 0) cuadro(c, wx, sup + 2, wz, mira, n);
                else if (Math.abs(o) == 5 && ((n / 15) & 1) == 0)
                    c.set(wx, sup + 3, wz, ((n / 30) % 2 == 0 ? Blocks.BLACK_WALL_BANNER : Blocks.RED_WALL_BANNER).defaultBlockState()
                            .setValue(WallBannerBlock.FACING, mira));
            }
        }
        // la salida de la escalera en la recepcion: hueco en el suelo con barandilla alrededor
        for (long k : hueco) c.set(cx + kx(k), F - 1, cz + kz(k), Pal.AIR);
        java.util.Set<Long> rail = new java.util.HashSet<>();
        for (long k : hueco)
            for (int ax = -1; ax <= 1; ax++)
                for (int az = -1; az <= 1; az++) {
                    long q = key(kx(k) + ax, kz(k) + az);
                    if (!hueco.contains(q) && !ultimo.contains(q)) rail.add(q);
                }
        for (long k : rail) {
            int x = cx + kx(k), z = cz + kz(k);
            boolean nn = rail.contains(key(kx(k), kz(k) - 1)), ss = rail.contains(key(kx(k), kz(k) + 1));
            boolean ee = rail.contains(key(kx(k) + 1, kz(k))), ww = rail.contains(key(kx(k) - 1, kz(k)));
            boolean recto = (nn && ss && !ee && !ww) || (ee && ww && !nn && !ss);
            var W = net.minecraft.world.level.block.state.properties.WallSide.LOW;
            var N0 = net.minecraft.world.level.block.state.properties.WallSide.NONE;
            BlockState st = Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH_WALL, nn ? W : N0)
                    .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH_WALL, ss ? W : N0)
                    .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.EAST_WALL, ee ? W : N0)
                    .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WEST_WALL, ww ? W : N0)
                    .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.UP, !recto);
            c.set(x, F, z, st);
            if (!recto) c.set(x, F + 1, z, Pal.LANTERN);
        }
    }

    private static long key(int dx, int dz) {
        return ((long) dx << 32) ^ (dz & 0xffffffffL);
    }

    private static int kx(long k) {
        return (int) (k >> 32);
    }

    private static int kz(long k) {
        return (int) k;
    }

    private static final String[] CUADROS = {"skull_and_roses", "wither", "bust", "void", "fire", "match", "stage", "earth", "wind", "water"};

    /** Un cuadro de 2 x 2 colgado en el muro de fuera de la escalera. */
    private void cuadro(Canvas c, int x, int y, int z, Direction mira, int n) {
        if (!c.inXZ(x, z)) return;
        String name = CUADROS[Math.floorMod(Hash.h(x, y, z, 31) + n, CUADROS.length)];
        var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.PAINTING_VARIANT,
                new net.minecraft.resources.ResourceLocation("minecraft", name));
        var holder = net.minecraft.core.registries.BuiltInRegistries.PAINTING_VARIANT.getHolder(key);
        if (holder.isEmpty()) return;
        c.addEntity(new net.minecraft.world.entity.decoration.Painting(c.level.getLevel(), new net.minecraft.core.BlockPos(x, y, z), mira, holder.get()));
    }

    // ------------------------------------------------------------ cuenco de madera que sostiene la sala
    private void bowl(Canvas c) {
        int depth = 26;
        int y0 = towerTop - depth;
        for (int x = Math.max(cx - HALL_R - 2, c.minX); x <= Math.min(cx + HALL_R + 2, c.maxX); x++)
            for (int z = Math.max(cz - HALL_R - 2, c.minZ); z <= Math.min(cz + HALL_R + 2, c.maxZ); z++) {
                int dx = x - cx, dz = z - cz;
                if (Math.max(Math.abs(dx), Math.abs(dz)) <= half) continue; // la torre ya esta
                double d = Math.sqrt(dx * dx + dz * dz);
                double ang = Math.atan2(dz, dx);
                for (int y = y0; y < towerTop; y++) {
                    double k = (y - y0) / (double) depth;
                    double rr = half * 1.05 + Math.pow(k, 0.8) * (HALL_R + 0.6 - half * 1.05) + noise.value(x * 0.2, y * 0.2, z * 0.2) * 1.2;
                    if (d > rr) continue;
                    // costillas de piedra cada 45 grados, el resto madera viva
                    boolean rib = Math.abs(Math.sin(ang * 4)) < 0.08 + 0.02 * k;
                    c.set(x, y, z, rib ? Pal.PB_BRICKS : bark(x, y, z, Direction.Axis.Y));
                }
            }
    }

    // ------------------------------------------------------------ SALA DEL ARBOL
    private int ceiling(double d) {
        double k = Math.min(1, d / (HALL_R - 1.0));
        return hallFloor + HALL_H - 1 + (int) Math.round(DOME * Math.sqrt(Math.max(0, 1 - k * k)));
    }

    /** Balcon corrido a media altura: el suelo esta en F+12 (se pisa en F+13) entre BAL_IN y BAL_OUT. */
    public static final int BALCON = 12;
    private static final double BAL_IN = HALL_R + 0.5 - 7.2, BAL_OUT = HALL_R + 0.5 - 2, BAL_EDGE = BAL_IN - 1.45;

    /** Distancia angular (radianes, 0..pi) entre ang y el norte (donde estan el trono y la puerta). */
    private static double alNorte(double ang) {
        double a = ang + Math.PI / 2;
        return Math.abs(Math.atan2(Math.sin(a), Math.cos(a)));
    }

    /** Al norte, detras del trono, no hay balcon: se ve el roseton entero. */
    private static boolean sectorBalcon(double ang) {
        return alNorte(ang) > 0.45;
    }

    /** Escaleras al balcon: dos tramos rectos pegados al muro este y oeste, subiendo hacia el norte. */
    private static final int ESC_X = 34, ESC_Z0 = 6;

    private void hall(Canvas c) {
        int F = hallFloor;
        double R = HALL_R + 0.5;
        for (int x = Math.max(cx - HALL_R - 3, c.minX); x <= Math.min(cx + HALL_R + 3, c.maxX); x++)
            for (int z = Math.max(cz - HALL_R - 3, c.minZ); z <= Math.min(cz + HALL_R + 3, c.maxZ); z++) {
                int dx = x - cx, dz = z - cz;
                double d = Math.sqrt(dx * dx + dz * dz);
                double ang = Math.atan2(dz, dx);
                if (d > R + 1.6) continue;
                if (d > R) {
                    // costillas de madera que trepan por fuera del muro
                    for (int y = F - 1; y <= F + HALL_H + 2; y++)
                        if (Math.sin(ang * 9 + (y - F) * 0.12 + noise.value(x * 0.2, y * 0.1, z * 0.2)) > 0.86)
                            c.set(x, y, z, bark(x, y, z, Direction.Axis.Y));
                    continue;
                }
                c.set(x, F - 1, z, hallFloorBlock(x, z, d, dx, dz));
                if (d > R - 2) {
                    muro(c, x, z, d, ang, d > R - 1);
                    for (int y = F + HALL_H + 1; y <= ceiling(d) + 1; y++) c.set(x, y, z, Pal.wall(x, y, z));
                    continue;
                }
                // interior: aire hasta la cupula
                int ceil = ceiling(d);
                c.column(x, z, F, ceil - 1, Pal.AIR);
                // cupula de dos bloques con nervios de madera
                double sector = Math.PI / 10;
                double rel = ang / sector;
                boolean rib = Math.abs(rel - Math.round(rel)) * sector * Math.max(d, 1) < 0.9;
                for (int y = ceil; y <= ceil + 1; y++)
                    c.set(x, y, z, rib ? Pal.log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Y) : Pal.roof(x, y, z));
                // balcon
                if (sectorBalcon(ang) && d > BAL_EDGE) {
                    c.set(x, F + BALCON, z, d > BAL_IN ? (((x + z) & 1) == 0 ? Pal.PB_BRICKS : Pal.DS_TILES) : Pal.PB_BRICKS);
                    // cornisa: escalones del reves bajo el borde
                    if (d <= BAL_EDGE + 1) c.set(x, F + BALCON - 1, z, Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Pal.dirOf(dx, dz), true));
                }
            }
        // barandilla del balcon (muro de piedra conectado, sin huecos en diagonal)
        for (int x = Math.max(cx - HALL_R, c.minX); x <= Math.min(cx + HALL_R, c.maxX); x++)
            for (int z = Math.max(cz - HALL_R, c.minZ); z <= Math.min(cz + HALL_R, c.maxZ); z++)
                if (baranda(x - cx, z - cz)) {
                    BlockState st = barandaState(x - cx, z - cz);
                    c.set(x, F + BALCON + 1, z, st);
                    if (st.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.UP) && Hash.pct(x, 0, z, seed + 55) < 45)
                        c.set(x, F + BALCON + 2, z, Pal.LANTERN);
                }
        escalerasBalcon(c);
    }

    /** Casillas de la barandilla del balcon: el borde de dentro (anillo de 1,45 para que no queden huecos en diagonal) y los cortes del norte. */
    private boolean baranda(int dx, int dz) {
        double d = Math.sqrt(dx * dx + dz * dz), ang = Math.atan2(dz, dx);
        if (d > BAL_OUT || d <= BAL_EDGE) return false;
        double a = alNorte(ang);
        if (a <= 0.45) return false;
        if (a <= 0.45 + 1.3 / d) return true;
        if (d <= BAL_IN) return true;
        // agujero de la escalera: barandilla por el sur
        return Math.abs(dx) >= ESC_X && dz == -1;
    }

    private BlockState barandaState(int dx, int dz) {
        BlockState s = Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState();
        boolean n = baranda(dx, dz - 1), so = baranda(dx, dz + 1), e = baranda(dx + 1, dz), w = baranda(dx - 1, dz);
        s = s.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH_WALL, n ? net.minecraft.world.level.block.state.properties.WallSide.LOW : net.minecraft.world.level.block.state.properties.WallSide.NONE)
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH_WALL, so ? net.minecraft.world.level.block.state.properties.WallSide.LOW : net.minecraft.world.level.block.state.properties.WallSide.NONE)
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.EAST_WALL, e ? net.minecraft.world.level.block.state.properties.WallSide.LOW : net.minecraft.world.level.block.state.properties.WallSide.NONE)
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WEST_WALL, w ? net.minecraft.world.level.block.state.properties.WallSide.LOW : net.minecraft.world.level.block.state.properties.WallSide.NONE);
        boolean recto = (n && so && !e && !w) || (e && w && !n && !so);
        return s.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.UP, !recto || Hash.pct(dx, 0, dz, 77) < 12);
    }

    /** Los dos tramos de escalera que suben al balcon (13 peldanos, del sur al norte, pegados al muro). */
    private void escalerasBalcon(Canvas c) {
        int F = hallFloor;
        double wallIn = HALL_R + 0.5 - 2;
        for (int s = -1; s <= 1; s += 2)
            for (int k = 0; k <= BALCON; k++) {
                int dz = ESC_Z0 - k, sup = F + 1 + k;
                for (int a = ESC_X; a <= HALL_R; a++) {
                    int dx = s * a;
                    if (Math.sqrt(dx * dx + dz * dz) > wallIn) break;
                    int x = cx + dx, z = cz + dz;
                    for (int y = F; y < sup - 1; y++) c.set(x, y, z, Pal.PB_BRICKS);
                    c.set(x, sup - 1, z, a <= ESC_X + 2 ? Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.NORTH, false) : Pal.PB_BRICKS);
                    for (int y = sup; y <= Math.min(sup + 3, F + BALCON + 3); y++)
                        if (y != F + BALCON || k >= BALCON - 4) c.set(x, y, z, Pal.AIR);
                }
                // barandilla por dentro (2 de alto en lo alto del tramo)
                int rx = cx + s * (ESC_X - 1), rz = cz + dz;
                if (k >= 2) {
                    c.set(rx, sup - 1, rz, Pal.PB_BRICKS);
                    c.set(rx, sup, rz, k % 4 == 0 ? Pal.PB_CHISELED : Pal.wallBlock(Blocks.POLISHED_BLACKSTONE_BRICK_WALL, Direction.Axis.Z));
                    if (k % 4 == 0 && sup + 1 < F + BALCON) c.set(rx, sup + 1, rz, Pal.LANTERN);
                }
            }
    }

    /** Vidriera (vidrio de colores segun la posicion dentro del ventanal). */
    private BlockState vidrio(int ry, double fracToWin, int sectorIdx) {
        if (fracToWin > 2.65) return Pal.GLASS_RED;
        boolean medallon = fracToWin > 1.0 && fracToWin < 2.2 && (ry % 4 == 1);
        if (medallon) return (sectorIdx & 1) == 0 ? Pal.GLASS_YELLOW : Pal.GLASS_ORANGE;
        return switch (Math.floorMod(ry / 2 + sectorIdx, 3)) {
            case 0 -> Pal.GLASS_PURPLE;
            case 1 -> Pal.GLASS_BLUE;
            default -> Pal.GLASS_RED;
        };
    }

    /** Roseton gigante detras del trono (al norte): null si (s, h) cae fuera. s = bloques a lo largo del muro, h = altura relativa al centro. */
    private static BlockState roseton(double s, double h) {
        double r = Math.sqrt(s * s + h * h);
        if (r >= 8.2) return null;
        if (r >= 7.2) return Pal.PB_CHISELED;
        double th = Math.atan2(h, s);
        if (r < 1.3) return Pal.GLASS_YELLOW;
        if (r < 2.1) return Pal.GILDED;
        if (Math.abs(Math.sin(th * 6)) < 0.16 * 3.0 / r) return Pal.PB;
        if (r > 5.6 && r < 6.3) return Pal.PB;
        if (r < 3.6) return Pal.GLASS_ORANGE;
        if (r < 5.6) return ((int) Math.floor((th + Math.PI) / (Math.PI / 6)) & 1) == 0 ? Pal.GLASS_RED : Pal.GLASS_PURPLE;
        return ((int) Math.floor((th + Math.PI) / (Math.PI / 12)) & 1) == 0 ? Pal.GLASS_BLUE : Pal.GLASS_RED;
    }

    private void muro(Canvas c, int x, int z, double d, double ang, boolean outer) {
        int F = hallFloor;
        double sector = Math.PI / 10;
        double rel = ang / sector;
        double fracToPil = Math.abs(rel - Math.round(rel)) * sector * d;
        double fracToWin = Math.abs((rel - Math.floor(rel)) - 0.5) * sector * d;
        int sectorIdx = (int) Math.floor(rel);
        double sNorte = (ang + Math.PI / 2) * d; // bloques a lo largo del muro desde el norte
        for (int y = F - 1; y <= F + HALL_H; y++) {
            int ry = y - F;
            BlockState s = null;
            if (Math.abs(sNorte) < 9) {
                BlockState ro = roseton(sNorte, ry - 18);
                if (ro != null) s = (outer || !(ro.getBlock() instanceof net.minecraft.world.level.block.StainedGlassBlock)) ? ro : Pal.AIR;
            }
            if (s == null) {
                if (fracToPil < 1.6) {
                    s = (ry == 0 || ry == HALL_H - 1 || ry == BALCON) ? Pal.PB_CHISELED : Pal.BASALT;
                } else {
                    boolean bajo = ry >= 2 && ry <= 9, alto = ry >= 15 && ry <= 21;
                    boolean win = (bajo || alto) && fracToWin < 3.3 && fracToWin > 0.5;
                    boolean arch = ((ry == 10 || ry == 22) && fracToWin < 2.3 && fracToWin > 0.5) || ((ry == 11 || ry == 23) && fracToWin < 1.2);
                    if ((win || arch) && outer) s = vidrio(ry, fracToWin, sectorIdx);
                    else if (win || arch) s = Pal.AIR;
                    else {
                        double w = 0.5 + 0.38 * Math.sin(ang * 3 + y * 0.21) + noise.value(x * 0.25, y * 0.12, z * 0.25) * 0.3;
                        s = w > 0.8 ? bark(x, y, z, Direction.Axis.Y) : (ry % 8 == 7 ? Pal.trim(x, y, z) : Pal.wall(x, y, z));
                    }
                }
            }
            c.set(x, y, z, s);
        }
    }

    private BlockState hallFloorBlock(int x, int z, double d, int dx, int dz) {
        if (Math.abs(dx) <= 1 && dz < -16 && dz > -HALL_R + 9) return Pal.b(Blocks.RED_NETHER_BRICKS);
        if (d >= 21 && d < 22) return Pal.GILDED;
        if (d >= 29.5 && d < 30.5) return Pal.DS_TILES;
        if (d > BAL_IN) return ((x + z) & 1) == 0 ? Pal.PB_BRICKS : Pal.DS_TILES;
        return ((Math.floorDiv(x, 2) + Math.floorDiv(z, 2)) & 1) == 0 ? Pal.DS_POLISHED : Pal.PB;
    }

    // ------------------------------------------------------------ el arbol gigante de la sala y la recepcion en su tronco

    /** Centro de la fuente de la recepcion (relativo al centro). */
    public static final int FUENTE_Z = -6;

    /** Radio del tronco a la altura y: ancho hasta el techo de la recepcion, luego se estrecha hasta la copa. */
    private double trunkR(int x, int y, int z, double ang) {
        int ry = y - hallFloor;
        double r;
        if (ry <= RECEP_H + 3) r = TRUNK_R;
        else if (ry <= 38) r = TRUNK_R - (ry - RECEP_H - 3) * 5.0 / (38 - RECEP_H - 3);
        else r = 12 - (ry - 38) * 5.0 / 16;
        double lejosPuerta = Math.max(0, Math.min(1, (alNorte(ang) - 0.3) / 0.3));
        if (ry < 4) r += (4 - ry) * 0.75 * lejosPuerta * (0.55 + 0.45 * Math.abs(Math.sin(ang * 4)));
        r += noise.value(x * 0.2, y * 0.08, z * 0.2) * 0.8 * (ry < RECEP_H + 2 ? lejosPuerta : 1);
        return r;
    }

    private int vault(double d) {
        return (int) Math.round(3 * Math.max(0, 1 - (d / RECEP_R) * (d / RECEP_R)));
    }

    private void trunk(Canvas c) {
        int F = hallFloor, top = F + 54;
        int rr = TRUNK_R + 4;
        for (int x = Math.max(cx - rr, c.minX); x <= Math.min(cx + rr, c.maxX); x++)
            for (int z = Math.max(cz - rr, c.minZ); z <= Math.min(cz + rr, c.maxZ); z++) {
                int dx = x - cx, dz = z - cz;
                double d = Math.sqrt(dx * dx + dz * dz), ang = Math.atan2(dz, dx);
                int techo = RECEP_H + vault(d);
                double rel = ang / (Math.PI / 6);
                double aPil = Math.abs(rel - Math.round(rel)) * (Math.PI / 6);
                for (int y = F - 1; y <= top; y++) {
                    if (d > trunkR(x, y, z, ang)) continue;
                    int ry = y - F;
                    BlockState s;
                    if (d < RECEP_R && ry <= techo) {
                        if (ry == -1) s = recepFloor(dx, dz, d);
                        else if (ry < techo) s = Pal.AIR;
                        else s = (aPil * Math.max(d, 1) < 0.7 || (d > 6.5 && d < 7.4) || d < 1.5) ? Pal.GILDED : Pal.PB_BRICKS;
                    } else if (d < RECEP_R + 1 && ry <= RECEP_H) {
                        // forro de la recepcion: piedra negra, zocalo y cornisa de oro, pilastras de basalto
                        if (ry == 0 || ry == RECEP_H - 1) s = Pal.GILDED;
                        else if (aPil * d < 1.1) s = ry % 4 == 2 ? Pal.PB_CHISELED : Pal.BASALT;
                        else s = ry % 5 == 3 ? Pal.PB_BRICKS_CRACKED : Pal.PB_BRICKS;
                    } else if (ry >= 1 && ry <= 37 && d > trunkR(x, y, z, ang) - 1.0 && Hash.f(x, y, z, seed + 21) < 0.015) {
                        s = Pal.b(Blocks.SHROOMLIGHT);
                    } else {
                        s = bark(x, y, z, Direction.Axis.Y);
                    }
                    c.set(x, y, z, s);
                }
            }
        puerta(c);
        recepcion(c);
        lianasTronco(c);
    }

    private BlockState recepFloor(int dx, int dz, double d) {
        if (d >= 12.0 && d < 12.9) return Pal.GILDED;
        double df = Math.sqrt(dx * dx + (dz - FUENTE_Z) * (dz - FUENTE_Z));
        if (df >= 3.2 && df < 4.0) return Pal.GILDED;
        double a = Math.atan2(dz, dx);
        if (d > 4 && Math.abs(Math.sin(a * 4)) < 0.05 * 12 / d) return Pal.DS_TILES;
        return ((dx + dz) & 1) == 0 ? Pal.PB : Pal.DS_POLISHED;
    }

    /** La gran puerta de la Sala del Arbol: hueco de 7 x 10 en el tronco, al norte, con su marco (cerrada con barrera). */
    private void puerta(Canvas c) {
        int F = hallFloor;
        for (int dx = -4; dx <= 4; dx++)
            for (int dz = -20; dz <= -15; dz++) {
                int x = cx + dx, z = cz + dz;
                boolean hueco = Math.abs(dx) <= 3;
                double d = Math.sqrt(dx * dx + dz * dz), ang = Math.atan2(dz, dx);
                for (int y = F - 1; y <= F + 11; y++) {
                    int ry = y - F;
                    boolean enTronco = d <= trunkR(x, y, z, ang) + 0.3;
                    BlockState s;
                    if (ry == -1) s = !enTronco ? null : (hueco && dz == -15 ? Pal.GILDED : Pal.PB_BRICKS);
                    else if (hueco && ry <= 9) s = dz == -15 ? Pal.b(Blocks.BARRIER) : Pal.AIR;
                    else if (!enTronco) s = null;
                    else if (!hueco && ry <= 10) s = (ry == 0 || ry == 10) ? Pal.GILDED : (ry % 3 == 1 ? Pal.PB_CHISELED : Pal.BASALT);
                    else if (ry == 10) s = dx == 0 ? Pal.GILDED : Pal.PB_BRICKS;
                    else s = dz == -15 ? ((dx & 1) == 0 ? Pal.PB_CHISELED : Pal.GILDED) : null;
                    if (s != null) c.set(x, y, z, s);
                }
            }
    }

    /** Recepcion: fuente, estatuas, estandartes, lamparas y alfombra. */
    private void recepcion(Canvas c) {
        int F = hallFloor;
        // fuente redonda con pilar central del que cae el agua
        int fx = cx, fz = cz + FUENTE_Z;
        for (int dx = -3; dx <= 3; dx++)
            for (int dz = -3; dz <= 3; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                int x = fx + dx, z = fz + dz;
                if (d < 2.2) {
                    c.set(x, F - 1, z, Pal.PB);
                    c.set(x, F, z, Pal.WATER);
                } else if (d < 3.2) {
                    c.set(x, F, z, Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Pal.dirOf(dx, dz), false));
                    c.set(x, F - 1, z, Pal.PB_BRICKS);
                }
            }
        // surtidor bajo (que no tape la puerta desde el ascensor): pedestal con taza de oro y el agua cayendo
        c.set(fx, F, fz, Pal.PB_CHISELED);
        c.set(fx, F + 1, fz, Pal.GILDED);
        c.flowingWater(fx, F + 2, fz);
        // lampara de arana encima de la fuente
        int techo = F + RECEP_H + vault(Math.abs(FUENTE_Z));
        for (int y = techo - 1; y > F + 8; y--) c.set(fx, y, fz, Pal.CHAIN);
        BlockState ch = ModDeco.chandelier(true, false);
        c.set(fx, F + 8, fz, ch != null ? ch : Pal.SOUL_LANTERN_HANG);
        for (int[] p : new int[][]{{7, -8}, {-7, -8}, {8, 6}, {-8, 6}}) {
            int x = cx + p[0], z = cz + p[1];
            int t = F + RECEP_H + vault(Math.sqrt(p[0] * p[0] + p[1] * p[1]));
            for (int y = t - 1; y > F + 9; y--) c.set(x, y, z, Pal.CHAIN);
            BlockState sm = ModDeco.chandelier(true, true);
            c.set(x, F + 9, z, sm != null ? sm : Pal.LANTERN_HANG);
        }
        // alfombra roja: del ascensor a la fuente, alrededor de ella y hasta la puerta
        BlockState roja = Pal.b(Blocks.RED_CARPET), negra = Pal.b(Blocks.BLACK_CARPET);
        for (int dx = -5; dx <= 5; dx++)
            for (int dz = -14; dz <= 0; dz++) {
                int ax = Math.abs(dx);
                boolean on = (ax <= 1 && dz >= -2) || ((ax == 4 || ax == 5) && dz <= -2 && dz >= -10) || (dz == -2 || dz == -10) || (ax <= 2 && dz <= -11);
                if (!on) continue;
                double df = Math.sqrt(dx * dx + (dz - FUENTE_Z) * (dz - FUENTE_Z));
                if (df < 3.3) continue;
                boolean borde = (dz <= -11 && ax == 2) || ((ax == 5) && dz <= -2 && dz >= -10);
                c.set(cx + dx, F, cz + dz, borde ? negra : roja);
            }
        // farolas a los lados de la alfombra
        for (int[] p : new int[][]{{4, -11}, {-4, -11}}) {
            int x = cx + p[0], z = cz + p[1];
            c.set(x, F, z, Pal.PB_CHISELED);
            c.set(x, F + 1, z, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
            c.set(x, F + 2, z, Pal.LANTERN);
        }
        // caballeros de piedra negra (armaduras) en pedestales, mirando al centro
        double[] angs = {-65, -115, 0, 55, 125, 165};
        for (int i = 0; i < angs.length; i++) {
            double a = Math.toRadians(angs[i]);
            int x = cx + (int) Math.round(Math.cos(a) * 12.2), z = cz + (int) Math.round(Math.sin(a) * 12.2);
            c.set(x, F, z, Pal.PB_CHISELED);
            c.set(x, F - 1, z, Pal.GILDED);
            if (!c.inXZ(x, z)) continue;
            net.minecraft.world.entity.decoration.ArmorStand st = net.minecraft.world.entity.EntityType.ARMOR_STAND.create(c.level.getLevel());
            if (st == null) continue;
            float yaw = (float) Math.toDegrees(Math.atan2(cz + 0.5 - (z + 0.5), cx + 0.5 - (x + 0.5))) - 90f;
            st.moveTo(x + 0.5, F + 1, z + 0.5, yaw, 0);
            st.setYHeadRot(yaw);
            st.setYBodyRot(yaw);
            st.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_HELMET));
            st.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_CHESTPLATE));
            st.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_LEGGINGS));
            st.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_BOOTS));
            st.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_SWORD));
            net.minecraft.nbt.CompoundTag t = new net.minecraft.nbt.CompoundTag();
            st.addAdditionalSaveData(t);
            t.putBoolean("ShowArms", true);
            t.putBoolean("NoBasePlate", true);
            t.putInt("DisabledSlots", 4144959);
            net.minecraft.nbt.CompoundTag pose = new net.minecraft.nbt.CompoundTag();
            pose.put("RightArm", rot(-70f, -8f, 0f));
            pose.put("LeftArm", rot(-70f, 12f, 0f));
            pose.put("Head", rot(8f, 0f, 0f));
            t.put("Pose", pose);
            st.readAdditionalSaveData(t);
            st.setInvulnerable(true);
            c.addEntity(st);
        }
        // estandartes en la pared, entre las estatuas
        double[] bans = {-40, -140, 27, 90, 150, -168};
        for (int i = 0; i < bans.length; i++) {
            double a = Math.toRadians(bans[i]);
            for (double rr = 13.0; rr <= 15.5; rr += 0.25) {
                int x = cx + (int) Math.round(Math.cos(a) * rr), z = cz + (int) Math.round(Math.sin(a) * rr);
                int dx = x - cx, dz = z - cz;
                Direction mira = Pal.dirOf(-dx, -dz);
                int bx = x - mira.getStepX(), bz = z - mira.getStepZ();
                double db = Math.sqrt((bx - cx) * (bx - cx) + (bz - cz) * (bz - cz));
                if (db < RECEP_R) continue;
                c.set(x, F + 8, z, ((i & 1) == 0 ? Blocks.RED_WALL_BANNER : Blocks.BLACK_WALL_BANNER).defaultBlockState()
                        .setValue(WallBannerBlock.FACING, mira));
                break;
            }
        }
        // apliques en las pilastras (cada 30 grados), a media altura
        for (int k = 0; k < 12; k++) {
            double a = k * Math.PI / 6;
            if (alNorte(a) < 0.4) continue; // la puerta
            for (double rr = 13.0; rr <= 15.5; rr += 0.25) {
                int x = cx + (int) Math.round(Math.cos(a) * rr), z = cz + (int) Math.round(Math.sin(a) * rr);
                int dx = x - cx, dz = z - cz;
                Direction mira = Pal.dirOf(-dx, -dz);
                int bx = x - mira.getStepX(), bz = z - mira.getStepZ();
                double db = Math.sqrt((bx - cx) * (bx - cx) + (bz - cz) * (bz - cz));
                if (db < RECEP_R) continue;
                BlockState l = ModDeco.wallLight(mira.getOpposite(), k);
                c.set(x, F + 4, z, l != null ? l : Blocks.SOUL_WALL_TORCH.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.WallTorchBlock.FACING, mira));
                break;
            }
        }
        // bancos a los lados de la llegada del ascensor
        for (int s = -1; s <= 1; s += 2)
            for (int dz = 3; dz <= 5; dz++) {
                BlockState b = ModDeco.bench(s < 0 ? Direction.EAST : Direction.WEST);
                c.set(cx + s * 6, F, cz + dz, b != null ? b : Pal.stairs(Blocks.DARK_OAK_STAIRS, s < 0 ? Direction.WEST : Direction.EAST, false));
            }
    }

    private static net.minecraft.nbt.ListTag rot(float a, float b, float c) {
        net.minecraft.nbt.ListTag l = new net.minecraft.nbt.ListTag();
        l.add(net.minecraft.nbt.FloatTag.valueOf(a));
        l.add(net.minecraft.nbt.FloatTag.valueOf(b));
        l.add(net.minecraft.nbt.FloatTag.valueOf(c));
        return l;
    }

    /** Enredaderas colgando por el tronco dentro de la sala. */
    private void lianasTronco(Canvas c) {
        int F = hallFloor;
        Rng r = new Rng(seed + 61);
        for (int i = 0; i < 40; i++) {
            double a = r.nextDouble() * Math.PI * 2;
            if (alNorte(a) < 0.35) continue;
            int y0 = F + 20 + r.range(0, 14), len = r.range(6, 20);
            for (int k = 0; k < len; k++) {
                int y = y0 - k;
                // por encima de la cabeza: nunca llegan al suelo de la sala
                if (y < F + com.darkcraft.gavran.SalaArbol.MIN_LIANA) break;
                // la primera casilla de aire fuera del tronco en esa direccion
                for (double rr = 11; rr <= 21; rr += 0.5) {
                    int x = cx + (int) Math.round(Math.cos(a) * rr), z = cz + (int) Math.round(Math.sin(a) * rr);
                    if (!c.inXZ(x, z)) break;
                    double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
                    if (d <= trunkR(x, y, z, Math.atan2(z - cz, x - cx))) continue;
                    Direction to = Pal.dirOf(cx - x, cz - z);
                    if (c.isAir(x, y, z) && !c.isAir(x + to.getStepX(), y, z + to.getStepZ()))
                        c.set(x, y, z, Blocks.VINE.defaultBlockState().setValue(net.minecraft.world.level.block.VineBlock.getPropertyForFace(to), true));
                    break;
                }
            }
        }
    }

    /** Ramas del arbol de dentro, bajo la cupula, con hojas, frutos brillantes y lianas. */
    private void arbolInterior(Canvas c) {
        int F = hallFloor;
        Rng r = new Rng(seed + 71);
        for (int k = 0; k < 6; k++) {
            double a = Math.toRadians(30 + 60 * k);
            int sy = F + 22 + (k % 2) * 3;
            int[] l = {cx + (int) Math.round(Math.cos(a) * 12), sy, cz + (int) Math.round(Math.sin(a) * 12),
                    cx + (int) Math.round(Math.cos(a) * 30), F + 31, cz + (int) Math.round(Math.sin(a) * 30), 2};
            ramaDentro(c, l);
            hojasDentro(c, l[3], F + 31, l[5], 6.5, r);
            hojasDentro(c, cx + (int) Math.round(Math.cos(a) * 22), F + 28, cz + (int) Math.round(Math.sin(a) * 22), 4.5, r);
        }
    }

    private void ramaDentro(Canvas c, int[] l) {
        int dx = l[3] - l[0], dy = l[4] - l[1], dz = l[5] - l[2];
        int n = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        Direction.Axis axis = Math.abs(dx) >= Math.abs(dz) ? Direction.Axis.X : Direction.Axis.Z;
        for (int i = 0; i <= n; i++) {
            double t = i / (double) n;
            double px = l[0] + dx * t, py = l[1] + dy * t + Math.sin(t * Math.PI) * 2.5, pz = l[2] + dz * t;
            double rad = l[6] * (1 - t * 0.55) + 0.3;
            int rr = (int) Math.ceil(rad);
            for (int ox = -rr; ox <= rr; ox++)
                for (int oy = -rr; oy <= rr; oy++)
                    for (int oz = -rr; oz <= rr; oz++) {
                        if (ox * ox + oy * oy + oz * oz > rad * rad + 0.3) continue;
                        int x = (int) Math.round(px) + ox, y = (int) Math.round(py) + oy, z = (int) Math.round(pz) + oz;
                        if (!c.inXZ(x, z) || !dentroSala(x, y, z)) continue;
                        c.set(x, y, z, bark(x, y, z, axis));
                    }
        }
    }

    /** Dentro del aire de la sala (sin tocar muros, cupula, balcon ni tronco). */
    private boolean dentroSala(int x, int y, int z) {
        double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
        return d <= HALL_R - 2.5 && y >= hallFloor + 16 && y < ceiling(d);
    }

    private void hojasDentro(Canvas c, int bx, int by, int bz, double rad, Rng r) {
        int rr = (int) Math.ceil(rad), ry = (int) Math.ceil(rad * 0.6);
        List<int[]> fondo = new ArrayList<>();
        for (int x = Math.max(bx - rr, c.minX); x <= Math.min(bx + rr, c.maxX); x++)
            for (int z = Math.max(bz - rr, c.minZ); z <= Math.min(bz + rr, c.maxZ); z++) {
                int low = Integer.MAX_VALUE;
                for (int y = by - ry; y <= by + ry; y++) {
                    double nx = (x - bx) / rad, ny = (y - by) / (rad * 0.6), nz = (z - bz) / rad;
                    double d = nx * nx + ny * ny + nz * nz + noise.value(x * 0.2, y * 0.2, z * 0.2) * 0.25;
                    if (d > 1 || !dentroSala(x, y, z) || !c.isAir(x, y, z)) continue;
                    c.set(x, y, z, Hash.pct(x, y, z, seed + 33) < 7 ? Pal.b(Blocks.SHROOMLIGHT) : leaf(x, y, z));
                    low = Math.min(low, y);
                }
                if (low != Integer.MAX_VALUE && Hash.pct(x, 0, z, seed + 34) < 22) fondo.add(new int[]{x, low, z});
            }
        // frutos brillantes y lianas colgando
        for (int[] f : fondo) {
            int len = 3 + Hash.pct(f[0], f[1], f[2], seed + 35) % 9;
            boolean bayas = Hash.pct(f[0], 1, f[2], seed + 36) < 60;
            for (int k = 1; k <= len; k++) {
                int y = f[1] - k;
                if (!c.isAir(f[0], y, f[2]) || y < hallFloor + 16) break;
                boolean tip = k == len;
                BlockState s = bayas ? (tip ? Blocks.CAVE_VINES.defaultBlockState().setValue(CaveVines.BERRIES, true)
                        : Blocks.CAVE_VINES_PLANT.defaultBlockState().setValue(CaveVines.BERRIES, k % 2 == 0))
                        : (tip ? Pal.b(Blocks.WEEPING_VINES) : Pal.b(Blocks.WEEPING_VINES_PLANT));
                c.set(f[0], y, f[2], s);
            }
        }
    }

    private void hallFurniture(Canvas c) {
        int F = hallFloor;
        int[] dais = {cx, cz - HALL_R + 3};
        Direction[] dirs = {Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH};
        // tarima semicircular con el trono al norte
        int tx = dais[0], tz = dais[1];
        for (int x = cx - 11; x <= cx + 11; x++)
            for (int z = cz - HALL_R + 1; z <= tz + 8; z++) {
                double dd = Math.sqrt((x - tx) * (x - tx) + (z - tz) * (z - tz));
                double dHall = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
                // hasta la cara de dentro del muro: sin zanja entre la tarima y el muro (detras del trono)
                if (dHall > HALL_R - 1.5 || dd > 9) continue;
                if (dd > 6.5) {
                    c.set(x, F, z, Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Pal.dirOf(tx - x, tz - z), false));
                } else {
                    c.set(x, F, z, Pal.PB_BRICKS);
                    c.set(x, F + 1, z, dd > 4.5 ? Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Pal.dirOf(tx - x, tz - z), false)
                            : (dd < 1.6 ? Pal.GILDED : Pal.PB_BRICKS));
                }
            }
        // trono gigante del caballero no-muerto
        int ty = F + 2;
        c.set(tx, ty, tz, Pal.stairs(Blocks.POLISHED_BLACKSTONE_STAIRS, Direction.NORTH, false));
        c.set(tx - 1, ty, tz, Pal.PB_CHISELED);
        c.set(tx + 1, ty, tz, Pal.PB_CHISELED);
        c.set(tx - 1, ty + 1, tz, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
        c.set(tx + 1, ty + 1, tz, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
        for (int y = ty; y <= ty + 8; y++) {
            c.set(tx, y, tz - 1, y == ty + 8 ? Pal.GILDED : Pal.PB_CHISELED);
            if (y >= ty + 2) {
                c.set(tx - 1, y, tz - 1, y >= ty + 6 ? Pal.GILDED : Pal.PB_BRICKS);
                c.set(tx + 1, y, tz - 1, y >= ty + 6 ? Pal.GILDED : Pal.PB_BRICKS);
            }
        }
        c.set(tx, ty + 9, tz - 1, Pal.b(Blocks.WITHER_SKELETON_SKULL));
        c.set(tx - 1, ty + 7, tz - 1, Pal.b(Blocks.WITHER_SKELETON_SKULL));
        c.set(tx + 1, ty + 7, tz - 1, Pal.b(Blocks.WITHER_SKELETON_SKULL));
        int[] b = bossSpot();
        c.set(b[0], b[1] - 1, b[2], Pal.CRYING_OBSIDIAN);
        // grandes braseros
        BlockState pit = ModDeco.firePit();
        for (int k = 0; k < 8; k++) {
            double a = Math.PI / 8 + k * Math.PI / 4;
            int x = cx + (int) Math.round(Math.cos(a) * (HALL_R - 8.5)), z = cz + (int) Math.round(Math.sin(a) * (HALL_R - 8.5));
            c.set(x, F, z, Pal.PB_CHISELED);
            c.set(x, F + 1, z, Pal.PB_BRICKS);
            c.set(x, F + 2, z, pit != null ? pit : Pal.SOUL_CAMPFIRE);
        }
        // estandartes y apliques en las pilastras de los puntos cardinales (abajo y en el balcon)
        for (Direction out : dirs) {
            int px = cx + out.getStepX() * (HALL_R - 2), pz = cz + out.getStepZ() * (HALL_R - 2);
            Direction in = out.getOpposite();
            for (int alto : new int[]{9, 22}) {
                if (out == Direction.NORTH) continue; // el roseton (arriba) y el respaldo del trono (abajo)
                c.set(px, F + alto, pz, (out == Direction.NORTH ? Blocks.RED_WALL_BANNER : Blocks.BLACK_WALL_BANNER).defaultBlockState()
                        .setValue(WallBannerBlock.FACING, in));
            }
            Direction side = out.getClockWise();
            for (int s = -1; s <= 1; s += 2) {
                int lx = px + side.getStepX() * 2 * s, lz = pz + side.getStepZ() * 2 * s;
                BlockState l = ModDeco.wallLight(out, s + 4);
                c.set(lx + out.getStepX(), F + 4, lz + out.getStepZ(), Pal.BASALT);
                c.set(lx, F + 4, lz, l != null ? l : Blocks.SOUL_WALL_TORCH.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.WallTorchBlock.FACING, in));
            }
        }
        // lamparas de arana colgando de la cupula, entre las ramas del arbol
        for (int k = 0; k < 6; k++) {
            double a = Math.toRadians(60 * k);
            int x = cx + (int) Math.round(Math.cos(a) * 25), z = cz + (int) Math.round(Math.sin(a) * 25);
            int ceil = ceiling(Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz)));
            int bottom = F + 17;
            for (int y = ceil - 1; y > bottom; y--) c.set(x, y, z, Pal.CHAIN);
            BlockState ch = ModDeco.chandelier(true, false);
            if (ch != null) c.set(x, bottom, z, ch);
            else {
                c.set(x, bottom, z, Pal.SOUL_LANTERN_HANG);
                for (Direction d : dirs) {
                    c.set(x + d.getStepX(), bottom + 1, z + d.getStepZ(), Pal.CHAIN);
                    c.set(x + d.getStepX(), bottom, z + d.getStepZ(), Pal.LANTERN_HANG);
                }
            }
        }
        // farolas por el suelo de la sala (entre las raices y los braseros)
        for (int k = 0; k < 12; k++) {
            double a = Math.toRadians(15 + 30 * k);
            if (alNorte(a) < 0.3) continue;
            int x = cx + (int) Math.round(Math.cos(a) * 25.5), z = cz + (int) Math.round(Math.sin(a) * 25.5);
            c.set(x, F, z, Pal.PB_CHISELED);
            c.set(x, F + 1, z, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
            c.set(x, F + 2, z, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
            c.set(x, F + 3, z, Pal.LANTERN);
        }
        // apliques en todas las pilastras, debajo y encima del balcon
        for (int k = 0; k < 20; k++) {
            double a = k * Math.PI / 10;
            for (double rr = HALL_R - 3.5; rr <= HALL_R - 1; rr += 0.25) {
                int x = cx + (int) Math.round(Math.cos(a) * rr), z = cz + (int) Math.round(Math.sin(a) * rr);
                Direction mira = Pal.dirOf(cx - x, cz - z);
                int bx = x - mira.getStepX(), bz = z - mira.getStepZ();
                double db = Math.sqrt((bx - cx) * (bx - cx) + (bz - cz) * (bz - cz));
                if (db <= HALL_R + 0.5 - 2) continue;
                for (int alto : new int[]{5, 17}) {
                    if (alto == 17 && !sectorBalcon(a)) continue;
                    if (!c.isAir(x, F + alto, z)) continue;
                    BlockState l = ModDeco.wallLight(mira.getOpposite(), k + alto);
                    c.set(x, F + alto, z, l != null ? l : Blocks.WALL_TORCH.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.WallTorchBlock.FACING, mira));
                }
                break;
            }
        }
        // faroles colgando de la cupula por encima del balcon
        for (int k = 0; k < 16; k++) {
            double a = Math.toRadians(11.25 + 22.5 * k);
            if (!sectorBalcon(a)) continue;
            int x = cx + (int) Math.round(Math.cos(a) * 33), z = cz + (int) Math.round(Math.sin(a) * 33);
            int ceil = ceiling(Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz)));
            int bottom = F + BALCON + 7;
            for (int y = ceil - 1; y > bottom; y--) c.set(x, y, z, Pal.CHAIN);
            c.set(x, bottom, z, Pal.LANTERN_HANG);
        }
        // raices del arbol por el suelo (en relieve cerca del tronco, a ras mas lejos)
        Rng r = new Rng(seed + 81);
        for (int k = 0; k < 8; k++) {
            double a = Math.toRadians(10 + 45 * k) + (r.nextDouble() - 0.5) * 0.2;
            if (alNorte(a) < 0.45) continue;
            double len = 24 + r.nextDouble() * 6;
            for (double rr = TRUNK_R - 1; rr <= len; rr += 0.5) {
                double t = (rr - TRUNK_R) / (len - TRUNK_R);
                double wob = noise.value(k * 3.1, rr * 0.15, 0) * 2.0;
                double px = Math.cos(a) * rr - Math.sin(a) * wob, pz = Math.sin(a) * rr + Math.cos(a) * wob;
                double rad = 1.6 - t * 0.9;
                int ir = (int) Math.ceil(rad);
                for (int ox = -ir; ox <= ir; ox++)
                    for (int oz = -ir; oz <= ir; oz++) {
                        if (ox * ox + oz * oz > rad * rad) continue;
                        int x = cx + (int) Math.round(px) + ox, z = cz + (int) Math.round(pz) + oz;
                        c.set(x, F - 1, z, bark(x, F - 1, z, Direction.Axis.Y));
                        if (t < 0.35 && ox * ox + oz * oz <= (rad - 0.6) * (rad - 0.6)) c.set(x, F, z, bark(x, F, z, Direction.Axis.Y));
                    }
            }
        }
        // lianas por las pilastras del muro (arriba y abajo del balcon)
        for (int k = 0; k < 20; k++) {
            double a = k * Math.PI / 10;
            if (Hash.pct(k, 0, 0, seed + 91) < 40) continue;
            int x = cx + (int) Math.round(Math.cos(a) * (HALL_R - 2)), z = cz + (int) Math.round(Math.sin(a) * (HALL_R - 2));
            Direction to = Pal.dirOf(x - cx, z - cz);
            int len = 5 + Hash.pct(k, 1, 0, seed + 92) % 8;
            for (int top : new int[]{F + HALL_H - 1, F + BALCON - 2}) {
                for (int i = 0; i < len; i++) {
                    int y = top - i;
                    if (y < F + com.darkcraft.gavran.SalaArbol.MIN_LIANA) break;
                    if (!c.isAir(x, y, z) || c.isAir(x + to.getStepX(), y, z + to.getStepZ())) break;
                    c.set(x, y, z, Blocks.VINE.defaultBlockState().setValue(net.minecraft.world.level.block.VineBlock.getPropertyForFace(to), true));
                }
            }
        }
        // cofres del tesoro del jefe detras del trono
        c.chest(tx + 3, F + 2, tz - 1, Direction.SOUTH, Decor.loot("tesoro"));
        c.chest(tx - 3, F + 2, tz - 1, Direction.SOUTH, Decor.loot("tesoro"));
    }

    // ------------------------------------------------------------ ramas, copa y colgantes
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
                        if (!c.inXZ(x, z) || insideHall(x, y, z)) continue;
                        c.set(x, y, z, bark(x, y, z, axis));
                    }
        }
    }

    /** True si el bloque esta dentro del espacio interior de la sala del jefe (no se toca). */
    private boolean insideHall(int x, int y, int z) {
        double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
        return d <= HALL_R - 1.5 && y >= hallFloor - 1 && y <= ceiling(d) + 1;
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
            int start = Integer.MIN_VALUE;
            for (int y = h[1] + 12; y >= h[1] - 4; y--) {
                if (!c.isAir(h[0], y, h[2])) {
                    start = y - 1;
                    break;
                }
            }
            if (start == Integer.MIN_VALUE) continue;
            for (int k = 0; k < h[3]; k++) {
                int y = start - k;
                if (!c.isAir(h[0], y, h[2]) || insideHall(h[0], y, h[2])) break;
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
            rootSeg(c, sx, top + 7, sz, ex, top + 1, ez, 2.2);
            rootSeg(c, ex, top + 1, ez, ex + a[2], (top + groundY) / 2, ez + a[3], 1.7);
            rootSeg(c, ex + a[2], (top + groundY) / 2, ez + a[3], ex + a[2] * 3, groundY + 1, ez + a[3] * 3, 1.5);
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
                        boolean insideKeep = x > keep.x0 && x < keep.x1 && z > keep.z0 && z < keep.z1 && y < baseY - 1;
                        if (insideKeep) continue;
                        c.set(x, y, z, bark(x, y, z, Direction.Axis.Y));
                    }
        }
    }

    // ------------------------------------------------------------ aguja que atraviesa la copa
    private void spire(Canvas c) {
        int sb = domeTop - 1;
        for (int x = Math.max(cx - SPIRE_R - 1, c.minX); x <= Math.min(cx + SPIRE_R + 1, c.maxX); x++)
            for (int z = Math.max(cz - SPIRE_R - 1, c.minZ); z <= Math.min(cz + SPIRE_R + 1, c.maxZ); z++) {
                int m = GreatTower.oct(x - cx, z - cz);
                for (int y = sb; y <= sb + SPIRE_H; y++) {
                    double rad = SPIRE_R * (1 - (y - sb) / (double) SPIRE_H);
                    if (m <= rad + 0.5 && m > rad - 1.6) {
                        int rel = y - sb;
                        BlockState s = rel % 10 == 9 ? Pal.trim(x, y, z) : Pal.roof(x, y, z);
                        if (rel % 10 == 4 && Math.abs(Math.abs(x - cx) - Math.abs(z - cz)) == 0) s = Pal.GILDED;
                        c.set(x, y, z, s);
                    }
                }
            }
        int apex = sb + SPIRE_H;
        c.column(cx, cz, apex - 2, apex + 1, Pal.GILDED);
        c.column(cx, cz, apex + 2, apex + 4, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
        c.set(cx, apex + 5, cz, Pal.b(Blocks.LIGHTNING_ROD));
    }
}
