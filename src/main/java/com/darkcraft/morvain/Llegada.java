package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.TorreCupula;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * El Arco del Umbral: donde se llega a Morvain al cruzar el portal de la Fortaleza de Hueso, en las Llanuras Palidas
 * (respuestas 89-90 del plan), mirando al norte: a lo lejos la Tierra sin Fe y, en el cielo, la ciudadela encadenada.
 * Una plaza redonda de piedra vieja con pilares rotos, el santuario en el centro (con la guia sentada al lado, la pone
 * {@link Morvain}), al sur el arco del portal de vuelta (el mismo arco apuntado que el de la fortaleza, ya abierto: gemas
 * encendidas y el remolino) y un camino de tierra que sale hacia el norte. Se escribe al generar el terreno (sin estado).
 */
final class Llegada {
    private Llegada() {}

    /** El centro de la plaza (el santuario) y su suelo (lo que se pisa esta en Y + 1). */
    static final int X = 0, Z = 3400;
    static final int Y = (int) Math.floor(Terreno.columna(X, Z).altura());
    /** El radio de lo enlosado y hasta donde se funde con el terreno. */
    static final int PLAZA = 15, MEZCLA = 27;
    /** El plano del portal de vuelta (al sur, mirando al norte). */
    static final int PZ = Z + 15;
    /** Lo que sale el camino hacia el norte. */
    static final int CAMINO = 120;
    /** Los pilares rotos: angulo (grados; 90 es el sur) y alto. */
    static final int[][] PILARES = {{0, 7}, {42, 4}, {138, 9}, {180, 2}, {222, 6}, {318, 3}};

    /** Donde se aparece al cruzar el portal: los pies, delante del arco, mirando al norte. */
    static BlockPos pies() {
        return new BlockPos(X, Y + 1, Z + 10);
    }

    static BlockPos santuario() {
        return new BlockPos(X, Y + 1, Z);
    }

    /** Donde se sienta la guia (junto al santuario, mirando a quien llega por el arco). */
    static BlockPos guia() {
        return new BlockPos(X + 2, Y + 1, Z + 1);
    }

    /** El pie del hueco del portal de vuelta (donde va el remolino). */
    static BlockPos portal() {
        return new BlockPos(X, Y + 1, PZ);
    }

    /** Si el chunk que empieza en (x0, z0) tiene algo de la plaza o de su camino. */
    static boolean toca(int x0, int z0) {
        return x0 + 15 >= X - MEZCLA - 2 && x0 <= X + MEZCLA + 2 && z0 + 15 >= Z - CAMINO - 4 && z0 <= Z + MEZCLA + 2;
    }

    /** Si el chunk esta en la plaza o lo que la rodea (ahi no se ponen arboles ni rocas). */
    static boolean plaza(int x0, int z0) {
        return x0 + 15 >= X - MEZCLA - 4 && x0 <= X + MEZCLA + 4 && z0 + 15 >= Z - MEZCLA - 4 && z0 <= Z + MEZCLA + 4;
    }

    /** Si (x, z) esta cerca del arco de llegada (para el nombre del sitio). */
    static boolean cerca(double x, double z, double r) {
        return Math.hypot(x - X, z - (Z + 4)) <= r;
    }

    private static BlockState b(Block x) {
        return x.defaultBlockState();
    }

    /** (Los bloques del mod; en las vistas sin juego, unos de Minecraft que se le parecen.) */
    private static BlockState santuarioBloque() {
        try {
            return DarkCraftMod.SANTUARIO.get().defaultBlockState();
        } catch (Throwable t) {
            return Blocks.LODESTONE.defaultBlockState();
        }
    }

    private static BlockState portalBloque() {
        try {
            return DarkCraftMod.PORTAL_SOMBRA.get().defaultBlockState();
        } catch (Throwable t) {
            return Blocks.RED_STAINED_GLASS.defaultBlockState();
        }
    }

    // ------------------------------------------------------------------ escribir un chunk

    /** Escribe la plaza, el arco, los pilares y el camino en el chunk (tops: lo alto del terreno de cada columna). */
    static void chunk(MorvainGenerador.Escritor e, int x0, int z0, int[] tops) {
        for (int dz = 0; dz < 16; dz++)
            for (int dx = 0; dx < 16; dx++) {
                int x = x0 + dx, z = z0 + dz;
                int nat = tops[dz * 16 + dx];
                double r = Math.hypot(x - X, z - Z);
                boolean arco = Math.abs(x - X) <= 9 && z >= PZ - 3 && z <= PZ + 3;
                if (r <= PLAZA + 0.5 || arco) {
                    allanar(e, x, z, nat, Y, false);
                    e.poner(x, Y, z, losa(x, z, r));
                } else if (r < MEZCLA) {
                    int h = (int) Math.round(Ruido.lerp(Ruido.suave(PLAZA + 0.5, MEZCLA, r), Y, nat));
                    if (h != nat) allanar(e, x, z, nat, h, true);
                    else if (z < Z) camino(e, x, z, nat);
                } else camino(e, x, z, nat);
            }
        for (int[] p : PILARES) pilar(e, p[0], p[1]);
        arco(e);
        // el santuario (apagado; su nombre lo apunta Morvain la primera vez que llega alguien) y las farolas del arco
        e.poner(X, Y + 1, Z, santuarioBloque());
        // la estela del mapa de las Llanuras (parte 7d): donde empieza el camino del norte, mirando al santuario
        e.poner(X, Y + 1, Z - 11, Fragmentos.bloque());
        for (int s = -1; s <= 1; s += 2) {
            int fx = X + s * 9, fz = PZ - 2;
            e.poner(fx, Y + 1, fz, b(Blocks.POLISHED_BLACKSTONE));
            e.poner(fx, Y + 2, fz, b(Blocks.POLISHED_BLACKSTONE_WALL));
            e.poner(fx, Y + 3, fz, b(Blocks.SOUL_LANTERN));
        }
    }

    /** Sube o baja la columna hasta h (con tierra por debajo y hierba encima si {@code hierba}). */
    private static void allanar(MorvainGenerador.Escritor e, int x, int z, int nat, int h, boolean hierba) {
        for (int y = h + 1; y <= nat; y++) e.poner(x, y, z, b(Blocks.AIR));
        for (int y = nat + 1; y < h; y++) e.poner(x, y, z, b(Blocks.DIRT));
        if (hierba) e.poner(x, h, z, b(Blocks.GRASS_BLOCK));
        else if (h > nat) e.poner(x, h, z, b(Blocks.DIRT));
    }

    /** El enlosado: piedra vieja (con musgo y grietas), un anillo de piedra negra y la tarima del santuario. */
    private static BlockState losa(int x, int z, double r) {
        int k = Ciudadela.h(x, 7, z);
        if (r < 2.5) return k % 5 == 0 ? b(Blocks.CHISELED_POLISHED_BLACKSTONE) : b(Blocks.POLISHED_BLACKSTONE);
        if (r >= 5.5 && r < 6.5) return k % 4 == 0 ? b(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS) : b(Blocks.POLISHED_BLACKSTONE_BRICKS);
        // en el borde, se pierden losas (asoma la tierra)
        if (r > PLAZA - 2.5 && k % 5 == 0) return k % 3 == 0 ? b(Blocks.COARSE_DIRT) : b(Blocks.GRASS_BLOCK);
        int q = k % 20;
        return q < 3 ? b(Blocks.MOSSY_STONE_BRICKS) : q < 6 ? b(Blocks.CRACKED_STONE_BRICKS) : q == 6 ? b(Blocks.ANDESITE)
                : q == 7 ? b(Blocks.MOSSY_COBBLESTONE) : b(Blocks.STONE_BRICKS);
    }

    /** El camino de tierra que sale de la plaza hacia el norte (serpentea un poco). */
    private static void camino(MorvainGenerador.Escritor e, int x, int z, int nat) {
        if (z > Z - PLAZA + 1 || z < Z - CAMINO || nat <= Terreno.AGUA) return;
        double d = Z - z;
        double cx = X + 2.5 * Math.sin(d / 23.0) + 1.2 * Math.sin(d / 7.3);
        if (Math.abs(x + 0.5 - cx) > 1.6) return;
        int k = Ciudadela.h(x, 3, z) % 20;
        // se va borrando al final
        if (d > CAMINO - 25 && k < (d - (CAMINO - 25)) * 0.8) return;
        e.poner(x, nat, z, k < 10 ? b(Blocks.DIRT_PATH) : k < 15 ? b(Blocks.COARSE_DIRT) : k < 19 ? b(Blocks.GRAVEL) : b(Blocks.MOSSY_COBBLESTONE));
    }

    /** Un pilar roto de 2 x 2 en el borde de la plaza (los bajos, con su trozo caido al lado). */
    private static void pilar(MorvainGenerador.Escritor e, int grados, int alto) {
        double a = Math.toRadians(grados);
        int px = (int) Math.round(X + 12.5 * Math.cos(a)) - 1, pz = (int) Math.round(Z + 12.5 * Math.sin(a)) - 1;
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 2; j++) {
                int x = px + i, z = pz + j;
                e.poner(x, Y + 1, z, b(Blocks.CHISELED_STONE_BRICKS));
                // lo de arriba, mellado (cada bloque de la ultima fila puede faltar)
                for (int y = Y + 2; y <= Y + alto; y++) {
                    int k = Ciudadela.h(x, y, z);
                    if (y == Y + alto && k % 3 == 0) continue;
                    e.poner(x, y, z, k % 4 == 0 ? b(Blocks.MOSSY_STONE_BRICKS) : k % 7 == 0 ? b(Blocks.CRACKED_STONE_BRICKS) : b(Blocks.STONE_BRICKS));
                }
                if (alto >= 7) e.poner(x, Y + alto + 1, z, b(Blocks.STONE_BRICK_SLAB));
            }
        if (alto <= 4) {
            // el trozo que se cayo: tumbado hacia fuera de la plaza
            double ox = Math.cos(a), oz = Math.sin(a);
            BlockState t = b(Blocks.STONE_BRICKS);
            for (int s = 3; s <= 7; s++) {
                int x = (int) Math.round(px + 0.5 + ox * s), z = (int) Math.round(pz + 0.5 + oz * s);
                int k = Ciudadela.h(x, 1, z);
                e.poner(x, Y + 1, z, k % 3 == 0 ? b(Blocks.MOSSY_STONE_BRICKS) : t);
            }
        }
    }

    /**
     * El arco del portal de vuelta: el mismo arco apuntado que el de la fortaleza ({@link TorreCupula#anillo}), tres de
     * hondo, con las gemas encendidas (esta abierto) y el hueco de bloques del portal (el remolino lo dibuja).
     */
    private static void arco(MorvainGenerador.Escritor e) {
        int t = Y + 1;
        int[] gemas = {2, 5, 8};
        BlockState portal = portalBloque();
        for (int dx = -TorreCupula.MARCO; dx <= TorreCupula.MARCO; dx++)
            for (int h = 0; h <= TorreCupula.AALTO + 4; h++) {
                int x = X + dx, a = TorreCupula.anillo(dx, h), w = TorreCupula.arcoPortal(h);
                if (a == 0) {
                    e.poner(x, t + h, PZ, portal);
                    continue;
                }
                for (int g = -1; g <= 1; g++) {
                    BlockState st = null;
                    if (a == 1) st = h == 0 ? TorreCupula.hierroNegro()
                            : TorreCupula.huesoLabrado(w >= 0 && Math.abs(dx) == w + 1 ? Direction.Axis.Y : Direction.Axis.X);
                    else if (a == 2) {
                        boolean gemaLado = Math.abs(dx) == TorreCupula.AX + 2 && (h == gemas[0] || h == gemas[1] || h == gemas[2]);
                        boolean clave = dx == 0 && h == TorreCupula.AALTO + 1;
                        if (h <= 1) st = TorreCupula.hierroNegro();
                        else if ((gemaLado || clave) && g != 0) st = TorreCupula.gema(true);
                        else st = TorreCupula.piedraSombra();
                    } else if (h == 0 && Math.abs(dx) == TorreCupula.MARCO) st = TorreCupula.hierroNegro();
                    else if (h == TorreCupula.AALTO + 2 && Math.abs(dx) <= 1) st = TorreCupula.hierroNegro();
                    else if (h == TorreCupula.AALTO + 3 && dx == 0 && g == 0) st = TorreCupula.huesoLabrado(Direction.Axis.Y);
                    if (st != null) e.poner(x, t + h, PZ + g, st);
                }
            }
        // dos huesos tumbados a los pies del arco, a cada lado
        for (int s = -1; s <= 1; s += 2)
            e.poner(X + s * (TorreCupula.MARCO + 1), t, PZ, b(Blocks.BONE_BLOCK).setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
    }
}
