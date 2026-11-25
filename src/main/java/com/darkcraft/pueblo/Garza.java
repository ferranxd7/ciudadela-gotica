package com.darkcraft.pueblo;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import com.darkcraft.pueblo.gen.Barco;
import com.darkcraft.pueblo.gen.PuebloPlan;
import com.darkcraft.world.NubeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntBinaryOperator;

/**
 * La Garza Gris como cosa que se mueve (parte 3d). El barco, bloque a bloque y sin mundo ({@link #plano}: lo que dibuja
 * {@link Barco} en un lienzo que solo apunta, con sus vallas y cristales ya unidos), para ponerlo y quitarlo de un
 * amarre —Brumavela o el muelle de Valdrenn: el mismo barco, desplazado, con la proa al sur— y para dibujarlo volando en
 * las cinematicas del viaje (la entidad que solo existe en el cliente usa este mismo plano).
 * <p>
 * Todo se pone y se quita <b>sin avisar a los vecinos</b> (que no salten los faroles, las escalas ni el plato de la mesa
 * al quedarse un momento sin apoyo, soltando su objeto) y lo que guardan los barriles viaja con el barco
 * ({@link #carga} / {@link #descargar}). Al quitarlo, donde estaba el casco vuelve la nube.
 */
public final class Garza {
    private Garza() {}

    /** Una celda del barco (en las coordenadas de Brumavela) y lo que hay en ella (tambien el aire que deja por dentro). */
    public record Celda(int x, int y, int z, BlockState estado) {
        public BlockPos pos(int dx, int dz) {
            return new BlockPos(x + dx, y, z + dz);
        }
    }

    /** Poner un bloque sin que sus vecinos se enteren: llega al cliente y nada mas. */
    private static final int CALLADO = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    private static List<Celda> roto, entero;
    private static Map<BlockPos, BlockState> mapaRoto, mapaEntero;

    /** El barco (roto, como se encuentra, o entero) celda a celda. */
    public static synchronized List<Celda> plano(boolean completo) {
        if (roto == null) {
            long semilla = PuebloPlan.get().barco.semilla();
            mapaRoto = dibujar(new Barco(semilla, false));
            mapaEntero = dibujar(new Barco(semilla, true));
            roto = lista(mapaRoto);
            entero = lista(mapaEntero);
        }
        return completo ? entero : roto;
    }

    private static synchronized Map<BlockPos, BlockState> mapa(boolean completo) {
        plano(completo);
        return completo ? mapaEntero : mapaRoto;
    }

    private static List<Celda> lista(Map<BlockPos, BlockState> m) {
        List<Celda> l = new ArrayList<>(m.size());
        for (Map.Entry<BlockPos, BlockState> e : m.entrySet()) l.add(new Celda(e.getKey().getX(), e.getKey().getY(), e.getKey().getZ(), e.getValue()));
        return List.copyOf(l);
    }

    /**
     * Lo que dibuja una pieza, celda a celda, sin ponerla en ningun mundo (con sus vallas y cristales ya unidos). Lo usan
     * el barco y el anden del muelle de Valdrenn.
     */
    public static Map<BlockPos, BlockState> dibujar(Element pieza) {
        Map<BlockPos, BlockState> m = new LinkedHashMap<>(8192);
        // las que la pieza pide unir a sus vecinos (las demas vallas se quedan como postes sueltos, igual que al generar)
        java.util.Set<BlockPos> unidas = new java.util.LinkedHashSet<>();
        pieza.build(Canvas.apunte(new Canvas.Apunte() {
            @Override
            public void pon(int x, int y, int z, BlockState s) {
                BlockPos p = new BlockPos(x, y, z);
                m.put(p, s);
                unidas.remove(p);
            }

            @Override
            public void une(int x, int y, int z, BlockState s) {
                BlockPos p = new BlockPos(x, y, z);
                m.put(p, s);
                unidas.add(p);
            }

            @Override
            public BlockState mira(int x, int y, int z) {
                BlockState s = m.get(new BlockPos(x, y, z));
                return s == null ? Blocks.AIR.defaultBlockState() : s;
            }
        }));
        unir(m, unidas);
        return m;
    }

    /** Lo que ve una valla a su alrededor: el plano, y aire donde no hay barco. */
    private record Vista(Map<BlockPos, BlockState> m) implements BlockGetter {
        @Override
        public BlockEntity getBlockEntity(BlockPos p) {
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos p) {
            BlockState s = m.get(p);
            return s == null ? Blocks.AIR.defaultBlockState() : s;
        }

        @Override
        public FluidState getFluidState(BlockPos p) {
            return getBlockState(p).getFluidState();
        }

        @Override
        public int getHeight() {
            return 512;
        }

        @Override
        public int getMinBuildHeight() {
            return -64;
        }
    }

    /**
     * Une las vallas y los cristales que lo piden a sus vecinos, como lo hace el juego al acabar de generar el chunk (una
     * valla se une a otra valla, a una cancela de frente y a lo que le da una cara entera; un cristal, a otro cristal, a un
     * muro bajo y a una cara entera): asi el plano es igual, estado a estado, que el barco que genero el mundo.
     */
    private static void unir(Map<BlockPos, BlockState> m, java.util.Set<BlockPos> unidas) {
        Vista v = new Vista(m);
        Map<BlockPos, BlockState> cambios = new LinkedHashMap<>();
        for (BlockPos donde : unidas) {
            Map.Entry<BlockPos, BlockState> e = Map.entry(donde, m.get(donde));
            BlockState s = e.getValue();
            if (!(s.getBlock() instanceof CrossCollisionBlock)) continue;
            boolean valla = s.getBlock() instanceof FenceBlock;
            for (Direction d : Direction.Plane.HORIZONTAL) {
                BlockPos np = e.getKey().relative(d);
                BlockState n = v.getBlockState(np);
                boolean cara = n.isFaceSturdy(v, np, d.getOpposite()) && !Block.isExceptionForConnection(n);
                boolean une = valla
                        ? cara || n.getBlock() instanceof FenceBlock || (n.getBlock() instanceof FenceGateBlock && FenceGateBlock.connectsToDirection(n, d.getOpposite()))
                        : cara || n.getBlock() instanceof IronBarsBlock || n.getBlock() instanceof WallBlock;
                s = s.setValue(net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION.get(d), une);
            }
            if (s != e.getValue()) cambios.put(e.getKey(), s);
        }
        m.putAll(cambios);
    }

    // ------------------------------------------------------------------ ponerlo y quitarlo

    /** Lo que queda en una celda cuando el barco no esta: nube hasta lo alto del mar de nubes de ese sitio, y aire. */
    private static BlockState sinBarco(int y, int cima) {
        if (y > cima) return Blocks.AIR.defaultBlockState();
        return DarkCraftMod.NUBE.get().defaultBlockState().setValue(NubeBlock.TONO, NubeBlock.tono(cima - y));
    }

    /**
     * Deja el amarre (desplazado {@code dx}, {@code dz} del de Brumavela) con el barco roto, entero o sin barco
     * ({@code que}: 0, 1 o -1), partiendo de como estuviera. {@code cima} dice lo alto del mar de nubes de cada columna
     * (en las coordenadas del mundo). Devuelve cuantos bloques ha cambiado.
     */
    public static int dejar(ServerLevel lvl, int que, int dx, int dz, IntBinaryOperator cima) {
        Map<BlockPos, BlockState> quiero = que < 0 ? Map.of() : mapa(que == 1);
        // todas las celdas que toca el barco, roto o entero
        Map<BlockPos, BlockState> entero = mapa(true);
        Map<BlockPos, BlockState> todas = new LinkedHashMap<>(mapa(false));
        for (Map.Entry<BlockPos, BlockState> e : entero.entrySet()) todas.putIfAbsent(e.getKey(), e.getValue());
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int n = 0;
        for (BlockPos c : todas.keySet()) {
            p.set(c.getX() + dx, c.getY(), c.getZ() + dz);
            BlockState va = quiero.get(c);
            if (va == null) va = sinBarco(c.getY(), cima.applyAsInt(p.getX(), p.getZ()));
            BlockState hay = lvl.getBlockState(p);
            if (hay == va) continue;
            // al llevarse el barco, lo que guardaban sus barriles no se tira: se ha recogido antes con carga() y viaja con
            // el. (Cualquier otro cofre que alguien haya puesto ahi suelta lo suyo, como al romperlo.)
            if (que < 0 && hay.hasBlockEntity()) {
                BlockState suyo = entero.get(c);
                if (suyo != null && suyo.hasBlockEntity()) Clearable.tryClear(lvl.getBlockEntity(p));
            }
            lvl.setBlock(p, va, CALLADO);
            n++;
        }
        return n;
    }

    /**
     * Remienda el barco entero que hay en ese amarre: vuelve a poner los bloques suyos que falten (o que sean otra cosa).
     * No toca lo que solo ha cambiado de estado (una puerta abierta, un barril que alguien esta mirando) ni lo que haya en
     * sus huecos. Devuelve cuantos ha puesto.
     */
    public static int remendar(ServerLevel lvl, int dx, int dz) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int n = 0;
        for (Celda c : plano(true)) {
            if (c.estado().isAir()) continue;
            p.set(c.x() + dx, c.y(), c.z() + dz);
            if (lvl.getBlockState(p).getBlock() == c.estado().getBlock()) continue;
            lvl.setBlock(p, c.estado(), CALLADO);
            n++;
        }
        return n;
    }

    /** Lo que guardan los barriles del barco que esta en ese amarre (para llevarlo al otro): se recoge antes de quitarlo. */
    public static ListTag carga(ServerLevel lvl, int dx, int dz) {
        ListTag l = new ListTag();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (Celda c : plano(true)) {
            if (!c.estado().hasBlockEntity()) continue;
            BlockEntity be = lvl.getBlockEntity(p.set(c.x() + dx, c.y(), c.z() + dz));
            if (be == null) continue;
            CompoundTag t = be.saveWithoutMetadata();
            if (t.isEmpty()) continue;
            CompoundTag o = new CompoundTag();
            o.putInt("x", c.x());
            o.putInt("y", c.y());
            o.putInt("z", c.z());
            o.put("datos", t);
            l.add(o);
        }
        return l;
    }

    /** Vuelve a meter en los barriles del barco recien puesto lo que llevaban. */
    public static void descargar(ServerLevel lvl, ListTag carga, int dx, int dz) {
        for (int i = 0; i < carga.size(); i++) {
            CompoundTag o = carga.getCompound(i);
            BlockEntity be = lvl.getBlockEntity(new BlockPos(o.getInt("x") + dx, o.getInt("y"), o.getInt("z") + dz));
            if (be == null) continue;
            be.load(o.getCompound("datos"));
            be.setChanged();
        }
    }

    /** Cuantas celdas del mundo no estan como el plano (roto o entero) en ese amarre: para las pruebas. */
    public static int distintas(ServerLevel lvl, boolean completo, int dx, int dz) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int n = 0;
        for (Celda c : plano(completo)) if (lvl.getBlockState(p.set(c.x() + dx, c.y(), c.z() + dz)) != c.estado()) n++;
        return n;
    }

    /** Si todos los chunks que toca el barco en ese amarre estan cargados. */
    public static boolean cargado(ServerLevel lvl, int dx, int dz) {
        for (int cx = (Barco.BX - 7 + dx) >> 4; cx <= (Barco.BX + 7 + dx) >> 4; cx++)
            for (int cz = (Barco.POPA - 2 + dz) >> 4; cz <= (Barco.PROA + 9 + dz) >> 4; cz++)
                if (lvl.getChunkSource().getChunkNow(cx, cz) == null) return false;
        return true;
    }
}
