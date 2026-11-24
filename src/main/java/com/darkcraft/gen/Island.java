package com.darkcraft.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;

import java.util.ArrayList;
import java.util.List;

/** Isla flotante: meseta superior, parte inferior conica con rocas colgantes, estalactitas y cascadas. */
public class Island extends Element {
    public interface Surface {
        /** Bloque de superficie o null para el estilo salvaje por defecto. */
        BlockState at(int x, int z, int topY);
    }

    public interface Plants {
        boolean allowed(int x, int z);
    }

    /** Los bloques de debajo de la superficie (de 1 a 3 bajo ella), o null para la tierra de siempre. */
    public interface Subsurface {
        BlockState at(int x, int y, int z, int fromTop);
    }

    public final int cx, cz, y0;
    private final double baseR, amp;
    private final int rim, drop, maxDepth;
    private final Noise noise;
    private final double off;
    private Surface surface = (x, z, t) -> null;
    private Plants plants = (x, z) -> true;
    private Subsurface subsurface = null;
    private final List<double[]> waterfalls = new ArrayList<>(); // {angulo, ancho}

    public Island(long seed, int cx, int cz, int y0, double baseR, double amp, int rim, int drop, int maxDepth) {
        super(seed);
        this.layer = 0;
        this.cx = cx;
        this.cz = cz;
        this.y0 = y0;
        this.baseR = baseR;
        this.amp = amp;
        this.rim = rim;
        this.drop = drop;
        this.maxDepth = maxDepth;
        this.noise = new Noise(seed);
        this.off = (seed & 1023) * 0.37;
        int R = (int) Math.ceil(baseR * (1 + amp)) + 2;
        bounds(cx - R, y0 - maxDepth - 60, cz - R, cx + R, y0 + 2, cz + R);
    }

    public Island surface(Surface s) {
        this.surface = s;
        return this;
    }

    public Island plants(Plants p) {
        this.plants = p;
        return this;
    }

    /** Otros bloques bajo la superficie (y sin musgo debajo de la isla). */
    public Island subsurface(Subsurface s) {
        this.subsurface = s;
        return this;
    }

    public Island waterfall(double angle, double width) {
        waterfalls.add(new double[]{angle, width});
        return this;
    }

    /** Ruido suave (-1..1) en manchas de unos 20 bloques, para variar el suelo. */
    public double mancha(int x, int z) {
        return noise.fbm(x * 0.045 + 17.3, z * 0.045 - 9.1, 2);
    }

    public double radiusAt(double ang) {
        double n = noise.fbm(Math.cos(ang) * 2.2 + off, Math.sin(ang) * 2.2 - off, 3);
        return baseR * (1 + amp * n);
    }

    /** Altura del bloque superior en (x,z), o Integer.MIN_VALUE si no hay isla. */
    public int topAt(int x, int z) {
        double dx = x - cx, dz = z - cz;
        double r = Math.sqrt(dx * dx + dz * dz);
        double R = radiusAt(Math.atan2(dz, dx));
        if (r > R) return Integer.MIN_VALUE;
        double plateau = R - rim;
        if (r <= plateau) return y0;
        double k = (r - plateau) / Math.max(1, R - plateau);
        return y0 - (int) Math.round(Math.pow(k, 1.4) * drop + noise.value(x * 0.15, z * 0.15) * 0.8);
    }

    private static double angDiff(double a, double b) {
        double d = Math.abs(a - b) % (Math.PI * 2);
        return d > Math.PI ? Math.PI * 2 - d : d;
    }

    @Override
    public void build(Canvas c) {
        for (int x = Math.max(x0, c.minX); x <= Math.min(x1, c.maxX); x++)
            for (int z = Math.max(z0, c.minZ); z <= Math.min(z1, c.maxZ); z++) column(c, x, z);
    }

    private void column(Canvas c, int x, int z) {
        double dx = x - cx, dz = z - cz;
        double r = Math.sqrt(dx * dx + dz * dz);
        double ang = Math.atan2(dz, dx);
        double R = radiusAt(ang);
        if (r > R) return;
        int top = topAt(x, z);
        double e = 1 - r / R;
        double nb = noise.fbm(x * 0.02 + 50, z * 0.02 - 50, 3);
        int depth = 3 + (int) Math.round(maxDepth * Math.pow(e, 0.65) * (0.78 + 0.35 * nb));
        // estalactitas gigantes en celdas de 9x9
        int extra = 0;
        int gx = Math.floorDiv(x, 9), gz = Math.floorDiv(z, 9);
        for (int ox = -1; ox <= 1; ox++)
            for (int oz = -1; oz <= 1; oz++) {
                int hx = gx + ox, hz = gz + oz;
                int hh = Hash.h(hx, 0, hz, seed ^ 0x51A1);
                if (hh % 100 > 55) continue;
                double scx = hx * 9 + 4.5 + (hh % 5 - 2), scz = hz * 9 + 4.5 + ((hh >> 4) % 5 - 2);
                double rad = 2.0 + (hh >> 8) % 3;
                double dd = Math.sqrt((x - scx) * (x - scx) + (z - scz) * (z - scz));
                if (dd >= rad) continue;
                double len = (6 + (hh >> 12) % 34) * (0.3 + 0.7 * e);
                int ex = (int) Math.round(len * Math.pow(1 - dd / rad, 1.3));
                extra = Math.max(extra, ex);
            }
        int bottom = top - depth - extra;

        // cascadas: canal y estanque
        boolean channel = false, edgeWater = false;
        for (double[] wf : waterfalls) {
            double a = wf[0];
            double Ra = radiusAt(a);
            double along = dx * Math.cos(a) + dz * Math.sin(a);
            double perp = Math.abs(-dx * Math.sin(a) + dz * Math.cos(a));
            double start = Ra - rim - 22;
            double px = Math.cos(a) * start, pz = Math.sin(a) * start;
            double pond = Math.sqrt((dx - px) * (dx - px) + (dz - pz) * (dz - pz));
            if ((perp <= wf[1] && along >= start && along <= Ra + 1) || pond <= wf[1] + 3) {
                channel = true;
                if (r > R - rim - 2) edgeWater = true;
            }
        }

        for (int y = bottom; y <= top; y++) {
            int fromTop = top - y;
            BlockState s;
            if (fromTop == 0) {
                BlockState sf = surface.at(x, z, top);
                s = sf != null ? sf : wildSurface(x, top, z, r, R);
            } else if (fromTop <= 3) {
                BlockState sb = subsurface != null ? subsurface.at(x, y, z, fromTop) : null;
                if (sb != null) s = sb;
                else {
                    int p = Hash.pct(x, y, z, seed + 3);
                    s = p < 60 ? Pal.DIRT : p < 80 ? Pal.COARSE_DIRT : Pal.ROOTED_DIRT;
                }
            } else {
                s = Pal.islandStone(x, y, z, fromTop);
                if (fromTop > 8) {
                    int p = Hash.h(x, y, z, seed + 99) % 1000;
                    if (p < 6) s = Pal.b(Blocks.DEEPSLATE_IRON_ORE);
                    else if (p < 9) s = Pal.b(Blocks.DEEPSLATE_COAL_ORE);
                    else if (p < 10) s = Pal.b(Blocks.DEEPSLATE_GOLD_ORE);
                    else if (p == 10 && fromTop > 30) s = Pal.b(Blocks.DEEPSLATE_DIAMOND_ORE);
                    else if (p < 14) s = Pal.b(Blocks.AMETHYST_BLOCK);
                }
            }
            if (y == bottom && subsurface == null && Hash.pct(x, y, z, 5) < 30) s = Pal.MOSS;
            c.set(x, y, z, s);
        }
        // cosas colgando debajo
        int hb = Hash.pct(x, bottom, z, seed + 7);
        if (hb < 4) {
            c.set(x, bottom - 1, z, Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN)
                    .setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.FRUSTUM));
            c.set(x, bottom - 2, z, Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN)
                    .setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.TIP));
        } else if (hb < 14) {
            c.set(x, bottom - 1, z, Pal.b(Blocks.HANGING_ROOTS));
        } else if (hb < 16) {
            int len = 2 + Hash.pct(x, 0, z, 9) % 6;
            for (int k = 1; k <= len; k++) c.set(x, bottom - k, z, k == len ? Pal.b(Blocks.WEEPING_VINES) : Pal.b(Blocks.WEEPING_VINES_PLANT));
        }

        if (channel) {
            c.set(x, top - 1, z, Pal.DS_COBBLED);
            if (edgeWater) c.flowingWater(x, top, z);
            else c.set(x, top, z, Pal.WATER);
            return;
        }
        // plantas
        if (!plants.allowed(x, z)) return;
        BlockState sf = c.get(x, top, z);
        if (!(sf.is(Blocks.GRASS_BLOCK) || sf.is(Blocks.PODZOL) || sf.is(Blocks.MOSS_BLOCK) || sf.is(Blocks.COARSE_DIRT))) return;
        int p = Hash.h(x, top, z, seed + 21) % 1000;
        BlockState plant = null;
        if (p < 150) plant = Pal.b(Blocks.GRASS);
        else if (p < 190) plant = Pal.b(Blocks.FERN);
        else if (p < 200) plant = Pal.b(Blocks.DEAD_BUSH);
        else if (p < 203) plant = Pal.b(Blocks.WITHER_ROSE);
        else if (p < 215 && sf.is(Blocks.MOSS_BLOCK)) plant = Pal.b(Blocks.MOSS_CARPET);
        else if (p < 218) plant = Pal.b(Blocks.BROWN_MUSHROOM);
        else if (p < 221) plant = Pal.b(Blocks.RED_MUSHROOM);
        if (plant != null) c.set(x, top + 1, z, plant);
    }

    private BlockState wildSurface(int x, int y, int z, double r, double R) {
        int p = Hash.pct(x, y, z, seed + 1);
        double edge = r / R;
        if (edge > 0.97 && p < 50) return Pal.islandStone(x, y, z, 0);
        if (p < 52) return Pal.GRASS_BLOCK;
        if (p < 70) return Pal.PODZOL;
        if (p < 82) return Pal.COARSE_DIRT;
        if (p < 94) return Pal.MOSS;
        if (p < 97) return Pal.ROOTED_DIRT;
        return Pal.GRAVEL;
    }

    public double rimAngleRadius(double ang) {
        return radiusAt(ang);
    }

    public int rimWidth() {
        return rim;
    }

    public static double distance(int ax, int az, int bx, int bz) {
        return Math.sqrt((double) (ax - bx) * (ax - bx) + (double) (az - bz) * (az - bz));
    }

    public static boolean angleNear(double a, double b, double tol) {
        return angDiff(a, b) < tol;
    }
}
