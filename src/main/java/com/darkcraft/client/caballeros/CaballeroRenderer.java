package com.darkcraft.client.caballeros;

import com.darkcraft.DarkCraftMod;
import com.darkcraft.caballeros.CaballeroZombi;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Los tres caballeros zombi: el mismo modelo con el tabardo de su color (rojo, morado o verde). */
public class CaballeroRenderer extends HumanoidMobRenderer<CaballeroZombi, CaballeroModelo> {
    private static final ResourceLocation ESPADA = tex("espada"), ALABARDA = tex("alabarda"), BALLESTA = tex("ballesta");

    private static ResourceLocation tex(String n) {
        return new ResourceLocation(DarkCraftMod.MODID, "textures/entity/caballero/" + n + ".png");
    }

    public CaballeroRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new CaballeroModelo(ctx.bakeLayer(CaballeroModelo.CAPA)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(CaballeroZombi e) {
        return switch (e.tipo()) {
            case "caballero_alabarda" -> ALABARDA;
            case "caballero_ballesta" -> BALLESTA;
            default -> ESPADA;
        };
    }
}
