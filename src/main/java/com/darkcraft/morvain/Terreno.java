package com.darkcraft.morvain;

/**
 * El mapa de Morvain, las Tierras Marchitas (respuestas 85-91 del plan): 16.000 x 16.000 bloques ({@link #MITAD} a cada
 * lado del centro), el mismo para todos y disenado a mano: un continente rodeado de un mar muerto, con sus regiones
 * repartidas alrededor del centro (donde espera la Sombra, bajo el castillo encadenado en el cielo):
 * <ul>
 * <li>al sur, las Llanuras Palidas (se llega por el portal) y al suroeste el Bosque Muerto;</li>
 * <li>al oeste el Pantano Podrido; al norte, de lado a lado, las Montanas Nevadas, colosales, que caen al mar;</li>
 * <li>al noreste la Academia Espectral en lo alto de una meseta de acantilados; al este la Capital en ruinas;</li>
 * <li>al sureste la Costa de los Huesos; alrededor del centro la Tierra sin Fe (grietas y rios de sangre) y en el centro
 * el crater con el lago de sangre.</li>
 * </ul>
 * Cada region pone su forma (llanos, colinas, crestas, meseta...) y se funden entre si en las fronteras (pesos suaves,
 * mas cortantes en la meseta: acantilados). Los rios (de nieve al pantano, el del este por la Capital al mar, el palido
 * por las llanuras al mar) y los de sangre (de la Tierra sin Fe al crater) cavan su cauce hasta el nivel del agua.
 * Solo Java y sin estado: lo usan el generador, Distant Horizons (desde sus hilos) y la vista del mapa sin juego.
 */
public final class Terreno {
    private Terreno() {}

    /** El mapa va de -MITAD a MITAD en x y en z. */
    public static final int MITAD = 8000;
    /** Nivel del agua (mar, pantano, rios; en la Tierra sin Fe y el crater, de sangre). */
    public static final int AGUA = 63;
    /** Radio del anillo de la Tierra sin Fe y del crater del centro. */
    public static final double ANILLO = 2100, CRATER = 1050;

    static final Ruido R = new Ruido(0x4D6F7276L), R2 = new Ruido(0x5368616C);

    static final double[][] LLANURAS = {{0, 5600}, {-1600, 6900}, {1900, 6600}, {700, 4300}, {-900, 4600}};
    static final double[][] BOSQUE = {{-4700, 4300}, {-5900, 6200}, {-3300, 5900}, {-4300, 2600}};
    static final double[][] PANTANO = {{-5900, 300}, {-6600, -1900}, {-4400, 1500}, {-4700, -1300}};
    static final double[][] MONTANAS = {{-6000, -6300}, {-3200, -6700}, {0, -6600}, {3100, -6800}, {6200, -6200},
            {-7000, -4300}, {7000, -4700}, {-1600, -5300}, {1600, -5300}};
    static final double[][] CAPITAL = {{4900, 600}, {3700, 1600}, {5700, -500}};
    static final double[][] ACADEMIA = {{4700, -4100}, {5600, -3300}};
    static final double[][] COSTA = {{6900, 4300}, {5000, 6700}, {7000, 2100}, {3400, 7300}};

    /** Las cumbres colosales: {x, z, radio, altura}. */
    static final double[][] COLOSOS = {{-2200, -6600, 1500, 720}, {2700, -6400, 1300, 690}, {-5600, -5700, 1100, 640},
            {5900, -5900, 1000, 610}};

    /** Los rios: puntos del cauce {x, z} y su ancho (en bloques). */
    static final double[][][] RIOS = {
            // de las nieves al pantano (nace al pie de la cordillera)
            {{-2900, -3300}, {-3300, -2600}, {-3800, -1900}, {-4400, -1200}, {-5100, -400}, {-5900, 300}, {-6900, 500}},
            // el del este: de la academia por la capital al mar
            {{3700, -2700}, {3900, -1900}, {4300, -800}, {4800, 500}, {5600, 1200}, {6500, 1700}, {7600, 2300}},
            // el palido: de la tierra sin fe por las llanuras al mar del sur
            {{700, 3500}, {700, 4500}, {1100, 5600}, {800, 6600}, {1300, 7500}, {1500, 8300}}};
    static final double[] ANCHO_RIOS = {22, 30, 18};

    /** Lo que hay en una columna. */
    public record Columna(double altura, Region region, boolean sangre, double rio) {
        /** Si el agua le cubre (mar, pantano, rio): hasta {@link #AGUA}. */
        public boolean agua() {
            return altura < AGUA;
        }
    }

    // ------------------------------------------------------------------ las regiones

    private static double dist(double[][] semillas, double x, double z) {
        double m = Double.MAX_VALUE;
        for (double[] s : semillas) {
            double dx = x - s[0], dz = z - s[1];
            m = Math.min(m, dx * dx + dz * dz);
        }
        return Math.sqrt(m);
    }

    /** Lo que se tuercen las fronteras (para que no salgan rectas). */
    private static double[] torcer(double x, double z) {
        return new double[]{x + 520 * R.fbm(x, z, 2200, 3), z + 520 * R.fbm(x + 913, z - 377, 2200, 3)};
    }

    /** Las bahias del contorno: {x, z, radio}. */
    static final double[][] BAHIAS = {{-8600, 600, 1500}, {8400, 3600, 1700}, {6200, 8800, 1600}, {-3000, 8900, 1300},
            {2600, -8700, 1100}, {-8300, -5600, 1200}};

    /** Cuanto hay de tierra (1) o de mar (0): un continente de costa irregular, con bahias y la del este mas adentro. */
    public static double tierra(double x, double z) {
        double wx = x + 900 * R.fbm(x, z, 2600, 3) + 260 * R2.fbm(x, z, 700, 2);
        double wz = z + 900 * R.fbm(x + 517, z - 311, 2600, 3) + 260 * R2.fbm(x + 99, z - 41, 700, 2);
        double rx = wx > 0 ? (z > -3200 ? 6950 : 7350) : 7400, rz = wz > 0 ? 7350 : 7450;
        double s = Math.pow(Math.abs(wx) / rx, 3.4) + Math.pow(Math.abs(wz) / rz, 3.4);
        double t = Ruido.suave(1.12, 0.9, s);
        for (double[] b : BAHIAS) t = Math.min(t, Ruido.suave(b[2] * 0.82, b[2] * 1.08, Math.hypot(wx - b[0], wz - b[1])));
        return t;
    }

    /** La distancia (con las fronteras torcidas) a cada region; el orden es el de {@link Region}. */
    static double[] distancias(double x, double z) {
        double[] w = torcer(x, z);
        double px = w[0], pz = w[1], rho = Math.hypot(px, pz);
        double[] d = new double[Region.values().length];
        d[Region.MAR.ordinal()] = Double.MAX_VALUE;
        d[Region.LLANURAS.ordinal()] = dist(LLANURAS, px, pz);
        d[Region.BOSQUE.ordinal()] = dist(BOSQUE, px, pz);
        d[Region.PANTANO.ordinal()] = dist(PANTANO, px, pz);
        // las montanas: cuanto mas al norte, mas cerca (una cordillera de lado a lado)
        d[Region.MONTANAS.ordinal()] = Math.min(dist(MONTANAS, px, pz), Math.max(0, pz + 5300) * 0.9 + 400);
        d[Region.CAPITAL.ordinal()] = dist(CAPITAL, px, pz);
        d[Region.ACADEMIA.ordinal()] = dist(ACADEMIA, px, pz) * 1.15;
        d[Region.COSTA.ordinal()] = dist(COSTA, px, pz);
        d[Region.SIN_FE.ordinal()] = Math.abs(rho - ANILLO) * 1.7 + 250;
        d[Region.CENTRO.ordinal()] = Math.max(0, rho - 200) * 1.3;
        return d;
    }

    /** La region de un sitio (la mas cercana; el mar donde no hay tierra). */
    public static Region region(double x, double z) {
        if (tierra(x, z) < 0.5) return Region.MAR;
        double[] d = distancias(x, z);
        int mejor = 1;
        for (int i = 2; i < d.length; i++) if (d[i] < d[mejor]) mejor = i;
        return Region.values()[mejor];
    }

    // ------------------------------------------------------------------ la altura

    /** La forma de cada region (sin mezclar). */
    static double forma(Region r, double x, double z) {
        switch (r) {
            case LLANURAS:
                return 74 + 9 * R.fbm(x, z, 900, 3) + 5 * R.fbm(x, z, 260, 2) + 2 * R2.fbm(x, z, 60, 2);
            case BOSQUE:
                return 88 + 20 * R.fbm(x, z, 700, 3) + 9 * R.fbm(x, z, 190, 2) + 3 * R2.fbm(x, z, 45, 2);
            case PANTANO:
                return 63.6 + 3.4 * R.fbm(x, z, 420, 3) + 2.4 * R2.fbm(x, z, 70, 2) + 1.2 * R2.fbm(x, z, 18, 1);
            case MONTANAS: {
                // la cordillera: crece hacia el norte, crestas afiladas (picos de 550-650)
                double m = Ruido.suave(-2600, -6100, z) * 0.75 + 0.25 * Ruido.suave(-1800, -4200, z);
                double c = R.crestas(x + 4000, z, 1500, 5);
                double cumbres = 0.3 + 0.7 * Math.pow(c, 1.35);
                double h = 112 + 470 * m * cumbres + 30 * R2.fbm(x, z, 260, 3) * (0.4 + m);
                // los colosos: cumbres que pasan de 700 (se ven desde media Morvain)
                for (double[] p : COLOSOS) {
                    double d = Math.hypot(x - p[0], z - p[1]);
                    if (d < p[2]) h = Math.max(h, Ruido.lerp(Math.pow(Ruido.suave(p[2], 0, d), 1.3), h, p[3] * (0.86 + 0.14 * c)));
                }
                return h;
            }
            case CAPITAL:
                return 95 + 7 * R.fbm(x, z, 800, 3) + 3 * R2.fbm(x, z, 150, 2);
            case ACADEMIA:
                // la meseta de acantilados
                return 242 + 16 * R.fbm(x, z, 520, 3) + 5 * R2.fbm(x, z, 110, 2);
            case COSTA:
                return 66 + 5 * R.fbm(x, z, 520, 3) + 2 * R2.fbm(x, z, 90, 2);
            case SIN_FE: {
                // tierra agrietada: grietas en red que bajan hasta la sangre
                double h = 120 + 24 * R.fbm(x, z, 640, 3) + 9 * R2.fbm(x, z, 150, 2);
                double c = R2.crestas(x, z, 950, 3);
                return h - (h - (AGUA - 4)) * Ruido.suave(0.87, 0.965, c);
            }
            case CENTRO: {
                double rho = Math.hypot(x, z);
                // el crater: borde a 175, fondo bajo la sangre; en medio, el peñon de la Sombra
                double h = 40 + 138 * Ruido.suave(430, CRATER, rho) + 7 * R.fbm(x, z, 200, 2);
                if (rho < 120) h = Math.max(h, 40 + 300 * Ruido.suave(120, 18, rho) + 6 * R2.fbm(x, z, 30, 2));
                return h;
            }
            default:
                return 38 + 5 * R.fbm(x, z, 600, 2);
        }
    }

    /** Lo suaves que son las fronteras de cada region (la meseta, casi de golpe: acantilados). */
    static double temple(Region r) {
        return switch (r) {
            case ACADEMIA -> 70;
            case MONTANAS -> 380;
            case CENTRO -> 160;
            default -> 240;
        };
    }

    /** La columna entera: altura (antes del suelo), region y rio. */
    public static Columna columna(double x, double z) {
        double t = tierra(x, z);
        double[] d = distancias(x, z);
        Region[] rs = Region.values();
        int mejor = 1;
        for (int i = 2; i < d.length; i++) if (d[i] < d[mejor]) mejor = i;
        double suma = 0, h = 0;
        for (int i = 1; i < d.length; i++) {
            double e = d[i] - d[mejor];
            double tt = Math.min(temple(rs[i]), temple(rs[mejor]));
            if (e > tt * 7) continue;
            double w = Math.exp(-e / tt);
            suma += w;
            h += w * forma(rs[i], x, z);
        }
        h /= suma;
        Region reg = rs[mejor];
        boolean sangre = reg == Region.SIN_FE || reg == Region.CENTRO;
        // los rios
        double rio = Double.MAX_VALUE;
        for (int k = 0; k < RIOS.length; k++) {
            double dr = distanciaPolilinea(RIOS[k], x + 60 * R2.fbm(x, z, 400, 2), z + 60 * R2.fbm(x - 50, z + 70, 400, 2));
            double ancho = ANCHO_RIOS[k];
            if (dr < ancho + 220) {
                double cauce = AGUA - 3 - 4 * Ruido.suave(ancho, 0, dr);
                double orilla = dr < ancho ? cauce : AGUA + 0.6 + (dr - ancho) * 0.45;
                h = Math.min(h, orilla);
                rio = Math.min(rio, dr - ancho);
            }
        }
        // el mar: lo que no es tierra baja al fondo
        if (t < 1) h = Ruido.lerp(t, forma(Region.MAR, x, z), h);
        if (t < 0.5) reg = Region.MAR;
        return new Columna(h, reg, sangre, rio);
    }

    /** La distancia de un punto a una linea quebrada. */
    static double distanciaPolilinea(double[][] p, double x, double z) {
        double m = Double.MAX_VALUE;
        for (int i = 0; i + 1 < p.length; i++) {
            double ax = p[i][0], az = p[i][1], bx = p[i + 1][0], bz = p[i + 1][1];
            double vx = bx - ax, vz = bz - az, wx = x - ax, wz = z - az;
            double l = vx * vx + vz * vz, u = l == 0 ? 0 : Math.max(0, Math.min(1, (wx * vx + wz * vz) / l));
            double dx = wx - u * vx, dz = wz - u * vz;
            m = Math.min(m, dx * dx + dz * dz);
        }
        return Math.sqrt(m);
    }
}
