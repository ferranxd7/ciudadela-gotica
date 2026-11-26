package com.darkcraft.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * La Fortaleza de Hueso rehecha (parte 5a; respuestas 27-45 del plan): una torre gotica de ~70 bloques en el centro de
 * la isla, con su patio alrededor y todo bajo una <b>cupula de cristal ahumado</b> enorme (paneles rojos, nervios de
 * hueso y de hierro negro) que la torre atraviesa; la cupula esta rota en parte (le faltan paneles, hay cristales por el
 * patio y por los huecos entran la lluvia y las enredaderas). Fuera, un anillo bajo de murallas en ruinas con tramos
 * caidos y torrecillas rotas, y la puerta: un arco gotico alto entre dos estatuas de caballeros encapuchados, con la reja
 * de hueso. Dentro de la cupula, un jardin muerto (arboles secos, rosales marchitos, una fuente rota, bancos) y
 * estanques con nenufares donde cae la lluvia. La torre (medio derrumbada por dentro, pero se recorre): mazmorras en el
 * sotano, la sala de guardias y la armeria en la planta baja, la capilla, dos plantas de aposentos, una sala en ruinas
 * sobre la cupula y el observatorio; arriba, la terraza del portal a cielo abierto. Se sube por la escalera de caracol de
 * la torrecilla del este (todas las plantas) o por el ascensor del centro (de la planta baja a la terraza).
 * <p>
 * Medidas relativas al centro (cx, cz); Y es la altura que se pisa en el patio.
 */
public final class TorreCupula extends Element {
    public final int cx, cz, Y;

    /** El anillo de murallas: radio del centro del muro, grosor (a cada lado, 1,5) y alto. */
    public static final int RM = 44, ALTO_MURO = 9;
    /** La cupula: semiesfera de radio RD con el centro en el suelo del patio. */
    public static final int RD = 40;
    /** La torre: octogono de apotema AT, muros de GT; plantas cada PISO desde Y (0..NPISOS-1) y la terraza en Y + TERRAZA. */
    public static final int AT = 10, GT = 2, PISO = 10, NPISOS = 6, TERRAZA = 60;
    /** El sotano (las mazmorras): se pisa en Y + SOTANO (una planta por debajo: la escalera llega a todas). */
    public static final int SOTANO = -10;
    /** La torrecilla de la escalera, al este de la torre: centro (cx + TX, cz) y radio. */
    public static final int TX = 12;
    public static final double RTU = 3.6;
    /** El ascensor: tubo en el centro de la torre, de -ASC a ASC. */
    public static final int ASC = 2;
    /** La puerta: medio ancho del hueco, alto (con la punta), z de la reja y de la cara de fuera del cuerpo de la puerta. */
    public static final int PX = 3, PALTO = 13, PZ = RM + 1, PFUERA = RM + 4;
    /** El portal (parte 5d) en la terraza: medio ancho del hueco, alto, z del plano. */
    public static final int AX = 4, AALTO = 13, AZ = -11;
    /** La terraza: medio lado del cuadrado. */
    public static final int TL = 13;

    public TorreCupula(long seed, int cx, int cz, int Y) {
        super(seed);
        this.cx = cx;
        this.cz = cz;
        this.Y = Y;
        this.layer = 3;
        bounds(cx - RM - 12, Y + SOTANO - 2, cz - RM - 12, cx + RM + 12, Y + TERRAZA + AALTO + 14, cz + RM + 12);
    }

    // ------------------------------------------------------------------ bloques

    public static BlockState hueso(Direction.Axis a) {
        return Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, a);
    }

    public static final BlockState HUESO = hueso(Direction.Axis.Y);
    private static final BlockState HIERRO = Pal.b(Blocks.POLISHED_BASALT);
    private static final BlockState AHUMADO = Pal.GLASS_TINTED;

    /** La reja de hueso (en el mundo de mentira de las vistas no hay bloques del mod: barrotes). */
    public static BlockState rejaHueso() {
        try {
            return com.darkcraft.DarkCraftMod.REJA_HUESO.get().defaultBlockState();
        } catch (Throwable t) {
            return Pal.IRON_BARS;
        }
    }

    /** La piedra de sombra con runas rojas del marco del portal (parte 5e); en las vistas (sin bloques del mod), obsidiana. */
    public static BlockState piedraSombra() {
        try {
            return com.darkcraft.DarkCraftMod.PIEDRA_SOMBRA.get().defaultBlockState();
        } catch (Throwable t) {
            return Pal.b(Blocks.OBSIDIAN);
        }
    }

    /** El hueso labrado (el anillo de dentro del arco del portal). */
    public static BlockState huesoLabrado(Direction.Axis a) {
        try {
            return com.darkcraft.DarkCraftMod.HUESO_LABRADO.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, a);
        } catch (Throwable t) {
            return hueso(a);
        }
    }

    /** El hierro negro (los zocalos y capiteles del portal). */
    public static BlockState hierroNegro() {
        try {
            return com.darkcraft.DarkCraftMod.HIERRO_NEGRO.get().defaultBlockState();
        } catch (Throwable t) {
            return Pal.b(Blocks.NETHERITE_BLOCK);
        }
    }

    /** Una gema roja del portal, apagada o encendida. */
    public static BlockState gema(boolean luz) {
        try {
            return com.darkcraft.DarkCraftMod.GEMA_ROJA.get().defaultBlockState().setValue(com.darkcraft.fortaleza.GemaBlock.LUZ, luz);
        } catch (Throwable t) {
            return Pal.b(luz ? Blocks.SHROOMLIGHT : Blocks.REDSTONE_BLOCK);
        }
    }

    /** La piedra de Valdrenn con hiladas de hueso y algo de musgo abajo. */
    private BlockState piedra(int x, int y, int z) {
        int ry = y - Y;
        if (Math.floorMod(ry, 9) == 4) return hueso(Direction.Axis.X);
        if (ry < 6 && Hash.pct(x, y, z, seed ^ 0x51) < 14) return Pal.MOSSY_STONE_BRICKS;
        return Pal.wall(x, y, z);
    }

    private static BlockState luz(int nivel) {
        return Blocks.LIGHT.defaultBlockState().setValue(net.minecraft.world.level.block.LightBlock.LEVEL, nivel);
    }

    private static void luzSi(Canvas c, int x, int y, int z, int nivel) {
        if (c.isAir(x, y, z)) c.set(x, y, z, luz(nivel));
    }

    // ------------------------------------------------------------------ formas

    /** "Radio" del octogono de la torre (apotema) para un punto: <= AT es dentro. */
    public static double octo(double dx, double dz) {
        double ax = Math.abs(dx), az = Math.abs(dz);
        return Math.max(Math.max(ax, az), (ax + az) / Math.sqrt(2));
    }

    /** Si (x, z) cae dentro del muro del anillo (los tramos caidos tambien cuentan). */
    public boolean dentroMuros(double x, double z) {
        return Math.hypot(x - cx, z - cz) <= RM + 1.5;
    }

    /** El hueco del arco de la puerta en la fila h (0 = el suelo): medio ancho (-1 si no hay hueco). */
    public static int arco(int h) {
        if (h < 0 || h >= PALTO) return -1;
        if (h <= 8) return PX;
        return switch (h) {
            case 9, 10 -> 2;
            case 11 -> 1;
            default -> 0;
        };
    }

    /** El hueco del portal (parte 5d): arco apuntado de 9 x 13. */
    public static int arcoPortal(int h) {
        if (h < 0 || h >= AALTO) return -1;
        if (h <= 7) return AX;
        return switch (h) {
            case 8, 9 -> 3;
            case 10 -> 2;
            case 11 -> 1;
            default -> 0;
        };
    }

    // ------------------------------------------------------------------ posiciones utiles (para el resto del mod)

    /** El centro de la puerta por fuera (donde llega el camino). */
    public int[] rellano() {
        return new int[]{cx, Y, cz + PFUERA + 4};
    }

    /** La reja de la puerta: {x0, y0, z, x1, y1}; al abrirse sube por su ranura hasta y1 + PALTO. */
    public int[] reja() {
        return new int[]{cx - PX, Y, cz + PZ, cx + PX, Y + PALTO - 1};
    }

    /** El cofre del acertijo (la armeria, en la planta baja). */
    public int[] cofre() {
        return new int[]{cx + 6, Y, cz - 3};
    }

    /** El pedestal del amuleto, delante del portal en la terraza (con dos de paso hasta el barandal del ascensor). */
    public int[] altar() {
        return new int[]{cx, Y + TERRAZA, cz + AZ + 4};
    }

    /** El hueco del portal: {x0, y0, z, x1, y1} (con su arco: {@link #arcoPortal}). */
    public int[] portal() {
        int y = Y + TERRAZA;
        return new int[]{cx - AX, y, cz + AZ, cx + AX, y + AALTO - 1};
    }

    /** Si (x, y) del plano del portal cae dentro del hueco del arco (lo demas del rectangulo de portal() es el marco). */
    public boolean enPortal(int x, int y) {
        int w = arcoPortal(y - (Y + TERRAZA));
        return w >= 0 && Math.abs(x - cx) <= w;
    }

    /** La plataforma del ascensor: abajo (planta baja) y arriba (la terraza), {x, y, z} del centro. */
    public int[] ascensorAbajo() {
        return new int[]{cx, Y, cz};
    }

    public int[] ascensorArriba() {
        return new int[]{cx, Y + TERRAZA, cz};
    }

    /** Atriles con los libros perdidos de la fortaleza: {x, y, z, libro, mira (0 S, 1 N, 2 E, 3 O)}. */
    public int[][] atriles() {
        return new int[][]{
                {cx + 6, Y + PALTO + 2, cz + PZ - 2, 6, 1},                 // la sala del torno, encima del paso
                {cx - 6, Y, cz + 4, 11, 2},                                  // sala de guardias
                {cx + 6, Y + PISO, cz - 6, 7, 3},                            // capilla, junto al altar
                {cx - 6, Y + 2 * PISO, cz - 5, 8, 2},                        // aposentos
                {cx + 5, Y + 5 * PISO, cz + 5, 9, 3},                        // observatorio
                {cx + 6, Y + TERRAZA, cz + AZ + 4, 10, 3},                   // la terraza, cerca del portal
        };
    }

    /** Los ojos de las dos estatuas de la puerta (se encienden al abrirla): {x, y, z} de cada bloque. */
    public int[][] ojos() {
        int[][] o = new int[4][];
        int k = 0;
        for (int s = -1; s <= 1; s += 2) {
            int sx = cx + s * 11;
            o[k++] = new int[]{sx - 1, Y + 15, cz + PFUERA + 4};
            o[k++] = new int[]{sx + 1, Y + 15, cz + PFUERA + 4};
        }
        return o;
    }

    // ------------------------------------------------------------------ trampas y secretos (parte 5c)

    /** El suelo que cede (planta 4, la de la ruina, al noreste): los bloques del suelo {x, y, z} (3 x 3). */
    public int[][] sueloQueCede() {
        int[][] o = new int[9][];
        int k = 0, y = piso(4) - 1;
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) o[k++] = new int[]{cx + 5 + dx, y, cz - 5 + dz};
        return o;
    }

    /** Las losas de los pinchos de hueso: {x, y que se pisa, z} (pasillo norte del sotano y camino norte del jardin). */
    public int[][] pinchos() {
        int ys = Y + SOTANO;
        return new int[][]{
                {cx - 1, ys, cz - 6}, {cx, ys, cz - 6}, {cx + 1, ys, cz - 6},
                {cx - 1, ys, cz - 7}, {cx, ys, cz - 7}, {cx + 1, ys, cz - 7},
                {cx - 1, Y, cz - 17}, {cx, Y, cz - 17}, {cx + 1, Y, cz - 17},
                {cx - 1, Y, cz - 18}, {cx, Y, cz - 18}, {cx + 1, Y, cz - 18},
        };
    }

    /** La trampa de flechas del paso de la puerta (por dentro de la reja): las celdas que la disparan {x, y, z}. */
    public int[][] flechasDisparo() {
        int[][] o = new int[2 * PX + 1][];
        for (int dx = -PX; dx <= PX; dx++) o[dx + PX] = new int[]{cx + dx, Y, cz + PZ - 2};
        return o;
    }

    /** Las aspilleras de la trampa de flechas: {x, y, z, de que lado dispara (-1 desde el oeste, 1 desde el este)}. */
    public int[][] flechasAspilleras() {
        int z = cz + PZ - 2;
        return new int[][]{{cx - PX - 1, Y + 1, z, -1}, {cx + PX + 1, Y + 1, z, 1}, {cx - PX - 1, Y + 1, z - 1, -1}, {cx + PX + 1, Y + 1, z - 1, 1}};
    }

    /** El muro falso del sotano (el muro de la torre al oeste de la sala de guardia de abajo): {x, y, z} de cada bloque. */
    public int[][] muroFalso() {
        int ys = Y + SOTANO;
        int[][] o = new int[18][];
        int k = 0;
        for (int x = cx - AT; x <= cx - AT + GT - 1; x++)
            for (int z = cz - 1; z <= cz + 1; z++)
                for (int y = ys; y < ys + 3; y++) o[k++] = new int[]{x, y, z};
        return o;
    }

    /** El bloque que va en ese sitio del muro falso (el del muro de la torre: para volver a ponerlo). */
    public BlockState bloqueMuro(int x, int y, int z) {
        return piedra(x, y, z);
    }

    /** El cofre de la sala secreta (detras del muro falso). */
    public int[] cofreSecreto() {
        return new int[]{cx - AT - 6, Y + SOTANO, cz};
    }

    /** La trampilla escondida en la taza de la fuente rota (baja a la cripta del vigia). */
    public int[] trampilla() {
        return new int[]{cx + 1, Y - 1, cz + 30};
    }

    /** El atril de la cripta de debajo de la fuente: el libro perdido 17. */
    public int[] atrilEscondido() {
        return new int[]{cx + 3, Y - 5, cz + 27};
    }

    // ------------------------------------------------------------------ construir

    @Override
    public void build(Canvas c) {
        murallas(c);
        puerta(c);
        estatuas(c);
        jardin(c);
        cupula(c);
        torre(c);
        torrecilla(c);
        ascensor(c);
        mazmorras(c);
        plantas(c);
        terraza(c);
        derrumbes(c);
        vegetacion(c);
        for (int[] a : atriles()) {
            Direction mira = switch (a[4]) {
                case 0 -> Direction.SOUTH;
                case 1 -> Direction.NORTH;
                case 2 -> Direction.EAST;
                default -> Direction.WEST;
            };
            try {
                c.lectern(a[0], a[1], a[2], mira, com.darkcraft.fortaleza.Perdidos.libro(a[3]));
            } catch (Throwable t) {
                c.set(a[0], a[1], a[2], Pal.facing(Blocks.LECTERN, mira));
            }
        }
        luces(c);
    }

    // ------------------------------------------------------------------ el anillo de murallas en ruinas

    /** Lo alto del muro del anillo en ese angulo (0 = caido hasta el suelo): tramos caidos y mellas. */
    private int altoMuro(double a) {
        // seis tramos caidos, repartidos (nunca en la puerta, al sur: a = pi/2)
        for (int k = 0; k < 6; k++) {
            double c0 = Math.PI / 2 + (k + 0.5) * Math.PI * 2 / 6 + (Hash.f(k, 1, 0, seed) - 0.5) * 0.35;
            double w = 0.07 + Hash.f(k, 2, 0, seed) * 0.06;
            double d = Math.abs(Math.atan2(Math.sin(a - c0), Math.cos(a - c0)));
            if (d < w) return (int) Math.round((d / w) * (d / w) * 3);
            if (d < w * 1.8) return (int) Math.round(3 + (d - w) / (w * 0.8) * (ALTO_MURO - 3));
        }
        return ALTO_MURO;
    }

    private void murallas(Canvas c) {
        int R = RM + 2;
        for (int x = Math.max(cx - R, c.minX); x <= Math.min(cx + R, c.maxX); x++)
            for (int z = Math.max(cz - R, c.minZ); z <= Math.min(cz + R, c.maxZ); z++) {
                double r = Math.hypot(x - cx, z - cz);
                if (r < RM - 1.5 || r >= RM + 1.5) continue;
                double a = Math.atan2(z - cz, x - cx);
                int alto = altoMuro(a);
                boolean fuera = r >= RM + 0.5;
                // cimiento hasta la isla
                for (int y = Y - 6; y < Y - 1; y++) if (c.isAir(x, y, z)) c.set(x, y, z, Pal.foundation(x, y, z));
                c.set(x, Y - 1, z, Pal.foundation(x, Y - 1, z));
                for (int y = Y; y < Y + alto; y++) c.set(x, y, z, piedra(x, y, z));
                // almenas por fuera donde el muro esta entero; mellas sueltas
                if (alto == ALTO_MURO && fuera && Math.floorMod((int) Math.round(a * RM), 2) == 0 && Hash.pct(x, 3, z, seed) < 85)
                    c.set(x, Y + alto, z, Pal.wall(x, Y + alto, z));
                else if (alto == ALTO_MURO && !fuera && Hash.pct(x, 4, z, seed) < 12) c.set(x, Y + alto - 1, z, Pal.AIR);
            }
        // los escombros de los tramos caidos: piedras y losas a los dos lados
        for (int k = 0; k < 6; k++) {
            double c0 = Math.PI / 2 + (k + 0.5) * Math.PI * 2 / 6 + (Hash.f(k, 1, 0, seed) - 0.5) * 0.35;
            for (int i = 0; i < 26; i++) {
                double aa = c0 + (Hash.f(k, i, 7, seed) - 0.5) * 0.3, rr = RM + (Hash.f(k, i, 8, seed) - 0.5) * 12;
                int x = cx + (int) Math.round(Math.cos(aa) * rr), z = cz + (int) Math.round(Math.sin(aa) * rr);
                if (!c.inXZ(x, z) || Math.abs(Math.hypot(x - cx, z - cz) - RM) < 1.6) continue;
                int y = suelo(c, x, z);
                if (y == Integer.MIN_VALUE) continue;
                int p = Hash.pct(x, i, z, seed);
                c.set(x, y, z, p < 45 ? Pal.DS_COBBLED : p < 75 ? Pal.slab(Blocks.COBBLED_DEEPSLATE_SLAB, false) : Pal.wall(x, y, z));
                if (p < 20) c.set(x, y + 1, z, Pal.slab(Blocks.DEEPSLATE_BRICK_SLAB, false));
            }
        }
        // las torrecillas rotas (ocho, menos donde la puerta)
        for (int k = 0; k < 8; k++) {
            double a = Math.PI / 2 + Math.PI / 8 + k * Math.PI / 4;
            int tx = cx + (int) Math.round(Math.cos(a) * RM), tz = cz + (int) Math.round(Math.sin(a) * RM);
            torrecillaRota(c, tx, tz, 11 + Hash.h(k, 3, 0, seed) % 7, k);
        }
    }

    /** Lo primero solido que hay de pie en (x, z) cerca del patio (la altura donde se pisa), o MIN_VALUE. */
    private int suelo(Canvas c, int x, int z) {
        for (int y = Y + 6; y >= Y - 10; y--)
            if (!c.isAir(x, y - 1, z) && c.isAir(x, y, z)) return y;
        return Integer.MIN_VALUE;
    }

    private void torrecillaRota(Canvas c, int tx, int tz, int alto, int k) {
        double r = 3.5;
        for (int x = tx - 5; x <= tx + 5; x++)
            for (int z = tz - 5; z <= tz + 5; z++) {
                if (!c.inXZ(x, z)) continue;
                double d = Math.hypot(x - tx, z - tz);
                if (d > r + 0.5) continue;
                boolean borde = d > r - 1.0;
                double ang = Math.atan2(z - tz, x - tx);
                // la rotura: lo alto baja por un lado
                int top = alto - (int) Math.round((1 + Math.sin(ang + k * 1.7)) * 3.5);
                for (int y = Y - 6; y < Y - 1; y++) if (c.isAir(x, y, z)) c.set(x, y, z, Pal.foundation(x, y, z));
                c.set(x, Y - 1, z, Pal.PB_BRICKS);
                for (int y = Y; y <= Y + alto + 2; y++) {
                    if (borde && y < Y + top) c.set(x, y, z, piedra(x, y, z));
                    else if (!borde && y == Y + ALTO_MURO - 1 && d < r - 1.5 && Hash.pct(x, y, z, seed) < 60) c.set(x, y, z, Pal.PB_BRICKS);
                    else c.set(x, y, z, Pal.AIR);
                }
                // saeteras
                if (borde && d > r - 0.5 && Math.abs(Math.sin(ang * 2)) < 0.2)
                    for (int y = Y + 4; y <= Y + 5; y++) if (y < Y + top) c.set(x, y, z, Pal.AIR);
            }
        // la puerta desde el adarve del anillo y desde el patio
        double a = Math.atan2(cz - tz, cx - tx);
        int px = tx + (int) Math.round(Math.cos(a) * 3), pz = tz + (int) Math.round(Math.sin(a) * 3);
        for (int y = Y; y <= Y + 2; y++) c.set(px, y, pz, Pal.AIR);
    }

    // ------------------------------------------------------------------ la puerta

    /**
     * La puerta: un cuerpo alto de piedra con el arco apuntado en sus dos caras; por dentro el paso es recto (la reja
     * es un rectangulo de 7 x PALTO que sube por su ranura, encima, hasta la sala del torno) y arriba el tejado empinado.
     */
    private void puerta(Canvas c) {
        int z0 = cz + RM - 3, z1 = cz + PFUERA;
        int alto = 2 * PALTO + 4;
        for (int x = cx - PX - 5; x <= cx + PX + 5; x++)
            for (int z = z0; z <= z1; z++) {
                if (!c.inXZ(x, z)) continue;
                int dx = Math.abs(x - cx);
                boolean cara = z == z0 || z == z1;
                for (int y = Y - 1; y <= Y + alto; y++) {
                    int h = y - Y;
                    // el tejado a dos aguas (cae hacia los lados)
                    int tejado = alto - Math.max(0, dx - 2);
                    if (h > tejado) continue;
                    int w = arco(h);
                    boolean paso = cara ? (w >= 0 && dx <= w) : (h >= 0 && h < PALTO && dx <= PX);
                    boolean ranura = z == cz + PZ && dx <= PX && h >= PALTO && h < 2 * PALTO;
                    boolean sala = !cara && dx <= PX + 3 && h >= PALTO + 2 && h <= PALTO + 7;
                    if (paso || ranura || sala) c.set(x, y, z, Pal.AIR);
                    else if (h == -1 || h == PALTO + 1 && !cara) c.set(x, y, z, Pal.PB_BRICKS);
                    else c.set(x, y, z, h >= tejado - 1 ? Pal.roof(x, y, z) : piedra(x, y, z));
                }
                if (dx <= PX) c.set(x, Y - 1, z, Pal.b(Blocks.BLACKSTONE));
            }
        // pinaculos de hueso en las cuatro esquinas del cuerpo de la puerta
        for (int sx = -1; sx <= 1; sx += 2)
            for (int z : new int[]{z0, z1}) {
                int x = cx + sx * (PX + 5);
                for (int y = Y + alto - 6; y <= Y + alto + 2; y++) c.set(x, y, z, y == Y + alto + 2 ? Pal.b(Blocks.END_ROD) : HUESO);
            }
        // las molduras de hueso del arco en las dos caras
        for (int z : new int[]{z0, z1})
            for (int h = 0; h <= PALTO; h++) {
                int w = h < PALTO ? arco(h) : -1;
                int ant = h > 0 ? arco(h - 1) : PX;
                for (int sx = -1; sx <= 1; sx += 2) {
                    if (w >= 0) c.set(cx + sx * (w + 1), Y + h, z, HUESO);
                    for (int xx = Math.max(w, 0) + 1; xx <= ant; xx++) c.set(cx + sx * (xx + 1), Y + h, z, HUESO);
                }
                if (w < 0) c.set(cx, Y + h, z, HUESO);
            }
        cerrarReja(c);
        for (int[] a : flechasAspilleras()) if (c.inXZ(a[0], a[2])) c.set(a[0], a[1], a[2], Pal.AIR);
        // faroles de almas bajo el arco y la escalera de mano a la sala del torno (por la cara de dentro, al este)
        c.set(cx, Y + PALTO - 1, z0 + 1, Pal.SOUL_LANTERN_HANG);
        int lx = cx + PX + 2;
        for (int y = Y; y <= Y + PALTO + 2; y++)
            c.set(lx, y, z0 + 1, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH));
        for (int y = Y; y <= Y + 1; y++) c.set(lx, y, z0, Pal.AIR);
        // el rellano de fuera, donde llega el camino
        for (int x = cx - PX - 3; x <= cx + PX + 3; x++)
            for (int z = z1 + 1; z <= z1 + 7; z++) {
                c.set(x, Y - 1, z, Math.abs(x - cx) <= PX ? Pal.b(Blocks.BLACKSTONE) : Pal.PB_BRICKS);
                for (int y = Y - 8; y < Y - 1; y++) if (c.isAir(x, y, z)) c.set(x, y, z, Pal.foundation(x, y, z));
                for (int y = Y; y <= Y + 4; y++) if (Math.abs(x - cx) <= PX + 2) c.set(x, y, z, Pal.AIR);
            }
    }

    /** La reja de hueso cerrada: el rectangulo de 7 x PALTO del paso (por fuera, el arco apuntado tapa sus esquinas). */
    public void cerrarReja(Canvas c) {
        BlockState r = rejaHueso();
        for (int h = 0; h < PALTO; h++)
            for (int dx = -PX; dx <= PX; dx++) c.set(cx + dx, Y + h, cz + PZ, r);
    }

    /**
     * Las dos estatuas de caballeros encapuchados a los lados de la puerta, por fuera, mirando al camino: 18 de alto, la
     * tunica hasta los pies, la capucha con la cara en sombra (los ojos se encienden al abrir la puerta) y las manos en el
     * pomo de una espada clavada delante.
     */
    private void estatuas(Canvas c) {
        for (int s = -1; s <= 1; s += 2) {
            int sx = cx + s * 11, sz = cz + PFUERA + 3;
            // pedestal de dos alturas
            for (int x = sx - 3; x <= sx + 3; x++)
                for (int z = sz - 3; z <= sz + 3; z++) {
                    for (int y = Y - 8; y < Y; y++) if (c.isAir(x, y, z)) c.set(x, y, z, Pal.foundation(x, y, z));
                    boolean borde = Math.abs(x - sx) == 3 || Math.abs(z - sz) == 3;
                    c.set(x, Y, z, borde ? Pal.PB_CHISELED : Pal.PB_BRICKS);
                    if (Math.abs(x - sx) <= 2 && Math.abs(z - sz) <= 2) c.set(x, Y + 1, z, Pal.PB_BRICKS);
                }
            // la tunica: de los pies (Y+2) a los hombros (Y+12), cada vez mas estrecha
            for (int y = Y + 2; y <= Y + 12; y++) {
                double t = (y - Y - 2) / 10.0;
                double rx = 2.4 - t * 0.9, rz = 1.9 - t * 0.7;
                for (int x = sx - 3; x <= sx + 3; x++)
                    for (int z = sz - 3; z <= sz + 3; z++) {
                        double dx = (x - sx) / rx, dz = (z - sz) / rz;
                        if (dx * dx + dz * dz > 1.15) continue;
                        // los pliegues: franjas mas oscuras
                        c.set(x, y, z, Math.floorMod(x + (y / 3), 3) == 0 ? Pal.DS_TILES : Pal.b(Blocks.BLACKSTONE));
                    }
            }
            // los hombros y la capucha (con la punta), la cara en sombra hacia el sur
            for (int x = sx - 2; x <= sx + 2; x++)
                for (int z = sz - 1; z <= sz + 1; z++) c.set(x, Y + 13, z, Pal.PB);
            for (int y = Y + 14; y <= Y + 17; y++)
                for (int x = sx - 1; x <= sx + 1; x++)
                    for (int z = sz - 1; z <= sz + 1; z++) c.set(x, y, z, Pal.b(Blocks.BLACKSTONE));
            c.set(sx, Y + 18, sz, Pal.b(Blocks.BLACKSTONE));
            c.set(sx, Y + 18, sz - 1, Pal.stairs(Blocks.BLACKSTONE_STAIRS, Direction.SOUTH, false));
            for (int x = sx - 1; x <= sx + 1; x++) {
                c.set(x, Y + 17, sz + 2, Pal.stairs(Blocks.BLACKSTONE_STAIRS, Direction.NORTH, true));
                for (int y = Y + 14; y <= Y + 16; y++) c.set(x, y, sz + 1, Pal.b(Blocks.BLACK_CONCRETE));
            }
            for (int y = Y + 14; y <= Y + 16; y++) {
                c.set(sx - 2, y, sz + 1, Pal.stairs(Blocks.BLACKSTONE_STAIRS, Direction.EAST, false));
                c.set(sx + 2, y, sz + 1, Pal.stairs(Blocks.BLACKSTONE_STAIRS, Direction.WEST, false));
            }
            // los brazos: bajan por delante hasta el pomo
            for (int y = Y + 9; y <= Y + 12; y++) {
                c.set(sx - 1, y, sz + 2, Pal.PB);
                c.set(sx + 1, y, sz + 2, Pal.PB);
            }
            // la espada clavada: hoja de hierro negro, cruz de hueso, pomo de oro bajo las manos
            for (int y = Y + 2; y <= Y + 8; y++) c.set(sx, y, sz + 3, HIERRO);
            for (int x = sx - 1; x <= sx + 1; x++) c.set(x, Y + 9, sz + 3, hueso(Direction.Axis.X));
            c.set(sx, Y + 10, sz + 3, Pal.PB_CHISELED);
            c.set(sx, Y + 11, sz + 3, Pal.GILDED);
        }
    }

    // ------------------------------------------------------------------ el jardin muerto

    /** Si en (x, z) del patio cae lluvia por un hueco de la cupula. */
    private boolean bajoHueco(int x, int z) {
        double r = Math.hypot(x - cx, z - cz);
        if (r >= RD - 1 || octo(x - cx, z - cz) <= AT + 1) return false;
        double h = Math.sqrt(RD * RD - r * r);
        return panelRoto(x, (int) Math.round(Y + h), z);
    }

    private void jardin(Canvas c) {
        int R = RD + 1;
        for (int x = Math.max(cx - R, c.minX); x <= Math.min(cx + R, c.maxX); x++)
            for (int z = Math.max(cz - R, c.minZ); z <= Math.min(cz + R, c.maxZ); z++) {
                int dx = x - cx, dz = z - cz;
                double r = Math.hypot(dx, dz);
                if (r > RM - 1.5 || octo(dx, dz) <= AT) continue;
                // el suelo: losas por los caminos, tierra y musgo por los parterres
                boolean camino = (Math.abs(dx) <= 2 && dz > 0) || (Math.abs(dz) <= 1 && Math.abs(dx) > AT)
                        || (Math.abs(dx) <= 1 && dz < 0) || Math.abs(r - 22) <= 1.6 || r > RD - 2.5;
                BlockState s;
                if (camino) s = Hash.pct(x, 0, z, seed) < 16 ? Pal.DS_TILES_CRACKED : Pal.paving(x, Y - 1, z);
                else {
                    double m = Math.sin(x * 0.21 + z * 0.13) + Math.cos(z * 0.19 - x * 0.07);
                    int p = Hash.pct(x, 1, z, seed);
                    s = m > 0.9 ? Pal.MOSS : p < 35 ? Pal.COARSE_DIRT : p < 60 ? Pal.PODZOL : p < 80 ? Pal.ROOTED_DIRT : Pal.GRASS_BLOCK;
                }
                for (int y = Y - 4; y < Y - 1; y++) if (c.isAir(x, y, z)) c.set(x, y, z, Pal.foundation(x, y, z));
                c.set(x, Y - 1, z, s);
                for (int y = Y; y <= Y + 3; y++) if (!c.isAir(x, y, z) && r < RD - 1) c.set(x, y, z, Pal.AIR);
                // los estanques donde cae la lluvia (fuera de los caminos)
                if (!camino && bajoHueco(x, z) && Hash.pct(x, 2, z, seed) < 85 && r > AT + 4) {
                    c.set(x, Y - 1, z, Pal.WATER);
                    c.set(x, Y - 2, z, Pal.b(Blocks.MUD));
                    if (Hash.pct(x, 5, z, seed) < 22) c.set(x, Y, z, Pal.b(Blocks.LILY_PAD));
                    continue;
                }
                if (camino || r > RD - 1.5) continue;
                // lo que crece (seco) y lo que hay tirado
                int p = Hash.h(x, 6, z, seed) % 1000;
                BlockState d = null;
                if (p < 70) d = Pal.b(Blocks.DEAD_BUSH);
                else if (p < 110) d = Pal.b(Blocks.WITHER_ROSE);
                else if (p < 160) d = Pal.b(Blocks.GRASS);
                else if (p < 175) d = Pal.b(Blocks.BROWN_MUSHROOM);
                else if (p < 182) d = Pal.b(Blocks.RED_MUSHROOM);
                else if (p < 188) d = Pal.b(Blocks.SKELETON_SKULL);
                if (d != null && (d.is(Blocks.DEAD_BUSH) || d.is(Blocks.WITHER_ROSE) || d.is(Blocks.GRASS) || d.is(Blocks.BROWN_MUSHROOM) || d.is(Blocks.RED_MUSHROOM))
                        && (s.is(Blocks.MOSS_BLOCK) || s.is(Blocks.PODZOL) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.ROOTED_DIRT) || s.is(Blocks.GRASS_BLOCK)))
                    c.set(x, Y, z, d.is(Blocks.GRASS) && !s.is(Blocks.GRASS_BLOCK) && !s.is(Blocks.MOSS_BLOCK) ? Pal.b(Blocks.DEAD_BUSH) : d);
                else if (d != null && d.is(Blocks.SKELETON_SKULL)) c.set(x, Y, z, d);
            }
        // los cristales caidos de la cupula (bajo los huecos, sobre los caminos y la tierra)
        for (int k = 0; k < 160; k++) {
            int x = cx + (int) Math.round((Hash.f(k, 1, 3, seed) - 0.5) * 2 * (RD - 3)), z = cz + (int) Math.round((Hash.f(k, 2, 3, seed) - 0.5) * 2 * (RD - 3));
            if (!c.inXZ(x, z) || Math.hypot(x - cx, z - cz) > RD - 3 || octo(x - cx, z - cz) <= AT + 1 || !bajoHueco(x, z)) continue;
            if (!c.isAir(x, Y, z) || c.get(x, Y - 1, z).is(Blocks.WATER)) continue;
            int p = Hash.pct(k, 4, 0, seed);
            c.set(x, Y, z, p < 45 ? AHUMADO : p < 70 ? Pal.b(Blocks.GRAY_STAINED_GLASS_PANE) : p < 85 ? Pal.GLASS_RED : Pal.b(Blocks.RED_STAINED_GLASS_PANE));
        }
        // arboles secos, rosales marchitos, bancos y la fuente rota
        for (int k = 0; k < 9; k++) {
            double a = k * Math.PI * 2 / 9 + 0.35, rr = 15 + Hash.f(k, 9, 0, seed) * 16;
            int x = cx + (int) Math.round(Math.cos(a) * rr), z = cz + (int) Math.round(Math.sin(a) * rr);
            // ni en el camino del sur (por donde pasa la camara de las cinematicas: sin ramas encima) ni en el de ronda
            if (Math.abs(x - cx) <= 8 && z > cz || Math.abs(Math.hypot(x - cx, z - cz) - 22) < 3) continue;
            arbolSeco(c, x, z, k);
        }
        for (int k = 0; k < 14; k++) {
            double a = k * Math.PI * 2 / 14 + 0.2;
            int x = cx + (int) Math.round(Math.cos(a) * 24.5), z = cz + (int) Math.round(Math.sin(a) * 24.5);
            if (Math.abs(x - cx) <= 3 || Math.abs(z - cz) <= 2) continue;
            rosal(c, x, z, k);
        }
        bancos(c);
        fuente(c, cx, cz + 28);
        // las losas de los pinchos del camino norte (cinceladas)
        for (int[] q : pinchos()) if (q[1] == Y && c.inXZ(q[0], q[2])) c.set(q[0], Y - 1, q[2], Pal.b(Blocks.CHISELED_DEEPSLATE));
    }

    private void arbolSeco(Canvas c, int x0, int z0, int k) {
        int alto = 5 + Hash.h(k, 1, 1, seed) % 4;
        Block tronco = k % 3 == 0 ? Blocks.STRIPPED_DARK_OAK_LOG : k % 3 == 1 ? Blocks.DARK_OAK_LOG : Blocks.MANGROVE_LOG;
        double x = x0, z = z0;
        double tx = (Hash.f(k, 2, 1, seed) - 0.5) * 0.35, tz = (Hash.f(k, 3, 1, seed) - 0.5) * 0.35;
        for (int i = 0; i < alto; i++) {
            c.set((int) Math.round(x), Y + i, (int) Math.round(z), Pal.log(tronco, Direction.Axis.Y));
            x += tx;
            z += tz;
        }
        // ramas: tres o cuatro, en diagonal hacia arriba
        int n = 3 + k % 2;
        for (int b = 0; b < n; b++) {
            double a = b * Math.PI * 2 / n + k;
            double bx = x, bz = z;
            int by = Y + alto - 1 - b % 2;
            for (int i = 1; i <= 3 + b % 2; i++) {
                bx += Math.cos(a);
                bz += Math.sin(a);
                if (i % 2 == 0) by++;
                Direction.Axis ax = Math.abs(Math.cos(a)) > Math.abs(Math.sin(a)) ? Direction.Axis.X : Direction.Axis.Z;
                c.set((int) Math.round(bx), by, (int) Math.round(bz), Pal.log(tronco, ax));
            }
            if (Hash.pct(k, b, 4, seed) < 40) c.set((int) Math.round(bx), by - 1, (int) Math.round(bz), Pal.b(Blocks.VINE));
        }
    }

    private void rosal(Canvas c, int x, int z, int k) {
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) {
                int px = x + dx, pz = z + dz;
                if (!c.isAir(px, Y, pz)) continue;
                int p = Hash.pct(px, k, pz, seed);
                c.set(px, Y - 1, pz, Pal.PODZOL);
                c.set(px, Y, pz, p < 40 ? Pal.b(Blocks.WITHER_ROSE) : p < 75 ? Pal.b(Blocks.DEAD_BUSH) : Pal.b(Blocks.SWEET_BERRY_BUSH));
            }
        // el borde de piedra
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++)
                if (Math.max(Math.abs(dx), Math.abs(dz)) == 2 && Hash.pct(x + dx, 9, z + dz, seed) < 70 && c.isAir(x + dx, Y, z + dz))
                    c.set(x + dx, Y, z + dz, Pal.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, false));
    }

    private void bancos(Canvas c) {
        for (int k = 0; k < 16; k++) {
            double a = k * Math.PI * 2 / 16 + Math.PI / 16;
            int x = cx + (int) Math.round(Math.cos(a) * 19.5), z = cz + (int) Math.round(Math.sin(a) * 19.5);
            if (Math.abs(x - cx) <= 3 || Math.abs(z - cz) <= 2 || Hash.pct(k, 3, 3, seed) < 25) continue;
            // mirando hacia fuera del anillo de paseo (de espaldas a la torre)
            Direction mira = Math.abs(Math.cos(a)) > Math.abs(Math.sin(a)) ? (Math.cos(a) > 0 ? Direction.WEST : Direction.EAST)
                    : (Math.sin(a) > 0 ? Direction.NORTH : Direction.SOUTH);
            if (c.isAir(x, Y, z)) c.set(x, Y, z, Pal.stairs(Blocks.DARK_OAK_STAIRS, mira, false));
        }
    }

    /** La fuente rota: taza octogonal partida, la estatua caida y un poco de agua sucia. */
    private void fuente(Canvas c, int fx, int fz) {
        for (int x = fx - 4; x <= fx + 4; x++)
            for (int z = fz - 4; z <= fz + 4; z++) {
                if (!c.inXZ(x, z)) continue;
                double d = octo(x - fx, z - fz);
                if (d > 4.3) continue;
                c.set(x, Y - 1, z, Pal.DS_TILES);
                boolean borde = d > 3.3;
                boolean roto = Hash.pct(x, 11, z, seed) < 30 && x > fx;
                if (borde) c.set(x, Y, z, roto ? Pal.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, false) : Pal.PB_BRICKS);
                else if (d < 3.3) {
                    c.set(x, Y - 1, z, Pal.PB_BRICKS);
                    if (Hash.pct(x, 12, z, seed) < 55 && d > 1.2) c.set(x, Y, z, Pal.b(Blocks.MOSS_CARPET));
                }
            }
        // el pie de la estatua y la estatua caida
        c.set(fx, Y, fz, Pal.PB_CHISELED);
        c.set(fx, Y + 1, fz, Pal.b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
        for (int i = 1; i <= 3; i++) c.set(fx + i, Y, fz - 1, i == 3 ? Pal.b(Blocks.CHISELED_DEEPSLATE) : Pal.b(Blocks.POLISHED_DEEPSLATE));
        c.set(fx - 2, Y, fz + 1, Pal.b(Blocks.WATER));
        c.set(fx - 1, Y, fz + 2, Pal.b(Blocks.WATER));
        cripta(c);
    }

    /**
     * La cripta del vigia (parte 5c, el libro perdido 17): una trampilla en la taza de la fuente, una escalera de mano y
     * debajo una salita con el atril, los huesos del vigia y un farol.
     */
    private void cripta(Canvas c) {
        int[] t = trampilla();
        int x0 = cx - 1, x1 = cx + 4, z0 = cz + 26, z1 = cz + 30, yb = Y - 6;
        for (int x = x0 - 1; x <= x1 + 1; x++)
            for (int z = z0 - 1; z <= z1 + 1; z++)
                for (int y = yb; y <= Y - 2; y++) {
                    if (!c.inXZ(x, z)) continue;
                    boolean borde = x < x0 || x > x1 || z < z0 || z > z1 || y == yb || y == Y - 2;
                    c.set(x, y, z, borde ? (y == yb ? Pal.DS_TILES : Pal.DS_BRICKS) : Pal.AIR);
                }
        // el pozo de la trampilla (con la escalera de mano por el lado sur)
        for (int y = Y - 5; y <= Y - 2; y++) {
            c.set(t[0], y, t[2], Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH));
            c.set(t[0], y, t[2] + 1, Pal.DS_BRICKS);
        }
        c.set(t[0], t[1], t[2], Blocks.DARK_OAK_TRAPDOOR.defaultBlockState()
                .setValue(net.minecraft.world.level.block.TrapDoorBlock.FACING, Direction.NORTH)
                .setValue(net.minecraft.world.level.block.TrapDoorBlock.HALF, net.minecraft.world.level.block.state.properties.Half.TOP));
        // los huesos del vigia, su farol y velas
        c.set(cx - 1, Y - 5, cz + 26, Pal.b(Blocks.SKELETON_SKULL));
        c.set(cx, Y - 5, cz + 26, hueso(Direction.Axis.X));
        c.set(cx - 1, Y - 5, cz + 27, hueso(Direction.Axis.Z));
        c.set(cx + 4, Y - 5, cz + 30, Pal.SOUL_LANTERN);
        c.set(cx + 4, Y - 5, cz + 26, Pal.candles(Blocks.BLACK_CANDLE, 3));
        c.set(t[0], Y, t[2], Pal.AIR);
        int[] a = atrilEscondido();
        try {
            c.lectern(a[0], a[1], a[2], Direction.SOUTH, com.darkcraft.fortaleza.Perdidos.libro(17));
        } catch (Throwable e) {
            c.set(a[0], a[1], a[2], Pal.facing(Blocks.LECTERN, Direction.SOUTH));
        }
    }

    // ------------------------------------------------------------------ la cupula

    /** Si el panel de la cupula que contiene ese bloque esta roto (sin cristal). */
    private boolean panelRoto(int x, int y, int z) {
        int[] p = panel(x, y, z);
        if (p == null) return false;
        // los rotos: uno de cada cinco, mas por el lado oeste (la tormenta)
        int h = Hash.h(p[0], p[1], 0, seed ^ 0xC0DE) % 100;
        double a = p[0] * Math.PI * 2 / 32;
        return h < 16 + (int) (Math.max(0, -Math.cos(a)) * 14);
    }

    /** El panel de un punto de la cupula: {sector (32 alrededor), banda (de 7 grados de alto)}, o null en la torre. */
    private int[] panel(int x, int y, int z) {
        double dx = x - cx, dz = z - cz, dy = Math.max(0, y - Y);
        double a = Math.atan2(dz, dx);
        if (a < 0) a += Math.PI * 2;
        double el = Math.atan2(dy, Math.hypot(dx, dz));
        return new int[]{(int) (a / (Math.PI * 2 / 32)), (int) (el / Math.toRadians(9))};
    }

    private void cupula(Canvas c) {
        int R = RD;
        for (int x = Math.max(cx - R, c.minX); x <= Math.min(cx + R, c.maxX); x++)
            for (int z = Math.max(cz - R, c.minZ); z <= Math.min(cz + R, c.maxZ); z++) {
                double dx = x - cx, dz = z - cz, rh = Math.hypot(dx, dz);
                if (rh > R + 0.5) continue;
                if (octo(dx, dz) <= AT + 0.5) continue; // la torre
                // la columna de la cupula: de la altura del borde interior al exterior
                double yIn = rh < R - 1.3 ? Math.sqrt((R - 1.3) * (R - 1.3) - rh * rh) : 0;
                double yOut = Math.sqrt(Math.max(0, (R + 0.5) * (R + 0.5) - rh * rh));
                int y0 = Y + (int) Math.floor(yIn), y1 = Y + (int) Math.ceil(yOut) - 1;
                double a = Math.atan2(dz, dx);
                for (int y = Math.max(Y, y0); y <= y1; y++) {
                    double d = Math.sqrt(rh * rh + (y + 0.5 - Y) * (y + 0.5 - Y));
                    if (d > R + 0.5 || d < R - 1.3) continue;
                    // la puerta de la cupula (al sur, como la de la muralla) y la del este (la torrecilla)
                    int hy = y - Y;
                    if (dz > R - 4 && Math.abs(dx) <= arco(hy) + 1 && arco(hy) >= 0) continue;
                    c.set(x, y, z, cristal(x, y, z, a, rh));
                }
            }
    }

    /** El bloque de la cupula en ese punto: nervio de hueso o de hierro negro, panel ahumado o rojo, o el hueco. */
    private BlockState cristal(int x, int y, int z, double a, double rh) {
        double el = Math.atan2(y + 0.5 - Y, rh);
        // los nervios: 16 meridianos (hueso y hierro alternos) y cuatro anillos
        double sec = Math.PI * 2 / 16;
        double off = Math.abs(Math.IEEEremainder(a, sec)) * rh;
        if (off < 0.75) {
            int m = (int) Math.round((a < 0 ? a + Math.PI * 2 : a) / sec) % 16;
            return m % 2 == 0 ? hueso(Direction.Axis.Y) : HIERRO;
        }
        double ring = Math.abs(Math.IEEEremainder(el, Math.toRadians(18))) * RD;
        if (ring < 0.7 && el > 0.05) return (int) Math.round(el / Math.toRadians(18)) % 2 == 0 ? HIERRO : hueso(Direction.Axis.X);
        if (panelRoto(x, y, z)) return Pal.AIR;
        int[] p = panel(x, y, z);
        // paneles rojos: una banda (la tercera) y rombos sueltos
        boolean rojo = p[1] == 3 || (p[0] + p[1]) % 7 == 0;
        return rojo ? Pal.GLASS_RED : (Hash.pct(p[0], p[1], 2, seed) < 30 ? Pal.GLASS_BLACK : AHUMADO);
    }

    // ------------------------------------------------------------------ la torre

    /** Las alturas de suelo de cada planta (el suelo esta en la de debajo). */
    public int piso(int k) {
        return Y + k * PISO;
    }

    private void torre(Canvas c) {
        int R = AT + 3;
        int top = Y + TERRAZA;
        for (int x = Math.max(cx - R, c.minX); x <= Math.min(cx + R, c.maxX); x++)
            for (int z = Math.max(cz - R, c.minZ); z <= Math.min(cz + R, c.maxZ); z++) {
                int dx = x - cx, dz = z - cz;
                double o = octo(dx, dz);
                if (o > AT + 0.5) continue;
                boolean muro = o > AT - GT + 0.5;
                for (int y = Y + SOTANO - 1; y <= top - 1; y++) {
                    int h = y - Y;
                    if (muro) c.set(x, y, z, piedra(x, y, z));
                    else if (h == SOTANO - 1 || (h >= -1 && (h + 1) % PISO == 0 && h < TERRAZA - 1) || h == TERRAZA - 1) c.set(x, y, z, suelo(x, y, z, h));
                    else c.set(x, y, z, Pal.AIR);
                }
            }
        // contrafuertes en las ocho esquinas, que suben hasta la terraza
        for (int k = 0; k < 8; k++) {
            double a = k * Math.PI / 4 + Math.PI / 8;
            double ex = Math.cos(a) * (AT / Math.cos(Math.PI / 8)), ez = Math.sin(a) * (AT / Math.cos(Math.PI / 8));
            for (int y = Y - 1; y < top - 4; y++) {
                int salida = y < Y + 20 ? 2 : y < Y + 45 ? 1 : 0;
                for (int i = 0; i <= salida; i++) {
                    int x = cx + (int) Math.round(ex + Math.cos(a) * i), z = cz + (int) Math.round(ez + Math.sin(a) * i);
                    c.set(x, y, z, i == salida && (y - Y) % 9 == 4 ? HUESO : Pal.PB_BRICKS);
                }
            }
        }
        // ventanas apuntadas en cada cara (vidrieras rojas y negras, algunas rotas)
        for (int k = 0; k < 8; k++) {
            double a = k * Math.PI / 4;
            for (int p = 0; p < NPISOS; p++) {
                if (k == 0 || (k == 2 && p == 0)) continue; // la torrecilla (este) y la puerta (sur)
                int y0 = piso(p) + 3;
                for (int h = 0; h < 5; h++) {
                    int w = h < 3 ? 1 : h == 3 ? 1 : 0;
                    for (int s = -w; s <= w; s++) {
                        double px = Math.cos(a) * (AT - 0.5) - Math.sin(a) * s * 0.9, pz = Math.sin(a) * (AT - 0.5) + Math.cos(a) * s * 0.9;
                        for (int g = 0; g < GT; g++) {
                            int x = cx + (int) Math.round(px - Math.cos(a) * g), z = cz + (int) Math.round(pz - Math.sin(a) * g);
                            boolean roto = Hash.pct(k, p, h, seed) < 25;
                            c.set(x, y0 + h, z, g == 0 ? (roto ? Pal.AIR : ((h + s) % 2 == 0 ? Pal.GLASS_RED : Pal.GLASS_BLACK)) : Pal.AIR);
                        }
                    }
                }
            }
        }
        // la puerta de la torre (al sur), con su arco de hueso
        for (int h = 0; h < 6; h++) {
            int w = h < 4 ? 1 : h == 4 ? 1 : 0;
            for (int s = -w; s <= w; s++)
                for (int z = cz + AT - GT + 1; z <= cz + AT + 1; z++) c.set(cx + s, Y + h, z, Pal.AIR);
            for (int s = -1; s <= 1; s += 2) c.set(cx + s * (w + 1), Y + h, cz + AT, HUESO);
        }
        c.set(cx, Y + 6, cz + AT, HUESO);
    }

    /** El suelo de cada planta: piedra pulida con losas, y madera en los aposentos. */
    private BlockState suelo(int x, int y, int z, int h) {
        int planta = (h + 1) / PISO;
        if (planta == 2 || planta == 3) return Pal.woodFloor(x, y, z);
        return ((Math.floorDiv(x, 2) + Math.floorDiv(z, 2)) & 1) == 0 ? Pal.PB_BRICKS : Pal.DS_TILES;
    }

    // ------------------------------------------------------------------ la torrecilla de la escalera (al este)

    /** Escalones de la escalera de caracol: PISO por vuelta (cada vuelta, una planta; al oeste se llega a cada una). */
    private static final int PASOS = PISO;

    /** Donde se pisa en el escalon del sector s (0..PASOS-1, angulo creciente de x hacia z) en la vuelta n. */
    private int pisaEscalon(int s, int n) {
        return Y + n * PISO + (s - PASOS / 2);
    }

    private void torrecilla(Canvas c) {
        int tx = cx + TX, tz = cz;
        int y0 = Y + SOTANO, y1 = Y + TERRAZA + 4;
        for (int x = tx - 5; x <= tx + 5; x++)
            for (int z = tz - 5; z <= tz + 5; z++) {
                if (!c.inXZ(x, z)) continue;
                double d = Math.hypot(x - tx, z - tz);
                if (d > RTU + 0.5) continue;
                boolean borde = d > RTU - 0.9;
                for (int y = y0 - 1; y <= y1; y++) {
                    if (borde) c.set(x, y, z, piedra(x, y, z));
                    else if (d < 1.0) c.set(x, y, z, y >= y1 - 1 ? Pal.SOUL_LANTERN : Pal.PB_CHISELED);
                    else c.set(x, y, z, y == y0 - 1 ? Pal.PB_BRICKS : Pal.AIR);
                }
                // tejado conico
                for (int y = y1 + 1; y <= y1 + 1 + (int) Math.round((RTU + 0.5 - d) * 2.2); y++) c.set(x, y, z, Pal.roof(x, y, z));
            }
        // los escalones (de la planta del sotano a la terraza)
        for (int x = tx - 4; x <= tx + 4; x++)
            for (int z = tz - 4; z <= tz + 4; z++) {
                if (!c.inXZ(x, z)) continue;
                double d = Math.hypot(x - tx, z - tz);
                if (d < 1.0 || d > RTU - 0.9) continue;
                double a = Math.atan2(z - tz, x - tx);
                if (a < 0) a += Math.PI * 2;
                int sct = (int) (a / (Math.PI * 2 / PASOS));
                Direction mira = sube((sct + 0.5) * Math.PI * 2 / PASOS);
                for (int n = SOTANO / PISO; n <= TERRAZA / PISO; n++) {
                    int y = pisaEscalon(sct, n);
                    if (y < y0 || y > Y + TERRAZA) continue;
                    // en el sector de las puertas (al oeste) el escalon es un bloque entero: el descansillo
                    c.set(x, y - 1, z, sct == PASOS / 2 ? Pal.PB_BRICKS : Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, mira, false));
                }
            }
        // arriba, donde ya no hay escalones (despues del descansillo de la terraza), suelo: que no se caiga nadie al hueco
        for (int x = tx - 4; x <= tx + 4; x++)
            for (int z = tz - 4; z <= tz + 4; z++) {
                if (!c.inXZ(x, z)) continue;
                double d = Math.hypot(x - tx, z - tz);
                if (d < 1.0 || d > RTU - 0.9) continue;
                double a = Math.atan2(z - tz, x - tx);
                if (a < 0) a += Math.PI * 2;
                int sct = (int) (a / (Math.PI * 2 / PASOS));
                if (sct > PASOS / 2) c.set(x, Y + TERRAZA - 1, z, Pal.PB_BRICKS);
            }
        // las puertas: al oeste (hacia la torre), una en cada planta y en la terraza
        for (int n = SOTANO / PISO; n <= TERRAZA / PISO; n++) {
            int y = Y + n * PISO;
            for (int x = cx + AT - GT; x <= tx - 2; x++)
                for (int h = 0; h < 3; h++) c.set(x, y + h, tz, Pal.AIR);
        }
    }

    /** Hacia donde mira un escalon (la direccion en que se sube) en el angulo a (sentido de x hacia z). */
    private static Direction sube(double a) {
        double tx = -Math.sin(a), tz = Math.cos(a);
        if (Math.abs(tx) > Math.abs(tz)) return tx > 0 ? Direction.EAST : Direction.WEST;
        return tz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    // ------------------------------------------------------------------ el ascensor (tubo del centro)

    private void ascensor(Canvas c) {
        int top = Y + TERRAZA;
        for (int x = cx - ASC - 1; x <= cx + ASC + 1; x++)
            for (int z = cz - ASC - 1; z <= cz + ASC + 1; z++) {
                if (!c.inXZ(x, z)) continue;
                int dx = Math.abs(x - cx), dz = Math.abs(z - cz);
                boolean esquina = dx == ASC + 1 && dz == ASC + 1;
                boolean borde = Math.max(dx, dz) == ASC + 1;
                for (int y = Y - 1; y < top; y++) {
                    int h = y - Y;
                    if (!borde) {
                        // el hueco: aire de arriba abajo (la plataforma la pone el ascensor), el fondo de piedra
                        c.set(x, y, z, h == -1 ? Pal.PB_CHISELED : Pal.AIR);
                        continue;
                    }
                    // la boca de abajo (en la planta baja, al sur) y la de arriba quedan abiertas
                    boolean boca = (h >= 0 && h < 4 && z == cz + ASC + 1 && dx <= 1);
                    if (boca) {
                        c.set(x, y, z, Pal.AIR);
                        continue;
                    }
                    if (esquina) c.set(x, y, z, h % 6 == 5 ? HUESO : Pal.b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                    else if (h % 6 == 5) c.set(x, y, z, HIERRO);
                    else c.set(x, y, z, (h / 2 + dx + dz) % 4 == 0 ? Pal.GLASS_RED : Pal.b(Blocks.IRON_BARS));
                }
            }
        // la runa de abajo (donde se espera) y el hueco abierto en la terraza
        c.set(cx, Y - 1, cz, Pal.b(Blocks.CRYING_OBSIDIAN));
    }

    // ------------------------------------------------------------------ las mazmorras (el sotano)

    private void mazmorras(Canvas c) {
        int ys = Y + SOTANO;
        // un pasillo norte-sur bajo la torre con celdas a los dos lados; alrededor del tubo del ascensor, dos salas de
        // guardia (al este, donde da la puerta de la escalera, y al oeste) que unen las dos mitades del pasillo
        for (int x = cx - AT + GT; x <= cx + AT - GT; x++)
            for (int z = cz - AT + GT; z <= cz + AT - GT; z++) {
                if (!c.inXZ(x, z) || octo(x - cx, z - cz) > AT - GT + 0.5) continue;
                int dx = x - cx, dz = z - cz;
                boolean sala = Math.abs(dz) <= ASC + 2 && Math.abs(dx) > ASC + 1;
                // las rejas de las celdas (en x = +-3) con su puerta; junto a las salas, abiertas
                if (Math.abs(dx) == 3 && Math.abs(dz) > ASC + 1) {
                    boolean abierta = Math.abs(dz) == ASC + 2 || Math.floorMod(dz, 4) == 1;
                    for (int y = ys; y < ys + 4; y++) c.set(x, y, z, abierta && y < ys + 2 ? Pal.AIR : Pal.IRON_BARS);
                }
                // separaciones entre celdas (no en las salas)
                if (!sala && Math.abs(dx) > 3 && Math.floorMod(dz, 4) == 3) for (int y = ys; y < ys + 4; y++) c.set(x, y, z, Pal.DS_BRICKS);
                // techo bajo
                c.set(x, ys + 4, z, Pal.DS_TILES);
                for (int y = ys + 5; y < Y - 1; y++) c.set(x, y, z, Pal.foundation(x, y, z));
                // lo que hay en las celdas: cadenas, paja podrida, huesos
                if (!sala && Math.abs(dx) > 4) {
                    int p = Hash.pct(x, ys, z, seed);
                    if (p < 10) c.set(x, ys, z, Pal.b(Blocks.HAY_BLOCK));
                    else if (p < 16) c.set(x, ys, z, Pal.b(Blocks.SKELETON_SKULL));
                    else if (p < 22) c.set(x, ys, z, hueso(Direction.Axis.Z));
                    else if (p < 28) c.set(x, ys + 3, z, Pal.CHAIN);
                    else if (p < 34) c.set(x, ys, z, Pal.COBWEB);
                }
            }
        // el tubo del ascensor no llega aqui: macizo
        for (int x = cx - ASC - 1; x <= cx + ASC + 1; x++)
            for (int z = cz - ASC - 1; z <= cz + ASC + 1; z++)
                for (int y = ys; y < ys + 4; y++) if (Math.max(Math.abs(x - cx), Math.abs(z - cz)) == ASC + 1) c.set(x, y, z, Pal.DS_BRICKS);
        // faroles colgando del techo del pasillo y de las salas
        for (int z = cz - 6; z <= cz + 6; z += 6) {
            if (Math.abs(z - cz) <= ASC + 1) continue;
            c.set(cx, ys + 3, z, Pal.SOUL_LANTERN_HANG);
        }
        for (int s2 = -1; s2 <= 1; s2 += 2) c.set(cx + s2 * 6, ys + 3, cz, Pal.SOUL_LANTERN_HANG);
        // las losas de los pinchos (cinceladas: el que se fija, las ve)
        for (int[] q : pinchos()) if (q[1] == ys) c.set(q[0], q[1] - 1, q[2], Pal.b(Blocks.CHISELED_DEEPSLATE));
        salaSecreta(c);
    }

    /**
     * La sala secreta (parte 5c): detras del muro falso del oeste del sotano (parece el muro de la torre; se abre al
     * golpearlo, fortaleza/Trampas), una cueva labrada en la roca de la isla con el tesoro del Maestre Velan.
     */
    private void salaSecreta(Canvas c) {
        int ys = Y + SOTANO;
        // el muro falso: el muro de siempre (se quita en el juego)
        for (int[] q : muroFalso()) c.set(q[0], q[1], q[2], piedra(q[0], q[1], q[2]));
        int x0 = cx - AT - 8, x1 = cx - AT - 1, z0 = cz - 3, z1 = cz + 3;
        for (int x = x0 - 1; x <= x1; x++)
            for (int z = z0 - 1; z <= z1 + 1; z++)
                for (int y = ys - 1; y <= ys + 4; y++) {
                    if (!c.inXZ(x, z)) continue;
                    boolean borde = x < x0 || z < z0 || z > z1 || y == ys - 1 || y == ys + 4;
                    if (x == x1 && !borde) {
                        // lo que queda entre el muro de la torre y la sala: el paso, de tres de ancho
                        c.set(x, y, z, Math.abs(z - cz) <= 1 && y < ys + 3 ? Pal.AIR : Pal.DS_BRICKS);
                        continue;
                    }
                    c.set(x, y, z, borde ? (y == ys - 1 ? Pal.DS_TILES_CRACKED : Pal.DS_BRICKS_CRACKED) : Pal.AIR);
                }
        // el tesoro: el cofre, velas, oro desparramado y los huesos del que lo guardaba
        int[] cf = cofreSecreto();
        try {
            c.chest(cf[0], cf[1], cf[2], Direction.EAST, new net.minecraft.resources.ResourceLocation(com.darkcraft.DarkCraftMod.MODID, "chests/fortaleza_secreto"));
        } catch (Throwable t) {
            c.set(cf[0], cf[1], cf[2], Pal.facing(Blocks.CHEST, Direction.EAST));
        }
        c.set(cf[0], cf[1], cf[2] - 2, Pal.b(Blocks.GOLD_BLOCK));
        c.set(cf[0], cf[1], cf[2] + 2, Pal.GILDED);
        c.set(cf[0] + 1, cf[1], cf[2] + 2, Pal.candles(Blocks.RED_CANDLE, 4));
        c.set(cf[0] + 1, cf[1], cf[2] - 2, Pal.candles(Blocks.RED_CANDLE, 3));
        c.set(cf[0] + 3, cf[1], cf[2] + 3, Pal.b(Blocks.SKELETON_SKULL));
        c.set(cf[0] + 3, cf[1], cf[2] + 2, hueso(Direction.Axis.Z));
        c.set(cf[0] + 2, cf[1] + 3, cf[2], Pal.SOUL_LANTERN_HANG);
    }

    // ------------------------------------------------------------------ cada planta

    private void plantas(Canvas c) {
        guardiasYArmeria(c, piso(0));
        capilla(c, piso(1));
        aposentos(c, piso(2), 0);
        aposentos(c, piso(3), 1);
        ruina(c, piso(4));
        for (int[] q : sueloQueCede()) if (c.inXZ(q[0], q[2])) c.set(q[0], q[1], q[2], Pal.DS_TILES_CRACKED);
        observatorio(c, piso(5));
    }

    private boolean dentro(int x, int z) {
        return octo(x - cx, z - cz) <= AT - GT + 0.5 && Math.max(Math.abs(x - cx), Math.abs(z - cz)) > ASC + 1;
    }

    /** La planta baja: al oeste la sala de guardias (mesas, chimenea), al este la armeria (yunques, barriles, el cofre). */
    private void guardiasYArmeria(Canvas c, int y) {
        // el tabique norte-sur por el este del ascensor, con su puerta
        for (int z = cz - AT + GT; z <= cz + AT - GT; z++) {
            if (!dentro(cx + ASC + 2, z) || Math.abs(z - cz) <= ASC + 1) continue;
            for (int h = 0; h < PISO - 1; h++) c.set(cx + ASC + 2, y + h, z, h < 3 && Math.abs(z - cz - 5) <= 0 ? Pal.AIR : Pal.wall(cx + ASC + 2, y + h, z));
        }
        // sala de guardias: dos mesas largas con bancos, barriles, la chimenea al oeste
        for (int z = cz - 5; z <= cz + 5; z += 10) {
            for (int x = cx - 7; x <= cx - 4; x++) {
                c.set(x, y, z, Pal.b(Blocks.DARK_OAK_PLANKS));
                c.set(x, y, z - 1, Pal.stairs(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, false));
                c.set(x, y, z + 1, Pal.stairs(Blocks.DARK_OAK_STAIRS, Direction.NORTH, false));
                if (Hash.pct(x, y, z, seed) < 30) c.set(x, y + 1, z, Pal.candles(Blocks.BLACK_CANDLE, 1 + Hash.h(x, 1, z, seed) % 3));
            }
        }
        for (int dz = -1; dz <= 1; dz++) {
            c.set(cx - AT + GT, y, cz + dz, Pal.CAMPFIRE);
            for (int h = 1; h < PISO - 1; h++) c.set(cx - AT + GT, y + h, cz + dz, Pal.b(Blocks.BLACKSTONE));
        }
        // armeria: yunques, piedra de afilar, barriles y el cofre del acertijo
        int[] cf = cofre();
        c.set(cx + 7, y, cz + 3, Pal.b(Blocks.ANVIL));
        c.set(cx + 7, y, cz - 5, Pal.b(Blocks.SMITHING_TABLE));
        c.set(cx + 4, y, cz + 6, Pal.b(Blocks.GRINDSTONE));
        c.set(cx + 5, y, cz - 6, Pal.b(Blocks.BARREL));
        c.set(cx + 6, y, cz - 6, Pal.b(Blocks.BARREL));
        c.set(cx + 5, y + 1, cz - 6, Pal.b(Blocks.BARREL));
        c.set(cx + 4, y, cz - 3, Pal.b(Blocks.CRAFTING_TABLE));
        try {
            c.chest(cf[0], cf[1], cf[2], Direction.WEST, new net.minecraft.resources.ResourceLocation(com.darkcraft.DarkCraftMod.MODID, "chests/acertijo"));
        } catch (Throwable t) {
            c.set(cf[0], cf[1], cf[2], Pal.facing(Blocks.CHEST, Direction.WEST));
        }
        // estandartes en los muros: rojo y negro
        for (int z = cz - 4; z <= cz + 4; z += 8) {
            c.set(cx + AT - GT, y + 5, z, Pal.b(Blocks.RED_WOOL));
            c.set(cx - AT + GT, y + 5, z + 2, Pal.b(Blocks.BLACK_WOOL));
        }
    }

    /** La capilla: bancos mirando al norte, el altar con velas y el rosetón rojo. */
    private void capilla(Canvas c, int y) {
        for (int z = cz + 2; z <= cz + 7; z += 2)
            for (int x = cx - 6; x <= cx + 6; x++) {
                if (Math.abs(x - cx) <= ASC + 2 || !dentro(x, z)) continue;
                c.set(x, y, z, Pal.stairs(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, false));
            }
        // el altar al norte (detras del tubo)
        for (int x = cx - 2; x <= cx + 2; x++) {
            c.set(x, y, cz - 6, Pal.PB_CHISELED);
            c.set(x, y + 1, cz - 6, Math.abs(x - cx) == 2 ? Pal.candles(Blocks.RED_CANDLE, 3) : Pal.b(Blocks.RED_CARPET));
        }
        c.set(cx, y + 1, cz - 7, Pal.b(Blocks.WITHER_SKELETON_SKULL));
        for (int x = cx - 1; x <= cx + 1; x++)
            for (int z = cz - 5; z <= cz - 4; z++) c.set(x, y - 1, z, Pal.b(Blocks.RED_WOOL));
        // candelabros
        for (int s = -1; s <= 1; s += 2) {
            c.set(cx + s * 5, y, cz - 5, Pal.b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            c.set(cx + s * 5, y + 1, cz - 5, Pal.candles(Blocks.BLACK_CANDLE, 4));
        }
    }

    /** Los aposentos: camas con dosel, armarios (libreria vacia = barriles), un escritorio. */
    private void aposentos(Canvas c, int y, int v) {
        int[][] camas = {{-6, -4}, {6, -4}, {-6, 4}, {6, 4}};
        for (int i = 0; i < camas.length; i++) {
            int x = cx + camas[i][0], z = cz + camas[i][1];
            if (!dentro(x, z)) continue;
            Direction cab = camas[i][0] < 0 ? Direction.WEST : Direction.EAST;
            BlockState cama = (i + v) % 2 == 0 ? Pal.b(Blocks.RED_BED) : Pal.b(Blocks.BLACK_BED);
            c.set(x, y, z, cama.setValue(net.minecraft.world.level.block.BedBlock.FACING, cab.getOpposite())
                    .setValue(net.minecraft.world.level.block.BedBlock.PART, net.minecraft.world.level.block.state.properties.BedPart.FOOT));
            c.set(x + cab.getStepX(), y, z, cama.setValue(net.minecraft.world.level.block.BedBlock.FACING, cab.getOpposite())
                    .setValue(net.minecraft.world.level.block.BedBlock.PART, net.minecraft.world.level.block.state.properties.BedPart.HEAD));
            // dosel de valla y alfombra
            for (int dz = -1; dz <= 1; dz += 2) {
                for (int h = 0; h < 3; h++) c.set(x, y + h, z + dz, Pal.DO_FENCE);
                c.set(x, y + 3, z + dz, Pal.DO_PLANKS);
            }
            c.set(x, y + 3, z, Pal.b(Blocks.RED_WOOL));
        }
        for (int x = cx - 2; x <= cx + 2; x++) c.set(x, y, cz + 7, Pal.b(Blocks.BARREL));
        c.set(cx + 4, y, cz - 7, Pal.b(Blocks.SPRUCE_PLANKS));
        c.set(cx + 5, y, cz - 7, Pal.b(Blocks.SPRUCE_PLANKS));
        c.set(cx + 4, y + 1, cz - 7, Pal.candles(Blocks.WHITE_CANDLE, 2));
    }

    /** La planta de encima de la cupula: la mitad del techo se vino abajo (entra la lluvia), escombros y vigas caidas. */
    private void ruina(Canvas c, int y) {
        for (int x = cx - AT + GT; x <= cx + AT - GT; x++)
            for (int z = cz - AT + GT; z <= cz + AT - GT; z++) {
                if (!dentro(x, z)) continue;
                // el techo (el suelo de la planta de arriba) cae por el oeste
                if (x - cx < -1 && Hash.pct(x, 21, z, seed) < 80) c.set(x, y + PISO - 1, z, Pal.AIR);
                if (x - cx < -1 && Hash.pct(x, 22, z, seed) < 30) c.set(x, y, z, Pal.DS_COBBLED);
            }
        // dos vigas caidas en diagonal
        for (int i = 0; i < 7; i++) {
            c.set(cx - 7 + i, y + i / 2, cz - 6 + i / 3, Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.X));
            c.set(cx - 7 + i / 3, y + i / 2, cz + 6 - i, Pal.log(Blocks.DARK_OAK_LOG, Direction.Axis.Z));
        }
    }

    /** El observatorio: el telescopio de cobre por una ventana grande al norte, mapas y la esfera armilar. */
    private void observatorio(Canvas c, int y) {
        // la ventana grande al norte: sin cristal
        for (int h = 2; h <= 7; h++)
            for (int x = cx - 2; x <= cx + 2; x++)
                for (int g = 0; g < GT; g++) c.set(x, y + h, cz - AT + g, Pal.AIR);
        // el telescopio: tubo de cobre que sale inclinado por la ventana
        for (int i = 0; i < 8; i++) {
            int x = cx, z = cz - 4 - i, yy = y + 2 + i / 2;
            c.set(x, yy, z, i == 7 ? Pal.b(Blocks.TINTED_GLASS) : Pal.b(Blocks.OXIDIZED_CUT_COPPER));
        }
        c.set(cx, y, cz - 4, Pal.b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
        c.set(cx, y + 1, cz - 4, Pal.b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
        // la esfera armilar (cadenas y un nucleo) y mesas con mapas
        c.set(cx - 6, y, cz + 4, Pal.PB_CHISELED);
        c.set(cx - 6, y + 1, cz + 4, Pal.CHAIN);
        c.set(cx - 6, y + 2, cz + 4, Pal.b(Blocks.LODESTONE));
        c.set(cx + 6, y, cz + 2, Pal.b(Blocks.CARTOGRAPHY_TABLE));
        c.set(cx + 6, y, cz - 2, Pal.b(Blocks.CARTOGRAPHY_TABLE));
    }

    // ------------------------------------------------------------------ la terraza del portal

    private void terraza(Canvas c) {
        int t = Y + TERRAZA;
        // las mensulas: de la torre al cuadrado de la terraza, en las cinco filas de debajo
        for (int x = Math.max(cx - TL, c.minX); x <= Math.min(cx + TL, c.maxX); x++)
            for (int z = Math.max(cz - TL, c.minZ); z <= Math.min(cz + TL, c.maxZ); z++) {
                int dx = x - cx, dz = z - cz;
                // la torrecilla de la escalera sigue hasta su tejado (sale a la terraza por su puerta del oeste)
                if (Math.hypot(x - (cx + TX), z - cz) <= RTU + 0.5) continue;
                double o = octo(dx, dz);
                int m = Math.max(Math.abs(dx), Math.abs(dz));
                for (int k = 1; k <= 5; k++) {
                    // a mas abajo, mas cerca de la torre
                    double alcance = AT + (TL - AT) * (6 - k) / 5.0;
                    if (o > AT && m <= alcance && o <= alcance + 1.5) c.set(x, t - 1 - k, z, k == 1 ? Pal.PB_BRICKS : Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS,
                            Math.abs(dx) >= Math.abs(dz) ? (dx > 0 ? Direction.WEST : Direction.EAST) : (dz > 0 ? Direction.NORTH : Direction.SOUTH), true));
                }
                // el suelo de la terraza; el hueco del ascensor tapado con barrera (la placa pasa; encima se la llama)
                boolean tubo = Math.max(Math.abs(dx), Math.abs(dz)) <= ASC;
                c.set(x, t - 1, z, tubo ? Pal.b(Blocks.BARRIER) : ((Math.floorDiv(x, 2) + Math.floorDiv(z, 2)) & 1) == 0 ? Pal.PB_BRICKS : Pal.DS_TILES);
                for (int y = t; y <= t + AALTO + 4; y++) c.set(x, y, z, Pal.AIR);
                // el pretil con almenas y pinaculos de hueso en las esquinas
                if (m == TL) {
                    c.set(x, t, z, Pal.wall(x, t, z));
                    if (((dx + dz) & 1) == 0) c.set(x, t + 1, z, Pal.wall(x, t + 1, z));
                    if (Math.abs(dx) == TL && Math.abs(dz) == TL)
                        for (int y = t + 1; y <= t + 6; y++) c.set(x, y, z, y == t + 6 ? Pal.b(Blocks.END_ROD) : HUESO);
                }
            }
        // el portal de la Sombra (parte 5e): el arco con su marco labrado, los pilares de las cadenas, las estatuas
        // encapuchadas y el pedestal del amuleto
        marcoPortal(c, t);
        for (int sg = -1; sg <= 1; sg += 2) {
            pilarPortal(c, cx + sg * PILAR, t, cz + AZ, sg);
            estatua(c, cx + sg * EX, t, cz + EZ);
        }
        pedestal(c, t);
        // el barandal del hueco del ascensor (menos por el sur, por donde se sale)
        for (int dx = -ASC - 1; dx <= ASC + 1; dx++)
            for (int dz = -ASC - 1; dz <= ASC + 1; dz++)
                if (Math.max(Math.abs(dx), Math.abs(dz)) == ASC + 1 && !(dz == ASC + 1 && Math.abs(dx) <= 1))
                    c.unir(cx + dx, t, cz + dz, Pal.b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
    }

    // ------------------------------------------------------------------ el portal de la Sombra (parte 5e)

    /** Los pilares de las cadenas, a cada lado del portal (x = cx +- PILAR, en su plano), y la fila de su capitel. */
    public static final int PILAR = 10, PILAR_ALTO = 11;
    /** Las estatuas encapuchadas: x = cx +- EX, z = cz + EZ; miran al norte, al portal. */
    public static final int EX = 9, EZ = 7;
    /** Las filas (sobre la terraza) de las cadenas que sujetan el marco a los pilares. */
    private static final int[] CADENAS = {3, 7, 10};
    /** Las filas de las gemas de los lados del marco (la clave va arriba del todo, en AALTO + 1). */
    private static final int[] GEMAS = {2, 5, 8};
    /** Medio ancho de lo que ocupa el marco (con el zocalo). */
    public static final int MARCO = AX + 3;

    private static final int[][] ANILLOS = anillos();

    /** Distancia (en el plano del portal, a lo ajedrez) de cada sitio al hueco del arco: 0 el hueco. */
    private static int[][] anillos() {
        int[][] o = new int[2 * MARCO + 1][AALTO + 4];
        for (int dx = -MARCO; dx <= MARCO; dx++)
            for (int h = 0; h < AALTO + 4; h++) {
                int d = 99;
                for (int hh = 0; hh < AALTO; hh++) {
                    int w = arcoPortal(hh);
                    int x = Math.max(-w, Math.min(w, dx));
                    d = Math.min(d, Math.max(Math.abs(dx - x), Math.abs(h - hh)));
                }
                o[dx + MARCO][h] = d;
            }
        return o;
    }

    /** El anillo del marco en (dx, h) del plano del portal: 0 el hueco, 1 el de hueso labrado, 2 el de piedra; -1 nada. */
    public static int anillo(int dx, int h) {
        if (h < 0 || h >= AALTO + 4 || Math.abs(dx) > MARCO) return -1;
        int d = ANILLOS[dx + MARCO][h];
        return d <= 2 ? d : -1;
    }

    /** Lo que ocupa el marco a cada lado en la fila h (el borde de fuera del anillo de piedra), o -1. */
    private static int bordeMarco(int h) {
        for (int dx = MARCO; dx >= 0; dx--) if (anillo(dx, h) > 0) return dx;
        return -1;
    }

    /**
     * El marco del arco apuntado (respuestas 51 y 52): tres de hondo; por dentro un anillo de hueso labrado, por fuera
     * otro de piedra de sombra con sus runas (con las gemas rojas, apagadas hasta que se abre, a los lados y en la clave),
     * zocalo de hierro negro y un remate en la punta.
     */
    private void marcoPortal(Canvas c, int t) {
        int z = cz + AZ;
        for (int dx = -MARCO; dx <= MARCO; dx++)
            for (int h = 0; h <= AALTO + 4; h++) {
                int x = cx + dx, a = anillo(dx, h), w = arcoPortal(h);
                if (a == 0) {
                    c.set(x, t + h, z, Pal.AIR);
                    continue;
                }
                for (int g = -1; g <= 1; g++) {
                    BlockState b = null;
                    if (a == 1) b = h == 0 ? hierroNegro() : huesoLabrado(w >= 0 && Math.abs(dx) == w + 1 ? Direction.Axis.Y : Direction.Axis.X);
                    else if (a == 2) {
                        boolean gemaLado = Math.abs(dx) == AX + 2 && contiene(GEMAS, h), clave = dx == 0 && h == AALTO + 1;
                        if (h <= 1) b = hierroNegro();
                        else if ((gemaLado || clave) && g != 0) b = gema(false);
                        else b = piedraSombra();
                    } else if (h == 0 && Math.abs(dx) == MARCO) b = hierroNegro();               // el zocalo
                    else if (h == AALTO + 2 && Math.abs(dx) <= 1) b = hierroNegro();           // el remate
                    else if (h == AALTO + 3 && dx == 0 && g == 0) b = huesoLabrado(Direction.Axis.Y);
                    if (b != null) c.set(x, t + h, z + g, b);
                }
            }
    }

    private static boolean contiene(int[] v, int n) {
        for (int k : v) if (k == n) return true;
        return false;
    }

    /** Las cadenas de un lado: del borde del marco al brazo del pilar, en las filas de CADENAS. {x, y, z}. */
    private int[][] cadenas(int sg, int t) {
        List<int[]> o = new ArrayList<>();
        for (int h : CADENAS)
            for (int dx = bordeMarco(h) + 1; dx <= PILAR - 2; dx++) o.add(new int[]{cx + sg * dx, t + h, cz + AZ});
        return o.toArray(new int[0][]);
    }

    /**
     * Un pilar de las cadenas (respuesta 55): fuste en cruz de piedra de sombra con bandas de hierro negro, capitel de
     * hierro, punta de hueso con una gema; las cadenas lo atan al marco (se rompen al abrirse: {@link #alAbrir}).
     */
    private void pilarPortal(Canvas c, int px, int t, int pz, int sg) {
        for (int a = -1; a <= 1; a++)
            for (int b = -1; b <= 1; b++) {
                c.set(px + a, t, pz + b, hierroNegro());
                c.set(px + a, t + PILAR_ALTO, pz + b, hierroNegro());
                if (Math.abs(a) + Math.abs(b) > 1) continue;
                for (int h = 1; h < PILAR_ALTO; h++)
                    c.set(px + a, t + h, pz + b, (h == 4 || h == 8) && (a != 0 || b != 0) ? hierroNegro() : piedraSombra());
            }
        c.set(px, t + PILAR_ALTO + 1, pz, huesoLabrado(Direction.Axis.Y));
        c.set(px, t + PILAR_ALTO + 2, pz, gema(false));
        BlockState cadena = Blocks.CHAIN.defaultBlockState().setValue(net.minecraft.world.level.block.ChainBlock.AXIS, Direction.Axis.X);
        for (int[] q : cadenas(sg, t)) c.set(q[0], q[1], q[2], cadena);
    }

    /**
     * Una estatua encapuchada gigante (respuesta 55): 16 de alto, de pie sobre su zocalo, con las manos sobre una espada de
     * hueso clavada delante; mira al norte (al portal). Bajo la capucha, oscuridad y dos gemas por ojos (se encienden al
     * abrirse el portal, como el pomo de la espada).
     */
    private void estatua(Canvas c, int sx, int t, int sz) {
        // (a: a lo ancho; b: hacia atras, el norte es b < 0; h: sobre la terraza)
        for (int a = -3; a <= 3; a++)
            for (int b = -3; b <= 2; b++)
                for (int h = 0; h <= 16; h++) {
                    BlockState st = estatua(sx + a, t + h, sz + b, a, b, h);
                    if (st != null) c.set(sx + a, t + h, sz + b, st);
                }
    }

    private BlockState estatua(int x, int y, int z, int a, int b, int h) {
        int aa = Math.abs(a);
        // el zocalo: hierro negro por el borde, piedra negra dentro
        if (h == 0) return aa <= 3 && b >= -3 && b <= 2 ? (aa == 3 || b == -3 || b == 2 ? hierroNegro() : Pal.PB_BRICKS) : null;
        // la espada de hueso, clavada delante (b = -3), con la guarda, el puno, las manos y el pomo
        if (b == -3) {
            if (a == 0 && h <= 5) return huesoLabrado(Direction.Axis.Y);
            if (aa <= 1 && h == 6) return hierroNegro();
            if (a == 0 && h == 7) return hierroNegro();
            if (aa == 1 && h == 7) return Pal.DS_TILES;
            if (a == 0 && h == 8) return gema(false);
            return null;
        }
        // los brazos: de los hombros, por los lados, hasta las manos
        if (aa == 3 && b == 0 && (h == 7 || h == 8)) return Pal.DS_TILES;
        if (aa == 3 && b == -1 && h == 7) return Pal.DS_TILES;
        if (aa == 2 && b == -2 && h == 7) return Pal.DS_TILES;
        // el manto: ancho abajo, luego el cuerpo y los hombros
        int ancho, del, det = 1;
        if (h == 1) { ancho = 3; del = -2; }
        else if (h == 2) { ancho = 2; del = -2; }
        else if (h <= 8) { ancho = 2; del = -1; }
        else if (h <= 10) { ancho = 3; del = -1; }
        else if (h <= 13) { ancho = 2; del = -2; }
        else if (h == 14) { ancho = 1; del = -1; det = 0; }
        else if (h == 15) { ancho = 0; del = 0; det = 0; }
        else return null;
        if (aa > ancho || b < del || b > det) return null;
        // los hombros y la capucha en pendiente (escalones con lo alto hacia dentro)
        Direction dentro = a > 0 ? Direction.WEST : Direction.EAST;
        if (h == 10 && aa == 3) return Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, dentro, false);
        if ((h == 13 && aa == 2) || (h == 14 && aa == 1)) return Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, dentro, false);
        if (h == 15) return Pal.b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
        if (h >= 11) {
            // la capucha: su borde por delante (b = -2) solo en los lados y por arriba; dentro, la oscuridad y los ojos
            if (b == -2 && aa <= 1 && h <= 12) return null;
            if (b == -1 && aa <= 1 && h <= 12) return h == 11 && aa == 1 ? gema(false) : Pal.b(Blocks.BLACK_CONCRETE);
            return Hash.pct(x, y, z, seed ^ 0x77) < 18 ? Pal.PB_BRICKS_CRACKED : Pal.PB_BRICKS;
        }
        if (h >= 9) return Pal.PB_BRICKS;
        // los pliegues del manto
        if (Hash.pct(x, y, z, seed ^ 0x78) < 10) return Pal.DS_TILES_CRACKED;
        return (a & 1) == 0 ? Pal.DS_TILES : Pal.DS_POLISHED;
    }

    /**
     * El pedestal del amuleto (respuesta 56): zocalo de hierro negro, columna de hueso labrado con sus contrafuertes y
     * arriba el hueco donde se queda el amuleto (flotando y girando, desde que abre el portal). A los lados, dos braseros.
     */
    private void pedestal(Canvas c, int t) {
        int[] al = altar();
        int x = al[0], y = al[1], z = al[2];
        for (int a = -1; a <= 1; a++)
            for (int b = -1; b <= 1; b++) c.set(x + a, y, z + b, hierroNegro());
        c.set(x, y + 1, z, huesoLabrado(Direction.Axis.Y));
        for (Direction d : Direction.Plane.HORIZONTAL) {
            c.set(x + d.getStepX(), y + 1, z + d.getStepZ(), Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, d.getOpposite(), false));
            c.set(x + d.getStepX(), y + 2, z + d.getStepZ(), Pal.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, false));
        }
        c.set(x, y + 2, z, Pal.AIR);
        for (int sg = -1; sg <= 1; sg += 2) {
            c.set(x + sg * 3, y, z, hierroNegro());
            c.set(x + sg * 3, y + 1, z, huesoLabrado(Direction.Axis.Y));
            c.set(x + sg * 3, y + 2, z, Pal.SOUL_LANTERN);
        }
    }

    /** Lo que cambia al abrirse el portal: el bloque de antes y el de despues, y en que momento (fase) de la apertura. */
    public record Cambio(int x, int y, int z, BlockState antes, BlockState despues, int fase) {}

    /** Fases de la apertura: las gemas del marco, los ojos de las estatuas, las cadenas, lo alto de los pilares, cascotes. */
    public static final int GEMAS_MARCO = 0, OJOS = 1, CADENA = 2, PILARES = 3, CASCOTES = 4;

    /**
     * Todo lo que cambia al abrirse el portal (respuestas 52 y 55): se encienden las gemas (las del marco y los ojos y los
     * pomos de las estatuas), se rompen las cadenas y lo alto de los pilares, y caen cascotes a la terraza. Sirve para
     * abrirlo ({@code FortalezaEventos.abrirPortal}), para dejarlo abierto en un chunk que se vuelve a levantar y para
     * cerrarlo (el comando de reiniciar).
     */
    public List<Cambio> alAbrir() {
        int t = Y + TERRAZA, z = cz + AZ;
        List<Cambio> o = new ArrayList<>();
        BlockState aire = Pal.AIR, apagada = gema(false), encendida = gema(true);
        // las gemas del marco, de abajo arriba, y la clave
        for (int h : GEMAS)
            for (int sg = -1; sg <= 1; sg += 2)
                for (int g = -1; g <= 1; g += 2) o.add(new Cambio(cx + sg * (AX + 2), t + h, z + g, apagada, encendida, GEMAS_MARCO));
        for (int g = -1; g <= 1; g += 2) o.add(new Cambio(cx, t + AALTO + 1, z + g, apagada, encendida, GEMAS_MARCO));
        BlockState cadena = Blocks.CHAIN.defaultBlockState().setValue(net.minecraft.world.level.block.ChainBlock.AXIS, Direction.Axis.X);
        for (int sg = -1; sg <= 1; sg += 2) {
            // los ojos y el pomo de la espada de cada estatua
            int sx = cx + sg * EX, sz = cz + EZ;
            o.add(new Cambio(sx - 1, t + 11, sz - 1, apagada, encendida, OJOS));
            o.add(new Cambio(sx + 1, t + 11, sz - 1, apagada, encendida, OJOS));
            o.add(new Cambio(sx, t + 8, sz - 3, apagada, encendida, OJOS));
            for (int[] q : cadenas(sg, t)) o.add(new Cambio(q[0], q[1], q[2], cadena, aire, CADENA));
            // lo alto del pilar salta: el capitel, la punta y la gema, y los brazos de la fila de debajo
            int px = cx + sg * PILAR;
            for (int a = -1; a <= 1; a++)
                for (int b = -1; b <= 1; b++) {
                    o.add(new Cambio(px + a, t + PILAR_ALTO, z + b, hierroNegro(), aire, PILARES));
                    if (Math.abs(a) + Math.abs(b) == 1) o.add(new Cambio(px + a, t + PILAR_ALTO - 1, z + b, piedraSombra(), aire, PILARES));
                }
            o.add(new Cambio(px, t + PILAR_ALTO - 1, z, piedraSombra(), Pal.DS_COBBLED, PILARES));
            o.add(new Cambio(px, t + PILAR_ALTO + 1, z, huesoLabrado(Direction.Axis.Y), aire, PILARES));
            o.add(new Cambio(px, t + PILAR_ALTO + 2, z, apagada, aire, PILARES));
            // los cascotes, en la terraza alrededor del pilar (ni en su zocalo, ni en el pretil, ni en el del marco)
            int[][] cas = {{8, 2}, {9, 3}, {11, 2}, {12, 0}, {8, -1}, {12, 3}};
            BlockState[] que = {Pal.DS_COBBLED, piedraSombra(), hierroNegro(), Pal.slab(Blocks.COBBLED_DEEPSLATE_SLAB, false), Pal.PB_BRICKS_CRACKED,
                    Pal.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, false)};
            for (int k = 0; k < cas.length; k++) o.add(new Cambio(cx + sg * cas[k][0], t, z + cas[k][1], aire, que[k], CASCOTES));
        }
        return o;
    }

    // ------------------------------------------------------------------ lo que se vino abajo

    /** Agujeros en los suelos (con escombros en el de abajo), columnas caidas y el musgo de las ruinas. */
    private void derrumbes(Canvas c) {
        for (int p = 1; p < NPISOS; p++) {
            int ys = piso(p) - 1;
            // un agujero por planta, lejos del tubo y de la puerta de la escalera
            double a = p * 2.3 + 0.7;
            int hx = cx + (int) Math.round(Math.cos(a) * 5.5), hz = cz + (int) Math.round(Math.sin(a) * 5.5);
            double rr = 1.8 + Hash.f(p, 1, 2, seed) * 1.2;
            for (int x = hx - 3; x <= hx + 3; x++)
                for (int z = hz - 3; z <= hz + 3; z++) {
                    if (!dentro(x, z) || x > cx + AT - GT - 2 && Math.abs(z - cz) <= 1) continue;
                    double d = Math.hypot(x - hx, z - hz) + Hash.f(x, ys, z, seed) * 0.8;
                    if (d < rr) {
                        c.set(x, ys, z, Pal.AIR);
                        // los cascotes abajo
                        if (Hash.pct(x, ys, z, seed) < 55 && c.isAir(x, piso(p - 1), z)) c.set(x, piso(p - 1), z, Hash.pct(x, 1, z, seed) < 50 ? Pal.DS_COBBLED : Pal.slab(Blocks.COBBLED_DEEPSLATE_SLAB, false));
                    } else if (d < rr + 0.8 && Hash.pct(x, 2, z, seed) < 40) c.set(x, ys, z, Pal.DS_TILES_CRACKED);
                }
        }
    }

    /** Enredaderas por fuera de la torre y colgando dentro de la cupula, musgo y liquen que brilla por dentro. */
    private void vegetacion(Canvas c) {
        int R = RD;
        for (int x = Math.max(cx - R, c.minX); x <= Math.min(cx + R, c.maxX); x++)
            for (int z = Math.max(cz - R, c.minZ); z <= Math.min(cz + R, c.maxZ); z++) {
                if (Hash.pct(x, 31, z, seed) > 7) continue;
                // lianas que cuelgan del techo de la cupula
                double rh = Math.hypot(x - cx, z - cz);
                if (rh > RD - 3 || octo(x - cx, z - cz) <= AT + 1) continue;
                int techo = Y + (int) Math.floor(Math.sqrt(Math.max(0, (RD - 1.3) * (RD - 1.3) - rh * rh)));
                if (c.isAir(x, techo - 1, z) && !c.isAir(x, techo, z)) {
                    int largo = 2 + Hash.h(x, 32, z, seed) % 9;
                    for (int y = techo - 1; y >= techo - largo && y > Y + 2 && c.isAir(x, y, z); y--)
                        c.set(x, y, z, y == techo - largo ? Pal.b(Blocks.CAVE_VINES).setValue(net.minecraft.world.level.block.CaveVines.BERRIES, Hash.pct(x, y, z, seed) < 40)
                                : Pal.b(Blocks.CAVE_VINES_PLANT).setValue(net.minecraft.world.level.block.CaveVines.BERRIES, Hash.pct(x, y, z, seed) < 25));
                }
            }
        // enredaderas por la cara de fuera de la torre (por debajo de la cupula y algo por encima)
        for (int x = Math.max(cx - AT - 1, c.minX); x <= Math.min(cx + AT + 1, c.maxX); x++)
            for (int z = Math.max(cz - AT - 1, c.minZ); z <= Math.min(cz + AT + 1, c.maxZ); z++) {
                double o = octo(x - cx, z - cz);
                if (o <= AT + 0.5 || o > AT + 1.5) continue;
                if (Hash.pct(x, 33, z, seed) > 45) continue;
                Direction cara = Math.abs(x - cx) >= Math.abs(z - cz) ? (x > cx ? Direction.WEST : Direction.EAST) : (z > cz ? Direction.NORTH : Direction.SOUTH);
                int alto = 6 + Hash.h(x, 34, z, seed) % 40;
                for (int y = Y + alto; y >= Y && c.isAir(x, y, z); y--) {
                    BlockState v = Pal.b(Blocks.VINE).setValue(VineBlock.getPropertyForFace(cara), true);
                    c.set(x, y, z, v);
                }
            }
        // liquen que brilla y musgo en los suelos de la torre
        for (int p = 0; p < NPISOS; p++) {
            int y = piso(p);
            for (int x = cx - AT + GT; x <= cx + AT - GT; x++)
                for (int z = cz - AT + GT; z <= cz + AT - GT; z++) {
                    if (!c.inXZ(x, z) || !dentro(x, z) || !c.isAir(x, y, z)) continue;
                    int q = Hash.pct(x, y, z, seed ^ 0x77);
                    if (q < 6) c.set(x, y, z, Pal.b(Blocks.MOSS_CARPET));
                    else if (q < 8 && !c.get(x, y - 1, z).isAir()) c.set(x, y, z, Pal.b(Blocks.BROWN_MUSHROOM));
                }
        }
    }

    /** Luz invisible repartida por la torre, la torrecilla, las mazmorras y la puerta (lo que se ve son los faroles). */
    private void luces(Canvas c) {
        for (int p = 0; p < NPISOS; p++)
            for (int[] d : new int[][]{{-6, -5}, {6, -5}, {-6, 5}, {6, 5}}) luzSi(c, cx + d[0], piso(p) + 4, cz + d[1], 10);
        for (int y = Y + SOTANO + 2; y < Y + TERRAZA; y += 6) luzSi(c, cx + TX + 2, y, cz + 2, 9);
        for (int z = cz - 6; z <= cz + 6; z += 4) luzSi(c, cx, Y + SOTANO + 2, z, 9);
        luzSi(c, cx, Y + 5, cz + PZ - 3, 10);
        luzSi(c, cx, Y + PALTO + 3, cz + PZ - 1, 9);
        for (int k = 0; k < 12; k++) {
            double a = k * Math.PI * 2 / 12;
            luzSi(c, cx + (int) Math.round(Math.cos(a) * 26), Y + 4, cz + (int) Math.round(Math.sin(a) * 26), 8);
        }
    }
}
