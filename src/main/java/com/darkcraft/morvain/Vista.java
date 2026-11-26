package com.darkcraft.morvain;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Para disenar sin abrir el juego ({@code tools/vista3d/Volcado.java morvain_*}): escribe lo que generaria Morvain en un
 * trozo del mapa (el terreno con su suelo, el Arco del Umbral y la ciudadela con sus cadenas), chunk a chunk como el
 * generador, desde una altura para arriba.
 */
public final class Vista {
    private Vista() {}

    public interface Poner {
        void poner(int x, int y, int z, BlockState st);
    }

    public static void volcar(Poner poner, int x0, int z0, int x1, int z1, int ymin) {
        BlockState agua = Blocks.WATER.defaultBlockState();
        for (int cx = x0 >> 4; cx <= x1 >> 4; cx++)
            for (int cz = z0 >> 4; cz <= z1 >> 4; cz++) {
                int bx = cx << 4, bz = cz << 4;
                MorvainGenerador.Escritor es = (x, y, z, st) -> {
                    if (x < Math.max(bx, x0) || x > Math.min(bx + 15, x1) || z < Math.max(bz, z0) || z > Math.min(bz + 15, z1) || y < ymin
                            || y < 1 || y >= Morvain.ALTO) return;
                    poner.poner(x, y, z, st);
                };
                MorvainGenerador.Trozo t = new MorvainGenerador.Trozo(bx, bz);
                int[] tops = new int[256];
                for (int dz = 0; dz < 16; dz++)
                    for (int dx = 0; dx < 16; dx++) {
                        Terreno.Columna col = t.c[dz][dx];
                        int x = bx + dx, z = bz + dz;
                        int top = Math.max(1, Math.min(Morvain.ALTO - 2, (int) Math.floor(col.altura())));
                        tops[dz * 16 + dx] = top;
                        Suelo.Capas capas = Suelo.capas(col, x, z, top, t.pendiente(dx, dz));
                        for (int y = Math.max(ymin, top - 6); y <= top; y++) es.poner(x, y, z, capas.en(y));
                        for (int y = top + 1; y <= Terreno.AGUA; y++) es.poner(x, y, z, agua);
                    }
                if (Llegada.toca(bx, bz)) Llegada.chunk(es, bx, bz, tops);
                if (Ciudadela.toca(bx, bz)) Ciudadela.chunk(es, bx, bz);
            }
    }

    /** Donde esta el Arco del Umbral: {x, y del suelo, z, z del portal}. */
    public static int[] llegada() {
        return new int[]{Llegada.X, Llegada.Y, Llegada.Z, Llegada.PZ};
    }

    /** El centro del eslabon i de la cadena c (0 la del este; en el centro de un eslabon no hay hierro): {x, y, z}. */
    public static int[] eslabon(int c, int i) {
        double[] p = Ciudadela.CADENAS[c].centro(i);
        return new int[]{(int) Math.floor(p[0]), (int) Math.floor(p[1]), (int) Math.floor(p[2])};
    }

    public static int eslabones(int c) {
        return Ciudadela.CADENAS[c].eslabones;
    }

    /** Lo mas alto de la ciudadela: la gema de la punta de la aguja, en (0, cima, 0). */
    public static int cima() {
        return Ciudadela.CIMA;
    }

    /** Donde estan las anclas de las cadenas: {x, y, z} de cada una. */
    public static int[][] anclas() {
        int[][] o = new int[Ciudadela.CADENAS.length][];
        for (int i = 0; i < o.length; i++) {
            Ciudadela.Cadena c = Ciudadela.CADENAS[i];
            o[i] = new int[]{(int) Math.floor(c.bx), c.suelo, (int) Math.floor(c.bz)};
        }
        return o;
    }
}
