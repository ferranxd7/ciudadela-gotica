package com.darkcraft.arranque;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lo que Dark Craft deja puesto la PRIMERA vez que arranca en un cliente (parte 6): la lejania de Distant Horizons a
 * 256 chunks (sin su actualizador automatico: DH va dentro del jar) y los shaders de Oculus encendidos con
 * Complementary Unbound si el instalador lo ha bajado a {@code shaderpacks/}.
 *
 * <p>Se hace una sola vez por instalacion (lo apunta en {@code config/darkcraft-arranque.properties}): si luego el
 * jugador cambia la lejania o los shaders, se respeta. Lo llama {@link Arranque} antes de que arranque el juego (Oculus
 * lee su configuracion al crear las opciones, antes de construir los mods). Solo Java: aqui todavia no se puede tocar
 * ninguna clase de Minecraft.</p>
 */
public final class PorDefecto {
    private PorDefecto() {}

    /** La lejania de Distant Horizons por defecto (respuesta 82 del plan). */
    public static final int LEJANIA = 256;
    public static final String MARCA = "darkcraft-arranque.properties";
    static final String DH = "DistantHorizons.toml";
    static final String OCULUS = "oculus.properties";

    /** Lo minimo para DH: el resto lo rellena el al arrancar (y respeta estos dos valores). */
    static final String DH_MINIMO = String.join("\n",
            "# Dark Craft: la lejania de Distant Horizons empieza en " + LEJANIA + " chunks (se puede cambiar en Opciones > Lejania).",
            "[client]",
            "\t[client.advanced]",
            "\t\t[client.advanced.graphics]",
            "\t\t\t[client.advanced.graphics.quality]",
            "\t\t\t\tlodChunkRenderDistanceRadius = " + LEJANIA,
            "\t\t[client.advanced.autoUpdater]",
            "\t\t\tenableAutoUpdater = false",
            "");

    private static final Pattern RADIO = Pattern.compile("(?m)^(\\s*lodChunkRenderDistanceRadius\\s*=\\s*)(\\d+)");
    private static final Pattern ACTUALIZADOR = Pattern.compile("(?m)^(\\s*enableAutoUpdater\\s*=\\s*)true\\b");

    /** Aplica lo que falte; devuelve lo que ha hecho (para el registro). Nunca lanza. */
    public static List<String> aplicar(Path juego) {
        List<String> hecho = new ArrayList<>();
        try {
            Path config = juego.resolve("config");
            Files.createDirectories(config);
            Path fm = config.resolve(MARCA);
            Properties marca = new Properties();
            if (Files.exists(fm)) try (InputStream in = Files.newInputStream(fm)) {
                marca.load(in);
            }
            boolean cambia = false;
            if (!"1".equals(marca.getProperty("lejania"))) {
                String r = lejania(config);
                hecho.add(r);
                if (r.startsWith("lejania:")) {
                    marca.setProperty("lejania", "1");
                    cambia = true;
                }
            }
            if (!"1".equals(marca.getProperty("shaders"))) {
                String r = shaders(juego, config);
                hecho.add(r);
                if (r.startsWith("shaders:")) {
                    marca.setProperty("shaders", "1");
                    cambia = true;
                }
            }
            if (cambia) try (OutputStream out = Files.newOutputStream(fm)) {
                marca.store(out, "Dark Craft: lo que ya se ha puesto por defecto una vez (borra una linea para repetirlo)");
            }
        } catch (Throwable t) {
            hecho.add("fallo: " + t);
        }
        return hecho;
    }

    /** "lejania: ..." si queda puesta; otra cosa si no se ha podido (se reintenta en el siguiente arranque). */
    static String lejania(Path config) throws IOException {
        Path f = config.resolve(DH);
        if (!Files.exists(f)) {
            Files.writeString(f, DH_MINIMO, StandardCharsets.UTF_8);
            return "lejania: " + DH + " creado con " + LEJANIA + " chunks y sin actualizador";
        }
        String s = Files.readString(f, StandardCharsets.UTF_8);
        Matcher m = RADIO.matcher(s);
        if (!m.find()) return "lejania pendiente: " + DH + " no tiene lodChunkRenderDistanceRadius";
        String antes = m.group(2);
        String t = RADIO.matcher(s).replaceFirst("$1" + LEJANIA);
        t = ACTUALIZADOR.matcher(t).replaceAll("$1false");
        if (!t.equals(s)) Files.writeString(f, t, StandardCharsets.UTF_8);
        return "lejania: " + antes + " -> " + LEJANIA + " chunks en el " + DH + " que habia";
    }

    /** "shaders: ..." si quedan puestos; si no hay Complementary en shaderpacks/ se reintenta en el siguiente arranque. */
    static String shaders(Path juego, Path config) throws IOException {
        String pack = buscarPack(juego.resolve("shaderpacks"));
        if (pack == null) return "shaders pendientes: no hay Complementary en shaderpacks/ (lo baja el instalador)";
        Path f = config.resolve(OCULUS);
        Properties p = new Properties();
        if (Files.exists(f)) try (InputStream in = Files.newInputStream(f)) {
            p.load(in);
        }
        String antes = p.getProperty("shaderPack", "") + (("false".equals(p.getProperty("enableShaders"))) ? " (apagados)" : "");
        p.setProperty("shaderPack", pack);
        p.setProperty("enableShaders", "true");
        p.setProperty("disableUpdateMessage", "true");
        try (OutputStream out = Files.newOutputStream(f)) {
            p.store(out, "This file stores configuration options for Iris, such as the currently active shaderpack");
        }
        return "shaders: " + pack + " encendido (antes: " + (antes.isEmpty() ? "ninguno" : antes) + ")";
    }

    /** El zip de Complementary Unbound (o, si no, cualquier Complementary) de la carpeta de shaders. */
    static String buscarPack(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) return null;
        String unbound = null, otro = null;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
            for (Path p : ds) {
                String n = p.getFileName().toString();
                String l = n.toLowerCase(Locale.ROOT);
                if (!l.startsWith("complementary") || !(l.endsWith(".zip") || Files.isDirectory(p))) continue;
                if (l.contains("unbound")) {
                    if (unbound == null || n.compareTo(unbound) > 0) unbound = n;
                } else if (otro == null || n.compareTo(otro) > 0) otro = n;
            }
        }
        return unbound != null ? unbound : otro;
    }
}
