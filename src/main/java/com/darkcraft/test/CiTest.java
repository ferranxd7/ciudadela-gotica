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
                    log.println("Escalera de la fortaleza en " + java.util.Arrays.toString(com.darkcraft.fortaleza.Puente.centro(fp)) + ", base a "
                            + com.darkcraft.fortaleza.Puente.base(lvl, fp));
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
                    boolean arq = e instanceof com.darkcraft.enemigos.Arquero;
                    int acciones = arq ? com.darkcraft.enemigos.ArqueroAnims.TICKS.length : com.darkcraft.enemigos.GuardiaAnims.TICKS.length;
                    for (int a = 1; a < acciones; a++) {
                        String nombre = arq ? com.darkcraft.enemigos.ArqueroAnims.NOMBRES[a] : com.darkcraft.enemigos.GuardiaAnims.NOMBRES[a];
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
            log.println("Valdrenn, la guardia: escudo " + escudo + "; disparan " + disparos + "; con el Rey vencido salen " + sinRey + " de " + puestos.size()
                    + ", con el Rey y Ser Gavran " + sinLosDos + " | " + com.darkcraft.valdrenn.ValdrennEnemigos.estado(server, k));
        } catch (Throwable t) {
            log.println("Valdrenn, la guardia: ERROR " + t);
            DarkCraftMod.LOGGER.error("[CI] la guardia de Valdrenn", t);
        }
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
