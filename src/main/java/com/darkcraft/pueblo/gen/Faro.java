package com.darkcraft.pueblo.gen;

import com.darkcraft.fortaleza.Perdidos;
import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Pal;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * El faro de Brumavela, en el penon del noreste: una torre encalada de siete de ancho con el zocalo de sillar, la
 * escalera de caracol por dentro (alrededor de un pilar; sube doce por vuelta, con rellanos llanos en las esquinas),
 * la galeria con su barandilla y la linterna de cristal donde el Capitan subio el corazon del barco: mientras este
 * ahi, la linterna alumbra.
 */
public final class Faro extends Element {
    public static final int X = Terreno.PENON_X + 4, Z = Terreno.PENON_Z - 4;
    /** Lo que sube la escalera (dos vueltas). */
    public static final int ALTO = 24;

    /** Lo que se pisa abajo y en la linterna. */
    public final int y, linterna;
    private final Terreno terreno;

    /** El anillo de la escalera, en el sentido de las agujas del reloj desde la esquina del noroeste. */
    private static final int[][] ANILLO = new int[16][];

    static {
        int i = 0;
        for (int x = -2; x < 2; x++) ANILLO[i++] = new int[]{x, -2};
        for (int z = -2; z < 2; z++) ANILLO[i++] = new int[]{2, z};
        for (int x = 2; x > -2; x--) ANILLO[i++] = new int[]{x, 2};
        for (int z = 2; z > -2; z--) ANILLO[i++] = new int[]{-2, z};
    }

    /** La puerta da al oeste (por donde llega la rampa): la celda del anillo de detras de la puerta. */
    private static final int ENTRADA = 14;

    public Faro(long seed, PuebloPlan plan) {
        super(seed);
        this.layer = 3;
        this.terreno = plan.terreno;
        int alto = Terreno.NADA;
        for (int dx = -4; dx <= 4; dx++)
            for (int dz = -4; dz <= 4; dz++) alto = Math.max(alto, terreno.alto(X + dx, Z + dz));
        this.y = alto + 1;
        this.linterna = y + ALTO;
        bounds(X - 6, y - 8, Z - 6, X + 6, linterna + 12, Z + 6);
        plan.sitio("faro", X - 6, y, Z);
        plan.sitio("faro_alto", X - 2, linterna, Z - 1);
        plan.sitio("corazon", X, linterna + 2, Z);
        plan.sitio("nota_capitan", X + 1, linterna, Z - 2);
    }

    private static boolean muro(int dx, int dz) {
        return Math.max(Math.abs(dx), Math.abs(dz)) == 3 && Math.abs(dx) + Math.abs(dz) <= 5;
    }

    private static boolean dentro(int dx, int dz) {
        return Math.max(Math.abs(dx), Math.abs(dz)) <= 2;
    }

    @Override
    public void build(Canvas c) {
        // el zocalo: un ochavo de sillar mas ancho, cuatro hiladas
        for (int dx = -4; dx <= 4; dx++)
            for (int dz = -4; dz <= 4; dz++) {
                if (Math.abs(dx) + Math.abs(dz) > 6) continue;
                int x = X + dx, z = Z + dz;
                int t = terreno.alto(x, z);
                boolean falda = !muro(dx, dz) && !dentro(dx, dz);
                for (int yy = t == Terreno.NADA ? y - 6 : Math.min(t + 1, y - 1); yy <= y - 1; yy++) c.set(x, yy, z, Mat.sillar(x, yy, z, seed));
                if (falda) {
                    int h = Math.max(Math.abs(dx), Math.abs(dz)) == 4 ? (Math.abs(dx) + Math.abs(dz) >= 6 ? 0 : 1) : 3;
                    for (int k = 0; k < h; k++) c.set(x, y + k, z, Mat.sillar(x, y + k, z, seed));
                    if (h == 1) c.set(x, y + 1, z, Pal.slab(Blocks.STONE_BRICK_SLAB, false));
                }
            }
        // el fuste
        for (int dx = -3; dx <= 3; dx++)
            for (int dz = -3; dz <= 3; dz++) {
                int x = X + dx, z = Z + dz;
                if (dentro(dx, dz)) {
                    for (int yy = y; yy <= linterna + 3; yy++) c.set(x, yy, z, Pal.AIR);
                    continue;
                }
                if (!muro(dx, dz)) continue;
                for (int yy = y; yy <= linterna - 1; yy++) {
                    BlockState s = yy <= y + 2 ? Mat.sillar(x, yy, z, seed) : yy == y + 13 || yy == y + 14 ? banda(x, yy, z) : Mat.cal(x, yy, z, seed);
                    // saeteras: una por cara y altura, girando
                    int cara = dx == 3 ? 0 : dz == 3 ? 1 : dx == -3 ? 2 : 3;
                    int k = Math.abs(dx) == 3 ? dz : dx;
                    if (k == 0 && yy > y + 4 && yy < linterna - 3 && Math.floorMod(yy - y - 5 - cara * 2, 8) < 2) {
                        c.unir(x, yy, z, Pal.IRON_BARS);
                        continue;
                    }
                    c.set(x, yy, z, s);
                }
            }
        // la puerta, al oeste, con su dintel
        c.set(X - 3, y, Z, Pal.AIR);
        c.set(X - 3, y + 1, Z, Pal.AIR);
        c.set(X - 3, y + 2, Z, Pal.b(Blocks.CHISELED_STONE_BRICKS));
        c.set(X - 4, y, Z, Pal.AIR);
        c.set(X - 4, y + 1, Z, Pal.AIR);
        c.set(X - 4, y + 2, Z, Pal.AIR);
        c.set(X - 4, y - 1, Z, Mat.sillar(X - 4, y - 1, Z, seed));
        escalera(c);
        galeria(c);
        linterna(c);
    }

    /** La franja oscura a media altura. */
    private BlockState banda(int x, int yy, int z) {
        return Hash.pct(x, yy, z, seed + 3) < 70 ? Pal.b(Blocks.GRAY_TERRACOTTA) : Pal.b(Blocks.CYAN_TERRACOTTA);
    }

    private void escalera(Canvas c) {
        // el pilar
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++)
                for (int yy = y - 1; yy <= linterna - 1; yy++) c.set(X + dx, yy, Z + dz, dx == 0 && dz == 0 ? Pal.log(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.Y) : Mat.muro(X + dx, yy, Z + dz, seed + 9));
        // el suelo de abajo
        for (int[] a : ANILLO) c.set(X + a[0], y - 1, Z + a[1], Mat.losas(X + a[0], y - 1, Z + a[1], seed));
        int idx = ENTRADA, ult = y - 1, puestos = 0;
        while (puestos < ALTO) {
            idx = (idx + 1) & 15;
            int[] a = ANILLO[idx], b = ANILLO[(idx + 1) & 15];
            int x = X + a[0], z = Z + a[1];
            if ((idx & 3) == 0) {
                // rellano: a la altura a la que deja el ultimo peldano
                c.set(x, ult, z, Mat.losas(x, ult, z, seed));
            } else {
                ult++;
                puestos++;
                c.set(x, ult, z, Pal.stairs(puestos % 7 == 3 ? Blocks.COBBLESTONE_STAIRS : Blocks.STONE_BRICK_STAIRS, Pal.dirOf(b[0] - a[0], b[1] - a[1]), false));
                if (puestos % 6 == 2 && ult > y + 4) c.set(x, ult - 1, z, Pal.SOUL_LANTERN_HANG);
            }
            for (int k = 1; k <= 3; k++) c.set(x, ult + k, z, Pal.AIR);
        }
    }

    private void galeria(Canvas c) {
        int g = linterna - 1;
        // el suelo de la linterna: todo menos el hueco por donde llega la escalera
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++) {
                boolean hueco = (dx == -2 && dz >= 0) || (dz == 2 && dx <= 0);
                if (hueco || Math.max(Math.abs(dx), Math.abs(dz)) <= 1) continue;
                c.set(X + dx, g, Z + dz, Mat.losas(X + dx, g, Z + dz, seed));
            }
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) c.set(X + dx, g, Z + dz, Pal.b(Blocks.POLISHED_ANDESITE));
        // la galeria de fuera: un vuelo de piedra sobre mensulas y la barandilla
        for (int dx = -5; dx <= 5; dx++)
            for (int dz = -5; dz <= 5; dz++) {
                int m = Math.max(Math.abs(dx), Math.abs(dz)), s = Math.abs(dx) + Math.abs(dz);
                if (m < 3 || s > 8 || dentro(dx, dz)) continue;
                int x = X + dx, z = Z + dz;
                if (muro(dx, dz)) {
                    c.set(x, g, z, Mat.sillar(x, g, z, seed));
                    continue;
                }
                c.set(x, g, z, m == 5 || s == 8 ? Pal.slab(Blocks.STONE_BRICK_SLAB, true) : Mat.sillar(x, g, z, seed));
                if (m == 4 && s <= 6) c.set(x, g - 1, z, Pal.stairs(Blocks.STONE_BRICK_STAIRS, Pal.dirOf(-dx, -dz), true));
                if (m == 5 || s == 8) c.unir(x, g + 1, z, Pal.DO_FENCE);
            }
    }

    private void linterna(Canvas c) {
        int g = linterna;
        for (int dx = -3; dx <= 3; dx++)
            for (int dz = -3; dz <= 3; dz++) {
                if (!muro(dx, dz)) continue;
                int x = X + dx, z = Z + dz;
                boolean poste = Math.abs(dx) + Math.abs(dz) == 5;
                boolean puerta = (dx == -3 && dz == -1) || (dx == 3 && dz == 1);
                for (int yy = g; yy <= g + 3; yy++) {
                    if (poste) c.set(x, yy, z, Pal.log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Y));
                    else if (yy == g + 3) c.set(x, yy, z, Pal.DS_TILES);
                    else if (puerta && yy <= g + 1) c.set(x, yy, z, Pal.AIR);
                    else if (yy == g) c.set(x, yy, z, Mat.sillar(x, yy, z, seed));
                    else if (Hash.pct(x, yy, z, seed + 4) < 16) c.set(x, yy, z, Pal.AIR);
                    else c.unir(x, yy, z, Pal.b(Blocks.GLASS_PANE));
                }
            }
        // el pedestal del corazon y su luz (la quita el servidor cuando alguien se lo lleva)
        c.set(X, g, Z, Pal.PB_CHISELED);
        c.set(X, g + 1, Z, Pal.b(Blocks.POLISHED_BLACKSTONE_WALL));
        c.set(X, g + 2, Z, Blocks.LIGHT.defaultBlockState());
        for (int yy = g + 3; yy <= g + 7; yy++) c.set(X, yy, Z, Pal.CHAIN);
        // el diario del Capitan y su arcon
        c.lectern(X + 1, g, Z - 2, Direction.SOUTH, Perdidos.libro(16));
        c.chest(X + 2, g, Z - 1, Direction.WEST, Casa.BOTIN_BUENO);
        c.set(X + 2, g, Z + 2, Mat.barril(Direction.UP));
        c.set(X + 2, g + 2, Z - 2, Pal.COBWEB);
        // el tejado: una piramide de pizarra con alero y su pararrayos
        for (int k = 0; k <= 4; k++) {
            int m = 4 - k, yy = g + 4 + k;
            for (int dx = -m; dx <= m; dx++)
                for (int dz = -m; dz <= m; dz++) {
                    if (Math.abs(dx) != m && Math.abs(dz) != m) continue;
                    if (m == 4 && Math.abs(dx) + Math.abs(dz) > 6) continue;
                    int x = X + dx, z = Z + dz;
                    if (m == 0) {
                        c.set(x, yy, z, Pal.DS_TILES);
                        continue;
                    }
                    Direction d = Math.abs(dz) == m && Math.abs(dx) < m ? (dz < 0 ? Direction.SOUTH : Direction.NORTH)
                            : Math.abs(dx) == m && Math.abs(dz) < m ? (dx < 0 ? Direction.EAST : Direction.WEST)
                            : (dz < 0 ? Direction.SOUTH : Direction.NORTH);
                    c.unir(x, yy, z, Pal.stairs(Blocks.DEEPSLATE_TILE_STAIRS, d, false));
                }
        }
        c.set(X, g + 9, Z, Pal.b(Blocks.LIGHTNING_ROD));
    }
}
