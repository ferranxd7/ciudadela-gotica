package com.darkcraft.personajes;

import com.darkcraft.DarkCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Kerrick, el mercader ambulante: de pie junto a los fuegos con su mochila enorme (sombrero de ala ancha, manta enrollada,
 * una olla y un farol colgando). Vende con ecos ({@code tienda.Tienda}).
 */
public class Mercader extends PersonajeBase {
    public Mercader(EntityType<? extends Mercader> t, Level l) {
        super(t, l);
    }

    @Override
    public String dialogo() {
        return "mercader";
    }

    @Override
    protected float vista() {
        return 8f;
    }

    /** Pone a Kerrick en {@code pos} mirando hacia {@code mira}. */
    public static Mercader poner(ServerLevel lvl, BlockPos pos, BlockPos mira) {
        return poner(DarkCraftMod.MERCADER.get(), lvl, pos, mira, "darkcraft_mercader", null);
    }
}
