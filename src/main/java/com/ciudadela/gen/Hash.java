package com.ciudadela.gen;

/** Hash por posicion: da variedad de bloques sin depender del chunk que se este generando. */
public final class Hash {
    private Hash() {}

    public static long mix(long z) {
        z = (z ^ (z >>> 33)) * 0xFF51AFD7ED558CCDL;
        z = (z ^ (z >>> 33)) * 0xC4CEB9FE1A85EC53L;
        return z ^ (z >>> 33);
    }

    /** Entero no negativo de 31 bits. */
    public static int h(int x, int y, int z, long salt) {
        long v = x * 0x632BE59BD9B4E019L ^ y * 0x9E3779B97F4A7C15L ^ z * 0xC2B2AE3D27D4EB4FL ^ salt * 0x165667B19E3779F9L;
        return (int) (mix(v) >>> 33);
    }

    /** Valor en [0, 1). */
    public static double f(int x, int y, int z, long salt) {
        return h(x, y, z, salt) / 2147483648.0;
    }

    public static int pct(int x, int y, int z, long salt) {
        return h(x, y, z, salt) % 100;
    }
}
