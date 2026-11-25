package com.darkcraft.fortaleza;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.FortalezaHueso;
import com.darkcraft.gen.FortalezaPlan;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Pal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * El puente de hueso y nubes que levanta la llave delante de la puerta de la fortaleza: una gran escalera de caracol
 * (cuadrada, de tres de ancho, con descansillos en las esquinas) desde el suelo hasta la altura de la isla, y un puente
 * corto hasta el arco de colmillos donde empieza el camino. En su hueco sube la corriente de nubes (FortalezaEventos).
 * Se levanta poco a poco, de abajo arriba, en unos ocho segundos.
 */
public final class Puente {
    private Puente() {}

    /** Distancia al centro de la escalera desde el centro del hueco (anillo de los escalones: de IN a OUT). */
    static final int IN = 4, OUT = 6, HUECO = 3;

    /** Centro {x, z} de la escalera: delante de la puerta, al sur de la isla; su descansillo del noreste da al camino. */
    public static int[] centro(FortalezaPlan f) {
        return new int[]{f.cx - 5, f.bordeZ + 13};
    }

    /** Si (x, y, z) esta en el hueco de la escalera (donde sube la corriente). */
    public static boolean enHueco(FortalezaPlan f, int base, double x, double y, double z) {
        int[] c = centro(f);
        double dx = Math.abs(x - (c[0] + 0.5)), dz = Math.abs(z - (c[1] + 0.5));
        return Math.max(dx, dz) <= HUECO + 0.45 && y >= base - 1.5 && y <= f.Y + 4;
    }

    record Pieza(int x, int y, int z, BlockState s, boolean pata) {
        Pieza(int x, int y, int z, BlockState s) {
            this(x, y, z, s, false);
        }
    }

    /** Puentes que se estan levantando. */
    private static final List<Obra> OBRAS = new ArrayList<>();

    private static final class Obra {
        final ServerLevel lvl;
        final List<Pieza> piezas;
        final int porTick;
        int i;

        Obra(ServerLevel lvl, List<Pieza> piezas) {
            this.lvl = lvl;
            this.piezas = piezas;
            this.porTick = Math.max(6, piezas.size() / 170 + 1);
        }
    }

    /**
     * La altura (que se pisa) del suelo bajo la escalera: lo mas alto del terreno bajo ella (o el agua), sin contar lo
     * que haya muy arriba (nubes de la fortaleza, ramas de arboles altos).
     */
    public static int base(ServerLevel lvl, FortalezaPlan f) {
        // en el mundo de nubes no hay suelo: la escalera sale de la plazoleta del camino de nubes
        if (com.darkcraft.world.NubesGenerador.es(lvl)) return com.darkcraft.world.NubesGenerador.SUELO;
        int[] c = centro(f);
        int g = lvl.getMinBuildHeight() + 1;
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dx = -OUT - 2; dx <= OUT + 2; dx += 2)
            for (int dz = -OUT - 2; dz <= OUT + 2; dz += 2) {
                int x = c[0] + dx, z = c[1] + dz;
                int y = Math.min(lvl.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), f.Y - 26);
                for (; y > lvl.getMinBuildHeight(); y--) {
                    BlockState b = lvl.getBlockState(m.set(x, y - 1, z));
                    if (b.blocksMotion() || !b.getFluidState().isEmpty()) break;
                }
                g = Math.max(g, y);
            }
        return Math.min(g, f.Y);
    }

    /** Empieza a levantar la escalera (devuelve la altura de su base). */
    public static int levantar(ServerLevel lvl, FortalezaPlan f) {
        int g = base(lvl, f);
        OBRAS.add(new Obra(lvl, piezas(f, g)));
        return g;
    }

    public static boolean levantando() {
        return !OBRAS.isEmpty();
    }

    /** Cada tick del servidor: unas cuantas piezas de cada obra, con polvo de hueso, nubes y el sonido. */
    public static void tick(MinecraftServer srv) {
        Iterator<Obra> it = OBRAS.iterator();
        while (it.hasNext()) {
            Obra o = it.next();
            int fin = Math.min(o.piezas.size(), o.i + o.porTick);
            BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
            for (; o.i < fin; o.i++) {
                Pieza p = o.piezas.get(o.i);
                m.set(p.x, p.y, p.z);
                if (!o.lvl.isLoaded(m)) continue;
                BlockState ahora = o.lvl.getBlockState(m);
                if (ahora.is(Blocks.BEDROCK) || o.lvl.getBlockEntity(m) != null) continue;
                if (p.pata) {
                    // patas de hueso hasta el terreno: solo bajo algo ya puesto y mientras haya aire
                    BlockState encima = o.lvl.getBlockState(m.above());
                    if (encima.isAir() || !(ahora.isAir() || ahora.canBeReplaced())) continue;
                } else if (p.s.isAir() && ahora.isAir()) continue;
                o.lvl.setBlock(m, p.s, 2);
                if (!p.s.isAir() && (o.i & 3) == 0) {
                    o.lvl.sendParticles(ParticleTypes.CLOUD, p.x + 0.5, p.y + 0.8, p.z + 0.5, 1, 0.3, 0.2, 0.3, 0.01);
                    o.lvl.sendParticles(ParticleTypes.WHITE_ASH, p.x + 0.5, p.y + 1.0, p.z + 0.5, 3, 0.4, 0.3, 0.4, 0.02);
                }
                if (o.i % 140 == 0)
                    o.lvl.playSound(null, p.x + 0.5, p.y + 0.5, p.z + 0.5, DarkCraftMod.SFX_PUENTE_HUESO.get(), SoundSource.BLOCKS, 2.2f,
                            0.9f + (o.i % 3) * 0.06f);
            }
            if (o.i >= o.piezas.size()) it.remove();
        }
    }

    // ------------------------------------------------------------------ las piezas

    private static final BlockState HUESO_Y = FortalezaHueso.HUESO;

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
            out.add(new Pieza(mid[0], p.h - 2, mid[1], FortalezaHueso.hueso(p.dir.getAxis() == Direction.Axis.X ? Direction.Axis.X : Direction.Axis.Z)));
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
                out.add(new Pieza(cx + i, y, cz - r, FortalezaHueso.hueso(Direction.Axis.X)));
                out.add(new Pieza(cx + i, y, cz + r, FortalezaHueso.hueso(Direction.Axis.X)));
                out.add(new Pieza(cx - r, y, cz + i, FortalezaHueso.hueso(Direction.Axis.Z)));
                out.add(new Pieza(cx + r, y, cz + i, FortalezaHueso.hueso(Direction.Axis.Z)));
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
