package com.darkcraft.morvain;

/**
 * Ruido de gradiente 2D (Perlin mejorado) con la tabla fija: Morvain es el mismo mapa para todos (respuesta 87), asi que
 * nada depende de la semilla del mundo. Solo Java: lo usan el generador, DH (desde sus hilos) y la vista sin juego.
 */
public final class Ruido {
    private final int[] p = new int[512];

    public Ruido(long semilla) {
        int[] q = new int[256];
        for (int i = 0; i < 256; i++) q[i] = i;
        long s = semilla * 6364136223846793005L + 1442695040888963407L;
        for (int i = 255; i > 0; i--) {
            s = s * 6364136223846793005L + 1442695040888963407L;
            int j = (int) ((s >>> 33) % (i + 1));
            int t = q[i];
            q[i] = q[j];
            q[j] = t;
        }
        for (int i = 0; i < 512; i++) p[i] = q[i & 255];
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double grad(int h, double x, double y) {
        switch (h & 7) {
            case 0: return x + y;
            case 1: return -x + y;
            case 2: return x - y;
            case 3: return -x - y;
            case 4: return x;
            case 5: return -x;
            case 6: return y;
            default: return -y;
        }
    }

    /** Entre -1 y 1 (mas o menos), periodo 256. */
    public double ruido(double x, double y) {
        int xi = (int) Math.floor(x), yi = (int) Math.floor(y);
        double xf = x - xi, yf = y - yi;
        xi &= 255;
        yi &= 255;
        double u = fade(xf), v = fade(yf);
        int aa = p[p[xi] + yi], ab = p[p[xi] + yi + 1], ba = p[p[xi + 1] + yi], bb = p[p[xi + 1] + yi + 1];
        double x1 = lerp(u, grad(aa, xf, yf), grad(ba, xf - 1, yf));
        double x2 = lerp(u, grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1));
        return lerp(v, x1, x2) * 0.92;
    }

    /** Octavas sumadas (escala = tamano de la mayor, en bloques); entre -1 y 1. */
    public double fbm(double x, double z, double escala, int octavas) {
        double a = 1, f = 1 / escala, s = 0, n = 0;
        for (int i = 0; i < octavas; i++) {
            s += a * ruido(x * f + i * 31.7, z * f - i * 17.3);
            n += a;
            a *= 0.5;
            f *= 2.03;
        }
        return s / n;
    }

    /** Crestas (montanas afiladas): 0 en los valles, 1 en las crestas. */
    public double crestas(double x, double z, double escala, int octavas) {
        double a = 1, f = 1 / escala, s = 0, n = 0, peso = 1;
        for (int i = 0; i < octavas; i++) {
            double r = 1 - Math.abs(ruido(x * f + i * 13.1, z * f + i * 7.9));
            r *= r;
            r *= peso;
            peso = Math.min(1, r * 2);
            s += a * r;
            n += a;
            a *= 0.5;
            f *= 2.1;
        }
        return s / n;
    }

    public static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    public static double suave(double a, double b, double x) {
        double t = Math.max(0, Math.min(1, (x - a) / (b - a)));
        return t * t * (3 - 2 * t);
    }
}
