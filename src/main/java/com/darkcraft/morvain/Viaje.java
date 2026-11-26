package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.net.Net;
import com.darkcraft.rol.Ficha;
import com.darkcraft.santuario.Santuarios;
import com.darkcraft.story.Logros;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Viajar entre los santuarios de Morvain (parte 7c, respuesta 92 del plan). Descansando en uno, el menu del santuario
 * tiene "Viajar": la lista de los de Morvain que este jugador ha encendido o en los que ha descansado (apuntados en su
 * ficha) con el mapa de Morvain. Al elegir uno (`Net.Viajar`): fundido a negro en su cliente (`Net.Fundido`), y a los
 * {@link #CORTE} ticks se le lleva delante de ese santuario, mirandolo (el corcel, si lo llevaba, se deshace). No se
 * puede con enemigos encima ni a uno que no conoce.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Viaje {
    private Viaje() {}

    /** Lo que dura el negro: cuando se le lleva y cuando vuelve a verse (ticks). */
    public static final int CORTE = 12, FUNDIDO = 34;

    private record Pendiente(String id, int cuando) {}

    private static final Map<UUID, Pendiente> PENDIENTES = new HashMap<>();

    /** La clave con la que la ficha apunta un santuario (la de {@code Ficha.santuarioNuevo}). */
    private static String clave(BlockPos pos) {
        return Morvain.NIVEL.location() + "@" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    /** Los santuarios de Morvain que conoce este jugador (por su id, en el orden de {@link SantuariosMorvain#todos}). */
    public static List<String> conocidos(ServerPlayer sp) {
        java.util.Set<String> l = Ficha.santuariosVistos(sp);
        List<String> r = new ArrayList<>();
        for (SantuariosMorvain.Sitio s : SantuariosMorvain.todos()) if (l.contains(clave(s.pos()))) r.add(s.id());
        return r;
    }

    /** El santuario de Morvain en el que esta descansando (o null). */
    public static SantuariosMorvain.Sitio aqui(ServerPlayer sp) {
        GlobalPos g = Santuarios.descansoEn(sp);
        if (g == null || g.dimension() != Morvain.NIVEL || !Santuarios.junto(sp)) return null;
        return SantuariosMorvain.en(g.pos());
    }

    /** Lo pide el cliente desde el menu del santuario. */
    public static void viajar(ServerPlayer sp, String id) {
        SantuariosMorvain.Sitio desde = aqui(sp), hacia = SantuariosMorvain.porId(id);
        if (!sp.isAlive() || desde == null || hacia == null || desde == hacia || PENDIENTES.containsKey(sp.getUUID())) return;
        if (!sp.isCreative() && com.darkcraft.rol.Rol.enCombate(sp)) {
            Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.santuario.enemigos", 0));
            return;
        }
        if (!conocidos(sp).contains(id) && !sp.isCreative()) return;
        ServerLevel m = sp.serverLevel();
        m.playSound(null, sp.getX(), sp.getY(), sp.getZ(), DarkCraftMod.SFX_SANTUARIO_DESCANSO.get(), SoundSource.PLAYERS, 0.9f, 0.8f);
        m.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, sp.getX(), sp.getY() + 1, sp.getZ(), 30, 0.4, 0.8, 0.4, 0.03);
        Net.to(sp, new Net.Fundido(FUNDIDO));
        PENDIENTES.put(sp.getUUID(), new Pendiente(id, sp.server.getTickCount() + CORTE));
        DarkCraftMod.LOGGER.info("[DarkCraft] viaje: {} de {} a {}", sp.getGameProfile().getName(), desde.id(), id);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || PENDIENTES.isEmpty()) return;
        int ahora = e.getServer().getTickCount();
        for (Iterator<Map.Entry<UUID, Pendiente>> it = PENDIENTES.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Pendiente> en = it.next();
            if (ahora < en.getValue().cuando) continue;
            it.remove();
            ServerPlayer sp = e.getServer().getPlayerList().getPlayer(en.getKey());
            ServerLevel m = Morvain.nivel(e.getServer());
            if (sp != null && m != null && sp.isAlive()) llevar(sp, m, SantuariosMorvain.porId(en.getValue().id));
        }
    }

    /** Delante del santuario (al sur si se puede), mirandolo. */
    static void llevar(ServerPlayer sp, ServerLevel m, SantuariosMorvain.Sitio s) {
        if (s == null) return;
        if (sp.getVehicle() instanceof com.darkcraft.montura.Corcel c) c.quitar();
        if (sp.isPassenger()) sp.stopRiding();
        BlockPos p = s.pos();
        m.getChunk(p.getX() >> 4, p.getZ() >> 4);
        double[] sitio = sitio(m, p);
        float giro = (float) (Mth.atan2(p.getZ() + 0.5 - sitio[2], p.getX() + 0.5 - sitio[0]) * Mth.RAD_TO_DEG) - 90f;
        sp.teleportTo(m, sitio[0], sitio[1], sitio[2], giro, 15f);
        sp.setDeltaMovement(0, 0, 0);
        sp.resetFallDistance();
        m.playSound(null, sitio[0], sitio[1], sitio[2], DarkCraftMod.SFX_SANTUARIO_DESCANSO.get(), SoundSource.PLAYERS, 0.8f, 1.1f);
        m.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, sitio[0], sitio[1] + 1, sitio[2], 30, 0.4, 0.8, 0.4, 0.03);
        Logros.dar(sp, "viaje");
        HECHOS++;
        DarkCraftMod.LOGGER.info("[DarkCraft] viaje: {} llega a {} ({} {} {})", sp.getGameProfile().getName(), s.id(), Mth.floor(sitio[0]),
                Mth.floor(sitio[1]), Mth.floor(sitio[2]));
    }

    /** Un sitio de pie junto al santuario: al sur, este, oeste o norte, a 2 bloques. */
    private static double[] sitio(ServerLevel m, BlockPos s) {
        int[][] lados = {{0, 2}, {2, 0}, {-2, 0}, {0, -2}, {0, 3}, {3, 0}};
        for (int[] d : lados)
            for (int dy = -1; dy <= 2; dy++) {
                BlockPos q = s.offset(d[0], dy, d[1]);
                if (m.getBlockState(q).isAir() && m.getBlockState(q.above()).isAir() && m.getBlockState(q.below()).isFaceSturdy(m, q.below(), net.minecraft.core.Direction.UP))
                    return new double[]{q.getX() + 0.5, q.getY(), q.getZ() + 0.5};
            }
        return new double[]{s.getX() + 0.5, s.getY() + 1, s.getZ() + 2.5};
    }

    // ------------------------------------------------------------------ lo que sabe el cliente (las marcas de la brujula)

    /** Le manda los santuarios de Morvain que conoce. */
    public static void avisar(ServerPlayer sp) {
        Net.to(sp, new Net.Conocidos(conocidos(sp)));
    }

    @SubscribeEvent
    public static void onEntrar(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp && Morvain.es(sp.level())) avisar(sp);
    }

    @SubscribeEvent
    public static void onCambio(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp && e.getTo() == Morvain.NIVEL) avisar(sp);
    }

    @SubscribeEvent
    public static void onSalir(PlayerEvent.PlayerLoggedOutEvent e) {
        PENDIENTES.remove(e.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent e) {
        PENDIENTES.clear();
    }

    /** Para la prueba: viajes hechos desde que arranco el juego. */
    public static int HECHOS;

    /** Para la prueba: si tiene un viaje en marcha. */
    public static boolean pendiente(ServerPlayer sp) {
        return PENDIENTES.containsKey(sp.getUUID());
    }

    // ------------------------------------------------------------------ el comando (para probar)

    /**
     * /darkcraft viaje: la lista (los que conoce, con su sitio); {@code conocer <id>|todos} los apunta como si hubiera
     * descansado en ellos; {@code olvidar} los borra de su ficha; {@code ir <id>} le lleva delante de uno (desde donde sea).
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comando() {
        return net.minecraft.commands.Commands.literal("viaje")
                .executes(c -> lista(c.getSource()))
                .then(net.minecraft.commands.Commands.literal("lista").executes(c -> lista(c.getSource())))
                .then(net.minecraft.commands.Commands.literal("olvidar").executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    int n = Ficha.olvidarSantuarios(sp, Morvain.NIVEL.location() + "@");
                    avisar(sp);
                    c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("olvidados " + n), false);
                    return 1;
                }))
                .then(net.minecraft.commands.Commands.literal("conocer")
                        .then(net.minecraft.commands.Commands.argument("id", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(ids(true), b))
                                .executes(c -> {
                                    ServerPlayer sp = c.getSource().getPlayerOrException();
                                    String id = com.mojang.brigadier.arguments.StringArgumentType.getString(c, "id");
                                    int n = 0;
                                    for (SantuariosMorvain.Sitio s : SantuariosMorvain.todos())
                                        if (id.equals("todos") || s.id().equals(id)) {
                                            Ficha.santuarioNuevo(sp, Morvain.NIVEL, s.pos());
                                            n++;
                                        }
                                    avisar(sp);
                                    int k = n;
                                    c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("conocidos " + k + " (ahora " + conocidos(sp).size() + ")"), false);
                                    return n > 0 ? 1 : 0;
                                })))
                .then(net.minecraft.commands.Commands.literal("ir")
                        .then(net.minecraft.commands.Commands.argument("id", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(ids(false), b))
                                .executes(c -> {
                                    ServerPlayer sp = c.getSource().getPlayerOrException();
                                    SantuariosMorvain.Sitio s = SantuariosMorvain.porId(com.mojang.brigadier.arguments.StringArgumentType.getString(c, "id"));
                                    ServerLevel m = Morvain.nivel(sp.server);
                                    if (s == null || m == null) return 0;
                                    llevar(sp, m, s);
                                    return 1;
                                })));
    }

    private static List<String> ids(boolean todos) {
        List<String> l = new ArrayList<>();
        if (todos) l.add("todos");
        for (SantuariosMorvain.Sitio s : SantuariosMorvain.todos()) l.add(s.id());
        return l;
    }

    private static int lista(net.minecraft.commands.CommandSourceStack src) {
        ServerPlayer sp = src.getPlayer();
        if (sp == null) return 0;
        List<String> c = conocidos(sp);
        src.sendSuccess(() -> net.minecraft.network.chat.Component.literal("santuarios de Morvain: conoce " + c.size() + " de " + SantuariosMorvain.todos().length), false);
        for (SantuariosMorvain.Sitio s : SantuariosMorvain.todos())
            src.sendSuccess(() -> net.minecraft.network.chat.Component.literal((c.contains(s.id()) ? " * " : "   ") + s.id() + " (" + s.region().id + ") "
                    + s.pos().toShortString()).append(" ").append(net.minecraft.network.chat.Component.translatable(s.nombre())), false);
        return 1;
    }
}
