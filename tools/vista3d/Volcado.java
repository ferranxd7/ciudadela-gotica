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
            case "torre" -> {
                // la fortaleza nueva de la parte 5a (isla, murallas, cupula y torre) en (0, 0), patio a Y 320
                long semilla = Long.parseLong(System.getProperty("semilla", "1234"));
                com.darkcraft.gen.Island isla = new com.darkcraft.gen.Island(semilla, 0, -3, 319, 80, 0.05, 6, 3, 52);
                isla.surface((x, z, top) -> net.minecraft.world.level.block.Blocks.GRAVEL.defaultBlockState())
                        .subsurface((x, y, z, d) -> net.minecraft.world.level.block.Blocks.TUFF.defaultBlockState()).plants((x, z) -> false);
                com.darkcraft.gen.TorreCupula t = new com.darkcraft.gen.TorreCupula(semilla, 0, 0, 320);
                for (int cx = -7; cx <= 6; cx++)
                    for (int cz = -7; cz <= 6; cz++) {
                        com.darkcraft.gen.Canvas cv = new com.darkcraft.gen.Canvas(lvl, cx, cz, RandomSource.create(cx * 31L + cz));
                        try {
                            if (isla.touchesChunk(cx, cz)) isla.build(cv);
                            if (t.touchesChunk(cx, cz)) t.build(cv);
                        } catch (Throwable e) {
                            System.out.println("error en el chunk " + cx + "," + cz + ": " + e);
                            e.printStackTrace();
                        }
                    }
                // -Dabierto=true: con el portal abierto (gemas encendidas, cadenas rotas, cascotes)
                if (Boolean.getBoolean("abierto"))
                    for (var c : t.alAbrir()) lvl.setBlock(new BlockPos(c.x(), c.y(), c.z()), c.despues(), 2);
            }
            case "montana" -> {
                // la fortaleza entera con su montana (parte 5b): el plano de verdad en (0, 0), patio a Y 320
                long semilla = Long.parseLong(System.getProperty("semilla", "1234"));
                com.darkcraft.gen.FortalezaPlan fp = new com.darkcraft.gen.FortalezaPlan(semilla, 0, 0, 320);
                boolean todo = Boolean.parseBoolean(System.getProperty("todo", "false"));
                com.darkcraft.gen.Montana m = fp.montana;
                for (long k : fp.chunks()) {
                    int cx = (int) (k >> 32), cz = (int) k;
                    // sin "todo": solo la montana y lo que haya entre ella y la isla
                    if (!todo && ((cz << 4) + 15 < fp.bordeZ - 20 || Math.abs(cx << 4) > 140)) continue;
                    try {
                        fp.construir(lvl, cx, cz, RandomSource.create(k));
                    } catch (Throwable e) {
                        System.out.println("error en el chunk " + cx + "," + cz + ": " + e);
                        e.printStackTrace();
                    }
                }
                // el puente ya tendido (como si se hubiera usado la llave)
                com.darkcraft.fortaleza.Puente.volcar(fp, (x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2));
                double[] pie = m.pie();
                int[] arcoM = m.arco();
                System.out.println("montana: cima " + m.mx + "," + m.mz + " a " + m.cima + "; arco " + java.util.Arrays.toString(arcoM)
                        + "; pie " + String.format(java.util.Locale.ROOT, "%.1f,%.1f a %.1f", pie[0], pie[1], pie[2]) + "; sendero "
                        + m.sendero().size() + " puntos, " + m.recodos().size() + " recodos; borde de la isla z " + fp.bordeZ);
                BlockState aire = Blocks.AIR.defaultBlockState();
                com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
                StringBuilder col = new StringBuilder("  columna del pie:");
                for (int y = 246; y <= 254; y++)
                    col.append(' ').append(y).append('=').append(mundo.en((int) Math.floor(pie[0]), y, (int) Math.floor(pie[1])).getBlock().getDescriptionId().replace("block.minecraft.", ""));
                System.out.println(col);
                var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, (int) Math.floor(pie[0]), (int) Math.round(pie[2]) + 1, (int) Math.floor(pie[1]),
                        -160, fp.bordeZ - 200, 160, m.mz + 200, 400000);
                System.out.println("a pie desde el pie del sendero: " + z0.celdas() + " celdas; llega al arco " + z0.alcanza(arcoM[0], arcoM[1], arcoM[2] + 2, 3)
                        + ", a la isla por el puente " + z0.alcanza(0, 320, fp.bordeZ - 3, 3) + ", a la puerta "
                        + z0.alcanza(0, 320, com.darkcraft.gen.TorreCupula.PFUERA + 4, 3));
                // parte 5c: los puestos de los enemigos, las trampas, la cripta y la sala secreta (con todo el plano)
                if (todo) {
                    com.darkcraft.gen.TorreCupula t = fp.f;
                    System.out.println("enemigos: " + com.darkcraft.fortaleza.FortalezaEnemigos.comprobar(mundo, fp));
                    int nm = 0;
                    for (var q : com.darkcraft.fortaleza.FortalezaEnemigos.puestos(fp))
                        marcas.put((q.tipo().equals("elite") ? "C" : q.tipo().substring(0, 1).toUpperCase()) + (nm++), new int[]{q.x(), q.y(), q.z()});
                    var jardin = com.darkcraft.pueblo.gen.Andar.desde(mundo, fp.cx, fp.Y + 1, fp.cz + 40, -90, -90, 90, 90, 3_000_000);
                    int ys = fp.Y + com.darkcraft.gen.TorreCupula.SOTANO;
                    StringBuilder tr = new StringBuilder("trampas: suelo que cede");
                    for (int[] q : t.sueloQueCede()) tr.append(' ').append(mundo.en(q[0], q[1], q[2]).getBlock().getDescriptionId().replace("block.minecraft.", "")).append(jardin.pisa(q[0], q[1], q[2]) ? "+" : "-");
                    tr.append("; pinchos");
                    for (int[] q : t.pinchos()) tr.append(' ').append(jardin.pisa(q[0], q[1] - 1, q[2]) ? "+" : "-");
                    tr.append("; flechas: disparo");
                    for (int[] q : t.flechasDisparo()) tr.append(' ').append(mundo.en(q[0], q[1], q[2]).isAir() ? "aire" : "?");
                    tr.append(", aspilleras");
                    for (int[] q : t.flechasAspilleras()) tr.append(' ').append(mundo.en(q[0], q[1], q[2]).isAir() ? "aire" : mundo.en(q[0], q[1], q[2]).getBlock().getDescriptionId().replace("block.minecraft.", ""));
                    System.out.println(tr);
                    System.out.println("a pie desde el jardin: " + jardin.celdas() + " celdas; sotano " + jardin.alcanza(fp.cx - 7, ys, fp.cz, 2)
                            + ", ante el muro falso " + jardin.pisa(fp.cx - 7, ys - 1, fp.cz) + ", cofre secreto (con el muro) " + jardin.alcanza(t.cofreSecreto()[0], t.cofreSecreto()[1], t.cofreSecreto()[2], 3)
                            + ", atril de la cripta (cerrada) " + jardin.alcanza(t.atrilEscondido()[0], t.atrilEscondido()[1], t.atrilEscondido()[2], 3)
                            + ", terraza " + jardin.alcanza(fp.cx, fp.Y + com.darkcraft.gen.TorreCupula.TERRAZA, fp.cz + 6, 3)
                            + ", ante el pedestal " + jardin.alcanza(t.altar()[0], t.altar()[1], t.altar()[2] + 2, 1)
                            + ", ante el portal " + jardin.alcanza(fp.cx, fp.Y + com.darkcraft.gen.TorreCupula.TERRAZA, fp.cz + com.darkcraft.gen.TorreCupula.AZ + 2, 1)
                            + ", atril de la terraza " + jardin.alcanza(t.atriles()[5][0], t.atriles()[5][1], t.atriles()[5][2], 3));
                    // con el muro falso deshecho y la trampilla abierta
                    for (int[] q : t.muroFalso()) MUNDO.put(BlockPos.asLong(q[0], q[1], q[2]), aire);
                    int[] tp = t.trampilla();
                    BlockState trampilla = mundo.en(tp[0], tp[1], tp[2]);
                    // (abierta, una trampilla sobre una escalera de mano que mira igual hace de escalera: asi la ve Andar)
                    MUNDO.put(BlockPos.asLong(tp[0], tp[1], tp[2]), Blocks.LADDER.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.LadderBlock.FACING, net.minecraft.core.Direction.NORTH));
                    var abierto = com.darkcraft.pueblo.gen.Andar.desde(mundo, fp.cx, fp.Y + 1, fp.cz + 40, -90, -90, 90, 90, 3_000_000);
                    System.out.println("con el muro deshecho y la trampilla (" + trampilla.getBlock().getDescriptionId().replace("block.minecraft.", "")
                            + ") abierta: " + abierto.celdas() + " celdas; cofre secreto " + abierto.alcanza(t.cofreSecreto()[0], t.cofreSecreto()[1], t.cofreSecreto()[2], 3)
                            + ", atril de la cripta " + abierto.alcanza(t.atrilEscondido()[0], t.atrilEscondido()[1], t.atrilEscondido()[2], 3)
                            + " (" + mundo.en(t.atrilEscondido()[0], t.atrilEscondido()[1], t.atrilEscondido()[2]).getBlock().getDescriptionId().replace("block.minecraft.", "") + ")");
                    MUNDO.put(BlockPos.asLong(tp[0], tp[1], tp[2]), trampilla);
                }
                // donde se corta: el punto del sendero mas bajo al que ya no se llega
                List<double[]> sen = m.sendero();
                for (int i = sen.size() - 1; i >= 0; i -= 4) {
                    double[] q = sen.get(i);
                    if (!z0.pisa((int) Math.floor(q[0]), (int) Math.floor(q[2]), (int) Math.floor(q[1]))) {
                        System.out.println("  no se pisa el sendero en " + String.format(java.util.Locale.ROOT, "%.1f,%.1f a %.1f (punto %d)", q[0], q[1], q[2], i));
                        break;
                    }
                }
            }
            case "camino" -> {
                // las paradas del camino de nubes (parte 5d) en un camino de mentira (tramos rectos con giros, como el de
                // verdad, de 2700 bloques): el camino de nube firme va de lana blanca (fuera del juego no hay bloques del mod)
                long semilla = Long.parseLong(System.getProperty("semilla", "1234"));
                double[] px = {0, 700, 1300, 1900, 2350}, pz = {0, 260, 300, 120, 640};
                var paradas = com.darkcraft.camino.Paradas.colocar(semilla, 0, 0, px, pz);
                BlockState lana = Blocks.WHITE_WOOL.defaultBlockState(), aire = Blocks.AIR.defaultBlockState();
                int solo = Integer.parseInt(System.getProperty("parada", "-1"));
                int dx0 = Integer.parseInt(System.getProperty("dx", "0"));
                for (var sp : paradas) {
                    if (solo >= 0 && sp.k() != solo) continue;
                    var pl = sp.plan();
                    // el camino de nube alrededor (a 1,6 de la linea, 40 bloques a cada lado de la parada)
                    for (int x = pl.cx - 60; x <= pl.cx + 60; x++)
                        for (int z = pl.cz - 60; z <= pl.cz + 60; z++)
                            if (pl.alCamino(x, z) <= 1.6) lvl.setBlock(new BlockPos(x, 250, z), lana, 2);
                    for (long k : pl.chunks()) pl.construir(lvl, (int) (k >> 32), (int) k, RandomSource.create(k));
                    com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
                    System.out.println("parada " + com.darkcraft.camino.Paradas.comprobar(mundo, sp) + " en " + pl.cx + "," + pl.cz
                            + " (a " + Math.round(sp.s()) + " del principio)");
                    List<com.darkcraft.enemigos.Puestos.Puesto> ps = new ArrayList<>();
                    com.darkcraft.camino.Paradas.puestos(ps, sp);
                    int n = 0;
                    for (var q : ps) marcas.put(q.tipo().substring(0, 1).toUpperCase() + sp.k() + "_" + (n++), new int[]{q.x(), q.y(), q.z()});
                    if (pl.cofre() != null) marcas.put("cofre" + sp.k(), pl.cofre());
                    if (pl.santuario() != null) marcas.put("SANTUARIO" + sp.k(), pl.santuario());
                }
                System.out.println("paradas: " + paradas.size() + " (tipos " + java.util.Arrays.toString(com.darkcraft.camino.Paradas.tipos(2700)) + ")");
                for (var sp : paradas) System.out.println("  " + sp.k() + " " + sp.nombre() + " centro " + sp.plan().cx + "," + sp.plan().cz);
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
                        case "elite" -> "C";
                        case "brandt" -> "K";
                        case "corteza" -> "V";
                        default -> "GAB".substring(q.variante(), q.variante() + 1);
                    };
                    marcas.put(letra + (k++), new int[]{q.lx(), q.y(), q.lz()});
                    if (!q.grupo().startsWith("torreon") && !q.grupo().startsWith("emb_") && !q.grupo().startsWith("torre_arbol") && !q.tipo().equals("elite") && !q.grupo().startsWith("jefe")) continue;
                    System.out.println("  dentro: " + letra + (k - 1) + " " + q.grupo() + " " + q.tipo() + "/" + q.variante() + " en " + q.lx() + "," + q.y() + "," + q.lz()
                            + " postura " + q.postura());
                }
                // los sellos de los guardianes (parte 4d): que hay en sus huecos (tiene que ser aire, o algo que se pueda tapar)
                for (String n : new String[]{"trono", "arbol"}) {
                    var l = n.equals("trono") ? com.darkcraft.valdrenn.ValdrennEnemigos.selloTrono(p) : com.darkcraft.valdrenn.ValdrennEnemigos.selloArbol(p);
                    java.util.Map<String, Integer> hay = new java.util.TreeMap<>();
                    for (BlockPos b : l) hay.merge(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(mundo.en(b.getX(), b.getY(), b.getZ()).getBlock()).getPath(), 1, Integer::sum);
                    StringBuilder donde = new StringBuilder();
                    for (int i = 0; i < l.size() && i < 40; i++) donde.append(' ').append(l.get(i).getX()).append(',').append(l.get(i).getY()).append(',').append(l.get(i).getZ());
                    System.out.println("sello del " + n + ": " + l.size() + " bloques " + hay + " |" + donde);
                }
            }
            case "morvain_ciudadela" -> {
                // la isla de la ciudadela (sin las cadenas lejos ni el terreno de debajo)
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), -124, -124, 124, 124, 330);
            }
            case "morvain_cadena" -> {
                // la cadena del este con su ancla y el crater de debajo
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), 60, -40, 900, 40, 30);
                int[] an = com.darkcraft.morvain.Vista.anclas()[0];
                marcas.put("ancla", new int[]{an[0], an[1] + 12, an[2]});
            }
            case "morvain_llegada" -> {
                int[] l = com.darkcraft.morvain.Vista.llegada();
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), l[0] - 40, l[2] - 130, l[0] + 40, l[2] + 40,
                        l[1] - 8);
                marcas.put("santuario", new int[]{l[0], l[1] + 2, l[2]});
            }
            case "morvain_santuario" -> {
                // un santuario de Morvain (-Dsitio=<id>; por defecto el primero) con lo que lo rodea
                String id = System.getProperty("sitio", "encrucijada");
                com.darkcraft.morvain.SantuariosMorvain.Sitio s = com.darkcraft.morvain.SantuariosMorvain.porId(id);
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), s.x() - 18, s.z() - 18, s.x() + 18,
                        s.z() + 18, s.y() - 10);
                marcas.put("santuario", new int[]{s.x(), s.y() + 2, s.z()});
            }
            case "morvain_velmor" -> {
                // Velmor, el pueblo marchito de las Llanuras (parte 7e): el terreno y el pueblo encima, chunk a chunk
                // (-Dcaja=x0,z0,x1,z1 para un trozo; por defecto todo, del puente a la puerta del este)
                String[] k = System.getProperty("caja", "636,3884,990,4200").split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, 58);
                com.darkcraft.morvain.Velmor v = com.darkcraft.morvain.Velmor.get();
                int hechos = 0;
                for (long c : v.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(c));
                    hechos++;
                }
                // lo que se sale de la caja, fuera (las piezas se dibujan enteras en sus chunks)
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                System.out.println("Velmor: " + v.casas.size() + " casas, " + v.elementos().size() + " piezas, " + hechos + " chunks; sitios "
                        + v.sitios().size() + "; la capilla a Y " + v.capillaY);
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
            }
            case "morvain_molino" -> {
                // el molino de la Molinera, al este de Velmor (parte 7f): la loma, la era, el molino, las aspas y el camino
                // (-Dcaja=x0,z0,x1,z1 para un trozo; por defecto la loma y lo de alrededor)
                String[] k = System.getProperty("caja", "1060,4020,1186,4126").split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, 58);
                com.darkcraft.morvain.Molino v = com.darkcraft.morvain.Molino.get();
                int hechos = 0;
                for (long c : v.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(c));
                    hechos++;
                }
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                System.out.println("El molino: " + v.elementos().size() + " piezas, " + hechos + " chunks; sitios " + v.sitios().keySet());
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
                // y se recorre a pie: del camino a la era, a la puerta del molino y (con la reja abierta) al arcon de arriba
                andarMolino(lvl, v);
            }
            case "morvain_arboleda" -> {
                // la Arboleda de los Ahorcados, en el Bosque Muerto (parte 7g): el claro con el Arbol de la Horca, los arboles
                // con sus sogas, la puerta, el patibulo y las tumbas (-Dcaja=x0,z0,x1,z1 para un trozo)
                String[] k = System.getProperty("caja", (com.darkcraft.morvain.Arboleda.GX - 48) + "," + (com.darkcraft.morvain.Arboleda.GZ - 52) + ",-4826,"
                        + (com.darkcraft.morvain.Arboleda.GZ + 46)).split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, 70);
                com.darkcraft.morvain.Arboleda v = com.darkcraft.morvain.Arboleda.get();
                int hechos = 0;
                for (long c : v.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(c));
                    hechos++;
                }
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                System.out.println("La arboleda: " + v.elementos().size() + " piezas, " + hechos + " chunks; " + v.colgados().size() + " colgados, "
                        + v.otros().size() + " mas; " + v.sogas + " sogas de colgar, " + v.lazos + " lazos, " + v.cabos + " cabos; el patibulo a Y " + v.fy
                        + "; sitios " + v.sitios().keySet());
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
                int n = 0;
                for (var c : v.colgados()) marcas.put("C" + (n++), new int[]{c.x(), c.y() + 1, c.z()});
                n = 0;
                for (var o : v.otros()) marcas.put("A" + (n++), new int[]{o.x(), o.y() + 1, o.z()});
                andarArboleda(lvl, v);
            }
            case "morvain_pilotes" -> {
                // la Aldea de los Pilotes, en el Pantano Podrido (parte 7i): la laguna, la plaza, las chozas, las pasarelas, la
                // capilla hundida y el embarcadero hasta el santuario (-Dcaja=x0,z0,x1,z1 para un trozo)
                int PX = com.darkcraft.morvain.Pilotes.CX, PZ = com.darkcraft.morvain.Pilotes.CZ;
                String[] k = System.getProperty("caja", (PX - 50) + "," + (PZ - 62) + "," + (PX + 52) + "," + (PZ + 54)).split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, 50);
                com.darkcraft.morvain.Pilotes v = com.darkcraft.morvain.Pilotes.get();
                int hechos = 0;
                for (long c : v.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(c));
                    hechos++;
                }
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                System.out.println("La aldea de los pilotes: " + v.elementos().size() + " piezas, " + hechos + " chunks; " + v.esperas().size() + " podridos; "
                        + v.tablas + " tablas, " + v.pilotes + " pilotes, " + v.faroles + " faroles; sitios " + v.sitios().keySet());
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
                int n = 0;
                for (var o : v.esperas()) marcas.put("P" + (n++), new int[]{o.x(), o.y() + 1, o.z()});
                andarPilotes(lvl, v);
            }
            case "morvain_laguna" -> {
                // la Laguna Negra, en el Pantano Podrido (parte 7j): la laguna, el embarcadero, el muelle, la barca del Barquero,
                // el camino desde el santuario del Embarcadero Podrido y el arco de la otra orilla (-Dcaja=x0,z0,x1,z1 para un trozo)
                int LX = com.darkcraft.morvain.LagunaNegra.CX, LZ = com.darkcraft.morvain.LagunaNegra.CZ;
                String[] k = System.getProperty("caja", (LX - 72) + "," + (LZ - 46) + "," + (LX + 48) + "," + (LZ + 46)).split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, 50);
                com.darkcraft.morvain.LagunaNegra v = com.darkcraft.morvain.LagunaNegra.get();
                int hechos = 0;
                for (long c : v.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(c));
                    hechos++;
                }
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                System.out.println("La laguna negra: " + v.elementos().size() + " piezas, " + hechos + " chunks; " + v.esperas().size() + " podridos; "
                        + v.tablas + " tablas, " + v.pilotes + " pilotes, " + v.postes + " postes; sitios " + v.sitios().keySet());
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
                int n = 0;
                for (var o : v.esperas()) marcas.put("P" + (n++), new int[]{o.x(), o.y() + 1, o.z()});
                andarLaguna(lvl, v);
            }
            case "morvain_monasterio" -> {
                // el Monasterio de la Helada, en las Montanas Nevadas (parte 7k): la terraza, la muralla, la iglesia y su campanario,
                // el claustro, las celdas, el refectorio, el cementerio, el huerto y el camino hasta el santuario de la Helada
                // (-Dcaja=x0,z0,x1,z1 para un trozo)
                int MX = com.darkcraft.morvain.Monasterio.CX, MZ = com.darkcraft.morvain.Monasterio.CZ;
                String[] k = System.getProperty("caja", (MX - 34) + "," + (MZ - 40) + "," + (MX + 46) + "," + (MZ + 58)).split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, 270);
                com.darkcraft.morvain.Monasterio v = com.darkcraft.morvain.Monasterio.get();
                int hechos = 0;
                for (long c : v.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(c));
                    hechos++;
                }
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                System.out.println("El monasterio: " + v.elementos().size() + " piezas, " + hechos + " chunks; " + v.esperas().size() + " helados; "
                        + v.bancos + " bancos, " + v.ventanas + " ventanas, " + v.tumbas + " tumbas, " + v.escalones + " escalones; sitios " + v.sitios().keySet());
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
                int n = 0;
                for (var o : v.esperas()) marcas.put("H" + (n++), new int[]{o.x(), o.y() + 1, o.z()});
                andarMonasterio(lvl, v);
            }
            case "morvain_ermita" -> {
                // la Ermita de la Cumbre (parte 7l): la escalera de los penitentes desde el postigo del huerto del monasterio,
                // el calvario con sus braseros y pilares, la ermita y la gran cruz de hielo (-Dcaja=x0,z0,x1,z1 para un trozo)
                int EX = com.darkcraft.morvain.Ermita.CX, EZ = com.darkcraft.morvain.Ermita.CZ;
                String[] k = System.getProperty("caja", (EX - 36) + "," + (EZ - 60) + "," + (EX + 36) + "," + (com.darkcraft.morvain.Ermita.ESC_Z0 + 14)).split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, 280);
                com.darkcraft.morvain.Monasterio mo = com.darkcraft.morvain.Monasterio.get();
                com.darkcraft.morvain.Ermita v = com.darkcraft.morvain.Ermita.get();
                int hechos = 0;
                for (long c : mo.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    mo.construir(lvl, cx, cz, RandomSource.create(c));
                }
                for (long c : v.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(c));
                    hechos++;
                }
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                System.out.println("La ermita: " + v.elementos().size() + " piezas, " + hechos + " chunks; " + v.esperas().size() + " helados; "
                        + v.escalones + " escalones (" + v.cortados() + " entre muros), " + v.pilares + " pilares, " + v.braseros().size()
                        + " braseros; sitios " + v.sitios().keySet());
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
                int n = 0;
                for (var o : v.esperas()) marcas.put("H" + (n++), new int[]{o.x(), o.y() + 1, o.z()});
                andarErmita(lvl, v);
            }
            case "morvain_capital" -> {
                // la Puerta Oeste de la Capital (parte 7m): la muralla con su puerta y sus torres, el camino del santuario con
                // los carros, la calle mayor con las casas quemadas, el cruce, la plaza del Juramento y el cuartel
                // (-Dcaja=x0,z0,x1,z1 para un trozo)
                int CX = com.darkcraft.morvain.Capital.CX, CZ = com.darkcraft.morvain.Capital.CZ;
                String[] k = System.getProperty("caja", (CX - 70) + "," + (CZ - 80) + "," + (CX + 124) + "," + (CZ + 80)).split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, 70);
                com.darkcraft.morvain.Capital v = com.darkcraft.morvain.Capital.get();
                int hechos = 0;
                for (long c : v.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(c));
                    hechos++;
                }
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                System.out.println("La capital: " + v.elementos().size() + " piezas, " + hechos + " chunks; " + v.esperas().size() + " calcinados; "
                        + v.casas.size() + " casas, " + v.torres + " torres, " + v.montones + " montones de ceniza, " + v.escombros + " escombros, "
                        + v.rescoldos + " rescoldos, " + v.carros + " carros; sitios " + v.sitios().keySet());
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
                int n = 0;
                for (var o : v.esperas()) marcas.put("C" + (n++), new int[]{o.x(), o.y() + 1, o.z()});
                andarCapital(lvl, v);
            }
            case "morvain_juramento" -> {
                // el Patio del Juramento (parte 7n): la plaza de la capital, la calle del Juramento, la puerta con su rastrillo, el
                // patio con el estrado, la piedra y los braseros, y la torre del Condestable (-Dcaja=x0,z0,x1,z1 para un trozo)
                int CX = com.darkcraft.morvain.Capital.CX, CZ = com.darkcraft.morvain.Capital.CZ;
                String[] k = System.getProperty("caja", (CX + 70) + "," + (CZ - 50) + "," + (CX + 215) + "," + (CZ + 50)).split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, 70);
                com.darkcraft.morvain.Capital c = com.darkcraft.morvain.Capital.get();
                com.darkcraft.morvain.Juramento v = com.darkcraft.morvain.Juramento.get();
                int hechos = 0;
                for (long q : c.chunks()) {
                    int cx = (int) (q >> 32), cz = (int) q;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    c.construir(lvl, cx, cz, RandomSource.create(q));
                }
                for (long q : v.chunks()) {
                    int cx = (int) (q >> 32), cz = (int) q;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(q));
                    hechos++;
                }
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                System.out.println("El patio del Juramento: " + v.elementos().size() + " piezas, " + hechos + " chunks; " + v.esperas().size()
                        + " esperan; " + v.torres + " torres de esquina, " + v.almenas + " almenas, " + v.estandartes + " estandartes, " + v.escombros
                        + " escombros; sitios " + v.sitios().keySet());
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
                int n = 0;
                for (var o : v.esperas()) marcas.put((n == 0 ? "K" : "C") + (n++), new int[]{o.x(), o.y() + 1, o.z()});
                andarJuramento(lvl, c, v);
            }
            case "morvain_academia" -> {
                // la Academia Espectral (parte 7o): el recinto con su muralla y sus torres, la puerta, el camino del santuario, el
                // patio de entrada, el claustro con la fuente, la Gran Biblioteca, el scriptorium, el aula, la torre del archivo,
                // la veta de amatista y las ruinas del aula vieja (-Dcaja=x0,z0,x1,z1 para un trozo)
                int AX = com.darkcraft.morvain.Academia.CX, AZ = com.darkcraft.morvain.Academia.CZ;
                String[] k = System.getProperty("caja", (AX - 44) + "," + (AZ - 68) + "," + (AX + 44) + "," + (AZ + 66)).split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, 225);
                com.darkcraft.morvain.Academia v = com.darkcraft.morvain.Academia.get();
                int hechos = 0;
                for (long c : v.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(c));
                    hechos++;
                }
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                System.out.println("La academia: " + v.elementos().size() + " piezas, " + hechos + " chunks; " + v.esperas().size() + " eruditos; "
                        + v.estantes + " estantes, " + v.mesas + " mesas, " + v.atriles + " atriles, " + v.ventanas + " ventanas, " + v.bancos + " bancos, "
                        + v.pupitres + " pupitres, " + v.cristales + " cristales, " + v.almenas + " almenas; sitios " + v.sitios().keySet());
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
                int n = 0;
                for (var o : v.esperas()) marcas.put("E" + (n++), new int[]{o.x(), o.y() + 1, o.z()});
                andarAcademia(lvl, v);
            }
            case "morvain_observatorio" -> {
                // el Observatorio de la Rectora (parte 7p): la terraza con su escalinata, la rotonda con la cupula y su rendija, el
                // estrado con el gran telescopio, el planetario, el vestibulo, la celda, la sala de los instrumentos, el camino y el
                // circulo de piedras (-Dcaja=x0,z0,x1,z1 para un trozo)
                int OX = com.darkcraft.morvain.Observatorio.CX, OZ = com.darkcraft.morvain.Observatorio.CZ;
                String[] k = System.getProperty("caja", (OX - 52) + "," + (OZ - 52) + "," + (OX + 52) + "," + (OZ + 62)).split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, 225);
                com.darkcraft.morvain.Observatorio v = com.darkcraft.morvain.Observatorio.get();
                int hechos = 0;
                for (long c : v.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(c));
                    hechos++;
                }
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                System.out.println("El observatorio: " + v.elementos().size() + " piezas, " + hechos + " chunks; " + v.esperas().size() + " que esperan; "
                        + v.lancetas + " lancetas, " + v.planetas + " planetas, " + v.menhires + " menhires, " + v.estantes + " estantes, " + v.faroles
                        + " faroles; sitios " + v.sitios().keySet());
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
                int n = 0;
                for (var o : v.esperas()) marcas.put((n == 0 ? "Y" : "E") + (n++), new int[]{o.x(), o.y() + 1, o.z()});
                andarObservatorio(lvl, v);
            }
            case "morvain_puerto" -> {
                // el Puerto Varado (parte 7q): el pueblo (el arco de costillas, la plaza con el ancla, la lonja, las casas, los
                // secaderos, las salinas, el varadero y la casa del farero), el muelle, la bahia seca (las barcas y la ballena), el
                // espigon y el Faro de los Huesos (-Dcaja=x0,z0,x1,z1 para un trozo)
                int PX = com.darkcraft.morvain.Varadero.CX, PZ = com.darkcraft.morvain.Varadero.CZ;
                String[] k = System.getProperty("caja", (PX - 62) + "," + (PZ - 68) + "," + (PX + 126) + "," + (PZ + 64)).split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, 40);
                com.darkcraft.morvain.Varadero v = com.darkcraft.morvain.Varadero.get();
                int hechos = 0;
                for (long c : v.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(c));
                    hechos++;
                }
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                System.out.println("El puerto: " + v.elementos().size() + " piezas, " + hechos + " chunks; " + v.esperas().size() + " salobres; "
                        + v.casas + " casas, " + v.barcas + " barcas, " + v.costillas + " costillas, " + v.norays + " norays, " + v.secaderos
                        + " secaderos, " + v.estanques + " estanques, " + v.montones + " montones de sal, " + v.faroles + " faroles, " + v.escalones
                        + " escalones; sitios " + v.sitios().keySet());
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
                int n = 0;
                for (var o : v.esperas()) marcas.put("S" + (n++), new int[]{o.x(), o.y() + 1, o.z()});
                andarPuerto(lvl, v);
            }
            case "morvain_costillar" -> {
                // el Costillar de la Ballena (parte 7r): el esqueleto (el espinazo, las costillas, la cola torcida al sur, la pelvis,
                // las aletas y la calavera con su hocico y sus quijadas), la fundidora, la barca volcada, los arpones, la sal y la
                // choza de Brann con el libro 27 (-Dcaja=x0,z0,x1,z1 para un trozo)
                String[] k = System.getProperty("caja", com.darkcraft.morvain.Costillar.X0 + "," + com.darkcraft.morvain.Costillar.Z0 + ","
                        + com.darkcraft.morvain.Costillar.X1 + "," + com.darkcraft.morvain.Costillar.Z1).split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, 50);
                com.darkcraft.morvain.Costillar v = com.darkcraft.morvain.Costillar.get();
                int hechos = 0;
                for (long c : v.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(c));
                    hechos++;
                }
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                System.out.println("El costillar: " + v.elementos().size() + " piezas, " + hechos + " chunks; " + v.esperas().size() + " que esperan; "
                        + v.costillas + " costillas (" + v.rotas + " rotas), " + v.vertebras + " vertebras, " + v.huesosCola + " huesos de la cola, "
                        + v.dedos + " dedos, " + v.barriles + " barriles, " + v.arpones + " arpones; sitios " + v.sitios().keySet());
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
                int n = 0;
                for (var o : v.esperas()) marcas.put((n == 0 ? "B" : "S") + (n++), new int[]{o.x(), o.y() + 1, o.z()});
                andarCostillar(lvl, v);
            }
            case "morvain_altar" -> {
                // el Altar Profanado (parte 7t): la basilica (la fachada con el roseton, el campanario, la nave sin techo con sus arcos,
                // el canal de sangre, el crucero con las pilas, la sacristia con el libro 29, el osario y el presbiterio con el altar y
                // la cruz rota), el atrio, el campamento y las tumbas (-Dcaja=x0,z0,x1,z1 para un trozo)
                String[] k = System.getProperty("caja", com.darkcraft.morvain.Altar.X0 + "," + com.darkcraft.morvain.Altar.Z0 + ","
                        + com.darkcraft.morvain.Altar.X1 + "," + com.darkcraft.morvain.Altar.Z1).split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, 50);
                com.darkcraft.morvain.Altar v = com.darkcraft.morvain.Altar.get();
                int hechos = 0;
                for (long c : v.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(c));
                    hechos++;
                }
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                System.out.println("El altar: " + v.elementos().size() + " piezas, " + hechos + " chunks; " + v.esperas().size() + " que esperan; "
                        + v.bancos + " bancos, " + v.velas + " velas, " + v.tumbas + " tumbas, " + v.tiendas + " tiendas, " + v.escombros
                        + " escombros, " + v.arcos + " arcos; sitios " + v.sitios().keySet());
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
                int n = 0;
                for (var o : v.esperas()) marcas.put((n == 0 ? "J" : "P") + (n++), new int[]{o.x(), o.y() + 1, o.z()});
                andarAltar(lvl, v);
            }
            case "morvain_cerco" -> {
                // el Cerco del Ancla (parte 7u): la muralla redonda con sus torres y su puerta, el ancla del sur con la escalera
                // tallada, la fragua, el campamento, la choza del primero con el libro 30 y el camino de los marcados desde el
                // santuario del Ancla del Sur (-Dcaja=x0,z0,x1,z1 para un trozo)
                String[] k = System.getProperty("caja", com.darkcraft.morvain.Cerco.X0 + "," + com.darkcraft.morvain.Cerco.Z0 + ","
                        + com.darkcraft.morvain.Cerco.X1 + "," + com.darkcraft.morvain.Cerco.Z1).split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, 50);
                Map<Long, BlockState> antes = new HashMap<>(MUNDO);
                com.darkcraft.morvain.Cerco v = com.darkcraft.morvain.Cerco.get();
                int hechos = 0;
                for (long c : v.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(c));
                    hechos++;
                }
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                System.out.println("El cerco: " + v.elementos().size() + " piezas, " + hechos + " chunks; " + v.esperas().size() + " que esperan; "
                        + v.estelas + " estelas, " + v.jaulas + " jaulas, " + v.yunques + " yunques, " + v.petates + " petates, " + v.farolas
                        + " farolas; sitios " + v.sitios().keySet());
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
                int n = 0;
                for (var o : v.esperas()) marcas.put((o.variante() == 0 ? "R" : o.variante() == 1 ? "C" : "Z") + (n++), new int[]{o.x(), o.y() + 1, o.z()});
                andarCerco(lvl, v, antes);
            }
            case "morvain_teatro" -> {
                // el Teatro Hundido (parte 7y): la grada, la orquesta, los pasos, el escenario con su muro, los bastidores, y bajo la
                // llanura el pasillo de la puerta real, la Sala de las Mascaras y el camerino (-Dcaja=x0,z0,x1,z1 para un trozo;
                // -Dabierto=true: los dos muros ilusorios abiertos)
                String[] k = System.getProperty("caja", com.darkcraft.morvain.Teatro.X0 + "," + com.darkcraft.morvain.Teatro.Z0 + ","
                        + com.darkcraft.morvain.Teatro.X1 + "," + (com.darkcraft.morvain.Teatro.Z1 + 10)).split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, 50);
                com.darkcraft.morvain.Teatro v = com.darkcraft.morvain.Teatro.get();
                int hechos = 0;
                for (long c : v.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(c));
                    hechos++;
                }
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                System.out.println("El teatro: " + v.elementos().size() + " piezas, " + hechos + " chunks; " + v.esperas().size() + " que esperan; "
                        + v.candilejas + " candilejas, " + v.columnas + " columnas, " + v.mascaras + " mascaras, " + v.espectadores + " espectadores, "
                        + v.lamparas + " lamparas; sitios " + v.sitios().keySet());
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
                int n = 0;
                for (var o : v.esperas()) marcas.put((n == 0 ? "B" : o.variante() == 0 ? "G" : o.variante() == 1 ? "I" : "C") + (n++), new int[]{o.x(), o.y() + 1, o.z()});
                andarTeatro(lvl, v);
                if (Boolean.getBoolean("abierto"))
                    for (String id : new String[]{com.darkcraft.morvain.Teatro.MURO_PUERTA, com.darkcraft.morvain.Teatro.MURO_TRONO})
                        for (BlockPos q : com.darkcraft.morvain.Teatro.muro(id)) MUNDO.remove(q.asLong());
            }
            case "morvain_nido" -> {
                // el Nido de Ceniza (parte 7z): la repisa quemada con sus penascos, el tesoro, la Atalaya del Cuervo y el camino del
                // santuario (-Dcaja=x0,z0,x1,z1 para un trozo)
                String[] k = System.getProperty("caja", com.darkcraft.morvain.Nido.X0 + "," + com.darkcraft.morvain.Nido.Z0 + ","
                        + com.darkcraft.morvain.Nido.X1 + "," + (com.darkcraft.morvain.Nido.Z1 + 22)).split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, 50);
                com.darkcraft.morvain.Nido v = com.darkcraft.morvain.Nido.get();
                int hechos = 0;
                for (long c : v.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(c));
                    hechos++;
                }
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                System.out.println("El nido: " + v.elementos().size() + " piezas, " + hechos + " chunks; " + v.esperas().size() + " que esperan; "
                        + v.calaveras + " calaveras, " + v.huesos + " huesos, " + v.lanzas + " lanzas, " + v.campanas + " campanas, " + v.velas
                        + " velas; sitios " + v.sitios().keySet());
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
                int n = 0;
                for (var o : v.esperas()) marcas.put((n == 0 ? "D" : o.variante() == 0 ? "G" : o.variante() == 1 ? "I" : "C") + (n++), new int[]{o.x(), o.y() + 1, o.z()});
                andarNido(lvl, v);
            }
            case "morvain_subida" -> {
                // la subida por la cadena del sur (parte 7aa): el camino de tablones de lo alto del ancla a la isla, los rellanos,
                // el Descansillo con su santuario, la Boca y la Garganta hasta la torre (-Dcaja=x0,z0,x1,z1 para un trozo; -Dsuelo=Y:
                // lo mas bajo que se vuelca)
                com.darkcraft.morvain.Subida v = com.darkcraft.morvain.Subida.get();
                String[] k = System.getProperty("caja", com.darkcraft.morvain.Subida.BX0 + "," + com.darkcraft.morvain.Subida.BZ0 + ","
                        + com.darkcraft.morvain.Subida.BX1 + "," + com.darkcraft.morvain.Subida.BZ1).split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                int ymin = Integer.parseInt(System.getProperty("suelo", "140"));
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, ymin);
                int hechos = 0;
                for (long c : v.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(c));
                    hechos++;
                }
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                System.out.println("La subida: " + v.elementos().size() + " piezas, " + hechos + " chunks; " + v.esperas().size() + " que esperan; "
                        + v.tablones + " tablones, " + v.peldanos + " peldanos, " + v.tallados + " tallados, " + v.postes + " postes, " + v.cuerdas
                        + " cuerdas, " + v.faroles + " faroles, " + v.velas + " velas, " + v.calaveras + " calaveras, " + v.barriles + " barriles, "
                        + v.dientes + " dientes, " + v.pedestales + " pedestales; sobre el hierro (no se pusieron) " + v.hierroPisado
                        + "; sitios " + v.sitios().keySet());
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
                int n = 0;
                for (var o : v.esperas())
                    if (o.z() >= z0 && o.z() <= z1)
                        marcas.put((o.tipo().equals("gargola") ? "G" : o.variante() == 0 ? "R" : o.variante() == 1 ? "C" : "Z") + (n++), new int[]{o.x(), o.y() + 1, o.z()});
                if (System.getProperty("caja") == null) andarSubida(lvl, v);
            }
            case "morvain_homenaje" -> {
                // la torre del homenaje por dentro (parte 7ab): la puerta, las seis plantas, las escaleras y el patio (-Dcaja=x0,z0,x1,z1 para
                // un trozo; -Dsuelo=Y: lo mas bajo que se vuelca; -Dabierta=true: el rastrillo y la reja de la azotea abiertos)
                com.darkcraft.morvain.Homenaje v = com.darkcraft.morvain.Homenaje.get();
                String[] k = System.getProperty("caja", (com.darkcraft.morvain.Homenaje.BX0 - 6) + "," + (com.darkcraft.morvain.Homenaje.BZ0 - 6) + ","
                        + (com.darkcraft.morvain.Homenaje.BX1 + 6) + "," + (com.darkcraft.morvain.Homenaje.BZ1 + 4)).split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                int ymin = Integer.parseInt(System.getProperty("suelo", "536"));
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, ymin);
                com.darkcraft.morvain.Subida sb = com.darkcraft.morvain.Subida.get();
                int hechos = 0;
                for (long c : sb.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    sb.construir(lvl, cx, cz, RandomSource.create(c));
                }
                for (long c : v.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(c));
                    hechos++;
                }
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), y = BlockPos.getY(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1 || y < ymin;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                minY = Math.max(minY, ymin);
                System.out.println("El homenaje: " + v.elementos().size() + " piezas, " + hechos + " chunks; " + v.esperas().size() + " que esperan; "
                        + v.pilares + " pilares, " + v.braseros + " braseros, " + v.faroles + " faroles, " + v.velas + " velas, " + v.estanterias
                        + " estanterias, " + v.celdas + " celdas, " + v.bancos + " bancos, " + v.espejos + " espejos, " + v.estatuas + " estatuas, "
                        + v.peldanos + " peldanos; sitios " + v.sitios().keySet());
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
                int n = 0;
                for (var o : v.esperas())
                    marcas.put((o.tipo().equals("isolde") ? "Q" : o.tipo().equals("centinela") ? (o.variante() == 0 ? "S" : "H") : o.tipo().substring(0, 1).toUpperCase())
                            + (n++), new int[]{o.x(), o.y() + 1, o.z()});
                if (System.getProperty("caja") == null) andarHomenaje(lvl, v);
            }
            case "morvain_grieta" -> {
                // el Puente de la Grieta (parte 7s): la grieta con su sangre, el puente con su espadana, la puerta del norte, el paso,
                // el cementerio, el humilladero, la hospederia con el libro 28 y el campamento de los penitentes
                // (-Dcaja=x0,z0,x1,z1 para un trozo)
                String[] k = System.getProperty("caja", (com.darkcraft.morvain.Grieta.CX - 125) + "," + (com.darkcraft.morvain.Grieta.CZ - 76) + ","
                        + (com.darkcraft.morvain.Grieta.CX + 108) + "," + (com.darkcraft.morvain.Grieta.CZ + 92)).split(",");
                int x0 = Integer.parseInt(k[0]), z0 = Integer.parseInt(k[1]), x1 = Integer.parseInt(k[2]), z1 = Integer.parseInt(k[3]);
                com.darkcraft.morvain.Vista.volcar((x, y, z, st) -> lvl.setBlock(new BlockPos(x, y, z), st, 2), x0, z0, x1, z1, 50);
                com.darkcraft.morvain.Grieta v = com.darkcraft.morvain.Grieta.get();
                int hechos = 0;
                for (long c : v.chunks()) {
                    int cx = (int) (c >> 32), cz = (int) c;
                    if ((cx << 4) + 15 < x0 || (cx << 4) > x1 || (cz << 4) + 15 < z0 || (cz << 4) > z1) continue;
                    v.construir(lvl, cx, cz, RandomSource.create(c));
                    hechos++;
                }
                MUNDO.keySet().removeIf(q -> {
                    int x = BlockPos.getX(q), z = BlockPos.getZ(q);
                    return x < x0 || x > x1 || z < z0 || z > z1;
                });
                minX = Math.max(minX, x0);
                maxX = Math.min(maxX, x1);
                minZ = Math.max(minZ, z0);
                maxZ = Math.min(maxZ, z1);
                System.out.println("La grieta: " + v.elementos().size() + " piezas, " + hechos + " chunks; " + v.esperas().size() + " que esperan; "
                        + v.tumbas + " tumbas (" + v.cruces + " cruces), " + v.tiendas + " tiendas, " + v.postes + " postes, " + v.velas + " velas; sitios "
                        + v.sitios().keySet());
                for (var e : v.sitios().entrySet()) {
                    int[] q = e.getValue();
                    if (q[0] >= x0 && q[0] <= x1 && q[2] >= z0 && q[2] <= z1) marcas.put(e.getKey(), new int[]{q[0], q[1] + 1, q[2]});
                }
                int n = 0;
                for (var o : v.esperas()) marcas.put("P" + (n++), new int[]{o.x(), o.y() + 1, o.z()});
                andarGrieta(lvl, v);
            }
            case "barco" -> construir(lvl, List.of(new com.darkcraft.pueblo.gen.Barco(77, true)));
            case "barco_roto" -> construir(lvl, List.of(new com.darkcraft.pueblo.gen.Barco(77, false)));
            default -> throw new IllegalArgumentException("plano desconocido: " + que);
        }
        System.out.println(que + ": " + MUNDO.size() + " bloques en " + (System.currentTimeMillis() - t0) + " ms; caja " + minX + "," + minY + "," + minZ
                + " .. " + maxX + "," + maxY + "," + maxZ + "; entidades " + ENTIDADES.size());
        escribir(salida, marcas);
    }

    /** La aldea de los pilotes (parte 7i): se llega a pie desde la entrada (junto al santuario) a todo, y cada podrido cabe en su sitio. */
    static void andarPilotes(WorldGenLevel lvl, com.darkcraft.morvain.Pilotes v) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
        int[] e = v.sitio("entrada");
        int X = com.darkcraft.morvain.Pilotes.CX, Z = com.darkcraft.morvain.Pilotes.CZ;
        var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], X - 60, Z - 66, X + 60, Z + 60, 1_500_000);
        StringBuilder sb = new StringBuilder("a pie desde la entrada: " + z0.celdas() + " celdas;");
        for (String s : v.sitios().keySet()) {
            int[] q = v.sitio(s);
            sb.append(' ').append(s).append(' ').append(z0.alcanza(q[0], q[1], q[2], 2));
        }
        System.out.println(sb);
        int malos = 0, chocan = 0;
        for (var o : v.esperas()) {
            boolean agua = o.postura() == com.darkcraft.enemigos.Enemigo.TUMBADO;
            if (!agua && !z0.alcanza(o.x(), o.y(), o.z(), 2.0)) {
                malos++;
                System.out.println("  " + o.grupo() + " sin llegar a pie: " + o.x() + "," + o.y() + "," + o.z());
            }
            // de pie cabe (dos de alto: 1,95); flotando, el fondo bajo sus pies
            for (int dy = 0; dy < 2; dy++)
                if (choca(mundo.en(o.x(), o.y() + dy, o.z()))) {
                    chocan++;
                    System.out.println("  " + o.grupo() + " metido en un bloque: " + o.x() + "," + (o.y() + dy) + "," + o.z() + " " + mundo.en(o.x(), o.y() + dy, o.z()));
                }
            if (!choca(mundo.en(o.x(), o.y() - 1, o.z()))) System.out.println("  " + o.grupo() + " sin suelo: " + o.x() + "," + o.y() + "," + o.z());
        }
        System.out.println("podridos: " + v.esperas().size() + ", a los que no se llega " + malos + ", metidos en un bloque " + chocan);
    }

    /**
     * La Laguna Negra (parte 7j): se llega a pie desde la entrada (junto al santuario) al muelle y al embarcadero; la barca no
     * (esta en el agua: el Barquero salta); el Barquero cabe en su barca (1,2 x 3,6) y cada podrido en su sitio.
     */
    static void andarLaguna(WorldGenLevel lvl, com.darkcraft.morvain.LagunaNegra v) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
        int[] e = v.sitio("entrada");
        int X = com.darkcraft.morvain.LagunaNegra.CX, Z = com.darkcraft.morvain.LagunaNegra.CZ;
        var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], X - 70, Z - 46, X + 48, Z + 46, 1_500_000);
        StringBuilder sb = new StringBuilder("a pie desde la entrada: " + z0.celdas() + " celdas;");
        for (String s : v.sitios().keySet()) {
            int[] q = v.sitio(s);
            sb.append(' ').append(s).append(' ').append(z0.alcanza(q[0], q[1], q[2], 2));
        }
        System.out.println(sb);
        // el embarcadero entero: cuantas de sus tablas se pisan a pie
        int pisa = 0, tablas = 0;
        for (int u = -com.darkcraft.morvain.LagunaNegra.R_PLATAFORMA; u <= com.darkcraft.morvain.LagunaNegra.R_PLATAFORMA; u++)
            for (int w = -com.darkcraft.morvain.LagunaNegra.R_PLATAFORMA; w <= com.darkcraft.morvain.LagunaNegra.R_PLATAFORMA; w++) {
                if (!choca(mundo.en(X + u, com.darkcraft.morvain.LagunaNegra.PASO, Z + w))) continue;
                tablas++;
                if (z0.alcanza(X + u, com.darkcraft.morvain.LagunaNegra.PASO + 1, Z + w, 0.6)) pisa++;
            }
        System.out.println("el embarcadero: " + tablas + " tablas, se pisan a pie " + pisa);
        // el Barquero en su barca: el suelo bajo sus pies y nada en su caja (1,2 x 3,6: cuatro de alto)
        int[] b = v.sitio("barquero");
        int dentro = 0;
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++)
                for (int dy = 0; dy < 4; dy++)
                    if ((dx == 0 || dz == 0) && choca(mundo.en(b[0] + dx, b[1] + dy, b[2] + dz))) {
                        dentro++;
                        System.out.println("  el Barquero choca con " + mundo.en(b[0] + dx, b[1] + dy, b[2] + dz) + " en " + (b[0] + dx) + "," + (b[1] + dy) + "," + (b[2] + dz));
                    }
        System.out.println("el Barquero: en " + b[0] + "," + b[1] + "," + b[2] + ", suelo " + mundo.en(b[0], b[1] - 1, b[2]) + ", bloques en su caja " + dentro);
        int malos = 0, chocan = 0;
        for (var o : v.esperas()) {
            boolean agua = o.postura() == com.darkcraft.enemigos.Enemigo.TUMBADO;
            if (agua && !mundo.en(o.x(), o.y(), o.z()).is(Blocks.WATER))
                System.out.println("  " + o.grupo() + " flotando sin agua: " + o.x() + "," + o.y() + "," + o.z() + " " + mundo.en(o.x(), o.y(), o.z()));
            if (!agua && !z0.alcanza(o.x(), o.y(), o.z(), 2.0)) {
                malos++;
                System.out.println("  " + o.grupo() + " sin llegar a pie: " + o.x() + "," + o.y() + "," + o.z());
            }
            for (int dy = 0; dy < 2; dy++)
                if (choca(mundo.en(o.x(), o.y() + dy, o.z()))) {
                    chocan++;
                    System.out.println("  " + o.grupo() + " metido en un bloque: " + o.x() + "," + (o.y() + dy) + "," + o.z() + " " + mundo.en(o.x(), o.y() + dy, o.z()));
                }
            if (!choca(mundo.en(o.x(), o.y() - 1, o.z()))) System.out.println("  " + o.grupo() + " sin suelo: " + o.x() + "," + o.y() + "," + o.z());
        }
        System.out.println("podridos: " + v.esperas().size() + ", a los que no se llega " + malos + ", metidos en un bloque " + chocan);
    }

    /** El monasterio (parte 7k): se llega a pie desde el santuario a todo, y cada helado cabe en su sitio y tiene suelo. */
    static void andarMonasterio(WorldGenLevel lvl, com.darkcraft.morvain.Monasterio v) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
        int[] e = v.sitio("entrada");
        int X = com.darkcraft.morvain.Monasterio.CX, Z = com.darkcraft.morvain.Monasterio.CZ;
        var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], X - 30, Z - 36, X + 44, Z + 56, 2_500_000);
        StringBuilder sb = new StringBuilder("a pie desde la entrada: " + z0.celdas() + " celdas;");
        for (String s : v.sitios().keySet()) {
            if (s.equals("campana")) continue;
            int[] q = v.sitio(s);
            sb.append(' ').append(s).append(' ').append(z0.alcanza(q[0], q[1], q[2], 2));
        }
        System.out.println(sb);
        int malos = 0, chocan = 0, sinSuelo = 0;
        for (var o : v.esperas()) {
            if (!z0.alcanza(o.x(), o.y(), o.z(), 2.0)) {
                malos++;
                System.out.println("  " + o.grupo() + " sin llegar a pie: " + o.x() + "," + o.y() + "," + o.z());
            }
            for (int dy = 0; dy < 2; dy++)
                if (choca(mundo.en(o.x(), o.y() + dy, o.z()))) {
                    chocan++;
                    System.out.println("  " + o.grupo() + " metido en un bloque: " + o.x() + "," + (o.y() + dy) + "," + o.z() + " " + mundo.en(o.x(), o.y() + dy, o.z()));
                }
            if (!choca(mundo.en(o.x(), o.y() - 1, o.z()))) {
                sinSuelo++;
                System.out.println("  " + o.grupo() + " sin suelo: " + o.x() + "," + o.y() + "," + o.z() + " " + mundo.en(o.x(), o.y() - 1, o.z()));
            }
        }
        System.out.println("helados: " + v.esperas().size() + ", a los que no se llega " + malos + ", metidos en un bloque " + chocan + ", sin suelo " + sinSuelo);
    }

    /**
     * La ermita (parte 7l): se llega a pie desde el postigo del huerto del monasterio a todo (la escalera, el calvario, la
     * ermita, la cruz); cada helado cabe y tiene suelo; donde reza el abad (1,6 x 4,8) no hay nada.
     */
    static void andarErmita(WorldGenLevel lvl, com.darkcraft.morvain.Ermita v) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
        int[] e = v.sitio("postigo");
        int X = com.darkcraft.morvain.Ermita.CX, Z = com.darkcraft.morvain.Ermita.CZ;
        var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], X - 34, Z - 58, X + 34, com.darkcraft.morvain.Ermita.ESC_Z0 + 12, 3_000_000);
        StringBuilder sb = new StringBuilder("a pie desde el postigo: " + z0.celdas() + " celdas;");
        for (String s : v.sitios().keySet()) {
            if (s.equals("cruz_alta")) continue;
            int[] q = v.sitio(s);
            sb.append(' ').append(s).append(' ').append(z0.alcanza(q[0], q[1], q[2], 2));
        }
        System.out.println(sb);
        // la escalera, escalon a escalon: se pisa y no hay nada encima (tres de alto)
        int malEsc = 0;
        for (int z = com.darkcraft.morvain.Ermita.ESC_Z1; z <= com.darkcraft.morvain.Ermita.ESC_Z0; z++) {
            int h = com.darkcraft.morvain.Ermita.escalon(z);
            for (int dx = -1; dx <= 1; dx++) {
                int x = com.darkcraft.morvain.Ermita.ESC_X + dx;
                boolean bien = choca(mundo.en(x, h - 1, z)) && !choca(mundo.en(x, h, z)) && !choca(mundo.en(x, h + 1, z)) && !choca(mundo.en(x, h + 2, z));
                if (!bien) {
                    malEsc++;
                    if (malEsc < 12) System.out.println("  escalon raro en " + x + "," + h + "," + z + ": " + mundo.en(x, h - 1, z) + " / " + mundo.en(x, h, z) + " / "
                            + mundo.en(x, h + 1, z) + " / " + mundo.en(x, h + 2, z));
                }
            }
        }
        System.out.println("escalera: " + v.escalones + " escalones, raros " + malEsc);
        int malos = 0, chocan = 0, sinSuelo = 0;
        for (var o : v.esperas()) {
            if (!z0.alcanza(o.x(), o.y(), o.z(), 2.0)) {
                malos++;
                System.out.println("  " + o.grupo() + " sin llegar a pie: " + o.x() + "," + o.y() + "," + o.z());
            }
            for (int dy = 0; dy < 3; dy++)
                if (choca(mundo.en(o.x(), o.y() + dy, o.z()))) {
                    chocan++;
                    System.out.println("  " + o.grupo() + " metido en un bloque: " + o.x() + "," + (o.y() + dy) + "," + o.z() + " " + mundo.en(o.x(), o.y() + dy, o.z()));
                }
            if (!choca(mundo.en(o.x(), o.y() - 1, o.z()))) {
                sinSuelo++;
                System.out.println("  " + o.grupo() + " sin suelo: " + o.x() + "," + o.y() + "," + o.z() + " " + mundo.en(o.x(), o.y() - 1, o.z()));
            }
        }
        System.out.println("helados: " + v.esperas().size() + ", a los que no se llega " + malos + ", metidos en un bloque " + chocan + ", sin suelo " + sinSuelo);
        int[] a = v.sitio("abad");
        int estorba = 0;
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++)
                for (int y = a[1]; y <= a[1] + 5; y++)
                    if (choca(mundo.en(a[0] + dx, y, a[2] + dz))) {
                        estorba++;
                        System.out.println("  estorba al abad: " + (a[0] + dx) + "," + y + "," + (a[2] + dz) + " " + mundo.en(a[0] + dx, y, a[2] + dz));
                    }
        System.out.println("el abad: suelo " + mundo.en(a[0], a[1] - 1, a[2]) + ", lo que estorba donde reza " + estorba + " (tiene que ser 0)");
    }

    /**
     * La Puerta Oeste de la Capital (parte 7m): se llega a pie desde la entrada (el camino del santuario) a cada sitio (la sala
     * de la guardia por la torre del norte, el adarve por sus escaleras) y a cada calcinado; ninguno esta metido en un bloque
     * (un poco de ceniza o una alfombra no cuentan) y todos tienen suelo.
     */
    static void andarCapital(WorldGenLevel lvl, com.darkcraft.morvain.Capital v) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
            BlockState s = MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
            // las alfombras de ceniza no estorban
            return s.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock ? aire : s;
        };
        int[] e = v.sitio("entrada");
        int X = com.darkcraft.morvain.Capital.CX, Z = com.darkcraft.morvain.Capital.CZ;
        var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], X - 70, Z - 80, X + 124, Z + 80, 3_000_000);
        StringBuilder sb = new StringBuilder("a pie desde la entrada: " + z0.celdas() + " celdas;");
        for (String s : v.sitios().keySet()) {
            int[] q = v.sitio(s);
            sb.append(' ').append(s).append(' ').append(z0.alcanza(q[0], q[1], q[2], 2));
        }
        System.out.println(sb);
        int malos = 0, chocan = 0, sinSuelo = 0;
        for (var o : v.esperas()) {
            if (!z0.alcanza(o.x(), o.y(), o.z(), 2.0)) {
                malos++;
                System.out.println("  " + o.grupo() + " sin llegar a pie: " + o.x() + "," + o.y() + "," + o.z());
            }
            for (int dy = 0; dy < 2; dy++) {
                BlockState st = mundo.en(o.x(), o.y() + dy, o.z());
                if (choca(st) && !(dy == 0 && st.is(Blocks.SNOW))) {
                    chocan++;
                    System.out.println("  " + o.grupo() + " metido en un bloque: " + o.x() + "," + (o.y() + dy) + "," + o.z() + " " + st);
                }
            }
            if (!choca(mundo.en(o.x(), o.y() - 1, o.z())) && !choca(MUNDO.getOrDefault(BlockPos.asLong(o.x(), o.y(), o.z()), aire))) {
                sinSuelo++;
                System.out.println("  " + o.grupo() + " sin suelo: " + o.x() + "," + o.y() + "," + o.z() + " " + mundo.en(o.x(), o.y() - 1, o.z()));
            }
        }
        System.out.println("calcinados: " + v.esperas().size() + ", a los que no se llega " + malos + ", metidos en un bloque " + chocan + ", sin suelo " + sinSuelo);
    }

    /**
     * El Patio del Juramento (parte 7n): se llega a pie desde la plaza de la capital por la calle y el paso del rastrillo (subido)
     * al patio, al estrado y a cada sitio de dentro; con la reja de la torre bajada no se entra en la torre y, quitada, si (el
     * arcon, el atril y lo alto por la escalera de mano). Cada uno de los que esperan se pisa y no esta metido en nada; donde
     * espera el Condestable cabe (1,6 de ancho y 4,6 de alto).
     */
    static void andarJuramento(WorldGenLevel lvl, com.darkcraft.morvain.Capital c, com.darkcraft.morvain.Juramento v) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
            BlockState s = MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
            return s.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock ? aire : s;
        };
        int[] e = c.sitio("plaza");
        int X = com.darkcraft.morvain.Capital.CX, Z = com.darkcraft.morvain.Capital.CZ;
        var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], X + 70, Z - 50, X + 215, Z + 50, 2_500_000);
        StringBuilder sb = new StringBuilder("a pie desde la plaza (reja bajada): " + z0.celdas() + " celdas;");
        for (String s : v.sitios().keySet()) {
            int[] q = v.sitio(s);
            sb.append(' ').append(s).append(' ').append(z0.alcanza(q[0], q[1], q[2], 2));
        }
        System.out.println(sb);
        int malos = 0, chocan = 0, sinSuelo = 0, n = 0;
        for (var o : v.esperas()) {
            if (!z0.alcanza(o.x(), o.y(), o.z(), 2.0)) {
                malos++;
                System.out.println("  " + o.grupo() + " sin llegar a pie: " + o.x() + "," + o.y() + "," + o.z());
            }
            for (int dy = 0; dy < 2; dy++) {
                BlockState st = mundo.en(o.x(), o.y() + dy, o.z());
                if (choca(st)) {
                    chocan++;
                    System.out.println("  " + o.grupo() + " metido en un bloque: " + o.x() + "," + (o.y() + dy) + "," + o.z() + " " + st);
                }
            }
            if (!choca(mundo.en(o.x(), o.y() - 1, o.z()))) {
                sinSuelo++;
                System.out.println("  " + o.grupo() + " sin suelo: " + o.x() + "," + o.y() + "," + o.z() + " " + mundo.en(o.x(), o.y() - 1, o.z()));
            }
            n++;
        }
        System.out.println("los que esperan: " + v.esperas().size() + ", a los que no se llega " + malos + ", metidos en un bloque " + chocan + ", sin suelo " + sinSuelo);
        int[] k = v.sitio("condestable");
        int estorba = 0;
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++)
                for (int y = k[1]; y <= k[1] + 5; y++)
                    if (choca(mundo.en(k[0] + dx, y, k[2] + dz))) {
                        estorba++;
                        System.out.println("  estorba al condestable: " + (k[0] + dx) + "," + y + "," + (k[2] + dz) + " " + mundo.en(k[0] + dx, y, k[2] + dz));
                    }
        // el paso del rastrillo: libre (subido) salvo los dientes de arriba
        int paso = 0;
        for (int dz = -com.darkcraft.morvain.Juramento.PASO_V; dz <= com.darkcraft.morvain.Juramento.PASO_V; dz++)
            for (int y = com.darkcraft.morvain.Juramento.PISO; y < com.darkcraft.morvain.Juramento.PISO + com.darkcraft.morvain.Juramento.PASO_ALTO - 1; y++)
                if (choca(mundo.en(X + com.darkcraft.morvain.Juramento.RASTRILLO_U, y, Z + dz))) paso++;
        System.out.println("el condestable: suelo " + mundo.en(k[0], k[1] - 1, k[2]) + ", lo que estorba donde espera " + estorba
                + " (tiene que ser 0); el paso del rastrillo tapado " + paso + " (tiene que ser 0)");
        // quitada la reja de la torre: se entra
        for (int dz = -1; dz <= 1; dz++)
            for (int y = com.darkcraft.morvain.Juramento.PISO; y <= com.darkcraft.morvain.Juramento.PISO + 3; y++)
                MUNDO.remove(BlockPos.asLong(X + com.darkcraft.morvain.Juramento.REJA_U, y, Z + dz));
        var z1 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], X + 70, Z - 50, X + 215, Z + 50, 2_500_000);
        StringBuilder sb1 = new StringBuilder("con la reja quitada:");
        for (String s : new String[]{"dentro_torre", "arcon", "atril", "alto_torre"}) {
            int[] q = v.sitio(s);
            sb1.append(' ').append(s).append(' ').append(z1.alcanza(q[0], q[1], q[2], 2));
        }
        System.out.println(sb1);
    }

    /**
     * La Academia Espectral (parte 7o): se llega a pie desde la entrada (el camino del santuario) a cada sitio (el altillo por
     * sus escalas, el archivo por la escala de la torre) y a cada erudito; ninguno esta metido en un bloque (las alfombras y
     * las velas no cuentan) y todos tienen suelo.
     */
    static void andarAcademia(WorldGenLevel lvl, com.darkcraft.morvain.Academia v) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
            BlockState s = MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
            return s.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock ? aire : s;
        };
        int[] e = v.sitio("entrada");
        int X = com.darkcraft.morvain.Academia.CX, Z = com.darkcraft.morvain.Academia.CZ;
        var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], X - 42, Z - 66, X + 42, Z + 64, 4_000_000);
        StringBuilder sb = new StringBuilder("a pie desde la entrada: " + z0.celdas() + " celdas;");
        for (String s : v.sitios().keySet()) {
            int[] q = v.sitio(s);
            sb.append(' ').append(s).append(' ').append(z0.alcanza(q[0], q[1], q[2], 2));
        }
        System.out.println(sb);
        int malos = 0, chocan = 0, sinSuelo = 0;
        for (var o : v.esperas()) {
            if (!z0.alcanza(o.x(), o.y(), o.z(), 2.0)) {
                malos++;
                System.out.println("  " + o.grupo() + " sin llegar a pie: " + o.x() + "," + o.y() + "," + o.z());
            }
            for (int dy = 0; dy < 2; dy++) {
                BlockState st = mundo.en(o.x(), o.y() + dy, o.z());
                if (choca(st)) {
                    chocan++;
                    System.out.println("  " + o.grupo() + " metido en un bloque: " + o.x() + "," + (o.y() + dy) + "," + o.z() + " " + st);
                }
            }
            if (!choca(mundo.en(o.x(), o.y() - 1, o.z()))) {
                sinSuelo++;
                System.out.println("  " + o.grupo() + " sin suelo: " + o.x() + "," + o.y() + "," + o.z() + " " + mundo.en(o.x(), o.y() - 1, o.z()));
            }
        }
        System.out.println("eruditos: " + v.esperas().size() + ", a los que no se llega " + malos + ", metidos en un bloque " + chocan + ", sin suelo " + sinSuelo);
    }

    /**
     * El observatorio (parte 7p): de la entrada del camino se llega a pie a cada sitio (la celda, su atril y su arcon solo con la
     * reja quitada; la azotea por su escala) y a cada uno de los que esperan; ninguno metido en un bloque (la Rectora mide 3,3)
     * y todos con suelo. Con el sello cerrado no se pasa del vestibulo a la rotonda.
     */
    /**
     * El Puerto Varado (parte 7q): se llega a pie desde la entrada (junto al santuario) a cada sitio (la bahia por las escaleras
     * del muelle y la grada, lo alto del faro por su escalera) y a cada salobre; ninguno esta metido en un bloque y todos tienen
     * suelo (el agua de los charcos cuenta como suelo: se pisa el barro de debajo).
     */
    static void andarPuerto(WorldGenLevel lvl, com.darkcraft.morvain.Varadero v) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
            BlockState s = MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
            return s.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock || s.getBlock() instanceof net.minecraft.world.level.block.SnowLayerBlock
                    && s.getValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS) <= 4 ? aire : s;
        };
        int[] e = v.sitio("entrada");
        int X = com.darkcraft.morvain.Varadero.CX, Z = com.darkcraft.morvain.Varadero.CZ;
        var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], X - 60, Z - 66, X + 124, Z + 62, 6_000_000);
        StringBuilder sb = new StringBuilder("a pie desde la entrada: " + z0.celdas() + " celdas;");
        for (String s : v.sitios().keySet()) {
            int[] q = v.sitio(s);
            sb.append(' ').append(s).append(' ').append(z0.alcanza(q[0], q[1], q[2], 2));
        }
        System.out.println(sb);
        int malos = 0, chocan = 0, sinSuelo = 0;
        for (var o : v.esperas()) {
            if (!z0.alcanza(o.x(), o.y(), o.z(), 2.0)) {
                malos++;
                System.out.println("  " + o.grupo() + " sin llegar a pie: " + o.x() + "," + o.y() + "," + o.z());
            }
            for (int dy = 0; dy < 2; dy++) {
                BlockState st = mundo.en(o.x(), o.y() + dy, o.z());
                if (choca(st)) {
                    chocan++;
                    System.out.println("  " + o.grupo() + " metido en un bloque: " + o.x() + "," + (o.y() + dy) + "," + o.z() + " " + st);
                }
            }
            BlockState abajo = mundo.en(o.x(), o.y() - 1, o.z());
            if (!choca(abajo) && !abajo.is(Blocks.WATER)) {
                sinSuelo++;
                System.out.println("  " + o.grupo() + " sin suelo: " + o.x() + "," + o.y() + "," + o.z() + " " + abajo);
            }
        }
        System.out.println("salobres: " + v.esperas().size() + ", a los que no se llega " + malos + ", metidos en un bloque " + chocan + ", sin suelo " + sinSuelo);
    }

    /** El Costillar (parte 7r): se llega a pie desde la entrada (junto al santuario) a todo (menos a lo alto de la calavera). */
    static void andarCostillar(WorldGenLevel lvl, com.darkcraft.morvain.Costillar v) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
            BlockState s = MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
            return s.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock || s.getBlock() instanceof net.minecraft.world.level.block.SnowLayerBlock
                    && s.getValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS) <= 4 ? aire : s;
        };
        int[] e = v.sitio("entrada");
        var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], com.darkcraft.morvain.Costillar.X0, com.darkcraft.morvain.Costillar.Z0,
                com.darkcraft.morvain.Costillar.X1, com.darkcraft.morvain.Costillar.Z1, 4_000_000);
        StringBuilder sb = new StringBuilder("a pie desde la entrada: " + z0.celdas() + " celdas;");
        for (String s : v.sitios().keySet()) {
            int[] q = v.sitio(s);
            sb.append(' ').append(s).append(' ').append(z0.alcanza(q[0], q[1], q[2], 2));
        }
        System.out.println(sb);
        int malos = 0, chocan = 0, sinSuelo = 0;
        for (var o : v.esperas()) {
            if (!z0.alcanza(o.x(), o.y(), o.z(), 2.0)) {
                malos++;
                System.out.println("  " + o.grupo() + " sin llegar a pie: " + o.x() + "," + o.y() + "," + o.z());
            }
            for (int dy = 0; dy < 2; dy++) {
                BlockState st = mundo.en(o.x(), o.y() + dy, o.z());
                if (choca(st)) {
                    chocan++;
                    System.out.println("  " + o.grupo() + " metido en un bloque: " + o.x() + "," + (o.y() + dy) + "," + o.z() + " " + st);
                }
            }
            BlockState abajo = mundo.en(o.x(), o.y() - 1, o.z());
            if (!choca(abajo)) {
                sinSuelo++;
                System.out.println("  " + o.grupo() + " sin suelo: " + o.x() + "," + o.y() + "," + o.z() + " " + abajo);
            }
        }
        // la sala: lo que se pisa dentro de las costillas (que la Vieja pase y Brann pelee): bloques que estorban a ras de suelo
        int estorban = 0, celdas = 0;
        int X = com.darkcraft.morvain.Costillar.CX, Z = com.darkcraft.morvain.Costillar.CZ, P = com.darkcraft.morvain.Costillar.PISO;
        for (int x = X + com.darkcraft.morvain.Costillar.SALA_U0; x <= X + com.darkcraft.morvain.Costillar.SALA_U1; x++)
            for (int z = Z - 16; z <= Z + 16; z++) {
                if (!com.darkcraft.morvain.Costillar.enSala(x + 0.5, z + 0.5)) continue;
                celdas++;
                if (choca(mundo.en(x, P, z)) || choca(mundo.en(x, P + 1, z))) estorban++;
            }
        System.out.println("Brann y los salobres: " + v.esperas().size() + ", a los que no se llega " + malos + ", metidos en un bloque " + chocan + ", sin suelo "
                + sinSuelo + "; en la sala " + celdas + " celdas, con algo que estorba " + estorban);
    }

    /**
     * El Altar Profanado (parte 7t): se llega a pie desde la entrada (el camino del santuario) a cada sitio (menos lo alto y la campana)
     * y a cada penitente; ninguno metido en un bloque y todos con suelo; y lo que estorba en el suelo de la nave y el crucero (donde
     * se pelea con Anselmo).
     */
    static void andarAltar(WorldGenLevel lvl, com.darkcraft.morvain.Altar v) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
            BlockState s = MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
            return s.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock || s.getBlock() instanceof net.minecraft.world.level.block.CandleBlock
                    || s.getBlock() instanceof net.minecraft.world.level.block.RedStoneWireBlock ? aire : s;
        };
        int[] e = v.sitio("entrada");
        var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], com.darkcraft.morvain.Altar.X0, com.darkcraft.morvain.Altar.Z0,
                com.darkcraft.morvain.Altar.X1, com.darkcraft.morvain.Altar.Z1, 4_000_000);
        StringBuilder sb = new StringBuilder("a pie desde la entrada: " + z0.celdas() + " celdas;");
        for (String s : v.sitios().keySet()) {
            if (s.equals("alto")) continue;
            int[] q = v.sitio(s);
            sb.append(' ').append(s).append(' ').append(z0.alcanza(q[0], q[1], q[2], 2));
        }
        System.out.println(sb);
        int malos = 0, chocan = 0, sinSuelo = 0;
        for (var o : v.esperas()) {
            if (!z0.alcanza(o.x(), o.y(), o.z(), 2.0)) {
                malos++;
                System.out.println("  " + o.grupo() + " sin llegar a pie: " + o.x() + "," + o.y() + "," + o.z());
            }
            for (int dy = 0; dy < 2; dy++) {
                BlockState st = mundo.en(o.x(), o.y() + dy, o.z());
                if (choca(st)) {
                    chocan++;
                    System.out.println("  " + o.grupo() + " metido en un bloque: " + o.x() + "," + (o.y() + dy) + "," + o.z() + " " + st);
                }
            }
            BlockState abajo = mundo.en(o.x(), o.y() - 1, o.z());
            if (!choca(abajo)) {
                sinSuelo++;
                System.out.println("  " + o.grupo() + " sin suelo: " + o.x() + "," + o.y() + "," + o.z() + " " + abajo);
            }
        }
        // la nave y el crucero, de pilar a pilar: lo que estorba a ras de suelo (sin contar los pilares)
        int estorban = 0, celdas = 0;
        int X = com.darkcraft.morvain.Altar.CX, Z = com.darkcraft.morvain.Altar.CZ, P = com.darkcraft.morvain.Altar.PISO;
        for (int x = X - 5; x <= X + 5; x++)
            for (int z = Z - 19; z <= Z + 22; z++) {
                celdas++;
                if (choca(mundo.en(x, P, z)) && choca(mundo.en(x, P + 1, z))) estorban++;
            }
        System.out.println("Anselmo y los penitentes: " + v.esperas().size() + ", a los que no se llega " + malos + ", metidos en un bloque " + chocan
                + ", sin suelo " + sinSuelo + "; en la nave " + celdas + " celdas, con algo que estorba " + estorban);
    }

    /**
     * El Cerco del Ancla (parte 7u): se llega a pie desde la entrada (junto al santuario del Ancla del Sur) a cada sitio (menos lo
     * alto de las torres y el sitio para mirar) y a cada marcado (menos los cazadores de lo alto de las torres, que estan para
     * dispararte); ninguno metido en un bloque y todos con suelo; la escalera sube a lo alto del ancla; y el cerco no toca ni la
     * cadena (el hierro negro) ni la piramide del ancla (salvo la escalera), que son de la ciudadela.
     */
    /**
     * El Teatro Hundido (parte 7y): desde la entrada (el camino del santuario) se llega a pie a la grada, la orquesta, los pasos, el
     * escenario y los bastidores; con la puerta real tapiada no se llega ni al pasillo ni a la sala; abierta, si (y a Pell); con el
     * muro del trono abierto, al camerino. Cada uno de los de alli con suelo y sin estar metido en un bloque.
     */
    static void andarTeatro(WorldGenLevel lvl, com.darkcraft.morvain.Teatro v) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        java.util.Set<Long> quitados = new java.util.HashSet<>();
        com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
            if (quitados.contains(BlockPos.asLong(x, y, z))) return aire;
            BlockState s = MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
            return s.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock || s.getBlock() instanceof net.minecraft.world.level.block.CandleBlock
                    ? aire : s;
        };
        int[] e = v.sitio("entrada");
        int X0 = com.darkcraft.morvain.Teatro.X0, Z0 = com.darkcraft.morvain.Teatro.Z0, X1 = com.darkcraft.morvain.Teatro.X1, Z1 = com.darkcraft.morvain.Teatro.Z1 + 8;
        for (int paso = 0; paso < 3; paso++) {
            if (paso == 1) for (BlockPos q : com.darkcraft.morvain.Teatro.muro(com.darkcraft.morvain.Teatro.MURO_PUERTA)) quitados.add(q.asLong());
            if (paso == 2) for (BlockPos q : com.darkcraft.morvain.Teatro.muro(com.darkcraft.morvain.Teatro.MURO_TRONO)) quitados.add(q.asLong());
            var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], X0, Z0, X1, Z1, 6_000_000);
            StringBuilder sb = new StringBuilder(paso == 0 ? "a pie, todo cerrado: " : paso == 1 ? "a pie, la puerta real abierta: " : "a pie, los dos muros abiertos: ");
            sb.append(z0.celdas()).append(" celdas;");
            for (String s : v.sitios().keySet()) {
                if (s.equals("alto")) continue;
                int[] q = v.sitio(s);
                sb.append(' ').append(s).append(' ').append(z0.alcanza(q[0], q[1], q[2], 2));
            }
            System.out.println(sb);
            if (paso < 2) continue;
            int malos = 0, chocan = 0, sinSuelo = 0;
            for (var o : v.esperas()) {
                if (!z0.alcanza(o.x(), o.y(), o.z(), 2.0)) {
                    malos++;
                    System.out.println("  " + o.grupo() + " sin llegar a pie: " + o.x() + "," + o.y() + "," + o.z());
                }
                for (int dy = 0; dy < 2; dy++) {
                    BlockState st = mundo.en(o.x(), o.y() + dy, o.z());
                    if (choca(st)) {
                        chocan++;
                        System.out.println("  " + o.grupo() + " metido en un bloque: " + o.x() + "," + (o.y() + dy) + "," + o.z() + " " + st);
                    }
                }
                BlockState abajo = mundo.en(o.x(), o.y() - 1, o.z());
                if (!choca(abajo)) {
                    sinSuelo++;
                    System.out.println("  " + o.grupo() + " sin suelo: " + o.x() + "," + o.y() + "," + o.z() + " " + abajo);
                }
            }
            System.out.println("los del teatro: " + v.esperas().size() + ", sin llegar " + malos + ", metidos en un bloque " + chocan + ", sin suelo " + sinSuelo);
        }
    }

    /**
     * El Nido de Ceniza (parte 7z): desde el camino del santuario se llega a pie a la repisa, al tesoro, a la atalaya (al atril y al
     * arcon) y, por la escalera de mano, a lo alto de la torre. Cada uno de los de alli con suelo y sin estar metido en un bloque.
     */
    static void andarNido(WorldGenLevel lvl, com.darkcraft.morvain.Nido v) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
            BlockState s = MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
            return s.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock || s.getBlock() instanceof net.minecraft.world.level.block.CandleBlock
                    || s.getBlock() instanceof net.minecraft.world.level.block.SnowLayerBlock ? aire : s;
        };
        int[] e = v.sitio("santuario");
        var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], com.darkcraft.morvain.Nido.X0, com.darkcraft.morvain.Nido.Z0,
                com.darkcraft.morvain.Nido.X1, com.darkcraft.morvain.Nido.Z1 + 20, 6_000_000);
        StringBuilder sb = new StringBuilder("a pie desde el santuario: " + z0.celdas() + " celdas;");
        for (String s : v.sitios().keySet()) {
            if (s.equals("alto")) continue;
            int[] q = v.sitio(s);
            sb.append(' ').append(s).append(' ').append(z0.alcanza(q[0], q[1], q[2], 2));
        }
        System.out.println(sb);
        int malos = 0, chocan = 0, sinSuelo = 0;
        for (var o : v.esperas()) {
            if (!z0.alcanza(o.x(), o.y(), o.z(), 2.0)) {
                malos++;
                System.out.println("  " + o.grupo() + " sin llegar a pie: " + o.x() + "," + o.y() + "," + o.z());
            }
            for (int dy = 0; dy < 2; dy++) {
                BlockState st = mundo.en(o.x(), o.y() + dy, o.z());
                if (choca(st)) {
                    chocan++;
                    System.out.println("  " + o.grupo() + " metido en un bloque: " + o.x() + "," + (o.y() + dy) + "," + o.z() + " " + st);
                }
            }
            BlockState abajo = mundo.en(o.x(), o.y() - 1, o.z());
            if (!choca(abajo)) {
                sinSuelo++;
                System.out.println("  " + o.grupo() + " sin suelo: " + o.x() + "," + o.y() + "," + o.z() + " " + abajo);
            }
        }
        System.out.println("los del nido: " + v.esperas().size() + ", sin llegar " + malos + ", metidos en un bloque " + chocan + ", sin suelo " + sinSuelo);
        // la repisa, despejada para el dragon: lo que hay por encima del suelo en un circulo de radio 26
        int altos = 0;
        for (int du = -26; du <= 26; du++)
            for (int dv = -26; dv <= 26; dv++) {
                if (du * du + dv * dv > 26 * 26) continue;
                for (int y = com.darkcraft.morvain.Nido.SUELO + 6; y < com.darkcraft.morvain.Nido.SUELO + 40; y++)
                    if (choca(mundo.en(com.darkcraft.morvain.Nido.CX + du, y, com.darkcraft.morvain.Nido.CZ + dv))) altos++;
            }
        System.out.println("por encima de la repisa (6 a 40 sobre el suelo, radio 26): " + altos + " bloques");
    }

    static void andarSubida(WorldGenLevel lvl, com.darkcraft.morvain.Subida v) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        BlockState losa = Blocks.DARK_OAK_SLAB.defaultBlockState();
        // (los huecos se saltan: Andar no sabe saltar, asi que se ven como si tuvieran tablon)
        com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
            BlockState s = MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
            if (s.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock || s.getBlock() instanceof net.minecraft.world.level.block.CandleBlock) return aire;
            if (s.isAir() && x >= com.darkcraft.morvain.Subida.X0 && x <= com.darkcraft.morvain.Subida.X1 && v.hayCamino(z)) {
                double sup = v.superficie(z);
                int yb = sup == Math.floor(sup) ? (int) sup - 1 : (int) Math.floor(sup);
                if (y == yb) return losa;
            }
            return s;
        };
        int[] e = v.sitio("ancla");
        var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], com.darkcraft.morvain.Subida.BX0, com.darkcraft.morvain.Subida.BZ0,
                com.darkcraft.morvain.Subida.BX1, com.darkcraft.morvain.Subida.BZ1, 8_000_000);
        StringBuilder sb = new StringBuilder("a pie desde lo alto del ancla: " + z0.celdas() + " celdas;");
        for (String s : v.sitios().keySet()) {
            if (s.startsWith("lado")) continue;
            int[] q = v.sitio(s);
            sb.append(' ').append(s).append(' ').append(z0.alcanza(q[0], q[1], q[2], 2));
        }
        System.out.println(sb);
        int malos = 0, chocan = 0, sinSuelo = 0;
        for (var o : v.esperas()) {
            boolean vuela = o.tipo().equals("gargola") && o.postura() == com.darkcraft.enemigos.Enemigo.DE_PIE;
            if (!vuela && !z0.alcanza(o.x(), o.y(), o.z(), 2.5)) {
                malos++;
                System.out.println("  " + o.grupo() + " sin llegar a pie: " + o.tipo() + " " + o.x() + "," + o.y() + "," + o.z());
            }
            for (int dy = 0; dy < 2; dy++) {
                BlockState st = mundo.en(o.x(), o.y() + dy, o.z());
                if (choca(st)) {
                    chocan++;
                    System.out.println("  " + o.grupo() + " metido en un bloque: " + o.tipo() + " " + o.x() + "," + (o.y() + dy) + "," + o.z() + " " + st);
                }
            }
            if (vuela) continue;
            BlockState abajo = mundo.en(o.x(), o.y() - 1, o.z());
            if (!choca(abajo)) {
                sinSuelo++;
                System.out.println("  " + o.grupo() + " sin suelo: " + o.tipo() + " " + o.x() + "," + o.y() + "," + o.z() + " " + abajo);
            }
        }
        System.out.println("los de la cadena: " + v.esperas().size() + ", sin llegar " + malos + ", metidos en un bloque " + chocan + ", sin suelo " + sinSuelo);
        // el perfil: cada 40 z, lo que se pisa y lo alto del hierro de debajo
        StringBuilder pf = new StringBuilder("perfil (z: pisa / hierro):");
        for (int z = com.darkcraft.morvain.Subida.Z_TORRE; z <= com.darkcraft.morvain.Subida.Z_ANCLA; z += 40) {
            int h = Integer.MIN_VALUE;
            for (int y = (int) v.superficie(z) + 2; y > (int) v.superficie(z) - 30; y--)
                if (com.darkcraft.morvain.Subida.hierro(0, y, z) || com.darkcraft.morvain.Subida.hierro(-1, y, z)) {
                    h = y;
                    break;
                }
            pf.append(' ').append(z).append(':').append(v.superficie(z)).append('/').append(h == Integer.MIN_VALUE ? "-" : String.valueOf(h));
        }
        System.out.println(pf);
    }

    /** El homenaje (parte 7ab): a pie del patio a todos los sitios, con el rastrillo y la reja de la azotea cerrados y abiertos. */
    static void andarHomenaje(WorldGenLevel lvl, com.darkcraft.morvain.Homenaje v) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        for (int vez = 0; vez < 3; vez++) {
            final boolean rastrillo = vez >= 1, reja = vez >= 2;
            java.util.Set<Long> quitar = new java.util.HashSet<>();
            if (rastrillo) for (BlockPos b : com.darkcraft.morvain.Homenaje.rastrillo()) quitar.add(b.asLong());
            if (reja) for (BlockPos b : com.darkcraft.morvain.Homenaje.rejaAzotea()) quitar.add(b.asLong());
            com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
                long q = BlockPos.asLong(x, y, z);
                if (quitar.contains(q)) return aire;
                BlockState s = MUNDO.getOrDefault(q, aire);
                return s.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock || s.getBlock() instanceof net.minecraft.world.level.block.CandleBlock ? aire : s;
            };
            int[] e = v.sitio("patio");
            var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], com.darkcraft.morvain.Homenaje.BX0 - 4, com.darkcraft.morvain.Homenaje.BZ0 - 4,
                    com.darkcraft.morvain.Homenaje.BX1 + 4, com.darkcraft.morvain.Homenaje.BZ1 + 4, 8_000_000);
            StringBuilder sb = new StringBuilder((reja ? "con todo abierto" : rastrillo ? "con el rastrillo abierto" : "cerrado") + ", a pie desde el patio: "
                    + z0.celdas() + " celdas;");
            for (String s : v.sitios().keySet()) {
                if (s.equals("frente")) continue;
                int[] q = v.sitio(s);
                sb.append(' ').append(s).append(' ').append(z0.alcanza(q[0], q[1], q[2], 2));
            }
            System.out.println(sb);
            if (!reja) continue;
            int malos = 0, chocan = 0, sinSuelo = 0;
            for (var o : v.esperas()) {
                if (!z0.alcanza(o.x(), o.y(), o.z(), 2.5)) {
                    malos++;
                    System.out.println("  " + o.grupo() + " sin llegar a pie: " + o.tipo() + " " + o.x() + "," + o.y() + "," + o.z());
                }
                for (int dy = 0; dy < (o.tipo().equals("isolde") || o.tipo().equals("centinela") ? 3 : 2); dy++) {
                    BlockState st = mundo.en(o.x(), o.y() + dy, o.z());
                    if (choca(st)) {
                        chocan++;
                        System.out.println("  " + o.grupo() + " metido en un bloque: " + o.tipo() + " " + o.x() + "," + (o.y() + dy) + "," + o.z() + " " + st);
                    }
                }
                BlockState abajo = mundo.en(o.x(), o.y() - 1, o.z());
                if (!choca(abajo)) {
                    sinSuelo++;
                    System.out.println("  " + o.grupo() + " sin suelo: " + o.tipo() + " " + o.x() + "," + o.y() + "," + o.z() + " " + abajo);
                }
            }
            System.out.println("los de la casa: " + v.esperas().size() + ", sin llegar " + malos + ", metidos en un bloque " + chocan + ", sin suelo " + sinSuelo);
        }
    }

    static void andarCerco(WorldGenLevel lvl, com.darkcraft.morvain.Cerco v, Map<Long, BlockState> antes) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
            BlockState s = MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
            return s.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock || s.getBlock() instanceof net.minecraft.world.level.block.CandleBlock
                    ? aire : s;
        };
        int[] e = v.sitio("entrada");
        var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], com.darkcraft.morvain.Cerco.X0, com.darkcraft.morvain.Cerco.Z0,
                com.darkcraft.morvain.Cerco.X1, com.darkcraft.morvain.Cerco.Z1, 6_000_000);
        StringBuilder sb = new StringBuilder("a pie desde la entrada: " + z0.celdas() + " celdas;");
        java.util.Set<String> mirar = java.util.Set.of("alto", "torre_este", "torre_oeste");
        for (String s : v.sitios().keySet()) {
            if (mirar.contains(s)) continue;
            int[] q = v.sitio(s);
            sb.append(' ').append(s).append(' ').append(z0.alcanza(q[0], q[1], q[2], 2));
        }
        System.out.println(sb);
        int malos = 0, chocan = 0, sinSuelo = 0, enAlto = 0;
        for (var o : v.esperas()) {
            boolean alto = o.grupo().equals("en_alto");
            if (alto) enAlto++;
            else if (!z0.alcanza(o.x(), o.y(), o.z(), 2.0)) {
                malos++;
                System.out.println("  " + o.grupo() + " sin llegar a pie: " + o.x() + "," + o.y() + "," + o.z());
            }
            for (int dy = 0; dy < 2; dy++) {
                BlockState st = mundo.en(o.x(), o.y() + dy, o.z());
                if (choca(st)) {
                    chocan++;
                    System.out.println("  " + o.grupo() + " metido en un bloque: " + o.x() + "," + (o.y() + dy) + "," + o.z() + " " + st);
                }
            }
            BlockState abajo = mundo.en(o.x(), o.y() - 1, o.z());
            if (!choca(abajo)) {
                sinSuelo++;
                System.out.println("  " + o.grupo() + " sin suelo: " + o.x() + "," + o.y() + "," + o.z() + " " + abajo);
            }
        }
        // lo de la ciudadela: el hierro negro de la cadena (y el aro y las puas del ancla) y la piramide, salvo la escalera
        int X = com.darkcraft.morvain.Cerco.CX, Z = com.darkcraft.morvain.Cerco.CZ, P = com.darkcraft.morvain.Cerco.PISO;
        BlockState hierro = com.darkcraft.gen.TorreCupula.hierroNegro();
        int hierros = 0, hierroTocado = 0, piramide = 0, piramideTocada = 0, escalera = 0;
        for (var en : antes.entrySet()) {
            long q = en.getKey();
            int x = BlockPos.getX(q), y = BlockPos.getY(q), z = BlockPos.getZ(q);
            BlockState ahora = MUNDO.getOrDefault(q, aire), era = en.getValue();
            boolean esHierro = era.getBlock() == hierro.getBlock();
            // la piramide de Ciudadela.ancla: lado 10 - (y - suelo) * 3 / 4 hasta suelo + 8, y las puas de las esquinas
            int du = Math.abs(x - X), dw = Math.abs(z - Z), lado = 10 - Math.max(0, y - (P - 1)) * 3 / 4;
            boolean enAncla = y >= P && !era.isAir() && (y <= P + 7 && Math.max(du, dw) <= lado || du == 11 && dw == 11 && y <= P + 6);
            if (!esHierro && !enAncla) continue;
            boolean deLaEscalera = Math.abs(x - X) <= 1 && z - Z >= 4 && z - Z <= 11 && y >= P && y <= P + 9;
            if (esHierro) {
                hierros++;
                if (ahora != era) {
                    hierroTocado++;
                    if (hierroTocado <= 5) System.out.println("  la cadena tocada en " + x + "," + y + "," + z + ": " + ahora);
                }
            } else {
                piramide++;
                if (ahora != era) {
                    if (deLaEscalera) escalera++;
                    else {
                        piramideTocada++;
                        if (piramideTocada <= 5) System.out.println("  el ancla tocada en " + x + "," + y + "," + z + ": " + era + " -> " + ahora);
                    }
                }
            }
        }
        // Aldric (parte 7v): su caja (1,2 x 3,0) en lo alto del ancla, libre (de la cadena tambien)
        for (var o : v.esperas()) {
            if (!o.grupo().equals(com.darkcraft.enemigos.Puestos.JEFE)) continue;
            StringBuilder al = new StringBuilder("Aldric en " + o.x() + "," + o.y() + "," + o.z() + ":");
            int llenos = 0;
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++)
                    for (int dy = 0; dy <= 2; dy++) {
                        BlockState st = mundo.en(o.x() + dx, o.y() + dy, o.z() + dz);
                        if (choca(st)) {
                            llenos++;
                            al.append(' ').append(dx).append('/').append(dy).append('/').append(dz).append('=').append(st.getBlock().getDescriptionId());
                        }
                    }
            System.out.println(al + (llenos == 0 ? " libre" : " (" + llenos + " llenos alrededor)") + "; suelo " + mundo.en(o.x(), o.y() - 1, o.z()));
        }
        // la escalera: de lo alto de cada peldano al siguiente, y desde el patio a la cima
        boolean cima = z0.alcanza(X, P + 8, Z + 3, 1.5);
        System.out.println("Los marcados: " + v.esperas().size() + " (en lo alto de las torres " + enAlto + "), a los que no se llega " + malos
                + ", metidos en un bloque " + chocan + ", sin suelo " + sinSuelo + "; la cima del ancla a pie " + cima + "; la ciudadela: hierro "
                + hierros + " (tocado " + hierroTocado + "), piramide " + piramide + " (tocada " + piramideTocada + ", la escalera " + escalera + ")");
    }

    /**
     * El Puente de la Grieta (parte 7s): se llega a pie desde la entrada (junto al santuario) a cada sitio (menos la campana, lo alto y el
     * fondo, que son para mirar) y a cada penitente; ninguno metido en un bloque y todos con suelo; y que sin el
     * puente solo se llega al otro lado rodeandola por una punta.
     */
    static void andarGrieta(WorldGenLevel lvl, com.darkcraft.morvain.Grieta v) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
            BlockState s = MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
            return s.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock ? aire : s;
        };
        int[] e = v.sitio("entrada");
        int X = com.darkcraft.morvain.Grieta.CX, Z = com.darkcraft.morvain.Grieta.CZ;
        var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], X - 125, Z - 76, X + 108, Z + 92, 6_000_000);
        StringBuilder sb = new StringBuilder("a pie desde la entrada: " + z0.celdas() + " celdas;");
        java.util.Set<String> mirar = java.util.Set.of("campana", "alto", "fondo");
        for (String s : v.sitios().keySet()) {
            if (mirar.contains(s)) continue;
            int[] q = v.sitio(s);
            sb.append(' ').append(s).append(' ').append(z0.alcanza(q[0], q[1], q[2], 2));
        }
        System.out.println(sb);
        int malos = 0, chocan = 0, sinSuelo = 0;
        for (var o : v.esperas()) {
            if (!z0.alcanza(o.x(), o.y(), o.z(), 2.0)) {
                malos++;
                System.out.println("  " + o.grupo() + " sin llegar a pie: " + o.x() + "," + o.y() + "," + o.z());
            }
            for (int dy = 0; dy < 2; dy++) {
                BlockState st = mundo.en(o.x(), o.y() + dy, o.z());
                if (choca(st)) {
                    chocan++;
                    System.out.println("  " + o.grupo() + " metido en un bloque: " + o.x() + "," + (o.y() + dy) + "," + o.z() + " " + st);
                }
            }
            BlockState abajo = mundo.en(o.x(), o.y() - 1, o.z());
            if (!choca(abajo)) {
                sinSuelo++;
                System.out.println("  " + o.grupo() + " sin suelo: " + o.x() + "," + o.y() + "," + o.z() + " " + abajo);
            }
        }
        // sin el tablero del puente, de la orilla del sur no se llega a la del norte (la grieta no se salta)
        int P = com.darkcraft.morvain.Grieta.PISO, S = com.darkcraft.morvain.Grieta.SUELO;
        com.darkcraft.pueblo.gen.Andar.Mundo sinPuente = (x, y, z) -> {
            int u = x - X, w = z - Z;
            if (Math.abs(u) <= 4 && w >= com.darkcraft.morvain.Grieta.PUENTE_V0 && w <= com.darkcraft.morvain.Grieta.PUENTE_V1 && y >= S - 40) return aire;
            return mundo.en(x, y, z);
        };
        int[] ps = v.sitio("puente_sur"), pn = v.sitio("puente_norte"), pu = v.sitio("puerta");
        var z1 = com.darkcraft.pueblo.gen.Andar.desde(sinPuente, ps[0], ps[1], ps[2] + 4, X - 125, Z - 76, X + 108, Z + 92, 6_000_000);
        System.out.println("Penitentes: " + v.esperas().size() + ", a los que no se llega " + malos + ", metidos en un bloque " + chocan + ", sin suelo "
                + sinSuelo + "; sin el puente, de la orilla del sur a la puerta del norte (rodeando la grieta por una punta) " + z1.alcanza(pu[0], pu[1], pu[2], 2)
                + ", celdas " + z1.celdas());
    }

    static void andarObservatorio(WorldGenLevel lvl, com.darkcraft.morvain.Observatorio v) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
            BlockState s = MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
            return s.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock ? aire : s;
        };
        int[] e = v.sitio("entrada");
        int X = com.darkcraft.morvain.Observatorio.CX, Z = com.darkcraft.morvain.Observatorio.CZ;
        var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], X - 50, Z - 50, X + 50, Z + 54, 4_000_000);
        StringBuilder sb = new StringBuilder("a pie desde la entrada (reja bajada): " + z0.celdas() + " celdas;");
        for (String s : v.sitios().keySet()) {
            int[] q = v.sitio(s);
            sb.append(' ').append(s).append(' ').append(z0.alcanza(q[0], q[1], q[2], 2));
        }
        System.out.println(sb);
        int malos = 0, chocan = 0, sinSuelo = 0, k = 0;
        for (var o : v.esperas()) {
            boolean rectora = k++ == 0;
            if (!z0.alcanza(o.x(), o.y(), o.z(), 2.0)) {
                malos++;
                System.out.println("  " + o.grupo() + " sin llegar a pie: " + o.x() + "," + o.y() + "," + o.z());
            }
            for (int dy = 0; dy < (rectora ? 4 : 3); dy++) {
                BlockState st = mundo.en(o.x(), o.y() + dy, o.z());
                if (choca(st)) {
                    chocan++;
                    System.out.println("  " + o.grupo() + " metido en un bloque: " + o.x() + "," + (o.y() + dy) + "," + o.z() + " " + st);
                }
            }
            if (!choca(mundo.en(o.x(), o.y() - 1, o.z()))) {
                sinSuelo++;
                System.out.println("  " + o.grupo() + " sin suelo: " + o.x() + "," + o.y() + "," + o.z() + " " + mundo.en(o.x(), o.y() - 1, o.z()));
            }
        }
        System.out.println("los que esperan: " + v.esperas().size() + ", a los que no se llega " + malos + ", metidos en un bloque " + chocan
                + ", sin suelo " + sinSuelo);
        // las hornacinas de los maestros: de pie y libres (3 de alto)
        int hl = 0;
        for (int[] q : com.darkcraft.morvain.Observatorio.hornacinas())
            if (choca(mundo.en(q[0], q[1] - 1, q[2])) && !choca(mundo.en(q[0], q[1], q[2])) && !choca(mundo.en(q[0], q[1] + 1, q[2]))
                    && !choca(mundo.en(q[0], q[1] + 2, q[2]))) hl++;
        System.out.println("hornacinas libres para los maestros: " + hl + "/4");
        // con la reja de la celda quitada
        for (BlockPos p : MUNDO.keySet().stream().map(BlockPos::of).toList())
            if (p.getX() == X + com.darkcraft.morvain.Observatorio.REJA_U && Math.abs(p.getZ() - Z) <= 1 && MUNDO.get(p.asLong()).is(Blocks.IRON_BARS))
                MUNDO.remove(p.asLong());
        var z1 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], X - 50, Z - 50, X + 50, Z + 54, 4_000_000);
        int[] ce = v.sitio("celda"), at = v.sitio("atril"), ar = v.sitio("arcon");
        System.out.println("con la reja quitada: la celda " + z1.alcanza(ce[0], ce[1], ce[2], 2) + ", el atril " + z1.alcanza(at[0], at[1], at[2], 1.5)
                + ", el arcon " + z1.alcanza(ar[0], ar[1], ar[2], 1.5));
        // con el sello cerrado no se pasa a la rotonda
        for (int u = -1; u <= 1; u++)
            for (int y = com.darkcraft.morvain.Observatorio.PISO; y < com.darkcraft.morvain.Observatorio.PISO + 4; y++)
                MUNDO.put(BlockPos.asLong(X + u, y, Z + com.darkcraft.morvain.Observatorio.SELLO_V), Blocks.PURPLE_STAINED_GLASS.defaultBlockState());
        MUNDO.put(BlockPos.asLong(X, com.darkcraft.morvain.Observatorio.PISO + 4, Z + com.darkcraft.morvain.Observatorio.SELLO_V),
                Blocks.PURPLE_STAINED_GLASS.defaultBlockState());
        var z2 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], X - 50, Z - 50, X + 50, Z + 54, 4_000_000);
        int[] ce2 = v.sitio("centro"), ve = v.sitio("vestibulo");
        System.out.println("con el sello cerrado: el vestibulo " + z2.alcanza(ve[0], ve[1], ve[2], 2) + ", el centro de la rotonda "
                + z2.alcanza(ce2[0], ce2[1], ce2[2], 2));
        for (int u = -1; u <= 1; u++)
            for (int y = com.darkcraft.morvain.Observatorio.PISO; y <= com.darkcraft.morvain.Observatorio.PISO + 4; y++)
                MUNDO.remove(BlockPos.asLong(X + u, y, Z + com.darkcraft.morvain.Observatorio.SELLO_V));
    }

    /** Si un bloque tiene caja de choque (sin la cache de formas del juego, que fuera de el no esta hecha). */
    static boolean choca(BlockState st) {
        return !st.isAir() && !st.getCollisionShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty();
    }

    /** El molino (parte 7f): se llega a pie del camino a la era y a la puerta; con la reja quitada, al arcon de arriba. */
    static void andarMolino(WorldGenLevel lvl, com.darkcraft.morvain.Molino v) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
        int[] c = v.sitio("camino"), era = v.sitio("era"), puerta = v.sitio("puerta_molino"), cofre = v.sitio("cofre_molino");
        int X = com.darkcraft.morvain.Molino.MX, Z = com.darkcraft.morvain.Molino.MZ;
        var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, c[0], c[1], c[2], 990, 4000, 1200, 4140, 900_000);
        System.out.println("a pie desde el camino: " + z0.celdas() + " celdas; la era " + z0.alcanza(era[0], era[1], era[2], 2) + ", la puerta del molino "
                + z0.alcanza(puerta[0], puerta[1], puerta[2], 2) + ", dentro (reja cerrada) " + z0.alcanza(X, com.darkcraft.morvain.Molino.Y + 1, Z + 2, 1.5)
                + ", el arcon " + z0.alcanza(cofre[0], cofre[1], cofre[2], 2));
        if (Boolean.getBoolean("debugMolino"))
            for (int y = com.darkcraft.morvain.Molino.Y; y <= com.darkcraft.morvain.Molino.Y + 4; y++) {
                StringBuilder f = new StringBuilder("  y " + y + ":");
                for (int x = X - 7; x <= X + 1; x++) f.append(' ').append(mundo.en(x, y, Z).getBlock().getDescriptionId().replace("block.minecraft.", ""));
                System.out.println(f);
            }
        for (int[] r : com.darkcraft.morvain.Molino.reja()) MUNDO.remove(BlockPos.asLong(r[0], r[1], r[2]));
        var z1 = com.darkcraft.pueblo.gen.Andar.desde(mundo, c[0], c[1], c[2], 990, 4000, 1200, 4140, 900_000);
        System.out.println("con la reja abierta: dentro " + z1.alcanza(X, com.darkcraft.morvain.Molino.Y + 1, Z + 2, 1.5) + ", el arcon de arriba "
                + z1.alcanza(cofre[0], cofre[1], cofre[2], 2));
        if (Boolean.getBoolean("debugMolino")) {
            StringBuilder f = new StringBuilder("  escalera:");
            for (int y = com.darkcraft.morvain.Molino.Y; y <= com.darkcraft.morvain.Molino.Y + 13; y++)
                f.append(' ').append(y).append('=').append(mundo.en(X + 1, y, Z).getBlock().getDescriptionId().replace("block.minecraft.", ""))
                        .append(z1.pisa(X + 1, y, Z) ? "[pisa]" : "");
            System.out.println(f);
            StringBuilder g = new StringBuilder("  al lado:");
            for (int y = com.darkcraft.morvain.Molino.Y + 8; y <= com.darkcraft.morvain.Molino.Y + 13; y++)
                g.append(' ').append(y).append('=').append(mundo.en(X + 2, y, Z).getBlock().getDescriptionId().replace("block.minecraft.", ""))
                        .append(z1.pisa(X + 2, y, Z) ? "[pisa]" : "");
            System.out.println(g);
        }
    }

    /**
     * La arboleda (parte 7g): de la entrada (junto al santuario) se llega a pie al claro, a la puerta, a lo alto del
     * patibulo (el atril y el arcon), a las tumbas y a cada puesto; cada colgado tiene su soga encima (a 5-9 bloques) y
     * nada le estorba (el cuerpo colgado no toca ningun bloque).
     */
    static void andarArboleda(WorldGenLevel lvl, com.darkcraft.morvain.Arboleda v) {
        BlockState aire = Blocks.AIR.defaultBlockState();
        com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> MUNDO.getOrDefault(BlockPos.asLong(x, y, z), aire);
        int[] e = v.sitio("entrada");
        int G = com.darkcraft.morvain.Arboleda.GX, Z = com.darkcraft.morvain.Arboleda.GZ;
        var z0 = com.darkcraft.pueblo.gen.Andar.desde(mundo, e[0], e[1], e[2], G - 60, Z - 60, -4820, Z + 60, 1_500_000);
        StringBuilder sb = new StringBuilder("a pie desde la entrada: " + z0.celdas() + " celdas;");
        for (String s : new String[]{"arbol", "claro", "puerta", "escalera", "patibulo", "atril", "arcon", "tumbas"}) {
            int[] q = v.sitio(s);
            sb.append(' ').append(s).append(' ').append(z0.alcanza(q[0], q[1], q[2], 2));
        }
        System.out.println(sb);
        int malos = 0, sinSoga = 0, tocan = 0;
        for (var c : v.colgados()) {
            if (!z0.alcanza(c.x(), c.y(), c.z(), 2.5)) {
                malos++;
                System.out.println("  colgado sin llegar a pie: " + c.x() + "," + c.y() + "," + c.z());
            }
            int arriba = -1;
            for (int dy = 1; dy <= 9 && arriba < 0; dy++) {
                BlockState st = mundo.en(c.x(), c.y() + dy, c.z());
                if (st.is(Blocks.CHAIN) || st.getBlock().getClass().getSimpleName().equals("SogaBlock")) arriba = dy;
            }
            if (arriba < 0) sinSoga++;
            else {
                // el cuerpo colgado: de 1,16 sobre el suelo hasta 3,56 (y 0,3 a cada lado del centro, que esta a 0,3 de la soga)
                for (int dy = 1; dy <= 3; dy++)
                    for (int dx = -1; dx <= 1; dx++)
                        for (int dz = -1; dz <= 1; dz++) {
                            if (Math.abs(dx) + Math.abs(dz) > 1) continue;
                            BlockState st = mundo.en(c.x() + dx, c.y() + dy, c.z() + dz);
                            if (!st.isAir() && !st.is(Blocks.CHAIN) && st.blocksMotion()) tocan++;
                        }
            }
        }
        int otrosMal = 0;
        for (var o : v.otros())
            if (!z0.alcanza(o.x(), o.y(), o.z(), 2.0)) {
                otrosMal++;
                System.out.println("  " + o.grupo() + " sin llegar a pie: " + o.x() + "," + o.y() + "," + o.z());
            }
        System.out.println("colgados: " + v.colgados().size() + ", a los que no se llega " + malos + ", sin soga encima " + sinSoga + ", bloques que tocan el cuerpo "
                + tocan + "; los demas: " + v.otros().size() + ", a los que no se llega " + otrosMal);
    
        // el Verdugo (parte 7h): su sitio en el tablado (cabe de pie: 1,2 de ancho y 3,4 de alto), se llega a pie y, delante,
        // el claro donde cae al saltar (sin nada que le corte de lado a lado del patibulo hasta 14 bloques)
        int[] vp = v.sitio("verdugo"), ap = v.sitio("ante_patibulo");
        // (su caja, con el centro en x + 0,5, z + 0,5: de x - 0,1 a x + 1,1; se mira, de mas, cada bloque que toca)
        int choca = 0;
        for (int dy = 0; dy <= 3; dy++)
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++)
                    if (mundo.en(vp[0] + dx, vp[1] + dy, vp[2] + dz).blocksMotion()) choca++;
        int claroLleno = 0;
        for (int z = vp[2] + 4; z <= vp[2] + 14; z++)
            for (int x = vp[0] - 1; x <= vp[0] + 1; x++) {
                int pie = com.darkcraft.morvain.Arboleda.pie(x, z);
                for (int dy = 0; dy < 3; dy++) if (mundo.en(x, pie + dy, z).blocksMotion()) claroLleno++;
            }
        System.out.println("el Verdugo: en " + vp[0] + "," + vp[1] + "," + vp[2] + ", bloques en su caja " + choca + " (tiene que ser 0), se llega a pie "
                + z0.alcanza(vp[0], vp[1], vp[2], 1.5) + "; ante el patibulo (" + ap[0] + "," + ap[1] + "," + ap[2] + ") se llega " + z0.alcanza(ap[0], ap[1], ap[2], 1.5)
                + "; bloques que estorban en el claro de delante " + claroLleno);
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
