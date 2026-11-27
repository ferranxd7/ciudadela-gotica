package com.darkcraft.client.armas;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.armas.FlechaEspecial;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Las flechas de fuego, de sangre y de escarcha en vuelo y clavadas (parte 7aj): la flecha del juego con la textura de su clase
 * ({@code textures/entity/flecha_<clase>.png}, de tools/rol/flechas.py, con el mismo reparto que la del juego). La de fuego va
 * ardiendo (la llama la pone el juego: va prendida).
 */
public class FlechaEspecialRenderer extends ArrowRenderer<FlechaEspecial> {
    private static final ResourceLocation[] TEX = new ResourceLocation[FlechaEspecial.Clase.values().length];

    static {
        for (FlechaEspecial.Clase c : FlechaEspecial.Clase.values())
            TEX[c.ordinal()] = new ResourceLocation(DarkCraftMod.MODID, "textures/entity/flecha_" + c.id + ".png");
    }

    public FlechaEspecialRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(FlechaEspecial e) {
        return TEX[e.clase().ordinal()];
    }
}
