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
