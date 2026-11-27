package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.personajes.Mercader;
import com.darkcraft.santuario.Santuarios;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Silas, el mercader ambulante de Morvain (parte 7af, respuesta 95 del plan: "mercaderes ambulantes"): va de santuario en
 * santuario por {@link #RUTA}, {@link #ESTANCIA} ticks en cada uno (dos dias del mundo de las nubes: en Morvain la hora no
 * pasa). Donde esta lo dice la hora del mundo ({@link #donde}): no se guarda nada mas que un desfase para los comandos
 * ({@code silas:N} en {@link Morvain.Datos}). Cada segundo ({@link #tick}): con alguien a menos de {@link #CERCA} de su
 * santuario de ahora (y su chunk cargado) se le pone junto al fuego, al otro lado de donde se sienta la guia; los que esten
 * en otro sitio se van (con una bocanada de humo) en cuanto no hay nadie a {@link #DESPIDE} bloques: no se esfuma delante de
 * nadie. A quien le pregunta adonde ira, se lo dice ({@link #rumbo}).
 */
public final class Ambulante {
    private Ambulante() {}

    /** Los santuarios por los que pasa, en orden: el Vado, los Juncos, la Puerta de la Academia, el Costillar, el Mirador y los Ahorcados. */
    public static final String[] RUTA = {"vado", "juncos", "puerta_academia", "costillar", "mirador", "ahorcados"};
    /** Lo que se queda en cada uno (ticks del mundo de las nubes). */
    public static final long ESTANCIA = 48000;
    static final int CERCA = 64, DESPIDE = 12;

    /** Lo que se adelanta (o atrasa) la ruta (comandos y pruebas). */
    public static int desfase(MinecraftServer srv) {
        for (String k : Morvain.Datos.get(srv).hechos)
            if (k.startsWith("silas:")) try {
                return Integer.parseInt(k.substring(6));
            } catch (NumberFormatException ignorado) {
                return 0;
            }
        return 0;
    }

    public static void desfase(MinecraftServer srv, int n) {
        Morvain.Datos d = Morvain.Datos.get(srv);
        List<String> viejos = new ArrayList<>();
        for (String k : d.hechos) if (k.startsWith("silas:")) viejos.add(k);
        for (String k : viejos) d.quitar(k);
        if (n != 0) d.marcar("silas:" + n);
    }

    /** En que punto de la ruta esta ahora (0..RUTA.length - 1). */
    public static int indice(MinecraftServer srv) {
        return (int) Math.floorMod(srv.overworld().getGameTime() / ESTANCIA + desfase(srv), (long) RUTA.length);
    }

    /** El santuario junto al que esta ahora. */
    public static String donde(MinecraftServer srv) {
        return RUTA[indice(srv)];
    }

    /** El siguiente de la ruta. */
    public static String siguiente(MinecraftServer srv) {
        return RUTA[(indice(srv) + 1) % RUTA.length];
    }

    /** Lo que le falta para irse (ticks). */
    public static long quedan(MinecraftServer srv) {
        return ESTANCIA - Math.floorMod(srv.overworld().getGameTime(), ESTANCIA);
    }

    /** Lleva la ruta al punto {@code i} (comandos y pruebas). */
    public static void mover(MinecraftServer srv, int i) {
        int ahora = (int) Math.floorMod(srv.overworld().getGameTime() / ESTANCIA, (long) RUTA.length);
        desfase(srv, Math.floorMod(i - ahora, RUTA.length));
    }

    /** Cada segundo: se va de donde no le toca y se pone donde le toca si hay alguien cerca. */
    static void tick(ServerLevel m) {
        MinecraftServer srv = m.getServer();
        String aqui = donde(srv);
        // los que estan en otro sitio se van (si no hay nadie delante)
        repasar(m, cargados(m));
        SantuariosMorvain.Sitio s = SantuariosMorvain.porId(aqui);
        if (s == null) return;
        BlockPos f = s.pos();
        if (!m.isLoaded(f)) return;
        boolean alguien = false;
        for (ServerPlayer p : m.players())
            if (!p.isSpectator() && p.blockPosition().distSqr(f) < CERCA * CERCA) alguien = true;
        if (!alguien || silas(m, s) != null) return;
        poner(m, s);
    }

    /** De esos, se van los que no estan donde les toca ahora (si no tienen a nadie delante). Devuelve cuantos se fueron. */
    public static int repasar(ServerLevel m, List<Mercader> l) {
        String aqui = donde(m.getServer());
        int n = 0;
        for (Mercader e : l)
            if (!e.isRemoved() && !aqui.equals(e.sitio()) && m.getNearestPlayer(e, DESPIDE) == null) {
                irse(m, e);
                n++;
            }
        return n;
    }

    /** Los Silas que hay cargados cerca de algun jugador. */
    static List<Mercader> cargados(ServerLevel m) {
        List<Mercader> l = new ArrayList<>();
        for (ServerPlayer p : m.players())
            for (Mercader e : m.getEntitiesOfClass(Mercader.class, p.getBoundingBox().inflate(128)))
                if (e.quien() == Mercader.Quien.SILAS && !l.contains(e)) l.add(e);
        return l;
    }

    /** El Silas que esta junto al santuario {@code s} (si esta cargado). */
    public static Mercader silas(ServerLevel m, SantuariosMorvain.Sitio s) {
        for (Mercader e : m.getEntitiesOfClass(Mercader.class, new AABB(s.pos()).inflate(12)))
            if (e.quien() == Mercader.Quien.SILAS) return e;
        return null;
    }

    /** Pone a Silas junto al santuario {@code s}: a 4-5 bloques del fuego, lejos de la guia, mirando hacia fuera. */
    public static Mercader poner(ServerLevel m, SantuariosMorvain.Sitio s) {
        BlockPos f = s.pos();
        BlockPos guia = null;
        for (com.darkcraft.personajes.Guia g : m.getEntitiesOfClass(com.darkcraft.personajes.Guia.class, new AABB(f).inflate(8))) guia = g.blockPosition();
        BlockPos sitio = Santuarios.cerca(m, f, 4, 6, guia, 3.5);
        if (sitio == null) sitio = Santuarios.cerca(m, f, 3, 7, null, 0);
        if (sitio == null) return null;
        BlockPos mira = sitio.offset(sitio.getX() - f.getX(), 0, sitio.getZ() - f.getZ());
        Mercader e = Mercader.ponerSilas(m, sitio, mira, s.id());
        if (e != null) {
            m.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, e.getX(), e.getY() + 1, e.getZ(), 6, 0.3, 0.5, 0.3, 0.01);
            DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: Silas llega al santuario {} ({})", s.id(), sitio.toShortString());
        }
        return e;
    }

    /** Se va: una bocanada de humo y desaparece. */
    static void irse(ServerLevel m, Mercader e) {
        m.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, e.getX(), e.getY() + 1, e.getZ(), 14, 0.35, 0.6, 0.35, 0.02);
        m.sendParticles(ParticleTypes.POOF, e.getX(), e.getY() + 1, e.getZ(), 10, 0.3, 0.6, 0.3, 0.02);
        m.playSound(null, e.getX(), e.getY() + 1, e.getZ(), SoundEvents.WOOL_PLACE, SoundSource.NEUTRAL, 0.6f, 0.8f);
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: Silas se va del santuario {}", e.sitio());
        e.discard();
    }

    /** A quien le pregunta adonde ira: el aviso con el santuario siguiente. */
    public static void rumbo(ServerPlayer sp) {
        SantuariosMorvain.Sitio s = SantuariosMorvain.porId(siguiente(sp.server));
        if (s == null) return;
        com.darkcraft.net.Net.to(sp, new com.darkcraft.net.Net.Cartel(com.darkcraft.net.Net.Cartel.AVISO, "darkcraft.silas.rumbo|" + s.nombre(), 0));
    }

    /** /darkcraft silas [ir | mover &lt;0-5&gt;] (sin mas: donde esta y adonde ira). */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> comando() {
        return net.minecraft.commands.Commands.literal("silas")
                .executes(c -> decir(c.getSource(), resumen(Morvain.nivel(c.getSource().getServer()))))
                .then(net.minecraft.commands.Commands.literal("ir").executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    ServerLevel m = Morvain.nivel(sp.server);
                    if (m == null) return 0;
                    llevar(sp, m);
                    return decir(c.getSource(), resumen(m));
                }))
                .then(net.minecraft.commands.Commands.literal("mover").then(net.minecraft.commands.Commands.argument("n",
                        com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, RUTA.length - 1)).executes(c -> {
                    ServerLevel m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    mover(m.getServer(), com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "n"));
                    return decir(c.getSource(), resumen(m));
                })));
    }

    /** Lleva al jugador junto al santuario donde esta Silas ahora (a 9 bloques, mirando al fuego). */
    public static void llevar(ServerPlayer sp, ServerLevel m) {
        SantuariosMorvain.Sitio s = SantuariosMorvain.porId(donde(sp.server));
        if (s == null) return;
        BlockPos f = s.pos();
        m.getChunk(f.getX() >> 4, f.getZ() >> 4);
        double x = f.getX() + 0.5, z = f.getZ() + 9.5;
        int y = m.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(x), (int) Math.floor(z));
        sp.teleportTo(m, x, y + 0.05, z, 180f, 10f);
    }

    private static int decir(net.minecraft.commands.CommandSourceStack src, String s) {
        src.sendSuccess(() -> net.minecraft.network.chat.Component.literal(s), false);
        return 1;
    }

    /** Para la prueba y el comando: donde esta, lo que le queda y si esta puesto. */
    public static String resumen(ServerLevel m) {
        MinecraftServer srv = m.getServer();
        SantuariosMorvain.Sitio s = SantuariosMorvain.porId(donde(srv));
        Mercader e = s == null ? null : silas(m, s);
        return "en " + donde(srv) + " (" + indice(srv) + " de " + RUTA.length + ", desfase " + desfase(srv) + ", le quedan " + quedan(srv) / 20 + " s)"
                + ", despues " + siguiente(srv) + "; " + (e == null ? "no esta puesto" : "puesto en " + e.blockPosition().toShortString() + " (" + e.sitio() + ")")
                + "; cargados " + cargados(m).size();
    }
}
