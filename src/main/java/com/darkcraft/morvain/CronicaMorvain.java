package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.gen.Canvas;
import com.darkcraft.story.Story;
import com.darkcraft.story.StoryEvents;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

/**
 * La cronica de Morvain (parte 7ae): los cofres de los capitulos 7-10, que cuenta Maelis con su voz. El generador de Morvain no
 * escribe datos de bloque, asi que se ponen una vez en tiempo de juego ({@link Morvain.Datos}, "cronica7"...), la primera vez
 * que alguien pasa cerca con su chunk cargado: el 7 junto al santuario del Arco del Umbral (al otro lado de Maelis), el 8 ante la
 * estatua caida de la plaza del Juramento de la Capital, el 9 en la choza de Aldric (el Cerco del Ancla, junto a la puerta) y el
 * 10 en la capilla negra de la torre del homenaje, ante el altar. Lo demas (el libro de cada jugador, la cinematica con los
 * planos de {@link PlanosCronica}, el logro) es lo de siempre de {@link StoryEvents}.
 */
public final class CronicaMorvain {
    private CronicaMorvain() {}

    /** A cuanto tiene que estar alguien para que se ponga su cofre. */
    static final int CERCA = 64;

    /** Donde va el cofre de cada capitulo de Morvain (7-10); null si no es de Morvain. */
    public static BlockPos sitio(int ch) {
        return switch (ch) {
            case 7 -> new BlockPos(Llegada.X - 2, Llegada.Y + 1, Llegada.Z - 1);
            case 8 -> new BlockPos(Capital.CX + Capital.PLAZA_U - 4, Capital.PISO, Capital.CZ);
            case 9 -> new BlockPos(Cerco.CX + 17, Cerco.PISO, Cerco.CZ + 18);
            case 10 -> new BlockPos(-1, Homenaje.pisa(Homenaje.CAPILLA), -13);
            default -> null;
        };
    }

    /** Hacia donde mira (por donde se abre) el cofre de cada capitulo. */
    public static Direction mira(int ch) {
        return switch (ch) {
            case 8 -> Direction.WEST;
            case 9 -> Direction.EAST;
            default -> Direction.SOUTH;
        };
    }

    /** Donde ponerse para abrirlo (los pies, mirando al cofre): para los comandos y las pruebas. */
    public static double[] delante(int ch) {
        BlockPos p = sitio(ch);
        Direction d = mira(ch);
        double x = p.getX() + 0.5 + d.getStepX() * 1.6, z = p.getZ() + 0.5 + d.getStepZ() * 1.6;
        return new double[]{x, p.getY(), z, d.getOpposite().toYRot()};
    }

    private static String clave(int ch) {
        return "cronica" + ch;
    }

    /** Si ya esta puesto el cofre de ese capitulo. */
    public static boolean puesto(MinecraftServer srv, int ch) {
        return Morvain.Datos.get(srv).hecho(clave(ch));
    }

    /** Cada segundo (desde {@link Morvain#onTick}): los cofres que falten, si hay alguien cerca con su chunk cargado. */
    static void tick(ServerLevel m) {
        Morvain.Datos d = Morvain.Datos.get(m.getServer());
        for (int ch = Story.MORVAIN; ch <= Story.CHAPTERS; ch++) {
            if (d.hecho(clave(ch))) continue;
            BlockPos p = sitio(ch);
            if (!m.isLoaded(p) || !m.isLoaded(p.below())) continue;
            boolean alguien = false;
            for (ServerPlayer q : m.players())
                if (q.blockPosition().distSqr(p) < CERCA * CERCA) {
                    alguien = true;
                    break;
                }
            if (alguien) poner(m, ch, d);
        }
    }

    /** Pone el cofre si ya esta lo de debajo (el suelo de la plaza o de la sala). */
    private static boolean poner(ServerLevel m, int ch, Morvain.Datos d) {
        BlockPos p = sitio(ch);
        if (m.getBlockState(p.below()).getCollisionShape(m, p.below()).isEmpty()) return false;
        StoryEvents.placeChapter(m, ch, p, mira(ch));
        d.marcar(clave(ch));
        DarkCraftMod.LOGGER.info("[DarkCraft] Morvain: el cofre del capitulo {} de la cronica, en {}", ch, p.toShortString());
        return true;
    }

    /** Pone ya los que falten (cargando sus chunks): para el comando y las pruebas. Devuelve cuantos hay puestos. */
    public static int ponerTodos(ServerLevel m) {
        Morvain.Datos d = Morvain.Datos.get(m.getServer());
        int n = 0;
        for (int ch = Story.MORVAIN; ch <= Story.CHAPTERS; ch++) {
            BlockPos p = sitio(ch);
            m.getChunk(p.getX() >> 4, p.getZ() >> 4);
            if (!d.hecho(clave(ch))) poner(m, ch, d);
            if (d.hecho(clave(ch))) n++;
        }
        return n;
    }

    /** Los olvida y quita sus cofres (si su chunk esta cargado): asi se vuelven a poner. */
    public static void olvidar(ServerLevel m) {
        Morvain.Datos d = Morvain.Datos.get(m.getServer());
        for (int ch = Story.MORVAIN; ch <= Story.CHAPTERS; ch++) {
            d.quitar(clave(ch));
            BlockPos p = sitio(ch);
            if (m.isLoaded(p) && StoryEvents.chapterAt(m, p) == ch) m.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    /** Para la prueba y el comando: cada capitulo, si esta puesto y si el cofre esta en su sitio con su marca. */
    public static String resumen(ServerLevel m) {
        StringBuilder b = new StringBuilder();
        for (int ch = Story.MORVAIN; ch <= Story.CHAPTERS; ch++) {
            BlockPos p = sitio(ch);
            String cofre = "?";
            if (m.isLoaded(p)) {
                BlockEntity be = m.getBlockEntity(p);
                cofre = be instanceof ChestBlockEntity ? "cofre marca " + be.getPersistentData().getInt(Canvas.STORY_TAG) : "sin cofre";
            }
            if (b.length() > 0) b.append("; ");
            b.append(ch).append(": ").append(puesto(m.getServer(), ch) ? "puesto" : "sin poner").append(" en ").append(p.toShortString())
                    .append(" (").append(cofre).append(")");
        }
        return b.toString();
    }

    // ------------------------------------------------------------------ comando

    /** /darkcraft cronica [ir <7-10> | poner | olvidar] (sin mas: como estan). */
    public static LiteralArgumentBuilder<CommandSourceStack> comando() {
        return Commands.literal("cronica")
                .executes(c -> decir(c.getSource(), resumen(Morvain.nivel(c.getSource().getServer()))))
                .then(Commands.literal("ir").then(Commands.argument("n", IntegerArgumentType.integer(Story.MORVAIN, Story.CHAPTERS)).executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    ServerLevel m = Morvain.nivel(sp.server);
                    if (m == null) return 0;
                    int ch = IntegerArgumentType.getInteger(c, "n");
                    double[] q = delante(ch);
                    m.getChunk(((int) Math.floor(q[0])) >> 4, ((int) Math.floor(q[2])) >> 4);
                    sp.teleportTo(m, q[0], q[1] + 0.05, q[2], (float) q[3], 15f);
                    return decir(c.getSource(), Component.translatable("darkcraft.cronica.ir", ch).getString());
                })))
                .then(Commands.literal("poner").executes(c -> {
                    ServerLevel m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    ponerTodos(m);
                    return decir(c.getSource(), resumen(m));
                }))
                .then(Commands.literal("olvidar").executes(c -> {
                    ServerLevel m = Morvain.nivel(c.getSource().getServer());
                    if (m == null) return 0;
                    olvidar(m);
                    return decir(c.getSource(), resumen(m));
                }));
    }

    private static int decir(CommandSourceStack src, String s) {
        src.sendSuccess(() -> Component.literal(s), false);
        return 1;
    }
}
