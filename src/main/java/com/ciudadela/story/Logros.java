package com.ciudadela.story;

import com.ciudadela.CiudadelaMod;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Los logros de la pestana "Ciudadela Gotica" (data/ciudadela/advancements). Se dan desde el codigo: los avisos
 * y el sonido los pone el propio Minecraft (fanfarria en los de desafio: el Rey y la Sala del Arbol).
 */
public final class Logros {
    private Logros() {}

    public static final String ENCONTRAR = "encontrar", CAPITULO1 = "capitulo1", REY = "rey", ARBOL = "arbol";

    public static void dar(ServerPlayer p, String id) {
        MinecraftServer srv = p.getServer();
        if (srv == null) return;
        Advancement a = srv.getAdvancements().getAdvancement(new ResourceLocation(CiudadelaMod.MODID, id));
        if (a == null) return;
        AdvancementProgress pr = p.getAdvancements().getOrStartProgress(a);
        if (pr.isDone()) return;
        List<String> faltan = new ArrayList<>();
        for (String c : pr.getRemainingCriteria()) faltan.add(c);
        for (String c : faltan) p.getAdvancements().award(a, c);
    }

    public static boolean tiene(ServerPlayer p, String id) {
        MinecraftServer srv = p.getServer();
        if (srv == null) return false;
        Advancement a = srv.getAdvancements().getAdvancement(new ResourceLocation(CiudadelaMod.MODID, id));
        return a != null && p.getAdvancements().getOrStartProgress(a).isDone();
    }
}
