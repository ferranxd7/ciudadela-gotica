import com.darkcraft.gen.Canvas;
import com.darkcraft.gen.Element;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Herramienta de diseno (no va en el mod): levanta un plano del mod SIN abrir Minecraft (solo arranca sus registros) en
 * un mundo de mentira y lo vuelca a un fichero para dibujarlo con vista.py. Asi se ve una isla o un edificio en segundos
 * en vez de esperar a la prueba de la CI.
 *
 *     tools/vista3d/vista.sh pueblo /tmp/pueblo        (compila el mod y esto, vuelca y dibuja)
 *
 * Planos: fortaleza (la Fortaleza de Hueso), pueblo (la isla de Brumavela), barco y barco_roto (La Garza Gris sola).
 */
public class Volcado {
    static final Map<Long, BlockState> MUNDO = new HashMap<>(1 << 20);
    static final List<String> ENTIDADES = new ArrayList<>();
    static int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE,
            maxZ = Integer.MIN_VALUE;

    static WorldGenLevel nivel() {
        BlockState aire = Blocks.AIR.defaultBlockState();
        RandomSource azar = RandomSource.create(7);
        InvocationHandler h = (proxy, m, a) -> {
            switch (m.getName()) {
                case "setBlock" -> {
                    BlockPos p = (BlockPos) a[0];
                    BlockState s = (BlockState) a[1];
                    if (s.isAir()) MUNDO.remove(p.asLong());
                    else {
                        MUNDO.put(p.asLong(), s);
                        minX = Math.min(minX, p.getX());
                        maxX = Math.max(maxX, p.getX());
                        minY = Math.min(minY, p.getY());
                        maxY = Math.max(maxY, p.getY());
                        minZ = Math.min(minZ, p.getZ());
                        maxZ = Math.max(maxZ, p.getZ());
                    }
                    return true;
                }
                case "getBlockState" -> {
                    return MUNDO.getOrDefault(((BlockPos) a[0]).asLong(), aire);
                }
                case "getFluidState" -> {
                    return MUNDO.getOrDefault(((BlockPos) a[0]).asLong(), aire).getFluidState();
                }
                case "getMinBuildHeight" -> {
                    return -64;
                }
                case "getMaxBuildHeight" -> {
                    return 704;
                }
                case "getHeight" -> {
                    if (a == null || a.length == 0) return 768;
                }
                case "getBlockEntity" -> {
                    return a != null && a.length == 2 ? java.util.Optional.empty() : null;
                }
                case "scheduleTick" -> {
                    return null;
                }
                case "addFreshEntity" -> {
                    net.minecraft.world.entity.Entity e = (net.minecraft.world.entity.Entity) a[0];
                    ENTIDADES.add(e.getClass().getSimpleName() + " " + e.getX() + " " + e.getY() + " " + e.getZ());
                    return true;
                }
                case "getRandom" -> {
                    return azar;
                }
                case "getSeed" -> {
                    return 0L;
                }
                case "isClientSide" -> {
                    return false;
                }
                case "hashCode" -> {
                    return 1;
                }
                case "equals" -> {
                    return proxy == a[0];
                }
                case "toString" -> {
                    return "mundo de mentira";
                }
                default -> {
                }
            }
            if (m.isDefault()) return InvocationHandler.invokeDefault(proxy, m, a);
            throw new UnsupportedOperationException("el mundo de mentira no sabe: " + m);
        };
        return (WorldGenLevel) Proxy.newProxyInstance(Volcado.class.getClassLoader(), new Class<?>[]{WorldGenLevel.class}, h);
    }

    /** Construye una lista de elementos (en su orden de capas) chunk a chunk, como lo hace el mod. */
    static void construir(WorldGenLevel lvl, List<? extends Element> elementos) {
        List<Element> orden = new ArrayList<>(elementos);
        orden.sort(java.util.Comparator.comparingInt(e -> e.layer));
        int cx0 = Integer.MAX_VALUE, cx1 = Integer.MIN_VALUE, cz0 = Integer.MAX_VALUE, cz1 = Integer.MIN_VALUE;
        for (Element e : orden) {
            cx0 = Math.min(cx0, e.x0 >> 4);
            cx1 = Math.max(cx1, e.x1 >> 4);
            cz0 = Math.min(cz0, e.z0 >> 4);
            cz1 = Math.max(cz1, e.z1 >> 4);
        }
        for (int cx = cx0; cx <= cx1; cx++)
            for (int cz = cz0; cz <= cz1; cz++) {
                Canvas c = new Canvas(lvl, cx, cz, RandomSource.create(cx * 31L + cz));
                for (Element e : orden) if (e.touchesChunk(cx, cz)) e.build(c);
            }
    }

    public static void main(String[] a) throws Exception {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        String que = a[0], salida = a[1];
        WorldGenLevel lvl = nivel();
        long t0 = System.currentTimeMillis();
        Map<String, int[]> marcas = new LinkedHashMap<>();
        switch (que) {
            case "fortaleza" -> {
                com.darkcraft.gen.FortalezaPlan p = new com.darkcraft.gen.FortalezaPlan(1234L, 0, 0, 320);
                for (long k : p.chunks()) p.construir(lvl, (int) (k >> 32), (int) k, RandomSource.create(k));
            }
            case "pueblo" -> {
                com.darkcraft.pueblo.gen.PuebloPlan p = com.darkcraft.pueblo.gen.PuebloPlan.get();
                for (long k : p.chunks()) p.construir(lvl, (int) (k >> 32), (int) k, RandomSource.create(k));
                // el recorrido a pie: con la cripta cerrada y abierta
                BlockState aire = Blocks.AIR.defaultBlockState();
                com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
                for (boolean abierta : new boolean[]{false, true}) {
                    var pasos = com.darkcraft.pueblo.gen.Recorrido.comprobar(mundo, abierta);
                    System.out.println("recorrido (cripta " + (abierta ? "abierta" : "cerrada") + "): " + com.darkcraft.pueblo.gen.Recorrido.resumen(pasos));
                    // y los puestos de los enemigos (parte 3c): que se pisan, que se llega y que no agobian donde se despierta
                    if (abierta) System.out.println("puestos de los enemigos: " + com.darkcraft.pueblo.PuebloEnemigos.comprobar(mundo));
                }
                marcas.putAll(p.sitios());
            }
            case "valdrenn" -> {
                // un Reino de Valdrenn entero con la semilla que se pida (VALDRENN_SEMILLA, por defecto 1234) en (0, 0)
                long semilla = Long.parseLong(System.getProperty("semilla", "1234"));
                com.darkcraft.gen.CastlePlan p = com.darkcraft.gen.CastlePlan.create(semilla, 0, 0, (x, z) -> com.darkcraft.world.NubesGenerador.SUELO);
                java.util.Set<Long> hechos = new java.util.HashSet<>();
                for (com.darkcraft.gen.Element e : p.elements())
                    for (int cx = e.x0 >> 4; cx <= e.x1 >> 4; cx++)
                        for (int cz = e.z0 >> 4; cz <= e.z1 >> 4; cz++)
                            if (p.touches(cx, cz) && hechos.add(((long) cx << 32) ^ (cz & 0xFFFFFFFFL))) p.buildChunk(lvl, cx, cz, RandomSource.create(cx * 31L + cz));
                System.out.println("valdrenn: " + p.elementCount() + " piezas, " + hechos.size() + " chunks");
                // los puestos de la guardia (parte 4): que se pisan y donde queda cada uno
                BlockState aire = Blocks.AIR.defaultBlockState();
                com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
                System.out.println("puestos de Valdrenn: " + com.darkcraft.valdrenn.ValdrennEnemigos.comprobar(mundo, p));
                int k = 0;
                for (var q : com.darkcraft.valdrenn.ValdrennEnemigos.todos(p)) {
                    String letra = switch (q.tipo()) {
                        case "arquero" -> "A";
                        case "perro" -> "P";
                        case "espectro" -> q.variante() == 0 ? "S" : "D";
                        default -> "GAB".substring(q.variante(), q.variante() + 1);
                    };
                    marcas.put(letra + (k++), new int[]{q.lx(), q.y(), q.lz()});
                    if (!q.grupo().startsWith("torreon")) continue;
                    System.out.println("  dentro: " + letra + (k - 1) + " " + q.grupo() + " " + q.tipo() + "/" + q.variante() + " en " + q.lx() + "," + q.y() + "," + q.lz()
                            + " postura " + q.postura());
                }
            }
            case "barco" -> construir(lvl, List.of(new com.darkcraft.pueblo.gen.Barco(77, true)));
            case "barco_roto" -> construir(lvl, List.of(new com.darkcraft.pueblo.gen.Barco(77, false)));
            default -> throw new IllegalArgumentException("plano desconocido: " + que);
        }
        System.out.println(que + ": " + MUNDO.size() + " bloques en " + (System.currentTimeMillis() - t0) + " ms; caja " + minX + "," + minY + "," + minZ
                + " .. " + maxX + "," + maxY + "," + maxZ + "; entidades " + ENTIDADES.size());
        escribir(salida, marcas);
    }

    /** El volcado: caja, paleta de estados y un indice de 16 bits por bloque (x, z, y), comprimido. */
    static void escribir(String salida, Map<String, int[]> marcas) throws Exception {
        int sx = maxX - minX + 1, sy = maxY - minY + 1, sz = maxZ - minZ + 1;
        Map<BlockState, Integer> paleta = new LinkedHashMap<>();
        paleta.put(Blocks.AIR.defaultBlockState(), 0);
        short[] datos = new short[sx * sy * sz];
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (Map.Entry<Long, BlockState> e : MUNDO.entrySet()) {
            m.set(e.getKey());
            Integer i = paleta.get(e.getValue());
            if (i == null) {
                i = paleta.size();
                paleta.put(e.getValue(), i);
            }
            datos[((m.getX() - minX) * sz + (m.getZ() - minZ)) * sy + (m.getY() - minY)] = (short) (int) i;
        }
        try (DataOutputStream o = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(salida), 1 << 16))) {
            o.writeInt(0x56495354);
            for (int v : new int[]{minX, minY, minZ, sx, sy, sz, paleta.size()}) o.writeInt(v);
            for (BlockState s : paleta.keySet()) {
                String n = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString();
                StringBuilder b = new StringBuilder(n);
                if (!s.getValues().isEmpty()) {
                    b.append('[');
                    boolean primero = true;
                    for (var p : s.getValues().entrySet()) {
                        if (!primero) b.append(',');
                        primero = false;
                        b.append(p.getKey().getName()).append('=').append(p.getValue().toString().toLowerCase(java.util.Locale.ROOT));
                    }
                    b.append(']');
                }
                byte[] bs = b.toString().getBytes(StandardCharsets.UTF_8);
                o.writeShort(bs.length);
                o.write(bs);
            }
            byte[] crudo = new byte[datos.length * 2];
            for (int i = 0; i < datos.length; i++) {
                crudo[2 * i] = (byte) (datos[i] >> 8);
                crudo[2 * i + 1] = (byte) datos[i];
            }
            o.write(crudo);
            o.writeInt(marcas.size());
            for (Map.Entry<String, int[]> e : marcas.entrySet()) {
                byte[] bs = e.getKey().getBytes(StandardCharsets.UTF_8);
                o.writeShort(bs.length);
                o.write(bs);
                for (int k = 0; k < 3; k++) o.writeInt(e.getValue()[k]);
            }
            o.writeInt(ENTIDADES.size());
            for (String s : ENTIDADES) {
                byte[] bs = s.getBytes(StandardCharsets.UTF_8);
                o.writeShort(bs.length);
                o.write(bs);
            }
        }
        System.out.println("volcado en " + salida + " (" + paleta.size() + " estados)");
    }
}
