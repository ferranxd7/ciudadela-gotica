import com.darkcraft.morvain.Region;
import com.darkcraft.morvain.Terreno;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * El mapa de Morvain en pergamino (parte 7c): la textura {@code textures/gui/mapa_morvain.png} que dibujan el viaje entre
 * santuarios y (despues) el mapa de la M. 1024 x 1024 para los 16.000 x 16.000 bloques (15,6 bloques por pixel): cada
 * region con su tinta sobre el papel, relieve con luz del noroeste, curvas de nivel cada 50, rios y lagos, la costa
 * entintada, el mar oscuro con lineas de oleaje y los bordes quemados. Sale del mismo {@link Terreno} que el juego: si
 * cambia el terreno, hay que volver a sacarlo.
 *
 * <pre>javac -d /tmp/mv src/main/java/com/darkcraft/morvain/{Ruido,Region,Terreno}.java tools/morvain/Pergamino.java
 * java -cp /tmp/mv Pergamino src/main/resources/assets/darkcraft/textures/gui/mapa_morvain.png</pre>
 */
public class Pergamino {
    static final int N = 1024;
    static final double PASO = 2.0 * Terreno.MITAD / N;

    static int rgb(int r, int g, int b) {
        return (Math.max(0, Math.min(255, r)) << 16) | (Math.max(0, Math.min(255, g)) << 8) | Math.max(0, Math.min(255, b));
    }

    static int mezcla(int a, int b, double t) {
        t = Math.max(0, Math.min(1, t));
        int r = (int) Math.round(((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) Math.round(((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bb = (int) Math.round((a & 255) * (1 - t) + (b & 255) * t);
        return rgb(r, g, bb);
    }

    static int luz(int c, double k) {
        return rgb((int) (((c >> 16) & 255) * k), (int) (((c >> 8) & 255) * k), (int) ((c & 255) * k));
    }

    /** La tinta de cada region sobre el papel. */
    static int tinta(Region r) {
        return switch (r) {
            case LLANURAS -> 0xD6C79A;
            case BOSQUE -> 0xA0946A;
            case PANTANO -> 0x989A70;
            case MONTANAS -> 0xE6E2D6;
            case ACADEMIA -> 0xB8AEC4;
            case CAPITAL -> 0xC4AE8E;
            case COSTA -> 0xE8DEC4;
            case SIN_FE -> 0xB4805E;
            case CENTRO -> 0x8E4436;
            default -> 0x3A4A52;
        };
    }

    static double ruido(int x, int y, int s) {
        long h = x * 73856093L ^ y * 19349663L ^ s * 83492791L;
        h ^= h >>> 13;
        h *= 0x5bd1e995L;
        h ^= h >>> 15;
        return ((h & 0xFFFF) / 65535.0);
    }

    /** Ruido suave del papel (valor de 0 a 1). */
    static double papel(int x, int y) {
        double v = 0, a = 0.5;
        for (int o = 0; o < 4; o++) {
            int c = 64 >> o;
            int x0 = Math.floorDiv(x, c), y0 = Math.floorDiv(y, c);
            double fx = (x - x0 * c) / (double) c, fy = (y - y0 * c) / (double) c;
            double sx = fx * fx * (3 - 2 * fx), sy = fy * fy * (3 - 2 * fy);
            double n00 = ruido(x0, y0, o), n10 = ruido(x0 + 1, y0, o), n01 = ruido(x0, y0 + 1, o), n11 = ruido(x0 + 1, y0 + 1, o);
            v += a * ((n00 * (1 - sx) + n10 * sx) * (1 - sy) + (n01 * (1 - sx) + n11 * sx) * sy);
            a *= 0.5;
        }
        return v / 0.9375;
    }

    public static void main(String[] args) throws Exception {
        long t0 = System.currentTimeMillis();
        double[][] h = new double[N + 2][N + 2];
        Region[][] reg = new Region[N + 2][N + 2];
        double[][] rio = new double[N + 2][N + 2];
        for (int j = 0; j < N + 2; j++)
            for (int i = 0; i < N + 2; i++) {
                double wx = -Terreno.MITAD + (i - 0.5) * PASO, wz = -Terreno.MITAD + (j - 0.5) * PASO;
                Terreno.Columna c = Terreno.columna(wx, wz);
                h[j][i] = c.altura();
                reg[j][i] = c.region();
                rio[j][i] = c.rio();
            }
        BufferedImage img = new BufferedImage(N, N, BufferedImage.TYPE_INT_ARGB);
        int tintaCosta = 0x3A2A1C, mar = 0x34434A, marClaro = 0x4C5E64, agua = 0x5A6E74, sangre = 0x7A1E1E;
        for (int y = 0; y < N; y++)
            for (int x = 0; x < N; x++) {
                int i = x + 1, j = y + 1;
                double a = h[j][i];
                Region r = reg[j][i];
                boolean esMar = r == Region.MAR;
                boolean mojado = a < Terreno.AGUA;
                int c;
                if (esMar) {
                    // el mar: mas claro junto a la costa, con lineas de oleaje
                    double hondo = Math.max(0, Math.min(1, (Terreno.AGUA - a) / 40.0));
                    c = mezcla(marClaro, mar, hondo);
                    if (hondo < 0.6 && ((int) Math.floor(a / 4.0)) % 3 == 0 && Math.floorMod(x + y * 3, 7) < 4) c = luz(c, 1.12);
                } else if (mojado) {
                    // rios, lagos y charcas (el del crater, de sangre)
                    c = r == Region.CENTRO || r == Region.SIN_FE ? sangre : agua;
                } else {
                    c = tinta(r);
                    // relieve: luz del noroeste
                    double dx = h[j][i + 1] - h[j][i - 1], dz = h[j + 1][i] - h[j - 1][i];
                    double sombra = 1.0 + (-dx - dz) * 0.018;
                    c = luz(c, Math.max(0.55, Math.min(1.18, sombra)));
                    // las montanas, con rayado en las laderas
                    if (r == Region.MONTANAS && Math.abs(dx) + Math.abs(dz) > 10 && Math.floorMod(x - y, 4) == 0) c = luz(c, 0.82);
                    // el bosque, con motas de arboles
                    if (r == Region.BOSQUE && ruido(x, y, 9) < 0.18) c = luz(c, 0.78);
                    // curvas de nivel cada 50
                    int k = (int) Math.floor(a / 50.0);
                    if (k != (int) Math.floor(h[j][i + 1] / 50.0) || k != (int) Math.floor(h[j + 1][i] / 50.0)) c = luz(c, 0.86);
                }
                // la costa entintada (tierra que toca el mar)
                if (!esMar) {
                    boolean costa = reg[j][i + 1] == Region.MAR || reg[j][i - 1] == Region.MAR || reg[j + 1][i] == Region.MAR || reg[j - 1][i] == Region.MAR;
                    if (costa) c = tintaCosta;
                }
                // el papel
                double p = papel(x, y);
                c = luz(c, 0.9 + 0.16 * p);
                // bordes quemados
                double bx = Math.min(x, N - 1 - x), by = Math.min(y, N - 1 - y), borde = Math.min(bx, by);
                double quemado = borde + 10 * (papel(x * 3, y * 3) - 0.5);
                int alfa = 255;
                if (quemado < 14) c = mezcla(c, 0x2A1A10, (14 - quemado) / 14.0 * 0.85);
                if (quemado < 2) alfa = 0;
                img.setRGB(x, y, (alfa << 24) | c);
            }
        File f = new File(args.length > 0 ? args[0] : "mapa_morvain.png");
        f.getAbsoluteFile().getParentFile().mkdirs();
        ImageIO.write(img, "png", f);
        System.out.println("mapa de Morvain: " + N + " x " + N + " en " + (System.currentTimeMillis() - t0) + " ms -> " + f);
    }
}
