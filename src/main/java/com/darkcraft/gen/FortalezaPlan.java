package com.darkcraft.gen;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * La Fortaleza de Hueso entera (desde la parte 5a, {@link TorreCupula}: la torre bajo la cupula de cristal): la isla de
 * roca y ceniza, la fortaleza, el camino de huesos hasta el borde (donde llega el puente de la llave) y el mar de nubes
 * alrededor. Se construye chunk a chunk, como el castillo de Valdrenn.
 */
public final class FortalezaPlan {
    public final int cx, cz, Y;
    public final TorreCupula f;
    public final Island isla;
    /** Borde de la isla delante de la puerta: donde empieza el camino (z) y donde llega el puente (z + 1). */
    public final int bordeZ;
    private final List<Element> elementos = new ArrayList<>();
    private final Map<Long, List<Element>> indice = new HashMap<>();
    private final Set<String> avisados = new HashSet<>();

    /** La semilla del plano (para calcular cosas de la fortaleza de antes). */
    private final long semilla;

    public FortalezaPlan(long seed, int cx, int cz, int Y) {
        this.semilla = seed;
        this.cx = cx;
        this.cz = cz;
        this.Y = Y;
        Rng rng = new Rng(seed);
        isla = new Island(rng.fork(), cx, cz - 3, Y - 1, 74, 0.05, 6, 3, 52);
        isla.surface(this::suelo).subsurface(this::subsuelo).plants((x, z) -> false);
        elementos.add(isla);
        f = new TorreCupula(rng.fork(), cx, cz, Y);
        elementos.add(f);
        int b = cz;
        while (b < cz + 110 && isla.topAt(cx, b + 1) != Integer.MIN_VALUE && isla.topAt(cx - 2, b + 1) != Integer.MIN_VALUE
                && isla.topAt(cx + 2, b + 1) != Integer.MIN_VALUE) b++;
        bordeZ = b;
        elementos.add(new Camino(rng.fork()));
        elementos.add(new Ruinas(rng.fork()));
        elementos.add(new Nubes(rng.fork()));
        elementos.sort(java.util.Comparator.comparingInt(e -> e.layer));
        for (Element e : elementos)
            for (int x = e.x0 >> 4; x <= e.x1 >> 4; x++)
                for (int z = e.z0 >> 4; z <= e.z1 >> 4; z++) {
                    indice.computeIfAbsent(clave(x, z), k -> new ArrayList<>()).add(e);
                    if (!(e instanceof Nubes)) nucleo.add(clave(x, z));
                }
    }

    /**
     * El centro de la escalera de hueso de la fortaleza de antes (la de las murallas cuadradas, parte 3, con una isla mas
     * pequena): la misma isla con el radio de entonces, y la escalera delante de su borde sur, como en Puente.centro.
     */
    public int[] centroPuenteAntiguo() {
        Island vieja = new Island(new Rng(semilla).fork(), cx, cz - 3, Y - 1, 55, 0.05, 6, 3, 46);
        int b = cz;
        while (b < cz + 90 && vieja.topAt(cx, b + 1) != Integer.MIN_VALUE && vieja.topAt(cx - 2, b + 1) != Integer.MIN_VALUE
                && vieja.topAt(cx + 2, b + 1) != Integer.MIN_VALUE) b++;
        return new int[]{cx - 5, b + 13};
    }

    /** Los chunks de la isla y la fortaleza (sin el mar de nubes de alrededor): con ellos hechos, la fortaleza esta en pie. */
    private final Set<Long> nucleo = new java.util.HashSet<>();

    public Set<Long> nucleo() {
        return nucleo;
    }

    public static long clave(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) ^ (chunkZ & 0xffffffffL);
    }

    public boolean toca(int chunkX, int chunkZ) {
        return indice.containsKey(clave(chunkX, chunkZ));
    }

    /** Todos los chunks que toca (clave chunkX << 32 | chunkZ). */
    public Set<Long> chunks() {
        return indice.keySet();
    }

    public void construir(WorldGenLevel level, int chunkX, int chunkZ, RandomSource random) {
        List<Element> l = indice.get(clave(chunkX, chunkZ));
        if (l == null) return;
        Canvas c = new Canvas(level, chunkX, chunkZ, random);
        for (Element e : l) {
            try {
                e.build(c);
            } catch (Throwable t) {
                String k = e.getClass().getSimpleName();
                if (avisados.add(k)) DarkCraftMod.LOGGER.error("Error construyendo {} de la fortaleza en {},{}", k, chunkX, chunkZ, t);
            }
        }
    }

    /** Donde llega el puente de la llave: el primer bloque fuera de la isla delante del camino. */
    public int[] llegadaPuente() {
        return new int[]{cx, Y, bordeZ + 1};
    }

    // ------------------------------------------------------------------ la isla

    /** Suelo de ceniza y hueso: grava y toba grises, basalto, tierra de almas, algo de tierra seca y polvo de hueso. */
    private BlockState suelo(int x, int z, int top) {
        // manchas grandes (de varios bloques) y luego el grano de cada bloque
        double m = isla.mancha(x, z);
        int p = Hash.pct(x, top, z, 0xA5E);
        if (m > 0.35) return p < 55 ? Pal.b(Blocks.SOUL_SOIL) : p < 80 ? Pal.COARSE_DIRT : Pal.GRAVEL;
        if (m < -0.4) return p < 45 ? Pal.b(Blocks.CALCITE) : p < 70 ? TorreCupula.HUESO : Pal.b(Blocks.TUFF);
        if (p < 30) return Pal.GRAVEL;
        if (p < 52) return Pal.b(Blocks.TUFF);
        if (p < 66) return Pal.b(Blocks.SMOOTH_BASALT);
        if (p < 78) return Pal.COARSE_DIRT;
        if (p < 88) return Pal.b(Blocks.SOUL_SOIL);
        if (p < 94) return Pal.b(Blocks.CALCITE);
        return TorreCupula.HUESO;
    }

    /** Bajo el suelo: grava, toba y tierra de almas (nada de tierra marron en el borde de la isla). */
    private BlockState subsuelo(int x, int y, int z, int fromTop) {
        int p = Hash.pct(x, y, z, 0x5B5);
        if (p < 34) return Pal.GRAVEL;
        if (p < 62) return Pal.b(Blocks.TUFF);
        if (p < 80) return Pal.b(Blocks.SOUL_SOIL);
        if (p < 92) return Pal.b(Blocks.SMOOTH_BASALT);
        return Pal.COARSE_DIRT;
    }

    /** El camino de huesos desde la puerta hasta el borde, con el arco de colmillos donde llega el puente. */
    final class Camino extends Element {
        Camino(long seed) {
            super(seed);
            this.layer = 4;
            bounds(cx - 9, Y - 3, cz + TorreCupula.PFUERA + 7, cx + 9, Y + 14, bordeZ + 2);
        }

        @Override
        public void build(Canvas c) {
            int z0 = cz + TorreCupula.PFUERA + 8;
            for (int z = z0; z <= bordeZ; z++) {
                for (int x = cx - 2; x <= cx + 2; x++) {
                    BlockState s = Math.abs(x - cx) <= 1 ? Pal.b(Blocks.BLACKSTONE) : Pal.PB_BRICKS;
                    if (Math.abs(x - cx) == 0 && (z & 3) == 0) s = Pal.PB_CHISELED;
                    c.set(x, Y - 1, z, s);
                    for (int y = Y; y <= Y + 3; y++) c.set(x, y, z, Pal.AIR);
                    // que se vea macizo por debajo hasta la isla
                    for (int y = Y - 4; y < Y - 1; y++) if (c.isAir(x, y, z)) c.set(x, y, z, Pal.wall(x, y, z));
                }
                // postes de hueso con farol de almas cada 5 bloques
                if ((z - z0) % 5 == 2)
                    for (int s = -1; s <= 1; s += 2) {
                        int x = cx + s * 3;
                        c.set(x, Y - 1, z, Pal.PB_BRICKS);
                        for (int y = Y; y <= Y + 2; y++) c.set(x, y, z, TorreCupula.HUESO);
                        c.set(x, Y + 3, z, Pal.b(Blocks.SKELETON_SKULL));
                        c.set(x + s, Y + 2, z, Blocks.SOUL_LANTERN.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.LanternBlock.HANGING, false));
                    }
            }
            // el arco de dos colmillos al borde: aqui llega el puente
            for (int s = -1; s <= 1; s += 2)
                for (int i = 0; i <= 30; i++) {
                    double t = i / 30.0;
                    double x = cx + s * (4.5 - t * 3.2), y = Y - 1 + Math.sin(t * Math.PI * 0.55) * 12;
                    int bx = (int) Math.round(x), by = (int) Math.round(y);
                    c.set(bx, by, bordeZ, TorreCupula.HUESO);
                    if (t < 0.5) c.set(bx, by, bordeZ - 1, TorreCupula.HUESO);
                }
            c.set(cx, Y + 11, bordeZ, Pal.b(Blocks.SKELETON_SKULL));
            for (int s = -1; s <= 1; s += 2) c.set(cx + s, Y + 10, bordeZ, Pal.CHAIN);
            c.set(cx, Y + 9, bordeZ, Pal.SOUL_LANTERN_HANG);
        }
    }

    /** Lo que hay por la isla fuera de las murallas: arbustos secos, huesos, calaveras y alguna lapida. */
    final class Ruinas extends Element {
        Ruinas(long seed) {
            super(seed);
            this.layer = 4;
            bounds(isla.x0, Y - 8, isla.z0, isla.x1, Y + 4, isla.z1);
        }

        @Override
        public void build(Canvas c) {
            for (int x = Math.max(x0, c.minX); x <= Math.min(x1, c.maxX); x++)
                for (int z = Math.max(z0, c.minZ); z <= Math.min(z1, c.maxZ); z++) {
                    int top = isla.topAt(x, z);
                    if (top == Integer.MIN_VALUE || f.dentroMuros(x, z) || Math.abs(x - cx) <= 16 && z > cz + TorreCupula.RM - 4) continue;
                    if (Math.hypot(x - cx, z - cz) <= TorreCupula.RM + 7) continue; // las torrecillas y su sombra
                    if (!c.isAir(x, top + 1, z)) continue;
                    int p = Hash.h(x, top, z, seed) % 1000;
                    BlockState s = null;
                    if (p < 40) s = Pal.b(Blocks.DEAD_BUSH);
                    else if (p < 46) s = Pal.b(Blocks.SKELETON_SKULL);
                    else if (p < 52) s = TorreCupula.hueso(Hash.pct(x, 1, z, seed) < 50 ? Direction.Axis.X : Direction.Axis.Z);
                    else if (p < 54) s = Pal.b(Blocks.WITHER_ROSE);
                    else if (p < 58) s = Pal.b(Blocks.BROWN_MUSHROOM);
                    if (s == null) continue;
                    BlockState debajo = c.get(x, top, z);
                    if (s.is(Blocks.DEAD_BUSH) && !(debajo.is(Blocks.COARSE_DIRT) || debajo.is(Blocks.PODZOL) || debajo.is(Blocks.ROOTED_DIRT)))
                        continue;
                    if ((s.is(Blocks.WITHER_ROSE) || s.is(Blocks.BROWN_MUSHROOM)) && !(debajo.is(Blocks.SOUL_SOIL) || debajo.is(Blocks.PODZOL)
                            || debajo.is(Blocks.COARSE_DIRT)))
                        continue;
                    c.set(x, top + 1, z, s);
                }
            // lapidas de los guardias caidos, al oeste
            for (int k = 0; k < 7; k++) {
                int x = cx - TorreCupula.RM - 14 + (k % 3) * 3 - Hash.pct(k, 5, 0, seed) % 2, z = cz - 12 + (k / 3) * 5;
                int top = isla.topAt(x, z);
                if (top == Integer.MIN_VALUE) continue;
                c.set(x, top + 1, z, Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.EAST, false));
                c.set(x - 1, top + 1, z, Pal.b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            }
        }
    }

    /** El mar de nubes: bancos de nubes blancas alrededor y por debajo del borde de la isla. */
    final class Nubes extends Element {
        private final List<double[]> bolas = new ArrayList<>(); // x, y, z, rx, ry, rz

        Nubes(long seed) {
            super(seed);
            this.layer = 1;
            Rng r = new Rng(seed);
            for (int k = 0; k < 22; k++) {
                double a = r.nextDouble() * Math.PI * 2, d = 84 + r.nextDouble() * 40;
                double bx = cx + Math.cos(a) * d, bz = cz - 3 + Math.sin(a) * d, by = Y - 7 - r.nextDouble() * 9;
                // el sur (por donde llega el puente) mas despejado
                if (Math.sin(a) > 0.82 && d < 80) continue;
                int n = 3 + (int) (r.nextDouble() * 4);
                for (int i = 0; i < n; i++) {
                    double[] b = {bx + (r.nextDouble() - 0.5) * 16, by + (r.nextDouble() - 0.5) * 2.5, bz + (r.nextDouble() - 0.5) * 16,
                            4 + r.nextDouble() * 6, 1.6 + r.nextDouble() * 1.6, 4 + r.nextDouble() * 6};
                    // nada de nubes donde ira la escalera de hueso (al sur, delante de la puerta)
                    if (Math.hypot(b[0] - (cx - 5), b[2] - (bordeZ + 13)) < 28 + Math.max(b[3], b[5])) continue;
                    bolas.add(b);
                }
            }
            int R = 136;
            bounds(cx - R, Y - 22, cz - R, cx + R, Y, cz + R);
        }

        @Override
        public void build(Canvas c) {
            for (double[] b : bolas) {
                int ax = (int) Math.floor(b[0] - b[3]), bx = (int) Math.ceil(b[0] + b[3]);
                int az = (int) Math.floor(b[2] - b[5]), bz = (int) Math.ceil(b[2] + b[5]);
                if (bx < c.minX || ax > c.maxX || bz < c.minZ || az > c.maxZ) continue;
                int ay = (int) Math.floor(b[1] - b[4]), by = (int) Math.ceil(b[1] + b[4]);
                for (int x = Math.max(ax, c.minX); x <= Math.min(bx, c.maxX); x++)
                    for (int z = Math.max(az, c.minZ); z <= Math.min(bz, c.maxZ); z++)
                        for (int y = ay; y <= by; y++) {
                            double dx = (x + 0.5 - b[0]) / b[3], dy = (y + 0.5 - b[1]) / b[4], dz = (z + 0.5 - b[2]) / b[5];
                            double d = dx * dx + dy * dy + dz * dz;
                            if (d > 1) continue;
                            if (!c.isAir(x, y, z)) continue;
                            BlockState s = y < b[1] - b[4] * 0.45 ? Pal.b(Blocks.LIGHT_GRAY_WOOL)
                                    : d > 0.82 && Hash.pct(x, y, z, seed) < 40 ? Pal.b(Blocks.WHITE_STAINED_GLASS) : Pal.b(Blocks.WHITE_WOOL);
                            c.set(x, y, z, s);
                        }
            }
        }
    }
}
