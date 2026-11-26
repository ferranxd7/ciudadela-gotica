package com.darkcraft.camino;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Espectro;
import com.darkcraft.enemigos.Guardia;
import com.darkcraft.enemigos.Hueco;
import com.darkcraft.enemigos.Puestos;
import com.darkcraft.enemigos.Puestos.Puesto;
import com.darkcraft.gen.Parada;
import com.darkcraft.pueblo.gen.Andar;
import com.darkcraft.world.CaminoNubes;
import com.darkcraft.world.CastleSites;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Las paradas del camino de nubes (parte 5d, respuesta 81 del plan): a lo largo del camino de cada reino, de la Torre de
 * Ascenso al pie del Monte del Colmillo, seis islotes ({@link Parada}): la atalaya rota, la capilla del peregrino (con
 * santuario), la caravana, el arco de la puerta vieja, el cementerio de las nubes y el campamento de la guardia (con
 * santuario). No se generan con el mundo: se levantan chunk a chunk la primera vez que alguien pasa a menos de
 * {@link #CERCA} bloques (asi salen tambien en los mundos de antes), como la fortaleza. Sus enemigos van en sus puestos
 * ({@link Puestos}, sitio {@code camino@x,z} del reino), sus cofres con la tabla {@code chests/camino} y sus nombres en el
 * Espejo ({@link #lugar}).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Paradas {
    private Paradas() {}

    public static final String PREFIJO = "camino@";
    /** Sin paradas en los primeros y los ultimos bloques del camino (la torre, la montana) y lo que hay entre una y otra. */
    static final int DESDE = 220, HASTA = 260, ENTRE = 370;
    /** A cuanto se empieza a levantar. */
    public static final int CERCA = 170;
    static final String GUARDIA = "guardia", ARQUERO = "arquero", PERRO = "perro", ESPECTRO = "espectro", ELITE = "elite", HUECO = "hueco";

    /** Una parada de un reino: su clave ("x,z:k"), su numero y su plano. */
    public record Sitio(String clave, int reinoX, int reinoZ, int k, Parada plan, double s) {
        public String nombre() {
            return Parada.NOMBRES[plan.tipo];
        }
    }

    private static final Map<String, List<Sitio>> CACHE = new LinkedHashMap<>(32, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<Sitio>> e) {
            return size() > 32;
        }
    };

    /** Las paradas del camino del reino de (sx, sz) (vacia si no tiene camino). */
    public static List<Sitio> de(long seed, int sx, int sz) {
        String k = seed + ":" + sx + ":" + sz;
        synchronized (CACHE) {
            List<Sitio> l = CACHE.get(k);
            if (l == null) {
                try {
                    l = calcular(seed, sx, sz);
                } catch (Throwable t) {
                    DarkCraftMod.LOGGER.error("No se pudieron poner las paradas del camino del reino {},{}", sx, sz, t);
                    l = List.of();
                }
                CACHE.put(k, l);
            }
            return l;
        }
    }

    /** Que paradas tocan para un camino de largo {@code largo}: los tipos, en orden. */
    public static int[] tipos(double largo) {
        int n = (int) Math.round((largo - DESDE - HASTA) / ENTRE);
        n = Math.max(2, Math.min(6, n));
        return switch (n) {
            case 2 -> new int[]{Parada.CAPILLA, Parada.CAMPAMENTO};
            case 3 -> new int[]{Parada.ATALAYA, Parada.CAPILLA, Parada.CAMPAMENTO};
            case 4 -> new int[]{Parada.ATALAYA, Parada.CAPILLA, Parada.CARAVANA, Parada.CAMPAMENTO};
            case 5 -> new int[]{Parada.ATALAYA, Parada.CAPILLA, Parada.CARAVANA, Parada.CEMENTERIO, Parada.CAMPAMENTO};
            default -> new int[]{Parada.ATALAYA, Parada.CAPILLA, Parada.CARAVANA, Parada.ARCO, Parada.CEMENTERIO, Parada.CAMPAMENTO};
        };
    }

    private static List<Sitio> calcular(long seed, int sx, int sz) {
        CaminoNubes.Ruta r = CaminoNubes.ruta(seed, sx, sz);
        if (r == null) return List.of();
        return colocar(seed, sx, sz, r.px(), r.pz());
    }

    /** Las paradas de un camino cualquiera (lo usan el juego y {@code tools/vista3d}, con un camino de mentira). */
    public static List<Sitio> colocar(long seed, int sx, int sz, double[] px, double[] pz) {
        CaminoNubes.Ruta r = new CaminoNubes.Ruta(px, pz, new int[0][], new int[0][], 0, 0, 0, 0);
        double largo = r.largo();
        int[] tipos = tipos(largo);
        List<Sitio> l = new ArrayList<>();
        double paso = (largo - DESDE - HASTA) / tipos.length;
        for (int k = 0; k < tipos.length; k++) {
            double s = DESDE + paso * (k + 0.5);
            double[] q = r.punto(s), a = r.punto(Math.max(0, s - 3)), b = r.punto(s + 3);
            long sk = seed ^ (sx * 341873128712L) ^ (sz * 132897987541L) ^ ((k + 1) * 0x9E3779B97F4A7C15L);
            Parada p = new Parada(sk, tipos[k], (int) Math.floor(q[0]), (int) Math.floor(q[1]), b[0] - a[0], b[1] - a[1], k % 2 == 0 ? 1 : -1, px, pz);
            l.add(new Sitio(sx + "," + sz + ":" + k, sx, sz, k, p, s));
        }
        return l;
    }

    /** Las paradas de los caminos que pasan cerca de (x, z) (a menos de {@code radio} de su centro). */
    public static List<Sitio> cerca(long seed, double x, double z, double radio) {
        List<Sitio> l = new ArrayList<>();
        int alc = com.darkcraft.fortaleza.Fortalezas.DIST + CaminoNubes.RODEO + 300;
        int rx0 = Math.floorDiv((int) x - alc, CastleSites.REGION), rx1 = Math.floorDiv((int) x + alc, CastleSites.REGION);
        int rz0 = Math.floorDiv((int) z - alc, CastleSites.REGION), rz1 = Math.floorDiv((int) z + alc, CastleSites.REGION);
        for (int rx = rx0; rx <= rx1; rx++)
            for (int rz = rz0; rz <= rz1; rz++) {
                int[] s = CastleSites.site(seed, rx, rz);
                if (s == null || Math.hypot(s[0] - x, s[1] - z) > alc) continue;
                for (Sitio p : de(seed, s[0], s[1]))
                    if (Math.hypot(p.plan().cx - x, p.plan().cz - z) <= radio) l.add(p);
            }
        return l;
    }

    // ------------------------------------------------------------------ lo que se guarda: los chunks hechos de cada una

    public static final class Datos extends SavedData {
        public static final String ID = "darkcraft_paradas";
        final Map<String, Set<Long>> hechos = new HashMap<>();
        final Set<String> completas = new HashSet<>();

        static Datos load(CompoundTag t) {
            Datos d = new Datos();
            CompoundTag h = t.getCompound("hechos");
            for (String k : h.getAllKeys()) {
                Set<Long> s = new HashSet<>();
                for (long l : h.getLongArray(k)) s.add(l);
                d.hechos.put(k, s);
            }
            CompoundTag c = t.getCompound("completas");
            for (String k : c.getAllKeys()) d.completas.add(k);
            return d;
        }

        @Override
        public CompoundTag save(CompoundTag t) {
            CompoundTag h = new CompoundTag();
            hechos.forEach((k, v) -> h.put(k, new LongArrayTag(v.stream().mapToLong(Long::longValue).toArray())));
            t.put("hechos", h);
            CompoundTag c = new CompoundTag();
            for (String k : completas) c.putBoolean(k, true);
            t.put("completas", c);
            return t;
        }

        public static Datos get(MinecraftServer srv) {
            return srv.overworld().getDataStorage().computeIfAbsent(Datos::load, Datos::new, ID);
        }

        public boolean completa(String clave) {
            return completas.contains(clave);
        }
    }

    // ------------------------------------------------------------------ levantarlas

    private static final ArrayDeque<Object[]> COLA = new ArrayDeque<>();
    private static final Set<String> EN_COLA = new HashSet<>();
    private static int descanso;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.side != LogicalSide.SERVER || !(e.player instanceof ServerPlayer p)) return;
        if (p.tickCount % 40 != 29 || p.level().dimension() != Level.OVERWORLD || p.getY() < 180) return;
        ServerLevel lvl = p.serverLevel();
        for (Sitio s : cerca(lvl.getSeed(), p.getX(), p.getZ(), CERCA)) encolar(lvl, s);
    }

    /** Pone en la cola los chunks que falten de esa parada (los que esten cargados). */
    public static void encolar(ServerLevel lvl, Sitio s) {
        Datos d = Datos.get(lvl.getServer());
        if (d.completa(s.clave())) {
            apuntar(lvl, s.reinoX(), s.reinoZ());
            return;
        }
        Set<Long> h = d.hechos.computeIfAbsent(s.clave(), k -> new HashSet<>());
        for (long k : s.plan().chunks()) {
            if (h.contains(k)) continue;
            String c = s.clave() + ":" + k;
            if (EN_COLA.contains(c)) continue;
            int cx = (int) (k >> 32), cz = (int) k;
            if (!lvl.getChunkSource().hasChunk(cx, cz)) continue;
            COLA.add(new Object[]{s, k});
            EN_COLA.add(c);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        construirUno(e.getServer());
    }

    private static void construirUno(MinecraftServer srv) {
        if (descanso > 0) {
            descanso--;
            return;
        }
        Object[] t = COLA.poll();
        if (t == null) return;
        Sitio s = (Sitio) t[0];
        long k = (long) t[1];
        EN_COLA.remove(s.clave() + ":" + k);
        ServerLevel lvl = srv.overworld();
        int cx = (int) (k >> 32), cz = (int) k;
        if (!lvl.getChunkSource().hasChunk(cx, cz)) return;
        Datos d = Datos.get(srv);
        Set<Long> h = d.hechos.computeIfAbsent(s.clave(), q -> new HashSet<>());
        if (h.contains(k)) return;
        long t0 = System.nanoTime();
        s.plan().construir(lvl, cx, cz, RandomSource.create(lvl.getSeed() ^ k ^ s.clave().hashCode()));
        h.add(k);
        santuario(lvl, s, cx, cz);
        if (h.containsAll(s.plan().chunks()) && d.completas.add(s.clave())) {
            DarkCraftMod.LOGGER.info("[DarkCraft] Parada del camino {} ({}) levantada en {},{}", s.clave(), s.nombre(), s.plan().cx, s.plan().cz);
            apuntar(lvl, s.reinoX(), s.reinoZ());
        }
        d.setDirty();
        long ms = (System.nanoTime() - t0) / 1_000_000;
        descanso = (int) Math.min(40, ms / 50);
    }

    /** Levanta ya todo lo que este cargado de esa parada (comandos y pruebas). Devuelve los chunks hechos. */
    public static int construirYa(ServerLevel lvl, Sitio s) {
        encolar(lvl, s);
        int n = 0;
        while (!COLA.isEmpty() && n < 200) {
            descanso = 0;
            construirUno(lvl.getServer());
            n++;
        }
        descanso = 0;
        return n;
    }

    /** El santuario de la parada (si tiene), en cuanto esta hecho su chunk: con su nombre. */
    private static void santuario(ServerLevel lvl, Sitio s, int cx, int cz) {
        int[] q = s.plan().santuario();
        if (q == null || (q[0] >> 4) != cx || (q[2] >> 4) != cz) return;
        com.darkcraft.santuario.Santuarios.ponerEn(lvl, "c" + s.clave(), new BlockPos(q[0], q[1], q[2]),
                "darkcraft.santuario.parada_" + s.nombre());
    }

    /** Para las pruebas: olvida lo hecho de esa parada (se vuelve a levantar). */
    public static void olvidar(MinecraftServer srv, Sitio s) {
        Datos d = Datos.get(srv);
        d.hechos.remove(s.clave());
        d.completas.remove(s.clave());
        d.setDirty();
    }

    // ------------------------------------------------------------------ el nombre en el Espejo

    /** El nombre del sitio si se esta en una parada (clave de idioma), o null. */
    public static String lugar(ServerPlayer p) {
        if (p.level().dimension() != Level.OVERWORLD || p.getY() < 230 || p.getY() > 300) return null;
        for (Sitio s : cerca(p.serverLevel().getSeed(), p.getX(), p.getZ(), 24)) {
            Parada q = s.plan();
            if (Math.hypot(q.cx + 0.5 - p.getX(), q.cz + 0.5 - p.getZ()) <= q.r + 2) return "darkcraft.mapa.lugar.parada_" + s.nombre();
        }
        return null;
    }

    // ------------------------------------------------------------------ sus enemigos

    public static String clave(int sx, int sz) {
        return PREFIJO + sx + "," + sz;
    }

    /** Apunta los puestos de las paradas del camino de ese reino (si no lo estaban). */
    public static void apuntar(ServerLevel lvl, int sx, int sz) {
        String k = clave(sx, sz);
        if (Puestos.apuntado(k)) return;
        List<Sitio> ps = de(lvl.getSeed(), sx, sz);
        List<Puesto> l = new ArrayList<>();
        for (Sitio s : ps) puestos(l, s);
        Puestos.registrar(k, l, w -> w.dimension() == Level.OVERWORLD, (w, q) -> {
            String g = q.grupo();
            int i = g.indexOf(':');
            return i < 0 || Datos.get(w.getServer()).completa(sx + "," + sz + ":" + g.substring(i + 1));
        });
        DarkCraftMod.LOGGER.info("[DarkCraft] Camino de nubes del reino {},{}: {} paradas, {} puestos de enemigos", sx, sz, ps.size(), l.size());
    }

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent e) {
        Puestos.olvidar(PREFIJO);
        COLA.clear();
        EN_COLA.clear();
    }

    /** Lo apunta el mod al arrancar: el tipo que no tenian los de Valdrenn y la fortaleza (los huecos de Brumavela). */
    public static void registrar() {
        Puestos.tipo(HUECO, DarkCraftMod.HUECO);
    }

    /** Los puestos de una parada (grupo "nombre:k": salen cuando esa parada esta en pie). */
    public static void puestos(List<Puesto> l, Sitio s) {
        Parada p = s.plan();
        String g = s.nombre() + ":" + s.k();
        int y = Parada.Y + 1;
        // hacia donde llega quien viene de Valdrenn (u negativo): los de cada parada miran hacia alli
        float mira = (float) Math.toDegrees(-Math.atan2(-p.ux, -p.uz));
        switch (p.tipo) {
            case Parada.ATALAYA -> {
                // (la plataforma de arriba, a Y + 11: el arquero; en la puerta, la ballesta)
                int[] pt = p.en(2, 9);
                l.add(new Puesto(ARQUERO, 0, pt[0], Parada.Y + 11, pt[1], mira, Enemigo.DE_PIE, 0, g));
                int[] d = p.en(2, 4.5);
                l.add(new Puesto(GUARDIA, Guardia.BALLESTA, d[0], y, d[1], mira, Enemigo.DE_PIE, 1, g));
                int[] a = p.en(-6, -4), b = p.en(7, -4);
                l.add(new Puesto(GUARDIA, Guardia.ESPADA, a[0], y, a[1], mira, Enemigo.DE_PIE, 4, g));
                l.add(new Puesto(GUARDIA, Guardia.ESPADA, b[0], y, b[1], mira, Enemigo.DE_PIE, 4, g));
            }
            case Parada.CAPILLA -> {
                // la capilla del santuario: sin enemigos
            }
            case Parada.CARAVANA -> {
                int[] a = p.en(-4, 11), b = p.en(-6, 10), c = p.en(5, 13), d = p.en(3, 5), e = p.en(-2, 4);
                l.add(new Puesto(PERRO, 0, a[0], y, a[1], mira, Enemigo.REZA, 0, g));
                l.add(new Puesto(PERRO, 0, b[0], y, b[1], mira, Enemigo.REZA, 0, g));
                l.add(new Puesto(PERRO, 0, c[0], y, c[1], mira, Enemigo.TUMBADO, 0, g));
                l.add(new Puesto(HUECO, Hueco.HORCA, d[0], y, d[1], mira, Enemigo.TUMBADO, 0, g));
                l.add(new Puesto(HUECO, Hueco.HOZ, e[0], y, e[1], mira, Enemigo.DE_PIE, 2, g));
            }
            case Parada.ARCO -> {
                // bajo el arco, a los lados del camino, y detras
                int[] a = p.en(-3, 2.2), b = p.en(-3, -2.2), c = p.en(7, 4), d = p.en(9, -5);
                l.add(new Puesto(GUARDIA, Guardia.ALABARDA, a[0], y, a[1], mira, Enemigo.DE_PIE, 0, g));
                l.add(new Puesto(GUARDIA, Guardia.ALABARDA, b[0], y, b[1], mira, Enemigo.DE_PIE, 0, g));
                l.add(new Puesto(GUARDIA, Guardia.ESPADA, c[0], y, c[1], mira, Enemigo.DE_PIE, 3, g));
                l.add(new Puesto(GUARDIA, Guardia.BALLESTA, d[0], y, d[1], mira, Enemigo.DE_PIE, 1, g));
            }
            case Parada.CEMENTERIO -> {
                int[] a = p.en(5, 10), b = p.en(-5, 13), c = p.en(-8, 8), d = p.en(8, 14), e = p.en(2, 3.5);
                l.add(new Puesto(ESPECTRO, Espectro.GARRAS, a[0], y, a[1], mira, Enemigo.REZA, 0, g));
                l.add(new Puesto(ESPECTRO, Espectro.ESTOQUE, b[0], y, b[1], mira, Enemigo.DE_PIE, 4, g));
                l.add(new Puesto(PERRO, 0, c[0], y, c[1], mira, Enemigo.TUMBADO, 0, g));
                l.add(new Puesto(PERRO, 0, d[0], y, d[1], mira, Enemigo.TUMBADO, 0, g));
                l.add(new Puesto(HUECO, Hueco.ANTORCHA, e[0], y, e[1], mira, Enemigo.TUMBADO, 0, g));
            }
            default -> {
                // el campamento: el capitan junto a la hoguera, dos caballeros, una ballesta y un perro
                int[] a = p.en(6, 4.5), b = p.en(1, 6), c = p.en(10, 6), d = p.en(12, -4), e = p.en(6, 9);
                l.add(new Puesto(ELITE, 0, a[0], y, a[1], mira, Enemigo.DE_PIE, 2, g));
                l.add(new Puesto(GUARDIA, Guardia.ESPADA, b[0], y, b[1], mira, Enemigo.DE_PIE, 2, g));
                l.add(new Puesto(GUARDIA, Guardia.ESPADA, c[0], y, c[1], mira, Enemigo.DE_PIE, 2, g));
                l.add(new Puesto(GUARDIA, Guardia.BALLESTA, d[0], y, d[1], mira, Enemigo.DE_PIE, 1, g));
                l.add(new Puesto(PERRO, 0, e[0], y, e[1], mira, Enemigo.REZA, 0, g));
            }
        }
    }

    /** Cuantas paradas estan en pie y cuantos enemigos quedan (para el comando). */
    public static String resumen(ServerLevel lvl, int sx, int sz) {
        Datos d = Datos.get(lvl.getServer());
        StringBuilder b = new StringBuilder("Camino de nubes del reino " + sx + "," + sz + ":");
        for (Sitio s : de(lvl.getSeed(), sx, sz))
            b.append(String.format(Locale.ROOT, " %d %s en %d,%d (a %d del principio)%s;", s.k(), s.nombre(), s.plan().cx, s.plan().cz,
                    Math.round(s.s()), d.completa(s.clave()) ? " en pie" : ""));
        String k = clave(sx, sz);
        if (Puestos.apuntado(k)) b.append(" enemigos ").append(Puestos.quedan(lvl.getServer(), k)).append(" de ").append(Puestos.sitio(k).puestos.size())
                .append(" (").append(Puestos.fuera(k)).append(" a la vista)");
        return b.toString();
    }

    // ------------------------------------------------------------------ /darkcraft camino ...

    /** El reino del camino donde esta el jugador (el de la parada mas cercana; si no, el reino mas cercano). */
    static int[] reino(ServerPlayer p) {
        long seed = p.serverLevel().getSeed();
        Sitio mejor = null;
        double md = Double.MAX_VALUE;
        for (Sitio s : cerca(seed, p.getX(), p.getZ(), 3000)) {
            double d = Math.hypot(s.plan().cx - p.getX(), s.plan().cz - p.getZ());
            if (d < md) {
                md = d;
                mejor = s;
            }
        }
        if (mejor != null) return new int[]{mejor.reinoX(), mejor.reinoZ()};
        return CastleSites.nearest(seed, p.getBlockX(), p.getBlockZ(), 1);
    }

    private interface Accion {
        int hacer(ServerPlayer p, int[] r, List<Sitio> ps) throws com.mojang.brigadier.exceptions.CommandSyntaxException;
    }

    private static int con(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> c, Accion a)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        int[] r = p.level().dimension() == Level.OVERWORLD ? reino(p) : null;
        List<Sitio> ps = r == null ? List.of() : de(p.serverLevel().getSeed(), r[0], r[1]);
        if (ps.isEmpty()) {
            c.getSource().sendFailure(net.minecraft.network.chat.Component.literal("No hay ningun camino de nubes cerca"));
            return 0;
        }
        return a.hacer(p, r, ps);
    }

    private static Sitio num(List<Sitio> ps, int n) {
        return ps.get(Math.max(0, Math.min(ps.size() - 1, n)));
    }

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comando() {
        var N = com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, 5);
        return net.minecraft.commands.Commands.literal("camino")
                .executes(c -> con(c, (p, r, ps) -> {
                    c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(resumen(p.serverLevel(), r[0], r[1])), false);
                    return ps.size();
                }))
                .then(net.minecraft.commands.Commands.literal("ir").then(net.minecraft.commands.Commands.argument("n", N).executes(c -> con(c, (p, r, ps) -> {
                    // al llegar a la parada por el camino, mirandola
                    Parada q = num(ps, com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "n")).plan();
                    int[] a = q.en(-(q.r + 3), 0);
                    float giro = (float) Math.toDegrees(-Math.atan2(q.ux, q.uz));
                    p.teleportTo(p.serverLevel(), a[0] + 0.5, Parada.Y + 1, a[1] + 0.5, giro, 8f);
                    return 1;
                }))))
                .then(net.minecraft.commands.Commands.literal("levantar").then(net.minecraft.commands.Commands.argument("n", N).executes(c -> con(c, (p, r, ps) -> {
                    Sitio s = num(ps, com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "n"));
                    for (long k : s.plan().chunks()) p.serverLevel().getChunk((int) (k >> 32), (int) k);
                    int n = construirYa(p.serverLevel(), s);
                    c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(s.nombre() + ": " + n + " chunks"), false);
                    return n;
                }))))
                .then(net.minecraft.commands.Commands.literal("enemigos")
                        .then(net.minecraft.commands.Commands.literal("volver").executes(c -> con(c, (p, r, ps) -> {
                            apuntar(p.serverLevel(), r[0], r[1]);
                            Puestos.todos(p.server, clave(r[0], r[1]), false);
                            return 1;
                        })))
                        .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> con(c, (p, r, ps) -> {
                            apuntar(p.serverLevel(), r[0], r[1]);
                            Puestos.todos(p.server, clave(r[0], r[1]), true);
                            return 1;
                        }))));
    }

    // ------------------------------------------------------------------ comprobarlo sin abrir el juego

    /**
     * Para {@code tools/vista3d} y las pruebas: por cada parada, si se cruza a pie de un lado al otro por el camino, si se
     * llega al cofre y al santuario, y si cada puesto se pisa y se llega (y que ninguno queda a menos de 14 del santuario).
     */
    public static String comprobar(Andar.Mundo m, Sitio s) {
        Parada p = s.plan();
        // (de un lado al otro del islote por su camino enlosado: el camino de nubes de fuera puede no estar hecho aun)
        int[] a = p.porCamino(-(p.r - 3)), b = p.porCamino(p.r - 3);
        Andar.Zona z = Andar.desde(m, a[0], Parada.Y + 1, a[1], p.cx - p.r - 16, p.cz - p.r - 16, p.cx + p.r + 16, p.cz + p.r + 16, 400_000);
        boolean cruza = z.alcanza(b[0], Parada.Y + 1, b[1], 2);
        int[] cf = p.cofre(), sa = p.santuario();
        boolean cofre = cf == null || z.alcanza(cf[0], cf[1], cf[2], 2.5);
        boolean sant = sa == null || z.alcanza(sa[0], sa[1], sa[2], 2.5);
        List<Puesto> l = new ArrayList<>();
        puestos(l, s);
        int bien = 0, cercaSant = 0;
        StringBuilder mal = new StringBuilder();
        for (Puesto q : l) {
            boolean hay = false;
            for (int r = 0; r <= 3 && !hay; r++)
                for (int dx = -r; dx <= r && !hay; dx++)
                    for (int dz = -r; dz <= r && !hay; dz++)
                        if (Math.max(Math.abs(dx), Math.abs(dz)) == r && z.pisa(q.x() + dx, q.y() - 1, q.z() + dz)) hay = true;
            if (hay) bien++;
            else if (mal.length() < 400) mal.append(String.format(Locale.ROOT, " [%s %d,%d,%d]", q.tipo(), q.x() - p.cx, q.y(), q.z() - p.cz));
            if (sa != null && Math.hypot(q.x() - sa[0], q.z() - sa[2]) < 14) cercaSant++;
        }
        return String.format(Locale.ROOT, "%s (%s): se cruza %s, cofre %s, santuario %s, puestos %d de %d bien%s%s (%d celdas)",
                s.clave(), s.nombre(), cruza, cofre, sant, bien, l.size(), mal, cercaSant > 0 ? ", " + cercaSant + " A MENOS DE 14 DEL SANTUARIO" : "", z.celdas());
    }
}
