package com.darkcraft.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * La Fortaleza de Hueso: una fortaleza gotica pequena sobre las nubes, de la misma piedra que el castillo de Valdrenn
 * y armada con huesos. Murallas cuadradas con cuatro torres, la puerta de hueso (un rastrillo de huesos y barrotes que
 * sube al abrirse) bajo una calavera gigante y dos colmillos, el patio dentro de las costillas de una criatura
 * enorme, y la torre del homenaje con cuatro pisos, una escalera de caracol y, en lo alto, el arco del portal.
 * Medidas relativas al centro (cx, cz); Y es la altura que se pisa.
 */
public final class FortalezaHueso extends Element {
    public final int cx, cz, Y;
    /** Murallas: semilado de fuera, grosor, alto. */
    public static final int MURO = 30, GROSOR = 3, ALTO_MURO = 12;
    /** La torre del homenaje: x de -KX a KX, z de KZ0 a KZ1, pisos cada PISO bloques, NPISOS pisos y la azotea. */
    public static final int KX = 9, KZ0 = -24, KZ1 = -6, PISO = 10, NPISOS = 4;
    /** La puerta: ancho (de -PX a PX), alto, y la z de la reja (cara de fuera). */
    public static final int PX = 3, PALTO = 8, PZ = 33;
    /** El arco del portal en la azotea: de -AX a AX, alto, z. */
    public static final int AX = 3, AALTO = 9, AZ = -18;

    public FortalezaHueso(long seed, int cx, int cz, int Y) {
        super(seed);
        this.cx = cx;
        this.cz = cz;
        this.Y = Y;
        this.layer = 3;
        bounds(cx - 44, Y - 2, cz - 44, cx + 44, Y + NPISOS * PISO + AALTO + 12, cz + 46);
    }

    public static BlockState hueso(Direction.Axis a) {
        return Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, a);
    }

    public static final BlockState HUESO = hueso(Direction.Axis.Y);

    /** La piedra del castillo con hiladas de hueso. */
    private BlockState piedra(int x, int y, int z) {
        int ry = y - Y;
        if (ry % 7 == 3) return hueso(Direction.Axis.X);
        return Pal.wall(x, y, z);
    }

    // ------------------------------------------------------------------ posiciones utiles (para el resto del mod)

    /** El centro de la puerta por fuera (donde llega el puente). */
    public int[] rellano() {
        return new int[]{cx, Y, cz + PZ + 5};
    }

    /** La reja de la puerta: {x0, y0, z, x1, y1}. */
    public int[] reja() {
        return new int[]{cx - PX, Y, cz + PZ, cx + PX, Y + PALTO - 1};
    }

    /** El cofre del acertijo (planta baja, al fondo). */
    public int[] cofre() {
        return new int[]{cx, Y + 1, cz + KZ0 + 3};
    }

    /** El altar delante del portal (en la azotea). */
    public int[] altar() {
        return new int[]{cx, Y + NPISOS * PISO, cz + AZ + 3};
    }

    /** Hueco del portal: {x0, y0, z, x1, y1}. */
    public int[] portal() {
        int y = Y + NPISOS * PISO;
        return new int[]{cx - AX + 1, y, cz + AZ, cx + AX - 1, y + AALTO - 2};
    }

    /** Atriles con los libros perdidos de la fortaleza: {x, y, z, libro, mira (0 S, 1 N, 2 E, 3 O)}. */
    public int[][] atriles() {
        int t = Y + NPISOS * PISO;
        return new int[][]{
                {cx + 4, Y + PALTO + 1, cz + 30, 6, 1},     // cuerpo de guardia de la puerta (encima del paso)
                {cx - 6, Y, cz + KZ0 + 4, 11, 2},          // planta baja
                {cx - 6, Y + PISO, cz + KZ0 + 4, 7, 2},    // piso 1
                {cx - 6, Y + 2 * PISO, cz + KZ0 + 4, 8, 2}, // piso 2
                {cx - 6, Y + 3 * PISO, cz + KZ0 + 4, 9, 2}, // piso 3
                {cx + 5, t, cz + AZ + 5, 10, 3},           // azotea, cerca del altar
        };
    }

    /** Los ojos de la calavera de la puerta (se encienden al abrirla): {x, y, z} de cada bloque. */
    public int[][] ojos() {
        int fz = cz + PZ + 2, fy = Y + PALTO + 1;
        int[][] o = new int[8][];
        int k = 0;
        for (int j = 3; j <= 4; j++)
            for (int i : new int[]{1, 2, 6, 7}) o[k++] = new int[]{cx - 4 + i, fy + 7 - j, fz};
        return o;
    }

    public boolean dentroMuros(double x, double z) {
        return Math.abs(x - cx) <= MURO + 0.5 && Math.abs(z - cz) <= MURO + 0.5;
    }

    // ------------------------------------------------------------------ construir

    @Override
    public void build(Canvas c) {
        patio(c);
        costillas(c);
        murallas(c);
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2) torre(c, cx + sx * MURO, cz + sz * MURO);
        puerta(c);
        homenaje(c);
        azotea(c);
        escalera(c);
        for (int[] a : atriles()) {
            Direction mira = switch (a[4]) {
                case 0 -> Direction.SOUTH;
                case 1 -> Direction.NORTH;
                case 2 -> Direction.EAST;
                default -> Direction.WEST;
            };
            c.lectern(a[0], a[1], a[2], mira, com.darkcraft.fortaleza.Perdidos.libro(a[3]));
        }
        luces(c);
    }

    /** Luz invisible (bloque de luz). */
    private static BlockState luz(int nivel) {
        return Blocks.LIGHT.defaultBlockState().setValue(net.minecraft.world.level.block.LightBlock.LEVEL, nivel);
    }

    private void luzSi(Canvas c, int x, int y, int z, int nivel) {
        if (c.isAir(x, y, z)) c.set(x, y, z, luz(nivel));
    }

    /**
     * Para que dentro se vea (y no se queden a oscuras): luz invisible repartida por cada piso de la torre del homenaje,
     * las torres y la puerta. Lo que se ve son los faroles y las antorchas de almas.
     */
    private void luces(Canvas c) {
        for (int p = 0; p < NPISOS; p++) {
            int yp = Y + p * PISO;
            for (int i = -1; i <= 1; i++)
                for (int z : new int[]{cz + KZ0 + 4, cz + KZ0 + 9, cz + KZ1 - 4})
                    luzSi(c, cx + i * 5, yp + 3, z, 10);
        }
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2) {
                int tx = cx + sx * MURO, tz = cz + sz * MURO;
                luzSi(c, tx, Y + 3, tz, 10);
                luzSi(c, tx, Y + ALTO_MURO + 4, tz, 10);
            }
        luzSi(c, cx, Y + PALTO + 4, cz + PZ - 4, 10);
        luzSi(c, cx, Y + 5, cz + PZ - 4, 9);
        luzSi(c, cx, Y + 5, cz + PZ + 3, 8);
    }

    private void patio(Canvas c) {
        for (int x = cx - MURO + GROSOR; x <= cx + MURO - GROSOR; x++)
            for (int z = cz - MURO + GROSOR; z <= cz + MURO - GROSOR; z++) {
                int dx = x - cx, dz = z - cz;
                BlockState s;
                if (Math.abs(dx) <= 1 && dz > KZ1) s = Pal.b(Blocks.BLACKSTONE);
                else if ((Math.abs(dx) == 2 && dz > KZ1) || ((dx + dz) & 7) == 0 && Hash.pct(x, Y, z, seed) < 60) s = HUESO;
                else s = ((Math.floorDiv(x, 2) + Math.floorDiv(z, 2)) & 1) == 0 ? Pal.PB_BRICKS : Pal.DS_TILES;
                c.set(x, Y - 1, z, s);
                for (int y = Y; y <= Y + 30; y++) if (!c.isAir(x, y, z) && y < Y + 3) c.set(x, y, z, Pal.AIR);
            }
        // montones de huesos y calaveras por el patio
        for (int k = 0; k < 18; k++) {
            int x = cx + (Hash.h(k, 1, 0, seed) % (2 * MURO - 10)) - (MURO - 5);
            int z = cz + (Hash.h(k, 2, 0, seed) % (2 * MURO - 10)) - (MURO - 5);
            int dx = x - cx, dz = z - cz;
            if (Math.abs(dx) <= 4 || (Math.abs(dx) <= KX + 1 && dz >= KZ0 - 1 && dz <= KZ1 + 1)) continue;
            c.set(x, Y, z, Hash.pct(k, 3, 0, seed) < 50 ? Pal.b(Blocks.SKELETON_SKULL) : hueso(Direction.Axis.X));
        }
        // braseros de fuego de almas a los lados del camino
        for (int dz = KZ1 + 6; dz <= MURO - 8; dz += 8)
            for (int s = -1; s <= 1; s += 2) {
                int x = cx + s * 5, z = cz + dz;
                c.set(x, Y, z, Pal.PB_CHISELED);
                c.set(x, Y + 1, z, Pal.SOUL_CAMPFIRE);
            }
    }

    /** Las costillas de una criatura gigante que cruzan el patio de muro a muro. */
    private void costillas(Canvas c) {
        int R = MURO - GROSOR;
        for (int dz = KZ1 + 4; dz <= MURO - 6; dz += 7) {
            int z = cz + dz;
            for (int i = 0; i <= 180; i++) {
                double a = Math.PI * i / 180.0;
                double x = cx + Math.cos(a) * (R + 1), y = Y + Math.sin(a) * 26;
                int bx = (int) Math.round(x), by = (int) Math.round(y);
                Direction.Axis ax = Math.abs(Math.cos(a)) > 0.7 ? Direction.Axis.Y : Direction.Axis.X;
                for (int t = 0; t <= 1; t++) c.set(bx, by + t, z, hueso(ax));
            }
            // farol colgando de lo alto de cada costilla
            for (int y = Y + 25; y > Y + 20; y--) c.set(cx, y, z, Pal.CHAIN);
            c.set(cx, Y + 20, z, Pal.SOUL_LANTERN_HANG);
        }
    }

    private void murallas(Canvas c) {
        for (int x = cx - MURO; x <= cx + MURO; x++)
            for (int z = cz - MURO; z <= cz + MURO; z++) {
                int dx = x - cx, dz = z - cz;
                int m = Math.max(Math.abs(dx), Math.abs(dz));
                if (m < MURO - GROSOR + 1 || m > MURO) continue;
                boolean fuera = m == MURO;
                for (int y = Y - 1; y <= Y + ALTO_MURO; y++) c.set(x, y, z, piedra(x, y, z));
                // almenas por fuera
                if (fuera && ((Math.abs(dx) + Math.abs(dz)) & 1) == 0) c.set(x, Y + ALTO_MURO + 1, z, Pal.wall(x, Y + ALTO_MURO + 1, z));
                // costillas de hueso que suben por la cara de fuera cada 6 bloques
                if (fuera && ((Math.abs(dx) == MURO && dz % 6 == 0) || (Math.abs(dz) == MURO && dx % 6 == 0)) && m == MURO) {
                    int ox = Math.abs(dx) == MURO ? Integer.signum(dx) : 0, oz = Math.abs(dz) == MURO ? Integer.signum(dz) : 0;
                    for (int y = Y - 1; y <= Y + ALTO_MURO - 1; y++) c.set(x + ox, y, z + oz, HUESO);
                    c.set(x + ox, Y + ALTO_MURO, z + oz, Pal.b(Blocks.SKELETON_SKULL));
                }
            }
        // farolas en el camino de ronda
        for (int k = -MURO + 6; k <= MURO - 6; k += 12) {
            for (int[] p : new int[][]{{k, -MURO + 2}, {k, MURO - 2}, {-MURO + 2, k}, {MURO - 2, k}}) {
                if (Math.abs(p[0]) <= PX + 4 && p[1] == MURO - 2) continue;
                c.set(cx + p[0], Y + ALTO_MURO + 1, cz + p[1], Pal.SOUL_LANTERN);
            }
        }
    }

    private void torre(Canvas c, int tx, int tz) {
        int r = 5, alto = 24;
        for (int x = tx - r - 1; x <= tx + r + 1; x++)
            for (int z = tz - r - 1; z <= tz + r + 1; z++) {
                double d = Math.hypot(x - tx, z - tz);
                if (d > r + 0.5) continue;
                boolean borde = d > r - 1.2;
                for (int y = Y - 1; y <= Y + alto; y++) {
                    int ry = y - Y;
                    if (borde) c.set(x, y, z, piedra(x, y, z));
                    else if (y == Y - 1 || ry == ALTO_MURO || ry == alto - 1) c.set(x, y, z, Pal.PB_BRICKS);
                    else c.set(x, y, z, Pal.AIR);
                }
                if (borde && ((x + z) & 1) == 0) c.set(x, Y + alto + 1, z, Pal.wall(x, Y + alto + 1, z));
                // aspilleras
                if (borde && d > r - 0.6) {
                    double a = Math.atan2(z - tz, x - tx);
                    if (Math.abs(Math.sin(a * 2)) < 0.15)
                        for (int y = Y + 9; y <= Y + 10; y++) c.set(x, y, z, Pal.b(Blocks.BLACK_STAINED_GLASS_PANE));
                }
            }
        // escalera de mano por dentro (pared del lado del centro) y huecos en los pisos
        int ox = Integer.signum(cx - tx), oz = Integer.signum(cz - tz);
        int lx = tx + ox * 3, lz = tz;
        Direction mira = ox > 0 ? Direction.WEST : Direction.EAST;
        for (int y = Y; y <= Y + alto; y++) c.set(lx, y, lz, Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, mira));
        // puerta desde el patio (en la diagonal, hacia el centro) y desde el camino de ronda
        for (int y = Y; y <= Y + 1; y++) c.set(tx + ox * 3, y, tz + oz * 3, Pal.AIR);
        for (int y = Y + ALTO_MURO + 1; y <= Y + ALTO_MURO + 2; y++) {
            c.set(tx + ox * r, y, tz, Pal.AIR);
            c.set(tx, y, tz + oz * r, Pal.AIR);
        }
        // colmillos de hueso arriba
        for (int k = 0; k < 4; k++) {
            double a = Math.PI / 4 + k * Math.PI / 2;
            for (int i = 0; i < 7; i++) {
                double rr = r + 0.5 + i * 0.35;
                int x = tx + (int) Math.round(Math.cos(a) * rr), z = tz + (int) Math.round(Math.sin(a) * rr);
                c.set(x, Y + alto + 1 + i, z, i == 6 ? Pal.b(Blocks.END_ROD) : HUESO);
            }
        }
        c.set(tx, Y + alto + 1, tz, Pal.SOUL_CAMPFIRE);
        // un farol de almas colgando en cada planta de dentro
        c.set(tx, Y + ALTO_MURO - 1, tz, Pal.CHAIN);
        c.set(tx, Y + ALTO_MURO - 2, tz, Pal.SOUL_LANTERN_HANG);
        c.set(tx, Y + alto - 2, tz, Pal.CHAIN);
        c.set(tx, Y + alto - 3, tz, Pal.SOUL_LANTERN_HANG);
    }

    private void puerta(Canvas c) {
        int z0 = cz + MURO - GROSOR - 1, z1 = cz + PZ + 1;
        // el cuerpo de la puerta
        for (int x = cx - PX - 4; x <= cx + PX + 4; x++)
            for (int z = z0; z <= z1; z++)
                for (int y = Y - 1; y <= Y + PALTO + 9; y++) {
                    int dx = Math.abs(x - cx);
                    boolean paso = dx <= PX && y >= Y && y < Y + PALTO;
                    boolean hueco = dx <= PX && z == cz + PZ && y >= Y + PALTO && y < Y + 2 * PALTO; // donde sube la reja
                    boolean sala = dx <= PX + 2 && z >= z0 + 1 && z <= z1 - 2 && y >= Y + PALTO + 1 && y <= Y + PALTO + 6;
                    if (paso || hueco || sala) c.set(x, y, z, Pal.AIR);
                    else c.set(x, y, z, y == Y - 1 ? Pal.PB_BRICKS : piedra(x, y, z));
                }
        // suelo del paso
        for (int x = cx - PX; x <= cx + PX; x++) for (int z = z0; z <= z1; z++) c.set(x, Y - 1, z, Pal.b(Blocks.BLACKSTONE));
        // la reja cerrada
        cerrarReja(c);
        // sala de la guardia encima del paso (con su atril) y la escalera de mano para subir desde el patio
        for (int y = Y; y <= Y + PALTO; y++) c.set(cx + PX + 2, y, z0 + 1, Blocks.LADDER.defaultBlockState()
                .setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.NORTH));
        c.set(cx + PX + 2, Y, z0, Pal.AIR);
        c.set(cx + PX + 2, Y + 1, z0, Pal.AIR);
        c.set(cx - PX - 1, Y + PALTO + 3, cz + PZ - 2, Pal.SOUL_LANTERN);
        // rellano de fuera para el puente
        for (int x = cx - PX - 2; x <= cx + PX + 2; x++)
            for (int z = z1 + 1; z <= z1 + 5; z++) {
                c.set(x, Y - 1, z, Math.abs(x - cx) <= PX ? Pal.b(Blocks.BLACKSTONE) : Pal.PB_BRICKS);
                for (int y = Y - 6; y < Y - 1; y++) if (c.isAir(x, y, z)) c.set(x, y, z, Pal.wall(x, y, z));
            }
        for (int s = -1; s <= 1; s += 2) {
            int x = cx + s * (PX + 2);
            for (int z = z1 + 1; z <= z1 + 5; z += 2) {
                c.set(x, Y, z, HUESO);
                c.set(x, Y + 1, z, Pal.b(Blocks.SKELETON_SKULL));
            }
        }
        // la calavera gigante encima de la puerta (cara de fuera)
        int fz = z1 + 1, fy = Y + PALTO + 1;
        String[] cara = {
                "..HHHHH..",
                ".HHHHHHH.",
                "HHHHHHHHH",
                "HOOHHHOOH",
                "HOOHHHOOH",
                "HHHHNHHHH",
                ".HHHHHHH.",
                "..HDHDH..",
        };
        for (int j = 0; j < cara.length; j++)
            for (int i = 0; i < 9; i++) {
                char ch = cara[j].charAt(i);
                int x = cx - 4 + i, y = fy + cara.length - 1 - j;
                BlockState s = switch (ch) {
                    case 'H' -> HUESO;
                    case 'O' -> Pal.CRYING_OBSIDIAN;
                    case 'N' -> Pal.b(Blocks.BLACKSTONE);
                    case 'D' -> Pal.b(Blocks.BONE_BLOCK);
                    default -> null;
                };
                if (s != null) c.set(x, y, fz, s);
            }
        // dos colmillos que salen a los lados de la puerta
        for (int s = -1; s <= 1; s += 2)
            for (int i = 0; i <= 40; i++) {
                double t = i / 40.0;
                double x = cx + s * (PX + 5 + t * 5), y = Y - 1 + Math.sin(t * Math.PI * 0.62) * 16, z = z1 + 2 + t * 3.5;
                int g = t < 0.6 ? 1 : 0;
                for (int a = -g; a <= g; a++)
                    for (int b = -g; b <= g; b++) c.set((int) Math.round(x) + a, (int) Math.round(y), (int) Math.round(z) + b, HUESO);
            }
        // estandartes rojos a los lados de la reja, por dentro
        for (int s = -1; s <= 1; s += 2)
            c.set(cx + s * (PX + 1), Y + 5, z0, Blocks.RED_WALL_BANNER.defaultBlockState().setValue(WallBannerBlock.FACING, Direction.NORTH));
    }

    /** La reja de hueso (irrompible: solo la sube la llave). */
    public static BlockState reja(int dx, int ry) {
        return com.darkcraft.DarkCraftMod.REJA_HUESO.get().defaultBlockState();
    }

    void cerrarReja(Canvas c) {
        for (int x = cx - PX; x <= cx + PX; x++)
            for (int y = Y; y < Y + PALTO; y++) c.set(x, y, cz + PZ, reja(x - cx + PX, y - Y));
    }

    private void homenaje(Canvas c) {
        int x0 = cx - KX, x1 = cx + KX, z0 = cz + KZ0, z1 = cz + KZ1, top = Y + NPISOS * PISO;
        for (int x = x0; x <= x1; x++)
            for (int z = z0; z <= z1; z++) {
                boolean muro = x <= x0 + 1 || x >= x1 - 1 || z <= z0 + 1 || z >= z1 - 1;
                for (int y = Y - 1; y <= top; y++) {
                    int ry = y - Y;
                    boolean suelo = (ry + 1) % PISO == 0 || y == Y - 1;
                    if (muro) {
                        BlockState s = piedra(x, y, z);
                        // ventanas ojivales de cristal negro y rojo en cada piso
                        boolean cara = x == x0 || x == x1 || z == z0 || z == z1;
                        int lado = (x == x0 || x == x1) ? z : x;
                        int pr = Math.floorMod(ry, PISO);
                        if (cara && Math.floorMod(lado, 5) == 2 && pr >= 3 && pr <= 6)
                            s = pr == 6 ? Pal.b(Blocks.RED_STAINED_GLASS) : Pal.b(Blocks.BLACK_STAINED_GLASS);
                        c.set(x, y, z, s);
                    } else c.set(x, y, z, suelo ? (y == Y - 1 ? Pal.PB_BRICKS : Pal.b(Blocks.DARK_OAK_PLANKS)) : Pal.AIR);
                }
            }
        // pilares de hueso en las esquinas de fuera
        for (int[] e : new int[][]{{x0 - 1, z0 - 1}, {x1 + 1, z0 - 1}, {x0 - 1, z1 + 1}, {x1 + 1, z1 + 1}})
            for (int y = Y - 1; y <= top + 3; y++) c.set(e[0], y, e[1], y > top ? Pal.b(Blocks.END_ROD) : HUESO);
        // la entrada (al sur, al patio)
        for (int x = cx - 1; x <= cx + 1; x++)
            for (int y = Y; y <= Y + 4; y++)
                for (int z = z1 - 1; z <= z1; z++) c.set(x, y, z, (y == Y + 4 && x != cx) ? Pal.PB_CHISELED : Pal.AIR);
        // planta baja: alfombra, columnas, el cofre del acertijo en su tarima y los estandartes
        for (int z = z0 + 2; z <= z1 - 2; z++) for (int x = cx - 1; x <= cx + 1; x++)
            c.set(x, Y, z, (x == cx ? Blocks.RED_CARPET : Blocks.BLACK_CARPET).defaultBlockState());
        for (int z = z0 + 5; z <= z1 - 3; z += 5)
            for (int s = -1; s <= 1; s += 2) {
                if (enEscalera(cx + s * 4, z)) continue; // ahi sube la escalera de caracol: una columna la atascaba
                for (int y = Y; y < Y + PISO - 1; y++) c.set(cx + s * 4, y, z, y == Y || y == Y + PISO - 2 ? Pal.PB_CHISELED : HUESO);
            }
        int[] cf = cofre();
        for (int x = cx - 2; x <= cx + 2; x++) c.set(x, Y, cf[2], Pal.PB_BRICKS);
        c.set(cx - 2, Y + 1, cf[2], Pal.SOUL_LANTERN);
        c.set(cx + 2, Y + 1, cf[2], Pal.SOUL_LANTERN);
        c.chest(cf[0], cf[1], cf[2], Direction.SOUTH, Decor.loot("acertijo"));
        // la mesa donde se forja el amuleto (en modo aventura no se puede poner una)
        c.set(cx + 1, Y + 1, cf[2], Pal.b(Blocks.CRAFTING_TABLE));
        for (int s = -1; s <= 1; s += 2)
            c.set(cx + s * 3, Y + 4, z0 + 2, Blocks.BLACK_WALL_BANNER.defaultBlockState().setValue(WallBannerBlock.FACING, Direction.SOUTH));
        // faroles colgando en cada piso, a media altura (desde el techo no llegaba la luz al suelo)
        for (int p = 0; p < NPISOS; p++) {
            int yTecho = Y + (p + 1) * PISO - 2;
            for (int[] f : new int[][]{{cx - 4, cz - 10}, {cx + 1, cz - 18}, {cx - 4, cz - 20}}) {
                for (int y = yTecho; y > yTecho - 3; y--) c.set(f[0], y, f[1], Pal.CHAIN);
                c.set(f[0], yTecho - 3, f[1], Pal.SOUL_LANTERN_HANG);
            }
        }
        // antorchas de almas en las columnas de la planta baja, mirando a la alfombra
        for (int z = z0 + 5; z <= z1 - 3; z += 5)
            for (int s = -1; s <= 1; s += 2) {
                if (enEscalera(cx + s * 4, z)) continue;
                c.set(cx + s * 3, Y + 3, z, Blocks.SOUL_WALL_TORCH.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.WallTorchBlock.FACING, s < 0 ? Direction.EAST : Direction.WEST));
            }
        // piso 1: el cuartel (literas de hueso y armas)
        int y1 = Y + PISO;
        for (int z = cz - 16; z <= z1 - 3; z += 3) {
            c.set(cx - 3, y1, z, HUESO);
            c.set(cx - 3, y1 + 1, z, Pal.slab(Blocks.DARK_OAK_SLAB, false));
            c.set(cx + 2, y1, z, Pal.b(Blocks.BARREL));
        }
        // piso 2: la biblioteca del maestre
        int y2 = Y + 2 * PISO;
        for (int x = x0 + 2; x <= x1 - 2; x++)
            for (int y = y2; y <= y2 + 3; y++) if (x != cx && x != cx - 1 && x != cx + 1) c.set(x, y, z1 - 2, Pal.b(Blocks.BOOKSHELF));
        c.set(cx + 2, y2, cz + KZ0 + 6, Pal.b(Blocks.CARTOGRAPHY_TABLE));
        // piso 3: la alcoba de la Reina (cama de hueso, espejo negro)
        int y3 = Y + 3 * PISO;
        for (int x = cx - 1; x <= cx + 1; x++) c.set(x, y3, z1 - 3, Pal.b(Blocks.WHITE_WOOL));
        for (int x = cx - 2; x <= cx + 2; x++) for (int y = y3 + 1; y <= y3 + 5; y++) c.set(x, y, z1 - 2, x == cx - 2 || x == cx + 2 || y == y3 + 5 ? HUESO : Pal.b(Blocks.BLACK_STAINED_GLASS));
    }

    /** Si (x, z) cae en la escalera de caracol (su pilar o el anillo de escalones). */
    private boolean enEscalera(int x, int z) {
        return Math.abs(x - (cx + KX - 4)) <= 1 && Math.abs(z - (cz + KZ0 + 4)) <= 1;
    }

    private static final int[][] ANILLO = {{-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}};

    /**
     * Arreglo para las fortalezas levantadas antes (la escalera se atascaba): quita la columna de hueso que caia sobre
     * un escalon (y su antorcha), deja tres bloques libres sobre cada escalon y abre la valla por los pasillos pegados
     * al muro, que encerraba a quien se bajaba en un piso. No toca nada mas, y si ya esta bien no cambia nada.
     */
    public void arreglarEscalera(Canvas c) {
        int px = cx + KX - 4, pz = cz + KZ0 + 4, top = Y + NPISOS * PISO;
        // la columna (cx + 4, cz + KZ0 + 5) y su antorcha
        int colX = cx + 4, colZ = cz + KZ0 + 5;
        if (enEscalera(colX, colZ)) {
            for (int y = Y; y < Y + PISO - 1; y++) {
                BlockState b = c.get(colX, y, colZ);
                if (b.is(Blocks.BONE_BLOCK) || b.is(Blocks.CHISELED_POLISHED_BLACKSTONE)) c.set(colX, y, colZ, Pal.AIR);
            }
            if (c.get(cx + 3, Y + 3, colZ).is(Blocks.SOUL_WALL_TORCH)) c.set(cx + 3, Y + 3, colZ, Pal.AIR);
        }
        despejarEscalera(c, px, pz, top);
        vallasEscalera(c, px, pz, true);
    }

    /** Tres bloques libres encima de cada escalon (menos otros escalones, que no los hay tan cerca). */
    private void despejarEscalera(Canvas c, int px, int pz, int top) {
        for (int k = 0; k < top - Y; k++) {
            int[] a = ANILLO[k % 8];
            for (int y = Y + k + 1; y <= Y + k + 3; y++) {
                BlockState b = c.get(px + a[0], y, pz + a[1]);
                if (!b.isAir() && !b.is(Blocks.LIGHT) && !b.is(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS)) c.set(px + a[0], y, pz + a[1], Pal.AIR);
            }
        }
    }

    /**
     * La barandilla alrededor del hueco de la escalera en cada piso, menos por donde se sale y menos en los pasillos
     * pegados a los muros norte y este (ahi no hay donde caerse, y la valla encerraba a quien se bajaba de la escalera).
     */
    private void vallasEscalera(Canvas c, int px, int pz, boolean soloQuitar) {
        for (int p = 1; p <= NPISOS; p++) {
            int[] sale = ANILLO[(p * PISO - 1) % 8];
            int ex = px + 2 * sale[0], ez = pz + 2 * sale[1];
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != 2) continue;
                    int x = px + dx, z = pz + dz;
                    boolean libre = Math.abs(x - ex) + Math.abs(z - ez) <= 1 || x >= cx + KX - 2 || z <= cz + KZ0 + 2;
                    if (libre) {
                        if (c.get(x, Y + p * PISO, z).is(Blocks.DARK_OAK_FENCE)) c.set(x, Y + p * PISO, z, Pal.AIR);
                    } else if (!soloQuitar) c.set(x, Y + p * PISO, z, Pal.connected(Blocks.DARK_OAK_FENCE, Direction.Axis.X));
                }
        }
    }

    /** Escalera de caracol en la esquina noreste de dentro de la torre, del suelo a la azotea. */
    private void escalera(Canvas c) {
        int px = cx + KX - 4, pz = cz + KZ0 + 4; // el pilar
        int[][] anillo = ANILLO;
        Direction[] mira = {Direction.EAST, Direction.EAST, Direction.SOUTH, Direction.SOUTH, Direction.WEST, Direction.WEST, Direction.NORTH, Direction.NORTH};
        int top = Y + NPISOS * PISO;
        // hueco en cada suelo (y en la azotea) y barandilla alrededor
        for (int p = 1; p <= NPISOS; p++) {
            int ys = Y + p * PISO - 1;
            for (int[] a : anillo) c.set(px + a[0], ys, pz + a[1], Pal.AIR);
        }
        for (int y = Y; y < top; y++) c.set(px, y, pz, Pal.PB_CHISELED);
        c.set(px, top, pz, Pal.SOUL_LANTERN);
        for (int k = 0; k < top - Y; k++) {
            int[] a = anillo[k % 8];
            c.set(px + a[0], Y + k, pz + a[1], Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, mira[k % 8], false));
        }
        despejarEscalera(c, px, pz, top);
        vallasEscalera(c, px, pz, false);
    }

    private void azotea(Canvas c) {
        int t = Y + NPISOS * PISO; // se pisa en t+1? no: el suelo de la azotea esta en t-1 y se pisa en t
        int x0 = cx - KX, x1 = cx + KX, z0 = cz + KZ0, z1 = cz + KZ1;
        for (int x = x0; x <= x1; x++)
            for (int z = z0; z <= z1; z++) {
                boolean borde = x == x0 || x == x1 || z == z0 || z == z1;
                for (int y = t; y <= t + AALTO + 4; y++) c.set(x, y, z, Pal.AIR);
                c.set(x, t - 1, z, ((x + z) & 1) == 0 ? Pal.PB_BRICKS : Pal.DS_TILES);
                if (borde) {
                    c.set(x, t, z, Pal.wall(x, t, z));
                    if (((x + z) & 1) == 0) c.set(x, t + 1, z, Pal.wall(x, t + 1, z));
                }
            }
        // el arco del portal: hueso y obsidiana llorosa, con cadenas
        int z = cz + AZ;
        for (int x = cx - AX; x <= cx + AX; x++)
            for (int y = t; y < t + AALTO; y++) {
                int dx = Math.abs(x - cx);
                boolean hueco = dx < AX && y < t + AALTO - 1;
                // el remate en punta
                if (y >= t + AALTO - 3 && dx == AX) continue;
                if (hueco) c.set(x, y, z, Pal.AIR);
                else c.set(x, y, z, ((y - t) % 3 == 1 || dx == 0) ? Pal.CRYING_OBSIDIAN : HUESO);
            }
        for (int dx = -AX + 1; dx <= AX - 1; dx++) c.set(cx + dx, t + AALTO, z, Math.abs(dx) <= 1 ? HUESO : Pal.AIR);
        c.set(cx, t + AALTO + 1, z, Pal.b(Blocks.WITHER_SKELETON_SKULL));
        for (int s = -1; s <= 1; s += 2) for (int y = t; y < t + 6; y++) c.set(cx + s * (AX + 1), y, z, y == t + 5 ? Pal.SOUL_LANTERN : Pal.CHAIN);
        // el altar delante del arco
        int[] al = altar();
        c.set(al[0], al[1], al[2], Pal.PB_CHISELED);
        c.set(al[0], al[1] + 1, al[2], Pal.GILDED);
        // dos candeleros de hueso con farol de almas a los lados (el fuego de verdad echaba humo negro delante del portal)
        for (int s = -1; s <= 1; s += 2) {
            c.set(al[0] + s * 2, al[1], al[2], Pal.PB_CHISELED);
            c.set(al[0] + s * 2, al[1] + 1, al[2], HUESO);
            c.set(al[0] + s * 2, al[1] + 2, al[2], Pal.SOUL_LANTERN);
        }
    }
}
