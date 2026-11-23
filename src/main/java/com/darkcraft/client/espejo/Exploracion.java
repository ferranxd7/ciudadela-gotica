package com.darkcraft.client.espejo;

import com.darkcraft.DarkCraftMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.LevelResource;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * El mapa explorado de una dimension, en el ordenador del jugador: regiones de 512x512 bloques con el color de lo
 * que se ve desde arriba (un pixel por bloque, ARGB; alfa 0 = sin explorar). Se rellena con los chunks que tiene
 * cargados el cliente y se guarda en {@code .minecraft/darkcraft/espejo/<mundo>/<dimension>/}.
 */
public final class Exploracion {
    public static final int R = 512;
    /** Explorado pero sin nada (el vacio del End). */
    public static final int VACIO = 0x01000000;

    public static final class Region {
        public final int rx, rz;
        public final int[] px = new int[R * R];
        /** Cambia cada vez que se pinta algo (para saber cuando subir la textura otra vez). */
        public int version;
        boolean sucia;

        Region(int rx, int rz) {
            this.rx = rx;
            this.rz = rz;
        }
    }

    private static final ExecutorService DISCO = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "darkcraft-espejo-disco");
        t.setDaemon(true);
        return t;
    });

    /** Lo que se esta escribiendo al disco (por si se vuelve a pedir antes de que acabe). */
    private static final java.util.concurrent.ConcurrentHashMap<Path, int[]> PEND = new java.util.concurrent.ConcurrentHashMap<>();

    public final String dim;
    private final Path carpeta;
    private final Map<Long, Region> regiones = new HashMap<>();
    private final Set<Long> noHay = new HashSet<>();
    private Region ultima;
    private long ultimaNo = Long.MIN_VALUE;

    Exploracion(String mundo, String dim) {
        this.dim = dim;
        this.carpeta = raiz().resolve(mundo).resolve(dim.replace(':', '_'));
    }

    Path carpeta() {
        return carpeta;
    }

    static Path raiz() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("darkcraft").resolve("espejo");
    }

    /** Carpeta del mundo actual: el nombre de la partida o la direccion del servidor. */
    static String mundoActual() {
        Minecraft mc = Minecraft.getInstance();
        String n = null;
        if (mc.getSingleplayerServer() != null) {
            Path p = mc.getSingleplayerServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
            n = "partida_" + (p.getFileName() == null ? "mundo" : p.getFileName().toString());
        } else {
            ServerData sd = mc.getCurrentServer();
            if (sd != null) n = "servidor_" + sd.ip;
        }
        if (n == null) n = "desconocido";
        return n.replaceAll("[^A-Za-z0-9._\\-]", "_");
    }

    /** Dimensiones con mapa guardado en este mundo (para el selector del libro). */
    static List<String> dimensionesGuardadas(String mundo) {
        List<String> out = new ArrayList<>();
        Path m = raiz().resolve(mundo);
        if (!Files.isDirectory(m)) return out;
        try (var s = Files.list(m)) {
            s.filter(Files::isDirectory).forEach(p -> {
                String n = p.getFileName().toString();
                int i = n.indexOf('_');
                if (i > 0) out.add(n.substring(0, i) + ":" + n.substring(i + 1));
            });
        } catch (IOException ignored) {
        }
        return out;
    }

    private static long clave(int rx, int rz) {
        return ((long) rx << 32) ^ (rz & 0xffffffffL);
    }

    /** La region (rx, rz): de memoria, del disco o nueva (si crear). Null si no hay y no se crea. */
    public Region region(int rx, int rz, boolean crear) {
        Region u = ultima;
        if (u != null && u.rx == rx && u.rz == rz) return u;
        long k = clave(rx, rz);
        if (!crear && k == ultimaNo) return null;
        Region r = regiones.get(k);
        if (r == null && !noHay.contains(k)) {
            r = leer(rx, rz);
            if (r != null) regiones.put(k, r);
            else noHay.add(k);
        }
        if (r == null && crear) {
            r = new Region(rx, rz);
            regiones.put(k, r);
            noHay.remove(k);
        }
        if (r != null) {
            ultima = r;
            if (ultimaNo == k) ultimaNo = Long.MIN_VALUE;
        } else ultimaNo = k;
        return r;
    }

    public int get(int x, int z) {
        Region r = region(Math.floorDiv(x, R), Math.floorDiv(z, R), false);
        return r == null ? 0 : r.px[Math.floorMod(z, R) * R + Math.floorMod(x, R)];
    }

    public java.util.Collection<Region> cargadas() {
        return regiones.values();
    }

    /** Todas las regiones con fichero o en memoria (para el mapa del libro). */
    public List<long[]> todas() {
        Set<Long> ks = new HashSet<>(regiones.keySet());
        if (Files.isDirectory(carpeta)) {
            try (var s = Files.list(carpeta)) {
                s.forEach(p -> {
                    String n = p.getFileName().toString();
                    if (!n.startsWith("r.") || !n.endsWith(".bin")) return;
                    String[] q = n.substring(2, n.length() - 4).split("\\.");
                    if (q.length != 2) return;
                    try {
                        ks.add(clave(Integer.parseInt(q[0]), Integer.parseInt(q[1])));
                    } catch (NumberFormatException ignored) {
                    }
                });
            } catch (IOException ignored) {
            }
        }
        List<long[]> out = new ArrayList<>();
        for (long k : ks) out.add(new long[]{(int) (k >> 32), (int) k});
        return out;
    }

    // ------------------------------------------------------------------ pintar un chunk

    /** Pinta lo que se ve del chunk desde arriba (en dimensiones con techo, el suelo a la altura del jugador). */
    public void escanear(ClientLevel lvl, LevelChunk ch, int yJugador) {
        ChunkPos cp = ch.getPos();
        int bx = cp.getMinBlockX(), bz = cp.getMinBlockZ();
        boolean techo = lvl.dimensionType().hasCeiling();
        int minY = lvl.getMinBuildHeight(), maxY = lvl.getMaxBuildHeight() - 1;
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        LevelChunk norte = lvl.getChunkSource().getChunk(cp.x, cp.z - 1, false);
        int[] alturaFila = new int[16];
        for (int x = 0; x < 16; x++) alturaFila[x] = norte != null ? superficie(lvl, norte, bx + x, bz - 1, techo, yJugador, minY, maxY, m) : Integer.MIN_VALUE;
        Region r = region(Math.floorDiv(bx, R), Math.floorDiv(bz, R), true);
        boolean cambio = false;
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                int wx = bx + x, wz = bz + z;
                int y = superficie(lvl, ch, wx, wz, techo, yJugador, minY, maxY, m);
                int col;
                if (y == Integer.MIN_VALUE) col = VACIO;
                else {
                    m.set(wx, y, wz);
                    BlockState st = ch.getBlockState(m);
                    int prof = 0;
                    if (st.getFluidState().is(FluidTags.WATER)) {
                        int yy = y - 1;
                        while (yy > minY && prof < 12 && ch.getBlockState(m.set(wx, yy, wz)).getFluidState().is(FluidTags.WATER)) {
                            prof++;
                            yy--;
                        }
                        m.set(wx, y, wz);
                    }
                    MapColor mc = st.getMapColor(lvl, m);
                    int rgb = mc == MapColor.NONE ? 0x202020 : mc.col;
                    float luz;
                    if (prof > 0 || st.getFluidState().is(FluidTags.WATER)) {
                        float d = prof * 0.1f + ((wx + wz) & 1) * 0.2f;
                        luz = d < 0.5f ? 1.0f : d > 0.9f ? 0.706f : 0.863f;
                    } else {
                        int yn = alturaFila[x];
                        luz = yn == Integer.MIN_VALUE ? 0.863f : y > yn ? 1.0f : y < yn ? 0.706f : 0.863f;
                    }
                    col = 0xff000000 | escala(rgb, luz);
                    alturaFila[x] = y;
                }
                int i = Math.floorMod(wz, R) * R + Math.floorMod(wx, R);
                if (r.px[i] != col) {
                    r.px[i] = col;
                    cambio = true;
                }
            }
        }
        if (cambio) {
            r.version++;
            r.sucia = true;
        }
    }

    private static int superficie(ClientLevel lvl, LevelChunk ch, int wx, int wz, boolean techo, int yJugador, int minY, int maxY,
                                  BlockPos.MutableBlockPos m) {
        if (!techo) {
            int y = ch.getHeight(Heightmap.Types.WORLD_SURFACE, wx, wz);
            while (y >= minY) {
                BlockState st = ch.getBlockState(m.set(wx, y, wz));
                if (!st.isAir() && st.getMapColor(lvl, m) != MapColor.NONE) return y;
                y--;
            }
            return Integer.MIN_VALUE;
        }
        // con techo (Nether): el primer suelo por debajo de la cabeza del jugador
        int y = Math.min(maxY - 1, yJugador + 2);
        int solidos = 0;
        while (y > minY && solidos < 8 && !ch.getBlockState(m.set(wx, y, wz)).isAir()) {
            y--;
            solidos++;
        }
        if (solidos >= 8) return Math.min(maxY - 1, yJugador + 2);
        while (y > minY) {
            BlockState st = ch.getBlockState(m.set(wx, y, wz));
            if (!st.isAir() && st.getMapColor(lvl, m) != MapColor.NONE) return y;
            y--;
        }
        return Integer.MIN_VALUE;
    }

    static int escala(int rgb, float f) {
        int r = Math.min(255, (int) (((rgb >> 16) & 255) * f)), g = Math.min(255, (int) (((rgb >> 8) & 255) * f)),
                b = Math.min(255, (int) ((rgb & 255) * f));
        return (r << 16) | (g << 8) | b;
    }

    // ------------------------------------------------------------------ disco

    private Path fichero(int rx, int rz) {
        return carpeta.resolve("r." + rx + "." + rz + ".bin");
    }

    private Region leer(int rx, int rz) {
        Path f = fichero(rx, rz);
        int[] p = PEND.get(f);
        if (p != null) {
            Region r = new Region(rx, rz);
            System.arraycopy(p, 0, r.px, 0, p.length);
            return r;
        }
        if (!Files.isRegularFile(f)) return null;
        try (InputStream in = new InflaterInputStream(Files.newInputStream(f)); DataInputStream d = new DataInputStream(in)) {
            if (d.readInt() != 0x44434531) return null;
            byte[] b = d.readNBytes(R * R * 4);
            if (b.length != R * R * 4) return null;
            Region r = new Region(rx, rz);
            ByteBuffer.wrap(b).asIntBuffer().get(r.px);
            return r;
        } catch (IOException | RuntimeException e) {
            DarkCraftMod.LOGGER.warn("Espejo: no se pudo leer {}: {}", f, e.toString());
            return null;
        }
    }

    /** Guarda en segundo plano las regiones que han cambiado. */
    public void guardar() {
        for (Region r : regiones.values()) {
            if (!r.sucia) continue;
            r.sucia = false;
            mandar(fichero(r.rx, r.rz), r.px.clone());
        }
    }

    private static void mandar(Path f, int[] copia) {
        PEND.put(f, copia);
        DISCO.submit(() -> {
            escribir(f, copia);
            PEND.remove(f, copia);
        });
    }

    private static void escribir(Path f, int[] px) {
        try {
            Files.createDirectories(f.getParent());
            Path tmp = f.resolveSibling(f.getFileName() + ".tmp");
            ByteBuffer bb = ByteBuffer.allocate(px.length * 4);
            bb.asIntBuffer().put(px);
            try (OutputStream out = new DeflaterOutputStream(Files.newOutputStream(tmp)); DataOutputStream d = new DataOutputStream(out)) {
                d.writeInt(0x44434531);
                d.write(bb.array());
            }
            Files.move(tmp, f, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try {
                Path tmp = f.resolveSibling(f.getFileName() + ".tmp");
                if (Files.exists(tmp)) Files.move(tmp, f, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
            }
            DarkCraftMod.LOGGER.warn("Espejo: no se pudo guardar {}: {}", f, e.toString());
        }
    }

    /** Suelta de memoria las regiones lejos de (x, z) que no se han tocado (despues de guardarlas). */
    public void soltarLejos(int x, int z, int distRegiones) {
        int rx = Math.floorDiv(x, R), rz = Math.floorDiv(z, R);
        regiones.values().removeIf(r -> {
            boolean lejos = Math.abs(r.rx - rx) > distRegiones || Math.abs(r.rz - rz) > distRegiones;
            if (lejos && r.sucia) {
                r.sucia = false;
                mandar(fichero(r.rx, r.rz), r.px.clone());
            }
            if (lejos && r == ultima) ultima = null;
            return lejos;
        });
    }
}
