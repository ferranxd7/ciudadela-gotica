package com.darkcraft.camino;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Jinete;
import com.darkcraft.enemigos.JineteAnims;
import com.darkcraft.fortaleza.Fortalezas;
import com.darkcraft.fortaleza.Perdidos;
import com.darkcraft.world.CaminoNubes;
import com.darkcraft.world.CastleSites;
import com.darkcraft.world.NubesGenerador;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Ser Lucan, el Jinete Nocturno (parte 7x): por donde va cada noche y cuando esta de verdad en el mundo. Hay uno en el camino
 * de nubes de cada reino de Valdrenn (su clave, la del reino: "x,z").
 * <ul>
 * <li>Sale de la Torre de Ascenso al caer la noche (hora 13000) y cabalga hacia la Fortaleza de Hueso; al amanecer (23000)
 * se deshace sin haber llegado: a esa hora ha hecho {@link #FRACCION} del camino. Por donde va lo dice la hora
 * ({@link #andado}): no hace falta guardar nada.</li>
 * <li>Sale (la entidad) cuando un jugador del mundo de nubes esta a {@link #SALE} bloques de por donde va y ese sitio esta
 * cargado (se alza de la niebla: {@code APARECE}, el cuerno se oye de lejos); se recoge (sin guardarse) cuando ya no queda nadie
 * a {@link #SE_VA} y no esta peleando. De dia no sale.</li>
 * <li>El que cae no vuelve nunca (SavedData {@code darkcraft_jinetes}: los caminos con su jinete caido). Al caer deja la carta
 * que llevaba (el libro perdido 31) a cada uno de los que le plantaron cara (y a los que esten a 48).</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Jinetes {
    private Jinetes() {}

    /** Lo cerca de por donde va que tiene que estar alguien para que salga, y lo lejos para que se recoja. */
    public static final double SALE = 128, SE_VA = 176;
    /** Lo que ha hecho del camino al amanecer (no llega nunca a la escalera). */
    public static final double FRACCION = 0.93;
    /** El libro perdido que lleva: la carta del Rey. */
    public static final int CARTA = 31;
    private static final Map<String, Jinete> FUERA = new HashMap<>();
    /**
     * Para la prueba del cliente: no sale solo (las demas pruebas van por el camino de nubes a cualquier hora y no lo
     * esperan; su tramo lo vuelve a dejar salir).
     */
    public static boolean quietos;

    public static String clave(int x, int z) {
        return x + "," + z;
    }

    /** El de ese camino que esta fuera (o null). */
    public static Jinete fuera(String reino) {
        Jinete j = FUERA.get(reino);
        return j != null && !j.isRemoved() ? j : null;
    }

    /** Si es de noche en ese mundo (de 13000 a 23000). */
    public static boolean noche(Level l) {
        long t = Math.floorMod(l.getDayTime(), 24000L);
        return t >= 13000 && t < 23000;
    }

    /** Lo que ha cabalgado esta noche por un camino (de 0 al caer la noche a {@link #FRACCION} del camino al amanecer). */
    public static double andado(Level l, CaminoNubes.Ruta r) {
        long t = Math.floorMod(l.getDayTime(), 24000L);
        return Mth.clamp((t - 13000) / 10000.0, 0, 1) * FRACCION * r.largo();
    }

    // ------------------------------------------------------------------ cada tick

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        MinecraftServer srv = e.getServer();
        ServerLevel ow = srv.overworld();
        if (ow == null) return;
        tick(ow, srv.getTickCount() % 10 == 7);
    }

    /** Los que estan fuera, a lo suyo (al amanecer se deshacen); cada medio segundo, los que tienen que salir o recogerse. */
    public static void tick(ServerLevel ow, boolean cada) {
        FUERA.values().removeIf(j -> j == null || j.isRemoved());
        boolean noche = noche(ow);
        for (Jinete j : new ArrayList<>(FUERA.values())) {
            if (!j.isAlive()) continue;
            if (!noche) {
                j.amanecer();
                continue;
            }
            if (!cada) continue;
            boolean alguien = false;
            for (ServerPlayer p : ow.players())
                if (!p.isSpectator() && Math.hypot(p.getX() - j.getX(), p.getZ() - j.getZ()) < SE_VA) alguien = true;
            if ((!alguien && !j.enPelea()) || !ow.isPositionEntityTicking(j.blockPosition())) {
                FUERA.remove(j.reino());
                j.discard();
                DarkCraftMod.LOGGER.info("[DarkCraft] el Jinete Nocturno del camino {} se aleja (lo andado {})", j.reino(), Math.round(j.s()));
            }
        }
        if (!cada || !noche || quietos || !NubesGenerador.es(ow)) return;
        Datos d = Datos.get(ow.getServer());
        long seed = ow.getSeed();
        for (ServerPlayer p : ow.players()) {
            if (p.isSpectator() || p.getY() < 200) continue;
            for (int[] reino : reinos(seed, p.getX(), p.getZ())) {
                String k = clave(reino[0], reino[1]);
                if (fuera(k) != null || d.caidos.contains(k)) continue;
                CaminoNubes.Ruta r = CaminoNubes.ruta(seed, reino[0], reino[1]);
                if (r == null) continue;
                double s = andado(ow, r);
                double[] q = r.punto(s);
                if (Math.hypot(p.getX() - q[0], p.getZ() - q[1]) > SALE) continue;
                if (!ow.isPositionEntityTicking(BlockPos.containing(q[0], NubesGenerador.SUELO, q[1]))) continue;
                crear(ow, k, r, s, true);
            }
        }
    }

    /** Los reinos cuyo camino puede pasar cerca de (x, z). */
    public static List<int[]> reinos(long seed, double x, double z) {
        List<int[]> l = new ArrayList<>();
        int alc = Fortalezas.DIST + CaminoNubes.RODEO + 300;
        int rx0 = Math.floorDiv((int) x - alc, CastleSites.REGION), rx1 = Math.floorDiv((int) x + alc, CastleSites.REGION);
        int rz0 = Math.floorDiv((int) z - alc, CastleSites.REGION), rz1 = Math.floorDiv((int) z + alc, CastleSites.REGION);
        for (int rx = rx0; rx <= rx1; rx++)
            for (int rz = rz0; rz <= rz1; rz++) {
                int[] s = CastleSites.site(seed, rx, rz);
                if (s == null) continue;
                CaminoNubes.Ruta r = CaminoNubes.ruta(seed, s[0], s[1]);
                if (r == null) continue;
                if (x < r.x0() - SALE || x > r.x1() + SALE || z < r.z0() - SALE || z > r.z1() + SALE) continue;
                l.add(s);
            }
        return l;
    }

    /**
     * Saca al jinete de un camino a {@code s} bloques del principio, a la altura del camino y mirando hacia la fortaleza.
     * {@code aparece}: se alza de la niebla (con el cuerno, que se oye de lejos). Devuelve el que sale (o null).
     */
    public static Jinete crear(ServerLevel l, String reino, CaminoNubes.Ruta r, double s, boolean aparece) {
        Jinete viejo = fuera(reino);
        if (viejo != null) viejo.discard();
        Jinete j = DarkCraftMod.JINETE.get().create(l);
        if (j == null) return null;
        double[] p = r.punto(s);
        double[] t = tangente(r, s);
        float giro = (float) (Mth.atan2(t[1], t[0]) * Mth.RAD_TO_DEG) - 90f;
        double y = NubesGenerador.SUELO;
        j.camino(r, reino, s);
        j.pies(y);
        j.moveTo(p[0], y, p[1], giro, 0);
        j.setYHeadRot(giro);
        j.setYBodyRot(giro);
        l.addFreshEntity(j);
        FUERA.put(reino, j);
        if (aparece) {
            j.empezar(JineteAnims.APARECE);
            l.playSound(null, p[0], y, p[1], DarkCraftMod.SFX_JINETE_CUERNO.get(), net.minecraft.sounds.SoundSource.HOSTILE, 8.0f, 0.95f);
        }
        DarkCraftMod.LOGGER.info("[DarkCraft] sale el Jinete Nocturno en el camino {} ({} {} {}, lo andado {} de {})", reino, Math.round(p[0]),
                Math.round(y), Math.round(p[1]), Math.round(s), Math.round(r.largo()));
        return j;
    }

    /** Ha caido (lo llama {@link Jinete#die}): ya no vuelve a ese camino; la carta, a los que le plantaron cara. */
    public static void caido(ServerLevel sl, Jinete j) {
        String k = j.reino();
        if (k != null) {
            Datos d = Datos.get(sl.getServer());
            d.caidos.add(k);
            d.setDirty();
            FUERA.remove(k);
        }
        Set<UUID> quienes = new HashSet<>(j.rivales());
        int n = 0;
        for (ServerPlayer p : sl.getServer().getPlayerList().getPlayers()) {
            boolean cerca = p.level() == sl && p.distanceToSqr(j) < 48 * 48;
            if (!quienes.contains(p.getUUID()) && !cerca) continue;
            if (p.isSpectator()) continue;
            com.darkcraft.rol.Equipo.dar(p, Perdidos.libro(CARTA));
            Perdidos.encontrado(p, CARTA);
            n++;
        }
        DarkCraftMod.LOGGER.info("[DarkCraft] ha caido el Jinete Nocturno del camino {} (la carta a {})", k, n);
    }

    // ------------------------------------------------------------------ el camino

    /** Lo lejos que esta (x, z) del camino (en horizontal). */
    public static double distancia(CaminoNubes.Ruta r, double x, double z) {
        double m = Double.MAX_VALUE;
        double[] px = r.px(), pz = r.pz();
        for (int i = 0; i + 1 < px.length; i++) m = Math.min(m, segmento(px[i], pz[i], px[i + 1], pz[i + 1], x, z)[0]);
        return m;
    }

    /** Lo andado del camino hasta el punto del camino mas cercano a (x, z). */
    public static double donde(CaminoNubes.Ruta r, double x, double z) {
        double m = Double.MAX_VALUE, mejor = 0, s = 0;
        double[] px = r.px(), pz = r.pz();
        for (int i = 0; i + 1 < px.length; i++) {
            double l = Math.hypot(px[i + 1] - px[i], pz[i + 1] - pz[i]);
            double[] q = segmento(px[i], pz[i], px[i + 1], pz[i + 1], x, z);
            if (q[0] < m) {
                m = q[0];
                mejor = s + q[1] * l;
            }
            s += l;
        }
        return mejor;
    }

    /** Hacia donde va el camino a {@code s} bloques del principio (unitario, {x, z}). */
    public static double[] tangente(CaminoNubes.Ruta r, double s) {
        double largo = r.largo();
        double[] a = r.punto(Mth.clamp(s - 1.5, 0, largo)), b = r.punto(Mth.clamp(s + 1.5, 0, largo));
        double dx = b[0] - a[0], dz = b[1] - a[1], l = Math.hypot(dx, dz);
        if (l < 1e-6) return new double[]{0, 1};
        return new double[]{dx / l, dz / l};
    }

    /** {distancia, fraccion del segmento} del punto (x, z) al segmento a-b. */
    private static double[] segmento(double ax, double az, double bx, double bz, double x, double z) {
        double dx = bx - ax, dz = bz - az, l2 = dx * dx + dz * dz;
        double t = l2 < 1e-9 ? 0 : Mth.clamp(((x - ax) * dx + (z - az) * dz) / l2, 0, 1);
        return new double[]{Math.hypot(ax + dx * t - x, az + dz * t - z), t};
    }

    // ------------------------------------------------------------------ comandos y pruebas

    /** Como esta el jinete de cada camino que pasa cerca de un sitio. */
    public static String estado(ServerLevel ow, double x, double z) {
        Datos d = Datos.get(ow.getServer());
        StringBuilder b = new StringBuilder();
        long t = Math.floorMod(ow.getDayTime(), 24000L);
        b.append(noche(ow) ? "de noche" : "de dia").append(" (hora ").append(t).append(")");
        for (int[] reino : reinos(ow.getSeed(), x, z)) {
            String k = clave(reino[0], reino[1]);
            CaminoNubes.Ruta r = CaminoNubes.ruta(ow.getSeed(), reino[0], reino[1]);
            if (r == null) continue;
            Jinete j = fuera(k);
            double[] q = r.punto(andado(ow, r));
            b.append("; camino ").append(k).append(" (").append(Math.round(r.largo())).append(" bloques): ")
                    .append(d.caidos.contains(k) ? "caido" : j != null ? "fuera en " + j.blockPosition().toShortString() + (j.enPelea() ? " peleando" : "")
                            + (j.amanece() ? " amaneciendo" : "") : noche(ow) ? "cabalgando por " + Math.round(q[0]) + " " + Math.round(q[1]) : "no sale de dia");
        }
        return b.toString();
    }

    /** El reino del camino mas cercano a un jugador (o null). */
    public static int[] reino(ServerPlayer sp) {
        long seed = sp.serverLevel().getSeed();
        int[] mejor = null;
        double md = Double.MAX_VALUE;
        for (int[] s : reinos(seed, sp.getX(), sp.getZ())) {
            CaminoNubes.Ruta r = CaminoNubes.ruta(seed, s[0], s[1]);
            if (r == null) continue;
            double dd = distancia(r, sp.getX(), sp.getZ());
            if (dd < md) {
                md = dd;
                mejor = s;
            }
        }
        return mejor;
    }

    /**
     * Para las pruebas: un sitio del camino lejos de las paradas (y de la torre y la montana), donde no hay mas enemigos: lo
     * andado de ese sitio.
     */
    public static double libre(long seed, int sx, int sz, CaminoNubes.Ruta r) {
        List<Paradas.Sitio> ps = Paradas.de(seed, sx, sz);
        double mejor = r.largo() * 0.3, md = -1;
        for (double s = 300; s <= r.largo() * 0.6; s += 10) {
            double m = Double.MAX_VALUE;
            for (Paradas.Sitio p : ps) m = Math.min(m, Math.abs(p.s() - s));
            if (m > md) {
                md = m;
                mejor = s;
            }
        }
        return mejor;
    }

    /** Para las pruebas: la hora de la noche a la que lleva andado {@code s} de su camino. */
    public static long hora(CaminoNubes.Ruta r, double s) {
        return 13000 + Math.round(Mth.clamp(s / (FRACCION * r.largo()), 0, 1) * 10000);
    }

    /** Vuelve a la vida el de un camino (comando y pruebas). */
    public static void revivir(MinecraftServer srv, String reino) {
        Datos d = Datos.get(srv);
        d.caidos.remove(reino);
        d.setDirty();
    }

    public static boolean caido(MinecraftServer srv, String reino) {
        return Datos.get(srv).caidos.contains(reino);
    }

    /** Recoge al que este fuera (comando y pruebas). */
    public static void recoger(String reino) {
        Jinete j = fuera(reino);
        if (j != null) j.discard();
        FUERA.remove(reino);
    }

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent e) {
        FUERA.clear();
    }

    /**
     * {@code /darkcraft jinete [ir | aqui | vencer | derribar | volver | noche | dia]}: como esta el del camino de alrededor;
     * llevar ante el (a 30 bloques por delante de por donde va, mirandole), sacarlo delante de quien lo pide, vencerle, que su
     * caballo tropiece, que vuelva (si habia caido), y poner la noche (cuando sale) o el dia.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comando() {
        return net.minecraft.commands.Commands.literal("jinete")
                .executes(c -> decir(c, estadoDe(c)))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> {
                    if (c.getSource().getEntity() instanceof ServerPlayer sp) ir(sp);
                    return decir(c, estadoDe(c));
                }))
                .then(net.minecraft.commands.Commands.literal("aqui").executes(c -> {
                    if (c.getSource().getEntity() instanceof ServerPlayer sp) aqui(sp);
                    return decir(c, estadoDe(c));
                }))
                .then(net.minecraft.commands.Commands.literal("vencer").executes(c -> {
                    if (c.getSource().getEntity() instanceof ServerPlayer sp) {
                        int[] r = reino(sp);
                        if (r != null) {
                            Jinete j = fuera(clave(r[0], r[1]));
                            if (j != null) j.vencer();
                            else {
                                Datos d = Datos.get(sp.server);
                                d.caidos.add(clave(r[0], r[1]));
                                d.setDirty();
                                com.darkcraft.story.Logros.dar(sp, Jinete.TIPO);
                            }
                        }
                    }
                    return decir(c, estadoDe(c));
                }))
                .then(net.minecraft.commands.Commands.literal("derribar").executes(c -> {
                    if (c.getSource().getEntity() instanceof ServerPlayer sp) {
                        int[] r = reino(sp);
                        Jinete j = r != null ? fuera(clave(r[0], r[1])) : null;
                        if (j != null) j.derribar();
                    }
                    return decir(c, estadoDe(c));
                }))
                .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                    if (c.getSource().getEntity() instanceof ServerPlayer sp) {
                        int[] r = reino(sp);
                        if (r != null) {
                            recoger(clave(r[0], r[1]));
                            revivir(sp.server, clave(r[0], r[1]));
                        }
                    }
                    return decir(c, estadoDe(c));
                }))
                .then(net.minecraft.commands.Commands.literal("noche").executes(c -> {
                    c.getSource().getServer().overworld().setDayTime(13500);
                    return decir(c, estadoDe(c));
                }))
                .then(net.minecraft.commands.Commands.literal("dia").executes(c -> {
                    c.getSource().getServer().overworld().setDayTime(1000);
                    return decir(c, estadoDe(c));
                }));
    }

    private static String estadoDe(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> c) {
        ServerLevel ow = c.getSource().getServer().overworld();
        var pos = c.getSource().getPosition();
        return "Jinete Nocturno: " + estado(ow, pos.x, pos.z);
    }

    /** Lleva a un jugador por delante de por donde va el jinete de su camino (a 30 bloques, de cara a el, en el camino). */
    public static void ir(ServerPlayer sp) {
        ServerLevel ow = sp.server.overworld();
        int[] reino = reino(sp);
        if (reino == null) return;
        CaminoNubes.Ruta r = CaminoNubes.ruta(ow.getSeed(), reino[0], reino[1]);
        if (r == null) return;
        Jinete j = fuera(clave(reino[0], reino[1]));
        double s = j != null ? donde(r, j.getX(), j.getZ()) : andado(ow, r);
        double s2 = Math.min(r.largo() - 5, s + 30);
        double[] p = r.punto(s2), q = r.punto(s);
        float giro = (float) (Mth.atan2(q[1] - p[1], q[0] - p[0]) * Mth.RAD_TO_DEG) - 90f;
        sp.teleportTo(ow, p[0], NubesGenerador.SUELO + 0.05, p[1], giro, 5f);
    }

    /** Saca al jinete del camino de alrededor en el camino, 18 bloques mas atras (hacia la torre) que quien lo pide y de cara a el. */
    public static Jinete aqui(ServerPlayer sp) {
        ServerLevel ow = sp.server.overworld();
        int[] reino = reino(sp);
        if (reino == null) return null;
        CaminoNubes.Ruta r = CaminoNubes.ruta(ow.getSeed(), reino[0], reino[1]);
        if (r == null) return null;
        String k = clave(reino[0], reino[1]);
        revivir(sp.server, k);
        double s0 = donde(r, sp.getX(), sp.getZ());
        double s = Math.max(5, s0 - 18);
        return crear(ow, k, r, s, true);
    }

    private static int decir(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> c, String s) {
        c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(s), false);
        return 1;
    }

    // ------------------------------------------------------------------ lo que se guarda

    public static final class Datos extends SavedData {
        public static final String ID = "darkcraft_jinetes";
        final Set<String> caidos = new HashSet<>();

        public static Datos get(MinecraftServer srv) {
            return srv.overworld().getDataStorage().computeIfAbsent(Datos::load, Datos::new, ID);
        }

        static Datos load(CompoundTag t) {
            Datos d = new Datos();
            ListTag l = t.getList("caidos", 8);
            for (int i = 0; i < l.size(); i++) d.caidos.add(l.getString(i));
            return d;
        }

        @Override
        public CompoundTag save(CompoundTag t) {
            ListTag l = new ListTag();
            for (String k : caidos) l.add(StringTag.valueOf(k));
            t.put("caidos", l);
            return t;
        }
    }
}
