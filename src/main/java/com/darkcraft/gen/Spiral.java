package com.darkcraft.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Escalera de caracol en un hueco de 5x5 alrededor de un pilar de 3x3 (sube 16 bloques por vuelta). */
public final class Spiral {
    private Spiral() {}

    public static final int[][] RING = new int[16][];

    static {
        int i = 0;
        for (int x = -2; x < 2; x++) RING[i++] = new int[]{x, -2};
        for (int z = -2; z < 2; z++) RING[i++] = new int[]{2, z};
        for (int x = 2; x > -2; x--) RING[i++] = new int[]{x, 2};
        for (int z = 2; z > -2; z--) RING[i++] = new int[]{-2, z};
    }

    /** True si (dx,dz) relativo al centro esta dentro del hueco 5x5 de la escalera. */
    public static boolean inWell(int dx, int dz) {
        return Math.abs(dx) <= 2 && Math.abs(dz) <= 2;
    }

    public static void build(Canvas c, int cx, int cz, int yStart, int yEnd, int phase, Block stairs, Canvas.StateFn pillar) {
        // Pilar central
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) {
                final int fdx = dx, fdz = dz;
                c.column(cx + dx, cz + dz, yStart - 1, yEnd + 3, (x, y, z) -> {
                    if (fdx == 0 && fdz == 0) return Pal.BASALT;
                    if ((y - yStart) % 9 == 4 && (fdx == 0 || fdz == 0)) return Pal.b(Blocks.SHROOMLIGHT);
                    return pillar.at(x, y, z);
                });
            }
        for (int i = 0; yStart + i <= yEnd; i++) {
            int y = yStart + i;
            int[] a = RING[Math.floorMod(i + phase, 16)];
            int[] b = RING[Math.floorMod(i + phase + 1, 16)];
            int x = cx + a[0], z = cz + a[1];
            boolean corner = Math.abs(a[0]) == 2 && Math.abs(a[1]) == 2;
            if (corner) {
                c.set(x, y, z, Pal.DS_TILES);
            } else {
                Direction d = Pal.dirOf(b[0] - a[0], b[1] - a[1]);
                c.set(x, y, z, Pal.stairs(stairs, d, false));
            }
            // espacio libre para la cabeza
            c.set(x, y + 1, z, Pal.AIR);
            c.set(x, y + 2, z, Pal.AIR);
            c.set(x, y + 3, z, Pal.AIR);
        }
    }
}
