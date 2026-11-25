package com.darkcraft.pueblo.gen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * Comprueba que un sitio se puede recorrer A PIE (para las pruebas y para disenar sin abrir el juego): desde un punto,
 * todo lo que alcanza un jugador andando, saltando un bloque, dejandose caer hasta cuatro y subiendo por escaleras de
 * mano. Las puertas cuentan como abiertas. No entiende de nadar ni de volteretas: si esto dice que se llega, se llega.
 */
public final class Andar {
    private Andar() {}

    public interface Mundo {
        BlockState en(int x, int y, int z);
    }

    private static final int[][] LADOS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    /** Lo alto de lo que choca en ese bloque (0 = se atraviesa; 1 = bloque entero; 0,5 = media losa). */
    private static double tope(BlockState s) {
        if (s.isAir() || s.getBlock() instanceof DoorBlock || s.getBlock() instanceof FenceGateBlock) return 0;
        VoxelShape v = s.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        if (v.isEmpty()) return 0;
        double m = v.max(Direction.Axis.Y);
        return m < 0.2 ? 0 : m;
    }

    private static boolean pasa(Mundo m, int x, int y, int z) {
        return tope(m.en(x, y, z)) == 0;
    }

    private static boolean trepa(Mundo m, int x, int y, int z) {
        BlockState s = m.en(x, y, z);
        // (fuera del juego las etiquetas no estan cargadas: la escalera de mano, por su clase)
        return s.getBlock() instanceof LadderBlock || s.is(BlockTags.CLIMBABLE);
    }

    /** Si se puede estar de pie SOBRE el bloque (x, y, z): devuelve a que altura quedan los pies, o -1. */
    private static double pies(Mundo m, int x, int y, int z) {
        double t = tope(m.en(x, y, z));
        if (t == 0 || t > 1.01) return -1;
        if (!pasa(m, x, y + 1, z) || !pasa(m, x, y + 2, z)) return -1;
        return y + t;
    }

    /** Una celda alcanzada: {x, y, z} del bloque que se pisa (de pie) o en el que se esta agarrado (trepando). */
    public static final class Zona {
        private final Set<Long> depie = new HashSet<>(), trepando = new HashSet<>();

        public int celdas() {
            return depie.size() + trepando.size();
        }

        /** Si desde lo alcanzado se llega con la mano a ese bloque (a menos de {@code radio} en planta; lo pisado puede quedar hasta cinco por debajo: una campana, lo alto de un estante). */
        public boolean alcanza(int x, int y, int z, double radio) {
            int r = (int) Math.ceil(radio);
            for (int dx = -r; dx <= r; dx++)
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz > radio * radio) continue;
                    for (int dy = -5; dy <= 2; dy++) {
                        long k = BlockPos.asLong(x + dx, y + dy, z + dz);
                        if (depie.contains(k) || trepando.contains(k)) return true;
                    }
                }
            return false;
        }

        /** Si se puede estar de pie justo en esa columna, a esa altura mas o menos. */
        public boolean pisa(int x, int y, int z) {
            for (int dy = -2; dy <= 1; dy++) if (depie.contains(BlockPos.asLong(x, y + dy, z))) return true;
            return false;
        }
    }

    /**
     * Todo lo que se alcanza andando desde (x, y, z) (y = donde van los pies), sin salir de la caja en planta
     * (x0..x1, z0..z1) ni pasar de {@code tope} celdas.
     */
    public static Zona desde(Mundo m, int x, int y, int z, int x0, int z0, int x1, int z1, int tope) {
        Zona zona = new Zona();
        ArrayDeque<long[]> cola = new ArrayDeque<>();
        // el suelo bajo el punto de salida
        for (int by = y; by >= y - 3; by--) {
            if (pies(m, x, by, z) >= 0) {
                zona.depie.add(BlockPos.asLong(x, by, z));
                cola.add(new long[]{x, by, z, 0});
                break;
            }
        }
        while (!cola.isEmpty() && zona.celdas() < tope) {
            long[] a = cola.poll();
            int ax = (int) a[0], ay = (int) a[1], az = (int) a[2];
            boolean trepa = a[3] == 1;
            double fa = trepa ? ay : pies(m, ax, ay, az);
            if (trepa) {
                // arriba y abajo por la escalera
                for (int d = -1; d <= 1; d += 2)
                    if (trepa(m, ax, ay + d, az) && zona.trepando.add(BlockPos.asLong(ax, ay + d, az))) cola.add(new long[]{ax, ay + d, az, 1});
                // al pie de la escalera, en su misma columna
                if (pies(m, ax, ay - 1, az) >= 0 && zona.depie.add(BlockPos.asLong(ax, ay - 1, az))) cola.add(new long[]{ax, ay - 1, az, 0});
            } else if (trepa(m, ax, ay + 1, az) && zona.trepando.add(BlockPos.asLong(ax, ay + 1, az))) cola.add(new long[]{ax, ay + 1, az, 1});
            for (int[] l : LADOS) {
                int bx = ax + l[0], bz = az + l[1];
                if (bx < x0 || bx > x1 || bz < z0 || bz > z1) continue;
                // a una escalera de mano de la columna de al lado (a la altura de los pies o de la cintura)
                for (int by = (int) Math.floor(fa) - 1; by <= (int) Math.floor(fa) + 1; by++)
                    if (trepa(m, bx, by, bz) && pasa(m, bx, by + 1, bz) | trepa(m, bx, by + 1, bz) && zona.trepando.add(BlockPos.asLong(bx, by, bz)))
                        cola.add(new long[]{bx, by, bz, 1});
                // a pie: subir hasta 1,25 (saltando) o bajar hasta 4
                for (int by = (int) Math.floor(fa) + 1; by >= (int) Math.floor(fa) - 5; by--) {
                    double fb = pies(m, bx, by, bz);
                    if (fb < 0) continue;
                    double sube = fb - fa;
                    if (sube > (trepa ? 1.05 : 1.25) || sube < -4.01) continue;
                    // hueco para el cuerpo: sobre B hasta la altura de A, y sobre A si hay que saltar
                    boolean libre = true;
                    int hasta = (int) Math.floor(Math.max(fa, fb) + 1.79);
                    for (int yy = by + 1; yy <= hasta && libre; yy++) if (!pasa(m, bx, yy, bz)) libre = false;
                    if (sube > 0.6 && !trepa) {
                        int techo = (int) Math.floor(fb + 1.79);
                        for (int yy = (int) Math.floor(fa); yy <= techo && libre; yy++) if (yy > ay && !pasa(m, ax, yy, az)) libre = false;
                    }
                    if (!libre) continue;
                    if (zona.depie.add(BlockPos.asLong(bx, by, bz))) cola.add(new long[]{bx, by, bz, 0});
                    break;
                }
            }
        }
        return zona;
    }
}
