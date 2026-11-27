package com.darkcraft.morvain;

/**
 * Los caminos de los gigantes errantes de Morvain (parte 7w): cada uno, una vuelta cerrada de puntos {x, z} por su region,
 * por tierra llana y seca, lejos de los sitios y de los santuarios (los eligio {@code tools/morvain/Rutas.java} con el
 * {@link Terreno}: lo que sube y baja, el agua y lo lejos que pasa de cada sitio). Solo Java y sin estado: lo usan el
 * servidor (por donde va cada uno aunque no le vea nadie) y la herramienta.
 */
public final class RutasGigantes {
    private RutasGigantes() {}

    /** Las vueltas: 0 las Llanuras Palidas, 1 la Costa de los Huesos, 2 la Capital en ruinas. */
    public static final double[][][] RUTAS = {
            // las Llanuras Palidas: al suroeste del rio palido, entre la encrucijada y el mar del sur
            {{-400, 5700}, {-1100, 5550}, {-1900, 5750}, {-2300, 6250}, {-1900, 6750}, {-1100, 6850}, {-400, 6600}, {-150, 6150}},
            // la Costa de los Huesos: entre las costillas, al suroeste del puerto varado
            {{3700, 4500}, {4700, 4700}, {5000, 5500}, {4500, 6200}, {3700, 6100}, {3400, 5300}},
            // la Capital en ruinas: al sureste de la ciudad, hacia la costa
            {{3900, 1000}, {4700, 900}, {5500, 1500}, {5200, 2100}, {4300, 2000}, {3700, 1600}},
    };

    private static final double[][] LARGOS = new double[RUTAS.length][];

    static {
        for (int i = 0; i < RUTAS.length; i++) {
            double[][] r = RUTAS[i];
            double[] l = new double[r.length + 1];
            for (int k = 0; k < r.length; k++) {
                double[] a = r[k], b = r[(k + 1) % r.length];
                l[k + 1] = l[k] + Math.hypot(b[0] - a[0], b[1] - a[1]);
            }
            LARGOS[i] = l;
        }
    }

    /** Lo que mide la vuelta entera. */
    public static double largo(int ruta) {
        double[] l = LARGOS[ruta];
        return l[l.length - 1];
    }

    /** El punto a {@code s} bloques del principio (da vueltas): {x, z, hacia x, hacia z}. */
    public static double[] punto(int ruta, double s) {
        double[][] r = RUTAS[ruta];
        double[] l = LARGOS[ruta];
        double total = l[l.length - 1];
        s = ((s % total) + total) % total;
        int k = 0;
        while (k < r.length - 1 && l[k + 1] < s) k++;
        double[] a = r[k], b = r[(k + 1) % r.length];
        double seg = l[k + 1] - l[k], t = seg < 1e-6 ? 0 : (s - l[k]) / seg;
        double dx = b[0] - a[0], dz = b[1] - a[1], n = Math.max(1e-6, Math.hypot(dx, dz));
        return new double[]{a[0] + dx * t, a[1] + dz * t, dx / n, dz / n};
    }

    /** Lo que se ha andado de la vuelta en el punto de ella mas cercano a (x, z). */
    public static double donde(int ruta, double x, double z) {
        double[][] r = RUTAS[ruta];
        double[] l = LARGOS[ruta];
        double mejor = Double.MAX_VALUE, s = 0;
        for (int k = 0; k < r.length; k++) {
            double[] a = r[k], b = r[(k + 1) % r.length];
            double dx = b[0] - a[0], dz = b[1] - a[1], ll = dx * dx + dz * dz;
            double t = ll < 1e-9 ? 0 : Math.max(0, Math.min(1, ((x - a[0]) * dx + (z - a[1]) * dz) / ll));
            double px = a[0] + dx * t, pz = a[1] + dz * t, d = (px - x) * (px - x) + (pz - z) * (pz - z);
            if (d < mejor) {
                mejor = d;
                s = l[k] + (l[k + 1] - l[k]) * t;
            }
        }
        return s;
    }

    /** Lo lejos de (x, z) a la vuelta. */
    public static double distancia(int ruta, double x, double z) {
        double[] p = punto(ruta, donde(ruta, x, z));
        return Math.hypot(p[0] - x, p[1] - z);
    }
}
