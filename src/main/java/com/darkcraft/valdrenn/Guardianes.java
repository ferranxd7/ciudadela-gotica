package com.darkcraft.valdrenn;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Guardian;
import com.darkcraft.enemigos.Puestos;
import com.darkcraft.gen.CastlePlan;
import com.darkcraft.net.Net;
import com.darkcraft.rol.Rol;
import com.darkcraft.story.Logros;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Los guardianes de Valdrenn (parte 4d): mientras vive el suyo, un <b>sello de sombra</b> cierra el paso (bloques de
 * barrera con una cortina de chispas rojas y negras): las puertas del salon del trono (Ser Brandt) y la escalera y el
 * ascensor de la torre del arbol (Vareth). Al caer el guardian el sello se rompe; tras caer el Rey (o Ser Gavran) ya no
 * hay guardian ni sello. Tambien reparte lo que dejan: a cada uno de los que pelearon, el cartel de JEFE ABATIDO con sus
 * ecos y el logro (hazana).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Guardianes {
    private Guardianes() {}

    /** Un sello: el reino, el tipo del guardian (el de los puestos), su puesto y los bloques que cierra. */
    static final class Sello {
        final String sitio, tipo;
        final int puesto;
        final List<BlockPos> bloques;
        final Set<BlockPos> puestos = new HashSet<>();
        boolean cerrado;

        Sello(String sitio, String tipo, int puesto, List<BlockPos> bloques) {
            this.sitio = sitio;
            this.tipo = tipo;
            this.puesto = puesto;
            this.bloques = List.copyOf(bloques);
        }
    }

    private static final Map<String, List<Sello>> SELLOS = new LinkedHashMap<>();
    private static final Map<UUID, Integer> AVISADO = new HashMap<>();

    /** Lo que queda por repartir cuando ha caido un guardian: (tick del servidor, quien, los que pelearon, donde). */
    private record Reparto(int cuando, String tipo, int ecos, String logro, Set<UUID> quienes, double x, double y, double z) {}

    private static final List<Reparto> REPARTOS = new ArrayList<>();

    /** Lo apunta {@link ValdrennEnemigos#apuntar}: los sellos de ese reino (de sus puestos de guardian). */
    static void apuntar(String sitio, CastlePlan plan) {
        List<Sello> l = new ArrayList<>();
        Puestos.Sitio s = Puestos.sitio(sitio);
        if (s == null) return;
        for (int i = 0; i < s.puestos.size(); i++) {
            Puestos.Puesto q = s.puestos.get(i);
            if (q.tipo().equals(ValdrennEnemigos.BRANDT)) l.add(new Sello(sitio, q.tipo(), i, ValdrennEnemigos.selloTrono(plan)));
            else if (q.tipo().equals(ValdrennEnemigos.CORTEZA)) l.add(new Sello(sitio, q.tipo(), i, ValdrennEnemigos.selloArbol(plan)));
        }
        List<Sello> antes = SELLOS.put(sitio, l);
        if (antes != null) for (Sello v : antes) v.puestos.clear();
    }

    /** Si el sello de ese guardian esta en pie ahora: vive su guardian (y su puesto esta en uso). */
    static boolean sellado(MinecraftServer srv, Sello v) {
        Puestos.Sitio s = Puestos.sitio(v.sitio);
        if (s == null || v.puesto >= s.puestos.size()) return false;
        return !Puestos.muerto(srv, v.sitio, v.puesto) && Puestos.enUso(srv.overworld(), v.sitio, v.puesto);
    }

    @SubscribeEvent
    public static void onServidor(TickEvent.ServerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END) return;
        MinecraftServer srv = ev.getServer();
        int ahora = srv.getTickCount();
        for (int i = REPARTOS.size() - 1; i >= 0; i--) {
            Reparto r = REPARTOS.get(i);
            if (ahora < r.cuando()) continue;
            REPARTOS.remove(i);
            repartir(srv, r);
        }
        if (SELLOS.isEmpty() || ahora % 4 != 1) return;
        ServerLevel lvl = srv.overworld();
        boolean revisar = ahora % 20 == 1;
        for (List<Sello> l : SELLOS.values())
            for (Sello v : l) {
                if (v.bloques.isEmpty()) continue;
                BlockPos c = v.bloques.get(0);
                ServerPlayer cerca = null;
                double mejor = 48 * 48;
                for (ServerPlayer p : lvl.players()) {
                    double d = p.distanceToSqr(c.getX() + 0.5, c.getY(), c.getZ() + 0.5);
                    if (d < mejor && !p.isSpectator()) {
                        mejor = d;
                        cerca = p;
                    }
                }
                if (revisar) {
                    boolean debe = cerca != null && sellado(srv, v);
                    if (debe && !v.cerrado) cerrar(lvl, v);
                    // (abierto: tambien si quedan barreras de antes de cerrarse el servidor)
                    else if (!debe && (v.cerrado || quedan(lvl, v))) abrir(lvl, v, false);
                }
                if (!v.cerrado || cerca == null || mejor > 24 * 24) continue;
                cortina(lvl, v);
                // quien se acerca al sello: un aviso corto (como mucho cada 6 s)
                if (mejor < 3.5 * 3.5) {
                    int ultimo = AVISADO.getOrDefault(cerca.getUUID(), -1000);
                    if (ahora - ultimo > 120) {
                        AVISADO.put(cerca.getUUID(), ahora);
                        Net.to(cerca, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.guardian.sello." + v.tipo, 0));
                    }
                }
            }
    }

    /** Si queda alguna barrera del sello puesta (en lo cargado). */
    private static boolean quedan(ServerLevel lvl, Sello v) {
        for (BlockPos b : v.bloques) if (lvl.isLoaded(b) && lvl.getBlockState(b).is(Blocks.BARRIER)) return true;
        return false;
    }

    private static void cerrar(ServerLevel lvl, Sello v) {
        for (BlockPos b : v.bloques) {
            if (!lvl.isLoaded(b)) return;
            var st = lvl.getBlockState(b);
            if (st.isAir() || st.canBeReplaced()) {
                lvl.setBlock(b, Blocks.BARRIER.defaultBlockState(), 3);
                v.puestos.add(b);
            }
        }
        v.cerrado = true;
    }

    /**
     * Quita el sello (con {@code roto}: se rompe con su sonido y su estallido). Quita toda barrera de sus bloques, tambien
     * las que puso antes de cerrarse el servidor (entonces {@code puestos} ya no las tiene): en esos huecos no hay otras.
     */
    private static void abrir(ServerLevel lvl, Sello v, boolean roto) {
        for (BlockPos b : v.bloques)
            if (lvl.isLoaded(b) && lvl.getBlockState(b).is(Blocks.BARRIER)) lvl.setBlock(b, Blocks.AIR.defaultBlockState(), 3);
        v.puestos.clear();
        v.cerrado = false;
        if (roto && !v.bloques.isEmpty()) {
            BlockPos c = v.bloques.get(v.bloques.size() / 2);
            lvl.playSound(null, c.getX() + 0.5, c.getY() + 1, c.getZ() + 0.5, DarkCraftMod.SFX_SELLO_ROMPE.get(), SoundSource.BLOCKS, 2.0f, 1.0f);
            for (BlockPos b : v.bloques)
                lvl.sendParticles(ParticleTypes.SOUL, b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.05);
        }
    }

    private static final DustParticleOptions ROJO = new DustParticleOptions(new Vector3f(0.7f, 0.05f, 0.08f), 1.2f);
    private static final DustParticleOptions NEGRO = new DustParticleOptions(new Vector3f(0.05f, 0.02f, 0.06f), 1.6f);

    /** La cortina del sello: chispas rojas y negras que suben por los bloques cerrados. */
    private static void cortina(ServerLevel lvl, Sello v) {
        for (BlockPos b : v.bloques) {
            if (lvl.random.nextFloat() > 0.45f) continue;
            double x = b.getX() + 0.5, y = b.getY() + lvl.random.nextDouble(), z = b.getZ() + 0.5;
            lvl.sendParticles(lvl.random.nextBoolean() ? ROJO : NEGRO, x, y, z, 1, 0.3, 0.3, 0.3, 0.0);
            if (lvl.random.nextFloat() < 0.15f) lvl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 1, 0.25, 0.2, 0.25, 0.005);
        }
    }

    // ------------------------------------------------------------------ cuando cae un guardian

    static void alCaer(Enemigo e, ServerPlayer quien) {
        if (!(e instanceof Guardian g) || !(e.level() instanceof ServerLevel lvl)) return;
        Set<UUID> quienes = new HashSet<>(g.rivales());
        if (quien != null) quienes.add(quien.getUUID());
        REPARTOS.add(new Reparto(lvl.getServer().getTickCount() + 80, g.tipo(), g.ecos(), g.logro(), quienes, e.getX(), e.getY(), e.getZ()));
        // el sello se rompe en cuanto cae
        List<Sello> l = SELLOS.get(e.sitio());
        if (l != null) for (Sello v : l) if (v.puesto == e.puesto() && v.cerrado) abrir(lvl, v, true);
        DarkCraftMod.LOGGER.info("[DarkCraft] Ha caido {} ({} le plantaron cara)", g.tipo(), quienes.size());
    }

    private static void repartir(MinecraftServer srv, Reparto r) {
        ServerLevel lvl = srv.overworld();
        for (ServerPlayer p : srv.getPlayerList().getPlayers()) {
            boolean cerca = p.level() == lvl && p.distanceToSqr(r.x(), r.y(), r.z()) < 48 * 48;
            if (!r.quienes().contains(p.getUUID()) && !cerca) continue;
            Rol.jefe(p, "entity.darkcraft." + r.tipo(), r.ecos());
            Logros.dar(p, r.logro());
            com.darkcraft.espejo.Espejo.bestia(p, r.tipo());
        }
    }

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent e) {
        SELLOS.clear();
        REPARTOS.clear();
        AVISADO.clear();
    }

    /** Para las pruebas: los bloques de los sellos de ese reino y si estan cerrados. */
    public static String estado(String sitio) {
        List<Sello> l = SELLOS.get(sitio);
        if (l == null) return "sin sellos";
        StringBuilder b = new StringBuilder();
        for (Sello v : l) b.append(' ').append(v.tipo).append(": ").append(v.bloques.size()).append(" bloques, ").append(v.cerrado ? "cerrado" : "abierto")
                .append(" (puestos ").append(v.puestos.size()).append(");");
        return b.toString().trim();
    }

    /** Para las pruebas: revisa ya los sellos de ese reino (como cada segundo con alguien cerca). */
    public static void revisar(ServerLevel lvl, String sitio) {
        List<Sello> l = SELLOS.get(sitio);
        if (l == null) return;
        for (Sello v : l) {
            boolean debe = sellado(lvl.getServer(), v);
            if (debe && !v.cerrado) cerrar(lvl, v);
            else if (!debe && (v.cerrado || quedan(lvl, v))) abrir(lvl, v, false);
        }
    }

    /** Si ese bloque esta cerrado por un sello ahora (pruebas). */
    public static boolean cerrado(ServerLevel lvl, BlockPos b) {
        return lvl.getBlockState(b).is(Blocks.BARRIER);
    }

    /** Los bloques del sello de ese tipo de guardian en ese reino (pruebas). */
    public static List<BlockPos> bloques(String sitio, String tipo) {
        for (Sello v : SELLOS.getOrDefault(sitio, List.of())) if (v.tipo.equals(tipo)) return v.bloques;
        return List.of();
    }

    /** El puesto del guardian de ese tipo en ese reino, o -1. */
    public static int puesto(String sitio, String tipo) {
        for (Sello v : SELLOS.getOrDefault(sitio, List.of())) if (v.tipo.equals(tipo)) return v.puesto;
        return -1;
    }

    /** Lo que se cuenta y se apunta mientras se reparte (pruebas): cuantos repartos quedan por hacer. */
    public static int repartosPendientes() {
        return REPARTOS.size();
    }

    // ------------------------------------------------------------------ /darkcraft valdrenn guardian <brandt|vareth> ...

    private static String tipoDe(String cual) {
        return switch (cual.toLowerCase(java.util.Locale.ROOT)) {
            case "brandt" -> ValdrennEnemigos.BRANDT;
            case "vareth", "corteza" -> ValdrennEnemigos.CORTEZA;
            default -> null;
        };
    }

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comando() {
        var cual = net.minecraft.commands.Commands.argument("cual", com.mojang.brigadier.arguments.StringArgumentType.word())
                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(List.of("brandt", "vareth"), b));
        return net.minecraft.commands.Commands.literal("guardian").then(cual
                .executes(c -> con(c, (p, k, tipo, n) -> decir(c, estado(p.server, k, tipo, n))))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> con(c, (p, k, tipo, n) -> {
                    Puestos.Puesto q = Puestos.sitio(k).puestos.get(n);
                    // delante de el (a 9 bloques, o menos si no cabe), mirandole
                    double fx = -Math.sin(Math.toRadians(q.giro())), fz = Math.cos(Math.toRadians(q.giro()));
                    ServerLevel lvl = p.serverLevel();
                    for (int d = 9; d >= 2; d--) {
                        BlockPos b = BlockPos.containing(q.x() + 0.5 + fx * d, q.y(), q.z() + 0.5 + fz * d);
                        if (!lvl.getBlockState(b).getCollisionShape(lvl, b).isEmpty() || !lvl.getBlockState(b.above()).getCollisionShape(lvl, b.above()).isEmpty())
                            continue;
                        p.teleportTo(lvl, b.getX() + 0.5, b.getY() + 0.05, b.getZ() + 0.5, q.giro() + 180f, 0f);
                        decir(c, estado(p.server, k, tipo, n));
                        return;
                    }
                    p.teleportTo(lvl, q.x() + 0.5 + fx * 9, q.y() + 0.05, q.z() + 0.5 + fz * 9, q.giro() + 180f, 0f);
                })))
                .then(net.minecraft.commands.Commands.literal("vencer").executes(c -> con(c, (p, k, tipo, n) -> {
                    Enemigo e = Puestos.enemigo(k, n);
                    if (e != null) e.kill();
                    else {
                        // (no ha salido: se da por caido y el sello se rompe en cuanto alguien se acerque)
                        Puestos.marcar(p.server, k, n, true);
                        Logros.dar(p, tipo.equals(ValdrennEnemigos.BRANDT) ? "brandt" : "vareth");
                    }
                    revisar(p.server.overworld(), k);
                    decir(c, estado(p.server, k, tipo, n));
                })))
                .then(net.minecraft.commands.Commands.literal("volver").executes(c -> con(c, (p, k, tipo, n) -> {
                    Puestos.marcar(p.server, k, n, false);
                    revisar(p.server.overworld(), k);
                    decir(c, estado(p.server, k, tipo, n));
                }))));
    }

    private interface Accion {
        void hacer(ServerPlayer p, String sitio, String tipo, int puesto);
    }

    private static int con(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> c, Accion f)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String tipo = tipoDe(com.mojang.brigadier.arguments.StringArgumentType.getString(c, "cual"));
        if (tipo == null) {
            c.getSource().sendFailure(net.minecraft.network.chat.Component.literal("Guardianes: brandt o vareth"));
            return 0;
        }
        String k = p.level().dimension() == net.minecraft.world.level.Level.OVERWORLD ? ValdrennEnemigos.reino(p.serverLevel(), p.blockPosition()) : null;
        int n = k == null ? -1 : puesto(k, tipo);
        if (n < 0) {
            c.getSource().sendFailure(net.minecraft.network.chat.Component.literal("No hay ningun Reino de Valdrenn cerca"));
            return 0;
        }
        f.hacer(p, k, tipo, n);
        return 1;
    }

    private static void decir(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> c, String s) {
        c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(s), false);
    }

    private static String estado(MinecraftServer srv, String k, String tipo, int n) {
        Enemigo e = Puestos.enemigo(k, n);
        String quien = tipo.equals(ValdrennEnemigos.BRANDT) ? "Ser Brandt" : "Vareth";
        String como = Puestos.muerto(srv, k, n) ? "caido" : !Puestos.enUso(srv.overworld(), k, n) ? "no sale (ya cayo su jefe)"
                : e == null ? "esperando (sale a 72 bloques)" : "en pie, " + Math.round(e.getHealth()) + " de " + Math.round(e.getMaxHealth())
                + (e instanceof Guardian g && g.enPelea() ? ", peleando" : "");
        return quien + ": " + como + "; " + estado(k);
    }
}
