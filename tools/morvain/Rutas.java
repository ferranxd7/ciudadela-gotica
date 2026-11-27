import com.darkcraft.morvain.Region;
import com.darkcraft.morvain.RutasGigantes;
import com.darkcraft.morvain.Terreno;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Los caminos de los gigantes errantes sin abrir el juego (parte 7w): java -cp clases Rutas carpeta [ruta]
 * Por cada vuelta de {@link RutasGigantes}: lo que mide, lo alto y lo bajo, la mayor cuesta (en 6 bloques, por el medio y
 * a 3 bloques a cada lado: por donde pisan sus pies), el agua que pisa (cuanta y lo honda), las regiones y lo cerca que
 * pasa de cada sitio y santuario; y una vista (rutaN.png) con el relieve, la vuelta en rojo y los sitios en amarillo.
 */
public class Rutas {
    /** Los sitios de Morvain: {x, z, radio}, con su nombre. */
    static final Object[][] SITIOS = {
            {"arco del umbral", 0, 3400, 40}, {"velmor", 795, 4042, 260}, {"molino", 1088, 4073, 120}, {"arboleda", -4888, 3192, 90},
            {"pilotes", -5650, -770, 100}, {"laguna negra", -3955, -1880, 120}, {"monasterio", 3312, -5452, 100}, {"ermita", 3326, -5612, 60},
            {"capital", 3270, 345, 180}, {"academia", 4110, -3250, 110}, {"observatorio", 5577, -3600, 80}, {"puerto varado", 5680, 4740, 150},
            {"costillar", 5306, 3000, 110}, {"grieta", 135, 2095, 150}, {"altar", -1864, -1652, 90}, {"cerco", 0, 860, 70},
            {"san encrucijada", -1132, 5099, 30}, {"san vado", 868, 4181, 30}, {"san molino", 1086, 4048, 30}, {"san ahorcados", -4832, 3200, 30},
            {"san raiz_hueca", -5674, 5712, 30}, {"san juncos", -5600, -720, 30}, {"san embarcadero", -4023, -1897, 30},
            {"san colmillo", -3132, -3588, 30}, {"san coloso", 886, -4922, 30}, {"san helada", 3352, -5400, 30},
            {"san puerta_academia", 4110, -3176, 30}, {"san observatorio", 5577, -3543, 30}, {"san puerta_capital", 3177, 343, 30},
            {"san juramento", 5088, -824, 30}, {"san faro_huesos", 5612, 4779, 30}, {"san costillar", 5244, 3000, 30},
            {"san grieta", 24, 2181, 30}, {"san altar", -1864, -1600, 30}, {"san mirador", 2120, -480, 30}, {"san ancla_sur", 50, 1080, 30},
    };

    public static void main(String[] a) throws Exception {
        File dir = new File(a[0]);
        dir.mkdirs();
        int solo = a.length > 1 ? Integer.parseInt(a[1]) : -1;
        for (int i = 0; i < RutasGigantes.RUTAS.length; i++) {
            if (solo >= 0 && i != solo) continue;
            evaluar(i, dir);
        }
    }

    static double alto(double x, double z) {
        return Terreno.columna(x, z).altura();
    }

    static void evaluar(int i, File dir) throws Exception {
        double L = RutasGigantes.largo(i);
        int n = (int) Math.ceil(L);
        double[] h = new double[n], hl = new double[n], hr = new double[n];
        double hmin = 1e9, hmax = -1e9, agua = 0, honda = 0, cuesta = 0, sCuesta = 0;
        int[] regiones = new int[Region.values().length];
        for (int k = 0; k < n; k++) {
            double[] p = RutasGigantes.punto(i, k);
            double nx = -p[3], nz = p[2];
            Terreno.Columna c = Terreno.columna(p[0], p[1]);
            h[k] = c.altura();
            hl[k] = alto(p[0] + nx * 3, p[1] + nz * 3);
            hr[k] = alto(p[0] - nx * 3, p[1] - nz * 3);
            hmin = Math.min(hmin, h[k]);
            hmax = Math.max(hmax, h[k]);
            regiones[c.region().ordinal()]++;
            if (c.agua()) {
                agua++;
                honda = Math.max(honda, Terreno.AGUA - h[k]);
            }
        }
        for (int k = 0; k < n; k++) {
            int j = (k + 6) % n;
            double m = Math.max(Math.abs(h[j] - h[k]), Math.max(Math.abs(hl[j] - hl[k]), Math.abs(hr[j] - hr[k]))) / 6.0;
            m = Math.max(m, Math.abs(hl[k] - hr[k]) / 6.0);
            if (m > cuesta) {
                cuesta = m;
                sCuesta = k;
            }
        }
        StringBuilder sb = new StringBuilder(String.format(Locale.ROOT, "ruta %d: %.0f bloques (%.1f min a 2 bloques/s); altura %.0f..%.0f; mayor cuesta %.2f en s=%.0f %s;"
                        + " agua %.0f bloques (honda hasta %.1f); regiones", i, L, L / 2 / 60, hmin, hmax, cuesta, sCuesta,
                java.util.Arrays.toString(RutasGigantes.punto(i, sCuesta)), agua, honda));
        for (Region r : Region.values()) if (regiones[r.ordinal()] > 0) sb.append(' ').append(r.id).append('=').append(regiones[r.ordinal()]);
        // lo cerca que pasa de los sitios
        List<String> cerca = new ArrayList<>();
        for (Object[] s : SITIOS) {
            double sx = ((Number) s[1]).doubleValue(), sz = ((Number) s[2]).doubleValue(), r = ((Number) s[3]).doubleValue();
            double d = RutasGigantes.distancia(i, sx, sz) - r;
            if (d < 400) cerca.add(String.format(Locale.ROOT, "%s a %.0f", s[0], d));
        }
        sb.append("; sitios a menos de 400 (del borde): ").append(cerca.isEmpty() ? "ninguno" : String.join(", ", cerca));
        // las cuestas de 0,5 o mas (donde estan)
        int malas = 0;
        StringBuilder donde = new StringBuilder();
        for (int k = 0; k < n; k += 4) {
            int j = (k + 6) % n;
            double m = Math.max(Math.abs(h[j] - h[k]), Math.max(Math.abs(hl[j] - hl[k]), Math.abs(hr[j] - hr[k]))) / 6.0;
            if (m >= 0.5) {
                malas++;
                if (donde.length() < 300) {
                    double[] p = RutasGigantes.punto(i, k);
                    donde.append(String.format(Locale.ROOT, " (%.0f,%.0f)", p[0], p[1]));
                }
            }
        }
        sb.append("; tramos de cuesta >= 0,5: ").append(malas).append(donde);
        System.out.println(sb);
        // la vista
        double x0 = 1e9, z0 = 1e9, x1 = -1e9, z1 = -1e9;
        for (double[] p : RutasGigantes.RUTAS[i]) {
            x0 = Math.min(x0, p[0]);
            z0 = Math.min(z0, p[1]);
            x1 = Math.max(x1, p[0]);
            z1 = Math.max(z1, p[1]);
        }
        x0 -= 500;
        z0 -= 500;
        x1 += 500;
        z1 += 500;
        int paso = 4;
        int w = (int) ((x1 - x0) / paso), hh = (int) ((z1 - z0) / paso);
        BufferedImage img = new BufferedImage(w, hh, BufferedImage.TYPE_INT_RGB);
        double[][] alt = new double[hh + 1][w + 1];
        for (int j = 0; j <= hh; j++)
            for (int k = 0; k <= w; k++) alt[j][k] = alto(x0 + k * paso, z0 + j * paso);
        for (int j = 0; j < hh; j++)
            for (int k = 0; k < w; k++) {
                Terreno.Columna c = Terreno.columna(x0 + k * paso, z0 + j * paso);
                double dx = (alt[j][k + 1] - alt[j][k]) / paso, dz = (alt[j + 1][k] - alt[j][k]) / paso;
                double luz = Math.max(0.25, Math.min(1.3, 0.85 - 1.6 * (dx + dz) * 0.5));
                int rgb = c.region().color;
                if (c.agua()) rgb = c.sangre() ? 0x6A0E10 : 0x2C4A5E;
                int r = (int) Math.min(255, ((rgb >> 16) & 255) * (c.agua() ? 1 : luz));
                int g = (int) Math.min(255, ((rgb >> 8) & 255) * (c.agua() ? 1 : luz));
                int b = (int) Math.min(255, (rgb & 255) * (c.agua() ? 1 : luz));
                if (!c.agua() && (int) (alt[j][k] / 10) != (int) (alt[j][k + 1] / 10)) {
                    r = r * 4 / 5;
                    g = g * 4 / 5;
                    b = b * 4 / 5;
                }
                img.setRGB(k, j, (r << 16) | (g << 8) | b);
            }
        Graphics2D gr = img.createGraphics();
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setColor(new Color(220, 30, 30));
        gr.setStroke(new BasicStroke(3f));
        double[][] r = RutasGigantes.RUTAS[i];
        for (int k = 0; k < r.length; k++) {
            double[] p = r[k], q = r[(k + 1) % r.length];
            gr.drawLine((int) ((p[0] - x0) / paso), (int) ((p[1] - z0) / paso), (int) ((q[0] - x0) / paso), (int) ((q[1] - z0) / paso));
            gr.fillOval((int) ((p[0] - x0) / paso) - 5, (int) ((p[1] - z0) / paso) - 5, 10, 10);
        }
        gr.setColor(new Color(255, 220, 40));
        gr.setStroke(new BasicStroke(2f));
        for (Object[] s : SITIOS) {
            double sx = ((Number) s[1]).doubleValue(), sz = ((Number) s[2]).doubleValue(), rr = ((Number) s[3]).doubleValue();
            int cx = (int) ((sx - x0) / paso), cz = (int) ((sz - z0) / paso), rp = (int) Math.max(3, rr / paso);
            if (cx < -rp || cz < -rp || cx > w + rp || cz > hh + rp) continue;
            gr.drawOval(cx - rp, cz - rp, 2 * rp, 2 * rp);
            gr.drawString((String) s[0], cx + rp + 2, cz);
        }
        gr.dispose();
        ImageIO.write(img, "png", new File(dir, "ruta" + i + ".png"));
    }
}
