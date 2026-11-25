package com.darkcraft.fortaleza;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Espectro;
import com.darkcraft.enemigos.Guardia;
import com.darkcraft.enemigos.Puestos;
import com.darkcraft.enemigos.Puestos.Puesto;
import com.darkcraft.gen.FortalezaPlan;
import com.darkcraft.gen.Montana;
import com.darkcraft.gen.TorreCupula;
import com.darkcraft.pueblo.gen.Andar;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Los enemigos de la Fortaleza de Hueso (parte 5c, respuestas 37 y 47 del plan), en sus puestos ({@link Puestos}: salen
 * al acercarse; el que cae no vuelve hasta que alguien descansa en un santuario o muere):
 * <ul>
 * <li><b>el Monte del Colmillo</b>: perros dormidos al pie del sendero, un caballero a media cuesta de cada tramo (el de
 * arriba mira cuesta abajo), arqueros en los recodos sin torre (tiran cuesta abajo, a lo largo del tramo siguiente), algo
 * dentro de cada torre en ruinas (caballeros o perros) y, en el mirador, un capitan de elite con dos alabardas ante el
 * arco de colmillos;</li>
 * <li><b>el jardin bajo la cupula</b>: dos alabardas al salir del paso de la puerta, caballeros de ronda por los caminos y
 * espectros: los vigias (los nuevos, de capucha y ojos rojos, que se desvanecen y aparecen a cada rato) junto a la fuente
 * y una dama velando en un banco;</li>
 * <li><b>la torre</b>: la sala de guardias y la armeria, una dama rezando ante el altar de la capilla, vigias y cortesanos
 * en los aposentos y el observatorio, un caballero entre los escombros de la planta en ruinas;</li>
 * <li><b>las mazmorras</b>: perros durmiendo en la sala de guardia del oeste (la del muro falso), un monton de huesos en
 * una celda y un carcelero en la del este.</li>
 * </ul>
 * Los de dentro solo salen con la fortaleza en pie ({@code completa}); en el rellano de la puerta (el santuario) no hay
 * nadie. Cada fortaleza se apunta sola cuando alguien se acerca ({@code fortaleza@x,z}).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class FortalezaEnemigos {
    private FortalezaEnemigos() {}

    public static final String PREFIJO = "fortaleza@";
    static final String GUARDIA = "guardia", ARQUERO = "arquero", PERRO = "perro", ESPECTRO = "espectro", ELITE = "elite";
    /** Los grupos: lo de fuera (la montana y su mirador) y lo de dentro (solo con la fortaleza en pie). */
    public static final String MONTANA = "montana", MIRADOR = "mirador", JARDIN = "jardin", TORRE = "torre", SOTANO = "sotano";
    static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;
    private static final int DE_PIE = Enemigo.DE_PIE, REZA = Enemigo.REZA, TUMBADO = Enemigo.TUMBADO;

    public static String clave(Fortalezas.Sitio s) {
        return PREFIJO + s.clave();
    }

    /** Si ese grupo es de dentro de la fortaleza (sale solo con ella en pie). */
    static boolean dentro(String grupo) {
        return grupo.equals(JARDIN) || grupo.equals(TORRE) || grupo.equals(SOTANO);
    }

    // ------------------------------------------------------------------ los puestos

    public static List<Puesto> puestos(FortalezaPlan f) {
        List<Puesto> l = new ArrayList<>();
        montana(l, f.montana);
        jardin(l, f);
        torre(l, f);
        sotano(l, f);
        return l;
    }

    private static void p(List<Puesto> l, String tipo, int v, int x, int y, int z, float giro, int postura, int ronda, String grupo) {
        l.add(new Puesto(tipo, v, x, y, z, giro, postura, ronda, grupo));
    }

    /** Hacia donde mira quien va de (x0, z0) a (x1, z1) (giro de Minecraft). */
    static float giro(double x0, double z0, double x1, double z1) {
        return (float) Math.toDegrees(-Math.atan2(x1 - x0, z1 - z0));
    }

    private static int pies(double h) {
        return (int) Math.floor(h + 0.01);
    }

    /** El sendero de la montana, su mirador y las torres en ruinas de los recodos. */
    private static void montana(List<Puesto> l, Montana m) {
        List<double[]> s = m.sendero(), rec = m.recodos();
        // el mirador: el capitan delante del arco, mirando al sendero, y dos alabardas a los lados de la bajada
        int[] a = m.arco();
        p(l, ELITE, 0, m.mx, a[1], m.mz - 2, SUR, DE_PIE, 3, MIRADOR);
        p(l, GUARDIA, Guardia.ALABARDA, m.mx - 4, a[1], m.mz + 3, SUR, DE_PIE, 0, MIRADOR);
        p(l, GUARDIA, Guardia.ALABARDA, m.mx + 4, a[1], m.mz + 3, SUR, DE_PIE, 0, MIRADOR);
        // a media cuesta de cada tramo (donde el sendero cruza el centro de la montana), un caballero mirando cuesta abajo;
        // en el de mas abajo (el primero que se pisa) dos perros dormidos
        List<Integer> cruces = new ArrayList<>();
        for (int i = 1; i + 1 < s.size(); i++) {
            double a0 = s.get(i - 1)[0] - m.mx, a1 = s.get(i)[0] - m.mx;
            if (a0 < 0 && a1 >= 0 || a0 > 0 && a1 <= 0) cruces.add(i);
        }
        for (int t = 0; t < cruces.size(); t++) {
            int i = cruces.get(t);
            double[] q = s.get(i), abajo = s.get(Math.min(s.size() - 1, i + 3));
            int x = (int) Math.floor(q[0]), z = (int) Math.floor(q[1]), y = pies(q[2]);
            float mira = giro(q[0], q[1], abajo[0], abajo[1]);
            if (t == cruces.size() - 1) {
                p(l, PERRO, 0, x, y, z, mira, REZA, 0, MONTANA);
                p(l, PERRO, 0, x + (abajo[0] > q[0] ? -3 : 3), y, z, mira, REZA, 0, MONTANA);
            } else p(l, GUARDIA, t % 3 == 1 ? Guardia.ALABARDA : Guardia.ESPADA, x, y, z, mira, DE_PIE, 3, MONTANA);
        }
        // los recodos: con torre (uno de cada dos y el ultimo), algo dentro; sin torre, un arquero que tira cuesta abajo
        for (int i = 0; i < rec.size(); i++) {
            double[] r = rec.get(i);
            boolean torre = i % 2 == 0 || i == rec.size() - 1;
            if (torre) {
                int tx = (int) Math.floor(r[0] + r[3] * 5), tz = (int) Math.floor(r[1]), y = (int) Math.floor(r[2]);
                float mira = giro(tx, tz, m.mx, m.mz);
                if ((i / 2) % 2 == 0) p(l, GUARDIA, Guardia.ESPADA, tx, y, tz + 1, mira, DE_PIE, 0, MONTANA);
                else {
                    p(l, PERRO, 0, tx - 1, y, tz - 1, mira, REZA, 0, MONTANA);
                    p(l, PERRO, 0, tx, y, tz + 1, mira, REZA, 0, MONTANA);
                }
            } else {
                // el arquero en el recodo, mirando a lo largo del tramo de debajo (el sendero va de la cima al pie)
                int k = cercano(s, r[0], r[1]);
                double[] abajo = s.get(Math.min(s.size() - 1, k + 12));
                p(l, ARQUERO, 0, (int) Math.floor(r[0]), pies(r[2]), (int) Math.floor(r[1]), giro(r[0], r[1], abajo[0], abajo[1]), DE_PIE, 0, MONTANA);
            }
        }
    }

    private static int cercano(List<double[]> s, double x, double z) {
        int k = 0;
        double mejor = Double.MAX_VALUE;
        for (int i = 0; i < s.size(); i++) {
            double d = Math.hypot(s.get(i)[0] - x, s.get(i)[1] - z);
            if (d < mejor) {
                mejor = d;
                k = i;
            }
        }
        return k;
    }

    /** El jardin bajo la cupula (la puerta esta al sur, la fuente en cz + 28 y la torre en el centro). */
    private static void jardin(List<Puesto> l, FortalezaPlan f) {
        int cx = f.cx, cz = f.cz, y = f.Y;
        // al salir del paso de la puerta (a 16 del santuario del rellano)
        p(l, GUARDIA, Guardia.ALABARDA, cx - 3, y, cz + 37, SUR, DE_PIE, 0, JARDIN);
        p(l, GUARDIA, Guardia.ALABARDA, cx + 3, y, cz + 37, SUR, DE_PIE, 0, JARDIN);
        // los vigias junto a la fuente, y otro al pie de la torre
        p(l, ESPECTRO, Espectro.VIGIA, cx - 7, y, cz + 25, SUR, DE_PIE, 4, JARDIN);
        p(l, ESPECTRO, Espectro.VIGIA, cx + 7, y, cz + 31, SUR, DE_PIE, 4, JARDIN);
        p(l, ESPECTRO, Espectro.VIGIA, cx - 4, y, cz + 14, SUR, DE_PIE, 3, JARDIN);
        // caballeros de ronda por los caminos del este, del oeste y del norte
        p(l, GUARDIA, Guardia.ESPADA, cx - 22, y, cz + 8, ESTE, DE_PIE, 6, JARDIN);
        p(l, GUARDIA, Guardia.ESPADA, cx + 22, y, cz - 8, OESTE, DE_PIE, 6, JARDIN);
        p(l, GUARDIA, Guardia.BALLESTA, cx + 16, y, cz + 22, OESTE, DE_PIE, 2, JARDIN);
        p(l, GUARDIA, Guardia.ESPADA, cx + 3, y, cz - 25, SUR, DE_PIE, 5, JARDIN);
        // una dama velando en el jardin del oeste
        p(l, ESPECTRO, Espectro.GARRAS, cx - 18, y, cz + 20, OESTE, REZA, 0, JARDIN);
    }

    /** Las plantas de la torre (planta baja en Y, cada planta 10 mas arriba). */
    private static void torre(List<Puesto> l, FortalezaPlan f) {
        TorreCupula t = f.f;
        int cx = f.cx, cz = f.cz;
        // planta baja: la sala de guardias (oeste) y la armeria (este)
        p(l, GUARDIA, Guardia.ESPADA, cx - 5, t.piso(0), cz, ESTE, DE_PIE, 2, TORRE);
        p(l, GUARDIA, Guardia.ALABARDA, cx + 6, t.piso(0), cz + 1, OESTE, DE_PIE, 1, TORRE);
        // la capilla: una dama rezando ante el altar (al norte del tubo)
        p(l, ESPECTRO, Espectro.GARRAS, cx, t.piso(1), cz - 4, NORTE, REZA, 0, TORRE);
        p(l, ESPECTRO, Espectro.VIGIA, cx + 6, t.piso(1), cz + 5, OESTE, DE_PIE, 2, TORRE);
        // los aposentos
        p(l, ESPECTRO, Espectro.VIGIA, cx, t.piso(2), cz + 5, SUR, DE_PIE, 2, TORRE);
        p(l, ESPECTRO, Espectro.ESTOQUE, cx - 5, t.piso(3), cz, ESTE, DE_PIE, 2, TORRE);
        p(l, ESPECTRO, Espectro.GARRAS, cx + 5, t.piso(3), cz - 1, OESTE, DE_PIE, 2, TORRE);
        // la planta en ruinas: un caballero entre los escombros (lejos del suelo que cede)
        p(l, GUARDIA, Guardia.ESPADA, cx + 6, t.piso(4), cz + 4, OESTE, DE_PIE, 2, TORRE);
        // el observatorio
        p(l, ESPECTRO, Espectro.VIGIA, cx + 5, t.piso(5), cz + 5, NORTE, DE_PIE, 2, TORRE);
    }

    /** Las mazmorras: perros en la sala del oeste, huesos en una celda, el carcelero en la sala del este. */
    private static void sotano(List<Puesto> l, FortalezaPlan f) {
        int cx = f.cx, cz = f.cz, ys = f.Y + TorreCupula.SOTANO;
        p(l, PERRO, 0, cx - 6, ys, cz - 1, ESTE, REZA, 0, SOTANO);
        p(l, PERRO, 0, cx - 6, ys, cz + 2, ESTE, REZA, 0, SOTANO);
        p(l, PERRO, 0, cx + 6, ys, cz + 9, OESTE, TUMBADO, 0, SOTANO);
        p(l, GUARDIA, Guardia.ESPADA, cx + 6, ys, cz - 1, OESTE, DE_PIE, 2, SOTANO);
    }

    // ------------------------------------------------------------------ apuntar cada fortaleza cuando alguien se acerca

    @SubscribeEvent
    public static void onServidor(TickEvent.ServerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || ev.getServer().getTickCount() % 40 != 21) return;
        ServerLevel lvl = ev.getServer().overworld();
        for (ServerPlayer p : lvl.players()) {
            Fortalezas.Sitio s = Fortalezas.cercana(lvl.getSeed(), p.getX(), p.getZ(), 420);
            if (s == null || Puestos.apuntado(clave(s))) continue;
            apuntar(lvl, s);
        }
    }

    /** Apunta los puestos de esa fortaleza (si ya lo estaban, los vuelve a apuntar). */
    public static void apuntar(ServerLevel lvl, Fortalezas.Sitio s) {
        FortalezaPlan f = Fortalezas.plan(lvl.getSeed(), s);
        String k = clave(s);
        Puestos.registrar(k, puestos(f), l -> l.dimension() == Level.OVERWORLD,
                (l, q) -> !dentro(q.grupo()) || FortalezaDatos.get(l.getServer()).de(s).completa);
        DarkCraftMod.LOGGER.info("[DarkCraft] Fortaleza de Hueso en {}: {} puestos de enemigos", s.clave(), Puestos.sitio(k).puestos.size());
    }

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent e) {
        Puestos.olvidar(PREFIJO);
    }

    /** Lo apunta el mod al arrancar: los tipos (son los mismos que los de Valdrenn). */
    public static void registrar() {
        Puestos.tipo(GUARDIA, DarkCraftMod.GUARDIA);
        Puestos.tipo(ARQUERO, DarkCraftMod.ARQUERO);
        Puestos.tipo(PERRO, DarkCraftMod.PERRO);
        Puestos.tipo(ESPECTRO, DarkCraftMod.ESPECTRO);
        Puestos.tipo(ELITE, DarkCraftMod.ELITE);
    }

    /** Cuantos quedan en pie y cuantos hay fuera (para el comando). */
    public static String resumen(ServerLevel lvl, Fortalezas.Sitio s, FortalezaPlan f) {
        String k = clave(s);
        if (!Puestos.apuntado(k)) apuntar(lvl, s);
        var st = Puestos.sitio(k);
        int[] n = new int[5];
        String[] g = {MONTANA, MIRADOR, JARDIN, TORRE, SOTANO};
        for (Puesto q : st.puestos) for (int i = 0; i < g.length; i++) if (q.grupo().equals(g[i])) n[i]++;
        return String.format(Locale.ROOT, "%s: %d de %d en pie (%d a la vista); montana %d, mirador %d, jardin %d, torre %d, sotano %d",
                k, Puestos.quedan(lvl.getServer(), k), st.puestos.size(), Puestos.fuera(k), n[0], n[1], n[2], n[3], n[4]);
    }

    // ------------------------------------------------------------------ comprobarlo sin abrir el juego

    /**
     * Para {@code tools/vista3d} y las pruebas: cada puesto, si se puede estar de pie en el (o a menos de tres bloques: en el
     * juego se busca sitio al lado) y si se llega andando: los de la montana desde su pie, los de dentro desde el jardin
     * (con la puerta abierta). Devuelve un resumen.
     */
    public static String comprobar(Andar.Mundo m, FortalezaPlan f) {
        return comprobar(m, f, false);
    }

    /** Igual; con {@code soloFuera}, solo los de la montana y su mirador (sin la fortaleza levantada). */
    public static String comprobar(Andar.Mundo m, FortalezaPlan f, boolean soloFuera) {
        double[] pie = f.montana.pie();
        int px = (int) Math.floor(pie[0]), pz = (int) Math.floor(pie[1]);
        Andar.Zona fuera = Andar.desde(m, px, (int) Math.round(pie[2]) + 1, pz, f.cx - 120, f.cz - 120, f.cx + 120, pz + 30, 3_000_000);
        Andar.Zona jardin = soloFuera ? fuera : Andar.desde(m, f.cx, f.Y + 1, f.cz + 40, f.cx - 80, f.cz - 80, f.cx + 80, f.cz + 80, 3_000_000);
        StringBuilder mal = new StringBuilder();
        int bien = 0, sinSitio = 0, lejos = 0, n = 0;
        for (Puesto p : puestos(f)) {
            if (soloFuera && dentro(p.grupo())) continue;
            n++;
            Andar.Zona z = dentro(p.grupo()) ? jardin : fuera;
            boolean hay = false;
            for (int r = 0; r <= 3 && !hay; r++)
                for (int dx = -r; dx <= r && !hay; dx++)
                    for (int dz = -r; dz <= r && !hay; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                        if (z.pisa(p.x() + dx, p.y() - 1, p.z() + dz)) hay = true;
                    }
            if (hay) {
                bien++;
                continue;
            }
            boolean depie = false;
            for (int dy = -3; dy <= 1 && !depie; dy++) {
                var s = m.en(p.x(), p.y() - 1 + dy, p.z());
                depie = !s.isAir() && m.en(p.x(), p.y() + dy, p.z()).isAir() && m.en(p.x(), p.y() + 1 + dy, p.z()).isAir();
            }
            if (depie) lejos++;
            else sinSitio++;
            if (mal.length() < 1500)
                mal.append(String.format(Locale.ROOT, " [%s %s %d,%d,%d %s]", p.grupo(), p.tipo(), p.x() - f.cx, p.y(), p.z() - f.cz, depie ? "no se llega" : "sin sitio"));
        }
        return String.format(Locale.ROOT, "%d puestos: %d bien, %d sin sitio, %d no se llega (montana %d celdas, jardin %d)%s",
                n, bien, sinSitio, lejos, fuera.celdas(), jardin.celdas(), mal);
    }
}
