package com.darkcraft.mision;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.morvain.Morvain;
import com.darkcraft.net.Net;
import com.darkcraft.personajes.Viajero;
import com.darkcraft.rol.Ficha;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Las misiones de Morvain (parte 7ag, respuestas 95 y 117 del plan): quien las da ({@code personajes.Viajero}), lo que cada jugador
 * lleva hecho de cada una (en su ficha, {@code Ficha.mision}: marcas de bits) y lo que sabe su cliente (la lista de las que conoce,
 * con adonde ir ahora: el diario de la Historia, la brujula y el mapa). {@link Oswin}, la campana del peregrino (7ag), {@link Hedda},
 * la bufanda roja (7ah), y {@link Ylva}, la luz del muelle (7an).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Misiones {
    private Misiones() {}

    /** Todas, en el orden del menu. */
    public static final String[] TODAS = {Oswin.ID, Hedda.ID, Ylva.ID};

    /** Lo ultimo que se le mando a cada uno (si cambia —coge o suelta la campana o la bufanda—, se le vuelve a mandar). */
    private static final Map<UUID, Long> MANDADO = new HashMap<>();

    /** Cada segundo, desde {@code Morvain.onTick}. */
    public static void tick(ServerLevel m) {
        Oswin.tick(m);
        Hedda.tick(m);
        Ylva.tick(m);
        for (ServerPlayer p : m.players()) {
            Long antes = MANDADO.get(p.getUUID());
            if (antes == null || antes != huella(p)) enviar(p);
        }
    }

    /** Lo que lleva de cada una (con lo que lleva encima), para no mandarlo dos veces igual (cada una cabe en 21 bits). */
    private static long huella(ServerPlayer p) {
        return (oswin(p) & 0x1FFFFFL) | ((heda(p) & 0x1FFFFFL) << 21) | ((ylva(p) & 0x1FFFFFL) << 42);
    }

    private static int oswin(ServerPlayer p) {
        return Oswin.marcas(p) | (Oswin.lleva(p) ? Oswin.LLEVA : 0);
    }

    private static int heda(ServerPlayer p) {
        return Hedda.marcas(p) | (Hedda.lleva(p) ? Hedda.LLEVA : 0);
    }

    private static int ylva(ServerPlayer p) {
        return Ylva.marcas(p) | (Ylva.lleva(p) ? Ylva.LLEVA : 0);
    }

    /** Le manda las misiones que conoce (las de marcas 0 no; tambien si lleva lo que se busca sin conocer a quien lo busca). */
    public static void enviar(ServerPlayer p) {
        List<Net.MisionE> l = new ArrayList<>();
        int k = oswin(p);
        if (k != 0) l.add(entrada(Oswin.ID, k, Oswin.objetivo(p)));
        int h = heda(p);
        if (h != 0) l.add(entrada(Hedda.ID, h, Hedda.objetivo(p)));
        int y = ylva(p);
        if (y != 0) l.add(entrada(Ylva.ID, y, Ylva.objetivo(p)));
        Net.to(p, new Net.Misiones(l));
        MANDADO.put(p.getUUID(), huella(p));
    }

    private static Net.MisionE entrada(String id, int k, BlockPos o) {
        return new Net.MisionE(id, k, o != null, o == null ? 0 : o.getX(), o == null ? 0 : o.getY(), o == null ? 0 : o.getZ());
    }

    @SubscribeEvent
    public static void onEntrar(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) enviar(sp);
    }

    @SubscribeEvent
    public static void onCambio(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) enviar(sp);
    }

    @SubscribeEvent
    public static void onSalir(PlayerEvent.PlayerLoggedOutEvent e) {
        MANDADO.remove(e.getEntity().getUUID());
        Hedda.olvidarJugador(e.getEntity().getUUID());
    }

    /** La Bendicion de Hedda (parte 7ah): un golpe que deja muy tocado al que la lleva. */
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public static void onDano(net.minecraftforge.event.entity.living.LivingDamageEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp && !e.isCanceled() && e.getAmount() > 0f) Hedda.golpe(sp, e.getAmount());
    }

    /** ...y en su tick siguiente, la cura. */
    @SubscribeEvent
    public static void onTickJugador(net.minecraftforge.event.TickEvent.PlayerTickEvent e) {
        if (e.phase == net.minecraftforge.event.TickEvent.Phase.END && e.player instanceof ServerPlayer sp) Hedda.curar(sp);
    }

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent e) {
        MANDADO.clear();
    }

    // ------------------------------------------------------------------ lo que se habla con quien da cada una

    /** Por donde empieza a hablar quien da la mision. */
    public static int inicio(ServerPlayer sp, Viajero v) {
        return switch (v.quien()) {
            case OSWIN -> Oswin.inicio(sp);
            case HEDDA -> Hedda.inicio(sp, v);
            case YLVA -> Ylva.inicio(sp);
        };
    }

    /** Lo que se le responde (aceptar, el precio o aquella noche, rechazar, devolver, quedarse, la verdad, la mentira, callar). */
    public static void accion(ServerPlayer sp, Viajero v, String accion) {
        switch (v.quien()) {
            case OSWIN -> Oswin.accion(sp, v, accion);
            case HEDDA -> Hedda.accion(sp, v, accion);
            case YLVA -> Ylva.accion(sp, v, accion);
        }
    }

    /** Si a este jugador le toca ver (y hablar con) quien da la mision: cada uno lleva la suya (parte 7ah). */
    public static boolean visible(ServerPlayer sp, Viajero v) {
        return switch (v.quien()) {
            case HEDDA -> Hedda.visible(sp, v);
            case YLVA -> Ylva.visible(sp);
            default -> true;
        };
    }

    /** Para las pruebas y el comando: como va la mision de cada uno. */
    public static String resumen(ServerPlayer sp) {
        int k = Oswin.marcas(sp);
        StringBuilder b = new StringBuilder("oswin ").append(k).append(" [");
        String[] n = {"conocida", "aceptada", "rechazada", "precio", "tomada", "devuelta", "quedada"};
        for (int i = 0; i < n.length; i++) if ((k & (1 << i)) != 0) b.append(' ').append(n[i]);
        b.append(" ]").append(Oswin.lleva(sp) ? " lleva la campana" : "");
        BlockPos o = Oswin.objetivo(sp);
        if (o != null) b.append(", ir a ").append(o.toShortString());
        return b.append("; ").append(resumenHedda(sp)).append("; ").append(resumenYlva(sp)).toString();
    }

    /** Como va la de Ylva. */
    public static String resumenYlva(ServerPlayer sp) {
        int k = Ylva.marcas(sp);
        StringBuilder b = new StringBuilder("ylva ").append(k).append(" [");
        String[] n = {"conocida", "aceptada", "rechazada", "noche", "tomada", "calma", "dada", "quedada"};
        for (int i = 0; i < n.length; i++) if ((k & (1 << i)) != 0) b.append(' ').append(n[i]);
        b.append(" ]").append(Ylva.conLuz(sp) ? " lleva la luz de Ylva" : "").append(Ylva.lleva(sp) ? " lleva el farol de Brann" : "");
        BlockPos o = Ylva.objetivo(sp);
        if (o != null) b.append(", ir a ").append(o.toShortString());
        return b.toString();
    }

    /** Como va la de Hedda. */
    public static String resumenHedda(ServerPlayer sp) {
        int k = Hedda.marcas(sp);
        StringBuilder b = new StringBuilder("hedda ").append(k).append(" [");
        String[] n = {"conocida", "aceptada", "rechazada", "noche", "tomada", "verdad", "mentira", "callada", "lanza"};
        for (int i = 0; i < n.length; i++) if ((k & (1 << i)) != 0) b.append(' ').append(n[i]);
        b.append(" ]").append(Hedda.lleva(sp) ? " lleva la bufanda" : "");
        BlockPos o = Hedda.objetivo(sp);
        if (o != null) b.append(", ir a ").append(o.toShortString());
        return b.toString();
    }

    /** Olvida lo hecho en una mision (y le quita lo que le dio: la campana, la bufanda, la lanza, los talismanes de la bolsa). */
    public static void olvidar(ServerPlayer sp, String id) {
        Ficha.mision(sp, id, 0);
        var inv = sp.getInventory();
        java.util.List<net.minecraft.world.item.Item> fuera = Oswin.ID.equals(id)
                ? java.util.List.of(DarkCraftMod.CAMPANA_PEREGRINO.get(), DarkCraftMod.CAMPANA_DIFUNTOS.get())
                : Hedda.ID.equals(id) ? java.util.List.of(DarkCraftMod.BUFANDA_ROJA.get(), DarkCraftMod.LANZA_TAM.get(),
                DarkCraftMod.TALISMANES.get(com.darkcraft.rol.Talisman.Clase.MADRE).get(), DarkCraftMod.TALISMANES.get(com.darkcraft.rol.Talisman.Clase.BUFANDA).get())
                : Ylva.ID.equals(id) ? java.util.List.of(DarkCraftMod.FAROL_YLVA.get(), DarkCraftMod.FAROL_BALLENERO.get(),
                DarkCraftMod.TALISMANES.get(com.darkcraft.rol.Talisman.Clase.YLVA).get(), DarkCraftMod.TALISMANES.get(com.darkcraft.rol.Talisman.Clase.BALLENERO).get())
                : java.util.List.of();
        for (int i = 0; i < inv.getContainerSize(); i++)
            if (fuera.contains(inv.getItem(i).getItem())) inv.setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
        com.darkcraft.dialogo.Conversaciones.olvidar(sp, id);
        enviar(sp);
    }

    // ------------------------------------------------------------------ los comandos

    private static int decir(CommandSourceStack c, String s) {
        c.sendSuccess(() -> Component.literal(s), false);
        return 1;
    }

    /** /darkcraft misiones [olvidar | marcar &lt;n&gt;] (sin mas: como van). */
    public static LiteralArgumentBuilder<CommandSourceStack> comando() {
        return Commands.literal("misiones")
                .executes(c -> decir(c.getSource(), resumen(c.getSource().getPlayerOrException())))
                .then(Commands.literal("olvidar").executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    for (String id : TODAS) olvidar(sp, id);
                    return decir(c.getSource(), resumen(sp));
                }))
                .then(Commands.literal("marcar").then(Commands.argument("n", IntegerArgumentType.integer(0, 127)).executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    Ficha.mision(sp, Oswin.ID, IntegerArgumentType.getInteger(c, "n"));
                    enviar(sp);
                    return decir(c.getSource(), resumen(sp));
                })))
                .then(Commands.literal("hedda").then(Commands.argument("n", IntegerArgumentType.integer(0, 511)).executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    Ficha.mision(sp, Hedda.ID, IntegerArgumentType.getInteger(c, "n"));
                    enviar(sp);
                    return decir(c.getSource(), resumen(sp));
                })))
                .then(Commands.literal("ylva").then(Commands.argument("n", IntegerArgumentType.integer(0, 255)).executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    Ficha.mision(sp, Ylva.ID, IntegerArgumentType.getInteger(c, "n"));
                    enviar(sp);
                    return decir(c.getSource(), resumenYlva(sp));
                })));
    }

    /** /darkcraft ylva [ir | farol | poner | olvidar] (sin mas: donde estan Ylva y los faroles). */
    public static LiteralArgumentBuilder<CommandSourceStack> comandoYlva() {
        return Commands.literal("ylva")
                .executes(c -> {
                    ServerLevel m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    var srv = m.getServer();
                    return decir(c.getSource(), "Ylva " + Ylva.sitio().toShortString() + (Morvain.hecho(srv, "ylva") ? " (puesta)" : "")
                            + ", el farol de Brann " + Ylva.farolArena().toShortString() + (Morvain.hecho(srv, "farol_brann") ? " (puesto)" : "")
                            + (com.darkcraft.morvain.Costillar.enPaz(srv) ? " (Brann ha caido)" : " (Brann sigue en pie)")
                            + ", en el muelle " + Ylva.farolMuelle().toShortString() + (Morvain.hecho(srv, "farol_muelle") ? " (puesto)" : ""));
                })
                .then(Commands.literal("ir").executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    ServerLevel m = Morvain.nivel(sp.server);
                    if (m == null) return 0;
                    llevarYlva(sp, m, false);
                    return decir(c.getSource(), resumenYlva(sp));
                }))
                .then(Commands.literal("farol").executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    ServerLevel m = Morvain.nivel(sp.server);
                    if (m == null) return 0;
                    llevarYlva(sp, m, true);
                    return decir(c.getSource(), resumenYlva(sp));
                }))
                .then(Commands.literal("poner").executes(c -> {
                    ServerLevel m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    if (Ylva.buscarYlva(m) == null) Ylva.ponerYlva(m);
                    if (Ylva.buscarFarol(m, 0) == null) Ylva.ponerFarol(m, 0);
                    if (Ylva.buscarFarol(m, 1) == null) Ylva.ponerFarol(m, 1);
                    return decir(c.getSource(), "Ylva y los dos faroles de Brann puestos");
                }))
                .then(Commands.literal("olvidar").executes(c -> {
                    ServerLevel m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    Ylva.olvidar(m);
                    return decir(c.getSource(), "Ylva y los faroles quitados");
                }));
    }

    /**
     * Lleva al jugador a Morvain: delante de Ylva (en el espigon, dos bloques al este y uno al sur, mirandola) o a la arena del Costillar,
     * delante del farol de Brann (dos bloques al sur, mirandolo).
     */
    public static void llevarYlva(ServerPlayer sp, ServerLevel m, boolean farol) {
        BlockPos a, de;
        if (farol) {
            BlockPos f = Ylva.farolArena();
            m.getChunk(f.getX() >> 4, f.getZ() >> 4);
            int y = m.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, f.getX(), f.getZ() + 2);
            de = new BlockPos(f.getX(), y, f.getZ() + 2);
            a = f;
        } else {
            BlockPos s = Ylva.sitio();
            m.getChunk(s.getX() >> 4, s.getZ() >> 4);
            de = new BlockPos(s.getX() + 2, s.getY(), s.getZ() + 1);
            a = s.above();
        }
        double dx = a.getX() + 0.5 - (de.getX() + 0.5), dz = a.getZ() + 0.5 - (de.getZ() + 0.5);
        double dy = a.getY() + 0.3 - (de.getY() + 1.62);
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz)), pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
        sp.teleportTo(m, de.getX() + 0.5, de.getY(), de.getZ() + 0.5, yaw, pitch);
    }

    /** /darkcraft hedda [ir | tam | poner | olvidar] (sin mas: donde estan Hedda y Tam). */
    public static LiteralArgumentBuilder<CommandSourceStack> comandoHedda() {
        return Commands.literal("hedda")
                .executes(c -> {
                    ServerLevel m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    var srv = m.getServer();
                    return decir(c.getSource(), "Hedda " + Hedda.sitio().toShortString() + (Morvain.hecho(srv, "hedda") ? " (puesta)" : "")
                            + ", Tam " + Hedda.tam().toShortString() + (Morvain.hecho(srv, "tam") ? " (puesto)" : "")
                            + ", de rodillas " + Hedda.luto().toShortString() + (Morvain.hecho(srv, "hedda_luto") ? " (puesta)" : ""));
                })
                .then(Commands.literal("ir").executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    ServerLevel m = Morvain.nivel(sp.server);
                    if (m == null) return 0;
                    llevarHedda(sp, m, false);
                    return decir(c.getSource(), resumenHedda(sp));
                }))
                .then(Commands.literal("tam").executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    ServerLevel m = Morvain.nivel(sp.server);
                    if (m == null) return 0;
                    llevarHedda(sp, m, true);
                    return decir(c.getSource(), resumenHedda(sp));
                }))
                .then(Commands.literal("poner").executes(c -> {
                    ServerLevel m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    if (Hedda.buscarHedda(m, 0) == null) Hedda.ponerHedda(m);
                    if (Hedda.buscarTam(m) == null) Hedda.ponerTam(m);
                    if (Hedda.buscarHedda(m, 1) == null) Hedda.ponerLuto(m);
                    return decir(c.getSource(), "Hedda, Tam y la Hedda de rodillas puestos");
                }))
                .then(Commands.literal("olvidar").executes(c -> {
                    ServerLevel m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    Hedda.olvidar(m);
                    return decir(c.getSource(), "Hedda y Tam quitados");
                }));
    }

    /**
     * Lleva al jugador a Morvain: delante de Hedda (a 3 bloques al este, mirandola) o a la sala de la guardia, delante de Tam (dos bloques
     * al este, en el suelo de la sala, mirandole).
     */
    public static void llevarHedda(ServerPlayer sp, ServerLevel m, boolean tam) {
        BlockPos a, de;
        if (tam) {
            BlockPos t = Hedda.tam();
            m.getChunk(t.getX() >> 4, t.getZ() >> 4);
            a = t.above();
            de = t.offset(2, 0, 0);
        } else {
            BlockPos s = Hedda.sitio();
            m.getChunk(s.getX() >> 4, s.getZ() >> 4);
            int y = m.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, s.getX() + 3, s.getZ());
            de = new BlockPos(s.getX() + 3, y, s.getZ());
            int ys = m.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, s.getX(), s.getZ());
            a = new BlockPos(s.getX(), ys, s.getZ());
        }
        double dx = a.getX() + 0.5 - (de.getX() + 0.5), dz = a.getZ() + 0.5 - (de.getZ() + 0.5);
        double dy = a.getY() + 0.5 - (de.getY() + 1.62);
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz)), pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
        sp.teleportTo(m, de.getX() + 0.5, de.getY(), de.getZ() + 0.5, yaw, pitch);
    }

    /** /darkcraft oswin [ir | campana | poner | olvidar | sin] (sin mas: donde estan Oswin y su campana). */
    public static LiteralArgumentBuilder<CommandSourceStack> comandoOswin() {
        return Commands.literal("oswin")
                .executes(c -> {
                    ServerLevel m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    int[] k = Oswin.cabo();
                    return decir(c.getSource(), "Oswin " + Oswin.sitio().toShortString() + (Morvain.hecho(m.getServer(), "oswin") ? " (puesto)" : "")
                            + ", la campana " + (k == null ? "-" : k[0] + ", " + k[1] + ", " + k[2]) + (Morvain.hecho(m.getServer(), "campana") ? " (puesta)" : ""));
                })
                .then(Commands.literal("ir").executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    ServerLevel m = Morvain.nivel(sp.server);
                    if (m == null) return 0;
                    llevar(sp, m, false);
                    return decir(c.getSource(), resumen(sp));
                }))
                .then(Commands.literal("campana").executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    ServerLevel m = Morvain.nivel(sp.server);
                    if (m == null) return 0;
                    llevar(sp, m, true);
                    return decir(c.getSource(), resumen(sp));
                }))
                .then(Commands.literal("poner").executes(c -> {
                    ServerLevel m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    if (Oswin.buscarOswin(m) == null) Oswin.ponerOswin(m);
                    if (Oswin.buscarCampana(m) == null) Oswin.ponerCampana(m);
                    return decir(c.getSource(), "Oswin y su campana puestos");
                }))
                .then(Commands.literal("olvidar").executes(c -> {
                    ServerLevel m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    Oswin.olvidar(m);
                    return decir(c.getSource(), "Oswin y su campana quitados");
                }));
    }

    /** Lleva al jugador a Morvain, delante de Oswin (a 2,5 bloques, mirandole) o bajo la campana (en el camino, mirandola). */
    public static void llevar(ServerPlayer sp, ServerLevel m, boolean campana) {
        BlockPos a, de;
        if (campana) {
            int[] c = Oswin.cabo();
            if (c == null) return;
            a = new BlockPos(c[0], c[1], c[2]);
            m.getChunk(a.getX() >> 4, a.getZ() >> 4);
            int[] s = com.darkcraft.morvain.Arboleda.get().sitio("ante_campana");
            de = s != null ? new BlockPos(s[0], s[1], s[2]) : a.offset(3, -2, 2);
            a = a.below();
        } else {
            BlockPos s = Oswin.sitio();
            m.getChunk(s.getX() >> 4, s.getZ() >> 4);
            int y = m.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, s.getX() + 3, s.getZ());
            de = new BlockPos(s.getX() + 3, y, s.getZ());
            a = s.above();
        }
        double dx = a.getX() + 0.5 - (de.getX() + 0.5), dz = a.getZ() + 0.5 - (de.getZ() + 0.5);
        double dy = a.getY() + 0.5 - (de.getY() + 1.62);
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz)), pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
        sp.teleportTo(m, de.getX() + 0.5, de.getY(), de.getZ() + 0.5, yaw, pitch);
    }
}
