package com.darkcraft.fortaleza;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.FortalezaPlan;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Pal;
import com.darkcraft.gen.TorreCupula;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * La escalera de caracol de hueso de la parte 5a (la que levantaba la llave delante de la isla, con la corriente de
 * nubes en su hueco). Desde la 5b se sube por la montana: esto solo sirve para quitarla, pieza a pieza, de un mundo
 * donde ya se levanto (FortalezaEventos, al pasar la fortaleza a la version 3).
 */
final class EscaleraVieja {
    private EscaleraVieja() {}

    static final int IN = 4, OUT = 6, HUECO = 3;

    /** Centro {x, z} de la escalera: delante de la puerta, al sur de la isla. */
    static int[] centro(FortalezaPlan f) {
        return new int[]{f.cx - 5, f.bordeZ + 13};
    }

    record Pieza(int x, int y, int z, BlockState s, boolean pata) {
        Pieza(int x, int y, int z, BlockState s) {
            this(x, y, z, s, false);
        }
    }

    /** Quita la escalera que se levanto desde la altura g: solo los bloques que siguen siendo suyos. Devuelve cuantos. */
    static int quitar(ServerLevel lvl, FortalezaPlan f, int g) {
        int n = 0;
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        var aire = Blocks.AIR.defaultBlockState();
        for (Pieza p : piezas(f, g)) {
            if (p.s.isAir()) continue;
            m.set(p.x, p.y, p.z);
            if (!lvl.isLoaded(m)) continue;
            if (lvl.getBlockState(m).is(p.s.getBlock())) {
                lvl.setBlock(m, aire, 2 | 16);
                n++;
            }
        }
        return n;
    }

    private static final BlockState HUESO_Y = TorreCupula.HUESO;


    private static BlockState peldano(Direction d) {
        return Pal.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, d, false);
    }

    /** Todo lo que hay que poner, de abajo arriba. */
    static List<Pieza> piezas(FortalezaPlan f, int g) {
        int[] c = centro(f);
        int cx = c[0], cz = c[1], Y = f.Y;
        List<Pieza> out = new ArrayList<>();
        // 1) la base: una plataforma de piedra negra con un anillo de hueso, sobre el suelo
        for (int dx = -OUT - 2; dx <= OUT + 2; dx++)
            for (int dz = -OUT - 2; dz <= OUT + 2; dz++) {
                int ch = Math.max(Math.abs(dx), Math.abs(dz));
                BlockState s = ch == OUT + 2 ? HUESO_Y : ch <= HUECO ? Pal.PB_CHISELED : ((dx + dz) & 1) == 0 ? Pal.PB_BRICKS : Pal.DS_TILES;
                out.add(new Pieza(cx + dx, g - 1, cz + dz, s));
                for (int y = g; y <= g + 2; y++) out.add(new Pieza(cx + dx, y, cz + dz, Pal.AIR));
                // patas de hueso hasta el terreno (si quedara hueco debajo) por el borde, como mucho 12
                if (ch == OUT + 2 && ((dx + dz) & 3) == 0)
                    for (int y = g - 2; y >= g - 13; y--) out.add(new Pieza(cx + dx, y, cz + dz, HUESO_Y, true));
            }
        // el hueco de la corriente de nubes, despejado de arriba abajo
        for (int y = g; y <= Y + 3; y++)
            for (int dx = -HUECO; dx <= HUECO; dx++)
                for (int dz = -HUECO; dz <= HUECO; dz++) out.add(new Pieza(cx + dx, y, cz + dz, Pal.AIR));
        // 2) los escalones de abajo arriba, y a la vez (para que todo crezca de abajo arriba) los cuatro pilares de hueso de
        // las esquinas, con un anillo de hueso que los une en cada vuelta: de lejos se ve una torre de huesos
        List<Paso> pasos = pasos(cx, cz, Y, g);
        int pilar = g - 1;
        for (int k = pasos.size() - 1; k >= 0; k--) {
            Paso p = pasos.get(k);
            for (; pilar < Math.min(p.h, Y - 1); ) pilares(out, cx, cz, ++pilar, g);
            for (int[] cel : p.celdas) {
                out.add(new Pieza(cel[0], p.h - 1, cel[1], p.descanso ? suelo(cel[0], cel[1]) : peldano(p.dir)));
                for (int y = p.h; y <= p.h + 2; y++) out.add(new Pieza(cel[0], y, cel[1], Pal.AIR));
            }
            // el espinazo: hueso debajo de la celda del medio
            int[] mid = p.celdas.get(p.celdas.size() / 2);
            out.add(new Pieza(mid[0], p.h - 2, mid[1], TorreCupula.hueso(p.dir.getAxis() == Direction.Axis.X ? Direction.Axis.X : Direction.Axis.Z)));
            // barandilla de hueso por fuera cada dos escalones, con calavera de vez en cuando; nubes cada cinco
            int[] fuera = p.fuera;
            if (fuera != null && k % 2 == 0) {
                out.add(new Pieza(fuera[0], p.h, fuera[1], HUESO_Y));
                if (k % 10 == 0) out.add(new Pieza(fuera[0], p.h + 1, fuera[1], Pal.b(Blocks.SKELETON_SKULL)));
                else if (k % 14 == 6) out.add(new Pieza(fuera[0], p.h + 1, fuera[1], Pal.SOUL_LANTERN));
            }
            if (fuera != null && k % 5 == 2) nube(out, fuera[0] + p.ox, p.h - 2, fuera[1] + p.oz, k);
        }
        for (; pilar < Y - 1; ) pilares(out, cx, cz, ++pilar, g);
        // 3) el puente corto hasta el arco de colmillos: hueso con nubes a los lados y costillas por encima
        for (int z = cz - OUT - 1; z >= f.bordeZ + 1; z--) {
            for (int x = cx + IN; x <= cx + OUT; x++) {
                out.add(new Pieza(x, Y - 1, z, x == cx + IN + 1 ? Pal.b(Blocks.BLACKSTONE) : HUESO_Y));
                for (int y = Y; y <= Y + 3; y++) out.add(new Pieza(x, y, z, Pal.AIR));
            }
            for (int s = 0; s <= 1; s++) {
                int x = s == 0 ? cx + IN - 1 : cx + OUT + 1;
                out.add(new Pieza(x, Y - 1, z, Pal.b(Blocks.WHITE_WOOL)));
                out.add(new Pieza(x, Y - 2, z, Pal.b(Blocks.LIGHT_GRAY_WOOL)));
                if ((z & 1) == 0) out.add(new Pieza(x, Y, z, Pal.b(Blocks.WHITE_STAINED_GLASS)));
            }
            if (((z - f.bordeZ) % 3) == 2)
                for (int i = 0; i <= 8; i++) {
                    double t = i / 8.0;
                    int x = (int) Math.round(cx + IN - 1 + t * (OUT - IN + 2));
                    int y = Y + 1 + (int) Math.round(Math.sin(t * Math.PI) * 3.2);
                    out.add(new Pieza(x, y, z, HUESO_Y));
                }
        }
        return out;
    }

    /** Los cuatro pilares de hueso de las esquinas a la altura y, y cada 28 bloques el anillo que los une (por fuera de los escalones). */
    private static void pilares(List<Pieza> out, int cx, int cz, int y, int g) {
        int r = OUT + 1;
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2) out.add(new Pieza(cx + sx * r, y, cz + sz * r, HUESO_Y));
        if ((y - g) % 28 == 20)
            for (int i = -r + 1; i <= r - 1; i++) {
                out.add(new Pieza(cx + i, y, cz - r, TorreCupula.hueso(Direction.Axis.X)));
                out.add(new Pieza(cx + i, y, cz + r, TorreCupula.hueso(Direction.Axis.X)));
                out.add(new Pieza(cx - r, y, cz + i, TorreCupula.hueso(Direction.Axis.Z)));
                out.add(new Pieza(cx + r, y, cz + i, TorreCupula.hueso(Direction.Axis.Z)));
            }
    }

    private static BlockState suelo(int x, int z) {
        return Hash.pct(x, 0, z, 0x5EE) < 25 ? Pal.PB_CHISELED : HUESO_Y;
    }

    /** Una nube pequena (lana blanca, gris por debajo y algo de cristal blanco en el borde). */
    private static void nube(List<Pieza> out, int x0, int y0, int z0, int k) {
        for (int dx = -2; dx <= 2; dx++)
            for (int dy = -1; dy <= 1; dy++)
                for (int dz = -2; dz <= 2; dz++) {
                    double d = dx * dx / 5.0 + dy * dy / 1.6 + dz * dz / 5.0;
                    if (d > 1.0) continue;
                    BlockState s = dy < 0 ? Pal.b(Blocks.LIGHT_GRAY_WOOL)
                            : d > 0.7 && Hash.pct(x0 + dx, y0 + dy, z0 + dz, k) < 50 ? Pal.b(Blocks.WHITE_STAINED_GLASS) : Pal.b(Blocks.WHITE_WOOL);
                    out.add(new Pieza(x0 + dx, y0 + dy, z0 + dz, s));
                }
    }

    /** Un escalon (o descansillo): su altura (la que se pisa), hacia donde se sube y sus celdas. */
    static final class Paso {
        final int h;
        final boolean descanso;
        final Direction dir;
        final List<int[]> celdas = new ArrayList<>();
        int[] fuera;
        int ox, oz;

        Paso(int h, boolean descanso, Direction dir) {
            this.h = h;
            this.descanso = descanso;
            this.dir = dir;
        }
    }

    /**
     * Los escalones de arriba abajo: el descansillo del noreste a la altura Y (da al puente), y bajando por el lado norte,
     * el oeste, el sur y el este, vuelta tras vuelta, hasta el suelo (altura g).
     */
    static List<Paso> pasos(int cx, int cz, int Y, int g) {
        List<Paso> l = new ArrayList<>();
        int h = Y;
        // descansillos: NE, NO, SO, SE (en el orden de bajada) y lados entre ellos
        int vuelta = 0;
        while (h > g && vuelta < 40) {
            // NE (arriba del todo) o el que toque
            l.add(descanso(cx, cz, 1, -1, h));
            // lado norte, bajando hacia el oeste (se sube hacia el este)
            for (int i = 3; i >= -3 && h > g; i--) {
                Paso p = new Paso(h, false, Direction.EAST);
                for (int dz = -OUT; dz <= -IN; dz++) p.celdas.add(new int[]{cx + i, cz + dz});
                p.fuera = new int[]{cx + i, cz - OUT - 1};
                p.oz = -2;
                l.add(p);
                h--;
            }
            if (h <= g) break;
            l.add(descanso(cx, cz, -1, -1, h));
            // lado oeste, bajando hacia el sur (se sube hacia el norte)
            for (int i = -3; i <= 3 && h > g; i++) {
                Paso p = new Paso(h, false, Direction.NORTH);
                for (int dx = -OUT; dx <= -IN; dx++) p.celdas.add(new int[]{cx + dx, cz + i});
                p.fuera = new int[]{cx - OUT - 1, cz + i};
                p.ox = -2;
                l.add(p);
                h--;
            }
            if (h <= g) break;
            l.add(descanso(cx, cz, -1, 1, h));
            // lado sur, bajando hacia el este (se sube hacia el oeste)
            for (int i = -3; i <= 3 && h > g; i++) {
                Paso p = new Paso(h, false, Direction.WEST);
                for (int dz = IN; dz <= OUT; dz++) p.celdas.add(new int[]{cx + i, cz + dz});
                p.fuera = new int[]{cx + i, cz + OUT + 1};
                p.oz = 2;
                l.add(p);
                h--;
            }
            if (h <= g) break;
            l.add(descanso(cx, cz, 1, 1, h));
            // lado este, bajando hacia el norte (se sube hacia el sur)
            for (int i = 3; i >= -3 && h > g; i--) {
                Paso p = new Paso(h, false, Direction.SOUTH);
                for (int dx = IN; dx <= OUT; dx++) p.celdas.add(new int[]{cx + dx, cz + i});
                p.fuera = new int[]{cx + OUT + 1, cz + i};
                p.ox = 2;
                l.add(p);
                h--;
            }
            vuelta++;
        }
        return l;
    }

    private static Paso descanso(int cx, int cz, int sx, int sz, int h) {
        Paso p = new Paso(h, true, Direction.NORTH);
        for (int a = IN; a <= OUT; a++)
            for (int b = IN; b <= OUT; b++) p.celdas.add(new int[]{cx + sx * a, cz + sz * b});
        p.fuera = new int[]{cx + sx * (OUT + 1), cz + sz * (OUT + 1)};
        p.ox = sx * 2;
        p.oz = sz * 2;
        return p;
    }
}
