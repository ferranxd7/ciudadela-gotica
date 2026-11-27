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
 * con adonde ir ahora: el diario de la Historia, la brujula y el mapa). Por ahora una: {@link Oswin}, la campana del peregrino.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Misiones {
    private Misiones() {}

    /** Todas, en el orden del menu. */
    public static final String[] TODAS = {Oswin.ID};

    /** Lo ultimo que se le mando a cada uno (si cambia —coge o suelta la campana—, se le vuelve a mandar). */
    private static final Map<UUID, Integer> MANDADO = new HashMap<>();

    /** Cada segundo, desde {@code Morvain.onTick}. */
    public static void tick(ServerLevel m) {
        Oswin.tick(m);
        for (ServerPlayer p : m.players()) {
            Integer antes = MANDADO.get(p.getUUID());
            if (antes == null || antes != huella(p)) enviar(p);
        }
    }

    private static int huella(ServerPlayer p) {
        return Oswin.marcas(p) | (Oswin.lleva(p) ? Oswin.LLEVA : 0);
    }

    /** Le manda las misiones que conoce (las de marcas 0 no; la de Oswin tambien si lleva su campana sin conocerle). */
    public static void enviar(ServerPlayer p) {
        List<Net.MisionE> l = new ArrayList<>();
        int k = huella(p);
        if (k != 0) {
            BlockPos o = Oswin.objetivo(p);
            l.add(new Net.MisionE(Oswin.ID, k, o != null, o == null ? 0 : o.getX(), o == null ? 0 : o.getY(), o == null ? 0 : o.getZ()));
        }
        Net.to(p, new Net.Misiones(l));
        MANDADO.put(p.getUUID(), k);
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
        };
    }

    /** Lo que se le responde (aceptar, el precio, rechazar, devolver, quedarse). */
    public static void accion(ServerPlayer sp, Viajero v, String accion) {
        switch (v.quien()) {
            case OSWIN -> Oswin.accion(sp, v, accion);
        }
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
        return b.toString();
    }

    /** Olvida lo hecho en una mision (y le quita lo que le dio: la campana). */
    public static void olvidar(ServerPlayer sp, String id) {
        Ficha.mision(sp, id, 0);
        if (Oswin.ID.equals(id)) {
            var inv = sp.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++)
                if (inv.getItem(i).is(DarkCraftMod.CAMPANA_PEREGRINO.get()) || inv.getItem(i).is(DarkCraftMod.CAMPANA_DIFUNTOS.get()))
                    inv.setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
            com.darkcraft.dialogo.Conversaciones.olvidar(sp, id);
        }
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
                })));
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
