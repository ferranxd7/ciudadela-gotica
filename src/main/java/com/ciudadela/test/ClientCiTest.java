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
            escTick(mc);
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

    private static int base;

    private static void at(int sec, String what, Consumer<Minecraft> r) {
        steps.add(new Step(base + Math.round(sec * 20f), what, r));
    }

    private static void atTicks(int t, String what, Consumer<Minecraft> r) {
        steps.add(new Step(base + t, what, r));
    }

    // ------------------------------------------------------------------ escaparate: el rey quieto en cada animacion

    private static com.ciudadela.rey.Rey modelo;
    /** Donde se monta el escaparate: lejos de cualquier ciudadela (alli siempre es de noche). */
    private static double ex, ey = 241, ez;

    private static int escaparate() {
        base = 0;
        at(2, "Escaparate: plataforma a mediodia lejos de la isla y el rey", m -> server(m, sp -> {
            sp.setGameMode(GameType.CREATIVE);
            ServerLevel lvl = sp.serverLevel();
            long seed = lvl.getSeed();
            int[] s = CastleSites.nearest(seed, 0, 0, 3);
            int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}};
            int fx = s[0] + 1300, fz = s[1];
            for (int[] d : dirs) {
                int cx = s[0] + d[0] * 1300, cz = s[1] + d[1] * 1300;
                int[] o = CastleSites.nearest(seed, cx, cz, 1);
                if (o == null || Math.hypot(o[0] - cx, o[1] - cz) > 900) {
                    fx = cx;
                    fz = cz;
                    break;
                }
            }
            ex = fx + 0.5;
            ez = fz + 0.5;
            log("escaparate en " + fx + "," + fz + " (ciudadela en " + s[0] + "," + s[1] + ")");
            lvl.setDayTime(6000);
            lvl.setWeatherParameters(6000, 0, false, false);
            int y = (int) ey - 1;
            for (int x = -4; x <= 4; x++)
                for (int z = -4; z <= 4; z++)
                    lvl.setBlockAndUpdate(new BlockPos(fx + x, y, fz + z), net.minecraft.world.level.block.Blocks.WHITE_CONCRETE.defaultBlockState());
            sp.setInvisible(true);
            sp.teleportTo(lvl, ex, ey - 5.0, ez, 0f, 0f);
            sp.getAbilities().flying = true;
            sp.onUpdateAbilities();
            modelo = CiudadelaMod.REY.get().create(lvl);
            modelo.moveTo(ex, ey, ez, 0f, 0f);
            modelo.setYBodyRot(0f);
            modelo.setYHeadRot(0f);
            lvl.addFreshEntity(modelo);
            modelo.muestra(1, com.ciudadela.rey.Rey.POSE_SENTADO, 0, false);
        }));
        escLista = new Object[][]{
                {1, com.ciudadela.rey.Rey.POSE_SENTADO, 0, false, "sentado", 1.5f},
                {1, com.ciudadela.rey.Rey.POSE_SENTADO, 0, true, "habla", 1.5f},
                {1, com.ciudadela.rey.Rey.POSE_PIE, 0, false, "quieto", 1.0f},
                {1, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.LEVANTARSE, false, "levantarse", 0f},
                {1, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.COMBO3, false, "combo3", 0f},
                {1, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.SALTO, false, "salto", 0f},
                {1, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.GIRO, false, "giro", 0f},
                {1, com.ciudadela.rey.Rey.POSE_GUARDIA, 0, false, "guardia", 1.2f},
                {1, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.CONTRAATAQUE, false, "contraataque", 0f},
                {1, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.TAJO_LEJOS, false, "tajo", 0f},
                {2, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.GRITO, false, "grito", 0f},
                {2, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.COMBO5, false, "combo5", 0f},
                {2, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.ABANICO, false, "abanico", 0f},
                {2, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.PILARES, false, "pilares", 0f},
                {2, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.BOLAS, false, "bolas", 0f},
                {2, com.ciudadela.rey.Rey.POSE_CARGA, 0, false, "embestida", 1.2f},
                {3, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.COMBO8, false, "combo8", 0f},
                {3, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.GEISER, false, "geiser", 0f},
                {3, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.GOLPE_FINAL, false, "golpe_final", 0f},
                {3, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.ATURDIDO, false, "aturdido", 0f},
                {3, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.ARRANCAR, false, "arrancar", 0f},
                {3, com.ciudadela.rey.Rey.POSE_PIE, 0, false, "gigante", 2.2f, true},
                {3, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.COMBO8, false, "gigante_combo8", 0f, true},
                {3, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.GEISER, false, "gigante_geiser", 0f, true},
                {3, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.GOLPE_FINAL, false, "gigante_golpe_final", 0f, true},
                {3, com.ciudadela.rey.Rey.POSE_PIE, com.ciudadela.rey.ReyAnims.MUERTE, false, "muerte", 0f, true},
                // proyectiles sueltos, vistos desde arriba: {fase, -1, tipo, ...}
                {0, -1, com.ciudadela.rey.ReyProyectil.TAJO, false, "proy_tajo_dorado", 0f},
                {1, -1, com.ciudadela.rey.ReyProyectil.TAJO, false, "proy_tajo_plata", 0f},
                {2, -1, com.ciudadela.rey.ReyProyectil.TAJO, false, "proy_tajo_fuego", 0f},
                {3, -1, com.ciudadela.rey.ReyProyectil.TAJO, false, "proy_tajo_lava", 0f},
                {2, -1, com.ciudadela.rey.ReyProyectil.BOLA, false, "proy_bola", 0f},
                {3, -1, com.ciudadela.rey.ReyProyectil.BOLA, false, "proy_bola_lava", 0f},
        };
        // el servidor integrado puede ir con retraso (genera terreno nuevo): cada pose se pide y se espera a que el
        // cliente la tenga antes de contar el tiempo de las capturas (lo lleva escTick, tick a tick)
        at(13, "Escaparate: empieza", m -> {
            escI = 0;
            escEstado = 0;
            escN = 0;
        });
        int fin = 13 * 20 + ESC_TICKS;
        atTicks(fin, "Fin del escaparate", m -> {
            if (escI >= 0) log("  escaparate sin terminar: se queda en " + escI + " de " + escLista.length);
            escI = -1;
            m.setCameraEntity(m.player);
            m.options.hideGui = false;
            server(m, sp -> {
                if (modelo != null) modelo.discard();
                modelo = null;
                sp.setInvisible(false);
            });
        });
        return fin + 20;
    }

    private static final int ESC_TICKS = 20 * 115;
    private static Object[][] escLista;
    private static int escI = -1, escEstado, escDesde, escK, escN;

    private static void escTick(Minecraft mc) {
        if (escI < 0 || escLista == null || mc.level == null) return;
        if (escI >= escLista.length) {
            log("  escaparate terminado");
            escI = -1;
            return;
        }
        Object[] e = escLista[escI];
        final int fase = (Integer) e[0], pose = (Integer) e[1], anim = (Integer) e[2];
        final boolean habla = (Boolean) e[3];
        final String name = (String) e[4];
        float len = anim > 0 ? com.ciudadela.rey.ReyAnims.TICKS[anim] / 20f : (Float) e[5];
        final boolean grande = e.length > 6 && (Boolean) e[6];
        if (pose == -1) {
            proyTick(mc, fase, anim, name);
            return;
        }
        float[] fr = anim > 0 ? new float[]{0.15f, 0.35f, 0.55f, 0.8f} : new float[]{1.0f};
        switch (escEstado) {
            case 0 -> {
                mc.options.hideGui = true;
                if (grande) verDesde(mc, new Vec3(ex + 5.2, ey + 3.4, ez + 8.8), new Vec3(ex, ey + 3.0, ez));
                else verDesde(mc, new Vec3(ex + 2.6, ey + 1.9, ez + 4.4), new Vec3(ex, ey + 1.5, ez));
                server(mc, sp -> {
                    if (modelo != null) {
                        modelo.muestraTransformado(grande);
                        modelo.muestra(fase, pose, anim, habla);
                        modelo.moveTo(ex, ey, ez, 0f, 0f);
                        modelo.setYBodyRot(0f);
                        modelo.setYHeadRot(0f);
                    }
                });
                escEstado = 1;
                escDesde = ticks;
            }
            case 1 -> {
                com.ciudadela.rey.Rey cr = null;
                if (modelo != null && mc.level.getEntity(modelo.getId()) instanceof com.ciudadela.rey.Rey r) cr = r;
                boolean ok = cr != null && cr.fase() == fase && cr.pose() == pose && cr.flag(com.ciudadela.rey.Rey.F_HABLA) == habla
                        && cr.actionId() == anim && (escI > 0 || ticks - escDesde > 100);
                if (ok) {
                    escEstado = 2;
                    escDesde = ticks;
                    escK = 0;
                    log("  escaparate: " + name + " en el cliente");
                } else if (ticks - escDesde > 200) {
                    log("  escaparate: " + name + " no llega al cliente (" + (cr == null ? "sin rey" : "fase " + cr.fase() + " pose " + cr.pose() + " accion " + cr.actionId()) + ")");
                    escI++;
                    escEstado = 0;
                }
            }
            default -> {
                if (escK < fr.length) {
                    int when = Math.round((anim > 0 ? len * fr[escK] : len) * 20);
                    if (ticks - escDesde >= when) {
                        shot(mc, String.format("esc_%02d_%s_%d", escN++, name, Math.round(fr[escK] * 100)));
                        escK++;
                    }
                } else {
                    escI++;
                    escEstado = 0;
                }
            }
        }
    }

    private static int proyId = -1;

    /** Un proyectil del Rey cruzando por delante de la camara (desde arriba, para ver la media luna). */
    private static void proyTick(Minecraft mc, int fase, int tipo, String name) {
        int[] when = tipo == com.ciudadela.rey.ReyProyectil.TAJO ? new int[]{5, 13, 22, 33} : new int[]{8, 20, 34, 50};
        switch (escEstado) {
            case 0 -> {
                mc.options.hideGui = true;
                verDesde(mc, new Vec3(ex + 0.5, ey + 4.6, ez + 6.2), new Vec3(ex, ey + 1.1, ez));
                proyId = -1;
                server(mc, sp -> {
                    if (modelo != null) modelo.moveTo(ex, ey - 40, ez, 0f, 0f);
                    ServerLevel lvl = sp.serverLevel();
                    com.ciudadela.rey.ReyProyectil pr = new com.ciudadela.rey.ReyProyectil(CiudadelaMod.REY_PROYECTIL.get(), lvl);
                    double v = tipo == com.ciudadela.rey.ReyProyectil.TAJO ? 0.26 : 0.16;
                    pr.moveTo(ex - 4.2, ey + 1.2, ez);
                    pr.preparar(tipo, new Vec3(v, 0, 0), 0f, fase);
                    lvl.addFreshEntity(pr);
                    proyId = pr.getId();
                });
                escEstado = 1;
                escDesde = ticks;
            }
            case 1 -> {
                if (proyId >= 0 && mc.level.getEntity(proyId) != null) {
                    escEstado = 2;
                    escDesde = ticks;
                    escK = 0;
                    log("  escaparate: " + name + " en el cliente");
                } else if (ticks - escDesde > 200) {
                    log("  escaparate: " + name + " no llega al cliente");
                    escI++;
                    escEstado = 0;
                }
            }
            default -> {
                if (escK < when.length) {
                    if (ticks - escDesde >= when[escK]) {
                        shot(mc, String.format("esc_%02d_%s_%d", escN++, name, when[escK]));
                        escK++;
                    }
                } else if (ticks - escDesde > when[when.length - 1] + 30) {
                    escI++;
                    escEstado = 0;
                }
            }
        }
    }

    // ------------------------------------------------------------------ armas del rey: golpes, combos y cubrirse

    private static final java.util.List<net.minecraft.world.entity.LivingEntity> dianas = new java.util.concurrent.CopyOnWriteArrayList<>();

    private static String estadoArmas(ServerPlayer sp) {
        StringBuilder b = new StringBuilder("energia=" + com.ciudadela.armas.Combos.energia(sp)
                + " (cliente " + com.ciudadela.client.armas.ArmasCliente.energiaCliente() + ")"
                + " forma=" + com.ciudadela.armas.EspadaRey.forma(sp.getMainHandItem())
                + "/" + com.ciudadela.armas.EspadaRey.forma(sp.getOffhandItem())
                + " jugador=" + String.format("%.1f", sp.getHealth())
                + " en " + String.format("%.1f,%.1f,%.1f", sp.getX() - ex, sp.getY() - ey, sp.getZ() - ez) + " | dianas:");
        for (net.minecraft.world.entity.LivingEntity d : dianas)
            b.append(' ').append(Math.round(d.getHealth())).append(d.isOnFire() ? "(arde)" : "");
        return b.toString();
    }

    private static void golpe(Minecraft m, int i) {
        if (dianas.isEmpty()) return;
        net.minecraft.world.entity.Entity c = m.level.getEntity(dianas.get(i % dianas.size()).getId());
        if (c == null) {
            log("  la diana " + i + " no esta en el cliente");
            return;
        }
        m.gameMode.attack(m.player, c);
        m.player.swing(InteractionHand.MAIN_HAND);
    }

    /** Shift (o Ctrl) + clic izquierdo, por el mismo evento que usa el juego al pulsar el raton. */
    private static void combo(Minecraft m, boolean ctrl) {
        net.minecraft.client.KeyMapping k = ctrl ? m.options.keySprint : m.options.keyShift;
        k.setDown(true);
        try {
            var ev = new net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered(0, m.options.keyAttack, InteractionHand.MAIN_HAND);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(ev);
            log("  evento de ataque: cancelado=" + ev.isCanceled() + " (cancelado = el combo se pidio al servidor)");
        } finally {
            k.setDown(false);
        }
    }

    private static void desdeElLado(Minecraft m) {
        m.options.hideGui = true;
        verDesde(m, new Vec3(ex + 6.2, ey + 2.4, ez - 2.4), new Vec3(ex, ey + 1.0, ez - 1.9));
    }

    private static void enPrimeraPersona(Minecraft m) {
        m.setCameraEntity(m.player);
        m.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
        m.options.hideGui = false;
    }

    private static void logArmas(Minecraft m, String what) {
        server(m, sp -> log("  armas " + what + ": " + estadoArmas(sp)));
    }

    private static int armas(int t0) {
        base = 0;
        atTicks(t0, "Armas: supervivencia con las dos espadas del Rey y tres dianas", m -> {
            enPrimeraPersona(m);
            server(m, sp -> {
                ServerLevel lvl = sp.serverLevel();
                int y = (int) ey - 1, cx = (int) Math.floor(ex), cz = (int) Math.floor(ez);
                for (int x = -9; x <= 9; x++)
                    for (int z = -9; z <= 9; z++)
                        lvl.setBlockAndUpdate(new BlockPos(cx + x, y, cz + z), net.minecraft.world.level.block.Blocks.WHITE_CONCRETE.defaultBlockState());
                sp.setInvisible(false);
                sp.setGameMode(GameType.SURVIVAL);
                sp.getInventory().clearContent();
                sp.getInventory().selected = 0;
                sp.getInventory().items.set(0, new ItemStack(CiudadelaMod.ESPADA_DIESTRA.get()));
                sp.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(CiudadelaMod.ESPADA_SINIESTRA.get()));
                sp.setHealth(sp.getMaxHealth());
                sp.getFoodData().setFoodLevel(20);
                sp.teleportTo(lvl, ex, ey, ez - 3.5, 0f, 10f);
                dianas.clear();
                for (int i = 0; i < 3; i++) {
                    net.minecraft.world.entity.monster.Husk h = net.minecraft.world.entity.EntityType.HUSK.create(lvl);
                    if (h == null) continue;
                    h.moveTo(ex + (i - 1) * 1.5, ey, ez - 0.6 + (i == 1 ? 0.5 : 0), 180f, 0f);
                    h.setYHeadRot(180f);
                    h.setYBodyRot(180f);
                    h.setNoAi(true);
                    h.setPersistenceRequired();
                    h.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(600);
                    h.setHealth(600);
                    h.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
                    lvl.addFreshEntity(h);
                    dianas.add(h);
                }
                log("  armas al empezar: " + estadoArmas(sp));
            });
        });
        int t = t0 + 50;
        for (int i = 0; i < 5; i++) {
            final int k = i;
            atTicks(t, "golpe normal " + (i + 1), m -> golpe(m, k));
            t += 15;
        }
        atTicks(t, "capturas: primera persona con la barra de energia", m -> shot(m, "cliente_81_espadas"));
        logArmasAt(t + 1, "tras 5 golpes (deberia haber 40)");
        t += 6;
        atTicks(t, "Ctrl + clic sin energia suficiente (no debe salir)", m -> combo(m, true));
        atTicks(t + 3, "captura del aviso de energia", m -> shot(m, "cliente_82_sin_energia"));
        logArmasAt(t + 10, "tras pedir el Juicio sin energia");
        t += 16;
        atTicks(t, "Shift + clic izquierdo: Tajo Real", m -> {
            desdeElLado(m);
            combo(m, false);
        });
        int[] tr = {4, 10, 16, 22, 27};
        for (int i = 0; i < tr.length; i++) {
            final int k = i;
            atTicks(t + tr[i], "captura del Tajo Real", m -> shot(m, String.format("cliente_%02d_tajo_real_%d", 83 + k, tr[k])));
        }
        logArmasAt(t + 12, "en mitad del Tajo Real");
        logArmasAt(t + 40, "tras el Tajo Real");
        t += 46;
        atTicks(t, "de vuelta a su sitio", m -> {
            enPrimeraPersona(m);
            server(m, sp -> sp.teleportTo(sp.serverLevel(), ex, ey, ez - 3.5, 0f, 10f));
        });
        t += 6;
        for (int i = 0; i < 10; i++) {
            final int k = i;
            atTicks(t, "golpe normal " + (i + 6), m -> golpe(m, k));
            t += 15;
        }
        atTicks(t, "captura: energia para el Juicio", m -> shot(m, "cliente_88_energia_llena"));
        logArmasAt(t + 1, "tras 10 golpes mas");
        t += 6;
        atTicks(t, "Ctrl + clic izquierdo: Juicio del Rey", m -> {
            desdeElLado(m);
            combo(m, true);
        });
        int[] ju = {8, 16, 26, 36, 45, 49};
        for (int i = 0; i < ju.length; i++) {
            final int k = i;
            atTicks(t + ju[i], "captura del Juicio", m -> shot(m, String.format("cliente_%02d_juicio_%d", 89 + k, ju[k])));
        }
        logArmasAt(t + 30, "en mitad del Juicio");
        logArmasAt(t + 62, "tras el Juicio");
        t += 70;
        atTicks(t, "Cubrirse: clic derecho mantenido", m -> {
            server(m, sp -> {
                sp.teleportTo(sp.serverLevel(), ex, ey, ez - 3.5, 0f, 10f);
                sp.setHealth(sp.getMaxHealth());
            });
            enPrimeraPersona(m);
            m.options.keyUse.setDown(true);
            m.gameMode.useItem(m.player, InteractionHand.MAIN_HAND);
        });
        atTicks(t + 12, "captura cubriendose (primera persona)", m -> shot(m, "cliente_95_bloqueo"));
        atTicks(t + 14, "golpe de frente mientras se cubre", m -> server(m, sp -> {
            sp.invulnerableTime = 0;
            float h0 = sp.getHealth();
            boolean usando = sp.isUsingItem() && com.ciudadela.armas.EspadaRey.es(sp.getUseItem());
            if (!dianas.isEmpty()) sp.hurt(sp.damageSources().mobAttack(dianas.get(1 % dianas.size())), 10f);
            log("  cubriendose=" + usando + " (cliente " + (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isUsingItem())
                    + "): golpe de 10 de frente -> quita " + String.format("%.1f", h0 - sp.getHealth()));
        }));
        atTicks(t + 18, "captura cubriendose (desde delante)", m -> {
            m.options.hideGui = true;
            verDesde(m, new Vec3(ex + 1.3, ey + 1.7, ez - 1.5), new Vec3(ex, ey + 1.1, ez - 3.5));
        });
        atTicks(t + 21, "captura", m -> shot(m, "cliente_96_bloqueo_fuera"));
        atTicks(t + 26, "dejar de cubrirse", m -> {
            m.options.keyUse.setDown(false);
            enPrimeraPersona(m);
        });
        atTicks(t + 50, "golpe de frente sin cubrirse", m -> server(m, sp -> {
            sp.invulnerableTime = 0;
            float h0 = sp.getHealth();
            boolean usando = sp.isUsingItem();
            if (!dianas.isEmpty()) sp.hurt(sp.damageSources().mobAttack(dianas.get(1 % dianas.size())), 10f);
            log("  cubriendose=" + usando + ": golpe de 10 de frente -> quita " + String.format("%.1f", h0 - sp.getHealth()));
        }));
        t += 60;
        atTicks(t, "Fin de las armas: se quitan las dianas", m -> server(m, sp -> {
            log("  armas al final: " + estadoArmas(sp));
            for (net.minecraft.world.entity.LivingEntity d : dianas) d.discard();
            dianas.clear();
            sp.getInventory().clearContent();
            sp.setHealth(sp.getMaxHealth());
            sp.setGameMode(GameType.CREATIVE);
        }));
        return t + 20;
    }

    private static void logArmasAt(int t, String what) {
        atTicks(t, "estado de las armas " + what, m -> logArmas(m, what));
    }

    /** El guion de la prueba (en segundos desde que se entra al mundo). */
    private static void plan(Minecraft mc) {
        steps.clear();
        next = 0;
        int fin = escaparate();
        fin = armas(fin);
        base = fin;
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
        // por orden de tiempo (cada parte apunta sus pasos a su manera); el orden de los empates se conserva
        steps.sort(java.util.Comparator.comparingInt(Step::at));
    }

    // ------------------------------------------------------------------ el Rey

    private static int[] castle;
    private static com.ciudadela.rey.Sala sala;
    private static boolean seguir;
    private static int ataques;

    private static void rey(int B) {
        atTicks(B, "Rey: al gran salon con armadura de netherite y espada afilada (dificultad normal)", m -> {
            m.setScreen(null);
            server(m, sp -> {
                sp.closeContainer();
                sp.setGameMode(GameType.SURVIVAL);
                sp.getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL, true);
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
        atTicks(B + 130, "Captura: el salon del trono desde la puerta", m -> {
            m.options.hideGui = true;
            shot(m, "cliente_19_salon");
        });
        atTicks(B + 134, "Camara delante del trono", m -> {
            var t = sala.t;
            verDesde(m, new Vec3(t.seatX() + 0.5, t.y() + 4.6, t.seatZ() + 5.5), new Vec3(t.seatX() + 0.5, t.y() + 4.6, t.seatZ() + 0.5));
        });
        atTicks(B + 150, "Captura: el rey sentado en su trono", m -> shot(m, "cliente_20_rey_trono"));
        atTicks(B + 152, "Entrar en el salon del trono", m -> {
            m.setCameraEntity(m.player);
            m.options.hideGui = false;
            server(m, sp -> {
                var t = sala.t;
                sp.teleportTo(sp.serverLevel(), t.seatX() + 0.5, t.y(), t.z1() - 1.5, 180f, 0f);
                var reyes = sp.serverLevel().getEntitiesOfClass(com.ciudadela.rey.Rey.class, sala.box().inflate(4));
                log("reyes en la sala: " + reyes.size() + (reyes.isEmpty() ? "" : " -> " + reyes.get(0).debug()));
            });
            reyStart = ticks;
        });
        int E = B + 152;
        int[] intro = {50, 110, 170, 240, 300, 330};
        for (int i = 0; i < intro.length; i++) {
            final int k = i;
            atTicks(E + intro[i], "Captura de la intro", m -> shot(m, String.format("cliente_%02d_intro_%d", 21 + k, k)));
        }
        int F = E + 400;
        atTicks(F, "Empieza la pelea: camara de observador", m -> {
            m.options.hideGui = true;
            seguir = true;
        });
        for (int i = 0; i < 6; i++) {
            final int k = i;
            atTicks(F + 20 + i * 55, "Captura fase 1", m -> {
                m.options.hideGui = k != 2;
                shot(m, String.format("cliente_%02d_fase1_%d", 30 + k, k));
            });
        }
        int F2 = F + 360;
        atTicks(F2, "Forzar fase 2", m -> server(m, sp -> reyCerca(sp).ifPresent(r -> r.forzarFase(2))));
        for (int i = 0; i < 6; i++) {
            final int k = i;
            atTicks(F2 + 15 + i * 50, "Captura fase 2", m -> {
                m.options.hideGui = k != 1;
                shot(m, String.format("cliente_%02d_fase2_%d", 40 + k, k));
            });
        }
        int F3 = F2 + 330;
        atTicks(F3, "Forzar fase 3 (a los volcanes)", m -> server(m, sp -> reyCerca(sp).ifPresent(r -> r.forzarFase(3))));
        int[] v = {40, 110, 175, 222, 262, 292, 400, 480};
        for (int i = 0; i < v.length; i++) {
            final int k = i;
            atTicks(F3 + v[i], "Captura fase 3", m -> {
                m.options.hideGui = k < 5 ? false : k != 6;
                shot(m, String.format("cliente_%02d_fase3_%d", 50 + k, k));
            });
        }
        int D = F3 + 540;
        atTicks(D, "Forzar la muerte del Rey", m -> server(m, sp -> reyCerca(sp).ifPresent(com.ciudadela.rey.Rey::forzarMuerte)));
        int[] muerte = {20, 70, 100, 125, 133, 142, 175, 230};
        for (int i = 0; i < muerte.length; i++) {
            final int k = i;
            atTicks(D + muerte[i], "Captura de la muerte", m -> {
                m.options.hideGui = false;
                shot(m, String.format("cliente_%02d_muerte_%d", 60 + k, k));
            });
        }
        atTicks(D + 260, "Abrir el cofre del premio (capitulo 3)", m -> {
            seguir = false;
            m.setCameraEntity(m.player);
            m.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
            server(m, sp -> {
                BlockPos p = sala.chest();
                log("cofre del premio: " + sp.serverLevel().getBlockState(p) + " | jugador en " + sp.level().dimension().location());
                sp.teleportTo(sp.getServer().overworld(), p.getX() + 0.5, p.getY(), p.getZ() + 2.5, 180f, 30f);
                sp.gameMode.useItemOn(sp, sp.serverLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(p), Direction.SOUTH, p, false));
            });
            cineStart = ticks;
        });
        atTicks(D + 260 + 210, "Captura del cofre del premio", m -> shot(m, "cliente_70_premio"));
        sello(D + 260 + 230);
    }

    // ------------------------------------------------------------------ el sello de la sala del arbol

    private static int[] hall;

    private static void sello(int S) {
        atTicks(S, "Torre del arbol: abajo del todo, mirando al tubo del ascensor y la escalera", m -> {
            m.setScreen(null);
            m.setCameraEntity(m.player);
            m.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
            m.options.hideGui = true;
            server(m, sp -> {
                sp.closeContainer();
                sp.getAbilities().flying = false;
                sp.onUpdateAbilities();
                ServerLevel lvl = sp.getServer().overworld();
                CastlePlan plan = PlanCache.get(lvl.getSeed(), castle[0], castle[1], (x, z) -> 64);
                hall = plan.treeHall();
                log("sala del arbol en " + hall[0] + "," + hall[2] + "," + hall[1] + " radio " + hall[3] + "; la torre empieza en y=" + hall[4]);
                sp.teleportTo(lvl, hall[0] + 0.5, hall[4], hall[1] + 10.5, 180f, -32f);
            });
        });
        atTicks(S + 70, "Captura: abajo de la torre; al disco", m -> {
            shot(m, "cliente_71_torre_abajo");
            server(m, sp -> {
                var a = com.ciudadela.arbol.TorreArbol.ascensor(sp.serverLevel(), hall);
                var d = com.ciudadela.arbol.TorreArbol.puerta(sp.serverLevel(), hall);
                log("  ascensor: " + (a == null ? "NO ESTA" : "estado " + a.estado() + " y=" + a.getY()) + " | puerta: " + (d == null ? "NO ESTA" : "estado " + d.estado()));
                sp.teleportTo(sp.serverLevel(), hall[0] + 0.5, hall[4] + 0.1, hall[1] + com.ciudadela.gen.TreeTower.SHAFT_Z + 0.5, 180f, 0f);
            });
        });
        atTicks(S + 135, "Captura: el disco arranca", m -> {
            server(m, sp -> log("  jugador montado: " + (sp.getVehicle() != null ? sp.getVehicle().getClass().getSimpleName() : "no") + " y=" + String.format("%.1f", sp.getY())));
            m.player.setXRot(35f);
            shot(m, "cliente_72_ascensor_0");
        });
        atTicks(S + 260, "Captura: a media altura del tubo", m -> {
            m.player.setYRot(110f);
            m.player.setXRot(-5f);
            server(m, sp -> log("  subiendo: y=" + String.format("%.1f", sp.getY()) + " montado=" + (sp.getVehicle() != null)));
            shot(m, "cliente_73_ascensor_1");
        });
        atTicks(S + 440, "Llegada a la recepcion", m -> {
            m.player.setYRot(180f);
            m.player.setXRot(4f);
            server(m, sp -> log("  arriba: y=" + String.format("%.2f", sp.getY()) + " (suelo " + hall[2] + ") montado=" + (sp.getVehicle() != null)
                    + " en el suelo=" + sp.onGround()));
        });
        atTicks(S + 450, "Captura: la recepcion", m -> shot(m, "cliente_74_recepcion"));
        atTicks(S + 460, "A la puerta con la llave", m -> server(m, sp -> {
            sp.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(CiudadelaMod.LLAVE_ARBOL.get()));
            sp.teleportTo(sp.serverLevel(), hall[0] + 0.5, hall[2], hall[1] - 10.5, 180f, -6f);
        }));
        atTicks(S + 490, "Usar la llave en la puerta", m -> {
            m.options.hideGui = false;
            server(m, sp -> {
                var d = com.ciudadela.arbol.TorreArbol.puerta(sp.serverLevel(), hall);
                log("  puerta antes: " + (d == null ? "NO ESTA" : "estado " + d.estado()) + " barrera: "
                        + sp.serverLevel().getBlockState(new BlockPos(hall[0], hall[2] + 2, hall[1] - 15)));
                if (d != null) log("  usar la llave: " + d.interact(sp, InteractionHand.MAIN_HAND));
            });
        });
        int[] w = {15, 45, 75, 105, 140};
        for (int i = 0; i < w.length; i++) {
            final int k = i;
            atTicks(S + 490 + w[i], "Captura de la puerta", m -> shot(m, String.format("cliente_%02d_puerta_%d", 75 + k, k)));
        }
        atTicks(S + 490 + 190, "Captura: la sala del arbol abierta", m -> {
            server(m, sp -> {
                var d = com.ciudadela.arbol.TorreArbol.puerta(sp.serverLevel(), hall);
                log("  puerta despues: " + (d == null ? "NO ESTA" : "estado " + d.estado()) + " barrera: "
                        + sp.serverLevel().getBlockState(new BlockPos(hall[0], hall[2] + 2, hall[1] - 15)));
            });
            m.options.hideGui = true;
            verDesde(m, new Vec3(hall[0] + 0.5, hall[2] + 3.2, hall[1] - 9.0), new Vec3(hall[0] + 0.5, hall[2] + 6, hall[1] - 38));
        });
        atTicks(S + 490 + 196, "captura", m -> shot(m, "cliente_80_sala_arbol"));
        atTicks(S + 490 + 200, "Desde el balcon", m -> verDesde(m, new Vec3(hall[0] + 35.5, hall[2] + 15.2, hall[1] + 14.5),
                new Vec3(hall[0], hall[2] + 14, hall[1] - 4)));
        atTicks(S + 490 + 206, "captura", m -> shot(m, "cliente_97_balcon"));
        atTicks(S + 490 + 210, "Hacia el trono", m -> verDesde(m, new Vec3(hall[0] + 9.5, hall[2] + 5, hall[1] - 19),
                new Vec3(hall[0] + 0.5, hall[2] + 10, hall[1] - 40)));
        atTicks(S + 490 + 216, "captura", m -> shot(m, "cliente_98_trono"));
        atTicks(S + 490 + 220, "La puerta desde la sala", m -> verDesde(m, new Vec3(hall[0] + 7.5, hall[2] + 4, hall[1] - 30),
                new Vec3(hall[0] + 0.5, hall[2] + 8, hall[1] - 14)));
        atTicks(S + 490 + 226, "captura", m -> {
            shot(m, "cliente_96_puerta_desde_sala");
            m.setCameraEntity(m.player);
        });
        S = S + 490 + 230 - 280;
        atTicks(S + 130 + 150, "Logros conseguidos", m -> server(m, sp -> {
            StringBuilder b = new StringBuilder("  logros:");
            for (String id : new String[]{com.ciudadela.story.Logros.ENCONTRAR, com.ciudadela.story.Logros.CAPITULO1,
                    com.ciudadela.story.Logros.REY, com.ciudadela.story.Logros.ARBOL})
                b.append(' ').append(id).append('=').append(com.ciudadela.story.Logros.tiene(sp, id) ? "SI" : "no");
            log(b.toString());
        }));
        atTicks(S + 130 + 160, "Pantalla de logros", m -> {
            m.setCameraEntity(m.player);
            m.options.hideGui = false;
            var ca = m.player.connection.getAdvancements();
            m.setScreen(new net.minecraft.client.gui.screens.advancements.AdvancementsScreen(ca));
            var raiz = ca.getAdvancements().get(new net.minecraft.resources.ResourceLocation("ciudadela", "raiz"));
            log("  pestana de logros: " + (raiz != null ? "esta" : "NO ESTA"));
            if (raiz != null) ca.setSelectedTab(raiz, true);
        });
        atTicks(S + 130 + 166, "captura de los logros", m -> shot(m, "cliente_99_logros"));
        atTicks(S + 130 + 170, "cerrar", m -> m.setScreen(null));
        atTicks(S + 130 + 175, "Fin de la prueba", ClientCiTest::finish);
    }

    // ------------------------------------------------------------------ camara de observador

    private static com.ciudadela.client.CineCamera obs;
    private static Vec3 obsPos;

    private static void verDesde(Minecraft mc, Vec3 pos, Vec3 look) {
        if (obs == null || obs.level() != mc.level) obs = new com.ciudadela.client.CineCamera(mc.level);
        Vec3 d = look.subtract(pos);
        float yaw = (float) (Math.atan2(d.z, d.x) * 180 / Math.PI) - 90f;
        float pitch = (float) -(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)) * 180 / Math.PI);
        obs.moveTo(pos.x, pos.y - com.ciudadela.client.CineCamera.EYE, pos.z, yaw, pitch);
        obs.setOldPosAndRot();
        mc.setCameraEntity(obs);
        mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
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
        if (obs != null && obs.level() != mc.level && mc.getCameraEntity() == obs) mc.setCameraEntity(mc.player);
        if (seguir && cr != null && !ClientCine.active()) {
            Vec3 b = cr.position(), pp = mc.player.position();
            Vec3 d = new Vec3(b.x - pp.x, 0, b.z - pp.z);
            if (d.lengthSqr() < 0.01) d = new Vec3(0, 0, 1);
            d = d.normalize();
            Vec3 side = new Vec3(-d.z, 0, d.x);
            Vec3 mid = b.add(pp).scale(0.5);
            Vec3 want = mid.add(side.scale(8.5)).subtract(d.scale(2.5)).add(0, 3.6, 0);
            if (sala != null && mc.level.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
                var tt = sala.t;
                want = new Vec3(Math.max(tt.x0() + 1.5, Math.min(tt.x1() - 0.5, want.x)), Math.min(tt.y() + tt.h() - 2, want.y),
                        Math.max(tt.z0() + 1.5, Math.min(tt.z1() - 0.5, want.z)));
            }
            obsPos = obsPos == null || obsPos.distanceToSqr(want) > 400 ? want : obsPos.add(want.subtract(obsPos).scale(0.12));
            verDesde(mc, obsPos, mid.add(0, 1.4, 0));
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
