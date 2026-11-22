package com.darkcraft.gen;

import java.util.HashSet;
import java.util.Set;

/** Una sala: rectangulo interior (sin muros), nivel del suelo y altura libre. */
public final class Room {
    public enum Type {
        GREAT_HALL, THRONE, DINING, KITCHEN, LIBRARY, LIBRARY_GRAND, BEDROOM, BATH, CELLAR, WINE_CELLAR,
        ARMORY, CHAPEL, STUDY, STORAGE, GALLERY, TREASURY, PRISON, CRYPT, ALCHEMY, BALLROOM, SERVANTS,
        BARRACKS, SMITHY, STABLE, OBSERVATORY, LIVING, EMPTY
    }

    public final Rect r;
    public final Type type;
    public int floorY;   // nivel donde se pisa (el suelo esta en floorY-1)
    /** Lados abiertos (sin pared): 1=norte, 2=sur, 4=oeste, 8=este. */
    public int open = 0;
    public int height;   // altura libre: aire de floorY a floorY+height-1
    /** Celdas de paso (puertas) que no se deben tapar con muebles. Clave = Room.key(x,z). */
    public final Set<Long> keepClear = new HashSet<>();
    /** Celdas pegadas a una ventana (ahi no van muebles altos ni cuadros). */
    public final Set<Long> windows = new HashSet<>();
    /** Hueco abierto encima de la sala (atrio de la biblioteca): ahi el techo de verdad esta en atriumTop. */
    public Rect atrium;
    public int atriumTop;

    public Room(Rect r, Type type) {
        this.r = r;
        this.type = type;
    }

    public int top() {
        return floorY + height - 1;
    }

    public static long key(int x, int z) {
        return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
    }

    /** Marca libre la celda de una puerta y sus vecinas dentro de la sala. */
    public void markDoor(int x, int z) {
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++)
                keepClear.add(key(x + dx, z + dz));
    }

    /**
     * Pasillo libre delante de una puerta: 'depth' casillas hacia dentro (nx, nz) y 'half' a cada lado.
     * Asi ningun mueble, pilar o sofa tapa la entrada.
     */
    public void markApproach(int x, int z, int nx, int nz, int depth, int half) {
        int px = -nz, pz = nx;
        for (int k = 0; k < depth; k++)
            for (int s = -half; s <= half; s++) {
                int cx = x + nx * k + px * s, cz = z + nz * k + pz * s;
                if (r.contains(cx, cz)) keepClear.add(key(cx, cz));
            }
    }

    public boolean clear(int x, int z) {
        return keepClear.contains(key(x, z));
    }

    public boolean window(int x, int z) {
        return windows.contains(key(x, z));
    }
}
