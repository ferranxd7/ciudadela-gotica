package com.darkcraft.cine;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gavran.SalaArbol;
import com.darkcraft.gavran.VictoriaGavran;
import com.darkcraft.gen.CastlePlan;
import com.darkcraft.net.Net;
import com.darkcraft.story.Story;
import com.darkcraft.story.StoryEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

/**
 * Lado servidor de las cinematicas: congela al jugador (no puede moverse ni recibir dano), lo lleva
 * de un plano a otro en el capitulo 1 (para que le lleguen los chunks de cada sitio) y al acabar lo
 * devuelve donde estaba y le abre el cofre. Los planos del capitulo 5 estan todos en la Sala del Arbol y
 * sus alrededores: ahi no se le mueve.
 */
public final class Cinematics {
    private Cinematics() {}

    private static final TicketType<ChunkPos> TICKET = TicketType.create("darkcraft_cine", Comparator.comparingLong(ChunkPos::toLong), 20 * 150);
    private static final Map<UUID, Session> ACTIVE = new HashMap<>();
    private static final String BACK = "darkcraft_vuelta";

    private static final class Session {
        int chapter, startTick, shot = 0;
        float total;
        List<Shot> shots = List.of();
        BlockPos chest;
        boolean moved, back;
        double rx, ry, rz;
        float ryaw, rpitch;
        boolean flying, mayfly, invisible;
    }

    /** Cinematicas de jefes (no son capitulos): los planos los manda quien la empieza. */
    public static final int REY_VOLCAN = 30, SELLO = 31, REY_INTRO = 32, GAVRAN_INTRO = 33, GAVRAN_CAMBIO = 34, GAVRAN_MUERTE = 35;

    /** Cinematica con planos dados, sin mover al jugador (los planos estan cerca de donde esta). */
    public static void playShots(ServerPlayer p, int id, List<Shot> shots, float total) {
        Session s = new Session();
        s.chapter = id;
        s.startTick = p.getServer().getTickCount();
        s.total = total;
        s.shots = shots;
        ACTIVE.put(p.getUUID(), s);
        Net.to(p, new Net.CineStart(id, shots, new double[0]));
    }

    public static boolean active(Player p) {
        return ACTIVE.containsKey(p.getUUID());
    }

    /**
     * Cinematica del capitulo para quien abre el cofre y los jugadores que esten a menos de 100 bloques.
     * Al terminar (o al saltarla) a quien la abrio se le abre el cofre en chest (si no es null).
     */
    public static void play(ServerPlayer opener, int chapter, BlockPos chest, boolean viewers) {
        ServerLevel lvl = opener.serverLevel();
        List<ServerPlayer> who = new ArrayList<>();
        who.add(opener);
        if (viewers)
            for (ServerPlayer p : lvl.players())
                if (p != opener && !active(p) && !p.isSpectator() && p.distanceToSqr(opener) <= 100 * 100) who.add(p);
        CastlePlan plan = chapter == 1 ? Story.planNear(lvl, opener.getBlockX(), opener.getBlockZ()) : null;
        double[] bolts = plan != null ? CineShots.bolts1(plan.cine(), lvl.getGameTime()) : new double[0];
        // capitulo 5: en la Sala del Arbol (si se esta en ella), con la llave de hueso flotando sobre el cofre
        SalaArbol sala = chapter == 5 ? SalaArbol.cerca(lvl, opener.getX(), opener.getZ()) : null;
        if (sala != null && (sala.dist(opener.getX(), opener.getZ()) > 80 || Math.abs(opener.getY() - sala.F()) > 40)) sala = null;
        if (sala != null) VictoriaGavran.llaveFlotante(lvl, sala, Math.round(Timeline.total(5) * 20) + 20);
        boolean preloaded = false, despejado = false;
        for (ServerPlayer p : who) {
            Session s = new Session();
            s.chapter = chapter;
            s.startTick = lvl.getServer().getTickCount();
            s.total = Timeline.total(chapter);
            s.chest = p == opener ? chest : null;
            if (plan != null) {
                s.shots = CineShots.chapter1(plan.cine(), p.getEyePosition(), p.getLookAngle());
                if (!preloaded) {
                    // que se vayan generando los sitios de cada plano antes de llegar a ellos
                    for (int k = 1; k < s.shots.size(); k++) {
                        Shot sh = s.shots.get(k);
                        double[] a = sh.at((sh.t0 + sh.t1) / 2);
                        ChunkPos cp = new ChunkPos(BlockPos.containing(a[0], a[1], a[2]));
                        lvl.getChunkSource().addRegionTicket(TICKET, cp, 4, cp);
                    }
                    preloaded = true;
                }
                s.rx = p.getX();
                s.ry = p.getY();
                s.rz = p.getZ();
                s.ryaw = p.getYRot();
                s.rpitch = p.getXRot();
                s.flying = p.getAbilities().flying;
                s.mayfly = p.getAbilities().mayfly;
                s.invisible = p.isInvisible();
                CompoundTag b = new CompoundTag();
                b.putDouble("x", s.rx);
                b.putDouble("y", s.ry);
                b.putDouble("z", s.rz);
                b.putFloat("yaw", s.ryaw);
                b.putFloat("pitch", s.rpitch);
                p.getPersistentData().put(BACK, b);
                p.stopRiding();
                p.getAbilities().mayfly = true;
                p.getAbilities().flying = true;
                p.onUpdateAbilities();
                p.setInvisible(true);
                s.moved = true;
            } else if (sala != null) {
                s.shots = CineShots.chapter5(sala, lvl, p.getEyePosition(), p.getYRot());
                if (!despejado) {
                    // que no tapen las lianas en los planos de dentro de la sala
                    for (int k = 0; k < 2; k++) sala.despejarVista(lvl, s.shots.get(k).pos, s.shots.get(k).look);
                    despejado = true;
                }
            }
            ACTIVE.put(p.getUUID(), s);
            Net.to(p, new Net.CineStart(chapter, s.shots, bolts));
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || ACTIVE.isEmpty()) return;
        MinecraftServer server = e.getServer();
        int now = server.getTickCount();
        Iterator<Map.Entry<UUID, Session>> it = ACTIVE.entrySet().iterator();
        List<Runnable> after = new ArrayList<>();
        while (it.hasNext()) {
            var en = it.next();
            Session s = en.getValue();
            ServerPlayer p = server.getPlayerList().getPlayer(en.getKey());
            if (p == null) {
                it.remove();
                continue;
            }
            float t = (now - s.startTick) / 20f;
            if (s.moved && !s.back) {
                // ir al sitio de cada plano mientras la pantalla esta en negro
                for (int k = s.shot + 1; k < s.shots.size(); k++) {
                    Shot sh = s.shots.get(k);
                    if (t < sh.t0 - 0.15f) break;
                    double[] a = sh.at((sh.t0 + sh.t1) / 2);
                    float[] ang = Shot.angles(a);
                    p.teleportTo(p.serverLevel(), a[0], a[1] - 1.62, a[2], ang[0], ang[1]);
                    s.shot = k;
                }
                if (t >= Timeline.back(s.chapter)) {
                    restore(p, s);
                    s.back = true;
                }
            }
            if (t >= s.total) {
                it.remove();
                after.add(() -> finish(p, s));
            }
        }
        after.forEach(Runnable::run);
    }

    /** El jugador ha mantenido ESPACIO: se acaba ya. */
    public static void skip(ServerPlayer p) {
        Session s = ACTIVE.remove(p.getUUID());
        if (s != null) finish(p, s);
    }

    private static void finish(ServerPlayer p, Session s) {
        if (s.moved && !s.back) restore(p, s);
        if (s.chapter >= REY_VOLCAN) return;
        Story.markSeen(p, s.chapter);
        if (s.chest != null) StoryEvents.openChest(p, s.chest, s.chapter);
    }

    private static void restore(ServerPlayer p, Session s) {
        if (!s.moved) return;
        p.teleportTo(p.serverLevel(), s.rx, s.ry, s.rz, s.ryaw, s.rpitch);
        p.getAbilities().flying = s.flying;
        p.getAbilities().mayfly = s.mayfly;
        p.onUpdateAbilities();
        p.setInvisible(s.invisible);
        p.fallDistance = 0;
        p.getPersistentData().remove(BACK);
        s.moved = false;
    }

    /** Termina sin abrir nada (muerte, cambio de dimension, salir del juego). */
    private static void abort(ServerPlayer p, boolean tell) {
        Session s = ACTIVE.remove(p.getUUID());
        if (s == null) return;
        if (s.moved && !s.back) {
            p.moveTo(s.rx, s.ry, s.rz, s.ryaw, s.rpitch);
            p.getAbilities().flying = s.flying;
            p.getAbilities().mayfly = s.mayfly;
            p.onUpdateAbilities();
            p.setInvisible(s.invisible);
            p.fallDistance = 0;
            p.getPersistentData().remove(BACK);
        }
        if (tell) Net.to(p, new Net.CineStop());
    }

    // ------------------------------------------------------------------ protecciones

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent e) {
        if (e.getEntity() instanceof ServerPlayer p && active(p)) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onTarget(LivingChangeTargetEvent e) {
        if (e.getNewTarget() instanceof ServerPlayer p && active(p)) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) abort(p, true);
    }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) abort(p, true);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) abort(p, false);
    }

    /** Si el servidor se cerro en mitad de una cinematica, el jugador vuelve a donde estaba. */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        CompoundTag b = p.getPersistentData().getCompound(BACK);
        if (b.isEmpty()) return;
        p.getPersistentData().remove(BACK);
        p.teleportTo(p.serverLevel(), b.getDouble("x"), b.getDouble("y"), b.getDouble("z"), b.getFloat("yaw"), b.getFloat("pitch"));
        p.gameMode.getGameModeForPlayer().updatePlayerAbilities(p.getAbilities());
        p.onUpdateAbilities();
        p.setInvisible(false);
        p.fallDistance = 0;
        DarkCraftMod.LOGGER.info("[DarkCraft] {} vuelve a su sitio tras una cinematica interrumpida", p.getName().getString());
    }

    @SubscribeEvent
    public static void onStopping(ServerStoppingEvent e) {
        for (UUID id : new ArrayList<>(ACTIVE.keySet())) {
            ServerPlayer p = e.getServer().getPlayerList().getPlayer(id);
            if (p != null) abort(p, false);
        }
        ACTIVE.clear();
    }

    /** Para las pruebas: los planos que tendria la cinematica del capitulo 1 en esta ciudadela. */
    public static List<Shot> preview(CastlePlan plan) {
        CastlePlan.Cine c = plan.cine();
        return CineShots.chapter1(c, new Vec3(c.ascX() + 4.5, c.y0() + 1.62, c.ascZ() + 0.5), new Vec3(1, 0, 0));
    }
}
