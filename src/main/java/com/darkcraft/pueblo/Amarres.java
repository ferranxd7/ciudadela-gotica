package com.darkcraft.pueblo;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.cine.Shot;
import com.darkcraft.pueblo.gen.Barco;
import com.darkcraft.pueblo.gen.MuelleValdrenn;
import com.darkcraft.santuario.Santuarios;
import com.darkcraft.world.MundoNubes;
import com.darkcraft.world.NubeBlock;
import com.darkcraft.world.NubesGenerador;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntBinaryOperator;

/**
 * Los dos amarres de La Garza Gris (parte 3d): el de <b>Brumavela</b> (el espigon de la cala, donde se encuentra rota) y
 * el del <b>muelle de Valdrenn</b>, al pie de la Torre de Ascenso. El barco es el mismo en los dos, desplazado
 * ({@link #de}: lo que hay que sumar a las coordenadas de Brumavela) y con la proa al sur.
 * <p>
 * El muelle de Valdrenn no se genera con el mundo (asi sale igual en los mundos de antes): se hace la primera vez que
 * alguien se acerca o zarpa hacia alli ({@link #pedir} y {@link #tick}): primero se <b>calma el mar de nubes</b> alrededor
 * (como en la cala de Brumavela: llano a ras del casco, y se funde con los bollos de siempre), un chunk por tick, y
 * despues se pone el anden ({@link MuelleValdrenn}) con su santuario. La campana de Brumavela (para llamar al barco
 * cuando esta en Valdrenn) se pone tambien aqui, en el espigon.
 */
public final class Amarres {
    private Amarres() {}

    public static final int BRUMAVELA = 0, VALDRENN = 1;

    /** El mar en calma del muelle de Valdrenn: su centro (en las coordenadas del barco), el radio llano y hasta donde se funde. */
    public static final int CALMA_X = -6, CALMA_Z = 139, CALMA_R0 = 36, CALMA_R1 = 56, CALMA_Y = 246;
    /** La campana del espigon de Brumavela (el poste; la campana va encima). */
    public static final int CAMPANA_BV_X = 2, CAMPANA_BV_Z = 135;
    public static final String SANTUARIO = "darkcraft.santuario.muelle_valdrenn";

    private static final int[] CERO = {0, 0};
    private static final int CALLADO = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    private static int[] valdrenn;
    private static long semilla;
    private static boolean buscado;

    /** Lo que hay que sumar a las coordenadas del barco en Brumavela para ese amarre: {dx, dz}; null si no existe aqui. */
    public static int[] de(ServerLevel lvl, int cual) {
        if (cual == BRUMAVELA) return CERO;
        if (!buscado || semilla != lvl.getSeed()) {
            // el camino de nubes sale de la torre hacia el sur por x = ascX; el anden va pegado a su oeste
            BlockPos l = MundoNubes.llegada(lvl.getSeed());
            valdrenn = l == null ? null : new int[]{l.getX() - 3, l.getZ() - 116};
            semilla = lvl.getSeed();
            buscado = true;
        }
        return valdrenn;
    }

    /** Lo alto del mar de nubes en cada columna de ese amarre (en las coordenadas del mundo). */
    public static IntBinaryOperator cima(ServerLevel lvl, int cual) {
        int[] o = de(lvl, cual);
        if (cual == BRUMAVELA || o == null) return NubesGenerador::cima;
        return (x, z) -> cimaValdrenn(x, z, o[0], o[1]);
    }

    /** El mar de nubes del muelle de Valdrenn: llano cerca del amarre y como siempre a partir de {@link #CALMA_R1}. */
    public static int cimaValdrenn(int x, int z, int dx, int dz) {
        int nat = NubesGenerador.cima(x, z);
        double ex = x - (CALMA_X + dx), ez = z - (CALMA_Z + dz), d2 = ex * ex + ez * ez;
        if (d2 >= CALMA_R1 * CALMA_R1) return nat;
        double d = Math.sqrt(d2);
        double k = d <= CALMA_R0 ? 1 : (CALMA_R1 - d) / (double) (CALMA_R1 - CALMA_R0);
        k = k * k * (3 - 2 * k);
        double calma = CALMA_Y + 0.9 * Math.sin(x * 0.29 + 1.3) * Math.cos(z * 0.23 - 0.7);
        return (int) Math.round(nat + (calma - nat) * k);
    }

    /** Si (x, z) cae en el muelle de Valdrenn o en el barco amarrado a el. */
    public static boolean enMuelle(ServerLevel lvl, double x, double z) {
        int[] o = de(lvl, VALDRENN);
        if (o == null) return false;
        double rx = x - o[0], rz = z - o[1];
        return rx >= Barco.BX - 7 && rx <= MuelleValdrenn.X1 + 1 && rz >= Barco.POPA - 2 && rz <= Barco.PROA + 8;
    }

    /** Si esta a menos de {@code r} bloques del amarre de Valdrenn. */
    public static boolean cerca(ServerLevel lvl, double x, double z, double r) {
        int[] o = de(lvl, VALDRENN);
        if (o == null) return false;
        double ex = x - (CALMA_X + o[0]), ez = z - (CALMA_Z + o[1]);
        return ex * ex + ez * ez < r * r;
    }

    // ------------------------------------------------------------------ donde se deja a quien llega

    /** A donde se mira al llegar (en las coordenadas del barco): en Valdrenn, la Torre de Ascenso; en Brumavela, el pueblo. */
    public static double[] vista(int cual) {
        return cual == VALDRENN ? new double[]{3.5, 270, 92.5} : new double[]{1.5, 266, 74.5};
    }

    private static final double[][] ALCAZAR = {{-7.5, 127.5}, {-5.5, 126.5}, {-9.5, 126.5}, {-6.5, 123.5}, {-9.5, 123.5}, {-5.5, 124.5}, {-10.5, 127.5}, {-4.5, 127.5}};

    /** Donde queda el viajero numero {@code n} al llegar: en el alcazar, junto al timon, mirando a tierra. {x, y, z, giro, cabeceo}. */
    public static double[] alcazar(ServerLevel lvl, int cual, int n) {
        int[] o = de(lvl, cual);
        double[] s = ALCAZAR[Math.floorMod(n, ALCAZAR.length)], v = vista(cual);
        double y = Barco.ALCAZAR_Y + 1;
        float[] a = Shot.angles(new double[]{s[0], y + 1.62, s[1], v[0], v[1], v[2]});
        return new double[]{s[0] + o[0], y, s[1] + o[1], a[0], a[1]};
    }

    /** Un sitio firme junto al amarre (por si aun no hay barco que pisar): {x, y, z, giro, cabeceo}. */
    public static double[] seguro(ServerLevel lvl, int cual) {
        if (cual == BRUMAVELA) return new double[]{0.5, Barco.CUBIERTA + 1, 138.5, 90, 0};
        int[] o = de(lvl, cual);
        if (Pueblo.Datos.get(lvl.getServer()).muelle)
            return new double[]{MuelleValdrenn.SEGURO_X + 0.5 + o[0], MuelleValdrenn.Y + 1, MuelleValdrenn.SEGURO_Z + 0.5 + o[1], 90, 0};
        // sin anden todavia: en el camino de nubes, delante de la torre
        return new double[]{3.5 + o[0], NubesGenerador.SUELO, 116.5 + o[1], 180, 0};
    }

    /** Donde esta la campana con la que se llama al barco desde ese amarre (el bloque de la campana). */
    public static BlockPos campana(ServerLevel lvl, int cual) {
        int[] o = de(lvl, cual);
        if (o == null) return null;
        return cual == BRUMAVELA ? new BlockPos(CAMPANA_BV_X, Barco.CUBIERTA + 2, CAMPANA_BV_Z)
                : new BlockPos(MuelleValdrenn.CAMPANA_X + o[0], MuelleValdrenn.Y + 2, MuelleValdrenn.CAMPANA_Z + o[1]);
    }

    // ------------------------------------------------------------------ hacer el muelle de Valdrenn

    private static int pedido = -1;
    private static List<long[]> porHacer;
    private static int hechos, cambiados;

    private static final net.minecraft.server.level.TicketType<net.minecraft.world.level.ChunkPos> OBRA =
            net.minecraft.server.level.TicketType.create("darkcraft_muelle", java.util.Comparator.comparingLong(net.minecraft.world.level.ChunkPos::toLong), 300);
    private static int billete = -1;

    /**
     * Alguien esta cerca del muelle de Valdrenn o va hacia alli: que se haga en cuanto esten cargados sus chunks (se
     * piden aqui, para que no dependa de la distancia de vision de nadie).
     */
    public static void pedir(ServerLevel lvl) {
        int ahora = lvl.getServer().getTickCount();
        pedido = ahora + 100;
        int[] o = de(lvl, VALDRENN);
        if (o == null || (billete >= 0 && ahora < billete)) return;
        billete = ahora + 100;
        net.minecraft.world.level.ChunkPos cp = new net.minecraft.world.level.ChunkPos((CALMA_X + o[0]) >> 4, (CALMA_Z + o[1]) >> 4);
        lvl.getChunkSource().addRegionTicket(OBRA, cp, 6, cp);
    }

    public static void olvidar() {
        porHacer = null;
        pedido = -1;
        billete = -1;
        buscado = false;
    }

    /** Si falta por hacer el muelle (y hay rumbo a Valdrenn). */
    public static boolean falta(ServerLevel lvl) {
        return de(lvl, VALDRENN) != null && !Pueblo.Datos.get(lvl.getServer()).muelle;
    }

    /** Cada tick del servidor: un paso de la obra del muelle, si esta pedida. */
    static void tick(ServerLevel lvl) {
        if (pedido < 0 || lvl.getServer().getTickCount() > pedido) {
            porHacer = null;
            return;
        }
        Pueblo.Datos d = Pueblo.Datos.get(lvl.getServer());
        int[] o = de(lvl, VALDRENN);
        if (d.muelle || o == null) return;
        int cx = CALMA_X + o[0], cz = CALMA_Z + o[1];
        if (porHacer == null) {
            List<long[]> l = new ArrayList<>();
            for (int x = (cx - CALMA_R1) >> 4; x <= (cx + CALMA_R1) >> 4; x++)
                for (int z = (cz - CALMA_R1) >> 4; z <= (cz + CALMA_R1) >> 4; z++) {
                    if (lvl.getChunkSource().getChunkNow(x, z) == null) return;
                    l.add(new long[]{x, z});
                }
            porHacer = l;
            hechos = cambiados = 0;
        }
        if (hechos < porHacer.size()) {
            long[] c = porHacer.get(hechos);
            if (lvl.getChunkSource().getChunkNow((int) c[0], (int) c[1]) == null) {
                porHacer = null;
                return;
            }
            cambiados += calmar(lvl, (int) c[0], (int) c[1], o);
            hechos++;
            return;
        }
        anden(lvl, o);
        d.muelle = true;
        d.cambio();
        porHacer = null;
        DarkCraftMod.LOGGER.info("[DarkCraft] El muelle de Valdrenn, hecho en {}, {} (el mar de nubes en calma: {} bloques cambiados)",
                MuelleValdrenn.SEGURO_X + o[0], MuelleValdrenn.SEGURO_Z + o[1], cambiados);
    }

    /** Hace el muelle de una vez (para las pruebas y los comandos): carga lo que haga falta. */
    public static boolean hacerYa(ServerLevel lvl) {
        Pueblo.Datos d = Pueblo.Datos.get(lvl.getServer());
        int[] o = de(lvl, VALDRENN);
        if (o == null) return false;
        if (d.muelle) return true;
        int cx = CALMA_X + o[0], cz = CALMA_Z + o[1], n = 0;
        for (int x = (cx - CALMA_R1) >> 4; x <= (cx + CALMA_R1) >> 4; x++)
            for (int z = (cz - CALMA_R1) >> 4; z <= (cz + CALMA_R1) >> 4; z++) {
                lvl.getChunk(x, z);
                n += calmar(lvl, x, z, o);
            }
        anden(lvl, o);
        d.muelle = true;
        d.cambio();
        porHacer = null;
        DarkCraftMod.LOGGER.info("[DarkCraft] El muelle de Valdrenn, hecho de una vez en {}, {} ({} bloques de nube cambiados)",
                MuelleValdrenn.SEGURO_X + o[0], MuelleValdrenn.SEGURO_Z + o[1], n);
        return true;
    }

    /** Deja el mar de nubes de ese chunk como dice {@link #cimaValdrenn}; las columnas con algo que no es nube no se tocan. */
    private static int calmar(ServerLevel lvl, int chx, int chz, int[] o) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        BlockState[] nube = NubesGenerador.nubes();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int cx = CALMA_X + o[0], cz = CALMA_Z + o[1], n = 0;
        for (int x = chx << 4; x < (chx << 4) + 16; x++)
            for (int z = chz << 4; z < (chz << 4) + 16; z++) {
                double ex = x - cx, ez = z - cz;
                if (ex * ex + ez * ez >= CALMA_R1 * CALMA_R1) continue;
                int nat = NubesGenerador.cima(x, z), va = cimaValdrenn(x, z, o[0], o[1]);
                int abajo = Math.min(NubesGenerador.fondo(x, z), va - 12), arriba = Math.max(nat, va) + 6;
                boolean libre = true;
                for (int y = abajo; y <= arriba && libre; y++) {
                    BlockState s = lvl.getBlockState(p.set(x, y, z));
                    libre = s.isAir() || s.is(DarkCraftMod.NUBE.get());
                }
                if (!libre) continue;
                for (int y = abajo; y <= arriba; y++) {
                    BlockState quiero = y > va ? aire : nube[NubeBlock.tono(va - y)];
                    if (lvl.getBlockState(p.set(x, y, z)) == quiero) continue;
                    lvl.setBlock(p, quiero, CALLADO);
                    n++;
                }
            }
        return n;
    }

    /** Pone el anden, une sus pretiles y enciende su sitio en la lista de santuarios. */
    private static void anden(ServerLevel lvl, int[] o) {
        Map<BlockPos, BlockState> m = Garza.dibujar(new MuelleValdrenn(lvl.getSeed()));
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (Map.Entry<BlockPos, BlockState> e : m.entrySet()) {
            p.set(e.getKey().getX() + o[0], e.getKey().getY(), e.getKey().getZ() + o[1]);
            if (lvl.getBlockState(p) != e.getValue()) lvl.setBlock(p, e.getValue(), CALLADO);
        }
        for (Map.Entry<BlockPos, BlockState> e : m.entrySet()) {
            if (!(e.getValue().getBlock() instanceof WallBlock)) continue;
            p.set(e.getKey().getX() + o[0], e.getKey().getY(), e.getKey().getZ() + o[1]);
            BlockState s = lvl.getBlockState(p), n = Block.updateFromNeighbourShapes(s, lvl, p);
            if (n != s) lvl.setBlock(p, n, CALLADO);
        }
        Santuarios.poner(lvl, new BlockPos(MuelleValdrenn.SANTUARIO_X + o[0], MuelleValdrenn.Y + 1, MuelleValdrenn.SANTUARIO_Z + o[1]), SANTUARIO);
    }

    /** La campana del espigon de Brumavela: se pone una vez (tambien en los mundos de antes). */
    static void campanaBrumavela(ServerLevel lvl) {
        BlockPos c = new BlockPos(CAMPANA_BV_X, Barco.CUBIERTA + 2, CAMPANA_BV_Z);
        if (!lvl.hasChunkAt(c) || lvl.getBlockState(c).is(Blocks.BELL)) return;
        BlockState suelo = lvl.getBlockState(c.below(2));
        if (suelo.isAir() || suelo.getBlock() instanceof NubeBlock) lvl.setBlock(c.below(2), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
        lvl.setBlock(c.below(), Blocks.DARK_OAK_LOG.defaultBlockState(), 3);
        lvl.setBlock(c, MuelleValdrenn.campana(), 3);
        DarkCraftMod.LOGGER.info("[DarkCraft] La campana del espigon de Brumavela, puesta en {}", c.toShortString());
    }
}
