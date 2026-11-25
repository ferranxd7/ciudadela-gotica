package com.darkcraft.valdrenn;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Espectro;
import com.darkcraft.enemigos.Guardia;
import com.darkcraft.enemigos.Puestos;
import com.darkcraft.enemigos.Puestos.Puesto;
import com.darkcraft.gen.CastlePlan;
import com.darkcraft.pueblo.gen.Andar;
import com.darkcraft.rey.ReyEstado;
import com.darkcraft.world.CastleSites;
import net.minecraft.core.BlockPos;
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
 * Los enemigos de cada Reino de Valdrenn (parte 4: "muchisimos enemigos, como en Dark Souls"). Estan en sus puestos
 * ({@link Puestos}: salen al acercarse, no vuelven hasta descansar o morir) y los puestos van en coordenadas del reino
 * (el centro del castillo; +z es el sur, donde esta la puerta): el puente desde la Torre de Ascenso, el camino y la
 * puerta exterior, los arqueros de las murallas y de las torres, el camino mayor, el cuartel y el patio de armas, la
 * puerta interior, el patio de la fuente y la puerta del torreon, el pueblo, el cementerio y los edificios del norte y
 * del este. Cada reino se apunta solo cuando alguien se acerca ({@code valdrenn@x,z}).
 * <p>
 * Tras caer el Rey se van los puestos marcados con {@link #REY} y tras caer Ser Gavran los de {@link #GAVRAN}: el
 * reino queda mas tranquilo (lo pidio el usuario).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class ValdrennEnemigos {
    private ValdrennEnemigos() {}

    public static final String GUARDIA = "guardia", ARQUERO = "arquero", PERRO = "perro", ESPECTRO = "espectro", ELITE = "elite";
    /** Los dos guardianes (parte 4d). */
    public static final String BRANDT = "brandt", CORTEZA = "corteza";
    /** Lo que se anade al grupo de los puestos que se van al caer el Rey o Ser Gavran. */
    public static final String REY = "~rey", GAVRAN = "~gavran";
    public static final String PREFIJO = "valdrenn@";
    /** Los puestos de los grupos que acaban asi estan donde no se llega andando (a proposito). */
    static final String ALTO = "_en_alto";
    /** Los de la torre del arbol: tras la puerta de la Llave del Arbol (no se llega andando hasta abrirla). */
    static final String TRAS_PUERTA = "_tras_puerta";
    /** Hacia donde miran (giro de Minecraft: 0 al sur, 90 al oeste, 180 al norte, -90 al este). */
    static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;
    static final int Y = CastlePlan.Y0, MURALLA = CastlePlan.Y0 + 26, MURALLA_INT = CastlePlan.Y0 + 36;
    private static final int DE_PIE = Enemigo.DE_PIE, REZA = Enemigo.REZA, TUMBADO = Enemigo.TUMBADO;

    /** Un puesto en coordenadas del reino. {@code quita}: 0 siempre; 1 se va al caer el Rey; 2 al caer Ser Gavran. */
    public record Local(String tipo, int variante, int lx, int y, int lz, float giro, int postura, int ronda, String grupo, int quita) {}

    /** Los puestos de un reino, en coordenadas del reino. {@code sur}: donde acaba el puente de la Torre de Ascenso (lz). */
    public static List<Local> locales(int sur) {
        List<Local> l = new ArrayList<>();
        int E = Guardia.ESPADA, A = Guardia.ALABARDA, B = Guardia.BALLESTA;
        // ---- el puente de la Torre de Ascenso: dos al final, donde se pisa la isla (los primeros que se ven)
        l.add(new Local(GUARDIA, E, -3, Y, sur - 6, SUR, DE_PIE, 0, "puente", 0));
        l.add(new Local(GUARDIA, E, 3, Y, sur - 8, SUR, DE_PIE, 0, "puente", 0));
        // ---- el camino hasta la puerta exterior
        l.add(new Local(GUARDIA, A, 0, Y, Math.min(sur - 20, 252), SUR, DE_PIE, 3, "camino", 1));
        l.add(new Local(GUARDIA, A, -4, Y, 232, SUR, DE_PIE, 0, "puerta", 0));
        l.add(new Local(GUARDIA, A, 4, Y, 232, SUR, DE_PIE, 0, "puerta", 0));
        // ---- arqueros: asomados a las almenas de la muralla exterior y en lo alto de la puerta (en el tejado, que da al
        //      camino: desde ahi ven el camino desde 12 bloques de la puerta hasta el puente; ahi arriba no se sube, a
        //      proposito: hay que pasar corriendo). De pie EN el hueco de la almena (sobre el antepecho), no en el adarve:
        //      desde el adarve el muro les tapa todo lo de abajo. Los huecos caen en las x con x % 4 == 3 o 0.
        for (int s : new int[]{-1, 1}) {
            l.add(new Local(ARQUERO, 0, 36 * s, MURALLA + 1, 220, SUR, DE_PIE, 0, "muralla", 0));
            l.add(new Local(ARQUERO, 0, 60 * s, MURALLA + 1, 220, SUR, DE_PIE, 0, "muralla", 1));
            l.add(new Local(ARQUERO, 0, 10 * s, Y + 33, 228, SUR, DE_PIE, 0, "puerta" + ALTO, 2));
        }
        // ---- el camino mayor, entre las dos puertas
        l.add(new Local(GUARDIA, E, 0, Y, 192, SUR, DE_PIE, 4, "camino_mayor", 1));
        l.add(new Local(GUARDIA, A, -3, Y, 166, SUR, DE_PIE, 0, "camino_mayor", 0));
        l.add(new Local(GUARDIA, B, 3, Y, 172, SUR, DE_PIE, 0, "camino_mayor", 2));
        // ---- el cuartel (puerta al norte) y el patio de armas (entrenando de dos en dos)
        l.add(new Local(GUARDIA, E, 51, Y, 147, NORTE, DE_PIE, 0, "cuartel", 1));
        l.add(new Local(GUARDIA, E, 95, Y, 156, ESTE, DE_PIE, 0, "patio_armas", 1));
        l.add(new Local(GUARDIA, E, 99, Y, 156, OESTE, DE_PIE, 0, "patio_armas", 1));
        l.add(new Local(GUARDIA, A, 95, Y, 166, ESTE, DE_PIE, 0, "patio_armas", 2));
        l.add(new Local(GUARDIA, E, 99, Y, 166, OESTE, DE_PIE, 0, "patio_armas", 2));
        l.add(new Local(GUARDIA, A, 109, Y, 161, OESTE, DE_PIE, 0, "patio_armas", 0));
        // ---- la puerta interior, y arqueros en su muralla y en las almenas de su tejado (ven todo el camino mayor)
        l.add(new Local(GUARDIA, A, -4, Y, 146, SUR, DE_PIE, 0, "puerta_interior", 0));
        l.add(new Local(GUARDIA, A, 4, Y, 146, SUR, DE_PIE, 0, "puerta_interior", 0));
        for (int s : new int[]{-1, 1}) l.add(new Local(ARQUERO, 0, 6 * s, MURALLA_INT + 1, 142, SUR, DE_PIE, 0, "puerta_interior_alta", 1));
        for (int s : new int[]{-1, 1}) {
            l.add(new Local(ARQUERO, 0, 36 * s, MURALLA_INT + 1, 134, SUR, DE_PIE, 0, "muralla_interior", 0));
            l.add(new Local(ARQUERO, 0, 60 * s, MURALLA_INT + 1, 134, SUR, DE_PIE, 0, "muralla_interior", 1));
        }
        // ---- el patio de la fuente y la puerta del torreon
        l.add(new Local(GUARDIA, E, -6, Y, 110, SUR, DE_PIE, 0, "patio", 0));
        l.add(new Local(GUARDIA, E, 6, Y, 110, SUR, DE_PIE, 0, "patio", 1));
        l.add(new Local(GUARDIA, A, 0, Y, 96, SUR, DE_PIE, 3, "patio", 0));
        l.add(new Local(GUARDIA, B, -26, Y, 78, ESTE, DE_PIE, 0, "patio", 2));
        l.add(new Local(GUARDIA, B, 26, Y, 78, OESTE, DE_PIE, 0, "patio", 2));
        l.add(new Local(GUARDIA, E, 0, Y, 58, SUR, DE_PIE, 4, "patio", 1));
        l.add(new Local(GUARDIA, A, -5, Y, 40, SUR, DE_PIE, 0, "torreon", 0));
        l.add(new Local(GUARDIA, A, 5, Y, 40, SUR, DE_PIE, 0, "torreon", 0));
        l.add(new Local(GUARDIA, E, -3, Y, 29, SUR, DE_PIE, 0, "torreon", 0));
        l.add(new Local(GUARDIA, E, 3, Y, 29, SUR, DE_PIE, 0, "torreon", 0));
        // ---- la ronda interior (la calle que rodea la muralla interior): dos que patrullan
        l.add(new Local(GUARDIA, E, -100, Y, 140, ESTE, DE_PIE, 6, "ronda", 1));
        l.add(new Local(GUARDIA, A, 100, Y, 140, OESTE, DE_PIE, 6, "ronda", 1));
        l.add(new Local(GUARDIA, E, 140, Y, -60, NORTE, DE_PIE, 6, "ronda", 1));
        l.add(new Local(GUARDIA, E, -140, Y, 60, SUR, DE_PIE, 6, "ronda", 1));
        // ---- el pueblo: en los cruces de las calles
        int[][] pueblo = {{-67, 188, E, 5}, {-84, 154, A, 0}, {86, 188, B, 0}, {-33, 188, E, 4}, {35, 188, A, 4},
                {-154, 103, E, 5}, {154, 103, A, 5}, {-171, 120, B, 0}, {171, -84, E, 5}, {-120, -171, A, 5}, {120, -171, E, 0}};
        for (int[] p : pueblo) l.add(new Local(GUARDIA, p[2], p[0], Y, p[1], SUR, DE_PIE, p[3], "pueblo", 1));
        // ---- el cementerio: velando de rodillas ante las tumbas, y alguno tirado entre los muertos
        l.add(new Local(GUARDIA, E, -160, Y, -30, OESTE, REZA, 0, "cementerio", 1));
        l.add(new Local(GUARDIA, A, -158, Y, 32, OESTE, REZA, 0, "cementerio", 1));
        l.add(new Local(GUARDIA, E, -176, Y, -40, SUR, TUMBADO, 0, "cementerio", 2));
        l.add(new Local(GUARDIA, E, -174, Y, 44, NORTE, TUMBADO, 0, "cementerio", 2));
        l.add(new Local(ARQUERO, 0, -164, Y, 0, ESTE, REZA, 0, "cementerio", 0));
        // ---- los edificios del este (cuadras y fragua) y del norte
        l.add(new Local(GUARDIA, E, 146, Y, -20, OESTE, DE_PIE, 0, "este", 1));
        l.add(new Local(GUARDIA, B, 146, Y, 40, OESTE, DE_PIE, 0, "este", 2));
        l.add(new Local(GUARDIA, E, -28, Y, -165, SUR, DE_PIE, 0, "norte", 1));
        l.add(new Local(GUARDIA, A, 29, Y, -167, SUR, DE_PIE, 0, "norte", 2));
        // ================================================================ parte 4b: perros de hueso y espectros de la corte
        // ---- las jaurias: dormidas junto al cuartel, sueltas por las calles, entre los muertos del cementerio, en el
        //      laberinto de setos y a la puerta de las cuadras
        int P = 0;
        l.add(new Local(PERRO, P, 75, Y, 150, OESTE, REZA, 0, "perrera", 1));
        l.add(new Local(PERRO, P, 78, Y, 153, OESTE, REZA, 0, "perrera", 1));
        l.add(new Local(PERRO, P, 75, Y, 156, OESTE, REZA, 0, "perrera", 2));
        int[][] jaurias = {{-101, 171, 2}, {101, -120, 2}, {-150, -137, 2}, {137, 154, 3}};
        for (int[] j : jaurias)
            for (int k = 0; k < j[2]; k++)
                l.add(new Local(PERRO, P, j[0] + 2 * k, Y, j[1] + 2 * k, k % 2 == 0 ? SUR : ESTE, DE_PIE, 4, "jauria", k == 0 ? 0 : 1));
        l.add(new Local(PERRO, P, -190, Y, 20, ESTE, TUMBADO, 0, "cementerio", 1));
        l.add(new Local(PERRO, P, -186, Y, -10, ESTE, TUMBADO, 0, "cementerio", 2));
        l.add(new Local(PERRO, P, 79, Y, -171, SUR, DE_PIE, 3, "laberinto", 0));
        l.add(new Local(PERRO, P, 81, Y, -169, SUR, DE_PIE, 3, "laberinto", 1));
        l.add(new Local(PERRO, P, 77, Y, -173, SUR, DE_PIE, 3, "laberinto", 2));
        l.add(new Local(PERRO, P, 148, Y, 10, OESTE, DE_PIE, 0, "cuadras", 1));
        l.add(new Local(PERRO, P, 148, Y, 14, OESTE, DE_PIE, 0, "cuadras", 2));
        // ---- la catedral: rezando en el pasillo de la nave, uno en el altar y dos que rondan por las naves de los lados
        int C = Espectro.ESTOQUE, D = Espectro.GARRAS;
        l.add(new Local(ESPECTRO, D, 92, Y, -10, NORTE, REZA, 0, "catedral", 0));
        l.add(new Local(ESPECTRO, C, 92, Y, 30, NORTE, REZA, 0, "catedral", 1));
        l.add(new Local(ESPECTRO, D, 92, Y + 1, -78, SUR, DE_PIE, 0, "catedral", 0));
        l.add(new Local(ESPECTRO, C, 72, Y, 0, SUR, DE_PIE, 5, "catedral", 1));
        l.add(new Local(ESPECTRO, C, 112, Y, -30, NORTE, DE_PIE, 5, "catedral", 2));
        // ================================================================ parte 4c: capitanes de elite y emboscadas
        // ---- los capitanes: al mando de la puerta exterior, del patio de armas y del patio de la fuente (el de la torre
        //      del arbol, en arbol(plan)); al verte arengan a los suyos y, si caen, los suyos se desaniman
        l.add(new Local(ELITE, 0, 0, Y, 239, SUR, DE_PIE, 0, "puerta", 0));
        l.add(new Local(ELITE, 0, 104, Y, 170, NORTE, DE_PIE, 0, "patio_armas", 1));
        l.add(new Local(ELITE, 0, 0, Y, 48, SUR, DE_PIE, 2, "patio", 0));
        // ---- las emboscadas: no estan hasta que alguien entra en su zona (emboscadas(plan)); salen de la tierra
        String EM = Emboscadas.GRUPO;
        // el puente: dos perros por detras (desde la torre) y un alabardero delante
        l.add(new Local(PERRO, P, -1, Y, sur + 29, NORTE, TUMBADO, 0, EM + "puente", 1));
        l.add(new Local(PERRO, P, 1, Y, sur + 31, NORTE, TUMBADO, 0, EM + "puente", 1));
        l.add(new Local(GUARDIA, A, 0, Y, sur - 1, SUR, TUMBADO, 0, EM + "puente", 0));
        // la plaza: cuatro alrededor de la Fuente Mayor
        l.add(new Local(GUARDIA, E, -12, Y, 70, ESTE, TUMBADO, 0, EM + "plaza", 0));
        l.add(new Local(GUARDIA, E, 12, Y, 86, OESTE, TUMBADO, 0, EM + "plaza", 0));
        l.add(new Local(GUARDIA, A, 12, Y, 70, OESTE, TUMBADO, 0, EM + "plaza", 1));
        l.add(new Local(PERRO, P, -12, Y, 86, ESTE, TUMBADO, 0, EM + "plaza", 1));
        // el cementerio: entre las tumbas del lado del pueblo (entre el mausoleo y la tapia), alrededor del arquero que
        //      reza, los muertos se levantan
        l.add(new Local(GUARDIA, E, -158, Y, -9, SUR, TUMBADO, 0, EM + "cementerio", 0));
        l.add(new Local(GUARDIA, A, -155, Y, 5, OESTE, TUMBADO, 0, EM + "cementerio", 0));
        l.add(new Local(GUARDIA, E, -161, Y, 10, NORTE, TUMBADO, 0, EM + "cementerio", 1));
        l.add(new Local(PERRO, P, -154, Y, -3, OESTE, TUMBADO, 0, EM + "cementerio", 1));
        // la biblioteca: tres espectros entre las estanterias de la sala grande
        l.add(new Local(ESPECTRO, C, -100, Y, -24, ESTE, TUMBADO, 0, EM + "biblioteca", 0));
        l.add(new Local(ESPECTRO, C, -86, Y, -36, OESTE, TUMBADO, 0, EM + "biblioteca", 1));
        l.add(new Local(ESPECTRO, D, -86, Y, -22, OESTE, TUMBADO, 0, EM + "biblioteca", 0));
        return l;
    }

    /** Las zonas que disparan las emboscadas de un reino, en el mundo (sus puestos: los grupos emb_*). */
    public static List<Emboscadas.Emboscada> emboscadas(CastlePlan plan) {
        List<Emboscadas.Emboscada> l = new ArrayList<>();
        int sur = sur(plan);
        if (plan.cine().asc()) l.add(new Emboscadas.Emboscada("puente", caja(plan, -4, Y - 3, sur + 12, 4, Y + 6, sur + 24)));
        l.add(new Emboscadas.Emboscada("plaza", caja(plan, -11, Y - 2, 66, 11, Y + 5, 90)));
        int[] e = escalera(plan);
        if (e != null) l.add(new Emboscadas.Emboscada("escalera", new net.minecraft.world.phys.AABB(e[0] - 4.5, e[4] + 1, e[1] - 4.5, e[0] + 5.5, e[5] - 0.5, e[1] + 5.5)));
        l.add(new Emboscadas.Emboscada("cementerio", caja(plan, -166, Y - 2, -6, -152, Y + 5, 6)));
        l.add(new Emboscadas.Emboscada("biblioteca", caja(plan, -100, Y - 1, -37, -86, Y + 5, -23)));
        return l;
    }

    private static net.minecraft.world.phys.AABB caja(CastlePlan plan, int x0, int y0, int z0, int x1, int y1, int z1) {
        return new net.minecraft.world.phys.AABB(plan.cx + x0, y0, plan.cz + z0, plan.cx + x1 + 1, y1, plan.cz + z1 + 1);
    }

    /**
     * La escalera de caracol del torreon (su torreta): {x, z, sx, sz, suelo de la planta 2, suelo de la planta 3} en el
     * mundo (sx, sz: hacia donde da su puerta), o null si no la tiene.
     */
    static int[] escalera(CastlePlan plan) {
        var keep = plan.torreon();
        if (keep == null) return null;
        int[] t = keep.turretInfo(), sg = keep.turretSigns();
        if (t == null || sg == null) return null;
        int y2 = Integer.MIN_VALUE, y3 = Integer.MIN_VALUE;
        for (var f : keep.plantas()) {
            if (f.index == 2) y2 = f.y;
            if (f.index == 3) y3 = f.y;
        }
        if (y2 == Integer.MIN_VALUE || y3 == Integer.MIN_VALUE) return null;
        return new int[]{t[0], t[1], sg[0], sg[1], y2, y3};
    }

    /**
     * La emboscada de la escalera del torreon (depende del plano): subiendo de la segunda planta a la tercera salen dos
     * caballeros en el rellano de arriba y un perro en el de abajo (en el lado contrario a la puerta de cada rellano).
     */
    public static List<Local> deLaEscalera(CastlePlan plan) {
        List<Local> l = new ArrayList<>();
        int[] e = escalera(plan);
        if (e == null) return l;
        int lx = e[0] - plan.cx, lz = e[1] - plan.cz, sx = e[2], sz = e[3];
        String g = Emboscadas.GRUPO + "escalera";
        l.add(new Local(GUARDIA, Guardia.ESPADA, lx - 3 * sx, e[5], lz, sx > 0 ? ESTE : OESTE, TUMBADO, 0, g, 0));
        l.add(new Local(GUARDIA, Guardia.ESPADA, lx, e[5], lz - 3 * sz, sz > 0 ? SUR : NORTE, TUMBADO, 0, g, 1));
        l.add(new Local(PERRO, 0, lx - 3 * sx, e[4], lz, sx > 0 ? ESTE : OESTE, TUMBADO, 0, g, 1));
        return l;
    }

    /**
     * La torre del arbol por dentro (parte 4c): en los rellanos de su escalera, uno de cada dos, de abajo arriba: un
     * caballero, dos perros, un alabardero, una dama de la corte y, en el ultimo, el capitan de la torre. Se van al caer
     * Ser Gavran.
     */
    public static List<Local> arbol(CastlePlan plan) {
        List<Local> l = new ArrayList<>();
        com.darkcraft.gen.TreeTower tr = plan.arbol();
        if (tr == null) return l;
        List<int[]> esc = tr.escalera();
        List<int[]> rellanos = new ArrayList<>();
        for (int i = 1; i < esc.size(); i++) if (esc.get(i)[4] == 1) rellanos.add(esc.get(i));
        String g = "torre_arbol" + TRAS_PUERTA;
        int k = 0;
        for (int i = 1; i < rellanos.size(); i += 2) {
            int[] r = rellanos.get(i);
            // mirando escalera abajo (por donde llega quien sube)
            float giro = switch (r[3]) {
                case 0 -> SUR;
                case 1 -> ESTE;
                case 2 -> NORTE;
                default -> OESTE;
            };
            int lx = tr.centerX() + r[0] - plan.cx, lz = tr.centerZ() + r[1] - plan.cz, y = r[2];
            int sx = Integer.signum(r[0]), sz = Integer.signum(r[1]);
            boolean ultimo = i + 2 >= rellanos.size();
            if (ultimo) l.add(new Local(ELITE, 0, lx, y, lz, giro, DE_PIE, 0, g, 2));
            else switch (k++ % 4) {
                case 0 -> l.add(new Local(GUARDIA, Guardia.ESPADA, lx, y, lz, giro, DE_PIE, 0, g, 2));
                case 1 -> {
                    l.add(new Local(PERRO, 0, lx, y, lz, giro, REZA, 0, g, 2));
                    l.add(new Local(PERRO, 0, lx - sx, y, lz - sz, giro, REZA, 0, g, 2));
                }
                case 2 -> l.add(new Local(GUARDIA, Guardia.ALABARDA, lx, y, lz, giro, DE_PIE, 0, g, 2));
                default -> l.add(new Local(ESPECTRO, Espectro.GARRAS, lx, y, lz, giro, DE_PIE, 0, g, 2));
            }
        }
        return l;
    }

    /**
     * Los de dentro del torreon (parte 4b): salen de sus salas (de cada planta, segun lo que sea cada una), asi que
     * dependen del plano. En coordenadas del reino, como los de fuera.
     */
    public static List<Local> interiores(CastlePlan plan) {
        List<Local> todo = new ArrayList<>();
        com.darkcraft.gen.Building keep = plan.torreon();
        if (keep == null) return todo;
        int C = Espectro.ESTOQUE, D = Espectro.GARRAS, E = Guardia.ESPADA, A = Guardia.ALABARDA;
        for (var f : keep.plantas()) {
            // en cada planta, como mucho estas salas con gente (la planta baja, todas); de cada clase de sala, por turnos
            // (el primer sotano, las bodegas, no: llenas de toneles y sin paso desde arriba en muchos planos)
            int tope = f.index == 0 ? 99 : f.index == 1 ? 5 : f.index == 2 ? 4 : f.index > 2 ? 3 : f.index == -1 ? 0 : 3;
            String g = "torreon_" + (f.index < 0 ? "sotano" + (-f.index) : "planta" + f.index);
            java.util.Map<String, List<List<Local>>> porClase = new java.util.TreeMap<>();
            int n = 0;
            for (var r : f.rooms) {
                var q = r.r;
                int w = q.x1 - q.x0, h = q.z1 - q.z0;
                if (w < 4 || h < 4) continue;
                // a un tercio y a dos tercios de la sala (en el centro suele haber una mesa)
                int ax = q.x0 + w / 3 - plan.cx, bx = q.x0 + 2 * w / 3 - plan.cx;
                int mz = q.z0 + h / 2 - plan.cz, az = q.z0 + h / 3 - plan.cz;
                float giro = (n++ % 2 == 0) ? SUR : NORTE;
                List<Local> sala = new ArrayList<>();
                String clase = r.type.name();
                switch (r.type) {
                    case GREAT_HALL -> {
                        // (desde la parte 4d lo guarda Ser Brandt: guardianes(plan))
                    }
                    case DINING -> {
                        sala.add(new Local(ESPECTRO, C, ax, f.y, az, giro, DE_PIE, 2, g, 0));
                        sala.add(new Local(ESPECTRO, D, bx, f.y, az, giro, DE_PIE, 2, g, 1));
                    }
                    case KITCHEN, SERVANTS, CELLAR, WINE_CELLAR -> {
                        sala.add(new Local(PERRO, 0, ax, f.y, mz, giro, REZA, 0, g, 0));
                        sala.add(new Local(PERRO, 0, bx, f.y, mz, giro, REZA, 0, g, 1));
                        clase = "PERROS";
                    }
                    case LIVING, GALLERY, BALLROOM -> sala.add(new Local(ESPECTRO, n % 2 == 0 ? C : D, ax, f.y, mz, giro, DE_PIE, 3, g, n % 2));
                    case ARMORY, TREASURY -> {
                        sala.add(new Local(GUARDIA, E, ax, f.y, mz, giro, DE_PIE, 0, g, 0));
                        if (w >= 8) sala.add(new Local(GUARDIA, A, bx, f.y, mz, giro, DE_PIE, 0, g, 1));
                        clase = "GUARDIA";
                    }
                    case CHAPEL, CRYPT -> sala.add(new Local(ESPECTRO, D, ax, f.y, mz, NORTE, REZA, 0, g, 0));
                    case LIBRARY, STUDY, ALCHEMY -> {
                        sala.add(new Local(ESPECTRO, C, ax, f.y, mz, giro, DE_PIE, 2, g, n % 2));
                        clase = "ESTUDIO";
                    }
                    case BEDROOM -> sala.add(new Local(ESPECTRO, D, ax, f.y, mz, giro, TUMBADO, 0, g, 1));
                    case PRISON -> sala.add(new Local(PERRO, 0, ax, f.y, mz, giro, TUMBADO, 0, g, 0));
                    default -> {
                    }
                }
                if (!sala.isEmpty()) porClase.computeIfAbsent(clase, k -> new ArrayList<>()).add(sala);
            }
            // por turnos: una sala de cada clase, luego otra de cada... sin dos salas a menos de 10 bloques
            List<List<Local>> elegidas = new ArrayList<>();
            List<List<List<Local>>> colas = new ArrayList<>(porClase.values());
            for (var c : colas) c.sort(java.util.Comparator.comparingInt(sl -> Math.floorMod(sl.get(0).lx() * 31 + sl.get(0).lz() * 17, 97)));
            boolean algo = true;
            for (int ronda = 0; algo && elegidas.size() < tope; ronda++) {
                algo = false;
                for (var c : colas) {
                    if (ronda >= c.size() || elegidas.size() >= tope) continue;
                    algo = true;
                    List<Local> sala = c.get(ronda);
                    boolean cerca = false;
                    for (var o : elegidas)
                        if (Math.hypot(o.get(0).lx() - sala.get(0).lx(), o.get(0).lz() - sala.get(0).lz()) < 10) cerca = true;
                    if (!cerca) elegidas.add(sala);
                }
            }
            for (var sala : elegidas) todo.addAll(sala);
        }
        return todo;
    }

    /** Todos los puestos del reino: los de fuera, los del torreon, la emboscada de su escalera, la torre del arbol y los guardianes. */
    public static List<Local> todos(CastlePlan plan) {
        List<Local> l = new ArrayList<>(locales(sur(plan)));
        l.addAll(interiores(plan));
        l.addAll(deLaEscalera(plan));
        l.addAll(arbol(plan));
        l.addAll(guardianes(plan));
        return l;
    }

    /** La puerta del salon del trono que da al gran salon (la del muro del sur): {x, z, alto} en el mundo, o null. */
    static int[] puertaDelTrono(CastlePlan plan) {
        CastlePlan.Throne t = plan.throne();
        if (t == null || t.doors().isEmpty()) return null;
        int[] mejor = null;
        for (int[] d : t.doors()) if (mejor == null || d[1] > mejor[1]) mejor = d;
        return mejor;
    }

    /**
     * Los dos guardianes (parte 4d), en el grupo de los jefes (caen una vez): Ser Brandt, de rodillas en el gran salon,
     * siete bloques delante de la puerta del trono, de espaldas a ella; se va al caer el Rey. Vareth, de rodillas en la base
     * de la torre del arbol, al norte del ascensor, mirando a la entrada; se va al caer Ser Gavran.
     */
    public static List<Local> guardianes(CastlePlan plan) {
        List<Local> l = new ArrayList<>();
        int[] d = puertaDelTrono(plan);
        if (d != null) l.add(new Local(BRANDT, 0, d[0] - plan.cx, Y, d[1] - plan.cz + 7, SUR, REZA, 0, Puestos.JEFE, 1));
        com.darkcraft.gen.TreeTower tr = plan.arbol();
        if (tr != null) l.add(new Local(CORTEZA, 0, tr.centerX() - plan.cx, tr.base(), tr.centerZ() - plan.cz - 6, SUR, REZA, 0, Puestos.JEFE, 2));
        return l;
    }

    /** Lo que cierra el sello de Ser Brandt: las puertas del salon del trono, todas, de arriba abajo. */
    public static List<BlockPos> selloTrono(CastlePlan plan) {
        List<BlockPos> l = new ArrayList<>();
        CastlePlan.Throne t = plan.throne();
        if (t == null) return l;
        for (int[] d : t.doors()) for (int y = t.y(); y < t.y() + d[2]; y++) l.add(new BlockPos(d[0], y, d[1]));
        return l;
    }

    /** Lo que cierra el sello de Vareth: el primer peldano de la escalera de la torre del arbol y la entrada del ascensor. */
    public static List<BlockPos> selloArbol(CastlePlan plan) {
        List<BlockPos> l = new ArrayList<>();
        com.darkcraft.gen.TreeTower tr = plan.arbol();
        if (tr == null) return l;
        int cx = tr.centerX(), cz = tr.centerZ(), b = tr.base();
        for (int x = 9; x <= 11; x++) for (int y = b + 1; y <= b + 4; y++) l.add(new BlockPos(cx + x, y, cz + 7));
        for (int x = -1; x <= 1; x++) for (int y = b; y <= b + 2; y++) l.add(new BlockPos(cx + x, y, cz + com.darkcraft.gen.TreeTower.SHAFT_Z + 3));
        return l;
    }

    /** Donde acaba el puente de la Torre de Ascenso, en el reino (lz); sin torre, la plazoleta de delante de la puerta. */
    public static int sur(CastlePlan plan) {
        CastlePlan.Cine c = plan.cine();
        // la torre esta a radio + 13 del centro y su puente llega a radio - 34
        return c.asc() ? (c.ascZ() - plan.cz) - 13 - 34 : CastlePlan.OCT + 40;
    }

    /** Los puestos de un reino, ya en coordenadas del mundo. */
    public static List<Puesto> puestos(CastlePlan plan) {
        List<Puesto> r = new ArrayList<>();
        for (Local p : todos(plan)) {
            String g = p.grupo() + (p.quita() == 1 ? REY : p.quita() == 2 ? GAVRAN : "");
            r.add(new Puesto(p.tipo(), p.variante(), plan.cx + p.lx(), p.y(), plan.cz + p.lz(), p.giro(), p.postura(), p.ronda(), g));
        }
        return r;
    }

    public static String clave(CastlePlan plan) {
        return PREFIJO + plan.cx + "," + plan.cz;
    }

    // ------------------------------------------------------------------ apuntar cada reino cuando alguien se acerca

    @SubscribeEvent
    public static void onServidor(TickEvent.ServerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || ev.getServer().getTickCount() % 40 != 13) return;
        ServerLevel lvl = ev.getServer().overworld();
        for (ServerPlayer p : lvl.players()) {
            int[] s = CastleSites.nearest(lvl.getSeed(), p.getBlockX(), p.getBlockZ(), 1);
            if (s == null || Math.hypot(s[0] - p.getX(), s[1] - p.getZ()) > CastlePlan.FOOTPRINT + 160) continue;
            String k = PREFIJO + s[0] + "," + s[1];
            if (Puestos.apuntado(k)) continue;
            CastlePlan plan = com.darkcraft.story.Story.planAt(lvl, s[0], s[1]);
            if (plan == null) continue;
            apuntar(plan);
        }
    }

    /** Apunta los puestos de ese reino (si ya lo estaban, los vuelve a apuntar). */
    public static void apuntar(CastlePlan plan) {
        String k = clave(plan);
        final int cx = plan.cx, cz = plan.cz;
        String gavran = com.darkcraft.gavran.SalaArbol.de(plan) == null ? null : com.darkcraft.gavran.SalaArbol.de(plan).key();
        Emboscadas.apuntar(k, emboscadas(plan));
        Puestos.registrar(k, puestos(plan), l -> l.dimension() == Level.OVERWORLD, (l, p) -> {
            if (!Emboscadas.disparada(k, p.grupo())) return false;
            if (p.grupo().endsWith(REY)) return ReyEstado.get(l.getServer()).get(cx + "," + cz) != ReyEstado.MUERTO;
            if (p.grupo().endsWith(GAVRAN)) return gavran == null || ReyEstado.get(l.getServer()).get(gavran) != ReyEstado.MUERTO;
            return true;
        });
        Guardianes.apuntar(k, plan);
        DarkCraftMod.LOGGER.info("[DarkCraft] Valdrenn en {}, {}: {} puestos de enemigos", cx, cz, Puestos.sitio(k).puestos.size());
    }

    /** El reino que hay alrededor de ese sitio (apuntandolo si hace falta), o null. */
    public static String reino(ServerLevel lvl, BlockPos cerca) {
        int[] s = CastleSites.nearest(lvl.getSeed(), cerca.getX(), cerca.getZ(), 1);
        if (s == null) return null;
        String k = PREFIJO + s[0] + "," + s[1];
        if (!Puestos.apuntado(k)) {
            CastlePlan plan = com.darkcraft.story.Story.planAt(lvl, s[0], s[1]);
            if (plan == null) return null;
            apuntar(plan);
        }
        return k;
    }

    @SubscribeEvent
    public static void onParada(ServerStoppedEvent e) {
        // (los reinos se apuntan segun el mundo: con otro mundo, otros)
        Puestos.olvidar(PREFIJO);
    }

    /** Lo apunta el mod al arrancar: los tipos de enemigo de los puestos de Valdrenn. */
    public static void registrar() {
        Puestos.tipo(GUARDIA, DarkCraftMod.GUARDIA);
        Puestos.tipo(ARQUERO, DarkCraftMod.ARQUERO);
        Puestos.tipo(PERRO, DarkCraftMod.PERRO);
        Puestos.tipo(ESPECTRO, DarkCraftMod.ESPECTRO);
        Puestos.tipo(ELITE, DarkCraftMod.ELITE);
        Puestos.tipo(BRANDT, DarkCraftMod.BRANDT);
        Puestos.tipo(CORTEZA, DarkCraftMod.CORTEZA);
        Puestos.alVolver(Emboscadas::rearmar);
        Puestos.alCaer(Emboscadas::alCaer);
        Puestos.alCaer(Guardianes::alCaer);
    }

    // ------------------------------------------------------------------ comandos: /darkcraft valdrenn enemigos ...

    /** Cuantos quedan en pie en ese reino y cuantos hay fuera. */
    public static String estado(net.minecraft.server.MinecraftServer srv, String k) {
        var s = Puestos.sitio(k);
        if (s == null) return "Valdrenn: sin apuntar";
        int rey = 0, gavran = 0;
        for (Puesto p : s.puestos) {
            if (p.grupo().endsWith(REY)) rey++;
            else if (p.grupo().endsWith(GAVRAN)) gavran++;
        }
        return k + ": " + Puestos.quedan(srv, k) + " de " + s.puestos.size() + " en pie (" + Puestos.fuera(k) + " a la vista); se van "
                + rey + " al caer el Rey y " + gavran + " al caer Ser Gavran";
    }

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comando() {
        return net.minecraft.commands.Commands.literal("enemigos")
                .executes(c -> con(c, (p, k) -> c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(estado(p.server, k)), false)))
                .then(net.minecraft.commands.Commands.literal("volver").executes(c -> con(c, (p, k) -> {
                    Puestos.todos(p.server, k, false);
                    c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(estado(p.server, k)), false);
                })))
                .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> con(c, (p, k) -> {
                    Puestos.todos(p.server, k, true);
                    c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(estado(p.server, k)), false);
                })))
                .then(net.minecraft.commands.Commands.literal("sacar").executes(c -> con(c, (p, k) -> {
                    int n = Puestos.sacarTodos(p.serverLevel(), k).size();
                    c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(n + " fuera; " + estado(p.server, k)), false);
                })))
                .then(net.minecraft.commands.Commands.literal("emboscada")
                        .executes(c -> con(c, (p, k) -> {
                            StringBuilder b = new StringBuilder();
                            for (var e : Emboscadas.de(k)) b.append(' ').append(e.nombre()).append(Emboscadas.disparadas(k).contains(e.nombre()) ? " (disparada)" : "");
                            c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("Emboscadas:" + b), false);
                        }))
                        .then(net.minecraft.commands.Commands.literal("armar").executes(c -> con(c, (p, k) -> {
                            Emboscadas.armar(k);
                            c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("Emboscadas armadas"), false);
                        })))
                        .then(net.minecraft.commands.Commands.argument("nombre", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .executes(c -> con(c, (p, k) -> {
                                    String n = com.mojang.brigadier.arguments.StringArgumentType.getString(c, "nombre");
                                    int salen = Emboscadas.disparar(p.serverLevel(), k, n, p);
                                    c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(salen < 0 ? "No hay ninguna emboscada " + n : "Emboscada " + n + ": salen " + salen), false);
                                }))))
                .then(net.minecraft.commands.Commands.literal("ir").then(net.minecraft.commands.Commands.argument("grupo", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .executes(c -> con(c, (p, k) -> {
                            String g = com.mojang.brigadier.arguments.StringArgumentType.getString(c, "grupo");
                            for (Puesto q : Puestos.sitio(k).puestos)
                                if (q.grupo().equals(g) || q.grupo().startsWith(g + "~")) {
                                    // a unos pasos, mirandole
                                    double dx = -Math.sin(Math.toRadians(q.giro())) * 7, dz = Math.cos(Math.toRadians(q.giro())) * 7;
                                    p.teleportTo(p.serverLevel(), q.x() + 0.5 + dx, q.y() + 0.1, q.z() + 0.5 + dz, q.giro() + 180f, 0f);
                                    return;
                                }
                            c.getSource().sendFailure(net.minecraft.network.chat.Component.literal("No hay puestos del grupo " + g));
                        }))));
    }

    private static int con(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> c,
                           java.util.function.BiConsumer<ServerPlayer, String> f) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String k = p.level().dimension() == Level.OVERWORLD ? reino(p.serverLevel(), p.blockPosition()) : null;
        if (k == null) {
            c.getSource().sendFailure(net.minecraft.network.chat.Component.literal("No hay ningun Reino de Valdrenn cerca"));
            return 0;
        }
        f.accept(p, k);
        return 1;
    }

    // ------------------------------------------------------------------ comprobarlo sin abrir el juego

    /**
     * Para {@code tools/vista3d} y las pruebas: cada puesto, si se puede estar de pie en el (o a menos de tres bloques:
     * en el juego se busca sitio al lado) y si se llega andando desde donde acaba el puente. Devuelve un resumen.
     */
    public static String comprobar(Andar.Mundo m, CastlePlan plan) {
        int sur = sur(plan);
        int cx = plan.cx, cz = plan.cz;
        StringBuilder mal = new StringBuilder();
        int bien = 0, sinSitio = 0, lejos = 0, enAlto = 0, trasPuerta = 0, n = 0;
        // (hasta la torre de ascenso: el puente entero, donde salen los perros de la emboscada)
        Andar.Zona zona = Andar.desde(m, cx, Y, cz + sur - 4, cx - 260, cz - 260, cx + 260, cz + sur + 42, 4_000_000);
        for (Local p : todos(plan)) {
            n++;
            int[] sitio = null;
            for (int r = 0; r <= 3 && sitio == null; r++)
                for (int dx = -r; dx <= r && sitio == null; dx++)
                    for (int dz = -r; dz <= r && sitio == null; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                        if (zona.pisa(cx + p.lx() + dx, p.y() - 1, cz + p.lz() + dz)) sitio = new int[]{dx, dz};
                    }
            if (sitio == null) {
                // no se llega: o no hay donde ponerse, o no se llega andando
                boolean pie = false;
                for (int dy = -3; dy <= 1 && !pie; dy++) {
                    var s = m.en(cx + p.lx(), p.y() - 1 + dy, cz + p.lz());
                    pie = !s.isAir() && m.en(cx + p.lx(), p.y() + dy, cz + p.lz()).isAir() && m.en(cx + p.lx(), p.y() + 1 + dy, cz + p.lz()).isAir();
                }
                if (pie && p.grupo().endsWith(ALTO)) {
                    enAlto++;
                    continue;
                }
                if (pie && p.grupo().endsWith(TRAS_PUERTA)) {
                    trasPuerta++;
                    continue;
                }
                if (pie) lejos++;
                else sinSitio++;
                if (mal.length() < 1500) mal.append(String.format(Locale.ROOT, " [%s %s %d,%d,%d %s]", p.grupo(), p.tipo(), p.lx(), p.y(), p.lz(), pie ? "no se llega" : "sin sitio"));
            } else bien++;
        }
        // lo que ve cada arquero del camino de la puerta (del puente a la puerta interior, cada 4 bloques) a tiro: los de lo
        // alto de las puertas tienen que ver casi todo el suyo; los de las almenas de las murallas, lo que tienen delante
        StringBuilder ven = new StringBuilder();
        for (Local p : todos(plan)) {
            if (!p.tipo().equals(ARQUERO) || p.postura() != DE_PIE) continue;
            int k = 0, total = 0;
            double ox = cx + p.lx() + 0.5, oy = p.y() + 1.74, oz = cz + p.lz() + 0.5;
            for (int z = 146; z <= sur; z += 4) {
                if (Math.abs(z - 217) <= 12 || Math.abs(z - 130) <= 13) continue; // bajo las puertas
                int suelo = Y - 1;
                for (int y = Y + 2; y >= Y - 8; y--)
                    if (solido(m.en(cx, y, cz + z))) {
                        suelo = y;
                        break;
                    }
                double tx = cx + 0.5, ty = suelo + 1 + 1.6, tz = cz + z + 0.5;
                double h = Math.hypot(tx - ox, tz - oz), d3 = Math.sqrt(h * h + (ty - oy) * (ty - oy));
                if (h > 52 || d3 > 64) continue;
                total++;
                if (seVe(m, ox, oy, oz, tx, ty, tz)) k++;
            }
            ven.append(String.format(Locale.ROOT, " %s %d,%d: %d/%d;", p.grupo(), p.lx(), p.lz(), k, total));
        }
        return String.format(Locale.ROOT, "%d puestos (sur %d): %d bien, %d en alto (sin camino, a proposito), %d tras la puerta del arbol, %d sin sitio, %d no se llega; recorrido %d celdas%s; del camino a tiro, lo que ve cada arquero:%s",
                n, sur, bien, enAlto, trasPuerta, sinSitio, lejos, zona.celdas(), mal, ven);
    }

    private static boolean solido(net.minecraft.world.level.block.state.BlockState s) {
        return !s.isAir() && !s.getCollisionShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, net.minecraft.core.BlockPos.ZERO).isEmpty();
    }

    /** Si la linea de un ojo a otro no pasa por ningun bloque con forma (en pasos de una decima de bloque). */
    private static boolean seVe(Andar.Mundo m, double x0, double y0, double z0, double x1, double y1, double z1) {
        double dx = x1 - x0, dy = y1 - y0, dz = z1 - z0, l = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int pasos = (int) Math.ceil(l / 0.1);
        int ux = Integer.MIN_VALUE, uy = 0, uz = 0;
        for (int i = 1; i < pasos; i++) {
            double t = (double) i / pasos;
            int bx = (int) Math.floor(x0 + dx * t), by = (int) Math.floor(y0 + dy * t), bz = (int) Math.floor(z0 + dz * t);
            if (bx == ux && by == uy && bz == uz) continue;
            ux = bx;
            uy = by;
            uz = bz;
            if (solido(m.en(bx, by, bz))) return false;
        }
        return true;
    }
}
