package com.darkcraft.fortaleza;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.FortalezaPlan;
import com.darkcraft.gen.Hash;
import com.darkcraft.gen.Montana;
import com.darkcraft.gen.Pal;
import com.darkcraft.gen.TorreCupula;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * El puente largo de hueso y nubes (parte 5b, respuesta 36 del plan): lo tiende la llave de hueso desde el arco de
 * colmillos del mirador de la montana (gen/Montana) hasta el borde de la isla de la fortaleza, donde esta el otro arco.
 * Cinco de ancho, sube despacio (medias losas) hasta la altura del patio; barandillas de piedra negra con postes de
 * hueso, costillas de hueso que lo cruzan por encima, un espinazo de hueso por debajo, farolillos colgados y nubes.
 * Se forma poco a poco, del mirador a la isla, en unos diez segundos.
 */
public final class Puente {
    private Puente() {}

    /** Medio ancho del tablero (la barandilla va en MEDIO + 1). */
    static final int MEDIO = 2;

    /** El arco del mirador (donde se usa la llave): {x, y que se pisa, z}. */
    public static int[] arco(FortalezaPlan f) {
        return f.montana.arco();
    }

    /** El pie del sendero de la montana {x, z}: donde llega el camino de nubes. */
    public static int[] pie(FortalezaPlan f) {
        double[] p = f.montana.pie();
        return new int[]{(int) Math.floor(p[0]), (int) Math.floor(p[1])};
    }

    /** De donde a donde va (en z: z0 junto al mirador, z1 junto a la isla). */
    static int z0(FortalezaPlan f) {
        return f.montana.mz - Montana.RP - 1;
    }

    static int z1(FortalezaPlan f) {
        return f.bordeZ + 1;
    }

    /** Lo que se pisa en el puente a la altura z (de la cima del monte a la del patio, en medias losas). */
    public static double altura(FortalezaPlan f, int z) {
        int a = z0(f), b = z1(f);
        double t = Math.max(0, Math.min(1, (a - z) / (double) Math.max(1, a - b)));
        double h = f.montana.cima + (f.Y - f.montana.cima) * t;
        return Math.round(h * 2) / 2.0;
    }

    /** Si (x, y, z) esta sobre el puente (o casi). */
    public static boolean enPuente(FortalezaPlan f, double x, double y, double z) {
        int bz = (int) Math.floor(z);
        if (bz > z0(f) + 1 || bz < z1(f) - 1 || Math.abs(x - (f.cx + 0.5)) > MEDIO + 2) return false;
        double h = altura(f, Math.max(z1(f), Math.min(z0(f), bz)));
        return y > h - 3 && y < h + 6;
    }

    record Pieza(int x, int y, int z, BlockState s) {}

    /** Donde poner cada bloque (para las vistas sin juego y las pruebas). */
    public interface Poner {
        void en(int x, int y, int z, BlockState s);
    }

    /** El puente ya tendido, bloque a bloque, en el orden en que se forma. */
    public static void volcar(FortalezaPlan f, Poner p) {
        for (Pieza q : piezas(f)) p.en(q.x, q.y, q.z, q.s);
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
            this.porTick = Math.max(6, piezas.size() / 200 + 1);
        }
    }

    /** Empieza a tender el puente. */
    public static void levantar(ServerLevel lvl, FortalezaPlan f) {
        OBRAS.add(new Obra(lvl, piezas(f)));
    }

    public static boolean levantando() {
        return !OBRAS.isEmpty();
    }

    /** Quita el puente (lo que sigue siendo suyo; para dejar la fortaleza como al principio). */
    public static void quitar(ServerLevel lvl, FortalezaPlan f) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        var aire = Blocks.AIR.defaultBlockState();
        for (Pieza p : piezas(f)) {
            if (p.s.isAir()) continue;
            m.set(p.x, p.y, p.z);
            if (lvl.isLoaded(m) && lvl.getBlockState(m).is(p.s.getBlock())) lvl.setBlock(m, aire, 2);
        }
    }

    /** El puente ya tendido, en un chunk recien construido (si su trozo de montana o de nubes se hizo despues). */
    static void enChunk(ServerLevel lvl, FortalezaPlan f, int cx, int cz) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (Pieza p : piezas(f))
            if ((p.x >> 4) == cx && (p.z >> 4) == cz) lvl.setBlock(m.set(p.x, p.y, p.z), p.s, 2);
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
                if (p.s.isAir() && ahora.isAir()) continue;
                // los muretes se unen a sus vecinos (con la bandera 2 se quedarian en postes sueltos)
                o.lvl.setBlock(m, p.s, p.s.is(BlockTags.WALLS) ? 3 : 2);
                if (!p.s.isAir() && (o.i & 3) == 0) {
                    o.lvl.sendParticles(ParticleTypes.CLOUD, p.x + 0.5, p.y + 0.8, p.z + 0.5, 1, 0.3, 0.2, 0.3, 0.01);
                    o.lvl.sendParticles(ParticleTypes.WHITE_ASH, p.x + 0.5, p.y + 1.0, p.z + 0.5, 3, 0.4, 0.3, 0.4, 0.02);
                }
                if (o.i % 160 == 0)
                    o.lvl.playSound(null, p.x + 0.5, p.y + 0.5, p.z + 0.5, DarkCraftMod.SFX_PUENTE_HUESO.get(), SoundSource.BLOCKS, 2.4f,
                            0.9f + (o.i % 3) * 0.06f);
            }
            if (o.i >= o.piezas.size()) it.remove();
        }
    }

    // ------------------------------------------------------------------ las piezas

    private static final BlockState HUESO_Y = TorreCupula.HUESO;
    private static final BlockState HUESO_Z = TorreCupula.hueso(Direction.Axis.Z);
    private static final BlockState HUESO_X = TorreCupula.hueso(Direction.Axis.X);

    /** Todo lo que hay que poner, del mirador a la isla (cada fila de z entera antes que la siguiente). */
    static List<Pieza> piezas(FortalezaPlan f) {
        int cx = f.cx, a = z0(f), b = z1(f);
        List<Pieza> out = new ArrayList<>();
        for (int z = a; z >= b; z--) {
            int k = a - z;
            double q = altura(f, z);
            int n = (int) Math.floor(q);
            boolean media = q - n > 0.25;
            // el tablero: espinazo de hueso en el centro, piedra negra y losas a los lados
            for (int dx = -MEDIO; dx <= MEDIO; dx++) {
                int x = cx + dx;
                BlockState s = dx == 0 ? HUESO_Z : Math.abs(dx) == 1 ? Pal.PB_BRICKS : ((z & 1) == 0 ? Pal.DS_TILES : Pal.PB_BRICKS_CRACKED);
                out.add(new Pieza(x, n - 1, z, s));
                if (media) out.add(new Pieza(x, n, z, Math.abs(dx) <= 1
                        ? Pal.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, false) : Pal.slab(Blocks.DEEPSLATE_TILE_SLAB, false)));
                for (int y = media ? n + 1 : n; y <= n + 4; y++) out.add(new Pieza(x, y, z, Pal.AIR));
            }
            // por debajo: la viga del centro y, cada cuatro, una vertebra
            out.add(new Pieza(cx, n - 2, z, HUESO_Z));
            if (k % 4 == 0) {
                out.add(new Pieza(cx - 1, n - 2, z, HUESO_X));
                out.add(new Pieza(cx + 1, n - 2, z, HUESO_X));
                out.add(new Pieza(cx, n - 3, z, HUESO_Y));
            }
            // las barandillas: viga de piedra, murete y postes de hueso (con calavera de vez en cuando)
            for (int s = -1; s <= 1; s += 2) {
                int x = cx + s * (MEDIO + 1);
                out.add(new Pieza(x, n - 1, z, Pal.PB_BRICKS));
                int y = media ? n + 1 : n;
                if (media) out.add(new Pieza(x, n, z, Pal.PB_BRICKS));
                if (k % 3 == 0) {
                    out.add(new Pieza(x, y, z, HUESO_Y));
                    if (k % 12 == 6) out.add(new Pieza(x, y + 1, z, Pal.b(Blocks.SKELETON_SKULL)));
                } else out.add(new Pieza(x, y, z, Pal.b(Blocks.POLISHED_BLACKSTONE_BRICK_WALL)));
            }
            // una costilla de hueso por encima cada seis, con un farolillo colgado de una de cada dos
            if (k % 6 == 3) {
                int base = media ? n + 1 : n;
                for (int i = 0; i <= 24; i++) {
                    double t = Math.PI * i / 24;
                    int x = cx + (int) Math.round(Math.cos(t) * (MEDIO + 1.2));
                    int y = base + (int) Math.round(Math.sin(t) * 5.2);
                    out.add(new Pieza(x, y, z, Math.abs(Math.cos(t)) > 0.6 ? HUESO_Y : HUESO_X));
                }
                if (k % 12 == 9) {
                    out.add(new Pieza(cx, base + 4, z, Pal.CHAIN));
                    out.add(new Pieza(cx, base + 3, z, Pal.SOUL_LANTERN_HANG));
                }
            }
            // nubes por debajo, a un lado y a otro
            if (k % 9 == 4) nube(out, cx, cx + ((k / 9) % 2 == 0 ? -3 : 3), n - 3, z, k);
        }
        return out;
    }

    /** Una nube (lana blanca, gris por debajo y algo de cristal blanco en el borde), colgando del puente. */
    private static void nube(List<Pieza> out, int cx, int x0, int y0, int z0, int k) {
        for (int dx = -4; dx <= 4; dx++)
            for (int dy = -1; dy <= 1; dy++)
                for (int dz = -3; dz <= 3; dz++) {
                    double d = dx * dx / 16.0 + dy * dy / 2.2 + dz * dz / 9.0;
                    if (d > 1.0) continue;
                    // que no tape la viga ni las vertebras de debajo del tablero
                    if (dy == 1 && Math.abs(x0 + dx - cx) <= 1) continue;
                    BlockState s = dy < 0 ? Pal.b(Blocks.LIGHT_GRAY_WOOL)
                            : d > 0.7 && Hash.pct(x0 + dx, y0 + dy, z0 + dz, k) < 50 ? Pal.b(Blocks.WHITE_STAINED_GLASS) : Pal.b(Blocks.WHITE_WOOL);
                    out.add(new Pieza(x0 + dx, y0 + dy, z0 + dz, s));
                }
    }
}
