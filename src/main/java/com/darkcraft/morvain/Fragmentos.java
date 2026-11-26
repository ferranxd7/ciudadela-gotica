package com.darkcraft.morvain;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.net.Net;
import com.darkcraft.rol.Ficha;
import com.darkcraft.story.Logros;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Los fragmentos del mapa de Morvain (parte 7d, respuesta 92 del plan: "fragmentos de mapa"). En cada region hay una
 * estela del mapa ({@link EstelaBlock}): la de las Llanuras en la plaza del Arco del Umbral, al empezar el camino del
 * norte; las demas en la tarima de uno de sus santuarios, a tres bloques al norte del fuego y mirandolo. Quien la usa
 * copia el mapa de esa region: se apunta en su ficha ({@link Ficha#mapaNuevo}), cartel "Fragmento de mapa", logros
 * `fragmento` (el primero) y `mapa_morvain` (todos), y su cliente lo sabe (`Net.Mapas`): el mapa de la M ensena esa
 * region dibujada en el pergamino (`client/morvain/MapaMorvain`); las demas, solo lo que se ha andado, entre la niebla.
 * El Mar Muerto se ve siempre (la silueta de la costa).
 */
@Mod.EventBusSubscriber(modid = DarkCraftMod.MODID)
public final class Fragmentos {
    private Fragmentos() {}

    /** Una estela: su region y donde esta el bloque (el pergamino mira al sur). */
    public record Estela(Region region, int x, int y, int z) {
        public BlockPos pos() {
            return new BlockPos(x, y, z);
        }
    }

    /** El santuario junto al que esta la estela de cada region (la de las Llanuras va en la plaza del arco). */
    private static final String[] JUNTO = {"ahorcados", "embarcadero", "colmillo", "puerta_academia", "puerta_capital", "costillar",
            "grieta", "ancla_sur"};

    /** Las nueve estelas (una por region de tierra), en el orden de las regiones. */
    public static final Estela[] ESTELAS;

    static {
        List<Estela> l = new ArrayList<>();
        l.add(new Estela(Region.LLANURAS, Llegada.X, Llegada.Y + 1, Llegada.Z - 11));
        for (String id : JUNTO) {
            SantuariosMorvain.Sitio s = SantuariosMorvain.porId(id);
            l.add(new Estela(s.region(), s.x(), s.y() + 1, s.z() - 3));
        }
        l.sort((a, b) -> Integer.compare(a.region.ordinal(), b.region.ordinal()));
        ESTELAS = l.toArray(new Estela[0]);
    }

    /** Para la prueba: fragmentos copiados desde que arranco el juego. */
    public static int TOMADOS;

    /** La estela de la tarima de este santuario (o null). */
    static Estela de(SantuariosMorvain.Sitio s) {
        for (Estela e : ESTELAS) if (e.x == s.x() && e.z == s.z() - 3 && e.y == s.y() + 1) return e;
        return null;
    }

    /** La estela que esta en pos (o null). */
    public static Estela en(BlockPos pos) {
        for (Estela e : ESTELAS) if (e.x == pos.getX() && e.y == pos.getY() && e.z == pos.getZ()) return e;
        return null;
    }

    public static Estela de(Region r) {
        for (Estela e : ESTELAS) if (e.region == r) return e;
        return null;
    }

    /** El bloque de la estela, mirando al sur (en las vistas sin juego, uno de Minecraft que se le parece). */
    static BlockState bloque() {
        try {
            return DarkCraftMod.ESTELA_MAPA.get().defaultBlockState().setValue(EstelaBlock.FACING, Direction.SOUTH);
        } catch (Throwable t) {
            return Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
        }
    }

    // ------------------------------------------------------------------ lo de cada jugador

    /** Las regiones cuyo mapa tiene (sus ids, en el orden de las regiones). */
    public static List<String> mapas(ServerPlayer sp) {
        Set<String> s = Ficha.mapas(sp);
        List<String> r = new ArrayList<>();
        for (Estela e : ESTELAS) if (s.contains(e.region.id)) r.add(e.region.id);
        return r;
    }

    /** Le manda a su cliente los mapas que tiene. */
    public static void avisar(ServerPlayer sp) {
        Net.to(sp, new Net.Mapas(mapas(sp)));
    }

    @SubscribeEvent
    public static void onEntrar(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) avisar(sp);
    }

    @SubscribeEvent
    public static void onCambio(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp && e.getTo() == Morvain.NIVEL) avisar(sp);
    }

    /** El jugador usa la estela de pos (clic derecho o la tecla del aviso). */
    public static void usar(ServerPlayer sp, BlockPos pos) {
        if (!sp.isAlive() || sp.isSpectator() || !Morvain.es(sp.level())) return;
        Estela e = en(pos);
        if (e == null || pos.distToCenterSqr(sp.position()) > 5.5 * 5.5) return;
        if (!Ficha.mapaNuevo(sp, e.region.id)) {
            Net.to(sp, new Net.Cartel(Net.Cartel.AVISO, "darkcraft.estela.ya", 0));
            return;
        }
        ServerLevel l = sp.serverLevel();
        double x = pos.getX() + 0.5, y = pos.getY() + 1.0, z = pos.getZ() + 0.5;
        l.playSound(null, x, y, z, DarkCraftMod.SFX_CODICE_PAGINA.get(), SoundSource.BLOCKS, 1.0f, 0.85f);
        l.playSound(null, x, y, z, DarkCraftMod.SFX_PIEZA.get(), SoundSource.BLOCKS, 0.8f, 1.1f);
        l.sendParticles(ParticleTypes.ENCHANT, x, y + 0.2, z, 60, 0.4, 0.5, 0.4, 0.6);
        l.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z - 0.6, 12, 0.25, 0.3, 0.1, 0.01);
        Net.to(sp, new Net.Cartel(Net.Cartel.FRAGMENTO, "darkcraft.zona.morvain." + e.region.id, mapas(sp).size()));
        Logros.dar(sp, "fragmento");
        if (mapas(sp).size() >= ESTELAS.length) Logros.dar(sp, "mapa_morvain");
        avisar(sp);
        TOMADOS++;
        DarkCraftMod.LOGGER.info("[DarkCraft] {} copia el mapa de {} ({} de {})", sp.getGameProfile().getName(), e.region.id, mapas(sp).size(),
                ESTELAS.length);
    }

    // ------------------------------------------------------------------ el comando (para probar)

    /**
     * /darkcraft mapa: los que tiene y donde estan las estelas; {@code dar <region>|todos} se los da sin cartel;
     * {@code quitar} se los quita; {@code ir <region>} le lleva delante de la estela de esa region (desde donde sea).
     */
    public static LiteralArgumentBuilder<CommandSourceStack> comando() {
        return Commands.literal("mapa")
                .executes(c -> lista(c.getSource()))
                .then(Commands.literal("lista").executes(c -> lista(c.getSource())))
                .then(Commands.literal("quitar").executes(c -> {
                    ServerPlayer sp = c.getSource().getPlayerOrException();
                    int n = Ficha.olvidarMapas(sp);
                    avisar(sp);
                    c.getSource().sendSuccess(() -> Component.literal("quitados " + n), false);
                    return 1;
                }))
                .then(Commands.literal("dar").then(Commands.argument("region", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(regiones(true), b))
                        .executes(c -> {
                            ServerPlayer sp = c.getSource().getPlayerOrException();
                            String r = StringArgumentType.getString(c, "region");
                            int n = 0;
                            for (Estela e : ESTELAS)
                                if (r.equals("todos") || e.region.id.equals(r)) {
                                    Ficha.mapaNuevo(sp, e.region.id);
                                    n++;
                                }
                            avisar(sp);
                            int k = n;
                            c.getSource().sendSuccess(() -> Component.literal("dados " + k + " (ahora " + mapas(sp).size() + ")"), false);
                            return n > 0 ? 1 : 0;
                        })))
                .then(Commands.literal("ir").then(Commands.argument("region", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(regiones(false), b))
                        .executes(c -> {
                            ServerPlayer sp = c.getSource().getPlayerOrException();
                            Estela e = null;
                            for (Estela q : ESTELAS) if (q.region.id.equals(StringArgumentType.getString(c, "region"))) e = q;
                            ServerLevel m = Morvain.nivel(sp.server);
                            if (e == null || m == null) return 0;
                            m.getChunk(e.x >> 4, e.z >> 4);
                            if (sp.getVehicle() instanceof com.darkcraft.montura.Corcel k) k.quitar();
                            sp.teleportTo(m, e.x + 0.5, e.y, e.z + 2.5, 180f, 15f);
                            return 1;
                        })));
    }

    private static List<String> regiones(boolean todos) {
        List<String> l = new ArrayList<>();
        if (todos) l.add("todos");
        for (Estela e : ESTELAS) l.add(e.region.id);
        return l;
    }

    private static int lista(CommandSourceStack src) {
        ServerPlayer sp = src.getPlayer();
        if (sp == null) return 0;
        List<String> t = mapas(sp);
        src.sendSuccess(() -> Component.literal("mapas de Morvain: tiene " + t.size() + " de " + ESTELAS.length), false);
        for (Estela e : ESTELAS)
            src.sendSuccess(() -> Component.literal((t.contains(e.region.id) ? " * " : "   ") + e.region.id + " " + e.pos().toShortString() + " ")
                    .append(Component.translatable("darkcraft.zona.morvain." + e.region.id)), false);
        return 1;
    }

    /** Una linea para las pruebas. */
    public static String resumen(ServerPlayer sp) {
        return "mapas " + mapas(sp) + " (" + mapas(sp).size() + " de " + ESTELAS.length + "), copiados " + TOMADOS;
    }
}
