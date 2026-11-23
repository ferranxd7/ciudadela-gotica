package com.darkcraft.rey;

import com.darkcraft.DarkCraftMod;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * Lo que rodea al Rey: que aparezca en su trono cuando alguien se acerca, que las salas de los jefes no
 * se puedan romper, que no se pueda hacer trampa durante la pelea y los comandos de prueba.
 */
public final class ReyEventos {
    private ReyEventos() {}

    // ------------------------------------------------------------------ que el Rey este en su trono

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer p)) return;
        Rey r = luchaDe(p);
        if (r != null) {
            // nada de volar con elitros durante la pelea
            if (p.isFallFlying()) p.stopFallFlying();
        }
        if (Volcan.es(p.level()) && !p.isCreative() && !p.isSpectator() && p.tickCount % 2 == 0) {
            double dx = p.getX() - Volcan.CENTRO.x, dz = p.getZ() - Volcan.CENTRO.z;
            boolean fuera = dx * dx + dz * dz > (Volcan.R + 1.5) * (Volcan.R + 1.5);
            if (p.getY() < Volcan.MAR + 1.5 || (fuera && p.getY() < Volcan.Y - 1)) Volcan.caer(p);
        }
        if (p.tickCount % 40 != 7 || p.level().dimension() != Level.OVERWORLD) return;
        ServerLevel ow = p.serverLevel();
        var plan = com.darkcraft.story.Story.planNear(ow, p.getBlockX(), p.getBlockZ());
        if (plan != null) {
            int[] th = plan.treeHall();
            if (th != null) {
                double hx = p.getX() - th[0], hz = p.getZ() - th[1];
                if (p.getY() > th[4] - 16 && p.getY() < th[2] + 70 && hx * hx + hz * hz < 80 * 80)
                    com.darkcraft.arbol.TorreArbol.asegurar(ow, plan, p);
            }
        }
        Sala s = Sala.near(ow, p.getBlockX(), p.getBlockZ());
        if (s == null || p.position().distanceToSqr(s.seat()) > 80 * 80) return;
        s.taparTrono(ow);
        asegurarRey(ow, s);
    }

    /** Pone al Rey en su trono si no esta (y no ha muerto ya). */
    public static void asegurarRey(ServerLevel ow, Sala s) {
        int st = ReyEstado.get(ow.getServer()).get(s.key());
        if (st == ReyEstado.MUERTO) return;
        BlockPos seat = BlockPos.containing(s.seat());
        if (!ow.isPositionEntityTicking(seat)) return;
        List<Rey> reyes = ow.getEntitiesOfClass(Rey.class, s.box().inflate(6));
        if (!reyes.isEmpty()) {
            for (int i = 1; i < reyes.size(); i++) reyes.get(i).discard();
            return;
        }
        if (st == ReyEstado.LUCHA) return; // esta peleando en otro sitio (los volcanes)
        Rey r = DarkCraftMod.REY.get().create(ow);
        if (r == null) return;
        Vec3 v = s.seat();
        r.setHome(v, s);
        r.moveTo(v.x, v.y, v.z, 0f, 0f);
        ow.addFreshEntity(r);
        s.openAll(ow);
        DarkCraftMod.LOGGER.info("[DarkCraft] El Rey vuelve a sentarse en su trono en {}", seat);
    }

    /** El jefe con el que esta peleando este jugador, o null. */
    public static Rey luchaDe(Player p) {
        if (!(p.level() instanceof ServerLevel sl)) return null;
        for (Rey r : sl.getEntitiesOfClass(Rey.class, p.getBoundingBox().inflate(96))) {
            if (r.luchando() || r.estado() == Rey.Estado.INTRO || r.estado() == Rey.Estado.LEVANTA || r.estado() == Rey.Estado.CAMBIO)
                if (r.jugadores().contains(p.getUUID())) return r;
        }
        return null;
    }

    // ------------------------------------------------------------------ salas indestructibles

    private static boolean protegido(Level lvl, BlockPos pos) {
        if (!(lvl instanceof ServerLevel sl)) return false;
        if (Volcan.es(sl)) return true;
        if (lvl.dimension() != Level.OVERWORLD) return false;
        Sala s = Sala.near(sl, pos.getX(), pos.getZ());
        if (s != null && s.inShell(pos)) return true;
        return Sello.enSalaArbol(sl, pos);
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent e) {
        if (e.getPlayer() != null && e.getPlayer().isCreative()) return;
        if (e.getLevel() instanceof Level lvl && protegido(lvl, e.getPos())) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || p.isCreative()) return;
        // durante la pelea no se puede poner nada (ni torres para subirse, ni nada raro)
        if (luchaDe(p) != null || Volcan.es(p.serverLevel())) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onExplode(ExplosionEvent.Detonate e) {
        Level lvl = e.getLevel();
        e.getAffectedBlocks().removeIf(pos -> protegido(lvl, pos));
    }

    @SubscribeEvent
    public static void onPearl(EntityTeleportEvent.EnderPearl e) {
        if (luchaDe(e.getPlayer()) != null || Volcan.es(e.getPlayer().serverLevel())) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onChorus(EntityTeleportEvent.ChorusFruit e) {
        if (e.getEntityLiving() instanceof ServerPlayer p && (luchaDe(p) != null || Volcan.es(p.serverLevel()))) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) {
            Rey r = luchaDe(p);
            // se burla (si no esta diciendo ya otra cosa)
            if (r != null && r.level() instanceof ServerLevel sl) r.hablaSiCalla(sl, r.fase() == 3 ? 13 : 7);
        }
    }

    /** Si mueres en los volcanes, tus cosas no se quedan alli: aparecen tiradas en el centro del salon del trono. */
    @SubscribeEvent
    public static void onDrops(net.minecraftforge.event.entity.living.LivingDropsEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || !Volcan.es(p.level()) || e.getDrops().isEmpty()) return;
        ServerLevel ow = p.getServer().overworld();
        Rey r = luchaDe(p);
        Sala s = r != null ? r.sala() : null;
        if (s == null) {
            BlockPos sp = ow.getSharedSpawnPos();
            s = Sala.near(ow, sp.getX(), sp.getZ());
        }
        if (s == null) return;
        net.minecraft.world.phys.Vec3 c = s.center();
        ow.getChunk((int) Math.floor(c.x) >> 4, (int) Math.floor(c.z) >> 4);
        for (net.minecraft.world.entity.item.ItemEntity it : e.getDrops()) {
            double a = p.getRandom().nextDouble() * Math.PI * 2, d = p.getRandom().nextDouble() * 1.6;
            var ie = new net.minecraft.world.entity.item.ItemEntity(ow, c.x + Math.cos(a) * d, s.t.y() + 0.3, c.z + Math.sin(a) * d, it.getItem().copy());
            ie.setDeltaMovement(0, 0.1, 0);
            ie.setDefaultPickUpDelay();
            ie.setUnlimitedLifetime();
            ow.addFreshEntity(ie);
        }
        e.setCanceled(true);
        DarkCraftMod.LOGGER.info("[DarkCraft] {} muere en los volcanes: {} objetos al centro del salon del trono", p.getName().getString(), e.getDrops().size());
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer p && Volcan.es(p.level()) && luchaDe(p) == null) Volcan.devolver(p);
    }

    // ------------------------------------------------------------------ comandos de prueba

    /** /darkcraft valdrenn rey ... (lo registra Comandos). */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> comando() {
        return Commands.literal("rey")
                .then(Commands.literal("estado").executes(c -> estado(c.getSource())))
                .then(Commands.literal("reiniciar").executes(c -> reiniciar(c.getSource())))
                .then(Commands.literal("fase").then(Commands.argument("n", IntegerArgumentType.integer(1, 3))
                        .executes(c -> fase(c.getSource(), IntegerArgumentType.getInteger(c, "n")))))
                .then(Commands.literal("matar").executes(c -> matar(c.getSource())))
                .then(Commands.literal("golpefinal").executes(c -> golpeFinal(c.getSource())))
                .then(Commands.literal("ir").executes(c -> ir(c.getSource())));
    }

    /** /darkcraft valdrenn rey golpefinal: el golpe final ya (solo en la fase 3, luchando). */
    private static int golpeFinal(CommandSourceStack src) {
        Rey r = reyDe(src);
        if (r == null || !r.forzarGolpeFinal()) {
            src.sendFailure(Component.literal("El Rey tiene que estar luchando en la fase 3 (/darkcraft valdrenn rey fase 3)"));
            return 0;
        }
        src.sendSuccess(() -> Component.literal("Golpe final: corre a un circulo dorado"), true);
        return 1;
    }

    private static Sala salaDe(CommandSourceStack src) {
        ServerLevel ow = src.getServer().overworld();
        Vec3 p = src.getPosition();
        return Sala.near(ow, (int) p.x, (int) p.z);
    }

    private static Rey reyDe(CommandSourceStack src) {
        if (src.getEntity() instanceof ServerPlayer p) {
            Rey r = luchaDe(p);
            if (r != null) return r;
            for (Rey o : p.serverLevel().getEntitiesOfClass(Rey.class, p.getBoundingBox().inflate(120))) return o;
        }
        return null;
    }

    private static int estado(CommandSourceStack src) {
        Sala s = salaDe(src);
        Rey r = reyDe(src);
        String st = s == null ? "sin reino de Valdrenn cerca" : switch (ReyEstado.get(src.getServer()).get(s.key())) {
            case ReyEstado.LUCHA -> "peleando";
            case ReyEstado.MUERTO -> "muerto";
            default -> "en su trono";
        };
        src.sendSuccess(() -> Component.literal("Rey: " + st + (r != null ? " | " + r.estado() + ", fase " + r.fase()
                + ", vida " + Math.round(r.getHealth()) + "/" + Math.round(r.getMaxHealth()) + ", jugadores " + r.jugadores().size() : "")), false);
        return 1;
    }

    private static int reiniciar(CommandSourceStack src) {
        Sala s = salaDe(src);
        if (s == null) return 0;
        ServerLevel ow = src.getServer().overworld();
        Rey r = reyDe(src);
        if (r != null && r.level() instanceof ServerLevel sl) r.reiniciar(sl, "comando");
        ReyEstado.get(src.getServer()).olvidar(s.key());
        s.openAll(ow);
        asegurarRey(ow, s);
        src.sendSuccess(() -> Component.literal("El Rey vuelve a esperar en su trono"), true);
        return 1;
    }

    private static int fase(CommandSourceStack src, int n) {
        Rey r = reyDe(src);
        if (r == null) return 0;
        r.forzarFase(n);
        src.sendSuccess(() -> Component.literal("Fase " + n), true);
        return 1;
    }

    private static int matar(CommandSourceStack src) {
        Rey r = reyDe(src);
        if (r == null) return 0;
        r.forzarMuerte();
        return 1;
    }

    private static int ir(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        Sala s = salaDe(src);
        if (s == null) {
            src.sendFailure(Component.translatable("darkcraft.cmd.no_reino"));
            return 0;
        }
        // a la puerta del gran salon, mirando al trono
        Vec3 c = s.center();
        p.teleportTo(src.getServer().overworld(), c.x, s.t.y(), s.t.z1() + 4.5, 180f, 0f);
        return 1;
    }
}
