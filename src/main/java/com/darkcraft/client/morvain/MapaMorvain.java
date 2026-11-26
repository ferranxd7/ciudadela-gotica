package com.darkcraft.client.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.Region;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;

/**
 * El mapa de Morvain para el libro de la M (parte 7d): el pergamino de {@code textures/gui/mapa_morvain.png} (el mismo
 * del viaje entre santuarios) con solo las regiones cuyo mapa tiene el jugador ({@link ClientMapa#MAPAS}) y el Mar Muerto;
 * lo demas, transparente (debajo se ve lo que ha andado, entre la niebla). Las regiones salen de
 * {@code mapa_morvain_regiones.png} (un gris por region: su orden x 25; lo escribe tools/morvain/Pergamino.java). Los
 * bordes de lo que se ve se funden y se queman un poco. Se rehace al cambiar los mapas. Tambien da donde poner el nombre
 * de cada region (el punto mas metido en ella: en las que son un anillo, como la Tierra sin Fe, no vale el centro).
 */
public final class MapaMorvain {
    private MapaMorvain() {}

    /** Lado de las texturas y la mitad del mundo que cubren (bloques). */
    public static final int N = 1024;
    public static final double MITAD = 8000;

    private static final ResourceLocation PAPEL = new ResourceLocation(DarkCraftMod.MODID, "textures/gui/mapa_morvain.png"),
            REGIONES = new ResourceLocation(DarkCraftMod.MODID, "textures/gui/mapa_morvain_regiones.png");

    private static int[] papel;
    private static byte[] region;
    private static int[][] nombre;
    private static boolean fallo;
    private static DynamicTexture tex;
    private static ResourceLocation id;
    private static int hecha = Integer.MIN_VALUE;

    /** La textura con las regiones que tiene (o null si no se pudo cargar). */
    public static ResourceLocation textura() {
        if (!cargar()) return null;
        if (id != null && hecha == ClientMapa.version) return id;
        componer();
        hecha = ClientMapa.version;
        return id;
    }

    /** Donde poner el nombre de la region (bloques: x, z) o null. */
    public static int[] nombre(Region r) {
        if (!cargar()) return null;
        int[] p = nombre[r.ordinal()];
        return p == null ? null : new int[]{(int) Math.round(-MITAD + (p[0] + 0.5) * 2 * MITAD / N), (int) Math.round(-MITAD + (p[1] + 0.5) * 2 * MITAD / N)};
    }

    /** La region de un sitio del mundo segun el mapa (o null fuera de el). */
    public static Region regionEn(double x, double z) {
        if (!cargar()) return null;
        int px = (int) Math.floor((x + MITAD) * N / (2 * MITAD)), pz = (int) Math.floor((z + MITAD) * N / (2 * MITAD));
        if (px < 0 || pz < 0 || px >= N || pz >= N) return null;
        return Region.values()[region[pz * N + px]];
    }

    private static boolean cargar() {
        if (papel != null) return true;
        if (fallo) return false;
        Minecraft mc = Minecraft.getInstance();
        try (InputStream a = mc.getResourceManager().open(PAPEL); NativeImage im = NativeImage.read(NativeImage.Format.RGBA, a);
             InputStream b = mc.getResourceManager().open(REGIONES); NativeImage rg = NativeImage.read(NativeImage.Format.RGBA, b)) {
            if (im.getWidth() != N || im.getHeight() != N || rg.getWidth() != N || rg.getHeight() != N) throw new IllegalStateException("medidas");
            int[] p = new int[N * N];
            byte[] r = new byte[N * N];
            int nr = Region.values().length;
            for (int y = 0; y < N; y++)
                for (int x = 0; x < N; x++) {
                    p[y * N + x] = im.getPixelRGBA(x, y);
                    int v = rg.getPixelRGBA(x, y) & 0xFF;
                    r[y * N + x] = (byte) Math.max(0, Math.min(nr - 1, Math.round(v / 25f)));
                }
            papel = p;
            region = r;
            nombre = puntos(r);
            return true;
        } catch (Exception e) {
            fallo = true;
            DarkCraftMod.LOGGER.warn("[DarkCraft] el mapa de Morvain no se pudo cargar: {}", e.toString());
            return false;
        }
    }

    /**
     * El punto mas metido de cada region (el mas lejos de otra region): distancia por chaflan en dos pasadas sobre una
     * cuadricula de la mitad de resolucion.
     */
    private static int[][] puntos(byte[] r) {
        int M = N / 2, nr = Region.values().length;
        int[] d = new int[M * M];
        byte[] q = new byte[M * M];
        for (int y = 0; y < M; y++)
            for (int x = 0; x < M; x++) q[y * M + x] = r[(2 * y) * N + 2 * x];
        final int INF = 1 << 20;
        for (int y = 0; y < M; y++)
            for (int x = 0; x < M; x++) {
                int i = y * M + x;
                boolean borde = x == 0 || y == 0 || x == M - 1 || y == M - 1 || q[i - 1] != q[i] || q[i + 1] != q[i] || q[i - M] != q[i] || q[i + M] != q[i];
                d[i] = borde ? 0 : INF;
            }
        for (int y = 1; y < M; y++)
            for (int x = 1; x < M - 1; x++) {
                int i = y * M + x;
                d[i] = Math.min(d[i], Math.min(Math.min(d[i - 1] + 2, d[i - M] + 2), Math.min(d[i - M - 1] + 3, d[i - M + 1] + 3)));
            }
        for (int y = M - 2; y >= 0; y--)
            for (int x = M - 2; x >= 1; x--) {
                int i = y * M + x;
                d[i] = Math.min(d[i], Math.min(Math.min(d[i + 1] + 2, d[i + M] + 2), Math.min(d[i + M + 1] + 3, d[i + M - 1] + 3)));
            }
        int[][] mejor = new int[nr][];
        int[] dm = new int[nr];
        for (int i = 0; i < M * M; i++) {
            int k = q[i];
            if (d[i] > dm[k]) {
                dm[k] = d[i];
                mejor[k] = new int[]{(i % M) * 2, (i / M) * 2};
            }
        }
        return mejor;
    }

    private static void componer() {
        boolean[] ver = new boolean[Region.values().length];
        ver[Region.MAR.ordinal()] = true;
        for (Region r : Region.values()) if (ClientMapa.tiene(r.id)) ver[r.ordinal()] = true;
        float[] m = new float[N * N];
        for (int i = 0; i < N * N; i++) m[i] = ver[region[i]] ? 1f : 0f;
        m = borrar(borrar(m, 2), 2);
        if (tex == null) {
            tex = new DynamicTexture(N, N, false);
            tex.setFilter(true, false);
            id = Minecraft.getInstance().getTextureManager().register("darkcraft_mapa_morvain", tex);
        }
        NativeImage im = tex.getPixels();
        if (im == null) return;
        for (int y = 0; y < N; y++)
            for (int x = 0; x < N; x++) {
                int i = y * N + x;
                float a = m[i];
                int c = papel[i];
                if (a <= 0.02f) {
                    im.setPixelRGBA(x, y, 0);
                    continue;
                }
                // el borde de lo que se ve, quemado: mas oscuro y medio transparente
                float k = a >= 0.999f ? 1f : 0.45f + 0.55f * a;
                int al = Math.round(((c >>> 24) & 255) * Math.min(1f, a * 1.35f));
                int r = Math.round((c & 255) * k), g = Math.round(((c >> 8) & 255) * k), b = Math.round(((c >> 16) & 255) * k);
                im.setPixelRGBA(x, y, (al << 24) | (b << 16) | (g << 8) | r);
            }
        tex.upload();
    }

    /** Media de cada pixel con los de alrededor (caja de lado 2r+1, en dos pasadas). */
    private static float[] borrar(float[] m, int r) {
        float[] t = new float[N * N], o = new float[N * N];
        float k = 1f / (2 * r + 1);
        for (int y = 0; y < N; y++)
            for (int x = 0; x < N; x++) {
                float s = 0;
                for (int d = -r; d <= r; d++) s += m[y * N + Math.max(0, Math.min(N - 1, x + d))];
                t[y * N + x] = s * k;
            }
        for (int y = 0; y < N; y++)
            for (int x = 0; x < N; x++) {
                float s = 0;
                for (int d = -r; d <= r; d++) s += t[Math.max(0, Math.min(N - 1, y + d)) * N + x];
                o[y * N + x] = s * k;
            }
        return o;
    }

    /** Para la prueba. */
    public static String resumen() {
        StringBuilder b = new StringBuilder("mapa de Morvain: ");
        if (!cargar()) return b.append("NO SE CARGA").toString();
        b.append("cargado, ").append(ClientMapa.MAPAS.size()).append(" regiones a la vista; nombres en");
        for (Region r : Region.values()) {
            int[] p = nombre(r);
            if (p != null && r != Region.MAR) b.append(' ').append(r.id).append('(').append(p[0]).append(',').append(p[1]).append(')');
        }
        return b.toString();
    }
}
