package com.ciudadela.test;

import com.ciudadela.CiudadelaMod;
import com.ciudadela.cine.Cinematics;
import com.ciudadela.cine.Timeline;
import com.ciudadela.client.ClientCine;
import com.ciudadela.client.ClientMusic;
import com.ciudadela.gen.CastlePlan;
import com.ciudadela.world.CastleSites;
import com.ciudadela.world.PlanCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Prueba automatica del CLIENTE (solo en GitHub Actions, con -Dciudadela.ciClient=true): abre el mundo
 * que genero la prueba del servidor, va a sitios concretos, saca capturas y anota si suenan la voz y la
 * musica (el sonido se graba aparte en un WAV). Al acabar cierra el juego.
 */
@Mod.EventBusSubscriber(modid = CiudadelaMod.MODID, value = Dist.CLIENT)
public final class ClientCiTest {
    private ClientCiTest() {}

    public static final boolean ON = Boolean.getBoolean("ciudadela.ciClient");
    private static final int LIMIT = 20 * 60 * 14;

    private static boolean loading, done;
    private static int ticks, inGame = -1, frames, fps;
    private static long fpsT;
    private static PrintWriter log;
    private static final List<Step> steps = new ArrayList<>();
    private static int next;
    private static int cineStart = -1;
    private static int[] chest1;

    private record Step(int at, String what, Consumer<Minecraft> run) {}

    private static void log(String s) {
        try {
            if (log == null) {
                File d = new File("ci-cliente");
                d.mkdirs();
                log = new PrintWriter(new FileWriter(new File(d, "informe_cliente.txt")), true);
            }
            log.println(String.format("[%6.1f s] %s", ticks / 20f, s));
        } catch (Exception ex) {
            CiudadelaMod.LOGGER.error("[CI cliente] no se puede escribir el informe", ex);
        }
        CiudadelaMod.LOGGER.info("[CI cliente] {}", s);
    }

    @SubscribeEvent
    public static void onRender(TickEvent.RenderTickEvent e) {
        if (!ON || e.phase != TickEvent.Phase.START) return;
        frames++;
        long now = System.currentTimeMillis();
        if (now - fpsT >= 1000) {
            fps = frames;
            frames = 0;
            fpsT = now;
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (!ON || done || e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        ticks++;
        try {
            if (ticks > LIMIT) {
                log("TIEMPO AGOTADO: se cierra el juego");
                finish(mc);
                return;
            }
            if (mc.level == null || mc.player == null) {
                if (!loading && mc.screen instanceof TitleScreen && ticks > 40) {
                    loading = true;
                    log("Pantalla de titulo: abriendo el mundo 'ciudadela'");
                    Object flows = Minecraft.class.getMethod("createWorldOpenFlows").invoke(mc);
                    Method load = null;
                    for (Method m : flows.getClass().getMethods())
                        if (m.getName().equals("loadLevel") && m.getParameterCount() == 2) load = m;
                    if (load == null) {
                        StringBuilder sb = new StringBuilder();
                        for (Method m : flows.getClass().getMethods()) sb.append(m).append('\n');
                        log("No encuentro loadLevel. Metodos:\n" + sb);
                        finish(mc);
                        return;
                    }
                    load.invoke(flows, mc.screen, "ciudadela");
                } else if (ticks % 200 == 0) {
                    log("esperando... pantalla=" + (mc.screen == null ? "ninguna" : mc.screen.getClass().getName()));
                }
                return;
            }
            if (inGame < 0) {
                inGame = ticks;
                log("Dentro del mundo");
                plan(mc);
            }
            int t = ticks - inGame;
            while (next < steps.size() && t >= steps.get(next).at) {
                Step s = steps.get(next++);
                log(s.what + "  | fps=" + fps + " | " + mc.levelRenderer.getChunkStatistics() + " | " + mc.getSoundManager().getDebugString());
                s.run.accept(mc);
            }
            reyTick(mc);
            if (cineStart >= 0 && (ticks - cineStart) % 40 == 0 && ticks - cineStart < 20 * 90) {
                log("  cinematica t=" + (ticks - cineStart) / 20 + " s: activa=" + ClientCine.active() + " voz=" + ClientCine.narrationPlaying()
                        + " musica=" + ClientMusic.debug());
            }
        } catch (Throwable ex) {
            log("ERROR: " + ex);
            CiudadelaMod.LOGGER.error("[CI cliente] fallo", ex);
            finish(mc);
        }
    }

    private static void server(Minecraft mc, Consumer<ServerPlayer> r) {
        MinecraftServer srv = mc.getSingleplayerServer();
        if (srv == null) {
            log("sin servidor integrado");
            return;
        }
        srv.execute(() -> {
            try {
                ServerPlayer sp = srv.getPlayerList().getPlayers().get(0);
                r.accept(sp);
            } catch (Throwable ex) {
                log("ERROR en el servidor: " + ex);
                CiudadelaMod.LOGGER.error("[CI cliente] fallo en el servidor", ex);
            }
        });
    }

    private static void shot(Minecraft mc, String name) {
        try {
            Method grab = null;
            for (Method m : net.minecraft.client.Screenshot.class.getMethods())
                if (m.getName().equals("grab") && m.getParameterCount() == 4) grab = m;
            if (grab == null) {
                log("no encuentro Screenshot.grab");
                return;
            }
            Consumer<net.minecraft.network.chat.Component> msg = c -> {};
            grab.invoke(null, mc.gameDirectory, name + ".png", mc.getMainRenderTarget(), msg);
            log("captura " + name);
        } catch (Throwable ex) {
            log("captura " + name + " fallida: " + ex);
        }
    }

    private static void at(int sec, String what, Consumer<Minecraft> r) {
        steps.add(new Step(Math.round(sec * 20f), what, r));
    }

    private static void atTicks(int t, String what, Consumer<Minecraft> r) {
        steps.add(new Step(t, what, r));
    }

    /** El guion de la prueba (en segundos desde que se entra al mundo). */
    private static void plan(Minecraft mc) {
        steps.clear();
        next = 0;
        at(2, "Preparar: modo creativo y a la torre del capitulo 1", m -> server(m, sp -> {
            sp.setGameMode(GameType.CREATIVE);
            ServerLevel lvl = sp.serverLevel();
            long seed = lvl.getSeed();
            int[] s = CastleSites.nearest(seed, 0, 0, 3);
            castle = s;
            CastlePlan plan = PlanCache.get(seed, s[0], s[1], (x, z) -> 64);
            chest1 = plan.storySpot(1);
            log("ciudadela en " + s[0] + "," + s[1] + "; cofre 1 en " + chest1[0] + "," + chest1[1] + "," + chest1[2]);
            sp.teleportTo(lvl, chest1[0] - 2.5, chest1[1], chest1[2] + 0.5, -90f, 12f);
        }));
        at(40, "Captura de la torre", m -> {
            m.options.hideGui = true;
            shot(m, "cliente_01_torre");
        });
        at(42, "Abrir el cofre del capitulo 1 con un clic derecho de verdad", m -> {
            m.options.hideGui = false;
            server(m, sp -> {
                BlockPos p = new BlockPos(chest1[0], chest1[1], chest1[2]);
                sp.gameMode.useItemOn(sp, sp.serverLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(p), Direction.WEST, p, false));
            });
            cineStart = ticks;
        });
        float total = Timeline.total(1);
        float[] when = {1.6f, 9f, 24f, 48f, total - 1.5f};
        for (int i = 0; i < when.length; i++) {
            final int k = i;
            atTicks(Math.round((42 + when[i]) * 20), "Captura de la cinematica t=" + when[i], m -> shot(m, String.format("cliente_%02d_cine_%02d", 2 + k, Math.round(when[k]))));
        }
        int after = Math.round((42 + total + 4) * 20);
        atTicks(after, "Captura del cofre abierto (libro y mapa)", m -> shot(m, "cliente_08_cofre"));
        rey(after + 40);
    }

    // ------------------------------------------------------------------ el Rey

    private static int[] castle;
    private static com.ciudadela.rey.Sala sala;
    private static boolean seguir;
    private static int ataques;

    private static void rey(int B) {
        atTicks(B, "Rey: al gran salon con armadura de netherite y espada afilada", m -> {
            m.setScreen(null);
            server(m, sp -> {
                sp.closeContainer();
                sp.setGameMode(GameType.SURVIVAL);
                ServerLevel lvl = sp.serverLevel();
                sala = com.ciudadela.rey.Sala.near(lvl, castle[0], castle[1]);
                if (sala == null) {
                    log("NO HAY SALA DEL TRONO");
                    return;
                }
                var t = sala.t;
                log("salon del trono " + t.x0() + "," + t.z0() + " - " + t.x1() + "," + t.z1() + " suelo " + t.y() + " alto " + t.h()
                        + " asiento " + t.seatX() + "," + t.seatY() + "," + t.seatZ() + " puertas " + t.doors().size()
                        + " cofre " + t.chestX() + "," + t.chestZ());
                for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[]{
                        net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST,
                        net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET}) {
                    net.minecraft.world.item.Item it = switch (slot) {
                        case HEAD -> net.minecraft.world.item.Items.NETHERITE_HELMET;
                        case CHEST -> net.minecraft.world.item.Items.NETHERITE_CHESTPLATE;
                        case LEGS -> net.minecraft.world.item.Items.NETHERITE_LEGGINGS;
                        default -> net.minecraft.world.item.Items.NETHERITE_BOOTS;
                    };
                    ItemStack a = new ItemStack(it);
                    a.enchant(net.minecraft.world.item.enchantment.Enchantments.ALL_DAMAGE_PROTECTION, 4);
                    sp.setItemSlot(slot, a);
                }
                ItemStack sw = new ItemStack(net.minecraft.world.item.Items.NETHERITE_SWORD);
                sw.enchant(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS, 5);
                sp.setItemInHand(InteractionHand.MAIN_HAND, sw);
                var hp = sp.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
                if (hp != null) hp.setBaseValue(400);
                sp.setHealth(400);
                sp.teleportTo(lvl, t.seatX() + 0.5, t.y(), t.z1() + 4.5, 180f, 0f);
            });
        });
        atTicks(B + 140, "Captura: el salon del trono desde la puerta", m -> {
            m.options.hideGui = true;
            shot(m, "cliente_20_salon");
        });
        atTicks(B + 150, "Entrar en el salon del trono", m -> {
            m.options.hideGui = false;
            server(m, sp -> {
                var t = sala.t;
                sp.teleportTo(sp.serverLevel(), t.seatX() + 0.5, t.y(), t.z1() - 1.5, 180f, 0f);
                var reyes = sp.serverLevel().getEntitiesOfClass(com.ciudadela.rey.Rey.class, sala.box().inflate(4));
                log("reyes en la sala: " + reyes.size() + (reyes.isEmpty() ? "" : " -> " + reyes.get(0).debug()));
            });
            reyStart = ticks;
        });
        int E = B + 150;
        int[] intro = {30, 90, 170, 250, 320, 370};
        for (int i = 0; i < intro.length; i++) {
            final int k = i;
            atTicks(E + intro[i], "Captura de la intro", m -> shot(m, String.format("cliente_%02d_intro_%d", 21 + k, k)));
        }
        int F = E + 380;
        atTicks(F, "Empieza la pelea: camara detras del jugador", m -> {
            m.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
            seguir = true;
        });
        for (int i = 0; i < 6; i++) {
            final int k = i;
            atTicks(F + 20 + i * 60, "Captura fase 1", m -> shot(m, String.format("cliente_%02d_fase1_%d", 30 + k, k)));
        }
        int F2 = F + 400;
        atTicks(F2, "Forzar fase 2", m -> server(m, sp -> reyCerca(sp).ifPresent(r -> r.forzarFase(2))));
        for (int i = 0; i < 6; i++) {
            final int k = i;
            atTicks(F2 + 15 + i * 50, "Captura fase 2", m -> shot(m, String.format("cliente_%02d_fase2_%d", 40 + k, k)));
        }
        int F3 = F2 + 340;
        atTicks(F3, "Forzar fase 3", m -> server(m, sp -> reyCerca(sp).ifPresent(r -> r.forzarFase(3))));
        for (int i = 0; i < 6; i++) {
            final int k = i;
            atTicks(F3 + 15 + i * 50, "Captura fase 3", m -> shot(m, String.format("cliente_%02d_fase3_%d", 50 + k, k)));
        }
        int D = F3 + 340;
        atTicks(D, "Forzar la muerte del Rey", m -> server(m, sp -> reyCerca(sp).ifPresent(com.ciudadela.rey.Rey::forzarMuerte)));
        int[] muerte = {20, 50, 75, 100, 125, 160, 200};
        for (int i = 0; i < muerte.length; i++) {
            final int k = i;
            atTicks(D + muerte[i], "Captura de la muerte", m -> shot(m, String.format("cliente_%02d_muerte_%d", 60 + k, k)));
        }
        atTicks(D + 230, "Abrir el cofre del premio (capitulo 3)", m -> {
            seguir = false;
            m.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
            server(m, sp -> {
                BlockPos p = sala.chest();
                log("cofre del premio: " + sp.serverLevel().getBlockState(p));
                sp.teleportTo(sp.serverLevel(), p.getX() + 0.5, p.getY(), p.getZ() + 2.5, 180f, 30f);
                sp.gameMode.useItemOn(sp, sp.serverLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(p), Direction.SOUTH, p, false));
            });
            cineStart = ticks;
        });
        atTicks(D + 230 + 200, "Captura del cofre del premio", m -> shot(m, "cliente_70_premio"));
        atTicks(D + 230 + 240, "Fin de la prueba", ClientCiTest::finish);
    }

    private static int reyStart = -1;

    private static java.util.Optional<com.ciudadela.rey.Rey> reyCerca(net.minecraft.server.level.ServerPlayer sp) {
        return sp.serverLevel().getEntitiesOfClass(com.ciudadela.rey.Rey.class, sp.getBoundingBox().inflate(120)).stream().findFirst();
    }

    /** Cada tick de la pelea: mirar al rey, atacarle de vez en cuando y anotar como va. */
    private static void reyTick(Minecraft mc) {
        if (reyStart < 0 || mc.level == null || mc.player == null) return;
        int t = ticks - reyStart;
        com.ciudadela.rey.Rey cr = null;
        for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering())
            if (e instanceof com.ciudadela.rey.Rey r) cr = r;
        if (seguir && cr != null) {
            Vec3 d = cr.position().add(0, 1.6, 0).subtract(mc.player.getEyePosition());
            float yaw = (float) (Math.atan2(d.z, d.x) * 180 / Math.PI) - 90f;
            float pitch = (float) -(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)) * 180 / Math.PI);
            mc.player.setYRot(yaw);
            mc.player.setXRot(pitch);
            mc.player.yRotO = yaw;
            mc.player.xRotO = pitch;
        }
        if (t % 20 == 0)
            server(mc, sp -> reyCerca(sp).ifPresentOrElse(r -> log("  rey: " + r.debug() + " | jugador vida " + Math.round(sp.getHealth())
                    + " en " + Math.round(sp.getX()) + "," + Math.round(sp.getY()) + "," + Math.round(sp.getZ())), () -> log("  rey: no esta")));
        // el jugador le ataca (espada de netherite afilada V) cuando esta cerca
        if (seguir && t % 13 == 0)
            server(mc, sp -> reyCerca(sp).ifPresent(r -> {
                if (r.distanceTo(sp) < 4.5 && r.luchando()) {
                    float before = r.getHealth();
                    sp.attack(r);
                    sp.swing(InteractionHand.MAIN_HAND, true);
                    if (++ataques % 5 == 0) log("  golpe del jugador: vida del rey " + Math.round(before) + " -> " + Math.round(r.getHealth()));
                }
                sp.setHealth(sp.getMaxHealth());
            }));
    }

    @SubscribeEvent
    public static void onDamage(net.minecraftforge.event.entity.living.LivingDamageEvent e) {
        if (!ON || !(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer)) return;
        log("  el jugador recibe " + String.format("%.1f", e.getAmount()) + " (" + String.format("%.1f", e.getAmount() / 2) + " corazones) de "
                + e.getSource().getMsgId());
    }

    private static void finish(Minecraft mc) {
        if (done) return;
        done = true;
        log("Cerrando el juego");
        if (log != null) log.close();
        mc.stop();
    }
}
