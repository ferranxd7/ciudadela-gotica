import com.darkcraft.morvain.Region;
import com.darkcraft.morvain.Terreno;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Vista del mapa de Morvain sin abrir el juego: java -cp clases Mapa salida.png [paso=16] [x0 z0 lado]
 * Colores por region con sombreado del relieve, agua (roja en la Tierra sin Fe y el crater), nieve en lo alto y curvas
 * de nivel cada 50 bloques. Escribe tambien la altura minima, maxima y lo que ocupa cada region.
 */
public class Mapa {
    public static void main(String[] a) throws Exception {
        String out = a[0];
        int paso = a.length > 1 ? Integer.parseInt(a[1]) : 16;
        int x0 = a.length > 2 ? Integer.parseInt(a[2]) : -Terreno.MITAD, z0 = a.length > 3 ? Integer.parseInt(a[3]) : -Terreno.MITAD;
        int lado = a.length > 4 ? Integer.parseInt(a[4]) : 2 * Terreno.MITAD;
        int n = lado / paso;
        double[][] h = new double[n + 1][n + 1];
        Terreno.Columna[][] c = new Terreno.Columna[n][n];
        long t = System.nanoTime();
        for (int j = 0; j <= n; j++)
            for (int i = 0; i <= n; i++) {
                Terreno.Columna col = Terreno.columna(x0 + i * paso, z0 + j * paso);
                h[j][i] = col.altura();
                if (i < n && j < n) c[j][i] = col;
            }
        double ms = (System.nanoTime() - t) / 1e6;
        BufferedImage img = new BufferedImage(n, n, BufferedImage.TYPE_INT_RGB);
        double min = 1e9, max = -1e9;
        int[] cuenta = new int[Region.values().length];
        for (int j = 0; j < n; j++)
            for (int i = 0; i < n; i++) {
                Terreno.Columna col = c[j][i];
                double y = col.altura();
                min = Math.min(min, y);
                max = Math.max(max, y);
                cuenta[col.region().ordinal()]++;
                // luz del noroeste
                double dx = (h[j][i + 1] - h[j][i]) / paso, dz = (h[j + 1][i] - h[j][i]) / paso;
                double luz = Math.max(0.25, Math.min(1.3, 0.85 - 1.6 * (dx + dz) * 0.5));
                int rgb = col.region().color;
                if (col.agua()) rgb = col.sangre() ? 0x6A0E10 : 0x2C4A5E;
                else if (y > 235 && col.region() == Region.MONTANAS) rgb = 0xF2F4F8;
                int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
                double k = col.agua() ? 1.0 - Math.min(0.5, (Terreno.AGUA - y) / 50.0) : luz;
                r = (int) Math.min(255, r * k);
                g = (int) Math.min(255, g * k);
                b = (int) Math.min(255, b * k);
                // curvas de nivel
                if (!col.agua() && (int) (y / 50) != (int) (h[j][i + 1] / 50)) {
                    r = r * 3 / 4;
                    g = g * 3 / 4;
                    b = b * 3 / 4;
                }
                img.setRGB(i, j, (r << 16) | (g << 8) | b);
            }
        ImageIO.write(img, "png", new File(out));
        StringBuilder s = new StringBuilder(String.format("%d x %d columnas en %.0f ms (%.1f us cada una); altura %.0f..%.0f;", n, n, ms,
                ms * 1000 / ((n + 1.0) * (n + 1.0)), min, max));
        for (Region r : Region.values()) s.append(' ').append(r.id).append('=').append(String.format("%.1f%%", 100.0 * cuenta[r.ordinal()] / (n * n)));
        System.out.println(s);
    }
}
