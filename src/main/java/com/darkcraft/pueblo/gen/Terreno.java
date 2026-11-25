package com.darkcraft.pueblo.gen;

import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Noise;
import com.darkcraft.gen.Pal;
import com.darkcraft.world.NubeBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;

/**
 * El terreno de la isla de Brumavela (parte 3b): una meseta ({@link #P}) que sale del mar de nubes, con la colina de la
 * capilla al noroeste, el penon del faro al noreste, la cala del muelle al sur (el muelle bajo, {@link #Q}, casi a ras de
 * las nubes) a la que baja la Cuesta por un tajo, y al oeste la bajada al Saliente. Por debajo, un cono de roca con
 * estalactitas que se hunde en las nubes. Todo sale de {@link #alto} (lo que se pisa en cada columna, o
 * {@link #NADA}): los edificios y los caminos lo consultan.
 */
public final class Terreno extends Element {
    /** La meseta y el muelle (el bloque de arriba: se pisa uno mas arriba). */
    public static final int P = 266, Q = 250;
    public static final int NADA = Integer.MIN_VALUE;
    /** Medio lado de lo que abarca la isla. */
    public static final int LADO = 152;

    // la colina de la capilla, el penon del faro, la cala del muelle, la Cuesta y el Saliente
    public static final int COLINA_X = -50, COLINA_Z = -46, COLINA_R = 27, COLINA_H = 6;
    public static final int PENON_X = 76, PENON_Z = -74, PENON_R = 22, PENON_H = 9;
    /** La rampa que sube al penon del faro: de x = RAMPA_X0 (a ras de la meseta) hacia el este, medio bloque por paso. */
    public static final int RAMPA_X0 = 46, RAMPA_X1 = RAMPA_X0 + 2 * PENON_H, RAMPA_Z = -72, RAMPA_ANCHO = 2;
    public static final int CALA_X = 0, CALA_Z = 110, CALA_R = 32, CALA_BORDE = 117;
    public static final int CUESTA_Z0 = 46, CUESTA_Z1 = 78, CUESTA_ANCHO = 3;
    public static final int BAJADA_X0 = -86, BAJADA_X1 = -114, BAJADA_Z = 6, BAJADA_ANCHO = 2;
    public static final int SALIENTE_X = -123, SALIENTE_Z = 6, SALIENTE_R = 9, SALIENTE_Y = 252;

    private final Noise ruido;
    private final int[] altos = new int[(2 * LADO + 1) * (2 * LADO + 1)];
    private final int[] fondos = new int[(2 * LADO + 1) * (2 * LADO + 1)];
    private final PuebloPlan plan;

    public Terreno(long seed, PuebloPlan plan) {
        super(seed);
        this.layer = 0;
        this.plan = plan;
        this.ruido = new Noise(seed);
        for (int x = -LADO; x <= LADO; x++)
            for (int z = -LADO; z <= LADO; z++) {
                int i = (x + LADO) * (2 * LADO + 1) + (z + LADO);
                altos[i] = calcular(x, z);
                fondos[i] = altos[i] == NADA ? NADA : fondo(x, z, altos[i]);
            }
        bounds(-LADO, P - 110, -LADO, LADO, P + PENON_H + 12, LADO);
    }

    /** Lo alto de la rampa del faro en esa x (en medios bloques exactos). */
    private static double rampa(int x) {
        return P + Math.max(0, Math.min(2 * PENON_H, x - RAMPA_X0)) * 0.5;
    }

    /** El radio de la isla hacia ese angulo (con el saliente del faro al noreste). */
    public double radio(double ang) {
        double n = ruido.fbm(Math.cos(ang) * 2.1 + 11.3, Math.sin(ang) * 2.1 - 4.7, 3);
        double r = 114 * (1 + 0.09 * n);
        double faro = Math.atan2(PENON_Z, PENON_X);
        double d = Math.abs(ang - faro);
        if (d > Math.PI) d = Math.PI * 2 - d;
        return r + 16 * Math.exp(-(d / 0.24) * (d / 0.24));
    }

    private static double suave(double t) {
        t = Math.max(0, Math.min(1, t));
        return t * t * (3 - 2 * t);
    }

    private int calcular(int x, int z) {
        double r = Math.sqrt((double) x * x + (double) z * z);
        double R = radio(Math.atan2(z, x));
        double dCala = Math.hypot(x - CALA_X, z - CALA_Z);
        boolean enCala = dCala < CALA_R + 5 && z <= CALA_BORDE;
        double dSal = Math.hypot(x - SALIENTE_X, z - SALIENTE_Z);
        boolean enSaliente = dSal <= SALIENTE_R + 0.3 * ruido.value(x * 0.4, z * 0.4);
        boolean enBajada = Math.abs(z - BAJADA_Z) <= BAJADA_ANCHO + 1 && x <= BAJADA_X0 && x >= SALIENTE_X;
        if (r > R && !enCala && !enSaliente && !enBajada) return NADA;
        double h = P;
        // el borde redondeado
        if (r <= R) {
            double k = (r - (R - 9)) / 9.0;
            if (k > 0) h -= Math.pow(k, 1.7) * 4.5 + ruido.value(x * 0.21, z * 0.21) * 0.8 * k;
        }
        // la colina de la capilla y el penon del faro
        // (las dos con lo alto llano: ahi van la capilla y el faro)
        double dc = Math.hypot(x - COLINA_X, z - COLINA_Z) / COLINA_R;
        if (dc < 1) h += COLINA_H * suave((1 - dc) / 0.48);
        double dp = Math.hypot(x - PENON_X, z - PENON_Z) / PENON_R;
        if (dp < 1) h += PENON_H * suave((1 - dp) / 0.4);
        // la rampa del faro: un terraplen por fuera del penon y un tajo por dentro
        if (x >= RAMPA_X0 - 1 && x <= RAMPA_X1 + 2 && r <= R) {
            int dz = Math.abs(z - RAMPA_Z);
            double c = rampa(x);
            if (dz <= RAMPA_ANCHO) h = c;
            else if (dz <= RAMPA_ANCHO + 3) h = Math.max(Math.min(h, c + (dz - RAMPA_ANCHO) * 2.5), c - (dz - RAMPA_ANCHO) * 2.0);
        }
        // la cala: el muelle llano y su acantilado
        if (dCala < CALA_R + 5 && z <= CALA_BORDE) {
            if (dCala <= CALA_R) h = Q;
            else h = Math.min(h, Q + (P - Q) * suave((dCala - CALA_R) / 5.0));
        }
        // la Cuesta: del pueblo al muelle, un tajo que baja medio bloque por paso
        if (Math.abs(x) <= CUESTA_ANCHO && z >= CUESTA_Z0 - 2 && z <= CUESTA_Z1 + 6) {
            double c = P - (z - CUESTA_Z0) * 0.5;
            h = Math.min(h, Math.max(Q, Math.min(P, c)));
        }
        // la bajada al Saliente y el propio Saliente
        if (enBajada) {
            double c = P - (BAJADA_X0 - x) * 0.5;
            if (Math.abs(z - BAJADA_Z) <= BAJADA_ANCHO) h = Math.min(h, Math.max(SALIENTE_Y, c));
            else if (r > R) return NADA;
        }
        if (enSaliente) h = r > R ? SALIENTE_Y : Math.min(h, SALIENTE_Y);
        return (int) Math.floor(h + 1e-6);
    }

    /** Lo de abajo de la isla en esa columna. */
    private int fondo(int x, int z, int alto) {
        double r = Math.sqrt((double) x * x + (double) z * z);
        double R = radio(Math.atan2(z, x));
        double e = Math.max(0, 1 - r / R);
        double nb = ruido.fbm(x * 0.02 + 50, z * 0.02 - 50, 3);
        int grueso = 4 + (int) Math.round(50 * Math.pow(e, 0.62) * (0.8 + 0.3 * nb));
        // estalactitas gigantes en celdas de 9 x 9
        int extra = 0;
        int gx = Math.floorDiv(x, 9), gz = Math.floorDiv(z, 9);
        for (int ox = -1; ox <= 1; ox++)
            for (int oz = -1; oz <= 1; oz++) {
                int hx = gx + ox, hz = gz + oz;
                int hh = Hash.h(hx, 0, hz, seed ^ 0x51A1);
                if (hh % 100 > 50) continue;
                double scx = hx * 9 + 4.5 + (hh % 5 - 2), scz = hz * 9 + 4.5 + ((hh >> 4) % 5 - 2);
                double rad = 2.0 + (hh >> 8) % 3;
                double dd = Math.sqrt((x - scx) * (x - scx) + (z - scz) * (z - scz));
                if (dd >= rad) continue;
                double len = (5 + (hh >> 12) % 26) * (0.3 + 0.7 * e);
                extra = Math.max(extra, (int) Math.round(len * Math.pow(1 - dd / rad, 1.3)));
            }
        int f = Math.min(P - grueso, alto - 4) - extra;
        // el Saliente y el frente del muelle (fuera del contorno): una repisa de roca
        if (r > R) f = alto - 4 - (int) Math.round(3 * Math.abs(ruido.value(x * 0.3, z * 0.3))) - extra / 2;
        return f;
    }

    /** Ruido suave (-1..1) en manchas de unos 15 bloques, para el suelo. */
    public double mancha(int x, int z) {
        return ruido.fbm(x * 0.06 + 31.3, z * 0.06 - 17.9, 2);
    }

    /** El bloque de arriba del terreno en (x, z) (se pisa en alto + 1), o {@link #NADA} si ahi no hay isla. */
    public int alto(int x, int z) {
        if (x < -LADO || x > LADO || z < -LADO || z > LADO) return NADA;
        return altos[(x + LADO) * (2 * LADO + 1) + (z + LADO)];
    }

    public boolean hay(int x, int z) {
        return alto(x, z) != NADA;
    }

    public int fondoDe(int x, int z) {
        if (x < -LADO || x > LADO || z < -LADO || z > LADO) return NADA;
        return fondos[(x + LADO) * (2 * LADO + 1) + (z + LADO)];
    }

    /** Si en la Cuesta o en la bajada toca media losa encima (los pasos de medio bloque). */
    public boolean mediaLosa(int x, int z) {
        if (Math.abs(x) <= CUESTA_ANCHO && z > CUESTA_Z0 && z < CUESTA_Z1 && alto(x, z) > Q) return ((z - CUESTA_Z0) & 1) == 1;
        if (enRampa(x, z)) return ((x - RAMPA_X0) & 1) == 1;
        if (Math.abs(z - BAJADA_Z) <= BAJADA_ANCHO && x < BAJADA_X0 && alto(x, z) > SALIENTE_Y && alto(x, z) < P - 1)
            return ((BAJADA_X0 - x) & 1) == 1;
        return false;
    }

    public boolean enCuesta(int x, int z) {
        return Math.abs(x) <= CUESTA_ANCHO && z >= CUESTA_Z0 - 1 && z <= CUESTA_Z1 + 2;
    }

    public boolean enRampa(int x, int z) {
        return Math.abs(z - RAMPA_Z) <= RAMPA_ANCHO && x > RAMPA_X0 && x < RAMPA_X1 && Math.hypot(x, z) <= radio(Math.atan2(z, x));
    }

    /**
     * Si en esa columna de una calle va media losa para subir sin saltar: cuando la calle sigue un bloque mas arriba en la
     * columna de al lado.
     */
    public boolean escalon(int x, int z) {
        if (plan.calle(x, z) > 0 || plan.enPlaza(x, z)) return false;
        int a = alto(x, z);
        if (a == NADA || enCuesta(x, z) || enBajada(x, z) || enRampa(x, z)) return false;
        for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}})
            if (alto(x + d[0], z + d[1]) == a + 1 && plan.calle(x + d[0], z + d[1]) <= 0.8) return true;
        return false;
    }

    public boolean enBajada(int x, int z) {
        return Math.abs(z - BAJADA_Z) <= BAJADA_ANCHO && x <= BAJADA_X0 + 1 && x >= SALIENTE_X + SALIENTE_R - 2;
    }

    public boolean enMuelle(int x, int z) {
        return alto(x, z) == Q && Math.hypot(x - CALA_X, z - CALA_Z) <= CALA_R + 1;
    }

    public boolean enSaliente(int x, int z) {
        return alto(x, z) == SALIENTE_Y && Math.hypot(x - SALIENTE_X, z - SALIENTE_Z) <= SALIENTE_R + 1;
    }

    @Override
    public void build(Canvas c) {
        for (int x = Math.max(x0, c.minX); x <= Math.min(x1, c.maxX); x++)
            for (int z = Math.max(z0, c.minZ); z <= Math.min(z1, c.maxZ); z++) columna(c, x, z);
    }

    private void columna(Canvas c, int x, int z) {
        int top = alto(x, z);
        if (top == NADA) {
            // junto a la isla: fuera las nubes que subirian por encima del borde
            return;
        }
        int bottom = fondoDe(x, z);
        boolean vecinoBajo = false;
        for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            int v = alto(x + d[0], z + d[1]);
            if (v == NADA || v < top - 2) vecinoBajo = true;
        }
        BlockState arriba = plan.superficie(x, z, top);
        boolean hierba = arriba.is(Blocks.GRASS_BLOCK) || arriba.is(Blocks.PODZOL) || arriba.is(Blocks.COARSE_DIRT) || arriba.is(Blocks.MOSS_BLOCK)
                || arriba.is(Blocks.ROOTED_DIRT) || arriba.is(Blocks.DIRT_PATH);
        for (int y = bottom; y <= top; y++) {
            int deArriba = top - y;
            BlockState s;
            if (deArriba == 0) s = arriba;
            else if (deArriba <= 3 && hierba && !vecinoBajo) {
                int p = Hash.pct(x, y, z, seed + 3);
                s = p < 60 ? Pal.DIRT : p < 82 ? Pal.COARSE_DIRT : Pal.ROOTED_DIRT;
            } else {
                s = Pal.islandStone(x, y, z, P - y);
                if (deArriba > 8) {
                    int p = Hash.h(x, y, z, seed + 99) % 1000;
                    if (p < 5) s = Pal.b(Blocks.DEEPSLATE_IRON_ORE);
                    else if (p < 8) s = Pal.b(Blocks.DEEPSLATE_COAL_ORE);
                    else if (p < 11) s = Pal.b(Blocks.AMETHYST_BLOCK);
                } else if (vecinoBajo && Hash.pct(x, y, z, seed + 5) < 16) s = Pal.b(Blocks.MOSSY_COBBLESTONE);
            }
            if (y == bottom && Hash.pct(x, y, z, 5) < 30) s = Pal.MOSS;
            c.set(x, y, z, s);
        }
        // los pasos de medio bloque de las cuestas
        if (mediaLosa(x, z)) c.set(x, top + 1, z, Pal.slab(Blocks.COBBLESTONE_SLAB, false));
        else if (escalon(x, z)) c.set(x, top + 1, z, Pal.slab(Hash.pct(x, top, z, 41) < 35 ? Blocks.MOSSY_COBBLESTONE_SLAB : Blocks.COBBLESTONE_SLAB, false));
        // lo que cuelga por debajo
        int hb = Hash.pct(x, bottom, z, seed + 7);
        if (hb < 4) {
            c.set(x, bottom - 1, z, Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN)
                    .setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.FRUSTUM));
            c.set(x, bottom - 2, z, Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN)
                    .setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.TIP));
        } else if (hb < 13) c.set(x, bottom - 1, z, Pal.b(Blocks.HANGING_ROOTS));
        // fuera las nubes que quedarian por encima del suelo
        for (int y = top + 1; y <= P + 4; y++) {
            BlockState s = c.get(x, y, z);
            if (s.getBlock() instanceof NubeBlock) c.set(x, y, z, Pal.AIR);
        }
        // hierbas
        if (!arriba.is(Blocks.GRASS_BLOCK) && !arriba.is(Blocks.PODZOL) && !arriba.is(Blocks.MOSS_BLOCK)) return;
        if (!plan.plantas(x, z) || mediaLosa(x, z) || escalon(x, z)) return;
        int p = Hash.h(x, top, z, seed + 21) % 1000;
        BlockState planta = null;
        if (p < 120) planta = Pal.b(Blocks.GRASS);
        else if (p < 150) planta = Pal.b(Blocks.FERN);
        else if (p < 172) planta = Pal.b(Blocks.DEAD_BUSH);
        else if (p < 176) planta = Pal.b(Blocks.WITHER_ROSE);
        else if (p < 180) planta = Pal.b(Blocks.BROWN_MUSHROOM);
        if (planta != null) c.set(x, top + 1, z, planta);
    }
}
