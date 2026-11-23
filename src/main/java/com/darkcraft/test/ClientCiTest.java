package com.darkcraft.test;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.cine.Cinematics;
import com.darkcraft.cine.Timeline;
import com.darkcraft.client.ClientCine;
import com.darkcraft.client.ClientMusic;
import com.darkcraft.gen.CastlePlan;
import com.darkcraft.world.CastleSites;
import com.darkcraft.world.PlanCache;
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
 * Prueba automatica del CLIENTE (solo en GitHub Actions, con -Ddarkcraft.ciClient=true): abre el mundo
 * que genero la prueba del servidor, va a sitios concretos, saca capturas y anota si suenan la voz y la
 * musica (el sonido se graba aparte en un WAV). Al acabar cierra el juego.
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID, value = Dist.CLIENT)
public final class ClientCiTest {
    private ClientCiTest() {}

    public static final boolean ON = Boolean.getBoolean("darkcraft.ciClient");
    private static final int LIMIT = 20 * 60 * 18;

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
            DarkCraftMod.LOGGER.error("[CI cliente] no se puede escribir el informe", ex);
        }
        DarkCraftMod.LOGGER.info("[CI cliente] {}", s);
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
                    log("Pantalla de titulo: abriendo el mundo 'darkcraft'");
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
                    load.invoke(flows, mc.screen, "darkcraft");
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
            gavranTick(mc);
            escTick(mc);
            if (cineStart >= 0 && (ticks - cineStart) % 40 == 0 && ticks - cineStart < 20 * 90) {
                log("  cinematica t=" + (ticks - cineStart) / 20 + " s: activa=" + ClientCine.active() + " voz=" + ClientCine.narrationPlaying()
                        + " musica=" + ClientMusic.debug());
            }
        } catch (Throwable ex) {
            log("ERROR: " + ex);
            DarkCraftMod.LOGGER.error("[CI cliente] fallo", ex);
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
                DarkCraftMod.LOGGER.error("[CI cliente] fallo en el servidor", ex);
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

    private static com.darkcraft.rey.Rey modelo;
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
            log("escaparate en " + fx + "," + fz + " (darkcraft en " + s[0] + "," + s[1] + ")");
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
            modelo = DarkCraftMod.REY.get().create(lvl);
            modelo.moveTo(ex, ey, ez, 0f, 0f);
            modelo.setYBodyRot(0f);
            modelo.setYHeadRot(0f);
            lvl.addFreshEntity(modelo);
            modelo.muestra(1, com.darkcraft.rey.Rey.POSE_SENTADO, 0, false);
            modeloG = DarkCraftMod.GAVRAN.get().create(lvl);
            modeloG.moveTo(ex, ey - 40, ez, 0f, 0f);
            lvl.addFreshEntity(modeloG);
            modeloG.muestra(com.darkcraft.gavran.Gavran.POSE_POSADO, 0, false);
        }));
        escLista = new Object[][]{
                {1, com.darkcraft.rey.Rey.POSE_SENTADO, 0, false, "sentado", 1.5f},
                {1, com.darkcraft.rey.Rey.POSE_SENTADO, 0, true, "habla", 1.5f},
                {1, com.darkcraft.rey.Rey.POSE_PIE, 0, false, "quieto", 1.0f},
                {1, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.LEVANTARSE, false, "levantarse", 0f},
                {1, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.COMBO3, false, "combo3", 0f},
                {1, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.SALTO, false, "salto", 0f},
                {1, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.GIRO, false, "giro", 0f},
                {1, com.darkcraft.rey.Rey.POSE_GUARDIA, 0, false, "guardia", 1.2f},
                {1, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.CONTRAATAQUE, false, "contraataque", 0f},
                {1, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.TAJO_LEJOS, false, "tajo", 0f},
                {2, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.GRITO, false, "grito", 0f},
                {2, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.COMBO5, false, "combo5", 0f},
                {2, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.ABANICO, false, "abanico", 0f},
                {2, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.PILARES, false, "pilares", 0f},
                {2, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.BOLAS, false, "bolas", 0f},
                {2, com.darkcraft.rey.Rey.POSE_CARGA, 0, false, "embestida", 1.2f},
                {3, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.COMBO8, false, "combo8", 0f},
                {3, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.GEISER, false, "geiser", 0f},
                {3, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.GOLPE_FINAL, false, "golpe_final", 0f},
                {3, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.ATURDIDO, false, "aturdido", 0f},
                {3, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.ARRANCAR, false, "arrancar", 0f},
                {3, com.darkcraft.rey.Rey.POSE_PIE, 0, false, "gigante", 2.2f, true},
                {3, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.COMBO8, false, "gigante_combo8", 0f, true},
                {3, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.GEISER, false, "gigante_geiser", 0f, true},
                {3, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.GOLPE_FINAL, false, "gigante_golpe_final", 0f, true},
                {3, com.darkcraft.rey.Rey.POSE_PIE, com.darkcraft.rey.ReyAnims.MUERTE, false, "muerte", 0f, true},
                // proyectiles sueltos, vistos desde arriba: {fase, -1, tipo, ...}
                {0, -1, com.darkcraft.rey.ReyProyectil.TAJO, false, "proy_tajo_dorado", 0f},
                {1, -1, com.darkcraft.rey.ReyProyectil.TAJO, false, "proy_tajo_plata", 0f},
                {2, -1, com.darkcraft.rey.ReyProyectil.TAJO, false, "proy_tajo_fuego", 0f},
                {3, -1, com.darkcraft.rey.ReyProyectil.TAJO, false, "proy_tajo_lava", 0f},
                {2, -1, com.darkcraft.rey.ReyProyectil.TAJO, false, "proy_tajo_lado", 0f},
                {2, -1, com.darkcraft.rey.ReyProyectil.BOLA, false, "proy_bola", 0f},
                {3, -1, com.darkcraft.rey.ReyProyectil.BOLA, false, "proy_bola_lava", 0f},
                // Ser Gavran (fase 10 = Gavran en el escaparate)
                {10, com.darkcraft.gavran.Gavran.POSE_POSADO, 0, false, "g_posado", 1.5f},
                {10, com.darkcraft.gavran.Gavran.POSE_POSADO, 0, true, "g_posado_habla", 1.2f},
                {10, com.darkcraft.gavran.Gavran.POSE_PIE, 0, false, "g_quieto", 1.0f},
                {10, com.darkcraft.gavran.Gavran.POSE_GUARDIA, 0, false, "g_guardia", 1.0f},
                {10, com.darkcraft.gavran.Gavran.POSE_CARGA, 0, false, "g_carga", 0.8f},
                {10, com.darkcraft.gavran.Gavran.POSE_PIE, com.darkcraft.gavran.GavranAnims.DESCENDER, false, "g_descender", 0f},
                {10, com.darkcraft.gavran.Gavran.POSE_PIE, com.darkcraft.gavran.GavranAnims.SALUDO, false, "g_saludo", 0f},
                {10, com.darkcraft.gavran.Gavran.POSE_PIE, com.darkcraft.gavran.GavranAnims.COMBO3, false, "g_combo3", 0f},
                {10, com.darkcraft.gavran.Gavran.POSE_PIE, com.darkcraft.gavran.GavranAnims.COMBO5, false, "g_combo5", 0f},
                {10, com.darkcraft.gavran.Gavran.POSE_PIE, com.darkcraft.gavran.GavranAnims.CONTRAATAQUE, false, "g_contraataque", 0f},
                {10, com.darkcraft.gavran.Gavran.POSE_PIE, com.darkcraft.gavran.GavranAnims.RAICES, false, "g_raices", 0f},
                {10, com.darkcraft.gavran.Gavran.POSE_PIE, com.darkcraft.gavran.GavranAnims.CLAVAR, false, "g_clavar", 0f},
                {10, com.darkcraft.gavran.Gavran.POSE_PIE, com.darkcraft.gavran.GavranAnims.INVOCAR, false, "g_invocar", 0f},
                {10, com.darkcraft.gavran.Gavran.POSE_PIE, com.darkcraft.gavran.GavranAnims.ATURDIDO, false, "g_aturdido", 0f},
                {10, com.darkcraft.gavran.Gavran.POSE_PIE, com.darkcraft.gavran.GavranAnims.SUBIR, false, "g_subir", 0f},
                {10, com.darkcraft.gavran.Gavran.POSE_PIE, com.darkcraft.gavran.GavranAnims.MUERTE, false, "g_muerte", 0f},
                // fase 2 (11 = Gavran en su fase 2: mas grande y con la textura del arbol)
                {11, com.darkcraft.gavran.Gavran.POSE_PIE, 0, false, "g2_quieto", 1.0f},
                {11, com.darkcraft.gavran.Gavran.POSE_PIE, com.darkcraft.gavran.GavranAnims.ALZARSE, false, "g2_alzarse", 0f},
                {11, com.darkcraft.gavran.Gavran.POSE_PIE, com.darkcraft.gavran.GavranAnims.JUICIO, false, "g2_juicio", 0f},
                {11, com.darkcraft.gavran.Gavran.POSE_PIE, com.darkcraft.gavran.GavranAnims.CAER, false, "g2_caer", 0f},
                {11, com.darkcraft.gavran.Gavran.POSE_PIE, com.darkcraft.gavran.GavranAnims.SUBIR_BALCON, false, "g2_subir_balcon", 0f},
                {11, com.darkcraft.gavran.Gavran.POSE_PIE, com.darkcraft.gavran.GavranAnims.LEVANTARSE, false, "g2_levantarse", 0f},
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
                if (modeloG != null) modeloG.discard();
                modeloG = null;
                sp.setInvisible(false);
            });
        });
        return fin + 20;
    }

    private static final int ESC_TICKS = 20 * 190;
    private static com.darkcraft.gavran.Gavran modeloG;
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
        float len = anim > 0 ? com.darkcraft.rey.ReyAnims.TICKS[anim] / 20f : (Float) e[5];
        final boolean grande = e.length > 6 && (Boolean) e[6];
        if (pose == -1) {
            proyTick(mc, fase, anim, name);
            return;
        }
        if (fase >= 10) {
            gavranEsc(mc, fase - 9, pose, anim, habla, name, anim > 0 ? com.darkcraft.gavran.GavranAnims.TICKS[anim] / 20f : (Float) e[5]);
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
                com.darkcraft.rey.Rey cr = null;
                if (modelo != null && mc.level.getEntity(modelo.getId()) instanceof com.darkcraft.rey.Rey r) cr = r;
                boolean ok = cr != null && cr.fase() == fase && cr.pose() == pose && cr.flag(com.darkcraft.rey.Rey.F_HABLA) == habla
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
                        shot(mc, String.format("esc_%03d_%s_%d", escN++, name, Math.round(fr[escK] * 100)));
                        escK++;
                    }
                } else {
                    escI++;
                    escEstado = 0;
                }
            }
        }
    }

    /** Ser Gavran en el escaparate: como el rey, pero mas lejos (mide 5,5 bloques). */
    private static void gavranEsc(Minecraft mc, int faseG, int pose, int anim, boolean habla, String name, float len) {
        float[] fr = anim > 0 ? new float[]{0.15f, 0.35f, 0.55f, 0.8f} : new float[]{1.0f};
        switch (escEstado) {
            case 0 -> {
                mc.options.hideGui = true;
                verDesde(mc, new Vec3(ex + 5.6, ey + 4.4, ez + 10.4), new Vec3(ex, ey + 2.9, ez));
                server(mc, sp -> {
                    if (modelo != null) modelo.moveTo(ex, ey - 40, ez, 0f, 0f);
                    if (modeloG != null) {
                        modeloG.faseMuestra(faseG);
                        modeloG.muestra(pose, anim, habla);
                        modeloG.moveTo(ex, ey, ez, 0f, 0f);
                        modeloG.setYBodyRot(0f);
                        modeloG.setYHeadRot(0f);
                    }
                });
                escEstado = 1;
                escDesde = ticks;
            }
            case 1 -> {
                com.darkcraft.gavran.Gavran cg = null;
                if (modeloG != null && mc.level.getEntity(modeloG.getId()) instanceof com.darkcraft.gavran.Gavran g) cg = g;
                boolean ok = cg != null && cg.pose() == pose && cg.flag(com.darkcraft.gavran.Gavran.F_HABLA) == habla
                        && cg.actionId() == anim && Math.abs(cg.getY() - ey) < 1;
                if (ok) {
                    escEstado = 2;
                    escDesde = ticks;
                    escK = 0;
                    log("  escaparate: " + name + " en el cliente");
                } else if (ticks - escDesde > 200) {
                    log("  escaparate: " + name + " no llega al cliente (" + (cg == null ? "sin gavran" : "pose " + cg.pose() + " accion " + cg.actionId()) + ")");
                    escI++;
                    escEstado = 0;
                }
            }
            default -> {
                if (escK < fr.length) {
                    int when = Math.round((anim > 0 ? len * fr[escK] : len) * 20);
                    if (ticks - escDesde >= when) {
                        shot(mc, String.format("esc_%03d_%s_%d", escN++, name, Math.round(fr[escK] * 100)));
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
        int[] when = tipo == com.darkcraft.rey.ReyProyectil.TAJO ? new int[]{5, 13, 22, 33} : new int[]{8, 20, 34, 50};
        switch (escEstado) {
            case 0 -> {
                mc.options.hideGui = true;
                // los tajos vienen de frente (como los ve quien los recibe); las bolas y "lado" cruzan de lado
                boolean frente = tipo == com.darkcraft.rey.ReyProyectil.TAJO && !name.contains("lado");
                if (frente) verDesde(mc, new Vec3(ex + 1.3, ey + 2.3, ez + 4.6), new Vec3(ex, ey + 1.3, ez - 2.5));
                else verDesde(mc, new Vec3(ex + 0.5, ey + 4.6, ez + 6.2), new Vec3(ex, ey + 1.1, ez));
                proyId = -1;
                server(mc, sp -> {
                    if (modelo != null) modelo.moveTo(ex, ey - 40, ez, 0f, 0f);
                    ServerLevel lvl = sp.serverLevel();
                    com.darkcraft.rey.ReyProyectil pr = new com.darkcraft.rey.ReyProyectil(DarkCraftMod.REY_PROYECTIL.get(), lvl);
                    double v = tipo == com.darkcraft.rey.ReyProyectil.TAJO ? (frente ? 0.22 : 0.26) : 0.16;
                    if (frente) pr.moveTo(ex, ey + 1.3, ez - 6.5);
                    else pr.moveTo(ex - 4.2, ey + 1.2, ez);
                    pr.preparar(tipo, frente ? new Vec3(0, 0, v) : new Vec3(v, 0, 0), 0f, fase);
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
                        shot(mc, String.format("esc_%03d_%s_%d", escN++, name, when[escK]));
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
        StringBuilder b = new StringBuilder("energia=" + com.darkcraft.armas.Combos.energia(sp)
                + " (cliente " + com.darkcraft.client.armas.ArmasCliente.energiaCliente() + ")"
                + " forma=" + com.darkcraft.armas.EspadaRey.forma(sp.getMainHandItem())
                + "/" + com.darkcraft.armas.EspadaRey.forma(sp.getOffhandItem())
                + " jugador=" + String.format("%.1f", sp.getHealth())
                + " en " + String.format("%.1f,%.1f,%.1f", sp.getX() - ex, sp.getY() - ey, sp.getZ() - ez) + " | dianas:");
        for (net.minecraft.world.entity.LivingEntity d : dianas)
            b.append(' ').append(Math.round(d.getHealth())).append(d.isOnFire() ? "(arde)" : "");
        return b.toString();
    }

    /** El titulo o subtitulo que se ve ahora en pantalla (campos protegidos de Gui; nombres de desarrollo). */
    private static String campoGui(Minecraft m, String campo) {
        try {
            var f = net.minecraft.client.gui.Gui.class.getDeclaredField(campo);
            f.setAccessible(true);
            Object v = f.get(m.gui);
            return v instanceof net.minecraft.network.chat.Component c ? c.getString() : "-";
        } catch (Exception e) {
            return "?";
        }
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
                sp.getInventory().items.set(0, new ItemStack(DarkCraftMod.ESPADA_DIESTRA.get()));
                sp.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(DarkCraftMod.ESPADA_SINIESTRA.get()));
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
            boolean usando = sp.isUsingItem() && com.darkcraft.armas.EspadaRey.es(sp.getUseItem());
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
            log("darkcraft en " + s[0] + "," + s[1] + "; cofre 1 en " + chest1[0] + "," + chest1[1] + "," + chest1[2]);
            sp.teleportTo(lvl, chest1[0] - 2.5, chest1[1], chest1[2] + 0.5, -90f, 12f);
        }));
        // los comandos nuevos (por sitio) y la ayuda; el viejo ya no existe
        at(5, "Comandos: /darkcraft ayuda, /darkcraft valdrenn localizar y el viejo /darkcraft localizar", m -> {
            m.options.hideGui = false;
            server(m, sp -> {
                comando(sp, "darkcraft ayuda");
                comando(sp, "darkcraft valdrenn localizar");
                comando(sp, "darkcraft localizar");
            });
        });
        at(7, "Abrir el chat", m -> m.setScreen(new net.minecraft.client.gui.screens.ChatScreen("")));
        at(8, "Captura del chat con la ayuda", m -> {
            shot(m, "cliente_00_ayuda");
            m.setScreen(null);
        });
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
        int finEspejo = espejo(after + 40);
        rey(finEspejo + 20);
        // por orden de tiempo (cada parte apunta sus pasos a su manera); el orden de los empates se conserva
        steps.sort(java.util.Comparator.comparingInt(Step::at));
    }

    // ------------------------------------------------------------------ el Espejo de la Sombra y el libro de la M

    private static com.darkcraft.client.espejo.LibroScreen libro;

    private static void mirarAlCastillo(Minecraft m) {
        server(m, sp -> {
            double dx = castle[0] - sp.getX(), dz = castle[1] - sp.getZ();
            float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            sp.teleportTo(sp.serverLevel(), sp.getX(), sp.getY(), sp.getZ(), yaw, 8f);
        });
    }

    private static int espejo(int t) {
        atTicks(t, "Espejo: se vuelve a despertar (aviso y sonido) mirando al castillo", m -> {
            m.setScreen(null);
            m.options.hideGui = false;
            log("  espejo antes: " + com.darkcraft.client.espejo.EspejoCliente.resumen());
            server(m, sp -> {
                sp.closeContainer();
                sp.setGameMode(GameType.CREATIVE);
                comando(sp, "darkcraft valdrenn espejo despertar");
            });
            mirarAlCastillo(m);
        });
        atTicks(t + 34, "Captura del aviso del espejo", m -> shot(m, "cliente_e00_despierta"));
        atTicks(t + 120, "Captura del espejo", m -> {
            log("  espejo: " + com.darkcraft.client.espejo.EspejoCliente.resumen());
            shot(m, "cliente_e01_espejo");
        });
        atTicks(t + 124, "zoom de cerca", m -> com.darkcraft.client.espejo.EspejoCliente.probar(0, 1));
        atTicks(t + 128, "captura", m -> shot(m, "cliente_e02_espejo_cerca"));
        atTicks(t + 130, "zoom de lejos", m -> com.darkcraft.client.espejo.EspejoCliente.probar(4, 1));
        atTicks(t + 134, "captura", m -> shot(m, "cliente_e03_espejo_lejos"));
        atTicks(t + 136, "espejo grande y un amigo de mentira", m -> {
            com.darkcraft.client.espejo.EspejoCliente.probar(2, 2);
            com.darkcraft.client.espejo.EspejoCliente.probarAmigo(true);
        });
        atTicks(t + 140, "captura", m -> shot(m, "cliente_e04_espejo_grande_amigo"));
        atTicks(t + 142, "espejo pequeno", m -> com.darkcraft.client.espejo.EspejoCliente.probar(2, 0));
        atTicks(t + 146, "captura", m -> shot(m, "cliente_e05_espejo_pequeno"));
        // el espejo fuera, a cielo abierto: el reflejo oscuro de la isla (no la planta)
        atTicks(t + 148, "Espejo fuera: en la avenida de la fuente", m -> server(m, sp -> {
            ServerLevel lvl = sp.serverLevel();
            CastlePlan plan = PlanCache.get(lvl.getSeed(), castle[0], castle[1], (x, z) -> 64);
            int fx = plan.cx + 1, fz = plan.cz + 116;
            lvl.getChunk(fx >> 4, fz >> 4); // cargado (si no, la altura es la del fondo del mundo y se cae al vacio)
            int y = lvl.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, fx, fz);
            if (y < CastlePlan.Y0 - 60) y = CastlePlan.Y0 + 2;
            log("  fuera en " + fx + "," + y + "," + fz);
            sp.teleportTo(lvl, fx + 0.5, y, fz + 0.5, 180f, 25f);
        }));
        atTicks(t + 226, "Captura del espejo fuera", m -> {
            log("  espejo fuera: " + com.darkcraft.client.espejo.EspejoCliente.resumen());
            shot(m, "cliente_e07_fuera");
        });
        atTicks(t + 228, "zoom de lejos fuera", m -> com.darkcraft.client.espejo.EspejoCliente.probar(4, 1));
        atTicks(t + 232, "captura", m -> shot(m, "cliente_e08_fuera_lejos"));
        final int L = t + 236;
        atTicks(L + 0, "Libro de la Sombra: historia", m -> {
            com.darkcraft.client.espejo.EspejoCliente.probar(2, 1);
            // como con la tecla M (da el logro libro_sombra)
            com.darkcraft.client.espejo.LibroScreen.abrir();
            libro = (com.darkcraft.client.espejo.LibroScreen) m.screen;
            libro.probar(0, 0, -1);
        });
        atTicks(L + 8, "captura", m -> shot(m, "cliente_e10_historia"));
        atTicks(L + 10, "la cronica, capitulo 1", m -> libro.probar(0, 0, 1));
        atTicks(L + 14, "captura", m -> shot(m, "cliente_e11_cronica"));
        atTicks(L + 16, "el diario", m -> libro.probar(0, 0, 10));
        atTicks(L + 20, "captura", m -> shot(m, "cliente_e12_diario"));
        atTicks(L + 22, "el mapa", m -> libro.probarZoom(1.5f));
        atTicks(L + 32, "captura", m -> shot(m, "cliente_e13_mapa"));
        atTicks(L + 34, "el mapa de lejos", m -> libro.probarZoom(0.3f));
        atTicks(L + 44, "captura", m -> shot(m, "cliente_e14_mapa_lejos"));
        atTicks(L + 46, "una marca nueva: el dialogo", m -> {
            libro.probarZoom(1.5f);
            libro.probarMarca("Torre del cronista", 1, 0, false);
        });
        atTicks(L + 52, "captura", m -> shot(m, "cliente_e15_marca_dialogo"));
        atTicks(L + 54, "escribir la marca (la pluma)", m -> libro.probarMarca("Torre del cronista", 1, 0, true));
        int[] esc = {6, 18, 30, 44};
        for (int i = 0; i < esc.length; i++) {
            final int k = i;
            atTicks(L + 54 + esc[i], "captura de la escena", m -> shot(m, "cliente_e16_escribe_" + k));
        }
        atTicks(L + 114, "captura del mapa con la marca", m -> {
            log("  espejo con la marca: " + com.darkcraft.client.espejo.EspejoCliente.resumen());
            shot(m, "cliente_e17_mapa_marca");
        });
        atTicks(L + 116, "logros", m -> libro.probar(2, 0, 0));
        atTicks(L + 122, "captura", m -> shot(m, "cliente_e18_logros"));
        atTicks(L + 124, "coleccion", m -> libro.probar(2, 1, 0));
        atTicks(L + 130, "captura", m -> shot(m, "cliente_e19_coleccion"));
        atTicks(L + 132, "bestiario", m -> libro.probar(2, 2, 1));
        atTicks(L + 142, "captura", m -> shot(m, "cliente_e20_bestiario"));
        atTicks(L + 144, "cerrar el libro", m -> {
            m.setScreen(null);
            com.darkcraft.client.espejo.EspejoCliente.probarAmigo(false);
            log("  logros del espejo: libro_sombra/primera_marca (los da el servidor)");
            server(m, sp -> log("  libro_sombra=" + com.darkcraft.story.Logros.tiene(sp, "libro_sombra")
                    + " primera_marca=" + com.darkcraft.story.Logros.tiene(sp, "primera_marca")));
        });
        return L + 152;
    }

    // ------------------------------------------------------------------ el Rey

    private static int[] castle;
    private static com.darkcraft.rey.Sala sala;
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
                sala = com.darkcraft.rey.Sala.near(lvl, castle[0], castle[1]);
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
        atTicks(B + 100, "Captura del espejo dentro del torreon (la planta)", m -> {
            m.options.hideGui = false;
            log("  espejo en el torreon: " + com.darkcraft.client.espejo.EspejoCliente.resumen());
            shot(m, "cliente_e06_planta");
        });
        atTicks(B + 130, "Captura: el salon del trono desde la puerta", m -> {
            m.options.hideGui = true;
            shot(m, "cliente_19_salon");
        });
        atTicks(B + 134, "Camara delante del trono", m -> {
            var t = sala.t;
            verDesde(m, new Vec3(t.seatX() + 0.5, t.y() + 4.6, t.seatZ() + 5.5), new Vec3(t.seatX() + 0.5, t.y() + 4.6, t.seatZ() + 0.5));
        });
        // el trono del Rey por detras y de lado (para ver que no queden huecos)
        atTicks(B + 136, "Cortes del trono del Rey", m -> {
            var t = sala.t;
            server(m, sp -> volcar(sp.serverLevel(), "trono del Rey", t.seatX() - 8, t.seatX() + 8, t.y() - 1, t.y() + 12,
                    t.seatZ() - 3, t.seatZ() + 1));
            verDesde(m, new Vec3(t.seatX() + 5.5, t.y() + 4.2, t.seatZ() + 0.5), new Vec3(t.seatX() - 0.5, t.y() + 3.8, t.seatZ() - 1.2));
        });
        atTicks(B + 139, "captura", m -> shot(m, "cliente_19b_trono_lado_d"));
        atTicks(B + 141, "el otro lado", m -> {
            var t = sala.t;
            verDesde(m, new Vec3(t.seatX() - 4.5, t.y() + 4.2, t.seatZ() + 0.5), new Vec3(t.seatX() + 1.5, t.y() + 3.8, t.seatZ() - 1.2));
        });
        atTicks(B + 144, "captura", m -> shot(m, "cliente_19c_trono_lado_i"));
        atTicks(B + 146, "Camara delante del trono", m -> {
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
                var reyes = sp.serverLevel().getEntitiesOfClass(com.darkcraft.rey.Rey.class, sala.box().inflate(4));
                log("reyes en la sala: " + reyes.size() + (reyes.isEmpty() ? "" : " -> " + reyes.get(0).debug()));
            });
            reyStart = ticks;
        });
        int E = B + 152;
        // la entrada dura lo que duran sus frases (con pausas): capturas en cada plano
        float[] ce = com.darkcraft.rey.Rey.cortesEntrada();
        float[] intro = {2.5f, (ce[0] + ce[1]) / 2, ce[1] + 1.5f, (ce[1] + ce[2]) / 2 + 1f, ce[2] + 1.0f, ce[3] - 0.3f};
        for (int i = 0; i < intro.length; i++) {
            final int k = i;
            atTicks(E + Math.round(intro[i] * 20), "Captura de la intro", m -> shot(m, String.format("cliente_%02d_intro_%d", 21 + k, k)));
        }
        int F = E + com.darkcraft.rey.Rey.INTRO_TICKS + 60;
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
        // el cambio a la fase 3 espera a que acabe la frase 5 (+76 ticks) y la pelea en los volcanes a la 10 (+38)
        int[] v = {40, 186, 251, 298, 338, 368, 514, 594};
        for (int i = 0; i < v.length; i++) {
            final int k = i;
            atTicks(F3 + v[i], "Captura fase 3", m -> {
                m.options.hideGui = k < 5 ? false : k != 6;
                shot(m, String.format("cliente_%02d_fase3_%d", 50 + k, k));
            });
        }
        // golpe final: los dos circulos dorados, el aviso grande y la cuenta atras
        int G = F3 + 500;   // cuando ya ha acabado la cinematica de la fase 3 (lucha otra vez)
        atTicks(G, "Forzar el golpe final", m -> server(m, sp -> reyCerca(sp).ifPresent(r -> log("  golpe final forzado: " + r.forzarGolpeFinal()))));
        atTicks(G + 24, "Captura golpe final (circulos)", m -> {
            m.options.hideGui = false;
            shot(m, "cliente_57_golpe_final_circulos");
            log("  titulo en pantalla: " + campoGui(m, "title") + " | " + campoGui(m, "subtitle"));
        });
        atTicks(G + 34, "Meterse en un circulo dorado", m -> server(m, sp -> reyCerca(sp).ifPresent(r -> {
            var cs = r.circulos();
            log("  circulos dorados: " + cs.size() + (cs.isEmpty() ? "" : " | el primero a " + Math.round(Math.hypot(cs.get(0).x - sp.getX(), cs.get(0).z - sp.getZ())) + " bloques"));
            if (!cs.isEmpty()) sp.teleportTo(sp.serverLevel(), cs.get(0).x, cs.get(0).y + 0.1, cs.get(0).z, sp.getYRot(), sp.getXRot());
        })));
        atTicks(G + 50, "Captura golpe final (cuenta atras)", m -> shot(m, "cliente_58_golpe_final_cuenta"));
        atTicks(G + 78, "Vida tras el golpe final", m -> server(m, sp -> log("  tras el golpe final: vida del jugador " + Math.round(sp.getHealth())
                + (sp.isDeadOrDying() ? " (muerto)" : "") + " | logro golpe_final=" + com.darkcraft.story.Logros.tiene(sp, com.darkcraft.story.Logros.GOLPE_FINAL))));
        int D = F3 + 654;
        atTicks(D, "Forzar la muerte del Rey", m -> server(m, sp -> reyCerca(sp).ifPresent(com.darkcraft.rey.Rey::forzarMuerte)));
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
                var a = com.darkcraft.arbol.TorreArbol.ascensor(sp.serverLevel(), hall);
                var d = com.darkcraft.arbol.TorreArbol.puerta(sp.serverLevel(), hall);
                log("  ascensor: " + (a == null ? "NO ESTA" : "estado " + a.estado() + " y=" + a.getY()) + " | puerta: " + (d == null ? "NO ESTA" : "estado " + d.estado()));
                sp.teleportTo(sp.serverLevel(), hall[0] + 0.5, hall[4] + 0.1, hall[1] + com.darkcraft.gen.TreeTower.SHAFT_Z + 0.5, 180f, 0f);
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
            sp.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(DarkCraftMod.LLAVE_ARBOL.get()));
            sp.teleportTo(sp.serverLevel(), hall[0] + 0.5, hall[2] + 0.1, hall[1] - 10.5, 180f, -6f);
        }));
        atTicks(S + 490, "Usar la llave en la puerta", m -> {
            m.options.hideGui = false;
            server(m, sp -> {
                var d = com.darkcraft.arbol.TorreArbol.puerta(sp.serverLevel(), hall);
                BlockPos pp = sp.blockPosition();
                log("  jugador en " + String.format("%.1f,%.1f,%.1f", sp.getX(), sp.getY(), sp.getZ()) + " pies " + sp.serverLevel().getBlockState(pp)
                        + " cabeza " + sp.serverLevel().getBlockState(pp.above()) + " | " + (d == null ? "" : String.format("a %.1f de la puerta", Math.sqrt(sp.distanceToSqr(d)))));
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
                var d = com.darkcraft.arbol.TorreArbol.puerta(sp.serverLevel(), hall);
                log("  jugador en " + String.format("%.1f,%.1f,%.1f", sp.getX(), sp.getY(), sp.getZ()) + " (" + sp.level().dimension().location() + ")");
                log("  puerta despues: " + (d == null ? "NO ESTA" : "estado " + d.estado()) + " barrera: "
                        + sp.serverLevel().getBlockState(new BlockPos(hall[0], hall[2] + 2, hall[1] - 15)));
            });
            m.options.hideGui = true;
            verDesde(m, new Vec3(hall[0] + 0.5, hall[2] + 3.2, hall[1] - 9.0), new Vec3(hall[0] + 0.5, hall[2] + 6, hall[1] - 38));
        });
        atTicks(S + 490 + 196, "captura", m -> shot(m, "cliente_80_sala_arbol"));
        atTicks(S + 490 + 200, "Desde el balcon", m -> verDesde(m, new Vec3(hall[0] + 32.0, hall[2] + 15.4, hall[1] + 13.0),
                new Vec3(hall[0] - 6, hall[2] + 12, hall[1] - 8)));
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
        int finG = gavran(S + 490 + 232);
        finG = mandoble(finG);
        finG = fortaleza(finG);
        S = finG - 280;
        atTicks(S + 130 + 150, "Logros conseguidos", m -> server(m, sp -> {
            StringBuilder b = new StringBuilder("  logros:");
            for (String id : new String[]{com.darkcraft.story.Logros.ENCONTRAR, com.darkcraft.story.Logros.CAPITULO1,
                    com.darkcraft.story.Logros.REY, com.darkcraft.story.Logros.ARBOL, com.darkcraft.story.Logros.GAVRAN_GUARDIA,
                    com.darkcraft.story.Logros.GAVRAN_FASE1, com.darkcraft.story.Logros.GAVRAN_FASE2, com.darkcraft.story.Logros.GAVRAN,
                    com.darkcraft.story.Logros.capitulo(5)})
                b.append(' ').append(id).append('=').append(com.darkcraft.story.Logros.tiene(sp, id) ? "SI" : "no");
            log(b.toString());
        }));
        atTicks(S + 130 + 160, "Pantalla de logros", m -> {
            m.setCameraEntity(m.player);
            m.options.hideGui = false;
            var ca = m.player.connection.getAdvancements();
            m.setScreen(new net.minecraft.client.gui.screens.advancements.AdvancementsScreen(ca));
            var raiz = ca.getAdvancements().get(new net.minecraft.resources.ResourceLocation("darkcraft", "raiz"));
            log("  pestana de logros: " + (raiz != null ? "esta" : "NO ESTA"));
            if (raiz != null) ca.setSelectedTab(raiz, true);
        });
        atTicks(S + 130 + 166, "captura de los logros", m -> shot(m, "cliente_99_logros"));
        atTicks(S + 130 + 170, "cerrar", m -> m.setScreen(null));
        atTicks(S + 130 + 175, "Fin de la prueba", ClientCiTest::finish);
    }

    // ------------------------------------------------------------------ Ser Gavran (parte B1)

    private static int gavranStart = -1;
    private static boolean seguirG;

    private static java.util.Optional<com.darkcraft.gavran.Gavran> gavranCerca(net.minecraft.server.level.ServerPlayer sp) {
        return sp.serverLevel().getEntitiesOfClass(com.darkcraft.gavran.Gavran.class, sp.getBoundingBox().inflate(120)).stream().findFirst();
    }

    private static void ataqueG(int t, String nombre, int[] capturas, String prefijo) {
        atTicks(t, "Gavran: ataque " + nombre, m -> server(m, sp -> gavranCerca(sp).ifPresent(g -> log("  ataque " + nombre + ": " + g.forzarAtaque(nombre)
                + " | " + g.debug()))));
        for (int i = 0; i < capturas.length; i++) {
            final int k = i;
            atTicks(t + capturas[i], "Captura " + nombre, m -> {
                m.options.hideGui = k != 0;
                shot(m, String.format("%s_%d", prefijo, k));
            });
        }
    }

    /** La pelea con Ser Gavran: entrar en la sala, la entrada (cinematica), sus ataques, guardia rota y fin de la fase 1. */
    private static int gavran(int G) {
        atTicks(G, "Gavran: en su rama antes de entrar", m -> {
            m.setCameraEntity(m.player);
            server(m, sp -> {
                sp.setGameMode(GameType.SURVIVAL);
                sp.setHealth(sp.getMaxHealth());
                ItemStack sw = new ItemStack(net.minecraft.world.item.Items.NETHERITE_SWORD);
                sw.enchant(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS, 5);
                sp.setItemInHand(InteractionHand.MAIN_HAND, sw);
                var g = gavranCerca(sp);
                log("  gavran: " + g.map(x -> x.debug() + " en " + String.format("%.1f,%.1f,%.1f", x.getX(), x.getY(), x.getZ())).orElse("NO ESTA"));
            });
        });
        atTicks(G + 6, "Mirar a la rama desde la sala", m -> {
            m.options.hideGui = true;
            // desde donde empieza el plano de la rama en la entrada (a un lado y casi a su altura)
            verDesde(m, new Vec3(hall[0] + 10.5, hall[2] + 27, hall[1] - 28), new Vec3(hall[0] + 0.5, hall[2] + 34.5, hall[1] - 19.5));
        });
        atTicks(G + 14, "captura", m -> shot(m, "cliente_g01_rama"));
        atTicks(G + 18, "Entrar en la Sala del Arbol", m -> {
            m.setCameraEntity(m.player);
            m.options.hideGui = false;
            server(m, sp -> sp.teleportTo(sp.serverLevel(), hall[0] + 0.5, hall[2], hall[1] - 21.5, 180f, 0f));
            gavranStart = ticks;
        });
        // la entrada dura lo que duran sus frases (con pausas): capturas en los momentos clave de cada plano (los de
        // cuando duraba 35 s, llevados a los planos de ahora)
        float[] viejos = {0f, 3.2f, 10.9f, 19.0f, 22.5f, 28.0f, 34.4f}, cg = com.darkcraft.gavran.Gavran.cortesEntrada();
        float[] nuevos = {0f, cg[0], cg[1], cg[2], cg[3], cg[4], cg[5]};
        float[] intro = {1.6f, 2.9f, 6.5f, 14.5f, 20.0f, 21.0f, 21.6f, 23.5f, 25.5f, 29.0f, 31.5f, 33.6f};
        for (int i = 0; i < intro.length; i++) {
            final int k = i;
            atTicks(G + 18 + Math.round(llevar(intro[i], viejos, nuevos) * 20), "Captura de la entrada de Gavran", m -> {
                if (k == 1) server(m, sp -> {
                    var d = com.darkcraft.arbol.TorreArbol.puerta(sp.serverLevel(), hall);
                    log("  puerta: " + (d == null ? "NO ESTA" : "estado " + d.estado()) + " | barrera: "
                            + sp.serverLevel().getBlockState(new BlockPos(hall[0], hall[2] + 2, hall[1] - 15)));
                });
                shot(m, String.format("cliente_g%02d_entrada_%d", 2 + k, k));
            });
        }
        int L = G + 18 + com.darkcraft.gavran.Gavran.INTRO_TICKS + 12;
        atTicks(L, "Gavran: empieza la pelea (camara de observador)", m -> {
            m.options.hideGui = true;
            seguirG = true;
            server(m, sp -> gavranCerca(sp).ifPresent(g -> log("  " + g.debug())));
        });
        atTicks(L + 30, "Captura: la barra de vida", m -> {
            m.options.hideGui = false;
            shot(m, "cliente_g20_barra");
        });
        // la vida no baja de la mitad antes de tiempo (el cambio de fase se fuerza mas abajo)
        for (int tv : new int[]{L + 300, L + 520, L + 700})
            atTicks(tv, "Vida de Gavran (prueba)", m -> server(m, sp -> gavranCerca(sp).ifPresent(g -> {
                if (g.fase() == 1) g.ponerVida(860);
            })));
        ataqueG(L + 60, "combo3", new int[]{8, 13, 24, 34}, "cliente_g21_combo3");
        ataqueG(L + 140, "raices", new int[]{8, 16, 22, 30}, "cliente_g22_raices");
        ataqueG(L + 220, "clavar", new int[]{10, 19, 28, 40}, "cliente_g23_clavar");
        ataqueG(L + 310, "embestida", new int[]{12, 22, 30}, "cliente_g24_embestida");
        ataqueG(L + 380, "invocar", new int[]{10, 28, 45}, "cliente_g25_invocar");
        ataqueG(L + 460, "combo5", new int[]{12, 30, 46, 62}, "cliente_g26_combo5");
        ataqueG(L + 560, "guardia", new int[]{10}, "cliente_g27_guardia");
        atTicks(L + 572, "Golpear a Gavran mientras se cubre (contraataca)", m -> server(m, sp -> gavranCerca(sp).ifPresent(g -> {
            if (g.distanceTo(sp) > 5) sp.teleportTo(sp.serverLevel(), g.getX() + g.adelanteX() * 3.5, g.getY(), g.getZ() + g.adelanteZ() * 3.5, sp.getYRot(), sp.getXRot());
            float h0 = g.getHealth();
            sp.attack(g);
            log("  golpe a la guardia: vida " + Math.round(h0) + " -> " + Math.round(g.getHealth()) + " | " + g.debug());
        })));
        for (int i = 0; i < 3; i++) {
            final int k = i;
            atTicks(L + 578 + i * 6, "Captura del contraataque", m -> shot(m, "cliente_g28_contraataque_" + k));
        }
        atTicks(L + 640, "Romperle la guardia", m -> server(m, sp -> gavranCerca(sp).ifPresent(g -> log("  guardia rota: " + g.forzarGuardiaRota()))));
        atTicks(L + 662, "Captura: aturdido", m -> {
            m.options.hideGui = false;
            shot(m, "cliente_g29_aturdido");
        });
        atTicks(L + 700, "Captura: aturdido", m -> shot(m, "cliente_g30_aturdido"));
        // ---------------------------------------------------------------- el arbol despierta (fase 2)
        atTicks(L + 760, "Fin de la fase 1: el arbol despierta", m -> server(m, sp -> gavranCerca(sp).ifPresent(g ->
                log("  cambio de fase: " + g.forzarFinFase() + " | " + g.debug()))));
        int[] cambio = {30, 110, 190, 262, 292, 345};
        for (int i = 0; i < cambio.length; i++) {
            final int k = i;
            atTicks(L + 760 + cambio[i], "Captura del cambio de fase", m -> {
                m.options.hideGui = false;
                shot(m, "cliente_g40_cambio_" + k);
                if (k == 5) server(m, sp -> gavranCerca(sp).ifPresent(g -> log("  en el cambio: " + g.debug())));
            });
        }
        int F2 = L + 760 + 420;
        atTicks(F2, "Fase 2: empieza", m -> {
            m.options.hideGui = true;
            seguirG = true;
            server(m, sp -> gavranCerca(sp).ifPresent(g -> log("  fase 2: " + g.debug() + " | logro fase 1: "
                    + com.darkcraft.story.Logros.tiene(sp, com.darkcraft.story.Logros.GAVRAN_FASE1))));
        });
        for (int tv : new int[]{F2 + 2, F2 + 100, F2 + 190, F2 + 430, F2 + 600, F2 + 700, F2 + 780})
            atTicks(tv, "Vida de Gavran (prueba)", m -> server(m, sp -> gavranCerca(sp).ifPresent(g -> {
                if (g.fase() >= 2) g.ponerVida(480);
            })));
        // el corazon: mirar a la luz verde de su pecho y pegar
        atTicks(F2 + 6, "Golpe normal y golpe al corazon", m -> server(m, sp -> gavranCerca(sp).ifPresent(g -> {
            Vec3 f = new Vec3(g.adelanteX(), 0, g.adelanteZ());
            Vec3 pos = g.position().add(f.scale(3.4));
            sp.teleportTo(sp.serverLevel(), pos.x, g.getY(), pos.z, sp.getYRot(), 0f);
            // primero al cuerpo (mirando a la cadera)
            mirarA(sp, g.position().add(0, 1.6, 0));
            sp.resetAttackStrengthTicker();
            g.invulnerableTime = 0;
            float h0 = g.getHealth();
            sp.attack(g);
            float d1 = h0 - g.getHealth();
            g.invulnerableTime = 0;
            mirarA(sp, g.corazonPos());
            sp.resetAttackStrengthTicker();
            float h1 = g.getHealth();
            sp.attack(g);
            float d2 = h1 - g.getHealth();
            log(String.format(java.util.Locale.ROOT, "  golpe al cuerpo %.1f | golpe al corazon %.1f | logro gavran_corazon=%s | %s", d1, d2,
                    com.darkcraft.story.Logros.tiene(sp, com.darkcraft.story.Logros.GAVRAN_CORAZON), g.debug()));
        })));
        atTicks(F2 + 12, "Captura del corazon", m -> {
            m.options.hideGui = false;
            shot(m, "cliente_g47_corazon");
        });
        ataqueG(F2 + 30, "atrapar", new int[]{10, 20, 30}, "cliente_g41_atrapar");
        atTicks(F2 + 50, "Atrapado?", m -> server(m, sp -> log("  jugador atrapado: lentitud "
                + sp.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN) + " | " + gavranCerca(sp).map(g -> g.debug()).orElse("-"))));
        ataqueG(F2 + 110, "hojas", new int[]{14, 31, 34, 60}, "cliente_g42_hojas");
        ataqueG(F2 + 200, "suelo", new int[]{20, 45, 90, 150}, "cliente_g43_suelo");
        ataqueG(F2 + 440, "juicio", new int[]{10, 40, 80, 86, 100}, "cliente_g44_juicio");
        // detras de un muro: las espinas no le alcanzan
        atTicks(F2 + 440 + 45, "Esconderse detras de un muro", m -> server(m, sp -> gavranCerca(sp).ifPresent(g -> {
            var muros = g.murosJuicio();
            if (muros.isEmpty()) {
                log("  muros del juicio: NINGUNO");
                return;
            }
            Vec3 c = muros.get(0)[0], n = muros.get(0)[1];
            Vec3 tras = c.add(n.scale(2.3));
            sp.teleportTo(sp.serverLevel(), tras.x, c.y, tras.z, sp.getYRot(), 0f);
            log("  muros del juicio: " + muros.size() + " | detras del primero en " + Math.round(tras.x) + "," + Math.round(tras.z)
                    + " bloque: " + sp.serverLevel().getBlockState(BlockPos.containing(c.x, c.y + 1, c.z)));
        })));
        atTicks(F2 + 440 + 90, "Despues de las espinas", m -> server(m, sp -> log("  tras el juicio: logro gavran_juicio="
                + com.darkcraft.story.Logros.tiene(sp, com.darkcraft.story.Logros.GAVRAN_JUICIO) + " vida " + Math.round(sp.getHealth()))));
        // al balcon: salta a por el
        atTicks(F2 + 610, "Subir al balcon", m -> server(m, sp -> gavranCerca(sp).ifPresent(g -> {
            var s = com.darkcraft.gavran.SalaArbol.cerca(sp.serverLevel(), g.getX(), g.getZ());
            if (s == null) return;
            double a = s.ang(g.getX(), g.getZ()) + 0.55;
            // al norte (detras del trono) no hay balcon
            if (Math.abs(net.minecraft.util.Mth.wrapDegrees(Math.toDegrees(a) + 90)) < 32) a -= 1.1;
            Vec3 p = s.enAnillo(a, 35.6, s.F() + 13);
            sp.teleportTo(sp.serverLevel(), p.x, p.y, p.z, sp.getYRot(), 0f);
            log("  en el balcon: " + sp.serverLevel().getBlockState(BlockPos.containing(p.x, p.y - 1, p.z)) + " a " + Math.round(p.x) + ","
                    + Math.round(p.y) + "," + Math.round(p.z));
        })));
        ataqueG(F2 + 625, "balcon", new int[]{8, 20, 30, 44}, "cliente_g45_balcon");
        atTicks(F2 + 700, "Volver al suelo", m -> server(m, sp -> gavranCerca(sp).ifPresent(g -> {
            var s = com.darkcraft.gavran.SalaArbol.cerca(sp.serverLevel(), g.getX(), g.getZ());
            if (s == null) return;
            Vec3 p = s.enAnillo(s.ang(g.getX(), g.getZ()) - 0.3, 26, s.F());
            sp.teleportTo(sp.serverLevel(), p.x, s.suelo(sp.serverLevel(), p.x, p.z), p.z, sp.getYRot(), 0f);
            log("  de vuelta al suelo | " + g.debug());
        })));
        ataqueG(F2 + 712, "bajar", new int[]{12, 30, 40}, "cliente_g46_bajar");
        // ---------------------------------------------------------------- muerte con honor (parte B3)
        int M = F2 + 860;
        atTicks(M, "Fin de la fase 2: muere con honor", m -> {
            seguirG = false;
            m.options.hideGui = false;
            server(m, sp -> gavranCerca(sp).ifPresent(g -> log("  fin de la fase 2: " + g.forzarFinFase() + " | " + g.debug())));
        });
        int[] muerte = {60, 200, 296, 345, 400, 440, 520};
        for (int i = 0; i < muerte.length; i++) {
            final int k = i;
            atTicks(M + muerte[i], "Captura de la muerte de Gavran", m -> {
                m.options.hideGui = false;
                shot(m, "cliente_g50_muerte_" + k);
                if (k == 0 || k == 3) server(m, sp -> gavranCerca(sp).ifPresent(g -> log("  muriendo: " + g.debug())));
            });
        }
        atTicks(M + 600, "Despues de su muerte", m -> {
            m.setCameraEntity(m.player);
            server(m, sp -> {
                ServerLevel lvl = sp.serverLevel();
                var s5 = com.darkcraft.gavran.SalaArbol.cerca(lvl, hall[0], hall[1]);
                BlockPos cofre = BlockPos.containing(s5.aterrizaje());
                var d = com.darkcraft.arbol.TorreArbol.puerta(lvl, hall);
                log("  tras la muerte: gavran " + gavranCerca(sp).map(g -> "SIGUE: " + g.debug()).orElse("ya no esta")
                        + " | estado guardado " + com.darkcraft.rey.ReyEstado.get(lvl.getServer()).get(s5.key())
                        + " | gano=" + com.darkcraft.rey.ReyEstado.get(lvl.getServer()).gano(s5.key(), sp.getUUID())
                        + " | puerta " + (d == null ? "NO ESTA" : "estado " + d.estado())
                        + " | cofre: " + lvl.getBlockState(cofre) + " capitulo " + com.darkcraft.story.StoryEvents.chapterAt(lvl, cofre));
                log("  logros: gavran_fase2=" + com.darkcraft.story.Logros.tiene(sp, com.darkcraft.story.Logros.GAVRAN_FASE2)
                        + " gavran=" + com.darkcraft.story.Logros.tiene(sp, com.darkcraft.story.Logros.GAVRAN));
                int flores = 0, cerezo = 0, hojas = 0, luces = 0, lucesCopa = 0;
                int F = hall[2], top = F + com.darkcraft.gen.TreeTower.HALL_H + 14;
                for (BlockPos q : BlockPos.betweenClosed(new BlockPos(hall[0] - 72, F - 1, hall[1] - 72), new BlockPos(hall[0] + 72, F + 96, hall[1] + 72))) {
                    var st = lvl.getBlockState(q);
                    if (st.is(net.minecraft.world.level.block.Blocks.FLOWERING_AZALEA_LEAVES)) flores++;
                    else if (st.is(net.minecraft.world.level.block.Blocks.CHERRY_LEAVES)) cerezo++;
                    else if (st.is(net.minecraft.tags.BlockTags.LEAVES)) hojas++;
                    else if (st.is(net.minecraft.world.level.block.Blocks.LIGHT)) {
                        if (q.getY() > top) lucesCopa++;
                        else luces++;
                    }
                }
                log("  el arbol florece: azalea florida " + flores + ", cerezo " + cerezo + ", hojas sin flor " + hojas
                        + " | luces en la sala " + luces + ", en la copa " + lucesCopa);
            });
        });
        // el trono de Gavran por detras y de lado (para ver que no queden huecos)
        atTicks(M + 602, "Cortes del trono de Gavran", m -> {
            m.options.hideGui = true;
            int tx = hall[0], tz = hall[1] - com.darkcraft.gen.TreeTower.HALL_R + 3, F = hall[2];
            server(m, sp -> volcar(sp.serverLevel(), "trono de Gavran", tx - 8, tx + 8, F - 1, F + 14, tz - 4, tz + 1));
            verDesde(m, new Vec3(tx + 5.5, F + 6.0, tz + 2.5), new Vec3(tx - 0.5, F + 5.0, tz - 1.5));
        });
        atTicks(M + 605, "captura", m -> shot(m, "cliente_g58_trono_lado_d"));
        atTicks(M + 607, "el otro lado", m -> {
            int tx = hall[0], tz = hall[1] - com.darkcraft.gen.TreeTower.HALL_R + 3, F = hall[2];
            verDesde(m, new Vec3(tx - 4.5, F + 6.0, tz + 2.5), new Vec3(tx + 1.5, F + 5.0, tz - 1.5));
        });
        atTicks(M + 610, "captura", m -> shot(m, "cliente_g59_trono_lado_i"));
        // ---------------------------------------------------------------- capitulo 5: el cofre, la cinematica con el narrador y el libro
        atTicks(M + 612, "vuelta al jugador", m -> {
            m.setCameraEntity(m.player);
            m.options.hideGui = false;
        });
        atTicks(M + 630, "Al cofre del capitulo 5", m -> server(m, sp -> {
            var s5 = com.darkcraft.gavran.SalaArbol.cerca(sp.serverLevel(), hall[0], hall[1]);
            BlockPos cofre = BlockPos.containing(s5.aterrizaje());
            sp.setGameMode(GameType.SURVIVAL);
            sp.getInventory().selected = 0;
            sp.getInventory().items.set(0, ItemStack.EMPTY);
            sp.teleportTo(sp.serverLevel(), cofre.getX() + 0.5, s5.suelo(sp.serverLevel(), cofre.getX() + 0.5, cofre.getZ() + 2.5), cofre.getZ() + 2.5, 180f, 25f);
        }));
        atTicks(M + 638, "Clic derecho en el cofre del capitulo 5", m -> {
            m.options.hideGui = false;
            // el cofre sube delante del trono, donde se arrodillo (SalaArbol.aterrizaje)
            BlockPos cofre = new BlockPos(hall[0], hall[2] + 2, hall[1] - com.darkcraft.gen.TreeTower.HALL_R + 7);
            var r = m.gameMode.useItemOn(m.player, InteractionHand.MAIN_HAND,
                    new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(cofre), net.minecraft.core.Direction.SOUTH, cofre, false));
            log("  clic en el cofre: " + r);
        });
        int C5 = M + 638;
        float t5 = com.darkcraft.cine.Timeline.NARR_START - 0.6f, b5 = com.darkcraft.cine.Timeline.back(5);
        float[] cortes = new float[7];
        cortes[0] = t5;
        for (int g = 0; g < 5; g++) cortes[g + 1] = com.darkcraft.cine.Timeline.cut(5, g);
        cortes[6] = b5;
        // titulo, y cada plano (algunos a principio y a final)
        float[] cap5 = {4.0f, mitad(cortes, 0, 0.5f), mitad(cortes, 1, 0.5f), mitad(cortes, 2, 0.25f), mitad(cortes, 2, 0.85f),
                mitad(cortes, 3, 0.5f), mitad(cortes, 4, 0.2f), mitad(cortes, 4, 0.9f), mitad(cortes, 5, 0.12f), mitad(cortes, 5, 0.55f),
                mitad(cortes, 5, 0.95f)};
        for (int i = 0; i < cap5.length; i++) {
            final int k = i;
            atTicks(C5 + Math.round(cap5[i] * 20) + 3, "Captura de la cinematica del capitulo 5", m -> {
                m.options.hideGui = false;
                shot(m, String.format("cliente_g%02d_capitulo5_%d", 60 + k, k));
                log(String.format(java.util.Locale.ROOT, "  capitulo 5 t=%.1f s: activa=%s capitulo=%d voz=%s (audio en el mod %s)", ClientCine.time(),
                        ClientCine.active(), ClientCine.chapter(), ClientCine.narrationPlaying(), com.darkcraft.cine.Timeline.hasAudio(5)));
            });
        }
        int finCine = C5 + Math.round(com.darkcraft.cine.Timeline.total(5) * 20) + 30;
        atTicks(finCine, "Captura: el cofre del capitulo 5", m -> {
            m.options.hideGui = false;
            shot(m, "cliente_g72_cofre5");
            StringBuilder b = new StringBuilder("  cofre abierto: " + (m.screen == null ? "NO" : m.screen.getClass().getSimpleName()) + " |");
            if (m.player.containerMenu instanceof net.minecraft.world.inventory.ChestMenu cm)
                for (int i = 0; i < 27; i++) {
                    ItemStack it = cm.getContainer().getItem(i);
                    if (!it.isEmpty()) b.append(' ').append(i).append('=').append(it.getCount()).append('x')
                            .append(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(it.getItem()).getPath());
                }
            log(b.toString());
            server(m, sp -> log("  logro capitulo5=" + com.darkcraft.story.Logros.tiene(sp, com.darkcraft.story.Logros.capitulo(5))
                    + " | cinematica vista=" + com.darkcraft.story.Story.seenCine(sp, 5)));
        });
        atTicks(finCine + 8, "Cerrar el cofre", m -> {
            m.player.closeContainer();
            m.setScreen(null);
        });
        return finCine + 20;
    }

    // ------------------------------------------------------------------ la Fortaleza de Hueso (parte 3)

    private static com.darkcraft.fortaleza.Fortalezas.Sitio fsit;
    private static com.darkcraft.gen.FortalezaPlan fplan;
    private static int fbase;

    private static String fpos(ServerPlayer sp) {
        return String.format(java.util.Locale.ROOT, "%.1f %.1f %.1f", sp.getX(), sp.getY(), sp.getZ());
    }

    private static Vec3 fv(double dx, double dy, double dz) {
        return new Vec3(fplan.cx + 0.5 + dx, fplan.Y + dy, fplan.cz + 0.5 + dz);
    }

    /** La Fortaleza de Hueso: se levanta sola al llegar, el puente de la llave, la corriente, la puerta, el acertijo, la forja y el capitulo 6. */
    private static int fortaleza(int F) {
        final int PZ = com.darkcraft.gen.FortalezaHueso.PZ;
        atTicks(F, "Fortaleza: a la Fortaleza de Hueso del reino (volando, en creativo)", m -> {
            enPrimeraPersona(m);
            m.options.hideGui = true;
            server(m, sp -> {
                ServerLevel lvl = sp.serverLevel();
                fsit = com.darkcraft.fortaleza.Fortalezas.delReino(lvl.getSeed(), castle[0], castle[1]);
                fplan = com.darkcraft.fortaleza.Fortalezas.plan(lvl.getSeed(), fsit);
                log("fortaleza en " + fsit.x() + "," + fsit.z() + " (reino " + fsit.reinoX() + "," + fsit.reinoZ() + ") Y " + fplan.Y
                        + " borde de la isla z " + fplan.bordeZ + ", " + fplan.chunks().size() + " chunks en el plano");
                sp.setGameMode(GameType.CREATIVE);
                sp.getAbilities().flying = true;
                sp.onUpdateAbilities();
                sp.teleportTo(lvl, fplan.cx + 0.5, fplan.Y + 34, fplan.cz + 0.5, 180f, 40f);
            });
        });
        for (int k = 1; k <= 6; k++) {
            final int kk = k;
            atTicks(F + k * 100, "Fortaleza: se va levantando", m -> {
                log("  musica: " + ClientMusic.debug());
                server(m, sp -> {
                    var est = com.darkcraft.fortaleza.FortalezaDatos.get(sp.server).de(fsit);
                    log("  fortaleza a los " + kk * 5 + " s: " + est.hechos.size() + " de " + fplan.chunks().size() + " chunks; zona="
                            + (com.darkcraft.fortaleza.FortalezaEventos.zona(sp) != null) + " lluvia=" + sp.serverLevel().isRaining());
                });
            });
        }
        int E = F + 640;
        // (las posiciones se calculan al llegar el paso: el plano de la fortaleza aun no existe al preparar la prueba)
        String[] nombres = {"cliente_f01_fortaleza", "cliente_f02_fortaleza_lado", "cliente_f03_desde_arriba", "cliente_f04_desde_abajo"};
        double[][] desde = {{78, 26, 72, 0, 14, -4}, {-66, 12, 40, 0, 10, -2}, {12, 64, 44, 0, 0, 0}, {70, -26, 58, 0, -12, 0}};
        for (int k = 0; k < nombres.length; k++) {
            final int kk = k;
            atTicks(E + k * 6, "Fortaleza desde fuera", m -> {
                if (fplan == null) return;
                double[] d = desde[kk];
                verDesde(m, fv(d[0], d[1], d[2]), fv(d[3], d[4], d[5]));
            });
            atTicks(E + k * 6 + 4, "captura", m -> shot(m, nombres[kk]));
        }
        // el puente de hueso y nubes: con la llave, al pie de la escalera
        int P = E + 40;
        atTicks(P, "Puente: con la llave al pie de la escalera", m -> {
            enPrimeraPersona(m);
            m.options.hideGui = true;
            server(m, sp -> {
                ServerLevel lvl = sp.serverLevel();
                int[] ce = com.darkcraft.fortaleza.Puente.centro(fplan);
                fbase = com.darkcraft.fortaleza.Puente.base(lvl, fplan);
                log("  escalera en " + ce[0] + "," + ce[1] + ", suelo a " + fbase + " (sube " + (fplan.Y - fbase) + " bloques)");
                sp.teleportTo(lvl, ce[0] + 10.5, fbase + 1, ce[1] + 10.5, 135f, -10f);
                sp.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(DarkCraftMod.LLAVE_HUESO.get()));
                sp.gameMode.useItem(sp, lvl, sp.getMainHandItem(), InteractionHand.MAIN_HAND);
                var est = com.darkcraft.fortaleza.FortalezaDatos.get(sp.server).de(fsit);
                log("  puente: " + est.puente + " base " + est.baseEscalera + " logro puente=" + com.darkcraft.story.Logros.tiene(sp, "puente"));
            });
        });
        int[][] vistas = {{20, 0}, {90, 1}, {180, 2}};
        for (int[] v : vistas) {
            final int vk = v[1];
            atTicks(P + v[0], "Puente: se levanta", m -> {
                int[] ce = com.darkcraft.fortaleza.Puente.centro(fplan);
                double alto = fbase + (fplan.Y - fbase) * (0.3 + 0.35 * vk);
                verDesde(m, new Vec3(ce[0] + 38 + vk * 6, alto + 14, ce[1] + 44 + vk * 6), new Vec3(ce[0] + 0.5, alto - 6, ce[1] + 0.5));
            });
            atTicks(P + v[0] + 4, "captura", m -> shot(m, "cliente_f05_puente_" + vk));
        }
        atTicks(P + 190, "Puente: desde arriba", m -> {
            int[] ce = com.darkcraft.fortaleza.Puente.centro(fplan);
            verDesde(m, new Vec3(ce[0] + 14, fplan.Y + 9, ce[1] - 16), new Vec3(ce[0] + 0.5, fplan.Y - 4, ce[1] + 3));
        });
        atTicks(P + 194, "captura", m -> shot(m, "cliente_f06_puente_arriba"));
        // la corriente de nubes: en supervivencia, en el hueco de la escalera
        atTicks(P + 200, "Corriente de nubes: al hueco de la escalera", m -> {
            enPrimeraPersona(m);
            m.options.hideGui = false;
            server(m, sp -> {
                int[] ce = com.darkcraft.fortaleza.Puente.centro(fplan);
                sp.setGameMode(GameType.SURVIVAL);
                sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 400, 4));
                sp.teleportTo(sp.serverLevel(), ce[0] + 0.5, fbase + 0.2, ce[1] + 0.5, 180f, -35f);
                log("  corriente: empieza en " + fpos(sp));
            });
        });
        atTicks(P + 230, "captura", m -> shot(m, "cliente_f07_corriente"));
        atTicks(P + 260, "Corriente: altura", m -> server(m, sp -> log("  corriente a los 3 s: " + fpos(sp) + " levitacion="
                + sp.hasEffect(net.minecraft.world.effect.MobEffects.LEVITATION))));
        atTicks(P + 300, "Corriente: altura", m -> server(m, sp -> log("  corriente a los 5 s: " + fpos(sp))));
        // la puerta: con la llave delante de la reja (cinematica)
        int D = P + 310;
        atTicks(D, "Puerta: con la llave delante de la reja", m -> {
            m.options.hideGui = false;
            server(m, sp -> {
                ServerLevel lvl = sp.serverLevel();
                sp.setGameMode(GameType.CREATIVE);
                sp.removeEffect(net.minecraft.world.effect.MobEffects.LEVITATION);
                sp.teleportTo(lvl, fplan.cx + 0.5, fplan.Y, fplan.cz + PZ + 8.5, 180f, 0f);
                sp.getCooldowns().removeCooldown(DarkCraftMod.LLAVE_HUESO.get());
                sp.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(DarkCraftMod.LLAVE_HUESO.get()));
                sp.gameMode.useItem(sp, lvl, sp.getMainHandItem(), InteractionHand.MAIN_HAND);
                log("  puerta abierta=" + com.darkcraft.fortaleza.FortalezaDatos.get(sp.server).de(fsit).puerta + " cine=" + Cinematics.active(sp));
            });
        });
        int[] tp = {30, 110, 160, 210, 270};
        for (int k = 0; k < tp.length; k++) {
            final int kk = k;
            atTicks(D + tp[k], "captura de la puerta", m -> {
                shot(m, "cliente_f10_puerta_" + kk);
                log(String.format(java.util.Locale.ROOT, "  puerta t=%.1f s: cine %s (%d)", ClientCine.time(), ClientCine.active(), ClientCine.chapter()));
            });
        }
        atTicks(D + 330, "Puerta: la reja arriba", m -> server(m, sp -> {
            int[] r = fplan.f.reja();
            ServerLevel lvl = sp.serverLevel();
            log("  reja: abajo " + lvl.getBlockState(new BlockPos(fplan.cx, r[1], r[2])).getBlock() + ", arriba "
                    + lvl.getBlockState(new BlockPos(fplan.cx, r[4] + 3, r[2])).getBlock() + "; logro puerta_hueso="
                    + com.darkcraft.story.Logros.tiene(sp, "puerta_hueso"));
        }));
        // el patio, los caballeros y la torre del homenaje
        int H = D + 340;
        atTicks(H, "Patio: dentro de las murallas, con caballeros zombi", m -> {
            enPrimeraPersona(m);
            m.options.hideGui = false;
            server(m, sp -> {
                sp.teleportTo(sp.serverLevel(), fplan.cx + 0.5, fplan.Y, fplan.cz + 22.5, 180f, -12f);
                int n = com.darkcraft.caballeros.Caballeros.grupo(sp.serverLevel(), sp, 3);
                log("  caballeros en el patio: " + n);
            });
        });
        atTicks(H + 30, "captura", m -> shot(m, "cliente_f11_patio"));
        atTicks(H + 34, "Caballeros de cerca", m -> {
            var l = m.level.getEntitiesOfClass(com.darkcraft.caballeros.CaballeroZombi.class, m.player.getBoundingBox().inflate(60));
            if (!l.isEmpty()) {
                var c = l.get(0);
                verDesde(m, c.position().add(3.2, 2.2, 3.2), c.position().add(0, 1.2, 0));
            }
            log("  caballeros que ve el cliente: " + l.size());
        });
        atTicks(H + 40, "captura", m -> shot(m, "cliente_f12_caballeros"));
        atTicks(H + 44, "Torre del homenaje: la planta baja", m -> {
            enPrimeraPersona(m);
            server(m, sp -> {
                sp.serverLevel().getEntitiesOfClass(com.darkcraft.caballeros.CaballeroZombi.class, sp.getBoundingBox().inflate(80))
                        .forEach(net.minecraft.world.entity.Entity::discard);
                sp.teleportTo(sp.serverLevel(), fplan.cx + 0.5, fplan.Y, fplan.cz - 8.5, 180f, 8f);
            });
        });
        atTicks(H + 70, "captura", m -> shot(m, "cliente_f13_homenaje"));
        atTicks(H + 72, "El cofre del acertijo", m -> server(m, sp -> {
            int[] c = fplan.f.cofre();
            BlockPos b = new BlockPos(c[0], c[1], c[2]);
            sp.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            sp.gameMode.useItemOn(sp, sp.serverLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(b), Direction.SOUTH, b, false));
        }));
        atTicks(H + 84, "captura del cofre", m -> {
            shot(m, "cliente_f14_cofre");
            StringBuilder b = new StringBuilder("  cofre del acertijo: " + (m.screen == null ? "NO" : m.screen.getClass().getSimpleName()) + " |");
            if (m.player.containerMenu instanceof net.minecraft.world.inventory.ChestMenu cm)
                for (int i = 0; i < cm.getContainer().getContainerSize(); i++) {
                    ItemStack it = cm.getContainer().getItem(i);
                    if (!it.isEmpty()) b.append(' ').append(it.getCount()).append('x')
                            .append(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(it.getItem()).getPath());
                }
            log(b.toString());
        });
        atTicks(H + 88, "El libro del acertijo", m -> {
            m.player.closeContainer();
            m.setScreen(null);
            server(m, sp -> sp.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(DarkCraftMod.LIBRO_ACERTIJO.get())));
        });
        atTicks(H + 94, "leer el acertijo", m -> m.gameMode.useItem(m.player, InteractionHand.MAIN_HAND));
        for (int k = 0; k < 4; k++) {
            final int kk = k;
            atTicks(H + 100 + k * 8, "pagina del acertijo", m -> {
                if (m.screen instanceof com.darkcraft.client.fortaleza.AcertijoScreen a) a.probar(Math.min(kk, 2), kk == 3);
                else log("  el acertijo no esta abierto: " + (m.screen == null ? "nada" : m.screen.getClass().getSimpleName()));
            });
            atTicks(H + 106 + k * 8, "captura", m -> shot(m, kk < 3 ? "cliente_f15_acertijo_" + (kk + 1) : "cliente_f16_forja"));
        }
        atTicks(H + 136, "El atril de la planta baja (libro perdido)", m -> {
            m.setScreen(null);
            server(m, sp -> {
                log("  logro acertijo=" + com.darkcraft.story.Logros.tiene(sp, "acertijo") + " receta conocida="
                        + sp.getRecipeBook().contains(new net.minecraft.resources.ResourceLocation("darkcraft", "amuleto_hueso")));
                int[] a = fplan.f.atriles()[1];
                BlockPos b = new BlockPos(a[0], a[1], a[2]);
                log("  atril: " + sp.serverLevel().getBlockState(b));
                sp.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                sp.gameMode.useItemOn(sp, sp.serverLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(b), Direction.UP, b, false));
            });
        });
        atTicks(H + 150, "captura", m -> {
            shot(m, "cliente_f17_atril");
            server(m, sp -> log("  libros perdidos: " + Integer.toBinaryString(com.darkcraft.espejo.Espejo.perdidos(sp))
                    + " logro libro_perdido=" + com.darkcraft.story.Logros.tiene(sp, "libro_perdido")));
        });
        // la forja del amuleto (como si se acabara de craftear en la mesa)
        int A = H + 156;
        atTicks(A, "Forja del amuleto: la cinematica", m -> {
            m.setScreen(null);
            m.player.closeContainer();
            server(m, sp -> {
                BlockPos mesa = sp.blockPosition().relative(Direction.NORTH, 2);
                sp.serverLevel().setBlock(mesa, net.minecraft.world.level.block.Blocks.CRAFTING_TABLE.defaultBlockState(), 3);
                ItemStack am = new ItemStack(DarkCraftMod.AMULETO_HUESO.get());
                sp.getInventory().add(am.copy());
                net.minecraftforge.event.ForgeEventFactory.firePlayerCraftingEvent(sp, am, new net.minecraft.world.SimpleContainer(9));
                log("  forja: cine=" + Cinematics.active(sp) + " logro amuleto=" + com.darkcraft.story.Logros.tiene(sp, "amuleto"));
            });
        });
        int[] ta = {20, 60, 110, 150};
        for (int k = 0; k < ta.length; k++) {
            final int kk = k;
            atTicks(A + ta[k], "captura de la forja", m -> shot(m, "cliente_f20_forja_" + kk));
        }
        // lo alto de la torre: el altar, el amuleto y el capitulo 6 (se abre el portal)
        int T = A + 190;
        int top = com.darkcraft.gen.FortalezaHueso.NPISOS * com.darkcraft.gen.FortalezaHueso.PISO;
        atTicks(T, "Azotea: junto al altar", m -> {
            enPrimeraPersona(m);
            m.options.hideGui = false;
            server(m, sp -> {
                int[] al = fplan.f.altar();
                sp.teleportTo(sp.serverLevel(), al[0] + 0.5, al[1], al[2] + 2.5, 180f, -8f);
                log("  altar: " + sp.serverLevel().getBlockState(new BlockPos(al[0], al[1] + 1, al[2])).getBlock());
            });
        });
        atTicks(T + 30, "captura", m -> shot(m, "cliente_f30_azotea"));
        atTicks(T + 34, "El amuleto en el altar: capitulo 6", m -> server(m, sp -> {
            sp.getCooldowns().removeCooldown(DarkCraftMod.AMULETO_HUESO.get());
            ItemStack am = new ItemStack(DarkCraftMod.AMULETO_HUESO.get());
            sp.setItemInHand(InteractionHand.MAIN_HAND, am);
            sp.gameMode.useItem(sp, sp.serverLevel(), am, InteractionHand.MAIN_HAND);
            log("  amuleto usado: portal=" + com.darkcraft.fortaleza.FortalezaDatos.get(sp.server).de(fsit).portal + " cine=" + Cinematics.active(sp)
                    + " logro portal=" + com.darkcraft.story.Logros.tiene(sp, "portal"));
        }));
        float tot6 = Timeline.total(6);
        float[] t6 = {1.6f, 9f, Timeline.cut(6, 0) + 2f, Timeline.cut(6, 1) + 2f, Timeline.cut(6, 2) + 3.5f, Timeline.cut(6, 3) + 2f,
                Timeline.cut(6, 4) + 2f, tot6 - 1.5f};
        int C6 = T + 34;
        for (int k = 0; k < t6.length; k++) {
            final int kk = k;
            atTicks(C6 + Math.round(t6[k] * 20), "captura del capitulo 6", m -> {
                shot(m, "cliente_f3" + (kk + 1) + "_capitulo6_" + kk);
                log(String.format(java.util.Locale.ROOT, "  capitulo 6 t=%.1f s: activa=%s capitulo=%d voz=%s (audio en el mod %s)", ClientCine.time(),
                        ClientCine.active(), ClientCine.chapter(), ClientCine.narrationPlaying(), Timeline.hasAudio(6)));
            });
        }
        int Q = C6 + Math.round(tot6 * 20) + 30;
        atTicks(Q, "El portal abierto", m -> {
            enPrimeraPersona(m);
            server(m, sp -> {
                int[] q = fplan.f.portal();
                log("  portal: " + sp.serverLevel().getBlockState(new BlockPos(fplan.cx, q[1] + 3, q[2])).getBlock() + "; libro 6 en el inventario="
                        + sp.getInventory().items.stream().anyMatch(st -> st.hasTag() && st.getTag().getInt("darkcraft_libro") == 6)
                        + "; logros capitulo6=" + com.darkcraft.story.Logros.tiene(sp, "capitulo6"));
            });
        });
        atTicks(Q + 10, "captura", m -> shot(m, "cliente_f40_portal"));
        atTicks(Q + 12, "Tocar el portal", m -> server(m, sp -> {
            int[] q = fplan.f.portal();
            sp.teleportTo(sp.serverLevel(), fplan.cx + 0.5, q[1] + 1, q[2] + 0.5, 180f, 0f);
        }));
        atTicks(Q + 30, "El portal empuja", m -> server(m, sp -> log("  despues de tocar el portal: " + fpos(sp) + " (el portal esta en z "
                + fplan.f.portal()[2] + ")")));
        atTicks(Q + 34, "El libro de la M: capitulo 6 y un libro perdido", m -> {
            com.darkcraft.client.espejo.LibroScreen.abrir();
            if (m.screen instanceof com.darkcraft.client.espejo.LibroScreen l) l.probar(0, 0, 6);
        });
        atTicks(Q + 42, "captura", m -> shot(m, "cliente_f41_libro_cap6"));
        atTicks(Q + 44, "libro perdido", m -> {
            if (m.screen instanceof com.darkcraft.client.espejo.LibroScreen l) l.probar(0, 0, 111);
        });
        atTicks(Q + 52, "captura", m -> shot(m, "cliente_f42_libro_perdido"));
        atTicks(Q + 54, "coleccion", m -> {
            if (m.screen instanceof com.darkcraft.client.espejo.LibroScreen l) l.probar(2, 1, 0);
        });
        atTicks(Q + 62, "captura", m -> shot(m, "cliente_f43_coleccion"));
        atTicks(Q + 64, "El espejo en la fortaleza", m -> {
            m.setScreen(null);
            enPrimeraPersona(m);
            log("  espejo en la fortaleza: " + com.darkcraft.client.espejo.EspejoCliente.resumen());
        });
        atTicks(Q + 80, "captura", m -> {
            shot(m, "cliente_f44_espejo_fortaleza");
            server(m, sp -> {
                StringBuilder b = new StringBuilder("  logros de la fortaleza:");
                for (String id : new String[]{"fortaleza", "puente", "puerta_hueso", "acertijo", "amuleto", "portal", "capitulo6", "libro_perdido"})
                    b.append(' ').append(id).append('=').append(com.darkcraft.story.Logros.tiene(sp, id) ? "SI" : "no");
                log(b.toString());
            });
        });
        return Q + 90;
    }

    // ------------------------------------------------------------------ el Mandoble del Juramento (parte B3)

    /** El Mandoble del Juramento en la Sala del Arbol ya en flor: a dos manos, energia, Raices del Juramento y Juicio de Espinas. */
    private static int mandoble(int t0) {
        final double[] o = new double[3];
        atTicks(t0, "Mandoble: en la sala, con tres dianas", m -> {
            enPrimeraPersona(m);
            server(m, sp -> {
                ServerLevel lvl = sp.serverLevel();
                o[0] = hall[0] - 7.5;
                o[1] = hall[2];
                o[2] = hall[1] + 28.5;
                sp.setGameMode(GameType.SURVIVAL);
                sp.getInventory().clearContent();
                sp.getInventory().selected = 0;
                sp.getInventory().items.set(0, new ItemStack(DarkCraftMod.MANDOBLE_JURAMENTO.get()));
                sp.setHealth(sp.getMaxHealth());
                sp.getFoodData().setFoodLevel(20);
                com.darkcraft.armas.Combos.sumar(sp, -com.darkcraft.armas.Combos.MAX);
                sp.teleportTo(lvl, o[0], o[1], o[2], -90f, 12f);
                dianas.clear();
                for (int i = 0; i < 3; i++) {
                    net.minecraft.world.entity.monster.Husk h = net.minecraft.world.entity.EntityType.HUSK.create(lvl);
                    if (h == null) continue;
                    h.moveTo(o[0] + 3.0 + (i == 1 ? 0.7 : 0), o[1], o[2] + (i - 1) * 1.5, 90f, 0f);
                    h.setYHeadRot(90f);
                    h.setYBodyRot(90f);
                    h.setNoAi(true);
                    h.setPersistenceRequired();
                    h.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(600);
                    h.setHealth(600);
                    h.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
                    lvl.addFreshEntity(h);
                    dianas.add(h);
                }
                log("  mandoble al empezar: " + estadoArmas(sp) + " | suelo " + lvl.getBlockState(BlockPos.containing(o[0], o[1] - 0.5, o[2])));
            });
        });
        atTicks(t0 + 30, "captura: el mandoble en primera persona", m -> shot(m, "cliente_m01_mandoble"));
        atTicks(t0 + 33, "el mandoble a dos manos, de lado", m -> {
            m.options.hideGui = true;
            verDesde(m, new Vec3(o[0] + 2.2, o[1] + 1.9, o[2] - 3.6), new Vec3(o[0] + 0.2, o[1] + 1.1, o[2]));
        });
        atTicks(t0 + 38, "captura", m -> shot(m, "cliente_m02_dos_manos"));
        int t = t0 + 42;
        atTicks(t, "de vuelta a primera persona", ClientCiTest::enPrimeraPersona);
        t += 4;
        for (int i = 0; i < 7; i++) {
            final int k = i;
            atTicks(t, "golpe con el mandoble " + (i + 1), m -> golpe(m, k));
            t += 15;
        }
        logArmasAt(t, "tras 7 golpes con el mandoble (deberia haber 77)");
        atTicks(t + 2, "Con una antorcha en la otra mano: Shift + clic (no debe salir)", m -> {
            server(m, sp -> sp.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(net.minecraft.world.item.Items.TORCH)));
        });
        atTicks(t + 6, "Shift + clic con la otra mano ocupada", m -> combo(m, false));
        atTicks(t + 10, "captura del aviso de dos manos", m -> shot(m, "cliente_m03_dos_manos_aviso"));
        logArmasAt(t + 12, "tras pedir las Raices con la otra mano ocupada (sigue en 77)");
        atTicks(t + 14, "quitar la antorcha", m -> server(m, sp -> sp.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY)));
        t += 20;
        atTicks(t, "Shift + clic: Raices del Juramento", m -> {
            m.options.hideGui = true;
            verDesde(m, new Vec3(o[0] + 5.0, o[1] + 3.2, o[2] - 8.5), new Vec3(o[0] + 6.5, o[1] + 0.8, o[2]));
            combo(m, false);
        });
        int[] ra = {4, 10, 16, 22};
        for (int i = 0; i < ra.length; i++) {
            final int k = i;
            atTicks(t + ra[i], "captura de las Raices del Juramento", m -> shot(m, "cliente_m04_raices_" + k));
        }
        logArmasAt(t + 30, "tras las Raices del Juramento");
        t += 36;
        atTicks(t, "de vuelta a primera persona", ClientCiTest::enPrimeraPersona);
        t += 4;
        for (int i = 0; i < 6; i++) {
            final int k = i;
            atTicks(t, "golpe con el mandoble " + (i + 8), m -> golpe(m, k));
            t += 15;
        }
        logArmasAt(t, "tras 6 golpes mas (deberia haber 100)");
        t += 4;
        atTicks(t, "Ctrl + clic: Juicio de Espinas", m -> {
            m.options.hideGui = true;
            verDesde(m, new Vec3(o[0] + 3.0, o[1] + 3.6, o[2] - 9.0), new Vec3(o[0] + 1.0, o[1] + 0.9, o[2]));
            combo(m, true);
        });
        int[] es = {6, 12, 16, 22, 30};
        for (int i = 0; i < es.length; i++) {
            final int k = i;
            atTicks(t + es[i], "captura del Juicio de Espinas", m -> shot(m, "cliente_m05_espinas_" + k));
        }
        logArmasAt(t + 40, "tras el Juicio de Espinas");
        t += 46;
        atTicks(t, "Fin del mandoble: se quitan las dianas", m -> {
            enPrimeraPersona(m);
            server(m, sp -> {
                log("  mandoble al final: " + estadoArmas(sp));
                for (net.minecraft.world.entity.LivingEntity d : dianas) d.discard();
                dianas.clear();
                sp.getInventory().clearContent();
                sp.setHealth(sp.getMaxHealth());
                sp.setGameMode(GameType.CREATIVE);
            });
        });
        return t + 20;
    }

    /** Lanza un comando como el jugador (con permiso de trucos) y apunta si ha funcionado (1) o no (0). */
    private static void comando(ServerPlayer sp, String cmd) {
        int r = sp.getServer().getCommands().performPrefixedCommand(sp.createCommandSourceStack().withPermission(4), cmd);
        log("  /" + cmd + " -> " + r);
    }

    /**
     * Escribe en el informe los bloques de una caja, en cortes verticales (uno por cada z, de atras hacia delante): una
     * letra por bloque, con su leyenda ('.' es aire). Para buscar huecos sin tener que verlo.
     */
    private static void volcar(ServerLevel lvl, String que, int x0, int x1, int y0, int y1, int z0, int z1) {
        java.util.Map<String, Character> letras = new java.util.LinkedHashMap<>();
        String abc = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder b = new StringBuilder("  bloques de " + que + " (x de " + x0 + " a " + x1 + ", y de " + y1 + " a " + y0 + "):\n");
        for (int z = z0; z <= z1; z++) {
            b.append("   z=").append(z).append('\n');
            for (int y = y1; y >= y0; y--) {
                b.append(String.format("   %4d ", y));
                for (int x = x0; x <= x1; x++) {
                    var st = lvl.getBlockState(new BlockPos(x, y, z));
                    if (st.isAir()) {
                        b.append('.');
                        continue;
                    }
                    String n = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(st.getBlock()).getPath();
                    Character c = letras.get(n);
                    if (c == null) {
                        c = letras.size() < abc.length() ? abc.charAt(letras.size()) : '?';
                        letras.put(n, c);
                    }
                    b.append(c);
                }
                b.append('\n');
            }
        }
        b.append("   leyenda:");
        letras.forEach((n, c) -> b.append(' ').append(c).append('=').append(n));
        log(b.toString());
    }

    /** Lleva el instante t de unos cortes de plano (viejos) a otros (nuevos), en linea recta entre cada dos. */
    private static float llevar(float t, float[] viejos, float[] nuevos) {
        for (int i = 0; i < viejos.length - 1; i++)
            if (t <= viejos[i + 1]) return nuevos[i] + (nuevos[i + 1] - nuevos[i]) * (t - viejos[i]) / (viejos[i + 1] - viejos[i]);
        return nuevos[nuevos.length - 1];
    }

    /** Instante a la fraccion f del plano g (cortes: el principio de cada plano y el final del ultimo). */
    private static float mitad(float[] cortes, int g, float f) {
        return cortes[g] + (cortes[g + 1] - cortes[g]) * f;
    }

    private static void mirarA(net.minecraft.server.level.ServerPlayer sp, Vec3 p) {
        Vec3 d = p.subtract(sp.getEyePosition());
        float yaw = (float) (Math.atan2(d.z, d.x) * 180 / Math.PI) - 90f;
        float pitch = (float) -(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)) * 180 / Math.PI);
        sp.setYRot(yaw);
        sp.setYHeadRot(yaw);
        sp.setXRot(pitch);
    }

    /** Cada tick de la pelea con Gavran: camara de lado (dentro del anillo de la sala), golpes del jugador y registro. */
    private static void gavranTick(Minecraft mc) {
        if (gavranStart < 0 || mc.level == null || mc.player == null || hall == null) return;
        int t = ticks - gavranStart;
        com.darkcraft.gavran.Gavran cg = null;
        for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering())
            if (e instanceof com.darkcraft.gavran.Gavran g) cg = g;
        if (seguirG && cg != null && !ClientCine.active()) {
            // camara fija en el anillo, a un lado de Gavran (el tronco no se mete en medio) y a media altura
            Vec3 b = cg.position();
            double cx = hall[0] + 0.5, cz = hall[1] + 0.5;
            double ag = Math.atan2(b.z - cz, b.x - cx) + 0.62;
            Vec3 want = new Vec3(cx + Math.cos(ag) * 31, hall[2] + 9.5, cz + Math.sin(ag) * 31);
            obsPos = obsPos == null || obsPos.distanceToSqr(want) > 400 ? want : obsPos.add(want.subtract(obsPos).scale(0.1));
            verDesde(mc, obsPos, b.add(0, 3.0, 0));
        }
        if (t % 20 == 0 && t < 20 * 280)
            server(mc, sp -> gavranCerca(sp).ifPresent(g -> log("  gavran: " + g.debug() + " | jugador vida " + Math.round(sp.getHealth())
                    + " en " + Math.round(sp.getX()) + "," + Math.round(sp.getY()) + "," + Math.round(sp.getZ()))));
        if (seguirG && t % 13 == 0)
            server(mc, sp -> gavranCerca(sp).ifPresent(g -> {
                if (g.distanceTo(sp) < 5.5 && g.luchando() && g.estado() != com.darkcraft.gavran.Gavran.Estado.FIN1) {
                    float before = g.getHealth();
                    sp.attack(g);
                    sp.swing(InteractionHand.MAIN_HAND, true);
                    if (++ataques % 5 == 0) log("  golpe del jugador: vida de gavran " + Math.round(before) + " -> " + Math.round(g.getHealth()));
                }
                sp.setHealth(sp.getMaxHealth());
            }));
    }

    // ------------------------------------------------------------------ camara de observador

    private static com.darkcraft.client.CineCamera obs;
    private static Vec3 obsPos;

    private static void verDesde(Minecraft mc, Vec3 pos, Vec3 look) {
        if (obs == null || obs.level() != mc.level) obs = new com.darkcraft.client.CineCamera(mc.level);
        Vec3 d = look.subtract(pos);
        float yaw = (float) (Math.atan2(d.z, d.x) * 180 / Math.PI) - 90f;
        float pitch = (float) -(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)) * 180 / Math.PI);
        obs.moveTo(pos.x, pos.y - com.darkcraft.client.CineCamera.EYE, pos.z, yaw, pitch);
        obs.setOldPosAndRot();
        mc.setCameraEntity(obs);
        mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
    }

    private static int reyStart = -1;

    private static java.util.Optional<com.darkcraft.rey.Rey> reyCerca(net.minecraft.server.level.ServerPlayer sp) {
        return sp.serverLevel().getEntitiesOfClass(com.darkcraft.rey.Rey.class, sp.getBoundingBox().inflate(120)).stream().findFirst();
    }

    /** Cada tick de la pelea: mirar al rey, atacarle de vez en cuando y anotar como va. */
    private static void reyTick(Minecraft mc) {
        if (reyStart < 0 || mc.level == null || mc.player == null) return;
        int t = ticks - reyStart;
        com.darkcraft.rey.Rey cr = null;
        for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering())
            if (e instanceof com.darkcraft.rey.Rey r) cr = r;
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
