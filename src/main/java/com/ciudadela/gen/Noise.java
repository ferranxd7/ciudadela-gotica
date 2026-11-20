package com.ciudadela.gen;

/** Ruido de valor suave 2D/3D, determinista y sin estado mutable (seguro entre hilos). */
public final class Noise {
    private final long seed;

    public Noise(long seed) {
        this.seed = seed;
    }

    private double corner(int x, int y, int z) {
        return Hash.f(x, y, z, seed) * 2.0 - 1.0;
    }

    private static double fade(double t) {
        return t * t * (3 - 2 * t);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public double value(double x, double z) {
        int xi = (int) Math.floor(x), zi = (int) Math.floor(z);
        double fx = fade(x - xi), fz = fade(z - zi);
        double a = corner(xi, 0, zi), b = corner(xi + 1, 0, zi);
        double c = corner(xi, 0, zi + 1), d = corner(xi + 1, 0, zi + 1);
        return lerp(lerp(a, b, fx), lerp(c, d, fx), fz);
    }

    public double value(double x, double y, double z) {
        int xi = (int) Math.floor(x), yi = (int) Math.floor(y), zi = (int) Math.floor(z);
        double fx = fade(x - xi), fy = fade(y - yi), fz = fade(z - zi);
        double x00 = lerp(corner(xi, yi, zi), corner(xi + 1, yi, zi), fx);
        double x10 = lerp(corner(xi, yi + 1, zi), corner(xi + 1, yi + 1, zi), fx);
        double x01 = lerp(corner(xi, yi, zi + 1), corner(xi + 1, yi, zi + 1), fx);
        double x11 = lerp(corner(xi, yi + 1, zi + 1), corner(xi + 1, yi + 1, zi + 1), fx);
        return lerp(lerp(x00, x10, fy), lerp(x01, x11, fy), fz);
    }

    public double fbm(double x, double z, int octaves) {
        double sum = 0, amp = 1, norm = 0, f = 1;
        for (int i = 0; i < octaves; i++) {
            sum += value(x * f + i * 17.3, z * f - i * 9.1) * amp;
            norm += amp;
            amp *= 0.5;
            f *= 2.0;
        }
        return sum / norm;
    }
}
