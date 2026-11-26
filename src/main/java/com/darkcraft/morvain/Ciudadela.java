package com.darkcraft.morvain;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * La ciudadela encadenada en el cielo de Morvain (respuesta 88 del plan), la referencia que se ve desde casi todo el mapa:
 * una isla de roca negra flotando sobre el crater (y sobre el peñon de la Sombra), con su muralla octogonal y ocho torres
 * de chapitel, la torre del homenaje con cuatro torreones y, encima, una aguja altisima con una gema roja en la punta.
 * Ocho cadenas enormes (eslabones de 41 bloques de hierro negro) la atan a sus anclas, en la ladera de dentro del crater.
 * De momento solo se ve (se entra mucho mas adelante). Se escribe al generar el terreno y sin estado, como {@link Terreno}:
 * Distant Horizons la dibuja desde lejos.
 */
final class Ciudadela {
    private Ciudadela() {}

    /** El suelo de la isla (el patio) y su radio. */
    static final int TOP = 540;
    static final double RADIO = 102;
    /** Lo que ocupa la isla con todo lo de encima (sin las cadenas): de -LADO a LADO en x y en z. */
    static final int LADO = 116;
    /** La muralla: octogono de apotema A, 3 de grueso y MURO de alto. */
    static final int A = 78, MURO = 24;
    /** Las torres de la muralla, en los vertices del octogono: radio, alto hasta el adarve y su chapitel. */
    static final double RV = A / Math.cos(Math.PI / 8), RT = 8;
    static final int TORRE = 44, CHAPITEL = 30;
    /** El homenaje: medio lado H, esquinas cortadas (|x| + |z| <= CHAFLAN) y K de alto. */
    static final int H = 26, CHAFLAN = 44, K = 84;
    /** Sus cuatro torreones, en (+-TT, +-TT): radio, alto y chapitel. */
    static final int TT = 21;
    static final double RTO = 6;
    static final int TORREON = 104, CHAPITEL_T = 30;
    /** La aguja del centro, sobre el homenaje: radio, el cuerpo y la punta. */
    static final double RAG = 11;
    static final int CUERPO = 42, PUNTA = 46;
    /** Lo mas alto (la gema de la punta de la aguja). */
    static final int CIMA = TOP + K + CUERPO + 2 + PUNTA + 5;

    // ------------------------------------------------------------------ las cadenas

    /** Los eslabones: medio largo de la parte recta, radio de las curvas, grueso (radio del hierro) y paso. */
    static final double HL = 10, RL = 8, TL = 2.5, PASO = 31;
    /** Donde estan las anclas (distancia al centro). */
    static final double ANCLA = 860;

    /** Una cadena: de la isla (a) a su ancla (b), con sus ejes y sus eslabones. */
    static final class Cadena {
        final double ax, ay, az, bx, by, bz, ux, uy, uz, n1x, n1y, n1z, n2x, n2y, n2z, largo, s0;
        final int eslabones, suelo;
        final int x0, x1, y0, y1, z0, z1;

        Cadena(double ang) {
            double c = Math.cos(ang), s = Math.sin(ang);
            ax = (RV + 4) * c;
            az = (RV + 4) * s;
            ay = TOP - 16;
            bx = ANCLA * c;
            bz = ANCLA * s;
            suelo = (int) Math.floor(Terreno.columna(bx, bz).altura());
            by = suelo + 9;
            double dx = bx - ax, dy = by - ay, dz = bz - az;
            largo = Math.sqrt(dx * dx + dy * dy + dz * dz);
            ux = dx / largo;
            uy = dy / largo;
            uz = dz / largo;
            // n1: horizontal, perpendicular a la cadena; n2 = u x n1
            double hx = -uz, hz = ux, hl = Math.hypot(hx, hz);
            n1x = hx / hl;
            n1y = 0;
            n1z = hz / hl;
            n2x = uy * n1z - uz * n1y;
            n2y = uz * n1x - ux * n1z;
            n2z = ux * n1y - uy * n1x;
            // el ultimo eslabon acaba justo en el ancla; el primero, metido en la roca de la isla
            eslabones = (int) Math.floor((largo - (HL + RL)) / PASO) + 1;
            s0 = largo - (HL + RL) - (eslabones - 1) * PASO;
            double m = HL + RL + TL + 1;
            x0 = (int) Math.floor(Math.min(ax, bx) - m) - 12;
            x1 = (int) Math.ceil(Math.max(ax, bx) + m) + 12;
            z0 = (int) Math.floor(Math.min(az, bz) - m) - 12;
            z1 = (int) Math.ceil(Math.max(az, bz) + m) + 12;
            y0 = (int) Math.floor(Math.min(ay, by) - m) - 8;
            y1 = (int) Math.ceil(Math.max(ay, by) + m);
        }

        boolean toca(int cx0, int cz0) {
            return cx0 + 15 >= x0 && cx0 <= x1 && cz0 + 15 >= z0 && cz0 <= z1;
        }

        /** El centro del eslabon i. */
        double[] centro(int i) {
            double s = s0 + i * PASO;
            return new double[]{ax + ux * s, ay + uy * s, az + uz * s};
        }

        /** Si el punto (x, y, z) es hierro del eslabon i (los pares tumbados, los impares de pie). */
        boolean hierro(int i, double[] c, double x, double y, double z) {
            double dx = x - c[0], dy = y - c[1], dz = z - c[2];
            double a = dx * ux + dy * uy + dz * uz;
            double b1 = dx * n1x + dy * n1y + dz * n1z, b2 = dx * n2x + dy * n2y + dz * n2z;
            double w = (i & 1) == 0 ? b1 : b2, o = (i & 1) == 0 ? b2 : b1;
            double aa = Math.abs(a);
            double d = aa <= HL ? Math.abs(Math.abs(w) - RL) : Math.abs(Math.hypot(aa - HL, w) - RL);
            return d * d + o * o <= TL * TL;
        }

        /** Medio tamano de la caja del eslabon i en cada eje. */
        double[] caja(int i) {
            double la = HL + RL + TL, lw = RL + TL, lo = TL;
            boolean par = (i & 1) == 0;
            double wx = par ? n1x : n2x, wy = par ? n1y : n2y, wz = par ? n1z : n2z;
            double ox = par ? n2x : n1x, oy = par ? n2y : n1y, oz = par ? n2z : n1z;
            return new double[]{Math.abs(ux) * la + Math.abs(wx) * lw + Math.abs(ox) * lo + 1,
                    Math.abs(uy) * la + Math.abs(wy) * lw + Math.abs(oy) * lo + 1,
                    Math.abs(uz) * la + Math.abs(wz) * lw + Math.abs(oz) * lo + 1};
        }
    }

    /** Las ocho cadenas, de las ocho torres de la muralla. */
    static final Cadena[] CADENAS = cadenas();

    private static Cadena[] cadenas() {
        Cadena[] c = new Cadena[8];
        for (int k = 0; k < 8; k++) c[k] = new Cadena(k * Math.PI / 4);
        return c;
    }

    /** Si el chunk que empieza en (x0, z0) tiene algo de la ciudadela o de sus cadenas. */
    static boolean toca(int x0, int z0) {
        if (x0 + 15 >= -LADO && x0 <= LADO && z0 + 15 >= -LADO && z0 <= LADO) return true;
        for (Cadena c : CADENAS) if (c.toca(x0, z0)) return true;
        return false;
    }

    // ------------------------------------------------------------------ escribir un chunk

    /** Escribe lo que toque del chunk que empieza en (x0, z0) (solo dentro de el). */
    static void chunk(MorvainGenerador.Escritor e, int x0, int z0) {
        if (x0 + 15 >= -LADO && x0 <= LADO && z0 + 15 >= -LADO && z0 <= LADO)
            for (int dz = 0; dz < 16; dz++)
                for (int dx = 0; dx < 16; dx++) columna(e, x0 + dx, z0 + dz);
        for (Cadena c : CADENAS)
            if (c.toca(x0, z0)) {
                cadena(e, c, x0, z0);
                ancla(e, c, x0, z0);
            }
    }

    private static BlockState b(Block x) {
        return x.defaultBlockState();
    }

    /** Un numero que solo depende del sitio (para variar los bloques sin estado). */
    static int h(int x, int y, int z) {
        int k = x * 73428767 ^ y * 912931 ^ z * 19349663;
        k ^= k >>> 13;
        k *= 0x5bd1e995;
        k ^= k >>> 15;
        return k & 0x7fffffff;
    }

    /** (Los bloques del mod; en las vistas sin juego, unos de Minecraft que se le parecen.) */
    private static BlockState hierroNegro() {
        return com.darkcraft.gen.TorreCupula.hierroNegro();
    }

    private static BlockState gema() {
        return com.darkcraft.gen.TorreCupula.gema(true);
    }

    /** La roca de la isla (negra, con vetas de basalto y alguna brasa). */
    private static BlockState roca(int x, int y, int z, int fondo) {
        int k = h(x, y, z);
        if (y < fondo + 3 && k % 23 == 0) return b(Blocks.MAGMA_BLOCK);
        if (k % 211 == 0) return b(Blocks.CRYING_OBSIDIAN);
        int v = (y + (int) (6 * Terreno.R2.fbm(x, z, 17, 1))) / 5;
        if ((v & 3) == 1) return b(Blocks.BASALT).setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        if ((v & 7) == 6) return b(Blocks.DEEPSLATE);
        return k % 37 == 0 ? b(Blocks.GILDED_BLACKSTONE) : b(Blocks.BLACKSTONE);
    }

    private static BlockState ladrillo(int x, int y, int z) {
        int k = h(x, y, z) % 10;
        return k < 2 ? b(Blocks.CRACKED_DEEPSLATE_BRICKS) : k == 2 ? b(Blocks.DEEPSLATE_TILES) : b(Blocks.DEEPSLATE_BRICKS);
    }

    private static BlockState negro(int x, int y, int z) {
        return h(x, y, z) % 6 == 0 ? b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS) : b(Blocks.POLISHED_BLACKSTONE_BRICKS);
    }

    /** El angulo (0..1 de vuelta) de (x, z) visto desde (cx, cz). */
    private static double vuelta(double x, double z, double cx, double cz) {
        double a = Math.atan2(z - cz, x - cx) / (2 * Math.PI);
        return a < 0 ? a + 1 : a;
    }

    /** Si toca almena en ese angulo (n almenas por vuelta, la mitad huecos). */
    private static boolean almena(double vuelta, int n) {
        return ((int) Math.floor(vuelta * n * 2) & 1) == 0;
    }

    /** Una columna de la isla y lo de encima. */
    private static void columna(MorvainGenerador.Escritor e, int x, int z) {
        double px = x + 0.5, pz = z + 0.5;
        double rho = Math.hypot(px, pz);
        double borde = RADIO + 9 * Terreno.R.fbm(px * 1.7, pz * 1.7, 70, 2);
        if (rho >= borde) return;
        double k = rho / borde;
        // la roca de debajo: un cono colgante con picos
        double picos = Math.max(0, Terreno.R.crestas(px + 5000, pz, 19, 2) - 0.72) / 0.28;
        int fondo = (int) Math.round(TOP - 6 - 140 * Math.pow(1 - k, 0.75) - 22 * (1 - k) * (0.5 + 0.5 * Terreno.R2.fbm(px, pz, 23, 2))
                - picos * 40 * Math.sqrt(1 - k));
        int top = k > 0.96 ? TOP - 1 : TOP;
        // la muralla (octogono)
        double d8 = -1e9, t8 = 0;
        for (int i = 0; i < 8; i++) {
            double f = Math.PI / 8 + i * Math.PI / 4, c = Math.cos(f), s = Math.sin(f);
            double d = px * c + pz * s;
            if (d > d8) {
                d8 = d;
                t8 = -px * s + pz * c;
            }
        }
        boolean dentro = d8 <= A - 3;
        for (int y = fondo; y <= top; y++) {
            BlockState st;
            if (y == top) {
                if (dentro) {
                    int q = h(x, 0, z) % 9;
                    st = q < 2 ? b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS) : q == 2 ? b(Blocks.CHISELED_POLISHED_BLACKSTONE)
                            : b(Blocks.POLISHED_BLACKSTONE_BRICKS);
                } else {
                    double n = Terreno.R2.fbm(px, pz, 9, 1);
                    st = n > 0.25 ? b(Blocks.SOUL_SOIL) : n > -0.2 ? b(Blocks.BLACKSTONE) : b(Blocks.GRAVEL);
                }
            } else st = roca(x, y, z, fondo);
            e.poner(x, y, z, st);
        }
        // la muralla
        boolean muro = d8 > A - 3 && d8 <= A;
        // las torres de la muralla (la mas cercana)
        double mejor = 1e9, vx = 0, vz = 0;
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4, cx = RV * Math.cos(a), cz = RV * Math.sin(a);
            double d = Math.hypot(px - cx, pz - cz);
            if (d < mejor) {
                mejor = d;
                vx = cx;
                vz = cz;
            }
        }
        if (muro && mejor > RT) muralla(e, x, z, d8, t8);
        if (mejor <= RT + 1.3) torre(e, x, z, mejor, vuelta(px, pz, vx, vz));
        // el homenaje, sus torreones y la aguja
        double ax = Math.abs(px), az = Math.abs(pz);
        double db = Math.min(H - Math.max(ax, az), (CHAFLAN - (ax + az)) / Math.sqrt(2));
        double dto = Math.hypot(ax - TT, az - TT);
        if (db >= -3.01) homenaje(e, x, z, px, pz, db);
        if (dto <= RTO + 1.2) torreon(e, x, z, dto, vuelta(ax, az, TT, TT));
        if (rho <= RAG + 1.3) aguja(e, x, z, rho, vuelta(px, pz, 0, 0));
    }

    private static void muralla(MorvainGenerador.Escritor e, int x, int z, double d8, double t8) {
        for (int y = TOP + 1; y <= TOP + MURO; y++)
            e.poner(x, y, z, y <= TOP + 3 ? negro(x, y, z) : y == TOP + MURO ? b(Blocks.DEEPSLATE_TILES) : ladrillo(x, y, z));
        // las almenas, por fuera
        if (d8 > A - 1 && ((int) Math.floor(t8 / 2) & 1) == 0) e.poner(x, TOP + MURO + 1, z, ladrillo(x, TOP + MURO + 1, z));
    }

    /** Una torre de la muralla: cuerpo hueco con suelos, troneras, matacanes con almenas y chapitel con su gema. */
    private static void torre(MorvainGenerador.Escritor e, int x, int z, double d, double v) {
        int cima = TOP + TORRE;
        if (d <= RT) {
            boolean pared = d > RT - 2;
            for (int y = TOP - 10; y <= cima; y++) {
                BlockState st;
                if (pared) {
                    int fila = y - TOP;
                    boolean tronera = Math.abs(((v * 4) % 1) - 0.5) > 0.42 && (fila >= 15 && fila <= 17 || fila >= 26 && fila <= 28 || fila >= 37 && fila <= 39);
                    st = tronera ? (fila >= 26 && fila <= 28 ? gema() : b(Blocks.RED_STAINED_GLASS)) : negro(x, y, z);
                } else if (y <= TOP || y == cima || y == TOP + 11 || y == TOP + 22 || y == TOP + 33) st = b(Blocks.DEEPSLATE_TILES);
                else st = b(Blocks.AIR);
                e.poner(x, y, z, st);
            }
        }
        // los matacanes y el parapeto
        if (d > RT - 0.6) {
            if (d > RT) for (int y = cima - 2; y <= cima; y++) e.poner(x, y, z, negro(x, y, z));
            e.poner(x, cima + 1, z, negro(x, cima + 1, z));
            if (almena(v, 12)) e.poner(x, cima + 2, z, negro(x, cima + 2, z));
        }
        // el chapitel
        chapitel(e, x, z, d, v, cima + 1, RT - 0.8, CHAPITEL);
    }

    /** Un chapitel conico de pizarra con cuatro nervios, un vastago de hierro negro y una gema encendida en la punta. */
    private static void chapitel(MorvainGenerador.Escritor e, int x, int z, double d, double v, int y0, double r0, int alto) {
        for (int y = y0; y <= y0 + alto; y++) {
            double f = (double) (y - y0) / alto;
            double r = r0 * Math.pow(1 - f, 1.1);
            if (d <= r && d > r - 1.7) {
                boolean nervio = Math.abs(((v * 4) % 1) - 0.5) > 0.42;
                e.poner(x, y, z, nervio ? b(Blocks.POLISHED_BLACKSTONE) : b(Blocks.DEEPSLATE_TILES));
            }
        }
        if (d < 0.75) {
            for (int y = y0 + alto - 1; y <= y0 + alto + 2; y++) e.poner(x, y, z, hierroNegro());
            e.poner(x, y0 + alto + 3, z, gema());
        }
    }

    /** La torre del homenaje: muros con contrafuertes y ventanas rojas, suelos por dentro y azotea almenada. */
    private static void homenaje(MorvainGenerador.Escritor e, int x, int z, double px, double pz, double db) {
        double ax = Math.abs(px), az = Math.abs(pz);
        // por donde va a lo largo del muro (para ventanas y contrafuertes): en las caras rectas, la otra coordenada
        boolean chaflan = (CHAFLAN - (ax + az)) / Math.sqrt(2) < H - Math.max(ax, az);
        double largo = chaflan ? (ax - az) / Math.sqrt(2) * 1.0 : (ax > az ? pz : px);
        int m = Math.floorMod((int) Math.floor(largo) + 4, 9);
        if (db < 0) {
            // los contrafuertes (solo en las caras rectas): escalonados hacia fuera
            if (chaflan || Math.floorMod((int) Math.floor(largo), 9) != 0) return;
            int alto = TOP + K - 14 - (int) Math.floor(-db) * 11;
            for (int y = TOP + 1; y <= alto; y++) e.poner(x, y, z, negro(x, y, z));
            e.poner(x, alto + 1, z, b(Blocks.DEEPSLATE_TILE_SLAB));
            return;
        }
        for (int y = TOP + 1; y <= TOP + K + 2; y++) {
            BlockState st = null;
            int fila = y - TOP;
            if (y > TOP + K) {
                // el parapeto y sus almenas
                if (db < 1.2 && (y == TOP + K + 1 || m < 2 || m > 6)) st = ladrillo(x, y, z);
            } else if (db < 3) {
                int f = (fila - 4) / 14, en = (fila - 4) % 14;
                boolean ventana = !chaflan && m < 2 && fila >= 4 && en < 8 && f <= 5;
                boolean ventanaChaflan = chaflan && m == 0 && fila >= 4 && en < 8 && f <= 5;
                if (ventana || ventanaChaflan) st = db < 1 ? ((f & 1) == 1 ? gema() : b(Blocks.RED_STAINED_GLASS)) : b(Blocks.AIR);
                else if (y == TOP + K) st = b(Blocks.DEEPSLATE_TILES);
                else st = !chaflan && m == 5 ? negro(x, y, z) : ladrillo(x, y, z);
            } else st = y == TOP + K || (fila - 1) % 14 == 0 && fila > 1 ? b(Blocks.DEEPSLATE_TILES) : b(Blocks.AIR);
            if (st != null) e.poner(x, y, z, st);
        }
    }

    /** Un torreon de una esquina del homenaje: sube por encima de la azotea, con su chapitel. */
    private static void torreon(MorvainGenerador.Escritor e, int x, int z, double d, double v) {
        int cima = TOP + TORREON;
        if (d <= RTO) {
            boolean pared = d > RTO - 1.6;
            for (int y = TOP + 1; y <= cima; y++) {
                int fila = y - TOP;
                if (pared) {
                    boolean tronera = fila > K + 4 && (fila - K - 4) % 10 < 3 && Math.abs(((v * 4) % 1) - 0.5) > 0.40;
                    e.poner(x, y, z, tronera ? gema() : negro(x, y, z));
                } else if (fila > K) e.poner(x, y, z, y == cima || (fila - K) % 10 == 0 ? b(Blocks.DEEPSLATE_TILES) : b(Blocks.AIR));
            }
        }
        if (d > RTO - 0.6) {
            e.poner(x, cima + 1, z, negro(x, cima + 1, z));
            if (almena(v, 8)) e.poner(x, cima + 2, z, negro(x, cima + 2, z));
        }
        chapitel(e, x, z, d, v, cima + 1, RTO - 0.6, CHAPITEL_T);
    }

    /** La aguja del centro, encima del homenaje: un cuerpo con ventanas rojas y la punta altisima con nervios. */
    private static void aguja(MorvainGenerador.Escritor e, int x, int z, double d, double v) {
        int y0 = TOP + K + 1, cima = y0 + CUERPO;
        if (d <= RAG) {
            boolean pared = d > RAG - 2;
            for (int y = y0; y <= cima; y++) {
                int fila = y - y0;
                if (pared) {
                    boolean ventana = fila >= 14 && fila <= 26 && Math.abs(((v * 8) % 1) - 0.5) > 0.4;
                    e.poner(x, y, z, ventana ? (fila >= 18 && fila <= 22 ? gema() : b(Blocks.RED_STAINED_GLASS)) : negro(x, y, z));
                } else e.poner(x, y, z, y == cima ? b(Blocks.DEEPSLATE_TILES) : b(Blocks.AIR));
            }
        }
        if (d > RAG - 0.6) {
            e.poner(x, cima + 1, z, negro(x, cima + 1, z));
            if (almena(v, 16)) e.poner(x, cima + 2, z, negro(x, cima + 2, z));
        }
        int p0 = cima + 2;
        for (int y = p0; y <= p0 + PUNTA; y++) {
            double f = (double) (y - p0) / PUNTA;
            double r = (RAG - 0.5) * Math.pow(1 - f, 1.25);
            if (d <= r && d > r - 1.8) {
                boolean nervio = Math.abs(((v * 8) % 1) - 0.5) > 0.42;
                e.poner(x, y, z, nervio ? b(Blocks.POLISHED_BLACKSTONE) : b(Blocks.DEEPSLATE_TILES));
            }
        }
        if (d < 0.75) {
            for (int y = p0 + PUNTA - 1; y <= p0 + PUNTA + 3; y++) e.poner(x, y, z, hierroNegro());
            e.poner(x, p0 + PUNTA + 4, z, gema());
        }
    }

    // ------------------------------------------------------------------ las cadenas y sus anclas

    private static void cadena(MorvainGenerador.Escritor e, Cadena c, int x0, int z0) {
        BlockState hierro = hierroNegro();
        for (int i = 0; i < c.eslabones; i++) {
            double[] ce = c.centro(i), m = c.caja(i);
            int bx0 = Math.max(x0, (int) Math.floor(ce[0] - m[0])), bx1 = Math.min(x0 + 15, (int) Math.ceil(ce[0] + m[0]));
            int bz0 = Math.max(z0, (int) Math.floor(ce[2] - m[2])), bz1 = Math.min(z0 + 15, (int) Math.ceil(ce[2] + m[2]));
            if (bx0 > bx1 || bz0 > bz1) continue;
            int by0 = (int) Math.floor(ce[1] - m[1]), by1 = (int) Math.ceil(ce[1] + m[1]);
            for (int x = bx0; x <= bx1; x++)
                for (int z = bz0; z <= bz1; z++)
                    for (int y = by0; y <= by1; y++)
                        if (c.hierro(i, ce, x + 0.5, y + 0.5, z + 0.5)) e.poner(x, y, z, hierro);
        }
    }

    /** El ancla: una piramide escalonada de piedra negra, con un aro de hierro negro, donde acaba la cadena. */
    private static void ancla(MorvainGenerador.Escritor e, Cadena c, int x0, int z0) {
        int cx = (int) Math.floor(c.bx), cz = (int) Math.floor(c.bz);
        if (cx + 11 < x0 || cx - 11 > x0 + 15 || cz + 11 < z0 || cz - 11 > z0 + 15) return;
        for (int x = Math.max(x0, cx - 11); x <= Math.min(x0 + 15, cx + 11); x++)
            for (int z = Math.max(z0, cz - 11); z <= Math.min(z0 + 15, cz + 11); z++) {
                int d = Math.max(Math.abs(x - cx), Math.abs(z - cz));
                for (int y = c.suelo - 8; y <= c.suelo + 8; y++) {
                    int lado = 10 - Math.max(0, y - c.suelo) * 3 / 4;
                    if (d > lado) continue;
                    BlockState st;
                    if (y == c.suelo + 1 && d == lado) st = hierroNegro();
                    else if (y == c.suelo + 8) st = b(Blocks.CHISELED_POLISHED_BLACKSTONE);
                    else st = negro(x, y, z);
                    e.poner(x, y, z, st);
                }
                // las cuatro puas de las esquinas
                if (Math.abs(x - cx) == 11 && Math.abs(z - cz) == 11)
                    for (int y = c.suelo - 2; y <= c.suelo + 7; y++) e.poner(x, y, z, hierroNegro());
            }
    }
}
