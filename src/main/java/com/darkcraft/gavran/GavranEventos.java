package com.darkcraft.gavran;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.arbol.PuertaArbol;
import com.darkcraft.arbol.TorreArbol;
import com.darkcraft.gen.CastlePlan;
import com.darkcraft.net.Net;
import com.darkcraft.rey.ReyEstado;
import com.darkcraft.rey.Sello;
import com.darkcraft.story.Story;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * Lo que rodea a Ser Gavran: que este en su rama cuando alguien se acerca a la Sala del Arbol, la sala cerrada
 * durante la pelea (puerta y ascensor), que no se pueda hacer trampa, que las cosas de quien muere se queden en
 * la sala y los comandos /darkcraft gavran ...
 */
public final class GavranEventos {
    private GavranEventos() {}

    public static final String TAG_ESCUDERO = "darkcraft_escudero";

    // ------------------------------------------------------------------ que este en su rama

    /** Pone a Gavran en su rama si no esta (y no ha caido ya). Lo llama TorreArbol.asegurar (cada 2 s, con alguien cerca). */
    public static void asegurar(ServerLevel ow, CastlePlan plan) {
        SalaArbol s = SalaArbol.de(plan);
        if (s == null) return;
        ReyEstado est = ReyEstado.get(ow.getServer());
        int st = est.get(s.key());
        if (st == ReyEstado.MUERTO) return;
        BlockPos centro = new BlockPos(s.cx(), s.F(), s.cz() - 20);
        if (!ow.isPositionEntityTicking(centro)) return;
        List<Gavran> gs = ow.getEntitiesOfClass(Gavran.class, s.caja());
        for (int i = 1; i < gs.size(); i++) gs.get(i).discard();
        if (gs.isEmpty()) {
            Gavran g = DarkCraftMod.GAVRAN.get().create(ow);
            if (g == null) return;
            Vec3 r = s.rama(ow);
            g.setSala(s, r);
            g.moveTo(r.x, r.y, r.z, 180f, 0f);
            Gavran.despejar(ow, s, r);
            // si ya hay alguien dentro de la sala (vuelve a aparecer tras caer), no empieza hasta que salgan
            for (ServerPlayer p : ow.players())
                if (Gavran.valido(p) && s.dentro(p.position())) g.desarmar();
            ow.addFreshEntity(g);
            if (st == ReyEstado.LUCHA) est.set(s.key(), ReyEstado.DORMIDO);
            Raices.limpiarSueltas(ow, s);
            DarkCraftMod.LOGGER.info("[DarkCraft] Ser Gavran vuelve a su rama en {}", BlockPos.containing(r));
        }
        // si el mundo se cerro con la puerta sellada por la pelea, que no se quede la barrera
        if (est.get(s.key()) != ReyEstado.LUCHA) {
            PuertaArbol d = TorreArbol.puerta(ow, new int[]{s.cx(), s.cz(), s.F(), 40, s.base()});
            if (d != null && d.estado() == PuertaArbol.ABIERTA) d.quitarBarrera(ow);
        }
    }

    /** La pelea empieza: la puerta se cierra (con su animacion) y el ascensor deja de responder. */
    static void cerrarSala(ServerLevel sl, SalaArbol s) {
        PuertaArbol d = TorreArbol.puerta(sl, new int[]{s.cx(), s.cz(), s.F(), 40, s.base()});
        if (d != null) d.cerrarLucha(sl);
    }

    /** La pelea acaba: si la puerta estaba abierta con la llave, se vuelve a abrir. */
    static void abrirSala(ServerLevel sl, SalaArbol s) {
        CastlePlan plan = Story.planNear(sl, s.cx(), s.cz());
        boolean llave = plan != null && ReyEstado.get(sl.getServer()).selloAbierto(Sello.key(plan));
        PuertaArbol d = TorreArbol.puerta(sl, new int[]{s.cx(), s.cz(), s.F(), 40, s.base()});
        if (d != null && llave) d.abrirLucha(sl);
    }

    /** Hay pelea en la Sala del Arbol de esta torre (para el ascensor). */
    public static boolean salaCerrada(ServerLevel sl, double x, double z) {
        SalaArbol s = SalaArbol.cerca(sl, x, z);
        return s != null && ReyEstado.get(sl.getServer()).get(s.key()) == ReyEstado.LUCHA;
    }

    /** Gavran luchando con este jugador (o en la entrada), o null. */
    public static Gavran luchaDe(Player p) {
        if (!(p.level() instanceof ServerLevel sl)) return null;
        for (Gavran g : sl.getEntitiesOfClass(Gavran.class, p.getBoundingBox().inflate(100))) {
            Gavran.Estado e = g.estado();
            if ((e == Gavran.Estado.INTRO || e == Gavran.Estado.LUCHA || e == Gavran.Estado.ATURDIDO || e == Gavran.Estado.CAMBIO
                    || e == Gavran.Estado.FIN1)
                    && g.jugadores().contains(p.getUUID())) return g;
        }
        return null;
    }

    // ------------------------------------------------------------------ sin monstruos sueltos en la sala

    /** En la Sala del Arbol y en su torre no salen monstruos solos: alli solo estan Gavran y sus escuderos. */
    @SubscribeEvent
    public static void onSpawn(MobSpawnEvent.FinalizeSpawn e) {
        if (e.getSpawnType() != MobSpawnType.NATURAL || !(e.getEntity() instanceof Enemy)) return;
        if (!(e.getLevel() instanceof ServerLevel sl) || sl.dimension() != Level.OVERWORLD) return;
        if (Sello.enSalaArbol(sl, BlockPos.containing(e.getX(), e.getY(), e.getZ()))) e.setSpawnCancelled(true);
    }

    // ------------------------------------------------------------------ nada de trampas durante la pelea

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent e) {
        if (e.getEntity() instanceof ServerPlayer p && !p.isCreative() && luchaDe(p) != null) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPearl(EntityTeleportEvent.EnderPearl e) {
        if (luchaDe(e.getPlayer()) != null) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onChorus(EntityTeleportEvent.ChorusFruit e) {
        if (e.getEntityLiving() instanceof ServerPlayer p && luchaDe(p) != null) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent e) {
        if (e.phase != net.minecraftforge.event.TickEvent.Phase.END || !(e.player instanceof ServerPlayer p) || p.tickCount % 5 != 0) return;
        if (p.isFallFlying() && luchaDe(p) != null) p.stopFallFlying();
    }

    /** Al caer un jugador: Gavran se despide de el. */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        if (e.getEntity() instanceof ServerPlayer p && p.level() instanceof ServerLevel sl) {
            Gavran g = luchaDe(p);
            if (g != null && g.estado() != Gavran.Estado.INTRO) g.habla(sl, 10);
        }
    }

    /** Lo que suelta quien cae en la pelea se queda en la sala sin desaparecer (como con el Rey). */
    @SubscribeEvent
    public static void onDrops(LivingDropsEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || e.getDrops().isEmpty()) return;
        Gavran g = luchaDe(p);
        if (g == null) return;
        for (ItemEntity it : e.getDrops()) it.setUnlimitedLifetime();
        DarkCraftMod.LOGGER.info("[DarkCraft] {} cae ante Ser Gavran: {} objetos se quedan en la sala", p.getName().getString(), e.getDrops().size());
    }

    // ------------------------------------------------------------------ comandos: /darkcraft gavran ...

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("darkcraft").requires(s -> s.hasPermission(2))
                .then(Commands.literal("gavran")
                        .then(Commands.literal("ir").executes(c -> ir(c.getSource())))
                        .then(Commands.literal("estado").executes(c -> estado(c.getSource())))
                        .then(Commands.literal("reiniciar").executes(c -> reiniciar(c.getSource())))
                        .then(Commands.literal("fase").then(Commands.argument("n", IntegerArgumentType.integer(1, 2))
                                .executes(c -> fase(c.getSource(), IntegerArgumentType.getInteger(c, "n")))))
                        .then(Commands.literal("matar").executes(c -> matar(c.getSource())))
                        .then(Commands.literal("ataque").then(Commands.argument("nombre", StringArgumentType.word())
                                .executes(c -> ataque(c.getSource(), StringArgumentType.getString(c, "nombre")))))
                        .then(Commands.literal("guardia").executes(c -> guardia(c.getSource())))));
    }

    private static SalaArbol salaDe(CommandSourceStack src) {
        ServerLevel ow = src.getServer().overworld();
        Vec3 p = src.getPosition();
        SalaArbol s = SalaArbol.cerca(ow, p.x, p.z);
        if (s != null) return s;
        int[] site = com.darkcraft.world.CastleSites.nearest(ow.getSeed(), (int) p.x, (int) p.z, 3);
        if (site == null) return null;
        return SalaArbol.de(com.darkcraft.world.PlanCache.get(ow.getSeed(), site[0], site[1], (x, z) -> 64));
    }

    public static Gavran gavranDe(CommandSourceStack src) {
        if (src.getEntity() instanceof ServerPlayer p) {
            Gavran g = luchaDe(p);
            if (g != null) return g;
            for (Gavran o : p.serverLevel().getEntitiesOfClass(Gavran.class, p.getBoundingBox().inflate(120))) return o;
        }
        return null;
    }

    /** A la Sala del Arbol, pasada la puerta, mirando al trono (empieza la pelea si se esta en supervivencia). */
    private static int ir(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        SalaArbol s = salaDe(src);
        if (s == null) {
            src.sendFailure(Component.translatable("darkcraft.cmd.no_reino"));
            return 0;
        }
        p.teleportTo(src.getServer().overworld(), s.ccx(), s.F(), s.cz() - 22.5, 180f, -12f);
        return 1;
    }

    private static int estado(CommandSourceStack src) {
        SalaArbol s = salaDe(src);
        Gavran g = gavranDe(src);
        String st = s == null ? "sin reino de Valdrenn cerca" : switch (ReyEstado.get(src.getServer()).get(s.key())) {
            case ReyEstado.LUCHA -> "peleando";
            case ReyEstado.MUERTO -> "caido";
            default -> "en su rama";
        };
        src.sendSuccess(() -> Component.literal("Ser Gavran: " + st + (g != null ? " | " + g.debug() : "")), false);
        return 1;
    }

    private static int reiniciar(CommandSourceStack src) {
        SalaArbol s = salaDe(src);
        if (s == null) return 0;
        ServerLevel ow = src.getServer().overworld();
        Gavran g = gavranDe(src);
        if (g != null && g.level() instanceof ServerLevel sl) g.reiniciar(sl, "comando");
        ReyEstado.get(src.getServer()).set(s.key(), ReyEstado.DORMIDO);
        CastlePlan plan = Story.planNear(ow, s.cx(), s.cz());
        if (plan != null) asegurar(ow, plan);
        src.sendSuccess(() -> Component.literal("Ser Gavran vuelve a su rama"), true);
        return 1;
    }

    private static int fase(CommandSourceStack src, int n) {
        Gavran g = gavranDe(src);
        if (g == null) return 0;
        if (n == 2) {
            // en la fase 1: el arbol despierta (cinematica y fase 2)
            if (g.fase() >= 2 || !g.forzarFinFase()) {
                src.sendFailure(Component.literal("Ser Gavran tiene que estar luchando en la fase 1"));
                return 0;
            }
            src.sendSuccess(() -> Component.literal("Ser Gavran: fase 2"), true);
        }
        return 1;
    }

    private static int matar(CommandSourceStack src) {
        Gavran g = gavranDe(src);
        if (g == null) return 0;
        g.forzarMuerte();
        return 1;
    }

    private static int ataque(CommandSourceStack src, String nombre) {
        Gavran g = gavranDe(src);
        if (g == null || !g.forzarAtaque(nombre)) {
            src.sendFailure(Component.literal("No (tiene que estar luchando; ataques: combo3 combo5 guardia embestida raices clavar invocar; fase 2: atrapar hojas suelo juicio balcon bajar salto)"));
            return 0;
        }
        return 1;
    }

    private static int guardia(CommandSourceStack src) {
        Gavran g = gavranDe(src);
        if (g == null || !g.forzarGuardiaRota()) return 0;
        return 1;
    }

    static void decir(ServerLevel sl, Gavran g, int n) {
        for (ServerPlayer p : sl.players())
            if (p.distanceToSqr(g) < 96 * 96) Net.to(p, new Net.ReyHabla(g.getId(), Gavran.VOZ_BASE + n));
    }

    static boolean enSala(Level lvl, BlockPos pos) {
        return lvl instanceof ServerLevel sl && Sello.enSalaArbol(sl, pos);
    }
}
