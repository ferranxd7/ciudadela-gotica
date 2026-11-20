package com.ciudadela.gen;

import java.util.HashSet;
import java.util.Set;

/** Una sala: rectangulo interior (sin muros), nivel del suelo y altura libre. */
public final class Room {
    public enum Type {
        GREAT_HALL, THRONE, DINING, KITCHEN, LIBRARY, LIBRARY_GRAND, BEDROOM, BATH, CELLAR, WINE_CELLAR,
        ARMORY, CHAPEL, STUDY, STORAGE, GALLERY, TREASURY, PRISON, CRYPT, ALCHEMY, BALLROOM, SERVANTS,
        BARRACKS, SMITHY, STABLE, OBSERVATORY, EMPTY
    }

    public final Rect r;
    public final Type type;
    public int floorY;   // nivel donde se pisa (el suelo esta en floorY-1)
    /** Lados abiertos (sin pared): 1=norte, 2=sur, 4=oeste, 8=este. */
    public int open = 0;
    public int height;   // altura libre: aire de floorY a floorY+height-1
    /** Celdas de paso (puertas) que no se deben tapar con muebles. Clave = Room.key(x,z). */
    public final Set<Long> keepClear = new HashSet<>();

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

    public boolean clear(int x, int z) {
        return keepClear.contains(key(x, z));
    }
}
