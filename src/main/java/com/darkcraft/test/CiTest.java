package com.darkcraft.test;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.CastlePlan;
import com.darkcraft.world.CastleSites;
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
                o.print(com.darkcraft.gen.ModDeco.report());
            }
            int[] s = CastleSites.nearest(seed, 0, 0, 3);
            if (s == null) {
                log.println("No se encontro ningun Reino de Valdrenn");
                return;
            }
            log.println("Reino de Valdrenn en " + s[0] + ", " + s[1]);
            // /locate structure darkcraft:valdrenn tiene que dar el mismo sitio
            try {
                var reg = lvl.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
                var h = reg.getHolderOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE,
                        new net.minecraft.resources.ResourceLocation("darkcraft", "valdrenn")));
                var found = lvl.getChunkSource().getGenerator().findNearestMapStructure(lvl, net.minecraft.core.HolderSet.direct(h),
                        new net.minecraft.core.BlockPos(0, 64, 0), 100, false);
                if (found == null) log.println("LOCATE darkcraft:valdrenn: NO ENCONTRADO");
                else log.println("LOCATE darkcraft:valdrenn: " + found.getFirst().getX() + ", " + found.getFirst().getZ()
                        + (Math.abs(found.getFirst().getX() - s[0]) < 16 && Math.abs(found.getFirst().getZ() - s[1]) < 16 ? " OK" : " DISTINTO"));
            } catch (Exception e) {
                log.println("LOCATE darkcraft:valdrenn: ERROR " + e);
            }
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
                if ((cx - c0x) % 5 == 0) DarkCraftMod.LOGGER.info("[CI] columnas de chunks generadas: {}/{}", cx - c0x + 1, c1x - c0x + 1);
            }
            long t1 = System.currentTimeMillis();
            log.println("Chunks generados: " + n + " en " + (t1 - t0) / 1000 + " s (" + (t1 - t0) / Math.max(1, n) + " ms/chunk)");
            log.println("Entidades de bloque: " + be);
            guardiaDeValdrenn(server, lvl, log, s);
            perrosYEspectros(server, lvl, log, s);
            elitesYEmboscadas(server, lvl, log, s);
            guardianesDeValdrenn(server, lvl, log, s);

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
            CastlePlan plan = com.darkcraft.world.PlanCache.get(seed, s[0], s[1], (x, z) -> 64);
            try (PrintWriter acc = new PrintWriter(new File(out, "accesos.txt"))) {
                int ok = 0, bad = 0;
                for (com.darkcraft.gen.Element e : plan.elements()) {
                    String res = null;
                    if (e instanceof com.darkcraft.gen.RoundTower rt) {
                        int target = rt.top == com.darkcraft.gen.RoundTower.Top.PLATFORM ? rt.topY + 1 : rt.lastFloor();
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
                    } else if (e instanceof com.darkcraft.gen.Building b && b.turretInfo() != null) {
                        int[] t = b.turretInfo();
                        res = "Escalera de edificio " + b.fp + ": " + reach(lvl, t[0], t[1], 3, t[2], t[3]);
                    } else if (e instanceof com.darkcraft.gen.TreeTower tt) {
                        // la escalera segura de la torre: desde abajo hasta el suelo de la recepcion del arbol
                        res = reach(lvl, tt.centerX(), tt.centerZ(), 16, tt.base(), tt.hallFloor);
                        res = "TORRE-ARBOL -> RECEPCION DEL ARBOL (y=" + tt.hallFloor + ", " + (tt.hallFloor - tt.base()) + " de subida): " + res;
                        plan(lvl, tt.centerX(), tt.centerZ(), 44, tt.hallFloor + 1, new File(out, "12_sala_del_arbol.png"));
                        plan(lvl, tt.centerX(), tt.centerZ(), 44, tt.hallFloor + com.darkcraft.gen.TreeTower.BALCON + 1, new File(out, "13_sala_del_arbol_balcon.png"));
                        plan(lvl, tt.centerX(), tt.centerZ(), 16, tt.base() + 40, new File(out, "24_torre_arbol_escalera.png"));
                        plan(lvl, tt.centerX(), tt.centerZ(), 16, tt.hallFloor, new File(out, "25_recepcion_del_arbol.png"));
                        // Ser Gavran: su rama (encima de la puerta) y donde cae al bajar
                        var sa = com.darkcraft.gavran.SalaArbol.de(plan);
                        if (sa != null) {
                            var rama = sa.rama(lvl);
                            acc.println("GAVRAN: rama en " + String.format("%.1f,%.1f,%.1f", rama.x, rama.y, rama.z) + " (suelo de la sala " + sa.F()
                                    + ", " + Math.round(rama.y - sa.F()) + " por encima) bloque bajo los pies: "
                                    + lvl.getBlockState(net.minecraft.core.BlockPos.containing(rama.x, rama.y - 1, rama.z))
                                    + " | aterriza en " + sa.aterrizaje() + " suelo " + sa.suelo(lvl, sa.aterrizaje().x, sa.aterrizaje().z));
                        }
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
                for (com.darkcraft.gen.Element e : plan.elements()) {
                    if (!(e instanceof com.darkcraft.gen.Building b)) continue;
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
                int tag = cbe == null ? 0 : cbe.getPersistentData().getInt(com.darkcraft.gen.Canvas.STORY_TAG);
                String sign = "sin cartel";
                for (int dx = -1; dx <= 1; dx++)
                    for (int dz = -1; dz <= 1; dz++) {
                        var sb = lvl.getBlockEntity(cp.offset(dx, 0, dz));
                        if (sb instanceof net.minecraft.world.level.block.entity.SignBlockEntity sg)
                            sign = "cartel [" + sg.getFrontText().getMessage(0, false).getString() + " | " + sg.getFrontText().getMessage(1, false).getString()
                                    + " | " + sg.getFrontText().getMessage(2, false).getString() + "] encerado=" + sg.isWaxed()
                                    + " brilla=" + sg.getFrontText().hasGlowingText()
                                    + " protegido=" + (sg.getPersistentData().getInt(com.darkcraft.gen.Canvas.STORY_TAG) == c);
                    }
                String what = st.getBlock() == net.minecraft.world.level.block.Blocks.CHEST ? "cofre marca=" + tag : st.toString();
                log.println("Capitulo " + c + " en " + sp[0] + "," + sp[1] + "," + sp[2] + ": " + what + ", " + sign
                        + ((c == 3 || c == 5) ? " (aparece al morir el jefe)" : ""));
            }
            for (int c = 1; c <= 5; c++) {
                var book = com.darkcraft.story.Story.book(c);
                log.println("Libro " + c + ": " + book.getTag().getList("pages", 8).size() + " paginas");
            }
            var md = com.darkcraft.gen.MapShapes.build(plan);
            log.println("Mapa: " + md.shapes().length + " numeros de dibujo, " + md.labels().length + " nombres");
            // ---- camara de la cinematica del capitulo 1: que no atraviese nada
            try (PrintWriter pw = new PrintWriter(new File(out, "camara.txt"))) {
                BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
                var shots = com.darkcraft.cine.Cinematics.preview(plan);
                int hits = 0, pts = 0;
                for (int i = 0; i < shots.size(); i++) {
                    var sh = shots.get(i);
                    int sHits = 0;
                    // el principio del primer plano sale de los ojos del jugador, al lado del cofre: no cuenta
                    for (float tt = sh.t0 + (i == 0 ? 1.5f : 0f); tt <= sh.t1; tt += 0.25f) {
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
                var table = server.getLootData().getLootTable(new net.minecraft.resources.ResourceLocation("darkcraft", "chests/" + name));
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
            // ---- el mundo de nubes (parte 3a): generador, donde se aparece y el camino de nubes hasta la fortaleza
            try {
                var gen = lvl.getChunkSource().getGenerator();
                log.println("Mundo: generador " + gen.getClass().getSimpleName() + ", dimensiones " + server.levelKeys());
                BlockPos ap = lvl.getSharedSpawnPos();
                lvl.getChunk(ap.getX() >> 4, ap.getZ() >> 4);
                log.println("Se aparece en " + ap.toShortString() + " (radio " + lvl.getGameRules().getInt(net.minecraft.world.level.GameRules.RULE_SPAWN_RADIUS)
                        + "): bajo los pies " + lvl.getBlockState(ap.below()).getBlock().getDescriptionId() + ", encima "
                        + lvl.getBlockState(ap).getBlock().getDescriptionId() + " y " + lvl.getBlockState(ap.above()).getBlock().getDescriptionId());
                var ruta = com.darkcraft.world.CaminoNubes.ruta(seed, s[0], s[1]);
                if (ruta == null) log.println("Camino de nubes: NO HAY");
                else {
                    StringBuilder b = new StringBuilder("Camino de nubes: " + Math.round(ruta.largo()) + " bloques, " + ruta.px().length + " puntos, "
                            + ruta.faroles().length + " faroles; muestras:");
                    int bien = 0, n2 = 0;
                    for (double d : new double[]{30, 200, 600, 1200, 1800, ruta.largo() - 60, ruta.largo() - 2}) {
                        double[] q = ruta.punto(d);
                        int qx = (int) Math.floor(q[0]), qz = (int) Math.floor(q[1]);
                        lvl.getChunk(qx >> 4, qz >> 4);
                        var st = lvl.getBlockState(new BlockPos(qx, com.darkcraft.world.NubesGenerador.CAMINO, qz));
                        boolean ok = st.is(DarkCraftMod.NUBE_FIRME.get()) && lvl.getBlockState(new BlockPos(qx, com.darkcraft.world.NubesGenerador.SUELO, qz)).isAir()
                                && lvl.getBlockState(new BlockPos(qx, com.darkcraft.world.NubesGenerador.SUELO + 1, qz)).isAir();
                        n2++;
                        if (ok) bien++;
                        b.append(" ").append(Math.round(d)).append("=").append(qx).append(",").append(qz).append(ok ? " OK" : " MAL(" + st.getBlock().getDescriptionId() + ")");
                    }
                    log.println(b + " -> " + bien + " de " + n2 + " bien");
                    var fs = com.darkcraft.fortaleza.Fortalezas.delReino(seed, s[0], s[1]);
                    var fp = com.darkcraft.fortaleza.Fortalezas.plan(seed, fs);
                    montanaDeLaFortaleza(log, lvl, fp);
                    paradasDelCamino(log, lvl, s);
                    // parte 5e: el capitulo 6 con la voz de la Sombra (su audio y sus tiempos los monta procesar.py)
                    float[][] l6 = com.darkcraft.cine.Timeline.lines(6);
                    log.println(String.format(java.util.Locale.ROOT,
                            "Capitulo 6 (la Sombra): audio %s, %d frases, %.1f s de voz; se abre el portal a los %.1f s, las nubes a los %.1f, el rayo a los %.1f; dura %.1f s; %s",
                            com.darkcraft.cine.Timeline.hasAudio(6), l6.length, com.darkcraft.cine.Timeline.narration(6), com.darkcraft.cine.Timeline.abrePortal6(),
                            com.darkcraft.cine.Timeline.nubes6(), com.darkcraft.cine.Timeline.rayo6(), com.darkcraft.cine.Timeline.total(6),
                            com.darkcraft.fortaleza.CieloRojo.resumen(server)));
                }
                int x = s[0] + 900, z = s[1] + 900;
                lvl.getChunk(x >> 4, z >> 4);
                log.println("Nube en " + x + "," + z + ": " + com.darkcraft.world.NubesGenerador.fondo(x, z) + ".." + com.darkcraft.world.NubesGenerador.cima(x, z)
                        + " -> " + lvl.getBlockState(new BlockPos(x, com.darkcraft.world.NubesGenerador.cima(x, z), z)).getBlock().getDescriptionId()
                        + " / encima " + lvl.getBlockState(new BlockPos(x, com.darkcraft.world.NubesGenerador.cima(x, z) + 1, z)).getBlock().getDescriptionId()
                        + " / debajo " + lvl.getBlockState(new BlockPos(x, com.darkcraft.world.NubesGenerador.fondo(x, z) - 1, z)).getBlock().getDescriptionId());
            } catch (Throwable t) {
                log.println("Mundo de nubes: ERROR " + t);
                DarkCraftMod.LOGGER.error("[CI] mundo de nubes", t);
            }
            // ---- Brumavela, la isla del pueblo (parte 3b): se genera entera, se recorre a pie y se prueba lo que cambia
            try {
                var pueblo = com.darkcraft.pueblo.gen.PuebloPlan.get();
                long tp = System.currentTimeMillis();
                for (long k : pueblo.chunks()) lvl.getChunk((int) (k >> 32), (int) k);
                int[] ini = pueblo.sitio("inicio");
                BlockPos ap = lvl.getSharedSpawnPos();
                log.println("Brumavela: " + pueblo.chunks().size() + " chunks en " + (System.currentTimeMillis() - tp) + " ms; se aparece en " + ap.toShortString()
                        + " (el plano dice " + ini[0] + ", " + ini[1] + ", " + ini[2] + "), bajo los pies " + lvl.getBlockState(ap.below()).getBlock().getDescriptionId()
                        + ", bioma " + lvl.getBiome(ap).unwrapKey().map(k -> k.location().toString()).orElse("?") + ", modo por defecto " + server.getDefaultGameType()
                        + ", nube junto al espigon " + com.darkcraft.world.NubesGenerador.cima(-8, 119) + ".." + com.darkcraft.world.NubesGenerador.cima(6, 140));
                BlockPos.MutableBlockPos mb = new BlockPos.MutableBlockPos();
                com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> lvl.getBlockState(mb.set(x, y, z));
                for (boolean abierta : new boolean[]{false, true})
                    log.println("Brumavela a pie (cripta " + (abierta ? "abierta" : "cerrada") + "): "
                            + com.darkcraft.pueblo.gen.Recorrido.resumen(com.darkcraft.pueblo.gen.Recorrido.comprobar(mundo, abierta)));
                // a menos de 17 bloques de donde se aparece no puede haber nada que se use con clic (la proteccion de un servidor dedicado)
                StringBuilder cerca = new StringBuilder();
                int usables = 0;
                for (int x = ap.getX() - 16; x <= ap.getX() + 16; x++)
                    for (int z = ap.getZ() - 16; z <= ap.getZ() + 16; z++)
                        for (int y = ap.getY() - 8; y <= ap.getY() + 30; y++) {
                            var b = lvl.getBlockState(mb.set(x, y, z)).getBlock();
                            if (b instanceof net.minecraft.world.level.block.DoorBlock || b instanceof net.minecraft.world.level.block.TrapDoorBlock
                                    || b instanceof net.minecraft.world.level.block.ChestBlock || b instanceof net.minecraft.world.level.block.LecternBlock
                                    || b instanceof net.minecraft.world.level.block.BellBlock || b instanceof net.minecraft.world.level.block.FenceGateBlock) {
                                usables++;
                                if (usables <= 6) cerca.append(' ').append(b.getDescriptionId()).append('@').append(x).append(',').append(y).append(',').append(z);
                            }
                        }
                log.println("Brumavela: cosas de clic a menos de 17 bloques de donde se aparece: " + usables + " (tiene que ser 0)" + cerca);
                // los bloques de la historia
                StringBuilder bl = new StringBuilder();
                for (String sitio : new String[]{"vela", "campana_pescador", "campana_tejedora", "campana_farero", "corazon", "nota_capitania", "nota_taberna", "nota_campanas",
                        "nota_marinero", "nota_capitan", "losa"}) {
                    int[] q = pueblo.sitio(sitio);
                    BlockPos b = new BlockPos(q[0], q[1], q[2]);
                    bl.append(' ').append(sitio).append('=').append(lvl.getBlockState(b).getBlock().getDescriptionId().replace("block.minecraft.", ""));
                    if (lvl.getBlockEntity(b) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity le)
                        bl.append("(libro ").append(com.darkcraft.fortaleza.Perdidos.de(le.getBook())).append(')');
                }
                log.println("Brumavela, bloques:" + bl);
                // la cripta se abre, el barco se repara y todo vuelve a su sitio
                var d = com.darkcraft.pueblo.Pueblo.Datos.get(server);
                int[] losa = pueblo.sitio("losa");
                com.darkcraft.pueblo.Pueblo.abrirCripta(lvl, null);
                String abierta = lvl.getBlockState(new BlockPos(losa[0], losa[1], losa[2])).getBlock().getDescriptionId();
                // (el barco roto, bloque a bloque, para comprobar que reparar y reiniciar lo deja como estaba)
                var barco = pueblo.barco;
                java.util.Map<Long, net.minecraft.world.level.block.Block> roto = new java.util.HashMap<>();
                for (int x = barco.x0 - 2; x <= barco.x1 + 2; x++)
                    for (int z = barco.z0 - 2; z <= barco.z1 + 2; z++)
                        for (int y = barco.y0 - 2; y <= barco.y1 + 2; y++) roto.put(BlockPos.asLong(x, y, z), lvl.getBlockState(mb.set(x, y, z)).getBlock());
                // (parte 3d-2) el plano del barco sin mundo tiene que ser el barco que acaba de generar el juego, estado a estado:
                // tambien como se unen sus vallas y sus cristales, que el plano une a mano
                {
                    StringBuilder dp = new StringBuilder();
                    int mal = 0;
                    for (var c : com.darkcraft.pueblo.Garza.plano(false)) {
                        var hay = lvl.getBlockState(mb.set(c.x(), c.y(), c.z()));
                        if (hay == c.estado() || (c.estado().isAir() && hay.isAir())) continue;
                        if (mal++ < 6) dp.append(" [").append(c.x()).append(',').append(c.y()).append(',').append(c.z()).append(" plano ").append(c.estado()).append(" generado ").append(hay).append(']');
                    }
                    log.println("Brumavela: el plano del barco roto contra el barco generado: " + mal + " celdas distintas de " + com.darkcraft.pueblo.Garza.plano(false).size()
                            + " (tiene que ser 0)" + dp);
                }
                com.darkcraft.pueblo.Pueblo.levantar(lvl, true);
                int lona = 0, yunques = 0;
                for (long k : roto.keySet()) {
                    var b = lvl.getBlockState(mb.set(k)).getBlock();
                    if (b == net.minecraft.world.level.block.Blocks.LIGHT_GRAY_WOOL || b == net.minecraft.world.level.block.Blocks.WHITE_WOOL
                            || b == net.minecraft.world.level.block.Blocks.GRAY_WOOL) lona++;
                    if (b instanceof net.minecraft.world.level.block.FallingBlock) yunques++;
                }
                int[] an = com.darkcraft.pueblo.gen.Barco.ancla();
                String eslabon = lvl.getBlockState(new BlockPos(an[0], an[1], an[2])).getBlock().getDescriptionId() + " sobre "
                        + lvl.getBlockState(new BlockPos(an[0], an[1] - 1, an[2])).getBlock().getDescriptionId();
                String palo = lvl.getBlockState(new BlockPos(com.darkcraft.pueblo.gen.Barco.BX, com.darkcraft.pueblo.gen.Barco.CUBIERTA + 12, com.darkcraft.pueblo.gen.Barco.PALO))
                        .getBlock().getDescriptionId();
                String rueda = lvl.getBlockState(new BlockPos(com.darkcraft.pueblo.gen.Barco.BX, com.darkcraft.pueblo.gen.Barco.ALCAZAR_Y + 2, com.darkcraft.pueblo.gen.Barco.TIMON_Z))
                        .getBlock().getDescriptionId();
                String baranda = lvl.getBlockState(new BlockPos(com.darkcraft.pueblo.gen.Barco.BX - 1, com.darkcraft.pueblo.gen.Barco.ALCAZAR_Y + 1, com.darkcraft.pueblo.gen.Barco.POPA)).toString();
                d.reparado = true;
                com.darkcraft.pueblo.Pueblo.reiniciar(lvl);
                log.println("Brumavela: la losa tras las campanas " + abierta + "; el barco reparado: palo " + palo + ", rueda " + rueda + ", barandilla " + baranda
                        + "; tras reiniciar: losa " + lvl.getBlockState(new BlockPos(losa[0], losa[1], losa[2])).getBlock().getDescriptionId() + ", palo "
                        + lvl.getBlockState(new BlockPos(com.darkcraft.pueblo.gen.Barco.BX, com.darkcraft.pueblo.gen.Barco.CUBIERTA + 12, com.darkcraft.pueblo.gen.Barco.PALO))
                        .getBlock().getDescriptionId() + " | " + com.darkcraft.pueblo.Pueblo.estado(d) + " | cosas " + com.darkcraft.pueblo.Pueblo.cosas(lvl).size());
                int distintos = 0;
                StringBuilder dif = new StringBuilder();
                for (var e : roto.entrySet()) {
                    var b = lvl.getBlockState(mb.set(e.getKey())).getBlock();
                    if (b == e.getValue()) continue;
                    distintos++;
                    if (distintos <= 6) dif.append(' ').append(BlockPos.of(e.getKey()).toShortString()).append(": ").append(e.getValue().getDescriptionId()).append(" -> ")
                            .append(b.getDescriptionId());
                }
                log.println("Brumavela: el barco reparado lleva " + lona + " bloques de lona, " + yunques + " bloques que caen (tiene que ser 0) y el eslabon del ancla "
                        + eslabon + "; tras reiniciar quedan " + distintos + " bloques distintos del barco roto (tiene que ser 0)" + dif);
                // el botin de las casas
                for (String name : new String[]{"pueblo", "pueblo_bueno"}) {
                    var table = server.getLootData().getLootTable(new net.minecraft.resources.ResourceLocation(DarkCraftMod.MODID, "chests/" + name));
                    var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(lvl)
                            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, net.minecraft.world.phys.Vec3.atCenterOf(ap))
                            .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
                    int cosas = 0;
                    java.util.Set<String> tipos = new java.util.TreeSet<>();
                    for (int i = 0; i < 100; i++)
                        for (var st : table.getRandomItems(params)) {
                            cosas++;
                            tipos.add(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(st.getItem()).getPath());
                        }
                    log.println("Botin " + name + ": " + cosas + " cosas en 100 cofres, " + tipos.size() + " distintas");
                }
            } catch (Throwable t) {
                log.println("Brumavela: ERROR " + t);
                DarkCraftMod.LOGGER.error("[CI] Brumavela", t);
            }
            // ---- los enemigos de Brumavela (parte 3c): sus puestos, que salen y hacen lo suyo sin romper nada, lo que guardan
            try {
                String sitio = com.darkcraft.pueblo.PuebloEnemigos.SITIO;
                BlockPos.MutableBlockPos mb = new BlockPos.MutableBlockPos();
                com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> lvl.getBlockState(mb.set(x, y, z));
                log.println("Brumavela, puestos de los enemigos: " + com.darkcraft.pueblo.PuebloEnemigos.comprobar(mundo) + " | dificultad " + lvl.getDifficulty());
                com.darkcraft.pueblo.Pueblo.reiniciar(lvl);
                var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(lvl, sitio);
                int enSuelo = 0, chocan = 0, fallos = 0;
                java.util.Map<String, Integer> tipos = new java.util.TreeMap<>();
                StringBuilder mal = new StringBuilder();
                for (var e : fuera) {
                    tipos.merge(e.tipo() + (e.postura() == 1 ? " (reza)" : e.postura() == 2 ? " (tumbado)" : ""), 1, Integer::sum);
                    boolean suelo = !lvl.getBlockState(BlockPos.containing(e.getX(), e.getY() - 0.2, e.getZ())).getCollisionShape(lvl, BlockPos.ZERO).isEmpty();
                    if (suelo) enSuelo++;
                    else if (mal.length() < 300) mal.append(' ').append(e.tipo()).append(" en el aire en ").append(e.blockPosition().toShortString()).append(';');
                    if (!lvl.noCollision(e)) {
                        chocan++;
                        if (mal.length() < 300) mal.append(' ').append(e.tipo()).append(" metido en un bloque en ").append(e.blockPosition().toShortString()).append(';');
                    }
                }
                // cada uno hace todas sus acciones (sin presa): que ninguna rompa nada
                com.darkcraft.enemigos.Capitan capitan = null;
                int arpones = 0;
                for (var e : fuera) {
                    if (e instanceof com.darkcraft.enemigos.Capitan c) capitan = c;
                    try {
                        e.postura(0);
                        int acciones = e instanceof com.darkcraft.enemigos.Capitan ? com.darkcraft.enemigos.CapitanAnims.TICKS.length
                                : e instanceof com.darkcraft.enemigos.Ahogado ? com.darkcraft.enemigos.AhogadoAnims.TICKS.length
                                : com.darkcraft.enemigos.HuecoAnims.TICKS.length;
                        for (int a = 1; a < acciones; a++) {
                            String nombre = e instanceof com.darkcraft.enemigos.Capitan ? com.darkcraft.enemigos.CapitanAnims.NOMBRES[a]
                                    : e instanceof com.darkcraft.enemigos.Ahogado ? com.darkcraft.enemigos.AhogadoAnims.NOMBRES[a] : com.darkcraft.enemigos.HuecoAnims.NOMBRES[a];
                            if (nombre.equals("morir")) continue;
                            e.empezar(a);
                            for (int k = 0; k < 80 && e.accion() == a; k++) e.tick();
                            if (e.accion() == a) {
                                fallos++;
                                if (mal.length() < 500) mal.append(' ').append(e.tipo()).append(": la accion ").append(nombre).append(" no acaba;");
                            }
                        }
                        for (int k = 0; k < 20; k++) e.tick();
                    } catch (Throwable t) {
                        fallos++;
                        if (mal.length() < 700) mal.append(' ').append(e.tipo()).append(": ").append(t).append(';');
                        DarkCraftMod.LOGGER.error("[CI] enemigo " + e.tipo(), t);
                    }
                }
                arpones = lvl.getEntitiesOfClass(com.darkcraft.enemigos.Arpon.class, new net.minecraft.world.phys.AABB(-160, 200, -160, 160, 330, 170)).size();
                log.println("Brumavela, enemigos fuera: " + fuera.size() + " de " + com.darkcraft.pueblo.PuebloEnemigos.puestos().size() + " " + tipos + "; en el suelo " + enSuelo
                        + ", metidos en un bloque " + chocan + " (tiene que ser 0), acciones que fallan " + fallos + " (tiene que ser 0), arpones lanzados " + arpones + mal);
                // lo que guardan: el timon (los de la taberna) y la puerta del faro (el Capitan)
                boolean timon0 = com.darkcraft.pueblo.PuebloEnemigos.timonGuardado(lvl), cap0 = com.darkcraft.pueblo.PuebloEnemigos.capitanEnPie(lvl);
                com.darkcraft.pueblo.PuebloEnemigos.reja(lvl);
                boolean reja0 = com.darkcraft.pueblo.PuebloEnemigos.rejaPuesta(lvl);
                int tripulacion = -1;
                String caida = "sin capitan";
                if (capitan != null) {
                    capitan.empezar(com.darkcraft.enemigos.CapitanAnims.LLAMAR);
                    for (int k = 0; k < 40; k++) capitan.tick();
                    tripulacion = capitan.tripulacion();
                    capitan.hurt(lvl.damageSources().generic(), 99999f);
                    for (int k = 0; k < 100 && !capitan.isRemoved(); k++) capitan.tick();
                    caida = "vivo=" + capitan.isAlive() + " quitado=" + capitan.isRemoved() + " tripulacion " + capitan.tripulacion();
                }
                com.darkcraft.pueblo.PuebloEnemigos.reja(lvl);
                var d3 = com.darkcraft.pueblo.Pueblo.Datos.get(server);
                boolean cap1 = com.darkcraft.pueblo.PuebloEnemigos.capitanEnPie(lvl), reja1 = com.darkcraft.pueblo.PuebloEnemigos.rejaPuesta(lvl);
                // los de la taberna caen: el timon queda libre
                int taberna0 = com.darkcraft.enemigos.Puestos.quedan(server, sitio, com.darkcraft.pueblo.PuebloEnemigos.TABERNA);
                for (var e : fuera) {
                    var q = e.puesto() >= 0 ? com.darkcraft.pueblo.PuebloEnemigos.puestos().get(e.puesto()) : null;
                    if (q != null && q.grupo().equals(com.darkcraft.pueblo.PuebloEnemigos.TABERNA)) e.hurt(lvl.damageSources().generic(), 99999f);
                }
                boolean timon1 = com.darkcraft.pueblo.PuebloEnemigos.timonGuardado(lvl);
                int quedan1 = com.darkcraft.enemigos.Puestos.quedan(server, sitio);
                log.println("Brumavela, lo que guardan: timon guardado " + timon0 + " -> " + timon1 + " (taberna " + taberna0 + " -> "
                        + com.darkcraft.enemigos.Puestos.quedan(server, sitio, com.darkcraft.pueblo.PuebloEnemigos.TABERNA) + "); el Capitan en pie " + cap0 + " -> " + cap1
                        + ", la reja " + reja0 + " -> " + reja1 + ", llama a " + tripulacion + " marineros, al caer: " + caida + ", capitan vencido=" + d3.capitan
                        + "; quedan " + quedan1 + " en pie");
                // todo como al principio (la prueba del cliente juega en este mundo)
                com.darkcraft.pueblo.Pueblo.reiniciar(lvl);
                log.println("Brumavela, tras reiniciar: " + com.darkcraft.pueblo.Pueblo.enemigos(server) + ", la reja " + com.darkcraft.pueblo.PuebloEnemigos.rejaPuesta(lvl)
                        + " | " + com.darkcraft.pueblo.Pueblo.estado(d3));
            } catch (Throwable t) {
                log.println("Brumavela, enemigos: ERROR " + t);
                DarkCraftMod.LOGGER.error("[CI] enemigos de Brumavela", t);
            }
            // ---- el barco que va y viene (parte 3d-2): el plano sin mundo, el muelle de Valdrenn, mover el barco y los viajes
            try {
                barcoQueVaYViene(server, lvl, log);
            } catch (Throwable t) {
                log.println("La Garza Gris: ERROR " + t);
                DarkCraftMod.LOGGER.error("[CI] el barco que va y viene", t);
                // que la prueba del cliente se encuentre el barco roto en Brumavela, pase lo que pase
                try {
                    com.darkcraft.pueblo.Pueblo.reiniciar(lvl);
                } catch (Throwable t2) {
                    log.println("La Garza Gris: ERROR al reiniciar " + t2);
                }
            }
            // ---- Morvain, las Tierras Marchitas (parte 7a): la dimension, cada region, el Arco del Umbral y la ciudadela
            try {
                morvain(server, log, out);
            } catch (Throwable t) {
                log.println("Morvain: ERROR " + t);
                DarkCraftMod.LOGGER.error("[CI] Morvain", t);
            }
            // la cinematica de inicio (parte 3d): sobre donde se despierta no hay nada con lo que tropezar al caer, y los dos
            // planos (la caida y el despertar) acaban donde tienen que acabar
            try {
                net.minecraft.core.BlockPos ap = com.darkcraft.world.MundoNubes.aparicion();
                net.minecraft.world.phys.Vec3 pies = new net.minecraft.world.phys.Vec3(ap.getX() + 0.5, ap.getY(), ap.getZ() + 0.5);
                int tapan = 0;
                StringBuilder cuales = new StringBuilder();
                for (int y = ap.getY(); y <= lvl.getMaxBuildHeight() + 2; y++) {
                    net.minecraft.world.level.block.state.BlockState st = lvl.getBlockState(new net.minecraft.core.BlockPos(ap.getX(), y, ap.getZ()));
                    if (st.isAir()) continue;
                    if (tapan++ < 4) cuales.append(" [Y ").append(y).append(' ').append(st.getBlock().getDescriptionId()).append(']');
                }
                double dx = com.darkcraft.pueblo.gen.Barco.BX + 0.5 - pies.x, dz = com.darkcraft.pueblo.gen.Barco.PALO + 0.5 - pies.z;
                float giro = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
                java.util.List<com.darkcraft.cine.Shot> pl = com.darkcraft.cine.Intro.planos(pies, giro);
                double[] arriba = pl.get(0).at(com.darkcraft.cine.Intro.CAIDA), abajo = pl.get(0).at(com.darkcraft.cine.Intro.ATRAPA);
                double[] fin = pl.get(1).at(com.darkcraft.cine.Intro.FIN);
                float[] mira = com.darkcraft.cine.Shot.angles(fin);
                log.println(String.format(java.util.Locale.ROOT, "Inicio: sobre donde se despierta, bloques que tapan %d (tiene que ser 0)%s; cae de Y %.0f a %.1f en %.1f s; "
                                + "acaba en %.2f %.2f %.2f mirando giro %.0f cabeceo %.0f (el barco queda a giro %.0f); las frases de la Sombra duran %s",
                        tapan, cuales, arriba[1], abajo[1], com.darkcraft.cine.Intro.ATRAPA - com.darkcraft.cine.Intro.CAIDA, fin[0], fin[1], fin[2], mira[0], mira[1], giro,
                        java.util.stream.IntStream.rangeClosed(1, 9).mapToObj(fr -> String.format(java.util.Locale.ROOT, "%.1f", com.darkcraft.rey.Voces.sombra(fr)))
                                .collect(java.util.stream.Collectors.joining(" "))));
            } catch (Throwable t) {
                log.println("Inicio: ERROR " + t);
                DarkCraftMod.LOGGER.error("[CI] cinematica de inicio", t);
            }
            log.println("Calibracion de muebles: " + (com.darkcraft.gen.Calib.done ? "hecha" : "NO"));
            log.println("Tiempo total: " + (System.currentTimeMillis() - t0) / 1000 + " s");
            log.println("OK");
        } catch (Throwable t) {
            DarkCraftMod.LOGGER.error("[CI] fallo en la prueba", t);
        } finally {
            server.halt(false);
        }
    }

    // ------------------------------------------------------------ Morvain (parte 7a)

    /**
     * Morvain: la dimension, una muestra de cada region (su bioma, su suelo y lo que tarda el chunk), los bloques del Arco
     * del Umbral (el santuario y el portal de vuelta; la guia y el remolino los pone la primera llegada de verdad: la
     * prueba del cliente) y la ciudadela con sus cadenas y anclas. Vistas: el arco y la ciudadela desde arriba y la
     * ciudadela de frente.
     */
    private static void morvain(MinecraftServer server, PrintWriter log, File out) throws Exception {
        ServerLevel m = com.darkcraft.morvain.Morvain.nivel(server);
        if (m == null) {
            log.println("Morvain: NO EXISTE la dimension darkcraft:morvain");
            return;
        }
        var gen = m.getChunkSource().getGenerator();
        log.println("Morvain: alto " + m.getMinBuildHeight() + ".." + m.getMaxBuildHeight() + ", generador " + gen.getClass().getSimpleName()
                + ", biomas " + gen.getBiomeSource().getClass().getSimpleName() + ", hora fija " + m.dimensionType().fixedTime().orElse(-1L)
                + ", efectos " + m.dimensionType().effectsLocation());
        Object[][] muestras = {{"llanuras", 0, 5600}, {"bosque", -4700, 4300}, {"pantano", -5900, 300}, {"montanas", -2200, -6600},
                {"capital", 4700, 1100}, {"academia", 4700, -4100}, {"costa", 6200, 4600}, {"sin_fe", 0, 2100}, {"centro", 500, 500}, {"mar", 0, 7900}};
        StringBuilder sb = new StringBuilder("Morvain, regiones:");
        int bien = 0;
        long total = 0;
        for (Object[] q : muestras) {
            int x = (Integer) q[1], z = (Integer) q[2];
            long t = System.nanoTime();
            m.getChunk(x >> 4, z >> 4);
            long ms = (System.nanoTime() - t) / 1_000_000;
            total += ms;
            int y = m.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, x, z);
            BlockPos p = new BlockPos(x, y - 1, z);
            String bioma = m.getBiome(p).unwrapKey().map(k -> k.location().getPath()).orElse("?");
            boolean ok = bioma.equals("morvain_" + q[0]);
            if (ok) bien++;
            sb.append(' ').append(q[0]).append(ok ? "" : " (bioma " + bioma + ")").append(" Y ").append(y).append(' ')
                    .append(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(m.getBlockState(p).getBlock()).getPath()).append(' ')
                    .append(ms).append(" ms;");
        }
        log.println(sb + " " + bien + " de " + muestras.length + " con su bioma; " + total + " ms");
        // el Arco del Umbral
        int[] ll = com.darkcraft.morvain.Vista.llegada();
        long t0 = System.currentTimeMillis();
        for (int cx = (ll[0] - 30) >> 4; cx <= (ll[0] + 30) >> 4; cx++)
            for (int cz = (ll[2] - 30) >> 4; cz <= (ll[3] + 6) >> 4; cz++) m.getChunk(cx, cz);
        BlockPos sant = new BlockPos(ll[0], ll[1] + 1, ll[2]);
        int portal = 0;
        for (int dx = -4; dx <= 4; dx++)
            for (int h = 0; h < 13; h++) if (m.getBlockState(new BlockPos(ll[0] + dx, ll[1] + 1 + h, ll[3])).is(DarkCraftMod.PORTAL_SOMBRA.get())) portal++;
        boolean pies = m.getBlockState(new BlockPos(ll[0], ll[1] + 1, ll[2] + 10)).isAir() && m.getBlockState(new BlockPos(ll[0], ll[1] + 2, ll[2] + 10)).isAir()
                && !m.getBlockState(new BlockPos(ll[0], ll[1], ll[2] + 10)).isAir();
        log.println("Morvain, el Arco del Umbral: suelo Y " + ll[1] + ", santuario " + (m.getBlockState(sant).is(DarkCraftMod.SANTUARIO.get()) ? "SI" : "NO")
                + ", portal de vuelta " + portal + " bloques (tiene que ser " + portalCeldas() + "), donde se llega libre " + (pies ? "SI" : "NO")
                + " (" + (System.currentTimeMillis() - t0) + " ms)");
        topView(m, ll[0], ll[2] + 2, 46, new File(out, "morvain_llegada.png"));
        // la ciudadela y sus cadenas
        t0 = System.currentTimeMillis();
        int n = 0;
        for (int cx = -8; cx <= 7; cx++)
            for (int cz = -8; cz <= 7; cz++) {
                m.getChunk(cx, cz);
                n++;
            }
        long ms = System.currentTimeMillis() - t0;
        int cima = com.darkcraft.morvain.Vista.cima();
        String punta = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(m.getBlockState(new BlockPos(0, cima, 0)).getBlock()).getPath();
        int[] e = com.darkcraft.morvain.Vista.eslabon(0, 10);
        m.getChunk(e[0] >> 4, e[2] >> 4);
        int hierro = 0;
        for (int dx = -12; dx <= 12; dx++)
            for (int dy = -12; dy <= 12; dy++)
                for (int dz = -12; dz <= 12; dz++)
                    if (m.getBlockState(new BlockPos(e[0] + dx, e[1] + dy, e[2] + dz)).is(DarkCraftMod.HIERRO_NEGRO.get())) hierro++;
        StringBuilder anclas = new StringBuilder();
        for (int[] a : com.darkcraft.morvain.Vista.anclas()) {
            m.getChunk(a[0] >> 4, a[2] >> 4);
            anclas.append(' ').append(a[0]).append(',').append(a[1]).append(',').append(a[2]).append('=')
                    .append(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(m.getBlockState(new BlockPos(a[0], a[1] + 8, a[2])).getBlock()).getPath());
        }
        log.println(String.format(java.util.Locale.ROOT, "Morvain, la ciudadela: %d chunks en %d ms (%.1f ms cada uno); en la punta de la aguja (Y %d) %s; "
                        + "el eslabon 10 de %d de la cadena del este (en %d %d %d): %d bloques de hierro negro alrededor; anclas:%s", n, ms, ms / (double) n, cima,
                punta, com.darkcraft.morvain.Vista.eslabones(0), e[0], e[1], e[2], hierro, anclas));
        topView(m, 0, 0, 132, new File(out, "morvain_ciudadela.png"));
        alzado(m, -132, 132, 330, Math.min(m.getMaxBuildHeight() - 1, cima + 6), 132, -132, new File(out, "morvain_ciudadela_alzado.png"));
        // el corcel (parte 7b): sus atributos y que herido de muerte no muere, se deshace
        try {
            com.darkcraft.montura.Corcel c = DarkCraftMod.CORCEL.get().create(m);
            if (c == null) log.println("Morvain, el corcel: NO SE CREA");
            else {
                c.moveTo(ll[0] + 0.5, ll[1] + 1, ll[2] + 10.5, 180f, 0f);
                m.addFreshEntity(c);
                String antes = String.format(java.util.Locale.ROOT, "vida %.0f, paso %.2f (galope %.2f), salta %.2f y %.2f, asiento %.2f, cabe %.2f x %.2f",
                        c.getMaxHealth(), c.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED),
                        c.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED) * com.darkcraft.montura.Corcel.GALOPE,
                        com.darkcraft.montura.Corcel.SALTO, com.darkcraft.montura.Corcel.SALTO_DOBLE, c.getPassengersRidingOffset(), c.getBbWidth(), c.getBbHeight());
                boolean caida = c.causeFallDamage(30f, 1f, m.damageSources().fall());
                boolean golpe = c.hurt(m.damageSources().generic(), 500f);
                log.println("Morvain, el corcel: " + antes + "; dano al caer " + caida + "; herido de muerte (" + golpe + "): vivo " + c.isAlive()
                        + ", se deshace " + c.deshaciendo() + ", accion " + c.accion() + " (deshacerse = " + com.darkcraft.enemigos.CorcelAnims.DESHACERSE + ")");
                c.discard();
            }
        } catch (Throwable t) {
            log.println("Morvain, el corcel: ERROR " + t);
        }
        // los santuarios (parte 7c): cada uno con su bloque en su sitio y con su nombre; tres vistos desde arriba
        try {
            StringBuilder ss = new StringBuilder("Morvain, los santuarios:");
            int bienS = 0;
            long t1 = System.currentTimeMillis();
            for (com.darkcraft.morvain.SantuariosMorvain.Sitio s : com.darkcraft.morvain.SantuariosMorvain.SITIOS) {
                m.getChunk(s.x() >> 4, s.z() >> 4);
                BlockPos p = s.pos();
                boolean hay = m.getBlockState(p).is(DarkCraftMod.SANTUARIO.get());
                String nombre = com.darkcraft.santuario.Santuarios.nombre(m, p);
                if (hay && nombre.equals(s.nombre())) bienS++;
                else ss.append(' ').append(s.id()).append(hay ? " (se llama " + nombre + ")" : " (NO hay santuario en " + p.toShortString() + ": "
                        + net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(m.getBlockState(p).getBlock()).getPath() + ")").append(';');
            }
            log.println(ss + " " + bienS + " de " + com.darkcraft.morvain.SantuariosMorvain.SITIOS.length + " en su sitio y con su nombre ("
                    + (System.currentTimeMillis() - t1) + " ms)");
            for (String id : new String[]{"encrucijada", "juncos", "helada"}) {
                com.darkcraft.morvain.SantuariosMorvain.Sitio s = com.darkcraft.morvain.SantuariosMorvain.porId(id);
                for (int cx = (s.x() - 22) >> 4; cx <= (s.x() + 22) >> 4; cx++)
                    for (int cz = (s.z() - 22) >> 4; cz <= (s.z() + 22) >> 4; cz++) m.getChunk(cx, cz);
                topView(m, s.x(), s.z(), 22, new File(out, "morvain_santuario_" + id + ".png"));
            }
        } catch (Throwable t) {
            log.println("Morvain, los santuarios: ERROR " + t);
        }
        // las estelas del mapa (parte 7d): una por region, cada una con su bloque mirando al sur
        try {
            StringBuilder se = new StringBuilder("Morvain, las estelas del mapa:");
            int bienE = 0;
            for (com.darkcraft.morvain.Fragmentos.Estela es : com.darkcraft.morvain.Fragmentos.ESTELAS) {
                m.getChunk(es.x() >> 4, es.z() >> 4);
                var st = m.getBlockState(es.pos());
                boolean hay = st.is(DarkCraftMod.ESTELA_MAPA.get());
                boolean sur = hay && st.getValue(com.darkcraft.morvain.EstelaBlock.FACING) == net.minecraft.core.Direction.SOUTH;
                boolean libre = m.getBlockState(es.pos().south()).isAir() && m.getBlockState(es.pos().south().above()).isAir();
                if (hay && sur && libre) bienE++;
                else se.append(' ').append(es.region().id).append(" (").append(es.pos().toShortString()).append(": ")
                        .append(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(st.getBlock()).getPath())
                        .append(sur ? "" : ", no mira al sur").append(libre ? "" : ", delante no hay hueco").append(");");
            }
            log.println(se + " " + bienE + " de " + com.darkcraft.morvain.Fragmentos.ESTELAS.length + " en su sitio, mirando al sur y con hueco delante");
        } catch (Throwable t) {
            log.println("Morvain, las estelas del mapa: ERROR " + t);
        }
    }

    /** Las celdas del hueco del arco del portal (las de bloque de portal). */
    private static int portalCeldas() {
        int n = 0;
        for (int h = 0; h < com.darkcraft.gen.TorreCupula.AALTO; h++) {
            int w = com.darkcraft.gen.TorreCupula.arcoPortal(h);
            if (w >= 0) n += 2 * w + 1;
        }
        return n;
    }

    /** De frente (desde el sur, mirando al norte): lo primero que se ve en cada (x, y), mas oscuro cuanto mas lejos. */
    private static void alzado(ServerLevel lvl, int x0, int x1, int y0, int y1, int zCerca, int zLejos, File f) throws Exception {
        int w = x1 - x0 + 1, h = y1 - y0 + 1;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int x = x0; x <= x1; x++)
            for (int y = y0; y <= y1; y++) {
                int rgb = 0x5A4A60;
                for (int z = zCerca; z >= zLejos; z--) {
                    BlockState st = lvl.getBlockState(m.set(x, y, z));
                    if (st.isAir()) continue;
                    rgb = shade(color(lvl, m, st), 1.0 - 0.5 * (zCerca - z) / (double) (zCerca - zLejos));
                    break;
                }
                img.setRGB(x - x0, y1 - y, rgb);
            }
        ImageIO.write(img, "png", f);
    }

    // ------------------------------------------------------------ prueba de "se puede subir andando"

    /**
     * La montana de la Fortaleza de Hueso (parte 5b) en el mundo de nubes de verdad: se levantan sus chunks, se sube a pie
     * del pie del sendero al mirador y, con el puente tendido, se cruza hasta el borde de la isla; luego se quita el puente
     * (la prueba del cliente lo tiende con la llave).
     */
    private static void montanaDeLaFortaleza(java.io.PrintWriter log, ServerLevel lvl, com.darkcraft.gen.FortalezaPlan fp) {
        try {
            com.darkcraft.gen.Montana m = fp.montana;
            int n = 0;
            for (long k : fp.chunks()) {
                int cx = (int) (k >> 32), cz = (int) k;
                if (!m.touchesChunk(cx, cz)) continue;
                lvl.getChunk(cx, cz);
                fp.construir(lvl, cx, cz, net.minecraft.util.RandomSource.create(k));
                n++;
            }
            com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> lvl.getBlockState(new BlockPos(x, y, z));
            double[] pie = m.pie();
            int[] a = m.arco();
            var zona = com.darkcraft.pueblo.gen.Andar.desde(mundo, (int) Math.floor(pie[0]), (int) Math.round(pie[2]) + 1, (int) Math.floor(pie[1]),
                    fp.cx - 160, fp.bordeZ - 40, fp.cx + 160, m.mz + 160, 200000);
            boolean arriba = zona.alcanza(a[0], a[1], a[2] + 2, 3);
            // los enemigos del sendero y del mirador (parte 5c): que cada puesto se pisa y se llega
            log.println("Montana de la fortaleza, enemigos: " + com.darkcraft.fortaleza.FortalezaEnemigos.comprobar(mundo, fp, true));
            // el puente tendido: del mirador al borde de la isla (la isla no esta levantada: hasta el arranque del puente)
            java.util.List<int[]> puestos = new java.util.ArrayList<>();
            com.darkcraft.fortaleza.Puente.volcar(fp, (x, y, z, st) -> {
                lvl.setBlock(new BlockPos(x, y, z), st, 2);
                if (!st.isAir()) puestos.add(new int[]{x, y, z});
            });
            var cruza = com.darkcraft.pueblo.gen.Andar.desde(mundo, a[0], a[1], a[2] + 2, fp.cx - 40, fp.bordeZ - 10, fp.cx + 40, a[2] + 20, 60000);
            boolean isla = cruza.alcanza(fp.cx, fp.Y, fp.bordeZ + 2, 2);
            for (int[] q : puestos) lvl.setBlock(new BlockPos(q[0], q[1], q[2]), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
            log.println(String.format(java.util.Locale.ROOT, "Montana de la fortaleza: %d chunks; cima %d,%d a %d; pie %.1f,%.1f a %.1f (%s); "
                            + "sendero de %d puntos y %d recodos; a pie del pie al mirador %s (%d celdas); por el puente (%d bloques) hasta la isla %s",
                    n, m.mx, m.mz, m.cima, pie[0], pie[1], pie[2],
                    lvl.getBlockState(new BlockPos((int) Math.floor(pie[0]), (int) Math.round(pie[2]) - 1, (int) Math.floor(pie[1]))).getBlock().getDescriptionId(),
                    m.sendero().size(), m.recodos().size(), arriba, zona.celdas(), puestos.size(), isla));
        } catch (Throwable t) {
            log.println("Montana de la fortaleza: ERROR " + t);
            t.printStackTrace();
        }
    }

    /**
     * Las paradas del camino de nubes (parte 5d) en el mundo de nubes de verdad: donde caen, se levantan una a una y se mira
     * que se cruzan por el camino, que se llega al cofre y al santuario y que cada puesto de sus enemigos se pisa.
     */
    private static void paradasDelCamino(java.io.PrintWriter log, ServerLevel lvl, int[] s) {
        try {
            var ps = com.darkcraft.camino.Paradas.de(lvl.getSeed(), s[0], s[1]);
            StringBuilder b = new StringBuilder("Camino de nubes, paradas: " + ps.size() + ":");
            for (var p : ps)
                b.append(String.format(java.util.Locale.ROOT, " %d %s en %d,%d (a %d)", p.k(), p.nombre(), p.plan().cx, p.plan().cz, Math.round(p.s())));
            log.println(b);
            com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> lvl.getBlockState(new BlockPos(x, y, z));
            for (var p : ps) {
                long t0 = System.currentTimeMillis();
                for (long k : p.plan().chunks()) lvl.getChunk((int) (k >> 32), (int) k);
                int n = com.darkcraft.camino.Paradas.construirYa(lvl, p);
                String r = com.darkcraft.camino.Paradas.comprobar(mundo, p);
                int[] sa = p.plan().santuario(), cf = p.plan().cofre();
                String extra = (sa == null ? "" : "; santuario " + (lvl.getBlockState(new BlockPos(sa[0], sa[1], sa[2])).is(DarkCraftMod.SANTUARIO.get()) ? "puesto" : "NO"))
                        + (cf == null ? "" : "; cofre " + (lvl.getBlockEntity(new BlockPos(cf[0], cf[1], cf[2])) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity ? "puesto" : "NO"));
                log.println("  parada " + r + "; " + n + " chunks en " + (System.currentTimeMillis() - t0) + " ms" + extra);
            }
            String k = com.darkcraft.camino.Paradas.clave(s[0], s[1]);
            log.println("  " + com.darkcraft.camino.Paradas.resumen(lvl, s[0], s[1]));
            // (la prueba del cliente las vuelve a ver; los enemigos, por caidos hasta entonces)
            if (com.darkcraft.enemigos.Puestos.apuntado(k)) com.darkcraft.enemigos.Puestos.todos(lvl.getServer(), k, true);
        } catch (Throwable t) {
            log.println("Camino de nubes, paradas: ERROR " + t);
            t.printStackTrace();
        }
    }

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
        java.util.Set<String> skip = java.util.Set.of("minecraft", "forge", "darkcraft");
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
    /** Lo alto de la nube de verdad en esa columna (o -1 si no hay nube entre 225 y 264). */
    /** Lo alto del mar de nubes en esa columna; -1 si lo de mas arriba no es nube (el anden, el camino de nube firme, el barco). */
    private static int nubeArriba(ServerLevel lvl, BlockPos.MutableBlockPos mb, int x, int z) {
        for (int y = 290; y >= 225; y--) {
            var s = lvl.getBlockState(mb.set(x, y, z));
            if (s.isAir()) continue;
            return s.is(DarkCraftMod.NUBE.get()) ? y : -1;
        }
        return -1;
    }

    /**
     * Parte 3d-2: el plano del barco sin mundo contra el barco de verdad, repararlo sin que salte nada, el muelle de
     * Valdrenn (el mar en calma, el anden, que se recorre a pie), llevar el barco de un muelle al otro con lo que guardan
     * sus barriles, y los planos y el vuelo de las tres cinematicas (la camara y el casco, siempre en el aire). Al acabar
     * lo deja todo como estaba, tambien las nubes de Valdrenn: el muelle tiene que hacerse solo en la prueba del cliente.
     */
    /**
     * La guardia de Valdrenn (parte 4a): los puestos del reino en el mundo de verdad (que se pisan y se llega), que salen
     * todos y en el suelo, que cada uno hace todas sus acciones sin romper nada, el escudo (de frente para casi todo; por la
     * espalda no), que la ballesta y el arco disparan, y que al caer el Rey y Ser Gavran se van los que tienen que irse.
     */
    private static void guardiaDeValdrenn(net.minecraft.server.MinecraftServer server, ServerLevel lvl, java.io.PrintWriter log, int[] s) {
        try {
            CastlePlan plan = com.darkcraft.story.Story.planAt(lvl, s[0], s[1]);
            com.darkcraft.valdrenn.ValdrennEnemigos.apuntar(plan);
            String k = com.darkcraft.valdrenn.ValdrennEnemigos.clave(plan);
            BlockPos.MutableBlockPos mb = new BlockPos.MutableBlockPos();
            com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> lvl.getBlockState(mb.set(x, y, z));
            log.println("Valdrenn, puestos de la guardia: " + com.darkcraft.valdrenn.ValdrennEnemigos.comprobar(mundo, plan));
            com.darkcraft.enemigos.Puestos.todos(server, k, false);
            // (las emboscadas, por disparadas: asi salen tambien sus puestos)
            com.darkcraft.valdrenn.Emboscadas.marcarTodas(k);
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(lvl, k);
            var puestos = com.darkcraft.enemigos.Puestos.sitio(k).puestos;
            int enSuelo = 0, chocan = 0, movidos = 0, fallos = 0;
            java.util.Map<String, Integer> tipos = new java.util.TreeMap<>();
            StringBuilder mal = new StringBuilder();
            for (var e : fuera) {
                tipos.merge(e.tipo() + (e.postura() == 1 ? " (reza)" : e.postura() == 2 ? " (tumbado)" : ""), 1, Integer::sum);
                boolean suelo = !lvl.getBlockState(BlockPos.containing(e.getX(), e.getY() - 0.2, e.getZ())).getCollisionShape(lvl, BlockPos.ZERO).isEmpty();
                if (suelo) enSuelo++;
                else if (mal.length() < 300) mal.append(' ').append(e.tipo()).append(" en el aire en ").append(e.blockPosition().toShortString()).append(';');
                if (!lvl.noCollision(e)) {
                    chocan++;
                    if (mal.length() < 400) mal.append(' ').append(e.tipo()).append(" metido en un bloque en ").append(e.blockPosition().toShortString()).append(';');
                }
                var q = puestos.get(e.puesto());
                if (Math.abs(e.getX() - (q.x() + 0.5)) > 0.1 || Math.abs(e.getZ() - (q.z() + 0.5)) > 0.1) movidos++;
            }
            StringBuilder sinSitio = new StringBuilder();
            for (int i = 0; i < puestos.size(); i++)
                if (com.darkcraft.enemigos.Puestos.enemigo(k, i) == null && sinSitio.length() < 400)
                    sinSitio.append(' ').append(puestos.get(i).grupo()).append(' ').append(puestos.get(i).tipo()).append(" en ")
                            .append(puestos.get(i).x() - s[0]).append(',').append(puestos.get(i).y()).append(',').append(puestos.get(i).z() - s[1]).append(';');
            // cada uno hace todas sus acciones (sin presa): que ninguna rompa nada
            for (var e : fuera) {
                try {
                    int postura = e.postura();
                    e.postura(0);
                    String[] nombres = e instanceof com.darkcraft.enemigos.Arquero ? com.darkcraft.enemigos.ArqueroAnims.NOMBRES
                            : e instanceof com.darkcraft.enemigos.Perro ? com.darkcraft.enemigos.PerroAnims.NOMBRES
                            : e instanceof com.darkcraft.enemigos.Espectro ? com.darkcraft.enemigos.EspectroAnims.NOMBRES
                            : e instanceof com.darkcraft.enemigos.Elite ? com.darkcraft.enemigos.EliteAnims.NOMBRES
                            : e instanceof com.darkcraft.enemigos.Brandt ? com.darkcraft.enemigos.BrandtAnims.NOMBRES
                            : e instanceof com.darkcraft.enemigos.Corteza ? com.darkcraft.enemigos.CortezaAnims.NOMBRES : com.darkcraft.enemigos.GuardiaAnims.NOMBRES;
                    int acciones = nombres.length;
                    for (int a = 1; a < acciones; a++) {
                        String nombre = nombres[a];
                        if (nombre.equals("morir")) continue;
                        e.empezar(a);
                        for (int t = 0; t < 90 && e.accion() == a; t++) e.tick();
                        if (e.accion() == a) {
                            fallos++;
                            if (mal.length() < 600) mal.append(' ').append(e.tipo()).append(": la accion ").append(nombre).append(" no acaba;");
                        }
                    }
                    for (int t = 0; t < 10; t++) e.tick();
                    e.postura(postura);
                } catch (Throwable t) {
                    fallos++;
                    if (mal.length() < 800) mal.append(' ').append(e.tipo()).append(": ").append(t).append(';');
                    DarkCraftMod.LOGGER.error("[CI] guardia " + e.tipo(), t);
                }
            }
            log.println("Valdrenn, la guardia fuera: " + fuera.size() + " de " + puestos.size() + " " + tipos + "; en el suelo " + enSuelo + ", metidos en un bloque "
                    + chocan + " (tiene que ser 0), apartados de su sitio " + movidos + ", acciones que fallan " + fallos + " (tiene que ser 0); sin sitio:" + sinSitio + mal);
            // el escudo: de frente casi nada; por la espalda, todo. Un muneco que hace de atacante (no se mete en el mundo)
            var g = DarkCraftMod.GUARDIA.get().create(lvl);
            var otro = DarkCraftMod.GUARDIA.get().create(lvl);
            String escudo = "sin guardia";
            if (g != null && otro != null) {
                double gx = s[0] + 0.5, gy = CastlePlan.Y0, gz = s[1] + 66.5;
                g.variante(com.darkcraft.enemigos.Guardia.ESPADA);
                g.moveTo(gx, gy, gz, 0f, 0f);
                g.yBodyRot = g.yHeadRot = 0f;
                lvl.addFreshEntity(g);
                otro.moveTo(gx, gy, gz + 2, 180f, 0f);
                float v0 = g.getHealth();
                g.hurt(lvl.damageSources().mobAttack(otro), 10f);
                float deFrente = v0 - g.getHealth();
                int bloquea = g.accion();
                g.setHealth(g.getMaxHealth());
                g.invulnerableTime = 0;
                for (int t = 0; t < 15; t++) g.tick();
                otro.moveTo(gx, gy, gz - 2, 0f, 0f);
                v0 = g.getHealth();
                g.hurt(lvl.damageSources().mobAttack(otro), 10f);
                float porDetras = v0 - g.getHealth();
                escudo = String.format(java.util.Locale.ROOT, "de frente quita %.1f (accion %s), por la espalda %.1f", deFrente,
                        bloquea == com.darkcraft.enemigos.GuardiaAnims.ESPADA_BLOQUEA ? "bloquea" : String.valueOf(bloquea), porDetras);
                g.discard();
            }
            // la ballesta y el arco disparan (a un soporte de armadura a 12 bloques, que hace de presa)
            int flechas0 = lvl.getEntitiesOfClass(net.minecraft.world.entity.projectile.Arrow.class, new net.minecraft.world.phys.AABB(s[0] - 40, 360, s[1] + 40, s[0] + 40, 420, s[1] + 100)).size();
            var blanco = net.minecraft.world.entity.EntityType.ARMOR_STAND.create(lvl);
            var b = DarkCraftMod.GUARDIA.get().create(lvl);
            var a = DarkCraftMod.ARQUERO.get().create(lvl);
            String disparos = "sin tiradores";
            if (blanco != null && b != null && a != null) {
                blanco.moveTo(s[0] + 0.5, CastlePlan.Y0, s[1] + 82.5, 0f, 0f);
                lvl.addFreshEntity(blanco);
                b.variante(com.darkcraft.enemigos.Guardia.BALLESTA);
                b.moveTo(s[0] - 3.5, CastlePlan.Y0, s[1] + 70.5, 0f, 0f);
                a.moveTo(s[0] + 3.5, CastlePlan.Y0, s[1] + 70.5, 0f, 0f);
                lvl.addFreshEntity(b);
                lvl.addFreshEntity(a);
                b.setTarget(blanco);
                a.setTarget(blanco);
                b.empezar(com.darkcraft.enemigos.GuardiaAnims.BALLESTA_DISPARO);
                a.empezar(com.darkcraft.enemigos.ArqueroAnims.DISPARO);
                for (int t = 0; t < 30; t++) {
                    b.tick();
                    a.tick();
                }
                int flechas = lvl.getEntitiesOfClass(net.minecraft.world.entity.projectile.Arrow.class, new net.minecraft.world.phys.AABB(s[0] - 40, 360, s[1] + 40, s[0] + 40, 420, s[1] + 100)).size() - flechas0;
                disparos = flechas + " flechas (tiene que ser 2)";
                for (var f : lvl.getEntitiesOfClass(net.minecraft.world.entity.projectile.Arrow.class, new net.minecraft.world.phys.AABB(s[0] - 40, 360, s[1] + 40, s[0] + 40, 420, s[1] + 100))) f.discard();
                blanco.discard();
                b.discard();
                a.discard();
            }
            // al caer el Rey y Ser Gavran se van los suyos
            com.darkcraft.enemigos.Puestos.todos(server, k, false);
            var est = com.darkcraft.rey.ReyEstado.get(server);
            String claveRey = plan.cx + "," + plan.cz;
            var sala = com.darkcraft.gavran.SalaArbol.de(plan);
            int rey0 = est.get(claveRey), gav0 = sala == null ? 0 : est.get(sala.key());
            est.set(claveRey, com.darkcraft.rey.ReyEstado.MUERTO);
            int sinRey = com.darkcraft.enemigos.Puestos.sacarTodos(lvl, k).size();
            com.darkcraft.enemigos.Puestos.todos(server, k, false);
            if (sala != null) est.set(sala.key(), com.darkcraft.rey.ReyEstado.MUERTO);
            int sinLosDos = com.darkcraft.enemigos.Puestos.sacarTodos(lvl, k).size();
            est.set(claveRey, rey0);
            if (sala != null) est.set(sala.key(), gav0);
            com.darkcraft.enemigos.Puestos.todos(server, k, false);
            com.darkcraft.valdrenn.Emboscadas.armar(k);
            log.println("Valdrenn, la guardia: escudo " + escudo + "; disparan " + disparos + "; con el Rey vencido salen " + sinRey + " de " + puestos.size()
                    + ", con el Rey y Ser Gavran " + sinLosDos + " | " + com.darkcraft.valdrenn.ValdrennEnemigos.estado(server, k));
        } catch (Throwable t) {
            log.println("Valdrenn, la guardia: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] la guardia de Valdrenn", t);
        }
    }

    /**
     * Parte 4b: el salto del perro (cuanto avanza y si muerde al caer), el espectro que se desvanece (donde aparece) y que
     * las flechas de los arqueros pasen entre los suyos.
     */
    private static void perrosYEspectros(net.minecraft.server.MinecraftServer server, ServerLevel lvl, java.io.PrintWriter log, int[] s) {
        try {
            double x = s[0] + 0.5, y = CastlePlan.Y0, z = s[1] + 66.5;
            // el salto: a 5 bloques de un soporte de armadura (que hace de presa)
            String salto = "sin perro";
            var perro = DarkCraftMod.PERRO.get().create(lvl);
            var blanco = net.minecraft.world.entity.EntityType.ARMOR_STAND.create(lvl);
            if (perro != null && blanco != null) {
                blanco.moveTo(x, y, z + 5.0, 180f, 0f);
                lvl.addFreshEntity(blanco);
                perro.moveTo(x, y, z, 0f, 0f);
                perro.yBodyRot = perro.yHeadRot = 0f;
                lvl.addFreshEntity(perro);
                perro.setTarget(blanco);
                for (int t = 0; t < 45 && perro.accion() != 0; t++) perro.tick(); // (el aullido de cuando ve a alguien)
                float v0 = blanco.getHealth();
                double z0 = perro.getZ();
                perro.empezar(com.darkcraft.enemigos.PerroAnims.SALTO);
                double alto = 0;
                for (int t = 0; t < 32; t++) {
                    perro.tick();
                    alto = Math.max(alto, perro.getY() - y);
                }
                salto = String.format(java.util.Locale.ROOT, "avanza %.1f bloques (tiene que ser 2-5) y sube %.1f; le quita al blanco %.1f", perro.getZ() - z0, alto, v0 - blanco.getHealth());
                perro.discard();
                blanco.discard();
            }
            // el espectro: a 10 bloques de su presa se desvanece y aparece a su lado o a su espalda
            String desvanece = "sin espectro";
            var esp = DarkCraftMod.ESPECTRO.get().create(lvl);
            var presa = net.minecraft.world.entity.EntityType.ARMOR_STAND.create(lvl);
            if (esp != null && presa != null) {
                presa.moveTo(x, y, z + 10.0, 180f, 0f);
                lvl.addFreshEntity(presa);
                esp.moveTo(x, y, z, 0f, 0f);
                lvl.addFreshEntity(esp);
                esp.setTarget(presa);
                esp.empezar(com.darkcraft.enemigos.EspectroAnims.DESVANECERSE);
                for (int t = 0; t < 30; t++) esp.tick();
                desvanece = String.format(java.util.Locale.ROOT, "aparece a %.1f de su presa (tiene que ser 2-3,5), %s", esp.distanceTo(presa),
                        lvl.noCollision(esp) ? "en un sitio libre" : "METIDO EN UN BLOQUE");
                esp.discard();
                presa.discard();
            }
            // las flechas de los suyos: una flecha de un arquero contra un caballero que esta en medio
            String flecha = "sin flecha";
            var arq = DarkCraftMod.ARQUERO.get().create(lvl);
            var cab = DarkCraftMod.GUARDIA.get().create(lvl);
            if (arq != null && cab != null) {
                arq.moveTo(x, y, z - 6, 0f, 0f);
                cab.moveTo(x, y, z, 180f, 0f);
                lvl.addFreshEntity(arq);
                lvl.addFreshEntity(cab);
                var f = new com.darkcraft.enemigos.Flecha(lvl, arq);
                f.setPos(x, y + 1.2, z - 3.0);
                f.shoot(0, 0, 1, 2.0f, 0f);
                lvl.addFreshEntity(f);
                for (int t = 0; t < 6; t++) f.tick();
                flecha = String.format(java.util.Locale.ROOT, "el caballero %s, la flecha sigue (z %+.1f del caballero)", cab.getHealth() >= cab.getMaxHealth() ? "sin un rasguno" : "HERIDO", f.getZ() - z);
                f.discard();
                arq.discard();
                cab.discard();
            }
            log.println("Valdrenn, perros y espectros: el salto del perro " + salto + "; el espectro " + desvanece + "; " + flecha);
        } catch (Throwable t) {
            log.println("Valdrenn, perros y espectros: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] perros y espectros", t);
        }
    }

    /**
     * Parte 4c: el capitan de elite (la arenga a los suyos, la onda del tajo, la embestida, los suyos desanimados al caer)
     * y las emboscadas (cada una saca a los suyos tumbados y a por quien la dispara; al descansar se esconden y se arman).
     */
    private static void elitesYEmboscadas(net.minecraft.server.MinecraftServer server, ServerLevel lvl, java.io.PrintWriter log, int[] s) {
        try {
            double x = s[0] + 0.5, y = CastlePlan.Y0, z = s[1] + 66.5;
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(lvl, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("5f2b8c1e-3d4a-4e6b-9a7c-1b2d3e4f5a6b"), "[DarkCraftCI]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            // ---- la arenga: el capitan y dos caballeros sin presa a 4 bloques; la presa, a 8 delante
            String arenga = "sin capitan", cae = "", onda = "", embestida = "";
            var cap = DarkCraftMod.ELITE.get().create(lvl);
            var g1 = DarkCraftMod.GUARDIA.get().create(lvl);
            var g2 = DarkCraftMod.GUARDIA.get().create(lvl);
            if (cap != null && g1 != null && g2 != null) {
                cap.moveTo(x, y, z, 0f, 0f);
                cap.yBodyRot = cap.yHeadRot = 0f;
                g1.moveTo(x - 4, y, z, 0f, 0f);
                g2.moveTo(x + 4, y, z, 0f, 0f);
                lvl.addFreshEntity(cap);
                lvl.addFreshEntity(g1);
                lvl.addFreshEntity(g2);
                fp.moveTo(x, y, z + 8, 180f, 0f);
                cap.setTarget(fp);
                cap.empezar(com.darkcraft.enemigos.EliteAnims.GRITO);
                for (int t = 0; t < 14; t++) cap.tick();
                int vanAPor = (g1.getTarget() == fp ? 1 : 0) + (g2.getTarget() == fp ? 1 : 0);
                arenga = String.format(java.util.Locale.ROOT, "los dos de al lado van a por su presa %d/2, animo %.2f y %.2f (tiene que ser 0,30), golpe %.1f -> %.1f",
                        vanAPor, g1.animo(), g2.animo(), com.darkcraft.enemigos.Guardia.atributos().build().getValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE),
                        g1.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE));
                for (int t = 0; t < 30 && cap.accion() != 0; t++) cap.tick();
                // ---- la onda del tajo: un jugador de pie a un lado del golpe (fuera del arco de la hoja) la recibe; en el aire, no
                fp.setHealth(fp.getMaxHealth());
                fp.invulnerableTime = 0;
                cap.moveTo(x, y, z, 0f, 0f);
                cap.yBodyRot = cap.yHeadRot = 0f;
                cap.setYRot(0f);
                fp.moveTo(x + 1.9, y, z + 2.9, 180f, 0f);
                fp.setOnGround(true);
                float v0 = fp.getHealth();
                cap.setTarget(null);
                cap.empezar(com.darkcraft.enemigos.EliteAnims.TAJO);
                for (int t = 0; t < 40 && cap.accion() != 0; t++) {
                    fp.setOnGround(true);
                    cap.tick();
                }
                float deSuelo = v0 - fp.getHealth();
                fp.setHealth(fp.getMaxHealth());
                fp.invulnerableTime = 0;
                cap.moveTo(x, y, z, 0f, 0f);
                cap.setYRot(0f);
                fp.moveTo(x + 1.9, y + 1.2, z + 2.9, 180f, 0f);
                fp.setOnGround(false);
                v0 = fp.getHealth();
                cap.empezar(com.darkcraft.enemigos.EliteAnims.TAJO);
                for (int t = 0; t < 40 && cap.accion() != 0; t++) {
                    fp.setOnGround(false);
                    cap.tick();
                }
                float enElAire = v0 - fp.getHealth();
                // (el jugador de mentira no esta en el mundo: lo que busca jugadores alrededor no lo encuentra; la onda se ve en el cliente)
                onda = String.format(java.util.Locale.ROOT, "la onda del tajo al jugador de mentira %.1f y %.1f (0: no esta en el mundo)", deSuelo, enElAire);
                // ---- la embestida: cuanto avanza
                cap.moveTo(x, y, z - 6, 0f, 0f);
                cap.setYRot(0f);
                cap.yBodyRot = cap.yHeadRot = 0f;
                double z0 = cap.getZ();
                cap.empezar(com.darkcraft.enemigos.EliteAnims.EMBESTIDA);
                for (int t = 0; t < 34 && cap.accion() != 0; t++) cap.tick();
                embestida = String.format(java.util.Locale.ROOT, "la embestida avanza %.1f bloques (3-6)", cap.getZ() - z0);
                // ---- cae el capitan: los suyos se desaniman
                cap.hurt(lvl.damageSources().generic(), 9999f);
                cae = String.format(java.util.Locale.ROOT, "al caer el capitan, animo de los suyos %.2f y %.2f (tiene que ser -0,25), su paso %.3f -> %.3f",
                        g1.animo(), g2.animo(), com.darkcraft.enemigos.Guardia.atributos().build().getValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED),
                        g1.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED));
                for (int t = 0; t < 70 && !cap.isRemoved(); t++) cap.tick();
                cap.discard();
                g1.discard();
                g2.discard();
            }
            log.println("Valdrenn, el capitan de elite: arenga " + arenga + "; " + onda + "; " + embestida + "; " + cae);
            // ---- las emboscadas del reino: cada una, junto a su zona
            CastlePlan plan = com.darkcraft.story.Story.planAt(lvl, s[0], s[1]);
            String k = com.darkcraft.valdrenn.ValdrennEnemigos.clave(plan);
            com.darkcraft.enemigos.Puestos.todos(server, k, false);
            com.darkcraft.valdrenn.Emboscadas.armar(k);
            var sitio = com.darkcraft.enemigos.Puestos.sitio(k);
            StringBuilder b = new StringBuilder();
            int bien = 0, todas = 0;
            for (var e : com.darkcraft.valdrenn.Emboscadas.de(k)) {
                todas++;
                int suyos = 0;
                for (var q : sitio.puestos) if (e.nombre().equals(com.darkcraft.valdrenn.Emboscadas.nombre(q.grupo()))) suyos++;
                var c = e.zona().getCenter();
                fp.moveTo(c.x, e.zona().minY + 1, c.z, 0f, 0f);
                // (antes de disparar, ninguno de los suyos esta fuera aunque el jugador este al lado; las ya disparadas, si)
                int antes = com.darkcraft.enemigos.Puestos.sacarTodos(lvl, k).stream().filter(en -> e.nombre().equals(com.darkcraft.valdrenn.Emboscadas.nombre(sitio.puestos.get(en.puesto()).grupo()))).toList().size();
                int salen = com.darkcraft.valdrenn.Emboscadas.disparar(lvl, k, e.nombre(), fp);
                int tumbados = 0, aPorEl = 0, dePie = 0;
                java.util.List<com.darkcraft.enemigos.Enemigo> suyosFuera = new java.util.ArrayList<>();
                for (int i = 0; i < sitio.puestos.size(); i++) {
                    if (!e.nombre().equals(com.darkcraft.valdrenn.Emboscadas.nombre(sitio.puestos.get(i).grupo()))) continue;
                    var en = com.darkcraft.enemigos.Puestos.enemigo(k, i);
                    if (en == null) continue;
                    suyosFuera.add(en);
                    if (en.postura() == com.darkcraft.enemigos.Enemigo.TUMBADO) tumbados++;
                    if (en.getTarget() == fp) aPorEl++;
                }
                for (int t = 0; t < 50; t++) for (var en : suyosFuera) en.tick();
                for (var en : suyosFuera) if (en.postura() == com.darkcraft.enemigos.Enemigo.DE_PIE) dePie++;
                if (antes == 0 && salen == suyos && tumbados == suyos && aPorEl == suyos && dePie == suyos) bien++;
                b.append(String.format(java.util.Locale.ROOT, " %s: %d puestos, antes fuera %d, salen %d, tumbados %d, a por el %d, en pie a los 2,5 s %d;",
                        e.nombre(), suyos, antes, salen, tumbados, aPorEl, dePie));
                // los demas que salieron con sacarTodos (para contar los de antes), fuera
                com.darkcraft.enemigos.Puestos.todos(server, k, false);
            }
            // al descansar (o morir) se esconden los que quedan en pie y se vuelven a armar
            com.darkcraft.valdrenn.Emboscadas.marcarTodas(k);
            int sacados = com.darkcraft.valdrenn.Emboscadas.disparar(lvl, k, "plaza", fp);
            com.darkcraft.valdrenn.Emboscadas.marcarTodas(k);
            int fueraAntes = 0;
            for (int i = 0; i < sitio.puestos.size(); i++)
                if (com.darkcraft.valdrenn.Emboscadas.nombre(sitio.puestos.get(i).grupo()) != null && com.darkcraft.enemigos.Puestos.enemigo(k, i) != null) fueraAntes++;
            com.darkcraft.enemigos.Puestos.reaparecer(fp);
            int fueraDespues = 0;
            for (int i = 0; i < sitio.puestos.size(); i++)
                if (com.darkcraft.valdrenn.Emboscadas.nombre(sitio.puestos.get(i).grupo()) != null && com.darkcraft.enemigos.Puestos.enemigo(k, i) != null) fueraDespues++;
            int arbol = 0, elites = 0;
            for (var q : sitio.puestos) {
                if (q.grupo().startsWith("torre_arbol")) arbol++;
                if (q.tipo().equals(com.darkcraft.valdrenn.ValdrennEnemigos.ELITE)) elites++;
            }
            log.println("Valdrenn, las emboscadas: " + bien + " de " + todas + " bien (tiene que ser todas);" + b + " al descansar: de " + fueraAntes + " fuera quedan "
                    + fueraDespues + " (0), disparadas " + com.darkcraft.valdrenn.Emboscadas.disparadas(k).size() + " (0) [antes salian " + sacados + "]; la torre del arbol "
                    + arbol + " puestos, capitanes de elite en el reino " + elites);
            com.darkcraft.enemigos.Puestos.todos(server, k, false);
            com.darkcraft.valdrenn.Emboscadas.armar(k);
        } catch (Throwable t) {
            log.println("Valdrenn, capitanes y emboscadas: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] capitanes y emboscadas", t);
        }
    }

    /**
     * Parte 4d: los dos guardianes. Sus sellos (con el guardian en pie se cierran; al caer se rompen; con su jefe vencido no
     * hay), la pelea (empieza al ver a alguien: se levanta y ruge; a media vida ruge otra vez y cambia), el paves de Ser
     * Brandt (de frente casi nada; en el muro nada y luego contesta con un escudazo; por la espalda todo; las flechas no),
     * las raices y las espinas de Vareth y el fuego (le hace el doble), lo que queda al caer y que no vuelven al descansar.
     */
    private static void guardianesDeValdrenn(net.minecraft.server.MinecraftServer server, ServerLevel lvl, java.io.PrintWriter log, int[] s) {
        var est = com.darkcraft.rey.ReyEstado.get(server);
        String claveRey = null, claveGavran = null, k = null;
        int rey0 = 0, gav0 = 0;
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        try {
            CastlePlan plan = com.darkcraft.story.Story.planAt(lvl, s[0], s[1]);
            com.darkcraft.valdrenn.ValdrennEnemigos.apuntar(plan);
            k = com.darkcraft.valdrenn.ValdrennEnemigos.clave(plan);
            final String B = com.darkcraft.valdrenn.ValdrennEnemigos.BRANDT, V = com.darkcraft.valdrenn.ValdrennEnemigos.CORTEZA;
            claveRey = plan.cx + "," + plan.cz;
            var sala = com.darkcraft.gavran.SalaArbol.de(plan);
            claveGavran = sala == null ? null : sala.key();
            rey0 = est.get(claveRey);
            gav0 = claveGavran == null ? 0 : est.get(claveGavran);
            // (ni el Rey ni Ser Gavran vencidos: si no, no hay guardianes)
            est.set(claveRey, com.darkcraft.rey.ReyEstado.DORMIDO);
            if (claveGavran != null) est.set(claveGavran, com.darkcraft.rey.ReyEstado.DORMIDO);
            com.darkcraft.enemigos.Puestos.todos(server, k, false);
            int pb = com.darkcraft.valdrenn.Guardianes.puesto(k, B), pv = com.darkcraft.valdrenn.Guardianes.puesto(k, V);
            for (String t : new String[]{B, V})
                for (BlockPos b : com.darkcraft.valdrenn.Guardianes.bloques(k, t)) lvl.getChunk(b.getX() >> 4, b.getZ() >> 4);
            // ---- los sellos, con los dos en pie
            com.darkcraft.valdrenn.Guardianes.revisar(lvl, k);
            String cerrados = sello(lvl, k, B) + ", " + sello(lvl, k, V);
            // ---- salen los dos (de rodillas en su puesto) y pelean con un jugador de mentira
            com.darkcraft.enemigos.Brandt brandt = null;
            com.darkcraft.enemigos.Corteza vareth = null;
            for (var e : com.darkcraft.enemigos.Puestos.sacarGrupo(lvl, k, g -> g.startsWith(com.darkcraft.enemigos.Puestos.JEFE))) {
                quitar.add(e);
                if (e instanceof com.darkcraft.enemigos.Brandt b) brandt = b;
                else if (e instanceof com.darkcraft.enemigos.Corteza c) vareth = c;
            }
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(lvl, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("5f2b8c1e-3d4a-4e6b-9a7c-1b2d3e4f5a6b"), "[DarkCraftCI]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            String sb = "Ser Brandt no sale", sv = "Vareth no sale";
            if (brandt != null) {
                String sitio = String.format(java.util.Locale.ROOT, "en %d,%d,%d del reino", brandt.getBlockX() - s[0], brandt.getBlockY(), brandt.getBlockZ() - s[1]);
                sb = guardianPelea(brandt, fp, com.darkcraft.enemigos.BrandtAnims.RUGIDO) + " " + sitio + "; " + paves(lvl, brandt, fp);
            }
            if (vareth != null) {
                String sitio = String.format(java.util.Locale.ROOT, "en %d,%d,%d del reino", vareth.getBlockX() - s[0], vareth.getBlockY(), vareth.getBlockZ() - s[1]);
                sv = guardianPelea(vareth, fp, com.darkcraft.enemigos.CortezaAnims.RUGIDO) + " " + sitio + "; " + corteza(lvl, vareth, fp, quitar);
            }
            log.println("Valdrenn, los guardianes: sellos " + cerrados + " (cerrados del todo); " + sb + " | " + sv);
            // ---- caen: el sello se rompe en el acto, el puesto queda por caido y al descansar no vuelven
            int repartos0 = com.darkcraft.valdrenn.Guardianes.repartosPendientes();
            String caen = "";
            for (var g : new com.darkcraft.enemigos.Guardian[]{brandt, vareth}) {
                if (g == null) continue;
                g.setTarget(fp);
                g.hurt(lvl.damageSources().playerAttack(fp), 99999f);
                caen += String.format(java.util.Locale.ROOT, " %s cae (vivo %s, puesto por caido %s, rivales %d);", g.tipo(), g.isAlive(),
                        com.darkcraft.enemigos.Puestos.muerto(server, k, g.puesto()), g.rivales().size());
                for (int t = 0; t < 70 && !g.isRemoved(); t++) g.tick();
            }
            String abiertos = sello(lvl, k, B) + ", " + sello(lvl, k, V);
            int repartos = com.darkcraft.valdrenn.Guardianes.repartosPendientes() - repartos0;
            fp.moveTo(s[0] + 0.5, CastlePlan.Y0, s[1] + 66.5, 0f, 0f);
            com.darkcraft.enemigos.Puestos.reaparecer(fp);
            boolean siguenCaidos = (pb < 0 || com.darkcraft.enemigos.Puestos.muerto(server, k, pb)) && (pv < 0 || com.darkcraft.enemigos.Puestos.muerto(server, k, pv));
            com.darkcraft.valdrenn.Guardianes.revisar(lvl, k);
            String tras = sello(lvl, k, B) + ", " + sello(lvl, k, V);
            // ---- con el Rey vencido no hay Ser Brandt (ni sello); con Ser Gavran vencido, ni Vareth
            com.darkcraft.enemigos.Puestos.todos(server, k, false);
            est.set(claveRey, com.darkcraft.rey.ReyEstado.MUERTO);
            if (claveGavran != null) est.set(claveGavran, com.darkcraft.rey.ReyEstado.MUERTO);
            com.darkcraft.valdrenn.Guardianes.revisar(lvl, k);
            String sinJefes = sello(lvl, k, B) + ", " + sello(lvl, k, V);
            int salenSinJefes = com.darkcraft.enemigos.Puestos.sacarGrupo(lvl, k, g -> g.startsWith(com.darkcraft.enemigos.Puestos.JEFE)).size();
            log.println("Valdrenn, los guardianes caen:" + caen + " sellos " + abiertos + " (abiertos); quedan por repartir " + repartos
                    + " (2: a los 4 s, cartel, ecos y logro); al descansar siguen caidos " + siguenCaidos + " y los sellos " + tras
                    + "; con el Rey y Ser Gavran vencidos salen " + salenSinJefes + " (0) y los sellos " + sinJefes);
        } catch (Throwable t) {
            log.println("Valdrenn, los guardianes: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] los guardianes", t);
        } finally {
            // todo como estaba: los dos en pie, los sellos abiertos (se cierran cuando llegue alguien) y el Rey y Ser Gavran
            try {
                for (var e : quitar) if (!e.isRemoved()) e.discard();
                if (claveRey != null) est.set(claveRey, rey0);
                if (claveGavran != null) est.set(claveGavran, gav0);
                if (k != null) {
                    com.darkcraft.enemigos.Puestos.todos(server, k, true);
                    com.darkcraft.valdrenn.Guardianes.revisar(lvl, k);
                    com.darkcraft.enemigos.Puestos.todos(server, k, false);
                    com.darkcraft.valdrenn.Emboscadas.armar(k);
                }
            } catch (Throwable t) {
                DarkCraftMod.LOGGER.error("[CI] los guardianes, al acabar", t);
            }
        }
    }

    /** Cuantos bloques de un sello estan cerrados ("brandt 45/45"). */
    private static String sello(ServerLevel lvl, String k, String tipo) {
        var l = com.darkcraft.valdrenn.Guardianes.bloques(k, tipo);
        int n = 0;
        for (BlockPos b : l) if (com.darkcraft.valdrenn.Guardianes.cerrado(lvl, b)) n++;
        return tipo + " " + n + "/" + l.size();
    }

    /** La pelea de un guardian: de rodillas ve a alguien, se levanta y ruge; a media vida ruge otra vez y cambia. */
    private static String guardianPelea(com.darkcraft.enemigos.Guardian g, net.minecraft.server.level.ServerPlayer fp, int rugido) {
        float giro = g.getYRot();
        double fx = -Math.sin(Math.toRadians(giro)), fz = Math.cos(Math.toRadians(giro));
        fp.moveTo(g.getX() + fx * 7, g.getY(), g.getZ() + fz * 7, giro + 180f, 0f);
        int postura0 = g.postura();
        g.setTarget(fp);
        boolean pelea = g.enPelea(), dePie = false, ruge = false;
        int t = 0;
        for (; t < 160 && !ruge; t++) {
            g.tick();
            if (g.postura() == com.darkcraft.enemigos.Enemigo.DE_PIE) dePie = true;
            if (g.accion() == rugido) ruge = true;
        }
        int tRuge = t;
        for (int i = 0; i < 80 && g.accion() != 0; i++) g.tick();
        float max = g.getMaxHealth();
        g.setHealth(max * 0.45f);
        boolean segunda = false, rugeOtra = false;
        for (int i = 0; i < 240 && !rugeOtra; i++) {
            g.tick();
            if (g.segunda()) segunda = true;
            if (segunda && g.accion() == rugido) rugeOtra = true;
        }
        for (int i = 0; i < 80 && g.accion() != 0; i++) g.tick();
        return String.format(java.util.Locale.ROOT, "%s (%.0f de vida) de rodillas %s, al verle pelea %s, en pie %s, ruge %s (a los %d ticks), a media vida pasa a la segunda %s y ruge %s",
                g.tipo(), max, postura0 == com.darkcraft.enemigos.Enemigo.REZA ? "si" : "no (" + postura0 + ")", pelea, dePie, ruge, tRuge, segunda, rugeOtra);
    }

    /** Lo que le quita un golpe (y le devuelve la vida). */
    private static float pegar(net.minecraft.world.entity.LivingEntity e, net.minecraft.world.damagesource.DamageSource s, float d) {
        e.invulnerableTime = 0;
        float v0 = e.getHealth();
        e.hurt(s, d);
        float q = v0 - e.getHealth();
        e.setHealth(v0);
        e.invulnerableTime = 0;
        return q;
    }

    /** El paves de Ser Brandt (mirando al sur): de frente, por la espalda, una flecha, y en el muro (y lo que hace despues). */
    private static String paves(ServerLevel lvl, com.darkcraft.enemigos.Brandt b, net.minecraft.server.level.ServerPlayer fp) {
        b.setTarget(null);
        for (int i = 0; i < 120 && b.accion() != 0; i++) b.tick();
        double x = b.getX(), y = b.getY(), z = b.getZ();
        b.moveTo(x, y, z, 0f, 0f);
        b.setYRot(0f);
        b.yBodyRot = b.yHeadRot = 0f;
        b.setHealth(b.getMaxHealth());
        fp.moveTo(x, y, z + 2.5, 180f, 0f);
        float deFrente = pegar(b, lvl.damageSources().playerAttack(fp), 20f);
        fp.moveTo(x, y, z - 2.5, 0f, 0f);
        float porDetras = pegar(b, lvl.damageSources().playerAttack(fp), 20f);
        var flecha = new net.minecraft.world.entity.projectile.Arrow(lvl, x, y + 1.5, z + 3.0);
        flecha.setOwner(fp);
        float conFlecha = pegar(b, lvl.damageSources().arrow(flecha, fp), 20f);
        flecha.discard();
        // el muro: de frente no le entra nada y, al acabar, contesta
        fp.moveTo(x, y, z + 2.5, 180f, 0f);
        b.setTarget(fp);
        b.empezar(com.darkcraft.enemigos.BrandtAnims.MURO);
        for (int i = 0; i < 4; i++) b.tick();
        float enMuro = pegar(b, lvl.damageSources().playerAttack(fp), 20f);
        int despues = -1;
        for (int i = 0; i < 80 && despues < 0; i++) {
            b.tick();
            int a = b.accion();
            if (a != com.darkcraft.enemigos.BrandtAnims.MURO && a != 0) despues = a;
        }
        for (int i = 0; i < 60 && b.accion() != 0; i++) b.tick();
        return String.format(java.util.Locale.ROOT, "el paves: de frente quita %.1f, por la espalda %.1f, una flecha de frente %.1f (0), en el muro %.1f (0) y luego %s",
                deFrente, porDetras, conFlecha, enMuro, despues < 0 ? "nada" : com.darkcraft.enemigos.BrandtAnims.NOMBRES[despues] + (despues == com.darkcraft.enemigos.BrandtAnims.ESCUDAZO ? "" : " (tenia que ser escudazo)"));
    }

    /** Vareth: el fuego (el doble), y cuantas raices salen en linea y cuantas espinas (las entidades que se ven). */
    private static String corteza(ServerLevel lvl, com.darkcraft.enemigos.Corteza v, net.minecraft.server.level.ServerPlayer fp,
                                  java.util.List<net.minecraft.world.entity.Entity> quitar) {
        v.setTarget(null);
        for (int i = 0; i < 120 && v.accion() != 0; i++) v.tick();
        v.setHealth(v.getMaxHealth());
        float normal = pegar(v, lvl.damageSources().generic(), 10f);
        float fuego = pegar(v, lvl.damageSources().onFire(), 10f);
        v.clearFire();
        int raices = brotan(lvl, v, com.darkcraft.enemigos.CortezaAnims.GOLPE_SUELO, quitar);
        int espinas = brotan(lvl, v, com.darkcraft.enemigos.CortezaAnims.ESPINAS, quitar);
        int alrededor = brotan(lvl, v, com.darkcraft.enemigos.CortezaAnims.RAICES, quitar);
        return String.format(java.util.Locale.ROOT, "el fuego le quita %.1f y lo de siempre %.1f (el doble); salen %d raices en linea (hasta 9), %d espinas (9) y %d raices alrededor (hasta 22)",
                fuego, normal, raices, espinas, alrededor);
    }

    /** Hace esa accion y cuenta cuantas entidades de raiz o espina salen a su alrededor. */
    private static int brotan(ServerLevel lvl, com.darkcraft.enemigos.Enemigo e, int accion, java.util.List<net.minecraft.world.entity.Entity> quitar) {
        var caja = e.getBoundingBox().inflate(16, 6, 16);
        java.util.Set<net.minecraft.world.entity.Entity> antes = new java.util.HashSet<>(lvl.getEntitiesOfClass(net.minecraft.world.entity.Display.class, caja));
        java.util.Set<net.minecraft.world.entity.Entity> nuevas = new java.util.HashSet<>();
        e.empezar(accion);
        for (int i = 0; i < 100; i++) {
            e.tick();
            for (var d : lvl.getEntitiesOfClass(net.minecraft.world.entity.Display.class, caja)) if (!antes.contains(d)) nuevas.add(d);
            if (e.accion() != accion && i > 40) break;
        }
        quitar.addAll(nuevas);
        return nuevas.size();
    }

    private static void barcoQueVaYViene(net.minecraft.server.MinecraftServer server, ServerLevel lvl, java.io.PrintWriter log) {
        final int BV = com.darkcraft.pueblo.Amarres.BRUMAVELA, VD = com.darkcraft.pueblo.Amarres.VALDRENN;
        var d = com.darkcraft.pueblo.Pueblo.Datos.get(server);
        BlockPos.MutableBlockPos mb = new BlockPos.MutableBlockPos();
        com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> lvl.getBlockState(mb.set(x, y, z));
        // 1. el plano del barco roto es el barco que se genero, estado a estado (tambien como se unen sus vallas)
        StringBuilder dif = new StringBuilder();
        int malRoto = 0;
        for (var c : com.darkcraft.pueblo.Garza.plano(false)) {
            var hay = lvl.getBlockState(mb.set(c.x(), c.y(), c.z()));
            if (hay == c.estado() || (c.estado().isAir() && hay.isAir())) continue;
            if (malRoto++ < 5) dif.append(" [").append(c.x()).append(',').append(c.y()).append(',').append(c.z()).append(" plano ").append(c.estado()).append(" mundo ").append(hay).append(']');
        }
        // 2. reparado de una vez: como el plano entero, el juego no cambiaria ninguna union y no salta nada al suelo
        com.darkcraft.pueblo.Pueblo.repararYa(lvl);
        int malEntero = com.darkcraft.pueblo.Garza.distintas(lvl, true, 0, 0), cambiaria = 0;
        StringBuilder cam = new StringBuilder();
        for (var c : com.darkcraft.pueblo.Garza.plano(true)) {
            if (c.estado().isAir()) continue;
            var hay = lvl.getBlockState(mb.set(c.x(), c.y(), c.z()));
            var seria = net.minecraft.world.level.block.Block.updateFromNeighbourShapes(hay, lvl, mb);
            if (seria == hay) continue;
            if (cambiaria++ < 4) cam.append(" [").append(c.x()).append(',').append(c.y()).append(',').append(c.z()).append(' ').append(hay).append(" -> ").append(seria).append(']');
        }
        var caja = new net.minecraft.world.phys.AABB(com.darkcraft.pueblo.gen.Barco.BX - 24, 230, com.darkcraft.pueblo.gen.Barco.POPA - 24,
                com.darkcraft.pueblo.gen.Barco.BX + 24, 290, com.darkcraft.pueblo.gen.Barco.PROA + 28);
        int sueltos = lvl.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, caja).size();
        log.println("La Garza Gris, el plano: roto " + com.darkcraft.pueblo.Garza.plano(false).size() + " celdas, distintas del barco generado " + malRoto
                + " (tiene que ser 0)" + dif + "; entero " + com.darkcraft.pueblo.Garza.plano(true).size() + " celdas, distintas tras reparar " + malEntero
                + " (0), uniones que el juego cambiaria " + cambiaria + " (0)" + cam + ", objetos sueltos junto al barco " + sueltos + " (0)");
        // la reparacion por etapas: cuantos bloques y cuando
        int pasos = 0;
        float t0 = 99, t1 = 0;
        int[] fases = new int[6];
        java.util.Map<BlockPos, net.minecraft.world.level.block.state.BlockState> roto = new java.util.HashMap<>();
        for (var c : com.darkcraft.pueblo.Garza.plano(false)) roto.put(new BlockPos(c.x(), c.y(), c.z()), c.estado());
        for (var c : com.darkcraft.pueblo.Garza.plano(true)) {
            if (roto.get(new BlockPos(c.x(), c.y(), c.z())) == c.estado()) continue;
            float cu = com.darkcraft.cine.Reparacion.cuando(c.x(), c.y(), c.z(), c.estado());
            pasos++;
            t0 = Math.min(t0, cu);
            t1 = Math.max(t1, cu);
            fases[cu < com.darkcraft.cine.Reparacion.CASCO0 ? 0 : cu < com.darkcraft.cine.Reparacion.PALO0 ? 1 : cu < com.darkcraft.cine.Reparacion.VERGAS ? 2
                    : cu < com.darkcraft.cine.Reparacion.VELAS0 ? 3 : cu < com.darkcraft.cine.Reparacion.PIEZAS ? 4 : 5]++;
        }
        log.println(String.format(java.util.Locale.ROOT, "La Garza Gris, la reparacion por etapas: %d bloques entre %.1f y %.1f s (se va lo roto %d, casco %d, palo %d, vergas %d, "
                + "velas %d, piezas %d); el destello a los %.1f s", pasos, t0, t1, fases[0], fases[1], fases[2], fases[3], fases[4], fases[5], com.darkcraft.cine.Reparacion.DESTELLO));
        // 3. el muelle de Valdrenn
        int[] o = com.darkcraft.pueblo.Amarres.de(lvl, VD);
        if (o == null) {
            log.println("La Garza Gris: SIN RUMBO a Valdrenn (no hay reino cerca del origen)");
            com.darkcraft.pueblo.Pueblo.reiniciar(lvl);
            return;
        }
        int cx = com.darkcraft.pueblo.Amarres.CALMA_X + o[0], cz = com.darkcraft.pueblo.Amarres.CALMA_Z + o[1], R = com.darkcraft.pueblo.Amarres.CALMA_R1 + 2;
        // (lo que habia, para dejarlo igual al acabar)
        java.util.Map<Long, net.minecraft.world.level.block.state.BlockState> antes = new java.util.HashMap<>();
        int natMin = 999, natMax = -999;
        for (int x = cx - R; x <= cx + R; x++)
            for (int z = cz - R; z <= cz + R; z++) {
                for (int y = 222; y <= 270; y++) antes.put(BlockPos.asLong(x, y, z), lvl.getBlockState(mb.set(x, y, z)));
                if (Math.hypot(x - cx, z - cz) <= com.darkcraft.pueblo.Amarres.CALMA_R0) {
                    int n = nubeArriba(lvl, mb, x, z);
                    if (n >= 0) {
                        natMin = Math.min(natMin, n);
                        natMax = Math.max(natMax, n);
                    }
                }
            }
        long ms = System.currentTimeMillis();
        boolean hecho = com.darkcraft.pueblo.Amarres.hacerYa(lvl);
        ms = System.currentTimeMillis() - ms;
        int calMin = 999, calMax = -999, escalon = 0;
        for (int x = cx - R; x <= cx + R; x++)
            for (int z = cz - R; z <= cz + R; z++) {
                int n = nubeArriba(lvl, mb, x, z);
                if (n < 0) continue;
                if (Math.hypot(x - cx, z - cz) <= com.darkcraft.pueblo.Amarres.CALMA_R0 - 1) {
                    calMin = Math.min(calMin, n);
                    calMax = Math.max(calMax, n);
                }
                // el mayor escalon entre una columna de nube y la de al lado (que no quede un muro de nube)
                int n2 = nubeArriba(lvl, mb, x + 1, z);
                if (n2 >= 0) escalon = Math.max(escalon, Math.abs(n - n2));
            }
        var seg = com.darkcraft.pueblo.Amarres.seguro(lvl, VD);
        BlockPos camp = com.darkcraft.pueblo.Amarres.campana(lvl, VD);
        BlockPos sant = new BlockPos(com.darkcraft.pueblo.gen.MuelleValdrenn.SANTUARIO_X + o[0], com.darkcraft.pueblo.gen.MuelleValdrenn.Y + 1, com.darkcraft.pueblo.gen.MuelleValdrenn.SANTUARIO_Z + o[1]);
        log.println("Muelle de Valdrenn: hecho=" + hecho + " en " + ms + " ms, amarre a " + o[0] + ", " + o[1] + " del de Brumavela; el mar de nubes en el radio llano antes "
                + natMin + ".." + natMax + ", ahora " + calMin + ".." + calMax + " (245..247), mayor escalon de nube " + escalon + "; bajo el sitio seguro "
                + lvl.getBlockState(BlockPos.containing(seg[0], seg[1] - 0.5, seg[2])).getBlock().getDescriptionId() + ", la campana "
                + lvl.getBlockState(camp).getBlock().getDescriptionId() + ", el santuario " + lvl.getBlockState(sant).getBlock().getDescriptionId() + " ("
                + com.darkcraft.santuario.Santuarios.nombre(lvl, sant) + "), el camino junto al anden "
                + lvl.getBlockState(new BlockPos(2 + o[0], 250, 140 + o[1])).getBlock().getDescriptionId());
        // 4. el barco, de Brumavela a Valdrenn con lo que guarda un barril, y que se recorre todo a pie desde el camino
        BlockPos barril = null;
        for (var c : com.darkcraft.pueblo.Garza.plano(true)) if (barril == null && c.estado().is(net.minecraft.world.level.block.Blocks.BARREL)) barril = new BlockPos(c.x(), c.y(), c.z());
        if (barril != null && lvl.getBlockEntity(barril) instanceof net.minecraft.world.Container cont)
            cont.setItem(4, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND, 7));
        com.darkcraft.pueblo.Travesia.mover(lvl, VD);
        int enV = com.darkcraft.pueblo.Garza.distintas(lvl, true, o[0], o[1]), restoBV = 0, nubeDentro = 0;
        for (var c : com.darkcraft.pueblo.Garza.plano(true)) {
            var hay = lvl.getBlockState(mb.set(c.x(), c.y(), c.z()));
            if (!hay.isAir() && !(hay.getBlock() instanceof com.darkcraft.world.NubeBlock)) restoBV++;
            if (com.darkcraft.pueblo.gen.Barco.dentro(c.x(), c.y(), c.z())
                    && lvl.getBlockState(mb.set(c.x() + o[0], c.y(), c.z() + o[1])).getBlock() instanceof com.darkcraft.world.NubeBlock) nubeDentro++;
        }
        String carga = "-";
        if (barril != null && lvl.getBlockEntity(barril.offset(o[0], 0, o[1])) instanceof net.minecraft.world.Container cont) carga = cont.getItem(4).toString();
        int sueltos2 = lvl.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, caja).size()
                + lvl.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, caja.move(o[0], 0, o[1])).size();
        BlockPos lle = com.darkcraft.world.MundoNubes.llegada(lvl.getSeed());
        var zona = com.darkcraft.pueblo.gen.Andar.desde(mundo, lle.getX(), lle.getY(), lle.getZ(), o[0] - 24, o[1] + 100, o[0] + 10, o[1] + 166, 80000);
        int[] tim = com.darkcraft.pueblo.gen.PuebloPlan.get().sitio("barco_timon"), cor = com.darkcraft.pueblo.gen.PuebloPlan.get().sitio("barco_corazon");
        int alcazar = 0;
        for (int n = 0; n < 8; n++) {
            double[] al = com.darkcraft.pueblo.Amarres.alcazar(lvl, VD, n);
            if (zona.pisa((int) Math.floor(al[0]), (int) Math.round(al[1]) - 1, (int) Math.floor(al[2]))) alcazar++;
        }
        log.println("La Garza Gris en Valdrenn: distintas del plano " + enV + " (0), en Brumavela queda del barco " + restoBV + " (0), nube dentro del casco " + nubeDentro
                + " (0), el barril lleva " + carga + " (7 diamantes), objetos sueltos " + sueltos2 + " (0) | " + com.darkcraft.pueblo.Pueblo.estado(d)
                + " | a pie desde el camino (" + zona.celdas() + " celdas): el anden " + zona.pisa((int) Math.floor(seg[0]), (int) seg[1] - 1, (int) Math.floor(seg[2]))
                + ", la campana " + zona.alcanza(camp.getX(), camp.getY(), camp.getZ(), 2.6) + ", el santuario " + zona.alcanza(sant.getX(), sant.getY(), sant.getZ(), 2.6)
                + ", el timon " + zona.alcanza(tim[0] + o[0], tim[1], tim[2] + o[1], 2.6) + ", la bodega " + zona.alcanza(cor[0] + o[0], cor[1], cor[2] + o[1], 2.6)
                + ", sitios del alcazar " + alcazar + " de 8 | cosas " + com.darkcraft.pueblo.Pueblo.cosas(lvl).size());
        // y de vuelta
        com.darkcraft.pueblo.Travesia.mover(lvl, BV);
        int enBV = com.darkcraft.pueblo.Garza.distintas(lvl, true, 0, 0), restoVD = 0;
        for (var c : com.darkcraft.pueblo.Garza.plano(true)) {
            var hay = lvl.getBlockState(mb.set(c.x() + o[0], c.y(), c.z() + o[1]));
            if (!hay.isAir() && !(hay.getBlock() instanceof com.darkcraft.world.NubeBlock)) restoVD++;
        }
        String carga2 = "-";
        if (barril != null && lvl.getBlockEntity(barril) instanceof net.minecraft.world.Container cont) {
            carga2 = cont.getItem(4).toString();
            cont.setItem(4, net.minecraft.world.item.ItemStack.EMPTY);
        }
        log.println("La Garza Gris de vuelta en Brumavela: distintas del plano " + enBV + " (0), en Valdrenn queda del barco " + restoVD + " (0), el barril lleva " + carga2
                + " | " + com.darkcraft.pueblo.Pueblo.estado(d));
        // 5. las cinematicas: con el barco fuera de los dos muelles, ni la camara ni el casco se meten en nada
        com.darkcraft.pueblo.Garza.dejar(lvl, -1, 0, 0, com.darkcraft.pueblo.Amarres.cima(lvl, BV));
        int[] cero = {0, 0};
        Object[][] viajes = {{"primer viaje a Valdrenn", com.darkcraft.cine.Viaje.PRIMERO, cero, o, VD}, {"viaje a Valdrenn", com.darkcraft.cine.Viaje.NORMAL, cero, o, VD},
                {"viaje a Brumavela", com.darkcraft.cine.Viaje.NORMAL, o, cero, BV}, {"llamada desde Valdrenn", com.darkcraft.cine.Viaje.LLAMADA, cero, o, VD},
                {"llamada desde Brumavela", com.darkcraft.cine.Viaje.LLAMADA, o, cero, BV}};
        double[][] casco = {{0, -4.6, -13}, {0, -4.6, -8}, {0, -4.6, -3}, {0, -4.6, 2}, {0, -4.6, 7}, {0, -4.6, 12}, {-4, -0.6, 0}, {4, -0.6, 0}, {0, 2.4, 20.5}, {0, 3.4, -13.5},
                {-6, 13.4, 0}, {6, 13.4, 0}, {0, 23.4, 0}};
        for (Object[] v : viajes) {
            int tipo = (Integer) v[1], a = (Integer) v[4];
            int[] de = (int[]) v[2], hacia = (int[]) v[3];
            var T = com.darkcraft.cine.Viaje.tiempos(tipo);
            double[] al = com.darkcraft.pueblo.Amarres.alcazar(lvl, a, 0), ojos = {al[0], al[1] + 1.62, al[2]};
            double[] vi = com.darkcraft.pueblo.Amarres.vista(a), vista = {vi[0] + hacia[0], vi[1], vi[2] + hacia[1]};
            var planos = com.darkcraft.cine.Viaje.planos(tipo, de, hacia, ojos, vista);
            StringBuilder mal = new StringBuilder();
            int malas = 0, muestras = 0;
            for (int k = 0; k < planos.size(); k++) {
                var sh = planos.get(k);
                for (int i = 0; i <= 24; i++) {
                    double t = sh.t0 + (sh.t1 - sh.t0) * i / 24.0;
                    // (el ultimo plano de un viaje empieza en negro, con el barco ya de bloques: se mira aparte)
                    double[] p = sh.at(t);
                    muestras++;
                    var st = lvl.getBlockState(BlockPos.containing(p[0], p[1], p[2]));
                    if (st.isAir()) continue;
                    if (malas++ < 4) mal.append(String.format(java.util.Locale.ROOT, " [plano %d a los %.1f s en %.1f %.1f %.1f: %s]", k + 1, t, p[0], p[1], p[2], st.getBlock().getDescriptionId()));
                }
            }
            // el vuelo: cada cuarto de segundo, unos cuantos puntos del casco y del aparejo
            int choques = 0, pasosVuelo = 0;
            double altoMax = 0, lejosMax = 0;
            StringBuilder ch = new StringBuilder();
            for (double t = 0; t < T.total(); t += 0.25) {
                double[] b = com.darkcraft.cine.Viaje.barco(T, t, de, hacia);
                if (b == null) continue;
                pasosVuelo++;
                altoMax = Math.max(altoMax, b[1] - com.darkcraft.cine.Viaje.PY);
                lejosMax = Math.max(lejosMax, Math.max(Math.hypot(b[0] - com.darkcraft.cine.Viaje.PX - de[0], b[2] - com.darkcraft.cine.Viaje.PZ - de[1]) < Math.hypot(b[0]
                        - com.darkcraft.cine.Viaje.PX - hacia[0], b[2] - com.darkcraft.cine.Viaje.PZ - hacia[1]) ? Math.hypot(b[0] - com.darkcraft.cine.Viaje.PX - de[0], b[2]
                        - com.darkcraft.cine.Viaje.PZ - de[1]) : Math.hypot(b[0] - com.darkcraft.cine.Viaje.PX - hacia[0], b[2] - com.darkcraft.cine.Viaje.PZ - hacia[1]), 0));
                double g = Math.toRadians(b[3]), cg = Math.cos(g), sg = Math.sin(g);
                for (double[] q : casco) {
                    // (giro 0 = proa al sur; el giro de Minecraft: x' = x cos - z sin, z' = x sin + z cos)
                    double x = b[0] + q[0] * cg - q[2] * sg, y = b[1] + q[1], z = b[2] + q[0] * sg + q[2] * cg;
                    var st = lvl.getBlockState(BlockPos.containing(x, y, z));
                    if (st.isAir() || st.getBlock() instanceof com.darkcraft.world.NubeBlock) continue;
                    if (choques++ < 4) ch.append(String.format(java.util.Locale.ROOT, " [a los %.2f s en %.1f %.1f %.1f: %s]", t, x, y, z, st.getBlock().getDescriptionId()));
                }
            }
            log.println(String.format(java.util.Locale.ROOT, "Cinematica %d (%s): %.1f s, %d planos, camara dentro de algo %d de %d (0)%s; el barco vuela %.1f s, sube hasta %.1f, "
                            + "se aleja de su amarre hasta %.0f, choca %d veces (0)%s; cambio %.2f, salto %.2f, toca %.2f, vuelve a ser de bloques %.2f, frases %d",
                    tipo, v[0], T.total(), planos.size(), malas, muestras, mal, pasosVuelo * 0.25, altoMax, lejosMax, choques, ch, T.cambioA, T.salto, T.toca(), T.cambioC, T.voz.length));
        }
        // la reparacion: sus tres planos, con el barco roto en su sitio
        com.darkcraft.pueblo.Pueblo.reiniciar(lvl);
        int malasR = 0;
        StringBuilder malR = new StringBuilder();
        var planosR = com.darkcraft.cine.Reparacion.planos();
        for (int k = 0; k < planosR.size(); k++)
            for (int i = 0; i <= 24; i++) {
                var sh = planosR.get(k);
                double[] p = sh.at(sh.t0 + (sh.t1 - sh.t0) * i / 24.0);
                var st = lvl.getBlockState(BlockPos.containing(p[0], p[1], p[2]));
                if (st.isAir()) continue;
                if (malasR++ < 4) malR.append(String.format(java.util.Locale.ROOT, " [plano %d en %.1f %.1f %.1f: %s]", k + 1, p[0], p[1], p[2], st.getBlock().getDescriptionId()));
            }
        // 6. todo como estaba: el barco roto en Brumavela y, en Valdrenn, ni muelle ni mar en calma (se haran solos en la prueba del cliente)
        int devueltos = 0;
        for (var e : antes.entrySet()) {
            mb.set(e.getKey());
            if (lvl.getBlockState(mb) == e.getValue()) continue;
            lvl.setBlock(mb, e.getValue(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS | net.minecraft.world.level.block.Block.UPDATE_KNOWN_SHAPE);
            devueltos++;
        }
        d.muelle = false;
        d.cambio();
        com.darkcraft.pueblo.Amarres.olvidar();
        log.println("La reparacion: " + planosR.size() + " planos, camara dentro de algo " + malasR + " (0)" + malR + " | al acabar: " + com.darkcraft.pueblo.Pueblo.estado(d)
                + ", barco roto distinto del plano " + com.darkcraft.pueblo.Garza.distintas(lvl, false, 0, 0) + " (0), bloques devueltos en Valdrenn " + devueltos
                + ", la campana " + lvl.getBlockState(camp).getBlock().getDescriptionId() + ", objetos sueltos "
                + lvl.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, caja).size());
    }
}
