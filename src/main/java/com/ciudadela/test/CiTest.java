package com.ciudadela.test;

import com.ciudadela.CiudadelaMod;
import com.ciudadela.gen.CastlePlan;
import com.ciudadela.world.CastleSites;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Prueba automatica (solo en GitHub Actions): genera una ciudadela completa en un servidor sin
 * jugadores, dibuja mapas PNG (vista aerea, cortes y plantas) y apaga el servidor.
 */
public final class CiTest {
    private CiTest() {}

    public static void run(MinecraftServer server) {
        File out = new File("ci-out");
        out.mkdirs();
        try (PrintWriter log = new PrintWriter(new File(out, "informe.txt"))) {
            ServerLevel lvl = server.overworld();
            long seed = lvl.getSeed();
            log.println("Altura del mundo: " + lvl.getMinBuildHeight() + " .. " + lvl.getMaxBuildHeight());
            dumpModBlocks(new File(out, "bloques_mods.txt"));
            try (PrintWriter o = new PrintWriter(new File(out, "orientacion.txt"))) {
                o.print(com.ciudadela.gen.ModDeco.report());
            }
            int[] s = CastleSites.nearest(seed, 0, 0, 3);
            if (s == null) {
                log.println("No se encontro ninguna ciudadela");
                return;
            }
            log.println("Ciudadela en " + s[0] + ", " + s[1]);
            int R = CastlePlan.FOOTPRINT;
            int c0x = (s[0] - R) >> 4, c1x = (s[0] + R) >> 4, c0z = (s[1] - R) >> 4, c1z = (s[1] + R) >> 4;
            long t0 = System.currentTimeMillis();
            int n = 0;
            Map<String, Integer> be = new TreeMap<>();
            for (int cx = c0x; cx <= c1x; cx++) {
                for (int cz = c0z; cz <= c1z; cz++) {
                    ChunkAccess ch = lvl.getChunk(cx, cz, ChunkStatus.FULL, true);
                    n++;
                    if (ch instanceof LevelChunk lc) {
                        for (BlockEntity b : lc.getBlockEntities().values()) {
                            String k = net.minecraftforge.registries.ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(b.getType()).getPath();
                            be.merge(k, 1, Integer::sum);
                        }
                    }
                }
                if ((cx - c0x) % 5 == 0) CiudadelaMod.LOGGER.info("[CI] columnas de chunks generadas: {}/{}", cx - c0x + 1, c1x - c0x + 1);
            }
            long t1 = System.currentTimeMillis();
            log.println("Chunks generados: " + n + " en " + (t1 - t0) / 1000 + " s (" + (t1 - t0) / Math.max(1, n) + " ms/chunk)");
            log.println("Entidades de bloque: " + be);

            int Y0 = CastlePlan.Y0;
            topView(lvl, s[0], s[1], R, new File(out, "1_vista_aerea.png"));
            sectionX(lvl, s[0], s[1], R, new File(out, "2_corte_este_oeste.png"), 0);
            sectionX(lvl, s[0], s[1], R, new File(out, "3_corte_gran_torre.png"), -15);
            sectionZ(lvl, s[0], s[1], R, new File(out, "4_corte_norte_sur.png"), 0);
            sectionZ(lvl, s[0], s[1], R, new File(out, "5_corte_catedral_ns.png"), 92);
            plan(lvl, s[0], s[1], 150, Y0 + 1, new File(out, "6_planta_baja.png"));
            plan(lvl, s[0], s[1], 150, Y0 + 15, new File(out, "7_planta_1.png"));
            plan(lvl, s[0], s[1], 150, Y0 + 24, new File(out, "8_planta_2.png"));
            plan(lvl, s[0], s[1], 150, Y0 - 6, new File(out, "9_sotano_1.png"));
            plan(lvl, s[0], s[1], 150, Y0 - 13, new File(out, "10_sotano_2.png"));
            plan(lvl, s[0], s[1], 240, Y0 + 27, new File(out, "11_murallas_ronda.png"));
            plan(lvl, s[0] - 41, s[1] + 7, 20, Y0, new File(out, "14_cocina_torreon_suelo.png"));
            plan(lvl, s[0] - 41, s[1] + 7, 20, Y0 + 1, new File(out, "15_cocina_torreon_encimeras.png"));
            plan(lvl, s[0] - 41, s[1] + 7, 20, Y0 + 2, new File(out, "16_cocina_torreon_armarios.png"));
            plan(lvl, s[0], s[1] - 38, 34, Y0 + 1, new File(out, "17_sala_del_trono.png"));
            // ---- comprobaciones automaticas
            CastlePlan plan = com.ciudadela.world.PlanCache.get(seed, s[0], s[1], (x, z) -> 64);
            try (PrintWriter acc = new PrintWriter(new File(out, "accesos.txt"))) {
                int ok = 0, bad = 0;
                for (com.ciudadela.gen.Element e : plan.elements()) {
                    String res = null;
                    if (e instanceof com.ciudadela.gen.RoundTower rt) {
                        int target = rt.top == com.ciudadela.gen.RoundTower.Top.PLATFORM ? rt.topY + 1 : rt.lastFloor();
                        res = reach(lvl, rt.cx, rt.cz, rt.radius - 1, rt.groundY, target);
                        StringBuilder doorsRes = new StringBuilder();
                        for (double[] d : rt.doorList()) {
                            int dy = (int) d[1];
                            int tx = rt.cx + (int) Math.round(Math.cos(d[0]) * (rt.radius - 2.5)), tz = rt.cz + (int) Math.round(Math.sin(d[0]) * (rt.radius - 2.5));
                            String rr = reachSpot(lvl, rt.cx, rt.cz, rt.radius - 1, rt.groundY, tx, dy, tz);
                            if (!rr.equals("OK")) doorsRes.append(" | puerta y=").append(dy).append(": ").append(rr);
                        }
                        if (doorsRes.length() > 0) res = res.replace("OK", "FALLA puertas") + doorsRes;
                        res = "Torre " + rt.cx + "," + rt.cz + " (radio " + rt.radius + ", " + rt.top + ", plantas " + rt.floors() + "): " + res;
                    } else if (e instanceof com.ciudadela.gen.Building b && b.turretInfo() != null) {
                        int[] t = b.turretInfo();
                        res = "Escalera de edificio " + b.fp + ": " + reach(lvl, t[0], t[1], 3, t[2], t[3]);
                    } else if (e instanceof com.ciudadela.gen.TreeTower tt) {
                        res = reach(lvl, tt.centerX(), tt.centerZ(), com.ciudadela.gen.TreeTower.HALL_R - 2, tt.base(), tt.hallFloor);
                        res = "TORRE-ARBOL -> SALA DEL JEFE (y=" + tt.hallFloor + "): " + res;
                        plan(lvl, tt.centerX(), tt.centerZ(), 26, tt.hallFloor + 1, new File(out, "12_sala_del_jefe.png"));
                        plan(lvl, tt.centerX(), tt.centerZ(), 26, tt.hallFloor + 6, new File(out, "13_sala_del_jefe_ventanas.png"));
                    }
                    if (res == null) continue;
                    if (res.contains("OK")) ok++;
                    else bad++;
                    acc.println(res);
                }
                acc.println("TOTAL: " + ok + " accesibles, " + bad + " con problemas");
                log.println("Accesos (subir a lo alto de cada torre): " + ok + " bien, " + bad + " mal");
            }
            // ---- puertas: que no las tape nada (contrafuertes, pilares, muebles, farolas...)
            try (PrintWriter pw = new PrintWriter(new File(out, "puertas.txt"))) {
                int ok = 0, bad = 0;
                BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
                for (com.ciudadela.gen.Element e : plan.elements()) {
                    if (!(e instanceof com.ciudadela.gen.Building b)) continue;
                    for (int[] en : b.entranceCells()) {
                        String why = blocked(lvl, m, en[0], b.baseY, en[1], en[2], en[3], 3);
                        if (why == null) ok++;
                        else {
                            bad++;
                            pw.println("ENTRADA " + b.fp + " en " + en[0] + "," + b.baseY + "," + en[1] + ": " + why);
                        }
                    }
                    for (int[] dr : b.innerDoors()) {
                        String w1 = blocked(lvl, m, dr[0], dr[1], dr[2], dr[3], dr[4], 2);
                        String w2 = blocked(lvl, m, dr[0], dr[1], dr[2], -dr[3], -dr[4], 2);
                        if (w1 == null && w2 == null) ok++;
                        else {
                            bad++;
                            pw.println("PUERTA " + b.fp + " en " + dr[0] + "," + dr[1] + "," + dr[2] + ": " + (w1 != null ? w1 : w2));
                        }
                    }
                }
                pw.println("TOTAL: " + ok + " despejadas, " + bad + " tapadas");
                log.println("Puertas: " + ok + " despejadas, " + bad + " tapadas");
            }
            // ---- cofres de la cronica con su cartel
            for (int c = 1; c <= 5; c++) {
                int[] sp = plan.storySpot(c);
                if (sp == null) {
                    log.println("Capitulo " + c + ": sin sitio");
                    continue;
                }
                BlockPos cp = new BlockPos(sp[0], sp[1], sp[2]);
                var st = lvl.getBlockState(cp);
                var cbe = lvl.getBlockEntity(cp);
                int tag = cbe == null ? 0 : cbe.getPersistentData().getInt(com.ciudadela.gen.Canvas.STORY_TAG);
                String sign = "sin cartel";
                for (int dx = -1; dx <= 1; dx++)
                    for (int dz = -1; dz <= 1; dz++) {
                        var sb = lvl.getBlockEntity(cp.offset(dx, 0, dz));
                        if (sb instanceof net.minecraft.world.level.block.entity.SignBlockEntity sg)
                            sign = "cartel [" + sg.getFrontText().getMessage(0, false).getString() + " | " + sg.getFrontText().getMessage(1, false).getString() + "]";
                    }
                String what = st.getBlock() == net.minecraft.world.level.block.Blocks.CHEST ? "cofre marca=" + tag : st.toString();
                log.println("Capitulo " + c + " en " + sp[0] + "," + sp[1] + "," + sp[2] + ": " + what + ", " + sign
                        + ((c == 3 || c == 5) ? " (aparece al morir el jefe)" : ""));
            }
            for (int c = 1; c <= 5; c++) {
                var book = com.ciudadela.story.Story.book(c);
                log.println("Libro " + c + ": " + book.getTag().getList("pages", 8).size() + " paginas");
            }
            var md = com.ciudadela.gen.MapShapes.build(plan);
            log.println("Mapa: " + md.shapes().length + " numeros de dibujo, " + md.labels().length + " nombres");
            // ---- camara de la cinematica del capitulo 1: que no atraviese nada
            try (PrintWriter pw = new PrintWriter(new File(out, "camara.txt"))) {
                BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
                var shots = com.ciudadela.cine.Cinematics.preview(plan);
                int hits = 0, pts = 0;
                for (int i = 0; i < shots.size(); i++) {
                    var sh = shots.get(i);
                    int sHits = 0;
                    for (float tt = sh.t0; tt <= sh.t1; tt += 0.25f) {
                        double[] a = sh.at(tt);
                        pts++;
                        int bx = (int) Math.floor(a[0]), by = (int) Math.floor(a[1]), bz = (int) Math.floor(a[2]);
                        String hit = null;
                        for (int dx = -1; dx <= 1 && hit == null; dx++)
                            for (int dy = -1; dy <= 1 && hit == null; dy++)
                                for (int dz = -1; dz <= 1 && hit == null; dz++) {
                                    var st = lvl.getBlockState(m.set(bx + dx, by + dy, bz + dz));
                                    if (!st.isAir() && !st.is(net.minecraft.world.level.block.Blocks.WATER)) hit = st.getBlock().getDescriptionId();
                                }
                        if (hit != null) {
                            sHits++;
                            if (sHits <= 6) pw.println("plano " + (i + 1) + " t=" + String.format("%.2f", tt) + " en " + bx + "," + by + "," + bz + ": " + hit);
                        }
                    }
                    hits += sHits;
                    pw.println("plano " + (i + 1) + ": " + sHits + " choques");
                }
                pw.println("TOTAL: " + hits + " de " + pts + " puntos chocan");
                log.println("Camara del capitulo 1: " + hits + " de " + pts + " puntos chocan con algo");
            }
            // ---- fotos de las salas nuevas
            plan(lvl, s[0] + 40, s[1] - 30, 30, Y0 + 1, new File(out, "18_sala_de_estar.png"), 5);
            plan(lvl, s[0] + 40, s[1] - 30, 30, Y0 + 6, new File(out, "19_sala_de_estar_campana.png"), 5);
            sectionZoomX(lvl, s[0] - 97, s[1] - 70, s[1] + 10, Y0 - 2, Y0 + 40, 5, new File(out, "20_biblioteca_corte.png"));
            var ci = plan.cine();
            plan(lvl, ci.ascX(), ci.ascZ(), 12, Y0, new File(out, "21_torre_ascenso_arriba.png"), 8);
            plan(lvl, s[0], s[1] + 85, 14, Y0, new File(out, "22_fuente_cofre.png"), 8);
            plan(lvl, s[0] + 92, s[1] - 76, 14, Y0 + 1, new File(out, "23_catedral_altar.png"), 8);
            int paintings = lvl.getEntitiesOfClass(net.minecraft.world.entity.decoration.Painting.class,
                    new net.minecraft.world.phys.AABB(s[0] - 60, Y0 - 20, s[1] - 60, s[0] + 60, Y0 + 60, s[1] + 30)).size();
            log.println("Cuadros en el torreon: " + paintings);
            // ---- bioma de tormenta
            log.println("Bioma en el centro de la isla: " + lvl.getBiome(new net.minecraft.core.BlockPos(s[0], Y0, s[1])).unwrapKey().map(k -> k.location().toString()).orElse("?"));
            log.println("Bioma en el suelo bajo la isla: " + lvl.getBiome(new net.minecraft.core.BlockPos(s[0] + 100, 70, s[1] + 100)).unwrapKey().map(k -> k.location().toString()).orElse("?"));
            log.println("Bioma lejos (800 bloques): " + lvl.getBiome(new net.minecraft.core.BlockPos(s[0] + 800, 70, s[1])).unwrapKey().map(k -> k.location().toString()).orElse("?"));
            // ---- libros encantados del botin
            for (String name : new String[]{"biblioteca", "tesoro", "cripta", "dormitorio", "armeria", "alquimia"}) {
                var table = server.getLootData().getLootTable(new net.minecraft.resources.ResourceLocation("ciudadela", "chests/" + name));
                int books = 0, ench = 0;
                for (int i = 0; i < 200; i++) {
                    var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(lvl)
                            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, net.minecraft.world.phys.Vec3.ZERO)
                            .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
                    for (var st : table.getRandomItems(params)) {
                        if (st.is(net.minecraft.world.item.Items.ENCHANTED_BOOK)) {
                            books++;
                            if (!net.minecraft.world.item.EnchantedBookItem.getEnchantments(st).isEmpty()) ench++;
                        }
                    }
                }
                log.println("Botin " + name + ": " + books + " libros encantados en 200 cofres, " + ench + " con encantamientos");
            }
            log.println("Calibracion de muebles: " + (com.ciudadela.gen.Calib.done ? "hecha" : "NO"));
            log.println("Tiempo total: " + (System.currentTimeMillis() - t0) / 1000 + " s");
            log.println("OK");
        } catch (Throwable t) {
            CiudadelaMod.LOGGER.error("[CI] fallo en la prueba", t);
        } finally {
            server.halt(false);
        }
    }

    // ------------------------------------------------------------ prueba de "se puede subir andando"

    private static boolean passable(ServerLevel lvl, BlockPos.MutableBlockPos m, int x, int y, int z) {
        BlockState st = lvl.getBlockState(m.set(x, y, z));
        var shape = st.getCollisionShape(lvl, m);
        return shape.isEmpty() || shape.max(net.minecraft.core.Direction.Axis.Y) <= 0.2;
    }

    private static boolean solid(ServerLevel lvl, BlockPos.MutableBlockPos m, int x, int y, int z) {
        BlockState st = lvl.getBlockState(m.set(x, y, z));
        var shape = st.getCollisionShape(lvl, m);
        return !shape.isEmpty() && shape.max(net.minecraft.core.Direction.Axis.Y) >= 0.45;
    }

    private static boolean stand(ServerLevel lvl, BlockPos.MutableBlockPos m, int x, int y, int z) {
        return passable(lvl, m, x, y, z) && passable(lvl, m, x, y + 1, z) && solid(lvl, m, x, y - 1, z);
    }

    /** Busqueda en anchura: ¿se puede llegar andando (saltando 1 bloque) desde abajo hasta targetY? */
    private static String reach(ServerLevel lvl, int cx, int cz, int r, int y0, int targetY) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int H = targetY - y0 + 6;
        int W = 2 * r + 1;
        boolean[] seen = new boolean[W * W * (H + 1)];
        java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();
        for (int x = cx - r; x <= cx + r; x++)
            for (int z = cz - r; z <= cz + r; z++)
                for (int y = y0; y <= y0 + 1; y++) {
                    if ((x - cx) * (x - cx) + (z - cz) * (z - cz) > r * r) continue;
                    if (stand(lvl, m, x, y, z)) {
                        seen[((x - cx + r) * W + (z - cz + r)) * (H + 1) + (y - y0)] = true;
                        q.add(new int[]{x, y, z});
                    }
                }
        if (q.isEmpty()) return "FALLA: no hay donde pisar abajo";
        int best = y0;
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!q.isEmpty()) {
            int[] p = q.poll();
            best = Math.max(best, p[1]);
            if (p[1] >= targetY) return "OK";
            for (int[] d : dirs) {
                int nx = p[0] + d[0], nz = p[2] + d[1];
                if ((nx - cx) * (nx - cx) + (nz - cz) * (nz - cz) > r * r) continue;
                for (int dy = 1; dy >= -3; dy--) {
                    int ny = p[1] + dy;
                    if (ny < y0 || ny > y0 + H) continue;
                    if (dy == 1 && !passable(lvl, m, p[0], p[1] + 2, p[2])) continue;
                    if (!stand(lvl, m, nx, ny, nz)) continue;
                    if (dy < 0) {
                        boolean clear = true;
                        for (int yy = ny + 2; yy <= p[1] + 1 && clear; yy++) if (!passable(lvl, m, nx, yy, nz)) clear = false;
                        if (!clear) continue;
                    }
                    int idx = ((nx - cx + r) * W + (nz - cz + r)) * (H + 1) + (ny - y0);
                    if (seen[idx]) continue;
                    seen[idx] = true;
                    q.add(new int[]{nx, ny, nz});
                }
            }
        }
        return "FALLA: solo se llega hasta y=" + best + " (faltan " + (targetY - best) + ")";
    }

    /** ¿Se llega andando desde abajo a pisar en (tx, ty, tz) o a 2 bloques de ahi a esa misma altura? */
    private static String reachSpot(ServerLevel lvl, int cx, int cz, int r, int y0, int tx, int ty, int tz) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int H = ty - y0 + 6;
        int W = 2 * r + 1;
        boolean[] seen = new boolean[W * W * (H + 1)];
        java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();
        for (int x = cx - r; x <= cx + r; x++)
            for (int z = cz - r; z <= cz + r; z++)
                for (int y = y0; y <= y0 + 1; y++) {
                    if ((x - cx) * (x - cx) + (z - cz) * (z - cz) > r * r) continue;
                    if (stand(lvl, m, x, y, z)) {
                        seen[((x - cx + r) * W + (z - cz + r)) * (H + 1) + (y - y0)] = true;
                        q.add(new int[]{x, y, z});
                    }
                }
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!q.isEmpty()) {
            int[] p = q.poll();
            if (p[1] == ty && Math.abs(p[0] - tx) <= 2 && Math.abs(p[2] - tz) <= 2) return "OK";
            for (int[] d : dirs) {
                int nx = p[0] + d[0], nz = p[2] + d[1];
                if ((nx - cx) * (nx - cx) + (nz - cz) * (nz - cz) > r * r) continue;
                for (int dy = 1; dy >= -3; dy--) {
                    int ny = p[1] + dy;
                    if (ny < y0 || ny > y0 + H) continue;
                    if (dy == 1 && !passable(lvl, m, p[0], p[1] + 2, p[2])) continue;
                    if (!stand(lvl, m, nx, ny, nz)) continue;
                    if (dy < 0) {
                        boolean clear = true;
                        for (int yy = ny + 2; yy <= p[1] + 1 && clear; yy++) if (!passable(lvl, m, nx, yy, nz)) clear = false;
                        if (!clear) continue;
                    }
                    int idx = ((nx - cx + r) * W + (nz - cz + r)) * (H + 1) + (ny - y0);
                    if (seen[idx]) continue;
                    seen[idx] = true;
                    q.add(new int[]{nx, ny, nz});
                }
            }
        }
        return "no se llega";
    }

    /** Lista todos los bloques de los mods de decoracion con sus propiedades. */
    private static void dumpModBlocks(File f) throws Exception {
        java.util.Set<String> skip = java.util.Set.of("minecraft", "forge", "ciudadela");
        Map<String, java.util.List<String>> byMod = new TreeMap<>();
        for (var e : net.minecraftforge.registries.ForgeRegistries.BLOCKS.getEntries()) {
            var id = e.getKey().location();
            if (skip.contains(id.getNamespace())) continue;
            StringBuilder sb = new StringBuilder(id.getPath());
            var props = e.getValue().getStateDefinition().getProperties();
            if (!props.isEmpty()) {
                sb.append(" {");
                boolean first = true;
                for (var p : props) {
                    if (!first) sb.append("; ");
                    first = false;
                    sb.append(p.getName()).append("=");
                    StringBuilder vals = new StringBuilder();
                    for (Object v : p.getPossibleValues()) {
                        if (vals.length() > 0) vals.append("|");
                        vals.append(v.toString().toLowerCase());
                    }
                    sb.append(vals);
                }
                sb.append("}");
            }
            byMod.computeIfAbsent(id.getNamespace(), k -> new java.util.ArrayList<>()).add(sb.toString());
        }
        try (PrintWriter w = new PrintWriter(f)) {
            for (var en : byMod.entrySet()) {
                java.util.Collections.sort(en.getValue());
                w.println("### " + en.getKey() + " (" + en.getValue().size() + " bloques)");
                for (String l : en.getValue()) w.println(l);
            }
        }
    }

    private static int color(ServerLevel lvl, BlockPos p, BlockState st) {
        int c = st.getMapColor(lvl, p).col;
        if (c == 0 && !st.isAir()) c = 0x404040;
        return c;
    }

    private static int shade(int rgb, double k) {
        int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
        r = (int) Math.max(0, Math.min(255, r * k));
        g = (int) Math.max(0, Math.min(255, g * k));
        b = (int) Math.max(0, Math.min(255, b * k));
        return (r << 16) | (g << 8) | b;
    }

    private static void topView(ServerLevel lvl, int cx, int cz, int R, File f) throws Exception {
        BufferedImage img = new BufferedImage(2 * R + 1, 2 * R + 1, BufferedImage.TYPE_INT_RGB);
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int top = lvl.getMaxBuildHeight() - 1;
        for (int dx = -R; dx <= R; dx++)
            for (int dz = -R; dz <= R; dz++) {
                int x = cx + dx, z = cz + dz;
                int rgb = 0x87A8D0;
                for (int y = top; y >= lvl.getMinBuildHeight(); y--) {
                    BlockState st = lvl.getBlockState(m.set(x, y, z));
                    if (st.isAir()) continue;
                    int col = color(lvl, m, st);
                    double k = 0.45 + 0.75 * Math.max(0, Math.min(1, (y - 60) / 600.0));
                    rgb = shade(col, k);
                    break;
                }
                img.setRGB(dx + R, dz + R, rgb);
            }
        ImageIO.write(img, "png", f);
    }

    private static void sectionX(ServerLevel lvl, int cx, int cz, int R, File f, int offZ) throws Exception {
        int ymin = 200, ymax = lvl.getMaxBuildHeight() - 1;
        BufferedImage img = new BufferedImage(2 * R + 1, ymax - ymin + 1, BufferedImage.TYPE_INT_RGB);
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dx = -R; dx <= R; dx++)
            for (int y = ymin; y <= ymax; y++) {
                BlockState st = lvl.getBlockState(m.set(cx + dx, y, cz + offZ));
                img.setRGB(dx + R, ymax - y, st.isAir() ? 0xDDE6F0 : color(lvl, m, st));
            }
        ImageIO.write(img, "png", f);
    }

    private static void sectionZ(ServerLevel lvl, int cx, int cz, int R, File f, int offX) throws Exception {
        int ymin = 200, ymax = lvl.getMaxBuildHeight() - 1;
        BufferedImage img = new BufferedImage(2 * R + 1, ymax - ymin + 1, BufferedImage.TYPE_INT_RGB);
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dz = -R; dz <= R; dz++)
            for (int y = ymin; y <= ymax; y++) {
                BlockState st = lvl.getBlockState(m.set(cx + offX, y, cz + dz));
                img.setRGB(dz + R, ymax - y, st.isAir() ? 0xDDE6F0 : color(lvl, m, st));
            }
        ImageIO.write(img, "png", f);
    }

    /** ¿Algo con colision delante de una puerta? (nx, nz) hacia donde se mira, depth casillas. */
    private static String blocked(ServerLevel lvl, BlockPos.MutableBlockPos m, int x, int y, int z, int nx, int nz, int depth) {
        for (int k = 1; k <= depth; k++)
            for (int dy = 0; dy <= 1; dy++) {
                int px = x + nx * k, pz = z + nz * k, py = y + dy;
                BlockState st = lvl.getBlockState(m.set(px, py, pz));
                var shape = st.getCollisionShape(lvl, m);
                if (shape.isEmpty()) continue;
                double top = shape.max(net.minecraft.core.Direction.Axis.Y);
                if (dy == 0 && top <= 0.55) continue; // alfombras, losas y escalones se pisan
                return st.getBlock().getDescriptionId() + " a " + k + " de la puerta (altura +" + dy + ")";
            }
        return null;
    }

    /** Corte vertical (plano x = cte) ampliado. */
    private static void sectionZoomX(ServerLevel lvl, int x, int z0, int z1, int y0, int y1, int S, File f) throws Exception {
        BufferedImage img = new BufferedImage((z1 - z0 + 1) * S, (y1 - y0 + 1) * S, BufferedImage.TYPE_INT_RGB);
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int z = z0; z <= z1; z++)
            for (int y = y0; y <= y1; y++) {
                BlockState st = lvl.getBlockState(m.set(x, y, z));
                int rgb = st.isAir() ? 0xDDE6F0 : color(lvl, m, st);
                if (st.is(net.minecraft.world.level.block.Blocks.CHAIN)) rgb = 0xFF00FF;
                for (int a = 0; a < S; a++) for (int b = 0; b < S; b++) img.setRGB((z - z0) * S + a, (y1 - y) * S + b, rgb);
            }
        ImageIO.write(img, "png", f);
    }

    private static void plan(ServerLevel lvl, int cx, int cz, int R, int y, File f) throws Exception {
        plan(lvl, cx, cz, R, y, f, 3);
    }

    /** Planta (corte horizontal) a escala S px por bloque. */
    private static void plan(ServerLevel lvl, int cx, int cz, int R, int y, File f, int S) throws Exception {
        BufferedImage img = new BufferedImage((2 * R + 1) * S, (2 * R + 1) * S, BufferedImage.TYPE_INT_RGB);
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dx = -R; dx <= R; dx++)
            for (int dz = -R; dz <= R; dz++) {
                BlockState st = lvl.getBlockState(m.set(cx + dx, y, cz + dz));
                int rgb;
                if (st.isAir()) {
                    BlockState below = lvl.getBlockState(m.set(cx + dx, y - 1, cz + dz));
                    rgb = below.isAir() ? 0x101018 : shade(color(lvl, m, below), 0.55);
                } else rgb = color(lvl, m, st);
                for (int a = 0; a < S; a++) for (int b = 0; b < S; b++) img.setRGB((dx + R) * S + a, (dz + R) * S + b, rgb);
            }
        ImageIO.write(img, "png", f);
    }
}
