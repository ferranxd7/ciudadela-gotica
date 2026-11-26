package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.enemigos.Enemigo;
import com.darkcraft.enemigos.Marchito;
import com.darkcraft.enemigos.Puestos;
import com.darkcraft.enemigos.Puestos.Puesto;
import com.darkcraft.pueblo.gen.Casa;

import java.util.ArrayList;
import java.util.List;

/**
 * Los Marchitos de las Llanuras Palidas (parte 7e): donde espera cada uno ({@link Puestos}, en Morvain).
 * <ul>
 * <li><b>Velmor</b> (sitio {@value #VELMOR}): uno en cada cabeza del puente roto (el primero que se ve llegando del arco,
 * en la orilla de enfrente), por las calles, en la plaza (uno de rodillas ante la horca), rezando en la capilla y en el
 * cementerio, en la taberna, la herreria y la casa del alguacil, por los campos; y dos <b>campaneros</b>: a la puerta de la
 * capilla y en los campos del este. Si un campanero te ve, toca y acuden los de alrededor ({@link Marchito#LLAMADA}).</li>
 * <li><b>Las llanuras</b> (sitio {@value #LLANURAS}): tres que vagan solos entre el Arco del Umbral y Velmor.</li>
 * <li><b>El molino</b> (sitio {@value #MOLINO}, parte 7f): la Molinera de rodillas en medio de la era (un jefe: cae una
 * vez) y dos marchitos por el camino que va de Velmor al molino.</li>
 * <li><b>La Arboleda de los Ahorcados</b> (sitio {@value #ARBOLEDA}, parte 7g, en el Bosque Muerto): los ahorcados que cuelgan
 * de las sogas del Arbol de la Horca, de los arboles de alrededor, de la puerta y del patibulo ({@link Arboleda#colgados}),
 * tres de rodillas al pie del arbol, tres tumbados entre las tumbas, uno junto al patibulo y otro por el camino.</li>
 * <li><b>El patibulo</b> (sitio {@value #PATIBULO}, parte 7h): Maese Odran, el Verdugo, de pie en el tablado apoyado en su hacha
 * (un jefe: cae una vez). Mientras vive, los colgados de la arboleda vuelven a colgar; vencido, la cuenta se cierra y ya no
 * vuelven ({@link Arboleda#liberar}).</li>
 * </ul>
 * Nadie a menos de 18 bloques del arco ni de 14 de un santuario.
 */
public final class MorvainEnemigos {
    private MorvainEnemigos() {}

    public static final String VELMOR = "velmor", LLANURAS = "llanuras", MOLINO = "molino", ARBOLEDA = "arboleda", PATIBULO = "patibulo";
    static final String MARCHITO = "marchito", MOLINERA = "molinera", AHORCADO = "ahorcado", VERDUGO = "verdugo";
    // hacia donde miran (el giro de Minecraft: 0 = sur, 90 = oeste, 180 = norte, -90 = este)
    private static final float SUR = 0f, OESTE = 90f, NORTE = 180f, ESTE = -90f;
    private static final int DE_PIE = Enemigo.DE_PIE, REZA = Enemigo.REZA, TUMBADO = Enemigo.TUMBADO;
    private static final int G = Marchito.GUADANA, F = Marchito.FAROL, C = Marchito.CAMPANA;

    private static final List<Puesto> VELMOR_P = new ArrayList<>(), LLANURAS_P = new ArrayList<>(), MOLINO_P = new ArrayList<>(),
            ARBOLEDA_P = new ArrayList<>(), PATIBULO_P = new ArrayList<>();

    /** Uno fuera, de pie en el suelo de Morvain. */
    private static void fuera(List<Puesto> l, int variante, int x, int z, float giro, int postura, int ronda, String grupo) {
        l.add(new Puesto(MARCHITO, variante, x, Velmor.pie(x, z), z, giro, postura, ronda, grupo));
    }

    /** Uno a una altura dada (el puente, la capilla, dentro de una casa). */
    private static void en(List<Puesto> l, int variante, int x, int y, int z, float giro, int postura, int ronda, String grupo) {
        l.add(new Puesto(MARCHITO, variante, x, y, z, giro, postura, ronda, grupo));
    }

    /** Dentro de una casa: (a, b) en las coordenadas de quien entra. */
    private static void casa(List<Puesto> l, Casa k, int variante, int a, int b, float giro, int postura, String grupo) {
        l.add(new Puesto(MARCHITO, variante, k.lx(a, b), k.y, k.lz(a, b), giro, postura, 0, grupo));
    }

    static synchronized void preparar() {
        if (!VELMOR_P.isEmpty()) return;
        Velmor v = Velmor.get();
        List<Puesto> l = VELMOR_P;
        int t = Velmor.TABLERO + 1, g = v.capillaY;
        // ---- el puente roto: uno en cada cabeza (el de la orilla del oeste mira a quien llega del arco)
        en(l, G, Velmor.PUENTE_X0 + 6, t, Velmor.PUENTE_Z, OESTE, DE_PIE, 0, "puente");
        en(l, F, Velmor.PUENTE_X1 - 4, t, Velmor.PUENTE_Z + 1, OESTE, DE_PIE, 0, "puente");
        // ---- la calle del puente
        fuera(l, G, 790, 4041, ESTE, DE_PIE, 5, "calle");
        fuera(l, G, 818, 4039, OESTE, DE_PIE, 0, "calle");
        fuera(l, G, 905, 4041, ESTE, DE_PIE, 0, "calle");
        fuera(l, G, 940, 4038, OESTE, TUMBADO, 0, "calle");
        // ---- la plaza: el del farol junto al pozo, uno de rodillas ante la horca, otro tirado
        fuera(l, F, 850, 4034, SUR, DE_PIE, 3, "plaza");
        fuera(l, G, 854, 4029, SUR, REZA, 0, "plaza");
        fuera(l, G, 842, 4041, ESTE, TUMBADO, 0, "plaza");
        // ---- la calle mayor
        fuera(l, G, 850, 3995, SUR, DE_PIE, 4, "calle");
        fuera(l, G, 849, 3962, SUR, DE_PIE, 0, "calle");
        fuera(l, G, 851, 4100, NORTE, DE_PIE, 5, "calle");
        fuera(l, F, 850, 4146, SUR, DE_PIE, 0, "puerta_sur");
        // ---- la capilla: el campanero a la puerta, dos rezando en los bancos y uno ante el altar
        en(l, C, 850, g, 3931, SUR, DE_PIE, 0, "capilla");
        en(l, G, 844, g, 3917, NORTE, REZA, 0, "capilla");
        en(l, G, 856, g, 3914, NORTE, REZA, 0, "capilla");
        en(l, F, 850, g, 3909, SUR, DE_PIE, 0, "capilla");
        // ---- el cementerio
        fuera(l, F, 820, 3914, ESTE, DE_PIE, 3, "cementerio");
        fuera(l, G, 817, 3917, SUR, TUMBADO, 0, "cementerio");
        fuera(l, G, 814, 3913, NORTE, REZA, 0, "cementerio");
        // ---- dentro: la taberna (dos sentados a sus jarras), la herreria y la casa del alguacil
        casa(l, v.taberna, G, 2, 3, ESTE, REZA, "taberna");
        casa(l, v.taberna, G, v.taberna.A - 3, 5, OESTE, REZA, "taberna");
        casa(l, v.herreria, G, 3, 4, NORTE, DE_PIE, "herreria");
        casa(l, v.alcaldia, F, 4, 4, SUR, DE_PIE, "alcaldia");
        // ---- los campos: el otro campanero en los del este, y quien sigue segando
        fuera(l, C, 920, 4090, NORTE, DE_PIE, 0, "campos");
        fuera(l, F, 935, 4105, OESTE, DE_PIE, 4, "campos");
        fuera(l, G, 910, 4075, ESTE, REZA, 0, "campos");
        fuera(l, G, 780, 3970, SUR, DE_PIE, 5, "campos");
        fuera(l, G, 771, 3986, ESTE, TUMBADO, 0, "campos");
        // ---- los que vagan por las llanuras, entre el arco y Velmor
        int[][] solos = {{250, 3640}, {420, 3790}, {596, 3905}};
        for (int[] s : solos) {
            int x = s[0], z = s[1];
            LLANURAS_P.add(new Puesto(MARCHITO, G, x, (int) Math.floor(Terreno.columna(x, z).altura()) + 1, z, NORTE, DE_PIE, 8, "llanuras"));
        }
        // ---- el molino (parte 7f): la Molinera de rodillas ante la piedra de moler, mirando al molino (el puesto 0); y
        // por el camino desde Velmor, uno con su farol que mira hacia el pueblo y otro de rodillas mirando al molino
        MOLINO_P.add(new Puesto(MOLINERA, 0, Molino.EX, Molino.Y + 1, Molino.EZ, ESTE, REZA, 0, Puestos.JEFE));
        MOLINO_P.add(new Puesto(MARCHITO, F, 1030, Molino.pie(1030, 4055), 4055, OESTE, DE_PIE, 4, "camino_molino"));
        MOLINO_P.add(new Puesto(MARCHITO, G, 1062, Molino.pie(1062, 4059), 4059, ESTE, REZA, 0, "camino_molino"));
        // ---- la Arboleda de los Ahorcados (parte 7g): los que cuelgan de las sogas (bajo cada una) y los demas
        Arboleda a = Arboleda.get();
        for (Arboleda.Colgado c : a.colgados())
            ARBOLEDA_P.add(new Puesto(AHORCADO, c.variante(), c.x(), c.y(), c.z(), c.giro(), Enemigo.COLGADO, 0, "colgados"));
        for (Arboleda.Otro o : a.otros())
            ARBOLEDA_P.add(new Puesto(AHORCADO, o.variante(), o.x(), o.y(), o.z(), o.giro(), o.postura(), o.ronda(), o.grupo()));
        // ---- el patibulo (parte 7h): el Verdugo de pie en el tablado, apoyado en el hacha, mirando al Arbol de la Horca
        int[] ve = a.sitio("verdugo");
        PATIBULO_P.add(new Puesto(VERDUGO, 0, ve[0], ve[1], ve[2], SUR, REZA, 0, Puestos.JEFE));
    }

    /** El numero del puesto de la Molinera en el sitio del molino. */
    public static int molinera() {
        return 0;
    }

    /** El numero del puesto del Verdugo en el sitio del patibulo. */
    public static int verdugo() {
        return 0;
    }

    /** Lo apunta el mod al arrancar. */
    public static void registrar() {
        Puestos.tipo(MARCHITO, DarkCraftMod.MARCHITO);
        Puestos.tipo(MOLINERA, DarkCraftMod.MOLINERA);
        Puestos.tipo(AHORCADO, DarkCraftMod.AHORCADO);
        Puestos.tipo(VERDUGO, DarkCraftMod.VERDUGO);
        preparar();
        Puestos.registrar(VELMOR, VELMOR_P, Morvain::es);
        Puestos.registrar(LLANURAS, LLANURAS_P, Morvain::es);
        Puestos.registrar(MOLINO, MOLINO_P, Morvain::es);
        // los colgados solo mientras vive el Verdugo (vencido, la cuenta se cierra: ya no vuelven a colgar)
        Puestos.registrar(ARBOLEDA, ARBOLEDA_P, Morvain::es, (l, p) -> !"colgados".equals(p.grupo()) || !Arboleda.cuentaCerrada(l.getServer()));
        Puestos.registrar(PATIBULO, PATIBULO_P, Morvain::es);
        // al caer la Molinera se abre la reja del molino
        Puestos.alCaer((e, quien) -> {
            if (e instanceof com.darkcraft.enemigos.Molinera && MOLINO.equals(e.sitio()) && e.level() instanceof net.minecraft.server.level.ServerLevel sl)
                Molino.puerta(sl, true, true);
            // y al caer el Verdugo, los colgados de la arboleda descansan
            if (e instanceof com.darkcraft.enemigos.Verdugo && PATIBULO.equals(e.sitio()) && e.level() instanceof net.minecraft.server.level.ServerLevel sl)
                Arboleda.liberar(sl);
        });
    }

    public static List<Puesto> molino() {
        preparar();
        return List.copyOf(MOLINO_P);
    }

    public static List<Puesto> arboleda() {
        preparar();
        return List.copyOf(ARBOLEDA_P);
    }

    public static List<Puesto> patibulo() {
        preparar();
        return List.copyOf(PATIBULO_P);
    }

    public static List<Puesto> velmor() {
        preparar();
        return List.copyOf(VELMOR_P);
    }

    public static List<Puesto> llanuras() {
        preparar();
        return List.copyOf(LLANURAS_P);
    }

    // ------------------------------------------------------------------ el comando

    /** {@code /darkcraft velmor [ir <sitio> | enemigos [volver | fuera | sacar]]}. */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comando() {
        return net.minecraft.commands.Commands.literal("velmor")
                .executes(c -> {
                    var src = c.getSource();
                    src.sendSuccess(() -> net.minecraft.network.chat.Component.literal(comprobar() + "; " + estado(src.getServer())), false);
                    return 1;
                })
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> ir(c.getSource(), "pozo"))
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Velmor.get().sitios().keySet(), b))
                                .executes(c -> ir(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("enemigos")
                        .executes(c -> {
                            var src = c.getSource();
                            src.sendSuccess(() -> net.minecraft.network.chat.Component.literal(estado(src.getServer())), false);
                            return 1;
                        })
                        .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), VELMOR, false);
                            Puestos.todos(c.getSource().getServer(), LLANURAS, false);
                            c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("vuelven todos los marchitos"), false);
                            return 1;
                        }))
                        .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), VELMOR, true);
                            Puestos.todos(c.getSource().getServer(), LLANURAS, true);
                            c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("fuera todos los marchitos"), false);
                            return 1;
                        }))
                        .then(net.minecraft.commands.Commands.literal("sacar").executes(c -> {
                            net.minecraft.server.level.ServerLevel m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            int n = Puestos.sacarTodos(m, VELMOR).size();
                            c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("fuera " + n), false);
                            return n;
                        })));
    }

    private static int ir(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Velmor.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Velmor.get().sitios().keySet() + ")"));
            return 0;
        }
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 1.5, 180f, 10f);
        return 1;
    }

    /** Cuantos quedan en pie y cuantos andan fuera. */
    public static String estado(net.minecraft.server.MinecraftServer srv) {
        return "Velmor: quedan " + Puestos.quedan(srv, VELMOR) + " de " + VELMOR_P.size() + " (fuera " + Puestos.fuera(VELMOR) + "); llanuras: quedan "
                + Puestos.quedan(srv, LLANURAS) + " de " + LLANURAS_P.size() + "; molino: quedan " + Puestos.quedan(srv, MOLINO) + " de " + MOLINO_P.size()
                + " (la Molinera " + (Molino.vencida(srv) ? "caida" : Puestos.enemigo(MOLINO, molinera()) != null ? "fuera" : "esperando") + ")";
    }

    /** {@code /darkcraft arboleda [ir <sitio> | enemigos [volver | fuera | sacar]]} (parte 7g). */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoArboleda() {
        return net.minecraft.commands.Commands.literal("arboleda")
                .executes(c -> decir(c, estadoArboleda(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> irArboleda(c.getSource(), "entrada"))
                        .then(net.minecraft.commands.Commands.argument("sitio", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(Arboleda.get().sitios().keySet(), b))
                                .executes(c -> irArboleda(c.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(c, "sitio")))))
                .then(net.minecraft.commands.Commands.literal("enemigos")
                        .executes(c -> decir(c, estadoArboleda(c.getSource().getServer())))
                        .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), ARBOLEDA, false);
                            return decir(c, "vuelven todos los ahorcados (y vuelven a colgar)");
                        }))
                        .then(net.minecraft.commands.Commands.literal("fuera").executes(c -> {
                            Puestos.todos(c.getSource().getServer(), ARBOLEDA, true);
                            return decir(c, "fuera todos los ahorcados");
                        }))
                        .then(net.minecraft.commands.Commands.literal("sacar").executes(c -> {
                            net.minecraft.server.level.ServerLevel m = Morvain.nivel(c.getSource().getServer());
                            if (m == null) return 0;
                            int n = Puestos.sacarTodos(m, ARBOLEDA).size();
                            return decir(c, "fuera " + n);
                        })));
    }

    private static int irArboleda(net.minecraft.commands.CommandSourceStack src, String sitio) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.level.ServerPlayer sp = src.getPlayerOrException();
        int[] s = Arboleda.get().sitio(sitio);
        net.minecraft.server.level.ServerLevel m = Morvain.nivel(src.getServer());
        if (s == null || m == null) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("no hay sitio " + sitio + " (" + Arboleda.get().sitios().keySet() + ")"));
            return 0;
        }
        // mirando al arbol de la horca
        float giro = (float) (Math.toDegrees(Math.atan2(Arboleda.GZ - s[2], Arboleda.GX - s[0])) - 90.0);
        sp.teleportTo(m, s[0] + 0.5, s[1] + 0.05, s[2] + 0.5, giro, 5f);
        return 1;
    }

    /** Cuantos ahorcados quedan y cuantos andan fuera (y cuantos cuelgan todavia). */
    public static String estadoArboleda(net.minecraft.server.MinecraftServer srv) {
        int cuelgan = 0;
        var sitio = Puestos.sitio(ARBOLEDA);
        if (sitio != null)
            for (int i = 0; i < ARBOLEDA_P.size(); i++)
                if (Puestos.enemigo(ARBOLEDA, i) instanceof com.darkcraft.enemigos.Ahorcado ah && ah.cuelga()) cuelgan++;
        String s = "arboleda: quedan " + Puestos.quedan(srv, ARBOLEDA) + " de " + ARBOLEDA_P.size() + " (fuera " + Puestos.fuera(ARBOLEDA) + ", colgados "
                + cuelgan + ")";
        var m = Morvain.nivel(srv);
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(Arboleda.PX, 80, Arboleda.PZ))) s += "; " + Arboleda.resumen(m);
        return s;
    }

    /** {@code /darkcraft molinera [ir | vencer | volver]}: como esta la Molinera, llevar ante la era, vencerla o que vuelva. */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoMolinera() {
        return net.minecraft.commands.Commands.literal("molinera")
                .executes(c -> decir(c, estadoMolinera(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> {
                    var sp = c.getSource().getPlayerOrException();
                    var m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    int[] e = Molino.get().sitio("entrada");
                    sp.teleportTo(m, e[0] + 0.5, e[1] + 0.05, e[2] + 0.5, -90f, 8f);
                    return decir(c, estadoMolinera(c.getSource().getServer()));
                }))
                .then(net.minecraft.commands.Commands.literal("vencer").executes(c -> {
                    var srv = c.getSource().getServer();
                    Enemigo e = Puestos.enemigo(MOLINO, molinera());
                    if (e != null) e.kill();
                    else {
                        Puestos.marcar(srv, MOLINO, molinera(), true);
                        if (c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) com.darkcraft.story.Logros.dar(sp, "molinera");
                    }
                    var m = Morvain.nivel(srv);
                    if (m != null) Molino.puerta(m, true, true);
                    return decir(c, estadoMolinera(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                    var srv = c.getSource().getServer();
                    Puestos.marcar(srv, MOLINO, molinera(), false);
                    var m = Morvain.nivel(srv);
                    if (m != null) Molino.puerta(m, false, false);
                    return decir(c, estadoMolinera(srv));
                }));
    }

    /** {@code /darkcraft verdugo [ir | vencer | volver]}: como esta el Verdugo, llevar ante el patibulo, vencerle o que vuelva. */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comandoVerdugo() {
        return net.minecraft.commands.Commands.literal("verdugo")
                .executes(c -> decir(c, estadoVerdugo(c.getSource().getServer())))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> {
                    var sp = c.getSource().getPlayerOrException();
                    var m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    // al claro, entre el arbol y el patibulo, mirando al patibulo
                    int[] e = Arboleda.get().sitio("ante_patibulo");
                    sp.teleportTo(m, e[0] + 0.5, e[1] + 0.05, e[2] + 0.5, 180f, -8f);
                    return decir(c, estadoVerdugo(c.getSource().getServer()));
                }))
                .then(net.minecraft.commands.Commands.literal("vencer").executes(c -> {
                    var srv = c.getSource().getServer();
                    Enemigo e = Puestos.enemigo(PATIBULO, verdugo());
                    if (e != null) e.kill();
                    else {
                        Puestos.marcar(srv, PATIBULO, verdugo(), true);
                        if (c.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) com.darkcraft.story.Logros.dar(sp, "verdugo");
                        var m = Morvain.nivel(srv);
                        if (m != null) Arboleda.liberar(m);
                    }
                    return decir(c, estadoVerdugo(srv));
                }))
                .then(net.minecraft.commands.Commands.literal("volver").executes(c -> {
                    var srv = c.getSource().getServer();
                    Puestos.marcar(srv, PATIBULO, verdugo(), false);
                    // los colgados vuelven a su sitio (y a colgar) en cuanto salgan
                    Puestos.todos(srv, ARBOLEDA, false);
                    return decir(c, estadoVerdugo(srv));
                }));
    }

    /** Como esta el Verdugo (para el comando y las pruebas). */
    public static String estadoVerdugo(net.minecraft.server.MinecraftServer srv) {
        Enemigo e = Puestos.enemigo(PATIBULO, verdugo());
        String s = "el Verdugo: " + (Arboleda.cuentaCerrada(srv) ? "caido (la cuenta esta cerrada)" : e == null ? "esperando (no ha salido)" : "fuera");
        if (e instanceof com.darkcraft.enemigos.Verdugo v) s += ", " + v.estado();
        var m = Morvain.nivel(srv);
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(Arboleda.PX, 80, Arboleda.PZ))) s += "; " + Arboleda.sogasColgados(m);
        return s;
    }

    private static int decir(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> c, String s) {
        c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(s), false);
        return 1;
    }

    /** Como esta la Molinera (para el comando y las pruebas). */
    public static String estadoMolinera(net.minecraft.server.MinecraftServer srv) {
        Enemigo e = Puestos.enemigo(MOLINO, molinera());
        String s = "la Molinera: " + (Molino.vencida(srv) ? "caida" : e == null ? "esperando (no ha salido)" : "fuera");
        if (e instanceof com.darkcraft.enemigos.Molinera mo)
            s += String.format(java.util.Locale.ROOT, ", vida %.0f/%.0f, postura %d, accion %d, pelea %s, segunda %s, suyos %d, en %.1f %.1f %.1f",
                    mo.getHealth(), mo.getMaxHealth(), mo.postura(), mo.accion(), mo.enPelea(), mo.segunda(), mo.suyos(), mo.getX(), mo.getY(), mo.getZ());
        var m = Morvain.nivel(srv);
        if (m != null && m.isLoaded(new net.minecraft.core.BlockPos(Molino.MX, Molino.Y, Molino.MZ))) s += "; " + Molino.resumen(m);
        return s;
    }

    /** Para la prueba: que ningun puesto quede cerca del arco ni de un santuario. */
    public static String comprobar() {
        preparar();
        int cerca = 0;
        List<Puesto> todos = new ArrayList<>(VELMOR_P);
        todos.addAll(LLANURAS_P);
        todos.addAll(MOLINO_P);
        todos.addAll(ARBOLEDA_P);
        todos.addAll(PATIBULO_P);
        for (Puesto p : todos) {
            if (Math.hypot(p.x() - Llegada.X, p.z() - Llegada.Z) < 18) cerca++;
            for (SantuariosMorvain.Sitio s : SantuariosMorvain.todos()) if (Math.hypot(p.x() - s.x(), p.z() - s.z()) < 14) cerca++;
        }
        int camp = 0;
        for (Puesto p : VELMOR_P) if (p.variante() == C) camp++;
        int colg = 0;
        for (Puesto p : ARBOLEDA_P) if (p.postura() == Enemigo.COLGADO) colg++;
        return VELMOR_P.size() + " en Velmor (" + camp + " campaneros), " + LLANURAS_P.size() + " por las llanuras, " + MOLINO_P.size()
                + " en el molino, " + ARBOLEDA_P.size() + " en la arboleda (" + colg + " colgados) y " + PATIBULO_P.size()
                + " en el patibulo; cerca del arco o de un santuario " + cerca;
    }
}
