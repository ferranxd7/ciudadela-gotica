package com.ciudadela.rey;

import com.ciudadela.CiudadelaMod;
import com.ciudadela.net.Net;
import com.ciudadela.story.StoryEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

/**
 * Al morir el Rey: se abren las puertas y del suelo sube, en un haz de luz dorada, el cofre del
 * capitulo 3 (con el libro, la llave del arbol, las armas del Rey y el botin para quien lo mato).
 */
public final class Victoria {
    private Victoria() {}

    public static final int SUBIDA = 70;

    private record Tarea(BlockPos pos, int fin) {}

    private static final List<Tarea> TAREAS = new ArrayList<>();
    private static int tick;

    public static void ganar(ServerLevel sl, Rey rey, List<ServerPlayer> vivos, List<UUID> todos) {
        MinecraftServer server = sl.getServer();
        ServerLevel ow = server.overworld();
        Sala s = rey.sala();
        if (s == null) return;
        ReyEstado st = ReyEstado.get(server);
        st.set(s.key(), ReyEstado.MUERTO);
        st.ganadores(s.key(), todos);
        CiudadelaMod.LOGGER.info("[Ciudadela] El Rey ha caido ({} jugadores)", todos.size());
        if (Volcan.es(sl)) Volcan.devolverTodos(sl);
        Rey.abrirPuertas(ow, s);
        BlockPos chest = s.chest();
        // que no quede nada encima (alfombra)
        ow.setBlock(chest, Blocks.AIR.defaultBlockState(), 3);
        Net.Cofre m = new Net.Cofre(chest.getX(), chest.getY(), chest.getZ(), SUBIDA + 160);
        for (ServerPlayer p : ow.players()) if (p.distanceToSqr(chest.getX(), chest.getY(), chest.getZ()) < 120 * 120) Net.to(p, m);
        ow.playSound(null, chest, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 2.0f, 0.6f);
        ow.playSound(null, chest, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 2.0f, 0.5f);
        TAREAS.add(new Tarea(chest, tick + SUBIDA));
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        tick++;
        if (TAREAS.isEmpty()) return;
        ServerLevel ow = e.getServer().overworld();
        Iterator<Tarea> it = TAREAS.iterator();
        while (it.hasNext()) {
            Tarea t = it.next();
            BlockPos p = t.pos();
            int left = t.fin() - tick;
            if (left % 5 == 0)
                ow.sendParticles(Rey.polvoOro(), p.getX() + 0.5, p.getY() + 0.6, p.getZ() + 0.5, 12, 0.6, 0.8, 0.6, 0.02);
            if (left % 10 == 0) ow.playSound(null, p, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.5f, 0.6f + (SUBIDA - left) / 100f);
            if (left <= 0) {
                StoryEvents.placeChapter(ow, 3, p, Direction.SOUTH);
                ow.sendParticles(ParticleTypes.END_ROD, p.getX() + 0.5, p.getY() + 1, p.getZ() + 0.5, 40, 0.5, 1.0, 0.5, 0.08);
                ow.playSound(null, p, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.5f, 0.7f);
                it.remove();
            }
        }
    }
}
