package com.ciudadela.world;

import com.ciudadela.gen.CastlePlan;
import com.ciudadela.gen.Hash;

/**
 * Donde aparecen las ciudadelas: el mundo se divide en regiones de 2400x2400 bloques y en cada una
 * hay un 55% de probabilidad de que haya una (nunca a menos de 1200 bloques del origen).
 */
public final class CastleSites {
    public static final int REGION = 2400;
    public static final int MARGIN = CastlePlan.FOOTPRINT + 60;
    public static final double CHANCE = 0.55;
    public static final int MIN_DIST = 1200;

    private CastleSites() {}

    /** Centro {x, z} de la ciudadela de la region (rx, rz), o null si no hay. */
    public static int[] site(long seed, int rx, int rz) {
        long h = Hash.mix(seed ^ (rx * 341873128712L) ^ (rz * 132897987541L) ^ 0x5EEDCA57L);
        double roll = (h >>> 11) * 0x1.0p-53;
        if (roll >= CHANCE) return null;
        long h2 = Hash.mix(h + 1), h3 = Hash.mix(h + 2);
        int span = REGION - 2 * MARGIN;
        int x = rx * REGION + MARGIN + (int) Math.floorMod(h2, (long) span);
        int z = rz * REGION + MARGIN + (int) Math.floorMod(h3, (long) span);
        if ((long) x * x + (long) z * z < (long) MIN_DIST * MIN_DIST) return null;
        return new int[]{x, z};
    }

    /** La ciudadela mas cercana a (x, z) buscando en un radio de regiones. */
    public static int[] nearest(long seed, int x, int z, int radiusRegions) {
        int rx0 = Math.floorDiv(x, REGION), rz0 = Math.floorDiv(z, REGION);
        int[] best = null;
        long bestD = Long.MAX_VALUE;
        for (int rx = rx0 - radiusRegions; rx <= rx0 + radiusRegions; rx++)
            for (int rz = rz0 - radiusRegions; rz <= rz0 + radiusRegions; rz++) {
                int[] s = site(seed, rx, rz);
                if (s == null) continue;
                long d = (long) (s[0] - x) * (s[0] - x) + (long) (s[1] - z) * (s[1] - z);
                if (d < bestD) {
                    bestD = d;
                    best = s;
                }
            }
        return best;
    }
}
