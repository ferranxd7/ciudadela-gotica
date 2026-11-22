package com.darkcraft.gen;

import java.util.List;

/** Generador aleatorio determinista (SplitMix64). Cada elemento crea el suyo a partir de su semilla. */
public final class Rng {
    private long s;

    public Rng(long seed) {
        this.s = seed ^ 0x9E3779B97F4A7C15L;
        next();
    }

    public long next() {
        long z = (s += 0x9E3779B97F4A7C15L);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    public int nextInt(int n) {
        if (n <= 1) return 0;
        return (int) Math.floorMod(next(), (long) n);
    }

    /** Entero en [a, b] (incluidos). */
    public int range(int a, int b) {
        if (b <= a) return a;
        return a + nextInt(b - a + 1);
    }

    public float nextFloat() {
        return (next() >>> 40) / (float) (1L << 24);
    }

    public double nextDouble() {
        return (next() >>> 11) * 0x1.0p-53;
    }

    public boolean chance(double p) {
        return nextDouble() < p;
    }

    public <T> T pick(T[] a) {
        return a[nextInt(a.length)];
    }

    public <T> T pick(List<T> a) {
        return a.get(nextInt(a.size()));
    }

    public long fork() {
        return next();
    }
}
