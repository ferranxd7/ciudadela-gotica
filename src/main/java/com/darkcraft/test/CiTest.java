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
                    jinete(server, lvl, log, s);
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
        velmor(server, m, log, out);
        molinera(server, m, log, out);
        arboleda(server, m, log, out);
        verdugo(server, m, log);
        pilotes(server, m, log, out);
        barquero(server, m, log, out);
        monasterio(server, m, log, out);
        ermita(server, m, log, out);
        capital(server, m, log, out);
        juramento(server, m, log, out);
        academia(server, m, log, out);
        observatorio(server, m, log, out);
        puerto(server, m, log, out);
        costillar(server, m, log, out);
        grieta(server, m, log, out);
        altar(server, m, log, out);
        cerco(server, m, log, out);
        aldric(server, m, log, out);
        gigantes(server, m, log, out);
        teatro(server, m, log, out);
        bufon(server, m, log, out);
        nido(server, m, log, out);
        dragon(server, m, log, out);
        cadena(server, m, log, out);
        gargolas(server, m, log, out);
        homenaje(server, m, log, out);
        centinelas(server, m, log, out);
        isolde(server, m, log, out);
    }

    /**
     * Parte 7m: la Puerta Oeste de la Capital (la muralla, la puerta con su rastrillo, la sala de la guardia con el libro 22,
     * la calle mayor, la plaza de la estatua caida y el cuartel; a pie desde el camino del santuario a todos sus sitios) y los
     * calcinados: donde esperan (en su suelo, ninguno metido en un bloque), el agazapado se levanta y el enterrado se alza de
     * la ceniza, cada accion acaba, el escudo de la guardia (de frente deja pasar poco y las flechas nada; por la espalda,
     * todo), el tajo que quema, el cantaro de brea (revienta y deja llamas que queman), el agarron del ceniciento (sujeta y
     * quema), su nube de ceniza al morir, y el fuego no les hace nada y el frio, mas.
     */
    private static void capital(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.CAPITAL;
        var C = com.darkcraft.morvain.Capital.get();
        final int CX = com.darkcraft.morvain.Capital.CX, CZ = com.darkcraft.morvain.Capital.CZ, PISO = com.darkcraft.morvain.Capital.PISO;
        try {
            long t0 = System.currentTimeMillis();
            int nch = 0;
            for (long k : C.chunks()) {
                m.getChunk((int) (k >> 32), (int) k);
                nch++;
            }
            long ms = System.currentTimeMillis() - t0;
            // a pie desde el camino del santuario (las alfombras de ceniza no estorban)
            var aire = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
                var st = m.getBlockState(new BlockPos(x, y, z));
                return st.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock ? aire : st;
            };
            int[] en = C.sitio("entrada");
            var zona = com.darkcraft.pueblo.gen.Andar.desde(mundo, en[0], en[1], en[2], CX - 70, CZ - 80, CX + 124, CZ + 80, 3_000_000);
            StringBuilder ap = new StringBuilder();
            int llega = 0, sitios = 0;
            for (var e : C.sitios().entrySet()) {
                // (la estatua es lo alto del pedestal: no se sube)
                if (e.getKey().equals("estatua")) continue;
                sitios++;
                int[] q = e.getValue();
                if (zona.alcanza(q[0], q[1], q[2], 2)) llega++;
                else ap.append(' ').append(e.getKey());
            }
            int lejos = 0;
            for (var o : C.esperas()) if (!zona.alcanza(o.x(), o.y(), o.z(), 2)) lejos++;
            var sc = com.darkcraft.morvain.SantuariosMorvain.porId("puerta_capital");
            log.println("Morvain, la puerta oeste de la capital: " + nch + " chunks en " + ms + " ms; " + com.darkcraft.morvain.Capital.resumen(m)
                    + "; santuario " + com.darkcraft.santuario.Santuarios.nombre(m, sc.pos()) + "; a pie desde el camino (" + zona.celdas() + " celdas) a "
                    + llega + " de " + sitios + " sitios" + (ap.length() == 0 ? "" : ", no a:" + ap) + "; calcinados a los que no se llega " + lejos
                    + " de " + C.esperas().size());
            topView(m, CX + 40, CZ, 90, new File(out, "morvain_capital.png"));
            topView(m, CX, CZ, 16, new File(out, "morvain_capital_puerta.png"));
        } catch (Throwable t) {
            log.println("Morvain, la puerta oeste de la capital: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] capital", t);
        }
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        try {
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            for (int k = 0; k < 10; k++)
                for (var e : fuera) if (e.isAlive()) e.tick();
            int agazapados = 0, enterrados = 0, dePie = 0, enSuelo = 0, chocan = 0;
            int[] porVariante = new int[3];
            StringBuilder mal = new StringBuilder();
            com.darkcraft.enemigos.Calcinado agazapado = null, enterrado = null;
            for (var e : fuera) {
                if (!(e instanceof com.darkcraft.enemigos.Calcinado c)) continue;
                porVariante[Math.max(0, Math.min(2, c.variante()))]++;
                if (!m.noCollision(c)) {
                    chocan++;
                    if (mal.length() < 300) mal.append(" metido en un bloque en ").append(c.blockPosition().toShortString()).append(';');
                }
                if (c.postura() == com.darkcraft.enemigos.Enemigo.REZA) {
                    agazapados++;
                    if (agazapado == null) agazapado = c;
                } else if (c.postura() == com.darkcraft.enemigos.Enemigo.TUMBADO) {
                    enterrados++;
                    if (enterrado == null) enterrado = c;
                } else dePie++;
                if (!m.getBlockState(BlockPos.containing(c.getX(), c.getY() - 0.2, c.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty()) enSuelo++;
                else if (mal.length() < 400) mal.append(' ').append(c.tipo()).append(" en el aire en ").append(c.blockPosition().toShortString()).append(';');
            }
            log.println("Morvain, los calcinados: " + com.darkcraft.morvain.MorvainEnemigos.comprobar() + "; fuera " + fuera.size() + " de "
                    + com.darkcraft.morvain.MorvainEnemigos.capital().size() + " (guardias " + porVariante[0] + ", incendiarios " + porVariante[1]
                    + ", cenicientos " + porVariante[2] + "): agazapados " + agazapados + ", enterrados " + enterrados + ", de pie " + dePie
                    + " (en su suelo " + enSuelo + "); metidos en un bloque " + chocan + " (tiene que ser 0)" + mal);
            StringBuilder sb = new StringBuilder("Morvain, los calcinados en accion:");
            // ---- el agazapado se levanta y el enterrado se alza de la ceniza (una presa a su lado)
            int alzados = com.darkcraft.enemigos.Calcinado.ALZADOS;
            for (var c : new com.darkcraft.enemigos.Calcinado[]{agazapado, enterrado}) {
                if (c == null) {
                    sb.append(" NO HAY ").append(c == agazapado ? "NINGUNO AGAZAPADO;" : "NINGUNO ENTERRADO;");
                    continue;
                }
                var presa = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
                if (presa == null) continue;
                presa.moveTo(c.getX() + 2.0, c.getY(), c.getZ() + 0.5, 0f, 0f);
                presa.setNoAi(true);
                m.addFreshEntity(presa);
                quitar.add(presa);
                int antes = c.postura();
                c.setTarget(presa);
                int sale = -1;
                for (int k = 0; k < 80; k++) {
                    c.tickCount++;
                    c.tick();
                    int a = c.accion();
                    if (sale < 0 && (a == com.darkcraft.enemigos.CalcinadoAnims.LEVANTARSE || a == com.darkcraft.enemigos.CalcinadoAnims.ALZARSE)) sale = k;
                    if (sale >= 0 && a == 0) break;
                }
                sb.append(String.format(java.util.Locale.ROOT, " el %s (%s, en %s) se %s en el tick %d y queda de pie %s;", c.tipo(),
                        antes == com.darkcraft.enemigos.Enemigo.REZA ? "agazapado" : "enterrado", c.blockPosition().toShortString(),
                        antes == com.darkcraft.enemigos.Enemigo.REZA ? "levanta" : "alza", sale, c.postura() == com.darkcraft.enemigos.Enemigo.DE_PIE));
                c.setTarget(null);
            }
            sb.append(" alzados +").append(com.darkcraft.enemigos.Calcinado.ALZADOS - alzados).append(';');
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio)) e.discard();
            // ---- en la calle mayor (de cara al oeste, a la puerta): uno de cada, cada accion acaba
            for (int v2 = 0; v2 < 3; v2++) {
                var a = DarkCraftMod.CALCINADO.get().create(m);
                if (a == null) continue;
                a.variante(v2);
                a.moveTo(CX + 16.5 + v2 * 4, PISO, CZ + 0.5, 90f, 0f);
                m.addFreshEntity(a);
                quitar.add(a);
                StringBuilder no = new StringBuilder();
                for (int ac = 1; ac < com.darkcraft.enemigos.CalcinadoAnims.TICKS.length; ac++) {
                    String nombre = com.darkcraft.enemigos.CalcinadoAnims.NOMBRES[ac];
                    if (nombre.equals("morir")) continue;
                    a.empezar(ac);
                    for (int k = 0; k < 90 && a.accion() == ac; k++) {
                        a.tickCount++;
                        a.tick();
                    }
                    if (a.accion() == ac) no.append(' ').append(nombre);
                    for (int k = 0; k < 10; k++) a.tick();
                }
                sb.append(' ').append(a.tipo()).append(String.format(java.util.Locale.ROOT, " (vida %.0f, armadura %d)", a.getMaxHealth(), a.getArmorValue()))
                        .append(": acciones que no acaban").append(no.length() == 0 ? " ninguna" : no).append(';');
                for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Cantaro.class, a.getBoundingBox().inflate(20))) f.discard();
                a.discard();
            }
            log.println(sb);
            sb = new StringBuilder("Morvain, los calcinados contra una presa:");
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("3e5f7a9b-1c2d-4e6f-8a0b-2c4d6e8f0a72"), "[DarkCraftCI7]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            final double gx = CX + 20.5, gz = CZ + 0.5;
            // ---- el escudo de la guardia: de frente (al oeste) deja pasar el 20 %, la flecha nada; por la espalda, todo
            var g = DarkCraftMod.CALCINADO.get().create(m);
            if (g != null) {
                g.variante(com.darkcraft.enemigos.Calcinado.GUARDIA);
                g.moveTo(gx, PISO, gz, 90f, 0f);
                g.yBodyRot = g.yHeadRot = 90f;
                g.setNoAi(true);
                m.addFreshEntity(g);
                quitar.add(g);
                int bloq = com.darkcraft.enemigos.Calcinado.BLOQUEOS;
                fp.moveTo(gx - 2.0, PISO, gz, -90f, 0f);
                float v0 = g.getHealth();
                g.hurt(m.damageSources().playerAttack(fp), 10f);
                float deFrente = v0 - g.getHealth();
                g.invulnerableTime = 0;
                var flecha = new net.minecraft.world.entity.projectile.Arrow(m, fp);
                flecha.setPos(gx - 1.6, PISO + 1.2, gz);
                v0 = g.getHealth();
                boolean entra = g.hurt(m.damageSources().arrow(flecha, fp), 6f);
                float conFlecha = v0 - g.getHealth();
                g.invulnerableTime = 0;
                fp.moveTo(gx + 2.0, PISO, gz, 90f, 0f);
                v0 = g.getHealth();
                g.hurt(m.damageSources().playerAttack(fp), 10f);
                float porDetras = v0 - g.getHealth();
                sb.append(String.format(java.util.Locale.ROOT, " el escudo: de frente -%.1f de 10, la flecha %s (-%.1f), por la espalda -%.1f; bloqueos +%d;",
                        deFrente, entra ? "entra" : "no entra", conFlecha, porDetras, com.darkcraft.enemigos.Calcinado.BLOQUEOS - bloq));
                g.discard();
            }
            // ---- el tajo (quema), el cantaro de brea (revienta y deja llamas que queman) y el agarron (sujeta y quema)
            int cant = com.darkcraft.enemigos.Calcinado.CANTAROS, rev = com.darkcraft.enemigos.Cantaro.REVIENTAN, dan = com.darkcraft.enemigos.Cantaro.DAN,
                    queman = com.darkcraft.enemigos.Cantaro.LLAMAS_QUEMAN, agarra = com.darkcraft.enemigos.Calcinado.AGARRONES,
                    quem = com.darkcraft.enemigos.Calcinado.QUEMADOS;
            String[] nombres = {"tajo", "cantaro", "agarron"};
            int[] acciones = {com.darkcraft.enemigos.CalcinadoAnims.TAJO, com.darkcraft.enemigos.CalcinadoAnims.LANZAR, com.darkcraft.enemigos.CalcinadoAnims.AGARRON};
            int[] variantes = {com.darkcraft.enemigos.Calcinado.GUARDIA, com.darkcraft.enemigos.Calcinado.INCENDIARIO, com.darkcraft.enemigos.Calcinado.CENICIENTO};
            double[] lejos = {2.4, 8.0, 3.4};
            int[] dura = {40, 110, 40};
            for (int i = 0; i < 3; i++) {
                var a = DarkCraftMod.CALCINADO.get().create(m);
                var presa = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
                if (a == null || presa == null) continue;
                a.variante(variantes[i]);
                // en la calle mayor, mirando al este (hacia la plaza)
                a.moveTo(gx, PISO, gz, -90f, 0f);
                presa.moveTo(gx + lejos[i], PISO, gz, 90f, 0f);
                presa.setNoAi(true);
                m.addFreshEntity(a);
                m.addFreshEntity(presa);
                quitar.add(a);
                quitar.add(presa);
                a.setTarget(presa);
                a.setYRot(-90f);
                a.yBodyRot = a.yHeadRot = -90f;
                a.empezar(acciones[i]);
                float vida = presa.getHealth();
                int fuego = 0;
                boolean frenada = false, llamas = false;
                for (int k = 0; k < dura[i]; k++) {
                    if (!a.isRemoved()) {
                        a.tickCount++;
                        a.tick();
                    }
                    for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Cantaro.class, presa.getBoundingBox().inflate(14))) {
                        if (!quitar.contains(f)) quitar.add(f);
                        f.tick();
                    }
                    for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Cantaro.Llamas.class, presa.getBoundingBox().inflate(14))) {
                        if (!quitar.contains(f)) quitar.add(f);
                        llamas = true;
                        f.tick();
                    }
                    // (la presa no se mueve: que no se apague sola el fuego entre golpe y golpe)
                    fuego = Math.max(fuego, presa.getRemainingFireTicks());
                    frenada |= presa.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
                    presa.invulnerableTime = 0;
                }
                sb.append(String.format(java.util.Locale.ROOT, " %s a %.1f: la presa -%.1f, ardiendo %d ticks, frenada %s%s;", nombres[i], lejos[i],
                        vida - presa.getHealth(), fuego, frenada, i == 1 ? ", llamas en el suelo " + llamas : ""));
                a.discard();
                presa.discard();
            }
            sb.append(String.format(java.util.Locale.ROOT, " cantaros +%d (revientan +%d, dan +%d, sus llamas queman +%d), agarrones +%d, quemados +%d;",
                    com.darkcraft.enemigos.Calcinado.CANTAROS - cant, com.darkcraft.enemigos.Cantaro.REVIENTAN - rev, com.darkcraft.enemigos.Cantaro.DAN - dan,
                    com.darkcraft.enemigos.Cantaro.LLAMAS_QUEMAN - queman, com.darkcraft.enemigos.Calcinado.AGARRONES - agarra,
                    com.darkcraft.enemigos.Calcinado.QUEMADOS - quem));
            // ---- el fuego no les hace nada; el frio, un 60 % mas
            var c1 = DarkCraftMod.CALCINADO.get().create(m);
            var c2 = DarkCraftMod.CALCINADO.get().create(m);
            if (c1 != null && c2 != null) {
                c1.variante(com.darkcraft.enemigos.Calcinado.CENICIENTO);
                c2.variante(com.darkcraft.enemigos.Calcinado.CENICIENTO);
                c1.moveTo(gx - 6, PISO, gz + 1.5, 0f, 0f);
                c2.moveTo(gx - 6, PISO, gz - 1.5, 0f, 0f);
                m.addFreshEntity(c1);
                m.addFreshEntity(c2);
                quitar.add(c1);
                quitar.add(c2);
                float v1 = c1.getHealth(), v2 = c2.getHealth();
                boolean fuegoLe = c1.hurt(m.damageSources().inFire(), 5f);
                boolean lavaLe = c1.hurt(m.damageSources().lava(), 5f);
                c2.hurt(m.damageSources().freeze(), 5f);
                sb.append(String.format(java.util.Locale.ROOT, " el fuego le hace %s y la lava %s (-%.1f); el frio -%.1f de 5;", fuegoLe ? "dano" : "nada",
                        lavaLe ? "dano" : "nada", v1 - c1.getHealth(), v2 - c2.getHealth()));
                // ---- y al morir el ceniciento revienta en una nube de ceniza
                int cen = com.darkcraft.enemigos.Calcinado.CENIZAS;
                c2.hurt(m.damageSources().playerAttack(fp), 9999f);
                int k = 0;
                boolean nube = false;
                for (; k < 90 && !c2.isRemoved(); k++) {
                    c2.tick();
                    for (var n : m.getEntitiesOfClass(net.minecraft.world.entity.AreaEffectCloud.class, c2.getBoundingBox().inflate(4)))
                        if (n.getOwner() == c2) {
                            nube = true;
                            if (!quitar.contains(n)) quitar.add(n);
                        }
                }
                sb.append(String.format(java.util.Locale.ROOT, " al morir el ceniciento revienta en ceniza %s (nube %s) y se va en %d ticks",
                        com.darkcraft.enemigos.Calcinado.CENIZAS > cen, nube, k));
            }
            log.println(sb);
        } catch (Throwable t) {
            log.println("Morvain, los calcinados: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] calcinados", t);
        } finally {
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    /**
     * Parte 7o: la Academia Espectral (el recinto con su puerta, el claustro con la fuente de amatista, la Gran Biblioteca con
     * el libro 24 en el estrado del grimorio, las alas y la torre del archivo; a pie desde el camino del santuario a todos sus
     * sitios y a cada erudito) y los eruditos: donde esperan (en su suelo, ninguno metido en un bloque), el que lee se alza y el
     * tumbado se levanta, cada accion acaba, el plumazo, el tintero (revienta), el orbe (sale, sigue a su presa y le da), las
     * runas (se encienden y revientan), el parpadeo (se aparta lejos), el tomazo (su onda) y el cadenazo (llega lejos y frena).
     */
    private static void academia(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.ACADEMIA;
        var A = com.darkcraft.morvain.Academia.get();
        final int CX = com.darkcraft.morvain.Academia.CX, CZ = com.darkcraft.morvain.Academia.CZ, PISO = com.darkcraft.morvain.Academia.PISO;
        try {
            long t0 = System.currentTimeMillis();
            int nch = 0;
            for (long k : A.chunks()) {
                m.getChunk((int) (k >> 32), (int) k);
                nch++;
            }
            long ms = System.currentTimeMillis() - t0;
            // a pie desde el camino del santuario (las alfombras —los papeles— no estorban); el altillo y el archivo, por sus escalas
            var aire = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
                var st = m.getBlockState(new BlockPos(x, y, z));
                return st.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock ? aire : st;
            };
            int[] en = A.sitio("entrada");
            var zona = com.darkcraft.pueblo.gen.Andar.desde(mundo, en[0], en[1], en[2], CX - 42, CZ - 66, CX + 42, CZ + 64, 4_000_000);
            StringBuilder ap = new StringBuilder();
            int llega = 0, sitios = 0;
            for (var e : A.sitios().entrySet()) {
                sitios++;
                int[] q = e.getValue();
                if (zona.alcanza(q[0], q[1], q[2], 2)) llega++;
                else ap.append(' ').append(e.getKey());
            }
            int lejos = 0;
            for (var o : A.esperas()) if (!zona.alcanza(o.x(), o.y(), o.z(), 2)) lejos++;
            var sc = com.darkcraft.morvain.SantuariosMorvain.porId("puerta_academia");
            log.println("Morvain, la academia espectral: " + nch + " chunks en " + ms + " ms; " + com.darkcraft.morvain.Academia.resumen(m)
                    + "; santuario " + com.darkcraft.santuario.Santuarios.nombre(m, sc.pos()) + " a " + Math.round(Math.hypot(sc.x() - CX, sc.z() - (CZ + 47)))
                    + " de la puerta; a pie desde el camino (" + zona.celdas() + " celdas) a " + llega + " de " + sitios + " sitios"
                    + (ap.length() == 0 ? "" : ", no a:" + ap) + "; eruditos a los que no se llega " + lejos + " de " + A.esperas().size());
            topView(m, CX, CZ - 6, 66, new File(out, "morvain_academia.png"));
            topView(m, CX, CZ - 27, 18, new File(out, "morvain_academia_biblioteca.png"));
        } catch (Throwable t) {
            log.println("Morvain, la academia espectral: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] academia", t);
        }
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        try {
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            for (int k = 0; k < 10; k++)
                for (var e : fuera) if (e.isAlive()) e.tick();
            int leyendo = 0, tumbados = 0, dePie = 0, enSuelo = 0, chocan = 0;
            int[] porVariante = new int[3];
            StringBuilder mal = new StringBuilder();
            com.darkcraft.enemigos.Erudito lector = null, tumbado = null;
            for (var e : fuera) {
                if (!(e instanceof com.darkcraft.enemigos.Erudito c)) continue;
                porVariante[Math.max(0, Math.min(2, c.variante()))]++;
                if (!m.noCollision(c)) {
                    chocan++;
                    if (mal.length() < 300) mal.append(" metido en un bloque en ").append(c.blockPosition().toShortString()).append(';');
                }
                if (c.postura() == com.darkcraft.enemigos.Enemigo.REZA) {
                    leyendo++;
                    if (lector == null) lector = c;
                } else if (c.postura() == com.darkcraft.enemigos.Enemigo.TUMBADO) {
                    tumbados++;
                    if (tumbado == null) tumbado = c;
                } else dePie++;
                if (!m.getBlockState(BlockPos.containing(c.getX(), c.getY() - 0.2, c.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty()) enSuelo++;
                else if (mal.length() < 400) mal.append(' ').append(c.tipo()).append(" en el aire en ").append(c.blockPosition().toShortString()).append(';');
            }
            log.println("Morvain, los eruditos: " + com.darkcraft.morvain.MorvainEnemigos.comprobar() + "; fuera " + fuera.size() + " de "
                    + com.darkcraft.morvain.MorvainEnemigos.academia().size() + " (aprendices " + porVariante[0] + ", maestros " + porVariante[1]
                    + ", custodios " + porVariante[2] + "): leyendo " + leyendo + ", tumbados " + tumbados + ", de pie " + dePie
                    + " (en su suelo " + enSuelo + "); metidos en un bloque " + chocan + " (tiene que ser 0)" + mal);
            StringBuilder sb = new StringBuilder("Morvain, los eruditos en accion:");
            // ---- el que lee se alza y el tumbado se levanta (una presa a su lado)
            for (var c : new com.darkcraft.enemigos.Erudito[]{lector, tumbado}) {
                if (c == null) {
                    sb.append(" NO HAY ").append(c == lector ? "NINGUNO LEYENDO;" : "NINGUNO TUMBADO;");
                    continue;
                }
                var presa = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
                if (presa == null) continue;
                presa.moveTo(c.getX() + 1.5, c.getY(), c.getZ() + 0.5, 0f, 0f);
                presa.setNoAi(true);
                m.addFreshEntity(presa);
                quitar.add(presa);
                int antes = c.postura();
                c.setTarget(presa);
                int sale = -1;
                for (int k = 0; k < 80; k++) {
                    c.tickCount++;
                    c.tick();
                    int a = c.accion();
                    if (sale < 0 && (a == com.darkcraft.enemigos.EruditoAnims.LEVANTARSE || a == com.darkcraft.enemigos.EruditoAnims.ALZARSE)) sale = k;
                    if (sale >= 0 && a == 0) break;
                }
                sb.append(String.format(java.util.Locale.ROOT, " el %s (%s, en %s) se %s en el tick %d y queda de pie %s;", c.tipo(),
                        antes == com.darkcraft.enemigos.Enemigo.REZA ? "leyendo" : "tumbado", c.blockPosition().toShortString(),
                        antes == com.darkcraft.enemigos.Enemigo.REZA ? "alza" : "levanta", sale, c.postura() == com.darkcraft.enemigos.Enemigo.DE_PIE));
                c.setTarget(null);
            }
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio)) e.discard();
            // ---- en el patio de entrada (de cara al sur, a la puerta): uno de cada, cada accion acaba
            for (int v2 = 0; v2 < 3; v2++) {
                var a = DarkCraftMod.ERUDITO.get().create(m);
                if (a == null) continue;
                a.variante(v2);
                a.moveTo(CX - 4.5 + v2 * 4, PISO, CZ + 32.5, 0f, 0f);
                m.addFreshEntity(a);
                quitar.add(a);
                StringBuilder no = new StringBuilder();
                for (int ac = 1; ac < com.darkcraft.enemigos.EruditoAnims.TICKS.length; ac++) {
                    String nombre = com.darkcraft.enemigos.EruditoAnims.NOMBRES[ac];
                    if (nombre.equals("morir")) continue;
                    a.empezar(ac);
                    for (int k = 0; k < 90 && a.accion() == ac; k++) {
                        a.tickCount++;
                        a.tick();
                    }
                    if (a.accion() == ac) no.append(' ').append(nombre);
                    for (int k = 0; k < 10; k++) a.tick();
                }
                sb.append(' ').append(a.tipo()).append(String.format(java.util.Locale.ROOT, " (vida %.0f, golpe %.1f, armadura %d)", a.getMaxHealth(),
                        a.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE), a.getArmorValue()))
                        .append(": acciones que no acaban").append(no.length() == 0 ? " ninguna" : no).append(';');
                for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Tinta.class, a.getBoundingBox().inflate(24))) f.discard();
                for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Orbe.class, a.getBoundingBox().inflate(24))) f.discard();
                a.discard();
            }
            log.println(sb);
            // ---- contra una presa (un aldeano quieto en el patio de entrada, al sur del erudito)
            sb = new StringBuilder("Morvain, los eruditos contra una presa:");
            int tint = com.darkcraft.enemigos.Erudito.TINTEROS, rev = com.darkcraft.enemigos.Tinta.REVIENTAN, orb = com.darkcraft.enemigos.Erudito.ORBES,
                    orbDan = com.darkcraft.enemigos.Orbe.DAN, run = com.darkcraft.enemigos.Erudito.RUNAS, parp = com.darkcraft.enemigos.Erudito.PARPADEOS,
                    tom = com.darkcraft.enemigos.Erudito.TOMAZOS, ond = com.darkcraft.enemigos.Erudito.ONDAS, cad = com.darkcraft.enemigos.Erudito.CADENAZOS;
            String[] nombres = {"plumazo", "tintero", "orbe", "runas", "parpadeo", "tomazo", "cadenazo"};
            int[] acciones = {com.darkcraft.enemigos.EruditoAnims.PLUMAZO, com.darkcraft.enemigos.EruditoAnims.TINTERO, com.darkcraft.enemigos.EruditoAnims.ORBE,
                    com.darkcraft.enemigos.EruditoAnims.RUNAS, com.darkcraft.enemigos.EruditoAnims.PARPADEO, com.darkcraft.enemigos.EruditoAnims.TOMAZO,
                    com.darkcraft.enemigos.EruditoAnims.CADENAZO};
            int[] variantes = {com.darkcraft.enemigos.Erudito.APRENDIZ, com.darkcraft.enemigos.Erudito.APRENDIZ, com.darkcraft.enemigos.Erudito.MAESTRO,
                    com.darkcraft.enemigos.Erudito.MAESTRO, com.darkcraft.enemigos.Erudito.MAESTRO, com.darkcraft.enemigos.Erudito.CUSTODIO,
                    com.darkcraft.enemigos.Erudito.CUSTODIO};
            double[] dist = {1.8, 7.0, 7.0, 6.0, 2.4, 2.4, 4.2};
            int[] dura = {40, 70, 120, 60, 40, 50, 50};
            final double gx = CX + 0.5, gz = CZ + 31.5;
            for (int i = 0; i < nombres.length; i++) {
                var a = DarkCraftMod.ERUDITO.get().create(m);
                var presa = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
                if (a == null || presa == null) continue;
                a.variante(variantes[i]);
                a.moveTo(gx, PISO, gz, 0f, 0f);
                presa.moveTo(gx, PISO, gz + dist[i], 180f, 0f);
                presa.setNoAi(true);
                m.addFreshEntity(a);
                m.addFreshEntity(presa);
                quitar.add(a);
                quitar.add(presa);
                a.setTarget(presa);
                a.setYRot(0f);
                a.yBodyRot = a.yHeadRot = 0f;
                a.empezar(acciones[i]);
                float vida = presa.getHealth();
                boolean frenada = false;
                double lejosMax = 0;
                for (int k = 0; k < dura[i]; k++) {
                    if (!a.isRemoved()) {
                        a.tickCount++;
                        a.tick();
                        lejosMax = Math.max(lejosMax, Math.hypot(a.getX() - gx, a.getZ() - gz));
                    }
                    for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Tinta.class, presa.getBoundingBox().inflate(16))) {
                        if (!quitar.contains(f)) quitar.add(f);
                        f.tick();
                    }
                    for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Orbe.class, presa.getBoundingBox().inflate(16))) {
                        if (!quitar.contains(f)) quitar.add(f);
                        f.tick();
                    }
                    presa.setOnGround(true);
                    frenada |= presa.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
                    presa.invulnerableTime = 0;
                }
                sb.append(String.format(java.util.Locale.ROOT, " %s (%s) a %.1f: la presa -%.1f%s%s;", nombres[i], a.tipo(), dist[i], vida - presa.getHealth(),
                        i == 6 ? ", frenada " + frenada : "", i == 4 ? String.format(java.util.Locale.ROOT, ", se aparta a %.1f", lejosMax)
                                : i == 3 ? ", runas por reventar " + a.runasPendientes() : ""));
                a.discard();
                presa.discard();
            }
            sb.append(String.format(java.util.Locale.ROOT, " tinteros +%d (revientan +%d), orbes +%d (dan +%d), runas +%d, parpadeos +%d, tomazos +%d (ondas +%d), cadenazos +%d",
                    com.darkcraft.enemigos.Erudito.TINTEROS - tint, com.darkcraft.enemigos.Tinta.REVIENTAN - rev, com.darkcraft.enemigos.Erudito.ORBES - orb,
                    com.darkcraft.enemigos.Orbe.DAN - orbDan, com.darkcraft.enemigos.Erudito.RUNAS - run, com.darkcraft.enemigos.Erudito.PARPADEOS - parp,
                    com.darkcraft.enemigos.Erudito.TOMAZOS - tom, com.darkcraft.enemigos.Erudito.ONDAS - ond, com.darkcraft.enemigos.Erudito.CADENAZOS - cad));
            log.println(sb);
        } catch (Throwable t) {
            log.println("Morvain, los eruditos: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] eruditos", t);
        } finally {
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    /**
     * Parte 7p: el Observatorio de la Academia (a pie desde el camino del santuario —la escalinata, el porche, el vestibulo y la
     * puerta de la rotonda— a todos sus sitios; a la celda de la Rectora, con el atril del libro 25 y el arcon, no mientras su
     * reja este bajada; la cupula con su rendija abierta, el planetario y el telescopio) y la Rectora: leyendo el cielo en el
     * estrado; al ver a alguien en la rotonda habla mientras se levanta, el sello de luz cierra la puerta (y pasa a la rotonda a
     * quien estaba en el hueco) y grita al acabar la frase; cada accion acaba; sus golpes a una presa (el baculo, que frena y no
     * llega lejos; los orbes; la lluvia de estrellas por la rendija, con sus marcas en el suelo de la rotonda; la constelacion,
     * que quema a quien esta de pie en una linea y no a quien salta; el parpadeo, que la aparta); a media vida se alza y de las
     * hornacinas salen sus maestros; la magia le hace la mitad y el fuego mas; y al caer: el sello se rompe, la reja de su celda
     * se alza y los que leian en la academia y a su puerta descansan y ya no vuelven. Al acabar, todo como estaba.
     */
    private static void observatorio(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.OBSERVATORIO, academia = com.darkcraft.morvain.MorvainEnemigos.ACADEMIA;
        var O = com.darkcraft.morvain.Observatorio.get();
        final int CX = com.darkcraft.morvain.Observatorio.CX, CZ = com.darkcraft.morvain.Observatorio.CZ, PISO = com.darkcraft.morvain.Observatorio.PISO;
        final int REJA = CX + com.darkcraft.morvain.Observatorio.REJA_U;
        final BlockPos rejaPos = new BlockPos(REJA, PISO + 1, CZ);
        try {
            long t0 = System.currentTimeMillis();
            int nch = 0;
            for (long k : O.chunks()) {
                m.getChunk((int) (k >> 32), (int) k);
                nch++;
            }
            long ms = System.currentTimeMillis() - t0;
            // a pie desde el camino del santuario (las alfombras no estorban): con la reja de la celda bajada y sin ella
            var aire = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
                var st = m.getBlockState(new BlockPos(x, y, z));
                return st.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock ? aire : st;
            };
            com.darkcraft.pueblo.gen.Andar.Mundo sinReja = (x, y, z) -> {
                var st = mundo.en(x, y, z);
                return x == REJA && st.is(net.minecraft.world.level.block.Blocks.IRON_BARS) ? aire : st;
            };
            int[] en = O.sitio("entrada");
            int ax0 = com.darkcraft.morvain.Observatorio.X0, az0 = com.darkcraft.morvain.Observatorio.Z0, ax1 = com.darkcraft.morvain.Observatorio.X1,
                    az1 = com.darkcraft.morvain.Observatorio.Z1;
            var zona = com.darkcraft.pueblo.gen.Andar.desde(mundo, en[0], en[1], en[2], ax0, az0, ax1, az1, 4_000_000);
            var zona2 = com.darkcraft.pueblo.gen.Andar.desde(sinReja, en[0], en[1], en[2], ax0, az0, ax1, az1, 4_000_000);
            StringBuilder no = new StringBuilder(), no2 = new StringBuilder();
            int llega = 0, sitios = 0;
            for (var e : O.sitios().entrySet()) {
                sitios++;
                int[] q = e.getValue();
                if (zona.alcanza(q[0], q[1], q[2], 2)) llega++;
                else no.append(' ').append(e.getKey());
                if (!zona2.alcanza(q[0], q[1], q[2], 2)) no2.append(' ').append(e.getKey());
            }
            int lejos = 0;
            for (var o : O.esperas()) if (!zona.alcanza(o.x(), o.y(), o.z(), 2)) lejos++;
            int hornacinas = 0;
            for (int[] h : com.darkcraft.morvain.Observatorio.hornacinas()) {
                BlockPos p = new BlockPos(h[0], h[1], h[2]);
                if (m.getBlockState(p).getCollisionShape(m, p).isEmpty() && m.getBlockState(p.above()).getCollisionShape(m, p.above()).isEmpty()
                        && !m.getBlockState(p.below()).getCollisionShape(m, p.below()).isEmpty()) hornacinas++;
            }
            var sc = com.darkcraft.morvain.SantuariosMorvain.porId("observatorio");
            log.println("Morvain, el observatorio: " + nch + " chunks en " + ms + " ms; " + com.darkcraft.morvain.Observatorio.resumen(m) + "; lancetas " + O.lancetas
                    + ", estrellas del suelo " + O.estrellas + ", planetas " + O.planetas + ", menhires " + O.menhires + ", estantes " + O.estantes + ", faroles "
                    + O.faroles + "; santuario " + com.darkcraft.santuario.Santuarios.nombre(m, sc.pos()) + " a " + Math.round(Math.hypot(sc.x() - en[0], sc.z() - en[2]))
                    + " de la entrada; a pie desde el camino (" + zona.celdas() + " celdas) a " + llega + " de " + sitios + " sitios" + (no.length() == 0 ? "" : ", no a:" + no)
                    + " (la reja de la celda esta bajada); sin la reja, no a:" + (no2.length() == 0 ? " ninguno" : no2) + "; eruditos a los que no se llega " + lejos
                    + " de " + O.esperas().size() + "; hornacinas libres " + hornacinas + " de " + com.darkcraft.morvain.Observatorio.hornacinas().size());
            topView(m, CX, CZ - 2, 54, new File(out, "morvain_observatorio.png"));
            topView(m, CX, CZ, 17, new File(out, "morvain_observatorio_rotonda.png"));
        } catch (Throwable t) {
            log.println("Morvain, el observatorio: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] observatorio", t);
        }
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        com.darkcraft.enemigos.Rectora re = null;
        try {
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            int suyos = 0, leyendo = 0, enSuelo = 0, chocan = 0;
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            for (int k = 0; k < 6; k++)
                for (var e : fuera) if (e.isAlive() && !(e instanceof com.darkcraft.enemigos.Rectora)) e.tick();
            for (var e : fuera) {
                if (e instanceof com.darkcraft.enemigos.Rectora x) re = x;
                else {
                    suyos++;
                    if (e.postura() == com.darkcraft.enemigos.Enemigo.REZA) leyendo++;
                }
                if (!m.noCollision(e)) chocan++;
                if (!m.getBlockState(BlockPos.containing(e.getX(), e.getY() - 0.2, e.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty()) enSuelo++;
            }
            if (re == null) {
                log.println("Morvain, la Rectora: NO SALE");
                return;
            }
            for (var e : fuera) if (e != re) e.discard();
            StringBuilder sb = new StringBuilder("Morvain, la Rectora: en " + re.blockPosition().toShortString() + " postura " + re.postura()
                    + " (1 leyendo el cielo), giro " + Math.round(re.getYRot()) + ", vida " + Math.round(re.getMaxHealth()) + ", caja " + re.getBbWidth() + " x "
                    + re.getBbHeight() + "; los suyos " + suyos + " (leyendo " + leyendo + "); en su suelo " + enSuelo + " de " + fuera.size()
                    + ", metidos en un bloque " + chocan + " (tiene que ser 0)");
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("4a6b8c0d-2e3f-4a5b-9c7d-1e2f3a4b5c89"), "[DarkCraftCI9]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            // ---- la ve desde la rotonda: habla mientras se levanta, el sello cierra la puerta (y pasa dentro a quien esta en el hueco) y grita
            final double fxx = CX + 0.5, fyy = PISO, fzz = CZ + 4.5;
            fp.moveTo(fxx, fyy, fzz, 180f, 0f);
            fp.setOnGround(true);
            var enLaPuerta = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
            final double zPuerta = CZ + com.darkcraft.morvain.Observatorio.SELLO_V + 0.5;
            if (enLaPuerta != null) {
                enLaPuerta.moveTo(CX + 0.5, PISO, zPuerta, 180f, 0f);
                enLaPuerta.setNoAi(true);
                m.addFreshEntity(enLaPuerta);
                quitar.add(enLaPuerta);
            }
            com.darkcraft.morvain.Observatorio.abrir(m, false);
            int sellados = com.darkcraft.morvain.Observatorio.SELLADOS;
            boolean selladoAntes = com.darkcraft.morvain.Observatorio.sellado(m);
            re.setTarget(fp);
            int levanta = -1, grita = -1, ataca = -1, quien = 0, sella = -1;
            for (int k = 0; k < 400; k++) {
                re.tickCount++;
                re.tick();
                com.darkcraft.morvain.Observatorio.tick(m);
                fp.moveTo(fxx, fyy, fzz, 180f, 0f);
                if (sella < 0 && com.darkcraft.morvain.Observatorio.sellado(m)) sella = k;
                int a = re.accion();
                if (levanta < 0 && a == com.darkcraft.enemigos.RectoraAnims.LEVANTARSE) levanta = k;
                if (grita < 0 && a == com.darkcraft.enemigos.RectoraAnims.GRITO) grita = k;
                if (grita >= 0 && ataca < 0 && a != 0 && a != com.darkcraft.enemigos.RectoraAnims.GRITO) {
                    ataca = k;
                    quien = a;
                    break;
                }
            }
            sb.append("; al verla: pelea ").append(re.enPelea()).append(", se levanta en el tick ").append(levanta).append(", el sello (cerrado antes ")
                    .append(selladoAntes).append(") se cierra en el ").append(sella).append(" (sellados +").append(com.darkcraft.morvain.Observatorio.SELLADOS - sellados)
                    .append("), el que estaba en la puerta ")
                    .append(enLaPuerta == null ? "?" : enLaPuerta.getZ() < zPuerta - 1.0 ? "pasa a la rotonda" : "SIGUE EN LA PUERTA en z " + enLaPuerta.getZ())
                    .append(", grita en el ").append(grita).append(" (la frase 1 dura ").append(com.darkcraft.rey.Voces.ticks(com.darkcraft.enemigos.Rectora.VOZ_BASE + 1))
                    .append("), y luego ").append(quien == 0 ? "nada" : com.darkcraft.enemigos.RectoraAnims.NOMBRES[quien]).append(" en el ").append(ataca)
                    .append(String.format(java.util.Locale.ROOT, ", de pie %s a %.1f de su presa", re.postura() == com.darkcraft.enemigos.Enemigo.DE_PIE,
                            Math.hypot(re.getX() - fp.getX(), re.getZ() - fp.getZ())));
            if (enLaPuerta != null) enLaPuerta.discard();
            // ---- cada accion acaba (sin presa y quieta: no elige ni grita por su cuenta; el alzarse va a media vida)
            re.quieta = true;
            re.setTarget(null);
            StringBuilder nn = new StringBuilder();
            for (int a = 1; a < com.darkcraft.enemigos.RectoraAnims.TICKS.length; a++) {
                String nombre = com.darkcraft.enemigos.RectoraAnims.NOMBRES[a];
                if (nombre.equals("morir") || nombre.equals("alzarse")) continue;
                re.moveTo(CX + 0.5, PISO, CZ - 2.5, 0f, 0f);
                re.empezar(a);
                for (int k = 0; k < 140 && re.accion() == a; k++) {
                    re.tickCount++;
                    re.tick();
                }
                if (re.accion() == a) nn.append(' ').append(nombre);
            }
            for (int k = 0; k < 60 && re.pendientes() > 0; k++) {
                re.tickCount++;
                re.tick();
            }
            for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Orbe.class, re.getBoundingBox().inflate(48))) f.discard();
            sb.append("; acciones que no acaban:").append(nn.length() == 0 ? " ninguna" : nn).append(" (por caer ").append(re.pendientes()).append(')');
            log.println(sb);
            // ---- sus golpes a una presa (un aldeano quieto y duro), en la rotonda: ella mirando al sur y la presa delante
            StringBuilder sg = new StringBuilder("Morvain, los golpes de la Rectora a una presa:");
            final double x0 = CX + 0.5, y0 = PISO, z0 = CZ - 2.5;
            var victima = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
            if (victima != null) {
                victima.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(900.0);
                victima.setHealth(900f);
                victima.setNoAi(true);
                victima.moveTo(x0, y0, z0 + 3, 180f, 0f);
                m.addFreshEntity(victima);
                quitar.add(victima);
                String[] golpes = {"baculo de cerca", "baculo de lejos", "orbes", "estrellas", "constelacion de pie", "constelacion saltando", "parpadeo"};
                int[] acc = {com.darkcraft.enemigos.RectoraAnims.BACULO, com.darkcraft.enemigos.RectoraAnims.BACULO, com.darkcraft.enemigos.RectoraAnims.ORBES,
                        com.darkcraft.enemigos.RectoraAnims.ESTRELLAS, com.darkcraft.enemigos.RectoraAnims.CONSTELACION,
                        com.darkcraft.enemigos.RectoraAnims.CONSTELACION, com.darkcraft.enemigos.RectoraAnims.PARPADEO};
                double[] lejos = {3.0, 6.5, 9.0, 8.0, 7.0, 7.0, 2.0};
                for (int i = 0; i < golpes.length; i++) {
                    final boolean salta = i == 5;
                    victima.setHealth(victima.getMaxHealth());
                    victima.invulnerableTime = 0;
                    victima.hurtTime = 0;
                    victima.removeAllEffects();
                    final double pz = z0 + lejos[i], py = y0 + (salta ? 1.2 : 0.0);
                    victima.moveTo(x0, py, pz, 180f, 0f);
                    int orbDan = com.darkcraft.enemigos.Orbe.DAN, est = re.estrellasQueDan(), lin = re.lineasQueDan(), par = re.parpadeos(), orb = re.orbes();
                    re.moveTo(x0, y0, z0, 0f, 0f);
                    re.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    re.setTarget(victima);
                    re.setYRot(0f);
                    re.yBodyRot = re.yHeadRot = 0f;
                    re.empezar(acc[i]);
                    float antes = victima.getHealth();
                    boolean frenada = false;
                    int marcas = -1, marcasDentro = 0, nodos = -1;
                    double aparta = 0;
                    for (int k = 0; k < 160; k++) {
                        boolean ella = re.accion() == acc[i] || re.pendientes() > 0;
                        if (ella) {
                            re.tickCount++;
                            re.tick();
                            if (acc[i] != com.darkcraft.enemigos.RectoraAnims.PARPADEO) re.moveTo(x0, y0, z0, 0f, 0f);
                            else aparta = Math.max(aparta, Math.hypot(re.getX() - x0, re.getZ() - z0));
                            if (marcas < 0 && !re.marcas().isEmpty()) {
                                marcas = re.marcas().size();
                                for (double[] q : re.marcas())
                                    if (com.darkcraft.morvain.Observatorio.enRotonda(q[0], q[2]) && Math.abs(q[1] - PISO) <= 1.1) marcasDentro++;
                            }
                            if (nodos < 0 && !re.nodos().isEmpty()) nodos = re.nodos().size();
                        }
                        boolean orbes = false;
                        for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Orbe.class, re.getBoundingBox().inflate(48))) {
                            if (!quitar.contains(f)) quitar.add(f);
                            if (!f.isRemoved()) {
                                f.tick();
                                orbes = true;
                            }
                        }
                        victima.moveTo(x0, py, pz, 180f, 0f);
                        victima.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                        victima.setOnGround(!salta);
                        victima.invulnerableTime = 0;
                        frenada |= victima.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
                        if (!ella && !orbes) break;
                    }
                    sg.append(' ').append(golpes[i]).append(String.format(java.util.Locale.ROOT, " a %.1f: -%.1f", lejos[i], antes - victima.getHealth()))
                            .append(frenada ? ", frenada" : "")
                            .append(acc[i] == com.darkcraft.enemigos.RectoraAnims.ORBES ? ", orbes " + (re.orbes() - orb) + " (dan " + (com.darkcraft.enemigos.Orbe.DAN - orbDan) + ")" : "")
                            .append(acc[i] == com.darkcraft.enemigos.RectoraAnims.ESTRELLAS ? ", estrellas " + marcas + " (sus marcas en el suelo de la rotonda " + marcasDentro
                                    + ", le dan " + (re.estrellasQueDan() - est) + ")" : "")
                            .append(acc[i] == com.darkcraft.enemigos.RectoraAnims.CONSTELACION ? ", estrellas de la constelacion " + nodos + " (le queman "
                                    + (re.lineasQueDan() - lin) + ")" : "")
                            .append(acc[i] == com.darkcraft.enemigos.RectoraAnims.PARPADEO ? String.format(java.util.Locale.ROOT, ", se aparta a %.1f (parpadeos +%d, en la rotonda %s)",
                                    aparta, re.parpadeos() - par, com.darkcraft.morvain.Observatorio.enRotonda(re.getX(), re.getZ())) : "")
                            .append(';');
                    for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Orbe.class, re.getBoundingBox().inflate(48))) f.discard();
                }
                sg.append(" en total: baculos ").append(re.baculos()).append(", orbes ").append(re.orbes()).append(", lluvias ").append(re.lluvias())
                        .append(" (estrellas ").append(re.estrellasCaidas()).append(", dan ").append(re.estrellasQueDan()).append("), constelaciones ")
                        .append(re.constelaciones()).append(" (dan ").append(re.lineasQueDan()).append("), parpadeos ").append(re.parpadeos());
                victima.discard();
            }
            log.println(sg);
            // ---- a media vida: se alza y de las hornacinas salen sus maestros (efimeros, con su presa)
            StringBuilder sm = new StringBuilder("Morvain, la Rectora a media vida:");
            re.quieta = false;
            re.moveTo(CX + 0.5, PISO + 1, CZ + com.darkcraft.morvain.Observatorio.RECTORA_V + 0.5, 0f, 0f);
            fp.moveTo(fxx, fyy, fzz, 180f, 0f);
            re.setTarget(fp);
            re.mediaVida();
            boolean alzarse = re.accion() == com.darkcraft.enemigos.RectoraAnims.ALZARSE;
            int k = 0;
            for (; k < 60 && re.maestrosAlzados() == 0; k++) {
                re.tickCount++;
                re.tick();
                fp.moveTo(fxx, fyy, fzz, 180f, 0f);
            }
            int bien = 0, conPresa = 0, maestros = 0, enHornacina = 0;
            for (var c : m.getEntitiesOfClass(com.darkcraft.enemigos.Erudito.class, new net.minecraft.world.phys.AABB(CX - 20, PISO - 4, CZ - 20, CX + 21, PISO + 8, CZ + 21))) {
                if (!c.esEfimero()) continue;
                quitar.add(c);
                if (c.variante() == com.darkcraft.enemigos.Erudito.MAESTRO) maestros++;
                boolean pisa = !m.getBlockState(BlockPos.containing(c.getX(), c.getY() - 0.2, c.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty();
                if (pisa && m.noCollision(c)) bien++;
                if (c.getTarget() == fp) conPresa++;
                for (int[] h : com.darkcraft.morvain.Observatorio.hornacinas())
                    if (Math.hypot(c.getX() - (h[0] + 0.5), c.getZ() - (h[2] + 0.5)) < 1.2) enHornacina++;
            }
            int acaba = 0;
            for (; acaba < 80 && re.accion() == com.darkcraft.enemigos.RectoraAnims.ALZARSE; acaba++) {
                re.tickCount++;
                re.tick();
                fp.moveTo(fxx, fyy, fzz, 180f, 0f);
            }
            sm.append(" segunda ").append(re.segunda()).append(", se alza ").append(alzarse).append(" (y acaba ").append(re.accion() != com.darkcraft.enemigos.RectoraAnims.ALZARSE)
                    .append("), salen en el tick ").append(k).append(' ').append(re.maestrosAlzados()).append(" (maestros ").append(maestros).append(", ante su hornacina ")
                    .append(enHornacina).append(", en su suelo y sin meterse en nada ").append(bien).append(", con presa ").append(conPresa).append("), en pie ").append(re.suyos());
            // ---- la magia le hace la mitad; el fuego, un 40 % mas que un golpe igual
            re.quieta = true;
            re.invulnerableTime = 0;
            float v0 = re.getHealth();
            re.hurt(m.damageSources().magic(), 10f);
            re.invulnerableTime = 0;
            float v1 = re.getHealth();
            re.hurt(m.damageSources().generic(), 10f);
            re.invulnerableTime = 0;
            float v2 = re.getHealth();
            re.hurt(m.damageSources().onFire(), 10f);
            re.invulnerableTime = 0;
            re.clearFire();
            sm.append(String.format(java.util.Locale.ROOT, "; la magia de 10, -%.1f; un golpe de 10, -%.1f; el fuego de 10, -%.1f", v0 - v1, v1 - v2, v2 - re.getHealth()));
            log.println(sm);
            // ---- cae: el sello se rompe, la reja de su celda se alza y los que leian (en la academia y a su puerta) descansan
            com.darkcraft.enemigos.Puestos.todos(server, academia, false);
            java.util.List<com.darkcraft.enemigos.Enemigo> lectores = new java.util.ArrayList<>();
            int deLaAcademia = 0;
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, academia))
                if (com.darkcraft.morvain.MorvainEnemigos.academia().get(e.puesto()).postura() == com.darkcraft.enemigos.Enemigo.REZA) {
                    deLaAcademia++;
                    lectores.add(e);
                }
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio))
                if (e != re && com.darkcraft.morvain.MorvainEnemigos.observatorio().get(e.puesto()).postura() == com.darkcraft.enemigos.Enemigo.REZA) lectores.add(e);
            for (int j = 0; j < 3; j++) com.darkcraft.morvain.Observatorio.tick(m);
            boolean selladoAlCaer = com.darkcraft.morvain.Observatorio.sellado(m), rejaAlCaer = m.getBlockState(rejaPos).is(net.minecraft.world.level.block.Blocks.IRON_BARS);
            int abiertos = com.darkcraft.morvain.Observatorio.ABIERTOS, desc = com.darkcraft.morvain.Observatorio.DESCANSAN;
            re.invulnerableTime = 0;
            re.hurt(m.damageSources().playerAttack(fp), 99999f);
            boolean muere = !re.isAlive();
            int abre = -1, alza = -1, seVa = -1;
            for (int fin = 0; fin < 320; fin++) {
                if (!re.isRemoved()) {
                    re.tickCount++;
                    re.tick();
                }
                com.darkcraft.morvain.Observatorio.tick(m);
                if (abre < 0 && !com.darkcraft.morvain.Observatorio.sellado(m)) abre = fin;
                if (alza < 0 && !m.getBlockState(rejaPos).is(net.minecraft.world.level.block.Blocks.IRON_BARS)) alza = fin;
                if (seVa < 0 && re.isRemoved()) seVa = fin;
                if (abre >= 0 && alza >= 0 && seVa >= 0) break;
            }
            com.darkcraft.morvain.Observatorio.paz(m);
            int descansan = 0;
            for (var e : lectores) if (e.isRemoved()) descansan++;
            int objetos = m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, re.getBoundingBox().inflate(6)).size();
            com.darkcraft.enemigos.Puestos.todos(server, academia, false);
            int salen = 0, vuelven = 0;
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, academia)) {
                salen++;
                if (com.darkcraft.morvain.MorvainEnemigos.academia().get(e.puesto()).postura() == com.darkcraft.enemigos.Enemigo.REZA) vuelven++;
            }
            com.darkcraft.enemigos.Puestos.todos(server, academia, false);
            log.println("Morvain, la Rectora cae: muere " + muere + "; al caer el sello cerrado " + selladoAlCaer + " y la reja bajada " + rejaAlCaer
                    + "; el sello se rompe en " + abre + " ticks (abiertos +" + (com.darkcraft.morvain.Observatorio.ABIERTOS - abiertos) + "), la reja de su celda se alza en "
                    + alza + ", se deshace en estrellas en " + seVa + "; vivo " + re.isAlive() + ", objetos " + objetos + ", en paz "
                    + com.darkcraft.morvain.Observatorio.enPaz(server) + ", sus maestros en pie " + re.suyos() + "; de los " + lectores.size() + " que leian ("
                    + deLaAcademia + " en la academia) descansan " + descansan + " (deshechos +" + (com.darkcraft.morvain.Observatorio.DESCANSAN - desc)
                    + "); despues en la academia salen " + salen + " y de los que leian vuelven " + vuelven + " (tiene que ser 0); "
                    + com.darkcraft.morvain.MorvainEnemigos.estadoRectora(server));
            for (var it : m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, re.getBoundingBox().inflate(8))) it.discard();
        } catch (Throwable t) {
            log.println("Morvain, la Rectora: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] rectora", t);
        } finally {
            if (re != null) re.quieta = false;
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            var caja = new net.minecraft.world.phys.AABB(CX - 60, PISO - 20, CZ - 60, CX + 60, PISO + 40, CZ + 60);
            for (var e : m.getEntitiesOfClass(com.darkcraft.enemigos.Erudito.class, caja)) if (e.esEfimero()) e.discard();
            for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Orbe.class, caja)) f.discard();
            // todo como antes: la Rectora leyendo el cielo, el sello abierto y la reja de su celda bajada; la academia, con sus lectores
            com.darkcraft.enemigos.Puestos.marcar(server, sitio, com.darkcraft.morvain.MorvainEnemigos.rectora(), false);
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            com.darkcraft.enemigos.Puestos.todos(server, academia, false);
            try {
                com.darkcraft.morvain.Observatorio.deshacerPaz(m);
                com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
                log.println("Morvain, la Rectora vuelve: " + com.darkcraft.morvain.MorvainEnemigos.estadoRectora(server));
            } catch (Throwable t) {
                log.println("Morvain, la Rectora vuelve: ERROR " + t);
            }
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    /**
     * Parte 7q: el Puerto Varado (a pie desde el santuario del Faro de los Huesos, bajo el arco de costillas, a todos sus sitios:
     * la bahia por las escaleras del muelle, la grada y la bajada del espigon, y lo alto del faro por su escalera de caracol; el
     * atril con el libro 26, el arcon, el muelle, el fondo seco, las costillas de la ballena y el fuego del faro) y los salobres:
     * en sus puestos (faenando, enterrados en la sal o de pie), uno de rodillas que se levanta y uno enterrado que se alza; cada
     * accion de cada uno acaba; sus golpes a una presa (el plomo, la red que vuela y enreda, el garfio que arrastra, el bichazo,
     * el rastrillazo con su costra de sal que frena y el punado de sal) y uno que cae y se deshace en sal. Al acabar, todo como
     * estaba.
     */
    private static void puerto(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.PUERTO;
        var A = com.darkcraft.morvain.Varadero.get();
        final int CX = com.darkcraft.morvain.Varadero.CX, CZ = com.darkcraft.morvain.Varadero.CZ, PISO = com.darkcraft.morvain.Varadero.PISO;
        try {
            long t0 = System.currentTimeMillis();
            int nch = 0;
            for (long k : A.chunks()) {
                m.getChunk((int) (k >> 32), (int) k);
                nch++;
            }
            long ms = System.currentTimeMillis() - t0;
            // a pie desde la entrada (las capas finas de nieve —la sal— no estorban)
            var aire = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
                var st = m.getBlockState(new BlockPos(x, y, z));
                return st.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock || st.getBlock() instanceof net.minecraft.world.level.block.SnowLayerBlock
                        && st.getValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS) <= 4 ? aire : st;
            };
            int[] en = A.sitio("entrada");
            var zona = com.darkcraft.pueblo.gen.Andar.desde(mundo, en[0], en[1], en[2], CX - 60, CZ - 66, CX + 124, CZ + 62, 6_000_000);
            StringBuilder ap = new StringBuilder();
            int llega = 0, sitios = 0;
            for (var e : A.sitios().entrySet()) {
                sitios++;
                int[] q = e.getValue();
                if (zona.alcanza(q[0], q[1], q[2], 2)) llega++;
                else ap.append(' ').append(e.getKey());
            }
            int lejos = 0;
            for (var o : A.esperas()) if (!zona.alcanza(o.x(), o.y(), o.z(), 2)) lejos++;
            var sc = com.darkcraft.morvain.SantuariosMorvain.porId("faro_huesos");
            log.println("Morvain, el puerto varado: " + nch + " chunks en " + ms + " ms; " + com.darkcraft.morvain.Varadero.resumen(m)
                    + "; santuario " + com.darkcraft.santuario.Santuarios.nombre(m, sc.pos()) + " (" + m.getBlockState(sc.pos()).getBlock().getDescriptionId()
                    + ") a " + Math.round(Math.hypot(sc.x() - (CX - 12), sc.z() - CZ)) + " de la plaza; a pie desde la entrada (" + zona.celdas()
                    + " celdas) a " + llega + " de " + sitios + " sitios" + (ap.length() == 0 ? "" : ", no a:" + ap) + "; salobres a los que no se llega "
                    + lejos + " de " + A.esperas().size() + "; lugar en la plaza " + (com.darkcraft.morvain.Varadero.dentro(CX - 12, CZ) ? "puerto" : "NO")
                    + ", en el faro " + com.darkcraft.morvain.Varadero.enFaro(CX + com.darkcraft.morvain.Varadero.FARO_U, CZ + com.darkcraft.morvain.Varadero.FARO_V)
                    + ", arriba " + com.darkcraft.morvain.Varadero.arribaDelFaro(CX + com.darkcraft.morvain.Varadero.FARO_U - 4 + 0.5,
                    com.darkcraft.morvain.Varadero.LINTERNA, CZ + com.darkcraft.morvain.Varadero.FARO_V + 0.5));
            topView(m, CX + 30, CZ - 2, 96, new File(out, "morvain_puerto.png"));
            topView(m, CX - 14, CZ - 4, 40, new File(out, "morvain_puerto_pueblo.png"));
        } catch (Throwable t) {
            log.println("Morvain, el puerto varado: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] puerto", t);
        }
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        try {
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            for (int k = 0; k < 10; k++)
                for (var e : fuera) if (e.isAlive()) e.tick();
            int faenando = 0, tumbados = 0, dePie = 0, enSuelo = 0, chocan = 0;
            int[] porVariante = new int[3];
            StringBuilder mal = new StringBuilder();
            com.darkcraft.enemigos.Salobre rodillas = null, enterrado = null;
            for (var e : fuera) {
                if (!(e instanceof com.darkcraft.enemigos.Salobre c)) continue;
                porVariante[Math.max(0, Math.min(2, c.variante()))]++;
                if (!m.noCollision(c)) {
                    chocan++;
                    if (mal.length() < 300) mal.append(" metido en un bloque en ").append(c.blockPosition().toShortString()).append(';');
                }
                if (c.postura() == com.darkcraft.enemigos.Enemigo.REZA) {
                    faenando++;
                    if (rodillas == null) rodillas = c;
                } else if (c.postura() == com.darkcraft.enemigos.Enemigo.TUMBADO) {
                    tumbados++;
                    if (enterrado == null) enterrado = c;
                } else dePie++;
                var abajo = m.getBlockState(BlockPos.containing(c.getX(), c.getY() - 0.2, c.getZ()));
                if (!abajo.getCollisionShape(m, BlockPos.ZERO).isEmpty() || c.isInWater()) enSuelo++;
                else if (mal.length() < 400) mal.append(' ').append(c.tipo()).append(" en el aire en ").append(c.blockPosition().toShortString()).append(';');
            }
            log.println("Morvain, los salobres: " + com.darkcraft.morvain.MorvainEnemigos.comprobar() + "; fuera " + fuera.size() + " de "
                    + com.darkcraft.morvain.MorvainEnemigos.puerto().size() + " (rederos " + porVariante[0] + ", bicheros " + porVariante[1]
                    + ", salineros " + porVariante[2] + "): faenando " + faenando + ", enterrados " + tumbados + ", de pie " + dePie
                    + " (en su suelo " + enSuelo + "); metidos en un bloque " + chocan + " (tiene que ser 0)" + mal);
            StringBuilder sb = new StringBuilder("Morvain, los salobres en accion:");
            // ---- el de rodillas se levanta y el enterrado se alza (una presa a su lado)
            int alz = com.darkcraft.enemigos.Salobre.ALZADOS;
            for (var c : new com.darkcraft.enemigos.Salobre[]{rodillas, enterrado}) {
                if (c == null) {
                    sb.append(" NO HAY ").append(c == rodillas ? "NINGUNO DE RODILLAS;" : "NINGUNO ENTERRADO;");
                    continue;
                }
                var presa = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
                if (presa == null) continue;
                presa.moveTo(c.getX() + 1.5, c.getY(), c.getZ() + 0.5, 0f, 0f);
                presa.setNoAi(true);
                m.addFreshEntity(presa);
                quitar.add(presa);
                int antes = c.postura();
                c.setTarget(presa);
                int sale = -1;
                for (int k = 0; k < 80; k++) {
                    c.tickCount++;
                    c.tick();
                    int a = c.accion();
                    if (sale < 0 && (a == com.darkcraft.enemigos.SalobreAnims.LEVANTARSE || a == com.darkcraft.enemigos.SalobreAnims.ALZARSE)) sale = k;
                    if (sale >= 0 && a == 0) break;
                }
                sb.append(String.format(java.util.Locale.ROOT, " el %s (%s, en %s) se %s en el tick %d y queda de pie %s;", c.tipo(),
                        antes == com.darkcraft.enemigos.Enemigo.REZA ? "de rodillas" : "enterrado", c.blockPosition().toShortString(),
                        antes == com.darkcraft.enemigos.Enemigo.REZA ? "levanta" : "alza", sale, c.postura() == com.darkcraft.enemigos.Enemigo.DE_PIE));
                c.setTarget(null);
            }
            sb.append(" alzados de la sal +").append(com.darkcraft.enemigos.Salobre.ALZADOS - alz).append(';');
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio)) e.discard();
            // ---- en la plaza (de cara al sur): uno de cada, cada accion acaba
            for (int v2 = 0; v2 < 3; v2++) {
                var a = DarkCraftMod.SALOBRE.get().create(m);
                if (a == null) continue;
                a.variante(v2);
                a.moveTo(CX - 16.5 + v2 * 4, PISO, CZ - 6.5, 0f, 0f);
                m.addFreshEntity(a);
                quitar.add(a);
                StringBuilder no = new StringBuilder();
                for (int ac = 1; ac < com.darkcraft.enemigos.SalobreAnims.TICKS.length; ac++) {
                    String nombre = com.darkcraft.enemigos.SalobreAnims.NOMBRES[ac];
                    if (nombre.equals("morir")) continue;
                    a.empezar(ac);
                    for (int k = 0; k < 90 && a.accion() == ac; k++) {
                        a.tickCount++;
                        a.tick();
                    }
                    if (a.accion() == ac) no.append(' ').append(nombre);
                    for (int k = 0; k < 10; k++) a.tick();
                }
                sb.append(' ').append(a.tipo()).append(String.format(java.util.Locale.ROOT, " (vida %.0f, golpe %.1f, armadura %d)", a.getMaxHealth(),
                        a.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE), a.getArmorValue()))
                        .append(": acciones que no acaban").append(no.length() == 0 ? " ninguna" : no).append(';');
                for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Red.class, a.getBoundingBox().inflate(24))) f.discard();
                a.discard();
            }
            log.println(sb);
            // ---- contra una presa (un aldeano quieto en la calle mayor, al sur del salobre)
            sb = new StringBuilder("Morvain, los salobres contra una presa:");
            int redes = com.darkcraft.enemigos.Salobre.REDES, pillan = com.darkcraft.enemigos.Red.PILLAN, garf = com.darkcraft.enemigos.Salobre.GARFIOS,
                    cos = com.darkcraft.enemigos.Salobre.COSTRAS, fre = com.darkcraft.enemigos.Salobre.FRENADOS, sal = com.darkcraft.enemigos.Salobre.SALES,
                    sald = com.darkcraft.enemigos.Salobre.SALADOS;
            String[] nombres = {"plomada", "red", "garfio", "bichazo", "rastrillazo", "sal"};
            int[] acciones = {com.darkcraft.enemigos.SalobreAnims.PLOMADA, com.darkcraft.enemigos.SalobreAnims.RED, com.darkcraft.enemigos.SalobreAnims.GARFIO,
                    com.darkcraft.enemigos.SalobreAnims.BICHAZO, com.darkcraft.enemigos.SalobreAnims.RASTRILLAZO, com.darkcraft.enemigos.SalobreAnims.SAL};
            int[] variantes = {com.darkcraft.enemigos.Salobre.REDERO, com.darkcraft.enemigos.Salobre.REDERO, com.darkcraft.enemigos.Salobre.BICHERO,
                    com.darkcraft.enemigos.Salobre.BICHERO, com.darkcraft.enemigos.Salobre.SALINERO, com.darkcraft.enemigos.Salobre.SALINERO};
            double[] dist = {1.8, 7.0, 4.3, 2.3, 2.2, 3.0};
            int[] dura = {40, 80, 50, 50, 60, 40};
            final double gx = CX + 6.5, gz = CZ - 1.5;
            for (int i = 0; i < nombres.length; i++) {
                var a = DarkCraftMod.SALOBRE.get().create(m);
                var presa = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
                if (a == null || presa == null) continue;
                a.variante(variantes[i]);
                a.moveTo(gx, PISO, gz, -90f, 0f);
                presa.moveTo(gx + dist[i], PISO, gz, 90f, 0f);
                presa.setNoAi(true);
                m.addFreshEntity(a);
                m.addFreshEntity(presa);
                quitar.add(a);
                quitar.add(presa);
                a.setTarget(presa);
                a.setYRot(-90f);
                a.yBodyRot = a.yHeadRot = -90f;
                a.empezar(acciones[i]);
                float vida = presa.getHealth();
                boolean frenada = false;
                double cerca = dist[i];
                for (int k = 0; k < dura[i]; k++) {
                    if (!a.isRemoved()) {
                        a.tickCount++;
                        a.tick();
                    }
                    for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Red.class, presa.getBoundingBox().inflate(16))) {
                        if (!quitar.contains(f)) quitar.add(f);
                        f.tick();
                    }
                    // la presa se mueve con lo que le den (el tiron del garfio): a mano, que sin IA no anda
                    net.minecraft.world.phys.Vec3 dv = presa.getDeltaMovement();
                    if (dv.lengthSqr() > 1e-4) {
                        presa.move(net.minecraft.world.entity.MoverType.SELF, dv);
                        presa.setDeltaMovement(dv.x * 0.55, presa.onGround() ? 0 : dv.y - 0.08, dv.z * 0.55);
                    }
                    cerca = Math.min(cerca, Math.hypot(presa.getX() - a.getX(), presa.getZ() - a.getZ()));
                    frenada |= presa.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
                    presa.invulnerableTime = 0;
                }
                sb.append(String.format(java.util.Locale.ROOT, " %s (%s) a %.1f: la presa -%.1f%s%s;", nombres[i], a.tipo(), dist[i], vida - presa.getHealth(),
                        i == 1 || i == 4 || i == 5 ? ", frenada " + frenada : "", i == 2 ? String.format(java.util.Locale.ROOT, ", arrastrada hasta %.1f", cerca) : ""));
                a.discard();
                presa.discard();
            }
            sb.append(String.format(java.util.Locale.ROOT, " redes +%d (enredan +%d), garfios +%d (tiron %.2f), costras +%d (frenan +%d), punados de sal +%d (salan +%d)",
                    com.darkcraft.enemigos.Salobre.REDES - redes, com.darkcraft.enemigos.Red.PILLAN - pillan, com.darkcraft.enemigos.Salobre.GARFIOS - garf,
                    com.darkcraft.enemigos.Salobre.ARRASTRE, com.darkcraft.enemigos.Salobre.COSTRAS - cos, com.darkcraft.enemigos.Salobre.FRENADOS - fre,
                    com.darkcraft.enemigos.Salobre.SALES - sal, com.darkcraft.enemigos.Salobre.SALADOS - sald));
            // ---- uno que cae: se deshace en sal
            var c = DarkCraftMod.SALOBRE.get().create(m);
            if (c != null) {
                c.variante(com.darkcraft.enemigos.Salobre.SALINERO);
                c.moveTo(gx, PISO, gz, 0f, 0f);
                m.addFreshEntity(c);
                quitar.add(c);
                int des = com.darkcraft.enemigos.Salobre.DESHECHOS;
                c.hurt(m.damageSources().generic(), 9999f);
                int k = 0;
                for (; k < 60 && !c.isRemoved(); k++) c.tick();
                sb.append(String.format(java.util.Locale.ROOT, "; uno que cae se deshace en sal +%d y se va en %d ticks", com.darkcraft.enemigos.Salobre.DESHECHOS - des, k));
            }
            log.println(sb);
        } catch (Throwable t) {
            log.println("Morvain, los salobres: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] salobres", t);
        } finally {
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    /** Lo que dejan los penitentes (las nubes de incienso): un tick, a mano (la prueba no mueve el mundo). */
    private static void tickIncienso(ServerLevel m, net.minecraft.world.phys.AABB caja, java.util.List<net.minecraft.world.entity.Entity> quitar) {
        for (var e : m.getEntitiesOfClass(com.darkcraft.enemigos.Incienso.class, caja)) {
            if (e.isRemoved()) continue;
            if (!quitar.contains(e)) quitar.add(e);
            e.tickCount++;
            e.tick();
        }
    }

    /**
     * Parte 7s: el Puente de la Grieta (a pie desde la entrada, junto al santuario, a todos sus sitios y a cada penitente; el atril con
     * el libro 28, el arcon, la campana, el tablero del puente, el hueco y la sangre de la grieta, el arco de la puerta y la imagen del
     * paso) y los penitentes: en sus puestos (rezando, postrados o de pie), uno de rodillas que se levanta y uno postrado que se alza;
     * cada accion de cada uno acaba; sus golpes a una presa (el azote y los tres azotes que hacen sangrar hasta la hemorragia, la
     * penitencia y el fervor, el cruzazo con su onda, el barrido, la embestida, el incensario que quema, el humo que hace sangrar y el
     * cantico que cura a los suyos) y uno que cae y se desangra. Al acabar, todo como estaba.
     */
    private static void grieta(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.GRIETA;
        var A = com.darkcraft.morvain.Grieta.get();
        final int CX = com.darkcraft.morvain.Grieta.CX, CZ = com.darkcraft.morvain.Grieta.CZ, PISO = com.darkcraft.morvain.Grieta.PISO;
        try {
            long t0 = System.currentTimeMillis();
            int nch = 0;
            for (long k : A.chunks()) {
                m.getChunk((int) (k >> 32), (int) k);
                nch++;
            }
            long ms = System.currentTimeMillis() - t0;
            var aire = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
                var st = m.getBlockState(new BlockPos(x, y, z));
                return st.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock ? aire : st;
            };
            int[] en = A.sitio("entrada");
            var zona = com.darkcraft.pueblo.gen.Andar.desde(mundo, en[0], en[1], en[2], CX - 125, CZ - 76, CX + 108, CZ + 92, 6_000_000);
            StringBuilder ap = new StringBuilder();
            int llega = 0, sitios = 0;
            java.util.Set<String> mirar = java.util.Set.of("campana", "alto", "fondo");
            for (var e : A.sitios().entrySet()) {
                if (mirar.contains(e.getKey())) continue;
                sitios++;
                int[] q = e.getValue();
                if (zona.alcanza(q[0], q[1], q[2], 2)) llega++;
                else ap.append(' ').append(e.getKey());
            }
            int lejos = 0;
            for (var o : A.esperas()) if (!zona.alcanza(o.x(), o.y(), o.z(), 2)) lejos++;
            var sc = com.darkcraft.morvain.SantuariosMorvain.porId("grieta");
            log.println("Morvain, el puente de la grieta: " + nch + " chunks en " + ms + " ms; " + com.darkcraft.morvain.Grieta.resumen(m)
                    + "; santuario " + com.darkcraft.santuario.Santuarios.nombre(m, sc.pos()) + " (" + m.getBlockState(sc.pos()).getBlock().getDescriptionId()
                    + ") a " + Math.round(com.darkcraft.morvain.Grieta.distancia(sc.x(), sc.z())) + " del puente; a pie desde la entrada (" + zona.celdas()
                    + " celdas) a " + llega + " de " + sitios + " sitios" + (ap.length() == 0 ? "" : ", no a:" + ap) + "; penitentes a los que no se llega "
                    + lejos + " de " + A.esperas().size() + "; lugar en el puente " + (com.darkcraft.morvain.Grieta.dentro(CX, CZ) ? "grieta" : "NO")
                    + ", en medio del puente " + com.darkcraft.morvain.Grieta.enPuente(CX + 0.5, CZ + 0.5) + ", en la cabeza del sur "
                    + com.darkcraft.morvain.Grieta.enPuente(CX + 0.5, CZ + 18.5) + " (tiene que ser false)");
            topView(m, CX + 10, CZ, 100, new File(out, "morvain_grieta.png"));
            topView(m, CX, CZ - 4, 30, new File(out, "morvain_grieta_puente.png"));
        } catch (Throwable t) {
            log.println("Morvain, el puente de la grieta: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] grieta", t);
        }
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        try {
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            for (int k = 0; k < 10; k++)
                for (var e : fuera) if (e.isAlive()) e.tick();
            int rezando = 0, postrados = 0, dePie = 0, enSuelo = 0, chocan = 0;
            int[] porVariante = new int[3];
            StringBuilder mal = new StringBuilder();
            com.darkcraft.enemigos.Penitente rodillas = null, postrado = null;
            for (var e : fuera) {
                if (!(e instanceof com.darkcraft.enemigos.Penitente c)) continue;
                porVariante[Math.max(0, Math.min(2, c.variante()))]++;
                if (!m.noCollision(c)) {
                    chocan++;
                    if (mal.length() < 300) mal.append(" metido en un bloque en ").append(c.blockPosition().toShortString()).append(';');
                }
                if (c.postura() == com.darkcraft.enemigos.Enemigo.REZA) {
                    rezando++;
                    if (rodillas == null) rodillas = c;
                } else if (c.postura() == com.darkcraft.enemigos.Enemigo.TUMBADO) {
                    postrados++;
                    if (postrado == null) postrado = c;
                } else dePie++;
                var abajo = m.getBlockState(BlockPos.containing(c.getX(), c.getY() - 0.2, c.getZ()));
                if (!abajo.getCollisionShape(m, BlockPos.ZERO).isEmpty()) enSuelo++;
                else if (mal.length() < 400) mal.append(' ').append(c.tipo()).append(" en el aire en ").append(c.blockPosition().toShortString()).append(';');
            }
            log.println("Morvain, los penitentes: " + com.darkcraft.morvain.MorvainEnemigos.comprobar() + "; fuera " + fuera.size() + " de "
                    + com.darkcraft.morvain.MorvainEnemigos.grieta().size() + " (flagelantes " + porVariante[0] + ", portadores " + porVariante[1]
                    + ", turiferarios " + porVariante[2] + "): rezando " + rezando + ", postrados " + postrados + ", de pie " + dePie
                    + " (en su suelo " + enSuelo + "); metidos en un bloque " + chocan + " (tiene que ser 0)" + mal);
            StringBuilder sb = new StringBuilder("Morvain, los penitentes en accion:");
            // ---- el de rodillas se levanta y el postrado se alza (una presa a su lado)
            int alz = com.darkcraft.enemigos.Penitente.ALZADOS;
            for (var c : new com.darkcraft.enemigos.Penitente[]{rodillas, postrado}) {
                if (c == null) {
                    sb.append(" NO HAY ").append(c == rodillas ? "NINGUNO DE RODILLAS;" : "NINGUNO POSTRADO;");
                    continue;
                }
                var presa = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
                if (presa == null) continue;
                presa.moveTo(c.getX() + 1.5, c.getY(), c.getZ() + 0.5, 0f, 0f);
                presa.setNoAi(true);
                m.addFreshEntity(presa);
                quitar.add(presa);
                int antes = c.postura();
                c.setTarget(presa);
                int sale = -1;
                for (int k = 0; k < 80; k++) {
                    c.tickCount++;
                    c.tick();
                    int a = c.accion();
                    if (sale < 0 && (a == com.darkcraft.enemigos.PenitenteAnims.LEVANTARSE || a == com.darkcraft.enemigos.PenitenteAnims.ALZARSE)) sale = k;
                    if (sale >= 0 && a == 0) break;
                }
                sb.append(String.format(java.util.Locale.ROOT, " el %s (%s, en %s) se %s en el tick %d y queda de pie %s;", c.tipo(),
                        antes == com.darkcraft.enemigos.Enemigo.REZA ? "de rodillas" : "postrado", c.blockPosition().toShortString(),
                        antes == com.darkcraft.enemigos.Enemigo.REZA ? "levanta" : "alza", sale, c.postura() == com.darkcraft.enemigos.Enemigo.DE_PIE));
                c.setTarget(null);
            }
            sb.append(" alzados de la postracion +").append(com.darkcraft.enemigos.Penitente.ALZADOS - alz).append(';');
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio)) e.discard();
            // ---- en el campamento (de cara al sur): uno de cada, cada accion acaba
            for (int v2 = 0; v2 < 3; v2++) {
                var a = DarkCraftMod.PENITENTE.get().create(m);
                if (a == null) continue;
                a.variante(v2);
                a.moveTo(CX + 24.5 + v2 * 5, PISO, CZ + 27.5, 0f, 0f);
                m.addFreshEntity(a);
                quitar.add(a);
                StringBuilder no = new StringBuilder();
                for (int ac = 1; ac < com.darkcraft.enemigos.PenitenteAnims.TICKS.length; ac++) {
                    String nombre = com.darkcraft.enemigos.PenitenteAnims.NOMBRES[ac];
                    if (nombre.equals("morir")) continue;
                    a.empezar(ac);
                    for (int k = 0; k < 90 && a.accion() == ac; k++) {
                        a.tickCount++;
                        a.tick();
                    }
                    if (a.accion() == ac) no.append(' ').append(nombre);
                    for (int k = 0; k < 10; k++) a.tick();
                }
                sb.append(' ').append(a.tipo()).append(String.format(java.util.Locale.ROOT, " (vida %.0f, golpe %.1f, armadura %d)", a.getMaxHealth(),
                        a.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE), a.getArmorValue()))
                        .append(": acciones que no acaban").append(no.length() == 0 ? " ninguna" : no).append(';');
                for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Incienso.class, a.getBoundingBox().inflate(24))) f.discard();
                a.discard();
            }
            log.println(sb);
            // ---- contra una presa (un aldeano quieto en el campamento, al este del penitente)
            sb = new StringBuilder("Morvain, los penitentes contra una presa:");
            int azo = com.darkcraft.enemigos.Penitente.AZOTES, hem = com.darkcraft.enemigos.Sangrado.HEMORRAGIAS, pen = com.darkcraft.enemigos.Penitente.PENITENCIAS,
                    fer = com.darkcraft.enemigos.Penitente.ENFERVORIZADOS, ond = com.darkcraft.enemigos.Penitente.ONDAS, emb = com.darkcraft.enemigos.Penitente.EMBESTIDAS,
                    hum = com.darkcraft.enemigos.Penitente.HUMOS, san = com.darkcraft.enemigos.Incienso.SANGRAN, can = com.darkcraft.enemigos.Penitente.CANTICOS,
                    cur = com.darkcraft.enemigos.Penitente.CURADOS;
            String[] nombres = {"azote", "azotes", "penitencia", "cruzazo", "barrido", "embestida", "incensario", "humo", "cantico"};
            int[] acciones = {com.darkcraft.enemigos.PenitenteAnims.AZOTE, com.darkcraft.enemigos.PenitenteAnims.AZOTES, com.darkcraft.enemigos.PenitenteAnims.PENITENCIA,
                    com.darkcraft.enemigos.PenitenteAnims.CRUZAZO, com.darkcraft.enemigos.PenitenteAnims.BARRIDO, com.darkcraft.enemigos.PenitenteAnims.EMBESTIDA,
                    com.darkcraft.enemigos.PenitenteAnims.INCENSARIO, com.darkcraft.enemigos.PenitenteAnims.HUMO, com.darkcraft.enemigos.PenitenteAnims.CANTICO};
            int F = com.darkcraft.enemigos.Penitente.FLAGELANTE, P = com.darkcraft.enemigos.Penitente.PORTADOR, T = com.darkcraft.enemigos.Penitente.TURIFERARIO;
            int[] variantes = {F, F, F, P, P, P, T, T, T};
            double[] dist = {2.2, 2.2, 6.0, 2.6, 2.4, 6.5, 2.4, 4.0, 5.0};
            int[] dura = {40, 60, 70, 50, 50, 60, 50, 140, 70};
            final double gx = CX + 20.5, gz = CZ + 33.5;
            for (int i = 0; i < nombres.length; i++) {
                var a = DarkCraftMod.PENITENTE.get().create(m);
                var presa = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
                if (a == null || presa == null) continue;
                a.variante(variantes[i]);
                a.moveTo(gx, PISO, gz, -90f, 0f);
                presa.moveTo(gx + dist[i], PISO, gz, 90f, 0f);
                presa.setNoAi(true);
                m.addFreshEntity(a);
                m.addFreshEntity(presa);
                quitar.add(a);
                quitar.add(presa);
                com.darkcraft.enemigos.Sangrado.limpiar(presa);
                // el cantico: un flagelante herido al lado
                com.darkcraft.enemigos.Penitente herido = null;
                if (acciones[i] == com.darkcraft.enemigos.PenitenteAnims.CANTICO) {
                    herido = DarkCraftMod.PENITENTE.get().create(m);
                    if (herido != null) {
                        herido.variante(F);
                        herido.moveTo(gx - 2.5, PISO, gz + 1.5, 0f, 0f);
                        m.addFreshEntity(herido);
                        quitar.add(herido);
                        herido.setHealth(herido.getMaxHealth() * 0.4f);
                    }
                }
                if (acciones[i] == com.darkcraft.enemigos.PenitenteAnims.PENITENCIA) a.setHealth(a.getMaxHealth() * 0.5f);
                a.setTarget(presa);
                a.setYRot(-90f);
                a.yBodyRot = a.yHeadRot = -90f;
                a.empezar(acciones[i]);
                float vida = presa.getHealth(), vidaA = a.getHealth(), vidaH = herido != null ? herido.getHealth() : 0f;
                float sangre = 0f;
                boolean frenada = false, ardiendo = false;
                for (int k = 0; k < dura[i]; k++) {
                    if (!a.isRemoved()) {
                        a.tickCount++;
                        a.tick();
                    }
                    if (herido != null && !herido.isRemoved()) herido.tick();
                    tickIncienso(m, presa.getBoundingBox().inflate(16), quitar);
                    // la presa se mueve con lo que le den (los empujones): a mano, que sin IA no anda
                    net.minecraft.world.phys.Vec3 dv = presa.getDeltaMovement();
                    if (dv.lengthSqr() > 1e-4) {
                        presa.move(net.minecraft.world.entity.MoverType.SELF, dv);
                        presa.setDeltaMovement(dv.x * 0.55, presa.onGround() ? 0 : dv.y - 0.08, dv.z * 0.55);
                    }
                    // pisa el suelo (para la onda del cruzazo: sin IA ni mundo que se mueva, el juego no lo apunta)
                    if (Math.abs(presa.getY() - PISO) < 0.05) presa.setOnGround(true);
                    sangre = Math.max(sangre, com.darkcraft.enemigos.Sangrado.nivel(presa));
                    frenada |= presa.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
                    ardiendo |= presa.isOnFire();
                    presa.invulnerableTime = 0;
                }
                String extra = switch (nombres[i]) {
                    case "azote", "azotes" -> String.format(java.util.Locale.ROOT, ", sangre %.0f", sangre);
                    case "penitencia" -> String.format(java.util.Locale.ROOT, ", el penitente -%.1f, enfervorizado %s", vidaA - a.getHealth(), a.animo() > 0);
                    case "incensario" -> ", ardiendo " + ardiendo;
                    case "humo" -> String.format(java.util.Locale.ROOT, ", sangre %.0f, frenada %s", sangre, frenada);
                    case "cantico" -> String.format(java.util.Locale.ROOT, ", el herido +%.1f", herido != null ? herido.getHealth() - vidaH : 0f);
                    default -> "";
                };
                sb.append(String.format(java.util.Locale.ROOT, " %s (%s) a %.1f: la presa -%.1f%s;", nombres[i], a.tipo(), dist[i], vida - presa.getHealth(), extra));
                if (herido != null) herido.discard();
                for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Incienso.class, presa.getBoundingBox().inflate(24))) f.discard();
                a.discard();
                presa.discard();
            }
            sb.append(String.format(java.util.Locale.ROOT, " azotes +%d, hemorragias +%d, penitencias +%d (enfervorizados +%d), ondas del cruzazo +%d, embestidas +%d,"
                            + " humos +%d (hacen sangrar +%d), canticos +%d (curan +%d)",
                    com.darkcraft.enemigos.Penitente.AZOTES - azo, com.darkcraft.enemigos.Sangrado.HEMORRAGIAS - hem, com.darkcraft.enemigos.Penitente.PENITENCIAS - pen,
                    com.darkcraft.enemigos.Penitente.ENFERVORIZADOS - fer, com.darkcraft.enemigos.Penitente.ONDAS - ond, com.darkcraft.enemigos.Penitente.EMBESTIDAS - emb,
                    com.darkcraft.enemigos.Penitente.HUMOS - hum, com.darkcraft.enemigos.Incienso.SANGRAN - san, com.darkcraft.enemigos.Penitente.CANTICOS - can,
                    com.darkcraft.enemigos.Penitente.CURADOS - cur));
            // ---- tres azotes seguidos: la hemorragia
            var fl = DarkCraftMod.PENITENTE.get().create(m);
            var presa = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
            if (fl != null && presa != null) {
                fl.variante(F);
                fl.moveTo(gx, PISO, gz, -90f, 0f);
                presa.moveTo(gx + 2.2, PISO, gz, 90f, 0f);
                presa.setNoAi(true);
                m.addFreshEntity(fl);
                m.addFreshEntity(presa);
                quitar.add(fl);
                quitar.add(presa);
                com.darkcraft.enemigos.Sangrado.limpiar(presa);
                int h0 = com.darkcraft.enemigos.Sangrado.HEMORRAGIAS;
                fl.setTarget(presa);
                for (int vez = 0; vez < 3; vez++) {
                    fl.setYRot(-90f);
                    fl.yBodyRot = fl.yHeadRot = -90f;
                    fl.empezar(com.darkcraft.enemigos.PenitenteAnims.AZOTES);
                    for (int k = 0; k < 46; k++) {
                        fl.tickCount++;
                        fl.tick();
                        presa.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                        presa.moveTo(gx + 2.2, PISO, gz, 90f, 0f);
                        presa.invulnerableTime = 0;
                        presa.setHealth(presa.getMaxHealth());
                    }
                }
                sb.append(String.format(java.util.Locale.ROOT, "; tres tandas de azotes: hemorragias +%d", com.darkcraft.enemigos.Sangrado.HEMORRAGIAS - h0));
                fl.discard();
                presa.discard();
            }
            // ---- uno que cae: se desangra
            var c = DarkCraftMod.PENITENTE.get().create(m);
            if (c != null) {
                c.variante(P);
                c.moveTo(gx, PISO, gz, 0f, 0f);
                m.addFreshEntity(c);
                quitar.add(c);
                int des = com.darkcraft.enemigos.Penitente.DESANGRADOS;
                c.hurt(m.damageSources().generic(), 9999f);
                int k = 0;
                for (; k < 80 && !c.isRemoved(); k++) c.tick();
                sb.append(String.format(java.util.Locale.ROOT, "; uno que cae se desangra +%d y se va en %d ticks", com.darkcraft.enemigos.Penitente.DESANGRADOS - des, k));
            }
            log.println(sb);
        } catch (Throwable t) {
            log.println("Morvain, los penitentes: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] penitentes", t);
        } finally {
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    /** Lo que deja Brann (el arpon, los faroles, sus charcos ardiendo y la Vieja): un tick, a mano (la prueba no mueve el mundo). */
    private static boolean tickBallenero(ServerLevel m, net.minecraft.world.phys.AABB caja, java.util.List<net.minecraft.world.entity.Entity> quitar) {
        java.util.List<net.minecraft.world.entity.Entity> l = new java.util.ArrayList<>();
        l.addAll(m.getEntitiesOfClass(com.darkcraft.enemigos.ArponBallenero.class, caja));
        l.addAll(m.getEntitiesOfClass(com.darkcraft.enemigos.FarolAceite.class, caja));
        l.addAll(m.getEntitiesOfClass(com.darkcraft.enemigos.Cantaro.Llamas.class, caja));
        l.addAll(m.getEntitiesOfClass(com.darkcraft.enemigos.Vieja.class, caja));
        boolean hay = false;
        for (var e : l) {
            if (e.isRemoved()) continue;
            if (!quitar.contains(e)) quitar.add(e);
            e.tickCount++;
            e.tick();
            hay = true;
        }
        return hay;
    }

    private static void limpiarBallenero(ServerLevel m, net.minecraft.world.phys.AABB caja) {
        var grande = caja.inflate(20, 60, 20);
        for (var e : m.getEntitiesOfClass(com.darkcraft.enemigos.ArponBallenero.class, grande)) e.discard();
        for (var e : m.getEntitiesOfClass(com.darkcraft.enemigos.FarolAceite.class, grande)) e.discard();
        for (var e : m.getEntitiesOfClass(com.darkcraft.enemigos.Cantaro.Llamas.class, grande)) e.discard();
        for (var e : m.getEntitiesOfClass(com.darkcraft.enemigos.Vieja.class, grande)) e.discard();
    }

    /**
     * Parte 7r: el Costillar de la Ballena (a pie desde el santuario a todos sus sitios; el atril con el libro 27, el arcon, el
     * espinazo, las costillas en la arena, la calavera y el suelo de la sala libre) y Brann, el Ballenero: de vigia en su sitio con
     * los salobres de alrededor; al ver a alguien bajo las costillas habla mientras se levanta y grita; cada accion acaba; sus
     * golpes a una presa (el arponazo, el tajo doble, el arpon que se clava y el tiron que la arrastra, el farol que revienta y deja
     * el suelo ardiendo, el salto con su onda y la Vieja que sale de la arena y embiste a lo largo de la sala); a media vida llama a
     * la Vieja y la vuelve a llamar; y al caer, la Vieja sube al cielo, el se deshace en sal y vuelve la marea: los salobres que
     * faenaban de rodillas (aqui y en el Puerto Varado) se deshacen en sal y ya no vuelven. Al acabar, todo como estaba.
     */
    private static void costillar(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.COSTILLAR, puerto = com.darkcraft.morvain.MorvainEnemigos.PUERTO;
        var A = com.darkcraft.morvain.Costillar.get();
        final int CX = com.darkcraft.morvain.Costillar.CX, CZ = com.darkcraft.morvain.Costillar.CZ, PISO = com.darkcraft.morvain.Costillar.PISO;
        try {
            long t0 = System.currentTimeMillis();
            int nch = 0;
            for (long k : A.chunks()) {
                m.getChunk((int) (k >> 32), (int) k);
                nch++;
            }
            long ms = System.currentTimeMillis() - t0;
            // a pie desde la entrada, junto al santuario (las capas finas de nieve —la sal— no estorban)
            var aire = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
                var st = m.getBlockState(new BlockPos(x, y, z));
                return st.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock || st.getBlock() instanceof net.minecraft.world.level.block.SnowLayerBlock
                        && st.getValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS) <= 4 ? aire : st;
            };
            int[] en = A.sitio("entrada");
            var zona = com.darkcraft.pueblo.gen.Andar.desde(mundo, en[0], en[1], en[2], com.darkcraft.morvain.Costillar.X0, com.darkcraft.morvain.Costillar.Z0,
                    com.darkcraft.morvain.Costillar.X1, com.darkcraft.morvain.Costillar.Z1, 4_000_000);
            StringBuilder no = new StringBuilder();
            int llega = 0, sitios = 0;
            for (var e : A.sitios().entrySet()) {
                // lo alto de la calavera es solo para mirar
                if (e.getKey().equals("alto")) continue;
                sitios++;
                int[] q = e.getValue();
                if (zona.alcanza(q[0], q[1], q[2], 2)) llega++;
                else no.append(' ').append(e.getKey());
            }
            int lejos = 0;
            for (var o : A.esperas()) if (!zona.alcanza(o.x(), o.y(), o.z(), 2)) lejos++;
            var sc = com.darkcraft.morvain.SantuariosMorvain.porId("costillar");
            log.println("Morvain, el costillar: " + nch + " chunks en " + ms + " ms; " + com.darkcraft.morvain.Costillar.resumen(m) + "; costillas " + A.costillas
                    + " (rotas " + A.rotas + "), vertebras " + A.vertebras + ", huesos de la cola " + A.huesosCola + ", dedos " + A.dedos + ", barriles " + A.barriles
                    + ", arpones " + A.arpones + "; santuario " + com.darkcraft.santuario.Santuarios.nombre(m, sc.pos()) + " ("
                    + m.getBlockState(sc.pos()).getBlock().getDescriptionId() + ") a " + Math.round(Math.hypot(sc.x() - en[0], sc.z() - en[2]))
                    + " de la entrada; a pie desde la entrada (" + zona.celdas() + " celdas) a " + llega + " de " + sitios + " sitios" + (no.length() == 0 ? "" : ", no a:" + no)
                    + "; los que esperan a los que no se llega " + lejos + " de " + A.esperas().size() + "; lugar en la sala "
                    + (com.darkcraft.morvain.Costillar.dentro(CX, CZ) ? "costillar" : "NO") + ", bajo las costillas " + com.darkcraft.morvain.Costillar.enSala(CX + 0.5, CZ + 0.5)
                    + ", en el santuario " + com.darkcraft.morvain.Costillar.dentro(sc.x(), sc.z()) + " (tiene que ser false)");
            topView(m, CX + 9, CZ, 70, new File(out, "morvain_costillar.png"));
            topView(m, CX - 2, CZ, 30, new File(out, "morvain_costillar_sala.png"));
        } catch (Throwable t) {
            log.println("Morvain, el costillar: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] costillar", t);
        }
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        final net.minecraft.world.phys.AABB caja = com.darkcraft.enemigos.Ballenero.caja();
        com.darkcraft.enemigos.Ballenero br = null;
        try {
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            int suyos = 0, rodillas = 0, enSuelo = 0, chocan = 0;
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            for (int k = 0; k < 6; k++)
                for (var e : fuera) if (e.isAlive() && !(e instanceof com.darkcraft.enemigos.Ballenero)) e.tick();
            for (var e : fuera) {
                if (e instanceof com.darkcraft.enemigos.Ballenero x) br = x;
                else {
                    suyos++;
                    if (e.postura() == com.darkcraft.enemigos.Enemigo.REZA) rodillas++;
                }
                if (!m.noCollision(e)) chocan++;
                if (!m.getBlockState(BlockPos.containing(e.getX(), e.getY() - 0.2, e.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty()) enSuelo++;
            }
            if (br == null) {
                log.println("Morvain, el Ballenero: NO SALE");
                return;
            }
            for (var e : fuera) if (e != br) e.discard();
            StringBuilder sb = new StringBuilder("Morvain, el Ballenero: en " + br.blockPosition().toShortString() + " postura " + br.postura() + " (1 de vigia), giro "
                    + Math.round(br.getYRot()) + ", vida " + Math.round(br.getMaxHealth()) + ", caja " + br.getBbWidth() + " x " + br.getBbHeight() + ", bajo las costillas "
                    + com.darkcraft.morvain.Costillar.enSala(br.getX(), br.getZ()) + ", musica " + br.musica() + "; los suyos " + suyos + " (de rodillas " + rodillas
                    + "); en su suelo " + enSuelo + " de " + fuera.size() + ", metidos en un bloque " + chocan + " (tiene que ser 0)");
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("5b7c9d1e-3f40-4b6c-8d8e-2f3a4b5c6d9a"), "[DarkCraftCI10]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            // ---- le ve bajo las costillas (a su espalda): habla mientras se levanta y grita al acabar
            final double fxx = CX - 9.5, fyy = PISO, fzz = CZ + 0.5;
            fp.moveTo(fxx, fyy, fzz, -90f, 0f);
            fp.setOnGround(true);
            br.setTarget(fp);
            int levanta = -1, grita = -1, ataca = -1, quien = 0;
            for (int k = 0; k < 400; k++) {
                br.tickCount++;
                br.tick();
                tickBallenero(m, caja, quitar);
                fp.moveTo(fxx, fyy, fzz, -90f, 0f);
                fp.setHealth(fp.getMaxHealth());
                int a = br.accion();
                if (levanta < 0 && a == com.darkcraft.enemigos.BalleneroAnims.LEVANTARSE) levanta = k;
                if (grita < 0 && a == com.darkcraft.enemigos.BalleneroAnims.GRITO) grita = k;
                if (grita >= 0 && ataca < 0 && a != 0 && a != com.darkcraft.enemigos.BalleneroAnims.GRITO) {
                    ataca = k;
                    quien = a;
                    break;
                }
            }
            sb.append("; al verle: pelea ").append(br.enPelea()).append(", se levanta en el tick ").append(levanta).append(", grita en el ").append(grita)
                    .append(" (la frase 1 dura ").append(com.darkcraft.rey.Voces.ticks(com.darkcraft.enemigos.Ballenero.VOZ_BASE + 1)).append("), y luego ")
                    .append(quien == 0 ? "nada" : com.darkcraft.enemigos.BalleneroAnims.NOMBRES[quien]).append(" en el ").append(ataca)
                    .append(String.format(java.util.Locale.ROOT, ", de pie %s a %.1f de su presa", br.postura() == com.darkcraft.enemigos.Enemigo.DE_PIE,
                            Math.hypot(br.getX() - fp.getX(), br.getZ() - fp.getZ())));
            // ---- cada accion acaba (sin presa y quieto: no elige ni grita por su cuenta; el tiron va tras el arpon)
            br.quieta = true;
            br.setTarget(null);
            for (int k = 0; k < 120 && (br.accion() != 0 || br.arponFuera()); k++) {
                br.tickCount++;
                br.tick();
                tickBallenero(m, caja, quitar);
            }
            limpiarBallenero(m, caja);
            StringBuilder nn = new StringBuilder();
            int vistas = 0;
            for (int a = 1; a < com.darkcraft.enemigos.BalleneroAnims.TICKS.length; a++) {
                String nombre = com.darkcraft.enemigos.BalleneroAnims.NOMBRES[a];
                if (nombre.equals("morir") || nombre.equals("levantarse")) continue;
                br.moveTo(CX + 0.5, PISO, CZ + 0.5, -90f, 0f);
                br.empezar(a);
                vistas++;
                for (int k = 0; k < 160 && br.accion() == a; k++) {
                    br.tickCount++;
                    br.tick();
                    tickBallenero(m, caja, quitar);
                }
                if (br.accion() == a) nn.append(' ').append(nombre);
            }
            int vuelve = 0;
            for (; vuelve < 120 && (br.accion() != 0 || br.arponFuera()); vuelve++) {
                br.tickCount++;
                br.tick();
                tickBallenero(m, caja, quitar);
            }
            for (int k = 0; k < 200 && tickBallenero(m, caja, quitar); k++) {
            }
            limpiarBallenero(m, caja);
            sb.append("; de ").append(vistas).append(" acciones no acaban:").append(nn.length() == 0 ? " ninguna" : nn).append(" (y el arpon vuelve a la mano en ")
                    .append(vuelve).append(" ticks: ").append(!br.arponFuera()).append(')');
            log.println(sb);
            // ---- sus golpes a una presa (un aldeano quieto y duro) bajo las costillas: el mirando al este, hacia la calavera
            StringBuilder sg = new StringBuilder("Morvain, los golpes del Ballenero a una presa:");
            final double x0 = CX - 12.5, y0 = PISO, z0 = CZ + 0.5;
            var victima = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
            if (victima != null) {
                victima.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(900.0);
                victima.setHealth(900f);
                victima.setNoAi(true);
                victima.moveTo(x0 + 3, y0, z0, 90f, 0f);
                m.addFreshEntity(victima);
                quitar.add(victima);
                String[] golpes = {"arponazo", "tajo", "arpon", "farol", "salto", "la Vieja"};
                int[] acc = {com.darkcraft.enemigos.BalleneroAnims.ARPONAZO, com.darkcraft.enemigos.BalleneroAnims.TAJO, com.darkcraft.enemigos.BalleneroAnims.ARPON,
                        com.darkcraft.enemigos.BalleneroAnims.FAROL, com.darkcraft.enemigos.BalleneroAnims.SALTO, com.darkcraft.enemigos.BalleneroAnims.LLAMAR};
                double[] lejos = {3.6, 2.8, 12.0, 9.0, 6.5, 8.0};
                int[] dura = {70, 80, 160, 200, 100, 240};
                for (int i = 0; i < golpes.length; i++) {
                    final boolean salta = acc[i] == com.darkcraft.enemigos.BalleneroAnims.SALTO;
                    victima.setHealth(victima.getMaxHealth());
                    victima.invulnerableTime = 0;
                    victima.hurtTime = 0;
                    victima.removeAllEffects();
                    victima.clearFire();
                    victima.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    victima.moveTo(x0 + lejos[i], y0, z0, 90f, 0f);
                    victima.setOnGround(true);
                    int arp = br.lanzados(), cla = com.darkcraft.enemigos.ArponBallenero.CLAVAN, tir = br.arrastrados(), far = br.faroles(),
                            rev = com.darkcraft.enemigos.FarolAceite.REVIENTAN, fdan = com.darkcraft.enemigos.FarolAceite.DAN,
                            que = com.darkcraft.enemigos.Cantaro.LLAMAS_QUEMAN, sal = br.saltos(), ond = br.ondasQueDan(), vsal = com.darkcraft.enemigos.Vieja.SALEN,
                            vemb = com.darkcraft.enemigos.Vieja.EMBISTEN, vdan = com.darkcraft.enemigos.Vieja.DAN, lla = br.llamadas(), apz = br.arponazos(), taj = br.tajos();
                    br.moveTo(x0, y0, z0, -90f, 0f);
                    br.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    br.setTarget(victima);
                    br.setYRot(-90f);
                    br.yBodyRot = br.yHeadRot = -90f;
                    br.empezar(acc[i]);
                    float antes = victima.getHealth();
                    double cerca = lejos[i], alto = 0, cae = 0;
                    boolean ardiendo = false;
                    String camino = "";
                    for (int k = 0; k < dura[i]; k++) {
                        boolean el = br.accion() != 0 || br.arponFuera();
                        if (el) {
                            br.setTarget(victima);
                            br.tickCount++;
                            br.tick();
                            if (!salta) br.moveTo(x0, y0, z0, -90f, 0f);
                            else alto = Math.max(alto, br.getY() - y0);
                        }
                        if (salta) victima.setOnGround(true);
                        boolean otros = tickBallenero(m, caja, quitar);
                        if (camino.isEmpty())
                            for (var v : m.getEntitiesOfClass(com.darkcraft.enemigos.Vieja.class, caja)) {
                                var c = v.camino();
                                camino = String.format(java.util.Locale.ROOT, " (de u %.0f a u %.0f, en v %.1f)", c[0].x - CX, c[1].x - CX, c[0].z - CZ - 0.5);
                            }
                        // la presa se mueve con lo que le den (el tiron, la onda, la Vieja): a mano, que sin IA no anda
                        net.minecraft.world.phys.Vec3 dv = victima.getDeltaMovement();
                        if (dv.lengthSqr() > 1e-4) {
                            victima.move(net.minecraft.world.entity.MoverType.SELF, dv);
                            victima.setDeltaMovement(dv.x * 0.55, victima.onGround() ? 0 : dv.y - 0.08, dv.z * 0.55);
                        }
                        victima.invulnerableTime = 0;
                        ardiendo |= victima.isOnFire();
                        cerca = Math.min(cerca, Math.hypot(victima.getX() - br.getX(), victima.getZ() - br.getZ()));
                        if (salta && br.onGround() && k > 12) cae = Math.hypot(br.getX() - victima.getX(), br.getZ() - victima.getZ());
                        if (!el && !otros && k > 2) break;
                    }
                    sg.append(' ').append(golpes[i]).append(String.format(java.util.Locale.ROOT, " a %.1f: -%.1f", lejos[i], antes - victima.getHealth()));
                    switch (i) {
                        case 0 -> sg.append(", arponazos +").append(br.arponazos() - apz).append(victima.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN) ? ", sangrando" : "");
                        case 1 -> sg.append(", tajos +").append(br.tajos() - taj);
                        case 2 -> sg.append(", arpones +").append(br.lanzados() - arp).append(" (se clavan +").append(com.darkcraft.enemigos.ArponBallenero.CLAVAN - cla)
                                .append("), tirones que arrastran +").append(br.arrastrados() - tir)
                                .append(String.format(java.util.Locale.ROOT, " (hasta %.1f de el; tiron %.2f), el arpon en la mano %s", cerca, br.arrastre(), !br.arponFuera()));
                        case 3 -> sg.append(", faroles +").append(br.faroles() - far).append(" (revientan +").append(com.darkcraft.enemigos.FarolAceite.REVIENTAN - rev)
                                .append(", dan +").append(com.darkcraft.enemigos.FarolAceite.DAN - fdan).append("), el charco quema +")
                                .append(com.darkcraft.enemigos.Cantaro.LLAMAS_QUEMAN - que).append(ardiendo ? ", ardiendo" : "");
                        case 4 -> sg.append(", saltos +").append(br.saltos() - sal).append(String.format(java.util.Locale.ROOT, " (sube %.1f, cae a %.1f de la presa)", alto, cae))
                                .append(", la onda da +").append(br.ondasQueDan() - ond);
                        case 5 -> sg.append(", llamadas +").append(br.llamadas() - lla).append(", la Vieja sale +").append(com.darkcraft.enemigos.Vieja.SALEN - vsal)
                                .append(camino).append(", embiste +").append(com.darkcraft.enemigos.Vieja.EMBISTEN - vemb).append(", da +")
                                .append(com.darkcraft.enemigos.Vieja.DAN - vdan);
                        default -> {
                        }
                    }
                    sg.append(';');
                    limpiarBallenero(m, caja);
                    if (salta) br.moveTo(x0, y0, z0, -90f, 0f);
                }
                sg.append(" en total: ").append(br.estado());
            }
            log.println(sg);
            // ---- a media vida: llama a la Vieja, y la vuelve a llamar (con una presa lejos, bajo las costillas)
            StringBuilder sm = new StringBuilder("Morvain, el Ballenero a media vida:");
            br.quieta = false;
            br.moveTo(x0, y0, z0, -90f, 0f);
            final double vx = x0 + 18;
            if (victima != null) {
                victima.moveTo(vx, y0, z0, 90f, 0f);
                br.setTarget(victima);
            }
            int ll0 = br.llamadas(), vs0 = com.darkcraft.enemigos.Vieja.SALEN, segunda = -1;
            br.mediaVida();
            boolean llama = br.accion() == com.darkcraft.enemigos.BalleneroAnims.LLAMAR;
            for (int k = 0; k < 560; k++) {
                if (victima != null) {
                    victima.moveTo(vx, y0, z0, 90f, 0f);
                    victima.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    victima.setHealth(victima.getMaxHealth());
                    victima.invulnerableTime = 0;
                    if (br.getTarget() == null) br.setTarget(victima);
                }
                br.tickCount++;
                br.tick();
                tickBallenero(m, caja, quitar);
                if (segunda < 0 && br.llamadas() >= ll0 + 2) segunda = k;
            }
            sm.append(" segunda ").append(br.segunda()).append(", llama a la Vieja ").append(llama).append(" (llamadas +").append(br.llamadas() - ll0)
                    .append(", salen +").append(com.darkcraft.enemigos.Vieja.SALEN - vs0).append(", la vuelve a llamar en el tick ").append(segunda)
                    .append(", cada ").append(com.darkcraft.enemigos.Ballenero.VIEJA_CADA).append("); ").append(br.estado());
            br.quieta = true;
            for (int k = 0; k < 160 && (br.accion() != 0 || br.arponFuera()); k++) {
                br.tickCount++;
                br.tick();
                tickBallenero(m, caja, quitar);
            }
            limpiarBallenero(m, caja);
            log.println(sm);
            // ---- cae: de rodillas sobre el arpon, la Vieja sale y sube al cielo, se deshace en sal y vuelve la marea
            com.darkcraft.enemigos.Puestos.todos(server, puerto, false);
            java.util.List<com.darkcraft.enemigos.Enemigo> faenan = new java.util.ArrayList<>();
            int delPuerto = 0;
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, puerto))
                if (com.darkcraft.morvain.MorvainEnemigos.puerto().get(e.puesto()).postura() == com.darkcraft.enemigos.Enemigo.REZA) {
                    delPuerto++;
                    faenan.add(e);
                }
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio))
                if (e != br && com.darkcraft.morvain.MorvainEnemigos.costillar().get(e.puesto()).postura() == com.darkcraft.enemigos.Enemigo.REZA) faenan.add(e);
            int desc = com.darkcraft.morvain.Costillar.DESCANSAN;
            boolean pazAntes = com.darkcraft.morvain.Costillar.enPaz(server);
            br.moveTo(CX + 3.5, PISO, CZ + 0.5, -90f, 0f);
            br.invulnerableTime = 0;
            br.hurt(m.damageSources().playerAttack(fp), 99999f);
            boolean muere = !br.isAlive();
            int sube = -1, seVa = -1;
            double arriba = 0;
            for (int fin = 0; fin < 260; fin++) {
                if (!br.isRemoved()) {
                    br.tickCount++;
                    br.tick();
                }
                for (var v : m.getEntitiesOfClass(com.darkcraft.enemigos.Vieja.class, caja.inflate(0, 60, 0))) {
                    if (!quitar.contains(v)) quitar.add(v);
                    if (v.isRemoved()) continue;
                    if (sube < 0 && v.estado() == com.darkcraft.enemigos.Vieja.SUBE) sube = fin;
                    v.tickCount++;
                    v.tick();
                    arriba = Math.max(arriba, v.getY() - PISO);
                }
                if (seVa < 0 && br.isRemoved()) seVa = fin;
                if (seVa >= 0 && sube >= 0 && fin > seVa + 5) break;
            }
            com.darkcraft.morvain.Costillar.paz(m);
            int descansan = 0;
            for (var e : faenan) if (e.isRemoved()) descansan++;
            int objetos = m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, br.getBoundingBox().inflate(6)).size();
            // despues: los que faenaban ya no vuelven (ni en el puerto ni aqui)
            com.darkcraft.enemigos.Puestos.todos(server, puerto, false);
            int salen = 0, vuelven = 0, vuelvenAqui = 0;
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, puerto)) {
                salen++;
                if (com.darkcraft.morvain.MorvainEnemigos.puerto().get(e.puesto()).postura() == com.darkcraft.enemigos.Enemigo.REZA) vuelven++;
            }
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio))
                if (com.darkcraft.morvain.MorvainEnemigos.costillar().get(e.puesto()).postura() == com.darkcraft.enemigos.Enemigo.REZA) vuelvenAqui++;
            com.darkcraft.enemigos.Puestos.todos(server, puerto, false);
            log.println("Morvain, el Ballenero cae: muere " + muere + " (en paz antes " + pazAntes + "); la Vieja sale y sube al cielo en el tick " + sube
                    + String.format(java.util.Locale.ROOT, " (hasta %.0f sobre la arena)", arriba) + ", el se deshace en sal en " + seVa + "; vivo " + br.isAlive()
                    + ", objetos " + objetos + ", en paz " + com.darkcraft.morvain.Costillar.enPaz(server) + "; de los " + faenan.size() + " salobres que faenaban ("
                    + delPuerto + " en el puerto) descansan " + descansan + " (deshechos +" + (com.darkcraft.morvain.Costillar.DESCANSAN - desc)
                    + "); despues en el puerto salen " + salen + " y de los que faenaban vuelven " + vuelven + ", y en el costillar " + vuelvenAqui
                    + " (tienen que ser 0); " + com.darkcraft.morvain.MorvainEnemigos.estadoBallenero(server));
            for (var it : m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, br.getBoundingBox().inflate(8))) it.discard();
        } catch (Throwable t) {
            log.println("Morvain, el Ballenero: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] ballenero", t);
        } finally {
            if (br != null) br.quieta = false;
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            limpiarBallenero(m, caja);
            // todo como antes: Brann de vigia bajo las costillas y los salobres que faenaban, en su sitio
            com.darkcraft.enemigos.Puestos.marcar(server, sitio, com.darkcraft.morvain.MorvainEnemigos.ballenero(), false);
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            com.darkcraft.enemigos.Puestos.todos(server, puerto, false);
            try {
                com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
                log.println("Morvain, el Ballenero vuelve: " + com.darkcraft.morvain.MorvainEnemigos.estadoBallenero(server));
            } catch (Throwable t) {
                log.println("Morvain, el Ballenero vuelve: ERROR " + t);
            }
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    private static boolean tickAltar(ServerLevel m, net.minecraft.world.phys.AABB caja, java.util.List<net.minecraft.world.entity.Entity> quitar) {
        boolean hay = false;
        for (var e : m.getEntitiesOfClass(com.darkcraft.enemigos.CharcoSangre.class, caja)) {
            if (e.isRemoved()) continue;
            if (!quitar.contains(e)) quitar.add(e);
            e.tickCount++;
            e.tick();
            hay = true;
        }
        return hay;
    }

    private static void limpiarAltar(ServerLevel m, net.minecraft.world.phys.AABB caja) {
        for (var e : m.getEntitiesOfClass(com.darkcraft.enemigos.CharcoSangre.class, caja.inflate(10, 20, 10))) e.discard();
    }

    /**
     * Parte 7t: el Altar Profanado (a pie desde el camino del santuario a todos sus sitios; el atril con el libro 29, el arcon, la
     * pila del altar, el canal y las pilas de sangre, la puerta, la campana y el suelo de la nave libre) y Anselmo, el Pontifice
     * Sangrante: de rodillas ante el altar con los penitentes de alrededor; al ver a alguien en la nave habla mientras se levanta y
     * predica; cada accion acaba; sus golpes a una presa (el ferulazo y el barrido que hacen sangrar, la aspersion del caliz, la
     * comunion que le cura con la sangre de quien sangra, el juicio con sus columnas de sangre); a media vida se unge: se alza la
     * procesion y deja charcos de sangre; y al caer, los penitentes que rezaban de rodillas en el altar y en el Puente de la Grieta se
     * deshacen y ya no vuelven. Al acabar, todo como estaba.
     */
    private static void altar(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.ALTAR, grieta = com.darkcraft.morvain.MorvainEnemigos.GRIETA;
        var A = com.darkcraft.morvain.Altar.get();
        final int CX = com.darkcraft.morvain.Altar.CX, CZ = com.darkcraft.morvain.Altar.CZ, PISO = com.darkcraft.morvain.Altar.PISO;
        final net.minecraft.world.phys.AABB caja = new net.minecraft.world.phys.AABB(CX - 40, PISO - 6, CZ - 45, CX + 40, PISO + 30, CZ + 55);
        try {
            long t0 = System.currentTimeMillis();
            int nch = 0;
            for (long k : A.chunks()) {
                m.getChunk((int) (k >> 32), (int) k);
                nch++;
            }
            long ms = System.currentTimeMillis() - t0;
            var aire = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
                var st = m.getBlockState(new BlockPos(x, y, z));
                return st.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock || st.getBlock() instanceof net.minecraft.world.level.block.CandleBlock
                        || st.getBlock() instanceof net.minecraft.world.level.block.RedStoneWireBlock ? aire : st;
            };
            int[] en = A.sitio("entrada");
            var zona = com.darkcraft.pueblo.gen.Andar.desde(mundo, en[0], en[1], en[2], com.darkcraft.morvain.Altar.X0, com.darkcraft.morvain.Altar.Z0,
                    com.darkcraft.morvain.Altar.X1, com.darkcraft.morvain.Altar.Z1, 4_000_000);
            StringBuilder no = new StringBuilder();
            int llega = 0, sitios = 0;
            for (var e : A.sitios().entrySet()) {
                if (e.getKey().equals("alto")) continue;
                sitios++;
                int[] q = e.getValue();
                if (zona.alcanza(q[0], q[1], q[2], 2)) llega++;
                else no.append(' ').append(e.getKey());
            }
            int lejos = 0;
            for (var o : A.esperas()) if (!zona.alcanza(o.x(), o.y(), o.z(), 2)) lejos++;
            var sc = com.darkcraft.morvain.SantuariosMorvain.porId("altar");
            log.println("Morvain, el altar profanado: " + nch + " chunks en " + ms + " ms; " + com.darkcraft.morvain.Altar.resumen(m) + "; bancos " + A.bancos
                    + ", velas " + A.velas + ", tumbas " + A.tumbas + ", tiendas " + A.tiendas + ", escombros " + A.escombros + ", arcos " + A.arcos + "; santuario "
                    + com.darkcraft.santuario.Santuarios.nombre(m, sc.pos()) + " (" + m.getBlockState(sc.pos()).getBlock().getDescriptionId() + ") a "
                    + Math.round(Math.hypot(sc.x() - en[0], sc.z() - en[2])) + " de la entrada; a pie desde la entrada (" + zona.celdas() + " celdas) a " + llega
                    + " de " + sitios + " sitios" + (no.length() == 0 ? "" : ", no a:" + no) + "; los que esperan a los que no se llega " + lejos + " de "
                    + A.esperas().size() + "; lugar en la nave " + (com.darkcraft.morvain.Altar.dentro(CX, CZ) ? "altar" : "NO") + ", en la basilica "
                    + com.darkcraft.morvain.Altar.enNave(CX + 0.5, CZ + 0.5) + ", en el santuario " + com.darkcraft.morvain.Altar.dentro(sc.x(), sc.z())
                    + " (tiene que ser false)");
            topView(m, CX, CZ + 4, 50, new File(out, "morvain_altar.png"));
            topView(m, CX, CZ - 10, 22, new File(out, "morvain_altar_nave.png"));
        } catch (Throwable t) {
            log.println("Morvain, el altar profanado: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] altar", t);
        }
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        com.darkcraft.enemigos.Pontifice pt = null;
        try {
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            int suyos = 0, rodillas = 0, enSuelo = 0, chocan = 0;
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            for (int k = 0; k < 6; k++)
                for (var e : fuera) if (e.isAlive() && !(e instanceof com.darkcraft.enemigos.Pontifice)) e.tick();
            for (var e : fuera) {
                if (e instanceof com.darkcraft.enemigos.Pontifice x) pt = x;
                else {
                    suyos++;
                    if (e.postura() == com.darkcraft.enemigos.Enemigo.REZA) rodillas++;
                }
                if (!m.noCollision(e)) chocan++;
                if (!m.getBlockState(BlockPos.containing(e.getX(), e.getY() - 0.2, e.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty()) enSuelo++;
            }
            if (pt == null) {
                log.println("Morvain, el Pontifice: NO SALE");
                return;
            }
            for (var e : fuera) if (e != pt) e.discard();
            StringBuilder sb = new StringBuilder("Morvain, el Pontifice: en " + pt.blockPosition().toShortString() + " postura " + pt.postura() + " (1 de rodillas), giro "
                    + Math.round(pt.getYRot()) + ", vida " + Math.round(pt.getMaxHealth()) + ", caja " + pt.getBbWidth() + " x " + pt.getBbHeight() + ", en la basilica "
                    + com.darkcraft.morvain.Altar.enNave(pt.getX(), pt.getZ()) + ", musica " + pt.musica() + "; los suyos " + suyos + " (de rodillas " + rodillas
                    + "); en su suelo " + enSuelo + " de " + fuera.size() + ", metidos en un bloque " + chocan + " (tiene que ser 0)");
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("5b7c9d1e-3f40-4b6c-8d8e-2f3a4b5c6dab"), "[DarkCraftCI12]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            // ---- le ve en la nave (a su espalda): habla mientras se levanta y predica al acabar
            final double fxx = CX + 0.5 + 3, fyy = PISO, fzz = CZ - 6.5;
            fp.moveTo(fxx, fyy, fzz, 180f, 0f);
            fp.setOnGround(true);
            pt.setTarget(fp);
            int levanta = -1, predica = -1, ataca = -1, quien = 0;
            for (int k = 0; k < 400; k++) {
                pt.tickCount++;
                pt.tick();
                tickAltar(m, caja, quitar);
                fp.moveTo(fxx, fyy, fzz, 180f, 0f);
                fp.setHealth(fp.getMaxHealth());
                com.darkcraft.enemigos.Sangrado.limpiar(fp);
                int a = pt.accion();
                if (levanta < 0 && a == com.darkcraft.enemigos.PontificeAnims.LEVANTARSE) levanta = k;
                if (predica < 0 && a == com.darkcraft.enemigos.PontificeAnims.SERMON) predica = k;
                if (predica >= 0 && ataca < 0 && a != 0 && a != com.darkcraft.enemigos.PontificeAnims.SERMON) {
                    ataca = k;
                    quien = a;
                    break;
                }
            }
            sb.append("; al verle: pelea ").append(pt.enPelea()).append(", se levanta en el tick ").append(levanta).append(", predica en el ").append(predica)
                    .append(" (la frase 1 dura ").append(com.darkcraft.rey.Voces.ticks(com.darkcraft.enemigos.Pontifice.VOZ_BASE + 1)).append("), y luego ")
                    .append(quien == 0 ? "nada" : com.darkcraft.enemigos.PontificeAnims.NOMBRES[quien]).append(" en el ").append(ataca)
                    .append(String.format(java.util.Locale.ROOT, ", de pie %s a %.1f de su presa", pt.postura() == com.darkcraft.enemigos.Enemigo.DE_PIE,
                            Math.hypot(pt.getX() - fp.getX(), pt.getZ() - fp.getZ())));
            // ---- cada accion acaba (sin presa y quieto: no elige ni predica por su cuenta)
            pt.quieta = true;
            pt.setTarget(null);
            for (int k = 0; k < 120 && pt.accion() != 0; k++) {
                pt.tickCount++;
                pt.tick();
                tickAltar(m, caja, quitar);
            }
            StringBuilder nn = new StringBuilder();
            int vistas = 0;
            for (int a = 1; a < com.darkcraft.enemigos.PontificeAnims.TICKS.length; a++) {
                String nombre = com.darkcraft.enemigos.PontificeAnims.NOMBRES[a];
                if (nombre.equals("morir") || nombre.equals("levantarse") || nombre.equals("uncion")) continue;
                pt.moveTo(CX + 0.5, PISO, CZ - 10.5, 180f, 0f);
                pt.empezar(a);
                vistas++;
                for (int k = 0; k < 160 && pt.accion() == a; k++) {
                    pt.tickCount++;
                    pt.tick();
                    tickAltar(m, caja, quitar);
                }
                if (pt.accion() == a) nn.append(' ').append(nombre);
            }
            for (int k = 0; k < 60 && pt.marcasPendientes() > 0; k++) {
                pt.tickCount++;
                pt.tick();
            }
            limpiarAltar(m, caja);
            sb.append("; de ").append(vistas).append(" acciones no acaban:").append(nn.length() == 0 ? " ninguna" : nn);
            log.println(sb);
            // ---- sus golpes a una presa (un aldeano quieto y duro) en el crucero: el mirando al sur, hacia la puerta
            StringBuilder sg = new StringBuilder("Morvain, los golpes del Pontifice a una presa:");
            final double x0 = CX + 0.5, y0 = PISO, z0 = CZ - 16.5;
            var victima = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
            if (victima != null) {
                victima.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(900.0);
                victima.setHealth(900f);
                victima.setNoAi(true);
                victima.moveTo(x0, y0, z0 + 3, 180f, 0f);
                m.addFreshEntity(victima);
                quitar.add(victima);
                String[] golpes = {"ferulazo", "barrido", "aspersion", "comunion", "juicio"};
                int[] acc = {com.darkcraft.enemigos.PontificeAnims.FERULAZO, com.darkcraft.enemigos.PontificeAnims.BARRIDO, com.darkcraft.enemigos.PontificeAnims.ASPERSION,
                        com.darkcraft.enemigos.PontificeAnims.COMUNION, com.darkcraft.enemigos.PontificeAnims.JUICIO};
                double[] lejos = {3.6, 3.0, 5.0, 8.0, 10.0};
                int[] dura = {70, 70, 70, 100, 110};
                for (int i = 0; i < golpes.length; i++) {
                    victima.setHealth(victima.getMaxHealth());
                    victima.invulnerableTime = 0;
                    victima.hurtTime = 0;
                    victima.removeAllEffects();
                    com.darkcraft.enemigos.Sangrado.limpiar(victima);
                    victima.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    victima.moveTo(x0, y0, z0 + lejos[i], 180f, 0f);
                    victima.setOnGround(true);
                    // la comunion: llega sangrando (y el, herido)
                    if (acc[i] == com.darkcraft.enemigos.PontificeAnims.COMUNION) {
                        com.darkcraft.enemigos.Sangrado.sumar(victima, 70f, pt);
                        pt.setHealth(pt.getMaxHealth() * 0.7f);
                    }
                    int fer = pt.ferulazos(), bar = pt.barridos(), asp = pt.aspersiones(), asj = pt.asperjados(), com_ = pt.comuniones(), beb = pt.bebidos(),
                            jui = pt.juicios(), mar = pt.marcadas(), col = pt.columnas(), cold = pt.columnasQueDan();
                    float curado = pt.curado(), vidaAntes = pt.getHealth();
                    pt.moveTo(x0, y0, z0, 0f, 0f);
                    pt.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    pt.setTarget(victima);
                    pt.setYRot(0f);
                    pt.yBodyRot = pt.yHeadRot = 0f;
                    pt.empezar(acc[i]);
                    float antes = victima.getHealth();
                    float sangre = 0;
                    double alto = 0;
                    for (int k = 0; k < dura[i]; k++) {
                        boolean el = pt.accion() != 0 || pt.marcasPendientes() > 0;
                        if (el) {
                            pt.setTarget(victima);
                            pt.tickCount++;
                            pt.tick();
                            pt.moveTo(x0, y0, z0, 0f, 0f);
                        }
                        tickAltar(m, caja, quitar);
                        net.minecraft.world.phys.Vec3 dv = victima.getDeltaMovement();
                        if (dv.lengthSqr() > 1e-4) {
                            victima.move(net.minecraft.world.entity.MoverType.SELF, dv);
                            victima.setDeltaMovement(dv.x * 0.55, victima.onGround() ? 0 : dv.y - 0.08, dv.z * 0.55);
                        }
                        alto = Math.max(alto, victima.getY() - y0);
                        victima.invulnerableTime = 0;
                        sangre = Math.max(sangre, com.darkcraft.enemigos.Sangrado.nivel(victima));
                        if (!el && k > 2) break;
                    }
                    sg.append(' ').append(golpes[i]).append(String.format(java.util.Locale.ROOT, " a %.1f: -%.1f", lejos[i], antes - victima.getHealth()));
                    switch (i) {
                        case 0 -> sg.append(", ferulazos +").append(pt.ferulazos() - fer).append(String.format(java.util.Locale.ROOT, ", sangra %.0f", sangre));
                        case 1 -> sg.append(", barridos +").append(pt.barridos() - bar).append(String.format(java.util.Locale.ROOT, ", sangra %.0f", sangre));
                        case 2 -> sg.append(", aspersiones +").append(pt.aspersiones() - asp).append(" (dan a +").append(pt.asperjados() - asj)
                                .append(String.format(java.util.Locale.ROOT, "), sangra %.0f", sangre));
                        case 3 -> sg.append(", comuniones +").append(pt.comuniones() - com_).append(" (bebe de +").append(pt.bebidos() - beb)
                                .append(String.format(java.util.Locale.ROOT, ", se cura %.0f: de %.0f a %.0f), a la presa le queda de sangre %.0f", pt.curado() - curado,
                                        vidaAntes, pt.getHealth(), com.darkcraft.enemigos.Sangrado.nivel(victima)));
                        case 4 -> sg.append(", juicios +").append(pt.juicios() - jui).append(" (marcas +").append(pt.marcadas() - mar).append(", columnas +")
                                .append(pt.columnas() - col).append(", dan +").append(pt.columnasQueDan() - cold)
                                .append(String.format(java.util.Locale.ROOT, ", la lanza %.1f arriba)", alto));
                        default -> {
                        }
                    }
                    sg.append(';');
                    limpiarAltar(m, caja);
                }
                sg.append(" en total: ").append(pt.estado());
            }
            log.println(sg);
            // ---- a media vida: se unge, se alza la procesion (de los canales de la nave) y deja charcos que hacen sangrar
            StringBuilder sm = new StringBuilder("Morvain, el Pontifice a media vida:");
            pt.quieta = false;
            pt.setHealth(pt.getMaxHealth());
            pt.moveTo(CX + 0.5, PISO, CZ - 12.5, 0f, 0f);
            final double vx = CX + 0.5, vz = CZ - 4.5;
            if (victima != null) {
                victima.moveTo(vx, y0, vz, 180f, 0f);
                pt.setTarget(victima);
            }
            int alz0 = pt.alzados(), ch0 = pt.charcos(), sang0 = com.darkcraft.enemigos.CharcoSangre.SANGRAN;
            pt.mediaVida();
            boolean unge = pt.accion() == com.darkcraft.enemigos.PontificeAnims.UNCION;
            int ungido = -1;
            for (int k = 0; k < 240; k++) {
                if (victima != null) {
                    // la presa pisa donde el pisa (los charcos)
                    victima.moveTo(pt.getX(), y0, pt.getZ() + 1.2, 180f, 0f);
                    victima.setOnGround(true);
                    victima.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    victima.setHealth(victima.getMaxHealth());
                    victima.invulnerableTime = 0;
                    if (pt.getTarget() == null) pt.setTarget(victima);
                }
                pt.tickCount++;
                pt.tick();
                pt.moveTo(CX + 0.5, PISO, CZ - 12.5, 0f, 0f);
                pt.setOnGround(true);
                tickAltar(m, caja, quitar);
                if (ungido < 0 && pt.ungido()) ungido = k;
            }
            int enPie = 0, efimeros = 0, enSu = 0;
            for (var p : pt.procesion()) {
                quitar.add(p);
                if (p.isAlive()) enPie++;
                if (p.esEfimero()) efimeros++;
                if (p.isAlive() && !m.getBlockState(BlockPos.containing(p.getX(), p.getY() - 0.2, p.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty()) enSu++;
            }
            sm.append(" segunda ").append(pt.segunda()).append(", se unge ").append(unge).append(" (ungido en el tick ").append(ungido).append("), la procesion +")
                    .append(pt.alzados() - alz0).append(" (en pie ").append(enPie).append(", efimeros ").append(efimeros).append(", en su suelo ").append(enSu)
                    .append("), charcos +").append(pt.charcos() - ch0).append(" (hacen sangrar +").append(com.darkcraft.enemigos.CharcoSangre.SANGRAN - sang0)
                    .append("), mas deprisa ").append(pt.ungido()).append("; ").append(pt.estado());
            for (var p : pt.procesion()) p.discard();
            pt.quieta = true;
            for (int k = 0; k < 160 && pt.accion() != 0; k++) {
                pt.tickCount++;
                pt.tick();
            }
            limpiarAltar(m, caja);
            log.println(sm);
            // ---- cae: de rodillas, el caliz se derrama, se deshace en sangre; la Tierra sin Fe descansa
            com.darkcraft.enemigos.Puestos.todos(server, grieta, false);
            java.util.List<com.darkcraft.enemigos.Enemigo> rezan = new java.util.ArrayList<>();
            int deLaGrieta = 0;
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, grieta))
                if (com.darkcraft.morvain.MorvainEnemigos.grieta().get(e.puesto()).postura() == com.darkcraft.enemigos.Enemigo.REZA) {
                    deLaGrieta++;
                    rezan.add(e);
                }
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio))
                if (e != pt && com.darkcraft.morvain.MorvainEnemigos.altar().get(e.puesto()).postura() == com.darkcraft.enemigos.Enemigo.REZA) rezan.add(e);
            int desc = com.darkcraft.morvain.Altar.DESCANSAN;
            boolean pazAntes = com.darkcraft.morvain.Altar.enPaz(server);
            pt.moveTo(CX + 0.5, PISO, CZ - 16.5, 180f, 0f);
            pt.invulnerableTime = 0;
            pt.hurt(m.damageSources().playerAttack(fp), 99999f);
            boolean muere = !pt.isAlive();
            int seVa = -1;
            for (int fin = 0; fin < 260; fin++) {
                if (!pt.isRemoved()) {
                    pt.tickCount++;
                    pt.tick();
                }
                if (seVa < 0 && pt.isRemoved()) {
                    seVa = fin;
                    break;
                }
            }
            com.darkcraft.morvain.Altar.paz(m);
            int descansan = 0;
            for (var e : rezan) if (e.isRemoved()) descansan++;
            int objetos = m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, pt.getBoundingBox().inflate(6)).size();
            // despues: los que rezaban ya no vuelven (ni en la grieta ni aqui)
            com.darkcraft.enemigos.Puestos.todos(server, grieta, false);
            int salen = 0, vuelven = 0, vuelvenAqui = 0;
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, grieta)) {
                salen++;
                if (com.darkcraft.morvain.MorvainEnemigos.grieta().get(e.puesto()).postura() == com.darkcraft.enemigos.Enemigo.REZA) vuelven++;
            }
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio))
                if (com.darkcraft.morvain.MorvainEnemigos.altar().get(e.puesto()).postura() == com.darkcraft.enemigos.Enemigo.REZA) vuelvenAqui++;
            com.darkcraft.enemigos.Puestos.todos(server, grieta, false);
            log.println("Morvain, el Pontifice cae: muere " + muere + " (en paz antes " + pazAntes + "); se deshace en sangre en " + seVa + "; vivo " + pt.isAlive()
                    + ", objetos " + objetos + ", en paz " + com.darkcraft.morvain.Altar.enPaz(server) + "; de los " + rezan.size() + " penitentes que rezaban ("
                    + deLaGrieta + " en la grieta) descansan " + descansan + " (deshechos +" + (com.darkcraft.morvain.Altar.DESCANSAN - desc)
                    + "); despues en la grieta salen " + salen + " y de los que rezaban vuelven " + vuelven + ", y en el altar " + vuelvenAqui
                    + " (tienen que ser 0); " + com.darkcraft.morvain.MorvainEnemigos.estadoPontifice(server));
            for (var it : m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, pt.getBoundingBox().inflate(8))) it.discard();
        } catch (Throwable t) {
            log.println("Morvain, el Pontifice: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] pontifice", t);
        } finally {
            if (pt != null) pt.quieta = false;
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            limpiarAltar(m, caja);
            // todo como antes: Anselmo de rodillas ante el altar y los penitentes que rezaban, en su sitio
            com.darkcraft.enemigos.Puestos.marcar(server, sitio, com.darkcraft.morvain.MorvainEnemigos.pontifice(), false);
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            com.darkcraft.enemigos.Puestos.todos(server, grieta, false);
            try {
                com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
                log.println("Morvain, el Pontifice vuelve: " + com.darkcraft.morvain.MorvainEnemigos.estadoPontifice(server));
            } catch (Throwable t) {
                log.println("Morvain, el Pontifice vuelve: ERROR " + t);
            }
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    /** Lo que dejan los marcados (las flechas y los reflejos): un tick, a mano (la prueba no mueve el mundo). */
    private static void tickMarcados(ServerLevel m, net.minecraft.world.phys.AABB caja, java.util.List<net.minecraft.world.entity.Entity> quitar) {
        java.util.List<net.minecraft.world.entity.Entity> l = new java.util.ArrayList<>();
        l.addAll(m.getEntitiesOfClass(com.darkcraft.enemigos.Flecha.class, caja));
        for (var r : m.getEntitiesOfClass(com.darkcraft.enemigos.Marcado.class, caja)) if (r.reflejo()) l.add(r);
        for (var e : l) {
            if (e.isRemoved()) continue;
            if (!quitar.contains(e)) quitar.add(e);
            e.tickCount++;
            e.tick();
        }
    }

    /**
     * Parte 7u: el Cerco del Ancla (a pie desde el santuario del Ancla del Sur a todos sus sitios —menos lo alto de las torres— y a
     * cada marcado —menos los cazadores de lo alto de las torres—; el atril con el libro 30, el arcon, la escalera del ancla, la
     * puerta, el pilon y el suelo del patio) y los marcados: en sus puestos (de rodillas golpeando el ancla, caidos de bruces o de
     * guardia), uno de rodillas que se levanta y uno de bruces que se alza; cada accion de cada uno acaba; sus golpes a una presa (el
     * mazazo con su onda, el barrido, el tajo, la estocada que se tira a fondo, el escudazo, el disparo, la flecha negra que nubla, la
     * punzada y el desliz); el escudo del caballero (de frente, por la espalda y una flecha); la marca que arde a media vida y el
     * reflejo que repite el golpe (no se le puede pegar y se deshace); y uno que cae y se deshace en sombra. Al acabar, como estaba.
     */
    private static void cerco(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.CERCO;
        var A = com.darkcraft.morvain.Cerco.get();
        final int CX = com.darkcraft.morvain.Cerco.CX, CZ = com.darkcraft.morvain.Cerco.CZ, PISO = com.darkcraft.morvain.Cerco.PISO;
        try {
            long t0 = System.currentTimeMillis();
            int nch = 0;
            for (long k : A.chunks()) {
                m.getChunk((int) (k >> 32), (int) k);
                nch++;
            }
            long ms = System.currentTimeMillis() - t0;
            var aire = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
                var st = m.getBlockState(new BlockPos(x, y, z));
                return st.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock || st.getBlock() instanceof net.minecraft.world.level.block.CandleBlock
                        ? aire : st;
            };
            int[] en = A.sitio("entrada");
            var zona = com.darkcraft.pueblo.gen.Andar.desde(mundo, en[0], en[1], en[2], com.darkcraft.morvain.Cerco.X0, com.darkcraft.morvain.Cerco.Z0,
                    com.darkcraft.morvain.Cerco.X1, com.darkcraft.morvain.Cerco.Z1, 6_000_000);
            StringBuilder ap = new StringBuilder();
            int llega = 0, sitios = 0;
            java.util.Set<String> mirar = java.util.Set.of("alto", "torre_este", "torre_oeste");
            for (var e : A.sitios().entrySet()) {
                if (mirar.contains(e.getKey())) continue;
                sitios++;
                int[] q = e.getValue();
                if (zona.alcanza(q[0], q[1], q[2], 2)) llega++;
                else ap.append(' ').append(e.getKey());
            }
            int lejos = 0, enAlto = 0;
            for (var o : A.esperas()) {
                if (o.grupo().equals("en_alto")) enAlto++;
                else if (!zona.alcanza(o.x(), o.y(), o.z(), 2)) lejos++;
            }
            var sc = com.darkcraft.morvain.SantuariosMorvain.porId("ancla_sur");
            log.println("Morvain, el cerco del ancla: " + nch + " chunks en " + ms + " ms; " + com.darkcraft.morvain.Cerco.resumen(m) + "; santuario "
                    + com.darkcraft.santuario.Santuarios.nombre(m, sc.pos()) + " (" + m.getBlockState(sc.pos()).getBlock().getDescriptionId() + ") a "
                    + Math.round(com.darkcraft.morvain.Cerco.distancia(sc.x(), sc.z())) + " del ancla; a pie desde la entrada (" + zona.celdas() + " celdas) a "
                    + llega + " de " + sitios + " sitios" + (ap.length() == 0 ? "" : ", no a:" + ap) + "; marcados a los que no se llega " + lejos + " de "
                    + (A.esperas().size() - enAlto) + " (y " + enAlto + " en lo alto de las torres); estelas " + A.estelas + ", jaulas " + A.jaulas + ", farolas "
                    + A.farolas + "; lugar en el patio " + (com.darkcraft.morvain.Cerco.dentro(CX + 0.5, CZ + 22.5) ? "cerco" : "NO") + " (dentro de la muralla "
                    + com.darkcraft.morvain.Cerco.enCerco(CX + 0.5, CZ + 22.5) + "), en el camino " + com.darkcraft.morvain.Cerco.dentro(A.sitio("camino")[0] + 0.5,
                    A.sitio("camino")[2] + 0.5) + ", en el santuario " + com.darkcraft.morvain.Cerco.dentro(sc.x() + 0.5, sc.z() + 0.5) + " (tiene que ser false)");
            topView(m, CX + 10, CZ + 70, 130, new File(out, "morvain_cerco.png"));
            topView(m, CX, CZ, 40, new File(out, "morvain_cerco_patio.png"));
        } catch (Throwable t) {
            log.println("Morvain, el cerco del ancla: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] cerco", t);
        }
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        final double gx = CX - 5.5, gz = CZ + 20.5;
        final var caja = new net.minecraft.world.phys.AABB(gx - 30, PISO - 6, gz - 30, gx + 30, PISO + 20, gz + 30);
        try {
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            for (int k = 0; k < 10; k++)
                for (var e : fuera) if (e.isAlive()) e.tick();
            int rezando = 0, caidos = 0, dePie = 0, enSuelo = 0, chocan = 0;
            int[] porVariante = new int[3];
            StringBuilder mal = new StringBuilder();
            com.darkcraft.enemigos.Marcado rodillas = null, caido = null;
            for (var e : fuera) {
                if (!(e instanceof com.darkcraft.enemigos.Marcado c)) continue;
                porVariante[Math.max(0, Math.min(2, c.variante()))]++;
                if (!m.noCollision(c)) {
                    chocan++;
                    if (mal.length() < 300) mal.append(" metido en un bloque en ").append(c.blockPosition().toShortString()).append(';');
                }
                if (c.postura() == com.darkcraft.enemigos.Enemigo.REZA) {
                    rezando++;
                    if (rodillas == null) rodillas = c;
                } else if (c.postura() == com.darkcraft.enemigos.Enemigo.TUMBADO) {
                    caidos++;
                    if (caido == null) caido = c;
                } else dePie++;
                var abajo = m.getBlockState(BlockPos.containing(c.getX(), c.getY() - 0.2, c.getZ()));
                if (!abajo.getCollisionShape(m, BlockPos.ZERO).isEmpty()) enSuelo++;
                else if (mal.length() < 400) mal.append(' ').append(c.tipo()).append(" en el aire en ").append(c.blockPosition().toShortString()).append(';');
            }
            log.println("Morvain, los marcados: " + com.darkcraft.morvain.MorvainEnemigos.comprobar() + "; fuera " + fuera.size() + " de "
                    + com.darkcraft.morvain.MorvainEnemigos.cerco().size() + " (rompedores " + porVariante[0] + ", caballeros " + porVariante[1] + ", cazadores "
                    + porVariante[2] + "): de rodillas " + rezando + ", caidos de bruces " + caidos + ", de pie " + dePie + " (en su suelo " + enSuelo
                    + "); metidos en un bloque " + chocan + " (tiene que ser 0)" + mal);
            StringBuilder sb = new StringBuilder("Morvain, los marcados en accion:");
            // ---- el de rodillas se levanta y el caido de bruces se alza (una presa a su lado)
            int alz = com.darkcraft.enemigos.Marcado.ALZADOS;
            for (var c : new com.darkcraft.enemigos.Marcado[]{rodillas, caido}) {
                if (c == null) {
                    sb.append(" NO HAY ").append(c == rodillas ? "NINGUNO DE RODILLAS;" : "NINGUNO CAIDO;");
                    continue;
                }
                var presa = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
                if (presa == null) continue;
                presa.moveTo(c.getX() + 1.5, c.getY(), c.getZ() + 0.5, 0f, 0f);
                presa.setNoAi(true);
                m.addFreshEntity(presa);
                quitar.add(presa);
                int antes = c.postura();
                c.setTarget(presa);
                int sale = -1;
                for (int k = 0; k < 80; k++) {
                    c.tickCount++;
                    c.tick();
                    int a = c.accion();
                    if (sale < 0 && (a == com.darkcraft.enemigos.MarcadoAnims.LEVANTARSE || a == com.darkcraft.enemigos.MarcadoAnims.ALZARSE)) sale = k;
                    if (sale >= 0 && a == 0) break;
                }
                sb.append(String.format(java.util.Locale.ROOT, " el %s (%s, en %s) se %s en el tick %d y queda de pie %s;", c.tipo(),
                        antes == com.darkcraft.enemigos.Enemigo.REZA ? "de rodillas" : "de bruces", c.blockPosition().toShortString(),
                        antes == com.darkcraft.enemigos.Enemigo.REZA ? "levanta" : "alza", sale, c.postura() == com.darkcraft.enemigos.Enemigo.DE_PIE));
                c.setTarget(null);
            }
            sb.append(" alzados de bruces +").append(com.darkcraft.enemigos.Marcado.ALZADOS - alz).append(';');
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio)) e.discard();
            // ---- en el patio (de cara al este): uno de cada, cada accion suya acaba
            int[][] suyas = {
                    {com.darkcraft.enemigos.MarcadoAnims.MAZAZO, com.darkcraft.enemigos.MarcadoAnims.BARRIDO},
                    {com.darkcraft.enemigos.MarcadoAnims.TAJO, com.darkcraft.enemigos.MarcadoAnims.ESTOCADA, com.darkcraft.enemigos.MarcadoAnims.ESCUDAZO,
                            com.darkcraft.enemigos.MarcadoAnims.BLOQUEO},
                    {com.darkcraft.enemigos.MarcadoAnims.DISPARO, com.darkcraft.enemigos.MarcadoAnims.FLECHA_NEGRA, com.darkcraft.enemigos.MarcadoAnims.PUNZADA,
                            com.darkcraft.enemigos.MarcadoAnims.DESLIZ}};
            int[] comunes = {com.darkcraft.enemigos.MarcadoAnims.ATURDIDO, com.darkcraft.enemigos.MarcadoAnims.LEVANTARSE, com.darkcraft.enemigos.MarcadoAnims.ALZARSE};
            for (int v2 = 0; v2 < 3; v2++) {
                var a = DarkCraftMod.MARCADO.get().create(m);
                if (a == null) continue;
                a.variante(v2);
                a.moveTo(gx, PISO, gz, -90f, 0f);
                m.addFreshEntity(a);
                quitar.add(a);
                StringBuilder no = new StringBuilder();
                java.util.List<Integer> lista = new java.util.ArrayList<>();
                for (int ac : suyas[v2]) lista.add(ac);
                for (int ac : comunes) lista.add(ac);
                for (int ac : lista) {
                    String nombre = com.darkcraft.enemigos.MarcadoAnims.NOMBRES[ac];
                    a.moveTo(gx, PISO, gz, -90f, 0f);
                    a.empezar(ac);
                    for (int k = 0; k < 90 && a.accion() == ac; k++) {
                        a.tickCount++;
                        a.tick();
                    }
                    if (a.accion() == ac) no.append(' ').append(nombre);
                    for (int k = 0; k < 10; k++) a.tick();
                }
                sb.append(' ').append(a.tipo()).append(String.format(java.util.Locale.ROOT, " (vida %.0f, golpe %.1f, armadura %d)", a.getMaxHealth(),
                        a.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE), a.getArmorValue()))
                        .append(": acciones que no acaban").append(no.length() == 0 ? " ninguna" : no).append(';');
                a.discard();
            }
            for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Flecha.class, caja)) f.discard();
            log.println(sb);
            // ---- contra una presa (un aldeano quieto en el patio, al este del marcado)
            sb = new StringBuilder("Morvain, los marcados contra una presa:");
            int maz = com.darkcraft.enemigos.Marcado.MAZAZOS, ond = com.darkcraft.enemigos.Marcado.ONDAS, bar = com.darkcraft.enemigos.Marcado.BARRIDOS,
                    taj = com.darkcraft.enemigos.Marcado.TAJOS, est = com.darkcraft.enemigos.Marcado.ESTOCADAS, esc = com.darkcraft.enemigos.Marcado.ESCUDAZOS,
                    dis = com.darkcraft.enemigos.Marcado.DISPAROS, neg = com.darkcraft.enemigos.Marcado.NEGRAS, nub = com.darkcraft.enemigos.Marcado.NUBLADOS,
                    pun = com.darkcraft.enemigos.Marcado.PUNZADAS, des = com.darkcraft.enemigos.Marcado.DESLICES;
            String[] nombres = {"mazazo", "barrido", "tajo", "estocada", "escudazo", "disparo", "flecha negra", "punzada", "desliz"};
            int[] acciones = {com.darkcraft.enemigos.MarcadoAnims.MAZAZO, com.darkcraft.enemigos.MarcadoAnims.BARRIDO, com.darkcraft.enemigos.MarcadoAnims.TAJO,
                    com.darkcraft.enemigos.MarcadoAnims.ESTOCADA, com.darkcraft.enemigos.MarcadoAnims.ESCUDAZO, com.darkcraft.enemigos.MarcadoAnims.DISPARO,
                    com.darkcraft.enemigos.MarcadoAnims.FLECHA_NEGRA, com.darkcraft.enemigos.MarcadoAnims.PUNZADA, com.darkcraft.enemigos.MarcadoAnims.DESLIZ};
            int R = com.darkcraft.enemigos.Marcado.ROMPEDOR, C = com.darkcraft.enemigos.Marcado.CABALLERO, Z = com.darkcraft.enemigos.Marcado.CAZADOR;
            int[] variantes = {R, R, C, C, C, Z, Z, Z, Z};
            double[] dist = {2.6, 2.6, 2.2, 6.0, 2.0, 9.0, 10.0, 2.2, 2.0};
            int[] dura = {50, 45, 35, 40, 30, 60, 70, 30, 30};
            for (int i = 0; i < nombres.length; i++) {
                var a = DarkCraftMod.MARCADO.get().create(m);
                var presa = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
                if (a == null || presa == null) continue;
                a.variante(variantes[i]);
                a.moveTo(gx, PISO, gz, -90f, 0f);
                presa.moveTo(gx + dist[i], PISO, gz, 90f, 0f);
                presa.setNoAi(true);
                m.addFreshEntity(a);
                m.addFreshEntity(presa);
                quitar.add(a);
                quitar.add(presa);
                a.setTarget(presa);
                a.setYRot(-90f);
                a.yBodyRot = a.yHeadRot = -90f;
                a.empezar(acciones[i]);
                float vida = presa.getHealth();
                double x0 = a.getX(), z0 = a.getZ(), lejos = 0;
                boolean nublada = false, debil = false, frenada = false;
                for (int k = 0; k < dura[i]; k++) {
                    if (!a.isRemoved()) {
                        a.tickCount++;
                        a.tick();
                    }
                    tickMarcados(m, caja, quitar);
                    // la presa se mueve con lo que le den (los empujones): a mano, que sin IA no anda; y pisa el suelo (para la onda)
                    net.minecraft.world.phys.Vec3 dv = presa.getDeltaMovement();
                    if (dv.lengthSqr() > 1e-4) {
                        presa.move(net.minecraft.world.entity.MoverType.SELF, dv);
                        presa.setDeltaMovement(dv.x * 0.55, presa.onGround() ? 0 : dv.y - 0.08, dv.z * 0.55);
                    }
                    if (Math.abs(presa.getY() - PISO) < 0.05) presa.setOnGround(true);
                    nublada |= presa.hasEffect(net.minecraft.world.effect.MobEffects.DARKNESS);
                    debil |= presa.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS);
                    frenada |= presa.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
                    lejos = Math.max(lejos, Math.hypot(a.getX() - x0, a.getZ() - z0));
                    presa.invulnerableTime = 0;
                }
                String extra = switch (nombres[i]) {
                    case "mazazo", "escudazo" -> ", frenada " + frenada;
                    case "estocada" -> String.format(java.util.Locale.ROOT, ", se tira %.1f", lejos);
                    case "flecha negra" -> ", nublada " + nublada + ", debil " + debil;
                    case "desliz" -> String.format(java.util.Locale.ROOT, ", aparece a %.1f de donde estaba y a %.1f de la presa", lejos, a.distanceTo(presa));
                    default -> "";
                };
                sb.append(String.format(java.util.Locale.ROOT, " %s (%s) a %.1f: la presa -%.1f%s;", nombres[i], a.tipo(), dist[i], vida - presa.getHealth(), extra));
                for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Flecha.class, caja)) f.discard();
                a.discard();
                presa.discard();
            }
            sb.append(String.format(java.util.Locale.ROOT, " mazazos +%d (ondas +%d), barridos +%d, tajos +%d, estocadas +%d, escudazos +%d, disparos +%d,"
                            + " flechas negras +%d (nublan +%d), punzadas +%d, deslices +%d",
                    com.darkcraft.enemigos.Marcado.MAZAZOS - maz, com.darkcraft.enemigos.Marcado.ONDAS - ond, com.darkcraft.enemigos.Marcado.BARRIDOS - bar,
                    com.darkcraft.enemigos.Marcado.TAJOS - taj, com.darkcraft.enemigos.Marcado.ESTOCADAS - est, com.darkcraft.enemigos.Marcado.ESCUDAZOS - esc,
                    com.darkcraft.enemigos.Marcado.DISPAROS - dis, com.darkcraft.enemigos.Marcado.NEGRAS - neg, com.darkcraft.enemigos.Marcado.NUBLADOS - nub,
                    com.darkcraft.enemigos.Marcado.PUNZADAS - pun, com.darkcraft.enemigos.Marcado.DESLICES - des));
            log.println(sb);
            // ---- el escudo del caballero: de frente, por la espalda y una flecha de frente
            sb = new StringBuilder("Morvain, el escudo del caballero:");
            var cab = DarkCraftMod.MARCADO.get().create(m);
            var otro = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
            if (cab != null && otro != null) {
                cab.variante(C);
                cab.moveTo(gx, PISO, gz, -90f, 0f);
                cab.yBodyRot = cab.yHeadRot = -90f;
                otro.setNoAi(true);
                m.addFreshEntity(cab);
                m.addFreshEntity(otro);
                quitar.add(cab);
                quitar.add(otro);
                int par = com.darkcraft.enemigos.Marcado.PARADOS;
                float[] quita = new float[3];
                for (int vez = 0; vez < 3; vez++) {
                    cab.setHealth(cab.getMaxHealth());
                    cab.invulnerableTime = 0;
                    cab.moveTo(gx, PISO, gz, -90f, 0f);
                    cab.yBodyRot = cab.yHeadRot = -90f;
                    boolean frente = vez != 1;
                    otro.moveTo(gx + (frente ? 2.0 : -2.0), PISO, gz, 0f, 0f);
                    float antes = cab.getHealth();
                    if (vez == 2) {
                        var fl = new net.minecraft.world.entity.projectile.Arrow(m, otro);
                        fl.setPos(gx + 1.2, PISO + 1.4, gz);
                        cab.hurt(m.damageSources().arrow(fl, otro), 8f);
                    } else cab.hurt(m.damageSources().mobAttack(otro), 8f);
                    quita[vez] = antes - cab.getHealth();
                    for (int k = 0; k < 20; k++) cab.tick();
                }
                sb.append(String.format(java.util.Locale.ROOT, " un golpe de 8 de frente le quita %.1f, por la espalda %.1f, una flecha de frente %.1f; parados +%d;"
                        + " la marca arde %s", quita[0], quita[1], quita[2], com.darkcraft.enemigos.Marcado.PARADOS - par, cab.arde()));
                // por la espalda hasta media vida: le arde la marca
                cab.setHealth(cab.getMaxHealth() * 0.55f);
                cab.invulnerableTime = 0;
                otro.moveTo(gx - 2.0, PISO, gz, 0f, 0f);
                cab.hurt(m.damageSources().mobAttack(otro), 6f);
                sb.append(String.format(java.util.Locale.ROOT, "; herido hasta %.0f de %.0f: la marca arde %s", cab.getHealth(), cab.getMaxHealth(), cab.arde()));
                cab.discard();
                otro.discard();
            }
            log.println(sb);
            // ---- la marca que arde: el reflejo repite el golpe un instante despues desde donde se dio (y no se le puede pegar)
            sb = new StringBuilder("Morvain, el reflejo de los marcados:");
            int[] repiten = {com.darkcraft.enemigos.MarcadoAnims.MAZAZO, com.darkcraft.enemigos.MarcadoAnims.TAJO, com.darkcraft.enemigos.MarcadoAnims.DISPARO};
            int[] deQuien = {R, C, Z};
            double[] aDist = {2.6, 2.2, 9.0};
            for (int i = 0; i < repiten.length; i++) {
                var a = DarkCraftMod.MARCADO.get().create(m);
                var presa = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
                if (a == null || presa == null) continue;
                a.variante(deQuien[i]);
                a.moveTo(gx, PISO, gz, -90f, 0f);
                presa.moveTo(gx + aDist[i], PISO, gz, 90f, 0f);
                presa.setNoAi(true);
                m.addFreshEntity(a);
                m.addFreshEntity(presa);
                quitar.add(a);
                quitar.add(presa);
                a.encenderMarca();
                a.setTarget(presa);
                a.setYRot(-90f);
                a.yBodyRot = a.yHeadRot = -90f;
                int ref = com.darkcraft.enemigos.Marcado.REFLEJOS, dan = com.darkcraft.enemigos.Marcado.REFLEJOS_DAN;
                a.empezar(repiten[i]);
                int sale = -1, seVa = -1;
                boolean pegable = false, golpeado = false;
                float golpe = 0f, deLaSombra = 0f;
                com.darkcraft.enemigos.Marcado sombra = null;
                for (int k = 0; k < 90; k++) {
                    float h = presa.getHealth();
                    if (!a.isRemoved()) {
                        a.tickCount++;
                        a.tick();
                    }
                    if (sombra == null)
                        for (var r : m.getEntitiesOfClass(com.darkcraft.enemigos.Marcado.class, caja))
                            if (r.reflejo() && !r.isRemoved()) {
                                sombra = r;
                                sale = k;
                                pegable = r.isPickable();
                                golpeado = r.hurt(m.damageSources().mobAttack(presa), 10f);
                            }
                    tickMarcados(m, caja, quitar);
                    // lo que le quitan: antes de salir el reflejo, el golpe de verdad; despues, el del reflejo
                    if (sombra == null) golpe += Math.max(0f, h - presa.getHealth());
                    else deLaSombra += Math.max(0f, h - presa.getHealth());
                    // la presa quieta en su sitio (que el reflejo la encuentre donde estaba)
                    presa.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    presa.moveTo(gx + aDist[i], PISO, gz, 90f, 0f);
                    presa.setOnGround(true);
                    presa.invulnerableTime = 0;
                    if (presa.getHealth() < 8f) presa.setHealth(presa.getMaxHealth());
                    if (sombra != null && seVa < 0 && sombra.isRemoved()) seVa = k;
                    if (seVa >= 0 && a.accion() == 0) break;
                }
                sb.append(String.format(java.util.Locale.ROOT, " %s (%s): el golpe le quita %.1f; sale el reflejo en el tick %d (+%d) y le quita %.1f (golpes +%d);"
                                + " se puede apuntar %s, se le puede pegar %s; se deshace en el tick %d;", com.darkcraft.enemigos.MarcadoAnims.NOMBRES[repiten[i]],
                        a.tipo(), golpe, sale, com.darkcraft.enemigos.Marcado.REFLEJOS - ref, deLaSombra, com.darkcraft.enemigos.Marcado.REFLEJOS_DAN - dan, pegable,
                        golpeado, seVa));
                for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Flecha.class, caja)) f.discard();
                for (var r : m.getEntitiesOfClass(com.darkcraft.enemigos.Marcado.class, caja)) if (r.reflejo()) r.discard();
                a.discard();
                presa.discard();
            }
            sb.append(" (el reflejo sale ").append(com.darkcraft.enemigos.Marcado.RETRASO).append(" ticks despues del golpe)");
            log.println(sb);
            // ---- uno que cae: se lleva la mano a la marca, cae de bruces y se deshace en sombra
            var c = DarkCraftMod.MARCADO.get().create(m);
            if (c != null) {
                c.variante(R);
                c.moveTo(gx, PISO, gz, 0f, 0f);
                m.addFreshEntity(c);
                quitar.add(c);
                int d0 = com.darkcraft.enemigos.Marcado.DESHECHOS;
                c.hurt(m.damageSources().generic(), 9999f);
                int k = 0;
                for (; k < 80 && !c.isRemoved(); k++) c.tick();
                log.println(String.format(java.util.Locale.ROOT, "Morvain, un marcado que cae: se deshace en sombra +%d y se va en %d ticks",
                        com.darkcraft.enemigos.Marcado.DESHECHOS - d0, k));
            }
        } catch (Throwable t) {
            log.println("Morvain, los marcados: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] marcados", t);
        } finally {
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Flecha.class, caja)) f.discard();
            for (var r : m.getEntitiesOfClass(com.darkcraft.enemigos.Marcado.class, caja)) if (r.reflejo()) r.discard();
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    /** Lo que deja Aldric (sus reflejos): un tick, a mano (la prueba no mueve el mundo). */
    // ------------------------------------------------------------------ parte 7y: el Teatro Hundido y Pell, el Bufon del Rey

    /**
     * Parte 7y: el Teatro Hundido. Se levantan sus chunks; a pie desde la entrada (el camino del santuario del Teatro) se llega a la
     * grada, la orquesta, el escenario, los pasos y los bastidores, pero no al pasillo ni a la sala con la puerta real tapiada;
     * golpear el tapiado (un jugador mirandolo, como el combate) lo deshace (muro ilusorio): entonces se llega al pasillo, a la sala y
     * a Pell; con el muro del trono deshecho, al camerino. Lo que hay (el atril con el libro 32, los arcones, la grada, el trono),
     * el santuario con su nombre, los sitios con nombre del Espejo y los calcinados del teatro en su sitio. Al acabar, los muros
     * cerrados otra vez (la prueba del cliente juega en este mundo).
     */
    private static void teatro(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.TEATRO;
        var T = com.darkcraft.morvain.Teatro.get();
        final int CX = com.darkcraft.morvain.Teatro.CX, CZ = com.darkcraft.morvain.Teatro.CZ;
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        try {
            long t0 = System.currentTimeMillis();
            int nch = 0;
            for (long k : T.chunks()) {
                m.getChunk((int) (k >> 32), (int) k);
                nch++;
            }
            long ms = System.currentTimeMillis() - t0;
            com.darkcraft.morvain.MurosIlusorios.cerrar(m, com.darkcraft.morvain.Teatro.MURO_PUERTA);
            com.darkcraft.morvain.MurosIlusorios.cerrar(m, com.darkcraft.morvain.Teatro.MURO_TRONO);
            var aire = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
                var st = m.getBlockState(new BlockPos(x, y, z));
                return st.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock || st.getBlock() instanceof net.minecraft.world.level.block.CandleBlock
                        ? aire : st;
            };
            int[] en = T.sitio("entrada");
            StringBuilder sb = new StringBuilder("Morvain, el teatro hundido: " + nch + " chunks en " + ms + " ms; " + com.darkcraft.morvain.Teatro.resumen(m));
            String[] fases = {"todo cerrado", "la puerta real abierta", "los dos muros abiertos"};
            for (int fase = 0; fase < 3; fase++) {
                if (fase == 1) {
                    // un jugador ante el tapiado, mirandolo, golpea (lo que hace el combate)
                    var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("7d9e1f30-5b62-4d8e-8fa0-4b5c6d7e8fcd"), "[DarkCraftCI14]"));
                    int[] q = T.sitio("puerta");
                    fp.moveTo(q[0] + 0.5, q[1], q[2] + 0.5, 180f, 0f);
                    fp.setYHeadRot(180f);
                    int ab0 = com.darkcraft.morvain.MurosIlusorios.ABIERTOS;
                    com.darkcraft.morvain.MurosIlusorios.golpe(fp);
                    sb.append("; golpe al tapiado: se abre +").append(com.darkcraft.morvain.MurosIlusorios.ABIERTOS - ab0).append(" (")
                            .append(com.darkcraft.morvain.MurosIlusorios.estado(m)).append(')');
                }
                if (fase == 2) com.darkcraft.morvain.MurosIlusorios.abrir(m, com.darkcraft.morvain.MurosIlusorios.muro(com.darkcraft.morvain.Teatro.MURO_TRONO), null);
                var zona = com.darkcraft.pueblo.gen.Andar.desde(mundo, en[0], en[1], en[2], com.darkcraft.morvain.Teatro.X0, com.darkcraft.morvain.Teatro.Z0,
                        com.darkcraft.morvain.Teatro.X1, com.darkcraft.morvain.Teatro.Z1, 6_000_000);
                sb.append("; a pie, ").append(fases[fase]).append(" (").append(zona.celdas()).append(" celdas):");
                for (var e : T.sitios().entrySet()) {
                    if (e.getKey().equals("alto")) continue;
                    int[] q = e.getValue();
                    sb.append(' ').append(e.getKey()).append(zona.alcanza(q[0], q[1], q[2], 2) ? "+" : "-");
                }
                if (fase == 2) {
                    int lejos = 0;
                    for (var o : T.esperas()) if (!zona.alcanza(o.x(), o.y(), o.z(), 2)) lejos++;
                    sb.append("; de los ").append(T.esperas().size()).append(" del teatro, a los que no se llega ").append(lejos);
                }
            }
            var st = com.darkcraft.morvain.SantuariosMorvain.porId("teatro");
            int[] sala = T.sitio("pell"), orq = T.sitio("orquesta"), esc = T.sitio("escena");
            sb.append("; santuario ").append(com.darkcraft.santuario.Santuarios.nombre(m, st.pos())).append(" (").append(m.getBlockState(st.pos()).getBlock().getDescriptionId())
                    .append(") a ").append(Math.round(com.darkcraft.morvain.Teatro.distancia(st.x(), st.z()))).append(" de la orquesta; el sitio: orquesta ")
                    .append(com.darkcraft.morvain.Teatro.dentro(orq[0] + 0.5, orq[2] + 0.5)).append(", sala ").append(com.darkcraft.morvain.Teatro.enSala(sala[0] + 0.5, sala[1], sala[2] + 0.5))
                    .append(", el escenario no es la sala ").append(!com.darkcraft.morvain.Teatro.enSala(esc[0] + 0.5, esc[1], esc[2] + 0.5));
            log.println(sb);
            topView(m, CX, CZ - 10, 64, new File(out, "morvain_teatro.png"));
            // ---- los calcinados del teatro: en su sitio, en su suelo y ninguno metido en un bloque
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            for (int k = 0; k < 8; k++)
                for (var e : fuera) if (e.isAlive() && !(e instanceof com.darkcraft.enemigos.Bufon)) e.tick();
            int[] porVariante = new int[3];
            int calcinados = 0, enSuelo = 0, chocan = 0, tumbados = 0, agazapados = 0, pell = 0;
            StringBuilder mal = new StringBuilder();
            for (var e : fuera) {
                if (e instanceof com.darkcraft.enemigos.Bufon) {
                    pell++;
                    continue;
                }
                if (!(e instanceof com.darkcraft.enemigos.Calcinado c)) continue;
                calcinados++;
                porVariante[Math.max(0, Math.min(2, c.variante()))]++;
                if (!m.noCollision(c)) {
                    chocan++;
                    if (mal.length() < 300) mal.append(" metido en un bloque en ").append(c.blockPosition().toShortString()).append(';');
                }
                if (c.postura() == com.darkcraft.enemigos.Enemigo.TUMBADO) tumbados++;
                if (c.postura() == com.darkcraft.enemigos.Enemigo.REZA) agazapados++;
                var abajo = m.getBlockState(BlockPos.containing(c.getX(), c.getY() - 0.2, c.getZ()));
                if (!abajo.getCollisionShape(m, BlockPos.ZERO).isEmpty()) enSuelo++;
                else if (mal.length() < 400) mal.append(" en el aire en ").append(c.blockPosition().toShortString()).append(';');
            }
            log.println("Morvain, los del teatro: fuera " + fuera.size() + " de " + com.darkcraft.morvain.MorvainEnemigos.teatro().size() + " (Pell " + pell
                    + ", calcinados " + calcinados + ": guardia " + porVariante[0] + ", incendiarios " + porVariante[1] + ", cenicientos " + porVariante[2]
                    + "; enterrados en la ceniza " + tumbados + ", agazapados " + agazapados + ", en su suelo " + enSuelo + "); metidos en un bloque " + chocan
                    + " (tiene que ser 0)" + mal);
        } catch (Throwable t) {
            log.println("Morvain, el teatro hundido: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] teatro", t);
        } finally {
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    /** Hace andar lo que tira Pell (cuchillos, bolas, llamas) y sus ilusiones en la caja. */
    private static void tickBufon(ServerLevel m, net.minecraft.world.phys.AABB caja, java.util.List<net.minecraft.world.entity.Entity> quitar) {
        for (var e : m.getEntitiesOfClass(net.minecraft.world.entity.Entity.class, caja,
                q -> q instanceof com.darkcraft.enemigos.ProyectilBufon || q instanceof com.darkcraft.enemigos.Cantaro.Llamas
                        || q instanceof com.darkcraft.enemigos.Bufon b && b.ilusion())) {
            if (e.isRemoved()) continue;
            if (!quitar.contains(e)) quitar.add(e);
            e.tickCount++;
            e.tick();
        }
    }

    /**
     * Parte 7y: Pell, el Bufon del Rey, en la Sala de las Mascaras: de pie en medio de su sala (y en su suelo); al ver a alguien
     * en su sala habla y hace la reverencia; cada accion acaba; sus golpes a una presa (el tajo doble, la pirueta, los tres
     * cuchillos, los malabares con sus tres bolas de fuego que revientan, la bomba de humo que ciega y de la que sale por la espalda,
     * la voltereta en la que no le entra nada); a media vida se parte en tres (dos ilusiones que se deshacen de un golpe, y pegarle
     * a el deshace las que queden); y al caer se queda tumbado, el teatro aplaude, se deshace y no vuelve. Al acabar, todo como
     * estaba.
     */
    private static void bufon(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.TEATRO;
        var T = com.darkcraft.morvain.Teatro.get();
        final int CX = com.darkcraft.morvain.Teatro.CX, CZ = com.darkcraft.morvain.Teatro.CZ, SALA = com.darkcraft.morvain.Teatro.SALA;
        final int SV = com.darkcraft.morvain.Teatro.SALA_V;
        final var caja = new net.minecraft.world.phys.AABB(CX - 20, SALA - 4, CZ + SV - 20, CX + 20, SALA + 14, CZ + SV + 20);
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        com.darkcraft.enemigos.Bufon pe = null;
        try {
            for (long k : T.chunks()) m.getChunk((int) (k >> 32), (int) k);
            com.darkcraft.morvain.MurosIlusorios.abrir(m, com.darkcraft.morvain.MurosIlusorios.muro(com.darkcraft.morvain.Teatro.MURO_PUERTA), null);
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio)) if (e instanceof com.darkcraft.enemigos.Bufon b && !b.ilusion()) pe = b;
            if (pe == null) {
                log.println("Morvain, Pell: NO SALE");
                return;
            }
            final double px = CX + 0.5, py = SALA + 1, pz = CZ + SV + 0.5;
            boolean enSuSuelo = !m.getBlockState(BlockPos.containing(pe.getX(), pe.getY() - 0.2, pe.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty();
            StringBuilder sb = new StringBuilder(String.format(java.util.Locale.ROOT, "Morvain, Pell: en %s postura %d (0 de pie), giro %d, vida %.0f, golpe %.1f,"
                            + " armadura %d, caja %.1f x %.1f, en medio de su sala %s, en su suelo %s, metido en un bloque %s (tiene que ser false), musica %d",
                    pe.blockPosition().toShortString(), pe.postura(), Math.round(pe.getYRot()), pe.getMaxHealth(),
                    pe.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE), pe.getArmorValue(), pe.getBbWidth(), pe.getBbHeight(),
                    Math.hypot(pe.getX() - px, pe.getZ() - pz) < 2, enSuSuelo, !m.noCollision(pe), pe.musica()));
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("8e0f2041-6c73-4e9f-90b1-5c6d7e8f90de"), "[DarkCraftCI15]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            // ---- le ve entrar en su sala: habla y hace la reverencia (no ataca hasta acabar la frase)
            final double fx = CX + 0.5, fy = SALA + 1, fz = CZ + SV + 9.5;
            fp.moveTo(fx, fy, fz, 180f, 0f);
            fp.setOnGround(true);
            boolean percibe = false;
            try {
                java.lang.reflect.Method mt = com.darkcraft.enemigos.Bufon.class.getDeclaredMethod("percibe", net.minecraft.world.entity.LivingEntity.class);
                mt.setAccessible(true);
                percibe = (Boolean) mt.invoke(pe, fp);
            } catch (Throwable ignored) {
            }
            pe.setTarget(fp);
            int reverencia = -1, primerGolpe = -1;
            for (int k = 0; k < 260; k++) {
                pe.tickCount++;
                pe.tick();
                tickBufon(m, caja, quitar);
                fp.moveTo(fx, fy, fz, 180f, 0f);
                fp.setOnGround(true);
                fp.setHealth(fp.getMaxHealth());
                int a = pe.accion();
                if (reverencia < 0 && a == com.darkcraft.enemigos.BufonAnims.REVERENCIA) reverencia = k;
                if (primerGolpe < 0 && a != 0 && a != com.darkcraft.enemigos.BufonAnims.REVERENCIA && a != com.darkcraft.enemigos.BufonAnims.RISA) {
                    primerGolpe = k;
                    break;
                }
            }
            sb.append("; le ve entrar ").append(percibe).append(": pelea ").append(pe.enPelea()).append(", la reverencia en el tick ").append(reverencia)
                    .append(", el primer golpe en el ").append(primerGolpe).append(" (la frase 1 dura ").append(com.darkcraft.rey.Voces.ticks(com.darkcraft.enemigos.Bufon.VOZ_BASE + 1))
                    .append(')');
            // ---- cada accion acaba (sin presa y quieto)
            pe.quieta = true;
            pe.setTarget(null);
            for (int k = 0; k < 120 && pe.accion() != 0; k++) {
                pe.tickCount++;
                pe.tick();
                tickBufon(m, caja, quitar);
            }
            StringBuilder nn = new StringBuilder();
            int vistas = 0;
            for (int a = 1; a < com.darkcraft.enemigos.BufonAnims.TICKS.length; a++) {
                String nombre = com.darkcraft.enemigos.BufonAnims.NOMBRES[a];
                if (nombre.equals("morir")) continue;
                pe.moveTo(px, py, pz, 0f, 0f);
                pe.empezar(a);
                vistas++;
                for (int k = 0; k < 160 && pe.accion() == a; k++) {
                    pe.tickCount++;
                    pe.tick();
                    tickBufon(m, caja, quitar);
                }
                if (pe.accion() == a) nn.append(' ').append(nombre);
                // (lo que deja: las ilusiones y el humo)
                for (int k = 0; k < 40 && (pe.accion() != 0 || pe.isInvisible()); k++) {
                    pe.tickCount++;
                    pe.tick();
                }
            }
            for (var b : m.getEntitiesOfClass(com.darkcraft.enemigos.Bufon.class, caja, b -> b.ilusion())) b.discard();
            pe.setInvisible(false);
            sb.append("; de ").append(vistas).append(" acciones no acaban:").append(nn.length() == 0 ? " ninguna" : nn);
            log.println(sb);
            // ---- sus golpes a una presa (un aldeano quieto y duro) en su sala, delante de el (al sur)
            StringBuilder sg = new StringBuilder("Morvain, los golpes de Pell a una presa:");
            var victima = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
            if (victima != null) {
                victima.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(900.0);
                victima.setHealth(900f);
                victima.setNoAi(true);
                m.addFreshEntity(victima);
                quitar.add(victima);
                String[] golpes = {"tajo", "pirueta", "cuchillos", "malabares", "humo", "voltereta"};
                int[] acc = {com.darkcraft.enemigos.BufonAnims.TAJO, com.darkcraft.enemigos.BufonAnims.PIRUETA, com.darkcraft.enemigos.BufonAnims.CUCHILLOS,
                        com.darkcraft.enemigos.BufonAnims.MALABARES, com.darkcraft.enemigos.BufonAnims.HUMO, com.darkcraft.enemigos.BufonAnims.VOLTERETA};
                double[] lejos = {2.4, 2.2, 8.0, 8.0, 2.6, 2.2};
                int[] dura = {40, 40, 70, 110, 60, 50};
                for (int i = 0; i < golpes.length; i++) {
                    victima.setHealth(victima.getMaxHealth());
                    victima.invulnerableTime = 0;
                    victima.hurtTime = 0;
                    victima.removeAllEffects();
                    victima.clearFire();
                    victima.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    victima.moveTo(px, py, pz + lejos[i], 180f, 0f);
                    victima.setOnGround(true);
                    int ta = pe.tajos(), pi = pe.piruetas(), cu = pe.cuchillos(), bo = pe.bolas(), hu = pe.humos(), ap = pe.apariciones(), vo = pe.volteretas(),
                            es = pe.esquivados();
                    int cd = com.darkcraft.enemigos.ProyectilBufon.CUCHILLOS_DAN, bd = com.darkcraft.enemigos.ProyectilBufon.BOLAS_DAN,
                            br = com.darkcraft.enemigos.ProyectilBufon.BOLAS_REVIENTAN;
                    pe.moveTo(px, py, pz, 0f, 0f);
                    pe.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    pe.setTarget(victima);
                    pe.setYRot(0f);
                    pe.yBodyRot = pe.yHeadRot = 0f;
                    pe.empezar(acc[i]);
                    float antes = victima.getHealth();
                    boolean esquiva = false, invisible = false;
                    double lejosAlSalir = -1;
                    for (int k = 0; k < dura[i]; k++) {
                        boolean el = pe.accion() != 0 || pe.isInvisible();
                        if (el) {
                            pe.setTarget(victima);
                            pe.tickCount++;
                            pe.tick();
                            // (el humo y la voltereta le llevan: no se le devuelve a su sitio)
                            if (acc[i] != com.darkcraft.enemigos.BufonAnims.HUMO && acc[i] != com.darkcraft.enemigos.BufonAnims.VOLTERETA) pe.moveTo(px, py, pz, 0f, 0f);
                        }
                        if (pe.isInvisible()) invisible = true;
                        if (acc[i] == com.darkcraft.enemigos.BufonAnims.HUMO && lejosAlSalir < 0 && pe.accion() == com.darkcraft.enemigos.BufonAnims.APARECE)
                            lejosAlSalir = Math.hypot(pe.getX() - victima.getX(), pe.getZ() - victima.getZ());
                        if (acc[i] == com.darkcraft.enemigos.BufonAnims.VOLTERETA && pe.accion() == com.darkcraft.enemigos.BufonAnims.VOLTERETA && k == 6) {
                            pe.invulnerableTime = 0;
                            esquiva = !pe.hurt(m.damageSources().playerAttack(fp), 5f);
                        }
                        tickBufon(m, caja, quitar);
                        moverPresa(victima);
                        victima.setOnGround(true);
                        victima.invulnerableTime = 0;
                        if (!el && k > 30) break;
                    }
                    sg.append(' ').append(golpes[i]).append(String.format(java.util.Locale.ROOT, " a %.1f: -%.1f", lejos[i], antes - victima.getHealth()));
                    switch (i) {
                        case 0 -> sg.append(", tajos +").append(pe.tajos() - ta);
                        case 1 -> sg.append(", piruetas +").append(pe.piruetas() - pi);
                        case 2 -> sg.append(", cuchillos +").append(pe.cuchillos() - cu).append(" (dan +").append(com.darkcraft.enemigos.ProyectilBufon.CUCHILLOS_DAN - cd).append(')');
                        case 3 -> sg.append(", bolas +").append(pe.bolas() - bo).append(" (revientan +").append(com.darkcraft.enemigos.ProyectilBufon.BOLAS_REVIENTAN - br)
                                .append(", dan de lleno +").append(com.darkcraft.enemigos.ProyectilBufon.BOLAS_DAN - bd).append(", la presa arde ").append(victima.getRemainingFireTicks() > 0)
                                .append(')');
                        case 4 -> sg.append(", humos +").append(pe.humos() - hu).append(" (se esfuma ").append(invisible).append(", sale +").append(pe.apariciones() - ap)
                                .append(String.format(java.util.Locale.ROOT, " a %.1f de su presa)", lejosAlSalir));
                        case 5 -> sg.append(", volteretas +").append(pe.volteretas() - vo).append(" (esquiva el golpe ").append(esquiva).append(", +")
                                .append(pe.esquivados() - es).append(String.format(java.util.Locale.ROOT, "; queda a %.1f de su presa)",
                                        Math.hypot(pe.getX() - victima.getX(), pe.getZ() - victima.getZ())));
                        default -> {
                        }
                    }
                    sg.append(';');
                    pe.setInvisible(false);
                }
                // los cegados por el humo: un jugador a su lado
                fp.moveTo(px + 1.2, py, pz + 1.2, 180f, 0f);
                fp.removeAllEffects();
                int ce = pe.cegados();
                pe.moveTo(px, py, pz, 0f, 0f);
                pe.setTarget(fp);
                pe.empezar(com.darkcraft.enemigos.BufonAnims.HUMO);
                for (int k = 0; k < 30 && (pe.accion() != 0 || pe.isInvisible()); k++) {
                    pe.tickCount++;
                    pe.tick();
                    fp.moveTo(px + 1.2, py, pz + 1.2, 180f, 0f);
                }
                sg.append(" el humo ciega a un jugador a su lado +").append(pe.cegados() - ce).append(" (ciego ")
                        .append(fp.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS)).append(");");
                fp.removeAllEffects();
                pe.setInvisible(false);
                sg.append(" en total: ").append(pe.estado());
            }
            log.println(sg);
            // ---- a media vida se parte en tres: las ilusiones se deshacen de un golpe y pegarle a el deshace las que queden
            StringBuilder sm = new StringBuilder("Morvain, Pell a media vida:");
            pe.setHealth(pe.getMaxHealth());
            pe.moveTo(px, py, pz, 0f, 0f);
            if (victima != null) {
                victima.moveTo(px, py, pz + 4, 180f, 0f);
                pe.setTarget(victima);
            }
            pe.mediaVida();
            boolean seParte = pe.accion() == com.darkcraft.enemigos.BufonAnims.ILUSION;
            for (int k = 0; k < 40 && pe.accion() == com.darkcraft.enemigos.BufonAnims.ILUSION; k++) {
                pe.tickCount++;
                pe.tick();
                tickBufon(m, caja, quitar);
            }
            var copias = new java.util.ArrayList<>(pe.ilusiones());
            int nCopias = copias.size();
            boolean iguales = true, sinBarra = true, noSeGuardan = true;
            for (var c : copias) {
                if (c.variante() != pe.variante()) iguales = false;
                if (c.enPelea()) sinBarra = false;
                if (c.shouldBeSaved()) noSeGuardan = false;
            }
            // a una se le pega: se deshace (y a el no le pasa nada)
            boolean unaSeDeshace = false;
            float vidaAntes = pe.getHealth();
            if (!copias.isEmpty()) {
                var c = copias.get(0);
                c.hurt(m.damageSources().playerAttack(fp), 3f);
                unaSeDeshace = c.isRemoved();
            }
            float trasCopia = pe.getHealth();
            int quedanAntes = pe.ilusiones().size();
            pe.invulnerableTime = 0;
            pe.hurt(m.damageSources().playerAttack(fp), 6f);
            int quedanDespues = pe.ilusiones().size();
            sm.append(" segunda ").append(pe.segunda()).append(", se parte ").append(seParte).append(" (ilusiones ").append(nCopias)
                    .append(", iguales a el ").append(iguales).append(", sin barra ").append(sinBarra).append(", no se guardan ").append(noSeGuardan)
                    .append("); a una se le pega y se deshace ").append(unaSeDeshace).append(String.format(java.util.Locale.ROOT, " (a el no le quita: %.0f -> %.0f",
                            vidaAntes, trasCopia)).append("); quedan ").append(quedanAntes)
                    .append(" y al pegarle a el ").append(quedanDespues).append(" (tiene que ser 0); ").append(pe.estado());
            log.println(sm);
            for (var b : m.getEntitiesOfClass(com.darkcraft.enemigos.Bufon.class, caja, b -> b.ilusion())) b.discard();
            // ---- cae: tumbado, el teatro aplaude, se deshace en confeti y no vuelve
            pe.quieta = true;
            for (int k = 0; k < 120 && pe.accion() != 0; k++) {
                pe.tickCount++;
                pe.tick();
            }
            pe.setTarget(fp);
            pe.invulnerableTime = 0;
            pe.hurt(m.damageSources().playerAttack(fp), 99999f);
            boolean muere = !pe.isAlive();
            int seVa = -1;
            for (int fin = 0; fin < 400; fin++) {
                if (!pe.isRemoved()) {
                    pe.tickCount++;
                    pe.tick();
                }
                if (seVa < 0 && pe.isRemoved()) {
                    seVa = fin;
                    break;
                }
            }
            com.darkcraft.enemigos.Puestos.marcar(server, sitio, com.darkcraft.morvain.MorvainEnemigos.bufon(), true);
            int objetos = m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, caja).size();
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            com.darkcraft.enemigos.Puestos.marcar(server, sitio, com.darkcraft.morvain.MorvainEnemigos.bufon(), true);
            boolean vuelve = false;
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio)) if (e instanceof com.darkcraft.enemigos.Bufon) vuelve = true;
            log.println("Morvain, Pell cae: muere " + muere + ", se deshace en el tick " + seVa + " (la frase 5 dura " + com.darkcraft.rey.Voces.ticks(com.darkcraft.enemigos.Bufon.VOZ_BASE + 5)
                    + "), objetos " + objetos + ", vuelve al descansar " + vuelve + " (tiene que ser false); " + com.darkcraft.morvain.MorvainEnemigos.estadoBufon(server));
            for (var it : m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, caja)) it.discard();
        } catch (Throwable t) {
            log.println("Morvain, Pell: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] bufon", t);
        } finally {
            if (pe != null) pe.quieta = false;
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            for (var b : m.getEntitiesOfClass(com.darkcraft.enemigos.Bufon.class, caja, b -> b.ilusion())) b.discard();
            // todo como antes: Pell en su sala, los calcinados en su sitio y los dos muros tapiados
            com.darkcraft.enemigos.Puestos.marcar(server, sitio, com.darkcraft.morvain.MorvainEnemigos.bufon(), false);
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            try {
                com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
                log.println("Morvain, Pell vuelve: " + com.darkcraft.morvain.MorvainEnemigos.estadoBufon(server));
            } catch (Throwable t) {
                log.println("Morvain, Pell vuelve: ERROR " + t);
            }
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            com.darkcraft.morvain.MurosIlusorios.cerrar(m, com.darkcraft.morvain.Teatro.MURO_PUERTA);
            com.darkcraft.morvain.MurosIlusorios.cerrar(m, com.darkcraft.morvain.Teatro.MURO_TRONO);
        }
    }

    // ------------------------------------------------------------------ parte 7z: el Nido de Ceniza y Vorthak, el Dragon de Ceniza

    /**
     * Parte 7z: el Nido de Ceniza. Se levantan sus chunks; a pie desde el santuario del Nido se llega a todos sus sitios (el camino,
     * la repisa, el tesoro, la atalaya, lo alto de su escalera, el atril y los arcones); lo que hay (el atril con el libro 33, los
     * arcones, el oro, la repisa llana y despejada, el muro de la torre); el santuario con su nombre; y los del nido en su sitio
     * (Vorthak dormido sobre su oro y los calcinados de la guarnicion).
     */
    private static void nido(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.NIDO;
        var N = com.darkcraft.morvain.Nido.get();
        final int CX = com.darkcraft.morvain.Nido.CX, CZ = com.darkcraft.morvain.Nido.CZ;
        try {
            long t0 = System.currentTimeMillis();
            int nch = 0;
            for (long k : N.chunks()) {
                m.getChunk((int) (k >> 32), (int) k);
                nch++;
            }
            long ms = System.currentTimeMillis() - t0;
            var aire = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
                var st = m.getBlockState(new BlockPos(x, y, z));
                return st.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock || st.getBlock() instanceof net.minecraft.world.level.block.CandleBlock
                        || st.getBlock() instanceof net.minecraft.world.level.block.SnowLayerBlock ? aire : st;
            };
            int[] sa = N.sitio("santuario");
            StringBuilder sb = new StringBuilder("Morvain, el nido de ceniza: " + nch + " chunks en " + ms + " ms; " + com.darkcraft.morvain.Nido.resumen(m));
            var zona = com.darkcraft.pueblo.gen.Andar.desde(mundo, sa[0], sa[1], sa[2], com.darkcraft.morvain.Nido.X0, com.darkcraft.morvain.Nido.Z0,
                    com.darkcraft.morvain.Nido.X1, com.darkcraft.morvain.Nido.Z1 + 20, 6_000_000);
            sb.append("; a pie desde el santuario (").append(zona.celdas()).append(" celdas):");
            for (var e : N.sitios().entrySet()) {
                if (e.getKey().equals("alto") || e.getKey().equals("dragon")) continue;
                int[] q = e.getValue();
                sb.append(' ').append(e.getKey()).append(zona.alcanza(q[0], q[1], q[2], 2) ? "+" : "-");
            }
            int lejos = 0;
            for (var o : N.esperas()) if (!com.darkcraft.enemigos.Puestos.JEFE.equals(o.grupo()) && !zona.alcanza(o.x(), o.y(), o.z(), 2)) lejos++;
            sb.append("; de los ").append(N.esperas().size() - 1).append(" calcinados del nido, a los que no se llega ").append(lejos);
            var st = com.darkcraft.morvain.SantuariosMorvain.porId("nido");
            int[] at = N.sitio("atalaya"), ce = N.sitio("centro");
            sb.append("; santuario ").append(com.darkcraft.santuario.Santuarios.nombre(m, st.pos())).append(" (").append(m.getBlockState(st.pos()).getBlock().getDescriptionId())
                    .append(") a ").append(Math.round(com.darkcraft.morvain.Nido.distancia(st.x(), st.z()))).append(" del centro; el sitio: repisa ")
                    .append(com.darkcraft.morvain.Nido.dentro(ce[0] + 0.5, ce[2] + 0.5)).append(", atalaya ")
                    .append(com.darkcraft.morvain.Nido.enAtalaya(at[0] + 7.5, at[1], at[2] + 0.5)).append(", el santuario no es la repisa ")
                    .append(!com.darkcraft.morvain.Nido.dentro(st.x() + 0.5, st.z() + 0.5));
            log.println(sb);
            topView(m, CX, CZ + 6, 64, new File(out, "morvain_nido.png"));
            // ---- los del nido: en su sitio, en su suelo y ninguno metido en un bloque (Vorthak no tiene fisica: duerme encima del oro)
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            for (int k = 0; k < 8; k++)
                for (var e : fuera) if (e.isAlive() && !(e instanceof com.darkcraft.enemigos.Dragon)) e.tick();
            int calcinados = 0, enSuelo = 0, chocan = 0, tumbados = 0, agazapados = 0;
            String dragon = "no sale";
            StringBuilder mal = new StringBuilder();
            for (var e : fuera) {
                if (e instanceof com.darkcraft.enemigos.Dragon d) {
                    int[] q = N.sitio("dragon");
                    dragon = String.format(java.util.Locale.ROOT, "en %s (su sitio %d,%d,%d; a %.1f), postura %d (1 dormido), sin fisica %s", d.blockPosition().toShortString(),
                            q[0], q[1], q[2], Math.hypot(d.getX() - q[0] - 0.5, d.getZ() - q[2] - 0.5), d.postura(), d.noPhysics);
                    continue;
                }
                if (!(e instanceof com.darkcraft.enemigos.Calcinado c)) continue;
                calcinados++;
                if (!m.noCollision(c)) {
                    chocan++;
                    if (mal.length() < 300) mal.append(" metido en un bloque en ").append(c.blockPosition().toShortString()).append(';');
                }
                if (c.postura() == com.darkcraft.enemigos.Enemigo.TUMBADO) tumbados++;
                if (c.postura() == com.darkcraft.enemigos.Enemigo.REZA) agazapados++;
                var abajo = m.getBlockState(BlockPos.containing(c.getX(), c.getY() - 0.2, c.getZ()));
                if (!abajo.getCollisionShape(m, BlockPos.ZERO).isEmpty()) enSuelo++;
                else if (mal.length() < 400) mal.append(" en el aire en ").append(c.blockPosition().toShortString()).append(';');
            }
            log.println("Morvain, los del nido: fuera " + fuera.size() + " de " + com.darkcraft.morvain.MorvainEnemigos.nido().size() + " (Vorthak " + dragon
                    + "; calcinados " + calcinados + ": enterrados en la ceniza " + tumbados + ", agazapados " + agazapados + ", en su suelo " + enSuelo
                    + "); metidos en un bloque " + chocan + " (tiene que ser 0)" + mal);
        } catch (Throwable t) {
            log.println("Morvain, el nido de ceniza: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] nido", t);
        } finally {
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    // ------------------------------------------------------------------ parte 7aa: la subida por la cadena del sur

    /**
     * Parte 7aa: la subida por la cadena del sur. Se levantan sus chunks; lo que hay ({@code Subida.resumen}: el atril con el libro
     * 34, el arcon, el santuario, los tablones en su sitio, la boca abierta, la planta baja de la torre y la puerta al patio); a pie
     * desde lo alto del ancla se llega a todos sus sitios (los huecos se saltan: aqui se ven con tablon); el santuario de la Mitad
     * de la Cadena con su nombre y en la lista de los de Morvain; donde se esta en cada sitio (el camino, el Descansillo, la Boca, la
     * ciudadela, y abajo en el Cerco ya no es la cadena); el corcel no viene en la cadena; y los de la cadena en su sitio: los
     * marcados en su suelo, las gargolas de los pedestales de piedra (de rodillas, sin fisica) y las de ronda volando; ninguno
     * metido en un bloque.
     */
    private static void cadena(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.CADENA;
        var S = com.darkcraft.morvain.Subida.get();
        try {
            long t0 = System.currentTimeMillis();
            int nch = 0;
            for (long k : S.chunks()) {
                m.getChunk((int) (k >> 32), (int) k);
                nch++;
            }
            long ms = System.currentTimeMillis() - t0;
            var aire = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            var losa = net.minecraft.world.level.block.Blocks.DARK_OAK_SLAB.defaultBlockState();
            // (los huecos se saltan: Andar no sabe saltar, asi que se ven como si tuvieran tablon)
            com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
                var st = m.getBlockState(new BlockPos(x, y, z));
                if (st.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock || st.getBlock() instanceof net.minecraft.world.level.block.CandleBlock) return aire;
                if (st.isAir() && x >= com.darkcraft.morvain.Subida.X0 && x <= com.darkcraft.morvain.Subida.X1 && S.hayCamino(z)) {
                    double sup = S.superficie(z);
                    int yb = sup == Math.floor(sup) ? (int) sup - 1 : (int) Math.floor(sup);
                    if (y == yb) return losa;
                }
                return st;
            };
            StringBuilder sb = new StringBuilder("Morvain, la cadena del sur: " + nch + " chunks en " + ms + " ms; " + com.darkcraft.morvain.Subida.resumen(m));
            int[] an = S.sitio("ancla");
            var zona = com.darkcraft.pueblo.gen.Andar.desde(mundo, an[0], an[1], an[2], com.darkcraft.morvain.Subida.BX0, com.darkcraft.morvain.Subida.BZ0,
                    com.darkcraft.morvain.Subida.BX1, com.darkcraft.morvain.Subida.BZ1, 8_000_000);
            sb.append("; a pie desde lo alto del ancla (").append(zona.celdas()).append(" celdas):");
            int faltan = 0;
            for (var e : S.sitios().entrySet()) {
                if (e.getKey().startsWith("lado")) continue;
                int[] q = e.getValue();
                boolean ok = zona.alcanza(q[0], q[1], q[2], 2);
                if (!ok) faltan++;
                sb.append(' ').append(e.getKey()).append(ok ? "+" : "-");
            }
            sb.append(" (sin llegar ").append(faltan).append(", tiene que ser 0)");
            var st = com.darkcraft.morvain.SantuariosMorvain.porId("cadena");
            sb.append("; santuario ").append(st == null ? "NO EN LA LISTA" : com.darkcraft.santuario.Santuarios.nombre(m, st.pos()) + " ("
                    + m.getBlockState(st.pos()).getBlock().getDescriptionId() + " en " + st.pos().toShortString() + ")");
            // donde se esta
            int[] de = S.sitio("descansillo"), bo = S.sitio("boca"), pa = S.sitio("patio"), me = S.sitio("medio");
            sb.append(String.format(java.util.Locale.ROOT, "; en el medio: camino %s, cadena %s; en el Descansillo %s; en la Boca %s; en el patio %s;"
                            + " abajo en el Cerco (0,%d,840): sobre la cadena %s",
                    com.darkcraft.morvain.Subida.enCamino(me[0] + 0.5, me[1], me[2] + 0.5), com.darkcraft.morvain.Subida.sobreCadena(me[0] + 0.5, me[1], me[2] + 0.5),
                    com.darkcraft.morvain.Subida.enDescansillo(de[0] + 0.5, de[1], de[2] + 0.5), com.darkcraft.morvain.Subida.enBoca(bo[0] + 0.5, bo[1], bo[2] + 0.5),
                    com.darkcraft.morvain.Subida.enCiudadela(pa[0] + 0.5, pa[1], pa[2] + 0.5), com.darkcraft.morvain.Cerco.PISO + 1,
                    com.darkcraft.morvain.Subida.sobreCadena(0.5, com.darkcraft.morvain.Cerco.PISO + 1, 840.5)));
            // el corcel no viene en la cadena
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("5b1da0aa-cade-4a50-8a11-0c0ffee07aa1"), "[DarkCraftCI17]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            fp.getInventory().add(new net.minecraft.world.item.ItemStack(DarkCraftMod.SILBATO_CORCEL.get()));
            fp.moveTo(me[0] + 0.5, me[1], me[2] + 0.5, 180f, 0f);
            boolean viene = com.darkcraft.montura.Montura.llamar(fp);
            sb.append("; el corcel en la cadena viene ").append(viene).append(" (tiene que ser false)");
            var c = com.darkcraft.montura.Montura.de(fp);
            if (c != null) c.discard();
            fp.getInventory().clearContent();
            log.println(sb);
            // ---- los de la cadena: en su sitio, en su suelo y ninguno metido en un bloque
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            for (int k = 0; k < 8; k++)
                for (var e : fuera) if (e.isAlive()) e.tick();
            int marcados = 0, enSuelo = 0, chocan = 0, piedra = 0, vuelan = 0, gargolas = 0;
            StringBuilder mal = new StringBuilder();
            for (var e : fuera) {
                if (e instanceof com.darkcraft.enemigos.Gargola g) {
                    gargolas++;
                    if (g.postura() == com.darkcraft.enemigos.Enemigo.REZA && g.noPhysics) piedra++;
                    if (g.vuela()) vuelan++;
                    if (!m.noCollision(g)) {
                        chocan++;
                        if (mal.length() < 300) mal.append(" gargola metida en un bloque en ").append(g.blockPosition().toShortString()).append(';');
                    }
                    continue;
                }
                marcados++;
                if (!m.noCollision(e)) {
                    chocan++;
                    if (mal.length() < 300) mal.append(" metido en un bloque en ").append(e.blockPosition().toShortString()).append(';');
                }
                var abajo = m.getBlockState(BlockPos.containing(e.getX(), e.getY() - 0.2, e.getZ()));
                if (!abajo.getCollisionShape(m, BlockPos.ZERO).isEmpty()) enSuelo++;
                else if (mal.length() < 400) mal.append(" en el aire en ").append(e.blockPosition().toShortString()).append(';');
            }
            log.println("Morvain, los de la cadena: fuera " + fuera.size() + " de " + com.darkcraft.morvain.MorvainEnemigos.cadena().size() + " (marcados "
                    + marcados + ", en su suelo " + enSuelo + "; gargolas " + gargolas + ": de piedra en su pedestal " + piedra + ", volando " + vuelan
                    + "); metidos en un bloque " + chocan + " (tiene que ser 0)" + mal);
        } catch (Throwable t) {
            log.println("Morvain, la cadena del sur: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] cadena", t);
        } finally {
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    /**
     * Parte 7aa: la gargola. La del primer rellano, de piedra en su pedestal: no ve a quien llega agachado a diez bloques, si de
     * pie, y no a veinte; despierta (se agrieta, grita) y alza el vuelo; en el aire da vueltas alrededor de su presa (por encima y a
     * un lado), se lanza en picado (le pilla y la echa de lado), le lanza las garras y se posa en el camino; en tierra el zarpazo
     * y el coletazo pegan y despega otra vez; muere en el aire: cae (con fisica) y se va.
     */
    private static void gargolas(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.CADENA;
        var S = com.darkcraft.morvain.Subida.get();
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder("Morvain, la gargola:");
        try {
            for (long k : S.chunks()) m.getChunk((int) (k >> 32), (int) k);
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            int[] r1 = S.sitio("rellano1");
            com.darkcraft.enemigos.Gargola g = null;
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio)) {
                if (g == null && e instanceof com.darkcraft.enemigos.Gargola q && q.postura() == com.darkcraft.enemigos.Enemigo.REZA
                        && Math.abs(q.getZ() - r1[2]) < 12) g = q;
                else e.discard();
            }
            if (g == null) {
                log.println("Morvain, la gargola: NO SALE");
                return;
            }
            sb.append(String.format(java.util.Locale.ROOT, " en %s (postura %d, sin fisica %s, vida %.0f, golpe %.1f, armadura %d)", g.blockPosition().toShortString(),
                    g.postura(), g.noPhysics, g.getMaxHealth(), g.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE), g.getArmorValue()));
            // ---- lo que ve de piedra
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("5b1da0aa-cade-4a50-8a11-0c0ffee07aa2"), "[DarkCraftCI18]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            String ve = "";
            try {
                java.lang.reflect.Method mt = com.darkcraft.enemigos.Gargola.class.getDeclaredMethod("percibe", net.minecraft.world.entity.LivingEntity.class);
                mt.setAccessible(true);
                double[][] pruebas = {{10, 1}, {10, 0}, {20, 0}};
                for (double[] pr : pruebas) {
                    // por el camino, cuesta abajo (al sur) desde la gargola
                    int z = (int) Math.floor(g.getZ() + pr[0]);
                    fp.moveTo(0.5, S.pisa(z), z + 0.5, 180f, 0f);
                    fp.setPose(pr[1] > 0 ? net.minecraft.world.entity.Pose.CROUCHING : net.minecraft.world.entity.Pose.STANDING);
                    ve += String.format(java.util.Locale.ROOT, " %s a %.0f: %s;", pr[1] > 0 ? "agachado" : "de pie", pr[0], mt.invoke(g, fp));
                }
                fp.setPose(net.minecraft.world.entity.Pose.STANDING);
            } catch (Throwable t) {
                ve = " ERROR " + t;
            }
            sb.append("; de piedra ve:").append(ve);
            // ---- la presa: un aldeano con mucha vida en el camino, delante del rellano
            var victima = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
            if (victima == null) {
                log.println(sb.append(" (sin presa)"));
                return;
            }
            victima.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(2000.0);
            victima.setHealth(2000f);
            victima.setNoAi(true);
            // (en el centro del rellano: llano)
            final double vx = r1[0] + 0.5, vy = r1[1], vz = r1[2] + 0.5;
            victima.moveTo(vx, vy, vz, 180f, 0f);
            m.addFreshEntity(victima);
            quitar.add(victima);
            g.setTarget(victima);
            int despierta = -1, vuela = -1;
            for (int k = 0; k < 120 && vuela < 0; k++) {
                pasoGargola(g, victima, vx, vy, vz);
                if (despierta < 0 && g.accion() == com.darkcraft.enemigos.GargolaAnims.DESPIERTA) despierta = k;
                if (vuela < 0 && g.vuela()) vuela = k;
            }
            sb.append(String.format(java.util.Locale.ROOT, "; despierta en el tick %d, vuela en el %d (postura %d, sin fisica %s)", despierta, vuela, g.postura(), g.noPhysics));
            // ---- en el aire: sus vueltas y sus ataques
            int pic0 = com.darkcraft.enemigos.Gargola.PICADOS, dan0 = com.darkcraft.enemigos.Gargola.PICADOS_DAN, gar0 = com.darkcraft.enemigos.Gargola.GARRAS_N,
                    pos0 = com.darkcraft.enemigos.Gargola.POSADAS, des0 = com.darkcraft.enemigos.Gargola.DESPEGUES;
            double sumaD = 0, sumaH = 0, empujon = 0;
            int nOrbita = 0, enBloque = 0;
            float antes = victima.getHealth();
            int posada = -1;
            for (int k = 0; k < 900; k++) {
                int dan = com.darkcraft.enemigos.Gargola.PICADOS_DAN;
                pasoGargolaSin(g);
                if (com.darkcraft.enemigos.Gargola.PICADOS_DAN > dan) empujon = Math.max(empujon, victima.getDeltaMovement().horizontalDistance());
                victima.moveTo(vx, vy, vz, 180f, 0f);
                victima.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                victima.setOnGround(true);
                victima.invulnerableTime = 0;
                if (g.vuela() && g.accion() == 0) {
                    sumaD += Math.hypot(g.getX() - vx, g.getZ() - vz);
                    sumaH += g.getY() - vy;
                    nOrbita++;
                }
                if (g.vuela() && !m.noCollision(g, g.getBoundingBox().deflate(0.2))) enBloque++;
                if (posada < 0 && !g.vuela() && g.isAlive() && k > 10) posada = k;
                // posada: lo que decide en tierra lo decide su cabeza del juego (que aqui no siempre anda): vuelve a volar
                if (!g.vuela() && g.accion() == 0 && k > 10) g.empezar(com.darkcraft.enemigos.GargolaAnims.DESPEGA);
                if (com.darkcraft.enemigos.Gargola.PICADOS - pic0 >= 1 && com.darkcraft.enemigos.Gargola.GARRAS_N - gar0 >= 1 && posada >= 0) break;
            }
            sb.append(String.format(java.util.Locale.ROOT, "; en el aire: a %.1f de su presa y %.1f por encima de media (%d ticks); picados %d (pillan %d, la echa %.2f por tick),"
                            + " garras %d, se posa %d (en el tick %d), despegues %d; dentro de un bloque volando %d ticks (tiene que ser 0); a la presa le quita %.1f",
                    nOrbita == 0 ? 0 : sumaD / nOrbita, nOrbita == 0 ? 0 : sumaH / nOrbita, nOrbita, com.darkcraft.enemigos.Gargola.PICADOS - pic0,
                    com.darkcraft.enemigos.Gargola.PICADOS_DAN - dan0, empujon, com.darkcraft.enemigos.Gargola.GARRAS_N - gar0,
                    com.darkcraft.enemigos.Gargola.POSADAS - pos0, posada, com.darkcraft.enemigos.Gargola.DESPEGUES - des0, enBloque, antes - victima.getHealth()));
            // ---- en tierra: el zarpazo de frente y el coletazo por detras, y vuelve a volar
            if (g.vuela()) g.probarVuelo(false);
            for (int k = 0; k < 40 && g.accion() != 0; k++) pasoGargolaSin(g);
            String[] golpes = {"zarpazo", "coletazo"};
            int[] acc = {com.darkcraft.enemigos.GargolaAnims.ZARPAZO, com.darkcraft.enemigos.GargolaAnims.COLETAZO};
            for (int i = 0; i < 2; i++) {
                // (mirando al sur, en el rellano: la presa dos bloques delante o dos detras)
                g.moveTo(vx, vy, vz - 1.0, 0f, 0f);
                g.setYRot(0f);
                g.yBodyRot = g.yHeadRot = 0f;
                victima.setHealth(victima.getMaxHealth());
                double px = vx, pz = i == 0 ? vz + 1.0 : vz - 3.0;
                g.setTarget(victima);
                g.empezar(acc[i]);
                float a0 = victima.getHealth();
                for (int k = 0; k < com.darkcraft.enemigos.GargolaAnims.TICKS[acc[i]] + 4 && g.accion() == acc[i]; k++) {
                    pasoGargolaSin(g);
                    victima.moveTo(px, vy, pz, 180f, 0f);
                    victima.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    victima.invulnerableTime = 0;
                }
                sb.append(String.format(java.util.Locale.ROOT, "; %s a %.1f: -%.1f", golpes[i], Math.hypot(px - g.getX(), pz - g.getZ()), a0 - victima.getHealth()));
            }
            int des1 = com.darkcraft.enemigos.Gargola.DESPEGUES;
            g.empezar(com.darkcraft.enemigos.GargolaAnims.DESPEGA);
            for (int k = 0; k < 30; k++) pasoGargolaSin(g);
            sb.append("; despega otra vez ").append(g.vuela()).append(" (+").append(com.darkcraft.enemigos.Gargola.DESPEGUES - des1).append(')');
            // ---- muere volando: cae y se va
            double y0 = g.getY();
            g.hurt(m.damageSources().genericKill(), 10000f);
            boolean cayo = false;
            int k;
            for (k = 0; k < 120 && !g.isRemoved(); k++) {
                g.tickCount++;
                g.tick();
                if (g.getY() < y0 - 0.5) cayo = true;
            }
            sb.append(String.format(java.util.Locale.ROOT, "; muere: vivo %s, volando %s, con fisica %s, cae %s, se va en %d ticks", g.isAlive(), g.vuela(), !g.noPhysics, cayo, k));
            log.println(sb);
        } catch (Throwable t) {
            log.println(sb + " ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] gargola", t);
        } finally {
            for (var e : quitar) e.discard();
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    /**
     * Parte 7ab: la torre del homenaje por dentro. Levanta sus chunks; el resumen (el atril con el libro 35, la Ultima Llama, los
     * arcones, el rastrillo y la reja de la azotea cerrados); a pie desde el patio: con el rastrillo cerrado no se entra; abierto, a
     * todo menos a la azotea; con la reja abierta, tambien; la Ultima Llama y su nombre; donde se esta; el rastrillo que sube solo
     * (CasaSombra); la reja que se abre con la reina caida y se cierra si vuelve; Maelis en la capilla; y los de la casa en su sitio.
     */
    private static void homenaje(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.HOMENAJE;
        var H = com.darkcraft.morvain.Homenaje.get();
        try {
            long t0 = System.currentTimeMillis();
            int nch = 0;
            for (long k : H.chunks()) {
                m.getChunk((int) (k >> 32), (int) k);
                nch++;
            }
            long ms = System.currentTimeMillis() - t0;
            StringBuilder sb = new StringBuilder("Morvain, la torre del homenaje: " + nch + " chunks en " + ms + " ms; " + com.darkcraft.morvain.Homenaje.resumen(m));
            // a pie desde el patio, cerrado, con el rastrillo abierto y con todo abierto (sin tocar el mundo: se ven como aire)
            var aire = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            for (int vez = 0; vez < 3; vez++) {
                java.util.Set<Long> quitar = new java.util.HashSet<>();
                if (vez >= 1) for (BlockPos b : com.darkcraft.morvain.Homenaje.rastrillo()) quitar.add(b.asLong());
                if (vez >= 2) for (BlockPos b : com.darkcraft.morvain.Homenaje.rejaAzotea()) quitar.add(b.asLong());
                com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
                    if (quitar.contains(BlockPos.asLong(x, y, z))) return aire;
                    var st = m.getBlockState(new BlockPos(x, y, z));
                    return st.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock || st.getBlock() instanceof net.minecraft.world.level.block.CandleBlock
                            || st.is(net.minecraft.world.level.block.Blocks.BARRIER) ? aire : st;
                };
                int[] pa = H.sitio("patio");
                var zona = com.darkcraft.pueblo.gen.Andar.desde(mundo, pa[0], pa[1], pa[2], com.darkcraft.morvain.Homenaje.BX0 - 4, com.darkcraft.morvain.Homenaje.BZ0 - 4,
                        com.darkcraft.morvain.Homenaje.BX1 + 4, com.darkcraft.morvain.Homenaje.BZ1 + 4, 8_000_000);
                int llega = 0, total = 0;
                StringBuilder no = new StringBuilder();
                for (var e : H.sitios().entrySet()) {
                    if (e.getKey().equals("frente")) continue;
                    int[] q = e.getValue();
                    total++;
                    if (zona.alcanza(q[0], q[1], q[2], 2)) llega++;
                    else no.append(' ').append(e.getKey());
                }
                sb.append("; ").append(vez == 0 ? "cerrado" : vez == 1 ? "con el rastrillo abierto" : "con todo abierto").append(", a pie desde el patio ")
                        .append(llega).append(" de ").append(total).append(" sitios (no:").append(no.length() == 0 ? " ninguno" : no).append(')');
            }
            sb.append(" (cerrado solo el patio, el camino y la puerta; con el rastrillo abierto todos menos azotea y puerta_sombra)");
            // la Ultima Llama
            var st = com.darkcraft.morvain.SantuariosMorvain.porId("ultima_llama");
            sb.append("; la Ultima Llama ").append(st == null ? "NO EN LA LISTA" : com.darkcraft.santuario.Santuarios.nombre(m, st.pos()) + " ("
                    + m.getBlockState(st.pos()).getBlock().getDescriptionId() + " en " + st.pos().toShortString() + ")");
            // donde se esta
            String[] salas = {"salon", "armeria", "archivo", "prision", "capilla", "sala_trono", "azotea", "patio"};
            sb.append("; donde se esta:");
            for (String n : salas) {
                int[] q = H.sitio(n);
                double x = q[0] + 0.5, y = q[1], z = q[2] + 0.5;
                String k = com.darkcraft.morvain.Homenaje.enHomenaje(x, y, z) ? com.darkcraft.morvain.Homenaje.sala(com.darkcraft.morvain.Homenaje.planta(y))
                        : com.darkcraft.morvain.Homenaje.enAzotea(x, y, z) ? "azotea" : "fuera";
                sb.append(' ').append(n).append('=').append(k.substring(k.lastIndexOf('.') + 1));
            }
            log.println(sb);
            // ---- la casa: el rastrillo que sube solo, la reja de la azotea con la reina caida (y si vuelve), Maelis en la capilla
            StringBuilder c = new StringBuilder("Morvain, la casa de la Sombra:");
            com.darkcraft.morvain.CasaSombra.cerrarRastrillo(m);
            c.append(" al empezar ").append(com.darkcraft.morvain.CasaSombra.resumen(m));
            com.darkcraft.morvain.CasaSombra.abrirRastrillo(m);
            int ticks = 0;
            for (; ticks < 80; ticks++) com.darkcraft.morvain.CasaSombra.tick(m);
            int barrotes = 0;
            for (BlockPos q : com.darkcraft.morvain.Homenaje.rastrillo()) if (m.getBlockState(q).is(net.minecraft.world.level.block.Blocks.IRON_BARS)) barrotes++;
            c.append("; el rastrillo sube: quedan ").append(barrotes).append(" barrotes (tiene que ser 0)");
            com.darkcraft.morvain.CasaSombra.revisar(m);
            c.append("; Maelis en la capilla: ").append(m.getEntitiesOfClass(com.darkcraft.personajes.Guia.class,
                    new net.minecraft.world.phys.AABB(com.darkcraft.morvain.Homenaje.GUIA_X - 3, com.darkcraft.morvain.Homenaje.SANT_Y - 1,
                            com.darkcraft.morvain.Homenaje.GUIA_Z - 3, com.darkcraft.morvain.Homenaje.GUIA_X + 4, com.darkcraft.morvain.Homenaje.SANT_Y + 4,
                            com.darkcraft.morvain.Homenaje.GUIA_Z + 4)).size());
            int reina = com.darkcraft.morvain.MorvainEnemigos.isolde();
            com.darkcraft.enemigos.Puestos.marcar(server, sitio, reina, true);
            com.darkcraft.morvain.CasaSombra.revisar(m);
            int reja = 0;
            for (BlockPos q : com.darkcraft.morvain.Homenaje.rejaAzotea()) if (m.getBlockState(q).is(net.minecraft.world.level.block.Blocks.IRON_BARS)) reja++;
            c.append("; con la reina caida, la reja de la azotea ").append(reja).append(" barrotes (tiene que ser 0)");
            com.darkcraft.enemigos.Puestos.marcar(server, sitio, reina, false);
            com.darkcraft.morvain.CasaSombra.revisar(m);
            reja = 0;
            for (BlockPos q : com.darkcraft.morvain.Homenaje.rejaAzotea()) if (m.getBlockState(q).is(net.minecraft.world.level.block.Blocks.IRON_BARS)) reja++;
            c.append(", y si vuelve ").append(reja).append(" de ").append(com.darkcraft.morvain.Homenaje.rejaAzotea().size());
            c.append("; al acabar ").append(com.darkcraft.morvain.CasaSombra.resumen(m));
            log.println(c);
            // ---- los de la casa: en su sitio, en su suelo y ninguno metido en un bloque
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            for (int k = 0; k < 8; k++)
                for (var e : fuera) if (e.isAlive()) {
                    e.tickCount++;
                    e.tick();
                }
            java.util.Map<String, Integer> tipos = new java.util.TreeMap<>();
            int enSuelo = 0, chocan = 0;
            StringBuilder mal = new StringBuilder();
            for (var e : fuera) {
                tipos.merge(e.tipo(), 1, Integer::sum);
                if (!m.noCollision(e)) {
                    chocan++;
                    if (mal.length() < 300) mal.append(' ').append(e.tipo()).append(" metido en un bloque en ").append(e.blockPosition().toShortString()).append(';');
                }
                var abajo = m.getBlockState(BlockPos.containing(e.getX(), e.getY() - 0.2, e.getZ()));
                if (!abajo.getCollisionShape(m, BlockPos.ZERO).isEmpty() || e instanceof com.darkcraft.enemigos.Isolde) enSuelo++;
                else if (mal.length() < 400) mal.append(' ').append(e.tipo()).append(" en el aire en ").append(e.blockPosition().toShortString()).append(';');
            }
            log.println("Morvain, los de la torre del homenaje: fuera " + fuera.size() + " de " + com.darkcraft.morvain.MorvainEnemigos.homenaje().size() + " " + tipos
                    + ", en su suelo " + enSuelo + "; metidos en un bloque " + chocan + " (tiene que ser 0)" + mal);
        } catch (Throwable t) {
            log.println("Morvain, la torre del homenaje: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] homenaje", t);
        } finally {
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    private static void pasoE(com.darkcraft.enemigos.Enemigo e, net.minecraft.world.entity.LivingEntity presa, double px, double py, double pz) {
        e.setOldPosAndRot();
        e.tickCount++;
        e.tick();
        if (presa != null) {
            presa.moveTo(px, py, pz, presa.getYRot(), 0f);
            presa.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            presa.setOnGround(true);
            presa.invulnerableTime = 0;
        }
    }

    /** Una victima: un aldeano quieto con mucha vida en (x, y, z) mirando a {@code giro}. */
    private static net.minecraft.world.entity.npc.Villager victima(ServerLevel m, double x, double y, double z, float giro, java.util.List<net.minecraft.world.entity.Entity> quitar) {
        var v = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
        if (v == null) return null;
        v.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(4000.0);
        v.setHealth(4000f);
        v.setNoAi(true);
        v.moveTo(x, y, z, giro, 0f);
        v.setYHeadRot(giro);
        v.yBodyRot = giro;
        m.addFreshEntity(v);
        quitar.add(v);
        return v;
    }

    /**
     * Parte 7ab: los centinelas. Todos fuera en su sitio; uno del espadon que espera de pie quieto: lo que ve (de lejos no, de cerca
     * si, agachado no), despierta, el tajo, el golpe, el humo (cruza hasta su presa y le pega); uno de la alabarda: la estocada, el
     * barrido por la espalda y el hachazo; y al morir se desploma y se va.
     */
    private static void centinelas(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.HOMENAJE;
        var H = com.darkcraft.morvain.Homenaje.get();
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder("Morvain, los centinelas:");
        try {
            for (long k : H.chunks()) m.getChunk((int) (k >> 32), (int) k);
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            com.darkcraft.enemigos.Centinela esp = null, ala = null;
            int n = 0;
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio)) {
                if (e instanceof com.darkcraft.enemigos.Centinela q) {
                    n++;
                    if (esp == null && q.variante() == com.darkcraft.enemigos.Centinela.ESPADON && q.postura() == com.darkcraft.enemigos.Enemigo.REZA
                            && com.darkcraft.morvain.Homenaje.planta(q.getY()) == com.darkcraft.morvain.Homenaje.BAJA && q.getZ() < 30) {
                        esp = q;
                        continue;
                    }
                    if (ala == null && q.variante() == com.darkcraft.enemigos.Centinela.ALABARDA && q.postura() == com.darkcraft.enemigos.Enemigo.REZA
                            && com.darkcraft.morvain.Homenaje.planta(q.getY()) == com.darkcraft.morvain.Homenaje.ARMERIA) {
                        ala = q;
                        continue;
                    }
                }
                e.discard();
            }
            if (esp == null || ala == null) {
                log.println(sb.append(" NO SALEN (centinelas ").append(n).append(')'));
                return;
            }
            sb.append(String.format(java.util.Locale.ROOT, " %d en la casa; el del espadon en %s (vida %.0f, golpe %.1f, armadura %d), el de la alabarda en %s", n,
                    esp.blockPosition().toShortString(), esp.getMaxHealth(), esp.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE),
                    esp.getArmorValue(), ala.blockPosition().toShortString()));
            // ---- lo que ve quieto (de pie, como una armadura mas)
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("5b1da0aa-cade-4a50-8a11-0c0ffee07ab1"), "[DarkCraftCI19]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            String ve = "";
            try {
                java.lang.reflect.Method mt = com.darkcraft.enemigos.Enemigo.class.getDeclaredMethod("percibe", net.minecraft.world.entity.LivingEntity.class);
                mt.setAccessible(true);
                net.minecraft.world.phys.Vec3 f = net.minecraft.world.phys.Vec3.directionFromRotation(0, esp.getYRot());
                double[][] pruebas = {{10, 0}, {5, 0}, {5, 1}};
                for (double[] pr : pruebas) {
                    fp.moveTo(esp.getX() + f.x * pr[0], esp.getY(), esp.getZ() + f.z * pr[0], esp.getYRot() + 180f, 0f);
                    fp.setPose(pr[1] > 0 ? net.minecraft.world.entity.Pose.CROUCHING : net.minecraft.world.entity.Pose.STANDING);
                    ve += String.format(java.util.Locale.ROOT, " %s a %.0f: %s;", pr[1] > 0 ? "agachado" : "de pie", pr[0], mt.invoke(esp, fp));
                }
                fp.setPose(net.minecraft.world.entity.Pose.STANDING);
            } catch (Throwable t) {
                ve = " ERROR " + t;
            }
            sb.append("; quieto ve:").append(ve).append(" (tiene que ser false, true, false)");
            // ---- despierta y sus golpes (la presa delante, a dos bloques y medio)
            net.minecraft.world.phys.Vec3 f = net.minecraft.world.phys.Vec3.directionFromRotation(0, esp.getYRot());
            double vx = esp.getX() + f.x * 2.5, vy = esp.getY(), vz = esp.getZ() + f.z * 2.5;
            var v = victima(m, vx, vy, vz, esp.getYRot() + 180f, quitar);
            if (v == null) {
                log.println(sb.append(" (sin presa)"));
                return;
            }
            int desp0 = com.darkcraft.enemigos.Centinela.DESPIERTAN;
            esp.setTarget(v);
            int despierta = -1;
            for (int k = 0; k < 80; k++) {
                pasoE(esp, v, vx, vy, vz);
                if (despierta < 0 && esp.postura() == com.darkcraft.enemigos.Enemigo.DE_PIE) despierta = k;
            }
            sb.append("; despierta (+").append(com.darkcraft.enemigos.Centinela.DESPIERTAN - desp0).append(") y queda de pie en el tick ").append(despierta);
            int[] golpes = {com.darkcraft.enemigos.CentinelaAnims.TAJO, com.darkcraft.enemigos.CentinelaAnims.GOLPE};
            String[] nombres = {"tajo", "golpe"};
            for (int i = 0; i < golpes.length; i++) {
                esp.moveTo(vx - f.x * 2.5, vy, vz - f.z * 2.5, esp.getYRot(), 0f);
                v.setHealth(v.getMaxHealth());
                esp.probar(golpes[i]);
                for (int k = 0; k < com.darkcraft.enemigos.CentinelaAnims.TICKS[golpes[i]] + 2 && esp.accion() == golpes[i]; k++) pasoE(esp, v, vx, vy, vz);
                sb.append(String.format(java.util.Locale.ROOT, "; %s: -%.1f", nombres[i], v.getMaxHealth() - v.getHealth()));
            }
            // el humo: la presa a ocho bloques, cruza hasta ella y le pega
            double hx = esp.getX() + f.x * 8, hz = esp.getZ() + f.z * 8;
            esp.moveTo(vx - f.x * 2.5, vy, vz - f.z * 2.5, esp.getYRot(), 0f);
            v.setHealth(v.getMaxHealth());
            int humos0 = com.darkcraft.enemigos.Centinela.HUMOS, llegan0 = com.darkcraft.enemigos.Centinela.HUMOS_LLEGAN;
            double antes = esp.distanceToSqr(hx, vy, hz);
            esp.probar(com.darkcraft.enemigos.CentinelaAnims.HUMO_ESPADON);
            for (int k = 0; k < com.darkcraft.enemigos.CentinelaAnims.TICKS[com.darkcraft.enemigos.CentinelaAnims.HUMO_ESPADON] + 2
                    && esp.accion() == com.darkcraft.enemigos.CentinelaAnims.HUMO_ESPADON; k++)
                pasoE(esp, v, hx, vy, hz);
            sb.append(String.format(java.util.Locale.ROOT, "; el humo: de %.1f a %.1f de su presa (humos +%d, llegan +%d), le quita %.1f", Math.sqrt(antes),
                    Math.sqrt(esp.distanceToSqr(hx, vy, hz)), com.darkcraft.enemigos.Centinela.HUMOS - humos0, com.darkcraft.enemigos.Centinela.HUMOS_LLEGAN - llegan0,
                    v.getMaxHealth() - v.getHealth()));
            // ---- la alabarda: la estocada (a cuatro y medio), el barrido (la presa a su espalda) y el hachazo
            net.minecraft.world.phys.Vec3 g = net.minecraft.world.phys.Vec3.directionFromRotation(0, ala.getYRot());
            double ax = ala.getX(), ay = ala.getY(), az = ala.getZ();
            ala.postura(com.darkcraft.enemigos.Enemigo.DE_PIE);
            ala.setTarget(v);
            double[] dist = {4.5, -2.5, 2.5};
            int[] acc = {com.darkcraft.enemigos.CentinelaAnims.ESTOCADA, com.darkcraft.enemigos.CentinelaAnims.BARRIDO, com.darkcraft.enemigos.CentinelaAnims.HACHAZO};
            String[] nom = {"estocada a 4,5", "barrido por la espalda", "hachazo"};
            for (int i = 0; i < acc.length; i++) {
                float giro = (float) (Math.toDegrees(Math.atan2(-g.x, g.z)));
                ala.moveTo(ax, ay, az, giro, 0f);
                ala.setYRot(giro);
                ala.yBodyRot = ala.yHeadRot = giro;
                double px = ax + g.x * dist[i], pz = az + g.z * dist[i];
                v.setHealth(v.getMaxHealth());
                v.moveTo(px, ay, pz, giro + 180f, 0f);
                ala.empezar(acc[i]);
                for (int k = 0; k < com.darkcraft.enemigos.CentinelaAnims.TICKS[acc[i]] + 2 && ala.accion() == acc[i]; k++) {
                    ala.moveTo(ax, ay, az, ala.getYRot(), 0f);
                    pasoE(ala, v, px, ay, pz);
                }
                sb.append(String.format(java.util.Locale.ROOT, "; %s: -%.1f", nom[i], v.getMaxHealth() - v.getHealth()));
            }
            // ---- muere: se desploma y se va
            int des0 = com.darkcraft.enemigos.Centinela.DESPLOMAN;
            esp.hurt(m.damageSources().genericKill(), 10000f);
            int k;
            for (k = 0; k < 120 && !esp.isRemoved(); k++) {
                esp.tickCount++;
                esp.tick();
            }
            sb.append(String.format(java.util.Locale.ROOT, "; muere: se desploma (+%d) y se va en %d ticks", com.darkcraft.enemigos.Centinela.DESPLOMAN - des0, k));
            log.println(sb);
        } catch (Throwable t) {
            log.println(sb + " ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] centinelas", t);
        } finally {
            for (var e : quitar) e.discard();
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    /**
     * Parte 7ab: la reina Isolde. Sentada en su trono: ve a quien entra en el salon, no a quien esta en la capilla de abajo; la pelea
     * (su presa delante del estrado): se levanta mientras dice su frase 1 y grita al acabarla; el salon se sella; las garras, el velo
     * (alrededor), el abrazo (cruza, coge y aprieta dos veces), la nana (quien la mira se duerme; quien no, no), desvanecerse (no se
     * le puede pegar, aparece detras y le echa las garras); a media vida le arde el velo (se le ve la cara) y el velo quema; las
     * lagrimas pillan a su presa; llama a su corte (dos espectros que se deshacen al acabar la pelea); sin presa la pelea se acaba y
     * el salon se abre; y al caer dice su ultima frase, se va al acabarla, su puesto queda caido y la reja de la azotea se abre.
     */
    private static void isolde(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.HOMENAJE;
        var H = com.darkcraft.morvain.Homenaje.get();
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder("Morvain, la reina Isolde:");
        try {
            for (long k : H.chunks()) m.getChunk((int) (k >> 32), (int) k);
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            com.darkcraft.enemigos.Isolde r = null;
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio)) {
                if (r == null && e instanceof com.darkcraft.enemigos.Isolde i) r = i;
                else e.discard();
            }
            if (r == null) {
                log.println(sb.append(" NO SALE"));
                return;
            }
            final com.darkcraft.enemigos.Isolde reina = r;
            sb.append(String.format(java.util.Locale.ROOT, " en %s (postura %d, vida %.0f, golpe %.1f, armadura %d, %.1f x %.1f, metida en un bloque %s)",
                    r.blockPosition().toShortString(), r.postura(), r.getMaxHealth(), r.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE),
                    r.getArmorValue(), r.getBbWidth(), r.getBbHeight(), !m.noCollision(r)));
            // ---- lo que ve sentada: a quien entra en el salon (a 21 bloques), no a quien esta en la capilla, debajo
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("5b1da0aa-cade-4a50-8a11-0c0ffee07ab2"), "[DarkCraftCI20]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            String ve = "";
            try {
                java.lang.reflect.Method mt = com.darkcraft.enemigos.Isolde.class.getDeclaredMethod("percibe", net.minecraft.world.entity.LivingEntity.class);
                mt.setAccessible(true);
                for (String n : new String[]{"sala_trono", "trono_entrada", "capilla"}) {
                    int[] q = H.sitio(n);
                    fp.moveTo(q[0] + 0.5, q[1], q[2] + 0.5, 0f, 0f);
                    ve += " " + n + ": " + mt.invoke(r, fp) + ";";
                }
            } catch (Throwable t) {
                ve = " ERROR " + t;
            }
            sb.append("; sentada ve:").append(ve).append(" (true, true, false)");
            // ---- la pelea: la presa delante del estrado
            int[] tr = H.sitio("sala_trono");
            double vx = 0.5, vy = tr[1], vz = 8.5;
            var v = victima(m, vx, vy, vz, 0f, quitar);
            if (v == null) {
                log.println(sb.append(" (sin presa)"));
                return;
            }
            r.setTarget(v);
            int levanta = -1, grita = -1, k;
            for (k = 0; k < 420 && grita < 0; k++) {
                pasoE(r, v, vx, vy, vz);
                if (levanta < 0 && r.accion() == com.darkcraft.enemigos.IsoldeAnims.LEVANTARSE) levanta = k;
                if (grita < 0 && r.accion() == com.darkcraft.enemigos.IsoldeAnims.GRITO) grita = k;
                if (k % 5 == 0) com.darkcraft.morvain.CasaSombra.tick(m);
            }
            com.darkcraft.morvain.CasaSombra.tick(m);
            sb.append(String.format(java.util.Locale.ROOT, "; en pelea %s: se levanta en el tick %d, grita en el %d (su frase 1 dura %d ticks); el salon %s",
                    r.enPelea(), levanta, grita, com.darkcraft.rey.Voces.ticks(com.darkcraft.enemigos.Isolde.VOZ_BASE + 1),
                    com.darkcraft.morvain.CasaSombra.sellado() ? "sellado" : "SIN SELLAR"));
            for (k = 0; k < 60 && r.accion() != 0; k++) pasoE(r, v, vx, vy, vz);
            // los golpes: el de cada accion a la presa delante (las garras), detras (el velo); en el suelo del salon (no en el estrado)
            final double ry = com.darkcraft.morvain.Homenaje.pisa(com.darkcraft.morvain.Homenaje.TRONO);
            java.util.function.BiFunction<Integer, Double, Float> golpe = (accion, d) -> {
                reina.moveTo(0.5, ry, 5.5, 0f, 0f);
                reina.setYRot(0f);
                reina.yBodyRot = reina.yHeadRot = 0f;
                double px = 0.5, pz = 5.5 + d;
                v.setHealth(v.getMaxHealth());
                v.moveTo(px, ry, pz, 180f, 0f);
                reina.empezar(accion);
                for (int j = 0; j < com.darkcraft.enemigos.IsoldeAnims.TICKS[accion] + 2 && reina.accion() == accion; j++) {
                    reina.moveTo(0.5, ry, 5.5, reina.getYRot(), 0f);
                    pasoE(reina, v, px, ry, pz);
                }
                return v.getMaxHealth() - v.getHealth();
            };
            sb.append(String.format(java.util.Locale.ROOT, "; las garras: -%.1f; el velo por la espalda: -%.1f", golpe.apply(com.darkcraft.enemigos.IsoldeAnims.GARRAS, 2.4),
                    golpe.apply(com.darkcraft.enemigos.IsoldeAnims.VELO, -2.8)));
            // el abrazo: la presa a ocho bloques; cruza, la coge y la aprieta (dos veces) y la suelta
            int ag0 = com.darkcraft.enemigos.Isolde.AGARRA, ap0 = com.darkcraft.enemigos.Isolde.APRIETOS;
            r.moveTo(0.5, ry, -2.5, 180f, 0f);
            v.setHealth(v.getMaxHealth());
            v.moveTo(0.5, ry, -10.5, 0f, 0f);
            r.setTarget(v);
            r.probar(com.darkcraft.enemigos.IsoldeAnims.ABRAZO);
            double cruza0 = r.getZ();
            boolean aprieta = false;
            for (k = 0; k < 90; k++) {
                pasoE(r, v, 0.5, ry, -10.5);
                if (r.accion() == com.darkcraft.enemigos.IsoldeAnims.APRIETA) aprieta = true;
                if (aprieta && r.accion() != com.darkcraft.enemigos.IsoldeAnims.APRIETA) break;
            }
            sb.append(String.format(java.util.Locale.ROOT, "; el abrazo: cruza %.1f, coge +%d, aprieta %s (+%d), la suelta %s, le quita %.1f", cruza0 - r.getZ(),
                    com.darkcraft.enemigos.Isolde.AGARRA - ag0, aprieta, com.darkcraft.enemigos.Isolde.APRIETOS - ap0, r.agarrado() == null, v.getMaxHealth() - v.getHealth()));
            // la nana: quien la mira de frente, si; de espaldas, no; y al dormirse, los ojos cerrados
            r.moveTo(0.5, ry, 5.5, 0f, 0f);
            fp.moveTo(0.5, ry, 12.5, 180f, 0f);
            fp.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, new net.minecraft.world.phys.Vec3(0.5, ry + r.getBbHeight() * 0.88, 5.5));
            boolean mira = r.laMira(fp);
            fp.setYRot(0f);
            fp.setXRot(0f);
            fp.setYHeadRot(0f);
            boolean deEspaldas = r.laMira(fp);
            r.dormir(fp);
            sb.append("; la nana: de frente la mira ").append(mira).append(", de espaldas ").append(deEspaldas).append(", dormido: ciego ")
                    .append(fp.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS)).append(", quieto ").append(fp.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN));
            fp.removeAllEffects();
            int nanas0 = com.darkcraft.enemigos.Isolde.NANAS;
            r.probar(com.darkcraft.enemigos.IsoldeAnims.NANA);
            for (k = 0; k < 80 && r.accion() == com.darkcraft.enemigos.IsoldeAnims.NANA; k++) pasoE(r, v, 0.5, ry, -10.5);
            sb.append(" (canta +").append(com.darkcraft.enemigos.Isolde.NANAS - nanas0).append(", acaba en ").append(k).append(')');
            // desvanecerse: la presa mira al norte (fuera de la alfombra); ella al sur, a seis; no se le puede pegar y aparece detras de la
            // presa (al sur, a 2,4) y le echa las garras
            v.moveTo(6.5, ry, 0.5, 180f, 0f);
            v.setYRot(180f);
            v.setYHeadRot(180f);
            v.yBodyRot = 180f;
            r.moveTo(6.5, ry, 7.5, 180f, 0f);
            int det0 = com.darkcraft.enemigos.Isolde.DETRAS_SALE;
            r.probar(com.darkcraft.enemigos.IsoldeAnims.DESVANECE);
            boolean leDan = true, garras = false;
            for (k = 0; k < 60; k++) {
                pasoE(r, v, 6.5, ry, 0.5);
                if (r.accion() == com.darkcraft.enemigos.IsoldeAnims.DESVANECE && r.accionTicks() == 9) {
                    float h = r.getHealth();
                    r.invulnerableTime = 0;
                    leDan = r.hurt(m.damageSources().generic(), 5f) || r.getHealth() < h;
                }
                if (r.accion() == com.darkcraft.enemigos.IsoldeAnims.GARRAS) {
                    garras = true;
                    break;
                }
            }
            sb.append(String.format(java.util.Locale.ROOT, "; desvanecerse: apagada le pegan %s, aparece detras +%d (a %.1f, %.1f de la presa: tiene que ser 0, 2,4), y le echa las garras %s",
                    leDan, com.darkcraft.enemigos.Isolde.DETRAS_SALE - det0, r.getX() - 6.5, r.getZ() - 0.5, garras));
            for (k = 0; k < 40 && r.accion() != 0; k++) pasoE(r, v, 6.5, ry, 0.5);
            // ---- a media vida: le arde el velo; el velo quema; las lagrimas; su corte
            int arde0 = com.darkcraft.enemigos.Isolde.VELO_ARDE;
            r.mediaVida();
            for (k = 0; k < 70 && r.accion() == com.darkcraft.enemigos.IsoldeAnims.VELO_ARDE; k++) pasoE(r, v, 0.5, ry, 0.5);
            sb.append(String.format(java.util.Locale.ROOT, "; a media vida (%.0f): le arde el velo +%d, se le ve la cara %s", r.getHealth(),
                    com.darkcraft.enemigos.Isolde.VELO_ARDE - arde0, r.sinVelo()));
            int q0 = com.darkcraft.enemigos.Isolde.VELOS_QUEMAN;
            v.clearFire();
            sb.append(String.format(java.util.Locale.ROOT, "; el velo que arde: -%.1f", golpe.apply(com.darkcraft.enemigos.IsoldeAnims.VELO, -2.8)));
            sb.append(", quema +").append(com.darkcraft.enemigos.Isolde.VELOS_QUEMAN - q0).append(" (presa ardiendo ").append(v.isOnFire()).append(')');
            v.clearFire();
            int lp0 = com.darkcraft.enemigos.Isolde.LAGRIMAS_PILLAN;
            r.moveTo(0.5, ry, 8.5, 180f, 0f);
            v.moveTo(0.5, ry, 0.5, 0f, 0f);
            v.setHealth(v.getMaxHealth());
            r.setTarget(v);
            r.probar(com.darkcraft.enemigos.IsoldeAnims.LAGRIMAS);
            for (k = 0; k < 60 && r.accion() == com.darkcraft.enemigos.IsoldeAnims.LAGRIMAS; k++) {
                r.moveTo(0.5, ry, 8.5, r.getYRot(), 0f);
                pasoE(r, v, 0.5, ry, 0.5);
            }
            sb.append(String.format(java.util.Locale.ROOT, "; las lagrimas: pillan +%d, -%.1f, pudre %s", com.darkcraft.enemigos.Isolde.LAGRIMAS_PILLAN - lp0,
                    v.getMaxHealth() - v.getHealth(), v.hasEffect(net.minecraft.world.effect.MobEffects.WITHER)));
            int co0 = com.darkcraft.enemigos.Isolde.CORTESANOS;
            r.probar(com.darkcraft.enemigos.IsoldeAnims.GRITO);
            for (k = 0; k < 50 && r.accion() == com.darkcraft.enemigos.IsoldeAnims.GRITO; k++) pasoE(r, v, 0.5, ry, 0.5);
            var corte = new java.util.ArrayList<>(r.corte());
            int enSala = 0;
            for (var e : corte) if (com.darkcraft.morvain.Homenaje.enSalaTrono(e.getX(), e.getY() + 0.5, e.getZ())) enSala++;
            sb.append("; su corte: +").append(com.darkcraft.enemigos.Isolde.CORTESANOS - co0).append(" espectros (en el salon ").append(enSala).append(", efimeros ")
                    .append(corte.stream().filter(com.darkcraft.enemigos.Enemigo::esEfimero).count()).append(')');
            // ---- sin presa: la pelea se acaba, la corte se deshace, vuelve el velo y el salon se abre
            r.setTarget(null);
            v.discard();
            for (k = 0; k < 140 && r.enPelea(); k++) {
                pasoE(r, null, 0, 0, 0);
                if (k % 5 == 0) com.darkcraft.morvain.CasaSombra.tick(m);
            }
            com.darkcraft.morvain.CasaSombra.tick(m);
            int quedan = 0;
            for (var e : corte) if (!e.isRemoved()) quedan++;
            sb.append(String.format(java.util.Locale.ROOT, "; sin presa: acaba la pelea en %d ticks (en pelea %s), quedan %d espectros, con el velo %s, vida %.0f/%.0f, el salon %s",
                    k, r.enPelea(), quedan, !r.sinVelo(), r.getHealth(), r.getMaxHealth(), com.darkcraft.morvain.CasaSombra.sellado() ? "SELLADO" : "abierto"));
            log.println(sb);
            // ---- cae: su ultima frase, se va al acabarla, el puesto queda caido y la reja se abre
            StringBuilder c = new StringBuilder("Morvain, la reina Isolde cae:");
            var v2 = victima(m, 0.5, ry, 8.5, 0f, quitar);
            r.setTarget(v2);
            int cae0 = com.darkcraft.enemigos.Isolde.CAE;
            r.vencer();
            int fin = r.fin();
            for (k = 0; k < fin + 40 && !r.isRemoved(); k++) {
                r.tickCount++;
                r.tick();
            }
            int drops = 0;
            for (var it : m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, r.getBoundingBox().inflate(6)))
                if (it.getItem().is(DarkCraftMod.PIEDRA_FORJA_3.get())) {
                    drops += it.getItem().getCount();
                    quitar.add(it);
                }
            com.darkcraft.morvain.CasaSombra.revisar(m);
            int reja = 0;
            for (BlockPos q : com.darkcraft.morvain.Homenaje.rejaAzotea()) if (m.getBlockState(q).is(net.minecraft.world.level.block.Blocks.IRON_BARS)) reja++;
            c.append(String.format(java.util.Locale.ROOT, " cae +%d, se va en %d ticks (su ultima frase %d + 50), su puesto caido %s, deja %d piedras de forja III; la reja de la azotea %d barrotes (tiene que ser 0); %s",
                    com.darkcraft.enemigos.Isolde.CAE - cae0, k, com.darkcraft.rey.Voces.ticks(com.darkcraft.enemigos.Isolde.VOZ_BASE + 6),
                    com.darkcraft.enemigos.Puestos.muerto(server, sitio, com.darkcraft.morvain.MorvainEnemigos.isolde()), drops, reja, com.darkcraft.morvain.CasaSombra.resumen(m)));
            log.println(c);
        } catch (Throwable t) {
            log.println(sb + " ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] isolde", t);
        } finally {
            for (var e : quitar) e.discard();
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            com.darkcraft.morvain.CasaSombra.revisar(m);
            if (com.darkcraft.morvain.CasaSombra.sellado()) com.darkcraft.morvain.CasaSombra.sellar(m, false);
        }
    }

    private static void pasoGargola(com.darkcraft.enemigos.Gargola g, net.minecraft.world.entity.LivingEntity presa, double px, double py, double pz) {
        pasoGargolaSin(g);
        presa.moveTo(px, py, pz, 180f, 0f);
        presa.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        presa.setOnGround(true);
        presa.invulnerableTime = 0;
    }

    private static void pasoGargolaSin(com.darkcraft.enemigos.Gargola g) {
        g.setOldPosAndRot();
        g.tickCount++;
        g.tick();
    }

    /** Hace andar lo que deja Vorthak (las bolas de fuego y las llamas del suelo) en la caja. */
    private static void tickDragon(ServerLevel m, net.minecraft.world.phys.AABB caja, java.util.List<net.minecraft.world.entity.Entity> quitar) {
        for (var e : m.getEntitiesOfClass(net.minecraft.world.entity.Entity.class, caja,
                q -> q instanceof com.darkcraft.enemigos.ProyectilDragon || q instanceof com.darkcraft.enemigos.Cantaro.Llamas)) {
            if (e.isRemoved()) continue;
            if (!quitar.contains(e)) quitar.add(e);
            e.tickCount++;
            e.tick();
        }
    }

    /** Un tick de Vorthak (y de lo que deja), con su presa quieta en su sitio y con la vida entera. */
    private static void pasoDragon(com.darkcraft.enemigos.Dragon d, ServerLevel m, net.minecraft.world.phys.AABB caja, java.util.List<net.minecraft.world.entity.Entity> quitar,
                                   net.minecraft.world.entity.LivingEntity presa, double px, double py, double pz) {
        d.tickCount++;
        d.tick();
        tickDragon(m, caja, quitar);
        if (presa != null) {
            presa.moveTo(px, py, pz, 180f, 0f);
            presa.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            presa.setOnGround(true);
            presa.invulnerableTime = 0;
        }
    }

    /**
     * Parte 7z: Vorthak, el Dragon de Ceniza. Duerme sobre su oro (de rodillas, sin fisica); no le despierta quien esta lejos ni quien
     * se acerca agachado, si quien llega de pie; al despertar habla, se levanta y ruge al acabar la frase; cada accion de tierra acaba;
     * sus golpes de tierra a una presa (el mordisco delante, el zarpazo, el coletazo detras, el aletazo que la echa lejos, el pisoton
     * y su onda —quien salta se libra—, el aliento que quema y que un muro en medio para). En el aire: despega (deja el suelo), el
     * vuelo rasante (quema en linea y el suelo queda ardiendo), las bolas de fuego (revientan), el picado (cae donde marco: la onda),
     * a media vida la lluvia de ascuas, y baja a tierra; derribado en el aire cae y queda aturdido. Al caer (volando, cae del cielo)
     * se tumba, se apaga y no vuelve. Al acabar, otra vez dormido en su oro.
     */
    private static void dragon(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.NIDO;
        var N = com.darkcraft.morvain.Nido.get();
        final int CX = com.darkcraft.morvain.Nido.CX, CZ = com.darkcraft.morvain.Nido.CZ, PISO = com.darkcraft.morvain.Nido.PISO;
        final var caja = new net.minecraft.world.phys.AABB(CX - 80, PISO - 20, CZ - 80, CX + 80, PISO + 60, CZ + 80);
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        java.util.List<Object[]> bloques = new java.util.ArrayList<>();
        com.darkcraft.enemigos.Dragon d = null;
        try {
            for (long k : N.chunks()) m.getChunk((int) (k >> 32), (int) k);
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio)) {
                if (e instanceof com.darkcraft.enemigos.Dragon g) d = g;
                // (los calcinados, fuera: aqui solo cuenta el)
                else e.discard();
            }
            if (d == null) {
                log.println("Morvain, Vorthak: NO SALE");
                return;
            }
            final double cx = CX + 0.5, cz = CZ + 0.5, cy = PISO;
            int[] q = N.sitio("dragon");
            var abajo = m.getBlockState(BlockPos.containing(d.getX(), d.getY() - 0.3, d.getZ()));
            StringBuilder sb = new StringBuilder(String.format(java.util.Locale.ROOT, "Morvain, Vorthak: en %s (sobre el oro: %s), postura %d (1 dormido),"
                            + " giro %d, vida %.0f, golpe %.1f, armadura %d, caja %.1f x %.1f, musica %d, sin fisica %s",
                    d.blockPosition().toShortString(), abajo.getBlock().getDescriptionId(), d.postura(), Math.round(d.getYRot()), d.getMaxHealth(),
                    d.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE), d.getArmorValue(), d.getBbWidth(), d.getBbHeight(),
                    d.musica(), d.noPhysics));
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("9f102152-7d84-4fa0-81c2-6d7e8f90a1ef"), "[DarkCraftCI16]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            // ---- dormido: lejos no, agachado cerca no, de pie a veinte si
            String ve = "";
            try {
                java.lang.reflect.Method mt = com.darkcraft.enemigos.Dragon.class.getDeclaredMethod("ve", net.minecraft.world.entity.player.Player.class);
                mt.setAccessible(true);
                double yaw = Math.toRadians(d.getYRot());
                double fx = -Math.sin(yaw), fz = Math.cos(yaw);
                double[][] pruebas = {{30, 0}, {10, 1}, {20, 0}};
                for (double[] pr : pruebas) {
                    double x = d.getX() + fx * pr[0], z = d.getZ() + fz * pr[0];
                    fp.moveTo(x, m.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, (int) Math.floor(x), (int) Math.floor(z)), z, 0f, 0f);
                    fp.setPose(pr[1] > 0 ? net.minecraft.world.entity.Pose.CROUCHING : net.minecraft.world.entity.Pose.STANDING);
                    ve += String.format(java.util.Locale.ROOT, " %s a %.0f: %s;", pr[1] > 0 ? "agachado" : "de pie", pr[0], mt.invoke(d, fp));
                }
                fp.setPose(net.minecraft.world.entity.Pose.STANDING);
            } catch (Throwable t) {
                ve = " ERROR " + t;
            }
            sb.append("; dormido le ve:").append(ve);
            // ---- se despierta: habla, se levanta y ruge al acabar la frase
            d.setTarget(fp);
            final double fx0 = fp.getX(), fy0 = fp.getY(), fz0 = fp.getZ();
            int despierta = -1, dePie = -1, ruge = -1;
            for (int k = 0; k < 420; k++) {
                pasoDragon(d, m, caja, quitar, fp, fx0, fy0, fz0);
                fp.setHealth(fp.getMaxHealth());
                int a = d.accion();
                if (despierta < 0 && a == com.darkcraft.enemigos.DragonAnims.DESPIERTA) despierta = k;
                if (dePie < 0 && d.postura() == com.darkcraft.enemigos.Enemigo.DE_PIE) dePie = k;
                if (ruge < 0 && a == com.darkcraft.enemigos.DragonAnims.RUGIDO) {
                    ruge = k;
                    break;
                }
            }
            sb.append("; despierta: pelea ").append(d.enPelea()).append(", se levanta en el tick ").append(despierta).append(" (de pie en el ").append(dePie)
                    .append("), ruge en el ").append(ruge).append(" (la frase 1 dura ").append(com.darkcraft.rey.Voces.ticks(com.darkcraft.enemigos.Dragon.VOZ_BASE + 1))
                    .append(')');
            // ---- cada accion de tierra acaba (sin presa y quieto, en medio de la repisa)
            d.quieta = true;
            d.setTarget(null);
            for (int k = 0; k < 120 && d.accion() != 0; k++) pasoDragon(d, m, caja, quitar, null, 0, 0, 0);
            StringBuilder nn = new StringBuilder();
            int vistas = 0;
            int[] tierra = {com.darkcraft.enemigos.DragonAnims.RUGIDO, com.darkcraft.enemigos.DragonAnims.MORDISCO, com.darkcraft.enemigos.DragonAnims.ZARPAZO,
                    com.darkcraft.enemigos.DragonAnims.COLETAZO, com.darkcraft.enemigos.DragonAnims.ALETAZO, com.darkcraft.enemigos.DragonAnims.PISOTON,
                    com.darkcraft.enemigos.DragonAnims.ALIENTO, com.darkcraft.enemigos.DragonAnims.ATURDIDO};
            for (int a : tierra) {
                d.moveTo(cx, cy, cz, 0f, 0f);
                d.empezar(a);
                vistas++;
                for (int k = 0; k < 200 && d.accion() == a; k++) pasoDragon(d, m, caja, quitar, null, 0, 0, 0);
                if (d.accion() == a) nn.append(' ').append(com.darkcraft.enemigos.DragonAnims.NOMBRES[a]);
            }
            sb.append("; de ").append(vistas).append(" acciones de tierra no acaban:").append(nn.length() == 0 ? " ninguna" : nn);
            log.println(sb);
            // ---- sus golpes de tierra a una presa (un aldeano quieto y duro), con el mirando al sur desde el centro de la repisa
            StringBuilder sg = new StringBuilder("Morvain, los golpes de Vorthak en tierra:");
            var victima = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
            if (victima != null) {
                victima.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(2000.0);
                victima.setHealth(2000f);
                victima.setNoAi(true);
                m.addFreshEntity(victima);
                quitar.add(victima);
                String[] golpes = {"mordisco", "zarpazo", "coletazo", "aletazo", "pisoton", "pisoton saltando", "aliento", "aliento tras un muro"};
                int[] acc = {com.darkcraft.enemigos.DragonAnims.MORDISCO, com.darkcraft.enemigos.DragonAnims.ZARPAZO, com.darkcraft.enemigos.DragonAnims.COLETAZO,
                        com.darkcraft.enemigos.DragonAnims.ALETAZO, com.darkcraft.enemigos.DragonAnims.PISOTON, com.darkcraft.enemigos.DragonAnims.PISOTON,
                        com.darkcraft.enemigos.DragonAnims.ALIENTO, com.darkcraft.enemigos.DragonAnims.ALIENTO};
                // (delante = al sur: +z; detras: -z)
                double[] dz = {7.0, 4.0, -8.0, 6.0, 4.5, 4.5, 13.0, 13.0};
                double[] dx = {0, 1.0, 0, 0, 0, 0, 0, 0};
                for (int i = 0; i < golpes.length; i++) {
                    boolean salta = golpes[i].endsWith("saltando"), muro = golpes[i].endsWith("muro");
                    victima.setHealth(victima.getMaxHealth());
                    victima.invulnerableTime = 0;
                    victima.hurtTime = 0;
                    victima.removeAllEffects();
                    victima.clearFire();
                    d.moveTo(cx, cy, cz, 0f, 0f);
                    d.setYRot(0f);
                    d.yBodyRot = d.yHeadRot = 0f;
                    double sueloV = cy;
                    double vx = cx + dx[i], vy = sueloV + (salta ? 1.4 : 0), vz = cz + dz[i];
                    if (muro) {
                        // un muro de piedra de por medio (entre su boca, a ocho bloques y medio, y la presa: siete de ancho y cinco de alto)
                        for (int wx = -3; wx <= 3; wx++)
                            for (int wy = 0; wy < 5; wy++) {
                                BlockPos b = BlockPos.containing(cx + wx, cy + wy, cz + 10.5);
                                bloques.add(new Object[]{b, m.getBlockState(b)});
                                m.setBlock(b, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 2);
                            }
                    }
                    victima.moveTo(vx, vy, vz, 180f, 0f);
                    victima.setOnGround(!salta);
                    int mo = d.mordiscos(), za = d.zarpazos(), co = d.golpesCola(), al = d.tirados(), pi = d.ondas(), qu = d.quemados(), ta = d.tapados();
                    d.setTarget(victima);
                    d.empezar(acc[i]);
                    float antes = victima.getHealth();
                    double lejos = 0;
                    for (int k = 0; k < com.darkcraft.enemigos.DragonAnims.TICKS[acc[i]] + 4 && d.accion() == acc[i]; k++) {
                        d.tickCount++;
                        d.tick();
                        tickDragon(m, caja, quitar);
                        if (acc[i] == com.darkcraft.enemigos.DragonAnims.ALETAZO) {
                            // (el aletazo la echa: se mide cuanto)
                            moverPresa(victima);
                            lejos = Math.max(lejos, Math.hypot(victima.getX() - vx, victima.getZ() - vz));
                        } else victima.moveTo(vx, vy, vz, 180f, 0f);
                        victima.setOnGround(!salta);
                        victima.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                        victima.invulnerableTime = 0;
                    }
                    sg.append(' ').append(golpes[i]).append(String.format(java.util.Locale.ROOT, " a %.1f: -%.1f", Math.hypot(dx[i], dz[i]), antes - victima.getHealth()));
                    switch (i) {
                        case 0 -> sg.append(", muerde +").append(d.mordiscos() - mo);
                        case 1 -> sg.append(", zarpazos +").append(d.zarpazos() - za);
                        case 2 -> sg.append(", la cola da +").append(d.golpesCola() - co);
                        case 3 -> sg.append(String.format(java.util.Locale.ROOT, ", tira +%d (la echa %.1f)", d.tirados() - al, lejos));
                        case 4, 5 -> sg.append(", la onda da +").append(d.ondas() - pi);
                        case 6 -> sg.append(", quema +").append(d.quemados() - qu).append(" (arde ").append(victima.getRemainingFireTicks() > 0).append(')');
                        case 7 -> sg.append(", quema +").append(d.quemados() - qu).append(", tapado +").append(d.tapados() - ta);
                        default -> {
                        }
                    }
                    sg.append(';');
                    victima.clearFire();
                    for (int k = bloques.size() - 1; k >= 0; k--) m.setBlock((BlockPos) bloques.get(k)[0], (net.minecraft.world.level.block.state.BlockState) bloques.get(k)[1], 2);
                    bloques.clear();
                    for (int k = 0; k < 40 && d.accion() != 0; k++) pasoDragon(d, m, caja, quitar, null, 0, 0, 0);
                }
            }
            log.println(sg);
            // ---- en el aire: despega, el rasante, las bolas, el picado, las ascuas (a media vida) y derribado
            StringBuilder sv = new StringBuilder("Morvain, Vorthak en el aire:");
            if (victima != null) {
                final double vx = cx + 3, vy = cy, vz = cz + 2;
                victima.setHealth(victima.getMaxHealth());
                victima.moveTo(vx, vy, vz, 180f, 0f);
                d.moveTo(cx - 8, cy, cz - 6, 0f, 0f);
                d.setTarget(victima);
                double y0 = d.getY();
                d.despegar();
                double alto = 0;
                for (int k = 0; k < 120; k++) {
                    pasoDragon(d, m, caja, quitar, victima, vx, vy, vz);
                    alto = Math.max(alto, d.getY() - y0);
                }
                sv.append(String.format(java.util.Locale.ROOT, " despega %s (despegues %d, sube %.1f, maniobra %s)", d.vuela(), d.despegues(), alto, d.maniobraActual()));
                String[] mans = {"rasante", "bolas", "picado"};
                for (String man : mans) {
                    if (!d.vuela()) {
                        d.despegar();
                        for (int k = 0; k < 90; k++) pasoDragon(d, m, caja, quitar, victima, vx, vy, vz);
                    }
                    victima.setHealth(victima.getMaxHealth());
                    victima.clearFire();
                    int ra = d.rasantes(), qr = d.quemadosRasante(), fu = d.fuegos(), bo = d.bolas(), pc = d.picados(), gp = d.golpesPicado(), at = d.aterrizajes();
                    int rev = com.darkcraft.enemigos.ProyectilDragon.REVIENTAN, dan = com.darkcraft.enemigos.ProyectilDragon.DAN + com.darkcraft.enemigos.ProyectilDragon.QUEMAN;
                    float antes = victima.getHealth();
                    boolean ok = d.maniobra(man);
                    int hecho = -1;
                    double bajo = 999;
                    for (int k = 0; k < 600; k++) {
                        pasoDragon(d, m, caja, quitar, victima, vx, vy, vz);
                        bajo = Math.min(bajo, d.getY() - cy);
                        boolean fin = switch (man) {
                            case "rasante" -> d.rasantes() > ra && d.accion() == 0;
                            case "bolas" -> d.bolas() >= bo + 3 && com.darkcraft.enemigos.ProyectilDragon.REVIENTAN >= rev + 3;
                            default -> d.aterrizajes() > at && d.accion() == 0;
                        };
                        if (fin) {
                            hecho = k;
                            break;
                        }
                    }
                    // (las llamas y las bolas que quedan, un rato)
                    for (int k = 0; k < 30; k++) tickDragon(m, caja, quitar);
                    sv.append(String.format(java.util.Locale.ROOT, "; %s (empieza %s, hecho en el tick %d, lo mas bajo a %.1f del suelo): -%.1f", man, ok, hecho, bajo,
                            antes - victima.getHealth()));
                    switch (man) {
                        case "rasante" -> sv.append(", rasantes +").append(d.rasantes() - ra).append(", quema +").append(d.quemadosRasante() - qr).append(", fuegos en el suelo +")
                                .append(d.fuegos() - fu);
                        case "bolas" -> sv.append(", bolas +").append(d.bolas() - bo).append(" (revientan +").append(com.darkcraft.enemigos.ProyectilDragon.REVIENTAN - rev)
                                .append(", dan +").append(com.darkcraft.enemigos.ProyectilDragon.DAN + com.darkcraft.enemigos.ProyectilDragon.QUEMAN - dan).append(')');
                        default -> sv.append(", picados +").append(d.picados() - pc).append(", la onda da +").append(d.golpesPicado() - gp).append(", en tierra ")
                                .append(!d.vuela());
                    }
                }
                // a media vida: ruge, y al volar, la lluvia de ascuas; luego baja a tierra
                for (int k = 0; k < 60 && d.accion() != 0; k++) pasoDragon(d, m, caja, quitar, victima, vx, vy, vz);
                d.mediaVida();
                int rg = d.rugidos(), ll = d.lluvias(), as = d.ascuasCaidas(), qa = d.quemadosAscuas(), at = d.aterrizajes();
                for (int k = 0; k < 80; k++) pasoDragon(d, m, caja, quitar, victima, vx, vy, vz);
                boolean segunda = d.segunda();
                d.maniobra("ascuas");
                int hecho = -1;
                for (int k = 0; k < 900; k++) {
                    pasoDragon(d, m, caja, quitar, victima, vx, vy, vz);
                    if (d.lluvias() > ll && d.aterrizajes() > at && d.accion() == 0) {
                        hecho = k;
                        break;
                    }
                }
                for (int k = 0; k < 30; k++) tickDragon(m, caja, quitar);
                sv.append("; media vida: segunda ").append(segunda).append(", ruge +").append(d.rugidos() - rg).append("; ascuas (hecho en el tick ").append(hecho)
                        .append("): lluvias +").append(d.lluvias() - ll).append(", caen +").append(d.ascuasCaidas() - as).append(", queman +")
                        .append(d.quemadosAscuas() - qa).append(", baja a tierra ").append(!d.vuela());
                // derribado en el aire: cae y queda aturdido
                if (!d.vuela()) {
                    d.despegar();
                    for (int k = 0; k < 90; k++) pasoDragon(d, m, caja, quitar, victima, vx, vy, vz);
                }
                int ca = d.caidas(), au = d.aturdidos();
                boolean volaba = d.vuela();
                d.derribar();
                int cae = -1;
                for (int k = 0; k < 200; k++) {
                    pasoDragon(d, m, caja, quitar, victima, vx, vy, vz);
                    if (cae < 0 && !d.vuela()) cae = k;
                    if (cae >= 0 && d.accion() == com.darkcraft.enemigos.DragonAnims.ATURDIDO) break;
                }
                sv.append("; derribado (volaba ").append(volaba).append("): cae +").append(d.caidas() - ca).append(" (al suelo en el tick ").append(cae)
                        .append("), aturdido +").append(d.aturdidos() - au).append(" (").append(com.darkcraft.enemigos.DragonAnims.NOMBRES[d.accion()]).append(')');
                sv.append("; en total: ").append(d.estado());
            }
            log.println(sv);
            // ---- cae (volando: cae del cielo), se tumba, se apaga y no vuelve
            for (int k = 0; k < 120 && d.accion() != 0; k++) pasoDragon(d, m, caja, quitar, null, 0, 0, 0);
            d.setHealth(d.getMaxHealth());
            if (!d.vuela()) {
                d.despegar();
                for (int k = 0; k < 90; k++) pasoDragon(d, m, caja, quitar, null, 0, 0, 0);
            }
            boolean volaba = d.vuela();
            double altoAlMorir = d.getY() - cy;
            d.setTarget(fp);
            d.invulnerableTime = 0;
            d.hurt(m.damageSources().playerAttack(fp), 99999f);
            boolean muere = !d.isAlive();
            int alSuelo = -1, seVa = -1;
            for (int fin = 0; fin < 600; fin++) {
                if (!d.isRemoved()) {
                    d.tickCount++;
                    d.tick();
                }
                if (alSuelo < 0 && !d.vuela()) alSuelo = fin;
                if (seVa < 0 && d.isRemoved()) {
                    seVa = fin;
                    break;
                }
            }
            com.darkcraft.enemigos.Puestos.marcar(server, sitio, com.darkcraft.morvain.MorvainEnemigos.dragon(), true);
            int objetos = m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, caja).size();
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            com.darkcraft.enemigos.Puestos.marcar(server, sitio, com.darkcraft.morvain.MorvainEnemigos.dragon(), true);
            boolean vuelve = false;
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio)) {
                if (e instanceof com.darkcraft.enemigos.Dragon) vuelve = true;
                else e.discard();
            }
            log.println(String.format(java.util.Locale.ROOT, "Morvain, Vorthak cae: muere %s volando %s (a %.1f del suelo), llega al suelo en el tick %d, se deshace en el"
                            + " tick %d (la frase 5 dura %d), objetos %d, vuelve al descansar %s (tiene que ser false); %s", muere, volaba, altoAlMorir, alSuelo, seVa,
                    com.darkcraft.rey.Voces.ticks(com.darkcraft.enemigos.Dragon.VOZ_BASE + 5), objetos, vuelve, com.darkcraft.morvain.MorvainEnemigos.estadoDragon(server)));
            for (var it : m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, caja)) it.discard();
        } catch (Throwable t) {
            log.println("Morvain, Vorthak: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] dragon", t);
        } finally {
            if (d != null) d.quieta = false;
            for (int k = bloques.size() - 1; k >= 0; k--) m.setBlock((BlockPos) bloques.get(k)[0], (net.minecraft.world.level.block.state.BlockState) bloques.get(k)[1], 2);
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            for (var e : m.getEntitiesOfClass(net.minecraft.world.entity.Entity.class, caja,
                    q -> q instanceof com.darkcraft.enemigos.ProyectilDragon || q instanceof com.darkcraft.enemigos.Cantaro.Llamas)) e.discard();
            // todo como antes: Vorthak dormido en su oro y los calcinados en su sitio
            com.darkcraft.enemigos.Puestos.marcar(server, sitio, com.darkcraft.morvain.MorvainEnemigos.dragon(), false);
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            try {
                com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
                log.println("Morvain, Vorthak vuelve: " + com.darkcraft.morvain.MorvainEnemigos.estadoDragon(server));
            } catch (Throwable t) {
                log.println("Morvain, Vorthak vuelve: ERROR " + t);
            }
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    private static void tickAldric(ServerLevel m, net.minecraft.world.phys.AABB caja, java.util.List<net.minecraft.world.entity.Entity> quitar) {
        for (var r : m.getEntitiesOfClass(com.darkcraft.enemigos.Aldric.class, caja)) {
            if (!r.reflejo() || r.isRemoved()) continue;
            if (!quitar.contains(r)) quitar.add(r);
            r.tickCount++;
            r.tick();
        }
    }

    /**
     * Parte 7w: los gigantes errantes de Morvain. Sus vueltas (lo que miden, lo que suben, que no pisan agua y que van por su
     * region); que andan de memoria sin nadie cerca; uno fuera: anda por su vuelta con los pies a ras de suelo; al verle, habla y
     * ruge al acabar la frase; sus golpes a una presa (el mazazo donde cae y su onda, el barrido, el pisoton, la patada que lanza,
     * la roca que arranca y tira); cae sobre una rodilla si se le pega mucho en poco tiempo (y entonces le entra mas); a media vida
     * ruge; y al caer: de rodillas, de bruces, su arma queda en el suelo (la campana de Ogmund) y no vuelve. Al acabar, todo como
     * estaba (sin el arma en el suelo y el gigante otra vez en pie).
     */
    private static void gigantes(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        com.darkcraft.enemigos.Gigante g = null;
        final int I = com.darkcraft.enemigos.Gigante.OGMUND;
        double s0 = com.darkcraft.morvain.Gigantes.andado(server, I);
        try {
            // ---- las vueltas
            StringBuilder sb = new StringBuilder("Morvain, gigantes: vueltas");
            com.darkcraft.morvain.Region[] suya = {com.darkcraft.morvain.Region.LLANURAS, com.darkcraft.morvain.Region.COSTA,
                    com.darkcraft.morvain.Region.CAPITAL};
            for (int i = 0; i < 3; i++) {
                double largo = com.darkcraft.morvain.RutasGigantes.largo(i), sube = 0, antes = Double.NaN;
                int agua = 0, fuera = 0, n = 0;
                for (double s = 0; s < largo; s += 8) {
                    double[] p = com.darkcraft.morvain.RutasGigantes.punto(i, s);
                    var c = com.darkcraft.morvain.Terreno.columna(p[0], p[1]);
                    n++;
                    if (c.altura() < com.darkcraft.morvain.Terreno.AGUA + 0.5) agua++;
                    if (c.region() != suya[i]) fuera++;
                    if (!Double.isNaN(antes)) sube = Math.max(sube, Math.abs(c.altura() - antes) / 8);
                    antes = c.altura();
                }
                sb.append(String.format(java.util.Locale.ROOT, " | %s: %d bloques, pendiente max %.2f, agua %d de %d (tiene que ser 0), fuera de su region %d",
                        com.darkcraft.enemigos.Gigante.TIPOS[i], Math.round(largo), sube, agua, n, fuera));
            }
            // ---- de memoria: sin nadie cerca andan solos (dos bloques por segundo)
            for (int i = 0; i < 3; i++) com.darkcraft.morvain.Gigantes.recoger(i);
            double[] antes = new double[3];
            for (int i = 0; i < 3; i++) antes[i] = com.darkcraft.morvain.Gigantes.andado(server, i);
            for (int k = 0; k < 20; k++) com.darkcraft.morvain.Gigantes.tick(m, true);
            sb.append(" | de memoria en 10 s:");
            for (int i = 0; i < 3; i++)
                sb.append(String.format(java.util.Locale.ROOT, " %.1f", com.darkcraft.morvain.Gigantes.andado(server, i) - antes[i]));
            sb.append(" (20 cada uno) | ").append(com.darkcraft.morvain.Gigantes.estado(server));
            log.println(sb);
            // ---- uno fuera, por su vuelta
            com.darkcraft.morvain.Gigantes.revivir(server, I);
            double s = com.darkcraft.morvain.Gigantes.andado(server, I);
            double[] p = com.darkcraft.morvain.RutasGigantes.punto(I, s);
            for (int cx = -4; cx <= 4; cx++)
                for (int cz = -4; cz <= 4; cz++) m.getChunk(((int) Math.floor(p[0]) >> 4) + cx, ((int) Math.floor(p[1]) >> 4) + cz);
            g = com.darkcraft.morvain.Gigantes.crear(m, I, s);
            if (g == null) {
                log.println("Morvain, gigante: NO SALE");
                return;
            }
            quitar.add(g);
            final com.darkcraft.enemigos.Gigante gg = g;
            double x0 = g.getX(), z0 = g.getZ(), sueloMax = 0;
            int pasos = 0;
            for (int k = 0; k < 200; k++) {
                g.tickCount++;
                g.tick();
                if (g.andando()) pasos++;
                sueloMax = Math.max(sueloMax, Math.abs(g.getY() - com.darkcraft.enemigos.Gigante.suelo(m, g.getX(), g.getZ(), g.getY())));
            }
            double anda = Math.hypot(g.getX() - x0, g.getZ() - z0);
            StringBuilder sg = new StringBuilder(String.format(java.util.Locale.ROOT, "Morvain, gigante %s: vida %.0f, golpe %.1f, armadura %d, caja %.1f x %.1f, "
                            + "sin fisica %s; en 10 s anda %.1f bloques (unos 20), andando %d ticks, lejos de su vuelta %.1f, los pies a %.2f del suelo como mucho,"
                            + " lo andado %.0f -> %.0f, musica %d, barra '%s'",
                    g.tipo(), g.getMaxHealth(), g.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE), g.getArmorValue(),
                    g.getBbWidth(), g.getBbHeight(), g.noPhysics, anda, pasos, com.darkcraft.morvain.RutasGigantes.distancia(I, g.getX(), g.getZ()), sueloMax, s,
                    g.s(), g.musica(), g.getDisplayName().getString()));
            log.println(sg);
            // ---- al ver a alguien: habla y ruge al acabar la frase
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("7d9e1f30-5b62-4d8e-8fa0-4b5c6d7e8fcd"), "[DarkCraftCI14]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            net.minecraft.world.phys.Vec3 f = net.minecraft.world.phys.Vec3.directionFromRotation(0, g.getYRot());
            final double px = g.getX() + f.x * 14, pz = g.getZ() + f.z * 14;
            final double py = com.darkcraft.enemigos.Gigante.suelo(m, px, pz, g.getY());
            fp.moveTo(px, py, pz, 0f, 0f);
            fp.setOnGround(true);
            g.setTarget(fp);
            int ruge = -1, primero = -1;
            for (int k = 0; k < 260 && ruge < 0; k++) {
                g.tickCount++;
                g.tick();
                fp.moveTo(px, py, pz, 0f, 0f);
                fp.setHealth(fp.getMaxHealth());
                if (g.accion() == com.darkcraft.enemigos.GiganteAnims.RUGIDO) ruge = k;
            }
            int dura = com.darkcraft.rey.Voces.ticks(com.darkcraft.enemigos.Gigante.VOZ_BASE + 1);
            for (int k = 0; k < 70; k++) {
                g.tickCount++;
                g.tick();
            }
            log.println(String.format(java.util.Locale.ROOT, "Morvain, gigante al ver a alguien: en pelea %s, ruge en el tick %d (la frase dura %d), "
                    + "a cuantos empuja el rugido %d, se le acerca: a %.1f", g.enPelea(), ruge, dura, g.empujados(), Math.hypot(g.getX() - px, g.getZ() - pz)));
            // ---- sus golpes a una presa (quieto: solo lo que se le manda)
            g.quieta = true;
            var victima = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
            StringBuilder sv = new StringBuilder("Morvain, los golpes del gigante a una presa:");
            if (victima != null) {
                victima.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(900.0);
                victima.setHealth(900f);
                victima.setNoAi(true);
                victima.moveTo(px, py, pz, 0f, 0f);
                m.addFreshEntity(victima);
                quitar.add(victima);
                String[] golpes = {"mazazo", "onda del mazazo", "barrido", "pisoton", "patada", "roca"};
                int[] acc = {com.darkcraft.enemigos.GiganteAnims.MAZAZO, com.darkcraft.enemigos.GiganteAnims.MAZAZO, com.darkcraft.enemigos.GiganteAnims.BARRIDO,
                        com.darkcraft.enemigos.GiganteAnims.PISOTON, com.darkcraft.enemigos.GiganteAnims.PATADA, com.darkcraft.enemigos.GiganteAnims.ROCA};
                double[] lejos = {6.0, 9.6, 5.5, 2.6, 3.2, 20.0};
                final double gx = g.getX(), gz = g.getZ();
                for (int i = 0; i < golpes.length; i++) {
                    victima.setHealth(victima.getMaxHealth());
                    victima.invulnerableTime = 0;
                    victima.hurtTime = 0;
                    victima.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    double vx = gx, vz = gz + lejos[i];
                    double vy = com.darkcraft.enemigos.Gigante.suelo(m, vx, vz, py);
                    victima.moveTo(vx, vy, vz, 180f, 0f);
                    victima.setOnGround(true);
                    g.moveTo(gx, com.darkcraft.enemigos.Gigante.suelo(m, gx, gz, g.getY()), gz, 0f, 0f);
                    g.setYRot(0f);
                    g.yBodyRot = g.yHeadRot = 0f;
                    g.setTarget(victima);
                    int maz = g.golpesMazazo(), ond = g.ondasMazazo(), bar = g.barridos(), pis = g.ondasPisoton(), pat = g.patadas(), roc = g.rocas();
                    int caen = com.darkcraft.enemigos.RocaGigante.CAEN, dan = com.darkcraft.enemigos.RocaGigante.DAN;
                    g.empezar(acc[i]);
                    float antesV = victima.getHealth();
                    double alto = 0;
                    com.darkcraft.enemigos.RocaGigante roca = null;
                    double cae = -1;
                    for (int k = 0; k < 140; k++) {
                        if (g.accion() != 0) {
                            g.setTarget(victima);
                            g.tickCount++;
                            g.tick();
                        }
                        victima.moveTo(vx, vy, vz, 180f, 0f);
                        victima.setOnGround(true);
                        victima.invulnerableTime = 0;
                        if (i == 4) {
                            alto = Math.max(alto, victima.getDeltaMovement().y);
                            victima.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                        }
                        if (roca == null)
                            for (var r : m.getEntitiesOfClass(com.darkcraft.enemigos.RocaGigante.class, g.getBoundingBox().inflate(30))) {
                                roca = r;
                                quitar.add(r);
                            }
                        if (roca != null && !roca.isRemoved()) {
                            roca.tick();
                            if (roca.isRemoved()) cae = Math.hypot(roca.getX() - vx, roca.getZ() - vz);
                        }
                        if (g.accion() == 0 && (roca == null || roca.isRemoved()) && k > 4) break;
                    }
                    sv.append(String.format(java.util.Locale.ROOT, " | %s (a %.1f): le quita %.0f", golpes[i], lejos[i], antesV - victima.getHealth()));
                    switch (i) {
                        case 0 -> sv.append(", de lleno ").append(g.golpesMazazo() - maz);
                        case 1 -> sv.append(", onda ").append(g.ondasMazazo() - ond);
                        case 2 -> sv.append(", barridos ").append(g.barridos() - bar);
                        case 3 -> sv.append(", onda del pie ").append(g.ondasPisoton() - pis);
                        case 4 -> sv.append(String.format(java.util.Locale.ROOT, ", patadas %d, la lanza hacia arriba %.2f", g.patadas() - pat, alto));
                        default -> sv.append(String.format(java.util.Locale.ROOT, ", rocas %d, caen %d, dan %d, cae a %.1f de la presa", g.rocas() - roc,
                                com.darkcraft.enemigos.RocaGigante.CAEN - caen, com.darkcraft.enemigos.RocaGigante.DAN - dan, cae));
                    }
                }
            }
            log.println(sv);
            // ---- se le pega mucho en poco tiempo: sobre una rodilla (y entonces le entra un 50 % mas)
            g.setTarget(fp);
            g.quieta = true;
            for (int k = 0; k < 40 && g.accion() != 0; k++) {
                g.tickCount++;
                g.tick();
            }
            float v0 = g.getHealth();
            int at0 = g.aturdidos();
            for (int k = 0; k < 12 && g.aturdidos() == at0; k++) {
                g.invulnerableTime = 0;
                g.hurt(m.damageSources().playerAttack(fp), 18f);
            }
            boolean rodilla = g.accion() == com.darkcraft.enemigos.GiganteAnims.ATURDIDO;
            float v1 = g.getHealth();
            g.invulnerableTime = 0;
            g.hurt(m.damageSources().playerAttack(fp), 20f);
            float entra = v1 - g.getHealth();
            log.println(String.format(java.util.Locale.ROOT, "Morvain, el gigante sobre una rodilla: tras %.0f de dano en un momento %s (aturdido %d), un golpe de 20 le quita %.1f"
                    + " (con la armadura; de pie, menos)", v0 - v1, rodilla, g.aturdidos() - at0, entra));
            for (int k = 0; k < 80; k++) {
                g.tickCount++;
                g.tick();
            }
            // ---- a media vida: ruge
            g.mediaVida();
            int rugeMedia = -1;
            for (int k = 0; k < 60 && rugeMedia < 0; k++) {
                g.tickCount++;
                g.tick();
                if (g.accion() == com.darkcraft.enemigos.GiganteAnims.RUGIDO) rugeMedia = k;
            }
            log.println("Morvain, el gigante a media vida: segunda " + g.segunda() + ", ruge en el tick " + rugeMedia);
            // ---- cae: de rodillas, de bruces, su arma en el suelo; ya no vuelve
            com.darkcraft.morvain.Gigantes.ULTIMO = new java.util.ArrayList<>();
            int rep = com.darkcraft.valdrenn.Guardianes.repartosPendientes();
            g.invulnerableTime = 0;
            g.hurt(m.damageSources().playerAttack(fp), 99999f);
            boolean muriendo = g.muriendo();
            int k = 0;
            for (; k < 260 && !g.isRemoved(); k++) {
                g.tickCount++;
                g.tick();
            }
            java.util.List<BlockPos> arma = com.darkcraft.morvain.Gigantes.ULTIMO;
            int campana = 0;
            for (BlockPos b : arma) if (m.getBlockState(b).is(net.minecraft.world.level.block.Blocks.BELL)) campana++;
            log.println(String.format(java.util.Locale.ROOT, "Morvain, el gigante cae: muriendo %s, se deshace en %d ticks (la animacion dura %d), su arma en el suelo %d bloques"
                            + " (campana %d), caido %s, repartos %d -> %d", muriendo, k, com.darkcraft.enemigos.GiganteAnims.TICKS[com.darkcraft.enemigos.GiganteAnims.MORIR],
                    arma.size(), campana, com.darkcraft.morvain.Gigantes.muerto(server, I), rep, com.darkcraft.valdrenn.Guardianes.repartosPendientes()));
            for (BlockPos b : arma) m.setBlock(b, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            vistaGigantes(m, out);
        } catch (Exception e) {
            log.println("Morvain, gigantes: FALLO " + e);
            e.printStackTrace(log);
        } finally {
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            com.darkcraft.morvain.Gigantes.recoger(I);
            com.darkcraft.morvain.Gigantes.revivir(server, I);
            com.darkcraft.morvain.Gigantes.poner(server, I, s0);
        }
    }

    /**
     * Parte 7x: Ser Lucan, el Jinete Nocturno, en el camino de nubes del reino. Por donde va cada noche (lo andado a cada hora: de
     * dia no sale y al amanecer aun no ha llegado a la escalera); uno fuera: se alza de la niebla y cabalga por su camino hacia la
     * fortaleza con los cascos a ras del camino; al ver a alguien habla y toca el cuerno al acabar la frase (hiela a los de
     * cerca); sus golpes a una presa en el camino (el tajo, el barrido por detras, el pisoton con su onda, la carga que se lleva
     * por delante a quien pilla, y un tajo de lado): el empujon va siempre a lo largo del camino, tambien el que lo echaria fuera;
     * el caballo tropieza si se le pega mucho en poco tiempo (y entonces le entra mas); a media vida, el cuerno otra vez y el
     * fuego de almas que deja la carga (quema a quien lo pisa); al amanecer se deshace; y al caer ya no vuelve a ese camino. Al
     * acabar, todo como estaba (vivo, sin nadie fuera y a la hora de antes).
     */
    private static void jinete(MinecraftServer server, ServerLevel lvl, PrintWriter log, int[] reino) {
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        long hora0 = lvl.getDayTime();
        String k = com.darkcraft.camino.Jinetes.clave(reino[0], reino[1]);
        final java.util.Locale L = java.util.Locale.ROOT;
        try {
            var r = com.darkcraft.world.CaminoNubes.ruta(lvl.getSeed(), reino[0], reino[1]);
            if (r == null) {
                log.println("Jinete Nocturno: SIN CAMINO");
                return;
            }
            final double Y = com.darkcraft.world.NubesGenerador.SUELO;
            // ---- por donde va cada noche
            StringBuilder sb = new StringBuilder(String.format(L, "Jinete Nocturno, el camino %s: %d bloques; lo andado a cada hora:", k, Math.round(r.largo())));
            for (long h : new long[]{6000, 12999, 13000, 15500, 18000, 20500, 22999, 23000}) {
                lvl.setDayTime(h);
                sb.append(String.format(L, " %d=%.0f%s", h, com.darkcraft.camino.Jinetes.andado(lvl, r), com.darkcraft.camino.Jinetes.noche(lvl) ? "" : " (de dia)"));
            }
            lvl.setDayTime(22999);
            sb.append(String.format(L, " | al amanecer le faltan %.0f bloques para la escalera", r.largo() - com.darkcraft.camino.Jinetes.andado(lvl, r)));
            double s0 = com.darkcraft.camino.Jinetes.libre(lvl.getSeed(), reino[0], reino[1], r);
            double[] p0 = r.punto(s0);
            for (int cx = -5; cx <= 5; cx++)
                for (int cz = -5; cz <= 5; cz++) lvl.getChunk(((int) Math.floor(p0[0]) >> 4) + cx, ((int) Math.floor(p0[1]) >> 4) + cz);
            boolean suyo = false;
            for (int[] o : com.darkcraft.camino.Jinetes.reinos(lvl.getSeed(), p0[0], p0[1])) if (o[0] == reino[0] && o[1] == reino[1]) suyo = true;
            var bajo = lvl.getBlockState(BlockPos.containing(p0[0], Y - 1, p0[1]));
            sb.append(String.format(L, " | lejos de las paradas: a %.0f del principio (%d %d), su camino pasa por alli %s, bajo los cascos %s",
                    s0, Math.round(p0[0]), Math.round(p0[1]), suyo, net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(bajo.getBlock()).getPath()));
            log.println(sb);
            // ---- uno fuera: se alza de la niebla y cabalga por su camino
            lvl.setDayTime(com.darkcraft.camino.Jinetes.hora(r, s0));
            com.darkcraft.camino.Jinetes.revivir(server, k);
            com.darkcraft.enemigos.Jinete j = com.darkcraft.camino.Jinetes.crear(lvl, k, r, s0, true);
            if (j == null) {
                log.println("Jinete Nocturno: NO SALE");
                return;
            }
            quitar.add(j);
            boolean aparece = j.accion() == com.darkcraft.enemigos.JineteAnims.APARECE;
            for (int t = 0; t < 34; t++) {
                j.tickCount++;
                j.tick();
            }
            double sIni = com.darkcraft.camino.Jinetes.donde(r, j.getX(), j.getZ()), lejosMax = 0, piesMax = 0;
            int andando = 0;
            for (int t = 0; t < 200; t++) {
                j.tickCount++;
                j.tick();
                if (j.andando()) andando++;
                lejosMax = Math.max(lejosMax, com.darkcraft.camino.Jinetes.distancia(r, j.getX(), j.getZ()));
                piesMax = Math.max(piesMax, Math.abs(j.getY() - Y));
            }
            double sFin = com.darkcraft.camino.Jinetes.donde(r, j.getX(), j.getZ());
            log.println(String.format(L, "Jinete Nocturno fuera: se alza de la niebla %s, vida %.0f, golpe %.1f, armadura %d, caja %.1f x %.1f, sin fisica %s, musica %d,"
                            + " barra '%s'; en 10 s cabalga %.1f bloques del camino hacia la fortaleza (unos 64), andando %d de 200 ticks, lejos del camino %.2f como mucho,"
                            + " los cascos a %.2f del camino como mucho | %s",
                    aparece, j.getMaxHealth(), j.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE), j.getArmorValue(),
                    j.getBbWidth(), j.getBbHeight(), j.noPhysics, j.musica(), j.getDisplayName().getString(), sFin - sIni, andando, lejosMax, piesMax,
                    com.darkcraft.camino.Jinetes.estado(lvl, j.getX(), j.getZ())));
            // ---- al ver a alguien: habla y toca el cuerno al acabar la frase (hiela a los de cerca)
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(lvl, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("7d9e1f30-5b62-4d8e-8fa0-4b5c6d7e8fce"), "[DarkCraftCI15]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            fp.removeAllEffects();
            double[] pf = r.punto(Math.min(r.largo() - 10, com.darkcraft.camino.Jinetes.donde(r, j.getX(), j.getZ()) + 8));
            final double px = pf[0], pz = pf[1];
            fp.moveTo(px, Y, pz, 0f, 0f);
            fp.setOnGround(true);
            j.setTarget(fp);
            int cuerno = -1;
            for (int t = 0; t < 300 && cuerno < 0; t++) {
                j.tickCount++;
                j.tick();
                fp.moveTo(px, Y, pz, 0f, 0f);
                fp.setHealth(fp.getMaxHealth());
                if (j.accion() == com.darkcraft.enemigos.JineteAnims.CUERNO) cuerno = t;
            }
            for (int t = 0; t < 50; t++) {
                j.tickCount++;
                j.tick();
                fp.moveTo(px, Y, pz, 0f, 0f);
                fp.setHealth(fp.getMaxHealth());
            }
            log.println(String.format(L, "Jinete Nocturno al ver a alguien: en pelea %s, toca el cuerno en el tick %d (la frase dura %d), cuernos %d, helados %d,"
                            + " al de delante: lentitud %s, debilidad %s; se le acerca: a %.1f",
                    j.enPelea(), cuerno, com.darkcraft.rey.Voces.ticks(com.darkcraft.enemigos.Jinete.VOZ_BASE + 1), j.cuernos(), j.helados(),
                    fp.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN), fp.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS),
                    Math.hypot(j.getX() - px, j.getZ() - pz)));
            // ---- sus golpes a una presa en el camino (quieto: solo lo que se le manda)
            j.quieta = true;
            for (int t = 0; t < 80 && j.accion() != 0; t++) {
                j.tickCount++;
                j.tick();
            }
            var victima = net.minecraft.world.entity.EntityType.VILLAGER.create(lvl);
            double s1 = Math.min(r.largo() - 80, s0 + 40);
            double[] q1 = r.punto(s1), tg = com.darkcraft.camino.Jinetes.tangente(r, s1);
            float giro = (float) (Math.atan2(tg[1], tg[0]) * 180 / Math.PI) - 90f;
            StringBuilder sv = new StringBuilder("Jinete Nocturno, sus golpes a una presa en el camino:");
            if (victima != null) {
                victima.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(900.0);
                victima.setHealth(900f);
                victima.setNoAi(true);
                victima.moveTo(q1[0], Y, q1[1], 0f, 0f);
                lvl.addFreshEntity(victima);
                quitar.add(victima);
                String[] golpes = {"tajo", "barrido (por detras)", "pisoton", "carga", "tajo de lado (el empujon iria fuera del camino)"};
                int[] acc = {com.darkcraft.enemigos.JineteAnims.TAJO, com.darkcraft.enemigos.JineteAnims.BARRIDO, com.darkcraft.enemigos.JineteAnims.PISOTEA,
                        com.darkcraft.enemigos.JineteAnims.CARGA, com.darkcraft.enemigos.JineteAnims.TAJO};
                // donde queda la presa: {delante (a lo largo del camino), a un lado}, y hacia donde mira el jinete (grados de mas)
                double[][] donde = {{4.0, 0}, {-4.0, 0}, {2.6, 0}, {20.0, 0}, {0, 1.3}};
                float[] mira = {0, 0, 0, 0, 90};
                for (int i = 0; i < golpes.length; i++) {
                    double vx = q1[0] + tg[0] * donde[i][0] - tg[1] * donde[i][1], vz = q1[1] + tg[1] * donde[i][0] + tg[0] * donde[i][1];
                    victima.setHealth(victima.getMaxHealth());
                    victima.invulnerableTime = 0;
                    victima.hurtTime = 0;
                    victima.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    victima.moveTo(vx, Y, vz, 0f, 0f);
                    float g = giro + mira[i];
                    j.moveTo(q1[0], Y, q1[1], g, 0f);
                    j.setYRot(g);
                    j.yBodyRot = j.yHeadRot = g;
                    j.setTarget(victima);
                    int ta = j.tajos(), ba = j.barridos(), on = j.ondasPisoton(), gc = j.golpesCarga(), fu = j.fueraDelCamino();
                    float antes = victima.getHealth();
                    net.minecraft.world.phys.Vec3 empujon = null;
                    j.empezar(acc[i]);
                    for (int t = 0; t < 100; t++) {
                        if (j.accion() != 0) {
                            j.setTarget(victima);
                            j.tickCount++;
                            j.tick();
                        }
                        if (empujon == null && victima.getHealth() < antes) empujon = victima.getDeltaMovement();
                        victima.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                        victima.moveTo(vx, Y, vz, 0f, 0f);
                        victima.invulnerableTime = 0;
                        victima.hurtTime = 0;
                        if (j.accion() == 0 && t > 4) break;
                    }
                    double[] tv = com.darkcraft.camino.Jinetes.tangente(r, com.darkcraft.camino.Jinetes.donde(r, vx, vz));
                    double l = empujon == null ? 0 : Math.hypot(empujon.x, empujon.z);
                    double desvio = l < 1e-4 ? -1 : Math.toDegrees(Math.acos(Math.min(1.0, Math.abs(empujon.x * tv[0] + empujon.z * tv[1]) / l)));
                    sv.append(String.format(L, " | %s: le quita %.1f, empujon %.2f a %.0f grados del camino", golpes[i], antes - victima.getHealth(), l, desvio));
                    switch (i) {
                        case 0, 4 -> sv.append(", tajos ").append(j.tajos() - ta);
                        case 1 -> sv.append(", barridos ").append(j.barridos() - ba);
                        case 2 -> sv.append(", onda ").append(j.ondasPisoton() - on);
                        default -> sv.append(", la carga le pilla ").append(j.golpesCarga() - gc);
                    }
                    sv.append(", enderezados al camino ").append(j.fueraDelCamino() - fu);
                }
            }
            log.println(sv);
            // ---- se le pega mucho en poco tiempo: el caballo tropieza (y entonces le entra un 30 % mas)
            j.setTarget(fp);
            for (int t = 0; t < 60 && j.accion() != 0; t++) {
                j.tickCount++;
                j.tick();
            }
            float h0 = j.getHealth();
            j.invulnerableTime = 0;
            j.hurt(lvl.damageSources().playerAttack(fp), 20f);
            float dePie = h0 - j.getHealth();
            int at0 = j.aturdidos();
            for (int i = 0; i < 12 && j.aturdidos() == at0; i++) {
                j.invulnerableTime = 0;
                j.hurt(lvl.damageSources().playerAttack(fp), 18f);
            }
            boolean tropieza = j.accion() == com.darkcraft.enemigos.JineteAnims.ATURDIDO;
            float h1 = j.getHealth();
            j.invulnerableTime = 0;
            j.hurt(lvl.damageSources().playerAttack(fp), 20f);
            float deRodillas = h1 - j.getHealth();
            log.println(String.format(L, "Jinete Nocturno, el caballo tropieza: tras %.0f de dano en un momento %s (aturdidos %d); un golpe de 20 le quita %.1f de pie y %.1f de rodillas",
                    h0 - h1, tropieza, j.aturdidos() - at0, dePie, deRodillas));
            for (int t = 0; t < 80; t++) {
                j.tickCount++;
                j.tick();
            }
            // ---- a media vida: el cuerno otra vez y el fuego de almas que deja la carga (quema a quien lo pisa)
            j.mediaVida();
            int cu0 = j.cuernos(), cuernoMedia = -1;
            for (int t = 0; t < 80 && cuernoMedia < 0; t++) {
                j.tickCount++;
                j.tick();
                if (j.accion() == com.darkcraft.enemigos.JineteAnims.CUERNO) cuernoMedia = t;
            }
            for (int t = 0; t < 70 && j.accion() != 0; t++) {
                j.tickCount++;
                j.tick();
            }
            String fuego = "sin presa";
            if (victima != null) {
                double vx = q1[0] + tg[0] * 20, vz = q1[1] + tg[1] * 20;
                victima.setHealth(victima.getMaxHealth());
                victima.moveTo(vx, Y, vz, 0f, 0f);
                j.moveTo(q1[0], Y, q1[1], giro, 0f);
                j.setYRot(giro);
                j.yBodyRot = j.yHeadRot = giro;
                j.setTarget(victima);
                int f0 = j.fuegosPuestos();
                double x0 = j.getX(), z0 = j.getZ();
                j.empezar(com.darkcraft.enemigos.JineteAnims.CARGA);
                for (int t = 0; t < 90 && j.accion() != 0; t++) {
                    j.setTarget(victima);
                    j.tickCount++;
                    j.tick();
                    victima.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    victima.moveTo(vx, Y, vz, 0f, 0f);
                    victima.invulnerableTime = 0;
                }
                double mx = (x0 + j.getX()) / 2, mz = (z0 + j.getZ()) / 2;
                int puestas = j.fuegosPuestos() - f0, qu0 = j.quemados();
                j.setTarget(null);
                j.parado = true;
                for (int t = 0; t < 25; t++) {
                    victima.moveTo(mx, Y, mz, 0f, 0f);
                    victima.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    victima.invulnerableTime = 0;
                    j.tickCount++;
                    j.tick();
                }
                fuego = String.format(L, "la carga deja %d llamas (arden %d), a la presa en medio del rastro la queman %d veces en 25 ticks (ardiendo %s)", puestas, j.fuegos(),
                        j.quemados() - qu0, victima.isOnFire());
                j.parado = false;
            }
            log.println(String.format(L, "Jinete Nocturno a media vida: segunda %s, el cuerno otra vez en el tick %d (cuernos %d -> %d); %s", j.segunda(), cuernoMedia, cu0, j.cuernos(), fuego));
            // ---- al amanecer: se deshace (sin nadie cerca, sin decir nada)
            lvl.setDayTime(23000);
            com.darkcraft.camino.Jinetes.tick(lvl, false);
            boolean amanece = j.amanece();
            int deshace = 0;
            for (; deshace < 90 && !j.isRemoved(); deshace++) {
                j.tickCount++;
                j.tick();
            }
            log.println(String.format(L, "Jinete Nocturno al amanecer: amanece %s, se deshace %s en %d ticks (la animacion dura %d), sigue fuera %s",
                    amanece, j.isRemoved(), deshace, com.darkcraft.enemigos.JineteAnims.TICKS[com.darkcraft.enemigos.JineteAnims.DESAPARECE],
                    com.darkcraft.camino.Jinetes.fuera(k) != null));
            // ---- cae: ya no vuelve a ese camino (y la carta)
            lvl.setDayTime(com.darkcraft.camino.Jinetes.hora(r, s1));
            com.darkcraft.enemigos.Jinete j2 = com.darkcraft.camino.Jinetes.crear(lvl, k, r, s1, false);
            if (j2 != null) {
                quitar.add(j2);
                j2.setTarget(fp);
                int rep = com.darkcraft.valdrenn.Guardianes.repartosPendientes();
                j2.invulnerableTime = 0;
                j2.hurt(lvl.damageSources().playerAttack(fp), 99999f);
                boolean muriendo = j2.muriendo();
                int t = 0;
                for (; t < 220 && !j2.isRemoved(); t++) {
                    j2.tickCount++;
                    j2.tick();
                }
                var carta = com.darkcraft.fortaleza.Perdidos.libro(com.darkcraft.camino.Jinetes.CARTA);
                var tag = carta.getTag();
                log.println(String.format(L, "Jinete Nocturno cae: muriendo %s, se deshace en %d ticks (la animacion dura %d), caido %s, sigue fuera %s, repartos %d -> %d;"
                                + " la carta: %s, %d paginas, libro perdido %d de %d", muriendo, t,
                        com.darkcraft.enemigos.JineteAnims.TICKS[com.darkcraft.enemigos.JineteAnims.MORIR], com.darkcraft.camino.Jinetes.caido(server, k),
                        com.darkcraft.camino.Jinetes.fuera(k) != null, rep, com.darkcraft.valdrenn.Guardianes.repartosPendientes(),
                        net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(carta.getItem()).getPath(), tag == null ? 0 : tag.getList("pages", 8).size(),
                        com.darkcraft.camino.Jinetes.CARTA, com.darkcraft.fortaleza.Perdidos.TOTAL));
            }
        } catch (Exception e) {
            log.println("Jinete Nocturno: FALLO " + e);
            e.printStackTrace(log);
        } finally {
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            com.darkcraft.camino.Jinetes.recoger(k);
            com.darkcraft.camino.Jinetes.revivir(server, k);
            lvl.setDayTime(hora0);
        }
    }

    /** El mapa de las tres vueltas (la region de cada punto) para verlas en las capturas de la prueba. */
    private static void vistaGigantes(ServerLevel m, File out) {
        try {
            int w = 512;
            var img = new java.awt.image.BufferedImage(w, w, java.awt.image.BufferedImage.TYPE_INT_RGB);
            double esc = 16000.0 / w;
            for (int y = 0; y < w; y++)
                for (int x = 0; x < w; x++) {
                    double wx = -8000 + x * esc, wz = -8000 + y * esc;
                    var c = com.darkcraft.morvain.Terreno.columna(wx, wz);
                    int col = c.region().color;
                    if (c.altura() < com.darkcraft.morvain.Terreno.AGUA) col = 0x1B2A33;
                    img.setRGB(x, y, col);
                }
            for (int i = 0; i < 3; i++) {
                double largo = com.darkcraft.morvain.RutasGigantes.largo(i);
                for (double s = 0; s < largo; s += 4) {
                    double[] p = com.darkcraft.morvain.RutasGigantes.punto(i, s);
                    int x = (int) ((p[0] + 8000) / esc), y = (int) ((p[1] + 8000) / esc);
                    if (x >= 0 && y >= 0 && x < w && y < w) img.setRGB(x, y, 0xFF2020);
                }
            }
            javax.imageio.ImageIO.write(img, "png", new File(out, "morvain_gigantes.png"));
        } catch (Exception ignored) {
        }
    }

    /** Mueve a la presa con lo que la empujan (la prueba no mueve el mundo): el arrastre del latigazo, las ondas. */
    private static void moverPresa(net.minecraft.world.entity.LivingEntity v) {
        net.minecraft.world.phys.Vec3 dv = v.getDeltaMovement();
        if (dv.lengthSqr() > 1e-4) {
            v.move(net.minecraft.world.entity.MoverType.SELF, dv);
            v.setDeltaMovement(dv.x * 0.6, v.onGround() ? 0 : dv.y - 0.08, dv.z * 0.6);
        }
    }

    /**
     * Parte 7v: Aldric, el Primer Marcado, en lo alto del ancla del Cerco: de rodillas en su puesto (y en su suelo); al verle
     * habla mientras se levanta, ruge al acabar la frase (los marcados del cerco se arrodillan, rendidos, y no toman presa; la
     * cadena cierra la puerta) y salta desde lo alto sobre su presa; cada accion acaba; sus golpes a una presa (el cadenazo, el
     * latigazo que la arrastra, la estrella y su onda, el torbellino que pega dos veces y el salto con su onda); a media vida le
     * arde la marca (el estallido), su reflejo repite el golpe (no se le puede pegar y se deshace) y encadena (las marcas y las
     * cadenas que brotan y sujetan); y al caer: se deshace en sombra, la puerta se abre, los marcados de rodillas y los rendidos
     * descansan y los de rodillas ya no vuelven. Al acabar, todo como estaba.
     */
    private static void aldric(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.CERCO;
        final int CX = com.darkcraft.morvain.Cerco.CX, CZ = com.darkcraft.morvain.Cerco.CZ, PISO = com.darkcraft.morvain.Cerco.PISO;
        final var caja = new net.minecraft.world.phys.AABB(CX - 50, PISO - 10, CZ - 50, CX + 50, PISO + 30, CZ + 60);
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        com.darkcraft.enemigos.Aldric al = null;
        try {
            for (long k : com.darkcraft.morvain.Cerco.get().chunks()) m.getChunk((int) (k >> 32), (int) k);
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            java.util.List<com.darkcraft.enemigos.Marcado> marcados = new java.util.ArrayList<>();
            for (int k = 0; k < 6; k++)
                for (var e : fuera) if (e.isAlive() && !(e instanceof com.darkcraft.enemigos.Aldric)) e.tick();
            for (var e : fuera) {
                if (e instanceof com.darkcraft.enemigos.Aldric x) al = x;
                else if (e instanceof com.darkcraft.enemigos.Marcado mk) marcados.add(mk);
            }
            if (al == null) {
                log.println("Morvain, Aldric: NO SALE");
                return;
            }
            boolean enSuSuelo = !m.getBlockState(BlockPos.containing(al.getX(), al.getY() - 0.2, al.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty();
            StringBuilder sb = new StringBuilder(String.format(java.util.Locale.ROOT, "Morvain, Aldric: en %s postura %d (1 de rodillas), giro %d, vida %.0f, golpe %.1f,"
                            + " armadura %d, caja %.1f x %.1f, en lo alto del ancla %s (a %d del suelo del patio), en su suelo %s, metido en un bloque %s (tiene que ser false),"
                            + " musica %d; marcados del cerco %d", al.blockPosition().toShortString(), al.postura(), Math.round(al.getYRot()), al.getMaxHealth(),
                    al.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE), al.getArmorValue(), al.getBbWidth(), al.getBbHeight(),
                    Math.abs(al.getX() - CX - 0.5) < 5 && Math.abs(al.getZ() - CZ - 0.5) < 5, Math.round(al.getY() - PISO), enSuSuelo, !m.noCollision(al), al.musica(),
                    marcados.size()));
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("6c8d0e2f-4a51-4c7d-9e9f-3a4b5c6d7ebc"), "[DarkCraftCI13]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            // ---- le ve en el patio: habla mientras se levanta, ruge al acabar (se arrodillan los marcados y la cadena cierra la puerta) y
            // salta desde lo alto sobre su presa
            final double fxx = CX + 4.5, fyy = PISO, fzz = CZ + 16.5;
            fp.moveTo(fxx, fyy, fzz, 180f, 0f);
            fp.setOnGround(true);
            int sell0 = com.darkcraft.morvain.Cerco.SELLADOS;
            al.setTarget(fp);
            int levanta = -1, ruge = -1, salta = -1, cae = -1, rindenEn = -1;
            boolean selloAlRugir = false;
            double alto = 0;
            for (int k = 0; k < 520; k++) {
                al.tickCount++;
                al.tick();
                tickAldric(m, caja, quitar);
                com.darkcraft.morvain.Cerco.tick(m);
                fp.moveTo(fxx, fyy, fzz, 180f, 0f);
                fp.setOnGround(true);
                fp.setHealth(fp.getMaxHealth());
                int a = al.accion();
                if (levanta < 0 && a == com.darkcraft.enemigos.AldricAnims.LEVANTARSE) levanta = k;
                if (ruge < 0 && a == com.darkcraft.enemigos.AldricAnims.RUGIDO) ruge = k;
                if (rindenEn < 0 && al.rendidos() > 0) {
                    rindenEn = k;
                    selloAlRugir = com.darkcraft.morvain.Cerco.sellado(m);
                }
                if (salta < 0 && a == com.darkcraft.enemigos.AldricAnims.SALTO) salta = k;
                if (salta >= 0) alto = Math.max(alto, al.getY() - PISO);
                if (salta >= 0 && al.caidas() > 0) {
                    cae = k;
                    break;
                }
            }
            int deRodillas = 0, rendidos = 0;
            for (var mk : marcados) {
                if (mk.isRemoved()) continue;
                if (mk.rendido()) rendidos++;
                if (mk.postura() == com.darkcraft.enemigos.Enemigo.REZA) deRodillas++;
            }
            // uno rendido no toma presa
            com.darkcraft.enemigos.Marcado unRendido = null;
            for (var mk : marcados) if (!mk.isRemoved() && mk.rendido()) unRendido = mk;
            boolean tomaPresa = false;
            if (unRendido != null) {
                unRendido.setTarget(fp);
                tomaPresa = unRendido.getTarget() != null;
            }
            int barreras = 0, cadenas = 0;
            for (int u = -2; u <= 2; u++)
                for (int y = PISO; y <= PISO + 6; y++) {
                    if (m.getBlockState(new BlockPos(CX + u, y, CZ + 33)).is(net.minecraft.world.level.block.Blocks.BARRIER)) barreras++;
                    if (m.getBlockState(new BlockPos(CX + u, y, CZ + 34)).is(net.minecraft.world.level.block.Blocks.CHAIN)) cadenas++;
                }
            sb.append("; al verle: pelea ").append(al.enPelea()).append(", se levanta en el tick ").append(levanta).append(", ruge en el ").append(ruge)
                    .append(" (la frase 1 dura ").append(com.darkcraft.rey.Voces.ticks(com.darkcraft.enemigos.Aldric.VOZ_BASE + 1)).append("), se rinden ")
                    .append(al.rendidos()).append(" en el ").append(rindenEn).append(" (de los ").append(marcados.size()).append(": rendidos ").append(rendidos)
                    .append(", de rodillas ").append(deRodillas).append("; uno rendido toma presa ").append(tomaPresa).append(", tiene que ser false), la puerta cerrada ")
                    .append(selloAlRugir).append(" (se cierra +").append(com.darkcraft.morvain.Cerco.SELLADOS - sell0).append(", barreras ").append(barreras)
                    .append(", cadenas ").append(cadenas).append("), salta en el ").append(salta).append(" y cae en el ").append(cae)
                    .append(String.format(java.util.Locale.ROOT, " (subio hasta %.1f sobre el patio) a %.1f de su presa, en el suelo del patio %s", alto,
                            Math.hypot(al.getX() - fxx, al.getZ() - fzz), Math.abs(al.getY() - PISO) < 0.6));
            // ---- cada accion acaba (sin presa y quieto: no elige por su cuenta)
            al.quieta = true;
            al.setTarget(null);
            for (int k = 0; k < 120 && al.accion() != 0; k++) {
                al.tickCount++;
                al.tick();
                tickAldric(m, caja, quitar);
            }
            StringBuilder nn = new StringBuilder();
            int vistas = 0;
            for (int a = 1; a < com.darkcraft.enemigos.AldricAnims.TICKS.length; a++) {
                String nombre = com.darkcraft.enemigos.AldricAnims.NOMBRES[a];
                if (nombre.equals("morir")) continue;
                al.moveTo(CX + 0.5, PISO, CZ + 20.5, 0f, 0f);
                al.empezar(a);
                vistas++;
                for (int k = 0; k < 160 && al.accion() == a; k++) {
                    al.tickCount++;
                    al.tick();
                    tickAldric(m, caja, quitar);
                }
                if (al.accion() == a) nn.append(' ').append(nombre);
            }
            for (int k = 0; k < 60 && al.marcasPendientes() > 0; k++) {
                al.tickCount++;
                al.tick();
            }
            sb.append("; de ").append(vistas).append(" acciones no acaban:").append(nn.length() == 0 ? " ninguna" : nn);
            log.println(sb);
            // ---- sus golpes a una presa (un aldeano quieto y duro) en el patio, al sur del ancla: el mirando al sur
            StringBuilder sg = new StringBuilder("Morvain, los golpes de Aldric a una presa:");
            final double x0 = CX + 0.5, y0 = PISO, z0 = CZ + 17.5;
            var victima = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
            if (victima != null) {
                victima.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(900.0);
                victima.setHealth(900f);
                victima.setNoAi(true);
                victima.moveTo(x0, y0, z0 + 3, 180f, 0f);
                m.addFreshEntity(victima);
                quitar.add(victima);
                String[] golpes = {"cadenazo", "latigazo", "estrella", "torbellino", "salto", "encadenar"};
                int[] acc = {com.darkcraft.enemigos.AldricAnims.CADENAZO, com.darkcraft.enemigos.AldricAnims.LATIGAZO, com.darkcraft.enemigos.AldricAnims.ESTRELLA,
                        com.darkcraft.enemigos.AldricAnims.TORBELLINO, com.darkcraft.enemigos.AldricAnims.SALTO, com.darkcraft.enemigos.AldricAnims.ENCADENAR};
                double[] lejos = {3.5, 6.5, 3.9, 3.0, 9.0, 7.0};
                int[] dura = {50, 50, 60, 60, 70, 90};
                for (int i = 0; i < golpes.length; i++) {
                    victima.setHealth(victima.getMaxHealth());
                    victima.invulnerableTime = 0;
                    victima.hurtTime = 0;
                    victima.removeAllEffects();
                    victima.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    victima.moveTo(x0, y0, z0 + lejos[i], 180f, 0f);
                    victima.setOnGround(true);
                    int cad = al.cadenazos(), lat = al.latigazos(), arr = al.arrastrados(), est = al.estrellas(), ond = al.ondas(), tor = al.torbellinos(),
                            sal = al.saltos(), cai = al.caidas(), ons = al.ondasSalto(), enc = al.encadenados(), mar = al.marcadas(), bro = al.brotes(),
                            suj = al.sujetos();
                    al.moveTo(x0, y0, z0, 0f, 0f);
                    al.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    al.setTarget(victima);
                    al.setYRot(0f);
                    al.yBodyRot = al.yHeadRot = 0f;
                    al.empezar(acc[i]);
                    float antes = victima.getHealth();
                    boolean frenado = false;
                    for (int k = 0; k < dura[i]; k++) {
                        boolean el = al.accion() != 0 || al.marcasPendientes() > 0;
                        if (el) {
                            al.setTarget(victima);
                            al.tickCount++;
                            al.tick();
                            // (el salto le lleva: no se le devuelve a su sitio)
                            if (acc[i] != com.darkcraft.enemigos.AldricAnims.SALTO) al.moveTo(x0, y0, z0, 0f, 0f);
                        }
                        moverPresa(victima);
                        victima.setOnGround(true);
                        victima.invulnerableTime = 0;
                        var lento = victima.getEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
                        if (lento != null && lento.getAmplifier() >= 5) frenado = true;
                        if (!el && k > 2) break;
                    }
                    sg.append(' ').append(golpes[i]).append(String.format(java.util.Locale.ROOT, " a %.1f: -%.1f", lejos[i], antes - victima.getHealth()));
                    switch (i) {
                        case 0 -> sg.append(", cadenazos +").append(al.cadenazos() - cad);
                        case 1 -> sg.append(", latigazos +").append(al.latigazos() - lat).append(" (arrastra +").append(al.arrastrados() - arr)
                                .append(String.format(java.util.Locale.ROOT, ": la presa queda a %.1f)", Math.hypot(victima.getX() - x0, victima.getZ() - z0)));
                        case 2 -> sg.append(", estrellas +").append(al.estrellas() - est).append(" (ondas que dan +").append(al.ondas() - ond).append(')');
                        case 3 -> sg.append(", torbellinos +").append(al.torbellinos() - tor).append(" (dos vueltas)");
                        case 4 -> sg.append(", saltos +").append(al.saltos() - sal).append(" (cae +").append(al.caidas() - cai).append(", ondas que dan +")
                                .append(al.ondasSalto() - ons).append(String.format(java.util.Locale.ROOT, ", queda a %.1f de la presa)",
                                        Math.hypot(al.getX() - victima.getX(), al.getZ() - victima.getZ())));
                        case 5 -> sg.append(", encadena +").append(al.encadenados() - enc).append(" (marcas +").append(al.marcadas() - mar).append(", brotan +")
                                .append(al.brotes() - bro).append(", sujetan +").append(al.sujetos() - suj).append(", la presa sujeta ").append(frenado).append(')');
                        default -> {
                        }
                    }
                    sg.append(';');
                }
                sg.append(" en total: ").append(al.estado());
            }
            log.println(sg);
            // ---- a media vida: le arde la marca (el estallido) y su reflejo repite el golpe
            StringBuilder sm = new StringBuilder("Morvain, Aldric a media vida:");
            al.setHealth(al.getMaxHealth());
            al.moveTo(x0, y0, z0, 0f, 0f);
            if (victima != null) {
                victima.moveTo(x0, y0, z0 + 3.2, 180f, 0f);
                victima.setHealth(victima.getMaxHealth());
                al.setTarget(victima);
            }
            int est0 = al.estallidos();
            al.mediaVida();
            boolean marca = al.accion() == com.darkcraft.enemigos.AldricAnims.MARCA;
            for (int k = 0; k < 70 && al.accion() != 0; k++) {
                al.tickCount++;
                al.tick();
                al.moveTo(x0, y0, z0, 0f, 0f);
                if (victima != null) {
                    victima.moveTo(x0, y0, z0 + 3.2, 180f, 0f);
                    victima.setOnGround(true);
                    victima.invulnerableTime = 0;
                    victima.clearFire();
                }
            }
            sm.append(" segunda ").append(al.segunda()).append(", la marca ").append(marca).append(" (estalla +").append(al.estallidos() - est0)
                    .append("), le arde ").append(al.arde());
            // el reflejo: el cadenazo, repetido desde donde lo dio
            int ref0 = al.reflejos(), dan0 = com.darkcraft.enemigos.Aldric.REFLEJOS_DAN;
            com.darkcraft.enemigos.Aldric sombra = null;
            boolean seLePega = true, seCoge = true;
            int seVaEn = -1;
            if (victima != null) {
                al.setTarget(victima);
                al.setYRot(0f);
                al.yBodyRot = al.yHeadRot = 0f;
                al.empezar(com.darkcraft.enemigos.AldricAnims.CADENAZO);
                for (int k = 0; k < 140; k++) {
                    if (al.accion() != 0) {
                        al.tickCount++;
                        al.tick();
                        al.moveTo(x0, y0, z0, 0f, 0f);
                    } else {
                        // (sin presa: el reflejo sale igual, a su hora)
                        al.tickCount++;
                        al.tick();
                    }
                    victima.moveTo(x0, y0, z0 + 3.2, 180f, 0f);
                    victima.setOnGround(true);
                    victima.invulnerableTime = 0;
                    tickAldric(m, caja, quitar);
                    if (sombra == null)
                        for (var r : m.getEntitiesOfClass(com.darkcraft.enemigos.Aldric.class, caja)) if (r.reflejo()) sombra = r;
                    if (sombra != null && seLePega) {
                        seLePega = sombra.hurt(m.damageSources().playerAttack(fp), 10f);
                        seCoge = sombra.isPickable();
                    }
                    if (sombra != null && sombra.isRemoved() && seVaEn < 0) {
                        seVaEn = k;
                        break;
                    }
                }
            }
            sm.append("; reflejos +").append(al.reflejos() - ref0).append(" (dan +").append(com.darkcraft.enemigos.Aldric.REFLEJOS_DAN - dan0).append(", se le pega ")
                    .append(seLePega).append(", se le apunta ").append(seCoge).append(", tienen que ser false; se deshace en el tick ").append(seVaEn).append("); ")
                    .append(al.estado());
            log.println(sm);
            // ---- cae: de rodillas, mira a la ciudadela, se deshace en sombra; la puerta se abre; los marcados de rodillas y los rendidos
            // descansan y los de rodillas ya no vuelven
            al.quieta = true;
            for (int k = 0; k < 120 && al.accion() != 0; k++) {
                al.tickCount++;
                al.tick();
            }
            // los marcados del principio (siguen fuera): los de rodillas en su puesto y tres de pie, rendidos (al acabar la pelea de
            // antes sin caer, los solto: se rinden otra vez y acaban de arrodillarse)
            java.util.List<com.darkcraft.enemigos.Marcado> rezan = new java.util.ArrayList<>(), rinden = new java.util.ArrayList<>();
            for (var mk : marcados) {
                if (mk.isRemoved() || !mk.isAlive()) continue;
                if (com.darkcraft.morvain.MorvainEnemigos.cerco().get(mk.puesto()).postura() == com.darkcraft.enemigos.Enemigo.REZA) rezan.add(mk);
                else if (rinden.size() < 3 && com.darkcraft.morvain.MorvainEnemigos.cerco().get(mk.puesto()).postura() == com.darkcraft.enemigos.Enemigo.DE_PIE
                        && !"en_alto".equals(com.darkcraft.morvain.MorvainEnemigos.cerco().get(mk.puesto()).grupo())) rinden.add(mk);
            }
            for (var mk : rinden) {
                mk.rendir(true, al);
                for (int k = 0; k < 80 && (mk.accion() != 0 || mk.postura() == com.darkcraft.enemigos.Enemigo.DE_PIE); k++) {
                    mk.tickCount++;
                    mk.tick();
                }
            }
            for (var mk : rezan) mk.setTarget(null);
            int desc = com.darkcraft.morvain.Cerco.DESCANSAN, abiertas0 = com.darkcraft.morvain.Cerco.ABIERTOS;
            boolean pazAntes = com.darkcraft.morvain.Cerco.enPaz(server);
            // la puerta, cerrada mientras pelea y mientras dice su ultima frase
            al.setTarget(fp);
            com.darkcraft.morvain.Cerco.tick(m);
            boolean cerradaAntes = com.darkcraft.morvain.Cerco.sellado(m);
            al.moveTo(CX + 0.5, PISO, CZ + 18.5, 180f, 0f);
            al.invulnerableTime = 0;
            al.hurt(m.damageSources().playerAttack(fp), 99999f);
            boolean muere = !al.isAlive();
            int seVa = -1;
            boolean cerradaHablando = false;
            for (int fin = 0; fin < 300; fin++) {
                if (!al.isRemoved()) {
                    al.tickCount++;
                    al.tick();
                }
                com.darkcraft.morvain.Cerco.tick(m);
                if (fin == 60) cerradaHablando = com.darkcraft.morvain.Cerco.sellado(m);
                if (seVa < 0 && al.isRemoved()) {
                    seVa = fin;
                    break;
                }
            }
            // (el puesto se da por caido al morir: lo hace Puestos; aqui, por si la prueba no lo vio)
            com.darkcraft.enemigos.Puestos.marcar(server, sitio, com.darkcraft.morvain.MorvainEnemigos.aldric(), true);
            com.darkcraft.morvain.Cerco.tick(m);
            boolean abierta = !com.darkcraft.morvain.Cerco.sellado(m);
            com.darkcraft.morvain.Cerco.paz(m);
            int descansan = 0, descansanRendidos = 0;
            for (var e : rezan) if (e.isRemoved()) descansan++;
            for (var e : rinden) if (e.isRemoved()) descansanRendidos++;
            int objetos = m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, al.getBoundingBox().inflate(6)).size();
            // despues: los que rezaban ya no vuelven
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            com.darkcraft.enemigos.Puestos.marcar(server, sitio, com.darkcraft.morvain.MorvainEnemigos.aldric(), true);
            int salen = 0, vuelven = 0;
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio)) {
                salen++;
                if (com.darkcraft.morvain.MorvainEnemigos.cerco().get(e.puesto()).postura() == com.darkcraft.enemigos.Enemigo.REZA) vuelven++;
            }
            log.println("Morvain, Aldric cae: muere " + muere + " (en paz antes " + pazAntes + "); la puerta cerrada al pelear " + cerradaAntes + " y mientras habla "
                    + cerradaHablando + ", se deshace en sombra en " + seVa + ", y despues abierta " + abierta + " (se abre +" + (com.darkcraft.morvain.Cerco.ABIERTOS - abiertas0)
                    + "); objetos " + objetos + ", en paz " + com.darkcraft.morvain.Cerco.enPaz(server) + "; de los " + rezan.size() + " marcados que estaban de rodillas descansan "
                    + descansan + " y de los " + rinden.size() + " rendidos " + descansanRendidos + " (deshechos +" + (com.darkcraft.morvain.Cerco.DESCANSAN - desc)
                    + "); despues salen " + salen + " y de los de rodillas vuelven " + vuelven + " (tiene que ser 0); " + com.darkcraft.morvain.MorvainEnemigos.estadoAldric(server));
            for (var it : m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, al.getBoundingBox().inflate(8))) it.discard();
        } catch (Throwable t) {
            log.println("Morvain, Aldric: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] aldric", t);
        } finally {
            if (al != null) al.quieta = false;
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            for (var r : m.getEntitiesOfClass(com.darkcraft.enemigos.Aldric.class, caja)) if (r.reflejo()) r.discard();
            com.darkcraft.morvain.Cerco.abrir(m, false);
            // todo como antes: Aldric de rodillas en lo alto del ancla y los marcados del cerco, en su sitio
            com.darkcraft.enemigos.Puestos.marcar(server, sitio, com.darkcraft.morvain.MorvainEnemigos.aldric(), false);
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            try {
                com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
                log.println("Morvain, Aldric vuelve: " + com.darkcraft.morvain.MorvainEnemigos.estadoAldric(server));
            } catch (Throwable t) {
                log.println("Morvain, Aldric vuelve: ERROR " + t);
            }
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    /**
     * Parte 7n: el Patio del Juramento (a pie desde la plaza de la capital por la calle y el paso del rastrillo a todos sus
     * sitios, y con la reja de la torre bajada no se entra en la torre; el atril con el libro 23, el arcon, la reja, los dientes
     * del rastrillo, la piedra y los braseros) y el Condestable: de rodilla en el estrado; al verle habla, se levanta, el rastrillo
     * cae (y saca del paso a quien estuviera debajo) y ruge al acabar la frase; cada accion acaba; sus golpes a un marchito (el
     * tajo y su linea de fuego, que llega mas lejos que el montante; el barrido; la llamarada por la espalda; la embestida; la
     * brea que cae de los muros) y la embestida contra un muro (se queda aturdido); a media vida toca el cuerno y se alza la
     * guardia de la ceniza; el fuego no le hace nada y el frio mas; el juramento (no cae la primera vez: se alza con un tercio de
     * la vida y furia; jurando no le entra nada); y al caer de verdad: el rastrillo se alza, la reja de su torre tambien, la
     * Puerta Oeste se abre y su guardia ya no vuelve. Al acabar, todo como estaba.
     */
    private static void juramento(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.JURAMENTO, capital = com.darkcraft.morvain.MorvainEnemigos.CAPITAL;
        var J = com.darkcraft.morvain.Juramento.get();
        var C = com.darkcraft.morvain.Capital.get();
        final int CX = com.darkcraft.morvain.Juramento.CX, CZ = com.darkcraft.morvain.Juramento.CZ, PISO = com.darkcraft.morvain.Juramento.PISO;
        final int CU = com.darkcraft.morvain.Juramento.CENTRO_U;
        try {
            long t0 = System.currentTimeMillis();
            int nch = 0;
            for (long k : J.chunks()) {
                m.getChunk((int) (k >> 32), (int) k);
                nch++;
            }
            long ms = System.currentTimeMillis() - t0;
            var aire = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> {
                var st = m.getBlockState(new BlockPos(x, y, z));
                return st.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock ? aire : st;
            };
            int[] pl = C.sitio("plaza");
            var zona = com.darkcraft.pueblo.gen.Andar.desde(mundo, pl[0], pl[1], pl[2], CX + 70, CZ - 50, CX + 215, CZ + 50, 2_500_000);
            StringBuilder si = new StringBuilder(), no = new StringBuilder();
            for (var e : J.sitios().entrySet()) {
                int[] q = e.getValue();
                (zona.alcanza(q[0], q[1], q[2], 2) ? si : no).append(' ').append(e.getKey());
            }
            log.println("Morvain, el patio del Juramento: " + nch + " chunks en " + ms + " ms; " + com.darkcraft.morvain.Juramento.resumen(m)
                    + "; a pie desde la plaza (" + zona.celdas() + " celdas) a:" + si + "; no (la reja de la torre esta bajada):" + no);
            topView(m, CX + CU, CZ, 44, new File(out, "morvain_juramento.png"));
        } catch (Throwable t) {
            log.println("Morvain, el patio del Juramento: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] juramento", t);
        }
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        try {
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            com.darkcraft.enemigos.Condestable ko = null;
            int guardia = 0, enSuelo = 0, chocan = 0;
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            for (int k = 0; k < 6; k++)
                for (var e : fuera) if (e.isAlive() && !(e instanceof com.darkcraft.enemigos.Condestable)) e.tick();
            for (var e : fuera) {
                if (e instanceof com.darkcraft.enemigos.Condestable x) ko = x;
                else guardia++;
                if (!m.noCollision(e)) chocan++;
                if (!m.getBlockState(BlockPos.containing(e.getX(), e.getY() - 0.2, e.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty()) enSuelo++;
            }
            if (ko == null) {
                log.println("Morvain, el Condestable: NO SALE");
                return;
            }
            for (var e : fuera) if (e != ko) e.discard();
            StringBuilder sb = new StringBuilder("Morvain, el Condestable: en " + ko.blockPosition().toShortString() + " postura " + ko.postura()
                    + " (1 de rodilla), giro " + Math.round(ko.getYRot()) + ", vida " + Math.round(ko.getMaxHealth()) + ", caja " + ko.getBbWidth() + " x "
                    + ko.getBbHeight() + "; su guardia " + guardia + "; en su suelo " + enSuelo + " de " + fuera.size() + ", metidos en un bloque " + chocan
                    + " (tiene que ser 0)");
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("4a6b8c0d-2e3f-4a5b-9c7d-1e2f3a4b5c83"), "[DarkCraftCI8]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            // ---- le ve desde dentro del patio: habla, se levanta, cae el rastrillo (y saca del paso al que esta debajo) y ruge
            int[] an = J.sitio("ante_condestable");
            final double fxx = an[0] + 0.5, fyy = an[1], fzz = an[2] + 0.5;
            fp.moveTo(fxx, fyy, fzz, -90f, 0f);
            fp.setOnGround(true);
            var debajo = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
            if (debajo != null) {
                debajo.moveTo(CX + com.darkcraft.morvain.Juramento.RASTRILLO_U + 0.5, PISO, CZ + 0.5, -90f, 0f);
                debajo.setNoAi(true);
                m.addFreshEntity(debajo);
                quitar.add(debajo);
            }
            com.darkcraft.morvain.Juramento.alzarYa(m);
            int caidas = com.darkcraft.morvain.Juramento.CAIDAS;
            ko.setTarget(fp);
            int levanta = -1, ruge = -1, ataca = -1, quien = 0, bajado = -1;
            for (int k = 0; k < 360; k++) {
                ko.tickCount++;
                ko.tick();
                com.darkcraft.morvain.Juramento.tick(m);
                fp.moveTo(fxx, fyy, fzz, -90f, 0f);
                if (bajado < 0 && com.darkcraft.morvain.Juramento.rastrilloBajado(m)) bajado = k;
                int a = ko.accion();
                if (levanta < 0 && a == com.darkcraft.enemigos.CondestableAnims.LEVANTARSE) levanta = k;
                if (ruge < 0 && a == com.darkcraft.enemigos.CondestableAnims.GRITO) ruge = k;
                if (ruge >= 0 && ataca < 0 && a != 0 && a != com.darkcraft.enemigos.CondestableAnims.GRITO) {
                    ataca = k;
                    quien = a;
                    break;
                }
            }
            sb.append("; al verle: pelea ").append(ko.enPelea()).append(", se levanta en el tick ").append(levanta).append(", el rastrillo cae del todo en el ")
                    .append(bajado).append(" (caidas +").append(com.darkcraft.morvain.Juramento.CAIDAS - caidas).append("), el que estaba debajo ")
                    .append(debajo == null ? "?" : debajo.getX() > CX + com.darkcraft.morvain.Juramento.RASTRILLO_U + 1.2 ? "pasa dentro" : "SIGUE DEBAJO en " + debajo.getX())
                    .append(", ruge en el ").append(ruge).append(" (la frase 1 dura ").append(com.darkcraft.rey.Voces.ticks(com.darkcraft.enemigos.Condestable.VOZ_BASE + 1))
                    .append("), y luego ").append(quien == 0 ? "nada" : com.darkcraft.enemigos.CondestableAnims.NOMBRES[quien]).append(" en el ").append(ataca)
                    .append(String.format(java.util.Locale.ROOT, ", de pie %s a %.1f de su presa", ko.postura() == com.darkcraft.enemigos.Enemigo.DE_PIE,
                            Math.hypot(ko.getX() - fp.getX(), ko.getZ() - fp.getZ())));
            if (debajo != null) debajo.discard();
            // ---- cada accion acaba (sin presa)
            ko.setTarget(null);
            StringBuilder nn = new StringBuilder();
            for (int a = 1; a < com.darkcraft.enemigos.CondestableAnims.TICKS.length; a++) {
                String nombre = com.darkcraft.enemigos.CondestableAnims.NOMBRES[a];
                if (nombre.equals("morir")) continue;
                ko.moveTo(CX + CU + 0.5, PISO, CZ + 0.5, 90f, 0f);
                ko.empezar(a);
                for (int k = 0; k < 140 && ko.accion() == a; k++) {
                    ko.tickCount++;
                    ko.tick();
                }
                if (ko.accion() == a) nn.append(' ').append(nombre);
            }
            for (int k = 0; k < 40 && ko.pendientes() > 0; k++) {
                ko.tickCount++;
                ko.tick();
            }
            for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Cantaro.class, ko.getBoundingBox().inflate(40))) f.discard();
            for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Cantaro.Llamas.class, ko.getBoundingBox().inflate(40))) f.discard();
            sb.append("; acciones que no acaban:").append(nn.length() == 0 ? " ninguna" : nn);
            log.println(sb);
            // ---- sus golpes a un marchito, en el patio (el Condestable en el estrado mirando al oeste, a la puerta)
            StringBuilder sg = new StringBuilder("Morvain, los golpes del Condestable a un marchito:");
            float giro = 90f;
            double fx = -Math.sin(Math.toRadians(giro)), fz = Math.cos(Math.toRadians(giro));
            double y = PISO, x0 = CX + CU + 0.5, z0 = CZ + 0.5;
            var victima = DarkCraftMod.MARCHITO.get().create(m);
            if (victima != null) {
                victima.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(900.0);
                victima.moveTo(x0 + fx * 3, y, z0 + fz * 3, giro + 180f, 0f);
                m.addFreshEntity(victima);
                quitar.add(victima);
                String[] golpes = {"tajo de cerca", "tajo de lejos (la linea de fuego)", "barrido", "llamarada por la espalda", "embestida", "alzar (la brea)"};
                int[] acc = {com.darkcraft.enemigos.CondestableAnims.TAJO, com.darkcraft.enemigos.CondestableAnims.TAJO, com.darkcraft.enemigos.CondestableAnims.BARRIDO,
                        com.darkcraft.enemigos.CondestableAnims.LLAMARADA, com.darkcraft.enemigos.CondestableAnims.EMBESTIDA, com.darkcraft.enemigos.CondestableAnims.ALZAR};
                double[] lejos = {3.0, 9.5, 3.0, -2.4, 10.0, 8.0};
                for (int i = 0; i < golpes.length; i++) {
                    victima.setHealth(victima.getMaxHealth());
                    victima.invulnerableTime = 0;
                    victima.hurtTime = 0;
                    victima.clearFire();
                    victima.removeAllEffects();
                    double px = x0 + fx * lejos[i], pz = z0 + fz * lejos[i];
                    victima.moveTo(px, y, pz, giro + 180f, 0f);
                    int ll = ko.llamasQueDan(), an2 = ko.anillosQueDan(), em = ko.embestidasQueDan(), br = ko.brea(), rev = com.darkcraft.enemigos.Cantaro.REVIENTAN,
                            qu = com.darkcraft.enemigos.Cantaro.LLAMAS_QUEMAN;
                    ko.moveTo(x0, y, z0, giro, 0f);
                    ko.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    ko.setTarget(victima);
                    ko.setYRot(giro);
                    ko.yBodyRot = ko.yHeadRot = giro;
                    ko.empezar(acc[i]);
                    float antes = victima.getHealth();
                    int fuego = 0;
                    double recorre = 0;
                    for (int k = 0; k < 150 && (ko.accion() == acc[i] || ko.pendientes() > 0 || k < 60); k++) {
                        ko.tickCount++;
                        ko.tick();
                        if (acc[i] != com.darkcraft.enemigos.CondestableAnims.EMBESTIDA) ko.moveTo(x0, y, z0, giro, 0f);
                        else recorre = Math.max(recorre, Math.hypot(ko.getX() - x0, ko.getZ() - z0));
                        ko.setYRot(giro);
                        ko.yBodyRot = ko.yHeadRot = giro;
                        if (acc[i] != com.darkcraft.enemigos.CondestableAnims.EMBESTIDA) victima.moveTo(px, y, pz, giro + 180f, 0f);
                        victima.invulnerableTime = 0;
                        for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Cantaro.class, ko.getBoundingBox().inflate(40))) {
                            if (!quitar.contains(f)) quitar.add(f);
                            f.tick();
                        }
                        for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Cantaro.Llamas.class, ko.getBoundingBox().inflate(40))) {
                            if (!quitar.contains(f)) quitar.add(f);
                            f.tick();
                        }
                        fuego = Math.max(fuego, victima.getRemainingFireTicks());
                    }
                    sg.append(' ').append(golpes[i]).append(String.format(java.util.Locale.ROOT, " a %.1f: -%.1f, ardiendo %d", Math.abs(lejos[i]), antes - victima.getHealth(), fuego))
                            .append(victima.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN) ? ", frenado" : "")
                            .append(ko.llamasQueDan() > ll ? ", le da la linea de fuego" : "").append(ko.anillosQueDan() > an2 ? ", le da el anillo" : "")
                            .append(ko.embestidasQueDan() > em ? String.format(java.util.Locale.ROOT, ", le arrolla (corre %.1f)", recorre) : "")
                            .append(acc[i] == com.darkcraft.enemigos.CondestableAnims.ALZAR ? ", cantaros " + (ko.brea() - br) + " (revientan "
                                    + (com.darkcraft.enemigos.Cantaro.REVIENTAN - rev) + ", sus llamas queman " + (com.darkcraft.enemigos.Cantaro.LLAMAS_QUEMAN - qu) + ")" : "")
                            .append(';');
                    for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Cantaro.Llamas.class, ko.getBoundingBox().inflate(40))) f.discard();
                }
                sg.append(" en total: tajos ").append(ko.tajos()).append(", llamas ").append(ko.llamasHechas()).append(" (dan ").append(ko.llamasQueDan())
                        .append("), barridos ").append(ko.barridos()).append(", llamaradas ").append(ko.llamaradas()).append(" (dan ").append(ko.anillosQueDan())
                        .append("), embestidas ").append(ko.embestidas()).append(" (dan ").append(ko.embestidasQueDan()).append("), alzar ").append(ko.alzados())
                        .append(" (brea ").append(ko.brea()).append(')');
                victima.discard();
            }
            // ---- la embestida contra el muro del norte: se estrella y se queda aturdido
            ko.setTarget(null);
            ko.moveTo(CX + CU + 0.5, PISO, CZ - com.darkcraft.morvain.Juramento.PATIO_V + 6.5, 180f, 0f);
            ko.setYRot(180f);
            ko.yBodyRot = ko.yHeadRot = 180f;
            int choques = ko.choques();
            ko.empezar(com.darkcraft.enemigos.CondestableAnims.EMBESTIDA);
            boolean aturdido = false;
            for (int k = 0; k < 60; k++) {
                ko.tickCount++;
                ko.tick();
                ko.setYRot(180f);
                aturdido |= ko.aturdido();
            }
            sg.append("; contra el muro: se estrella ").append(ko.choques() > choques).append(", aturdido ").append(aturdido);
            log.println(sg);
            // ---- a media vida: el cuerno y la guardia que se alza de los montones de ceniza
            StringBuilder sm = new StringBuilder("Morvain, el Condestable a media vida:");
            ko.moveTo(CX + CU + 0.5, PISO, CZ + 0.5, 90f, 0f);
            fp.moveTo(fxx, fyy, fzz, -90f, 0f);
            ko.setTarget(fp);
            ko.mediaVida();
            for (int k = 0; k < 40 && ko.guardiasAlzados() == 0; k++) {
                ko.tickCount++;
                ko.tick();
                fp.moveTo(fxx, fyy, fzz, -90f, 0f);
            }
            int bien = 0, conPresa = 0, guardias = 0;
            for (var c : m.getEntitiesOfClass(com.darkcraft.enemigos.Calcinado.class, ko.getBoundingBox().inflate(30))) {
                if (!c.esEfimero()) continue;
                quitar.add(c);
                if (c.variante() == com.darkcraft.enemigos.Calcinado.GUARDIA) guardias++;
                boolean pisa = !m.getBlockState(BlockPos.containing(c.getX(), c.getY() - 0.2, c.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty();
                if (pisa && m.noCollision(c)) bien++;
                if (c.getTarget() == fp) conPresa++;
            }
            sm.append(" segunda ").append(ko.segunda()).append(", se alzan ").append(ko.guardiasAlzados()).append(" (guardias ").append(guardias)
                    .append(", en su suelo y sin meterse en nada ").append(bien).append(", con presa ").append(conPresa).append("), en pie ").append(ko.suyos());
            // ---- el fuego no le hace nada; el frio, un 60 % mas que un golpe igual
            ko.invulnerableTime = 0;
            float v0 = ko.getHealth();
            boolean fuegoLe = ko.hurt(m.damageSources().inFire(), 10f);
            ko.invulnerableTime = 0;
            float v1 = ko.getHealth();
            ko.hurt(m.damageSources().generic(), 10f);
            ko.invulnerableTime = 0;
            float v2 = ko.getHealth();
            ko.hurt(m.damageSources().freeze(), 10f);
            ko.invulnerableTime = 0;
            sm.append(String.format(java.util.Locale.ROOT, "; el fuego le hace %s (-%.1f); un golpe de 10, -%.1f; el frio de 10, -%.1f", fuegoLe ? "dano" : "nada", v0 - v1,
                    v1 - v2, v2 - ko.getHealth()));
            log.println(sm);
            // ---- el juramento: el golpe de muerte no le mata la primera vez; jurando no le entra nada; se alza con un tercio
            StringBuilder sj = new StringBuilder("Morvain, el juramento del Condestable:");
            ko.setHealth(20f);
            ko.invulnerableTime = 0;
            ko.hurt(m.damageSources().playerAttack(fp), 500f);
            sj.append(String.format(java.util.Locale.ROOT, " tras un golpe de muerte: vivo %s, vida %.0f, jura %s (juramentos %d)", ko.isAlive(), ko.getHealth(),
                    ko.accion() == com.darkcraft.enemigos.CondestableAnims.JURAMENTO, ko.juramentos()));
            ko.invulnerableTime = 0;
            boolean entra = ko.hurt(m.damageSources().playerAttack(fp), 50f);
            sj.append(", jurando un golpe de 50 ").append(entra ? "ENTRA" : "no entra");
            int k = 0;
            for (; k < 120 && ko.accion() == com.darkcraft.enemigos.CondestableAnims.JURAMENTO; k++) {
                ko.tickCount++;
                ko.tick();
                fp.moveTo(fxx, fyy, fzz, -90f, 0f);
            }
            sj.append(String.format(java.util.Locale.ROOT, "; se alza en %d ticks con %.0f de %.0f (%.0f %%), furia %s", k, ko.getHealth(), ko.getMaxHealth(),
                    100f * ko.getHealth() / ko.getMaxHealth(), ko.furia()));
            log.println(sj);
            // ---- cae de verdad: el rastrillo se alza, la reja de su torre tambien, la Puerta Oeste se abre y su guardia descansa
            com.darkcraft.enemigos.Puestos.todos(server, capital, false);
            int deLaPuerta = 0;
            java.util.List<com.darkcraft.enemigos.Enemigo> puerta = new java.util.ArrayList<>();
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, capital))
                if (com.darkcraft.morvain.Juramento.DESCANSAN.contains(com.darkcraft.morvain.MorvainEnemigos.capital().get(e.puesto()).grupo())) {
                    deLaPuerta++;
                    puerta.add(e);
                }
            int barrotes = com.darkcraft.morvain.Juramento.barrotesPuertaOeste(m), alzados = com.darkcraft.morvain.Juramento.ALZADOS;
            ko.setTarget(null);
            ko.invulnerableTime = 0;
            ko.hurt(m.damageSources().playerAttack(fp), 99999f);
            boolean bajadoAlCaer = com.darkcraft.morvain.Juramento.rastrilloBajado(m);
            int fin = 0;
            for (; fin < 260; fin++) {
                if (!ko.isRemoved()) {
                    ko.tickCount++;
                    ko.tick();
                }
                com.darkcraft.morvain.Juramento.tick(m);
                if (ko.isRemoved() && com.darkcraft.morvain.Juramento.ALZADOS > alzados) break;
            }
            com.darkcraft.morvain.Juramento.paz(m);
            int descansan = 0;
            for (var e : puerta) if (e.isRemoved()) descansan++;
            int objetos = m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, ko.getBoundingBox().inflate(6)).size();
            com.darkcraft.enemigos.Puestos.todos(server, capital, false);
            int vuelven = 0;
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, capital))
                if (com.darkcraft.morvain.Juramento.DESCANSAN.contains(com.darkcraft.morvain.MorvainEnemigos.capital().get(e.puesto()).grupo())) vuelven++;
            log.println("Morvain, el Condestable cae: el rastrillo bajado al caer " + bajadoAlCaer + ", alzado del todo en " + fin + " ticks (alzados +"
                    + (com.darkcraft.morvain.Juramento.ALZADOS - alzados) + "), vivo " + ko.isAlive() + ", objetos " + objetos + ", en paz "
                    + com.darkcraft.morvain.Juramento.enPaz(server) + ", su guardia en pie " + ko.suyos() + "; la Puerta Oeste: de " + barrotes
                    + " barrotes quedan " + com.darkcraft.morvain.Juramento.barrotesPuertaOeste(m) + "; de los " + deLaPuerta + " de la puerta descansan " + descansan
                    + ", despues vuelven " + vuelven + " (tiene que ser 0); " + com.darkcraft.morvain.MorvainEnemigos.estadoCondestable(server));
            for (var it : m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, ko.getBoundingBox().inflate(8))) it.discard();
        } catch (Throwable t) {
            log.println("Morvain, el Condestable: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] condestable", t);
        } finally {
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            for (var e : m.getEntitiesOfClass(com.darkcraft.enemigos.Calcinado.class, new net.minecraft.world.phys.AABB(CX + 110, PISO - 10, CZ - 40, CX + 205, PISO + 30, CZ + 40)))
                if (e.esEfimero()) e.discard();
            for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Cantaro.class, new net.minecraft.world.phys.AABB(CX + 110, PISO - 10, CZ - 40, CX + 205, PISO + 30, CZ + 40)))
                f.discard();
            // todo como antes: el Condestable de rodilla en su estrado, el rastrillo subido, la reja y la Puerta Oeste como estaban
            com.darkcraft.enemigos.Puestos.marcar(server, sitio, com.darkcraft.morvain.MorvainEnemigos.condestable(), false);
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            com.darkcraft.enemigos.Puestos.todos(server, capital, false);
            try {
                com.darkcraft.morvain.Juramento.deshacerPaz(m);
                com.darkcraft.morvain.Juramento.alzarYa(m);
                com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
                log.println("Morvain, el Condestable vuelve: " + com.darkcraft.morvain.MorvainEnemigos.estadoCondestable(server));
            } catch (Throwable t) {
                log.println("Morvain, el Condestable vuelve: ERROR " + t);
            }
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    /**
     * Parte 7l: la escalera de los penitentes y la ermita de la cumbre (los braseros encendidos, el arcon, la campana, la rosa
     * de hielo, la cruz; la escalera, escalon a escalon, y a pie del postigo del monasterio a todos sus sitios), sus helados
     * (de rodillas en los rellanos de la escalera y enterrados junto al calvario) y Hrodric, el Abad de la Cumbre: de rodillas en
     * medio del calvario; al ver a alguien habla, se levanta y ruge al acabar la frase; cada accion acaba; sus golpes a un
     * marchito (el baculazo y su fila de puas, que llega mas lejos que el baculo; el barrido; el pisoton; el aliento que hiela y
     * frena; los carambanos que caen donde esta); la ventisca: lejos de los braseros se hiela y duele, junto a uno se deshiela;
     * a media vida llama a sus hermanos (se alzan de la nieve) y levanta la ventisca; el frio no le hace nada y el fuego, mas; y
     * al caer, la cumbre en paz: los hermanos que rezaban en la iglesia del monasterio descansan y ya no vuelven.
     */
    private static void ermita(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String escalera = com.darkcraft.morvain.MorvainEnemigos.ERMITA, cumbre = com.darkcraft.morvain.MorvainEnemigos.CUMBRE,
                monasterio = com.darkcraft.morvain.MorvainEnemigos.MONASTERIO;
        var E = com.darkcraft.morvain.Ermita.get();
        final int CX = com.darkcraft.morvain.Ermita.CX, CZ = com.darkcraft.morvain.Ermita.CZ, PISO = com.darkcraft.morvain.Ermita.PISO;
        final int EX = com.darkcraft.morvain.Ermita.ESC_X, EZ0 = com.darkcraft.morvain.Ermita.ESC_Z0, EZ1 = com.darkcraft.morvain.Ermita.ESC_Z1;
        try {
            long t0 = System.currentTimeMillis();
            int nch = 0;
            for (long k : E.chunks()) {
                m.getChunk((int) (k >> 32), (int) k);
                nch++;
            }
            long ms = System.currentTimeMillis() - t0;
            // la escalera, escalon a escalon (los tres de ancho): se pisa y encima hay hueco (tres de alto)
            int malos = 0, subida = 0;
            StringBuilder raros = new StringBuilder();
            for (int z = EZ1; z <= EZ0; z++) {
                int h = com.darkcraft.morvain.Ermita.escalon(z);
                if (z > EZ1) subida = Math.max(subida, Math.abs(h - com.darkcraft.morvain.Ermita.escalon(z - 1)));
                for (int dx = -1; dx <= 1; dx++) {
                    BlockPos q = new BlockPos(EX + dx, h, z);
                    boolean bien = !m.getBlockState(q.below()).getCollisionShape(m, q.below()).isEmpty();
                    // (encima, hueco: aire o, como mucho, un poco de nieve)
                    for (int dy = 0; dy < 3; dy++) {
                        var st = m.getBlockState(q.above(dy));
                        bien &= st.getCollisionShape(m, q.above(dy)).isEmpty() || dy == 0 && st.is(net.minecraft.world.level.block.Blocks.SNOW);
                    }
                    if (!bien) {
                        malos++;
                        if (raros.length() < 300) raros.append(' ').append(q.toShortString()).append(';');
                    }
                }
            }
            // a pie desde el postigo del huerto del monasterio
            com.darkcraft.pueblo.gen.Andar.Mundo mundo = (x, y, z) -> m.getBlockState(new BlockPos(x, y, z));
            int[] po = E.sitio("postigo");
            var zona = com.darkcraft.pueblo.gen.Andar.desde(mundo, po[0], po[1], po[2], com.darkcraft.morvain.Ermita.X0, com.darkcraft.morvain.Ermita.Z0,
                    com.darkcraft.morvain.Ermita.X1, EZ0 + 6, 600000);
            StringBuilder ap = new StringBuilder();
            int llega = 0, sitios = 0;
            for (var en : E.sitios().entrySet()) {
                if (en.getKey().equals("cruz_alta")) continue;
                sitios++;
                int[] q = en.getValue();
                if (zona.alcanza(q[0], q[1], q[2], 2)) llega++;
                else ap.append(' ').append(en.getKey());
            }
            log.println("Morvain, la ermita de la cumbre: " + nch + " chunks en " + ms + " ms; " + com.darkcraft.morvain.Ermita.resumen(m)
                    + "; la escalera: " + (EZ0 - EZ1 + 1) + " filas de Y " + com.darkcraft.morvain.Ermita.escalon(EZ0) + " a " + com.darkcraft.morvain.Ermita.escalon(EZ1)
                    + ", el escalon mas alto " + subida + " (tiene que ser 1), escalones sin pisar o sin hueco " + malos + raros
                    + "; a pie desde el postigo (" + zona.celdas() + " celdas) a " + llega + " de " + sitios + " sitios" + (ap.length() == 0 ? "" : ", no a:" + ap));
            topView(m, CX, CZ - 12, 46, new File(out, "morvain_ermita.png"));
            topView(m, EX, (EZ0 + EZ1) / 2, 62, new File(out, "morvain_escalera.png"));
        } catch (Throwable t) {
            log.println("Morvain, la ermita de la cumbre: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] ermita", t);
        }
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        try {
            // ---- los helados de la escalera: de rodillas en los rellanos, enterrados junto al calvario
            com.darkcraft.enemigos.Puestos.todos(server, escalera, false);
            var suyos = com.darkcraft.enemigos.Puestos.sacarTodos(m, escalera);
            for (int k = 0; k < 10; k++)
                for (var e : suyos) if (e.isAlive()) e.tick();
            int rezan = 0, enterrados = 0, enSuelo = 0, chocan = 0;
            StringBuilder mal = new StringBuilder();
            for (var e : suyos) {
                if (!m.noCollision(e)) {
                    chocan++;
                    if (mal.length() < 300) mal.append(" metido en un bloque en ").append(e.blockPosition().toShortString()).append(';');
                }
                if (e.postura() == com.darkcraft.enemigos.Enemigo.REZA) rezan++;
                else if (e.postura() == com.darkcraft.enemigos.Enemigo.TUMBADO) enterrados++;
                if (!m.getBlockState(BlockPos.containing(e.getX(), e.getY() - 0.2, e.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty()) enSuelo++;
                else if (mal.length() < 400) mal.append(' ').append(e.tipo()).append(" en el aire en ").append(e.blockPosition().toShortString()).append(';');
            }
            log.println("Morvain, la escalera de los penitentes: fuera " + suyos.size() + " de " + com.darkcraft.morvain.MorvainEnemigos.ermita().size()
                    + ": de rodillas " + rezan + ", enterrados " + enterrados + " (en su suelo " + enSuelo + "); metidos en un bloque " + chocan + " (tiene que ser 0)" + mal);
            com.darkcraft.enemigos.Puestos.todos(server, escalera, false);
            // ---- el Abad, de rodillas en medio del calvario
            com.darkcraft.enemigos.Puestos.todos(server, cumbre, false);
            com.darkcraft.enemigos.Abad ab = null;
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, cumbre)) if (e instanceof com.darkcraft.enemigos.Abad x) ab = x;
            if (ab == null) {
                log.println("Morvain, el Abad: NO SALE");
                return;
            }
            boolean suelo = !m.getBlockState(BlockPos.containing(ab.getX(), ab.getY() - 0.2, ab.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty();
            StringBuilder sb = new StringBuilder("Morvain, el Abad: en " + ab.blockPosition().toShortString() + " postura " + ab.postura() + " (1 de rodillas), giro "
                    + Math.round(ab.getYRot()) + ", en el suelo " + suelo + ", metido en un bloque " + !m.noCollision(ab) + ", vida " + Math.round(ab.getMaxHealth())
                    + ", caja " + ab.getBbWidth() + " x " + ab.getBbHeight() + ", al calor de un brasero en el centro " + com.darkcraft.enemigos.Abad.alCalor(CX + 0.5, PISO, CZ + 0.5));
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("9c4d2e1f-5a6b-4c7d-8e9f-1a2b3c4d5e71"), "[DarkCraftCI6]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            // ---- le ve desde la entrada del calvario: habla, se levanta y ruge al acabar la frase
            int[] an = E.sitio("ante_abad");
            final double fxx = an[0] + 0.5, fyy = an[1], fzz = an[2] + 0.5;
            fp.moveTo(fxx, fyy, fzz, 180f, 0f);
            fp.setOnGround(true);
            ab.setTarget(fp);
            int levanta = -1, ruge = -1, ataca = -1, quien = 0;
            for (int k = 0; k < 360; k++) {
                ab.tickCount++;
                ab.tick();
                fp.moveTo(fxx, fyy, fzz, 180f, 0f);
                int a = ab.accion();
                if (levanta < 0 && a == com.darkcraft.enemigos.AbadAnims.LEVANTARSE) levanta = k;
                if (ruge < 0 && a == com.darkcraft.enemigos.AbadAnims.GRITO) ruge = k;
                if (ruge >= 0 && ataca < 0 && a != 0 && a != com.darkcraft.enemigos.AbadAnims.GRITO) {
                    ataca = k;
                    quien = a;
                    break;
                }
            }
            sb.append("; al verle: pelea ").append(ab.enPelea()).append(", se levanta en el tick ").append(levanta).append(", ruge en el ").append(ruge)
                    .append(" (la frase 1 dura ").append(com.darkcraft.rey.Voces.ticks(com.darkcraft.enemigos.Abad.VOZ_BASE + 1)).append("), y luego ")
                    .append(quien == 0 ? "nada" : com.darkcraft.enemigos.AbadAnims.NOMBRES[quien]).append(" en el ").append(ataca)
                    .append(String.format(java.util.Locale.ROOT, ", de pie %s a %.1f de su presa", ab.postura() == com.darkcraft.enemigos.Enemigo.DE_PIE,
                            Math.hypot(ab.getX() - fp.getX(), ab.getZ() - fp.getZ())));
            // ---- cada accion acaba (sin presa)
            ab.setTarget(null);
            StringBuilder no = new StringBuilder();
            for (int a = 1; a < com.darkcraft.enemigos.AbadAnims.TICKS.length; a++) {
                String nombre = com.darkcraft.enemigos.AbadAnims.NOMBRES[a];
                if (nombre.equals("morir")) continue;
                ab.empezar(a);
                for (int k = 0; k < 140 && ab.accion() == a; k++) {
                    ab.tickCount++;
                    ab.tick();
                }
                if (ab.accion() == a) no.append(' ').append(nombre);
            }
            for (int k = 0; k < 40 && ab.pendientes() > 0; k++) {
                ab.tickCount++;
                ab.tick();
            }
            sb.append("; acciones que no acaban:").append(no.length() == 0 ? " ninguna" : no);
            log.println(sb);
            // ---- sus golpes a un marchito, en el calvario (el Abad mirando al sur, hacia la entrada)
            StringBuilder sg = new StringBuilder("Morvain, los golpes del Abad a un marchito:");
            float giro = 0f;
            double fx = -Math.sin(Math.toRadians(giro)), fz = Math.cos(Math.toRadians(giro));
            double y = PISO, x0 = CX + 0.5, z0 = CZ + 2.5;
            var victima = DarkCraftMod.MARCHITO.get().create(m);
            if (victima != null) {
                victima.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(600.0);
                victima.moveTo(x0 + fx * 3, y, z0 + fz * 3, giro + 180f, 0f);
                m.addFreshEntity(victima);
                quitar.add(victima);
                // {accion, a cuanto delante (o detras, negativo)}
                String[] golpes = {"baculazo de cerca", "baculazo de lejos (las puas)", "barrido", "pisoton por la espalda", "aliento", "carambanos"};
                int[] acc = {com.darkcraft.enemigos.AbadAnims.BACULAZO, com.darkcraft.enemigos.AbadAnims.BACULAZO, com.darkcraft.enemigos.AbadAnims.BARRIDO,
                        com.darkcraft.enemigos.AbadAnims.PISOTON, com.darkcraft.enemigos.AbadAnims.ALIENTO, com.darkcraft.enemigos.AbadAnims.CARAMBANOS};
                double[] lejos = {3.0, 8.1, 3.0, -2.6, 6.0, 7.0};
                for (int i = 0; i < golpes.length; i++) {
                    victima.setHealth(victima.getMaxHealth());
                    victima.invulnerableTime = 0;
                    victima.hurtTime = 0;
                    victima.setTicksFrozen(0);
                    victima.removeAllEffects();
                    double px = x0 + fx * lejos[i], pz = z0 + fz * lejos[i];
                    victima.moveTo(px, y, pz, giro + 180f, 0f);
                    int pu = ab.puasQueDan(), ca = ab.carambanosQueDan(), al = ab.heladosAliento();
                    ab.moveTo(x0, y, z0, giro, 0f);
                    ab.setTarget(victima);
                    ab.setYRot(giro);
                    ab.yBodyRot = ab.yHeadRot = giro;
                    ab.empezar(acc[i]);
                    float antes = victima.getHealth();
                    int frio = 0;
                    for (int k = 0; k < 140 && (ab.accion() == acc[i] || ab.pendientes() > 0); k++) {
                        ab.tickCount++;
                        ab.tick();
                        ab.moveTo(x0, y, z0, giro, 0f);
                        ab.setYRot(giro);
                        ab.yBodyRot = ab.yHeadRot = giro;
                        victima.moveTo(px, y, pz, giro + 180f, 0f);
                        victima.invulnerableTime = 0;
                        frio = Math.max(frio, victima.getTicksFrozen());
                    }
                    sg.append(' ').append(golpes[i]).append(String.format(java.util.Locale.ROOT, " a %.1f: -%.1f, helado %d", Math.abs(lejos[i]), antes - victima.getHealth(), frio))
                            .append(victima.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN) ? ", frenado" : "")
                            .append(ab.puasQueDan() > pu ? ", le da una pua" : "").append(ab.carambanosQueDan() > ca ? ", le cae un carambano" : "")
                            .append(ab.heladosAliento() > al ? ", le hiela el aliento" : "").append(';');
                }
                sg.append(" en total: baculazos ").append(ab.baculazos()).append(", puas ").append(ab.puas()).append(" (dan ").append(ab.puasQueDan())
                        .append("), pisotones ").append(ab.pisotones()).append(", alientos ").append(ab.alientos()).append(", carambanos ").append(ab.carambanos())
                        .append(" (dan ").append(ab.carambanosQueDan()).append(')');
                victima.discard();
            }
            log.println(sg);
            // ---- la ventisca: lejos de los braseros se hiela (y le duele); junto a uno, se deshiela
            StringBuilder sv = new StringBuilder("Morvain, la ventisca del Abad:");
            var lejosV = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
            var calorV = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
            int[] br = E.sitio("brasero_0");
            if (lejosV != null && calorV != null) {
                lejosV.moveTo(CX + 0.5, PISO, CZ + 6.5, 0f, 0f);
                calorV.moveTo(br[0] + 0.5, br[1], br[2] + 0.5, 0f, 0f);
                lejosV.setNoAi(true);
                calorV.setNoAi(true);
                m.addFreshEntity(lejosV);
                m.addFreshEntity(calorV);
                quitar.add(lejosV);
                quitar.add(calorV);
                calorV.setTicksFrozen(120);
                float vida = lejosV.getHealth();
                int hel = ab.heladosVentisca(), des = ab.deshelados();
                ab.ventisca(true);
                boolean hay = ab.ventisca();
                int heladoEn = -1;
                for (int k = 0; k < 90; k++) {
                    ab.tickCount++;
                    ab.ventiscaSobre(lejosV);
                    ab.ventiscaSobre(calorV);
                    lejosV.invulnerableTime = 0;
                    if (heladoEn < 0 && lejosV.isFullyFrozen()) heladoEn = k;
                }
                sv.append(" empieza ").append(hay).append(String.format(java.util.Locale.ROOT,
                        "; lejos de los braseros: helado del todo en el tick %d, -%.1f de vida (le duele %d veces); junto al brasero (al calor %s): de 120 a %d de frio, se deshiela %d veces",
                        heladoEn, vida - lejosV.getHealth(), ab.heladosVentisca() - hel, com.darkcraft.enemigos.Abad.alCalor(calorV.getX(), calorV.getY(), calorV.getZ()),
                        calorV.getTicksFrozen(), ab.deshelados() - des));
                ab.ventisca(false);
                sv.append("; se acaba ").append(!ab.ventisca());
            }
            log.println(sv);
            // ---- a media vida: grita, se alzan sus hermanos de la nieve del calvario y se levanta la ventisca
            StringBuilder sm = new StringBuilder("Morvain, el Abad a media vida:");
            ab.moveTo(CX + 0.5, PISO, CZ + 0.5, 0f, 0f);
            fp.moveTo(fxx, fyy, fzz, 180f, 0f);
            ab.setTarget(fp);
            int vent = ab.ventiscas();
            ab.mediaVida();
            for (int k = 0; k < 40 && ab.alzados() == 0; k++) {
                ab.tickCount++;
                ab.tick();
                fp.moveTo(fxx, fyy, fzz, 180f, 0f);
            }
            int bien = 0, conPresa = 0;
            for (var h : m.getEntitiesOfClass(com.darkcraft.enemigos.Helado.class, ab.getBoundingBox().inflate(24))) {
                if (!h.esEfimero()) continue;
                quitar.add(h);
                boolean pisa = !m.getBlockState(BlockPos.containing(h.getX(), h.getY() - 0.2, h.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty();
                if (pisa && m.noCollision(h)) bien++;
                if (h.getTarget() == fp) conPresa++;
            }
            sm.append(" segunda ").append(ab.segunda()).append(", se alzan ").append(ab.alzados()).append(" (en su suelo y sin meterse en nada ").append(bien)
                    .append(", con presa ").append(conPresa).append("), en pie ").append(ab.suyos()).append(", ventisca ").append(ab.ventisca())
                    .append(" (ventiscas +").append(ab.ventiscas() - vent).append(')');
            // ---- el frio no le hace nada; el fuego, mas que un golpe igual
            ab.invulnerableTime = 0;
            float v0 = ab.getHealth();
            boolean frioLe = ab.hurt(m.damageSources().freeze(), 10f);
            ab.invulnerableTime = 0;
            float v1 = ab.getHealth();
            ab.hurt(m.damageSources().generic(), 10f);
            ab.invulnerableTime = 0;
            float v2 = ab.getHealth();
            ab.hurt(m.damageSources().inFire(), 10f);
            ab.invulnerableTime = 0;
            sm.append(String.format(java.util.Locale.ROOT, "; el frio le hace %s (-%.1f); un golpe de 10, -%.1f; el fuego de 10, -%.1f", frioLe ? "dano" : "nada", v0 - v1,
                    v1 - v2, v2 - ab.getHealth()));
            log.println(sm);
            // ---- cae: la cumbre en paz; los hermanos que rezaban en la iglesia del monasterio descansan y ya no vuelven
            com.darkcraft.enemigos.Puestos.todos(server, monasterio, false);
            int iglesiaAntes = 0;
            java.util.List<com.darkcraft.enemigos.Enemigo> deLaIglesia = new java.util.ArrayList<>();
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, monasterio))
                if ("iglesia".equals(com.darkcraft.morvain.MorvainEnemigos.monasterio().get(e.puesto()).grupo())) {
                    iglesiaAntes++;
                    deLaIglesia.add(e);
                }
            ab.setTarget(null);
            ab.hurt(m.damageSources().playerAttack(fp), 99999f);
            int k = 0;
            for (; k < 150 && !ab.isRemoved(); k++) {
                ab.tickCount++;
                ab.tick();
            }
            int descansan = 0;
            for (var e : deLaIglesia) if (e.isRemoved()) descansan++;
            int objetos = m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, ab.getBoundingBox().inflate(5)).size();
            com.darkcraft.enemigos.Puestos.todos(server, monasterio, false);
            int iglesiaDespues = 0, otrosDespues = 0;
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, monasterio)) {
                if ("iglesia".equals(com.darkcraft.morvain.MorvainEnemigos.monasterio().get(e.puesto()).grupo())) iglesiaDespues++;
                else otrosDespues++;
            }
            log.println("Morvain, el Abad cae: se va en " + k + " ticks (vivo " + ab.isAlive() + "), objetos " + objetos + ", la cumbre en paz "
                    + com.darkcraft.morvain.Ermita.enPaz(server) + ", sus hermanos en pie " + ab.suyos() + "; de los " + iglesiaAntes
                    + " hermanos de la iglesia descansan " + descansan + "; despues salen " + iglesiaDespues + " en la iglesia (tiene que ser 0) y " + otrosDespues
                    + " por el resto del monasterio; " + com.darkcraft.morvain.MorvainEnemigos.estadoAbad(server));
            for (var it : m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, ab.getBoundingBox().inflate(8))) it.discard();
        } catch (Throwable t) {
            log.println("Morvain, el Abad: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] abad", t);
        } finally {
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            for (var e : m.getEntitiesOfClass(com.darkcraft.enemigos.Helado.class, new net.minecraft.world.phys.AABB(CX - 40, PISO - 10, CZ - 50, CX + 40, PISO + 20, CZ + 40)))
                if (e.esEfimero()) e.discard();
            // todo como antes: el Abad de rodillas en el calvario y los hermanos de la iglesia
            com.darkcraft.enemigos.Puestos.todos(server, cumbre, false);
            com.darkcraft.enemigos.Puestos.todos(server, monasterio, false);
            com.darkcraft.enemigos.Puestos.todos(server, escalera, false);
            try {
                com.darkcraft.enemigos.Puestos.sacarTodos(m, cumbre);
                log.println("Morvain, el Abad vuelve: " + com.darkcraft.morvain.MorvainEnemigos.estadoAbad(server) + " | iglesia del monasterio: quedan "
                        + com.darkcraft.enemigos.Puestos.quedan(server, monasterio, "iglesia"));
            } catch (Throwable t) {
                log.println("Morvain, el Abad vuelve: ERROR " + t);
            }
            com.darkcraft.enemigos.Puestos.todos(server, cumbre, false);
        }
    }

    /**
     * Parte 7k: el Monasterio de la Helada (la terraza, el atril con el libro 21, el arcon, la campana, la fuente helada, los
     * bancos de la iglesia) y los helados: los de rodillas y los enterrados salen en su sitio, los de pie en su suelo y ninguno
     * metido en un bloque; cada accion acaba; el de rodillas rompe el hielo al llegar su presa y el enterrado se alza; el aliento
     * hiela y frena; el martillazo hace brotar sus puas, que llegan mas lejos que el martillo; las esquirlas dan y hielan; el
     * frio no les hace nada y el fuego, mas; al morir se rompen en esquirlas. Al acabar todo vuelve a estar como antes.
     */
    private static void monasterio(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.MONASTERIO;
        var v = com.darkcraft.morvain.Monasterio.get();
        final int CX = com.darkcraft.morvain.Monasterio.CX, CZ = com.darkcraft.morvain.Monasterio.CZ, PISO = com.darkcraft.morvain.Monasterio.PISO;
        try {
            long t0 = System.currentTimeMillis();
            int nch = 0;
            for (long k : v.chunks()) {
                m.getChunk((int) (k >> 32), (int) k);
                nch++;
            }
            log.println("Morvain, el monasterio de la helada: " + nch + " chunks en " + (System.currentTimeMillis() - t0) + " ms; "
                    + com.darkcraft.morvain.Monasterio.resumen(m) + "; santuario " + com.darkcraft.santuario.Santuarios.nombre(m,
                    com.darkcraft.morvain.SantuariosMorvain.porId("helada").pos()));
            topView(m, CX + 6, CZ + 8, 50, new File(out, "morvain_monasterio.png"));
            topView(m, CX, CZ - 21, 14, new File(out, "morvain_monasterio_iglesia.png"));
        } catch (Throwable t) {
            log.println("Morvain, el monasterio de la helada: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] monasterio", t);
        }
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        try {
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            for (int k = 0; k < 10; k++)
                for (var e : fuera) if (e.isAlive()) e.tick();
            int rezan = 0, enterrados = 0, dePie = 0, enSuelo = 0, chocan = 0;
            StringBuilder mal = new StringBuilder();
            com.darkcraft.enemigos.Helado rezando = null, enterrado = null;
            for (var e : fuera) {
                if (!(e instanceof com.darkcraft.enemigos.Helado h)) continue;
                if (!m.noCollision(h)) {
                    chocan++;
                    if (mal.length() < 300) mal.append(" metido en un bloque en ").append(h.blockPosition().toShortString()).append(';');
                }
                if (h.postura() == com.darkcraft.enemigos.Enemigo.REZA) {
                    rezan++;
                    if (rezando == null && "claustro".equals(h.sitio() == null ? "" : com.darkcraft.morvain.MorvainEnemigos.monasterio().get(h.puesto()).grupo()))
                        rezando = h;
                } else if (h.postura() == com.darkcraft.enemigos.Enemigo.TUMBADO) {
                    enterrados++;
                    if (enterrado == null) enterrado = h;
                } else dePie++;
                if (!m.getBlockState(BlockPos.containing(h.getX(), h.getY() - 0.2, h.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty()) enSuelo++;
                else if (mal.length() < 400) mal.append(' ').append(h.tipo()).append(" en el aire en ").append(h.blockPosition().toShortString()).append(';');
            }
            log.println("Morvain, los helados: " + com.darkcraft.morvain.MorvainEnemigos.comprobar() + "; fuera " + fuera.size() + " de "
                    + com.darkcraft.morvain.MorvainEnemigos.monasterio().size() + ": de rodillas " + rezan + ", enterrados " + enterrados + ", de pie " + dePie
                    + " (en su suelo " + enSuelo + "); metidos en un bloque " + chocan + " (tiene que ser 0)" + mal);
            StringBuilder sb = new StringBuilder("Morvain, los helados en accion:");
            // ---- el de rodillas rompe el hielo y se levanta; el enterrado se alza (una presa a su lado)
            int rotos = com.darkcraft.enemigos.Helado.ROTOS;
            for (var h : new com.darkcraft.enemigos.Helado[]{rezando, enterrado}) {
                if (h == null) {
                    sb.append(" NO HAY ").append(h == rezando ? "NINGUNO DE RODILLAS;" : "NINGUNO ENTERRADO;");
                    continue;
                }
                var presa = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
                if (presa == null) continue;
                presa.moveTo(h.getX() + 2.5, h.getY(), h.getZ() + 0.5, 0f, 0f);
                presa.setNoAi(true);
                m.addFreshEntity(presa);
                quitar.add(presa);
                int antes = h.postura();
                h.setTarget(presa);
                int rompe = -1;
                for (int k = 0; k < 80; k++) {
                    h.tickCount++;
                    h.tick();
                    int a = h.accion();
                    if (rompe < 0 && (a == com.darkcraft.enemigos.HeladoAnims.LEVANTARSE || a == com.darkcraft.enemigos.HeladoAnims.ALZARSE)) rompe = k;
                    if (rompe >= 0 && a == 0) break;
                }
                sb.append(String.format(java.util.Locale.ROOT, " el %s (en %s) rompe el hielo en el tick %d y queda de pie %s;",
                        antes == com.darkcraft.enemigos.Enemigo.REZA ? "de rodillas" : "enterrado", h.blockPosition().toShortString(), rompe,
                        h.postura() == com.darkcraft.enemigos.Enemigo.DE_PIE));
                h.setTarget(null);
            }
            sb.append(" hielos rotos +").append(com.darkcraft.enemigos.Helado.ROTOS - rotos).append(';');
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio)) e.discard();
            // ---- uno de cada en el patio del claustro: cada accion acaba
            for (int v2 = 0; v2 < 3; v2++) {
                var a = DarkCraftMod.HELADO.get().create(m);
                if (a == null) continue;
                a.variante(v2);
                a.moveTo(CX - 4.5 + v2 * 4, PISO, CZ + 6.5, 0f, 0f);
                m.addFreshEntity(a);
                quitar.add(a);
                StringBuilder no = new StringBuilder();
                for (int ac = 1; ac < com.darkcraft.enemigos.HeladoAnims.TICKS.length; ac++) {
                    String nombre = com.darkcraft.enemigos.HeladoAnims.NOMBRES[ac];
                    if (nombre.equals("morir")) continue;
                    a.empezar(ac);
                    for (int k = 0; k < 90 && a.accion() == ac; k++) {
                        a.tickCount++;
                        a.tick();
                    }
                    if (a.accion() == ac) no.append(' ').append(nombre);
                    for (int k = 0; k < 10; k++) a.tick();
                }
                sb.append(' ').append(a.tipo()).append(String.format(java.util.Locale.ROOT, " (vida %.0f)", a.getMaxHealth())).append(": acciones que no acaban")
                        .append(no.length() == 0 ? " ninguna" : no).append(';');
                a.discard();
            }
            // ---- el aliento, el martillazo (sus puas, mas alla del martillo) y las esquirlas contra una presa
            int ali = com.darkcraft.enemigos.Helado.ALIENTOS, pua = com.darkcraft.enemigos.Helado.PUAS, esq = com.darkcraft.enemigos.Helado.ESQUIRLAS,
                    dan = com.darkcraft.enemigos.Esquirla.DAN;
            String[] nombres = {"aliento", "martillazo", "esquirlas"};
            int[] acciones = {com.darkcraft.enemigos.HeladoAnims.ALIENTO, com.darkcraft.enemigos.HeladoAnims.MARTILLAZO, com.darkcraft.enemigos.HeladoAnims.ESQUIRLAS};
            int[] variantes = {com.darkcraft.enemigos.Helado.MONJE, com.darkcraft.enemigos.Helado.PENITENTE, com.darkcraft.enemigos.Helado.PEREGRINO};
            double[] lejos = {3.0, 4.4, 8.0};
            for (int i = 0; i < 3; i++) {
                var a = DarkCraftMod.HELADO.get().create(m);
                var presa = net.minecraft.world.entity.EntityType.VILLAGER.create(m);
                if (a == null || presa == null) continue;
                a.variante(variantes[i]);
                // al sur de la fuente, mirando al sur (hacia el portico)
                a.moveTo(CX + 0.5, PISO, CZ + 4.5, 0f, 0f);
                presa.moveTo(CX + 0.5, PISO, CZ + 4.5 + lejos[i], 180f, 0f);
                presa.setNoAi(true);
                m.addFreshEntity(a);
                m.addFreshEntity(presa);
                quitar.add(a);
                quitar.add(presa);
                a.setTarget(presa);
                a.setYRot(0f);
                a.yBodyRot = a.yHeadRot = 0f;
                a.empezar(acciones[i]);
                float vida = presa.getHealth();
                int frio = 0;
                for (int k = 0; k < 70; k++) {
                    a.tickCount++;
                    a.tick();
                    frio = Math.max(frio, presa.getTicksFrozen());
                    for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.Esquirla.class, presa.getBoundingBox().inflate(12))) {
                        if (!quitar.contains(f)) quitar.add(f);
                        f.tick();
                    }
                    frio = Math.max(frio, presa.getTicksFrozen());
                    presa.invulnerableTime = 0;
                }
                sb.append(String.format(java.util.Locale.ROOT, " %s a %.1f: la presa -%.1f, helada %d, frenada %s;", nombres[i], lejos[i], vida - presa.getHealth(),
                        frio, presa.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN)));
                a.discard();
                // (fuera tambien la presa: las esquirlas de la siguiente le darian a ella, que se queda en medio)
                presa.discard();
            }
            sb.append(String.format(java.util.Locale.ROOT, " alientos +%d, puas +%d, esquirlas +%d (dan %d);", com.darkcraft.enemigos.Helado.ALIENTOS - ali,
                    com.darkcraft.enemigos.Helado.PUAS - pua, com.darkcraft.enemigos.Helado.ESQUIRLAS - esq, com.darkcraft.enemigos.Esquirla.DAN - dan));
            // ---- el frio no les hace nada; el fuego, un 60 % mas
            var h1 = DarkCraftMod.HELADO.get().create(m);
            var h2 = DarkCraftMod.HELADO.get().create(m);
            if (h1 != null && h2 != null) {
                h1.moveTo(CX - 5.5, PISO, CZ - 3.5, 0f, 0f);
                h2.moveTo(CX + 5.5, PISO, CZ - 3.5, 0f, 0f);
                m.addFreshEntity(h1);
                m.addFreshEntity(h2);
                quitar.add(h1);
                quitar.add(h2);
                float v1 = h1.getHealth(), v2 = h2.getHealth();
                boolean frioLe = h1.hurt(m.damageSources().freeze(), 5f);
                h2.hurt(m.damageSources().inFire(), 5f);
                sb.append(String.format(java.util.Locale.ROOT, " el frio le hace %s (-%.1f); el fuego -%.1f de 5;", frioLe ? "dano" : "nada", v1 - h1.getHealth(),
                        v2 - h2.getHealth()));
                // ---- y al morir se rompe en esquirlas
                int est = com.darkcraft.enemigos.Helado.ESTALLIDOS;
                var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("7a3c9d2e-4b5f-4c6d-8e9f-0a1b2c3d4e60"), "[DarkCraftCI5]"));
                h1.hurt(m.damageSources().playerAttack(fp), 9999f);
                int k = 0;
                for (; k < 90 && !h1.isRemoved(); k++) h1.tick();
                sb.append(String.format(java.util.Locale.ROOT, " al morir se rompe %s y se va en %d ticks", com.darkcraft.enemigos.Helado.ESTALLIDOS > est, k));
            }
            log.println(sb);
        } catch (Throwable t) {
            log.println("Morvain, los helados: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] helados", t);
        } finally {
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    /**
     * Parte 7i: la Aldea de los Pilotes (la laguna, el atril con el libro 20, el arcon, el fuego, el embarcadero) y los
     * podridos: los que esperan en el agua salen flotando, los demas en el suelo y ninguno metido en un bloque; cada accion
     * acaba; el vomito, el eructo y el fuego fatuo dejan su nube; el del agua se alza al acercarse su presa; el hinchado
     * revienta al morir; a ellos su veneno no les hace nada. Al acabar todo vuelve a estar como antes.
     */
    private static void pilotes(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.PILOTES;
        var v = com.darkcraft.morvain.Pilotes.get();
        int CX = com.darkcraft.morvain.Pilotes.CX, CZ = com.darkcraft.morvain.Pilotes.CZ, PASO = com.darkcraft.morvain.Pilotes.PASO;
        try {
            long t0 = System.currentTimeMillis();
            int nch = 0;
            for (long k : v.chunks()) {
                m.getChunk((int) (k >> 32), (int) k);
                nch++;
            }
            log.println("Morvain, la aldea de los pilotes: " + nch + " chunks en " + (System.currentTimeMillis() - t0) + " ms; "
                    + com.darkcraft.morvain.Pilotes.resumen(m) + "; santuario " + com.darkcraft.santuario.Santuarios.nombre(m,
                    com.darkcraft.morvain.SantuariosMorvain.porId("juncos").pos()));
            topView(m, CX, CZ - 6, 58, new File(out, "morvain_pilotes.png"));
            topView(m, CX, CZ - 32, 14, new File(out, "morvain_pilotes_capilla.png"));
        } catch (Throwable t) {
            log.println("Morvain, la aldea de los pilotes: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] pilotes", t);
        }
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        try {
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            int enAgua = 0, flotan = 0, enSuelo = 0, otros = 0, chocan = 0;
            StringBuilder mal = new StringBuilder();
            com.darkcraft.enemigos.Podrido flotando = null;
            // (unos ticks para que los del agua suban a flote)
            for (int k = 0; k < 30; k++)
                for (var e : fuera) if (e.isAlive()) e.tick();
            for (var e : fuera) {
                if (!(e instanceof com.darkcraft.enemigos.Podrido a)) continue;
                if (!m.noCollision(a)) {
                    chocan++;
                    if (mal.length() < 300) mal.append(" metido en un bloque en ").append(a.blockPosition().toShortString()).append(';');
                }
                if (a.postura() == com.darkcraft.enemigos.Enemigo.TUMBADO) {
                    enAgua++;
                    if (a.isInWater()) flotan++;
                    else if (mal.length() < 400) mal.append(" ").append(a.tipo()).append(" fuera del agua en ").append(a.blockPosition().toShortString()).append(';');
                    if (flotando == null && a.variante() == com.darkcraft.enemigos.Podrido.PODRIDO) flotando = a;
                } else {
                    otros++;
                    if (!m.getBlockState(BlockPos.containing(a.getX(), a.getY() - 0.2, a.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty() || a.isInWater()) enSuelo++;
                    else if (mal.length() < 400) mal.append(' ').append(a.tipo()).append(" en el aire en ").append(a.blockPosition().toShortString()).append(';');
                }
            }
            log.println("Morvain, los podridos: " + com.darkcraft.morvain.MorvainEnemigos.comprobar() + "; fuera " + fuera.size() + " de "
                    + com.darkcraft.morvain.MorvainEnemigos.pilotes().size() + ": en el agua " + enAgua + " (a flote " + flotan + "), los demas " + otros
                    + " (en su suelo " + enSuelo + "); metidos en un bloque " + chocan + " (tiene que ser 0)" + mal);
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("7a3c9d2e-4b5f-4c6d-8e9f-0a1b2c3d4e5f"), "[DarkCraftCI4]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            StringBuilder sb = new StringBuilder("Morvain, los podridos en accion:");
            // ---- el del agua se alza: una presa a su lado
            if (flotando != null) {
                var presa = DarkCraftMod.MARCHITO.get().create(m);
                if (presa != null) {
                    BlockPos h = flotando.hogar();
                    presa.moveTo(CX + 0.5, PASO + 1, CZ + 3.5, 0f, 0f);
                    m.addFreshEntity(presa);
                    quitar.add(presa);
                    flotando.setTarget(presa);
                    int alza = -1;
                    for (int k = 0; k < 80; k++) {
                        flotando.tickCount++;
                        flotando.tick();
                        if (alza < 0 && flotando.accion() == com.darkcraft.enemigos.PodridoAnims.ALZARSE) alza = k;
                        if (alza >= 0 && flotando.accion() == 0) break;
                    }
                    sb.append(String.format(java.util.Locale.ROOT, " el del agua (en %s) se alza en el tick %d y queda de pie %s;", h.toShortString(), alza,
                            flotando.postura() == com.darkcraft.enemigos.Enemigo.DE_PIE));
                }
            } else sb.append(" NO HAY NINGUNO FLOTANDO;");
            // ---- uno de cada en la plaza: cada accion acaba
            int gx = CX + 3, gz = CZ + 3;
            for (int v2 = 0; v2 < 3; v2++) {
                var a = DarkCraftMod.PODRIDO.get().create(m);
                if (a == null) continue;
                a.variante(v2);
                a.moveTo(gx + 0.5 - v2 * 3, PASO + 1, gz + 0.5, 0f, 0f);
                m.addFreshEntity(a);
                quitar.add(a);
                StringBuilder no = new StringBuilder();
                for (int ac = 1; ac < com.darkcraft.enemigos.PodridoAnims.TICKS.length; ac++) {
                    String nombre = com.darkcraft.enemigos.PodridoAnims.NOMBRES[ac];
                    if (nombre.equals("morir")) continue;
                    a.empezar(ac);
                    for (int k = 0; k < 90 && a.accion() == ac; k++) {
                        a.tickCount++;
                        a.tick();
                    }
                    if (a.accion() == ac) no.append(' ').append(nombre);
                    for (int k = 0; k < 10; k++) a.tick();
                }
                sb.append(' ').append(a.tipo()).append(String.format(java.util.Locale.ROOT, " (vida %.0f)", a.getMaxHealth())).append(": acciones que no acaban")
                        .append(no.length() == 0 ? " ninguna" : no).append(';');
            }
            // ---- el vomito, el fuego fatuo y el eructo contra un marchito: sus nubes envenenan
            int vom = com.darkcraft.enemigos.Podrido.VOMITOS, fat = com.darkcraft.enemigos.Podrido.FATUOS, nub = com.darkcraft.enemigos.Podrido.NUBES;
            String[] nombres = {"vomito", "fuego fatuo", "eructo"};
            int[] acciones = {com.darkcraft.enemigos.PodridoAnims.VOMITO, com.darkcraft.enemigos.PodridoAnims.LANZAR_FATUO, com.darkcraft.enemigos.PodridoAnims.ERUCTO};
            int[] variantes = {com.darkcraft.enemigos.Podrido.PODRIDO, com.darkcraft.enemigos.Podrido.FATUO, com.darkcraft.enemigos.Podrido.HINCHADO};
            double[] lejos = {4.0, 8.0, 2.0};
            for (int i = 0; i < 3; i++) {
                var a = DarkCraftMod.PODRIDO.get().create(m);
                var presa = DarkCraftMod.MARCHITO.get().create(m);
                if (a == null || presa == null) continue;
                a.variante(variantes[i]);
                // por el embarcadero, mirando al sur
                double ay = PASO + 1, az = CZ + 9 + i * 0.0;
                a.moveTo(CX + 0.5, ay, CZ + 8.5, 0f, 0f);
                presa.moveTo(CX + 0.5, ay, CZ + 8.5 + lejos[i], 180f, 0f);
                m.addFreshEntity(a);
                m.addFreshEntity(presa);
                quitar.add(a);
                quitar.add(presa);
                a.setTarget(presa);
                a.setYRot(0f);
                a.yBodyRot = a.yHeadRot = 0f;
                a.empezar(acciones[i]);
                float vida = presa.getHealth();
                for (int k = 0; k < 110; k++) {
                    if (a.accion() != 0) {
                        a.tickCount++;
                        a.tick();
                    }
                    presa.tick();
                    for (var n : m.getEntitiesOfClass(net.minecraft.world.entity.AreaEffectCloud.class, presa.getBoundingBox().inflate(6))) {
                        if (!quitar.contains(n)) quitar.add(n);
                        n.tick();
                    }
                    for (var f : m.getEntitiesOfClass(com.darkcraft.enemigos.FuegoFatuo.class, presa.getBoundingBox().inflate(12))) {
                        if (!quitar.contains(f)) quitar.add(f);
                        f.tick();
                    }
                }
                sb.append(String.format(java.util.Locale.ROOT, " %s a %.0f: la presa -%.1f, envenenada %s;", nombres[i], lejos[i], vida - presa.getHealth(),
                        presa.hasEffect(net.minecraft.world.effect.MobEffects.POISON)));
                for (var e2 : m.getEntitiesOfClass(net.minecraft.world.entity.AreaEffectCloud.class, presa.getBoundingBox().inflate(8))) e2.discard();
            }
            sb.append(String.format(java.util.Locale.ROOT, " vomitos +%d, fuegos +%d, nubes +%d;", com.darkcraft.enemigos.Podrido.VOMITOS - vom,
                    com.darkcraft.enemigos.Podrido.FATUOS - fat, com.darkcraft.enemigos.Podrido.NUBES - nub));
            // ---- a ellos su veneno no les hace nada
            var inmune = DarkCraftMod.PODRIDO.get().create(m);
            if (inmune != null) sb.append(" su veneno a un podrido: ").append(inmune.canBeAffected(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.POISON, 100)) ? "LE AFECTA" : "nada").append(';');
            // ---- el hinchado revienta: una nube grande y un golpe a quien este pegado
            var h = DarkCraftMod.PODRIDO.get().create(m);
            var pegada = DarkCraftMod.MARCHITO.get().create(m);
            if (h != null && pegada != null) {
                h.variante(com.darkcraft.enemigos.Podrido.HINCHADO);
                h.moveTo(CX - 3.5, PASO + 1, CZ - 2.5, 0f, 0f);
                pegada.moveTo(CX - 3.5, PASO + 1, CZ - 1.0, 180f, 0f);
                m.addFreshEntity(h);
                m.addFreshEntity(pegada);
                quitar.add(h);
                quitar.add(pegada);
                int rev = com.darkcraft.enemigos.Podrido.REVENTONES;
                h.hurt(m.damageSources().playerAttack(fp), 9999f);
                for (int k = 0; k < 80 && !h.isRemoved(); k++) h.tick();
                int nubes = m.getEntitiesOfClass(net.minecraft.world.entity.AreaEffectCloud.class, new net.minecraft.world.phys.AABB(h.blockPosition()).inflate(4)).size();
                sb.append(String.format(java.util.Locale.ROOT, " el hinchado cae: revienta %s, nubes alrededor %d, quitado %s", com.darkcraft.enemigos.Podrido.REVENTONES > rev,
                        nubes, h.isRemoved()));
                for (var e2 : m.getEntitiesOfClass(net.minecraft.world.entity.AreaEffectCloud.class, new net.minecraft.world.phys.AABB(h.blockPosition()).inflate(6))) e2.discard();
            }
            log.println(sb);
        } catch (Throwable t) {
            log.println("Morvain, los podridos: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] podridos", t);
        } finally {
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    /**
     * Parte 7g: la Arboleda de los Ahorcados (el Arbol de la Horca, el atril con el libro 19, el arcon, las sogas) y los
     * ahorcados: los colgados salen bajo su soga, en el aire y sin tocar nada; los demas en el suelo; cada accion acaba; uno
     * colgado se descuelga hacia su presa (la soga se queda deshilachada) y el golpe de la caida le alcanza; el lazo engancha
     * y tira de la presa; el salto se lanza a por ella; y caen sin dejar el cuerpo.
     */
    private static void arboleda(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.ARBOLEDA;
        var v = com.darkcraft.morvain.Arboleda.get();
        try {
            long t0 = System.currentTimeMillis();
            int nch = 0;
            for (long k : v.chunks()) {
                m.getChunk((int) (k >> 32), (int) k);
                nch++;
            }
            log.println("Morvain, la arboleda: " + nch + " chunks en " + (System.currentTimeMillis() - t0) + " ms; " + com.darkcraft.morvain.Arboleda.resumen(m)
                    + "; santuario " + com.darkcraft.santuario.Santuarios.nombre(m, com.darkcraft.morvain.SantuariosMorvain.porId("ahorcados").pos()));
            topView(m, com.darkcraft.morvain.Arboleda.GX + 6, com.darkcraft.morvain.Arboleda.GZ, 56, new File(out, "morvain_arboleda.png"));
            topView(m, com.darkcraft.morvain.Arboleda.PX, com.darkcraft.morvain.Arboleda.PZ, 14, new File(out, "morvain_arboleda_patibulo.png"));
        } catch (Throwable t) {
            log.println("Morvain, la arboleda: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] arboleda", t);
        }
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        try {
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            int colgados = 0, conSoga = 0, alto = 0, chocan = 0, enSuelo = 0, otros = 0;
            StringBuilder mal = new StringBuilder();
            com.darkcraft.enemigos.Ahorcado uno = null;
            for (var e : fuera) {
                if (!(e instanceof com.darkcraft.enemigos.Ahorcado a)) continue;
                if (!m.noCollision(a)) {
                    chocan++;
                    if (mal.length() < 300) mal.append(" metido en un bloque en ").append(a.blockPosition().toShortString()).append(';');
                }
                if (a.postura() == com.darkcraft.enemigos.Enemigo.COLGADO) {
                    colgados++;
                    if (a.soga() != null && m.getBlockState(a.soga()).getBlock() instanceof com.darkcraft.morvain.SogaBlock) conSoga++;
                    // los pies, entre 0,8 y 1,6 sobre el suelo de su puesto
                    double sobre = a.getY() - a.hogar().getY();
                    if (sobre > 0.8 && sobre < 1.6) alto++;
                    else if (mal.length() < 400) mal.append(String.format(java.util.Locale.ROOT, " colgado a %.2f del suelo;", sobre));
                    if (uno == null && a.variante() == com.darkcraft.enemigos.Ahorcado.GARRAS) uno = a;
                } else {
                    otros++;
                    if (!m.getBlockState(BlockPos.containing(a.getX(), a.getY() - 0.2, a.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty()) enSuelo++;
                    else if (mal.length() < 400) mal.append(' ').append(a.tipo()).append(" en el aire en ").append(a.blockPosition().toShortString()).append(';');
                }
            }
            log.println("Morvain, los ahorcados: " + com.darkcraft.morvain.MorvainEnemigos.comprobar() + "; fuera " + fuera.size() + " de "
                    + com.darkcraft.morvain.MorvainEnemigos.arboleda().size() + ": colgados " + colgados + " (con su soga encima " + conSoga + ", a la altura "
                    + alto + "), los demas " + otros + " (en el suelo " + enSuelo + "); metidos en un bloque " + chocan + " (tiene que ser 0)" + mal);
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("5f2b8c1e-3d4a-4e6b-9a7c-1b2d3e4f5a6d"), "[DarkCraftCI3]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            StringBuilder sb = new StringBuilder("Morvain, los ahorcados en accion:");
            // ---- se descuelga: hacia su presa (un marchito a 3 bloques), la soga se queda deshilachada y el golpe le alcanza
            if (uno != null) {
                var victima = DarkCraftMod.MARCHITO.get().create(m);
                if (victima != null) {
                    BlockPos h = uno.hogar();
                    double vy = com.darkcraft.enemigos.Puestos.suelo(m, h.getX() + 3, h.getY(), h.getZ());
                    victima.moveTo(h.getX() + 3.5, vy, h.getZ() + 0.5, 90f, 0f);
                    m.addFreshEntity(victima);
                    quitar.add(victima);
                    double antes = Math.hypot(uno.getX() - victima.getX(), uno.getZ() - victima.getZ());
                    float vida = victima.getHealth();
                    int caidas = com.darkcraft.enemigos.Ahorcado.CAIDAS;
                    BlockPos soga = uno.soga();
                    uno.setTarget(victima);
                    int suelta = -1;
                    for (int k = 0; k < 60; k++) {
                        uno.tickCount++;
                        uno.tick();
                        victima.tick();
                        if (suelta < 0 && uno.accion() == com.darkcraft.enemigos.AhorcadoAnims.CAER) suelta = k;
                        if (suelta >= 0 && uno.accion() == 0) break;
                    }
                    double despues = Math.hypot(uno.getX() - victima.getX(), uno.getZ() - victima.getZ());
                    var parte = soga == null ? null : m.getBlockState(soga).getBlock() instanceof com.darkcraft.morvain.SogaBlock
                            ? m.getBlockState(soga).getValue(com.darkcraft.morvain.SogaBlock.PARTE) : null;
                    sb.append(String.format(java.util.Locale.ROOT, " se descuelga en el tick %d, de %.1f a %.1f de su presa, en el suelo %s, postura %d, caidas +%d,"
                                    + " la presa -%.1f, la soga %s;", suelta, antes, despues, uno.onGround(), uno.postura(), com.darkcraft.enemigos.Ahorcado.CAIDAS - caidas,
                            vida - victima.getHealth(), parte));
                }
            } else sb.append(" NO HAY NINGUNO COLGADO;");
            // ---- uno de cada en el claro: cada accion acaba; el lazo engancha y tira; el salto se lanza
            int gx = com.darkcraft.morvain.Arboleda.GX + 8, gz = com.darkcraft.morvain.Arboleda.GZ + 8;
            double gy = com.darkcraft.enemigos.Puestos.suelo(m, gx, com.darkcraft.morvain.Arboleda.pie(gx, gz), gz);
            for (int v2 = 0; v2 < 2; v2++) {
                var a = DarkCraftMod.AHORCADO.get().create(m);
                if (a == null) continue;
                a.variante(v2);
                a.moveTo(gx + 0.5 + v2 * 4, gy, gz + 0.5, 0f, 0f);
                m.addFreshEntity(a);
                quitar.add(a);
                StringBuilder no = new StringBuilder();
                for (int ac = 1; ac < com.darkcraft.enemigos.AhorcadoAnims.TICKS.length; ac++) {
                    String nombre = com.darkcraft.enemigos.AhorcadoAnims.NOMBRES[ac];
                    if (nombre.equals("morir")) continue;
                    a.setNoGravity(false);
                    a.empezar(ac);
                    for (int k = 0; k < 90 && a.accion() == ac; k++) {
                        a.tickCount++;
                        a.tick();
                    }
                    if (a.accion() == ac) no.append(' ').append(nombre);
                    for (int k = 0; k < 10; k++) a.tick();
                }
                sb.append(' ').append(a.tipo()).append(": acciones que no acaban").append(no.length() == 0 ? " ninguna" : no).append(';');
            }
            // el lazo: a 7 bloques delante
            var lazo = DarkCraftMod.AHORCADO.get().create(m);
            var presa = DarkCraftMod.MARCHITO.get().create(m);
            if (lazo != null && presa != null) {
                lazo.variante(com.darkcraft.enemigos.Ahorcado.SOGA);
                lazo.moveTo(gx + 0.5, gy, gz - 8 + 0.5, 0f, 0f);
                double py = com.darkcraft.enemigos.Puestos.suelo(m, gx, com.darkcraft.morvain.Arboleda.pie(gx, gz - 1), gz - 1);
                presa.moveTo(gx + 0.5, py, gz - 1 + 0.5, 180f, 0f);
                m.addFreshEntity(lazo);
                m.addFreshEntity(presa);
                quitar.add(lazo);
                quitar.add(presa);
                lazo.setTarget(presa);
                lazo.empezar(com.darkcraft.enemigos.AhorcadoAnims.LAZO);
                int lazos = com.darkcraft.enemigos.Ahorcado.LAZOS;
                double antes = lazo.distanceTo(presa);
                for (int k = 0; k < 60 && lazo.accion() == com.darkcraft.enemigos.AhorcadoAnims.LAZO; k++) {
                    lazo.tickCount++;
                    lazo.tick();
                    presa.tick();
                }
                sb.append(String.format(java.util.Locale.ROOT, " el lazo: engancha %s, la presa de %.1f a %.1f;", com.darkcraft.enemigos.Ahorcado.LAZOS > lazos,
                        antes, lazo.distanceTo(presa)));
            }
            // el salto: a 5,5 bloques
            var salta = DarkCraftMod.AHORCADO.get().create(m);
            var otra = DarkCraftMod.MARCHITO.get().create(m);
            if (salta != null && otra != null) {
                salta.variante(com.darkcraft.enemigos.Ahorcado.GARRAS);
                salta.moveTo(gx - 6 + 0.5, gy, gz + 0.5, -90f, 0f);
                double py = com.darkcraft.enemigos.Puestos.suelo(m, gx, com.darkcraft.morvain.Arboleda.pie(gx - 1, gz), gz);
                otra.moveTo(gx - 0.5, py, gz + 0.5, 90f, 0f);
                m.addFreshEntity(salta);
                m.addFreshEntity(otra);
                quitar.add(salta);
                quitar.add(otra);
                salta.setTarget(otra);
                salta.setYRot(-90f);
                salta.yBodyRot = salta.yHeadRot = -90f;
                salta.empezar(com.darkcraft.enemigos.AhorcadoAnims.SALTO);
                double antes = salta.distanceTo(otra);
                float vida = otra.getHealth();
                for (int k = 0; k < 50 && salta.accion() == com.darkcraft.enemigos.AhorcadoAnims.SALTO; k++) {
                    salta.tickCount++;
                    salta.tick();
                    otra.tick();
                }
                sb.append(String.format(java.util.Locale.ROOT, " el salto: de %.1f a %.1f, la presa -%.1f;", antes, salta.distanceTo(otra), vida - otra.getHealth()));
                // cae: sin quedarse el cuerpo
                salta.setTarget(null);
                salta.hurt(m.damageSources().playerAttack(fp), 9999f);
                for (int k = 0; k < 80 && !salta.isRemoved(); k++) salta.tick();
                sb.append(" uno cae: vivo ").append(salta.isAlive()).append(", quitado ").append(salta.isRemoved());
            }
            log.println(sb);
        } catch (Throwable t) {
            log.println("Morvain, los ahorcados: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] ahorcados", t);
        } finally {
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    /**
     * Parte 7h: Maese Odran, el Verdugo de la Arboleda. Sale apoyado en el hacha en el tablado del patibulo; al ver a alguien se
     * yergue, no ruge hasta acabar de hablar y luego salta del patibulo sobre su presa (cae en el suelo, cerca de ella y sin
     * meterse en nada); cada accion acaba; sus golpes a un marchito (el hachazo con su onda, el barrido que pilla tambien a
     * quien salta, el doble, el pisoton detras); el lazo engancha, tira y le sigue la ejecucion; a media vida despierta a los
     * colgados de cerca y, sin colgados, se alzan otros de la tierra; al caer, la cuenta se cierra: los colgados descansan, sus
     * sogas se quedan con el lazo vacio y ya no vuelven a colgar. Al acabar todo vuelve a estar como antes.
     */
    /**
     * La Laguna Negra y el Barquero (parte 7j): la laguna levantada (el agua, el embarcadero, la barca con su arcon, los faroles
     * de almas), vistas desde arriba; sus podridos (a flote los del agua); el Barquero en su barca: al ver a alguien en el
     * embarcadero habla, ruge y salta de la barca a las tablas; cada accion acaba; sus golpes a un marchito (el remazo con su ola,
     * el barrido, el golpe de linterna que quema), el bichero que engancha y tira, el anillo de fuego (de pie le llega, saltando
     * no); a media vida se alzan sus pasajeros (o suben otros del agua); y al caer, la laguna en paz.
     */
    private static void barquero(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String barca = com.darkcraft.morvain.MorvainEnemigos.BARCA, laguna = com.darkcraft.morvain.MorvainEnemigos.LAGUNA;
        var L = com.darkcraft.morvain.LagunaNegra.get();
        final int CX = com.darkcraft.morvain.LagunaNegra.CX, CZ = com.darkcraft.morvain.LagunaNegra.CZ, PASO = com.darkcraft.morvain.LagunaNegra.PASO;
        try {
            long t0 = System.currentTimeMillis();
            int nch = 0;
            for (long k : L.chunks()) {
                m.getChunk((int) (k >> 32), (int) k);
                nch++;
            }
            log.println("Morvain, la laguna negra: " + nch + " chunks en " + (System.currentTimeMillis() - t0) + " ms; " + com.darkcraft.morvain.LagunaNegra.resumen(m)
                    + "; santuario " + com.darkcraft.santuario.Santuarios.nombre(m, com.darkcraft.morvain.SantuariosMorvain.porId("embarcadero").pos()));
            topView(m, CX - 12, CZ, 50, new File(out, "morvain_laguna.png"));
            topView(m, CX + 4, CZ, 16, new File(out, "morvain_laguna_embarcadero.png"));
        } catch (Throwable t) {
            log.println("Morvain, la laguna negra: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] laguna", t);
        }
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        try {
            // ---- sus podridos: los del agua, a flote
            com.darkcraft.enemigos.Puestos.todos(server, laguna, false);
            var podridos = com.darkcraft.enemigos.Puestos.sacarTodos(m, laguna);
            for (int k = 0; k < 30; k++)
                for (var e : podridos) if (e.isAlive()) e.tick();
            int flotan = 0, enAgua = 0, chocan = 0;
            for (var e : podridos) {
                if (!m.noCollision(e)) chocan++;
                if (e.postura() == com.darkcraft.enemigos.Enemigo.TUMBADO) {
                    enAgua++;
                    if (e.isInWater()) flotan++;
                }
            }
            log.println("Morvain, la laguna negra, sus podridos: fuera " + podridos.size() + " de " + com.darkcraft.morvain.MorvainEnemigos.laguna().size()
                    + ", en el agua " + enAgua + " (a flote " + flotan + "), metidos en un bloque " + chocan + " (tiene que ser 0)");
            // ---- el Barquero en su barca
            com.darkcraft.enemigos.Puestos.todos(server, barca, false);
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, barca);
            com.darkcraft.enemigos.Barquero ba = null;
            for (var e : fuera) if (e instanceof com.darkcraft.enemigos.Barquero x) ba = x;
            if (ba == null) {
                log.println("Morvain, el Barquero: NO SALE (" + fuera.size() + " fuera)");
                return;
            }
            boolean suelo = !m.getBlockState(BlockPos.containing(ba.getX(), ba.getY() - 0.2, ba.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty();
            StringBuilder sb = new StringBuilder("Morvain, el Barquero: en " + ba.blockPosition().toShortString() + " postura " + ba.postura()
                    + " (1 de vigia en su barca), en la barca " + (Math.abs(ba.getY() - (com.darkcraft.morvain.LagunaNegra.BARCA_SUELO + 1)) < 0.05) + ", en el suelo " + suelo
                    + ", metido en un bloque " + !m.noCollision(ba) + ", en el agua " + ba.isInWater() + ", vida " + Math.round(ba.getMaxHealth())
                    + ", caja " + ba.getBbWidth() + " x " + ba.getBbHeight());
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("9c4d2e1f-5a6b-4c7d-8e9f-1a2b3c4d5e70"), "[DarkCraftCI4]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            // ---- le ve desde el embarcadero (a unos once bloques de la barca): habla, ruge al acabar y salta a las tablas
            final double fxx = CX + 4.5, fzz = CZ + 0.5, fyy = PASO + 1;
            fp.moveTo(fxx, fyy, fzz, -90f, 0f);
            fp.setOnGround(true);
            double yAntes = ba.getY();
            ba.setTarget(fp);
            int levanta = -1, ruge = -1, salta = -1, cae = -1;
            for (int k = 0; k < 400; k++) {
                ba.tickCount++;
                ba.tick();
                fp.moveTo(fxx, fyy, fzz, -90f, 0f);
                if (levanta < 0 && ba.accion() == com.darkcraft.enemigos.BarqueroAnims.LEVANTARSE) levanta = k;
                if (ruge < 0 && ba.accion() == com.darkcraft.enemigos.BarqueroAnims.GRITO) ruge = k;
                if (salta < 0 && ba.accion() == com.darkcraft.enemigos.BarqueroAnims.SALTO) salta = k;
                if (salta >= 0 && ba.accion() != com.darkcraft.enemigos.BarqueroAnims.SALTO) {
                    cae = k;
                    break;
                }
            }
            for (int k = 0; k < 6; k++) ba.tick();
            boolean sueloTras = !m.getBlockState(BlockPos.containing(ba.getX(), ba.getY() - 0.2, ba.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty();
            sb.append("; al verle: pelea ").append(ba.enPelea()).append(", se yergue en el tick ").append(levanta).append(", ruge en el ").append(ruge)
                    .append(" (la frase 1 dura ").append(com.darkcraft.rey.Voces.ticks(com.darkcraft.enemigos.Barquero.VOZ_BASE + 1)).append("), salta en el ")
                    .append(salta).append(" y acaba en el ").append(cae).append(String.format(java.util.Locale.ROOT,
                            ": de Y %.1f a %.1f (las tablas, %d), a %.1f de su presa, en el suelo %s, metido en un bloque %s, en el agua %s, saltos %d", yAntes,
                            ba.getY(), PASO + 1, Math.hypot(ba.getX() - fp.getX(), ba.getZ() - fp.getZ()), sueloTras, !m.noCollision(ba), ba.isInWater(), ba.saltos()));
            // ---- cada accion acaba (sin presa)
            ba.setTarget(null);
            StringBuilder mal = new StringBuilder();
            for (int a = 1; a < com.darkcraft.enemigos.BarqueroAnims.TICKS.length; a++) {
                String nombre = com.darkcraft.enemigos.BarqueroAnims.NOMBRES[a];
                if (nombre.equals("morir")) continue;
                ba.empezar(a);
                for (int k = 0; k < 120 && ba.accion() == a; k++) {
                    ba.tickCount++;
                    ba.tick();
                }
                if (ba.accion() == a) mal.append(' ').append(nombre).append(" no acaba;");
                if (ba.volando()) mal.append(' ').append(nombre).append(": sigue volando;");
            }
            for (int k = 0; k < 40 && ba.anilloActivo(); k++) {
                ba.tickCount++;
                ba.tick();
            }
            sb.append("; acciones que no acaban:").append(mal.length() == 0 ? " ninguna" : mal);
            // ---- sus golpes, a un marchito (al jugador de mentira no le entra nada), en medio del embarcadero
            float giro = -90f;
            double fx = -Math.sin(Math.toRadians(giro)), fz = Math.cos(Math.toRadians(giro));
            double y = PASO + 1, x0 = CX - 3.5, z0 = CZ + 0.5;
            String[] golpes = {"remazo", "barrido de pie", "barrido saltando", "golpe de linterna"};
            int[] acc = {com.darkcraft.enemigos.BarqueroAnims.REMAZO, com.darkcraft.enemigos.BarqueroAnims.BARRIDO, com.darkcraft.enemigos.BarqueroAnims.BARRIDO,
                    com.darkcraft.enemigos.BarqueroAnims.GOLPE_LINTERNA};
            double[] lejos = {3.0, 3.0, 3.0, 2.4};
            double[] alto = {0, 0, 1.1, 0};
            sb.append("; golpes a un marchito:");
            var victima = DarkCraftMod.MARCHITO.get().create(m);
            if (victima != null) {
                victima.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(400.0);
                victima.moveTo(x0 + fx * 3, y, z0 + fz * 3, giro + 180f, 0f);
                m.addFreshEntity(victima);
                quitar.add(victima);
                for (int i = 0; i < golpes.length; i++) {
                    victima.setHealth(victima.getMaxHealth());
                    victima.invulnerableTime = 0;
                    victima.hurtTime = 0;
                    victima.clearFire();
                    double px = x0 + fx * lejos[i], pz = z0 + fz * lejos[i];
                    victima.moveTo(px, y + alto[i], pz, giro + 180f, 0f);
                    int ondas = ba.ondas();
                    ba.moveTo(x0, y, z0, giro, 0f);
                    ba.setTarget(victima);
                    ba.setYRot(giro);
                    ba.yBodyRot = ba.yHeadRot = giro;
                    ba.empezar(acc[i]);
                    float antes = victima.getHealth();
                    for (int k = 0; k < 120 && ba.accion() == acc[i]; k++) {
                        ba.tickCount++;
                        ba.tick();
                        victima.moveTo(px, y + alto[i], pz, giro + 180f, 0f);
                    }
                    sb.append(' ').append(golpes[i]).append(String.format(java.util.Locale.ROOT, " -%.1f", antes - victima.getHealth()))
                            .append(ba.ondas() > ondas ? " (ola)" : "").append(victima.isOnFire() ? " (arde)" : "").append(';');
                }
                // ---- el bichero: a cinco bloques y medio delante; engancha, tira y el golpe de linterna (sin lo que le quedaba del
                // empujon del golpe de linterna: si no, se iba resbalando fuera de su alcance antes de que el garfio llegara)
                victima.setHealth(victima.getMaxHealth());
                victima.invulnerableTime = 0;
                victima.hurtTime = 0;
                victima.clearFire();
                victima.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                victima.setTarget(null);
                double px = x0 + fx * 5.5, pz = z0 + fz * 5.5;
                victima.moveTo(px, y, pz, giro + 180f, 0f);
                ba.moveTo(x0, y, z0, giro, 0f);
                ba.setTarget(victima);
                ba.setYRot(giro);
                ba.yBodyRot = ba.yHeadRot = giro;
                int eng = ba.enganches(), rem = ba.rematados();
                double antes = Math.hypot(victima.getX() - ba.getX(), victima.getZ() - ba.getZ());
                float vida = victima.getHealth();
                ba.empezar(com.darkcraft.enemigos.BarqueroAnims.BICHERO);
                double cerca = antes;
                for (int k = 0; k < 110 && ba.accion() != 0; k++) {
                    ba.tickCount++;
                    ba.tick();
                    if (ba.accion() == com.darkcraft.enemigos.BarqueroAnims.BICHERO && ba.enganches() == eng) {
                        victima.moveTo(px, y, pz, giro + 180f, 0f);
                        victima.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    } else victima.tick();
                    cerca = Math.min(cerca, Math.hypot(victima.getX() - ba.getX(), victima.getZ() - ba.getZ()));
                }
                sb.append(String.format(java.util.Locale.ROOT, " el bichero: engancha %s, la presa de %.1f a %.1f, golpe de linterna %s, la presa -%.1f%s;",
                        ba.enganches() > eng, antes, cerca, ba.rematados() > rem, vida - victima.getHealth(), victima.isOnFire() ? " (arde)" : ""));
                // ---- el anillo de fuego: a la presa de pie a cuatro bloques le llega; a la que salta, no
                victima.setHealth(victima.getMaxHealth());
                victima.invulnerableTime = 0;
                victima.hurtTime = 0;
                victima.clearFire();
                StringBuilder an = new StringBuilder(" el anillo:");
                for (boolean salta2 : new boolean[]{false, true}) {
                    double qx = x0 + fx * 4, qz = z0 + fz * 4;
                    victima.moveTo(qx, y + (salta2 ? 1.2 : 0), qz, giro + 180f, 0f);
                    victima.setOnGround(!salta2);
                    victima.setNoGravity(salta2);
                    ba.moveTo(x0, y, z0, giro, 0f);
                    ba.setTarget(victima);
                    int alc = ba.alcanzados();
                    float v0 = victima.getHealth();
                    ba.empezar(com.darkcraft.enemigos.BarqueroAnims.LINTERNA);
                    for (int k = 0; k < 120 && (ba.accion() != 0 || ba.anilloActivo()); k++) {
                        ba.tickCount++;
                        ba.tick();
                        victima.moveTo(qx, y + (salta2 ? 1.2 : 0), qz, giro + 180f, 0f);
                        victima.setOnGround(!salta2);
                    }
                    victima.setNoGravity(false);
                    an.append(salta2 ? " saltando" : " de pie").append(" le llega ").append(ba.alcanzados() > alc)
                            .append(String.format(java.util.Locale.ROOT, " (-%.1f%s)", v0 - victima.getHealth(), victima.isOnFire() ? ", arde" : "")).append(';');
                    victima.clearFire();
                    victima.invulnerableTime = 0;
                }
                sb.append(an).append(" anillos ").append(ba.anillos()).append(';');
                victima.discard();
            }
            log.println(sb);
            // ---- a media vida: se alzan sus pasajeros (los de la laguna, fuera y flotando)
            StringBuilder sm = new StringBuilder("Morvain, el Barquero a media vida:");
            com.darkcraft.enemigos.Puestos.todos(server, laguna, false);
            var pasajeros = com.darkcraft.enemigos.Puestos.sacarTodos(m, laguna);
            for (int k = 0; k < 20; k++)
                for (var e : pasajeros) if (e.isAlive()) e.tick();
            int flotandoAntes = 0;
            for (var e : pasajeros) if (e.postura() == com.darkcraft.enemigos.Enemigo.TUMBADO) flotandoAntes++;
            ba.moveTo(CX + 2.5, y, CZ + 0.5, -90f, 0f);
            fp.moveTo(fxx + 2, fyy, fzz, -90f, 0f);
            ba.setTarget(fp);
            ba.setHealth(ba.getMaxHealth() * 0.45f);
            for (int k = 0; k < 160 && ba.despertados() + ba.subidos() == 0; k++) {
                ba.tickCount++;
                ba.tick();
                fp.moveTo(fxx + 2, fyy, fzz, -90f, 0f);
            }
            int conPresa = 0;
            for (var e : pasajeros) if (e.getTarget() == fp) conPresa++;
            sm.append(" segunda ").append(ba.segunda()).append(", flotando en la laguna ").append(flotandoAntes).append(", se alzan ").append(ba.despertados())
                    .append(" (con presa ").append(conPresa).append("), suben del agua ").append(ba.subidos()).append(';');
            // ---- otra pelea, sin nadie flotando: suben del agua al embarcadero
            ba.setTarget(null);
            for (int k = 0; k < 130 && ba.enPelea(); k++) {
                ba.tickCount++;
                ba.tick();
            }
            com.darkcraft.enemigos.Puestos.todos(server, laguna, true);
            for (var e : m.getEntitiesOfClass(com.darkcraft.enemigos.Podrido.class, ba.getBoundingBox().inflate(40))) if (e.esEfimero()) e.discard();
            boolean acabo = !ba.enPelea();
            int subidos = ba.subidos();
            ba.setTarget(fp);
            for (int k = 0; k < 80; k++) {
                ba.tickCount++;
                ba.tick();
                fp.moveTo(fxx + 2, fyy, fzz, -90f, 0f);
            }
            ba.setHealth(ba.getMaxHealth() * 0.45f);
            for (int k = 0; k < 200 && ba.subidos() == subidos; k++) {
                ba.tickCount++;
                ba.tick();
                fp.moveTo(fxx + 2, fyy, fzz, -90f, 0f);
            }
            int enTablas = 0;
            for (var e : m.getEntitiesOfClass(com.darkcraft.enemigos.Podrido.class, ba.getBoundingBox().inflate(16)))
                if (e.esEfimero() && Math.abs(e.getY() - (PASO + 1)) < 0.6 && m.noCollision(e)) enTablas++;
            sm.append(" otra pelea (la anterior acabo ").append(acabo).append(") sin nadie flotando: suben ").append(ba.subidos() - subidos)
                    .append(" (en las tablas y sin meterse en nada ").append(enTablas).append("), en pie ").append(ba.suyos());
            log.println(sm);
            // ---- cae: la laguna en paz
            com.darkcraft.enemigos.Puestos.todos(server, laguna, false);
            com.darkcraft.enemigos.Puestos.sacarTodos(m, laguna);
            ba.setTarget(null);
            ba.hurt(m.damageSources().playerAttack(fp), 99999f);
            for (int k = 0; k < 90 && !ba.isRemoved(); k++) {
                ba.tickCount++;
                ba.tick();
            }
            int objetos = m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, ba.getBoundingBox().inflate(4)).size();
            String faroles = com.darkcraft.morvain.LagunaNegra.faroles(m);
            // ya no salen los pasajeros (los del muelle, si)
            com.darkcraft.enemigos.Puestos.todos(server, laguna, false);
            int pasajerosDespues = 0, otrosDespues = 0;
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, laguna)) {
                if ("pasajeros".equals(com.darkcraft.morvain.MorvainEnemigos.laguna().get(e.puesto()).grupo())) pasajerosDespues++;
                else otrosDespues++;
            }
            log.println("Morvain, el Barquero cae: vivo " + ba.isAlive() + ", la laguna en paz " + com.darkcraft.morvain.LagunaNegra.enPaz(server) + ", subidos en pie "
                    + ba.suyos() + ", objetos " + objetos + "; " + faroles + "; despues salen " + pasajerosDespues + " pasajeros (tiene que ser 0) y "
                    + otrosDespues + " de los del muelle");
            for (var it : m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, ba.getBoundingBox().inflate(6))) it.discard();
            for (var e : m.getEntitiesOfClass(com.darkcraft.enemigos.Podrido.class, ba.getBoundingBox().inflate(40))) if (e.esEfimero()) e.discard();
        } catch (Throwable t) {
            log.println("Morvain, el Barquero: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] barquero", t);
        } finally {
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            // todo como antes: el Barquero en su barca, sus pasajeros flotando y los faroles de almas
            com.darkcraft.enemigos.Puestos.todos(server, barca, false);
            com.darkcraft.enemigos.Puestos.todos(server, laguna, false);
            try {
                com.darkcraft.morvain.LagunaNegra.almas(m);
                com.darkcraft.enemigos.Puestos.sacarTodos(m, laguna);
                log.println("Morvain, el Barquero vuelve: " + com.darkcraft.morvain.MorvainEnemigos.estadoBarquero(server));
            } catch (Throwable t) {
                log.println("Morvain, el Barquero vuelve: ERROR " + t);
            }
            com.darkcraft.enemigos.Puestos.todos(server, laguna, false);
            com.darkcraft.enemigos.Puestos.todos(server, barca, false);
        }
    }

    private static void verdugo(MinecraftServer server, ServerLevel m, PrintWriter log) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.PATIBULO, arb = com.darkcraft.morvain.MorvainEnemigos.ARBOLEDA;
        var A = com.darkcraft.morvain.Arboleda.get();
        final int PX = com.darkcraft.morvain.Arboleda.PX, PZ = com.darkcraft.morvain.Arboleda.PZ;
        java.util.List<net.minecraft.world.entity.Entity> quitar = new java.util.ArrayList<>();
        try {
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            com.darkcraft.enemigos.Puestos.todos(server, arb, false);
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            com.darkcraft.enemigos.Verdugo ve = null;
            for (var e : fuera) if (e instanceof com.darkcraft.enemigos.Verdugo x) ve = x;
            if (ve == null) {
                log.println("Morvain, el Verdugo: NO SALE (" + fuera.size() + " fuera)");
                return;
            }
            boolean suelo = !m.getBlockState(BlockPos.containing(ve.getX(), ve.getY() - 0.2, ve.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty();
            StringBuilder sb = new StringBuilder("Morvain, el Verdugo: en " + ve.blockPosition().toShortString() + " postura " + ve.postura()
                    + " (1 apoyado en el hacha), en el tablado " + (Math.abs(ve.getY() - (A.fy + 1)) < 0.05) + ", en el suelo " + suelo
                    + ", metido en un bloque " + !m.noCollision(ve) + ", vida " + Math.round(ve.getMaxHealth()) + ", caja " + ve.getBbWidth() + " x " + ve.getBbHeight());
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("5f2b8c1e-3d4a-4e6b-9a7c-1b2d3e4f5a6e"), "[DarkCraftCI4]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            // ---- le ve a nueve bloques, abajo, delante del patibulo: se yergue mientras habla, ruge al acabar y salta
            final int fzz = PZ + 10;
            final double fyy = com.darkcraft.enemigos.Puestos.suelo(m, PX, com.darkcraft.morvain.Arboleda.pie(PX, fzz), fzz);
            fp.moveTo(PX + 0.5, fyy, fzz + 0.5, 180f, 0f);
            double yAntes = ve.getY();
            ve.setTarget(fp);
            int levanta = -1, ruge = -1, salta = -1, cae = -1;
            for (int k = 0; k < 400; k++) {
                ve.tickCount++;
                ve.tick();
                fp.moveTo(PX + 0.5, fyy, fzz + 0.5, 180f, 0f);
                if (levanta < 0 && ve.accion() == com.darkcraft.enemigos.VerdugoAnims.LEVANTARSE) levanta = k;
                if (ruge < 0 && ve.accion() == com.darkcraft.enemigos.VerdugoAnims.GRITO) ruge = k;
                if (salta < 0 && ve.accion() == com.darkcraft.enemigos.VerdugoAnims.SALTO) salta = k;
                if (salta >= 0 && ve.accion() != com.darkcraft.enemigos.VerdugoAnims.SALTO) {
                    cae = k;
                    break;
                }
            }
            for (int k = 0; k < 6; k++) ve.tick();
            boolean sueloTras = !m.getBlockState(BlockPos.containing(ve.getX(), ve.getY() - 0.2, ve.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty();
            sb.append("; al verle: pelea ").append(ve.enPelea()).append(", se yergue en el tick ").append(levanta).append(", ruge en el ").append(ruge)
                    .append(" (la frase 1 dura ").append(com.darkcraft.rey.Voces.ticks(com.darkcraft.enemigos.Verdugo.VOZ_BASE + 1)).append("), salta en el ")
                    .append(salta).append(" y acaba en el ").append(cae).append(String.format(java.util.Locale.ROOT,
                            ": de Y %.1f a %.1f, a %.1f de su presa, en el suelo %s, metido en un bloque %s, saltos %d", yAntes, ve.getY(),
                            Math.hypot(ve.getX() - fp.getX(), ve.getZ() - fp.getZ()), sueloTras, !m.noCollision(ve), ve.saltos()));
            // ---- cada accion acaba (sin presa)
            ve.setTarget(null);
            StringBuilder mal = new StringBuilder();
            for (int a = 1; a < com.darkcraft.enemigos.VerdugoAnims.TICKS.length; a++) {
                String nombre = com.darkcraft.enemigos.VerdugoAnims.NOMBRES[a];
                if (nombre.equals("morir")) continue;
                ve.empezar(a);
                for (int k = 0; k < 120 && ve.accion() == a; k++) {
                    ve.tickCount++;
                    ve.tick();
                }
                if (ve.accion() == a) mal.append(' ').append(nombre).append(" no acaba;");
                if (ve.volando()) mal.append(' ').append(nombre).append(": sigue volando;");
            }
            sb.append("; acciones que no acaban:").append(mal.length() == 0 ? " ninguna" : mal);
            // ---- sus golpes, a un marchito (al jugador de mentira no le entra nada)
            float giro = ve.getYRot();
            double fx = -Math.sin(Math.toRadians(giro)), fz = Math.cos(Math.toRadians(giro));
            double y = ve.getY(), x0 = ve.getX(), z0 = ve.getZ();
            String[] golpes = {"hachazo", "barrido de pie", "barrido saltando", "doble", "pisoton detras", "ejecucion"};
            int[] acc = {com.darkcraft.enemigos.VerdugoAnims.HACHAZO, com.darkcraft.enemigos.VerdugoAnims.BARRIDO, com.darkcraft.enemigos.VerdugoAnims.BARRIDO,
                    com.darkcraft.enemigos.VerdugoAnims.DOBLE, com.darkcraft.enemigos.VerdugoAnims.PISOTON, com.darkcraft.enemigos.VerdugoAnims.EJECUCION};
            double[] lejos = {3.0, 3.0, 3.0, 2.5, -2.2, 2.4};
            double[] alto = {0, 0, 1.1, 0, 0, 0};
            sb.append("; golpes a un marchito:");
            var victima = DarkCraftMod.MARCHITO.get().create(m);
            if (victima != null) {
                victima.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(300.0);
                victima.moveTo(x0 + fx * 3, y, z0 + fz * 3, giro + 180f, 0f);
                m.addFreshEntity(victima);
                quitar.add(victima);
                for (int i = 0; i < golpes.length; i++) {
                    victima.setHealth(victima.getMaxHealth());
                    victima.invulnerableTime = 0;
                    victima.hurtTime = 0;
                    double px = x0 + fx * lejos[i], pz = z0 + fz * lejos[i];
                    victima.moveTo(px, y + alto[i], pz, giro + 180f, 0f);
                    int ondas = ve.ondas();
                    ve.moveTo(x0, y, z0, giro, 0f);
                    ve.setTarget(victima);
                    ve.setYRot(giro);
                    ve.yBodyRot = ve.yHeadRot = giro;
                    ve.empezar(acc[i]);
                    float antes = victima.getHealth();
                    for (int k = 0; k < 120 && ve.accion() == acc[i]; k++) {
                        ve.tickCount++;
                        ve.tick();
                        victima.moveTo(px, y + alto[i], pz, giro + 180f, 0f);
                    }
                    sb.append(' ').append(golpes[i]).append(String.format(java.util.Locale.ROOT, " -%.1f", antes - victima.getHealth()))
                            .append(ve.ondas() > ondas ? " (onda)" : "").append(';');
                }
                // ---- el lazo: a ocho bloques delante; engancha, tira y la ejecucion
                victima.setHealth(victima.getMaxHealth());
                victima.invulnerableTime = 0;
                victima.hurtTime = 0;
                double px = x0 + fx * 8, pz = z0 + fz * 8;
                double py = com.darkcraft.enemigos.Puestos.suelo(m, net.minecraft.util.Mth.floor(px), net.minecraft.util.Mth.floor(y), net.minecraft.util.Mth.floor(pz));
                victima.moveTo(px, py, pz, giro + 180f, 0f);
                ve.moveTo(x0, y, z0, giro, 0f);
                ve.setTarget(victima);
                ve.setYRot(giro);
                ve.yBodyRot = ve.yHeadRot = giro;
                int lazos = ve.lazos(), ejec = ve.ejecuciones();
                double antes = Math.hypot(victima.getX() - ve.getX(), victima.getZ() - ve.getZ());
                float vida = victima.getHealth();
                ve.empezar(com.darkcraft.enemigos.VerdugoAnims.LAZO);
                double cerca = antes;
                for (int k = 0; k < 110 && ve.accion() != 0; k++) {
                    ve.tickCount++;
                    ve.tick();
                    victima.tick();
                    cerca = Math.min(cerca, Math.hypot(victima.getX() - ve.getX(), victima.getZ() - ve.getZ()));
                }
                sb.append(String.format(java.util.Locale.ROOT, " el lazo: engancha %s, la presa de %.1f a %.1f, ejecucion %s, la presa -%.1f;",
                        ve.lazos() > lazos, antes, cerca, ve.ejecuciones() > ejec, vida - victima.getHealth()));
                victima.discard();
            }
            log.println(sb);
            // ---- a media vida: despierta a los colgados de cerca (los de la arboleda, fuera)
            StringBuilder sm = new StringBuilder("Morvain, el Verdugo a media vida:");
            com.darkcraft.enemigos.Puestos.todos(server, arb, false);
            var ahorcados = com.darkcraft.enemigos.Puestos.sacarTodos(m, arb);
            int colgandoAntes = 0;
            for (var e : ahorcados) if (e instanceof com.darkcraft.enemigos.Ahorcado ah && ah.cuelga()) colgandoAntes++;
            ve.moveTo(PX + 0.5, fyy, PZ + 6.5, 180f, 0f);
            fp.moveTo(PX + 0.5, fyy, fzz + 0.5, 180f, 0f);
            ve.setTarget(fp);
            ve.setHealth(ve.getMaxHealth() * 0.45f);
            for (int k = 0; k < 160 && ve.descolgados() + ve.alzados() == 0; k++) {
                ve.tickCount++;
                ve.tick();
                fp.moveTo(PX + 0.5, fyy, fzz + 0.5, 180f, 0f);
            }
            int caen = 0;
            for (var e : ahorcados) if (e instanceof com.darkcraft.enemigos.Ahorcado ah && ah.getTarget() == fp) caen++;
            sm.append(" segunda ").append(ve.segunda()).append(", colgados fuera ").append(colgandoAntes).append(", se descuelgan ").append(ve.descolgados())
                    .append(" (con presa ").append(caen).append("), se alzan de la tierra ").append(ve.alzados()).append(';');
            // ---- otra pelea, sin colgados: se alzan de la tierra
            ve.setTarget(null);
            for (int k = 0; k < 130 && ve.enPelea(); k++) {
                ve.tickCount++;
                ve.tick();
            }
            com.darkcraft.enemigos.Puestos.todos(server, arb, true);
            boolean acabo = !ve.enPelea();
            int alzados = ve.alzados();
            ve.setTarget(fp);
            for (int k = 0; k < 80; k++) {
                ve.tickCount++;
                ve.tick();
                fp.moveTo(PX + 0.5, fyy, fzz + 0.5, 180f, 0f);
            }
            ve.setHealth(ve.getMaxHealth() * 0.45f);
            for (int k = 0; k < 200 && ve.alzados() == alzados; k++) {
                ve.tickCount++;
                ve.tick();
                fp.moveTo(PX + 0.5, fyy, fzz + 0.5, 180f, 0f);
            }
            sm.append(" otra pelea (la anterior acabo ").append(acabo).append(") sin colgados: se alzan ").append(ve.alzados() - alzados)
                    .append(", en pie ").append(ve.suyos());
            log.println(sm);
            // ---- cae: la cuenta se cierra
            ve.setTarget(null);
            ve.hurt(m.damageSources().playerAttack(fp), 99999f);
            for (int k = 0; k < 90 && !ve.isRemoved(); k++) {
                ve.tickCount++;
                ve.tick();
            }
            int objetos = m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, ve.getBoundingBox().inflate(4)).size();
            String sogas = com.darkcraft.morvain.Arboleda.sogasColgados(m);
            // ya no salen colgados (los demas, si)
            com.darkcraft.enemigos.Puestos.todos(server, arb, false);
            int colgadosDespues = 0, otrosDespues = 0;
            for (var e : com.darkcraft.enemigos.Puestos.sacarTodos(m, arb)) {
                if (e.postura() == com.darkcraft.enemigos.Enemigo.COLGADO || "colgados".equals(com.darkcraft.morvain.MorvainEnemigos.arboleda().get(e.puesto()).grupo())) colgadosDespues++;
                else otrosDespues++;
            }
            log.println("Morvain, el Verdugo cae: vivo " + ve.isAlive() + ", la cuenta cerrada " + com.darkcraft.morvain.Arboleda.cuentaCerrada(server) + ", alzados en pie "
                    + ve.suyos() + ", objetos " + objetos + "; " + sogas + "; despues salen " + colgadosDespues + " colgados (tiene que ser 0) y " + otrosDespues + " de los demas");
            for (var it : m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, ve.getBoundingBox().inflate(6))) it.discard();
            for (var e : m.getEntitiesOfClass(com.darkcraft.enemigos.Ahorcado.class, ve.getBoundingBox().inflate(40))) if (e.esEfimero()) e.discard();
        } catch (Throwable t) {
            log.println("Morvain, el Verdugo: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] verdugo", t);
        } finally {
            for (var e : quitar) if (!e.isRemoved()) e.discard();
            // todo como antes: el Verdugo en pie, los colgados de vuelta en sus sogas (las rehacen al colgar)
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            com.darkcraft.enemigos.Puestos.todos(server, arb, false);
            try {
                com.darkcraft.enemigos.Puestos.sacarTodos(m, arb);
                log.println("Morvain, el Verdugo vuelve: " + com.darkcraft.morvain.MorvainEnemigos.estadoVerdugo(server));
            } catch (Throwable t) {
                log.println("Morvain, el Verdugo vuelve: ERROR " + t);
            }
            com.darkcraft.enemigos.Puestos.todos(server, arb, false);
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        }
    }

    /**
     * Velmor y los Marchitos (parte 7e): el pueblo levantado (casas, el atril de la capilla con el libro 18, la campana del
     * campanario, los arcones), vistas desde arriba; los puestos de los marchitos, todos fuera y en el suelo, todas sus
     * acciones; la campanada que llama a los de alrededor; el farol que estalla; y que caen en ceniza con su logro.
     */
    private static void velmor(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        try {
            com.darkcraft.morvain.Velmor v = com.darkcraft.morvain.Velmor.get();
            long t2 = System.currentTimeMillis();
            int nch = 0;
            for (long k : v.chunks()) {
                int cx = (int) (k >> 32), cz = (int) k;
                // (el pueblo, el puente y el vado; los caminos de fuera no hacen falta)
                if ((cx << 4) + 15 < 636 || (cz << 4) > 4200) continue;
                m.getChunk(cx, cz);
                nch++;
            }
            log.println("Morvain, Velmor: " + nch + " chunks en " + (System.currentTimeMillis() - t2) + " ms; " + com.darkcraft.morvain.Velmor.resumen(m));
            topView(m, 852, 4026, 140, new File(out, "morvain_velmor.png"));
            topView(m, 846, 3916, 30, new File(out, "morvain_velmor_capilla.png"));
            topView(m, 690, 4040, 52, new File(out, "morvain_velmor_puente.png"));
        } catch (Throwable t) {
            log.println("Morvain, Velmor: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] Velmor", t);
        }
        try {
            String sitio = com.darkcraft.morvain.MorvainEnemigos.VELMOR;
            log.println("Morvain, los marchitos: " + com.darkcraft.morvain.MorvainEnemigos.comprobar() + " | dificultad " + m.getDifficulty());
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            int enSuelo = 0, chocan = 0, fallos = 0;
            java.util.Map<String, Integer> tipos = new java.util.TreeMap<>();
            StringBuilder mal = new StringBuilder();
            for (var e : fuera) {
                tipos.merge(e.tipo() + (e.postura() == 1 ? " (reza)" : e.postura() == 2 ? " (tumbado)" : ""), 1, Integer::sum);
                boolean suelo = !m.getBlockState(BlockPos.containing(e.getX(), e.getY() - 0.2, e.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty();
                if (suelo) enSuelo++;
                else if (mal.length() < 300) mal.append(' ').append(e.tipo()).append(" en el aire en ").append(e.blockPosition().toShortString()).append(';');
                if (!m.noCollision(e)) {
                    chocan++;
                    if (mal.length() < 300) mal.append(' ').append(e.tipo()).append(" metido en un bloque en ").append(e.blockPosition().toShortString()).append(';');
                }
            }
            // cada uno hace todas sus acciones (sin presa): que ninguna rompa nada
            for (var e : fuera) {
                try {
                    e.postura(0);
                    for (int a = 1; a < com.darkcraft.enemigos.MarchitoAnims.TICKS.length; a++) {
                        String nombre = com.darkcraft.enemigos.MarchitoAnims.NOMBRES[a];
                        if (nombre.equals("morir")) continue;
                        e.empezar(a);
                        for (int k = 0; k < 90 && e.accion() == a; k++) e.tick();
                        if (e.accion() == a) {
                            fallos++;
                            if (mal.length() < 500) mal.append(' ').append(e.tipo()).append(": la accion ").append(nombre).append(" no acaba;");
                        }
                    }
                    for (int k = 0; k < 20; k++) e.tick();
                } catch (Throwable t) {
                    fallos++;
                    if (mal.length() < 700) mal.append(' ').append(e.tipo()).append(": ").append(t).append(';');
                    DarkCraftMod.LOGGER.error("[CI] marchito " + e.tipo(), t);
                }
            }
            log.println("Morvain, los marchitos fuera: " + fuera.size() + " de " + com.darkcraft.morvain.MorvainEnemigos.velmor().size() + " " + tipos
                    + "; en el suelo " + enSuelo + ", metidos en un bloque " + chocan + " (tiene que ser 0), acciones que fallan " + fallos
                    + " (tiene que ser 0)" + mal);
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            // la campanada: un campanero con presa toca y los de alrededor van a por ella; el farol estalla y deja fuego de almas
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("5f2b8c1e-3d4a-4e6b-9a7c-1b2d3e4f5a6b"), "[DarkCraftCI]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            int x = com.darkcraft.morvain.Velmor.X, z = com.darkcraft.morvain.Velmor.Z + 4;
            double y = com.darkcraft.enemigos.Puestos.suelo(m, x, m.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            var camp = DarkCraftMod.MARCHITO.get().create(m);
            var a1 = DarkCraftMod.MARCHITO.get().create(m);
            var a2 = DarkCraftMod.MARCHITO.get().create(m);
            var lejos = DarkCraftMod.MARCHITO.get().create(m);
            String campana = "no se crean";
            if (camp != null && a1 != null && a2 != null && lejos != null) {
                camp.variante(com.darkcraft.enemigos.Marchito.CAMPANA);
                a1.variante(com.darkcraft.enemigos.Marchito.GUADANA);
                a2.variante(com.darkcraft.enemigos.Marchito.FAROL);
                lejos.variante(com.darkcraft.enemigos.Marchito.GUADANA);
                camp.moveTo(x + 0.5, y, z + 0.5, 0f, 0f);
                a1.moveTo(x + 8.5, y, z + 0.5, 0f, 0f);
                a2.moveTo(x - 12.5, y, z + 3.5, 0f, 0f);
                lejos.moveTo(x + 0.5, y, z + 40.5, 0f, 0f);
                for (var e : new com.darkcraft.enemigos.Marchito[]{camp, a1, a2, lejos}) m.addFreshEntity(e);
                fp.moveTo(x + 0.5, y, z + 12.5, 180f, 0f);
                camp.setTarget(fp);
                int antes = com.darkcraft.enemigos.Marchito.CAMPANADAS;
                camp.empezar(com.darkcraft.enemigos.MarchitoAnims.CAMPANA_TOCAR);
                for (int k = 0; k < 40 && camp.accion() == com.darkcraft.enemigos.MarchitoAnims.CAMPANA_TOCAR; k++) camp.tick();
                campana = "campanadas " + (com.darkcraft.enemigos.Marchito.CAMPANADAS - antes) + "; acuden: el de 8 bloques " + (a1.getTarget() == fp)
                        + ", el de 12 " + (a2.getTarget() == fp) + ", el de 40 " + (lejos.getTarget() == fp) + " (tiene que ser false)";
                // el farol: estalla delante
                a2.setTarget(null);
                a2.moveTo(x - 12.5, y, z + 3.5, 0f, 0f);
                a2.empezar(com.darkcraft.enemigos.MarchitoAnims.FAROL_GOLPE);
                for (int k = 0; k < 40 && a2.accion() != 0; k++) a2.tick();
                campana += "; el farol deja fuego de almas " + a2.fuegoArde();
                // caer: en ceniza, sin quedarse el cuerpo
                fp.moveTo(x + 2.5, y, z + 0.5, 90f, 0f);
                a1.hurt(m.damageSources().playerAttack(fp), 9999f);
                for (int k = 0; k < 60 && !a1.isRemoved(); k++) a1.tick();
                campana += "; uno cae: vivo " + a1.isAlive() + ", quitado " + a1.isRemoved();
                for (var e : new com.darkcraft.enemigos.Marchito[]{camp, a1, a2, lejos}) if (!e.isRemoved()) e.discard();
            }
            log.println("Morvain, la campana de los marchitos: " + campana);
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
        } catch (Throwable t) {
            log.println("Morvain, los marchitos: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] marchitos", t);
        }
    }

    /**
     * Parte 7f: el molino (la era despejada, la muela, las aspas, el arcon y la reja) y la Molinera: sale de rodillas en su
     * sitio, al ver a alguien se levanta y no grita ni ataca hasta acabar de hablar; cada accion acaba; la siega pilla a quien
     * esta de pie y no a quien salta; el tajo y el pisoton levantan su onda; a media vida grita y llama a los suyos; al caer
     * se abre la reja del molino.
     */
    private static void molinera(MinecraftServer server, ServerLevel m, PrintWriter log, File out) {
        String sitio = com.darkcraft.morvain.MorvainEnemigos.MOLINO;
        try {
            var v = com.darkcraft.morvain.Molino.get();
            long t0 = System.currentTimeMillis();
            int nch = 0;
            for (long k : v.chunks()) {
                int cx = (int) (k >> 32), cz = (int) k;
                if ((cx << 4) + 15 < 1090) continue;
                m.getChunk(cx, cz);
                nch++;
            }
            log.println("Morvain, el molino: " + nch + " chunks en " + (System.currentTimeMillis() - t0) + " ms; " + com.darkcraft.morvain.Molino.resumen(m)
                    + "; santuario del camino " + com.darkcraft.santuario.Santuarios.nombre(m, com.darkcraft.morvain.SantuariosMorvain.porId("molino").pos()));
            topView(m, com.darkcraft.morvain.Molino.EX + 10, com.darkcraft.morvain.Molino.EZ, 40, new File(out, "morvain_molino.png"));
        } catch (Throwable t) {
            log.println("Morvain, el molino: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] molino", t);
        }
        try {
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            var fuera = com.darkcraft.enemigos.Puestos.sacarTodos(m, sitio);
            com.darkcraft.enemigos.Molinera mo = null;
            for (var e : fuera) if (e instanceof com.darkcraft.enemigos.Molinera x) mo = x;
            if (mo == null) {
                log.println("Morvain, la Molinera: NO SALE (" + fuera.size() + " fuera)");
                return;
            }
            boolean suelo = !m.getBlockState(BlockPos.containing(mo.getX(), mo.getY() - 0.2, mo.getZ())).getCollisionShape(m, BlockPos.ZERO).isEmpty();
            StringBuilder sb = new StringBuilder("Morvain, la Molinera: fuera " + fuera.size() + " del molino; ella en " + mo.blockPosition().toShortString()
                    + " postura " + mo.postura() + " (1 de rodillas), en el suelo " + suelo + ", metida en un bloque " + !m.noCollision(mo)
                    + ", vida " + Math.round(mo.getMaxHealth()) + ", escala de su caja " + mo.getBbWidth() + " x " + mo.getBbHeight());
            var fp = net.minecraftforge.common.util.FakePlayerFactory.get(m, new com.mojang.authlib.GameProfile(java.util.UUID.fromString("5f2b8c1e-3d4a-4e6b-9a7c-1b2d3e4f5a6c"), "[DarkCraftCI2]"));
            fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            double y = mo.getY();
            // la ve a 10 bloques: se levanta mientras habla y no grita hasta acabar
            fp.moveTo(mo.getX() - 10, y, mo.getZ(), -90f, 0f);
            mo.setTarget(fp);
            int levanta = -1, grita = -1;
            for (int k = 0; k < 260; k++) {
                mo.tickCount++;
                mo.tick();
                if (levanta < 0 && mo.accion() == com.darkcraft.enemigos.MolineraAnims.LEVANTARSE) levanta = k;
                if (grita < 0 && mo.accion() == com.darkcraft.enemigos.MolineraAnims.GRITO) grita = k;
                if (grita >= 0 && k > grita + 4) break;
            }
            sb.append("; al verle: pelea ").append(mo.enPelea()).append(", se levanta en el tick ").append(levanta).append(", grita en el ")
                    .append(grita).append(" (la frase 1 dura ").append(com.darkcraft.rey.Voces.ticks(com.darkcraft.enemigos.Molinera.VOZ_BASE + 1)).append(")");
            for (int k = 0; k < 60 && mo.accion() != 0; k++) {
                mo.tickCount++;
                mo.tick();
            }
            // cada accion acaba (sin presa); la siega, con uno de pie delante y luego saltando
            mo.setTarget(null);
            StringBuilder mal = new StringBuilder();
            for (int a = 1; a < com.darkcraft.enemigos.MolineraAnims.TICKS.length; a++) {
                String nombre = com.darkcraft.enemigos.MolineraAnims.NOMBRES[a];
                if (nombre.equals("morir")) continue;
                mo.empezar(a);
                for (int k = 0; k < 120 && mo.accion() == a; k++) {
                    mo.tickCount++;
                    mo.tick();
                }
                if (mo.accion() == a) mal.append(' ').append(nombre).append(" no acaba;");
            }
            sb.append("; acciones que no acaban:").append(mal.length() == 0 ? " ninguna" : mal);
            float giro = mo.getYRot();
            double fx = -Math.sin(Math.toRadians(giro)), fz = Math.cos(Math.toRadians(giro));
            String[] golpes = {"siega de pie", "siega saltando", "tajo", "estocada a 6", "pisoton detras"};
            int[] acc = {com.darkcraft.enemigos.MolineraAnims.SIEGA, com.darkcraft.enemigos.MolineraAnims.SIEGA, com.darkcraft.enemigos.MolineraAnims.TAJO,
                    com.darkcraft.enemigos.MolineraAnims.ESTOCADA, com.darkcraft.enemigos.MolineraAnims.PISOTON};
            double[] lejos = {3.0, 3.0, 3.5, 6.0, -2.4};
            double[] alto = {0, 1.1, 0, 0, 0};
            sb.append("; golpes (a un marchito: al jugador de mentira no le entra nada):");
            var victima = DarkCraftMod.MARCHITO.get().create(m);
            if (victima != null) {
                victima.moveTo(mo.getX() + fx * 3, y, mo.getZ() + fz * 3, giro + 180f, 0f);
                m.addFreshEntity(victima);
                for (int i = 0; i < golpes.length; i++) {
                    victima.setHealth(victima.getMaxHealth());
                    victima.invulnerableTime = 0;
                    victima.hurtTime = 0;
                    double px = mo.getX() + fx * lejos[i], pz = mo.getZ() + fz * lejos[i];
                    victima.moveTo(px, y + alto[i], pz, giro + 180f, 0f);
                    int ondas = mo.ondas();
                    mo.setTarget(victima);
                    mo.setYRot(giro);
                    mo.yBodyRot = mo.yHeadRot = giro;
                    mo.empezar(acc[i]);
                    float antes = victima.getHealth();
                    for (int k = 0; k < 120 && mo.accion() == acc[i]; k++) {
                        mo.tickCount++;
                        mo.tick();
                        victima.moveTo(px, y + alto[i], pz, giro + 180f, 0f);
                    }
                    sb.append(' ').append(golpes[i]).append(String.format(java.util.Locale.ROOT, " -%.1f", antes - victima.getHealth()))
                            .append(mo.ondas() > ondas ? " (onda)" : "").append(';');
                    mo.moveTo(mo.hogar().getX() + 0.5, mo.hogar().getY(), mo.hogar().getZ() + 0.5, giro, 0f);
                }
                mo.setTarget(null);
                victima.discard();
            }
            fp.setHealth(fp.getMaxHealth());
            // a media vida: grita y salen los suyos
            fp.moveTo(mo.getX() - fx * -8, y, mo.getZ() - fz * -8, giro + 180f, 0f);
            mo.setTarget(fp);
            mo.setHealth(mo.getMaxHealth() * 0.45f);
            int llamaron = -1;
            for (int k = 0; k < 120; k++) {
                mo.tickCount++;
                mo.tick();
                if (mo.suyos() > 0) {
                    llamaron = k;
                    break;
                }
            }
            sb.append(" a media vida: segunda ").append(mo.segunda()).append(", salen ").append(mo.suyos()).append(" marchitos (en el tick ").append(llamaron)
                    .append(")");
            log.println(sb);
            // cae: se abre la reja del molino
            int[] cofre = com.darkcraft.morvain.Molino.get().sitio("cofre_molino");
            mo.setTarget(null);
            mo.hurt(m.damageSources().playerAttack(fp), 99999f);
            for (int k = 0; k < 80 && !mo.isRemoved(); k++) {
                mo.tickCount++;
                mo.tick();
            }
            int objetos = m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, mo.getBoundingBox().inflate(4)).size();
            log.println("Morvain, la Molinera cae: viva " + mo.isAlive() + ", caida " + com.darkcraft.morvain.Molino.vencida(server) + ", suyos en pie "
                    + mo.suyos() + ", objetos " + objetos + "; " + com.darkcraft.morvain.Molino.resumen(m) + " (arcon en " + cofre[0] + "," + cofre[1] + "," + cofre[2] + ")");
            for (var it : m.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, mo.getBoundingBox().inflate(6))) it.discard();
            for (var e : m.getEntitiesOfClass(com.darkcraft.enemigos.Marchito.class, mo.getBoundingBox().inflate(30))) if (e.esEfimero()) e.discard();
        } catch (Throwable t) {
            log.println("Morvain, la Molinera: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] molinera", t);
        } finally {
            com.darkcraft.enemigos.Puestos.todos(server, sitio, false);
            com.darkcraft.morvain.Molino.puerta(m, false, false);
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
