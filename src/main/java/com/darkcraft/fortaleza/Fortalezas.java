package com.darkcraft.fortaleza;

import com.darkcraft.gen.FortalezaPlan;
import com.darkcraft.gen.Hash;
import com.darkcraft.world.CastleSites;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Donde esta la Fortaleza de Hueso de cada Reino de Valdrenn: a 2500 bloques del reino, en la direccion que la deja mas
 * lejos de los demas reinos (y de sus tormentas), flotando sobre las nubes. Lo mismo en el servidor y en la generacion.
 */
public final class Fortalezas {
    private Fortalezas() {}

    /** Distancia al reino, altura que se pisa en el patio y radio de la zona (lluvia, musica, caballeros). */
    public static final int DIST = 2500, Y = 320, ZONA = 130;

    private static final Map<String, int[]> SITIOS = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, int[]> e) {
            return size() > 256;
        }
    };
    private static final Map<String, FortalezaPlan> PLANES = new LinkedHashMap<>(8, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, FortalezaPlan> e) {
            return size() > 4;
        }
    };

    /** Centro {x, z} de la fortaleza del reino que esta en (sx, sz). */
    public static synchronized int[] de(long seed, int sx, int sz) {
        String k = seed + ":" + sx + ":" + sz;
        int[] r = SITIOS.get(k);
        if (r != null) return r;
        long h = Hash.mix(seed ^ (sx * 0x9E3779B97F4A7C15L) ^ (sz * 0xC2B2AE3D27D4EB4FL) ^ 0xB0E5F0L);
        double a0 = (h >>> 11) * 0x1.0p-53 * Math.PI * 2;
        int[] mejor = null;
        double md = -1;
        for (int k2 = 0; k2 < 16; k2++) {
            double a = a0 + k2 * Math.PI * 2 / 16;
            int fx = sx + (int) Math.round(Math.cos(a) * DIST), fz = sz + (int) Math.round(Math.sin(a) * DIST);
            // (el origen cuenta como un reino mas: ahi esta la isla de Brumavela)
            double d = Math.hypot(fx, fz);
            int rx0 = Math.floorDiv(fx, CastleSites.REGION), rz0 = Math.floorDiv(fz, CastleSites.REGION);
            for (int rx = rx0 - 2; rx <= rx0 + 2; rx++)
                for (int rz = rz0 - 2; rz <= rz0 + 2; rz++) {
                    int[] s = CastleSites.site(seed, rx, rz);
                    if (s != null) d = Math.min(d, Math.hypot(s[0] - fx, s[1] - fz));
                }
            if (d > md) {
                md = d;
                mejor = new int[]{fx, fz};
            }
            // la primera (en el orden de la semilla) que queda bien lejos de cualquier reino
            if (d >= 1500) break;
        }
        SITIOS.put(k, mejor);
        return mejor;
    }

    /** Una fortaleza: su centro y el reino al que pertenece. */
    public record Sitio(int x, int z, int reinoX, int reinoZ) {
        public String clave() {
            return x + "," + z;
        }
    }

    /** Las fortalezas a menos de radio bloques (en horizontal) de (x, z). */
    public static List<Sitio> cercanas(long seed, double x, double z, int radio) {
        List<Sitio> out = new ArrayList<>();
        int alcance = DIST + radio + CastleSites.REGION;
        int rx0 = Math.floorDiv((int) x - alcance, CastleSites.REGION), rx1 = Math.floorDiv((int) x + alcance, CastleSites.REGION);
        int rz0 = Math.floorDiv((int) z - alcance, CastleSites.REGION), rz1 = Math.floorDiv((int) z + alcance, CastleSites.REGION);
        for (int rx = rx0; rx <= rx1; rx++)
            for (int rz = rz0; rz <= rz1; rz++) {
                int[] s = CastleSites.site(seed, rx, rz);
                if (s == null) continue;
                if (Math.abs(Math.hypot(s[0] - x, s[1] - z) - DIST) > radio + 2) continue;
                int[] f = de(seed, s[0], s[1]);
                if (Math.hypot(f[0] - x, f[1] - z) <= radio) out.add(new Sitio(f[0], f[1], s[0], s[1]));
            }
        return out;
    }

    /** La fortaleza mas cercana a (x, z) (buscando hasta max bloques) o null. */
    public static Sitio cercana(long seed, double x, double z, int max) {
        Sitio mejor = null;
        double md = Double.MAX_VALUE;
        for (Sitio s : cercanas(seed, x, z, max)) {
            double d = Math.hypot(s.x() - x, s.z() - z);
            if (d < md) {
                md = d;
                mejor = s;
            }
        }
        return mejor;
    }

    /** La fortaleza del reino mas cercano a (x, z) (la que abre la llave de hueso de ese reino). */
    public static Sitio delReino(long seed, double x, double z) {
        int[] s = CastleSites.nearest(seed, (int) x, (int) z, 3);
        if (s == null) return null;
        int[] f = de(seed, s[0], s[1]);
        return new Sitio(f[0], f[1], s[0], s[1]);
    }

    public static synchronized FortalezaPlan plan(long seed, int fx, int fz) {
        String k = seed + ":" + fx + ":" + fz;
        FortalezaPlan p = PLANES.get(k);
        if (p == null) {
            p = new FortalezaPlan(seed ^ ((long) fx * 73L + fz * 31L) ^ 0xF0E7A1L, fx, fz, Y);
            PLANES.put(k, p);
        }
        return p;
    }

    public static FortalezaPlan plan(long seed, Sitio s) {
        return plan(seed, s.x(), s.z());
    }
}
