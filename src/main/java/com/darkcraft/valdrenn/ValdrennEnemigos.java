package com.darkcraft.valdrenn;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Guardia;
import com.darkcraft.enemigos.Puestos;
import com.darkcraft.enemigos.Puestos.Puesto;
import com.darkcraft.gen.CastlePlan;
import com.darkcraft.pueblo.gen.Andar;
import com.darkcraft.rey.ReyEstado;
import com.darkcraft.world.CastleSites;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Los enemigos de cada Reino de Valdrenn (parte 4: "muchisimos enemigos, como en Dark Souls"). Estan en sus puestos
 * ({@link Puestos}: salen al acercarse, no vuelven hasta descansar o morir) y los puestos van en coordenadas del reino
 * (el centro del castillo; +z es el sur, donde esta la puerta): el puente desde la Torre de Ascenso, el camino y la
 * puerta exterior, los arqueros de las murallas y de las torres, el camino mayor, el cuartel y el patio de armas, la
 * puerta interior, el patio de la fuente y la puerta del torreon, el pueblo, el cementerio y los edificios del norte y
 * del este. Cada reino se apunta solo cuando alguien se acerca ({@code valdrenn@x,z}).
 * <p>
 * Tras caer el Rey se van los puestos marcados con {@link #REY} y tras caer Ser Gavran los de {@link #GAVRAN}: el
 * reino queda mas tranquilo (lo pidio el usuario).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class ValdrennEnemigos {
    private ValdrennEnemigos() {}

    public static final String GUARDIA = "guardia", ARQUERO = "arquero";
    /** Lo que se anade al grupo de los puestos que se van al caer el Rey o Ser Gavran. */
    public static final String REY = "~rey", GAVRAN = "~gavran";
    public static final String PREFIJO = "valdrenn@";
    /** Los puestos de los grupos que acaban asi estan donde no se llega andando (a proposito). */
    static final String ALTO = "_en_alto";
    /** Hacia donde miran (giro de Minecraft: 0 al sur, 90 al oeste, 180 al norte, -90 al este). */
    static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;
    static final int Y = CastlePlan.Y0, MURALLA = CastlePlan.Y0 + 26, MURALLA_INT = CastlePlan.Y0 + 36;
    private static final int DE_PIE = Enemigo.DE_PIE, REZA = Enemigo.REZA, TUMBADO = Enemigo.TUMBADO;

    /** Un puesto en coordenadas del reino. {@code quita}: 0 siempre; 1 se va al caer el Rey; 2 al caer Ser Gavran. */
    public record Local(String tipo, int variante, int lx, int y, int lz, float giro, int postura, int ronda, String grupo, int quita) {}

    /** Los puestos de un reino, en coordenadas del reino. {@code sur}: donde acaba el puente de la Torre de Ascenso (lz). */
    public static List<Local> locales(int sur) {
        List<Local> l = new ArrayList<>();
        int E = Guardia.ESPADA, A = Guardia.ALABARDA, B = Guardia.BALLESTA;
        // ---- el puente de la Torre de Ascenso: dos al final, donde se pisa la isla (los primeros que se ven)
        l.add(new Local(GUARDIA, E, -3, Y, sur - 6, SUR, DE_PIE, 0, "puente", 0));
        l.add(new Local(GUARDIA, E, 3, Y, sur - 8, SUR, DE_PIE, 0, "puente", 0));
        // ---- el camino hasta la puerta exterior
        l.add(new Local(GUARDIA, A, 0, Y, Math.min(sur - 20, 252), SUR, DE_PIE, 3, "camino", 1));
        l.add(new Local(GUARDIA, A, -4, Y, 232, SUR, DE_PIE, 0, "puerta", 0));
        l.add(new Local(GUARDIA, A, 4, Y, 232, SUR, DE_PIE, 0, "puerta", 0));
        // ---- arqueros: asomados a las almenas de la muralla exterior y en lo alto de la puerta (en el tejado, que da al
        //      camino: desde ahi ven el camino desde 12 bloques de la puerta hasta el puente; ahi arriba no se sube, a
        //      proposito: hay que pasar corriendo). De pie EN el hueco de la almena (sobre el antepecho), no en el adarve:
        //      desde el adarve el muro les tapa todo lo de abajo. Los huecos caen en las x con x % 4 == 3 o 0.
        for (int s : new int[]{-1, 1}) {
            l.add(new Local(ARQUERO, 0, 36 * s, MURALLA + 1, 220, SUR, DE_PIE, 0, "muralla", 0));
            l.add(new Local(ARQUERO, 0, 60 * s, MURALLA + 1, 220, SUR, DE_PIE, 0, "muralla", 1));
            l.add(new Local(ARQUERO, 0, 10 * s, Y + 33, 228, SUR, DE_PIE, 0, "puerta" + ALTO, 2));
        }
        // ---- el camino mayor, entre las dos puertas
        l.add(new Local(GUARDIA, E, 0, Y, 192, SUR, DE_PIE, 4, "camino_mayor", 1));
        l.add(new Local(GUARDIA, A, -3, Y, 166, SUR, DE_PIE, 0, "camino_mayor", 0));
        l.add(new Local(GUARDIA, B, 3, Y, 172, SUR, DE_PIE, 0, "camino_mayor", 2));
        // ---- el cuartel (puerta al norte) y el patio de armas (entrenando de dos en dos)
        l.add(new Local(GUARDIA, E, 51, Y, 147, NORTE, DE_PIE, 0, "cuartel", 1));
        l.add(new Local(GUARDIA, E, 95, Y, 156, ESTE, DE_PIE, 0, "patio_armas", 1));
        l.add(new Local(GUARDIA, E, 99, Y, 156, OESTE, DE_PIE, 0, "patio_armas", 1));
        l.add(new Local(GUARDIA, A, 95, Y, 166, ESTE, DE_PIE, 0, "patio_armas", 2));
        l.add(new Local(GUARDIA, E, 99, Y, 166, OESTE, DE_PIE, 0, "patio_armas", 2));
        l.add(new Local(GUARDIA, A, 109, Y, 161, OESTE, DE_PIE, 0, "patio_armas", 0));
        // ---- la puerta interior, y arqueros en su muralla y en las almenas de su tejado (ven todo el camino mayor)
        l.add(new Local(GUARDIA, A, -4, Y, 146, SUR, DE_PIE, 0, "puerta_interior", 0));
        l.add(new Local(GUARDIA, A, 4, Y, 146, SUR, DE_PIE, 0, "puerta_interior", 0));
        for (int s : new int[]{-1, 1}) l.add(new Local(ARQUERO, 0, 6 * s, MURALLA_INT + 1, 142, SUR, DE_PIE, 0, "puerta_interior_alta", 1));
        for (int s : new int[]{-1, 1}) {
            l.add(new Local(ARQUERO, 0, 36 * s, MURALLA_INT + 1, 134, SUR, DE_PIE, 0, "muralla_interior", 0));
            l.add(new Local(ARQUERO, 0, 60 * s, MURALLA_INT + 1, 134, SUR, DE_PIE, 0, "muralla_interior", 1));
        }
        // ---- el patio de la fuente y la puerta del torreon
        l.add(new Local(GUARDIA, E, -6, Y, 110, SUR, DE_PIE, 0, "patio", 0));
        l.add(new Local(GUARDIA, E, 6, Y, 110, SUR, DE_PIE, 0, "patio", 1));
        l.add(new Local(GUARDIA, A, 0, Y, 96, SUR, DE_PIE, 3, "patio", 0));
        l.add(new Local(GUARDIA, B, -26, Y, 78, ESTE, DE_PIE, 0, "patio", 2));
        l.add(new Local(GUARDIA, B, 26, Y, 78, OESTE, DE_PIE, 0, "patio", 2));
        l.add(new Local(GUARDIA, E, 0, Y, 58, SUR, DE_PIE, 4, "patio", 1));
        l.add(new Local(GUARDIA, A, -5, Y, 40, SUR, DE_PIE, 0, "torreon", 0));
        l.add(new Local(GUARDIA, A, 5, Y, 40, SUR, DE_PIE, 0, "torreon", 0));
        l.add(new Local(GUARDIA, E, -3, Y, 29, SUR, DE_PIE, 0, "torreon", 0));
        l.add(new Local(GUARDIA, E, 3, Y, 29, SUR, DE_PIE, 0, "torreon", 0));
        // ---- la ronda interior (la calle que rodea la muralla interior): dos que patrullan
        l.add(new Local(GUARDIA, E, -100, Y, 140, ESTE, DE_PIE, 6, "ronda", 1));
        l.add(new Local(GUARDIA, A, 100, Y, 140, OESTE, DE_PIE, 6, "ronda", 1));
        l.add(new Local(GUARDIA, E, 140, Y, -60, NORTE, DE_PIE, 6, "ronda", 1));
        l.add(new Local(GUARDIA, E, -140, Y, 60, SUR, DE_PIE, 6, "ronda", 1));
        // ---- el pueblo: en los cruces de las calles
        int[][] pueblo = {{-67, 188, E, 5}, {-84, 154, A, 0}, {86, 188, B, 0}, {-33, 188, E, 4}, {35, 188, A, 4},
                {-154, 103, E, 5}, {154, 103, A, 5}, {-171, 120, B, 0}, {171, -84, E, 5}, {-120, -171, A, 5}, {120, -171, E, 0}};
        for (int[] p : pueblo) l.add(new Local(GUARDIA, p[2], p[0], Y, p[1], SUR, DE_PIE, p[3], "pueblo", 1));
        // ---- el cementerio: velando de rodillas ante las tumbas, y alguno tirado entre los muertos
        l.add(new Local(GUARDIA, E, -160, Y, -30, OESTE, REZA, 0, "cementerio", 1));
        l.add(new Local(GUARDIA, A, -158, Y, 32, OESTE, REZA, 0, "cementerio", 1));
        l.add(new Local(GUARDIA, E, -176, Y, -40, SUR, TUMBADO, 0, "cementerio", 2));
        l.add(new Local(GUARDIA, E, -174, Y, 44, NORTE, TUMBADO, 0, "cementerio", 2));
        l.add(new Local(ARQUERO, 0, -164, Y, 0, ESTE, REZA, 0, "cementerio", 0));
        // ---- los edificios del este (cuadras y fragua) y del norte
        l.add(new Local(GUARDIA, E, 146, Y, -20, OESTE, DE_PIE, 0, "este", 1));
        l.add(new Local(GUARDIA, B, 146, Y, 40, OESTE, DE_PIE, 0, "este", 2));
        l.add(new Local(GUARDIA, E, -28, Y, -165, SUR, DE_PIE, 0, "norte", 1));
        l.add(new Local(GUARDIA, A, 29, Y, -167, SUR, DE_PIE, 0, "norte", 2));
        return l;
    }

    /** Donde acaba el puente de la Torre de Ascenso, en el reino (lz); sin torre, la plazoleta de delante de la puerta. */
    public static int sur(CastlePlan plan) {
        CastlePlan.Cine c = plan.cine();
        // la torre esta a radio + 13 del centro y su puente llega a radio - 34
        return c.asc() ? (c.ascZ() - plan.cz) - 13 - 34 : CastlePlan.OCT + 40;
    }

    /** Los puestos de un reino, ya en coordenadas del mundo. */
    public static List<Puesto> puestos(CastlePlan plan) {
        List<Puesto> r = new ArrayList<>();
        for (Local p : locales(sur(plan))) {
            String g = p.grupo() + (p.quita() == 1 ? REY : p.quita() == 2 ? GAVRAN : "");
            r.add(new Puesto(p.tipo(), p.variante(), plan.cx + p.lx(), p.y(), plan.cz + p.lz(), p.giro(), p.postura(), p.ronda(), g));
        }
        return r;
    }

    public static String clave(CastlePlan plan) {
        return PREFIJO + plan.cx + "," + plan.cz;
    }

    // ------------------------------------------------------------------ apuntar cada reino cuando alguien se acerca

    @SubscribeEvent
    public static void onServidor(TickEvent.ServerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || ev.getServer().getTickCount() % 40 != 13) return;
        ServerLevel lvl = ev.getServer().overworld();
        for (ServerPlayer p : lvl.players()) {
            int[] s = CastleSites.nearest(lvl.getSeed(), p.getBlockX(), p.getBlockZ(), 1);
            if (s == null || Math.hypot(s[0] - p.getX(), s[1] - p.getZ()) > CastlePlan.FOOTPRINT + 160) continue;
            String k = PREFIJO + s[0] + "," + s[1];
            if (Puestos.apuntado(k)) continue;
            CastlePlan plan = com.darkcraft.story.Story.planAt(lvl, s[0], s[1]);
            if (plan == null) continue;
            apuntar(plan);
        }
    }

    /** Apunta los puestos de ese reino (si ya lo estaban, los vuelve a apuntar). */
    public static void apuntar(CastlePlan plan) {
        String k = clave(plan);
        final int cx = plan.cx, cz = plan.cz;
        String gavran = com.darkcraft.gavran.SalaArbol.de(plan) == null ? null : com.darkcraft.gavran.SalaArbol.de(plan).key();
        Puestos.registrar(k, puestos(plan), l -> l.dimension() == Level.OVERWORLD, (l, p) -> {
            if (p.grupo().endsWith(REY)) return ReyEstado.get(l.getServer()).get(cx + "," + cz) != ReyEstado.MUERTO;
            if (p.grupo().endsWith(GAVRAN)) return gavran == null || ReyEstado.get(l.getServer()).get(gavran) != ReyEstado.MUERTO;
            return true;
        });
        DarkCraftMod.LOGGER.info("[DarkCraft] Valdrenn en {}, {}: {} puestos de enemigos", cx, cz, Puestos.sitio(k).puestos.size());
    }

    /** El reino que hay alrededor de ese sitio (apuntandolo si hace falta), o null. */
    public static String reino(ServerLevel lvl, BlockPos cerca) {
        int[] s = CastleSites.nearest(lvl.getSeed(), cerca.getX(), cerca.getZ(), 1);
        if (s == null) return null;
        String k = PREFIJO + s[0] + "," + s[1];
        if (!Puestos.apuntado(k)) {
            CastlePlan plan = com.darkcraft.story.Story.planAt(lvl, s[0], s[1]);
            if (plan == null) return null;
            apuntar(plan);
        }
        return k;
    }

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent e) {
        // (los reinos se apuntan segun el mundo: con otro mundo, otros)
        Puestos.olvidar(PREFIJO);
    }

    /** Lo apunta el mod al arrancar: los tipos de enemigo de los puestos de Valdrenn. */
    public static void registrar() {
        Puestos.tipo(GUARDIA, DarkCraftMod.GUARDIA);
        Puestos.tipo(ARQUERO, DarkCraftMod.ARQUERO);
    }

    // ------------------------------------------------------------------ comandos: /darkcraft valdrenn enemigos ...

    /** Cuantos quedan en pie en ese reino y cuantos hay fuera. */
    public static String estado(net.minecraft.server.MinecraftServer srv, String k) {
        var s = Puestos.sitio(k);
        if (s == null) return "Valdrenn: sin apuntar";
        int rey = 0, gavran = 0;
        for (Puesto p : s.puestos) {
            if (p.grupo().endsWith(REY)) rey++;
            else if (p.grupo().endsWith(GAVRAN)) gavran++;
        }
        return k + ": " + Puestos.quedan(srv, k) + " de " + s.puestos.size() + " en pie (" + Puestos.fuera(k) + " a la vista); se van "
                + rey + " al caer el Rey y " + gavran + " al caer Ser Gavran";
    }

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comando() {
        return net.minecraft.commands.Commands.literal("enemigos")
                .executes(c -> con(c, (p, k) -> c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(estado(p.server, k)), false)))
                .then(net.minecraft.commands.Commands.literal("volver").executes(c -> con(c, (p, k) -> {
                    Puestos.todos(p.server, k, false);
                    c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(estado(p.server, k)), false);
                })))
                .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> con(c, (p, k) -> {
                    Puestos.todos(p.server, k, true);
                    c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(estado(p.server, k)), false);
                })))
                .then(net.minecraft.commands.Commands.literal("sacar").executes(c -> con(c, (p, k) -> {
                    int n = Puestos.sacarTodos(p.serverLevel(), k).size();
                    c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(n + " fuera; " + estado(p.server, k)), false);
                })))
                .then(net.minecraft.commands.Commands.literal("ir").then(net.minecraft.commands.Commands.argument("grupo", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .executes(c -> con(c, (p, k) -> {
                            String g = com.mojang.brigadier.arguments.StringArgumentType.getString(c, "grupo");
                            for (Puesto q : Puestos.sitio(k).puestos)
                                if (q.grupo().equals(g) || q.grupo().startsWith(g + "~")) {
                                    // a unos pasos, mirandole
                                    double dx = -Math.sin(Math.toRadians(q.giro())) * 7, dz = Math.cos(Math.toRadians(q.giro())) * 7;
                                    p.teleportTo(p.serverLevel(), q.x() + 0.5 + dx, q.y() + 0.1, q.z() + 0.5 + dz, q.giro() + 180f, 0f);
                                    return;
                                }
                            c.getSource().sendFailure(net.minecraft.network.chat.Component.literal("No hay puestos del grupo " + g));
                        }))));
    }

    private static int con(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> c,
                           java.util.function.BiConsumer<ServerPlayer, String> f) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String k = p.level().dimension() == Level.OVERWORLD ? reino(p.serverLevel(), p.blockPosition()) : null;
        if (k == null) {
            c.getSource().sendFailure(net.minecraft.network.chat.Component.literal("No hay ningun Reino de Valdrenn cerca"));
            return 0;
        }
        f.accept(p, k);
        return 1;
    }

    // ------------------------------------------------------------------ comprobarlo sin abrir el juego

    /**
     * Para {@code tools/vista3d} y las pruebas: cada puesto, si se puede estar de pie en el (o a menos de tres bloques:
     * en el juego se busca sitio al lado) y si se llega andando desde donde acaba el puente. Devuelve un resumen.
     */
    public static String comprobar(Andar.Mundo m, CastlePlan plan) {
        int sur = sur(plan);
        int cx = plan.cx, cz = plan.cz;
        StringBuilder mal = new StringBuilder();
        int bien = 0, sinSitio = 0, lejos = 0, enAlto = 0, n = 0;
        Andar.Zona zona = Andar.desde(m, cx, Y, cz + sur - 4, cx - 260, cz - 260, cx + 260, cz + sur + 4, 4_000_000);
        for (Local p : locales(sur)) {
            n++;
            int[] sitio = null;
            for (int r = 0; r <= 3 && sitio == null; r++)
                for (int dx = -r; dx <= r && sitio == null; dx++)
                    for (int dz = -r; dz <= r && sitio == null; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                        if (zona.pisa(cx + p.lx() + dx, p.y() - 1, cz + p.lz() + dz)) sitio = new int[]{dx, dz};
                    }
            if (sitio == null) {
                // no se llega: o no hay donde ponerse, o no se llega andando
                boolean pie = false;
                for (int dy = -3; dy <= 1 && !pie; dy++) {
                    var s = m.en(cx + p.lx(), p.y() - 1 + dy, cz + p.lz());
                    pie = !s.isAir() && m.en(cx + p.lx(), p.y() + dy, cz + p.lz()).isAir() && m.en(cx + p.lx(), p.y() + 1 + dy, cz + p.lz()).isAir();
                }
                if (pie && p.grupo().endsWith(ALTO)) {
                    enAlto++;
                    continue;
                }
                if (pie) lejos++;
                else sinSitio++;
                if (mal.length() < 1500) mal.append(String.format(Locale.ROOT, " [%s %s %d,%d,%d %s]", p.grupo(), p.tipo(), p.lx(), p.y(), p.lz(), pie ? "no se llega" : "sin sitio"));
            } else bien++;
        }
        // lo que ve cada arquero del camino de la puerta (del puente a la puerta interior, cada 4 bloques) a tiro: los de lo
        // alto de las puertas tienen que ver casi todo el suyo; los de las almenas de las murallas, lo que tienen delante
        StringBuilder ven = new StringBuilder();
        for (Local p : locales(sur)) {
            if (!p.tipo().equals(ARQUERO) || p.postura() != DE_PIE) continue;
            int k = 0, total = 0;
            double ox = cx + p.lx() + 0.5, oy = p.y() + 1.74, oz = cz + p.lz() + 0.5;
            for (int z = 146; z <= sur; z += 4) {
                if (Math.abs(z - 217) <= 12 || Math.abs(z - 130) <= 13) continue; // bajo las puertas
                int suelo = Y - 1;
                for (int y = Y + 2; y >= Y - 8; y--)
                    if (solido(m.en(cx, y, cz + z))) {
                        suelo = y;
                        break;
                    }
                double tx = cx + 0.5, ty = suelo + 1 + 1.6, tz = cz + z + 0.5;
                double h = Math.hypot(tx - ox, tz - oz), d3 = Math.sqrt(h * h + (ty - oy) * (ty - oy));
                if (h > 52 || d3 > 64) continue;
                total++;
                if (seVe(m, ox, oy, oz, tx, ty, tz)) k++;
            }
            ven.append(String.format(Locale.ROOT, " %s %d,%d: %d/%d;", p.grupo(), p.lx(), p.lz(), k, total));
        }
        return String.format(Locale.ROOT, "%d puestos (sur %d): %d bien, %d en alto (sin camino, a proposito), %d sin sitio, %d no se llega; recorrido %d celdas%s; del camino a tiro, lo que ve cada arquero:%s",
                n, sur, bien, enAlto, sinSitio, lejos, zona.celdas(), mal, ven);
    }

    private static boolean solido(net.minecraft.world.level.block.state.BlockState s) {
        return !s.isAir() && !s.getCollisionShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, net.minecraft.core.BlockPos.ZERO).isEmpty();
    }

    /** Si la linea de un ojo a otro no pasa por ningun bloque con forma (en pasos de una decima de bloque). */
    private static boolean seVe(Andar.Mundo m, double x0, double y0, double z0, double x1, double y1, double z1) {
        double dx = x1 - x0, dy = y1 - y0, dz = z1 - z0, l = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int pasos = (int) Math.ceil(l / 0.1);
        int ux = Integer.MIN_VALUE, uy = 0, uz = 0;
        for (int i = 1; i < pasos; i++) {
            double t = (double) i / pasos;
            int bx = (int) Math.floor(x0 + dx * t), by = (int) Math.floor(y0 + dy * t), bz = (int) Math.floor(z0 + dz * t);
            if (bx == ux && by == uy && bz == uz) continue;
            ux = bx;
            uy = by;
            uz = bz;
            if (solido(m.en(bx, by, bz))) return false;
        }
        return true;
    }
}
